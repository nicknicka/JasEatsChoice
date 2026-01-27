<template>
  <div class="chat-container" @click="handleGlobalClick">
    <!-- 使用新的头部组件 -->
    <ChatHeader @open-action-panel="openActionPanelWithTab" />

    <div class="chat-content" :class="{ 'is-resizing': isResizing }">
      <!-- 左侧会话列表 -->
      <div
        class="conversation-list-wrapper"
        :style="{ width: leftPanelWidth + 'px' }"
        :class="{ 'is-resizing': isResizing }"
      >
        <ConversationList
          v-model="selectedConversation"
          :conversations="sortedConversations"
          @select="selectConversation"
          @contextmenu="showContextMenu"
          @toggle-pin="togglePin"
          @create-new="openActionPanelWithTab"
        />

        <!-- 会话右键菜单 -->
        <ConversationContextMenu
          :visible="contextMenuVisible"
          :conversation="selectedContextConversation"
          :position="contextMenuPosition"
          @toggle-pin="togglePin"
          @delete="deleteConversation"
        />
      </div>

      <!-- 可拖动分隔条 -->
      <div
        class="resize-divider"
        @mousedown="startResize"
        @dblclick="resetPanelWidth"
        :class="{
          'is-resizing': isResizing,
          'near-min-width': isNearMinWidth,
          'near-max-width': isNearMaxWidth
        }"
        title="拖动调整宽度，双击重置"
      >
        <div v-if="isResizing" class="resize-tooltip">
          {{ Math.round(leftPanelWidth) }}px
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div v-if="selectedConversation" class="chat-area">
        <!-- 聊天头部 -->
        <ChatAreaHeader
          :conversation="selectedConversation"
          :has-group-order="hasGroupOrder"
          @search="searchMessages"
          @export="exportChatHistory"
          @create-group-order="createGroupOrder"
          @join-group-order="joinGroupOrder"
          @show-group-detail="openGroupDetail"
        />

        <!-- 悬浮订单按钮 -->
        <GroupOrderFloatingButton
          v-if="selectedConversation.type === 'group' && hasGroupOrder"
          :item-count="groupOrderItemsCount"
          @click="orderDrawerVisible = true"
        />

        <!-- 消息搜索结果面板 -->
        <MessageSearchPanel
          :visible="isSearching"
          :results="messageSearchResults"
          :current-index="currentSearchIndex"
          @clear="clearSearch"
          @jump="jumpToSearchResult"
        />

        <!-- 消息列表 -->
        <div ref="messagesContainerRef" class="messages-container">
          <!-- 加载更多提示 -->
          <div
            v-if="msgPageNum > 1 || totalMessages > msgPageSize"
            class="load-more-tip"
            @click="hasMoreMessages && !isLoadingMessages && loadMoreMessages()"
          >
            <span v-if="isLoadingMessages" class="loading-text">
              <el-icon class="is-loading"><Loading /></el-icon> 加载中...
            </span>
            <span v-else-if="hasMoreMessages" class="clickable-text">点击加载更多消息</span>
            <span v-else class="no-more-text">没有更多消息了</span>
          </div>

          <!-- 使用消息列表组件 -->
          <MessageItem
            v-for="message in chatMessages"
            :key="message.id"
            :message="message"
            :user-id="userId"
            :format-message-time="formatMessageTime"
            :can-recall-message="canRecallMessage"
            @command="handleMessageCommand"
            @resend="resendMessage"
          />
        </div>

        <!-- 空数据提示 -->
        <div v-if="chatMessages.length === 0" class="empty-chat">
          <el-empty description="暂无聊天记录"></el-empty>
        </div>

        <!-- 消息输入框 -->
        <MessageInput
          :replying-to="replyingTo"
          :disabled="!selectedConversation"
          @send="sendMessage"
          @cancel-reply="cancelReply"
          @send-image="sendImageMessage"
          @send-file="sendFileMessage"
        />
      </div>

      <!-- 空选择提示 -->
      <div v-else class="empty-select" @click="openActionPanelWithTab">
        <div class="empty-icon">💬</div>
        <p class="empty-title">请选择一个会话开始交流</p>
        <p class="empty-tip">或点击此处创建新会话</p>
      </div>
    </div>

    <!-- 统一操作面板 -->
    <NewActionPanel
      v-model="actionPanelVisible"
      :friends="friends"
      :conversations="conversations"
      :user-id="userId"
      @start-chat="startChatFromPanel"
      @create-group="createGroupFromPanel"
      @add-friend="handleAddFriendFromPanel"
      @refresh-friends="fetchFriends"
    />

    <ForwardMessageDialog
      v-model="forwardDialogVisible"
      :message="forwardMessage"
      :conversations="conversations"
      @confirm="handleForwardConfirm"
    />

    <GroupDetailDialog v-model="groupDetailDialogVisible" :group-info="currentGroupInfo" />

    <MerchantSelectDialog
      v-model="merchantSelectDialogVisible"
      :merchants="merchants"
      @select="selectMerchant"
    />

    <ProductSelectDialog
      v-model="productSelectDialogVisible"
      :merchant="selectedMerchant"
      @add-to-cart="addProductToCart"
      @confirm="confirmProductSelection"
    />

    <GroupOrderDrawer
      v-model="orderDrawerVisible"
      :group-order="currentGroupOrder"
      :current-user-id="userId"
      :pending-review-count="pendingReviewCount"
      :pending-payment-count="pendingPaymentCount"
      @change-merchant="changeMerchant"
      @continue-order="openMerchantSelectDialog"
      @select-merchant="openMerchantSelectDialog"
      @go-to-pay="goToOrderConfirmation"
      @open-add-dish-dialog="openAddDishDialog"
      @open-add-dish-review="openAddDishReview"
      @open-pending-payment="openPendingPayment"
    />

    <!-- 加菜对话框 -->
    <AddDishDialog
      v-model="addDishDialogVisible"
      :group-order-id="currentGroupOrderId"
      :ordered-dishes="orderedDishes"
      :available-dishes="availableDishes"
      :allergy-conflicts="allergyConflicts"
      @success="handleAddDishSuccess"
    />

    <!-- 加菜审核面板 -->
    <AddDishReviewPanel
      v-model="addDishReviewVisible"
      :group-order-id="currentGroupOrderId"
      @refresh="loadPendingReviewCount"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

// ========== 面板宽度控制 ==========
const leftPanelWidth = ref(280) // 左侧面板默认宽度
const isResizing = ref(false) // 是否正在拖动
const isNearMinWidth = ref(false) // 是否接近最小宽度
const isNearMaxWidth = ref(false) // 是否接近最大宽度
let animationFrameId = null // 动画帧ID

// 开始拖动
const startResize = (e) => {
  isResizing.value = true
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault() // 防止拖动时选中文字
}

// 处理拖动
const handleResize = (e) => {
  // 使用 requestAnimationFrame 优化性能
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }

  animationFrameId = requestAnimationFrame(() => {
    const container = document.querySelector('.chat-content')
    if (!container) return

    const containerRect = container.getBoundingClientRect()
    const newWidth = e.clientX - containerRect.left

    // 限制最小和最大宽度
    const minWidth = 220
    const maxWidth = 500
    const minThreshold = minWidth + 30
    const maxThreshold = maxWidth - 30

    if (newWidth >= minWidth && newWidth <= maxWidth) {
      leftPanelWidth.value = newWidth

      // 检测是否接近边界
      isNearMinWidth.value = newWidth <= minThreshold
      isNearMaxWidth.value = newWidth >= maxThreshold
    }
  })
}

// 停止拖动
const stopResize = () => {
  isResizing.value = false
  isNearMinWidth.value = false
  isNearMaxWidth.value = false
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
}

// 重置面板宽度
const resetPanelWidth = () => {
  leftPanelWidth.value = 280
  isNearMinWidth.value = false
  isNearMaxWidth.value = false
  ElMessage.success('面板宽度已重置为 280px')
}

// Composables
import { useWebSocketChat } from '../../composables/useWebSocketChat'
import { useChatMessages } from '../../composables/useChatMessages'
import { useMessageActions } from '../../composables/useMessageActions'
import { useConversations } from '../../composables/useConversations'

// Components
import ChatHeader from '../../components/chat/ChatHeader.vue'
import ChatAreaHeader from '../../components/chat/ChatAreaHeader.vue'
import ConversationList from '../../components/chat/ConversationList.vue'
import ConversationContextMenu from '../../components/chat/ConversationContextMenu.vue'
import MessageItem from '../../components/chat/MessageItem.vue'
import MessageInput from '../../components/chat/MessageInput.vue'
import GroupOrderFloatingButton from '../../components/chat/GroupOrderFloatingButton.vue'
import MessageSearchPanel from '../../components/chat/MessageSearchPanel.vue'

