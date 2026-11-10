const { test, expect, loginAsUser } = require('../fixtures/test-base')
const { getUser, getUserId } = require('../utils/real-data')
const { TEST_API_BASE } = require('../utils/api')
const {
  markRunning,
  markPassed,
  markFailed,
  updateSummary
} = require('../utils/progress')

test.describe('模块九：个人中心与教程', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsUser(page)
  })

  test('9.1 个人信息', async ({ page }) => {
    markRunning('9.1')
    try {
      const user = await getUser()
      const userId = await getUserId()
      const statsBody = await (
        await fetch(`${TEST_API_BASE}/v1/orders/user/${encodeURIComponent(userId)}/statistics`)
      ).json()
      expect(statsBody.success).toBe(true)
      await page.goto('/user/home/profile')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.user-name')).toHaveText(user.nickname)
      await expect(page.getByText(user.phone, { exact: true })).toBeVisible()
      await expect(page.getByText(`${statsBody.data.inProgress}笔`).first()).toBeVisible()
      markPassed('9.1', `展示用户 ${user.nickname} 的真实资料`)
    } catch (e) {
      markFailed('9.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('9.2 头像上传', async ({ page }) => {
    markRunning('9.2')
    try {
      const user = await getUser()
      const avatarResponse = await fetch(new URL(user.avatar, TEST_API_BASE))
      expect(avatarResponse.ok).toBe(true)
      expect(avatarResponse.headers.get('content-type')).toMatch(/^image\//)
      expect((await avatarResponse.arrayBuffer()).byteLength).toBeGreaterThan(0)

      await page.goto('/user/home/profile')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.getByRole('button', { name: '更换头像' })).toBeVisible()
      const avatarImage = page.locator('.avatar-wrapper .el-avatar img')
      await expect(avatarImage).toHaveAttribute('src', new RegExp(user.avatar.split('/').pop()))
      await expect.poll(() => avatarImage.evaluate((img) => img.complete && img.naturalWidth > 0)).toBe(true)
      markPassed('9.2', `头像 ${user.avatar.split('/').pop()} 返回图片并成功展示`)
    } catch (e) {
      markFailed('9.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('9.3 修改密码', async ({ page }) => {
    markRunning('9.3')
    try {
      await page.goto('/user/home/settings')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await page.locator('.form-label', { hasText: '密码' }).locator('..').getByRole('button', { name: '修改' }).click()
      const dialog = page.getByRole('dialog', { name: '修改密码' })
      await expect(dialog).toBeVisible()
      await expect(dialog.getByPlaceholder('请输入旧密码')).toBeVisible()
      await expect(dialog.getByPlaceholder('请输入新密码')).toBeVisible()
      await expect(dialog.getByPlaceholder('请确认新密码')).toBeVisible()
      markPassed('9.3', '修改密码弹窗含旧密码、新密码和确认密码字段')
    } catch (e) {
      markFailed('9.3', e.message.slice(0, 50))
      throw e
    }
  })

  test('9.4 饮食偏好', async ({ page }) => {
    markRunning('9.4')
    try {
      const user = await getUser()
      await page.goto('/user/home/profile')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await page.getByRole('button', { name: '编辑资料' }).click()

      const dialog = page.getByRole('dialog')
      await expect(dialog.locator(`input[placeholder="给自己取个名字吧"]`)).toHaveValue(user.nickname)
      await expect(dialog.locator(`input[placeholder="example@email.com"]`)).toHaveValue(user.email)
      // 地区字段有两种渲染：级联数据就绪时是 el-cascader（有选中值会清空内部
      // input 的 placeholder，不能按 placeholder 定位，见 #162）；高德数据不可用时
      // 退回只读 el-input。CI 没有高德 key，两种形态都要能断到真实回填的所在地。
      const locationInput = dialog
        .locator('.el-cascader input, input[placeholder="选择省 / 市 / 区"]')
        .first()
      const locationValue = await locationInput.inputValue()
      for (const part of user.location.split(' ')) {
        expect(locationValue).toContain(part)
      }
      await expect(dialog.getByText(user.preferTags.priceRange, { exact: true })).toBeVisible()
      await expect(dialog.getByText(user.preferTags.nutritionNeeds, { exact: true })).toBeVisible()
      markPassed('9.4', `回填 ${user.nickname} 的邮箱与饮食偏好`)
    } catch (e) {
      markFailed('9.4', e.message.slice(0, 50))
      throw e
    }
  })

  test('9.5 消费记录', async ({ page }) => {
    markRunning('9.5')
    try {
      const userId = await getUserId()
      const user = await getUser({ userId })
      const records = await (await fetch(
        `${TEST_API_BASE}/v1/consume-history?userId=${encodeURIComponent(userId)}&type=all&page=1&pageSize=10`
      )).json()
      expect(records.success).toBe(true)
      await page.goto('/user/home/consume-history')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const balanceText = Number(user.wallet.balance || 0).toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      })
      await expect(page.locator('.app-stat-card').first()).toContainText(`¥${balanceText}`)
      await expect(page.locator('.consume-history-page')).toContainText(
        records.data.records[0].description
      )
      markPassed('9.5', `余额展示为 ${user.wallet.balance}，流水首条与接口一致`)
    } catch (e) {
      markFailed('9.5', e.message.slice(0, 50))
      throw e
    }
  })

  test('9.6 教程列表', async ({ page }) => {
    markRunning('9.6')
    try {
      const response = await fetch(`${TEST_API_BASE}/v1/tutorial/list`)
      const tutorials = await response.json()
      expect(Array.isArray(tutorials)).toBe(true)
      expect(tutorials.length).toBeGreaterThan(0)

      await page.goto('/user/home/tutorials')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      const cards = page.locator('.tutorial-card')
      await expect(cards).toHaveCount(tutorials.length)
      await expect(cards.first().locator('.tutorial-title')).toHaveText(tutorials[0].title)
      markPassed('9.6', `教程列表展示接口返回的 ${tutorials.length} 条真实教程`)
    } catch (e) {
      markFailed('9.6', e.message.slice(0, 50))
      throw e
    }
  })

  test('9.7 教程详情', async ({ page }) => {
    markRunning('9.7')
    try {
      const tutorials = await (await fetch(`${TEST_API_BASE}/v1/tutorial/list`)).json()
      const tutorial = tutorials[0]
      await page.goto(`/user/home/tutorials/${tutorial.id}`)
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.locator('.tutorial-title')).toHaveText(tutorial.title)
      await expect(page.locator('.content-text')).toContainText(tutorial.content.replace(/[#*]/g, '').split('\n')[1] || tutorial.title)
      await expect(page.locator('body')).not.toContainText('教程不存在')
      markPassed('9.7', `详情展示教程 ${tutorial.title} 的真实内容`)
    } catch (e) {
      markFailed('9.7', e.message.slice(0, 50))
      throw e
    }
  })

  test('9.8 发布教程', async ({ page }) => {
    markRunning('9.8')
    try {
      await page.goto('/user/home/tutorials/publish')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      await expect(page.getByRole('heading', { name: '发布教程' })).toBeVisible()
      await expect(page.locator('input[placeholder*="给教程起个好标题"]')).toBeVisible()
      await expect(page.getByText('教程类型', { exact: true })).toBeVisible()
      await expect(page.getByText('教程内容', { exact: true })).toBeVisible()
      markPassed('9.8', '发布教程页含标题、类型和内容表单')
    } catch (e) {
      markFailed('9.8', e.message.slice(0, 50))
      throw e
    }
  })

  test('9.9 我的教程', async ({ page }) => {
    markRunning('9.9')
    try {
      const userId = await getUserId()
      const response = await fetch(
        `${TEST_API_BASE}/v1/tutorial/user/my?userId=${encodeURIComponent(userId)}`
      )
      const body = await response.json()
      const tutorials = Array.isArray(body) ? body : body.data || []

      await page.goto('/user/home/tutorials/my')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)
      await expect(page.getByRole('heading', { name: '我的教程' })).toBeVisible()
      await expect(page.getByRole('button', { name: '发布新教程' })).toBeVisible()
      if (tutorials.length > 0) {
        await expect(page.locator('.tutorial-card').first()).toContainText(tutorials[0].title)
      }
      markPassed('9.9', tutorials.length > 0
        ? `我的教程展示 ${tutorials.length} 条真实记录`
        : '当前用户暂无教程，页面展示真实空态和发布入口')
    } catch (e) {
      markFailed('9.9', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => {
    updateSummary()
  })
})
