package utils

import (
	"github.com/google/uuid"
)

// Uid 生成唯一 ID。
func Uid() string {
	return uuid.NewString()
}
