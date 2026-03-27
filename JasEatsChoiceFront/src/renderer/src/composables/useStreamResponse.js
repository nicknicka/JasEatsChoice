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
        // 查找包含实际数据的元素（done、content或card_data）
        const actualDataItem = dataArray.find((item) => {
          const itemData = item.data
          return itemData &&
                 typeof itemData === 'object' &&
                 (itemData.hasOwnProperty('done') ||
                  itemData.hasOwnProperty('content') ||
                  itemData.hasOwnProperty('card_data') ||
                  itemData.hasOwnProperty('message')) // ✅ 添加message字段支持
        })

        if (actualDataItem && actualDataItem.data) {
          parsedData = actualDataItem.data
        }
      } else if (data.startsWith('{')) {
        // 直接的对象格式：{ content: string, done: boolean, card_data: object, message: string }
        parsedData = JSON.parse(data)

        // ✅ 如果有message字段但没有content字段，转换格式
        if (parsedData.message && !parsedData.content) {
          // 如果是进度消息（包含progress字段），跳过不显示
          if (parsedData.progress === true) {
            logger.log('⏭️ 跳过进度消息:', parsedData.message)
            return null
          }
          // 否则将message作为content
          parsedData.content = parsedData.message
        }
      } else {
        // ✅ 纯文本格式（最终结果）
        parsedData = {
          content: data,
          done: false
        }
      }

      return parsedData
    } catch (error) {
      // ✅ 如果JSON解析失败，可能是纯文本，直接作为content
      if (data.length > 0 && !data.startsWith('[')) {
        logger.log('📝 纯文本数据:', data.substring(0, 50) + '...')
        return {
          content: data,
          done: false
        }
      }
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
    let hasReceivedContent = false

    try {
      while (true) {
        const { done, value } = await reader.read()

        // ✅ 连接关闭时，如果收到过内容，标记为完成
        if (done) {
          if (hasReceivedContent) {
            logger.log('✅ SSE连接关闭，处理完成')
            if (onComplete) onComplete()
          } else {
            logger.log('⚠️ SSE连接关闭，但未收到内容')
          }
          break
        }

        const chunk = decoder.decode(value, { stream: true })
        buffer += chunk

        // 按行分割数据
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          const parsedData = parseSSELine(line)
          if (!parsedData) continue

          // 检查是否结束（done标记）
          if (parsedData.done === true) {
            logger.log('✅ 接收到完成标记')
            if (onComplete) onComplete()
            return
          }

          // 追加内容
          if (parsedData.content && onContent) {
            hasReceivedContent = true
            logger.log('📝 追加内容:', parsedData.content.substring(0, 50) + '...')
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
