package com.example.springboottest.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springboottest.entity.GameCard;
import com.example.springboottest.entity.GameCheckin;
import com.example.springboottest.entity.GameDrawRecord;
import com.example.springboottest.entity.GameQuizQuestion;
import com.example.springboottest.entity.GameQuizRecord;
import com.example.springboottest.entity.GameUserAchievement;
import com.example.springboottest.entity.GameUserCard;
import com.example.springboottest.entity.GameUserPoint;
import com.example.springboottest.mapper.GameCardMapper;
import com.example.springboottest.mapper.GameCheckinMapper;
import com.example.springboottest.mapper.GameDrawRecordMapper;
import com.example.springboottest.mapper.GameQuizQuestionMapper;
import com.example.springboottest.mapper.GameQuizRecordMapper;
import com.example.springboottest.mapper.GameUserAchievementMapper;
import com.example.springboottest.mapper.GameUserCardMapper;
import com.example.springboottest.mapper.GameUserPointMapper;
import com.example.springboottest.mapper.UserMapper;
import com.example.springboottest.service.GameService;
import com.example.springboottest.vo.CheckinResultVO;
import com.example.springboottest.vo.CollectResultVO;
import com.example.springboottest.vo.DrawResultVO;
import com.example.springboottest.vo.GameAchievementVO;
import com.example.springboottest.vo.GameCardVO;
import com.example.springboottest.vo.GameProfileVO;
import com.example.springboottest.vo.QuizQuestionVO;
import com.example.springboottest.vo.QuizResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 四个小游戏的业务实现（全部为新增能力，不涉及任何原有业务）
 *
 * 设计要点：
 *   1) 卡片池（game_card）对所有用户共享，用户的收集 / 收藏状态存在 game_user_card；
 *   2) 积分只有一个来源表 game_user_point，任何加分 / 扣分都走 addPoints，方便对账；
 *   3) 成就条件写在下面的 ACH_DEFS 常量里，解锁记录存 game_user_achievement，
 *      因此「达成数量弹一次成就弹窗」这件事只需要判断有没有新插入即可；
 *   4) 涉及写多张表的动作（收集 / 打卡 / 答题 / 抽奖）都加了 @Transactional。
 */
@Service
public class GameServiceImpl implements GameService {

    // ================= 数值配置（想调平衡改这里就够了） =================

    /** 首次进入游戏页赠送的见面礼积分 */
    private static final int WELCOME_POINTS = 60;
    /** 每日打卡基础积分 */
    private static final int CHECKIN_BASE_POINTS = 10;
    /** 连续打卡满 7 天的额外奖励 */
    private static final int CHECKIN_WEEK_BONUS = 20;
    /** 单次抽奖消耗积分 */
    private static final int DRAW_COST = 20;
    /** 抽到重复卡返还的积分 */
    private static final int DUPLICATE_REFUND = 5;
    /** 每日答题奖励上限（防止无限刷分） */
    private static final int QUIZ_DAILY_LIMIT = 10;
    /** 一次默认给几道题 */
    private static final int QUIZ_DEFAULT_SIZE = 5;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 稀有度中文名（顺序固定，前端转盘按这个顺序画） */
    private static final Map<String, String> RARITY_NAME = new LinkedHashMap<>();

    static {
        RARITY_NAME.put("COMMON", "普通");
        RARITY_NAME.put("RARE", "稀有");
        RARITY_NAME.put("EPIC", "史诗");
        RARITY_NAME.put("LEGENDARY", "传说");
    }

    /** 成就条件类型 */
    private enum AchType { COLLECT, CHECKIN, QUIZ }

    /** 成就定义（条件 + 文案都在一起，省掉一张定义表） */
    private static class AchDef {
        final String code;
        final String title;
        final String desc;
        final String emoji;
        final int target;
        final AchType type;

        AchDef(String code, String title, String desc, String emoji, int target, AchType type) {
            this.code = code;
            this.title = title;
            this.desc = desc;
            this.emoji = emoji;
            this.target = target;
            this.type = type;
        }
    }

