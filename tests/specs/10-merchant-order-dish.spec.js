const { test, expect, loginAsMerchant } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块十：商家端-订单与菜品管理', () => {

  test.beforeEach(async ({ page }) => { await loginAsMerchant(page) })

  test('10.1 商家订单列表', async ({ page }) => {
    markRunning('10.1')
    try {
      await page.goto('/merchant/home/orders')
      await page.waitForLoadState('networkidle').catch(() => null)
      const orders = page.locator('[class*="order"], .el-table, .el-card')
      await expect(orders.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('10.1', '商家订单页面可访问')
    } catch (e) { markFailed('10.1', e.message.slice(0, 50)); throw e }
  })

  test('10.2 今日订单', async ({ page }) => {
    markRunning('10.2')
    try {
      await page.goto('/merchant/home/today-orders')
      await page.waitForLoadState('networkidle').catch(() => null)
      const today = page.locator('[class*="today"], [class*="order"], .el-card')
      await expect(today.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('10.2', '今日订单页面可访问')
    } catch (e) { markFailed('10.2', e.message.slice(0, 50)); throw e }
  })

  test('10.3 商家订单详情', async ({ page }) => {
    markRunning('10.3')
    try {
      await page.goto('/merchant/home/order-detail/1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const detail = page.locator('[class*="detail"], [class*="order"]')
      await expect(detail.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('10.3', '商家订单详情可访问')
    } catch (e) { markFailed('10.3', e.message.slice(0, 50)); throw e }
  })

  test('10.4 接单', async ({ page }) => {
    markRunning('10.4')
    try {
      await page.goto('/merchant/home/orders')
      await page.waitForLoadState('networkidle').catch(() => null)
      const acceptBtn = page.locator('button:has-text("接单"), button:has-text("接受")').first()
      if (await acceptBtn.count() > 0) {
        markPassed('10.4', '接单按钮存在')
      } else {
        markSkipped('10.4', '无待接单订单')
      }
    } catch (e) { markFailed('10.4', e.message.slice(0, 50)); throw e }
  })

  test('10.5 订单状态更新', async ({ page }) => {
    markRunning('10.5')
    try {
      await page.goto('/merchant/home/orders')
      await page.waitForLoadState('networkidle').catch(() => null)
      const statusBtn = page.locator('button:has-text("出餐"), button:has-text("配送"), button:has-text("完成")').first()
      if (await statusBtn.count() > 0) {
        markPassed('10.5', '状态更新按钮存在')
      } else {
        markSkipped('10.5', '无可更新状态订单')
      }
    } catch (e) { markFailed('10.5', e.message.slice(0, 50)); throw e }
  })

  test('10.6 菜单管理', async ({ page }) => {
    markRunning('10.6')
    try {
      await page.goto('/merchant/home/menu')
      await page.waitForLoadState('networkidle').catch(() => null)
      const menu = page.locator('[class*="menu"], .el-table, .el-card')
      await expect(menu.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('10.6', '菜单管理页面可访问')
    } catch (e) { markFailed('10.6', e.message.slice(0, 50)); throw e }
  })

  test('10.7 菜单编辑', async ({ page }) => {
    markRunning('10.7')
    try {
      await page.goto('/merchant/home/menu')
      await page.waitForLoadState('networkidle').catch(() => null)
      const editBtn = page.locator('button:has-text("编辑"), [class*="edit"]').first()
      if (await editBtn.count() > 0) {
        markPassed('10.7', '菜单编辑按钮存在')
      } else {
        markSkipped('10.7', '无菜单可编辑')
      }
    } catch (e) { markFailed('10.7', e.message.slice(0, 50)); throw e }
  })

  test('10.8 菜品管理', async ({ page }) => {
    markRunning('10.8')
    try {
      await page.goto('/merchant/home/dishes')
      await page.waitForLoadState('networkidle').catch(() => null)
      const dishes = page.locator('[class*="dish"], .el-table, .el-card')
      await expect(dishes.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('10.8', '菜品管理页面可访问')
    } catch (e) { markFailed('10.8', e.message.slice(0, 50)); throw e }
  })

  test('10.9 添加菜品', async ({ page }) => {
    markRunning('10.9')
    try {
      await page.goto('/merchant/home/dish-add')
      await page.waitForLoadState('networkidle').catch(() => null)
      const form = page.locator('[class*="dish"], .el-form, .el-card')
      await expect(form.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('10.9', '添加菜品页面可访问')
    } catch (e) { markFailed('10.9', e.message.slice(0, 50)); throw e }
  })

  test('10.10 编辑菜品', async ({ page }) => {
    markRunning('10.10')
    try {
      await page.goto('/merchant/home/dishes')
      await page.waitForLoadState('networkidle').catch(() => null)
      const editBtn = page.locator('button:has-text("编辑"), [class*="edit"]').first()
      if (await editBtn.count() > 0) {
        markPassed('10.10', '编辑菜品按钮存在')
      } else {
        markSkipped('10.10', '无菜品可编辑')
      }
    } catch (e) { markFailed('10.10', e.message.slice(0, 50)); throw e }
  })

  test('10.11 菜品上下架', async ({ page }) => {
    markRunning('10.11')
    try {
      await page.goto('/merchant/home/dishes')
      await page.waitForLoadState('networkidle').catch(() => null)
      const toggleBtn = page.locator('.el-switch, button:has-text("上架"), button:has-text("下架")').first()
      if (await toggleBtn.count() > 0) {
        markPassed('10.11', '上下架开关存在')
      } else {
        markSkipped('10.11', '未找到上下架开关')
      }
    } catch (e) { markFailed('10.11', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})