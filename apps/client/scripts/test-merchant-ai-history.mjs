import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  buildMerchantHistoryPayload,
  createMerchantAIHistoryService,
  mapMerchantHistoryMessage
} from '../src/renderer/src/views/merchant/AI/composables/merchantAIHistoryService.js'

const formatTime = (date) => date.toISOString().slice(11, 16)
const card = { schema: 'jaseat_card_v1', header: { title: { text: '订单' } } }

test('历史消息按接口返回顺序恢复发送者、文字和卡片', () => {
  const createdAt = '2026-10-30T10:30:00Z'
  const history = [
    { id: 10, sender: 'user', content: '今日销售情况', createdAt },
    { id: 11, sender: 'ai', content: '销售额增长了', createdAt },
    {
      id: 12,
      sender: 'ai',
      content: '这是订单卡片',
      messageType: 'uni_card',
      cardData: JSON.stringify(card),
      createdAt
    }
  ]

  assert.deepEqual(history.map((item, index) => mapMerchantHistoryMessage(item, index, formatTime)), [
    {
      id: 10,
      sender: 'user',
      content: '今日销售情况',
      messageType: null,
      cardData: null,
      time: '10:30',
      avatar: '👤'
    },
    {
      id: 11,
      sender: 'ai',
      content: '销售额增长了',
      messageType: null,
      cardData: null,
      time: '10:30',
      avatar: '🤖'
    },
    {
      id: 12,
      sender: 'ai',
      content: '这是订单卡片',
      messageType: 'uni_card',
      cardData: card,
      time: '10:30',
      avatar: '🤖'
    }
  ])
})

test('保存 payload 携带幂等客户端消息 ID 和卡片数据', () => {
  assert.deepEqual(
    buildMerchantHistoryPayload({
      userId: '42',
      sender: 'ai',
      content: '销售分析',
      messageType: 'uni_card',
      cardData: card,
      clientMessageId: 'merchant-ai-test'
    }),
    {
      userId: '42',
      sender: 'ai',
      content: '销售分析',
      messageType: 'uni_card',
      cardData: card,
      clientMessageId: 'merchant-ai-test'
    }
  )
})

test('保存失败返回待重试状态，不抛出影响聊天展示', async () => {
  const request = { post: async () => { throw new Error('network down') } }
  const service = createMerchantAIHistoryService({
    request,
    getUserId: () => '42',
    getToken: () => 'token'
  })
  const result = await service.saveMessage({ sender: 'user', content: '你好' })

  assert.equal(result.ok, false)
  assert.equal(result.status, 'retryable')
  assert.match(result.error.message, /network down/)
})

test('重试时使用同一个客户端消息 ID', async () => {
  const posts = []
  let shouldFail = true
  const request = {
    post: async (url, payload) => {
      posts.push({ url, payload })
      if (shouldFail) throw new Error('failed')
      return { data: { code: 200 } }
    }
  }
  const service = createMerchantAIHistoryService({
    request,
    getUserId: () => '42',
    getToken: () => 'token'
  })
  const message = { sender: 'user', content: '你好', clientMessageId: 'merchant-user-test' }

  await service.saveMessage(message)
  shouldFail = false
  await service.saveMessage(message)

  assert.equal(posts.length, 2)
  assert.equal(posts[0].url.endsWith('/v1/ai/chat/save'), true)
  assert.equal(posts[0].payload.clientMessageId, 'merchant-user-test')
  assert.deepEqual(posts[1].payload, posts[0].payload)
})
