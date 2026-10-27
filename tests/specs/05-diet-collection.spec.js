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

async function getCollections() {
  return getJson(`/v1/collections?userId=${encodeURIComponent(testUserId)}`)
}

async function findRecommendationDish(page) {
  const name = await page.locator('.recommend-item').first().locator('.item-name').innerText()
  const recommendations = await getJson(`/v1/recommendations/${testUserId}`)
  const dish = recommendations.recommendations.find((item) => item.dishName === name)
  if (!dish) {
    throw new Error(`推荐接口中未找到页面菜品：${name}`)
  }
  return { id: String(dish.dishId), name }
}

async function waitForCollectionCount(page, count) {
  await expect(page.locator('.collection-stats')).toContainText(`共 ${count} 个收藏`)
  await expect(page.locator('.collection-card')).toHaveCount(count)
}

test.describe('模块五：饮食管理与收藏', () => {

  test.beforeAll(async () => { testUserId = await getUserId() })

  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('5.1 卡路里统计', async ({ page }) => {
    markRunning('5.1')
    try {
      await page.goto('/user/home/calorie')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const chart = page.locator('[class*="calorie"], [class*="chart"], [class*="statistic"]')
      await expect(chart.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.1', '卡路里页面可访问')
    } catch (e) { markFailed('5.1', e.message.slice(0, 50)); throw e }
  })

  test('5.2 饮食记录', async ({ page }) => {
    markRunning('5.2')
    try {
      await page.goto('/user/home/diet-record')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const record = page.locator('[class*="diet"], [class*="record"], .el-card')
      await expect(record.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.2', '饮食记录页面可访问')
    } catch (e) { markFailed('5.2', e.message.slice(0, 50)); throw e }
  })

  test('5.3 今日食谱', async ({ page }) => {
    markRunning('5.3')
    try {
      await page.goto('/user/home/today-recipe')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const recipe = page.locator('[class*="recipe"], [class*="today"], .el-card')
      await expect(recipe.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.3', '今日食谱页面可访问')
    } catch (e) { markFailed('5.3', e.message.slice(0, 50)); throw e }
  })

  test('5.4 我的食谱', async ({ page }) => {
    markRunning('5.4')
    try {
      await page.goto('/user/home/my-recipe')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const recipe = page.locator('[class*="recipe"], .el-card')
      await expect(recipe.first()).toBeVisible({ timeout: 10000 })
      markPassed('5.4', '我的食谱页面可访问')
    } catch (e) { markFailed('5.4', e.message.slice(0, 50)); throw e }
  })

  test('5.5 营养分析', async ({ page }) => {
    markRunning('5.5')
    try {
      await page.goto('/user/home/calorie')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const date = new Date().toISOString().split('T')[0]
      const records = await getJson(`/calorie-records/user/${testUserId}/date/${date}`)
      const totals = records.reduce((acc, record) => ({
        protein: acc.protein + (Number(record.protein) || 0),
        carbohydrate: acc.carbohydrate + (Number(record.carbohydrate) || 0),
        fat: acc.fat + (Number(record.fat) || 0)
      }), { protein: 0, carbohydrate: 0, fat: 0 })
      const expected = [
        ['蛋白质', Math.round(totals.protein * 100) / 100, 90],
        ['碳水化合物', Math.round(totals.carbohydrate * 100) / 100, 250],
        ['脂肪', Math.round(totals.fat * 100) / 100, 70]
      ]

      for (const [name, value, goal] of expected) {
        const item = page.locator('.nutrition-item').filter({ hasText: name })
        await expect(item.locator('.nutri-val')).toHaveText(String(value))
        await expect(item.locator('.nutri-unit')).toHaveText('g')
        await expect(item.locator('.nutri-goal')).toHaveText(`/ ${goal}g`)
      }
      markPassed('5.5', '蛋白质/碳水/脂肪数值与目标来自真实接口')
    } catch (e) { markFailed('5.5', e.message.slice(0, 50)); throw e }
  })

  test('5.6 我的收藏列表', async ({ page }) => {
    markRunning('5.6')
    try {
      const collections = await getCollections()
      await page.goto('/user/home/my-collection')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await waitForCollectionCount(page, collections.length)
      await expect(page.locator('.collection-card .collection-title').first()).not.toBeEmpty()
      const pageTitles = await page.locator('.collection-card .collection-title').allTextContents()
      expect(pageTitles.length).toBeGreaterThan(0)
      markPassed('5.6', `收藏数量与接口一致：${collections.length}`)
    } catch (e) { markFailed('5.6', e.message.slice(0, 50)); throw e }
  })

  test.fixme('5.7 添加收藏 (blocked by #46)', async ({ page }) => {
    await page.goto('/user/home/recommend')
    await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
    await expect(page.locator('.recommend-item').first()).toBeVisible({ timeout: 10000 })
    const dish = await findRecommendationDish(page)
    const before = await getCollections()
    const item = page.locator('.recommend-item').filter({ hasText: dish.name }).first()

    await item.locator('button.act-btn').filter({ hasText: '☆ 收藏' }).click()
    await expect(item.locator('button.act-btn')).toContainText('★ 已收藏')

    const after = await getCollections()
    expect(after.filter((entry) => entry.collectableType === 'dish' && String(entry.collectableId) === dish.id)).toHaveLength(
      before.filter((entry) => entry.collectableType === 'dish' && String(entry.collectableId) === dish.id).length + 1
    )
  })

  test.fixme('5.8 取消收藏 (blocked by #17)', async ({ page }) => {
    await page.goto('/user/home/my-collection')
    await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
    const before = await getCollections()
    const card = page.locator('.collection-card').first()
    const title = await card.locator('.collection-title').innerText()

    await card.locator('.delete-btn').click()
    await page.locator('.el-message-box__btns button').filter({ hasText: '确定' }).click()
    await expect(page.locator('.collection-card').filter({ hasText: title })).toHaveCount(0)

    const after = await getCollections()
    expect(after).toHaveLength(before.length - 1)
  })

  test.afterAll(() => { updateSummary() })
})
