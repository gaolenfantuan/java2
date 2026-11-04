package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 抽奖记录，对应数据库表 game_draw_record
 * 稀有度存的是「抽中时的快照」，以后即使调整卡片池也不影响历史记录的可读性。
 */
@TableName("game_draw_record")
public class GameDrawRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long cardId;

    /** 抽中时的稀有度快照 */
    private String rarity;

    /** 本次消耗积分 */
    private Integer costPoints;

    private LocalDateTime createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public Integer getCostPoints() { return costPoints; }
    public void setCostPoints(Integer costPoints) { this.costPoints = costPoints; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
