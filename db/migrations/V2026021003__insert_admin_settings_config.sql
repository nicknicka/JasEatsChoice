-- 插入管理员系统设置页面所需的配置数据

-- 系统设置分组配置
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_name`, `config_group`, `config_type`, `description`, `is_system`, `status`) VALUES
-- 系统设置
(REPLACE(UUID(), '-', ''), 'system.site.name', '佳食宜选', '网站名称', 'system', 'string', '系统网站的名称', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'system.user.upload.enabled', 'false', '允许用户上传教程', 'system', 'boolean', '开启后，普通用户可以上传自己的教程', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'system.tutorial.review.required', 'true', '教程审核机制', 'system', 'boolean', '商家和AI生成的教程需要审核后发布', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'system.ai.tutorial.enabled', 'true', 'AI教程生成', 'system', 'boolean', '启用AI自动生成教程内容', TRUE, 'active'),

-- 通知设置
(REPLACE(UUID(), '-', ''), 'notification.email.enabled', 'true', '邮件通知', 'notification', 'boolean', '审核结果等重要事件将通过邮件通知', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'notification.sms.enabled', 'false', '短信通知', 'notification', 'boolean', '紧急通知将通过短信发送', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'notification.review.notify.enabled', 'true', '审核通知', 'notification', 'boolean', '商家提交审核时通知管理员', TRUE, 'active'),

-- 安全设置
(REPLACE(UUID(), '-', ''), 'security.session.timeout', '30', '会话超时时间', 'security', 'number', '会话超时时间（分钟）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'security.login.maxAttempts', '5', '最大登录尝试次数', 'security', 'number', '超过次数后将锁定账户', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'security.password.minLength', '6', '密码最小长度', 'security', 'number', '用户密码的最小长度要求', TRUE, 'active')

ON DUPLICATE KEY UPDATE
  config_value = VALUES(config_value),
  config_name = VALUES(config_name),
  description = VALUES(description);
