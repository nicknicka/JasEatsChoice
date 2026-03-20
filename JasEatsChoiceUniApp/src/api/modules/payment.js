/**
 * 支付相关API
 * 对接后端 PaymentController
 * 基础路径: /v1/payment
 */
import { get, post } from '@/utils/request'

export const paymentApi = {
  /**
   * PAY-001: 获取订单支付信息
   * GET /v1/payment/order/{orderId}
   * @param {string} orderId - 订单ID
   * @returns {Promise} 订单支付信息
   */
  getOrderInfo: (orderId) => get(`/v1/payment/order/${orderId}`),

  /**
   * PAY-002: 获取可用优惠券
   * GET /v1/payment/coupons
   * @param {Object} params - 查询参数
   * @param {string} params.userId - 用户ID
   * @param {number} params.orderAmount - 订单金额（可选）
   * @returns {Promise} 可用优惠券列表
   */
  getAvailableCoupons: (params) => get('/v1/payment/coupons', params),

  /**
   * PAY-003: 创建支付订单
   * POST /v1/payment/create
   * @param {Object} data - 支付数据
   * @param {string} data.orderId - 订单ID
   * @param {string} data.userId - 用户ID
   * @param {string} data.paymentMethod - 支付方式（wallet/wechat/alipay）
   * @param {string} data.couponId - 优惠券ID（可选）
   * @returns {Promise} 支付订单信息
   */
  createPayment: (data) => post('/v1/payment/create', data),

  /**
   * PAY-004: 微信支付
   * POST /v1/payment/wechat
   * @param {Object} data - 支付数据
   * @param {string} data.paymentNo - 支付流水号
   * @returns {Promise} 微信支付参数
   */
  wechatPay: (data) => post('/v1/payment/wechat', data),

  /**
   * PAY-005: 支付宝支付
   * POST /v1/payment/alipay
   * @param {Object} data - 支付数据
   * @param {string} data.paymentNo - 支付流水号
   * @returns {Promise} 支付宝支付参数
   */
  alipay: (data) => post('/v1/payment/alipay', data),

  /**
   * PAY-006: 余额支付
   * POST /v1/payment/balance
   * @param {Object} data - 支付数据
   * @param {string} data.paymentNo - 支付流水号
   * @param {string} data.paymentPassword - 支付密码（可选）
   * @returns {Promise} 支付结果
   */
  balancePay: (data) => post('/v1/payment/balance', data),

  /**
   * PAY-007: 查询支付状态
   * GET /v1/payment/status/{paymentNo}
   * @param {string} paymentNo - 支付流水号
   * @returns {Promise} 支付状态信息
   */
  getPaymentStatus: (paymentNo) => get(`/v1/payment/status/${paymentNo}`),

  /**
   * 查询订单支付状态（通过订单ID）
   * GET /v1/payment/order/{orderId}/status
   * @param {string} orderId - 订单ID
   * @returns {Promise} 支付状态信息
   */
  getOrderPaymentStatus: (orderId) => get(`/v1/payment/order/${orderId}/status`),

  /**
   * 轮询查询支付状态（用于前端实现支付状态轮询）
   * @param {string} paymentNo - 支付流水号
   * @param {Object} options - 轮询配置
   * @param {number} options.interval - 轮询间隔（毫秒），默认2000
   * @param {number} options.maxAttempts - 最大轮询次数，默认15
   * @param {Function} options.onSuccess - 支付成功回调
   * @param {Function} options.onFailed - 支付失败回调
   * @param {Function} options.onTimeout - 轮询超时回调
   * @returns {Promise} 轮询结果
   */
  pollPaymentStatus: (paymentNo, options = {}) => {
    const {
      interval = 2000,
      maxAttempts = 15,
      onSuccess,
      onFailed,
      onTimeout
    } = options

    return new Promise((resolve, reject) => {
      let attempts = 0

      const poll = async () => {
        attempts++

        try {
          const response = await paymentApi.getPaymentStatus(paymentNo)

          // ✅ 修复：适配修复后的响应格式
          // response现在是完整对象 { code: "200", data: { status: "..." } }
          const code = response.code || response.statusCode
          const data = response.data || response

          if (code === 200 || code === '200') {
            const status = data.status

            if (status === 'success') {
              // 支付成功
              if (onSuccess) onSuccess(data)
              resolve(data)
              return
            } else if (status === 'failed') {
              // 支付失败
              if (onFailed) onFailed(data)
              reject(new Error('支付失败'))
              return
            } else if (status === 'pending') {
              // 继续轮询
              if (attempts < maxAttempts) {
                setTimeout(poll, interval)
              } else {
                // 超时
                if (onTimeout) onTimeout()
                reject(new Error('支付超时'))
              }
            }
          } else {
            // API错误
            reject(new Error(response.message || '查询支付状态失败'))
          }
        } catch (error) {
          // 网络错误或其他异常，继续轮询
          if (attempts < maxAttempts) {
            setTimeout(poll, interval)
          } else {
            reject(error)
          }
        }
      }

      // 开始轮询
      poll()
    })
  }
}

export default paymentApi
