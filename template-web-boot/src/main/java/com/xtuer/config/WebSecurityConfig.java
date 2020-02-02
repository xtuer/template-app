package com.xtuer.config;

import com.xtuer.bean.Urls;
import com.xtuer.security.JwtAuthenticationFilter;
import com.xtuer.security.WebLoginSuccessHandler;
import com.xtuer.security.WebLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private WebLoginSuccessHandler webLoginSuccessHandler;

    @Autowired
    private WebLogoutSuccessHandler webLogoutSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 权限控制
        http.authorizeRequests()
                // 不需要登录的 URL
                .antMatchers(
                        "/",
                        "/login",
                        "/logout",
                        "/api/pt/**",
                        "/api/demo/**",
                        Urls.API_ORGS,
                        Urls.API_LOGIN_TOKENS,
                        Urls.API_LOGIN_USERS_CURRENT
                ).permitAll()
                // 需要对应角色的 URL (此处不能加前缀 ROLE_，但是数据库里需要加 ROLE_)
                .antMatchers("/door").hasRole("ADMIN_SYSTEM")
                // 需要登录，但不限角色
                .anyRequest().authenticated();

        // 登录页面的 URL: /login, GET 请求
        // 登录表单的 URL: /login, POST 请求
        http.formLogin()
                .loginPage(Urls.PAGE_LOGIN)
                .successHandler(webLoginSuccessHandler)
                .permitAll();

        // 注销的 URL: /logout，GET 或者 POST 请求
        http.logout()
                .logoutSuccessHandler(webLogoutSuccessHandler)
                .permitAll();

        // 使用 Jwt 时可禁用 Session: 需要同时设置 STATELESS 和禁用 CSRF 才能使得 Spring Security 不创建 Session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.csrf().disable();

        // 在登录表单的 Filter 前插入 JwtAuthenticationFilter，使用 Jwt 先尝试身份认证
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    // 配置 Spring Security 忽略的路径，一般为静态资源
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/static/**", "/static-p/**", "/static-m/**");
    }
}
