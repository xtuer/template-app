package com.xtuer.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xtuer.bean.Role;
import com.xtuer.bean.User;
import com.xtuer.config.AppConfig;
import com.xtuer.util.Jwt;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * 生成 token 的 service.
 *
 * 用法:
 * 使用用户生成 Token: TokenService.generateToken(user)
 * 从 Token 提取用户: TokenService.extractUser(token)
 */
@Service
@Getter
@Setter
@Slf4j
public class JwtService {
    @Autowired
    protected AppConfig config;

    // 生成 token
    public String generateToken(User user) {
        // Token 中保存 id, username, nickname, roles
        long expiredAt = System.currentTimeMillis() + config.getAuthTokenDuration() * 1000L;
        return Jwt.create(config.getAppId(), config.getAppKey()).expiredAt(expiredAt)
                .param("userId",   user.getUserId() + "")
                .param("username", user.getUsername())
                .param("nickname", user.getNickname())
                .param("roles",    JSON.toJSONString(user.getRoles()))
                .token();
    }

    // 检测 token 的有效性
    public boolean checkToken(String token) {
        return Jwt.checkToken(token, config.getAppKey());
    }

    // 从 token 中提取用户
    public User extractUser(String token) {
        if (!this.checkToken(token)) {
            return null;
        }

        try {
            // 获取 token 中保存的 id, username, nickname, roles
            Map<String, String> params = Jwt.params(token);
            long         id   = Long.parseLong(params.get("userId"));
            String username   = params.get("username");
            String nickname   = params.get("nickname");
            Set<String> roles = JSON.parseObject(params.get("roles"), new TypeReference<Set<String>>() {});
            Role[] rolesX     = roles.stream().map(role -> Enum.valueOf(Role.class, role)).toArray(Role[]::new);

            User user = new User(id, username, "[protected]", rolesX);
            user.setNickname(nickname);

            return user;
        } catch (Exception ex) {
            return null;
        }
    }
}
