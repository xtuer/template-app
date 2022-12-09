package bean

// 定义系统中使用到的所有 URLs。
const (
	API_TEST           = "/api/test"
	API_EXECUTE_CMD    = "/api/execute/cmd"    // 执行命令
	API_EXECUTE_SCRIPT = "/api/execute/script" // 执行脚本
	API_JOBS_BY_ID     = "/api/jobs/:jobId"    // 指定 ID 的任务
)
