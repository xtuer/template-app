#ifndef CONFIG_H
#define CONFIG_H

#include "util/Singleton.h"

#define ConfigInstance Singleton<Config>::getInstance()

class Json;
class QString;
class QStringList;
class QSettings;

/**
 * 用于读写配置文件:
 * 1. 配置文件位于: data/config.json，存储配置的信息，例如数据库信息，QSS 文件的路径
 * 2. 读取配置，如 Singleton<Config>::getInstance().getDatabaseName();
 */
class Config {
    SINGLETON(Config)

public:
    // 销毁 Config 的资源，如有必要，在 main 函数结束前调用，例如保存配置文件
    void destroy();

    // 数据库信息
    QString getDatabaseType() const;            // 数据库的类型, 如QPSQL, QSQLITE, QMYSQL
    QString getDatabaseHost() const;            // 数据库主机的IP
    QString getDatabaseName() const;            // 数据库名
    QString getDatabaseUsername() const;        // 登录数据库的用户名
    QString getDatabasePassword() const;        // 登录数据库的密码
    QString getDatabaseTestOnBorrowSql() const; // 验证连接的 SQL
    bool getDatabaseTestOnBorrow() const;       // 是否验证连接
    int  getDatabaseMaxWaitTime() const;        // 线程获取连接最大等待时间
    int  getDatabaseMaxConnectionCount() const; // 最大连接数
    int  getDatabasePort() const;               // 数据库的端口号
    bool isDatabaseDebug() const;               // 是否打印出执行的 SQL 语句和参数
    QStringList getDatabaseSqlFiles() const;    // SQL 语句文件, 可以是多个

    // 其它
    QStringList getQssFiles() const; // QSS 样式表文件, 可以是多个

private:
    Json *json;
    QSettings *appSettings;
};

#endif // CONFIG_H
