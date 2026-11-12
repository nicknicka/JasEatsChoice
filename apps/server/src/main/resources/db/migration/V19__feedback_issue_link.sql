-- 反馈与 GitHub Issue 的一对一关联（ticket #536）。
--
-- fb_feedback_issue_link 是幂等创建的事实来源；Issue 正文里的隐藏标记
-- `<!-- feedback:<id> -->` 只用于对账。桥接只处理「已接受 + 平台系统问题 + 规格完整」的反馈。

ALTER TABLE `fb_feedback`
  ADD COLUMN `platform_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否判定为平台系统问题' AFTER `environment`;

CREATE TABLE IF NOT EXISTS `fb_feedback_issue_link` (
  `link_id`      varchar(64)  NOT NULL COMMENT '关联ID',
  `feedback_id`  varchar(64)  NOT NULL COMMENT '反馈ID',
  `repo`         varchar(128) NOT NULL COMMENT '目标仓库 owner/name',
  `issue_number` varchar(32)  NOT NULL COMMENT 'Issue 编号',
  `issue_url`    varchar(512) DEFAULT NULL COMMENT 'Issue 地址',
  `state`        varchar(16)  NOT NULL DEFAULT 'open' COMMENT 'Issue 状态：open/closed',
  `created_at`   datetime     DEFAULT NULL,
  `updated_at`   datetime     DEFAULT NULL,
  PRIMARY KEY (`link_id`),
  UNIQUE KEY `uk_fb_issue_link_feedback` (`feedback_id`),
  UNIQUE KEY `uk_fb_issue_link_repo_issue` (`repo`, `issue_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈-Issue 关联';
