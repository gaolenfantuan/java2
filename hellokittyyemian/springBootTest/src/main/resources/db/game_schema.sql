-- =====================================================================
--  小游戏模块 —— 独立建表脚本
--  位置：src/main/resources/db/game_schema.sql
--
--  设计原则：
--    1) 和原有业务表（user / admin / category / home_item）完全解耦，
--       表名统一以 game_ 开头，不和任何原有表产生关联或外键。
--    2) 原有 schema.sql 一行都没改，本文件由 application.yml 里的
--       spring.sql.init.schema-locations 追加加载（排在 schema.sql 之后）。
--    3) 每次启动先 DROP 再 CREATE，配合内存库做到「重启即重置」。
-- =====================================================================

-- 注意 DROP 顺序：先删子表/记录表，再删主表
DROP TABLE IF EXISTS game_user_achievement;
DROP TABLE IF EXISTS game_draw_record;
DROP TABLE IF EXISTS game_quiz_record;
DROP TABLE IF EXISTS game_quiz_question;
DROP TABLE IF EXISTS game_user_point;
DROP TABLE IF EXISTS game_checkin;
DROP TABLE IF EXISTS game_user_card;
DROP TABLE IF EXISTS game_card;


-- -----------------------------------------------------
-- 【玩法一：卡片收集】卡片池（图鉴定义表）
--   12 张卡片，带稀有度和抽奖权重；drop_weight 越大越容易被抽到
-- -----------------------------------------------------
CREATE TABLE game_card
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    code        VARCHAR(50)  NOT NULL COMMENT '卡片英文标识（唯一），如 strawberry_cake',
    name        VARCHAR(50)  NOT NULL COMMENT '卡片名，如「草莓奶油蛋糕」',
    emoji       VARCHAR(16)  DEFAULT NULL COMMENT '卡片图案（用 emoji，不依赖图片）',
    series      VARCHAR(30)  DEFAULT NULL COMMENT '所属系列：甜点 / 穿搭 / 好物 / 心情',
    rarity      VARCHAR(20)  NOT NULL DEFAULT 'COMMON' COMMENT '稀有度：COMMON/RARE/EPIC/LEGENDARY',
    bg          VARCHAR(160) DEFAULT NULL COMMENT '卡片渐变背景，前端直接当 style 用',
    intro       VARCHAR(255) DEFAULT NULL COMMENT '卡片小传',
    drop_weight INT          NOT NULL DEFAULT 10 COMMENT '抽奖权重，越大越容易抽到',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time TIMESTAMP    DEFAULT NULL COMMENT '创建时间',
    update_time TIMESTAMP    DEFAULT NULL COMMENT '更新时间'
);

CREATE UNIQUE INDEX uk_game_card_code ON game_card (code);


-- -----------------------------------------------------
-- 【玩法一：卡片收集】用户卡片表：既记录「收集」也记录「收藏」
--   collect_count > 0  表示已收集（图鉴点亮）
--   favorite = 1       表示已收藏（收藏页展示）
--   (user_id, card_id) 唯一：同一个人同一张卡只有一行，重复收集只累加次数
-- -----------------------------------------------------
CREATE TABLE game_user_card
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id            BIGINT    NOT NULL COMMENT '用户ID',
    card_id            BIGINT    NOT NULL COMMENT '卡片ID',
    collect_count      INT       NOT NULL DEFAULT 0 COMMENT '收集次数（抽到重复卡会累加）',
    favorite           TINYINT   NOT NULL DEFAULT 0 COMMENT '是否收藏：1是 0否',
    first_collect_time TIMESTAMP DEFAULT NULL COMMENT '首次收集时间',
    deleted            TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time        TIMESTAMP DEFAULT NULL COMMENT '创建时间',
    update_time        TIMESTAMP DEFAULT NULL COMMENT '更新时间'
);

CREATE UNIQUE INDEX uk_game_user_card ON game_user_card (user_id, card_id);


-- -----------------------------------------------------
-- 【玩法二：每日打卡】一个用户一天最多一条
--   唯一索引 uk_game_checkin 从数据库层面兜住「一天只能打卡一次」
-- -----------------------------------------------------
CREATE TABLE game_checkin
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id        BIGINT    NOT NULL COMMENT '用户ID',
    checkin_date   DATE      NOT NULL COMMENT '打卡日期',
    streak_days    INT       NOT NULL DEFAULT 1 COMMENT '打卡时的连续天数',
    points_awarded INT       NOT NULL DEFAULT 0 COMMENT '本次奖励积分',
    deleted        TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time    TIMESTAMP DEFAULT NULL COMMENT '创建时间',
    update_time    TIMESTAMP DEFAULT NULL COMMENT '更新时间'
);

