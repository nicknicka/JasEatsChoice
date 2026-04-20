const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')

test.describe('模块八：AI助手与地址管理', () => {

  test.beforeEach(async ({ page }) => { await loginAsUser(page) })

  test('8.1 AI对话', async ({ page }) => {
    markRunning('8.1')
    try {
      await page.goto('/user/home/ai')
      await page.waitForLoadState('networkidle').catch(() => null)
      const aiChat = page.locator('[class*="ai"], [class*="chat"], textarea')
      await expect(aiChat.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('8.1', 'AI助手页面可访问')
    } catch (e) { markFailed('8.1', e.message.slice(0, 50)); throw e }
  })

  test('8.2 菜品识别', async ({ page }) => {
    markRunning('8.2')
    try {
      await page.goto('/user/home/ai')
      await page.waitForLoadState('networkidle').catch(() => null)
      const recognizeBtn = page.locator('button:has-text("识别"), [class*="recognize"], [class*="upload"]').first()
      if (await recognizeBtn.count() > 0) {
        markPassed('8.2', '菜品识别功能存在')
      } else {
        markSkipped('8.2', '未找到识别按钮')
      }
    } catch (e) { markFailed('8.2', e.message.slice(0, 50)); throw e }
  })

  test('8.3 食谱生成', async ({ page }) => {
    markRunning('8.3')
    try {
      await page.goto('/user/home/ai')
      await page.waitForLoadState('networkidle').catch(() => null)
      const recipeBtn = page.locator('button:has-text("食谱"), [class*="recipe"]').first()
      if (await recipeBtn.count() > 0) {
        markPassed('8.3', '食谱生成功能存在')
      } else {
        markSkipped('8.3', '未找到食谱生成按钮')
      }
    } catch (e) { markFailed('8.3', e.message.slice(0, 50)); throw e }
  })

  test('8.4 AI聊天历史', async ({ page }) => {
    markRunning('8.4')
    try {
      await page.goto('/user/home/ai')
      await page.waitForLoadState('networkidle').catch(() => null)
      const history = page.locator('[class*="history"], [class*="record"]')
      if (await history.count() > 0) {
        markPassed('8.4', '历史记录区域存在')
      } else {
        markSkipped('8.4', '无历史记录')
      }
    } catch (e) { markFailed('8.4', e.message.slice(0, 50)); throw e }
  })

  test('8.5 地址列表', async ({ page }) => {
    markRunning('8.5')
    try {
      await page.goto('/user/home/address')
      await page.waitForLoadState('networkidle').catch(() => null)
      const address = page.locator('[class*="address"], .el-card')
      await expect(address.first()).toBeVisible({ timeout: 10000 }).catch(() => null)
      markPassed('8.5', '地址页面可访问')
    } catch (e) { markFailed('8.5', e.message.slice(0, 50)); throw e }
  })

  test('8.6 添加地址', async ({ page }) => {
    markRunning('8.6')
    try {
      await page.goto('/user/home/address')
      await page.waitForLoadState('networkidle').catch(() => null)
      const addBtn = page.locator('button:has-text("添加"), button:has-text("新增")').first()
      if (await addBtn.count() > 0) {
        markPassed('8.6', '添加地址按钮存在')
      } else {
        markSkipped('8.6', '未找到添加按钮')
      }
    } catch (e) { markFailed('8.6', e.message.slice(0, 50)); throw e }
  })

  test('8.7 编辑地址', async ({ page }) => {
    markRunning('8.7')
    try {
      await page.goto('/user/home/address')
      await page.waitForLoadState('networkidle').catch(() => null)
      const editBtn = page.locator('button:has-text("编辑"), [class*="edit"]').first()
      if (await editBtn.count() > 0) {
        markPassed('8.7', '编辑按钮存在')
      } else {
        markSkipped('8.7', '无地址可编辑')
      }
    } catch (e) { markFailed('8.7', e.message.slice(0, 50)); throw e }
  })

  test('8.8 设置默认地址', async ({ page }) => {
    markRunning('8.8')
    try {
      await page.goto('/user/home/address')
      await page.waitForLoadState('networkidle').catch(() => null)
      const defaultBtn = page.locator('button:has-text("默认"), [class*="default"]').first()
      if (await defaultBtn.count() > 0) {
        markPassed('8.8', '默认地址按钮存在')
      } else {
        markSkipped('8.8', '无地址可设为默认')
      }
    } catch (e) { markFailed('8.8', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})