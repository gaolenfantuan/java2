package com.example.springboottest.vo;

import java.util.List;

/**
 * 通用分页结果
 *
 * @param <T> 列表元素类型
 */
public class PageVO<T> {

    /** 当前页数据 */
    private List<T> list;
    /** 总条数 */
    private Long total;
    /** 当前页码（从 1 开始） */
    private Long page;
    /** 每页条数 */
    private Long size;
    /** 总页数 */
    private Long pages;
    /** 是否还有下一页（前端做「加载更多」时用） */
    private Boolean hasNext;

    public PageVO() {
    }

    public PageVO(List<T> list, Long total, Long page, Long size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = size == null || size == 0 ? 0 : (total + size - 1) / size;
        this.hasNext = page != null && this.pages != null && page < this.pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getPage() {
        return page;
    }

    public void setPage(Long page) {
        this.page = page;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getPages() {
        return pages;
    }

    public void setPages(Long pages) {
        this.pages = pages;
    }

    public Boolean getHasNext() {
        return hasNext;
    }

    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }
}
