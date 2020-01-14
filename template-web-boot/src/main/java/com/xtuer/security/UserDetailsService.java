package com.xtuer.security;

import com.xtuer.bean.User;
import com.xtuer.service.OrganizationService;
import com.xtuer.service.UserService;
import com.xtuer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationService orgService;

    /**
     * 使用 username 加载用户的信息，如密码，权限等
     *
     * @param username 登陆表单中用户输入的用户名
     * @return 返回查找到的用户对象
     * @throws UsernameNotFoundException 找不到用户异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long orgId = orgService.getCurrentOrganizationId(); // 当前机构 ID
        User user  = userService.findUser(username, orgId); // 数据库中查找用户

        if (user == null) {
            throw new UsernameNotFoundException(username + " not found!");
        }

        return SecurityUtils.buildUserDetails(user); // 构建 Spring Security 需要的用户信息 UserDetails
    }
}
