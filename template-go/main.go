package main

import (
	"fmt"
	"math/rand"
	"net/http"
	"newdtagent/bean"
	"newdtagent/controller"
	"newdtagent/service"
	"time"

	"github.com/gin-gonic/gin"
)

func main() {
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

	// 路由注册。
	controller.NewZooController().RegisterRoutes(router)
	controller.NewExecuteController(service.NewExecuteService()).RegisterRoutes(router)

	router.Run(":8080")
}