    private static final List<AchDef> ACH_DEFS = List.of(
            new AchDef("collect_3", "初遇甜心", "收集 3 张卡片", "🍬", 3, AchType.COLLECT),
            new AchDef("collect_6", "甜点小达人", "收集 6 张卡片", "🍰", 6, AchType.COLLECT),
            new AchDef("collect_9", "图鉴收藏家", "收集 9 张卡片", "📚", 9, AchType.COLLECT),
            new AchDef("collect_12", "全图鉴达成", "收集全部 12 张卡片", "👑", 12, AchType.COLLECT),
            new AchDef("checkin_3", "三日不断", "连续打卡 3 天", "☀️", 3, AchType.CHECKIN),
            new AchDef("checkin_7", "一周甜心", "连续打卡 7 天", "📅", 7, AchType.CHECKIN),
            new AchDef("checkin_14", "半月坚持", "连续打卡 14 天", "🏅", 14, AchType.CHECKIN),
            new AchDef("quiz_5", "甜点学徒", "累计答对 5 题", "🎓", 5, AchType.QUIZ),
            new AchDef("quiz_10", "甜点博士", "累计答对 10 题", "🧁", 10, AchType.QUIZ)
    );

    private final GameCardMapper cardMapper;
    private final GameUserCardMapper userCardMapper;
    private final GameCheckinMapper checkinMapper;
    private final GameUserPointMapper pointMapper;
    private final GameQuizQuestionMapper quizQuestionMapper;
    private final GameQuizRecordMapper quizRecordMapper;
    private final GameDrawRecordMapper drawRecordMapper;
    private final GameUserAchievementMapper achievementMapper;
    private final UserMapper userMapper;

    public GameServiceImpl(GameCardMapper cardMapper,
                           GameUserCardMapper userCardMapper,
                           GameCheckinMapper checkinMapper,
                           GameUserPointMapper pointMapper,
                           GameQuizQuestionMapper quizQuestionMapper,
                           GameQuizRecordMapper quizRecordMapper,
                           GameDrawRecordMapper drawRecordMapper,
                           GameUserAchievementMapper achievementMapper,
                           UserMapper userMapper) {
        this.cardMapper = cardMapper;
        this.userCardMapper = userCardMapper;
        this.checkinMapper = checkinMapper;
        this.pointMapper = pointMapper;
        this.quizQuestionMapper = quizQuestionMapper;
        this.quizRecordMapper = quizRecordMapper;
        this.drawRecordMapper = drawRecordMapper;
        this.achievementMapper = achievementMapper;
        this.userMapper = userMapper;
    }

    // ==================================================================
    //  总览
    // ==================================================================

