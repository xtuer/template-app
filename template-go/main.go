package main

import (
	"newdtagent/controller"
	"newdtagent/service"

	"github.com/gin-gonic/gin"
)

func main() {
	router := gin.Default()

	// 路由注册。
	controller.NewZooController().RegisterRoutes(router)
	controller.NewExecuteController(service.NewExecuteService()).RegisterRoutes(router)

	router.Run(":8080")
}
