-- =====================================================
-- 教程表升级 - 支持多来源数据管理
-- 创建时间: 2025-01-29
-- 说明: 添加管理员/商家/AI三种来源类型支持
-- =====================================================

-- 使用数据库
USE jia_shi_yi_xuan;

-- 1. 备份现有数据
CREATE TABLE IF NOT EXISTS tutorial_backup_20250129 AS SELECT * FROM tutorial;

-- 2. 添加新字段
ALTER TABLE tutorial
ADD COLUMN source_type VARCHAR(20) NOT NULL DEFAULT 'ADMIN'
COMMENT '来源类型: ADMIN-管理员, MERCHANT-商家, AI_GENERATED-AI生成'
AFTER views,

ADD COLUMN source_id BIGINT
COMMENT '来源ID: 管理员ID/商家ID/AI模型版本'
AFTER source_type,

ADD COLUMN author_type VARCHAR(20)
COMMENT '作者类型: ADMIN, MERCHANT, AI'
AFTER source_id,

ADD COLUMN author_id BIGINT
COMMENT '作者ID'
AFTER author_type,

ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED'
COMMENT '状态: DRAFT-草稿, PENDING-待审核, PUBLISHED-已发布, REJECTED-已拒绝'
AFTER featured,

ADD COLUMN review_status VARCHAR(20)
COMMENT '审核状态: NOT_SUBMITTED-未提交, PENDING-待审核, APPROVED-通过, REJECTED-拒绝'
AFTER status,

ADD COLUMN reviewer_id BIGINT
COMMENT '审核人ID'
AFTER review_status,

ADD COLUMN review_time DATETIME
COMMENT '审核时间'
AFTER reviewer_id,

ADD COLUMN review_comment TEXT
COMMENT '审核意见'
AFTER review_time,

ADD COLUMN is_official TINYINT(1) DEFAULT 0
COMMENT '是否官方认证(仅管理员发布)'
AFTER review_comment,

ADD COLUMN linked_merchant_id BIGINT
COMMENT '关联商家ID(商家教程可用)'
AFTER is_official,

ADD COLUMN linked_dish_id BIGINT
COMMENT '关联菜品ID(商家教程可用)'
AFTER linked_merchant_id,

ADD COLUMN ai_model_version VARCHAR(50)
COMMENT 'AI模型版本(AI教程)'
AFTER linked_dish_id,

ADD COLUMN tags JSON
COMMENT '标签数组: ["健康", "低卡", "素食"]'
AFTER ai_model_version,

ADD COLUMN difficulty VARCHAR(20)
COMMENT '难度: BEGINNER-初级, INTERMEDIATE-中级, ADVANCED-高级'
AFTER tags,

ADD COLUMN calories INT
COMMENT '卡路里'
AFTER difficulty,

ADD COLUMN prep_time VARCHAR(20)
COMMENT '准备时间'
AFTER calories,

ADD COLUMN servings INT
COMMENT '份量'
AFTER prep_time,

ADD COLUMN rating DECIMAL(3,2) DEFAULT 0.00
COMMENT '评分(0-5)'
AFTER servings,

ADD COLUMN rating_count INT DEFAULT 0
COMMENT '评分人数'
AFTER rating,

ADD COLUMN favorite_count INT DEFAULT 0
COMMENT '收藏次数'
AFTER rating_count,

ADD COLUMN view_count INT DEFAULT 0
COMMENT '浏览次数'
AFTER favorite_count,

ADD COLUMN share_count INT DEFAULT 0
COMMENT '分享次数'
AFTER view_count;

-- 3. 添加索引
ALTER TABLE tutorial
ADD INDEX idx_source_type (source_type),
ADD INDEX idx_status (status),
ADD INDEX idx_review_status (review_status),
ADD INDEX idx_featured (featured),
ADD INDEX idx_author (author_id),
ADD INDEX idx_linked_dish (linked_dish_id),
ADD INDEX idx_linked_merchant (linked_merchant_id),
ADD INDEX idx_rating (rating);

