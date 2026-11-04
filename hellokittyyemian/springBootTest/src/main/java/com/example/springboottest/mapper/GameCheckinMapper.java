package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.GameCheckin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 每日打卡 Mapper
 */
@Mapper
public interface GameCheckinMapper extends BaseMapper<GameCheckin> {
}
