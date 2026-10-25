/**
 * 用户相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取用户信息
   */
  getUserInfo(userId) {
    return api.get(`/v1/users/${userId}`)
  }
}
