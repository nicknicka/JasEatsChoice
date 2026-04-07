<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/authStore'
import api, { decodeJwt } from '../../utils/api.js'
import { API_CONFIG, WS_CONFIG } from '../../config/index.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import {
  Bell,
  ChatDotRound,
  Notification,
  ChatLineSquare,
  Refresh,
  Check,
  Filter,
  Search,
  Delete,
  Download,
  CircleCheck,
  Select
} from '@element-plus/icons-vue'

const router = useRouter()

// 消息分类映射
const messageCategories = {
  all: { text: '所有消息', icon: ChatDotRound },
  system: { text: '系统通知', icon: Notification },
  order: { text: '订单消息', icon: Bell },
  comment: { text: '评价消息', icon: ChatLineSquare }
}

// 消息数据，将从后端API获取
const messages = ref([])
const selectedMessage = ref(null)
const activeCategory = ref('all')
const loading = ref(false)

// 筛选后的消息
const filteredMessages = ref([])

// 未读消息统计
const unreadCounts = ref({
  system: 0,
  order: 0,
  comment: 0,
  total: 0
})

// 搜索关键词
const searchKeyword = ref('')

// WebSocket 连接
let websocket = null
const reconnectAttempts = ref(0)
const maxReconnectAttempts = 5

// 批量选择
const selectedMessages = ref([])
const selectMode = ref(false)

// 用户ID
const userId = ref('')

// 数字动画
const animatedValues = ref({
  total: 0,
  system: 0,
  order: 0,
  comment: 0
})

// 动画数字
const animateValue = (key, endValue, duration = 1000) => {
  const startValue = animatedValues.value[key]
  const startTime = performance.now()

  const animate = (currentTime) => {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)
    animatedValues.value[key] = Math.floor(startValue + (endValue - startValue) * easeOutQuart)

    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      animatedValues.value[key] = endValue
    }
  }

  requestAnimationFrame(animate)
}

// ==================== WebSocket 实时推送 ====================

// 初始化 WebSocket 连接
const initWebSocket = () => {
  if (!userId.value) return

  try {
    // 获取认证 token
    const authStore = useAuthStore()
    const token = authStore.token

    if (!token) {
      console.warn('⚠️ 无法获取 token，跳过 WebSocket 连接')
      return
    }

    // 构建 WebSocket URL（需要传递 userId 和 token 两个参数）
    const wsUrl = `${WS_CONFIG.chatUrl}?userId=${userId.value}&token=${token}`
    websocket = new WebSocket(wsUrl)

    websocket.onopen = () => {
      console.log('✅ WebSocket 连接成功')
      reconnectAttempts.value = 0
    }

    websocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        console.log('📨 收到新消息:', data)

        // 添加到消息列表
        if (data.type || data.content) {
          const newMessage = {
            id: data.id || data.msgId || Date.now(),
            title: data.content || data.title,
            content: data.content || '',
            sender: data.senderName || data.fromId || '系统',
            time: data.createTime || data.time || new Date().toISOString().slice(0, 19).replace('T', ' '),
            isRead: data.readStatus || false,
            type: data.type || 'system'
          }

          messages.value.unshift(newMessage)
          updateFilter()

          // 显示通知
          ElMessage.success({
            message: `收到新消息: ${newMessage.title}`,
            duration: 3000,
            showClose: true
          })
        }
      } catch (error) {
        console.error('处理 WebSocket 消息失败:', error)
      }
    }

    websocket.onerror = (error) => {
      console.error('❌ WebSocket 错误:', error)
    }

    websocket.onclose = () => {
      console.log('🔌 WebSocket 连接关闭')
      // 尝试重连
      if (reconnectAttempts.value < maxReconnectAttempts) {
        reconnectAttempts.value++
        console.log(`🔄 尝试重连 (${reconnectAttempts.value}/${maxReconnectAttempts})...`)
        setTimeout(() => {
          initWebSocket()
        }, 3000)
      }
    }
  } catch (error) {
    console.error('WebSocket 初始化失败:', error)
  }
}

// 关闭 WebSocket 连接
const closeWebSocket = () => {
  if (websocket) {
    websocket.close()
    websocket = null
  }
}

// ==================== 消息加载和刷新 ====================

