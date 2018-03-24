package ebag.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 表示地区的类，例如省、市、区
 */

@Getter
@Setter
@Accessors(chain = true)
public class Region {
    private Integer id;
    private String  name;
    private Integer level;    // 省的 level 为 1
    private Integer parentId; // 省的 parentId 为 0
}
