-- =============================================
-- "想吃列表"审核系统 - 数据库迁移脚本
-- 版本：V2025_01_30_3
-- 作者：Claude
-- 描述：添加用户想吃列表审核功能
-- =============================================

-- 1. 创建想吃列表项表
CREATE TABLE IF NOT EXISTS `t_wish_list_item` (
  `id` VARCHAR(32) NOT NULL COMMENT '主键ID',
  `user_id` VARCHAR(32) NOT NULL COMMENT '用户ID',
  `merchant_id` VARCHAR(32) DEFAULT NULL COMMENT '商家ID（可选，指定商家的需求）',
  `dish_name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  `dish_image` VARCHAR(500) DEFAULT NULL COMMENT '菜品图片URL',
  `taste_requirement` VARCHAR(200) DEFAULT NULL COMMENT '口味要求',
  `description` TEXT DEFAULT NULL COMMENT '详细描述',
  `recipe_id` VARCHAR(32) DEFAULT NULL COMMENT '参考食谱ID',
  `audit_status` INT DEFAULT 0 COMMENT '审核状态：0-待审核, 1-已通过, 2-已拒绝, 3-申诉中, 4-申诉成功, 5-申诉失败, 6-超时自动通过, 7-已撤回',
  `rejection_reason_code` INT DEFAULT NULL COMMENT '拒绝原因代码',
  `rejection_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因说明',
  `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '商家审核备注',
  `auditor_id` VARCHAR(32) DEFAULT NULL COMMENT '审核人ID',
  `auditor_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `is_appealed` TINYINT(1) DEFAULT 0 COMMENT '是否申诉',
  `appeal_content` TEXT DEFAULT NULL COMMENT '申诉内容',
  `appeal_time` DATETIME DEFAULT NULL COMMENT '申诉时间',
  `appeal_reply` TEXT DEFAULT NULL COMMENT '申诉回复',
  `appeal_reply_time` DATETIME DEFAULT NULL COMMENT '申诉回复时间',
  `appeal_replier_id` VARCHAR(32) DEFAULT NULL COMMENT '申诉回复人ID',
  `expected_available_time` DATETIME DEFAULT NULL COMMENT '期望上架时间',
  `actual_available_time` VARCHAR(200) DEFAULT NULL COMMENT '实际上架时间（审核通过后商家填写的预计时间）',
  `timeout_time` DATETIME NOT NULL COMMENT '超时时间（24小时自动通过）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_merchant_id` (`merchant_id`),
  INDEX `idx_audit_status` (`audit_status`),
  INDEX `idx_timeout_time` (`timeout_time`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='想吃列表项表';

-- =============================================
-- 迁移完成提示
-- =============================================
SELECT '想吃列表审核系统数据库迁移完成！' AS message;

-- 说明：
-- 1. 用户提交想吃需求后，系统设置24小时超时时间
-- 2. 商家24小时内未审核则自动通过（audit_status=6）
-- 3. 商家拒绝时必须选择拒绝原因代码
-- 4. 用户对拒绝结果可发起申诉（audit_status=3）
-- 5. 商家需在12小时内回复申诉（可扩展为定时任务）
