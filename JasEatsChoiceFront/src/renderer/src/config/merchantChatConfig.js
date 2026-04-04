/**
 * 商家端AI聊天配置
 */

// 聊天配置
export const MERCHANT_CHAT_CONFIG = {
  MAX_MESSAGE_LENGTH: 1000,
  MAX_IMAGE_SIZE: 10 * 1024 * 1024,
  API_TIMEOUT: 60000
}

// 商家端快捷提问列表
export const MERCHANT_QUICK_QUESTIONS = [
  '今日销售情况分析',
  '本周热销菜品有哪些？',
  '帮我分析最近的差评原因',
  '明天应该备多少货？',
  '如何提高客单价？',
  '最近有哪些菜品销量下滑？',
  '帮我写一条菜品推荐文案',
  '分析我的客户评价趋势'
]

// 商家端欢迎消息
export const MERCHANT_WELCOME_MESSAGE = `您好！我是您的AI经营助手。我可以帮您：

📊 **经营分析** - 销售趋势、菜品排行、客户画像
💬 **评价回复** - 自动生成专业的评价回复建议
📝 **菜品优化** - 生成吸引人的菜品描述
📈 **经营建议** - 备货建议、定价策略、促销方案

有什么可以帮您的吗？`

// 错误消息
export const MERCHANT_ERROR_MESSAGES = {
  NETWORK_ERROR: '网络连接失败，请检查网络后重试',
  SERVER_ERROR: '服务器繁忙，请稍后重试',
  TIMEOUT: '请求超时，请重试',
  NO_DATA: '暂无经营数据，请先积累订单',
  ANALYSIS_FAILED: '数据分析失败，请稍后重试',
  INPUT_EMPTY: '请输入您的问题',
  CLEARED: '输入已清空'
}

// 日志工具
export const merchantLogger = {
  log: (...args) => {
    if (import.meta.env.DEV) {
      console.log('[商家AI]', ...args)
    }
  },
  error: (...args) => {
    console.error('[商家AI]', ...args)
  },
  warn: (...args) => {
    if (import.meta.env.DEV) {
      console.warn('[商家AI]', ...args)
    }
  }
}
