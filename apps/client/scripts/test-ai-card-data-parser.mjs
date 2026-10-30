import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  normalizeAICardData,
  parseCardContent,
} from '../src/renderer/src/utils/aiCardDataParser.js'
import { useUniCardParser } from '../src/renderer/src/views/user/AI/composables/useUniCardParser.js'

const card = { schema: 'jaseat_card_v1', header: { title: { text: '订单' } } }

test('对象、JSON 字符串、数组和卡片标记归一化为同一数据模型', () => {
  const fromMarker =
    '这是卡片说明\n[CARD_DATA_START]' +
    JSON.stringify(card) +
    '[CARD_DATA_END]后续内容'

  assert.deepEqual(normalizeAICardData(card), card)
  assert.deepEqual(normalizeAICardData(JSON.stringify(card)), card)
  assert.deepEqual(normalizeAICardData([JSON.stringify(card)]), [card])
  assert.deepEqual(parseCardContent(fromMarker), {
    content: '这是卡片说明',
    cardData: card,
    messageType: 'uni_card',
  })
})

test('畸形卡片 JSON 返回 null，不进入渲染层', () => {
  assert.equal(normalizeAICardData('{"schema":'), null)
  assert.equal(normalizeAICardData(['{"schema":']), null)
  assert.deepEqual(parseCardContent('[CARD_DATA_START]{"schema":}[CARD_DATA_END]'), {
    content: '',
    cardData: null,
    messageType: null,
  })
})

test('用户端 UniCard 解析器复用统一归一化边界', () => {
  const { parseCardData } = useUniCardParser()

  assert.deepEqual(parseCardData(JSON.stringify(card)), { parsed: true, card, isUniCard: true })
  assert.deepEqual(parseCardData([JSON.stringify(card)]), { parsed: true, card, isUniCard: true })
  assert.deepEqual(parseCardData('{"schema":'), { parsed: false, card: null, isUniCard: false })
})
