import com.alibaba.fastjson.JSON;
import com.edu.training.bean.User;
import com.edu.training.security.TokenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:config/application.xml"})
public class TokenServiceTest {
    @Autowired
    TokenService tokenService;

    @Test
    public void checkToken() {
        // 创建用户对象
        User user = new User(1234L, "Biao", "---", "ROLE_ADMIN");
        user.setEmail("biao.mac@icloud.com");
        user.setNickname("二狗");

        // 使用 user 生成 token
        String token = tokenService.generateToken(user);
        System.out.println(token);

        // 检测 token 是否有效
        System.out.println(tokenService.checkToken(token));

        // 从 token 中提取用户
        user = tokenService.extractUser(token);
        System.out.println(JSON.toJSONString(user));
    }
}
