-- 修复钱包表中的 version 字段，将 NULL 值设置为 0
UPDATE t_wallet SET version = 0 WHERE version IS NULL;

-- 查询修复结果
SELECT id, user_id, balance, version, status FROM t_wallet;
