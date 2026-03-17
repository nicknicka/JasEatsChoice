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

    // 选中的城市
    selectedCity: uni.getStorageSync('selectedCity') || {
      name: '定位中...',
      adcode: ''
    },

    // 天气信息
    weather: uni.getStorageSync('weather') || null,

    // 是否已授权定位
    isLocationAuthorized: false
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
     */
    async getCurrentPosition() {
      return new Promise((resolve, reject) => {
        uni.getLocation({
          type: 'gcj02',
          success: (res) => {
            this.setCurrentLocation({
              latitude: res.latitude,
              longitude: res.longitude
            })
            this.isLocationAuthorized = true
            resolve(res)
          },
          fail: (err) => {
            console.error('获取位置失败:', err)
            this.isLocationAuthorized = false
            reject(err)
          }
        })
      })
    },

    /**
     * 选择位置（打开地图选择器）
     */
    async chooseLocation() {
      return new Promise((resolve, reject) => {
        uni.chooseLocation({
          success: (res) => {
            this.setCurrentLocation({
              latitude: res.latitude,
              longitude: res.longitude,
              address: res.address,
              name: res.name
            })
            resolve(res)
          },
          fail: (err) => {
            console.error('选择位置失败:', err)
            reject(err)
          }
        })
      })
    }
  }
})
