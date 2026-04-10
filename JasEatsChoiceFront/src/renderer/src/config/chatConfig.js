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
  IMAGE_TYPES: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
}

// 快捷提问分组
export const QUICK_QUESTION_CATEGORIES = [
  {
    id: 'recommendation',
    title: '饮食推荐',
    description: '想不到吃什么时，先从这里开始',
    accent: '#D4845A',
    questions: [
      '给我推荐一份适合减脂的午餐',
      '今天晚餐吃什么更清爽',
      '推荐几道高蛋白又好做的菜'
    ]
  },
  {
    id: 'nutrition',
    title: '营养分析',
    description: '快速看热量、蛋白质和搭配是否合理',
    accent: '#7BAE7F',
    questions: [
      '帮我分析番茄炒蛋的热量和营养',
      '这份餐的蛋白质够不够',
      '我今天的饮食有没有太油腻'
    ]
  },
  {
    id: 'plan',
    title: '饮食规划',
    description: '适合给自己做一份更完整的安排',
    accent: '#8E7CC3',
    questions: [
      '给我做一份一周健康饮食计划',
      '帮我安排今天三餐的搭配',
      '运动后适合吃什么'
    ]
  },
  {
    id: 'personal',
    title: '个性化建议',
    description: '结合你的偏好和目标做推荐',
    accent: '#E2B455',
    questions: [
      '根据我的口味推荐几道菜',
      '适合控糖的外卖怎么点',
      '推荐几种低卡但有饱腹感的食物'
    ]
  }
]

// 快捷提问列表（兼容旧逻辑）
export const QUICK_QUESTIONS = QUICK_QUESTION_CATEGORIES.flatMap((category) => category.questions)

// 常用表情列表
export const COMMON_EMOJIS = [
  '😊',
  '😂',
  '🤔',
  '👍',
  '👎',
  '❤️',
  '🔥',
  '✨',
  '🍎',
  '🥗',
  '🍲',
  '🍜',
  '🍕',
  '🍰',
  '☕',
  '🥤',
  '💪',
  '🏃',
  '🧘',
  '😋',
  '🤤',
  '😌',
  '🤗',
  '😎'
]

// 错误消息映射
export const ERROR_MESSAGES = {
  // 通用错误
  NETWORK_ERROR: '网络连接超时，请检查网络设置',
  SERVER_ERROR: '服务器内部错误，请稍后重试',
  SERVICE_UNAVAILABLE: '服务暂时不可用，请稍后重试',

  // 验证错误
  EMPTY_MESSAGE: '请输入问题',
  MESSAGE_TOO_LONG: '消息长度超过限制',
  INVALID_IMAGE_TYPE: '请选择图片文件',
  IMAGE_TOO_LARGE: '图片大小不能超过10MB',
  EMPTY_RECIPE: '请输入食谱',
  RECIPE_TOO_SHORT: '食谱长度不能少于{min}个字符',
  RECIPE_TOO_LONG: '食谱长度不能超过{max}个字符',

  // 操作结果
  UPLOAD_SUCCESS: '图片上传成功',
  CLEARED: '已清空',
  PERSONAL_ENABLED: '已开启个性化建议',
  PERSONAL_DISABLED: '已关闭个性化建议',
  CLEARED_SUCCESS: '聊天记录已清空',
  SETTING_SAVE_FAILED: '设置保存失败'
}

// 默认欢迎消息
export const WELCOME_MESSAGE = '您好！我是您的AI饮食助手。有什么可以帮您的吗？'

// 调试配置
export const DEBUG = import.meta.env.DEV

// 日志辅助函数
export const logger = {
  log: DEBUG ? console.log : () => {},
  error: DEBUG ? console.error : () => {},
  warn: DEBUG ? console.warn : () => {},
  info: DEBUG ? console.info : () => {}
}
