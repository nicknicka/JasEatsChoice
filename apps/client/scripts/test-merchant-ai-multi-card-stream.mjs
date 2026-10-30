import assert from 'node:assert/strict'
import { test } from 'node:test'
import { createMerchantAIStreamingCollector } from '../src/renderer/src/views/merchant/AI/composables/merchantAIStreamingService.js'
import {
  buildMerchantHistoryPayload,
  mapMerchantHistoryMessage
} from '../src/renderer/src/views/merchant/AI/composables/merchantAIHistoryService.js'

const formatTime = (date) => date.toISOString().slice(11, 16)
const cardA = { schema: 'jaseat_card_v1', header: { title: { text: '订单 A' } } }
const cardB = { schema: 'jaseat_card_v1', header: { title: { text: '订单 B' } } }

test('连续多个显式卡片事件按顺序收集', () => {
  const collector = createMerchantAIStreamingCollector()

  collector.appendCardData(cardA)
  collector.appendCardData(cardB)

  assert.deepEqual(collector.getSnapshot(), {
    content: '',
    cardData: [cardA, cardB],
    messageType: 'uni_card'
  })
})

test('卡片开始、JSON 和结束标记被拆分到多个 chunk 时仍能解析', () => {
  const collector = createMerchantAIStreamingCollector()
  const chunks = [
    '前面内容',
    '[CARD',
    '_DATA_ST',
    'ART]',
    JSON.stringify(cardA),
    '[CARD_DATA',
    '_END]后面内容'
  ]

  chunks.forEach((chunk) => collector.appendContent(chunk))

  assert.deepEqual(collector.getSnapshot(), {
    content: '前面内容后面内容',
    cardData: [cardA],
    messageType: 'uni_card'
  })
})

test('一次回复中的多个内容卡片标记按顺序收集，结束标记后的文本保留', () => {
  const collector = createMerchantAIStreamingCollector()

  collector.appendContent(
    `说明一[CARD_DATA_START]${JSON.stringify(cardA)}[CARD_DATA_END]` +
      `中间说明[CARD_DATA_START]${JSON.stringify(cardB)}[CARD_DATA_END]结尾说明`
  )

  assert.deepEqual(collector.getSnapshot(), {
    content: '说明一中间说明结尾说明',
    cardData: [cardA, cardB],
    messageType: 'uni_card'
  })
})

test('显式卡片数据和相同的内容卡片标记不会重复展示', () => {
  const collector = createMerchantAIStreamingCollector()

  collector.appendCardData(cardA)
  collector.appendContent(`说明[CARD_DATA_START]${JSON.stringify(cardA)}[CARD_DATA_END]后续内容`)

  assert.deepEqual(collector.getSnapshot(), {
    content: '说明后续内容',
    cardData: [cardA],
    messageType: 'uni_card'
  })
})

test('连续完全相同的独立卡片事件不会被全局去重合并', () => {
  const collector = createMerchantAIStreamingCollector()

  collector.appendCardData(cardA)
  collector.appendCardData(cardA)
  collector.appendContent(`说明[CARD_DATA_START]${JSON.stringify(cardA)}[CARD_DATA_END]后续内容`)

  assert.deepEqual(collector.getSnapshot(), {
    content: '说明后续内容',
    cardData: [cardA, cardA],
    messageType: 'uni_card'
  })
})

test('卡片-only 和文本加卡片消息保存与刷新后都能恢复多张卡片', () => {
  const createdAt = '2026-10-30T10:30:00Z'
  const cardOnlyPayload = buildMerchantHistoryPayload({
    userId: '42',
    sender: 'ai',
    content: '',
    messageType: 'uni_card',
    cardData: [cardA, cardB]
  })

  assert.deepEqual(cardOnlyPayload.cardData, [cardA, cardB])
  assert.deepEqual(
    mapMerchantHistoryMessage(
      {
        sender: 'ai',
        content: '',
        messageType: 'uni_card',
        cardData: JSON.stringify([cardA, cardB]),
        createdAt
      },
      0,
      formatTime
    ),
    {
      id: 1,
      sender: 'ai',
      content: '',
      messageType: 'uni_card',
      cardData: [cardA, cardB],
      time: '10:30',
      avatar: '🤖'
    }
  )

  const textCardMessage = mapMerchantHistoryMessage(
    {
      sender: 'ai',
      content: `说明[CARD_DATA_START]${JSON.stringify(cardA)}[CARD_DATA_END]后续[CARD_DATA_START]${JSON.stringify(cardB)}[CARD_DATA_END]结尾`,
      createdAt
    },
    1,
    formatTime
  )

  assert.deepEqual(textCardMessage.content, '说明后续结尾')
  assert.deepEqual(textCardMessage.cardData, [cardA, cardB])
})
