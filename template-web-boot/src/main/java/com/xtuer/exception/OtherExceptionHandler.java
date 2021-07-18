package com.xtuer.exception;

import org.springframework.core.Ordered;
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
@Order(Ordered.LOWEST_PRECEDENCE)
public final class OtherExceptionHandler extends BaseExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleException(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        String error = "异常: 服务器 " + BaseExceptionHandler.IP + ", ID " + uidGenerator.getUID() + "\n" + ex.getMessage();
        String stack = super.getStack(request, ex);

        return super.handleException(request, response, ex, error, stack, 500);
    }
}
