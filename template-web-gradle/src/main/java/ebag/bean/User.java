package ebag.bean;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户类型，根据 userdetails.User 的设计，roles, authorities, enabled, expired 等状态不能修改，
 * 只能是创建用户对象的时候传入进来。
 */
@Getter
@Setter
public class User extends org.springframework.security.core.userdetails.User {
    private Long   id;
    private String username;
    private String password;
    private String mail;
    private boolean enabled;
    private Set<String> roles = new HashSet<>(); // 用户的角色

    public User() {
        // 父类不允许空的用户名、密码和权限，所以给个默认的，这样就可以用默认的构造函数创建 User 对象。
        super("non-exist-username", "", new HashSet<>());
    }

    public User(String username, String password, String... roles) {
        this(null, username, password, roles);
    }

    /**
     * 使用账号、密码、角色创建用户
     *
     * @param id 用户的 ID
     * @param username 账号
     * @param password 密码
     * @param roles    角色
     */
    public User(Long id, String username, String password, String... roles) {
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
    public User(Long id, String username, String password, boolean enabled, String... roles) {
        super(username, password, enabled, true, true, true, AuthorityUtils.createAuthorityList(roles));
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled  = enabled;
        this.roles.addAll(Arrays.asList(roles));
    }

    /**
     * 用户是直接从数据库查询生成的，或者用户信息修改后，例如角色、可用状态修改后不会更新到父类的 authorities 中，需要重新创建一个用户对象才行
     *
     * @param user 已有用户对象
     * @return 新的用户对象，权限等信息更新到了父类的 authorities 中
     */
    public static User userForSpringSecurity(User user) {
        User newUser = new User(user.id, user.username, user.password, user.enabled, user.getRoles().toArray(new String[0]));
        newUser.mail = user.mail;

        return newUser;
    }

    public static void main(String[] args) {
        User user1 = new User();
        System.out.println(JSON.toJSONString(user1));
        System.out.println(user1.getRoles());

        User user2 = new User("Bob", "Passw0rd", "ROLE_USER", "ROLE_ADMIN");
        System.out.println(JSON.toJSONString(user2));
        System.out.println(user2.getRoles());

        user2.setEnabled(false);
        System.out.println(JSON.toJSONString(user2));
    }
}
