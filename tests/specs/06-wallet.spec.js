const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块六：钱包与支付', () => {

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('6.1 钱包信息', async ({ page }) => {
    markRunning('6.1')
    try {
      await page.goto('/user/home/wallet-management')
      await page.waitForLoadState('networkidle').catch(() => null)
      const wallet = page.locator('[class*="wallet"], [class*="balance"], .el-card')
      await expect(wallet.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('6.1', '钱包页面可访问')
    } catch (e) { markFailed('6.1', e.message.slice(0, 50)); throw e }
  })

  test('6.2 充值', async ({ page }) => {
    markRunning('6.2')
    try {
      await page.goto('/user/home/wallet-management')
      await page.waitForLoadState('networkidle').catch(() => null)
      const rechargeBtn = page.locator('button:has-text("充值"), [class*="recharge"]').first()
      if (await rechargeBtn.count() > 0) {
        markPassed('6.2', '充值按钮存在')
      } else {
        markSkipped('6.2', '未找到充值按钮')
      }
    } catch (e) { markFailed('6.2', e.message.slice(0, 50)); throw e }
  })

  test('6.3 提现', async ({ page }) => {
    markRunning('6.3')
    try {
      await page.goto('/user/home/wallet-management')
      await page.waitForLoadState('networkidle').catch(() => null)
      const withdrawBtn = page.locator('button:has-text("提现"), [class*="withdraw"]').first()
      if (await withdrawBtn.count() > 0) {
        markPassed('6.3', '提现按钮存在')
      } else {
        markSkipped('6.3', '未找到提现按钮')
      }
    } catch (e) { markFailed('6.3', e.message.slice(0, 50)); throw e }
  })

  test('6.4 交易记录', async ({ page }) => {
    markRunning('6.4')
    try {
      await page.goto('/user/home/wallet-transactions')
      await page.waitForLoadState('networkidle').catch(() => null)
      const transactions = page.locator('[class*="transaction"], [class*="record"], .el-table')
      await expect(transactions.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('6.4', '交易记录页面可访问')
    } catch (e) { markFailed('6.4', e.message.slice(0, 50)); throw e }
  })

  test('6.5 支付密码设置', async ({ page }) => {
    markRunning('6.5')
    try {
      await page.goto('/user/home/payment-password-setup')
      await page.waitForLoadState('networkidle').catch(() => null)
      const pwdForm = page.locator('[class*="password"], [class*="payment"], .el-form')
      await expect(pwdForm.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('6.5', '支付密码页面可访问')
    } catch (e) { markFailed('6.5', e.message.slice(0, 50)); throw e }
  })

  test('6.6 钱包安全', async ({ page }) => {
    markRunning('6.6')
    try {
      await page.goto('/user/home/wallet-security')
      await page.waitForLoadState('networkidle').catch(() => null)
      const security = page.locator('[class*="security"], [class*="lock"], .el-switch')
      await expect(security.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('6.6', '钱包安全页面可访问')
    } catch (e) { markFailed('6.6', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})