package com.xtuer.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPObject;
import lombok.Getter;
import lombok.Setter;

/**
 * Http Ajax 请求返回时用作返回的对象，FastJson 自动转换为 Json 字符串返回给前端。
 *
 * 虽然同一个请求在不同情况下返回的 Result 中的 data 类型可能不同，例如 Result<User> findUserByName(String name)，
 * 查询到用户时返回 Result 中 data 是 User 对象，查询不到用户时可返回 Result 中 data 是 String 对象，不过没关系，
 * 在我们的实现中允许这么做，好处是标志出了请求正确响应时返回的数据类型，因为这个是我们最关心的，至于错误的类型，
 * 一般会用 String 描述，前端得到 success 为 false，大多都是把错误信息显示给用户即可。
 */
@Getter
@Setter
public final class Result<T> {
    private boolean success;    // 成功时为 true，失败时为 false
    private String  message;    // 成功或则失败时的描述信息
    private Object  data;       // 成功或则失败时的更多详细数据，一般失败时不需要
    private Integer statusCode; // 状态码，一般是当 success 为 true 或者 false 时不足够表达时可使用

    public Result(boolean success, String message) {
        this(success, message, null);
    }

    public Result(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public Result(boolean success, String message, Object data, int statusCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = statusCode;
    }

    public static <T> Result<T> ok() {
        return new Result<>(true, "success");
    }

    public static <T> Result<T> ok(Object data) {
        return new Result<>(true, "success", data);
    }

    public static <T> Result<T> ok(String message, Object data) {
        return new Result<>(true, message, data);
    }

    public static <T> Result<T> fail() {
        return new Result<>(false, "fail");
    }

    public static <T> Result<T> fail(Object data) {
        return new Result<>(false, "fail", data);
    }

    public static <T> Result<T> fail(String message, Object data) {
        return new Result<>(false, message, data);
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

    // 测试
    public static void main(String[] args) {
        // JSON
        Result<User> r1 = Result.ok();
        Result<User> r2 = Result.ok(new User("Alice", "Passw0rd"));
        Result<User> r3 = Result.ok("Yes", new Demo(123456L, "Physics"));

        System.out.println(JSON.toJSONString(r1));
        System.out.println(JSON.toJSONString(r2));
        System.out.println(JSON.toJSONString(r3));

        System.out.println(r3.getData());

        // JSONP
        System.out.println(Result.jsonp("callback", Result.ok("Hello")));
    }
}
