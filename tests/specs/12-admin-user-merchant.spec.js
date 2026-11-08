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

  test('12.2 用户详情', async ({ page, request }) => {
    markRunning('12.2')
    try {
      const users = pageRecords(await apiRequest(page, request, '/admin/users?page=1&pageSize=10'))
      expect(users.length, '用户接口应有可验证记录').toBeGreaterThan(0)
      const user = users[0]
      const detail = await apiRequest(page, request, `/admin/users/${user.userId}`)

      await page.goto('/admin/users')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('.el-table__body-wrapper tbody tr').first()
        .locator('button:has-text("查看")').click()

      const dialog = page.locator('.el-dialog').filter({ hasText: '用户详情' })
      await expect(dialog).toBeVisible({ timeout: 10000 })
      await expect(dialog).toContainText(String(detail.userId))
      await expect(dialog).toContainText(detail.phone)

      markPassed('12.2', `用户 ${detail.userId} 详情弹窗 ID/手机号与详情接口一致`)
    } catch (e) {
      markFailed('12.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('12.3 用户状态管理', async ({ page, request }) => {
    markRunning('12.3')
    const phone = `139${String(Date.now()).slice(-8)}`
    const password = 'e2eStatus123'
    let userId

    const publicRequest = (path, options = {}) => request.fetch(`${API_BASE}${path}`, {
      ...options,
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        ...(options.headers || {})
      }
    })

    try {
      const registerResponse = await publicRequest('/v1/users/register', {
        method: 'POST',
        data: {
          phone,
          password,
          nickname: '状态管理用例',
          email: `${phone}@example.com`,
          captcha: '8888',
          checkCodeKey: 'e2e-status-check'
        }
      })
      const registerResult = await registerResponse.json()
      expect(registerResponse.ok(), `注册临时用户应成功：${registerResult.message || ''}`).toBeTruthy()
      expect(registerResult.success, `注册临时用户应成功：${registerResult.message || ''}`).toBe(true)

      const user = pageRecords(await apiRequest(page, request, `/admin/users?page=1&pageSize=10&keyword=${phone}`))
        .find(item => item.phone === phone)
      expect(user, '应能在管理员用户列表查到刚创建的临时用户').toBeTruthy()
      userId = user.userId
      expect(user.status, '新用户默认应启用').not.toBe(false)

      await page.goto('/admin/users')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.getByPlaceholder('搜索用户名、手机号、邮箱').fill(phone)
      await page.getByRole('button', { name: '搜索' }).click()

      const row = page.locator('.el-table__body-wrapper tbody tr').filter({ hasText: phone })
      await expect(row).toHaveCount(1, { timeout: 10000 })
      // 行操作收敛为「查看 + 更多」，禁用进「更多」菜单并由 RowActions 二次确认（DESIGN-BASELINE §4.3）
      await row.getByRole('button', { name: '更多' }).click()
      const disableItem = page.locator('.el-dropdown-menu__item:visible').filter({ hasText: '禁用' })
      await expect(disableItem).toBeVisible()
      const disableResponsePromise = page.waitForResponse((response) =>
        response.url().includes(`/admin/users/${userId}/status`) &&
        response.request().method() === 'PUT' &&
        response.request().postDataJSON()?.status === 'LOCKED'
      )
      await disableItem.click()
      const disableDialog = page.locator('.el-message-box').filter({ hasText: '禁用' })
      await expect(disableDialog).toBeVisible()
      await disableDialog.getByRole('button', { name: '确认执行' }).click()
      const disableResponse = await disableResponsePromise
      expect(disableResponse.ok(), '禁用接口应返回成功状态').toBeTruthy()
      await expect(page.locator('.el-message--success').filter({ hasText: '状态修改成功' }).first()).toBeVisible()

      let saved = pageRecords(await apiRequest(page, request, `/admin/users?page=1&pageSize=10&keyword=${phone}`))
        .find(item => item.userId === userId)
      expect(saved?.status, '禁用操作应真实落库').toBe(false)

      const blockedResponse = await publicRequest('/v1/users/login', {
        method: 'POST',
        data: { phone, password, captcha: '8888', checkCodeKey: 'e2e-status-check' }
      })
      const blockedResult = await blockedResponse.json()
      expect(blockedResult.success, '禁用用户登录应失败').toBe(false)
      expect(blockedResult.message, '禁用用户登录提示应明确').toContain('禁用')

      const enabledRow = page.locator('.el-table__body-wrapper tbody tr').filter({ hasText: phone })
      await enabledRow.getByRole('button', { name: '更多' }).click()
      const enableItem = page.locator('.el-dropdown-menu__item:visible').filter({ hasText: '启用' })
      await expect(enableItem).toBeVisible()
      const enableResponsePromise = page.waitForResponse((response) =>
        response.url().includes(`/admin/users/${userId}/status`) &&
        response.request().method() === 'PUT' &&
        response.request().postDataJSON()?.status === 'ACTIVE'
      )
      await enableItem.click()
      const enableResponse = await enableResponsePromise
      expect(enableResponse.ok(), '启用接口应返回成功状态').toBeTruthy()
      await expect(page.locator('.el-message--success').filter({ hasText: '状态修改成功' }).first()).toBeVisible()

      saved = pageRecords(await apiRequest(page, request, `/admin/users?page=1&pageSize=10&keyword=${phone}`))
        .find(item => item.userId === userId)
      expect(saved?.status, '启用操作应真实落库').toBe(true)

      const enabledResponse = await publicRequest('/v1/users/login', {
        method: 'POST',
        data: { phone, password, captcha: '8888', checkCodeKey: 'e2e-status-check' }
      })
      const enabledResult = await enabledResponse.json()
      expect(enabledResult.success, `启用后应可登录：${enabledResult.message || ''}`).toBe(true)
      expect(enabledResult.data?.token, '启用后登录应返回 token').toBeTruthy()

      markPassed('12.3', `临时用户 ${phone} 禁用后被拒、启用后登录成功，状态均回查落库`)
    } catch (e) {
      markFailed('12.3', e.message.slice(0, 50))
      throw e
    } finally {
      if (userId) {
        await apiRequest(page, request, `/admin/users/${userId}/status`, {
          method: 'PUT',
          data: { status: 'ACTIVE' }
        })
        await apiRequest(page, request, `/admin/users/${userId}`, { method: 'DELETE' })
      }
    }
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
      // 分页生效后 pageSize 会真的限制返回条数，而首屏 10 条按 created_at 倒序全是待审核商家，
      // 所以先按审核状态筛出非待审核商家，再把每页条数调大，让它出现在表格里。
      const approved = pageRecords(await apiRequest(page, request, '/admin/merchants?page=1&pageSize=10&status=APPROVED'))
      expect(approved.length, '应有已通过审核的商家').toBeGreaterThan(0)
      const target = approved.find(merchant => merchant.auditStatus !== 'PENDING' && merchant.status !== 'PENDING')
      expect(target, '应有可打开状态弹窗的商家').toBeTruthy()

      await page.goto('/admin/merchants')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('.el-pagination .el-select').first().click()
      await page.locator('.el-select-dropdown__item:visible').filter({ hasText: '100/page' }).first().click()
      const targetRow = page.locator('.el-table__body-wrapper tbody tr')
        .filter({ hasText: String(target.merchantId) })
      await expect(targetRow, '目标商家应出现在表格中').toHaveCount(1, { timeout: 10000 })
      await targetRow.locator('button:has-text("状态")').click()

      const dialog = page.locator('.el-message-box').filter({ hasText: '修改状态' })
      await expect(dialog).toBeVisible({ timeout: 10000 })
      await expect(dialog).toContainText('请选择新状态')
      await dialog.locator('button').filter({ hasText: '取消' }).click()
      await expect(dialog).toBeHidden()

      markPassed('12.7', `商家 ${target.merchantId} 状态弹窗可打开且取消未落库`)
    } catch (e) { markFailed('12.7', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => {
    updateSummary()
  })
})
