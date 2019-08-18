package training.security;

import training.bean.User;
import training.controller.Urls;
import training.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * 使用 token 进行身份验证的过滤器。
 * 如果 header 或者 cookie 中有 auth-token，获取它对应的用户，如果用户有效则放行访问，否则 AJAX 请求返回 401 错误，普通页面重定向到登录页。
 * 如果 header 或者 cookie 没有 auth-token，则进入下一个过滤器使用表单登录验证。
 */
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    @Autowired
    private TokenService tokenService;

    public TokenAuthenticationFilter() {
        super("/"); // 参考 UsernamePasswordAuthenticationFilter
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        // 从 token 中提取 user，如果 user 不为 null，则用其创建一个 Authentication 对象
        User user = tokenService.extractUser(WebUtils.getAuthToken(request));

        if (user == null) {
            return null;
        } else {
            user.protectPassword();  // 密码不能为 null，但是也没有用，所以随便设置一个吧
            user = user.cloneForSecurity(); // 生成 Spring Security 可使用的用户对象

            return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 如果 header 或者 cookie 里有 auth-token 时，则使用 token 查询用户数据进行登陆验证
        if (WebUtils.getAuthToken(request) != null) {
            // 1. 尝试进行身份认证
            // 2. 如果用户无效
            //    2.1. AJAX 请求返回 401，方便拦截统一处理
            //    2.2. 普通请求则删除 token，重定向到登录页
            // 3. 如果用户有效，则保存到 SecurityContext 中，供本次访问后续使用
            Authentication auth = attemptAuthentication(request, response);

            if (auth == null) {
                if (WebUtils.useAjax(request)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token 无效");
                } else {
                    WebUtils.deleteCookie(response, SecurityConstant.AUTH_TOKEN_KEY);
                    response.sendRedirect(Urls.PAGE_LOGIN);
                }

                return;
            } else  {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // 继续调用下一个 filter: UsernamePasswordAuthenticationToken
        chain.doFilter(request, response);
    }

    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
}