// Dialog Components
import NewActionPanel from '../../components/chat/dialogs/NewActionPanel.vue'
import ForwardMessageDialog from '../../components/chat/dialogs/ForwardMessageDialog.vue'
import GroupDetailDialog from '../../components/chat/dialogs/GroupDetailDialog.vue'
import MerchantSelectDialog from '../../components/chat/dialogs/MerchantSelectDialog.vue'
import ProductSelectDialog from '../../components/chat/dialogs/ProductSelectDialog.vue'
import GroupOrderDrawer from '../../components/chat/dialogs/GroupOrderDrawer.vue'
import AddDishDialog from '../../components/chat/dialogs/AddDishDialog.vue'
import AddDishReviewPanel from '../../components/chat/dialogs/AddDishReviewPanel.vue'

// Constants
import { MESSAGE_CONFIG } from '../../constants/chatConstants'
import { MERCHANT_API } from '../../constants/apiConstants'
import api from '../../utils/api.js'
import { decodeJwt } from '../../utils/api.js'

// ========== 用户信息 ==========
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'

const authStore = useAuthStore(pinia)
const userId = ref(authStore.userId || 1)
const token = ref(authStore.token || '')
const msgPageSize = MESSAGE_CONFIG.DEFAULT_PAGE_SIZE

// 如果 token 存在，解码获取 userId
if (token.value) {
  const decodedToken = decodeJwt(token.value)
  if (decodedToken && decodedToken.userId) {
    userId.value = parseInt(decodedToken.userId, 10)
  }
}

// ========== 路由信息 ==========
const router = useRouter()
const route = useRoute()

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
  updateConversationLastMessage,
  loadConversations
} = useConversations(userId)

const {
  chatHistory,
  chatMessages,
  msgPageNum,
  totalMessages,
  hasMoreMessages,
  isLoadingMessages,
  messagesContainerRef,
  formatMessageTime,
  loadChatMessages,
  loadMoreMessages,
  addMessage,
  scrollToBottom,
  loadChatHistoryFromLocal
} = useChatMessages({ userId, selectedConversation })

const {
  messageSearchResults,
  isSearching,
  currentSearchIndex,
  replyingTo,
  forwardDialogVisible,
  forwardMessage,
  selectedForwardTarget,
  searchMessages,
  clearSearch,
  jumpToSearchResult,
  exportChatHistory,
  canRecallMessage,
  handleMessageCommand: handleMessageCommandBase,
  confirmForward,
  cancelReply
} = useMessageActions({
  chatHistory,
  chatMessages,
  userId,
  formatMessageTime
})

// ========== WebSocket 消息处理 ==========
const handleWebSocketMessage = (data) => {
  console.log('🔔 [WebSocket] 收到消息:', {
    type: data.type,
    hasContent: !!data.content,
    messageId: data.content?.id,
    fromId: data.content?.fromId,
    toId: data.content?.toId
  })

  switch (data.type) {
    case 'chat':
      if (data.content) {
        const fromId = data.content.fromId || data.content.sender || '未知'

        // ⭐ 获取会话ID（优先使用后端返回的sessionId，否则根据fromId和toId生成）
        let sessionId = data.content.sessionId
        if (!sessionId) {
          // 如果后端没有返回sessionId，根据消息类型和fromId/toId生成
          const toId = data.content.toId
          const sessionType = data.content.sessionType ||
            (toId?.startsWith('G') ? 'group' : 'single')

          if (sessionType === 'group') {
            // 群聊：使用群ID作为sessionId（后端已转换为S开头）
            sessionId = toId
          } else {
            // 单聊：使用双方ID生成哈希sessionId（与后端保持一致）
            const ids = [fromId, toId].sort()
            const combined = ids[0] + '_' + ids[1] + '_JasEatsChoice_Chat_2026'
            // 简单的哈希生成（模拟后端逻辑）
            let hash = 0
            for (let i = 0; i < combined.length; i++) {
              const char = combined.charCodeAt(i)
              hash = ((hash << 5) - hash) + char
              hash = hash & hash // Convert to 32bit integer
            }
            sessionId = 'S' + Math.abs(hash).toString(16).padStart(32, '0')
          }

          console.log('⚠️ [WebSocket] 后端未返回sessionId，前端生成:', sessionId)
        }

        // 确定发送者显示名称
        let senderName = null
        if (fromId !== userId.value.toString()) {
          // ⭐ 优先使用后端返回的发送者名称
          if (data.content.senderName || data.content.username || data.content.nickname) {
            senderName = data.content.senderName || data.content.username || data.content.nickname
            console.log('📛 [WebSocket] 使用后端返回的发送者名称:', senderName)
          } else if (selectedConversation.value?.type === 'single') {
            // 单聊：使用会话名称（对方的名字）
            senderName = selectedConversation.value.name
            console.log('📛 [WebSocket] 单聊：使用会话名称:', senderName)
          } else if (selectedConversation.value?.type === 'group') {
            // ⭐ 群聊：不应该直接使用 fromId，而是尝试查询或显示"未知用户"
            console.warn('⚠️ [WebSocket] 群聊消息缺少发送者名称, fromId:', fromId)
            console.warn('⚠️ [WebSocket] 后端返回的字段:', {
              senderName: data.content.senderName,
              username: data.content.username,
              nickname: data.content.nickname,
              fromId: fromId
            })
            senderName = fromId // 临时使用 fromId，但应该显示为"未知用户"或查询用户信息
          }
        }

        // ⭐ 确保消息有正确的 id 和 msgId 字段（优先使用 msgId）
        const messageId = data.content.msgId || data.content.id || Date.now()

        const message = {
          ...data.content,
          msgId: messageId,  // ⭐ 确保保留 msgId 字段
          id: messageId,     // ⭐ 标准化为 id 字段（兼容）
          formattedTime: formatMessageTime(data.content.createTime || data.content.time),
          fromId,
          senderName
        }

        console.log('💬 [WebSocket] 处理聊天消息:', {
          sessionId,
          messageId,
          msgId: messageId,
          fromId,
          toId: data.content.toId,
          content: message.content?.substring(0, 50)
        })

        // ⭐ 使用正确的sessionId和message
        addMessage(message, sessionId)
        updateConversationLastMessage(sessionId, message)
      }
      break
    case 'notification':
      console.log('📢 [WebSocket] 收到通知:', data.content?.message)
      ElMessage.info(data.content?.message || '收到新通知')
      break
    default:
      console.log('⚠️ [WebSocket] 未知消息类型:', data.type)
  }
}

const { initWebSocket, closeWebSocket } = useWebSocketChat({
  userId,
  token,
  onMessage: handleWebSocketMessage
})

// ========== 群订单管理 ==========
const groupOrders = ref({})
const orderDrawerVisible = ref(false)

const hasGroupOrder = computed(() => {
  return Boolean(
    selectedConversation.value &&
    selectedConversation.value.type === 'group' &&
    groupOrders.value[selectedConversation.value.id]
  )
})

const currentGroupOrder = computed(() => {
  if (!selectedConversation.value || !hasGroupOrder.value) return null
  return groupOrders.value[selectedConversation.value.id]
})

const groupOrderItemsCount = computed(() => {
  return currentGroupOrder.value?.orderItems?.length || 0
})

// ========== 商家选择相关 ==========
const merchantSelectDialogVisible = ref(false)
const productSelectDialogVisible = ref(false)
const selectedMerchant = ref(null)
const orderingMerchant = ref(null)

const merchants = ref([
  // 模拟商家数据
])

// ========== 对话框状态管理 ==========
const actionPanelVisible = ref(false)
const groupDetailDialogVisible = ref(false)

const friends = ref([])
const currentGroupInfo = ref(null)

// ========== 加菜功能 ==========
const addDishDialogVisible = ref(false)
const addDishReviewVisible = ref(false)
const pendingReviewCount = ref(0)
const pendingPaymentCount = ref(0)

// 当前群订单ID
const currentGroupOrderId = computed(() => {
  return currentGroupOrder.value?.orderId || null
})

// 已点菜品列表
const orderedDishes = ref([])

// 可用菜品列表
const availableDishes = ref([])

// 饮食禁忌冲突
const allergyConflicts = ref([])

// ========== 加菜功能方法 ==========

// 加载已点菜品
const loadOrderedDishes = async () => {
  if (!currentGroupOrder.value) return

  try {
    // 从群订单中获取已点菜品
    orderedDishes.value = currentGroupOrder.value?.orderItems || []
  } catch (error) {
    console.error('加载已点菜品失败:', error)
  }
}

