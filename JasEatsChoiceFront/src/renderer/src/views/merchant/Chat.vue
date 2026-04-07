<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Picture } from '@element-plus/icons-vue'
import router from '../../router'
import { useAuthStore } from '../../store/authStore'
import ConversationList from '../../components/merchant/chat/ConversationList.vue'
import ChatMessageList from '../../components/merchant/chat/ChatMessageList.vue'
import MessageInput from '../../components/merchant/chat/MessageInput.vue'
import {
  getCurrentUserId,
  getChatSessions,
  getChatMessages,
  sendMessage as sendChatMessage,
  markMessagesAsRead,
  buildSessionId,
  formatMessageForSend,
  createLocalMessage,
  handleApiError
} from '../../utils/chat/chatApi'
import {
  formatConversationTime,
  cleanMessage,
  sortConversationsByUnread
} from '../../utils/chat/messageUtils'

// ==================== 数据定义 ====================

// 合并的会话列表（包含单聊和群聊）
const conversations = ref([])

// 聊天记录
const chatMessages = ref([])

// 当前选中的会话
const selectedConversation = ref(null)

// 搜索关键词
const searchKeyword = ref('')

// 仅显示未读
const showUnreadOnly = ref(false)

// 同步至群聊开关
const syncToGroup = ref(false)

// 发送中状态
const sending = ref(false)

// 当前用户ID
const currentUserId = ref('')

// 模拟数据开关
const USE_MOCK_DATA = false

// 会话列表宽度
const conversationListWidth = ref(250)

// 拖拽相关状态
const isResizing = ref(false)

// ==================== 计算属性 ====================

// 会话类型
const conversationType = computed(() => {
  return selectedConversation.value?.type || 'private'
})

// 筛选后的会话列表
const filteredConversations = computed(() => {
  let result = conversations.value

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(
      (conv) =>
        conv.name.toLowerCase().includes(keyword) ||
        conv.lastMessage.toLowerCase().includes(keyword)
    )
  }

  // 仅显示未读
  if (showUnreadOnly.value) {
    result = result.filter((conv) => conv.unreadCount > 0)
  }

  // 排序
  return sortConversationsByUnread(result)
})

// ==================== 模拟数据 ====================

