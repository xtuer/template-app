## 服务器

处理 Ajax 请求的函数返回类型规定为 **Result**，统一返回格式，减少不必要的混乱:

```java
@GetMapping(UriView.REST_KNOWLEDGE_POINTS)
@ResponseBody
public Result<List<KnowledgePoint>> knowledgePoints() {
    List<KnowledgePoint> kps = questionMapper.knowledgePoints();
    return Result.ok(kps);
}
```

```java
@PutMapping(UriView.REST_QUESTIONS_COUNT)
@ResponseBody
public Result updateQuestionsCount() {
    // 1. 先设置 count 为 0
    // 2. 题目表使用知识点 ID 分组，统计每个分组下有多少个题目，然后更新到知识点表
    questionMapper.cleanQuestionsCount();
    questionMapper.updateQuestionsCount();

    return Result.ok();
}
```

## Result 说明

AJAX 请求时的响应对象使用 Result，前端接收到 JSON 数据:

* success:

  * true: 正常请求不出错

    * 单个对象: 例如 MyBatis 查询数据库请求单个对象，存在就不说了，不存在时则返回 null，此时 success 仍然为 true，但是 data 因为 null 不输出，JS 中可判断后使用:

      ```js
      if (result.data) {
          // use data
      }
      ```

    * 对象数组: 例如 MyBatis 查询数据库请求多个对象，查找不到时返回空的 list，此时 success 仍然为 true，data 为 []，JS 中可正常使用数组访问:

      ```js
      result.data.forEach(item => {
          // use item
      });
      ```

  * false: 例如某些数据不满足业务需求，参数验证不通过等时把 success 设置为 false，前端收到 JSON 如:

    ```js
    {
        "success": false,
        "message": "fail",
        "data"   : "ID 不能为 null\nInfo 不能为空"
    }​
    ```

* data: 保存数据的地方

## 浏览器

使用 jQuery 的 Rest 插件 **jquery.rest.js** 进行 Ajax 请求，使用 RESTful 风格:

* `获取数据` 使用 GET，前端调用 `$.rest.get()`，后端使用 `@GetMapping`

  ```js
  $.rest.get({url: '/rest', data: {name: '黄彪'}, success: function(result) {
      console.log(result);
  }});
  ```

* `创建数据` 使用 POST，前端调用 `$.rest.create()`，后端使用 `@PostMapping`

  ```js
  $.rest.create({url: '/rest', success: function(result) {
      console.log(result);
  }});
  ```

* `更新数据` 使用 PUT，前端调用 `$.rest.update()`，后端使用 `@PutMapping`

  ```js
  $.rest.update({url: '/rest', data: {name: 'Bob', age: 22}, success: function(result) {
      console.log(result);
  }});
  ```

* `删除数据` 使用 DELETE，前端调用 `$.rest.remove()`，后端使用 `@DeleteMapping`

  ```js
  $.rest.remove({url: '/rest', success: function(result) {
      console.log(result);
  }});
  ```

提示: 请求时如果发生异常、token 无效、url 找不到、无权访问等，jquery.rest.js 中已经对错误进行了拦截进行统一处理，弹窗提示用户，所以在我们的业务代码里不需要关心这些错误，只需要在 success 函数中编写正常的业务逻辑代码，偶尔处理一下 complete 函数，如隐藏加载状态。

## 更多示例

使用方法参考 <http://qtdebug.com/fe-rest/>，下面列举了一些例子:

```js
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>REST</title>
</head>
<body>
    <script src="http://cdn.bootcss.com/jquery/3.2.1/jquery.min.js"></script>
    <script src="/js/jquery.rest.js"></script>
    <script>
        // [1] 服务器端的 GET 需要启用 UTF-8 才不会乱吗
        $.rest.get({url: '/rest', data: {name: 'Alice'}, success: function(result) {
            console.log(result);
        }});
 
        // url 中可以使用 {name} 的参数占位符，然后使用 pathVariables 中的属性替换
        $.rest.get({url: '/api/users/{userId}', pathVariables: {userId: 23}, data: {name: 'Alice'}, 
            success: function(result) {
                console.log(result);
            }
        });
      
        // [2] 普通 form 表单提交 rest Ajax 请求
        $.rest.create({url: '/rest', success: function(result) {
            console.log(result);
        }});
        $.rest.update({url: '/rest', data: {name: '黄飞鸿', age: 22}, success: function(result) {
            console.log(result);
        }});
      
        $.rest.remove({url: '/rest', success: function(result) {
            console.log(result);
        }});
      
        // [3] 使用 request body 传递复杂 Json 对象
        $.rest.create({url: '/rest/requestBody', data: {name: 'Alice'}, jsonRequestBody: true, 
            success: function(result) {
                console.log(result);
            }
        });
      
        $.rest.update({url: '/rest/requestBody', data: {name: 'Alice'}, jsonRequestBody: true, 
            success: function(result) {
                console.log(result);
            }
        });
      
        $.rest.remove({url: '/rest/requestBody', data: {name: 'Alice'}, jsonRequestBody: true, 
            success: function(result) {
                console.log(result);
            }
        });
    </script>
</body>
</html>
```

> 尽量使用 Restful 风格