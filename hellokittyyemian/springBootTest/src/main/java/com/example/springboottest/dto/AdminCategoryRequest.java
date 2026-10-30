package com.example.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 后台「新增 / 编辑分类」的请求体
 *
 * level 不用前端传，后端按 parentId 自动算：
 *   parentId = 0 -> 一级分类（level 1）
 *   parentId = 某个一级分类 -> 二级分类（level 2）
 */
public class AdminCategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最多 50 个字")
    private String name;

    /** 英文标识，留空会自动生成 */
    @Size(max = 50, message = "英文标识最多 50 个字符")
    private String code;

    /** 父分类 ID，0 表示一级分类 */
    private Long parentId;

    @Size(max = 16, message = "图标最多 16 个字符")
    private String icon;

    private Integer sort;
    /** 1 正常，0 隐藏 */
    private Integer status;

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
}
