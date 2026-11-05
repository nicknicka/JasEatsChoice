/**
 * 逐页截图脚本（独立运行，不依赖 Playwright test runner）。
 *
 * 用法：
 *   node scripts/capture_screens.js                             # 全量 75 页
 *   ONLY=05-order-confirmation node scripts/capture_screens.js   # 只跑指定 slug（补跑/冒烟）
 *
 * 前置：
 *   - 后端栈在 7777 健康运行（make up）
 *   - Web 开发服务器在 5173（npm run dev:web）；scripts/screenshots.sh 会自动拉起
 *
 * 环境变量：
 *   SCREENSHOT_BASE_URL  Web 基址，默认 http://localhost:5173
 *   TEST_API_BASE        后端 API 基址，默认 http://localhost:7777/api
 *   SCREENSHOT_OUT       产出目录，默认 <repo>/output/web-screenshots
 *   ONLY                 逗号分隔的 slug 白名单（按 role/slug 合并进 manifest.json）
 */
const fs = require('fs')
const path = require('path')

const PROJ = path.resolve(__dirname, '..')
const { chromium } = require(path.join(PROJ, 'tests/node_modules/@playwright/test'))
const {
  loginAsUser,
  loginAsMerchant,
  bootstrapAdminSession
} = require(path.join(PROJ, 'tests/fixtures/test-base'))

const WEB_SHIM = path.join(PROJ, 'apps/client/src/renderer/src/platform/webShim.js')
const BASE = process.env.SCREENSHOT_BASE_URL || 'http://localhost:5173'
const API = process.env.TEST_API_BASE || 'http://localhost:7777/api'
const OUT = process.env.SCREENSHOT_OUT || path.join(PROJ, 'output/web-screenshots')
const VIEWPORT = { width: 1440, height: 900 }
const ONLY = (process.env.ONLY || '').split(',').map((s) => s.trim()).filter(Boolean)

const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

async function apiData(p) {
  const res = await fetch(`${API}${p}`)
  if (!res.ok) throw new Error(`GET ${p} -> ${res.status}`)
  const body = await res.json()
  return body && Object.prototype.hasOwnProperty.call(body, 'data') ? body.data : body
}

async function loadReferenceData() {
  const users = await apiData('/v1/users/search?keyword=17322222222&searchType=phone')
  const user = Array.isArray(users) ? users[0] : users
  const userId = user && (user.userId || user.id)
  const merchantId = user && user.merchantId

  let dishId = null
  let menuId = null
  let cartItems = []
  try {
    const menus = await apiData(`/v1/menus/merchants/${encodeURIComponent(merchantId)}/menu`)
    const firstMenu = (menus || [])[0]
    menuId = firstMenu ? String(firstMenu.id) : null
    const dishes = (menus || [])
      .flatMap((m) => m.dishes || [])
      .filter((d) => d && d.id && String(d.id) !== '1')
    const preferred = dishes.find((d) => String(d.id) === '1001') || dishes[0]
    dishId = preferred ? String(preferred.id) : null
    cartItems = dishes.slice(0, 2).map((d) => ({
      id: String(d.id),
      name: d.name,
      price: Number(d.price) || 0,
      quantity: 1,
      category: d.category || ''
    }))
  } catch (e) {
    console.warn('菜品取数失败:', e.message)
  }

  let merchantInfo = null
  try {
    const m = await apiData(`/v1/merchants/${encodeURIComponent(merchantId)}`)
    merchantInfo = {
      merchantId: String(m.merchantId || m.id),
      name: m.merchantName || m.name || '佳食优选餐厅',
      rating: m.rating || 4.8,
      deliveryTime: '约30分钟',
      deliveryFee: 5
    }
  } catch (e) {
    console.warn('商家信息取数失败:', e.message)
  }

  const userOrders = await apiData(`/v1/orders/user/${encodeURIComponent(userId)}`)
  const orderId = (userOrders && userOrders[0] && userOrders[0].id) || null
  const reviewableOrder = (userOrders || []).find((o) => String(o.status) === '3')
  const evaluateOrderId = (reviewableOrder && reviewableOrder.id) || orderId

  const merchantOrders = await apiData(
    `/v1/orders/merchant/${encodeURIComponent(merchantId)}?today=false`
  )
  const merchantOrderId = (merchantOrders && merchantOrders[0] && merchantOrders[0].id) || null

  let tutorialId = null
  try {
    const tutorials = await apiData('/v1/tutorial/list')
    tutorialId = tutorials && tutorials[0] ? String(tutorials[0].id) : null
  } catch (e) {
    console.warn('教程取数失败:', e.message)
  }

  return { userId, merchantId, dishId, menuId, cartItems, merchantInfo, orderId, evaluateOrderId, merchantOrderId, tutorialId }
}

