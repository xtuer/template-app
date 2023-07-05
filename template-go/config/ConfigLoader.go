package config

import (
	"flag"
	"fmt"
	"os"
	"xtuer/bean"
	"xtuer/utils"

	"sigs.k8s.io/yaml"
)

// 应用配置对象，不想配置内容被外界直接修改所以定义为栈对象。
var conf bean.AppConfig

// GetAppConfig 获取配置对象 (Immutable)
func GetAppConfig() bean.AppConfig {
	return conf
}

// init 读取配置创建应用配置对象。
func init() {
	// 从配置文件读取配置。
	readConfig()

	// 程序端口。
	if conf.Port == 0 {
		conf.Port = 8000
	}

	// 本机 IP。
	if conf.HostIp == "" {
		ips := utils.HostIps()
		if len(ips) > 0 {
			conf.HostIp = ips[0]
		}
	}

	// 上传文件临时目录。
	if conf.UploadTempDir == "" {
		conf.UploadTempDir = conf.WorkDir + "/upload-temp"
	}

	// 日志目录。
	if conf.LogDir == "" {
		conf.LogDir = conf.WorkDir + "/logs"
	}

	// 日志文件名。
	conf.LogName = utils.ExecName()

	// PID 文件路径。
	conf.PidPath = conf.WorkDir + "/pid"

	// Sqlite 数据库文件路径。
	conf.DbPath = conf.WorkDir + "/data.db"
	if !utils.FileExist(conf.DbPath) {
		panic(fmt.Sprintf("数据库文件不存在: %s", conf.DbPath))
	}
	if !utils.IsFileWritable(conf.DbPath) {
		panic(fmt.Sprintf("数据库文件不可写: %s", conf.DbPath))
	}

	// 签名的盐。
	if conf.SignSalt == "" {
		conf.SignSalt = "newdt"
	}
	// 签名的秘钥。
	if conf.SignSecret == "" {
		conf.SignSecret = "shindata"
	}

	// 打印配置。
	fmt.Println("配置:", utils.ToJson(conf))
}

// readConfig 从配置文件读取配置。
func readConfig() {
	// 工作目录。
	var workDir string

	// 解析命令行参数: go run main.go -wd /Users/biao/Documents/workspace/newdt/xtuer
	flag.StringVar(&workDir, "wd", "", "工作目录")
	flag.Parse()

	// 如果没有 wd 参数指定工作目录则设置为可执行程序所在目录。
	if workDir == "" {
		workDir = utils.ExecDir()
	}
	// 如果 workDir 为根目录则设置为 ""，因为下面在拼路径时都会带上 /，如果不设置就会造成 //config.yml 这种情况。
	if workDir == "/" {
		workDir = ""
	}

	// 读取配置文件内容。
	configContent, err := os.ReadFile(workDir + "/config.yml")
	if err != nil {
		panic(err)
	}

	// 把 yaml 内容反序列化为配置对象。
	err = yaml.Unmarshal(configContent, &conf)
	if err != nil {
		panic(err)
	}

	// 设置工作目录。
	conf.WorkDir = workDir
}
