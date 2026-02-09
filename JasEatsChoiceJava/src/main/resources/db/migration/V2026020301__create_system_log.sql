-- 创建系统日志表
CREATE TABLE IF NOT EXISTS t_system_log (
    log_id VARCHAR(32) PRIMARY KEY COMMENT '日志ID',
    operation_type VARCHAR(20) COMMENT '操作类型：LOGIN-登录, LOGOUT-登出, CREATE-创建, UPDATE-更新, DELETE-删除, QUERY-查询, EXPORT-导出, OTHER-其他',
    module VARCHAR(50) COMMENT '操作模块：USER-用户管理, MERCHANT-商家管理, ORDER-订单管理, DISH-菜品管理, ADMIN-管理员管理, FINANCE-财务管理, STATISTICS-统计管理, SYSTEM-系统管理, HOT_TOPIC-热点管理',
    description VARCHAR(500) COMMENT '操作描述',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人名称',
    operator_type VARCHAR(20) COMMENT '操作人类型：ADMIN-管理员, USER-用户, MERCHANT-商家, SYSTEM-系统',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    execute_time BIGINT COMMENT '执行时长(毫秒)',
    ip VARCHAR(50) COMMENT 'IP地址',
    status VARCHAR(20) COMMENT '操作状态：SUCCESS-成功, FAILED-失败',
    error_message TEXT COMMENT '错误信息',
    browser VARCHAR(100) COMMENT '浏览器类型',
    os VARCHAR(100) COMMENT '操作系统',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    INDEX idx_operator_id (operator_id),
    INDEX idx_module (module),
    INDEX idx_operation_type (operation_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';
