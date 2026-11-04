package com.example.springboottest.dto;

/**
 * 游戏通用请求体（收集卡片 / 收藏卡片 / 打卡 / 抽奖都用它）
 *
 * 项目里前台用户没有独立的登录令牌，所以沿用「前端把 userId 带上来」的方式，
 * 后端统一校验这个用户是否真实存在（见 GameServiceImpl#checkUser）。
 */
public class GameActionRequest {

    /** 用户 ID（登录接口返回的 data.id） */
    private Long userId;

    /** 卡片 ID（收集 / 收藏时必填） */
    private Long cardId;

    /** 收藏状态：true 收藏，false 取消（不传则按「切换」处理） */
    private Boolean favorite;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public Boolean getFavorite() { return favorite; }
    public void setFavorite(Boolean favorite) { this.favorite = favorite; }
}
