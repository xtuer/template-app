package bean

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
)

// Result 为请求结果的统一响应对象。
type Result struct {
	Code    int    `json:"code"`           // 业务逻辑相关的 code，不是 HTTP Status Code
	Success bool   `json:"success"`        // 业务逻辑处理成功时为 true，错误时为 false
	Msg     string `json:"msg"`            // 请求的描述
	Data    any    `json:"data,omitempty"` // 请求的 payload
}

// FailResponseWithMessage 设置处理失败的响应数据。
func FailResponseWithMessage(c *gin.Context, reason any) {
	c.AbortWithStatusJSON(http.StatusOK, Result{
		Success: false,
		Msg:     fmt.Sprintf("%v", reason),
	})
}

// OkResponseWithData 设置处理成功的响应数据。
func OkResponseWithData(c *gin.Context, data any) {
	c.IndentedJSON(http.StatusOK, Result{
		Success: true,
		Data:    data,
	})
}

// OkResponseWithMessage 设置处理成功的响应数据。
func OkResponseWithMessage(c *gin.Context, msg string) {
	c.IndentedJSON(http.StatusOK, Result{
		Success: true,
		Msg:     msg,
	})
}
