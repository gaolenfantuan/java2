-- =====================================================
--  H2 内存数据库 初始化数据
--  位置：src/main/resources/db/data.sql
--  每次启动都会执行（内存库重启即重建，所以不会重复）
--
--  演示账号：kitty / 1234
--  密码是 SHA-256("1234" + "hello_kitty_salt") 的结果
--
--  分类 id 刻意写成 1/11/12... 这种「一级用个位、二级用两位数」的形式，
--  一眼就能看出层级，插入内容时也不容易写错。
-- =====================================================

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
