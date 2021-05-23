package com.xtuer.util;

import com.alibaba.fastjson.JSON;
import com.xtuer.bean.Mime;
import com.xtuer.bean.Result;
import com.xtuer.bean.SecurityConst;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * Web 操作相关的辅助工具，例如:
 *     获取客户端 IP
 *     读写删除 Cookie
 *     读取文件到 HttpServletResponse
 *     向 HttpServletResponse 写入 Ajax 响应
 *     判断请求是否使用 Ajax，获取 URI 的文件名
 * 提示:
 *     1. HttpServletRequest.getRequestURI() 返回的 URI 不带有参数
 */
@Slf4j
public final class WebUtils {
    public static final String UNKNOWN = "unknown";

    /**
     * 判断请求是否 AJAX 请求
     *
     * @param request HttpServletRequest 对象
     * @return 如果是 AJAX 请求则返回 true，否则返回 false
     */
    public static boolean useAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    /**
     * 使用 AJAX 的方式把响应写入 response 中，编码使用 UTF-8，HTTP 状态码为 200
     *
     * @param response HttpServletResponse 对象，用于写入请求的响应
     * @param result   响应的数据
     */
    public static void ajaxResponse(HttpServletResponse response, Result<?> result) {
        ajaxResponse(response, result, 200);
    }

    /**
     * 使用 AJAX 的方式把响应写入 response 中，编码使用 UTF-8，HTTP 状态码为 200
     *
     * @param response HttpServletResponse 对象，用于写入请求的响应
     * @param result   响应的数据
     * @param statusCode HTTP 状态码
     */
    public static void ajaxResponse(HttpServletResponse response, Result<?> result, int statusCode) {
        ajaxResponse(response, JSON.toJSONString(result), statusCode);
    }

    /**
     * 使用 AJAX 的方式把响应写入 response 中，编码使用 UTF-8
     *
     * @param response   HttpServletResponse 对象，用于写入请求的响应
     * @param data       响应的数据
     * @param statusCode HTTP 状态码
     */
    public static void ajaxResponse(HttpServletResponse response, String data, int statusCode) {
        response.setContentType("application/json"); // 使用 ajax 的方式
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        try {
            // 写入数据到流里，刷新并关闭流
            PrintWriter writer = response.getWriter();
            writer.write(data);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
            log.warn(ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * 获取当前线程的 request
     *
     * @return 返回 request
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获取 path 位于 WEB-INF 目录下的绝对路径，常用于读取静态资源，配置文件等。
     * 例如 path 为 static/books，则返回的路径为 ${project-path}/WEB-INF/static/books
     *
     * @param path 关于 WEB-INF 的相对路径
     * @return 返回 path 的绝对路径
     */
    public static String getPathInWebInf(String path) {
        return WebUtils.getRequest().getServletContext().getRealPath("/WEB-INF/" + path);
    }

    /**
     * 获取名字为 name 的 cookie 的值
     *
     * @param request HttpServletRequest 对象
     * @param name    Cookie 的名字
     * @return 返回名字为 name 的 cookie 的值，如果 name 不存在则返回 null
     */
    public static String getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 把 name/value 写入 cookie
     *
     * @param response 请求响应的 HttpServletResponse
     * @param name     cookie 的 name
     * @param value    cookie 的 value
     * @param maxAge   cookie 的过期时间，单位为秒，为 0 时删除 cookie
     */
    public static void writeCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");

        response.addCookie(cookie);
    }

    /**
     * 删除 cookie
     *
     * @param response 请求响应的 HttpServletResponse
     * @param name     cookie 的 name
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        writeCookie(response, name, null, 0);
    }

    /**
     * 从 header 或者 cookie 里获取 auth-token
     *
     * @param request HttpServletRequest 对象
     * @return 返回登录生成的 token
     */
    public static String getAuthToken(HttpServletRequest request) {
        String token = request.getHeader(SecurityConst.AUTH_TOKEN_KEY);

        if (token == null) {
            token = WebUtils.getCookie(request, SecurityConst.AUTH_TOKEN_KEY);
        }

        return token;
    }

    /**
     * 获取客户端的 IP
     *
     * @param request HttpServletRequest 对象
     * @return 客户端的 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        // 有多个 Proxy 的情况: X-Forwarded-For: client, proxy1, proxy2 是一串 IP，第一个 IP 是客户端的 IP
        // 只有 1 个 Proxy 时取到的就是客户端的 IP
        if (!(ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip))) {
            String[] ips = ip.split(",");
            return ips[0];
        }

        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr(); // 没有使用 Proxy 时是客户端的 IP, 使用 Proxy 时是最近的 Proxy 的 IP
        }

        return ip;
    }

    /**
     * 获取服务器的本地局域网 IP
     *
     * @return 返回 IP
     */
    public static String getLocalIp() {
        String returnIp = ""; // 有的服务器没有设置 192 的局域网 IP，例如阿里云的云主机

        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

            while (e.hasMoreElements()) {
                NetworkInterface n = e.nextElement();
                Enumeration<InetAddress> addresses = n.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress i = addresses.nextElement();
                    String currentAddress = i.getHostAddress();

                    if (currentAddress.startsWith("192.")) {
                        return currentAddress; // 优先返回 192 开头的局域网 IP
                    } else if (!i.isLoopbackAddress() && currentAddress.matches("(\\d+\\.){3}(\\d+)")) {
                        returnIp = currentAddress;
                    }
                }
            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
        }

        return returnIp;
    }

    /**
     * 获取当前请求的 Host
     *
     * @return 请求的 host
     */
    public static String getHost() {
        try {
            String host = new URL(WebUtils.getRequest().getRequestURL().toString()).getHost();
            return WebUtils.simplifyHost(host);
        } catch (MalformedURLException e) {
            log.warn(ExceptionUtils.getStackTrace(e));
        }

        return null;
    }

    /**
     * 根据文件名向 response 中写入对应的 content type，如无对应的 content type 则使用 application/octet-stream 表示文件要进行下载。
     * 注意: 访问图片的时候只有设置正确的 content type 浏览器才能正确的显示图片，否则有的时候会把图片作为普通文件进行下载。
     * 文件的 content type 请参考 http://tool.oschina.net/commons
     *
     * @param filename 文件名
     * @param response HttpServletResponse 对象
     */
    public static void setContentType(String filename, HttpServletResponse response) {
        String contentType = Mime.getContentType(filename);
        response.setContentType(contentType);
    }

    /**
     * 获取请求的 URI (没有参数, 域名等信息)
     *
     * @param request HttpServletRequest 对象
     * @return 返回请求的 URI
     */
    @SneakyThrows
    public static String getUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        uri = URLDecoder.decode(uri, StandardCharsets.UTF_8.name()); // 解码 URI 中的特殊字符，如中文字符

        return uri;
    }

