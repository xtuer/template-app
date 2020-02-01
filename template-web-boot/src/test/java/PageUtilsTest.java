import com.xtuer.bean.Page;
import com.xtuer.util.PageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class PageUtilsTest {
    @Test
    public void testOffset() {
        Assertions.assertEquals(0,  PageUtils.offset(0,  10));
        Assertions.assertEquals(0,  PageUtils.offset(1,  10));
        Assertions.assertEquals(80, PageUtils.offset(9,  10));
        Assertions.assertEquals(90, PageUtils.offset(10, 10));
    }

    @Test
    public void testPageCount() {
        Assertions.assertEquals(1, PageUtils.pageCount(0,  10));
        Assertions.assertEquals(1, PageUtils.pageCount(1,  10));
        Assertions.assertEquals(1, PageUtils.pageCount(9,  10));
        Assertions.assertEquals(1, PageUtils.pageCount(10, 10));
        Assertions.assertEquals(2, PageUtils.pageCount(15, 10));
        Assertions.assertEquals(2, PageUtils.pageCount(20, 10));
        Assertions.assertEquals(3, PageUtils.pageCount(21, 10));
    }

    @Test
    public void testPage() {
        Assertions.assertEquals(0,  Page.of(0, 10).getOffset());
        Assertions.assertEquals(0,  Page.of(1, 10).getOffset());
        Assertions.assertEquals(80, Page.of(9, 10).getOffset());
        Assertions.assertEquals(90, Page.of(10, 10).getOffset());
    }
}
