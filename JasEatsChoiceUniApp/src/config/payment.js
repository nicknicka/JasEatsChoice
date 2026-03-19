/**
 * 支付模块配置
 * 集中管理支付相关常量和参数，便于维护
 */

// 支付方式枚举
export const PaymentMethod = {
  WALLET: 'wallet',      // 余额支付
  WECHAT: 'wechat',    // 微信支付
  ALIPAY: 'alipay'     // 支付宝支付
}

// 支付状态枚举
export const PaymentStatus = {
  PENDING: 'pending',   // 待支付
  SUCCESS: 'success',   // 支付成功
  FAILED: 'failed',     // 支付失败
  REFUND: 'refund'      // 已退款
}

// 支付轮询配置
export const PaymentPollConfig = {
  INTERVAL: 2000,       // 轮询间隔（毫秒）
  MAX_ATTEMPTS: 15,     // 最大轮询次数
  TIMEOUT: 30000        // 超时时间（毫秒）
}

// 支付方式显示名称
export const PaymentMethodName = {
  [PaymentMethod.WALLET]: '余额支付',
  [PaymentMethod.WECHAT]: '微信支付',
  [PaymentMethod.ALIPAY]: '支付宝支付'
}

// 支付方式图标
export const PaymentMethodIcon = {
  [PaymentMethod.WALLET]: 'wallet',
  [PaymentMethod.WECHAT]: 'weixin',
  [PaymentMethod.ALIPAY]: 'wallet-filled'
}

// 支付方式颜色
export const PaymentMethodColor = {
  [PaymentMethod.WALLET]: '#FF6B35',
  [PaymentMethod.WECHAT]: '#09BB07',
  [PaymentMethod.ALIPAY]: '#1677FF'
}

// 支付提示信息
export const PaymentMessages = {
  [PaymentMethod.WECHAT]: '推荐使用',
  [PaymentMethod.ALIPAY]: '数亿用户的选择',
  [PaymentMethod.WALLET]: '快捷支付'
}

// 支付超时时间（分钟）
export const PAYMENT_TIMEOUT_MINUTES = 15

// 最低支付金额（元）
export const MIN_PAYMENT_AMOUNT = 0.01

// 最大支付金额（元）
export const MAX_PAYMENT_AMOUNT = 50000

// 支付密码长度
export const PAYMENT_PASSWORD_LENGTH = 6

// 支付结果页面跳转延迟（毫秒）
export const PAYMENT_RESULT_REDIRECT_DELAY = 2000

// 默认配置
export const defaultPaymentConfig = {
  // 是否自动跳转到支付结果页
  autoRedirect: true,

  // 是否显示支付成功动画
  showSuccessAnimation: true,

  // 是否开启支付状态轮询
  enablePolling: true,

  // 支付超时后是否自动关闭
  autoCloseOnTimeout: false
}

// 支付错误码映射
export const PaymentErrorCodes = {
  BALANCE_INSUFFICIENT: '4001',     // 余额不足
  PAYMENT_TIMEOUT: '4002',          // 支付超时
  PAYMENT_FAILED: '4003',           // 支付失败
  ORDER_NOT_FOUND: '4004',          // 订单不存在
  INVALID_PAYMENT_METHOD: '4005',   // 不支持的支付方式
  PAYMENT_PASSWORD_ERROR: '4006',   // 支付密码错误
  PAYMENT_RECORD_NOT_FOUND: '4007', // 支付记录不存在
  ORDER_STATUS_ERROR: '4008',       // 订单状态异常
  COUPON_UNAVAILABLE: '4009',       // 优惠券不可用
  AMOUNT_ERROR: '4010'              // 金额异常
}

// 支付错误消息映射
export const PaymentErrorMessages = {
  [PaymentErrorCodes.BALANCE_INSUFFICIENT]: '余额不足，请先充值',
  [PaymentErrorCodes.PAYMENT_TIMEOUT]: '支付超时，请重新支付',
  [PaymentErrorCodes.PAYMENT_FAILED]: '支付失败，请稍后重试',
  [PaymentErrorCodes.ORDER_NOT_FOUND]: '订单不存在',
  [PaymentErrorCodes.INVALID_PAYMENT_METHOD]: '不支持的支付方式',
  [PaymentErrorCodes.PAYMENT_PASSWORD_ERROR]: '支付密码错误',
  [PaymentErrorCodes.PAYMENT_RECORD_NOT_FOUND]: '支付记录不存在',
  [PaymentErrorCodes.ORDER_STATUS_ERROR]: '订单状态异常，无法支付',
  [PaymentErrorCodes.COUPON_UNAVAILABLE]: '优惠券不可用或已过期',
  [PaymentErrorCodes.AMOUNT_ERROR]: '支付金额异常'
}

// 获取支付方式显示名称
export function getPaymentMethodName(method) {
  return PaymentMethodName[method] || '未知支付方式'
}

// 获取支付方式图标
export function getPaymentMethodIcon(method) {
  return PaymentMethodIcon[method] || 'wallet'
}

// 获取支付方式颜色
export function getPaymentMethodColor(method) {
  return PaymentMethodColor[method] || '#333'
}

// 获取支付错误消息
export function getPaymentErrorMessage(code) {
  return PaymentErrorMessages[code] || '支付失败，请稍后重试'
}

// 验证支付金额
export function validatePaymentAmount(amount) {
  if (amount < MIN_PAYMENT_AMOUNT) {
    return {
      valid: false,
      message: `最低支付金额为 ${MIN_PAYMENT_AMOUNT} 元`
    }
  }

  if (amount > MAX_PAYMENT_AMOUNT) {
    return {
      valid: false,
      message: `最大支付金额为 ${MAX_PAYMENT_AMOUNT} 元`
    }
  }

  return {
    valid: true,
    message: '金额有效'
  }
}

// 导出所有配置
export default {
  PaymentMethod,
  PaymentStatus,
  PaymentPollConfig,
  PaymentMethodName,
  PaymentMethodIcon,
  PaymentMethodColor,
  PaymentMessages,
  PAYMENT_TIMEOUT_MINUTES,
  MIN_PAYMENT_AMOUNT,
  MAX_PAYMENT_AMOUNT,
  PAYMENT_PASSWORD_LENGTH,
  PAYMENT_RESULT_REDIRECT_DELAY,
  defaultPaymentConfig,
  PaymentErrorCodes,
  PaymentErrorMessages,
  getPaymentMethodName,
  getPaymentMethodIcon,
  getPaymentMethodColor,
  getPaymentErrorMessage,
  validatePaymentAmount
}
