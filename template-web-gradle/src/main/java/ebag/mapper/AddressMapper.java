package ebag.mapper;

import ebag.bean.Address;
import ebag.bean.Region;

import java.util.List;

/**
 * 普通的 Mapper，主要负责一些简单功能的数据库访问
 */
public interface AddressMapper {
    /**
     * 查找指定 level 的地区
     *
     * @param level 地区的 level，省的 level 为 1
     * @return 地区的列表
     */
    List<Region> findRegionsByLevel(int level);

    /**
     * 查找 parentId 为传入的 parentId 的地区
     *
     * @param parentId 地区的 parentId
     * @return 地区的列表
     */
    List<Region> findRegionsByParentId(int parentId);

    /**
     * 使用 ID 查找地址
     *
     * @param addressId 地址的 ID
     * @return 返回地址
     */
    Address findAddressById(long addressId);

    /**
     * 查找用户的地址，一个用户可以有多个地址
     *
     * @param userId 用户 ID
     * @return 地址的列表
     */
    List<Address> findAddressesByUserId(long userId);

    /**
     * 插入或者更新地址: 地址 ID 不存在时进行插入，存在时更新地址
     *
     * @param address 地址
     */
    void insertOrUpdateAddress(Address address);
}
