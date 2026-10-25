-- 创建系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` VARCHAR(64) PRIMARY KEY COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_name` VARCHAR(100) COMMENT '配置名称',
    `config_group` VARCHAR(50) NOT NULL DEFAULT 'system' COMMENT '配置分组',
    `config_type` VARCHAR(20) DEFAULT 'string' COMMENT '配置类型：string-字符串, number-数字, boolean-布尔, json-JSON对象',
    `description` VARCHAR(500) COMMENT '配置描述',
    `is_system` BOOLEAN DEFAULT FALSE COMMENT '是否系统内置',
    `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态：active-启用, inactive-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_config_group` (`config_group`),
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `config_name`, `config_group`, `config_type`, `description`, `is_system`, `status`) VALUES
-- 系统配置
(REPLACE(UUID(), '-', ''), 'system.name', '佳食宜选', '系统名称', 'system', 'string', '系统名称', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'system.logo', '', '系统LOGO', 'system', 'string', '系统LOGO地址', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'system.copyright', '© 2024 佳食宜选', '版权信息', 'system', 'string', '版权信息', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'system.icp', '', 'ICP备案号', 'system', 'string', 'ICP备案号', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'system.version', '1.0.0', '系统版本', 'system', 'string', '系统版本号', TRUE, 'active'),

-- 支付配置
(REPLACE(UUID(), '-', ''), 'payment.min_recharge', '10', '最低充值金额', 'payment', 'number', '用户最低充值金额（元）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'payment.min_withdraw', '100', '最低提现金额', 'payment', 'number', '用户最低提现金额（元）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'payment.withdraw_fee_rate', '0.01', '提现手续费率', 'payment', 'number', '提现手续费率（0.01表示1%）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'payment.withdraw_fixed_fee', '2', '提现固定手续费', 'payment', 'number', '提现固定手续费（元）', TRUE, 'active'),

-- 订单配置
(REPLACE(UUID(), '-', ''), 'order.auto_cancel_minutes', '15', '订单自动取消时间', 'order', 'number', '未支付订单自动取消时间（分钟）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'order.auto_complete_minutes', '30', '订单自动完成时间', 'order', 'number', '订单自动完成时间（分钟）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'order.max_items', '50', '订单最大商品数', 'order', 'number', '单个订单最多包含商品数', TRUE, 'active'),

-- 用户配置
(REPLACE(UUID(), '-', ''), 'user.default_avatar', 'https://example.com/default-avatar.png', '默认头像', 'user', 'string', '新用户默认头像地址', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'user.register_coupon', '10', '注册赠送优惠券', 'user', 'number', '新用户注册赠送优惠券金额（元）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'user.invite_reward', '5', '邀请奖励', 'user', 'number', '邀请新用户奖励金额（元）', TRUE, 'active'),

-- 短信配置
(REPLACE(UUID(), '-', ''), 'sms.enabled', 'false', '启用短信', 'sms', 'boolean', '是否启用短信发送功能', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'sms.template.register', 'SMS_123456789', '注册短信模板', 'sms', 'string', '用户注册验证码短信模板CODE', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'sms.template.login', 'SMS_123456790', '登录短信模板', 'sms', 'string', '用户登录验证码短信模板CODE', TRUE, 'active'),

-- 邮件配置
(REPLACE(UUID(), '-', ''), 'email.enabled', 'false', '启用邮件', 'email', 'boolean', '是否启用邮件发送功能', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'email.host', 'smtp.example.com', 'SMTP服务器', 'email', 'string', '邮件SMTP服务器地址', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'email.port', '587', 'SMTP端口', 'email', 'number', '邮件SMTP服务器端口', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'email.from', 'noreply@example.com', '发件人邮箱', 'email', 'string', '系统发件人邮箱地址', TRUE, 'active'),

-- AI配置
(REPLACE(UUID(), '-', ''), 'ai.enabled', 'true', '启用AI功能', 'ai', 'boolean', '是否启用AI饮食助手功能', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'ai.model', 'glm-4', 'AI模型', 'ai', 'string', '使用的AI模型', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'ai.max_tokens', '2000', 'AI最大Token数', 'ai', 'number', 'AI回复最大Token数', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'ai.temperature', '0.7', 'AI温度参数', 'ai', 'number', 'AI生成的温度参数（0-1）', TRUE, 'active'),

-- 文件上传配置
(REPLACE(UUID(), '-', ''), 'upload.max_file_size', '10', '最大文件大小', 'upload', 'number', '文件上传最大大小（MB）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'upload.allowed_types', 'jpg,jpeg,png,gif,pdf,doc,docx', '允许上传的文件类型', 'upload', 'string', '允许上传的文件扩展名（逗号分隔）', TRUE, 'active'),
(REPLACE(UUID(), '-', ''), 'upload.image_max_width', '2000', '图片最大宽度', 'upload', 'number', '上传图片最大宽度（像素）', TRUE, 'active');
