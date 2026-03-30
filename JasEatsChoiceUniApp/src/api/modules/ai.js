/**
 * AI相关API
 * 对接后端 AIController 和 AIStreamController
 * 基础路径: /v1/ai
 */
import { get, post, del } from '@/utils/request'
import { AI_API } from '../urlEnum'

/**
 * 清理markdown代码块标记
 * 处理AI模型返回的 ```json ... ``` 格式
 * @param {string} content - 原始内容
 * @returns {string} 清理后的内容
 */
const cleanMarkdownCodeBlocks = (content) => {
  if (!content || typeof content !== 'string') {
    return content
  }

  let cleaned = content

  // 移除开头的 ```json 或 ``` 标记
  cleaned = cleaned.replace(/^```json\s*\n?/i, '')
  cleaned = cleaned.replace(/^```\s*\n?/i, '')

  // 移除结尾的 ``` 标记
  cleaned = cleaned.replace(/\n?```\s*$/i, '')

  // 移除中间可能出现的独立 ``` 标记（清理多余格式）
  cleaned = cleaned.replace(/\n?```\n?/g, '\n')

  // 清理多余的空行
  cleaned = cleaned.replace(/\n{3,}/g, '\n\n').trim()

  if (cleaned !== content) {
    console.log('🧹 已清理markdown代码块标记')
  }

  return cleaned
}

export const aiApi = {
  /**
   * 清理markdown代码块标记
   * 处理AI模型返回的 ```json ... ``` 格式
   * @param {string} content - 原始内容
   * @returns {string} 清理后的内容
   */
  cleanMarkdownCodeBlocks,

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
   * AI对话（使用SupervisorAgent）
   * POST /agent/supervisor/chat
   * @param {Object} data - 对话数据
   * @param {Function} onMessage - 消息回调
   * @param {Function} onComplete - 完成回调
   * @param {Function} onError - 错误回调
   * @returns {Promise} 返回响应
   *
   * 注意：UniApp不支持SSE，使用普通POST接口
   * 桌面端使用 /agent/supervisor-sse/chat (SSE流式)
   */
  streamChat: async (data, onMessage, onComplete, onError) => {
    const userId = uni.getStorageSync('userId') || '1'
    const token = uni.getStorageSync('token') || ''

    console.log('==================== AI聊天请求开始 ====================')
    console.log('📝 用户消息:', data.message)
    console.log('👤 用户ID:', userId)
    console.log('🌐 接口路径:', `${AI_API.BASE_URL}${AI_API.CHAT}`)

    try {
      // 使用标准POST请求
      const response = await new Promise((resolve, reject) => {
        uni.request({
          url: `${AI_API.BASE_URL}${AI_API.CHAT}`,
          method: 'POST',
          header: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
          },
          data: {
            message: data.message,
            userId: userId
          },
          timeout: 60000, // 60秒超时
          success: (res) => {
            console.log('✅ HTTP响应状态码:', res.statusCode)
            if (res.statusCode === 200) {
              resolve(res.data)
            } else {
              reject(new Error(`HTTP ${res.statusCode}: ${res.data?.message || '请求失败'}`))
            }
          },
          fail: (err) => {
            console.error('❌ 请求失败:', err)
            reject(err)
          }
        })
      })

      console.log('📦 后端响应:', response)

      // SupervisorAgent返回标准ResponseResult格式: { success: true, code: "200", data: "内容" }
      if (response && (response.success === true || response.code === 200 || response.code === "200")) {
        let message = response.data

        if (!message) {
          console.error('❌ 响应data为空')
          onError && onError(new Error('响应内容为空'))
          return
        }

        // 🔧 清理markdown代码块标记（如果存在）
        message = cleanMarkdownCodeBlocks(message)

        console.log('📥 收到AI回复内容:', {
          content: message.substring(0, 100) + (message.length > 100 ? '...' : ''),
          length: message.length,
          timestamp: new Date().toISOString()
        })

        // 回调处理
        onMessage(message)
        onComplete && onComplete()

        console.log('==================== AI聊天请求完成 ====================')
      } else {
        console.error('❌ 业务失败:', response)

        // 解析错误信息，提取友好提示
        let errorMsg = 'AI回复失败，请稍后重试'
        if (response?.message) {
          const msg = response.message
          // 如果是JSON解析错误，给出更友好的提示
          if (msg.includes('Failed to parse') || msg.includes('```json')) {
            errorMsg = 'AI服务暂时异常，请重新发送消息'
          } else {
            errorMsg = msg
          }
        }

        onError && onError(new Error(errorMsg))
      }
    } catch (error) {
      console.error('❌ AI聊天请求异常:', error)
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
