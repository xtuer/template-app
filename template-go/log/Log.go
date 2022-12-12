package log

import (
	"fmt"
	"io"
	"os"
	"runtime"
	"time"

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

var Log *logrus.Logger = logrus.New()

// init 初始化 Logrus 日志对象。
func init() {
	// Create a new logger.
	// Log := logrus.New()
	Log.SetReportCaller(true)
	Log.SetLevel(logrus.DebugLevel)

	// 输出 JSON 格式的日子
	Log.SetFormatter(&logrus.JSONFormatter{
		CallerPrettyfier: func(f *runtime.Frame) (string, string) {
			return fmt.Sprintf("%s():%d", f.Function, f.Line), ""
		},
	})

	// Create a rotating log file with a daily rolling format.
	logWriter, err := rotatelogs.New(
		"logs/app-%Y%m%d.log",
		rotatelogs.WithLinkName("logs/app.log"),
		rotatelogs.WithMaxAge(time.Duration(24)*time.Hour),
	)
	if err != nil {
		Log.SetOutput(os.Stdout)
	} else {
		// Use the rotating log file as the output destination, also print to the console.
		Log.SetOutput(io.MultiWriter(logWriter, os.Stdout))
	}
}
