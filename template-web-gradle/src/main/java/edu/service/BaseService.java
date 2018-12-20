package edu.service;

import edu.bean.User;
import edu.util.SecurityUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@Accessors(chain = true)
public class BaseService {
    @Autowired
    protected IdWorker idWorker;

    @Autowired
    private RedisDao redisDao;

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

    /**
     * 是否当前登录的用户
     *
     * @param userId 用户 ID
     * @return 如果是当前登录用户返回 true，否则返回 false
     */
    public boolean isCurrentUser(long userId) {
        return this.getLoginUserId() == userId;
    }
}