// 加载消息列表
const loadMessages = async () => {
  loading.value = true
  try {
    const response = await api.get(API_CONFIG.message.list, {
      params: { userId: userId.value }
    })

    if (response.data && response.data.success) {
      // 转换后端返回的数据格式
      const formattedMessages = response.data.data.map((message) => ({
        id: message.id,
        title: message.content,
        content: message.content,
        sender: message.senderName,
        time: message.createTime,
        isRead: message.readStatus,
        // ⭐ 根据后端返回的类型或内容判断消息类型
        type: determineMessageType(message)
      }))

      messages.value = formattedMessages
      updateFilter()
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 判断消息类型
const determineMessageType = (message) => {
  // 如果后端已经返回了类型，直接使用
  if (message.type && ['system', 'order', 'comment'].includes(message.type)) {
    return message.type
  }

  // 否则根据内容关键词判断
  const content = (message.content || '').toLowerCase()
  if (content.includes('订单') || content.includes('order')) {
    return 'order'
  } else if (content.includes('评价') || content.includes('评论') || content.includes('review')) {
    return 'comment'
  }
  return 'system'
}

// ==================== 搜索和筛选 ====================

// 搜索消息
const searchMessages = computed(() => {
  if (!searchKeyword.value) {
    return filteredMessages.value
  }

  const keyword = searchKeyword.value.toLowerCase()
  return filteredMessages.value.filter(
    (msg) =>
      msg.title.toLowerCase().includes(keyword) ||
      msg.content.toLowerCase().includes(keyword) ||
      (msg.sender && msg.sender.toLowerCase().includes(keyword))
  )
})

// ==================== 消息操作 ====================

// 标记单条消息为已读（同步到后端）
const markAsRead = async (message) => {
  try {
    // ⭐ 调用后端 API 标记已读
    await api.put(`${API_CONFIG.message.send}/${message.id}/read`)

    // 更新本地状态
    message.isRead = true
    ElMessage.success('消息已标记为已读')
    updateFilter()
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败，请稍后重试')
  }
}

// 标记所有消息为已读（同步到后端）
const markAllAsRead = async () => {
  try {
    const unreadMessages = filteredMessages.value.filter((msg) => !msg.isRead)

    if (unreadMessages.length === 0) {
      ElMessage.info('没有未读消息')
      return
    }

    // 批量标记已读
    const promises = unreadMessages.map((msg) =>
      api.put(`${API_CONFIG.message.send}/${msg.id}/read`)
    )

    await Promise.all(promises)

    // 更新本地状态
    filteredMessages.value.forEach((message) => {
      message.isRead = true
    })

    ElMessage.success('所有消息已标记为已读')
    updateFilter()
  } catch (error) {
    console.error('批量标记已读失败:', error)
    ElMessage.error('批量标记已读失败，请稍后重试')
  }
}

// 删除单条消息
const deleteMessage = async (message) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '删除确认', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // ⭐ 调用后端 API 删除消息
    await api.delete(`${API_CONFIG.message.send}/${message.id}`)

    // 从本地列表中移除
    const index = messages.value.findIndex((m) => m.id === message.id)
    if (index > -1) {
      messages.value.splice(index, 1)
    }

    ElMessage.success('消息已删除')
    updateFilter()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除失败，请稍后重试')
    }
  }
}

// 批量删除消息
const batchDeleteMessages = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择要删除的消息')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedMessages.value.length} 条消息吗？`,
      '批量删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 批量删除
    const promises = selectedMessages.value.map((msg) =>
      api.delete(`${API_CONFIG.message.send}/${msg.id}`)
    )

    await Promise.all(promises)

    // 从本地列表中移除
    const deletedIds = selectedMessages.value.map((msg) => msg.id)
    messages.value = messages.value.filter((msg) => !deletedIds.includes(msg.id))

    ElMessage.success(`成功删除 ${selectedMessages.value.length} 条消息`)

    // 退出选择模式并清空选择
    selectMode.value = false
    selectedMessages.value = []

    updateFilter()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败，请稍后重试')
    }
  }
}

// 批量标记已读
const batchMarkAsRead = async () => {
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请先选择要操作的消息')
    return
  }

  try {
    const unreadSelected = selectedMessages.value.filter((msg) => !msg.isRead)

    if (unreadSelected.length === 0) {
      ElMessage.info('选中的消息都已标记为已读')
      return
    }

    // 批量标记已读
    const promises = unreadSelected.map((msg) =>
      api.put(`${API_CONFIG.message.send}/${msg.id}/read`)
    )

    await Promise.all(promises)

    // 更新本地状态
    selectedMessages.value.forEach((msg) => {
      msg.isRead = true
    })

    ElMessage.success(`成功标记 ${unreadSelected.length} 条消息为已读`)

    // 退出选择模式并清空选择
    selectMode.value = false
    selectedMessages.value = []

    updateFilter()
  } catch (error) {
    console.error('批量标记已读失败:', error)
    ElMessage.error('批量标记已读失败，请稍后重试')
  }
}

