import { ref } from 'vue'
import { logger } from '../config/chatConfig'

/**
 * 流式响应处理
 * @returns {Object} 流式响应相关方法和状态
 */
export function useStreamResponse() {
  const isStreaming = ref(false)

  /**
   * 解析SSE数据行
   * @param {string} line - SSE数据行
   * @returns {Object|null} 解析后的数据
   */
  const parseSSELine = (line) => {
    const trimmedLine = line.trim()
    if (!trimmedLine.startsWith('data:')) {
      return null
    }

    const data = trimmedLine.substring(5).trim()
    if (!data) {
      return null
    }

    try {
      let parsedData

      // Spring Boot的SseEmitter数组格式：[{...}, {...}, {...}]
      if (data.startsWith('[')) {
        const dataArray = JSON.parse(data)
        // 查找mediaType为null的元素（包含实际数据）
        const actualDataItem = dataArray.find(item => item.mediaType === null)

        if (actualDataItem && actualDataItem.data) {
          parsedData = actualDataItem.data
        }
      } else {
        // 直接的对象格式：{ content: string, done: boolean }
        parsedData = JSON.parse(data)
      }

      return parsedData
    } catch (error) {
      logger.log('⚠️ 跳过无效数据:', data)
      return null
    }
  }

  /**
   * 处理流式响应
   * @param {ReadableStreamDefaultReader} reader - 流读取器
   * @param {Function} onContent - 内容回调
   * @param {Function} onComplete - 完成回调
   * @param {Function} onError - 错误回调
   * @returns {Promise<void>}
   */
  const processStream = async (reader, onContent, onComplete, onError) => {
    isStreaming.value = true
    const decoder = new TextDecoder()
    let buffer = ''

    try {
      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk

        // 按行分割数据
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          const parsedData = parseSSELine(line)
          if (!parsedData) continue

          // 检查是否结束
          if (parsedData.done === true) {
            logger.log('✅ 接收完成')
            if (onComplete) onComplete()
            return
          }

          // 追加内容
          if (parsedData.content && onContent) {
            onContent(parsedData.content)
          }
        }
      }
    } catch (error) {
      // 用户主动取消
      if (error.name === 'AbortError') {
        logger.log('ℹ️ 用户主动停止流式传输')
        return
      }

      // 其他错误
      logger.error('❌ 流式传输错误:', error)
      if (onError) onError(error)
      throw error
    } finally {
      isStreaming.value = false
    }
  }

  return {
    isStreaming,
    processStream,
    parseSSELine
  }
}
