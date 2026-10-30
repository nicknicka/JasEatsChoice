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

export function normalizeAICardList(rawData) {
  const normalizedData = normalizeAICardData(rawData)

  if (!normalizedData) return []
  return Array.isArray(normalizedData) ? normalizedData : [normalizedData]
}

export function parseCardContents(content) {
  if (typeof content !== 'string') {
    return { content, cardData: null, messageType: null }
  }

  const textParts = []
  const cards = []
  let cursor = 0

  while (cursor <= content.length) {
    const startIndex = content.indexOf(CARD_START, cursor)
    if (startIndex === -1) {
      textParts.push(content.substring(cursor))
      break
    }

    textParts.push(content.substring(cursor, startIndex))
    const endIndex = content.indexOf(CARD_END, startIndex + CARD_START.length)
    if (endIndex === -1) {
      break
    }

    normalizeAICardList(content.substring(startIndex + CARD_START.length, endIndex)).forEach((card) => {
      cards.push(card)
    })
    cursor = endIndex + CARD_END.length
  }

  const text = textParts.join('').trim()

  return {
    content: text,
    cardData: cards.length > 0 ? cards : null,
    messageType: cards.length > 0 ? 'uni_card' : null
  }
}

export function createCardContentStreamParser({ onText, onCard }) {
  let collectingCardData = false
  let cardDataBuffer = ''
  let pendingText = ''
  let pendingCardEnd = ''

  const partialMarkerLength = (text, marker) => {
    const maxLength = Math.min(text.length, marker.length - 1)

    for (let length = maxLength; length > 0; length -= 1) {
      if (marker.startsWith(text.substring(text.length - length))) {
        return length
      }
    }
    return 0
  }

  const append = (chunk) => {
    if (collectingCardData) {
      let data = pendingCardEnd + chunk
      pendingCardEnd = ''

      const endIndex = data.indexOf(CARD_END)
      if (endIndex === -1) {
        const holdLength = partialMarkerLength(data, CARD_END)
        cardDataBuffer += data.substring(0, data.length - holdLength)
        if (holdLength > 0) pendingCardEnd = data.substring(data.length - holdLength)
        return
      }

      cardDataBuffer += data.substring(0, endIndex)
      const cardData = normalizeAICardData(cardDataBuffer)
      if (cardData) onCard(cardData)
      collectingCardData = false
      cardDataBuffer = ''
      append(data.substring(endIndex + CARD_END.length))
      return
    }

    let data = pendingText + chunk
    pendingText = ''

    for (;;) {
      const startIndex = data.indexOf(CARD_START)
      if (startIndex === -1) {
        const holdLength = partialMarkerLength(data, CARD_START)
        const text = data.substring(0, data.length - holdLength)
        if (text) onText(text)
        if (holdLength > 0) pendingText = data.substring(data.length - holdLength)
        return
      }

      if (startIndex > 0) onText(data.substring(0, startIndex))
      const endIndex = data.indexOf(CARD_END, startIndex + CARD_START.length)
      if (endIndex === -1) {
        collectingCardData = true
        cardDataBuffer = data.substring(startIndex + CARD_START.length)
        return
      }

      const cardData = normalizeAICardData(data.substring(startIndex + CARD_START.length, endIndex))
      if (cardData) onCard(cardData)
      data = data.substring(endIndex + CARD_END.length)
    }
  }

  return { append }
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
