/**
 * 商家AI流式消息的多卡片收集边界
 */
import { createCardContentStreamParser, normalizeAICardData } from '../../../../utils/aiCardDataParser.js'

export function createMerchantAIStreamingCollector() {
  let content = ''
  const cards = []
  const cardKeys = new Set()

  const appendCard = (card) => {
    const key = JSON.stringify(card)
    if (cardKeys.has(key)) return

    cards.push(card)
    cardKeys.add(key)
  }

  const appendCardData = (rawCardData) => {
    const cardData = normalizeAICardData(rawCardData)
    if (!cardData) return

    if (Array.isArray(cardData)) {
      cardData.forEach(appendCard)
      return
    }

    appendCard(cardData)
  }

  const parser = createCardContentStreamParser({
    onText: (text) => {
      content += text
    },
    onCard: appendCard
  })

  const getSnapshot = () => ({
    content,
    cardData: cards.length > 0 ? cards : null,
    messageType: cards.length > 0 ? 'uni_card' : null
  })

  return {
    appendContent: (chunk) => {
      parser.append(chunk)
      return getSnapshot()
    },
    appendCardData,
    getSnapshot
  }
}
