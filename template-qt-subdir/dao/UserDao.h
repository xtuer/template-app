#ifndef USERDAO_H
#define USERDAO_H

#include <QList>
#include <QString>
#include <QVariant>
#include <QVariantMap>

class User;

class UserDao {
public:
    static User findUserById(int id);
    static User findUserByUsername(const QString &username);
    static User findUserByUsernameAndPassword(const QString &username, const QString &password);
    static QList<User> findAllUsers();
    static bool isUsernameUsed(const QString &username);

    static bool updatePassword(int id, const QString &password);

    static int  insertUser(const User &user);
    static bool updateUser(const User &user);
    static int  deleteUser(int id);

private:
    static User mapToUser(const QVariantMap &rowMap);
};

#endif // USERDAO_H
