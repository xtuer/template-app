package edu.service;

import edu.bean.Result;
import edu.bean.User;
import edu.mapper.UserMapper;
import edu.util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提供用户相关的服务，例如查询用户，创建用户
 */
@Service
public class UserService {
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private UserMapper userMapper;

    /**
     * 查找学校 schoolId 下匹配账号的用户
     *
     * @param username 账号
     * @param schoolId 学校 ID
     * @return 返回查询到的用户，如果无匹配的用户则返回 null
     */
    public User findUser(String username, long schoolId) {
        // 系统管理员的学校 ID 定义为 0，其他的都是 64 位整数，而且系统中只有系统管理员的学校 ID 为 0
        return userMapper.findUserByUsernameAndSchoolId(username, schoolId);
    }

    /**
     * 查找学校 schoolId 下匹配账号和密码的用户
     *
     * @param username 账号
     * @param password 密码
     * @param schoolId 学校 ID
     * @return 返回查询到的用户，如果无匹配的用户则返回 null
     */
    public User findUser(String username, String password, long schoolId) {
        User user = userMapper.findUserByUsernameAndSchoolId(username, schoolId);

        if (user != null && Utils.isPasswordValidByBCrypt(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    /**
     * 查找用户，隐藏密码
     *
     * @param userId 用户 ID
     * @return 返回查找到的用户，查找不到则返回 null
     */
    public User findUser(long userId) {
        return findUser(userId, false);
    }

    /**
     * 查找用户，如果 withPassword 为 true 则携带密码，为 false 则隐藏密码
     *
     * @param userId       用户 ID
     * @param withPassword 是否携带密码
     * @return 返回查找到的用户，查找不到则返回 null
     */
    public User findUser(long userId, boolean withPassword) {
        User user = userMapper.findUserById(userId);

        if (user == null) {
            return null;
        }

        if (!withPassword) {
            user.setPassword("[protected]");
        }

        return user;
    }

    /**
     * 检查用户名在学校中是否使用过
     *
     * @param username 用户名
     * @param schoolId 学校 ID
     * @return 用户名使用过返回 true，没有使用过返回 false
     */
    public boolean isUsernameUsedInSchool(String username, long schoolId) {
        User user = userMapper.findUserByUsernameAndSchoolId(username, schoolId);
        return user != null;
    }

    /**
     * 创建或者更新用户
     *
     * @param user 用户
     * @return 返回用户的 ID
     */
    @Transactional(rollbackFor = Exception.class)
    public long createOrUpdateUser(User user) {
        // 1. 如果用户 ID 为 null 或者 0，为其分配一个 ID
        // 2. 保存用户到数据库
        // 3. 保存用户的角色到数据库

        Long userId = user.getId();

        if (userId == null || userId == 0) {
            userId = idWorker.nextId();
            user.setId(userId);
        }

        userMapper.insertOrUpdateUser(user);

        // 查找已经存在的用户 ID
        userId = userMapper.findUserByUsernameAndSchoolId(user.getUsername(), user.getSchoolId()).getId();
        user.setId(userId);

        return userId;
    }

    /**
     * 更新用户的登录状态: 登录次数+1，记录最后的登录时间
     *
     * @param userId 用户 ID
     */
    public void updateUserLoginStatus(long userId) {
        userMapper.updateUserLoginStatus(userId);
    }

    /**
     * 更新用户的手机号
     * URL: http://localhost:8080/api/users/{userId}/mobiles
     * 参数: mobile: 手机号，类型为字符串
     *
     * @param userId 用户的 ID
     * @param mobile 用户的手机号
     */
    public Result<String> updateUserMobile(Long userId, String mobile) {
        mobile = StringUtils.trim(mobile);

        if (StringUtils.isNumeric(mobile) && mobile.length() == 11) {
            // 简单的校验：手机号为 11 个数字
            userMapper.updateUserMobile(userId, mobile);
            return Result.ok();
        } else {
            return Result.fail("请输入正确的手机号", "");
        }
    }

    /**
     * 更新用户的密码
     *
     * @param userId        用户的 ID
     * @param oldPassword   旧密码
     * @param newPassword   新密码
     * @param renewPassword 确认的新密码
     * @return 返回执行结果 Result
     */
    public Result<String> updateUserPassword(long userId, String oldPassword, String newPassword, String renewPassword) {
        // 1. 密码为空则返回
        // 2. newPassword 和 renewPassword 不相同或者为空则返回
        // 3. 查询用户得到用户密码 password
        // 4. 比较 password 和 oldPassword，不相同则返回
        // 5. 密码最小长度为 6，最大长度为 50
        // 6. 验证都通过了，更新用户密码

        // [1] 密码为空则返回
        if (StringUtils.isBlank(newPassword)) {
            return Result.fail("密码不能为空", "");
        }

        // [2] newPassword 和 renewPassword 不相同或者为空则返回
        if (StringUtils.isBlank(newPassword) || !newPassword.equals(renewPassword)) {
            return Result.fail("新密码 2 次输入不匹配", "");
        }

        // [3] 查询用户得到用户密码 password
        // [4] 比较 password 和 oldPassword，不相同则返回
        User user = userMapper.findUserById(userId);
        if (!Utils.isPasswordValidByBCrypt(oldPassword, user.getPassword())) {
            return Result.fail("旧密码不正确", "");
        }

        // [5] 密码最小长度为 6，最大长度为 50
        if (newPassword.length() < 6 || newPassword.length() > 50) {
            return Result.fail("密码的长度为 6 到 50 位", "");
        }

        // [6] 验证都通过了，更新用户密码
        userMapper.updateUserPassword(userId, Utils.passwordByBCrypt(newPassword));
        return Result.ok();
    }

    /**
     * 重置用户的密码为 123456
     *
     * @param userId 用户的 ID
     */
    public void resetUserPassword(long userId) {
        String password = Utils.passwordByBCrypt("123456"); // 使用 BCrypt 加密
        userMapper.updateUserPassword(userId, password);
    }
}
