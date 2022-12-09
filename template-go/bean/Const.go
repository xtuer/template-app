package bean

import "strings"

// JobState 为任务状态。
type JobState string

const (
	JSRunning  JobState = "running"
	JSFinished JobState = "finished"
	JSCanceled JobState = "canceled"
)

// 脚本类型
type ScriptType string

const (
	STShell  ScriptType = "shell"
	STPython ScriptType = "python"
)

// Trim 掉 ScriptType 的前后空格
func (o ScriptType) Trim() ScriptType {
	return ScriptType(strings.TrimSpace(string(o)))
}
