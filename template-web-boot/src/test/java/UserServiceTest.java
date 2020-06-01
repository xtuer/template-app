import com.xtuer.Application;
import com.xtuer.bean.User;
import com.xtuer.service.UserService;
import com.xtuer.util.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { Application.class })
// @ActiveProfiles({ "mac" }) // 指定测试的 active profile (dev, default)
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    public void findUser() {
        User user = userService.findUser("admin", 1);
        Utils.dump(user);
    }
}
