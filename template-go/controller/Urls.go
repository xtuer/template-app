package controller

// 定义系统中使用到的所有 URLs。
const (
	// Agent 使用的 URI。
	API_TEST                  = "/api/test"
	API_JOBS_EXECUTE_CMD      = "/api/jobs/execute/cmd"        // 执行命令
	API_JOBS_EXECUTE_SCRIPT   = "/api/jobs/execute/script"     // 执行脚本
	API_JOBS_BY_ID            = "/api/jobs/:jobId"             // 指定 ID 的任务
	API_JOBS_COUNT            = "/api/jobs/count"              // 所有的任务数量
	API_JOBS_COUNT_OF_RUNNING = "/api/jobs/countOfRunningJobs" // 正在执行的任务数量
	API_MEM_STATS             = "/api/memStats"                // 内存分配状态
	API_UPLOADS               = "/api/uploads"                 // 上传信息
	API_UPLOADS_BY_FILE_UID   = "/api/uploads/:fileUid"        // 根据文件 Uid 对应的上传信息
	API_UPLOADS_CHUNK         = "/api/uploads/:fileUid/chunks" // 文件上传的分片
	API_STATS                 = "/api/stats"                   // Agent 的状态
	API_PING                  = "/api/ping"                    // 测试 Agent 是否可访问
	API_TRANSFERS_FILE        = "/api/transfers/file"          // 传输文件
	API_TRANSFERS_DIR         = "/api/transfers/dir"           // 传输目录
	API_TRANSFERS_BY_UID      = "/api/transfers/:transferUid"  // 传输文件

	// Watchdog 使用的 URI。
	API_AGENTS_UPDATE        = "/api/agents/versions/:version/update" // 升级 Agent
	API_AGENTS_START         = "/api/agents/start"                    // 启动 Agent
	API_AGENTS_STOP          = "/api/agents/stop"                     // 退出 Agent
	API_AGENTS_STATS         = "/api/agents/stats"                    // 获取 Agent
	API_AGENTS_UPDATE_STATUS = "/api/agents/updateStatus"             // 升级 Agent 状态
	API_AGENTS_LOGS          = "/api/agents/logs"                     // 列出日志文件名
	API_AGENTS_LOGS_CONTENT  = "/api/agents/logs/:logName/content"    // 查看日志内容
)
