/**
 * Composable: useGroupChat
 * 用途：群聊核心逻辑管理
 * 创建时间：2026-03-20
 */
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { groupApi } from '@/api/modules/group.js'
import WebSocketClient from '@/utils/websocket.js'

export function useGroupChat() {
  // 当前用户信息
  const currentUserId = ref('')
  const token = ref('')

  // 群信息
  const groupInfo = ref({
    id: '',
    name: '美食爱好者群',
    avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=群',
    memberCount: 25,
    notice: '欢迎加入美食爱好者群，一起分享美食！'
  })

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
  const init = async () => {
    // 获取用户信息
    currentUserId.value = uni.getStorageSync('userId') || ''
    token.value = uni.getStorageSync('token') || ''

    // 获取页面参数
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const options = currentPage.options

    if (options && options.id) {
      groupInfo.value.id = options.id
      if (options.name) {
        groupInfo.value.name = decodeURIComponent(options.name)
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
      const wsUrl = `wss://api.example.com/ws/group/${groupInfo.value.id}`
      wsClient = new WebSocketClient(wsUrl)

      wsClient.on('_connected', () => {
        console.log('[GroupChat] WebSocket已连接')
      })

      wsClient.on('message', (data) => {
        console.log('[GroupChat] 收到消息', data)
        handleMessage(data)
      })

      await wsClient.connect(token.value)
    } catch (error) {
      console.error('[GroupChat] WebSocket连接失败', error)
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
      nickname: message.senderNickname || '群成员',
      avatar: message.senderAvatar || '/static/default-avatar.png',
      isGroup: true,
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

      const res = await groupApi.getMessages(groupInfo.value.id, {
        page: currentPage.value,
        size: pageSize
      })

      uni.hideLoading()

      if (res.code === 200 && res.data) {
        const formattedMessages = res.data.map(msg => ({
          id: msg.id,
          isSelf: msg.senderId === currentUserId.value,
          userId: msg.senderId,
          nickname: msg.senderNickname || '群成员',
          avatar: msg.senderAvatar || '/static/default-avatar.png',
          isGroup: true,
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

        console.log('加载群消息成功，数量:', formattedMessages.length)
      } else {
        throw new Error(res.message || '获取消息失败')
      }
    } catch (error) {
      console.error('加载群消息失败:', error)
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
    const members = [
      { id: 1, name: '张三', avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=张' },
      { id: 2, name: '李四', avatar: 'https://via.placeholder.com/80/52C41A/FFFFFF?text=李' },
      { id: 3, name: '王五', avatar: 'https://via.placeholder.com/80/1677FF/FFFFFF?text=王' }
    ]
    const myAvatar = 'https://via.placeholder.com/80/FAAD14/FFFFFF?text=我'

    for (let i = 0; i < 15; i++) {
      const isSelf = i % 4 === 0
      const member = members[i % members.length]

      messages.push({
        id: Date.now() + i,
        isSelf,
        userId: isSelf ? 0 : member.id,
        nickname: isSelf ? '我' : member.name,
        avatar: isSelf ? myAvatar : member.avatar,
        isGroup: true,
        type: 'text',
        content: `这是第${i + 1}条群消息`,
        time: new Date(Date.now() - (15 - i) * 60000),
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

      const res = await groupApi.getMessages(groupInfo.value.id, {
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
            nickname: msg.senderNickname || '群成员',
            avatar: msg.senderAvatar || '/static/default-avatar.png',
            isGroup: true,
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
      isGroup: true,
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
      const res = await groupApi.sendMessage({
        groupId: groupInfo.value.id,
        senderId: currentUserId.value,
        type: 'text',
        content,
        quote: newMessage.quote
      })

      if (res.code === 200) {
        newMessage.status = 'success'

        // 同时通过WebSocket发送
        if (wsClient && wsClient.isConnected()) {
          wsClient.send({
            type: 'group_message',
            groupId: groupInfo.value.id,
            senderId: currentUserId.value,
            dataType: 'text',
            content,
            quote: newMessage.quote,
            timestamp: Date.now()
          }).catch(err => {
            console.error('[GroupChat] WebSocket发送消息失败', err)
          })
        }
      } else {
        throw new Error(res.message || '发送失败')
      }
    } catch (error) {
      console.error('[GroupChat] 发送消息失败', error)
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

  // 生命周期
  onMounted(() => {
    init()
  })

  onUnmounted(() => {
    disconnectWebSocket()
  })

  return {
    // 数据
    groupInfo,
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
    scrollToBottom
  }
}
