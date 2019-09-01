package com.edu.training.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户类型，根据 userdetails.User 的设计, authorities, enabled, expired 等状态不能修改，只能是创建用户对象的时候传入进来。
 * Roles 和 authorities 是对应的，authorities 是给 Spring Security 使用的，roles 是给普通业务逻辑使用的。
 * 但更新 roles 后并不会自动更新用户的 authorities，需要调用 cloneForSecurity() 生成新的对象才能更新 authorities。
 */
@Getter
@Setter
@Accessors(chain = true)
@JSONType(ignores = {"name", "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
public class User extends org.springframework.security.core.userdetails.User {
    private static final long serialVersionUID = 1L;

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
    private boolean enabled;  // 状态: 0 (禁用), 1 (启用)

    private Set<String> roles = new HashSet<>(); // 角色，需要前缀 ROLE_，例如 ROLE_ADMIN_SYSTEM

    public User() {
        // 父类不允许空的用户名、密码和权限，所以给个默认的，这样就可以用默认的构造函数创建 User 对象。
        super("[username]", "[protected]", new HashSet<>());
    }

    public User(String username, String password, String... roles) {
        this(0, username, password, roles);
    }

    /**
     * 使用账号、密码、角色创建用户
     *
     * @param id 用户的 ID
     * @param username 账号
     * @param password 密码
     * @param roles    角色
     */
    public User(long id, String username, String password, String... roles) {
        this(id, username, password, true, roles);
        this.id = id;
    }

    /**
     * 使用账号、密码、是否禁用、角色创建用户
     *
     * @param id 用户的 ID
     * @param username 账号
     * @param password 密码
     * @param enabled  是否禁用
     * @param roles    角色
     */
    public User(long id, String username, String password, boolean enabled, String... roles) {
        super(username, password, enabled, true, true, true, AuthorityUtils.createAuthorityList(roles));
        this.id       = id;
        this.username = username;
        this.password = password;
        this.enabled  = enabled;
        this.roles = new HashSet<>(Arrays.asList(roles));
    }

    /**
     * 用户是直接从数据库查询到的，或者用户手动创建的，例如修改角色、可用状态等不会更新父类的 authorities 和 enabled，
     * 也就是说新更新的信息 Spring Security 看不到，需要克隆当前用户生成 Spring Security 需要的用户对象才能给 Spring Security 使用
     *
     * @return 返回新的用户对象，权限等信息更新到了父类的 authorities 中
     */
    public User cloneForSecurity() {
        String[] roles   = this.getRoles().toArray(new String[] {});
        User newUser     = new User(this.id, this.username, this.password, this.enabled, roles);
        newUser.email    = this.email;
        newUser.nickname = this.nickname;
        newUser.avatar   = this.avatar;
        newUser.orgId    = this.orgId;

        return newUser;
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

    /**
     * 保护密码
     */
    public void protectPassword() {
        password = "[protected]";
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
        user3 = user3.cloneForSecurity();
        System.out.println(JSON.toJSONString(user3));
    }
}
