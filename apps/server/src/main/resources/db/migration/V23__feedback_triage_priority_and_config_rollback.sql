-- 反馈分诊的决策层 + 配置版本回滚留痕（ticket #546）。
--
-- 决策记录见 issue #546：
--   1A 优先级取值 normal/important/urgent（与公告同款）
--   2A 优先级在分诊时一并设置
--   3A 负责人存 sys_admin.admin_id（bigint，与主键同型），显示时解析 real_name（回落 username）
--   6B 配置历史区分手工修改与回滚
-- 4A 时限不落库（前端按优先级取阈值）、5A 影响面由代码派生，故本迁移不含对应列。

ALTER TABLE `fb_feedback`
  ADD COLUMN `priority`    varchar(16) NOT NULL DEFAULT 'normal'
      COMMENT '分诊优先级：normal-普通, important-重要, urgent-紧急',
  ADD COLUMN `assignee_id` bigint DEFAULT NULL
      COMMENT '当前负责人：sys_admin.admin_id，未认领为空';

ALTER TABLE `ai_agent_runtime_config_history`
  ADD COLUMN `change_kind`    varchar(16) NOT NULL DEFAULT 'manual'
      COMMENT '变更来源：manual-手工修改, rollback-回填旧版后保存',
  ADD COLUMN `source_version` int DEFAULT NULL
      COMMENT '回滚来源版本（change_kind=rollback 时有效）';
