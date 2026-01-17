/**
 * 聊天模块常量配置
 */

// WebSocket 配置
export const WEBSOCKET_CONFIG = {
  // 心跳间隔（毫秒）
  HEARTBEAT_INTERVAL: 30000,
  // 最大重连次数
  MAX_RECONNECT_ATTEMPTS: 10,
  // 基础重连延迟（毫秒）
  BASE_RECONNECT_DELAY: 2000,
  // 最大重连延迟（毫秒）
  MAX_RECONNECT_DELAY: 30000,
  // 消息去重缓存大小
  MAX_RECEIVED_IDS: 1000
}

// 消息配置
export const MESSAGE_CONFIG = {
  // 默认分页大小
  DEFAULT_PAGE_SIZE: 50,
  // 消息撤回时间限制（分钟）
  RECALL_TIME_LIMIT: 2,
  // 搜索防抖延迟（毫秒）
  SEARCH_DEBOUNCE_DELAY: 300,
  // 滚动加载阈值（像素）
  SCROLL_THRESHOLD: 100,
  // 滚动防抖延迟（毫秒）
  SCROLL_DEBOUNCE_DELAY: 200
}

// 本地存储配置
export const STORAGE_CONFIG = {
  // 默认存储天数
  DEFAULT_STORAGE_DAYS: 7,
  // 每个会话最大消息数
  MAX_MESSAGES_PER_SESSION: 100,
  // 存储键名
  CHAT_HISTORY_KEY: 'chatHistory',
  CHAT_STORAGE_DAYS_KEY: 'chatStorageDays',
  CHAT_MAX_MESSAGES_KEY: 'chatMaxMessagesPerSession'
}

// 消息类型
export const MESSAGE_TYPES = {
  SINGLE: 'single',
  GROUP: 'group',
  SYSTEM: 'system',
  NOTIFICATION: 'notification',
  HEARTBEAT: 'heartbeat'
}

// 消息状态
export const MESSAGE_STATUS = {
  SENDING: 'sending',
  SENT: 'sent',
  FAILED: 'failed',
  RECALLED: 'recalled'
}

// 会话类型
export const CONVERSATION_TYPES = {
  PRIVATE: 'private',
  GROUP: 'group',
  FRIEND: 'friend'
}

// 订单状态
export const ORDER_STATUS = {
  ACTIVE: 'active',
  CLOSED: 'closed',
  PAID: 'paid'
}

// 搜索类型
export const SEARCH_TYPES = {
  NICKNAME: 'nickname',
  PHONE: 'phone',
  EMAIL: 'email'
}

// 时间格式化配置
export const TIME_FORMATS = {
  JUST_NOW: '刚刚',
  TODAY: '今天',
  YESTERDAY: '昨天',
  DAYS_AGO: '天前'
}

// UI 配置
export const UI_CONFIG = {
  // 消息最大宽度百分比
  MESSAGE_MAX_WIDTH: 70,
  // 会话列表宽度百分比
  CONVERSATION_LIST_WIDTH: 37,
  // 悬浮按钮位置
  FLOAT_BUTTON_BOTTOM: 80,
  FLOAT_BUTTON_RIGHT: 40,
  // 搜索结果面板宽度
  SEARCH_PANEL_WIDTH: 350,
  // 搜索结果面板最大高度
  SEARCH_PANEL_MAX_HEIGHT: 400
}
