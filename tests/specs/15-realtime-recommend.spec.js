const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')
const { TEST_API_BASE } = require('../utils/api')
const { createEphemeralTask, deleteEphemeralTask } = require('../utils/real-data')

test.describe('模块十五：实时推荐推送', () => {

  test('15.1 用户端共享布局建立 WebSocket 连接', async ({ page }) => {
    markRunning('15.1')
    const socketUrls = []
    page.on('websocket', (ws) => socketUrls.push(ws.url()))

    try {
      await loginAsUser(page)

      // 页面加载时 vite 自身的 HMR 连接会先出现，这里只认带身份的 /ws 业务连接
      await expect.poll(
        () => socketUrls.find((u) => /\/ws\?/.test(u) && u.includes('userId=')),
        { message: '用户端共享布局应建立带身份的 /ws 业务连接', timeout: 20000 }
      ).toBeTruthy()

      const businessSocket = socketUrls.find((u) => /\/ws\?/.test(u) && u.includes('userId='))
      markPassed('15.1', `用户端布局建立业务连接：${businessSocket.replace(/token=[^&]+/, 'token=***')}`)
    } catch (e) {
      markFailed('15.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('15.2 后端真实推送到达时显示可点击提示并跳转推荐页', async ({ page, request }) => {
    markRunning('15.2')
    const taskId = `e2e-realtime-${Date.now()}`
    let registered = false
    const socketUrls = []
    page.on('websocket', (ws) => socketUrls.push(ws.url()))

    try {
      await loginAsUser(page)

      // 推送需要接收方：先确认带身份的业务连接真的建立了，
      // 否则后端会因无人在线而跳过，用例就退化成空验证。
      const isBusinessSocket = (u) => /\/ws\?/.test(u) && u.includes('userId=')
      await expect.poll(
        () => socketUrls.some(isBusinessSocket),
        { message: '推送前应已建立带身份的业务连接', timeout: 20000 }
      ).toBeTruthy()

      // 登记一个不会自动调度的临时任务，指向后端真实的定时推荐方法
      createEphemeralTask({
        id: taskId,
        taskName: 'e2e-实时推荐推送',
        className: 'com.xx.jaseatschoicejava.service.impl.RealtimeRecommendationServiceImpl',
        methodName: 'scheduleRecommendationPush'
      })
      registered = true

      // 触发真实链路：后端反射调用定时推荐方法 → 生成推荐 → 推给在线连接
      const executed = await request.post(`${TEST_API_BASE}/v1/scheduled-tasks/${taskId}/execute`)
      expect(executed.ok(), '执行临时任务接口应返回成功').toBeTruthy()

      const notice = page.locator('.el-notification')
      await expect(notice.first()).toBeVisible({ timeout: 20000 })
      await expect(notice.first()).toContainText('为你推荐')

      // 提示应可点击并进入推荐页
      await notice.first().click()
      await expect(page).toHaveURL(/\/user\/home\/recommend/, { timeout: 15000 })

      markPassed('15.2', '后端真实推送到达、显示提示且点击进入推荐页')
    } catch (e) {
      markFailed('15.2', e.message.slice(0, 50))
      throw e
    } finally {
      if (registered) {
        try {
          deleteEphemeralTask(taskId)
        } catch (cleanupError) {
          console.warn('清理临时任务失败:', cleanupError.message)
        }
      }
    }
  })

  test('15.3 多个标签页各自保持独立连接', async ({ browser }) => {
    markRunning('15.3')
    const context = await browser.newContext()
    const firstTab = await context.newPage()
    const secondTab = await context.newPage()
    const firstUrls = []
    const secondUrls = []
    firstTab.on('websocket', (ws) => firstUrls.push(ws.url()))
    secondTab.on('websocket', (ws) => secondUrls.push(ws.url()))

    try {
      await loginAsUser(firstTab)
      await secondTab.goto('/user/home')

      const isBusinessSocket = (u) => /\/ws\?/.test(u) && u.includes('userId=')
      await expect.poll(() => firstUrls.some(isBusinessSocket), { timeout: 20000 }).toBeTruthy()
      await expect.poll(() => secondUrls.some(isBusinessSocket), { timeout: 20000 }).toBeTruthy()

      markPassed('15.3', '两个标签页各自建立了业务连接')
    } catch (e) {
      markFailed('15.3', e.message.slice(0, 50))
      throw e
    } finally {
      await context.close()
    }
  })

  test.afterAll(() => { updateSummary() })
})
