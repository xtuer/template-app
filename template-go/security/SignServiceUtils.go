package security

import (
	"fmt"
	"newdtagent/config"
	"newdtagent/utils"
	"time"
)

// Sign 计算签名。
//
// @param signAt 签名的时间，单位为秒。
// @return 返回签名字符串。
func Sign(signAt int64) string {
	salt := config.GetAppConfig().SignSalt
	secret := config.GetAppConfig().SignSecret

	return utils.Sign(secret, salt, signAt)
}

// CheckSign 检查签名是否有效。
//
// @param sign 签名。
// @param signAt 签名的时间，单位为秒。
// @param validDuration 签名的有效期。
func CheckSign(sign string, signAt int64, validDuration time.Duration) bool {
	/*
	 签名有效判断逻辑:
	 1. 验证签名的时效性: 签名时间 + 签名有效期在当前时间之前则签名无效。
	 2. 使用 secret 和 signAt 计算得到签名 newSign。
	 3. 如果 newSign 和传入的签名 sign 相等则签名有效。
	*/

	// [1] 验证签名的时效性: 签名时间 + 签名有效期在当前时间之前则签名无效。
	validUntil := signAt + int64(validDuration.Seconds())
	if validUntil < time.Now().Unix() {
		return false
	}

	// [2] 使用 secret 和 signAt 计算得到签名 newSign。
	newSign := Sign(signAt)

	// [3] 如果 newSign 和传入的签名 sign 相等则签名有效。
	return newSign == sign
}

// SignHeaders 创建认证需要的 headers。
func SignHeaders() map[string]interface{} {
	signAt := time.Now().Unix()
	headers := map[string]interface{}{
		Param_Sign:   Sign(signAt),
		Param_SignAt: fmt.Sprintf("%d", signAt),
	}

	return headers
}
