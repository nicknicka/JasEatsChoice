const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { getMerchant, getDish, getUserId } = require('../utils/real-data')
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

  test('3.4 商家公告', async ({ page, request }) => {
    markRunning('3.4')
    const merchantId = '7638432224340229'
    const title = `E2E 公告 ${Date.now()}`
    let createdId
    try {
      // seed 公告的 end_time 已过期，会被 AnnouncementScheduler 自动下线，不能作为稳定前置数据。
      // 这里自建一条 end_time 在未来的临时公告，验证后再清理。
      const createResp = await request.post(`${TEST_API_BASE}/v1/merchant/${merchantId}/announcements`, {
        data: {
          title,
          content: `${title} 的内容`,
          type: 'system',
          priority: 'normal',
          targetAudience: 'all',
          readCount: 0,
          readUsers: 0,
          status: 'active',
          startTime: new Date(Date.now() - 3600_000).toISOString().slice(0, 19),
          endTime: new Date(Date.now() + 86_400_000).toISOString().slice(0, 19)
        }
      })
      const createBody = await createResp.json()
      expect(createBody.success, `新增公告应成功：${createBody.message || ''}`).toBe(true)
      createdId = createBody.data.id
      expect(createdId).toBeTruthy()

      const listResp = await request.get(`${TEST_API_BASE}/v1/merchant/${merchantId}/announcements`)
      const listBody = await listResp.json()
      expect(listBody.success).toBe(true)
      const mine = (listBody.data || []).find((item) => String(item.id) === String(createdId))
      expect(mine, '刚创建的公告应能在接口中查到').toBeTruthy()
      expect(mine.status).toBe('active')

      await page.goto(`/user/home/merchant-detail?merchantId=${encodeURIComponent(merchantId)}`)
      await expect(page.locator('.merchant-announcements')).toBeVisible({ timeout: 10000 })
      const card = page.locator('.announcement-card', { hasText: title })
      await expect(card).toBeVisible({ timeout: 10000 })
      await expect(card.locator('.announcement-title')).toHaveText(title)
      await expect(card.locator('.announcement-content')).toHaveText(`${title} 的内容`)
      markPassed('3.4', `商家公告展示与接口一致（临时公告 ${createdId}）`)

      const emptyResp = await request.get(`${TEST_API_BASE}/v1/merchant/9999999999999/announcements`)
      const emptyBody = await emptyResp.json()
      expect(emptyBody.success).toBe(true)
      expect(emptyBody.data).toEqual([])
      await page.goto(`/user/home/merchant-detail?merchantId=9999999999999`)
      await expect(page.locator('.announcements-empty')).toHaveText('该商家暂无公告')
    } catch (e) {
      markFailed('3.4', e.message.slice(0, 50))
      throw e
    } finally {
      if (createdId) {
        await request.delete(`${TEST_API_BASE}/v1/merchant/${merchantId}/announcements/${createdId}`).catch(() => null)
      }
    }
  })

  test('3.5 商家评价列表', async ({ page }) => {
    markRunning('3.5')
    try {
      const merchant = await getMerchant()
      const merchantId = merchant.merchantId || merchant.id
      const response = await fetch(`${TEST_API_BASE}/v1/reviews/merchant/${encodeURIComponent(merchantId)}`)
      const payload = await response.json()

      expect(response.ok).toBeTruthy()
      expect(payload.success).toBe(true)
      const reviews = payload.data || []
      expect(reviews.length).toBeGreaterThan(0)

      await page.goto(`/user/home/merchant-detail?merchantId=${encodeURIComponent(merchantId)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const commentsTab = page.locator('.merchant-menu-tabs .el-tabs__item', { hasText: '用户评价' })
      await expect(commentsTab).toBeVisible({ timeout: 10000 })
      await commentsTab.scrollIntoViewIfNeeded()
      await commentsTab.click()
      await expect(page.locator('.comments-header')).toContainText(`共 ${reviews.length} 条评价`)
      await expect(page.locator('.comment-card')).toHaveCount(reviews.length)
      await expect(page.locator('.comment-content').first()).toHaveText(reviews[0].content)
      markPassed('3.5', `评价列表与接口一致（${reviews.length} 条）`)
    } catch (e) {
      markFailed('3.5', e.message.slice(0, 50))
      throw e
    }
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

  test('3.7 菜品详情', async ({ page }) => {
    markRunning('3.7')
    try {
      const merchant = await getMerchant()
      const merchantId = merchant.merchantId || merchant.id
      const dish = await getDish({ merchantId })
      const dishId = dish.id || dish.dishId
      const dishName = dish.name || dish.dishName

      await page.goto(`/user/home/dish-detail/${encodeURIComponent(dishId)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.page-title')).toHaveText('菜品详情')
      await expect(page.locator('.dish-name')).toHaveText(dishName)
      await expect(page.locator('.current-price')).toContainText(`¥${Number(dish.price).toFixed(2)}`)
      markPassed('3.7', `菜品详情展示真实菜品 ${dishName}`)
    } catch (e) {
      markFailed('3.7', e.message.slice(0, 50))
      throw e
    }
  })

  test('3.8 菜品收藏', async ({ page }) => {
    markRunning('3.8')
    try {
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

      const collectionRequestPromise = page.waitForRequest((request) =>
        request.method() === 'POST' && new URL(request.url()).pathname.endsWith('/v1/collections')
      )
      await favoriteButton.click()
      const collectionRequest = await collectionRequestPromise
      const collectionPayload = collectionRequest.postDataJSON()

      expect(collectionPayload.collectableType).toBe('dish')
      expect(collectionPayload.collectableId).toBeTruthy()
      await expect(favoriteButton).toHaveText(/★ 已收藏/)
      await expect(page.locator('.el-message--success')).toHaveText(/收藏成功/, { timeout: 10000 })

      const userId = await getUserId()
      const queryResponse = await fetch(`${TEST_API_BASE}/v1/collections?userId=${encodeURIComponent(userId)}`)
      const payload = await queryResponse.json()
      expect(queryResponse.ok).toBeTruthy()
      expect(payload.success).toBe(true)
      expect((payload.data || []).some((item) =>
        item.collectableType === collectionPayload.collectableType
        && String(item.collectableId) === String(collectionPayload.collectableId)
      )).toBeTruthy()

      const cleanupUrl = new URL(`${TEST_API_BASE}/v1/collections`)
      cleanupUrl.searchParams.set('userId', userId)
      cleanupUrl.searchParams.set('type', collectionPayload.collectableType)
      cleanupUrl.searchParams.set('id', String(collectionPayload.collectableId))
      await fetch(cleanupUrl, { method: 'DELETE' })

      await page.route('**/v1/collections', route => {
        if (route.request().method() === 'POST') {
          return route.fulfill({
            status: 500,
            contentType: 'application/json',
            body: JSON.stringify({ success: false, code: '500', message: '收藏失败' })
          })
        }
        return route.continue()
      })

      await page.reload()
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(favoriteButton).toHaveText(/☆ 收藏/)
      await favoriteButton.click()
      await expect(page.locator('.el-message--error')).toHaveText(/收藏失败/, { timeout: 10000 })
      await expect(favoriteButton).toHaveText(/☆ 收藏/)

      const failedQueryResponse = await fetch(`${TEST_API_BASE}/v1/collections?userId=${encodeURIComponent(userId)}`)
      const failedPayload = await failedQueryResponse.json()
      expect((failedPayload.data || []).some((item) =>
        item.collectableType === collectionPayload.collectableType
        && String(item.collectableId) === String(collectionPayload.collectableId)
      )).toBeFalsy()

      markPassed('3.8', '收藏成功落库，失败时保持未收藏状态')
    } catch (e) {
      markFailed('3.8', e.message.slice(0, 50))
      throw e
    }
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
