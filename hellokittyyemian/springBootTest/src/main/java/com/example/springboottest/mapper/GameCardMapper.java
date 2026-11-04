package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.GameCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 游戏卡片池 Mapper（继承 BaseMapper，增删改查自动具备）
 */
@Mapper
public interface GameCardMapper extends BaseMapper<GameCard> {
}
