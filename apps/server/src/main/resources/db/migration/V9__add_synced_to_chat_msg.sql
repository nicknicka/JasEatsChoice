-- 消息同步状态：0-未同步，1-已同步。现有消息默认未同步。
ALTER TABLE `ai_chat_msg`
    ADD COLUMN `synced` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已同步到群：0-未同步，1-已同步';