// 加载可用菜品
const loadAvailableDishes = async () => {
  if (!currentGroupOrder.value?.merchantId) return

  try {
    const response = await api.get(`/v1/dishes/merchant/${currentGroupOrder.value.merchantId}`)
    availableDishes.value = response.data.data || []
  } catch (error) {
    console.error('加载可用菜品失败:', error)
  }
}

// 检查饮食禁忌冲突
const checkAllergyConflicts = async (dishItems) => {
  try {
    const response = await api.post('/v1/add-dish/check-allergy', {
      groupOrderId: currentGroupOrderId.value,
      dishItems: dishItems.map(dish => ({
        dishId: dish.dishId,
        quantity: dish.quantity
      }))
    })

    if (response.data.data?.hasConflict) {
      allergyConflicts.value = response.data.data.conflicts || []
    } else {
      allergyConflicts.value = []
    }
  } catch (error) {
    console.error('检查饮食禁忌失败:', error)
    allergyConflicts.value = []
  }
}

// 打开加菜对话框
const openAddDishDialog = async () => {
  await loadOrderedDishes()
  await loadAvailableDishes()
  addDishDialogVisible.value = true
}

// 打开审核面板
const openAddDishReview = () => {
  addDishReviewVisible.value = true
}

// 打开待支付池（可选功能）
const openPendingPayment = () => {
  ElMessage.info('待支付加菜池功能开发中')
}

// 加菜成功回调
const handleAddDishSuccess = async () => {
  ElMessage.success('加菜请求已提交')
  await loadPendingReviewCount()
  await loadOrderedDishes()
}

// 加载待审核数量
const loadPendingReviewCount = async () => {
  if (!currentGroupOrderId.value) return

  try {
    const response = await api.get(`/v1/add-dish/review-list/${currentGroupOrderId.value}`)
    const reviewList = response?.data?.data || []
    pendingReviewCount.value = reviewList.length
  } catch (error) {
    console.error('加载待审核数量失败:', error)
    pendingReviewCount.value = 0
  }
}

// ========== 全局点击事件 ==========
const handleGlobalClick = () => {
  closeContextMenu()
}

// ========== 会话操作 ==========
const selectConversation = async (conversation) => {
  selectedConversation.value = conversation

  // 清空未读消息
  if (conversation.unreadCount > 0) {
    try {
      // 调用后端API清空未读数
      await api.post(`/v1/chat/sessions/${conversation.id}/unread-clear`, {
        userId: userId.value.toString()
      })

      conversation.unreadCount = 0
      ElMessage.success('消息已标记为已读')
    } catch (error) {
      console.error('标记已读失败:', error)
      // 即使API调用失败，也清空前端未读数（用户体验优先）
      conversation.unreadCount = 0
    }
  }

  await loadChatMessages(conversation.id)

  // 加载群订单信息（如果是群聊）
  if (conversation.type === 'group') {
    const pendingOrder = JSON.parse(sessionStorage.getItem('pendingOrder'))
    if (pendingOrder && pendingOrder.fromChat) {
      if (pendingOrder.groupName === conversation.name) {
        groupOrders.value[conversation.id] = {
          orderId: pendingOrder.orderId,
          groupId: conversation.id,
          groupName: pendingOrder.groupName,
          creator: pendingOrder.creator,
          members: pendingOrder.members,
          orderItems: pendingOrder.cartItems,
          totalAmount: pendingOrder.totalAmount,
          status: 'active',
          createTime: new Date().toISOString()
        }
        ElMessage.info('已恢复未完成的订单')
      }
    }
  }
}

// ========== 消息操作 ==========
const handleMessageCommand = async (command, message) => {
  // 如果是回复命令，为消息对象添加 senderName 属性
  if (command === 'reply') {
    const messageWithName = {
      ...message,
      senderName: message.fromId === userId.value.toString()
        ? '我'
        : selectedConversation.value?.name || message.fromId
    }
    await handleMessageCommandBase(command, messageWithName, selectedConversation)
  } else {
    await handleMessageCommandBase(command, message, selectedConversation)
  }
}

const handleForwardConfirm = async (data) => {
  // 设置转发目标
  selectedForwardTarget.value = data.targetId
  // 调用转发确认函数
  await confirmForward()
}

