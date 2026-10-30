import assert from 'node:assert/strict'
import { test } from 'node:test'
import { useStreamResponse } from '../src/renderer/src/composables/useStreamResponse.js'
import { sanitizeAICardMarkdown } from '../src/renderer/src/utils/aiCardMarkdownSanitizer.js'
import { createMerchantAICardViewModel } from '../src/renderer/src/views/merchant/AI/composables/merchantAICardDisplayService.js'

const validCard = {
  schema: 'jaseat_card_v1',
  header: { title: { text: '订单' } },
  elements: [{ tag: 'dish_list', dishes: [{ dishName: '宫保鸡丁', actions: [{ text: '加入购物车' }] }] }],
  actions: [{ text: '提交订单' }],
  footer: { note: '仅供参考', actions: [{ text: '查看详情' }] }
}

function createReader(lines) {
  const encodedLines = lines.map((line) => new TextEncoder().encode(`${line}\n`))
  return {
    read: async () => (encodedLines.length ? { done: false, value: encodedLines.shift() } : { done: true })
  }
}

test('onEvent 抛错后 SSE 流继续处理后续事件', async () => {
  const received = []
  const { processStream } = useStreamResponse()
  const reader = createReader([
    'data:[{"data":{"card_data":{"schema":"bad"}}}]',
    'data:{"content":"后续内容"}'
  ])

  await processStream(
    reader,
    (content) => received.push({ type: 'content', content }),
    () => {},
    () => {},
    () => {},
    (event) => {
      if (event.card_data) throw new Error('card handler failed')
      received.push({ type: 'event', event })
    }
  )

  assert.deepEqual(
    received.filter((item) => item.type === 'content'),
    [{ type: 'content', content: '后续内容' }]
  )
  assert.equal(received.some((item) => item.type === 'event'), true)
})

test('商家卡片视图模型只保留展示数据，不出现原始 JSON', () => {
  const cardView = createMerchantAICardViewModel(validCard, '订单说明')

  assert.equal(cardView.kind, 'card')
  assert.equal(cardView.card.actions, undefined)
  assert.equal(cardView.card.elements[0].dishes[0].actions, undefined)
  assert.equal(cardView.card.footer.actions, undefined)

  const fallbackView = createMerchantAICardViewModel({ schema: 'not_supported' }, '{"secret":"raw"}')
  assert.equal(fallbackView.kind, 'text')
  assert.equal(fallbackView.text, '卡片内容暂不可用')

  const explanatoryView = createMerchantAICardViewModel({ schema: 'not_supported' }, '这张卡片暂不支持')
  assert.equal(explanatoryView.kind, 'text')
  assert.equal(explanatoryView.text, '这张卡片暂不支持')
})

test('卡片 Markdown 内容消毒后不含危险 HTML', () => {
  const html = sanitizeAICardMarkdown(
    '<script>alert(1)</script><img src="x" onerror="alert(1)">**重点**'
  )

  assert.equal(html.includes('<script'), false)
  assert.equal(html.includes('<img'), false)
  assert.equal(html.includes('<strong>重点</strong>'), true)
})
