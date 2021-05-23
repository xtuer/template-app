package com.xtuer.exception;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SpringMvc 使用的异常处理器，统一处理未捕捉的异常:
 * 1. 当 AJAX 请求时发生异常，返回 JSON 格式的错误信息，状态码为 500
 * 2. 非 AJAX 请求时发生异常，错误信息显示到 HTML 网页
 */
@ControllerAdvice
@Order(2)
public final class GlobalExceptionHandler extends BaseExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ModelAndView exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String error = "异常: 服务器 " + BaseExceptionHandler.IP + ", ID " + uidGenerator.getUID();
        String stack = String.format("网址: %s%n参数: %s%n堆栈: %s",
                request.getRequestURL(),
                JSON.toJSONString(request.getParameterMap()),
                ExceptionUtils.getStackTrace(ex));

        // 错误编码
        int code = 500;

        // 优先使用自定义 code
        if (ex instanceof ApplicationException) {
            code = ((ApplicationException) ex).getCode();
            code = code == 0 ? 500 : code;
        }

        return super.handleException(request, response, ex, error, stack, code);
    }
}
