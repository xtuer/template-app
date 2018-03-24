package ebag.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 地址类，信息由省市县以及街道地址组成
 */
@Getter
@Setter
@Accessors(chain = true)
public class Address {
    private Long   id;
    private Long   provinceId; // 省的 ID
    private Long   cityId;     // 市的 ID
    private Long   countyId;   // 县的 ID
    private String province;   // 省的名字
    private String city;       // 市的名字
    private String county;     // 县的名字
    private String district;   // 街道地址
    private Long   userId;     // 地址所属用户的 ID
}
