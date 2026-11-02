const { test, expect } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

// 该用例通过 API contract mock 验证管理员页面的完整发布操作流；
// 外部 provider 行为由后端 AiRuntimeConfigReleaseAcceptanceTest 覆盖。
const SECRET_KEY = 'e2e-fake-key-98765432109876543210'
const ACTIVE_FINGERPRINT = '8d43aa55aa4f'
const NEW_FINGERPRINT = 'a5a1ba577d2c'
const EMBED_PROVIDER = 'openai-compatible'
const EMBED_MODEL = 'bge-m3'
const EMBED_DIMENSIONS = 1024
const EMBED_BASE_URL = 'http://host.docker.internal:11434/v1'
const NEW_EMBED_BASE_URL = 'http://embedding-new.example.com:11434/v1'
const EMBED_FINGERPRINT = '3c9f1a2b7d4e'
const NEW_EMBED_MODEL = 'bge-large-zh-v1.5'
const EMBED_REBUILD_FAILURE = '公告索引重建失败：connection refused'

function snapshot(model, version, fingerprint, embeddingOverride = {}) {
  return {
    provider: 'zhipu',
    model,
    supervisorModel: model,
    visionModel: 'glm-4.6v-flash',
    baseUrl: 'https://candidate.example.com',
    timeoutMs: 30000,
    temperature: 0.7,
    updatedAt: '2026-10-31T09:00:00',
    version,
    keyStatus: { configured: true, fingerprint, updatedAt: '2026-10-31T08:30:00' },
    capabilities: {
      available: true,
      textChat: true,
      streaming: true,
      vision: true,
      functionCalling: true,
      agentic: true,
      unavailableMessage: ''
    },
    supportedModels: ['glm-4-flash', 'glm-4.6v-flash', 'glm-4.1v-thinking-flash'],
    providers: [{ code: 'zhipu', name: '智谱', models: ['glm-4-flash', 'glm-4.6v-flash', 'glm-4.1v-thinking-flash'] }],
    embedding: {
      provider: EMBED_PROVIDER,
      model: EMBED_MODEL,
      dimensions: EMBED_DIMENSIONS,
      baseUrl: EMBED_BASE_URL,
      providers: ['zhipu', 'openai-compatible'],
      keyStatus: { configured: true, fingerprint: EMBED_FINGERPRINT, updatedAt: '2026-10-31T08:35:00' },
      ...embeddingOverride
    }
  }
}

