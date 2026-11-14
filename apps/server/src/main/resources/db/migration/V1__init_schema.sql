-- V1: 初始 schema - 74 张表
-- 来源：db/init/01_schema.sql（mysqldump 导出，已清理 mysqldump 特有语法）
-- 仅包含 CREATE TABLE，不包含视图、触发器、存储过程

SET NAMES utf8mb4;

-- Table: announcement
CREATE TABLE `announcement` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家ID，NULL表示系统公告',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'system' COMMENT '公告类型：system-系统公告, activity-活动公告, urgent-紧急公告, update-更新说明',
  `priority` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT '优先级：normal-普通, important-重要, urgent-紧急',
  `target_audience` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'all' COMMENT '目标群体：all-全部用户, merchant-商家端, customer-用户端',
  `read_count` bigint DEFAULT '0' COMMENT '阅读量',
  `read_users` bigint DEFAULT '0' COMMENT '阅读人数',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '公告状态 (active: 启用, inactive: 禁用)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: discount
CREATE TABLE `discount` (
`id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `discount_value` double DEFAULT NULL COMMENT '优惠力度',
  `min_amount` double DEFAULT NULL COMMENT '最低消费',
  `limit_per_user` int DEFAULT NULL COMMENT '每人限领',
  `usage_notes` text COLLATE utf8mb4_unicode_ci COMMENT '使用说明',
  `validity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '有效期类型',
  `validity_period` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '有效期范围',
  `valid_days` int DEFAULT NULL COMMENT '有效天数',
  `used_count` int DEFAULT '0' COMMENT '使用次数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: dish_features
CREATE TABLE `dish_features` (
`id` bigint NOT NULL AUTO_INCREMENT,
  `dish_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品分类',
  `tags` json DEFAULT NULL COMMENT '标签数组: ["辣", "川菜", "下饭菜"]',
  `flavor_profile` json DEFAULT NULL COMMENT '风味特征: {"spicy": 0.8, "salty": 0.5}',
  `nutrition_info` json DEFAULT NULL COMMENT '营养信息: {calories, protein, fat, carbs}',
  `ingredients` json DEFAULT NULL COMMENT '主要食材: ["鸡肉", "土豆", "辣椒"]',
  `cooking_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '烹饪方式: 炒/煮/蒸/烤',
  `suitable_scenarios` json DEFAULT NULL COMMENT '适用场景: ["工作日", "聚餐", "宵夜"]',
  `time_period_tags` json DEFAULT NULL COMMENT '时段标签: ["午餐", "晚餐"]',
  `season_tags` json DEFAULT NULL COMMENT '季节标签: ["夏季", "冬季"]',
  `price_level` tinyint DEFAULT NULL COMMENT '价格等级 1-5',
  `popularity_score` decimal(5,2) DEFAULT NULL COMMENT '热度分数',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dish_id` (`dish_id`),
  KEY `idx_category` (`category`),
  KEY `idx_popularity` (`popularity_score` DESC),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: dish_similarity
CREATE TABLE `dish_similarity` (
`id` bigint NOT NULL AUTO_INCREMENT,
  `dish_id_a` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品A ID',
  `dish_id_b` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品B ID',
  `similarity_score` decimal(5,4) NOT NULL COMMENT '相似度分数 0-1',
  `similarity_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '相似度类型: content(基于内容)/collaborative(协同过滤)/hybrid(混合)',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dish_pair_type` (`dish_id_a`,`dish_id_b`,`similarity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: hot_topic
CREATE TABLE `hot_topic` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '热点内容',
  `priority` int DEFAULT '0' COMMENT '优先级',
  `source_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'MANUAL' COMMENT '来源类型',
  `source_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源ID',
  `start_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生效开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '生效结束时间',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT '状态',
  `click_count` int DEFAULT '0' COMMENT '点击次数',
  `share_count` int DEFAULT '0' COMMENT '分享次数',
  `require_review` tinyint(1) DEFAULT '0' COMMENT '是否需要审核',
  `review_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'APPROVED' COMMENT '审核状态',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核意见',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: j_food_categories
CREATE TABLE `j_food_categories` (
`id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '食物分类名称',
  `father_id` varchar(50) DEFAULT NULL COMMENT '父级分类ID',
  `cate_id` varchar(50) DEFAULT NULL COMMENT '当前分类ID',
  `is_subcategory` tinyint(1) DEFAULT '0' COMMENT '是否为子分类',
  `parent_category_id` int DEFAULT NULL COMMENT '父分类在数据库中的ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: j_food_nutrition
CREATE TABLE `j_food_nutrition` (
`id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cate_id` int DEFAULT '0',
  `food_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '食物名称',
  `alias_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '别名或俗名',
  `english_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '英文名称',
  `edible_part` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '食部(可食部分比例%)',
  `water` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '水分',
  `energy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '能量',
  `protein` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '蛋白质',
  `fat` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '脂肪',
  `cholesterol` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '胆固醇',
  `ash` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '灰分',
  `carbohydrate` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '碳水化合物',
  `dietary_fiber` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '总膳食纤维',
  `carotene` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '胡萝卜素',
  `vitamin_a` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '维生素A',
  `vitamin_e` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'α-TE',
  `thiamin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '硫胺素',
  `riboflavin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '核黄素',
  `niacin` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '烟酸',
  `vitamin_c` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '维生素C',
  `calcium` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '钙',
  `phosphorus` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '磷',
  `potassium` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '钾',
  `sodium` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '钠',
  `magnesium` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '镁',
  `iron` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '铁',
  `zinc` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '锌',
  `selenium` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '硒',
  `copper` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '铜',
  `manganese` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '锰',
  `iodine` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '碘',
  `sfa` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '饱和脂肪酸',
  `mufa` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '单不饱和脂肪酸',
  `pufa` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '多不饱和脂肪酸',
  `fatty_acids_total` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '脂肪酸合计',
  PRIMARY KEY (`id`),
  KEY `idx_food_name` (`food_name`),
  KEY `idx_english_name` (`english_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: recommendation_log
CREATE TABLE `recommendation_log` (
`id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `recommendation_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推荐批次ID',
  `dish_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `rank` int DEFAULT NULL COMMENT '推荐排序位置',
  `score` decimal(10,4) DEFAULT NULL COMMENT '推荐得分',
  `algorithm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用的算法',
  `reason` json DEFAULT NULL COMMENT '推荐理由: {factors: [{type, name, score}]}',
  `is_clicked` tinyint(1) DEFAULT '0' COMMENT '是否被点击',
  `is_ordered` tinyint(1) DEFAULT '0' COMMENT '是否被下单',
  `feedback_time` datetime DEFAULT NULL COMMENT '反馈时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '推荐时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_recommendation_id` (`recommendation_id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_user_recommendation` (`user_id`,`recommendation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: system_config
CREATE TABLE `system_config` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `config_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置名称',
  `config_group` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'system' COMMENT '配置分组',
  `config_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'string' COMMENT '配置类型：string-字符串, number-数字, boolean-布尔, json-JSON对象',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置描述',
  `is_system` tinyint(1) DEFAULT '0' COMMENT '是否系统内置',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT '状态：active-启用, inactive-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`),
  KEY `idx_config_group` (`config_group`),
  KEY `idx_config_key` (`config_key`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_add_dish_request
CREATE TABLE `t_add_dish_request` (
`id` varchar(64) NOT NULL COMMENT '加菜请求ID',
  `group_order_id` varchar(64) NOT NULL COMMENT '群订单ID',
  `original_order_id` varchar(64) DEFAULT NULL COMMENT '原订单ID(关联已支付订单)',
  `request_user_id` varchar(64) NOT NULL COMMENT '加菜请求人ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `dish_info` json NOT NULL COMMENT '加菜菜品信息JSON: [{dishId,quantity,customization,price}]',
  `total_amount` decimal(10,2) NOT NULL COMMENT '加菜总金额',
  `approval_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态: 0-待审核,1-审核通过,2-审核驳回,3-已撤回,4-超时驳回',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `reviewer_id` varchar(64) DEFAULT NULL COMMENT '审核人ID(群订单发起者)',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `first_remind_time` datetime DEFAULT NULL COMMENT '首次提醒时间(10分钟)',
  `second_remind_time` datetime DEFAULT NULL COMMENT '二次提醒时间',
  `timeout_time` datetime DEFAULT NULL COMMENT '超时时间(15分钟)',
  `related_payment_id` varchar(64) DEFAULT NULL COMMENT '关联支付记录ID',
  `related_order_id` varchar(64) DEFAULT NULL COMMENT '关联订单ID(审核通过后创建)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_timeout_time` (`timeout_time`),
  KEY `idx_request_user` (`request_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_add_dish_setting
CREATE TABLE `t_add_dish_setting` (
`id` varchar(64) NOT NULL COMMENT '主键ID',
  `group_order_id` varchar(64) NOT NULL COMMENT '群订单ID',
  `add_dish_permission` tinyint NOT NULL DEFAULT '0' COMMENT '加菜权限: 0-全员可加菜,1-仅发起者可加菜',
  `budget_limit` decimal(10,2) DEFAULT NULL COMMENT '单次加菜预算限制(可选)',
  `max_dish_count` int DEFAULT NULL COMMENT '单次加菜数量限制(可选)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_order_id` (`group_order_id`),
  KEY `idx_group_order_id` (`group_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_address
CREATE TABLE `t_address` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认地址（0-否/1-是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_admin
CREATE TABLE `t_admin` (
`admin_id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) NOT NULL COMMENT '管理员用户名',
  `password` varchar(64) NOT NULL COMMENT '密码（加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃, LOCKED-锁定, DELETED-删除',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID（关联t_role表）',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_admin_operation_log
CREATE TABLE `t_admin_operation_log` (
`log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` bigint DEFAULT NULL COMMENT '管理员ID',
  `username` varchar(50) DEFAULT NULL COMMENT '管理员用户名',
  `operation_type` varchar(50) DEFAULT NULL COMMENT '操作类型：LOGIN, LOGOUT, CREATE, UPDATE, DELETE, AUDIT等',
  `module_name` varchar(50) DEFAULT NULL COMMENT '模块名称',
  `operation_desc` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法：GET, POST, PUT, DELETE',
  `request_url` varchar(255) DEFAULT NULL COMMENT '请求URL',
  `request_params` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `execute_time` int DEFAULT NULL COMMENT '执行时长（毫秒）',
  `status` varchar(20) DEFAULT NULL COMMENT '状态：SUCCESS-成功, FAIL-失败',
  `error_message` text COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_admin_role_relation
CREATE TABLE `t_admin_role_relation` (
`relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `admin_id` bigint NOT NULL COMMENT '管理员ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`,`role_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_ai_chat_history
CREATE TABLE `t_ai_chat_history` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `sender` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者：user-用户，ai-AI助手',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `message_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '消息类型（用于卡片显示）',
  `card_data` text COLLATE utf8mb4_unicode_ci COMMENT '卡片数据（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_banner
CREATE TABLE `t_banner` (
`id` varchar(32) NOT NULL COMMENT '轮播图ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `type` varchar(20) DEFAULT 'link' COMMENT '类型: link-外部链接, dish-菜品, merchant-商家, activity-活动',
  `target_type` varchar(50) DEFAULT '' COMMENT '跳转目标类型',
  `target_id` varchar(32) DEFAULT '' COMMENT '跳转目标ID',
  `link` varchar(500) DEFAULT '' COMMENT '外部链接',
  `position` varchar(20) DEFAULT 'home' COMMENT '位置: home-首页, merchant-商家页, dish-菜品页',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `status` int DEFAULT '1' COMMENT '状态: 0-禁用, 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_calorie_record
CREATE TABLE `t_calorie_record` (
`id` varchar(64) NOT NULL COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dish_id` bigint DEFAULT NULL COMMENT '菜品ID',
  `calorie` int NOT NULL COMMENT '卡路里',
  `protein` double DEFAULT '0' COMMENT '蛋白质含量(g)',
  `fat` double DEFAULT '0' COMMENT '脂肪含量(g)',
  `carbohydrate` double DEFAULT '0' COMMENT '碳水化合物含量(g)',
  `meal_time` varchar(20) NOT NULL COMMENT '餐次（breakfast/lunch/dinner/snack）',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `food_name` varchar(255) DEFAULT NULL COMMENT '食物名称',
  `description` varchar(500) DEFAULT NULL COMMENT '食物描述',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`record_time`),
  KEY `idx_meal_time` (`meal_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_chat_msg
CREATE TABLE `t_chat_msg` (
`msg_id` varchar(64) NOT NULL,
  `from_id` varchar(64) NOT NULL COMMENT '发送者ID',
  `to_id` varchar(64) NOT NULL COMMENT '接收者ID',
  `session_id` varchar(64) DEFAULT NULL COMMENT '会话ID(私聊为生成的会话标识,群聊为群ID)',
  `msg_type` varchar(20) NOT NULL COMMENT '消息类型：text/image/file',
  `session_type` varchar(20) NOT NULL COMMENT '会话类型：single/group',
  `content` text NOT NULL COMMENT '消息内容',
  `reply_to` varchar(64) DEFAULT NULL COMMENT '回复的消息ID',
  `reply_content` text COMMENT '被回复的消息内容',
  `reply_from_id` varchar(64) DEFAULT NULL COMMENT '被回复消息的发送者ID',
  `reply_from_name` varchar(100) DEFAULT NULL COMMENT '被回复消息的发送者名称',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态:0-未读,1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `file_url` varchar(512) DEFAULT NULL COMMENT '文件URL（图片/文件消息时使用）',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名（文件消息时使用）',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  PRIMARY KEY (`msg_id`),
  KEY `idx_from_to_time` (`from_id`,`to_id`,`create_time`),
  KEY `idx_to_read` (`to_id`,`read_status`),
  KEY `idx_reply_to` (`reply_to`),
  KEY `idx_session_time` (`session_id`,`create_time`),
  KEY `idx_file_url` (`file_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_chat_session
CREATE TABLE `t_chat_session` (
`id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) NOT NULL COMMENT '会话标识（私聊为双方用户ID组合，群聊为群ID）',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `session_type` varchar(20) NOT NULL COMMENT '会话类型: single-私聊, group-群聊',
  `session_name` varchar(100) NOT NULL COMMENT '会话名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '会话头像',
  `last_message` text COMMENT '最后一条消息内容',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后一条消息时间',
  `unread_count` int DEFAULT '0' COMMENT '未读消息数',
  `pinned` tinyint(1) DEFAULT '0' COMMENT '是否置顶（0-未置顶，1-置顶）',
  `member_count` int DEFAULT '0' COMMENT '成员数量（群聊）',
  `group_id` varchar(64) DEFAULT NULL COMMENT '群组ID（仅群聊会话有效）',
  `target_id` varchar(255) DEFAULT NULL COMMENT '目标用户ID（仅单聊会话有效，表示对方的userId）',
  `related_order_id` varchar(64) DEFAULT NULL COMMENT '关联的群订单ID（群订单专属会话有效）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_session` (`user_id`,`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_session_type` (`session_type`),
  KEY `idx_last_message_time` (`last_message_time`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_collection
CREATE TABLE `t_collection` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `collectable_type` varchar(20) NOT NULL COMMENT '收藏类型：merchant, dish, article, recipe等',
  `collectable_id` bigint NOT NULL COMMENT '收藏对象ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type_id` (`collectable_type`,`collectable_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_consume_history
CREATE TABLE `t_consume_history` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '类型：recharge（充值）、consume（消费）、withdraw（提现）',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `status` varchar(20) NOT NULL COMMENT '状态：success（成功）、failed（失败）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_contact
CREATE TABLE `t_contact` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `target_id` varchar(64) NOT NULL COMMENT '目标ID（好友ID或群ID）',
  `relation_type` varchar(20) NOT NULL COMMENT '关系类型：friend(好友)、group(群成员)',
  `status` varchar(20) NOT NULL COMMENT '关系状态：normal(正常)、pending(待验证)、blocked(已拉黑)',
  `role` varchar(20) DEFAULT NULL COMMENT '群角色：creator(创建者)、admin(管理员)、member(普通成员)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_relation_type` (`relation_type`),
  KEY `idx_status` (`status`),
  KEY `idx_user_target_relation` (`user_id`,`target_id`,`relation_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_content_extraction
CREATE TABLE `t_content_extraction` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `source_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容源ID',
  `dish_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品名称',
  `dish_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品图片',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '菜品描述',
  `ingredients` text COLLATE utf8mb4_unicode_ci COMMENT '食材列表（JSON格式）',
  `steps` text COLLATE utf8mb4_unicode_ci COMMENT '制作步骤（JSON格式）',
  `steps_description` text COLLATE utf8mb4_unicode_ci COMMENT '步骤整体描述',
  `cooking_time` int DEFAULT NULL COMMENT '制作时长（分钟）',
  `difficulty` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '难度：EASY-简单, MEDIUM-中等, HARD-困难',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签（逗号分隔）',
  `calories` int DEFAULT NULL COMMENT '卡路里',
  `is_published` tinyint(1) DEFAULT '0' COMMENT '是否已发布为食谱',
  `recipe_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联的食谱ID',
  `manual_score` int DEFAULT NULL COMMENT '人工评分（1-5）',
  `is_verified` tinyint(1) DEFAULT '0' COMMENT '是否人工验证',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_dish_name` (`dish_name`),
  KEY `idx_is_published` (`is_published`),
  KEY `idx_is_verified` (`is_verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_content_source
CREATE TABLE `t_content_source` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `content_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容URL',
  `content_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容类型：VIDEO-视频, ARTICLE-文章, IMAGE-图片',
  `platform` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台：DOUYIN-抖音, XIAOHONGSHU-小红书, BILIBILI-哔哩哔哩, WECHAT-微信, OTHER-其他',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容标题',
  `author` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者/UP主',
  `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片URL',
  `video_duration` int DEFAULT NULL COMMENT '视频时长（秒）',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '内容描述',
  `is_extracted` tinyint(1) DEFAULT '0' COMMENT '是否已提取',
  `extraction_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '提取状态：PENDING-待提取, PROCESSING-提取中, SUCCESS-成功, FAILED-失败',
  `extraction_time` datetime DEFAULT NULL COMMENT '提取时间',
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_platform` (`platform`),
  KEY `idx_extraction_status` (`extraction_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_dish
CREATE TABLE `t_dish` (
`id` varchar(64) NOT NULL COMMENT '菜品ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜品名称',
  `category` varchar(50) NOT NULL DEFAULT '其他' COMMENT '菜品分类',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `calorie` int NOT NULL COMMENT '卡路里',
  `estimated_cooking_minutes` int DEFAULT '15' COMMENT '预估烹饪时长（分钟）',
  `step_template` varchar(20) DEFAULT 'NORMAL' COMMENT '烹饪流程模板：NORMAL-正餐流程，FAST-快餐流程，CUSTOM-自定义流程',
  `ingredients` json DEFAULT NULL COMMENT '食材列表（JSON格式）',
  `description` text COMMENT '菜品描述',
  `cooking_steps` text COMMENT '烹饪步骤（JSON格式）',
  `nutrition` json DEFAULT NULL COMMENT '营养信息（JSON格式）',
  `image` varchar(500) DEFAULT NULL COMMENT '菜品图片URL',
  `score` decimal(5,2) DEFAULT NULL COMMENT '推荐得分',
  `is_online` tinyint(1) DEFAULT '1' COMMENT '是否上架：1-上架，0-下架',
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝',
  `audit_comment` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_admin_id` bigint DEFAULT NULL COMMENT '审核管理员ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  `order_count` int DEFAULT '0' COMMENT '下单次数',
  `favorite_count` int DEFAULT '0' COMMENT '收藏次数',
  `avg_rating` decimal(3,2) DEFAULT NULL COMMENT '平均评分',
  `tags` json DEFAULT NULL COMMENT '标签数组',
  `stock` int DEFAULT '-1' COMMENT '库存数量：-1表示不限量，>=0表示实际库存',
  `is_fast_food_virtual` tinyint(1) GENERATED ALWAYS AS ((case when (`step_template` = _utf8mb4'FAST') then 1 else 0 end)) VIRTUAL COMMENT '是否为快餐（虚拟字段，自动计算）',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_category` (`category`),
  KEY `idx_calorie` (`calorie`),
  KEY `idx_view_count` (`view_count` DESC),
  KEY `idx_order_count` (`order_count` DESC),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_merchant_audit_time` (`merchant_id`,`audit_status`,`create_time`),
  KEY `idx_category_online_rating` (`category`,`is_online`,`avg_rating`),
  KEY `idx_view_order_count` (`view_count` DESC,`order_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_dish_step_config
CREATE TABLE `t_dish_step_config` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `merchant_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品ID（为NULL表示全局默认配置）',
  `step_code` int NOT NULL COMMENT '步骤代码',
  `step_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '步骤名称',
  `step_order` int NOT NULL COMMENT '步骤顺序',
  `estimated_minutes` int DEFAULT NULL COMMENT '预计耗时（分钟）',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用：1-启用 0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_dish_step` (`merchant_id`,`dish_id`,`step_code`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_dish_step_history
CREATE TABLE `t_dish_step_history` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `order_dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单菜品ID',
  `order_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `old_step_status` int DEFAULT NULL COMMENT '原步骤状态',
  `new_step_status` int NOT NULL COMMENT '新步骤状态',
  `operation_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作类型：FORWARD-前进, BACKWARD-回退, SKIP-跳过',
  `operator_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人姓名',
  `rollback_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回退原因',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `estimated_minutes` int DEFAULT NULL COMMENT '预计完成时间（分钟）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_dish_id` (`order_dish_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_extraction_config
CREATE TABLE `t_extraction_config` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `platform` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台',
  `config_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_key` (`platform`,`config_key`),
  KEY `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_extraction_task
CREATE TABLE `t_extraction_task` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `source_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容源ID',
  `task_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型：OCR-图片识别, NLP-文本分析, VIDEO-视频分析',
  `task_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILED-失败',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `priority` int DEFAULT '0' COMMENT '优先级（数字越大越优先）',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `result_data` text COLLATE utf8mb4_unicode_ci COMMENT '结果数据（JSON格式）',
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_festival
CREATE TABLE `t_festival` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `festival_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节日名称',
  `festival_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节日类型：TRADITIONAL-传统节日, WESTERN-西方节日, SEASONAL-季节性, CUSTOM-自定义',
  `festival_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节日日期（MM-dd格式，如01-01）',
  `year` int DEFAULT NULL COMMENT '年份（NULL表示每年重复）',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节日描述',
  `icon` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节日图标',
  `background_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '背景图片',
  `theme_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主题颜色',
  `start_date` date DEFAULT NULL COMMENT '开始日期（用于季节性）',
  `end_date` date DEFAULT NULL COMMENT '结束日期（用于季节性）',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `sort_order` int DEFAULT '0' COMMENT '排序权重',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_festival_type` (`festival_type`),
  KEY `idx_festival_date` (`festival_date`),
  KEY `idx_start_end_date` (`start_date`,`end_date`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_festival_dish_recommend
CREATE TABLE `t_festival_dish_recommend` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `festival_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节日ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `recommend_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推荐类型：MAIN-主推, SECONDARY-次推, THEME-主题, SEASONAL-季节',
  `recommend_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐理由',
  `position` int DEFAULT '0' COMMENT '展示位置（0-首页, 1-列表顶, 2- banner）',
  `priority` int DEFAULT '0' COMMENT '优先级（数字越大越靠前）',
  `click_count` int DEFAULT '0' COMMENT '点击次数统计',
  `order_count` int DEFAULT '0' COMMENT '订单次数统计',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_festival_dish` (`festival_id`,`dish_id`),
  KEY `idx_festival_id` (`festival_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_recommend_type` (`recommend_type`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_group
CREATE TABLE `t_group` (
`id` varchar(64) NOT NULL COMMENT '群组ID',
  `group_name` varchar(100) NOT NULL COMMENT '群名称',
  `creator_id` varchar(64) NOT NULL COMMENT '创建者ID',
  `description` varchar(500) DEFAULT NULL COMMENT '群描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_group_member
CREATE TABLE `t_group_member` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(64) NOT NULL COMMENT '群ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `role` varchar(20) DEFAULT 'member' COMMENT '角色：owner-群主，admin-管理员，member-普通成员',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_group_order
CREATE TABLE `t_group_order` (
`id` varchar(64) NOT NULL COMMENT '群订单ID',
  `initiator_id` varchar(64) NOT NULL,
  `merchant_id` varchar(64) DEFAULT NULL,
  `group_id` varchar(64) NOT NULL,
  `address_id` varchar(64) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
  `status` tinyint DEFAULT '0' COMMENT '订单状态（0-待支付/1-待接单/2-制作中/3-已完成/4-已取消）',
  `payment_mode` tinyint NOT NULL DEFAULT '0' COMMENT '支付模式: 0-统一支付,1-个人单独支付',
  `add_dish_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否允许加菜: 0-否,1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `total_amount` double DEFAULT '0' COMMENT '订单总金额',
  `max_participants` int NOT NULL DEFAULT '1' COMMENT '最大参与人数',
  `locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已锁单：0-未锁单，1-已锁单',
  `confirmed_time` datetime DEFAULT NULL COMMENT '确认成团时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_group_order_addition
CREATE TABLE `t_group_order_addition` (
`id` varchar(32) NOT NULL COMMENT '加菜记录ID',
  `group_order_id` varchar(32) NOT NULL COMMENT '群订单ID',
  `group_id` varchar(32) NOT NULL COMMENT '群组ID',
  `user_id` varchar(32) NOT NULL COMMENT '加菜用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '加菜用户姓名',
  `dishes` json NOT NULL COMMENT '加菜菜品列表',
  `total_amount` decimal(10,2) NOT NULL COMMENT '加菜总额',
  `status` varchar(30) NOT NULL DEFAULT 'pending_review' COMMENT '状态：pending_review(待审核)/approved_pending_payment(审核通过待支付)/rejected(已驳回)/paid(已支付)',
  `request_time` datetime NOT NULL COMMENT '请求时间',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
  `check_diet_restrictions` json DEFAULT NULL COMMENT '饮食禁忌检查结果',
  `related_order_id` varchar(32) DEFAULT NULL COMMENT '关联的加菜订单ID',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_request_time` (`request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_group_order_dish
CREATE TABLE `t_group_order_dish` (
`id` varchar(64) NOT NULL COMMENT 'ID',
  `group_order_id` varchar(64) NOT NULL COMMENT '群订单ID',
  `dish_id` varchar(64) NOT NULL COMMENT '菜品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `customization` varchar(255) DEFAULT NULL COMMENT '定制要求',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_group_order_member
CREATE TABLE `t_group_order_member` (
`id` varchar(64) NOT NULL COMMENT '主键ID',
  `group_order_id` varchar(64) NOT NULL COMMENT '拼单ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `role` varchar(32) NOT NULL DEFAULT 'member' COMMENT '成员角色：initiator-发起人，member-普通成员',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `pay_status` varchar(32) NOT NULL DEFAULT 'pending' COMMENT '支付状态：pending-待支付，paid-已支付，refund-已退款',
  `paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '累计实付金额',
  `leave_status` tinyint NOT NULL DEFAULT '0' COMMENT '离开状态：0-在拼单中，1-已退出',
  `leave_time` datetime DEFAULT NULL COMMENT '退出时间',
  `invite_by` varchar(64) DEFAULT NULL COMMENT '邀请人用户ID',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_order_member_order_user` (`group_order_id`,`user_id`),
  KEY `idx_group_order_member_order_id` (`group_order_id`),
  KEY `idx_group_order_member_user_id` (`user_id`),
  KEY `idx_group_order_member_leave_status` (`leave_status`),
  KEY `idx_group_order_member_pay_status` (`pay_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_ingredient_conflict_rule
CREATE TABLE `t_ingredient_conflict_rule` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则ID',
  `rule_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `conflict_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '冲突类型：ALLERGY-过敏, INCOMPATIBLE-食材冲突, CUISINE-烹饪禁忌',
  `main_ingredients` json NOT NULL COMMENT '主要食材（JSON数组，如["辣椒", "辣椒粉"]）',
  `conflict_tags` json NOT NULL COMMENT '冲突标签（JSON数组，如["mild_no_spicy", "no_spicy"]）',
  `severity` int DEFAULT '2' COMMENT '严重程度：1-低, 2-中, 3-高',
  `priority` int DEFAULT '3' COMMENT '推荐优先级：1-高（红色）, 2-中高（黄色）, 3-中（蓝色）, 4-低（灰色）',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '冲突描述',
  `suggestion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '建议内容',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用：1-启用 0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_conflict_type` (`conflict_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_menu
CREATE TABLE `t_menu` (
`id` varchar(64) NOT NULL COMMENT '菜单ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜单名称',
  `type` varchar(20) NOT NULL COMMENT '菜单类型（workday/weekend/afternoon_tea/package等）',
  `description` text COMMENT '菜单描述',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '菜单状态（active/draft/inactive）',
  `auto_start_time` datetime DEFAULT NULL COMMENT '自动上架时间',
  `auto_end_time` datetime DEFAULT NULL COMMENT '自动下架时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_type_status` (`type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_menu_dish
CREATE TABLE `t_menu_dish` (
`id` varchar(64) NOT NULL COMMENT '菜单菜品关联ID',
  `menu_id` varchar(64) NOT NULL COMMENT '菜单ID',
  `dish_id` varchar(64) NOT NULL COMMENT '菜品ID',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` int DEFAULT '1' COMMENT '菜品在菜单中的状态：1-上架，0-下架',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_dish` (`menu_id`,`dish_id`),
  KEY `idx_menu` (`menu_id`),
  KEY `idx_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_merchant
CREATE TABLE `t_merchant` (
`id` varchar(64) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '商家名称',
  `address` varchar(255) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `category` varchar(50) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `business_hours` json DEFAULT NULL,
  `average_price` decimal(10,2) DEFAULT NULL COMMENT '人均消费',
  `status` tinyint(1) DEFAULT '1' COMMENT '商家状态（0-关闭/1-营业）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `business_license` varchar(255) DEFAULT NULL COMMENT '营业执照',
  `business_scope` json DEFAULT NULL,
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人姓名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '商家头像',
  `album` json DEFAULT NULL COMMENT '商家相册',
  `rating` decimal(3,2) DEFAULT '0.00',
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝',
  `audit_reason` varchar(500) DEFAULT NULL COMMENT '审核备注/原因',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_by` varchar(50) DEFAULT NULL COMMENT '审核人ID',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_location` (`longitude`,`latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_message_record
CREATE TABLE `t_message_record` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` bigint DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '消息内容',
  `message_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'text' COMMENT '消息类型 (text: 文本, image: 图片, file: 文件)',
  `send_time` datetime DEFAULT NULL COMMENT '发送时间',
  `read_status` tinyint DEFAULT '0' COMMENT '阅读状态 (0: 未读, 1: 已读)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_mock_oauth_user
CREATE TABLE `t_mock_oauth_user` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `provider` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'wechat/qq',
  `open_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟OpenID',
  `union_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模拟UnionID',
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟昵称',
  `avatar_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模拟头像',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模拟授权密码（演示用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_openid` (`provider`,`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_notification
CREATE TABLE `t_notification` (
`id` varchar(64) NOT NULL COMMENT '通知ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `type` varchar(64) DEFAULT 'system' COMMENT '通知类型',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态（0-未读/1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_nutrition
CREATE TABLE `t_nutrition` (
`id` varchar(32) NOT NULL COMMENT '主键ID',
  `food_code` varchar(20) DEFAULT NULL COMMENT '食物编码',
  `food_name` varchar(100) NOT NULL COMMENT '食物名称',
  `edible` decimal(5,2) DEFAULT NULL COMMENT '可食部(%)',
  `energy_kcal` decimal(10,2) DEFAULT NULL COMMENT '能量(千卡)',
  `energy_kj` decimal(10,2) DEFAULT NULL COMMENT '能量(千焦)',
  `water` decimal(10,2) DEFAULT NULL COMMENT '水分(g)',
  `protein` decimal(10,2) DEFAULT NULL COMMENT '蛋白质(g)',
  `fat` decimal(10,2) DEFAULT NULL COMMENT '脂肪(g)',
  `cho` decimal(10,2) DEFAULT NULL COMMENT '碳水化合物(g)',
  `dietary_fiber` decimal(10,2) DEFAULT NULL COMMENT '膳食纤维(g)',
  `cholesterol` decimal(10,2) DEFAULT NULL COMMENT '胆固醇(mg)',
  `ash` decimal(10,2) DEFAULT NULL COMMENT '灰分(g)',
  `vitamin_a` decimal(10,2) DEFAULT NULL COMMENT '维生素A(μgRE)',
  `carotene` decimal(10,2) DEFAULT NULL COMMENT '胡萝卜素(μg)',
  `retinol` decimal(10,2) DEFAULT NULL COMMENT '视黄醇(μg)',
  `thiamin` decimal(10,2) DEFAULT NULL COMMENT '硫胺素(mg)',
  `riboflavin` decimal(10,2) DEFAULT NULL COMMENT '核黄素(mg)',
  `niacin` decimal(10,2) DEFAULT NULL COMMENT '烟酸(mg)',
  `vitamin_c` decimal(10,2) DEFAULT NULL COMMENT '维生素C(mg)',
  `vitamin_e_total` decimal(10,2) DEFAULT NULL COMMENT '维生素E总(mg)',
  `vitamin_e_1` decimal(10,2) DEFAULT NULL COMMENT 'α-维生素E(mg)',
  `vitamin_e_2` decimal(10,2) DEFAULT NULL COMMENT 'β+γ-维生素E(mg)',
  `vitamin_e_3` decimal(10,2) DEFAULT NULL COMMENT 'δ-维生素E(mg)',
  `ca` decimal(10,2) DEFAULT NULL COMMENT '钙(mg)',
  `p` decimal(10,2) DEFAULT NULL COMMENT '磷(mg)',
  `k` decimal(10,2) DEFAULT NULL COMMENT '钾(mg)',
  `na` decimal(10,2) DEFAULT NULL COMMENT '钠(mg)',
  `mg` decimal(10,2) DEFAULT NULL COMMENT '镁(mg)',
  `fe` decimal(10,2) DEFAULT NULL COMMENT '铁(mg)',
  `zn` decimal(10,2) DEFAULT NULL COMMENT '锌(mg)',
  `se` decimal(10,2) DEFAULT NULL COMMENT '硒(μg)',
  `cu` decimal(10,2) DEFAULT NULL COMMENT '铜(mg)',
  `mn` decimal(10,2) DEFAULT NULL COMMENT '锰(mg)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注说明',
  `data_source` varchar(50) DEFAULT '中国食物成分表第6版' COMMENT '数据来源',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `food_code` (`food_code`),
  KEY `idx_food_name` (`food_name`),
  KEY `idx_energy_kcal` (`energy_kcal`),
  KEY `idx_protein` (`protein`),
  KEY `idx_fat` (`fat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_order
CREATE TABLE `t_order` (
`id` varchar(50) NOT NULL COMMENT '订单ID',
  `parent_order_id` varchar(64) DEFAULT NULL COMMENT '父订单ID(加菜订单关联原订单)',
  `is_add_order` tinyint NOT NULL DEFAULT '0' COMMENT '是否为加菜订单: 0-否,1-是',
  `add_dish_request_id` varchar(64) DEFAULT NULL COMMENT '加菜请求ID',
  `add_dish_user_id` bigint DEFAULT NULL COMMENT '加菜人ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `merchant_id` varchar(50) NOT NULL COMMENT '商家ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态（0-待支付/1-待接单/2-制作中/3-已完成/4-已取消）',
  `payment_id` varchar(50) DEFAULT NULL COMMENT '支付记录ID',
  `paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已支付金额',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `address` varchar(255) NOT NULL COMMENT '配送地址',
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `address_id` varchar(50) DEFAULT NULL COMMENT '配送地址ID',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_id` (`payment_id`),
  KEY `idx_parent_order` (`parent_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_order_dish
CREATE TABLE `t_order_dish` (
`id` varchar(64) NOT NULL,
  `order_id` varchar(64) NOT NULL,
  `dish_id` varchar(64) NOT NULL,
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `customization` varchar(255) DEFAULT NULL COMMENT '定制要求',
  `step_status` int DEFAULT '0' COMMENT '步骤状态：0-待备菜 1-备菜中 2-预处理中 3-烹饪中 4-摆盘中 5-待上菜 6-已上菜 10-快餐制作中 11-快餐打包中 12-快餐待出餐 13-快餐已出餐',
  `step_start_time` datetime DEFAULT NULL COMMENT '当前步骤开始时间',
  `estimated_completion_time` datetime DEFAULT NULL COMMENT '预计完成时间',
  `cooking_minutes` int DEFAULT NULL COMMENT '烹饪耗时（分钟）',
  `step_sort` int DEFAULT '999' COMMENT '步骤排序（优先级），数值越小越优先处理',
  `is_fast_food` tinyint(1) DEFAULT '0' COMMENT '是否为快餐：1-是 0-否',
  `serving_status` int DEFAULT '0' COMMENT '上菜状态：0-未上菜 1-已上菜 2-已撤餐',
  `is_add_dish` tinyint NOT NULL DEFAULT '0' COMMENT '是否加菜: 0-否,1-是',
  `add_dish_user_id` bigint DEFAULT NULL COMMENT '加菜人ID',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_dish` (`dish_id`),
  KEY `idx_step_status` (`step_status`),
  KEY `idx_step_sort` (`step_sort`),
  KEY `idx_order_step_status` (`order_id`,`step_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_payment_record
CREATE TABLE `t_payment_record` (
`id` varchar(64) NOT NULL COMMENT '支付记录ID',
  `payment_no` varchar(64) NOT NULL COMMENT '支付流水号',
  `order_id` varchar(64) NOT NULL COMMENT '订单ID',
  `user_id` varchar(64) NOT NULL COMMENT '支付用户ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `payment_method` varchar(20) NOT NULL DEFAULT 'wallet' COMMENT '支付方式：wallet-钱包, wechat-微信, alipay-支付宝',
  `payment_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '支付状态：pending-待支付, success-成功, failed-失败, refund-已退款',
  `transaction_id` varchar(128) DEFAULT NULL COMMENT '第三方交易ID',
  `paid_time` datetime DEFAULT NULL COMMENT '支付完成时间',
  `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT '退款金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `payment_no` (`payment_no`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`payment_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_permission
CREATE TABLE `t_permission` (
`permission_id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` varchar(100) NOT NULL COMMENT '权限名称',
  `permission_code` varchar(50) NOT NULL COMMENT '权限编码',
  `resource_type` varchar(20) NOT NULL COMMENT '资源类型：MENU-菜单, BUTTON-按钮, API-接口',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限ID（0表示顶级权限）',
  `path` varchar(100) DEFAULT NULL COMMENT '路由路径（菜单类型使用）',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `description` varchar(200) DEFAULT NULL COMMENT '权限描述',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, DISABLED-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `permission_code` (`permission_code`),
  KEY `idx_permission_code` (`permission_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_recharge_record
CREATE TABLE `t_recharge_record` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '充值记录ID',
  `recharge_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '充值流水号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付方式：wechat-微信, alipay-支付宝, bank-银行卡',
  `recharge_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '充值状态：pending-待支付, success-成功, failed-失败',
  `transaction_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方交易ID',
  `paid_time` datetime DEFAULT NULL COMMENT '支付完成时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `recharge_no` (`recharge_no`),
  UNIQUE KEY `uk_recharge_no` (`recharge_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`recharge_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_recipe
CREATE TABLE `t_recipe` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '食谱ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `name` varchar(255) NOT NULL COMMENT '食谱名称',
  `type` varchar(20) NOT NULL COMMENT '食谱类型: breakfast/lunch/dinner/snack',
  `calories` int NOT NULL COMMENT '卡路里',
  `protein` int NOT NULL COMMENT '蛋白质',
  `carbs` int NOT NULL COMMENT '碳水化合物',
  `fat` int NOT NULL COMMENT '脂肪',
  `detail` text COMMENT '食谱详情',
  `custom_nutrition` text COMMENT '自定义营养信息(JSON格式)',
  `cook_time` varchar(50) DEFAULT NULL COMMENT '烹饪时间',
  `is_favorite` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否收藏: 0-否, 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `items` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_refund_record
CREATE TABLE `t_refund_record` (
`refund_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款记录ID',
  `refund_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款流水号',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户名',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `reason` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款原因：QUALITY-商品质量问题, DONT_WANT-不想要了, DELAY-配送延迟, OTHER-其他',
  `reason_description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款原因描述',
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT '退款状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝, PROCESSING-处理中, SUCCESS-成功, FAILED-失败',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  `audit_by` bigint DEFAULT NULL COMMENT '审核人ID',
  `audit_by_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人名称',
  `process_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理备注',
  `refund_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '退款方式：ORIGINAL-原路退回, BALANCE-退回余额',
  `transaction_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方退款流水号',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`refund_id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_reject_recommendation
CREATE TABLE `t_reject_recommendation` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `dish_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `reject_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拒绝时间',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拒绝原因',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_user_dish` (`user_id`,`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_review
CREATE TABLE `t_review` (
`id` varchar(50) NOT NULL COMMENT '评价ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `merchant_id` varchar(50) NOT NULL COMMENT '商家ID',
  `order_id` varchar(50) NOT NULL COMMENT '订单ID',
  `dish_id` varchar(50) DEFAULT NULL COMMENT '菜品ID',
  `rating` tinyint NOT NULL COMMENT '评价星级(1-5)',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `images` varchar(2000) DEFAULT NULL COMMENT '评价图片URL列表(JSON格式)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status` tinyint DEFAULT '0' COMMENT '评价状态(0-正常,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_rating_create_time` (`rating`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_review_reply
CREATE TABLE `t_review_reply` (
`id` varchar(50) NOT NULL COMMENT '回复ID',
  `review_id` varchar(50) NOT NULL COMMENT '评价ID',
  `merchant_id` varchar(50) NOT NULL COMMENT '商家ID',
  `content` varchar(500) DEFAULT NULL COMMENT '回复内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_additional` tinyint DEFAULT '0' COMMENT '是否为追加评价(0-商家回复,1-用户追评)',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_role
CREATE TABLE `t_role` (
`role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(20) NOT NULL COMMENT '角色编码：SUPER_ADMIN-超级管理员, ADMIN-普通管理员, AUDITOR-审核员',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, DISABLED-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_code` (`role_code`),
  KEY `idx_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_role_permission
CREATE TABLE `t_role_permission` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_role_permission_relation
CREATE TABLE `t_role_permission_relation` (
`relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_scheduled_task
CREATE TABLE `t_scheduled_task` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID',
  `task_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `task_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务代码',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '任务描述',
  `cron_expression` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Cron表达式',
  `task_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务类型：ONCE-一次性, CRON-Cron表达式, FIXED_RATE-固定频率, FIXED_DELAY-固定延迟',
  `rate_in_millis` bigint DEFAULT NULL COMMENT '执行频率（毫秒）',
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT '任务状态：ENABLED-启用, DISABLED-禁用, PAUSED-暂停',
  `task_class_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务类名',
  `task_method_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务方法名',
  `task_params` text COLLATE utf8mb4_unicode_ci COMMENT '任务参数（JSON格式）',
  `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `execute_count` int DEFAULT '0' COMMENT '执行次数',
  `fail_count` int DEFAULT '0' COMMENT '失败次数',
  `last_execute_result` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后执行结果：SUCCESS-成功, FAILED-失败, RUNNING-运行中',
  `last_error_message` text COLLATE utf8mb4_unicode_ci COMMENT '最后执行错误信息',
  `log_execution` tinyint(1) DEFAULT '0' COMMENT '是否记录执行日志',
  `timeout_seconds` int DEFAULT '0' COMMENT '超时时间（秒），0表示不限制',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `retried_count` int DEFAULT '0' COMMENT '已重试次数',
  `task_group` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务分组',
  `priority` int DEFAULT '3' COMMENT '优先级：1-高, 2-中, 3-低',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_task_code` (`task_code`),
  KEY `idx_next_execute_time` (`next_execute_time`),
  KEY `idx_task_group` (`task_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_scheduled_task_log
CREATE TABLE `t_scheduled_task_log` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志ID',
  `task_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务ID',
  `task_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务名称',
  `task_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务代码',
  `start_time` datetime DEFAULT NULL COMMENT '执行开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '执行结束时间',
  `duration` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
  `execute_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '执行状态：SUCCESS-成功, FAILED-失败, TIMEOUT-超时',
  `result_message` text COLLATE utf8mb4_unicode_ci COMMENT '执行结果信息',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `exception_stack` text COLLATE utf8mb4_unicode_ci COMMENT '异常堆栈',
  `execute_params` text COLLATE utf8mb4_unicode_ci COMMENT '执行参数（JSON格式）',
  `execute_result` text COLLATE utf8mb4_unicode_ci COMMENT '执行返回结果（JSON格式）',
  `server_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务器IP',
  `server_hostname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '服务器主机名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_task_code` (`task_code`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_execute_status` (`execute_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_system_log
CREATE TABLE `t_system_log` (
`log_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志ID',
  `operation_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作类型',
  `module` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作模块',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作描述',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人名称',
  `operator_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作人类型',
  `method` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求方法',
  `params` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数',
  `result` text COLLATE utf8mb4_unicode_ci COMMENT '返回结果',
  `execute_time` bigint DEFAULT NULL COMMENT '执行时长(毫秒)',
  `ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作状态',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `browser` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '浏览器类型',
  `os` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作系统',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_module` (`module`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_user
CREATE TABLE `t_user` (
`user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(64) NOT NULL COMMENT '密码（BCrypt加密）',
  `payment_password` varchar(255) DEFAULT NULL COMMENT '支付密码（加密存储）',
  `has_payment_password` tinyint(1) DEFAULT '0' COMMENT '是否已设置支付密码：0-未设置，1-已设置',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `height` decimal(5,1) DEFAULT NULL COMMENT '身高(cm)',
  `weight` decimal(5,1) DEFAULT NULL COMMENT '体重(kg)',
  `diet_goal` varchar(20) DEFAULT NULL COMMENT '饮食目标（lose_weight/keep_fit/gain_weight）',
  `allergies` json DEFAULT NULL COMMENT '过敏食材列表',
  `prefer_tags` json DEFAULT NULL COMMENT '饮食偏好标签（JSON格式）',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱地址',
  `disable_weather_recommend` tinyint(1) DEFAULT '0' COMMENT '是否关闭天气推荐',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像URL',
  `merchant_id` varchar(64) DEFAULT NULL COMMENT '商家ID',
  `location` varchar(50) DEFAULT NULL COMMENT '用户所在地',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别：male-男，female-女，other-其他',
  `birthday` varchar(20) DEFAULT NULL COMMENT '生日',
  `bio` text COMMENT '个人简介',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_user_custom_event
CREATE TABLE `t_user_custom_event` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `event_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件名称',
  `event_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型：BIRTHDAY-生日, ANNIVERSARY-纪念日, PARTY-聚会, OTHER-其他',
  `event_date` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件日期（MM-dd格式）',
  `year` int DEFAULT NULL COMMENT '年份（NULL表示每年重复）',
  `reminder_days` int DEFAULT '3' COMMENT '提前提醒天数',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '事件描述',
  `preferred_dishes` json DEFAULT NULL COMMENT '偏好菜品列表（JSON数组）',
  `guest_count` int DEFAULT NULL COMMENT '预计用餐人数',
  `budget_per_person` decimal(10,2) DEFAULT NULL COMMENT '人均预算',
  `is_active` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_event_date` (`event_date`),
  KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_user_oauth
CREATE TABLE `t_user_oauth` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID (OA + 16位数字)',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关联的本地用户ID',
  `provider` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台标识: wechat/qq',
  `open_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OpenID',
  `union_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '微信UnionID（打通小程序）',
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方昵称',
  `avatar_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方头像URL',
  `access_token` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'access_token（加密存储）',
  `refresh_token` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'refresh_token（加密存储）',
  `token_expires_at` datetime DEFAULT NULL COMMENT 'token过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_openid` (`provider`,`open_id`),
  UNIQUE KEY `uk_provider_unionid` (`provider`,`union_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_user_preference
CREATE TABLE `t_user_preference` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_weights` varchar(1000) DEFAULT '{}' COMMENT '标签权重，JSON格式（如 {"spicy": 0.1, "sweet": 0.8}）',
  `disable_weather_recommend` bit(1) DEFAULT b'0' COMMENT '是否关闭天气推荐',
  `diet_goal` varchar(50) DEFAULT NULL COMMENT '饮食目标（如 low_calorie, high_protein 等）',
  `allergies` varchar(500) DEFAULT '[]' COMMENT '过敏食材列表，JSON格式',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `enable_ai_personal_data` tinyint(1) DEFAULT '1' COMMENT '是否允许AI使用个人数据：0-不允许，1-允许',
  `enable_order_notification` tinyint(1) DEFAULT '1' COMMENT '订单通知开关',
  `enable_activity_notification` tinyint(1) DEFAULT '1' COMMENT '活动通知开关',
  `enable_merchant_reply_notification` tinyint(1) DEFAULT '1' COMMENT '商家回复通知开关',
  `enable_group_chat_notification` tinyint(1) DEFAULT '1' COMMENT '群聊消息通知开关',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_user_recommend_history
CREATE TABLE `t_user_recommend_history` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `festival_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节日ID',
  `custom_event_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义事件ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品ID',
  `recommend_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '推荐类型',
  `is_clicked` tinyint(1) DEFAULT '0' COMMENT '是否点击',
  `is_ordered` tinyint(1) DEFAULT '0' COMMENT '是否下单',
  `feedback_score` int DEFAULT NULL COMMENT '反馈评分（1-5）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_festival_id` (`festival_id`),
  KEY `idx_custom_event_id` (`custom_event_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_wallet
CREATE TABLE `t_wallet` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '钱包ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '当前余额（元）',
  `total_recharge` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '累计充值金额',
  `total_consume` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '累计消费金额',
  `total_withdraw` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '累计提现金额',
  `version` int NOT NULL DEFAULT '0' COMMENT '乐观锁版本号',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT '钱包状态：active-正常, frozen-冻结',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `locked` tinyint(1) DEFAULT '0' COMMENT '是否锁定（用于钱包安全设置）0-未锁定, 1-锁定',
  `verify_enabled` tinyint(1) DEFAULT '1' COMMENT '是否开启支付验证 0-关闭, 1-开启',
  `daily_limit` decimal(10,2) DEFAULT '5000.00' COMMENT '单日交易限额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_wish_list_item
CREATE TABLE `t_wish_list_item` (
`id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `merchant_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家ID（可选，指定商家的需求）',
  `dish_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜品名称',
  `dish_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜品图片URL',
  `taste_requirement` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '口味要求',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '详细描述',
  `recipe_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '参考食谱ID',
  `audit_status` int DEFAULT '0' COMMENT '审核状态：0-待审核, 1-已通过, 2-已拒绝, 3-申诉中, 4-申诉成功, 5-申诉失败, 6-超时自动通过, 7-已撤回',
  `rejection_reason_code` int DEFAULT NULL COMMENT '拒绝原因代码',
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拒绝原因说明',
  `audit_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家审核备注',
  `auditor_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `is_appealed` tinyint(1) DEFAULT '0' COMMENT '是否申诉',
  `appeal_content` text COLLATE utf8mb4_unicode_ci COMMENT '申诉内容',
  `appeal_time` datetime DEFAULT NULL COMMENT '申诉时间',
  `appeal_reply` text COLLATE utf8mb4_unicode_ci COMMENT '申诉回复',
  `appeal_reply_time` datetime DEFAULT NULL COMMENT '申诉回复时间',
  `appeal_replier_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申诉回复人ID',
  `expected_available_time` datetime DEFAULT NULL COMMENT '期望上架时间',
  `actual_available_time` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实际上架时间（审核通过后商家填写的预计时间）',
  `timeout_time` datetime NOT NULL COMMENT '超时时间（24小时自动通过）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_timeout_time` (`timeout_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: t_withdraw_record
CREATE TABLE `t_withdraw_record` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提现记录ID',
  `withdraw_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提现流水号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '提现金额',
  `fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '手续费',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实际到账金额',
  `withdraw_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提现方式：wechat-微信, alipay-支付宝, bank-银行卡',
  `account_info` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提现账号信息（脱敏）',
  `withdraw_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '提现状态：pending-待审核, approved-已通过, rejected-已拒绝, processing-处理中, success-成功, failed-失败',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_user` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '拒绝原因',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `withdraw_no` (`withdraw_no`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`withdraw_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: tutorial
CREATE TABLE `tutorial` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教程标题',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教程类型：video/article',
  `duration` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '时长',
  `views` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '观看/阅读量',
  `source_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ADMIN' COMMENT '来源类型: ADMIN-管理员, MERCHANT-商家, AI_GENERATED-AI生成',
  `source_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '来源ID: 管理员ID/商家ID/用户ID/AI版本',
  `author_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者类型: ADMIN, MERCHANT, AI',
  `author_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者ID',
  `featured` tinyint(1) DEFAULT '0' COMMENT '是否在首页推荐',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态: DRAFT-草稿, PENDING-待审核, PUBLISHED-已发布, REJECTED-已拒绝',
  `review_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核状态: NOT_SUBMITTED-未提交, PENDING-待审核, APPROVED-通过, REJECTED-拒绝',
  `reviewer_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_comment` text COLLATE utf8mb4_unicode_ci COMMENT '审核意见',
  `is_official` tinyint(1) DEFAULT '0' COMMENT '是否官方认证(仅管理员发布)',
  `linked_merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联商家ID（商家教程可用）',
  `linked_dish_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联菜品ID（商家教程可用）',
  `ai_model_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'AI模型版本(AI教程)',
  `tags` json DEFAULT NULL COMMENT '标签数组: ["健康", "低卡", "素食"]',
  `difficulty` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '难度: BEGINNER-初级, INTERMEDIATE-中级, ADVANCED-高级',
  `calories` int DEFAULT NULL COMMENT '卡路里',
  `prep_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '准备时间',
  `servings` int DEFAULT NULL COMMENT '份量',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT '评分(0-5)',
  `rating_count` int DEFAULT '0' COMMENT '评分人数',
  `favorite_count` int DEFAULT '0' COMMENT '收藏次数',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  `share_count` int DEFAULT '0' COMMENT '分享次数',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '教程内容',
  `cover_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '封面图片',
  `video_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频地址',
  `author` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_status` (`status`),
  KEY `idx_review_status` (`review_status`),
  KEY `idx_featured` (`featured`),
  KEY `idx_author` (`author_id`),
  KEY `idx_linked_dish` (`linked_dish_id`),
  KEY `idx_linked_merchant` (`linked_merchant_id`),
  KEY `idx_rating` (`rating`),
  KEY `idx_author_id` (`author_id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_linked_merchant_id` (`linked_merchant_id`),
  KEY `idx_linked_dish_id` (`linked_dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_behavior
CREATE TABLE `user_behavior` (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `behavior_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '行为类型: view(浏览)/click(点击)/order(下单)/favorite(收藏)/reject(拒绝)/share(分享)',
  `item_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品类型: dish(菜品)/merchant(商家)/recipe(食谱)',
  `item_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物品ID',
  `context` json DEFAULT NULL COMMENT '上下文信息: {time, weather, location, device}',
  `duration` int DEFAULT NULL COMMENT '行为持续时长(秒), 浏览类行为使用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`created_time`),
  KEY `idx_item_time` (`item_id`,`created_time`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_user_behavior_type` (`user_id`,`behavior_type`,`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_coupon
CREATE TABLE `user_coupon` (
`id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券ID',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券名称',
  `amount` decimal(10,2) NOT NULL COMMENT '优惠金额',
  `min_amount` decimal(10,2) DEFAULT '0.00' COMMENT '最低消费金额',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'available' COMMENT '优惠券状态：available-可用, used-已使用, expired-已过期',
  `order_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '关联订单ID',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_user_id_status` (`user_id`,`status`),
  KEY `idx_user_id_expire` (`user_id`,`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: user_profile
CREATE TABLE `user_profile` (
`id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `preference_tags` json DEFAULT NULL COMMENT '偏好标签: [{"tag": "川菜", "score": 0.8}, ...]',
  `dietary_restrictions` json DEFAULT NULL COMMENT '饮食禁忌: ["过敏原", "宗教禁忌"]',
  `flavor_preference` json DEFAULT NULL COMMENT '口味偏好: {"spicy": 0.7, "sweet": 0.3, "salty": 0.5}',
  `price_preference` json DEFAULT NULL COMMENT '价格偏好: {"min": 10, "max": 50, "optimal": 25}',
  `nutrition_goals` json DEFAULT NULL COMMENT '营养目标: {"calories": 2000, "protein": 100}',
  `meal_pattern` json DEFAULT NULL COMMENT '用餐模式: {"breakfast": "07:00", "lunch": "12:00", "dinner": "18:30"}',
  `statistics` json DEFAULT NULL COMMENT '统计数据: {total_orders, avg_order_amount, fav_categories}',
  `last_updated` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

