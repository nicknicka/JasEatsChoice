/**
 * 请求重试逻辑组合式函数
 */
import { HOME_CONSTANTS } from '../constants/home'

export function useRetry() {
  /**
   * 带重试机制的请求函数
   * @param fetchFn 要执行的请求函数
   * @param maxRetries 最大重试次数
   * @param delay 基础延迟时间（毫秒）
   */
  const retryFetch = async <T>(
    fetchFn: () => Promise<T>,
    maxRetries: number = HOME_CONSTANTS.RETRY.MAX_RETRIES,
    delay: number = HOME_CONSTANTS.RETRY.DELAY
  ): Promise<T> => {
    for (let i = 0; i < maxRetries; i++) {
      try {
        return await fetchFn()
      } catch (error) {
        console.error(`请求失败 (尝试 ${i + 1}/${maxRetries}):`, error)

        if (i === maxRetries - 1) {
          throw error
        }

        // 指数退避
        await new Promise((resolve) => setTimeout(resolve, delay * Math.pow(2, i)))
      }
    }

    throw new Error('重试失败')
  }

  return {
    retryFetch
  }
}
