package ebag.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import ebag.bean.User;
import ebag.util.Jwt;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * 生成 token 的 service.
 */
@Getter
@Setter
public class TokenService {
    @Value("${authTokenDuration}")
    private int authTokenDuration; // 身份认证 token 的有效期

    @Value("${appId}")
    private String appId; // 应用的 ID

    @Value("${appKey}")
    private String appKey; // 应用的秘钥，可以定期更换

    // 生成 token
    public String generateToken(User user) {
        // Token 中保存 id, username, roles
        long expiredAt = System.currentTimeMillis() + authTokenDuration * 1000L;
        return Jwt.create(appId, appKey).expiredAt(expiredAt)
                .param("id", user.getId() + "")
                .param("username", user.getUsername())
                .param("roles", JSON.toJSONString(user.getRoles().toArray(new String[0])))
                .token();
    }

    // 检测 token 的有效性
    public boolean checkToken(String token) {
        return Jwt.checkToken(token, appKey);
    }

    // 从 token 中提取用户
    public User extractUser(String token) {
        if (!this.checkToken(token)) {
            return null;
        }

        try {
            // 获取 token 中保存的 id, username, roles
            Map<String, String> params = Jwt.params(token);
            Long         id = Long.parseLong(params.get("id"));
            String username = params.get("username");
            String[]  roles = JSON.parseObject(params.get("roles"), new TypeReference<String[]>() {});

            return new User(id, username, "no-password", roles);
        } catch (Exception ex) {
            return null;
        }
    }

    public static void main(String[] args) {
        TokenService service = new TokenService();

        // 创建用户对象
        User user = new User(1234L, "Biao", "---", "ROLE_ADMIN", "ROLE_STAFF");
        user.setMail("biao.mac@icloud.com");

        // 使用 user 生成 token
        String token = service.generateToken(user);
        System.out.println(token);

        // 检测 token 是否有效
        System.out.println(service.checkToken(token));

        // 从 token 中提取用户
        user = service.extractUser(token);
        System.out.println(JSON.toJSONString(user));
    }
}
