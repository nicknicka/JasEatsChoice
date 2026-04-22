const fs = require('fs/promises')
const path = require('path')
const { chromium } = require('playwright')

const ROOT_DIR = '/Users/nickxiao/.codex/worktrees/4d60/JasEatsChoice'
const APP_ORIGIN = 'http://[::1]:5173'
const OUTPUT_DIR = path.join(ROOT_DIR, 'output/playwright/thesis-screenshots')
const FIGURE_DIR = path.join(ROOT_DIR, '论文插图')
const SAMPLE_IMAGE = path.join(OUTPUT_DIR, 'sample_dish.png')

let page

const svgData = (label, bg, fg = '#ffffff') => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="640" height="400" viewBox="0 0 640 400">
      <defs>
        <linearGradient id="g" x1="0" x2="1" y1="0" y2="1">
          <stop offset="0%" stop-color="${bg}"/>
          <stop offset="100%" stop-color="#1f2937"/>
        </linearGradient>
      </defs>
      <rect width="640" height="400" rx="32" fill="url(#g)"/>
      <circle cx="520" cy="110" r="68" fill="rgba(255,255,255,0.14)"/>
      <circle cx="118" cy="290" r="88" fill="rgba(255,255,255,0.08)"/>
      <text x="56" y="190" font-size="44" fill="${fg}" font-family="PingFang SC, Microsoft YaHei, sans-serif" font-weight="700">${label}</text>
      <text x="56" y="238" font-size="24" fill="rgba(255,255,255,0.88)" font-family="PingFang SC, Microsoft YaHei, sans-serif">佳食宜选论文页面截图素材</text>
    </svg>
  `
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

const userAvatar = svgData('张三', '#2563eb')
const merchantAvatar = svgData('轻食工坊', '#16a34a')

const seededUserInfo = {
  userId: '1000000000000000',
  phone: '13800138000',
  nickname: '张三',
  email: 'zhangsan@example.com',
  location: '广东省 深圳市',
  height: 175,
  weight: 68,
  dietGoal: '增肌塑形',
  allergies: [],
  preferTags: ['高蛋白', '低糖'],
  disableWeatherRecommend: false,
  createTime: '2026-03-01 10:00:00',
  updateTime: '2026-04-17 09:30:00',
  merchantId: '7638432224340229',
  avatar: userAvatar
}

const seededMerchantInfo = {
  id: '7638432224340229',
  name: '佳食轻食工坊',
  address: '软件园三期 B 栋 1 层',
  phone: '19233333333',
  status: true,
  businessLicense: '91440300MA5EXAMPLE',
  businessScope: ['轻食简餐', '健身餐', '饮品'],
  contactName: '陈店长',
  avatar: merchantAvatar,
  rating: 4.8,
  category: '轻食',
  averagePrice: 43
}

const merchantCards = [
  {
    id: 'm-1001',
    name: '轻盈能量碗',
    image: svgData('轻盈能量碗', '#0f766e'),
    rating: 4.9,
    distance: '0.8km',
    status: '营业中',
    type: '轻食',
    tags: ['高蛋白', '低脂', '午间热销'],
    isNew: true,
    discount: '满39减8'
  },
  {
    id: 'm-1002',
    name: '晨光健康早餐',
    image: svgData('晨光健康早餐', '#b45309'),
    rating: 4.8,
    distance: '1.2km',
    status: '营业中',
    type: '早餐',
    tags: ['低糖', '全麦', '早餐榜'],
    isNew: false,
    discount: '第二份半价'
  },
  {
    id: 'm-1003',
    name: '燃脂轻食研究所',
    image: svgData('燃脂轻食研究所', '#7c3aed'),
    rating: 4.7,
    distance: '1.6km',
    status: '营业中',
    type: '健身餐',
    tags: ['增肌', '低卡', '沙拉热销'],
    isNew: false,
    discount: '满58减12'
  },
  {
    id: 'm-1004',
    name: '都市鲜食厨房',
    image: svgData('都市鲜食厨房', '#dc2626'),
    rating: 4.6,
    distance: '2.4km',
    status: '营业中',
    type: '中餐',
    tags: ['时蔬', '现炒', '健康套餐'],
    isNew: false,
    discount: '新客立减6元'
  }
]

const orderList = [
  {
    id: 'ORD20260417001',
    createTime: '2026-04-17 11:26:05',
    totalAmount: 46.0,
    status: 1,
    address: '软件园一路 18 号 A 座 802',
    customerName: '张三',
    customerPhone: '13800138000'
  },
  {
    id: 'ORD20260417002',
    createTime: '2026-04-17 11:12:28',
    totalAmount: 62.0,
    status: 2,
    address: '科技大道 66 号 12 楼',
    customerName: '李四',
    customerPhone: '13800138001'
  },
  {
    id: 'ORD20260417003',
    createTime: '2026-04-17 10:45:09',
    totalAmount: 88.0,
    status: 3,
    address: '青年社区 5 栋 2 单元 301',
    customerName: '王五',
    customerPhone: '13900001234'
  }
]

const orderDishes = {
  ORD20260417001: [
    { dishId: 1, dishName: '香煎鸡胸能量碗', quantity: 1, price: 32.0 },
    { dishId: 2, dishName: '希腊酸奶水果杯', quantity: 1, price: 14.0 }
  ],
  ORD20260417002: [
    { dishId: 3, dishName: '牛油果藜麦沙拉', quantity: 1, price: 36.0 },
    { dishId: 4, dishName: '全麦鸡肉卷', quantity: 1, price: 26.0 }
  ],
  ORD20260417003: [{ dishId: 5, dishName: '黑椒牛肉蛋白餐', quantity: 2, price: 44.0 }]
}

const statisticsData = {
  code: '200',
  data: {
    basicStats: {
      orders: 286,
      totalAmount: 18436.0,
      avgAmount: 64.5,
      newCustomers: 38
    },
    orderTrend: [
      { time: '周一', orders: 32 },
      { time: '周二', orders: 41 },
      { time: '周三', orders: 39 },
      { time: '周四', orders: 45 },
      { time: '周五', orders: 56 },
      { time: '周六', orders: 44 },
      { time: '周日', orders: 29 }
    ],
    dishSalesRank: [
      { name: '香煎鸡胸能量碗', sales: 128, revenue: 4096.0 },
      { name: '牛油果藜麦沙拉', sales: 104, revenue: 3744.0 },
      { name: '黑椒牛肉蛋白餐', sales: 92, revenue: 4048.0 },
      { name: '希腊酸奶水果杯', sales: 88, revenue: 1232.0 }
    ]
  }
}

async function ensureDirectories() {
  await fs.mkdir(OUTPUT_DIR, { recursive: true })
  await fs.mkdir(FIGURE_DIR, { recursive: true })
}

async function switchRole(role) {
  await page.evaluate((nextRole) => {
    localStorage.setItem('auth_currentRole', nextRole)
  }, role)
}

async function persistScreenshot(filename, saver) {
  const outputPath = path.join(OUTPUT_DIR, filename)
  const figurePath = path.join(FIGURE_DIR, filename)
  await saver(outputPath)
  await fs.copyFile(outputPath, figurePath)
}

async function computeClipRegion({ topSelector, bottomSelector, padding = {} }) {
  const topLocator = page.locator(topSelector).first()
  const bottomLocator = page.locator(bottomSelector).first()
  const topBox = await topLocator.boundingBox()
  const bottomBox = await bottomLocator.boundingBox()

  if (!topBox || !bottomBox) {
    throw new Error(`未获取到裁切区域: ${topSelector} / ${bottomSelector}`)
  }

  const left = padding.left ?? 0
  const top = padding.top ?? 0
  const right = padding.right ?? 0
  const bottom = padding.bottom ?? 0

  const x = Math.max(0, topBox.x - left)
  const y = Math.max(0, topBox.y - top)
  const maxRight = Math.max(topBox.x + topBox.width, bottomBox.x + bottomBox.width) + right
  const maxBottom = bottomBox.y + bottomBox.height + bottom

  return {
    x,
    y,
    width: maxRight - x,
    height: maxBottom - y
  }
}

async function clearTransientUi() {
  await page.evaluate(() => {
    document.querySelectorAll('.el-message, .el-notification, .el-overlay, .el-popup-parent--hidden').forEach((node) => {
      if (node.classList.contains('el-overlay')) {
        const style = window.getComputedStyle(node)
        if (style.position === 'fixed') {
          node.remove()
        }
        return
      }
      node.remove()
    })
  })
}

async function installBrowserState() {
  await page.addInitScript(({ seededUserAvatar, seededMerchantAvatar }) => {
    const toBase64Url = (value) =>
      btoa(unescape(encodeURIComponent(value))).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '')

    const header = toBase64Url(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
    const payload = toBase64Url(
      JSON.stringify({
        userId: '1000000000000000',
        username: '张三',
        exp: 1893456000
      })
    )
    const token = `${header}.${payload}.signature`

    localStorage.setItem('auth_token', token)
    localStorage.setItem('auth_userId', '1000000000000000')
    localStorage.setItem('auth_phone', '13800138000')
    localStorage.setItem('auth_merchantId', '7638432224340229')
    localStorage.setItem('auth_currentRole', 'user')

    localStorage.setItem(
      'userInfo',
      JSON.stringify({
        userId: '1000000000000000',
        phone: '13800138000',
        nickname: '张三',
        email: 'zhangsan@example.com',
        location: '广东省 深圳市',
        height: 175,
        weight: 68,
        dietGoal: '增肌塑形',
        allergies: [],
        preferTags: ['高蛋白', '低糖'],
        disableWeatherRecommend: false,
        createTime: '2026-03-01 10:00:00',
        updateTime: '2026-04-17 09:30:00',
        merchantId: '7638432224340229',
        avatar: seededUserAvatar
      })
    )

    localStorage.setItem(
      'merchantInfo',
      JSON.stringify({
        id: '7638432224340229',
        name: '佳食轻食工坊',
        address: '软件园三期 B 栋 1 层',
        phone: '19233333333',
        status: true,
        businessLicense: '91440300MA5EXAMPLE',
        businessScope: ['轻食简餐', '健身餐', '饮品'],
        contactName: '陈店长',
        avatar: seededMerchantAvatar,
        rating: 4.8,
        category: '轻食',
        averagePrice: 43
      })
    )

    window.api = {
      store: {
        get: async () => null,
        set: async () => true,
        delete: async () => true
      },
      window: {
        resizeToMain: async () => true,
        resizeToLogin: async () => true,
        minimize: async () => true,
        maximize: async () => true,
        close: async () => true
      },
      clipboard: {
        writeText: async () => true
      },
      uploadImage: async () => ({ success: true, url: seededUserAvatar }),
      connectWebSocket: async () => true,
      disconnectWebSocket: async () => true,
      sendWebSocketMessage: async () => true,
      onWebSocketOpen: () => () => {},
      onWebSocketMessage: () => () => {},
      onWebSocketClose: () => () => {},
      onWebSocketError: () => () => {},
      removeWebSocketListener: async () => true,
      openExternal: async () => true
    }
  }, { seededUserAvatar: userAvatar, seededMerchantAvatar: merchantAvatar })
}

async function installRouteMocks() {
  await page.route('**/*', async (route) => {
    const url = route.request().url()
    const method = route.request().method()

    const json = async (body) => {
      await route.fulfill({
        status: 200,
        headers: {
          'access-control-allow-origin': '*',
          'content-type': 'application/json; charset=utf-8'
        },
        body: JSON.stringify(body)
      })
    }

    if (!url.includes('/api/') && !url.includes('/agent/')) {
      await route.continue()
      return
    }

    if (url.includes('/api/v1/merchant/') && url.includes('/statistics')) {
      await json(statisticsData)
      return
    }

    if (url.endsWith('/api/v1/merchant') || url.includes('/api/v1/merchant?')) {
      await json({ code: '200', data: merchantCards })
      return
    }

    if (url.includes('/api/v1/users/1000000000000000/preferences')) {
      await json({
        code: '200',
        data: {
          calorieTarget: 2100,
          nutritionGoals: {
            蛋白质: 110,
            碳水化合物: 240,
            脂肪: 68
          }
        }
      })
      return
    }

    if (url.includes('/api/calorie-records/user/1000000000000000/date/')) {
      await json({
        code: '200',
        data: [
          { calorie: 486, protein: 32, carbohydrate: 48, fat: 14 },
          { calorie: 612, protein: 42, carbohydrate: 55, fat: 18 },
          { calorie: 438, protein: 28, carbohydrate: 41, fat: 15 }
        ]
      })
      return
    }

    if (url.includes('/api/calorie-records/user/1000000000000000/week')) {
      await json({
        code: '200',
        data: [
          { day: '周一', consumed: 1820 },
          { day: '周二', consumed: 1960 },
          { day: '周三', consumed: 2050 },
          { day: '周四', consumed: 2140 },
          { day: '周五', consumed: 1980 },
          { day: '周六', consumed: 2210 },
          { day: '周日', consumed: 1536 }
        ]
      })
      return
    }

    if (url.includes('/api/notifications/unread-count')) {
      await json({ code: '200', data: 3 })
      return
    }

    if (url.includes('/api/v1/contacts/friends/requests')) {
      await json({ code: '200', data: [] })
      return
    }

    if (url.includes('/api/v1/contacts/friends?')) {
      await json({ code: '200', data: [] })
      return
    }

    if (url.includes('/api/v1/orders/merchant/7638432224340229')) {
      await json({ success: true, message: 'ok', data: orderList })
      return
    }

    if (url.includes('/api/v1/orders/') && url.includes('/dishes')) {
      const matched = url.match(/\/api\/v1\/orders\/([^/]+)\/dishes/)
      const orderId = matched ? matched[1] : ''
      await json({ success: true, message: 'ok', data: orderDishes[orderId] || [] })
      return
    }

    if (url.includes('/api/v1/ai/chat/has-history')) {
      await json({ code: '200', success: true, data: false })
      return
    }

    if (url.includes('/api/v1/ai/chat/history')) {
      await json({ code: '200', success: true, data: [] })
      return
    }

    if (url.includes('/api/v1/ai/dish-recognize') && method === 'POST') {
      await json({
        code: '200',
        success: true,
        data: {
          name: '香煎鸡胸能量碗',
          calories: 526,
          protein: 42,
          fat: 16,
          carbs: 48,
          difficulty: '简单',
          preparationTime: '18 分钟',
          ingredients: ['鸡胸肉', '藜麦', '西兰花', '圣女果', '牛油果'],
          tags: ['高蛋白', '控卡友好', '午餐推荐'],
          confidence: 0.96,
          nutritionScore: 92
        }
      })
      return
    }

    if (url.includes('/agent/supervisor-sse/chat')) {
      await route.fulfill({
        status: 200,
        headers: {
          'access-control-allow-origin': '*',
          'content-type': 'text/event-stream; charset=utf-8'
        },
        body: 'event: done\ndata: {"success":true}\n\n'
      })
      return
    }

    if (url.includes('/api/v1/users/1000000000000000')) {
      await json({ code: '200', data: seededUserInfo })
      return
    }

    if (url.includes('/api/v1/merchant/7638432224340229')) {
      await json({ code: '200', data: seededMerchantInfo })
      return
    }

    await json({ code: '200', success: true, data: [] })
  })
}

async function waitForStablePage(selector, extraDelay = 1200) {
  await page.waitForSelector(selector, { timeout: 20000 })
  await page.waitForLoadState('networkidle').catch(() => {})
  await page.waitForFunction(() => {
    const masks = Array.from(document.querySelectorAll('.el-loading-mask'))
    return masks.every((mask) => {
      const style = window.getComputedStyle(mask)
      return style.display === 'none' || style.visibility === 'hidden' || Number(style.opacity || '1') === 0
    })
  }, { timeout: 20000 }).catch(() => {})
  await page.waitForTimeout(extraDelay)
}

async function capture(options) {
  const {
    pathname,
    selector,
    filename,
    beforeShot,
    afterStable,
    viewportHeight = 1000,
    clipHeight = null,
    clipRegion = null
  } = options

  await page.setViewportSize({ width: 1500, height: viewportHeight })
  await page.goto(`${APP_ORIGIN}${pathname}`, { waitUntil: 'domcontentloaded' })
  if (beforeShot) {
    await beforeShot()
  }
  await waitForStablePage(selector)
  if (afterStable) {
    await afterStable()
  }
  await clearTransientUi()
  await page.evaluate(() => window.scrollTo(0, 0))

  const locator = page.locator(selector).first()
  if (clipRegion) {
    const clip = await computeClipRegion(clipRegion)
    await persistScreenshot(filename, async (outputPath) => {
      await page.screenshot({
        path: outputPath,
        clip
      })
    })
    return
  }
  if (clipHeight) {
    const box = await locator.boundingBox()
    if (!box) {
      throw new Error(`未获取到截图区域: ${selector}`)
    }
    await persistScreenshot(filename, async (outputPath) => {
      await page.screenshot({
        path: outputPath,
        clip: {
          x: box.x,
          y: box.y,
          width: box.width,
          height: Math.min(box.height, clipHeight)
        }
      })
    })
    return
  }

  await persistScreenshot(filename, async (outputPath) => {
    await locator.screenshot({ path: outputPath })
  })
}

async function main() {
  await ensureDirectories()
  const browser = await chromium.launch({ headless: true })

  try {
    page = await browser.newPage({
      viewport: { width: 1500, height: 1000 },
      locale: 'zh-CN'
    })

    await installBrowserState()
    await installRouteMocks()

    await capture({
      pathname: '/user/home/merchants',
      selector: '.merchant-list-container',
      filename: 'fig_4_3_user_merchant_list.png',
      viewportHeight: 1320,
      clipRegion: {
        topSelector: '.merchant-list-container',
        bottomSelector: '.merchant-grid',
        padding: { bottom: 20 }
      },
      afterStable: async () => {
        await page.waitForTimeout(600)
      }
    })

    await capture({
      pathname: '/user/home/calorie',
      selector: '.nordic-calorie',
      filename: 'fig_4_4_user_calorie.png',
      viewportHeight: 980,
      clipHeight: 760
    })

    await capture({
      pathname: '/user/home/ai',
      selector: '.ai-page-content',
      filename: 'fig_4_7_ai_assistant.png',
      viewportHeight: 1360,
      beforeShot: async () => {
        await page.waitForTimeout(800)
        await page.click('#tab-btn-recognition')
        await page.waitForSelector('.recognition-section', { timeout: 20000 })
        await page.setInputFiles('#dish-image-upload', SAMPLE_IMAGE)
        await page.click('.primary-btn')
        await page.waitForSelector('.recognition-result', { timeout: 20000 })
        await page.waitForTimeout(1800)
      }
    })

    await switchRole('merchant')
    await capture({
      pathname: '/merchant/home/orders',
      selector: '.merchant-orders-container',
      filename: 'fig_4_5_merchant_orders.png',
      viewportHeight: 1320,
      clipRegion: {
        topSelector: '.merchant-orders-container',
        bottomSelector: '.order-item',
        padding: { bottom: 20 }
      },
      afterStable: async () => {
        await page.waitForTimeout(800)
      }
    })

    await capture({
      pathname: '/merchant/home/statistics',
      selector: '.statistics-container',
      filename: 'fig_4_6_merchant_statistics.png',
      viewportHeight: 1400,
      beforeShot: async () => {
        await page.waitForFunction(() => {
          const chart = document.querySelector('.chart')
          return chart && chart.querySelector('canvas')
        }, { timeout: 20000 })
      },
      afterStable: async () => {
        await page.waitForTimeout(1600)
      }
    })
  } finally {
    await browser.close()
  }
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
