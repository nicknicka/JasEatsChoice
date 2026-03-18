import { get, post } from '@/utils/request'

/**
 * AI相关API
 */
export const aiApi = {
  /**
   * AI对话
   * @param {Object} data - 对话数据
   * @param {string} data.message - 用户消息
   * @param {string} data.conversationId - 会话ID
   * @param {Array} data.history - 历史消息
   */
  chat: (data) => post('/api/ai/chat', data),

  /**
   * 获取对话历史
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getHistory: (params) => get('/api/ai/history', params),

  /**
   * 获取对话详情
   * @param {string} conversationId - 会话ID
   */
  getConversation: (conversationId) => get(`/api/ai/conversation/${conversationId}`),

  /**
   * 内容提取
   * @param {Object} data - 提取数据
   * @param {string} data.type - 类型(article/video/image)
   * @param {string} data.url - 内容URL
   * @param {string} data.content - 文本内容
   */
  extractContent: (data) => post('/api/ai/extract', data),

  /**
   * 营养分析
   * @param {Object} data - 分析数据
   * @param {Array} data.foods - 食物列表
   */
  analyzeNutrition: (data) => post('/api/ai/nutrition', data),

  /**
   * 食谱生成
   * @param {Object} data - 生成参数
   * @param {Array} data.ingredients - 食材列表
   * @param {string} data.preference - 偏好
   */
  generateRecipe: (data) => post('/api/ai/recipe/generate', data),

  /**
   * 获取快捷提问
   */
  getQuickQuestions: () => get('/api/ai/questions'),

  /**
   * 清除对话历史
   * @param {string} conversationId - 会话ID
   */
  clearHistory: (conversationId) => del(`/api/ai/conversation/${conversationId}`)
}
