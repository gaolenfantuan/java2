/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : hello_kitty

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 04/11/2026 09:48:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐧诲綍鐢ㄦ埛鍚嶏紙鍞?竴锛',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鏄电О',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀵嗙爜锛圫HA-256 鍔犵洂鍚庣殑瀵嗘枃锛',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '鐘舵?锛?姝ｅ父 0绂佺敤',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?鏈?垹闄?1宸插垹闄',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '超级管理员', '1e7786c1f0033ebfe3e4ac7c6d6089ebf748beaf79455062335830629fe99444', 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍒嗙被鍚嶇О锛屽?銆岀敎鐐广?銆岃泲绯曘?',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鍒嗙被鑻辨枃鏍囪瘑锛堝敮涓?級锛屽? dessert / cake',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '鐖跺垎绫籌D锛? 琛ㄧず涓?骇鍒嗙被',
  `level` tinyint NOT NULL DEFAULT 1 COMMENT '灞傜骇锛? 涓?骇锛? 浜岀骇',
  `icon` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'emoji 鍥炬爣',
  `sort` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭鍙凤紝瓒婂皬瓒婇潬鍓',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '鐘舵?锛?姝ｅ父 0闅愯棌',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?鏈?垹闄?1宸插垹闄',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_code`(`code` ASC) USING BTREE,
  INDEX `idx_category_parent`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '甜点', 'dessert', 0, 1, '🍰', 1, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (2, '穿搭', 'outfit', 0, 1, '🎀', 2, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (3, '好物', 'goods', 0, 1, '🧸', 3, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (4, '心情', 'mood', 0, 1, '🌸', 4, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (11, '蛋糕', 'cake', 1, 2, '🍰', 1, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (12, '布丁', 'pudding', 1, 2, '🍮', 2, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (13, '甜甜圈', 'donut', 1, 2, '🍩', 3, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (21, '发饰', 'hairpin', 2, 2, '🎀', 1, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (22, '服装', 'clothing', 2, 2, '👗', 2, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (23, '袜子', 'socks', 2, 2, '🧦', 3, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (31, '咖啡', 'coffee', 3, 2, '☕', 1, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (32, '玩偶', 'doll', 3, 2, '🧸', 2, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (33, '数码', 'digital', 3, 2, '🎧', 3, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (41, '季节', 'season', 4, 2, '🌸', 1, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (42, '日常', 'daily', 4, 2, '🌷', 2, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `category` VALUES (43, '习惯', 'habit', 4, 2, '📖', 3, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');

-- ----------------------------
-- Table structure for home_item
-- ----------------------------
DROP TABLE IF EXISTS `home_item`;
CREATE TABLE `home_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鏍囬?',
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '绠?粙/鎻忚堪锛坉esc 鏄?SQL 鍏抽敭瀛楋紝鎵?互鍒楀悕鍙?summary锛',
  `emoji` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '灏侀潰 emoji锛堜笉渚濊禆鍥剧墖璧勬簮锛',
  `tag` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鏍囩?锛屽?銆?浠婃棩鎺ㄨ崘銆',
  `cover_bg` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '灏侀潰娓愬彉鑳屾櫙锛屽墠绔?洿鎺ュ綋 style 鐢',
  `category_id` bigint NOT NULL COMMENT '鎵?睘鍒嗙被ID锛堜竴鑸?槸浜岀骇鍒嗙被锛',
  `likes` int NOT NULL DEFAULT 0 COMMENT '鐐硅禐鏁',
  `views` int NOT NULL DEFAULT 0 COMMENT '娴忚?閲',
  `is_hot` tinyint NOT NULL DEFAULT 0 COMMENT '鏄?惁鐑?棬鎺ㄨ崘锛?鏄?0鍚',
  `sort` int NOT NULL DEFAULT 0 COMMENT '鎺掑簭鍙凤紝瓒婂皬瓒婇潬鍓',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '鐘舵?锛?涓婃灦 0涓嬫灦',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?鏈?垹闄?1宸插垹闄',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_item_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_item_likes`(`likes` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of home_item
