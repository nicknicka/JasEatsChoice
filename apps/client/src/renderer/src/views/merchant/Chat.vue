<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import router from '../../router'
import { useAuthStore } from '../../store/authStore'
import ConversationList from '../../components/merchant/chat/ConversationList.vue'
import ChatMessageList from '../../components/merchant/chat/ChatMessageList.vue'
import MessageInput from '../../components/merchant/chat/MessageInput.vue'
import {
  getCurrentMerchantId,
  getChatSessions,
  getChatMessages,
  sendMessage as sendChatMessage,
  markMessagesAsRead,
  formatMessageForSend,
  createLocalMessage,
  handleApiError
} from '../../utils/chat/chatApi'
import { cleanMessage, sortConversationsByUnread } from '../../utils/chat/messageUtils'

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

// ==================== 初始化 ====================

// 页面加载
onMounted(async () => {
  // 获取当前商家ID（商家端聊天使用 merchantId）
  currentUserId.value = getCurrentMerchantId()

  // 验证商家ID
  if (!currentUserId.value || currentUserId.value === '1') {
    // 无法获取商家ID，弹出提示框要求重新登录
    ElMessageBox.alert('无法获取商家ID，请重新登录', '身份验证失败', {
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
    const sessions = await getChatSessions(currentUserId.value)

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
    // ⭐ 直接使用后端返回的 sessionId（conversation.id）
    // ⚠️ 不再使用 buildSessionId，因为后端已经统一生成了sessionId
    const sessionId = conversation.id
    const messages = await getChatMessages(sessionId, currentUserId.value)

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
    const sessionId = conversation.id
    const result = await markMessagesAsRead(sessionId, currentUserId.value)

    if (!result.success) {
      await loadMessages(conversation)
      return
    }

    conversation.unreadCount = 0
    ElMessage.success('消息已标记为已读')
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

    // ⭐ 确定 toId：
    // - 单聊：使用 targetId（对方的 userId 或 merchantId）
    // - 群聊：使用 groupId
    let toId
    let sessionType
    if (selectedConversation.value.type === 'group') {
      toId = selectedConversation.value.groupId
      sessionType = 'group'
    } else {
      toId = selectedConversation.value.targetId || selectedConversation.value.userId
      sessionType = 'single'
    }

    if (!toId) {
      ElMessage.error('无法获取接收者ID')
      sending.value = false
      return
    }

    // 构建消息数据
    const messageData = formatMessageForSend(
      cleanedContent,
      currentUserId.value,  // fromId: 商家的 merchantId
      toId,                 // toId: 用户的 userId 或群ID
      sessionType
    )

    console.log('📤 [商家端发送消息]', {
      fromId: currentUserId.value,
      toId: toId,
      sessionType: sessionType
    })

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
