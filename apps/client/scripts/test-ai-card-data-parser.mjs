import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  createAICardSourceCollector,
  getFirstAICardData,
  hasAICardPayload,
  normalizeAICardData,
  parseCardContentList,
  parseFirstCardContent
} from '../src/renderer/src/utils/aiCardDataParser.js'
import { useUniCardParser } from '../src/renderer/src/views/user/AI/composables/useUniCardParser.js'

const card = { schema: 'jaseat_card_v1', header: { title: { text: '订单' } } }
const cardA = { schema: 'jaseat_card_v1', header: { title: { text: '订单 A' } } }
const cardB = { schema: 'jaseat_card_v1', header: { title: { text: '订单 B' } } }

test('对象、JSON 字符串、数组和卡片标记归一化为同一数据模型', () => {
  const fromMarker =
    '这是卡片说明\n[CARD_DATA_START]' + JSON.stringify(card) + '[CARD_DATA_END]后续内容'

  assert.deepEqual(normalizeAICardData(card), card)
  assert.deepEqual(normalizeAICardData(JSON.stringify(card)), card)
  assert.deepEqual(normalizeAICardData([JSON.stringify(card)]), [card])
  assert.deepEqual(parseFirstCardContent(fromMarker), {
    content: '这是卡片说明',
    cardData: card,
    messageType: 'uni_card'
  })
})

test('畸形卡片 JSON 返回 null，不进入渲染层', () => {
  assert.equal(normalizeAICardData('{"schema":'), null)
  assert.equal(normalizeAICardData(['{"schema":']), null)
  assert.deepEqual(parseFirstCardContent('[CARD_DATA_START]{"schema":}[CARD_DATA_END]'), {
    content: '',
    cardData: null,
    messageType: null
  })
})

test('用户端 UniCard 解析器复用统一归一化边界', () => {
  const { parseCardData } = useUniCardParser()

  assert.deepEqual(parseCardData(JSON.stringify(card)), { parsed: true, card, isUniCard: true })
  assert.deepEqual(parseCardData([JSON.stringify(card)]), { parsed: true, card, isUniCard: true })
  assert.deepEqual(parseCardData('{"schema":'), { parsed: false, card: null, isUniCard: false })
})

test('多卡片解析按顺序返回全部卡片', () => {
  const content =
    '第一张\n[CARD_DATA_START]' +
    JSON.stringify(cardA) +
    '[CARD_DATA_END]\n第二张\n[CARD_DATA_START]' +
    JSON.stringify(cardB) +
    '[CARD_DATA_END]'

  assert.deepEqual(parseCardContentList(content), {
    content: '第一张\n\n第二张',
    cardData: [cardA, cardB],
    messageType: 'uni_card'
  })
  assert.deepEqual(getFirstAICardData([JSON.stringify(cardA), cardB]), cardA)
})

test('畸形卡片 payload 可被识别并保留安全回退状态', () => {
  assert.equal(hasAICardPayload({ schema: 'bad' }), true)
  assert.equal(hasAICardPayload('{"schema":'), true)
  assert.equal(hasAICardPayload('[CARD_DATA_START]{"schema":}[CARD_DATA_END]'), true)
  assert.equal(hasAICardPayload(null), false)
  assert.equal(hasAICardPayload('普通文本'), false)
})

test('显式卡片与内容标记只在同源重复时去重，独立重复事件保留', () => {
  const collector = createAICardSourceCollector()

  collector.appendExplicitCard(cardA)
  collector.appendExplicitCard(cardA)
  collector.appendContentCard(cardA)

  assert.deepEqual(collector.getUniqueCards(), [cardA, cardA])
})

test('显式卡片与内容标记代表同一批卡片时去除跨源重复', () => {
  const collector = createAICardSourceCollector()

  collector.appendExplicitCard(cardA)
  collector.appendExplicitCard(cardB)
  collector.appendContentCard(cardA)
  collector.appendContentCard(cardB)

  assert.deepEqual(collector.getUniqueCards(), [cardA, cardB])
})
