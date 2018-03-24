package ebag.mapper;

import ebag.bean.Dict;

import java.util.List;

public interface DictMapper {
    /**
     * 查找指定类型下的字典，如果 type 为空或者 null，则查找所有的字典
     *
     * @param type   字典分类
     * @param offset 起始位置
     * @param count  查找数量
     * @return 返回字典的列表
     */
    List<Dict> findDictsByType(String type, int offset, int count);

    /**
     * 查找的字典所有分类
     *
     * @return 分类的列表
     */
    List<String> findDictTypes();

    /**
     * 插入或者更新已经存在的字典，code+type 唯一标记一个字典数据
     *
     * @param dict 字典对象
     */
    void insertOrUpdateDict(Dict dict);
}
