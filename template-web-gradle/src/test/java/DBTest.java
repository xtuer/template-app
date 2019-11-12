import com.edu.training.bean.Role;
import com.edu.training.bean.User;
import com.edu.training.mapper.UserMapper;
import com.edu.training.service.RepoFileService;
import com.edu.training.service.UserService;
import com.edu.training.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:config/application.xml"})
public class DBTest {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RepoFileService fileService;

    @Autowired
    private UserService userService;

    // gradle clean test --tests DBTest.testFindUser
    @Test
    public void testFindUser() {
        User user = userMapper.findUserById(1);
        Utils.dump(user);

        Utils.dump(userMapper.findUsersByOrgId(1, 0, 10));
    }

    @Test
    public void testMoveTempFileToRepo() {
        fileService.moveTempFileToRepo("/file/repo/332524228286873600.jpg");
    }

    @Test
    public void handleHtml() {
        System.out.println(fileService.moveTempFileToRepoInHtml("<a href=\"/file/temp/332804396779831296.png\">Go</a>"));
    }

    // 测试插入用户
    @Test
    public void testInsertUser() {
        User user = new User();
        user.setId(2);
        user.setUsername("test-user");
        user.setPassword("Passw0rd");
        user.setNickname("机构管理员");
        user.addRole(Role.ROLE_ADMIN_ORG);
        user.setOrgId(1);

        userService.createOrUpdateUser(user);
    }
}
