INSERT INTO sys_permission
  (permission_name, permission_code, resource_type, parent_id, path, icon, sort_order, status, create_time, update_time)
SELECT 'AI 模型', 'admin:setting:ai', 'API', 9, '/api/admin/settings/ai/current', NULL, 8, 'ACTIVE',
       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1 FROM sys_permission WHERE permission_code = 'admin:setting:ai'
);
