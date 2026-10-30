-- =====================================================
--  H2 内存数据库 完整建表 + 初始化数据脚本（独立版）
--
--  说明：项目启动时会自动执行 src/main/resources/db/schema.sql 和 data.sql，
--        正常情况下你不需要手动跑这个文件。
--        本文件用于：想在 H2 控制台 / DBeaver 里手动执行时使用。
--
--  H2 控制台地址：http://localhost:8080/h2-console
--    JDBC URL : jdbc:h2:mem:hello_kitty;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=USER
--    用户名    : sa
--    密码      : （留空）
--
--  内容：user（前台用户） / admin（后台管理员） / category（分级分类） / home_item（首页内容）
--  账号：前台 kitty / 1234  ，后台 admin / admin123
-- =====================================================

/* ===== 一、建表 ===== */

DROP TABLE IF EXISTS home_item;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS user;

-- -----------------------------------------------------
-- 用户表
-- -----------------------------------------------------
CREATE TABLE user
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username    VARCHAR(50)  NOT NULL COMMENT '登录用户名（唯一）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    password    VARCHAR(128) NOT NULL COMMENT '密码（SHA-256 加盐后的密文）',
    avatar      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time TIMESTAMP    DEFAULT NULL COMMENT '创建时间',
    update_time TIMESTAMP    DEFAULT NULL COMMENT '更新时间'
);

-- 用户名唯一索引：即使代码里判重失效，数据库层面也不会出现重复用户名
CREATE UNIQUE INDEX uk_username ON user (username);


-- -----------------------------------------------------
-- 后台管理员表（和前台用户分开，互不影响）
--   登录入口：/admin/login（前端）
--   演示账号：admin / admin123
-- -----------------------------------------------------
CREATE TABLE admin
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username    VARCHAR(50)  NOT NULL COMMENT '登录用户名（唯一）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    password    VARCHAR(128) NOT NULL COMMENT '密码（SHA-256 加盐后的密文）',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time TIMESTAMP    DEFAULT NULL COMMENT '创建时间',
    update_time TIMESTAMP    DEFAULT NULL COMMENT '更新时间'
);

CREATE UNIQUE INDEX uk_admin_username ON admin (username);


-- -----------------------------------------------------
-- 分类表（支持分级：parent_id = 0 是一级分类，否则是某个一级分类下的二级分类）
-- -----------------------------------------------------
CREATE TABLE category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name        VARCHAR(50) NOT NULL COMMENT '分类名称，如「甜点」「蛋糕」',
    code        VARCHAR(50) NOT NULL COMMENT '分类英文标识（唯一），如 dessert / cake',
    parent_id   BIGINT      NOT NULL DEFAULT 0 COMMENT '父分类ID，0 表示一级分类',
    level       TINYINT     NOT NULL DEFAULT 1 COMMENT '层级：1 一级，2 二级',
    icon        VARCHAR(16) DEFAULT NULL COMMENT 'emoji 图标',
    sort        INT         NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    status      TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1正常 0隐藏',
    deleted     TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time TIMESTAMP   DEFAULT NULL COMMENT '创建时间',
    update_time TIMESTAMP   DEFAULT NULL COMMENT '更新时间'
);

CREATE UNIQUE INDEX uk_category_code ON category (code);
CREATE INDEX idx_category_parent ON category (parent_id);


-- -----------------------------------------------------
-- 首页内容表
-- -----------------------------------------------------
CREATE TABLE home_item
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    summary     VARCHAR(255) DEFAULT NULL COMMENT '简介/描述（desc 是 SQL 关键字，所以列名叫 summary）',
    emoji       VARCHAR(16)  DEFAULT NULL COMMENT '封面 emoji（不依赖图片资源）',
    tag         VARCHAR(50)  DEFAULT NULL COMMENT '标签，如「#今日推荐」',
    cover_bg    VARCHAR(160) DEFAULT NULL COMMENT '封面渐变背景，前端直接当 style 用',
    category_id BIGINT       NOT NULL COMMENT '所属分类ID（一般是二级分类）',
    likes       INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    views       INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
    is_hot      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否热门推荐：1是 0否',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1上架 0下架',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    create_time TIMESTAMP    DEFAULT NULL COMMENT '创建时间',
    update_time TIMESTAMP    DEFAULT NULL COMMENT '更新时间'
);

CREATE INDEX idx_item_category ON home_item (category_id);
CREATE INDEX idx_item_likes ON home_item (likes);


/* ===== 二、初始化数据 ===== */

INSERT INTO user (username, nickname, password, avatar, status, deleted, create_time, update_time)
VALUES ('kitty',
        '小猫咪',
        'ed9ea76f3038618483947ee604441f13fd268c49d66d1782ebe222f82ef7edd1',
        NULL,
        1,
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);


