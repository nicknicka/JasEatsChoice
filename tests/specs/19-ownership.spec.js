// 模块十九：资源归属校验（#597）
//
// 既有 18 个 spec 全部用同一个账号（17322222222 同时是用户与商家），
// 这种安排无法证伪归属校验——同一个令牌打自己的资源永远合法。
// 本 spec 用**无商家账号**（13800138010，usr_user.merchant_id 为 NULL）的令牌，
// 去打拥有者（17322222222，绑定 merchantId 7638432224340229）的资源。
//
// 响应约定（记牢，否则会假绿）：
//   ResponseResult.fail("403", ...) → HTTP 200 + success:false + code:"403"
//   单资源越权按"不存在"处理 → HTTP 200 + success:false + code:"404"
// 所以断言必须同时看 success 与 code；只看 HTTP 状态码会把 200 当成通过。
const { test, expect } = require('../fixtures/test-base')
const { markRunning, markPassed, markFailed, updateSummary } = require('../utils/progress')
const { TEST_API_BASE } = require('../utils/api')

const API_BASE = TEST_API_BASE.replace(/\/$/, '')

// dev 栈 CAPTCHA_TEST_FIXED_CODE=8888，与 tests/utils/auth-token.js 同一口径。
const FIXED_CAPTCHA = process.env.TEST_CAPTCHA_FIXED_CODE || '8888'

// 种子里的拥有者：账号 17322222222，userId 3384650106421960，绑定商家 7638432224340229
const OWNER = {
  phone: '17322222222',
  password: 'asdasd',
  userId: '3384650106421960',
  merchantId: '7638432224340229'
}
// 种子里的无商家账号：userId 4590322501655448，usr_user.merchant_id 为 NULL
const PLAIN_ACCOUNT = { phone: '13800138010', password: 'asdasd', userId: '4590322501655448' }
const OWNER_MERCHANT_ID = OWNER.merchantId
// 种子中属于该商家的一个真实会话，用于 19.7 的越权"清除未读"
const OWNER_CHAT_SESSION_ID = 'Smerchant20260520_01'

let ownerToken = null
let plainToken = null
let plainUserId = null

/**
 * 发一个请求并原样返回响应信封。
 *
 * 与 11-merchant-stats-ai.spec.js 里的 apiRequest 不同：越权用例**不能**在这里先断言
 * success !== false，否则期望失败的请求会先在辅助函数里把自己断言死。
 */
