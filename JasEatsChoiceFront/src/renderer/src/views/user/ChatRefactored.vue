<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3 class="page-title">【聊天消息】</h3>
      <div class="chat-actions">
        <el-button type="primary" size="small" @click="createNewChat"> + 新建聊天 </el-button>
        <el-button type="primary" size="small" @click="openAddFriendDialog"> + 加好友 </el-button>
        <el-button type="primary" size="small" @click="createNewGroup"> + 新建群聊 </el-button>
      </div>
    </div>

    <div class="chat-content">
      <!-- 左侧会话列表 -->
      <ConversationList
        :conversations="sortedConversations"
        v-model="selectedConversation"
        @select="handleSelectConversation"
        @contextmenu="showContextMenu"
        @toggle-pin="togglePin"
      />

      <!-- 会话右键菜单 -->
      <div
        v-if="contextMenuVisible && selectedContextConversation"
        class="context-menu"
        :style="{
          left: contextMenuPosition.x + 'px',
          top: contextMenuPosition.y + 'px'
        }"
        @click.stop
      >
        <div class="menu-item" @click="togglePin(selectedContextConversation)">
          {{ selectedContextConversation.pinned ? '取消置顶' : '置顶会话' }}
        </div>
        <div class="menu-item" @click="deleteConversation(selectedContextConversation)" style="color: #ff4d4f">
          删除会话
        </div>
      </div>

      <!-- 右侧聊天内容 -->
      <div class="chat-area" v-if="selectedConversation">
        <!-- 聊天头部 -->
        <div class="chat-area-header">
          <div class="conversation-info">
            <div class="name-info">
              <span class="name">{{ selectedConversation.name }}</span>
              <span v-if="selectedConversation.type === 'group'" class="member-count">
                ({{ selectedConversation.memberCount || '0' }}人)</span
              >
            </div>
          </div>
          <div class="header-actions">
            <!-- 消息搜索 -->
            <el-input
              v-model="searchKeyword"
              placeholder="搜索消息记录"
              size="small"
              style="width: 200px; margin-right: 10px"
              @input="searchMessages"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <!-- 导出聊天记录 -->
            <el-button size="small" @click="exportChatHistory(selectedConversation)" style="margin-right: 10px">
              <el-icon><Download /></el-icon> 导出记录
            </el-button>
            <!-- 群聊操作 -->
            <div class="chat-actions" v-if="selectedConversation.type === 'group'">
              <el-button
                type="primary"
                size="small"
                @click="createGroupOrder"
                v-if="!groupOrders[selectedConversation.id]"
                >创建群订单</el-button
              >
              <el-button size="small" @click="joinGroupOrder">加入群订单</el-button>
              <el-button size="small" @click="openGroupDetail">群聊详情</el-button>
            </div>
          </div>
        </div>

        <!-- 悬浮订单按钮 -->
        <div
          v-if="selectedConversation.type === 'group' && groupOrders[selectedConversation.id]"
          ref="floatBtnRef"
          class="floating-order-btn"
          @click="handleCartClick"
          @mousedown="startDrag"
          @selectstart="handleSelectStart"
        >
          <div class="order-btn-inner">
            <el-icon :size="24" color="white"><ShoppingCart /></el-icon>
            <span
              class="cart-count"
              v-if="groupOrders[selectedConversation.id].orderItems.length > 0"
            >
              {{ groupOrders[selectedConversation.id].orderItems.length }}
            </span>
          </div>
        </div>

        <!-- 搜索结果面板 -->
        <div v-if="isSearching" class="search-results-panel">
          <div class="search-header">
            <span>找到 {{ messageSearchResults.length }} 条结果</span>
            <el-button type="text" size="small" @click="clearSearch">
              <el-icon><Close /></el-icon> 清除
            </el-button>
          </div>
          <div class="search-results-list">
            <div
              v-for="(result, index) in messageSearchResults"
              :key="result.id"
              class="search-result-item"
              :class="{ 'active': currentSearchIndex === index }"
              @click="jumpToSearchResult(index)"
            >
              <div class="result-time">{{ result.formattedTime }}</div>
              <div class="result-content" v-html="result.highlightedContent"></div>
            </div>
          </div>
        </div>

        <!-- 聊天内容 -->
        <ChatMessageList
          ref="messageListRef"
          :messages="chatMessages"
          :current-user-id="userId.toString()"
          :msg-page-num="msgPageNum"
          :msg-page-size="msgPageSize"
          :total-messages="totalMessages"
          :has-more-messages="hasMoreMessages"
          :is-loading-messages="isLoadingMessages"
          :can-recall="canRecallMessage"
          :format-message-time="formatMessageTime"
          @load-more="loadMoreMessages"
          @command="handleMessageCommand"
          @resend="resendMessage"
        />

        <!-- 消息输入框 -->
        <div class="message-input-container">
          <!-- 回复预览 -->
          <div v-if="replyingTo" class="reply-preview">
            <div class="reply-content">
              <div class="reply-header">
                <span class="reply-label">回复 @{{ replyingTo.senderName || replyingTo.fromId }}</span>
                <el-icon @click="cancelReply" style="cursor: pointer"><Close /></el-icon>
              </div>
              <div class="reply-text">{{ replyingTo.content }}</div>
            </div>
          </div>

          <el-input
            v-model="newMessage"
            type="textarea"
            placeholder="输入消息内容..."
            :rows="2"
            @keyup.enter="sendMessage"
          />
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>

      <!-- 空选择提示 -->
      <div class="empty-select" v-else>
        <div class="empty-icon">💬</div>
        <p>请选择一个会话开始交流</p>
      </div>
    </div>

    <!-- 群订单购物车悬浮窗 - 保留原有功能 -->
    <el-drawer
      v-model="orderDrawerVisible"
      title="当前群订单"
      direction="rtl"
      size="45%"
      :close-on-click-modal="true"
    >
      <!-- 群订单内容保持不变... -->
      <div v-if="selectedConversation && groupOrders[selectedConversation.id]" class="order-overview">
        <div class="overview-item">
          <span class="info-label">群名称：</span>
          <span class="info-value">{{ groupOrders[selectedConversation.id].groupName }}</span>
        </div>
        <div class="overview-item">
          <span class="info-label">总金额：</span>
          <span class="info-value"
            >¥{{ groupOrders[selectedConversation.id].totalAmount.toFixed(2) }}</span
          >
        </div>
      </div>
    </el-drawer>

    <!-- 其他对话框保持不变... -->
    <!-- 新建群聊对话框 -->
    <el-dialog
      v-model="groupDialogVisible"
      title="新建群聊"
      width="400px"
      @close="cancelCreateGroup"
    >
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="群名称">
          <el-input v-model="groupForm.name" placeholder="请输入群名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelCreateGroup">取消</el-button>
          <el-button type="primary" @click="handleCreateGroup">创建</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 加好友对话框 -->
    <el-dialog
      v-model="addFriendDialogVisible"
      title="添加好友"
      :width="selectedUser ? '800px' : '400px'"
    >
      <div style="display: flex; height: 500px">
        <div style="flex: 1; padding: 10px">
          <el-input
            v-model="friendSearchQuery"
            placeholder="搜索用户名/手机号/邮箱"
            @keyup.enter="searchUsersForAdd"
          >
            <template #append>
              <el-button :icon="Search" @click="searchUsersForAdd"></el-button>
            </template>
          </el-input>

          <div class="user-list" v-if="addFriendResults.length > 0">
            <div
              v-for="user in paginatedUsers"
              :key="user.id"
              class="user-item"
              @click="showUserDetails(user)"
            >
              <div class="user-avatar">{{ user.avatar }}</div>
              <div class="user-info">
                <div class="user-name">{{ user.nickname || user.username }}</div>
              </div>
              <el-button type="primary" size="small" @click.stop="sendFriendRequest(user)">
                加好友
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Search, Close, Download, ArrowDown, Loading } from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { decodeJwt } from '../../utils/api.js'

