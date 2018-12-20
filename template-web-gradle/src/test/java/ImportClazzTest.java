import com.alibaba.fastjson.JSON;
import edu.bean.Result;
import edu.bean.Role;
import edu.bean.User;
import edu.bean.clazz.Clazz;
import edu.bean.clazz.Student;
import edu.bean.clazz.Teacher;
import edu.service.InitSchoolService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * 导入班级、老师、学生到数据库
 * 执行 SQL 语句插入数据库前进行数据校验
 *
 * 提示：导入时使用 schoolId+班级编码 为班级唯一标志，导入后使用 SQL 把其更新为对应的 clazzId
 */
@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:config/application.xml"})
public class ImportClazzTest {
    @Autowired
    private InitSchoolService initSchoolService;

    private static final long SCHOOL_ID = 123467890L;
    private static final File VALID_EXCEL   = new File("/Users/Biao/Documents/workspace/new-ebag-web/ebag-web-app/impex/账号模板.xlsx");
    private static final File INVALID_EXCEL = new File("/Users/Biao/Documents/workspace/new-ebag-web/ebag-web-app/impex/账号模板-invalid.xlsx");

    /**
     * 测试加载正确的数据
     */
    @Test
    public void loadClazzesAndTeachersAndStudentsOnValid() throws Exception {
        try (InputStream in = new FileInputStream(VALID_EXCEL)) {
            List<Clazz>   clazzes  = new LinkedList<>();
            List<Teacher> teachers = new LinkedList<>();
            List<Student> students = new LinkedList<>();

            initSchoolService.loadClazzesAndTeachersAndStudents(in, SCHOOL_ID, clazzes, teachers, students);
            System.out.println(JSON.toJSONString(clazzes, true));
            System.out.println(JSON.toJSONString(teachers, true));
            System.out.println(JSON.toJSONString(students, true));

            List<String> errorMessages = new LinkedList<>();
            initSchoolService.validateClazzesAndTeachersAndStudents(clazzes, teachers, students, errorMessages);
            System.out.println(String.join("\n", errorMessages));
        }
    }

    /**
     * 测试加载有错误的数据，查看错误信息
     */
    @Test
    public void loadClazzesAndTeachersAndStudentsOnInvalid() throws Exception {
        try (InputStream in = new FileInputStream(INVALID_EXCEL)) {
            List<Clazz>   clazzes  = new LinkedList<>();
            List<Teacher> teachers = new LinkedList<>();
            List<Student> students = new LinkedList<>();

            initSchoolService.loadClazzesAndTeachersAndStudents(in, SCHOOL_ID, clazzes, teachers, students);
            System.out.println(JSON.toJSONString(clazzes, true));
            System.out.println(JSON.toJSONString(teachers, true));
            System.out.println(JSON.toJSONString(students, true));

            List<String> errorMessages = new LinkedList<>();
            initSchoolService.validateClazzesAndTeachersAndStudents(clazzes, teachers, students, errorMessages);
            System.out.println(String.join("\n", errorMessages));
        }
    }

    /**
     * 测试导入正确账号到数据库
     */
    @Test
    public void importUsersToDbOnValid() throws Exception {
        try (InputStream in = new FileInputStream(VALID_EXCEL)) {
            Result<String> result = initSchoolService.importClazzesAndTeachersAndStudents(SCHOOL_ID, in);
            System.out.println(JSON.toJSONString(result));
        }
    }

    /**
     * 测试导入错误账号到数据库
     */
    @Test
    public void importUsersToDbOnInvalid() throws Exception {
        try (InputStream in = new FileInputStream(INVALID_EXCEL)) {
            Result<String> result = initSchoolService.importClazzesAndTeachersAndStudents(SCHOOL_ID, in);
            System.out.println(JSON.toJSONString(result));
        }
    }

    /**
     * 测试复制 bean 之间的属性，因为在导入老师和学生时用到
     */
    @Test
    public void testCopyProperties() {
        User user = new User();
        Student student = new Student();
        student.setId(1L).setUsername("Alice").setPassword("111111");

        BeanUtils.copyProperties(student, user);
        user.setRole(Role.ROLE_STUDENT);
        System.out.println(JSON.toJSONString(user, true));
    }
}
