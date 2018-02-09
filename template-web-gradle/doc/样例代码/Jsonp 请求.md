## 服务器

```java
@GetMapping(value="/jsonp-test", produces="application/javascript;charset=UTF-8")
// @GetMapping(value="/jsonp-test", produces=Urls.JSONP_CONTENT_TYPE)
@ResponseBody
public String jsonpTest(@RequestParam String callback) {
    return Result.jsonp(callback, Result.ok("Congratulation", "Your data object"));
}
```

> 注意: 服务器端需要设置返回的 Content-Type 为 "application/javascript;charset=UTF-8"，这里使用 produces 来设置

## 浏览器

```js
// 1. 使用 jquery.rest.js 中自定义插件
$.jsonp('http://127.0.0.1:8080/demo/jsonp-test', function(data) {
    console.log(data);
});

// 2. 使用 jQuery 的 ajax 函数
$.ajax({
    url     : 'http://127.0.0.1:8080/demo/jsonp-test',
    type    : 'GET',
    dataType: 'jsonp',
    success : function(data) {
        console.log(data);
    }
});
```