// Composables - 使用新架构
import { useWebSocketChat } from '../../composables/useWebSocketChat'
import { useChatMessages } from '../../composables/useChatMessages'
import { useMessageActions } from '../../composables/useMessageActions'
import { useConversations } from '../../composables/useConversations'

// Components - 使用新组件
import ConversationList from '../../components/chat/ConversationList.vue'
import ChatMessageList from '../../components/chat/ChatMessageList.vue'

// Constants
import { MESSAGE_CONFIG } from '../../constants/chatConstants'

const router = useRouter()

// ========== 用户信息 ==========
const userId = ref(parseInt(localStorage.getItem('userId') || '1', 10))
const token = localStorage.getItem('token')
if (token) {
  const decodedToken = decodeJwt(token)
  if (decodedToken && decodedToken.userId) {
    userId.value = decodedToken.userId
  }
}

// ========== 使用 Composables ==========
const {
  conversations,
  selectedConversation,
  contextMenuVisible,
  selectedContextConversation,
  contextMenuPosition,
  sortedConversations,
  showContextMenu,
  closeContextMenu,
  togglePin,
  deleteConversation,
  selectConversation: selectConversationBase,
  updateConversationLastMessage
} = useConversations()

const {
  chatHistory,
  chatMessages,
  msgPageNum,
  msgPageSize,
  totalMessages,
  hasMoreMessages,
  isLoadingMessages,
  messagesContainerRef,
  formatMessageTime,
  loadChatMessages,
  loadMoreMessages,
  addMessage,
  saveChatHistoryToLocal,
  loadChatHistoryFromLocal
} = useChatMessages({ userId, selectedConversation })

