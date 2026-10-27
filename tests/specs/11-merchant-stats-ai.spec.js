const { test, expect, loginAsMerchant } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

test.describe('模块十一：商家端-经营与AI', () => {

  test.beforeEach(async ({ page }) => { await loginAsMerchant(page) })

  test('11.1 经营统计', async ({ page }) => {
    markRunning('11.1')
    try {
      await page.goto('/merchant/home/statistics')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const stats = page.locator('[class*="statistics"], [class*="chart"], .el-card')
      await expect(stats.first()).toBeVisible({ timeout: 10000 })
      markPassed('11.1', '经营统计页面可访问')
    } catch (e) { markFailed('11.1', e.message.slice(0, 50)); throw e }
  })

  test('11.2 评价中心', async ({ page }) => {
    markRunning('11.2')
    try {
      await page.goto('/merchant/home/comments')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const comments = page.locator('[class*="comment"], [class*="review"], .el-card')
      await expect(comments.first()).toBeVisible({ timeout: 10000 })
      markPassed('11.2', '评价中心页面可访问')
    } catch (e) { markFailed('11.2', e.message.slice(0, 50)); throw e }
  })

  test('11.3 想吃列表审核', async ({ page }) => {
    markRunning('11.3')
    try {
      await page.goto('/merchant/home/wish-list-audit')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const wishlist = page.locator('[class*="wish"], [class*="audit"], .el-table')
      await expect(wishlist.first()).toBeVisible({ timeout: 10000 })
      markPassed('11.3', '想吃列表审核页面可访问')
    } catch (e) { markFailed('11.3', e.message.slice(0, 50)); throw e }
  })

  test('11.4 经营指标', async ({ page }) => {
    markRunning('11.4')
    try {
      await page.goto('/merchant/home/ai')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const metrics = page.locator('[class*="metric"], [class*="indicator"], [class*="ai"]')
      await expect(metrics.first()).toBeVisible({ timeout: 10000 })
      markPassed('11.4', 'AI经营助手页面可访问')
    } catch (e) { markFailed('11.4', e.message.slice(0, 50)); throw e }
  })

  test('11.5 AI建议', async ({ page }) => {
    markRunning('11.5')
    try {
      await page.goto('/merchant/home/ai')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      // 探针确认：商家 AI 页四个 tab（经营助手/经营洞察/评价回复/菜品描述），
      // 「经营洞察」渲染 BusinessInsight：时间切换 .time-tabs + 指标卡 .metric-card。
      await page.locator('#merchant-tab-insight').click()
      await expect(page.locator('.business-insight')).toBeVisible({ timeout: 10000 })
      await expect(page.locator('.metric-card').first()).toBeVisible({ timeout: 10000 })
      markPassed('11.5', '经营洞察 tab 可打开，指标卡可见')
    } catch (e) { markFailed('11.5', e.message.slice(0, 50)); throw e }
  })

  test('11.6 评价分析', async ({ page }) => {
    markRunning('11.6')
    try {
      await page.goto('/merchant/home/comments')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const analysis = page.locator('[class*="analysis"], [class*="rating"], [class*="统计"]')
      await expect(analysis.first()).toBeVisible({ timeout: 10000 })
      markPassed('11.6', '评价分析区域存在')
    } catch (e) { markFailed('11.6', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
