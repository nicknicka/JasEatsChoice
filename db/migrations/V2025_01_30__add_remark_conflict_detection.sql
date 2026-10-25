-- =============================================
-- 备注冲突智能提示功能 - 数据库迁移脚本
-- 版本：V2025_01_30_2
-- 作者：Claude
-- 描述：添加备注冲突检测和智能提示功能
-- =============================================

-- 1. 创建食材冲突规则表
CREATE TABLE IF NOT EXISTS `t_ingredient_conflict_rule` (
  `id` VARCHAR(32) NOT NULL COMMENT '规则ID',
  `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
  `conflict_type` VARCHAR(20) NOT NULL COMMENT '冲突类型：ALLERGY-过敏, INCOMPATIBLE-食材冲突, CUISINE-烹饪禁忌',
  `main_ingredients` JSON NOT NULL COMMENT '主要食材（JSON数组）',
  `conflict_tags` JSON NOT NULL COMMENT '冲突标签（JSON数组）',
  `severity` INT DEFAULT 2 COMMENT '严重程度：1-低, 2-中, 3-高',
  `priority` INT DEFAULT 3 COMMENT '推荐优先级：1-高（红色）, 2-中高（黄色）, 3-中（蓝色）, 4-低（灰色）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '冲突描述',
  `suggestion` VARCHAR(500) DEFAULT NULL COMMENT '建议内容',
  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_conflict_type` (`conflict_type`),
  INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食材冲突规则表';

-- 2. 插入默认的冲突规则（使用MD5生成32位ID）
INSERT INTO `t_ingredient_conflict_rule`
(`id`, `rule_name`, `conflict_type`, `main_ingredients`, `conflict_tags`, `severity`, `priority`, `description`, `suggestion`)
VALUES
-- 辣椒相关冲突规则
(MD5('rule1'), '免辣与辣椒冲突', 'INCOMPATIBLE',
 JSON_ARRAY('辣椒', '辣椒粉', '干辣椒', '鲜辣椒', '辣椒油', '豆瓣酱', '火锅底料'),
 JSON_ARRAY('mild_no_spicy'),
 3, 2,
 '该菜品包含辣椒成分，您选择了"免辣"标签',
 '建议：如不能吃辣，请选择其他菜品'),

(MD5('rule2'), '微辣与辣椒冲突', 'INCOMPATIBLE',
 JSON_ARRAY('辣椒', '辣椒粉', '干辣椒', '鲜辣椒', '辣椒油'),
 JSON_ARRAY('mild_spicy'),
 2, 3,
 '该菜品为辣味菜品，您选择了"微辣"标签',
 '建议：商家会适当减少辣椒用量'),

-- 葱姜蒜冲突规则
(MD5('rule3'), '不要葱与葱冲突', 'INCOMPATIBLE',
 JSON_ARRAY('葱', '大葱', '小葱', '青葱', '葱白', '葱花'),
 JSON_ARRAY('no_onion'),
 2, 3,
 '该菜品包含葱，您选择了"不要葱"标签',
 '建议：商家会不上葱花'),

(MD5('rule4'), '不要蒜与蒜冲突', 'INCOMPATIBLE',
 JSON_ARRAY('蒜', '大蒜', '蒜蓉', '蒜泥', '蒜末'),
 JSON_ARRAY('no_garlic'),
 2, 3,
 '该菜品包含蒜，您选择了"不要蒜"标签',
 '建议：商家会不上蒜'),

(MD5('rule5'), '不要姜与姜冲突', 'INCOMPATIBLE',
 JSON_ARRAY('姜', '生姜', '老姜', '姜片', '姜丝', '姜末'),
 JSON_ARRAY('no_ginger'),
 2, 3,
 '该菜品包含姜，您选择了"不要姜"标签',
 '建议：商家会不上姜'),

-- 香菜冲突规则
(MD5('rule6'), '不要香菜与香菜冲突', 'INCOMPATIBLE',
 JSON_ARRAY('香菜', '芫荽'),
 JSON_ARRAY('no_coriander'),
 2, 3,
 '该菜品包含香菜，您选择了"不要香菜"标签',
 '建议：商家会不上香菜'),

-- 芝麻冲突规则
(MD5('rule7'), '不要芝麻与芝麻冲突', 'INCOMPATIBLE',
 JSON_ARRAY('芝麻', '白芝麻', '黑芝麻', '芝麻酱', '芝麻油'),
 JSON_ARRAY('no_sesame'),
 2, 3,
 '该菜品包含芝麻，您选择了"不要芝麻"标签',
 '建议：商家会不上芝麻'),

-- 醋冲突规则
(MD5('rule8'), '不要醋与醋冲突', 'INCOMPATIBLE',
 JSON_ARRAY('醋', '陈醋', '米醋', '白醋', '香醋'),
 JSON_ARRAY('no_vinegar'),
 2, 3,
 '该菜品需要醋，您选择了"不要醋"标签',
 '建议：商家会不上醋或醋分装'),

-- 花生过敏警告
(MD5('rule9'), '花生过敏警告', 'ALLERGY',
 JSON_ARRAY('花生', '花生酱', '花生油'),
 JSON_ARRAY('allergy_peanut'),
 3, 1,
 '该菜品包含花生成分，为常见过敏源',
 '警告：如有花生过敏，请勿选择此菜品或提前告知商家');

-- =============================================
-- 迁移完成提示
-- =============================================
SELECT '备注冲突智能提示功能数据库迁移完成！' AS message;
