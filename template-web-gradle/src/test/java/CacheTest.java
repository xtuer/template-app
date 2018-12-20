import bean.Fox;
import edu.bean.RedisKey;
import edu.bean.clazz.Student;
import edu.mapper.ClazzMapper;
import edu.service.RedisDao;
import edu.service.TeachingService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * redis 缓存测试
 *
 * 2018/7/20
 */
@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:application-test.xml"})
public class CacheTest {
    @Autowired
    private RedisDao redisDao;

    @Autowired
    private ClazzMapper clazzMapper;

    @Autowired
    private TeachingService teachingService;

    @Test
    public void testSet() {
        Long clazzId = 168172211088130049L;
        String key = String.format(RedisKey.CLAZZ_STUDENTS, clazzId); // "clazz:{clazzId}:students";
        Set<Long> set = redisDao.getFromSet(key, Long.class, () -> clazzMapper.findStudentsByClazzId(clazzId).stream().map(Student::getId).collect(Collectors.toSet()), 100);
        System.out.println(set);
    }

    @Test
    public void testIsMember() {
        Long studentId = 168172211788578826L;
        Long clazzId = 168172211088130049L;
        String key = String.format(RedisKey.CLAZZ_STUDENTS, clazzId); // "clazz:{clazzId}:students";

        boolean member = redisDao.isMember(key, studentId.toString());
        System.out.println("isMember? " + member);
    }

    @Test
    public void testIsThisClazz() {
        Long studentId = 168172211788578826L;
        Long clazzId = 168172211088130049L;
        boolean studentInClazz = teachingService.isStudentInClazz(studentId, clazzId);
        System.out.println("studentInClazz: " + studentInClazz);
    }

    @Test
    public void testSetLong() {
        Set<Long> cachedIds = redisDao.getFromSet("test-set-ids", Long.class, () -> {
            Set<Long> ids = new TreeSet<>();
            ids.add(1L);
            ids.add(2L);
            ids.add(3L);
            ids.add(4L);

            return ids;
        }, 1000);

        cachedIds.forEach(id -> {
            System.out.println(id + 1);
        });
    }

    @Test
    public void testSetWithObject() {
        Set<Fox> cachedFoxes = redisDao.getFromSet("test-set-groups", Fox.class, () -> {
            System.out.println("Create foxes");
            Set<Fox> foxes = new HashSet<>();
            foxes.add(new Fox(1, "Alice"));
            foxes.add(new Fox(2, "Bob"));
            foxes.add(new Fox(3, "Cherry"));
            return foxes;
        }, 1000);

        cachedFoxes.forEach(f -> System.out.println(f.getName()));
    }
}
