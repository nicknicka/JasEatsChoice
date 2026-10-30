/**
 * 商家AI聊天历史持久化边界
 */
import { API_CONFIG } from '../../../../config/index.js'
import {
  createAICardSourceCollector,
  normalizeAICardList,
  parseCardContentList
} from '../../../../utils/aiCardDataParser.js'

export function mapMerchantHistoryMessage(item, index, formatTime) {
  const extracted = parseCardContentList(item.content)
  const cardCollector = createAICardSourceCollector()
  normalizeAICardList(item.cardData).forEach((card) => cardCollector.appendExplicitCard(card))
  ;(extracted.cardData || []).forEach((card) => cardCollector.appendContentCard(card))
  const cards = cardCollector.getUniqueCards()
  const cardData = cards.length > 0 ? cards : null

  return {
    id: item.id || index + 1,
    sender: item.sender,
    content: extracted.content,
    messageType: item.messageType || (cardData ? 'uni_card' : null),
    cardData,
    time: formatTime(new Date(item.createdAt)),
    avatar: item.sender === 'ai' ? '🤖' : '👤'
  }
}

export function buildMerchantHistoryPayload({
  userId,
  sender,
  content,
  messageType = null,
  cardData = null,
  clientMessageId = null
}) {
  const payload = { userId, sender, content }

  if (messageType && cardData) {
    payload.messageType = messageType
    payload.cardData = cardData
  }

  if (clientMessageId) {
    payload.clientMessageId = clientMessageId
  }

  return payload
}

export function createMerchantAIHistoryService({ request, getUserId, getToken }) {
  const saveMessage = async (message) => {
    try {
      const payload = buildMerchantHistoryPayload({
        userId: getUserId(),
        sender: message.sender,
        content: message.content || '',
        messageType: message.messageType,
        cardData: message.cardData,
        clientMessageId: message.clientMessageId
      })

      const response = await request.post(API_CONFIG.baseURL + API_CONFIG.ai.save, payload, {
        headers: { Authorization: `Bearer ${getToken()}` }
      })

      if (response.data?.code !== 200) {
        throw new Error(response.data?.message || '保存商家AI聊天记录失败')
      }

      return { ok: true, status: 'saved' }
    } catch (error) {
      return { ok: false, status: 'retryable', error }
    }
  }

  return { saveMessage }
}
