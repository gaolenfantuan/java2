package com.example.springboottest.vo;

/**
 * 首页横幅统计数据（全部来自数据库，不是写死的）
 */
public class HomeStatsVO {

    /** 内容总数 */
    private Long itemTotal;
    /** 分类总数（一级 + 二级） */
    private Long categoryTotal;
    /** 累计点赞数 */
    private Long likeTotal;
    /** 热门内容数 */
    private Long hotTotal;

    public HomeStatsVO() {
    }

    public HomeStatsVO(Long itemTotal, Long categoryTotal, Long likeTotal, Long hotTotal) {
        this.itemTotal = itemTotal;
        this.categoryTotal = categoryTotal;
        this.likeTotal = likeTotal;
        this.hotTotal = hotTotal;
    }

    public Long getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(Long itemTotal) {
        this.itemTotal = itemTotal;
    }

    public Long getCategoryTotal() {
        return categoryTotal;
    }

    public void setCategoryTotal(Long categoryTotal) {
        this.categoryTotal = categoryTotal;
    }

    public Long getLikeTotal() {
        return likeTotal;
    }

    public void setLikeTotal(Long likeTotal) {
        this.likeTotal = likeTotal;
    }

    public Long getHotTotal() {
        return hotTotal;
    }

    public void setHotTotal(Long hotTotal) {
        this.hotTotal = hotTotal;
    }
}
