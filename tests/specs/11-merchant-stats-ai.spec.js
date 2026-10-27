const { test, expect, loginAsMerchant } = require('../fixtures/test-base')
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

async function getMerchantId(page) {
  const merchantId = await page.evaluate(() => {
    const direct = localStorage.getItem('auth_merchantId')
    if (direct) return String(direct)

    try {
      const auth = JSON.parse(localStorage.getItem('auth') || '{}')
      return auth?.merchantId ? String(auth.merchantId) : ''
    } catch {
      return ''
    }
  })
  expect(merchantId, '商家登录后应写入 merchantId').toBeTruthy()
  return merchantId
}

function sortedCounts(value) {
  return Object.entries(value || {})
    .map(([stars, count]) => ({ stars: Number(stars), count: Number(count) }))
    .sort((a, b) => b.stars - a.stars)
}

test.describe('模块十一：商家端-经营与AI', () => {

  test.beforeEach(async ({ page }) => { await loginAsMerchant(page) })

  test('11.1 经营统计', async ({ page, request }) => {
    markRunning('11.1')
    try {
      await page.goto('/merchant/home/statistics')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const merchantId = await getMerchantId(page)
      const data = await apiRequest(page, request, `/v1/merchant/${merchantId}/statistics?timeRange=all`)
      const cards = page.locator('.stat-card')
      await expect(cards).toHaveCount(4, { timeout: 10000 })

      await expect(cards.filter({ hasText: '总订单数' }).locator('.stat-value'))
        .toHaveText(String(data.basicStats.orders))
      await expect(cards.filter({ hasText: '总销售额' }).locator('.stat-value'))
        .toContainText(Number(data.basicStats.totalAmount).toFixed(2))
      await expect(cards.filter({ hasText: '客单价' }).locator('.stat-value'))
        .toContainText(Number(data.basicStats.avgAmount).toFixed(2))
      await expect(cards.filter({ hasText: '新客户数' }).locator('.stat-value'))
        .toHaveText(String(data.basicStats.newCustomers))

      const rankRows = page.locator('.sales-rank-list .sales-rank-item')
      if (data.dishSalesRank.length > 0) {
        await expect(rankRows).toHaveCount(data.dishSalesRank.length)
        await expect(rankRows.first()).toContainText(data.dishSalesRank[0].name)
        await expect(rankRows.first()).toContainText(`销量: ${data.dishSalesRank[0].sales} 份`)
      } else {
        await expect(rankRows).toHaveCount(0)
        await expect(page.locator('.rank-card .el-empty')).toBeVisible()
      }

      markPassed('11.1', '统计卡与真实接口值一致，菜品排行行数与数据一致')
    } catch (e) { markFailed('11.1', e.message.slice(0, 50)); throw e }
  })

  test('11.2 评价中心', async ({ page, request }) => {
    markRunning('11.2')
    try {
      await page.goto('/merchant/home/comments')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const merchantId = await getMerchantId(page)
      const [reviews, stats] = await Promise.all([
        apiRequest(page, request, `/v1/reviews/merchant/${merchantId}`),
        apiRequest(page, request, `/v1/reviews/merchant/${merchantId}/statistics`)
      ])

      const statCards = page.locator('.stat-card')
      await expect(statCards.filter({ hasText: '总评价数' }).locator('.stat-value'))
        .toHaveText(String(stats.total))
      await expect(statCards.filter({ hasText: '平均评分' }).locator('.stat-value'))
        .toHaveText(Number(stats.avgRating).toFixed(1))

      const commentCards = page.locator('.comment-item')
      expect(reviews.length, '真实评价接口应有可验证数据').toBeGreaterThan(0)
      await expect(commentCards).toHaveCount(reviews.length, { timeout: 10000 })
      await expect(commentCards.first()).toContainText(String(reviews[0].rating))
      await expect(commentCards.first()).toContainText(reviews[0].content)

      markPassed('11.2', `评价统计与 ${reviews.length} 条真实评价一致`)
    } catch (e) { markFailed('11.2', e.message.slice(0, 50)); throw e }
  })

  test('11.3 想吃列表审核', async ({ page, request }) => {
    markRunning('11.3')
    try {
      await page.goto('/merchant/home/wish-list-audit')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const data = await apiRequest(page, request, '/v1/wish-list/merchant/pending')
      const pendingCount = data.filter(item => item.auditStatus === 0).length
      await expect(page.locator('.stat-card').filter({ hasText: '待审核' }).locator('.stat-number'))
        .toHaveText(String(pendingCount))

      const cards = page.locator('.wish-card')
      await expect(cards).toHaveCount(data.length, { timeout: 10000 })
      if (data.length > 0) {
        await expect(cards.first()).toContainText(data[0].dishName)
      } else {
        await expect(page.locator('.el-empty')).toBeVisible()
      }

      markPassed('11.3', `待审核数 ${pendingCount}，列表行数与接口一致`)
    } catch (e) { markFailed('11.3', e.message.slice(0, 50)); throw e }
  })

  test('11.4 经营指标', async ({ page, request }) => {
    markRunning('11.4')
    try {
      await page.goto('/merchant/home/ai')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('#merchant-tab-insight').click()

      const merchantId = await getMerchantId(page)
      const metrics = await apiRequest(page, request, `/v1/merchant/insight/${merchantId}/metrics?timeRange=today`)
      const cards = page.locator('.metric-card')
      await expect(cards).toHaveCount(4, { timeout: 10000 })

      const expected = [
        ['营业额', `¥${Number(metrics.revenue).toLocaleString()}`],
        ['订单数', `${metrics.orders} 单`],
        ['客单价', `¥${metrics.averagePrice}`],
        ['平均评分', `${metrics.rating} 分`]
      ]
      for (const [label, value] of expected) {
        await expect(cards.filter({ hasText: label }).locator('.metric-value')).toHaveText(value)
      }

      markPassed('11.4', '四个经营指标与真实接口一致')
    } catch (e) { markFailed('11.4', e.message.slice(0, 50)); throw e }
  })

  test('11.5 AI建议', async ({ page, request }) => {
    markRunning('11.5')
    try {
      await page.goto('/merchant/home/ai')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('#merchant-tab-insight').click()

      const merchantId = await getMerchantId(page)
      const rating = await apiRequest(page, request, `/v1/merchant/insight/${merchantId}/rating-distribution`)
      await expect(page.locator('.business-insight')).toBeVisible({ timeout: 10000 })

      const ratingRows = page.locator('.rating-distribution .rating-item')
      await expect(ratingRows).toHaveCount(rating.length)
      for (const item of sortedCounts(Object.fromEntries((rating || []).map(row => [row.stars, row.count])))) {
        await expect(ratingRows.filter({ hasText: `${item.stars}星` }))
          .toContainText(`${item.count}条`)
      }

      markPassed('11.5', '经营洞察与真实评分分布一致')
    } catch (e) { markFailed('11.5', e.message.slice(0, 50)); throw e }
  })

  test('11.6 评价分析', async ({ page, request }) => {
    markRunning('11.6')
    try {
      await page.goto('/merchant/home/comments')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const merchantId = await getMerchantId(page)
      const stats = await apiRequest(page, request, `/v1/reviews/merchant/${merchantId}/statistics`)
      const positiveRate = stats.total === 0
        ? '0.0'
        : (((stats.ratingCounts['5'] + stats.ratingCounts['4']) / stats.total) * 100).toFixed(1)
      const replyRate = stats.total === 0 ? '0.0' : ((stats.repliedCount / stats.total) * 100).toFixed(1)

      const overview = page.locator('.overview-section')
      await expect(overview).toContainText('好评率')
      await expect(overview).toContainText(`${positiveRate}%`)
      await expect(overview).toContainText('回复率')
      await expect(overview).toContainText(`${replyRate}%`)
      await expect(overview).toContainText('平均评分')
      await expect(overview).toContainText(Number(stats.avgRating).toFixed(1))
      const fiveStar = page.locator('.rating-bar-item').filter({ hasText: '5星' })
      await expect(fiveStar).toContainText(String(stats.ratingCounts['5']))

      markPassed('11.6', '好评率、回复率与评分分布为真实统计数据')
    } catch (e) { markFailed('11.6', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
