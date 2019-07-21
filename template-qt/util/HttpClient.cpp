#include "HttpClient.h"

#include <QDebug>
#include <QFile>
#include <QHash>
#include <QUrlQuery>
#include <QHttpPart>
#include <QHttpMultiPart>

/*-----------------------------------------------------------------------------|
 |                              HttpClientPrivate                              |
 |----------------------------------------------------------------------------*/
/**
 * @brief 请求的类型
 *
 * 注: UPLOAD 不是 HTTP Method，只是为了上传时对请求进行特殊处理而定义的
 */
enum class HttpClientRequestMethod {
    GET, POST, PUT, DELETE, UPLOAD
};

/**
 * @brief 缓存 HttpClientPrivate 的数据成员，方便在异步 lambda 中使用 = 以值的方式访问。
 */
class HttpClientPrivateCache {
public:
    std::function<void (const QString &)>      successHandler = nullptr;
    std::function<void (const QString &, int)>    failHandler = nullptr;
    std::function<void ()>                    completeHandler = nullptr;
    bool debug    = false;
    bool internal = false;
    QString charset;
    QNetworkAccessManager* manager = nullptr;
};

/**
 * @brief HttpClient 的辅助类，封装不希望暴露给客户端的数据和方法，使得 HttpClient 只暴露必要的 API 给客户端。
 */
class HttpClientPrivate {
    friend class HttpClient;

    HttpClientPrivate(const QString &url);
    ~HttpClientPrivate();

    /**
     * @brief 缓存 HttpClientPrivate 的数据成员
     *
     * @return 返回 HttpClientPrivateCache 缓存对象
     */
    HttpClientPrivateCache cache();

    /**
     * @brief 获取 Manager，如果传入了 manager 则返回此 manager，否则新创建一个 manager，默认会自动创建一个 manager，
     *        使用传入的 manager 则 interval 被设置为 false，自动创建的 manager 则设置 interval 为 true
     *
     * @return 返回 QNetworkAccessManager 对象
     */
    QNetworkAccessManager* getManager();

    /**
     * @brief 使用用户设定的 URL、请求头、参数等创建 Request
     *
     * @param d      HttpClientPrivate 的对象
     * @param method 请求的类型
     * @return 返回可用于执行请求的 QNetworkRequest
     */
    static QNetworkRequest createRequest(HttpClientPrivate *d, HttpClientRequestMethod method);

    /**
     * @brief 执行请求的辅助函数
     *
     * @param d      HttpClientPrivate 的对象
     * @param method 请求的类型
     */
    static void executeQuery(HttpClientPrivate *d, HttpClientRequestMethod method);

    /**
     * @brief 上传文件或者数据
     *
     * @param d     HttpClientPrivate 的对象
     * @param paths 要上传的文件的路径(path 和 data 不能同时使用)
     * @param data  要上传的文件的数据
     */
    static void upload(HttpClientPrivate *d, const QStringList &paths, const QByteArray &data);

    /**
     * @brief 使用 GET 进行下载，下载的文件保存到 savePath
     *
     * @param d        HttpClientPrivate 的对象
     * @param savePath 下载的文件保存路径
     */
    static void download(HttpClientPrivate *d, const QString &savePath);

    /**
     * @brief 使用 GET 进行下载，当有数据可读取时回调 readyRead(), 大多数情况下应该在 readyRead() 里把数据保存到文件
     *
     * @param readyRead 有数据可读取时的回调 lambda 函数
     */
    static void download(HttpClientPrivate *d, std::function<void (const QByteArray &)> readyRead);

    /**
     * @brief 读取服务器响应的数据
     *
     * @param reply   请求的 QNetworkReply 对象
     * @param charset 请求响应的字符集，默认使用 UTF-8
     * @return 返回服务器端响应的字符串
     */
    static QString readReply(QNetworkReply *reply, const QString &charset = "UTF-8");

