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
        const res = await userApi.getUserInfo()
        this.setUserInfo(res.data)
        return res.data
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
        this.setToken(res.data.token)
        this.setUserInfo(res.data.userInfo)
        return res.data
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
