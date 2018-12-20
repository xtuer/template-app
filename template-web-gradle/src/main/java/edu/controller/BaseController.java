package edu.controller;

import edu.bean.User;
import edu.service.FileService;
import edu.service.IdWorker;
import edu.util.SecurityUtils;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 系统中有大量的访问学校、登录用户、生成 ID 等操作，如果每个控制器都各自的注入相关对象进行访问就比较麻烦，
 * 于是在基础控制器 BaseController 提供这些操作，其他控制器继承 BaseController，就可以省去不少工作量了。
 */
@Getter
public class BaseController {
    @Autowired
    private FileService fileService;

    @Autowired
    private IdWorker idWorker;

    /**
     * 获取当前登录用户
     *
     * @return 登录用户
     */
    public User getLoginUser() {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return 登录用户 ID，不存在则返回 0
     */
    public long getLoginUserId() {
        User user = SecurityUtils.getLoginUser();
        return user == null ? 0 : user.getId();
    }

    /**
     * 获取当前域名对应的学校 ID
     *
     * @return 学校的 ID
     */
    public long getSchoolId() {
        return 0;
    }

    /**
     * 生成唯一的 64 位 long 的 ID
     *
     * @return 返回唯一 ID
     */
    public long generateId() {
        return idWorker.nextId();
    }
}
