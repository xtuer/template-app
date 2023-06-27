package bean

import (
	"fmt"
	"strings"
)

/**
 * 替换字符串中对应的占位符。
 * 占位符的格式为 ${var}，例如 "Hello ${name}"
 *
 * @param params Map 的 key 为 src 中的占位符名称，value 为用于替换的值。
 * @return 返回替换后得到的字符串
 */
type String string

func (s String) ReplacePlaceholders(params map[string]interface{}) string {
	result := string(s)

	for k, v := range params {
		value := fmt.Sprintf("%v", v)
		result = strings.ReplaceAll(result, "${"+k+"}", value)
	}

	return result
}
