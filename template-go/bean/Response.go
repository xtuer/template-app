package bean

// Response 为请求结果的统一响应对象。
type Response struct {
	Code    int         `json:"code"`           // 业务逻辑相关的 code，不是 HTTP Status Code
	Success bool        `json:"success"`        // 业务逻辑处理成功时为 true，错误时为 false
	Msg     string      `json:"msg"`            // 请求的描述
	Data    interface{} `json:"data,omitempty"` // 请求的 payload
}
