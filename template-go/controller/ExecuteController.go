package controller

import (
	"fmt"
	"newdtagent/bean"
	"newdtagent/service"
	"strings"

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
	router.POST(bean.API_EXECUTE_CMD, R(o.ExecuteCmd()))
	router.POST(bean.API_EXECUTE_SCRIPT, R(o.ExecuteScript()))
	router.GET(bean.API_JOBS_BY_ID, R(o.FindJobById()))
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
		 3. 参数校验: job 的 Cmd 字段不能为空。
		 4. 执行 Job 的命令。
		*/

		// [1] 创建 Job 对象。
		job := bean.NewJob()

		// [2] 参数获取: 把请求体中的数据绑定到 job。
		if err := c.ShouldBindJSON(job); err != nil {
			return FailResultWithMessage(err)
		}

		// 去掉命令多余的空格。
		job.Cmd = strings.TrimSpace(job.Cmd)

		// [3] 参数校验: job 的 Cmd 字段不能为空。
		if err := o.executeService.ValidateCmdJob(job); err != nil {
			return FailResultWithMessage(err)
		}

		// [4] 执行 Job 的命令。
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
		 3. 参数校验: ScriptName, ScriptContent, ScriptType 字段不能为空。
		 4. 执行 Job 的命令。
		*/

		// [1] 创建 Job 对象。
		job := bean.NewJob()

		// [2] 参数获取: 把请求体中的数据绑定到 job。
		if err := c.ShouldBindJSON(job); err != nil {
			return FailResultWithMessage(err)
		}

		// 去掉命令多余的空格。
		job.ScriptName = strings.TrimSpace(job.ScriptName)
		job.ScriptContent = strings.TrimSpace(job.ScriptContent)
		job.ScriptType = job.ScriptType.Trim()

		// [3] 参数校验: job 的 Cmd 字段不能为空。
		if err := o.executeService.ValidateScriptJob(job); err != nil {
			return FailResultWithMessage(err)
		}

		// [4] 执行 Job 的脚本。
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
// 测试: curl -X GET 'http://localhost:8080/api/jobs/2559aa8e-2fe1-4f9a-a061-0c8a754abbc9'
func (o *ExecuteController) FindJobById() RequestHandlerFunc {
	return func(c *gin.Context) *bean.Result {
		jobId := c.Param("jobId")
		job := o.executeService.FindJobById(jobId)

		if job == nil {
			return FailResultWithMessage(fmt.Sprintf("Job not found, jobId: %s", jobId))
		} else {
			return OkResultWithData(job)
		}
	}
}
