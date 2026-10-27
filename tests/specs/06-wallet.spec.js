const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')
const { TEST_API_BASE } = require('../utils/api')
const { getUserId } = require('../utils/real-data')

const API_BASE = TEST_API_BASE.replace(/\/$/, '')
let testUserId

async function getJson(path) {
  const response = await fetch(`${API_BASE}${path}`)
  const body = await response.json()
  if (!response.ok || body.success !== true) {
    throw new Error(`GET ${path} 失败：${body.message || response.statusText}`)
  }
  return body.data
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  })
}

test.describe('模块六：钱包与支付', () => {

  test.beforeAll(async () => { testUserId = await getUserId() })

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('6.1 钱包信息', async ({ page }) => {
    markRunning('6.1')
    try {
      const wallet = await getJson(`/v1/wallet/info/${testUserId}`)
      await page.goto('/user/home/wallet-management')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.balance-number')).toHaveText(formatNumber(wallet.balance))
      const stats = page.locator('.wallet-stats .stat-item')
      await expect(stats.filter({ hasText: '累计充值' }).locator('.stat-value')).toHaveText(`${formatNumber(wallet.totalRecharge)}个`)
      await expect(stats.filter({ hasText: '累计消费' }).locator('.stat-value')).toHaveText(`${formatNumber(wallet.totalConsume)}个`)
      await expect(stats.filter({ hasText: '累计提现' }).locator('.stat-value')).toHaveText(`${formatNumber(wallet.totalWithdraw)}个`)
      markPassed('6.1', `余额与三项累计值与接口一致：${wallet.balance}`)
    } catch (e) { markFailed('6.1', e.message.slice(0, 50)); throw e }
  })

  test('6.2 充值', async ({ page }) => {
    markRunning('6.2')
    try {
      const wallet = await getJson(`/v1/wallet/info/${testUserId}`)
      await page.goto('/user/home/wallet-management')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const rechargeButton = page.locator('.wallet-actions button').filter({ hasText: '充值' })
      await rechargeButton.click()
      let dialog = page.locator('.el-dialog').filter({ hasText: '充值说明' })
      await expect(dialog).toBeVisible()
      await expect(dialog.locator('.method-tip')).toHaveText('微信支付、支付宝暂未开放')

      await expect(dialog.locator('.quick-amount-buttons button')).toHaveText(['10币', '50币', '100币', '200币', '500币'])
      await dialog.locator('button').filter({ hasText: '50币' }).click()
      const amountInput = dialog.locator('input[placeholder="请输入充值金额"]')
      await expect(amountInput).toHaveValue('50')
      await expect(dialog.locator('.el-input-group__append')).toHaveText('平台币')
      await dialog.locator('button').filter({ hasText: '取消' }).click()
      await expect(dialog).toBeHidden()

      await rechargeButton.click()
      dialog = page.locator('.el-dialog').filter({ hasText: '充值说明' })
      await expect(dialog).toBeVisible()
      await dialog.locator('button').filter({ hasText: '立即充值' }).click()
      await expect(page.locator('.el-message').filter({ hasText: '请输入有效的充值金额' })).toBeVisible()

      const after = await getJson(`/v1/wallet/info/${testUserId}`)
      expect(Number(after.balance)).toBe(Number(wallet.balance))
      markPassed('6.2', `充值金额与校验交互正确，余额保持 ${wallet.balance}`)
    } catch (e) { markFailed('6.2', e.message.slice(0, 50)); throw e }
  })

  test('6.3 提现', async ({ page }) => {
    markRunning('6.3')
    try {
      const wallet = await getJson(`/v1/wallet/info/${testUserId}`)
      await page.goto('/user/home/wallet-management')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await page.locator('.wallet-actions button').filter({ hasText: '提现' }).click()
      const dialog = page.locator('.el-dialog').filter({ hasText: '提现规则' })
      await expect(dialog).toBeVisible()
      await expect(dialog.locator('.balance-highlight')).toHaveText(`${wallet.balance}平台币`)
      await expect(dialog.locator('.el-alert')).toContainText('单笔提现最低金额：10平台币')
      await expect(dialog.locator('input[placeholder="请输入支付密码"]')).toHaveAttribute('type', 'password')

      await dialog.locator('button').filter({ hasText: '全部提现' }).click()
      await expect(dialog.locator('input[placeholder="请输入提现金额"]')).toHaveValue(String(wallet.balance))
      await expect(dialog.locator('.fee-text')).toHaveText(`${Math.max(Math.floor(wallet.balance * 0.02), 1).toFixed(2)}平台币`)
      await expect(dialog.locator('.actual-amount')).toHaveText(`${(wallet.balance - Math.max(Math.floor(wallet.balance * 0.02), 1)).toFixed(2)}平台币`)
      markPassed('6.3', `提现对话框读取余额与手续费计算正确：${wallet.balance}`)
    } catch (e) { markFailed('6.3', e.message.slice(0, 50)); throw e }
  })

  test('6.4 交易记录', async ({ page }) => {
    markRunning('6.4')
    try {
      const wallet = await getJson(`/v1/wallet/info/${testUserId}`)
      const history = await getJson(`/v1/consume-history?userId=${testUserId}&type=all&page=1&pageSize=20`)
      await page.goto('/user/home/wallet-transactions')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.balance-amount')).toHaveText(`${formatNumber(wallet.balance)} 平台币`)
      await expect(page.locator('.transaction-item')).toHaveCount(history.records.length)
      const first = history.records[0]
      const firstRow = page.locator('.transaction-item').first()
      await expect(firstRow).toContainText(first.description)
      await expect(firstRow.locator('.transaction-amount')).toHaveText(`${first.type === 'recharge' ? '+' : '-'}${formatNumber(first.amount)}`)
      await expect(firstRow.locator('.el-tag')).toHaveText(first.status === 'success' ? '成功' : '失败')

      const income = history.records.filter((item) => item.type === 'recharge' && item.status === 'success').reduce((sum, item) => sum + Number(item.amount), 0)
      const expense = history.records.filter((item) => (item.type === 'consume' || item.type === 'withdraw') && item.status === 'success').reduce((sum, item) => sum + Number(item.amount), 0)
      const stats = page.locator('.stats-row .stat-card')
      await expect(stats.filter({ hasText: '总收入' }).locator('.stat-value')).toHaveText(`+${formatNumber(income)}`)
      await expect(stats.filter({ hasText: '总支出' }).locator('.stat-value')).toHaveText(`-${formatNumber(expense)}`)
      await expect(stats.filter({ hasText: '交易笔数' }).locator('.stat-value')).toHaveText(String(history.records.length))
      markPassed('6.4', `余额、首条记录和统计与接口一致：${history.records.length} 条`)
    } catch (e) { markFailed('6.4', e.message.slice(0, 50)); throw e }
  })

  test('6.5 支付密码设置', async ({ page }) => {
    markRunning('6.5')
    try {
      const passwordStatus = await getJson(`/v1/payment-password/check/${testUserId}`)
      await page.goto('/user/home/payment-password-setup')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.page-title')).toHaveText(passwordStatus.hasPaymentPassword ? '修改支付密码' : '设置支付密码')
      const passwordInputs = page.locator('.password-form input[type="password"]')
      await expect(passwordInputs).toHaveCount(passwordStatus.hasPaymentPassword ? 3 : 2)
      await expect(page.locator('.password-form button').filter({ hasText: passwordStatus.hasPaymentPassword ? '确认修改' : '确认设置' })).toBeVisible()
      await expect(page.locator('.password-form button').filter({ hasText: '重置' })).toBeVisible()
      await expect(page.locator('.forgot-card')).toHaveCount(passwordStatus.hasPaymentPassword ? 1 : 0)
      markPassed('6.5', `支付密码状态与接口一致：${passwordStatus.hasPaymentPassword ? '已设置' : '未设置'}`)
    } catch (e) { markFailed('6.5', e.message.slice(0, 50)); throw e }
  })

  test('6.6 钱包安全', async ({ page }) => {
    markRunning('6.6')
    try {
      const settings = await getJson(`/v1/wallet/security-settings?userId=${testUserId}`)
      const passwordStatus = await getJson(`/v1/payment-password/check/${testUserId}`)
      await page.goto('/user/home/wallet-security')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.item-status')).toHaveText(passwordStatus.hasPaymentPassword ? '已设置' : '未设置')
      await expect(page.locator('.security-item').filter({ hasText: '钱包锁定' }).locator('.el-switch')).toHaveClass(settings.locked ? /is-checked/ : /^(?!.*is-checked).*$/)
      await expect(page.locator('.security-item').filter({ hasText: '单笔交易限额' })).toContainText(`当前限额：${String(settings.dailyLimit)} 平台币`)
      await expect(page.locator('.security-item').filter({ hasText: '支付验证' }).locator('.el-switch')).toHaveClass(settings.verifyEnabled ? /is-checked/ : /^(?!.*is-checked).*$/)
      markPassed('6.6', '密码状态、锁定、限额、支付验证与接口一致')
    } catch (e) { markFailed('6.6', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
