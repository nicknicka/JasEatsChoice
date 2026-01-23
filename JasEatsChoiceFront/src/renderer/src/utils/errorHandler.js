import { ERROR_MESSAGES } from '../config/chatConfig'

/**
 * 统一API错误处理
 * @param {Error} error - 错误对象
 * @returns {string} 用户友好的错误消息
 */
export function handleApiError(error) {
  if (error.response) {
    // 服务器返回了错误响应
    const status = error.response.status

    switch (status) {
      case 400:
        return error.response.data?.message || '请求参数错误'
      case 401:
        return '未授权，请重新登录'
      case 403:
        return '没有权限访问此资源'
      case 404:
        return ERROR_MESSAGES.SERVICE_UNAVAILABLE
      case 500:
        return ERROR_MESSAGES.SERVER_ERROR
      case 502:
      case 503:
      case 504:
        return ERROR_MESSAGES.SERVICE_UNAVAILABLE
      default:
        return error.response.data?.message || `服务器错误(${status})，请稍后重试`
    }
  } else if (error.request) {
    // 请求已发出但没有收到响应
    return ERROR_MESSAGES.NETWORK_ERROR
  } else {
    // 其他错误
    return error.message || '操作失败，请稍后重试'
  }
}

/**
 * 格式化错误消息（支持变量替换）
 * @param {string} template - 消息模板
 * @param {Object} variables - 变量对象
 * @returns {string} 格式化后的消息
 */
export function formatErrorMessage(template, variables = {}) {
  return template.replace(/\{(\w+)\}/g, (match, key) => {
    return variables[key] !== undefined ? variables[key] : match
  })
}

/**
 * 根据错误类型显示提示
 * @param {Error} error - 错误对象
 * @param {Function} messageFn - 消息显示函数（如ElMessage.error）
 */
export function showErrorToast(error, messageFn = console.error) {
  const errorMsg = handleApiError(error)
  messageFn(errorMsg)
}
