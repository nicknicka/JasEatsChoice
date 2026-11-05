<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElNotification, ElMessage } from 'element-plus'
import { useWeather } from '../../composables/useWeather.js'
import { getAvatarUrl } from '../../utils/avatar'
import { useRecommendations } from '../../composables/useRecommendations.js'
// 导入 Element Plus 图标
import {
  Location,
  Calendar,
  Promotion,
  VideoCamera,
  ArrowRight,
  Star,
  Share,
  Coffee,
  Document,
  Check,
  Shop,
  MagicStick,
  Sunny,
  Cloudy,
  Drizzling,
  Lightning
} from '@element-plus/icons-vue'
import CommonMapLocationPicker from '../../components/CommonMapLocationPicker.vue'
import WeatherCanvasFx from '../../components/WeatherCanvasFx.vue'
import { useRouter } from 'vue-router'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
// 导入 WebSocket 常量
import { WS_CONFIG } from '../../constants/wsConstants.js'
// 导入 authStore 和 userStore
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'

const router = useRouter()
const authStore = useAuthStore(pinia)
const userStore = useUserStore(pinia)

// 使用天气组合式函数
const {
  weather,
  weatherDetailVisible,
  showWeatherSkeleton,
  tempRangeText,
  weatherGradient,
  weatherTheme,
  aqiInfo,
  clothingAdvice,
  exerciseAdvice,
  fetchWeather: fetchWeatherData,
  showWeatherDetail,
  getRecommendedDishesSeries: getWeatherRecommendation,
  getLocationHistory,
  clearWeatherCache
} = useWeather()

// 使用智能推荐系统
const { loadAllRecommendations } = useRecommendations()

// 天气图标统一使用线性图标，避免 emoji 在不同系统下渲染不一致
const weatherIconComponent = computed(() => {
  switch (weatherTheme.value) {
    case 'sunny':
    case 'hot':
      return Sunny
    case 'rain':
      return Drizzling
    case 'thunder':
      return Lightning
    default:
      return Cloudy
  }
})

// 默认菜品占位图 - 更精美的设计
const defaultDishImage =
  'data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'480\' height=\'360\' viewBox=\'0 0 480 360\'%3E%3Cdefs%3E%3ClinearGradient id=\'d\' x1=\'0\' y1=\'0\' x2=\'0\' y2=\'1\'%3E%3Cstop offset=\'0\' stop-color=\'%23FBF8F4\'/%3E%3Cstop offset=\'1\' stop-color=\'%23F1E7DB\'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width=\'480\' height=\'360\' fill=\'url(%23d)\'/%3E%3Ccircle cx=\'240\' cy=\'178\' r=\'96\' fill=\'%23EFDDCD\' opacity=\'0.62\'/%3E%3Cg fill=\'none\' stroke=\'%23C67B5C\' stroke-width=\'7\' stroke-linecap=\'round\' stroke-linejoin=\'round\' opacity=\'0.82\'%3E%3Cpath d=\'M170 178h140a10 10 0 0 1 10 10c0 36-29 66-66 66h-28c-37 0-66-30-66-66a10 10 0 0 1 10-10z\'/%3E%3Cpath d=\'M210 168c0-16 16-18 16-34\'/%3E%3Cpath d=\'M254 166c0-18 18-20 18-38\'/%3E%3Cpath d=\'M204 268h72\'/%3E%3C/g%3E%3C/svg%3E'

// 默认教程缩略图 - 更精美的设计
const defaultTutorialThumbnail =
  'data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'480\' height=\'360\' viewBox=\'0 0 480 360\'%3E%3Cdefs%3E%3ClinearGradient id=\'t\' x1=\'0\' y1=\'0\' x2=\'0\' y2=\'1\'%3E%3Cstop offset=\'0\' stop-color=\'%23FAF9F6\'/%3E%3Cstop offset=\'1\' stop-color=\'%23EAEEE6\'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width=\'480\' height=\'360\' fill=\'url(%23t)\'/%3E%3Ccircle cx=\'240\' cy=\'176\' r=\'94\' fill=\'%23DEE9DC\' opacity=\'0.7\'/%3E%3Cg fill=\'none\' stroke=\'%237BAE7F\' stroke-width=\'7\' stroke-linecap=\'round\' stroke-linejoin=\'round\' opacity=\'0.85\'%3E%3Cpath d=\'M168 160h60a12 12 0 0 1 12 12v62a12 12 0 0 1-12 12h-60a12 12 0 0 1-12-12v-62a12 12 0 0 1 12-12z\'/%3E%3Cpath d=\'M252 160h60a12 12 0 0 1 12 12v62a12 12 0 0 1-12 12h-60a12 12 0 0 1-12-12v-62a12 12 0 0 1 12-12z\'/%3E%3Cpath d=\'M180 190h36M180 212h36\'/%3E%3Cpath d=\'M264 190h36M264 212h36\'/%3E%3C/g%3E%3C/svg%3E'

// 图片加载错误处理
const handleImageError = (event) => {
  event.target.src = defaultDishImage
}

const handleTutorialImageError = (event) => {
  if (event.target.dataset.fallbackApplied === 'true') {
    return
  }
  event.target.dataset.fallbackApplied = 'true'
  event.target.src = defaultTutorialThumbnail
}

const normalizeImageUrl = (url, fallback) => {
  const rawUrl = String(url || '').trim().replace(/\\/g, '/')
  if (!rawUrl) {
    return fallback
  }

  if (rawUrl.startsWith('//')) {
    return `${window.location.protocol}${rawUrl}`
  }

  // 内联/绝对地址（blob: 预览、file: 桌面端、https: 外链）原样交给浏览器
  if (/^(data:|blob:|file:|https?:)/i.test(rawUrl)) {
    return rawUrl
  }

  return getAvatarUrl(rawUrl)
}

const isExternalPlaceholderImage = (url) => {
  return /(?:dummyimage\.com|via\.placeholder\.com|placehold\.co)/i.test(url)
}

const resolveDishImage = (image) => {
  const rawImage = String(image || '').trim()
  if (!rawImage || isExternalPlaceholderImage(rawImage)) {
    return defaultDishImage
  }

  if (!/[/:.]/.test(rawImage)) {
    return defaultDishImage
  }

  return normalizeImageUrl(rawImage, defaultDishImage)
}

const getTutorialImageValue = (tutorial = {}) => {
  return (
    tutorial.thumbnail ||
    tutorial.coverImage ||
    tutorial.cover_image ||
    tutorial.imageUrl ||
    tutorial.image_url ||
    tutorial.coverUrl ||
    tutorial.cover_url ||
    tutorial.image ||
    ''
  )
}

const resolveTutorialThumbnail = (tutorial) => {
  return normalizeImageUrl(getTutorialImageValue(tutorial), defaultTutorialThumbnail)
}

// 加载状态
const nearbyLoading = ref(false)
const recommendedDishesLoading = ref(true)
const tutorialsLoading = ref(true)

// 教程数据 - 从后端获取
const featuredTutorials = ref([])

// 今日推荐菜品 - 来自后端
const recommendedDishes = ref([])
// 推荐加载失败标志
const recommendLoadFailed = ref(false)
// 今日热点 - 从后端获取（包含详细信息）
const hotTopic = ref({
  content: '',
  sourceType: '',
  sourceId: '',
  redirectUrl: '',
  clickable: false
})
// 收藏的菜品ID列表
const favoriteDishIds = ref(new Set())

