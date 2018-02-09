package com.xtuer.exception;

import com.alibaba.fastjson.JSON;
import com.xtuer.bean.Result;
import com.xtuer.controller.Urls;
import com.xtuer.util.NetUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SpringMvc 使用的异常处理类，统一处理未捕捉的异常:
 * 1. 当 AJAX 请求时发生异常，返回 JSON 格式的错误信息
 * 2. 非 AJAX 请求时发生异常，错误信息显示到 HTML 网页
 */
public final class HandlerExceptionResolver implements org.springframework.web.servlet.HandlerExceptionResolver {
    private static Logger logger = LoggerFactory.getLogger(HandlerExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler, Exception ex) {
        String error = ex.getMessage();
        String stack = ExceptionUtils.getStackTrace(ex);

        // 异常记录到日志里，对于运维非常重要
        logger.warn(error);
        logger.warn(stack);

        return NetUtils.useAjax(request) ? handleAjaxException(response, error, stack)
                                         : handleNonAjaxException(ex, error, stack);
    }

    /**
     * 处理 AJAX 请求时的异常: 把异常信息使用 Result 格式化为 JSON 格式，以 AJAX 的方式写入到响应数据中。
     *
     * @param response HttpServletResponse 对象
     * @param error 异常的描述信息
     * @param stack 异常的堆栈信息
     * @return 返回 null，这时 SpringMvc 不会去查找 view，会根据 response 中的信息进行响应。
     */
    private ModelAndView handleAjaxException(HttpServletResponse response, String error, String stack) {
        Result result = Result.fail(error, stack);
        NetUtils.ajaxResponse(response, JSON.toJSONString(result));
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
    private ModelAndView handleNonAjaxException(Exception ex, String error, String stack) {
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
