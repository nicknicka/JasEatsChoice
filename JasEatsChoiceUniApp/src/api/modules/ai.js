/**
 * AI相关API
 * 对接后端 AIController 和 AIStreamController
 * 基础路径: /v1/ai
 */
import { get, post, del } from '@/utils/request'
import { AI_API, buildUrl } from '../urlEnum'

export const aiApi = {
  /**
   * AI对话
   * POST /v1/ai/chat
   * @param {Object} data - 对话数据
   * @param {string} data.message - 用户消息
   * @param {string} data.conversationId - 会话ID
   * @param {Array} data.history - 历史消息
   * @returns {Promise} 返回AI回复
   */
  chat: (data) => post(AI_API.CHAT, data),

  /**
   * AI对话（别名）
   * @param {Object} data - 对话数据
   * @returns {Promise} 返回AI回复
   */
  chatOld: (data) => post('/api/ai/chat', data),

  /**
   * AI流式对话
   * POST /v1/ai/chat/stream
   * @param {Object} data - 对话数据
   * @param {string} data.message - 用户消息
   * @param {string} data.conversationId - 会话ID
   * @param {Array} data.history - 历史消息
   * @returns {Promise} 返回流式响应
   */
  streamChat: (data) => post(AI_API.STREAM_CHAT, data),

  /**
   * AI推荐
   * POST /v1/ai/recommend
   * @param {Object} data - 推荐数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.type - 推荐类型(dish/merchant)
   * @param {Object} data.preferences - 偏好设置
   * @returns {Promise} 返回推荐结果
   */
  recommend: (data) => post(AI_API.RECOMMEND, data),

  /**
   * AI分析
   * POST /v1/ai/analyze
   * @param {Object} data - 分析数据
   * @param {string} data.type - 分析类型(nutrition/calorie/health)
   * @param {Array} data.foods - 食物列表
   * @returns {Promise} 返回分析结果
   */
  analyze: (data) => post(AI_API.ANALYZE, data),

  /**
   * 获取对话历史
   * GET /api/ai/history
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回对话历史
   */
  getHistory: (params) => get('/api/ai/history', params),

  /**
   * 获取对话详情
   * GET /api/ai/conversation/{conversationId}
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回对话详情
   */
  getConversation: (conversationId) => get(buildUrl('/api/ai/conversation/:conversationId', { conversationId })),

  /**
   * 内容提取
   * POST /api/ai/extract
   * @param {Object} data - 提取数据
   * @param {string} data.type - 类型(article/video/image)
   * @param {string} data.url - 内容URL
   * @param {string} data.content - 文本内容
   * @returns {Promise} 返回提取结果
   */
  extractContent: (data) => post('/api/ai/extract', data),

  /**
   * 营养分析
   * POST /api/ai/nutrition
   * @param {Object} data - 分析数据
   * @param {Array} data.foods - 食物列表
   * @returns {Promise} 返回营养分析结果
   */
  analyzeNutrition: (data) => post('/api/ai/nutrition', data),

  /**
   * 食谱生成
   * POST /api/ai/recipe/generate
   * @param {Object} data - 生成参数
   * @param {Array} data.ingredients - 食材列表
   * @param {string} data.preference - 偏好
   * @returns {Promise} 返回生成的食谱
   */
  generateRecipe: (data) => post('/api/ai/recipe/generate', data),

  /**
   * 获取快捷提问
   * GET /api/ai/questions
   * @returns {Promise} 返回快捷提问列表
   */
  getQuickQuestions: () => get('/api/ai/questions'),

  /**
   * 清除对话历史
   * DELETE /api/ai/conversation/{conversationId}
   * @param {string} conversationId - 会话ID
   * @returns {Promise} 返回清除结果
   */
  clearHistory: (conversationId) => del(buildUrl('/api/ai/conversation/:conversationId', { conversationId }))
}

export default aiApi
