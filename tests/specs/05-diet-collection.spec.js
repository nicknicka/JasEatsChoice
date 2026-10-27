const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

test.describe('模块五：饮食管理与收藏', () => {

  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('5.1 卡路里统计', async ({ page }) => {
    markRunning('5.1')
    try {
      await page.goto('/user/home/calorie')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const chart = page.locator('[class*="calorie"], [class*="chart"], [class*="statistic"]')
      await expect(chart.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.1', '卡路里页面可访问')
    } catch (e) { markFailed('5.1', e.message.slice(0, 50)); throw e }
  })

  test('5.2 饮食记录', async ({ page }) => {
    markRunning('5.2')
    try {
      await page.goto('/user/home/diet-record')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const record = page.locator('[class*="diet"], [class*="record"], .el-card')
      await expect(record.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.2', '饮食记录页面可访问')
    } catch (e) { markFailed('5.2', e.message.slice(0, 50)); throw e }
  })

  test('5.3 今日食谱', async ({ page }) => {
    markRunning('5.3')
    try {
      await page.goto('/user/home/today-recipe')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const recipe = page.locator('[class*="recipe"], [class*="today"], .el-card')
      await expect(recipe.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.3', '今日食谱页面可访问')
    } catch (e) { markFailed('5.3', e.message.slice(0, 50)); throw e }
  })

  test('5.4 我的食谱', async ({ page }) => {
    markRunning('5.4')
    try {
      await page.goto('/user/home/my-recipe')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const recipe = page.locator('[class*="recipe"], .el-card')
      await expect(recipe.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.4', '我的食谱页面可访问')
    } catch (e) { markFailed('5.4', e.message.slice(0, 50)); throw e }
  })

  test('5.5 营养分析', async ({ page }) => {
    markRunning('5.5')
    try {
      await page.goto('/user/home/calorie')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const nutrition = page.locator('[class*="nutrition"], [class*="nutrient"], [class*="分析"]')
      await expect(nutrition.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.5', '营养分析区域存在')
    } catch (e) { markFailed('5.5', e.message.slice(0, 50)); throw e }
  })

  test('5.6 我的收藏列表', async ({ page }) => {
    markRunning('5.6')
    try {
      await page.goto('/user/home/my-collection')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const collection = page.locator('[class*="collection"], [class*="favorite"], .el-card')
      await expect(collection.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.6', '收藏页面可访问')
    } catch (e) { markFailed('5.6', e.message.slice(0, 50)); throw e }
  })

  test('5.7 添加收藏', async ({ page }) => {
    markRunning('5.7')
    try {
      // 收藏按钮在推荐页的菜品卡上（Recommend.vue item-actions 里的「☆ 收藏」），
      // 不在菜品详情页。点第一张推荐卡上的收藏按钮，走 handleFavoriteClick → toggleFavorite。
      await page.goto('/user/home/recommend')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('button.act-btn', { hasText: '收藏' }).first()).toBeVisible({ timeout: 10000 })
      await page.locator('button.act-btn', { hasText: '收藏' }).first().click()
      markPassed('5.7', '推荐页收藏按钮可点击')
    } catch (e) { markFailed('5.7', e.message.slice(0, 50)); throw e }
  })

  test('5.8 取消收藏', async ({ page }) => {
    markRunning('5.8')
    try {
      // 我的收藏页每张卡片右上角有 circle 删除按钮 .delete-btn（无文字，removeCollection）。
      // 只断言可点击入口存在，不实际删除种子收藏数据。
      await page.goto('/user/home/my-collection')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.collection-card .delete-btn').first()).toBeVisible({ timeout: 10000 })
      markPassed('5.8', '收藏项删除按钮存在')
    } catch (e) { markFailed('5.8', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
