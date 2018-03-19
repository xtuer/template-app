package ebag.security;

import com.alibaba.fastjson.JSON;
import com.mzlion.easyokhttp.HttpClient;
import ebag.bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuthAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private String qqClientId = "101292272";
    private String qqClientSecret = "5bdbe9403fcc3abe8eba172337904b5a";

    private String QQ_ACCESS_TOKEN_URL = "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&redirect_uri=%s&code=%s";
    private String QQ_OPEN_ID_URL = "https://graph.qq.com/oauth2.0/me?access_token=%s";
    private String QQ_CALLBACK = "http://open.qtdebug.com:8080/oauth/qq/callback";

    public OAuthAuthenticationFilter() {
        super("/");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        return null;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // 被拦截到说明是 QQ 登陆成功的回调地址 http://host:port/oauth/qq/callback
        if (request.getRequestURI().startsWith("/oauth/qq/callback")) {
            // [1] 获取 code
            String code = request.getParameter("code");
            System.out.println("Code: " + code);

            // [2] 用 code 换取 access token
            // 响应: access_token=1A2CF189A4BBEE25CACE587CDD106512&expires_in=7776000&refresh_token=A5A3B6D90955ED6934EC42F2EECDA4BC
            String accessTokenUrl = String.format(QQ_ACCESS_TOKEN_URL, qqClientId, qqClientSecret, QQ_CALLBACK, code);
            String responseData = HttpClient.get(accessTokenUrl).execute().asString();
            String token = responseData.replaceAll("access_token=(.+)&expires_in=.+", "$1");
            System.out.println("Access Token: " + token);

            // [3] 用 access token 获取用户的 open ID
            // 响应: callback( {"client_id":"101292272","openid":"4584E3AAABFC5F052971C278790E9FCF"} );
            String openIdUrl = String.format(QQ_OPEN_ID_URL, token);
            responseData =HttpClient.get(openIdUrl).execute().asString();
            int start = responseData.indexOf("{");
            int end = responseData.lastIndexOf("}") + 1;
            String json = responseData.substring(start, end);
            String openId = JSON.parseObject(json).getString("openid");
            System.out.println("Open ID: " + openId);

            // [4] 使用 openId 查找用户
            User user = new User("admin", "----", "ROLE_ADMIN"); // 假设 admin 是使用 open id 查找到的用户吧
            // user = null; // user 赋值为 null，表示没找到用户

            if (user != null) {
                // [5] 用户存在，登陆成功，跳转到登陆前的页面
                Authentication auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
                super.successfulAuthentication(request, response, chain, auth); // 跳转到登陆前页面
            } else {
                // [6] 用户不存在，跳转到 "创建|绑定已有用户" 页面，
                // 绑定好用户后保存用户信息到: SecurityContextHolder.getContext().setAuthentication(auth)
                // 然后跳转到登陆前的页面
                DefaultRedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
                redirectStrategy.sendRedirect(request, response, "/page/bindUser");
            }

            return;
        } else if (request.getRequestURI().startsWith("/oauth/weixin/callback")) {

        }

        chain.doFilter(request, response);
    }

    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
}
