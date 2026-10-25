-- 添加通知设置字段到用户偏好表
ALTER TABLE t_user_preference
ADD COLUMN enable_order_notification TINYINT(1) DEFAULT 1 COMMENT '订单通知开关';

ALTER TABLE t_user_preference
ADD COLUMN enable_activity_notification TINYINT(1) DEFAULT 1 COMMENT '活动通知开关';

ALTER TABLE t_user_preference
ADD COLUMN enable_merchant_reply_notification TINYINT(1) DEFAULT 1 COMMENT '商家回复通知开关';

ALTER TABLE t_user_preference
ADD COLUMN enable_group_chat_notification TINYINT(1) DEFAULT 1 COMMENT '群聊消息通知开关';
