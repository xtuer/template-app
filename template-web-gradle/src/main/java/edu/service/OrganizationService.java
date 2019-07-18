package edu.service;

import com.alicp.jetcache.anno.Cached;
import edu.bean.CacheConst;
import edu.bean.Organization;
import edu.bean.RedisKey;
import edu.mapper.OrganizationMapper;
import edu.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 机构的服务
 */
@Service
public class OrganizationService extends BaseService {
    @Autowired
    private OrganizationMapper orgMapper;

    /**
     * 获取当前请求使用的域名所属机构
     *
     * @return 返回域名所属的机构，如果找不到则返回 null
     */
    public Organization getCurrentOrganization() {
        // 先从缓存里查找机构，如果缓存里没有，再从数据库加载
        String host = WebUtils.getHost();
        Organization org = findOrganizationByHost(host);

        return org;
    }

    /**
     * 获取当前域名对应的机构 ID
     *
     * @return 组织 ID
     */
    public long getCurrentOrganizationId() {
        // 如果没有找到域名对应的机构，则返回 1，表明是系统管理员的机构
        Organization org = getCurrentOrganization();

        return org != null ? org.getId() : 1;
    }

    /**
     * 查找域名所属的机构
     * 先从缓存里查找机构，如果缓存里没有，再从数据库加载
     *
     * @param host 机构的域名
     * @return 返回域名所属机构
     */
    @Cached(name = CacheConst.NAME_ORG, key = CacheConst.KEY_ORG)
    public Organization findOrganizationByHost(String host) {
        return orgMapper.findOrganizationByHost(host);
    }
}
