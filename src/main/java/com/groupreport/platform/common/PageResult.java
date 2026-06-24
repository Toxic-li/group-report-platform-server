package com.groupreport.platform.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long current;

    /** 每页条数 */
    private long size;

    /** 总页数 */
    private long pages;

    /** 是否有上一页 */
    private boolean hasPrevious;

    /** 是否有下一页 */
    private boolean hasNext;

    public PageResult() {}

    public PageResult(Page<T> page) {
        this.records = page.getRecords();
        this.total = page.getTotal();
        this.current = page.getCurrent();
        this.size = page.getSize();
        this.pages = page.getPages();
        this.hasPrevious = page.hasPrevious();
        this.hasNext = page.hasNext();
    }

    public PageResult(List<T> records, long total, long current, long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;
        this.hasPrevious = current > 1;
        this.hasNext = current < this.pages;
    }

    /**
     * 从MyBatis Plus的Page对象创建
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page);
    }
}
