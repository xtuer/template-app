package bean

import (
	"fmt"
	"time"
)

// 使用: t1 := CTime(time.Now())
type CTime time.Time

func (o CTime) MarshalJSON() ([]byte, error) {
	formatted := fmt.Sprintf(`"%s"`, time.Time(o).Format("2006-01-02 15:04:05"))
	return []byte(formatted), nil
}

func (o *CTime) UnmarshalJSON(b []byte) error {
	t, err := time.Parse("2006-01-02 15:04:05", string(b[1:len(b)-1]))

	if err != nil {
		return err
	}

	*o = CTime(t)
	return nil
}
