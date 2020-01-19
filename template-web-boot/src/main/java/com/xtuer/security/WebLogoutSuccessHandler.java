package com.xtuer.security;

import com.xtuer.bean.SecurityConst;
import com.xtuer.util.WebUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 网页上注销成功的处理器: 删除 Cookie 中的 auth-token，security 中的身份信息，然后跳转到登录页
 */
@Component
public class WebLogoutSuccessHandler implements LogoutSuccessHandler {
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        SecurityContextHolder.getContext().setAuthentication(null);
        WebUtils.deleteCookie(response, SecurityConst.AUTH_TOKEN_KEY);
        response.sendRedirect("/login?logout");
    }
}
