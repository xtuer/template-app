package utils

import (
	"fmt"
	"testing"
)

func TestUid(t *testing.T) {
	fmt.Println(Uid())
}

func TestMd5(t *testing.T) {
	fmt.Println(Md5("Hello"))
}

func TestSaveScriptToTempFile(t *testing.T) {
	path, _ := SaveScriptToTempFile("Test only content")
	fmt.Println(path)
}
