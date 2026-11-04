package com.example.springboottest.vo;

import java.util.List;

/**
 * 游戏总览：一个用户的全部游戏数据，一次请求返回
 *
 * 「我的」页、游戏大厅顶部、收藏页的图鉴概览都用它，
 * 好处是前端只请求一次就能把进度条、连续打卡、积分都渲染出来。
 */
public class GameProfileVO {

    private Long userId;

    /** 当前可用积分 */
    private Integer points;

    /** 累计获得积分 */
    private Integer totalEarned;

    /** 累计消耗积分 */
    private Integer totalSpent;

    // ---------------- 卡片收集 ----------------

    /** 已收集卡片数 */
    private Integer collectedCount;

    /** 卡片总数 */
    private Integer totalCards;

    /** 收集百分比（0-100，整数） */
    private Integer collectPercent;

    /** 已收藏（收藏页那张卡片）的图鉴数量 */
    private Integer favoriteCardCount;

    // ---------------- 每日打卡 ----------------

    /** 当前连续打卡天数 */
    private Integer streakDays;

    /** 今天是否已经打过卡 */
    private Boolean todayChecked;

    /** 累计打卡总天数 */
    private Integer totalCheckinDays;

    // ---------------- 趣味答题 ----------------

    /** 累计答对题数 */
    private Integer correctCount;

    /** 累计答题次数 */
    private Integer totalQuizCount;

    /** 今天已答题数（用于限制每日刷分） */
    private Integer todayQuizCount;

    // ---------------- 幸运抽奖 ----------------

    /** 累计抽奖次数 */
    private Integer drawCount;

    // ---------------- 成就 ----------------

    /** 全部成就（含未解锁） */
    private List<GameAchievementVO> achievements;

    /** 下一个待解锁的成就，全部解锁时为 null */
    private GameAchievementVO nextAchievement;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getTotalEarned() { return totalEarned; }
    public void setTotalEarned(Integer totalEarned) { this.totalEarned = totalEarned; }

    public Integer getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Integer totalSpent) { this.totalSpent = totalSpent; }

    public Integer getCollectedCount() { return collectedCount; }
    public void setCollectedCount(Integer collectedCount) { this.collectedCount = collectedCount; }

    public Integer getTotalCards() { return totalCards; }
    public void setTotalCards(Integer totalCards) { this.totalCards = totalCards; }

    public Integer getCollectPercent() { return collectPercent; }
    public void setCollectPercent(Integer collectPercent) { this.collectPercent = collectPercent; }

    public Integer getFavoriteCardCount() { return favoriteCardCount; }
    public void setFavoriteCardCount(Integer favoriteCardCount) { this.favoriteCardCount = favoriteCardCount; }

    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }

    public Boolean getTodayChecked() { return todayChecked; }
    public void setTodayChecked(Boolean todayChecked) { this.todayChecked = todayChecked; }

    public Integer getTotalCheckinDays() { return totalCheckinDays; }
    public void setTotalCheckinDays(Integer totalCheckinDays) { this.totalCheckinDays = totalCheckinDays; }

    public Integer getCorrectCount() { return correctCount; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public Integer getTotalQuizCount() { return totalQuizCount; }
    public void setTotalQuizCount(Integer totalQuizCount) { this.totalQuizCount = totalQuizCount; }

    public Integer getTodayQuizCount() { return todayQuizCount; }
    public void setTodayQuizCount(Integer todayQuizCount) { this.todayQuizCount = todayQuizCount; }

    public Integer getDrawCount() { return drawCount; }
    public void setDrawCount(Integer drawCount) { this.drawCount = drawCount; }

    public List<GameAchievementVO> getAchievements() { return achievements; }
    public void setAchievements(List<GameAchievementVO> achievements) { this.achievements = achievements; }

    public GameAchievementVO getNextAchievement() { return nextAchievement; }
    public void setNextAchievement(GameAchievementVO nextAchievement) { this.nextAchievement = nextAchievement; }
}
