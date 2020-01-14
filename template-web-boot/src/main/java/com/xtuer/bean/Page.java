package com.xtuer.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import com.xtuer.util.PageUtils;

/**
 * 分页使用的类
 */
@Getter
@Setter
@Accessors(chain = true)
public class Page {
    private int number; // 页码
    private int offset; // 起始位置
    private int size;   // 数量

    /**
     * 使用 pageNumber 和 pageSize 创建分页对象
     *
     * @param pageNumber // 页码，从 1 开始
     * @param pageSize   // 数量
     * @return 返回分页对象
     */
    public static Page of(int pageNumber, int pageSize) {
        Page page = new Page();
        int offset = PageUtils.offset(pageNumber, pageSize);
        page.setOffset(offset).setSize(pageSize);

        return page;
    }
}
