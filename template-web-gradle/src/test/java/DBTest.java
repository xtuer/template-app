import edu.bean.User;
import edu.mapper.UserMapper;
import edu.service.FileService;
import edu.util.Utils;
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
    private FileService fileService;

    @Test
    public void testFindUser() {
        User user = userMapper.findUserById(1);
        Utils.dump(user);

        Utils.dump(userMapper.findUsersByOrgId(1, 0, 10));
    }

    @Test
    public void testMoveTempFileToRepo() {
        fileService.moveFileToRepo("/file/repo/332524228286873600.jpg");
    }

    @Test
    public void handleHtml() {
        System.out.println(fileService.moveFileToRepoInHtml("<a href=\"/file/temp/332804396779831296.png\">Go</a>"));
    }
}