// 模拟会话列表
const getMockConversations = () => {
  const now = new Date()
  return [
    {
      id: 'user001',
      type: 'private',
      name: '张三',
      avatar: '👨',
      lastMessage: '请问今天的套餐还有吗？',
      time: new Date(now - 5 * 60000).toISOString().slice(0, 19).replace('T', ' '),
      unreadCount: 2,
      relatedOrder: true
    },
    {
      id: 'user002',
      type: 'private',
      name: '李四',
      avatar: '👩',
      lastMessage: '好的，我马上下单',
      time: new Date(now - 30 * 60000).toISOString().slice(0, 19).replace('T', ' '),
      unreadCount: 0
    },
    {
      id: 'user003',
      type: 'private',
      name: '王五',
      avatar: '👴',
      lastMessage: '什么时候能送到？',
      time: new Date(now - 2 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
      unreadCount: 1,
      relatedOrder: true
    },
    {
      id: 'group001',
      type: 'group',
      name: '今日订单群',
      avatar: '👥',
      lastMessage: '【订单同步】张三：请问今天的套餐还有吗？',
      time: new Date(now - 10 * 60000).toISOString().slice(0, 19).replace('T', ' '),
      unreadCount: 5,
      memberCount: 32
    },
    {
      id: 'group002',
      type: 'group',
      name: 'VIP客户群',
      avatar: '👥',
      lastMessage: '李四：好的，我马上下单',
      time: new Date(now - 1 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
      unreadCount: 0,
      memberCount: 18
    },
    {
      id: 'user004',
      type: 'private',
      name: '赵六',
      avatar: '👵',
      lastMessage: '谢谢，收到订单了',
      time: new Date(now - 4 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
      unreadCount: 0
    },
    {
      id: 'user005',
      type: 'private',
      name: '小明',
      avatar: '👦',
      lastMessage: '能不能加个辣？',
      time: new Date(now - 1 * 86400000).toISOString().slice(0, 19).replace('T', ' '),
      unreadCount: 3
    }
  ]
}

// 模拟聊天记录
const getMockMessages = (conversationId) => {
  const now = new Date()
  const messagesMap = {
    user001: [
      {
        id: 1,
        sender: 'customer',
        content: '你好，在吗？',
        time: new Date(now - 30 * 60000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 2,
        sender: 'merchant',
        content: '在的，请问有什么需要帮助的？',
        time: new Date(now - 25 * 60000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 3,
        sender: 'customer',
        content: '请问今天的套餐还有吗？',
        time: new Date(now - 5 * 60000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: false
      }
    ],
    user002: [
      {
        id: 1,
        sender: 'merchant',
        content: '您好，感谢关注我们的店铺！',
        time: new Date(now - 1 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 2,
        sender: 'customer',
        content: '好的，我马上下单',
        time: new Date(now - 30 * 60000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      }
    ],
    user003: [
      {
        id: 1,
        sender: 'customer',
        content: '我下单了',
        time: new Date(now - 3 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 2,
        sender: 'merchant',
        content: '好的，收到您的订单了',
        time: new Date(now - 2.5 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 3,
        sender: 'customer',
        content: '什么时候能送到？',
        time: new Date(now - 2 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: false
      }
    ],
    group001: [
      {
        id: 1,
        sender: '张三',
        content: '大家好，今天有什么推荐？',
        time: new Date(now - 1 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 2,
        sender: '我',
        content: '今天有红烧肉套餐和鱼香肉丝套餐',
        time: new Date(now - 55 * 60000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 3,
        sender: '李四',
        content: '我要一份红烧肉套餐',
        time: new Date(now - 50 * 60000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 4,
        sender: '我',
        content: '收到，马上为您准备',
        time: new Date(now - 45 * 60000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      }
    ],
    group002: [
      {
        id: 1,
        sender: '王总',
        content: '明天的团餐准备好了吗？',
        time: new Date(now - 2 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      },
      {
        id: 2,
        sender: '我',
        content: '正在准备中，明天早上准时送到',
        time: new Date(now - 1.5 * 3600000).toISOString().slice(0, 19).replace('T', ' '),
        isRead: true
      }
    ]
  }
  return messagesMap[conversationId] || []
}

// ==================== 初始化 ====================

// 页面加载
onMounted(async () => {
  // 使用模拟数据时跳过用户ID验证
  if (USE_MOCK_DATA) {
    // 加载模拟会话列表
    await loadSessions()
    return
  }

  // 获取当前用户ID
  currentUserId.value = getCurrentUserId()

  // 验证用户ID
  if (!currentUserId.value || currentUserId.value === '1') {
    // 无法获取用户ID，弹出提示框要求重新登录
    ElMessageBox.alert('无法获取用户ID，请重新登录', '身份验证失败', {
      confirmButtonText: '重新登录',
      type: 'error',
      closeOnClickModal: false,
      closeOnPressEscape: false
    })
      .then(() => {
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      })
      .catch(() => {
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
      })
    return
  }

  // 加载会话列表
  await loadSessions()
})

// ==================== API调用 ====================

// 加载会话列表
const loadSessions = async () => {
  try {
    let sessions

    if (USE_MOCK_DATA) {
      // 使用模拟数据
      sessions = getMockConversations()
      currentUserId.value = 'merchant001'
    } else {
      // 从 API 获取数据
      sessions = await getChatSessions(currentUserId.value)
    }

    if (sessions.length > 0) {
      conversations.value = sessions

      // 默认选中第一个会话
      selectedConversation.value = sessions[0]

      // 加载聊天记录
      await loadMessages(selectedConversation.value)
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 加载聊天记录
const loadMessages = async (conversation) => {
  try {
    let messages

    if (USE_MOCK_DATA) {
      // 使用模拟数据
      messages = getMockMessages(conversation.id)
    } else {
      // ⭐ 直接使用后端返回的 sessionId（conversation.id）
      // ⚠️ 不再使用 buildSessionId，因为后端已经统一生成了sessionId
      const sessionId = conversation.id
      messages = await getChatMessages(sessionId, currentUserId.value)
    }

    chatMessages.value = messages
  } catch (error) {
    console.error('加载聊天记录失败:', error)
    chatMessages.value = []
  }
}

// ==================== 事件处理 ====================

// 选择会话
const selectConversation = async (conversation) => {
  selectedConversation.value = conversation

  // 标记已读
  if (conversation.unreadCount > 0) {
    conversation.unreadCount = 0

    // 仅在非模拟数据模式下显示提示和调用API
    if (!USE_MOCK_DATA) {
      ElMessage.success('消息已标记为已读')

      // ⭐ 直接使用后端返回的 sessionId（conversation.id）
      // ⚠️ 不再使用 buildSessionId，因为后端已经统一生成了sessionId
      const sessionId = conversation.id
      await markMessagesAsRead(sessionId, currentUserId.value) // ⭐ 需要传入 userId
    }
  }

  // 加载聊天记录
  await loadMessages(conversation)
}

// 发送消息
const sendMessage = async (content) => {
  if (!content.trim() || !selectedConversation.value) {
    return
  }

  // 设置发送状态
  sending.value = true

  try {
    // 清理消息内容
    const cleanedContent = cleanMessage(content)

    // 构建消息数据
    const messageData = formatMessageForSend(
      cleanedContent,
      currentUserId.value,
      selectedConversation.value.id,
      selectedConversation.value.type
    )

    // 发送到后端
    const result = await sendChatMessage(messageData)

    if (result.success) {
      // 创建本地消息对象
      const localMessage = createLocalMessage(cleanedContent, selectedConversation.value.type)

      // 添加到聊天记录
      chatMessages.value.push(localMessage)

      // 更新会话列表的最后一条消息
      selectedConversation.value.lastMessage = cleanedContent
      selectedConversation.value.time = localMessage.time

      // 将当前会话移到最前面
      const index = conversations.value.indexOf(selectedConversation.value)
      if (index > -1) {
        conversations.value.splice(index, 1)
        conversations.value.unshift(selectedConversation.value)
      }

      // 同步消息到所有群聊
      if (syncToGroup.value && selectedConversation.value.type === 'private') {
        const syncMessageContent = `【订单同步】${cleanedContent}`

        // 更新所有群聊的最后消息
        conversations.value.forEach((conversation) => {
          if (conversation.type === 'group') {
            conversation.lastMessage = syncMessageContent
            conversation.time = localMessage.time
            conversation.unreadCount++

            // 将群聊会话移到前面
            const groupIndex = conversations.value.indexOf(conversation)
            if (groupIndex > -1) {
              conversations.value.splice(groupIndex, 1)
              conversations.value.unshift(conversation)
            }
          }
        })

        // 重置同步开关
        syncToGroup.value = false

        // 提示用户消息已同步
        ElMessage.info('消息已同步至所有群聊')
      }

      ElMessage.success('消息发送成功')
    } else {
      ElMessage.error(result.message || '发送消息失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    handleApiError(error, '发送消息失败，请稍后重试')
  } finally {
    // 清除发送状态
    sending.value = false
  }
}

// 搜索会话
const handleSearch = (keyword) => {
  searchKeyword.value = keyword
}

// 切换仅显示未读
const toggleUnreadOnly = () => {
  showUnreadOnly.value = !showUnreadOnly.value
}

// 上传文件
const handleUploadFile = (file) => {
  ElMessage.info('文件上传功能开发中...')
}

// 上传图片
const handleUploadImage = (file) => {
  ElMessage.info('图片上传功能开发中...')
}

// ==================== 拖拽调整宽度 ====================

// 开始拖拽
const startResize = (e) => {
  isResizing.value = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

// 拖拽中
const onResize = (e) => {
  if (!isResizing.value) return

  const container = document.querySelector('.chat-content')
  if (!container) return

  const containerRect = container.getBoundingClientRect()
  const newWidth = e.clientX - containerRect.left

  // 限制最小和最大宽度
  const minWidth = 220
  const maxWidth = 600

  if (newWidth >= minWidth && newWidth <= maxWidth) {
    conversationListWidth.value = newWidth
  }
}

// 停止拖拽
const stopResize = () => {
  isResizing.value = false
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
}
</script>

<template>
  <div class="chat-optimized-container">
    <!-- 头部 -->
    <div class="chat-header">
      <h3 class="page-title">【商家消息】</h3>
    </div>

    <!-- 搜索和筛选栏 -->
    <div class="search-filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索会话..."
        :prefix-icon="Search"
        clearable
        class="search-input"
      />
      <el-checkbox v-model="showUnreadOnly" @change="toggleUnreadOnly"> 仅显示未读 </el-checkbox>
    </div>

    <!-- 聊天内容 -->
    <div class="chat-content">
      <!-- 会话列表 -->
      <div class="conversation-list-container" :style="{ width: conversationListWidth + 'px' }">
        <ConversationList
          :conversations="filteredConversations"
          :selected-conversation="selectedConversation"
          :search-keyword="searchKeyword"
          :show-unread-only="showUnreadOnly"
          @select="selectConversation"
        />
      </div>

      <!-- 拖拽条 -->
      <div class="resize-handle" @mousedown="startResize" :class="{ 'is-resizing': isResizing }">
        <div class="resize-handle-bar"></div>
      </div>

      <!-- 聊天区域 -->
      <div v-if="selectedConversation" class="chat-area">
        <!-- 聊天头部 -->
        <div class="chat-area-header">
          <div class="conversation-info">
            <div class="name-info">
              <span class="name">{{ selectedConversation.name }}</span>
              <span v-if="selectedConversation.type === 'group'" class="member-count">
                ({{ selectedConversation.memberCount }}人)
              </span>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <ChatMessageList
          :messages="chatMessages"
          :conversation-type="conversationType"
          :current-user-id="currentUserId"
        />

        <!-- 消息输入 -->
        <MessageInput
          :disabled="!selectedConversation"
          :sending="sending"
          :show-sync-toggle="selectedConversation.type === 'private'"
          :sync-to-group="syncToGroup"
          @update:sync-to-group="syncToGroup = $event"
          @send="sendMessage"
          @upload-file="handleUploadFile"
          @upload-image="handleUploadImage"
        />
      </div>

      <!-- 未选择会话时的提示 -->
      <div v-else class="chat-area">
        <div class="chat-area-empty">
          <el-empty description="请先选择一个会话" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

.chat-optimized-container {
  padding: 0 20px 20px 20px;
  height: calc(100vh - 60px);

  .chat-header {
    margin-bottom: 16px;

    .page-title {
      font-size: 1.286rem /* 原值: 18px */;
      font-weight: 600;
      margin: 0;
      color: @merchant-text;
    }
  }

  .search-filter-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: @merchant-surface;
    border-radius: 12px;
    border: 1px solid @merchant-border;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

    .search-input {
      flex: 1;
      max-width: 400px;

      :deep(.el-input__wrapper) {
        border-radius: 8px;
      }
    }
  }

  .chat-content {
    display: flex;
    gap: 0;
    height: calc(100vh - 180px);

    .conversation-list-container {
      flex-shrink: 0;
      transition: width 0.1s ease;
      border: 1px solid @merchant-border;
      border-right: none;
      border-radius: 16px 0 0 16px;
      overflow: hidden;
      background: @merchant-surface;
      height: 100%;
    }

    .resize-handle {
      width: 6px;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: col-resize;
      background: @merchant-border;
      transition: all 0.2s ease;
      position: relative;
      user-select: none;

      &:hover {
        background: @merchant-primary;
        width: 8px;

        .resize-handle-bar {
          background: @merchant-surface;
        }
      }

      &.is-resizing {
        background: @merchant-primary;
        width: 8px;

        .resize-handle-bar {
          background: @merchant-surface;
        }
      }

      .resize-handle-bar {
        width: 3px;
        height: 40px;
        background: @merchant-border;
        border-radius: 2px;
        transition: all 0.2s ease;
      }
    }

    .chat-area {
      flex: 1;
      border: 1px solid @merchant-border;
      border-left: none;
      border-radius: 0 16px 16px 0;
      display: flex;
      flex-direction: column;
      background: @merchant-surface;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      overflow: hidden;

      .chat-area-header {
        padding: 16px 20px;
        border-bottom: 1px solid @merchant-border;
        background: @merchant-surface;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        flex-shrink: 0;

        .conversation-info {
          display: flex;
          align-items: center;

          .name-info {
            .name {
              font-size: 1.143rem /* 原值: 16px */;
              font-weight: 600;
              color: @merchant-text;
            }

            .member-count {
              font-size: 0.857rem /* 原值: 12px */;
              color: @merchant-text-muted;
              margin-left: 8px;
            }
          }
        }
      }

      .chat-area-empty {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}
</style>
