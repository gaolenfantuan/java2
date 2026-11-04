package com.example.springboottest.service;

import com.example.springboottest.vo.CheckinResultVO;
import com.example.springboottest.vo.CollectResultVO;
import com.example.springboottest.vo.DrawResultVO;
import com.example.springboottest.vo.GameCardVO;
import com.example.springboottest.vo.GameProfileVO;
import com.example.springboottest.vo.QuizQuestionVO;
import com.example.springboottest.vo.QuizResultVO;

import java.util.List;
import java.util.Map;

/**
 * 小游戏业务接口（四大玩法：卡片收集、每日打卡、趣味答题、幸运抽奖）
 *
 * 说明：这一整套是新增的独立业务，和首页内容（HomeService）没有任何耦合，
 *      数据也只落在 game_* 表里，所以不会影响原有接口的返回结构。
 */
public interface GameService {

    /** 游戏总览：积分 / 收集进度 / 连续打卡 / 答题统计 / 成就 */
    GameProfileVO getProfile(Long userId);

    /** 卡片图鉴（含当前用户的收集、收藏状态） */
    List<GameCardVO> getCards(Long userId);

    /** 收集一张卡片（点击收集） */
    CollectResultVO collect(Long userId, Long cardId);

    /** 收藏 / 取消收藏一张已收集的卡片 */
    GameCardVO toggleFavorite(Long userId, Long cardId, Boolean favorite);

    /** 每日打卡 */
    CheckinResultVO checkin(Long userId);

    /** 随机取几道题（不含答案） */
    List<QuizQuestionVO> getQuizQuestions(Long userId, Integer limit);

    /** 提交答案 */
    QuizResultVO answerQuiz(Long userId, Long questionId, String choice);

    /** 幸运抽奖（消耗积分） */
    DrawResultVO draw(Long userId);

    /** 前端渲染所需的常量：抽奖消耗、每日答题上限、稀有度权重与配色 */
    Map<String, Object> getMeta();
}