function buildPlan(d) {
  const q = encodeURIComponent
  return {
    public: [
      { slug: '01-login', url: '/login' },
      { slug: '02-register', url: '/register' },
      { slug: '03-forgot-password', url: '/forgot-password' },
      { slug: '04-merchant-register', url: '/merchant/register' }
    ],
    user: [
      { slug: '01-home', url: '/user/home' },
      { slug: '02-recommend', url: '/user/home/recommend' },
      { slug: '03-merchants', url: '/user/home/merchants' },
      {
        slug: '04-merchant-detail',
        url: `/user/home/merchant-detail?merchantId=${q(d.merchantId)}`,
        requires: 'merchantId'
      },
      {
        slug: '05-order-confirmation',
        url: '/user/home/order-confirmation',
        seedSession: {
          key: 'pendingOrder',
          value: {
            merchant: d.merchantInfo,
            cartItems: d.cartItems,
            totalAmount: d.cartItems.reduce((s, i) => s + i.price * i.quantity, 0),
            userName: 'asd'
          }
        }
      },
      { slug: '06-today-recipe', url: '/user/home/today-recipe' },
      { slug: '07-calorie', url: '/user/home/calorie' },
      { slug: '08-my-recipe', url: '/user/home/my-recipe' },
      { slug: '09-diet-record', url: '/user/home/diet-record' },
      { slug: '10-wish-list', url: '/user/home/wish-list' },
      { slug: '11-orders', url: '/user/home/orders' },
      { slug: '12-order-detail', url: `/user/home/order-detail/${q(d.orderId)}`, requires: 'orderId' },
      {
        slug: '13-evaluate-order',
        url: `/user/home/evaluate-order/${q(d.evaluateOrderId)}`,
        requires: 'evaluateOrderId'
      },
      { slug: '14-consume-history', url: '/user/home/consume-history' },
      { slug: '15-message-center', url: '/user/home/message-center' },
      { slug: '16-system-notification', url: '/user/home/system-notification' },
      { slug: '17-chat', url: '/user/home/chat' },
      { slug: '18-contacts', url: '/user/home/contacts' },
      { slug: '19-ai', url: '/user/home/ai' },
      { slug: '20-settings', url: '/user/home/settings' },
      { slug: '21-profile', url: '/user/home/profile' },
      { slug: '22-wallet-management', url: '/user/home/wallet-management' },
      { slug: '23-wallet-transactions', url: '/user/home/wallet-transactions' },
      { slug: '24-payment-password-setup', url: '/user/home/payment-password-setup' },
      { slug: '25-wallet-security', url: '/user/home/wallet-security' },
      { slug: '26-address', url: '/user/home/address' },
      { slug: '27-contact', url: '/user/home/contact' },
      { slug: '28-dish-detail', url: `/user/home/dish-detail/${q(d.dishId)}`, requires: 'dishId' },
      { slug: '29-my-collection', url: '/user/home/my-collection' },
      { slug: '30-tutorials', url: '/user/home/tutorials' },
      {
        slug: '31-tutorial-detail',
        url: `/user/home/tutorials/${q(d.tutorialId)}`,
        requires: 'tutorialId'
      },
      { slug: '32-tutorials-publish', url: '/user/home/tutorials/publish' },
      { slug: '33-tutorials-my', url: '/user/home/tutorials/my' },
      { slug: '34-hot-topic', url: '/user/home/hot-topic' }
    ],
    merchant: [
      { slug: '01-home', url: '/merchant/home' },
      { slug: '02-orders', url: '/merchant/home/orders' },
      { slug: '03-today-orders', url: '/merchant/home/today-orders' },
      { slug: '04-menu', url: '/merchant/home/menu' },
      { slug: '05-messages', url: '/merchant/home/messages' },
      { slug: '06-menu-edit', url: `/merchant/home/menu-edit?menuId=${q(d.menuId)}` },
      { slug: '07-dish-management', url: '/merchant/home/dish-management' },
      { slug: '08-dish-edit', url: '/merchant/home/dish-edit' },
      { slug: '09-chat', url: '/merchant/home/chat' },
      { slug: '10-statistics', url: '/merchant/home/statistics' },
      {
        slug: '11-order-detail',
        url: `/merchant/home/order-detail/${q(d.merchantOrderId)}`,
        requires: 'merchantOrderId'
      },
      { slug: '12-comments', url: '/merchant/home/comments' },
      { slug: '13-settings', url: '/merchant/home/settings' },
      { slug: '14-tutorials', url: '/merchant/home/tutorials' },
      { slug: '15-wish-list-audit', url: '/merchant/home/wish-list-audit' },
      { slug: '16-ai', url: '/merchant/home/ai' }
    ],
    admin: [
      { slug: '01-dashboard', url: '/admin/dashboard' },
      { slug: '02-users', url: '/admin/users' },
      { slug: '03-merchants', url: '/admin/merchants' },
      { slug: '04-merchants-audit', url: '/admin/merchants/audit' },
      { slug: '05-orders', url: '/admin/orders' },
      { slug: '06-dishes', url: '/admin/dishes' },
      { slug: '07-dishes-audit', url: '/admin/dishes/audit' },
      { slug: '08-tutorials-manage', url: '/admin/tutorials/manage' },
      { slug: '09-tutorials-review', url: '/admin/tutorials/review' },
      { slug: '10-topics', url: '/admin/topics' },
      { slug: '11-announcements', url: '/admin/announcements' },
      { slug: '12-withdrawals', url: '/admin/finance/withdrawals' },
      { slug: '13-recharges', url: '/admin/finance/recharges' },
      { slug: '14-refunds', url: '/admin/finance/refunds' },
      { slug: '15-settings', url: '/admin/settings' },
      { slug: '16-profile', url: '/admin/settings/profile' },
      { slug: '17-password', url: '/admin/settings/password' },
      { slug: '18-roles', url: '/admin/settings/roles' },
      { slug: '19-permissions', url: '/admin/settings/permissions' },
      { slug: '20-logs', url: '/admin/settings/logs' },
      { slug: '21-statistics', url: '/admin/statistics' }
    ]
  }
}

