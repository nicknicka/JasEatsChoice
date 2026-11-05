-- 首页「今日热点」种子数据修复。
--
-- 背景：V2 种子里 seedtopic 系列演示话题的 end_date 固定为 2026-06-20，到期后
-- HotTopicMapper.selectActiveHotTopic 只剩早期手工测试留下的脏数据（content 为
-- 'test' / '888999'），首页热点条因此展示无意义内容。
--
-- 处理：
--   1. 演示话题改为不过期，保证首页始终有可读文案；
--   2. 停用早期脏数据，避免其抢占热点位。
--
-- 说明：V2 已被 Flyway 应用，不能回改（validate-on-migrate=true 会校验 checksum），
--       因此以新增迁移的方式修正。

UPDATE `cnt_hot_topic`
SET `end_date` = NULL
WHERE `id` LIKE 'seedtopic%'
  AND `status` = 'ACTIVE';

UPDATE `cnt_hot_topic`
SET `status` = 'INACTIVE'
WHERE `content` IN ('test', '888999');
