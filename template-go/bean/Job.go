package bean

import (
	"newdtagent/utils"
	"time"
)

// Job 为执行 CMD 或 Script 时创建的对象，描述了任务和任务的进程的信息。
type Job struct {
	Id            string     `json:"id"`            // 任务的 ID，使用 uuid 生成
	Cmd           string     `json:"cmd"`           // 要执行的命令
	Params        string     `json:"params"`        // 执行命令或者脚本的参数，格式为 -k1 v1 -k2 v2
	ScriptName    string     `json:"scriptName"`    // 要执行的脚本名称
	ScriptContent string     `json:"scriptContent"` // 脚本内容
	ScriptType    ScriptType `json:"scriptType"`    // 脚本类型，值为 shell 或者 python
	Async         bool       `json:"async"`         // 是否异步执行命令或者脚本，为 true 时异步执行，为 false 时同步执行

	Pid    int      `json:"pid"`    // 任务的进程的 PID
	State  JobState `json:"state"`  // 任务的状态，值为 running, finished, canceled
	Stdout string   `json:"stdout"` // 任务的进程的标准输出
	Stderr string   `json:"stderr"` // 任务的进程的错误输出
	Rc     int      `json:"rc"`     // 任务的进程的返回值

	CreateTime time.Time `json:"createTime"` // 任务创建的时间
	StartTime  time.Time `json:"startTime"`  // 任务开始的时间
	FinishTime time.Time `json:"finishTime"` // 任务结束的时间
}

// NewJob 创建 Job 指针对象，并且自动生成 Job 的 ID。
func NewJob() *Job {
	job := &Job{}
	job.Id = utils.Uid()
	job.CreateTime = time.Now()

	return job
}