const sendMessage = async (content) => {
  if (!content.trim() || !selectedConversation.value) {
    return
  }

  console.log('📤 [sendMessage] 准备发送消息')
  console.log('📤 [sendMessage] 会话信息:', {
    id: selectedConversation.value.id,
    targetId: selectedConversation.value.targetId,
    groupId: selectedConversation.value.groupId,
    name: selectedConversation.value.name,
    type: selectedConversation.value.type
  })

  // ⭐ 对于群聊，使用 groupId 作为 toId
  // ⭐ 对于单聊，使用 targetId（对方的 userId）作为 toId
  let toId
  if (selectedConversation.value.type === 'group' && selectedConversation.value.groupId) {
    toId = selectedConversation.value.groupId
  } else if (selectedConversation.value.type === 'single' && selectedConversation.value.targetId) {
    toId = selectedConversation.value.targetId
  } else {
    // ❌ 不兼容旧数据，直接报错
    const errorInfo = {
      会话类型: selectedConversation.value.type,
      会话ID: selectedConversation.value.id,
      会话名称: selectedConversation.value.name,
      有无groupId: !!selectedConversation.value.groupId,
      有无targetId: !!selectedConversation.value.targetId
    }
    console.error('❌ [sendMessage] 会话数据不完整，无法发送消息:', errorInfo)
    ElMessage.error('会话数据异常，请重新选择会话')
    return
  }

  console.log('📤 [sendMessage] 使用的toId:', toId, '(原会话ID:', selectedConversation.value.id + ')')

  const messageData = {
    fromId: userId.value.toString(),
    toId: toId,  // ⭐ 修正：群聊使用 groupId，单聊使用target id
    sessionType: selectedConversation.value.type || 'single',  // 会话类型
    msgType: 'text',                                             // 消息类型
    content: content.trim()
  }

  if (replyingTo.value) {
    messageData.replyTo = replyingTo.value.id
    messageData.replyContent = replyingTo.value.content
    messageData.replyFromId = replyingTo.value.fromId

    // 确定回复消息的发送者显示名称
    if (replyingTo.value.fromId === userId.value.toString()) {
      // 回复自己的消息
      messageData.replyFromName = '我'
    } else {
      // 回复他人的消息，使用会话名称
      messageData.replyFromName = selectedConversation.value.name || replyingTo.value.fromId
    }
  }

  const tempMessage = {
    id: Date.now(),
    fromId: userId.value.toString(),
    toId: selectedConversation.value.id,
    msgType: messageData.msgType,
    content: messageData.content,
    replyTo: messageData.replyTo,
    replyContent: messageData.replyContent,
    replyFromId: messageData.replyFromId,
    replyFromName: messageData.replyFromName,
    createTime: new Date().toISOString(),
    formattedTime: '刚刚',
    status: 'sending'
  }

  chatMessages.value.push(tempMessage)
  chatHistory.value[selectedConversation.value.id] = chatMessages.value
  setTimeout(() => scrollToBottom(), 100)

  try {
    const response = await api.post('/v1/chat/messages', messageData)

    console.log('📤 [sendMessage] 发送成功，返回数据:', response.data)
    if (response.code === '200') {
      const sentMessage = response.data

      const index = chatMessages.value.findIndex((msg) => msg.id === tempMessage.id)
      if (index !== -1) {
        chatMessages.value[index] = {
          ...sentMessage,
          formattedTime: formatMessageTime(sentMessage.createTime || sentMessage.time),
          fromId: sentMessage.fromId || userId.value.toString(),
          status: 'success'
        }
      }

      updateConversationLastMessage(selectedConversation.value.id, sentMessage)

      if (replyingTo.value) {
        replyingTo.value = null
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error)

    const index = chatMessages.value.findIndex((msg) => msg.id === tempMessage.id)
    if (index !== -1) {
      chatMessages.value[index].status = 'failed'
      chatMessages.value[index].canResend = true
    }

    ElMessage.error('发送失败，请点击重发')
  }
}

// 发送图片消息
const sendImageMessage = async (fileInfo) => {
  if (!selectedConversation.value) {
    return
  }

  console.log('📤 [sendImageMessage] 准备发送图片消息')
  console.log('📤 [sendImageMessage] 会话信息:', {
    id: selectedConversation.value.id,
    targetId: selectedConversation.value.targetId,
    groupId: selectedConversation.value.groupId,
    name: selectedConversation.value.name,
    type: selectedConversation.value.type
  })

  // ⭐ 对于群聊，使用 groupId 作为 toId
  // ⭐ 对于单聊，使用 targetId（对方的 userId）作为 toId
  let toId
  if (selectedConversation.value.type === 'group' && selectedConversation.value.groupId) {
    toId = selectedConversation.value.groupId
  } else if (selectedConversation.value.type === 'single' && selectedConversation.value.targetId) {
    toId = selectedConversation.value.targetId
  } else {
    // ❌ 不兼容旧数据，直接报错
    const errorInfo = {
      会话类型: selectedConversation.value.type,
      会话ID: selectedConversation.value.id,
      会话名称: selectedConversation.value.name,
      有无groupId: !!selectedConversation.value.groupId,
      有无targetId: !!selectedConversation.value.targetId
    }
    console.error('❌ [sendImageMessage] 会话数据不完整，无法发送消息:', errorInfo)
    ElMessage.error('会话数据异常，请重新选择会话')
    return
  }

  console.log('📤 [sendImageMessage] 使用的toId:', toId, '(原会话ID:', selectedConversation.value.id + ')')

  // 创建临时消息，显示骨架屏
  const tempMessage = {
    id: Date.now(),
    fromId: userId.value.toString(),
    toId: toId,
    msgType: 'image',
    content: '[图片]',
    fileUrl: fileInfo.fileUrl,
    fullUrl: fileInfo.fullUrl,
    fileName: fileInfo.fileName,
    fileSize: fileInfo.fileSize,
    fileType: fileInfo.fileType,
    createTime: new Date().toISOString(),
    formattedTime: '刚刚',
    status: 'sending',
    isLoading: true  // 标记为加载中，用于显示骨架屏
  }

  chatMessages.value.push(tempMessage)
  chatHistory.value[selectedConversation.value.id] = chatMessages.value
  setTimeout(() => scrollToBottom(), 100)

  try {
    const messageData = {
      fromId: userId.value.toString(),
      toId: toId,  // ⭐ 修正：群聊使用 groupId，单聊使用会话 id
      sessionType: selectedConversation.value.type || 'single',  // 会话类型
      msgType: 'image',                                            // 消息类型
      content: '[图片]',
      fileUrl: fileInfo.fileUrl,
      fileName: fileInfo.fileName,
      fileSize: fileInfo.fileSize,
      fileType: fileInfo.fileType
    }

    console.log('📤 [sendImageMessage] 发送消息数据:', messageData)

    const response = await api.post('/v1/chat/messages', messageData)

    console.log('📥 [sendImageMessage] 收到后端响应:', response)

    if (response.code === '200') {
      const sentMessage = response.data
      console.log('✅ [sendImageMessage] 后端返回的消息:', {
        原始数据: sentMessage,
        msgId: sentMessage.msgId || sentMessage.id,
        fileUrl: sentMessage.fileUrl,
        fullUrl: sentMessage.fullUrl
      })

      const index = chatMessages.value.findIndex((msg) => msg.id === tempMessage.id)
      console.log('🔍 [sendImageMessage] 查找临时消息:', {
        临时ID: tempMessage.id,
        找到索引: index,
        当前消息数: chatMessages.value.length
      })

      if (index !== -1) {
        // ⭐ 确保使用正确的消息ID
        const finalMessage = {
          ...sentMessage,
          id: sentMessage.msgId || sentMessage.id || tempMessage.id,  // 优先使用后端返回的ID
          // 保留fullUrl，因为后端返回的数据可能没有这个字段
          fullUrl: sentMessage.fullUrl || tempMessage.fullUrl,
          formattedTime: formatMessageTime(sentMessage.createTime || sentMessage.time),
          fromId: sentMessage.fromId || userId.value.toString(),
          status: 'success',
          isLoading: false
        }

        console.log('✅ [sendImageMessage] 更新消息:', {
          旧消息: chatMessages.value[index],
          新消息: finalMessage
        })

        chatMessages.value[index] = finalMessage
      }

      updateConversationLastMessage(selectedConversation.value.id, sentMessage)
    }
  } catch (error) {
    console.error('发送图片消息失败:', error)

    const index = chatMessages.value.findIndex((msg) => msg.id === tempMessage.id)
    if (index !== -1) {
      chatMessages.value[index].status = 'failed'
      chatMessages.value[index].canResend = true
      chatMessages.value[index].isLoading = false
    }

    ElMessage.error('发送失败，请点击重发')
  }
}

// 发送文件消息
const sendFileMessage = async (fileInfo) => {
  if (!selectedConversation.value) {
    return
  }

  console.log('📤 [sendFileMessage] 准备发送文件消息')
  console.log('📤 [sendFileMessage] 会话信息:', {
    id: selectedConversation.value.id,
    targetId: selectedConversation.value.targetId,
    groupId: selectedConversation.value.groupId,
    name: selectedConversation.value.name,
    type: selectedConversation.value.type
  })

  // ⭐ 对于群聊，使用 groupId 作为 toId
  // ⭐ 对于单聊，使用 targetId（对方的 userId）作为 toId
  let toId
  if (selectedConversation.value.type === 'group' && selectedConversation.value.groupId) {
    toId = selectedConversation.value.groupId
  } else if (selectedConversation.value.type === 'single' && selectedConversation.value.targetId) {
    toId = selectedConversation.value.targetId
  } else {
    // ❌ 不兼容旧数据，直接报错
    const errorInfo = {
      会话类型: selectedConversation.value.type,
      会话ID: selectedConversation.value.id,
      会话名称: selectedConversation.value.name,
      有无groupId: !!selectedConversation.value.groupId,
      有无targetId: !!selectedConversation.value.targetId
    }
    console.error('❌ [sendFileMessage] 会话数据不完整，无法发送消息:', errorInfo)
    ElMessage.error('会话数据异常，请重新选择会话')
    return
  }

  console.log('📤 [sendFileMessage] 使用的toId:', toId, '(原会话ID:', selectedConversation.value.id + ')')

  // 创建临时消息，显示加载状态
  const tempMessage = {
    id: Date.now(),
    fromId: userId.value.toString(),
    toId: toId,
    msgType: 'file',
    content: `[文件] ${fileInfo.fileName}`,
    fileUrl: fileInfo.fileUrl,
    fullUrl: fileInfo.fullUrl,
    fileName: fileInfo.fileName,
    fileSize: fileInfo.fileSize,
    fileType: fileInfo.fileType,
    createTime: new Date().toISOString(),
    formattedTime: '刚刚',
    status: 'sending'
  }

  chatMessages.value.push(tempMessage)
  chatHistory.value[selectedConversation.value.id] = chatMessages.value
  setTimeout(() => scrollToBottom(), 100)

  try {
    const messageData = {
      fromId: userId.value.toString(),
      toId: toId,  // ⭐ 修正：群聊使用 groupId，单聊使用会话 id
      sessionType: selectedConversation.value.type || 'single',  // 会话类型
      msgType: 'file',                                             // 消息类型
      content: `[文件] ${fileInfo.fileName}`,
      fileUrl: fileInfo.fileUrl,
      fileName: fileInfo.fileName,
      fileSize: fileInfo.fileSize,
      fileType: fileInfo.fileType
    }

    const response = await api.post('/v1/chat/messages', messageData)

    if (response.code === '200') {
      const sentMessage = response.data

      const index = chatMessages.value.findIndex((msg) => msg.id === tempMessage.id)
      if (index !== -1) {
        chatMessages.value[index] = {
          ...sentMessage,
          formattedTime: formatMessageTime(sentMessage.createTime || sentMessage.time),
          fromId: sentMessage.fromId || userId.value.toString(),
          status: 'success'
        }
      }

      updateConversationLastMessage(selectedConversation.value.id, sentMessage)
    }
  } catch (error) {
    console.error('发送文件消息失败:', error)

    const index = chatMessages.value.findIndex((msg) => msg.id === tempMessage.id)
    if (index !== -1) {
      chatMessages.value[index].status = 'failed'
      chatMessages.value[index].canResend = true
    }

    ElMessage.error('发送失败，请点击重发')
  }
}

const resendMessage = async (failedMessage) => {
  try {
    // ⭐ 确定正确的 toId
    let toId
    if (selectedConversation.value.type === 'group' && selectedConversation.value.groupId) {
      toId = selectedConversation.value.groupId
    } else if (selectedConversation.value.type === 'single' && selectedConversation.value.targetId) {
      toId = selectedConversation.value.targetId
    } else {
      // ❌ 不兼容旧数据，直接报错
      const errorInfo = {
        会话类型: selectedConversation.value.type,
        会话ID: selectedConversation.value.id,
        会话名称: selectedConversation.value.name,
        有无groupId: !!selectedConversation.value.groupId,
        有无targetId: !!selectedConversation.value.targetId
      }
      console.error('❌ [resendMessage] 会话数据不完整，无法重发消息:', errorInfo)
      ElMessage.error('会话数据异常，请重新选择会话')
      return
    }

    const messageData = {
      fromId: userId.value.toString(),
      toId: toId, // ⭐ 使用正确的 toId
      sessionType: selectedConversation.value.type || 'single',
      msgType: failedMessage.msgType || 'text',
      content: failedMessage.content
    }

    const response = await api.post('/v1/chat/messages', messageData)

    if (response.code === '200') {
      const index = chatMessages.value.findIndex((msg) => msg.id === failedMessage.id)
      if (index !== -1) {
        chatMessages.value.splice(index, 1)
      }

      const sentMessage = response.data
      chatMessages.value.push(sentMessage)
      chatHistory.value[selectedConversation.value.id] = chatMessages.value

      selectedConversation.value.lastMessage = sentMessage.content
      selectedConversation.value.time = sentMessage.time

      ElMessage.success('消息重发成功')
    }
  } catch (error) {
    console.error('重发消息失败:', error)
    ElMessage.error('重发消息失败，请稍后重试')
  }
}

// ========== 对话框操作 ==========
const openActionPanelWithTab = () => {
  // 打开统一操作面板
  actionPanelVisible.value = true
}

// ========== 统一操作面板事件处理 ==========
const startChatFromPanel = (user) => {
  console.log('💬 [startChatFromPanel] 开始聊天:', user)
  console.log('💬 [startChatFromPanel] 当前会话列表:', conversations.value.map(c => ({ id: c.id, name: c.name })))

  const existingConversation = conversations.value.find((conv) => conv.id === user.id)

  if (existingConversation) {
    console.log('💬 [startChatFromPanel] 会话已存在，直接选中:', existingConversation)
    selectedConversation.value = existingConversation
  } else {
    console.log('💬 [startChatFromPanel] 会话不存在，创建新会话')
    const newConversation = {
      ...user,
      lastMessage: '开始聊天吧！',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }

    conversations.value.unshift(newConversation)
    console.log('💬 [startChatFromPanel] 新会话已添加，当前会话数量:', conversations.value.length)
    selectedConversation.value = newConversation

    chatHistory.value[newConversation.id] = []
  }

  ElMessage.success(`已开始与 ${user.name} 的对话`)
}

const createGroupFromPanel = async (data) => {
  try {
    // 0. 验证成员列表中不能包含当前用户
    const hasCurrentUser = data.members.some(member => member.id === userId.value.toString())
    if (hasCurrentUser) {
      ElMessage.error('不能将自己添加为群成员')
      return
    }

    // 1. 先调用后端API创建群
    const groupResponse = await api.post('/v1/groups', {
      groupName: data.name.trim(),
      creatorId: userId.value,
      memberCount: data.members.length + 1
    })

    if (groupResponse.code !== '200' || !groupResponse.data) {
      ElMessage.error('创建群失败，请稍后重试')
      return
    }

    console.log('📦 [创建群] 后端返回数据:', groupResponse.data)
    const groupId = groupResponse.data.groupId || groupResponse.data.id
    const sessionId = groupResponse.data.sessionId
    console.log('📦 [创建群] 提取的 groupId:', groupId)
    console.log('📦 [创建群] 提取的 sessionId:', sessionId)
    const groupName = data.name.trim()

    // 2. 将创建者添加到群成员关系
    try {
      await api.post('/v1/contacts/groups/join', {
        userId: userId.value.toString(),
        targetId: groupId.toString(),
        relationType: 'group',
        status: 'normal'
      })
    } catch (error) {
      console.error('添加创建者到群成员关系失败:', error)
      ElMessage.error('添加群成员关系失败，请稍后重试')
      return
    }

    // 3. 将选中的成员添加到群成员关系
    const memberJoinResults = []
    for (const member of data.members) {
      try {
        await api.post('/v1/contacts/groups/join', {
          userId: member.id.toString(),
          targetId: groupId.toString(),
          relationType: 'group',
          status: 'normal'
        })
        memberJoinResults.push({ member: member.name, success: true })
      } catch (error) {
        console.error(`添加成员 ${member.name} 到群失败:`, error)
        memberJoinResults.push({ member: member.name, success: false, error })
      }
    }

    // 检查是否有成员添加失败
    const failedMembers = memberJoinResults.filter(r => !r.success)
    if (failedMembers.length > 0) {
      ElMessage.warning(`部分成员添加失败: ${failedMembers.map(f => f.member).join(', ')}`)
    }

    // 4. 为所有成员创建聊天会话记录（并行执行以提高性能）
    const sessionCreatePromises = []
    const sessionResults = []

    // 为创建者创建会话
    const creatorSessionPromise = api.post('/v1/chat/sessions', {
      userId: userId.value.toString(),
      sessionId: sessionId,
      sessionType: 'group',
      sessionName: groupName,
      avatar: '👥',
      memberCount: data.members.length + 1,
      groupId: groupId.toString() // ⭐ 传入groupId
    }).then(response => {
      sessionResults.push({ user: '我', success: response.code === '200', response })
      return response
    }).catch(error => {
      sessionResults.push({ user: '我', success: false, error })
      throw error
    })
    sessionCreatePromises.push(creatorSessionPromise)

    // 为每个成员创建会话
    for (const member of data.members) {
      const memberSessionPromise = api.post('/v1/chat/sessions', {
        userId: member.id.toString(),
        sessionId: sessionId,
        sessionType: 'group',
        sessionName: groupName,
        avatar: '👥',
        memberCount: data.members.length + 1,
        groupId: groupId.toString() // ⭐ 传入groupId
      }).then(response => {
        sessionResults.push({ user: member.name, success: response.code === '200', response })
        return response
      }).catch(error => {
        sessionResults.push({ user: member.name, success: false, error })
        throw error
      })
      sessionCreatePromises.push(memberSessionPromise)
    }

    // 等待所有会话创建完成，任何一个失败都会抛出错误
    try {
      await Promise.all(sessionCreatePromises)
    } catch (error) {
      console.error('会话创建失败:', error)

      // 分析失败的会话
      const failedSessions = sessionResults.filter(r => !r.success)

      if (failedSessions.length > 0) {
        ElMessage.error(
          `会话创建失败: ${failedSessions.map(f => f.user).join(', ')}。请稍后重试`
        )
        return
      }
    }

    // 验证会话创建结果
    const failedSessions = sessionResults.filter(r => !r.success)
    if (failedSessions.length > 0) {
      ElMessage.error(
        `部分会话创建失败: ${failedSessions.map(f => f.user).join(', ')}。群聊可能无法正常使用`
      )
      return
    }

    // console.log('✅ 所有群聊会话记录创建成功', sessionResults)

    // 5. 从服务器刷新会话列表，确保数据同步
    console.log('🔄 [createGroupFromPanel] 准备刷新会话列表')
    console.log('🔄 [createGroupFromPanel] 刷新前会话数量:', conversations.value.length)
    const refreshSuccess = await fetchConversations()
    if (!refreshSuccess) {
      ElMessage.warning('群聊已创建，但会话列表刷新失败，请手动刷新')
    }

    // 6. 查找新创建的群聊会话
    console.log('🔍 [createGroupFromPanel] 查找新创建的群聊会话, sessionId:', sessionId)
    const newGroupConversation = conversations.value.find(c => c.id === sessionId)
    if (newGroupConversation) {
      console.log('✅ [createGroupFromPanel] 找到新创建的群聊会话:', newGroupConversation)
      // ⭐ 在会话对象中添加 groupId 字段（用于后续获取群信息）
      newGroupConversation.groupId = groupId

      selectedConversation.value = newGroupConversation
      chatHistory.value[sessionId] = []

      // 添加系统消息
      const systemMsg = {
        id: 1,
        sender: '系统',
        content: `群聊 "${groupName}" 已创建`,
        time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }
      chatHistory.value[sessionId].push(systemMsg)
    } else {
      console.error('❌ [createGroupFromPanel] 未找到新创建的群聊会话:', sessionId)
      console.error('❌ [createGroupFromPanel] 当前会话列表:', conversations.value.map(c => ({ id: c.id, name: c.name })))
      console.error('❌ [createGroupFromPanel] groupId:', groupId)
      ElMessage.error('群聊创建成功，但无法打开会话')
      return
    }

    ElMessage.success(`群聊 "${groupName}" 已创建，共 ${data.members.length + 1} 人`)
  } catch (error) {
    console.error('❌ [createGroupFromPanel] 创建群失败:', error)
    ElMessage.error(`创建群失败: ${error.message || '请稍后重试'}`)
  }
}

const handleAddFriendFromPanel = (user) => {
  ElMessage.success(`已向 ${user.name} 发送好友申请`)
  fetchFriends()
}

const openGroupDetail = async () => {
  if (!selectedConversation.value || selectedConversation.value.type !== 'group') return

  try {
    // ⭐ 优先使用 groupId（如果存在），否则使用 id
    const groupId = selectedConversation.value.groupId || selectedConversation.value.id

    console.log('📋 [群详情] 获取群信息，groupId:', groupId)

    // 1. 并行获取群信息和群成员列表
    const [groupResponse, membersResponse] = await Promise.all([
      api.get(`/v1/groups/${groupId}`),
      api.get(`/v1/contacts/groups/${groupId}/members`)
    ])

    if (groupResponse.code !== '200' || !groupResponse.data) {
      ElMessage.error('获取群信息失败')
      return
    }

    if (membersResponse.code !== '200') {
      ElMessage.error('获取群成员失败')
      return
    }

    const groupData = groupResponse.data
    const membersData = membersResponse.data || []

    // 2. 获取每个成员的用户信息
    const membersWithNames = await Promise.all(
      membersData.map(async (contact) => {
        try {
          const userResponse = await api.get(`/v1/users/${contact.userId}`)
          const userData = userResponse.data

          // 判断是否是当前用户
          const isCurrentUser = contact.userId === userId.value.toString()

          return {
            id: contact.userId,
            name: userData.nickname || userData.username || '未知用户',
            role: contact.role || 'member',
            isCurrentUser
          }
        } catch (error) {
          console.error(`获取成员 ${contact.userId} 信息失败:`, error)
          return {
            id: contact.userId,
            name: '未知用户',
            role: contact.role || 'member',
            isCurrentUser: contact.userId === userId.value.toString()
          }
        }
      })
    )

    // 3. 获取创建人信息
    let creatorName = '未知用户'
    if (groupData.creatorId) {
      try {
        const creatorResponse = await api.get(`/v1/users/${groupData.creatorId}`)
        const creatorData = creatorResponse.data
        creatorName = groupData.creatorId === userId.value.toString()
          ? '我'
          : (creatorData.nickname || creatorData.username || '未知用户')
      } catch (error) {
        console.error('获取创建人信息失败:', error)
        creatorName = groupData.creatorId === userId.value.toString() ? '我' : '未知用户'
      }
    }

    // 4. 格式化创建时间
    let formattedCreateTime = '未知时间'
    if (groupData.createTime) {
      try {
        const createTime = new Date(groupData.createTime)
        formattedCreateTime = createTime.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        })
      } catch (error) {
        console.error('格式化时间失败:', error)
      }
    }

    // 5. 组装群详情数据
    currentGroupInfo.value = {
      id: groupData.id,
      name: groupData.groupName || selectedConversation.value.name,
      avatar: selectedConversation.value.avatar,
      memberCount: membersWithNames.length, // 使用实际成员数量
      members: membersWithNames.map(m => m.name), // 成员名称列表
      memberDetails: membersWithNames, // 保存详细信息供后续使用
      creator: creatorName,
      creatorId: groupData.creatorId,
      createdAt: formattedCreateTime
    }

    groupDetailDialogVisible.value = true
  } catch (error) {
    console.error('获取群详情失败:', error)
    ElMessage.error('获取群详情失败，请稍后重试')
  }
}

