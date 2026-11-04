package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户卡片实体，对应数据库表 game_user_card
 *
 * 一行 = 某个用户的某张卡片。两个状态合在一行里存：
 *   collectCount > 0  -> 已收集（图鉴点亮）
 *   favorite = 1      -> 已收藏（收藏页展示）
 *
 * 唯一索引 (user_id, card_id) 保证不会出现重复行。
 */
@TableName("game_user_card")
public class GameUserCard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long cardId;

    /** 收集次数，抽到重复卡会累加 */
    private Integer collectCount;

    /** 是否收藏：1 是，0 否 */
    private Integer favorite;

    /** 首次收集时间 */
    private LocalDateTime firstCollectTime;

    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public Integer getCollectCount() { return collectCount; }
    public void setCollectCount(Integer collectCount) { this.collectCount = collectCount; }

    public Integer getFavorite() { return favorite; }
    public void setFavorite(Integer favorite) { this.favorite = favorite; }

    public LocalDateTime getFirstCollectTime() { return firstCollectTime; }
    public void setFirstCollectTime(LocalDateTime firstCollectTime) { this.firstCollectTime = firstCollectTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
