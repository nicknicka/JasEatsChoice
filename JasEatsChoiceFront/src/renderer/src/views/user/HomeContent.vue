<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElNotification, ElMessage } from 'element-plus'
import { useWeather } from '../../composables/useWeather.js'
// 导入 Element Plus 图标
import {
  Location,
  VideoCamera,
  ArrowRight,
  Star,
  Share,
  Search,
  Coffee,
  Document,
  Check,
  Shop,
  MagicStick
} from '@element-plus/icons-vue'
import CommonMapLocationPicker from '../../components/CommonMapLocationPicker.vue'
import { useRouter } from 'vue-router'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
// 导入 WebSocket 常量
import { WS_CONFIG } from '../../constants/wsConstants.js'
// 导入 authStore
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore(pinia)

// 使用天气组合式函数
const {
  weather,
  weatherDetailVisible,
  showWeatherSkeleton,
  tempRangeText,
  weatherGradient,
  weatherIcon,
  weatherEmoji,
  aqiInfo,
  clothingAdvice,
  exerciseAdvice,
  fetchWeather: fetchWeatherData,
  showWeatherDetail,
  getRecommendedDishesSeries: getWeatherRecommendation,
  getLocationHistory,
  clearWeatherCache
} = useWeather()

// 默认菜品占位图 - 更精美的设计
const defaultDishImage =
  'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Cdefs%3E%3ClinearGradient id="grad1" x1="0%25" y1="0%25" x2="100%25" y2="100%25"%3E%3Cstop offset="0%25" style="stop-color:%23ff6b6b;stop-opacity:0.1" /%3E%3Cstop offset="100%25" style="stop-color:%23ffa8a8;stop-opacity:0.2" /%3E%3C/linearGradient%3E%3C/defs%3E%3Crect fill="url(%23grad1)" width="400" height="300"/%3E%3Ccircle cx="200" cy="130" r="50" fill="%23ff6b6b" opacity="0.15"/%3E%3Ctext x="200" y="130" font-size="48" text-anchor="middle" fill="%23ff6b6b" opacity="0.3"%3E🍽️%3C/text%3E%3Ctext x="200" y="200" font-family="Arial, sans-serif" font-size="20" font-weight="600" text-anchor="middle" fill="%23999"%3E暂无图片%3C/text%3E%3Ctext x="200" y="230" font-family="Arial, sans-serif" font-size="14" text-anchor="middle" fill="%23bbb"%3E精彩美食即将呈现%3C/text%3E%3C/svg%3E'

// 默认教程缩略图 - 更精美的设计
const defaultTutorialThumbnail =
  'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="200"%3E%3Cdefs%3E%3ClinearGradient id="grad2" x1="0%25" y1="0%25" x2="100%25" y2="100%25"%3E%3Cstop offset="0%25" style="stop-color:%236ba4ff;stop-opacity:0.1" /%3E%3Cstop offset="100%25" style="stop-color:%23a8c8ff;stop-opacity:0.2" /%3E%3C/linearGradient%3E%3C/defs%3E%3Crect fill="url(%23grad2)" width="300" height="200"/%3E%3Ccircle cx="150" cy="85" r="40" fill="%236ba4ff" opacity="0.15"/%3E%3Ctext x="150" y="90" font-size="40" text-anchor="middle" fill="%236ba4ff" opacity="0.3"%3E📖%3C/text%3E%3Ctext x="150" y="150" font-family="Arial, sans-serif" font-size="16" font-weight="600" text-anchor="middle" fill="%23999"%3E暂无缩略图%3C/text%3E%3Ctext x="150" y="175" font-family="Arial, sans-serif" font-size="12" text-anchor="middle" fill="%23bbb"%3E教程内容加载中%3C/text%3E%3C/svg%3E'

// 图片加载错误处理
const handleImageError = (event) => {
  event.target.src = defaultDishImage
}

// 加载状态
const nearbyLoading = ref(false)
const recommendedDishesLoading = ref(true)
const tutorialsLoading = ref(true)

// 教程数据 - 从后端获取
const featuredTutorials = ref([])

// 今日推荐菜品 - 来自后端
const recommendedDishes = ref([])
// 推荐菜品空状态消息
const recommendEmptyMessage = ref('暂无推荐菜品')
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
// 搜索关键字
const searchKeyword = ref('')

// 位置选择弹窗
const mapLocationPickerVisible = ref(false)

