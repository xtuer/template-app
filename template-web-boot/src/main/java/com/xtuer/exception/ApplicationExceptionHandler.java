package com.xtuer.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理 Controller 中 ApplicationException
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class ApplicationExceptionHandler extends BaseExceptionHandler {
    @ExceptionHandler(value = ApplicationException.class)
    public ModelAndView handleApplicationException(HttpServletRequest request, HttpServletResponse response, ApplicationException ex) {
        String error = ex.getMessage(); // 错误消息
        String stack = super.getStack(request, ex);

        return super.handleException(request, response, ex, error, stack, ex.getCode());
    }
}
