import edu.util.Utils;
import org.junit.Test;

public class PasswordTest {
    @Test
    public void testPasswordMinLength() {
        String rawPassword = "1";
        String encryptedPassword = Utils.passwordByBCrypt(rawPassword);
        System.out.println(Utils.isPasswordValidByBCrypt(rawPassword, encryptedPassword));
    }
}
