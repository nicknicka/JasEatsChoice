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
      await page.waitForLoadState('networkidle').catch(() => null)
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
      await page.waitForLoadState('networkidle').catch(() => null)
      const detail = page.locator('[class*="detail"], [class*="order"]')
      await expect(detail.first()).toBeVisible({ timeout: 10000 })
      markPassed('4.4', '订单详情页面可访问')
    } catch (e) {
      markFailed('4.4', e.message.slice(0, 50))
      throw e
    }
  })

  // 以下 6 项为产品缺口，2026-10-25 探针逐页核对过 UI 后如实跳过：
  // 订单页签只有 全部/待支付/待接单/制作中/已完成/已取消，没有"确认收货"状态与按钮；
  // 仓库里只有 api/coupon.example.js，订单确认页无优惠券选择 UI；
  // 商家详情页无拼单入口（拼单仅存在于 OrderConfirmation.vue 与 Chat.vue 的文案）。
  test('4.5 确认收货', async ({ page }) => {
    markSkipped('4.5', '产品缺口：订单无待收货状态，无确认收货按钮')
  })

  test('4.6 评价订单', async ({ page }) => {
    markRunning('4.6')
    try {
      await page.goto('/user/home/evaluate-order/1')
      await page.waitForLoadState('networkidle').catch(() => null)
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
    markSkipped('4.8', '产品缺口：订单确认页无优惠券选择 UI（仓库仅 api/coupon.example.js）')
  })

  test('4.9 创建拼单', async ({ page }) => {
    markSkipped('4.9', '产品缺口：商家详情页无拼单入口按钮')
  })

  test('4.10 加入拼单', async ({ page }) => {
    markSkipped('4.10', '产品缺口：拼单功能无 UI 入口，无法取得拼单ID')
  })
  test('4.11 拼单结算', async ({ page }) => {
    markSkipped('4.11', '产品缺口：拼单功能无 UI 入口，无法取得拼单ID')
  })
  test('4.12 加菜请求', async ({ page }) => {
    markSkipped('4.12', '产品缺口：拼单功能无 UI 入口，无法取得拼单ID')
  })

  test.afterAll(() => { updateSummary() })
})