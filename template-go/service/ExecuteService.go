package service

import (
	"encoding/json"
	"errors"
	"newdtagent/bean"
)

// ExecuteService 执行服务。
type ExecuteService struct {
	jobStore *bean.JobStore
}

// NewExecuteService 创建 ExecuteService 对象。
func NewExecuteService() *ExecuteService {
	return &ExecuteService{
		jobStore: bean.NewJobStore(),
	}
}

// ValidateCmdJob 验证 CMD Job 的参数。
// @return 验证通过返回 nil，验证不通过返回错误对象。
func (o ExecuteService) ValidateCmdJob(job *bean.Job) error {
	if job.Cmd == "" {
		return errors.New("cmd 不能为空")
	} else {
		return nil
	}
}

// ValidateScriptJob 验证 Script Job 的参数。
// @return 验证通过返回 nil，验证不通过返回错误对象。
func (o ExecuteService) ValidateScriptJob(job *bean.Job) error {
	var errMsg string

	if job.ScriptName == "" {
		errMsg = "scriptName 不能为空"
	}
	if job.ScriptContent == "" {
		errMsg = "scriptContent 不能为空"
	}
	if job.ScriptType != bean.STShell && job.ScriptType != bean.STPython {
		errMsg = "scriptType 的值必须为 shell 或者 python"
	}

	if errMsg != "" {
		return errors.New(errMsg)
	} else {
		return nil
	}
}

// FindJobById 查找 ID 为传入参数 jobId 的任务。
// @return 返回查询到的任务，查找不到时返回 nil。
func (o ExecuteService) FindJobById(jobId string) *bean.Job {
	job := o.jobStore.FindJobById(jobId)

	if job == nil {
		return nil
	}

	// 深拷贝 job，去掉 Job 中的 ScriptContent。
	j, _ := json.Marshal(job)
	json.Unmarshal(j, job)
	job.ScriptContent = ""

	return job
}

// ExecuteJob 执行 Job。
func (o *ExecuteService) ExecuteJob(job *bean.Job) {
	o.jobStore.AddJob(job)
}
