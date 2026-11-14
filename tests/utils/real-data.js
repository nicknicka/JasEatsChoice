// E2E 真实前置数据 helper：统一从运行中的测试后端 API 获取。
const { execFileSync } = require('child_process')
const path = require('path')
const { TEST_API_BASE } = require('./api')
const { userToken } = require('./auth-token')

const DEFAULT_USER_PHONE = process.env.TEST_USER_PHONE || process.env.TEST_USER || '17322222222'

function buildUrl(path) {
  const base = TEST_API_BASE.replace(/\/$/, '')
  return `${base}${path.startsWith('/') ? path : `/${path}`}`
}

// SecurityConfig 默认拒绝后，这些用户/商家/订单数据接口都需要登录态。
async function request(path, options = {}) {
  const token = await userToken()
  const response = await fetch(buildUrl(path), {
    ...options,
    headers: {
      Accept: 'application/json',
      ...(options.headers || {}),
      Authorization: `Bearer ${token}`
    }
  })

  const result = await response.json().catch(() => null)
  if (!response.ok || !result || result.success !== true) {
    const message = result?.message || response.statusText || '未知错误'
    throw new Error(`请求 ${path} 失败（${response.status}）：${message}`)
  }

  return result.data
}

function idOf(item, fields, label) {
  for (const field of fields) {
    if (item?.[field] !== undefined && item[field] !== null && item[field] !== '') {
      return String(item[field])
    }
  }
  throw new Error(`${label}接口未返回可用 ID`)
}

function byId(items, id, fields, label) {
  const normalizedId = String(id)
  const item = (items || []).find((candidate) =>
    fields.some((field) => candidate?.[field] !== undefined && String(candidate[field]) === normalizedId)
  )
  if (!item) {
    throw new Error(`${label} ${normalizedId} 不存在`)
  }
  return item
}

async function getUser({ userId, phone = DEFAULT_USER_PHONE } = {}) {
  if (userId) {
    return request(`/v1/users/${encodeURIComponent(userId)}`)
  }

  const query = new URLSearchParams({ keyword: phone, searchType: 'phone' })
  const users = await request(`/v1/users/search?${query}`)
  const user = (users || []).find((candidate) => candidate?.phone === phone) || users?.[0]
  if (!user) {
    throw new Error(`未找到测试用户：${phone}`)
  }
  return user
}

async function getUserId(options = {}) {
  return idOf(await getUser(options), ['userId', 'id'], '用户')
}

async function getMerchant({ merchantId, user } = {}) {
  let resolvedMerchantId = merchantId
  if (!resolvedMerchantId) {
    const resolvedUser = user || await getUser()
    resolvedMerchantId = resolvedUser?.merchantId
  }

  if (!resolvedMerchantId) {
    const merchants = await request('/v1/merchants?page=1&size=100')
    const merchant = merchants?.[0]
    if (!merchant) {
      throw new Error('商家列表为空，无法获取 merchantId')
    }
    return merchant
  }

  return request(`/v1/merchants/${encodeURIComponent(resolvedMerchantId)}`)
}

async function getMerchantId(options = {}) {
  return idOf(await getMerchant(options), ['merchantId', 'id'], '商家')
}

async function getDish({ merchantId, dishId } = {}) {
  if (dishId) {
    return request(`/v1/dishes/${encodeURIComponent(dishId)}`)
  }

  const resolvedMerchantId = await getMerchantId({ merchantId })
  const menus = await request(`/v1/menus/merchants/${encodeURIComponent(resolvedMerchantId)}/menu`)
  const candidates = (menus || [])
    .flatMap((menu) => menu?.dishes || [])
    .filter((candidate) => candidate?.id && String(candidate.id) !== '1' && candidate.status !== false)
  const dish = candidates.find((candidate) => String(candidate.id) === '1001')
    || candidates.find((candidate) => String(candidate.id).startsWith('seed_'))
    || candidates[0]

  if (!dish) {
    throw new Error(`商家 ${resolvedMerchantId} 没有可用的非 1 菜品`)
  }
  return dish
}

async function getDishId(options = {}) {
  return idOf(await getDish(options), ['id', 'dishId'], '菜品')
}

async function getOrder({ orderId, userId, merchantId, status } = {}) {
  if (orderId) {
    return request(`/v1/orders/${encodeURIComponent(orderId)}`)
  }

  let orders
  if (merchantId) {
    orders = await request(`/v1/orders/merchant/${encodeURIComponent(merchantId)}?today=false`)
  } else {
    const resolvedUserId = await getUserId({ userId })
    orders = await request(`/v1/orders/user/${encodeURIComponent(resolvedUserId)}`)
  }

  const order = (orders || []).find((candidate) =>
    status === undefined || String(candidate?.status) === String(status)
  )
  if (!order) {
    const scope = merchantId ? `商家 ${merchantId}` : `用户 ${userId || DEFAULT_USER_PHONE}`
    const statusText = status === undefined ? '' : `、状态 ${status}`
    throw new Error(`${scope}没有匹配订单${statusText}`)
  }
  return order
}

async function getOrderId(options = {}) {
  return idOf(await getOrder(options), ['id', 'orderId'], '订单')
}

async function getAddress({ userId, addressId, isDefault = true } = {}) {
  const resolvedUserId = await getUserId({ userId })
  const query = new URLSearchParams({ userId: resolvedUserId })
  const addresses = await request(`/v1/addresses/user?${query}`)

  if (addressId) {
    return byId(addresses, addressId, ['id', 'addressId'], '地址')
  }

  const matchesDefault = (candidate) => isDefault === true
    ? candidate?.isDefault === 1 || candidate?.isDefault === true
    : candidate?.isDefault === 0 || candidate?.isDefault === false
  const address = (addresses || []).find(matchesDefault) || (isDefault ? addresses?.[0] : null)
  if (!address) {
    throw new Error(`用户 ${resolvedUserId} 没有地址`)
  }
  return address
}

