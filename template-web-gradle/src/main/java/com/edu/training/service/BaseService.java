package com.edu.training.service;

import com.edu.training.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseService {
    @Autowired
    protected IdWorker idWorker;

    @Autowired
    protected UserService userService;

    @Autowired
    protected TempFileService tempFileService;

    @Autowired
    protected RepoFileService repoFileService;

    @Autowired
    protected AppConfig config;

    /**
     * 生成唯一的 64 位 long 的 ID
     *
     * @return 返回唯一 ID
     */
    public long nextId() {
        return idWorker.nextId();
    }
}
