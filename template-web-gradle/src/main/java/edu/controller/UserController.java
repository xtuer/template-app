package edu.controller;

import edu.bean.Result;
import edu.bean.User;
import edu.mapper.UserMapper;
import edu.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 操作用户信息的控制器
 */
@Controller
public class UserController extends BaseController {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    /**
     * 使用用户 ID 查询用户信息
     * URL: http://localhost:8080/api/users/{userId}
     *
     * @param userId 用户的 ID
     * @return 查询到时 payload 为用户对象, success 为 true，查询不到时 success 为 false, payload 为 null
     */
    @GetMapping(Urls.API_USERS_BY_ID)
    @ResponseBody
    public Result<User> findUserById(@PathVariable long userId) {
        User user = userService.findUser(userId);

        if (user != null) {
            user.protectPassword();
            return Result.ok(user);
        } else {
            return Result.fail("ID 为 " + userId + "的用户不存在");
        }
    }

    /**
     * 更新用户的昵称
     * 网址: http://localhost:8080/api/users/{userId}/nicknames
     * 参数: nickname: 新昵称
     *
     * @param userId   用户的 ID
     * @param nickname 用户的昵称
     */
    @PutMapping(Urls.API_USER_NICKNAMES)
    @ResponseBody
    public Result<String> updateUserNickname(@PathVariable long userId, @RequestParam String nickname) {
        nickname = StringUtils.trim(nickname);

        if (StringUtils.isBlank(nickname)) {
            return Result.fail("名字不能为空", "");
        }

        userMapper.updateUserNickname(userId, nickname);

        return Result.ok();
    }

    /**
     * 更新用户的头像
     * 网址: http://localhost:8080/api/users/{userId}/avatars
     * 参数: avatar: 头像的 URL，类型为字符串
     *
     * @param userId 用户的 ID
     * @param avatar 头像的 URL
     */
    @PutMapping(Urls.API_USER_AVATARS)
    @ResponseBody
    public Result<String> updateUserAvatar(@PathVariable long userId, @RequestParam String avatar) {
        // 1. 移动 avatar 的图片到 repo
        // 2. 更新数据库中用户的 avatar
        // 3. 返回 avatar 的 url

        avatar = getFileService().moveFileToRepo(avatar);
        userMapper.updateUserAvatar(userId, avatar);

        return Result.ok(avatar);
    }

    /**
     * 更新用户的性别
     * URL: http://localhost:8080/api/users/{userId}/genders
     * 参数: gender: 性别，类型为整数，0(未设置), 1(男), 2(女)
     *
     * @param userId 用户的 ID
     * @param gender 用户的性别
     */
    @PutMapping(Urls.API_USER_GENDERS)
    @ResponseBody
    public Result<String> updateUserGender(@PathVariable Long userId, @RequestParam int gender) {
        userMapper.updateUserGender(userId, gender);
        return Result.ok();
    }

    /**
     * 更新用户的手机号
     * URL: http://localhost:8080/api/users/{userId}/mobiles
     * 参数: mobile: 手机号，类型为字符串
     *
     * @param userId 用户的 ID
     * @param mobile 用户的手机号
     */
    @PutMapping(Urls.API_USER_MOBILES)
    @ResponseBody
    public Result<String> updateUserMobile(@PathVariable Long userId, @RequestParam String mobile) {
        return userService.updateUserMobile(userId, mobile);
    }

    /**
     * 更新用户的密码
     * URL: http://localhost:8080/api/users/{userId}/passwords
     * 参数:
     *     oldPassword: 密码，类型为字符串
     *     newPassword: 新密码，类型为字符串
     *     renewPassword: 确认的新密码，类型为字符串
     *
     *
     * @param userId        用户的 ID
     * @param oldPassword   旧密码
     * @param newPassword   新密码
     * @param renewPassword 确认的新密码
     * @return 返回执行结果 Result
     */
    @PutMapping(Urls.API_USER_PASSWORDS)
    @ResponseBody
    public Result<String> updateUserPassword(@PathVariable Long userId,
                                             @RequestParam String oldPassword,
                                             @RequestParam String newPassword,
                                             @RequestParam String renewPassword) {
        return userService.updateUserPassword(userId, oldPassword, newPassword, renewPassword);
    }

    /**
     * 重置用户的密码
     *
     * @param userId 用户的 ID
     */
    @PutMapping(Urls.API_USER_PASSWORDS_RESET)
    @ResponseBody
    public Result<String> resetUserPassword(@PathVariable Long userId) {
        userService.resetUserPassword(userId);
        return Result.ok();
    }
}
