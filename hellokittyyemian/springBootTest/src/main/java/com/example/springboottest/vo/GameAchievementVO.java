package com.example.springboottest.vo;

/**
 * 成就展示对象
 * progress / target 让前端可以直接画进度条，unlocked 决定是否点亮。
 */
public class GameAchievementVO {

    /** 成就编码，如 collect_6 */
    private String code;

    /** 成就标题 */
    private String title;

    /** 成就描述 */
    private String desc;

    /** 图标 emoji */
    private String emoji;

    /** 目标值（收集 6 张 / 连续打卡 7 天…） */
    private Integer target;

    /** 当前进度 */
    private Integer progress;

    /** 是否已解锁 */
    private Boolean unlocked;

    /** 解锁时间文案（yyyy-MM-dd HH:mm），未解锁为 null */
    private String unlockedAt;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public Integer getTarget() { return target; }
    public void setTarget(Integer target) { this.target = target; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Boolean getUnlocked() { return unlocked; }
    public void setUnlocked(Boolean unlocked) { this.unlocked = unlocked; }

    public String getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(String unlockedAt) { this.unlockedAt = unlockedAt; }
}
