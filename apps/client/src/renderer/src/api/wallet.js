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
   * 提现申请（创建提现记录，等待审核）
   */
  withdraw(userId, amount, withdrawNo, withdrawMethod = 'wechat', accountInfo = '微信钱包') {
    return api.post('/v1/wallet/withdraw', null, {
      params: { userId, amount, withdrawNo, withdrawMethod, accountInfo }
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
    return api.put('/v1/wallet/lock-status', null, {
      params: { userId, locked }
    })
  },

  /**
   * 获取钱包安全设置
   * @param {number} userId - 用户ID
   * @returns {Promise} 安全设置
   */
  getWalletSecuritySettings(userId) {
    return api.get('/v1/wallet/security-settings', {
      params: { userId }
    })
  }
}
