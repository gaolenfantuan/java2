package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 成就解锁记录，对应数据库表 game_user_achievement
 *
 * 成就的「条件和文案」写在 GameServiceImpl 的常量里（不需要额外一张定义表），
 * 这张表只负责记录「谁在什么时候解锁了哪个成就」，保证弹窗只弹一次。
 */
@TableName("game_user_achievement")
public class GameUserAchievement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 成就编码，如 collect_6 */
    private String code;

    /** 解锁时间 */
    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
