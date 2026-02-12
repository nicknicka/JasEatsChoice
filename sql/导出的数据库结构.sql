-- MySQL dump 10.13  Distrib 9.4.0, for macos15.4 (arm64)
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_address`
--

DROP TABLE IF EXISTS `t_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'åœ°å€ID',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `receiver_name` varchar(50) NOT NULL COMMENT 'æ”¶è´§äººå§“å',
  `receiver_phone` varchar(20) NOT NULL COMMENT 'æ”¶è´§äººç”µè¯',
  `province` varchar(50) NOT NULL COMMENT 'çœä»½',
  `city` varchar(50) NOT NULL COMMENT 'åŸŽå¸‚',
  `district` varchar(50) NOT NULL COMMENT 'åŒºåŽ¿',
  `detail` varchar(255) NOT NULL COMMENT 'è¯¦ç»†åœ°å€',
  `is_default` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦é»˜è®¤åœ°å€ï¼ˆ0-å¦/1-æ˜¯ï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç”¨æˆ·åœ°å€è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_calorie_record`
--

DROP TABLE IF EXISTS `t_calorie_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_calorie_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dish_id` bigint DEFAULT NULL COMMENT '菜品ID',
  `calorie` int NOT NULL COMMENT '卡路里',
  `meal_time` varchar(20) NOT NULL COMMENT '餐次（breakfast/lunch/dinner/snack）',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `food_name` varchar(255) DEFAULT NULL COMMENT '食物名称',
  `description` varchar(500) DEFAULT NULL COMMENT '食物描述',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`record_time`),
  KEY `idx_meal_time` (`meal_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='卡路里摄入记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_chat_msg`
--

DROP TABLE IF EXISTS `t_chat_msg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_chat_msg` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `from_id` varchar(50) NOT NULL COMMENT '发送方ID',
  `to_id` varchar(50) NOT NULL COMMENT '接收方ID',
  `msg_type` varchar(20) NOT NULL COMMENT '消息类型（single/group/order_sync）',
  `content` text NOT NULL COMMENT '消息内容',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态（0-未读/1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_to` (`from_id`,`to_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_collection`
--

DROP TABLE IF EXISTS `t_collection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `collectable_type` varchar(20) NOT NULL COMMENT 'æ”¶è—ç±»åž‹ï¼šmerchant, dish, article, recipeç­‰',
  `collectable_id` bigint NOT NULL COMMENT 'æ”¶è—å¯¹è±¡ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type_id` (`collectable_type`,`collectable_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ”¶è—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_consume_history`
--

DROP TABLE IF EXISTS `t_consume_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_consume_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'è®°å½•ID',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `type` varchar(20) NOT NULL COMMENT 'ç±»åž‹ï¼šrechargeï¼ˆå……å€¼ï¼‰ã€consumeï¼ˆæ¶ˆè´¹ï¼‰ã€withdrawï¼ˆæçŽ°ï¼‰',
  `amount` decimal(10,2) NOT NULL COMMENT 'é‡‘é¢',
  `description` varchar(255) DEFAULT NULL COMMENT 'æè¿°',
  `status` varchar(20) NOT NULL COMMENT 'çŠ¶æ€ï¼šsuccessï¼ˆæˆåŠŸï¼‰ã€failedï¼ˆå¤±è´¥ï¼‰',
  `create_time` datetime NOT NULL COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ¶ˆè´¹è®°å½•';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_contact`
--

DROP TABLE IF EXISTS `t_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID（好友ID或群ID）',
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='联系人关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_dish`
--

