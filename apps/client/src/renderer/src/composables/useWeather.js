import { ref, computed } from 'vue'
import { ElNotification, ElMessageBox } from 'element-plus'
import api from '../utils/api.js'
import { API_CONFIG } from '../config/index.js'

// 天气缓存配置
const CACHE_DURATION = 30 * 60 * 1000 // 30分钟
const weatherCache = ref(null)
const locationHistory = ref([])

// 从 localStorage 加载位置历史
const loadLocationHistory = () => {
  try {
    const saved = localStorage.getItem('locationHistory')
    if (saved) {
      locationHistory.value = JSON.parse(saved)
    }
  } catch (error) {
    console.error('加载位置历史失败:', error)
  }
}

// 保存位置历史到 localStorage
const saveLocationHistory = () => {
  try {
    localStorage.setItem('locationHistory', JSON.stringify(locationHistory.value))
  } catch (error) {
    console.error('保存位置历史失败:', error)
  }
}

// 添加位置到历史记录
const addToLocationHistory = (location) => {
  if (!location || location === '未获取到详细地址') return

  // 移除重复项
  locationHistory.value = locationHistory.value.filter((item) => item !== location)

  // 添加到开头
  locationHistory.value.unshift(location)

  // 限制最多保存 5 条
  if (locationHistory.value.length > 5) {
    locationHistory.value = locationHistory.value.slice(0, 5)
  }

  saveLocationHistory()
}

