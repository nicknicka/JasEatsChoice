-- =============================================
-- 视频/文章内容提取系统 - 数据库迁移脚本
-- 版本：V2025_01_31_2
-- 作者：Claude
-- 描述：添加从视频/文章中提取菜品内容的功能
-- =============================================

-- 1. 创建内容源表
CREATE TABLE IF NOT EXISTS `t_content_source` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `content_url` VARCHAR(500) NOT NULL COMMENT '内容URL',
  `content_type` VARCHAR(20) NOT NULL COMMENT '内容类型：VIDEO-视频, ARTICLE-文章, IMAGE-图片',
  `platform` VARCHAR(50) NOT NULL COMMENT '平台：DOUYIN-抖音, XIAOHONGSHU-小红书, BILIBILI-哔哩哔哩, WECHAT-微信, OTHER-其他',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '内容标题',
  `author` VARCHAR(100) DEFAULT NULL COMMENT '作者/UP主',
  `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片URL',
  `video_duration` INT DEFAULT NULL COMMENT '视频时长（秒）',
  `description` TEXT DEFAULT NULL COMMENT '内容描述',
  `is_extracted` TINYINT(1) DEFAULT 0 COMMENT '是否已提取',
  `extraction_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '提取状态：PENDING-待提取, PROCESSING-提取中, SUCCESS-成功, FAILED-失败',
  `extraction_time` DATETIME DEFAULT NULL COMMENT '提取时间',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_platform` (`platform`),
  INDEX `idx_extraction_status` (`extraction_status`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容源表';

-- 2. 创建内容提取表
CREATE TABLE IF NOT EXISTS `t_content_extraction` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `source_id` VARCHAR(32) NOT NULL COMMENT '内容源ID',
  `dish_name` VARCHAR(100) DEFAULT NULL COMMENT '菜品名称',
  `dish_image` VARCHAR(500) DEFAULT NULL COMMENT '菜品图片',
  `description` TEXT DEFAULT NULL COMMENT '菜品描述',
  `ingredients` TEXT DEFAULT NULL COMMENT '食材列表（JSON格式）',
  `steps` TEXT DEFAULT NULL COMMENT '制作步骤（JSON格式）',
  `cooking_time` INT DEFAULT NULL COMMENT '制作时长（分钟）',
  `difficulty` VARCHAR(20) DEFAULT NULL COMMENT '难度：EASY-简单, MEDIUM-中等, HARD-困难',
  `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `calories` INT DEFAULT NULL COMMENT '卡路里',
  `is_published` TINYINT(1) DEFAULT 0 COMMENT '是否已发布为食谱',
  `recipe_id` VARCHAR(32) DEFAULT NULL COMMENT '关联的食谱ID',
  `manual_score` INT DEFAULT NULL COMMENT '人工评分（1-5）',
  `is_verified` TINYINT(1) DEFAULT 0 COMMENT '是否人工验证',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_source_id` (`source_id`),
  INDEX `idx_dish_name` (`dish_name`),
  INDEX `idx_is_published` (`is_published`),
  INDEX `idx_is_verified` (`is_verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容提取表';

-- 3. 创建提取任务表
CREATE TABLE IF NOT EXISTS `t_extraction_task` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `source_id` VARCHAR(32) NOT NULL COMMENT '内容源ID',
  `task_type` VARCHAR(20) NOT NULL COMMENT '任务类型：OCR-图片识别, NLP-文本分析, VIDEO-视频分析',
  `task_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILED-失败',
  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
  `priority` INT DEFAULT 0 COMMENT '优先级（数字越大越优先）',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `result_data` TEXT DEFAULT NULL COMMENT '结果数据（JSON格式）',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_source_id` (`source_id`),
  INDEX `idx_task_status` (`task_status`),
  INDEX `idx_priority` (`priority`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提取任务表';

-- 4. 创建提取配置表
CREATE TABLE IF NOT EXISTS `t_extraction_config` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `platform` VARCHAR(50) NOT NULL COMMENT '平台',
  `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
  `config_value` TEXT DEFAULT NULL COMMENT '配置值',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_key` (`platform`, `config_key`),
  INDEX `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提取配置表';

-- =============================================
-- 插入初始配置数据
-- =============================================

-- 插入平台配置
INSERT INTO `t_extraction_config` (`id`, `platform`, `config_key`, `config_value`, `description`) VALUES
(MD5('config_douyin_api_key'), 'DOUYIN', 'api_key', '', '抖音API密钥'),
(MD5('config_douyin_enabled'), 'DOUYIN', 'enabled', 'true', '是否启用抖音提取'),
(MD5('config_xiaohongshu_enabled'), 'XIAOHONGSHU', 'enabled', 'true', '是否启用小红书提取'),
(MD5('config_bilibili_enabled'), 'BILIBILI', 'enabled', 'true', '是否启用B站提取'),
(MD5('config_wechat_enabled'), 'WECHAT', 'enabled', 'true', '是否启用微信提取'),
(MD5('config_ocr_enabled'), 'SYSTEM', 'ocr_enabled', 'true', '是否启用OCR识别'),
(MD5('config_nlp_enabled'), 'SYSTEM', 'nlp_enabled', 'true', '是否启用NLP分析'),
(MD5('config_auto_publish'), 'SYSTEM', 'auto_publish', 'false', '是否自动发布为食谱');

-- =============================================
-- 创建视图：内容提取概览
-- =============================================
CREATE OR REPLACE VIEW `v_content_extraction_overview` AS
SELECT
    cs.id,
    cs.user_id,
    cs.content_url,
    cs.content_type,
    cs.platform,
    cs.title,
    cs.author,
    cs.cover_image,
    cs.extraction_status,
    ce.dish_name,
    ce.dish_image,
    ce.description,
    ce.ingredients,
    ce.steps,
    ce.cooking_time,
    ce.difficulty,
    ce.tags,
    ce.calories,
    ce.is_published,
    ce.recipe_id,
    ce.is_verified,
    cs.create_time,
    cs.extraction_time
FROM t_content_source cs
LEFT JOIN t_content_extraction ce ON cs.id = ce.source_id
ORDER BY cs.create_time DESC;

-- =============================================
-- 迁移完成提示
-- =============================================
SELECT '视频/文章内容提取系统数据库迁移完成！' AS message;

-- 说明：
-- 1. 支持从抖音、小红书、B站、微信等平台提取内容
-- 2. 支持视频、文章、图片三种内容类型
-- 3. 异步任务队列处理提取请求
-- 4. OCR和NLP技术自动识别菜品信息
-- 5. 人工验证和评分机制
-- 6. 可一键发布为正式食谱
