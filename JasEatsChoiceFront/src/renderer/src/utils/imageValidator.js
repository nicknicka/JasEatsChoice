import { CHAT_CONFIG, ERROR_MESSAGES } from '../config/chatConfig'
import { formatErrorMessage } from './errorHandler'

/**
 * 验证图片文件
 * @param {File} file - 文件对象
 * @returns {{valid: boolean, error?: string}} 验证结果
 */
export function validateImageFile(file) {
  // 检查文件是否存在
  if (!file) {
    return { valid: false, error: '请选择文件' }
  }

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    return { valid: false, error: ERROR_MESSAGES.INVALID_IMAGE_TYPE }
  }

  // 检查图片类型是否支持
  if (!CHAT_CONFIG.IMAGE_TYPES.includes(file.type)) {
    return {
      valid: false,
      error: `不支持的图片格式，仅支持：${CHAT_CONFIG.IMAGE_TYPES.map((t) => t.split('/')[1]).join('、')}`
    }
  }

  // 检查文件大小
  if (file.size > CHAT_CONFIG.MAX_IMAGE_SIZE) {
    return { valid: false, error: ERROR_MESSAGES.IMAGE_TOO_LARGE }
  }

  return { valid: true }
}

/**
 * 创建图片预览URL
 * @param {File} file - 文件对象
 * @returns {string} blob URL
 */
export function createImagePreview(file) {
  return URL.createObjectURL(file)
}

/**
 * 释放图片预览URL
 * @param {string} url - blob URL
 */
export function revokeImagePreview(url) {
  if (url && url.startsWith('blob:')) {
    URL.revokeObjectURL(url)
  }
}

/**
 * 读取文件为DataURL
 * @param {File} file - 文件对象
 * @returns {Promise<string>} DataURL
 */
export function readFileAsDataURL(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = (e) => reject(new Error('文件读取失败'))
    reader.readAsDataURL(file)
  })
}

/**
 * 验证食谱内容
 * @param {string} recipe - 食谱内容
 * @returns {{valid: boolean, error?: string}} 验证结果
 */
export function validateRecipe(recipe) {
  const trimmed = recipe.trim()

  if (!trimmed) {
    return { valid: false, error: ERROR_MESSAGES.EMPTY_RECIPE }
  }

  if (trimmed.length > CHAT_CONFIG.RECIPE_MAX_LENGTH) {
    return {
      valid: false,
      error: formatErrorMessage(ERROR_MESSAGES.RECIPE_TOO_LONG, {
        max: CHAT_CONFIG.RECIPE_MAX_LENGTH
      })
    }
  }

  return { valid: true }
}

/**
 * 验证聊天消息
 * @param {string} message - 消息内容
 * @returns {{valid: boolean, error?: string}} 验证结果
 */
export function validateMessage(message) {
  const trimmed = message.trim()

  if (!trimmed) {
    return { valid: false, error: ERROR_MESSAGES.EMPTY_MESSAGE }
  }

  if (trimmed.length > CHAT_CONFIG.MAX_MESSAGE_LENGTH) {
    return {
      valid: false,
      error: formatErrorMessage(ERROR_MESSAGES.MESSAGE_TOO_LONG, {
        max: CHAT_CONFIG.MAX_MESSAGE_LENGTH
      })
    }
  }

  return { valid: true }
}