    /**
     * @brief 请求结束的处理函数
     *
     * @param cache          HttpClientPrivateCache 缓存对象
     * @param reply          QNetworkReply 对象，不能为 NULL
     * @param successMessage 请求成功的消息
     * @param failMessage    请求失败的消息
     */
    static void handleFinish(HttpClientPrivateCache cache, QNetworkReply *reply, const QString &successMessage, const QString &failMessage);

    /////////////////////////////////////////////////// 成员变量 //////////////////////////////////////////////
    QString   url;                            // 请求的 URL
    QString   json;                           // 请求的参数使用 Json 格式
    QUrlQuery params;                         // 请求的参数使用 Form 格式
    QString   charset = "UTF-8";              // 请求响应的字符集
    QHash<QString, QString> headers;          // 请求头
    QNetworkAccessManager *manager = nullptr; // 执行 HTTP 请求的 QNetworkAccessManager 对象
    bool useJson  = false;                    // 为 true 时请求使用 Json 格式传递参数，否则使用 Form 格式传递参数
    bool debug    = false;                    // 为 true 时输出请求的 URL 和参数
    bool internal = true;                     // 是否使用自动创建的 manager

    std::function<void (const QString &)>   successHandler = nullptr; // 成功的回调函数，参数为响应的字符串
    std::function<void (const QString &, int)> failHandler = nullptr; // 失败的回调函数，参数为失败原因和 HTTP status code
    std::function<void ()>                 completeHandler = nullptr; // 结束的回调函数，无参数
};

HttpClientPrivate::HttpClientPrivate(const QString &url) : url(url) { }

HttpClientPrivate::~HttpClientPrivate() {
    manager         = nullptr;
    successHandler  = nullptr;
    failHandler     = nullptr;
    completeHandler = nullptr;
}

// 缓存 HttpClientPrivate 的数据成员
HttpClientPrivateCache HttpClientPrivate::cache() {
    HttpClientPrivateCache cache;

    cache.successHandler  = successHandler;
    cache.failHandler     = failHandler;
    cache.completeHandler = completeHandler;
    cache.debug    = debug;
    cache.internal = internal;
    cache.charset  = charset;
    cache.manager  = getManager();

    return cache;
}

// 执行请求的辅助函数
void HttpClientPrivate::executeQuery(HttpClientPrivate *d, HttpClientRequestMethod method) {
    // 1. 缓存需要的变量，在 lambda 中使用 = 捕获进行值传递 (不能使用引用 &，因为 d 已经被析构)
    // 2. 创建请求需要的变量
    // 3. 根据 method 执行不同的请求
    // 4. 请求结束时获取响应数据，在 handleFinish 中执行回调函数

    // [1] 缓存需要的变量，在 lambda 中使用 = 捕获进行值传递 (不能使用引用 &，因为 d 已经被析构)
    HttpClientPrivateCache cache = d->cache();

    // [2] 创建请求需要的变量
    QNetworkRequest request = HttpClientPrivate::createRequest(d, method);
    QNetworkReply    *reply = nullptr;

    // [3] 根据 method 执行不同的请求
    switch (method) {
    case HttpClientRequestMethod::GET:
        reply = cache.manager->get(request);
        break;
    case HttpClientRequestMethod::POST:
        reply = cache.manager->post(request, d->useJson ? d->json.toUtf8() : d->params.toString(QUrl::FullyEncoded).toUtf8());
        break;
    case HttpClientRequestMethod::PUT:
        reply = cache.manager->put(request, d->useJson ? d->json.toUtf8() : d->params.toString(QUrl::FullyEncoded).toUtf8());
        break;
    case HttpClientRequestMethod::DELETE:
        reply = cache.manager->deleteResource(request);
        break;
    default:
        break;
    }

    // [4] 请求结束时获取响应数据，在 handleFinish 中执行回调函数
    // 请求结束时一次性读取所有响应数据
    QObject::connect(reply, &QNetworkReply::finished, [=] {
        QString successMessage = HttpClientPrivate::readReply(reply, cache.charset.toUtf8());
        QString failMessage    = reply->errorString();
        HttpClientPrivate::handleFinish(cache, reply, successMessage, failMessage);
    });
}

