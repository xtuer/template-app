package com.edu.training.controller;

import com.edu.training.bean.Result;
import com.edu.training.bean.User;
import com.edu.training.security.TokenService;
import com.edu.training.service.UserService;
import com.edu.training.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthenticationController extends BaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @GetMapping("/")
    public String index() {
        return Urls.FORWARD + Urls.PAGE_LOGIN;
    }

    /**
     * 登录页面，登录错误，注销页面，对应的 URL 为:
     *     登录页面: /page/login
     *     登录错误: /page/login?error=1
     *     注销成功: /page/login?logout=1
     *
     * 网址: http://localhost:8080/page/login
     *
     * @param error   不为 null 表示登陆出错
     * @param logout  不为 null 表示注销成功
     * @param request HttpServletRequest 对象
     * @param model   保存数据到 view 中显示
     */
    @GetMapping(value= Urls.PAGE_LOGIN)
    public String loginPage(@RequestParam(value="error",  required=false) String error,
                            @RequestParam(value="logout", required=false) String logout,
                            HttpServletRequest request,
                            ModelMap model) {
        // 判断当前登录的状态
        String status = "";
        status = (error  != null) ? "账号或密码无效" : status; // 登录错误
        status = (logout != null) ? "" : status; // 注销成功
        model.put("status", status);

        // 请求当前登录用户的头像。
        // 因为 login 页面不需要权限访问，所以被 spring security 拦截，在 SecurityContextHolder 中没有用户的登录信息，
        // 所以从用户的 cookie 中获取用户 ID，然后从数据库中查询用户信息
        User user = tokenService.extractUser(WebUtils.getAuthToken(request));

        // 得到登录的用户信息后，使用用户的 ID 从数据库查询用户的完整信息
        if (user != null) {
            user = userService.findUser(user.getId());

            if (user != null) {
                model.put("avatar", user.getAvatar());
            }
        }

        return Urls.FILE_LOGIN;
    }

    /**
     * 权限不够时访问 Spring Security forward request 到此方法.
     */
    @GetMapping(Urls.PAGE_DENY)
    @ResponseBody
    public String toDenyPage(HttpServletRequest request) {
        // Ajax 访问时权限不够抛异常，我们提供的异常处理器会转换为 JSON 格式返回.
        if (WebUtils.useAjax(request)) {
            throw new RuntimeException("权限不够");
        }

        // 普通访问返回错误信息或者相关页面
        return "权限不够!";
    }

    /**
     * 请求当前登录用户
     * 网址: http://localhost:8080/api/login/users/current
     *
     * @return payload 为登录用户，如没有登录则 payload 为 null，success 为 false
     */
    @GetMapping(Urls.API_LOGIN_USERS_CURRENT)
    @ResponseBody
    public Result<User> getCurrentUser() {
        User user = super.getLoginUser();

        if (user != null) {
            user = userService.findUser(user.getId()); // 从数据库里查询用户信息
            return Result.ok(user);
        } else {
            return Result.failMessage("还没有登录");
        }
    }


    /**
     * 使用用户名和密码请求 token.
     * 网址: http://localhost:8080/api/login/tokens
     * 参数: username and password
     *
     * @param username 账号
     * @param password 密码
     */
    @PostMapping(Urls.API_LOGIN_TOKENS)
    @ResponseBody
    public Result<String> loginToken(@RequestParam String username, @RequestParam String password) {
        User user = userService.findUser(username, password, super.getCurrentOrganizationId());

        if (user == null) {
            return Result.fail("用户名或密码不正确", "");
        }

        userService.createLoginRecord(user.getId(), user.getUsername()); // 创建用户的登录记录
        String token = tokenService.generateToken(user);

        return Result.ok(token);
    }

    /**
     * 绑定用户
     */
    @GetMapping("/page/bindUser")
    @ResponseBody
    public String bindUser(HttpServletRequest request, HttpServletResponse response) {
        // 1. 绑定用户，用户不存在则先创建
        // TODO

        // 2. 绑定用户成功后使用 savedRequest 重定向到登陆前的页面，这里只是为了展示怎么取到登陆前页面的 URL
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        String redirectUrl = (savedRequest != null) ? savedRequest.getRedirectUrl() : "/";

        return redirectUrl;
    }
}
