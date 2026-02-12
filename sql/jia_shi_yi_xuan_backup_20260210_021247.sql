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
-- Table structure for table `announcement`
--

DROP TABLE IF EXISTS `announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å…¬å‘ŠID',
  `merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å•†å®¶IDï¼ŒNULLè¡¨ç¤ºç³»ç»Ÿå…¬å‘Š',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å…¬å‘Šæ ‡é¢˜',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å…¬å‘Šå†…å®¹',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'system' COMMENT 'å…¬å‘Šç±»åž‹ï¼šsystem-ç³»ç»Ÿå…¬å‘Š, activity-æ´»åŠ¨å…¬å‘Š, urgent-ç´§æ€¥å…¬å‘Š, update-æ›´æ–°è¯´æ˜Ž',
  `priority` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'normal' COMMENT 'ä¼˜å…ˆçº§ï¼šnormal-æ™®é€š, important-é‡è¦, urgent-ç´§æ€¥',
  `target_audience` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'all' COMMENT 'ç›®æ ‡ç¾¤ä½“ï¼šall-å…¨éƒ¨ç”¨æˆ·, merchant-å•†å®¶ç«¯, customer-ç”¨æˆ·ç«¯',
  `read_count` bigint DEFAULT '0' COMMENT 'é˜…è¯»é‡',
  `read_users` bigint DEFAULT '0' COMMENT 'é˜…è¯»äººæ•°',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'active' COMMENT 'å…¬å‘ŠçŠ¶æ€ (active: å¯ç”¨, inactive: ç¦ç”¨)',
  `start_time` datetime DEFAULT NULL COMMENT 'å¼€å§‹æ—¶é—´',
  `end_time` datetime DEFAULT NULL COMMENT 'ç»“æŸæ—¶é—´',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å•†å®¶å…¬å‘Šè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement`
--

LOCK TABLES `announcement` WRITE;
/*!40000 ALTER TABLE `announcement` DISABLE KEYS */;
INSERT INTO `announcement` VALUES (1,NULL,'测试公告','这是一个测试公告','system','normal','all',0,0,'active','2026-01-11 06:06:52',NULL,'2026-01-11 10:52:54','2026-02-09 23:24:03'),(2,NULL,'31299','test asd 123 ','system','normal','all',0,0,'inactive','2026-01-04 18:04:00','2026-01-11 06:00:19','2026-01-11 14:00:52','2026-02-09 23:24:03'),(4,NULL,'312','test asd 123 312ws123','system','normal','all',0,0,'active','2026-02-20 14:06:03','2026-02-13 00:00:00','2026-01-11 14:01:07','2026-02-09 23:24:03');
/*!40000 ALTER TABLE `announcement` ENABLE KEYS */;
UNLOCK TABLES;

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
  `discount_value` double DEFAULT NULL COMMENT 'ä¼˜æƒ åŠ›åº¦',
  `min_amount` double DEFAULT NULL COMMENT 'æœ€ä½Žæ¶ˆè´¹',
  `limit_per_user` int DEFAULT NULL COMMENT 'æ¯äººé™é¢†',
  `usage_notes` text COLLATE utf8mb4_unicode_ci COMMENT 'ä½¿ç”¨è¯´æ˜Ž',
  `validity_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æœ‰æ•ˆæœŸç±»åž‹',
  `validity_period` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æœ‰æ•ˆæœŸèŒƒå›´',
  `valid_days` int DEFAULT NULL COMMENT 'æœ‰æ•ˆå¤©æ•°',
  `used_count` int DEFAULT '0' COMMENT 'ä½¿ç”¨æ¬¡æ•°',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2009927413764476930 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `discount`
--

LOCK TABLES `discount` WRITE;
/*!40000 ALTER TABLE `discount` DISABLE KEYS */;
INSERT INTO `discount` VALUES (2009927413764476929,7638432224340229,'3123','满减','21231','active','2026-01-10 17:56:53','2026-01-10 17:56:53',20,220,1,'222','permanent',NULL,30,0);
/*!40000 ALTER TABLE `discount` ENABLE KEYS */;
UNLOCK TABLES;

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
  `price_level` tinyint DEFAULT NULL COMMENT 'ä»·æ ¼ç­‰çº§ 1-5',
  `popularity_score` decimal(5,2) DEFAULT NULL COMMENT 'çƒ­åº¦åˆ†æ•°',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `dish_id` (`dish_id`),
  KEY `idx_category` (`category`),
  KEY `idx_popularity` (`popularity_score` DESC),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èœå“ç‰¹å¾è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish_features`
--

LOCK TABLES `dish_features` WRITE;
/*!40000 ALTER TABLE `dish_features` DISABLE KEYS */;
INSERT INTO `dish_features` VALUES (1,'1','category_1','[]','{\"salty\": 0.5, \"spicy\": 0.5}',NULL,NULL,'ç‚’',NULL,NULL,NULL,3,0.00,'2026-01-24 15:57:58','2026-01-24 15:59:55'),(2,'2008574121167847426','主食','[]','{\"salty\": 0.5, \"spicy\": 0.5}',NULL,NULL,'ç‚’',NULL,NULL,NULL,5,0.00,'2026-01-24 15:57:58','2026-01-24 15:59:55'),(3,'2008733197583519745','主食','[]','{\"salty\": 0.5, \"spicy\": 0.5}',NULL,NULL,'ç‚’',NULL,NULL,NULL,1,0.00,'2026-01-24 15:57:58','2026-01-24 15:59:55'),(4,'2008733624953769986','主食','[]','{\"salty\": 0.5, \"spicy\": 0.5}',NULL,NULL,'ç‚’',NULL,NULL,NULL,1,0.00,'2026-01-24 15:57:58','2026-01-24 15:59:55'),(5,'2008737368059711489','主食','[]','{\"salty\": 0.5, \"spicy\": 0.5}',NULL,NULL,'ç‚’',NULL,NULL,NULL,1,0.00,'2026-01-24 15:57:58','2026-01-24 15:59:55'),(8,'1001','æ—©é¤','[\"ç²¥å“\", \"æš–èƒƒ\", \"è¥å…»\", \"æ—©é¤\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,75.50,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(9,'1002','æ—©é¤','[\"ç»å…¸\", \"æ­é…\", \"ä¼ ç»Ÿ\", \"æ—©é¤\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,82.30,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(10,'1003','æ—©é¤','[\"çš®è–„\", \"æ±å¤š\", \"ç²¾è‡´\", \"æ—©é¤\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,88.60,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(11,'1004','æ—©é¤','[\"å¤©æ´¥\", \"é£Žå‘³\", \"é¥±è…¹\", \"æ—©é¤\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,79.20,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(12,'1005','æ—©é¤','[\"çš®è–„\", \"æ±¤é²œ\", \"æ¸…æ·¡\", \"æ—©é¤\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,76.80,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(13,'1006','å·èœ','[\"éº»è¾£\", \"é…¸ç”œ\", \"é¸¡è‚‰\", \"ä¸‹é¥­\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,91.50,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(14,'1007','å·èœ','[\"é…¸ç”œ\", \"å¼€èƒƒ\", \"ä¸‹é¥­\", \"ç»å…¸\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,89.30,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(15,'1008','å·èœ','[\"éº»è¾£\", \"ç´ é£Ÿ\", \"ä¸‹é¥­\", \"ç»å…¸\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,92.10,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(16,'1009','å·èœ','[\"éº»è¾£\", \"é²œå«©\", \"é‡å£å‘³\", \"èšé¤\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,4,87.60,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(17,'1010','å·èœ','[\"è‚¥è€Œä¸è…»\", \"ä¸‹é¥­\", \"ç»å…¸\", \"å·èœ\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,90.40,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(18,'1011','å®¶å¸¸èœ','[\"è‚¥è€Œä¸è…»\", \"å…¥å£å³åŒ–\", \"ç»å…¸\", \"ä¸‹é¥­\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,4,94.20,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(19,'1012','å®¶å¸¸èœ','[\"é…¸ç”œ\", \"å¤–é…¥é‡Œå«©\", \"ä¸‹é¥­\", \"ç»å…¸\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,4,88.90,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(20,'1013','ç²¤èœ','[\"æ¸…æ·¡\", \"é²œå«©\", \"åŽŸæ±åŽŸå‘³\", \"å¥åº·\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,4,93.50,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(21,'1014','ç²¤èœ','[\"çš®çˆ½è‚‰æ»‘\", \"æ¸…æ·¡\", \"åŽŸå‘³\", \"ç»å…¸\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,85.70,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(22,'1015','ç´ èœ','[\"æ¸…çˆ½\", \"å¥åº·\", \"ä½Žè„‚\", \"ç´ é£Ÿ\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,80.10,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(23,'1016','ç«é”…','[\"æš–èº«\", \"æ»‹è¡¥\", \"èšé¤\", \"å†¬å­£\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,5,86.80,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(24,'1017','ç«é”…','[\"é²œç¾Ž\", \"æ»‹è¡¥\", \"èšé¤\", \"ç‰›è‚‰\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,5,84.50,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(25,'1018','ç«é”…','[\"é…¸ç”œ\", \"å¼€èƒƒ\", \"è€å°‘çš†å®œ\", \"æ¸…æ·¡\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,4,81.20,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(26,'1019','è½»é£Ÿ','[\"ä½Žè„‚\", \"é«˜è›‹ç™½\", \"å¥åº·\", \"å‡è„‚\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,78.90,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(27,'1020','è½»é£Ÿ','[\"è¥å…»å‡è¡¡\", \"é¥±è…¹\", \"å¥åº·\", \"è½»é£Ÿ\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,76.40,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(28,'1021','è½»é£Ÿ','[\"æ¸…çˆ½\", \"ä½Žå¡\", \"å¥åº·\", \"ç´ é£Ÿ\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,82.60,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(29,'1022','è½»é£Ÿ','[\"ç²—ç²®\", \"å¥åº·\", \"ä¾¿æº\", \"æ—©é¤\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,75.30,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(30,'1023','è½»é£Ÿ','[\"è¶…çº§é£Ÿç‰©\", \"è¥å…»\", \"å¥åº·\", \"è½»é£Ÿ\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,73.80,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(31,'1024','æ±¤ç±»','[\"æ¸…æ·¡\", \"é²œé¦™\", \"å¼€èƒƒ\", \"è¥å…»\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,77.50,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(32,'1025','æ±¤ç±»','[\"é…¸è¾£\", \"å¼€èƒƒ\", \"æš–èƒƒ\", \"ä¸‹é¥­\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,74.80,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(33,'1026','æ±¤ç±»','[\"æ¸…æ·¡\", \"æ»‹è¡¥\", \"æ¸…çƒ­\", \"å¥åº·\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,79.10,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(34,'1027','æ±¤ç±»','[\"ç®€å•\", \"å¿«æ·\", \"è¥å…»\", \"å®¶å¸¸\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,71.30,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(35,'1028','æ±¤ç±»','[\"é²œç¾Ž\", \"é†‡åŽš\", \"æ»‹è¡¥\", \"å¥åº·\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,80.70,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(36,'1034','ç”œç‚¹','[\"ç»å…¸\", \"æµ“éƒ\", \"ç²¾è‡´\", \"ä¸‹åˆèŒ¶\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,3,86.40,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(37,'1035','ç”œç‚¹','[\"é¡ºæ»‘\", \"é¦™ç”œ\", \"æ¸…çˆ½\", \"ç”œå“\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,2,79.50,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(38,'1036','ç”œç‚¹','[\"ç¥›æ¹¿\", \"å…»é¢œ\", \"å¥åº·\", \"ç”œå“\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,74.20,'2026-01-25 09:12:13','2026-01-25 09:12:13'),(39,'1043','é¥®å“','[\"æ¸…çƒ­\", \"é™ç«\", \"å¥åº·\", \"é¥®å“\"]',NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,72.60,'2026-01-25 09:12:13','2026-01-25 09:12:13');
/*!40000 ALTER TABLE `dish_features` ENABLE KEYS */;
UNLOCK TABLES;

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
  `similarity_score` decimal(5,4) NOT NULL COMMENT 'ç›¸ä¼¼åº¦åˆ†æ•° 0-1',
  `similarity_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç›¸ä¼¼åº¦ç±»åž‹: content(åŸºäºŽå†…å®¹)/collaborative(ååŒè¿‡æ»¤)/hybrid(æ··åˆ)',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dish_pair_type` (`dish_id_a`,`dish_id_b`,`similarity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èœå“ç›¸ä¼¼åº¦è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dish_similarity`
--

