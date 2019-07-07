package edu.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class BaseService {
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisDao redisDao;

    /**
     * 生成唯一的 64 位 long 的 ID
     *
     * @return 返回唯一 ID
     */
    public long nextId() {
        return idWorker.nextId();
    }
}
