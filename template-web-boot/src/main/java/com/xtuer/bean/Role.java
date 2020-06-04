package com.xtuer.bean;

/**
 * 用户角色
 */
public enum Role {
    ROLE_ADMIN_SYSTEM("系统管理员"),
    ROLE_ADMIN_ORG("机构管理员"),
    ROLE_FORM_TEACHER("班主任"),
    ROLE_TEACHER("老师"),
    ROLE_STUDENT("学生"),
    ROLE_USER("普通用户");

    // 角色的中文名字，方便记忆 (JSON 序列化的时候不会输出)
    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }
}
