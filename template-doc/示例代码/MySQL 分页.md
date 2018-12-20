分页涉及以下几个变量: 

* **pageSize**: 每页显示的记录个数
* **pageCount**: 总页数
* **pageNumber**: 要查询的页码，最小值是 1，也就是页码从 1 开始
* **recordCount**: 记录总数
* **offset**: 查询的起始位置，从 0 开始

总页数 `pageCount = recordCount / pageSize`，起始位置 `offset = (pageNumber-1) * pageSize`，SQL 语句如 `SELECT * FROM question LIMIT 10, 25`，10 是 offset, 25 是 pageSize，在我们的项目里，可以使用 `PageUtils` 来计算:

```java
int offset = PageUtils.offset(pageNumber, pageSize); // 用于计算起始位置
int pageCount = PageUtils.pageCount(recordCount, pageSize); // 用于计算共有多少页
```

分页查询一般有三种方式:

* 使用二个请求：第一个请求查询总共有多少页，第二个请求查询指定页的数据，需要提供两个接口
* 使用一个请求：直接请求查询指定页的数据，例如网页上点击 `MORE` 按钮，加载下一页的数据，当到最后一页后，`MORE` 按钮消失，只需要一个接口，但不知道总页数，浏览器的滚动条触底后自动加载下一页的功能现在很流行，可以使用此接口实现
* 使用一个请求：每次请求都进行两次 SQL 查询，第一个查询总共有多少页，第二个查询指定页的数据，然后拼成一个结果返回，接口简单了，但是每次请求都要进行一次额外的总页数查询，效率不是很高

下面的分页查询介绍第一种的实现方式请求题目，第二种方式是第一种方式的部分。

> 提示：如果数据量很大如有 100 万条，直接 limit offset, count 进行分页查询的话效率会很低，这时记录前一页最大的 ID 为 previousPageMaxId，在分页查询的 SQL 中加上 where id>previousPageMaxId 进行过滤就能大幅度的提高查询效率。

## 服务器

1. 定义 URL

2. 定义 Controller

   ```java
   @Controller
   public class SchoolController {
       /**
        * 查找当前域名学校下的学生
        * URL: http://localhost:8080/api/students
        *
        * @param pageNumber 页码
        * @param pageSize   数量
        * @return payload 为学生的数组
        */
       @GetMapping(Urls.API_STUDENTS)
       @ResponseBody
       public Result<List<Student>> findStudentsBySchool(@RequestParam(defaultValue="1") int pageNumber,
                                                         @RequestParam(defaultValue="100") int pageSize) {
           long schoolId = schoolService.getSchoolId();
           int offset = PageUtils.offset(pageNumber, pageSize);
           return Result.ok(clazzMapper.findStudentsBySchoolId(schoolId, offset, count));
       }
   }
   ```

3. 定义 Mapper

   ```java
   public interface ClazzMapper {
       /**
        * 查找学校下的学生
        *
        * @param schoolId 学校 ID
        * @param offset   分页的起始位置
        * @param count    分页的记录数量
        * @return 返回学生列表，如无学生返回空的列表
        */
       List<Student> findStudentsBySchoolId(long schoolId, int offset, int count);
   }
   ```

   ```xml
   <mapper namespace="ebag.mapper.ClazzMapper">
       <!-- 查找学校下的学生 -->
       <select id="findStudentsBySchoolId" resultMap="studentResultMap">
           SELECT
               user.id         AS id,
               user.username   AS username,
               user.nickname   AS nickname,
               user.school_id  AS school_id,
               clazz.name      AS clazz_name,
               clazz.code      AS clazz_code,
               clazz.id        AS clazz_id,
               ''              AS clazz_subject
           FROM clazz_student  AS cs
               LEFT JOIN user  ON user.school_id  = #{schoolId} AND user.id  = cs.student_id
               LEFT JOIN clazz ON clazz.school_id = #{schoolId} AND clazz.id = cs.clazz_id
           WHERE cs.school_id = #{schoolId} LIMIT ${offset}, ${count};
       </select>
   </mapper>
   ```

   > **提示**: 
   >
   > * 注意 MyBatis 传递多个参数的方式: 
   >
   >   * 使用 `@Param` 标记参数名，这样 xml 文件里就能直接访问了
   >
   >   * 使用 Java8 的编译参数 `-parameters` 编译项目，这样连 `@Param` 都省了
   >
   >     ```java
   >     List<Question> questions(@Param("offset") int offset, @Param("size") int size);
   >     可以替换为
   >     List<Question> questions(int offset, int size);
   >     ```
   >
   >
   > * 应该在每一个地方都加上注释，虽然繁琐，但是清晰，一下子就能看懂对应的功能
   >
   > * SQL 语句中关键字应该大写: SELECT, UPDATE, FROM, DROP, WHERE, AND, OR, LIMIT, ...
   >

   ## 浏览器

   请参考[前端分页查询](/前端代码/分页查询.html)

   ​

   ​

   ​