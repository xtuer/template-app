package com.edu.training.mapper;

import com.edu.training.bean.Organization;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 机构的 Mapper
 */
public interface OrganizationMapper {
    /**
     * 使用 ID 查询机构
     *
     * @param id 机构 ID
     * @return 返回查询到的机构，查询不到返回 null
     */
    Organization findOrganizationById(long id);

    /**
     * 使用域名查询机构，每个机构有独立的域名
     *
     * @param host 域名
     * @return 返回查询到的机构，查询不到返回 null
     */
    Organization findOrganizationByHost(String host);

    /**
     * 使用机构名称模糊查询查询机构
     *
     * @param name 机构名 (部分)
     * @return 机构数组
     */
    List<Organization> findOrganizationsLikeName(@Param("name") String name);

    /**
     * 判断指定 ID 的机构是否可使用此域名
     *
     * @param organizationId 机构 ID
     * @param host           机构域名
     * @return 可使用返回 true，不可使用返回 false
     */
    boolean isHostAvailable(long organizationId, String host);

    /**
     * 新增或更新机构
     *
     * @param organization 机构
     */
    void insertOrUpdateOrganization(Organization organization);

    /**
     * 启用禁用机构
     *
     * @param id      机构 ID
     * @param enabled 是否启用
     */
    void enableOrganization(long id, boolean enabled);

    /**
     * 使用机构 ID 删除机构
     *
     * @param id 机构 ID
     */
    void deleteOrganization(long id);
}
