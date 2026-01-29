-- ==========================================
-- 推荐拒绝功能 - 数据库验证脚本
-- ==========================================

-- 1. 检查表是否存在
SELECT '========================================' AS '';
SELECT '1. 检查表是否存在' AS '';
SELECT '========================================' AS '';
SHOW TABLES LIKE 't_reject_recommendation';

-- 2. 查看表结构
SELECT '\n========================================' AS '';
SELECT '2. 查看表结构' AS '';
SELECT '========================================' AS '';
DESC t_reject_recommendation;

-- 3. 查看索引
SELECT '\n========================================' AS '';
SELECT '3. 查看索引' AS '';
SELECT '========================================' AS '';
SHOW INDEX FROM t_reject_recommendation;

-- 4. 测试插入数据
SELECT '\n========================================' AS '';
SELECT '4. 测试插入数据' AS '';
SELECT '========================================' AS '';
INSERT INTO t_reject_recommendation (id, user_id, dish_id, reject_time, reason)
VALUES (
    CONCAT('TEST_', UNIX_TIMESTAMP()),
    '1',
    '1001',
    NOW(),
    '测试拒绝原因'
);

-- 5. 查询插入的数据
SELECT '\n========================================' AS '';
SELECT '5. 查询测试数据' AS '';
SELECT '========================================' AS '';
SELECT * FROM t_reject_recommendation WHERE user_id = '1';

-- 6. 测试统计查询
SELECT '\n========================================' AS '';
SELECT '6. 测试统计查询' AS '';
SELECT '========================================' AS '';
SELECT
    user_id,
    dish_id,
    COUNT(*) as reject_count
FROM t_reject_recommendation
WHERE user_id = '1'
GROUP BY user_id, dish_id;

-- 7. 测试频繁拒绝查询（阈值2）
SELECT '\n========================================' AS '';
SELECT '7. 测试频繁拒绝查询（阈值2）' AS '';
SELECT '========================================' AS '';
SELECT
    dish_id,
    COUNT(*) as reject_count
FROM t_reject_recommendation
WHERE user_id = '1'
GROUP BY dish_id
HAVING COUNT(*) >= 2;

-- 8. 清理测试数据
SELECT '\n========================================' AS '';
SELECT '8. 清理测试数据' AS '';
SELECT '========================================' AS '';
DELETE FROM t_reject_recommendation WHERE user_id = '1' AND dish_id = '1001';

-- 9. 验证清理成功
SELECT '\n========================================' AS '';
SELECT '9. 验证清理成功（应该为空）' AS '';
SELECT '========================================' AS '';
SELECT COUNT(*) as remaining_count FROM t_reject_recommendation WHERE user_id = '1';

SELECT '\n========================================' AS '';
SELECT '✅ 数据库验证完成' AS '';
SELECT '========================================' AS '';
