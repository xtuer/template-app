package com.xtuer.bean;

import com.alibaba.fastjson.JSONPObject;
import com.xtuer.util.Utils;
import lombok.Getter;
import lombok.Setter;

/**
 * <pre>
 * 规定应用中 Api 接口都返回统一格式的 Json 数据，方便前端调用，通过类 Result + Json HttpMessageConverter 来实现自动转换。
 * 类 Result 提供了多个变种的方法 ok() 和 fail() 简化创建 Result 对象。
 *
 * 成功关注的是数据，失败关注的是错误信息，所以
 *     A. 方法 ok() 的核心是 data (code 无特殊情况都为 0)
 *     B. 方法 fail() 的核心是 message
 */
@Getter
@Setter
public final class Result<T> {
    private boolean success; // 成功时为 true，失败时为 false
    private String  message; // 成功或则失败时的描述信息
    private String  stack;   // 抛出异常时的堆栈信息
    private T       data;    // 成功或则失败时的更多详细数据，一般失败时不需要
    private int     code;    // 状态码，一般是当 success 为 true 或者 false 不足够表达时才使用，平时忽略即可

    public Result(boolean success, String message) {
        this(success, message, null);
    }

    public Result(boolean success, String message, T data) {
        this(success, message, data, 0);
    }

    public Result(boolean success, String message, T data, int code) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.code = code;
    }

    public static <T> Result<T> ok() {
        return Result.ok(null, "success");
    }

    public static <T> Result<T> ok(T data) {
        return Result.ok(data, "success");
    }

    public static <T> Result<T> ok(T data, String message, String ...messageArgs) {
        if (messageArgs.length > 0) {
            message = Utils.replaceBracePlaceholder(message, messageArgs);
        }

        return new Result<>(true, message, data, 0);
    }

    public static <T> Result<T> fail() {
        return Result.fail("fail", 0);
    }

    public static <T> Result<T> fail(String message, String ...messageArgs) {
        if (messageArgs.length > 0) {
            message = Utils.replaceBracePlaceholder(message, messageArgs);
        }

        return Result.fail(message, 0);
    }

    public static <T> Result<T> fail(String message, int code) {
        return new Result<>(false, message, null, code);
    }

    /**
     * 返回单个对象时根据对象是否为空返回成功或者失败的不同结果:
     *     A. data 不为 null 时执行 Result.ok(data)
     *     B. data 等于 null 时执行 Result.failMessage(error)
     */
    public static <T> Result<T> single(T data) {
        return Result.single(data, "");
    }

    public static <T> Result<T> single(T data, String error) {
        return (data != null) ? Result.ok(data) : Result.fail(error);
    }

    /**
     * 使用传入的回调函数名字 callback 和参数 params 构造一个 JSONP 响应格式的字符串。
     *
     * @param callback 浏览器端 JSONP 回调函数的名字
     * @param data 参数列表
     * @return 返回 JSONP 格式的字符串
     */
    public static String jsonp(String callback, Object data) {
        JSONPObject jp = new JSONPObject(callback);
        jp.addParameter(data);

        return jp.toString();
    }
}
