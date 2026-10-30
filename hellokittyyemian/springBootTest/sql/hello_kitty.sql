-- =====================================================
--  Hello Kitty 登录 / 注册 数据库脚本
--  数据库：hello_kitty   字符集：utf8mb4
--  使用方式（任选其一）：
--    1) Navicat / IDEA 数据库面板：打开本文件 -> 全选执行
--    2) 命令行：mysql -uroot -p123456 < hello_kitty.sql
-- =====================================================

CREATE DATABASE IF NOT EXISTS `hello_kitty`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `hello_kitty`;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名（唯一）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码（SHA-256 加盐后的密文）',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    `create_time` DATETIME     DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='用户表';

-- ----------------------------
-- 演示账号
--   用户名：kitty
--   密  码：1234
--   （密码是 SHA-256("1234" + "hello_kitty_salt") 的结果）
-- ----------------------------
INSERT INTO `user` (`username`, `nickname`, `password`, `avatar`, `status`, `deleted`, `create_time`, `update_time`)
VALUES ('kitty',
        '小猫咪',
        'ed9ea76f3038618483947ee604441f13fd268c49d66d1782ebe222f82ef7edd1',
        NULL,
        1,
        0,
        NOW(),
        NOW());

-- 查看结果
SELECT `id`, `username`, `nickname`, `status`, `create_time` FROM `user`;
