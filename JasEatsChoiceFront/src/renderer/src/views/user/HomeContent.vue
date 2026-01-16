<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElNotification } from 'element-plus'
import { useLocation } from '../../composables/useLocation.js'
// 导入 Element Plus 图标
import {
  Sunny,
  Cloudy,
  Location,
  VideoCamera,
  ArrowRight,
  Star,
  Share,
  Search,
  Coffee,
  Document
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
// 导入 WebSocket 常量
import { WS_CONFIG } from '../../constants/wsConstants.js'

const router = useRouter()

// 默认菜品占位图
const defaultDishImage =
  'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="300"%3E%3Crect fill="%23f0f0f0" width="400" height="300"/%3E%3Ctext fill="%23999" font-family="Arial" font-size="24" x="50%25" y="50%25" text-anchor="middle" dominant-baseline="middle"%3E暂无图片%3C/text%3E%3C/svg%3E'

// 默认教程缩略图
const defaultTutorialThumbnail =
  'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="300" height="200"%3E%3Crect fill="%23f5f5f5" width="300" height="200"/%3E%3Ctext fill="%23999" font-family="Arial" font-size="20" x="50%25" y="50%25" text-anchor="middle" dominant-baseline="middle"%3E教程缩略图%3C/text%3E%3C/svg%3E'

// 图片加载错误处理
const handleImageError = (event) => {
  event.target.src = defaultDishImage
}

// 加载状态
const nearbyLoading = ref(false)
const recommendedDishesLoading = ref(true)
const tutorialsLoading = ref(true)
const refreshing = ref(false)

// 教程数据 - 从后端获取
const featuredTutorials = ref([])

// 今日推荐菜品 - 来自后端
const recommendedDishes = ref([])
// 推荐菜品空状态消息
const recommendEmptyMessage = ref('暂无推荐菜品')
// 今日热点 - 从后端获取
const hotTopic = ref('')
// 收藏的菜品ID列表
const favoriteDishIds = ref(new Set())
// 搜索关键字
const searchKeyword = ref('')

// 天气和位置数据
const weather = ref({
  temp: 32,
  condition: '晴天',
  city: '',
  address: ''
})

// 使用位置选择组合式函数
const { cascaderLocationData, locationDialogVisible, manualLocation, handleManualLocationSelect } =
  useLocation()

// 根据天气条件获取对应的图标
const getWeatherIcon = () => {
  const condition = weather.value.condition
  if (!condition) return Sunny
  if (condition.includes('晴')) return Sunny
  if (
    condition.includes('云') ||
    condition.includes('阴') ||
    condition.includes('雨') ||
    condition.includes('雷') ||
    condition.includes('雪')
  )
    return Cloudy
  return Sunny
}

// 根据天气条件获取推荐的菜品系列
const getRecommendedDishesSeries = () => {
  const condition = weather.value.condition
  const temp = weather.value.temp

  // 默认值
  if (condition === undefined) {
    return '热门推荐'
  }

  // 高温天气推荐
  if (temp > 28 || condition.includes('晴')) {
    return '冰饮/凉菜系列'
  }
  // 低温天气推荐
  if (temp < 15 || condition.includes('雪')) {
    return '热食/火锅系列'
  }
  // 雨天推荐
  if (condition.includes('雨')) {
    return '汤品/暖食系列'
  }
  // 多云阴天推荐
  if (condition.includes('云') || condition.includes('阴')) {
    return '均衡饮食系列'
  }
  // 默认推荐
  return '特色菜品系列'
}

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
      hotTopic.value = response.data
    } else {
      // 接口成功但返回空数据时,清空热点
      hotTopic.value = ''
    }
  } catch (error) {
    console.error('加载今日热点失败:', error)
    // 请求失败时使用默认文本
    hotTopic.value = ''
    // 热点不是关键功能,只记录错误不显示通知
  }
}

// 处理自动定位
const handleAutoLocation = async () => {
  // 调用现有 fetchWeather 函数（无参数）获取自动定位
  await fetchWeather()
  // 定位成功后关闭对话框
  locationDialogVisible.value = false
}

// 处理位置确认
const handleConfirmLocation = () => {
  if (manualLocation.value && manualLocation.value.length > 0) {
    // 对于级联选择器，将数组拼接成完整地址字符串
    const fullAddress = manualLocation.value.join('')
    // 从位置数组中提取城市用于天气API (简化逻辑)
    const city = manualLocation.value[1] || manualLocation.value[0] || ''

    // 立即在UI上更新地址 - 确保不是数组或空数组
    weather.value.address = Array.isArray(fullAddress)
      ? '未获取到详细地址'
      : fullAddress || '未获取到详细地址'
    weather.value.city = Array.isArray(city) ? city.join('') : city || '未知城市'

    // 获取详细天气信息
    fetchWeather(city).then(() => {
      locationDialogVisible.value = false
    })
  } else {
    // 如果未选择手动位置，则使用自动定位
    handleAutoLocation()
  }
}

