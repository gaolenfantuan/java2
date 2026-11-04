package com.example.springboottest.vo;

import java.util.List;

/**
 * 收集卡片 / 抽奖之后的返回结果
 * 带上「最新进度 + 本次新解锁的成就」，前端据此弹成就弹窗、更新进度条。
 */
public class CollectResultVO {

    /** 本次操作的卡片 */
    private GameCardVO card;

    /** 是否第一次获得（false 表示重复收集 / 重复抽到） */
    private Boolean firstTime;

    /** 收集总数 */
    private Integer collectedCount;

    /** 卡片总数 */
    private Integer totalCards;

    /** 收集百分比 */
    private Integer collectPercent;

    /** 本次新解锁的成就（没有就是空数组） */
    private List<GameAchievementVO> newAchievements;

    /** 给用户看的一句话 */
    private String message;

    public GameCardVO getCard() { return card; }
    public void setCard(GameCardVO card) { this.card = card; }

    public Boolean getFirstTime() { return firstTime; }
    public void setFirstTime(Boolean firstTime) { this.firstTime = firstTime; }

    public Integer getCollectedCount() { return collectedCount; }
    public void setCollectedCount(Integer collectedCount) { this.collectedCount = collectedCount; }

    public Integer getTotalCards() { return totalCards; }
    public void setTotalCards(Integer totalCards) { this.totalCards = totalCards; }

    public Integer getCollectPercent() { return collectPercent; }
    public void setCollectPercent(Integer collectPercent) { this.collectPercent = collectPercent; }

    public List<GameAchievementVO> getNewAchievements() { return newAchievements; }
    public void setNewAchievements(List<GameAchievementVO> newAchievements) { this.newAchievements = newAchievements; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