-- 4. 更新现有数据的默认值（将现有数据标记为管理员发布的官方内容）
UPDATE tutorial
SET
    source_type = 'ADMIN',
    author_type = 'ADMIN',
    status = 'PUBLISHED',
    review_status = 'APPROVED',
    is_official = 1,
    difficulty = 'BEGINNER',
    rating = 4.5,
    rating_count = 100,
    view_count = CASE
        WHEN views REGEXP '^[0-9]+$' THEN CAST(views AS UNSIGNED)
        WHEN views REGEXP '^[0-9]+\\.?[0-9]*k$' THEN CAST(REPLACE(views, 'k', '') AS DECIMAL(10,2)) * 1000
        ELSE 0
    END
WHERE source_type IS NULL OR source_type = 'ADMIN';

-- 5. 插入测试数据
INSERT INTO tutorial (
    title, type, duration, source_type, author_type, author,
    status, review_status, featured, is_official,
    difficulty, calories, prep_time, servings,
    content, cover_image, video_url,
    rating, rating_count, view_count, favorite_count, share_count,
    tags
) VALUES
-- 管理员发布的官方教程
('青木瓜沙拉制作教程', 'video', '5:30', 'ADMIN', 'ADMIN', '官方营养师',
 'PUBLISHED', 'APPROVED', 1, 1,
 'BEGINNER', 120, '15分钟', 2,
 '### 制作步骤\n1. 将青木瓜去皮，用刨刀切成细丝\n2. 加入花生碎、红辣椒丝、蒜末\n3. 调制料汁：鱼露2勺+柠檬汁3勺+糖1勺\n4. 将料汁倒入木瓜丝，用手抓拌均匀\n5. 最后加入西红柿片和生菜叶点缀即可\n\n### 小贴士\n- 选择未成熟的青木瓜，口感更脆爽\n- 根据个人口味调整辣椒和鱼露用量',
 'https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=800', NULL,
 4.8, 156, 12500, 342, 128,
 '["健康", "低卡", "泰式", "素食"]'),

('夏日低卡饮食指南', 'article', '8分钟', 'ADMIN', 'ADMIN', '官方营养师',
 'PUBLISHED', 'APPROVED', 1, 1,
 'BEGINNER', 0, '10分钟', 1,
 '## 夏日低卡饮食黄金法则\n\n### 🌞 早餐篇\n- 选择燕麦粥配水果（约300卡）\n- 全麦面包+水煮蛋+无糖豆浆（约350卡）\n\n### 🥗 午餐篇\n- 鸡胸肉沙拉配油醋汁（约400卡）\n- 荞麦面配时蔬（约450卡）\n\n### 🌙 晚餐篇\n- 清蒸鱼+时蔬（约350卡）\n- 豆腐汤+小碗糙米饭（约400卡）\n\n### 💡 关键提示\n1. 多喝水，每天至少2L\n2. 避免含糖饮料\n3. 选择蒸煮等健康烹饪方式\n4. 控制份量，七分饱即可',
 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800', NULL,
 4.9, 234, 8200, 567, 189,
 '["健康", "低卡", "减脂", "营养"]'),

('健康早餐搭配技巧', 'video', '3:45', 'ADMIN', 'ADMIN', '官方营养师',
 'PUBLISHED', 'APPROVED', 0, 1,
 'INTERMEDIATE', 380, '10分钟', 1,
 '## 均衡早餐四要素\n\n### 1. 优质碳水（拳头大小）\n- 燕麦粥、全麦面包、糙米饭\n\n### 2. 优质蛋白（手掌大小）\n- 鸡蛋、牛奶、豆浆、瘦肉\n\n### 3. 蔬菜水果（双手捧起大小）\n- 香蕉、苹果、菠菜、西红柿\n\n### 4. 健康脂肪（拇指大小）\n- 坚果、牛油果、橄榄油\n\n### ⏰ 时间优化\n- 前一天晚上准备食材\n- 利用微波炉快速加热\n- 选择快手食谱',
 'https://images.unsplash.com/photo-1494390248081-4e521a5940db?w=800', NULL,
 4.6, 189, 9700, 423, 156,
 '["健康", "早餐", "营养", "快手"]'),