LOCK TABLES `dish_similarity` WRITE;
/*!40000 ALTER TABLE `dish_similarity` DISABLE KEYS */;
/*!40000 ALTER TABLE `dish_similarity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hot_topic`
--

DROP TABLE IF EXISTS `hot_topic`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hot_topic` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'çƒ­ç‚¹å†…å®¹',
  `priority` int DEFAULT '0' COMMENT 'ä¼˜å…ˆçº§',
  `source_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'MANUAL' COMMENT 'æ¥æºç±»åž‹',
  `source_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ¥æºID',
  `start_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'ç”Ÿæ•ˆå¼€å§‹æ—¶é—´',
  `end_date` datetime DEFAULT NULL COMMENT 'ç”Ÿæ•ˆç»“æŸæ—¶é—´',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€',
  `click_count` int DEFAULT '0' COMMENT 'ç‚¹å‡»æ¬¡æ•°',
  `share_count` int DEFAULT '0' COMMENT 'åˆ†äº«æ¬¡æ•°',
  `require_review` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦éœ€è¦å®¡æ ¸',
  `review_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'APPROVED' COMMENT 'å®¡æ ¸çŠ¶æ€',
  `reviewer_id` bigint DEFAULT NULL COMMENT 'å®¡æ ¸äººID',
  `review_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `review_comment` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸æ„è§',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¤‡æ³¨ä¿¡æ¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `create_by` bigint DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `update_by` bigint DEFAULT NULL COMMENT 'æ›´æ–°äººID',
  PRIMARY KEY (`id`),
  KEY `idx_start_date` (`start_date`),
  KEY `idx_end_date` (`end_date`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ä»Šæ—¥çƒ­ç‚¹è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hot_topic`
--

LOCK TABLES `hot_topic` WRITE;
/*!40000 ALTER TABLE `hot_topic` DISABLE KEYS */;
INSERT INTO `hot_topic` VALUES ('1','test',10,'MANUAL',NULL,'2026-02-06 00:00:00','2026-03-03 00:00:00','ACTIVE',2,0,0,'APPROVED',NULL,NULL,NULL,'test','2026-01-29 10:33:50','2026-02-09 22:54:52',NULL,NULL),('2','asd',5,'MANUAL',NULL,'2026-01-29 10:33:50',NULL,'INACTIVE',0,0,0,'APPROVED',NULL,NULL,NULL,'æ—©é¤æŽ¨è','2026-01-29 10:33:50','2026-02-09 22:59:33',NULL,NULL),('3','888999',3,'MANUAL',NULL,'2026-01-29 10:33:50',NULL,'ACTIVE',0,0,0,'APPROVED',NULL,NULL,NULL,'å‡è„‚æœŸé¥®é£ŸæŽ¨è','2026-01-29 10:33:50','2026-01-31 11:19:26',NULL,NULL);
/*!40000 ALTER TABLE `hot_topic` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recommendation_log`
--

DROP TABLE IF EXISTS `recommendation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recommendation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
  `recommendation_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æŽ¨èæ‰¹æ¬¡ID',
  `dish_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `rank` int DEFAULT NULL COMMENT 'æŽ¨èæŽ’åºä½ç½®',
  `score` decimal(10,4) DEFAULT NULL COMMENT 'æŽ¨èå¾—åˆ†',
  `algorithm` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä½¿ç”¨çš„ç®—æ³•',
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
) ENGINE=InnoDB AUTO_INCREMENT=307 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æŽ¨èè®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recommendation_log`
--

LOCK TABLES `recommendation_log` WRITE;
/*!40000 ALTER TABLE `recommendation_log` DISABLE KEYS */;
INSERT INTO `recommendation_log` VALUES (1,'3384650106421960','3002933f-e9fb-41cb-9ca7-2e292fa74b88','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:36'),(2,'3384650106421960','3002933f-e9fb-41cb-9ca7-2e292fa74b88','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:36'),(3,'3384650106421960','3002933f-e9fb-41cb-9ca7-2e292fa74b88','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:36'),(4,'3384650106421960','3002933f-e9fb-41cb-9ca7-2e292fa74b88','1001',4,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:36'),(5,'3384650106421960','3002933f-e9fb-41cb-9ca7-2e292fa74b88','1011',5,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:36'),(6,'3384650106421960','3002933f-e9fb-41cb-9ca7-2e292fa74b88','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-29 18:05:36'),(7,'3384650106421960','36c52fed-fa47-4034-be73-891e3ca98247','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:38'),(8,'3384650106421960','36c52fed-fa47-4034-be73-891e3ca98247','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:38'),(9,'3384650106421960','36c52fed-fa47-4034-be73-891e3ca98247','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:38'),(10,'3384650106421960','36c52fed-fa47-4034-be73-891e3ca98247','1',4,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-29 18:05:38'),(11,'3384650106421960','36c52fed-fa47-4034-be73-891e3ca98247','1001',5,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:38'),(12,'3384650106421960','36c52fed-fa47-4034-be73-891e3ca98247','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:05:38'),(13,'3384650106421960','23649a3a-5b1b-486a-877e-60b9b462a5cb','1011',1,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:33'),(14,'3384650106421960','23649a3a-5b1b-486a-877e-60b9b462a5cb','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:33'),(15,'3384650106421960','23649a3a-5b1b-486a-877e-60b9b462a5cb','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:33'),(16,'3384650106421960','23649a3a-5b1b-486a-877e-60b9b462a5cb','1',4,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-29 18:21:33'),(17,'3384650106421960','23649a3a-5b1b-486a-877e-60b9b462a5cb','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:33'),(18,'3384650106421960','23649a3a-5b1b-486a-877e-60b9b462a5cb','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:33'),(19,'3384650106421960','4224f713-1817-48eb-8c12-ebb526015282','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:35'),(20,'3384650106421960','4224f713-1817-48eb-8c12-ebb526015282','1013',2,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:35'),(21,'3384650106421960','4224f713-1817-48eb-8c12-ebb526015282','1011',3,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:35'),(22,'3384650106421960','4224f713-1817-48eb-8c12-ebb526015282','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:35'),(23,'3384650106421960','4224f713-1817-48eb-8c12-ebb526015282','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:21:35'),(24,'3384650106421960','4224f713-1817-48eb-8c12-ebb526015282','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-29 18:21:35'),(25,'3384650106421960','01923d76-c76a-4af6-8c78-b20a79508d88','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:27'),(26,'3384650106421960','01923d76-c76a-4af6-8c78-b20a79508d88','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:27'),(27,'3384650106421960','01923d76-c76a-4af6-8c78-b20a79508d88','1',3,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-29 18:24:27'),(28,'3384650106421960','01923d76-c76a-4af6-8c78-b20a79508d88','1001',4,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:27'),(29,'3384650106421960','01923d76-c76a-4af6-8c78-b20a79508d88','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:27'),(30,'3384650106421960','01923d76-c76a-4af6-8c78-b20a79508d88','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:27'),(31,'3384650106421960','d2827ec1-c35d-422c-a44b-14e371633e92','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-29 18:24:29'),(32,'3384650106421960','d2827ec1-c35d-422c-a44b-14e371633e92','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:29'),(33,'3384650106421960','d2827ec1-c35d-422c-a44b-14e371633e92','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:29'),(34,'3384650106421960','d2827ec1-c35d-422c-a44b-14e371633e92','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:29'),(35,'3384650106421960','d2827ec1-c35d-422c-a44b-14e371633e92','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:29'),(36,'3384650106421960','d2827ec1-c35d-422c-a44b-14e371633e92','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:29'),(37,'3384650106421960','0a112b67-9299-4ea0-a4d7-5df9b963308e','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:41'),(38,'3384650106421960','0a112b67-9299-4ea0-a4d7-5df9b963308e','1',2,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-29 18:24:41'),(39,'3384650106421960','0a112b67-9299-4ea0-a4d7-5df9b963308e','1001',3,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:41'),(40,'3384650106421960','0a112b67-9299-4ea0-a4d7-5df9b963308e','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:41'),(41,'3384650106421960','0a112b67-9299-4ea0-a4d7-5df9b963308e','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:41'),(42,'3384650106421960','0a112b67-9299-4ea0-a4d7-5df9b963308e','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-29 18:24:41'),(43,'3384650106421960','f0638359-b91e-4ff5-a20b-ee5e0c531f52','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:38'),(44,'3384650106421960','f0638359-b91e-4ff5-a20b-ee5e0c531f52','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:38'),(45,'3384650106421960','f0638359-b91e-4ff5-a20b-ee5e0c531f52','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:38'),(46,'3384650106421960','f0638359-b91e-4ff5-a20b-ee5e0c531f52','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:38'),(47,'3384650106421960','f0638359-b91e-4ff5-a20b-ee5e0c531f52','1',5,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 10:52:38'),(48,'3384650106421960','f0638359-b91e-4ff5-a20b-ee5e0c531f52','1013',6,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:38'),(49,'3384650106421960','c5e1a84a-e5d2-4895-a557-33697fb4009a','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 10:52:59'),(50,'3384650106421960','c5e1a84a-e5d2-4895-a557-33697fb4009a','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:59'),(51,'3384650106421960','c5e1a84a-e5d2-4895-a557-33697fb4009a','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:59'),(52,'3384650106421960','c5e1a84a-e5d2-4895-a557-33697fb4009a','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:59'),(53,'3384650106421960','c5e1a84a-e5d2-4895-a557-33697fb4009a','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:59'),(54,'3384650106421960','c5e1a84a-e5d2-4895-a557-33697fb4009a','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:52:59'),(55,'3384650106421960','94fc1f8c-34f0-4a0c-bead-c561860930ad','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 10:53:01'),(56,'3384650106421960','94fc1f8c-34f0-4a0c-bead-c561860930ad','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:53:01'),(57,'3384650106421960','94fc1f8c-34f0-4a0c-bead-c561860930ad','1011',3,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:53:01'),(58,'3384650106421960','94fc1f8c-34f0-4a0c-bead-c561860930ad','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:53:01'),(59,'3384650106421960','94fc1f8c-34f0-4a0c-bead-c561860930ad','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:53:01'),(60,'3384650106421960','94fc1f8c-34f0-4a0c-bead-c561860930ad','1013',6,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:53:01'),(61,'3384650106421960','5596631d-8d54-470f-94e6-1f68615344e9','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:12'),(62,'3384650106421960','5596631d-8d54-470f-94e6-1f68615344e9','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:12'),(63,'3384650106421960','5596631d-8d54-470f-94e6-1f68615344e9','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:12'),(64,'3384650106421960','5596631d-8d54-470f-94e6-1f68615344e9','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:12'),(65,'3384650106421960','5596631d-8d54-470f-94e6-1f68615344e9','1',5,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 10:54:12'),(66,'3384650106421960','5596631d-8d54-470f-94e6-1f68615344e9','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:12'),(67,'3384650106421960','615aadab-520a-4e5e-987b-9c0b23ab3a44','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:14'),(68,'3384650106421960','615aadab-520a-4e5e-987b-9c0b23ab3a44','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:14'),(69,'3384650106421960','615aadab-520a-4e5e-987b-9c0b23ab3a44','1001',3,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:14'),(70,'3384650106421960','615aadab-520a-4e5e-987b-9c0b23ab3a44','1',4,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 10:54:14'),(71,'3384650106421960','615aadab-520a-4e5e-987b-9c0b23ab3a44','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:14'),(72,'3384650106421960','615aadab-520a-4e5e-987b-9c0b23ab3a44','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 10:54:14'),(73,'3384650106421960','7c3727e5-6669-4e49-b0fb-6d88318f2140','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:13:47'),(74,'3384650106421960','7c3727e5-6669-4e49-b0fb-6d88318f2140','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:13:47'),(75,'3384650106421960','7c3727e5-6669-4e49-b0fb-6d88318f2140','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:13:47'),(76,'3384650106421960','7c3727e5-6669-4e49-b0fb-6d88318f2140','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:13:47'),(77,'3384650106421960','7c3727e5-6669-4e49-b0fb-6d88318f2140','1001',5,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:13:47'),(78,'3384650106421960','7c3727e5-6669-4e49-b0fb-6d88318f2140','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 11:13:47'),(79,'3384650106421960','459e0a02-7f52-4025-aed8-162d1d8c7574','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:15:42'),(80,'3384650106421960','459e0a02-7f52-4025-aed8-162d1d8c7574','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:15:42'),(81,'3384650106421960','459e0a02-7f52-4025-aed8-162d1d8c7574','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:15:42'),(82,'3384650106421960','459e0a02-7f52-4025-aed8-162d1d8c7574','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:15:42'),(83,'3384650106421960','459e0a02-7f52-4025-aed8-162d1d8c7574','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:15:42'),(84,'3384650106421960','459e0a02-7f52-4025-aed8-162d1d8c7574','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 11:15:42'),(85,'3384650106421960','a8b684ad-27b9-49dc-b4d6-c2ad34746340','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 11:16:24'),(86,'3384650106421960','a8b684ad-27b9-49dc-b4d6-c2ad34746340','1013',2,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:24'),(87,'3384650106421960','a8b684ad-27b9-49dc-b4d6-c2ad34746340','1001',3,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:24'),(88,'3384650106421960','a8b684ad-27b9-49dc-b4d6-c2ad34746340','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:24'),(89,'3384650106421960','a8b684ad-27b9-49dc-b4d6-c2ad34746340','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:24'),(90,'3384650106421960','a8b684ad-27b9-49dc-b4d6-c2ad34746340','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:24'),(91,'3384650106421960','aac3ac03-6369-4f2f-90ec-17695e7e4e17','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:29'),(92,'3384650106421960','aac3ac03-6369-4f2f-90ec-17695e7e4e17','1',2,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 11:16:29'),(93,'3384650106421960','aac3ac03-6369-4f2f-90ec-17695e7e4e17','1001',3,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:29'),(94,'3384650106421960','aac3ac03-6369-4f2f-90ec-17695e7e4e17','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:29'),(95,'3384650106421960','aac3ac03-6369-4f2f-90ec-17695e7e4e17','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:29'),(96,'3384650106421960','aac3ac03-6369-4f2f-90ec-17695e7e4e17','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:29'),(97,'3384650106421960','1f582077-cd2c-4478-a546-8ca548dc230a','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:31'),(98,'3384650106421960','1f582077-cd2c-4478-a546-8ca548dc230a','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:31'),(99,'3384650106421960','1f582077-cd2c-4478-a546-8ca548dc230a','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:31'),(100,'3384650106421960','1f582077-cd2c-4478-a546-8ca548dc230a','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:31'),(101,'3384650106421960','1f582077-cd2c-4478-a546-8ca548dc230a','1',5,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 11:16:31'),(102,'3384650106421960','1f582077-cd2c-4478-a546-8ca548dc230a','1013',6,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 11:16:31'),(103,'3384650106421960','01d6cc8e-85b9-4cda-be62-0bba45224fcc','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 14:34:59'),(104,'3384650106421960','01d6cc8e-85b9-4cda-be62-0bba45224fcc','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 14:34:59'),(105,'3384650106421960','01d6cc8e-85b9-4cda-be62-0bba45224fcc','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 14:34:59'),(106,'3384650106421960','01d6cc8e-85b9-4cda-be62-0bba45224fcc','1013',4,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 14:34:59'),(107,'3384650106421960','01d6cc8e-85b9-4cda-be62-0bba45224fcc','1011',5,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 14:34:59'),(108,'3384650106421960','01d6cc8e-85b9-4cda-be62-0bba45224fcc','1001',6,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 14:34:59'),(109,'3384650106421960','23512c37-7297-4e9f-9cbe-5145e6cc70ac','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 20:26:33'),(110,'3384650106421960','23512c37-7297-4e9f-9cbe-5145e6cc70ac','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:33'),(111,'3384650106421960','23512c37-7297-4e9f-9cbe-5145e6cc70ac','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:33'),(112,'3384650106421960','23512c37-7297-4e9f-9cbe-5145e6cc70ac','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:33'),(113,'3384650106421960','23512c37-7297-4e9f-9cbe-5145e6cc70ac','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:33'),(114,'3384650106421960','23512c37-7297-4e9f-9cbe-5145e6cc70ac','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:33'),(115,'3384650106421960','fcafdf39-e1d6-4091-8252-7780606d3849','1008',1,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:35'),(116,'3384650106421960','fcafdf39-e1d6-4091-8252-7780606d3849','1006',2,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:35'),(117,'3384650106421960','fcafdf39-e1d6-4091-8252-7780606d3849','1',3,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 20:26:35'),(118,'3384650106421960','fcafdf39-e1d6-4091-8252-7780606d3849','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:35'),(119,'3384650106421960','fcafdf39-e1d6-4091-8252-7780606d3849','1013',5,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:35'),(120,'3384650106421960','fcafdf39-e1d6-4091-8252-7780606d3849','1001',6,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:35'),(121,'3384650106421960','f90e319c-fe26-450a-9aa8-4f52b9f81fef','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 20:26:37'),(122,'3384650106421960','f90e319c-fe26-450a-9aa8-4f52b9f81fef','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:37'),(123,'3384650106421960','f90e319c-fe26-450a-9aa8-4f52b9f81fef','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:37'),(124,'3384650106421960','f90e319c-fe26-450a-9aa8-4f52b9f81fef','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:37'),(125,'3384650106421960','f90e319c-fe26-450a-9aa8-4f52b9f81fef','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:37'),(126,'3384650106421960','f90e319c-fe26-450a-9aa8-4f52b9f81fef','1001',6,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 20:26:37'),(127,'3384650106421960','c20fe606-5c48-4b93-bffb-091475546a50','1008',1,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:41'),(128,'3384650106421960','c20fe606-5c48-4b93-bffb-091475546a50','1006',2,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:41'),(129,'3384650106421960','c20fe606-5c48-4b93-bffb-091475546a50','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:41'),(130,'3384650106421960','c20fe606-5c48-4b93-bffb-091475546a50','1',4,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 21:20:41'),(131,'3384650106421960','c20fe606-5c48-4b93-bffb-091475546a50','1001',5,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:41'),(132,'3384650106421960','c20fe606-5c48-4b93-bffb-091475546a50','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:41'),(133,'3384650106421960','ba5e6c33-6845-4ac6-b12e-582ce47ce6ea','1011',1,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:43'),(134,'3384650106421960','ba5e6c33-6845-4ac6-b12e-582ce47ce6ea','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:43'),(135,'3384650106421960','ba5e6c33-6845-4ac6-b12e-582ce47ce6ea','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:43'),(136,'3384650106421960','ba5e6c33-6845-4ac6-b12e-582ce47ce6ea','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:43'),(137,'3384650106421960','ba5e6c33-6845-4ac6-b12e-582ce47ce6ea','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:20:43'),(138,'3384650106421960','ba5e6c33-6845-4ac6-b12e-582ce47ce6ea','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 21:20:43'),(139,'3384650106421960','5ee8a2e4-129b-436a-ad36-36b896ba58f5','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:00'),(140,'3384650106421960','5ee8a2e4-129b-436a-ad36-36b896ba58f5','1',2,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 21:55:00'),(141,'3384650106421960','5ee8a2e4-129b-436a-ad36-36b896ba58f5','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:00'),(142,'3384650106421960','5ee8a2e4-129b-436a-ad36-36b896ba58f5','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:00'),(143,'3384650106421960','5ee8a2e4-129b-436a-ad36-36b896ba58f5','1013',5,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:00'),(144,'3384650106421960','5ee8a2e4-129b-436a-ad36-36b896ba58f5','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:00'),(145,'3384650106421960','62f3bba6-df96-404a-ab5e-53331e29779a','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:02'),(146,'3384650106421960','62f3bba6-df96-404a-ab5e-53331e29779a','1013',2,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:02'),(147,'3384650106421960','62f3bba6-df96-404a-ab5e-53331e29779a','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:02'),(148,'3384650106421960','62f3bba6-df96-404a-ab5e-53331e29779a','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:02'),(149,'3384650106421960','62f3bba6-df96-404a-ab5e-53331e29779a','1',5,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-30 21:55:02'),(150,'3384650106421960','62f3bba6-df96-404a-ab5e-53331e29779a','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-30 21:55:02'),(151,'3384650106421960','4104d7b2-9818-484e-9fe7-9355548f2ebe','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:29'),(152,'3384650106421960','4104d7b2-9818-484e-9fe7-9355548f2ebe','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:29'),(153,'3384650106421960','4104d7b2-9818-484e-9fe7-9355548f2ebe','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:29'),(154,'3384650106421960','4104d7b2-9818-484e-9fe7-9355548f2ebe','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:29'),(155,'3384650106421960','4104d7b2-9818-484e-9fe7-9355548f2ebe','1013',5,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:29'),(156,'3384650106421960','4104d7b2-9818-484e-9fe7-9355548f2ebe','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 10:29:29'),(157,'3384650106421960','ca6c404c-1ffb-463f-a562-6ee8ed27b42d','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:31'),(158,'3384650106421960','ca6c404c-1ffb-463f-a562-6ee8ed27b42d','1013',2,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:31'),(159,'3384650106421960','ca6c404c-1ffb-463f-a562-6ee8ed27b42d','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:31'),(160,'3384650106421960','ca6c404c-1ffb-463f-a562-6ee8ed27b42d','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:31'),(161,'3384650106421960','ca6c404c-1ffb-463f-a562-6ee8ed27b42d','1011',5,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:31'),(162,'3384650106421960','ca6c404c-1ffb-463f-a562-6ee8ed27b42d','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 10:29:31'),(163,'3384650106421960','25d1ba23-8758-460f-9d41-239e4debbfaa','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:33'),(164,'3384650106421960','25d1ba23-8758-460f-9d41-239e4debbfaa','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:33'),(165,'3384650106421960','25d1ba23-8758-460f-9d41-239e4debbfaa','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:33'),(166,'3384650106421960','25d1ba23-8758-460f-9d41-239e4debbfaa','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:33'),(167,'3384650106421960','25d1ba23-8758-460f-9d41-239e4debbfaa','1001',5,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 10:29:33'),(168,'3384650106421960','25d1ba23-8758-460f-9d41-239e4debbfaa','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 10:29:33'),(169,'3384650106421960','1947a8fd-a6bb-4030-9184-45124d638253','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:01'),(170,'3384650106421960','1947a8fd-a6bb-4030-9184-45124d638253','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:01'),(171,'3384650106421960','1947a8fd-a6bb-4030-9184-45124d638253','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:01'),(172,'3384650106421960','1947a8fd-a6bb-4030-9184-45124d638253','1013',4,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:01'),(173,'3384650106421960','1947a8fd-a6bb-4030-9184-45124d638253','1011',5,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:01'),(174,'3384650106421960','1947a8fd-a6bb-4030-9184-45124d638253','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:01:01'),(175,'3384650106421960','f0de1fc7-96d0-4064-8493-726475996d0f','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:10'),(176,'3384650106421960','f0de1fc7-96d0-4064-8493-726475996d0f','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:10'),(177,'3384650106421960','f0de1fc7-96d0-4064-8493-726475996d0f','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:10'),(178,'3384650106421960','f0de1fc7-96d0-4064-8493-726475996d0f','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:10'),(179,'3384650106421960','f0de1fc7-96d0-4064-8493-726475996d0f','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:10'),(180,'3384650106421960','f0de1fc7-96d0-4064-8493-726475996d0f','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:01:10'),(181,'3384650106421960','0eef1f1e-f2c8-46a2-b535-bfa55de156a7','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:01:13'),(182,'3384650106421960','0eef1f1e-f2c8-46a2-b535-bfa55de156a7','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:13'),(183,'3384650106421960','0eef1f1e-f2c8-46a2-b535-bfa55de156a7','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:13'),(184,'3384650106421960','0eef1f1e-f2c8-46a2-b535-bfa55de156a7','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:13'),(185,'3384650106421960','0eef1f1e-f2c8-46a2-b535-bfa55de156a7','1013',5,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:13'),(186,'3384650106421960','0eef1f1e-f2c8-46a2-b535-bfa55de156a7','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:01:13'),(187,'3384650106421960','253d9aae-e922-4d75-9e39-d023076a659b','1011',1,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:04'),(188,'3384650106421960','253d9aae-e922-4d75-9e39-d023076a659b','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:04'),(189,'3384650106421960','253d9aae-e922-4d75-9e39-d023076a659b','1',3,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:06:04'),(190,'3384650106421960','253d9aae-e922-4d75-9e39-d023076a659b','1013',4,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:04'),(191,'3384650106421960','253d9aae-e922-4d75-9e39-d023076a659b','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:04'),(192,'3384650106421960','253d9aae-e922-4d75-9e39-d023076a659b','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:04'),(193,'3384650106421960','5e2abaeb-7546-4e3e-83ed-989c5a0dbba0','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:06:06'),(194,'3384650106421960','5e2abaeb-7546-4e3e-83ed-989c5a0dbba0','1013',2,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:06'),(195,'3384650106421960','5e2abaeb-7546-4e3e-83ed-989c5a0dbba0','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:06'),(196,'3384650106421960','5e2abaeb-7546-4e3e-83ed-989c5a0dbba0','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:06'),(197,'3384650106421960','5e2abaeb-7546-4e3e-83ed-989c5a0dbba0','1011',5,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:06'),(198,'3384650106421960','5e2abaeb-7546-4e3e-83ed-989c5a0dbba0','1001',6,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:06'),(199,'3384650106421960','27ef065f-c09a-49c8-9727-a18cb2819dd5','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:09'),(200,'3384650106421960','27ef065f-c09a-49c8-9727-a18cb2819dd5','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:09'),(201,'3384650106421960','27ef065f-c09a-49c8-9727-a18cb2819dd5','1',3,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:06:09'),(202,'3384650106421960','27ef065f-c09a-49c8-9727-a18cb2819dd5','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:09'),(203,'3384650106421960','27ef065f-c09a-49c8-9727-a18cb2819dd5','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:09'),(204,'3384650106421960','27ef065f-c09a-49c8-9727-a18cb2819dd5','1001',6,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:09'),(205,'3384650106421960','61fd0a2c-fd3e-41ee-b6f5-6ae82ad486ce','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:10'),(206,'3384650106421960','61fd0a2c-fd3e-41ee-b6f5-6ae82ad486ce','1013',2,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:10'),(207,'3384650106421960','61fd0a2c-fd3e-41ee-b6f5-6ae82ad486ce','1011',3,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:10'),(208,'3384650106421960','61fd0a2c-fd3e-41ee-b6f5-6ae82ad486ce','1',4,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:06:10'),(209,'3384650106421960','61fd0a2c-fd3e-41ee-b6f5-6ae82ad486ce','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:10'),(210,'3384650106421960','61fd0a2c-fd3e-41ee-b6f5-6ae82ad486ce','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:10'),(211,'3384650106421960','61219a1b-83e3-4beb-9b04-f9caa25ba23d','1011',1,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:13'),(212,'3384650106421960','61219a1b-83e3-4beb-9b04-f9caa25ba23d','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:13'),(213,'3384650106421960','61219a1b-83e3-4beb-9b04-f9caa25ba23d','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:13'),(214,'3384650106421960','61219a1b-83e3-4beb-9b04-f9caa25ba23d','1',4,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:06:13'),(215,'3384650106421960','61219a1b-83e3-4beb-9b04-f9caa25ba23d','1013',5,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:13'),(216,'3384650106421960','61219a1b-83e3-4beb-9b04-f9caa25ba23d','1001',6,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:06:13'),(217,'3384650106421960','3088f6dd-00b4-48d7-bb84-ffa4528aa35a','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:09:56'),(218,'3384650106421960','3088f6dd-00b4-48d7-bb84-ffa4528aa35a','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:56'),(219,'3384650106421960','3088f6dd-00b4-48d7-bb84-ffa4528aa35a','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:56'),(220,'3384650106421960','3088f6dd-00b4-48d7-bb84-ffa4528aa35a','1001',4,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:56'),(221,'3384650106421960','3088f6dd-00b4-48d7-bb84-ffa4528aa35a','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:56'),(222,'3384650106421960','3088f6dd-00b4-48d7-bb84-ffa4528aa35a','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:56'),(223,'3384650106421960','2fbff67c-effb-45e4-9ac1-df9bfc3ba3eb','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:09:58'),(224,'3384650106421960','2fbff67c-effb-45e4-9ac1-df9bfc3ba3eb','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:58'),(225,'3384650106421960','2fbff67c-effb-45e4-9ac1-df9bfc3ba3eb','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:58'),(226,'3384650106421960','2fbff67c-effb-45e4-9ac1-df9bfc3ba3eb','1001',4,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:58'),(227,'3384650106421960','2fbff67c-effb-45e4-9ac1-df9bfc3ba3eb','1013',5,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:58'),(228,'3384650106421960','2fbff67c-effb-45e4-9ac1-df9bfc3ba3eb','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:09:58'),(229,'3384650106421960','388c10d5-c9a3-4de5-ac30-a754ff52f7a1','1008',1,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:23'),(230,'3384650106421960','388c10d5-c9a3-4de5-ac30-a754ff52f7a1','1006',2,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:23'),(231,'3384650106421960','388c10d5-c9a3-4de5-ac30-a754ff52f7a1','1001',3,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:23'),(232,'3384650106421960','388c10d5-c9a3-4de5-ac30-a754ff52f7a1','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:23'),(233,'3384650106421960','388c10d5-c9a3-4de5-ac30-a754ff52f7a1','1013',5,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:23'),(234,'3384650106421960','388c10d5-c9a3-4de5-ac30-a754ff52f7a1','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:10:23'),(235,'3384650106421960','4339fdf5-5d92-4f6e-accd-196386d9eca0','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:25'),(236,'3384650106421960','4339fdf5-5d92-4f6e-accd-196386d9eca0','1',2,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:10:25'),(237,'3384650106421960','4339fdf5-5d92-4f6e-accd-196386d9eca0','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:25'),(238,'3384650106421960','4339fdf5-5d92-4f6e-accd-196386d9eca0','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:25'),(239,'3384650106421960','4339fdf5-5d92-4f6e-accd-196386d9eca0','1011',5,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:25'),(240,'3384650106421960','4339fdf5-5d92-4f6e-accd-196386d9eca0','1013',6,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:25'),(241,'3384650106421960','044655b7-b89c-433d-935e-97c2078e838a','1013',1,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:41'),(242,'3384650106421960','044655b7-b89c-433d-935e-97c2078e838a','1',2,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:10:41'),(243,'3384650106421960','044655b7-b89c-433d-935e-97c2078e838a','1001',3,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:41'),(244,'3384650106421960','044655b7-b89c-433d-935e-97c2078e838a','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:41'),(245,'3384650106421960','044655b7-b89c-433d-935e-97c2078e838a','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:41'),(246,'3384650106421960','044655b7-b89c-433d-935e-97c2078e838a','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:41'),(247,'3384650106421960','f578c0ce-215d-45d3-8169-2476d5a84a85','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:10:43'),(248,'3384650106421960','f578c0ce-215d-45d3-8169-2476d5a84a85','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:43'),(249,'3384650106421960','f578c0ce-215d-45d3-8169-2476d5a84a85','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:43'),(250,'3384650106421960','f578c0ce-215d-45d3-8169-2476d5a84a85','1001',4,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:43'),(251,'3384650106421960','f578c0ce-215d-45d3-8169-2476d5a84a85','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:43'),(252,'3384650106421960','f578c0ce-215d-45d3-8169-2476d5a84a85','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:10:43'),(253,'3384650106421960','41211cf2-65b6-482f-9187-37f4d8149ab0','1011',1,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:15:39'),(254,'3384650106421960','41211cf2-65b6-482f-9187-37f4d8149ab0','1008',2,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:15:39'),(255,'3384650106421960','41211cf2-65b6-482f-9187-37f4d8149ab0','1006',3,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:15:39'),(256,'3384650106421960','41211cf2-65b6-482f-9187-37f4d8149ab0','1013',4,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:15:39'),(257,'3384650106421960','41211cf2-65b6-482f-9187-37f4d8149ab0','1001',5,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:15:39'),(258,'3384650106421960','41211cf2-65b6-482f-9187-37f4d8149ab0','1',6,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:15:39'),(259,'3384650106421960','cd2b64da-eab1-4c44-a7ed-db410821da1e','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:16:16'),(260,'3384650106421960','cd2b64da-eab1-4c44-a7ed-db410821da1e','1013',2,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:16'),(261,'3384650106421960','cd2b64da-eab1-4c44-a7ed-db410821da1e','1001',3,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:16'),(262,'3384650106421960','cd2b64da-eab1-4c44-a7ed-db410821da1e','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:16'),(263,'3384650106421960','cd2b64da-eab1-4c44-a7ed-db410821da1e','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:16'),(264,'3384650106421960','cd2b64da-eab1-4c44-a7ed-db410821da1e','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:16'),(265,'3384650106421960','24ddca8f-77c2-4033-8512-212768a82723','1',1,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:16:19'),(266,'3384650106421960','24ddca8f-77c2-4033-8512-212768a82723','1001',2,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:19'),(267,'3384650106421960','24ddca8f-77c2-4033-8512-212768a82723','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:19'),(268,'3384650106421960','24ddca8f-77c2-4033-8512-212768a82723','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:19'),(269,'3384650106421960','24ddca8f-77c2-4033-8512-212768a82723','1013',5,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:19'),(270,'3384650106421960','24ddca8f-77c2-4033-8512-212768a82723','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:19'),(271,'3384650106421960','d87f121b-ea09-40e8-964d-fdbf12066dda','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:47'),(272,'3384650106421960','d87f121b-ea09-40e8-964d-fdbf12066dda','1013',2,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:47'),(273,'3384650106421960','d87f121b-ea09-40e8-964d-fdbf12066dda','1',3,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:16:47'),(274,'3384650106421960','d87f121b-ea09-40e8-964d-fdbf12066dda','1008',4,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:47'),(275,'3384650106421960','d87f121b-ea09-40e8-964d-fdbf12066dda','1006',5,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:47'),(276,'3384650106421960','d87f121b-ea09-40e8-964d-fdbf12066dda','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:16:47'),(277,'3384650106421960','447367db-25b5-44a0-b063-1e6e252a0fec','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:31'),(278,'3384650106421960','447367db-25b5-44a0-b063-1e6e252a0fec','1011',2,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:31'),(279,'3384650106421960','447367db-25b5-44a0-b063-1e6e252a0fec','1',3,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:19:31'),(280,'3384650106421960','447367db-25b5-44a0-b063-1e6e252a0fec','1013',4,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:31'),(281,'3384650106421960','447367db-25b5-44a0-b063-1e6e252a0fec','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:31'),(282,'3384650106421960','447367db-25b5-44a0-b063-1e6e252a0fec','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:31'),(283,'3384650106421960','c2a497c5-c280-4c50-9000-387db05916be','1008',1,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:36'),(284,'3384650106421960','c2a497c5-c280-4c50-9000-387db05916be','1006',2,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:36'),(285,'3384650106421960','c2a497c5-c280-4c50-9000-387db05916be','1001',3,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:36'),(286,'3384650106421960','c2a497c5-c280-4c50-9000-387db05916be','1013',4,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:36'),(287,'3384650106421960','c2a497c5-c280-4c50-9000-387db05916be','1',5,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:19:36'),(288,'3384650106421960','c2a497c5-c280-4c50-9000-387db05916be','1011',6,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:19:36'),(289,'3384650106421960','18cacf6e-c2ff-4f0f-8152-67c69fdb75b2','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:46:29'),(290,'3384650106421960','18cacf6e-c2ff-4f0f-8152-67c69fdb75b2','1',2,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 11:46:29'),(291,'3384650106421960','18cacf6e-c2ff-4f0f-8152-67c69fdb75b2','1008',3,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:46:29'),(292,'3384650106421960','18cacf6e-c2ff-4f0f-8152-67c69fdb75b2','1006',4,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:46:29'),(293,'3384650106421960','18cacf6e-c2ff-4f0f-8152-67c69fdb75b2','1011',5,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:46:29'),(294,'3384650106421960','18cacf6e-c2ff-4f0f-8152-67c69fdb75b2','1013',6,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 11:46:29'),(295,'3384650106421960','2bea5d58-c8e9-4fce-a27b-203db8a69736','1011',1,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 13:20:42'),(296,'3384650106421960','2bea5d58-c8e9-4fce-a27b-203db8a69736','1',2,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 13:20:42'),(297,'3384650106421960','2bea5d58-c8e9-4fce-a27b-203db8a69736','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 13:20:42'),(298,'3384650106421960','2bea5d58-c8e9-4fce-a27b-203db8a69736','1001',4,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 13:20:42'),(299,'3384650106421960','2bea5d58-c8e9-4fce-a27b-203db8a69736','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 13:20:42'),(300,'3384650106421960','2bea5d58-c8e9-4fce-a27b-203db8a69736','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 13:20:42'),(301,'3384650106421960','36a9392b-8556-4dc0-b4d5-1d9f57059de5','1001',1,15.3500,'hybrid','{\"factors\": [{\"name\": \"热度7550%\", \"type\": \"popularity\", \"score\": 75.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 14:59:29'),(302,'3384650106421960','36a9392b-8556-4dc0-b4d5-1d9f57059de5','1',2,0.2500,'hybrid','{\"factors\": [{\"name\": \"热度0%\", \"type\": \"popularity\", \"score\": 0.0}], \"primary\": \"系统推荐\"}',0,0,NULL,'2026-01-31 14:59:29'),(303,'3384650106421960','36a9392b-8556-4dc0-b4d5-1d9f57059de5','1013',3,18.9500,'hybrid','{\"factors\": [{\"name\": \"热度9350%\", \"type\": \"popularity\", \"score\": 93.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 14:59:29'),(304,'3384650106421960','36a9392b-8556-4dc0-b4d5-1d9f57059de5','1011',4,19.0900,'hybrid','{\"factors\": [{\"name\": \"热度9420%\", \"type\": \"popularity\", \"score\": 94.2}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 14:59:29'),(305,'3384650106421960','36a9392b-8556-4dc0-b4d5-1d9f57059de5','1008',5,18.6700,'hybrid','{\"factors\": [{\"name\": \"热度9210%\", \"type\": \"popularity\", \"score\": 92.1}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 14:59:29'),(306,'3384650106421960','36a9392b-8556-4dc0-b4d5-1d9f57059de5','1006',6,18.5500,'hybrid','{\"factors\": [{\"name\": \"热度9150%\", \"type\": \"popularity\", \"score\": 91.5}], \"primary\": \"热门推荐\"}',0,0,NULL,'2026-01-31 14:59:29');
/*!40000 ALTER TABLE `recommendation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_add_dish_request`
--

DROP TABLE IF EXISTS `t_add_dish_request`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_add_dish_request` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'åŠ èœè¯·æ±‚ID',
  `group_order_id` bigint NOT NULL COMMENT 'ç¾¤è®¢å•ID',
  `original_order_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'åŽŸè®¢å•ID(å…³è”å·²æ”¯ä»˜è®¢å•)',
  `request_user_id` bigint NOT NULL COMMENT 'åŠ èœè¯·æ±‚äººID',
  `merchant_id` bigint NOT NULL COMMENT 'å•†å®¶ID',
  `dish_info` json NOT NULL COMMENT 'åŠ èœèœå“ä¿¡æ¯JSON: [{dishId,quantity,customization,price}]',
  `total_amount` decimal(10,2) NOT NULL COMMENT 'åŠ èœæ€»é‡‘é¢',
  `approval_status` tinyint NOT NULL DEFAULT '0' COMMENT 'å®¡æ ¸çŠ¶æ€: 0-å¾…å®¡æ ¸,1-å®¡æ ¸é€šè¿‡,2-å®¡æ ¸é©³å›ž,3-å·²æ’¤å›ž,4-è¶…æ—¶é©³å›ž',
  `reject_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é©³å›žåŽŸå› ',
  `reviewer_id` bigint DEFAULT NULL COMMENT 'å®¡æ ¸äººID(ç¾¤è®¢å•å‘èµ·è€…)',
  `review_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `first_remind_time` datetime DEFAULT NULL COMMENT 'é¦–æ¬¡æé†’æ—¶é—´(10åˆ†é’Ÿ)',
  `second_remind_time` datetime DEFAULT NULL COMMENT 'äºŒæ¬¡æé†’æ—¶é—´',
  `timeout_time` datetime DEFAULT NULL COMMENT 'è¶…æ—¶æ—¶é—´(15åˆ†é’Ÿ)',
  `related_payment_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”æ”¯ä»˜è®°å½•ID',
  `related_order_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”è®¢å•ID(å®¡æ ¸é€šè¿‡åŽåˆ›å»º)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_group_order_id` (`group_order_id`),
  KEY `idx_approval_status` (`approval_status`),
  KEY `idx_timeout_time` (`timeout_time`),
  KEY `idx_request_user` (`request_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='åŠ èœè¯·æ±‚è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_add_dish_request`
--

LOCK TABLES `t_add_dish_request` WRITE;
/*!40000 ALTER TABLE `t_add_dish_request` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_add_dish_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_add_dish_setting`
--

DROP TABLE IF EXISTS `t_add_dish_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_add_dish_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_order_id` bigint NOT NULL COMMENT 'ç¾¤è®¢å•ID',
  `add_dish_permission` tinyint NOT NULL DEFAULT '0' COMMENT 'åŠ èœæƒé™: 0-å…¨å‘˜å¯åŠ èœ,1-ä»…å‘èµ·è€…å¯åŠ èœ',
  `budget_limit` decimal(10,2) DEFAULT NULL COMMENT 'å•æ¬¡åŠ èœé¢„ç®—é™åˆ¶(å¯é€‰)',
  `max_dish_count` int DEFAULT NULL COMMENT 'å•æ¬¡åŠ èœæ•°é‡é™åˆ¶(å¯é€‰)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_order_id` (`group_order_id`),
  KEY `idx_group_order_id` (`group_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='åŠ èœè®¾ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_add_dish_setting`
--

LOCK TABLES `t_add_dish_setting` WRITE;
/*!40000 ALTER TABLE `t_add_dish_setting` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_add_dish_setting` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `t_address`
--

LOCK TABLES `t_address` WRITE;
/*!40000 ALTER TABLE `t_address` DISABLE KEYS */;
INSERT INTO `t_address` VALUES (1,6,'13822223333','13822223333','河南省','漯河市','','asdasd',0,'2025-12-12 20:34:38','2025-12-12 20:34:38'),(2,6,'test','13822223331','广东省','肇庆市','','asdasd',1,'2025-12-12 20:45:04','2025-12-12 20:45:10');
/*!40000 ALTER TABLE `t_address` ENABLE KEYS */;
UNLOCK TABLES;

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
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `create_by` bigint DEFAULT NULL COMMENT 'åˆ›å»ºäººID',
  `update_by` bigint DEFAULT NULL COMMENT 'æ›´æ–°äººID',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤‡æ³¨',
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç®¡ç†å‘˜è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_admin`
--

LOCK TABLES `t_admin` WRITE;
/*!40000 ALTER TABLE `t_admin` DISABLE KEYS */;
INSERT INTO `t_admin` VALUES (1,'admin','$2a$10$wCbmy/lSuxrVSlArXI1TA.l5Uxhvhj7gn/5xf/Q85XuEU29PM4x0O','asdfghj',NULL,NULL,NULL,'ACTIVE',1,'2026-02-10 01:27:48',NULL,'2026-01-31 11:39:20','2026-01-31 16:42:23',NULL,NULL,NULL);
/*!40000 ALTER TABLE `t_admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_admin_operation_log`
--

DROP TABLE IF EXISTS `t_admin_operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_admin_operation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'æ—¥å¿—ID',
  `admin_id` bigint DEFAULT NULL COMMENT 'ç®¡ç†å‘˜ID',
  `username` varchar(50) DEFAULT NULL COMMENT 'ç®¡ç†å‘˜ç”¨æˆ·å',
  `operation_type` varchar(50) DEFAULT NULL COMMENT 'æ“ä½œç±»åž‹ï¼šLOGIN, LOGOUT, CREATE, UPDATE, DELETE, AUDITç­‰',
  `module_name` varchar(50) DEFAULT NULL COMMENT 'æ¨¡å—åç§°',
  `operation_desc` varchar(255) DEFAULT NULL COMMENT 'æ“ä½œæè¿°',
  `request_method` varchar(10) DEFAULT NULL COMMENT 'è¯·æ±‚æ–¹æ³•ï¼šGET, POST, PUT, DELETE',
  `request_url` varchar(255) DEFAULT NULL COMMENT 'è¯·æ±‚URL',
  `request_params` text COMMENT 'è¯·æ±‚å‚æ•°',
  `response_result` text COMMENT 'å“åº”ç»“æžœ',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IPåœ°å€',
  `execute_time` int DEFAULT NULL COMMENT 'æ‰§è¡Œæ—¶é•¿ï¼ˆæ¯«ç§’ï¼‰',
  `status` varchar(20) DEFAULT NULL COMMENT 'çŠ¶æ€ï¼šSUCCESS-æˆåŠŸ, FAIL-å¤±è´¥',
  `error_message` text COMMENT 'é”™è¯¯ä¿¡æ¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`log_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç®¡ç†å‘˜æ“ä½œæ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_admin_operation_log`
--

