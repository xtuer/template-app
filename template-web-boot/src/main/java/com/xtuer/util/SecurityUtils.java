package com.xtuer.util;

import com.xtuer.bean.Role;
import com.xtuer.bean.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.stream.Collectors;

public final class SecurityUtils {
    /**
     * 判断当前用户是否已经登陆
     * @return 登陆状态返回 true, 否则返回 false
     */
    public static boolean isLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // URL 没有经过 Spring Security 登陆验证的 filter 时 auth 为 null
        if (auth == null) {
            return false;
        }

        String username = auth.getName();
        return !"anonymousUser".equals(username);
    }

    /**
     * 获取登陆用户的信息
     *
     * @return 返回登陆的用户，如果没有登陆则返回 null
     */
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // URL 没有经过 Spring Security 登陆验证的 filter 时 auth 为 null
        if (auth == null) {
            return null;
        }

        Object p = auth.getPrincipal();
        return (p instanceof User) ? (User) p : null;
    }

    /**
     * 使用用户创建 UserDetails 给 SpringSecurity 使用
     *
     * @param user 用户对象
     * @return 返回用户对应的 UserDetails
     */
    public static UserDetails buildUserDetails(User user) {
        // 枚举角色转为字符串角色
        String[] roles = user.getRoles().stream().map(Role::name).toArray(String[]::new);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, true, true,
                AuthorityUtils.createAuthorityList(roles)
        );
    }
}
