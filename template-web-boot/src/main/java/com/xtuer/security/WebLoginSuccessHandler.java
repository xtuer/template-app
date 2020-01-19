package com.xtuer.security;

import com.xtuer.bean.SecurityConst;
import com.xtuer.bean.User;
import com.xtuer.config.AppConfig;
import com.xtuer.service.OrganizationService;
import com.xtuer.service.UserService;
import com.xtuer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * 网页 Form 表单登陆成功处理器，主要作用为创建 token 保存到 cookie，然后跳转到角色对应的页面
 */
@Component
public class WebLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private AppConfig config;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OrganizationService orgService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // 1. 获取登录用户 (访问这个函数，说明是通过表单成功登录过来的，一定能够查询到用户)
        // 2. 创建用户的登录记录
        // 3. 生成用户的 token 保存到 cookie
        // 4. 生成 Spring Security 可使用的用户对象，保存到 SecurityContext 供 Spring Security 接下来的鉴权使用
        // 5. 登录成功后根据用户的角色跳转到对应的页面

        // [1] 获取登录用户 (访问这个函数，说明是通过表单成功登录过来的，一定能够查询到用户)
        String username = request.getParameter(SecurityConst.LOGIN_USERNAME);
        String password = request.getParameter(SecurityConst.LOGIN_PASSWORD);
        long   orgId    = orgService.getCurrentOrganizationId();
        User   user     = userService.findUser(username, password, orgId);

        // [2] 创建用户的登录记录
        // [3] 生成用户的 token 保存到 cookie
        userService.loginToken(user, response);

        // [4] 生成 Spring Security 可使用的用户对象，保存到 SecurityContext 供 Spring Security 接下来的鉴权使用
        Collection<? extends GrantedAuthority> authorities = SecurityUtils.buildUserDetails(user).getAuthorities();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // [5] 登录成功后根据用户的角色跳转到对应的页面
        userService.redirectToUserBackendPage(user, response);
    }
}
