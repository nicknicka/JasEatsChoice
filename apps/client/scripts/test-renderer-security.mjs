import assert from 'node:assert/strict'
import { test } from 'node:test'

import { buildHighlightSegments, escapeRegExp } from '../src/renderer/src/utils/searchHighlight.js'
import {
  isRetryableMethod,
  shouldRetryRequest
} from '../src/renderer/src/utils/apiRetry.js'
import { isSessionUsable } from '../src/renderer/src/utils/tokenCleaner.js'
import {
  forgetAccount,
  rememberAccount,
  sanitizeSavedAccounts
} from '../src/renderer/src/utils/savedAccounts.js'

// 这些用例覆盖 #567 的纯函数部分：搜索结果只切文本、密码不落盘、
// 只重试 GET/HEAD、解析失败的 token 不放行。

const encodePayload = (payload) =>
  Buffer.from(JSON.stringify(payload)).toString('base64').replace(/=+$/, '')

const tokenWith = (payload) => `e2e.${encodePayload(payload)}.e2e`

test('escapeRegExp 转义正则元字符，不抛异常', () => {
  assert.equal(escapeRegExp('('), '\\(')
  assert.equal(escapeRegExp('*'), '\\*')
  assert.equal(escapeRegExp('\\'), '\\\\')
  assert.equal(escapeRegExp('a.b?c'), 'a\\.b\\?c')
})

test('buildHighlightSegments 把消息原文切成纯文本片段，不生成 HTML', () => {
  const content = '<img src=x onerror=alert(1)> 你好世界'
  const segments = buildHighlightSegments(content, 'world')

  // 没有命中关键词时整体是纯文本
  assert.deepEqual(segments, [{ text: content, highlight: false }])

  const hit = buildHighlightSegments(content, 'img')
  assert.deepEqual(hit, [
    { text: '<', highlight: false },
    { text: 'img', highlight: true },
    { text: ' src=x onerror=alert(1)> 你好世界', highlight: false }
  ])
  // 任何片段的 text 都不含标签拼接出来的 HTML
  assert.equal(hit.map((segment) => segment.text).join(''), content)
})

test('buildHighlightSegments 命中时大小写不敏感且保留原文大小写', () => {
  assert.deepEqual(buildHighlightSegments('Hello World', 'world'), [
    { text: 'Hello ', highlight: false },
    { text: 'World', highlight: true }
  ])
})

test('buildHighlightSegments 对正则元字符关键词不抛异常', () => {
  for (const keyword of ['(', '*', '\\', '+?', '[a-z]']) {
    assert.doesNotThrow(() => buildHighlightSegments(`前缀 ${keyword} 后缀`, keyword))
    const segments = buildHighlightSegments(`前缀 ${keyword} 后缀`, keyword)
    assert.equal(segments.map((segment) => segment.text).join(''), `前缀 ${keyword} 后缀`)
  }
})

test('buildHighlightSegments 非法输入返回空数组', () => {
  assert.deepEqual(buildHighlightSegments('', 'x'), [])
  assert.deepEqual(buildHighlightSegments(null, 'x'), [])
})

test('只对 GET/HEAD 重试', () => {
  assert.equal(isRetryableMethod('GET'), true)
  assert.equal(isRetryableMethod('get'), true)
  assert.equal(isRetryableMethod('HEAD'), true)
  assert.equal(isRetryableMethod(undefined), true)
  assert.equal(isRetryableMethod('POST'), false)
  assert.equal(isRetryableMethod('PUT'), false)
  assert.equal(isRetryableMethod('PATCH'), false)
  assert.equal(isRetryableMethod('DELETE'), false)
})

test('shouldRetryRequest：GET 网络错误重试，POST 一律不重试', () => {
  const timeout = { code: 'ETIMEDOUT' }
  assert.equal(shouldRetryRequest({ config: { method: 'get' }, error: timeout, maxRetries: 2 }), true)
  assert.equal(shouldRetryRequest({ config: { method: 'post' }, error: timeout, maxRetries: 2 }), false)
  assert.equal(
    shouldRetryRequest({ config: { method: 'get' }, error: { response: { status: 500 } }, maxRetries: 2 }),
    true
  )
  assert.equal(
    shouldRetryRequest({ config: { method: 'get' }, error: { response: { status: 404 } }, maxRetries: 2 }),
    false
  )
  assert.equal(
    shouldRetryRequest({
      config: { method: 'get', retryCount: 2 },
      error: timeout,
      maxRetries: 2
    }),
    false
  )
})

test('isSessionUsable：解析失败或缺 exp 一律拒绝', () => {
  assert.equal(isSessionUsable(''), false)
  assert.equal(isSessionUsable(null), false)
  assert.equal(isSessionUsable('x'), false)
  assert.equal(isSessionUsable('a.b.c'), false)
  assert.equal(isSessionUsable(tokenWith({ userId: '1' })), false)
  assert.equal(isSessionUsable(tokenWith({ exp: Math.floor(Date.now() / 1000) - 10 })), false)
  assert.equal(isSessionUsable(tokenWith({ exp: Math.floor(Date.now() / 1000) + 3600 })), true)
})

test('savedAccounts 只持久化手机号', () => {
  const legacy = [
    { phone: '13800000001', password: 'plain-secret' },
    { username: '13800000002', password: 'another-secret' },
    { phone: '13800000001', password: 'dup' }
  ]
  const sanitized = sanitizeSavedAccounts(legacy)
  assert.deepEqual(sanitized, [{ phone: '13800000001' }, { phone: '13800000002' }])
  assert.equal(JSON.stringify(sanitized).includes('secret'), false)

  const remembered = rememberAccount(sanitized, '13800000003')
  assert.deepEqual(remembered, [
    { phone: '13800000001' },
    { phone: '13800000002' },
    { phone: '13800000003' }
  ])
  // 幂等：同一个手机号不重复追加
  assert.deepEqual(rememberAccount(remembered, '13800000003'), remembered)

  assert.deepEqual(forgetAccount(remembered, '13800000002'), [
    { phone: '13800000001' },
    { phone: '13800000003' }
  ])
})
