package com.xtuer.service;

import com.github.wujun234.uid.impl.CachedUidGenerator;
import com.xtuer.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseService {
    @Autowired
    private CachedUidGenerator uidGenerator;

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
    final public long nextId() {
        return uidGenerator.getUID();
    }
}
