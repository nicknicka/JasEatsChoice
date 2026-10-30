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
      return parseFirstCardContent(rawData).cardData
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

export function getFirstAICardData(rawData) {
  return normalizeAICardList(rawData)[0] || null
}

export function hasAICardPayload(rawData) {
  if (!rawData) return false

  if (typeof rawData === 'string') {
    const trimmedData = rawData.trim()
    return (
      rawData.includes(CARD_START) || trimmedData.startsWith('{') || trimmedData.startsWith('[')
    )
  }

  return true
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

export function createAICardSourceCollector() {
  const tokens = []

  const appendToken = (card, source) => {
    if (!card || typeof card !== 'object') return

    const key = JSON.stringify(card)
    const hasCrossSourceDuplicate = tokens.some(
      (token) => !token.duplicate && token.source !== source && JSON.stringify(token.card) === key
    )

    tokens.push({ card, source, duplicate: hasCrossSourceDuplicate })
  }

  return {
    appendExplicitCard: (card) => appendToken(card, 'explicit'),
    appendContentCard: (card) => appendToken(card, 'content'),
    getUniqueCards: () => tokens.filter((token) => !token.duplicate).map((token) => token.card)
  }
}

const findCardSegment = (content, cursor = 0) => {
  const startIndex = content.indexOf(CARD_START, cursor)
  if (startIndex === -1) return null

  const endIndex = content.indexOf(CARD_END, startIndex + CARD_START.length)
  if (endIndex === -1) return null

  return { startIndex, endIndex }
}

export function parseCardContentList(content) {
  if (typeof content !== 'string') {
    return { content, cardData: null, messageType: null }
  }

  const textParts = []
  const cards = []
  let cursor = 0

  while (cursor <= content.length) {
    const segment = findCardSegment(content, cursor)
    if (!segment) {
      textParts.push(content.substring(cursor))
      break
    }

    textParts.push(content.substring(cursor, segment.startIndex))
    cards.push(
      ...normalizeAICardList(
        content.substring(segment.startIndex + CARD_START.length, segment.endIndex)
      )
    )
    cursor = segment.endIndex + CARD_END.length
  }

  const text = textParts.join('').trim()
  return {
    content: text,
    cardData: cards.length > 0 ? cards : null,
    messageType: cards.length > 0 ? 'uni_card' : null
  }
}

export function parseFirstCardContent(content) {
  if (typeof content !== 'string') {
    return { content, cardData: null, messageType: null }
  }

  const segment = findCardSegment(content)
  if (!segment) {
    return { content, cardData: null, messageType: null }
  }

  const cardData = normalizeAICardData(
    content.substring(segment.startIndex + CARD_START.length, segment.endIndex)
  )

  return {
    content: content.substring(0, segment.startIndex).trim(),
    cardData,
    messageType: cardData ? 'uni_card' : null
  }
}
