<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/authStore'
import api, { decodeJwt } from '../../utils/api.js'
import { WS_CONFIG } from '../../config/index.js'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppStatCard from '../../components/common/AppStatCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import {
  Bell,
  ChatDotRound,
  Notification,
  ChatLineSquare,
  Refresh,
  Check,
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

const ensureApiSuccess = (response, defaultMessage) => {
  if (response?.success === false) {
    throw new Error(response.message || defaultMessage)
  }
}

const normalizeMessage = (message) => {
  const content = typeof message?.content === 'string' ? message.content : ''

  return {
    id: message?.id,
    title: content || '未命名消息',
    content,
    sender: message?.senderName || message?.senderId || '系统',
    time: message?.createdAt || message?.sendTime || '',
    isRead: message?.readStatus === 1 || message?.readStatus === true,
    type: determineMessageType(message)
  }
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
            time:
              data.createdAt ||
              data.time ||
              new Date().toISOString().slice(0, 19).replace('T', ' '),
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
    const response = await api.get('/v1/message/records', {
      params: {
        userId: userId.value,
        pageNum: 1,
        pageSize: 50
      }
    })

    ensureApiSuccess(response, '加载消息失败')

    const messageList = Array.isArray(response?.data?.records)
      ? response.data.records
      : Array.isArray(response?.data)
        ? response.data
        : []

    messages.value = messageList.map(normalizeMessage)
    updateFilter()
    return true
  } catch (error) {
    console.error('加载消息失败:', error)
    ElMessage.error('加载消息失败，请稍后重试')
    return false
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
    const response = await api.put(`/v1/message/records/${message.id}/read`)
    ensureApiSuccess(response, '标记已读失败')

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

    const response = await api.put('/v1/message/records/all-read', null, {
      params: { userId: userId.value }
    })
    ensureApiSuccess(response, '全部标记已读失败')

    filteredMessages.value.forEach((message) => {
      message.isRead = true
    })
    await loadMessages()
    ElMessage.success('所有消息已标记为已读')
  } catch (error) {
    console.error('全部标记已读失败:', error)
    ElMessage.error('全部标记已读失败，请稍后重试')
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
    const response = await api.delete(`/v1/message/records/${message.id}`)
    ensureApiSuccess(response, '删除消息失败')

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

    const messageIds = selectedMessages.value.map((msg) => msg.id)
    const response = await api.delete('/v1/message/records/batch', {
      data: messageIds
    })
    ensureApiSuccess(response, '批量删除失败')

    // 从本地列表中移除
    messages.value = messages.value.filter((msg) => !messageIds.includes(msg.id))

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

    const response = await api.put(
      '/v1/message/records/read/batch',
      unreadSelected.map((msg) => msg.id)
    )
    ensureApiSuccess(response, '批量标记已读失败')

    unreadSelected.forEach((msg) => {
      msg.isRead = true
    })
    await loadMessages()

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

// 0 条时收起统计与批量操作（评审 P0）：只在有消息时渲染
const hasMessages = computed(() => filteredMessages.value.length > 0)

const hasUnreadMessage = computed(() => filteredMessages.value.some((message) => !message.isRead))

// 次级统计条：品牌色主指标之外的 4 个中性指标（§4.8）
const secondaryStats = computed(() => [
  { label: '未读消息', value: unreadCounts.value.total },
  { label: '系统通知', value: unreadCounts.value.system },
  { label: '订单消息', value: unreadCounts.value.order },
  { label: '评价消息', value: unreadCounts.value.comment }
])

const calculateUnreadCounts = () => {
  const nextCounts = {
    system: 0,
    order: 0,
    comment: 0,
    total: 0
  }

  messages.value.forEach((message) => {
    if (message.isRead) {
      return
    }

    nextCounts.total++
    if (Object.prototype.hasOwnProperty.call(nextCounts, message.type)) {
      nextCounts[message.type]++
    }
  })

  unreadCounts.value = nextCounts
}

// 切换消息分类
const selectCategory = (category) => {
  activeCategory.value = category
  updateFilter()
}

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
  // 优先使用商家ID，降级时再使用JWT中的用户ID
  const authStore = useAuthStore()
  if (authStore.merchantId || localStorage.getItem('auth_merchantId')) {
    userId.value = authStore.merchantId || localStorage.getItem('auth_merchantId') || ''
  } else if (authStore.token) {
    const token = authStore.token
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
  const success = await loadMessages()
  if (success) {
    ElMessage.success('刷新成功')
  }
}
</script>

<template>
  <div class="messages-management-container">
    <AppPageHeader title="消息中心" subtitle="管理您的所有通知和消息" compact>
      <template #actions>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <div class="messages-content">
      <!-- 消息列表 -->
      <div class="messages-list-container" v-if="!selectedMessage">
        <!-- 概览：有数据时才显示（§4.8；0 条收起统计，评审 P0） -->
        <section v-if="hasMessages" class="stats-section">
          <AppStatCard class="stats-overview__primary" label="总消息" :value="totalStats.total" />
          <div class="stats-overview__secondary">
            <div v-for="stat in secondaryStats" :key="stat.label" class="stats-secondary">
              <span class="stats-secondary__value">{{ stat.value }}</span>
              <span class="stats-secondary__label">{{ stat.label }}</span>
            </div>
          </div>
        </section>

        <!-- 分类筛选 + 列表工具条（有数据时才显示批量操作） -->
        <div v-if="hasMessages" class="category-section">
          <div class="category-tags">
            <div
              v-for="category in ['all', 'system', 'order', 'comment']"
              :key="category"
              :class="['category-tag', { active: activeCategory === category }]"
              @click="selectCategory(category)"
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

          <div class="category-toolbar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索消息内容..."
              :prefix-icon="Search"
              clearable
              size="small"
              class="search-input"
            />

            <div class="action-buttons">
              <template v-if="selectMode">
                <span class="selected-count">已选 {{ selectedMessages.length }} 条</span>
                <el-button size="small" @click="toggleSelectAll">
                  {{ selectedMessages.length === searchMessages.length ? '取消全选' : '全选' }}
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  @click="batchMarkAsRead"
                  :icon="CircleCheck"
                  :disabled="selectedMessages.length === 0"
                >
                  批量已读
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="batchDeleteMessages"
                  :icon="Delete"
                  :disabled="selectedMessages.length === 0"
                >
                  批量删除
                </el-button>
                <el-button size="small" @click="toggleSelectMode">退出</el-button>
              </template>

              <template v-else>
                <el-button
                  size="small"
                  @click="toggleSelectMode"
                  :icon="Select"
                  :disabled="!hasMessages"
                >
                  批量管理
                </el-button>
                <el-button
                  size="small"
                  @click="markAllAsRead"
                  :icon="Check"
                  :disabled="!hasUnreadMessage"
                >
                  全部标记为已读
                </el-button>
                <el-button size="small" @click="refreshMessages" :loading="loading" :icon="Refresh">
                  刷新
                </el-button>
                <el-button size="small" @click="exportMessages" :icon="Download">导出</el-button>
              </template>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="messages-list" v-loading="loading">
          <div
            v-for="message in searchMessages"
            :key="message.id"
            :class="[
              'message-item',
              {
                'unread-message': !message.isRead,
                selected: selectMode && selectedMessages.includes(message)
              }
            ]"
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
                <el-tag :type="message.isRead ? 'info' : 'warning'" size="small">
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

        <!-- 空数据提示：0 条时上移到筛选区位置，并收起统计与批量操作（评审 P0） -->
        <AppEmptyState
          v-if="filteredMessages.length === 0 && !loading"
          class="empty-messages"
          description="暂无消息"
          action-text="刷新"
          compact
          @action="refreshMessages"
        />
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
            <el-tag :type="selectedMessage.isRead ? 'info' : 'warning'">
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
            type="primary"
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
  --card-highlight: inset 0 1px 0 rgba(255, 255, 255, 0.6);

  box-sizing: border-box;
  padding: 0 var(--space-6) var(--space-6);

  // 概览：主指标 + 4 个次级条（§4.8），无彩色渐变（§6.2）
  .stats-section {
    margin-bottom: var(--space-4);
  }

  .stats-overview__primary {
    max-width: 320px;
  }

  .stats-overview__primary :deep(.app-stat-card__value) {
    font-size: var(--text-h1);
  }

  .stats-overview__secondary {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: var(--card-gap-compact);
    margin-top: var(--card-gap-compact);
  }

  .stats-secondary {
    display: flex;
    flex-direction: column;
    gap: var(--space-1);
    padding: var(--card-padding-compact);
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    box-shadow: var(--card-highlight);
  }

  .stats-secondary__value {
    color: var(--ink-900);
    font-family: var(--font-num);
    font-size: var(--text-h2);
    font-weight: var(--weight-semibold);
    line-height: 1.2;
    font-variant-numeric: tabular-nums;
  }

  .stats-secondary__label {
    color: var(--ink-500);
    font-size: var(--text-body-sm);
  }

  // 分类与列表工具条：紧凑卡片（§4.2 L1）
  .category-section {
    display: flex;
    flex-direction: column;
    gap: var(--space-4);
    padding: var(--card-padding-compact);
    margin-bottom: var(--card-gap-compact);
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    box-shadow: var(--card-highlight);
  }

  .category-tags {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-2);
  }

  .category-tag {
    display: inline-flex;
    align-items: center;
    gap: var(--space-2);
    padding: var(--space-2) var(--space-3);
    color: var(--ink-700);
    font-size: var(--text-body-sm);
    font-weight: var(--weight-medium);
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-md);
    cursor: pointer;
    user-select: none;
    transition:
      color var(--duration-fast) var(--ease-standard),
      background-color var(--duration-fast) var(--ease-standard),
      border-color var(--duration-fast) var(--ease-standard);

    .tag-icon {
      font-size: var(--text-body);
    }

    .tag-badge {
      margin-left: var(--space-1);
    }

    &:hover {
      color: var(--brand-500);
      border-color: var(--brand-300);
    }

    &.active {
      color: var(--brand-600);
      background: var(--brand-50);
      border-color: var(--brand-500);
      font-weight: var(--weight-semibold);
    }
  }

  .category-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: var(--space-3);

    .search-input {
      width: 240px;
    }

    .search-input :deep(.el-input__wrapper) {
      border-radius: var(--radius-sm);
    }

    .action-buttons {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: var(--space-2);
    }

    .selected-count {
      color: var(--ink-500);
      font-size: var(--text-body-sm);
    }
  }

  // 0 条空状态：居中在筛选区位置（§4.5）
  .empty-messages {
    max-width: 480px;
    margin: var(--space-8) auto 0;
  }

  .messages-content {
    .messages-list-container {
      .messages-list {
        .message-item {
          display: flex;
          align-items: flex-start;
          padding: var(--space-4);
          border: 1px solid var(--line);
          border-radius: var(--radius-lg);
          margin-bottom: var(--card-gap-compact);
          background: var(--surface);
          box-shadow: var(--card-highlight);
          cursor: pointer;
          transition:
            border-color var(--duration-fast) var(--ease-standard),
            background-color var(--duration-fast) var(--ease-standard);
          position: relative;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            left: 0;
            top: 0;
            bottom: 0;
            width: 3px;
            background: var(--line);
            transition: background-color var(--duration-fast) var(--ease-standard);
            border-radius: var(--radius-lg) 0 0 var(--radius-lg);
          }

          &:hover:hover {
            border-color: var(--brand-300);

            .message-title {
              color: var(--brand-500);
            }
          }

          &.unread-message {
            background: var(--surface);
            border-color: var(--line);

            &::before {
              background: var(--danger);
            }

            .message-title {
              color: var(--ink-900);
              font-weight: 700;
            }

            .message-preview {
              color: var(--ink-500);
            }
          }

          &.selected {
            background: var(--info-bg);
            border-color: var(--info);
          }

          .message-checkbox {
            margin-right: 12px;
            display: flex;
            align-items: center;

            :deep(.el-checkbox__inner) {
              border-radius: var(--radius-sm);
            }
          }

          .message-left {
            position: relative;
            margin-right: 18px;

            .message-icon {
              width: 40px;
              height: 40px;
              display: flex;
              align-items: center;
              justify-content: center;
              border-radius: var(--radius-md);

              &.icon-system {
                background: var(--warning-bg);
                color: var(--warning);
              }

              &.icon-order {
                background: var(--info-bg);
                color: var(--info);
              }

              &.icon-comment {
                background: var(--success-bg);
                color: var(--success);
              }
            }

            .message-indicator {
              position: absolute;
              top: -3px;
              right: -3px;
              width: 12px;
              height: 12px;
              background: var(--danger);
              border: 2px solid var(--surface);
              border-radius: var(--radius-full);
            }
          }

          .message-content {
            flex: 1;

            .message-title {
              font-size: var(--text-body);
              font-weight: var(--weight-semibold);
              margin-bottom: var(--space-2);
              color: var(--ink-900);
              transition: color var(--duration-fast) var(--ease-standard);
            }

            .message-preview {
              font-size: var(--text-body-sm);
              color: var(--ink-500);
              margin-bottom: var(--space-2);
              line-height: 1.6;
            }

            .message-meta {
              display: flex;
              justify-content: space-between;
              align-items: center;
              font-size: var(--text-caption);
              color: var(--ink-400);
              gap: var(--space-2);

              .delete-btn {
                opacity: 0;
                transition: opacity var(--duration-fast) var(--ease-standard);

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
    }

    .message-detail-container {
      padding: 0;

      .detail-header {
        margin-bottom: var(--card-gap);
        padding: var(--card-padding-compact);
        background: var(--surface);
        border-radius: var(--radius-lg);
        border: 1px solid var(--line);

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
            border-radius: var(--radius-md);

            &.icon-system {
              background: var(--warning-bg);
              color: var(--warning);
            }

            &.icon-order {
              background: var(--info-bg);
              color: var(--info);
            }

            &.icon-comment {
              background: var(--success-bg);
              color: var(--success);
            }
          }

          h3 {
            font-size: var(--text-h2);
            margin: 0;
            flex: 1;
          }
        }

        .detail-meta {
          display: flex;
          flex-direction: column;
          gap: var(--space-2);
          font-size: var(--text-body);

          .meta-item {
            display: flex;
            align-items: center;
            gap: 8px;

            .meta-label {
              color: var(--ink-400);
              font-weight: 500;
              min-width: 60px;
            }

            .meta-value {
              color: var(--ink-900);
            }
          }
        }
      }

      .detail-content {
        font-size: var(--text-body);
        line-height: 1.8;
        margin-bottom: var(--space-6);
        padding: var(--card-padding-compact);
        background: var(--surface);
        border-radius: var(--radius-lg);
        border: 1px solid var(--line);
        color: var(--ink-900);
      }

      .detail-actions {
        display: flex;
        justify-content: flex-end;
        gap: var(--space-3);
      }
    }
  }
}


// 响应式设计
@media (max-width: 768px) {
  .messages-management-container {
    padding: 0 var(--space-4) var(--space-4);

    .stats-overview__primary {
      max-width: none;
    }

    .stats-overview__secondary {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .category-toolbar {
      flex-direction: column;
      align-items: stretch;

      .search-input {
        width: 100%;
      }
    }

    .messages-list {
      .message-item {
        padding: 14px;
      }
    }
  }
}
</style>