// 从后端获取位置和天气数据
const fetchWeather = async (selectedCity = null) => {
  try {
    if (selectedCity) {
      // 为选择的城市获取天气信息
      weather.value.city = selectedCity
      const weatherResponse = await api.get(
        `${API_CONFIG.weather.current}?city=${encodeURIComponent(selectedCity)}`
      )
      if (weatherResponse?.data) {
        const { temperature, condition } = weatherResponse.data
        // 仅当值已定义时才更新
        if (temperature !== undefined) {
          weather.value.temp = temperature
        }
        if (condition !== undefined) {
          weather.value.condition = condition
        }
      }
    } else {
      // 步骤1: 从后端获取当前位置
      const locationResponse = await api.get(API_CONFIG.location.location)
      if (locationResponse.data) {
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

        // 步骤2: 根据城市获取天气信息
        const weatherResponse = await api.get(
          `${API_CONFIG.weather.current}?city=${encodeURIComponent(city)}`
        )
        if (weatherResponse?.data) {
          const { temperature, condition } = weatherResponse.data
          // 仅当值已定义时才更新
          if (temperature !== undefined) {
            weather.value.temp = temperature
          }
          if (condition !== undefined) {
            weather.value.condition = condition
          }
        }
      }
    }
  } catch (error) {
    console.error(selectedCity ? '加载天气失败:' : '加载天气或位置失败:', error)
  }
  console.log('获取天气数据:', weather.value)
}

// 处理菜单导航
const navigateTo = (path) => {
  router.push(path)
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
  // TODO: 跳转到教程详情页或执行其他操作
  console.log('点击教程:', tutorial.name)
  // router.push(`/user/home/tutorials/${tutorial.id}`)
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
      await new Promise(resolve => setTimeout(resolve, delay * Math.pow(2, i)))
    }
  }
}

// WebSocket 连接
let wsAttempts = 0
const maxAttempts = 10