-- 商家发布的教程（待审核）
('秘制红烧肉做法', 'video', '12:30', 'MERCHANT', 'MERCHANT', '川味香餐厅',
 'PENDING', 'PENDING', 0, 0,
 'INTERMEDIATE', 580, '90分钟', 4,
 '## 川味香餐厅招牌红烧肉\n\n### 食材准备\n- 五花肉 500g\n- 冰糖 30g\n- 生抽、老抽适量\n\n### 制作步骤\n1. 五花肉切块，冷水下锅焯水\n2. 热锅下冰糖炒至枣红色\n3. 下肉块翻炒上色\n4. 加生抽、老抽调色调味\n5. 加水没过肉块，大火烧开转小火焖煮\n6. 最后大火收汁即可\n\n### 店家秘籍\n- 焯水时加料酒去腥\n- 炒糖色要用小火，避免发苦',
 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800', NULL,
 4.7, 0, 0, 0, 0,
 '["肉类", "红烧", "家常菜", "下饭"]'),

-- AI生成的教程（待审核）
('番茄鸡蛋面的10种做法', 'article', '15分钟', 'AI_GENERATED', 'AI', 'AI智能助手',
 'DRAFT', 'NOT_SUBMITTED', 0, 0,
 'BEGINNER', 320, '20分钟', 2,
 '## AI生成的番茄鸡蛋面做法大全\n\n### 做法一：经典家常版\n1. 番茄切块，鸡蛋打散\n2. 热锅炒蛋盛起\n3. 炒番茄出汁\n4. 加水煮开，下面条\n5. 最后加入鸡蛋块即可\n\n### 做法二：浓汤版\n- 番茄先炒出沙\n- 加入番茄酱增味\n- 用淀粉勾芡\n\n### 做法三：快手版\n- 使用挂面\n- 番茄切丁更容易出汁\n- 全程大火5分钟完成\n\n*(此内容由AI生成，待人工审核后发布)*',
 'https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800', NULL,
 0.0, 0, 0, 0, 0,
 '["面条", "家常菜", "快手", "AI生成"]');

-- 6. 创建视图：教程列表（包含关联信息）
CREATE OR REPLACE VIEW v_tutorial_list AS
SELECT
    t.*,
    CASE
        WHEN t.source_type = 'ADMIN' AND t.is_official = 1 THEN '官方认证'
        WHEN t.source_type = 'MERCHANT' THEN m.name
        WHEN t.source_type = 'AI_GENERATED' THEN 'AI智能助手'
        ELSE '未知来源'
    END AS source_name,
    m.name AS merchant_name,
    m.logo AS merchant_logo,
    d.name AS dish_name,
    d.image AS dish_image,
    u.username AS reviewer_name
FROM tutorial t
LEFT JOIN merchant m ON t.linked_merchant_id = m.id
LEFT JOIN dish d ON t.linked_dish_id = d.id
LEFT JOIN user u ON t.reviewer_id = u.id;

-- 7. 创建存储过程：更新教程统计数据
DELIMITER //
CREATE PROCEDURE update_tutorial_stats(IN tutorial_id BIGINT)
BEGIN
    UPDATE tutorial
    SET
        favorite_count = (SELECT COUNT(*) FROM collection WHERE target_id = tutorial_id AND type = 'tutorial'),
        view_count = view_count + 1
    WHERE id = tutorial_id;
END //
DELIMITER ;

-- 8. 完成提示
SELECT '✅ 教程表升级完成！' AS status,
       '已添加' AS message,
       COUNT(*) AS total_tutorials
FROM tutorial;

-- 9. 验证数据
SELECT
    id,
    title,
    source_type,
    author_type,
    status,
    review_status,
    is_official,
    difficulty,
    rating
FROM tutorial
ORDER BY create_time DESC
LIMIT 10;
