package security

import (
	"net/http"
	"newdtagent/bean"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"
)

const (
	Param_Sign   string = "sign"
	Param_SignAt string = "signAt"
)

// URI 的白名单，不需要鉴权验证。
var uriWhiteList map[string]struct{} = map[string]struct{}{
	"/api/test":     {},
	"/api/memStats": {},
	"/api/stats":    {},
	"/api/ping":     {},
}

// AuthenticateUsingSign 验证签名，验证通过后才能继续访问。
func AuthenticateUsingSign(c *gin.Context) {
	/**
	签名验证逻辑:
	1. 获取请求的路径。
	2. 如果请求路径在白名单中，不需要鉴权验证。
	3. 获取签名的 sign 和 signAt 信息:
	   3.1 如果能从 URL Query 中获取签名信息则从 URL Query 中获取。
	   3.2 如果能从 Header 中获取签名信息则从 Header 中获取。
	   3.3 如果获取不到签名信息则终止请求。
	4. 验证签名，签名无效者终止请求。
	5. 验证通过，请求继续。
	*/

	// [1] 获取请求的路径。
	// /api/jobs/test   <-- router.GET("/api/jobs/test", func)
	// /api/jobs/:jobId <-- router.GET("/api/jobs/:jobId", func)
	requestPath := c.FullPath()

	// url 有 GET 请求， 但没有 POST 请求。
	// 使用 POST 请求访问此 url 时 requestPath 为空。
	if requestPath == "" {
		AbortWhenUnauthorized(c, c.Request.Method+" 请求不支持")
		return
	}

	// [2] 如果请求路径在白名单中，不需要鉴权验证。
	if _, ok := uriWhiteList[requestPath]; ok {
		return
	}

	var sign string
	var signAt int64

	// [3] 获取签名的 sign 和 signAt 信息
	if c.Query(Param_Sign) != "" {
		// [3.1] 3.1 如果能从 URL Query 中获取签名信息则从 URL Query 中获取。
		sign = c.Query(Param_Sign)
		if v, err := strconv.Atoi(c.Query(Param_SignAt)); err == nil {
			signAt = int64(v)
		}
	} else if c.GetHeader(Param_Sign) != "" {
		// [3.2] 如果能从 Header 中获取签名信息则从 Header 中获取。
		sign = c.GetHeader(Param_Sign)
		if v, err := strconv.Atoi(c.GetHeader(Param_SignAt)); err == nil {
			signAt = int64(v)
		}
	} else {
		// [3.3] 如果获取不到签名信息则终止请求。
		AbortWhenUnauthorized(c)
		return
	}

	// [4] 验证签名，签名无效者终止请求。
	if !CheckSign(sign, signAt, 30*time.Minute) {
		AbortWhenUnauthorized(c, "签名无效")
		return
	}

	// [5] 验证通过，请求继续。
	c.Next()
}

// AbortWhenUnauthorized 鉴权不通过的响应。
func AbortWhenUnauthorized(c *gin.Context, reasons ...string) {
	reason := "无权访问"
	if len(reasons) > 0 {
		reason = reasons[0]
	}
	c.AbortWithStatusJSON(http.StatusUnauthorized, bean.Response{
		Code:    http.StatusUnauthorized,
		Success: false,
		Msg:     reason,
	})
}
