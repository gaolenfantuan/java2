package com.example.springboottest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 后台「新增 / 编辑内容」的请求体
 *
 * 新增和编辑共用一个：编辑时前端会把完整表单传过来，全部字段覆盖式更新。
 */
public class AdminItemRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多 100 个字")
    private String title;

    @Size(max = 255, message = "简介最多 255 个字")
    private String summary;

    @Size(max = 16, message = "emoji 最多 16 个字符")
    private String emoji;

    @Size(max = 50, message = "标签最多 50 个字")
    private String tag;

    @Size(max = 160, message = "封面背景太长啦")
    private String coverBg;

    @NotNull(message = "请选择所属分类")
    private Long categoryId;

    private Integer likes;
    private Integer views;
    /** 1 热门，0 普通 */
    private Integer isHot;
    private Integer sort;
    /** 1 上架（首页可见），0 下架 */
    private Integer status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCoverBg() {
        return coverBg;
    }

    public void setCoverBg(String coverBg) {
        this.coverBg = coverBg;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getIsHot() {
        return isHot;
    }

    public void setIsHot(Integer isHot) {
        this.isHot = isHot;
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
