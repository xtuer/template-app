package com.xtuer.service;

import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.Cached;
import com.xtuer.bean.*;
import com.xtuer.mapper.OrganizationMapper;
import com.xtuer.util.Utils;
import com.xtuer.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 机构的服务 (机构会使用域名作为 key 缓存到 Redis 中)
 */
@Service
public class OrganizationService extends BaseService {
    @Autowired
    private OrganizationMapper orgMapper;

    @Autowired
    private OrganizationService self;

    /**
     * 获取当前请求使用的域名所属机构
     *
     * @return 返回域名所属的机构，如果找不到则返回 null
     */
    public Organization getCurrentOrganization() {
        // 先从缓存里查找机构，如果缓存里没有，再从数据库加载
        String host = WebUtils.getHost();
        Organization org = self.findOrganizationByHost(host);

        return org;
    }

    /**
     * 获取当前域名对应的机构 ID
     *
     * @return 组织 ID
     */
    public long getCurrentOrganizationId() {
        // 如果没有找到域名对应的机构，则返回 1，表明是系统管理员的机构
        Organization org = self.getCurrentOrganization();

        return org != null ? org.getOrgId() : 1;
    }

    /**
     * 使用机构 ID 查询机构，同时会查询出机构的管理员
     *
     * @param orgId 机构 ID
     * @return 返回机构
     */
    public Organization findOrganization(long orgId) {
        Organization org = orgMapper.findOrganizationById(orgId);

        if (org != null) {
            User admin = userService.findUser(org.getAdminId());
            org.setAdmin(admin);
        }

        return org;
    }

    /**
     * 查找域名所属的机构
     * 先从缓存里查找机构，如果缓存里没有，再从数据库加载
     *
     * @param host 机构的域名
     * @return 返回域名所属机构
     */
    @Cached(name = CacheConst.CACHE, key = CacheConst.KEY_ORG_HOST)
    public Organization findOrganizationByHost(String host) {
        return orgMapper.findOrganizationByHost(host);
    }

    /**
     * 新增或更新机构
     *
     * @param org 机构对象
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<String> upsertOrganization(Organization org){
        // 0. 去掉字符串两端的空格，并调整域名
        // 1. 检查机构的域名是否可用 (如果域名被其他机构使用了则不可使用)
        // 2. 移动机构的 Logo 到文件仓库
        // 3. 如果机构的 ID 为 0，则说明是新建机构，则为其分配 ID，否则为更新机构
        // 4. 如果管理员的 ID 为 0，则新建管理员，如果管理员已经存在，则不进行处理
        // 5. 插入或者更新机构
        // 6. 清楚机构的缓存

        // [0] 去掉字符串两端的空格
        org.setName(StringUtils.trim(org.getName()));
        org.setContactPerson(StringUtils.trim(org.getContactPerson()));
        org.setContactMobile(StringUtils.trim(org.getContactMobile()));
        org.setHost(WebUtils.simplifyHost(org.getHost()));
        org.setLogo(StringUtils.trim(org.getLogo()));
        org.getAdmin().setUsername(org.getAdmin().getUsername());

        // [1] 检查机构的域名是否可用 (如果域名被其他机构使用了则不可使用)
        if (!orgMapper.isHostAvailable(org.getOrgId(), org.getHost())) {
            return Result.fail("域名 " + org.getHost() + " 已经被使用");
        }

        // [2] 移动机构的 Logo 到文件仓库
        String logo = repoFileService.moveTempFileToRepo(org.getLogo());
        org.setLogo(logo);

        // [3] 如果机构的 ID 为 0，则说明是新建机构，则为其分配 ID，否则为更新机构
        if (Utils.isInvalidId(org.getOrgId())) {
            org.setOrgId(nextId());
        }

        // [4] 如果管理员的 ID 为 0，则新建管理员，如果管理员已经存在，则不进行处理
        User admin = org.getAdmin();
        if (Utils.isInvalidId(admin.getUserId())) {
            // 提示: 管理员的 username 不需要判断是否可以，因为他是本机构的第一个用户
            admin.setUserId(nextId());
            admin.addRole(Role.ROLE_ADMIN_ORG);
            admin.setNickname("机构管理员");
            admin.setOrgId(org.getOrgId());
            userService.createOrUpdateUser(admin); // 创建管理员
        }

        // [5] 插入或者更新机构
        org.setAdminId(admin.getUserId()); // 设置机构的管理员
        orgMapper.upsertOrganization(org);

        // [6] 清楚机构的缓存
        self.invalidateOrganizationCache(org.getHost());

        return Result.ok();
    }

    /**
     * 使用机构 ID 删除机构
     *
     * @param orgId 机构 ID
     */
    public void deleteOrganization(long orgId){
        Organization org = orgMapper.findOrganizationById(orgId);

        if (org != null) {
            orgMapper.deleteOrganization(orgId);
            self.invalidateOrganizationCache(org.getHost());
        }
    }

    /**
     * 启用禁用机构
     *
     * @param orgId   机构 ID
     * @param enabled 启用禁用的 boolean 值，为 true 启用机构，为 false 禁用机构
     */
    public void enableOrganization(long orgId, boolean enabled) {
        Organization org = orgMapper.findOrganizationById(orgId);

        if (org != null) {
            orgMapper.enableOrganization(orgId, enabled);
            self.invalidateOrganizationCache(org.getHost());
        }
    }

    /**
     * 删除机构的缓存
     *
     * @param host 机构 host
     */
    @CacheInvalidate(name = CacheConst.CACHE, key = CacheConst.KEY_ORG_HOST)
    public void invalidateOrganizationCache(String host) {

    }
}
