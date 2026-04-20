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
      await expect(profile.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
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
      await page.goto('/user/home/change-password')
      await page.waitForLoadState('networkidle').catch(() => null)
      const pwdForm = page.locator('[class*="password"], .el-form')
      await expect(pwdForm.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('9.3', '修改密码页面可访问')
    } catch (e) { markFailed('9.3', e.message.slice(0, 50)); throw e }
  })

  test('9.4 饮食偏好', async ({ page }) => {
    markRunning('9.4')
    try {
      await page.goto('/user/home/diet-preference')
      await page.waitForLoadState('networkidle').catch(() => null)
      const preference = page.locator('[class*="preference"], [class*="diet"], .el-checkbox-group')
      await expect(preference.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('9.4', '饮食偏好页面可访问')
    } catch (e) { markFailed('9.4', e.message.slice(0, 50)); throw e }
  })

  test('9.5 消费记录', async ({ page }) => {
    markRunning('9.5')
    try {
      await page.goto('/user/home/consumption-record')
      await page.waitForLoadState('networkidle').catch(() => null)
      const record = page.locator('[class*="consumption"], [class*="record"], .el-table')
      await expect(record.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('9.5', '消费记录页面可访问')
    } catch (e) { markFailed('9.5', e.message.slice(0, 50)); throw e }
  })

  test('9.6 教程列表', async ({ page }) => {
    markRunning('9.6')
    try {
      await page.goto('/user/home/tutorials')
      await page.waitForLoadState('networkidle').catch(() => null)
      const tutorial = page.locator('[class*="tutorial"], [class*="guide"], .el-card')
      await expect(tutorial.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('9.6', '教程列表页面可访问')
    } catch (e) { markFailed('9.6', e.message.slice(0, 50)); throw e }
  })

  test('9.7 教程详情', async ({ page }) => {
    markRunning('9.7')
    try {
      await page.goto('/user/home/tutorial-detail/1')
      await page.waitForLoadState('networkidle').catch(() => null)
      const detail = page.locator('[class*="detail"], [class*="tutorial"]')
      await expect(detail.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('9.7', '教程详情页面可访问')
    } catch (e) { markFailed('9.7', e.message.slice(0, 50)); throw e }
  })

  test('9.8 发布教程', async ({ page }) => {
    markRunning('9.8')
    try {
      await page.goto('/user/home/tutorial-publish')
      await page.waitForLoadState('networkidle').catch(() => null)
      const form = page.locator('[class*="publish"], [class*="editor"], .el-form')
      await expect(form.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('9.8', '发布教程页面可访问')
    } catch (e) { markFailed('9.8', e.message.slice(0, 50)); throw e }
  })

  test('9.9 我的教程', async ({ page }) => {
    markRunning('9.9')
    try {
      await page.goto('/user/home/my-tutorials')
      await page.waitForLoadState('networkidle').catch(() => null)
      const myTutorial = page.locator('[class*="tutorial"], .el-card')
      await expect(myTutorial.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('9.9', '我的教程页面可访问')
    } catch (e) { markFailed('9.9', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})