// 位置选择弹窗
const mapLocationPickerVisible = ref(false)
const locationSource = ref('unknown')

// 从后端获取推荐菜品 - 使用智能推荐算法
const fetchRecommendedDishes = async () => {
  recommendedDishesLoading.value = true
  recommendLoadFailed.value = false
  try {
    // 使用智能推荐系统（个性化推荐 + 天气推荐 + 节日推荐）
    const allRecommendations = await loadAllRecommendations()

    // 转换数据格式以兼容轮播组件
    if (allRecommendations && allRecommendations.length > 0) {
      // 取前3个推荐用于首页轮播
      const topRecommendations = allRecommendations.slice(0, 3)

      // 转换数据格式以匹配原recipe格式
      recommendedDishes.value = topRecommendations.map(rec => {
        // 将tags数组转换为字符串（如果存在）
        const tagsString = Array.isArray(rec.tags)
          ? rec.tags.slice(0, 3).join(' ') // 最多取前3个标签
          : (rec.tags || '')

        return {
          id: rec.id,
          name: rec.name,
          image: resolveDishImage(rec.image),
          category: rec.recommendSource || rec.type || '推荐',
          kcal: rec.calories || 0, // 使用kcal字段以兼容模板
          calories: rec.calories || 0, // 同时保留calories字段
          tags: tagsString, // 转换为字符串
          nutrition: rec.nutrition || null,
          reason: rec.reason || '',
          rating: rec.rating || 4.8,
          // 保留原始推荐数据以便后续使用
          _rawRecommendation: rec
        }
      })

      // 预加载图片（如果有真实图片URL）
      preloadImages(recommendedDishes.value)
    } else {
      recommendedDishes.value = []
    }
  } catch (error) {
    console.error('加载推荐菜品失败:', error)
    recommendedDishes.value = []
    recommendLoadFailed.value = true
    showError('加载推荐菜品失败,请检查网络连接')
  } finally {
    recommendedDishesLoading.value = false
  }
}

// 从后端获取今日热点
const fetchHotTopic = async () => {
  try {
    const response = await retryFetch(async () => {
      return await api.get(API_CONFIG.home.hotTopic)
    })

    if (response.data) {
      // 新API返回的是对象，包含content、sourceType、clickable等信息
      if (typeof response.data === 'object') {
        hotTopic.value = response.data
      } else {
        // 兼容旧API（返回字符串）
        hotTopic.value = {
          content: response.data,
          clickable: false
        }
      }
    } else {
      // 接口成功但返回空数据时,清空热点
      hotTopic.value = { content: '', clickable: false }
    }
  } catch (error) {
    console.error('加载今日热点失败:', error)
    // 请求失败时使用默认文本
    hotTopic.value = { content: '', clickable: false }
    // 热点不是关键功能,只记录错误不显示通知
  }
}

// 处理热点点击
const handleHotTopicClick = () => {
  // 保存热点数据到 localStorage，供详情页使用
  localStorage.setItem('currentHotTopic', JSON.stringify(hotTopic.value))

  // 记录点击
  api.post(API_CONFIG.home.hotTopicClick, { content: hotTopic.value.content }).catch(err => {
    console.error('记录热点点击失败:', err)
  })

  // 跳转到热点详情页
  router.push('/user/home/hot-topic')
}

const TRUSTED_LOCATION_SOURCES = ['gps', 'manual', 'search']

// 处理位置选择
const handleLocationSelected = async (locationData) => {
  const { address, source, position } = locationData

  locationSource.value = source || 'manual'

  // 更新天气位置信息
  weather.value.address = address || '已选择位置'
  weather.value.city = extractCityFromAddress(address)
  weather.value.locationSource = locationSource.value

  // 获取详细天气信息
  fetchWeather(weather.value.city)

  // 保存位置到后端
  try {
    const userId = authStore.userId
    if (userId) {
      await api.put(
        API_CONFIG.user.update.replace('{userId}', userId),
        { location: address }
      )

      if (TRUSTED_LOCATION_SOURCES.includes(locationSource.value) && position?.lng && position?.lat) {
        localStorage.setItem('user_last_location', JSON.stringify({
          address,
          lng: position.lng,
          lat: position.lat,
          timestamp: Date.now(),
          source: locationSource.value,
          accuracy: locationSource.value
        }))
      }

      // 更新本地用户信息
      if (userStore.userInfo) {
        userStore.userInfo.location = address
      }

      console.log('位置已保存到后端:', address)
    }
  } catch (error) {
    console.error('保存位置失败:', error)
    // 不影响主流程，只记录错误
  }

  ElMessage.success(`已选择位置：${address}`)
}

// 从地址中提取城市名称
const extractCityFromAddress = (address) => {
  if (!address) return ''

  // 简单的提取逻辑，可以根据实际地址格式调整
  const cityMatch = address.match(/(北京市|上海市|广州市|深圳市|杭州市|成都市|武汉市|西安市|南京市|重庆市|天津市|青岛市|大连市|厦门市|苏州市|无锡市|宁波市|长沙市|郑州市)/)
  if (cityMatch) {
    return cityMatch[1].replace('市', '')
  }

  // 如果没有匹配到，尝试提取省/市
  const parts = address.split('省')
  if (parts.length > 1) {
    const cityParts = parts[1].split('市')
    if (cityParts.length > 1) {
      return cityParts[0]
    }
  }

  return ''
}

// 从 localStorage 读取上次保存的位置
const loadLastLocation = async () => {
  try {
    const stored = localStorage.getItem('user_last_location')
    if (stored) {
      const locationData = JSON.parse(stored)
      locationSource.value = locationData.source || 'cache'

      // 检查是否过期（24小时内有效）
      const LOCATION_CACHE_DURATION = 24 * 60 * 60 * 1000
      if (!TRUSTED_LOCATION_SOURCES.includes(locationSource.value)) {
        localStorage.removeItem('user_last_location')
        return
      }

      if (Date.now() - locationData.timestamp < LOCATION_CACHE_DURATION) {
        const { lng, lat, address } = locationData

        if (address) {
          weather.value.address = address
          weather.value.city = extractCityFromAddress(address)
          weather.value.locationSource = locationSource.value
          console.log('已加载上次保存的位置:', address, locationSource.value)
          return
        }

        // 调用逆地理编码获取地址
        try {
          const locationApi = (await import('../../api/location.js')).default
          const response = await locationApi.reverseGeocode(lng.toString(), lat.toString())

          if (response && (response.success === true || response.code === '200') && response.data) {
            const address = response.data.formattedAddress || response.data.address
            weather.value.address = address || ''
            weather.value.city = extractCityFromAddress(address)
            weather.value.locationSource = locationSource.value
            console.log('已加载上次保存的位置:', address, locationSource.value)
          }
        } catch (error) {
          console.warn('获取上次位置地址失败:', error)
        }
      }
    }
  } catch (error) {
    console.warn('读取上次位置失败:', error)
  }
}

// 从后端获取位置和天气数据
const fetchWeather = async (selectedCity = null) => {
  try {
    await fetchWeatherData(selectedCity, {
      onRetry: () => fetchWeather(selectedCity),
      onManualSelect: () => {
        mapLocationPickerVisible.value = true
      }
    })
  } catch (error) {
    console.error('加载天气失败:', error)
  }
}

