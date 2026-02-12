-- 群订单加菜表
CREATE TABLE IF NOT EXISTS t_group_order_addition (
    id VARCHAR(32) PRIMARY KEY COMMENT '加菜记录ID',
    group_order_id VARCHAR(32) NOT NULL COMMENT '群订单ID',
    group_id VARCHAR(32) NOT NULL COMMENT '群组ID',
    user_id VARCHAR(32) NOT NULL COMMENT '加菜用户ID',
    user_name VARCHAR(50) COMMENT '加菜用户姓名',
    dishes JSON NOT NULL COMMENT '加菜菜品列表',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '加菜总额',
    status VARCHAR(30) NOT NULL DEFAULT 'pending_review' COMMENT '状态：pending_review(待审核)/approved_pending_payment(审核通过待支付)/rejected(已驳回)/paid(已支付)',
    request_time DATETIME NOT NULL COMMENT '请求时间',
    review_time DATETIME COMMENT '审核时间',
    reject_reason VARCHAR(200) COMMENT '驳回原因',
    check_diet_restrictions JSON COMMENT '饮食禁忌检查结果',
    related_order_id VARCHAR(32) COMMENT '关联的加菜订单ID',
    pay_time DATETIME COMMENT '支付时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_group_order_id (group_order_id),
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_request_time (request_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群订单加菜表';
