package com.xtuer.controller;

import com.github.wujun234.uid.impl.CachedUidGenerator;
import com.xtuer.bean.User;
import com.xtuer.service.OrganizationService;
import com.xtuer.service.RepoFileService;
import com.xtuer.service.TempFileService;
import com.xtuer.service.UserService;
import com.xtuer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 系统中有大量的访问组织、登录用户、生成 ID 等操作，如果每个控制器都各自的注入相关对象进行访问就比较麻烦，
 * 于是在基础控制器 BaseController 提供这些操作，其他控制器继承 BaseController，就可以省去不少工作量了。
 */
public class BaseController {
    @Autowired
    protected UserService userService;

    @Autowired
    protected OrganizationService orgService;

    @Autowired
    protected TempFileService tempFileService;

    @Autowired
    protected RepoFileService repoFileService;

    @Autowired
    private CachedUidGenerator uidGenerator;

    /**
     * 获取当前登录用户
     *
     * @return 返回登录用户，如果没有登录返回 null
     */
    final protected User getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return 返回登录用户 ID，如果没有登录返回 0
     */
    final protected long getCurrentUserId() {
        // 从 Security context 中获取登录的用户，此用户信息是从 token 里解析出来的，只有用户的关键信息
        User user = SecurityUtils.getCurrentUser();
        return user == null ? 0 : user.getUserId();
    }

    /**
     * 获取当前域名对应的机构 ID
     *
     * @return 组织 ID
     */
    final protected long getCurrentOrganizationId() {
        return orgService.getCurrentOrganizationId();
    }

    /**
     * 生成唯一的 64 位 long 的 ID
     *
     * @return 返回唯一 ID
     */
    final protected long nextId() {
        return uidGenerator.getUID();
    }
}