    @Override
    public GameProfileVO getProfile(Long userId) {
        checkUser(userId);
        ensureAccount(userId);

        List<GameCard> cards = allCards();
        Map<Long, GameUserCard> stateMap = userCardMap(userId);

        int collected = (int) stateMap.values().stream()
                .filter(this::isCollected)
                .count();
        int favoriteCards = (int) stateMap.values().stream()
                .filter(row -> row.getFavorite() != null && row.getFavorite() == 1)
                .filter(this::isCollected)
                .count();

        int totalCards = cards.size();
        int percent = totalCards == 0 ? 0 : collected * 100 / totalCards;

        GameUserPoint account = ensureAccount(userId);
        List<GameCheckin> checkins = checkins(userId);
        LocalDate today = LocalDate.now();
        boolean todayChecked = checkins.stream().anyMatch(c -> today.equals(c.getCheckinDate()));

        int correctCount = countQuizRecords(userId, true);
        int totalQuizCount = countQuizRecords(userId, null);
        int todayQuizCount = countQuizToday(userId);
        int drawCount = (int) drawRecordMapper.selectCount(
                Wrappers.<GameDrawRecord>lambdaQuery().eq(GameDrawRecord::getUserId, userId)).longValue();

        List<GameAchievementVO> achievements = buildAchievements(userId, collected, maxStreak(userId), correctCount);

        GameProfileVO vo = new GameProfileVO();
        vo.setUserId(userId);
        vo.setPoints(nvl(account.getPoints()));
        vo.setTotalEarned(nvl(account.getTotalEarned()));
        vo.setTotalSpent(nvl(account.getTotalSpent()));
        vo.setCollectedCount(collected);
        vo.setTotalCards(totalCards);
        vo.setCollectPercent(percent);
        vo.setFavoriteCardCount(favoriteCards);
        vo.setStreakDays(currentStreak(userId));
        vo.setTodayChecked(todayChecked);
        vo.setTotalCheckinDays(checkins.size());
        vo.setCorrectCount(correctCount);
        vo.setTotalQuizCount(totalQuizCount);
        vo.setTodayQuizCount(todayQuizCount);
        vo.setDrawCount(drawCount);
        vo.setAchievements(achievements);
        vo.setNextAchievement(achievements.stream()
                .filter(a -> Boolean.FALSE.equals(a.getUnlocked()))
                .findFirst()
                .orElse(null));
        return vo;
    }

    @Override
    public Map<String, Object> getMeta() {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("drawCost", DRAW_COST);
        meta.put("welcomePoints", WELCOME_POINTS);
        meta.put("checkinBasePoints", CHECKIN_BASE_POINTS);
        meta.put("checkinWeekBonus", CHECKIN_WEEK_BONUS);
        meta.put("duplicateRefund", DUPLICATE_REFUND);
        meta.put("quizDailyLimit", QUIZ_DAILY_LIMIT);

        // 稀有度权重按卡片池里的真实数据统计，改数据即改概率，不用改代码
        List<GameCard> cards = allCards();
        List<Map<String, Object>> rarities = new ArrayList<>();
        RARITY_NAME.forEach((code, name) -> {
            int weight = cards.stream()
                    .filter(c -> code.equals(c.getRarity()))
                    .mapToInt(c -> nvl(c.getDropWeight()))
                    .sum();
            long count = cards.stream().filter(c -> code.equals(c.getRarity())).count();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", code);
            item.put("name", name);
            item.put("weight", weight);
            item.put("count", count);
            rarities.add(item);
        });
        meta.put("rarities", rarities);
        meta.put("totalCards", cards.size());
        return meta;
    }

    // ==================================================================
    //  玩法一：卡片收集 / 收藏
    // ==================================================================

    @Override
    public List<GameCardVO> getCards(Long userId) {
        checkUser(userId);
        ensureAccount(userId);
        return buildCards(userId);
    }

    @Override
    @Transactional
    public CollectResultVO collect(Long userId, Long cardId) {
        checkUser(userId);
        ensureAccount(userId);
        GameCard card = requireCard(cardId);

        GameUserCard row = findUserCard(userId, cardId);
        boolean firstTime;
        if (row == null) {
            row = new GameUserCard();
            row.setUserId(userId);
            row.setCardId(cardId);
            row.setCollectCount(1);
            row.setFavorite(0);
            row.setFirstCollectTime(LocalDateTime.now());
            row.setUpdateTime(LocalDateTime.now());
            userCardMapper.insert(row);
            firstTime = true;
        } else if (!isCollected(row)) {
            row.setCollectCount(1);
            row.setFirstCollectTime(LocalDateTime.now());
            row.setUpdateTime(LocalDateTime.now());
            userCardMapper.updateById(row);
            firstTime = true;
        } else {
            row.setCollectCount(nvl(row.getCollectCount()) + 1);
            row.setUpdateTime(LocalDateTime.now());
            userCardMapper.updateById(row);
            firstTime = false;
        }

        List<GameAchievementVO> newly = evaluateAchievements(userId);
        int collected = countCollected(userId);
        int total = allCards().size();

        CollectResultVO vo = new CollectResultVO();
        vo.setCard(toCardVO(card, row));
        vo.setFirstTime(firstTime);
        vo.setCollectedCount(collected);
        vo.setTotalCards(total);
        vo.setCollectPercent(total == 0 ? 0 : collected * 100 / total);
        vo.setNewAchievements(newly);
        vo.setMessage(firstTime
                ? "🎀 收到「" + card.getName() + "」啦！"
                : "这张已经收集过啦，又摸了一下~");
        return vo;
    }

