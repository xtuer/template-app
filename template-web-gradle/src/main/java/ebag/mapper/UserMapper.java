package ebag.mapper;

import ebag.bean.User;

import java.util.List;

public interface UserMapper {
    /**
     * 使用 ID 查找用户
     *
     * @param userId 用户 ID
     * @return 返回用户对象
     */
    User findUserById(long userId);

    /**
     * 查找学校的用户
     *
     * @param schoolId 学校
     * @param offset   起始位置
     * @param count    数量
     * @return 返回用户列表
     */
    List<User> findUsersBySchoolId(long schoolId, int offset, int count);

    /**
     * 使用用户名和密码查找学校下的用户
     *
     * @param username 用户名
     * @param password 密码
     * @param schoolId 学校 ID
     * @return 返回用户对象
     */
    User findUserByUsernameAndPasswordAndSchoolId(String username, String password, long schoolId);

    /**
     * 插入或更新用户: 如果 ID 存在或者 school_id + username 的组合存在则进行更新
     *
     * @param user 用户
     */
    void insertOrUpdateUser(User user);
}
