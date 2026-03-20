/**
 * Composable: useChatRoom
 * 用途：聊天室（单聊）核心逻辑管理
 * 创建时间：2026-03-20
 */
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { messageApi } from '@/api/modules/message.js'
import WebSocketClient from '@/utils/websocket.js'

export function useChatRoom() {
  // 当前用户信息
  const currentUserId = ref('')
  const token = ref('')

  // 对方用户信息
  const userInfo = ref({
    id: '',
    name: '',
    avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=用',
    isOnline: false
  })

  // 会话ID
  const conversationId = ref('')

  // 消息列表
  const messageList = ref([])
  const loadingHistory = ref(false)
  const hasMoreHistory = ref(true)
  const scrollIntoView = ref('')
  const currentPage = ref(1)
  const pageSize = 20

  // 回复消息
  const replyMessage = ref(null)

  // WebSocket客户端
  let wsClient = null

  /**
   * 初始化
   */
  const init = async (options) => {
    // 获取用户信息
    currentUserId.value = uni.getStorageSync('userId') || ''
    token.value = uni.getStorageSync('token') || ''

    // 解析页面参数
    if (options) {
      if (options.userId) {
        userInfo.value.id = options.userId
      }
      if (options.userName) {
        userInfo.value.name = decodeURIComponent(options.userName)
      }
      if (options.userAvatar) {
        userInfo.value.avatar = decodeURIComponent(options.userAvatar)
      }
      if (options.conversationId) {
        conversationId.value = options.conversationId
      }
    }

    // 加载消息和连接WebSocket
    await loadMessages()
    connectWebSocket()
  }

  /**
   * 连接WebSocket
   */
  const connectWebSocket = async () => {
    try {
      const wsUrl = `wss://api.example.com/ws/chat/${currentUserId.value}/${userInfo.value.id}`
      wsClient = new WebSocketClient(wsUrl)

      wsClient.on('_connected', () => {
        console.log('[ChatRoom] WebSocket已连接')
      })

      wsClient.on('message', (data) => {
        console.log('[ChatRoom] 收到消息', data)
        handleMessage(data)
      })

      await wsClient.connect(token.value)
    } catch (error) {
      console.error('[ChatRoom] WebSocket连接失败', error)
      uni.showToast({
        title: '连接失败',
        icon: 'none'
      })
    }
  }

  /**
   * 断开WebSocket
   */
  const disconnectWebSocket = () => {
    if (wsClient) {
      wsClient.close()
      wsClient = null
    }
  }

  /**
   * 处理收到的消息
   */
  const handleMessage = (message) => {
    const formattedMessage = {
      id: message.id || Date.now(),
      isSelf: message.senderId === currentUserId.value,
      userId: message.senderId,
      nickname: message.senderNickname || '对方',
      avatar: message.senderAvatar || '/static/default-avatar.png',
      isGroup: false,
      type: message.type || 'text',
      content: message.content,
      time: new Date(message.timestamp || Date.now()),
      showTime: shouldShowTime(message),
      status: 'success'
    }

    messageList.value.push(formattedMessage)

    nextTick(() => {
      scrollToBottom()
    })
  }

  /**
   * 判断是否显示时间
   */
  const shouldShowTime = (message) => {
    if (messageList.value.length === 0) {
      return true
    }

    const lastMessage = messageList.value[messageList.value.length - 1]
    const timeDiff = new Date(message.timestamp || Date.now()) - new Date(lastMessage.time)

    return timeDiff > 5 * 60 * 1000
  }

  /**
   * 加载消息
   */
  const loadMessages = async () => {
    try {
      uni.showLoading({ title: '加载中...' })

      const res = await messageApi.getMessages(conversationId.value, {
        page: currentPage.value,
        size: pageSize
      })

      uni.hideLoading()

      if (res.code === 200 && res.data) {
        const formattedMessages = res.data.map(msg => ({
          id: msg.id,
          isSelf: msg.senderId === currentUserId.value,
          userId: msg.senderId,
          nickname: msg.senderNickname || '对方',
          avatar: msg.senderAvatar || '/static/default-avatar.png',
          isGroup: false,
          type: msg.messageType || 'text',
          content: msg.content,
          time: new Date(msg.createTime || Date.now()),
          showTime: false,
          status: 'success'
        }))

        // 计算是否显示时间
        formattedMessages.forEach((msg, index) => {
          if (index === 0) {
            msg.showTime = true
          } else {
            const prevMsg = formattedMessages[index - 1]
            const timeDiff = msg.time - prevMsg.time
            msg.showTime = timeDiff > 5 * 60 * 1000
          }
        })

        messageList.value = formattedMessages

        nextTick(() => {
          scrollToBottom()
        })

        console.log('加载聊天消息成功，数量:', formattedMessages.length)
      } else {
        throw new Error(res.message || '获取消息失败')
      }
    } catch (error) {
      console.error('加载聊天消息失败:', error)
      uni.hideLoading()

      // 如果API调用失败，使用模拟数据
      messageList.value = generateMockMessages()
      nextTick(() => {
        scrollToBottom()
      })
    }
  }

  /**
   * 生成模拟消息
   */
  const generateMockMessages = () => {
    const messages = []
    const otherAvatar = 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=店'
    const myAvatar = 'https://via.placeholder.com/80/FAAD14/FFFFFF?text=我'

    for (let i = 0; i < 10; i++) {
      const isSelf = i % 3 === 0

      messages.push({
        id: Date.now() + i,
        isSelf,
        userId: isSelf ? 0 : 1,
        nickname: isSelf ? '我' : '商家',
        avatar: isSelf ? myAvatar : otherAvatar,
        isGroup: false,
        type: 'text',
        content: `这是第${i + 1}条消息`,
        time: new Date(Date.now() - (10 - i) * 60000),
        showTime: i === 0 || i % 5 === 0,
        status: 'success'
      })
    }

    return messages
  }

  /**
   * 加载更多消息
   */
  const loadMoreMessages = async () => {
    if (!hasMoreHistory.value || loadingHistory.value) return

    loadingHistory.value = true

    try {
      currentPage.value++

      const res = await messageApi.getMessages(conversationId.value, {
        page: currentPage.value,
        size: pageSize
      })

      if (res.code === 200 && res.data) {
        if (res.data.length === 0) {
          hasMoreHistory.value = false
        } else {
          const formattedMessages = res.data.map(msg => ({
            id: msg.id,
            isSelf: msg.senderId === currentUserId.value,
            userId: msg.senderId,
            nickname: msg.senderNickname || '对方',
            avatar: msg.senderAvatar || '/static/default-avatar.png',
            isGroup: false,
            type: msg.messageType || 'text',
            content: msg.content,
            time: new Date(msg.createTime || Date.now()),
            showTime: false,
            status: 'success'
          }))

          messageList.value = [...formattedMessages, ...messageList.value]

          if (formattedMessages.length > 0) {
            scrollIntoView.value = 'message-' + formattedMessages[0].id
          }
        }
      } else {
        currentPage.value--
        hasMoreHistory.value = false
      }
    } catch (error) {
      console.error('加载历史消息失败:', error)
      currentPage.value--
      hasMoreHistory.value = false
    } finally {
      loadingHistory.value = false
    }
  }

  /**
   * 滚动到底部
   */
  const scrollToBottom = () => {
    nextTick(() => {
      if (messageList.value.length > 0) {
        const lastMessage = messageList.value[messageList.value.length - 1]
        scrollIntoView.value = 'message-' + lastMessage.id
      }
    })
  }

  /**
   * 发送消息
   */
  const sendMessage = async (content) => {
    const newMessage = {
      id: Date.now(),
      isSelf: true,
      userId: currentUserId.value,
      nickname: '我',
      avatar: uni.getStorageSync('avatar') || 'https://via.placeholder.com/80/FAAD14/FFFFFF?text=我',
      isGroup: false,
      type: 'text',
      content,
      time: new Date(),
      showTime: true,
      status: 'sending',
      quote: replyMessage.value ? {
        author: replyMessage.value.author,
        content: replyMessage.value.content
      } : null
    }

    messageList.value.push(newMessage)
    replyMessage.value = null

    scrollToBottom()

    try {
      const res = await messageApi.sendMessage({
        conversationId: conversationId.value,
        senderId: currentUserId.value,
        receiverId: userInfo.value.id,
        type: 'text',
        content,
        quote: newMessage.quote
      })

      if (res.code === 200) {
        newMessage.status = 'success'

        // 同时通过WebSocket发送
        if (wsClient && wsClient.isConnected()) {
          wsClient.send({
            type: 'private_message',
            senderId: currentUserId.value,
            receiverId: userInfo.value.id,
            dataType: 'text',
            content,
            quote: newMessage.quote,
            timestamp: Date.now()
          }).catch(err => {
            console.error('[ChatRoom] WebSocket发送消息失败', err)
          })
        }
      } else {
        throw new Error(res.message || '发送失败')
      }
    } catch (error) {
      console.error('[ChatRoom] 发送消息失败', error)
      newMessage.status = 'fail'
      uni.showToast({
        title: error.message || '发送失败',
        icon: 'none'
      })
    }
  }

  /**
   * 设置回复消息
   */
  const setReplyMessage = (message) => {
    replyMessage.value = {
      author: message.nickname,
      content: message.content
    }
  }

  /**
   * 取消回复
   */
  const cancelReply = () => {
    replyMessage.value = null
  }

  /**
   * 更新用户在线状态
   */
  const updateOnlineStatus = (isOnline) => {
    userInfo.value.isOnline = isOnline
  }

  // 生命周期
  onMounted(() => {
    // 获取页面参数
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const options = currentPage.options

    init(options)
  })

  onUnmounted(() => {
    disconnectWebSocket()
  })

  return {
    // 数据
    userInfo,
    messageList,
    loadingHistory,
    hasMoreHistory,
    scrollIntoView,
    replyMessage,

    // 方法
    loadMoreMessages,
    sendMessage,
    setReplyMessage,
    cancelReply,
    updateOnlineStatus,
    scrollToBottom
  }
}
