-- =====================================================
-- 修复群聊创建问题 - 修改 t_group 表的 id 字段类型
-- 数据库: jia_shi_yi_xuan
-- 日期: 2026-01-20
-- =====================================================

USE jia_shi_yi_xuan;

-- 1. 查看 t_group 表当前结构
DESC t_group;

-- 2. 修改 t_group 表的 id 字段类型为 VARCHAR(64)
ALTER TABLE t_group MODIFY COLUMN id VARCHAR(64) NOT NULL COMMENT '群组ID';

-- 3. 如果有 t_group_member 表，也修改 group_id 字段
-- 检查表是否存在
-- ALTER TABLE t_group_member MODIFY COLUMN group_id VARCHAR(64) NOT NULL COMMENT '群组ID';

-- 4. 验证修改结果
DESC t_group;

-- 5. 查看现有数据（如果有）
SELECT * FROM t_group;

-- =====================================================
-- 执行完成后，请重启 Java 应用
-- =====================================================