async function shoot(page, role, item, manifest) {
  const dir = path.join(OUT, role)
  fs.mkdirSync(dir, { recursive: true })
  const file = path.join(dir, `${item.slug}.png`)
  const entry = {
    role,
    slug: item.slug,
    url: item.url,
    file: path.relative(OUT, file),
    finalUrl: null,
    title: null,
    ok: false,
    note: null
  }
  const errors = []
  const onError = (e) => errors.push(String(e && e.message ? e.message : e))
  page.on('pageerror', onError)

  try {
    if (item.requires && (item.url.includes('/undefined') || item.url.includes('/null'))) {
      throw new Error(`缺少真实数据：${item.requires}`)
    }
    if (item.seedSession) {
      await page
        .evaluate(
          ({ k, v }) => sessionStorage.setItem(k, JSON.stringify(v)),
          { k: item.seedSession.key, v: item.seedSession.value }
        )
        .catch(() => {})
    }
    await page.goto(`${BASE}${item.url}`, { waitUntil: 'domcontentloaded', timeout: 30000 })
    await sleep(1500)

    for (let i = 0; i < 5; i += 1) {
      const visible = await page.locator('.el-loading-mask:visible').count().catch(() => 0)
      if (visible === 0) break
      await sleep(1000)
    }
    await sleep(400)

    const u = new URL(page.url())
    entry.finalUrl = u.pathname + u.search
    entry.title = await page.title().catch(() => null)
    await page.screenshot({ path: file, fullPage: true })
    entry.ok = true
    process.stdout.write(`  ✓ ${item.slug}\n`)
  } catch (e) {
    entry.note = e.message
    await page.screenshot({ path: file, fullPage: true }).catch(() => {})
    console.error(`  ✗ ${item.slug} -> ${e.message}`)
  } finally {
    page.off('pageerror', onError)
  }

  if (errors.length) {
    entry.note = (entry.note ? `${entry.note}; ` : '') + `console错误: ${errors.slice(0, 2).join(' | ')}`
  }
  manifest.push(entry)
}

async function runRole(browser, role, items) {
  const filtered = ONLY.length ? items.filter((i) => ONLY.includes(i.slug)) : items
  if (!filtered.length) return []

  console.log(`\n=== ${role}（${filtered.length} 页）===`)
  const context = await browser.newContext({ viewport: VIEWPORT, baseURL: BASE })
  const page = await context.newPage()
  await page.addInitScript({ path: WEB_SHIM })
  page.on('dialog', (d) => d.dismiss().catch(() => {}))

  const manifest = []
  try {
    if (role === 'user') await loginAsUser(page)
    if (role === 'merchant') await loginAsMerchant(page)
    if (role === 'admin') await bootstrapAdminSession(page)

    for (const item of filtered) {
      await shoot(page, role, item, manifest)
    }
  } finally {
    await context.close()
  }
  return manifest
}

;(async () => {
  try {
    const res = await fetch(BASE, { signal: AbortSignal.timeout(5000) })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
  } catch (e) {
    console.error(`Web 开发服务器不可访问（${BASE}）：${e.message}`)
    console.error('请先运行：cd apps/client && npm run dev:web')
    process.exit(1)
  }

  console.log('读取真实数据 ID…')
  const data = await loadReferenceData()
  console.log(' ', JSON.stringify(data))
  const plan = buildPlan(data)

  const browser = await chromium.launch()
  const manifest = []
  try {
    for (const role of ['public', 'user', 'merchant', 'admin']) {
      const entries = await runRole(browser, role, plan[role])
      manifest.push(...entries)
    }
  } finally {
    await browser.close()
  }

  const manifestPath = path.join(OUT, 'manifest.json')
  let merged = manifest
  if (ONLY.length && fs.existsSync(manifestPath)) {
    const prev = JSON.parse(fs.readFileSync(manifestPath, 'utf8'))
    const byKey = new Map(prev.map((e) => [`${e.role}/${e.slug}`, e]))
    for (const e of manifest) byKey.set(`${e.role}/${e.slug}`, e)
    merged = [...byKey.values()]
  }
  fs.writeFileSync(manifestPath, JSON.stringify(merged, null, 2))
  const ok = merged.filter((m) => m.ok).length
  console.log(`\n完成：${ok}/${merged.length} 张，输出目录 ${OUT}`)
})().catch((e) => {
  console.error('脚本失败:', e)
  process.exit(1)
})
