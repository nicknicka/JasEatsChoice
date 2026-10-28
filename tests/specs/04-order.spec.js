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

async function createOrder(api, status, totalAmount) {
  const userId = await getUserId()
  const merchantId = await getMerchantId()
  const dish = await getDish({ merchantId })
  const dishId = dish.id || dish.dishId
  const price = Number(dish.price)
  return apiRequest(api, 'POST', '/v1/orders', {
    order: {
      userId,
      merchantId,
      totalAmount: totalAmount ?? price,
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

  test('4.2 订单支付', async ({ page, request }) => {
    markRunning('4.2')
    try {
      const userId = await getUserId()
      const orderId = await createOrder(request, 0)
      const order = await getOrder({ orderId })
      const amount = Number(order.totalAmount)

      // 充值后支付相同金额，既保证余额充足，也让钱包余额回到测试前水平。
      const walletBefore = await apiRequest(request, 'GET', `/v1/wallet/info/${userId}`)
      const rechargeNo = `e2e-order-pay-${Date.now()}`
      await apiRequest(
        request,
        'POST',
        `/v1/wallet/recharge?userId=${encodeURIComponent(userId)}&amount=${encodeURIComponent(amount)}&rechargeNo=${encodeURIComponent(rechargeNo)}`
      )
      const walletAfterRecharge = await apiRequest(request, 'GET', `/v1/wallet/info/${userId}`)

      await page.goto('/user/home/orders?status=pending')
      const card = page.locator('.order-card').filter({ hasText: orderId })
      await expect(card).toBeVisible({ timeout: 10000 })
      await expect(card.locator('.el-tag')).toContainText('待支付')
      const payButton = card.locator('button:has-text("支付")')
      await expect(payButton).toBeVisible()
      await payButton.click()

      await expect.poll(async () => (await getOrder({ orderId })).status).toBe(1)
      const walletAfterPayment = await apiRequest(request, 'GET', `/v1/wallet/info/${userId}`)
      expect(Number(walletAfterRecharge.balance) - Number(walletAfterPayment.balance)).toBe(amount)
      expect(Number(walletAfterPayment.balance)).toBe(Number(walletBefore.balance))

      // 详情页同样必须提供可见入口，且支付后状态立即刷新。
      const detailOrderId = await createOrder(request, 0)
      const detailOrder = await getOrder({ orderId: detailOrderId })
      const detailAmount = Number(detailOrder.totalAmount)
      const detailRechargeNo = `e2e-order-detail-pay-${Date.now()}`
      await apiRequest(
        request,
        'POST',
        `/v1/wallet/recharge?userId=${encodeURIComponent(userId)}&amount=${encodeURIComponent(detailAmount)}&rechargeNo=${encodeURIComponent(detailRechargeNo)}`
      )
      await page.goto(`/user/home/order-detail/${detailOrderId}`)
      const detailPayButton = page.locator('.order-actions button:has-text("支付")')
      await expect(detailPayButton).toBeVisible()
      await detailPayButton.click()
      await expect.poll(async () => (await getOrder({ orderId: detailOrderId })).status).toBe(1)
      await expect(page.locator('.status-header .el-tag')).toContainText('待接单')
      const walletAfterDetailPayment = await apiRequest(request, 'GET', `/v1/wallet/info/${userId}`)
      expect(Number(walletAfterDetailPayment.balance)).toBe(Number(walletBefore.balance))

      // 支付失败必须展示后端消息，且订单不能变成已支付。
      const blockedWalletBefore = await apiRequest(request, 'GET', `/v1/wallet/info/${userId}`)
      const blockedOrderId = await createOrder(request, 0, Number(blockedWalletBefore.balance) + 100)
      await page.goto(`/user/home/order-detail/${blockedOrderId}`)
      const blockedPayButton = page.locator('.order-actions button:has-text("支付")')
      await expect(blockedPayButton).toBeVisible()
      const blockedResponse = page.waitForResponse((response) =>
        response.url().includes(`/v1/orders/${blockedOrderId}/pay`)
      )
      await blockedPayButton.click()
      const blockedHttpResponse = await blockedResponse
      const blockedBody = await blockedHttpResponse.json()
      expect(blockedBody.success).toBe(false)
      expect(blockedBody.message).toContain('余额不足')
      await expect(page.locator('.el-message--error')).toContainText(blockedBody.message)
      await expect.poll(async () => (await getOrder({ orderId: blockedOrderId })).status).toBe(0)
      const blockedWalletAfter = await apiRequest(request, 'GET', `/v1/wallet/info/${userId}`)
      expect(Number(blockedWalletAfter.balance)).toBe(Number(blockedWalletBefore.balance))

      markPassed('4.2', `订单 ${orderId} 列表支付、订单 ${detailOrderId} 详情支付均成功，状态 0→1，钱包余额恢复；余额不足时显示后端错误`)
    } catch (e) {
      markFailed('4.2', e.message.slice(0, 50))
      throw e
    }
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

  test('4.10 加入拼单', async ({ page, request }) => {
    markRunning('4.10')
    try {
      const groupName = `E2E加入拼单 ${Date.now()}`
      const { groupId } = await createGroupWithSession(request, groupName)
      const currentUserId = await getUserId()
      const initiatorId = await getUserId({ phone: '13800138010' })
      const groupOrderId = await apiRequest(request, 'POST', '/v1/group-orders/group-orders', {
        initiatorId,
        groupId,
        maxParticipants: 2
      })

      await page.goto('/user/home/chat')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const conversation = page.locator('.conversation-item').filter({ hasText: groupName })
      await expect(conversation).toBeVisible({ timeout: 10000 })
      await conversation.click()

      const joinBtn = page.locator('button:has-text("加入群订单")').first()
      await expect(joinBtn).toBeVisible({ timeout: 10000 })
      await joinBtn.click()
      await expect(page.locator('.el-message').filter({ hasText: '已加入群订单' })).toBeVisible({ timeout: 10000 })

      const getDetail = () => apiRequest(
        request,
        'GET',
        `/v1/group-orders/group-orders/${groupOrderId}?userId=${currentUserId}`
      )
      await expect.poll(async () => {
        const detail = await getDetail()
        return detail.groupOrder?.currentUserJoined === true
          && detail.groupOrder.members?.some((member) => String(member.userId) === String(currentUserId))
      }).toBe(true)

      await page.reload()
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const refreshedConversation = page.locator('.conversation-item').filter({ hasText: groupName })
      await expect(refreshedConversation).toBeVisible({ timeout: 10000 })
      await refreshedConversation.click()
      await expect(page.locator('button:has-text("加入群订单")')).toBeHidden()
      await expect(page.locator('.floating-order-btn')).toBeVisible({ timeout: 10000 })

      const refreshedDetail = await getDetail()
      expect(refreshedDetail.groupOrder.currentUserJoined).toBe(true)
      expect(refreshedDetail.groupOrder.members.some((member) => String(member.userId) === String(currentUserId))).toBe(true)
      markPassed('4.10', `用户 ${currentUserId} 通过 UI 加入群订单 ${groupOrderId}，刷新后仍在成员中`)
    } catch (e) {
      markFailed('4.10', e.message.slice(0, 50))
      throw e
    }
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

  test('4.12 加菜请求', async ({ page, request }) => {
    markRunning('4.12')
    try {
      const groupName = `E2E加菜 ${Date.now()}`
      const { groupId } = await createGroupWithSession(request, groupName)
      const merchantId = await getMerchantId()
      const merchant = await getMerchant({ merchantId })
      const dish = await getDish({ merchantId })
      const dishId = dish.id || dish.dishId
      const userId = await getUserId()
      const groupOrderId = await apiRequest(request, 'POST', '/v1/group-orders/group-orders', {
        initiatorId: userId,
        groupId,
        maxParticipants: 2,
        dishItems: []
      })

      const rechargeNo = `E2E-ADD-DISH-${Date.now()}`
      const rechargeAmount = Math.max(Number(dish.price || 0), 1) + 10
      await apiRequest(
        request,
        'POST',
        `/v1/wallet/recharge?userId=${encodeURIComponent(userId)}&amount=${encodeURIComponent(rechargeAmount)}&rechargeNo=${encodeURIComponent(rechargeNo)}`
      )

      await page.goto('/user/home/chat')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const conversation = page.locator('.conversation-item').filter({ hasText: groupName })
      await expect(conversation).toBeVisible({ timeout: 10000 })
      await conversation.click()
      const floatingOrderBtn = page.locator('.floating-order-btn')
      await expect(floatingOrderBtn).toBeVisible({ timeout: 10000 })

      await floatingOrderBtn.click()
      const draftDrawer = page.locator('.group-order-drawer').filter({ hasText: groupName })
      await expect(draftDrawer).toBeVisible({ timeout: 10000 })
      await draftDrawer.locator('button.quick-order-btn:has-text("选择商家")').click()

      const merchantDialog = page.locator('.merchant-select-dialog')
      await expect(merchantDialog).toBeVisible({ timeout: 10000 })
      await merchantDialog.locator('.merchant-card').filter({ hasText: merchant.name }).first().click()

      const productDialog = page.locator('.product-select-dialog')
      await expect(productDialog).toBeVisible({ timeout: 10000 })
      const productItem = productDialog.locator('.product-item').filter({ hasText: dish.name }).first()
      await expect(productItem).toBeVisible({ timeout: 10000 })
      await productItem.locator('.el-checkbox').click()
      await productDialog.locator('button.batch-btn:has-text("加入订单")').click()
      await expect(page.locator('.el-message').filter({ hasText: '已批量加入 1 个商品' })).toBeVisible({ timeout: 10000 })

      await draftDrawer.locator('.el-drawer__close-btn').click()
      await expect(draftDrawer).toBeHidden({ timeout: 10000 })

      await apiRequest(request, 'POST', `/v1/group-orders/group-orders/${groupOrderId}/selections`, {
        userId,
        merchantId,
        dishes: [{ dishId: String(dishId), quantity: 1, customization: 'E2E 加菜前置' }]
      })
      await apiRequest(request, 'POST', `/v1/group-orders/group-orders/${groupOrderId}/confirm`, { userId })
      await apiRequest(request, 'POST', `/v1/group-orders/group-orders/${groupOrderId}/pay`, {
        userId,
        paymentType: 'single',
        paymentMethod: 'balance'
      })

      await page.evaluate((paidGroupOrderId) => {
        sessionStorage.setItem('paidGroupOrderId', paidGroupOrderId)
      }, groupOrderId)
      await conversation.click()
      await floatingOrderBtn.click()

      const drawer = page.locator('.group-order-drawer, .el-drawer').filter({ hasText: groupName })
      await expect(drawer).toBeVisible({ timeout: 10000 })
      await expect(drawer.locator('.header-right .el-tag').filter({ hasText: '已支付' })).toBeVisible({ timeout: 10000 })
      const addDishBtn = drawer.locator('button:has-text("我要加菜")')
      await drawer.locator('.el-drawer__body').evaluate((element) => {
        element.scrollTop = element.scrollHeight
      })
      await expect(addDishBtn).toBeVisible({ timeout: 10000 })
      await addDishBtn.click()
      const dialog = page.locator('.add-dish-dialog')
      await expect(dialog).toBeVisible({ timeout: 10000 })

      const dishItem = dialog.locator('.dish-item').filter({ hasText: dish.name }).first()
      await expect(dishItem).toBeVisible({ timeout: 10000 })
      await dishItem.locator('button:has-text("添加")').click()
      const submitBtn = dialog.locator('button:has-text("提交加菜请求")')
      await expect(submitBtn).toBeEnabled()

      const addDishResponsePromise = page.waitForResponse((response) =>
        response.request().method() === 'POST'
          && response.url().includes('/api/v1/add-dish/request')
      )
      await submitBtn.click()
      const addDishResponse = await addDishResponsePromise
      expect(addDishResponse.ok()).toBeTruthy()
      const submitted = addDishResponse.request().postDataJSON()
      expect(submitted.groupOrderId).toBe(groupOrderId)
      expect(String(submitted.dishItems[0].dishId)).toBe(String(dishId))

      await expect(page.locator('.el-message').filter({ hasText: '加菜请求已提交' }).first()).toBeVisible({ timeout: 10000 })

      const reviewList = await apiRequest(request, 'GET', `/v1/add-dish/review-list/${groupOrderId}`)
      expect(reviewList).toHaveLength(1)
      expect(reviewList[0].status).toBe(0)
      expect(reviewList[0].groupOrderId).toBe(groupOrderId)
      expect(Number(reviewList[0].totalAmount)).toBe(Number(dish.price))
      markPassed('4.12', `群订单 ${groupOrderId} 经 UI 提交加菜请求并回查 pending 记录`)
    } catch (e) {
      markFailed('4.12', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => {
    updateSummary()
  })
})
