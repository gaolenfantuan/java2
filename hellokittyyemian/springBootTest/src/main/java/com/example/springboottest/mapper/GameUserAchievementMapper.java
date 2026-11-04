package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.GameUserAchievement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成就解锁记录 Mapper
 */
@Mapper
public interface GameUserAchievementMapper extends BaseMapper<GameUserAchievement> {
}
