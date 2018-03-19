package ebag.service;

import ebag.bean.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public User findUserByUsername(String username) {
        // Spring Security 5 使用密码的前缀决定使用哪个 PasswordEncoder，实现同时支持多种加密方式
        // 不需要给 authentication-provider 配置 PasswordEncoder
        // {noop}表示不加密密码，{bcrypt} 使用 bcrypt 加密
        if ("admin".equals(username)) {
            return new User(1L, "admin", "{noop}admin", "ROLE_ADMIN");
        } else if ("alice".equals(username)) {
            // 密码是: password
            return new User(2L, "alice", "{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG", "ROLE_USER");
        }

        return null;
    }

    public User findUserByUsernamePassword(String username, String password) {
        return findUserByUsername(username); // 测试使用
    }
}
