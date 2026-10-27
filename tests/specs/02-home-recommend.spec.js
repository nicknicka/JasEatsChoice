const { test, expect } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')
const { TEST_API_BASE } = require('../utils/api')

const API_BASE = TEST_API_BASE.replace(/\/$/, '')
const RECOMMEND_USER = {
  phone: process.env.TEST_RECOMMEND_USER_PHONE || '13800138000',
  password: process.env.TEST_RECOMMEND_USER_PWD || 'asdasd'
}
const FEEDBACK_USER = {
  phone: process.env.TEST_FEEDBACK_USER_PHONE || '13800138001',
  password: process.env.TEST_FEEDBACK_USER_PWD || 'asdasd'
}

async function getJson(path) {
  const response = await fetch(`${API_BASE}${path}`)
  const result = await response.json().catch(() => null)
  expect(response.ok, `${path} 应返回 HTTP 成功`).toBeTruthy()
  expect(result?.success, `${path} 应返回 success=true`).toBe(true)
  return result.data
}

async function getRecommendations(user) {
  return getJson(`/v1/recommendations/${user.userId}?scene=home&limit=20`)
}

async function getUserIdByPhone(phone) {
  const users = await getJson(`/v1/users/search?keyword=${encodeURIComponent(phone)}&searchType=phone`)
  const user = users.find((candidate) => candidate.phone === phone)
  expect(user, `未找到测试用户：${phone}`).toBeTruthy()
  return user.userId
}

async function loginUser(page, credentials) {
  await page.goto('/login')
  await page.getByPlaceholder('手机号').fill(credentials.phone)
  await page.getByPlaceholder('密码').fill(credentials.password)

  const captcha = await getJson('/v1/captcha/checkCode')
  await page.getByPlaceholder('验证码').fill(captcha.fixedCaptchaCode || captcha.captchaAnswer)
  await page.locator('.agreement-row .el-checkbox').click()
  await page.locator('button.login-btn').click()
  await page.waitForURL('**/user/home**', { timeout: 20000 })
  return page.evaluate(() => ({
    userId: localStorage.getItem('auth_userId')
  }))
}

async function clearRejectRecord(userId, dishId) {
  const response = await fetch(
    `${API_BASE}/v1/recommendations/rejects?userId=${encodeURIComponent(userId)}&dishId=${encodeURIComponent(dishId)}`,
    { method: 'DELETE' }
  )
  const payload = await response.json().catch(() => null)
  expect(payload?.success, `清理拒绝记录失败：dishId=${dishId}`).toBe(true)
}

