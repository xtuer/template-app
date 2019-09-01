package com.edu.training.util;

import com.edu.training.bean.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
    public static User getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // URL 没有经过 Spring Security 登陆验证的 filter 时 auth 为 null
        if (auth == null) {
            return null;
        }

        Object p = auth.getPrincipal();
        return (p instanceof User) ? (User) p : null;
    }
}
