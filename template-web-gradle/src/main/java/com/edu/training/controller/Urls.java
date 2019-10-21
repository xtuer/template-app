package com.edu.training.controller;

/**
 * 集中管理 URL.
 *
 * 其实此类名叫 Urls 不是很合适，基本都是 URI，但是对于大多数人来说 URL 更熟悉好记忆一些。
 * 还有少量变量不是 URI，例如 JSONP_CONTENT_TYPE，FORWARD 等，但不多，为了减少类，故就放在这里吧，约定好了就行。
 *
 * 变量名和 URI 规则:
 * 1. 页面 URI 的变量名以 PAGE_ 开头，此 URI 以 /page 开头，看到 URL 就知道是什么用途了
 * 2. 页面对应模版文件的变量名以 FILE_ 开头，表明文件的路径，即模版的路径
 * 3. 普通 FORM 表单处理 URI 的变量名以 FORM_ 开头，此 URI 以 /form 开头
 * 4. 操作资源的 api 变量名以 API_ 开头，此 URI 以 /api 开头，使用 RESTful 风格，资源名使用复数
 */
public interface Urls {
    String JSONP_CONTENT_TYPE = "application/javascript;charset=UTF-8"; // JSONP 响应的 header

    // 通用
    String FORWARD    = "forward:";
    String REDIRECT   = "redirect:";
    String PAGE_404   = "/404";
    String FILE_ERROR = "error.html";

    // 案例展示
    String PAGE_DEMO_REST   = "/page/demo/rest";
    String FILE_DEMO_REST   = "demo/rest.html";
    String FORM_DEMO_UPLOAD = "/form/demo/upload";
    String API_DEMO_MYBATIS = "/api/demo/mybatis/{id}";

    String PAGE_DOWNLOAD  = "/page/download"; // 下载
    String FILE_DOWNLOAD  = "download.html";  // 下载

    // 登录注销
    String PAGE_LOGIN = "/page/login"; // 登陆
    String PAGE_DENY  = "/page/deny";  // 无权访问页面的 URL
    String FILE_LOGIN = "login.html";  // 登陆页面
    String API_LOGIN_TOKENS          = "/api/login/tokens";        // 登陆用户的 token
    String API_LOGIN_USERS_CURRENT   = "/api/login/users/current"; // 当前登录的用户
    String PAGE_USER_BACKEND         = "/page/userBackend";        // 访问当前登录用户的后台页面地址

    // 用户
    String API_USERS_BY_ID          = "/api/users/{userId}";                 // 指定 ID 的用户
    String API_USER_PASSWORDS_RESET = "/api/users/{userId}/passwords/reset"; // 重置密码

    String API_SERVER_CURRENT_TIME = "/api/serverCurrentTime"; // 服务器当前时间

    // API 使用 RESTful 风格，变量名以 API_ 开头，URI 以 /api 开头, 资源都用复数形式便于统一管理 URL。
    // 下面以操作 subject, qa 资源的 RESTful 风格的 URL 为例:
    // 列出 qa 有 2 个相关的 URL，一是列出所有的 questions 用 API_QUESTIONS，
    // 另一个是列出主题下的所有 questions 用 API_QUESTIONS_IN_SUBJECT。
    String API_SUBJECTS        = "/api/subjects";
    String API_SUBJECTS_BY_ID  = "/api/subjects/{subjectId}";

    // 上传文件、图片到临时目录，仓库中的文件为正式的文件
    String FORM_UPLOAD_TEMP_FILE  = "/form/upload/temp/file";           // 上传一个临时文件
    String FORM_UPLOAD_TEMP_FILES = "/form/upload/temp/files";          // 上传多个临时文件
    String URL_TEMP_FILE_PREFIX   = "/file/temp/";                      // 临时文件的 URL 前缀
    String URL_TEMP_FILE          = "/file/temp/{filename}";            // 临时文件的 URL
    String URL_REPO_FILE_PREFIX   = "/file/repo/";                      // 仓库文件的 URL 前缀
    String URL_REPO_FILE          = "/file/repo/{date}/{filename}";     // 仓库文件的 URL，按日期保存
    String URL_REPO_FILE_DOWNLOAD = "/file/download/{date}/{filename}"; // 下载仓库文件的 URL，按日期保存

    // 机构
    String API_ORGS        = "/api/orgs";                 // 所有机构
    String API_ORGS_BY_ID  = "/api/orgs/{orgId}";         // 指定 ID 的机构
    String API_ORGS_ENABLE = "/api/orgs/{orgId}/enabled"; // 指定 ID 的机构的启用禁用状态

}
