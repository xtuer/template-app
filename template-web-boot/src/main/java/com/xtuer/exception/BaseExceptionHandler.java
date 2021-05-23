package com.xtuer.exception;

import com.github.wujun234.uid.impl.CachedUidGenerator;
import com.xtuer.bean.Result;
import com.xtuer.bean.Urls;
import com.xtuer.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 异常处理基类
 */
@Slf4j
public class BaseExceptionHandler {
    /**
     * ID 生成器
     */
    @Autowired
    protected CachedUidGenerator uidGenerator;

    /**
     * 局域网 IP
     */
    protected static final String IP = WebUtils.getLocalIp();

    /**
     * 异常处理，AJAX 请求和普通请求响应不同
     *
     * @param error 错误信息
     * @param stack 堆栈信息
     * @param code  错误代码
     */
    protected final ModelAndView handleException(HttpServletRequest request,
                                                 HttpServletResponse response,
                                                 Exception ex,
                                                 String error,
                                                 String stack,
                                                 int code) {
        // 异常记录到日志里，对于运维非常重要
        log.warn(error);
        log.warn(stack);

        return WebUtils.useAjax(request)
                ? handleAjaxException(response, error, stack, code)
                : handleNonAjaxException(ex, error, stack);
    }

    /**
     * 处理 AJAX 请求时的异常: 把异常信息使用 Result 格式化为 JSON 格式，以 AJAX 的方式写入到响应数据中，HTTP 状态码为 500，Result.code 也为 500
     *
     * @param response HttpServletResponse 对象
     * @param error    异常的描述信息
     * @param stack    异常的堆栈信息
     * @return 返回 null，这时 SpringMvc 不会去查找 view，会根据 response 中的信息进行响应
     */
    protected final ModelAndView handleAjaxException(HttpServletResponse response, String error, String stack, int code) {
        Result<?> result = Result.fail(error, code);
        result.setStack(stack);

        WebUtils.ajaxResponse(response, result, HttpServletResponse.SC_OK);
        return null;
    }

    /**
     * 处理非 AJAX 请求时的异常:
     * 1. 如果异常是 ApplicationException 类型的
     *    A. 如果指定了 errorViewName，则在 errorViewName 对应的网页上显示异常
     *    B. 如果没有指定 errorViewName，则在显示异常的默认页面显示异常
     * 2. 非 ApplicationException 的异常，即其它所有类型的异常，则在显示异常的默认页面显示异常
     *
     * @param ex 异常对象
     * @param error 异常的描述信息
     * @param stack 异常的堆栈信息
     * @return ModelAndView 对象，给定了 view 和异常信息
     */
    protected final ModelAndView handleNonAjaxException(Exception ex, String error, String stack) {
        String errorPageFile = Urls.FILE_ERROR; // 显示错误的默认页面

        // 如果是我们定义的异常 ApplicationException，则取得它的异常显示页面的 view name
        if (ex instanceof ApplicationException) {
            ApplicationException appEx = (ApplicationException) ex;
            errorPageFile = (appEx.getErrorPageFile() == null) ? errorPageFile : appEx.getErrorPageFile();
        }

        ModelMap model = new ModelMap();
        model.addAttribute("error", error);  // 异常信息
        model.addAttribute("detail", stack); // 异常堆栈

        return new ModelAndView(errorPageFile, model);
    }
}