// ========== 群订单操作 ==========
const createGroupOrder = () => {
  if (!selectedConversation.value) return

  const newOrder = {
    orderId: Date.now(),
    groupId: selectedConversation.value.id,
    groupName: selectedConversation.value.name,
    creator: '我',
    members: ['我'],
    orderItems: [],
    totalAmount: 0,
    status: 'active',
    createTime: new Date().toISOString()
  }

  groupOrders.value[selectedConversation.value.id] = newOrder
  ElMessage.success('群订单已创建')
}

const joinGroupOrder = () => {
  ElMessage.info('已加入群订单')
}

const openMerchantSelectDialog = async () => {
  if (!selectedConversation.value || !hasGroupOrder.value) {
    ElMessage.error('请先创建群订单')
    return
  }

  if (orderingMerchant.value) {
    selectedMerchant.value = orderingMerchant.value
    productSelectDialogVisible.value = true
  } else {
    // 从后端获取商家列表
    await fetchMerchants()
    merchantSelectDialogVisible.value = true
  }
}

const selectMerchant = async (merchant) => {
  selectedMerchant.value = merchant
  orderingMerchant.value = merchant
  merchantSelectDialogVisible.value = false

  if (selectedConversation.value && hasGroupOrder.value) {
    const currentOrder = groupOrders.value[selectedConversation.value.id]
    currentOrder.merchantId = merchant.id
    currentOrder.merchantName = merchant.name
  }

  // 从后端获取商家菜品数据
  await fetchMerchantProducts(merchant.id)
  productSelectDialogVisible.value = true
}

