const { test, expect, bootstrapAdminSession } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块十二：管理员端-用户与商家管理', () => {

  test.beforeEach(async ({ page }) => { await bootstrapAdminSession(page) })

  test('12.1 用户列表', async ({ page }) => {
    markRunning('12.1')
    try {
      await page.goto('/admin/users')
      await page.waitForLoadState('networkidle').catch(() => null)
      const users = page.locator('[class*="user"], .el-table, .el-card')
      await expect(users.first()).toBeVisible({ timeout: 10000 })
      markPassed('12.1', '用户管理页面可访问')
    } catch (e) { markFailed('12.1', e.message.slice(0, 50)); throw e }
  })

  test('12.2 用户详情', async ({ page }) => {
    markRunning('12.2')
    try {
      await page.goto('/admin/users')
      await page.waitForLoadState('networkidle').catch(() => null)
      const detailBtn = page.locator('button:has-text("详情"), button:has-text("查看"), a:has-text("详情")').first()
      if (await detailBtn.count() > 0) {
        markPassed('12.2', '用户详情按钮存在')
      } else {
        markSkipped('12.2', '无用户详情按钮')
      }
    } catch (e) { markFailed('12.2', e.message.slice(0, 50)); throw e }
  })

  test('12.3 用户状态管理', async ({ page }) => {
    markRunning('12.3')
    try {
      await page.goto('/admin/users')
      await page.waitForLoadState('networkidle').catch(() => null)
      // 产品缺口：UserManagement.vue 表格无状态列，编辑弹窗只有资料字段
      // （昵称/手机号/邮箱/头像/性别/生日/位置/简介），没有启用/禁用控件。
      // 如实跳过，不把"打开编辑弹窗"冒充成状态管理。
      markSkipped('12.3', '产品无用户状态管理控件：表格无状态列，编辑弹窗仅资料字段')
    } catch (e) { markFailed('12.3', e.message.slice(0, 50)); throw e }
  })

  test('12.4 商家列表', async ({ page }) => {
    markRunning('12.4')
    try {
      await page.goto('/admin/merchants')
      await page.waitForLoadState('networkidle').catch(() => null)
      const merchants = page.locator('[class*="merchant"], .el-table, .el-card')
      await expect(merchants.first()).toBeVisible({ timeout: 10000 })
      markPassed('12.4', '商家管理页面可访问')
    } catch (e) { markFailed('12.4', e.message.slice(0, 50)); throw e }
  })

  test('12.5 商家审核', async ({ page }) => {
    markRunning('12.5')
    try {
      await page.goto('/admin/merchants/audit')
      await page.waitForLoadState('networkidle').catch(() => null)
      const audit = page.locator('[class*="audit"], [class*="pending"], .el-table')
      await expect(audit.first()).toBeVisible({ timeout: 10000 })
      markPassed('12.5', '商家审核页面可访问')
    } catch (e) { markFailed('12.5', e.message.slice(0, 50)); throw e }
  })

  test('12.6 商家详情', async ({ page }) => {
    markRunning('12.6')
    try {
      await page.goto('/admin/merchants')
      await page.waitForLoadState('networkidle').catch(() => null)
      // MerchantManagement.vue 行内「查看」打开 el-dialog 详情（el-descriptions 含"商家ID"）
      await page.locator('.el-table button:has-text("查看")').first().click()
      await expect(page.locator('.el-dialog').filter({ hasText: '商家ID' })).toBeVisible({ timeout: 10000 })
      markPassed('12.6', '商家详情弹窗可打开')
    } catch (e) { markFailed('12.6', e.message.slice(0, 50)); throw e }
  })

  test('12.7 商家状态管理', async ({ page }) => {
    markRunning('12.7')
    try {
      await page.goto('/admin/merchants')
      await page.waitForLoadState('networkidle').catch(() => null)
      // 非 PENDING 商家行有「状态」按钮，走 ElMessageBox.prompt（inputType: select，营业中/已暂停）。
      // 只验证弹窗可打开，选「取消」收尾，不改任何商家状态。
      await page.locator('.el-table button:has-text("状态")').first().click()
      await expect(page.locator('.el-message-box').filter({ hasText: '修改状态' })).toBeVisible({ timeout: 10000 })
      await page.locator('.el-message-box button').filter({ hasText: '取消' }).click()
      markPassed('12.7', '商家状态修改弹窗可打开（已取消，未落库）')
    } catch (e) { markFailed('12.7', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
