#include "Permission.h"

const QMap<PermissionFlag, QString> Permission::PERMISSIONS {
    { PermissionFlag::PROGRAM_RECORD,  "编程记录器" },
    { PermissionFlag::READ_RECORD,     "读取记录器" },
    { PermissionFlag::MODIFY_SECURITY, "更改安全选项" },
    { PermissionFlag::MODIFY_SETTINGS, "更改一般设置" },
    { PermissionFlag::AUDIT_TRAIL,     "显示审计追踪" },
    { PermissionFlag::USERMANG,        "用户管理" },
    { PermissionFlag::SIGNATURE,       "电子签名" },
    { PermissionFlag::QUIT,            "退出程序" },
    { PermissionFlag::CHECK,           "检测" },
};

Permission::Permission(PermissionFlags permissions) :permissions(permissions) {

}

// 获取权限
PermissionFlags Permission::getPermissions() const {
    return permissions;
}

// 获取权限的整形值，存储到数据库，文件里时有用
int Permission::getPermissionsValue() const {
    return PermissionFlags::Int(permissions);
}

// 检测是否有权限
bool Permission::hasPermission(PermissionFlag permission) const {
    return permissions.testFlag(permission);
}

// 增加权限
void Permission::addPermission(PermissionFlag permission) {
    permissions |= permission;
}

// 删除权限
void Permission::removePermission(PermissionFlag permission) {
    permissions &= (~permission);
}
