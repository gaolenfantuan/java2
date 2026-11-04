package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.GameUserPoint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户积分账户 Mapper
 */
@Mapper
public interface GameUserPointMapper extends BaseMapper<GameUserPoint> {
}
