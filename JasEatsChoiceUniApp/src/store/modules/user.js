import { defineStore } from 'pinia'
import { userApi } from '@/api'

/**
 * 用户状态管理
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    // Token
    token: uni.getStorageSync('token') || '',

    // 用户信息
    userInfo: uni.getStorageSync('userInfo') || null,

    // 用户角色：user | merchant | admin
    role: uni.getStorageSync('role') || 'user',

    // 是否登录
    isLogin: !!uni.getStorageSync('token')
  }),

  getters: {
    // 判断是否为商家
    isMerchant: (state) => state.role === 'merchant',

    // 判断是否为管理员
    isAdmin: (state) => state.role === 'admin',

    // 获取用户昵称
    nickname: (state) => state.userInfo?.nickname || '未登录',

    // 获取用户头像
    avatar: (state) => state.userInfo?.avatar || '',

    // 获取会员等级
    memberLevel: (state) => state.userInfo?.memberLevel || 0
  },

  actions: {
    /**
     * 设置Token
     * @param {string} token - JWT Token
     */
    setToken(token) {
      this.token = token
      this.isLogin = !!token
      uni.setStorageSync('token', token)
    },

    /**
     * 设置用户信息
     * @param {Object} userInfo - 用户信息
     */
    setUserInfo(userInfo) {
      if (!userInfo) {
        console.warn('setUserInfo: userInfo is null or undefined')
        return
      }
      this.userInfo = userInfo
      if (userInfo.role) {
        this.role = userInfo.role
        uni.setStorageSync('role', userInfo.role)
      }
      uni.setStorageSync('userInfo', userInfo)
    },

    /**
     * 获取用户信息
     */
    async fetchUserInfo() {
      try {
        if (!this.userId) {
          throw new Error('用户ID不存在')
        }

        const res = await userApi.getUserInfo(this.userId)
        console.log('获取用户信息响应:', res)

        // 兼容不同的响应格式
        if (res && res.data) {
          this.setUserInfo(res.data)
        } else if (res && typeof res === 'object') {
          // 直接返回用户对象
          this.setUserInfo(res)
        }

        return res
      } catch (error) {
        console.error('获取用户信息失败:', error)
        throw error
      }
    },

    /**
     * 登录（支持验证码和密码两种方式）
     * @param {Object} data - 登录数据
     * @param {string} data.phone - 手机号
     * @param {string} data.code - 验证码（验证码登录时使用）
     * @param {string} data.password - 密码（密码登录时使用）
     * @param {string} data.captcha - 图形验证码
     * @param {string} data.checkCodeKey - 验证码key
     */
    async login(data) {
      try {
        const res = await userApi.login(data)
        console.log('登录响应完整数据:', res)
        console.log('登录响应 data 字段:', res.data)

        // 检查返回的数据结构
        if (!res) {
          throw new Error('登录失败：服务器返回数据为空')
        }

        // 处理 token
        let token = null
        if (res.token) {
          token = res.token
        } else if (res.data && res.data.token) {
          token = res.data.token
        }

        if (token) {
          this.setToken(token)
        }

        // 处理用户信息 - 兼容多种数据结构
        let userInfo = null
        if (res.user) {
          userInfo = res.user
        } else if (res.userInfo) {
          userInfo = res.userInfo
        } else if (res.data && res.data.user) {
          userInfo = res.data.user
        } else if (res.data && res.data.userInfo) {
          userInfo = res.data.userInfo
        } else if (res.data && res.data.userId) {
          // 如果只有 userId，构建最小用户信息
          userInfo = { userId: res.data.userId }
        }

        if (userInfo) {
          console.log('设置用户信息:', userInfo)
          this.setUserInfo(userInfo)

          // 如果用户信息包含 userId，也保存到 userInfo
          if (userInfo.userId || userInfo.id) {
            this.userId = userInfo.userId || userInfo.id
          }
        } else {
          console.warn('登录响应中没有找到用户信息，响应结构:', res)
        }

        return res
      } catch (error) {
        console.error('登录失败:', error)
        throw error
      }
    },

    /**
     * 注册
     * @param {Object} data - 注册数据
     */
    async register(data) {
      try {
        const res = await userApi.register(data)
        return res.data
      } catch (error) {
        console.error('注册失败:', error)
        throw error
      }
    },

    /**
     * 微信登录
     * @param {Object} data - 微信登录数据
     */
    async wechatLogin(data) {
      try {
        const res = await userApi.wechatLogin(data)
        this.setToken(res.data.token)
        this.setUserInfo(res.data.userInfo)
        return res.data
      } catch (error) {
        console.error('微信登录失败:', error)
        throw error
      }
    },

    /**
     * 更新用户信息
     * @param {Object} data - 用户信息
     */
    async updateUserInfo(data) {
      try {
        const res = await userApi.updateUserInfo(data)
        this.setUserInfo(res.data)
        return res.data
      } catch (error) {
        console.error('更新用户信息失败:', error)
        throw error
      }
    },

    /**
     * 退出登录
     */
    logout() {
      this.token = ''
      this.userInfo = null
      this.role = 'user'
      this.isLogin = false

      // 清除本地存储
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('role')

      // 跳转到登录页
      uni.reLaunch({
        url: '/pages/login/index'
      })
    },

    /**
     * 检查登录状态
     */
    checkLogin() {
      if (!this.token) {
        uni.showToast({
          title: '请先登录',
          icon: 'none'
        })
        uni.navigateTo({
          url: '/pages/login/index'
        })
        return false
      }
      return true
    }
  }
})
