const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

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

  test('15.2 推荐推送到达时显示可点击提示', async ({ page }) => {
    markRunning('15.2')
    let serverSide = null

    // 保留与真实后端的连接（connectToServer），同时允许注入一条服务端推送
    await page.routeWebSocket(/\/ws/, (ws) => {
      serverSide = ws
      const upstream = ws.connectToServer()
      upstream.onMessage((msg) => ws.send(msg))
    })

    try {
      await loginAsUser(page)

      await expect.poll(() => !!serverSide, { timeout: 15000 }).toBeTruthy()

      // 以服务端身份推送一条推荐消息
      serverSide.send(JSON.stringify({
        msgType: 'realtime_recommend',
        userId: '3384650106421960',
        context: '定时推荐 - 午餐',
        data: [{ dishId: 'demo-1' }, { dishId: 'demo-2' }]
      }))

      const notice = page.locator('.el-notification')
      await expect(notice.first()).toBeVisible({ timeout: 10000 })
      await expect(notice.first()).toContainText('为你推荐')
      await expect(notice.first()).toContainText('2 道菜')
      markPassed('15.2', '推荐推送到达后显示可点击提示')
    } catch (e) {
      markFailed('15.2', e.message.slice(0, 50))
      throw e
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
