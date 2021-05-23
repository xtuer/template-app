package com.xtuer.exception;

import com.alibaba.fastjson.JSON;
import com.xtuer.util.Utils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理 Controller 中 @Valid 参数校验异常，优先于普通异常处理
 */
@ControllerAdvice
@Order(1)
public class ValidExceptionHandler extends BaseExceptionHandler {
    @ExceptionHandler(value = BindException.class)
    public ModelAndView argumentInvalidHandler(HttpServletRequest request, HttpServletResponse response, BindException ex) {
        String error = Utils.getBindingMessage(ex.getBindingResult()); // 错误消息
        String stack = String.format("网址: %s%n参数: %s%n堆栈: %s",
                request.getRequestURL(),
                JSON.toJSONString(request.getParameterMap()),
                ExceptionUtils.getStackTrace(ex));

        return super.handleException(request, response, ex, error, stack, 501);
    }
}
