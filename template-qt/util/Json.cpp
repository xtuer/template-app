#include "Json.h"

#include <QDebug>
#include <QFile>
#include <QTextStream>
#include <QRegularExpression>
#include <QJsonParseError>

/*-----------------------------------------------------------------------------|
 |                         JsonPrivate implementation                          |
 |----------------------------------------------------------------------------*/
struct JsonPrivate {
    JsonPrivate(const QString &jsonOrJsonFilePath, bool fromFile);

    void setValue(QJsonObject &parent, const QString &path, const QJsonValue &newValue);
    QJsonValue getValue(const QString &path, const QJsonObject &fromNode) const;

    QJsonObject root;    // Json 的根节点
    QJsonDocument doc;   // Json 的文档对象
    bool valid = true;   // Json 是否有效
    QString errorString; // Json 无效时的错误信息
};

JsonPrivate::JsonPrivate(const QString &jsonOrJsonFilePath, bool fromFile) {
    QByteArray json("{}"); // json 的内容

    // 如果传人的是 Json 文件的路径，则读取内容
    if (fromFile) {
        QFile file(jsonOrJsonFilePath);

        if (file.open(QIODevice::ReadOnly | QIODevice::Text)) {
            json = file.readAll();
        } else {
            valid = false;
            errorString = QString("Cannot open the file: %1").arg(jsonOrJsonFilePath);
            qDebug() << errorString;
            return;
        }
    } else {
        json = jsonOrJsonFilePath.toUtf8();
    }

    // 解析 Json
    QJsonParseError error;
    doc = QJsonDocument::fromJson(json, &error);

    if (QJsonParseError::NoError == error.error) {
        root = doc.object();
    } else {
        valid = false;
        errorString = QString("%1\nOffset: %2").arg(error.errorString()).arg(error.offset);
        qDebug() << errorString;
    }
}

// 使用递归+引用设置 Json 的值，因为 toObject() 等返回的是对象的副本，对其修改不会改变原来的对象，所以需要用引用来实现
void JsonPrivate::setValue(QJsonObject &parent, const QString &path, const QJsonValue &newValue) {
    const int indexOfDot   = path.indexOf('.');     // 第一个 . 的位置
    const QString property = path.left(indexOfDot); // 第一个 . 之前的内容，如果 indexOfDot 是 -1 则返回整个字符串
    const QString restPath = (indexOfDot>0) ? path.mid(indexOfDot+1) : QString(); // 第一个 . 后面的内容

    QJsonValue fieldValue = parent[property];

    if(restPath.isEmpty()) {
        // 找到要设置的属性
        fieldValue = newValue;
    } else {
        // 路径中间的属性，递归访问它的子属性
        QJsonObject obj = fieldValue.toObject();
        setValue(obj, restPath, newValue);
        fieldValue = obj; // 因为 QJsonObject 操作的都是对象的副本，所以递归结束后需要保存起来再次设置回 parent
    }

    parent[property] = fieldValue; // 如果不存在则会创建
}

// 读取属性的值，如果 fromNode 为空，则从跟节点开始访问
QJsonValue JsonPrivate::getValue(const QString &path, const QJsonObject &fromNode) const {
    // 1. 确定搜索的根节点，如果 fromNode 为空则搜索的根节点为 root
    // 2. 把 path 使用分隔符 . 分解成多个属性名字
    // 3. 从搜索的根节点开始向下查找到倒数第二个属性名字对应的 QJsonObject parent
    //    如 "user.address.street"，要设置的属性为 street，它的 parent 是 address
    // 4. 返回 parent 中属性名为倒数第一个属性名字对应的属性值

    // [1] 确定搜索的根节点，如果 fromNode 为空则搜索的根节点为 root
    // [2] 把 path 使用分隔符 . 分解成多个属性名字
    QJsonObject parent = fromNode.isEmpty() ? root : fromNode;
    QStringList names  = path.split(QRegularExpression("\\."));

    // [3] 从搜索的根节点开始向下查找到倒数第二个属性名字对应的 QJsonObject parent
    int size = names.size();
    for (int i = 0; i < size - 1; ++i) {
        if (parent.isEmpty()) {
            return QJsonValue();
        }

        parent = parent.value(names.at(i)).toObject();
    }

    // [4] 返回 parent 中属性名为倒数第一个属性名字对应的属性值
    return parent.value(names.last());
}

/*-----------------------------------------------------------------------------|
 |                             Json implementation                             |
 |----------------------------------------------------------------------------*/
Json::Json(const QString &jsonOrJsonFilePath, bool fromFile) : d(new JsonPrivate(jsonOrJsonFilePath, fromFile)) {
}

Json::~Json() {
    delete d;
}

// JSON 是否有效，有效的 JSON 返回 true，否则返回 false
bool Json::isValid() const {
    return d->valid;
}

// JSON 无效时的错误信息
QString Json::errorString() const {
    return d->errorString;
}

int Json::getInt(const QString &path, int def, const QJsonObject &fromNode) const {
    return getJsonValue(path, fromNode).toInt(def);
}

bool Json::getBool(const QString &path, bool def, const QJsonObject &fromNode) const {
    return getJsonValue(path, fromNode).toBool(def);
}

double Json::getDouble(const QString &path, double def, const QJsonObject &fromNode) const {
    return getJsonValue(path, fromNode).toDouble(def);
}

QString Json::getString(const QString &path, const QString &def, const QJsonObject &fromNode) const {
    return getJsonValue(path, fromNode).toString(def);
}

QStringList Json::getStringList(const QString &path, const QJsonObject &fromNode) const {
    QStringList result;
    QJsonArray array = getJsonValue(path, fromNode).toArray();

    for (QJsonArray::const_iterator iter = array.begin(); iter != array.end(); ++iter) {
        QJsonValue value = *iter;
        result << value.toString();
    }

    return result;
}

QJsonArray Json::getJsonArray(const QString &path, const QJsonObject &fromNode) const {
    // 如果根节点是数组时特殊处理
    if (("." == path || "" == path) && fromNode.isEmpty()) {
        return d->doc.array();
    }

    return getJsonValue(path, fromNode).toArray();
}

QJsonObject Json::getJsonObject(const QString &path, const QJsonObject &fromNode) const {
    return getJsonValue(path, fromNode).toObject();
}

QJsonValue Json::getJsonValue(const QString &path, const QJsonObject &fromNode) const {
    return d->getValue(path, fromNode);
}


void Json::set(const QString &path, const QJsonValue &value) {
    d->setValue(d->root, path, value);
}

void Json::set(const QString &path, const QStringList &strings) {
    QJsonArray array;

    for (const QString &str : strings) {
        array.append(str);
    }

    d->setValue(d->root, path, array);
}

// 把 JSON 保存到 path 指定的文件
void Json::save(const QString &path, bool pretty) const {
    QFile file(path);

    if (!file.open(QIODevice::WriteOnly | QIODevice::Truncate | QIODevice::Text)) {
        return;
    }

    QTextStream out(&file);
    out << toString(pretty);
    out.flush();
    file.close();
}

// 把 Json 对象转换为 JSON 字符串
QString Json::toString(bool pretty) const {
    return QJsonDocument(d->root).toJson(pretty ? QJsonDocument::Indented : QJsonDocument::Compact);
}
