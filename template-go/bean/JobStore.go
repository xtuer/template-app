package bean

// JobStore 保存了所有的任务信息。
type JobStore struct {
	jobs []*Job
}

// NewJobStore 创建 JobStore 对象。
func NewJobStore() *JobStore {
	return &JobStore{jobs: make([]*Job, 0)}
}

// JobCount 返回任务数量。
func (o JobStore) JobCount() int {
	return len(o.jobs)
}

// RunningJobCount 返回正在执行的任务数量。
func (o JobStore) RunningJobCount() int {
	count := 0

	for _, job := range o.jobs {
		if job.State == JSRunning {
			count++
		}
	}

	return count
}

// AddJob 添加一个 Job 到 JobStore 里。
func (o *JobStore) AddJob(job *Job) {
	if job != nil {
		o.jobs = append(o.jobs, job)
	}
}

// FindJobById 查找 ID 为传入参数 jobId 的任务。
func (o *JobStore) FindJobById(jobId string) *Job {
	for _, job := range o.jobs {
		if job.Id == jobId {
			return job
		}
	}

	return nil
}
