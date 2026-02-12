-- ========================================
-- 菜品表(t_dish)结构优化方案
-- 执行时间：2026-01-31
-- ========================================

USE jia_shi_yi_xuan;

-- ========================================
-- 优化1：移除 is_fast_food 字段
-- 原因：与 step_template 功能重复
-- is_fast_food = 1 对应 step_template = 'FAST'
-- is_fast_food = 0 对应 step_template = 'NORMAL'
-- ========================================

-- 备份数据（可选）
-- CREATE TABLE t_dish_backup_20260131 AS SELECT * FROM t_dish;

-- 删除冗余字段（先检查字段是否存在）
-- 使用存储过程方式检查字段存在性
SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
    AND TABLE_NAME = 't_dish'
    AND COLUMN_NAME = 'is_fast_food'
);

-- 删除字段（如果存在）
SET @sql = IF(@column_exists > 0,
    'ALTER TABLE t_dish DROP COLUMN is_fast_food',
    'SELECT "字段 is_fast_food 不存在，跳过删除" AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 优化2：字段重命名，提升语义清晰度
-- ========================================

-- 将 status 重命名为 is_online，更清晰地表达上架/下架状态
ALTER TABLE t_dish CHANGE status is_online TINYINT(1) DEFAULT 1 COMMENT '是否上架：1-上架，0-下架';

-- 将 cooking_minutes 重命名为 estimated_cooking_minutes，更准确表达预估时间
ALTER TABLE t_dish CHANGE cooking_minutes estimated_cooking_minutes INT DEFAULT 15 COMMENT '预估烹饪时长（分钟）';

-- ========================================
-- 优化3：调整字段类型和约束
-- ========================================

-- step_template 添加默认值和注释
ALTER TABLE t_dish MODIFY COLUMN step_template VARCHAR(20) DEFAULT 'NORMAL' COMMENT '烹饪流程模板：NORMAL-正餐流程，FAST-快餐流程，CUSTOM-自定义流程';

-- category 添加默认值
ALTER TABLE t_dish MODIFY COLUMN category VARCHAR(50) NOT NULL DEFAULT '其他' COMMENT '菜品分类';

-- stock 添加默认值，并改为无限制表示
ALTER TABLE t_dish MODIFY COLUMN stock INT DEFAULT -1 COMMENT '库存数量：-1表示不限量，>=0表示实际库存';

-- ========================================
-- 优化4：添加有用的索引
-- ========================================

-- 组合索引：商家+审核状态+创建时间（审核列表查询）
ALTER TABLE t_dish ADD INDEX idx_merchant_audit_time (merchant_id, audit_status, create_time);

-- 组合索引：分类+是否上架+评分（菜品推荐查询）
ALTER TABLE t_dish ADD INDEX idx_category_online_rating (category, is_online, avg_rating);

-- 组合索引：浏览次数+订单次数（热门菜品查询）
ALTER TABLE t_dish ADD INDEX idx_view_order_count (view_count DESC, order_count DESC);

-- ========================================
-- 优化5：数据清理和迁移
-- ========================================

-- 更新 step_template 的默认值
UPDATE t_dish SET step_template = 'NORMAL' WHERE step_template IS NULL OR step_template = '';

-- 更新库存默认值
UPDATE t_dish SET stock = -1 WHERE stock IS NULL;

-- 更新分类为空的数据
UPDATE t_dish SET category = '其他' WHERE category IS NULL OR category = '';

-- ========================================
-- 优化6：添加虚拟字段（Generated Column）
-- MySQL 5.7+ 支持，用于自动计算字段
-- ========================================

-- 添加一个是否快餐的虚拟字段（用于查询方便）
ALTER TABLE t_dish ADD COLUMN is_fast_food_virtual TINYINT(1) GENERATED ALWAYS AS
    (CASE WHEN step_template = 'FAST' THEN 1 ELSE 0 END) VIRTUAL COMMENT '是否为快餐（虚拟字段，自动计算）';

-- ========================================
-- 优化后的表结构查询
-- ========================================

-- 查看优化后的表结构
DESCRIBE t_dish;

-- 查看索引
SHOW INDEX FROM t_dish;

-- ========================================
-- 回滚方案（如果需要）
-- ========================================

/*
-- 恢复 is_fast_food 字段
ALTER TABLE t_dish ADD COLUMN is_fast_food TINYINT(1) DEFAULT 0 COMMENT '是否为快餐：1-是，0-否' AFTER estimated_cooking_minutes;

-- 恢复 status 字段名
ALTER TABLE t_dish CHANGE is_online status TINYINT(1) DEFAULT 1 COMMENT '菜品状态（0-下架/1-上架）';

-- 恢复 cooking_minutes 字段名
ALTER TABLE t_dish CHANGE estimated_cooking_minutes cooking_minutes INT DEFAULT 15 COMMENT '标准烹饪时长（分钟）';

-- 删除虚拟字段
ALTER TABLE t_dish DROP COLUMN is_fast_food_virtual;

-- 删除新增的索引
ALTER TABLE t_dish DROP INDEX idx_merchant_audit_time;
ALTER TABLE t_dish DROP INDEX idx_category_online_rating;
ALTER TABLE t_dish DROP INDEX idx_view_order_count;
*/
