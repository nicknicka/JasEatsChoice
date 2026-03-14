-- 营养数据库表（中国食物成分表）
-- 基于《中国食物成分表 标准版（第6版）》

-- 删除已存在的表（如果需要）
-- DROP TABLE IF EXISTS t_nutrition;

CREATE TABLE IF NOT EXISTS t_nutrition (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    food_code VARCHAR(20) UNIQUE COMMENT '食物编码',
    food_name VARCHAR(100) NOT NULL COMMENT '食物名称',
    edible DECIMAL(5,2) COMMENT '可食部(%)',

    -- 能量
    energy_kcal DECIMAL(10,2) COMMENT '能量(千卡)',
    energy_kj DECIMAL(10,2) COMMENT '能量(千焦)',

    -- 主要营养素（每100g可食部）
    water DECIMAL(10,2) COMMENT '水分(g)',
    protein DECIMAL(10,2) COMMENT '蛋白质(g)',
    fat DECIMAL(10,2) COMMENT '脂肪(g)',
    cho DECIMAL(10,2) COMMENT '碳水化合物(g)',
    dietary_fiber DECIMAL(10,2) COMMENT '膳食纤维(g)',

    -- 其他营养素
    cholesterol DECIMAL(10,2) COMMENT '胆固醇(mg)',
    ash DECIMAL(10,2) COMMENT '灰分(g)',

    -- 维生素
    vitamin_a DECIMAL(10,2) COMMENT '维生素A(μgRE)',
    carotene DECIMAL(10,2) COMMENT '胡萝卜素(μg)',
    retinol DECIMAL(10,2) COMMENT '视黄醇(μg)',
    thiamin DECIMAL(10,2) COMMENT '硫胺素(mg)',
    riboflavin DECIMAL(10,2) COMMENT '核黄素(mg)',
    niacin DECIMAL(10,2) COMMENT '烟酸(mg)',
    vitamin_c DECIMAL(10,2) COMMENT '维生素C(mg)',
    vitamin_e_total DECIMAL(10,2) COMMENT '维生素E总(mg)',
    vitamin_e_1 DECIMAL(10,2) COMMENT 'α-维生素E(mg)',
    vitamin_e_2 DECIMAL(10,2) COMMENT 'β+γ-维生素E(mg)',
    vitamin_e_3 DECIMAL(10,2) COMMENT 'δ-维生素E(mg)',

    -- 矿物质
    ca DECIMAL(10,2) COMMENT '钙(mg)',
    p DECIMAL(10,2) COMMENT '磷(mg)',
    k DECIMAL(10,2) COMMENT '钾(mg)',
    na DECIMAL(10,2) COMMENT '钠(mg)',
    mg DECIMAL(10,2) COMMENT '镁(mg)',
    fe DECIMAL(10,2) COMMENT '铁(mg)',
    zn DECIMAL(10,2) COMMENT '锌(mg)',
    se DECIMAL(10,2) COMMENT '硒(μg)',
    cu DECIMAL(10,2) COMMENT '铜(mg)',
    mn DECIMAL(10,2) COMMENT '锰(mg)',

    -- 元数据
    remark VARCHAR(500) COMMENT '备注说明',
    data_source VARCHAR(50) DEFAULT '中国食物成分表第6版' COMMENT '数据来源',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引
    INDEX idx_food_name (food_name),
    INDEX idx_energy_kcal (energy_kcal),
    INDEX idx_protein (protein),
    INDEX idx_fat (fat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中国食物成分表营养数据';

-- 插入测试数据（鸡肉示例）
INSERT INTO t_nutrition (
    id, food_code, food_name, edible,
    energy_kcal, energy_kj,
    water, protein, fat, cho, dietary_fiber,
    cholesterol, ash,
    vitamin_a, carotene, retinol,
    thiamin, riboflavin, niacin, vitamin_c,
    vitamin_e_total, vitamin_e_1, vitamin_e_2, vitamin_e_3,
    ca, p, k, na, mg, fe, zn, se, cu, mn,
    remark, data_source
) VALUES (
    REPLACE(UUID(), '-', ''), '091101x', '鸡 (代表值)', 63.0,
    145.0, 608.0,
    70.5, 20.3, 6.7, 0.9, 0.0,
    106.0, 1.1,
    92.0, 0.0, 92.0,
    0.06, 0.07, 7.54, 0.0,
    1.34, 1.34, 0.37, 0.10,
    13.0, 166.0, 249.0, 62.8, 22.0, 1.8, 1.46, 11.92, 0.09, 0.05,
    '代表值', '中国食物成分表第6版'
);
