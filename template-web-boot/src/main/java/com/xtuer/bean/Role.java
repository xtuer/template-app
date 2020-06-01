package com.xtuer.bean;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户角色
 */
public enum Role {
    ROLE_ADMIN_SYSTEM, // 系统管理员
    ROLE_ADMIN_ORG,    // 机构管理员
    ROLE_FORM_TEACHER, // 班主任
    ROLE_TEACHER,      // 老师
    ROLE_STUDENT,      // 学生
    ROLE_USER;         // 普通用户

    /**
     * 把字符串的角色转为枚举的角色
     *
     * @param texts 字符串的角色
     * @return 返回枚举角色的 Set
     */
    public static Set<Role> fromStrings(Set<String> texts) {
        Set<Role> roles = new HashSet<>();

        for (String text : texts) {
            roles.add(Enum.valueOf(Role.class, text));
        }

        return roles;
    }

    /**
     * 把枚举类型的角色转为字符串类型的角色
     *
     * @param roles 枚举类型的角色
     * @return 返回字符串角色的数组
     */
    public static String[] toArray(Set<Role> roles) {
        return roles.stream()
                .map(Role::name)
                .collect(Collectors.toList())
                .toArray(new String[] {});
    }
}
