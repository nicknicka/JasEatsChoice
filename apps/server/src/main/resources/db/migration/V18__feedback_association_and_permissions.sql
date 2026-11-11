-- 反馈关联修正审计 + 管理端反馈权限（ticket #534 / #535 共用）。
--
-- 管理员修正订单/商家关联、分诊、解决都写入同一张审计表，禁止静默改数据。
-- 五个权限码对应管理端的 list / detail / triage / reply / resolve 动作。

CREATE TABLE IF NOT EXISTS `fb_feedback_audit` (
  `audit_id`    varchar(64)  NOT NULL COMMENT '审计ID',
  `feedback_id` varchar(64)  NOT NULL COMMENT '所属反馈ID',
  `action`      varchar(32)  NOT NULL COMMENT '动作：ASSOCIATION/TRIAGE/RESOLVE/REPLY',
  `actor_type`  varchar(16)  NOT NULL COMMENT '操作者类型：ADMIN/SYSTEM',
  `actor_id`    varchar(64)  DEFAULT NULL COMMENT '操作者ID',
  `detail`      varchar(1000) DEFAULT NULL COMMENT '变更详情',
  `created_at`  datetime     DEFAULT NULL,
  PRIMARY KEY (`audit_id`),
  KEY `idx_fb_audit_feedback` (`feedback_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈操作审计';

INSERT INTO `sys_permission`
  (permission_name, permission_code, resource_type, parent_id, path, icon, sort_order, status, created_at, updated_at)
SELECT '反馈列表', 'admin:feedback:list', 'API', NULL, '/api/admin/feedbacks', NULL, 20, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'admin:feedback:list');

INSERT INTO `sys_permission`
  (permission_name, permission_code, resource_type, parent_id, path, icon, sort_order, status, created_at, updated_at)
SELECT '反馈详情', 'admin:feedback:detail', 'API', NULL, '/api/admin/feedbacks/{feedbackId}', NULL, 21, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'admin:feedback:detail');

INSERT INTO `sys_permission`
  (permission_name, permission_code, resource_type, parent_id, path, icon, sort_order, status, created_at, updated_at)
SELECT '反馈分诊', 'admin:feedback:triage', 'API', NULL, '/api/admin/feedbacks/{feedbackId}/triage', NULL, 22, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'admin:feedback:triage');

INSERT INTO `sys_permission`
  (permission_name, permission_code, resource_type, parent_id, path, icon, sort_order, status, created_at, updated_at)
SELECT '反馈回复', 'admin:feedback:reply', 'API', NULL, '/api/admin/feedbacks/{feedbackId}/messages', NULL, 23, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'admin:feedback:reply');

INSERT INTO `sys_permission`
  (permission_name, permission_code, resource_type, parent_id, path, icon, sort_order, status, created_at, updated_at)
SELECT '反馈解决', 'admin:feedback:resolve', 'API', NULL, '/api/admin/feedbacks/{feedbackId}/resolve', NULL, 24, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'admin:feedback:resolve');
