<template>
  <div class="chat-container" @click="handleGlobalClick">
    <!-- 使用新的头部组件 -->
    <ChatHeader @open-action-panel="openActionPanelWithTab" />

    <div class="chat-content">
      <!-- 左侧会话列表 -->
      <div class="conversation-list-wrapper">
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
      @change-merchant="changeMerchant"
      @continue-order="openMerchantSelectDialog"
      @select-merchant="openMerchantSelectDialog"
      @go-to-pay="goToOrderConfirmation"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

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

// Constants
import { MESSAGE_CONFIG } from '../../constants/chatConstants'
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
  updateConversationLastMessage
} = useConversations()

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
  console.log('收到 WebSocket 消息:', data)

  switch (data.type) {
    case 'chat':
      if (data.content) {
        const fromId = data.content.fromId || data.content.sender || '未知'

        // 确定发送者显示名称
        let senderName = null
        if (fromId !== userId.value.toString()) {
          if (data.content.senderName || data.content.username || data.content.nickname) {
            senderName = data.content.senderName || data.content.username || data.content.nickname
          } else if (selectedConversation.value?.type === 'single') {
            senderName = selectedConversation.value.name
          } else if (selectedConversation.value?.type === 'group') {
            senderName = fromId
          }
        }

        const message = {
          ...data.content,
          formattedTime: formatMessageTime(data.content.createTime || data.content.time),
          fromId,
          senderName
        }
        addMessage(message, data.content.toId)
        updateConversationLastMessage(data.content.toId, message)
      }
      break
    case 'notification':
      ElMessage.info(data.content?.message || '收到新通知')
      break
    default:
      console.log('未知消息类型:', data)
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
  return (
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

  const messageData = {
    fromId: userId.value.toString(),
    toId: selectedConversation.value.id,
    msgType: selectedConversation.value.type || 'single',
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

const resendMessage = async (failedMessage) => {
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
  const existingConversation = conversations.value.find((conv) => conv.id === user.id)

  if (existingConversation) {
    selectedConversation.value = existingConversation
  } else {
    const newConversation = {
      ...user,
      lastMessage: '开始聊天吧！',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }

    conversations.value.unshift(newConversation)
    selectedConversation.value = newConversation

    chatHistory.value[newConversation.id] = []
  }

  ElMessage.success(`已开始与 ${user.name} 的对话`)
}

const createGroupFromPanel = async (data) => {
  try {
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

    const groupId = groupResponse.data.id
    const groupName = data.name.trim()

    // 2. 将创建者添加到群成员关系
    await api.post('/v1/contacts/groups/join', {
      userId: userId.value.toString(),
      targetId: groupId.toString(),
      relationType: 'group',
      status: 'normal'
    })

    // 3. 将选中的成员添加到群成员关系
    for (const member of data.members) {
      try {
        await api.post('/v1/contacts/groups/join', {
          userId: member.id.toString(),
          targetId: groupId.toString(),
          relationType: 'group',
          status: 'normal'
        })
      } catch (error) {
        console.error(`添加成员 ${member.name} 到群失败:`, error)
      }
    }

    // 4. 创建前端会话对象
    const newGroup = {
      id: groupId,
      type: 'group',
      name: groupName,
      avatar: '👥',
      lastMessage: '暂无消息',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      unreadCount: 0,
      memberCount: data.members.length + 1,
      pinned: false
    }

    conversations.value.push(newGroup)
    chatHistory.value[groupId] = []

    // 5. 添加系统消息
    const systemMsg = {
      id: 1,
      sender: '系统',
      content: `群聊 "${groupName}" 已创建`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
    chatHistory.value[groupId].push(systemMsg)
    newGroup.lastMessage = systemMsg.content

    selectedConversation.value = newGroup

    ElMessage.success(`群聊 "${groupName}" 已创建`)
  } catch (error) {
    console.error('创建群失败:', error)

    // 如果后端调用失败，降级使用前端模拟（仅开发模式）
    const newGroupId = Date.now()
    const memberNames = data.members.map((member) => member.name)

    const newGroup = {
      id: newGroupId,
      type: 'group',
      name: data.name,
      avatar: '👥',
      lastMessage: '暂无消息',
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      unreadCount: 0,
      memberCount: memberNames.length + 1,
      pinned: false
    }

    conversations.value.push(newGroup)
    chatHistory.value[newGroupId] = []

    const systemMsg = {
      id: 1,
      sender: '系统',
      content: `群聊 "${newGroup.name}" 已创建`,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }
    chatHistory.value[newGroupId].push(systemMsg)
    newGroup.lastMessage = systemMsg.content

    selectedConversation.value = newGroup

    ElMessage.success(`群聊 "${data.name}" 已创建（离线模式）`)
  }
}

const handleAddFriendFromPanel = (user) => {
  ElMessage.success(`已向 ${user.name} 发送好友申请`)
  fetchFriends()
}

const openGroupDetail = () => {
  if (!selectedConversation.value || selectedConversation.value.type !== 'group') return

  currentGroupInfo.value = {
    id: selectedConversation.value.id,
    name: selectedConversation.value.name,
    avatar: selectedConversation.value.avatar,
    memberCount: selectedConversation.value.memberCount,
    members: ['我', '张三', '李四', '王五', '赵六'],
    creator: '我',
    createdAt: '2024-01-15 10:30:00'
  }

  groupDetailDialogVisible.value = true
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

const openMerchantSelectDialog = () => {
  if (!selectedConversation.value || !hasGroupOrder.value) {
    ElMessage.error('请先创建群订单')
    return
  }

  if (orderingMerchant.value) {
    selectedMerchant.value = orderingMerchant.value
    productSelectDialogVisible.value = true
  } else {
    merchantSelectDialogVisible.value = true
  }
}

const selectMerchant = (merchant) => {
  selectedMerchant.value = merchant
  orderingMerchant.value = merchant
  merchantSelectDialogVisible.value = false

  if (selectedConversation.value && hasGroupOrder.value) {
    const currentOrder = groupOrders.value[selectedConversation.value.id]
    currentOrder.merchantId = merchant.id
    currentOrder.merchantName = merchant.name
  }

  productSelectDialogVisible.value = true
}

const addProductToCart = () => {
  ElMessage.success('商品已加入购物车')
}

const confirmProductSelection = () => {
  ElMessage.success('商品已添加到群订单')
  productSelectDialogVisible.value = false
}

const changeMerchant = () => {
  merchantSelectDialogVisible.value = true
}

const goToOrderConfirmation = () => {
  ElMessage.info('跳转到订单确认页面')
}

// ========== 生命周期 ==========
onMounted(async () => {
  console.log('🚀 [Chat] Chat组件挂载，开始初始化')
  try {
    // 先从本地加载聊天历史缓存（同步函数）
    loadChatHistoryFromLocal()
    console.log('📦 [Chat] 本地缓存加载完成', Object.keys(chatHistory.value))

    const conversationsResponse = await api.get(`/v1/chat/users/${userId.value}/chat-sessions`)

    console.log('🚀 [Chat] 会话列表, conversationsResponse', conversationsResponse)
    console.log('📡 [Chat] 会话列表API响应', {
      code: conversationsResponse.code,
      dataLength: conversationsResponse.data?.length,
      userId: userId.value
    })

    await fetchFriends()

    if (conversationsResponse.code === '200') {
      conversations.value = conversationsResponse.data
      console.log(`👥 [Chat] 会话列表已更新 - 共 ${conversations.value.length} 个会话`)

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
    if (response.code === '200') {
      // 为每个好友获取详细信息
      const friendsWithDetails = await Promise.all(
        response.data.map(async (contact) => {
          try {
            const userResponse = await api.get(`/v1/users/${contact.targetId}`)
            const userData = userResponse.data

            return {
              id: contact.targetId,
              name: userData.nickname || userData.username || '好友',
              avatar: userData.avatar || '👤',
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
    gap: 16px;
    flex: 1;
    min-height: 0;
    overflow: hidden;

    .conversation-list-wrapper {
      width: 240px;
      min-width: 220px;
      height: 100%;
      display: flex;
      flex-direction: column;
      border: 1px solid #e4e7ed;
      border-radius: 12px;
      background-color: #fff;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      transform: translateY(0);

      &:hover {
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
        transform: translateY(-4px);
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
