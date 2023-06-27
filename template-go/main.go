package main

import (
	"fmt"
	"math/rand"
	"net/http"
	"newdtagent/bean"
	"newdtagent/config"
	"newdtagent/controller"
	"newdtagent/utils"
	"os"
	"time"

	"github.com/gin-gonic/gin"
)

func main() {
	// 如果端口已经被使用，则不允许启动。
	port := config.GetAppConfig().Port
	if utils.IsPortUsed(port) {
		fmt.Printf("端口 [%d] 已被占用，启动失败\n", port)
		os.Exit(100)
	}

	// 保存 PID。
	utils.SavePid(config.GetAppConfig().PidPath)

	// 系统随机数种子。
	rand.Seed(time.Now().UnixNano())

	// router := gin.Default()
	// Creates a router without any middleware by default
	router := gin.New()

	// Global middleware
	// Logger middleware will write the logs to gin.DefaultWriter even if you set with GIN_MODE=release.
	// By default gin.DefaultWriter = os.Stdout
	router.Use(gin.Logger())

	// router.Use(ginlogrus.Logger(log.Log), gin.Recovery())

	// Recovery middleware recovers from any panics and writes a 500 if there was one.
	router.Use(gin.CustomRecovery(func(c *gin.Context, recovered interface{}) {
		c.AbortWithStatusJSON(http.StatusInternalServerError, bean.Response{
			Success: false,
			Code:    http.StatusInternalServerError,
			Msg:     fmt.Sprintf("%v", recovered),
		})
	}))

	// 验证签名。
	// router.Use(security.AuthenticateUsingSign)

	// 路由注册。
	controller.NewZooController().RegisterRoutes(router)

	// 启动服务。
	addr := fmt.Sprintf(":%d", config.GetAppConfig().Port)
	router.Run(addr)
}
