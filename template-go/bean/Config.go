package bean

// AppConfig 为应用配置。
type AppConfig struct {
	Port                   uint   `json:"port"`                   // 程序端口                        - [可配置，程序端口，默认为 8000]
	HostIp                 string `json:"hostIp"`                 // 本机 IP                        - [可配置，默认程序自动获取]
	WorkDir                string `json:"workDir"`                // 工作目录                        - <不可配置>
	LogDir                 string `json:"logDir"`                 // 日志目录                        - [可配置，默认在可执行程序目录]
	LogName                string `json:"logName"`                // 日志文件名                      - (不包含后缀) <不可配置>
	UploadTempDir          string `json:"uploadTempDir"`          // 上传文件临时目录                 - [可配置，默认在可执行程序目录]
	DbPath                 string `json:"dbPath"`                 // 数据库文件路径                   - <不可配置，在可执行程序目录>
	PidPath                string `json:"pidPath"`                // 当前进程的 PID 文件路径           - <不可配置>
	SignSalt               string `json:"signSalt"`               // 签名使用的盐 					  - [可配置，默认为 newdt]
	SignSecret             string `json:"signSecret"`             // 签名使用的秘钥 				  - [可配置，默认为 shindata]
	RemoveScriptWhenFinish bool   `json:"removeScriptWhenFinish"` // 脚本执行结束后删除脚本             - [可配置，默认为 false]
}

// NewAppConfig 创建 AppConfig 对象。
func NewAppConfig() *AppConfig {
	return &AppConfig{}
}
