package service

import (
	"errors"
	"newdtagent/bean"
	"newdtagent/utils"
	"strings"
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
	if strings.Contains(job.ScriptName, " ") {
		errMsg = "scriptName 不能包含空格"
	}
	if job.ScriptContent == "" {
		errMsg = "scriptContent 不能为空"
	}
	if !job.IsShellJob() && !job.IsPythonJob() {
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

	// TODO: 如果任务状态是 running，需要检查任务的进程是否还在，如果不存在则任务状态设置为 canceled。

	return job
}

// ExecuteJob 执行 Job。
func (o *ExecuteService) ExecuteJob(job *bean.Job) error {
	/*
	 执行逻辑:
	 1. 如果是脚本类型，则把脚本内容保存到文件。
	*/

	// [1] 如果是脚本类型，则把脚本内容保存到文件。
	if err := o.saveScriptToFileAsNeeded(job); err != nil {
		return err
	}

	o.jobStore.AddJob(job)
	return nil
}

// saveScriptToFileAsNeeded 把脚本内容保存到文件，并且把得到的文件路径保存到 Job.ScriptPath
func (o *ExecuteService) saveScriptToFileAsNeeded(job *bean.Job) error {
	// 既不是 shell 也不是 python 脚本类型则返回。
	if !job.IsShellJob() && !job.IsPythonJob() {
		return nil
	}

	// 脚本内容保存到临时文件。
	path, err := utils.SaveScriptToTempFile(job.ScriptContent)
	if err != nil {
		return err
	}

	// 保存脚本路径；清除 Job.ScriptContent，因为脚本内容保存到文件后不再需要保存在对象里了。
	job.ScriptPath = path
	job.ScriptContent = ""
	return nil
}
