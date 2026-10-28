const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')
const { TEST_API_BASE } = require('../utils/api')

test.describe('模块七：聊天与消息', () => {

async function apiRequest(page, request, path, options = {}) {
  const token = await page.evaluate(() => localStorage.getItem('auth_token') || localStorage.getItem('token') || '')
  const { body, ...requestOptions } = options
  const response = await request.fetch(`${TEST_API_BASE.replace(/\/$/, '')}${path}`, {
    ...requestOptions,
    data: body,
    headers: {
      Accept: 'application/json',
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  })
  const responseBody = await response.json().catch(() => null)
  if (!response.ok() || !responseBody?.success) {
    throw new Error(`请求 ${path} 失败（${response.status()}）：${responseBody?.message || '无响应'}`)
  }
  return responseBody.data
}

async function getConversation(page, request, userId, sessionId) {
  const conversations = await apiRequest(page, request, `/v1/chat/users/${encodeURIComponent(userId)}/chat-sessions`)
  const conversation = conversations.find(item => item.id === sessionId)
  if (!conversation) throw new Error(`会话 ${sessionId} 不存在`)
  return conversation
}

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('7.1 聊天会话', async ({ page }) => {
    markRunning('7.1')
    try {
      await page.goto('/user/home/chat')
      await expect(page.locator('.conversation-list')).toBeVisible({ timeout: 10000 })
      const firstConversation = page.locator('.conversation-item').first()
      await expect(firstConversation).toBeVisible({ timeout: 10000 })
      await expect(firstConversation.locator('.name')).not.toBeEmpty()
      markPassed('7.1', '会话列表加载真实会话名称')
    } catch (e) { markFailed('7.1', e.message.slice(0, 50)); throw e }
  })

  test('7.2 发送消息', async ({ page }) => {
    markRunning('7.2')
    try {
      await page.goto('/user/home/chat')
      const firstConversation = page.locator('.conversation-item').first()
      await expect(firstConversation).toBeVisible({ timeout: 10000 })
      const conversationName = (await firstConversation.locator('.name').innerText()).trim()
      await firstConversation.click()

      const header = page.locator('.chat-area-header')
      await expect(header.locator('.name')).toHaveText(conversationName, { timeout: 10000 })

      const message = `E2E 聊天消息 ${Date.now()}`
      const input = page.locator('.message-input-container textarea').first()
      await expect(input).toBeEnabled({ timeout: 10000 })
      await input.fill(message)
      await page.locator('.message-input-container .send-button').click()

      const sentMessage = page.locator('.message-item.my-message', { hasText: message }).first()
      await expect(sentMessage).toBeVisible({ timeout: 10000 })
      await expect(sentMessage.locator('.message-status.failed')).toHaveCount(0)
      const currentConversation = page.locator('.conversation-item').filter({ hasText: conversationName }).first()
      await expect(currentConversation.locator('.last-message')).toContainText(message, { timeout: 10000 })
      markPassed('7.2', '真实消息发送后出现在当前会话')
    } catch (e) { markFailed('7.2', e.message.slice(0, 50)); throw e }
  })

  test('7.6 会话置顶持久化', async ({ page, request }) => {
    markRunning('7.6')
    const userId = await page.evaluate(() => localStorage.getItem('auth_userId') || localStorage.getItem('userId'))
    const sessionId = `Se2e_pin_${Date.now()}`
    try {
      await apiRequest(page, request, '/v1/chat/sessions', {
        method: 'POST',
        body: JSON.stringify({
          userId: String(userId),
          sessionId,
          sessionType: 'single',
          sessionName: 'E2E置顶会话',
          avatar: '💬',
          memberCount: 0,
          targetId: `e2e_pin_target_${Date.now()}`
        })
      })

      await page.goto('/user/home/chat')
      const targetConversation = page.locator('.conversation-item').filter({ hasText: 'E2E置顶会话' }).first()
      await expect(targetConversation).toBeVisible({ timeout: 10000 })
      const pinResponsePromise = page.waitForResponse((response) =>
        response.url().includes(`/v1/chat/sessions/${sessionId}/toggle-pin`) &&
        response.request().method() === 'POST'
      )
      await targetConversation.dispatchEvent('contextmenu')
      await page.locator('.context-menu .menu-item').filter({ hasText: '置顶会话' }).click()
      const pinResponse = await pinResponsePromise
      const pinBody = await pinResponse.json()
      expect(pinResponse.ok(), `置顶接口应返回成功：${pinBody.message || ''}`).toBeTruthy()
      expect(pinBody.success, `置顶接口不应返回失败：${pinBody.message || ''}`).toBe(true)
      expect(pinBody.data).toBe(true)

      await expect.poll(async () => (await getConversation(page, request, userId, sessionId)).pinned).toBe(true)
      await page.reload()
      await expect.poll(async () => (await getConversation(page, request, userId, sessionId)).pinned).toBe(true)
      markPassed('7.6', `临时会话 ${sessionId} 置顶并重新查询后仍为 true`)
    } catch (e) { markFailed('7.6', e.message.slice(0, 50)); throw e }
    finally {
      await apiRequest(page, request, `/v1/chat/sessions/${sessionId}?userId=${encodeURIComponent(userId)}`, {
        method: 'DELETE'
      }).catch(() => null)
    }
  })

  test('7.3 消息通知', async ({ page }) => {
    markRunning('7.3')
    try {
      await page.goto('/user/home/message-center')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const notification = page.locator('[class*="message"], [class*="notification"], .el-card')
      await expect(notification.first()).toBeVisible({ timeout: 10000 })
      markPassed('7.3', '消息中心页面可访问')
    } catch (e) { markFailed('7.3', e.message.slice(0, 50)); throw e }
  })

  test('7.4 消息中心', async ({ page }) => {
    markRunning('7.4')
    try {
      await page.goto('/user/home/system-notification')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const sysNotif = page.locator('[class*="notification"], [class*="system"], .el-card')
      await expect(sysNotif.first()).toBeVisible({ timeout: 10000 })
      markPassed('7.4', '系统通知页面可访问')
    } catch (e) { markFailed('7.4', e.message.slice(0, 50)); throw e }
  })

  test('7.5 通讯录', async ({ page }) => {
    markRunning('7.5')
    try {
      await page.goto('/user/home/contacts')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const contacts = page.locator('[class*="contact"], [class*="address-book"], .el-card')
      await expect(contacts.first()).toBeVisible({ timeout: 10000 })
      markPassed('7.5', '通讯录页面可访问')
    } catch (e) { markFailed('7.5', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
