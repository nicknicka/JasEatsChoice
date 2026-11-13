/**
 * 请求重试判定。
 *
 * 幂等性没有保证的请求（POST/PUT/PATCH/DELETE）在超时后自动重发，
 * 可能重复下单、重复扣款，所以只对 GET/HEAD 重试（见 #567）。
 */

export const RETRYABLE_METHODS = ['get', 'head']

/** axios 通常已把 method 小写；缺省（未显式设置）按 GET 处理。 */
export const normalizeMethod = (method) => (method ? String(method).toLowerCase() : 'get')

export const isRetryableMethod = (method) => RETRYABLE_METHODS.includes(normalizeMethod(method))

/** 可重试的错误：网络类错误或服务端 5xx。 */
export const isRetryableError = (error) => {
  if (!error) return false
  const status = error.response?.status
  return (
    error.code === 'ETIMEDOUT' ||
    error.code === 'ECONNABORTED' ||
    error.code === 'NETWORK_ERROR' ||
    (typeof status === 'number' && status >= 500)
  )
}

/**
 * 是否应该重试这次请求。
 * @param {{ config: object, error: object, maxRetries: number }} params
 */
export const shouldRetryRequest = ({ config, error, maxRetries }) => {
  if (!config) return false
  if ((config.retryCount || 0) >= maxRetries) return false
  if (!isRetryableMethod(config.method)) return false
  return isRetryableError(error)
}

export default { RETRYABLE_METHODS, normalizeMethod, isRetryableMethod, isRetryableError, shouldRetryRequest }
