package com.xtuer.security;

import com.xtuer.bean.SecurityConst;
import com.xtuer.bean.Urls;
import com.xtuer.bean.User;
import com.xtuer.config.AppConfig;
import com.xtuer.util.SecurityUtils;
import com.xtuer.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * 使用 token 进行身份验证的过滤器。
 * 如果 header 或者 cookie 中有 auth-token，获取它对应的用户，如果用户有效则放行访问，否则 AJAX 请求返回 401 错误，普通页面重定向到登录页。
 * 如果 header 或者 cookie 没有 auth-token，则进入下一个过滤器使用表单登录验证。
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private AppConfig config;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 1. 如果已经通过认证，则执行下一个 filter
        // 2. 从 Header 或者 Cookie 里获取 token
        //    2.1 如果 token 为 null，则执行下一个 filter
        //    2.2 如果 token 不为 null，从 token 中提取 user
        // 3. user 为空则说明 token 无效
        //    3.1 删除 cookie 里的 token，避免多次重定向
        //    3.2 AJAX 请求返回 401，方便拦截统一处理
        //    3.3 普通请求重定向到登录页
        // 4. user 不为空，则认证成功，把用户信息放入安全上下文中，供 Spring Security 后续使用

        // [1] 如果已经通过认证，则执行下一个 filter
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        // [2] 从 Header 或者 Cookie 里获取 token
        String token = WebUtils.getAuthToken(request);

        // [2.1] 如果 token 为 null，则执行下一个 filter
        if (null == token) {
            chain.doFilter(request, response);
            return;
        }

        // [2.2] 如果 token 不为 null，从 token 中提取 user
        User user = jwtService.extractUser(token);

        if (user == null) {
            // [3] user 为空则说明 token 无效
            log.warn("[失败] Token 无效: {}", token);

            // [3.1] 删除 cookie 里的 token，避免多次重定向
            // [3.2] AJAX 请求返回 401，方便拦截统一处理
            // [3.3] 普通请求重定向到登录页
            WebUtils.deleteCookie(response, SecurityConst.AUTH_TOKEN_KEY);

            if (WebUtils.useAjax(request)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 无效");
            } else {
                response.sendRedirect(Urls.PAGE_LOGIN);
            }

            return; // 返回，不继续执行下一个 filter
        } else {
            // [4] user 不为空，则认证成功，把用户信息放入安全上下文中，供 Spring Security 后续使用
            Collection<? extends GrantedAuthority> authorities = SecurityUtils.buildUserDetails(user).getAuthorities();
            Authentication auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 如果 header 里有 save-auth-token: true，则保存 token 到 cookie
            // 移动端使用 web view 打开页面时通过 header 设置 auth-token，通过这样的方式把 auth-token 持久化到 cookie 里，
            // 以后页面中的访问都能带上用户登录信息了
            if ("true".equals(request.getHeader(SecurityConst.SAVE_AUTH_TOKEN_KEY))) {
                WebUtils.writeCookie(response, SecurityConst.AUTH_TOKEN_KEY, token, config.getAuthTokenDuration());
            }
        }

        // 继续调用下一个 filter: UsernamePasswordAuthenticationToken
        chain.doFilter(request, response);
    }
}
