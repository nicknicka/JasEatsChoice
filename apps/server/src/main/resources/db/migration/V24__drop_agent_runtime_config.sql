-- 瘦身：删除未被任何消费者使用的 Agent 运行时配置（ticket #552）。
--
-- agent 的模型 / 密钥 / 策略配置真相在 DSH（另一个运行时），JAS 不再保存。
-- 保留 sys_permission 的 admin:agent:runtime：状态页仍用它做只读访问控制。

DROP TABLE IF EXISTS `ai_agent_runtime_config_history`;
DROP TABLE IF EXISTS `ai_agent_runtime_config`;
