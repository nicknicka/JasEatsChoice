-- 修复教程表中的中文乱码问题
-- 执行时间：2026-01-31

USE jia_shi_yi_xuan;

-- 修复教程标题、作者等字段的中文乱码
UPDATE tutorial SET title = '青木瓜沙拉制作教程' WHERE id = 1;
UPDATE tutorial SET author = '美食专家' WHERE id = 1;

UPDATE tutorial SET title = '夏日低卡饮食指南' WHERE id = 2;
UPDATE tutorial SET author = '营养学家' WHERE id = 2;

UPDATE tutorial SET title = '健康早餐搭配技巧' WHERE id = 3;
UPDATE tutorial SET author = '美食达人' WHERE id = 3;

UPDATE tutorial SET title = '减脂餐制作基础' WHERE id = 4;
UPDATE tutorial SET author = '健身教练' WHERE id = 4;

UPDATE tutorial SET title = '果汁制作小技巧' WHERE id = 5;
UPDATE tutorial SET author = '营养师' WHERE id = 5;

UPDATE tutorial SET title = '均衡饮食营养知识' WHERE id = 6;
UPDATE tutorial SET author = '医学专家' WHERE id = 6;

UPDATE tutorial SET title = '清爽木瓜沙拉制作教程' WHERE id = 7;
UPDATE tutorial SET author = '官方营养师' WHERE id = 7;

UPDATE tutorial SET title = '夏日低卡饮食指南' WHERE id = 8;
UPDATE tutorial SET author = '官方营养师' WHERE id = 8;

UPDATE tutorial SET title = '健康早餐搭配技巧' WHERE id = 9;
UPDATE tutorial SET author = '官方营养师' WHERE id = 9;

-- 验证修复结果
SELECT id, title, author, status, review_status
FROM tutorial
ORDER BY id
LIMIT 10;
