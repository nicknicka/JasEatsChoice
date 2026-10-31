const { test, expect } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

// 该用例通过 API contract mock 验证管理员页面的完整发布操作流；
// 外部 provider 行为由后端 AiRuntimeConfigReleaseAcceptanceTest 覆盖。
const SECRET_KEY = 'e2e-fake-key-98765432109876543210'
const ACTIVE_FINGERPRINT = '8d43aa55aa4f'
const NEW_FINGERPRINT = 'a5a1ba577d2c'

function snapshot(model, version, fingerprint) {
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
    providers: [{ code: 'zhipu', name: '智谱', models: ['glm-4-flash', 'glm-4.6v-flash', 'glm-4.1v-thinking-flash'] }]
  }
}

test.describe('AI 热加载发布验收', () => {
  test('16.1 查看、测试、保存、热切换、脱敏和回滚', async ({ page }) => {
    markRunning('16.1')
    const currentModel = 'glm-4-flash'
    const candidateModel = 'glm-4.1v-thinking-flash'
    const activeSnapshot = snapshot(currentModel, 2, ACTIVE_FINGERPRINT)
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

    page.on('response', async response => {
      if (!new URL(response.url()).pathname.startsWith('/api/admin/')) return
      try { apiResponses.push(await response.text()) } catch { apiResponses.push('') }
    })

    await page.route('**/api/admin/settings/config/group/*', route =>
      route.fulfill({ json: { success: true, data: {} } }))
    await page.route('**/api/admin/settings/ai/current', route =>
      route.fulfill({ json: { success: true, data: activeSnapshot } }))
    await page.route('**/api/admin/settings/ai/previous-summary', route =>
      route.fulfill({ json: { success: true, data: previousSummary } }))
    await page.route('**/api/admin/settings/ai/test', route =>
      route.fulfill({ json: { success: true, data: { success: true, message: '连接成功', model: candidateModel, testedAt: '2026-10-31T09:01:00' } } }))
    await page.route('**/api/admin/settings/ai/activate', route =>
      route.fulfill({ json: { success: true, data: snapshot(candidateModel, 3, NEW_FINGERPRINT) } }))
    await page.route('**/api/admin/settings/ai/rollback', route =>
      route.fulfill({ json: { success: true, data: activeSnapshot } }))

    await page.addInitScript(() => {
      const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 })).replace(/=+$/, '')
      localStorage.setItem('admin_token', `e2e.${payload}.e2e`)
      localStorage.setItem('admin_info', JSON.stringify({ id: 1, username: 'admin' }))
    })

    try {
      await page.goto('/admin/settings')
      await page.getByRole('tab', { name: 'AI 模型' }).click()
      const aiSection = page.locator('.el-tab-pane').filter({ hasText: '当前配置' })

      await expect(page.getByText('首期仅支持单后端实例')).toBeVisible()
      await expect(aiSection.getByText(currentModel).first()).toBeVisible()
      await expect(aiSection.getByText(`指纹：${ACTIVE_FINGERPRINT}`)).toBeVisible()
      await expect(aiSection.getByText('更新时间：2026-10-31T08:30:00')).toBeVisible()

      const formItem = label => aiSection.locator('.el-form-item').filter({ hasText: label })
      await formItem('API Key').locator('input[type="password"]').fill(SECRET_KEY)

      await formItem('Provider').locator('.el-select').click()
      await page.getByRole('option', { name: '智谱' }).click()

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

      await Promise.all([
        page.waitForResponse(response => new URL(response.url()).pathname === '/api/admin/settings/ai/activate'),
        page.getByRole('button', { name: '保存并激活' }).click()
      ])
      await expect(aiSection.getByText(candidateModel).first()).toBeVisible()
      await expect(aiSection.getByText(`指纹：${NEW_FINGERPRINT}`)).toBeVisible()
      await expect(formItem('API Key').locator('input[type="password"]')).toHaveValue('')

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