LOCK TABLES `t_admin_operation_log` WRITE;
/*!40000 ALTER TABLE `t_admin_operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_admin_operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_admin_role_relation`
--

DROP TABLE IF EXISTS `t_admin_role_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_admin_role_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å…³è”ID',
  `admin_id` bigint NOT NULL COMMENT 'ç®¡ç†å‘˜ID',
  `role_id` bigint NOT NULL COMMENT 'è§’è‰²ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`,`role_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç®¡ç†å‘˜-è§’è‰²å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_admin_role_relation`
--

LOCK TABLES `t_admin_role_relation` WRITE;
/*!40000 ALTER TABLE `t_admin_role_relation` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_admin_role_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_ai_chat_history`
--

DROP TABLE IF EXISTS `t_ai_chat_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ai_chat_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
  `sender` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å‘é€è€…ï¼šuser-ç”¨æˆ·ï¼Œai-AIåŠ©æ‰‹',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ¶ˆæ¯å†…å®¹',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AIèŠå¤©åŽ†å²è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_ai_chat_history`
--

LOCK TABLES `t_ai_chat_history` WRITE;
/*!40000 ALTER TABLE `t_ai_chat_history` DISABLE KEYS */;
INSERT INTO `t_ai_chat_history` VALUES (12,'3384650106421960','ai','您好！我是您的AI饮食助手。有什么可以帮您的吗？','2026-01-23 15:01:52'),(13,'3384650106421960','user','适合运动后的食物','2026-01-23 15:20:28'),(14,'3384650106421960','user','test','2026-01-23 15:22:52'),(15,'3384650106421960','ai','您好！看起来您可能只是想测试一下我是否可以正常响应。是的，我可以正常工作。如果您有任何关于饮食健康的问题，或者需要个性化的饮食建议，请随时告诉我，我会尽我所能提供帮助。现在，请问有什么可以帮助您的？','2026-01-23 15:22:54'),(16,'3384650106421960','user','test2','2026-01-23 15:22:58'),(17,'3384650106421960','user','hello','2026-01-23 18:14:31'),(18,'3384650106421960','ai','你好！我是“佳食宜选”的专业AI饮食助手，很高兴为你提供饮食健康方面的帮助。有什么关于饮食的问题或者需要个性化的饮食建议吗？我在这里为你解答。','2026-01-23 18:14:34'),(19,'3384650106421960','user','test','2026-01-23 18:20:54'),(20,'3384650106421960','ai','您好！看起来您可能想测试一下我的功能。请告诉我您在饮食方面有什么具体的问题或需求，无论是关于营养搭配、健康食谱推荐，还是对特定食物的疑问，我都会尽力为您提供专业且实用的建议。','2026-01-23 18:20:57'),(21,'3384650106421960','user','测试','2026-01-23 19:56:37'),(22,'3384650106421960','ai','您好！我是“佳食宜选”的专业AI饮食助手。请问有什么关于饮食健康的问题或者需要个性化的饮食建议吗？无论是营养搭配、食谱推荐，还是食物营养成分分析，我都会尽力为您提供专业且实用的建议。','2026-01-23 19:56:40'),(23,'3384650106421960','user','适合运动后的食物','2026-01-23 19:58:03'),(24,'3384650106421960','ai','运动后补充营养是非常重要的，可以帮助身体恢复和增强肌肉。以下是一些适合运动后食用的食物建议：\n\n1. **蛋白质**：蛋白质有助于肌肉修复和生长。\n   - 鸡胸肉、鱼、豆腐、低脂酸奶、鸡蛋和坚果都是不错的选择。\n\n2. **碳水化合物**：碳水化合物可以迅速补充运动中消耗的糖原，恢复能量。\n   - 全谷物面包、燕麦、米饭、土豆和水果都是很好的碳水化合物来源。\n\n3. **健康脂肪**：健康的脂肪有助于身体吸收其他营养素。\n   - 鳄梨、坚果、橄榄油和鱼油都是富含健康脂肪的食物。\n\n4. **水分**：运动后补充水分同样重要，以避免脱水和恢复体液平衡。\n   - 清水、运动饮料或含有电解质的饮料都是不错的选择。\n\n以下是一个运动后饮食的例子：\n\n- **蛋白质**：一份鸡胸肉或豆腐\n- **碳水化合物**：一份全麦面包或一份燕麦粥\n- **健康脂肪**：一小把杏仁或一些鳄梨\n- **水分**：一大杯水或运动饮料\n\n此外，以下是一些额外的建议：\n\n- 运动后30分钟内食用这些食物可以最大化恢复效果。\n- 避免高糖和高脂肪的食物，因为它们可能导致血糖水平迅速上升后又下降。\n- 如果可能，尝试将食物混合成一杯蛋白质奶昔，这样既方便又快速。\n\n记得，每个人的身体和需求都是不同的，所以最好根据自己的具体情况调整饮食。','2026-01-23 19:58:13'),(25,'4590322501655448','ai','您好！我是您的AI饮食助手。😊\n\n我可以帮助您：\n- 推荐健康食谱\n- 分析营养成分\n- 提供饮食建议\n\n有什么可以帮您的吗？','2026-01-26 23:44:35');
/*!40000 ALTER TABLE `t_ai_chat_history` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `t_calorie_record`
--

LOCK TABLES `t_calorie_record` WRITE;
/*!40000 ALTER TABLE `t_calorie_record` DISABLE KEYS */;
INSERT INTO `t_calorie_record` VALUES ('1',1,NULL,1,0,0,0,'早餐','2025-12-05 06:26:00','','ad'),('2',1,NULL,17,0,0,0,'加餐','2025-12-05 11:26:00','bnm','hgk'),('2005534396936896514',4590322501655448,NULL,2,0,0,0,'午餐','2025-12-29 06:00:00','asd','ds'),('2005534615657267201',4590322501655448,NULL,213,0,0,0,'早餐','2025-12-29 07:00:00','ds','ds'),('2005534690693365761',4590322501655448,NULL,4,0,0,0,'早餐','2025-12-29 15:01:00','ds','asd'),('2005535198116069377',4590322501655448,NULL,0,0,0,0,'早餐','2025-12-29 07:02:00','test','dasd'),('2005535296791265282',4590322501655448,NULL,17,0,0,0,'早餐','2025-12-29 09:17:00','test17','17'),('2005538514157977601',4590322501655448,NULL,3,0,0,0,'早餐','2025-12-29 17:17:00','17asd','17ads\n'),('2005622402540642305',4590322501655448,NULL,123,230,12,2,'早餐','2025-12-29 20:48:00','asd','asdasd'),('2007348133046050817',3384650106421960,NULL,23,0.1,0.1,0.1,'早餐','2026-01-03 15:07:00','asd','dasd');
/*!40000 ALTER TABLE `t_calorie_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_chat_msg`
--

DROP TABLE IF EXISTS `t_chat_msg`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_chat_msg` (
  `msg_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `from_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å‘é€è€…ID',
  `to_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æŽ¥æ”¶è€…ID',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä¼šè¯ID(ç§èŠä¸ºç”Ÿæˆçš„ä¼šè¯æ ‡è¯†,ç¾¤èŠä¸ºç¾¤ID)',
  `msg_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ¶ˆæ¯ç±»åž‹ï¼štext/image/file',
  `session_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¼šè¯ç±»åž‹ï¼šsingle/group',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ¶ˆæ¯å†…å®¹',
  `reply_to` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å›žå¤çš„æ¶ˆæ¯ID',
  `reply_content` text COLLATE utf8mb4_unicode_ci COMMENT 'è¢«å›žå¤çš„æ¶ˆæ¯å†…å®¹',
  `reply_from_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'è¢«å›žå¤æ¶ˆæ¯çš„å‘é€è€…ID',
  `reply_from_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'è¢«å›žå¤æ¶ˆæ¯çš„å‘é€è€…åç§°',
  `read_status` tinyint(1) DEFAULT '0' COMMENT 'å·²è¯»çŠ¶æ€:0-æœªè¯»,1-å·²è¯»',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `file_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ–‡ä»¶URLï¼ˆå›¾ç‰‡/æ–‡ä»¶æ¶ˆæ¯æ—¶ä½¿ç”¨ï¼‰',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ–‡ä»¶åï¼ˆæ–‡ä»¶æ¶ˆæ¯æ—¶ä½¿ç”¨ï¼‰',
  `file_size` bigint DEFAULT NULL COMMENT 'æ–‡ä»¶å¤§å°ï¼ˆå­—èŠ‚ï¼‰',
  `file_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ–‡ä»¶MIMEç±»åž‹',
  PRIMARY KEY (`msg_id`),
  KEY `idx_from_to_time` (`from_id`,`to_id`,`create_time`),
  KEY `idx_to_read` (`to_id`,`read_status`),
  KEY `idx_reply_to` (`reply_to`),
  KEY `idx_session_time` (`session_id`,`create_time`),
  KEY `idx_file_url` (`file_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èŠå¤©æ¶ˆæ¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_chat_msg`
--

LOCK TABLES `t_chat_msg` WRITE;
/*!40000 ALTER TABLE `t_chat_msg` DISABLE KEYS */;
INSERT INTO `t_chat_msg` VALUES ('CM1109673929557445','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 21:29:30',NULL,NULL,NULL,NULL),('CM1182219067541597','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','hello',NULL,NULL,NULL,NULL,0,'2026-01-26 18:40:41',NULL,NULL,NULL,NULL),('CM1372818071865121','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','wifi',NULL,NULL,NULL,NULL,0,'2026-01-26 21:49:56',NULL,NULL,NULL,NULL),('CM1409717140800869','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','1',NULL,NULL,NULL,NULL,0,'2026-01-26 23:42:24',NULL,NULL,NULL,NULL),('CM1455590537479639','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','1',NULL,NULL,NULL,NULL,0,'2026-01-26 21:46:20',NULL,NULL,NULL,NULL),('CM1572436399670662','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','hello',NULL,NULL,NULL,NULL,0,'2026-01-26 21:30:38',NULL,NULL,NULL,NULL),('CM1633463857431912','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','5',NULL,NULL,NULL,NULL,0,'2026-01-26 23:42:21',NULL,NULL,NULL,NULL),('CM1695591238485860','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 21:48:51',NULL,NULL,NULL,NULL),('CM1775843208152457','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','1',NULL,NULL,NULL,NULL,0,'2026-01-26 21:30:47',NULL,NULL,NULL,NULL),('CM2318751365317825','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','1',NULL,NULL,NULL,NULL,0,'2026-01-26 22:00:18',NULL,NULL,NULL,NULL),('CM2537734741771730','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 21:46:21',NULL,NULL,NULL,NULL),('CM2554135492117669','4590322501655448','G5505379491825551','S2745020123385201','text','group','a',NULL,NULL,NULL,NULL,0,'2026-01-27 11:23:55',NULL,NULL,NULL,NULL),('CM2901368085300919','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','1',NULL,NULL,NULL,NULL,0,'2026-01-26 21:16:09',NULL,NULL,NULL,NULL),('CM2914020082107767','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 21:47:58',NULL,NULL,NULL,NULL),('CM2919629065667725','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 22:00:22',NULL,NULL,NULL,NULL),('CM2949353515239317','4590322501655448','G5505379491825551','S2745020123385201','text','group','u',NULL,NULL,NULL,NULL,0,'2026-01-26 23:58:41',NULL,NULL,NULL,NULL),('CM3051784420181457','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','4',NULL,NULL,NULL,NULL,0,'2026-01-26 21:46:22',NULL,NULL,NULL,NULL),('CM3087391024105168','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','t',NULL,NULL,NULL,NULL,0,'2026-01-26 21:33:05',NULL,NULL,NULL,NULL),('CM3088832021678100','4590322501655448','G5505379491825551','S2745020123385201','text','group','1',NULL,NULL,NULL,NULL,0,'2026-01-27 16:14:30',NULL,NULL,NULL,NULL),('CM3099597516031332','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','3',NULL,NULL,NULL,NULL,0,'2026-01-26 23:42:25',NULL,NULL,NULL,NULL),('CM3813903171765598','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','3',NULL,NULL,NULL,NULL,0,'2026-01-26 23:42:19',NULL,NULL,NULL,NULL),('CM3928885323024395','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 21:36:27',NULL,NULL,NULL,NULL),('CM4135668433667821','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','hi',NULL,NULL,NULL,NULL,0,'2026-01-26 21:49:59',NULL,NULL,NULL,NULL),('CM4294903350576519','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','4',NULL,NULL,NULL,NULL,0,'2026-01-26 21:30:49',NULL,NULL,NULL,NULL),('CM4364939203528466','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','test',NULL,NULL,NULL,NULL,0,'2026-01-26 21:32:09',NULL,NULL,NULL,NULL),('CM4604220323972751','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','hi',NULL,NULL,NULL,NULL,0,'2026-01-26 21:34:11',NULL,NULL,NULL,NULL),('CM4910127280309419','3384650106421960','G5505379491825551','S2745020123385201','text','group','hell',NULL,NULL,NULL,NULL,0,'2026-01-27 11:24:01',NULL,NULL,NULL,NULL),('CM5043962499450704','3384650106421960','G5505379491825551','S2745020123385201','text','group','3',NULL,NULL,NULL,NULL,0,'2026-01-27 11:30:10',NULL,NULL,NULL,NULL),('CM5045126083888520','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 21:30:48',NULL,NULL,NULL,NULL),('CM5168307199297252','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','asd',NULL,NULL,NULL,NULL,0,'2026-01-26 21:33:33',NULL,NULL,NULL,NULL),('CM5171677122283588','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','1',NULL,NULL,NULL,NULL,0,'2026-01-26 21:36:17',NULL,NULL,NULL,NULL),('CM5184414468163488','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 21:38:49',NULL,NULL,NULL,NULL),('CM5426164176405640','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 22:00:26',NULL,NULL,NULL,NULL),('CM5586697795894332','3384650106421960','G5505379491825551','S2745020123385201','text','group','3',NULL,NULL,NULL,NULL,0,'2026-01-28 09:48:29',NULL,NULL,NULL,NULL),('CM5723113126085988','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 23:42:25',NULL,NULL,NULL,NULL),('CM5962841747047790','3384650106421960','G5505379491825551','S2745020123385201','text','group','4',NULL,NULL,NULL,NULL,0,'2026-01-27 11:47:07',NULL,NULL,NULL,NULL),('CM6353924195010363','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','hi',NULL,NULL,NULL,NULL,0,'2026-01-26 21:49:16',NULL,NULL,NULL,NULL),('CM6382423528425305','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','3',NULL,NULL,NULL,NULL,0,'2026-01-26 21:48:41',NULL,NULL,NULL,NULL),('CM6568408391174229','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','hi',NULL,NULL,NULL,NULL,0,'2026-01-26 18:40:32',NULL,NULL,NULL,NULL),('CM7063583354668864','4590322501655448','G5505379491825551','S2745020123385201','text','group','2',NULL,NULL,NULL,NULL,0,'2026-01-27 11:30:05',NULL,NULL,NULL,NULL),('CM8155646084761041','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','3',NULL,NULL,NULL,NULL,0,'2026-01-26 21:46:22',NULL,NULL,NULL,NULL),('CM8380956281816455','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','3',NULL,NULL,NULL,NULL,0,'2026-01-26 21:30:49',NULL,NULL,NULL,NULL),('CM8567246955332968','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','4',NULL,NULL,NULL,NULL,0,'2026-01-26 23:42:21',NULL,NULL,NULL,NULL),('CM8683917305038309','4590322501655448','3384650106421960','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2026-01-26 21:46:22.152 ERROR 36593 --- [nio-8080-exec-1] c.x.j.websocket.WebSocketMessageService  : ❌ [WebSocket] 推送消息失败给用户 3384650106421960: Java 8 date/time type `java.time.LocalDateTime` not supported by default: add Module \"com.fasterxml.jackson.datatype:jackson-datatype-jsr310\" to enable handling (through reference chain: java.util.HashMap[\"content\"]->java.util.HashMap[\"createTime\"])\n\ncom.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type `java.time.LocalDateT',NULL,NULL,NULL,NULL,0,'2026-01-26 21:46:34',NULL,NULL,NULL,NULL),('CM8794861844022380','4590322501655448','G9728057976243353','S2394929237350660','text','group','2',NULL,NULL,NULL,NULL,0,'2026-01-26 23:45:49',NULL,NULL,NULL,NULL),('CM9201556653491568','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','hello',NULL,NULL,NULL,NULL,0,'2026-01-26 21:48:01',NULL,NULL,NULL,NULL),('CM9251289360910232','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','2',NULL,NULL,NULL,NULL,0,'2026-01-26 21:38:52',NULL,NULL,NULL,NULL),('CM9556046231600188','3384650106421960','G5505379491825551','S2745020123385201','text','group','2',NULL,NULL,NULL,NULL,0,'2026-01-28 09:48:29',NULL,NULL,NULL,NULL),('CM9851709763667623','3384650106421960','4590322501655448','S58234cb0310fdfc04b643ce4c4c8015c','text','single','hi',NULL,NULL,NULL,NULL,0,'2026-01-26 21:34:35',NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `t_chat_msg` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_chat_session`
--

DROP TABLE IF EXISTS `t_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¼šè¯æ ‡è¯†ï¼ˆç§èŠä¸ºåŒæ–¹ç”¨æˆ·IDç»„åˆï¼Œç¾¤èŠä¸ºç¾¤IDï¼‰',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
  `session_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¼šè¯ç±»åž‹: single-ç§èŠ, group-ç¾¤èŠ',
  `session_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¼šè¯åç§°',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä¼šè¯å¤´åƒ',
  `last_message` text COLLATE utf8mb4_unicode_ci COMMENT 'æœ€åŽä¸€æ¡æ¶ˆæ¯å†…å®¹',
  `last_message_time` datetime DEFAULT NULL COMMENT 'æœ€åŽä¸€æ¡æ¶ˆæ¯æ—¶é—´',
  `unread_count` int DEFAULT '0' COMMENT 'æœªè¯»æ¶ˆæ¯æ•°',
  `pinned` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ç½®é¡¶ï¼ˆ0-æœªç½®é¡¶ï¼Œ1-ç½®é¡¶ï¼‰',
  `member_count` int DEFAULT '0' COMMENT 'æˆå‘˜æ•°é‡ï¼ˆç¾¤èŠï¼‰',
  `group_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç¾¤ç»„IDï¼ˆä»…ç¾¤èŠä¼šè¯æœ‰æ•ˆï¼‰',
  `target_id` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç›®æ ‡ç”¨æˆ·IDï¼ˆä»…å•èŠä¼šè¯æœ‰æ•ˆï¼Œè¡¨ç¤ºå¯¹æ–¹çš„userIdï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_session` (`user_id`,`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_session_type` (`session_type`),
  KEY `idx_last_message_time` (`last_message_time`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=79 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èŠå¤©ä¼šè¯è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_chat_session`
--

LOCK TABLES `t_chat_session` WRITE;
/*!40000 ALTER TABLE `t_chat_session` DISABLE KEYS */;
INSERT INTO `t_chat_session` VALUES (71,'S58234cb0310fdfc04b643ce4c4c8015c','3384650106421960','single','nick','👤','3','2026-01-26 23:42:25',0,0,0,NULL,'4590322501655448','2026-01-26 18:40:25','2026-01-26 23:42:25'),(72,'S58234cb0310fdfc04b643ce4c4c8015c','4590322501655448','single','asd','👤','3','2026-01-26 23:42:25',0,0,0,NULL,'3384650106421960','2026-01-26 18:40:32','2026-01-26 23:44:31'),(73,'S2394929237350660','3384650106421960','group','1','👥',NULL,NULL,0,0,2,'G9728057976243353',NULL,'2026-01-26 23:45:24','2026-01-26 23:45:24'),(74,'S2394929237350660','4590322501655448','group','1','👥','2','2026-01-26 23:45:49',0,0,2,'G9728057976243353',NULL,'2026-01-26 23:45:24','2026-01-26 23:45:49'),(76,'S2745020123385201','3384650106421960','group','2','👥','3','2026-01-28 09:48:29',0,0,2,'G5505379491825551',NULL,'2026-01-26 23:58:34','2026-01-28 09:48:29'),(77,'S2745020123385201','4590322501655448','group','2','👥','3','2026-01-28 09:48:29',0,0,2,'G5505379491825551',NULL,'2026-01-26 23:58:34','2026-01-28 09:48:29');
/*!40000 ALTER TABLE `t_chat_session` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=2016694498247081986 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ”¶è—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_collection`
--

LOCK TABLES `t_collection` WRITE;
/*!40000 ALTER TABLE `t_collection` DISABLE KEYS */;
INSERT INTO `t_collection` VALUES (1,1,'merchant',1,'2025-12-12 17:17:26'),(2,1,'dish',1,'2025-12-12 17:17:26'),(3,1,'article',1,'2025-12-12 17:17:26'),(4,1,'merchant',2,'2025-12-12 17:17:26'),(5,1,'dish',2,'2025-12-12 17:17:26'),(6,1,'article',2,'2025-12-12 17:17:26'),(17,3384650106421960,'merchant',7638432224340229,'2026-01-12 16:52:48');
/*!40000 ALTER TABLE `t_collection` ENABLE KEYS */;
UNLOCK TABLES;

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
) ENGINE=InnoDB AUTO_INCREMENT=2016339322692743170 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ¶ˆè´¹è®°å½•';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_consume_history`
--

LOCK TABLES `t_consume_history` WRITE;
/*!40000 ALTER TABLE `t_consume_history` DISABLE KEYS */;
INSERT INTO `t_consume_history` VALUES (1,1,'recharge',100.00,'å……å€¼100ä¸ªå¹³å°å¸','success','2025-12-12 16:50:56'),(2,1,'consume',25.50,'è´­ä¹°å®«ä¿é¸¡ä¸','success','2025-12-12 16:50:56'),(3,1,'consume',18.80,'è´­ä¹°é±¼é¦™è‚‰ä¸','success','2025-12-12 16:50:56'),(4,1,'recharge',50.00,'å……å€¼50ä¸ªå¹³å°å¸','success','2025-12-12 16:50:56'),(5,1,'withdraw',80.00,'æçŽ°80å…ƒ','success','2025-12-12 16:50:56'),(2010978767551393793,3384650106421960,'recharge',333.00,'钱包充值 - RCH1768289674884111','success','2026-01-13 15:34:35'),(2011009877127794689,3384650106421960,'recharge',1234.00,'钱包充值 - RCH1768297092054950','success','2026-01-13 17:38:12'),(2011053099891851265,3384650106421960,'withdraw',300.00,'钱包提现 - WTH1768307397197555','success','2026-01-13 20:29:57'),(2011073926972686338,3384650106421960,'consume',75.00,'支付订单 - JD1768312307843','success','2026-01-13 21:52:43'),(2011387827513749506,3384650106421960,'consume',75.00,'支付订单 - JD1768387196617','success','2026-01-14 18:40:03'),(2011409671339929601,3384650106421960,'consume',75.00,'支付订单 - JD1768392131163','success','2026-01-14 20:06:50'),(2011415898040070145,3384650106421960,'consume',75.00,'支付订单 - JD1768393885910','success','2026-01-14 20:31:35'),(2011432336490524674,3384650106421960,'recharge',75.00,'订单退款 - JD1768392131163 (用户取消订单)','success','2026-01-14 21:36:54'),(2011432346582020097,3384650106421960,'recharge',75.00,'订单退款 - JD1768393885910 (用户取消订单)','success','2026-01-14 21:36:57'),(2012075831005810689,3384650106421960,'consume',195.00,'支付订单 - JD1768551227890','success','2026-01-16 16:13:55'),(2012089506601132033,3384650106421960,'consume',75.00,'支付订单 - JD1768554456940','success','2026-01-16 17:08:16'),(2016145203668303874,3384650106421960,'consume',315.00,'支付订单 - JD1769521187912','success','2026-01-27 21:44:09'),(2016145285998297090,3384650106421960,'recharge',315.00,'订单退款 - JD1769521187912 (用户取消订单)','success','2026-01-27 21:44:29'),(2016332670014894082,3384650106421960,'consume',125.00,'支付订单 - JD1769566136871','success','2026-01-28 10:09:05'),(2016336313019576322,3384650106421960,'consume',125.00,'支付订单 - JD1769567009896','success','2026-01-28 10:23:33'),(2016339322692743169,3384650106421960,'consume',125.00,'支付订单 - JD1769567712571','success','2026-01-28 10:35:31');
/*!40000 ALTER TABLE `t_consume_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_contact`
--

DROP TABLE IF EXISTS `t_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `user_id` varchar(64) NOT NULL COMMENT 'ç”¨æˆ·ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=2015816641958494210 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='联系人关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_contact`
--

LOCK TABLES `t_contact` WRITE;
/*!40000 ALTER TABLE `t_contact` DISABLE KEYS */;
INSERT INTO `t_contact` VALUES (2015734270915534849,'4590322501655448','3384650106421960','friend','normal',NULL,'2026-01-26 18:31:15','2026-01-26 18:31:24'),(2015734308362280962,'3384650106421960','4590322501655448','friend','normal',NULL,'2026-01-26 18:31:24','2026-01-26 18:31:24'),(2015813325904003073,'4590322501655448','G9728057976243353','group','normal',NULL,'2026-01-26 23:45:23','2026-01-26 23:45:23'),(2015813325933363201,'3384650106421960','G9728057976243353','group','normal',NULL,'2026-01-26 23:45:23','2026-01-26 23:45:23'),(2015816641941716993,'4590322501655448','G5505379491825551','group','normal',NULL,'2026-01-26 23:58:34','2026-01-26 23:58:34'),(2015816641958494209,'3384650106421960','G5505379491825551','group','normal',NULL,'2026-01-26 23:58:34','2026-01-26 23:58:34');
/*!40000 ALTER TABLE `t_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_content_extraction`
--

DROP TABLE IF EXISTS `t_content_extraction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_content_extraction` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `source_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å†…å®¹æºID',
  `dish_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“åç§°',
  `dish_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“å›¾ç‰‡',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'èœå“æè¿°',
  `ingredients` text COLLATE utf8mb4_unicode_ci COMMENT 'é£Ÿæåˆ—è¡¨ï¼ˆJSONæ ¼å¼ï¼‰',
  `steps` text COLLATE utf8mb4_unicode_ci COMMENT 'åˆ¶ä½œæ­¥éª¤ï¼ˆJSONæ ¼å¼ï¼‰',
  `cooking_time` int DEFAULT NULL COMMENT 'åˆ¶ä½œæ—¶é•¿ï¼ˆåˆ†é’Ÿï¼‰',
  `difficulty` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'éš¾åº¦ï¼šEASY-ç®€å•, MEDIUM-ä¸­ç­‰, HARD-å›°éš¾',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ ‡ç­¾ï¼ˆé€—å·åˆ†éš”ï¼‰',
  `calories` int DEFAULT NULL COMMENT 'å¡è·¯é‡Œ',
  `is_published` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å·²å‘å¸ƒä¸ºé£Ÿè°±',
  `recipe_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”çš„é£Ÿè°±ID',
  `manual_score` int DEFAULT NULL COMMENT 'äººå·¥è¯„åˆ†ï¼ˆ1-5ï¼‰',
  `is_verified` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦äººå·¥éªŒè¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_dish_name` (`dish_name`),
  KEY `idx_is_published` (`is_published`),
  KEY `idx_is_verified` (`is_verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å†…å®¹æå–è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_content_extraction`
--

LOCK TABLES `t_content_extraction` WRITE;
/*!40000 ALTER TABLE `t_content_extraction` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_content_extraction` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_content_source`
--

DROP TABLE IF EXISTS `t_content_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_content_source` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
  `content_url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å†…å®¹URL',
  `content_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å†…å®¹ç±»åž‹ï¼šVIDEO-è§†é¢‘, ARTICLE-æ–‡ç« , IMAGE-å›¾ç‰‡',
  `platform` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å¹³å°ï¼šDOUYIN-æŠ–éŸ³, XIAOHONGSHU-å°çº¢ä¹¦, BILIBILI-å“”å“©å“”å“©, WECHAT-å¾®ä¿¡, OTHER-å…¶ä»–',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å†…å®¹æ ‡é¢˜',
  `author` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä½œè€…/UPä¸»',
  `cover_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å°é¢å›¾ç‰‡URL',
  `video_duration` int DEFAULT NULL COMMENT 'è§†é¢‘æ—¶é•¿ï¼ˆç§’ï¼‰',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'å†…å®¹æè¿°',
  `is_extracted` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å·²æå–',
  `extraction_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'æå–çŠ¶æ€ï¼šPENDING-å¾…æå–, PROCESSING-æå–ä¸­, SUCCESS-æˆåŠŸ, FAILED-å¤±è´¥',
  `extraction_time` datetime DEFAULT NULL COMMENT 'æå–æ—¶é—´',
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é”™è¯¯ä¿¡æ¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_platform` (`platform`),
  KEY `idx_extraction_status` (`extraction_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å†…å®¹æºè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_content_source`
--

LOCK TABLES `t_content_source` WRITE;
/*!40000 ALTER TABLE `t_content_source` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_content_source` ENABLE KEYS */;
UNLOCK TABLES;

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
  `category` varchar(50) NOT NULL DEFAULT 'å…¶ä»–' COMMENT 'èœå“åˆ†ç±»',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `calorie` int NOT NULL COMMENT '卡路里',
  `estimated_cooking_minutes` int DEFAULT '15' COMMENT 'é¢„ä¼°çƒ¹é¥ªæ—¶é•¿ï¼ˆåˆ†é’Ÿï¼‰',
  `step_template` varchar(20) DEFAULT 'NORMAL' COMMENT 'çƒ¹é¥ªæµç¨‹æ¨¡æ¿ï¼šNORMAL-æ­£é¤æµç¨‹ï¼ŒFAST-å¿«é¤æµç¨‹ï¼ŒCUSTOM-è‡ªå®šä¹‰æµç¨‹',
  `ingredients` json DEFAULT NULL COMMENT '食材列表（JSON格式）',
  `description` text COMMENT '菜品描述',
  `image` varchar(500) DEFAULT NULL COMMENT 'èœå“å›¾ç‰‡URL',
  `score` decimal(5,2) DEFAULT NULL COMMENT 'æŽ¨èå¾—åˆ†',
  `is_online` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦ä¸Šæž¶ï¼š1-ä¸Šæž¶ï¼Œ0-ä¸‹æž¶',
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼šPENDING-å¾…å®¡æ ¸, APPROVED-å·²é€šè¿‡, REJECTED-å·²æ‹’ç»',
  `audit_comment` varchar(500) DEFAULT NULL COMMENT 'å®¡æ ¸æ„è§',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `audit_admin_id` bigint DEFAULT NULL COMMENT 'å®¡æ ¸ç®¡ç†å‘˜ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `view_count` int DEFAULT '0' COMMENT 'æµè§ˆæ¬¡æ•°',
  `order_count` int DEFAULT '0' COMMENT 'ä¸‹å•æ¬¡æ•°',
  `favorite_count` int DEFAULT '0' COMMENT 'æ”¶è—æ¬¡æ•°',
  `avg_rating` decimal(3,2) DEFAULT NULL COMMENT 'å¹³å‡è¯„åˆ†',
  `tags` json DEFAULT NULL COMMENT 'æ ‡ç­¾æ•°ç»„',
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
) ENGINE=InnoDB AUTO_INCREMENT=2008737368059711581 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_dish`
--

LOCK TABLES `t_dish` WRITE;
/*!40000 ALTER TABLE `t_dish` DISABLE KEYS */;
INSERT INTO `t_dish` (`id`, `merchant_id`, `name`, `category`, `price`, `calorie`, `estimated_cooking_minutes`, `step_template`, `ingredients`, `description`, `image`, `score`, `is_online`, `audit_status`, `audit_comment`, `audit_time`, `audit_admin_id`, `create_time`, `update_time`, `view_count`, `order_count`, `favorite_count`, `avg_rating`, `tags`, `stock`) VALUES (1,7638432224340229,'宫保鸡丁','category_1',38.00,450,15,'NORMAL',NULL,'经典川菜',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2025-11-22 11:35:20','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1001,7638432224340229,'皮蛋瘦肉粥','早餐',12.00,180,15,'NORMAL',NULL,'ç»å…¸ç²¤å¼æ—©é¤ç²¥å“ï¼Œè¥å…»æš–èƒƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1002,7638432224340229,'豆浆油条','早餐',8.00,350,15,'NORMAL',NULL,'ä¼ ç»Ÿä¸­å¼æ—©é¤æ­é…',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1003,7638432224340229,'å°ç¬¼åŒ…','早餐',15.00,280,15,'NORMAL',NULL,'çš®è–„é¦…å¤šï¼Œæ±æ°´ä¸°å¯Œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1004,7638432224340229,'ç…Žé¥¼æžœå­','早餐',10.00,320,15,'NORMAL',NULL,'å¤©æ´¥é£Žå‘³æ—©é¤',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1005,7638432224340229,'é¦„é¥¨','æ—©é¤',12.00,220,15,'NORMAL',NULL,'çš®è–„é¦…å«©ï¼Œæ±¤æ¸…å‘³é²œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1006,7638432224340229,'å®«ä¿é¸¡ä¸','å·èœ',38.00,450,15,'NORMAL',NULL,'ç»å…¸å·èœï¼Œé…¸ç”œå¾®è¾£ï¼Œé¸¡è‚‰å«©æ»‘',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1007,7638432224340229,'é±¼é¦™è‚‰ä¸','å·èœ',35.00,380,15,'NORMAL',NULL,'ä¼ ç»Ÿå·èœï¼Œé…¸ç”œå¼€èƒƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1008,7638432224340229,'éº»å©†è±†è…','å·èœ',28.00,320,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œä¸‹é¥­ç¥žå™¨',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1009,7638432224340229,'æ°´ç…®é±¼','å·èœ',68.00,520,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œé±¼è‚‰æ»‘å«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1010,7638432224340229,'å›žé”…è‚‰','å·èœ',42.00,580,15,'NORMAL',NULL,'å·èœä¹‹é¦–ï¼Œè‚¥è€Œä¸è…»',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1011,7638432224340229,'çº¢çƒ§è‚‰','å®¶å¸¸èœ',45.00,580,15,'NORMAL',NULL,'è‰²æ³½çº¢äº®ï¼Œè‚¥è€Œä¸è…»ï¼Œå…¥å£å³åŒ–',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1012,7638432224340229,'ç³–é†‹æŽ’éª¨','å®¶å¸¸èœ',52.00,520,15,'NORMAL',NULL,'é…¸ç”œå¯å£ï¼Œå¤–é…¥é‡Œå«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1013,7638432224340229,'æ¸…è’¸é²ˆé±¼','ç²¤èœ',78.00,280,15,'NORMAL',NULL,'åŽŸæ±åŽŸå‘³ï¼Œè‚‰è´¨é²œå«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1014,7638432224340229,'ç™½åˆ‡é¸¡','ç²¤èœ',58.00,350,15,'NORMAL',NULL,'çš®çˆ½è‚‰æ»‘ï¼ŒåŽŸæ±åŽŸå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1015,7638432224340229,'è’œè“‰è¥¿å…°èŠ±','ç´ èœ',22.00,120,15,'NORMAL',NULL,'æ¸…é¦™çˆ½è„†ï¼Œå¥åº·ç¾Žå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1016,7638432224340229,'ç¾Šè‚‰ç«é”…','ç«é”…',128.00,800,15,'NORMAL',NULL,'æš–èº«é©±å¯’ï¼Œæ»‹è¡¥å…»ç”Ÿ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1017,7638432224340229,'ç‰›è‚‰ç«é”…','ç«é”…',118.00,750,15,'NORMAL',NULL,'è‚‰è´¨é²œå«©ï¼Œæ±¤åº•æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1018,7638432224340229,'ç•ªèŒ„é”…','ç«é”…',88.00,320,15,'NORMAL',NULL,'é…¸ç”œå¼€èƒƒï¼Œè€å°‘çš†å®œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1019,7638432224340229,'é¸¡è‚‰æ²™æ‹‰','è½»é£Ÿ',32.00,350,15,'NORMAL',NULL,'ä½Žè„‚é«˜è›‹ç™½ï¼Œå¥åº·è½»é£Ÿ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1020,7638432224340229,'ç‰›è‚‰æ³¢å¥‡é¥­','è½»é£Ÿ',38.00,420,15,'NORMAL',NULL,'è¥å…»å‡è¡¡ï¼Œé¥±è…¹æ„Ÿå¼º',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1021,7638432224340229,'è”¬èœæ²™æ‹‰','è½»é£Ÿ',25.00,150,15,'NORMAL',NULL,'æ¸…çˆ½å¥åº·ï¼Œå‡è„‚å¿…å¤‡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1022,7638432224340229,'å…¨éº¦ä¸‰æ˜Žæ²»','è½»é£Ÿ',28.00,280,15,'NORMAL',NULL,'ç²—ç²®ç»†ä½œï¼Œè¥å…»å¥åº·',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1023,7638432224340229,'è—œéº¦é¥­','è½»é£Ÿ',35.00,320,15,'NORMAL',NULL,'è¶…çº§é£Ÿç‰©ï¼Œè¥å…»æ»¡åˆ†',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1024,7638432224340229,'è¥¿æ¹–ç‰›è‚‰ç¾¹','æ±¤ç±»',28.00,180,15,'NORMAL',NULL,'æ­å·žåèœï¼Œé²œé¦™æ»‘å«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1025,7638432224340229,'é…¸è¾£æ±¤','æ±¤ç±»',18.00,120,15,'NORMAL',NULL,'å¼€èƒƒçˆ½å£ï¼Œé…¸è¾£é²œé¦™',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1026,7638432224340229,'å†¬ç“œæŽ’éª¨æ±¤','æ±¤ç±»',32.00,220,15,'NORMAL',NULL,'æ¸…æ·¡æ»‹è¡¥ï¼Œæ¸…çƒ­è§£æš‘',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1027,7638432224340229,'ç´«èœè›‹èŠ±æ±¤','æ±¤ç±»',12.00,80,15,'NORMAL',NULL,'ç®€å•å¿«æ·ï¼Œè¥å…»ä¸°å¯Œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1028,7638432224340229,'èŒè‡æ±¤','æ±¤ç±»',38.00,160,15,'NORMAL',NULL,'é²œç¾Žé†‡åŽšï¼Œå¢žå¼ºå…ç–«',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1029,7638432224340229,'æ‰¬å·žç‚’é¥­','ä¸»é£Ÿ',22.00,420,15,'NORMAL',NULL,'ç²’ç²’åˆ†æ˜Žï¼Œé¦™å‘³æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1030,7638432224340229,'å¹¿å¼è…Šè‚ ç…²ä»”é¥­','ä¸»é£Ÿ',35.00,580,15,'NORMAL',NULL,'é”…å·´é¦™è„†ï¼Œè…Šå‘³æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1031,7638432224340229,'ç‰›è‚‰é¢','ä¸»é£Ÿ',26.00,520,15,'NORMAL',NULL,'æ±¤æµ“é¢åŠ²ï¼Œç‰›è‚‰è½¯çƒ‚',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1032,7638432224340229,'é‡åº†å°é¢','ä¸»é£Ÿ',18.00,480,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œé‡åº†é£Žå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1033,7638432224340229,'æ—¥å¼æ‹‰é¢','ä¸»é£Ÿ',38.00,550,15,'NORMAL',NULL,'æ±¤åº•æµ“éƒï¼Œé¢æ¡åŠ²é“',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1034,7638432224340229,'ææ‹‰ç±³è‹','ç”œç‚¹',32.00,380,15,'NORMAL',NULL,'æ„å¤§åˆ©ç»å…¸ç”œç‚¹',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1035,7638432224340229,'èŠ’æžœå¸ƒä¸','ç”œç‚¹',18.00,220,15,'NORMAL',NULL,'å£æ„Ÿé¡ºæ»‘ï¼ŒèŠ’æžœé¦™ç”œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1036,7638432224340229,'çº¢è±†è–ç±³æ±¤','ç”œç‚¹',15.00,150,15,'NORMAL',NULL,'ç¥›æ¹¿æŽ’æ¯’ï¼Œå…»é¢œç¾Žç™½',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1037,7638432224340229,'å†°ç³–é›ªæ¢¨','ç”œç‚¹',16.00,120,15,'NORMAL',NULL,'æ¶¦è‚ºæ­¢å’³ï¼Œæ¸…çƒ­è§£æ¯’',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1038,7638432224340229,'åŒçš®å¥¶','ç”œç‚¹',18.00,200,15,'NORMAL',NULL,'é¦™ç”œå«©æ»‘ï¼Œå¹¿ä¸œåç‚¹',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1039,7638432224340229,'å†°çˆ½æŸ æª¬æ°´','é¥®å“',8.00,50,15,'NORMAL',NULL,'æ¸…çˆ½è§£æ¸´ï¼Œå¤æ—¥å¿…å¤‡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1040,7638432224340229,'çŽ°ç£¨è±†æµ†','é¥®å“',10.00,120,15,'NORMAL',NULL,'æµ“éƒé¦™é†‡ï¼Œè¥å…»å¥åº·',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1041,7638432224340229,'é²œæ¦¨æ©™æ±','é¥®å“',18.00,140,15,'NORMAL',NULL,'ç»´ç”Ÿç´ Cæ»¡æ»¡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1042,7638432224340229,'çç å¥¶èŒ¶','é¥®å“',22.00,380,15,'NORMAL',NULL,'Qå¼¹çˆ½æ»‘ï¼Œå¥¶é¦™æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(1043,7638432224340229,'èŠèŠ±èŒ¶','é¥®å“',12.00,30,15,'NORMAL',NULL,'æ¸…çƒ­é™ç«ï¼Œæ¸…è‚æ˜Žç›®',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:12:13','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008574121167847426,7638432224340229,'123','主食',120.00,0,15,'NORMAL','{\"optional\": [\"1\", \"2\", \"3\", \"22\", \"333\"], \"mandatory\": [\"111\", \"222\"]}',NULL,NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-07 00:19:22','2026-01-31 20:40:30',0,0,0,NULL,NULL,0),(2008733197583519745,7638432224340229,'dads','主食',0.00,0,15,'NORMAL','{\"optional\": [\"s\", \"d\", \"a\"], \"mandatory\": [\"a\", \"s\", \"d\"]}',NULL,NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-07 10:51:29','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008733624953769986,7638432224340229,'ds','主食',0.00,0,15,'NORMAL','{\"optional\": [\"s\", \"d\"], \"mandatory\": [\"a\", \"s\"]}',NULL,NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-07 10:53:11','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711489,7638432224340229,'31','主食',12.00,0,15,'NORMAL','{\"optional\": [\"1\", \"2\"], \"mandatory\": [\"21\", \"2\", \"3\"]}',NULL,NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-07 11:08:03','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711490,7638432224340229,'çš®è›‹ç˜¦è‚‰ç²¥','æ—©é¤',12.00,180,15,'NORMAL',NULL,'ç»å…¸ç²¤å¼æ—©é¤ç²¥å“ï¼Œè¥å…»æš–èƒƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711491,7638432224340229,'è±†æµ†æ²¹æ¡','æ—©é¤',8.00,350,15,'NORMAL',NULL,'ä¼ ç»Ÿä¸­å¼æ—©é¤æ­é…',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711492,7638432224340229,'å°ç¬¼åŒ…','æ—©é¤',15.00,280,15,'NORMAL',NULL,'çš®è–„é¦…å¤šï¼Œæ±æ°´ä¸°å¯Œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711493,7638432224340229,'ç…Žé¥¼æžœå­','æ—©é¤',10.00,320,15,'NORMAL',NULL,'å¤©æ´¥é£Žå‘³æ—©é¤',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711494,7638432224340229,'é¦„é¥¨','æ—©é¤',12.00,220,15,'NORMAL',NULL,'çš®è–„é¦…å«©ï¼Œæ±¤æ¸…å‘³é²œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711495,7638432224340229,'å®«ä¿é¸¡ä¸','å·èœ',38.00,450,15,'NORMAL',NULL,'ç»å…¸å·èœï¼Œé…¸ç”œå¾®è¾£ï¼Œé¸¡è‚‰å«©æ»‘',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711496,7638432224340229,'é±¼é¦™è‚‰ä¸','å·èœ',35.00,380,15,'NORMAL',NULL,'ä¼ ç»Ÿå·èœï¼Œé…¸ç”œå¼€èƒƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711497,7638432224340229,'éº»å©†è±†è…','å·èœ',28.00,320,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œä¸‹é¥­ç¥žå™¨',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711498,7638432224340229,'æ°´ç…®é±¼','å·èœ',68.00,520,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œé±¼è‚‰æ»‘å«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711499,7638432224340229,'å›žé”…è‚‰','å·èœ',42.00,580,15,'NORMAL',NULL,'å·èœä¹‹é¦–ï¼Œè‚¥è€Œä¸è…»',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711500,7638432224340229,'çº¢çƒ§è‚‰','å®¶å¸¸èœ',45.00,580,15,'NORMAL',NULL,'è‰²æ³½çº¢äº®ï¼Œè‚¥è€Œä¸è…»ï¼Œå…¥å£å³åŒ–',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711501,7638432224340229,'ç³–é†‹æŽ’éª¨','å®¶å¸¸èœ',52.00,520,15,'NORMAL',NULL,'é…¸ç”œå¯å£ï¼Œå¤–é…¥é‡Œå«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711502,7638432224340229,'æ¸…è’¸é²ˆé±¼','ç²¤èœ',78.00,280,15,'NORMAL',NULL,'åŽŸæ±åŽŸå‘³ï¼Œè‚‰è´¨é²œå«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711503,7638432224340229,'ç™½åˆ‡é¸¡','ç²¤èœ',58.00,350,15,'NORMAL',NULL,'çš®çˆ½è‚‰æ»‘ï¼ŒåŽŸæ±åŽŸå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711504,7638432224340229,'è’œè“‰è¥¿å…°èŠ±','ç´ èœ',22.00,120,15,'NORMAL',NULL,'æ¸…é¦™çˆ½è„†ï¼Œå¥åº·ç¾Žå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711505,7638432224340229,'ç¾Šè‚‰ç«é”…','ç«é”…',128.00,800,15,'NORMAL',NULL,'æš–èº«é©±å¯’ï¼Œæ»‹è¡¥å…»ç”Ÿ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711506,7638432224340229,'ç‰›è‚‰ç«é”…','ç«é”…',118.00,750,15,'NORMAL',NULL,'è‚‰è´¨é²œå«©ï¼Œæ±¤åº•æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711507,7638432224340229,'ç•ªèŒ„é”…','ç«é”…',88.00,320,15,'NORMAL',NULL,'é…¸ç”œå¼€èƒƒï¼Œè€å°‘çš†å®œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711508,7638432224340229,'é¸¡è‚‰æ²™æ‹‰','è½»é£Ÿ',32.00,350,15,'NORMAL',NULL,'ä½Žè„‚é«˜è›‹ç™½ï¼Œå¥åº·è½»é£Ÿ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711509,7638432224340229,'ç‰›è‚‰æ³¢å¥‡é¥­','è½»é£Ÿ',38.00,420,15,'NORMAL',NULL,'è¥å…»å‡è¡¡ï¼Œé¥±è…¹æ„Ÿå¼º',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711510,7638432224340229,'è”¬èœæ²™æ‹‰','è½»é£Ÿ',25.00,150,15,'NORMAL',NULL,'æ¸…çˆ½å¥åº·ï¼Œå‡è„‚å¿…å¤‡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711511,7638432224340229,'å…¨éº¦ä¸‰æ˜Žæ²»','è½»é£Ÿ',28.00,280,15,'NORMAL',NULL,'ç²—ç²®ç»†ä½œï¼Œè¥å…»å¥åº·',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711512,7638432224340229,'è—œéº¦é¥­','è½»é£Ÿ',35.00,320,15,'NORMAL',NULL,'è¶…çº§é£Ÿç‰©ï¼Œè¥å…»æ»¡åˆ†',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711513,7638432224340229,'è¥¿æ¹–ç‰›è‚‰ç¾¹','æ±¤ç±»',28.00,180,15,'NORMAL',NULL,'æ­å·žåèœï¼Œé²œé¦™æ»‘å«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711514,7638432224340229,'é…¸è¾£æ±¤','æ±¤ç±»',18.00,120,15,'NORMAL',NULL,'å¼€èƒƒçˆ½å£ï¼Œé…¸è¾£é²œé¦™',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711515,7638432224340229,'å†¬ç“œæŽ’éª¨æ±¤','æ±¤ç±»',32.00,220,15,'NORMAL',NULL,'æ¸…æ·¡æ»‹è¡¥ï¼Œæ¸…çƒ­è§£æš‘',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711516,7638432224340229,'ç´«èœè›‹èŠ±æ±¤','æ±¤ç±»',12.00,80,15,'NORMAL',NULL,'ç®€å•å¿«æ·ï¼Œè¥å…»ä¸°å¯Œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711517,7638432224340229,'èŒè‡æ±¤','æ±¤ç±»',38.00,160,15,'NORMAL',NULL,'é²œç¾Žé†‡åŽšï¼Œå¢žå¼ºå…ç–«',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711518,7638432224340229,'æ‰¬å·žç‚’é¥­','ä¸»é£Ÿ',22.00,420,15,'NORMAL',NULL,'ç²’ç²’åˆ†æ˜Žï¼Œé¦™å‘³æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711519,7638432224340229,'å¹¿å¼è…Šè‚ ç…²ä»”é¥­','ä¸»é£Ÿ',35.00,580,15,'NORMAL',NULL,'é”…å·´é¦™è„†ï¼Œè…Šå‘³æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711520,7638432224340229,'ç‰›è‚‰é¢','ä¸»é£Ÿ',26.00,520,15,'NORMAL',NULL,'æ±¤æµ“é¢åŠ²ï¼Œç‰›è‚‰è½¯çƒ‚',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711521,7638432224340229,'é‡åº†å°é¢','ä¸»é£Ÿ',18.00,480,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œé‡åº†é£Žå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711522,7638432224340229,'æ—¥å¼æ‹‰é¢','ä¸»é£Ÿ',38.00,550,15,'NORMAL',NULL,'æ±¤åº•æµ“éƒï¼Œé¢æ¡åŠ²é“',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711523,7638432224340229,'ææ‹‰ç±³è‹','ç”œç‚¹',32.00,380,15,'NORMAL',NULL,'æ„å¤§åˆ©ç»å…¸ç”œç‚¹',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711524,7638432224340229,'èŠ’æžœå¸ƒä¸','ç”œç‚¹',18.00,220,15,'NORMAL',NULL,'å£æ„Ÿé¡ºæ»‘ï¼ŒèŠ’æžœé¦™ç”œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711525,7638432224340229,'çº¢è±†è–ç±³æ±¤','ç”œç‚¹',15.00,150,15,'NORMAL',NULL,'ç¥›æ¹¿æŽ’æ¯’ï¼Œå…»é¢œç¾Žç™½',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711526,7638432224340229,'å†°ç³–é›ªæ¢¨','ç”œç‚¹',16.00,120,15,'NORMAL',NULL,'æ¶¦è‚ºæ­¢å’³ï¼Œæ¸…çƒ­è§£æ¯’',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711527,7638432224340229,'åŒçš®å¥¶','ç”œç‚¹',18.00,200,15,'NORMAL',NULL,'é¦™ç”œå«©æ»‘ï¼Œå¹¿ä¸œåç‚¹',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711528,7638432224340229,'å†°çˆ½æŸ æª¬æ°´','é¥®å“',8.00,50,15,'NORMAL',NULL,'æ¸…çˆ½è§£æ¸´ï¼Œå¤æ—¥å¿…å¤‡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711529,7638432224340229,'çŽ°ç£¨è±†æµ†','é¥®å“',10.00,120,15,'NORMAL',NULL,'æµ“éƒé¦™é†‡ï¼Œè¥å…»å¥åº·',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711530,7638432224340229,'é²œæ¦¨æ©™æ±','é¥®å“',18.00,140,15,'NORMAL',NULL,'ç»´ç”Ÿç´ Cæ»¡æ»¡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711531,7638432224340229,'çç å¥¶èŒ¶','é¥®å“',22.00,380,15,'NORMAL',NULL,'Qå¼¹çˆ½æ»‘ï¼Œå¥¶é¦™æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711532,7638432224340229,'èŠèŠ±èŒ¶','é¥®å“',12.00,30,15,'NORMAL',NULL,'æ¸…çƒ­é™ç«ï¼Œæ¸…è‚æ˜Žç›®',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:10:09','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711533,7638432224340229,'çš®è›‹ç˜¦è‚‰ç²¥','æ—©é¤',12.00,180,15,'NORMAL',NULL,'ç»å…¸ç²¤å¼æ—©é¤ç²¥å“ï¼Œè¥å…»æš–èƒƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711534,7638432224340229,'è±†æµ†æ²¹æ¡','æ—©é¤',8.00,350,15,'NORMAL',NULL,'ä¼ ç»Ÿä¸­å¼æ—©é¤æ­é…',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711535,7638432224340229,'å°ç¬¼åŒ…','æ—©é¤',15.00,280,15,'NORMAL',NULL,'çš®è–„é¦…å¤šï¼Œæ±æ°´ä¸°å¯Œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711536,7638432224340229,'ç…Žé¥¼æžœå­','æ—©é¤',10.00,320,15,'NORMAL',NULL,'å¤©æ´¥é£Žå‘³æ—©é¤',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711537,7638432224340229,'é¦„é¥¨','æ—©é¤',12.00,220,15,'NORMAL',NULL,'çš®è–„é¦…å«©ï¼Œæ±¤æ¸…å‘³é²œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711538,7638432224340229,'å®«ä¿é¸¡ä¸','å·èœ',38.00,450,15,'NORMAL',NULL,'ç»å…¸å·èœï¼Œé…¸ç”œå¾®è¾£ï¼Œé¸¡è‚‰å«©æ»‘',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711539,7638432224340229,'é±¼é¦™è‚‰ä¸','å·èœ',35.00,380,15,'NORMAL',NULL,'ä¼ ç»Ÿå·èœï¼Œé…¸ç”œå¼€èƒƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711540,7638432224340229,'éº»å©†è±†è…','å·èœ',28.00,320,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œä¸‹é¥­ç¥žå™¨',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711541,7638432224340229,'æ°´ç…®é±¼','å·èœ',68.00,520,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œé±¼è‚‰æ»‘å«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711542,7638432224340229,'å›žé”…è‚‰','å·èœ',42.00,580,15,'NORMAL',NULL,'å·èœä¹‹é¦–ï¼Œè‚¥è€Œä¸è…»',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711543,7638432224340229,'çº¢çƒ§è‚‰','å®¶å¸¸èœ',45.00,580,15,'NORMAL',NULL,'è‰²æ³½çº¢äº®ï¼Œè‚¥è€Œä¸è…»ï¼Œå…¥å£å³åŒ–',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711544,7638432224340229,'ç³–é†‹æŽ’éª¨','å®¶å¸¸èœ',52.00,520,15,'NORMAL',NULL,'é…¸ç”œå¯å£ï¼Œå¤–é…¥é‡Œå«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711545,7638432224340229,'æ¸…è’¸é²ˆé±¼','ç²¤èœ',78.00,280,15,'NORMAL',NULL,'åŽŸæ±åŽŸå‘³ï¼Œè‚‰è´¨é²œå«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711546,7638432224340229,'ç™½åˆ‡é¸¡','ç²¤èœ',58.00,350,15,'NORMAL',NULL,'çš®çˆ½è‚‰æ»‘ï¼ŒåŽŸæ±åŽŸå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711547,7638432224340229,'è’œè“‰è¥¿å…°èŠ±','ç´ èœ',22.00,120,15,'NORMAL',NULL,'æ¸…é¦™çˆ½è„†ï¼Œå¥åº·ç¾Žå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711548,7638432224340229,'ç¾Šè‚‰ç«é”…','ç«é”…',128.00,800,15,'NORMAL',NULL,'æš–èº«é©±å¯’ï¼Œæ»‹è¡¥å…»ç”Ÿ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711549,7638432224340229,'ç‰›è‚‰ç«é”…','ç«é”…',118.00,750,15,'NORMAL',NULL,'è‚‰è´¨é²œå«©ï¼Œæ±¤åº•æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711550,7638432224340229,'ç•ªèŒ„é”…','ç«é”…',88.00,320,15,'NORMAL',NULL,'é…¸ç”œå¼€èƒƒï¼Œè€å°‘çš†å®œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711551,7638432224340229,'é¸¡è‚‰æ²™æ‹‰','è½»é£Ÿ',32.00,350,15,'NORMAL',NULL,'ä½Žè„‚é«˜è›‹ç™½ï¼Œå¥åº·è½»é£Ÿ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711552,7638432224340229,'ç‰›è‚‰æ³¢å¥‡é¥­','è½»é£Ÿ',38.00,420,15,'NORMAL',NULL,'è¥å…»å‡è¡¡ï¼Œé¥±è…¹æ„Ÿå¼º',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711553,7638432224340229,'è”¬èœæ²™æ‹‰','è½»é£Ÿ',25.00,150,15,'NORMAL',NULL,'æ¸…çˆ½å¥åº·ï¼Œå‡è„‚å¿…å¤‡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711554,7638432224340229,'å…¨éº¦ä¸‰æ˜Žæ²»','è½»é£Ÿ',28.00,280,15,'NORMAL',NULL,'ç²—ç²®ç»†ä½œï¼Œè¥å…»å¥åº·',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711555,7638432224340229,'è—œéº¦é¥­','è½»é£Ÿ',35.00,320,15,'NORMAL',NULL,'è¶…çº§é£Ÿç‰©ï¼Œè¥å…»æ»¡åˆ†',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711556,7638432224340229,'è¥¿æ¹–ç‰›è‚‰ç¾¹','æ±¤ç±»',28.00,180,15,'NORMAL',NULL,'æ­å·žåèœï¼Œé²œé¦™æ»‘å«©',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711557,7638432224340229,'é…¸è¾£æ±¤','æ±¤ç±»',18.00,120,15,'NORMAL',NULL,'å¼€èƒƒçˆ½å£ï¼Œé…¸è¾£é²œé¦™',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711558,7638432224340229,'å†¬ç“œæŽ’éª¨æ±¤','æ±¤ç±»',32.00,220,15,'NORMAL',NULL,'æ¸…æ·¡æ»‹è¡¥ï¼Œæ¸…çƒ­è§£æš‘',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711559,7638432224340229,'ç´«èœè›‹èŠ±æ±¤','æ±¤ç±»',12.00,80,15,'NORMAL',NULL,'ç®€å•å¿«æ·ï¼Œè¥å…»ä¸°å¯Œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711560,7638432224340229,'èŒè‡æ±¤','æ±¤ç±»',38.00,160,15,'NORMAL',NULL,'é²œç¾Žé†‡åŽšï¼Œå¢žå¼ºå…ç–«',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711561,7638432224340229,'æ‰¬å·žç‚’é¥­','ä¸»é£Ÿ',22.00,420,15,'NORMAL',NULL,'ç²’ç²’åˆ†æ˜Žï¼Œé¦™å‘³æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711562,7638432224340229,'å¹¿å¼è…Šè‚ ç…²ä»”é¥­','ä¸»é£Ÿ',35.00,580,15,'NORMAL',NULL,'é”…å·´é¦™è„†ï¼Œè…Šå‘³æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711563,7638432224340229,'ç‰›è‚‰é¢','ä¸»é£Ÿ',26.00,520,15,'NORMAL',NULL,'æ±¤æµ“é¢åŠ²ï¼Œç‰›è‚‰è½¯çƒ‚',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711564,7638432224340229,'é‡åº†å°é¢','ä¸»é£Ÿ',18.00,480,15,'NORMAL',NULL,'éº»è¾£é²œé¦™ï¼Œé‡åº†é£Žå‘³',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711565,7638432224340229,'æ—¥å¼æ‹‰é¢','ä¸»é£Ÿ',38.00,550,15,'NORMAL',NULL,'æ±¤åº•æµ“éƒï¼Œé¢æ¡åŠ²é“',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711566,7638432224340229,'ææ‹‰ç±³è‹','ç”œç‚¹',32.00,380,15,'NORMAL',NULL,'æ„å¤§åˆ©ç»å…¸ç”œç‚¹',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711567,7638432224340229,'èŠ’æžœå¸ƒä¸','ç”œç‚¹',18.00,220,15,'NORMAL',NULL,'å£æ„Ÿé¡ºæ»‘ï¼ŒèŠ’æžœé¦™ç”œ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711568,7638432224340229,'çº¢è±†è–ç±³æ±¤','ç”œç‚¹',15.00,150,15,'NORMAL',NULL,'ç¥›æ¹¿æŽ’æ¯’ï¼Œå…»é¢œç¾Žç™½',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711569,7638432224340229,'å†°ç³–é›ªæ¢¨','ç”œç‚¹',16.00,120,15,'NORMAL',NULL,'æ¶¦è‚ºæ­¢å’³ï¼Œæ¸…çƒ­è§£æ¯’',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711570,7638432224340229,'åŒçš®å¥¶','ç”œç‚¹',18.00,200,15,'NORMAL',NULL,'é¦™ç”œå«©æ»‘ï¼Œå¹¿ä¸œåç‚¹',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711571,7638432224340229,'å†°çˆ½æŸ æª¬æ°´','é¥®å“',8.00,50,15,'NORMAL',NULL,'æ¸…çˆ½è§£æ¸´ï¼Œå¤æ—¥å¿…å¤‡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711572,7638432224340229,'çŽ°ç£¨è±†æµ†','é¥®å“',10.00,120,15,'NORMAL',NULL,'æµ“éƒé¦™é†‡ï¼Œè¥å…»å¥åº·',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711573,7638432224340229,'é²œæ¦¨æ©™æ±','é¥®å“',18.00,140,15,'NORMAL',NULL,'ç»´ç”Ÿç´ Cæ»¡æ»¡',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711574,7638432224340229,'çç å¥¶èŒ¶','é¥®å“',22.00,380,15,'NORMAL',NULL,'Qå¼¹çˆ½æ»‘ï¼Œå¥¶é¦™æµ“éƒ',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711575,7638432224340229,'èŠèŠ±èŒ¶','é¥®å“',12.00,30,15,'NORMAL',NULL,'æ¸…çƒ­é™ç«ï¼Œæ¸…è‚æ˜Žç›®',NULL,NULL,1,'APPROVED','ç³»ç»Ÿé»˜è®¤é€šè¿‡ï¼ˆåŽ†å²æ•°æ®ï¼‰','2026-01-31 20:40:30',NULL,'2026-01-25 09:11:08','2026-01-31 20:40:30',0,0,0,NULL,NULL,100),(2008737368059711576,7638432224340229,'å®«ä¿é¸¡ä¸','å·èœ',38.00,320,15,'NORMAL',NULL,'ç»å…¸å·èœï¼Œé…¸ç”œå¾®è¾£ï¼Œé¸¡è‚‰å«©æ»‘',NULL,NULL,1,'APPROVED','','2026-01-31 21:02:22',NULL,'2026-01-31 21:00:21','2026-01-31 21:00:21',0,0,0,4.50,NULL,100),(2008737368059711577,7638432224340229,'é±¼é¦™è‚‰ä¸','å·èœ',32.00,280,12,'NORMAL',NULL,'æ— éª¨çŒªè‚‰ä¸ï¼Œé…æœ¨è€³ç¬‹ä¸ï¼Œé…¸ç”œå£',NULL,NULL,1,'PENDING',NULL,NULL,NULL,'2026-01-31 21:00:21','2026-01-31 21:00:21',0,0,0,4.30,NULL,100),(2008737368059711578,7638432224340229,'æ°´ç…®é±¼','å·èœ',68.00,450,25,'NORMAL',NULL,'è‰é±¼ç‰‡ï¼Œè±†èŠ½é…èœï¼Œéº»è¾£é²œé¦™',NULL,NULL,1,'PENDING',NULL,NULL,NULL,'2026-01-31 21:00:21','2026-01-31 21:00:21',0,0,0,4.70,NULL,50),(2008737368059711579,7638432224340229,'éº»å©†è±†è…','å·èœ',22.00,180,10,'NORMAL',NULL,'å«©è±†è…é…è‚‰æœ«ï¼Œéº»è¾£å‘³æµ“',NULL,NULL,1,'PENDING',NULL,NULL,NULL,'2026-01-31 21:00:21','2026-01-31 21:00:21',0,0,0,4.20,NULL,200),(2008737368059711580,7638432224340229,'å›žé”…è‚‰','å·èœ',42.00,380,18,'NORMAL',NULL,'äº”èŠ±è‚‰ç‰‡é…é’æ¤’ï¼Œå’¸é²œå¾®è¾£',NULL,NULL,1,'PENDING',NULL,NULL,NULL,'2026-01-31 21:00:21','2026-01-31 21:00:21',0,0,0,4.60,NULL,80);
/*!40000 ALTER TABLE `t_dish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_dish_step_config`
--

DROP TABLE IF EXISTS `t_dish_step_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_dish_step_config` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `merchant_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å•†å®¶ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“IDï¼ˆä¸ºNULLè¡¨ç¤ºå…¨å±€é»˜è®¤é…ç½®ï¼‰',
  `step_code` int NOT NULL COMMENT 'æ­¥éª¤ä»£ç ',
  `step_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ­¥éª¤åç§°',
  `step_order` int NOT NULL COMMENT 'æ­¥éª¤é¡ºåº',
  `estimated_minutes` int DEFAULT NULL COMMENT 'é¢„è®¡è€—æ—¶ï¼ˆåˆ†é’Ÿï¼‰',
  `is_enabled` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨ï¼š1-å¯ç”¨ 0-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_dish_step` (`merchant_id`,`dish_id`,`step_code`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_dish_id` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èœå“æ­¥éª¤é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_dish_step_config`
--

LOCK TABLES `t_dish_step_config` WRITE;
/*!40000 ALTER TABLE `t_dish_step_config` DISABLE KEYS */;
INSERT INTO `t_dish_step_config` VALUES ('236c30ba-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,0,'å¾…å¤‡èœ',1,0,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c5f7c-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,1,'å¤‡èœä¸­',2,10,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c6256-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,2,'é¢„å¤„ç†ä¸­',3,5,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c6332-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,3,'çƒ¹é¥ªä¸­',4,15,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c642c-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,4,'æ‘†ç›˜ä¸­',5,3,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c64fe-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,5,'å¾…ä¸Šèœ',6,0,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c659e-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,6,'å·²ä¸Šèœ',7,0,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c77f0-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,10,'åˆ¶ä½œä¸­',1,8,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c7980-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,11,'æ‰“åŒ…ä¸­',2,2,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c7a2a-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,12,'å¾…å‡ºé¤',3,0,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('236c7ac0-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,13,'å·²å‡ºé¤',4,0,1,'2026-01-30 22:08:23','2026-01-30 22:08:23'),('42d86f7c-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,0,'å¾…å¤‡èœ',1,0,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d87878-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,1,'å¤‡èœä¸­',2,10,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d87a6c-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,2,'é¢„å¤„ç†ä¸­',3,5,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d87d6e-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,3,'çƒ¹é¥ªä¸­',4,15,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d87f9e-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,4,'æ‘†ç›˜ä¸­',5,3,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d881ba-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,5,'å¾…ä¸Šèœ',6,0,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d8839a-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,6,'å·²ä¸Šèœ',7,0,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d893da-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,10,'åˆ¶ä½œä¸­',1,8,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d89650-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,11,'æ‰“åŒ…ä¸­',2,2,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d89754-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,12,'å¾…å‡ºé¤',3,0,1,'2026-01-30 22:09:15','2026-01-30 22:09:15'),('42d89826-fde5-11f0-9ddf-14f281b1','DEFAULT',NULL,13,'å·²å‡ºé¤',4,0,1,'2026-01-30 22:09:15','2026-01-30 22:09:15');
/*!40000 ALTER TABLE `t_dish_step_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_dish_step_history`
--

DROP TABLE IF EXISTS `t_dish_step_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_dish_step_history` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `order_dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è®¢å•èœå“ID',
  `order_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è®¢å•ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `old_step_status` int DEFAULT NULL COMMENT 'åŽŸæ­¥éª¤çŠ¶æ€',
  `new_step_status` int NOT NULL COMMENT 'æ–°æ­¥éª¤çŠ¶æ€',
  `operation_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œç±»åž‹ï¼šFORWARD-å‰è¿›, BACKWARD-å›žé€€, SKIP-è·³è¿‡',
  `operator_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œäººID',
  `operator_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œäººå§“å',
  `rollback_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å›žé€€åŽŸå› ',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `estimated_minutes` int DEFAULT NULL COMMENT 'é¢„è®¡å®Œæˆæ—¶é—´ï¼ˆåˆ†é’Ÿï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_order_dish_id` (`order_dish_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èœå“æ­¥éª¤åŽ†å²è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_dish_step_history`
--

LOCK TABLES `t_dish_step_history` WRITE;
/*!40000 ALTER TABLE `t_dish_step_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_dish_step_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_extraction_config`
--

DROP TABLE IF EXISTS `t_extraction_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_extraction_config` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `platform` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å¹³å°',
  `config_key` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'é…ç½®é”®',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT 'é…ç½®å€¼',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é…ç½®æè¿°',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_platform_key` (`platform`,`config_key`),
  KEY `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æå–é…ç½®è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_extraction_config`
--

LOCK TABLES `t_extraction_config` WRITE;
/*!40000 ALTER TABLE `t_extraction_config` DISABLE KEYS */;
INSERT INTO `t_extraction_config` VALUES ('1956bd211dc72be7eb32cea37c3e3913','DOUYIN','api_key','',1,'æŠ–éŸ³APIå¯†é’¥','2026-01-31 10:04:14','2026-01-31 10:04:14'),('1e2508563343c435d5e4e61e8928b9bf','XIAOHONGSHU','enabled','true',1,'æ˜¯å¦å¯ç”¨å°çº¢ä¹¦æå–','2026-01-31 10:04:14','2026-01-31 10:04:14'),('3ebd0d3b5564385d804cc6669524a085','SYSTEM','auto_publish','false',1,'æ˜¯å¦è‡ªåŠ¨å‘å¸ƒä¸ºé£Ÿè°±','2026-01-31 10:04:14','2026-01-31 10:04:14'),('4077b5a366acf462417e45d2c1089a5a','DOUYIN','enabled','true',1,'æ˜¯å¦å¯ç”¨æŠ–éŸ³æå–','2026-01-31 10:04:14','2026-01-31 10:04:14'),('4096d22a389c4d96f81b4c2dff42338a','BILIBILI','enabled','true',1,'æ˜¯å¦å¯ç”¨Bç«™æå–','2026-01-31 10:04:14','2026-01-31 10:04:14'),('491f270e4c3460d7534ce3b98a175265','SYSTEM','ocr_enabled','true',1,'æ˜¯å¦å¯ç”¨OCRè¯†åˆ«','2026-01-31 10:04:14','2026-01-31 10:04:14'),('c7019cb41205630a36c20789b24da345','WECHAT','enabled','true',1,'æ˜¯å¦å¯ç”¨å¾®ä¿¡æå–','2026-01-31 10:04:14','2026-01-31 10:04:14'),('e2e83eae532fbad9f4fab7773f431c6e','SYSTEM','nlp_enabled','true',1,'æ˜¯å¦å¯ç”¨NLPåˆ†æž','2026-01-31 10:04:14','2026-01-31 10:04:14');
/*!40000 ALTER TABLE `t_extraction_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_extraction_task`
--

DROP TABLE IF EXISTS `t_extraction_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_extraction_task` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `source_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å†…å®¹æºID',
  `task_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä»»åŠ¡ç±»åž‹ï¼šOCR-å›¾ç‰‡è¯†åˆ«, NLP-æ–‡æœ¬åˆ†æž, VIDEO-è§†é¢‘åˆ†æž',
  `task_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT 'ä»»åŠ¡çŠ¶æ€ï¼šPENDING-å¾…å¤„ç†, PROCESSING-å¤„ç†ä¸­, SUCCESS-æˆåŠŸ, FAILED-å¤±è´¥',
  `retry_count` int DEFAULT '0' COMMENT 'é‡è¯•æ¬¡æ•°',
  `priority` int DEFAULT '0' COMMENT 'ä¼˜å…ˆçº§ï¼ˆæ•°å­—è¶Šå¤§è¶Šä¼˜å…ˆï¼‰',
  `start_time` datetime DEFAULT NULL COMMENT 'å¼€å§‹æ—¶é—´',
  `end_time` datetime DEFAULT NULL COMMENT 'ç»“æŸæ—¶é—´',
  `result_data` text COLLATE utf8mb4_unicode_ci COMMENT 'ç»“æžœæ•°æ®ï¼ˆJSONæ ¼å¼ï¼‰',
  `error_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'é”™è¯¯ä¿¡æ¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_priority` (`priority`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æå–ä»»åŠ¡è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_extraction_task`
--

LOCK TABLES `t_extraction_task` WRITE;
/*!40000 ALTER TABLE `t_extraction_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_extraction_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_festival`
--

DROP TABLE IF EXISTS `t_festival`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_festival` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `festival_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èŠ‚æ—¥åç§°',
  `festival_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èŠ‚æ—¥ç±»åž‹ï¼šTRADITIONAL-ä¼ ç»ŸèŠ‚æ—¥, WESTERN-è¥¿æ–¹èŠ‚æ—¥, SEASONAL-å­£èŠ‚æ€§, CUSTOM-è‡ªå®šä¹‰',
  `festival_date` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èŠ‚æ—¥æ—¥æœŸï¼ˆMM-ddæ ¼å¼ï¼Œå¦‚01-01ï¼‰',
  `year` int DEFAULT NULL COMMENT 'å¹´ä»½ï¼ˆNULLè¡¨ç¤ºæ¯å¹´é‡å¤ï¼‰',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èŠ‚æ—¥æè¿°',
  `icon` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èŠ‚æ—¥å›¾æ ‡',
  `background_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èƒŒæ™¯å›¾ç‰‡',
  `theme_color` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä¸»é¢˜é¢œè‰²',
  `start_date` date DEFAULT NULL COMMENT 'å¼€å§‹æ—¥æœŸï¼ˆç”¨äºŽå­£èŠ‚æ€§ï¼‰',
  `end_date` date DEFAULT NULL COMMENT 'ç»“æŸæ—¥æœŸï¼ˆç”¨äºŽå­£èŠ‚æ€§ï¼‰',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºæƒé‡',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_festival_type` (`festival_type`),
  KEY `idx_festival_date` (`festival_date`),
  KEY `idx_start_end_date` (`start_date`,`end_date`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èŠ‚æ—¥è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_festival`
--

LOCK TABLES `t_festival` WRITE;
/*!40000 ALTER TABLE `t_festival` DISABLE KEYS */;
INSERT INTO `t_festival` VALUES ('019db6463a515dadf5327f3bfc788ff5','ç«¯åˆèŠ‚','TRADITIONAL','05-05',NULL,'ç«¯åˆä½³èŠ‚ï¼Œç²½å­é£˜é¦™','ðŸš£',NULL,'#4CAF50',NULL,NULL,1,95,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('054ff87fc0100ef6485e30e1c8698c71','ä¸­ç§‹èŠ‚','TRADITIONAL','08-15',NULL,'æœˆåœ†äººå›¢åœ†ï¼Œæœˆé¥¼å®´','ðŸ¥®',NULL,'#FFD700',NULL,NULL,1,98,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('1c4fca95776b8368eb1970e986e4bb13','æ„Ÿæ©èŠ‚','WESTERN','11-28',NULL,'æ„Ÿæ©ç››å®´','ðŸ¦ƒ',NULL,'#D2691E',NULL,NULL,1,82,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('1efc399ef9bec44f57b0408e23efc715','ä¸‡åœ£èŠ‚','WESTERN','10-31',NULL,'ä¸‡åœ£å¤œï¼Œæžæ€ªç¾Žé£Ÿ','ðŸŽƒ',NULL,'#FF8C00',NULL,NULL,1,70,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('344860f244ef3ed0dd3895f20bc6028b','æ¸…æ˜ŽèŠ‚','TRADITIONAL','04-04',NULL,'æ¸…æ˜Žæ—¶èŠ‚ï¼Œé’å›¢æ˜¥å®´','ðŸŒ¿',NULL,'#90EE90',NULL,NULL,1,80,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('45256668d0cdd869420c6de89a06ec1a','ç§‹å­£æ»‹è¡¥','SEASONAL',NULL,NULL,'é‡‘ç§‹æ—¶èŠ‚ï¼Œæ»‹è¡¥å…»ç”Ÿ','ðŸ‚',NULL,'#DAA520','2025-09-01','2025-11-30',1,74,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('6f6b91cfbc49f3716bb6a6686a2583c1','å…ƒå®µèŠ‚','TRADITIONAL','01-15',NULL,'å…ƒå®µä½³èŠ‚ï¼Œåƒæ±¤åœ†èµèŠ±ç¯','ðŸ®',NULL,'#FF6B6B',NULL,NULL,1,90,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('71427d61fc70a69d740767c8cc96d531','çˆ¶äº²èŠ‚','WESTERN','06-15',NULL,'è‡´æ•¬çˆ¶äº²ï¼Œè±ªè¿ˆç››å®´','ðŸ‘”',NULL,'#4169E1',NULL,NULL,1,91,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('8101671798d541916444e83735b10ef5','å…ƒæ—¦','WESTERN','01-01',NULL,'æ–°å¹´æ–°æ°”è±¡ï¼Œè·¨å¹´å®´','ðŸŽ†',NULL,'#FFD700',NULL,NULL,1,89,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('8a623cfd523b13b759bfef43fe9db3d1','æ„šäººèŠ‚','WESTERN','04-01',NULL,'è¶£å‘³èŠ‚æ—¥ï¼Œåˆ›æ„èœå“','ðŸ¤¡',NULL,'#FFA500',NULL,NULL,1,60,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('8a6371e78b055b9e433a022786389b13','æ˜¥å­£æ—¶ä»¤','SEASONAL',NULL,NULL,'æ˜¥æš–èŠ±å¼€ï¼Œå°é²œæ­£å½“æ—¶','ðŸŒ¸',NULL,'#98FB98','2025-03-01','2025-05-31',1,70,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('91a6bf01809170874581b9957a564722','é‡é˜³èŠ‚','TRADITIONAL','09-09',NULL,'ç™»é«˜èµç§‹ï¼Œæ•¬è€å®´','ðŸŒ¼',NULL,'#FF8C00',NULL,NULL,1,75,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('9700b0ea3c0aae2de573e416dce0e6f7','å†¬å­£æš–èº«','SEASONAL',NULL,NULL,'å¯’å†¬è…Šæœˆï¼Œæš–å¿ƒæš–èƒƒ','â„ï¸',NULL,'#4682B4','2025-12-01','2026-02-28',1,76,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('9c9b5886db2022d20d0b12eeccb50f11','ä¸ƒå¤•èŠ‚','TRADITIONAL','07-07',NULL,'ä¸­å›½æƒ…äººèŠ‚ï¼Œæµªæ¼«æ™šé¤','ðŸ’•',NULL,'#FF69B4',NULL,NULL,1,85,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('9f04c00717f78e18a28b3cf3a6fb3a5a','å¤å­£æ¶ˆæš‘','SEASONAL',NULL,NULL,'ç‚Žç‚Žå¤æ—¥ï¼Œæ¸…å‡‰ä¸€å¤','ðŸŒž',NULL,'#00CED1','2025-06-01','2025-08-31',1,72,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('af88b79dd7d5f5cdb91f4ac32f589071','åœ£è¯žèŠ‚','WESTERN','12-25',NULL,'å¹³å®‰å¤œç‹‚æ¬¢ï¼Œåœ£è¯žå¤§é¤','ðŸŽ„',NULL,'#FF0000',NULL,NULL,1,94,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('d3afc5f640a4eb46a6ed844407c28bab','å†¬è‡³','TRADITIONAL','12-21',NULL,'å†¬è‡³å¤§å¦‚å¹´ï¼Œé¥ºå­å®´','ðŸ¥Ÿ',NULL,'#87CEEB',NULL,NULL,1,88,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('ddca3b0429c0de02f9f066847caaf1ec','æƒ…äººèŠ‚','WESTERN','02-14',NULL,'æµªæ¼«æƒ…äººèŠ‚ï¼Œç”œèœœåŒäººé¤','ðŸŒ¹',NULL,'#FF1493',NULL,NULL,1,92,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('ee87db1b657181c98acf6e7dac21fcb4','æ˜¥èŠ‚','TRADITIONAL','01-01',NULL,'ä¸­å›½æœ€é‡è¦çš„ä¼ ç»ŸèŠ‚æ—¥ï¼Œå›¢åœ†é¥­æ˜¯æ ¸å¿ƒ','ðŸ§§',NULL,'#E60000',NULL,NULL,1,100,'2026-01-31 09:54:10','2026-01-31 09:54:10'),('fd82da724b5c67ccd5595d311b49bb03','æ¯äº²èŠ‚','WESTERN','05-12',NULL,'æ„Ÿæ©æ¯äº²ï¼Œæ¸©é¦¨å®¶å®´','ðŸ’',NULL,'#FF69B4',NULL,NULL,1,93,'2026-01-31 09:54:10','2026-01-31 09:54:10');
/*!40000 ALTER TABLE `t_festival` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_festival_dish_recommend`
--

DROP TABLE IF EXISTS `t_festival_dish_recommend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_festival_dish_recommend` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `festival_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èŠ‚æ—¥ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `recommend_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æŽ¨èç±»åž‹ï¼šMAIN-ä¸»æŽ¨, SECONDARY-æ¬¡æŽ¨, THEME-ä¸»é¢˜, SEASONAL-å­£èŠ‚',
  `recommend_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æŽ¨èç†ç”±',
  `position` int DEFAULT '0' COMMENT 'å±•ç¤ºä½ç½®ï¼ˆ0-é¦–é¡µ, 1-åˆ—è¡¨é¡¶, 2- bannerï¼‰',
  `priority` int DEFAULT '0' COMMENT 'ä¼˜å…ˆçº§ï¼ˆæ•°å­—è¶Šå¤§è¶Šé å‰ï¼‰',
  `click_count` int DEFAULT '0' COMMENT 'ç‚¹å‡»æ¬¡æ•°ç»Ÿè®¡',
  `order_count` int DEFAULT '0' COMMENT 'è®¢å•æ¬¡æ•°ç»Ÿè®¡',
  `is_active` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_festival_dish` (`festival_id`,`dish_id`),
  KEY `idx_festival_id` (`festival_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_recommend_type` (`recommend_type`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='èŠ‚æ—¥æŽ¨èèœå“å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_festival_dish_recommend`
--

LOCK TABLES `t_festival_dish_recommend` WRITE;
/*!40000 ALTER TABLE `t_festival_dish_recommend` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_festival_dish_recommend` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_group`
--

DROP TABLE IF EXISTS `t_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group` (
  `id` varchar(64) NOT NULL COMMENT 'ç¾¤ç»„ID',
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
-- Dumping data for table `t_group`
--

LOCK TABLES `t_group` WRITE;
/*!40000 ALTER TABLE `t_group` DISABLE KEYS */;
INSERT INTO `t_group` VALUES ('G5505379491825551','2',4590322501655448,NULL,'2026-01-26 23:58:34','2026-01-26 23:58:34'),('G9728057976243353','1',4590322501655448,NULL,'2026-01-26 23:45:24','2026-01-26 23:45:24');
/*!40000 ALTER TABLE `t_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_group_order`
--

DROP TABLE IF EXISTS `t_group_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_group_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ç¾¤è®¢å•ID',
  `initiator_id` varchar(64) NOT NULL,
  `merchant_id` varchar(64) DEFAULT NULL,
  `group_id` varchar(64) NOT NULL,
  `address_id` varchar(64) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL COMMENT 'è®¢å•å¤‡æ³¨',
  `status` tinyint DEFAULT '0' COMMENT 'è®¢å•çŠ¶æ€ï¼š0-å¾…æ”¯ä»˜ã€1-å¾…æŽ¥å•ã€2-å¤‡èœä¸­ã€3-çƒ¹é¥ªä¸­ã€4-å¾…ä¸Šèœã€5-å·²å®Œæˆã€6-å·²å–æ¶ˆ',
  `payment_mode` tinyint NOT NULL DEFAULT '0' COMMENT 'æ”¯ä»˜æ¨¡å¼: 0-ç»Ÿä¸€æ”¯ä»˜,1-ä¸ªäººå•ç‹¬æ”¯ä»˜',
  `add_dish_enabled` tinyint NOT NULL DEFAULT '1' COMMENT 'æ˜¯å¦å…è®¸åŠ èœ: 0-å¦,1-æ˜¯',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `total_amount` double DEFAULT '0' COMMENT 'è®¢å•æ€»é‡‘é¢',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_merchant_id` (`merchant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2016543345597222915 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¾¤è®¢å•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_group_order`
--

LOCK TABLES `t_group_order` WRITE;
/*!40000 ALTER TABLE `t_group_order` DISABLE KEYS */;
INSERT INTO `t_group_order` VALUES (2016324170874470402,'3384650106421960',NULL,'G5505379491825551',NULL,NULL,1,0,1,'2026-01-28 09:35:18','2026-01-28 10:35:31',125),(2016329138809384962,'3384650106421960',NULL,'G9728057976243353',NULL,NULL,-1,0,1,'2026-01-28 09:55:03','2026-01-28 09:55:03',0),(2016543345597222914,'3384650106421960',NULL,'G5505379491825551',NULL,NULL,-1,0,1,'2026-01-29 00:06:14','2026-01-29 00:06:14',0);
/*!40000 ALTER TABLE `t_group_order` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `t_group_order_dish`
--

LOCK TABLES `t_group_order_dish` WRITE;
/*!40000 ALTER TABLE `t_group_order_dish` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_group_order_dish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_ingredient_conflict_rule`
--

DROP TABLE IF EXISTS `t_ingredient_conflict_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_ingredient_conflict_rule` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è§„åˆ™ID',
  `rule_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è§„åˆ™åç§°',
  `conflict_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å†²çªç±»åž‹ï¼šALLERGY-è¿‡æ•, INCOMPATIBLE-é£Ÿæå†²çª, CUISINE-çƒ¹é¥ªç¦å¿Œ',
  `main_ingredients` json NOT NULL COMMENT 'ä¸»è¦é£Ÿæï¼ˆJSONæ•°ç»„ï¼Œå¦‚["è¾£æ¤’", "è¾£æ¤’ç²‰"]ï¼‰',
  `conflict_tags` json NOT NULL COMMENT 'å†²çªæ ‡ç­¾ï¼ˆJSONæ•°ç»„ï¼Œå¦‚["mild_no_spicy", "no_spicy"]ï¼‰',
  `severity` int DEFAULT '2' COMMENT 'ä¸¥é‡ç¨‹åº¦ï¼š1-ä½Ž, 2-ä¸­, 3-é«˜',
  `priority` int DEFAULT '3' COMMENT 'æŽ¨èä¼˜å…ˆçº§ï¼š1-é«˜ï¼ˆçº¢è‰²ï¼‰, 2-ä¸­é«˜ï¼ˆé»„è‰²ï¼‰, 3-ä¸­ï¼ˆè“è‰²ï¼‰, 4-ä½Žï¼ˆç°è‰²ï¼‰',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å†²çªæè¿°',
  `suggestion` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å»ºè®®å†…å®¹',
  `enabled` tinyint(1) DEFAULT '1' COMMENT 'æ˜¯å¦å¯ç”¨ï¼š1-å¯ç”¨ 0-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_conflict_type` (`conflict_type`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='é£Ÿæå†²çªè§„åˆ™è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_ingredient_conflict_rule`
--

LOCK TABLES `t_ingredient_conflict_rule` WRITE;
/*!40000 ALTER TABLE `t_ingredient_conflict_rule` DISABLE KEYS */;
INSERT INTO `t_ingredient_conflict_rule` VALUES ('06dc9327cab5eec927be5a3f85fb3e3d','ä¸è¦é†‹ä¸Žé†‹å†²çª','INCOMPATIBLE','[\"é†‹\", \"é™ˆé†‹\", \"ç±³é†‹\", \"ç™½é†‹\", \"é¦™é†‹\"]','[\"no_vinegar\"]',2,3,'è¯¥èœå“éœ€è¦é†‹ï¼Œæ‚¨é€‰æ‹©äº†\"ä¸è¦é†‹\"æ ‡ç­¾','å»ºè®®ï¼šå•†å®¶ä¼šä¸ä¸Šé†‹æˆ–é†‹åˆ†è£…',1,'2026-01-31 09:20:50','2026-01-31 09:20:50'),('195cfcc7a03824d72e1370ce221fffd5','ä¸è¦èŠéº»ä¸ŽèŠéº»å†²çª','INCOMPATIBLE','[\"èŠéº»\", \"ç™½èŠéº»\", \"é»‘èŠéº»\", \"èŠéº»é…±\", \"èŠéº»æ²¹\"]','[\"no_sesame\"]',2,3,'è¯¥èœå“åŒ…å«èŠéº»ï¼Œæ‚¨é€‰æ‹©äº†\"ä¸è¦èŠéº»\"æ ‡ç­¾','å»ºè®®ï¼šå•†å®¶ä¼šä¸ä¸ŠèŠéº»',1,'2026-01-31 09:20:50','2026-01-31 09:20:50'),('2851fd5a6160195a64777bb778222edb','å¾®è¾£ä¸Žè¾£æ¤’å†²çª','INCOMPATIBLE','[\"è¾£æ¤’\", \"è¾£æ¤’ç²‰\", \"å¹²è¾£æ¤’\", \"é²œè¾£æ¤’\", \"è¾£æ¤’æ²¹\"]','[\"mild_spicy\"]',2,3,'è¯¥èœå“ä¸ºè¾£å‘³èœå“ï¼Œæ‚¨é€‰æ‹©äº†\"å¾®è¾£\"æ ‡ç­¾','å»ºè®®ï¼šå•†å®¶ä¼šé€‚å½“å‡å°‘è¾£æ¤’ç”¨é‡',1,'2026-01-31 09:20:50','2026-01-31 09:20:50'),('410e2b50bdda0bc877d620eecbec3c86','ä¸è¦å§œä¸Žå§œå†²çª','INCOMPATIBLE','[\"å§œ\", \"ç”Ÿå§œ\", \"è€å§œ\", \"å§œç‰‡\", \"å§œä¸\", \"å§œæœ«\"]','[\"no_ginger\"]',2,3,'è¯¥èœå“åŒ…å«å§œï¼Œæ‚¨é€‰æ‹©äº†\"ä¸è¦å§œ\"æ ‡ç­¾','å»ºè®®ï¼šå•†å®¶ä¼šä¸ä¸Šå§œ',1,'2026-01-31 09:20:50','2026-01-31 09:20:50'),('6974b8cab3b11a9e064291c17ada5f35','ä¸è¦è‘±ä¸Žè‘±å†²çª','INCOMPATIBLE','[\"è‘±\", \"å¤§è‘±\", \"å°è‘±\", \"é’è‘±\", \"è‘±ç™½\", \"è‘±èŠ±\"]','[\"no_onion\"]',2,3,'è¯¥èœå“åŒ…å«è‘±ï¼Œæ‚¨é€‰æ‹©äº†\"ä¸è¦è‘±\"æ ‡ç­¾','å»ºè®®ï¼šå•†å®¶ä¼šä¸ä¸Šè‘±èŠ±',1,'2026-01-31 09:20:50','2026-01-31 09:20:50'),('74169658fd07aee63ca7b7480311da7c','èŠ±ç”Ÿè¿‡æ•è­¦å‘Š','ALLERGY','[\"èŠ±ç”Ÿ\", \"èŠ±ç”Ÿé…±\", \"èŠ±ç”Ÿæ²¹\"]','[\"allergy_peanut\"]',3,1,'è¯¥èœå“åŒ…å«èŠ±ç”Ÿæˆåˆ†ï¼Œä¸ºå¸¸è§è¿‡æ•æº','è­¦å‘Šï¼šå¦‚æœ‰èŠ±ç”Ÿè¿‡æ•ï¼Œè¯·å‹¿é€‰æ‹©æ­¤èœå“æˆ–æå‰å‘ŠçŸ¥å•†å®¶',1,'2026-01-31 09:20:50','2026-01-31 09:20:50'),('c40a5cb1778c6bdbfab032479bc0ade1','ä¸è¦é¦™èœä¸Žé¦™èœå†²çª','INCOMPATIBLE','[\"é¦™èœ\", \"èŠ«è½\"]','[\"no_coriander\"]',2,3,'è¯¥èœå“åŒ…å«é¦™èœï¼Œæ‚¨é€‰æ‹©äº†\"ä¸è¦é¦™èœ\"æ ‡ç­¾','å»ºè®®ï¼šå•†å®¶ä¼šä¸ä¸Šé¦™èœ',1,'2026-01-31 09:20:50','2026-01-31 09:20:50'),('c97611e69b488404880580a677a7f856','ä¸è¦è’œä¸Žè’œå†²çª','INCOMPATIBLE','[\"è’œ\", \"å¤§è’œ\", \"è’œè“‰\", \"è’œæ³¥\", \"è’œæœ«\"]','[\"no_garlic\"]',2,3,'è¯¥èœå“åŒ…å«è’œï¼Œæ‚¨é€‰æ‹©äº†\"ä¸è¦è’œ\"æ ‡ç­¾','å»ºè®®ï¼šå•†å®¶ä¼šä¸ä¸Šè’œ',1,'2026-01-31 09:20:50','2026-01-31 09:20:50'),('fe20ee904eb67235c101cea9e6bc59e4','å…è¾£ä¸Žè¾£æ¤’å†²çª','INCOMPATIBLE','[\"è¾£æ¤’\", \"è¾£æ¤’ç²‰\", \"å¹²è¾£æ¤’\", \"é²œè¾£æ¤’\", \"è¾£æ¤’æ²¹\", \"è±†ç“£é…±\", \"ç«é”…åº•æ–™\"]','[\"mild_no_spicy\"]',3,2,'è¯¥èœå“åŒ…å«è¾£æ¤’æˆåˆ†ï¼Œæ‚¨é€‰æ‹©äº†\"å…è¾£\"æ ‡ç­¾','å»ºè®®ï¼šå¦‚ä¸èƒ½åƒè¾£ï¼Œè¯·é€‰æ‹©å…¶ä»–èœå“',1,'2026-01-31 09:20:50','2026-01-31 09:20:50');
/*!40000 ALTER TABLE `t_ingredient_conflict_rule` ENABLE KEYS */;
UNLOCK TABLES;

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
  `description` text COMMENT 'èœå•æè¿°',
  `status` varchar(20) NOT NULL DEFAULT 'active' COMMENT '菜单状态（active/draft/inactive）',
  `auto_start_time` datetime DEFAULT NULL COMMENT '自动上架时间',
  `auto_end_time` datetime DEFAULT NULL COMMENT '自动下架时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_type_status` (`type`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2008415581836992515 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_menu`
--

LOCK TABLES `t_menu` WRITE;
/*!40000 ALTER TABLE `t_menu` DISABLE KEYS */;
INSERT INTO `t_menu` VALUES (2008409035581218818,7638432224340229,'test13asd','早餐','sdsasdtest','active','2026-01-27 11:20:25','2026-01-27 23:20:25','2026-01-06 13:23:23','2026-01-27 19:49:34'),(2008415581836992514,7638432224340229,'123','午餐','das','inactive','2026-01-07 00:54:01','2026-01-07 21:54:01','2026-01-06 13:49:23','2026-01-27 19:50:00');
/*!40000 ALTER TABLE `t_menu` ENABLE KEYS */;
UNLOCK TABLES;

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
  `status` int DEFAULT '1' COMMENT 'èœå“åœ¨èœå•ä¸­çš„çŠ¶æ€ï¼š1-ä¸Šæž¶ï¼Œ0-ä¸‹æž¶',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_dish` (`menu_id`,`dish_id`),
  KEY `idx_menu` (`menu_id`),
  KEY `idx_dish` (`dish_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2016116367786360835 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单菜品关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_menu_dish`
--

LOCK TABLES `t_menu_dish` WRITE;
/*!40000 ALTER TABLE `t_menu_dish` DISABLE KEYS */;
INSERT INTO `t_menu_dish` VALUES (2016116367773777922,2008409035581218818,2008574121167847426,0,1),(2016116367786360834,2008409035581218818,2008733624953769986,1,1);
/*!40000 ALTER TABLE `t_menu_dish` ENABLE KEYS */;
UNLOCK TABLES;

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
  `audit_status` varchar(20) DEFAULT 'PENDING' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼šPENDING-å¾…å®¡æ ¸, APPROVED-å·²é€šè¿‡, REJECTED-å·²æ‹’ç»',
  `audit_reason` varchar(500) DEFAULT NULL COMMENT 'å®¡æ ¸å¤‡æ³¨/åŽŸå› ',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `audit_by` varchar(50) DEFAULT NULL COMMENT 'å®¡æ ¸äººID',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_location` (`longitude`,`latitude`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_merchant`
--

LOCK TABLES `t_merchant` WRITE;
/*!40000 ALTER TABLE `t_merchant` DISABLE KEYS */;
INSERT INTO `t_merchant` VALUES (7638432224340229,'test','广东省/佛山市/三水区/进港大道1号',112.851080,23.045220,'1、2、3、33、4、5','19233333333','{\"end\": \"21:00\", \"start\": \"09:00\"}',10.00,1,'2025-12-19 15:13:59','2026-01-28 16:13:35','$2a$10$OsR4FjpVRzUjm6NI1O76ROe0a3MS/8GTPQCpzAJVsJyB23IKRiqnS','test@qq.com','asd','[\"快餐\", \"饮品\"]','asd','http://localhost:8080/api/uploads/avatar-7638432224340229-1769587220410.png','{\"dishes\": [], \"environment\": [\"http://localhost:8080/api/uploads/1769588015975-a8a81a36-d130-4a31-9db7-e08a202f7d70.png\"]}',0.00,'PENDING',NULL,NULL,NULL);
/*!40000 ALTER TABLE `t_merchant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_message_record`
--

DROP TABLE IF EXISTS `t_message_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_message_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `sender_id` bigint DEFAULT NULL COMMENT 'å‘é€è€…ID',
  `receiver_id` bigint DEFAULT NULL COMMENT 'æŽ¥æ”¶è€…ID',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT 'æ¶ˆæ¯å†…å®¹',
  `message_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'text' COMMENT 'æ¶ˆæ¯ç±»åž‹ (text: æ–‡æœ¬, image: å›¾ç‰‡, file: æ–‡ä»¶)',
  `send_time` datetime DEFAULT NULL COMMENT 'å‘é€æ—¶é—´',
  `read_status` tinyint DEFAULT '0' COMMENT 'é˜…è¯»çŠ¶æ€ (0: æœªè¯», 1: å·²è¯»)',
  `create_time` datetime DEFAULT NULL COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT NULL COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æ¶ˆæ¯è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_message_record`
--

LOCK TABLES `t_message_record` WRITE;
/*!40000 ALTER TABLE `t_message_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_message_record` ENABLE KEYS */;
UNLOCK TABLES;

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
  `type` varchar(20) DEFAULT 'system' COMMENT 'æ¶ˆæ¯ç±»åž‹: order-è®¢å•æ¶ˆæ¯, system-ç³»ç»Ÿé€šçŸ¥, promotion-ä¼˜æƒ æ´»åŠ¨',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'å‘é€æ—¶é—´',
  `read_status` tinyint(1) DEFAULT '0' COMMENT '已读状态（0-未读/1-已读）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_read_status` (`read_status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_notification`
--

LOCK TABLES `t_notification` WRITE;
/*!40000 ALTER TABLE `t_notification` DISABLE KEYS */;
INSERT INTO `t_notification` VALUES (1,3384650106421960,'订单状态更新','您的订单已发货，请耐心等待','order','2026-01-26 11:16:46',1,'2026-01-26 11:16:46'),(2,3384650106421960,'系统通知','欢迎使用佳食宜选，祝您用餐愉快','system','2026-01-26 11:16:46',1,'2026-01-26 11:16:46'),(3,3384650106421960,'优惠活动','限时优惠：全场满100减20，速来抢购！','promotion','2026-01-26 11:16:46',1,'2026-01-26 11:16:46');
/*!40000 ALTER TABLE `t_notification` ENABLE KEYS */;
UNLOCK TABLES;

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
  `user_id` varchar(50) NOT NULL COMMENT 'ç”¨æˆ·ID',
  `merchant_id` varchar(50) NOT NULL COMMENT 'å•†å®¶ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '订单状态（0-待支付/1-待接单/2-备菜中/3-烹饪中/4-待上菜/5-已完成/6-已取消）',
  `payment_id` varchar(50) DEFAULT NULL COMMENT 'æ”¯ä»˜è®°å½•ID',
  `paid_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'å·²æ”¯ä»˜é‡‘é¢',
  `payment_time` datetime DEFAULT NULL COMMENT 'æ”¯ä»˜æ—¶é—´',
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
-- Dumping data for table `t_order`
--

LOCK TABLES `t_order` WRITE;
/*!40000 ALTER TABLE `t_order` DISABLE KEYS */;
INSERT INTO `t_order` VALUES ('JD1768312307843',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',75.00,6,'2011073926872023041',75.00,'2026-01-13 21:52:43','商家地址','使用优惠券: 新用户专享50元优惠券','2026-01-13 21:52:42','2026-01-13 21:52:43',NULL),('JD1768387196617',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',75.00,6,'2011387827450834946',75.00,'2026-01-14 18:40:03','商家地址','使用优惠券: 新用户专享50元优惠券','2026-01-14 18:40:02','2026-01-14 18:40:03',NULL),('JD1768392131163',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',75.00,6,'2011409671264432129',75.00,'2026-01-14 20:06:50','商家地址','使用优惠券: 新用户专享50元优惠券','2026-01-14 20:06:50','2026-01-14 21:36:54',NULL),('JD1768393885910',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',75.00,6,'2011415898002321409',75.00,'2026-01-14 20:31:35','商家地址','使用优惠券: 新用户专享50元优惠券','2026-01-14 20:31:35','2026-01-14 21:36:57',NULL),('JD1768551070707',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',75.00,0,NULL,0.00,NULL,'商家地址','使用优惠券: 新用户专享50元优惠券','2026-01-16 16:11:16','2026-01-16 16:11:16',NULL),('JD1768551227890',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',195.00,7,'2012075830888370178',195.00,'2026-01-16 16:13:55','商家地址','使用优惠券: 新用户专享50元优惠券','2026-01-16 16:13:55','2026-01-16 16:13:55',NULL),('JD1768554456940',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',75.00,7,'2012089506550800385',75.00,'2026-01-16 17:08:16','商家地址','使用优惠券: 新用户专享50元优惠券 | 准时配送，请提前联系，配送时电话联系','2026-01-16 17:08:15','2026-01-16 17:08:16',NULL),('JD1769521187912',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',315.00,6,'2016145203622166530',315.00,'2026-01-27 21:44:09','商家地址','使用优惠券: 新用户专享50元优惠券','2026-01-27 21:44:09','2026-01-27 21:44:29',NULL),('JD1769566136871',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',125.00,1,'2016332669951979522',125.00,'2026-01-28 10:09:05','商家地址',NULL,'2026-01-28 10:09:04','2026-01-28 10:09:05',NULL),('JD1769567009896',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',125.00,1,'2016336312981827586',125.00,'2026-01-28 10:23:33','商家地址',NULL,'2026-01-28 10:23:33','2026-01-28 10:23:33',NULL),('JD1769567712571',NULL,0,NULL,NULL,'3384650106421960','7638432224340229',125.00,1,'2016339322663383042',125.00,'2026-01-28 10:35:31','商家地址',NULL,'2026-01-28 10:35:30','2026-01-28 10:35:31',NULL);
/*!40000 ALTER TABLE `t_order` ENABLE KEYS */;
UNLOCK TABLES;

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
  `cooking_minutes` int DEFAULT NULL COMMENT 'çƒ¹é¥ªè€—æ—¶ï¼ˆåˆ†é’Ÿï¼‰',
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
-- Dumping data for table `t_order_dish`
--

LOCK TABLES `t_order_dish` WRITE;
/*!40000 ALTER TABLE `t_order_dish` DISABLE KEYS */;
INSERT INTO `t_order_dish` VALUES ('2011409671126020097','JD1768392131163','2008574121167847426',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2011415897964572673','JD1768393885910','2008574121167847426',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2012075164581261314','JD1768551070707','2008574121167847426',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2012075830653489154','JD1768551227890','2008574121167847426',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2012075830666072066','JD1768551227890','2008574121167847426',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2012075830666072067','JD1768551227890','2008733624953769986',1,0.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2012089506458525698','JD1768554456940','2008574121167847426',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2016145203546669057','JD1769521187912','2008574121167847426_1769521031712',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2016145203550863362','JD1769521187912','2008574121167847426_1769521158046',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2016145203550863363','JD1769521187912','2008574121167847426_1769521171061',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2016145203550863364','JD1769521187912','2008733624953769986_1769521171063',1,0.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2016332669872287746','JD1769566136871','2008574121167847426_1769566110178',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2016332669872287747','JD1769566136871','2008733624953769986_1769566127727',1,0.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2016336312927301633','JD1769567009896','2008574121167847426_1769567007493',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL),('2016339322617245698','JD1769567712571','2008574121167847426_1769567700834',1,120.00,NULL,0,NULL,NULL,NULL,999,0,0,0,NULL);
/*!40000 ALTER TABLE `t_order_dish` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_payment_record`
--

DROP TABLE IF EXISTS `t_payment_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_payment_record` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ”¯ä»˜è®°å½•ID',
  `payment_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ”¯ä»˜æµæ°´å·',
  `order_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è®¢å•ID',
  `user_id` bigint NOT NULL COMMENT 'æ”¯ä»˜ç”¨æˆ·ID',
  `merchant_id` bigint NOT NULL COMMENT 'å•†å®¶ID',
  `amount` decimal(10,2) NOT NULL COMMENT 'æ”¯ä»˜é‡‘é¢',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'wallet' COMMENT 'æ”¯ä»˜æ–¹å¼ï¼šwallet-é’±åŒ…, wechat-å¾®ä¿¡, alipay-æ”¯ä»˜å®',
  `payment_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'æ”¯ä»˜çŠ¶æ€ï¼špending-å¾…æ”¯ä»˜, success-æˆåŠŸ, failed-å¤±è´¥, refund-å·²é€€æ¬¾',
  `transaction_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç¬¬ä¸‰æ–¹äº¤æ˜“ID',
  `paid_time` datetime DEFAULT NULL COMMENT 'æ”¯ä»˜å®Œæˆæ—¶é—´',
  `refund_amount` decimal(10,2) DEFAULT '0.00' COMMENT 'é€€æ¬¾é‡‘é¢',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `payment_no` (`payment_no`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`payment_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æ”¯ä»˜è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_payment_record`
--

LOCK TABLES `t_payment_record` WRITE;
/*!40000 ALTER TABLE `t_payment_record` DISABLE KEYS */;
INSERT INTO `t_payment_record` VALUES ('2011073926872023041','PAY20260113215242767850','JD1768312307843',3384650106421960,7638432224340229,75.00,'wallet','success',NULL,'2026-01-13 21:52:43',0.00,NULL,'2026-01-13 21:52:43','2026-01-13 21:52:43'),('2011387827450834946','PAY20260114184002497829','JD1768387196617',3384650106421960,7638432224340229,75.00,'wallet','success',NULL,'2026-01-14 18:40:03',0.00,NULL,'2026-01-14 18:40:02','2026-01-14 18:40:03'),('2011409671264432129','PAY20260114200650471598','JD1768392131163',3384650106421960,7638432224340229,75.00,'wallet','refund',NULL,'2026-01-14 20:06:50',75.00,NULL,'2026-01-14 20:06:50','2026-01-14 21:36:54'),('2011415898002321409','PAY20260114203135041784','JD1768393885910',3384650106421960,7638432224340229,75.00,'wallet','refund',NULL,'2026-01-14 20:31:35',75.00,NULL,'2026-01-14 20:31:35','2026-01-14 21:36:57'),('2012075830888370178','PAY202601161613552062','JD1768551227890',3384650106421960,7638432224340229,195.00,'wallet','success',NULL,'2026-01-16 16:13:55',0.00,NULL,'2026-01-16 16:13:55','2026-01-16 16:13:55'),('2012089506550800385','PAY202601161708158877','JD1768554456940',3384650106421960,7638432224340229,75.00,'wallet','success',NULL,'2026-01-16 17:08:16',0.00,NULL,'2026-01-16 17:08:16','2026-01-16 17:08:16'),('2016145203622166530','PAY202601272144095120','JD1769521187912',3384650106421960,7638432224340229,315.00,'wallet','refund',NULL,'2026-01-27 21:44:09',315.00,NULL,'2026-01-27 21:44:09','2026-01-27 21:44:29'),('2016332669951979522','PAY202601281009044408','JD1769566136871',3384650106421960,7638432224340229,125.00,'wallet','success',NULL,'2026-01-28 10:09:05',0.00,NULL,'2026-01-28 10:09:05','2026-01-28 10:09:05'),('2016336312981827586','PAY202601281023332273','JD1769567009896',3384650106421960,7638432224340229,125.00,'wallet','success',NULL,'2026-01-28 10:23:33',0.00,NULL,'2026-01-28 10:23:33','2026-01-28 10:23:33'),('2016339322663383042','PAY202601281035309683','JD1769567712571',3384650106421960,7638432224340229,125.00,'wallet','success',NULL,'2026-01-28 10:35:31',0.00,NULL,'2026-01-28 10:35:31','2026-01-28 10:35:31');
/*!40000 ALTER TABLE `t_payment_record` ENABLE KEYS */;
UNLOCK TABLES;

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
  `icon` varchar(50) DEFAULT NULL COMMENT 'å›¾æ ‡',
  `description` varchar(200) DEFAULT NULL COMMENT 'æƒé™æè¿°',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºåºå·',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€ï¼šACTIVE-å¯ç”¨, DISABLED-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`permission_id`),
  UNIQUE KEY `permission_code` (`permission_code`),
  KEY `idx_permission_code` (`permission_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æƒé™è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_permission`
--

LOCK TABLES `t_permission` WRITE;
/*!40000 ALTER TABLE `t_permission` DISABLE KEYS */;
INSERT INTO `t_permission` VALUES (1,'æŽ§åˆ¶å°','admin:dashboard','MENU',0,'/admin/dashboard',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(2,'ç”¨æˆ·ç®¡ç†','admin:user','MENU',0,'/admin/users',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(3,'å•†å®¶ç®¡ç†','admin:merchant','MENU',0,'/admin/merchants',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(4,'è®¢å•ç®¡ç†','admin:order','MENU',0,'/admin/orders',NULL,NULL,4,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(5,'èœå“ç®¡ç†','admin:dish','MENU',0,'/admin/dishes',NULL,NULL,5,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(6,'æ•™ç¨‹ç®¡ç†','admin:tutorial','MENU',0,'/admin/tutorials',NULL,NULL,6,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(7,'å†…å®¹ç®¡ç†','admin:content','MENU',0,'/admin/content',NULL,NULL,7,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(8,'è´¢åŠ¡ç®¡ç†','admin:finance','MENU',0,'/admin/finance',NULL,NULL,8,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(9,'ç³»ç»Ÿè®¾ç½®','admin:setting','MENU',0,'/admin/settings',NULL,NULL,9,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(10,'æ•°æ®ç»Ÿè®¡','admin:statistics','MENU',0,'/admin/statistics',NULL,NULL,10,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(11,'ç”¨æˆ·åˆ—è¡¨','admin:user:list','API',2,'/api/admin/users',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(12,'ç”¨æˆ·è¯¦æƒ…','admin:user:detail','API',2,'/api/admin/users/*',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(13,'ä¿®æ”¹ç”¨æˆ·çŠ¶æ€','admin:user:status','API',2,'/api/admin/users/*/status',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(14,'åˆ é™¤ç”¨æˆ·','admin:user:delete','API',2,'/api/admin/users/*',NULL,NULL,4,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(15,'å•†å®¶åˆ—è¡¨','admin:merchant:list','API',3,'/api/admin/merchants',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(16,'å®¡æ ¸å•†å®¶','admin:merchant:audit','API',3,'/api/admin/merchants/*/audit',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-02-01 14:22:25'),(17,'å•†å®¶çŠ¶æ€','admin:merchant:status','API',3,'/api/admin/merchants/*/status',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-02-01 14:22:25'),(18,'è®¢å•åˆ—è¡¨','admin:order:list','API',4,'/api/admin/orders',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(19,'è®¢å•è¯¦æƒ…','admin:order:detail','API',4,'/api/admin/orders/*',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(20,'è®¢å•çŠ¶æ€','admin:order:status','API',4,'/api/admin/orders/*/status',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-02-01 14:22:25'),(21,'èœå“åˆ—è¡¨','admin:dish:list','API',5,'/api/admin/dishes',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(22,'å®¡æ ¸èœå“','admin:dish:audit','API',5,'/api/admin/dishes/*/audit',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-02-01 14:22:25'),(23,'æ•™ç¨‹åˆ—è¡¨','admin:tutorial:list','API',6,'/api/admin/tutorials',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(24,'æ•™ç¨‹å®¡æ ¸','admin:tutorial:audit','API',6,'/api/admin/tutorials/*/audit',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(25,'æ•™ç¨‹åˆ é™¤','admin:tutorial:delete','API',6,'/api/admin/tutorials/*',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(26,'çƒ­ç‚¹è¯é¢˜åˆ—è¡¨','admin:topic:list','API',7,'/api/admin/topics',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(27,'çƒ­ç‚¹è¯é¢˜å®¡æ ¸','admin:topic:audit','API',7,'/api/admin/topics/*/audit',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(28,'å…¬å‘Šåˆ—è¡¨','admin:announcement:list','API',7,'/api/admin/announcements',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(29,'å…¬å‘Šå‘å¸ƒ','admin:announcement:create','API',7,'/api/admin/announcements',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(30,'æçŽ°å®¡æ ¸','admin:finance:withdrawal','API',8,'/api/admin/finance/withdrawals',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(31,'å……å€¼è®°å½•','admin:finance:recharge','API',8,'/api/admin/finance/recharges',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(32,'é€€æ¬¾ç®¡ç†','admin:finance:refund','API',8,'/api/admin/finance/refunds',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(33,'è§’è‰²ç®¡ç†','admin:setting:role','MENU',9,'/admin/settings/roles',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(34,'è§’è‰²åˆ—è¡¨','admin:setting:role:list','API',9,'/admin/roles',NULL,NULL,2,'ACTIVE','2026-01-31 11:39:20','2026-02-10 02:02:07'),(35,'è§’è‰²åˆ›å»º','admin:setting:role:create','API',9,'/admin/roles',NULL,NULL,3,'ACTIVE','2026-01-31 11:39:20','2026-02-10 02:02:07'),(36,'æƒé™åˆ†é…','admin:setting:permission','API',9,'/api/admin/settings/permissions',NULL,NULL,4,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(37,'ç³»ç»Ÿæ—¥å¿—','admin:setting:log','MENU',9,'/admin/settings/logs',NULL,NULL,5,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(38,'æ—¥å¿—æŸ¥çœ‹','admin:setting:log:view','API',9,'/api/admin/settings/logs',NULL,NULL,6,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(39,'ç»Ÿè®¡æ•°æ®','admin:statistics:view','API',10,'/api/admin/statistics',NULL,NULL,1,'ACTIVE','2026-01-31 11:39:20','2026-01-31 11:39:20'),(40,'ç”¨æˆ·ç®¡ç†','user:manage','MENU',0,'/admin/users','User','ç”¨æˆ·ç®¡ç†æ¨¡å—',1,'ACTIVE','2026-02-01 14:15:23','2026-02-01 14:15:23'),(41,'ç¼–è¾‘ç”¨æˆ·','admin:user:edit','API',1,NULL,NULL,'ç¼–è¾‘ç”¨æˆ·',3,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(42,'å•†å®¶ç®¡ç†','merchant:manage','MENU',0,'/admin/merchants','Shop','å•†å®¶ç®¡ç†',2,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(43,'å•†å®¶è¯¦æƒ…','admin:merchant:detail','API',6,NULL,NULL,'æŸ¥çœ‹å•†å®¶è¯¦æƒ…',2,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(44,'è®¢å•ç®¡ç†','order:manage','MENU',0,'/admin/orders','Document','è®¢å•ç®¡ç†',3,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(45,'è®¢å•ç»Ÿè®¡','admin:order:view','API',11,NULL,NULL,'æŸ¥çœ‹è®¢å•ç»Ÿè®¡',4,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(46,'èœå“ç®¡ç†','dish:manage','MENU',0,'/admin/dishes','Food','èœå“ç®¡ç†',4,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(47,'èœå“è¯¦æƒ…','admin:dish:detail','API',16,NULL,NULL,'æŸ¥çœ‹èœå“è¯¦æƒ…',2,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(48,'è´¢åŠ¡ç®¡ç†','finance:manage','MENU',0,'/admin/finance','Money','è´¢åŠ¡ç®¡ç†',5,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(49,'è´¢åŠ¡ç»Ÿè®¡','admin:finance:statistics','API',21,NULL,NULL,'æŸ¥çœ‹è´¢åŠ¡ç»Ÿè®¡',3,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(50,'ç³»ç»Ÿç®¡ç†','system:manage','MENU',0,'/admin/system','Setting','ç³»ç»Ÿç®¡ç†',6,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(51,'ç³»ç»Ÿæ—¥å¿—','admin:system:logs','API',26,NULL,NULL,'æŸ¥çœ‹ç³»ç»Ÿæ—¥å¿—',1,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(52,'æ¸…ç†æ—¥å¿—','admin:system:logs:clean','API',26,NULL,NULL,'æ¸…ç†æ—¥å¿—',2,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(53,'å¯¼å‡ºæ—¥å¿—','admin:system:logs:export','API',26,NULL,NULL,'å¯¼å‡ºæ—¥å¿—',3,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(54,'è§’è‰²æƒé™','role:manage','MENU',0,'/admin/roles','Lock','è§’è‰²æƒé™',7,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(55,'è§’è‰²åˆ—è¡¨','admin:role:list','API',31,NULL,NULL,'æŸ¥çœ‹è§’è‰²åˆ—è¡¨',1,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(56,'è§’è‰²è¯¦æƒ…','admin:role:detail','API',31,NULL,NULL,'æŸ¥çœ‹è§’è‰²è¯¦æƒ…',2,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(57,'åˆ›å»ºè§’è‰²','admin:role:create','API',31,NULL,NULL,'åˆ›å»ºè§’è‰²',3,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(58,'æ›´æ–°è§’è‰²','admin:role:update','API',31,NULL,NULL,'æ›´æ–°è§’è‰²',4,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(59,'åˆ é™¤è§’è‰²','admin:role:delete','API',31,NULL,NULL,'åˆ é™¤è§’è‰²',5,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(60,'åˆ†é…æƒé™','admin:role:assign','API',31,NULL,NULL,'åˆ†é…æƒé™',6,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(61,'æƒé™åˆ—è¡¨','admin:permission:list','API',31,NULL,NULL,'æŸ¥çœ‹æƒé™åˆ—è¡¨',7,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(62,'æƒé™è¯¦æƒ…','admin:permission:detail','API',31,NULL,NULL,'æŸ¥çœ‹æƒé™è¯¦æƒ…',8,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(63,'åˆ›å»ºæƒé™','admin:permission:create','API',31,NULL,NULL,'åˆ›å»ºæƒé™',9,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(64,'æ›´æ–°æƒé™','admin:permission:update','API',31,NULL,NULL,'æ›´æ–°æƒé™',10,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(65,'åˆ é™¤æƒé™','admin:permission:delete','API',31,NULL,NULL,'åˆ é™¤æƒé™',11,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(66,'æ•°æ®ç»Ÿè®¡','statistics:manage','MENU',0,'/admin/statistics','DataLine','æ•°æ®ç»Ÿè®¡',8,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(67,'ä»ªè¡¨ç›˜','admin:statistics:dashboard','API',44,NULL,NULL,'æŸ¥çœ‹ä»ªè¡¨ç›˜',1,'ACTIVE','2026-02-01 14:22:25','2026-02-01 14:22:25'),(82,'æçŽ°å®¡æ ¸','admin:finance:withdrawals','API',8,'/api/admin/withdrawals',NULL,NULL,1,'ACTIVE','2026-02-10 00:15:53','2026-02-10 00:15:53'),(83,'å……å€¼è®°å½•ç®¡ç†','admin:finance:recharges','API',8,'/api/admin/finance/recharges',NULL,NULL,2,'ACTIVE','2026-02-10 00:23:40','2026-02-10 00:23:40'),(84,'é€€æ¬¾ç®¡ç†','admin:finance:refunds','API',8,'/api/admin/finance/refunds',NULL,NULL,3,'ACTIVE','2026-02-10 00:23:40','2026-02-10 00:23:40');
/*!40000 ALTER TABLE `t_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_recharge_record`
--

DROP TABLE IF EXISTS `t_recharge_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_recharge_record` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å……å€¼è®°å½•ID',
  `recharge_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'å……å€¼æµæ°´å·',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `amount` decimal(10,2) NOT NULL COMMENT 'å……å€¼é‡‘é¢',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ”¯ä»˜æ–¹å¼ï¼šwechat-å¾®ä¿¡, alipay-æ”¯ä»˜å®, bank-é“¶è¡Œå¡',
  `recharge_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'å……å€¼çŠ¶æ€ï¼špending-å¾…æ”¯ä»˜, success-æˆåŠŸ, failed-å¤±è´¥',
  `transaction_id` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç¬¬ä¸‰æ–¹äº¤æ˜“ID',
  `paid_time` datetime DEFAULT NULL COMMENT 'æ”¯ä»˜å®Œæˆæ—¶é—´',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `recharge_no` (`recharge_no`),
  UNIQUE KEY `uk_recharge_no` (`recharge_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`recharge_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='å……å€¼è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_recharge_record`
--

LOCK TABLES `t_recharge_record` WRITE;
/*!40000 ALTER TABLE `t_recharge_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_recharge_record` ENABLE KEYS */;
UNLOCK TABLES;

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
  `detail` text COMMENT 'é£Ÿè°±è¯¦æƒ…',
  `cook_time` varchar(50) DEFAULT NULL COMMENT '烹饪时间',
  `is_favorite` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否收藏: 0-否, 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `items` json DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1767709457627 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='食谱表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_recipe`
--

LOCK TABLES `t_recipe` WRITE;
/*!40000 ALTER TABLE `t_recipe` DISABLE KEYS */;
INSERT INTO `t_recipe` VALUES (11,4590322501655448,'爱上大','lunch',0,0,0,0,NULL,'',0,'2025-12-26 20:54:01','2025-12-26 20:54:01',NULL),(12,4590322501655448,'阿诗丹顿','lunch',0,0,0,0,NULL,'',0,'2025-12-26 21:04:43','2025-12-26 21:04:43',NULL),(13,4590322501655448,'阿诗丹顿','breakfast',12,0,20,10,NULL,'',0,'2025-12-26 21:06:38','2025-12-26 21:07:00',NULL),(14,4590322501655448,'a阿诗丹顿','lunch',0,0,0,0,NULL,'',0,'2025-12-26 21:07:34','2025-12-26 21:07:34',NULL),(15,4590322501655448,'asd','breakfast',0,0,0,0,NULL,'',0,'2025-12-26 21:24:42','2025-12-26 21:25:21',NULL),(16,4590322501655448,'asd','breakfast',0,0,0,0,NULL,'',0,'2025-12-26 21:28:41','2025-12-26 21:28:50',NULL),(17,4590322501655448,'asd','lunch',0,0,0,0,NULL,'',0,'2025-12-26 21:39:32','2025-12-26 21:40:39','[{\"fat\": 2, \"name\": \"asd\", \"carbs\": 2, \"protein\": 1, \"calories\": 1, \"ingredients\": [\"ds\"]}, {\"fat\": 230, \"name\": \"test\", \"carbs\": 230, \"protein\": 32, \"calories\": 12, \"ingredients\": [\"asdsad\", \"d\", \"ss\"]}]'),(18,4590322501655448,'asd','dinner',0,0,0,0,NULL,'',0,'2025-12-26 21:45:36','2025-12-27 09:54:15','[{\"name\": \"111\", \"ingredients\": [\"222\", \"2223\"]}]'),(19,4590322501655448,'test','breakfast',0,0,0,0,NULL,'',0,'2025-12-27 10:18:01','2025-12-29 14:29:09','[{\"name\": \"sdsd\", \"ingredients\": [\"sdsd\", \"sa\"]}, {\"fat\": 0, \"name\": \"sdsd\", \"carbs\": 0, \"protein\": 0, \"calories\": 0, \"ingredients\": [\"da\"]}, {\"fat\": 0, \"name\": \"dsds\", \"carbs\": 0, \"protein\": 0, \"calories\": 0, \"ingredients\": [\"asd\", \"ds\"]}]'),(20,4590322501655448,'sdsd','lunch',0,0,0,0,NULL,'',0,'2025-12-27 12:20:14','2025-12-29 14:29:08','[{\"fat\": 0, \"name\": \"鸡胸肉盖饭\", \"carbs\": 0, \"protein\": 0, \"calories\": 0, \"ingredients\": []}, {\"fat\": 0, \"name\": \"糙米粥\", \"carbs\": 0, \"protein\": 0, \"calories\": 0, \"ingredients\": []}, {\"fat\": 0, \"name\": \"有机蔬菜沙拉\", \"carbs\": 0, \"protein\": 0, \"calories\": 0, \"ingredients\": []}, {\"fat\": 32230, \"name\": \"dasd\", \"carbs\": 0, \"protein\": 30, \"calories\": 123, \"ingredients\": [\"dsds\"]}]'),(21,4590322501655448,'sdd','breakfast',0,0,0,0,NULL,'',0,'2025-12-27 12:20:22','2025-12-29 14:29:08','[{\"fat\": 0, \"name\": \"dsaas\", \"carbs\": 0, \"protein\": 0, \"calories\": 0, \"ingredients\": [\"asd\", \"s\", \"d\", \"a\"]}]'),(24,4590322501655448,'asd','breakfast',0,0,0,0,NULL,'',0,'2025-12-27 15:41:10','2025-12-29 15:30:13','[{\"fat\": 0, \"name\": \"asd\", \"carbs\": 0, \"protein\": 0, \"calories\": 0, \"ingredients\": [\"asd\", \"ds\", \"sd\"]}]'),(25,4590322501655448,'sda','dinner',0,0,0,0,NULL,'',0,'2025-12-28 13:20:46','2025-12-28 14:18:46','[{\"fat\": 33, \"name\": \"asd\", \"carbs\": 0, \"protein\": 320, \"calories\": 12, \"ingredients\": [\"dsds\", \"sd\", \"a\", \"s\"]}]'),(26,4590322501655448,'dsd','lunch',0,0,0,0,NULL,'',0,'2025-12-29 14:36:24','2025-12-29 15:30:13','[]'),(1766992800950,4590322501655448,'asd','早餐',12312,0,0,0,NULL,'',0,'2025-12-29 15:20:01','2025-12-29 15:20:01','[\"a\"]'),(1767441295278,3384650106421960,'asd','早餐',0,0,0,0,NULL,'',0,'2026-01-03 19:54:55','2026-01-03 19:54:55','[]'),(1767442087891,3384650106421960,'a','早餐',0,0,0,0,NULL,'',0,'2026-01-03 20:08:08','2026-01-03 20:08:08','[]'),(1767709457626,3384650106421960,'31','早餐',0,0,0,0,NULL,'',0,'2026-01-06 22:24:18','2026-01-06 22:24:30','[{\"fat\": 0, \"name\": \"a\", \"carbs\": 0, \"protein\": 0, \"calories\": 0, \"ingredients\": [\"s\", \"a\", \"d\", \"f\"]}]');
/*!40000 ALTER TABLE `t_recipe` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_reject_recommendation`
--

DROP TABLE IF EXISTS `t_reject_recommendation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_reject_recommendation` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `user_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
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
-- Dumping data for table `t_reject_recommendation`
--

LOCK TABLES `t_reject_recommendation` WRITE;
/*!40000 ALTER TABLE `t_reject_recommendation` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_reject_recommendation` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `t_review`
--

LOCK TABLES `t_review` WRITE;
/*!40000 ALTER TABLE `t_review` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_review` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `t_review_reply`
--

LOCK TABLES `t_review_reply` WRITE;
/*!40000 ALTER TABLE `t_review_reply` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_review_reply` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_role`
--

DROP TABLE IF EXISTS `t_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'è§’è‰²ID',
  `role_name` varchar(50) NOT NULL COMMENT 'è§’è‰²åç§°',
  `role_code` varchar(20) NOT NULL COMMENT 'è§’è‰²ç¼–ç ï¼šSUPER_ADMIN-è¶…çº§ç®¡ç†å‘˜, ADMIN-æ™®é€šç®¡ç†å‘˜, AUDITOR-å®¡æ ¸å‘˜',
  `description` varchar(255) DEFAULT NULL COMMENT 'è§’è‰²æè¿°',
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'çŠ¶æ€ï¼šACTIVE-å¯ç”¨, DISABLED-ç¦ç”¨',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  `sort_order` int DEFAULT '0' COMMENT 'æŽ’åºåºå·',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_code` (`role_code`),
  KEY `idx_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§’è‰²è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_role`
--

LOCK TABLES `t_role` WRITE;
/*!40000 ALTER TABLE `t_role` DISABLE KEYS */;
INSERT INTO `t_role` VALUES (1,'è¶…çº§ç®¡ç†å‘˜','SUPER_ADMIN','æ‹¥æœ‰æ‰€æœ‰æƒé™çš„è¶…çº§ç®¡ç†å‘˜','ACTIVE','2026-01-31 11:39:20','2026-02-01 14:15:23',1),(2,'æ™®é€šç®¡ç†å‘˜','ADMIN','æ‹¥æœ‰å¸¸è§„ç®¡ç†æƒé™','ACTIVE','2026-01-31 11:39:20','2026-02-01 14:15:23',2),(3,'å®¡æ ¸å‘˜','AUDITOR','è´Ÿè´£å®¡æ ¸å•†å®¶å’Œå†…å®¹','ACTIVE','2026-01-31 11:39:20','2026-02-01 14:15:23',3);
/*!40000 ALTER TABLE `t_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_role_permission`
--

DROP TABLE IF EXISTS `t_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `role_id` bigint NOT NULL COMMENT 'è§’è‰²ID',
  `permission_id` bigint NOT NULL COMMENT 'æƒé™ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=142 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§’è‰²æƒé™å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_role_permission`
--

LOCK TABLES `t_role_permission` WRITE;
/*!40000 ALTER TABLE `t_role_permission` DISABLE KEYS */;
INSERT INTO `t_role_permission` VALUES (1,1,1,'2026-01-31 11:39:20'),(2,1,2,'2026-01-31 11:39:20'),(3,1,3,'2026-01-31 11:39:20'),(4,1,4,'2026-01-31 11:39:20'),(5,1,5,'2026-01-31 11:39:20'),(6,1,6,'2026-01-31 11:39:20'),(7,1,7,'2026-01-31 11:39:20'),(8,1,8,'2026-01-31 11:39:20'),(9,1,9,'2026-01-31 11:39:20'),(10,1,10,'2026-01-31 11:39:20'),(11,1,11,'2026-01-31 11:39:20'),(12,1,12,'2026-01-31 11:39:20'),(13,1,13,'2026-01-31 11:39:20'),(14,1,14,'2026-01-31 11:39:20'),(15,1,15,'2026-01-31 11:39:20'),(16,1,16,'2026-01-31 11:39:20'),(17,1,17,'2026-01-31 11:39:20'),(18,1,18,'2026-01-31 11:39:20'),(19,1,19,'2026-01-31 11:39:20'),(20,1,20,'2026-01-31 11:39:20'),(21,1,21,'2026-01-31 11:39:20'),(22,1,22,'2026-01-31 11:39:20'),(23,1,23,'2026-01-31 11:39:20'),(24,1,24,'2026-01-31 11:39:20'),(25,1,25,'2026-01-31 11:39:20'),(26,1,26,'2026-01-31 11:39:20'),(27,1,27,'2026-01-31 11:39:20'),(28,1,28,'2026-01-31 11:39:20'),(29,1,29,'2026-01-31 11:39:20'),(30,1,30,'2026-01-31 11:39:20'),(31,1,31,'2026-01-31 11:39:20'),(32,1,32,'2026-01-31 11:39:20'),(33,1,33,'2026-01-31 11:39:20'),(34,1,34,'2026-01-31 11:39:20'),(35,1,35,'2026-01-31 11:39:20'),(36,1,36,'2026-01-31 11:39:20'),(37,1,37,'2026-01-31 11:39:20'),(38,1,38,'2026-01-31 11:39:20'),(39,1,39,'2026-01-31 11:39:20'),(64,2,29,'2026-01-31 11:39:20'),(65,2,28,'2026-01-31 11:39:20'),(66,2,7,'2026-01-31 11:39:20'),(67,2,1,'2026-01-31 11:39:20'),(68,2,5,'2026-01-31 11:39:20'),(69,2,22,'2026-01-31 11:39:20'),(70,2,21,'2026-01-31 11:39:20'),(71,2,8,'2026-01-31 11:39:20'),(72,2,31,'2026-01-31 11:39:20'),(73,2,32,'2026-01-31 11:39:20'),(74,2,30,'2026-01-31 11:39:20'),(75,2,3,'2026-01-31 11:39:20'),(76,2,16,'2026-01-31 11:39:20'),(77,2,15,'2026-01-31 11:39:20'),(78,2,17,'2026-01-31 11:39:20'),(79,2,4,'2026-01-31 11:39:20'),(80,2,19,'2026-01-31 11:39:20'),(81,2,18,'2026-01-31 11:39:20'),(82,2,20,'2026-01-31 11:39:20'),(83,2,9,'2026-01-31 11:39:20'),(84,2,10,'2026-01-31 11:39:20'),(85,2,39,'2026-01-31 11:39:20'),(86,2,27,'2026-01-31 11:39:20'),(87,2,26,'2026-01-31 11:39:20'),(88,2,6,'2026-01-31 11:39:20'),(89,2,24,'2026-01-31 11:39:20'),(90,2,25,'2026-01-31 11:39:20'),(91,2,23,'2026-01-31 11:39:20'),(92,2,2,'2026-01-31 11:39:20'),(93,2,14,'2026-01-31 11:39:20'),(94,2,12,'2026-01-31 11:39:20'),(95,2,11,'2026-01-31 11:39:20'),(96,2,13,'2026-01-31 11:39:20'),(127,3,22,'2026-01-31 11:39:20'),(128,3,16,'2026-01-31 11:39:20'),(129,3,27,'2026-01-31 11:39:20'),(130,3,24,'2026-01-31 11:39:20'),(134,1,82,'2026-02-10 00:15:53'),(135,2,82,'2026-02-10 00:15:53'),(136,1,83,'2026-02-10 00:23:40'),(137,1,84,'2026-02-10 00:23:40'),(139,2,83,'2026-02-10 00:23:40'),(140,2,84,'2026-02-10 00:23:40');
/*!40000 ALTER TABLE `t_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_role_permission_relation`
--

DROP TABLE IF EXISTS `t_role_permission_relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_role_permission_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å…³è”ID',
  `role_id` bigint NOT NULL COMMENT 'è§’è‰²ID',
  `permission_id` bigint NOT NULL COMMENT 'æƒé™ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=262 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§’è‰²-æƒé™å…³è”è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_role_permission_relation`
--

LOCK TABLES `t_role_permission_relation` WRITE;
/*!40000 ALTER TABLE `t_role_permission_relation` DISABLE KEYS */;
INSERT INTO `t_role_permission_relation` VALUES (1,1,1,'2026-02-01 14:22:25'),(2,1,2,'2026-02-01 14:22:25'),(3,1,3,'2026-02-01 14:22:25'),(4,1,4,'2026-02-01 14:22:25'),(5,1,5,'2026-02-01 14:22:25'),(6,1,6,'2026-02-01 14:22:25'),(7,1,7,'2026-02-01 14:22:25'),(8,1,8,'2026-02-01 14:22:25'),(9,1,9,'2026-02-01 14:22:25'),(10,1,10,'2026-02-01 14:22:25'),(11,1,40,'2026-02-01 14:22:25'),(12,1,42,'2026-02-01 14:22:25'),(13,1,44,'2026-02-01 14:22:25'),(14,1,46,'2026-02-01 14:22:25'),(15,1,48,'2026-02-01 14:22:25'),(16,1,50,'2026-02-01 14:22:25'),(17,1,54,'2026-02-01 14:22:25'),(18,1,66,'2026-02-01 14:22:25'),(19,1,41,'2026-02-01 14:22:25'),(20,1,11,'2026-02-01 14:22:25'),(21,1,12,'2026-02-01 14:22:25'),(22,1,13,'2026-02-01 14:22:25'),(23,1,14,'2026-02-01 14:22:25'),(24,1,15,'2026-02-01 14:22:25'),(25,1,16,'2026-02-01 14:22:25'),(26,1,17,'2026-02-01 14:22:25'),(27,1,18,'2026-02-01 14:22:25'),(28,1,19,'2026-02-01 14:22:25'),(29,1,20,'2026-02-01 14:22:25'),(30,1,21,'2026-02-01 14:22:25'),(31,1,22,'2026-02-01 14:22:25'),(32,1,23,'2026-02-01 14:22:25'),(33,1,24,'2026-02-01 14:22:25'),(34,1,25,'2026-02-01 14:22:25'),(35,1,43,'2026-02-01 14:22:25'),(36,1,26,'2026-02-01 14:22:25'),(37,1,27,'2026-02-01 14:22:25'),(38,1,28,'2026-02-01 14:22:25'),(39,1,29,'2026-02-01 14:22:25'),(40,1,30,'2026-02-01 14:22:25'),(41,1,31,'2026-02-01 14:22:25'),(42,1,32,'2026-02-01 14:22:25'),(43,1,33,'2026-02-01 14:22:25'),(44,1,34,'2026-02-01 14:22:25'),(45,1,35,'2026-02-01 14:22:25'),(46,1,36,'2026-02-01 14:22:25'),(47,1,37,'2026-02-01 14:22:25'),(48,1,38,'2026-02-01 14:22:25'),(49,1,39,'2026-02-01 14:22:25'),(50,1,45,'2026-02-01 14:22:25'),(51,1,47,'2026-02-01 14:22:25'),(52,1,49,'2026-02-01 14:22:25'),(53,1,51,'2026-02-01 14:22:25'),(54,1,52,'2026-02-01 14:22:25'),(55,1,53,'2026-02-01 14:22:25'),(56,1,55,'2026-02-01 14:22:25'),(57,1,56,'2026-02-01 14:22:25'),(58,1,57,'2026-02-01 14:22:25'),(59,1,58,'2026-02-01 14:22:25'),(60,1,59,'2026-02-01 14:22:25'),(61,1,60,'2026-02-01 14:22:25'),(62,1,61,'2026-02-01 14:22:25'),(63,1,62,'2026-02-01 14:22:25'),(64,1,63,'2026-02-01 14:22:25'),(65,1,64,'2026-02-01 14:22:25'),(66,1,65,'2026-02-01 14:22:25'),(67,1,67,'2026-02-01 14:22:25'),(128,2,29,'2026-02-01 14:22:25'),(129,2,28,'2026-02-01 14:22:25'),(130,2,7,'2026-02-01 14:22:25'),(131,2,1,'2026-02-01 14:22:25'),(132,2,5,'2026-02-01 14:22:25'),(133,2,22,'2026-02-01 14:22:25'),(134,2,47,'2026-02-01 14:22:25'),(135,2,21,'2026-02-01 14:22:25'),(136,2,8,'2026-02-01 14:22:25'),(137,2,31,'2026-02-01 14:22:25'),(138,2,32,'2026-02-01 14:22:25'),(139,2,49,'2026-02-01 14:22:25'),(140,2,30,'2026-02-01 14:22:25'),(141,2,3,'2026-02-01 14:22:25'),(142,2,16,'2026-02-01 14:22:25'),(143,2,43,'2026-02-01 14:22:25'),(144,2,15,'2026-02-01 14:22:25'),(145,2,17,'2026-02-01 14:22:25'),(146,2,4,'2026-02-01 14:22:25'),(147,2,19,'2026-02-01 14:22:25'),(148,2,18,'2026-02-01 14:22:25'),(149,2,20,'2026-02-01 14:22:25'),(150,2,45,'2026-02-01 14:22:25'),(151,2,63,'2026-02-01 14:22:25'),(152,2,65,'2026-02-01 14:22:25'),(153,2,62,'2026-02-01 14:22:25'),(154,2,61,'2026-02-01 14:22:25'),(155,2,64,'2026-02-01 14:22:25'),(156,2,60,'2026-02-01 14:22:25'),(157,2,57,'2026-02-01 14:22:25'),(158,2,59,'2026-02-01 14:22:25'),(159,2,56,'2026-02-01 14:22:25'),(160,2,55,'2026-02-01 14:22:25'),(161,2,58,'2026-02-01 14:22:25'),(162,2,9,'2026-02-01 14:22:25'),(163,2,37,'2026-02-01 14:22:25'),(164,2,38,'2026-02-01 14:22:25'),(165,2,36,'2026-02-01 14:22:25'),(166,2,33,'2026-02-01 14:22:25'),(167,2,35,'2026-02-01 14:22:25'),(168,2,34,'2026-02-01 14:22:25'),(169,2,10,'2026-02-01 14:22:25'),(170,2,67,'2026-02-01 14:22:25'),(171,2,39,'2026-02-01 14:22:25'),(172,2,51,'2026-02-01 14:22:25'),(173,2,52,'2026-02-01 14:22:25'),(174,2,53,'2026-02-01 14:22:25'),(175,2,27,'2026-02-01 14:22:25'),(176,2,26,'2026-02-01 14:22:25'),(177,2,6,'2026-02-01 14:22:25'),(178,2,24,'2026-02-01 14:22:25'),(179,2,25,'2026-02-01 14:22:25'),(180,2,23,'2026-02-01 14:22:25'),(181,2,2,'2026-02-01 14:22:25'),(182,2,14,'2026-02-01 14:22:25'),(183,2,12,'2026-02-01 14:22:25'),(184,2,41,'2026-02-01 14:22:25'),(185,2,11,'2026-02-01 14:22:25'),(186,2,13,'2026-02-01 14:22:25'),(187,2,46,'2026-02-01 14:22:25'),(188,2,48,'2026-02-01 14:22:25'),(189,2,42,'2026-02-01 14:22:25'),(190,2,44,'2026-02-01 14:22:25'),(191,2,66,'2026-02-01 14:22:25'),(192,2,40,'2026-02-01 14:22:25'),(255,3,22,'2026-02-01 14:22:25'),(256,3,47,'2026-02-01 14:22:25'),(257,3,21,'2026-02-01 14:22:25'),(258,3,16,'2026-02-01 14:22:25'),(259,3,43,'2026-02-01 14:22:25'),(260,3,15,'2026-02-01 14:22:25');
/*!40000 ALTER TABLE `t_role_permission_relation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_system_log`
--

DROP TABLE IF EXISTS `t_system_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_system_log` (
  `log_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ—¥å¿—ID',
  `operation_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œç±»åž‹',
  `module` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œæ¨¡å—',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œæè¿°',
  `operator_id` bigint DEFAULT NULL COMMENT 'æ“ä½œäººID',
  `operator_name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œäººåç§°',
  `operator_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œäººç±»åž‹',
  `method` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'è¯·æ±‚æ–¹æ³•',
  `params` text COLLATE utf8mb4_unicode_ci COMMENT 'è¯·æ±‚å‚æ•°',
  `result` text COLLATE utf8mb4_unicode_ci COMMENT 'è¿”å›žç»“æžœ',
  `execute_time` bigint DEFAULT NULL COMMENT 'æ‰§è¡Œæ—¶é•¿(æ¯«ç§’)',
  `ip` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IPåœ°å€',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œçŠ¶æ€',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT 'é”™è¯¯ä¿¡æ¯',
  `browser` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æµè§ˆå™¨ç±»åž‹',
  `os` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ“ä½œç³»ç»Ÿ',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  PRIMARY KEY (`log_id`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_module` (`module`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç³»ç»Ÿæ—¥å¿—è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_system_log`
--

LOCK TABLES `t_system_log` WRITE;
/*!40000 ALTER TABLE `t_system_log` DISABLE KEYS */;
INSERT INTO `t_system_log` VALUES ('2020874039232360449','UPDATE','HOT_TOPIC','更新热点：1',1,'admin','ADMIN','HotTopicAdminController.update','id=1',NULL,0,NULL,'SUCCESS',NULL,NULL,NULL,'2026-02-09 22:54:52'),('2020875218205077506','UPDATE','HOT_TOPIC','更新热点：2',1,'admin','ADMIN','HotTopicAdminController.update','id=2',NULL,0,NULL,'SUCCESS',NULL,NULL,NULL,'2026-02-09 22:59:33'),('2020888898380546049','UPDATE','ANNOUNCEMENT','更新系统公告：312',1,'admin','ADMIN','AdminAnnouncementController.updateAnnouncement','announcementId=4',NULL,0,NULL,'SUCCESS',NULL,NULL,NULL,'2026-02-09 23:53:54');
/*!40000 ALTER TABLE `t_system_log` ENABLE KEYS */;
UNLOCK TABLES;

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
  `payment_password` varchar(255) DEFAULT NULL COMMENT 'æ”¯ä»˜å¯†ç ï¼ˆåŠ å¯†å­˜å‚¨ï¼‰',
  `has_payment_password` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å·²è®¾ç½®æ”¯ä»˜å¯†ç ï¼š0-æœªè®¾ç½®ï¼Œ1-å·²è®¾ç½®',
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
  `location` varchar(50) DEFAULT NULL COMMENT 'ç”¨æˆ·æ‰€åœ¨åœ°',
  `gender` varchar(10) DEFAULT NULL COMMENT 'æ€§åˆ«ï¼šmale-ç”·ï¼Œfemale-å¥³ï¼Œother-å…¶ä»–',
  `birthday` varchar(20) DEFAULT NULL COMMENT 'ç”Ÿæ—¥',
  `bio` text COMMENT 'ä¸ªäººç®€ä»‹',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user`
--

LOCK TABLES `t_user` WRITE;
/*!40000 ALTER TABLE `t_user` DISABLE KEYS */;
INSERT INTO `t_user` VALUES (1000000000000000,'13800138000','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'张三',NULL,NULL,NULL,NULL,NULL,NULL,0,'2025-12-20 20:30:30','2025-12-20 20:37:42',NULL,NULL,NULL,NULL,NULL,NULL),(1000000000000001,'13800138001','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'李四',NULL,NULL,NULL,NULL,NULL,NULL,0,'2025-12-20 20:30:30','2025-12-20 20:37:42',NULL,NULL,NULL,NULL,NULL,NULL),(1000000000000002,'17322222221','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'å°æ˜Ž',170.0,60.0,'keep_fit','[]','[\"ç´ é£Ÿ\", \"è½»é£Ÿ\", \"å¥åº·é¤\"]',NULL,0,'2025-12-20 20:31:50','2025-12-20 20:37:42',NULL,NULL,NULL,NULL,NULL,NULL),(1000000000000003,'17322222223','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'å°çº¢',160.0,48.0,'lose_weight','[\"æµ·é²œ\"]','[\"ä½Žå¡\", \"å‡è„‚é¤\", \"æ²™æ‹‰\"]',NULL,0,'2025-12-20 20:31:50','2025-12-20 20:37:42',NULL,NULL,NULL,NULL,NULL,NULL),(1000000000000004,'17322222224','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'å°åˆš',185.0,90.0,'gain_weight','[]','[\"è¥¿é¤\", \"ç‰›æŽ’\", \"æ±‰å ¡\"]',NULL,1,'2025-12-20 20:31:50','2025-12-20 20:37:42',NULL,NULL,NULL,NULL,NULL,NULL),(3384650106421960,'17322222222','$2a$10$H.WwxnoRrbx1LztA2ocrQOixxGss4ZFRHHuM5Z1NLJJxC9eoDu0Cu',NULL,0,'asd',NULL,NULL,NULL,NULL,NULL,'asd@qq.com',0,'2025-12-19 10:44:14','2025-12-19 10:44:14','/api/uploads/3384650106421960/4d60fa71-a4fa-4346-94c2-5a1ed316bbab.png',7638432224340229,NULL,NULL,NULL,NULL),(4590322501655448,'13822222222','$2a$10$zTOYTi1VHq9cgjXM/kgFTuJShygJ3AVNrcV2mDXXaFRvQpzTyH/5i',NULL,0,'nick',NULL,NULL,NULL,NULL,NULL,'asd@qq.com',0,'2025-12-14 21:08:49','2025-12-26 13:25:53','/api/uploads/4590322501655448/4b88c01d-3ad5-464b-85e8-b67bfcd1ae79.png',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `t_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user_custom_event`
--

DROP TABLE IF EXISTS `t_user_custom_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_custom_event` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
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
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_event_date` (`event_date`),
  KEY `idx_event_type` (`event_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·è‡ªå®šä¹‰äº‹ä»¶è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_custom_event`
--

LOCK TABLES `t_user_custom_event` WRITE;
/*!40000 ALTER TABLE `t_user_custom_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_user_custom_event` ENABLE KEYS */;
UNLOCK TABLES;

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
  `enable_activity_notification` tinyint(1) DEFAULT '1' COMMENT 'æ´»åŠ¨é€šçŸ¥å¼€å…³',
  `enable_merchant_reply_notification` tinyint(1) DEFAULT '1' COMMENT 'å•†å®¶å›žå¤é€šçŸ¥å¼€å…³',
  `enable_group_chat_notification` tinyint(1) DEFAULT '1' COMMENT 'ç¾¤èŠæ¶ˆæ¯é€šçŸ¥å¼€å…³',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2015813157733384195 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户推荐偏好表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_preference`
--

LOCK TABLES `t_user_preference` WRITE;
/*!40000 ALTER TABLE `t_user_preference` DISABLE KEYS */;
INSERT INTO `t_user_preference` VALUES (2014594261701468161,3384650106421960,'{}',_binary '\0',NULL,'[]','2026-01-23 19:56:31',1,1,1,1,1),(2015813157733384194,4590322501655448,'{}',_binary '\0',NULL,'[]','2026-01-26 23:44:44',0,1,1,1,1);
/*!40000 ALTER TABLE `t_user_preference` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_user_recommend_history`
--

DROP TABLE IF EXISTS `t_user_recommend_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_user_recommend_history` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
  `festival_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èŠ‚æ—¥ID',
  `custom_event_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'è‡ªå®šä¹‰äº‹ä»¶ID',
  `dish_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“ID',
  `recommend_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æŽ¨èç±»åž‹',
  `is_clicked` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ç‚¹å‡»',
  `is_ordered` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ä¸‹å•',
  `feedback_score` int DEFAULT NULL COMMENT 'åé¦ˆè¯„åˆ†ï¼ˆ1-5ï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_festival_id` (`festival_id`),
  KEY `idx_custom_event_id` (`custom_event_id`),
  KEY `idx_dish_id` (`dish_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·æŽ¨èè®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_user_recommend_history`
--

LOCK TABLES `t_user_recommend_history` WRITE;
/*!40000 ALTER TABLE `t_user_recommend_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_user_recommend_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_wallet`
--

DROP TABLE IF EXISTS `t_wallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_wallet` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'é’±åŒ…ID',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'å½“å‰ä½™é¢ï¼ˆå…ƒï¼‰',
  `total_recharge` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ç´¯è®¡å……å€¼é‡‘é¢',
  `total_consume` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ç´¯è®¡æ¶ˆè´¹é‡‘é¢',
  `total_withdraw` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'ç´¯è®¡æçŽ°é‡‘é¢',
  `version` int NOT NULL DEFAULT '0' COMMENT 'ä¹è§‚é”ç‰ˆæœ¬å·',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'active' COMMENT 'é’±åŒ…çŠ¶æ€ï¼šactive-æ­£å¸¸, frozen-å†»ç»“',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·é’±åŒ…è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_wallet`
--

LOCK TABLES `t_wallet` WRITE;
/*!40000 ALTER TABLE `t_wallet` DISABLE KEYS */;
INSERT INTO `t_wallet` VALUES ('1',1000000000000000,0.00,0.00,0.00,0.00,0,'active','2026-01-13 14:45:28','2026-01-13 14:45:28'),('2',1000000000000001,0.00,0.00,0.00,0.00,0,'active','2026-01-13 14:45:28','2026-01-13 14:45:28'),('3',4590322501655448,0.00,0.00,0.00,0.00,0,'active','2026-01-13 14:45:28','2026-01-13 14:45:28'),('4',1000000000000002,0.00,0.00,0.00,0.00,0,'active','2026-01-13 14:45:28','2026-01-13 14:45:28'),('5',3384650106421960,472.00,1567.00,1260.00,300.00,16,'active','2026-01-13 14:45:28','2026-01-28 10:35:31'),('6',1000000000000003,0.00,0.00,0.00,0.00,0,'active','2026-01-13 14:45:28','2026-01-13 14:45:28'),('7',1000000000000004,0.00,0.00,0.00,0.00,0,'active','2026-01-13 14:45:28','2026-01-13 14:45:28');
/*!40000 ALTER TABLE `t_wallet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_wish_list_item`
--

DROP TABLE IF EXISTS `t_wish_list_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_wish_list_item` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ä¸»é”®ID',
  `user_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
  `merchant_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å•†å®¶IDï¼ˆå¯é€‰ï¼ŒæŒ‡å®šå•†å®¶çš„éœ€æ±‚ï¼‰',
  `dish_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'èœå“åç§°',
  `dish_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'èœå“å›¾ç‰‡URL',
  `taste_requirement` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å£å‘³è¦æ±‚',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT 'è¯¦ç»†æè¿°',
  `recipe_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å‚è€ƒé£Ÿè°±ID',
  `audit_status` int DEFAULT '0' COMMENT 'å®¡æ ¸çŠ¶æ€ï¼š0-å¾…å®¡æ ¸, 1-å·²é€šè¿‡, 2-å·²æ‹’ç», 3-ç”³è¯‰ä¸­, 4-ç”³è¯‰æˆåŠŸ, 5-ç”³è¯‰å¤±è´¥, 6-è¶…æ—¶è‡ªåŠ¨é€šè¿‡, 7-å·²æ’¤å›ž',
  `rejection_reason_code` int DEFAULT NULL COMMENT 'æ‹’ç»åŽŸå› ä»£ç ',
  `rejection_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ‹’ç»åŽŸå› è¯´æ˜Ž',
  `audit_remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å•†å®¶å®¡æ ¸å¤‡æ³¨',
  `auditor_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸äººID',
  `auditor_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸äººå§“å',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `is_appealed` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦ç”³è¯‰',
  `appeal_content` text COLLATE utf8mb4_unicode_ci COMMENT 'ç”³è¯‰å†…å®¹',
  `appeal_time` datetime DEFAULT NULL COMMENT 'ç”³è¯‰æ—¶é—´',
  `appeal_reply` text COLLATE utf8mb4_unicode_ci COMMENT 'ç”³è¯‰å›žå¤',
  `appeal_reply_time` datetime DEFAULT NULL COMMENT 'ç”³è¯‰å›žå¤æ—¶é—´',
  `appeal_replier_id` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç”³è¯‰å›žå¤äººID',
  `expected_available_time` datetime DEFAULT NULL COMMENT 'æœŸæœ›ä¸Šæž¶æ—¶é—´',
  `actual_available_time` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®žé™…ä¸Šæž¶æ—¶é—´ï¼ˆå®¡æ ¸é€šè¿‡åŽå•†å®¶å¡«å†™çš„é¢„è®¡æ—¶é—´ï¼‰',
  `timeout_time` datetime NOT NULL COMMENT 'è¶…æ—¶æ—¶é—´ï¼ˆ24å°æ—¶è‡ªåŠ¨é€šè¿‡ï¼‰',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_audit_status` (`audit_status`),
  KEY `idx_timeout_time` (`timeout_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æƒ³åƒåˆ—è¡¨é¡¹è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_wish_list_item`
--

LOCK TABLES `t_wish_list_item` WRITE;
/*!40000 ALTER TABLE `t_wish_list_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_wish_list_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `t_withdraw_record`
--

DROP TABLE IF EXISTS `t_withdraw_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `t_withdraw_record` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æçŽ°è®°å½•ID',
  `withdraw_no` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æçŽ°æµæ°´å·',
  `user_id` bigint NOT NULL COMMENT 'ç”¨æˆ·ID',
  `amount` decimal(10,2) NOT NULL COMMENT 'æçŽ°é‡‘é¢',
  `fee` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT 'æ‰‹ç»­è´¹',
  `actual_amount` decimal(10,2) NOT NULL COMMENT 'å®žé™…åˆ°è´¦é‡‘é¢',
  `withdraw_method` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æçŽ°æ–¹å¼ï¼šwechat-å¾®ä¿¡, alipay-æ”¯ä»˜å®, bank-é“¶è¡Œå¡',
  `account_info` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æçŽ°è´¦å·ä¿¡æ¯ï¼ˆè„±æ•ï¼‰',
  `withdraw_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'æçŽ°çŠ¶æ€ï¼špending-å¾…å®¡æ ¸, approved-å·²é€šè¿‡, rejected-å·²æ‹’ç», processing-å¤„ç†ä¸­, success-æˆåŠŸ, failed-å¤±è´¥',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `audit_user` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸äºº',
  `complete_time` datetime DEFAULT NULL COMMENT 'å®Œæˆæ—¶é—´',
  `reject_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'æ‹’ç»åŽŸå› ',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å¤‡æ³¨',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'åˆ›å»ºæ—¶é—´',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'æ›´æ–°æ—¶é—´',
  PRIMARY KEY (`id`),
  UNIQUE KEY `withdraw_no` (`withdraw_no`),
  UNIQUE KEY `uk_withdraw_no` (`withdraw_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`withdraw_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='æçŽ°è®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `t_withdraw_record`
--

LOCK TABLES `t_withdraw_record` WRITE;
/*!40000 ALTER TABLE `t_withdraw_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `t_withdraw_record` ENABLE KEYS */;
UNLOCK TABLES;

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
-- Dumping data for table `tmp_id_mappings`
--

LOCK TABLES `tmp_id_mappings` WRITE;
/*!40000 ALTER TABLE `tmp_id_mappings` DISABLE KEYS */;
/*!40000 ALTER TABLE `tmp_id_mappings` ENABLE KEYS */;
UNLOCK TABLES;

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
  `author_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä½œè€…ç±»åž‹: ADMIN, MERCHANT, AI',
  `author_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ä½œè€…ID',
  `featured` tinyint(1) DEFAULT '0' COMMENT '是否在首页推荐',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PUBLISHED' COMMENT 'çŠ¶æ€: DRAFT-è‰ç¨¿, PENDING-å¾…å®¡æ ¸, PUBLISHED-å·²å‘å¸ƒ, REJECTED-å·²æ‹’ç»',
  `review_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸çŠ¶æ€: NOT_SUBMITTED-æœªæäº¤, PENDING-å¾…å®¡æ ¸, APPROVED-é€šè¿‡, REJECTED-æ‹’ç»',
  `reviewer_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å®¡æ ¸äººID',
  `review_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ—¶é—´',
  `review_comment` text COLLATE utf8mb4_unicode_ci COMMENT 'å®¡æ ¸æ„è§',
  `is_official` tinyint(1) DEFAULT '0' COMMENT 'æ˜¯å¦å®˜æ–¹è®¤è¯(ä»…ç®¡ç†å‘˜å‘å¸ƒ)',
  `linked_merchant_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”å•†å®¶IDï¼ˆå•†å®¶æ•™ç¨‹å¯ç”¨ï¼‰',
  `linked_dish_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å…³è”èœå“IDï¼ˆå•†å®¶æ•™ç¨‹å¯ç”¨ï¼‰',
  `ai_model_version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'AIæ¨¡åž‹ç‰ˆæœ¬(AIæ•™ç¨‹)',
  `tags` json DEFAULT NULL COMMENT 'æ ‡ç­¾æ•°ç»„: ["å¥åº·", "ä½Žå¡", "ç´ é£Ÿ"]',
  `difficulty` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'éš¾åº¦: BEGINNER-åˆçº§, INTERMEDIATE-ä¸­çº§, ADVANCED-é«˜çº§',
  `calories` int DEFAULT NULL COMMENT 'å¡è·¯é‡Œ',
  `prep_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'å‡†å¤‡æ—¶é—´',
  `servings` int DEFAULT NULL COMMENT 'ä»½é‡',
  `rating` decimal(3,2) DEFAULT '0.00' COMMENT 'è¯„åˆ†(0-5)',
  `rating_count` int DEFAULT '0' COMMENT 'è¯„åˆ†äººæ•°',
  `favorite_count` int DEFAULT '0' COMMENT 'æ”¶è—æ¬¡æ•°',
  `view_count` int DEFAULT '0' COMMENT 'æµè§ˆæ¬¡æ•°',
  `share_count` int DEFAULT '0' COMMENT 'åˆ†äº«æ¬¡æ•°',
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
) ENGINE=InnoDB AUTO_INCREMENT=2017069966498480131 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='制作教程与指南表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tutorial`
--

LOCK TABLES `tutorial` WRITE;
/*!40000 ALTER TABLE `tutorial` DISABLE KEYS */;
INSERT INTO `tutorial` VALUES (2017045959384526850,'我的拿手菜','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'INTERMEDIATE',NULL,NULL,NULL,0.00,0,0,0,0,'## 菜品介绍\n\n### 食材准备\n\n### 制作步骤\n\n### 小贴士',NULL,NULL,'asasdfgh','2026-01-30 09:23:26','2026-02-01 10:48:53'),(2017045980041474049,'我的拿手菜','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'INTERMEDIATE',NULL,NULL,NULL,0.00,0,0,0,0,'## 菜品介绍\n\n### 食材准备\n\n### 制作步骤\n\n### 小贴士',NULL,NULL,'asasdfgh','2026-01-30 09:23:31','2026-02-01 10:48:53'),(2017047001597767681,'我的拿手菜','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'INTERMEDIATE',NULL,NULL,NULL,0.00,0,0,0,0,'## 菜品介绍\n\n### 食材准备\n\n### 制作步骤\n\n### 小贴士',NULL,NULL,'asasdfgh','2026-01-30 09:27:35','2026-02-01 10:48:53'),(2017047463877177345,'健康饮食心得','article','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,NULL,NULL,0.00,0,0,0,0,'## 饮食心得\n\n### 推荐搭配\n\n### 营养建议',NULL,NULL,'asasdfgh','2026-01-30 09:29:25','2026-02-01 10:48:53'),(2017048366839525378,'快手食谱分享','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,NULL,NULL,0.00,0,0,1,0,'## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项',NULL,NULL,'asasdfgh','2026-01-30 09:33:00','2026-02-01 10:48:53'),(2017049690733834241,'快手食谱分享','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'PENDING','PENDING',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,NULL,NULL,0.00,0,0,0,0,'## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项',NULL,NULL,'asasdfgh','2026-01-30 09:38:16','2026-02-01 10:48:53'),(2017050161129222145,'快手食谱分享','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'PENDING','PENDING',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,NULL,NULL,0.00,0,0,0,0,'## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项',NULL,NULL,'asasdfgh','2026-01-30 09:40:08','2026-02-01 10:48:53'),(2017050982407499777,'快手食谱分享','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'PUBLISHED','APPROVED','1','2026-02-01 11:18:42','',0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,NULL,NULL,0.00,0,0,0,0,'## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项',NULL,NULL,'asasdfgh','2026-01-30 09:43:24','2026-02-01 11:18:42'),(2017066808271466497,'快手食谱分享','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,NULL,NULL,0.00,0,0,0,0,'## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项',NULL,NULL,'asasdfgh','2026-01-30 10:46:17','2026-02-01 10:48:53'),(2017066861635596289,'快手食谱分享','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,NULL,NULL,0.00,0,0,1,0,'## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项',NULL,NULL,'asasdfgh','2026-01-30 10:46:30','2026-02-01 10:48:53'),(2017068332150177794,'快手食谱分享','video','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,NULL,NULL,0.00,0,0,0,0,'## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项',NULL,NULL,'asasdfgh','2026-01-30 10:52:20','2026-02-01 10:48:53'),(2017068817821220866,'快手食谱分享','video','22',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'DRAFT','NOT_SUBMITTED',NULL,NULL,NULL,0,NULL,NULL,NULL,NULL,'BEGINNER',22,'33',1,0.00,0,0,35,0,'## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项','http://localhost:8080/api/uploads/chat/94e040f0-f905-42f6-9856-26c3d8ee7dfc.png','http://localhost:8080/api/uploads/chat/e58857a1-89b4-4bfd-bb83-77b7541c4bf4.mov','asasdfgh','2026-01-30 10:54:16','2026-02-01 10:48:53'),(2017069966498480130,'健康饮食心得test','article','',NULL,'MERCHANT',NULL,'MERCHANT','1',0,'PUBLISHED','APPROVED','1','2026-02-01 10:47:52','1',0,NULL,NULL,NULL,NULL,'BEGINNER',NULL,'',NULL,0.00,0,0,17,0,'## 饮食心得\n\n### 推荐搭配\n\n### 营养建议','http://localhost:8080/api/uploads/chat/8fc5c0ca-f9c2-4ff7-a45f-36c7a39fece0.png','','asasdfgh','2026-01-30 10:58:50','2026-02-01 10:48:53');
/*!40000 ALTER TABLE `tutorial` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tutorial_backup_20250129`
--

DROP TABLE IF EXISTS `tutorial_backup_20250129`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tutorial_backup_20250129` (
  `id` bigint NOT NULL DEFAULT '0' COMMENT '主键id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教程标题',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教程类型：video/article',
  `duration` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '时长',
  `views` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '观看/阅读量',
  `featured` tinyint(1) DEFAULT '0' COMMENT '是否在首页推荐',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '教程内容',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '封面图片',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '视频地址',
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '作者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tutorial_backup_20250129`
--

LOCK TABLES `tutorial_backup_20250129` WRITE;
/*!40000 ALTER TABLE `tutorial_backup_20250129` DISABLE KEYS */;
INSERT INTO `tutorial_backup_20250129` VALUES (1,'青木瓜沙拉制作教程','video','5:30','12.5k',1,'详细的青木瓜沙拉制作教程...','http://example.com/cover1.jpg',NULL,'美食专家','2025-11-29 14:05:32','2025-11-29 14:05:32'),(2,'夏日低卡饮食指南','article','8分钟','8.2k',1,'夏日低卡饮食的详细指南...','http://example.com/cover2.jpg',NULL,'营养学家','2025-11-29 14:05:32','2025-11-29 14:05:32'),(3,'健康早餐搭配技巧','video','3:45','9.7k',0,'健康早餐的搭配技巧...','http://example.com/cover3.jpg',NULL,'美食达人','2025-11-29 14:05:32','2025-11-29 14:05:32'),(4,'减脂餐制作基础','article','12分钟','15.8k',0,'减脂餐制作的基础知识...','http://example.com/cover4.jpg',NULL,'健身教练','2025-11-29 14:05:32','2025-11-29 14:05:32'),(5,'果汁制作小技巧','video','4:15','7.3k',0,'果汁制作的小技巧...','http://example.com/cover5.jpg',NULL,'营养师','2025-11-29 14:05:32','2025-11-29 14:05:32'),(6,'均衡饮食营养知识','article','15分钟','21.2k',0,'均衡饮食的营养知识...','http://example.com/cover6.jpg',NULL,'医学专家','2025-11-29 14:05:32','2025-11-29 14:05:32');
/*!40000 ALTER TABLE `tutorial_backup_20250129` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_behavior`
--

DROP TABLE IF EXISTS `user_behavior`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä¸»é”®ID',
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
  `behavior_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è¡Œä¸ºç±»åž‹: view(æµè§ˆ)/click(ç‚¹å‡»)/order(ä¸‹å•)/favorite(æ”¶è—)/reject(æ‹’ç»)/share(åˆ†äº«)',
  `item_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç‰©å“ç±»åž‹: dish(èœå“)/merchant(å•†å®¶)/recipe(é£Ÿè°±)',
  `item_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç‰©å“ID',
  `context` json DEFAULT NULL COMMENT 'ä¸Šä¸‹æ–‡ä¿¡æ¯: {time, weather, location, device}',
  `duration` int DEFAULT NULL COMMENT 'è¡Œä¸ºæŒç»­æ—¶é•¿(ç§’), æµè§ˆç±»è¡Œä¸ºä½¿ç”¨',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT 'è¡Œä¸ºæ—¶é—´',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`created_time`),
  KEY `idx_item_time` (`item_id`,`created_time`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_user_behavior_type` (`user_id`,`behavior_type`,`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·è¡Œä¸ºè®°å½•è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_behavior`
--

LOCK TABLES `user_behavior` WRITE;
/*!40000 ALTER TABLE `user_behavior` DISABLE KEYS */;
INSERT INTO `user_behavior` VALUES (1,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-29 18:24:27'),(2,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-29 18:24:29'),(3,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-29 18:24:41'),(4,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 10:52:38'),(5,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 10:52:59'),(6,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 10:53:01'),(7,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 10:54:12'),(8,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 10:54:14'),(9,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 11:13:47'),(10,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 11:15:42'),(11,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 11:16:24'),(12,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 11:16:29'),(13,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 11:16:31'),(14,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 14:34:59'),(15,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 20:26:33'),(16,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 20:26:35'),(17,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 20:26:37'),(18,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 21:20:41'),(19,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 21:20:43'),(20,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 21:55:00'),(21,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-30 21:55:02'),(22,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 10:29:29'),(23,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 10:29:31'),(24,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 10:29:33'),(25,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:01:01'),(26,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:01:10'),(27,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:01:13'),(28,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:06:04'),(29,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:06:06'),(30,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:06:09'),(31,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:06:10'),(32,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:06:13'),(33,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:09:56'),(34,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:09:58'),(35,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:10:23'),(36,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:10:25'),(37,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:10:41'),(38,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:10:43'),(39,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:15:39'),(40,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:16:17'),(41,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:16:19'),(42,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:16:47'),(43,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:19:31'),(44,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:19:36'),(45,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 11:46:30'),(46,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 13:20:42'),(47,'3384650106421960','view','recommendation','recommendation_list_home','{}',NULL,'2026-01-31 14:59:29');
/*!40000 ALTER TABLE `user_behavior` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_profile`
--

DROP TABLE IF EXISTS `user_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç”¨æˆ·ID',
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
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ç”¨æˆ·ç”»åƒè¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_profile`
--

LOCK TABLES `user_profile` WRITE;
/*!40000 ALTER TABLE `user_profile` DISABLE KEYS */;
INSERT INTO `user_profile` VALUES (1,'3384650106421960','[]',NULL,'{\"salty\": 0.5, \"spicy\": 0.5, \"sweet\": 0.5}','{\"max\": 50.0, \"min\": 10.0, \"optimal\": 25.0}','{\"fat\": null, \"carbs\": null, \"protein\": 60.0, \"calories\": 2000.0}',NULL,'{\"totalOrders\": 0, \"favCategories\": [], \"avgOrderAmount\": 0.0, \"avgItemsPerOrder\": null, \"mostFrequentTimePeriod\": null}','2026-01-29 10:09:57');
/*!40000 ALTER TABLE `user_profile` ENABLE KEYS */;
UNLOCK TABLES;

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

-- Dump completed on 2026-02-10  2:12:48