// 切换选择模式
const toggleSelectMode = () => {
  selectMode.value = !selectMode.value
  if (!selectMode.value) {
    selectedMessages.value = []
  }
}

// 切换消息选择状态
const toggleMessageSelection = (message) => {
  const index = selectedMessages.value.findIndex((m) => m.id === message.id)
  if (index > -1) {
    selectedMessages.value.splice(index, 1)
  } else {
    selectedMessages.value.push(message)
  }
}

// 全选/取消全选
const toggleSelectAll = () => {
  if (selectedMessages.value.length === searchMessages.value.length) {
    // 取消全选
    selectedMessages.value = []
  } else {
    // 全选
    selectedMessages.value = [...searchMessages.value]
  }
}

// 导出消息
const exportMessages = async () => {
  try {
    const dataToExport = searchMessages.value.map((msg) => ({
      标题: msg.title,
      内容: msg.content,
      发送者: msg.sender,
      时间: msg.time,
      状态: msg.isRead ? '已读' : '未读',
      类型: msg.type
    }))

    // 转换为 JSON 字符串
    const jsonStr = JSON.stringify(dataToExport, null, 2)

    // 创建 Blob 对象
    const blob = new Blob([jsonStr], { type: 'application/json' })

    // 创建下载链接
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `消息导出_${new Date().toISOString().slice(0, 10)}.json`

    // 触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    // 释放 URL 对象
    URL.revokeObjectURL(url)

    ElMessage.success('消息导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请稍后重试')
  }
}

// 总计统计
const totalStats = computed(() => {
  return {
    total: messages.value.length,
    todayMessages: messages.value.filter((msg) => {
      const msgDate = new Date(msg.time)
      const today = new Date()
      return msgDate.toDateString() === today.toDateString()
    }).length
  }
})

// 监听未读消息变化，触发动画
watch(
  unreadCounts,
  (newVal) => {
    animateValue('total', newVal.total)
    animateValue('system', newVal.system)
    animateValue('order', newVal.order)
    animateValue('comment', newVal.comment)
  },
  { deep: true }
)

// 更新筛选
const updateFilter = () => {
  filteredMessages.value = messages.value.filter((message) => {
    return activeCategory.value === 'all' || message.type === activeCategory.value
  })
  calculateUnreadCounts() // 更新未读消息统计
}

// 页面加载时初始化
onMounted(async () => {
  // 从后端API加载实际消息数据
  // 从JWT令牌中获取用户ID
  const authStore = useAuthStore()
  const token = authStore.token

  if (token) {
    const decodedToken = decodeJwt(token)
    if (decodedToken && decodedToken.userId) {
      userId.value = decodedToken.userId
    }
  } else {
    // 无法获取用户ID，弹出提示框要求重新登录
    ElMessageBox.alert('无法获取用户ID，请重新登录', '身份验证失败', {
      confirmButtonText: '重新登录',
      type: 'error',
      closeOnClickModal: false,
      closeOnPressEscape: false
    })
      .then(() => {
        // 用户点击重新登录按钮，清除本地存储并跳转到登录页面
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      })
      .catch(() => {
        // 点击取消按钮的处理，也可以跳转到登录页面
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      })
    return
  }

  // 加载消息列表
  await loadMessages()

  // 初始化 WebSocket 连接
  initWebSocket()
})

// 组件卸载时关闭 WebSocket
onUnmounted(() => {
  closeWebSocket()
})

// 查看消息详情
const viewMessageDetail = (message) => {
  // 检查是否是订单消息，如果是则导航到订单详情页
  if (message.type === 'order') {
    // 从消息标题或内容中提取订单号
    const orderIdMatch = message.title.match(/订单号(?:JD)?(\\d+)/)
    if (orderIdMatch) {
      const orderId = orderIdMatch[1]
      router.push(`/merchant/home/order-detail/${orderId}`)
      return
    }
  }
  // 普通消息则显示详情
  selectedMessage.value = message
  // 自动标记为已读
  if (!message.isRead) {
    message.isRead = true
    ElMessage.success('消息已标记为已读')
    updateFilter() // 刷新筛选后的列表以更新状态
  }
}

// 返回消息列表
const backToList = () => {
  selectedMessage.value = null
}

// ==================== 刷新消息 ====================

