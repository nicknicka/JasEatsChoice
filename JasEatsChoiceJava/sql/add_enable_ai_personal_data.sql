-- 添加AI个性化数据授权字段
-- 用于控制AI是否可以使用用户的个人数据提供个性化建议

ALTER TABLE t_user_preference
ADD COLUMN `enable_ai_personal_data` tinyint(1) DEFAULT '1'
COMMENT '是否允许AI使用个人数据：0-不允许，1-允许';

-- 查看表结构确认
DESC t_user_preference;
