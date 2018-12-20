package edu.bean;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.authority.AuthorityUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;

/**
 * 用户类型，根据 userdetails.User 的设计，roles, authorities, enabled, expired 等状态不能修改，
 * 只能是创建用户对象的时候传入进来。
 */
@Getter
@Setter
@Accessors(chain = true)
public class User extends org.springframework.security.core.userdetails.User {
    @NotNull(message = "用户 ID 不能为空")
    private Long id;

    @NotBlank(message = "账号不能为空")
    private String username; // 账号

    @NotBlank(message = "密码不能为空")
    private String password; // 密码

    private String  nickname; // 姓名或昵称
    private String  email;    // 邮件地址
    private String  mobile;   // 手机号码
    private String  phone;    // 固定电话
    private String  role;     // 角色
    private String  avatar;   // 头像
    private Long    schoolId; // 所属学校的 ID
    private int     gender;   // 性别: 0(未设置), 1(男), 2(女)
    private boolean enabled;  // 1 为启用，0 为禁用

    public User() {
        // 父类不允许空的用户名、密码和权限，所以给个默认的，这样就可以用默认的构造函数创建 User 对象。
        super("[username]", "[protected]", new HashSet<>());
    }

    public User(String username, String password, String role) {
        this(null, username, password, role);
    }

    /**
     * 使用账号、密码、角色创建用户
     *
     * @param id 用户的 ID
     * @param username 账号
     * @param password 密码
     * @param role     角色
     */
    public User(Long id, String username, String password, String role) {
        this(id, username, password, true, role);
        this.id = id;
    }

    /**
     * 使用账号、密码、是否禁用、角色创建用户
     *
     * @param id 用户的 ID
     * @param username 账号
     * @param password 密码
     * @param enabled  是否禁用
     * @param role     角色
     */
    public User(Long id, String username, String password, boolean enabled, String role) {
        super(username, password, enabled, true, true, true, AuthorityUtils.createAuthorityList(role));
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled  = enabled;
        this.role     = role;
    }

    /**
     * 用户是直接从数据库查询生成的，或者用户信息修改后，例如角色、可用状态修改后不会更新到父类的 authorities 中，需要重新创建一个用户对象才行
     *
     * @param user 已有用户对象
     * @return 新的用户对象，权限等信息更新到了父类的 authorities 中
     */
    public static User userForSpringSecurity(User user) {
        User newUser     = new User(user.id, user.username, user.password, user.enabled, user.getRole());
        newUser.email    = user.email;
        newUser.nickname = user.nickname;
        newUser.avatar   = user.avatar;
        newUser.schoolId = user.schoolId;

        return newUser;
    }

    public void protectPassword() {
        password = "[protected]";
    }

    public static void main(String[] args) {
        User user1 = new User();
        System.out.println(JSON.toJSONString(user1));
        System.out.println(user1.getRole());

        User user2 = new User("Bob", "Passw0rd", "ROLE_USER");
        System.out.println(JSON.toJSONString(user2));
        System.out.println(user2.getRole());

        user2.setEnabled(false);
        System.out.println(JSON.toJSONString(user2));
    }
}
