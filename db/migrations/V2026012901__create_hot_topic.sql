-- 创建今日热点表
CREATE TABLE IF NOT EXISTS hot_topic (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    content VARCHAR(500) NOT NULL COMMENT '热点内容',
    priority INT DEFAULT 0 COMMENT '优先级，数值越大优先级越高',
    source_type VARCHAR(20) DEFAULT 'MANUAL' COMMENT '来源类型: MANUAL-手动设置, TUTORIAL-来自教程, AI-AI生成, API-第三方API',
    source_id VARCHAR(100) COMMENT '来源ID（教程ID等）',

    -- 时间控制
    start_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生效开始时间',
    end_date DATETIME COMMENT '生效结束时间',

    -- 状态管理
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-生效, INACTIVE-未生效, EXPIRED-已过期',

    -- 统计数据
    click_count INT DEFAULT 0 COMMENT '点击次数',
    share_count INT DEFAULT 0 COMMENT '分享次数',

    -- 审核信息
    require_review BOOLEAN DEFAULT FALSE COMMENT '是否需要审核',
    review_status VARCHAR(20) DEFAULT 'APPROVED' COMMENT '审核状态: PENDING-待审核, APPROVED-通过, REJECTED-拒绝',
    reviewer_id BIGINT COMMENT '审核人ID',
    review_time DATETIME COMMENT '审核时间',
    review_comment VARCHAR(500) COMMENT '审核意见',

    -- 备注
    remark VARCHAR(500) COMMENT '备注信息',

    -- 时间戳
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',

    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_source_type (source_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='今日热点表';

-- 插入初始示例数据
INSERT INTO hot_topic (id, content, priority, source_type, start_date, status, remark)
VALUES
    ('1', '夏日清凉饮食指南', 10, 'MANUAL', NOW(), 'ACTIVE', '手动配置的夏季饮食热点'),
    ('2', '健康早餐新吃法', 5, 'MANUAL', NOW(), 'ACTIVE', '早餐推荐'),
    ('3', '低卡路里美食精选', 3, 'MANUAL', NOW(), 'ACTIVE', '减脂期饮食推荐');