// 群订单购物车
const groupOrderCart = ref({})

/**
 * 添加商品到购物车
 */
const addProductToCart = ({ product, customization }) => {
  if (!selectedConversation.value || !hasGroupOrder.value) {
    ElMessage.warning('请先创建群订单')
    return
  }

  const currentOrder = groupOrders.value[selectedConversation.value.id]
  if (!currentOrder) return

  // 构建购物车项
  const cartItemId = `${product.id}_${Date.now()}`
  const cartItem = {
    id: cartItemId,
    productId: product.id,
    productName: product.name,
    productPrice: product.price || 0,
    productImage: product.image,
    quantity: customization.quantity || 1,
    optionalIngredients: customization.optionalIngredients || [],
    remark: customization.remark || '',
    // 计算小计
    subtotal: (product.price || 0) * (customization.quantity || 1) +
      (customization.optionalIngredients || []).reduce((sum, ing) => sum + (ing.price || 0), 0)
  }

  // 添加到购物车
  if (!groupOrderCart.value[currentOrder.orderId]) {
    groupOrderCart.value[currentOrder.orderId] = []
  }
  groupOrderCart.value[currentOrder.orderId].push(cartItem)

  // 更新群订单的商品项
  if (!currentOrder.orderItems) {
    currentOrder.orderItems = []
  }
  currentOrder.orderItems.push(cartItem)

  ElMessage.success(`已添加 ${customization.quantity || 1}份 ${product.name}`)
}

/**
 * 确认商品选择
 */
const confirmProductSelection = () => {
  productSelectDialogVisible.value = false
  ElMessage.success('商品选择完成')
}

const changeMerchant = () => {
  merchantSelectDialogVisible.value = true
}

const goToOrderConfirmation = () => {
  ElMessage.info('跳转到订单确认页面')
}

// ========== 监听群订单抽屉状态 ==========
watch(orderDrawerVisible, async (newVal) => {
  if (newVal) {
    // 抽屉打开时加载待审核数量和已点菜品
    await loadPendingReviewCount()
    await loadOrderedDishes()
  }
})

// ========== 监听路由变化 ==========
watch(() => route.query, async (newQuery) => {
  // 当路由参数中的 friendId 变化时，处理从联系人页面跳转
  if (newQuery.friendId) {
    console.log('📍 [Chat] 检测到路由参数变化:', newQuery)
    await handleChatFromContact()
  }
}, { deep: true })

// ========== 生命周期 ==========
onMounted(async () => {
   console.log('🚀 [Chat] Chat组件挂载，开始初始化')
  try {
    // 先从本地加载聊天历史缓存（同步函数）
    loadChatHistoryFromLocal()
    console.log('📦 [Chat] 本地缓存加载完成', Object.keys(chatHistory.value))

    const conversationsResponse = await api.get(`/v1/chat/users/${userId.value}/chat-sessions`)

    console.log('📡 [Chat] 会话列表API响应', {
      code: conversationsResponse.code,
      dataLength: conversationsResponse.data?.length,
      userId: userId.value,
      sessionIds: conversationsResponse.data?.map(c => ({ id: c.id, name: c.name, groupId: c.groupId }))
    })

    await fetchFriends()

    if (conversationsResponse.code === '200') {
      conversations.value = conversationsResponse.data
      console.log(`👥 [Chat] 会话列表已更新 - 共 ${conversations.value.length} 个会话`)
      console.log('👥 [Chat] 会话详情:', conversations.value.map(c => ({
        id: c.id,
        name: c.name,
        type: c.type,
        groupId: c.groupId
      })))

      if (sortedConversations.value.length > 0) {
        selectedConversation.value = sortedConversations.value[0]
        console.log(`✅ [Chat] 自动选择第一个会话 - ID: ${selectedConversation.value.id}, 名称: ${selectedConversation.value.name}`)
        await loadChatMessages(selectedConversation.value.id)
      } else {
        console.warn('⚠️ [Chat] 会话列表为空，没有可显示的会话')
      }
    } else {
      console.error(`❌ [Chat] 获取会话列表失败 - code: ${conversationsResponse.code}`)
    }

    initWebSocket()

    // 处理从联系人页面跳转到聊天页面
    await handleChatFromContact()
  } catch (error) {
    console.error('❌ [Chat] 加载数据失败:', error)
    ElMessage.error('加载数据失败，请稍后重试')
  }
})

