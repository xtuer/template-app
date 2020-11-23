import com.alibaba.fastjson.JSON;
import com.xtuer.bean.Result;
import com.xtuer.bean.Role;
import com.xtuer.bean.User;
import org.junit.jupiter.api.Test;

public class ResultTest {
    @Test
    void toJson() {
        // Result
        Result<User> r1 = Result.ok();
        Result<User> r2 = Result.ok(new User("Alice", "Passw0rd", Role.ROLE_ADMIN_SYSTEM));

        // JSON
        System.out.println(JSON.toJSONString(r1));
        System.out.println(JSON.toJSONString(r2));

        System.out.println(JSON.toJSONString(r2.getData(), true));

        // JSONP
        System.out.println(Result.jsonp("callback", Result.ok("Hello")));

        // Message ARg
        System.out.println(Result.fail("你好{}", "小明").getMessage());
    }
}
