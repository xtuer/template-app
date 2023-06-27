package controller

import (
	"fmt"
	"math/rand"
	"newdtagent/bean"
	"newdtagent/log"
	"runtime"
	"time"

	"github.com/dustin/go-humanize"
	"github.com/gin-gonic/gin"
)

// ZooController 演示 Controller 的实现规范。
// [1] 定义控制器结构体。
type ZooController struct{}

// [2] 创建控制器对象。
func NewZooController() *ZooController {
	return &ZooController{}
}

// [3] 注册当前控制器的路由。
func (o *ZooController) RegisterRoutes(router *gin.Engine) {
	router.GET(API_TEST, R(o.GetTest()))
	router.GET(API_MEM_STATS, R(o.MemStats()))
	router.GET(API_PING, R(o.Ping()))
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
func (o *ZooController) GetTest() RequestHandlerFunc {
	return func(c *gin.Context) bean.Response {
		log.Log.Info("Hello test!")

		n := rand.Intn(10)
		if n%3 == 0 {
			return ErrorResponseWithMessage(fmt.Sprintf("Time is %v", time.Now()))
		} else {
			panic("Panic occured")
		}
	}
}

// MemStats 获取内存状态。
// 链接: http://localhost:8080/api/memStats
// 参数: 无
// 方法: GET
// 响应: payload 为内存状态对象。
//
// 测试: curl http://localhost:8080/api/memStats
func (o *ZooController) MemStats() RequestHandlerFunc {
	return func(c *gin.Context) bean.Response {
		var mem runtime.MemStats
		runtime.ReadMemStats(&mem)

		return OkResponseWithData(gin.H{
			"alloc":      humanize.IBytes(mem.Alloc),
			"totalAlloc": humanize.IBytes(mem.TotalAlloc), // 累计的内存分配总量
			"heapAlloc":  humanize.IBytes(mem.HeapAlloc),  // 实时的内存分配情况
			"heapSys":    humanize.IBytes(mem.HeapSys),
		})
	}
}

// Ping 测试 Agent 可访问。
// 链接: http://localhost:8080/api/ping
// 参数: 无
// 方法: GET
// 响应: payload 为 PONG。
//
// 测试: curl http://localhost:8080/api/ping
func (o *ZooController) Ping() RequestHandlerFunc {
	return func(c *gin.Context) bean.Response {
		return OkResponseWithMessage("PONG")
	}
}