// 处理查找附近商家
const handleNearbySearch = async () => {
  nearbyLoading.value = true
  try {
    await router.push('/user/home/merchants')
  } finally {
    // 延迟重置加载状态,确保用户看到反馈
    setTimeout(() => {
      nearbyLoading.value = false
    }, 500)
  }
}

// 处理教程卡片点击
const handleTutorialClick = (tutorial) => {
  // 跳转到教程详情页
  const tutorialId = tutorial.id
  if (tutorialId) {
    router.push(`/user/home/tutorials/${tutorialId}`)
  } else {
    console.warn('教程缺少ID:', tutorial)
  }
}

// 处理菜品卡片点击
const handleDishClick = (dish) => {
  console.log('点击菜品:', dish.name)
  // 跳转到菜品详情页,需要后端提供菜品详情API和路由
  // router.push(`/user/home/dish/${dish.id}`)
}

// 首页展示的推荐菜品
const filteredDishes = computed(() => recommendedDishes.value)

// 推荐空状态消息
const recommendEmptyMessage = computed(() => {
  return recommendLoadFailed.value ? '加载失败,请重试' : '暂无推荐菜品'
})

// 热量统一格式
const formatKcal = (value) => {
  const kcal = Number(value)
  return Number.isFinite(kcal) && kcal > 0 ? `${Math.round(kcal)} kcal` : '热量待补'
}

