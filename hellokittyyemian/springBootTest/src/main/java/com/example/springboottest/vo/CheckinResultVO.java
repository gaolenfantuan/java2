package com.example.springboottest.vo;

import java.util.List;

/**
 * 打卡结果
 */
public class CheckinResultVO {

    /** 今天之前是否已经打过卡 */
    private Boolean alreadyChecked;

    /** 打卡后的连续天数 */
    private Integer streakDays;

    /** 本次奖励积分（已打卡过则为 0） */
    private Integer pointsAwarded;

    /** 累计打卡总天数 */
    private Integer totalCheckinDays;

    /** 打卡后的积分余额 */
    private Integer points;

    /** 本次新解锁的成就 */
    private List<GameAchievementVO> newAchievements;

    private String message;

    public Boolean getAlreadyChecked() { return alreadyChecked; }
    public void setAlreadyChecked(Boolean alreadyChecked) { this.alreadyChecked = alreadyChecked; }

    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }

    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public Integer getTotalCheckinDays() { return totalCheckinDays; }
    public void setTotalCheckinDays(Integer totalCheckinDays) { this.totalCheckinDays = totalCheckinDays; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public List<GameAchievementVO> getNewAchievements() { return newAchievements; }
    public void setNewAchievements(List<GameAchievementVO> newAchievements) { this.newAchievements = newAchievements; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
