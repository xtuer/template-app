package bean

// LogfileInfo 为日志文件信息。
type LogfileInfo struct {
	Filename  string `json:"filename"`  // 日志文件名
	LineCount int    `json:"lineCount"` // 文件行数
}