// 使用 GET 进行下载，下载的文件保存到 savePath
void HttpClientPrivate::download(HttpClientPrivate *d, const QString &savePath) {
    // 1. 打开下载文件，如果打开文件出错，不进行下载
    // 2. 给请求结束的回调函数注入关闭释放文件的行为
    // 3. 调用下载的重载函数开始下载
    QFile *file = new QFile(savePath);

    // [1] 打开下载文件，如果打开文件出错，不进行下载
    if (!file->open(QIODevice::WriteOnly | QIODevice::Truncate)) {
        file->close();
        file->deleteLater();

        if (d->debug) {
            qDebug().noquote() << QString("[错误] 打开文件出错: %1").arg(savePath);
        }

        if (nullptr != d->failHandler) {
            d->failHandler(QString("[错误] 打开文件出错: %1").arg(savePath), -1);
        }

        return;
    }

    // [2] 给请求结束的回调函数注入关闭释放文件的行为
    std::function<void ()> userCompleteHandler     = d->completeHandler;
    std::function<void ()> injectedCompleteHandler = [=]() {
        // 请求结束后释放文件对象
        file->flush();
        file->close();
        file->deleteLater();

        // 执行用户指定的结束回调函数
        if (nullptr != userCompleteHandler) {
            userCompleteHandler();
        }
    };
    d->completeHandler = injectedCompleteHandler;

    // [3] 调用下载的重载函数开始下载
    HttpClientPrivate::download(d, [=](const QByteArray &data) {
        file->write(data);
    });
}

// 使用 GET 进行下载，当有数据可读取时回调 readyRead(), 大多数情况下应该在 readyRead() 里把数据保存到文件
void HttpClientPrivate::download(HttpClientPrivate *d, std::function<void (const QByteArray &)> readyRead) {
    // 1. 缓存需要的变量，在 lambda 中使用 = 捕获进行值传递 (不能使用引用 &，因为 d 已经被析构)
    // 2. 创建请求需要的变量，执行请求
    // 3. 有数据可读取时回调 readyRead()
    // 4. 请求结束时获取响应数据，在 handleFinish 中执行回调函数

    // [1] 缓存需要的变量，在 lambda 中使用 = 捕捉使用 (不能使用引用 &，因为 d 已经被析构)
    HttpClientPrivateCache cache = d->cache();

    // [2] 创建请求需要的变量，执行请求
    QNetworkRequest request = HttpClientPrivate::createRequest(d, HttpClientRequestMethod::GET);
    QNetworkReply    *reply = cache.manager->get(request);

    // [3] 有数据可读取时回调 readyRead()
    QObject::connect(reply, &QNetworkReply::readyRead, [=] {
        readyRead(reply->readAll());
    });

    // [4] 请求结束时获取响应数据，在 handleFinish 中执行回调函数
    QObject::connect(reply, &QNetworkReply::finished, [=] {
        QString successMessage = "下载完成"; // 请求结束时一次性读取所有响应数据
        QString failMessage    = reply->errorString();
        HttpClientPrivate::handleFinish(cache, reply, successMessage, failMessage);
    });
}

