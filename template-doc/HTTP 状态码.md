使用此框架，HTTP 的状态码主要为:

* 200: 正常访问:
  * AJAX 时返回 JSON 格式数据
* 401: Token 无效
* 403: 权限不够
* 404: URL 找不到
* 500: 服务器抛出异常
  * AJAX 时异常信息被包装在 JSON 格式数据中
  * 页面访问到统一的错误页面显示错误信息，或者访问此异常指定的页面

移动端访问时，最好是都模拟 AJAX 请求，即设置请求头 `X-Requested-With` 为 `XMLHttpRequest`，这样异常被包装在 JSON 的响应中，且状态码为 500，容易拦截处理。

常用的 HTTP 状态码介绍可参考 <http://www.cnblogs.com/starof/p/5035119.html>。

