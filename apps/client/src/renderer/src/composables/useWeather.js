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
    temp: '',
    tempMin: '',
    tempMax: '',
    condition: '',
    city: '',
    address: '',
    humidity: '',
    windSpeed: '',
    aqi: '',
    loading: false,
    error: null
  })

  // 天气详情弹窗
  const weatherDetailVisible = ref(false)

  // 获取天气对应的主题渐变色（多层叠加，营造氛围感）
  const getWeatherGradient = () => {
    const condition = weather.value.condition
    const temp = weather.value.temp

    if (!condition || temp === '' || temp === null || temp === undefined) {
      return 'radial-gradient(circle at 30% 20%, rgba(255,255,255,0.12) 0%, transparent 50%), linear-gradient(135deg, #C9C4BC 0%, #A8A29A 100%)'
    }
    if (condition.includes('晴') || condition.includes('热')) {
      return 'radial-gradient(circle at 82% 12%, rgba(255,235,160,0.4) 0%, transparent 45%), radial-gradient(circle at 8% 88%, rgba(255,90,50,0.2) 0%, transparent 50%), linear-gradient(135deg, #FFB347 0%, #FF8C42 45%, #FF6B6B 100%)'
    }
    if (condition.includes('雨')) {
      return 'radial-gradient(circle at 50% 0%, rgba(160,180,220,0.2) 0%, transparent 55%), radial-gradient(circle at 85% 80%, rgba(50,70,130,0.3) 0%, transparent 50%), linear-gradient(135deg, #5B7BAA 0%, #4A6A9E 45%, #3D5A8C 100%)'
    }
    if (condition.includes('雪')) {
      return 'radial-gradient(circle at 25% 10%, rgba(255,255,255,0.5) 0%, transparent 50%), radial-gradient(circle at 75% 75%, rgba(100,140,180,0.25) 0%, transparent 45%), linear-gradient(135deg, #8EB4D4 0%, #70A0C8 50%, #5A8AB8 100%)'
    }
    if (condition.includes('云') || condition.includes('阴')) {
      return 'radial-gradient(ellipse at 30% 15%, rgba(255,255,255,0.15) 0%, transparent 50%), radial-gradient(ellipse at 75% 55%, rgba(140,160,190,0.3) 0%, transparent 45%), linear-gradient(135deg, #5A789A 0%, #4A6888 50%, #385068 100%)'
    }
    if (condition.includes('雷') || condition.includes('暴')) {
      return 'radial-gradient(circle at 50% 0%, rgba(180,140,200,0.15) 0%, transparent 50%), radial-gradient(circle at 80% 70%, rgba(70,50,130,0.3) 0%, transparent 45%), linear-gradient(135deg, #3D3A5C 0%, #2E2A4A 45%, #1E1A38 100%)'
    }
    if (temp > 30) {
      return 'radial-gradient(circle at 80% 15%, rgba(255,220,100,0.4) 0%, transparent 45%), linear-gradient(135deg, #F85636 0%, #F59E2F 50%, #FFD723 100%)'
    }
    if (temp < 10) {
      return 'radial-gradient(circle at 30% 15%, rgba(255,255,255,0.2) 0%, transparent 50%), linear-gradient(135deg, #5B8AB8 0%, #4A7AA8 50%, #7AB0D8 100%)'
    }
    return 'radial-gradient(circle at 75% 15%, rgba(255,230,180,0.3) 0%, transparent 45%), linear-gradient(135deg, #FFB347 0%, #FF8C42 50%, #FF6B6B 100%)'
  }

  // 获取天气主题键（用于 CSS 装饰动画）
  const getWeatherTheme = () => {
    const condition = weather.value.condition
    const temp = weather.value.temp

    if (!condition || temp === '' || temp === null || temp === undefined) return 'default'
    if (condition.includes('晴') || condition.includes('热')) return 'sunny'
    if (condition.includes('雨')) return 'rain'
    if (condition.includes('雪')) return 'snow'
    if (condition.includes('云') || condition.includes('阴')) return 'cloudy'
    if (condition.includes('雷') || condition.includes('暴')) return 'thunder'
    if (temp > 30) return 'hot'
    if (temp < 10) return 'cold'
    return 'default'
  }

  // 获取天气对应的图标
  const getWeatherIcon = () => {
    const condition = weather.value.condition
    if (!condition) return ''

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
    if (!condition) return ''

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
    if (aqi === '' || aqi === null || aqi === undefined) {
      return { text: '暂无数据', color: '#8A857E' }
    }
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

    if (!condition || temp === '' || temp === null || temp === undefined) {
      return '暂无天气建议'
    }

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

    if (temp === '' || temp === null || temp === undefined) {
      return ''
    }
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

    if (!condition || aqi === '' || aqi === null || aqi === undefined) {
      return ''
    }
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
        if (response?.success === false) {
          throw new Error(response.message || '天气服务不可用')
        }

        if (response?.data) {
          weatherData = {
            temp: response.data.temperature ?? '',
            tempMin: response.data.tempMin ?? '',
            tempMax: response.data.tempMax ?? '',
            condition: response.data.condition ?? '',
            humidity: response.data.humidity ?? '',
            windSpeed: response.data.windSpeed ?? '',
            aqi: response.data.aqi ?? '',
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
        if (locationResponse?.success === false) {
          throw new Error(locationResponse.message || '定位服务不可用')
        }

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
          if (weatherResponse?.success === false) {
            throw new Error(weatherResponse.message || '天气服务不可用')
          }

          if (weatherResponse?.data) {
            weatherData = {
              temp: weatherResponse.data.temperature ?? '',
              tempMin: weatherResponse.data.tempMin ?? '',
              tempMax: weatherResponse.data.tempMax ?? '',
              condition: weatherResponse.data.condition ?? '',
              humidity: weatherResponse.data.humidity ?? '',
              windSpeed: weatherResponse.data.windSpeed ?? '',
              aqi: weatherResponse.data.aqi ?? '',
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
    weatherTheme: computed(() => getWeatherTheme()),
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