// 使用主进程 WebSocket 初始化带有自动重连功能的 WebSocket 连接
const initializeWebSocket = () => {
  // 使用 WebSocket 常量构建完整 URL
  const wsUrl = `${WS_CONFIG.URL}${WS_CONFIG.ENDPOINT}` // 后端 Netty 服务器 URL

  console.log('Connecting to WebSocket server:', wsUrl)

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
    console.log('WebSocket connection established')

    // 必要时发送身份验证
    const authMsg = {
      msgType: 'auth',
      userId: localStorage.getItem('userId'), // 替换为实际用户 ID
      token: 'test-token' // 替换为实际令牌
    }
    sendWebSocketMessage(authMsg)
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
    console.log('WebSocket connection closed:', code, reason)

    // 如果未达到最大尝试次数则自动重连
    if (wsAttempts < maxAttempts) {
      wsAttempts++
      const delay = Math.min(3000 * wsAttempts, 30000) // 指数退避
      setTimeout(() => {
        console.log(`Reconnecting WebSocket... Attempt ${wsAttempts}/${maxAttempts}`)
        initializeWebSocket()
      }, delay)
    }
  })

  window.api?.onWebSocketError((error) => {
    console.error('WebSocket error:', error)
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

// 下拉刷新功能
const onRefresh = async () => {
  refreshing.value = true
  try {
    await Promise.all([
      fetchFeaturedTutorials(),
      fetchRecommendedDishes(),
      fetchHotTopic(),
      fetchWeather()
    ])
  } catch (error) {
    console.error('刷新失败:', error)
  } finally {
    refreshing.value = false
  }
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
  <el-pull-refresh v-model="refreshing" @refresh="onRefresh" aria-label="下拉刷新内容">
    <!-- 搜索框 -->
    <div class="search-section" role="search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索菜品、教程..."
        :prefix-icon="Search"
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

    <!-- Right Content Area -->
    <div class="weather-section" role="region" aria-label="天气信息">
    <el-card shadow="hover" class="weather-card">
      <div class="weather-content">
        <el-icon class="weather-icon"><component :is="getWeatherIcon()" /></el-icon>
        <div class="weather-info">
          <div class="location">
            <el-button type="text" size="small" @click="locationDialogVisible = true" class="location-button">
              <el-icon><Location /></el-icon>
              <span class="location-text">
                {{ weather.address || weather.city || '选择位置' }}
              </span>
              <el-tag size="small" type="info" effect="plain" round>切换</el-tag>
            </el-button>
          </div>
          <div class="temp-section">
            <span class="temp">{{ weather.temp }}℃</span>
            <span class="weather-condition">{{ weather.condition }}</span>
          </div>
          <div class="recommendation">
            <el-icon class="sparkle-icon">✨</el-icon>
            <span>{{ getRecommendedDishesSeries() }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>

  <div class="recommendation-section" role="region" aria-label="今日推荐菜品">
    <h3 id="recommendations-heading">今日推荐</h3>
    <!-- 骨架屏加载中 -->
    <div v-if="recommendedDishesLoading" class="skeleton-wrapper">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item variant="image" style="width: 100%; height: 320px; border-radius: 8px" />
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
                  v-model="dish.rating"
                  disabled
                  show-score
                  text-color="#FF6B6B"
                  class="rating"
                ></el-rate>
              </div>
            </div>
          </el-card>
        </el-carousel-item>
      </el-carousel>
    </div>
  </div>

  <!-- 今日热点 - 只有当有数据时显示 -->
  <div class="hot-section" v-if="hotTopic">
    <el-card shadow="hover" class="hot-card">
      <div class="hot-content">
        <div class="hot-icon-wrapper">
          <span class="fire-icon">🔥</span>
          <span class="hot-badge">HOT</span>
        </div>
        <div class="hot-text">
          <span class="hot-label">今日热点</span>
          <span class="hot-description">{{ hotTopic }}</span>
        </div>
        <el-icon class="hot-arrow"><ArrowRight /></el-icon>
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
        @click="navigateTo('/user/home/tutorials')"
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
            <el-skeleton-item variant="image" style="width: 100%; height: 120px; border-radius: 4px" />
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
          :aria-label="`教程: ${tutorial.name}, ${tutorial.duration || '5分钟'}`"
          role="listitem"
          tabindex="0"
          @keyup.enter="handleTutorialClick(tutorial)"
        >
          <div class="tutorial-thumbnail">
            <img
              :src="tutorial.thumbnail || defaultTutorialThumbnail"
              :alt="tutorial.name"
              loading="lazy"
            />
            <div class="tutorial-type-badge">
              <el-icon v-if="tutorial.type === 'video'"><VideoCamera /></el-icon>
              <span v-else>💡</span>
            </div>
          </div>
          <div class="tutorial-content">
            <h4 class="tutorial-title">{{ tutorial.name }}</h4>
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

  <!-- Location Selection Dialog -->
  <el-dialog v-model="locationDialogVisible" title="选择位置" width="400px">
    <div class="location-dialog-content">
      <!-- Auto-location button -->
      <el-button type="primary" class="auto-location-btn" @click="handleAutoLocation">
        <el-icon><Location /></el-icon>
        自动定位
      </el-button>

      <!-- Manual location selection -->
      <div class="manual-location-section">
        <h4>手动选择</h4>
        <el-cascader
          v-model="manualLocation"
          :options="cascaderLocationData"
          placeholder="请选择省/市/区"
          style="width: 100%"
          @change="handleManualLocationSelect"
          clearable
        />
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="locationDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmLocation"> 确认 </el-button>
      </span>
    </template>
  </el-dialog>
  </el-pull-refresh>
</template>

<style scoped lang="less">
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
      :deep(.el-input__wrapper) {
        border-radius: 24px;
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
        border-radius: 0 24px 24px 0;
        background-color: #ff6b6b;
        border-color: #ff6b6b;
        color: #fff;

        .el-button {
          background-color: transparent;
          border: none;
          color: #fff;

          &:hover {
            background-color: rgba(255, 255, 255, 0.1);
          }
        }
      }
    }
  }

  .weather-section {
    margin-bottom: 16px;

    .weather-card {
      background: linear-gradient(135deg, #fff9f0 0%, #fff 100%);
      border: 1px solid #ffe8cc;

      :deep(.el-card__body) {
        padding: 20px;
      }
    }

    .weather-content {
      display: flex;
      align-items: center;
      gap: 20px;

      .weather-icon {
        font-size: 56px;
        color: #f7b267;
        flex-shrink: 0;
      }

      .weather-info {
        flex: 1;
        font-size: 18px;

        .location {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;

          .location-button {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 8px;
            border-radius: 8px;
            transition: all 0.3s ease;

            &:hover {
              background-color: rgba(255, 107, 107, 0.1);
            }

            .location-text {
              font-size: 14px;
              color: #333;
              max-width: 200px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }

            .el-tag {
              margin-left: 4px;
              font-size: 11px;
              padding: 0 8px;
              height: 20px;
              line-height: 20px;
            }
          }
        }

        .temp-section {
          display: flex;
          align-items: baseline;
          gap: 12px;
          margin-bottom: 8px;

          .temp {
            font-size: 32px;
            font-weight: bold;
            color: #ff6b6b;
          }

          .weather-condition {
            font-size: 16px;
            color: #999;
          }
        }

        .recommendation {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 15px;
          color: #666;

          .sparkle-icon {
            font-size: 16px;
            animation: sparkle 1.5s ease-in-out infinite;
          }
        }
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
      transform: scale(1.2);
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
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(255, 107, 107, 0.15);
      }

      &:active {
        transform: translateY(-2px);
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
          transition: transform 0.3s ease;
        }

        &:hover img {
          transform: scale(1.05);
        }

        .dish-category {
          position: absolute;
          top: 12px;
          left: 12px;
          background: rgba(255, 107, 107, 0.9);
          color: white;
          padding: 4px 12px;
          border-radius: 12px;
          font-size: 12px;
          font-weight: bold;
          backdrop-filter: blur(4px);
          z-index: 2;
        }
      }

      .dish-info-overlay {
        position: relative;
        z-index: 2;
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        padding: 20px;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.3) 50%, transparent 100%);

        .dish-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;

          .dish-name {
            flex: 1;
            font-size: 20px;
            font-weight: bold;
            color: #fff;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
          }

          .dish-actions {
            display: flex;
            gap: 8px;
            flex-shrink: 0;
          }

          .share-btn,
          .favorite-btn {
            background: rgba(255, 255, 255, 0.2);
            border: none;
            backdrop-filter: blur(4px);
            color: #fff;
            transition: all 0.3s ease;

            &:hover {
              background: rgba(255, 255, 255, 0.3);
              transform: scale(1.1);
            }

            &:active {
              transform: scale(0.95);
            }
          }

          .favorite-btn {
            &.is-favorite {
              background: #ff6b6b;
              color: #fff;

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
          margin-bottom: 8px;
          font-size: 14px;

          .dish-kcal {
            color: #fff;
            font-weight: 500;
            text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
          }

          .dish-tags {
            color: #fff;
            font-size: 12px;
            padding: 2px 8px;
            background: rgba(255, 107, 107, 0.8);
            border-radius: 8px;
            text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
          }
        }

        .dish-rating {
          margin-top: 4px;

          :deep(.el-rate__text) {
            color: #fff !important;
            text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
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
      padding: 60px 0;
      background-color: #fafafa;
      border-radius: 10px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);

      .empty-icon {
        color: #ddd;
        margin-bottom: 20px;
      }
    }
  }

  .hot-section {
    margin-bottom: 16px;

    .hot-card {
      background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
      border: 1px solid #ffe0e0;

      :deep(.el-card__body) {
        padding: 16px 20px;
      }
    }

    .hot-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .hot-icon-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-shrink: 0;

        .fire-icon {
          font-size: 28px;
          animation: fire-pulse 1.5s ease-in-out infinite;
          display: inline-block;
        }

        .hot-badge {
          background: #ff6b6b;
          color: white;
          padding: 2px 10px;
          border-radius: 12px;
          font-size: 11px;
          font-weight: bold;
          letter-spacing: 0.5px;
          box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
        }
      }

      .hot-text {
        flex: 1;
        display: flex;
        flex-direction: column;
        gap: 4px;

        .hot-label {
          font-size: 12px;
          color: #999;
          font-weight: 500;
        }

        .hot-description {
          font-size: 16px;
          color: #333;
          font-weight: 500;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .hot-arrow {
        color: #ff6b6b;
        font-size: 18px;
        flex-shrink: 0;
        transition: transform 0.3s ease;

        &:hover {
          transform: translateX(4px);
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

  /* Location dialog styles */
  .location-dialog-content {
    padding: 20px 0;

    .auto-location-btn {
      margin-bottom: 20px;
      width: 100%;
    }

    .manual-location-section {
      h4 {
        margin: 0 0 10px 0;
        font-size: 14px;
        font-weight: bold;
      }

      .location-note {
        font-size: 12px;
        color: #909399;
        margin-top: 5px;
      }
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
    animation: fadeIn 0.5s ease-in;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  // 骨架屏样式
  .skeleton-wrapper {
    margin-bottom: 16px;
  }

  .tutorial-skeleton {
    margin-bottom: 16px;
  }

  // 移动端响应式适配
  @media (max-width: 768px) {
    padding: 12px;

    .weather-section {
      .weather-content {
        gap: 12px;

        .weather-icon {
          font-size: 40px;
        }

        .weather-info {
          font-size: 14px;

          .location {
            .location-button {
              padding: 2px 6px;

              .location-text {
                max-width: 120px;
                font-size: 12px;
              }
            }
          }

          .temp-section {
            .temp {
              font-size: 24px;
            }

            .weather-condition {
              font-size: 14px;
            }
          }

          .recommendation {
            font-size: 13px;
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