async function apiJson(request, path, { token, method = 'GET', data } = {}) {
  const response = await request.fetch(`${API_BASE}${path}`, {
    method,
    // 显式放宽到 30s：机器有负载时单次请求会偶发卡住，
    // 而本 spec 要证伪的是归属校验，不该被基础设施抖动带成红。
    timeout: 30000,
    headers: {
      Accept: 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    ...(data === undefined ? {} : { data })
  })
  const body = await response.json().catch(() => null)
  return { httpStatus: response.status(), body }
}

function describeEnvelope(result) {
  if (!result.body) {
    return `非 JSON 响应（HTTP ${result.httpStatus}）`
  }
  return `success=${result.body.success}、code=${result.body.code}、message=${result.body.message}`
}

/** 断言"越权被拒"：同时校验 success === false 与 code，不看 HTTP 状态码。 */
function expectFailure(result, path, expectedCode) {
  expect(result.body, `接口 ${path} 应返回 JSON，实际 ${describeEnvelope(result)}`).not.toBeNull()
  expect(result.body.success, `接口 ${path} 应返回 success=false，实际 ${describeEnvelope(result)}`).toBe(false)
  expect(String(result.body.code), `接口 ${path} 期望 code=${expectedCode}，实际 ${describeEnvelope(result)}`).toBe(expectedCode)
}

/** 断言"合法调用方被放行"，并返回 data。 */
function expectSuccess(result, path) {
  expect(result.body, `接口 ${path} 应返回 JSON，实际 ${describeEnvelope(result)}`).not.toBeNull()
  expect(result.body.success, `接口 ${path} 应返回 success=true，实际 ${describeEnvelope(result)}`).toBe(true)
  if (result.body.code !== undefined && result.body.code !== null) {
    expect(String(result.body.code), `接口 ${path} 应返回 code=200，实际 ${describeEnvelope(result)}`).toBe('200')
  }
  return result.body.data
}

/** 用拥有者令牌请求一个应当成功的接口。 */
async function ownerGet(request, path) {
  return expectSuccess(await apiJson(request, path, { token: ownerToken }), path)
}

/**
 * 用固定验证码走 API 登录，取回令牌与登录者档案。
 *
 * 与 tests/utils/auth-token.js 的 userToken() 同一套做法。这里不走 UI 登录：
 * 本 spec 要证伪的是后端归属校验，登录本身已有 01-auth 覆盖，而前端 SPA 冷启动在
 * 机器有负载时会超过 loginAsUser 的 20s waitForURL 上限（实测出现过连续超时，
 * 把 beforeAll 一起拖挂），用 API 登录可以让这份 spec 只对被测的归属校验负责。
 */
async function loginViaApi(request, account) {
  const result = await apiJson(request, '/v1/users/login', {
    method: 'POST',
    data: {
      phone: account.phone,
      password: account.password,
      captcha: FIXED_CAPTCHA,
      checkCodeKey: 'e2e-ownership'
    }
  })
  return expectSuccess(result, `/v1/users/login（${account.phone}）`)
}

test.describe('模块十九：资源归属校验', () => {

  // 默认 30s 对 beforeAll 里的两次登录偏紧（机器有负载时单次登录偶发卡住）。
  test.describe.configure({ timeout: 60000 })

  test.beforeAll(async ({ request }) => {
    // 两个身份的令牌都来自真实登录，并当场核对种子身份，避免账号绑定漂移后
    // 这份 spec 变成"两个管理员互打"的假绿。
    const ownerSession = await loginViaApi(request, OWNER)
    const plainSession = await loginViaApi(request, PLAIN_ACCOUNT)

    ownerToken = ownerSession.token
    plainToken = plainSession.token
    plainUserId = String(plainSession.user?.userId ?? '')

    expect(ownerToken, '拥有者令牌应获取成功').toBeTruthy()
    expect(plainToken, '无商家账号令牌应获取成功').toBeTruthy()
    expect(String(ownerSession.user?.userId), '拥有者 userId 应与种子一致').toBe(OWNER.userId)
    expect(String(ownerSession.user?.merchantId), '拥有者应绑定种子商家').toBe(OWNER.merchantId)
    expect(plainUserId, '无商家账号 userId 应与种子一致').toBe(PLAIN_ACCOUNT.userId)
    expect(
      plainSession.user?.merchantId,
      '无商家账号的 merchantId 必须为空，否则本 spec 失去证伪能力'
    ).toBeFalsy()
    expect(plainUserId, '攻击方不能就是拥有者本人').not.toBe(OWNER.userId)
  })

  test('19.1 越权读取商家订单', async ({ request }) => {
    markRunning('19.1')
    try {
      const path = `/v1/orders/merchant/${OWNER_MERCHANT_ID}?today=false`
      expectFailure(await apiJson(request, path, { token: plainToken }), path, '403')

      markPassed('19.1', '无商家账号读他人商家订单返回 code=403')
    } catch (e) { markFailed('19.1', e.message.slice(0, 50)); throw e }
  })

  test('19.2 越权读取商家经营统计', async ({ request }) => {
    markRunning('19.2')
    try {
      const path = `/v1/merchant/${OWNER_MERCHANT_ID}/statistics?timeRange=today`
      expectFailure(await apiJson(request, path, { token: plainToken }), path, '403')

      markPassed('19.2', '无商家账号读他人经营统计返回 code=403')
    } catch (e) { markFailed('19.2', e.message.slice(0, 50)); throw e }
  })

  test('19.3 越权读取商家营业概览', async ({ request }) => {
    markRunning('19.3')
    try {
      const path = `/v1/merchant/${OWNER_MERCHANT_ID}/business-overview`
      expectFailure(await apiJson(request, path, { token: plainToken }), path, '403')

      markPassed('19.3', '无商家账号读他人营业概览返回 code=403')
    } catch (e) { markFailed('19.3', e.message.slice(0, 50)); throw e }
  })

  test('19.4 越权翻转商家营业状态', async ({ request }) => {
    markRunning('19.4')
    try {
      const readPath = `/v1/merchant/${OWNER_MERCHANT_ID}`
      const attackPath = `/v1/merchant/${OWNER_MERCHANT_ID}/status`

      const before = await ownerGet(request, readPath)
      expect(before.status, '商家详情应返回营业状态 status').not.toBeUndefined()

      const attack = await apiJson(request, attackPath, { token: plainToken, method: 'PUT' })
      const after = await ownerGet(request, readPath)

      // 先查副作用：无论返回什么码，营业状态都不该被别人的请求翻转。
      expect(after.status, `越权 PUT 后营业状态被改变：${before.status} -> ${after.status}`).toBe(before.status)
      expectFailure(attack, attackPath, '403')

      markPassed('19.4', `返回 code=403，营业状态保持 ${after.status} 未被翻转`)
    } catch (e) { markFailed('19.4', e.message.slice(0, 50)); throw e }
  })

  test('19.5 越权读取商家评价统计', async ({ request }) => {
    markRunning('19.5')
    try {
      const path = `/v1/reviews/merchant/${OWNER_MERCHANT_ID}/statistics`
      expectFailure(await apiJson(request, path, { token: plainToken }), path, '403')

      markPassed('19.5', '无商家账号读他人评价统计返回 code=403')
    } catch (e) { markFailed('19.5', e.message.slice(0, 50)); throw e }
  })

  test('19.6 越权读取商家会话列表', async ({ request }) => {
    markRunning('19.6')
    try {
      const path = `/v1/chat/users/${OWNER_MERCHANT_ID}/chat-sessions`
      expectFailure(await apiJson(request, path, { token: plainToken }), path, '403')

      markPassed('19.6', '无商家账号读他人会话列表返回 code=403')
    } catch (e) { markFailed('19.6', e.message.slice(0, 50)); throw e }
  })

  test('19.7 越权清除他人会话未读', async ({ request }) => {
    markRunning('19.7')
    try {
      const path = `/v1/chat/sessions/${OWNER_CHAT_SESSION_ID}/unread-clear`
      const result = await apiJson(request, path, {
        token: plainToken,
        method: 'POST',
        data: { userId: OWNER_MERCHANT_ID }
      })

      // 会话真实存在（拥有者侧可读），所以 404 只可能是归属校验拦下来的。
      const ownerSessions = await ownerGet(request, `/v1/chat/users/${OWNER_MERCHANT_ID}/chat-sessions`)
      expect(
        (ownerSessions || []).map(session => session.id),
        `种子会话 ${OWNER_CHAT_SESSION_ID} 应真实存在，否则 404 无法证明归属校验`
      ).toContain(OWNER_CHAT_SESSION_ID)

      expectFailure(result, path, '404')

      markPassed('19.7', `真实存在的他人会话返回 code=404（${OWNER_CHAT_SESSION_ID}）`)
    } catch (e) { markFailed('19.7', e.message.slice(0, 50)); throw e }
  })

  test('19.8 越权读取他人评价详情', async ({ request }) => {
    markRunning('19.8')
    try {
      const reviews = await ownerGet(request, `/v1/reviews/merchant/${OWNER_MERCHANT_ID}`)
      // 评价详情的判据是"作者本人或该商家"，所以要挑一条**不是**攻击方写的评价，
      // 否则攻击方以作者身份访问是合法的，断言 404 会变成误判。
      const target = (reviews || []).find(review => review.id && review.userId !== plainUserId)
      expect(
        target,
        `种子商家 ${OWNER_MERCHANT_ID} 下应有一条不属于无商家账号（${plainUserId}）的评价，否则无法验证越权`
      ).toBeTruthy()

      const path = `/v1/reviews/${target.id}`
      // 先证明这条评价对合法调用方（该商家）可读，排除"根本不存在"导致的假 404。
      await ownerGet(request, path)
      expectFailure(await apiJson(request, path, { token: plainToken }), path, '404')

      markPassed('19.8', `他人评价 ${target.id} 对合法商家可读，对无商家账号返回 code=404`)
    } catch (e) { markFailed('19.8', e.message.slice(0, 50)); throw e }
  })

  test('19.9 越权修改他人菜品', async ({ request }) => {
    markRunning('19.9')
    try {
      const dishes = await ownerGet(request, `/v1/dishes/merchant/${OWNER_MERCHANT_ID}`)
      const target = (dishes || []).find(dish => dish.id && String(dish.merchantId) === OWNER_MERCHANT_ID)
      expect(target, `种子商家 ${OWNER_MERCHANT_ID} 下应有菜品，否则无法验证越权`).toBeTruthy()

      const path = `/v1/dishes/${target.id}`
      const before = await ownerGet(request, path)
      const attack = await apiJson(request, path, {
        token: plainToken,
        method: 'PUT',
        data: { name: `${before.name}-越权` }
      })
      const after = await ownerGet(request, path)

      // 先查副作用：越权请求不该改动菜品数据。
      expect(after.name, `越权 PUT 后菜品名称被改变：${before.name} -> ${after.name}`).toBe(before.name)
      expectFailure(attack, path, '403')

      markPassed('19.9', `菜品 ${target.id} 返回 code=403，名称保持 ${after.name} 未被改动`)
    } catch (e) { markFailed('19.9', e.message.slice(0, 50)); throw e }
  })

  test('19.10 越权批量删除他人菜品', async ({ request }) => {
    markRunning('19.10')
    try {
      const dishes = await ownerGet(request, `/v1/dishes/merchant/${OWNER_MERCHANT_ID}`)
      const target = (dishes || []).find(dish => dish.id && String(dish.merchantId) === OWNER_MERCHANT_ID)
      expect(target, `种子商家 ${OWNER_MERCHANT_ID} 下应有菜品，否则无法验证越权`).toBeTruthy()

      const path = '/v1/dishes/batch'
      // 请求体形态与商家端 DishManagement.vue 一致：dish.id 是字符串，前端原样塞进 dishIds。
      const attack = await apiJson(request, path, {
        token: plainToken,
        method: 'PUT',
        data: { dishIds: [target.id] }
      })

      // 先查副作用：越权批量删除不该真的删掉菜品。
      const after = await apiJson(request, `/v1/dishes/${target.id}`, { token: ownerToken })
      expect(
        String(after.body?.data?.id || ''),
        `越权批量删除请求把菜品 ${target.id} 删掉了（${describeEnvelope(after)}）`
      ).toBe(String(target.id))

      expectFailure(attack, path, '403')

      markPassed('19.10', `菜品 ${target.id} 未被删除，批量删除返回 code=403`)
    } catch (e) { markFailed('19.10', e.message.slice(0, 50)); throw e }
  })

  test('19.11 越权读取他人想吃项', async ({ request }) => {
    markRunning('19.11')
    let createdItemId = null
    try {
      // 种子拥有者的想吃列表为空，无法取现成样本，按任务允许自建一条并清理。
      const created = await apiJson(request, '/v1/wish-list/item', {
        token: ownerToken,
        method: 'POST',
        data: { dishName: `e2e19-越权样本-${Date.now()}`, description: '模块十九越权用例自建样本' }
      })
      createdItemId = expectSuccess(created, '/v1/wish-list/item（自建样本）')
      expect(createdItemId, '自建想吃项应返回 itemId').toBeTruthy()

      const path = `/v1/wish-list/item/${createdItemId}`
      // 先证明该想吃项对拥有者本人可读，排除"根本没建成"导致的假 404。
      await ownerGet(request, path)
      expectFailure(await apiJson(request, path, { token: plainToken }), path, '404')

      markPassed('19.11', `自建想吃项 ${createdItemId} 属拥有者，无商家账号读取返回 code=404`)
    } finally {
      if (createdItemId) {
        // 清理自建数据：该接口只有"撤回"语义（auditStatus=7），没有硬删除路由。
        await apiJson(request, `/v1/wish-list/item/${createdItemId}`, {
          token: ownerToken,
          method: 'DELETE'
        }).catch(() => null)
      }
    }
  })

  test('19.12 拥有者令牌重放同一批接口（正对照）', async ({ request }) => {
    markRunning('19.12')
    try {
      const paths = [
        `/v1/orders/merchant/${OWNER_MERCHANT_ID}?today=false`,
        `/v1/merchant/${OWNER_MERCHANT_ID}/statistics?timeRange=today`,
        `/v1/reviews/merchant/${OWNER_MERCHANT_ID}/statistics`
      ]

      for (const path of paths) {
        // 与 19.1/19.2/19.5 完全相同的请求，只把令牌换成拥有者，必须被放行。
        expectSuccess(await apiJson(request, path, { token: ownerToken }), path)
      }

      markPassed('19.12', '拥有者令牌重放 19.1/19.2/19.5 三个接口全部 success=true')
    } catch (e) { markFailed('19.12', e.message.slice(0, 50)); throw e }
  })

  test.afterAll(() => { updateSummary() })
})
