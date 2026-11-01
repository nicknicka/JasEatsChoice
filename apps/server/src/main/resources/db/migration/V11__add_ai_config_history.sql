-- AI 配置历史使用专用表，避免普通系统配置权限通过 system_config 接口读取。
CREATE TABLE `ai_config_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `history_json` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'AI配置历史JSON',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `ai_config_history_idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
