#include "UserDao.h"
#include "bean/User.h"
#include "db/DBUtil.h"
#include "db/Sqls.h"
#include "bean/Permission.h"

const char * const SQL_NAMESPACE_USER = "User";

User UserDao::findUserById(int id) {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "findUserById").arg(id);
    return DBUtil::selectBean(mapToUser, sql);
}

User UserDao::findUserByUsername(const QString &username) {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "findUserByUsername");

    QVariantMap params;
    params["username"] = username;

    return DBUtil::selectBean(mapToUser, sql, params);
}

User UserDao::findUserByUsernameAndPassword(const QString &username, const QString &password) {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "findUserByUsernameAndPassword");

    QVariantMap params;
    params["username"] = username;
    params["password"] = password;

    return DBUtil::selectBean(mapToUser, sql, params);
}

QList<User> UserDao::findAllUsers() {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "findAllUsers");
    return DBUtil::selectBeans(mapToUser, sql);
}

bool UserDao::isUsernameUsed(const QString &username) {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "isUsernameUsed");

    QVariantMap params;
    params["username"] = username;

    return DBUtil::selectVariant(sql, params).toBool();
}

bool UserDao::updatePassword(int id, const QString &password) {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "updatePassword");

    QVariantMap params;
    params["id"] = id;
    params["password"] = password;

    return DBUtil::update(sql, params);
}

int UserDao::insertUser(const User& user) {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "insertUser");

    QVariantMap params;
    params["username"]   = user.username;
    params["password"]   = user.password;
    params["creator"]    = user.creator;
    params["permission"] = user.permission.getPermissionsValue();

    return DBUtil::insert(sql, params);
}

bool UserDao::updateUser(const User& user) {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "updateUser");

    QVariantMap params;
    params["id"]         = user.id;
    params["username"]   = user.username;
    params["password"]   = user.password;
    params["permission"] = user.permission.getPermissionsValue();

    return DBUtil::update(sql, params);
}

int UserDao::deleteUser(int id) {
    QString sql = SqlInstance.getSql(SQL_NAMESPACE_USER, "deleteUser");

    QVariantMap params;
    params["id"] = id;

    return DBUtil::update(sql, params);
}

User UserDao::mapToUser(const QVariantMap &rowMap) {
    User user;

    user.id         = rowMap.value("id", 0).toInt();
    user.username   = rowMap.value("username").toString();
    user.password   = rowMap.value("password").toString();
    user.creator    = rowMap.value("creator").toString();
    user.permission = Permission(PermissionFlags(rowMap.value("permission", 0).toInt()));

    return user;
}
