-- 邮件通知通道配置（ticket #540）。默认关闭，避免误发邮件；启用前先配置收件人与 SMTP。

INSERT INTO `ai_agent_channel_config`
  (`channel_code`, `enabled`, `event_types`, `min_severity`, `quiet_start`, `quiet_end`, `recipients`, `retry_limit`, `updated_at`)
SELECT 'email', 0, NULL, 'warning', '22:00', '07:00', NULL, 2, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ai_agent_channel_config WHERE channel_code = 'email');
