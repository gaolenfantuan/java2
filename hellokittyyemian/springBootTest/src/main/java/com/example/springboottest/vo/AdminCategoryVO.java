package com.example.springboottest.vo;

/**
 * 后台分类列表（平铺返回，前端按 parentId 自己缩进展示）
 *
 * 带上 parentName / itemCount 是为了表格里能直接显示「属于谁、挂了多少内容」。
 */
public class AdminCategoryVO {

    private Long id;
    private String name;
    private String code;
    private Long parentId;
    /** 父分类名称，一级分类为空 */
    private String parentName;
    private Integer level;
    private String icon;
    private Integer sort;
    private Integer status;
    /** 直接挂在该分类下的内容数量（不含子分类） */
    private Long itemCount;
    private String createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getItemCount() {
        return itemCount;
    }

    public void setItemCount(Long itemCount) {
        this.itemCount = itemCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
