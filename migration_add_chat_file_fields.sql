-- 为t_chat_msg表添加文件上传相关字段
-- 执行日期: 2025-01-25
-- 说明: 支持聊天消息中的图片和文件上传功能

USE jia_shi_yi_xuan;

-- 添加文件URL字段
ALTER TABLE t_chat_msg ADD COLUMN file_url VARCHAR(512) NULL COMMENT '文件URL（图片/文件消息时使用）' AFTER create_time;

-- 添加文件名字段
ALTER TABLE t_chat_msg ADD COLUMN file_name VARCHAR(255) NULL COMMENT '文件名（文件消息时使用）' AFTER file_url;

-- 添加文件大小字段
ALTER TABLE t_chat_msg ADD COLUMN file_size BIGINT NULL COMMENT '文件大小（字节）' AFTER file_name;

-- 添加文件MIME类型字段
ALTER TABLE t_chat_msg ADD COLUMN file_type VARCHAR(100) NULL COMMENT '文件MIME类型' AFTER file_size;

-- 为文件URL字段添加索引，提高查询性能
ALTER TABLE t_chat_msg ADD INDEX idx_file_url (file_url);

-- 验证字段是否添加成功
SELECT
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'jia_shi_yi_xuan'
  AND TABLE_NAME = 't_chat_msg'
  AND COLUMN_NAME IN ('file_url', 'file_name', 'file_size', 'file_type');
