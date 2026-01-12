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
import { formatConversationTime, cleanMessage, sortConversationsByUnread } from '../../utils/chat/messageUtils'

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
    result = result.filter(conv =>
      conv.name.toLowerCase().includes(keyword) ||
      conv.lastMessage.toLowerCase().includes(keyword)
    )
  }

  // 仅显示未读
  if (showUnreadOnly.value) {
    result = result.filter(conv => conv.unreadCount > 0)
  }

  // 排序
  return sortConversationsByUnread(result)
})

// ==================== 初始化 ====================

// 页面加载
onMounted(async () => {
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
    // 构建会话ID
    const sessionId = buildSessionId(
      currentUserId.value,
      conversation.id,
      conversation.type
    )

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
    conversation.unreadCount = 0
    ElMessage.success('消息已标记为已读')

    // 调用API标记已读
    const sessionId = buildSessionId(
      currentUserId.value,
      conversation.id,
      conversation.type
    )
    await markMessagesAsRead(sessionId)
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
      const localMessage = createLocalMessage(
        cleanedContent,
        selectedConversation.value.type
      )

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
      <el-checkbox v-model="showUnreadOnly" @change="toggleUnreadOnly">
        仅显示未读
      </el-checkbox>
    </div>

    <!-- 聊天内容 -->
    <div class="chat-content">
      <!-- 会话列表 -->
      <ConversationList
        :conversations="filteredConversations"
        :selected-conversation="selectedConversation"
        :search-keyword="searchKeyword"
        :show-unread-only="showUnreadOnly"
        @select="selectConversation"
      />

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
.chat-optimized-container {
  padding: 0 20px 20px 20px;
  height: calc(100vh - 60px);

  .chat-header {
    margin-bottom: 16px;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
      color: #1f2937;
    }
  }

  .search-filter-bar {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    padding: 12px 16px;
    background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
    border-radius: 12px;
    border: 1px solid #e8eef5;
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
    gap: 20px;
    height: calc(100vh - 180px);

    .chat-area {
      flex: 1;
      border: 1px solid #e8eef5;
      border-radius: 16px;
      display: flex;
      flex-direction: column;
      background: #ffffff;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
      overflow: hidden;

      .chat-area-header {
        padding: 16px 20px;
        border-bottom: 1px solid #e8eef5;
        background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
        flex-shrink: 0;

        .conversation-info {
          display: flex;
          align-items: center;

          .name-info {
            .name {
              font-size: 16px;
              font-weight: 600;
              color: #1f2937;
            }

            .member-count {
              font-size: 12px;
              color: #909399;
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
