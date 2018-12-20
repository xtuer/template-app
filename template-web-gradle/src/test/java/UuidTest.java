import edu.util.Utils;
import org.junit.Assert;
import org.junit.Test;

public class UuidTest {
    @Test
    public void testUuid() {
        String uuid1 = Utils.uuid();
        String uuid2 = Utils.uuid();

        System.out.println(uuid1);

        Assert.assertNotEquals(uuid1, uuid2);
    }
}
