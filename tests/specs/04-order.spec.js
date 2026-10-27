const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块四：订单流程', () => {

  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('4.1 创建订单', async ({ page }) => {
    markRunning('4.1')
    try {
      await page.goto('/user/home/merchants')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('a[href*="merchant-detail"], [class*="merchant-card"], [class*="merchant-item"]').first().click()
      await page.waitForTimeout(1500)
      await page.locator('button:has-text("加入购物车")').first().click()
      await expect(page.locator('.el-message').filter({ hasText: '已加入购物车' })).toBeVisible({ timeout: 10000 })
      await page.locator('.draggable-cart-ball').click()
      const drawer = page.locator('.el-dialog').filter({ hasText: '我的购物车' })
      await expect(drawer).toBeVisible({ timeout: 10000 })
      await drawer.locator('button:has-text("提交订单")').click()
      await page.waitForURL('**/user/home/order-confirmation', { timeout: 15000 })
      await expect(page.locator('.el-card, [class*="order"]').first()).toBeVisible({ timeout: 10000 })
      markPassed('4.1', '加购→购物车→提交订单→订单确认页')
    } catch (e) {
      markFailed('4.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.2 订单支付', async ({ page }) => {
    markRunning('4.2')
    try {
      await page.goto('/user/home/orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const payBtn = page.locator('button:has-text("支付"), button:has-text("付款")').first()
      await expect(payBtn).toBeVisible({ timeout: 10000 })
      markPassed('4.2', '支付按钮存在')
    } catch (e) {
      markFailed('4.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.3 订单列表', async ({ page }) => {
    markRunning('4.3')
    try {
      await page.goto('/user/home/orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const orderList = page.locator('[class*="order"], .el-card, [class*="list"]')
      await expect(orderList.first()).toBeVisible({ timeout: 10000 })
      markPassed('4.3', '订单列表页面可访问')
    } catch (e) {
      markFailed('4.3', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.4 订单详情', async ({ page }) => {
    markRunning('4.4')
    try {
      await page.goto('/user/home/order-detail/1')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const detail = page.locator('[class*="detail"], [class*="order"]')
      await expect(detail.first()).toBeVisible({ timeout: 10000 })
      markPassed('4.4', '订单详情页面可访问')
    } catch (e) {
      markFailed('4.4', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.5 确认收货', async ({ page }) => {
    markRunning('4.5')
    try {
      await page.goto('/user/home/orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const confirmBtn = page.locator('button:has-text("确认收货")').first()
      await expect(confirmBtn).toBeVisible({ timeout: 10000 })
      markPassed('4.5', '确认收货按钮存在')
    } catch (e) { markFailed('4.5', e.message.slice(0, 50)); throw e }
  })

  test('4.6 评价订单', async ({ page }) => {
    markRunning('4.6')
    try {
      await page.goto('/user/home/evaluate-order/1')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const evalForm = page.locator('[class*="evaluate"], [class*="review"], .el-rate')
      await expect(evalForm.first()).toBeVisible({ timeout: 10000 })
      markPassed('4.6', '评价页面可访问')
    } catch (e) {
      markFailed('4.6', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.7 取消订单', async ({ page }) => {
    markRunning('4.7')
    try {
      await page.goto('/user/home/orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const cancelBtn = page.locator('button:has-text("取消")').first()
      await expect(cancelBtn).toBeVisible({ timeout: 10000 })
      markPassed('4.7', '取消按钮存在')
    } catch (e) {
      markFailed('4.7', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.8 优惠券使用', async ({ page }) => {
    markRunning('4.8')
    try {
      await page.goto('/user/home/order-confirmation')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.coupon-section')).toBeVisible({ timeout: 10000 })
      markPassed('4.8', '优惠券选择区域存在')
    } catch (e) { markFailed('4.8', e.message.slice(0, 50)); throw e }
  })

  test('4.9 创建拼单', async ({ page }) => {
    markRunning('4.9')
    try {
      await page.goto('/user/home/chat')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const createBtn = page.locator('button:has-text("创建群订单")').first()
      await expect(createBtn).toBeVisible({ timeout: 10000 })
      markPassed('4.9', '群聊创建群订单入口存在')
    } catch (e) { markFailed('4.9', e.message.slice(0, 50)); throw e }
  })

  test.fixme('4.10 加入拼单 (blocked by #37)', async ({ page }) => {
    await page.goto('/user/home/chat')
    await expect(page.locator('button:has-text("加入群订单")').first()).toBeVisible({ timeout: 10000 })
  })
  test('4.11 拼单结算', async ({ page }) => {
    markRunning('4.11')
    try {
      await page.goto('/user/home/chat')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const settleBtn = page.locator('button:has-text("去结算")').first()
      await expect(settleBtn).toBeVisible({ timeout: 10000 })
      markPassed('4.11', '拼单结算入口存在')
    } catch (e) { markFailed('4.11', e.message.slice(0, 50)); throw e }
  })
  test.fixme('4.12 加菜请求 (blocked by #40)', async ({ page }) => {
    await page.goto('/user/home/chat')
    await expect(page.locator('button:has-text("我要加菜")').first()).toBeVisible({ timeout: 10000 })
  })

  test.afterAll(() => {
    markSkipped('4.10', '#37', '加入群订单仍是假成功')
    markSkipped('4.12', '#40', '加菜链路尚未真实化')
    updateSummary()
  })
})
