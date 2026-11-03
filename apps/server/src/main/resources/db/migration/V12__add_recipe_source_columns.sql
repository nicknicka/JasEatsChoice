ALTER TABLE `cnt_recipe`
  ADD COLUMN `source_url` varchar(500) DEFAULT NULL COMMENT '来源链接',
  ADD COLUMN `source_platform` varchar(32) DEFAULT NULL COMMENT '来源平台';
