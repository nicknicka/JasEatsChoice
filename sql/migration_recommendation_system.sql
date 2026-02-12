-- ========================================
-- 佳食宜选推荐系统数据库迁移脚本
-- 创建时间: 2026-01-24
-- 说明: 创建推荐系统所需的5张核心表
-- ========================================

-- ========================================
-- 1. 用户行为记录表 (user_behavior)
-- 用途: 记录用户的所有行为，用于构建用户画像和推荐训练
-- ========================================
CREATE TABLE IF NOT EXISTS `user_behavior` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
  `behavior_type` VARCHAR(20) NOT NULL COMMENT '行为类型: view(浏览)/click(点击)/order(下单)/favorite(收藏)/reject(拒绝)/share(分享)',
  `item_type` VARCHAR(20) NOT NULL COMMENT '物品类型: dish(菜品)/merchant(商家)/recipe(食谱)',
  `item_id` VARCHAR(50) NOT NULL COMMENT '物品ID',
  `context` JSON COMMENT '上下文信息: {time, weather, location, device}',
  `duration` INT DEFAULT NULL COMMENT '行为持续时长(秒), 浏览类行为使用',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
  INDEX `idx_user_time` (`user_id`, `created_time`),
  INDEX `idx_item_time` (`item_id`, `created_time`),
  INDEX `idx_behavior_type` (`behavior_type`),
  INDEX `idx_user_behavior_type` (`user_id`, `behavior_type`, `created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为记录表';

-- ========================================
-- 2. 用户画像表 (user_profile)
-- 用途: 定期更新的用户特征标签和统计数据
-- ========================================
CREATE TABLE IF NOT EXISTS `user_profile` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户ID',
  `preference_tags` JSON COMMENT '偏好标签: [{"tag": "川菜", "score": 0.8}, ...]',
  `dietary_restrictions` JSON COMMENT '饮食禁忌: ["过敏原", "宗教禁忌"]',
  `flavor_preference` JSON COMMENT '口味偏好: {"spicy": 0.7, "sweet": 0.3, "salty": 0.5}',
  `price_preference` JSON COMMENT '价格偏好: {"min": 10, "max": 50, "optimal": 25}',
  `nutrition_goals` JSON COMMENT '营养目标: {"calories": 2000, "protein": 100}',
  `meal_pattern` JSON COMMENT '用餐模式: {"breakfast": "07:00", "lunch": "12:00", "dinner": "18:30"}',
  `statistics` JSON COMMENT '统计数据: {total_orders, avg_order_amount, fav_categories}',
  `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';

-- ========================================
-- 3. 菜品特征表 (dish_features)
-- 用途: 菜品的静态和动态特征，用于匹配和相似度计算
-- ========================================
CREATE TABLE IF NOT EXISTS `dish_features` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `dish_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '菜品ID',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '菜品分类',
  `tags` JSON COMMENT '标签数组: ["辣", "川菜", "下饭菜"]',
  `flavor_profile` JSON COMMENT '风味特征: {"spicy": 0.8, "salty": 0.5}',
  `nutrition_info` JSON COMMENT '营养信息: {calories, protein, fat, carbs}',
  `ingredients` JSON COMMENT '主要食材: ["鸡肉", "土豆", "辣椒"]',
  `cooking_method` VARCHAR(50) DEFAULT NULL COMMENT '烹饪方式: 炒/煮/蒸/烤',
  `suitable_scenarios` JSON COMMENT '适用场景: ["工作日", "聚餐", "宵夜"]',
  `time_period_tags` JSON COMMENT '时段标签: ["午餐", "晚餐"]',
  `season_tags` JSON COMMENT '季节标签: ["夏季", "冬季"]',
  `price_level` TINYINT DEFAULT NULL COMMENT '价格等级 1-5',
  `popularity_score` DECIMAL(5,2) DEFAULT NULL COMMENT '热度分数',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_category` (`category`),
  INDEX `idx_popularity` (`popularity_score` DESC),
  INDEX `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品特征表';

-- ========================================
-- 4. 推荐记录表 (recommendation_log)
-- 用途: 记录每次推荐的详情，用于效果分析和离线评估
-- ========================================
CREATE TABLE IF NOT EXISTS `recommendation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` VARCHAR(50) NOT NULL COMMENT '用户ID',
  `recommendation_id` VARCHAR(50) NOT NULL COMMENT '推荐批次ID',
  `dish_id` VARCHAR(50) NOT NULL COMMENT '菜品ID',
  `rank` INT DEFAULT NULL COMMENT '推荐排序位置',
  `score` DECIMAL(10,4) DEFAULT NULL COMMENT '推荐得分',
  `algorithm` VARCHAR(50) DEFAULT NULL COMMENT '使用的算法',
  `reason` JSON COMMENT '推荐理由: {factors: [{type, name, score}]}',
  `is_clicked` BOOLEAN DEFAULT FALSE COMMENT '是否被点击',
  `is_ordered` BOOLEAN DEFAULT FALSE COMMENT '是否被下单',
  `feedback_time` DATETIME DEFAULT NULL COMMENT '反馈时间',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '推荐时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_recommendation_id` (`recommendation_id`),
  INDEX `idx_created_time` (`created_time`),
  INDEX `idx_user_recommendation` (`user_id`, `recommendation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='推荐记录表';

-- ========================================
-- 5. 菜品相似度表 (dish_similarity)
-- 用途: 预计算的菜品相似度，用于协同过滤和"相似菜品"推荐
-- ========================================
CREATE TABLE IF NOT EXISTS `dish_similarity` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `dish_id_a` VARCHAR(50) NOT NULL COMMENT '菜品A ID',
  `dish_id_b` VARCHAR(50) NOT NULL COMMENT '菜品B ID',
  `similarity_score` DECIMAL(5,4) NOT NULL COMMENT '相似度分数 0-1',
  `similarity_type` VARCHAR(20) NOT NULL COMMENT '相似度类型: content(基于内容)/collaborative(协同过滤)/hybrid(混合)',
  `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_dish_pair_type` (`dish_id_a`, `dish_id_b`, `similarity_type`),
  INDEX `idx_similarity_score` (`similarity_score` DESC),
  INDEX `idx_dish_a` (`dish_id_a`),
  INDEX `idx_dish_b` (`dish_id_b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品相似度表';

-- ========================================
-- 优化现有表结构
-- ========================================

-- 为dish表添加统计字段（如果不存在）
ALTER TABLE `dish`
ADD COLUMN IF NOT EXISTS `view_count` INT DEFAULT 0 COMMENT '浏览次数',
ADD COLUMN IF NOT EXISTS `order_count` INT DEFAULT 0 COMMENT '下单次数',
ADD COLUMN IF NOT EXISTS `favorite_count` INT DEFAULT 0 COMMENT '收藏次数',
ADD COLUMN IF NOT EXISTS `avg_rating` DECIMAL(3,2) DEFAULT NULL COMMENT '平均评分',
ADD COLUMN IF NOT EXISTS `tags` JSON COMMENT '标签数组',
ADD INDEX IF NOT EXISTS `idx_view_count` (`view_count` DESC),
ADD INDEX IF NOT EXISTS `idx_order_count` (`order_count` DESC);

-- ========================================
-- 初始化数据
-- ========================================

-- 为现有菜品初始化特征数据（示例）
INSERT INTO `dish_features` (`dish_id`, `category`, `tags`, `flavor_profile`, `cooking_method`, `price_level`, `popularity_score`)
SELECT
  id as dish_id,
  category,
  '[]' as tags,
  '{"spicy": 0.5, "salty": 0.5}' as flavor_profile,
  '炒' as cooking_method,
  CASE
    WHEN price < 15 THEN 1
    WHEN price < 25 THEN 2
    WHEN price < 40 THEN 3
    WHEN price < 60 THEN 4
    ELSE 5
  END as price_level,
  0.5 as popularity_score
FROM `dish`
WHERE NOT EXISTS (SELECT 1 FROM `dish_features` WHERE `dish_features`.`dish_id` = `dish`.`id`);

-- ========================================
-- 创建视图（便于查询）
-- ========================================

-- 用户行为统计视图
CREATE OR REPLACE VIEW `v_user_behavior_stats` AS
SELECT
  user_id,
  behavior_type,
  COUNT(*) as behavior_count,
  COUNT(DISTINCT item_id) as unique_items,
  MIN(created_time) as first_behavior,
  MAX(created_time) as last_behavior
FROM `user_behavior`
GROUP BY user_id, behavior_type;

-- 菜品热度统计视图
CREATE OR REPLACE VIEW `v_dish_popularity` AS
SELECT
  d.id as dish_id,
  d.name as dish_name,
  d.view_count,
  d.order_count,
  d.favorite_count,
  d.avg_rating,
  COALESCE(df.popularity_score, 0) as feature_popularity,
  (d.view_count * 0.3 + d.order_count * 0.5 + d.favorite_count * 0.2) as computed_popularity
FROM `dish` d
LEFT JOIN `dish_features` df ON d.id = df.dish_id;

-- ========================================
-- 创建存储过程（定时任务使用）
-- ========================================

DELIMITER //

-- 存储过程：计算菜品热度分数
CREATE PROCEDURE IF NOT EXISTS `sp_calculate_dish_popularity`()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE v_dish_id VARCHAR(50);
  DECLARE v_view_count INT;
  DECLARE v_order_count INT;
  DECLARE v_fav_count INT;
  DECLARE v_avg_rating DECIMAL(3,2);
  DECLARE v_popularity DECIMAL(5,2);

  DECLARE cursor CURSOR FOR
    SELECT id, view_count, order_count, favorite_count, avg_rating
    FROM `dish`;

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN cursor;

  read_loop: LOOP
    FETCH cursor INTO v_dish_id, v_view_count, v_order_count, v_fav_count, v_avg_rating;

    IF done THEN
      LEAVE read_loop;
    END IF;

    -- 计算热度分数 (0-100)
    SET v_popularity = (
      (LEAST(v_view_count, 1000) / 1000 * 30) +
      (LEAST(v_order_count, 500) / 500 * 50) +
      (LEAST(v_fav_count, 200) / 200 * 20)
    );

    -- 如果有评分，加上评分权重
    IF v_avg_rating IS NOT NULL THEN
      SET v_popularity = v_popularity + (v_avg_rating / 5 * 10);
    END IF;

    -- 限制在0-100范围
    SET v_popularity = GREATEST(LEAST(v_popularity, 100), 0);

    -- 更新到菜品特征表
    INSERT INTO `dish_features` (`dish_id`, `popularity_score`)
    VALUES (v_dish_id, v_popularity / 100)
    ON DUPLICATE KEY UPDATE `popularity_score` v_popularity / 100;

  END LOOP;

  CLOSE cursor;
END //

DELIMITER ;

-- ========================================
-- 执行说明
-- ========================================
-- 1. 在MySQL中执行此脚本：
--    mysql -u root -p jia_shi_yi_xuan < migration_recommendation_system.sql
--
-- 2. 验证表是否创建成功：
--    SHOW TABLES LIKE '%user_behavior%';
--    SHOW TABLES LIKE '%user_profile%';
--    SHOW TABLES LIKE '%dish_features%';
--    SHOW TABLES LIKE '%recommendation_log%';
--    SHOW TABLES LIKE '%dish_similarity%';
--
-- 3. 测试存储过程：
--    CALL sp_calculate_dish_popularity();
--
-- 4. 设置定时任务（可选）：
--    在crontab中添加：
--    0 2 * * * mysql -u root -p密码 jia_shi_yi_xuan -e "CALL sp_calculate_dish_popularity();"
-- ========================================
