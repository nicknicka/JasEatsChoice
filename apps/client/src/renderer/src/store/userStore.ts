import { defineStore } from 'pinia'
import axios from 'axios'
import { API_CONFIG } from '../config'
import { useAuthStore } from './authStore'
const authStore = useAuthStore()

export interface UserInfo {
  userId: string
  phone: string
  nickname: string
  email: string
  location: string | null
  height: number | null
  weight: number | null
  dietGoal: string | null
  allergies: any // 过敏原信息，JSON格式
  preferTags: any // 饮食偏好标签，JSON格式
  disableWeatherRecommend: boolean | null
  createTime: string // 创建时间
  updateTime: string | null
  merchantId: string | null // 商家ID，如果不为空表示用户已注册为商家
  avatar: string // 用户头像URL
}

export interface MerchantInfo {
  id: string
  name: string
  address?: string
  phone: string
  status: boolean
  createTime?: string
  updateTime?: string
  businessLicense: string
  businessScope: string[]
  contactName: string
  avatar?: string
  rating?: number
  businessHours?: any // JSON 类型，存储营业时间范围 {start: "HH:mm", end: "HH:mm"}
  email?: string
  category?: string
  averagePrice?: number
  longitude?: number
  latitude?: number
  // 其他商家信息字段
}

export const useUserStore = defineStore('user', {
  // 严格的 Pinia 状态声明
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null') as UserInfo | null,
    merchantInfo: JSON.parse(localStorage.getItem('merchantInfo') || 'null') as MerchantInfo | null
  }),

  // Getters (计算属性)
  getters: {
    isMerchantRegistered: (state) => !!state.userInfo?.merchantId || authStore.hasMerchantId
  },

  // Actions (方法)
  actions: {
    async fetchUserInfo() {
      try {
        const authStore = useAuthStore()
        if (!authStore.userId) {
          throw new Error('用户ID不存在')
        }

        // 使用正确的API端点路径，后端是GET /v1/users/{userId}
        const response = await axios.get(`${API_CONFIG.baseURL}/v1/users/${authStore.userId}`)

        if (response.data.code === '200') {
          // 确保height和weight是数字类型（处理后端可能返回的数组类型）
          const userData = response.data.data
          // 如果是数组，取第一个元素；否则直接转换
          if (Array.isArray(userData.height)) {
            userData.height = Number(userData.height[0]) || null
          } else if (userData.height) {
            userData.height = Number(userData.height)
          }

          if (Array.isArray(userData.weight)) {
            userData.weight = Number(userData.weight[0]) || null
          } else if (userData.weight) {
            userData.weight = Number(userData.weight)
          }

          this.userInfo = userData
          // 将最新用户信息保存到localStorage
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))

          // 如果用户有注册商家ID，保存到authStore并获取商家信息
          if (
            this.userInfo &&
            this.userInfo.merchantId &&
            String(this.userInfo.merchantId) !== 'null' &&
            String(this.userInfo.merchantId) !== ''
          ) {
            authStore.setMerchantId(this.userInfo.merchantId)
            await this.fetchMerchantInfo() // 自动获取商家信息
          }

          return userData
        } else {
          throw new Error(response.data.message || '获取用户信息失败')
        }
      } catch (error: any) {
        console.error('获取用户信息失败:', error)
        throw error
      }
    },

    setUserInfo(info: UserInfo) {
      // 确保height和weight是数字类型（处理可能的数组类型）
      const userData = { ...info }
      // 如果是数组，取第一个元素；否则直接转换
      if (Array.isArray(userData.height)) {
        userData.height = Number(userData.height[0]) || null
      } else if (userData.height) {
        userData.height = Number(userData.height)
      }

      if (Array.isArray(userData.weight)) {
        userData.weight = Number(userData.weight[0]) || null
      } else if (userData.weight) {
        userData.weight = Number(userData.weight)
      }

      this.userInfo = userData
      // 将最新用户信息保存到localStorage
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      const authStore = useAuthStore()
      // 如果用户有注册商家ID，保存到authStore
      if (userData.merchantId) {
        authStore.setMerchantId(userData.merchantId)
      }
    },

    async fetchMerchantInfo() {
      try {
        const authStore = useAuthStore()
        if (
          !authStore.merchantId ||
          String(authStore.merchantId) === 'null' ||
          String(authStore.merchantId) === ''
        ) {
          throw new Error('商家ID不存在')
        }

        const response = await axios.get(
          `${API_CONFIG.baseURL}/v1/merchant/${authStore.merchantId}`
        )
        if (response.data.code === '200') {
          this.merchantInfo = response.data.data
          localStorage.setItem('merchantInfo', JSON.stringify(this.merchantInfo))
          return response.data.data
        } else {
          throw new Error(response.data.message || '获取商家信息失败')
        }
      } catch (error: any) {
        console.error('获取商家信息失败:', error)
        throw error
      }
    },

    // 更新用户头像
    async updateUserAvatar(avatar: string) {
      if (this.userInfo) {
        // Update in store first
        this.userInfo.avatar = avatar
        // 将最新用户信息保存到localStorage
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))

        // Try to update on backend if userId exists
        try {
          const authStore = useAuthStore()
          if (authStore.userId) {
            const response = await axios.put(`${API_CONFIG.baseURL}/v1/users/${authStore.userId}`, {
              avatar
            })

            // Check response.data for status
            if (response.data?.code !== '200') {
              console.error(
                'Failed to update avatar on backend:',
                response.data?.message || 'Unknown error'
              )
            }
          }
        } catch (error) {
          console.error('Failed to update avatar on backend:', error)
        }
      }
    },

    // 更新商家头像
    updateMerchantAvatar(avatar: string) {
      if (this.merchantInfo) {
        this.merchantInfo = {
          ...this.merchantInfo,
          avatar
        }
        localStorage.setItem('merchantInfo', JSON.stringify(this.merchantInfo))
      }
    },

    setMerchantInfo(info: MerchantInfo) {
      this.merchantInfo = info
      localStorage.setItem('merchantInfo', JSON.stringify(this.merchantInfo))
    },

    clearUserInfo() {
      this.userInfo = null
      this.merchantInfo = null
    }
  }
})