// 从后端获取推荐菜品
const fetchRecommendedDishes = async () => {
  recommendedDishesLoading.value = true
  try {
    const response = await retryFetch(async () => {
      return await api.get(API_CONFIG.recipe.recommend)
    })

    // Check if response has a message
    if (response.message) {
      recommendEmptyMessage.value = response.message
    }

    // Handle both null/undefined and empty array cases
    if (response.data && Array.isArray(response.data) && response.data.length > 0) {
      recommendedDishes.value = response.data
      // 预加载图片
      preloadImages(response.data)
    } else {
      // Set to empty array to show empty state
      recommendedDishes.value = []
    }
  } catch (error) {
    console.error('加载推荐菜品失败:', error)
    // Reset to default message on error
    recommendEmptyMessage.value = '加载失败,请重试'
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

// 处理位置选择
const handleLocationSelected = (locationData) => {
  const { address } = locationData

  // 更新天气位置信息
  weather.value.address = address || '已选择位置'
  weather.value.city = extractCityFromAddress(address)

  // 获取详细天气信息
  fetchWeather(weather.value.city)

  ElMessage.success(`已选择位置：${address}`)
}

// 从地址中提取城市名称
const extractCityFromAddress = (address) => {
  if (!address) return '北京'

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

  return '北京'
}

// 从后端获取位置和天气数据
const fetchWeather = async (selectedCity = null) => {
  try {
    await fetchWeatherData(selectedCity, {
      onRetry: () => fetchWeather(selectedCity),
      onManualSelect: () => {
        locationDialogVisible.value = true
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

// 过滤后的推荐菜品(基于搜索关键字)
const filteredDishes = computed(() => {
  if (!searchKeyword.value) {
    return recommendedDishes.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return recommendedDishes.value.filter((dish) => {
    return (
      dish.name?.toLowerCase().includes(keyword) ||
      dish.category?.toLowerCase().includes(keyword) ||
      dish.tags?.toLowerCase().includes(keyword)
    )
  })
})

// 处理搜索
const handleSearch = () => {
  console.log('搜索:', searchKeyword.value)
  // 可以添加搜索分析或跳转到搜索结果页
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
}

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

    // Handle both null/undefined and empty array cases for consistency
    if (response.data && Array.isArray(response.data) && response.data.length > 0) {
      featuredTutorials.value = response.data
    } else {
      featuredTutorials.value = []
    }
  } catch (error) {
    console.error('加载精选教程失败:', error)
    // 失败时使用模拟数据作为备份
    featuredTutorials.value = [
      { name: '青木瓜沙拉制作教程', type: 'video' },
      { name: '夏日低卡饮食指南', type: 'article' }
    ]
    showError('加载教程失败,显示默认内容')
  } finally {
    tutorialsLoading.value = false
  }
}

// 图片预加载功能
const preloadImages = (items) => {
  items.forEach((item) => {
    if (item.image || item.thumbnail) {
      const img = new Image()
      img.src = item.image || item.thumbnail
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
  loadFavorites() // 加载收藏列表
  fetchFeaturedTutorials()
  fetchRecommendedDishes()
  await fetchWeather()
  fetchHotTopic() // 新增：获取今日热点

  if (window.api) {
    initializeWebSocket()
  }
})
</script>

<template>
  <!-- 主内容区域 -->
  <div class="main-content-wrapper">
    <!-- 顶部操作栏 -->
    <div class="top-action-bar">
      <div class="search-section" role="search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索菜品、教程..."
          clearable
          size="large"
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="clearSearch"
          aria-label="搜索菜品和教程"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" aria-label="执行搜索">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 天气信息区域 - 新设计 -->
    <div class="weather-section-new" role="region" aria-label="天气信息">
      <!-- 天气骨架屏 -->
      <div v-if="showWeatherSkeleton" class="weather-skeleton-wrapper">
        <div class="weather-card-new skeleton-weather">
          <el-skeleton animated>
            <template #template>
              <div class="weather-skeleton-vertical">
                <el-skeleton-item variant="text" style="width: 80px; height: 24px" />
                <el-skeleton-item variant="text" style="width: 100px; height: 18px; margin-top: 12px" />
                <el-skeleton-item variant="text" style="width: 120px; height: 16px; margin-top: 12px" />
                <el-skeleton-item variant="text" style="width: 140px; height: 16px; margin-top: 12px" />
                <el-skeleton-item variant="text" style="width: 150px; height: 16px; margin-top: 12px" />
              </div>
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 天气卡片 - 新设计 -->
      <div v-else class="weather-card-new" :style="{ background: weatherGradient }">
        <div class="weather-content-new">
          <!-- 温度显示 -->
          <div class="weather-temp-line">
            <span class="weather-icon-new">{{ weatherEmoji }}</span>
            <span class="temp-value-new">{{ weather.temp }}°C</span>
          </div>

          <!-- 温度范围 -->
          <div v-if="tempRangeText" class="temp-range-new">
            {{ tempRangeText }}
          </div>

          <!-- 位置信息 -->
          <div class="location-line">
            <span class="location-icon-new">📍</span>
            <span class="location-text-new">当前位置</span>
            <button
              class="location-select-btn"
              @click="mapLocationPickerVisible = true"
              :title="weather.address || weather.city || '点击选择位置'"
            >
              点击选择位置 ↗
            </button>
          </div>

          <!-- 天气状况 -->
          <div class="weather-condition-line" @click="showWeatherDetail" style="cursor: pointer">
            <span class="weather-icon-new">{{ weatherEmoji }}</span>
            <span class="weather-condition-text">{{ weather.condition || '未知天气' }}</span>
          </div>

          <!-- 今日推荐 -->
          <div class="recommendation-line">
            <span class="sparkle-icon-new">✨</span>
            <span class="recommendation-label-new">今日推荐</span>
            <div class="recommendation-text-new" :title="getWeatherRecommendation()">
              {{ getWeatherRecommendation() }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 天气详情弹窗 -->
    <el-dialog
      v-model="weatherDetailVisible"
      title="天气详情"
      width="500px"
      class="weather-detail-dialog"
    >
      <div class="weather-detail-content">
        <div class="detail-item">
          <span class="detail-label">当前温度</span>
          <span class="detail-value">{{ weather.temp }}°C</span>
        </div>
        <div v-if="tempRangeText" class="detail-item">
          <span class="detail-label">温度范围</span>
          <span class="detail-value">{{ tempRangeText }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">天气状况</span>
          <span class="detail-value">
            {{ weatherEmoji }} {{ weather.condition || '未知天气' }}
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">湿度</span>
          <span class="detail-value">{{ weather.humidity }}%</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">风速</span>
          <span class="detail-value">{{ weather.windSpeed }} m/s</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">空气质量</span>
          <span class="detail-value" :style="{ color: aqiInfo.color }">
            {{ aqiInfo.text }} (AQI: {{ weather.aqi }})
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">位置</span>
          <span class="detail-value">{{ weather.city }} {{ weather.address }}</span>
        </div>
        <div class="detail-advice">
          <div class="advice-item">
            <span class="advice-label">穿衣建议</span>
            <span class="advice-text">{{ clothingAdvice }}</span>
          </div>
          <div class="advice-item">
            <span class="advice-label">运动建议</span>
            <span class="advice-text">{{ exerciseAdvice }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <div class="recommendation-section" role="region" aria-label="今日推荐菜品">
      <h3 id="recommendations-heading">今日推荐</h3>
      <!-- 骨架屏加载中 -->
      <div v-if="recommendedDishesLoading" class="skeleton-wrapper">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item
              variant="image"
              style="width: 100%; height: 320px; border-radius: 8px"
            />
          </template>
        </el-skeleton>
      </div>
      <!-- When there are no recommended dishes -->
      <div v-else-if="recommendedDishes.length === 0" class="empty-recommendations">
        <el-empty :description="recommendEmptyMessage">
          <template #image>
            <div class="empty-icon">
              <el-icon :size="80"><Coffee /></el-icon>
            </div>
          </template>
          <el-button type="primary" @click="fetchRecommendedDishes">重新加载</el-button>
        </el-empty>
      </div>

      <!-- When there are recommended dishes -->
      <div v-else class="fade-in">
        <el-carousel
          :interval="3000"
          height="320px"
          indicator-position="outside"
          arrow="never"
          class="recommendation-carousel"
          role="region"
          :aria-label="'推荐菜品轮播,共' + filteredDishes.length + '个'"
        >
          <el-carousel-item v-for="(dish, index) in filteredDishes" :key="index">
            <el-card
              shadow="hover"
              class="dish-card enhanced-card"
              @click="handleDishClick(dish)"
              :aria-label="`菜品: ${dish.name}, ${dish.kcal} 卡路里, 评分: ${dish.rating}分`"
              role="article"
              tabindex="0"
              @keyup.enter="handleDishClick(dish)"
            >
              <!-- 菜品图片区域 - 作为背景层 -->
              <div class="dish-image-background">
                <img
                  :src="dish.image || defaultDishImage"
                  :alt="dish.name"
                  loading="lazy"
                  @error="handleImageError"
                />
                <!-- 分类标签 -->
                <span class="dish-category">{{ dish.category || '推荐' }}</span>
              </div>
              <!-- 菜品信息区域 - 覆盖在图片上方 -->
              <div class="dish-info-overlay">
                <div class="dish-header">
                  <div class="dish-name">{{ dish.name }}</div>
                  <div class="dish-actions">
                    <el-button
                      circle
                      size="small"
                      class="share-btn"
                      @click="shareDish(dish, $event)"
                      :aria-label="`分享 ${dish.name}`"
                      tabindex="0"
                      @keyup.enter="shareDish(dish, $event)"
                    >
                      <el-icon><Share /></el-icon>
                    </el-button>
                    <el-button
                      circle
                      size="small"
                      class="favorite-btn"
                      @click="toggleFavorite(dish, $event)"
                      :class="{ 'is-favorite': isFavorite(dish) }"
                      :aria-label="`${isFavorite(dish) ? '取消收藏' : '收藏'} ${dish.name}`"
                      tabindex="0"
                      @keyup.enter="toggleFavorite(dish, $event)"
                    >
                      <el-icon><Star /></el-icon>
                    </el-button>
                  </div>
                </div>
                <div class="dish-meta">
                  <span class="dish-kcal">{{ dish.kcal }} kcal</span>
                  <span v-if="dish.tags" class="dish-tags">{{ dish.tags }}</span>
                </div>
                <div class="dish-rating">
                  <el-rate
                    v-if="dish.rating && dish.rating > 0"
                    v-model="dish.rating"
                    disabled
                    show-score
                    text-color="#FF6B6B"
                    class="rating"
                  ></el-rate>
                  <div v-else class="no-rating">
                    <el-icon><Star /></el-icon>
                    <span>暂无评分</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <!-- 今日热点 - 只有当有数据时显示 -->
    <div class="hot-section" v-if="hotTopic.content" @click="handleHotTopicClick">
      <el-card shadow="hover" class="hot-card" :class="{ 'is-clickable': hotTopic.clickable }">
        <div class="hot-content">
          <div class="hot-icon-wrapper">
            <span class="fire-icon">🔥</span>
            <span class="hot-badge">HOT</span>
          </div>
          <div class="hot-text">
            <span class="hot-label">今日热点</span>
            <span class="hot-description">{{ hotTopic.content }}</span>
          </div>
          <el-icon class="hot-arrow" v-if="hotTopic.clickable"><ArrowRight /></el-icon>
        </div>
      </el-card>
    </div>

    <div class="nearby-section">
      <el-button
        type="primary"
        size="large"
        class="nearby-btn"
        @click="handleNearbySearch"
        :loading="nearbyLoading"
        :loading-icon="Location"
      >
        <el-icon v-if="!nearbyLoading"><Location /></el-icon>
        {{ nearbyLoading ? '定位中...' : '查找附近商家' }}
      </el-button>
    </div>

    <div class="tutorial-section" role="region" aria-label="制作教程与指南">
      <div class="section-header">
        <h3 id="tutorials-heading">制作教程与指南</h3>
        <el-button
          text
          type="primary"
          @click="router.push('/user/home/tutorials?fromSidebar=true')"
          class="view-all-btn"
          aria-label="查看所有教程"
        >
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <!-- 教程骨架屏 -->
      <div v-if="tutorialsLoading" class="tutorial-skeleton">
        <div class="tutorial-grid">
          <el-skeleton v-for="i in 4" :key="i" animated>
            <template #template>
              <el-skeleton-item
                variant="image"
                style="width: 100%; height: 120px; border-radius: 4px"
              />
              <el-skeleton-item variant="h3" style="width: 80%; margin: 12px 0 8px" />
              <el-skeleton-item variant="text" style="width: 60%" />
            </template>
          </el-skeleton>
        </div>
      </div>

      <!-- 当教程数据为空时显示 -->
      <div v-else-if="featuredTutorials.length === 0" class="empty-tutorials">
        <el-empty description="暂无教程数据">
          <template #image>
            <div class="empty-icon">
              <el-icon :size="80"><Document /></el-icon>
            </div>
          </template>
          <el-button type="primary" @click="fetchFeaturedTutorials">重新加载</el-button>
        </el-empty>
      </div>

      <!-- 当教程数据不为空时显示 -->
      <div v-else class="fade-in">
        <div class="tutorial-grid" role="list" aria-label="教程列表">
          <el-card
            shadow="hover"
            class="tutorial-card enhanced"
            v-for="(tutorial, index) in featuredTutorials.slice(0, 4)"
            :key="index"
            @click="handleTutorialClick(tutorial)"
            :aria-label="`教程: ${tutorial.name || tutorial.title}, ${tutorial.duration || '5分钟'}`"
            role="listitem"
            tabindex="0"
            @keyup.enter="handleTutorialClick(tutorial)"
          >
            <div class="tutorial-thumbnail">
              <img
                :src="tutorial.thumbnail || tutorial.coverImage || defaultTutorialThumbnail"
                :alt="tutorial.name || tutorial.title"
                loading="lazy"
              />
              <div class="tutorial-type-badge">
                <el-icon v-if="tutorial.type === 'video'"><VideoCamera /></el-icon>
                <span v-else>💡</span>
              </div>
            </div>
            <div class="tutorial-content">
              <!-- 来源标签 -->
              <div class="tutorial-source-badges">
                <!-- 官方认证标签 -->
                <el-tag v-if="tutorial.source_type === 'ADMIN' && tutorial.is_official"
                        type="danger"
                        size="small"
                        effect="dark">
                  <el-icon><Check /></el-icon> 官方认证
                </el-tag>

                <!-- 商家标签 -->
                <el-tag v-if="tutorial.source_type === 'MERCHANT'"
                        type="warning"
                        size="small"
                        effect="plain">
                  <el-icon><Shop /></el-icon> {{ tutorial.merchantName || '商家' }}
                </el-tag>

                <!-- AI生成标签 -->
                <el-tag v-if="tutorial.source_type === 'AI_GENERATED'"
                        :type="tutorial.review_status === 'APPROVED' ? 'success' : 'info'"
                        size="small"
                        effect="plain">
                  <el-icon><MagicStick /></el-icon>
                  AI生成
                  <span v-if="tutorial.review_status === 'APPROVED'" class="reviewed-badge">
                    ✓ 人工审核
                  </span>
                </el-tag>
              </div>

              <h4 class="tutorial-title">{{ tutorial.name || tutorial.title }}</h4>
              <div class="tutorial-meta">
                <span class="tutorial-duration">{{ tutorial.duration || '5分钟' }}</span>
                <el-rate
                  v-if="tutorial.rating"
                  v-model="tutorial.rating"
                  disabled
                  size="small"
                  show-score
                />
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 地图位置选择弹窗 -->
    <CommonMapLocationPicker
      v-model:visible="mapLocationPickerVisible"
      @location-selected="handleLocationSelected"
    />
  </div>
</template>

<style scoped lang="less">
// 主内容包裹层
.main-content-wrapper {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  background-color: #fafafa;
}

// 顶部操作栏
.top-action-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 0 0 16px 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  position: sticky;
  top: 0;
  z-index: 100;
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.95);

  // 确保所有元素使用相同的盒模型
  * {
    box-sizing: border-box;
  }

  // 确保所有直接子元素在垂直方向上对齐
  > * {
    align-self: center;
  }

  .search-section {
    flex: 1;
    margin: 0;
    display: flex;
    align-items: center;
    height: 48px;

    .search-input {
      // 确保输入框容器没有额外间距
      display: flex;
      width: 100%;
      height: 100%;
      align-items: center;

      :deep(.el-input) {
        height: 100%;
        display: flex;
        align-items: center;
      }

      :deep(.el-input__wrapper) {
        // 左侧圆角，右侧直角以便与按钮完美衔接
        border-radius: 24px 0 0 24px;
        border-right: none;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        padding-right: 0;
        padding-top: 0;
        padding-bottom: 0;
        background: rgba(255, 255, 255, 0.95);
        height: 100%;
        display: flex;
        align-items: center;

        // 重置内部输入框的样式
        .el-input__inner {
          height: 100% !important;
          line-height: 48px !important;
          display: flex;
          align-items: center;
        }

        &:hover {
          box-shadow: 0 4px 16px rgba(255, 107, 107, 0.15);
          background: rgba(255, 255, 255, 1);
        }

        &.is-focus {
          box-shadow: 0 4px 24px rgba(255, 107, 107, 0.3);
          border-right: none;
          background: rgba(255, 255, 255, 1);
        }
      }

      :deep(.el-input-group__append) {
        // 左侧直角，右侧圆角
        border-radius: 0 24px 24px 0;
        background: linear-gradient(135deg, #ff6b6b 0%, #ff5252 100%);
        border: none;
        border-left: none;
        padding: 0;
        padding-top: 0;
        padding-bottom: 0;
        margin: 0;
        margin-left: -1px; // 负边距确保无缝衔接
        box-shadow: 0 2px 12px rgba(255, 107, 107, 0.3);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        z-index: 1; // 确保按钮覆盖在输入框边框上
        overflow: hidden;
        height: 100%;
        display: flex;
        align-items: center;

        // 添加波纹效果
        &::before {
          content: '';
          position: absolute;
          top: 50%;
          left: 50%;
          width: 0;
          height: 0;
          border-radius: 50%;
          background: rgba(255, 255, 255, 0.3);
          transform: translate(-50%, -50%);
          transition:
            width 0.6s,
            height 0.6s;
        }

        &:hover::before {
          width: 300px;
          height: 300px;
        }

        .el-button {
          background-color: transparent;
          border: none;
          color: #fff;
          font-weight: 600;
          padding: 0 24px;
          height: 100%;
          border-radius: 0 24px 24px 0;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: none;
          margin: 0;
          position: relative;
          z-index: 1;
          display: inline-flex;
          align-items: center;
          justify-content: center;

          &:hover {
            background-color: rgba(255, 255, 255, 0.15);
            transform: scale(1.02);
            box-shadow: none;
          }

          &:active {
            transform: scale(0.98);
          }
        }
      }

      // 修复输入框组整体的边框问题
      :deep(.el-input-group__append),
      :deep(.el-input-group__prepend) {
        box-shadow: none;
      }
    }
  }
}

// 刷新旋转动画
@keyframes refresh-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes loading-spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-nav-bar {
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b6b;
}

.search-input {
  width: 400px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  background-color: #f0f2f5;
  border-right: 1px solid #e6e8eb;
  padding: 20px 0;
  display: flex;
  flex-direction: column;

  .avatar-section {
    text-align: center;
    padding-bottom: 20px;
    border-bottom: 1px solid #e6e8eb;
    margin-bottom: 20px;
  }

  .menu-list {
    border: none;
    flex: 1;
  }

  .setting-menu {
    border-top: 1px solid #e6e8eb;
    margin-top: auto;
    width: 100%;
  }
}

.content-area {
  padding: 20px;
  background-color: #fafafa;
  overflow-y: auto;

  .search-section {
    margin-bottom: 16px;

    .search-input {
      // 确保输入框容器没有额外间距
      display: inline-flex;
      width: 100%;

      :deep(.el-input__wrapper) {
        // 左侧圆角，右侧直角以便与按钮完美衔接
        border-radius: 24px 0 0 24px;
        border-right: none;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
        transition: all 0.3s ease;

        &:hover {
          box-shadow: 0 4px 16px rgba(255, 107, 107, 0.15);
        }

        &.is-focus {
          box-shadow: 0 4px 16px rgba(255, 107, 107, 0.25);
        }
      }

      :deep(.el-input-group__append) {
        // 左侧直角，右侧圆角
        border-radius: 0 24px 24px 0;
        background-color: #ff6b6b;
        border-color: #ff6b6b;
        border-left: none;
        color: #fff;
        padding: 0;
        margin: 0;
        margin-left: -1px; // 负边距确保无缝衔接
        position: relative;
        z-index: 1; // 确保按钮覆盖在输入框边框上

        .el-button {
          background-color: transparent;
          border: none;
          color: #fff;
          border-radius: 0 24px 24px 0;
          margin: 0;

          &:hover {
            background-color: rgba(255, 255, 255, 0.1);
          }
        }
      }
    }
  }

  .weather-section {
    margin-bottom: 20px;

    .weather-skeleton-wrapper {
      .weather-card {
        background: linear-gradient(
          135deg,
          rgba(255, 255, 255, 0.9) 0%,
          rgba(255, 255, 255, 0.7) 100%
        );
        border: 1px solid rgba(0, 0, 0, 0.06);

        .weather-skeleton-content {
          display: flex;
          align-items: center;
        }
      }
    }

    .weather-card {
      border: none;
      overflow: visible;
      position: relative;
      border-radius: 16px;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
      }

      &::before {
        content: '';
        position: absolute;
        top: -50%;
        right: -10%;
        width: 200px;
        height: 200px;
        background: radial-gradient(circle, rgba(255, 255, 255, 0.2) 0%, transparent 70%);
        border-radius: 50%;
        pointer-events: none;
      }

      &::after {
        content: '';
        position: absolute;
        bottom: -30%;
        left: -5%;
        width: 150px;
        height: 150px;
        background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
        border-radius: 50%;
        pointer-events: none;
      }

      :deep(.el-card__body) {
        padding: 24px 28px;
        position: relative;
        z-index: 1;
      }
    }

    &.enhanced-weather {
      .weather-content {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 24px;
        color: #fff;

        .weather-visual {
          display: flex;
          align-items: center;
          gap: 16px;
          flex-shrink: 0;

          .weather-icon-wrapper {
            width: 80px;
            height: 80px;
            background: linear-gradient(
              135deg,
              rgba(255, 255, 255, 0.3),
              rgba(255, 255, 255, 0.15)
            );
            backdrop-filter: blur(12px);
            border-radius: 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow:
              0 8px 20px rgba(0, 0, 0, 0.18),
              inset 0 1px 1px rgba(255, 255, 255, 0.3);
            transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
            border: 1px solid rgba(255, 255, 255, 0.25);
            cursor: pointer;

            &:hover {
              transform: scale(1.06) rotate(6deg);
              background: linear-gradient(
                135deg,
                rgba(255, 255, 255, 0.35),
                rgba(255, 255, 255, 0.2)
              );
              box-shadow:
                0 12px 28px rgba(0, 0, 0, 0.22),
                inset 0 1px 1px rgba(255, 255, 255, 0.4);
            }

            .weather-emoji {
              font-size: 40px;
              filter: drop-shadow(0 3px 8px rgba(0, 0, 0, 0.25));
              animation: emoji-bounce 2s ease-in-out infinite;
            }

            .weather-icon {
              font-size: 48px;
              color: #fff;
              filter: drop-shadow(0 3px 8px rgba(0, 0, 0, 0.25));
            }
          }

          .temp-display {
            display: flex;
            align-items: baseline;
            gap: 4px;
            padding: 4px 0;

            .temp-value {
              font-size: 56px;
              font-weight: 800;
              line-height: 1;
              background: linear-gradient(180deg, #ffffff 0%, rgba(255, 255, 255, 0.85) 100%);
              -webkit-background-clip: text;
              -webkit-text-fill-color: transparent;
              background-clip: text;
              filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.25));
              letter-spacing: -2px;
              font-family:
                -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
                sans-serif;
            }

            .temp-unit {
              font-size: 24px;
              font-weight: 600;
              opacity: 0.9;
              text-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
              letter-spacing: -0.3px;
            }
          }

          .temp-range {
            font-size: 14px;
            font-weight: 600;
            opacity: 0.85;
            text-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
            letter-spacing: 0.5px;
            margin-top: 4px;
          }
        }

        .weather-info {
          flex: 1;
          display: flex;
          flex-direction: column;
          justify-content: center;
          gap: 14px;
          min-width: 0;

          .location-section {
            .location-label {
              display: flex;
              align-items: center;
              gap: 6px;
              margin-bottom: 8px;
              font-size: 11px;
              font-weight: 600;
              letter-spacing: 1px;
              text-transform: uppercase;
              opacity: 0.85;

              .location-icon {
                font-size: 14px;
                animation: location-pulse 2s ease-in-out infinite;
              }

              .label-text {
                text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
              }
            }

            .location-button {
              display: inline-flex;
              align-items: center;
              gap: 8px;
              color: #fff;
              padding: 10px 18px;
              background: linear-gradient(
                135deg,
                rgba(255, 255, 255, 0.25),
                rgba(255, 255, 255, 0.15)
              );
              backdrop-filter: blur(12px);
              border-radius: 20px;
              transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
              border: 1px solid rgba(255, 255, 255, 0.2);
              font-size: 14px;
              box-shadow: 0 3px 12px rgba(0, 0, 0, 0.1);
              min-width: 0;
              max-width: 100%;

              &:hover {
                background: linear-gradient(
                  135deg,
                  rgba(255, 255, 255, 0.32),
                  rgba(255, 255, 255, 0.2)
                );
                transform: translateY(-2px);
                box-shadow: 0 6px 18px rgba(0, 0, 0, 0.15);

                .edit-icon {
                  transform: rotate(90deg) scale(1.1);
                }
              }

              &:active {
                transform: translateY(0);
              }

              .location-text {
                font-size: 14px;
                font-weight: 600;
                flex: 1;
                min-width: 0;
                max-width: 280px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                letter-spacing: 0.3px;
                text-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
                line-height: 1.3;
              }

              .edit-icon {
                font-size: 16px;
                opacity: 0.85;
                transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                flex-shrink: 0;
              }
            }
          }

          .weather-details {
            display: flex;
            flex-direction: column;
            gap: 10px;

            .condition-badge {
              display: inline-flex;
              align-items: center;
              gap: 8px;
              padding: 8px 16px;
              background: linear-gradient(
                135deg,
                rgba(255, 255, 255, 0.22),
                rgba(255, 255, 255, 0.12)
              );
              backdrop-filter: blur(10px);
              border-radius: 18px;
              border: 1px solid rgba(255, 255, 255, 0.18);
              box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
              align-self: flex-start;
              transition: all 0.25s ease;

              &:hover {
                transform: translateX(3px);
                background: linear-gradient(
                  135deg,
                  rgba(255, 255, 255, 0.28),
                  rgba(255, 255, 255, 0.16)
                );
              }

              .condition-icon {
                font-size: 20px;
                filter: drop-shadow(0 1px 4px rgba(0, 0, 0, 0.15));
                line-height: 1;
              }

              .condition-text {
                font-size: 14px;
                font-weight: 600;
                letter-spacing: 0.5px;
                text-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
                line-height: 1.3;
              }
            }

            .recommendation-card {
              background: linear-gradient(
                135deg,
                rgba(255, 255, 255, 0.28),
                rgba(255, 255, 255, 0.16)
              );
              backdrop-filter: blur(12px);
              border-radius: 16px;
              padding: 12px 18px;
              border: 1px solid rgba(255, 255, 255, 0.22);
              box-shadow:
                0 4px 14px rgba(0, 0, 0, 0.12),
                inset 0 1px 1px rgba(255, 255, 255, 0.25);
              transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
              max-width: 100%;
              overflow: hidden;

              &:hover {
                transform: translateY(-1px);
                background: linear-gradient(
                  135deg,
                  rgba(255, 255, 255, 0.35),
                  rgba(255, 255, 255, 0.22)
                );
                box-shadow:
                  0 6px 18px rgba(0, 0, 0, 0.16),
                  inset 0 1px 1px rgba(255, 255, 255, 0.3);
              }

              .recommendation-header {
                display: flex;
                align-items: center;
                gap: 6px;
                margin-bottom: 6px;
                font-size: 10px;
                font-weight: 600;
                letter-spacing: 1.2px;
                text-transform: uppercase;
                opacity: 0.9;

                .sparkle-icon {
                  font-size: 14px;
                  animation: sparkle 2s ease-in-out infinite;
                  filter: drop-shadow(0 0 4px rgba(255, 255, 255, 0.5));
                  line-height: 1;
                }

                .recommendation-label {
                  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
                }
              }

              .recommendation-content {
                font-size: 15px;
                font-weight: 700;
                line-height: 1.4;
                letter-spacing: 0.4px;
                text-shadow: 0 2px 6px rgba(0, 0, 0, 0.18);
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }
          }
        }
      }
    }

    // 位置脉冲动画
    @keyframes location-pulse {
      0%,
      100% {
        opacity: 1;
        transform: scale(1);
      }
      50% {
        opacity: 0.7;
        transform: scale(1.1);
      }
    }
  }

  // 星光动画
  @keyframes sparkle {
    0%,
    100% {
      opacity: 1;
      transform: scale(1);
    }

    50% {
      opacity: 0.6;
      transform: scale(1.1);
    }
  }

  // 新的天气部分样式
  .weather-section-new {
    margin-bottom: 20px;
    padding: 0 20px;

    .weather-card-new {
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      overflow: hidden;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
        pointer-events: none;
      }

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
      }

      .weather-content-new {
        position: relative;
        z-index: 1;
        padding: 20px 24px;
        display: flex;
        flex-direction: column;
        gap: 12px;
        color: #fff;

        // 温度行
        .weather-temp-line {
          display: flex;
          align-items: center;
          gap: 12px;

          .weather-icon-new {
            font-size: 32px;
            filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.2));
            animation: icon-float 3s ease-in-out infinite;
          }

          .temp-value-new {
            font-size: 48px;
            font-weight: 700;
            line-height: 1;
            text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
            letter-spacing: -1px;
            background: linear-gradient(180deg, #ffffff 0%, rgba(255, 255, 255, 0.9) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
          }
        }

        // 温度范围
        .temp-range-new {
          font-size: 18px;
          font-weight: 500;
          opacity: 0.95;
          text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
          letter-spacing: 0.5px;
          margin-top: -4px;
        }

        // 位置信息行
        .location-line {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-wrap: wrap;

          .location-icon-new {
            font-size: 18px;
            filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.15));
            animation: location-pulse 2s ease-in-out infinite;
          }

          .location-text-new {
            font-size: 16px;
            font-weight: 500;
            opacity: 0.95;
            text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
          }

          .location-select-btn {
            background: rgba(255, 255, 255, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.3);
            color: #64b5f6;
            padding: 6px 14px;
            border-radius: 16px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
            backdrop-filter: blur(8px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            text-shadow: none;

            &:hover {
              background: rgba(255, 255, 255, 0.3);
              transform: translateY(-1px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            }

            &:active {
              transform: translateY(0);
            }
          }
        }

        // 天气状况行
        .weather-condition-line {
          display: flex;
          align-items: center;
          gap: 10px;
          transition: all 0.25s ease;

          &:hover {
            transform: translateX(4px);
          }

          .weather-icon-new {
            font-size: 22px;
            filter: drop-shadow(0 1px 4px rgba(0, 0, 0, 0.15));
          }

          .weather-condition-text {
            font-size: 16px;
            font-weight: 600;
            text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
          }
        }

        // 推荐行
        .recommendation-line {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 12px 16px;
          background: rgba(255, 255, 255, 0.15);
          backdrop-filter: blur(10px);
          border-radius: 12px;
          border: 1px solid rgba(255, 255, 255, 0.2);
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
          transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

          &:hover {
            background: rgba(255, 255, 255, 0.22);
            transform: translateY(-1px);
            box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
          }

          .sparkle-icon-new {
            font-size: 20px;
            animation: sparkle 2s ease-in-out infinite;
            filter: drop-shadow(0 0 4px rgba(255, 255, 255, 0.4));
          }

          .recommendation-label-new {
            font-size: 14px;
            font-weight: 700;
            letter-spacing: 1px;
            text-transform: uppercase;
            opacity: 0.9;
            text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
          }

          .recommendation-text-new {
            font-size: 15px;
            font-weight: 600;
            text-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }

    // 图标浮动动画
    @keyframes icon-float {
      0%,
      100% {
        transform: translateY(0);
      }
      50% {
        transform: translateY(-6px);
      }
    }

    // 骨架屏样式
    .skeleton-weather {
      background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 255, 255, 0.7) 100%);
      border: 1px solid rgba(0, 0, 0, 0.06);

      .weather-skeleton-vertical {
        padding: 20px 24px;
        display: flex;
        flex-direction: column;
        gap: 12px;
      }
    }
  }

  // 火焰闪烁动画
  @keyframes flame-flicker {
    0% {
      transform: scale(1) rotate(-2deg);
      opacity: 0.9;
    }

    100% {
      transform: scale(1.1) rotate(2deg);
      opacity: 1;
    }
  }

  // 浮动动画
  @keyframes float {
    0%,
    100% {
      transform: translateY(0px);
    }

    50% {
      transform: translateY(-10px);
    }
  }

  .recommendation-section {
    margin-bottom: 16px;

    h3 {
      margin-bottom: 12px;
      font-size: 20px;
      font-weight: bold;
    }

    .dish-card {
      height: 320px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      position: relative;
      transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      border-radius: 16px;

      &:hover {
        transform: translateY(-8px) scale(1.02);
        box-shadow: 0 16px 40px rgba(255, 107, 107, 0.3);

        .dish-image-background img {
          transform: scale(1.1);
        }

        .share-btn,
        .favorite-btn {
          opacity: 1;
          transform: translateY(0);
        }
      }

      &:active {
        transform: translateY(-4px) scale(0.98);
        transition: all 0.1s ease;
      }

      .dish-image-background {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        z-index: 1;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
          filter: brightness(0.95) contrast(1.05) saturate(1.05);
        }

        .dish-category {
          position: absolute;
          top: 16px;
          left: 16px;
          background: linear-gradient(
            135deg,
            rgba(255, 107, 107, 0.95) 0%,
            rgba(255, 135, 135, 0.95) 100%
          );
          color: white;
          padding: 8px 16px;
          border-radius: 20px;
          font-size: 13px;
          font-weight: 700;
          backdrop-filter: blur(8px);
          box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
          z-index: 2;
          letter-spacing: 0.5px;
          border: 1px solid rgba(255, 255, 255, 0.2);
        }
      }

      .dish-info-overlay {
        position: relative;
        z-index: 2;
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        padding: 24px;
        background: linear-gradient(
          to top,
          rgba(0, 0, 0, 0.88) 0%,
          rgba(0, 0, 0, 0.65) 35%,
          rgba(0, 0, 0, 0.35) 65%,
          transparent 100%
        );

        .dish-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 12px;
          margin-bottom: 12px;

          .dish-name {
            flex: 1;
            font-size: 24px;
            font-weight: 700;
            color: #fff;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            text-shadow: 0 2px 8px rgba(0, 0, 0, 0.6);
            letter-spacing: 0.5px;
          }

          .dish-actions {
            display: flex;
            gap: 8px;
            flex-shrink: 0;
          }

          .share-btn,
          .favorite-btn {
            width: 40px;
            height: 40px;
            background: rgba(255, 255, 255, 0.15);
            border: 1px solid rgba(255, 255, 255, 0.2);
            backdrop-filter: blur(8px);
            color: #fff;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            opacity: 0.9;

            &:hover {
              background: rgba(255, 255, 255, 0.35);
              transform: scale(1.15) translateY(-2px);
              box-shadow: 0 6px 16px rgba(0, 0, 0, 0.4);
              opacity: 1;
            }

            &:active {
              transform: scale(1.05) translateY(0);
            }
          }

          .favorite-btn {
            &.is-favorite {
              background: rgba(255, 215, 0, 0.35);
              border-color: rgba(255, 215, 0, 0.5);
              color: #ffd700;
              box-shadow: 0 0 16px rgba(255, 215, 0, 0.4);

              .el-icon {
                animation: star-bounce 0.3s ease;
              }
            }
          }
        }

        .dish-meta {
          display: flex;
          justify-content: flex-start;
          align-items: center;
          gap: 12px;
          margin-bottom: 12px;
          font-size: 14px;

          .dish-kcal {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            color: #fff;
            font-weight: 700;
            padding: 8px 16px;
            background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
            border-radius: 20px;
            backdrop-filter: blur(8px);
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
            box-shadow: 0 3px 10px rgba(255, 107, 107, 0.4);
            font-size: 15px;
            border: 1px solid rgba(255, 255, 255, 0.2);

            &::before {
              content: '🔥';
              font-size: 16px;
              animation: flame-flicker 0.5s ease-in-out infinite alternate;
            }
          }

          .dish-tags {
            color: rgba(255, 255, 255, 0.95);
            font-size: 13px;
            font-weight: 600;
            padding: 6px 14px;
            background: rgba(255, 255, 255, 0.15);
            backdrop-filter: blur(8px);
            border-radius: 16px;
            border: 1px solid rgba(255, 255, 255, 0.2);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
          }
        }

        .dish-rating {
          margin-top: 4px;

          :deep(.el-rate) {
            .el-rate__icon {
              font-size: 22px;
              color: #ffd700;
              text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
            }
          }

          :deep(.el-rate__text) {
            color: #fff !important;
            font-size: 16px;
            font-weight: 700;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
          }

          .no-rating {
            display: flex;
            align-items: center;
            gap: 6px;
            color: rgba(255, 255, 255, 0.8);
            font-size: 14px;
            font-weight: 500;

            .el-icon {
              font-size: 18px;
              opacity: 0.6;
            }
          }
        }
      }
    }

    // 轮播指示器样式优化
    :deep(.el-carousel__indicators) {
      .el-carousel__indicator {
        .el-carousel__button {
          width: 24px;
          height: 3px;
          border-radius: 2px;
          background-color: #ddd;
        }

        &.is-active .el-carousel__button {
          background-color: #ff6b6b;
        }
      }
    }

    /* Empty recommendations styling */
    .empty-recommendations {
      margin-bottom: 16px;
      text-align: center;
      padding: 60px 20px;
      background: linear-gradient(135deg, #fff9f9 0%, #fff 100%);
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(255, 107, 107, 0.08);
      border: 1px solid rgba(255, 107, 107, 0.1);
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 6px 20px rgba(255, 107, 107, 0.12);
        transform: translateY(-2px);
      }

      .empty-icon {
        color: #ff9a9a;
        margin-bottom: 24px;
        animation: float 3s ease-in-out infinite;
      }

      /* 美化空状态的文本 */
      :deep(.el-empty__description) {
        color: #666;
        font-size: 15px;
        margin-top: 16px;
        font-weight: 500;
      }

      /* 美化重新加载按钮 */
      :deep(.el-button) {
        margin-top: 24px;
        border-radius: 24px;
        padding: 10px 32px;
        font-size: 14px;
        font-weight: 600;
        background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(255, 107, 107, 0.4);
        }

        &:active {
          transform: translateY(0);
        }
      }
    }
  }

  .hot-section {
    margin-bottom: 20px;

    .hot-card {
      background: linear-gradient(135deg, #fff5f5 0%, #ffe8e8 50%, #fff 100%);
      border: none;
      overflow: hidden;
      position: relative;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 4px 16px rgba(255, 107, 107, 0.1);
      cursor: default;

      &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        width: 4px;
        height: 100%;
        background: linear-gradient(180deg, #ff6b6b 0%, #ff8787 100%);
      }

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
      }

      // 可点击状态
      &.is-clickable {
        cursor: pointer;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 8px 24px rgba(255, 107, 107, 0.3);
        }

        .hot-content {
          .hot-description {
            color: #ff6b6b;
            font-weight: 600;
          }

          .hot-arrow {
            opacity: 1;
            transform: translateX(4px);
          }
        }
      }

      :deep(.el-card__body) {
        padding: 20px 24px;
      }
    }

    .hot-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .hot-icon-wrapper {
        display: flex;
        align-items: center;
        gap: 12px;
        flex-shrink: 0;

        .fire-icon {
          font-size: 36px;
          animation: fire-pulse 2s ease-in-out infinite;
          display: inline-block;
          filter: drop-shadow(0 2px 4px rgba(255, 107, 107, 0.3));
        }

        .hot-badge {
          background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
          color: white;
          padding: 6px 14px;
          border-radius: 16px;
          font-size: 12px;
          font-weight: 700;
          letter-spacing: 1px;
          box-shadow: 0 4px 12px rgba(255, 107, 107, 0.4);
          border: 1px solid rgba(255, 255, 255, 0.2);
        }
      }

      .hot-text {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 6px;

        .hot-label {
          font-size: 13px;
          color: #999;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.5px;
        }

        .hot-description {
          font-size: 17px;
          color: #333;
          font-weight: 600;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          line-height: 1.4;
        }
      }

      .hot-arrow {
        color: #ff6b6b;
        font-size: 20px;
        flex-shrink: 0;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        background: rgba(255, 107, 107, 0.1);
        padding: 8px;
        border-radius: 12px;
        opacity: 0.5;

        &:hover {
          transform: translateX(6px);
          background: rgba(255, 107, 107, 0.2);
        }
      }
    }
  }

  // 火焰动画
  @keyframes fire-pulse {
    0%,
    100% {
      transform: scale(1) rotate(0deg);
    }
    25% {
      transform: scale(1.05) rotate(-3deg);
    }
    50% {
      transform: scale(1.1) rotate(0deg);
    }
    75% {
      transform: scale(1.05) rotate(3deg);
    }
  }

  .nearby-section {
    margin-bottom: 16px;

    .nearby-btn {
      background-color: #ff6b6b;
      border: none;
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 500;

      &:hover {
        background-color: #ff5252;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }

  .tutorial-section {
    margin-bottom: 16px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h3 {
        margin: 0;
        font-size: 20px;
        font-weight: bold;
        color: #333;
      }

      .header-actions {
        display: flex;
        gap: 12px;
        align-items: center;
      }

      .publish-btn {
        display: flex;
        align-items: center;
        gap: 6px;
        border-radius: 20px;
        padding: 8px 16px;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.3s;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }

        .el-icon {
          font-size: 16px;
        }
      }

      .view-all-btn {
        font-size: 14px;
        display: flex;
        align-items: center;
        gap: 4px;

        &:hover {
          .el-icon {
            transform: translateX(4px);
          }
        }

        .el-icon {
          transition: transform 0.3s ease;
        }
      }
    }

    .tutorial-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;
      margin-bottom: 20px;
    }

    .tutorial-card {
      height: 200px;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(255, 107, 107, 0.15);

        .tutorial-thumbnail img {
          transform: scale(1.05);
        }
      }

      .tutorial-thumbnail {
        width: 100%;
        height: 120px;
        position: relative;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform 0.3s ease;
        }

        .tutorial-type-badge {
          position: absolute;
          top: 8px;
          right: 8px;
          width: 32px;
          height: 32px;
          background: rgba(0, 0, 0, 0.6);
          backdrop-filter: blur(4px);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 14px;

          .el-icon {
            font-size: 16px;
          }
        }
      }

      .tutorial-content {
        flex: 1;
        padding: 12px;
        display: flex;
        flex-direction: column;

        .tutorial-title {
          margin: 0 0 8px 0;
          font-size: 15px;
          font-weight: 600;
          color: #333;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .tutorial-source-badges {
          display: flex;
          gap: 6px;
          margin-bottom: 8px;
          flex-wrap: wrap;

          .el-tag {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 11px;
            padding: 2px 8px;
            border-radius: 10px;
            height: 20px;
            line-height: 1;

            .el-icon {
              font-size: 12px;
            }

            .reviewed-badge {
              margin-left: 4px;
              padding-left: 4px;
              border-left: 1px solid currentColor;
            }
          }
        }

        .tutorial-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: auto;
          font-size: 13px;

          .tutorial-duration {
            color: #999;
          }

          .el-rate {
            :deep(.el-rate__text) {
              font-size: 12px;
            }
          }
        }
      }
    }

    .empty-tutorials {
      margin-bottom: 20px;
      text-align: center;
      padding: 60px 0;
      background-color: #fafafa;
      border-radius: 10px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);

      .empty-icon {
        color: #ddd;
        margin-bottom: 20px;
      }
    }

    /* 美化空状态的文本 */
    :deep(.el-empty__description) {
      color: #909399;
      font-size: 16px;
      margin-top: 20px;
    }

    /* 美化重新加载按钮 */
    .empty-tutorials .el-button {
      margin-top: 30px;
      border-radius: 25px;
      padding: 8px 32px;
      font-size: 14px;
    }
  }

  // 星星收藏动画
  @keyframes star-bounce {
    0% {
      transform: scale(1);
    }
    50% {
      transform: scale(1.3);
    }
    100% {
      transform: scale(1);
    }
  }

  // 淡入动画
  .fade-in {
    animation: fadeInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  }

  @keyframes fadeInUp {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // 为轮播项添加交错动画
  :deep(.el-carousel-item) {
    transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);

    &.is-active {
      .dish-card {
        animation: cardSlideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
      }
    }
  }

  @keyframes cardSlideIn {
    0% {
      opacity: 0;
      transform: translateX(30px) scale(0.95);
    }
    100% {
      opacity: 1;
      transform: translateX(0) scale(1);
    }
  }

  // 骨架屏样式
  .skeleton-wrapper {
    margin-bottom: 16px;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

    :deep(.el-skeleton) {
      padding: 0;

      .el-skeleton__item {
        background: linear-gradient(
          90deg,
          rgba(0, 0, 0, 0.06) 25%,
          rgba(0, 0, 0, 0.12) 50%,
          rgba(0, 0, 0, 0.06) 75%
        );
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s ease-in-out infinite;
      }
    }
  }

  .tutorial-skeleton {
    margin-bottom: 16px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

    :deep(.el-skeleton) {
      .el-skeleton__item {
        background: linear-gradient(
          90deg,
          rgba(0, 0, 0, 0.06) 25%,
          rgba(0, 0, 0, 0.12) 50%,
          rgba(0, 0, 0, 0.06) 75%
        );
        background-size: 200% 100%;
        animation: skeleton-loading 1.5s ease-in-out infinite;
      }
    }
  }

  // 骨架屏加载动画
  @keyframes skeleton-loading {
    0% {
      background-position: 200% 0;
    }

    100% {
      background-position: -200% 0;
    }
  }

  // Emoji 弹跳动画
  @keyframes emoji-bounce {
    0%,
    100% {
      transform: translateY(0);
    }

    50% {
      transform: translateY(-8px);
    }
  }

  // 天气详情弹窗样式
  .weather-detail-dialog {
    :deep(.el-dialog__body) {
      padding: 20px;
    }

    .weather-detail-content {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .detail-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
        border-radius: 12px;
        transition: all 0.3s ease;

        &:hover {
          background: linear-gradient(135deg, #e9ecef 0%, #dee2e6 100%);
          transform: translateX(4px);
        }

        .detail-label {
          font-size: 14px;
          color: #666;
          font-weight: 600;
        }

        .detail-value {
          font-size: 15px;
          color: #333;
          font-weight: 700;
        }
      }

      .detail-advice {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-top: 8px;
        padding-top: 16px;
        border-top: 1px solid #e9ecef;

        .advice-item {
          display: flex;
          flex-direction: column;
          gap: 6px;
          padding: 12px;
          background: linear-gradient(135deg, #fff9f0 0%, #fff3e0 100%);
          border-radius: 10px;
          border-left: 3px solid #ff9800;

          .advice-label {
            font-size: 13px;
            color: #ff9800;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.5px;
          }

          .advice-text {
            font-size: 14px;
            color: #555;
            line-height: 1.5;
          }
        }
      }
    }
  }

  // 移动端响应式适配
  @media (max-width: 768px) {
    padding: 12px;

    .weather-section {
      .weather-card {
        :deep(.el-card__body) {
          padding: 18px 20px;
        }
      }

      .weather-content {
        flex-direction: column;
        gap: 16px;

        .weather-visual {
          width: 100%;
          justify-content: center;

          .weather-icon-wrapper {
            width: 64px;
            height: 64px;

            .weather-icon {
              font-size: 36px;
            }
          }

          .temp-display {
            .temp-value {
              font-size: 42px;
              letter-spacing: -1.5px;
            }

            .temp-unit {
              font-size: 18px;
            }
          }
        }

        .weather-info {
          width: 100%;
          gap: 12px;

          .location-section {
            .location-label {
              font-size: 10px;
              gap: 4px;
              margin-bottom: 6px;

              .location-icon {
                font-size: 12px;
              }
            }

            .location-button {
              padding: 8px 14px;
              font-size: 13px;
              width: 100%;
              justify-content: center;

              .location-text {
                font-size: 13px;
                max-width: 200px;
              }

              .edit-icon {
                font-size: 14px;
              }
            }
          }

          .weather-details {
            gap: 8px;

            .condition-badge {
              padding: 6px 12px;
              width: 100%;
              justify-content: center;

              .condition-icon {
                font-size: 16px;
              }

              .condition-text {
                font-size: 13px;
              }
            }

            .recommendation-card {
              padding: 10px 14px;
              width: 100%;

              .recommendation-header {
                gap: 4px;
                margin-bottom: 4px;

                .sparkle-icon {
                  font-size: 12px;
                }

                .recommendation-label {
                  font-size: 9px;
                }
              }

              .recommendation-content {
                font-size: 13px;
                text-align: center;
              }
            }
          }
        }
      }
    }

    .recommendation-section {
      h3 {
        font-size: 18px;
      }

      .dish-card {
        height: 280px;

        .dish-info-overlay {
          padding: 12px;

          .dish-name {
            font-size: 16px;
          }

          .dish-meta {
            font-size: 12px;
            gap: 8px;
          }

          .dish-rating {
            :deep(.el-rate) {
              font-size: 12px;
            }
          }
        }
      }
    }

    .tutorial-section {
      .section-header {
        h3 {
          font-size: 18px;
        }

        .header-actions {
          gap: 8px;
        }

        .publish-btn {
          padding: 6px 12px;
          font-size: 12px;

          .el-icon {
            font-size: 14px;
          }
        }

        .view-all-btn {
          font-size: 12px;
        }
      }

      .tutorial-grid {
        grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
        gap: 12px;
      }

      .tutorial-card {
        height: 180px;

        .tutorial-thumbnail {
          height: 100px;
        }

        .tutorial-content {
          padding: 8px;

          .tutorial-title {
            font-size: 13px;
          }

          .tutorial-meta {
            font-size: 11px;
          }
        }
      }
    }

    .nearby-section {
      .nearby-btn {
        height: 44px;
        font-size: 14px;
      }
    }
  }
}
</style>