    @Override
    @Transactional
    public GameCardVO toggleFavorite(Long userId, Long cardId, Boolean favorite) {
        checkUser(userId);
        GameCard card = requireCard(cardId);
        GameUserCard row = findUserCard(userId, cardId);
        if (row == null || !isCollected(row)) {
            throw new IllegalArgumentException("先把这张卡片收集起来，才能收藏哦~");
        }
        int target;
        if (favorite == null) {
            target = (row.getFavorite() != null && row.getFavorite() == 1) ? 0 : 1;
        } else {
            target = favorite ? 1 : 0;
        }
        row.setFavorite(target);
        row.setUpdateTime(LocalDateTime.now());
        userCardMapper.updateById(row);
        return toCardVO(card, row);
    }

    // ==================================================================
    //  玩法二：每日打卡
    // ==================================================================

    @Override
    @Transactional
    public CheckinResultVO checkin(Long userId) {
        checkUser(userId);
        ensureAccount(userId);
        LocalDate today = LocalDate.now();

        List<GameCheckin> checkins = checkins(userId);
        boolean todayChecked = checkins.stream().anyMatch(c -> today.equals(c.getCheckinDate()));

        CheckinResultVO vo = new CheckinResultVO();
        if (todayChecked) {
            GameUserPoint account = ensureAccount(userId);
            vo.setAlreadyChecked(true);
            vo.setStreakDays(currentStreak(userId));
            vo.setPointsAwarded(0);
            vo.setTotalCheckinDays(checkins.size());
            vo.setPoints(nvl(account.getPoints()));
            vo.setNewAchievements(List.of());
            vo.setMessage("今天已经打过卡啦，明天记得再来哦~");
            return vo;
        }

        // 昨天打过 -> 连续天数 +1；否则从 1 重新算
        GameCheckin latest = checkins.isEmpty() ? null : checkins.get(0);
        int streak = (latest != null && today.minusDays(1).equals(latest.getCheckinDate()))
                ? nvl(latest.getStreakDays()) + 1
                : 1;
        int award = CHECKIN_BASE_POINTS + (streak % 7 == 0 ? CHECKIN_WEEK_BONUS : 0);

        GameCheckin row = new GameCheckin();
        row.setUserId(userId);
        row.setCheckinDate(today);
        row.setStreakDays(streak);
        row.setPointsAwarded(award);
        row.setCreateTime(LocalDateTime.now());
        checkinMapper.insert(row);

        GameUserPoint account = addPoints(userId, award, true);
        List<GameAchievementVO> newly = evaluateAchievements(userId);

        vo.setAlreadyChecked(false);
        vo.setStreakDays(streak);
        vo.setPointsAwarded(award);
        vo.setTotalCheckinDays(checkins.size() + 1);
        vo.setPoints(nvl(account.getPoints()));
        vo.setNewAchievements(newly);
        vo.setMessage(streak % 7 == 0 && streak > 0
                ? "🎉 连续打卡 " + streak + " 天，额外奖励 " + CHECKIN_WEEK_BONUS + " 积分！"
                : "打卡成功，积分 +" + award + " ~");
        return vo;
    }

    // ==================================================================
    //  玩法三：甜点趣味答题
    // ==================================================================

