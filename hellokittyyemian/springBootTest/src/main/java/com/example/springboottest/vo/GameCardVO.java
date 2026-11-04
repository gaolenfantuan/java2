package com.example.springboottest.vo;

/**
 * 卡片展示对象：卡片本身信息 + 当前用户的收集 / 收藏状态
 * 图鉴页、收集游戏、抽奖结果都用这个结构，前端只需要一套渲染逻辑。
 */
public class GameCardVO {

    private Long id;

    private String code;

    private String name;

    private String emoji;

    /** 系列：甜点 / 穿搭 / 好物 / 心情 */
    private String series;

    /** 稀有度编码：COMMON / RARE / EPIC / LEGENDARY */
    private String rarity;

    /** 稀有度中文名，前端直接展示 */
    private String rarityName;

    private String bg;

    private String intro;

    /** 当前用户是否已收集 */
    private Boolean collected;

    /** 当前用户是否已收藏 */
    private Boolean favorite;

    /** 已收集次数 */
    private Integer count;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }

    public String getRarityName() { return rarityName; }
    public void setRarityName(String rarityName) { this.rarityName = rarityName; }

    public String getBg() { return bg; }
    public void setBg(String bg) { this.bg = bg; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }

    public Boolean getCollected() { return collected; }
    public void setCollected(Boolean collected) { this.collected = collected; }

    public Boolean getFavorite() { return favorite; }
    public void setFavorite(Boolean favorite) { this.favorite = favorite; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