export function useWeather() {
  // 天气数据
  const weather = ref({
    temp: 32,
    tempMin: 28,
    tempMax: 36,
    condition: '晴天',
    city: '',
    address: '',
    humidity: 65,
    windSpeed: 3,
    aqi: 50,
    loading: false,
    error: null
  })

  // 天气详情弹窗
  const weatherDetailVisible = ref(false)

  // 获取天气对应的主题渐变色
  const getWeatherGradient = () => {
    const condition = weather.value.condition
    const temp = weather.value.temp

    if (condition.includes('晴') || condition.includes('热')) {
      // 晴天/热天 - 橙红色
      return 'linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%)'
    }
    if (condition.includes('雨')) {
      // 雨天 - 蓝紫色
      return 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }
    if (condition.includes('雪')) {
      // 雪天 - 浅蓝色
      return 'linear-gradient(135deg, #e0eafc 0%, #cfdef3 100%)'
    }
    if (condition.includes('云') || condition.includes('阴')) {
      // 多云/阴天 - 天蓝色
      return 'linear-gradient(135deg, #89c4f4 0%, #5d9cec 100%)'
    }
    if (condition.includes('雷') || condition.includes('暴')) {
      // 雷暴天气 - 深紫色
      return 'linear-gradient(135deg, #4a569d 0%, #243b55 100%)'
    }
    if (temp > 30) {
      // 高温 - 火红渐变
      return 'linear-gradient(135deg, #f83600 0%, #f9d423 100%)'
    }
    if (temp < 10) {
      // 低温 - 冰蓝渐变
      return 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)'
    }
    // 默认 - 温暖橙
    return 'linear-gradient(135deg, #ff9a56 0%, #ff6b6b 100%)'
  }

  // 获取天气对应的图标
  const getWeatherIcon = () => {
    const condition = weather.value.condition
    if (!condition) return 'Sunny'

    if (condition.includes('晴')) return 'Sunny'
    if (condition.includes('云') || condition.includes('阴')) return 'Cloudy'
    if (condition.includes('雨')) return 'Drizzling'
    if (condition.includes('雷')) return 'Lightning'
    if (condition.includes('雪')) return 'Snowy'
    if (condition.includes('雾') || condition.includes('霾')) return 'Cloudy'

    return 'Sunny'
  }

  // 获取天气对应的 Emoji
  const getWeatherEmoji = () => {
    const condition = weather.value.condition
    if (!condition) return '☀️'

    if (condition.includes('晴')) return '☀️'
    if (condition.includes('云')) return '⛅'
    if (condition.includes('阴')) return '☁️'
    if (condition.includes('小雨')) return '🌦️'
    if (condition.includes('雨')) return '🌧️'
    if (condition.includes('雷')) return '⛈️'
    if (condition.includes('雪')) return '❄️'
    if (condition.includes('雾')) return '🌫️'

    return '☀️'
  }

  // 获取空气质量描述
  const getAQIDescription = () => {
    const aqi = weather.value.aqi
    if (aqi <= 50) return { text: '优', color: '#52c41a' }
    if (aqi <= 100) return { text: '良', color: '#1890ff' }
    if (aqi <= 150) return { text: '轻度污染', color: '#faad14' }
    if (aqi <= 200) return { text: '中度污染', color: '#ff7a45' }
    if (aqi <= 300) return { text: '重度污染', color: '#f5222d' }
    return { text: '严重污染', color: '#7f0021' }
  }

  // 根据天气条件和时间智能推荐菜品系列
  const getRecommendedDishesSeries = () => {
    const condition = weather.value.condition
    const temp = weather.value.temp
    const humidity = weather.value.humidity
    const hour = new Date().getHours()

    // 早餐时段 (6-10点)
    if (hour >= 6 && hour < 10) {
      return '营养早餐系列'
    }

    // 午餐时段 (11-13点)
    if (hour >= 11 && hour < 13) {
      if (temp > 28) {
        return '清爽午餐系列'
      }
      return '均衡午餐系列'
    }

    // 晚餐时段 (17-20点)
    if (hour >= 17 && hour < 20) {
      if (temp < 15) {
        return '暖胃晚餐系列'
      }
      return '精选晚餐系列'
    }

    // 夜宵时段 (21-凌晨2点)
    if (hour >= 21 || hour < 2) {
      return '轻食夜宵系列'
    }

    // 高温高湿天气
    if (temp > 30 && humidity > 70) {
      return '清爽解暑系列'
    }

    // 低温天气
    if (temp < 15 || condition.includes('雪')) {
      return '热食/火锅系列'
    }

    // 高温晴天
    if (temp > 28 || condition.includes('晴')) {
      return '冰饮/凉菜系列'
    }

    // 雨天
    if (condition.includes('雨')) {
      return '汤品/暖食系列'
    }

    // 多云阴天
    if (condition.includes('云') || condition.includes('阴')) {
      return '均衡饮食系列'
    }

    // 默认推荐
    return '特色菜品系列'
  }

  // 获取穿衣建议
  const getClothingAdvice = () => {
    const temp = weather.value.temp
    const condition = weather.value.condition

    if (temp < 10) {
      return '寒冷，建议穿羽绒服、棉衣等厚重冬装'
    }
    if (temp < 18) {
      return '较凉，建议穿夹克、毛衣、长袖等'
    }
    if (temp < 25) {
      return '舒适，建议穿长袖衬衫、薄外套等'
    }
    if (temp < 30) {
      return '温暖，建议穿短袖、短裤等轻薄衣物'
    }
    return '炎热，建议穿短袖、短裤等透气衣物，注意防晒'
  }

  // 获取运动建议
  const getExerciseAdvice = () => {
    const condition = weather.value.condition
    const aqi = weather.value.aqi

    if (aqi > 150) {
      return '空气质量不佳，不建议户外运动'
    }

    if (condition.includes('雨') || condition.includes('雪')) {
      return '天气不佳，建议室内运动'
    }

    if (condition.includes('晴') || condition.includes('云')) {
      return '天气不错，适合户外运动'
    }

    return '适宜运动'
  }

  // 显示天气详情弹窗
  const showWeatherDetail = () => {
    weatherDetailVisible.value = true
  }

  // 显示错误对话框
  const showWeatherErrorDialog = (onRetry, onManualSelect) => {
    ElMessageBox.confirm('无法获取位置信息，是否手动选择位置？', '定位失败', {
      confirmButtonText: '手动选择',
      cancelButtonText: '重试',
      type: 'warning',
      distinguishCancelAndClose: true
    })
      .then(() => {
        // 用户选择手动选择
        if (onManualSelect) onManualSelect()
      })
      .catch((action) => {
        if (action === 'cancel') {
          // 用户选择重试
          if (onRetry) onRetry()
        }
      })
  }

  // 从缓存获取天气数据
  const getWeatherFromCache = (cacheKey) => {
    if (weatherCache.value && weatherCache.value[cacheKey]) {
      const cached = weatherCache.value[cacheKey]
      const now = Date.now()

      if (now - cached.timestamp < CACHE_DURATION) {
        console.log('使用缓存的天气数据:', cached.data)
        return cached.data
      }
    }
    return null
  }

  // 保存天气数据到缓存
  const saveWeatherToCache = (cacheKey, data) => {
    if (!weatherCache.value) {
      weatherCache.value = {}
    }

    weatherCache.value[cacheKey] = {
      data: { ...data },
      timestamp: Date.now()
    }

    // 限制缓存大小
    const keys = Object.keys(weatherCache.value)
    if (keys.length > 10) {
      // 删除最旧的缓存
      let oldestKey = keys[0]
      let oldestTime = weatherCache.value[oldestKey].timestamp

      for (const key of keys) {
        if (weatherCache.value[key].timestamp < oldestTime) {
          oldestTime = weatherCache.value[key].timestamp
          oldestKey = key
        }
      }

      delete weatherCache.value[oldestKey]
    }
  }

  // 清除天气缓存
  const clearWeatherCache = () => {
    weatherCache.value = null
  }

  // 获取天气数据
  const fetchWeather = async (selectedCity = null, options = {}) => {
    const { onRetry, onManualSelect } = options

    weather.value.loading = true
    weather.value.error = null

    try {
      let cacheKey = 'default'
      let weatherData = null

      if (selectedCity) {
        // 使用选择的城市
        cacheKey = `city_${selectedCity}`

        // 先尝试从缓存获取
        const cached = getWeatherFromCache(cacheKey)
        if (cached) {
          weather.value = { ...weather.value, ...cached }
          weather.value.loading = false
          return weather.value
        }

        // 获取指定城市的天气
        weather.value.city = selectedCity
        const response = await api.get(
          `${API_CONFIG.weather.current}?city=${encodeURIComponent(selectedCity)}`
        )

        if (response?.data) {
          weatherData = {
            temp: response.data.temperature || 25,
            tempMin: response.data.tempMin || response.data.temperature - 3,
            tempMax: response.data.tempMax || response.data.temperature + 3,
            condition: response.data.condition || '晴天',
            humidity: response.data.humidity || 60,
            windSpeed: response.data.windSpeed || 3,
            aqi: response.data.aqi || 50,
            city: selectedCity
          }

          // 保存到缓存
          saveWeatherToCache(cacheKey, weatherData)
        }
      } else {
        // 自动定位
        cacheKey = 'auto_location'

        // 先尝试从缓存获取
        const cached = getWeatherFromCache(cacheKey)
        if (cached) {
          weather.value = { ...weather.value, ...cached }
          weather.value.loading = false
          return weather.value
        }

        // 步骤1: 获取位置
        const locationResponse = await api.get(API_CONFIG.location.location)

        if (locationResponse?.data) {
          let { city, address } = locationResponse.data

          // 处理异常数据格式
          if (Array.isArray(city)) {
            city = city.join('')
          }
          if (Array.isArray(address) || address === '[][]') {
            address = '未获取到详细地址'
          }

          weather.value.city = city
          weather.value.address = address

          // 添加到位置历史
          addToLocationHistory(address)

          // 步骤2: 获取天气
          const weatherResponse = await api.get(
            `${API_CONFIG.weather.current}?city=${encodeURIComponent(city)}`
          )

          if (weatherResponse?.data) {
            weatherData = {
              temp: weatherResponse.data.temperature || 25,
              tempMin: weatherResponse.data.tempMin || weatherResponse.data.temperature - 3,
              tempMax: weatherResponse.data.tempMax || weatherResponse.data.temperature + 3,
              condition: weatherResponse.data.condition || '晴天',
              humidity: weatherResponse.data.humidity || 60,
              windSpeed: weatherResponse.data.windSpeed || 3,
              aqi: weatherResponse.data.aqi || 50,
              city: city,
              address: address
            }

            // 保存到缓存
            saveWeatherToCache(cacheKey, weatherData)
          }
        }
      }

      // 更新天气数据
      if (weatherData) {
        weather.value = { ...weather.value, ...weatherData }
      }

      weather.value.loading = false
      return weather.value
    } catch (error) {
      console.error('获取天气失败:', error)
      weather.value.loading = false
      weather.value.error = error.message

      // 显示错误对话框
      if (!selectedCity) {
        showWeatherErrorDialog(
          () => fetchWeather(null, { onRetry, onManualSelect }), // 重试
          onManualSelect // 手动选择
        )
      } else {
        ElNotification.error({
          title: '获取天气失败',
          message: '请检查网络连接或稍后重试',
          duration: 3000
        })
      }

      throw error
    }
  }

  // 获取位置历史记录
  const getLocationHistory = () => {
    if (locationHistory.value.length === 0) {
      loadLocationHistory()
    }
    return locationHistory.value
  }

  // 清除位置历史
  const clearLocationHistory = () => {
    locationHistory.value = []
    localStorage.removeItem('locationHistory')
  }

  // 计算属性：温度范围显示
  const tempRangeText = computed(() => {
    if (weather.value.tempMin && weather.value.tempMax) {
      return `${weather.value.tempMin}° ~ ${weather.value.tempMax}°`
    }
    return null
  })

  // 计算属性：是否需要加载骨架屏
  const showWeatherSkeleton = computed(() => {
    return weather.value.loading && !weather.value.city
  })

  // 初始化时加载位置历史
  loadLocationHistory()

  return {
    // 状态
    weather,
    weatherDetailVisible,
    showWeatherSkeleton,

    // 计算属性
    tempRangeText,
    weatherGradient: computed(() => getWeatherGradient()),
    weatherIcon: computed(() => getWeatherIcon()),
    weatherEmoji: computed(() => getWeatherEmoji()),
    aqiInfo: computed(() => getAQIDescription()),
    clothingAdvice: computed(() => getClothingAdvice()),
    exerciseAdvice: computed(() => getExerciseAdvice()),

    // 方法
    fetchWeather,
    showWeatherDetail,
    getRecommendedDishesSeries,
    getLocationHistory,
    clearLocationHistory,
    clearWeatherCache,
    getWeatherGradient
  }
}
