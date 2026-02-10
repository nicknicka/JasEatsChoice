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
    // TODO: 后端API实现后启用以下代码
    // return api.post('/v1/verification/send', null, {
    //   params: { phone, type }
    // })

    // 临时使用模拟响应
    console.log(`[模拟] 发送验证码到手机: ${phone}, 类型: ${type}`)

    return Promise.resolve({
      code: '200',
      message: '验证码已发送（模拟）',
      data: {
        phone,
        type,
        expireTime: 300, // 5分钟有效期
        code: '123456', // 模拟验证码，实际开发中不会返回
        note: '模拟数据 - 后端验证码API待实现'
      }
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
    // TODO: 后端API实现后启用以下代码
    // return api.post('/v1/verification/verify', null, {
    //   params: { phone, code, type }
    // })

    // 临时使用模拟验证逻辑
    console.log(`[模拟] 验证验证码: ${code}, 手机: ${phone}, 类型: ${type}`)

    // 模拟：123456 是正确的验证码
    const isValid = code === '123456'

    return Promise.resolve({
      code: isValid ? '200' : '400',
      message: isValid ? '验证成功' : '验证码错误或已过期',
      data: {
        valid: isValid,
        note: '模拟数据 - 后端验证码API待实现'
      }
    })
  }
}
