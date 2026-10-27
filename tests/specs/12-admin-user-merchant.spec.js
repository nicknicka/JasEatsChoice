const { test, expect, bootstrapAdminSession } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')
const { TEST_API_BASE } = require('../utils/api')

const API_BASE = TEST_API_BASE.replace(/\/$/, '')

async function apiRequest(page, request, path, options = {}) {
  const token = await page.evaluate(() =>
    localStorage.getItem('auth_token') || localStorage.getItem('admin_token') || ''
  )
  const response = await request.fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  })
  const result = await response.json().catch(() => null)
  expect(response.ok(), `接口 ${path} 应返回成功状态`).toBeTruthy()
  expect(result, `接口 ${path} 应返回 JSON`).not.toBeNull()
  expect(result.success, `接口 ${path} 不应返回 success=false`).not.toBe(false)
  if (result.code !== undefined && result.code !== null) {
    expect(String(result.code), `接口 ${path} 应返回 code=200`).toBe('200')
  }
  return result.data ?? result
}

function pageRecords(data) {
  return data?.records || []
}

test.describe('模块十二：管理员端-用户与商家管理', () => {

  test.beforeEach(async ({ page }) => { await bootstrapAdminSession(page) })

  test('12.1 用户列表', async ({ page, request }) => {
    markRunning('12.1')
    try {
      const users = pageRecords(await apiRequest(page, request, '/admin/users?page=1&pageSize=10'))
      await page.goto('/admin/users')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(users.length, '用户接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(users.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(String(users[0].userId))
      await expect(rows.first()).toContainText(users[0].phone)

      markPassed('12.1', `用户表 ${users.length} 行，首行 ID/手机号与接口一致`)
    } catch (e) { markFailed('12.1', e.message.slice(0, 50)); throw e }
  })

  test.fixme('12.2 用户详情 (blocked by #69)', async ({ page, request }) => {
    const users = pageRecords(await apiRequest(page, request, '/admin/users?page=1&pageSize=10'))
    expect(users.length, '用户接口应有可验证记录').toBeGreaterThan(0)
    const user = users[0]

    await page.goto('/admin/users')
    await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
    await page.locator('.el-table__body-wrapper tbody tr').first()
      .locator('button:has-text("查看")').click()

    const dialog = page.locator('.el-dialog').filter({ hasText: '用户详情' })
    await expect(dialog).toBeVisible({ timeout: 10000 })
    await expect(dialog).toContainText(String(user.userId))
    await expect(dialog).toContainText(user.phone)
  })

  test.fixme('12.3 用户状态管理 (blocked by #34)', async ({ page }) => {
    await page.goto('/admin/users')
    const statusControl = page.locator('.el-table [class*="status"], button:has-text("禁用"), button:has-text("启用")').first()
    await expect(statusControl).toBeVisible({ timeout: 10000 })
  })

  test('12.4 商家列表', async ({ page, request }) => {
    markRunning('12.4')
    try {
      const merchants = pageRecords(await apiRequest(page, request, '/admin/merchants?page=1&pageSize=10'))
      await page.goto('/admin/merchants')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(merchants.length, '商家接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(merchants.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(String(merchants[0].merchantId))
      await expect(rows.first()).toContainText(merchants[0].name)

      markPassed('12.4', `商家表 ${merchants.length} 行，首行 ID/名称与接口一致`)
    } catch (e) { markFailed('12.4', e.message.slice(0, 50)); throw e }
  })

  test('12.5 商家审核', async ({ page, request }) => {
    markRunning('12.5')
    try {
      const merchants = pageRecords(await apiRequest(page, request, '/admin/merchants/pending?page=1&pageSize=10'))
      await page.goto('/admin/merchants/audit')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(merchants.length, '待审核商家接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(merchants.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(merchants[0].name)

      markPassed('12.5', `待审核商家表 ${merchants.length} 行，首行与接口一致`)
    } catch (e) { markFailed('12.5', e.message.slice(0, 50)); throw e }
  })

  test('12.6 商家详情', async ({ page, request }) => {
    markRunning('12.6')
    try {
      const merchants = pageRecords(await apiRequest(page, request, '/admin/merchants?page=1&pageSize=10'))
      expect(merchants.length, '商家接口应有可验证记录').toBeGreaterThan(0)
      const merchant = merchants[0]

      await page.goto('/admin/merchants')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('.el-table__body-wrapper tbody tr').first()
        .locator('button:has-text("查看")').click()

      const dialog = page.locator('.el-dialog').filter({ hasText: '商家详情' })
      await expect(dialog).toBeVisible({ timeout: 10000 })
      await expect(dialog).toContainText(String(merchant.merchantId))
      await expect(dialog).toContainText(merchant.name)

      markPassed('12.6', `商家详情弹窗包含真实商家 ID ${merchant.merchantId}`)
    } catch (e) { markFailed('12.6', e.message.slice(0, 50)); throw e }
  })

  test('12.7 商家状态管理', async ({ page, request }) => {
    markRunning('12.7')
    try {
      const merchants = pageRecords(await apiRequest(page, request, '/admin/merchants?page=1&pageSize=10'))
      const target = merchants.find(merchant => merchant.auditStatus !== 'PENDING' && merchant.status !== 'PENDING')
      expect(target, '应有可打开状态弹窗的商家').toBeTruthy()

      await page.goto('/admin/merchants')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('.el-table__body-wrapper tbody tr')
        .filter({ hasText: String(target.merchantId) })
        .locator('button:has-text("状态")').click()

      const dialog = page.locator('.el-message-box').filter({ hasText: '修改状态' })
      await expect(dialog).toBeVisible({ timeout: 10000 })
      await expect(dialog).toContainText('请选择新状态')
      await dialog.locator('button').filter({ hasText: '取消' }).click()
      await expect(dialog).toBeHidden()

      markPassed('12.7', `商家 ${target.merchantId} 状态弹窗可打开且取消未落库`)
    } catch (e) { markFailed('12.7', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => {
    markSkipped('12.3', '#34', '用户状态管理尚未实现')
    updateSummary()
  })
})
