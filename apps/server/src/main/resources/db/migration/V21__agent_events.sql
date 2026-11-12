-- Agent 事件与通知通道配置（ticket #539）。
--
-- AgentEvent 只承载标识、状态、严重级别、脱敏摘要、行动事项与时间，不含原始反馈、
-- 附件或个人信息。事件按 event_id 去重；通道失败只影响通知，不影响 dsh Ticket。

CREATE TABLE IF NOT EXISTS `ai_agent_event` (
  `event_id`       varchar(128) NOT NULL COMMENT '事件唯一ID（去重键）',
  `event_type`     varchar(64)  NOT NULL COMMENT '事件类型：waiting_for_human/ticket_blocked/ticket_failed/state_changed',
  `severity`       varchar(16)  NOT NULL DEFAULT 'info' COMMENT 'info/warning/critical',
  `ticket_id`      varchar(128) DEFAULT NULL COMMENT 'dsh Ticket 标识',
  `ticket_status`  varchar(64)  DEFAULT NULL COMMENT 'Ticket 状态',
  `summary`        varchar(500) NOT NULL COMMENT '脱敏摘要',
  `action_required` varchar(500) DEFAULT NULL COMMENT '需要人工处理的事项',
  `link`           varchar(512) DEFAULT NULL COMMENT '后台引用链接',
  `occurred_at`    datetime     DEFAULT NULL,
  `created_at`     datetime     DEFAULT NULL,
  PRIMARY KEY (`event_id`),
  KEY `idx_agent_event_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 事件';

CREATE TABLE IF NOT EXISTS `ai_agent_channel_config` (
  `channel_code`   varchar(32)  NOT NULL COMMENT '通道：in_app/websocket/email',
  `enabled`        tinyint      NOT NULL DEFAULT 1 COMMENT '是否启用',
  `event_types`    varchar(255) DEFAULT NULL COMMENT '订阅的事件类型，逗号分隔；空=全部',
  `min_severity`   varchar(16)  NOT NULL DEFAULT 'info' COMMENT '最低严重级别',
  `quiet_start`    varchar(5)   DEFAULT NULL COMMENT '安静时段开始 HH:mm',
  `quiet_end`      varchar(5)   DEFAULT NULL COMMENT '安静时段结束 HH:mm',
  `recipients`     varchar(500) DEFAULT NULL COMMENT '接收人（用户ID，逗号分隔）',
  `retry_limit`    int          NOT NULL DEFAULT 2 COMMENT '失败重试次数',
  `updated_at`     datetime     DEFAULT NULL,
  PRIMARY KEY (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 通知通道配置';

INSERT INTO `ai_agent_channel_config` (`channel_code`, `enabled`, `event_types`, `min_severity`, `retry_limit`, `updated_at`)
SELECT 'in_app', 1, NULL, 'info', 2, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ai_agent_channel_config WHERE channel_code = 'in_app');

INSERT INTO `ai_agent_channel_config` (`channel_code`, `enabled`, `event_types`, `min_severity`, `retry_limit`, `updated_at`)
SELECT 'websocket', 1, NULL, 'info', 2, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM ai_agent_channel_config WHERE channel_code = 'websocket');
