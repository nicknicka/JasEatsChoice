/**
 * AI相关API
 * 对接后端 AIController 和 AIStreamController
 * 基础路径: /v1/ai
 */
import { get, post, del } from '@/utils/request'
import { AI_API, buildUrl } from '../urlEnum'

export const aiApi = {
  /**
   * AI对话（普通）
   * POST /v1/ai/chat
   * @param {Object} data - 对话数据
   * @param {string} data.message - 用户消息
   * @param {string} data.conversationId - 会话ID
   * @param {Array} data.history - 历史消息
   * @returns {Promise} 返回AI回复
   */
  chat: (data) => post(AI_API.CHAT, data),

  /**
   * AI流式对话（SSE）
   * POST /v1/ai/chat
   * @param {Object} data - 对话数据
   * @param {Function} onMessage - 消息回调
   * @param {Function} onComplete - 完成回调
   * @param {Function} onError - 错误回调
   * @returns {Promise} 返回流式响应
   */
  streamChat: async (data, onMessage, onComplete, onError) => {
    const userId = uni.getStorageSync('userId') || '1'
    const token = uni.getStorageSync('token') || ''

    try {
      // 使用uni.request模拟SSE
      const response = await new Promise((resolve, reject) => {
        const requestTask = uni.request({
          url: `${AI_API.BASE_URL}${AI_API.CHAT}`,
          method: 'POST',
          header: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : '',
            'Accept': 'text/event-stream'
          },
          data: {
            ...data,
            userId
          },
          success: (res) => {
            if (res.statusCode === 200) {
              resolve(res.data)
            } else {
              reject(new Error(`HTTP ${res.statusCode}`))
            }
          },
          fail: (err) => {
            reject(err)
          }
        })
      })

      // 处理响应
      if (response && response.data) {
        // 如果是完整响应，一次性返回
        if (typeof response.data === 'string') {
          onMessage(response.data)
        } else if (response.data.message) {
          onMessage(response.data.message)
        }
        onComplete && onComplete()
      }
    } catch (error) {
      onError && onError(error)
      throw error
    }
  },

  /**
   * 获取聊天历史记录
   * GET /v1/ai/chat/history
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回历史记录
   */
  getHistory: (userId) => get(`${AI_API.HISTORY}?userId=${userId}`),

  /**
   * 保存聊天消息
   * POST /v1/ai/chat/save
   * @param {Object} data - 消息数据
   * @param {string} data.userId - 用户ID
   * @param {string} data.sender - 发送者（user/ai）
   * @param {string} data.content - 消息内容
   * @returns {Promise} 返回保存结果
   */
  saveMessage: (data) => post(AI_API.SAVE, data),

  /**
   * 清空聊天记录
   * DELETE /v1/ai/chat/clear
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回清除结果
   */
  clearHistory: (userId) => del(`${AI_API.CLEAR}?userId=${userId}`),

  /**
   * 检查是否有聊天历史
   * GET /v1/ai/chat/has-history
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回是否有历史记录
   */
  hasHistory: (userId) => get(`${AI_API.HAS_HISTORY}?userId=${userId}`),

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
  getQuickQuestions: () => get('/api/ai/questions')
}

export default aiApi
