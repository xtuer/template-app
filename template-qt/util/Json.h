#ifndef JSON_H
#define JSON_H

#include <QJsonArray>
#include <QJsonValue>
#include <QJsonObject>
#include <QJsonDocument>

struct JsonPrivate;

/**
 * Qt 的 JSON API 读写多层次的属性不够方便，这个类的目的就是能够使用带 "." 的路径格式访问 Json 的属性，例如
 * "id" 访问的是根节点下的 id，"user.address.street" 访问根节点下 user 的 address 的 street 的属性。
 *
 * JSON 例子 (JSON 的 key 必须用双引号括起来，值有不同的类型，数值类型不用双引号括起来，字符类型的才用)：
 * {
 *     "id": 18191,
 *     "user": {
 *         "address": {
 *             "street": "Wiessenstrasse",
 *             "postCode": "100001"
 *         },
 *         "childrenNames": ["Alice", "Bob", "John"]
 *     }
 * }
 *
 * 创建 Json 对象: Json json(jsonString) or Json json(jsonFilePath, true)
 * 保存 Json 对象到文件: json.save("xxx.json")
 *
 * 访问 id:     json.getInt("id")，返回 18191
 * 访问 street: json.getString("user.address.street")，返回 "Wiessenstrasse"
 * 访问 childrenNames: json.getStringList("user.childrenNames") 得到字符串列表("Alice", "Bob", "John")
 * 设置 "user.address.postCode" 则可以使用 json.set("user.address.postCode", "056231")
 * 如果根节点是数组，则使用 json.getJsonArray(".") 获取
 *
 * 如果读取的属性不存在，则返回指定的默认值，如 "database.username.firstName" 不存在，
 * 调用 json.getString("database.username.firstName", "defaultName")，由于要访问的属性不存在，
 * 得到的是一个空的 QJsonValue，所以返回我们指定的默认值 "defaultName"。
 *
 * 如果要修改的属性不存在，则会自动的先创建属性，然后设置它的值。
 *
 * 注意: JSON 文件要使用 UTF-8 编码。
 */
class Json {
public:
    /**
     * 使用 JSON 字符串或者从文件读取 JSON 内容创建 Json 对象。
     * 如果 fromFile 为 true， 则 jsonOrJsonFilePath 为 JSON 文件的路径
     * 如果 fromFile 为 false，则 jsonOrJsonFilePath 为 JSON 的字符串内容
     *
     * @param jsonOrJsonFilePath JSON 的字符串内容或者 JSON 文件的路径
     * @param fromFile 为 true，则 jsonOrJsonFilePath 为 JSON 文件的路径，为 false 则 jsonOrJsonFilePath 为 JSON 的字符串内容
     */
    explicit Json(const QString &jsonOrJsonFilePath = "{}", bool fromFile = false);
    ~Json();

    bool isValid() const;        // JSON 是否有效，有效的 JSON 返回 true，否则返回 false
    QString errorString() const; // JSON 无效时的错误信息

    /**
     * 读取路径 path 对应属性的整数值
     *
     * @param path 带 "." 的路径格
     * @param def 如果要找的属性不存在时返回的默认值
     * @param fromNode 从此节点开始查找，如果为默认值 QJsonObject()，则从 Json 的根节点开始查找
     * @return 整数值
     */
    int         getInt(const QString &path, int def = 0, const QJsonObject &fromNode = QJsonObject()) const;
    bool        getBool(const QString &path, bool def = false, const QJsonObject &fromNode = QJsonObject()) const;
    double      getDouble(const QString &path, double def = 0.0, const QJsonObject &fromNode = QJsonObject()) const;
    QString     getString(const QString &path, const QString &def = QString(), const QJsonObject &fromNode = QJsonObject()) const;
    QStringList getStringList(const QString &path, const QJsonObject &fromNode = QJsonObject()) const;

    QJsonArray  getJsonArray( const QString &path, const QJsonObject &fromNode = QJsonObject()) const;
    QJsonValue  getJsonValue( const QString &path, const QJsonObject &fromNode = QJsonObject()) const;
    QJsonObject getJsonObject(const QString &path, const QJsonObject &fromNode = QJsonObject()) const;

    /**
     * @brief 设置 path 对应的 Json 属性的值
     * @param path  path 带 "." 的路径格
     * @param value 可以是整数，浮点数，字符串，QJsonValue, QJsonObject 等，具体请参考 QJsonValue 的构造函数
     */
    void set(const QString &path, const QJsonValue &value);
    void set(const QString &path, const QStringList &strings);

    /**
     * @brief 把 JSON 保存到 path 指定的文件
     *
     * @param path 文件的路径
     * @param pretty 为 true 时格式化 JSON 字符串，为 false 则使用压缩格式去掉多余的空白字符
     */
    void save(const QString &path, bool pretty = true) const;

    /**
     * @brief 把 Json 对象转换为 JSON 字符串
     * @param pretty 为 true 时格式化 JSON 字符串，为 false 则使用压缩格式去掉多余的空白字符
     * @return Json 对象的字符串表示
     */
    QString toString(bool pretty = true) const;

public:
    JsonPrivate *d;
};

#endif // JSON_H
