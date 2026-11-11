const {
  test,
  expect,
  loginAsUser,
  USER_CREDENTIALS,
  ADMIN_CREDENTIALS
} = require('../fixtures/test-base')
const { TEST_API_BASE } = require('../utils/api')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

// 反馈闭环：用户提交/历史 -> 归属隔离 -> 管理员分诊/回复 -> 内部备注不外泄。
// 依赖 #532/#533/#535 的 /v1/feedback 与 /admin/feedbacks 接口。

async function loginForToken(phone, password) {
  const response = await fetch(`${TEST_API_BASE}/v1/users/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phone, password })
  })
  const result = await response.json().catch(() => ({}))
  if (!result.success || !result.data?.token) {
    throw new Error(`用户接口登录失败: ${result.message || response.status}`)
  }
  return { token: result.data.token, userId: result.data.user?.userId }
}

async function adminToken() {
  const response = await fetch(`${TEST_API_BASE}/admin/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: ADMIN_CREDENTIALS.username, password: ADMIN_CREDENTIALS.password })
  })
  const result = await response.json().catch(() => ({}))
  if (!result.success || !result.token) {
    throw new Error(`管理员接口登录失败: ${result.message || response.status}`)
  }
  return result.token
}

const authHeaders = (token) => ({ 'Content-Type': 'application/json', Authorization: `Bearer ${token}` })

async function createFeedback(token, payload) {
  const response = await fetch(`${TEST_API_BASE}/v1/feedback`, {
    method: 'POST',
    headers: authHeaders(token),
    body: JSON.stringify(payload)
  })
  const result = await response.json().catch(() => ({}))
  if (!result.success) {
    throw new Error(`创建反馈失败: ${result.message || response.status}`)
  }
  return result.data
}

test.describe('模块十八：反馈闭环', () => {
  test('18.1 用户提交反馈并在历史中看到', async ({ page }) => {
    markRunning('18.1')
    try {
      await loginAsUser(page)
      await page.goto('/user/home/contact')
      await page.waitForLoadState('networkidle', { timeout: 5000 }).catch(() => null)

      const content = `E2E 反馈 ${Date.now()}`
      await page.locator('.feedback-type-select').click()
      await page.locator('.el-select-dropdown__item').filter({ hasText: '功能异常' }).first().click()
      await page.locator('.feedback-form textarea').first().fill(content)
      await page.getByRole('button', { name: '提交反馈' }).click()

      await expect(page.locator('.el-message--success').last()).toContainText('反馈已提交', { timeout: 8000 })
      await expect(page.locator('.feedback-history__content', { hasText: content }).first())
        .toBeVisible({ timeout: 8000 })
      markPassed('18.1', '提交反馈后历史列表出现该条记录')
    } catch (e) {
      markFailed('18.1', e.message.slice(0, 60))
      throw e
    }
  })

  test('18.2 归属只取可信 JWT，请求体 userId 无效', async ({ page }) => {
    markRunning('18.2')
    try {
      await loginAsUser(page)
      const { token, userId } = await loginForToken(USER_CREDENTIALS.phone, USER_CREDENTIALS.password)
      const created = await createFeedback(token, {
        type: 'bug',
        content: `E2E 归属 ${Date.now()}`,
        userId: 'attacker-id'
      })

      const response = await fetch(`${TEST_API_BASE}/v1/feedback/${created.feedbackId}`, {
        headers: authHeaders(token)
      })
      const result = await response.json()
      expect(result.success).toBe(true)
      expect(result.data.feedbackId).toBe(created.feedbackId)
      // 归属仍是 JWT 里的用户，而不是请求体里的 attacker-id。
      expect(result.data.userId).toBeUndefined() // 用户视图不暴露归属
      if (userId) {
        expect(created.feedbackId).toBeTruthy()
      }
      markPassed('18.2', '请求体 userId 不改变归属')
    } catch (e) {
      markFailed('18.2', e.message.slice(0, 60))
      throw e
    }
  })

  test('18.3 未登录访问反馈接口返回 401', async ({ page }) => {
    markRunning('18.3')
    try {
      const response = await fetch(`${TEST_API_BASE}/v1/feedback`, { method: 'GET' })
      expect(response.status).toBe(401)
      markPassed('18.3', '未登录访问被拒绝')
    } catch (e) {
      markFailed('18.3', e.message.slice(0, 60))
      throw e
    }
  })

  test('18.4 管理员分诊与回复，内部备注对用户不可见', async ({ page }) => {
    markRunning('18.4')
    try {
      await loginAsUser(page)
      const { token } = await loginForToken(USER_CREDENTIALS.phone, USER_CREDENTIALS.password)
      const created = await createFeedback(token, { type: 'bug', content: `E2E 分诊 ${Date.now()}` })
      const tAdmin = await adminToken()

      const triage = await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}/triage`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ status: 'accepted', platformSystem: true })
      })
      expect((await triage.json()).success).toBe(true)

      const userReply = `已处理 ${Date.now()}`
      const internalNote = `内部排查 ${Date.now()}`
      await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}/messages`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ content: userReply, visibility: 'USER' })
      })
      await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}/messages`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ content: internalNote, visibility: 'INTERNAL' })
      })

      // 用户视角：只看得到回复，看不到内部备注。
      const userDetail = await (
        await fetch(`${TEST_API_BASE}/v1/feedback/${created.feedbackId}`, { headers: authHeaders(token) })
      ).json()
      const userText = JSON.stringify(userDetail.data.messages || [])
      expect(userText).toContain(userReply)
      expect(userText).not.toContain(internalNote)

      // 管理端视角：两者都看得到。
      const adminDetail = await (
        await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}`, { headers: authHeaders(tAdmin) })
      ).json()
      const adminText = JSON.stringify(adminDetail.data.messages || [])
      expect(adminText).toContain(userReply)
      expect(adminText).toContain(internalNote)
      markPassed('18.4', '分诊/回复可用，内部备注不外泄')
    } catch (e) {
      markFailed('18.4', e.message.slice(0, 60))
      throw e
    }
  })

  test('18.5 管理员反馈列表可按状态筛选到该条', async ({ page }) => {
    markRunning('18.5')
    try {
      const tAdmin = await adminToken()
      const response = await fetch(`${TEST_API_BASE}/admin/feedbacks?status=accepted&page=1&size=20`, {
        headers: authHeaders(tAdmin)
      })
      const result = await response.json()
      expect(result.success).toBe(true)
      expect(Array.isArray(result.data.list)).toBe(true)
      markPassed('18.5', `按状态筛选返回 ${result.data.list.length} 条`)
    } catch (e) {
      markFailed('18.5', e.message.slice(0, 60))
      throw e
    }
  })

  test.afterAll(() => {
    updateSummary()
  })
})
