package com.xtuer.exception;

import com.xtuer.util.Utils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理 Controller 中 @Valid 参数校验异常，IllegalArgumentException 参数异常，优先于普通异常处理
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ArgumentExceptionHandler extends BaseExceptionHandler {
    // @Valid 验证参数失败异常处理
    @ExceptionHandler(value = BindException.class)
    public ModelAndView handleBindException(HttpServletRequest request, HttpServletResponse response, BindException ex) {
        String error = Utils.getBindingMessage(ex.getBindingResult()); // 错误消息
        String stack = super.getStack(request, ex);

        return super.handleException(request, response, ex, error, stack, 501);
    }

    // @Valid 验证参数失败异常处理
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ModelAndView handleMethodArgumentNotValidException(HttpServletRequest request, HttpServletResponse response, MethodArgumentNotValidException ex) {
        String error = Utils.getBindingMessage(ex.getBindingResult()); // 错误消息
        String stack = super.getStack(request, ex);

        return super.handleException(request, response, ex, error, stack, 501);
    }

    // 无效参数异常处理
    @ExceptionHandler(value = IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentException(HttpServletRequest request, HttpServletResponse response, IllegalArgumentException ex) {
        String error = ex.getMessage(); // 错误消息
        String stack = super.getStack(request, ex);

        return super.handleException(request, response, ex, error, stack, 502);
    }
}
