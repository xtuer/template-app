import com.edu.training.bean.Page;
import com.edu.training.util.PageUtils;
import org.junit.Assert;
import org.junit.Test;

public class PageUtilsTest {
    @Test
    public void testOffset() {
        Assert.assertEquals(0,  PageUtils.offset(0,  10));
        Assert.assertEquals(0,  PageUtils.offset(1,  10));
        Assert.assertEquals(80, PageUtils.offset(9,  10));
        Assert.assertEquals(90, PageUtils.offset(10, 10));
    }

    @Test
    public void testPageCount() {
        Assert.assertEquals(1, PageUtils.pageCount(0,  10));
        Assert.assertEquals(1, PageUtils.pageCount(1,  10));
        Assert.assertEquals(1, PageUtils.pageCount(9,  10));
        Assert.assertEquals(1, PageUtils.pageCount(10, 10));
        Assert.assertEquals(2, PageUtils.pageCount(15, 10));
        Assert.assertEquals(2, PageUtils.pageCount(20, 10));
        Assert.assertEquals(3, PageUtils.pageCount(21, 10));
    }

    @Test
    public void testPage() {
        Assert.assertEquals(0, Page.of(0, 10).getOffset());
        Assert.assertEquals(0, Page.of(1, 10).getOffset());
        Assert.assertEquals(80, Page.of(9, 10).getOffset());
        Assert.assertEquals(90, Page.of(10, 10).getOffset());
    }
}
