/**
 * 商家AI流式消息的多卡片收集边界
 */
import {
  createAICardSourceCollector,
  createCardContentStreamParser,
  normalizeAICardList
} from '../../../../utils/aiCardDataParser.js'

export function createMerchantAIStreamingCollector() {
  let content = ''
  const cardCollector = createAICardSourceCollector()

  const appendCardData = (rawCardData) => {
    normalizeAICardList(rawCardData).forEach((card) => cardCollector.appendExplicitCard(card))
  }

  const parser = createCardContentStreamParser({
    onText: (text) => {
      content += text
    },
    onCard: (card) => cardCollector.appendContentCard(card)
  })

  const getSnapshot = () => {
    const cards = cardCollector.getUniqueCards()
    return {
      content,
      cardData: cards.length > 0 ? cards : null,
      messageType: cards.length > 0 ? 'uni_card' : null
    }
  }

  return {
    appendContent: (chunk) => {
      parser.append(chunk)
      return getSnapshot()
    },
    appendCardData,
    getSnapshot
  }
}
