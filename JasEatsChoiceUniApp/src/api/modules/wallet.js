/**
 * 钱包相关API
 * 对接后端 WalletController
 * 基础路径: /v1/wallet
 */
import { get, post, put } from '@/utils/request'

export const walletApi = {
  /**
   * 获取钱包信息
   * GET /v1/wallet
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   */
  getInfo: (params) => get('/v1/wallet', params),

  /**
   * 获取余额
   * GET /v1/wallet/balance
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   */
  getBalance: (params) => get('/v1/wallet/balance', params),

  /**
   * 获取积分
   * GET /v1/wallet/points
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
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
   */
  getRedpackets: (params) => get('/v1/wallet/redpackets', params),

  /**
   * 获取交易记录
   * GET /v1/wallet/transactions
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   * @param {string} params.type - 交易类型(recharge/withdraw/consume/refund/reward)
   */
  getTransactions: (params) => get('/v1/wallet/transactions', params),

  /**
   * 充值
   * POST /v1/wallet/recharge
   * @param {Object} data - 充值数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.amount - 充值金额
   * @param {string} data.paymentMethod - 支付方式(alipay/wechat/balance)
   */
  recharge: (data) => post('/v1/wallet/recharge', data),

  /**
   * 提现
   * POST /v1/wallet/withdraw
   * @param {Object} data - 提现数据
   * @param {string} data.userId - 用户ID
   * @param {number} data.amount - 提现金额
   * @param {string} data.account - 提现账户
   * @param {string} data.accountType - 账户类型(alipay/wechat/bank)
   * @param {string} data.realName - 真实姓名
   */
  withdraw: (data) => post('/v1/wallet/withdraw', data),

  /**
   * 转账
   * POST /v1/wallet/transfer
   * @param {Object} data - 转账数据
   * @param {string} data.fromUserId - 转出用户ID
   * @param {string} data.toUserId - 转入用户ID
   * @param {number} data.amount - 转账金额
   * @param {string} data.remark - 备注
   */
  transfer: (data) => post('/v1/wallet/transfer', data),

  /**
   * 获取积分记录
   * GET /v1/wallet/points/records
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
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
   */
  exchangePoints: (data) => post('/v1/wallet/points/exchange', data),

  /**
   * 获取充值套餐
   * GET /v1/wallet/recharge/packages
   */
  getRechargePackages: () => get('/v1/wallet/recharge/packages'),

  /**
   * 获取提现记录
   * GET /v1/wallet/withdraw/records
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.page - 页码
   * @param {number} params.size - 每页数量
   */
  getWithdrawRecords: (params) => get('/v1/wallet/withdraw/records', params),

  /**
   * 取消提现
   * PUT /v1/wallet/withdraw/{id}/cancel
   * @param {string} id - 提现记录ID
   * @param {Object} data - 数据
   * @param {string} data.userId - 用户ID
   */
  cancelWithdraw: (id, data) => put(`/v1/wallet/withdraw/${id}/cancel`, data),

  /**
   * 微信支付
   * POST /v1/payment/wechat
   * @param {Object} data - 支付数据
   * @param {string} data.orderId - 订单ID
   * @param {string} data.prepayId - 预支付ID
   */
  wechatPay: (data) => post('/v1/payment/wechat', data),

  /**
   * 支付宝支付
   * POST /v1/payment/alipay
   * @param {Object} data - 支付数据
   * @param {string} data.orderId - 订单ID
   */
  alipayPay: (data) => post('/v1/payment/alipay', data)
}

export default walletApi
