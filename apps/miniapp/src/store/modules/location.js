import { defineStore } from 'pinia'

/**
 * 位置状态管理
 */
export const useLocationStore = defineStore('location', {
  state: () => ({
    // 当前位置
    currentLocation: {
      latitude: null,
      longitude: null,
      province: '',
      city: '',
      district: '',
      address: ''
    },

    // 选中的城市（延迟初始化，避免在 store 初始化时调用 uni API）
    selectedCity: {
      name: '定位中...',
      adcode: ''
    },

    // 天气信息（延迟初始化，避免在 store 初始化时调用 uni API）
    weather: null,

    // 是否已授权定位
    isLocationAuthorized: false,

    // 是否已初始化
    _initialized: false
  }),

  getters: {
    // 获取当前位置描述
    locationText: (state) => {
      if (state.currentLocation.address) {
        return state.currentLocation.address
      }
      return state.selectedCity.name || '定位中...'
    }
  },

  actions: {
    /**
     * 初始化 store - 从本地存储恢复数据
     * 这个方法应该在应用启动时调用
     */
    initialize() {
      if (this._initialized) return

      try {
        // 从本地存储恢复选中的城市
        const savedCity = uni.getStorageSync('selectedCity')
        if (savedCity) {
          this.selectedCity = savedCity
        }

        // 从本地存储恢复天气信息
        const savedWeather = uni.getStorageSync('weather')
        if (savedWeather) {
          this.weather = savedWeather
        }

        this._initialized = true
        console.log('✅ Location store 初始化成功')
      } catch (error) {
        console.error('❌ Location store 初始化失败:', error)
      }
    },

    /**
     * 设置当前位置
     * @param {Object} location - 位置信息
     */
    setCurrentLocation(location) {
      this.currentLocation = {
        latitude: location.latitude,
        longitude: location.longitude,
        province: location.province || '',
        city: location.city || '',
        district: location.district || '',
        address: location.address || ''
      }
    },

    /**
     * 设置选中的城市
     * @param {Object} city - 城市信息
     */
    setSelectedCity(city) {
      this.selectedCity = {
        name: city.name,
        adcode: city.adcode
      }
      uni.setStorageSync('selectedCity', this.selectedCity)
    },

    /**
     * 设置天气信息
     * @param {Object} weather - 天气信息
     */
    setWeather(weather) {
      this.weather = weather
      uni.setStorageSync('weather', weather)
    },

    /**
     * 获取当前位置
     * 添加超时处理，默认10秒超时
     */
    async getCurrentPosition(timeout = 10000) {
      return new Promise((resolve, reject) => {
        // 创建超时定时器
        const timer = setTimeout(() => {
          console.error('获取位置超时')
          this.isLocationAuthorized = false
          reject(new Error('获取位置超时，请检查定位权限或GPS信号'))
        }, timeout)

        uni.getLocation({
          type: 'gcj02',
          success: (res) => {
            clearTimeout(timer)
            this.setCurrentLocation({
              latitude: res.latitude,
              longitude: res.longitude
            })
            this.isLocationAuthorized = true
            resolve(res)
          },
          fail: (err) => {
            clearTimeout(timer)
            console.error('获取位置失败:', err)
            this.isLocationAuthorized = false

            // 根据错误码提供更友好的提示
            let errorMsg = '获取位置失败'
            if (err.errMsg && err.errMsg.includes('auth')) {
              errorMsg = '请授权定位权限'
            } else if (err.errMsg && err.errMsg.includes('timeout')) {
              errorMsg = '定位超时，请稍后重试'
            }
            reject(new Error(errorMsg))
          }
        })
      })
    },

    /**
     * 选择位置（打开地图选择器）
     * 添加超时处理，默认30秒超时（给用户足够时间选择）
     */
    async chooseLocation(timeout = 30000) {
      return new Promise((resolve, reject) => {
        // 创建超时定时器
        const timer = setTimeout(() => {
          console.error('选择位置超时')
          reject(new Error('选择位置超时'))
        }, timeout)

        uni.chooseLocation({
          success: (res) => {
            clearTimeout(timer)
            this.setCurrentLocation({
              latitude: res.latitude,
              longitude: res.longitude,
              address: res.address,
              name: res.name
            })
            resolve(res)
          },
          fail: (err) => {
            clearTimeout(timer)
            console.error('选择位置失败:', err)

            // 用户取消选择不报错
            if (err.errMsg && err.errMsg.includes('cancel')) {
              reject(new Error('用户取消选择位置'))
            } else {
              reject(new Error('选择位置失败'))
            }
          }
        })
      })
    }
  }
})
