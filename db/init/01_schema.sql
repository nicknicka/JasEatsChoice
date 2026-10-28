-- JasEatsChoice init: table definitions only, no rows.
-- Regenerate with scripts/build_db_seed.py + scripts/fix_dump_mojibake.py (see db/README.md).
SET NAMES utf8mb4;-- MySQL dump 10.13  Distrib 9.4.0, for Linux (aarch64)
--
-- Host: localhost    Database: jia_shi_yi_xuan
-- ------------------------------------------------------
-- Server version	9.4.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `announcement`
--

DROP TABLE IF EXISTS `announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家ID，NULL表示系统公告',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告内容',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'system' COMMENT '公告类型：system-系统公告, activity-活动公告, urgent-紧急公告, update-更新说明',
  `priority` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT 'ä¼˜å…ˆçº§ï¼šnormal-æ™®é€š, important-é‡è¦, urgent-ç´§æ€¥',
  `target_audience` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'all' COMMENT '目标群体：all-全部用户, merchant-商家端, customer-用户端',
  `read_count` bigint DEFAULT '0' COMMENT 'é˜…è¯»é‡',
  `read_users` bigint DEFAULT '0' COMMENT '阅读人数',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT 'å…¬å‘ŠçŠ¶æ€ (active: å¯ç”¨, inactive: ç¦ç”¨)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT 'ç»“æŸæ—¶é—´',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2023710121550073858 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å•†å®¶å…¬å‘Šè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `discount`
--

DROP TABLE IF EXISTS `discount`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  `limit_per_user` int DEFAULT NULL COMMENT 'æ¯äººé™é¢†',
  `usage_notes` text COLLATE utf8mb4_unicode_ci COMMENT '使用说明',
  `validity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '有效期类型',
  `validity_period` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '有效期范围',
  `valid_days` int DEFAULT NULL COMMENT '有效天数',
  `used_count` int DEFAULT '0' COMMENT '使用次数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2023714263790178307 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dish_features`
--

DROP TABLE IF EXISTS `dish_features`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dish_features` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dish_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“åˆ†ç±»',
  `tags` json DEFAULT NULL COMMENT 'æ ‡ç­¾æ•°ç»„: ["è¾£", "å·èœ", "ä¸‹é¥­èœ"]',
  `flavor_profile` json DEFAULT NULL COMMENT 'é£Žå‘³ç‰¹å¾: {"spicy": 0.8, "salty": 0.5}',
  `nutrition_info` json DEFAULT NULL COMMENT 'è¥å…»ä¿¡æ¯: {calories, protein, fat, carbs}',
  `ingredients` json DEFAULT NULL COMMENT 'ä¸»è¦é£Ÿæ: ["é¸¡è‚‰", "åœŸè±†", "è¾£æ¤’"]',
  `cooking_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'çƒ¹é¥ªæ–¹å¼: ç‚’/ç…®/è’¸/çƒ¤',
  `suitable_scenarios` json DEFAULT NULL COMMENT 'é€‚ç”¨åœºæ™¯: ["å·¥ä½œæ—¥", "èšé¤", "å®µå¤œ"]',
  `time_period_tags` json DEFAULT NULL COMMENT 'æ—¶æ®µæ ‡ç­¾: ["åˆé¤", "æ™šé¤"]',
  `season_tags` json DEFAULT NULL COMMENT 'å­£èŠ‚æ ‡ç­¾: ["å¤å­£", "å†¬å­£"]',
  `price_level` tinyint DEFAULT NULL COMMENT '价格等级 1-5',
  `popularity_score` decimal(5,2) DEFAULT NULL COMMENT '热度分数',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dish_id` (`dish_id`),
  KEY `idx_category` (`category`),
  KEY `idx_popularity` (`popularity_score` DESC),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èœå“ç‰¹å¾è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dish_similarity`
--

DROP TABLE IF EXISTS `dish_similarity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dish_similarity` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dish_id_a` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“A ID',
  `dish_id_b` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“B ID',
  `similarity_score` decimal(5,4) NOT NULL COMMENT '相似度分数 0-1',
  `similarity_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç›¸ä¼¼åº¦ç±»åž‹: content(åŸºäºŽå†…å®¹)/collaborative(ååŒè¿‡æ»¤)/hybrid(æ··åˆ)',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dish_pair_type` (`dish_id_a`,`dish_id_b`,`similarity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èœå“ç›¸ä¼¼åº¦è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hot_topic`
--

DROP TABLE IF EXISTS `hot_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hot_topic` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '热点内容',
  `priority` int DEFAULT '0' COMMENT '优先级',
  `source_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'MANUAL' COMMENT 'æ¥æºç±»åž‹',
  `source_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ¥æºID',
  `start_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生效开始时间',
  `end_date` datetime DEFAULT NULL COMMENT 'ç”Ÿæ•ˆç»“æŸæ—¶é—´',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€',
  `click_count` int DEFAULT '0' COMMENT '点击次数',
  `share_count` int DEFAULT '0' COMMENT '分享次数',
  `require_review` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦éœ€è¦å®¡æ ¸',
  `review_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'APPROVED' COMMENT 'å®¡æ ¸çŠ¶æ€',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸æ„è§',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¤‡æ³¨ä¿¡æ¯',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ä»Šæ—¥çƒ­ç‚¹è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `j_food_categories`
--

DROP TABLE IF EXISTS `j_food_categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `j_food_categories` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(255) NOT NULL COMMENT '食物分类名称',
  `father_id` varchar(50) DEFAULT NULL COMMENT '父级分类ID',
  `cate_id` varchar(50) DEFAULT NULL COMMENT '当前分类ID',
  `is_subcategory` tinyint(1) DEFAULT '0' COMMENT '是否为子分类',
  `parent_category_id` int DEFAULT NULL COMMENT '父分类在数据库中的ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb3 COMMENT='食物营养成分-分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `j_food_nutrition`
--

DROP TABLE IF EXISTS `j_food_nutrition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=1615 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食物营养成分表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `recommendation_log`
--

