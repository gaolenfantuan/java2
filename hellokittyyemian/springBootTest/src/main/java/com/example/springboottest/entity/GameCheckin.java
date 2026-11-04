package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日打卡记录，对应数据库表 game_checkin
 * 唯一索引 (user_id, checkin_date) 保证「一天只能打一次」这件事在数据库层面成立。
 */
@TableName("game_checkin")
public class GameCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 打卡日期 */
    private LocalDate checkinDate;

    /** 打卡时的连续天数快照 */
    private Integer streakDays;

    /** 本次奖励积分 */
    private Integer pointsAwarded;

    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getCheckinDate() { return checkinDate; }
    public void setCheckinDate(LocalDate checkinDate) { this.checkinDate = checkinDate; }

    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }

    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