-- ----------------------------
INSERT INTO `home_item` VALUES (1, '草莓奶油蛋糕', '三层软绵戚风，夹着新鲜草莓和轻奶油。', '🍰', '#今日推荐', 'linear-gradient(135deg,#ffe3ef,#ffc9de)', 11, 128, 1024, 1, 1, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (2, '焦糖布丁', '微微发苦的焦糖配滑嫩蛋奶，一口就化。', '🍮', '#下午茶', 'linear-gradient(135deg,#fff2d8,#ffe0b3)', 12, 96, 760, 0, 2, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (3, '丝绒蝴蝶结发夹', '正红色丝绒，戴上去就是甜甜的小猫。', '🎀', '#点睛单品', 'linear-gradient(135deg,#ffdbe6,#ffb6cc)', 21, 215, 1680, 1, 3, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (4, '粉色百褶裙', '轻盈有垂感，转起来像一朵开花的花瓣。', '👗', '#春日出街', 'linear-gradient(135deg,#ffe8f4,#ffcfe6)', 22, 174, 1310, 0, 4, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (5, '猫咪拉花拿铁', '奶泡上画一只小猫，拍照超好看。', '☕', '#咖啡时间', 'linear-gradient(135deg,#f3e7dd,#e6d2c3)', 31, 88, 690, 0, 5, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (6, '毛绒小熊抱枕', '软乎乎一大只，抱着睡觉特别安心。', '🧸', '#治愈系', 'linear-gradient(135deg,#ffeede,#ffd9bd)', 32, 302, 2140, 1, 6, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (7, '樱花季限定', '一年只开两周，错过就要再等一年。', '🌸', '#季节限定', 'linear-gradient(135deg,#ffe6f2,#ffd0e6)', 41, 456, 3205, 1, 7, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (8, '今日份好心情', '天气很好，适合出门走走，买一束花。', '🌷', '#记录生活', 'linear-gradient(135deg,#fff0f5,#ffdcea)', 42, 67, 540, 0, 8, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (9, '草莓甜甜圈', '粉粉的糖霜加上彩针，看着就很开心。', '🍩', '#甜食控', 'linear-gradient(135deg,#ffe4ee,#ffc7dc)', 13, 143, 1150, 0, 9, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (10, '粉色头戴耳机', '降噪不错，通勤路上安静听歌。', '🎧', '#数码好物', 'linear-gradient(135deg,#ffe1ec,#ffc4db)', 33, 121, 980, 0, 10, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (11, '猫咪中筒袜', '袜口绣了小耳朵，露出来才发现很可爱。', '🧦', '#小心机', 'linear-gradient(135deg,#fff0f7,#ffd9ea)', 23, 59, 470, 0, 11, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `home_item` VALUES (12, '睡前读十页书', '把手机放远一点，读几页就困了。', '📖', '#好习惯', 'linear-gradient(135deg,#f6efff,#e8dcff)', 43, 41, 380, 0, 12, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '涓婚敭ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '鐧诲綍鐢ㄦ埛鍚嶏紙鍞?竴锛',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '鏄电О',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '瀵嗙爜锛圫HA-256 鍔犵洂鍚庣殑瀵嗘枃锛',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '澶村儚鍦板潃',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '鐘舵?锛?姝ｅ父 0绂佺敤',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?鏈?垹闄?1宸插垹闄',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2, 'kitty', '小猫咪', 'ed9ea76f3038618483947ee604441f13fd268c49d66d1782ebe222f82ef7edd1', NULL, 1, 0, '2026-11-04 09:03:14', '2026-11-04 09:03:14');
INSERT INTO `user` VALUES (4, 'root', '高冷饭团', '9daa4d3a5d4819ddacf7cea893f301c6354f5828c5437f4cb4cf614fbdbb2f38', NULL, 1, 0, '2026-11-04 09:09:13', '2026-11-04 09:09:13');

SET FOREIGN_KEY_CHECKS = 1;
