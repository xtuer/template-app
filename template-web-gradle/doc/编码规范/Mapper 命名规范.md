Mapper 中方法的命名：`动词后面跟上返回类型名`、`参数部分参考 JPA 属性查询的命名规范`，方法名要尽量的能够望文生义，以查询用户为例大致如下：

|     描述               |         方法                                                     |
| ------------------ | ------------------------------------------------------------ |
| 使用 id 查询用户   | 我们: `User findUserById(long userId)`<br>JPA: `User findById(long id)` |
| 用户名密码查询用户 | 我们: `User findUserByUsernameAndPassword(String username, String password)`<br>JPA: `User findByUsernameAndPassword(String username, String password)` |
| 查询多个用户       | 我们: `List<User> findUsersBySchoolId(long schoolId)` (User 用复数)<br>JPA: `List<User> findBySchoolId(long schoolId)` |
| 插入用户           | `void insertUser(User user)`                                 |
| 更新用户           | `void updateUser(User user)`                                 |
| 删除用户           | `void deleteUser(long userId)`                               |
| 插入或更新用户     | `void insertOrUpdateUser(User user)`                         |
| 更新用户昵称       | `void updateUserNickname(long userId, String nickname)`      |

## JPA 属性查询命名规范

| Key Word    | SQL                              | Method                                                |
| ----------- | -------------------------------- | ----------------------------------------------------- |
| And         | 等价于 SQL 中的 `AND` 关键字     | `findByUsernameAndPassword(String user, Striang pwd)` |
| Or          | 等价于 SQL 中的 `OR` 关键字      | `findByUsernameOrAddress(String user, String addr)`   |
| Between     | 等价于 SQL 中的 `BETWEEN` 关键字 | `findBySalaryBetween(int max, int min)`               |
| LessThan    | 等价于 SQL 中的 `<`              | `findBySalaryLessThan(int max)`                       |
| GreaterThan | 等价于 SQL 中的 `>`              | `findBySalaryGreaterThan(int min)`                    |
| IsNull      | 等价于 SQL 中的 `IS NULL`        | `findByUsernameIsNull()`                              |
| IsNotNull   | 等价于 SQL 中的 `IS NOT NULL`    | `findByUsernameIsNotNull()`                           |
| NotNull     | 与 `IsNotNull` 等价              |                                                       |
| Like        | 等价于 SQL 中的 `LIKE`           | `findByUsernameLike(String user)`                     |
| NotLike     | 等价于 SQL 中的 `NOT LIKE`       | `findByUsernameNotLike(String user)`                  |
| OrderBy     | 等价于 SQL 中的 `ORDER BY`       | `findByUsernameOrderBySalaryAsc(String user)`         |
| Not         | 等价于 SQL 中的 `!=`             | `findByUsernameNot(String user)`                      |
| In          | 等价于 SQL 中的 `IN`             | `findByUsernameIn(Collection<String> userList)`       |
| NotIn       | 等价于 SQL 中的 `NOT IN`         | `findByUsernameNotIn(Collection<String> userList)`    |


![](../img/jpa-name-convention-1.png)

![](../img/jpa-name-convention-2.png)