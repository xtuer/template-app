package edu.bean;

import com.alibaba.fastjson.JSON;
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
 * 用户类型，根据 userdetails.User 的设计，roles, authorities, enabled, expired 等状态不能修改，
 * 只能是创建用户对象的时候传入进来。
 */
@Getter
@Setter
@Accessors(chain = true)
public class User extends org.springframework.security.core.userdetails.User {
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

    private Set<String> roles = new HashSet<>(); // 角色

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
     * 用户是直接从数据库查询生成的，或者用户信息修改后，例如角色、可用状态修改后不会更新到父类的 authorities 中，需要重新创建一个用户对象才行
     *
     * @param user 已有用户对象
     * @return 新的用户对象，权限等信息更新到了父类的 authorities 中
     */
    public static User userForSpringSecurity(User user) {
        String[] roles   = user.getRoles().toArray(new String[]{});
        User newUser     = new User(user.id, user.username, user.password, user.enabled, roles);
        newUser.email    = user.email;
        newUser.nickname = user.nickname;
        newUser.avatar   = user.avatar;
        newUser.orgId    = user.orgId;

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
    }
}
