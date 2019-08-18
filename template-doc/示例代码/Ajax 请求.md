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

使用 Axios 的 Rest 插件 **axios.rest.js** 进行 Ajax 请求，使用 RESTful 风格:

* `获取数据` 使用 GET，前端调用 `Rest.get()`，后端使用 `@GetMapping`

  ```js
  Rest.get({ url: '/api/rest', data: { pageNumber: 3 } }).then(result => {
      console.log(result);
  });
  ```

* `创建数据` 使用 POST，前端调用 `Rest.create()`，后端使用 `@PostMapping`

  ```js
  Rest.create({ url: '/rest' } }).then(result => {
      console.log(result);
  });
  ```

* `更新数据` 使用 PUT，前端调用 `Rest.update()`，后端使用 `@PutMapping`

  ```js
  Rest.update({ url: '/rest', data: { name: 'Bob', age: 22 } }).then(result => {
      console.log(result);
  });
  ```

* `删除数据` 使用 DELETE，前端调用 `Rest.remove()`，后端使用 `@DeleteMapping`

  ```js
  Rest.remove({ url: '/rest' }).then(result => {
      console.log(result);
  });
  ```

提示: 请求时如果发生异常、token 无效、url 找不到、无权访问等，axios.rest.js 中已经对错误进行了拦截进行统一处理，弹窗提示用户，所以在我们的业务代码里不需要关心这些错误，只需要在 success 函数中编写正常的业务逻辑代码，偶尔处理一下 complete 函数，如隐藏加载状态。

## 更多示例

使用方法参考 <https://qtdebug.com/fe-axios-rest/>，下面列举了一些例子:

```js
Rest.get({ url: '/api/rest', data: { name: '张飞', value: 99 } }).then(result => {
    console.log(result);
});
Rest.create({ url: '/api/rest', data: { name: '张飞', value: 99 } }).then(result => {
    console.log(result);
});
Rest.update({ url: '/api/rest', data: { name: '张飞', value: 99 } }).then(result => {
    console.log(result);
});
Rest.remove({ url: '/api/rest', data: { name: '张飞', value: 99 } }).then(result => {
    console.log(result);
});

Rest.create({ url: '/api/rest1', data: { name: '张飞', value: 99 }, json: true }).then(result => {
    console.log(result);
});
Rest.update({ url: '/api/rest1', data: { name: '张飞', value: 99 }, json: true }).then(result => {
    console.log(result);
});
Rest.remove({ url: '/api/rest1', data: { name: '张飞', value: 99 }, json: true }).then(result => {
    console.log(result);
});
```

> 尽量使用 Restful 风格