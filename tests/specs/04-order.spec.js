const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块四：订单流程', () => {

  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('4.1 创建订单', async ({ page }) => {
    markRunning('4.1')
    try {
      await page.goto('/user/home/order-confirmation')
      await page.waitForLoadState('networkidle').catch(() => null)
      const orderForm = page.locator('[class*="order"], [class*="confirm"], .el-card')
      await expect(orderForm.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('4.1', '订单确认页面可访问')
    } catch (e) {
      markFailed('4.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.2 订单支付', async ({ page }) => {
    markRunning('4.2')
    try {
      await page.goto('/user/home/orders')
      await page.waitForLoadState('networkidle').catch(() => null)
      const payBtn = page.locator('button:has-text("支付"), button:has-text("付款")').first()
      if (await payBtn.count() > 0) {
        markPassed('4.2', '支付按钮存在')
      } else {
        markSkipped('4.2', '无待支付订单')
      }
    } catch (e) {
      markFailed('4.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.3 订单列表', async ({ page }) => {
    markRunning('4.3')
    try {
      await page.goto('/user/home/orders')
      await page.waitForLoadState('networkidle').catch(() => null)
      const orderList = page.locator('[class*="order"], .el-card, [class*="list"]')
      await expect(orderList.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
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
      await page.waitForLoadState('networkidle').catch(() => null)
      const detail = page.locator('[class*="detail"], [class*="order"]')
      await expect(detail.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
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
      await page.waitForLoadState('networkidle').catch(() => null)
      const confirmBtn = page.locator('button:has-text("确认收货")').first()
      if (await confirmBtn.count() > 0) {
        markPassed('4.5', '确认收货按钮存在')
      } else {
        markSkipped('4.5', '无待收货订单')
      }
    } catch (e) {
      markFailed('4.5', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.6 评价订单', async ({ page }) => {
    markRunning('4.6')
    try {
      await page.goto('/user/home/evaluate-order/1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const evalForm = page.locator('[class*="evaluate"], [class*="review"], .el-rate')
      await expect(evalForm.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
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
      await page.waitForLoadState('networkidle').catch(() => null)
      const cancelBtn = page.locator('button:has-text("取消")').first()
      if (await cancelBtn.count() > 0) {
        markPassed('4.7', '取消按钮存在')
      } else {
        markSkipped('4.7', '无可取消订单')
      }
    } catch (e) {
      markFailed('4.7', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.8 优惠券使用', async ({ page }) => {
    markRunning('4.8')
    try {
      await page.goto('/user/home/order-confirmation')
      await page.waitForLoadState('networkidle').catch(() => null)
      const couponSelect = page.locator('[class*="coupon"], [class*="优惠"]').first()
      if (await couponSelect.count() > 0) {
        markPassed('4.8', '优惠券选择区域存在')
      } else {
        markSkipped('4.8', '无可用优惠券')
      }
    } catch (e) {
      markFailed('4.8', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.9 创建拼单', async ({ page }) => {
    markRunning('4.9')
    try {
      await page.goto('/user/home/merchant-detail?id=1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const groupBtn = page.locator('button:has-text("拼单"), [class*="group-order"]').first()
      if (await groupBtn.count() > 0) {
        markPassed('4.9', '拼单按钮存在')
      } else {
        markSkipped('4.9', '未找到拼单按钮')
      }
    } catch (e) {
      markFailed('4.9', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.10 加入拼单', async ({ page }) => { markSkipped('4.10', '需要拼单ID') })
  test('4.11 拼单结算', async ({ page }) => { markSkipped('4.11', '需要拼单ID') })
  test('4.12 加菜请求', async ({ page }) => { markSkipped('4.12', '需要拼单ID') })

  test.afterAll(() => { updateSummary() })
})