-- 反馈消息与附件（ticket #533）。
--
-- 一条反馈下的消息历史区分「用户可见」与「内部备注」；内部备注永不出现在用户端。
-- 附件只保存元数据与相对路径，实际文件落在 Git 外的运行时上传目录。

CREATE TABLE IF NOT EXISTS `fb_feedback_message` (
  `message_id`  varchar(64)  NOT NULL COMMENT '消息ID',
  `feedback_id` varchar(64)  NOT NULL COMMENT '所属反馈ID',
  `author_type` varchar(16)  NOT NULL COMMENT '作者类型：USER/ADMIN/SYSTEM',
  `author_id`   varchar(64)  DEFAULT NULL COMMENT '作者ID',
  `visibility`  varchar(16)  NOT NULL DEFAULT 'USER' COMMENT '可见性：USER 用户可见 / INTERNAL 内部备注',
  `content`     text         NOT NULL COMMENT '消息内容',
  `created_at`  datetime     DEFAULT NULL,
  PRIMARY KEY (`message_id`),
  KEY `idx_fb_message_feedback` (`feedback_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈消息';

CREATE TABLE IF NOT EXISTS `fb_feedback_attachment` (
  `attachment_id` varchar(64)  NOT NULL COMMENT '附件ID',
  `feedback_id`   varchar(64)  NOT NULL COMMENT '所属反馈ID',
  `message_id`    varchar(64)  DEFAULT NULL COMMENT '所属消息ID（可空）',
  `uploader_id`   varchar(64)  DEFAULT NULL COMMENT '上传者ID',
  `url`           varchar(512) NOT NULL COMMENT '访问路径（相对，不含服务器地址）',
  `original_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `content_type`  varchar(128) DEFAULT NULL COMMENT 'MIME 类型',
  `file_size`     bigint       DEFAULT NULL COMMENT '字节数',
  `created_at`    datetime     DEFAULT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `idx_fb_attachment_feedback` (`feedback_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈附件';
