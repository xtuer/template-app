package training.exception;

/**
 * 定义应用程序级别统一的异常，能够指定异常显示的页面，即 error page file。
 * 默认的错误页面是 error.html，异常还能传入 errorPageFile 指定自己的错误页面。
 */
public class ApplicationException extends RuntimeException {
    private String errorPageFile = null; // 错误页面的模版文件

    public ApplicationException(String message) {
        this(message, null);
    }

    public ApplicationException(String message, String errorPageFile) {
        super(message);
        this.errorPageFile = errorPageFile;
    }

    public String getErrorPageFile() {
        return errorPageFile;
    }
}
