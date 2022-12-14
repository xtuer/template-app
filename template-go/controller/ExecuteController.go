package controller

import (
	"fmt"
	"net/http"
	"newdtagent/bean"
	"newdtagent/service"

	"github.com/gin-gonic/gin"
)

// ExecuteController 为执行 CMD 或者 Script 的 Controller。
type ExecuteController struct {
	executeService *service.ExecuteService // 执行服务对象。
}

// NewExecuteController 创建 ExecuteController 控制器对象。
func NewExecuteController(executeService *service.ExecuteService) *ExecuteController {
	return &ExecuteController{executeService}
}

// RegisterRoutes 注册路由。
func (o *ExecuteController) RegisterRoutes(router *gin.Engine) {
	router.POST(API_EXECUTE_CMD, R(o.ExecuteCmd()))
	router.POST(API_EXECUTE_SCRIPT, R(o.ExecuteScript()))
	router.GET(API_JOBS_BY_ID, R(o.FindJobById()))
}

// ExecuteCmd 接收执行 CMD 的请求。
// 链接: http://localhost:8080/api/execute/cmd
// 参数: 无
// 方法: POST
// 请求体: {"cmd": "ls /root", "params": "-k1 v1 -k2 v2", "async": true }
// 响应: payload 为 Job 对象。
//
// 测试: curl -X POST 'http://localhost:8080/api/execute/cmd' -H 'Content-Type: application/json' -d '{"cmd": "ls /root", "async": true}'
func (o *ExecuteController) ExecuteCmd() RequestHandlerFunc {
	return func(c *gin.Context) *bean.Result {
		/*
		 逻辑:
		 1. 创建 Job 对象。
		 2. 参数获取: 把请求体中的数据绑定到 job。
		 3. 执行 Job 的命令。
		*/

		// [1] 创建 Job 对象。
		job := bean.NewJob()

		// [2] 参数获取: 把请求体中的数据绑定到 job。
		if err := c.ShouldBindJSON(job); err != nil {
			return FailResultWithMessage(err, http.StatusBadRequest)
		}

		// [3] 执行 Job 的命令。
		if err := o.executeService.ExecuteJob(job); err != nil {
			return FailResultWithMessage(err)
		}

		return OkResultWithData(job)
	}
}

// ExecuteScript 接收脚本并执行。
// 链接: http://localhost:8080/api/execute/script
// 参数: 无
// 方法: POST
// 请求体: {"cmd": "ls /root", "params": "-k1 v1 -k2 v2", "async": true }
// 响应: payload 为 Job 对象。
//
// 测试: curl -X POST 'http://localhost:8080/api/execute/script' -H 'Content-Type: application/json' -d '{"scriptName": "x.sh", "scriptContent": "echo hi", "scriptType": "shell"}'
func (o *ExecuteController) ExecuteScript() RequestHandlerFunc {
	return func(c *gin.Context) *bean.Result {
		/*
		 逻辑:
		 1. 创建 Job 对象。
		 2. 参数获取: 把请求体中的数据绑定到 job。
		 3. 执行 Job 的命令。
		*/

		// [1] 创建 Job 对象。
		job := bean.NewJob()

		// [2] 参数获取: 把请求体中的数据绑定到 job。
		if err := c.ShouldBindJSON(job); err != nil {
			return FailResultWithMessage(err, http.StatusBadRequest)
		}

		// [3] 执行 Job 的脚本。
		if err := o.executeService.ExecuteJob(job); err != nil {
			return FailResultWithMessage(err)
		}

		return OkResultWithData(job)
	}
}

// FindJobById 查找 ID 为传入参数 jobId 的任务。
// 链接: http://localhost:8080/api/jobs/:jobId
// 参数: 无
// 方法: GET
// 响应: payload 为 Job 对象。
//
// 测试: curl 'http://localhost:8080/api/jobs/2559aa8e-2fe1-4f9a-a061-0c8a754abbc9'
func (o *ExecuteController) FindJobById() RequestHandlerFunc {
	return func(c *gin.Context) *bean.Result {
		jobId := c.Param("jobId")
		job := o.executeService.FindJobById(jobId)

		if job == nil {
			return FailResultWithMessage(fmt.Sprintf("Job not found, jobId: %s", jobId), http.StatusNotFound)
		} else {
			return OkResultWithData(job)
		}
	}
}
