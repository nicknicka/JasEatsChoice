-- Agent 运行时配置与版本历史（ticket #538）。
--
-- 模型 key 不落库、不落日志、不回显：只写文件型密钥存储（见 FileSecretStore）。
-- 硬边界（不合并、不部署、不访问生产数据）由代码固定，页面无法关闭。

CREATE TABLE IF NOT EXISTS `ai_agent_runtime_config` (
  `config_id`        varchar(32)  NOT NULL DEFAULT 'default' COMMENT '单例配置ID',
  `provider`         varchar(64)  DEFAULT NULL COMMENT '模型供应商（白名单内）',
  `model`            varchar(128) DEFAULT NULL COMMENT '模型名',
  `endpoint`         varchar(255) DEFAULT NULL COMMENT 'endpoint',
  `data_policy_ack`  tinyint      NOT NULL DEFAULT 0 COMMENT '数据政策确认',
  `version`          int          NOT NULL DEFAULT 0 COMMENT '版本号',
  `updated_by`       varchar(64)  DEFAULT NULL COMMENT '最近修改人',
  `updated_at`       datetime     DEFAULT NULL,
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 运行时配置';

CREATE TABLE IF NOT EXISTS `ai_agent_runtime_config_history` (
  `history_id`      varchar(64) NOT NULL COMMENT '历史ID',
  `config_id`       varchar(32) NOT NULL COMMENT '配置ID',
  `version`         int         NOT NULL COMMENT '版本号',
  `provider`        varchar(64)  DEFAULT NULL,
  `model`           varchar(128) DEFAULT NULL,
  `endpoint`        varchar(255) DEFAULT NULL,
  `data_policy_ack` tinyint     NOT NULL DEFAULT 0,
  `updated_by`      varchar(64) DEFAULT NULL,
  `created_at`      datetime    DEFAULT NULL,
  PRIMARY KEY (`history_id`),
  KEY `idx_agent_runtime_history` (`config_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 运行时配置历史';

-- 页面只对 admin:agent:runtime 开放；只有 super 角色默认可用，授予其他角色走后台分配。
INSERT INTO `sys_permission`
  (permission_name, permission_code, resource_type, parent_id, path, icon, sort_order, status, created_at, updated_at)
SELECT 'Agent 运行时', 'admin:agent:runtime', 'API', NULL, '/api/admin/agent/runtime', NULL, 25, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'admin:agent:runtime');