onBeforeUnmount(() => {
  closeWebSocket()
})

const fetchFriends = async () => {
  try {
    const response = await api.get(`/v1/contacts/friends?userId=${userId.value}`)
    console.log('🚀 [Chat] 获取好友列表, response', response)
    if (response.code === '200') {
      // 为每个好友获取详细信息
      const friendsWithDetails = await Promise.all(
        response.data.map(async (contact) => {
          try {
            const userResponse = await api.get(`/v1/users/${contact.targetId}`)
            const userData = userResponse.data

            // 判断头像是否为有效的图片 URL
            const isValidAvatarUrl = (avatar) => {
              if (!avatar) return false
              // 只接受 http://、https:// 或 data:image 开头的 URL
              return /^https?:\/\//.test(avatar) || /^data:image/.test(avatar)
            }

            const avatar = isValidAvatarUrl(userData.avatar) ? userData.avatar : '👤'

            return {
              id: contact.targetId,
              name: userData.nickname || userData.username || '好友',
              avatar: avatar,
              lastMessage: '',
              time: '',
              unreadCount: 0,
              type: 'friend'
            }
          } catch (error) {
            console.error(`获取好友 ${contact.targetId} 信息失败:`, error)
            // 如果获取用户信息失败，返回基本信息
            return {
              id: contact.targetId,
              name: '好友',
              avatar: '👤',
              lastMessage: '',
              time: '',
              unreadCount: 0,
              type: 'friend'
            }
          }
        })
      )

      friends.value = friendsWithDetails
    }
  } catch (error) {
    console.error('获取好友列表失败:', error)
  }
}

// 获取会话列表
const fetchConversations = async () => {
  try {
    console.log('📡 [fetchConversations] 开始获取会话列表')
    console.log('📡 [fetchConversations] 当前会话数量:', conversations.value.length)
    console.log('📡 [fetchConversations] 当前会话IDs:', conversations.value.map(c => c.id))

    const conversationsResponse = await api.get(`/v1/chat/users/${userId.value}/chat-sessions`)

    console.log('📡 [fetchConversations] 后端返回:', {
      code: conversationsResponse.code,
      dataLength: conversationsResponse.data?.length,
      sessionIds: conversationsResponse.data?.map(c => ({ id: c.id, name: c.name, groupId: c.groupId }))
    })

    if (conversationsResponse.code === '200') {
      const oldConversationIds = new Set(conversations.value.map(c => c.id))

      // 检测是否有新增的会话
      const addedConversations = conversationsResponse.data.filter(c => !oldConversationIds.has(c.id))
      if (addedConversations.length > 0) {
        console.log('➕ [fetchConversations] 检测到新增会话:', addedConversations.map(c => ({ id: c.id, name: c.name })))
      }

      // 检测是否有被移除的会话
      const newConversationIds = new Set(conversationsResponse.data.map(c => c.id))
      const removedConversations = conversations.value.filter(c => !newConversationIds.has(c.id))
      if (removedConversations.length > 0) {
        console.log('➖ [fetchConversations] 检测到移除会话:', removedConversations.map(c => ({ id: c.id, name: c.name })))
      }

      conversations.value = conversationsResponse.data
      console.log(`✅ [fetchConversations] 会话列表已更新 - 共 ${conversations.value.length} 个会话`)
      return true
    } else {
      console.error(`❌ [fetchConversations] 获取会话列表失败 - code: ${conversationsResponse.code}`)
      return false
    }
  } catch (error) {
    console.error('❌ [fetchConversations] 获取会话列表失败:', error)
    return false
  }
}

// ========== 处理从联系人页面跳转 ==========
/**
 * 处理从联系人页面跳转到聊天页面
 * 检查是否存在会话，如果不存在则创建新会话
 */
const handleChatFromContact = async () => {
  const friendId = route.query.friendId
  const friendName = route.query.friendName

  if (!friendId) {
    console.log('💬 [handleChatFromContact] 没有friendId参数，跳过处理')
    return
  }

  console.log('💬 [handleChatFromContact] 从联系人页面跳转:', { friendId, friendName })

  // 检查会话列表中是否已存在与该好友的会话
  const existingConversation = conversations.value.find(
    (conv) => conv.type === 'single' && conv.id === friendId.toString()
  )

  if (existingConversation) {
    console.log('💬 [handleChatFromContact] 会话已存在，直接选中:', existingConversation)
    selectedConversation.value = existingConversation
    await loadChatMessages(existingConversation.id)
  } else {
    console.log('💬 [handleChatFromContact] 会话不存在，创建新会话')

    try {
      // 调用后端API创建会话
      const sessionId = friendId.toString() // 单聊的sessionId就是对方的userId
      const response = await api.post('/v1/chat/sessions', {
        userId: userId.value.toString(),
        sessionId: sessionId,
        sessionType: 'single',
        sessionName: friendName || friendId.toString(),
        avatar: '👤'
      })

      if (response.code === '200') {
        console.log('✅ [handleChatFromContact] 会话创建成功, 响应数据:', response.data)

        // 刷新会话列表
        const refreshSuccess = await fetchConversations()

        if (refreshSuccess) {
          console.log('🔄 [handleChatFromContact] 会话列表刷新成功，当前会话列表:', conversations.value.map(c => ({ id: c.id, name: c.name })))

          // 查找新创建的会话 - 使用多种匹配方式
          let newConversation = conversations.value.find((conv) => conv.id === sessionId)

          // 如果没找到，尝试通过会话名称匹配
          if (!newConversation && friendName) {
            newConversation = conversations.value.find((conv) => conv.name === friendName && conv.type === 'single')
            console.log('🔍 [handleChatFromContact] 通过名称匹配会话:', newConversation)
          }

          // 如果还是没找到，直接创建一个临时会话对象
          if (!newConversation) {
            console.warn('⚠️ [handleChatFromContact] 会话列表中未找到，创建临时会话对象')
            newConversation = {
              id: sessionId,
              name: friendName || friendId.toString(),
              type: 'single',
              avatar: '👤',
              lastMessage: '开始聊天吧！',
              time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
              unreadCount: 0,
              targetId: friendId.toString() // ⭐ 保存对方的 userId，用于发送消息
            }
            // 添加到会话列表的开头
            conversations.value.unshift(newConversation)
            console.log('➕ [handleChatFromContact] 已创建临时会话并添加到列表')
          } else {
            // ⭐ 如果找到了会话，确保它有 targetId 字段
            if (!newConversation.targetId && newConversation.type === 'single') {
              newConversation.targetId = friendId.toString()
              console.log('🔧 [handleChatFromContact] 为会话添加 targetId:', friendId)
            }
          }

          console.log('✅ [handleChatFromContact] 找到新创建的会话:', newConversation)
          selectedConversation.value = newConversation
          chatHistory.value[newConversation.id] = []
          ElMessage.success(`已开始与 ${friendName || '好友'} 的对话`)
        } else {
          console.error('❌ [handleChatFromContact] 刷新会话列表失败')
          ElMessage.error('刷新会话列表失败')
        }
      } else {
        console.error('❌ [handleChatFromContact] 创建会话失败:', response)
        ElMessage.error('创建会话失败')
      }
    } catch (error) {
      console.error('❌ [handleChatFromContact] 创建会话异常:', error)
      ElMessage.error('创建会话失败')
    }
  }

  // 清除路由参数，避免重复处理
  router.replace({ query: {} })
}

// 获取商家列表
const fetchMerchants = async () => {
  try {
    ElMessage.info('正在加载商家列表...')
    const response = await api.get(MERCHANT_API.LIST)

    if (response.code === '200' || response.data) {
      merchants.value = response.data || []
      // console.log(`🏪 [Chat] 商家列表已加载 - 共 ${merchants.value.length} 个商家`)
      ElMessage.success(`已加载 ${merchants.value.length} 个商家`)
    } else {
      ElMessage.error('获取商家列表失败')
    }
  } catch (error) {
    console.error('❌ [Chat] 获取商家列表失败:', error)
    ElMessage.error('获取商家列表失败，请稍后重试')
  }
}

