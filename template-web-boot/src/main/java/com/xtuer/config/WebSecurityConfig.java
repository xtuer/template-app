package com.xtuer.config;

import com.xtuer.bean.Urls;
import com.xtuer.security.JwtAuthenticationFilter;
import com.xtuer.security.LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 权限控制
        http.authorizeRequests()
                .antMatchers("/",
                        "/api/orgs",
                        "/api/login/users/current",
                        "/api/**").permitAll() // 不需要登录
                .antMatchers("/door").hasRole("ADMIN_SYSTEM") // 需要登录，且角色为 ADMIN_SYSTEM (不能加前缀 ROLE_)
                .anyRequest().authenticated(); // 需要登录，什么角色都可以

        // 登录页面的 URL: /login, GET 请求
        // 登录表单的 URL: /login, POST 请求
        http.formLogin()
                .loginPage(Urls.PAGE_LOGIN)
                .successHandler(loginSuccessHandler)
                .permitAll();

        // 注销的 URL: /logout，POST 请求
        http.logout().permitAll();

        // 使用 Jwt 时可禁用 Session: 需要同时设置 STATELESS 和禁用 CSRF 才能使得 Spring Security 不创建 Session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.csrf().disable();

        // 在登录表单的 Filter 前插入 JwtAuthenticationFilter，使用 Jwt 先尝试身份认证
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    // Security 忽略的路径不经过 Security 相关的 Filter 处理，不需要登录，并且访问效率高
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/static/**", "/static-p/**", "/static-m/**");
    }
}
