package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.GameDrawRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 抽奖记录 Mapper
 */
@Mapper
public interface GameDrawRecordMapper extends BaseMapper<GameDrawRecord> {
}
