import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  // 严格的 Pinia 状态声明
  state: () => ({
    token: localStorage.getItem('auth_token') || '',
    userId: localStorage.getItem('auth_userId')
      ? Number(localStorage.getItem('auth_userId'))
      : null,
    phone: localStorage.getItem('auth_phone') || '',
    merchantId: localStorage.getItem('auth_merchantId')
      ? Number(localStorage.getItem('auth_merchantId'))
      : null,
    currentRole: localStorage.getItem('auth_currentRole') || ''
  }),

  // Getters (计算属性)
  getters: {
    isAuthenticated: (state) => !!state.token,
    hasMerchantId: (state) => !!state.merchantId
  },

  // Actions (方法)
  actions: {
    setToken(newToken: string) {
      this.token = newToken
      localStorage.setItem('auth_token', newToken)
    },

    setUserId(newUserId: string | number) {
      const userId = Number(newUserId)
      this.userId = Number.isNaN(userId) ? null : userId
      if (this.userId) {
        localStorage.setItem('auth_userId', String(this.userId))
      } else {
        localStorage.removeItem('auth_userId')
      }
    },

    setMerchantId(newMerchantId: string | number) {
      // 只存储有效的商家ID
      const merchantId = Number(newMerchantId)
      if (!Number.isNaN(merchantId) && merchantId > 0) {
        this.merchantId = merchantId
        localStorage.setItem('auth_merchantId', String(merchantId))
      } else {
        // 如果是无效值，清除商家ID
        this.merchantId = null
        localStorage.removeItem('auth_merchantId')
      }
    },

    setCurrentRole(newRole: string) {
      this.currentRole = newRole
      localStorage.setItem('auth_currentRole', newRole)
    },

    setPhone(phone: string) {
      this.phone = phone
      localStorage.setItem('auth_phone', phone)
    },

    clearAuth() {
      this.token = ''
      this.userId = ''
      this.phone = ''
      this.merchantId = ''
      this.currentRole = ''

      // 清空localStorage中的所有认证信息
      localStorage.removeItem('auth_token')
      localStorage.removeItem('auth_userId')
      localStorage.removeItem('auth_phone')
      localStorage.removeItem('auth_merchantId')
      localStorage.removeItem('auth_currentRole')
    }
  }
})
