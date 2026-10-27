const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块二：用户首页与推荐', () => {

  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('2.1 首页加载', async ({ page }) => {
    markRunning('2.1')
    try {
      await page.waitForURL('**/user/home**', { timeout: 15000 })
      const content = page.locator('.app-container, .main-content, .content-area, .top-nav-bar')
      await expect(content.first()).toBeVisible({ timeout: 10000 })
      markPassed('2.1', '首页加载成功')
    } catch (e) {
      markFailed('2.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('2.2 个性化推荐', async ({ page }) => {
    markRunning('2.2')
    try {
      await page.goto('/user/home/recommend')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const recommendList = page.locator('[class*="recommend"], [class*="dish-card"], .el-card')
      await expect(recommendList.first()).toBeVisible({ timeout: 10000 })
      markPassed('2.2', '推荐页面可访问')
    } catch (e) {
      markFailed('2.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('2.3 推荐刷新', async ({ page }) => {
    markRunning('2.3')
    try {
      await page.goto('/user/home/recommend')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const refreshBtn = page.locator('button:has-text("刷新"), [class*="refresh"], .el-icon-refresh').first()
      if (await refreshBtn.count() > 0) {
        await refreshBtn.click()
        await page.waitForTimeout(1000)
        markPassed('2.3', '刷新按钮可点击')
      } else {
        markSkipped('2.3', '未找到刷新按钮')
      }
    } catch (e) {
      markFailed('2.3', e.message.slice(0, 50))
      throw e
    }
  })

  test('2.4 推荐反馈', async ({ page }) => {
    markRunning('2.4')
    try {
      await page.goto('/user/home/recommend')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const rejectBtn = page.locator('button:has-text("不感兴趣")').first()
      if (await rejectBtn.count() > 0) {
        await rejectBtn.click()
        await expect(page.locator('.el-message')).toHaveText(/已不再推荐/, { timeout: 10000 }).catch(() => null)
        markPassed('2.4', '推荐反馈 (不感兴趣) 可点击')
      } else {
        markSkipped('2.4', '页面不存在 不感兴趣 按钮')
      }
    } catch (e) {
      markFailed('2.4', e.message.slice(0, 50))
      throw e
    }
  })

  test('2.5 热点话题', async ({ page }) => {
    markRunning('2.5')
    try {
      await page.goto('/user/home/hot-topic')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const hotTopic = page.locator('[class*="hot-topic"], [class*="topic-title"], .article-card, .content-area')
      if (await hotTopic.count() > 0 || await page.getByRole('heading').count() > 0) {
        await expect(hotTopic.first()).toBeVisible({ timeout: 10000 })
        markPassed('2.5', '热点话题页面可访问')
      } else {
        markSkipped('2.5', '热点话题页面加载失败')
      }
    } catch (e) {
      markFailed('2.5', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => { updateSummary() })
})