    /**
     * 获取请求中的文件名
     * 例如 http://localhost:8080/preview/file/temp/220059763684147200.doc?size=100 得到文件名 220059763684147200.doc
     *
     * @param request HttpServletRequest 对象
     * @return 返回请求的文件名
     */
    public static String getUriFilename(HttpServletRequest request) {
        String uri = WebUtils.getUri(request);
        String filename = FilenameUtils.getName(uri);

        return filename;
    }

    /**
     * 读取文件到 HttpServletResponse
     *
     * @param file     文件
     * @param response HttpServletResponse 对象
     */
    public static void readFileToResponse(File file, HttpServletResponse response) throws IOException {
        readFileToResponse(file.getAbsolutePath(), file.getName(), WebUtils.getRequest(), response);
    }

    /**
     * 读取文件到 HttpServletResponse
     *
     * @param path     // 文件的路径
     * @param filename // 文件名，因为文件的路径中的文件名是编码过的，而真实的文件名保存在数据库，所以 path 中的文件名很可能不是真正的文件名
     * @param request  // HttpServletRequest 对象
     * @param response // HttpServletResponse 对象
     * @throws IOException 访问文件发生异常时抛出
     */
    public static void readFileToResponse(String path, String filename, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseUtils.readFileToResponse(path, filename, request, response);

        // // 1. 如果文件不存在则返回 404 页面
        // // 2. 文件存在，写入文件名
        // // 3. 根据文件名写入对应的 Content-Type
        // // 4. 设置文件的大小，浏览器就能够知道下载进度了
        // // 5. 读取文件到 response 的输出流中
        //
        // if (!Files.exists(Paths.get(path))) {
        //     // [1] 如果文件不存在则返回 404 页面
        //     log.warn("文件 {} 不存在", path);
        //     response.sendError(HttpServletResponse.SC_NOT_FOUND);
        //     return;
        // }
        //
        // File file = new File(path);
        // log.debug("访问文件 {}", path);
        //
        // WebUtils.setResponseFilename(filename, response);  // [2] 写入文件名
        // WebUtils.setContentType(file.getName(), response); // [3] 设置 content type，让浏览器能正确的知道文件的处理方式
        // response.setContentLengthLong(file.length());      // [4] 设置文件的大小，浏览器就能够知道下载进度了
        //
        // // [5] 读取文件到 response 的输出流中 (使用 try 自动关闭流)
        // try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
        //     IOUtils.copy(in, out);
        // }
    }


    /**
     * 向 response 中写入文件名
     *
     * @param filename 文件名
     * @param response HttpServletResponse 对象
     */
    public static void setResponseFilename(String filename, HttpServletResponse response) {
        // 正确显示文件名: inline 和 attachment 要分别处理:
        // 1. 如果文件可以在浏览器中直接显示则把文件名写入 inline 的 Content-Disposition
        // 2. 如果文件不可以在浏览器中显示则把文件名写入 attachment 的 Content-Disposition 用于下载

        if (StringUtils.isNotBlank(filename)) {
            filename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1); // 解决乱码问题

            if (Utils.isInlineFileForBrowser(filename)) {
                // 浏览器内嵌显示的文件，例如图片，普通文本文件
                response.setHeader("Content-Disposition", "inline;filename=" + filename);
            } else {
                // 浏览器中弹出下载对话框进行下载的文件，例如 rar, zip
                response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            }
        }
    }

    /**
     * 把域名前后的空白字符去掉，去掉域名前的 www. :
     * google.com     返回 google.com
     * www.google.com 返回 google.com
     *
     * @param host 域名
     * @return 返回简化后的域名
     */
    public static String simplifyHost(String host) {
        return RegExUtils.removePattern(StringUtils.trim(host), "^www\\.");
    }
}
