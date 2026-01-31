-- =============================================
-- 节日场景化推荐系统 - 数据库迁移脚本
-- 版本：V2025_01_31_1
-- 作者：Claude
-- 描述：添加节日场景化推荐功能
-- =============================================

-- 1. 创建节日表
CREATE TABLE IF NOT EXISTS `t_festival` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `festival_name` VARCHAR(50) NOT NULL COMMENT '节日名称',
  `festival_type` VARCHAR(20) NOT NULL COMMENT '节日类型：TRADITIONAL-传统节日, WESTERN-西方节日, SEASONAL-季节性, CUSTOM-自定义',
  `festival_date` VARCHAR(20) DEFAULT NULL COMMENT '节日日期（MM-dd格式，如01-01）',
  `year` INT DEFAULT NULL COMMENT '年份（NULL表示每年重复）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '节日描述',
  `icon` VARCHAR(200) DEFAULT NULL COMMENT '节日图标',
  `background_image` VARCHAR(500) DEFAULT NULL COMMENT '背景图片',
  `theme_color` VARCHAR(20) DEFAULT NULL COMMENT '主题颜色',
  `start_date` DATE DEFAULT NULL COMMENT '开始日期（用于季节性）',
  `end_date` DATE DEFAULT NULL COMMENT '结束日期（用于季节性）',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `sort_order` INT DEFAULT 0 COMMENT '排序权重',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_festival_type` (`festival_type`),
  INDEX `idx_festival_date` (`festival_date`),
  INDEX `idx_start_end_date` (`start_date`, `end_date`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节日表';

-- 2. 创建节日推荐菜品关联表
CREATE TABLE IF NOT EXISTS `t_festival_dish_recommend` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `festival_id` VARCHAR(32) NOT NULL COMMENT '节日ID',
  `dish_id` VARCHAR(32) NOT NULL COMMENT '菜品ID',
  `recommend_type` VARCHAR(20) NOT NULL COMMENT '推荐类型：MAIN-主推, SECONDARY-次推, THEME-主题, SEASONAL-季节',
  `recommend_reason` VARCHAR(500) DEFAULT NULL COMMENT '推荐理由',
  `position` INT DEFAULT 0 COMMENT '展示位置（0-首页, 1-列表顶, 2- banner）',
  `priority` INT DEFAULT 0 COMMENT '优先级（数字越大越靠前）',
  `click_count` INT DEFAULT 0 COMMENT '点击次数统计',
  `order_count` INT DEFAULT 0 COMMENT '订单次数统计',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_festival_id` (`festival_id`),
  INDEX `idx_dish_id` (`dish_id`),
  INDEX `idx_recommend_type` (`recommend_type`),
  INDEX `idx_priority` (`priority`),
  UNIQUE KEY `uk_festival_dish` (`festival_id`, `dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='节日推荐菜品关联表';

-- 3. 创建用户自定义事件表
CREATE TABLE IF NOT EXISTS `t_user_custom_event` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `event_name` VARCHAR(50) NOT NULL COMMENT '事件名称',
  `event_type` VARCHAR(20) NOT NULL COMMENT '事件类型：BIRTHDAY-生日, ANNIVERSARY-纪念日, PARTY-聚会, OTHER-其他',
  `event_date` VARCHAR(20) NOT NULL COMMENT '事件日期（MM-dd格式）',
  `year` INT DEFAULT NULL COMMENT '年份（NULL表示每年重复）',
  `reminder_days` INT DEFAULT 3 COMMENT '提前提醒天数',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '事件描述',
  `preferred_dishes` JSON DEFAULT NULL COMMENT '偏好菜品列表（JSON数组）',
  `guest_count` INT DEFAULT NULL COMMENT '预计用餐人数',
  `budget_per_person` DECIMAL(10,2) DEFAULT NULL COMMENT '人均预算',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_event_date` (`event_date`),
  INDEX `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自定义事件表';

-- 4. 创建用户推荐记录表（用于个性化推荐）
CREATE TABLE IF NOT EXISTS `t_user_recommend_history` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `festival_id` VARCHAR(32) DEFAULT NULL COMMENT '节日ID',
  `custom_event_id` VARCHAR(32) DEFAULT NULL COMMENT '自定义事件ID',
  `dish_id` VARCHAR(32) NOT NULL COMMENT '菜品ID',
  `recommend_type` VARCHAR(20) NOT NULL COMMENT '推荐类型',
  `is_clicked` TINYINT(1) DEFAULT 0 COMMENT '是否点击',
  `is_ordered` TINYINT(1) DEFAULT 0 COMMENT '是否下单',
  `feedback_score` INT DEFAULT NULL COMMENT '反馈评分（1-5）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_festival_id` (`festival_id`),
  INDEX `idx_custom_event_id` (`custom_event_id`),
  INDEX `idx_dish_id` (`dish_id`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户推荐记录表';

-- =============================================
-- 插入初始节日数据
-- =============================================

-- 传统节日
INSERT INTO `t_festival` (`id`, `festival_name`, `festival_type`, `festival_date`, `description`, `icon`, `theme_color`, `sort_order`) VALUES
(MD5('festival_spring_festival'), '春节', 'TRADITIONAL', '01-01', '中国最重要的传统节日，团圆饭是核心', '🧧', '#E60000', 100),
(MD5('festival_lantern'), '元宵节', 'TRADITIONAL', '01-15', '元宵佳节，吃汤圆赏花灯', '🏮', '#FF6B6B', 90),
(MD5('festival_qingming'), '清明节', 'TRADITIONAL', '04-04', '清明时节，青团春宴', '🌿', '#90EE90', 80),
(MD5('festival_dragon_boat'), '端午节', 'TRADITIONAL', '05-05', '端午佳节，粽子飘香', '🚣', '#4CAF50', 95),
(MD5('festival_qixi'), '七夕节', 'TRADITIONAL', '07-07', '中国情人节，浪漫晚餐', '💕', '#FF69B4', 85),
(MD5('festival_mid_autumn'), '中秋节', 'TRADITIONAL', '08-15', '月圆人团圆，月饼宴', '🥮', '#FFD700', 98),
(MD5('festival_double_ninth'), '重阳节', 'TRADITIONAL', '09-09', '登高赏秋，敬老宴', '🌼', '#FF8C00', 75),
(MD5('festival_winter_solstice'), '冬至', 'TRADITIONAL', '12-21', '冬至大如年，饺子宴', '🥟', '#87CEEB', 88);

-- 西方节日
INSERT INTO `t_festival` (`id`, `festival_name`, `festival_type`, `festival_date`, `description`, `icon`, `theme_color`, `sort_order`) VALUES
(MD5('festival_valentines'), '情人节', 'WESTERN', '02-14', '浪漫情人节，甜蜜双人餐', '🌹', '#FF1493', 92),
(MD5('festival_april_fools'), '愚人节', 'WESTERN', '04-01', '趣味节日，创意菜品', '🤡', '#FFA500', 60),
(MD5('festival_mothers'), '母亲节', 'WESTERN', '05-12', '感恩母亲，温馨家宴', '💐', '#FF69B4', 93),
(MD5('festival_fathers'), '父亲节', 'WESTERN', '06-15', '致敬父亲，豪迈盛宴', '👔', '#4169E1', 91),
(MD5('festival_halloween'), '万圣节', 'WESTERN', '10-31', '万圣夜，搞怪美食', '🎃', '#FF8C00', 70),
(MD5('festival_thanksgiving'), '感恩节', 'WESTERN', '11-28', '感恩盛宴', '🦃', '#D2691E', 82),
(MD5('festival_christmas'), '圣诞节', 'WESTERN', '12-25', '平安夜狂欢，圣诞大餐', '🎄', '#FF0000', 94),
(MD5('festival_new_year'), '元旦', 'WESTERN', '01-01', '新年新气象，跨年宴', '🎆', '#FFD700', 89);

-- 季节性推荐
INSERT INTO `t_festival` (`id`, `festival_name`, `festival_type`, `start_date`, `end_date`, `description`, `icon`, `theme_color`, `sort_order`) VALUES
(MD5('festival_spring'), '春季时令', 'SEASONAL', '2025-03-01', '2025-05-31', '春暖花开，尝鲜正当时', '🌸', '#98FB98', 70),
(MD5('festival_summer'), '夏季消暑', 'SEASONAL', '2025-06-01', '2025-08-31', '炎炎夏日，清凉一夏', '🌞', '#00CED1', 72),
(MD5('festival_autumn'), '秋季滋补', 'SEASONAL', '2025-09-01', '2025-11-30', '金秋时节，滋补养生', '🍂', '#DAA520', 74),
(MD5('festival_winter'), '冬季暖身', 'SEASONAL', '2025-12-01', '2026-02-28', '寒冬腊月，暖心暖胃', '❄️', '#4682B4', 76);

-- =============================================
-- 创建视图：当前生效的节日
-- =============================================
CREATE OR REPLACE VIEW `v_active_festivals` AS
SELECT
    f.*,
    CASE
        WHEN f.festival_type = 'SEASONAL' AND CURDATE() BETWEEN f.start_date AND f.end_date THEN 1
        WHEN f.festival_type != 'SEASONAL' AND
             DATEDIFF(CONCAT(YEAR(CURDATE()), '-', f.festival_date), CURDATE()) BETWEEN 0 AND 7 THEN 1
        ELSE 0
    END AS is_current,
    CASE
        WHEN f.festival_type != 'SEASONAL' THEN
            DATEDIFF(CONCAT(YEAR(CURDATE()), '-', f.festival_date), CURDATE())
        ELSE NULL
    END AS days_until_festival
FROM t_festival f
WHERE f.is_active = 1;

-- =============================================
-- 迁移完成提示
-- =============================================
SELECT '节日场景化推荐系统数据库迁移完成！' AS message;

-- 说明：
-- 1. 支持传统节日、西方节日、季节性推荐、用户自定义事件
-- 2. 节日推荐菜品关联表支持多种推荐类型和优先级
-- 3. 用户推荐记录表用于个性化推荐算法优化
-- 4. 视图v_active_festivals提供当前生效节日的查询
