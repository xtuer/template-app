package bean

// Result 为请求结果的统一响应对象。
type Result struct {
	Id      string `json:"id,omitempty"`   // 响应的唯一 ID，一般错误发生的时候才使用
	Code    int    `json:"code"`           // 业务逻辑相关的 code，不是 HTTP Status Code
	Success bool   `json:"success"`        // 业务逻辑处理成功时为 true，错误时为 false
	Msg     string `json:"msg"`            // 请求的描述
	Data    any    `json:"data,omitempty"` // 请求的 payload
}
