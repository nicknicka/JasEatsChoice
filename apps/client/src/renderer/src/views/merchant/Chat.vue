<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import router from '../../router'
import { useAuthStore } from '../../store/authStore'
import ConversationList from '../../components/merchant/chat/ConversationList.vue'
import ChatMessageList from '../../components/merchant/chat/ChatMessageList.vue'
import MessageInput from '../../components/merchant/chat/MessageInput.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
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
    <!-- 页头：merchant 紧凑版；标题与侧栏菜单「商家聊天」一致，返回在右侧动作区（§4.7） -->
    <AppPageHeader title="商家聊天" :compact="true">
      <template #actions>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <!-- 聊天内容：左栏会话列表 + 右侧当前会话 -->
    <div class="chat-content">
      <!-- 会话列表：搜索与未读筛选并入左栏顶部，与列表同宽（评审 P0；§4.3） -->
      <div class="conversation-list-container" :style="{ width: conversationListWidth + 'px' }">
        <div class="conversation-toolbar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索会话..."
            :prefix-icon="Search"
            clearable
            class="conversation-toolbar__search"
          />
          <el-checkbox v-model="showUnreadOnly" class="conversation-toolbar__unread">
            仅显示未读
          </el-checkbox>
        </div>

        <!-- 会话列表 -->
        <div class="conversation-list-body">
          <ConversationList
            :conversations="filteredConversations"
            :selected-conversation="selectedConversation"
            :search-keyword="searchKeyword"
            :show-unread-only="showUnreadOnly"
            @select="selectConversation"
          />
        </div>
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

<style scoped>
/* 本页颜色、间距、圆角、字阶、动效全部取自 tokens.css 的 CSS 变量（§3、§7.1），无裸色值。 */
.chat-optimized-container {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 0 var(--space-6) var(--space-6);
}

/* 左右分栏：占满页头以下的剩余高度，内容各自滚动（§4.6） */
.chat-content {
  display: flex;
  flex: 1;
  min-height: 0;
}

/* 左栏：搜索与未读筛选并入列表列，与列表同宽，不再横跨整页（评审 P0；§4.3） */
.conversation-list-container {
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-right: none;
  border-radius: var(--radius-lg) 0 0 var(--radius-lg);
  transition: width var(--duration-fast) var(--ease-standard);
}

.conversation-toolbar {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  background: var(--surface);
  border-bottom: 1px solid var(--line);
}

/* 工具条控件取 --control-height-sm（§3.4） */
.conversation-toolbar :deep(.el-input__wrapper) {
  min-height: var(--control-height-sm);
  border-radius: var(--radius-sm);
}

.conversation-toolbar__unread :deep(.el-checkbox__label) {
  padding-left: var(--space-2);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

/* 列表占满工具条以下的剩余高度，内部自行滚动 */
.conversation-list-body {
  display: flex;
  flex: 1;
  min-height: 0;
}

/* 拖拽条：hover 与拖拽中升为品牌色（§3.5 状态切换 150ms） */
.resize-handle {
  position: relative;
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 6px;
  cursor: col-resize;
  background: var(--line);
  user-select: none;
  transition:
    width var(--duration-fast) var(--ease-standard),
    background var(--duration-fast) var(--ease-standard);
}

.resize-handle:hover,
.resize-handle.is-resizing {
  width: 8px;
  background: var(--brand-500);
}

.resize-handle-bar {
  width: 3px;
  height: 40px;
  background: var(--line);
  border-radius: var(--radius-full);
  transition: background var(--duration-fast) var(--ease-standard);
}

.resize-handle:hover .resize-handle-bar,
.resize-handle.is-resizing .resize-handle-bar {
  background: var(--surface);
}

/* 右侧当前会话面板：与左栏合成一块 L1 卡片（§3.4） */
.chat-area {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  border-left: none;
  border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
  box-shadow: var(--shadow-sm);
}

.chat-area-header {
  flex-shrink: 0;
  padding: var(--space-4) var(--space-5);
  background: var(--surface);
  border-bottom: 1px solid var(--line);
}

.chat-area-header .conversation-info {
  display: flex;
  align-items: center;
}

.chat-area-header .name {
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.chat-area-header .member-count {
  margin-left: var(--space-2);
  font-size: var(--text-caption);
  color: var(--ink-400);
}

.chat-area-empty {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
}

/* 对话区与消息气泡（评审 P1；§3.4、§7.1）：
   气泡与对话底由子组件 ChatMessageList.vue 渲染，这里用 :deep() 做页面级覆盖，
   让改动只落回本页；己方=品牌强调，对方=中性，并约束阅读行宽。 */
.chat-content :deep(.chat-area .chat-message-list) {
  background: var(--canvas);
}

/* 消息从面板底部贴底排列，减少内容悬空（评审 P1；§4.6） */
.chat-content :deep(.chat-area .chat-message-list .messages-wrapper) {
  justify-content: flex-end;
  min-height: 100%;
}

.chat-content :deep(.chat-area .message-item) {
  max-width: 65%;
}

.chat-content :deep(.chat-area .message-item.my-message .message-content) {
  background: var(--brand-100);
  border: 1px solid var(--brand-300);
  color: var(--ink-900);
}

.chat-content :deep(.chat-area .message-item.my-message .message-content .message-time) {
  color: var(--brand-700);
}

.chat-content :deep(.chat-area .message-item:not(.my-message) .message-content) {
  background: var(--surface);
  border: 1px solid var(--line);
  color: var(--ink-700);
}

.chat-content :deep(.chat-area .message-item:not(.my-message) .message-content .message-time) {
  color: var(--ink-400);
}
</style>
