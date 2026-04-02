import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  // 严格的 Pinia 状态声明
  state: () => ({
    token: localStorage.getItem('auth_token') || '',
    userId: localStorage.getItem('auth_userId') || '',  // 保持为String类型
    phone: localStorage.getItem('auth_phone') || '',
    merchantId: localStorage.getItem('auth_merchantId') || '',  // 保持为String类型
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
      // 保持为 String 类型，避免精度丢失
      this.userId = String(newUserId || '').trim()
      if (this.userId) {
        localStorage.setItem('auth_userId', this.userId)
      } else {
        localStorage.removeItem('auth_userId')
      }
    },

    setMerchantId(newMerchantId: string | number) {
      // 保持为 String 类型
      const merchantIdStr = String(newMerchantId || '').trim()
      if (merchantIdStr && merchantIdStr !== '0' && merchantIdStr !== 'null') {
        this.merchantId = merchantIdStr
        localStorage.setItem('auth_merchantId', merchantIdStr)
      } else {
        // 如果是无效值，清除商家ID
        this.merchantId = ''
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
