const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')
const { getOrder, getUserId, getMerchant, getMerchantId, getDish, getDishId, getCoupon } = require('../utils/real-data')
const { TEST_API_BASE } = require('../utils/api')

async function apiRequest(api, method, path, data) {
  const response = await api.fetch(`${TEST_API_BASE}${path}`, {
    method,
    data
  })
  const body = await response.json().catch(() => null)
  if (!response.ok() || !body?.success) {
    throw new Error(`${method} ${path} 失败：${body?.message || response.status()}`)
  }
  return body.data
}

async function createOrder(api, status) {
  const userId = await getUserId()
  const merchantId = await getMerchantId()
  const dish = await getDish({ merchantId })
  const dishId = dish.id || dish.dishId
  const price = Number(dish.price)
  return apiRequest(api, 'POST', '/v1/orders', {
    order: {
      userId,
      merchantId,
      totalAmount: price,
      status,
      address: '到店自取'
    },
    dishes: [{ dishId: String(dishId), quantity: 1, price }]
  })
}

async function setOrderStatus(api, orderId, status) {
  await apiRequest(api, 'PUT', `/v1/orders/${orderId}/status`, { status })
}

async function createGroupWithSession(api, groupName) {
  const userId = await getUserId()
  const group = await apiRequest(api, 'POST', '/v1/groups', {
    groupName,
    creatorId: userId,
    memberCount: 1
  })
  const groupId = group.groupId || group.group?.id || group.id
  const sessionId = group.sessionId
  await apiRequest(api, 'POST', '/v1/chat/sessions', {
    userId,
    sessionId,
    sessionType: 'group',
    sessionName: groupName,
    avatar: '👥',
    memberCount: 1,
    groupId
  })
  return { groupId, sessionId }
}

