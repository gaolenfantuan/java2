package com.example.springboottest.controller;

import com.example.springboottest.common.Result;
import com.example.springboottest.dto.GameActionRequest;
import com.example.springboottest.dto.QuizAnswerRequest;
import com.example.springboottest.service.GameService;
import com.example.springboottest.vo.CheckinResultVO;
import com.example.springboottest.vo.CollectResultVO;
import com.example.springboottest.vo.DrawResultVO;
import com.example.springboottest.vo.GameCardVO;
import com.example.springboottest.vo.GameProfileVO;
import com.example.springboottest.vo.QuizQuestionVO;
import com.example.springboottest.vo.QuizResultVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 小游戏接口（全新独立模块）
 *
 * 重要：这个 Controller 只暴露 /api/game/**，和原有的 /api/home/**、/api/auth/**、
 *      /api/admin/** 完全隔离，原有接口的路径、参数、返回结构一个字节都没有改动。
 *
 * 完整的接口清单：
 *   GET  /api/game/meta                     抽奖消耗、每日答题上限、稀有度权重
 *   GET  /api/game/profile?userId=1         游戏总览（积分 / 收集进度 / 连续打卡 / 成就）
 *   GET  /api/game/cards?userId=1           卡片图鉴（含收集、收藏状态）
 *   POST /api/game/collect                  收集一张卡片
 *   POST /api/game/favorite                 收藏 / 取消收藏
 *   POST /api/game/checkin                  每日打卡
 *   GET  /api/game/quiz?userId=1&limit=5    随机取题（不含答案）
 *   POST /api/game/quiz/answer              提交答案
 *   POST /api/game/draw                     幸运抽奖
 *
 * 返回结构沿用项目统一的 Result{ code, message, data }。
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /** 前端渲染常量：抽奖一次多少分、每天最多答几题、各稀有度权重 */
    @GetMapping("/meta")
    public Result<Map<String, Object>> meta() {
        return Result.success(gameService.getMeta());
    }

    /** 游戏总览：一个请求拿到积分、收集进度、连续打卡、答题统计、成就列表 */
    @GetMapping("/profile")
    public Result<GameProfileVO> profile(@RequestParam Long userId) {
        return Result.success(gameService.getProfile(userId));
    }

    /** 卡片图鉴 */
    @GetMapping("/cards")
    public Result<List<GameCardVO>> cards(@RequestParam Long userId) {
        return Result.success(gameService.getCards(userId));
    }

    /** 收集卡片（点击卡片时调用） */
    @PostMapping("/collect")
    public Result<CollectResultVO> collect(@RequestBody GameActionRequest request) {
        return Result.success(gameService.collect(request.getUserId(), request.getCardId()));
    }

    /** 收藏 / 取消收藏（favorite 不传则按「切换」处理） */
    @PostMapping("/favorite")
    public Result<GameCardVO> favorite(@RequestBody GameActionRequest request) {
        return Result.success(gameService.toggleFavorite(
                request.getUserId(), request.getCardId(), request.getFavorite()));
    }

    /** 每日打卡（一天只能一次） */
    @PostMapping("/checkin")
    public Result<CheckinResultVO> checkin(@RequestBody GameActionRequest request) {
        return Result.success(gameService.checkin(request.getUserId()));
    }

    /** 随机取题：只返回题干和选项，答案在后端 */
    @GetMapping("/quiz")
    public Result<List<QuizQuestionVO>> quiz(@RequestParam Long userId,
                                             @RequestParam(required = false) Integer limit) {
        return Result.success(gameService.getQuizQuestions(userId, limit));
    }

    /** 提交答案，答对加分 */
    @PostMapping("/quiz/answer")
    public Result<QuizResultVO> answer(@RequestBody QuizAnswerRequest request) {
        return Result.success(gameService.answerQuiz(
                request.getUserId(), request.getQuestionId(), request.getChoice()));
    }

    /** 幸运抽奖（消耗积分，稀有度按权重随机） */
    @PostMapping("/draw")
    public Result<DrawResultVO> draw(@RequestBody GameActionRequest request) {
        return Result.success(gameService.draw(request.getUserId()));
    }
}
