package edu.mapper;

import edu.bean.Organization;

/**
 * 机构的 Mapper
 */
public interface OrganizationMapper {
    /**
     * 使用域名查询机构，每个机构有独立的域名
     *
     * @param host 域名
     * @return 返回查询到的机构，查询不到返回 null
     */
    Organization findOrganizationByHost(String host);
}
