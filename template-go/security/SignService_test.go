package security

import (
	"fmt"
	"testing"
	"time"
)

// 测试生成签名。
func TestSign(t *testing.T) {
	signedAt := time.Now().Unix()
	fmt.Printf("signed at: %d\n", signedAt)
	fmt.Printf("sign: %s\n", Sign(signedAt))
}

// 测试签名是否有效。
func TestCheckSign(t *testing.T) {
	signedAt := int64(1671074602)
	sign := "671a004660fc671797102fd65f0be0bf"
	valid := CheckSign(sign, signedAt, 5*time.Minute)

	fmt.Println("sign valid:", valid)
}
