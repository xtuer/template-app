package com.xtuer.security;

import com.xtuer.bean.User;
import com.xtuer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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
        User user = userService.findUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(username + " not found!");
        }

        return user;
    }
}
