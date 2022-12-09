package bean

import (
	"fmt"
	"testing"
)

func TestJobStats(t *testing.T) {
	runningJob := NewJob()
	runningJob.State = JSRunning

	store := NewJobStore()
	store.AddJob(NewJob())
	store.AddJob(runningJob)

	fmt.Println(store.JobCount())
	fmt.Println(store.RunningJobCount())

	fmt.Println(*runningJob)
}
