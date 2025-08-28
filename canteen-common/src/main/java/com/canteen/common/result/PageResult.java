package com.canteen.common.result;

import lombok.Data;

import java.util.List;

/**
 * 分页返回结果
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    private Long total;
    private List<T> records;
    private Long current;
    private Long size;
    private Long pages;

    public PageResult() {}

    public PageResult(Long total, List<T> records, Long current, Long size) {
        this.total = total;
        this.records = records;
        this.current = current;
        this.size = size;
        this.pages = (total + size - 1) / size;
    }

    /**
     * 构建分页结果
     * @param total 总记录数
     * @param records 当前页数据
     * @param current 当前页码
     * @param size 每页大小
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> records, Long current, Long size) {
        return new PageResult<>(total, records, current, size);
    }
}