DROP TABLE IF EXISTS `recommendation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recommendation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `recommendation_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æŽ¨èæ‰¹æ¬¡ID',
  `dish_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `rank` int DEFAULT NULL COMMENT 'æŽ¨èæŽ’åºä½ç½®',
  `score` decimal(10,4) DEFAULT NULL COMMENT 'æŽ¨èå¾—åˆ†',
  `algorithm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '使用的算法',
  `reason` json DEFAULT NULL COMMENT 'æŽ¨èç†ç”±: {factors: [{type, name, score}]}',
  `is_clicked` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦è¢«ç‚¹å‡»',
  `is_ordered` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦è¢«ä¸‹å•',
  `feedback_time` datetime DEFAULT NULL COMMENT 'åé¦ˆæ—¶é—´',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'æŽ¨èæ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_recommendation_id` (`recommendation_id`),
  KEY `idx_created_time` (`created_time`),
  KEY `idx_user_recommendation` (`user_id`,`recommendation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æŽ¨èè®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'é…ç½®ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'é…ç½®é”®',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT 'é…ç½®å€¼',
  `config_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é…ç½®åç§°',
  `config_group` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'system' COMMENT 'é…ç½®åˆ†ç»„',
  `config_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'string' COMMENT 'é…ç½®ç±»åž‹ï¼šstring-å­—ç¬¦ä¸², number-æ•°å­—, boolean-å¸ƒå°”, json-JSONå¯¹è±¡',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é…ç½®æè¿°',
  `is_system` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ç³»ç»Ÿå†…ç½®',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT 'çŠ¶æ€ï¼šactive-å¯ç”¨, inactive-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`),
  KEY `idx_config_group` (`config_group`),
  KEY `idx_config_key` (`config_key`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç³»ç»Ÿé…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_add_dish_request`
--

DROP TABLE IF EXISTS `t_add_dish_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_add_dish_request` (
  `id` varchar(64) NOT NULL COMMENT 'åŠ èœè¯·æ±‚ID',
  `group_order_id` varchar(64) NOT NULL COMMENT '群订单ID',
  `original_order_id` varchar(64) DEFAULT NULL COMMENT 'åŽŸè®¢å•ID(å…³è”å·²æ”¯ä»˜è®¢å•)',
  `request_user_id` varchar(64) NOT NULL COMMENT '加菜请求人ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `dish_info` json NOT NULL COMMENT 'åŠ èœèœå“ä¿¡æ¯JSON: [{dishId,quantity,customization,price}]',
  `total_amount` decimal(10,2) NOT NULL COMMENT 'åŠ èœæ€»é‡‘é¢',
  `approval_status` tinyint NOT NULL DEFAULT '0' COMMENT 'å®¡æ ¸çŠ¶æ€: 0-å¾…å®¡æ ¸,1-å®¡æ ¸é€šè¿‡,2-å®¡æ ¸é©³å›ž,3-å·²æ’¤å›ž,4-è¶…æ—¶é©³å›ž',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `reviewer_id` varchar(64) DEFAULT NULL COMMENT '审核人ID(群订单发起者)',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `first_remind_time` datetime DEFAULT NULL COMMENT 'é¦–æ¬¡æé†’æ—¶é—´(10åˆ†é’Ÿ)',
  `second_remind_time` datetime DEFAULT NULL COMMENT 'äºŒæ¬¡æé†’æ—¶é—´',
  `timeout_time` datetime DEFAULT NULL COMMENT '超时时间(15分钟)',
  `related_payment_id` varchar(64) DEFAULT NULL COMMENT 'å…³è”æ”¯ä»˜è®°å½•ID',
  `related_order_id` varchar(64) DEFAULT NULL COMMENT 'å…³è”è®¢å•ID(å®¡æ ¸é€šè¿‡åŽåˆ›å»º)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_timeout_time` (`timeout_time`),
  KEY `idx_request_user` (`request_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åŠ èœè¯·æ±‚è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_add_dish_setting`
--

DROP TABLE IF EXISTS `t_add_dish_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_add_dish_setting` (
  `id` varchar(64) NOT NULL COMMENT '主键ID',
  `group_order_id` varchar(64) NOT NULL COMMENT '群订单ID',
  `add_dish_permission` tinyint NOT NULL DEFAULT '0' COMMENT 'åŠ èœæƒé™: 0-å…¨å‘˜å¯åŠ èœ,1-ä»…å‘èµ·è€…å¯åŠ èœ',
  `budget_limit` decimal(10,2) DEFAULT NULL COMMENT 'å•æ¬¡åŠ èœé¢„ç®—é™åˆ¶(å¯é€‰)',
  `max_dish_count` int DEFAULT NULL COMMENT 'å•æ¬¡åŠ èœæ•°é‡é™åˆ¶(å¯é€‰)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_order_id` (`group_order_id`),
  KEY `idx_group_order_id` (`group_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åŠ èœè®¾ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_address`
--

DROP TABLE IF EXISTS `t_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'åœ°å€ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT 'æ”¶è´§äººå§“å',
  `receiver_phone` varchar(20) NOT NULL COMMENT 'æ”¶è´§äººç”µè¯',
  `province` varchar(50) NOT NULL COMMENT 'çœä»½',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail` varchar(255) NOT NULL COMMENT 'è¯¦ç»†åœ°å€',
  `is_default` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦é»˜è®¤åœ°å€ï¼ˆ0-å¦/1-æ˜¯ï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2059995675927699459 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç”¨æˆ·åœ°å€è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_admin`
--

DROP TABLE IF EXISTS `t_admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_admin` (
  `admin_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ç®¡ç†å‘˜ID',
  `username` varchar(50) NOT NULL COMMENT 'ç®¡ç†å‘˜ç”¨æˆ·å',
  `password` varchar(64) NOT NULL COMMENT 'å¯†ç ï¼ˆåŠ å¯†ï¼‰',
  `real_name` varchar(50) DEFAULT NULL COMMENT 'çœŸå®žå§“å',
  `phone` varchar(11) DEFAULT NULL COMMENT 'æ‰‹æœºå·',
  `email` varchar(100) DEFAULT NULL COMMENT 'é‚®ç®±',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'å¤´åƒURL',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€ï¼šACTIVE-æ´»è·ƒ, LOCKED-é”å®š, DELETED-åˆ é™¤',
  `role_id` bigint DEFAULT NULL COMMENT 'è§’è‰²IDï¼ˆå…³è”t_roleè¡¨ï¼‰',
  `last_login_time` datetime DEFAULT NULL COMMENT 'æœ€åŽç™»å½•æ—¶é—´',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT 'æœ€åŽç™»å½•IP',
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç®¡ç†å‘˜è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_admin_operation_log`
--

DROP TABLE IF EXISTS `t_admin_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_admin_operation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` bigint DEFAULT NULL COMMENT 'ç®¡ç†å‘˜ID',
  `username` varchar(50) DEFAULT NULL COMMENT 'ç®¡ç†å‘˜ç”¨æˆ·å',
  `operation_type` varchar(50) DEFAULT NULL COMMENT 'æ“ä½œç±»åž‹ï¼šLOGIN, LOGOUT, CREATE, UPDATE, DELETE, AUDITç­‰',
  `module_name` varchar(50) DEFAULT NULL COMMENT 'æ¨¡å—åç§°',
  `operation_desc` varchar(255) DEFAULT NULL COMMENT 'æ“ä½œæè¿°',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法：GET, POST, PUT, DELETE',
  `request_url` varchar(255) DEFAULT NULL COMMENT 'è¯·æ±‚URL',
  `request_params` text COMMENT 'è¯·æ±‚å‚æ•°',
  `response_result` text COMMENT 'å“åº”ç»“æžœ',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IPåœ°å€',
  `execute_time` int DEFAULT NULL COMMENT '执行时长（毫秒）',
  `status` varchar(20) DEFAULT NULL COMMENT 'çŠ¶æ€ï¼šSUCCESS-æˆåŠŸ, FAIL-å¤±è´¥',
  `error_message` text COMMENT 'é”™è¯¯ä¿¡æ¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç®¡ç†å‘˜æ“ä½œæ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_admin_role_relation`
--

DROP TABLE IF EXISTS `t_admin_role_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_admin_role_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å…³è”ID',
  `admin_id` bigint NOT NULL COMMENT 'ç®¡ç†å‘˜ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`,`role_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç®¡ç†å‘˜-è§’è‰²å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_ai_chat_history`
--

DROP TABLE IF EXISTS `t_ai_chat_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ai_chat_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `sender` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å‘é€è€…ï¼šuser-ç”¨æˆ·ï¼Œai-AIåŠ©æ‰‹',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ¶ˆæ¯å†…å®¹',
  `message_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ¶ˆæ¯ç±»åž‹ï¼ˆç”¨äºŽå¡ç‰‡æ˜¾ç¤ºï¼‰',
  `card_data` text COLLATE utf8mb4_unicode_ci COMMENT 'å¡ç‰‡æ•°æ®ï¼ˆJSONæ ¼å¼ï¼‰',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1104 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AIèŠå¤©åŽ†å²è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_banner`
--

DROP TABLE IF EXISTS `t_banner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_banner` (
  `id` varchar(32) NOT NULL COMMENT '轮播图ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `image_url` varchar(500) NOT NULL COMMENT '图片URL',
  `type` varchar(20) DEFAULT 'link' COMMENT 'ç±»åž‹: link-å¤–éƒ¨é“¾æŽ¥, dish-èœå“, merchant-å•†å®¶, activity-æ´»åŠ¨',
  `target_type` varchar(50) DEFAULT '' COMMENT '跳转目标类型',
  `target_id` varchar(32) DEFAULT '' COMMENT '跳转目标ID',
  `link` varchar(500) DEFAULT '' COMMENT '外部链接',
  `position` varchar(20) DEFAULT 'home' COMMENT 'ä½ç½®: home-é¦–é¡µ, merchant-å•†å®¶é¡µ, dish-èœå“é¡µ',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºé¡ºåº',
  `status` int DEFAULT '1' COMMENT 'çŠ¶æ€: 0-ç¦ç”¨, 1-å¯ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è½®æ’­å›¾è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_calorie_record`
--

DROP TABLE IF EXISTS `t_calorie_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_calorie_record` (
  `id` varchar(64) NOT NULL COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dish_id` bigint DEFAULT NULL COMMENT '菜品ID',
  `calorie` int NOT NULL COMMENT '卡路里',
  `protein` double DEFAULT '0' COMMENT 'è›‹ç™½è´¨å«é‡(g)',
  `fat` double DEFAULT '0' COMMENT 'è„‚è‚ªå«é‡(g)',
  `carbohydrate` double DEFAULT '0' COMMENT 'ç¢³æ°´åŒ–åˆç‰©å«é‡(g)',
  `meal_time` varchar(20) NOT NULL COMMENT '餐次（breakfast/lunch/dinner/snack）',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `food_name` varchar(255) DEFAULT NULL COMMENT '食物名称',
  `description` varchar(500) DEFAULT NULL COMMENT '食物描述',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`record_time`),
  KEY `idx_meal_time` (`meal_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卡路里摄入记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_chat_msg`
--

DROP TABLE IF EXISTS `t_chat_msg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_chat_msg` (
  `msg_id` varchar(64) NOT NULL,
  `from_id` varchar(64) NOT NULL COMMENT 'å‘é€è€…ID',
  `to_id` varchar(64) NOT NULL COMMENT '接收者ID',
  `session_id` varchar(64) DEFAULT NULL COMMENT 'ä¼šè¯ID(ç§èŠä¸ºç”Ÿæˆçš„ä¼šè¯æ ‡è¯†,ç¾¤èŠä¸ºç¾¤ID)',
  `msg_type` varchar(20) NOT NULL COMMENT 'æ¶ˆæ¯ç±»åž‹ï¼štext/image/file',
  `session_type` varchar(20) NOT NULL COMMENT 'ä¼šè¯ç±»åž‹ï¼šsingle/group',
  `content` text NOT NULL COMMENT 'æ¶ˆæ¯å†…å®¹',
  `reply_to` varchar(64) DEFAULT NULL COMMENT 'å›žå¤çš„æ¶ˆæ¯ID',
  `reply_content` text COMMENT 'è¢«å›žå¤çš„æ¶ˆæ¯å†…å®¹',
  `reply_from_id` varchar(64) DEFAULT NULL COMMENT 'è¢«å›žå¤æ¶ˆæ¯çš„å‘é€è€…ID',
  `reply_from_name` varchar(100) DEFAULT NULL COMMENT 'è¢«å›žå¤æ¶ˆæ¯çš„å‘é€è€…åç§°',
  `read_status` tinyint(1) DEFAULT '0' COMMENT 'å·²è¯»çŠ¶æ€:0-æœªè¯»,1-å·²è¯»',
  `synced` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已同步到群：0-未同步，1-已同步',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `file_url` varchar(512) DEFAULT NULL COMMENT 'æ–‡ä»¶URLï¼ˆå›¾ç‰‡/æ–‡ä»¶æ¶ˆæ¯æ—¶ä½¿ç”¨ï¼‰',
  `file_name` varchar(255) DEFAULT NULL COMMENT 'æ–‡ä»¶åï¼ˆæ–‡ä»¶æ¶ˆæ¯æ—¶ä½¿ç”¨ï¼‰',
  `file_size` bigint DEFAULT NULL COMMENT 'æ–‡ä»¶å¤§å°ï¼ˆå­—èŠ‚ï¼‰',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件MIME类型',
  PRIMARY KEY (`msg_id`),
  KEY `idx_from_to_time` (`from_id`,`to_id`,`create_time`),
  KEY `idx_to_read` (`to_id`,`read_status`),
  KEY `idx_reply_to` (`reply_to`),
  KEY `idx_session_time` (`session_id`,`create_time`),
  KEY `idx_file_url` (`file_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='èŠå¤©æ¶ˆæ¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_chat_session`
--

DROP TABLE IF EXISTS `t_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) NOT NULL COMMENT 'ä¼šè¯æ ‡è¯†ï¼ˆç§èŠä¸ºåŒæ–¹ç”¨æˆ·IDç»„åˆï¼Œç¾¤èŠä¸ºç¾¤IDï¼‰',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `session_type` varchar(20) NOT NULL COMMENT 'ä¼šè¯ç±»åž‹: single-ç§èŠ, group-ç¾¤èŠ',
  `session_name` varchar(100) NOT NULL COMMENT 'ä¼šè¯åç§°',
  `avatar` varchar(255) DEFAULT NULL COMMENT 'ä¼šè¯å¤´åƒ',
  `last_message` text COMMENT 'æœ€åŽä¸€æ¡æ¶ˆæ¯å†…å®¹',
  `last_message_time` datetime DEFAULT NULL COMMENT 'æœ€åŽä¸€æ¡æ¶ˆæ¯æ—¶é—´',
  `unread_count` int DEFAULT '0' COMMENT 'æœªè¯»æ¶ˆæ¯æ•°',
  `pinned` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ç½®é¡¶ï¼ˆ0-æœªç½®é¡¶ï¼Œ1-ç½®é¡¶ï¼‰',
  `member_count` int DEFAULT '0' COMMENT 'æˆå‘˜æ•°é‡ï¼ˆç¾¤èŠï¼‰',
  `group_id` varchar(64) DEFAULT NULL COMMENT '群组ID（仅群聊会话有效）',
  `target_id` varchar(255) DEFAULT NULL COMMENT 'ç›®æ ‡ç”¨æˆ·IDï¼ˆä»…å•èŠä¼šè¯æœ‰æ•ˆï¼Œè¡¨ç¤ºå¯¹æ–¹çš„userIdï¼‰',
  `related_order_id` varchar(64) DEFAULT NULL COMMENT 'å…³è”çš„ç¾¤è®¢å•IDï¼ˆç¾¤è®¢å•ä¸“å±žä¼šè¯æœ‰æ•ˆï¼‰',
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
) ENGINE=InnoDB AUTO_INCREMENT=911 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='èŠå¤©ä¼šè¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_collection`
--

DROP TABLE IF EXISTS `t_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `collectable_type` varchar(20) NOT NULL COMMENT 'æ”¶è—ç±»åž‹ï¼šmerchant, dish, article, recipeç­‰',
  `collectable_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ”¶è—å¯¹è±¡ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type_id` (`collectable_type`,`collectable_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2056958148986998786 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ”¶è—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_consume_history`
--

DROP TABLE IF EXISTS `t_consume_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_consume_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT 'ç±»åž‹ï¼šrechargeï¼ˆå……å€¼ï¼‰ã€consumeï¼ˆæ¶ˆè´¹ï¼‰ã€withdrawï¼ˆæçŽ°ï¼‰',
  `amount` decimal(10,2) NOT NULL COMMENT 'é‡‘é¢',
  `description` varchar(255) DEFAULT NULL COMMENT 'æè¿°',
  `status` varchar(20) NOT NULL COMMENT 'çŠ¶æ€ï¼šsuccessï¼ˆæˆåŠŸï¼‰ã€failedï¼ˆå¤±è´¥ï¼‰',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2062398050747514883 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ¶ˆè´¹è®°å½•';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_contact`
--

DROP TABLE IF EXISTS `t_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `target_id` varchar(64) NOT NULL COMMENT 'ç›®æ ‡IDï¼ˆå¥½å‹IDæˆ–ç¾¤IDï¼‰',
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
) ENGINE=InnoDB AUTO_INCREMENT=2015816641958494219 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='联系人关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_content_extraction`
--

DROP TABLE IF EXISTS `t_content_extraction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_content_extraction` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `source_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å†…å®¹æºID',
  `dish_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“åç§°',
  `dish_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“å›¾ç‰‡',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'èœå“æè¿°',
  `ingredients` text COLLATE utf8mb4_unicode_ci COMMENT 'é£Ÿæåˆ—è¡¨ï¼ˆJSONæ ¼å¼ï¼‰',
  `steps` text COLLATE utf8mb4_unicode_ci COMMENT 'åˆ¶ä½œæ­¥éª¤ï¼ˆJSONæ ¼å¼ï¼‰',
  `steps_description` text COLLATE utf8mb4_unicode_ci COMMENT 'æ­¥éª¤æ•´ä½“æè¿°',
  `cooking_time` int DEFAULT NULL COMMENT '制作时长（分钟）',
  `difficulty` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'éš¾åº¦ï¼šEASY-ç®€å•, MEDIUM-ä¸­ç­‰, HARD-å›°éš¾',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ ‡ç­¾ï¼ˆé€—å·åˆ†éš”ï¼‰',
  `calories` int DEFAULT NULL COMMENT 'å¡è·¯é‡Œ',
  `is_published` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å·²å‘å¸ƒä¸ºé£Ÿè°±',
  `recipe_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”çš„é£Ÿè°±ID',
  `manual_score` int DEFAULT NULL COMMENT '人工评分（1-5）',
  `is_verified` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦äººå·¥éªŒè¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_dish_name` (`dish_name`),
  KEY `idx_is_published` (`is_published`),
  KEY `idx_is_verified` (`is_verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å†…å®¹æå–è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_content_source`
--

DROP TABLE IF EXISTS `t_content_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_content_source` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `content_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容URL',
  `content_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容类型：VIDEO-视频, ARTICLE-文章, IMAGE-图片',
  `platform` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å¹³å°ï¼šDOUYIN-æŠ–éŸ³, XIAOHONGSHU-å°çº¢ä¹¦, BILIBILI-å“”å“©å“”å“©, WECHAT-å¾®ä¿¡, OTHER-å…¶ä»–',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容标题',
  `author` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者/UP主',
  `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å°é¢å›¾ç‰‡URL',
  `video_duration` int DEFAULT NULL COMMENT '视频时长（秒）',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'å†…å®¹æè¿°',
  `is_extracted` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å·²æå–',
  `extraction_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'æå–çŠ¶æ€ï¼šPENDING-å¾…æå–, PROCESSING-æå–ä¸­, SUCCESS-æˆåŠŸ, FAILED-å¤±è´¥',
  `extraction_time` datetime DEFAULT NULL COMMENT 'æå–æ—¶é—´',
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é”™è¯¯ä¿¡æ¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_platform` (`platform`),
  KEY `idx_extraction_status` (`extraction_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å†…å®¹æºè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_dish`
--

DROP TABLE IF EXISTS `t_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_dish` (
  `id` varchar(64) NOT NULL COMMENT '菜品ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜品名称',
  `category` varchar(50) NOT NULL DEFAULT 'å…¶ä»–' COMMENT 'èœå“åˆ†ç±»',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `calorie` int NOT NULL COMMENT '卡路里',
  `estimated_cooking_minutes` int DEFAULT '15' COMMENT '预估烹饪时长（分钟）',
  `step_template` varchar(20) DEFAULT 'NORMAL' COMMENT 'çƒ¹é¥ªæµç¨‹æ¨¡æ¿ï¼šNORMAL-æ­£é¤æµç¨‹ï¼ŒFAST-å¿«é¤æµç¨‹ï¼ŒCUSTOM-è‡ªå®šä¹‰æµç¨‹',
  `ingredients` json DEFAULT NULL COMMENT '食材列表（JSON格式）',
  `description` text COMMENT '菜品描述',
  `cooking_steps` text COMMENT 'çƒ¹é¥ªæ­¥éª¤ï¼ˆJSONæ ¼å¼ï¼‰',
  `nutrition` json DEFAULT NULL COMMENT 'è¥å…»ä¿¡æ¯ï¼ˆJSONæ ¼å¼ï¼‰',
  `image` varchar(500) DEFAULT NULL COMMENT 'èœå“å›¾ç‰‡URL',
  `score` decimal(5,2) DEFAULT NULL COMMENT 'æŽ¨èå¾—åˆ†',
  `is_online` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦ä¸Šæž¶ï¼š1-ä¸Šæž¶ï¼Œ0-ä¸‹æž¶',
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼šPENDING-å¾…å®¡æ ¸, APPROVED-å·²é€šè¿‡, REJECTED-å·²æ‹’ç»',
  `audit_comment` varchar(500) DEFAULT NULL COMMENT 'å®¡æ ¸æ„è§',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_admin_id` bigint DEFAULT NULL COMMENT 'å®¡æ ¸ç®¡ç†å‘˜ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `view_count` int DEFAULT '0' COMMENT 'æµè§ˆæ¬¡æ•°',
  `order_count` int DEFAULT '0' COMMENT 'ä¸‹å•æ¬¡æ•°',
  `favorite_count` int DEFAULT '0' COMMENT 'æ”¶è—æ¬¡æ•°',
  `avg_rating` decimal(3,2) DEFAULT NULL COMMENT 'å¹³å‡è¯„åˆ†',
  `tags` json DEFAULT NULL COMMENT '标签数组',
  `stock` int DEFAULT '-1' COMMENT 'åº“å­˜æ•°é‡ï¼š-1è¡¨ç¤ºä¸é™é‡ï¼Œ>=0è¡¨ç¤ºå®žé™…åº“å­˜',
  `is_fast_food_virtual` tinyint(1) GENERATED ALWAYS AS ((case when (`step_template` = _utf8mb4'FAST') then 1 else 0 end)) VIRTUAL COMMENT 'æ˜¯å¦ä¸ºå¿«é¤ï¼ˆè™šæ‹Ÿå­—æ®µï¼Œè‡ªåŠ¨è®¡ç®—ï¼‰',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_dish_step_config`
--

DROP TABLE IF EXISTS `t_dish_step_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_dish_step_config` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `merchant_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商家ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“IDï¼ˆä¸ºNULLè¡¨ç¤ºå…¨å±€é»˜è®¤é…ç½®ï¼‰',
  `step_code` int NOT NULL COMMENT 'æ­¥éª¤ä»£ç ',
  `step_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ­¥éª¤åç§°',
  `step_order` int NOT NULL COMMENT 'æ­¥éª¤é¡ºåº',
  `estimated_minutes` int DEFAULT NULL COMMENT '预计耗时（分钟）',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨ï¼š1-å¯ç”¨ 0-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_dish_step` (`merchant_id`,`dish_id`,`step_code`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èœå“æ­¥éª¤é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_dish_step_history`
--

DROP TABLE IF EXISTS `t_dish_step_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_dish_step_history` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `order_dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è®¢å•èœå“ID',
  `order_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è®¢å•ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `old_step_status` int DEFAULT NULL COMMENT 'åŽŸæ­¥éª¤çŠ¶æ€',
  `new_step_status` int NOT NULL COMMENT 'æ–°æ­¥éª¤çŠ¶æ€',
  `operation_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œç±»åž‹ï¼šFORWARD-å‰è¿›, BACKWARD-å›žé€€, SKIP-è·³è¿‡',
  `operator_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œäººID',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œäººå§“å',
  `rollback_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '回退原因',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `estimated_minutes` int DEFAULT NULL COMMENT 'é¢„è®¡å®Œæˆæ—¶é—´ï¼ˆåˆ†é’Ÿï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_dish_id` (`order_dish_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èœå“æ­¥éª¤åŽ†å²è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_extraction_config`
--

DROP TABLE IF EXISTS `t_extraction_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_extraction_config` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `platform` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å¹³å°',
  `config_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'é…ç½®é”®',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT 'é…ç½®å€¼',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é…ç½®æè¿°',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_key` (`platform`,`config_key`),
  KEY `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æå–é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_extraction_task`
--

DROP TABLE IF EXISTS `t_extraction_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_extraction_task` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `source_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å†…å®¹æºID',
  `task_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä»»åŠ¡ç±»åž‹ï¼šOCR-å›¾ç‰‡è¯†åˆ«, NLP-æ–‡æœ¬åˆ†æž, VIDEO-è§†é¢‘åˆ†æž',
  `task_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'ä»»åŠ¡çŠ¶æ€ï¼šPENDING-å¾…å¤„ç†, PROCESSING-å¤„ç†ä¸­, SUCCESS-æˆåŠŸ, FAILED-å¤±è´¥',
  `retry_count` int DEFAULT '0' COMMENT 'é‡è¯•æ¬¡æ•°',
  `priority` int DEFAULT '0' COMMENT '优先级（数字越大越优先）',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT 'ç»“æŸæ—¶é—´',
  `result_data` text COLLATE utf8mb4_unicode_ci COMMENT 'ç»“æžœæ•°æ®ï¼ˆJSONæ ¼å¼ï¼‰',
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é”™è¯¯ä¿¡æ¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æå–ä»»åŠ¡è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_festival`
--

DROP TABLE IF EXISTS `t_festival`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_festival` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `festival_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èŠ‚æ—¥åç§°',
  `festival_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节日类型：TRADITIONAL-传统节日, WESTERN-西方节日, SEASONAL-季节性, CUSTOM-自定义',
  `festival_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èŠ‚æ—¥æ—¥æœŸï¼ˆMM-ddæ ¼å¼ï¼Œå¦‚01-01ï¼‰',
  `year` int DEFAULT NULL COMMENT 'å¹´ä»½ï¼ˆNULLè¡¨ç¤ºæ¯å¹´é‡å¤ï¼‰',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èŠ‚æ—¥æè¿°',
  `icon` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节日图标',
  `background_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '背景图片',
  `theme_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主题颜色',
  `start_date` date DEFAULT NULL COMMENT '开始日期（用于季节性）',
  `end_date` date DEFAULT NULL COMMENT 'ç»“æŸæ—¥æœŸï¼ˆç”¨äºŽå­£èŠ‚æ€§ï¼‰',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºæƒé‡',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_festival_type` (`festival_type`),
  KEY `idx_festival_date` (`festival_date`),
  KEY `idx_start_end_date` (`start_date`,`end_date`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èŠ‚æ—¥è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_festival_dish_recommend`
--

DROP TABLE IF EXISTS `t_festival_dish_recommend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_festival_dish_recommend` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `festival_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '节日ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `recommend_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æŽ¨èç±»åž‹ï¼šMAIN-ä¸»æŽ¨, SECONDARY-æ¬¡æŽ¨, THEME-ä¸»é¢˜, SEASONAL-å­£èŠ‚',
  `recommend_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æŽ¨èç†ç”±',
  `position` int DEFAULT '0' COMMENT 'å±•ç¤ºä½ç½®ï¼ˆ0-é¦–é¡µ, 1-åˆ—è¡¨é¡¶, 2- bannerï¼‰',
  `priority` int DEFAULT '0' COMMENT 'ä¼˜å…ˆçº§ï¼ˆæ•°å­—è¶Šå¤§è¶Šé å‰ï¼‰',
  `click_count` int DEFAULT '0' COMMENT '点击次数统计',
  `order_count` int DEFAULT '0' COMMENT 'è®¢å•æ¬¡æ•°ç»Ÿè®¡',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_festival_dish` (`festival_id`,`dish_id`),
  KEY `idx_festival_id` (`festival_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_recommend_type` (`recommend_type`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èŠ‚æ—¥æŽ¨èèœå“å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group`
--

DROP TABLE IF EXISTS `t_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group` (
  `id` varchar(64) NOT NULL COMMENT '群组ID',
  `group_name` varchar(100) NOT NULL COMMENT '群名称',
  `creator_id` varchar(64) NOT NULL COMMENT '创建者ID',
  `description` varchar(500) DEFAULT NULL COMMENT '群描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_member`
--

DROP TABLE IF EXISTS `t_group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_id` varchar(64) NOT NULL COMMENT '群ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `role` varchar(20) DEFAULT 'member' COMMENT 'è§’è‰²ï¼šowner-ç¾¤ä¸»ï¼Œadmin-ç®¡ç†å‘˜ï¼Œmember-æ™®é€šæˆå‘˜',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¾¤æˆå‘˜å…³ç³»è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_order`
--

DROP TABLE IF EXISTS `t_group_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group_order` (
  `id` varchar(64) NOT NULL COMMENT '群订单ID',
  `initiator_id` varchar(64) NOT NULL,
  `merchant_id` varchar(64) DEFAULT NULL,
  `group_id` varchar(64) NOT NULL,
  `address_id` varchar(64) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL COMMENT 'è®¢å•å¤‡æ³¨',
  `status` tinyint DEFAULT '0' COMMENT 'è®¢å•çŠ¶æ€ï¼ˆ0-å¾…æ”¯ä»˜/1-å¾…æŽ¥å•/2-åˆ¶ä½œä¸­/3-å·²å®Œæˆ/4-å·²å–æ¶ˆï¼‰',
  `payment_mode` tinyint NOT NULL DEFAULT '0' COMMENT 'æ”¯ä»˜æ¨¡å¼: 0-ç»Ÿä¸€æ”¯ä»˜,1-ä¸ªäººå•ç‹¬æ”¯ä»˜',
  `add_dish_enabled` tinyint NOT NULL DEFAULT '1' COMMENT 'æ˜¯å¦å…è®¸åŠ èœ: 0-å¦,1-æ˜¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `total_amount` double DEFAULT '0' COMMENT 'è®¢å•æ€»é‡‘é¢',
  `max_participants` int NOT NULL DEFAULT '1' COMMENT '最大参与人数',
  `locked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已锁单：0-未锁单，1-已锁单',
  `confirmed_time` datetime DEFAULT NULL COMMENT '确认成团时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¾¤è®¢å•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_order_addition`
--

DROP TABLE IF EXISTS `t_group_order_addition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group_order_addition` (
  `id` varchar(32) NOT NULL COMMENT 'åŠ èœè®°å½•ID',
  `group_order_id` varchar(32) NOT NULL COMMENT 'ç¾¤è®¢å•ID',
  `group_id` varchar(32) NOT NULL COMMENT '群组ID',
  `user_id` varchar(32) NOT NULL COMMENT 'åŠ èœç”¨æˆ·ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT 'åŠ èœç”¨æˆ·å§“å',
  `dishes` json NOT NULL COMMENT 'åŠ èœèœå“åˆ—è¡¨',
  `total_amount` decimal(10,2) NOT NULL COMMENT 'åŠ èœæ€»é¢',
  `status` varchar(30) NOT NULL DEFAULT 'pending_review' COMMENT 'çŠ¶æ€ï¼špending_review(å¾…å®¡æ ¸)/approved_pending_payment(å®¡æ ¸é€šè¿‡å¾…æ”¯ä»˜)/rejected(å·²é©³å›ž)/paid(å·²æ”¯ä»˜)',
  `request_time` datetime NOT NULL COMMENT '请求时间',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
  `check_diet_restrictions` json DEFAULT NULL COMMENT 'é¥®é£Ÿç¦å¿Œæ£€æŸ¥ç»“æžœ',
  `related_order_id` varchar(32) DEFAULT NULL COMMENT 'å…³è”çš„åŠ èœè®¢å•ID',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_request_time` (`request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¾¤è®¢å•åŠ èœè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_order_dish`
--

DROP TABLE IF EXISTS `t_group_order_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group_order_dish` (
  `id` varchar(64) NOT NULL COMMENT 'ID',
  `group_order_id` varchar(64) NOT NULL COMMENT '群订单ID',
  `dish_id` varchar(64) NOT NULL COMMENT '菜品ID',
  `quantity` int NOT NULL COMMENT 'æ•°é‡',
  `customization` varchar(255) DEFAULT NULL COMMENT 'å®šåˆ¶è¦æ±‚',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¾¤è®¢å•èœå“è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_order_member`
--

DROP TABLE IF EXISTS `t_group_order_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='拼单成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_ingredient_conflict_rule`
--

DROP TABLE IF EXISTS `t_ingredient_conflict_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ingredient_conflict_rule` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则ID',
  `rule_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è§„åˆ™åç§°',
  `conflict_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å†²çªç±»åž‹ï¼šALLERGY-è¿‡æ•, INCOMPATIBLE-é£Ÿæå†²çª, CUISINE-çƒ¹é¥ªç¦å¿Œ',
  `main_ingredients` json NOT NULL COMMENT 'ä¸»è¦é£Ÿæï¼ˆJSONæ•°ç»„ï¼Œå¦‚["è¾£æ¤’", "è¾£æ¤’ç²‰"]ï¼‰',
  `conflict_tags` json NOT NULL COMMENT 'å†²çªæ ‡ç­¾ï¼ˆJSONæ•°ç»„ï¼Œå¦‚["mild_no_spicy", "no_spicy"]ï¼‰',
  `severity` int DEFAULT '2' COMMENT 'ä¸¥é‡ç¨‹åº¦ï¼š1-ä½Ž, 2-ä¸­, 3-é«˜',
  `priority` int DEFAULT '3' COMMENT 'æŽ¨èä¼˜å…ˆçº§ï¼š1-é«˜ï¼ˆçº¢è‰²ï¼‰, 2-ä¸­é«˜ï¼ˆé»„è‰²ï¼‰, 3-ä¸­ï¼ˆè“è‰²ï¼‰, 4-ä½Žï¼ˆç°è‰²ï¼‰',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å†²çªæè¿°',
  `suggestion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '建议内容',
  `enabled` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨ï¼š1-å¯ç”¨ 0-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_conflict_type` (`conflict_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='é£Ÿæå†²çªè§„åˆ™è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_menu`
--

DROP TABLE IF EXISTS `t_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_menu` (
  `id` varchar(64) NOT NULL COMMENT '菜单ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜单名称',
  `type` varchar(20) NOT NULL COMMENT '菜单类型（workday/weekend/afternoon_tea/package等）',
  `description` text COMMENT 'èœå•æè¿°',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '菜单状态（active/draft/inactive）',
  `auto_start_time` datetime DEFAULT NULL COMMENT '自动上架时间',
  `auto_end_time` datetime DEFAULT NULL COMMENT '自动下架时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_type_status` (`type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_menu_dish`
--

DROP TABLE IF EXISTS `t_menu_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_menu_dish` (
  `id` varchar(64) NOT NULL COMMENT '菜单菜品关联ID',
  `menu_id` varchar(64) NOT NULL COMMENT '菜单ID',
  `dish_id` varchar(64) NOT NULL COMMENT '菜品ID',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` int DEFAULT '1' COMMENT 'èœå“åœ¨èœå•ä¸­çš„çŠ¶æ€ï¼š1-ä¸Šæž¶ï¼Œ0-ä¸‹æž¶',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_dish` (`menu_id`,`dish_id`),
  KEY `idx_menu` (`menu_id`),
  KEY `idx_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单菜品关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_merchant`
--

DROP TABLE IF EXISTS `t_merchant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼šPENDING-å¾…å®¡æ ¸, APPROVED-å·²é€šè¿‡, REJECTED-å·²æ‹’ç»',
  `audit_reason` varchar(500) DEFAULT NULL COMMENT '审核备注/原因',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_by` varchar(50) DEFAULT NULL COMMENT '审核人ID',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_location` (`longitude`,`latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_message_record`
--

DROP TABLE IF EXISTS `t_message_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_message_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` bigint DEFAULT NULL COMMENT 'å‘é€è€…ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT 'æ¶ˆæ¯å†…å®¹',
  `message_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'text' COMMENT 'æ¶ˆæ¯ç±»åž‹ (text: æ–‡æœ¬, image: å›¾ç‰‡, file: æ–‡ä»¶)',
  `send_time` datetime DEFAULT NULL COMMENT 'å‘é€æ—¶é—´',
  `read_status` tinyint DEFAULT '0' COMMENT 'é˜…è¯»çŠ¶æ€ (0: æœªè¯», 1: å·²è¯»)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æ¶ˆæ¯è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_mock_oauth_user`
--

DROP TABLE IF EXISTS `t_mock_oauth_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_mock_oauth_user` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `provider` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'wechat/qq',
  `open_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟OpenID',
  `union_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模拟UnionID',
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模拟昵称',
  `avatar_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ¨¡æ‹Ÿå¤´åƒ',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ¨¡æ‹ŸæŽˆæƒå¯†ç ï¼ˆæ¼”ç¤ºç”¨ï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_openid` (`provider`,`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æ¨¡æ‹Ÿç¬¬ä¸‰æ–¹ç”¨æˆ·ï¼ˆæ¯•è®¾æ¼”ç¤ºï¼‰';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_notification`
--

DROP TABLE IF EXISTS `t_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_notification` (
  `id` varchar(64) NOT NULL COMMENT '通知ID',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `type` varchar(64) DEFAULT 'system' COMMENT '通知类型',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'å‘é€æ—¶é—´',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态（0-未读/1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_nutrition`
--

DROP TABLE IF EXISTS `t_nutrition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_nutrition` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `food_code` varchar(20) DEFAULT NULL COMMENT 'é£Ÿç‰©ç¼–ç ',
  `food_name` varchar(100) NOT NULL COMMENT 'é£Ÿç‰©åç§°',
  `edible` decimal(5,2) DEFAULT NULL COMMENT 'å¯é£Ÿéƒ¨(%)',
  `energy_kcal` decimal(10,2) DEFAULT NULL COMMENT 'èƒ½é‡(åƒå¡)',
  `energy_kj` decimal(10,2) DEFAULT NULL COMMENT 'èƒ½é‡(åƒç„¦)',
  `water` decimal(10,2) DEFAULT NULL COMMENT '水分(g)',
  `protein` decimal(10,2) DEFAULT NULL COMMENT '蛋白质(g)',
  `fat` decimal(10,2) DEFAULT NULL COMMENT 'è„‚è‚ª(g)',
  `cho` decimal(10,2) DEFAULT NULL COMMENT 'ç¢³æ°´åŒ–åˆç‰©(g)',
  `dietary_fiber` decimal(10,2) DEFAULT NULL COMMENT '膳食纤维(g)',
  `cholesterol` decimal(10,2) DEFAULT NULL COMMENT '胆固醇(mg)',
  `ash` decimal(10,2) DEFAULT NULL COMMENT 'ç°åˆ†(g)',
  `vitamin_a` decimal(10,2) DEFAULT NULL COMMENT '维生素A(μgRE)',
  `carotene` decimal(10,2) DEFAULT NULL COMMENT 'èƒ¡èåœç´ (Î¼g)',
  `retinol` decimal(10,2) DEFAULT NULL COMMENT '视黄醇(μg)',
  `thiamin` decimal(10,2) DEFAULT NULL COMMENT '硫胺素(mg)',
  `riboflavin` decimal(10,2) DEFAULT NULL COMMENT 'æ ¸é»„ç´ (mg)',
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
  `mg` decimal(10,2) DEFAULT NULL COMMENT 'é•(mg)',
  `fe` decimal(10,2) DEFAULT NULL COMMENT 'é“(mg)',
  `zn` decimal(10,2) DEFAULT NULL COMMENT '锌(mg)',
  `se` decimal(10,2) DEFAULT NULL COMMENT '硒(μg)',
  `cu` decimal(10,2) DEFAULT NULL COMMENT '铜(mg)',
  `mn` decimal(10,2) DEFAULT NULL COMMENT '锰(mg)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注说明',
  `data_source` varchar(50) DEFAULT 'ä¸­å›½é£Ÿç‰©æˆåˆ†è¡¨ç¬¬6ç‰ˆ' COMMENT 'æ•°æ®æ¥æº',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `food_code` (`food_code`),
  KEY `idx_food_name` (`food_name`),
  KEY `idx_energy_kcal` (`energy_kcal`),
  KEY `idx_protein` (`protein`),
  KEY `idx_fat` (`fat`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä¸­å›½é£Ÿç‰©æˆåˆ†è¡¨è¥å…»æ•°æ®';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_order`
--

DROP TABLE IF EXISTS `t_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_order` (
  `id` varchar(50) NOT NULL COMMENT 'è®¢å•ID',
  `parent_order_id` varchar(64) DEFAULT NULL COMMENT 'çˆ¶è®¢å•ID(åŠ èœè®¢å•å…³è”åŽŸè®¢å•)',
  `is_add_order` tinyint NOT NULL DEFAULT '0' COMMENT 'æ˜¯å¦ä¸ºåŠ èœè®¢å•: 0-å¦,1-æ˜¯',
  `add_dish_request_id` varchar(64) DEFAULT NULL COMMENT 'åŠ èœè¯·æ±‚ID',
  `add_dish_user_id` bigint DEFAULT NULL COMMENT 'åŠ èœäººID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `merchant_id` varchar(50) NOT NULL COMMENT '商家ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT 'è®¢å•çŠ¶æ€ï¼ˆ0-å¾…æ”¯ä»˜/1-å¾…æŽ¥å•/2-åˆ¶ä½œä¸­/3-å·²å®Œæˆ/4-å·²å–æ¶ˆï¼‰',
  `payment_id` varchar(50) DEFAULT NULL COMMENT '支付记录ID',
  `paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'å·²æ”¯ä»˜é‡‘é¢',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `address` varchar(255) NOT NULL COMMENT '配送地址',
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `address_id` varchar(50) DEFAULT NULL COMMENT 'é…é€åœ°å€ID',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_payment_id` (`payment_id`),
  KEY `idx_parent_order` (`parent_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_order_dish`
--

DROP TABLE IF EXISTS `t_order_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_order_dish` (
  `id` varchar(64) NOT NULL,
  `order_id` varchar(64) NOT NULL,
  `dish_id` varchar(64) NOT NULL,
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `customization` varchar(255) DEFAULT NULL COMMENT '定制要求',
  `step_status` int DEFAULT '0' COMMENT 'æ­¥éª¤çŠ¶æ€ï¼š0-å¾…å¤‡èœ 1-å¤‡èœä¸­ 2-é¢„å¤„ç†ä¸­ 3-çƒ¹é¥ªä¸­ 4-æ‘†ç›˜ä¸­ 5-å¾…ä¸Šèœ 6-å·²ä¸Šèœ 10-å¿«é¤åˆ¶ä½œä¸­ 11-å¿«é¤æ‰“åŒ…ä¸­ 12-å¿«é¤å¾…å‡ºé¤ 13-å¿«é¤å·²å‡ºé¤',
  `step_start_time` datetime DEFAULT NULL COMMENT 'å½“å‰æ­¥éª¤å¼€å§‹æ—¶é—´',
  `estimated_completion_time` datetime DEFAULT NULL COMMENT 'é¢„è®¡å®Œæˆæ—¶é—´',
  `cooking_minutes` int DEFAULT NULL COMMENT '烹饪耗时（分钟）',
  `step_sort` int DEFAULT '999' COMMENT 'æ­¥éª¤æŽ’åºï¼ˆä¼˜å…ˆçº§ï¼‰ï¼Œæ•°å€¼è¶Šå°è¶Šä¼˜å…ˆå¤„ç†',
  `is_fast_food` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ä¸ºå¿«é¤ï¼š1-æ˜¯ 0-å¦',
  `serving_status` int DEFAULT '0' COMMENT 'ä¸ŠèœçŠ¶æ€ï¼š0-æœªä¸Šèœ 1-å·²ä¸Šèœ 2-å·²æ’¤é¤',
  `is_add_dish` tinyint NOT NULL DEFAULT '0' COMMENT 'æ˜¯å¦åŠ èœ: 0-å¦,1-æ˜¯',
  `add_dish_user_id` bigint DEFAULT NULL COMMENT 'åŠ èœäººID',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_dish` (`dish_id`),
  KEY `idx_step_status` (`step_status`),
  KEY `idx_step_sort` (`step_sort`),
  KEY `idx_order_step_status` (`order_id`,`step_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单菜品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_payment_record`
--

DROP TABLE IF EXISTS `t_payment_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_payment_record` (
  `id` varchar(64) NOT NULL COMMENT '支付记录ID',
  `payment_no` varchar(64) NOT NULL COMMENT 'æ”¯ä»˜æµæ°´å·',
  `order_id` varchar(64) NOT NULL COMMENT 'è®¢å•ID',
  `user_id` varchar(64) NOT NULL COMMENT '支付用户ID',
  `merchant_id` varchar(64) NOT NULL COMMENT '商家ID',
  `amount` decimal(10,2) NOT NULL COMMENT 'æ”¯ä»˜é‡‘é¢',
  `payment_method` varchar(20) NOT NULL DEFAULT 'wallet' COMMENT 'æ”¯ä»˜æ–¹å¼ï¼šwallet-é’±åŒ…, wechat-å¾®ä¿¡, alipay-æ”¯ä»˜å®',
  `payment_status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT 'æ”¯ä»˜çŠ¶æ€ï¼špending-å¾…æ”¯ä»˜, success-æˆåŠŸ, failed-å¤±è´¥, refund-å·²é€€æ¬¾',
  `transaction_id` varchar(128) DEFAULT NULL COMMENT '第三方交易ID',
  `paid_time` datetime DEFAULT NULL COMMENT 'æ”¯ä»˜å®Œæˆæ—¶é—´',
  `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT 'é€€æ¬¾é‡‘é¢',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ”¯ä»˜è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_permission`
--

DROP TABLE IF EXISTS `t_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_permission` (
  `permission_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'æƒé™ID',
  `permission_name` varchar(100) NOT NULL COMMENT 'æƒé™åç§°',
  `permission_code` varchar(50) NOT NULL COMMENT 'æƒé™ç¼–ç ',
  `resource_type` varchar(20) NOT NULL COMMENT 'èµ„æºç±»åž‹ï¼šMENU-èœå•, BUTTON-æŒ‰é’®, API-æŽ¥å£',
  `parent_id` bigint DEFAULT '0' COMMENT 'çˆ¶æƒé™IDï¼ˆ0è¡¨ç¤ºé¡¶çº§æƒé™ï¼‰',
  `path` varchar(100) DEFAULT NULL COMMENT 'è·¯ç”±è·¯å¾„ï¼ˆèœå•ç±»åž‹ä½¿ç”¨ï¼‰',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `description` varchar(200) DEFAULT NULL COMMENT 'æƒé™æè¿°',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºåºå·',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€ï¼šACTIVE-å¯ç”¨, DISABLED-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `permission_code` (`permission_code`),
  KEY `idx_permission_code` (`permission_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æƒé™è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_recharge_record`
--

DROP TABLE IF EXISTS `t_recharge_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_recharge_record` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '充值记录ID',
  `recharge_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å……å€¼æµæ°´å·',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT 'å……å€¼é‡‘é¢',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ”¯ä»˜æ–¹å¼ï¼šwechat-å¾®ä¿¡, alipay-æ”¯ä»˜å®, bank-é“¶è¡Œå¡',
  `recharge_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'å……å€¼çŠ¶æ€ï¼špending-å¾…æ”¯ä»˜, success-æˆåŠŸ, failed-å¤±è´¥',
  `transaction_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方交易ID',
  `paid_time` datetime DEFAULT NULL COMMENT 'æ”¯ä»˜å®Œæˆæ—¶é—´',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `recharge_no` (`recharge_no`),
  UNIQUE KEY `uk_recharge_no` (`recharge_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`recharge_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å……å€¼è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_recipe`
--

DROP TABLE IF EXISTS `t_recipe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  `custom_nutrition` text COMMENT 'è‡ªå®šä¹‰è¥å…»ä¿¡æ¯(JSONæ ¼å¼)',
  `cook_time` varchar(50) DEFAULT NULL COMMENT '烹饪时间',
  `is_favorite` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否收藏: 0-否, 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `items` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2062397879099817986 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食谱表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_refund_record`
--

DROP TABLE IF EXISTS `t_refund_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_refund_record` (
  `refund_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '退款记录ID',
  `refund_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'é€€æ¬¾æµæ°´å·',
  `order_id` bigint NOT NULL COMMENT 'è®¢å•ID',
  `order_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è®¢å•å·',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç”¨æˆ·å',
  `refund_amount` decimal(10,2) NOT NULL COMMENT 'é€€æ¬¾é‡‘é¢',
  `reason` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é€€æ¬¾åŽŸå› ï¼šQUALITY-å•†å“è´¨é‡é—®é¢˜, DONT_WANT-ä¸æƒ³è¦äº†, DELAY-é…é€å»¶è¿Ÿ, OTHER-å…¶ä»–',
  `reason_description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é€€æ¬¾åŽŸå› æè¿°',
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'é€€æ¬¾çŠ¶æ€ï¼šPENDING-å¾…å®¡æ ¸, APPROVED-å·²é€šè¿‡, REJECTED-å·²æ‹’ç», PROCESSING-å¤„ç†ä¸­, SUCCESS-æˆåŠŸ, FAILED-å¤±è´¥',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `process_time` datetime DEFAULT NULL COMMENT 'å¤„ç†æ—¶é—´',
  `audit_by` bigint DEFAULT NULL COMMENT '审核人ID',
  `audit_by_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸äººåç§°',
  `process_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¤„ç†å¤‡æ³¨',
  `refund_method` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é€€æ¬¾æ–¹å¼ï¼šORIGINAL-åŽŸè·¯é€€å›ž, BALANCE-é€€å›žä½™é¢',
  `transaction_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç¬¬ä¸‰æ–¹é€€æ¬¾æµæ°´å·',
  `complete_time` datetime DEFAULT NULL COMMENT 'å®Œæˆæ—¶é—´',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`refund_id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='é€€æ¬¾è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_reject_recommendation`
--

DROP TABLE IF EXISTS `t_reject_recommendation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_reject_recommendation` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `dish_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `reject_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'æ‹’ç»æ—¶é—´',
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ‹’ç»åŽŸå› ',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_user_dish` (`user_id`,`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æŽ¨èæ‹’ç»è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_review`
--

DROP TABLE IF EXISTS `t_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_review` (
  `id` varchar(50) NOT NULL COMMENT 'è¯„ä»·ID',
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `merchant_id` varchar(50) NOT NULL COMMENT '商家ID',
  `order_id` varchar(50) NOT NULL COMMENT 'è®¢å•ID',
  `dish_id` varchar(50) DEFAULT NULL COMMENT 'èœå“ID',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_review_reply`
--

DROP TABLE IF EXISTS `t_review_reply`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_review_reply` (
  `id` varchar(50) NOT NULL COMMENT 'å›žå¤ID',
  `review_id` varchar(50) NOT NULL COMMENT 'è¯„ä»·ID',
  `merchant_id` varchar(50) NOT NULL COMMENT '商家ID',
  `content` varchar(500) DEFAULT NULL COMMENT '回复内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_additional` tinyint DEFAULT '0' COMMENT 'æ˜¯å¦ä¸ºè¿½åŠ è¯„ä»·(0-å•†å®¶å›žå¤,1-ç”¨æˆ·è¿½è¯„)',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价回复表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_role`
--

DROP TABLE IF EXISTS `t_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) NOT NULL COMMENT 'è§’è‰²åç§°',
  `role_code` varchar(20) NOT NULL COMMENT 'è§’è‰²ç¼–ç ï¼šSUPER_ADMIN-è¶…çº§ç®¡ç†å‘˜, ADMIN-æ™®é€šç®¡ç†å‘˜, AUDITOR-å®¡æ ¸å‘˜',
  `description` varchar(255) DEFAULT NULL COMMENT 'è§’è‰²æè¿°',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€ï¼šACTIVE-å¯ç”¨, DISABLED-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºåºå·',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_code` (`role_code`),
  KEY `idx_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§’è‰²è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_role_permission`
--

DROP TABLE IF EXISTS `t_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT 'æƒé™ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§’è‰²æƒé™å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_role_permission_relation`
--

DROP TABLE IF EXISTS `t_role_permission_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role_permission_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å…³è”ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT 'æƒé™ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=264 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§’è‰²-æƒé™å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_scheduled_task`
--

DROP TABLE IF EXISTS `t_scheduled_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_scheduled_task` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务ID',
  `task_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä»»åŠ¡åç§°',
  `task_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä»»åŠ¡ä»£ç ',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'ä»»åŠ¡æè¿°',
  `cron_expression` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Cronè¡¨è¾¾å¼',
  `task_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä»»åŠ¡ç±»åž‹ï¼šONCE-ä¸€æ¬¡æ€§, CRON-Cronè¡¨è¾¾å¼, FIXED_RATE-å›ºå®šé¢‘çŽ‡, FIXED_DELAY-å›ºå®šå»¶è¿Ÿ',
  `rate_in_millis` bigint DEFAULT NULL COMMENT '执行频率（毫秒）',
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ENABLED' COMMENT 'ä»»åŠ¡çŠ¶æ€ï¼šENABLED-å¯ç”¨, DISABLED-ç¦ç”¨, PAUSED-æš‚åœ',
  `task_class_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä»»åŠ¡ç±»å',
  `task_method_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä»»åŠ¡æ–¹æ³•å',
  `task_params` text COLLATE utf8mb4_unicode_ci COMMENT 'ä»»åŠ¡å‚æ•°ï¼ˆJSONæ ¼å¼ï¼‰',
  `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `execute_count` int DEFAULT '0' COMMENT '执行次数',
  `fail_count` int DEFAULT '0' COMMENT '失败次数',
  `last_execute_result` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æœ€åŽæ‰§è¡Œç»“æžœï¼šSUCCESS-æˆåŠŸ, FAILED-å¤±è´¥, RUNNING-è¿è¡Œä¸­',
  `last_error_message` text COLLATE utf8mb4_unicode_ci COMMENT 'æœ€åŽæ‰§è¡Œé”™è¯¯ä¿¡æ¯',
  `log_execution` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦è®°å½•æ‰§è¡Œæ—¥å¿—',
  `timeout_seconds` int DEFAULT '0' COMMENT 'è¶…æ—¶æ—¶é—´ï¼ˆç§’ï¼‰ï¼Œ0è¡¨ç¤ºä¸é™åˆ¶',
  `retry_count` int DEFAULT '0' COMMENT 'é‡è¯•æ¬¡æ•°',
  `retried_count` int DEFAULT '0' COMMENT 'å·²é‡è¯•æ¬¡æ•°',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å®šæ—¶ä»»åŠ¡è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_scheduled_task_log`
--

DROP TABLE IF EXISTS `t_scheduled_task_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_scheduled_task_log` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志ID',
  `task_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '任务ID',
  `task_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä»»åŠ¡åç§°',
  `task_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä»»åŠ¡ä»£ç ',
  `start_time` datetime DEFAULT NULL COMMENT '执行开始时间',
  `end_time` datetime DEFAULT NULL COMMENT 'æ‰§è¡Œç»“æŸæ—¶é—´',
  `duration` bigint DEFAULT NULL COMMENT '执行时长（毫秒）',
  `execute_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ‰§è¡ŒçŠ¶æ€ï¼šSUCCESS-æˆåŠŸ, FAILED-å¤±è´¥, TIMEOUT-è¶…æ—¶',
  `result_message` text COLLATE utf8mb4_unicode_ci COMMENT 'æ‰§è¡Œç»“æžœä¿¡æ¯',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT 'é”™è¯¯ä¿¡æ¯',
  `exception_stack` text COLLATE utf8mb4_unicode_ci COMMENT '异常堆栈',
  `execute_params` text COLLATE utf8mb4_unicode_ci COMMENT 'æ‰§è¡Œå‚æ•°ï¼ˆJSONæ ¼å¼ï¼‰',
  `execute_result` text COLLATE utf8mb4_unicode_ci COMMENT 'æ‰§è¡Œè¿”å›žç»“æžœï¼ˆJSONæ ¼å¼ï¼‰',
  `server_ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æœåŠ¡å™¨IP',
  `server_hostname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æœåŠ¡å™¨ä¸»æœºå',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_task_code` (`task_code`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_execute_status` (`execute_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å®šæ—¶ä»»åŠ¡æ‰§è¡Œæ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_system_log`
--

DROP TABLE IF EXISTS `t_system_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_system_log` (
  `log_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日志ID',
  `operation_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œç±»åž‹',
  `module` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œæ¨¡å—',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œæè¿°',
  `operator_id` bigint DEFAULT NULL COMMENT 'æ“ä½œäººID',
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œäººåç§°',
  `operator_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œäººç±»åž‹',
  `method` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求方法',
  `params` text COLLATE utf8mb4_unicode_ci COMMENT 'è¯·æ±‚å‚æ•°',
  `result` text COLLATE utf8mb4_unicode_ci COMMENT '返回结果',
  `execute_time` bigint DEFAULT NULL COMMENT '执行时长(毫秒)',
  `ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IPåœ°å€',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œçŠ¶æ€',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT 'é”™è¯¯ä¿¡æ¯',
  `browser` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æµè§ˆå™¨ç±»åž‹',
  `os` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œç³»ç»Ÿ',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_module` (`module`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç³»ç»Ÿæ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(64) NOT NULL COMMENT '密码（BCrypt加密）',
  `payment_password` varchar(255) DEFAULT NULL COMMENT 'æ”¯ä»˜å¯†ç ï¼ˆåŠ å¯†å­˜å‚¨ï¼‰',
  `has_payment_password` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å·²è®¾ç½®æ”¯ä»˜å¯†ç ï¼š0-æœªè®¾ç½®ï¼Œ1-å·²è®¾ç½®',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_user_custom_event`
--

DROP TABLE IF EXISTS `t_user_custom_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_custom_event` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `event_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'äº‹ä»¶åç§°',
  `event_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'äº‹ä»¶ç±»åž‹ï¼šBIRTHDAY-ç”Ÿæ—¥, ANNIVERSARY-çºªå¿µæ—¥, PARTY-èšä¼š, OTHER-å…¶ä»–',
  `event_date` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'äº‹ä»¶æ—¥æœŸï¼ˆMM-ddæ ¼å¼ï¼‰',
  `year` int DEFAULT NULL COMMENT 'å¹´ä»½ï¼ˆNULLè¡¨ç¤ºæ¯å¹´é‡å¤ï¼‰',
  `reminder_days` int DEFAULT '3' COMMENT 'æå‰æé†’å¤©æ•°',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'äº‹ä»¶æè¿°',
  `preferred_dishes` json DEFAULT NULL COMMENT 'åå¥½èœå“åˆ—è¡¨ï¼ˆJSONæ•°ç»„ï¼‰',
  `guest_count` int DEFAULT NULL COMMENT 'é¢„è®¡ç”¨é¤äººæ•°',
  `budget_per_person` decimal(10,2) DEFAULT NULL COMMENT 'äººå‡é¢„ç®—',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_event_date` (`event_date`),
  KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·è‡ªå®šä¹‰äº‹ä»¶è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_user_oauth`
--

DROP TABLE IF EXISTS `t_user_oauth`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_oauth` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID (OA + 16ä½æ•°å­—)',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å…³è”çš„æœ¬åœ°ç”¨æˆ·ID',
  `provider` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å¹³å°æ ‡è¯†: wechat/qq',
  `open_id` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OpenID',
  `union_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¾®ä¿¡UnionIDï¼ˆæ‰“é€šå°ç¨‹åºï¼‰',
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '第三方昵称',
  `avatar_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç¬¬ä¸‰æ–¹å¤´åƒURL',
  `access_token` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'access_token（加密存储）',
  `refresh_token` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'refresh_token（加密存储）',
  `token_expires_at` datetime DEFAULT NULL COMMENT 'token过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_openid` (`provider`,`open_id`),
  UNIQUE KEY `uk_provider_unionid` (`provider`,`union_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç¬¬ä¸‰æ–¹OAuthè´¦å·å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_user_preference`
--

DROP TABLE IF EXISTS `t_user_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tag_weights` varchar(1000) DEFAULT '{}' COMMENT '标签权重，JSON格式（如 {"spicy": 0.1, "sweet": 0.8}）',
  `disable_weather_recommend` bit(1) DEFAULT b'0' COMMENT '是否关闭天气推荐',
  `diet_goal` varchar(50) DEFAULT NULL COMMENT '饮食目标（如 low_calorie, high_protein 等）',
  `allergies` varchar(500) DEFAULT '[]' COMMENT '过敏食材列表，JSON格式',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `enable_ai_personal_data` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å…è®¸AIä½¿ç”¨ä¸ªäººæ•°æ®ï¼š0-ä¸å…è®¸ï¼Œ1-å…è®¸',
  `enable_order_notification` tinyint(1) DEFAULT '1' COMMENT 'è®¢å•é€šçŸ¥å¼€å…³',
  `enable_activity_notification` tinyint(1) DEFAULT '1' COMMENT '活动通知开关',
  `enable_merchant_reply_notification` tinyint(1) DEFAULT '1' COMMENT 'å•†å®¶å›žå¤é€šçŸ¥å¼€å…³',
  `enable_group_chat_notification` tinyint(1) DEFAULT '1' COMMENT 'ç¾¤èŠæ¶ˆæ¯é€šçŸ¥å¼€å…³',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2021410848815214594 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户推荐偏好表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_user_recommend_history`
--

DROP TABLE IF EXISTS `t_user_recommend_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_recommend_history` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `festival_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '节日ID',
  `custom_event_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '自定义事件ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `recommend_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æŽ¨èç±»åž‹',
  `is_clicked` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ç‚¹å‡»',
  `is_ordered` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ä¸‹å•',
  `feedback_score` int DEFAULT NULL COMMENT 'åé¦ˆè¯„åˆ†ï¼ˆ1-5ï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_festival_id` (`festival_id`),
  KEY `idx_custom_event_id` (`custom_event_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·æŽ¨èè®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_wallet`
--

DROP TABLE IF EXISTS `t_wallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_wallet` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '钱包ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'å½“å‰ä½™é¢ï¼ˆå…ƒï¼‰',
  `total_recharge` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ç´¯è®¡å……å€¼é‡‘é¢',
  `total_consume` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ç´¯è®¡æ¶ˆè´¹é‡‘é¢',
  `total_withdraw` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ç´¯è®¡æçŽ°é‡‘é¢',
  `version` int NOT NULL DEFAULT '0' COMMENT 'ä¹è§‚é”ç‰ˆæœ¬å·',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT 'é’±åŒ…çŠ¶æ€ï¼šactive-æ­£å¸¸, frozen-å†»ç»“',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `locked` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦é”å®šï¼ˆç”¨äºŽé’±åŒ…å®‰å…¨è®¾ç½®ï¼‰0-æœªé”å®š, 1-é”å®š',
  `verify_enabled` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¼€å¯æ”¯ä»˜éªŒè¯ 0-å…³é—­, 1-å¼€å¯',
  `daily_limit` decimal(10,2) DEFAULT '5000.00' COMMENT 'å•æ—¥äº¤æ˜“é™é¢',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·é’±åŒ…è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_wish_list_item`
--

DROP TABLE IF EXISTS `t_wish_list_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_wish_list_item` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `merchant_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å•†å®¶IDï¼ˆå¯é€‰ï¼ŒæŒ‡å®šå•†å®¶çš„éœ€æ±‚ï¼‰',
  `dish_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“åç§°',
  `dish_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“å›¾ç‰‡URL',
  `taste_requirement` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å£å‘³è¦æ±‚',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'è¯¦ç»†æè¿°',
  `recipe_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å‚è€ƒé£Ÿè°±ID',
  `audit_status` int DEFAULT '0' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼š0-å¾…å®¡æ ¸, 1-å·²é€šè¿‡, 2-å·²æ‹’ç», 3-ç”³è¯‰ä¸­, 4-ç”³è¯‰æˆåŠŸ, 5-ç”³è¯‰å¤±è´¥, 6-è¶…æ—¶è‡ªåŠ¨é€šè¿‡, 7-å·²æ’¤å›ž',
  `rejection_reason_code` int DEFAULT NULL COMMENT 'æ‹’ç»åŽŸå› ä»£ç ',
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ‹’ç»åŽŸå› è¯´æ˜Ž',
  `audit_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商家审核备注',
  `auditor_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸äººå§“å',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `is_appealed` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ç”³è¯‰',
  `appeal_content` text COLLATE utf8mb4_unicode_ci COMMENT '申诉内容',
  `appeal_time` datetime DEFAULT NULL COMMENT '申诉时间',
  `appeal_reply` text COLLATE utf8mb4_unicode_ci COMMENT 'ç”³è¯‰å›žå¤',
  `appeal_reply_time` datetime DEFAULT NULL COMMENT 'ç”³è¯‰å›žå¤æ—¶é—´',
  `appeal_replier_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç”³è¯‰å›žå¤äººID',
  `expected_available_time` datetime DEFAULT NULL COMMENT '期望上架时间',
  `actual_available_time` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®žé™…ä¸Šæž¶æ—¶é—´ï¼ˆå®¡æ ¸é€šè¿‡åŽå•†å®¶å¡«å†™çš„é¢„è®¡æ—¶é—´ï¼‰',
  `timeout_time` datetime NOT NULL COMMENT 'è¶…æ—¶æ—¶é—´ï¼ˆ24å°æ—¶è‡ªåŠ¨é€šè¿‡ï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_timeout_time` (`timeout_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æƒ³åƒåˆ—è¡¨é¡¹è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_withdraw_record`
--

DROP TABLE IF EXISTS `t_withdraw_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_withdraw_record` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æçŽ°è®°å½•ID',
  `withdraw_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æçŽ°æµæ°´å·',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT 'æçŽ°é‡‘é¢',
  `fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '手续费',
  `actual_amount` decimal(10,2) NOT NULL COMMENT 'å®žé™…åˆ°è´¦é‡‘é¢',
  `withdraw_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æçŽ°æ–¹å¼ï¼šwechat-å¾®ä¿¡, alipay-æ”¯ä»˜å®, bank-é“¶è¡Œå¡',
  `account_info` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æçŽ°è´¦å·ä¿¡æ¯ï¼ˆè„±æ•ï¼‰',
  `withdraw_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'æçŽ°çŠ¶æ€ï¼špending-å¾…å®¡æ ¸, approved-å·²é€šè¿‡, rejected-å·²æ‹’ç», processing-å¤„ç†ä¸­, success-æˆåŠŸ, failed-å¤±è´¥',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `audit_user` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人',
  `complete_time` datetime DEFAULT NULL COMMENT 'å®Œæˆæ—¶é—´',
  `reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ‹’ç»åŽŸå› ',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `withdraw_no` (`withdraw_no`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`withdraw_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æçŽ°è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tutorial`
--

DROP TABLE IF EXISTS `tutorial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tutorial` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教程标题',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '教程类型：video/article',
  `duration` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '时长',
  `views` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '观看/阅读量',
  `source_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ADMIN' COMMENT 'æ¥æºç±»åž‹: ADMIN-ç®¡ç†å‘˜, MERCHANT-å•†å®¶, AI_GENERATED-AIç”Ÿæˆ',
  `source_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ¥æºID: ç®¡ç†å‘˜ID/å•†å®¶ID/ç”¨æˆ·ID/AIç‰ˆæœ¬',
  `author_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者类型: ADMIN, MERCHANT, AI',
  `author_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者ID',
  `featured` tinyint(1) DEFAULT '0' COMMENT '是否在首页推荐',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PUBLISHED' COMMENT 'çŠ¶æ€: DRAFT-è‰ç¨¿, PENDING-å¾…å®¡æ ¸, PUBLISHED-å·²å‘å¸ƒ, REJECTED-å·²æ‹’ç»',
  `review_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸çŠ¶æ€: NOT_SUBMITTED-æœªæäº¤, PENDING-å¾…å®¡æ ¸, APPROVED-é€šè¿‡, REJECTED-æ‹’ç»',
  `reviewer_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_comment` text COLLATE utf8mb4_unicode_ci COMMENT 'å®¡æ ¸æ„è§',
  `is_official` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å®˜æ–¹è®¤è¯(ä»…ç®¡ç†å‘˜å‘å¸ƒ)',
  `linked_merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”å•†å®¶IDï¼ˆå•†å®¶æ•™ç¨‹å¯ç”¨ï¼‰',
  `linked_dish_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”èœå“IDï¼ˆå•†å®¶æ•™ç¨‹å¯ç”¨ï¼‰',
  `ai_model_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'AI模型版本(AI教程)',
  `tags` json DEFAULT NULL COMMENT 'æ ‡ç­¾æ•°ç»„: ["å¥åº·", "ä½Žå¡", "ç´ é£Ÿ"]',
  `difficulty` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'éš¾åº¦: BEGINNER-åˆçº§, INTERMEDIATE-ä¸­çº§, ADVANCED-é«˜çº§',
  `calories` int DEFAULT NULL COMMENT 'å¡è·¯é‡Œ',
  `prep_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '准备时间',
  `servings` int DEFAULT NULL COMMENT 'ä»½é‡',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT '评分(0-5)',
  `rating_count` int DEFAULT '0' COMMENT '评分人数',
  `favorite_count` int DEFAULT '0' COMMENT 'æ”¶è—æ¬¡æ•°',
  `view_count` int DEFAULT '0' COMMENT 'æµè§ˆæ¬¡æ•°',
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
) ENGINE=InnoDB AUTO_INCREMENT=2057327777420861443 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='制作教程与指南表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_behavior`
--

DROP TABLE IF EXISTS `user_behavior`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `behavior_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è¡Œä¸ºç±»åž‹: view(æµè§ˆ)/click(ç‚¹å‡»)/order(ä¸‹å•)/favorite(æ”¶è—)/reject(æ‹’ç»)/share(åˆ†äº«)',
  `item_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç‰©å“ç±»åž‹: dish(èœå“)/merchant(å•†å®¶)/recipe(é£Ÿè°±)',
  `item_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç‰©å“ID',
  `context` json DEFAULT NULL COMMENT 'ä¸Šä¸‹æ–‡ä¿¡æ¯: {time, weather, location, device}',
  `duration` int DEFAULT NULL COMMENT 'è¡Œä¸ºæŒç»­æ—¶é•¿(ç§’), æµè§ˆç±»è¡Œä¸ºä½¿ç”¨',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '行为时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`created_time`),
  KEY `idx_item_time` (`item_id`,`created_time`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_user_behavior_type` (`user_id`,`behavior_type`,`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·è¡Œä¸ºè®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_coupon`
--

DROP TABLE IF EXISTS `user_coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_coupon` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券ID',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¼˜æƒ åˆ¸åç§°',
  `amount` decimal(10,2) NOT NULL COMMENT 'ä¼˜æƒ é‡‘é¢',
  `min_amount` decimal(10,2) DEFAULT '0.00' COMMENT 'æœ€ä½Žæ¶ˆè´¹é‡‘é¢',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'available' COMMENT 'ä¼˜æƒ åˆ¸çŠ¶æ€ï¼šavailable-å¯ç”¨, used-å·²ä½¿ç”¨, expired-å·²è¿‡æœŸ',
  `order_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”è®¢å•ID',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·ä¼˜æƒ åˆ¸è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_profile`
--

DROP TABLE IF EXISTS `user_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID',
  `preference_tags` json DEFAULT NULL COMMENT 'åå¥½æ ‡ç­¾: [{"tag": "å·èœ", "score": 0.8}, ...]',
  `dietary_restrictions` json DEFAULT NULL COMMENT 'é¥®é£Ÿç¦å¿Œ: ["è¿‡æ•åŽŸ", "å®—æ•™ç¦å¿Œ"]',
  `flavor_preference` json DEFAULT NULL COMMENT 'å£å‘³åå¥½: {"spicy": 0.7, "sweet": 0.3, "salty": 0.5}',
  `price_preference` json DEFAULT NULL COMMENT 'ä»·æ ¼åå¥½: {"min": 10, "max": 50, "optimal": 25}',
  `nutrition_goals` json DEFAULT NULL COMMENT 'è¥å…»ç›®æ ‡: {"calories": 2000, "protein": 100}',
  `meal_pattern` json DEFAULT NULL COMMENT 'ç”¨é¤æ¨¡å¼: {"breakfast": "07:00", "lunch": "12:00", "dinner": "18:30"}',
  `statistics` json DEFAULT NULL COMMENT 'ç»Ÿè®¡æ•°æ®: {total_orders, avg_order_amount, fav_categories}',
  `last_updated` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æœ€åŽæ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·ç”»åƒè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `v_active_festivals`
--

DROP TABLE IF EXISTS `v_active_festivals`;
/*!50001 DROP VIEW IF EXISTS `v_active_festivals`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_active_festivals` AS SELECT 
 1 AS `id`,
 1 AS `festival_name`,
 1 AS `festival_type`,
 1 AS `festival_date`,
 1 AS `year`,
 1 AS `description`,
 1 AS `icon`,
 1 AS `background_image`,
 1 AS `theme_color`,
 1 AS `start_date`,
 1 AS `end_date`,
 1 AS `is_active`,
 1 AS `sort_order`,
 1 AS `create_time`,
 1 AS `update_time`,
 1 AS `is_current`,
 1 AS `days_until_festival`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_content_extraction_overview`
--

DROP TABLE IF EXISTS `v_content_extraction_overview`;
/*!50001 DROP VIEW IF EXISTS `v_content_extraction_overview`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_content_extraction_overview` AS SELECT 
 1 AS `id`,
 1 AS `user_id`,
 1 AS `content_url`,
 1 AS `content_type`,
 1 AS `platform`,
 1 AS `title`,
 1 AS `author`,
 1 AS `cover_image`,
 1 AS `extraction_status`,
 1 AS `dish_name`,
 1 AS `dish_image`,
 1 AS `description`,
 1 AS `ingredients`,
 1 AS `steps`,
 1 AS `cooking_time`,
 1 AS `difficulty`,
 1 AS `tags`,
 1 AS `calories`,
 1 AS `is_published`,
 1 AS `recipe_id`,
 1 AS `is_verified`,
 1 AS `create_time`,
 1 AS `extraction_time`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_dish_popularity`
--

DROP TABLE IF EXISTS `v_dish_popularity`;
/*!50001 DROP VIEW IF EXISTS `v_dish_popularity`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_dish_popularity` AS SELECT 
 1 AS `dish_id`,
 1 AS `dish_name`,
 1 AS `view_count`,
 1 AS `order_count`,
 1 AS `favorite_count`,
 1 AS `avg_rating`,
 1 AS `feature_popularity`,
 1 AS `computed_popularity`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_order_dish_step_overview`
--

DROP TABLE IF EXISTS `v_order_dish_step_overview`;
/*!50001 DROP VIEW IF EXISTS `v_order_dish_step_overview`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_order_dish_step_overview` AS SELECT 
 1 AS `order_dish_id`,
 1 AS `order_id`,
 1 AS `dish_id`,
 1 AS `dish_name`,
 1 AS `quantity`,
 1 AS `step_status`,
 1 AS `step_start_time`,
 1 AS `estimated_completion_time`,
 1 AS `cooking_minutes`,
 1 AS `step_sort`,
 1 AS `is_fast_food`,
 1 AS `serving_status`,
 1 AS `order_status`,
 1 AS `step_change_count`,
 1 AS `last_step_change_time`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_behavior_stats`
--

DROP TABLE IF EXISTS `v_user_behavior_stats`;
/*!50001 DROP VIEW IF EXISTS `v_user_behavior_stats`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_behavior_stats` AS SELECT 
 1 AS `user_id`,
 1 AS `behavior_type`,
 1 AS `behavior_count`,
 1 AS `unique_items`,
 1 AS `first_behavior`,
 1 AS `last_behavior`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping routines for database 'jia_shi_yi_xuan'
--
/*!50003 DROP FUNCTION IF EXISTS `generate_session_id` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` FUNCTION `generate_session_id`(user1 VARCHAR(64), user2 VARCHAR(64)) RETURNS varchar(128) CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci
    READS SQL DATA
    DETERMINISTIC
    COMMENT 'ç”Ÿæˆå•èŠä¼šè¯ID(ä¸¤ä¸ªç”¨æˆ·IDæŒ‰å­—å…¸åºæ‹¼æŽ¥)'
BEGIN
  DECLARE result VARCHAR(128);

  -- 按字典序排序后拼接，确保唯一性
  IF user1 < user2 THEN
    SET result = CONCAT(user1, '_', user2);
  ELSE
    SET result = CONCAT(user2, '_', user1);
  END IF;

  RETURN result;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_calculate_dish_popularity` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_calculate_dish_popularity`()
BEGIN
  -- 更新所有菜品的 popularity_score
  UPDATE dish_features df
  JOIN (
    SELECT
      CAST(d.id AS CHAR) as dish_id,
      (LEAST(COALESCE(d.view_count, 0), 1000) / 1000 * 30 +
       LEAST(COALESCE(d.order_count, 0), 500) / 500 * 50 +
       LEAST(COALESCE(d.favorite_count, 0), 200) / 200 * 20) as base_popularity
    FROM t_dish d
  ) calc ON df.dish_id = calc.dish_id
  SET df.popularity_score = LEAST(base_popularity, 100) / 100;
  
  -- 为有评分的菜品加上评分权重
  UPDATE dish_features df
  JOIN t_dish d ON df.dish_id = CAST(d.id AS CHAR)
  SET df.popularity_score = df.popularity_score + (COALESCE(d.avg_rating, 0) / 5 * 0.1)
  WHERE d.avg_rating IS NOT NULL;
  
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Final view structure for view `v_active_festivals`
--

/*!50001 DROP VIEW IF EXISTS `v_active_festivals`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_active_festivals` AS select `f`.`id` AS `id`,`f`.`festival_name` AS `festival_name`,`f`.`festival_type` AS `festival_type`,`f`.`festival_date` AS `festival_date`,`f`.`year` AS `year`,`f`.`description` AS `description`,`f`.`icon` AS `icon`,`f`.`background_image` AS `background_image`,`f`.`theme_color` AS `theme_color`,`f`.`start_date` AS `start_date`,`f`.`end_date` AS `end_date`,`f`.`is_active` AS `is_active`,`f`.`sort_order` AS `sort_order`,`f`.`create_time` AS `create_time`,`f`.`update_time` AS `update_time`,(case when ((`f`.`festival_type` = 'SEASONAL') and (curdate() between `f`.`start_date` and `f`.`end_date`)) then 1 when ((`f`.`festival_type` <> 'SEASONAL') and ((to_days(concat(year(curdate()),'-',`f`.`festival_date`)) - to_days(curdate())) between 0 and 7)) then 1 else 0 end) AS `is_current`,(case when (`f`.`festival_type` <> 'SEASONAL') then (to_days(concat(year(curdate()),'-',`f`.`festival_date`)) - to_days(curdate())) else NULL end) AS `days_until_festival` from `t_festival` `f` where (`f`.`is_active` = 1) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_content_extraction_overview`
--

/*!50001 DROP VIEW IF EXISTS `v_content_extraction_overview`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_content_extraction_overview` AS select `cs`.`id` AS `id`,`cs`.`user_id` AS `user_id`,`cs`.`content_url` AS `content_url`,`cs`.`content_type` AS `content_type`,`cs`.`platform` AS `platform`,`cs`.`title` AS `title`,`cs`.`author` AS `author`,`cs`.`cover_image` AS `cover_image`,`cs`.`extraction_status` AS `extraction_status`,`ce`.`dish_name` AS `dish_name`,`ce`.`dish_image` AS `dish_image`,`ce`.`description` AS `description`,`ce`.`ingredients` AS `ingredients`,`ce`.`steps` AS `steps`,`ce`.`cooking_time` AS `cooking_time`,`ce`.`difficulty` AS `difficulty`,`ce`.`tags` AS `tags`,`ce`.`calories` AS `calories`,`ce`.`is_published` AS `is_published`,`ce`.`recipe_id` AS `recipe_id`,`ce`.`is_verified` AS `is_verified`,`cs`.`create_time` AS `create_time`,`cs`.`extraction_time` AS `extraction_time` from (`t_content_source` `cs` left join `t_content_extraction` `ce` on((`cs`.`id` = `ce`.`source_id`))) order by `cs`.`create_time` desc */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_dish_popularity`
--

/*!50001 DROP VIEW IF EXISTS `v_dish_popularity`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_dish_popularity` AS select `d`.`id` AS `dish_id`,`d`.`name` AS `dish_name`,`d`.`view_count` AS `view_count`,`d`.`order_count` AS `order_count`,`d`.`favorite_count` AS `favorite_count`,`d`.`avg_rating` AS `avg_rating`,coalesce(`df`.`popularity_score`,0) AS `feature_popularity`,(((coalesce(`d`.`view_count`,0) * 0.3) + (coalesce(`d`.`order_count`,0) * 0.5)) + (coalesce(`d`.`favorite_count`,0) * 0.2)) AS `computed_popularity` from (`t_dish` `d` left join `dish_features` `df` on((cast(`d`.`id` as char charset latin1) = `df`.`dish_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_order_dish_step_overview`
--

/*!50001 DROP VIEW IF EXISTS `v_order_dish_step_overview`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_order_dish_step_overview` AS select `od`.`id` AS `order_dish_id`,`od`.`order_id` AS `order_id`,cast(`od`.`dish_id` as char charset binary) AS `dish_id`,`d`.`name` AS `dish_name`,`od`.`quantity` AS `quantity`,`od`.`step_status` AS `step_status`,`od`.`step_start_time` AS `step_start_time`,`od`.`estimated_completion_time` AS `estimated_completion_time`,`od`.`cooking_minutes` AS `cooking_minutes`,`od`.`step_sort` AS `step_sort`,`od`.`is_fast_food` AS `is_fast_food`,`od`.`serving_status` AS `serving_status`,`o`.`status` AS `order_status`,(select count(0) from `t_dish_step_history` `dsh` where (cast(`dsh`.`order_dish_id` as char charset binary) = cast(`od`.`id` as char charset binary))) AS `step_change_count`,(select max(`dsh`.`create_time`) from `t_dish_step_history` `dsh` where (cast(`dsh`.`order_dish_id` as char charset binary) = cast(`od`.`id` as char charset binary))) AS `last_step_change_time` from ((`t_order_dish` `od` left join `t_dish` `d` on((cast(`od`.`dish_id` as char charset binary) = cast(`d`.`id` as char charset binary)))) left join `t_order` `o` on((cast(`od`.`order_id` as char charset binary) = cast(`o`.`id` as char charset binary)))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_behavior_stats`
--

/*!50001 DROP VIEW IF EXISTS `v_user_behavior_stats`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = latin1 */;
/*!50001 SET character_set_results     = latin1 */;
/*!50001 SET collation_connection      = latin1_swedish_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_behavior_stats` AS select `user_behavior`.`user_id` AS `user_id`,`user_behavior`.`behavior_type` AS `behavior_type`,count(0) AS `behavior_count`,count(distinct `user_behavior`.`item_id`) AS `unique_items`,min(`user_behavior`.`created_time`) AS `first_behavior`,max(`user_behavior`.`created_time`) AS `last_behavior` from `user_behavior` group by `user_behavior`.`user_id`,`user_behavior`.`behavior_type` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-25  8:59:53
