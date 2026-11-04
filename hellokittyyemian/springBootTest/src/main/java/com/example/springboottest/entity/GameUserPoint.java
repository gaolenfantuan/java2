package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户积分账户，对应数据库表 game_user_point
 * 一个用户一行，第一次进游戏页时自动开户，并赠送一笔「见面礼」积分。
 */
@TableName("game_user_point")
public class GameUserPoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 当前可用积分 */
    private Integer points;

    /** 累计获得 */
    private Integer totalEarned;

    /** 累计消耗 */
    private Integer totalSpent;

    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public Integer getTotalEarned() { return totalEarned; }
    public void setTotalEarned(Integer totalEarned) { this.totalEarned = totalEarned; }

    public Integer getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Integer totalSpent) { this.totalSpent = totalSpent; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
