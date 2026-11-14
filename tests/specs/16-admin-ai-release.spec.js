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
const PROVIDER = 'openai-compatible'
const CURRENT_MODEL = 'gpt-4o-mini'
const CANDIDATE_MODEL = 'gpt-4o'
const VISION_MODEL = 'gpt-4o'
const SUPPORTED_MODELS = [CURRENT_MODEL, CANDIDATE_MODEL, 'gpt-4.1']

function snapshot(model, version, fingerprint, embeddingOverride = {}) {
  return {
    provider: PROVIDER,
    model,
    supervisorModel: model,
    visionModel: VISION_MODEL,
    baseUrl: 'https://candidate.example.com',
    timeoutMs: 30000,
    temperature: 0.7,
    visionVerified: true,
    visionVerificationReason: '',
    needsReconfiguration: false,
    configurationNotice: '',
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
    supportedModels: SUPPORTED_MODELS,
    providers: [{ code: PROVIDER, name: 'OpenAI 兼容', models: SUPPORTED_MODELS }],
    embedding: {
      provider: EMBED_PROVIDER,
      model: EMBED_MODEL,
      dimensions: EMBED_DIMENSIONS,
      baseUrl: EMBED_BASE_URL,
      providers: [PROVIDER],
      keyStatus: { configured: true, fingerprint: EMBED_FINGERPRINT, updatedAt: '2026-10-31T08:35:00' },
      ...embeddingOverride
    }
  }
}

test.describe('AI 热加载发布验收', () => {
  test('16.1 查看、测试、保存、热切换、脱敏和回滚', async ({ page }) => {
    markRunning('16.1')
    const currentModel = CURRENT_MODEL
    const candidateModel = CANDIDATE_MODEL
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
      provider: PROVIDER,
      model: currentModel,
      supervisorModel: currentModel,
      visionModel: VISION_MODEL,
      baseUrl: '',
      timeoutMs: 30000,
      temperature: 0.7,
      visionVerified: true,
      visionVerificationReason: '',
      needsReconfiguration: false,
      configurationNotice: '',
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
    // 本用例刻意用假令牌 + 全量 stub 跑，不真实登录。SecurityConfig 默认拒绝后
    // /admin/current 会返回 401，触发全局拦截器清管理员态并跳登录页（#576）。
    // 布局挂载即调它，必须一并 stub，否则页面渲染后被弹走。
    await page.route('**/api/admin/current', route =>
      route.fulfill({ json: { success: true, admin: { adminId: 1, username: 'admin', realName: '管理员', roleCode: 'SUPER_ADMIN', roleName: '超级管理员' } } }))
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
        { id: CURRENT_MODEL, name: CURRENT_MODEL, supportsImages: false },
        { id: CANDIDATE_MODEL, name: CANDIDATE_MODEL, supportsImages: true },
        { id: 'gpt-4.1', name: 'gpt-4.1', supportsImages: true }
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
      const aiSection = page.locator('.el-tab-pane').filter({ hasText: 'AI 服务' })

      await expect(page.getByText('首期仅支持单后端实例')).toBeVisible()
      await expect(page.getByText('Embedding 模型与向量维度不在线切换')).toHaveCount(0)

      const embeddingSummary = aiSection.locator('.embedding-summary')
      await expect(embeddingSummary.getByText(EMBED_PROVIDER)).toBeVisible()
      await expect(embeddingSummary.getByText(EMBED_MODEL)).toBeVisible()
      await expect(embeddingSummary.getByText(String(EMBED_DIMENSIONS))).toBeVisible()
      await expect(embeddingSummary.getByText(`指纹：${EMBED_FINGERPRINT}`)).toBeVisible()

      await expect(aiSection.getByText(currentModel).first()).toBeVisible()
      await expect(aiSection.getByText(`指纹：${ACTIVE_FINGERPRINT}`)).toBeVisible()
      await expect(aiSection.getByText('更新于 2026-10-31T09:00:00')).toBeVisible()

      // 只读态 -> 编辑态：候选配置收在「修改配置」编辑卡里
      await aiSection.getByRole('button', { name: '修改配置' }).click()

      const formItem = label => aiSection.locator('.el-form-item').filter({ hasText: label, hasNotText: 'Embedding' })
      const embeddingItem = label => aiSection.locator('.embedding-candidate .el-form-item').filter({ hasText: label })
      await formItem('API Key').first().locator('input[type="password"]').fill(SECRET_KEY)

      await formItem('服务商').locator('.el-select').click()
      await page.getByRole('option', { name: 'OpenAI 兼容' }).click()

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/models'),
        formItem('模型列表').getByRole('button', { name: '从网关获取模型' }).click()
      ])
      await expect(aiSection.getByText('已获取 3 个模型')).toBeVisible()

      await formItem('聊天模型').locator('.el-select').click()
      await page.getByRole('option', { name: candidateModel, exact: true }).last().click()

      // Supervisor 收在「高级设置」里，默认跟随聊天模型
      await formItem('高级设置').locator('.el-switch').click()
      await formItem('Supervisor').locator('.el-select').click()
      await page.getByRole('option', { name: candidateModel, exact: true }).last().click()

      await formItem('视觉模型').locator('.el-select').click()
      await page.getByRole('option', { name: VISION_MODEL, exact: true }).last().click()

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/test'),
        aiSection.getByRole('button', { name: '测试候选配置' }).click()
      ])
      await expect(aiSection.getByText('测试通过')).toBeVisible()
      expect(capturedRequests.test.embedding).toMatchObject({
        provider: EMBED_PROVIDER,
        model: EMBED_MODEL,
        dimensions: EMBED_DIMENSIONS,
        baseUrl: EMBED_BASE_URL,
        apiKey: ''
      })

      await embeddingItem('服务地址').locator('input').fill(NEW_EMBED_BASE_URL)
      await embeddingItem('模型').locator('input').fill(NEW_EMBED_MODEL)

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/activate'),
        page.getByRole('button', { name: '保存并激活' }).click()
      ])
      await expect(aiSection.getByText(candidateModel).first()).toBeVisible()
      await expect(aiSection.getByText(`指纹：${NEW_FINGERPRINT}`)).toBeVisible()
      await expect(aiSection.locator('input[type="password"]')).toHaveCount(0)
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
        aiSection.getByRole('button', { name: '重试重建索引' }).click()
      ])
      await expect(embeddingSummary.getByText('就绪')).toBeVisible()
      expect(rebuildCalls, '重试重建应发出请求').toBe(1)

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/current'),
        page.getByRole('button', { name: '刷新状态' }).click()
      ])
      await expect(embeddingSummary.getByText('不可用')).toBeVisible()
      await expect(embeddingSummary.getByText(EMBED_REBUILD_FAILURE)).toBeVisible()

      // 配置历史收在折叠面板里，先展开再回滚
      await aiSection.getByText('配置历史').click()
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
