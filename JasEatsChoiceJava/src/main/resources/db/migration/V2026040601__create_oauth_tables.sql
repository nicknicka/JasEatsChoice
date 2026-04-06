-- 第三方 OAuth 账号关联表
CREATE TABLE IF NOT EXISTS t_user_oauth (
  id varchar(64) NOT NULL COMMENT '主键ID (OA + 16位数字)',
  user_id varchar(64) NOT NULL COMMENT '关联的本地用户ID',
  provider varchar(20) NOT NULL COMMENT '平台标识: wechat/qq',
  open_id varchar(128) NOT NULL COMMENT 'OpenID',
  union_id varchar(128) DEFAULT NULL COMMENT '微信UnionID（打通小程序）',
  nickname varchar(100) DEFAULT NULL COMMENT '第三方昵称',
  avatar_url varchar(512) DEFAULT NULL COMMENT '第三方头像URL',
  access_token varchar(512) DEFAULT NULL COMMENT 'access_token（加密存储）',
  refresh_token varchar(512) DEFAULT NULL COMMENT 'refresh_token（加密存储）',
  token_expires_at datetime DEFAULT NULL COMMENT 'token过期时间',
  create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_provider_openid (provider, open_id),
  UNIQUE KEY uk_provider_unionid (provider, union_id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方OAuth账号关联表';

-- 模拟第三方用户表（毕设演示用）
CREATE TABLE IF NOT EXISTS t_mock_oauth_user (
  id varchar(64) NOT NULL COMMENT '主键ID',
  provider varchar(20) NOT NULL COMMENT 'wechat/qq',
  open_id varchar(128) NOT NULL COMMENT '模拟OpenID',
  union_id varchar(128) DEFAULT NULL COMMENT '模拟UnionID',
  nickname varchar(100) NOT NULL COMMENT '模拟昵称',
  avatar_url varchar(512) DEFAULT NULL COMMENT '模拟头像',
  password varchar(100) DEFAULT NULL COMMENT '模拟授权密码（演示用）',
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_provider_openid (provider, open_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模拟第三方用户（毕设演示）';

-- 预置模拟测试账号
INSERT INTO t_mock_oauth_user (id, provider, open_id, union_id, nickname, avatar_url, password) VALUES
('MO1', 'wechat', 'wx_mock_openid_001', 'mock_unionid_001', '微信用户小明', 'https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzXlibXg7JC1RfF5YbLrgI6ZzmfFnicneCwkYq1mBuBstMWiciaRtznYH0BgSbnJriaOdicZGB0QMkeN8A/0', '123456'),
('MO2', 'qq', 'qq_mock_openid_001', NULL, 'QQ用户小红', 'https://q.qlogo.cn/qqapp/101235589/ABCD1234/100', '123456'),
('MO3', 'wechat', 'wx_mock_openid_002', 'mock_unionid_002', '微信用户小刚', 'https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzXlibXg7JC1RfF5YbLrgI6ZzmfFnicneCwkYq1mBuBstMWiciaRtznYH0BgSbnJriaOdicZGB0QMkeN8A/0', '123456');
