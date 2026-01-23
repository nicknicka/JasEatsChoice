/**
 * AI聊天相关配置
 */

// 聊天配置
export const CHAT_CONFIG = {
  // 消息长度限制
  MAX_MESSAGE_LENGTH: 500,

  // 图片大小限制（10MB）
  MAX_IMAGE_SIZE: 10 * 1024 * 1024,

  // 食谱长度限制
  RECIPE_MIN_LENGTH: 20,
  RECIPE_MAX_LENGTH: 10000,

  // 支持的图片类型
  IMAGE_TYPES: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'],
}

// 快捷提问列表
export const QUICK_QUESTIONS = [
  "推荐适合减肥的食谱",
  "今日卡路里摄入建议",
  "如何搭配营养均衡的饮食",
  "推荐低卡路里零食",
  "适合运动后的食物",
]

// 常用表情列表
export const COMMON_EMOJIS = [
  "😊", "😂", "🤔", "👍", "👎", "❤️", "🔥", "✨",
  "🍎", "🥗", "🍲", "🍜", "🍕", "🍰", "☕", "🥤",
  "💪", "🏃", "🧘", "😋", "🤤", "😌", "🤗", "😎",
]

// 错误消息映射
export const ERROR_MESSAGES = {
  // 通用错误
  NETWORK_ERROR: "网络连接超时，请检查网络设置",
  SERVER_ERROR: "服务器内部错误，请稍后重试",
  SERVICE_UNAVAILABLE: "服务暂时不可用，请稍后重试",

  // 验证错误
  EMPTY_MESSAGE: "请输入问题",
  MESSAGE_TOO_LONG: "消息长度超过限制",
  INVALID_IMAGE_TYPE: "请选择图片文件",
  IMAGE_TOO_LARGE: "图片大小不能超过10MB",
  EMPTY_RECIPE: "请输入食谱",
  RECIPE_TOO_SHORT: "食谱长度不能少于{min}个字符",
  RECIPE_TOO_LONG: "食谱长度不能超过{max}个字符",

  // 操作结果
  UPLOAD_SUCCESS: "图片上传成功",
  CLEARED: "已清空",
  PERSONAL_ENABLED: "已开启个性化建议",
  PERSONAL_DISABLED: "已关闭个性化建议",
  CLEARED_SUCCESS: "聊天记录已清空",
  SETTING_SAVE_FAILED: "设置保存失败",
}

// 默认欢迎消息
export const WELCOME_MESSAGE = "您好！我是您的AI饮食助手。有什么可以帮您的吗？"

// 调试配置
export const DEBUG = import.meta.env.DEV

// 日志辅助函数
export const logger = {
  log: DEBUG ? console.log : () => {},
  error: DEBUG ? console.error : () => {},
  warn: DEBUG ? console.warn : () => {},
  info: DEBUG ? console.info : () => {},
}
