/**
 * 钱包相关API
 * 对接后端 WalletController 和 ConsumeHistoryController
 */
import { get, post } from '@/utils/request'
import { WALLET_API, PAYMENT_API, buildUrl } from '../urlEnum'

const getCurrentUserId = () => {
  const userInfo = uni.getStorageSync('userInfo') || {}
  return userInfo.userId || userInfo.id || uni.getStorageSync('userId') || ''
}

const buildQueryUrl = (url, params = {}) => {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&')

  return query ? `${url}?${query}` : url
}

const buildTradeNo = (prefix) => `${prefix}${Date.now()}${Math.floor(Math.random() * 1000)}`

const normalizeWalletResponse = (response) => {
  const data = response?.data || {}
  return {
    ...response,
    balance: data.balance,
    points: data.points,
    redEnvelopes: data.redEnvelopes ?? data.redPackets ?? 0
  }
}

const normalizeTransactionResponse = (response) => {
  const pageData = response?.data || {}
  const list = pageData.records || []

  return {
    ...response,
    list,
    total: pageData.total || 0,
    data: {
      ...pageData,
      list
    }
  }
}

const resolveUserAndPayload = (userIdOrData, maybeData) => {
  if (typeof userIdOrData === 'object' || userIdOrData === undefined) {
    const payload = userIdOrData || {}
    return {
      userId: payload.userId || getCurrentUserId(),
      data: payload
    }
  }

  return {
    userId: userIdOrData || getCurrentUserId(),
    data: maybeData || {}
  }
}

const mapTransactionType = (type) => {
  if (!type || type === 'all') return 'all'
  if (type === 'income') return 'recharge'
  if (type === 'expense') return 'consume'
  return type
}

export const walletApi = {
  /**
   * 获取钱包信息
   * GET /v1/wallet/info/{userId}
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回钱包信息
   */
  getWallet: (userId) => get(
    buildUrl(WALLET_API.GET_WALLET, { userId: userId || getCurrentUserId() })
  ).then(normalizeWalletResponse),

  /**
   * 获取钱包信息（别名）
   * @param {Object|string} params - 查询参数或用户ID
   * @returns {Promise} 返回钱包信息
   */
  getInfo: (params = {}) => {
    const userId = typeof params === 'string' ? params : params.userId
    return walletApi.getWallet(userId || getCurrentUserId())
  },

  /**
   * 获取余额
   * GET /v1/wallet/balance/{userId}
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回余额
   */
  getBalance: (userId) => get(
    buildUrl(WALLET_API.GET_BALANCE, { userId: userId || getCurrentUserId() })
  ),

  /**
   * 获取余额（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回余额
   */
  getBalanceOld: (params = {}) => walletApi.getBalance(params.userId),

  /**
   * 获取积分
   * 当前后端未提供独立积分接口，先复用钱包信息
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回积分信息
   */
  getPoints: async (params = {}) => {
    const response = await walletApi.getWallet(params.userId)
    return {
      ...response,
      data: {
        ...response.data,
        points: response.data?.points || 0,
        integral: response.data?.points || 0
      }
    }
  },

  /**
   * 获取红包列表
   * 当前后端未提供红包接口，返回空列表避免请求旧路径
   * @returns {Promise<Array>} 返回空列表
   */
  getRedpackets: async () => [],

  /**
   * 获取交易记录
   * GET /v1/consume-history
   * @param {string|Object} userId - 用户ID，或直接传查询参数对象
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回交易记录
   */
  getTransactions: (userId, params) => {
    const query = typeof userId === 'object' || userId === undefined
      ? { ...(userId || {}) }
      : { ...(params || {}), userId }

    return get(WALLET_API.GET_TRANSACTIONS, {
      userId: query.userId || getCurrentUserId(),
      type: mapTransactionType(query.type),
      startDate: query.startDate,
      endDate: query.endDate,
      page: query.page || query.pageNum || 1,
      pageSize: query.pageSize || query.size || 10
    }).then(normalizeTransactionResponse)
  },

  /**
   * 获取交易记录（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回交易记录
   */
  getTransactionsOld: (params = {}) => walletApi.getTransactions(params),

  /**
   * 充值
   * POST /v1/wallet/recharge
   * @param {string|Object} userId - 用户ID或充值数据
   * @param {Object} data - 充值数据
   * @returns {Promise} 返回充值结果
   */
  recharge: (userId, data) => {
    const resolved = resolveUserAndPayload(userId, data)
    return post(buildQueryUrl(WALLET_API.RECHARGE, {
      userId: resolved.userId,
      amount: resolved.data.amount,
      rechargeNo: resolved.data.rechargeNo || buildTradeNo('RC')
    }))
  },

  /**
   * 充值（别名）
   * @param {Object} data - 充值数据
   * @returns {Promise} 返回充值结果
   */
  rechargeOld: (data) => walletApi.recharge(data),

  /**
   * 提现
   * POST /v1/wallet/withdraw
   * @param {string|Object} userId - 用户ID或提现数据
   * @param {Object} data - 提现数据
   * @returns {Promise} 返回提现结果
   */
  withdraw: (userId, data) => {
    const resolved = resolveUserAndPayload(userId, data)
    const accountInfo = resolved.data.accountInfo || resolved.data.account || resolved.data.realName || ''

    return post(buildQueryUrl(WALLET_API.WITHDRAW, {
      userId: resolved.userId,
      amount: resolved.data.amount,
      withdrawNo: resolved.data.withdrawNo || buildTradeNo('WD'),
      withdrawMethod: resolved.data.withdrawMethod || resolved.data.accountType || 'wechat',
      accountInfo
    }))
  },

  /**
   * 提现（别名）
   * @param {Object} data - 提现数据
   * @returns {Promise} 返回提现结果
   */
  withdrawOld: (data) => walletApi.withdraw(data),

  /**
   * 转账
   * 当前后端未提供转账接口
   * @returns {Promise}
   */
  transfer: () => Promise.reject(new Error('后端暂未提供转账接口')),

  /**
   * 获取积分记录
   * 当前后端未提供独立积分明细接口，先返回空数组
   * @returns {Promise<Array>} 返回空列表
   */
  getPointsRecords: async () => [],

  /**
   * 积分兑换
   * 当前后端未提供积分兑换接口
   * @returns {Promise}
   */
  exchangePoints: () => Promise.reject(new Error('后端暂未提供积分兑换接口')),

  /**
   * 获取充值套餐
   * 当前后端未提供充值套餐接口，返回固定选项
   * @returns {Promise<Array>} 返回默认套餐
   */
  getRechargePackages: async () => ([
    { amount: 20 },
    { amount: 50 },
    { amount: 100 },
    { amount: 200 }
  ]),

  /**
   * 获取提现记录
   * 当前后端未提供独立提现记录接口，复用消费记录筛选
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回提现记录
   */
  getWithdrawRecords: (params = {}) => walletApi.getTransactions({
    ...params,
    type: 'withdraw'
  }),

  /**
   * 取消提现
   * 当前后端未提供取消提现接口
   * @returns {Promise}
   */
  cancelWithdraw: () => Promise.reject(new Error('后端暂未提供取消提现接口')),

  // ==================== 支付相关 ====================

  createPayment: (data) => post(PAYMENT_API.CREATE_PAYMENT, data),

  queryPayment: (data) => post(PAYMENT_API.QUERY_PAYMENT, data),

  refund: (data) => post(PAYMENT_API.REFUND, data),

  wechatPay: (data) => post('/v1/payment/wechat', data),

  alipayPay: (data) => post('/v1/payment/alipay', data)
}

export default walletApi