test.describe('模块二：用户首页与推荐', () => {

  test.beforeEach(async ({ page }) => {
    page.recommendUserId = await getUserIdByPhone(RECOMMEND_USER.phone)
    await loginUser(page, RECOMMEND_USER)
  })

  test('2.1 首页加载', async ({ page }) => {
    markRunning('2.1')
    try {
      const data = await getRecommendations({ userId: page.recommendUserId })
      const expectedNames = data.recommendations.map((item) => item.dishName)
      expect(expectedNames.length).toBeGreaterThanOrEqual(3)

      const hotTopic = await getJson('/v1/home/hot-topic')
      await expect(page.getByText('今日推荐', { exact: true })).toBeVisible({ timeout: 15000 })
      const visibleNames = await page.locator('.rec-card .rec-dish-name').allTextContents()
      expect(visibleNames).toHaveLength(3)
      expect(visibleNames.every((name) => expectedNames.includes(name))).toBe(true)
      await expect(page.locator('.hot-content')).toHaveText(hotTopic.content)

      markPassed('2.1', '首页展示接口返回的推荐与热点真实数据')
    } catch (e) {
      markFailed('2.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('2.2 个性化推荐', async ({ page }) => {
    markRunning('2.2')
    try {
      const data = await getRecommendations({ userId: page.recommendUserId })
      const expectedNames = data.recommendations.map((item) => item.dishName)
      expect(expectedNames.length).toBeGreaterThan(0)

      await page.goto('/user/home/recommend')
      await expect(page.getByRole('heading', { name: '我的推荐' })).toBeVisible()
      await expect.poll(async () => {
        const visibleNames = await page.locator('.recommend-item .item-name').allTextContents()
        return visibleNames.length >= 3 && visibleNames.every((name) => expectedNames.includes(name))
      }).toBe(true)
      await expect(page.locator('.item-reason').first()).not.toBeEmpty()

      markPassed('2.2', '推荐列表展示接口返回的真实菜品数据')
    } catch (e) {
      markFailed('2.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('2.3 推荐刷新', async ({ page }) => {
    markRunning('2.3')
    try {
      await page.goto('/user/home/recommend')
      const firstItem = page.locator('.recommend-item').first()
      await expect(firstItem).toBeVisible({ timeout: 10000 })

      const responsePromise = page.waitForResponse((response) =>
        response.url().includes(`/v1/recommendations/${page.recommendUserId}`) &&
        response.request().method() === 'GET'
      )
      await page.locator('.refresh-btn').click()
      const response = await responsePromise
      const payload = await response.json()
      expect(payload.success).toBe(true)
      expect(payload.data.recommendations.length).toBeGreaterThan(0)

      const expectedNames = payload.data.recommendations.map((item) => item.dishName)
      await expect.poll(async () => {
        const visibleNames = await page.locator('.recommend-item .item-name').allTextContents()
        return visibleNames.some((name) => expectedNames.includes(name))
      }).toBe(true)
      await expect(page.locator('.refresh-btn')).not.toHaveClass(/spinning/)

      markPassed('2.3', '刷新触发真实推荐查询并保持列表一致')
    } catch (e) {
      markFailed('2.3', e.message.slice(0, 50))
      throw e
    }
  })

  test('2.4 推荐反馈', async ({ page }) => {
    markRunning('2.4')
    try {
      page.feedbackUserId = await getUserIdByPhone(FEEDBACK_USER.phone)
      await loginUser(page, FEEDBACK_USER)

      await page.goto('/user/home/recommend')
      const items = page.locator('.recommend-item')
      await expect(items.first()).toBeVisible({ timeout: 10000 })
      const countBefore = await items.count()
      expect(countBefore).toBeGreaterThan(0)
      const rejectedName = await items.first().locator('.item-name').innerText()
      const recommendations = await getRecommendations({ userId: page.feedbackUserId })
      const rejectedDish = recommendations.recommendations.find((item) => item.dishName === rejectedName)
      expect(rejectedDish, `应在接口结果中找到待拒绝菜品：${rejectedName}`).toBeTruthy()

      const responsePromise = page.waitForResponse((response) =>
        response.url().includes(`/v1/recommendations/${page.feedbackUserId}/reject`) &&
        response.request().method() === 'POST'
      )
      await items.first().getByRole('button', { name: '不感兴趣' }).click()
      const response = await responsePromise
      const payload = await response.json()
      expect(payload.success, `拒绝推荐接口应成功：${payload.message || ''}`).toBe(true)
      await expect(page.locator('.el-message--success').filter({ hasText: '已标记为不感兴趣' })).toBeVisible()
      await expect(items).toHaveCount(countBefore - 1)
      await expect(page.locator('.recommend-item .item-name', { hasText: rejectedName })).toHaveCount(0)

      await clearRejectRecord(page.feedbackUserId, rejectedDish.dishId)

      markPassed('2.4', '拒绝反馈真实提交并从推荐列表移除对应菜品')
    } catch (e) {
      markFailed('2.4', e.message.slice(0, 50))
      throw e
    }
  })

  test('2.5 热点话题', async ({ page }) => {
    markRunning('2.5')
    try {
      const hotTopic = await getJson('/v1/home/hot-topic')
      expect(hotTopic.content).toBeTruthy()

      await page.goto('/user/home/hot-topic')
      await expect(page).toHaveURL(/\/user\/home\/hot-topic/)
      await expect(page.getByRole('heading', { name: '今日热点' })).toBeVisible()
      await expect(page.locator('.topic-title')).toHaveText(hotTopic.content)

      markPassed('2.5', '热点详情展示接口返回的真实内容')
    } catch (e) {
      markFailed('2.5', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => { updateSummary() })
})
