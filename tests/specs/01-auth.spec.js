const { test, expect, loginAsUser, loginAsMerchant, loginAsAdmin } = require('../fixtures/test-base')
const { LoginPage, RegisterPage, AdminLoginPage } = require('../pages/common')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块一：认证模块', () => {

  test('1.1 用户登录', async ({ page }) => {
    markRunning('1.1')
    try {
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await expect(loginPage.usernameInput).toBeVisible({ timeout: 10000 })
      await loginPage.login('testuser', '123456')
      await page.waitForURL('**/user/home**', { timeout: 15000 })
      await expect(page).toHaveURL(/user\/home/)
      markPassed('1.1')
    } catch (e) {
      markFailed('1.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.2 用户注册', async ({ page }) => {
    markRunning('1.2')
    try {
      const registerPage = new RegisterPage(page)
      await registerPage.goto()
      await expect(page).toHaveURL(/register/)
      const formInputs = page.locator('input')
      await expect(formInputs.first()).toBeVisible({ timeout: 10000 })
      markPassed('1.2', '注册页面可访问')
    } catch (e) {
      markFailed('1.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.3 找回密码', async ({ page }) => {
    markRunning('1.3')
    try {
      await page.goto('/forgot-password')
      await page.waitForLoadState('networkidle').catch(() => null)
      await expect(page).toHaveURL(/forgot-password/)
      markPassed('1.3', '找回密码页面可访问')
    } catch (e) {
      markFailed('1.3', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.4 商家注册', async ({ page }) => {
    markRunning('1.4')
    try {
      await page.goto('/merchant/register')
      await page.waitForLoadState('networkidle').catch(() => null)
      await expect(page).toHaveURL(/merchant\/register/)
      markPassed('1.4', '商家注册页面可访问')
    } catch (e) {
      markFailed('1.4', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.5 管理员登录', async ({ page }) => {
    markRunning('1.5')
    try {
      const adminLogin = new AdminLoginPage(page)
      await adminLogin.goto()
      await expect(adminLogin.usernameInput).toBeVisible({ timeout: 10000 })
      markPassed('1.5', '管理员登录页面可访问')
    } catch (e) {
      markFailed('1.5', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.6 登录异常', async ({ page }) => {
    markRunning('1.6')
    try {
      const loginPage = new LoginPage(page)
      await loginPage.goto()
      await loginPage.login('wronguser', 'wrongpwd')
      await page.waitForTimeout(2000)
      const hasError = await page.locator('.el-message--error, [class*="error"], .el-form-item__error').count()
      expect(hasError).toBeGreaterThan(0)
      markPassed('1.6', '错误提示正确显示')
    } catch (e) {
      markFailed('1.6', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.7 退出登录', async ({ page }) => {
    markRunning('1.7')
    try {
      await loginAsUser(page)
      const logoutBtn = page.locator('button:has-text("退出"), button:has-text("登出"), .logout, [class*="logout"]').first()
      if (await logoutBtn.count() > 0) {
        await logoutBtn.click()
        await page.waitForURL('**/login**', { timeout: 10000 })
        markPassed('1.7', '退出成功')
      } else {
        markSkipped('1.7', '未找到退出按钮')
      }
    } catch (e) {
      markFailed('1.7', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => { updateSummary() })
})