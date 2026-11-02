const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

// 依赖 #196 的后端接口（GET/POST/DELETE /v1/content-extraction/xhs-session）。
// #196 合入前本 spec 会失败；PR 冒烟只跑 01/04/10，不会覆盖到它。
const SESSION_PATH = '/api/v1/content-extraction/xhs-session'
const AUTH_SOURCE_ID = 'e2e-xhs-auth-source'
const AUTH_SOURCE_TITLE = 'E2E 需要登录小红书'

async function authToken(page) {
  return page.evaluate(() => localStorage.getItem('auth_token'))
}

async function deleteSession(page) {
  const token = await authToken(page)
  if (!token) return
  await page.request.delete(SESSION_PATH, { headers: { Authorization: `Bearer ${token}` } })
}

// 真实 AUTH_REQUIRED 依赖小红书线上风控，不可控；这里注入固定来源保证入口可复现。
async function stubAuthRequiredSource(page) {
  await page.route('**/v1/content-extraction/sources', (route) => route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify({
      success: true,
      code: '200',
      message: '成功',
      data: [{
        id: AUTH_SOURCE_ID,
        title: AUTH_SOURCE_TITLE,
        platform: 'XIAOHONGSHU',
        platformName: '小红书',
        contentType: 'VIDEO',
        contentTypeName: '视频',
        contentUrl: 'https://www.xiaohongshu.com/explore/e2e-auth-required',
        extractionStatus: 'AUTH_REQUIRED',
        extractionStatusName: '需要登录小红书',
        extractionId: null,
        createdAt: new Date().toISOString()
      }]
    })
  }))
}

test.describe('模块十七：内容提取登录态绑定', () => {

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('17.1 小红书登录态绑定并自动重试', async ({ page }) => {
    markRunning('17.1')
    let reExtractCount = 0
    try {
      await stubAuthRequiredSource(page)
      await page.route('**/v1/content-extraction/source/*/re-extract', (route) => {
        reExtractCount += 1
        return route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, code: '200', message: '成功' })
        })
      })
      await deleteSession(page)

      await page.goto('/user/home/ai')
      await page.locator('#tab-btn-extraction').click()

      const card = page.locator('.source-card', { hasText: AUTH_SOURCE_TITLE }).first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await card.click()

      const dialog = page.locator('.el-dialog', { hasText: '绑定小红书登录态' })
      await expect(dialog).toBeVisible({ timeout: 10000 })
      await expect(dialog.locator('.xhs-status')).toHaveText('未绑定')

      // 首次绑定必须勾选确认后才能提交
      await dialog.locator('textarea').fill('a=1')
      const bindButton = dialog.getByRole('button', { name: '绑定并重试' })
      await expect(bindButton).toBeDisabled()
      await dialog.locator('.el-checkbox').click()
      await expect(bindButton).toBeEnabled()

      // 非法登录态（缺少 web_session）被服务端拒绝，并显示服务端返回的具体原因
      await bindButton.click()
      const errorToast = page.locator('.el-message--error').last()
      await expect(errorToast).toBeVisible({ timeout: 10000 })
      await expect(errorToast).toContainText(/web_session|登录态|cookie/i)

      // 格式合法的假登录态绑定成功 → 自动重试该条
      await dialog.locator('textarea').fill('web_session=e2e-fake-session; a=1')
      await bindButton.click()
      await expect(dialog).toBeHidden({ timeout: 10000 })
      await expect.poll(() => reExtractCount, { timeout: 10000 }).toBeGreaterThan(0)

      // 绑定状态可从服务端读回
      const token = await authToken(page)
      const statusResponse = await page.request.get(SESSION_PATH, {
        headers: { Authorization: `Bearer ${token}` }
      })
      const statusResult = await statusResponse.json()
      expect(statusResult.success).toBe(true)
      expect(statusResult.data.bound).toBe(true)

      // 解绑
      await card.click()
      await expect(dialog.locator('.xhs-status')).toHaveText('已绑定')
      await dialog.getByRole('button', { name: '解绑' }).click()
      await expect(dialog.locator('.xhs-status')).toHaveText('未绑定')

      markPassed('17.1', '弹窗 → 非法被拒 → 绑定成功并自动重试 → 解绑')
    } catch (e) {
      markFailed('17.1', e.message.slice(0, 60))
      throw e
    } finally {
      await deleteSession(page)
    }
  })

  test.afterAll(() => { updateSummary() })
})
