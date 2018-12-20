package edu.security;

import edu.bean.User;
import edu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserService userService;

    /**
     * 使用 username 加载用户的信息，如密码，权限等
     * @param username 登陆表单中用户输入的用户名
     * @return 返回查找到的用户对象
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long schoolId = 0;
        User user = userService.findUser(username, schoolId);

        if (user == null) {
            throw new UsernameNotFoundException(username + " not found!");
        }

        user = User.userForSpringSecurity(user); // 构建 Spring Security 需要的用户

        return user;
    }
}
