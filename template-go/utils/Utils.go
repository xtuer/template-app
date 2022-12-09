package utils

import (
	"fmt"
	"net/http"
	"newdtagent/bean"

	"github.com/gin-gonic/gin"
)

// FailWithMessage 设置处理失败的响应数据。
func FailWithMessage(c *gin.Context, reason any) {
	c.AbortWithStatusJSON(http.StatusOK, bean.Result{
		Success: false,
		Msg:     fmt.Sprintf("%v", reason),
	})
}

// OkWithData 设置处理成功的响应数据。
func OkWithData(c *gin.Context, data any) {
	c.IndentedJSON(http.StatusOK, bean.Result{
		Success: true,
		Data:    data,
	})
}

// OkWithMessage 设置处理成功的响应数据。
func OkWithMessage(c *gin.Context, msg string) {
	c.IndentedJSON(http.StatusOK, bean.Result{
		Success: true,
		Msg:     msg,
	})
}
