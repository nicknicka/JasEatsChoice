/**
 * 消息类型枚举
 */

// 消息类型
export const MessageType = {
  TEXT: 'text',              // 文本消息
  IMAGE: 'image',            // 图片消息
  VOICE: 'voice',            // 语音消息
  VIDEO: 'video',            // 视频消息
  LOCATION: 'location',      // 位置消息
  DISH: 'dish',              // 菜品卡片
  ORDER: 'order',            // 订单卡片
  RECIPE: 'recipe',          // 食谱卡片
  SYSTEM: 'system',          // 系统消息
  NOTIFICATION: 'notification', // 通知消息
  CUSTOM: 'custom'           // 自定义消息
}

// 会话类型
export const ConversationType = {
  SINGLE: 'single',          // 单聊
  GROUP: 'group',            // 群聊
  SYSTEM: 'system',          // 系统通知
  BROADCAST: 'broadcast'     // 广播消息
}

// 消息状态
export const MessageStatus = {
  SENDING: 'sending',        // 发送中
  SENT: 'sent',             // 已发送
  DELIVERED: 'delivered',    // 已送达
  READ: 'read',             // 已读
  FAILED: 'failed'          // 发送失败
}

// 通知类型
export const NotificationType = {
  SYSTEM: 'system',          // 系统通知
  ORDER: 'order',            // 订单通知
  ACTIVITY: 'activity',      // 活动通知
  CHAT: 'chat',              // 聊天通知
  PROMOTION: 'promotion'     // 促销通知
}

// 消息优先级
export const MessagePriority = {
  LOW: 1,
  NORMAL: 2,
  HIGH: 3,
  URGENT: 4
}

// 导出合并对象
export default {
  MessageType,
  ConversationType,
  MessageStatus,
  NotificationType,
  MessagePriority
}
