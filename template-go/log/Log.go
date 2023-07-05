package log

import (
	"fmt"
	"io"
	"os"
	"runtime"
	"time"
	"xtuer/config"

	rotatelogs "github.com/lestrrat-go/file-rotatelogs"
	"github.com/sirupsen/logrus"
)

/*
 使用案例:
 // Use the logger.
 log.Log.Info("Alice")

 // Fields 中输出具体的数据，Msg 中说明中干啥。
 log.Log.WithFields(logrus.Fields{
   "path":  "/root/x.sh",
   "dstIP": "192.168.12.101",
 }).Warn("执行脚本")
*/

// Logrus 的 Log 对象。
var Log *logrus.Logger

// init 初始化 Logrus 日志对象。
func init() {
	// Create a new logger.
	Log = logrus.New()
	Log.SetReportCaller(true)
	Log.SetLevel(logrus.DebugLevel)

	// 输出 JSON 格式的日志。
	Log.SetFormatter(&logrus.JSONFormatter{
		DisableHTMLEscape: true,
		CallerPrettyfier: func(f *runtime.Frame) (string, string) {
			return fmt.Sprintf("%s():%d", f.Function, f.Line), ""
		},
		TimestampFormat: "2006-01-02 15:04:05",
	})

	// Create a rotating log file with a daily rolling format.
	conf := config.GetAppConfig()
	historyLogPattern := fmt.Sprintf("%s/%s-%%Y%%m%%d.log", conf.LogDir, conf.LogName) // conf.LogDir+"/agent-%Y%m%d.log"
	currentLogPath := fmt.Sprintf("%s/%s.log", conf.LogDir, conf.LogName)              // conf.LogDir+"/agent.log"

	logWriter, err := rotatelogs.New(
		historyLogPattern,
		rotatelogs.WithLinkName(currentLogPath),
		rotatelogs.WithMaxAge(time.Duration(24*30)*time.Hour), // 日志保留 30 天
	)
	if err != nil {
		Log.SetOutput(os.Stdout)
	} else {
		// Use the rotating log file as the output destination, also print to the console.
		Log.SetOutput(io.MultiWriter(logWriter, os.Stdout))
	}
}
