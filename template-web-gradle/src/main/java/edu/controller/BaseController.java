package edu.controller;

import edu.bean.User;
import edu.service.FileService;
import edu.service.IdWorker;
import edu.util.SecurityUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 系统中有大量的访问组织、登录用户、生成 ID 等操作，如果每个控制器都各自的注入相关对象进行访问就比较麻烦，
 * 于是在基础控制器 BaseController 提供这些操作，其他控制器继承 BaseController，就可以省去不少工作量了。
 */
@Getter
public class BaseController {
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private FileService fileService;

    /**
     * 获取当前登录用户
     *
     * @return 登录用户
     */
    protected User getLoginUser() {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return 登录用户 ID，不存在则返回 0
     */
    protected long getLoginUserId() {
        User user = SecurityUtils.getLoginUser();
        return user == null ? 0 : user.getId();
    }

    /**
     * 获取当前域名对应的组织 ID
     *
     * @return 组织 ID
     */
    protected long getOrgId() {
        // TODO: 如果没有找到域名对应的组织，则返回 1，表明是系统管理员的机构
        return 1;
    }

    /**
     * 生成唯一的 64 位 long 的 ID
     *
     * @return 返回唯一 ID
     */
    protected long nextId() {
        return idWorker.nextId();
    }
}
