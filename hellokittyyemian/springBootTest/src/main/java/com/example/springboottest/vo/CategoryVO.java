package com.example.springboottest.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树节点（返回给前端的结构）
 *
 * 一级分类带 children（二级分类），二级分类 children 为空数组。
 * count 是「该分类及其所有子分类」下的内容条数，前端可以直接显示「甜点 (3)」。
 */
public class CategoryVO {

    private Long id;
    private String name;
    private String code;
    private String icon;
    private Long parentId;
    private Integer level;
    /** 该分类（含子分类）下的内容数量 */
    private Long count;
    private List<CategoryVO> children = new ArrayList<>();

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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<CategoryVO> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryVO> children) {
        this.children = children;
    }
}
