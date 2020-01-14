package com.xtuer.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import com.xtuer.util.SecurityUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户类型
 */
@Getter
@Setter
@Accessors(chain = true)
@JSONType(ignores = {"password"})
public class User {
    private long id;

    @NotBlank(message = "账号不能为空")
    private String username; // 账号

    @NotBlank(message = "密码不能为空")
    private String password; // 密码

    private String  nickname; // 姓名或昵称
    private String  email;    // 邮件地址
    private String  mobile;   // 手机号码
    private String  phone;    // 固定电话
    private String  avatar;   // 头像
    private long    orgId;    // 所属机构的 ID
    private int     gender;   // 性别: 0 (未设置), 1 (男), 2 (女)
    private boolean enabled = true;  // 状态: false (禁用), true (启用)

    private Set<String> roles = new HashSet<>(); // 角色，需要前缀 ROLE_，例如 ROLE_ADMIN_SYSTEM

    public User() {}

    public User(String username, String password, String... roles) {
        this(0, username, password, roles);
    }

    public User(long id, String username, String password, String... roles) {
        this.id = id;
        this.username = username;
        this.password = password;

        for (String role : roles) {
            this.addRole(role);
        }
    }

    /**
     * 增加角色
     *
     * @param role 角色
     * @return 返回用户对象
     */
    public User addRole(String role) {
        role = StringUtils.trim(role);

        if (StringUtils.isNotBlank(role)) {
            roles.add(role);
        }

        return this;
    }

    /**
     * 判断用户是否有传入的角色
     *
     * @param role 角色
     * @return 有此角色返回 true，否则返回 false
     */
    public boolean hasRole(String role) {
        return this.roles.contains(role);
    }

    public static void main(String[] args) {
        User user1 = new User();
        System.out.println(JSON.toJSONString(user1));
        System.out.println(user1.getRoles());

        User user2 = new User("Bob", "Passw0rd", "ROLE_USER");
        System.out.println(JSON.toJSONString(user2));
        System.out.println(user2.getRoles());

        user2.setEnabled(false);
        System.out.println(JSON.toJSONString(user2));

        User user3 = new User();
        user3.setUsername("Bob").setPassword("pass").addRole("ADMIN");
        System.out.println(JSON.toJSONString(SecurityUtils.buildUserDetails(user3)));
    }
}