const {
  searchKeyword,
  messageSearchResults,
  isSearching,
  currentSearchIndex,
  replyingTo,
  searchMessages,
  clearSearch,
  jumpToSearchResult,
  exportChatHistory,
  canRecallMessage,
  handleMessageCommand: handleMessageCommandBase,
  cancelReply
} = useMessageActions({
  chatHistory,
  chatMessages,
  userId,
  formatMessageTime
})

const { isConnected, initWebSocket, closeWebSocket } = useWebSocketChat({
  userId,
  token,
  onMessage: handleWebSocketMessage
})

// ========== 保留的原有功能状态 ==========

// 群订单相关
const groupOrders = ref({})
const orderDrawerVisible = ref(false)

// 对话框状态
const groupDialogVisible = ref(false)
const addFriendDialogVisible = ref(false)
const friendSearchQuery = ref('')
const addFriendResults = ref([])
const selectedUser = ref(null)
const currentPage = ref(1)
const pageSize = ref(7)

// 表单数据
const groupForm = ref({
  name: '',
  members: ''
})

const newMessage = ref('')

// WebSocket 消息处理
function handleWebSocketMessage(data) {
  switch (data.type) {
    case 'chat':
      handleNewMessage(data.content)
      break
    case 'notification':
      ElMessage.info(data.content)
      break
    case 'heartbeat':
      console.log('收到心跳响应')
      break
  }
}

// 处理新消息
function handleNewMessage(message) {
  const sessionId = message.sessionId || message.toId

  if (selectedConversation.value && sessionId === selectedConversation.value.id) {
    const processedMsg = {
      ...message,
      formattedTime: formatMessageTime(message.createTime || message.time),
      fromId: message.fromId || message.sender || '未知'
    }
    addMessage(processedMsg, sessionId)
  }

  updateConversationLastMessage(sessionId, message)
}

// 选择会话
async function handleSelectConversation(conversation) {
  await selectConversationBase(conversation)

  // 加载聊天记录
  if (conversation) {
    await loadChatMessages(conversation.id)
  }
}

