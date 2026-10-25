-- 添加用户个人资料字段
-- 执行日期: 2026-02-02

-- 添加性别字段
ALTER TABLE t_user ADD COLUMN gender VARCHAR(10) DEFAULT NULL COMMENT '性别：male-男，female-女，other-其他' AFTER location;

-- 添加生日字段
ALTER TABLE t_user ADD COLUMN birthday VARCHAR(20) DEFAULT NULL COMMENT '生日' AFTER gender;

-- 添加个人简介字段
ALTER TABLE t_user ADD COLUMN bio TEXT DEFAULT NULL COMMENT '个人简介' AFTER birthday;