// 问候语
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 今日日期
const todayDate = computed(() => {
  const now = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${weekdays[now.getDay()]}`
})


// 错误提示函数
const showError = (message, duration = 3000) => {
  ElNotification.error({
    title: '错误',
    message,
    duration
  })
}

const showSuccess = (message, duration = 2000) => {
  ElNotification.success({
    title: '成功',
    message,
    duration
  })
}

// 带重试机制的请求函数
const retryFetch = async (fetchFn, maxRetries = 3, delay = 1000) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await fetchFn()
    } catch (error) {
      console.error(`请求失败 (尝试 ${i + 1}/${maxRetries}):`, error)
      if (i === maxRetries - 1) {
        throw error
      }
      // 指数退避
      await new Promise((resolve) => setTimeout(resolve, delay * Math.pow(2, i)))
    }
  }
}

// WebSocket 连接
let wsAttempts = 0
const maxAttempts = 3 // 减少最大重连次数
let wsAuthenticated = false // 添加认证状态标志

// 使用主进程 WebSocket 初始化带有自动重连功能的 WebSocket 连接
const initializeWebSocket = () => {
  // 检查 token 是否存在
  const token = authStore.token
  const userId = authStore.userId

  if (!token) {
    console.error('❌ 无法连接 WebSocket: token 不存在，请先登录')
    ElMessage.error('未登录，无法连接实时消息服务')
    return
  }

  if (!userId) {
    console.error('❌ 无法连接 WebSocket: userId 不存在')
    ElMessage.error('用户信息不完整，请重新登录')
    return
  }

  // 使用 WebSocket 常量构建完整 URL，并添加认证参数
  const wsUrl = `${WS_CONFIG.URL}${WS_CONFIG.ENDPOINT}?userId=${userId}&token=${token}`

  console.log('🔌 Connecting to WebSocket server:', wsUrl)
  console.log('📝 当前用户:', userId, 'Token存在:', !!token)

  // 通过 IPC 使用主进程的 WebSocket
  if (window.api) {
    window.api.connectWebSocket(wsUrl)
  } else {
    console.warn('WebSocket API not available')
  }
}

// 发送 WebSocket 消息
const sendWebSocketMessage = (message) => {
  if (window.api) {
    window.api.sendWebSocketMessage(message)
  } else {
    console.error('API not available, cannot send WebSocket message')
  }
}

// WebSocket 事件处理器设置
const listenersRegistered =
  window.api?.webSocketListenersRegistered || window.webSocketListenersRegistered
if (!listenersRegistered && window.api) {
  // 监听来自主进程的 WebSocket 事件
  window.api?.onWebSocketOpen(() => {
    console.log('✅ WebSocket 连接已建立')
    wsAuthenticated = true // 标记为已认证（握手阶段已完成）
    wsAttempts = 0 // 重置重连计数器
    console.log('🔐 认证成功，userId:', authStore.userId)
  })

  window.api?.onWebSocketMessage((message) => {
    console.log('WebSocket message received:', message)

    // 处理字符串和 Uint8Array 类型的消息
    let messageString
    if (message instanceof Uint8Array) {
      // 使用 UTF-8 将 Uint8Array 解码为字符串
      messageString = new TextDecoder().decode(message)
    } else if (typeof message === 'string') {
      messageString = message
    } else {
      console.error('Unknown WebSocket message type:', typeof message)
      return
    }

    try {
      // 解析 JSON 消息
      const parsedMessage = JSON.parse(messageString)
      console.log('Parsed WebSocket message:', parsedMessage)

      const { msgType, content, fromId, toId } = parsedMessage

      switch (msgType) {
        case 'auth':
          console.log('Authentication response:', content)
          // 标记认证成功
          wsAuthenticated = true
          wsAttempts = 0 // 重置重连计数器
          break

        case 'orderUpdate':
          console.log('Order update received:', content)
          // 更新订单状态的UI
          break

        case 'chat':
          console.log('Chat message from', fromId, 'to', toId, ':', content)
          // 更新聊天UI
          break

        case 'system':
          console.log('System message:', content)
          // 显示系统通知
          break

        default:
          console.log('Unknown message type:', msgType)
      }
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error)
      console.error('Message content:', messageString)
    }
  })

  window.api?.onWebSocketClose((code, reason) => {
    console.log('⚠️ WebSocket 连接已关闭')
    console.log('📊 关闭代码:', code)
    console.log('📝 关闭原因:', reason)

    // 常见错误码说明
    let errorDesc = ''
    switch (code) {
      case 1000:
        errorDesc = '正常关闭'
        break
      case 1001:
        errorDesc = '端点离开'
        break
      case 1002:
        errorDesc = '协议错误'
        break
      case 1003:
        errorDesc = '不支持的数据类型'
        break
      case 1006:
        errorDesc = '连接异常关闭'
        break
      case 1007:
        errorDesc = '数据类型不一致'
        break
      case 1008:
        errorDesc = '违反政策'
        break
      case 1009:
        errorDesc = '消息过大'
        break
      case 1010:
        errorDesc = '缺少扩展'
        break
      case 1011:
        errorDesc = '内部错误'
        break
      case 1015:
        errorDesc = 'TLS握手失败'
        break
      default:
        errorDesc = `未知错误 (${code})`
    }
    console.log('❌ 错误描述:', errorDesc)

    // 如果已经认证成功但连接关闭，不重连（避免频繁重连）
    // 如果未达到最大尝试次数则自动重连
    if (!wsAuthenticated && wsAttempts < maxAttempts) {
      wsAttempts++
      const delay = Math.min(5000 * wsAttempts, 30000) // 增加初始延迟到 5 秒
      console.log(`🔄 ${delay / 1000}秒后尝试第 ${wsAttempts}/${maxAttempts} 次重连...`)
      setTimeout(() => {
        initializeWebSocket()
      }, delay)
    } else if (wsAuthenticated) {
      console.log('ℹ️ WebSocket 已认证成功但连接关闭，可能是服务端问题，停止重连')
    } else {
      console.error('❌ WebSocket 已达到最大重连次数，停止重连')
      if (code === 1006) {
        ElMessage.error('连接服务器失败，请检查网络或重新登录')
      }
    }
  })

  window.api?.onWebSocketError((error) => {
    console.error('❌ WebSocket 错误:', error)

    // 检查是否是认证错误
    if (error && error.message && error.message.includes('401')) {
      console.error('🔐 认证失败，可能的原因：')
      console.error('  1. Token 已过期')
      console.error('  2. Token 无效')
      console.error('  3. 未登录')

      ElMessage.error({
        message: '认证失败，请重新登录',
        duration: 5000,
        showClose: true
      })
    }
  })

  // 在添加属性之前检查 api 是否可扩展
  if (window.api && Object.isExtensible(window.api)) {
    window.api.webSocketListenersRegistered = true
  } else {
    // 如果api对象不可扩展，则使用一个单独的变量
    window.webSocketListenersRegistered = true
  }
}

// 从后端获取精选教程数据
const fetchFeaturedTutorials = async () => {
  tutorialsLoading.value = true
  try {
    const response = await retryFetch(async () => {
      return await api.get(API_CONFIG.tutorial.featured)
    })

    const tutorials = Array.isArray(response)
      ? response
      : Array.isArray(response?.data)
        ? response.data
        : []

    featuredTutorials.value = tutorials.map((tutorial) => ({
      ...tutorial,
      title: tutorial.title || tutorial.name || '教程',
      thumbnail: resolveTutorialThumbnail(tutorial),
      type: tutorial.type || 'article'
    }))
  } catch (error) {
    console.error('加载精选教程失败:', error)
    featuredTutorials.value = []
    showError('加载教程失败,请稍后重试')
  } finally {
    tutorialsLoading.value = false
  }
}

// 图片预加载功能
const preloadImages = (items) => {
  items.forEach((item) => {
    const imageUrl = item.image || item.thumbnail
    // 只预加载真实图片URL（跳过emoji和data: URL）
    if (imageUrl && !imageUrl.startsWith('data:') && !imageUrl.startsWith('http')) {
      try {
        const img = new Image()
        img.src = imageUrl
      } catch (error) {
        console.warn('图片预加载失败:', imageUrl, error)
      }
    }
  })
}

// 收藏功能
const loadFavorites = () => {
  const saved = localStorage.getItem('favoriteDishes')
  if (saved) {
    try {
      favoriteDishIds.value = new Set(JSON.parse(saved))
    } catch (error) {
      console.error('加载收藏失败:', error)
      favoriteDishIds.value = new Set()
    }
  }
}

const saveFavorites = () => {
  localStorage.setItem('favoriteDishes', JSON.stringify([...favoriteDishIds.value]))
}

const isFavorite = (dish) => {
  return favoriteDishIds.value.has(dish.id || dish.name)
}

const toggleFavorite = (dish, event) => {
  event.stopPropagation() // 阻止事件冒泡,避免触发卡片点击
  const dishId = dish.id || dish.name

  if (favoriteDishIds.value.has(dishId)) {
    favoriteDishIds.value.delete(dishId)
    showSuccess(`已取消收藏: ${dish.name}`)
  } else {
    favoriteDishIds.value.add(dishId)
    showSuccess(`已收藏: ${dish.name}`)
  }

  saveFavorites()
}

// 分享功能
const shareDish = async (dish, event) => {
  event.stopPropagation()

  const shareData = {
    title: dish.name,
    text: `${dish.name} - ${dish.kcal} 卡路里`,
    url: window.location.href
  }

  try {
    if (navigator.share) {
      await navigator.share(shareData)
      showSuccess('分享成功')
    } else {
      // 降级处理:复制到剪贴板
      const shareText = `${shareData.title}\n${shareData.text}\n${shareData.url}`
      await navigator.clipboard.writeText(shareText)
      showSuccess('已复制到剪贴板')
    }
  } catch (error) {
    if (error.name !== 'AbortError') {
      console.error('分享失败:', error)
      showError('分享失败,请重试')
    }
  }
}

// 在挂载时初始化WebSocket
onMounted(async () => {
  loadLastLocation() // 加载上次保存的位置
  loadFavorites() // 加载收藏列表
  fetchFeaturedTutorials()
  await fetchWeather()
  fetchHotTopic() // 新增：获取今日热点

  if (window.api) {
    initializeWebSocket()
  }
})

watch(
  () => authStore.userId || localStorage.getItem('auth_userId'),
  (userId) => {
    if (userId) fetchRecommendedDishes()
  },
  { immediate: true }
)
</script>


<template>
  <div class="savour-home">
    <!-- 噪点纹理 -->
    <div class="noise-layer"></div>

    <!-- 可滚动内容 -->
    <div class="home-scroll-container">
      <!-- 英雄区域：问候 + 天气 -->
      <section class="hero-section fade-in-up">
        <div class="hero-grid">
          <!-- 左侧：问候与快捷操作 -->
          <div class="hero-greeting">
            <p class="greeting-label">{{ greetingText }}</p>
            <h1 class="greeting-title">发现今日美味</h1>
            <p class="greeting-date">{{ todayDate }}</p>
            <div class="hero-actions">
              <button
                class="action-pill pill-primary"
                @click="handleNearbySearch"
                :disabled="nearbyLoading"
              >
                <el-icon class="pill-icon"><Location /></el-icon>
                <span>附近商家</span>
              </button>
              <button class="action-pill" @click="router.push('/user/home/ai')">
                <el-icon class="pill-icon"><MagicStick /></el-icon>
                <span>AI 助手</span>
              </button>
              <button class="action-pill" @click="router.push('/user/home/today-recipe')">
                <el-icon class="pill-icon"><Calendar /></el-icon>
                <span>今日食谱</span>
              </button>
            </div>
          </div>

          <!-- 右侧：天气卡片 -->
          <div class="hero-weather fade-in-up-delay">
            <!-- 骨架屏 -->
            <div v-if="showWeatherSkeleton" class="weather-skeleton">
              <el-skeleton animated>
                <template #template>
                  <div style="padding: 24px; display: flex; flex-direction: column; gap: 12px;">
                    <el-skeleton-item variant="text" style="width: 60%; height: 20px" />
                    <el-skeleton-item variant="text" style="width: 40%; height: 48px" />
                    <el-skeleton-item variant="text" style="width: 80%; height: 16px" />
                    <el-skeleton-item variant="text" style="width: 70%; height: 16px" />
                  </div>
                </template>
              </el-skeleton>
            </div>

            <!-- 天气卡片 -->
            <div v-else class="weather-card scale-in" :data-theme="weatherTheme" :style="{ background: weatherGradient }">
              <WeatherCanvasFx :theme="weatherTheme" />
              <div class="weather-inner">
                <div class="weather-temp-row">
                  <el-icon class="weather-icon"><component :is="weatherIconComponent" /></el-icon>
                  <span class="weather-temp-value">{{ weather.temp }}°</span>
                </div>
                <div v-if="tempRangeText" class="weather-range">{{ tempRangeText }}</div>
                <div class="weather-condition" @click="showWeatherDetail">
                  {{ weather.condition || '未知天气' }}
                </div>
                <div class="weather-location">
                  <el-icon class="weather-location-icon"><Location /></el-icon>
                  <span class="location-name">{{ weather.address || weather.city || '当前位置' }}</span>
                  <button class="location-change-btn" @click="mapLocationPickerVisible = true">
                    更换位置
                  </button>
                </div>
                <div class="weather-recommendation">
                  <el-icon class="sparkle"><MagicStick /></el-icon>
                  <span class="rec-text">{{ getWeatherRecommendation() }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 今日推荐 -->
      <section class="recs-section fade-in-up-delay-200">
        <div class="section-header-row">
          <div>
            <h2 class="section-title">今日推荐</h2>
            <p class="section-subtitle">根据你的口味和天气精心挑选</p>
          </div>
        </div>

        <!-- 骨架屏 -->
        <div v-if="recommendedDishesLoading" class="recs-scroll">
          <div v-for="i in 3" :key="i" class="rec-skeleton">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; height: 200px; border-radius: 16px" />
                <div style="padding: 16px;">
                  <el-skeleton-item variant="h3" style="width: 70%" />
                  <el-skeleton-item variant="text" style="width: 40%; margin-top: 8px" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else-if="filteredDishes.length === 0" class="recs-empty">
          <div class="empty-icon-wrapper">
            <el-icon :size="48"><Coffee /></el-icon>
          </div>
          <p class="empty-text">{{ recommendEmptyMessage }}</p>
          <button class="retry-btn" @click="fetchRecommendedDishes">重新加载</button>
        </div>

        <!-- 推荐卡片 -->
        <div v-else class="rec-grid">
          <div
            v-for="dish in filteredDishes"
            :key="dish.id"
            class="rec-card"
            @click="handleDishClick(dish)"
            tabindex="0"
            @keyup.enter="handleDishClick(dish)"
          >
            <div class="rec-card-image">
              <img :src="dish.image || defaultDishImage" :alt="dish.name" loading="lazy" @error="handleImageError" />
              <span class="rec-category-badge">{{ dish.category || '推荐' }}</span>
              <div class="rec-card-actions">
                <button class="rec-action-btn" @click="shareDish(dish, $event)" title="分享">
                  <el-icon><Share /></el-icon>
                </button>
                <button
                  class="rec-action-btn"
                  :class="{ 'is-fav': isFavorite(dish) }"
                  @click="toggleFavorite(dish, $event)"
                  :title="isFavorite(dish) ? '取消收藏' : '收藏'"
                >
                  <el-icon><Star /></el-icon>
                </button>
              </div>
            </div>
            <div class="rec-card-body">
              <h3 class="rec-dish-name">{{ dish.name }}</h3>
              <div class="rec-dish-meta">
                <span class="rec-kcal">{{ formatKcal(dish.kcal) }}</span>
                <span v-if="dish.tags" class="rec-tags">{{ dish.tags }}</span>
              </div>
              <div class="rec-rating">
                <el-rate
                  v-if="dish.rating && dish.rating > 0"
                  v-model="dish.rating"
                  disabled
                  show-score
                  size="small"
                />
                <span v-else class="no-rating">暂无评分</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 今日热点 -->
      <section v-if="hotTopic.content" class="hot-section fade-in-up-delay-300" @click="handleHotTopicClick">
        <div class="hot-card" :class="{ clickable: hotTopic.clickable }">
          <div class="hot-left">
            <span class="hot-emoji"><el-icon><Promotion /></el-icon></span>
            <span class="hot-badge">HOT</span>
          </div>
          <div class="hot-center">
            <span class="hot-label">今日热点</span>
            <p class="hot-content">{{ hotTopic.content }}</p>
          </div>
          <el-icon v-if="hotTopic.clickable" class="hot-arrow"><ArrowRight /></el-icon>
        </div>
      </section>

      <!-- 美食教程 -->
      <section class="tutorials-section fade-in-up-delay-400">
        <div class="section-header-row">
          <div>
            <h2 class="section-title">美食教程</h2>
            <p class="section-subtitle">跟着大厨学做菜</p>
          </div>
          <button class="view-all-link" @click="router.push('/user/home/tutorials?fromSidebar=true')">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>

        <!-- 教程骨架屏 -->
        <div v-if="tutorialsLoading" class="tuts-scroll">
          <div v-for="i in 3" :key="i" class="tut-skeleton">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" style="width: 100%; height: 140px; border-radius: 12px" />
                <div style="padding: 12px;">
                  <el-skeleton-item variant="h3" style="width: 80%" />
                  <el-skeleton-item variant="text" style="width: 50%; margin-top: 8px" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>

        <!-- 空教程 -->
        <div v-else-if="featuredTutorials.length === 0" class="tuts-empty">
          <div class="empty-icon-wrapper">
            <el-icon :size="48"><Document /></el-icon>
          </div>
          <p class="empty-text">暂无教程数据</p>
          <button class="retry-btn" @click="fetchFeaturedTutorials">重新加载</button>
        </div>

        <!-- 教程卡片 -->
        <div v-else class="tut-grid">
          <div
            v-for="tutorial in featuredTutorials.slice(0, 4)"
            :key="tutorial.id || tutorial.name || tutorial.title"
            class="tut-card"
            @click="handleTutorialClick(tutorial)"
            tabindex="0"
            @keyup.enter="handleTutorialClick(tutorial)"
          >
            <div class="tut-image">
              <img
                :src="tutorial.thumbnail || defaultTutorialThumbnail"
                :alt="tutorial.name || tutorial.title"
                loading="lazy"
                @error="handleTutorialImageError"
              />
              <div class="tut-type-badge">
                <el-icon v-if="tutorial.type === 'video'"><VideoCamera /></el-icon>
                <el-icon v-else><MagicStick /></el-icon>
                <span>{{ tutorial.type === 'video' ? '视频' : '图文' }}</span>
              </div>
            </div>
            <div class="tut-info">
              <div class="tut-badges">
                <el-tag v-if="tutorial.source_type === 'ADMIN' && tutorial.is_official" type="danger" size="small" effect="dark">
                  <el-icon><Check /></el-icon> 官方
                </el-tag>
                <el-tag v-if="tutorial.source_type === 'MERCHANT'" type="warning" size="small" effect="plain">
                  <el-icon><Shop /></el-icon> {{ tutorial.merchantName || '商家' }}
                </el-tag>
                <el-tag v-if="tutorial.source_type === 'AI_GENERATED'" :type="tutorial.review_status === 'APPROVED' ? 'success' : 'info'" size="small" effect="plain">
                  <el-icon><MagicStick /></el-icon> AI
                </el-tag>
              </div>
              <h4 class="tut-title">{{ tutorial.name || tutorial.title }}</h4>
              <div class="tut-meta">
                <span class="tut-duration">{{ tutorial.duration || '5分钟' }}</span>
                <el-rate v-if="tutorial.rating" v-model="tutorial.rating" disabled size="small" show-score />
              </div>
            </div>
          </div>
        </div>
      </section>

      <div class="bottom-spacer"></div>
    </div>

    <!-- 天气详情弹窗 -->
    <el-dialog v-model="weatherDetailVisible" title="天气详情" width="480px" class="savour-dialog">
      <div class="weather-detail">
        <div class="detail-row">
          <span class="detail-label">当前温度</span>
          <span class="detail-value">{{ weather.temp }}°C</span>
        </div>
        <div v-if="tempRangeText" class="detail-row">
          <span class="detail-label">温度范围</span>
          <span class="detail-value">{{ tempRangeText }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">天气状况</span>
          <span class="detail-value weather-detail-condition">
            <el-icon><component :is="weatherIconComponent" /></el-icon>
            {{ weather.condition || '未知' }}
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">湿度</span>
          <span class="detail-value">{{ weather.humidity }}%</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">风速</span>
          <span class="detail-value">{{ weather.windSpeed }} m/s</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">空气质量</span>
          <span class="detail-value" :style="{ color: aqiInfo.color }">{{ aqiInfo.text }} (AQI: {{ weather.aqi }})</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">位置</span>
          <span class="detail-value">{{ weather.city }} {{ weather.address }}</span>
        </div>
        <div class="detail-advice-section">
          <div class="advice-card">
            <span class="advice-label">穿衣建议</span>
            <p class="advice-text">{{ clothingAdvice }}</p>
          </div>
          <div class="advice-card">
            <span class="advice-label">运动建议</span>
            <p class="advice-text">{{ exerciseAdvice }}</p>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 地图位置选择弹窗 -->
    <CommonMapLocationPicker
      v-model:visible="mapLocationPickerVisible"
      @location-selected="handleLocationSelected"
    />
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

// ===== Savour 设计系统 =====
@savour-bg: #F6F3ED;
@savour-surface: #FFFFFF;
@savour-text: #2D2A26;
@savour-text-sec: #8A857E;
@savour-text-muted: #B5AFA6;
@savour-accent: #C67B5C;
@savour-accent-hover: #B56A4A;
@savour-accent-light: #F4E6DE;
@savour-green: #7BAE7F;
@savour-gold: #D4A855;
@savour-border: #E8E2D8;
@savour-radius: 16px;
@savour-radius-lg: 24px;
@savour-pill: 100px;

.font-display() {
  font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', 'STSong', serif;
}

// ===== 动画 =====
@keyframes savour-fade-up {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes savour-scale-in {
  from { opacity: 0; transform: scale(0.96); }
  to { opacity: 1; transform: scale(1); }
}

@keyframes sparkle-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.15); opacity: 0.7; }
}

@keyframes weather-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}


@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.fade-in-up { animation: savour-fade-up 0.5s ease both; }
.fade-in-up-delay { animation: savour-fade-up 0.5s ease 0.1s both; }
.fade-in-up-delay-100 { animation: savour-fade-up 0.5s ease 0.15s both; }
.fade-in-up-delay-200 { animation: savour-fade-up 0.5s ease 0.25s both; }
.fade-in-up-delay-300 { animation: savour-fade-up 0.5s ease 0.35s both; }
.fade-in-up-delay-400 { animation: savour-fade-up 0.5s ease 0.45s both; }
.scale-in { animation: savour-scale-in 0.4s ease both; }

// ===== 主容器 =====
.savour-home {
  width: 100%;
  height: 100%;
  position: relative;
  background: @savour-bg;
  overflow: hidden;
  box-sizing: border-box;

  // 噪点纹理
  .noise-layer {
    position: fixed;
    inset: 0;
    pointer-events: none;
    z-index: 9999;
    opacity: 0.02;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
    background-repeat: repeat;
    background-size: 256px;
  }

  .home-scroll-container {
    width: 100%;
    height: 100%;
    overflow-y: auto;
    overflow-x: hidden;
    box-sizing: border-box;
    padding: @nordic-space-lg @nordic-space-lg @nordic-space-2xl;

    > section {
      width: 100%;
      max-width: 1200px;
      margin-left: auto;
      margin-right: auto;
    }

    &::-webkit-scrollbar { width: 6px; }
    &::-webkit-scrollbar-track { background: transparent; }
    &::-webkit-scrollbar-thumb {
      background: @savour-border;
      border-radius: 3px;
      &:hover { background: @savour-text-muted; }
    }
  }
}

// ===== 英雄区域 =====
.hero-section {
  margin-bottom: @nordic-space-lg;

  .hero-grid {
    display: grid;
    grid-template-columns: minmax(0, 7fr) minmax(0, 5fr);
    gap: @nordic-space-lg;
    align-items: stretch;
  }

  .hero-greeting {
    min-height: 196px;
    display: flex;
    flex-direction: column;

    .greeting-label {
      .font-display();
      font-size: @nordic-text-md;
      color: @savour-accent;
      font-weight: 600;
      margin: 0 0 @nordic-space-sm;
      letter-spacing: 0.5px;
    }

    .greeting-title {
      .font-display();
      font-size: 36px;
      font-weight: 700;
      color: @savour-text;
      margin: 0 0 @nordic-space-sm;
      letter-spacing: -0.5px;
      line-height: 1.2;
    }

    .greeting-date {
      font-size: @nordic-text-sm;
      color: @savour-text-sec;
      margin: 0 0 @nordic-space-lg;
    }

    .hero-actions {
      display: flex;
      gap: @nordic-space-sm;
      flex-wrap: wrap;

      .action-pill {
        display: inline-flex;
        align-items: center;
        gap: @nordic-space-sm;
        padding: 10px 20px;
        border-radius: @savour-pill;
        border: 1.5px solid @savour-border;
        background: @savour-surface;
        color: @savour-text;
        font-size: @nordic-text-sm;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.25s ease;
        white-space: nowrap;

        .pill-icon { font-size: @nordic-text-md; }

        &:hover {
          border-color: @savour-accent;
          background: @savour-accent-light;
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(198, 123, 92, 0.15);
        }

        &:active { transform: translateY(0); }

        &.pill-primary {
          background: @savour-accent;
          color: #fff;
          border-color: @savour-accent;

          &:hover {
            background: @savour-accent-hover;
            border-color: @savour-accent-hover;
            box-shadow: 0 4px 16px rgba(198, 123, 92, 0.3);
          }
        }

        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
          transform: none;
        }
      }

      margin-top: auto;
    }
  }

  .hero-weather {
    .weather-skeleton {
      min-height: 196px;
      background: @savour-surface;
      border-radius: @savour-radius-lg;
      border: 1px solid @savour-border;
      overflow: hidden;
    }

    .weather-card {
      --card-glass-bg: rgba(255, 255, 255, 0.15);
      --card-glass-bg-hover: rgba(255, 255, 255, 0.2);
      --card-glass-border: rgba(255, 255, 255, 0.22);
      --card-glass-border-strong: rgba(255, 255, 255, 0.3);
      --card-text: #fff;
      --card-text-gradient: linear-gradient(180deg, #fff 0%, rgba(255, 255, 255, 0.82) 100%);
      --card-text-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
      --card-subtle-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);

      border-radius: @savour-radius-lg;
      overflow: hidden;
      position: relative;
      min-height: 196px;
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15), inset 0 1px 0 rgba(255, 255, 255, 0.3);
      transition: transform 0.3s ease, box-shadow 0.3s ease;

      &::before {
        content: '';
        position: absolute;
        inset: 0;
        background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
        background-size: 200px 200px;
        opacity: 0.09;
        pointer-events: none;
      }

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.3);
      }

      .weather-inner {
        position: relative;
        z-index: 1;
        padding: 24px;
        display: flex;
        flex-direction: column;
        gap: 12px;
        color: var(--card-text);
      }

      .weather-temp-row {
        display: flex;
        align-items: center;
        gap: 12px;

        .weather-icon {
          font-size: 40px;
          color: #fff;
          filter: drop-shadow(0 3px 8px rgba(0, 0, 0, 0.25));
          animation: weather-float 3s ease-in-out infinite;
        }

        .weather-temp-value {
          font-size: 56px;
          font-weight: 800;
          line-height: 1;
          letter-spacing: -2px;
          background: var(--card-text-gradient);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
          filter: drop-shadow(var(--card-text-shadow));
        }
      }

      .weather-range {
        font-size: 18px;
        font-weight: 500;
        opacity: 0.9;
        text-shadow: var(--card-subtle-shadow);
        margin-top: -4px;
      }

      .weather-condition {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 6px 16px;
        background: var(--card-glass-bg);
        backdrop-filter: blur(10px);
        border-radius: 20px;
        font-size: 14px;
        font-weight: 600;
        cursor: pointer;
        transition: background 0.2s ease;
        border: 1px solid var(--card-glass-border);
        align-self: flex-start;
        text-shadow: var(--card-subtle-shadow);

        &:hover { background: var(--card-glass-bg-hover); }
      }

      .weather-location {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;

        .location-name {
          font-weight: 500;
          opacity: 0.95;
          max-width: 200px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .location-change-btn {
          padding: 4px 12px;
          background: var(--card-glass-bg);
          border: 1px solid var(--card-glass-border-strong);
          border-radius: 12px;
          color: var(--card-text);
          font-size: 12px;
          cursor: pointer;
          backdrop-filter: blur(8px);
          transition: all 0.2s ease;

          &:hover {
            background: var(--card-glass-bg-hover);
            transform: translateY(-1px);
          }
        }
      }

      .weather-recommendation {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 16px;
        background: var(--card-glass-bg);
        backdrop-filter: blur(10px);
        border-radius: 12px;
        border: 1px solid var(--card-glass-border);
        font-size: 14px;
        font-weight: 600;
        margin-top: 4px;

        .sparkle {
          animation: sparkle-pulse 2s ease-in-out infinite;
          font-size: 18px;
        }

        .rec-text {
          opacity: 0.95;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    @keyframes weather-thunder-flash {
      0%, 78%, 100% { opacity: 0; }
      80% { opacity: 0.35; }
      83% { opacity: 0; }
      86% { opacity: 0.5; }
      89% { opacity: 0; }
    }

    [data-theme='sunny'],
    [data-theme='hot'] {
      --card-glass-bg: rgba(255, 244, 220, 0.22);
      --card-glass-bg-hover: rgba(255, 244, 220, 0.3);
      --card-glass-border: rgba(255, 244, 220, 0.4);
      --card-glass-border-strong: rgba(255, 244, 220, 0.5);
      --card-text-gradient: linear-gradient(180deg, #fffaf0 0%, rgba(255, 250, 240, 0.85) 100%);
    }

    [data-theme='rain'] {
      --card-glass-bg: rgba(255, 255, 255, 0.12);
      --card-glass-bg-hover: rgba(255, 255, 255, 0.18);
      --card-glass-border: rgba(255, 255, 255, 0.2);
      --card-glass-border-strong: rgba(255, 255, 255, 0.28);
    }

    [data-theme='snow'],
    [data-theme='cold'] {
      --card-glass-bg: rgba(245, 250, 255, 0.28);
      --card-glass-bg-hover: rgba(245, 250, 255, 0.4);
      --card-glass-border: rgba(255, 255, 255, 0.45);
      --card-glass-border-strong: rgba(255, 255, 255, 0.55);
      --card-text: #182438;
      --card-text-gradient: linear-gradient(180deg, #22324a 0%, rgba(34, 50, 74, 0.85) 100%);
      --card-text-shadow: none;
      --card-subtle-shadow: none;

      &::after {
        content: '';
        position: absolute;
        inset: 0;
        background:
          radial-gradient(circle at 18% 18%, rgba(255, 255, 255, 0.5) 0%, transparent 30%),
          radial-gradient(circle at 80% 78%, rgba(255, 255, 255, 0.35) 0%, transparent 38%);
        pointer-events: none;
      }
    }

    [data-theme='cloudy'] {
      --card-glass-bg: rgba(255, 255, 255, 0.1);
      --card-glass-bg-hover: rgba(255, 255, 255, 0.16);
      --card-glass-border: rgba(255, 255, 255, 0.18);
      --card-glass-border-strong: rgba(255, 255, 255, 0.26);
    }

    [data-theme='thunder'] {
      --card-glass-bg: rgba(255, 255, 255, 0.08);
      --card-glass-bg-hover: rgba(255, 255, 255, 0.14);
      --card-glass-border: rgba(255, 255, 255, 0.16);
      --card-glass-border-strong: rgba(255, 255, 255, 0.24);

      &::after {
        content: '';
        position: absolute;
        inset: 0;
        background: radial-gradient(circle at 50% 8%, rgba(212, 180, 255, 0.45) 0%, transparent 50%);
        animation: weather-thunder-flash 8s ease-in-out infinite;
        pointer-events: none;
      }
    }
  }
}

// ===== 通用区域头部 =====
.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: @nordic-space-md;

  .section-title {
    .font-display();
    font-size: 24px;
    font-weight: 700;
    color: @savour-text;
    margin: 0;
    letter-spacing: -0.3px;
  }

  .section-subtitle {
    font-size: @nordic-text-sm;
    color: @savour-text-sec;
    margin: 4px 0 0;
  }

  .view-all-link {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    color: @savour-accent;
    font-size: @nordic-text-sm;
    font-weight: 600;
    background: none;
    border: none;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: @savour-pill;
    transition: all 0.2s ease;

    &:hover { background: @savour-accent-light; }

    .el-icon { transition: transform 0.2s ease; }

    &:hover .el-icon { transform: translateX(3px); }
  }
}

// ===== 推荐区域 =====
.recs-section {
  margin-bottom: @nordic-space-xl;
}

.rec-grid,
.tut-grid,
.recs-scroll,
.tuts-scroll {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: @nordic-space-lg;
}

.rec-card {
  width: 100%;
  border-radius: @savour-radius;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid @savour-border;
  background: @savour-surface;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba(198, 123, 92, 0.35);
    box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);

    .rec-card-image img { transform: scale(1.04); }
    .rec-action-btn { opacity: 1; }
  }

  .rec-card-image {
    position: relative;
    width: 100%;
    aspect-ratio: 4 / 3;
    overflow: hidden;
    background: @savour-bg;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.4s ease;
    }

    .rec-category-badge {
      position: absolute;
      top: 12px;
      left: 12px;
      z-index: 2;
      padding: 4px 8px;
      background: rgba(0, 0, 0, 0.45);
      color: #fff;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 600;
      backdrop-filter: blur(4px);
    }

    .rec-card-actions {
      position: absolute;
      top: 12px;
      right: 12px;
      z-index: 2;
      display: flex;
      gap: 8px;
    }

    .rec-action-btn {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      border: none;
      background: rgba(255, 255, 255, 0.9);
      color: @savour-text;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
      transition: all 0.2s ease;

      &:hover {
        background: #fff;
        color: @savour-accent;
        transform: scale(1.06);
      }

      &.is-fav {
        opacity: 1;
        color: @savour-gold;
      }
    }
  }

  .rec-card-body {
    padding: 16px;

    .rec-dish-name {
      font-size: 17px;
      font-weight: 700;
      color: @savour-text;
      margin: 0 0 10px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .rec-dish-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 10px;
      min-height: 24px;

      .rec-kcal {
        padding: 3px 8px;
        background: @savour-accent-light;
        border-radius: 6px;
        color: @savour-accent-hover;
        font-size: 12px;
        font-weight: 600;
        white-space: nowrap;
      }

      .rec-tags {
        color: @savour-text-sec;
        font-size: 12px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .rec-rating {
      display: flex;
      align-items: center;
      min-height: 18px;

      :deep(.el-rate) { height: 18px; }
      :deep(.el-rate__icon) {
        color: @savour-gold;
        margin-right: 2px;
      }
      :deep(.el-rate__text) {
        color: @savour-text-sec !important;
        font-size: 12px;
      }
    }

    .no-rating {
      color: @savour-text-muted;
      font-size: 12px;
    }
  }
}

// 骨架屏
.rec-skeleton,
.tut-skeleton {
  background: @savour-surface;
  border-radius: @savour-radius;
  overflow: hidden;
  border: 1px solid @savour-border;

  :deep(.el-skeleton__item) {
    background: linear-gradient(90deg, rgba(0, 0, 0, 0.04) 25%, rgba(0, 0, 0, 0.08) 50%, rgba(0, 0, 0, 0.04) 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s ease infinite;
  }
}

// 空状态
.recs-empty,
.tuts-empty {
  text-align: center;
  padding: 48px 20px;
  background: @savour-surface;
  border-radius: @savour-radius;
  border: 1px dashed @savour-border;

  .empty-icon-wrapper {
    color: @savour-text-muted;
    margin-bottom: @nordic-space-md;
  }

  .empty-text {
    color: @savour-text-sec;
    font-size: @nordic-text-base;
    margin: 0 0 @nordic-space-md;
  }

  .retry-btn {
    padding: 8px 24px;
    background: @savour-accent;
    color: #fff;
    border: none;
    border-radius: @savour-pill;
    font-size: @nordic-text-sm;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: @savour-accent-hover;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(198, 123, 92, 0.3);
    }
  }
}

// ===== 今日热点 =====
.hot-section {
  margin-bottom: @nordic-space-lg;
  cursor: pointer;

  .hot-card {
    display: flex;
    align-items: center;
    gap: @nordic-space-md;
    padding: @nordic-space-md @nordic-space-lg;
    background: @savour-surface;
    border-radius: @savour-radius;
    border: 1px solid @savour-border;
    border-left: 4px solid #E25B45;
    transition: all 0.25s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    }

    &.clickable {
      cursor: pointer;

      &:hover {
        box-shadow: 0 8px 24px rgba(226, 91, 69, 0.12);
        .hot-arrow { transform: translateX(4px); }
      }
    }

    .hot-left {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-shrink: 0;

      .hot-emoji {
        display: inline-flex;
        align-items: center;
        color: #E25B45;
        font-size: 24px;
      }

      .hot-badge {
        padding: 4px 10px;
        background: linear-gradient(135deg, #E25B45, #FF7B5C);
        color: #fff;
        border-radius: 12px;
        font-size: 11px;
        font-weight: 800;
        letter-spacing: 1px;
      }
    }

    .hot-center {
      flex: 1;
      min-width: 0;

      .hot-label {
        font-size: 12px;
        color: @savour-text-muted;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .hot-content {
        font-size: 15px;
        font-weight: 600;
        color: @savour-text;
        margin: 4px 0 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .hot-arrow {
      color: #E25B45;
      font-size: 18px;
      transition: transform 0.2s ease;
      flex-shrink: 0;
    }
  }
}

// ===== 教程区域 =====
.tutorials-section {
  margin-bottom: @nordic-space-xl;
}

.tut-card {
  width: 100%;
  background: @savour-surface;
  border-radius: @savour-radius;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid @savour-border;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    transform: translateY(-4px);
    border-color: rgba(198, 123, 92, 0.35);
    box-shadow: 0 10px 24px rgba(45, 42, 38, 0.08);

    .tut-image img { transform: scale(1.04); }
  }

  .tut-image {
    width: 100%;
    aspect-ratio: 4 / 3;
    overflow: hidden;
    position: relative;
    background: @savour-bg;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.4s ease;
    }

    .tut-type-badge {
      position: absolute;
      top: 12px;
      left: 12px;
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      background: rgba(0, 0, 0, 0.45);
      backdrop-filter: blur(4px);
      border-radius: 6px;
      color: #fff;
      font-size: 12px;
      font-weight: 600;
    }
  }

  .tut-info {
    padding: 16px;

    .tut-badges {
      display: flex;
      gap: 4px;
      margin-bottom: 8px;
      flex-wrap: wrap;

      .el-tag {
        font-size: 11px;
        padding: 2px 6px;
        border-radius: 8px;
        height: 18px;
        line-height: 1;

        .el-icon { font-size: 10px; }
      }
    }

    .tut-title {
      font-size: 15px;
      font-weight: 600;
      color: @savour-text;
      margin: 0 0 8px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .tut-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12px;

      .tut-duration { color: @savour-text-sec; }
      :deep(.el-rate__text) { font-size: 11px; }
    }
  }
}

// ===== 弹窗样式 =====
.savour-dialog {
  :deep(.el-dialog) {
    border-radius: @savour-radius-lg;
    overflow: hidden;
  }

  :deep(.el-dialog__header) {
    background: @savour-accent-light;
    padding: 16px 20px;
    margin: 0;

    .el-dialog__title {
      .font-display();
      font-weight: 700;
      color: @savour-text;
    }
  }

  :deep(.el-dialog__body) {
    padding: 20px;
  }
}

.weather-detail {
  .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 16px;
    background: @savour-bg;
    border-radius: 12px;
    margin-bottom: 8px;
    transition: background 0.2s ease;

    &:hover { background: darken(@savour-bg, 3%); }

    .detail-label {
      font-size: 14px;
      color: @savour-text-sec;
      font-weight: 500;
    }

    .detail-value {
      font-size: 14px;
      color: @savour-text;
      font-weight: 700;
    }

    .weather-detail-condition {
      display: inline-flex;
      align-items: center;
      gap: 6px;
    }
  }

  .detail-advice-section {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid @savour-border;
    display: flex;
    flex-direction: column;
    gap: 10px;

    .advice-card {
      padding: 12px 16px;
      background: @savour-accent-light;
      border-radius: 12px;
      border-left: 3px solid @savour-accent;

      .advice-label {
        font-size: 12px;
        color: @savour-accent;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .advice-text {
        font-size: 14px;
        color: @savour-text;
        margin: 4px 0 0;
        line-height: 1.5;
      }
    }
  }
}

// ===== 底部间距 =====
.bottom-spacer {
  height: 32px;
}

// ===== 响应式 =====
@media (max-width: 900px) {
  .hero-section .hero-grid {
    grid-template-columns: 1fr;
    gap: @nordic-space-md;
  }

  .hero-greeting {
    min-height: auto;
  }

  .hero-greeting .greeting-title {
    font-size: 28px;
  }

  .weather-card {
    min-height: 220px;
  }
}

@media (max-width: 600px) {
  .home-scroll-container {
    padding: 16px 16px 32px;
  }

  .hero-greeting .greeting-title {
    font-size: 26px;
  }

  .hero-actions .action-pill {
    flex: 1 1 calc(50% - 8px);
    justify-content: center;
  }

  .section-header-row {
    align-items: flex-start;
  }

  .rec-grid,
  .tut-grid,
  .recs-scroll,
  .tuts-scroll {
    grid-template-columns: 1fr;
  }
}
</style>
