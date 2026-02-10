/**
 * 验证码相关API
 *
 * 后端需要实现的接口：
 * - POST /v1/verification/send - 发送验证码
 * - POST /v1/verification/verify - 验证验证码
 *
 * 当前状态：使用模拟数据，待后端API实现后启用真实API调用
 */
import api from '../utils/api'

export default {
  /**
   * 发送验证码
   * @param {string} phone - 手机号
   * @param {string} type - 验证码类型（register-注册, login-登录, reset-重置密码, payment-支付密码）
   * @returns {Promise} 发送结果
   */
  sendVerificationCode(phone, type = 'payment') {
    return api.post('/v1/verification/send', null, {
      params: { phone, type }
    })
  },

  /**
   * 验证验证码
   * @param {string} phone - 手机号
   * @param {string} code - 验证码
   * @param {string} type - 验证码类型
   * @returns {Promise} 验证结果
   */
  verifyCode(phone, code, type = 'payment') {
    return api.post('/v1/verification/verify', null, {
      params: { phone, code, type }
    })
  }
}
