package ebag.controller;

import ebag.bean.Result;
import ebag.bean.User;
import ebag.security.TokenService;
import ebag.service.UserService;
import ebag.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class AuthenticationController {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    /**
     * 处理登录，登录错误，注销，对应的 URL 为:
     *     登录页面: /login
     *     登录错误: /login?error=1
     *     注销成功: /login?logout=1
     *
     * @param error  不为 null 表示登陆出错
     * @param logout 不为 null 表示注销成功
     * @param model  保存数据到 view 中显示
     */
    @GetMapping(value= Urls.PAGE_LOGIN)
    public String loginPage(@RequestParam(value="error",  required=false) String error,
                            @RequestParam(value="logout", required=false) String logout,
                            ModelMap model) {
        String status = "";
        status = (error != null)  ? "Username or password is not correct" : status; // 登录错误
        status = (logout != null) ? "Logout successful" : status; // 注销成功
        model.put("status", status);

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
     * 使用用户名和密码请求 token.
     * URL: http://localhost:8080/api/login/tokens
     *
     * @param username 用户名
     * @param password 密码
     */
    @PostMapping(Urls.API_LOGIN_TOKENS)
    @ResponseBody
    public Result<String> loginToken(@RequestParam String username, @RequestParam String password) {
        User user = userService.findUserByUsernamePassword(username, password);

        if (user == null) {
            return Result.fail("用户名或密码不正确");
        }

        String token = tokenService.generateToken(user);
        return Result.ok("success", token);
    }

    /**
     * 用已有的 token 交换一个新的 token.
     * URL: http://localhost:8080/api/login/tokens
     *
     * @param token JWT token
     */
    @PutMapping(Urls.API_LOGIN_TOKENS)
    @ResponseBody
    public Result<String> refreshToken(@RequestParam String token) {
        if (!tokenService.checkToken(token)) {
            return Result.fail("Token is invalid", "");
        }

        User user = tokenService.extractUser(token);
        token = tokenService.generateToken(user);
        return Result.ok("success", token);
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
