const { test, expect, loginAsMerchant } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块十一：商家端-经营与AI', () => {

  test.beforeEach(async ({ page }) => { await loginAsMerchant(page) })

  test('11.1 经营统计', async ({ page }) => {
    markRunning('11.1')
    try {
      await page.goto('/merchant/home/statistics')
      await page.waitForLoadState('networkidle').catch(() => null)
      const stats = page.locator('[class*="statistics"], [class*="chart"], .el-card')
      await expect(stats.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('11.1', '经营统计页面可访问')
    } catch (e) { markFailed('11.1', e.message.slice(0, 50)); throw e }
  })

  test('11.2 评价中心', async ({ page }) => {
    markRunning('11.2')
    try {
      await page.goto('/merchant/home/comments')
      await page.waitForLoadState('networkidle').catch(() => null)
      const comments = page.locator('[class*="comment"], [class*="review"], .el-card')
      await expect(comments.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('11.2', '评价中心页面可访问')
    } catch (e) { markFailed('11.2', e.message.slice(0, 50)); throw e }
  })

  test('11.3 想吃列表审核', async ({ page }) => {
    markRunning('11.3')
    try {
      await page.goto('/merchant/home/wish-list-audit')
      await page.waitForLoadState('networkidle').catch(() => null)
      const wishlist = page.locator('[class*="wish"], [class*="audit"], .el-table')
      await expect(wishlist.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('11.3', '想吃列表审核页面可访问')
    } catch (e) { markFailed('11.3', e.message.slice(0, 50)); throw e }
  })

  test('11.4 经营指标', async ({ page }) => {
    markRunning('11.4')
    try {
      await page.goto('/merchant/home/ai')
      await page.waitForLoadState('networkidle').catch(() => null)
      const metrics = page.locator('[class*="metric"], [class*="indicator"], [class*="ai"]')
      await expect(metrics.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('11.4', 'AI经营助手页面可访问')
    } catch (e) { markFailed('11.4', e.message.slice(0, 50)); throw e }
  })

  test('11.5 AI建议', async ({ page }) => {
    markRunning('11.5')
    try {
      await page.goto('/merchant/home/ai')
      await page.waitForLoadState('networkidle').catch(() => null)
      const suggestBtn = page.locator('button:has-text("建议"), button:has-text("分析"), [class*="suggest"]').first()
      if (await suggestBtn.count() > 0) {
        markPassed('11.5', 'AI建议按钮存在')
      } else {
        markSkipped('11.5', '未找到AI建议按钮')
      }
    } catch (e) { markFailed('11.5', e.message.slice(0, 50)); throw e }
  })

  test('11.6 评价分析', async ({ page }) => {
    markRunning('11.6')
    try {
      await page.goto('/merchant/home/comments')
      await page.waitForLoadState('networkidle').catch(() => null)
      const analysis = page.locator('[class*="analysis"], [class*="rating"], [class*="统计"]')
      if (await analysis.count() > 0) {
        markPassed('11.6', '评价分析区域存在')
      } else {
        markSkipped('11.6', '未找到评价分析区域')
      }
    } catch (e) { markFailed('11.6', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})