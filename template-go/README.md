**项目结构介绍:**

* 典型的 Java Web 项目结构:
  * bean
  * controller
  * service
  * utils
* gin 作为 Web 服务框架
* logrus 作为日志库，日志按天滚动保存
* 实用工具:
  * 生成 uid
  * 生成 MD5
* 统一的 HTTP 请求格式，使用结构体 bean.Result

**处理请求的步骤:**

1. 在 **controller/Urls.go** 中定义 URL 如 `API_XXX = "/api/xxx"`，把所有 URL 统一集中管理。

2. 定义 Controller: 在 **controller/XxxController.go** 中创建一个结构体 `XxxController`，创建函数 RegisterRoutes 注册 gin 的路由:

   ```go
   type XxxController struct{}
   
   // 创建 Controller 对象。
   func NewXxxController() *XxxController {
   	return &XxxController{}
   }
   
   // 注册在 gin 中的路由，函数 R 把处理请求返回的函数转换为 gin.HandlerFunc。
   func (o *XxxController) RegisterRoutes(router *gin.Engine) {
   	router.GET(API_XXX, R(o.GetTest()))
   }
   ```

   > 参考 **controller/ZooController.go**

3. 在 XxxController 中实现处理请求的方法，方法的返回值为 RequestHandlerFunc，方法中返回函数，如下:

   ```go
   func (o *XxxController) GetTest() RequestHandlerFunc {
   	return func(c *gin.Context) *bean.Result {
       return OkResultWithMessage("hello")
   	}
   }
   ```

   > Controller 中只是简单的转发请求，复杂一点的业务逻辑可以创建一个结构体 XxxService 来处理，并把它作为 Controller 的一个属性，然后在 Controller 的方法中就可以调用 Service 的方法了。

4. 在 main 函数中注册 Controller: 

   ```go
   controller.NewXxxController().RegisterRoutes(router)
   ```

   