    @Override
    public List<QuizQuestionVO> getQuizQuestions(Long userId, Integer limit) {
        checkUser(userId);
        ensureAccount(userId);
        int size = (limit == null || limit < 1) ? QUIZ_DEFAULT_SIZE : Math.min(limit, QUIZ_DAILY_LIMIT);

        List<GameQuizQuestion> all = quizQuestionMapper.selectList(
                Wrappers.<GameQuizQuestion>lambdaQuery().orderByAsc(GameQuizQuestion::getSort));
        if (all.isEmpty()) {
            return List.of();
        }
        Collections.shuffle(all, new Random());
        return all.stream().limit(size).map(this::toQuestionVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuizResultVO answerQuiz(Long userId, Long questionId, String choice) {
        checkUser(userId);
        ensureAccount(userId);
        if (questionId == null) {
            throw new IllegalArgumentException("缺少题目信息哦~");
        }
        if (choice == null || choice.trim().isEmpty()) {
            throw new IllegalArgumentException("先选一个答案再提交吧~");
        }
        GameQuizQuestion question = quizQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("这道题不见啦，换一题试试~");
        }
        if (countQuizToday(userId) >= QUIZ_DAILY_LIMIT) {
            throw new IllegalArgumentException("今天的答题奖励已经领满 " + QUIZ_DAILY_LIMIT + " 题啦，明天再来~");
        }

        String picked = choice.trim().toUpperCase();
        boolean correct = question.getAnswerKey() != null
                && question.getAnswerKey().equalsIgnoreCase(picked);
        int award = correct ? nvl(question.getPoints()) : 0;

        GameQuizRecord record = new GameQuizRecord();
        record.setUserId(userId);
        record.setQuestionId(questionId);
        record.setChosen(picked);
        record.setCorrect(correct ? 1 : 0);
        record.setPointsAwarded(award);
        record.setCreateTime(LocalDateTime.now());
        quizRecordMapper.insert(record);

        GameUserPoint account = correct
                ? addPoints(userId, award, true)
                : ensureAccount(userId);
        List<GameAchievementVO> newly = evaluateAchievements(userId);

        QuizResultVO vo = new QuizResultVO();
        vo.setCorrect(correct);
        vo.setChosen(picked);
        vo.setAnswerKey(question.getAnswerKey());
        vo.setExplanation(question.getExplanation());
        vo.setPointsAwarded(award);
        vo.setPoints(nvl(account.getPoints()));
        vo.setNewAchievements(newly);
        vo.setMessage(correct ? "答对啦！积分 +" + award + " 🎉" : "差一点点，答案是 " + question.getAnswerKey() + " 哦~");
        return vo;
    }

    // ==================================================================
    //  玩法四：幸运抽奖
    // ==================================================================

    @Override
    @Transactional
    public DrawResultVO draw(Long userId) {
        checkUser(userId);
        GameUserPoint account = ensureAccount(userId);
        if (nvl(account.getPoints()) < DRAW_COST) {
            throw new IllegalArgumentException("积分不足 " + DRAW_COST + " 分，先去打卡或答题攒一点吧~");
        }

        List<GameCard> pool = allCards().stream()
                .filter(c -> nvl(c.getDropWeight()) > 0)
                .collect(Collectors.toList());
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("卡片池还没准备好，晚点再来抽吧~");
        }

        // 按权重掷一次
        int totalWeight = pool.stream().mapToInt(c -> nvl(c.getDropWeight())).sum();
        int hit = new Random().nextInt(Math.max(totalWeight, 1));
        GameCard picked = pool.get(pool.size() - 1);
        int cursor = 0;
        for (GameCard c : pool) {
            cursor += nvl(c.getDropWeight());
            if (hit < cursor) {
                picked = c;
                break;
            }
        }

        // 先扣分
        account = addPoints(userId, -DRAW_COST, false);

        // 再入图鉴（抽到就算收集）
        GameUserCard row = findUserCard(userId, picked.getId());
        boolean firstTime;
        if (row == null) {
            row = new GameUserCard();
            row.setUserId(userId);
            row.setCardId(picked.getId());
            row.setCollectCount(1);
            row.setFavorite(0);
            row.setFirstCollectTime(LocalDateTime.now());
            row.setUpdateTime(LocalDateTime.now());
            userCardMapper.insert(row);
            firstTime = true;
        } else if (!isCollected(row)) {
            row.setCollectCount(1);
            row.setFirstCollectTime(LocalDateTime.now());
            row.setUpdateTime(LocalDateTime.now());
            userCardMapper.updateById(row);
            firstTime = true;
        } else {
            row.setCollectCount(nvl(row.getCollectCount()) + 1);
            row.setUpdateTime(LocalDateTime.now());
            userCardMapper.updateById(row);
            firstTime = false;
        }

        // 重复卡返还一点积分，避免抽到重复太挫败
        int refund = 0;
        if (!firstTime) {
            refund = DUPLICATE_REFUND;
            account = addPoints(userId, refund, true);
        }

        GameDrawRecord record = new GameDrawRecord();
        record.setUserId(userId);
        record.setCardId(picked.getId());
        record.setRarity(picked.getRarity());
        record.setCostPoints(DRAW_COST);
        record.setCreateTime(LocalDateTime.now());
        drawRecordMapper.insert(record);

        List<GameAchievementVO> newly = evaluateAchievements(userId);

        DrawResultVO vo = new DrawResultVO();
        vo.setCard(toCardVO(picked, row));
        vo.setRarity(picked.getRarity());
        vo.setRarityName(RARITY_NAME.getOrDefault(picked.getRarity(), "普通"));
        vo.setCostPoints(DRAW_COST);
        vo.setRefundPoints(refund);
        vo.setPoints(nvl(account.getPoints()));
        vo.setFirstTime(firstTime);
        vo.setNewAchievements(newly);
        vo.setMessage(firstTime
                ? "🎉 抽到新卡片「" + picked.getName() + "」！"
                : "这张已经有了，返还 " + refund + " 积分~");
        return vo;
    }