// 刷新消息列表（真实刷新）
const refreshMessages = async () => {
  await loadMessages()
  ElMessage.success('刷新成功')
}
</script>

<template>
  <div class="messages-management-container">
    <!-- 头部 -->
    <div class="messages-header">
      <div class="header-left">
        <h3 class="page-title">消息中心</h3>
        <p class="page-subtitle">管理您的所有通知和消息</p>
      </div>
      <div class="header-right" v-if="!selectedMessage">
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索消息内容..."
          :prefix-icon="Search"
          clearable
          class="search-input"
        />
        <CommonBackButton />
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section" v-if="!selectedMessage">
      <div class="stat-card total">
        <div class="stat-icon">
          <el-icon :size="28"><ChatDotRound /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ totalStats.total }}</div>
          <div class="stat-label">总消息</div>
        </div>
      </div>

      <div class="stat-card unread">
        <div class="stat-icon">
          <el-icon :size="28"><Bell /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.total }}</div>
          <div class="stat-label">未读消息</div>
        </div>
      </div>

      <div class="stat-card system">
        <div class="stat-icon">
          <el-icon :size="28"><Notification /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.system }}</div>
          <div class="stat-label">系统通知</div>
        </div>
      </div>

      <div class="stat-card order">
        <div class="stat-icon">
          <el-icon :size="28"><Bell /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.order }}</div>
          <div class="stat-label">订单消息</div>
        </div>
      </div>

      <div class="stat-card comment">
        <div class="stat-icon">
          <el-icon :size="28"><ChatLineSquare /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value animated-number">{{ animatedValues.comment }}</div>
          <div class="stat-label">评价消息</div>
        </div>
      </div>
    </div>

    <div class="messages-content">
      <!-- 消息分类与列表 -->
      <div class="messages-list-container" v-if="!selectedMessage">
        <!-- 分类筛选 -->
        <div class="category-section">
          <div class="filter-header">
            <el-icon class="filter-icon"><Filter /></el-icon>
            <span class="filter-label">消息分类</span>
          </div>
          <div class="category-tags">
            <div
              v-for="category in ['all', 'system', 'order', 'comment']"
              :key="category"
              :class="[
                'category-tag',
                `category-tag-${category}`,
                { active: activeCategory === category }
              ]"
              @click="
                () => {
                  activeCategory = category
                  updateFilter()
                }
              "
            >
              <el-icon class="tag-icon">
                <component :is="messageCategories[category].icon" />
              </el-icon>
              <span class="tag-text">{{ messageCategories[category].text }}</span>
              <el-badge
                v-if="category !== 'all' && unreadCounts[category] > 0"
                :value="unreadCounts[category]"
                type="danger"
                class="tag-badge"
              />
            </div>
          </div>

          <!-- 操作按钮组 -->
          <div class="action-buttons">
            <!-- 批量操作按钮 -->
            <el-button
              v-if="!selectMode"
              type="primary"
              @click="toggleSelectMode"
              :icon="Select"
              size="default"
            >
              批量管理
            </el-button>
            <template v-else>
              <el-button @click="toggleSelectAll" size="default">
                {{ selectedMessages.length === searchMessages.length ? '取消全选' : '全选' }}
              </el-button>
              <el-button
                type="success"
                @click="batchMarkAsRead"
                :icon="CircleCheck"
                :disabled="selectedMessages.length === 0"
                size="default"
              >
                批量已读
              </el-button>
              <el-button
                type="danger"
                @click="batchDeleteMessages"
                :icon="Delete"
                :disabled="selectedMessages.length === 0"
                size="default"
              >
                批量删除
              </el-button>
              <el-button @click="toggleSelectMode" size="default">退出</el-button>
            </template>

            <el-button
              type="success"
              @click="markAllAsRead"
              :icon="Check"
              :disabled="selectMode"
              size="default"
            >
              全部标记为已读
            </el-button>
            <el-button
              type="default"
              @click="refreshMessages"
              :loading="loading"
              :icon="Refresh"
              size="default"
            >
              刷新
            </el-button>
            <el-button
              type="default"
              @click="exportMessages"
              :icon="Download"
              :disabled="selectMode"
              size="default"
            >
              导出
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-list" v-loading="loading">
          <div
            v-for="message in searchMessages"
            :key="message.id"
            :class="['message-item', { 'unread-message': !message.isRead, 'selected': selectMode && selectedMessages.includes(message) }]"
            @click="selectMode ? toggleMessageSelection(message) : viewMessageDetail(message)"
          >
            <!-- 批量选择复选框 -->
            <div class="message-checkbox" v-if="selectMode">
              <el-checkbox
                :model-value="selectedMessages.includes(message)"
                @update:model-value="toggleMessageSelection(message)"
                @click.stop
              />
            </div>

            <div class="message-left">
              <div class="message-icon" :class="`icon-${message.type}`">
                <el-icon :size="20">
                  <component :is="messageCategories[message.type]?.icon || Notification" />
                </el-icon>
              </div>
              <div class="message-indicator" v-if="!message.isRead"></div>
            </div>

            <div class="message-content">
              <div class="message-title">{{ message.title }}</div>
              <div class="message-preview" v-if="message.content">
                {{ message.content.substring(0, 50) }}{{ message.content.length > 50 ? '...' : '' }}
              </div>
              <div class="message-meta">
                <span class="message-time">{{ message.time }}</span>
                <el-tag :type="message.isRead ? 'success' : 'warning'" size="small">
                  {{ message.isRead ? '已读' : '未读' }}
                </el-tag>
                <!-- 删除按钮（仅在非选择模式时显示） -->
                <el-button
                  v-if="!selectMode"
                  type="danger"
                  size="small"
                  :icon="Delete"
                  circle
                  class="delete-btn"
                  @click.stop="deleteMessage(message)"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 空数据提示 -->
        <div v-if="filteredMessages.length === 0 && !loading" class="empty-messages">
          <el-empty description="暂无消息"></el-empty>
        </div>
      </div>

      <!-- 消息详情 -->
      <div class="message-detail-container" v-if="selectedMessage">
        <div class="detail-header">
          <div class="detail-title">
            <div class="title-icon" :class="`icon-${selectedMessage.type}`">
              <el-icon :size="24">
                <component :is="messageCategories[selectedMessage.type]?.icon || Notification" />
              </el-icon>
            </div>
            <h3>{{ selectedMessage.title }}</h3>
            <el-tag :type="selectedMessage.isRead ? 'success' : 'warning'">
              {{ selectedMessage.isRead ? '已读' : '未读' }}
            </el-tag>
          </div>
          <div class="detail-meta">
            <div class="meta-item" v-if="selectedMessage.sender">
              <span class="meta-label">发送者:</span>
              <span class="meta-value">{{ selectedMessage.sender }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">时间:</span>
              <span class="meta-value">{{ selectedMessage.time }}</span>
            </div>
          </div>
        </div>
        <div class="detail-content">
          {{ selectedMessage.content }}
        </div>
        <div class="detail-actions">
          <CommonBackButton @click="backToList" :useRouterBack="false" text="返回列表" />
          <el-button
            v-if="!selectedMessage.isRead"
            type="success"
            @click="markAsRead(selectedMessage)"
          >
            标记为已读
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

.messages-management-container {
  padding: 0 20px 20px 20px;

  .messages-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px 28px;
    background: linear-gradient(135deg, @merchant-primary 0%, @merchant-primary-dark 100%);
    border-radius: 20px;
    box-shadow: 0 8px 24px @merchant-shadow-hover;
    margin-bottom: 24px;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -10%;
      width: 300px;
      height: 300px;
      background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
      border-radius: 50%;
    }

    .header-left {
      display: flex;
      flex-direction: column;
      gap: 6px;
      position: relative;
      z-index: 1;

      .page-title {
        font-size: 26px;
        font-weight: 700;
        margin: 0;
        color: @merchant-surface;
        letter-spacing: 0.8px;
        text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      }

      .page-subtitle {
        font-size: 1rem /* 原值: 14px */;
        color: rgba(255, 255, 255, 0.9);
        margin: 0;
        font-weight: 400;
      }
    }

    .header-right {
      display: flex;
      gap: 12px;
      align-items: center;
      position: relative;
      z-index: 1;
      flex-wrap: wrap;

      .search-input {
        width: 260px;

        :deep(.el-input__wrapper) {
          background: rgba(255, 255, 255, 0.9);
          border-radius: 8px;
          box-shadow: 0 2px 8px @merchant-shadow;
        }
      }

      :deep(.el-button) {
        backdrop-filter: blur(12px);
        background: rgba(255, 255, 255, 0.15);
        border: 1px solid rgba(255, 255, 255, 0.35);
        color: @merchant-surface;
        box-shadow: 0 4px 12px @merchant-shadow;
        font-weight: 500;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        &:hover {
          transform: translateY(-2px);
          background: rgba(255, 255, 255, 0.25);
          box-shadow: 0 6px 16px @merchant-shadow-hover;
        }

        &:active {
          transform: translateY(0);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }
    }
  }

  // 统计卡片
  .stats-section {
    display: flex;
    justify-content: space-between;
    align-items: stretch;
    padding: 20px;
    background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-bg 100%);
    border: 1px solid @merchant-border;
    border-radius: 20px;
    margin-bottom: 24px;
    flex-wrap: wrap;
    gap: 16px;
    box-shadow: 0 2px 16px @merchant-shadow;

    .stat-card {
      display: flex;
      align-items: center;
      gap: 18px;
      padding: 22px 24px;
      background: @merchant-surface;
      border-radius: 18px;
      box-shadow: 0 4px 16px @merchant-shadow;
      transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      flex: 1;
      min-width: 160px;
      border: 1px solid @merchant-border;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 5px;
        transition: width 0.3s ease;
      }

      &:hover {
        transform: translateY(-6px) scale(1.01);
        box-shadow: 0 12px 32px @merchant-shadow-hover;

        &::before {
          width: 6px;
        }

        .stat-icon {
          transform: scale(1.1) rotate(5deg);
        }
      }

      &:active {
        transform: translateY(-3px) scale(1.005);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        border-radius: 16px;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

        .el-icon {
          color: inherit;
        }
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 2rem /* 原值: 28px */;
          font-weight: 700;
          color: @merchant-text;
          line-height: 1.1;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 0.929rem /* 原值: 13px */;
          color: @merchant-text-muted;
          font-weight: 500;
          letter-spacing: 0.3px;
        }
      }

      &.total {
        &::before {
          background: linear-gradient(180deg, @merchant-primary 0%, @merchant-primary-dark 100%);
        }
        .stat-icon {
          background: @merchant-primary-light;
          color: @merchant-primary;
          box-shadow: 0 4px 12px rgba(74, 122, 77, 0.15);
        }
        .stat-value {
          background: linear-gradient(135deg, @merchant-primary 0%, @merchant-primary-dark 100%);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
          background-clip: text;
        }
      }

      &.unread {
        &::before {
          background: linear-gradient(180deg, @merchant-error 0%, darken(@merchant-error, 8%) 100%);
        }
        .stat-icon {
          background: @merchant-error-light;
          color: @merchant-error;
          box-shadow: 0 4px 12px rgba(196, 91, 91, 0.15);
        }
        .stat-value {
          color: @merchant-error;
        }
      }

      &.system {
        &::before {
          background: linear-gradient(180deg, @merchant-warning 0%, darken(@merchant-warning, 5%) 100%);
        }
        .stat-icon {
          background: @merchant-warning-light;
          color: @merchant-warning;
          box-shadow: 0 4px 12px rgba(212, 168, 85, 0.15);
        }
        .stat-value {
          color: @merchant-warning;
        }
      }

      &.order {
        &::before {
          background: linear-gradient(180deg, @merchant-info 0%, darken(@merchant-info, 8%) 100%);
        }
        .stat-icon {
          background: @merchant-info-light;
          color: @merchant-info;
          box-shadow: 0 4px 12px rgba(91, 139, 210, 0.15);
        }
        .stat-value {
          color: @merchant-info;
        }
      }

      &.comment {
        &::before {
          background: linear-gradient(180deg, @merchant-success 0%, darken(@merchant-success, 8%) 100%);
        }
        .stat-icon {
          background: @merchant-success-light;
          color: @merchant-success;
          box-shadow: 0 4px 12px rgba(90, 143, 94, 0.15);
        }
        .stat-value {
          color: @merchant-success;
        }
      }
    }
  }

  .messages-content {
    .messages-list-container {
      .category-section {
        display: flex;
        flex-direction: column;
        gap: 12px;
        padding: 16px 20px;
        background: linear-gradient(135deg, @merchant-surface-alt 0%, @merchant-surface 100%);
        border-radius: 12px;
        margin-bottom: 16px;
        box-shadow: 0 2px 12px @merchant-shadow;
        border: 1px solid @merchant-border;

        .filter-header {
          display: flex;
          align-items: center;
          gap: 6px;

          .filter-icon {
            font-size: 1.143rem /* 原值: 16px */;
            color: @merchant-primary;
          }

          .filter-label {
            font-size: 0.929rem /* 原值: 13px */;
            font-weight: 600;
            color: @merchant-text;
          }
        }

        .category-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;

          .category-tag {
            cursor: pointer;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            padding: 6px 12px;
            font-size: 0.929rem /* 原值: 13px */;
            font-weight: 500;
            border-radius: 8px;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            user-select: none;
            position: relative;

            .tag-icon {
              font-size: 1rem /* 原值: 14px */;
            }

            .tag-text {
              font-size: 0.929rem /* 原值: 13px */;
            }

            .tag-badge {
              margin-left: 4px;
            }

            &:hover {
              transform: translateY(-1px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
            }

            &.category-tag-all {
              background: @merchant-surface-alt;
              color: @merchant-text-sec;
              border: 1px solid @merchant-border;
              &.active {
                background: linear-gradient(135deg, @merchant-primary 0%, @merchant-primary-dark 100%);
                color: @merchant-surface;
                border-color: @merchant-primary;
                box-shadow: 0 2px 6px rgba(74, 122, 77, 0.3);
              }
            }

            &.category-tag-system {
              background: @merchant-warning-light;
              color: @merchant-warning;
              border: 1px solid @merchant-warning-light;
              &.active {
                background: linear-gradient(135deg, @merchant-warning 0%, darken(@merchant-warning, 5%) 100%);
                color: @merchant-surface;
              }
            }

            &.category-tag-order {
              background: @merchant-info-light;
              color: @merchant-info;
              border: 1px solid @merchant-info-light;
              &.active {
                background: linear-gradient(135deg, @merchant-info 0%, lighten(@merchant-info, 8%) 100%);
                color: @merchant-surface;
              }
            }

            &.category-tag-comment {
              background: @merchant-success-light;
              color: @merchant-success;
              border: 1px solid @merchant-success-light;
              &.active {
                background: linear-gradient(135deg, @merchant-success 0%, lighten(@merchant-success, 8%) 100%);
                color: @merchant-surface;
              }
            }
          }
        }

        .action-buttons {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          align-items: center;
          margin-top: 12px;
          padding-top: 12px;
          border-top: 1px solid @merchant-divider;

          :deep(.el-button) {
            height: 32px;
            padding: 0 16px;
            font-size: 0.929rem;

            &:hover {
              transform: translateY(-1px);
            }

            &:active {
              transform: translateY(0);
            }

            &:disabled {
              opacity: 0.5;
              cursor: not-allowed;
            }
          }
        }
      }

      .messages-list {
        .message-item {
          display: flex;
          align-items: flex-start;
          padding: 18px 22px;
          border: 2px solid @merchant-border;
          border-radius: 14px;
          margin-bottom: 12px;
          background-color: @merchant-surface;
          cursor: pointer;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
          position: relative;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 0;
            bottom: 0;
            width: 4px;
            background: @merchant-border;
            transition: all 0.3s ease;
            border-radius: 14px 0 0 14px;
          }

          &:hover {
            box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
            border-color: @merchant-border;
            transform: translateY(-3px);

            .message-icon {
              transform: scale(1.08);
            }

            .message-title {
              color: @merchant-primary;
            }
          }

          &:active {
            transform: translateY(-1px);
          }

          &.unread-message {
            background: linear-gradient(to right, @merchant-error-light 0%, @merchant-surface 35%);
            border-color: @merchant-error-light;

            &::before {
              background: linear-gradient(180deg, @merchant-error 0%, lighten(@merchant-error, 10%) 100%);
              box-shadow: 0 0 12px rgba(245, 108, 108, 0.4);
            }

            .message-title {
              color: @merchant-text;
              font-weight: 700;
            }

            .message-preview {
              color: @merchant-text-sec;
            }
          }

          &.selected {
            background: linear-gradient(to right, @merchant-info-light 0%, @merchant-surface 35%);
            border-color: @merchant-info-light;
          }

          .message-checkbox {
            margin-right: 12px;
            display: flex;
            align-items: center;

            :deep(.el-checkbox__inner) {
              border-radius: 6px;
            }
          }

          .message-left {
            position: relative;
            margin-right: 18px;

            .message-icon {
              width: 48px;
              height: 48px;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: 14px;
              transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

              &.icon-system {
                background: linear-gradient(
                  135deg,
                  rgba(230, 162, 60, 0.12) 0%,
                  rgba(230, 162, 60, 0.06) 100%
                );
                color: @merchant-warning;
                box-shadow: 0 4px 12px rgba(230, 162, 60, 0.12);
              }

              &.icon-order {
                background: linear-gradient(
                  135deg,
                  rgba(91, 139, 210, 0.12) 0%,
                  rgba(91, 139, 210, 0.06) 100%
                );
                color: @merchant-info;
                box-shadow: 0 4px 12px rgba(91, 139, 210, 0.12);
              }

              &.icon-comment {
                background: linear-gradient(
                  135deg,
                  rgba(90, 143, 94, 0.12) 0%,
                  rgba(90, 143, 94, 0.06) 100%
                );
                color: @merchant-success;
                box-shadow: 0 4px 12px rgba(90, 143, 94, 0.12);
              }
            }

            .message-indicator {
              position: absolute;
              top: -3px;
              right: -3px;
              width: 12px;
              height: 12px;
              background: linear-gradient(135deg, @merchant-error 0%, lighten(@merchant-error, 10%) 100%);
              border: 2.5px solid @merchant-surface;
              border-radius: 50%;
              box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
              animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
            }
          }

          .message-content {
            flex: 1;

            .message-title {
              font-size: 1.071rem /* 原值: 15px */;
              font-weight: 600;
              margin-bottom: 7px;
              color: @merchant-text;
              transition: color 0.2s ease;
            }

            .message-preview {
              font-size: 0.929rem /* 原值: 13px */;
              color: @merchant-text-sec;
              margin-bottom: 10px;
              line-height: 1.6;
            }

            .message-meta {
              display: flex;
              justify-content: space-between;
              align-items: center;
              font-size: 0.857rem /* 原值: 12px */;
              color: @merchant-text-muted;
              gap: 8px;

              .delete-btn {
                opacity: 0;
                transition: opacity 0.2s ease;

                &:hover {
                  transform: scale(1.1);
                }
              }
            }

            &:hover .delete-btn {
              opacity: 1;
            }
          }
        }
      }

      .empty-messages {
        text-align: center;
        margin-top: 50px;
      }
    }

    .message-detail-container {
      padding: 0;

      .detail-header {
        margin-bottom: 20px;
        padding: 20px;
        background: @merchant-surface;
        border-radius: 12px;
        border: 1px solid @merchant-border;

        .detail-title {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 15px;

          .title-icon {
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 10px;

            &.icon-system {
              background: linear-gradient(
                135deg,
                rgba(230, 162, 60, 0.15) 0%,
                rgba(230, 162, 60, 0.08) 100%
              );
              color: @merchant-warning;
            }

            &.icon-order {
              background: linear-gradient(
                135deg,
                rgba(91, 139, 210, 0.15) 0%,
                rgba(91, 139, 210, 0.08) 100%
              );
              color: @merchant-info;
            }

            &.icon-comment {
              background: linear-gradient(
                135deg,
                rgba(90, 143, 94, 0.15) 0%,
                rgba(90, 143, 94, 0.08) 100%
              );
              color: @merchant-success;
            }
          }

          h3 {
            font-size: 1.429rem /* 原值: 20px */;
            margin: 0;
            flex: 1;
          }
        }

        .detail-meta {
          display: flex;
          flex-direction: column;
          gap: 8px;
          font-size: 1rem /* 原值: 14px */;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 8px;

            .meta-label {
              color: @merchant-text-muted;
              font-weight: 500;
              min-width: 60px;
            }

            .meta-value {
              color: @merchant-text;
            }
          }
        }
      }

      .detail-content {
        font-size: 1.143rem /* 原值: 16px */;
        line-height: 1.8;
        margin-bottom: 30px;
        padding: 24px;
        background: @merchant-surface;
        border-radius: 12px;
        border: 1px solid @merchant-border;
        color: @merchant-text;
      }

      .detail-actions {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
      }
    }
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .messages-management-container {
    padding: 12px;

    .messages-header {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;
    }

    .stats-section {
      gap: 10px;

      .stat-card {
        min-width: calc(50% - 5px);
        padding: 16px;

        .stat-value {
          font-size: 1.429rem /* 原值: 20px */ !important;
        }
      }
    }

    .messages-content {
      .messages-list-container {
        .category-section {
          padding: 12px;

          .category-tags {
            .category-tag {
              padding: 4px 10px;
              font-size: 0.857rem /* 原值: 12px */;
            }
          }
        }

        .messages-list {
          .message-item {
            padding: 14px;

            .message-left {
              .message-icon {
                width: 38px;
                height: 38px;
              }
            }

            .message-content {
              .message-title {
                font-size: 1rem /* 原值: 14px */;
              }

              .message-preview {
                font-size: 0.857rem /* 原值: 12px */;
              }
            }
          }
        }
      }
    }
  }
}
</style>