// 上传文件或者数据的实现
void HttpClientPrivate::upload(HttpClientPrivate *d, const QStringList &paths, const QByteArray &data) {
    // 1. 缓存需要的变量，在 lambda 中使用 = 捕获进行值传递 (不能使用引用 &，因为 d 已经被析构)
    // 2. 创建 Form 表单的参数 Text Part
    // 3. 创建上传的 File Part
    //    3.1 使用文件创建 File Part
    //    3.2 使用数据创建 File Part
    // 4. 创建请求需要的变量，执行请求
    // 5. 请求结束时释放 multiPart 和打开的文件，获取响应数据，在 handleFinish 中执行回调函数

    // [1] 缓存需要的变量，在 lambda 中使用 = 捕捉使用 (不能使用引用 &，因为 d 已经被析构)
    HttpClientPrivateCache cache = d->cache();

    // [2] 创建 Form 表单的参数 Text Part
    QHttpMultiPart *multiPart = new QHttpMultiPart(QHttpMultiPart::FormDataType);
    QList<QPair<QString, QString> > paramItems = d->params.queryItems();
    for (int i = 0; i < paramItems.size(); ++i) {
        QString name  = paramItems.at(i).first;
        QString value = paramItems.at(i).second;

        QHttpPart textPart;
        textPart.setHeader(QNetworkRequest::ContentDispositionHeader, QString("form-data; name=\"%1\"").arg(name));
        textPart.setBody(value.toUtf8());
        multiPart->append(textPart);
    }

    if (paths.size() > 0) {
        // [3.1] 使用文件创建 File Part
        QString inputName = paths.size() == 1 ? "file" : "files"; // 一个文件时为 file，多个文件时为 files

        for (const QString &path : paths) {
            // path 为空时，不上传文件
            if (path.isEmpty()) {
                continue;
            }

            // We cannot delete the file now, so delete it with the multiPart
            QFile *file = new QFile(path, multiPart);

            // 如果文件打开失败，则释放资源返回，终止上传
            if(!file->open(QIODevice::ReadOnly)) {
                QString failMessage = QString("打开文件失败[%2]: %1").arg(path).arg(file->errorString());

                if (cache.debug) {
                    qDebug().noquote() << failMessage;
                }

                if (nullptr != cache.failHandler) {
                    cache.failHandler(failMessage, -1);
                }

                multiPart->deleteLater();
                return;
            }

            // 单个文件时，name 为服务器端获取文件的参数名，为 file
            // 多个文件时，name 为服务器端获取文件的参数名，为 files
            // 注意: 服务器是 Java 的则用 form-data
            // 注意: 服务器是 PHP  的则用 multipart/form-data
            QString disposition = QString("form-data; name=\"%1\"; filename=\"%2\"").arg(inputName).arg(file->fileName());
            QHttpPart filePart;
            filePart.setHeader(QNetworkRequest::ContentDispositionHeader, QVariant(disposition));
            filePart.setBodyDevice(file);
            multiPart->append(filePart);
        }
    } else {
        // [3.2] 使用数据创建 File Part
        QString   disposition = QString("form-data; name=\"file\"; filename=\"no-name\"");
        QHttpPart dataPart;
        dataPart.setHeader(QNetworkRequest::ContentDispositionHeader, QVariant(disposition));
        dataPart.setBody(data);
        multiPart->append(dataPart);
    }

    // [4] 创建请求需要的变量，执行请求
    QNetworkRequest request = HttpClientPrivate::createRequest(d, HttpClientRequestMethod::UPLOAD);
    QNetworkReply    *reply = cache.manager->post(request, multiPart);

    // [5] 请求结束时释放 multiPart 和文件，获取响应数据，在 handleFinish 中执行回调函数
    QObject::connect(reply, &QNetworkReply::finished, [=] {
        multiPart->deleteLater(); // 释放资源: multiPart + file

        QString successMessage = HttpClientPrivate::readReply(reply, cache.charset); // 请求结束时一次性读取所有响应数据
        QString failMessage    = reply->errorString();
        HttpClientPrivate::handleFinish(cache, reply, successMessage, failMessage);
    });
}

// 获取 Manager，如果传入了 manager 则返回此 manager，否则新创建一个 manager，默认会自动创建一个 manager
QNetworkAccessManager* HttpClientPrivate::getManager() {
    return internal ? new QNetworkAccessManager() : manager;
}