// 发送消息
async function sendMessage() {
  if (!newMessage.value.trim() || !selectedConversation.value) {
    return
  }

  const messageData = {
    fromId: userId.value.toString(),
    toId: selectedConversation.value.id,
    msgType: selectedConversation.value.type || 'single',
    content: newMessage.value.trim()
  }

  if (replyingTo.value) {
    messageData.replyTo = replyingTo.value.id
    messageData.replyContent = replyingTo.value.content
    messageData.replyFromId = replyingTo.value.fromId
    messageData.replyFromName = replyingTo.value.senderName || replyingTo.value.fromId
  }

  try {
    const response = await api.post('/v1/chat/messages', messageData)

    if (response.code === '200') {
      const sentMessage = response.data
      addMessage(sentMessage, selectedConversation.value.id)
      updateConversationLastMessage(selectedConversation.value.id, sentMessage)
      newMessage.value = ''
      if (replyingTo.value) {
        cancelReply()
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  }
}

// 消息命令处理（转发、撤回等）
async function handleMessageCommand(command, message) {
  if (command === 'recall') {
    try {
      await ElMessageBox.confirm('确认撤回这条消息吗?', '撤回消息', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      })

      const response = await api.post(`/v1/chat/messages/${message.id}/recall`, {
        userId: userId.value.toString()
      })

      if (response.code === '200') {
        const index = chatMessages.value.findIndex((msg) => msg.id === message.id)
        if (index !== -1) {
          chatMessages.value[index].content = '消息已撤回'
        }
        ElMessage.success('消息已撤回')
      }
    } catch (error) {
      if (error !== 'cancel') {
        console.error('撤回消息失败:', error)
        ElMessage.error('撤回消息失败')
      }
    }
  } else {
    await handleMessageCommandBase(command, message)
  }
}

// 重发消息
async function resendMessage(failedMessage) {
  try {
    const messageData = {
      fromId: userId.value.toString(),
      toId: selectedConversation.value.id,
      msgType: selectedConversation.value.type || 'single',
      content: failedMessage.content
    }

    const response = await api.post('/v1/chat/messages', messageData)

    if (response.code === '200') {
      const index = chatMessages.value.findIndex((msg) => msg.id === failedMessage.id)
      if (index !== -1) {
        chatMessages.value.splice(index, 1)
      }

      const sentMessage = response.data
      addMessage(sentMessage, selectedConversation.value.id)
      ElMessage.success('消息重发成功')
    }
  } catch (error) {
    console.error('重发消息失败:', error)
    ElMessage.error('重发消息失败')
  }
}

// ========== 群订单功能 ==========
function createGroupOrder() {
  if (selectedConversation.value) {
    const order = {
      orderId: `GO${Date.now()}`,
      groupId: selectedConversation.value.id,
      groupName: selectedConversation.value.name,
      creator: '我',
      members: ['我'],
      orderItems: [],
      totalAmount: 0.0,
      status: 'active',
      createTime: new Date().toISOString()
    }

    groupOrders.value[selectedConversation.value.id] = order
    ElMessage.success('群订单已创建')
  }
}

function joinGroupOrder() {
  if (selectedConversation.value) {
    const conversationOrder = groupOrders.value[selectedConversation.value.id]
    if (conversationOrder) {
      if (!conversationOrder.members.includes('我')) {
        conversationOrder.members.push('我')
        ElMessage.success('已加入群订单')
      } else {
        ElMessage.warning('你已经在群订单中了')
      }
    } else {
      ElMessage.error('当前群没有订单')
    }
  }
}

function openGroupDetail() {
  ElMessage.info('群聊详情功能开发中')
}

// 悬浮按钮拖拽
const floatBtnRef = ref(null)
const isDragging = ref(false)
const hasDragged = ref(false)
const startX = ref(0)
const startY = ref(0)
let handleMouseMoveFn = null
let handleMouseUpFn = null

function handleCartClick() {
  if (hasDragged.value) {
    hasDragged.value = false
    return
  }
  if (!isDragging.value) {
    orderDrawerVisible.value = true
  }
}

function startDrag(e) {
  if (!floatBtnRef.value) return
  isDragging.value = true
  startX.value = e.clientX - floatBtnRef.value.offsetLeft
  startY.value = e.clientY - floatBtnRef.value.offsetTop

  handleMouseMoveFn = (moveEvent) => {
    hasDragged.value = true
    if (isDragging.value && floatBtnRef.value) {
      let newX = moveEvent.clientX - startX.value
      let newY = moveEvent.clientY - startY.value

      const windowWidth = window.innerWidth
      const windowHeight = window.innerHeight
      const btnWidth = floatBtnRef.value.offsetWidth
      const btnHeight = floatBtnRef.value.offsetHeight

      newX = Math.max(0, Math.min(newX, windowWidth - btnWidth))
      newY = Math.max(0, Math.min(newY, windowHeight - btnHeight))

      floatBtnRef.value.style.left = newX + 'px'
      floatBtnRef.value.style.top = newY + 'px'
      floatBtnRef.value.style.bottom = 'auto'
      floatBtnRef.value.style.right = 'auto'
    }
  }

  handleMouseUpFn = () => {
    isDragging.value = false
  }

  document.addEventListener('mousemove', handleMouseMoveFn)
  document.addEventListener('mouseup', handleMouseUpFn)
  e.preventDefault()
}

function handleSelectStart(e) {
  e.preventDefault()
}

// ========== 好友管理功能 ==========
function openAddFriendDialog() {
  addFriendDialogVisible.value = true
}

async function searchUsersForAdd() {
  if (!friendSearchQuery.value) {
    addFriendResults.value = []
    return
  }

  try {
    const response = await api.get(`/v1/users/search?keyword=${friendSearchQuery.value}`)

    if (response.code === '200') {
      addFriendResults.value = response.data.map((user) => ({
        id: user.userId,
        nickname: user.nickname,
        username: user.username,
        avatar: '👤'
      }))
      currentPage.value = 1
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
    ElMessage.error('搜索用户失败')
  }
}

function showUserDetails(user) {
  selectedUser.value = user
}

async function sendFriendRequest(user) {
  try {
    const response = await api.post(`/v1/contacts/friends/request`, {
      userId: userId.value,
      targetId: user.id
    })

    if (response.code === '200') {
      ElMessage.success('已发送好友请求')
      addFriendDialogVisible.value = false
    }
  } catch (error) {
    console.error('发送好友请求失败:', error)
    ElMessage.error('发送好友请求失败')
  }
}

// ========== 群聊管理功能 ==========
function createNewGroup() {
  groupDialogVisible.value = true
}

function handleCreateGroup() {
  if (!groupForm.value.name.trim()) {
    ElMessage.error('请输入群名称')
    return
  }

  const newGroupId = Date.now()
  const newGroup = {
    id: newGroupId,
    type: 'group',
    name: groupForm.value.name.trim(),
    avatar: '👥',
    lastMessage: '暂无消息',
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    unreadCount: 0,
    memberCount: 1,
    pinned: false
  }

  conversations.value.push(newGroup)
  groupDialogVisible.value = false
  groupForm.value = { name: '', members: '' }
  ElMessage.success('群聊已创建')
}

function cancelCreateGroup() {
  groupDialogVisible.value = false
  groupForm.value = { name: '', members: '' }
}

// ========== 聊天功能 ==========
function createNewChat() {
  ElMessage.info('新建聊天功能开发中')
}

// 计算属性
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return addFriendResults.value.slice(start, end)
})

