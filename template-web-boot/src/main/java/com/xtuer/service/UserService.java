package com.xtuer.service;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.Cached;
import com.xtuer.bean.*;
import com.xtuer.mapper.UserMapper;
import com.xtuer.security.JwtService;
import com.xtuer.util.Utils;
import com.xtuer.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 提供用户相关的服务，例如查询用户、创建用户、更新用户信息。
 *
 * 注意: 根据更新的用户信息，决定是否需要从 Redis 中删除缓存的用户。
 */
@Service
public class UserService extends BaseService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtService jwtService;

    /**
     * 查找机构 orgId 下指定账号的用户
     *
     * @param username 账号
     * @param orgId    机构 ID
     * @return 返回查询到的用户，如果无匹配的用户则返回 null
     */
    public User findUser(String username, long orgId) {
        // 系统管理员的机构 ID 定义为 1，其他的都是 64 位整数，而且系统中只有系统管理员的机构 ID 为 1
        return userMapper.findUserByUsernameAndOrgId(username, orgId);
    }

    /**
     * 查找机构 orgId 下账号和密码匹配的用户
     *
     * @param username 账号
     * @param password 密码
     * @param orgId    机构 ID
     * @return 返回查询到的用户，如果无匹配的用户则返回 null
     */
    public User findUser(String username, String password, long orgId) {
        // 由于使用了 BCrypt 进行加密，所以不能简单的使用密码去数据库进行搜索
        User user = userMapper.findUserByUsernameAndOrgId(username, orgId);

        if (user != null && Utils.isPasswordValidByBCrypt(password, user.getPassword())) {
            return user;
        }

        return null;
    }

    /**
     * 查找用户
     * 先从缓存里查找用户，如果缓存里没有，再从数据库加载
     *
     * @param userId 用户 ID
     * @return 返回查找到的用户，查找不到则返回 null
     */
    @Cached(name = CacheConst.CACHE, key = CacheConst.KEY_USER_ID)
    public User findUser(long userId) {
        return userMapper.findUserById(userId);
    }

    /**
     * 检查用户名在机构中是否使用过
     *
     * @param username 用户名
     * @param orgId    机构 ID
     * @return 用户名使用过返回 true，没有使用过返回 false
     */
    public boolean isUsernameUsed(String username, long orgId) {
        User user = userMapper.findUserByUsernameAndOrgId(username, orgId);
        return user != null;
    }

    /**
     * 创建或者更新用户，如果用户是新用户则会更新用户的 ID
     *
     * @param user 用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateUser(User user) {
        // 1. 如果用户 ID 无效，为其分配一个 ID，加密密码
        // 2. 保存用户到数据库
        // 3. 更新用户 ID: 如果 orgId + username 已经存在，则是更新已存在用户，userId 设置为数据库中对应用户的 ID，而不使用前面设置的
        //    重复导入用户数据的时候可能会用到
        // 4. 添加用户的角色 (如果已经存在则会自动忽略)

        long userId = user.getUserId();

        if (Utils.isInvalidId(userId)) {
            userId = nextId();
            user.setUserId(userId);
        }

        user.setPassword(Utils.passwordByBCrypt(user.getPassword())); // 加密密码

        // [2] 保存用户到数据库
        userMapper.upsertUser(user);

        // [3] 更新用户 ID: 如果 orgId + username 已经存在，则是更新已存在用户，userId 设置为数据库中对应用户的 ID，而不使用前面设置的
        userId = userMapper.findUserByUsernameAndOrgId(user.getUsername(), user.getOrgId()).getUserId();
        user.setUserId(userId);

        // [4] 添加用户的角色 (如果已经存在则会自动忽略)
        for (Role role : user.getRoles()) {
            userMapper.insertUserRole(user.getUserId(), role);
        }
    }

    /**
     * 用户登录后，创建 token
     *
     * @param user     用户对象
     * @param response 响应对象
     * @return 返回 token
     */
    public String loginToken(User user, HttpServletResponse response) {
        // 1. 创建用户的登录记录
        userMapper.insertUserLoginRecord(user.getUserId(), user.getUsername());

        // 2. 生成用户的 token，并保存 token 到 cookie (方便浏览器端使用 Ajax 登录)
        String token = jwtService.generateToken(user);
        WebUtils.writeCookie(response, SecurityConst.AUTH_TOKEN_KEY, token, config.getAuthTokenDuration());

        return token;
    }

    /**
     * 更新用户的手机号
     *
     * @param userId 用户的 ID
     * @param mobile 用户的手机号
     */
    @CacheInvalidate(name = CacheConst.CACHE, key = CacheConst.KEY_USER_ID)
    public Result<String> updateUserMobile(long userId, String mobile) {
        mobile = StringUtils.trim(mobile);

        if (StringUtils.isNumeric(mobile) && mobile.length() == 11) {
            // 简单的校验：手机号为 11 个数字
            userMapper.updateUserMobile(userId, mobile);
            return Result.ok("手机更新成功");
        } else {
            return Result.fail("请输入正确的手机号");
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
            return Result.fail("密码不能为空");
        }

        // [2] newPassword 和 renewPassword 不相同或者为空则返回
        if (StringUtils.isBlank(newPassword) || !newPassword.equals(renewPassword)) {
            return Result.fail("新密码 2 次输入不匹配");
        }

        // [3] 查询用户得到用户密码 password
        // [4] 比较 password 和 oldPassword，不相同则返回
        User user = userMapper.findUserById(userId);
        if (!Utils.isPasswordValidByBCrypt(oldPassword, user.getPassword())) {
            return Result.fail("旧密码不正确");
        }

        // [5] 密码最小长度为 5，最大长度为 50
        if (newPassword.length() < 5 || newPassword.length() > 50) {
            return Result.fail("密码的长度为 5 到 50 位");
        }

        // [6] 验证都通过了，更新用户密码
        userMapper.updateUserPassword(userId, Utils.passwordByBCrypt(newPassword));

        return Result.ok(null, "密码更新成功");
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

    /**
     * 更新用户的昵称
     *
     * @param userId   用户的 ID
     * @param nickname 用户的昵称
     */
    @CacheInvalidate(name = CacheConst.CACHE, key = CacheConst.KEY_USER_ID)
    public void updateUserNickname(long userId, String nickname) {
        userMapper.updateUserNickname(userId, nickname);
    }

    /**
     * 更新用户的头像
     *
     * @param userId 用户 ID
     * @param avatar 用户头像
     */
    @CacheInvalidate(name = CacheConst.CACHE, key = CacheConst.KEY_USER_ID)
    public String updateUserAvatar(long userId, String avatar) {
        // 1. 移动 avatar 的图片到 repo 并得到 avatar 的最新 URL
        // 2. 更新数据库中用户的 avatar
        // 3. 删除缓存用户
        // 4. 返回 avatar 的 url

        avatar = repoFileService.moveTempFileToRepo(avatar);

        if (avatar != null) {
            userMapper.updateUserAvatar(userId, avatar);
        }

        return avatar;
    }

    /**
     * 更新用户的性别
     *
     * @param userId 用户的 ID
     * @param gender 用户的性别
     */
    @CacheInvalidate(name = CacheConst.CACHE, key = CacheConst.KEY_USER_ID)
    public void updateUserGender(long userId, int gender) {
        userMapper.updateUserGender(userId, gender);
    }

    /**
     * 根据用户的角色重定向到用户对应的后台页面，管理员重定向到管理员页面，学员重定向到学习中心
     *
     * @param user     用户
     * @param response HttpServletResponse
     */
    public void redirectToUserBackendPage(User user, HttpServletResponse response) throws IOException {
        if (user.hasRole(Role.ROLE_ADMIN_SYSTEM)) {
            response.sendRedirect("/admin");
        } else if (user.hasRole(Role.ROLE_ADMIN_ORG)) {
            response.sendRedirect("/admin-org");
        } else if (user.hasRole(Role.ROLE_USER)) {
            response.sendRedirect("/user");
        } else {
            response.sendRedirect("/");
        }
    }
}
