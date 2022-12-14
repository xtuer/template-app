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
type RequestHandlerFunc = func(c *gin.Context) *bean.Result

// R 封装 controller 请求处理的返回值 Result 到 gin.Context 里，统一处理错误信息。
func R(doRequestFn RequestHandlerFunc) gin.HandlerFunc {
	return func(c *gin.Context) {
		// 执行请求。
		r := doRequestFn(c)

		// 错误发生的时候，输出错误信息到日志里。
		if !r.Success {
			url := c.Request.RequestURI
			log.Log.WithFields(logrus.Fields{
				"url":     url,
				"errId":   r.Id,
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

// FailResultWithMessage 设置处理失败的响应数据。
// 示例:
// - 参数绑定失败: FailResultWithMessage(err, http.StatusBadRequest)
// - 查找不到对象: FailResultWithMessage(err, http.StatusNotFound)
// - 状态码默认值: FailResultWithMessage(err)
func FailResultWithMessage(reason any, code ...int) *bean.Result {
	// 错误码默认为 1，如果指定了错误码则使用指定的。
	var c int = 1
	if len(code) == 1 {
		c = code[0]
	}

	return &bean.Result{
		Id:      utils.Uid(),
		Code:    c,
		Success: false,
		Msg:     fmt.Sprintf("%v", reason),
	}
}

// OkResultWithData 设置处理成功的响应数据。
func OkResultWithData(data any) *bean.Result {
	return &bean.Result{
		Code:    0,
		Success: true,
		Data:    data,
	}
}

// OkResultWithMessage 设置处理成功的响应数据。
func OkResultWithMessage(msg string) *bean.Result {
	return &bean.Result{
		Code:    0,
		Success: true,
		Msg:     msg,
	}
}

// OkResultWithMessageAndData 设置处理成功的消息和数据。
func OkResultWithMessageAndData(msg string, data any) *bean.Result {
	return &bean.Result{
		Code:    0,
		Success: true,
		Msg:     msg,
		Data:    data,
	}
}
