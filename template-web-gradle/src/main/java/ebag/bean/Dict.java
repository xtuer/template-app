package ebag.bean;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 字典指常用、很少修改的数据，例如年级，学段，默认的班级名字、各种术语等。
 * 字典根据类型进行管理，同一个类型下可能有多个字典数据，例如学段就包含小学、初中、高中、大学等。
 */
@Getter
@Setter
@Accessors(chain = true)
public class Dict {
    private Long   id;    // 字典的 ID

    @Excel(name = "编码")
    private String code;  // 字典的编码

    @Excel(name = "值")
    private String value; // 字典的值

    @Excel(name = "分类")
    private String type;  // 字典的类型

    @Excel(name = "描述")
    private String description; // 字典的描述
}
