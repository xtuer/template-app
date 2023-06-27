package bean

import (
	"encoding/json"
	"fmt"
	"time"
)

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

func main() {
	t1 := CTime(time.Now())
	fmt.Println(t1)

	j, _ := json.Marshal(t1)
	fmt.Println(string(j))

	var t2 CTime
	json.Unmarshal(j, &t2)

	// 时间戳差 8 个小时
	fmt.Println(time.Time(t2))
}