    // ==================================================================
    //  内部工具方法
    // ==================================================================

    /** 校验用户真实存在（前端把 userId 传上来，后端兜一层） */
    private void checkUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("请先登录后再玩小游戏哦~");
        }
        if (userMapper.selectById(userId) == null) {
            throw new IllegalArgumentException("没找到这个用户，请重新登录~");
        }
    }

    private GameCard requireCard(Long cardId) {
        if (cardId == null) {
            throw new IllegalArgumentException("缺少卡片信息哦~");
        }
        GameCard card = cardMapper.selectById(cardId);
        if (card == null) {
            throw new IllegalArgumentException("这张卡片不存在啦~");
        }
        return card;
    }

    /** 积分账户：没有就开一个，并送一份见面礼 */
    private GameUserPoint ensureAccount(Long userId) {
        GameUserPoint account = pointMapper.selectOne(
                Wrappers.<GameUserPoint>lambdaQuery().eq(GameUserPoint::getUserId, userId));
        if (account == null) {
            account = new GameUserPoint();
            account.setUserId(userId);
            account.setPoints(WELCOME_POINTS);
            account.setTotalEarned(WELCOME_POINTS);
            account.setTotalSpent(0);
            account.setUpdateTime(LocalDateTime.now());
            pointMapper.insert(account);
        }
        return account;
    }

    /** 加减积分统一入口：正数加到累计获得，负数加到累计消耗 */
    private GameUserPoint addPoints(Long userId, int delta, boolean earned) {
        GameUserPoint account = ensureAccount(userId);
        int before = nvl(account.getPoints());
        int after = Math.max(0, before + delta);
        account.setPoints(after);
        if (delta > 0) {
            account.setTotalEarned(nvl(account.getTotalEarned()) + delta);
        } else if (delta < 0) {
            account.setTotalSpent(nvl(account.getTotalSpent()) - delta);
        }
        account.setUpdateTime(LocalDateTime.now());
        pointMapper.updateById(account);
        return account;
    }

    private List<GameCard> allCards() {
        return cardMapper.selectList(
                Wrappers.<GameCard>lambdaQuery().orderByAsc(GameCard::getSort));
    }

    private List<GameCardVO> buildCards(Long userId) {
        Map<Long, GameUserCard> stateMap = userCardMap(userId);
        return allCards().stream()
                .map(card -> toCardVO(card, stateMap.get(card.getId())))
                .collect(Collectors.toList());
    }

    private Map<Long, GameUserCard> userCardMap(Long userId) {
        return userCardMapper.selectList(
                        Wrappers.<GameUserCard>lambdaQuery().eq(GameUserCard::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(GameUserCard::getCardId, Function.identity(), (a, b) -> a));
    }

    private GameUserCard findUserCard(Long userId, Long cardId) {
        return userCardMapper.selectOne(Wrappers.<GameUserCard>lambdaQuery()
                .eq(GameUserCard::getUserId, userId)
                .eq(GameUserCard::getCardId, cardId));
    }

    private boolean isCollected(GameUserCard row) {
        return row != null && nvl(row.getCollectCount()) > 0;
    }

    private int countCollected(Long userId) {
        return (int) userCardMapper.selectCount(Wrappers.<GameUserCard>lambdaQuery()
                .eq(GameUserCard::getUserId, userId)
                .gt(GameUserCard::getCollectCount, 0)).longValue();
    }

    private GameCardVO toCardVO(GameCard card, GameUserCard state) {
        GameCardVO vo = new GameCardVO();
        vo.setId(card.getId());
        vo.setCode(card.getCode());
        vo.setName(card.getName());
        vo.setEmoji(card.getEmoji());
        vo.setSeries(card.getSeries());
        vo.setRarity(card.getRarity());
        vo.setRarityName(RARITY_NAME.getOrDefault(card.getRarity(), "普通"));
        vo.setBg(card.getBg());
        vo.setIntro(card.getIntro());
        vo.setCollected(isCollected(state));
        vo.setFavorite(state != null && state.getFavorite() != null && state.getFavorite() == 1);
        vo.setCount(state == null ? 0 : nvl(state.getCollectCount()));
        return vo;
    }

    private QuizQuestionVO toQuestionVO(GameQuizQuestion q) {
        QuizQuestionVO vo = new QuizQuestionVO();
        vo.setId(q.getId());
        vo.setContent(q.getContent());
        vo.setCategory(q.getCategory());
        vo.setDifficulty(q.getDifficulty());
        vo.setPoints(q.getPoints());
        vo.setOptions(List.of(
                new QuizQuestionVO.Option("A", q.getOptionA()),
                new QuizQuestionVO.Option("B", q.getOptionB()),
                new QuizQuestionVO.Option("C", q.getOptionC()),
                new QuizQuestionVO.Option("D", q.getOptionD())
        ));
        return vo;
    }

    private List<GameCheckin> checkins(Long userId) {
        return checkinMapper.selectList(Wrappers.<GameCheckin>lambdaQuery()
                .eq(GameCheckin::getUserId, userId)
                .orderByDesc(GameCheckin::getCheckinDate));
    }

    /** 当前连续天数：最后一次打卡是今天或昨天才算「还在连续」 */
    private int currentStreak(Long userId) {
        List<GameCheckin> list = checkins(userId);
        if (list.isEmpty()) {
            return 0;
        }
        GameCheckin latest = list.get(0);
        LocalDate today = LocalDate.now();
        if (today.equals(latest.getCheckinDate()) || today.minusDays(1).equals(latest.getCheckinDate())) {
            return nvl(latest.getStreakDays());
        }
        return 0;
    }

    /** 历史最长连续天数：成就进度用它，断签也不回退 */
    private int maxStreak(Long userId) {
        return checkins(userId).stream()
                .mapToInt(c -> nvl(c.getStreakDays()))
                .max()
                .orElse(0);
    }

    private int countQuizRecords(Long userId, Boolean correct) {
        var wrapper = Wrappers.<GameQuizRecord>lambdaQuery().eq(GameQuizRecord::getUserId, userId);
        if (correct != null) {
            wrapper.eq(GameQuizRecord::getCorrect, correct ? 1 : 0);
        }
        return (int) quizRecordMapper.selectCount(wrapper).longValue();
    }

    private int countQuizToday(Long userId) {
        return (int) quizRecordMapper.selectCount(Wrappers.<GameQuizRecord>lambdaQuery()
                .eq(GameQuizRecord::getUserId, userId)
                .ge(GameQuizRecord::getCreateTime, LocalDate.now().atStartOfDay())).longValue();
    }

    // ---------------- 成就 ----------------

    private List<GameAchievementVO> buildAchievements(Long userId, int collected, int maxStreak, int correct) {
        Map<String, LocalDateTime> unlocked = achievementMapper.selectList(
                        Wrappers.<GameUserAchievement>lambdaQuery().eq(GameUserAchievement::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(GameUserAchievement::getCode,
                        a -> a.getCreateTime() == null ? LocalDateTime.now() : a.getCreateTime(),
                        (a, b) -> a));

        List<GameAchievementVO> list = new ArrayList<>();
        for (AchDef def : ACH_DEFS) {
            int progress = progressOf(def, collected, maxStreak, correct);
            LocalDateTime at = unlocked.get(def.code);
            GameAchievementVO vo = new GameAchievementVO();
            vo.setCode(def.code);
            vo.setTitle(def.title);
            vo.setDesc(def.desc);
            vo.setEmoji(def.emoji);
            vo.setTarget(def.target);
            vo.setProgress(progress);
            vo.setUnlocked(at != null);
            vo.setUnlockedAt(at == null ? null : at.format(TIME_FMT));
            list.add(vo);
        }
        return list;
    }

    /**
     * 结算成就：把这次动作之后「新达成」的成就写进库，并返回给前端弹窗。
     * 因为先查已解锁集合再算，所以同一个成就只会弹一次。
     */
    private List<GameAchievementVO> evaluateAchievements(Long userId) {
        int collected = countCollected(userId);
        int maxStreak = maxStreak(userId);
        int correct = countQuizRecords(userId, true);

        Map<String, LocalDateTime> unlocked = achievementMapper.selectList(
                        Wrappers.<GameUserAchievement>lambdaQuery().eq(GameUserAchievement::getUserId, userId))
                .stream()
                .collect(Collectors.toMap(GameUserAchievement::getCode,
                        a -> a.getCreateTime() == null ? LocalDateTime.now() : a.getCreateTime(),
                        (a, b) -> a));

        List<GameAchievementVO> newly = new ArrayList<>();
        for (AchDef def : ACH_DEFS) {
            if (unlocked.containsKey(def.code)) {
                continue;
            }
            int progress = progressOf(def, collected, maxStreak, correct);
            if (progress < def.target) {
                continue;
            }
            LocalDateTime now = LocalDateTime.now();
            GameUserAchievement row = new GameUserAchievement();
            row.setUserId(userId);
            row.setCode(def.code);
            row.setCreateTime(now);
            achievementMapper.insert(row);

            GameAchievementVO vo = new GameAchievementVO();
            vo.setCode(def.code);
            vo.setTitle(def.title);
            vo.setDesc(def.desc);
            vo.setEmoji(def.emoji);
            vo.setTarget(def.target);
            vo.setProgress(progress);
            vo.setUnlocked(true);
            vo.setUnlockedAt(now.format(TIME_FMT));
            newly.add(vo);
        }
        return newly;
    }

    private int progressOf(AchDef def, int collected, int maxStreak, int correct) {
        switch (def.type) {
            case COLLECT:
                return collected;
            case CHECKIN:
                return maxStreak;
            case QUIZ:
                return correct;
            default:
                return 0;
        }
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }
}
