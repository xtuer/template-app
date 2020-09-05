package com.xtuer.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtuer.util.SecurityUtils;
import com.xtuer.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户类型
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@JsonIgnoreProperties({ "password" })
public class User {
    private long userId; // 用户 ID

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
    private boolean enabled = true; // 状态: false (禁用), true (启用)

    private Set<Role> roles = new HashSet<>(); // 角色，需要前缀 ROLE_，例如 ROLE_ADMIN_SYSTEM

    public User() {}

    public User(String username, String password, Role... roles) {
        this(0, username, password, roles);
    }

    public User(long userId, String username, String password, Role... roles) {
        this.userId   = userId;
        this.username = username;
        this.password = password;

        for (Role role : roles) {
            this.addRole(role);
        }
    }

    /**
     * 增加角色
     *
     * @param role 角色
     * @return 返回用户对象
     */
    public User addRole(Role role) {
        roles.add(role);

        return this;
    }

    /**
     * 判断用户是否有传入的角色
     *
     * @param role 角色
     * @return 有此角色返回 true，否则返回 false
     */
    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    public static void main(String[] args) throws JsonProcessingException {
        User user1 = new User();
        Utils.dump(user1);
        System.out.println(user1.getRoles());

        User user2 = new User("Bob", "Passw0rd", Role.ROLE_USER);
        Utils.dump(user2);
        System.out.println(user2.getRoles());

        user2.setEnabled(false);
        Utils.dump(user2);

        user2 = new ObjectMapper().readValue(Utils.toJson(user2), User.class);
        System.out.println("From JSON");
        Utils.dump(user2);

        User user3 = new User();
        user3.setUsername("Bob").setPassword("pass").addRole(Role.ROLE_ADMIN_ORG);
        Utils.dump(SecurityUtils.buildUserDetails(user3));
    }
}
