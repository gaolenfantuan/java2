package com.example.springboottest.vo;

import java.util.List;

/**
 * 抽奖结果
 * 前端拿它来播「转盘停下 → 卡片翻出 → 稀有度光效」这一整套动画。
 */
public class DrawResultVO {

    /** 抽到的卡片（已带上用户收集状态） */
    private GameCardVO card;

    /** 稀有度编码 */
    private String rarity;

    /** 稀有度中文名 */
    private String rarityName;

    /** 本次消耗积分 */
    private Integer costPoints;

    /** 重复卡返还的积分 */
    private Integer refundPoints;

    /** 抽完之后的积分余额 */
    private Integer points;

    /** 是否第一次获得这张卡 */
    private Boolean firstTime;

    /** 本次新解锁的成就 */
    private List<GameAchievementVO> newAchievements;

    private String message;

    public GameCardVO getCard() { return card; }
    public void setCard(GameCardVO card) { this.card = card; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getRarityName() { return rarityName; }
    public void setRarityName(String rarityName) { this.rarityName = rarityName; }

    public Integer getCostPoints() { return costPoints; }
    public void setCostPoints(Integer costPoints) { this.costPoints = costPoints; }

    public Integer getRefundPoints() { return refundPoints; }
    public void setRefundPoints(Integer refundPoints) { this.refundPoints = refundPoints; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Boolean getFirstTime() { return firstTime; }
    public void setFirstTime(Boolean firstTime) { this.firstTime = firstTime; }

    public List<GameAchievementVO> getNewAchievements() { return newAchievements; }
    public void setNewAchievements(List<GameAchievementVO> newAchievements) { this.newAchievements = newAchievements; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
