package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.GameQuizRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题记录 Mapper
 */
@Mapper
public interface GameQuizRecordMapper extends BaseMapper<GameQuizRecord> {
}
