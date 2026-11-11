-- 用户反馈基础域（规格 #530 / ticket #532）。
--
-- 一条反馈是聚合根：可信身份来自 JWT，状态与处理结果编码分离，订单/商家关联可空，
-- 联系方式与设备元数据只作内部排查使用，永不进入 GitHub Issue、dsh 上下文或通知正文。
-- 消息、附件、分诊审计、Issue 关联由后续 ticket 各自的新迁移补齐。

CREATE TABLE IF NOT EXISTS `fb_feedback` (
  `feedback_id`     varchar(64)  NOT NULL COMMENT '反馈ID',
  `user_id`         varchar(64)  NOT NULL COMMENT '提交用户ID（来自可信 JWT）',
  `feedback_type`   varchar(32)  NOT NULL COMMENT '类型：bug/feature/ui/performance/order/payment/other',
  `content`         text         NOT NULL COMMENT '反馈正文',
  `status`          varchar(16)  NOT NULL DEFAULT 'pending' COMMENT '生命周期状态：pending/triaging/needs_info/accepted/rejected/resolved',
  `resolution_code` varchar(32)  DEFAULT NULL COMMENT '处理结果编码：duplicate/wontfix/not_reproducible/fixed/answered',
  `order_id`        varchar(64)  DEFAULT NULL COMMENT '关联订单ID（内部排查用，可空）',
  `merchant_id`     varchar(64)  DEFAULT NULL COMMENT '关联商家ID（内部排查用，可空）',
  `contact`         varchar(128) DEFAULT NULL COMMENT '联系方式（内部排查用，可空）',
  `device_info`     varchar(1024) DEFAULT NULL COMMENT '设备元数据 JSON（内部排查用）',
  `environment`     varchar(255) DEFAULT NULL COMMENT '环境摘要（供脱敏桥接使用）',
  `created_at`      datetime     DEFAULT NULL,
  `updated_at`      datetime     DEFAULT NULL,
  PRIMARY KEY (`feedback_id`),
  KEY `idx_fb_feedback_user` (`user_id`, `created_at`),
  KEY `idx_fb_feedback_status` (`status`, `created_at`),
  KEY `idx_fb_feedback_order` (`order_id`),
  KEY `idx_fb_feedback_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈';
