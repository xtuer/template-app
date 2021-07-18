package com.xtuer.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * 定义应用程序级别统一的异常，能够指定异常显示的页面，即 error page file。
 * 默认的错误页面是 error.html，异常还能传入 errorPageFile 指定自己的错误页面。
 */
@Getter
@Setter
public class ApplicationException extends RuntimeException {
    /**
     * 错误代码: 应用内部传递异常使用
     */
    private int code = 500;

    /**
     * 错误页面的模版文件: 给错误页面使用
     */
    private String errorPageFile;

    public ApplicationException(String message) {
        this(message, null);
    }

    public ApplicationException(String message, int code) {
        this(message, null);
        this.code = code;
    }

    public ApplicationException(String message, String errorPageFile) {
        super(message);
        this.errorPageFile = errorPageFile;
    }
}
