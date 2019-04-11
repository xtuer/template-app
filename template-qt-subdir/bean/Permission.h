#ifndef PERMISSION_H
#define PERMISSION_H

#include <QMap>
#include <QString>

/**
 * 权限枚举，值为 2 的幂: 0x1, 0x2, 0x4, 0x8, 0x10, 0x20, 0x40, 0x80, 0x100, 0x200, ...
 * 注意: enum 可以自动转换为 int，但是 int 不能自动转换为 enum，需要手动转换如 PermissionFlag(0x2)
 */
enum PermissionFlag {
    USERMANG        = 0x01, // 用户管理
    PROGRAM_RECORD  = 0x02, // 编程记录器
    READ_RECORD     = 0x04, // 读取记录器
    MODIFY_SECURITY = 0x08, // 更改安全选项
    MODIFY_SETTINGS = 0x10, // 更改一般设置
    AUDIT_TRAIL     = 0x20, // 显示审计追踪
    SIGNATURE       = 0x40, // 电子签名
    QUIT            = 0x80, // 退出程序
    CHECK           = 0x100 // 检测
};

// PermissionFlags fs(7);    // int to Flags
// PermissionFlags::Int(fs); // Flags to int
Q_DECLARE_FLAGS(PermissionFlags, PermissionFlag)
Q_DECLARE_OPERATORS_FOR_FLAGS(PermissionFlags)

/**
 * 权限类，提供了获取、判断、增加、删除权限的操作
 *
 * 创建权限:
 *     Permission p1(PermissionFlags(1223)); // 使用整数创建权限
 *     Permission p2(PermissionFlag::ADMIN | PermissionFlag::GUEST);
 * 增加权限: p.addPermission(PermissionFlag::ADMIN);
 * 判断权限: p.hasPermission(PermissionFlag::ADMIN);
 * 删除权限: p.removePermission(PermissionFlag::ADMIN);
 * 列出权限:
 *     foreach (PermissionFlag p, Permission::PERMISSIONS.keys()) {
 *         qDebug() << Permission::PERMISSIONS.value(p);
 *     }
 * 权限整数表示: p.getPermissionsValue();
 */
class Permission {
public:
    Permission(PermissionFlags permissions = 0);

    /**
     * 获取权限
     */
    PermissionFlags getPermissions() const;

    /**
     * 获取权限的整形值，存储到数据库，文件里时有用
     */
    int getPermissionsValue() const;

    /**
     * 检测是否有权限
     *
     * @param permission 是否含有的权限
     * @return 有权限则返回 true，否则返回 false
     */
     bool hasPermission(PermissionFlag permission) const;

    /**
     * 增加权限
     *
     * @param permission 要增加的权限
     */
     void addPermission(PermissionFlag permission);

    /**
     * 删除权限
     *
     * @param permission 要删除的权限
     */
     void removePermission(PermissionFlag permission);

     // 所有权限的 map: key 为权限值，value 为权限的字符串描述
     static const QMap<PermissionFlag, QString> PERMISSIONS;

private:
    PermissionFlags permissions; // 权限
};

#endif // PERMISSION_H