CREATE UNIQUE INDEX uk_game_checkin ON game_checkin (user_id, checkin_date);


-- -----------------------------------------------------
-- 【通用】积分账户表：一个用户一条，首次进入游戏时自动开户
--   四个玩法的积分增减全部集中在这一张表
-- -----------------------------------------------------
CREATE TABLE game_user_point
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id      BIGINT    NOT NULL COMMENT '用户ID',
    points       INT       NOT NULL DEFAULT 0 COMMENT '当前可用积分',
    total_earned INT       NOT NULL DEFAULT 0 COMMENT '累计获得',
    total_spent  INT       NOT NULL DEFAULT 0 COMMENT '累计消耗',
    deleted      TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time  TIMESTAMP DEFAULT NULL COMMENT '创建时间',
    update_time  TIMESTAMP DEFAULT NULL COMMENT '更新时间'
);

CREATE UNIQUE INDEX uk_game_point_user ON game_user_point (user_id);


-- -----------------------------------------------------
-- 【玩法三：甜点答题】题库（甜点 / 穿搭主题）
--   题干列名用 content，避开部分数据库的 question 关键字
-- -----------------------------------------------------
CREATE TABLE game_quiz_question
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    content     VARCHAR(255) NOT NULL COMMENT '题目（question 在部分数据库是关键字，列名用 content）',
    option_a    VARCHAR(120) NOT NULL COMMENT '选项A',
    option_b    VARCHAR(120) NOT NULL COMMENT '选项B',
    option_c    VARCHAR(120) NOT NULL COMMENT '选项C',
    option_d    VARCHAR(120) NOT NULL COMMENT '选项D',
    answer_key  CHAR(1)      NOT NULL COMMENT '正确答案：A/B/C/D',
    category    VARCHAR(30)  DEFAULT NULL COMMENT '题目分类：甜点 / 穿搭',
    difficulty  VARCHAR(20)  DEFAULT '简单' COMMENT '难度描述',
    points      INT          NOT NULL DEFAULT 5 COMMENT '答对奖励积分',
    explanation VARCHAR(255) DEFAULT NULL COMMENT '答案解析',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0停用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time TIMESTAMP    DEFAULT NULL COMMENT '创建时间',
    update_time TIMESTAMP    DEFAULT NULL COMMENT '更新时间'
);


-- -----------------------------------------------------
-- 【玩法三：甜点答题】答题记录表：每次提交都落库
--   既用于「我的成绩」统计，也用于限制每日奖励次数
-- -----------------------------------------------------
CREATE TABLE game_quiz_record
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id        BIGINT    NOT NULL COMMENT '用户ID',
    question_id    BIGINT    NOT NULL COMMENT '题目ID',
    chosen         VARCHAR(4) DEFAULT NULL COMMENT '用户选择的选项',
    correct        TINYINT   NOT NULL DEFAULT 0 COMMENT '是否答对：1是 0否',
    points_awarded INT       NOT NULL DEFAULT 0 COMMENT '本次获得积分',
    deleted        TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time    TIMESTAMP DEFAULT NULL COMMENT '创建时间',
    update_time    TIMESTAMP DEFAULT NULL COMMENT '更新时间'
);

CREATE INDEX idx_game_quiz_user ON game_quiz_record (user_id);


-- -----------------------------------------------------
-- 【玩法四：幸运抽奖】抽奖记录表
--   rarity 存的是抽中那一刻的稀有度快照，方便以后改配置也不影响历史
-- -----------------------------------------------------
CREATE TABLE game_draw_record
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    card_id     BIGINT      NOT NULL COMMENT '抽到的卡片ID',
    rarity      VARCHAR(20) NOT NULL COMMENT '抽到时的稀有度快照',
    cost_points INT         NOT NULL DEFAULT 0 COMMENT '本次消耗积分',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time TIMESTAMP   DEFAULT NULL COMMENT '创建时间',
    update_time TIMESTAMP   DEFAULT NULL COMMENT '更新时间'
);

CREATE INDEX idx_game_draw_user ON game_draw_record (user_id);


-- -----------------------------------------------------
-- 【成就】解锁记录表
--   成就条件写在代码常量里（GameServiceImpl.ACH_DEFS），这里只记录「谁解锁了什么」，
--   唯一索引保证同一个成就只解锁一次，前端弹窗也就只弹一次
-- -----------------------------------------------------
CREATE TABLE game_user_achievement
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    code        VARCHAR(50) NOT NULL COMMENT '成就编码，如 collect_6',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time TIMESTAMP   DEFAULT NULL COMMENT '解锁时间',
    update_time TIMESTAMP   DEFAULT NULL COMMENT '更新时间'
);

CREATE UNIQUE INDEX uk_game_user_ach ON game_user_achievement (user_id, code);
