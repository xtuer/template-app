package controller

import (
	"fmt"
	"net/http"
	"newdtagent/bean"
	"newdtagent/log"
	"newdtagent/utils"

	"github.com/gin-gonic/gin"
	"github.com/sirupsen/logrus"
)

// RequestHandlerFunc 为 controller 处理请求的函数签名。
type RequestHandlerFunc = func(c *gin.Context) bean.Response

// R 封装 controller 请求处理的返回值 Response 到 gin.Context 里，统一处理错误信息。
func R(doRequestFn RequestHandlerFunc) gin.HandlerFunc {
	return func(c *gin.Context) {
		// 执行请求。
		r := doRequestFn(c)

		if !r.Success {
			// 错误发生的时候，输出错误信息到日志里。
			url := c.Request.RequestURI
			log.Log.WithFields(logrus.Fields{
				"url":     url,
				"errMsg":  r.Msg,
				"errCode": r.Code,
			}).Error("[请求错误]")

			// 错误发生时，不继续后面可能存在的处理逻辑。
			c.AbortWithStatusJSON(http.StatusOK, r)
		} else {
			c.IndentedJSON(http.StatusOK, r)
		}
	}
}

// ErrorResponseWithMessage 设置处理失败的响应数据。
// 示例:
// - 参数绑定失败: ErrorResponseWithMessage(err, http.StatusBadRequest)
// - 查找不到对象: ErrorResponseWithMessage(err, http.StatusNotFound)
// - 状态码默认值: ErrorResponseWithMessage(err)
func ErrorResponseWithMessage(reason interface{}, code ...int) bean.Response {
	// 错误码默认为 1，如果指定了错误码则使用指定的。
	var c int = 1
	if len(code) == 1 {
		c = code[0]
	}

	return bean.Response{
		Code:    c,
		Success: false,
		Msg:     fmt.Sprintf("ErrId: %s, %v", utils.Uid(), reason), // 错误发生的时候生成一个唯一的 ID 方便查找问题
	}
}

// ErrorResponseWithMessageAndData 设置处理失败的消息和数据。
func ErrorResponseWithMessageAndData(reason interface{}, data interface{}, code ...int) bean.Response {
	// 错误码默认为 1，如果指定了错误码则使用指定的。
	var c int = 1
	if len(code) == 1 {
		c = code[0]
	}

	return bean.Response{
		Code:    c,
		Success: false,
		Msg:     fmt.Sprintf("ErrId: %s, %v", utils.Uid(), reason), // 错误发生的时候生成一个唯一的 ID 方便查找问题
		Data:    data,
	}
}

// OkResponseWithData 设置处理成功的响应数据。
func OkResponseWithData(data interface{}) bean.Response {
	return bean.Response{
		Code:    0,
		Success: true,
		Data:    data,
	}
}

// OkResponseWithMessage 设置处理成功的响应数据。
func OkResponseWithMessage(msg string) bean.Response {
	return bean.Response{
		Code:    0,
		Success: true,
		Msg:     msg,
	}
}

// OkResponseWithMessageAndData 设置处理成功的消息和数据。
func OkResponseWithMessageAndData(msg string, data interface{}) bean.Response {
	return bean.Response{
		Code:    0,
		Success: true,
		Msg:     msg,
		Data:    data,
	}
}
