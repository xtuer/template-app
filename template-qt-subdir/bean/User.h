#ifndef USER_H
#define USER_H

#include "Permission.h"
#include <QString>
#include <QDebug>

class User {
public:
    /**
     * 创建用户
     *
     * @param id 默认为 0，为 0 时表示用户不是有效用户
     * @param username 账号
     * @param password 密码
     * @param permission 权限
     */
    User(int id = 0,
         const QString &username = QString(),
         const QString &password = QString(),
         const Permission &permission = Permission());

    bool isValid() const; // id 大于 0 时为有效用户 (数据库里自增长 ID 从 1 开始)

    friend QDebug operator<<(QDebug stream, const User &user);

    int id; // 用户 ID
    QString username; // 账号
    QString password; // 密码
    QString creator;  // 操作员
    Permission permission; // 权限
};

#endif // USER_H
