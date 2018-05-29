#include "HttpClient.h"

#include <QDebug>
#include <QStringList>
#include <QFile>
#include <QHash>
#include <QUrlQuery>
#include <QNetworkReply>
#include <QNetworkRequest>
#include <QNetworkAccessManager>
#include <QHttpPart>
#include <QHttpMultiPart>

class HttpClientPrivate {
public:
    HttpClientPrivate(const QString &url);

    QString   url;    // 请求的 URL
    QUrlQuery params; // 请求的参数使用 Form 格式
    QString   json;   // 请求的参数使用 Json 格式
    QHash<QString, QString> headers; // 请求头
    QNetworkAccessManager  *manager;

    bool useJson; // 为 true 时请求使用 Json 格式传递参数，否则使用 Form 格式传递参数
    bool debug;   // 为 true 时输出请求的 URL 和参数

    // HTTP 请求的类型
    enum HttpMethod {
        GET, POST, PUT, DELETE, UPLOAD /* UPLOAD 不是 HTTP Method，只是为了上传时特殊处理而定义的 */
    };

    /**
     * @brief 获取 Manager，如果使用传入的 manager 则返回此 manager，否则新创建一个 manager
     * @param d        HttpClientPrivate 的对象
     * @param internal 使用传入的 manager 则 interval 被设置为 false，创建新的 manager 则设置 interval 为 true
     * @return 返回 QNetworkAccessManager 对象
     */
    static QNetworkAccessManager* getManager(HttpClientPrivate *d, bool *internal);

    /**
     * @brief 使用用户设定的 URL、请求头等创建 Request
     * @param d      HttpClientPrivate 的对象
     * @param method 请求的类型
     * @return 返回可用于执行请求的 QNetworkRequest
     */
    static QNetworkRequest createRequest(HttpClientPrivate *d, HttpMethod method);

    /**
     * @brief 执行请求的辅助函数
     * @param d              HttpClient 的辅助对象
     * @param method         请求的类型
     * @param successHandler 请求成功的回调 lambda 函数
     * @param errorHandler   请求失败的回调 lambda 函数
     * @param encoding       请求响应的编码
     */
    static void executeQuery(HttpClientPrivate *d, HttpMethod method,
                             std::function<void (const QString &)> successHandler,
                             std::function<void (const QString &)> errorHandler,
                             const char *encoding);

    /**
     * @brief 上传文件或者数据
     * @param d     HttpClientPrivate 的对象
     * @param paths 要上传的文件的路径(path 和 data 不能同时使用)
     * @param data  要上传的文件的数据
     * @param successHandler 请求成功的回调 lambda 函数
     * @param errorHandler   请求失败的回调 lambda 函数
     * @param encoding       请求响应的编码
     */
    static void upload(HttpClientPrivate *d,
                       const QStringList &paths, const QByteArray &data,
                       std::function<void (const QString &)> successHandler,
                       std::function<void (const QString &)> errorHandler,
                       const char *encoding);

    /**
     * @brief 读取服务器响应的数据
     * @param reply    请求的 QNetworkReply 对象
     * @param encoding 请求响应的编码，默认使用 UTF-8
     * @return 服务器端响应的字符串
     */
    static QString readReply(QNetworkReply *reply, const char *encoding = "UTF-8");

    /**
     * @brief 请求结束的处理函数
     * @param debug          如果为 true 则输出调试信息，为 false 不输出
     * @param successMessage 请求成功的消息
     * @param errorMessage   请求失败的消息
     * @param successHandler 请求成功的回调 lambda 函数
     * @param errorHandler   请求失败的回调 lambda 函数
     * @param reply          QNetworkReply 对象，不能为 NULL
     * @param manager        请求的 manager，不为 NULL 时在此函数中 delete
     */
    static void handleFinish(bool debug,
                             const QString &successMessage,
                             const QString &errorMessage,
                             std::function<void (const QString &)> successHandler,
                             std::function<void (const QString &)> errorHandler,
                             QNetworkReply *reply, QNetworkAccessManager *manager);
};

HttpClientPrivate::HttpClientPrivate(const QString &url) : url(url), manager(NULL), useJson(false), debug(false) {
}

// 注意: 不要在回调函数中使用 d，因为回调函数被调用时 HttpClient 对象很可能已经被释放掉了。
HttpClient::HttpClient(const QString &url) : d(new HttpClientPrivate(url)) {
}

HttpClient::~HttpClient() {
    delete d;
}

HttpClient &HttpClient::manager(QNetworkAccessManager *manager) {
    d->manager = manager;
    return *this;
}

