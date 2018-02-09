package com.xtuer.security;

import com.xtuer.bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 使用 token 进行身份验证的过滤器。
 * 如果 request header 中有 auth-token，使用 auth-token 的值查询对应的登陆用户，如果用户有效则放行访问，否则返回 401 错误。
 */
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    @Autowired
    private TokenService tokenService;

    private static ThreadLocal<Boolean> allowSessionCreation = new ThreadLocal<>(); // 是否允许当前请求创建 session

    public TokenAuthenticationFilter() {
        super("/"); // 参考 UsernamePasswordAuthenticationFilter
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        // 从 token 中提取 user，如果 user 不为 null，则用其创建一个 Authentication 对象
        String token = request.getHeader("auth-token");
        User user = tokenService.extractUser(token);

        if (user == null) {
            return null;
        } else {
            user.setPassword("un-demand-password");  // 密码不能为 null，但是也没有用，所以随便设置一个吧
            user = User.userForSpringSecurity(user); // 生成 Spring Security 可使用的用户对象

            return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        allowSessionCreation.set(true); // 默认创建 session

        // 如果 header 里有 auth-token 时，则使用 token 查询用户数据进行登陆验证
        String token = request.getHeader("auth-token");

        if (token != null) {
            // 1. 尝试进行身份认证
            // 2. 如果用户无效，则返回 401
            // 3. 如果用户有效，则保存到 SecurityContext 中，供本次方式后续使用
            Authentication auth = attemptAuthentication(request, response);

            // user 不为 null 者身份验证成功
            if (auth != null) {
                // 保存认证信息到 SecurityContext，禁止 HttpSessionSecurityContextRepository 创建 session
                allowSessionCreation.set(false);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else  {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 无效，请重新申请 token");
                return;
            }
        }

        // 继续调用下一个 filter: UsernamePasswordAuthenticationToken
        chain.doFilter(request, response);
    }

    public static boolean isAllowSessionCreation() {
        Boolean allow = allowSessionCreation.get();
        return allow == null ? true : allow; // 如果是 null，则说明没有设置过，使用默认的，也既是 true
    }

    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
}