// 使用用户设定的 URL、请求头、参数等创建 Request
QNetworkRequest HttpClientPrivate::createRequest(HttpClientPrivate *d, HttpClientRequestMethod method) {
    // 1. 如果是 GET 请求，并且参数不为空，则编码请求的参数，放到 URL 后面
    // 2. 调试时输出网址和参数
    // 3. 设置 Content-Type
    // 4. 添加请求头到 request 中

    bool get      = method == HttpClientRequestMethod::GET;
    bool upload   = method == HttpClientRequestMethod::UPLOAD;
    bool withForm = !get && !upload && !d->useJson; // PUT、POST 或者 DELETE 请求，且 useJson 为 false
    bool withJson = !get && !upload &&  d->useJson; // PUT、POST 或者 DELETE 请求，且 useJson 为 true

    // [1] 如果是 GET 请求，并且参数不为空，则编码请求的参数，放到 URL 后面
    if (get && !d->params.isEmpty()) {
        d->url += "?" + d->params.toString(QUrl::FullyEncoded);
    }

    // [2] 调试时输出网址和参数
    if (d->debug) {
        qDebug().noquote() << "[网址]" << d->url;

        if (withJson) {
            qDebug().noquote() << "[参数]" << d->json;
        } else if (withForm || upload) {
            QList<QPair<QString, QString> > paramItems = d->params.queryItems();
            QString buffer; // 避免多次调用 qDebug() 输入调试信息，每次 qDebug() 都有可能输出行号等

            // 按键值对的方式输出参数
            for (int i = 0; i < paramItems.size(); ++i) {
                QString name  = paramItems.at(i).first;
                QString value = paramItems.at(i).second;

                if (0 == i) {
                    buffer += QString("[参数] %1=%2\n").arg(name).arg(value);
                } else {
                    buffer += QString("       %1=%2\n").arg(name).arg(value);
                }
            }

            if (!buffer.isEmpty()) {
                qDebug().noquote() << buffer;
            }
        }
    }

    // [3] 设置 Content-Type
    // 如果是 POST 请求，useJson 为 true 时添加 Json 的请求头，useJson 为 false 时添加 Form 的请求头
    if (withForm) {
        d->headers["Content-Type"] = "application/x-www-form-urlencoded";
    } else if (withJson) {
        d->headers["Content-Type"] = "application/json; charset=utf-8";
    }

    // [4] 添加请求头到 request 中
    QNetworkRequest request(QUrl(d->url));
    for (auto i = d->headers.cbegin(); i != d->headers.cend(); ++i) {
        request.setRawHeader(i.key().toUtf8(), i.value().toUtf8());
    }

    return request;
}

// 读取服务器响应的数据
QString HttpClientPrivate::readReply(QNetworkReply *reply, const QString &charset) {
    QTextStream in(reply);
    QString result;
    in.setCodec(charset.toUtf8());

    while (!in.atEnd()) {
        result += in.readLine();
    }

    return result;
}

// 请求结束的处理函数
void HttpClientPrivate::handleFinish(HttpClientPrivateCache cache, QNetworkReply *reply, const QString &successMessage, const QString &failMessage) {
    // 1. 执行请求成功的回调函数
    // 2. 执行请求失败的回调函数
    // 3. 执行请求结束的回调函数
    // 4. 释放 reply 和 manager 对象

    if (reply->error() == QNetworkReply::NoError) {
        if (cache.debug) {
            qDebug().noquote() << QString("[结束] 成功: %1").arg(successMessage);
        }

        // [1] 执行请求成功的回调函数
        if (nullptr != cache.successHandler) {
            cache.successHandler(successMessage);
        }
    } else {
        if (cache.debug) {
            qDebug().noquote() << QString("[结束] 失败: %1").arg(failMessage);
        }

        // [2] 执行请求失败的回调函数
        if (nullptr != cache.failHandler) {
            cache.failHandler(failMessage, reply->error());
        }
    }

    // [3] 执行请求结束的回调函数
    if (nullptr != cache.completeHandler) {
        cache.completeHandler();
    }

    // [4] 释放 reply 和 manager 对象
    if (nullptr != reply) {
        reply->deleteLater();
    }

    if (cache.internal && nullptr != cache.manager) {
        cache.manager->deleteLater();
    }
}

