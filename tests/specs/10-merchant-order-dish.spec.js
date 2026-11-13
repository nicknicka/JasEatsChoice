const { test, expect, loginAsMerchant } = require('../fixtures/test-base')
const { getMerchantId, getOrder } = require('../utils/real-data')
const { TEST_API_BASE } = require('../utils/api')
const {
  markRunning,
  markPassed,
  markFailed,
  updateSummary
} = require('../utils/progress')

function apiUrl(path, params = {}) {
  const url = new URL(`${TEST_API_BASE}${path}`)
  Object.entries(params).forEach(([key, value]) => url.searchParams.set(key, value))
  return url.toString()
}

async function apiRequest(page, path, options = {}) {
  const token = await page.evaluate(() => localStorage.getItem('auth_token') || localStorage.getItem('token'))
  const response = await fetch(`${TEST_API_BASE}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  })
  const body = await response.json().catch(() => null)
  if (!response.ok || !body || body.success !== true) {
    throw new Error(`请求 ${path} 失败（${response.status}）：${body?.message || '无响应错误信息'}`)
  }
  return body.data
}

async function restoreOrderStatus(page, orderId, status) {
  await apiRequest(page, `/v1/orders/${encodeURIComponent(orderId)}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status })
  })
}

async function getChatSession(page, userId, sessionId) {
  const sessions = await apiRequest(page, `/v1/chat/users/${encodeURIComponent(userId)}/chat-sessions`)
  return sessions.find(item => item.id === sessionId)
}

