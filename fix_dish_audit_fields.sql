-- 给t_dish表添加审核相关字段
-- 执行时间：2026-01-31

USE jia_shi_yi_xuan;

-- 添加审核状态字段
ALTER TABLE t_dish
ADD COLUMN audit_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已拒绝'
AFTER status;

-- 添加审核意见字段
ALTER TABLE t_dish
ADD COLUMN audit_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见'
AFTER audit_status;

-- 添加审核时间字段
ALTER TABLE t_dish
ADD COLUMN audit_time DATETIME DEFAULT NULL COMMENT '审核时间'
AFTER audit_comment;

-- 添加审核管理员ID字段
ALTER TABLE t_dish
ADD COLUMN audit_admin_id BIGINT DEFAULT NULL COMMENT '审核管理员ID'
AFTER audit_time;

-- 添加索引
ALTER TABLE t_dish
ADD INDEX idx_audit_status (audit_status);

-- 将现有已上架的菜品标记为已通过审核
UPDATE t_dish
SET audit_status = 'APPROVED',
    audit_time = NOW(),
    audit_comment = '系统默认通过（历史数据）'
WHERE status = 1;

-- 查看更新结果
SELECT id, name, status, audit_status, audit_comment, audit_time
FROM t_dish
LIMIT 10;
