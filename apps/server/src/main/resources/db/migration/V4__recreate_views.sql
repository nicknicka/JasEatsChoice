-- V4: 重建视图（V3 改名后视图引用了旧表名）
-- v_active_festivals: 活跃节日列表，计算 is_current 和 days_until_festival
-- v_user_behavior_stats: 用户行为统计（已在 V3 重建）

-- ===== v_active_festivals =====
-- 基于 cnt_festival 表，计算 is_current（当前是否在节日期间）和 days_until_festival（距离下一个节日的天数）
-- 支持年度循环节日（year IS NULL）和特定年份节日（year IS NOT NULL）

DROP VIEW IF EXISTS `v_active_festivals`;

CREATE VIEW `v_active_festivals` AS
SELECT
    f.id,
    f.festival_name,
    f.festival_type,
    f.festival_date,
    f.year,
    f.description,
    f.icon,
    f.background_image,
    f.theme_color,
    f.start_date,
    f.end_date,
    f.is_active,
    f.sort_order,
    f.create_time,
    f.update_time,
    -- is_current: 节日是否当前生效
    -- 简化逻辑：is_active = 1 且（今天在 start_date~end_date 之间，或今天就是节日当天）
    CASE
        WHEN f.is_active = 0 THEN 0
        -- 有明确日期范围的季节性节日
        WHEN f.start_date IS NOT NULL AND f.end_date IS NOT NULL
            AND CURDATE() BETWEEN f.start_date AND f.end_date THEN 1
        -- MM-DD 格式的固定日期节日，今年的
        WHEN f.festival_date IS NOT NULL
            AND DATE(CONCAT(YEAR(CURDATE()), '-', f.festival_date)) = CURDATE() THEN 1
        -- MM-DD 格式的固定日期节日，明年的（跨年场景）
        WHEN f.festival_date IS NOT NULL
            AND f.year IS NULL
            AND DATE(CONCAT(YEAR(CURDATE()) + 1, '-', f.festival_date)) = CURDATE() THEN 1
        ELSE 0
    END AS is_current,
    -- days_until_festival: 距离下一个节日的天数
    -- 简化逻辑：基于 festival_date 计算今年或明年的日期
    CASE
        WHEN f.is_active = 0 THEN NULL
        WHEN f.festival_date IS NULL THEN NULL
        ELSE
            DATEDIFF(
                CASE
                    -- 今年的节日还没到
                    WHEN DATE(CONCAT(YEAR(CURDATE()), '-', f.festival_date)) >= CURDATE()
                    THEN DATE(CONCAT(YEAR(CURDATE()), '-', f.festival_date))
                    -- 今年的已经过了，且是年度循环节日，算明年的
                    WHEN f.year IS NULL
                    THEN DATE(CONCAT(YEAR(CURDATE()) + 1, '-', f.festival_date))
                    -- 今年的过了且不是年度循环，返回 NULL
                    ELSE NULL
                END,
                CURDATE()
            )
    END AS days_until_festival
FROM `cnt_festival` f
WHERE f.is_active = 1;
