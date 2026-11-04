package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.GameUserCard;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户卡片（收集 / 收藏）Mapper
 */
@Mapper
public interface GameUserCardMapper extends BaseMapper<GameUserCard> {
}
