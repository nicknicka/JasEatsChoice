import assert from 'node:assert/strict'
import { test } from 'node:test'
import {
  createRecipeSSEParser,
  readRecipeEventStream
} from '../src/renderer/src/utils/recipeOptimizationStream.js'

const encode = (value) => new TextEncoder().encode(value)

function createReader(chunks) {
  const queue = chunks.map(encode)
  return {
    async read() {
      return queue.length > 0 ? { done: false, value: queue.shift() } : { done: true }
    }
  }
}

test('解析命名事件和 JSON data，并按 token 顺序拼接内容', async () => {
  const events = []
  const reader = createReader([
    'event: progress\ndata: {"message":"正在分析食谱内容..."}\n\n',
    'event: token\ndata: {"content":"推荐"}\n\n',
    'event: token\ndata: {"content":"食谱"}\n\n',
    'event: result\ndata: {"result":{"recipe":{"name":"番茄鸡蛋"}}}\n\n',
    'event: done\ndata: {"elapsedMs":12345}\n\n'
  ])

  await readRecipeEventStream(reader, (event) => events.push(event))

  assert.deepEqual(events.map((event) => event.event), [
    'progress',
    'token',
    'token',
    'result',
    'done'
  ])
  assert.equal(events[0].data.message, '正在分析食谱内容...')
  assert.equal(
    events.filter((event) => event.event === 'token').map((event) => event.data.content).join(''),
    '推荐食谱'
  )
  assert.deepEqual(events.at(-2).data, { result: { recipe: { name: '番茄鸡蛋' } } })
  assert.deepEqual(events.at(-1).data, { elapsedMs: 12345 })
})

test('支持多行 data 和跨 chunk 的 CRLF 事件块', () => {
  const events = []
  const parser = createRecipeSSEParser((event) => events.push(event))

  parser.feed('event: token\r\ndata: {"content":\r\n')
  parser.feed('data: "分块内容"}\r\n\r')
  parser.feed('\nevent: done\r\ndata: {"elapsedMs":800}\r\n\r\n')
  parser.finish()

  assert.deepEqual(events, [
    { event: 'token', data: { content: '分块内容' } },
    { event: 'done', data: { elapsedMs: 800 } }
  ])
})

test('error 事件后继续接收 done，并保留错误码与耗时', async () => {
  const events = []
  await readRecipeEventStream(
    createReader([
      'event: error\ndata: {"code":"AI_TIMEOUT","message":"生成超时，请稍后重试"}\n\n',
      'event: done\ndata: {"elapsedMs":120000}\n\n'
    ]),
    (event) => events.push(event)
  )

  assert.deepEqual(events, [
    {
      event: 'error',
      data: { code: 'AI_TIMEOUT', message: '生成超时，请稍后重试' }
    },
    { event: 'done', data: { elapsedMs: 120000 } }
  ])
})
