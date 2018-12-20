package edu.mapper;

import edu.bean.User;

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
     * 使用用户名查找学校下的用户 (因为使用的是 BCrypt 加密密码，每次计算出的密码都是不同的，所以不能使用密码进行查询)
     *
     * @param username 用户名
     * @param schoolId 学校 ID
     * @return 返回用户对象
     */
    User findUserByUsernameAndSchoolId(String username, long schoolId);

    /**
     * 插入或更新用户: 如果 ID 存在或者 school_id + username 的组合存在则进行更新
     *
     * @param user 用户
     */
    void insertOrUpdateUser(User user);

    /**
     * 更新用户的登录状态: 登录次数+1，记录最后的登录时间
     *
     * @param userId 用户 ID
     */
    void updateUserLoginStatus(long userId);

    /**
     * 更新用户的昵称
     *
     * @param userId   用户的 ID
     * @param nickname 用户的昵称
     */
    void updateUserNickname(long userId, String nickname);

    /**
     * 更新用户的头像
     *
     * @param userId 用户 ID
     * @param avatar 头像的 URL
     */
    void updateUserAvatar(long userId, String avatar);

    /**
     * 更新用户的性别
     *
     * @param userId 用户的 ID
     * @param gender 用户的性别
     */
    void updateUserGender(long userId, int gender);

    /**
     * 更新用户的手机号
     *
     * @param userId 用户的 ID
     * @param mobile 用户的手机号
     */
    void updateUserMobile(long userId, String mobile);

    /**
     * 更新用户的密码
     *
     * @param userId   用户的 ID
     * @param password 用户的密码
     */
    void updateUserPassword(long userId, String password);
}