// 传入 debug 为 true 则使用 debug 模式，请求执行时输出请求的 URL 和参数等
HttpClient &HttpClient::debug(bool debug) {
    d->debug = debug;

    return *this;
}

// 添加 Form 格式参数
HttpClient &HttpClient::param(const QString &name, const QString &value) {
    d->params.addQueryItem(name, value);

    return *this;
}

// 添加 Json 格式参数
HttpClient &HttpClient::json(const QString &json) {
    d->useJson  = true;
    d->json = json;

    return *this;
}

// 添加访问头
HttpClient &HttpClient::header(const QString &header, const QString &value) {
    d->headers[header] = value;

    return *this;
}

// 执行 GET 请求
void HttpClient::get(std::function<void (const QString &)> successHandler,
                     std::function<void (const QString &)> errorHandler,
                     const char *encoding) {
    HttpClientPrivate::executeQuery(d, HttpClientPrivate::GET, successHandler, errorHandler, encoding);
}

// 执行 POST 请求
void HttpClient::post(std::function<void (const QString &)> successHandler,
                      std::function<void (const QString &)> errorHandler,
                      const char *encoding) {
    HttpClientPrivate::executeQuery(d, HttpClientPrivate::POST, successHandler, errorHandler, encoding);
}

// 执行 PUT 请求
void HttpClient::put(std::function<void (const QString &)> successHandler,
                     std::function<void (const QString &)> errorHandler,
                     const char *encoding) {
    HttpClientPrivate::executeQuery(d, HttpClientPrivate::PUT, successHandler, errorHandler, encoding);
}

// 执行 DELETE 请求
void HttpClient::remove(std::function<void (const QString &)> successHandler,
                        std::function<void (const QString &)> errorHandler,
                        const char *encoding) {
    HttpClientPrivate::executeQuery(d, HttpClientPrivate::DELETE, successHandler, errorHandler, encoding);
}

void HttpClient::download(const QString &destinationPath,
                          std::function<void (const QString &)> successHandler,
                          std::function<void (const QString &)> errorHandler) {
    bool  debug = d->debug;
    QFile *file = new QFile(destinationPath);

    if (file->open(QIODevice::WriteOnly)) {
        download([=](const QByteArray &data) {
            file->write(data);
        }, [=](const QString &) {
            // 请求结束后释放文件对象
            file->flush();
            file->close();
            file->deleteLater();

            if (debug) {
                qDebug().noquote() << QString("下载完成，保存到: %1").arg(destinationPath);
            }

            if (NULL != successHandler) {
                successHandler(QString("下载完成，保存到: %1").arg(destinationPath));
            }
        }, errorHandler);
    } else {
        // 打开文件出错
        if (debug) {
            qDebug().noquote() << QString("打开文件出错: %1").arg(destinationPath);
        }

        if (NULL != errorHandler) {
            errorHandler(QString("打开文件出错: %1").arg(destinationPath));
        }
    }
}

// 使用 GET 进行下载，当有数据可读取时回调 readyRead(), 大多数情况下应该在 readyRead() 里把数据保存到文件
void HttpClient::download(std::function<void (const QByteArray &)> readyRead,
                          std::function<void (const QString &)> successHandler,
                          std::function<void (const QString &)> errorHandler) {
    bool debug = d->debug;
    bool internal;
    QNetworkAccessManager *manager = HttpClientPrivate::getManager(d, &internal);
    QNetworkRequest        request = HttpClientPrivate::createRequest(d, HttpClientPrivate::GET);
    QNetworkReply           *reply = manager->get(request);

    // 有数据可读取时回调 readyRead()
    QObject::connect(reply, &QNetworkReply::readyRead, [=] {
        readyRead(reply->readAll());
    });

    // 请求结束
    QObject::connect(reply, &QNetworkReply::finished, [=] {
        QString successMessage = "下载完成"; // 请求结束时一次性读取所有响应数据
        QString errorMessage   = reply->errorString();
        HttpClientPrivate::handleFinish(debug, successMessage, errorMessage, successHandler, errorHandler,
                                        reply, internal ? manager : NULL);
    });
}

// 上传文件
void HttpClient::upload(const QString &path,
                        std::function<void (const QString &)> successHandler,
                        std::function<void (const QString &)> errorHandler,
                        const char *encoding) {
    QStringList paths = (QStringList() << path);
    HttpClientPrivate::upload(d, paths, QByteArray(), successHandler, errorHandler, encoding);
}

