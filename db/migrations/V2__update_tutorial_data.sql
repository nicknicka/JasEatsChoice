-- =====================================================
-- 教程表数据更新脚本
-- 说明: 更新现有数据并插入测试数据
-- =====================================================

USE jia_shi_yi_xuan;

-- 1. 更新现有数据的默认值（将现有数据标记为管理员发布的官方内容）
UPDATE tutorial
SET
    author_type = COALESCE(author_type, 'ADMIN'),
    status = 'PUBLISHED',
    review_status = COALESCE(review_status, 'APPROVED'),
    difficulty = COALESCE(difficulty, 'BEGINNER'),
    rating = COALESCE(rating, 4.5),
    rating_count = COALESCE(rating_count, 100),
    view_count = CASE
        WHEN view_count > 0 THEN view_count
        WHEN views REGEXP '^[0-9]+$' THEN CAST(views AS UNSIGNED)
        WHEN views REGEXP '^[0-9]+\\.?[0-9]*k$' THEN CAST(REPLACE(views, 'k', '') AS DECIMAL(10,2)) * 1000
        ELSE 0
    END
WHERE author_type IS NULL OR author_type = 'ADMIN';

-- 2. 插入测试数据
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
 '["面条", "家常菜", "快手", "AI生成"]')
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    source_type = VALUES(source_type),
    status = VALUES(status);

-- 3. 验证数据
SELECT
    '✅ 教程表数据更新完成！' AS status,
    COUNT(*) AS total_tutorials,
    SUM(CASE WHEN source_type = 'ADMIN' THEN 1 ELSE 0 END) AS admin_count,
    SUM(CASE WHEN source_type = 'MERCHANT' THEN 1 ELSE 0 END) AS merchant_count,
    SUM(CASE WHEN source_type = 'AI_GENERATED' THEN 1 ELSE 0 END) AS ai_count
FROM tutorial;

-- 4. 查看示例数据
SELECT
    id,
    title,
    source_type,
    author_type,
    author,
    status,
    review_status,
    is_official,
    difficulty,
    rating,
    view_count
FROM tutorial
ORDER BY create_time DESC
LIMIT 10;
