package com.xtuer.controller;

import com.xtuer.bean.Organization;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.bean.User;
import com.xtuer.config.AppConfig;
import com.xtuer.security.JwtService;
import com.xtuer.service.UserService;
import com.xtuer.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class AuthenticationController extends BaseController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AppConfig config;

    /**
     * 访问首页，可以根据用户的角色、浏览器类型跳转到不同的页面
     */
    @GetMapping("/")
    public String index() {
        return Urls.FORWARD + Urls.PAGE_LOGIN;
    }

    /**
     * 登录页面，登录错误，注销页面，对应的 URL 为:
     *     登录页面: /login
     *     登录错误: /login?error=1
     *     注销成功: /login?logout=1
     *
     * 网址: http://localhost:8080/login
     * 参数: error, logout
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
        status = (logout != null) ? "" : status;             // 注销成功
        model.put("status", status);

        // 请求当前登录用户的头像。
        // 因为 login 页面不需要权限访问，所以被 spring security 拦截，在 SecurityContextHolder 中没有用户的登录信息，
        // 所以从用户的 cookie 中获取用户 ID，然后从数据库中查询用户信息
        User user = jwtService.extractUser(WebUtils.getAuthToken(request));

        // 得到登录的用户信息后，使用用户的 ID 从数据库查询用户的完整信息
        if (user != null) {
            user = userService.findUser(user.getUserId());

            if (user != null) {
                model.put("avatar", user.getAvatar());
            }
        }

        // 当前机构
        Organization org = super.orgService.getCurrentOrganization();
        model.put("org", org);

        return Urls.FILE_LOGIN;
    }

    /**
     * 请求当前登录用户
     *
     * 网址: http://localhost:8080/api/login/users/current
     * 参数: 无
     *
     * @return payload 为登录用户，如没有登录则 payload 为 null，success 为 false
     */
    @GetMapping(Urls.API_LOGIN_USERS_CURRENT)
    @ResponseBody
    public Result<User> getCurrentLoginUser() {
        User user = super.getCurrentUser();

        if (user != null) {
            user = userService.findUser(user.getUserId()); // 从数据库里查询用户信息
            return Result.ok(user);
        } else {
            return Result.fail("还没有登录");
        }
    }


    /**
     * 使用用户名和密码请求 token.
     *
     * 网址: http://localhost:8080/api/login/tokens
     * 参数: username and password
     *
     * @param username 账号
     * @param password 密码
     */
    @PostMapping(Urls.API_LOGIN_TOKENS)
    @ResponseBody
    public Result<String> loginToken(@RequestParam String username, @RequestParam String password, HttpServletResponse response) {
        User user = userService.findUser(username, password, super.getCurrentOrganizationId());

        if (user == null) {
            return Result.fail("用户名或密码不正确");
        }

        // 生成用户的 token，并保存 token 到 cookie (方便浏览器端使用 Ajax 登录)
        String token = userService.loginToken(user, response);

        return Result.ok(token);
    }

    /**
     * 访问当前登录用户的后台页面地址
     *
     * 网址: http://localhost:8080/userBackend
     * 参数: 无
     */
    @GetMapping(Urls.PAGE_USER_BACKEND)
    public void toUserBackendPage(HttpServletResponse response) throws IOException {
        userService.redirectToUserBackendPage(super.getCurrentUser(), response);
    }
}