async function fetchJson(page, path, params = {}) {
  const token = await page.evaluate(() => localStorage.getItem('auth_token') || localStorage.getItem('token'))
  const response = await fetch(apiUrl(path, params), {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
  if (!response.ok) {
    throw new Error(`请求 ${path} 失败（${response.status}）`)
  }
  return response.json()
}

async function createTemporaryOrder(page, merchantId) {
  const userId = await page.evaluate(() => localStorage.getItem('auth_userId'))
  const dishes = await apiRequest(page, `/v1/dishes?merchantId=${encodeURIComponent(merchantId)}`)
  const dish = dishes?.[0]
  expect(dish, '商家没有可用于创建临时订单的菜品').toBeTruthy()

  const orderId = await apiRequest(page, '/v1/orders', {
    method: 'POST',
    body: JSON.stringify({
      order: {
        userId: String(userId),
        merchantId,
        totalAmount: Number(dish.price),
        status: 1,
        address: '删除回归临时订单'
      },
      dishes: [{ dishId: String(dish.id), quantity: 1, price: Number(dish.price) }]
    })
  })
  return { orderId, dish }
}

async function createTemporaryMenu(page, merchantId, dishId) {
  const name = `删除回归-${Date.now()}`
  const menu = await apiRequest(page, `/v1/menus/merchants/${encodeURIComponent(merchantId)}/menu`, {
    method: 'POST',
    body: JSON.stringify({
      merchantId,
      menuName: name,
      category: '删除回归',
      status: 'inactive'
    })
  })
  await apiRequest(page, `/v1/menus/merchants/${encodeURIComponent(merchantId)}/menu/${encodeURIComponent(menu.id)}`, {
    method: 'PUT',
    body: JSON.stringify({
      name,
      category: '删除回归',
      status: 'offline',
      dishes: [{ id: String(dishId), status: 1 }]
    })
  })
  return { ...menu, name }
}

test.describe('模块十：商家端-订单与菜品管理', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsMerchant(page)
  })

  test('10.1 商家订单列表', async ({ page }) => {
    markRunning('10.1')
    try {
      const merchantId = await getMerchantId()
      const orders = await apiRequest(page, `/v1/orders/merchant/${encodeURIComponent(merchantId)}?today=false`)
      await page.goto('/merchant/home/orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.order-item')).toHaveCount(orders.length)
      await expect(page.locator('.order-item').first()).toContainText(orders[0].id)
      markPassed('10.1', `订单列表展示商家 ${merchantId} 的 ${orders.length} 条真实订单`)
    } catch (e) {
      markFailed('10.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('10.2 今日订单', async ({ page }) => {
    markRunning('10.2')
    try {
      const merchantId = await getMerchantId()
      const todayOrders = await apiRequest(page, `/v1/orders/merchant/${encodeURIComponent(merchantId)}?today=true`)
      await page.goto('/merchant/home/today-orders')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.order-item')).toHaveCount(todayOrders.length)
      if (todayOrders.length > 0) {
        await expect(page.locator('.order-item').first()).toContainText(todayOrders[0].id)
      }
      markPassed('10.2', `今日订单展示接口返回的 ${todayOrders.length} 条订单`)
    } catch (e) {
      markFailed('10.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('10.3 商家订单详情', async ({ page }) => {
    markRunning('10.3')
    try {
      const merchantId = await getMerchantId()
      const order = await getOrder({ merchantId })
      const dishes = await apiRequest(page, `/v1/orders/${encodeURIComponent(order.id)}/dishes`)

      await page.goto(`/merchant/home/order-detail/${encodeURIComponent(order.id)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.order-no')).toHaveText(order.id)
      await expect(page.locator('.total-value')).toContainText(order.totalAmount.toFixed(2))
      await expect(page.locator('.item-name').first()).toHaveText(dishes[0].dishName)
      markPassed('10.3', `详情展示真实订单 ${order.id} 及其菜品 ${dishes[0].dishName}`)
    } catch (e) {
      markFailed('10.3', e.message.slice(0, 50))
      throw e
    }
  })

  test('10.4 接单', async ({ page }) => {
    markRunning('10.4')
    const merchantId = await getMerchantId()
    const order = await getOrder({ merchantId, status: 1 })
    const originalStatus = order.status
    try {
      await page.goto('/merchant/home/orders?status=1')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const orderRow = page.locator('.order-item', { hasText: order.id })
      await expect(orderRow).toBeVisible()
      const updateResponse = page.waitForResponse((response) =>
        response.url().includes(`/api/v1/orders/${order.id}/status`) &&
        response.request().method() === 'PUT'
      )
      await orderRow.getByRole('button', { name: '接单' }).click()
      expect((await updateResponse).ok()).toBe(true)

      const updated = await apiRequest(page, `/v1/orders/${encodeURIComponent(order.id)}`)
      expect(updated.status).toBe(2)
      await page.goto(`/merchant/home/order-detail/${encodeURIComponent(order.id)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.status-text')).toHaveText('制作中')
      markPassed('10.4', `订单 ${order.id} 从待接单更新为制作中`)
    } catch (e) {
      markFailed('10.4', e.message.slice(0, 50))
      throw e
    } finally {
      await restoreOrderStatus(page, order.id, originalStatus)
    }
  })

  test('10.5 订单状态更新', async ({ page }) => {
    markRunning('10.5')
    const merchantId = await getMerchantId()
    const order = await getOrder({ merchantId, status: 2 })
    const originalStatus = order.status
    try {
      await page.goto('/merchant/home/orders?status=2')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const orderRow = page.locator('.order-item', { hasText: order.id })
      await expect(orderRow).toBeVisible()
      const updateResponse = page.waitForResponse((response) =>
        response.url().includes(`/api/v1/orders/${order.id}/status`) &&
        response.request().method() === 'PUT'
      )
      await orderRow.getByRole('button', { name: '完成订单' }).click()
      expect((await updateResponse).ok()).toBe(true)

      const updated = await apiRequest(page, `/v1/orders/${encodeURIComponent(order.id)}`)
      expect(updated.status).toBe(3)
      await page.goto(`/merchant/home/order-detail/${encodeURIComponent(order.id)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.status-text')).toHaveText('已完成')
      markPassed('10.5', `订单 ${order.id} 从制作中更新为已完成`)
    } catch (e) {
      markFailed('10.5', e.message.slice(0, 50))
      throw e
    } finally {
      await restoreOrderStatus(page, order.id, originalStatus)
    }
  })

  test('10.6 菜单管理', async ({ page }) => {
    markRunning('10.6')
    try {
      const merchantId = await getMerchantId()
      const menus = await apiRequest(page, `/v1/menus/merchants/${encodeURIComponent(merchantId)}/menu`)
      await page.goto('/merchant/home/menu')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.menu-item')).toHaveCount(menus.length)
      await expect(page.locator('.menu-item').first()).toContainText(menus[0].name || menus[0].menuName)
      markPassed('10.6', `菜单管理展示 ${menus.length} 条真实菜单`)
    } catch (e) {
      markFailed('10.6', e.message.slice(0, 50))
      throw e
    }
  })

  test('10.7 菜单编辑', async ({ page }) => {
    markRunning('10.7')
    try {
      const merchantId = await getMerchantId()
      const menus = await apiRequest(page, `/v1/menus/merchants/${encodeURIComponent(merchantId)}/menu`)
      const menu = menus[0]
      await page.goto('/merchant/home/menu')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const menuRow = page.locator('.menu-item', { hasText: menu.name || menu.menuName })
      await menuRow.getByRole('button', { name: '编辑' }).click()
      await page.waitForURL(/\/merchant\/home\/menu-edit/)

      await expect(page.locator('input[placeholder="请输入菜单名称"]')).toHaveValue(menu.name || menu.menuName)
      await expect(page.locator('.dish-item').first()).toBeVisible()
      markPassed('10.7', `菜单编辑页加载真实菜单 ${menu.name || menu.menuName} 及其菜品`)
    } catch (e) {
      markFailed('10.7', e.message.slice(0, 50))
      throw e
    }
  })

  test('10.8 菜品管理', async ({ page }) => {
    markRunning('10.8')
    try {
      const merchantId = await getMerchantId()
      const dishes = await apiRequest(page, `/v1/dishes?merchantId=${encodeURIComponent(merchantId)}`)
      await page.goto('/merchant/home/dish-management')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.dish-item')).toHaveCount(dishes.length)
      await expect(page.locator('.dish-item').first()).toContainText(dishes[0].name)
      markPassed('10.8', `菜品管理展示 ${dishes.length} 条真实菜品`)
    } catch (e) {
      markFailed('10.8', e.message.slice(0, 50))
      throw e
    }
  })

  test('10.9 添加菜品', async ({ page }) => {
    markRunning('10.9')
    try {
      await page.goto('/merchant/home/dish-edit')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.getByRole('heading', { name: '【菜品编辑】' })).toBeVisible()
      await expect(page.locator('input[placeholder="请输入菜品名称"]')).toBeVisible()
      await expect(page.locator('.info-label', { hasText: '价格' })).toBeVisible()
      markPassed('10.9', '菜品编辑页含名称和价格表单字段')
    } catch (e) {
      markFailed('10.9', e.message.slice(0, 50))
      throw e
    }
  })

  test('10.10 编辑菜品', async ({ page }) => {
    markRunning('10.10')
    try {
      const merchantId = await getMerchantId()
      const dishes = await apiRequest(page, `/v1/dishes?merchantId=${encodeURIComponent(merchantId)}`)
      const dish = dishes.find((item) => item.name === '皮蛋瘦肉粥') || dishes[0]
      await page.goto('/merchant/home/dish-management')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const dishRow = page.locator('.dish-item', { hasText: dish.name }).first()
      await expect(dishRow).toBeVisible()
      await dishRow.getByRole('button', { name: '编辑' }).click()
      const dialog = page.getByRole('dialog', { name: '编辑菜品' })
      await expect(dialog).toBeVisible()
      await expect(dialog.locator('input[placeholder="例：宫保鸡丁"]')).toHaveValue(dish.name)
      await expect(dialog.locator('input[placeholder="请输入价格"]')).toHaveValue(String(dish.price))
      markPassed('10.10', `编辑弹窗加载真实菜品 ${dish.name}`)
    } catch (e) {
      markFailed('10.10', e.message.slice(0, 50))
      throw e
    }
  })

  test('10.12 删除订单后查询与刷新均不再出现', async ({ page }) => {
    markRunning('10.12')
    const merchantId = await getMerchantId()
    let orderId
    try {
      const temporary = await createTemporaryOrder(page, merchantId)
      orderId = temporary.orderId
      // 用订单号把列表收敛到目标记录，避免在共享库累积的整张长表里渲染/定位
      await page.goto(`/merchant/home/orders?search=${encodeURIComponent(orderId)}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const row = page.locator('.order-item', { hasText: orderId })
      await expect(row).toBeVisible()
      await row.locator('.more-btn').click()
      await page.locator('.el-dropdown-menu:visible').getByText('删除订单', { exact: true }).click()
      await page.locator('.el-message-box').getByRole('button', { name: '确定删除' }).click()
      await expect(page.locator('.el-message').filter({ hasText: '订单已删除' })).toBeVisible()

      await expect.poll(async () => {
        const orders = await apiRequest(page, `/v1/orders/merchant/${encodeURIComponent(merchantId)}?today=false`)
        return orders.some((order) => order.id === orderId)
      }).toBe(false)

      await page.reload()
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.order-item', { hasText: orderId })).toHaveCount(0)
      markPassed('10.12', `临时订单 ${orderId} 删除后 API 查询与刷新均不存在`)
      orderId = null
    } catch (e) {
      markFailed('10.12', e.message.slice(0, 50))
      throw e
    } finally {
      if (orderId) {
        await apiRequest(page, `/v1/orders/${encodeURIComponent(orderId)}?merchantId=${encodeURIComponent(merchantId)}`, {
          method: 'DELETE'
        }).catch(() => null)
      }
    }
  })

  test('10.13 批量删除菜单后查询与刷新均不再出现', async ({ page }) => {
    markRunning('10.13')
    const merchantId = await getMerchantId()
    const dishes = await apiRequest(page, `/v1/dishes?merchantId=${encodeURIComponent(merchantId)}`)
    const dish = dishes?.[0]
    expect(dish, '商家没有可用于创建临时菜单的菜品').toBeTruthy()
    let menuId
    let menuName
    try {
      const menu = await createTemporaryMenu(page, merchantId, dish.id)
      menuId = menu.id
      menuName = menu.name
      await page.goto('/merchant/home/menu')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const row = page.locator('.menu-item', { hasText: menuName })
      await expect(row).toBeVisible()
      await row.locator('.el-checkbox').click()
      await page.getByRole('button', { name: '批量删除' }).click()
      await page.locator('.el-message-box').getByRole('button', { name: '确定删除' }).click()
      await expect(page.locator('.el-message').filter({ hasText: '批量删除成功' })).toBeVisible()

      await expect.poll(async () => {
        const menus = await apiRequest(page, `/v1/menus/merchants/${encodeURIComponent(merchantId)}/menu`)
        return menus.some((item) => item.id === menuId)
      }).toBe(false)

      await page.reload()
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.locator('.menu-item', { hasText: menuName })).toHaveCount(0)
      markPassed('10.13', `临时菜单 ${menuName} 批量删除后 API 查询与刷新均不存在`)
      menuId = null
    } catch (e) {
      markFailed('10.13', e.message.slice(0, 50))
      throw e
    } finally {
      if (menuId) {
        await apiRequest(page, '/v1/menus/menu/batch', {
          method: 'POST',
          body: JSON.stringify({ menuIds: [menuId], action: 'delete', merchantId })
        }).catch(() => null)
      }
    }
  })

  test('10.11 菜品上下架', async ({ page }) => {
    markRunning('10.11')
    const merchantId = await getMerchantId()
    const menus = await apiRequest(page, `/v1/menus/merchants/${encodeURIComponent(merchantId)}/menu`)
    const linkedDishIds = new Set(
      menus.flatMap((menu) => (menu.dishes || []).map((dish) => String(dish.id)))
    )
    const dishes = await apiRequest(page, `/v1/dishes?merchantId=${encodeURIComponent(merchantId)}`)
    const dish = dishes.find((item) => !linkedDishIds.has(String(item.id)))
    expect(dish, '找不到未关联菜单的菜品，无法安全验证上下架').toBeTruthy()
    const originalStatus = dish.status
    try {
      await page.goto('/merchant/home/dish-management')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const dishRow = page.locator('.dish-item', { hasText: dish.name }).first()
      const actionName = originalStatus ? '下架' : '上架'
      await expect(dishRow).toBeVisible()
      const updateResponse = page.waitForResponse((response) =>
        response.url().includes(`/api/v1/dishes/${dish.id}/status`) &&
        response.request().method() === 'PUT'
      )
      await dishRow.getByRole('button', { name: actionName, exact: true }).click()
      await page.locator('.el-message-box').getByRole('button', { name: '确定' }).click()
      expect((await updateResponse).ok()).toBe(true)

      await expect.poll(async () => {
        const dishes = await apiRequest(page, `/v1/dishes?merchantId=${encodeURIComponent(merchantId)}`)
        return dishes.find((item) => String(item.id) === String(dish.id))?.status
      }).toBe(!originalStatus)
      await expect(dishRow.getByText(originalStatus ? '下架' : '上架', { exact: true })).toBeVisible()
      markPassed('10.11', `菜品 ${dish.name} 从${originalStatus ? '上架' : '下架'}切换为${originalStatus ? '下架' : '上架'}`)
    } catch (e) {
      markFailed('10.11', e.message.slice(0, 50))
      throw e
    } finally {
      await apiRequest(page, `/v1/dishes/${encodeURIComponent(dish.id)}/status`, {
        method: 'PUT',
        body: JSON.stringify({ status: originalStatus })
      })
    }
  })

  test('10.14 商家会话已读持久化', async ({ page }) => {
    markRunning('10.14')
    const merchantId = await getMerchantId()
    const sessionId = `Se2e_read_${Date.now()}`
    try {
      await apiRequest(page, '/v1/chat/sessions', {
        method: 'POST',
        body: JSON.stringify({
          userId: String(merchantId),
          sessionId,
          sessionType: 'single',
          sessionName: 'E2E已读会话',
          memberCount: 0,
          targetId: `e2e_missing_${Date.now()}`
        })
      })
      await apiRequest(page, `/v1/chat/sessions/${sessionId}/unread-clear`, {
        method: 'POST',
        body: JSON.stringify({ userId: String(merchantId) })
      })
      await apiRequest(page, `/v1/chat/sessions/${sessionId}/unread-increment`, {
        method: 'POST',
        body: JSON.stringify({ userId: String(merchantId) })
      })
      expect((await getChatSession(page, merchantId, sessionId)).unreadCount).toBe(1)

      await page.goto('/merchant/home/chat')
      const conversation = page.locator('.conversation-item').filter({ hasText: 'E2E已读会话' }).first()
      await expect(conversation).toBeVisible({ timeout: 10000 })
      await conversation.click()
      await expect(page.locator('.el-message').filter({ hasText: '消息已标记为已读' })).toBeVisible({ timeout: 10000 })

      await expect.poll(async () => (await getChatSession(page, merchantId, sessionId)).unreadCount).toBe(0)
      await page.reload()
      await expect(page.locator('.conversation-item').filter({ hasText: 'E2E已读会话' }).first().locator('.unread-badge')).toHaveCount(0, { timeout: 10000 })
      markPassed('10.14', `商家会话 ${sessionId} 已读状态刷新后仍为已读`)
    } catch (e) {
      markFailed('10.14', e.message.slice(0, 50))
      throw e
    } finally {
      await apiRequest(page, `/v1/chat/sessions/${sessionId}?userId=${encodeURIComponent(merchantId)}`, {
        method: 'DELETE'
      }).catch(() => null)
    }
  })

  test('10.15 相册删除需本人或管理员（#573）', async ({ page }) => {
    markRunning('10.15')
    const merchantId = await getMerchantId()
    try {
      // 商家账号 token 删除自己的相册，应越过归属判定（不再 403 拦住），进入查库流程
      const token = await page.evaluate(() => localStorage.getItem('auth_token') || localStorage.getItem('token'))
      const response = await fetch(
        `${TEST_API_BASE}/v1/merchant/${encodeURIComponent(merchantId)}/album?imageUrl=${encodeURIComponent('https://cdn.example.com/album/1.jpg')}&albumType=environment`,
        { method: 'DELETE', headers: token ? { Authorization: `Bearer ${token}` } : {} }
      )
      const body = await response.json().catch(() => null)
      expect(String(body?.code)).not.toBe('403')
      expect(String(body?.code)).not.toBe('401')
      markPassed('10.15', `商家删除本人相册未被归属判定拦截（code=${body?.code}）`)
    } catch (e) {
      markFailed('10.15', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => {
    updateSummary()
  })
})
