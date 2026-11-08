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

test.describe('模块十四：管理员端-系统管理', () => {

  test.beforeEach(async ({ page }) => { await bootstrapAdminSession(page) })

  test('14.1 数据统计', async ({ page, request }) => {
    markRunning('14.1')
    try {
      const dashboard = await apiRequest(page, request, '/admin/statistics/dashboard?days=7')
      await page.goto('/admin/dashboard')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const cards = page.locator('.stats-grid .stat-card')
      await expect(cards).toHaveCount(5)
      await expect(cards.filter({ hasText: '用户总数' })).toContainText(String(dashboard.summary.totalUsers))
      await expect(cards.filter({ hasText: '商家总数' })).toContainText(String(dashboard.summary.totalMerchants))
      await expect(cards.filter({ hasText: '今日订单' })).toContainText(String(dashboard.summary.newOrders))
      await expect(cards.filter({ hasText: '今日收入' })).toContainText(Number(dashboard.summary.newRevenue).toFixed(2))
      await expect(cards.filter({ hasText: '今日新增用户' })).toContainText(String(dashboard.summary.newUsers))

      markPassed('14.1', '五张统计卡与接口 summary 值一致')
    } catch (e) { markFailed('14.1', e.message.slice(0, 50)); throw e }
  })

  test('14.2 角色管理', async ({ page, request }) => {
    markRunning('14.2')
    try {
      const roles = recordsOf(await apiRequest(page, request, '/admin/roles?page=1&pageSize=100'))
      await page.goto('/admin/settings/roles')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(roles.length, '角色接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(roles.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(String(roles[0].roleId))
      await expect(rows.first()).toContainText(roles[0].roleName)
      await expect(rows.first()).toContainText(roles[0].roleCode)

      markPassed('14.2', `角色表 ${roles.length} 行，首行 ID/名称/编码与接口一致`)
    } catch (e) { markFailed('14.2', e.message.slice(0, 50)); throw e }
  })

  test('14.3 权限管理', async ({ page, request }) => {
    markRunning('14.3')
    try {
      const permissions = await apiRequest(page, request, '/admin/permissions/tree')
      await page.goto('/admin/settings/permissions')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(permissions.length, '权限树接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      const flatten = items => items.flatMap(item => [item, ...flatten(item.children || [])])
      const flatPermissions = flatten(permissions)
      await expect(rows).toHaveCount(flatPermissions.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(permissions[0].permissionName)
      await expect(rows.first()).toContainText(permissions[0].permissionCode)

      markPassed('14.3', `权限树展开 ${flatPermissions.length} 行，首行名称/编码与接口一致`)
    } catch (e) { markFailed('14.3', e.message.slice(0, 50)); throw e }
  })

  test('14.4 公告管理', async ({ page, request }) => {
    markRunning('14.4')
    try {
      const announcementPage = await apiRequest(page, request, '/admin/announcements?page=1&pageSize=10')
      const announcements = recordsOf(announcementPage)
      await page.goto('/admin/announcements')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      expect(announcements.length, '公告接口应有可验证记录').toBeGreaterThan(0)
      // 分页契约：有记录时接口必须给出真实 total（#433 回归防线，前端不再用当前页记录数兜底）
      expect(announcementPage.total, '公告接口应有真实 total').toBeGreaterThan(0)
      expect(announcementPage.total, 'total 不应小于当前页记录数').toBeGreaterThanOrEqual(announcements.length)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(announcements.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(announcements[0].title)
      await expect(rows.first()).toContainText(announcements[0].content)

      markPassed('14.4', `公告表 ${announcements.length} 行，首行标题/内容与接口一致；total=${announcementPage.total}`)
    } catch (e) { markFailed('14.4', e.message.slice(0, 50)); throw e }
  })

  test('14.5 热点话题管理', async ({ page, request }) => {
    markRunning('14.5')
    try {
      const topics = await apiRequest(page, request, '/v1/admin/hot-topic?page=1&size=10')
      await page.goto('/admin/topics')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const topicRecords = topics.records || topics || []
      expect(topicRecords.length, '热点话题接口应有可验证记录').toBeGreaterThan(0)
      const rows = page.locator('.el-table__body-wrapper tbody tr')
      await expect(rows).toHaveCount(topicRecords.length, { timeout: 10000 })
      await expect(rows.first()).toContainText(topicRecords[0].content)
      await expect(rows.first()).toContainText(String(topicRecords[0].priority))

      markPassed('14.5', `热点话题表 ${topicRecords.length} 行，首行内容/优先级与接口一致`)
    } catch (e) { markFailed('14.5', e.message.slice(0, 50)); throw e }
  })

  test('14.6 系统设置', async ({ page, request }) => {
    markRunning('14.6')
    try {
      const [systemConfigs, notificationConfigs, securityConfigs] = await Promise.all([
        apiRequest(page, request, '/admin/settings/config/group/system'),
        apiRequest(page, request, '/admin/settings/config/group/notification'),
        apiRequest(page, request, '/admin/settings/config/group/security')
      ])
      await page.goto('/admin/settings')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const formItem = label => page.locator('.el-form-item').filter({ hasText: label })
      await expect(formItem('网站名称').locator('input')).toHaveValue(systemConfigs['system.site.name'])
      await expect(formItem('AI教程生成').locator('.el-switch'))
        .toHaveClass(new RegExp(systemConfigs['system.ai.tutorial.enabled'] === 'true' ? 'is-checked' : '^(?!.*is-checked)'))

      await page.getByRole('tab', { name: '通知设置' }).click()
      await expect(formItem('邮件通知').locator('.el-switch'))
        .toHaveClass(new RegExp(notificationConfigs['notification.email.enabled'] === 'true' ? 'is-checked' : '^(?!.*is-checked)'))
      await expect(formItem('短信通知').locator('.el-switch'))
        .toHaveClass(new RegExp(notificationConfigs['notification.sms.enabled'] === 'true' ? 'is-checked' : '^(?!.*is-checked)'))

      await page.getByRole('tab', { name: '安全设置' }).click()
      await expect(formItem('会话超时时间').locator('input'))
        .toHaveValue(securityConfigs['security.session.timeout'])
      await expect(formItem('最大登录尝试次数').locator('input'))
        .toHaveValue(securityConfigs['security.login.maxAttempts'])
      await expect(formItem('密码最小长度').locator('input'))
        .toHaveValue(securityConfigs['security.password.minLength'])

      markPassed('14.6', '系统、通知、安全设置控件值与真实配置一致')
    } catch (e) { markFailed('14.6', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
