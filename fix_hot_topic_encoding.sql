-- ============================================
-- 修复今日热点表字符集编码问题
-- 执行此SQL可解决中文乱码问题
-- ============================================

-- 1. 查看当前数据库和表的字符集
USE jia_shi_yi_xuan;
SHOW VARIABLES LIKE 'character%';
SHOW VARIABLES LIKE 'collation%';

-- 2. 修改数据库字符集为 utf8mb4（如果还不是）
ALTER DATABASE jia_shi_yi_xuan CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 3. 修改今日热点表的字符集
-- 先查看表结构
SHOW CREATE TABLE hot_topic;

-- 修改表的字符集
ALTER TABLE hot_topic CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. 如果有其他相关表也一并修改（只修改实际存在的表）
-- ALTER TABLE tutorial CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- ALTER TABLE user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- ALTER TABLE merchant CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. 验证修改结果
SHOW TABLE STATUS WHERE Name = 'hot_topic';

-- ============================================
-- 如果数据已经是乱码，需要先清理再重新插入
-- ============================================

-- 备份数据（可选）
-- CREATE TABLE hot_topic_backup AS SELECT * FROM hot_topic;

-- 清空旧数据
-- TRUNCATE TABLE hot_topic;

-- 重新插入正确的数据
-- INSERT INTO hot_topic (content, source_type, source_id, redirect_url, clickable)
-- VALUES ('今日美食推荐：清爽柠檬蜂蜜水', 'ADMIN', NULL, NULL, true);

-- ============================================
-- 查看当前数据（检查是否还有乱码）
-- ============================================
SELECT * FROM hot_topic;
