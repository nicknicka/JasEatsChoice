/**
 * 钱包相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取钱包信息
   */
  getWalletInfo(userId) {
    return api.get(`/v1/wallet/info/${userId}`)
  },

  /**
   * 获取余额
   */
  getBalance(userId) {
    return api.get(`/v1/wallet/balance/${userId}`)
  },

  /**
   * 检查余额是否足够
   */
  checkBalance(userId, amount) {
    return api.get('/v1/wallet/check', {
      params: { userId, amount }
    })
  },

  /**
   * 充值
   */
  recharge(userId, amount, rechargeNo) {
    return api.post('/v1/wallet/recharge', null, {
      params: { userId, amount, rechargeNo }
    })
  },

  /**
   * 提现
   */
  withdraw(userId, amount, withdrawNo) {
    return api.post('/v1/wallet/withdraw', null, {
      params: { userId, amount, withdrawNo }
    })
  },

  /**
   * 获取消费记录（交易记录）
   */
  getConsumeHistory(
    userId,
    type = 'all',
    page = 1,
    pageSize = 10,
    startDate = null,
    endDate = null,
    status = null
  ) {
    const params = { userId, type, page, pageSize }
    if (startDate) params.startDate = startDate
    if (endDate) params.endDate = endDate
    if (status) params.status = status
    return api.get('/v1/consume-history', { params })
  },

  /**
   * 更新钱包锁定状态
   * @param {number} userId - 用户ID
   * @param {boolean} locked - 是否锁定
   * @returns {Promise} 更新结果
   */
  updateWalletLockStatus(userId, locked) {
    // TODO: 后端API实现后启用以下代码
    // return api.put('/v1/wallet/lock-status', null, {
    //   params: { userId, locked }
    // })

    // 临时使用模拟响应
    console.log(`[模拟] 更新钱包锁定状态: userId=${userId}, locked=${locked}`)

    return Promise.resolve({
      code: '200',
      message: locked ? '钱包已锁定' : '钱包已解锁',
      data: {
        userId,
        locked,
        note: '模拟数据 - 后端钱包锁定API待实现'
      }
    })
  },

  /**
   * 获取钱包安全设置
   * @param {number} userId - 用户ID
   * @returns {Promise} 安全设置
   */
  getWalletSecuritySettings(userId) {
    // TODO: 后端API实现后启用以下代码
    // return api.get('/v1/wallet/security-settings', {
    //   params: { userId }
    // })

    // 临时使用模拟数据
    return Promise.resolve({
      code: '200',
      message: '获取成功',
      data: {
        userId,
        locked: false,
        verifyEnabled: true,
        dailyLimit: 5000,
        note: '模拟数据 - 后端安全设置API待实现'
      }
    })
  }
}