// 获取商家菜品（菜单）
const fetchMerchantProducts = async (merchantId) => {
  try {
    ElMessage.info('正在加载菜品信息...')
    const response = await api.get(`/v1/menus/merchants/${merchantId}/menu`)

    if (response.code === '200' && response.data) {
      const menuData = response.data
      if (selectedMerchant.value) {
        // MenuController返回的是菜单数组,每个菜单包含dishes
        // 需要合并所有菜单的菜品
        let products = []

        if (Array.isArray(menuData)) {
          // 遍历所有菜单,提取菜品
          menuData.forEach(menu => {
            if (menu.dishes && Array.isArray(menu.dishes)) {
              products = products.concat(menu.dishes)
            }
          })
        }

        console.log(`📦 [Chat] 从 ${menuData.length} 个菜单中加载了 ${products.length} 个菜品`)

        // 处理商品数据，确保包含必选食材、可选食材等信息
        selectedMerchant.value.products = products.map(product => ({
          ...product,
          // 确保基本字段存在
          id: product.id || product.dishId || Date.now() + Math.random(),
          name: product.name || product.dishName || '未命名商品',
          price: product.price || 0,
          description: product.description || product.desc || '',
          image: product.image || product.img || product.dishImg || null,
          category: product.category || '其他',
          status: product.status !== undefined ? product.status : 'available',
          // 处理必选食材
          requiredIngredients: product.requiredIngredients || [],
          // 处理可选食材
          optionalIngredients: (product.optionalIngredients || []).map(ing => {
            if (typeof ing === 'string') {
              return {
                id: `ing_${Date.now()}_${Math.random()}`,
                name: ing,
                price: 0,
                selected: false
              }
            }
            return {
              id: ing.id || `ing_${Date.now()}_${Math.random()}`,
              name: ing.name || ing.ingredientName || '',
              price: ing.price || ing.extraPrice || 0,
              selected: ing.selected || false
            }
          }),
          // 营养信息
          nutritionInfo: product.nutritionInfo || {
            calories: product.calories || product.calorie || 0,
            protein: product.protein || 0,
            fat: product.fat || 0,
            carbohydrate: product.carbohydrate || 0
          },
          // 注意事项
          allergyInfo: product.allergyInfo || product.allergens || [],
          tips: product.tips || ''
        }))
      }
      ElMessage.success(`已加载 ${selectedMerchant.value?.products?.length || 0} 个菜品`)
    } else {
      console.error('❌ [Chat] 获取菜品失败:', response)
      ElMessage.error('获取菜品信息失败')
    }
  } catch (error) {
    console.error('❌ [Chat] 获取商家菜品失败:', error)
    ElMessage.error('获取菜品信息失败，请稍后重试')
  }
}
</script>

<style scoped lang="less">
.chat-container {
  padding: 16px;
  background-color: #f5f7fa;
  height: 85vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .chat-content {
    display: flex;
    gap: 8px;
    flex: 1;
    min-height: 0;
    overflow: hidden;

    // 拖动时的全局优化
    &.is-resizing {
      // 禁用用户选择，提升性能
      user-select: none;
      cursor: col-resize;

      // 拖动时优化子元素渲染
      * {
        pointer-events: none;
      }
    }

    .conversation-list-wrapper {
      min-width: 220px;
      height: 100%;
      display: flex;
      flex-direction: column;
      border: 1px solid #e4e7ed;
      border-radius: 12px;
      background-color: #fff;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      transition: box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1),
                  transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      transform: translateY(0);

      &:hover {
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
        transform: translateY(-4px);
      }

      // 拖动时添加弹性反馈动画
      &.is-resizing {
        animation: subtle-pulse 1.5s ease-in-out infinite;
      }
    }

    @keyframes subtle-pulse {
      0%, 100% {
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      }
      50% {
        box-shadow: 0 4px 25px rgba(64, 158, 255, 0.2);
      }
    }

    .resize-divider {
      width: 8px;
      height: 100%;
      background: linear-gradient(
        90deg,
        transparent 0%,
        #e4e7ed 40%,
        #c0c4cc 50%,
        #e4e7ed 60%,
        transparent 100%
      );
      cursor: col-resize;
      position: relative;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      flex-shrink: 0;
      align-self: center;
      border-radius: 4px;

      // 分隔条中间的拖动手柄样式 - 使用虚线效果
      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 2px;
        height: 60px;
        background: repeating-linear-gradient(
          to bottom,
          #909399 0px,
          #909399 4px,
          transparent 4px,
          transparent 8px
        );
        border-radius: 1px;
        opacity: 0.6;
        transition: all 0.3s ease;
      }

      &:hover {
        background: linear-gradient(
          90deg,
          transparent 0%,
          #dcdfe6 40%,
          #b0b4bc 50%,
          #dcdfe6 60%,
          transparent 100%
        );

        &::before {
          opacity: 1;
          background: repeating-linear-gradient(
            to bottom,
            #409eff 0px,
            #409eff 4px,
            transparent 4px,
            transparent 8px
          );
          height: 70px;
        }
      }

      // 接近最小宽度时的警告样式
      &.near-min-width::before {
        background: repeating-linear-gradient(
          to bottom,
          #e6a23c 0px,
          #e6a23c 4px,
          transparent 4px,
          transparent 8px
        ) !important;
        opacity: 1 !important;
      }

      // 接近最大宽度时的警告样式
      &.near-max-width::before {
        background: repeating-linear-gradient(
          to bottom,
          #f56c6c 0px,
          #f56c6c 4px,
          transparent 4px,
          transparent 8px
        ) !important;
        opacity: 1 !important;
      }

      &.is-resizing {
        background: linear-gradient(
          90deg,
          transparent 0%,
          #409eff 40%,
          #66b1ff 50%,
          #409eff 60%,
          transparent 100%
        );
        box-shadow: 0 0 12px rgba(64, 158, 255, 0.5);
        transition: none;

        &::before {
          opacity: 1;
          background: #ffffff;
          height: 80px;
          transition: none;
        }
      }

      // 拖动时禁用文本选择
      &.is-resizing,
      &:hover {
        user-select: none;
      }

      // 拖动提示
      .resize-tooltip {
        position: absolute;
        top: -40px;
        left: 50%;
        transform: translateX(-50%);
        background: #409eff;
        color: #fff;
        padding: 6px 14px;
        border-radius: 6px;
        font-size: 13px;
        font-weight: 500;
        white-space: nowrap;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        animation: tooltip-fadein 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        pointer-events: none;
        z-index: 100;

        &::after {
          content: '';
          position: absolute;
          bottom: -6px;
          left: 50%;
          transform: translateX(-50%);
          border-left: 6px solid transparent;
          border-right: 6px solid transparent;
          border-top: 6px solid #409eff;
        }
      }

      @keyframes tooltip-fadein {
        from {
          opacity: 0;
          transform: translateX(-50%) translateY(8px);
        }
        to {
          opacity: 1;
          transform: translateX(-50%) translateY(0);
        }
      }
    }

    .chat-area {
      flex: 1;
      display: flex;
      flex-direction: column;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      background-color: #fff;
      position: relative;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      overflow: hidden;
      transition: box-shadow 0.3s ease;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      }

      .messages-container {
        flex: 1;
        overflow-y: auto;
        padding: 16px;

        .load-more-tip {
          text-align: center;
          padding: 12px;
          cursor: pointer;
          color: #909399;

          .clickable-text:hover {
            color: #409eff;
          }
        }
      }

      .empty-chat {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }

    .empty-select {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border: 1px solid #e4e7ed;
      border-radius: 8px;
      background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
      color: #666;
      padding: 60px 20px;
      min-height: 400px;
      cursor: pointer;
      user-select: none;
      transition: all 0.3s ease;

      &:hover {
        border-color: #409eff;
        box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
        transform: translateY(-2px);

        .empty-icon {
          transform: scale(1.1);
        }
      }

      &:active {
        transform: translateY(0);
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
      }

      .empty-icon {
        font-size: 80px;
        margin-bottom: 24px;
        opacity: 0.8;
        animation: float 3s ease-in-out infinite;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: transform 0.3s ease;
      }

      .empty-title {
        font-size: 18px;
        font-weight: 500;
        color: #1a1a1a;
        margin: 0 0 8px 0;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      .empty-tip {
        font-size: 14px;
        color: #666;
        margin: 0;
        line-height: 1.6;
        display: flex;
        align-items: center;
        justify-content: center;
      }

      @keyframes float {
        0%,
        100% {
          transform: translateY(0px);
        }
        50% {
          transform: translateY(-10px);
        }
      }
    }
  }
}
</style>
