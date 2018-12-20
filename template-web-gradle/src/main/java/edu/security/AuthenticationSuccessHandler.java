package edu.security;

import edu.bean.Role;
import edu.bean.User;
import edu.service.UserService;
import edu.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    // 身份认证 token 的有效期
    @Value("${authTokenDuration}")
    private int authTokenDuration;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        // 用户名密码查询用户 (访问这个函数，说明是通过表单成功登录过来的，使用用户名密码一定能够查询到用户)
        String username = request.getParameter(SecurityConstant.LOGIN_USERNAME);
        String password = request.getParameter(SecurityConstant.LOGIN_PASSWORD);
        User   user     = userService.findUser(username, password, 0);
        userService.updateUserLoginStatus(user.getId()); // 更新登录状态

        // 生成 token 保存到 cookie
        String token = tokenService.generateToken(user);
        WebUtils.writeCookie(response, SecurityConstant.AUTH_TOKEN_KEY, token, authTokenDuration);

        // 生成 Spring Security 可使用的用户对象，保存到 SecurityContext 供 Spring Security 使用
        user = User.userForSpringSecurity(user);
        Authentication auth =  new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 登录成功后根据用户的角色进行跳转
        if (Role.ROLE_ADMIN_SYSTEM.equals(user.getRole())) {
            response.sendRedirect("/page/admin-system");
        } else if (Role.ROLE_ADMIN_SCHOOL.equals(user.getRole())) {
            response.sendRedirect("/page/admin-school");
        } else {
            response.sendRedirect("/page/teacher");
        }
    }
}
