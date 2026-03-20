<template>
  <view class="chat-room-container">
    <!-- 顶部导航 -->
    <view class="chat-header">
      <view class="header-left" @tap="goBack">
        <uni-icons type="back" size="22" color="#333"></uni-icons>
      </view>
      <view class="header-center" @tap="showUserInfo">
        <image class="user-avatar" :src="userInfo.avatar" mode="aspectFill"></image>
        <view class="user-info">
          <text class="user-name">{{ userInfo.name }}</text>
          <text class="user-status">{{ userInfo.isOnline ? '在线' : '离线' }}</text>
        </view>
      </view>
      <view class="header-right" @tap="showMoreMenu">
        <uni-icons type="more" size="22" color="#333"></uni-icons>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="message-list"
      scroll-y
      :scroll-into-view="scrollIntoView"
      @scrolltoupper="loadMoreMessages"
    >
      <view
        :id="'message-' + item.id"
        class="message-item"
        :class="{ self: item.isSelf }"
        v-for="item in messageList"
        :key="item.id"
      >
        <!-- 时间戳 -->
        <view class="message-time" v-if="item.showTime">
          {{ formatMessageTime(item.time) }}
        </view>

        <!-- 消息内容 -->
        <view class="message-wrapper">
          <!-- 对方头像 -->
          <image
            class="message-avatar"
            :src="item.avatar"
            mode="aspectFill"
            v-if="!item.isSelf"
          ></image>

          <!-- 消息气泡 -->
          <view class="message-content">
            <!-- 文本消息 -->
            <view class="text-message" v-if="item.type === 'text'">
              {{ item.content }}
            </view>

            <!-- 图片消息 -->
            <image
              class="image-message"
              :src="item.content"
              mode="widthFix"
              v-if="item.type === 'image'"
              @tap="previewImage(item.content)"
            ></image>

            <!-- 菜品卡片 -->
            <view class="dish-card" v-if="item.type === 'dish'" @tap="viewDish(item.dishId)">
              <image class="dish-image" :src="item.dishImage" mode="aspectFill"></image>
              <view class="dish-info">
                <text class="dish-name">{{ item.dishName }}</text>
                <text class="dish-price">¥{{ item.dishPrice }}</text>
              </view>
            </view>

            <!-- 订单卡片 -->
            <view class="order-card" v-if="item.type === 'order'" @tap="viewOrder(item.orderId)">
              <view class="order-header">
                <text class="order-title">{{ item.orderTitle }}</text>
                <text class="order-status" :class="'status-' + item.orderStatus">
                  {{ item.orderStatusText }}
                </text>
              </view>
              <view class="order-content">
                <text class="order-desc">{{ item.orderDesc }}</text>
                <text class="order-amount">¥{{ item.orderAmount }}</text>
              </view>
            </view>

            <!-- 引用消息 -->
            <view class="quote-message" v-if="item.quote">
              <view class="quote-header">
                <text class="quote-author">{{ item.quote.author }}</text>
              </view>
              <text class="quote-content">{{ item.quote.content }}</text>
            </view>

            <!-- 消息状态 -->
            <view class="message-status" v-if="item.isSelf">
              <uni-icons
                v-if="item.status === 'sending'"
                type="spinner-cycle"
                size="14"
                color="#999"
              ></uni-icons>
              <uni-icons
                v-else-if="item.status === 'success'"
                type="checkmarkempty"
                size="14"
                color="#999"
              ></uni-icons>
              <uni-icons
                v-else-if="item.status === 'fail'"
                type="close"
                size="14"
                color="#F5222D"
              ></uni-icons>
            </view>
          </view>

          <!-- 自己头像 -->
          <image
            class="message-avatar"
            :src="item.avatar"
            mode="aspectFill"
            v-if="item.isSelf"
          ></image>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMoreHistory">
        <text v-if="loadingHistory">加载中...</text>
        <text v-else>下拉加载历史消息</text>
      </view>
    </scroll-view>

    <!-- 回复预览 -->
    <view class="reply-preview" v-if="replyMessage">
      <view class="reply-content">
        <text class="reply-author">{{ replyMessage.author }}</text>
        <text class="reply-text">{{ replyMessage.content }}</text>
      </view>
      <view class="reply-close" @tap="cancelReply">
        <uni-icons type="close" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <!-- 输入区域 -->
    <view class="input-area">
      <view class="input-tools">
        <button class="tool-btn" @tap="chooseImage">
          <uni-icons type="image" size="22" color="#666"></uni-icons>
        </button>
        <button class="tool-btn" @tap="chooseDish">
          <uni-icons type="shop" size="22" color="#666"></uni-icons>
        </button>
        <button class="tool-btn" @tap="chooseOrder">
          <uni-icons type="list" size="22" color="#666"></uni-icons>
        </button>
      </view>
      <view class="input-wrapper">
        <textarea
          class="input-field"
          v-model="inputContent"
          :placeholder="replyMessage ? '回复消息...' : '输入消息...'"
          :auto-height="true"
          :maxlength="500"
          @focus="onInputFocus"
        />
      </view>
      <button class="send-btn" @tap="sendMessage" :disabled="!canSend">
        发送
      </button>
    </view>

    <!-- 更多菜单 -->
    <uni-popup ref="morePopup" type="bottom">
      <view class="more-menu">
        <view class="menu-grid">
          <view class="menu-item" @tap="clearHistory">
            <uni-icons type="trash" size="24" color="#FF6B35"></uni-icons>
            <text class="menu-label">清空记录</text>
          </view>
          <view class="menu-item" @tap="searchMessage">
            <uni-icons type="search" size="24" color="#FF6B35"></uni-icons>
            <text class="menu-label">搜索记录</text>
          </view>
          <view class="menu-item" @tap="reportUser">
            <uni-icons type="flag" size="24" color="#FF6B35"></uni-icons>
            <text class="menu-label">举报用户</text>
          </view>
          <view class="menu-item" @tap="addToBlacklist">
            <uni-icons type="close" size="24" color="#FF6B35"></uni-icons>
            <text class="menu-label">加入黑名单</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { messageApi } from '@/api/modules/message.js'
