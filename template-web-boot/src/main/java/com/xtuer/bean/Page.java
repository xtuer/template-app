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
    private int pageNumber = 1 ; // 页码
    private int pageSize   = 10; // 每页数量
    private int offset     = 0 ; // 起始位置

    public int getOffset() {
        return PageUtils.offset(pageNumber, pageSize);
    }

    /**
     * 作用同 pageSize，在 Mapper xml 中使用 size 比 pageSize 更好看一些
     *
     * @return 返回数量
     */
    public int getSize() {
        return this.getPageSize();
    }

    /**
     * 作用同 pageSize，在 Mapper xml 中使用 count 比 pageSize 更好看一些
     *
     * @return 返回数量
     */
    public int getCount() {
        return this.getPageSize();
    }

    public Page setPageNumber(int pageNumber) {
        this.pageNumber = Math.max(1, pageNumber);
        return this;
    }

    public Page setPageSize(int pageSize) {
        this.pageSize = Math.max(1, pageSize);
        return this;
    }

    /**
     * 使用 pageNumber 和 pageSize 创建分页对象
     *
     * @param pageNumber // 页码，从 1 开始
     * @param pageSize   // 数量
     * @return 返回分页对象
     */
    public static Page of(int pageNumber, int pageSize) {
        int offset = PageUtils.offset(pageNumber, pageSize);
        Page page  = new Page();
        page.setOffset(offset).setPageNumber(pageNumber).setPageSize(pageSize);

        return page;
    }
}
