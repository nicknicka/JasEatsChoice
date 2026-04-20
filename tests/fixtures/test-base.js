const { test: base, expect } = require('@playwright/test')

const USER_CREDENTIALS = {
  username: process.env.TEST_USER || 'testuser',
  password: process.env.TEST_USER_PWD || '123456'
}

const MERCHANT_CREDENTIALS = {
  username: process.env.TEST_MERCHANT || 'testmerchant',
  password: process.env.TEST_MERCHANT_PWD || '123456'
}

const ADMIN_CREDENTIALS = {
  username: process.env.TEST_ADMIN || 'admin',
  password: process.env.TEST_ADMIN_PWD || 'admin123'
}

const test = base.extend({
  userPage: async ({ browser }, use) => {
    const context = await browser.newContext()
    const page = await context.newPage()
    await page.goto('/login')
    await use(page)
    await context.close()
  },
  merchantPage: async ({ browser }, use) => {
    const context = await browser.newContext()
    const page = await context.newPage()
    await page.goto('/login')
    await use(page)
    await context.close()
  },
  adminPage: async ({ browser }, use) => {
    const context = await browser.newContext()
    const page = await context.newPage()
    await page.goto('/admin/login')
    await use(page)
    await context.close()
  }
})

async function loginAsUser(page, username, password) {
  const user = username || USER_CREDENTIALS.username
  const pwd = password || USER_CREDENTIALS.password
  await page.goto('/login')
  await page.waitForSelector('input[placeholder*="用户名"], input[placeholder*="账号"], input[type="text"]', { timeout: 10000 }).catch(() => null)
  const usernameInput = page.locator('input').first()
  const passwordInput = page.locator('input[type="password"]').first()
  await usernameInput.fill(user)
  await passwordInput.fill(pwd)
  const loginBtn = page.locator('button:has-text("登录"), button:has-text("登 录"), button[type="submit"]').first()
  await loginBtn.click()
  await page.waitForURL('**/user/home**', { timeout: 15000 }).catch(() => null)
  return page
}

async function loginAsMerchant(page, username, password) {
  const user = username || MERCHANT_CREDENTIALS.username
  const pwd = password || MERCHANT_CREDENTIALS.password
  await page.goto('/login')
  await page.waitForSelector('input', { timeout: 10000 }).catch(() => null)
  const usernameInput = page.locator('input').first()
  const passwordInput = page.locator('input[type="password"]').first()
  await usernameInput.fill(user)
  await passwordInput.fill(pwd)
  const loginBtn = page.locator('button:has-text("登录"), button[type="submit"]').first()
  await loginBtn.click()
  await page.waitForURL('**/merchant/home**', { timeout: 15000 }).catch(() => null)
  return page
}

async function loginAsAdmin(page, username, password) {
  const user = username || ADMIN_CREDENTIALS.username
  const pwd = password || ADMIN_CREDENTIALS.password
  await page.goto('/admin/login')
  await page.waitForSelector('input', { timeout: 10000 }).catch(() => null)
  const usernameInput = page.locator('input').first()
  const passwordInput = page.locator('input[type="password"]').first()
  await usernameInput.fill(user)
  await passwordInput.fill(pwd)
  const loginBtn = page.locator('button:has-text("登录"), button[type="submit"]').first()
  await loginBtn.click()
  await page.waitForURL('**/admin/dashboard**', { timeout: 15000 }).catch(() => null)
  return page
}

module.exports = {
  test,
  expect,
  loginAsUser,
  loginAsMerchant,
  loginAsAdmin,
  USER_CREDENTIALS,
  MERCHANT_CREDENTIALS,
  ADMIN_CREDENTIALS
}