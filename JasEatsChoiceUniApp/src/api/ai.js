/**
 * AI相关API
 */
import request from '@/utils/request'

/**
 * AI对话
 */
export const chat = (data) => {
  return request({
    url: '/api/ai/chat',
    method: 'POST',
    data
  })
}

/**
 * 获取快捷提问
 */
export const getQuickQuestions = () => {
  return request({
    url: '/api/ai/questions',
    method: 'GET'
  })
}

/**
 * 获取聊天历史
 */
export const getChatHistory = (params) => {
  return request({
    url: '/api/ai/history',
    method: 'GET',
    params
  })
}

/**
 * 清空聊天历史
 */
export const clearChatHistory = () => {
  return request({
    url: '/api/ai/history/clear',
    method: 'POST'
  })
}

/**
 * 内容提取（从图片中提取食谱信息）
 */
export const extractContent = (data) => {
  return request({
    url: '/api/ai/extract',
    method: 'POST',
    data,
    header: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 生成食谱推荐
 */
export const generateRecipe = (params) => {
  return request({
    url: '/api/ai/recipe/generate',
    method: 'GET',
    params
  })
}

/**
 * 营养分析
 */
export const analyzeNutrition = (data) => {
  return request({
    url: '/api/ai/nutrition/analyze',
    method: 'POST',
    data
  })
}

export default {
  chat,
  getQuickQuestions,
  getChatHistory,
  clearChatHistory,
  extractContent,
  generateRecipe,
  analyzeNutrition
}
