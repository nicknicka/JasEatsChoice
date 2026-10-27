-- 用户状态：1-启用，0-禁用。现有用户默认启用。
ALTER TABLE `usr_user`
    ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用';
