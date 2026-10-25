-- 创建定时任务执行日志表
CREATE TABLE IF NOT EXISTS `t_scheduled_task_log` (
  `id` VARCHAR(64) NOT NULL COMMENT '日志ID',
  `task_id` VARCHAR(64) COMMENT '任务ID',
  `task_name` VARCHAR(255) COMMENT '任务名称',
  `task_code` VARCHAR(100) COMMENT '任务代码',
  `start_time` DATETIME COMMENT '执行开始时间',
  `end_time` DATETIME COMMENT '执行结束时间',
  `duration` BIGINT COMMENT '执行时长（毫秒）',
  `execute_status` VARCHAR(50) COMMENT '执行状态：SUCCESS-成功, FAILED-失败, TIMEOUT-超时',
  `result_message` TEXT COMMENT '执行结果信息',
  `error_message` TEXT COMMENT '错误信息',
  `exception_stack` TEXT COMMENT '异常堆栈',
  `execute_params` TEXT COMMENT '执行参数（JSON格式）',
  `execute_result` TEXT COMMENT '执行返回结果（JSON格式）',
  `server_ip` VARCHAR(50) COMMENT '服务器IP',
  `server_hostname` VARCHAR(100) COMMENT '服务器主机名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_task_code` (`task_code`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_execute_status` (`execute_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务执行日志表';
