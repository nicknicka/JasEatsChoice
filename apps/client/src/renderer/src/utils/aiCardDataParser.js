/**
 * AI 卡片数据归一化边界
 * 统一处理对象、JSON 字符串、数组和内容卡片标记，畸形数据返回 null。
 */

const CARD_START = '[CARD_DATA_START]'
const CARD_END = '[CARD_DATA_END]'

const parseCardJSON = (value) => {
  if (typeof value !== 'string') {
    return value && typeof value === 'object' ? value : null
  }

  try {
    const parsedData = JSON.parse(value)
    return parsedData && typeof parsedData === 'object' ? parsedData : null
  } catch (error) {
    return null
  }
}

export function normalizeAICardData(rawData) {
  if (Array.isArray(rawData)) {
    const normalizedData = rawData
      .map((item) => normalizeAICardData(item))
      .filter((item) => item !== null)
    return normalizedData.length > 0 ? normalizedData : null
  }

  if (typeof rawData === 'string') {
    if (rawData.includes(CARD_START) && rawData.includes(CARD_END)) {
      return parseCardContent(rawData).cardData
    }
    return parseCardJSON(rawData)
  }

  return parseCardJSON(rawData)
}

export function parseCardContent(content) {
  if (typeof content !== 'string') {
    return { content, cardData: null, messageType: null }
  }

  if (!content.includes(CARD_START) || !content.includes(CARD_END)) {
    return { content, cardData: null, messageType: null }
  }

  const startIndex = content.indexOf(CARD_START)
  const endIndex = content.indexOf(CARD_END)
  const cardData = normalizeAICardData(content.substring(startIndex + CARD_START.length, endIndex))

  return {
    content: content.substring(0, startIndex).trim(),
    cardData,
    messageType: cardData ? 'uni_card' : null
  }
}
