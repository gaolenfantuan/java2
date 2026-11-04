package com.example.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springboottest.entity.GameQuizQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题题库 Mapper
 */
@Mapper
public interface GameQuizQuestionMapper extends BaseMapper<GameQuizQuestion> {
}
