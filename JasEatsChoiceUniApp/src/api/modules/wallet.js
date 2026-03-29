/**
 * 钱包相关API
 * 对接后端 WalletController
 * 基础路径: /v1/wallet
 */
import { get, post, put } from '@/utils/request'
import { WALLET_API, PAYMENT_API, buildUrl } from '../urlEnum'

export const walletApi = {
  /**
   * 获取钱包信息
   * GET /v1/wallet/{userId}
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回钱包信息
   */
  getWallet: (userId) => get(buildUrl(WALLET_API.GET_WALLET, { userId })),

  /**
   * 获取钱包信息（别名）
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回钱包信息
   */
  getInfo: (params) => get('/v1/wallet', params),

  /**
   * 获取余额
   * GET /v1/wallet/{userId}/balance
   * @param {string} userId - 用户ID
   * @returns {Promise} 返回余额
   */
  getBalance: (userId) => get(buildUrl(WALLET_API.GET_BALANCE, { userId })),

  /**
   * 获取余额（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回余额
   */
  getBalanceOld: (params) => get('/v1/wallet/balance', params),

  /**
   * 获取积分
   * GET /v1/wallet/points
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @returns {Promise} 返回积分
   */
  getPoints: (params) => get('/v1/wallet/points', params),

  /**
   * 获取红包列表
   * GET /v1/wallet/redpackets
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.status - 状态(available/used/expired)
   * @returns {Promise} 返回红包列表
   */
  getRedpackets: (params) => get('/v1/wallet/redpackets', params),

  /**
   * 获取交易记录
   * GET /v1/wallet/{userId}/transactions
   * @param {string} userId - 用户ID
   * @param {Object} params - 查询参数
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 交易类型(recharge/withdraw/consume/refund/reward)
   * @returns {Promise} 返回交易记录
   */
  getTransactions: (userId, params) => get(buildUrl(WALLET_API.GET_TRANSACTIONS, { userId }), params),

  /**
   * 获取交易记录（别名）
   * @param {Object} params - 查询参数
   * @returns {Promise} 返回交易记录
   */
  getTransactionsOld: (params) => get('/v1/wallet/transactions', params),

  /**
   * 充值
   * POST /v1/wallet/{userId}/recharge
   * @param {string} userId - 用户ID
   * @param {Object} data - 充值数据
   * @param {number} data.amount - 充值金额
   * @param {string} data.paymentMethod - 支付方式(alipay/wechat/balance)
   * @returns {Promise} 返回充值结果
   */
  recharge: (userId, data) => post(buildUrl(WALLET_API.RECHARGE, { userId }), data),

  /**
   * 充值（别名）
   * @param {Object} data - 充值数据
   * @returns {Promise} 返回充值结果
   */
  rechargeOld: (data) => post('/v1/wallet/recharge', data),

  /**
   * 提现
   * POST /v1/wallet/{userId}/withdraw
   * @param {string} userId - 用户ID
   * @param {Object} data - 提现数据
   * @param {number} data.amount - 提现金额
   * @param {string} data.account - 提现账户
   * @param {string} data.accountType - 账户类型(alipay/wechat/bank)
   * @param {string} data.realName - 真实姓名
   * @returns {Promise} 返回提现结果
   */
  withdraw: (userId, data) => post(buildUrl(WALLET_API.WITHDRAW, { userId }), data),

  /**
   * 提现（别名）
   * @param {Object} data - 提现数据
   * @returns {Promise} 返回提现结果
   */
  withdrawOld: (data) => post('/v1/wallet/withdraw', data),

  /**
   * 转账
   * POST /v1/wallet/transfer
   * @param {Object} data - 转账数据
   * @param {string} data.fromUserId - 转出用户ID
   * @param {string} data.toUserId - 转入用户ID
   * @param {number} data.amount - 转账金额
   * @param {string} data.remark - 备注
   * @returns {Promise} 返回转账结果
   */
  transfer: (data) => post('/v1/wallet/transfer', data),

  /**
   * 获取积分记录
   * GET /v1/wallet/points/records
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回积分记录
   */
  getPointsRecords: (params) => get('/v1/wallet/points/records', params),

  /**
   * 积分兑换
   * POST /v1/wallet/points/exchange
   * @param {Object} data - 兑换数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.points - 兑换积分数量
   * @param {string} data.target - 兑换目标(balance/coupon/gift)
   * @param {string} data.targetId - 目标ID
   * @returns {Promise} 返回兑换结果
   */
  exchangePoints: (data) => post('/v1/wallet/points/exchange', data),

  /**
   * 获取充值套餐
   * GET /v1/wallet/recharge/packages
   * @returns {Promise} 返回充值套餐
   */
  getRechargePackages: () => get('/v1/wallet/recharge/packages'),

  /**
   * 获取提现记录
   * GET /v1/wallet/withdraw/records
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @returns {Promise} 返回提现记录
   */
  getWithdrawRecords: (params) => get('/v1/wallet/withdraw/records', params),

  /**
   * 取消提现
   * PUT /v1/wallet/withdraw/{id}/cancel
   * @param {string} id - 提现记录ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   * @returns {Promise} 返回取消结果
   */
  cancelWithdraw: (id, data) => put(`/v1/wallet/withdraw/${id}/cancel`, data),

  // ==================== 支付相关 ====================

  /**
   * 创建支付
   * POST /v1/payment/create
   * @param {Object} data - 支付数据
   * @param {string} data.orderId - 订单ID
   * @param {string} data.paymentMethod - 支付方式
   * @param {number} data.amount - 支付金额
   * @returns {Promise} 返回支付结果
   */
  createPayment: (data) => post(PAYMENT_API.CREATE_PAYMENT, data),

  /**
   * 查询支付状态
   * POST /v1/payment/query
   * @param {Object} data - 查询数据
   * @param {string} data.orderId - 订单ID
   * @returns {Promise} 返回支付状态
   */
  queryPayment: (data) => post(PAYMENT_API.QUERY_PAYMENT, data),

  /**
   * 申请退款
   * POST /v1/payment/refund
   * @param {Object} data - 退款数据
   * @param {string} data.orderId - 订单ID
   * @param {number} data.amount - 退款金额
   * @param {string} data.reason - 退款原因
   * @returns {Promise} 返回退款结果
   */
  refund: (data) => post(PAYMENT_API.REFUND, data),

  /**
   * 微信支付
   * POST /v1/payment/wechat
   * @param {Object} data - 支付数据
   * @param {string} data.orderId - 订单ID
   * @param {string} data.prepayId - 预支付ID
   * @returns {Promise} 返回支付结果
   */
  wechatPay: (data) => post('/v1/payment/wechat', data),

  /**
   * 支付宝支付
   * POST /v1/payment/alipay
   * @param {Object} data - 支付数据
   * @param {string} data.orderId - 订单ID
   * @returns {Promise} 返回支付结果
   */
  alipayPay: (data) => post('/v1/payment/alipay', data)
}

export default walletApi
