#ifndef HTTPCLIENT_H
#define HTTPCLIENT_H

#include <functional>
#include <QMap>
#include <QVariant>
#include <QStringList>
#include <QNetworkReply>
#include <QNetworkRequest>
#include <QNetworkAccessManager>

class HttpClientPrivate;

/**
 * 对 QNetworkAccessManager 简单封装的 HTTP 访问客户端，简化 GET、POST、PUT、DELETE、上传、下载等操作。
 * 在执行请求前设置需要的参数和回调函数:
 *     1. 调用 header() 设置请求头
 *     2. 调用 param() 设置参数，使用 Form 表单的方式提交请求，GET 请求的 query parameters 也可以用它设置
 *     3. 调用 json() 设置 JSON 字符串的 request body，Content-Type 为 application/json，
 *        当然也可以不是 JSON 格式，因使用 request body 的情况多数是使用 JSON 格式传递复杂对象，故命名为 json
 *     4. 调用 success() 注册请求成功的回调函数
 *     5. 调用 fail() 注册请求失败的回调函数
 *     6. 调用 complete() 注册请求结束的回调函数
 *        success(), fail(), complete() 的回调函数是可选的，根据需要注册对应的回调函数，也可以一个都不注册
 * 然后根据请求的类型调用 get(), post(), put(), remove(), download(), upload() 执行 HTTP 请求
 *
 * 默认 HttpClient 会创建一个 QNetworkAccessManager，如果不想使用默认的，调用 manager() 传入即可。
 * 调用 debug(true) 设置为调试模式，输出调试信息如 URL、参数等。
 */
class HttpClient {
public:
    HttpClient(const QString &url);
    ~HttpClient();

    /**
     * @brief 每创建一个 QNetworkAccessManager 对象都会创建一个线程，当频繁的访问网络时，为了节省线程资源，
     *     可以传入 QNetworkAccessManager 给多个请求共享 (它不会被 HttpClient 删除，用户需要自己手动删除)。
     *     如果没有使用 manager() 传入一个 QNetworkAccessManager，则 HttpClient 会自动的创建一个，并且在网络访问完成后自动删除它。
     *
     * @param  manager 执行 HTTP 请求的 QNetworkAccessManager 对象
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& manager(QNetworkAccessManager *manager);

    /**
     * @brief  参数 debug 为 true 则使用 debug 模式，请求执行时输出请求的 URL 和参数等
     *
     * @param  debug 是否启用调试模式
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& debug(bool debug);

    /**
     * @brief 添加一个请求的参数，可以多次调用添加多个参数
     *
     * @param name  参数的名字
     * @param value 参数的值
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& param(const QString &name, const QVariant &value);

    /**
     * @brief 添加多个请求的参数
     *
     * @param ps QMap 类型的参数，key 为参数名，value 为参数值
     *           可以使用 {{"name", 1}, {"box", 2}} 的方式创建 QMap 对象
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& params(const QMap<QString, QVariant> &ps);

    /**
     * @brief 添加请求的参数 (请求体)，使用 Json 格式，例如 "{\"name\": \"Alice\"}"
     *
     * @param json 请求体 (request body) 为 Json 格式的参数字符串
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& json(const QString &json);

    /**
     * @brief 添加请求头
     *
     * @param name  请求头的名字
     * @param value 请求头的值
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& header(const QString &name, const QString &value);

    /**
     * @brief 添加多个请求头
     *
     * @param nameValues 请求头的名字和值对
     *                   可以使用 {{"name", 1}, {"box", 2}} 的方式创建 QMap 对象
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& headers(const QMap<QString, QString> nameValues);

    /**
     * @brief 注册请求成功的回调函数
     *
     * @param successHandler 成功的回调函数，参数为响应的字符串
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& success(std::function<void (const QString &)> successHandler);

    /**
     * @brief 注册请求失败的回调函数
     *
     * @param failHandler 失败的回调函数，参数为失败原因和 HTTP 状态码
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& fail(std::function<void (const QString &, int)> failHandler);

    /**
     * @brief 注册请求结束的回调函数，不管成功还是失败请求结束后都会执行
     *
     * @param completeHandler 完成的回调函数，无参数
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& complete(std::function<void ()> completeHandler);

    /**
     * @brief 设置请求响应的字符集，默认使用 UTF-8
     *
     * @param cs 字符集
     * @return 返回 HttpClient 的引用，可以用于链式调用
     */
    HttpClient& charset(const QString &cs);

    /**
     * @brief 执行 GET 请求
     */
    void get();

    /**
     * @brief 执行 POST 请求
     */
    void post();

    /**
     * @brief 执行 PUT 请求
     */
    void put();

    /**
     * @brief 执行 DELETE 请求，由于 delete 是 C++ 的运算符，所以用同义词 remove
     *        注意: Qt 提供的 DELETE 请求是不支持传递参数的，
     *        请参考 QNetworkAccessManager::deleteResource(const QNetworkRequest &request)
     */
    void remove();

    /**
     * @brief 使用 GET 进行下载，下载的文件保存到 savePath
     *
     * @param savePath 下载的文件保存路径
     */
    void download(const QString &savePath);

    /**
     * @brief 上传单个文件
     *        使用 POST 上传，服务器端获取文件的参数名为 file
     *
     * @param path 要上传的文件的路径
     */
    void upload(const QString &path);

    /**
     * @brief 上传文件，文件的内容已经读取到 data 中
     *        使用 POST 上传，服务器端获取文件的参数名为 file
     *
     * @param path 要上传的文件的路径
     */
    void upload(const QByteArray &data);

    /**
     * @brief 上传多个文件
     *        使用 POST 上传，服务器端获取文件的参数名为 files
     *
     * @param paths 要上传的文件的路径
     */
    void upload(const QStringList &paths);

private:
    HttpClientPrivate *d;
};

#endif // HTTPCLIENT_H