test.describe('模块四：订单流程', () => {

  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('4.1 创建订单', async ({ page }) => {
    markRunning('4.1')
    try {
      await page.goto('/user/home/merchants')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.locator('a[href*="merchant-detail"], [class*="merchant-card"], [class*="merchant-item"]').first().click()
      await page.waitForTimeout(1500)
      await page.locator('button:has-text("加入购物车")').first().click()
      await expect(page.locator('.el-message').filter({ hasText: '已加入购物车' })).toBeVisible({ timeout: 10000 })
      await page.locator('.draggable-cart-ball').click()
      const drawer = page.locator('.el-dialog').filter({ hasText: '我的购物车' })
      await expect(drawer).toBeVisible({ timeout: 10000 })
      await drawer.locator('button:has-text("提交订单")').click()
      await page.waitForURL('**/user/home/order-confirmation', { timeout: 15000 })
      await expect(page.locator('.order-confirmation-container .merchant-name')).toContainText(/\S+/)
      markPassed('4.1', '加购→购物车→提交订单→订单确认页')
    } catch (e) {
      markFailed('4.1', e.message.slice(0, 50))
      throw e
    }
  })

  test.fixme('4.2 订单支付 (blocked by #59)', async ({ page, request }) => {
    const orderId = await createOrder(request, 0)
    await page.goto('/user/home/orders?status=pending')
    const card = page.locator('.order-card').filter({ hasText: orderId })
    await expect(card).toBeVisible({ timeout: 10000 })
    await expect(card.locator('.el-tag')).toContainText('待支付')
    await card.locator('button:has-text("支付"), button:has-text("付款")').click()
    await expect.poll(async () => (await getOrder({ orderId })).status).toBe(1)
  })

  test('4.3 订单列表', async ({ page }) => {
    markRunning('4.3')
    try {
      await page.goto('/user/home/orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.order-card').first()).toBeVisible({ timeout: 10000 })
      await expect(page.locator('.order-list .order-card').first()).toContainText(/¥\d/)
      markPassed('4.3', '订单列表加载真实订单卡片')
    } catch (e) {
      markFailed('4.3', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.4 订单详情', async ({ page }) => {
    markRunning('4.4')
    try {
      const order = await getOrder({ status: 3 })
      await page.goto(`/user/home/order-detail/${order.id}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.order-no')).toContainText(order.id)
      await expect(page.locator('.status-header .el-tag')).toContainText('已完成')
      markPassed('4.4', `按真实订单 ${order.id} 校验详情`)
    } catch (e) {
      markFailed('4.4', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.5 确认收货', async ({ page, request }) => {
    markRunning('4.5')
    try {
      const orderId = await createOrder(request, 1)
      await setOrderStatus(request, orderId, 2)

      await page.goto('/user/home/orders?status=preparing')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const card = page.locator('.order-card').filter({ hasText: orderId })
      await expect(card).toBeVisible({ timeout: 10000 })
      await card.locator('button:has-text("确认收货")').click()
      await page.locator('.el-message-box button:has-text("确认收货")').click()
      await expect(page.locator('.el-message').filter({ hasText: '已确认收货' })).toBeVisible({ timeout: 10000 })
      await expect.poll(async () => (await getOrder({ orderId })).status).toBe(3)
      markPassed('4.5', `订单 ${orderId} 状态由 2 变为 3`)
    } catch (e) {
      markFailed('4.5', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.6 评价订单', async ({ page, request }) => {
    markRunning('4.6')
    try {
      const orderId = await createOrder(request, 3)
      const reviewContent = `E2E评价 ${Date.now()}`
      await page.goto(`/user/home/evaluate-order/${orderId}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.order-no')).toContainText(orderId)
      await page.locator('.el-rate__item').nth(4).click()
      await page.locator('.review-textarea textarea').fill(reviewContent)
      await page.locator('.submit-btn').click()
      await expect(page.locator('.el-message').filter({ hasText: '评价提交成功' })).toBeVisible({ timeout: 10000 })
      const review = await apiRequest(request, 'GET', `/v1/reviews/order/${orderId}`)
      expect(review.rating).toBe(5)
      expect(review.content).toBe(reviewContent)
      markPassed('4.6', `新订单 ${orderId} 提交评价并回查成功`)
    } catch (e) {
      markFailed('4.6', e.message.slice(0, 50))
      throw e
    }
  })

  test('4.7 取消订单', async ({ page, request }) => {
    markRunning('4.7')
    try {
      const orderId = await createOrder(request, 1)
      await page.goto('/user/home/orders?status=pendingAccept')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const card = page.locator('.order-card').filter({ hasText: orderId })
      await expect(card).toBeVisible({ timeout: 10000 })
      await card.locator('button:has-text("取消订单")').click()
      await expect(page.locator('.el-message').filter({ hasText: '订单已取消' })).toBeVisible({ timeout: 10000 })
      await expect.poll(async () => (await getOrder({ orderId })).status).toBe(4)
      markPassed('4.7', `订单 ${orderId} 状态由 1 变为 4`)
    } catch (e) {
      markFailed('4.7', e.message.slice(0, 50))
      throw e
    }
  })

  test.fixme('4.8 优惠券使用 (blocked by #60)', async ({ page, request }) => {
    const merchant = await getMerchant()
    const dish = await getDish({ merchantId: merchant.merchantId || merchant.id })
    const userId = await getUserId()
    const coupon = await getCoupon({ userId, issueIfMissing: true })
    const totalAmount = Math.max(Number(coupon.minAmount || 0), 120)

    await page.addInitScript(({ pendingOrder }) => {
      sessionStorage.setItem('pendingOrder', JSON.stringify(pendingOrder))
    }, {
      pendingOrder: {
        cartItems: [{ id: dish.id || dish.dishId, name: dish.name, price: totalAmount, quantity: 1 }],
        totalAmount,
        merchant: { id: merchant.merchantId || merchant.id, name: merchant.name }
      }
    })
    await page.goto('/user/home/order-confirmation')
    const couponSection = page.locator('.coupon-section')
    await expect(couponSection).toBeVisible({ timeout: 10000 })
    await expect(couponSection).toContainText(coupon.name)
    await couponSection.locator('button:has-text("使用")').click()
    await expect(page.locator('.discount')).toBeVisible({ timeout: 10000 })
    await expect(page.locator('.total-value')).not.toHaveText(`¥${totalAmount.toFixed(2)}`)
  })

  test('4.9 创建拼单', async ({ page, request }) => {
    markRunning('4.9')
    try {
      const groupName = `E2E拼单 ${Date.now()}`
      const { groupId } = await createGroupWithSession(request, groupName)

      await page.goto('/user/home/chat')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const conversation = page.locator('.conversation-item').filter({ hasText: groupName })
      await expect(conversation).toBeVisible({ timeout: 10000 })
      await conversation.click()
      await expect(page.locator('.chat-area-header')).toContainText(groupName, { timeout: 10000 })
      const createBtn = page.locator('.group-quick-actions .order-shortcut')
      await expect(createBtn).toBeVisible({ timeout: 10000 })
      await createBtn.click()
      await expect(page.locator('.el-message').filter({ hasText: '群订单已创建' })).toBeVisible({ timeout: 10000 })

      await expect.poll(async () => {
        const drafts = await apiRequest(request, 'GET', `/v1/group-orders/groups/${groupId}/orders?status=-1`)
        const userId = await getUserId()
        return drafts.some((order) => order.initiatorId === userId)
      }).toBe(true)
      markPassed('4.9', `群聊 ${groupId} 通过 UI 创建真实草稿订单`)
    } catch (e) {
      markFailed('4.9', e.message.slice(0, 50))
      throw e
    }
  })

  test.fixme('4.10 加入拼单 (blocked by #37)', async ({ page }) => {
    await page.goto('/user/home/chat')
    await expect(page.locator('button:has-text("加入群订单")').first()).toBeVisible({ timeout: 10000 })
  })

  test('4.11 拼单结算', async ({ page, request }) => {
    markRunning('4.11')
    try {
      const groupName = `E2E结算 ${Date.now()}`
      const { groupId } = await createGroupWithSession(request, groupName)
      const merchantId = await getMerchantId()
      const dishId = await getDishId({ merchantId })
      const userId = await getUserId()
      const groupOrderId = await apiRequest(request, 'POST', '/v1/group-orders/group-orders', {
        initiatorId: userId,
        groupId,
        merchantId,
        maxParticipants: 2,
        dishItems: [{ dishId: String(dishId), quantity: 1, userId }]
      })

      const confirmed = await apiRequest(request, 'POST', `/v1/group-orders/group-orders/${groupOrderId}/confirm`, {
        userId
      })
      expect(confirmed.locked).toBe(true)
      expect(confirmed.totalAmount).toBeGreaterThan(0)

      const settlement = await apiRequest(request, 'GET', `/v1/group-orders/group-orders/${groupOrderId}/settlement?userId=${userId}`)
      expect(settlement.orderId).toBe(groupOrderId)
      expect(settlement.totalAmount).toBe(confirmed.totalAmount)

      await page.goto('/user/home/chat')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.conversation-item').filter({ hasText: groupName })).toBeVisible({ timeout: 10000 })
      markPassed('4.11', `群订单 ${groupOrderId} 确认后结算金额一致`)
    } catch (e) {
      markFailed('4.11', e.message.slice(0, 50))
      throw e
    }
  })

  test.fixme('4.12 加菜请求 (blocked by #40)', async ({ page }) => {
    await page.goto('/user/home/chat')
    await expect(page.locator('button:has-text("我要加菜")').first()).toBeVisible({ timeout: 10000 })
  })

  test.afterAll(() => {
    updateSummary()
  })
})
