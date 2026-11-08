const { test, expect, bootstrapAdminSession } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')
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

const recordsOf = data => data?.records || []

test.describe('模块十三：管理员端-订单菜品与财务', () => {

  test.beforeEach(async ({ page }) => { await bootstrapAdminSession(page) })

  test('13.1 管理员订单管理', async ({ page, request }) => {
    markRunning('13.1')
    try {
      const orders = recordsOf(await apiRequest(page, request, '/admin/orders?page=1&pageSize=10'))
      await page.goto('/admin/orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(orders.length, '订单接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(orders.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(String(orders[0].orderId || orders[0].id))
      await expect(rows.first()).toContainText(String(orders[0].totalAmount))

      markPassed('13.1', `订单表 ${orders.length} 行，首行 ID/金额与接口一致`)
    } catch (e) { markFailed('13.1', e.message.slice(0, 50)); throw e }
  })

  test('13.2 管理员订单状态更新', async ({ page, request }) => {
    markRunning('13.2')
    try {
      const orders = recordsOf(await apiRequest(page, request, '/admin/orders?page=1&pageSize=10'))
      expect(orders.length, '订单接口应有可验证记录').toBeGreaterThan(0)
      const order = orders[0]
      const orderId = String(order.orderId || order.id)

      await page.goto('/admin/orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const row = page.locator('.el-table__body-wrapper tbody tr').filter({ hasText: orderId })
      await expect(row).toBeVisible()
      await row.locator('button:has-text("状态")').click()

      const dialog = page.locator('.el-dialog').filter({ hasText: '修改订单状态' })
      await expect(dialog).toBeVisible({ timeout: 10000 })
      await expect(dialog).toContainText('当前状态')
      await expect(dialog.locator('.el-select')).toBeVisible()
      await dialog.locator('button').filter({ hasText: '取消' }).click()
      await expect(dialog).toBeHidden()

      markPassed('13.2', `订单 ${orderId} 状态弹窗可打开且取消未落库`)
    } catch (e) { markFailed('13.2', e.message.slice(0, 50)); throw e }
  })

  test('13.3 管理员菜品管理', async ({ page, request }) => {
    markRunning('13.3')
    try {
      const dishes = recordsOf(await apiRequest(page, request, '/admin/dishes?page=1&pageSize=10'))
      await page.goto('/admin/dishes')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(dishes.length, '菜品接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(dishes.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(String(dishes[0].dishId))
      await expect(rows.first()).toContainText(dishes[0].dishName)

      markPassed('13.3', `菜品表 ${dishes.length} 行，首行 ID/名称与接口一致`)
    } catch (e) { markFailed('13.3', e.message.slice(0, 50)); throw e }
  })

  test('13.4 菜品审核', async ({ page, request }) => {
    markRunning('13.4')
    try {
      const dishes = recordsOf(await apiRequest(page, request, '/admin/dishes/audit?page=1&pageSize=10&auditStatus=PENDING'))
      await page.goto('/admin/dishes/audit')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.waitForTimeout(500)

      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(dishes.length, { timeout: 10000 })
      if (dishes.length > 0) {
        await expect(rows.first()).toContainText(String(dishes[0].dishId))
        await expect(rows.first()).toContainText(dishes[0].dishName)
        await rows.first().locator('button:has-text("详情")').click()
        const dialog = page.locator('.el-dialog').filter({ hasText: '菜品详情' })
        await expect(dialog).toBeVisible({ timeout: 10000 })
        await expect(dialog).toContainText(String(dishes[0].dishId))
      } else {
        // 空数据用全站统一的中文空状态，且隐藏无意义的分页条（DESIGN-BASELINE §4.3 §4.5 §5.5）
        const empty = page.locator('.app-empty-state')
        await expect(empty).toBeVisible({ timeout: 10000 })
        await expect(empty).toContainText('当前没有待审核的菜品')
        await expect(page.locator('.pagination-container')).toHaveCount(0)
      }

      markPassed('13.4', `菜品审核接口 ${dishes.length} 条，列表与真实响应一致`)
    } catch (e) { markFailed('13.4', e.message.slice(0, 50)); throw e }
  })

  test('13.5 提现审核', async ({ page, request }) => {
    markRunning('13.5')
    try {
      const withdrawals = recordsOf(await apiRequest(page, request, '/admin/finance/withdrawals?page=1&pageSize=10'))
      await page.goto('/admin/finance/withdrawals')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(withdrawals.length, '提现接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(withdrawals.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(String(withdrawals[0].withdrawNo))
      await expect(rows.first()).toContainText(String(withdrawals[0].userId))

      markPassed('13.5', `提现表 ${withdrawals.length} 行，首行流水号/用户 ID 与接口一致`)
    } catch (e) { markFailed('13.5', e.message.slice(0, 50)); throw e }
  })

  test('13.6 充值记录', async ({ page, request }) => {
    markRunning('13.6')
    try {
      const recharges = recordsOf(await apiRequest(page, request, '/admin/finance/recharges?page=1&pageSize=10'))
      await page.goto('/admin/finance/recharges')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(recharges.length, '充值接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(recharges.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(String(recharges[0].rechargeId || recharges[0].id || recharges[0].rechargeNo))
      await expect(rows.first()).toContainText(String(recharges[0].amount))

      markPassed('13.6', `充值表 ${recharges.length} 行，首行记录 ID/金额与接口一致`)
    } catch (e) { markFailed('13.6', e.message.slice(0, 50)); throw e }
  })

  test('13.7 退款管理', async ({ page, request }) => {
    markRunning('13.7')
    try {
      const refunds = recordsOf(await apiRequest(page, request, '/admin/finance/refunds?page=1&pageSize=10'))
      await page.goto('/admin/finance/refunds')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(refunds.length, '退款接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(refunds.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(String(refunds[0].refundId))
      await expect(rows.first()).toContainText(String(refunds[0].refundAmount))

      markPassed('13.7', `退款表 ${refunds.length} 行，首行退款 ID/金额与接口一致`)
    } catch (e) { markFailed('13.7', e.message.slice(0, 50)); throw e }
  })

  test('13.8 财务统计', async ({ page, request }) => {
    markRunning('13.8')
    try {
      const dashboard = await apiRequest(page, request, '/admin/statistics/dashboard?days=30')
      await page.goto('/admin/statistics')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('.el-radio-button').filter({ hasText: '最近30天' }).click()
      await page.waitForTimeout(500)

      const summary = dashboard.summary
      const statCards = page.locator('.stats-cards .stat-card')
      await expect(statCards.filter({ hasText: '总用户数' })).toContainText(String(summary.totalUsers))
      await expect(statCards.filter({ hasText: '总商家数' })).toContainText(String(summary.totalMerchants))
      await expect(statCards.filter({ hasText: '总订单数' })).toContainText(String(summary.totalOrders))
      await expect(statCards.filter({ hasText: '总收入' })).toContainText(Number(summary.totalRevenue).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }))

      const rows = page.locator('.table-card .el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(dashboard.daily.length, { timeout: 10000 })
      if (dashboard.daily.length > 0) {
        await expect(rows.first()).toContainText(dashboard.daily[0].date)
      }

      markPassed('13.8', `统计汇总与每日 ${dashboard.daily.length} 行均来自真实接口`)
    } catch (e) { markFailed('13.8', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