// 上传数据
void HttpClient::upload(const QByteArray &data,
                        std::function<void (const QString &)> successHandler,
                        std::function<void (const QString &)> errorHandler,
                        const char *encoding) {
    HttpClientPrivate::upload(d, QStringList(), data, successHandler, errorHandler, encoding);
}

void HttpClient::upload(const QStringList &paths,
                        std::function<void (const QString &)> successHandler,
                        std::function<void (const QString &)> errorHandler,
                        const char *encoding) {
    HttpClientPrivate::upload(d, paths, QByteArray(), successHandler, errorHandler, encoding);
}

// 上传文件或者数据的实现
void HttpClientPrivate::upload(HttpClientPrivate *d,
                               const QStringList &paths, const QByteArray &data,
                               std::function<void (const QString &)> successHandler,
                               std::function<void (const QString &)> errorHandler,
                               const char *encoding) {
    bool debug = d->debug;
    QHttpMultiPart *multiPart = new QHttpMultiPart(QHttpMultiPart::FormDataType);

    // 创建 Form 表单的参数 Text Part
    QList<QPair<QString, QString> > paramItems = d->params.queryItems();
    for (int i = 0; i < paramItems.size(); ++i) {
        QHttpPart textPart;
        QString name  = paramItems.at(i).first;
        QString value = paramItems.at(i).second;
        textPart.setHeader(QNetworkRequest::ContentDispositionHeader, QString("form-data; name=\"%1\"").arg(name));
        textPart.setBody(value.toUtf8());
        multiPart->append(textPart);
    }

    if (paths.size() > 0) {
        // 上传文件
        QString inputName = paths.size() == 1 ? "file" : "files"; // 一个文件时为 file，多个文件时为 files

        for (const QString &path : paths) {
            if (!path.isEmpty()) {
                // path 不为空时，上传文件
                QFile *file = new QFile(path);
                file->setParent(multiPart); // we cannot delete the file now, so delete it with the multiPart

                // 如果文件打开失败，则释放资源返回
                if(!file->open(QIODevice::ReadOnly)) {
                    QString errorMessage = QString("打开文件失败[%2]: %1").arg(path).arg(file->errorString());

                    if (debug) {
                        qDebug().noquote() << errorMessage;
                    }

                    if (NULL != errorHandler) {
                        errorHandler(errorMessage);
                    }

                    multiPart->deleteLater();
                    return;
                }

                // 文件上传的参数名为 file，值为文件名
                // 服务器是 Java 的则用 form-data
                // 服务器是 PHP  的则用 multipart/form-data
                QString   disposition = QString("form-data; name=\"%1\"; filename=\"%2\"").arg(inputName).arg(file->fileName());
                QHttpPart filePart;
                filePart.setHeader(QNetworkRequest::ContentDispositionHeader, QVariant(disposition));
                filePart.setBodyDevice(file);
                multiPart->append(filePart);
            }
        }
    } else {
        // 上传数据
        QString   disposition = QString("form-data; name=\"file\"; filename=\"no-name\"");
        QHttpPart dataPart;
        dataPart.setHeader(QNetworkRequest::ContentDispositionHeader, QVariant(disposition));
        dataPart.setBody(data);
        multiPart->append(dataPart);
    }

    bool internal;
    QNetworkAccessManager *manager = HttpClientPrivate::getManager(d, &internal);
    QNetworkRequest        request = HttpClientPrivate::createRequest(d, HttpClientPrivate::UPLOAD);
    QNetworkReply           *reply = manager->post(request, multiPart);

    QObject::connect(reply, &QNetworkReply::finished, [=] {
        multiPart->deleteLater(); // 释放资源: multiPart + file

        QString successMessage = HttpClientPrivate::readReply(reply, encoding); // 请求结束时一次性读取所有响应数据
        QString errorMessage   = reply->errorString();
        HttpClientPrivate::handleFinish(debug, successMessage, errorMessage, successHandler, errorHandler,
                                        reply, internal ? manager : NULL);
    });
}

