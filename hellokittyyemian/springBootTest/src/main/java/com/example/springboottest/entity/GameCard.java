package com.example.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 游戏卡片实体，对应数据库表 game_card
 *
 * 这是一张「卡片池」定义表：12 张卡片，带稀有度和抽奖权重。
 * 用户的收集 / 收藏状态在 game_user_card 里，两者分开，卡片池对所有用户是共享的。
 */
@TableName("game_card")
public class GameCard {

    /** 主键，数据库自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 卡片英文标识（唯一），如 strawberry_cake */
    private String code;

    /** 卡片名 */
    private String name;

    /** 卡片图案（emoji） */
    private String emoji;

    /** 所属系列：甜点 / 穿搭 / 好物 / 心情 */
    private String series;

    /** 稀有度：COMMON / RARE / EPIC / LEGENDARY */
    private String rarity;

    /** 渐变背景，前端直接当 style 用 */
    private String bg;

    /** 卡片小传 */
    private String intro;

    /** 抽奖权重，越大越容易抽到 */
    private Integer dropWeight;

    /** 排序号，越小越靠前 */
    private Integer sort;

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

    public String getBg() { return bg; }
    public void setBg(String bg) { this.bg = bg; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }

    public Integer getDropWeight() { return dropWeight; }
    public void setDropWeight(Integer dropWeight) { this.dropWeight = dropWeight; }

    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
}