async function getAddressId(options = {}) {
  return idOf(await getAddress(options), ['id', 'addressId'], '地址')
}

async function getCoupon({ userId, couponId, status = 'available', issueIfMissing = false } = {}) {
  const resolvedUserId = await getUserId({ userId })
  const query = new URLSearchParams({ userId: resolvedUserId })
  if (!couponId && status) {
    query.set('status', status)
  }

  const coupons = await request(`/v1/coupons/user?${query}`)
  if (couponId) {
    return byId(coupons, couponId, ['id', 'couponId'], '优惠券')
  }

  if (coupons?.[0]) {
    return coupons[0]
  }

  if (issueIfMissing && (!status || status === 'available')) {
    const issueQuery = new URLSearchParams({ userId: resolvedUserId })
    return request(`/v1/coupons/issue-test?${issueQuery}`, { method: 'POST' })
  }

  throw new Error(`用户 ${resolvedUserId} 没有可用优惠券`)
}

async function getCouponId(options = {}) {
  return idOf(await getCoupon(options), ['id', 'couponId'], '优惠券')
}

/**
 * 在运行中的测试库里登记一个不会自动调度的临时任务，
 * 用于通过既有执行接口触发后端的真实定时方法（例如定时推荐推送）。
 * 返回任务 ID，调用方必须在 finally 中用 deleteEphemeralTask 清理。
 */
function createEphemeralTask({ id, taskName, className, methodName }) {
  const normalizedId = String(id)
  if (!/^[A-Za-z0-9_-]+$/.test(normalizedId)) {
    throw new Error(`临时任务 ID 非法：${normalizedId}`)
  }

  const repoRoot = path.resolve(__dirname, '../..')
  const sql = [
    'INSERT INTO sys_scheduled_task',
    '(id, task_name, task_code, task_type, status, task_class_name, task_method_name, log_execution, priority)',
    `VALUES ('${normalizedId}', '${taskName}', '${normalizedId}', 'ONCE', 'DISABLED',`,
    `'${className}', '${methodName}', 0, 3)`
  ].join(' ')

  execFileSync('docker', [
    'compose',
    '--env-file', path.join(repoRoot, 'deploy/.env'),
    '-f', path.join(repoRoot, 'deploy/compose.yml'),
    '-f', path.join(repoRoot, 'deploy/compose.dev.yml'),
    'exec', '-T', 'mysql', 'sh', '-c',
    `mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "${sql}"`
  ], {
    cwd: repoRoot,
    encoding: 'utf8',
    stdio: ['ignore', 'pipe', 'pipe'],
    env: { ...process.env, COMPOSE_PROJECT_NAME: process.env.COMPOSE_PROJECT_NAME || 'jaseats' }
  }).trim()

  return normalizedId
}

/**
 * 删除 createEphemeralTask 登记的临时任务及其执行日志。
 */
function deleteEphemeralTask(taskId) {
  const normalizedId = String(taskId)
  if (!/^[A-Za-z0-9_-]+$/.test(normalizedId)) {
    throw new Error(`临时任务 ID 非法：${normalizedId}`)
  }

  const repoRoot = path.resolve(__dirname, '../..')
  const sql = `DELETE FROM sys_scheduled_task_log WHERE task_id = '${normalizedId}'; ` +
    `DELETE FROM sys_scheduled_task WHERE id = '${normalizedId}';`

  execFileSync('docker', [
    'compose',
    '--env-file', path.join(repoRoot, 'deploy/.env'),
    '-f', path.join(repoRoot, 'deploy/compose.yml'),
    '-f', path.join(repoRoot, 'deploy/compose.dev.yml'),
    'exec', '-T', 'mysql', 'sh', '-c',
    `mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "${sql}"`
  ], {
    cwd: repoRoot,
    encoding: 'utf8',
    stdio: ['ignore', 'pipe', 'pipe'],
    env: { ...process.env, COMPOSE_PROJECT_NAME: process.env.COMPOSE_PROJECT_NAME || 'jaseats' }
  }).trim()
}

function deleteCoupon(couponId) {
  const normalizedId = String(couponId)
  if (!/^\d+$/.test(normalizedId)) {
    throw new Error(`临时优惠券 ID 非法：${normalizedId}`)
  }

  const repoRoot = path.resolve(__dirname, '../..')
  const sql = `DELETE FROM usr_user_coupon WHERE id = '${normalizedId}'; SELECT ROW_COUNT();`
  const output = execFileSync('docker', [
    'compose',
    '--env-file', path.join(repoRoot, 'deploy/.env'),
    '-f', path.join(repoRoot, 'deploy/compose.yml'),
    '-f', path.join(repoRoot, 'deploy/compose.dev.yml'),
    'exec', '-T', 'mysql', 'sh', '-c',
    `mysql -N -B -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "${sql}"`
  ], {
    cwd: repoRoot,
    encoding: 'utf8',
    stdio: ['ignore', 'pipe', 'pipe'],
    env: { ...process.env, COMPOSE_PROJECT_NAME: process.env.COMPOSE_PROJECT_NAME || 'jaseats' }
  }).trim()

  if (output !== '1') {
    throw new Error(`临时优惠券 ${normalizedId} 清理失败，影响行数：${output || 0}`)
  }
}

module.exports = {
  getUser,
  getUserId,
  getMerchant,
  getMerchantId,
  getDish,
  getDishId,
  getOrder,
  getOrderId,
  getAddress,
  getAddressId,
  getCoupon,
  getCouponId,
  deleteCoupon,
  createEphemeralTask,
  deleteEphemeralTask
}
