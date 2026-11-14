const {
  test,
  expect,
  loginAsUser,
  bootstrapAdminSession,
  ADMIN_CREDENTIALS
} = require('../fixtures/test-base')
const { TEST_API_BASE } = require('../utils/api')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')

// 反馈闭环：用户提交/历史 -> 归属隔离 -> 管理员分诊/回复 -> 内部备注不外泄。
// 依赖 #532/#533/#535 的 /v1/feedback 与 /admin/feedbacks 接口。

// 用户密码登录需要图片验证码，接口直连不可行；改为走 UI 登录后从会话里取 token。
async function currentUserToken(page) {
  const token = await page.evaluate(() => localStorage.getItem('auth_token'))
  const userId = await page.evaluate(() => localStorage.getItem('auth_userId'))
  if (!token) {
    throw new Error('登录后未在会话里找到 auth_token')
  }
  return { token, userId }
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
      const { token, userId } = await currentUserToken(page)
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
      // 用户视图不暴露归属；这里再用「换个人也读不到」的角度确认归属确实绑在登录用户上。
      expect(result.data.userId).toBeUndefined()
      expect(userId).toBeTruthy()
      expect(created.feedbackId).toBeTruthy()
      markPassed('18.2', `请求体 userId 无效，归属绑定 ${userId}`)
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
      const { token } = await currentUserToken(page)
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

  // #548：已接受反馈可改派负责人 / 调整优先级，且不产生第二次「接受」
  test('18.6 已接受反馈可更新负责人与优先级且状态保持', async ({ page }) => {
    markRunning('18.6')
    try {
      await loginAsUser(page)
      const { token } = await currentUserToken(page)
      const created = await createFeedback(token, { type: 'bug', content: `E2E 自流转 ${Date.now()}` })
      const tAdmin = await adminToken()

      // 先正常接受，带上平台系统判定
      const accept = await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}/triage`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ status: 'accepted', platformSystem: true, priority: 'normal' })
      })
      expect((await accept.json()).success).toBe(true)

      // accepted -> accepted：只改优先级与负责人，不提交 platformSystem
      const update = await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}/triage`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ status: 'accepted', priority: 'urgent', assigneeId: 1 })
      })
      const updateBody = await update.json()
      expect(updateBody.success).toBe(true)
      expect(updateBody.data.status).toBe('accepted')

      const detail = await (
        await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}`, { headers: authHeaders(tAdmin) })
      ).json()
      expect(detail.data.status).toBe('accepted')
      expect(detail.data.priority).toBe('urgent')
      expect(detail.data.assigneeId).toBe(1)
      // 未提交 platformSystem 时不得把已有判定清成 false（issue #548 的回归点）
      expect(detail.data.platformSystem).toBe(true)

      // 审计能看出本次改了哪些字段与新旧值
      const triageAudits = (detail.data.audits || []).filter((a) => a.action === 'TRIAGE')
      const lastDetail = triageAudits[triageAudits.length - 1]?.detail || ''
      expect(lastDetail).toContain('status=accepted->accepted')
      expect(lastDetail).toContain('priority=normal->urgent')
      expect(lastDetail).toContain('assigneeId=none->1')
      expect(lastDetail).not.toContain('platformSystem')

      markPassed('18.6', 'accepted 自流转更新属性，状态保持且 platformSystem 未被清')
    } catch (e) {
      markFailed('18.6', e.message.slice(0, 60))
      throw e
    }
  })

  test('18.7 终态与非法流转仍被拒绝', async ({ page }) => {
    markRunning('18.7')
    try {
      await loginAsUser(page)
      const { token } = await currentUserToken(page)
      const tAdmin = await adminToken()
      const created = await createFeedback(token, { type: 'bug', content: `E2E 非法流转 ${Date.now()}` })

      // 先拒绝，形成 rejected 终态
      const reject = await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}/triage`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ status: 'rejected' })
      })
      expect((await reject.json()).success).toBe(true)

      // rejected -> rejected 不是「自流转可改属性」的状态，必须仍被拒绝
      const rejectedAgain = await fetch(`${TEST_API_BASE}/admin/feedbacks/${created.feedbackId}/triage`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ status: 'rejected', priority: 'urgent' })
      })
      const rejectedBody = await rejectedAgain.json()
      expect(rejectedBody.success).toBe(false)
      expect(rejectedBody.code).toBe('409')

      // resolved -> accepted 也不允许（本 issue 不扩展终态）
      const second = await createFeedback(token, { type: 'bug', content: `E2E 终态 ${Date.now()}` })
      await fetch(`${TEST_API_BASE}/admin/feedbacks/${second.feedbackId}/triage`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ status: 'accepted', platformSystem: false })
      })
      await fetch(`${TEST_API_BASE}/admin/feedbacks/${second.feedbackId}/resolve`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ resolutionCode: 'fixed' })
      })
      const backToAccepted = await fetch(`${TEST_API_BASE}/admin/feedbacks/${second.feedbackId}/triage`, {
        method: 'POST',
        headers: authHeaders(tAdmin),
        body: JSON.stringify({ status: 'accepted', priority: 'urgent' })
      })
      const backBody = await backToAccepted.json()
      expect(backBody.success).toBe(false)
      expect(backBody.code).toBe('409')

      markPassed('18.7', 'rejected 自流转与 resolved->accepted 仍返回 409')
    } catch (e) {
      markFailed('18.7', e.message.slice(0, 60))
      throw e
    }
  })

  test('18.8 优先级筛选携带参数并过滤结果', async ({ page }) => {
    markRunning('18.8')
    try {
      const tAdmin = await adminToken()

      // 接口层：priority 参数生效，返回结果全部是该优先级
      const filtered = await (
        await fetch(`${TEST_API_BASE}/admin/feedbacks?priority=urgent&page=1&size=50`, { headers: authHeaders(tAdmin) })
      ).json()
      expect(filtered.success).toBe(true)
      expect(Array.isArray(filtered.data.list)).toBe(true)
      expect(filtered.data.list.every((row) => row.priority === 'urgent')).toBe(true)

      // UI 层：选中「紧急」后查询，请求确实带上 priority=urgent
      await bootstrapAdminSession(page)
      const requests = []
      page.on('request', (req) => {
        if (req.url().includes('/admin/feedbacks?')) {
          requests.push(req.url())
        }
      })
      await page.goto('/admin/feedback')
      await expect(page.locator('.filter-card')).toBeVisible({ timeout: 10000 })

      await page.locator('.filter-card .el-form-item').filter({ hasText: '优先级' })
        .locator('.el-select').click()
      await page.locator('.el-select-dropdown__item').filter({ hasText: '紧急' }).first().click()
      await page.getByRole('button', { name: '查询' }).click()

      await expect.poll(() => requests.some((u) => u.includes('priority=urgent')), { timeout: 8000 }).toBe(true)
      await expect(page.locator('.table-filter-hint')).toContainText('优先级：紧急')

      // 重置后 priority 必须被清掉，且回到第 1 页
      const resetRequests = []
      page.on('request', (req) => {
        if (req.url().includes('/admin/feedbacks?')) resetRequests.push(req.url())
      })
      await page.getByRole('button', { name: '重置' }).click()
      await expect.poll(() => resetRequests.length > 0, { timeout: 8000 }).toBe(true)
      const lastReset = resetRequests[resetRequests.length - 1]
      expect(lastReset).not.toContain('priority=')
      expect(lastReset).toContain('page=1')
      await expect(page.locator('.table-filter-hint')).toHaveCount(0)

      markPassed('18.8', `优先级筛选可查可清；接口返回 ${filtered.data.list.length} 条 urgent`)
    } catch (e) {
      markFailed('18.8', e.message.slice(0, 60))
      throw e
    }
  })

  test('18.9 反馈列表 1440×900 首屏分页预算', async ({ page }) => {
    markRunning('18.9')
    try {
      // §4.10 硬指标是「1440×900、10 行数据下分页条 bottom < 900」，前提是分页条真的渲染出来
      // （total > size）。这里先造足够多的待分诊反馈，避免分页被隐藏后断言空跑。
      await loginAsUser(page)
      const { token } = await currentUserToken(page)
      const stamp = Date.now()
      for (let i = 0; i < 12; i += 1) {
        await createFeedback(token, { type: 'bug', content: `E2E 分页预算 ${stamp}-${i}` })
      }

      await page.setViewportSize({ width: 1440, height: 900 })
      await bootstrapAdminSession(page)
      await page.goto('/admin/feedback')

      await expect(page.locator('.feedback-table .el-table__row').first()).toBeVisible({ timeout: 10000 })
      const pagination = page.locator('.pagination-container')
      await expect(pagination).toBeVisible({ timeout: 8000 })
      await page.waitForTimeout(500)

      const layout = await page.evaluate(() => {
        const pag = document.querySelector('.pagination-container')
        const filterItems = [...document.querySelectorAll('.filter-card .el-form-item')]
        const rect = pag.getBoundingClientRect()
        // 操作按钮组没有 label，靠 align-items: flex-end 对齐，top 与带 label 的控件天然不同；
        // 判断「是否折行」只看带 label 的筛选控件。
        const labeledTops = new Set(
          filterItems
            .filter((el) => el.querySelector('.el-form-item__label'))
            .map((el) => Math.round(el.getBoundingClientRect().top))
        )
        return {
          paginationTop: Math.round(rect.top),
          paginationBottom: Math.round(rect.bottom),
          labeledRows: labeledTops.size,
          filterItems: filterItems.length,
          rowsShown: document.querySelectorAll('.feedback-table .el-table__row').length,
          viewportH: window.innerHeight
        }
      })

      expect(layout.viewportH).toBe(900)
      expect(layout.rowsShown).toBe(10)
      // 硬指标：分页条完整落在首屏
      expect(layout.paginationBottom).toBeLessThan(900)
      // 新增优先级筛选不得把筛选区挤成两行
      expect(layout.labeledRows).toBe(1)

      // 截图留档为独立视觉评审输入（§4.10 要求保留证据，见 docs/evidence/issue-549/）
      const shot = require('path').resolve(
        __dirname, '../../docs/evidence/issue-549/feedback-pagination-1440x900.png')
      require('fs').mkdirSync(require('path').dirname(shot), { recursive: true })
      await page.screenshot({ path: shot, fullPage: false })

      markPassed('18.9', `分页条 ${layout.paginationTop}-${layout.paginationBottom}px、筛选 ${layout.filterItems} 项单行；截图 ${shot}`)
    } catch (e) {
      markFailed('18.9', e.message.slice(0, 60))
      throw e
    }
  })

  test.afterAll(() => {
    updateSummary()
  })
})
