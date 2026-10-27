const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

test.describe('模块七：聊天与消息', () => {

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
