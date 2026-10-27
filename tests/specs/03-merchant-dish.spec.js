const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { getMerchant, getDish } = require('../utils/real-data')
const { TEST_API_BASE } = require('../utils/api')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

test.describe('模块三：商家浏览与详情', () => {

  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('3.1 商家列表', async ({ page }) => {
    markRunning('3.1')
    try {
      const merchant = await getMerchant()
      const merchantName = merchant.name || merchant.merchantName
      const merchantRating = Number(merchant.rating ?? merchant.score).toFixed(1)

      await page.goto('/user/home/merchants')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const merchantCard = page.locator('.merchant-card').filter({ hasText: merchantName })
      await expect(merchantCard).toBeVisible({ timeout: 10000 })
      await expect(merchantCard.locator('.merchant-name')).toHaveText(merchantName)
      await expect(merchantCard.locator('.rating-number')).toHaveText(merchantRating)
      markPassed('3.1', `商家列表展示真实商家 ${merchantName}`)
    } catch (e) {
      markFailed('3.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.2 商家搜索', async ({ page }) => {
    markRunning('3.2')
    try {
      const merchant = await getMerchant()
      const merchantName = merchant.name || merchant.merchantName

      await page.goto('/user/home/merchants')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const searchInput = page.locator('input[placeholder*="搜索"], input[placeholder*="商家"], .el-input__inner').first()
      await expect(searchInput).toBeVisible({ timeout: 10000 })
      await searchInput.fill(merchantName)

      const matchingCards = page.locator('.merchant-card').filter({ hasText: merchantName })
      await expect(matchingCards).toHaveCount(1)
      await expect(matchingCards.locator('.merchant-name')).toHaveText(merchantName)
      markPassed('3.2', `搜索结果匹配真实商家 ${merchantName}`)
    } catch (e) {
      markFailed('3.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.3 商家详情', async ({ page }) => {
    markRunning('3.3')
    try {
      const merchant = await getMerchant()
      const merchantId = merchant.merchantId || merchant.id
      const merchantName = merchant.name || merchant.merchantName

      await page.goto(`/user/home/merchant-detail?merchantId=${encodeURIComponent(merchantId)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.merchant-name')).toHaveText(merchantName, { timeout: 10000 })
      await expect(page.locator('.rating-score-text').first()).toHaveText(`${merchant.rating}分`)
      await expect(page.locator('.address-text')).toContainText(merchant.address)
      markPassed('3.3', `商家详情展示 ${merchantName} 的真实名称、评分和地址`)
    } catch (e) {
      markFailed('3.3', e.message.slice(0, 50))
      throw e
    }
  })

  test.fixme('3.4 商家公告 (blocked by #19)', async ({ page }) => {
    const merchant = await getMerchant()
    const merchantId = merchant.merchantId || merchant.id

    await page.goto(`/user/home/merchant-detail?merchantId=${encodeURIComponent(merchantId)}`)
    const announcement = page.locator('[class*="announcement"], [class*="notice"], [class*="公告"]')
    await expect(announcement.first()).toBeVisible({ timeout: 10000 })
  })

  test.fixme('3.5 商家评价列表 (blocked by #19)', async ({ page }) => {
    const merchant = await getMerchant()
    const merchantId = merchant.merchantId || merchant.id
    const response = await fetch(`${TEST_API_BASE}/v1/reviews/merchant/${encodeURIComponent(merchantId)}`)
    const payload = await response.json()

    expect(response.ok).toBeTruthy()
    expect(payload.success).toBe(true)
    const reviews = payload.data || []
    expect(reviews.length).toBeGreaterThan(0)

    await page.goto(`/user/home/merchant-detail?merchantId=${encodeURIComponent(merchantId)}`)
    await page.locator('.el-tabs__item', { hasText: '用户评价' }).click()

    await expect(page.locator('.comments-header')).toContainText(`共 ${reviews.length} 条评价`)
    await expect(page.locator('.comment-card')).toHaveCount(reviews.length)
    await expect(page.locator('.comment-content').first()).toHaveText(reviews[0].content)
  })

  test('3.6 菜品列表', async ({ page }) => {
    markRunning('3.6')
    try {
      const merchant = await getMerchant()
      const merchantId = merchant.merchantId || merchant.id
      const dish = await getDish({ merchantId })
      const dishName = dish.name || dish.dishName

      await page.goto(`/user/home/merchant-detail?merchantId=${encodeURIComponent(merchantId)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const dishCard = page.locator('.dish-card').filter({ hasText: dishName })
      await expect(dishCard).toBeVisible({ timeout: 10000 })
      await expect(dishCard.locator('.dish-name')).toHaveText(dishName)
      await expect(dishCard.locator('.dish-price')).toContainText(Number(dish.price).toFixed(2))
      markPassed('3.6', `菜品列表展示真实菜品 ${dishName}`)
    } catch (e) {
      markFailed('3.6', e.message.slice(0, 50))
      throw e
    }
  })

  test.fixme('3.7 菜品详情 (blocked by #17)', async ({ page }) => {
    const merchant = await getMerchant()
    const merchantId = merchant.merchantId || merchant.id
    const dish = await getDish({ merchantId })
    const dishId = dish.id || dish.dishId
    const dishName = dish.name || dish.dishName

    await page.goto(`/user/home/dish-detail/${encodeURIComponent(dishId)}`)

    await expect(page.locator('.page-title')).toHaveText('菜品详情')
    await expect(page.locator('.dish-name')).toHaveText(dishName)
    await expect(page.locator('.current-price')).toContainText(`¥${Number(dish.price).toFixed(2)}`)
  })

  test.fixme('3.8 菜品收藏 (blocked by #17)', async ({ page }) => {
    // 该测试用户已拒绝过全部推荐；先隔离共享拒绝记录，再验证收藏状态切换。
    await page.route('**/v1/recommendations/rejects/list*', route => route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true, code: '200', message: '成功', data: [] })
    }))

    await page.goto('/user/home/recommend')
    await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

    const favoriteButton = page.locator('.recommend-item button.act-btn', { hasText: '收藏' }).first()
    await expect(favoriteButton).toBeVisible({ timeout: 10000 })
    await expect(favoriteButton).toHaveText(/☆ 收藏/)

    await favoriteButton.click()
    await expect(favoriteButton).toHaveText(/★ 已收藏/)
    await expect(page.locator('.el-message')).toHaveText(/收藏成功/, { timeout: 10000 })
  })

  test('3.9 菜品加购', async ({ page }) => {
    markRunning('3.9')
    try {
      const merchant = await getMerchant()
      const merchantId = merchant.merchantId || merchant.id
      const dish = await getDish({ merchantId })
      const dishName = dish.name || dish.dishName

      await page.goto(`/user/home/merchant-detail?merchantId=${encodeURIComponent(merchantId)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const dishCard = page.locator('.dish-card').filter({ hasText: dishName })
      await expect(dishCard).toBeVisible({ timeout: 10000 })
      await dishCard.locator('button', { hasText: '加入购物车' }).click()

      await expect(page.locator('.el-message')).toHaveText(new RegExp(`已加入购物车`), { timeout: 10000 })
      await expect(page.locator('.draggable-cart-ball .el-badge__content')).toHaveText('1')
      markPassed('3.9', `真实菜品 ${dishName} 加入购物车，数量更新为 1`)
    } catch (e) {
      markFailed('3.9', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => {
    updateSummary()
  })
})
