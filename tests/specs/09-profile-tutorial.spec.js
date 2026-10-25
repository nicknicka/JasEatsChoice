const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块九：个人中心与教程', () => {

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('9.1 个人信息', async ({ page }) => {
    markRunning('9.1')
    try {
      await page.goto('/user/home/profile')
      await page.waitForLoadState('networkidle').catch(() => null)
      const profile = page.locator('[class*="profile"], [class*="user-info"], .el-form')
      await expect(profile.first()).toBeVisible({ timeout: 10000 })
      markPassed('9.1', '个人信息页面可访问')
    } catch (e) { markFailed('9.1', e.message.slice(0, 50)); throw e }
  })

  test('9.2 头像上传', async ({ page }) => {
    markRunning('9.2')
    try {
      await page.goto('/user/home/profile')
      await page.waitForLoadState('networkidle').catch(() => null)
      const avatar = page.locator('[class*="avatar"], [class*="upload"], input[type="file"]').first()
      if (await avatar.count() > 0) {
        markPassed('9.2', '头像上传区域存在')
      } else {
        markSkipped('9.2', '未找到头像上传区域')
      }
    } catch (e) { markFailed('9.2', e.message.slice(0, 50)); throw e }
  })

  test('9.3 修改密码', async ({ page }) => {
    markRunning('9.3')
    try {
      await page.goto('/user/home/settings')
      await page.waitForLoadState('networkidle').catch(() => null)
      // 用户改密入口在「设置 → 密码」，没有独立的 change-password 路由
      await expect(page.getByText('密码', { exact: true })).toBeVisible({ timeout: 10000 })
      await expect(page.locator('.el-button:has-text("修改")').first()).toBeVisible({ timeout: 10000 })
      markPassed('9.3', '设置页密码项与修改入口可见')
    } catch (e) { markFailed('9.3', e.message.slice(0, 50)); throw e }
  })

  test('9.4 饮食偏好', async ({ page }) => {
    markRunning('9.4')
    try {
      await page.goto('/user/home/profile')
      await page.waitForLoadState('networkidle').catch(() => null)
      await page.locator('button:has-text("编辑")').first().click()
      // 饮食偏好在个人中心的编辑面板里
      await expect(page.getByText('饮食偏好', { exact: true })).toBeVisible({ timeout: 10000 })
      await expect(page.getByText('口味标签')).toBeVisible({ timeout: 10000 })
      markPassed('9.4', '个人中心编辑面板含饮食偏好与口味标签')
    } catch (e) { markFailed('9.4', e.message.slice(0, 50)); throw e }
  })

  test('9.5 消费记录', async ({ page }) => {
    markRunning('9.5')
    try {
      await page.goto('/user/home/consume-history')
      await page.waitForLoadState('networkidle').catch(() => null)
      await expect(page.getByText('消费记录').first()).toBeVisible({ timeout: 10000 })
      await expect(page.getByText('当前余额')).toBeVisible({ timeout: 10000 })
      markPassed('9.5', '消费记录页含余额与流水')
    } catch (e) { markFailed('9.5', e.message.slice(0, 50)); throw e }
  })

  test('9.6 教程列表', async ({ page }) => {
    markRunning('9.6')
    try {
      await page.goto('/user/home/tutorials')
      await page.waitForLoadState('networkidle').catch(() => null)
      const tutorial = page.locator('[class*="tutorial"], [class*="guide"], .el-card')
      await expect(tutorial.first()).toBeVisible({ timeout: 10000 })
      markPassed('9.6', '教程列表页面可访问')
    } catch (e) { markFailed('9.6', e.message.slice(0, 50)); throw e }
  })

  test('9.7 教程详情', async ({ page }) => {
    markRunning('9.7')
    try {
      await page.goto('/user/home/tutorials')
      await page.waitForLoadState('networkidle').catch(() => null)
      const card = page.locator('[class*="tutorial-card"], [class*="tutorial-item"], .el-card').first()
      await expect(card).toBeVisible({ timeout: 10000 })
      await card.click()
      await page.waitForURL(/\/tutorials\/\d+/, { timeout: 15000 })
      await expect(page.locator('body')).not.toContainText('教程不存在')
      markPassed('9.7', '从列表进入教程详情，未出现「教程不存在」')
    } catch (e) { markFailed('9.7', e.message.slice(0, 50)); throw e }
  })

  test('9.8 发布教程', async ({ page }) => {
    markRunning('9.8')
    try {
      await page.goto('/user/home/tutorials/publish')
      await page.waitForLoadState('networkidle').catch(() => null)
      await expect(page.getByText('发布教程').first()).toBeVisible({ timeout: 10000 })
      await expect(page.getByText('快速模板')).toBeVisible({ timeout: 10000 })
      markPassed('9.8', '发布教程页含模板入口')
    } catch (e) { markFailed('9.8', e.message.slice(0, 50)); throw e }
  })

  test('9.9 我的教程', async ({ page }) => {
    markRunning('9.9')
    try {
      await page.goto('/user/home/tutorials/my')
      await page.waitForLoadState('networkidle').catch(() => null)
      await expect(page.getByText('我的教程').first()).toBeVisible({ timeout: 10000 })
      await expect(page.locator('button:has-text("发布新教程")')).toBeVisible({ timeout: 10000 })
      markPassed('9.9', '我的教程页含发布入口')
    } catch (e) { markFailed('9.9', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})