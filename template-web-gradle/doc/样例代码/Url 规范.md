URI 规范和变量名 :

1. 返回数据的 URL 要使用 **RESTful** 风格，最直接的好处是 1 个 URL 可以有 4 种功能，方便管理，还符合流行技术

   > 可阅读 [RESTful API 设计最佳实践](https://segmentfault.com/a/1190000011516151)

2. 页面 URI 的变量名以 `PAGE_` 开头，此 URI 以 `/page` 开头，看到 URL 就知道是什么用途了

3. 页面对应模版文件的变量名以 `FILE_` 开头，表明这个 URI 是文件的路径，即模版的路径

4. 普通 FORM 表单处理 URI 的变量名以 `FORM_` 开头，此 URI 以 `/form` 开头

5. 操作资源的 API 变量名以 `API_` 开头，此 URI 以 `/api` 开头，使用 RESTful 风格

```java
String PAGE_LOGIN       = "/page/login";  // 登陆
String FORM_DEMO_UPLOAD = "/form/demo/upload";
String API_SUBJECTS        = "/api/subjects";
String API_SUBJECTS_BY_ID  = "/api/subjects/{subjectId}";
```

更多请参考 **Urls.java**。