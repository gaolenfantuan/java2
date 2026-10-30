package com.example.springboottest.vo;

/**
 * 后台概览页的统计数字
 */
public class AdminStatsVO {

    /** 内容总数（含下架） */
    private Long itemTotal;
    /** 已上架 */
    private Long onlineTotal;
    /** 已下架 */
    private Long offlineTotal;
    /** 分类总数 */
    private Long categoryTotal;
    /** 累计点赞 */
    private Long likeTotal;
    /** 累计浏览 */
    private Long viewTotal;
    /** 热门内容数 */
    private Long hotTotal;

    public AdminStatsVO() {
    }

    public AdminStatsVO(Long itemTotal, Long onlineTotal, Long offlineTotal, Long categoryTotal,
                        Long likeTotal, Long viewTotal, Long hotTotal) {
        this.itemTotal = itemTotal;
        this.onlineTotal = onlineTotal;
        this.offlineTotal = offlineTotal;
        this.categoryTotal = categoryTotal;
        this.likeTotal = likeTotal;
        this.viewTotal = viewTotal;
        this.hotTotal = hotTotal;
    }

    public Long getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(Long itemTotal) {
        this.itemTotal = itemTotal;
    }

    public Long getOnlineTotal() {
        return onlineTotal;
    }

    public void setOnlineTotal(Long onlineTotal) {
        this.onlineTotal = onlineTotal;
    }

    public Long getOfflineTotal() {
        return offlineTotal;
    }

    public void setOfflineTotal(Long offlineTotal) {
        this.offlineTotal = offlineTotal;
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

    public Long getViewTotal() {
        return viewTotal;
    }

    public void setViewTotal(Long viewTotal) {
        this.viewTotal = viewTotal;
    }

    public Long getHotTotal() {
        return hotTotal;
    }

    public void setHotTotal(Long hotTotal) {
        this.hotTotal = hotTotal;
    }
}