-- =====================================================
--  后台管理员账号：admin / admin123
--  密码 = SHA-256("admin123" + "hello_kitty_salt")
-- =====================================================
INSERT INTO admin (username, nickname, password, status, deleted, create_time, update_time)
VALUES ('admin',
        '超级管理员',
        '1e7786c1f0033ebfe3e4ac7c6d6089ebf748beaf79455062335830629fe99444',
        1,
        0,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP);


-- =====================================================
--  一级分类（parent_id = 0）
-- =====================================================
INSERT INTO category (id, name, code, parent_id, level, icon, sort, status, deleted, create_time, update_time) VALUES
(1, '甜点', 'dessert', 0, 1, '🍰', 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '穿搭', 'outfit',  0, 1, '🎀', 2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '好物', 'goods',   0, 1, '🧸', 3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '心情', 'mood',    0, 1, '🌸', 4, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
--  二级分类（parent_id 指向一级分类）
-- =====================================================
INSERT INTO category (id, name, code, parent_id, level, icon, sort, status, deleted, create_time, update_time) VALUES
(11, '蛋糕',   'cake',      1, 2, '🍰', 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, '布丁',   'pudding',   1, 2, '🍮', 2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, '甜甜圈', 'donut',     1, 2, '🍩', 3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, '发饰',   'hairpin',   2, 2, '🎀', 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, '服装',   'clothing',  2, 2, '👗', 2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, '袜子',   'socks',     2, 2, '🧦', 3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, '咖啡',   'coffee',    3, 2, '☕', 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, '玩偶',   'doll',      3, 2, '🧸', 2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(33, '数码',   'digital',   3, 2, '🎧', 3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(41, '季节',   'season',    4, 2, '🌸', 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(42, '日常',   'daily',     4, 2, '🌷', 2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(43, '习惯',   'habit',     4, 2, '📖', 3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- =====================================================
--  首页内容（12 条，和前端原来的假数据一一对应）
-- =====================================================
INSERT INTO home_item (title, summary, emoji, tag, cover_bg, category_id, likes, views, is_hot, sort, status, deleted, create_time, update_time) VALUES
('草莓奶油蛋糕', '三层软绵戚风，夹着新鲜草莓和轻奶油。', '🍰', '#今日推荐', 'linear-gradient(135deg,#ffe3ef,#ffc9de)', 11, 128, 1024, 1, 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('焦糖布丁',     '微微发苦的焦糖配滑嫩蛋奶，一口就化。', '🍮', '#下午茶',   'linear-gradient(135deg,#fff2d8,#ffe0b3)', 12,  96,  760, 0, 2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('丝绒蝴蝶结发夹', '正红色丝绒，戴上去就是甜甜的小猫。',   '🎀', '#点睛单品', 'linear-gradient(135deg,#ffdbe6,#ffb6cc)', 21, 215, 1680, 1, 3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('粉色百褶裙',   '轻盈有垂感，转起来像一朵开花的花瓣。', '👗', '#春日出街', 'linear-gradient(135deg,#ffe8f4,#ffcfe6)', 22, 174, 1310, 0, 4, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('猫咪拉花拿铁', '奶泡上画一只小猫，拍照超好看。',       '☕', '#咖啡时间', 'linear-gradient(135deg,#f3e7dd,#e6d2c3)', 31,  88,  690, 0, 5, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('毛绒小熊抱枕', '软乎乎一大只，抱着睡觉特别安心。',     '🧸', '#治愈系',   'linear-gradient(135deg,#ffeede,#ffd9bd)', 32, 302, 2140, 1, 6, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('樱花季限定',   '一年只开两周，错过就要再等一年。',     '🌸', '#季节限定', 'linear-gradient(135deg,#ffe6f2,#ffd0e6)', 41, 456, 3205, 1, 7, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('今日份好心情', '天气很好，适合出门走走，买一束花。',   '🌷', '#记录生活', 'linear-gradient(135deg,#fff0f5,#ffdcea)', 42,  67,  540, 0, 8, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('草莓甜甜圈',   '粉粉的糖霜加上彩针，看着就很开心。',   '🍩', '#甜食控',   'linear-gradient(135deg,#ffe4ee,#ffc7dc)', 13, 143, 1150, 0, 9, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('粉色头戴耳机', '降噪不错，通勤路上安静听歌。',         '🎧', '#数码好物', 'linear-gradient(135deg,#ffe1ec,#ffc4db)', 33, 121,  980, 0, 10, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('猫咪中筒袜',   '袜口绣了小耳朵，露出来才发现很可爱。', '🧦', '#小心机',   'linear-gradient(135deg,#fff0f7,#ffd9ea)', 23,  59,  470, 0, 11, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('睡前读十页书', '把手机放远一点，读几页就困了。',       '📖', '#好习惯',   'linear-gradient(135deg,#f6efff,#e8dcff)', 43,  41,  380, 0, 12, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
