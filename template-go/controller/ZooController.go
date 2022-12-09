package controller

import (
	"net/http"
	"newdtagent/bean"

	"github.com/gin-gonic/gin"
)

// ZooController 演示 Controller 的实现规范。
// [1] 定义控制器结构体。
type ZooController struct{}

// [2]: 创建控制器对象。
func NewZooController() *ZooController {
	return &ZooController{}
}

// [3] 注册当前控制器的路由。
func (o *ZooController) RegisterRoutes(router *gin.Engine) {
	router.GET(bean.API_TEST, o.GetTest())
}

// [4] 请求响应实现。
// GetTest 测试案例。
// 链接: http://localhost:8080/api/test
// 参数: 无
// 方法: GET
// 请求体: {} [可选]
// 响应: payload 为 JSON 对象。
//
// 测试: curl -X GET 'http://localhost:8080/api/test'
func (o *ZooController) GetTest() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.IndentedJSON(http.StatusOK, gin.H{
			"msg": "Hello",
		})
	}
}
