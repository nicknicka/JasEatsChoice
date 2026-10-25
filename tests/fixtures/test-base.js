const path = require('path')
const { test: base, expect } = require('@playwright/test')
const { LoginPage, AdminLoginPage } = require('../pages/common')
// 后端地址与 utils/api.js 共用同一个来源（TEST_API_BASE）
const { TEST_API_BASE } = require('../utils/api')

const USER_CREDENTIALS = {
  phone: process.env.TEST_USER_PHONE || process.env.TEST_USER || '17322222222',
  password: process.env.TEST_USER_PWD || 'asdasd'
}

const MERCHANT_CREDENTIALS = {
  phone: process.env.TEST_MERCHANT_PHONE || process.env.TEST_MERCHANT || '17322222222',
  password: process.env.TEST_MERCHANT_PWD || 'asdasd'
}

const ADMIN_CREDENTIALS = {
  username: process.env.TEST_ADMIN_USER || process.env.TEST_ADMIN || 'admin',
  password: process.env.TEST_ADMIN_PWD || 'admin123'
}

// Web 版产物与用例共用同一份 window.api 垫片（apps/client 里的 platform/webShim.js）。
// 该文件不含 import/export，所以 Playwright 可以直接按路径注入。
const WEB_SHIM_PATH = path.resolve(
  __dirname,
  '../../apps/client/src/renderer/src/platform/webShim.js'
)

async function installElectronApiStub(page) {
  await page.addInitScript({ path: WEB_SHIM_PATH })
}

const test = base.extend({
  page: async ({ page }, use) => {
    await installElectronApiStub(page)
    await use(page)
  },
  userPage: async ({ browser }, use) => {
    const context = await browser.newContext()
    const page = await context.newPage()
    await installElectronApiStub(page)
    await page.goto('/login')
    await use(page)
    await context.close()
  },
  merchantPage: async ({ browser }, use) => {
    const context = await browser.newContext()
    const page = await context.newPage()
    await installElectronApiStub(page)
    await page.goto('/login')
    await use(page)
    await context.close()
  },
  adminPage: async ({ browser }, use) => {
    const context = await browser.newContext()
    const page = await context.newPage()
    await installElectronApiStub(page)
    await page.goto('/admin/login')
    await use(page)
    await context.close()
  }
})

async function loginAsUser(page, username, password) {
  const user = username || USER_CREDENTIALS.phone
  const pwd = password || USER_CREDENTIALS.password
  const loginPage = new LoginPage(page)
  await loginPage.goto()
  await loginPage.login(user, pwd)
  await page.waitForURL('**/user/home**', { timeout: 20000 })
  return page
}

async function loginAsMerchant(page, username, password) {
  const user = username || MERCHANT_CREDENTIALS.phone
  const pwd = password || MERCHANT_CREDENTIALS.password
  await loginAsUser(page, user, pwd)
  await page.goto('/merchant/home')
  await page.waitForURL('**/merchant/home**', { timeout: 20000 })
  return page
}

async function loginAsAdmin(page, username, password) {
  const user = username || ADMIN_CREDENTIALS.username
  const pwd = password || ADMIN_CREDENTIALS.password
  const adminLoginPage = new AdminLoginPage(page)
  await adminLoginPage.goto()
  await adminLoginPage.login(user, pwd)
  await page.waitForURL('**/admin/dashboard**', { timeout: 20000 })
  return page
}

async function bootstrapAdminSession(page, username, password) {
  const user = username || ADMIN_CREDENTIALS.username
  const pwd = password || ADMIN_CREDENTIALS.password

  const response = await fetch(`${TEST_API_BASE}/admin/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: user, password: pwd })
  })

  const result = await response.json().catch(() => ({}))
  if (!response.ok || !result.success || !result.token) {
    throw new Error(`管理员接口登录失败: ${result.message || response.status}`)
  }

  await page.goto('/login')
  await page.evaluate(({ token, admin }) => {
    localStorage.setItem('admin_token', token)
    localStorage.setItem('admin_info', JSON.stringify(admin || {}))
  }, { token: result.token, admin: result.admin || {} })

  await page.goto('/admin/dashboard')
  await page.waitForURL('**/admin/dashboard**', { timeout: 20000 })
  return page
}

module.exports = {
  test,
  expect,
  loginAsUser,
  loginAsMerchant,
  loginAsAdmin,
  bootstrapAdminSession,
  USER_CREDENTIALS,
  MERCHANT_CREDENTIALS,
  ADMIN_CREDENTIALS
}
