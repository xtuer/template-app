package edu.security;

import edu.bean.Role;
import edu.bean.User;
import edu.service.ConfigService;
import edu.service.OrganizationService;
import edu.service.UserService;
import edu.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登陆成功处理器，主要作用为创建 token 保存到 cookie，然后跳转到角色对应的页面
 */
public class AuthenticationSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {
    @Autowired
    private ConfigService config;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

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
        String username = request.getParameter(SecurityConstant.LOGIN_USERNAME);
        String password = request.getParameter(SecurityConstant.LOGIN_PASSWORD);
        long   orgId    = orgService.getCurrentOrganizationId();
        User   user     = userService.findUser(username, password, orgId);

        // [2] 创建用户的登录记录
        userService.createLoginRecord(user.getId(), user.getUsername());

        // [3] 生成用户的 token 保存到 cookie
        String token = tokenService.generateToken(user);
        WebUtils.writeCookie(response, SecurityConstant.AUTH_TOKEN_KEY, token, config.getAuthTokenDuration());

        // [4] 生成 Spring Security 可使用的用户对象，保存到 SecurityContext 供 Spring Security 接下来的鉴权使用
        user = user.cloneForSecurity();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // [5] 登录成功后根据用户的角色跳转到对应的页面
        if (user.hasRole(Role.ROLE_ADMIN_SYSTEM)) {
            response.sendRedirect("/page/admin-system");
        } else if (user.hasRole(Role.ROLE_ADMIN_SCHOOL)) {
            response.sendRedirect("/page/admin-school");
        } else {
            response.sendRedirect("/page/teacher");
        }
    }
}