/*-----------------------------------------------------------------------------|
 |                                 HttpClient                                  |
 |----------------------------------------------------------------------------*/

// 注意: 在异步请求中 HttpClient 的 HttpClientPrivate 成员变量 d 已经被析构，所以需要先缓存相关变量为栈对象，使用 = 以值的方式访问
HttpClient::HttpClient(const QString &url) : d(new HttpClientPrivate(url)) { }

HttpClient::~HttpClient() {
    delete d;
}

// 传入 QNetworkAccessManager 给多个请求共享
HttpClient& HttpClient::manager(QNetworkAccessManager *manager) {
    d->manager  = manager;
    d->internal = (nullptr == manager);

    return *this;
}

// 传入 debug 为 true 则使用 debug 模式，请求执行时输出请求的 URL 和参数等
HttpClient& HttpClient::debug(bool debug) {
    d->debug = debug;

    return *this;
}

// 添加一个请求的参数，可以多次调用添加多个参数
HttpClient& HttpClient::param(const QString &name, const QVariant &value) {
    d->params.addQueryItem(name, value.toString());

    return *this;
}

// 添加多个请求的参数
HttpClient& HttpClient::params(const QMap<QString, QVariant> &ps) {
    for (auto iter = ps.cbegin(); iter != ps.cend(); ++iter) {
        d->params.addQueryItem(iter.key(), iter.value().toString());
    }

    return *this;
}

// 添加请求的参数 (请求体)，使用 Json 格式，例如 "{\"name\": \"Alice\"}"
HttpClient& HttpClient::json(const QString &json) {
    d->json    = json;
    d->useJson = true;

    return *this;
}

// 添加请求头
HttpClient& HttpClient::header(const QString &name, const QString &value) {
    d->headers[name] = value;

    return *this;
}

// 添加多个请求头
HttpClient& HttpClient::headers(const QMap<QString, QString> nameValues) {
    for (auto i = nameValues.cbegin(); i != nameValues.cend(); ++i) {
        d->headers[i.key()] = i.value();
    }

    return *this;
}

// 注册请求成功的回调函数
HttpClient& HttpClient::success(std::function<void (const QString &)> successHandler) {
    d->successHandler = successHandler;

    return *this;
}

// 注册请求失败的回调函数
HttpClient& HttpClient::fail(std::function<void (const QString &, int)> failHandler) {
    d->failHandler = failHandler;

    return *this;
}

// 注册请求结束的回调函数，不管成功还是失败都会执行
HttpClient& HttpClient::complete(std::function<void ()> completeHandler) {
    d->completeHandler = completeHandler;

    return *this;
}

// 设置请求响应的编码
HttpClient& HttpClient::charset(const QString &cs) {
    d->charset = cs;

    return *this;
}

// 执行 GET 请求
void HttpClient::get() {
    HttpClientPrivate::executeQuery(d, HttpClientRequestMethod::GET);
}

// 执行 POST 请求
void HttpClient::post() {
    HttpClientPrivate::executeQuery(d, HttpClientRequestMethod::POST);
}

// 执行 PUT 请求
void HttpClient::put() {
    HttpClientPrivate::executeQuery(d, HttpClientRequestMethod::PUT);
}

// 执行 DELETE 请求
void HttpClient::remove() {
    HttpClientPrivate::executeQuery(d, HttpClientRequestMethod::DELETE);
}

// 使用 GET 进行下载，下载的文件保存到 savePath
void HttpClient::download(const QString &savePath) {
    HttpClientPrivate::download(d, savePath);
}

// 上传文件
void HttpClient::upload(const QString &path) {
    QStringList paths = { path };
    HttpClientPrivate::upload(d, paths, QByteArray());
}

// 上传文件，文件的内容以及读取到 data 中
void HttpClient::upload(const QByteArray &data) {
    HttpClientPrivate::upload(d, QStringList(), data);
}

// 上传多个文件
void HttpClient::upload(const QStringList &paths) {
    HttpClientPrivate::upload(d, paths, QByteArray());
}
