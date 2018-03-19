package ebag.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public final class NetUtils {
    private static Logger logger = LoggerFactory.getLogger(NetUtils.class);

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
     * @param data     响应的数据
     */
    public static void ajaxResponse(HttpServletResponse response, String data) {
        ajaxResponse(response, data, 200);
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
            logger.warn(ExceptionUtils.getStackTrace(ex));
        }
    }

    /**
     * 获取名字为 name 的 cookie 的值
     *
     * @param request
     * @param name Cookie 的名字
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
     * @return 返回创建的 cookie
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
     * 获取客户端的 IP
     *
     * @param  request Http 请求对象
     * @return 客户端的 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        final String UNKNOWN = "unknown";
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
}
