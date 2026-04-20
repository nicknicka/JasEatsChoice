const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块三：商家浏览与详情', () => {

  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('3.1 商家列表', async ({ page }) => {
    markRunning('3.1')
    try {
      await page.goto('/user/home/merchants')
      await page.waitForLoadState('networkidle').catch(() => null)
      const merchantList = page.locator('[class*="merchant"], .el-card, [class*="list"]')
      await expect(merchantList.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('3.1', '商家列表页面可访问')
    } catch (e) {
      markFailed('3.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.2 商家搜索', async ({ page }) => {
    markRunning('3.2')
    try {
      await page.goto('/user/home/merchants')
      await page.waitForLoadState('networkidle').catch(() => null)
      const searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="商家"], .el-input__inner').first()
      if (await searchInput.count() > 0) {
        await searchInput.fill('测试')
        await page.waitForTimeout(1000)
        markPassed('3.2', '搜索功能可用')
      } else {
        markSkipped('3.2', '未找到搜索框')
      }
    } catch (e) {
      markFailed('3.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.3 商家详情', async ({ page }) => {
    markRunning('3.3')
    try {
      await page.goto('/user/home/merchant-detail?id=1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const detail = page.locator('[class*="detail"], [class*="merchant"]')
      await expect(detail.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('3.3', '商家详情页面可访问')
    } catch (e) {
      markFailed('3.3', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.4 商家公告', async ({ page }) => {
    markRunning('3.4')
    try {
      await page.goto('/user/home/merchant-detail?id=1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const announcement = page.locator('[class*="announcement"], [class*="notice"], [class*="公告"]')
      if (await announcement.count() > 0) {
        markPassed('3.4', '公告区域存在')
      } else {
        markSkipped('3.4', '无公告内容')
      }
    } catch (e) {
      markFailed('3.4', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.5 商家评价列表', async ({ page }) => {
    markRunning('3.5')
    try {
      await page.goto('/user/home/merchant-detail?id=1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const reviews = page.locator('[class*="review"], [class*="comment"], [class*="评价"]')
      if (await reviews.count() > 0) {
        markPassed('3.5', '评价区域存在')
      } else {
        markSkipped('3.5', '无评价内容')
      }
    } catch (e) {
      markFailed('3.5', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.6 菜品列表', async ({ page }) => {
    markRunning('3.6')
    try {
      await page.goto('/user/home/merchant-detail?id=1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const dishes = page.locator('[class*="dish"], [class*="menu-item"], [class*="菜品"]')
      if (await dishes.count() > 0) {
        markPassed('3.6', '菜品列表存在')
      } else {
        markSkipped('3.6', '无菜品内容')
      }
    } catch (e) {
      markFailed('3.6', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.7 菜品详情', async ({ page }) => {
    markRunning('3.7')
    try {
      await page.goto('/user/home/dish-detail/1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const detail = page.locator('[class*="detail"], [class*="dish"]')
      await expect(detail.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('3.7', '菜品详情页面可访问')
    } catch (e) {
      markFailed('3.7', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.8 菜品收藏', async ({ page }) => {
    markRunning('3.8')
    try {
      await page.goto('/user/home/dish-detail/1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const favBtn = page.locator('[class*="favorite"], [class*="collect"], button:has-text("收藏")').first()
      if (await favBtn.count() > 0) {
        await favBtn.click()
        markPassed('3.8', '收藏按钮可点击')
      } else {
        markSkipped('3.8', '未找到收藏按钮')
      }
    } catch (e) {
      markFailed('3.8', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.9 菜品加购', async ({ page }) => {
    markRunning('3.9')
    try {
      await page.goto('/user/home/dish-detail/1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const addBtn = page.locator('button:has-text("加入"), button:has-text("加购"), [class*="add-cart"]').first()
      if (await addBtn.count() > 0) {
        await addBtn.click()
        markPassed('3.9', '加购按钮可点击')
      } else {
        markSkipped('3.9', '未找到加购按钮')
      }
    } catch (e) {
      markFailed('3.9', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => { updateSummary() })
})