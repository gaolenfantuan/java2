-- =====================================================
--  H2 内存数据库 建表脚本
--  位置：src/main/resources/db/schema.sql
--  Spring Boot 启动时会自动执行（application.yml 里配了 sql.init）
--  注意：user 是 H2 关键字，JDBC URL 里加了 NON_KEYWORDS=USER 才能用这个表名
--
--  四张表：
--    user       —— 前台用户
--    admin      —— 后台管理员（后台管理系统登录用）
--    category   —— 内容分类（parent_id 实现分级，0 表示一级分类）
--    home_item  —— 首页内容
-- =====================================================

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
