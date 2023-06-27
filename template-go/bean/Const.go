package bean

import (
	"strings"
	"time"
)

// JobState 为任务状态。
type JobState string

const (
	JS_Running  JobState = "running"  // 执行中
	JS_Success  JobState = "success"  // 执行成功
	JS_Failed   JobState = "failed"   // 执行失败
	JS_Canceled JobState = "canceled" // 被取消
)

// ScriptType 为脚本类型。
type ScriptType string

const (
	ST_Shell  ScriptType = "shell"
	ST_Python ScriptType = "python"
)

// Trim 掉 ScriptType 的前后空格。
func (o ScriptType) Trim() ScriptType {
	return ScriptType(strings.TrimSpace(string(o)))
}

// 文件和分片上传状态。
const (
	US_Init     int = 0 // 初始化
	US_Success  int = 1 // 上传成功、合并成功
	US_Failed   int = 2 // 上传失败、合并失败
	US_Handling int = 3 // 上传中、合并中
)

// 升级 Agent 状态
const (
	UAS_Init     = 0 // 未开始
	UAS_Success  = 1 // 成功
	UAS_Failed   = 2 // 失败
	UAS_Updating = 3 // 升级中
)

// 文件分片大小: 10M
const Upload_File_Chunk_Size int64 = 10000000

// Agent 版本。
const Version = "v1.4"

// Agent 启动时间。
var StartTime time.Time = time.Now()
