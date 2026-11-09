-- 清理存量智谱模型默认配置。
--
-- 背景：V2 种子里 sys_system_config 的 ai.model 被写成智谱模型名，存量库升级后
-- 这条旧默认值仍然留在活动配置里。
--
-- 处理：把活动配置里残留的智谱模型名中性化为通用默认模型 gpt-4o-mini。
--   1. ai.model 这一行按智谱模型名前缀匹配后替换；
--   2. ai.runtime-config（运行时 JSON 配置）由 AiRuntimeConfigServiceImpl 在启动时
--      按旧 provider 迁移，这里不重复处理 JSON。
--
-- 说明：V2 已被 Flyway 应用，不能回改（validate-on-migrate=true 会校验 checksum），
--       因此以新增迁移的方式修正。WHERE 只命中智谱模型名，替换后再执行不再匹配，
--       迁移可重复执行且不会二次污染。

UPDATE `sys_system_config`
SET `config_value` = 'gpt-4o-mini',
    `updated_at` = CURRENT_TIMESTAMP
WHERE `config_key` = 'ai.model'
  AND `config_value` IS NOT NULL
  AND (
    LOWER(TRIM(`config_value`)) = 'glm'
    OR LOWER(TRIM(`config_value`)) LIKE 'glm-%'
    OR LOWER(TRIM(`config_value`)) LIKE 'zhipu%'
    OR LOWER(TRIM(`config_value`)) LIKE 'charglm%'
    OR LOWER(TRIM(`config_value`)) LIKE 'cogview%'
    OR LOWER(TRIM(`config_value`)) LIKE 'cogvideo%'
  );