// 执行请求的辅助函数
void HttpClientPrivate::executeQuery(HttpClientPrivate *d, HttpMethod method,
                                     std::function<void (const QString &)> successHandler,
                                     std::function<void (const QString &)> errorHandler,
                                     const char *encoding) {
    // 如果不使用外部的 manager 则创建一个新的，在访问完成后会自动删除掉
    bool debug = d->debug;
    bool internal;
    QNetworkAccessManager *manager = HttpClientPrivate::getManager(d, &internal);
    QNetworkRequest        request = HttpClientPrivate::createRequest(d, method);
    QNetworkReply           *reply = NULL;

    switch (method) {
    case HttpClientPrivate::GET:
        reply = manager->get(request);
        break;
    case HttpClientPrivate::POST:
        reply = manager->post(request, d->useJson ? d->json.toUtf8() : d->params.toString(QUrl::FullyEncoded).toUtf8());
        break;
    case HttpClientPrivate::PUT:
        reply = manager->put(request, d->useJson ? d->json.toUtf8() : d->params.toString(QUrl::FullyEncoded).toUtf8());
        break;
    case HttpClientPrivate::DELETE:
        reply = manager->deleteResource(request);
        break;
    default:
        break;
    }

    QObject::connect(reply, &QNetworkReply::finished, [=] {
        QString successMessage = HttpClientPrivate::readReply(reply, encoding); // 请求结束时一次性读取所有响应数据
        QString errorMessage   = reply->errorString();
        HttpClientPrivate::handleFinish(debug, successMessage, errorMessage, successHandler, errorHandler,
                                        reply, internal ? manager : NULL);
    });
}

QNetworkAccessManager* HttpClientPrivate::getManager(HttpClientPrivate *d, bool *internal) {
    *internal = d->manager == NULL;
    return *internal ? new QNetworkAccessManager() : d->manager;
}

QNetworkRequest HttpClientPrivate::createRequest(HttpClientPrivate *d, HttpMethod method) {
    bool get      = method == HttpMethod::GET;
    bool upload   = method == HttpClientPrivate::UPLOAD;
    bool postForm = !get && !upload && !d->useJson;
    bool postJson = !get && !upload &&  d->useJson;

    // 如果是 GET 请求，并且参数不为空，则编码请求的参数，放到 URL 后面
    if (get && !d->params.isEmpty()) {
        d->url += "?" + d->params.toString(QUrl::FullyEncoded);
    }

    // 调试时输出网址和参数
    if (d->debug) {
        qDebug().noquote() << "网址:" << d->url;

        if (postJson) {
            qDebug().noquote() << "参数:" << d->json;
        } else if (postForm || upload) {
            QList<QPair<QString, QString> > paramItems = d->params.queryItems();
            QString buffer; // 避免多次调用 qDebug() 输入调试信息，每次 qDebug() 都有可能输出行号等

            // 按键值对的方式输出参数
            for (int i = 0; i < paramItems.size(); ++i) {
                QString name  = paramItems.at(i).first;
                QString value = paramItems.at(i).second;

                if (0 == i) {
                    buffer += QString("参数: %1=%2\n").arg(name).arg(value);
                } else {
                    buffer += QString("     %1=%2\n").arg(name).arg(value);
                }
            }

            if (!buffer.isEmpty()) {
                qDebug().noquote() << buffer;
            }
        }
    }

    // 如果是 POST 请求，useJson 为 true 时添加 Json 的请求头，useJson 为 false 时添加 Form 的请求头
    if (postForm) {
        d->headers["Content-Type"] = "application/x-www-form-urlencoded";
    } else if (postJson) {
        d->headers["Accept"]       = "application/json; charset=utf-8";
        d->headers["Content-Type"] = "application/json";
    }

    // 把请求的头添加到 request 中
    QNetworkRequest request(QUrl(d->url));
    QHashIterator<QString, QString> iter(d->headers);
    while (iter.hasNext()) {
        iter.next();
        request.setRawHeader(iter.key().toUtf8(), iter.value().toUtf8());
    }

    return request;
}

QString HttpClientPrivate::readReply(QNetworkReply *reply, const char *encoding) {
    QTextStream in(reply);
    QString result;
    in.setCodec(encoding);

    while (!in.atEnd()) {
        result += in.readLine();
    }

    return result;
}

void HttpClientPrivate::handleFinish(bool debug,
                                     const QString &successMessage,
                                     const QString &errorMessage,
                                     std::function<void (const QString &)> successHandler,
                                     std::function<void (const QString &)> errorHandler,
                                     QNetworkReply *reply, QNetworkAccessManager *manager) {
    if (reply->error() == QNetworkReply::NoError) {
        // 请求成功
        if (debug) {
            qDebug().noquote() << QString("[成功]请求结束: %1").arg(successMessage);
        }

        if (NULL != successHandler) {
            successHandler(successMessage);
        }
    } else {
        // 请求失败
        if (debug) {
            qDebug().noquote() << QString("[失败]请求结束: %1").arg(errorMessage);
        }

        if (NULL != errorHandler) {
            errorHandler(errorMessage);
        }
    }

    // 释放资源
    reply->deleteLater();

    if (NULL != manager) {
        manager->deleteLater();
    }
}