DROP TABLE IF EXISTS `t_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜品ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜品名称',
  `category` varchar(50) NOT NULL COMMENT '菜品分类',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `calorie` int NOT NULL COMMENT '卡路里',
  `ingredients` json DEFAULT NULL COMMENT '食材列表（JSON格式）',
  `description` text COMMENT '菜品描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '菜品状态（0-下架/1-上架）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_category` (`category`),
  KEY `idx_calorie` (`calorie`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group`
--

DROP TABLE IF EXISTS `t_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group` (
  `id` bigint NOT NULL,
  `group_name` varchar(100) NOT NULL COMMENT '群名称',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `description` varchar(500) DEFAULT NULL COMMENT '群描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='群信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_order`
--

DROP TABLE IF EXISTS `t_group_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ç¾¤è®¢å•ID',
  `initiator_id` bigint NOT NULL COMMENT 'ç¾¤è®¢å•å‘èµ·è€…ID',
  `merchant_id` bigint NOT NULL COMMENT 'å•†å®¶ID',
  `group_id` bigint NOT NULL COMMENT 'ç¾¤ID',
  `address_id` bigint NOT NULL COMMENT 'åœ°å€ID',
  `remark` varchar(255) DEFAULT NULL COMMENT 'è®¢å•å¤‡æ³¨',
  `status` tinyint DEFAULT '0' COMMENT 'è®¢å•çŠ¶æ€ï¼š0-å¾…æ”¯ä»˜ã€1-å¾…æŽ¥å•ã€2-å¤‡èœä¸­ã€3-çƒ¹é¥ªä¸­ã€4-å¾…ä¸Šèœã€5-å·²å®Œæˆã€6-å·²å–æ¶ˆ',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¾¤è®¢å•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_group_order_dish`
--

DROP TABLE IF EXISTS `t_group_order_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group_order_dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `group_order_id` bigint NOT NULL COMMENT 'ç¾¤è®¢å•ID',
  `dish_id` bigint NOT NULL COMMENT 'èœå“ID',
  `quantity` int NOT NULL COMMENT 'æ•°é‡',
  `customization` varchar(255) DEFAULT NULL COMMENT 'å®šåˆ¶è¦æ±‚',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¾¤è®¢å•èœå“è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_menu`
--

DROP TABLE IF EXISTS `t_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `name` varchar(100) NOT NULL COMMENT '菜单名称',
  `type` varchar(20) NOT NULL COMMENT '菜单类型（workday/weekend/afternoon_tea/package等）',
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
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `sort` int DEFAULT '0' COMMENT '排序',
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
  `id` bigint NOT NULL,
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
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_location` (`longitude`,`latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_notification`
--

DROP TABLE IF EXISTS `t_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态（0-未读/1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_order`
--

DROP TABLE IF EXISTS `t_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态（0-待支付/1-待接单/2-备菜中/3-烹饪中/4-待上菜/5-已完成/6-已取消）',
  `address` varchar(255) NOT NULL COMMENT '配送地址',
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `address_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_order_dish`
--

DROP TABLE IF EXISTS `t_order_dish`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_order_dish` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `dish_id` bigint NOT NULL COMMENT '菜品ID',
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `customization` varchar(255) DEFAULT NULL COMMENT '定制要求',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单菜品表';
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
  `content` text NOT NULL COMMENT '食谱内容',
  `calories` int NOT NULL COMMENT '卡路里',
  `protein` int NOT NULL COMMENT '蛋白质',
  `carbs` int NOT NULL COMMENT '碳水化合物',
  `fat` int NOT NULL COMMENT '脂肪',
  `cook_time` varchar(50) DEFAULT NULL COMMENT '烹饪时间',
  `is_favorite` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否收藏: 0-否, 1-是',
  `is_today` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为今日食谱: 0-否, 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食谱表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_review`
--

DROP TABLE IF EXISTS `t_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `dish_id` bigint DEFAULT NULL COMMENT '菜品ID',
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
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `content` varchar(500) DEFAULT NULL COMMENT '回复内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评价回复表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_user`
--

DROP TABLE IF EXISTS `t_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user` (
  `user_id` bigint NOT NULL,
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `password` varchar(64) NOT NULL COMMENT '密码（BCrypt加密）',
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
  `merchant_id` bigint DEFAULT NULL COMMENT 'å•†å®¶ID',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户推荐偏好表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tmp_id_mappings`
--

DROP TABLE IF EXISTS `tmp_id_mappings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tmp_id_mappings` (
  `old_id` bigint NOT NULL,
  `new_id` bigint NOT NULL,
  `table_name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`old_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tutorial`
--

DROP TABLE IF EXISTS `tutorial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tutorial` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `title` varchar(255) NOT NULL COMMENT '教程标题',
  `type` varchar(20) NOT NULL COMMENT '教程类型：video/article',
  `duration` varchar(20) DEFAULT NULL COMMENT '时长',
  `views` varchar(20) DEFAULT NULL COMMENT '观看/阅读量',
  `featured` tinyint(1) DEFAULT '0' COMMENT '是否在首页推荐',
  `content` text COMMENT '教程内容',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `video_url` varchar(255) DEFAULT NULL COMMENT '视频地址',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='制作教程与指南表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-20 20:43:08
