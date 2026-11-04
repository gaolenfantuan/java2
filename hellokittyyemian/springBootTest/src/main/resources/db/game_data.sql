-- =====================================================================
--  小游戏模块 —— 独立种子数据脚本
--  位置：src/main/resources/db/game_data.sql
--
--  由 application.yml 的 spring.sql.init.data-locations 追加加载
--  （排在原有 data.sql 之后，原有 data.sql 一行没改）
-- =====================================================================

-- -----------------------------------------------------
-- 卡片池：12 张，稀有度越高抽奖权重越低
--   权重参考：COMMON 20 / RARE 10 / EPIC 4 / LEGENDARY 1
--   总权重 = 20*6 + 10*3 + 4*1 + 1*2 = 156
--   也就是说：普通卡约 76.9%、稀有 19.2%、史诗 2.6%、传说 1.3%
-- -----------------------------------------------------
INSERT INTO game_card (code, name, emoji, series, rarity, bg, intro, drop_weight, sort, status, deleted, create_time, update_time) VALUES
('strawberry_cake', '草莓奶油蛋糕', '🍰', '甜点', 'RARE',      'linear-gradient(135deg,#ffe3ef,#ffb6cf)', '三层戚风夹着草莓和轻奶油，是店里的招牌。',  10,  1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('bow_hairpin',     '丝绒蝴蝶结',   '🎀', '穿搭', 'COMMON',    'linear-gradient(135deg,#ffdbe6,#ffb0c8)', '正红色丝绒，戴上立刻甜甜的。',              20,  2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('caramel_pudding', '焦糖布丁',     '🍮', '甜点', 'COMMON',    'linear-gradient(135deg,#fff2d8,#ffdfae)', '微微发苦的焦糖配滑嫩蛋奶，一勺就化。',      20,  3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('strawberry_donut','草莓甜甜圈',   '🍩', '甜点', 'COMMON',    'linear-gradient(135deg,#ffe4ee,#ffc2da)', '粉色糖霜撒彩针，看着就开心。',              20,  4, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('milk_bottle',     '限定牛奶瓶',   '🥛', '甜点', 'COMMON',    'linear-gradient(135deg,#f4f7ff,#dfe9ff)', '玻璃瓶装的草莓牛奶，一天只出十瓶。',        20,  5, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('teddy_doll',      '毛绒小熊',     '🧸', '好物', 'RARE',      'linear-gradient(135deg,#ffeede,#ffd6b5)', '软乎乎一大只，抱着睡觉特别安心。',          10,  6, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('sakura_petal',    '樱花瓣',       '🌸', '心情', 'COMMON',    'linear-gradient(135deg,#ffe6f2,#ffcbdf)', '一年只开两周，落在手心里就化了。',          20,  7, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rainbow_candy',   '彩虹糖',       '🌈', '心情', 'EPIC',      'linear-gradient(135deg,#e8f4ff,#ffd9ec)', '七种颜色一把抓，心情也跟着变甜。',         4,  8, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('star_hairtie',    '星星发绳',     '⭐', '穿搭', 'COMMON',    'linear-gradient(135deg,#fffbe0,#ffe9a8)', '随手一扎就有元气，星星朝上更可爱。',        20,  9, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('heart_bag',       '爱心小包',     '👜', '穿搭', 'RARE',      'linear-gradient(135deg,#ffe1ef,#ffbdd8)', '能装下口红和零钱，出门刚刚好。',            10, 10, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('angel_kitty',     '天使小猫',     '👼', '心情', 'LEGENDARY', 'linear-gradient(135deg,#fff8e7,#ffd9a8)', '据说看到它的人，这一周都会很幸运。',        1, 11, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('golden_crown',    '黄金皇冠',     '👑', '好物', 'LEGENDARY', 'linear-gradient(135deg,#fff6cc,#ffd166)', '图鉴最高荣誉，只有最执着的人才集得到。',    1, 12, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- -----------------------------------------------------
-- 答题题库：甜点 / 穿搭 各 6 题，答对 +5 积分
--   正确答案统一放在 A 位，但接口不会下发 answer_key，
--   选项顺序在前端是打乱展示的，所以不影响公平性
-- -----------------------------------------------------
INSERT INTO game_quiz_question (content, option_a, option_b, option_c, option_d, answer_key, category, difficulty, points, explanation, sort, status, deleted, create_time, update_time) VALUES
('提拉米苏（Tiramisu）在意大利语里的原意是什么？', '带我走', '甜心', '早安', '云朵', 'A', '甜点', '简单', 5, 'Tiramisu 有「带我走」的意思，是它最浪漫的标签。', 1, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('马卡龙最经典的层次口感是？', '外脆内软', '硬如石头', '咸香酥脆', '冰凉弹牙', 'A', '甜点', '简单', 5, '好的马卡龙外壳薄脆、内里湿润，中间还有一层夹馅。', 2, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('戚风蛋糕能烤得蓬松，关键靠什么打发？', '蛋白霜', '奶油', '黄油', '牛奶', 'A', '甜点', '简单', 5, '蛋白霜撑起结构；翻拌时消泡就会塌，所以手法要轻。', 3, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('慕斯之所以能凝固成形，通常靠的是？', '吉利丁', '面粉', '酵母', '小苏打', 'A', '甜点', '中等', 5, '吉利丁（明胶）是慕斯凝固的主力，冷藏后才能定型。', 4, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('舒芙蕾出炉后为什么会塌陷？', '温度下降导致气泡回缩', '糖放少了', '模具太大', '没放黄油', 'A', '甜点', '中等', 5, '舒芙蕾靠热空气撑起来，一旦离炉降温就会自然回缩，最好立刻吃。', 5, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('蛋糕上捏花朵造型用的「翻糖」，英文是？', 'Fondant', 'Cream', 'Custard', 'Meringue', 'A', '甜点', '中等', 5, 'Fondant 可塑性极强，翻糖花是蛋糕装饰的常见技法。', 6, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hello Kitty 标志性的蝴蝶结一般戴在哪一侧？', '左耳', '右耳', '头顶', '脖子上', 'A', '穿搭', '简单', 5, '她常年长在左耳上，是最好认的辨识点。', 7, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('日系水手服最标志性的部件是？', '水手领', '西装领', '高领', '一字领', 'A', '穿搭', '简单', 5, '水手领（後ろ襟）就是背后那块方方正正的方巾式领子。', 8, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('洛丽塔（Lolita）风格最常见的裙型是？', 'A 字蓬蓬裙', '紧身包臀裙', '直筒长裙', '工装短裤', 'A', '穿搭', '简单', 5, '钟形裙撑撑出的蓬蓬裙是 Lolita 最典型的轮廓。', 9, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('粉色系穿搭里，最安全的配色搭档是？', '白色与奶油色', '大红色与黑色', '荧光绿', '深棕与橄榄绿', 'A', '穿搭', '中等', 5, '白色和奶油色能压住粉色的甜腻，整体更干净。', 10, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('把「JK 制服」的 JK 展开，指的是？', '女子高中生', '老师', '大学生', '上班族', 'A', '穿搭', '简单', 5, 'JK 是日语「女子高校生」的罗马音缩写。', 11, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('真丝、羊毛这类面料在清洗时最需要注意的是？', '冷水轻柔手洗', '开水烫洗', '强力甩干', '长时间暴晒', 'A', '穿搭', '中等', 5, '蛋白质纤维怕高温怕拧，冷水轻揉、平铺阴干最安全。', 12, 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
