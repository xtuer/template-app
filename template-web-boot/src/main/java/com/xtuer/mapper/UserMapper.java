package com.xtuer.mapper;

import com.xtuer.bean.Role;
import com.xtuer.bean.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户的 Mapper
 */
@Mapper
public interface UserMapper {
    /**
     * 使用 ID 查找用户
     *
     * @param userId 用户 ID
     * @return 返回用户对象
     */
    User findUserById(long userId);

    /**
     * 使用用户名查找学校下的用户 (因为使用的是 BCrypt 加密密码，每次计算出的密码都是不同的，所以不能使用密码进行查询)
     *
     * @param username 用户名
     * @param orgId    机构 ID
     * @return 返回用户对象
     */
    User findUserByUsernameAndOrgId(String username, long orgId);

    /**
     * 查找机构的用户
     *
     * @param orgId  机构 ID
     * @param offset 起始位置
     * @param count  数量
     * @return 返回用户列表
     */
    List<User> findUsersByOrgId(long orgId, int offset, int count);

    /**
     * 插入或更新用户: 如果 ID 存在或者 school_id + username 的组合存在则进行更新
     *
     * @param user 用户
     */
    void upsertUser(User user);

    /**
     * 创建用户的登录记录
     *
     * @param userId   用户 ID
     * @param username 用户账号
     */
    void insertUserLoginRecord(long userId, String username);

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

    /**
     * 插入用户的角色
     *
     * @param userId 用户的 ID
     * @param role   用户的角色
     */
    void insertUserRole(long userId, Role role);

}
