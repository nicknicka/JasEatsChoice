<script setup>
import { ref, onMounted } from 'vue'
import { useLocation } from '../../composables/useLocation.js'
// 导入 Element Plus 图标
import { Sunny, Cloudy, Location, VideoCamera, ArrowRight } from '@element-plus/icons-vue'
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

// 教程数据 - 从后端获取
const featuredTutorials = ref([])

// 今日推荐菜品 - 来自后端
const recommendedDishes = ref([])
// 推荐菜品空状态消息
const recommendEmptyMessage = ref('暂无推荐菜品')
// 今日热点 - 从后端获取
const hotTopic = ref('')

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
const fetchRecommendedDishes = () => {
  api
    .get(API_CONFIG.recipe.recommend)
    .then((response) => {
      // Check if response has a message
      if (response.message) {
        recommendEmptyMessage.value = response.message
      }

      // Handle both null/undefined and empty array cases
      if (response.data && Array.isArray(response.data) && response.data.length > 0) {
        recommendedDishes.value = response.data
      } else {
        // Set to empty array to show empty state
        recommendedDishes.value = []
      }
    })
    .catch((error) => {
      console.error('加载推荐菜品失败:', error)
      // Reset to default message on error
      recommendEmptyMessage.value = '暂无推荐菜品'
    })
}

// 从后端获取今日热点
const fetchHotTopic = () => {
  // 假设后端提供了获取今日热点的API
  api
    .get(API_CONFIG.home.hotTopic)
    .then((response) => {
      if (response.data) {
        hotTopic.value = response.data
      } else {
        // 接口成功但返回空数据时，清空热点
        hotTopic.value = ''
      }
    })
    .catch((error) => {
      console.error('加载今日热点失败:', error)
      // 请求失败时使用默认文本
      hotTopic.value = ''
    })
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
const fetchFeaturedTutorials = () => {
  api
    .get(API_CONFIG.tutorial.featured)
    .then((response) => {
      // Handle both null/undefined and empty array cases for consistency
      if (response.data && Array.isArray(response.data) && response.data.length > 0) {
        featuredTutorials.value = response.data
      } else {
        featuredTutorials.value = []
      }
    })
    .catch((error) => {
      console.error('加载精选教程失败:', error)
      // 失败时使用模拟数据作为备份
      featuredTutorials.value = [
        { name: '青木瓜沙拉制作教程', type: 'video' },
        { name: '夏日低卡饮食指南', type: 'article' }
      ]
    })
}

// 在挂载时初始化WebSocket
onMounted(async () => {
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
  <!-- Right Content Area -->
  <div class="weather-section">
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

  <div class="recommendation-section">
    <h3>今日推荐</h3>
    <!-- When there are no recommended dishes -->
    <div v-if="recommendedDishes.length === 0" class="empty-recommendations">
      <el-empty :description="recommendEmptyMessage">
        <el-button type="primary" @click="fetchRecommendedDishes">重新加载</el-button>
      </el-empty>
    </div>

    <!-- When there are recommended dishes -->
    <div v-else>
      <el-carousel
        :interval="3000"
        height="320px"
        indicator-position="outside"
        arrow="never"
        class="recommendation-carousel"
      >
        <el-carousel-item v-for="(dish, index) in recommendedDishes" :key="index">
          <el-card shadow="hover" class="dish-card enhanced-card">
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
              <div class="dish-name">{{ dish.name }}</div>
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

  <div class="tutorial-section">
    <div class="section-header">
      <h3>制作教程与指南</h3>
      <el-button text type="primary" @click="navigateTo('/user/home/tutorials')" class="view-all-btn">
        查看全部 <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <!-- 当教程数据为空时显示 -->
    <div v-if="featuredTutorials.length === 0" class="empty-tutorials">
      <el-empty description="暂无教程数据">
        <el-button type="primary" @click="fetchFeaturedTutorials">重新加载</el-button>
      </el-empty>
    </div>

    <!-- 当教程数据不为空时显示 -->
    <div v-else>
      <div class="tutorial-grid">
        <el-card
          shadow="hover"
          class="tutorial-card enhanced"
          v-for="(tutorial, index) in featuredTutorials.slice(0, 4)"
          :key="index"
          @click="handleTutorialClick(tutorial)"
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

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(255, 107, 107, 0.15);
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

        .dish-name {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 8px;
          color: #fff;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.5);
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
}
</style>
