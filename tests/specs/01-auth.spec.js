const { test, expect, loginAsUser, loginAsAdmin, USER_CREDENTIALS } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, markSkipped, updateSummary } = require('../utils/progress')
const { TEST_API_BASE } = require('../utils/api')

const API_BASE = TEST_API_BASE.replace(/\/$/, '')

async function api(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, options)
  const result = await response.json().catch(() => null)
  return { response, result }
}

async function fetchCaptcha() {
  const { response, result } = await api('/v1/captcha/checkCode')
  expect(response.ok, '验证码接口应返回成功').toBeTruthy()
  expect(result?.success, '验证码接口应返回 success=true').toBe(true)
  return result.data
}

test.describe('模块一：认证模块', () => {

  test('1.1 用户登录', async ({ page }) => {
    markRunning('1.1')
    try {
      await loginAsUser(page)

      await expect(page).toHaveURL(/\/user\/home/)
      await expect(page.getByText('今日推荐', { exact: true })).toBeVisible({ timeout: 15000 })

      const auth = await page.evaluate(() => ({
        token: localStorage.getItem('auth_token'),
        userId: localStorage.getItem('auth_userId'),
        phone: localStorage.getItem('auth_phone')
      }))
      expect(auth.token).toBeTruthy()
      expect(auth.userId).toBeTruthy()
      expect(auth.phone).toBe(USER_CREDENTIALS.phone)

      markPassed('1.1', '登录成功、跳转首页并写入认证态')
    } catch (e) {
      markFailed('1.1', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.2 用户注册', async ({ page }) => {
    markRunning('1.2')
    try {
      await page.goto('/register')
      await expect(page).toHaveURL(/\/register/)
      await expect(page.getByRole('heading', { name: '创建账号' })).toBeVisible()

      const nickname = `e2e_${Date.now()}`
      const password = 'e2ePass123'
      await page.getByPlaceholder('用户名').fill(nickname)
      await page.getByPlaceholder('密码（6-32位）').fill(password)
      await page.getByPlaceholder('确认密码').fill(password)
      await page.getByRole('button', { name: /下一步/ }).click()
      await expect(page.getByText('填写联系方式', { exact: true })).toBeVisible()

      const captcha = await fetchCaptcha()
      const phone = `139${String(Date.now()).slice(-8)}`
      const email = `${nickname}@example.com`
      await page.getByPlaceholder('邮箱').fill(email)
      await page.getByPlaceholder('手机号').fill(phone)
      await page.getByPlaceholder('验证码').fill(captcha.fixedCaptchaCode || captcha.captchaAnswer)

      const registerResponsePromise = page.waitForResponse((response) =>
        response.url().includes('/v1/users/register') && response.request().method() === 'POST'
      )
      await page.getByRole('button', { name: '注册', exact: true }).click()
      const registerResponse = await registerResponsePromise
      const registerPayload = await registerResponse.json()
      expect(registerPayload.success, `注册接口应成功：${registerPayload.message || ''}`).toBe(true)
      expect(registerPayload.data).toBe('注册成功')

      await page.waitForURL(/\/login/, { timeout: 15000 })
      await expect(page.locator('button.login-btn')).toBeVisible()
      await expect(page.getByPlaceholder('手机号')).toBeVisible()

      markPassed('1.2', '完整注册并跳转登录页')
    } catch (e) {
      markFailed('1.2', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.3 找回密码', async ({ page }) => {
    markRunning('1.3')
    try {
      await page.goto('/forgot-password')
      await expect(page).toHaveURL(/\/forgot-password/)
      await expect(page.getByRole('heading', { name: '重置密码' })).toBeVisible()
      await expect(page.getByText('验证手机号', { exact: true })).toBeVisible()

      await page.getByPlaceholder('注册时使用的手机号').fill(USER_CREDENTIALS.phone)
      const smsResponsePromise = page.waitForResponse((response) =>
        response.url().includes('/v1/users/send-sms-code') && response.request().method() === 'POST'
      )
      await page.getByRole('button', { name: '获取验证码' }).click()
      const smsResponse = await smsResponsePromise
      const smsPayload = await smsResponse.json()
      expect(smsPayload.success, `短信接口应成功：${smsPayload.message || ''}`).toBe(true)
      await expect(page.locator('.el-message--success').filter({ hasText: '验证码已发送' })).toBeVisible()
      await expect(page.locator('.code-btn')).toBeDisabled()

      await page.getByPlaceholder('短信验证码').fill('000000')
      await page.getByRole('button', { name: /下一步/ }).click()
      await expect(page.getByText('设置新密码', { exact: true })).toBeVisible()
      await expect(page.getByPlaceholder('新密码（6-32位）')).toBeVisible()
      await expect(page.getByPlaceholder('确认新密码')).toBeVisible()

      markPassed('1.3', '真实发送验证码并进入重置密码步骤')
    } catch (e) {
      markFailed('1.3', e.message.slice(0, 50))
      throw e
    }
  })

  test.fixme('1.4 商家注册 (blocked by #58)', async ({ page }) => {
    await loginAsUser(page)
    await page.goto('/merchant/register')
    await expect(page).toHaveURL(/\/merchant\/register/)
    await expect(page.getByRole('heading', { name: '商户注册' })).toBeVisible()

    const suffix = Date.now()
    const merchantName = `测试商户${suffix}`
    const phone = `137${String(suffix).slice(-8)}`
    const captcha = await fetchCaptcha()

    await page.getByPlaceholder('请输入商户名称').fill(merchantName)
    await page.getByPlaceholder('请输入营业执照号').fill(`E2E${suffix}`)
    await page.locator('.el-select').click()
    await page.getByRole('option', { name: '中餐' }).click()
    await page.getByPlaceholder('请输入联系人姓名').fill('测试联系人')
    await page.getByPlaceholder('请输入联系人电话').fill(phone)
    await page.getByPlaceholder('请输入邮箱').fill(`merchant_${suffix}@example.com`)
    await page.getByPlaceholder('请输入验证码').fill(captcha.fixedCaptchaCode || captcha.captchaAnswer)

    const responsePromise = page.waitForResponse((response) =>
      response.url().includes('/v1/merchant/register') && response.request().method() === 'POST'
    )
    await page.getByRole('button', { name: '注册', exact: true }).click()
    const response = await responsePromise
    const payload = await response.json()
    expect(payload.success, `商家注册接口应成功：${payload.message || ''}`).toBe(true)
    expect(payload.data?.id).toBeTruthy()

    const users = await api(`/v1/users/search?keyword=${encodeURIComponent(phone)}&searchType=phone`)
    expect(users.result.data[0].merchantId).toBe(payload.data.id)
  })

  test('1.5 管理员登录', async ({ page }) => {
    markRunning('1.5')
    try {
      await loginAsAdmin(page)

      await expect(page).toHaveURL(/\/admin\/dashboard/)
      await expect(page.locator('.admin-layout')).toBeVisible()
      await expect(page.locator('.admin-dashboard-container')).toBeVisible()

      const auth = await page.evaluate(() => ({
        token: localStorage.getItem('admin_token'),
        info: localStorage.getItem('admin_info')
      }))
      expect(auth.token).toBeTruthy()
      expect(auth.info).toBeTruthy()
      expect(JSON.parse(auth.info).username).toBe('admin')

      markPassed('1.5', '管理员登录成功、进入控制台并保存管理员认证态')
    } catch (e) {
      markFailed('1.5', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.6 登录异常', async ({ page }) => {
    markRunning('1.6')
    try {
      await page.goto('/login')
      await expect(page).toHaveURL(/\/login/)

      await page.getByPlaceholder('手机号').fill(USER_CREDENTIALS.phone)
      await page.getByPlaceholder('密码').fill('wrongpwd')
      const captcha = await fetchCaptcha()
      await page.getByPlaceholder('验证码').fill(captcha.fixedCaptchaCode || captcha.captchaAnswer)
      await page.locator('.agreement-row .el-checkbox').click()
      await page.locator('button.login-btn').click()

      const errorMessage = page.locator('.el-message--error')
      await expect(errorMessage.last()).toContainText(/登录失败|密码/)
      await expect(page).not.toHaveURL(/\/user\/home/)
      await expect(page).toHaveURL(/\/login/)

      const token = await page.evaluate(() => localStorage.getItem('auth_token'))
      expect(token).toBeNull()

      markPassed('1.6', '错误密码返回错误提示且不写入认证态')
    } catch (e) {
      markFailed('1.6', e.message.slice(0, 50))
      throw e
    }
  })

  test('1.7 退出登录', async ({ page }) => {
    markRunning('1.7')
    try {
      await loginAsUser(page)
      await page.goto('/user/home/profile')
      await expect(page).toHaveURL(/\/user\/home\/profile/)

      const logoutBtn = page.getByRole('button', { name: '退出登录' })
      await expect(logoutBtn).toBeVisible({ timeout: 10000 })
      await logoutBtn.click()

      const confirmBtn = page.locator('.el-message-box__btns .el-button--primary').last()
      await expect(confirmBtn).toBeVisible({ timeout: 5000 })
      await confirmBtn.click()

      await page.waitForURL('**/login**', { timeout: 10000 })
      await expect(page.locator('button.login-btn')).toBeVisible()

      const auth = await page.evaluate(() => ({
        authToken: localStorage.getItem('auth_token'),
        authUserId: localStorage.getItem('auth_userId'),
        token: localStorage.getItem('token'),
        userId: localStorage.getItem('userId')
      }))
      expect(auth.authToken).toBeNull()
      expect(auth.authUserId).toBeNull()
      expect(auth.token).toBeNull()
      expect(auth.userId).toBeNull()

      markPassed('1.7', '退出成功、返回登录页并清空登录态')
    } catch (e) {
      markFailed('1.7', e.message.slice(0, 50))
      throw e
    }
  })

  test.afterAll(() => {
    markSkipped('1.4', '#58', '桌面商家注册前后端契约不一致，无法真实完成注册')
    updateSummary()
  })
})