import WebSocketClient from '@/utils/websocket.js'

// 当前用户ID
const currentUserId = ref('')
const token = ref('')

// 对方用户信息
const userInfo = ref({
  id: '',
  name: '老王家常菜',
  avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=店',
  isOnline: true
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

// 输入
const inputContent = ref('')
const replyMessage = ref(null)

// 弹窗
const morePopup = ref(null)

// WebSocket客户端
let wsClient = null

onMounted(() => {
  // 获取用户信息
  currentUserId.value = uni.getStorageSync('userId') || ''
  token.value = uni.getStorageSync('token') || ''

  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options) {
    // 解析用户信息
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
  loadMessages()
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

/**
 * 连接WebSocket - IM-001
 */
const connectWebSocket = async () => {
  try {
    // 构建WebSocket URL
    const wsUrl = `wss://api.example.com/ws/chat/${currentUserId.value}/${userInfo.value.id}`

    // 创建WebSocket客户端
    wsClient = new WebSocketClient(wsUrl)

    // 监听连接成功
    wsClient.on('_connected', () => {
      console.log('[ChatRoom] WebSocket已连接')
    })

    // 监听消息
    wsClient.on('message', (data) => {
      console.log('[ChatRoom] 收到消息', data)
      handleMessage(data)
    })

    // 连接
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
 * 断开WebSocket - IM-001
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
  // 转换消息格式
  const formattedMessage = {
    id: message.id || Date.now(),
    isSelf: message.senderId === currentUserId.value,
    avatar: message.senderId === currentUserId.value
      ? uni.getStorageSync('avatar') || ''
      : userInfo.value.avatar,
    type: message.type || 'text',
    content: message.content,
    time: new Date(message.timestamp || Date.now()),
    showTime: shouldShowTime(message),
    status: 'success'
  }

  messageList.value.push(formattedMessage)

  // 滚动到底部
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

  // 如果距离上一条消息超过5分钟，显示时间
  return timeDiff > 5 * 60 * 1000
}

/**
 * 加载消息 - IM-002: 调用API获取消息列表
 */
const loadMessages = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    // IM-002: 调用API获取消息列表
    const res = await messageApi.getMessages({
      userId: currentUserId.value,
      pageSize: pageSize,
      pageNum: currentPage.value
    })

    uni.hideLoading()

    if (res.code === 200 && res.data) {
      // 转换消息格式
      const formattedMessages = res.data.map(msg => ({
        id: msg.id,
        isSelf: msg.senderId === currentUserId.value,
        avatar: msg.senderId === currentUserId.value
          ? uni.getStorageSync('avatar') || ''
          : userInfo.value.avatar,
        type: msg.messageType || 'text',
        content: msg.content,
        time: new Date(msg.createTime || Date.now()),
        showTime: false, // 稍后计算
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

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })

      console.log('加载消息成功，数量:', formattedMessages.length)
    } else {
      throw new Error(res.message || '获取消息失败')
    }
  } catch (error) {
    console.error('加载消息失败:', error)
    uni.hideLoading()

    // 如果API调用失败，使用模拟数据（开发阶段）
    messageList.value = generateMockMessages()
    nextTick(() => {
      scrollToBottom()
    })

    // uni.showToast({
    //   title: error.message || '加载消息失败',
    //   icon: 'none'
    // })
  }
}

/**
 * 生成模拟消息（开发阶段使用）
 */
const generateMockMessages = () => {
  const messages = []
  const myAvatar = 'https://via.placeholder.com/80/52C41A/FFFFFF?text=我'
  const otherAvatar = userInfo.value.avatar

  for (let i = 0; i < 10; i++) {
    const isSelf = i % 3 === 0
    messages.push({
      id: Date.now() + i,
      isSelf,
      avatar: isSelf ? myAvatar : otherAvatar,
      type: 'text',
      content: `这是第${i + 1}条消息`,
      time: new Date(Date.now() - (10 - i) * 60000),
      showTime: i === 0,
      status: 'success'
    })
  }

  return messages
}

/**
 * 加载历史消息 - IM-003: 加载历史消息（分页）
 */
const loadMoreMessages = async () => {
  if (!hasMoreHistory.value || loadingHistory.value) return

  loadingHistory.value = true

  try {
    currentPage.value++

    // IM-003: 调用API获取历史消息
    const res = await messageApi.getMessages({
      userId: currentUserId.value,
      pageSize: pageSize,
      pageNum: currentPage.value
    })

    if (res.code === 200 && res.data) {
      if (res.data.length === 0) {
        hasMoreHistory.value = false
      } else {
        // 转换消息格式并插入到列表顶部
        const formattedMessages = res.data.map(msg => ({
          id: msg.id,
          isSelf: msg.senderId === currentUserId.value,
          avatar: msg.senderId === currentUserId.value
            ? uni.getStorageSync('avatar') || ''
            : userInfo.value.avatar,
          type: msg.messageType || 'text',
          content: msg.content,
          time: new Date(msg.createTime || Date.now()),
          showTime: false,
          status: 'success'
        }))

        messageList.value = [...formattedMessages, ...messageList.value]

        // 滚动到第一条新消息的位置
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
 * 格式化消息时间
 */
const formatMessageTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
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
 * 能否发送
 */
const canSend = computed(() => {
  return inputContent.value.trim().length > 0
})

/**
 * 发送消息 - IM-004: 发送文本消息到服务器
 */
const sendMessage = async () => {
  if (!canSend.value) return

  const newMessage = {
    id: Date.now(),
    isSelf: true,
    avatar: uni.getStorageSync('avatar') || 'https://via.placeholder.com/80/52C41A/FFFFFF?text=我',
    type: 'text',
    content: inputContent.value,
    time: new Date(),
    showTime: true,
    status: 'sending',
    quote: replyMessage.value ? {
      author: replyMessage.value.author,
      content: replyMessage.value.content
    } : null
  }

  messageList.value.push(newMessage)
  const contentToSend = inputContent.value
  inputContent.value = ''
  replyMessage.value = null

  scrollToBottom()

  try {
    // IM-004: 调用API发送文本消息
    const res = await messageApi.sendTextMessage({
      senderId: currentUserId.value,
      receiverId: userInfo.value.id,
      content: contentToSend
    })

    if (res.code === 200) {
      newMessage.status = 'success'

      // 同时通过WebSocket发送（实时通信）
      if (wsClient && wsClient.isConnected()) {
        wsClient.send({
          type: 'message',
          dataType: 'text',
          senderId: currentUserId.value,
          receiverId: userInfo.value.id,
          content: contentToSend,
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
    newMessage.status = 'failed'
    uni.showToast({
      title: error.message || '发送失败',
      icon: 'none'
    })
  }
}

/**
 * 选择图片
 */
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      sendImageMessage(res.tempFilePaths[0])
    }
  })
}

/**
 * 发送图片消息 - IM-005: 上传图片并发送
 */
const sendImageMessage = async (imagePath) => {
  const newMessage = {
    id: Date.now(),
    isSelf: true,
    avatar: uni.getStorageSync('avatar') || 'https://via.placeholder.com/80/52C41A/FFFFFF?text=我',
    type: 'image',
    content: imagePath,
    time: new Date(),
    showTime: true,
    status: 'sending'
  }

  messageList.value.push(newMessage)
  scrollToBottom()

  try {
    // IM-005: 上传图片
    uni.showLoading({ title: '上传中...' })

    const uploadRes = await new Promise((resolve, reject) => {
      uni.uploadFile({
        url: 'https://api.example.com/v1/upload/image',
        filePath: imagePath,
        name: 'file',
        header: {
          'Authorization': token.value
        },
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            resolve(data)
          } catch (error) {
            reject(error)
          }
        },
        fail: (err) => {
          reject(err)
        }
      })
    })

    uni.hideLoading()

    if (uploadRes.code === 200) {
      const imageUrl = uploadRes.data.url

      // 发送图片消息
      const res = await messageApi.sendImageMessage({
        senderId: currentUserId.value,
        receiverId: userInfo.value.id,
        imageUrl: imageUrl
      })

      if (res.code === 200) {
        newMessage.status = 'success'
        newMessage.content = imageUrl

        // 同时通过WebSocket发送（实时通信）
        if (wsClient && wsClient.isConnected()) {
          wsClient.send({
            type: 'message',
            dataType: 'image',
            senderId: currentUserId.value,
            receiverId: userInfo.value.id,
            content: imageUrl,
            timestamp: Date.now()
          }).catch(err => {
            console.error('[ChatRoom] WebSocket发送图片失败', err)
          })
        }
      } else {
        throw new Error(res.message || '发送失败')
      }
    } else {
      throw new Error(uploadRes.message || '上传失败')
    }
  } catch (error) {
    console.error('[ChatRoom] 发送图片失败', error)
    uni.hideLoading()
    newMessage.status = 'failed'
    uni.showToast({
      title: error.message || '发送失败',
      icon: 'none'
    })
  }
}

/**
 * 选择菜品 - IM-006: 跳转到选择菜品页面
 */
const chooseDish = () => {
  uni.navigateTo({
    url: '/pages-common/chat/dish-selector?userId=' + userInfo.value.id
  })
}

/**
 * 选择订单 - IM-007: 跳转到选择订单页面
 */
const chooseOrder = () => {
  uni.navigateTo({
    url: '/pages-common/chat/order-selector?userId=' + userInfo.value.id
  })
}

/**
 * 预览图片
 */
const previewImage = (url) => {
  uni.previewImage({
    urls: [url],
    current: url
  })
}

/**
 * 查看菜品
 */
const viewDish = (dishId) => {
  uni.navigateTo({
    url: `/pages/dish/detail?id=${dishId}`
  })
}

/**
 * 查看订单
 */
const viewOrder = (orderId) => {
  uni.navigateTo({
    url: `/pages/order/detail?id=${orderId}`
  })
}

/**
 * 输入框聚焦
 */
const onInputFocus = () => {
  nextTick(() => {
    scrollToBottom()
  })
}

/**
 * 取消回复
 */
const cancelReply = () => {
  replyMessage.value = null
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 显示用户信息
 */
const showUserInfo = () => {
  uni.navigateTo({
    url: `/pages/home/merchant-detail?id=${userInfo.value.id}`
  })
}

/**
 * 显示更多菜单
 */
const showMoreMenu = () => {
  morePopup.value?.open()
}

/**
 * 清空记录
 */
const clearHistory = () => {
  uni.showModal({
    title: '清空记录',
    content: '确定清空所有聊天记录吗？',
    success: (res) => {
      if (res.confirm) {
        messageList.value = []
        morePopup.value?.close()
      }
    }
  })
}

/**
 * 搜索记录 - IM-008: 跳转到搜索页面
 */
const searchMessage = () => {
  morePopup.value?.close()

  // 跳转到消息搜索页面
  uni.navigateTo({
    url: `/pages-common/chat/search?conversationId=${conversationId.value}&userId=${userInfo.value.id}`
  })
}

/**
 * 举报用户 - IM-009: 跳转到举报页面
 */
const reportUser = () => {
  morePopup.value?.close()

  // 跳转到举报页面
  uni.navigateTo({
    url: `/pages-common/report?type=user&targetId=${userInfo.value.id}&targetName=${encodeURIComponent(userInfo.value.name)}`
  })
}

/**
 * 加入黑名单
 */
const addToBlacklist = () => {
  uni.showModal({
    title: '加入黑名单',
    content: '确定将此用户加入黑名单吗？',
    success: (res) => {
      if (res.confirm) {
        morePopup.value?.close()
        uni.showToast({
          title: '已加入黑名单',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 处理收到的消息
 */
const handleMessage = (message) => {
  messageList.value.push({
    ...message,
    isSelf: false,
    avatar: userInfo.value.avatar,
    showTime: true
  })
  scrollToBottom()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.chat-room-container {
  height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.chat-header {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid #eee;
}

.header-center {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.user-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.user-status {
  font-size: 22rpx;
  color: #52C41A;
}

/* 消息列表 */
.message-list {
  flex: 1;
  padding: 20rpx;
}

.message-item {
  margin-bottom: 30rpx;
}

.message-time {
  text-align: center;
  font-size: 24rpx;
  color: #999;
  margin-bottom: 20rpx;
}

.message-wrapper {
  display: flex;
  gap: 20rpx;
}

.message-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-content {
  max-width: 500rpx;
  position: relative;
}

.self .message-wrapper {
  flex-direction: row-reverse;
}

.self .message-content {
  background: #FF6B35;
  color: #fff;
  border-radius: 20rpx 0 20rpx 20rpx;
}

.message-content:not(.self .message-content) {
  background: #fff;
  border-radius: 0 20rpx 20rpx 20rpx;
}

.text-message {
  padding: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-all;
}

.image-message {
  max-width: 400rpx;
  border-radius: 12rpx;
}

.dish-card {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
}

.dish-image {
  width: 300rpx;
  height: 200rpx;
}

.dish-info {
  padding: 15rpx;
  display: flex;
  justify-content: space-between;
}

.dish-name {
  font-size: 26rpx;
  color: #333;
}

.dish-price {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

.order-card {
  background: #F5F5F5;
  border-radius: 12rpx;
  padding: 20rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.order-title {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.order-status {
  font-size: 22rpx;
  padding: 5rpx 10rpx;
  border-radius: 8rpx;

  &.status-pending {
    background: #FFF7E6;
    color: #FF6B35;
  }

  &.status-completed {
    background: #E8F5E9;
    color: #52C41A;
  }
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-desc {
  font-size: 24rpx;
  color: #666;
}

.order-amount {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

.quote-message {
  background: rgba(0, 0, 0, 0.05);
  padding: 15rpx;
  border-radius: 8rpx;
  margin-bottom: 10rpx;
}

.quote-author {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-bottom: 5rpx;
}

.quote-content {
  font-size: 24rpx;
  color: #666;
}

.message-status {
  position: absolute;
  bottom: 10rpx;
  right: 10rpx;
}

.load-more {
  text-align: center;
  padding: 20rpx;
  font-size: 24rpx;
  color: #999;
}

/* 回复预览 */
.reply-preview {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
  border-top: 1rpx solid #eee;
}

.reply-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.reply-author {
  font-size: 22rpx;
  color: #999;
}

.reply-text {
  font-size: 24rpx;
  color: #666;
  @include text-ellipsis;
}

.reply-close {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
}

/* 输入区域 */
.input-area {
  background: #fff;
  padding: 20rpx 30rpx;
  border-top: 1rpx solid #eee;
}

.input-tools {
  display: flex;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.tool-btn {
  width: 60rpx;
  height: 60rpx;
  background: #F5F5F5;
  border-radius: 50%;
  border: none;
  @include flex-center;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 20rpx;
}

.input-field {
  flex: 1;
  min-height: 70rpx;
  max-height: 200rpx;
  padding: 15rpx 20rpx;
  background: #F5F5F5;
  border-radius: 20rpx;
  font-size: 28rpx;
}

.send-btn {
  width: 120rpx;
  height: 70rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 35rpx;
  border: none;
  @include flex-center;

  &[disabled] {
    background: #E8E8E8;
    color: #999;
  }
}

/* 更多菜单 */
.more-menu {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 30rpx;
}

.menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15rpx;
}

.menu-label {
  font-size: 24rpx;
  color: #333;
}
</style>