// ========== 生命周期 ==========
onMounted(async () => {
  loadChatHistoryFromLocal()
  initWebSocket()

  // 添加全局点击事件监听器
  document.addEventListener('click', closeContextMenu)

  try {
    const conversationsResponse = await api.get(`/v1/chat/users/${userId.value}/chat-sessions`)

    if (conversationsResponse.code === '200') {
      conversations.value = conversationsResponse.data

      if (sortedConversations.value.length > 0) {
        const firstConv = sortedConversations.value[0]
        await handleSelectConversation(firstConv)
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
})

onBeforeUnmount(() => {
  closeWebSocket()
  saveChatHistoryToLocal()
  document.removeEventListener('click', closeContextMenu)

  // 清理拖拽事件监听器
  if (handleMouseMoveFn) {
    document.removeEventListener('mousemove', handleMouseMoveFn)
  }
  if (handleMouseUpFn) {
    document.removeEventListener('mouseup', handleMouseUpFn)
  }
})
</script>

<style scoped lang="less">
// 保持原有样式不变
.chat-container {
  padding: 0 20px 20px 20px;
  height: calc(100vh - 60px);

  .chat-header {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .page-title {
      font-size: 18px;
      font-weight: 600;
      margin: 0;
    }

    .chat-actions {
      display: flex;
      gap: 10px;
    }
  }

  .chat-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 120px);

    .chat-area {
      flex: 1;
      border: 1px solid #e4e7ed;
      border-radius: 4px;
      display: flex;
      flex-direction: column;

      .chat-area-header {
        padding: 12px;
        border-bottom: 1px solid #e4e7ed;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .conversation-info {
          display: flex;
          align-items: center;

          .name-info {
            .name {
              font-weight: 500;
            }

            .member-count {
              font-size: 12px;
              color: #909399;
            }
          }
        }
      }

      .floating-order-btn {
        position: fixed;
        bottom: 80px;
        right: 40px;
        width: 60px;
        height: 60px;
        background-color: #67c23a;
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        box-shadow: 0 3px 15px 0 rgba(103, 194, 58, 0.4);
        z-index: 1000;

        &:hover {
          background-color: #85ce61;
          transform: translateY(-3px);
        }

        .cart-count {
          position: absolute;
          top: -5px;
          right: -5px;
          background: #f56c6c;
          color: white;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 13px;
        }
      }

      .search-results-panel {
        position: absolute;
        top: 60px;
        right: 20px;
        width: 350px;
        max-height: 400px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
        z-index: 100;

        .search-header {
          padding: 12px 16px;
          border-bottom: 1px solid #ebeef5;
          display: flex;
          justify-content: space-between;
        }

        .search-results-list {
          max-height: 340px;
          overflow-y: auto;
          padding: 8px;

          .search-result-item {
            padding: 10px;
            border-radius: 6px;
            cursor: pointer;
            margin-bottom: 4px;

            &:hover {
              background-color: #f5f7fa;
            }

            &.active {
              background-color: #e6f7ff;
              border: 1px solid #1890ff;
            }

            .result-time {
              font-size: 11px;
              color: #909399;
              margin-bottom: 4px;
            }

            .result-content {
              font-size: 13px;
              word-break: break-word;
            }
          }
        }
      }

      .message-input-container {
        padding: 12px;
        border-top: 1px solid #e4e7ed;
        display: flex;
        flex-direction: column;
        gap: 12px;

        .reply-preview {
          padding: 10px 12px;
          background-color: #f5f7fa;
          border-radius: 6px;
          border-left: 3px solid #409eff;

          .reply-content {
            .reply-header {
              display: flex;
              justify-content: space-between;
              margin-bottom: 6px;

              .reply-label {
                font-size: 13px;
                font-weight: 500;
                color: #409eff;
              }
            }

            .reply-text {
              font-size: 12px;
              color: #606266;
            }
          }
        }

        > div:not(.reply-preview) {
          display: flex;
          gap: 12px;
        }
      }
    }
  }
}

.context-menu {
  position: fixed;
  z-index: 10000;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-width: 160px;
  padding: 8px 0;

  .menu-item {
    padding: 10px 20px;
    cursor: pointer;
    font-size: 14px;

    &:hover {
      background-color: #f5f7fa;
    }
  }
}

.empty-select {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  color: #999;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
}

.user-list {
  max-height: 300px;
  overflow-y: auto;
  margin-top: 15px;

  .user-item {
    display: flex;
    align-items: center;
    padding: 10px;
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 8px;

    &:hover {
      background-color: #f5f7fa;
    }

    .user-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background-color: #e0e0e0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      margin-right: 10px;
    }

    .user-info {
      flex: 1;

      .user-name {
        font-weight: 500;
      }
    }
  }
}
</style>
