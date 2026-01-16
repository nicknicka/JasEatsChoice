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
    endDate = null
  ) {
    return api.get('/v1/consume-history', {
      params: { userId, type, page, pageSize, startDate, endDate }
    })
  }
}
