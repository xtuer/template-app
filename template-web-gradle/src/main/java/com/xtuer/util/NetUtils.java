package com.xtuer.util;

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
     * 使用 AJAX 的方式把响应写入 response 中
     *
     * @param response HttpServletResponse 对象，用于写入请求的响应
     * @param data 响应的数据
     */
    public static void ajaxResponse(HttpServletResponse response, String data) {
        response.setContentType("application/json"); // 使用 ajax 的方式
        response.setCharacterEncoding("UTF-8");

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
     * 获取名字为 name 的  cookie 的值.
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
