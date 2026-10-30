package com.example.springboottest.vo;

/**
 * 首页内容卡片（返回给前端的结构）
 *
 * 字段名刻意和前端 CardItem.vue 对齐（desc / bg / category），
 * 这样前端从假数据切到真接口时，卡片组件一行都不用改。
 */
public class HomeItemVO {

    private Long id;
    private String title;
    /** 简介（对应数据库 summary） */
    private String desc;
    private String emoji;
    private String tag;
    /** 封面渐变背景 */
    private String bg;
    private Long categoryId;
    /** 分类名称，如「甜点」 */
    private String category;
    /** 二级分类名称，如「蛋糕」 */
    private String subCategory;
    private Integer likes;
    private Integer views;
    /** 是否热门 */
    private Boolean hot;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getBg() {
        return bg;
    }

    public void setBg(String bg) {
        this.bg = bg;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
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

    public Boolean getHot() {
        return hot;
    }

    public void setHot(Boolean hot) {
        this.hot = hot;
    }
}