test.describe('AI 热加载发布验收', () => {
  test('16.1 查看、测试、保存、热切换、脱敏和回滚', async ({ page }) => {
    markRunning('16.1')
    const currentModel = 'glm-4-flash'
    const candidateModel = 'glm-4.1v-thinking-flash'
    const activeSnapshot = snapshot(currentModel, 2, ACTIVE_FINGERPRINT, { state: 'READY' })
    const unavailableSnapshot = snapshot(candidateModel, 3, NEW_FINGERPRINT, {
      model: NEW_EMBED_MODEL,
      baseUrl: NEW_EMBED_BASE_URL,
      state: 'UNAVAILABLE',
      statusReason: EMBED_REBUILD_FAILURE
    })
    let currentCallCount = 0
    const previousSummary = {
      version: 1,
      updatedAt: '2026-10-31T08:00:00',
      provider: 'zhipu',
      model: currentModel,
      supervisorModel: currentModel,
      visionModel: 'glm-4.6v-flash',
      baseUrl: '',
      timeoutMs: 30000,
      temperature: 0.7,
      keyConfigured: true,
      keyFingerprint: ACTIVE_FINGERPRINT
    }
    const apiResponses = []
    const capturedRequests = {}

    page.on('response', async response => {
      if (!new URL(response.url()).pathname.startsWith('/api/admin/')) return
      try { apiResponses.push(await response.text()) } catch { apiResponses.push('') }
    })

    await page.route('**/api/admin/settings/config/group/*', route =>
      route.fulfill({ json: { success: true, data: {} } }))
    await page.route('**/api/admin/settings/ai/current', route => {
      // #188 /current 需按调用次序返回不同快照：首屏 READY，刷新状态时返回 UNAVAILABLE
      const payload = currentCallCount === 0 ? activeSnapshot : unavailableSnapshot
      currentCallCount += 1
      return route.fulfill({ json: { success: true, data: payload } })
    })
    await page.route('**/api/admin/settings/ai/previous-summary', route =>
      route.fulfill({ json: { success: true, data: previousSummary } }))
    await page.route('**/api/admin/settings/ai/models', route =>
      route.fulfill({ json: { success: true, data: [
        { id: 'glm-4-flash', name: 'glm-4-flash', supportsImages: false },
        { id: 'glm-4.6v-flash', name: 'glm-4.6v-flash', supportsImages: true },
        { id: 'glm-4.1v-thinking-flash', name: 'glm-4.1v-thinking-flash', supportsImages: true }
      ] } }))
    await page.route('**/api/admin/settings/ai/test', route => {
      capturedRequests.test = route.request().postDataJSON()
      return route.fulfill({ json: { success: true, data: { success: true, message: '连接成功', model: candidateModel, testedAt: '2026-10-31T09:01:00' } } })
    })
    await page.route('**/api/admin/settings/ai/activate', route => {
      capturedRequests.activate = route.request().postDataJSON()
      // #188 换 Embedding 模型保存后后端返回重建中的快照
      return route.fulfill({ json: { success: true, data: snapshot(candidateModel, 3, NEW_FINGERPRINT, {
        model: NEW_EMBED_MODEL,
        baseUrl: NEW_EMBED_BASE_URL,
        state: 'REBUILDING',
        statusReason: '正在重建公告索引'
      }) } })
    })
    let rebuildCalls = 0
    await page.route('**/api/admin/settings/ai/embedding/rebuild', route => {
      rebuildCalls += 1
      return route.fulfill({ json: { success: true, data: snapshot(candidateModel, 3, NEW_FINGERPRINT, {
        model: NEW_EMBED_MODEL,
        baseUrl: NEW_EMBED_BASE_URL,
        state: 'READY'
      }) } })
    })
    await page.route('**/api/admin/settings/ai/rollback', route =>
      route.fulfill({ json: { success: true, data: activeSnapshot } }))

    await page.addInitScript(() => {
      const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })).replace(/=+$/, '')
      localStorage.setItem('admin_token', `e2e.${payload}.e2e`)
      localStorage.setItem('admin_info', JSON.stringify({ id: 1, username: 'admin' }))
    })

    try {
      await page.goto('/admin/settings')
      // Element Plus 下拉的过渡动画会让元素在无头浏览器下持续移动，Playwright 会报
      // "element is not stable"（见 #174）。测试里禁用过渡与动画，避免依赖浏览器通道。
      await page.addStyleTag({
        content: '*, *::before, *::after { transition: none !important; animation: none !important; }'
      })
      await page.getByRole('tab', { name: 'AI 模型' }).click()
      const aiSection = page.locator('.el-tab-pane').filter({ hasText: '当前配置' })

      await expect(page.getByText('首期仅支持单后端实例')).toBeVisible()
      await expect(page.getByText('Embedding 模型与向量维度不在线切换')).toHaveCount(0)

      const embeddingSummary = aiSection.locator('.embedding-summary')
      await expect(embeddingSummary.getByText(EMBED_PROVIDER)).toBeVisible()
      await expect(embeddingSummary.getByText(EMBED_MODEL)).toBeVisible()
      await expect(embeddingSummary.getByText(String(EMBED_DIMENSIONS))).toBeVisible()
      await expect(embeddingSummary.getByText(`指纹：${EMBED_FINGERPRINT}`)).toBeVisible()

      await expect(aiSection.getByText(currentModel).first()).toBeVisible()
      await expect(aiSection.getByText(`指纹：${ACTIVE_FINGERPRINT}`)).toBeVisible()
      await expect(aiSection.getByText('更新时间：2026-10-31T08:30:00')).toBeVisible()

      const formItem = label => aiSection.locator('.el-form-item').filter({ hasText: label, hasNotText: 'Embedding' })
      const embeddingItem = label => aiSection.locator('.embedding-candidate .el-form-item').filter({ hasText: label })
      await formItem('API Key').locator('input[type="password"]').fill(SECRET_KEY)

      await formItem('Provider').locator('.el-select').click()
      await page.getByRole('option', { name: '智谱' }).click()

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/models'),
        formItem('模型列表').getByRole('button', { name: '从网关获取模型' }).click()
      ])
      await expect(aiSection.getByText('已获取 3 个模型')).toBeVisible()

      await formItem('聊天模型').locator('.el-select').click()
      await page.getByRole('option', { name: candidateModel, exact: true }).last().click()

      await formItem('Supervisor 模型').locator('.el-select').click()
      await page.getByRole('option', { name: candidateModel, exact: true }).last().click()

      await formItem('视觉模型').locator('.el-select').click()
      await page.getByRole('option', { name: 'glm-4.6v-flash', exact: true }).last().click()

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/test'),
        formItem('测试候选配置').getByRole('button', { name: '测试候选配置' }).click()
      ])
      await expect(aiSection.getByText('连通成功')).toBeVisible()
      expect(capturedRequests.test.embedding).toMatchObject({
        provider: EMBED_PROVIDER,
        model: EMBED_MODEL,
        dimensions: EMBED_DIMENSIONS,
        baseUrl: EMBED_BASE_URL,
        apiKey: ''
      })

      await embeddingItem('服务地址').locator('input').fill(NEW_EMBED_BASE_URL)
      await embeddingItem('Embedding 模型').locator('input').fill(NEW_EMBED_MODEL)

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/activate'),
        page.getByRole('button', { name: '保存并激活' }).click()
      ])
      await expect(aiSection.getByText(candidateModel).first()).toBeVisible()
      await expect(aiSection.getByText(`指纹：${NEW_FINGERPRINT}`)).toBeVisible()
      await expect(formItem('API Key').locator('input[type="password"]')).toHaveValue('')
      expect(capturedRequests.activate.embedding).toMatchObject({
        provider: EMBED_PROVIDER,
        model: NEW_EMBED_MODEL,
        dimensions: EMBED_DIMENSIONS,
        baseUrl: NEW_EMBED_BASE_URL,
        apiKey: ''
      })
      await expect(embeddingSummary.getByText('重建中')).toBeVisible()

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/embedding/rebuild'),
        page.getByRole('button', { name: '重试重建' }).click()
      ])
      await expect(embeddingSummary.getByText('就绪')).toBeVisible()
      expect(rebuildCalls, '重试重建应发出请求').toBe(1)

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/current'),
        page.getByRole('button', { name: '刷新状态' }).click()
      ])
      await expect(embeddingSummary.getByText('不可用')).toBeVisible()
      await expect(embeddingSummary.getByText(EMBED_REBUILD_FAILURE)).toBeVisible()

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/rollback'),
        page.getByRole('button', { name: '回滚上一版' }).click()
      ])
      await expect(aiSection.getByText(currentModel).first()).toBeVisible()
      await expect(aiSection.getByText(`指纹：${ACTIVE_FINGERPRINT}`)).toBeVisible()

      const pageText = await page.locator('body').innerText()
      expect(pageText, '页面不得出现 key 明文').not.toContain(SECRET_KEY)
      for (const responseBody of apiResponses) {
        expect(responseBody, 'API 响应不得出现 key 明文').not.toContain(SECRET_KEY)
      }
      markPassed('16.1', '查看/测试/保存/热切换/脱敏/回滚 UI 流通过')
    } catch (e) {
      markFailed('16.1', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => { updateSummary() })
})
