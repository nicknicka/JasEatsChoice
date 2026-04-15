<template>
  <view class="group-chat-container">
    <!-- 顶部导航 -->
    <view class="chat-header">
      <view class="header-left" @tap="goBack">
        <uni-icons type="back" size="22" color="#333"></uni-icons>
      </view>
      <view class="header-center" @tap="showGroupDetail">
        <image class="group-avatar" :src="groupInfo.avatar" mode="aspectFill"></image>
        <view class="group-info">
          <text class="group-name">{{ groupInfo.name }}</text>
          <text class="group-members">({{ groupInfo.memberCount }}人)</text>
        </view>
      </view>
      <view class="header-right" @tap="showMoreMenu">
        <uni-icons type="more" size="22" color="#333"></uni-icons>
      </view>
    </view>

    <!-- 群公告 -->
    <view class="group-notice" v-if="groupInfo.notice" @tap="viewNotice">
      <uni-icons type="notification" size="16" color="#FF6B35"></uni-icons>
      <text class="notice-text">{{ groupInfo.notice }}</text>
      <uni-icons type="arrowright" size="14" color="#999"></uni-icons>
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
        @longpress="showMessageMenu(item)"
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
            @tap="showMemberInfo(item.userId)"
          ></image>

          <!-- 昵称 -->
          <text class="member-name" v-if="!item.isSelf && item.isGroup">{{ item.nickname }}</text>

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
                <text class="order-status">{{ item.orderStatusText }}</text>
              </view>
              <view class="order-content">
                <text class="order-desc">{{ item.orderDesc }}</text>
                <text class="order-amount">¥{{ item.orderAmount }}</text>
              </view>
            </view>

            <!-- 群订单卡片 -->
            <view class="group-order-card" v-if="item.type === 'groupOrder'" @tap="viewGroupOrder(item.orderId)">
              <view class="group-order-header">
                <uni-icons type="shop" size="20" color="#FF6B35"></uni-icons>
                <text class="group-order-title">群订单</text>
              </view>
              <view class="group-order-content">
                <view class="group-order-avatars">
                  <image
                    class="avatar-item"
                    :src="avatar"
                    mode="aspectFill"
                    v-for="(avatar, index) in item.avatars"
                    :key="index"
                  ></image>
                  <view class="avatar-count">+{{ item.joinCount }}</view>
                </view>
                <text class="group-order-amount">¥{{ item.totalAmount }}</text>
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
                color="#fff"
              ></uni-icons>
              <uni-icons
                v-else-if="item.status === 'success'"
                type="checkmarkempty"
                size="14"
                color="#fff"
              ></uni-icons>
              <uni-icons
                v-else-if="item.status === 'fail'"
                type="close"
                size="14"
                color="#fff"
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
        <button class="tool-btn" @tap="createGroupOrder">
          <uni-icons type="cart" size="22" color="#666"></uni-icons>
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
        <view class="menu-title">更多操作</view>
        <view class="menu-list">
          <view class="menu-item" @tap="viewGroupDetail">
            <uni-icons type="person" size="20" color="#666"></uni-icons>
            <text class="menu-label">群详情</text>
          </view>
          <view class="menu-item" @tap="viewGroupOrder">
            <uni-icons type="shop" size="20" color="#666"></uni-icons>
            <text class="menu-label">群订单</text>
          </view>
          <view class="menu-item" @tap="searchHistory">
            <uni-icons type="search" size="20" color="#666"></uni-icons>
            <text class="menu-label">搜索记录</text>
          </view>
          <view class="menu-item" @tap="clearHistory">
            <uni-icons type="trash" size="20" color="#666"></uni-icons>
            <text class="menu-label">清空记录</text>
          </view>
          <view class="menu-item danger" @tap="quitGroup">
            <uni-icons type="close" size="20" color="#F5222D"></uni-icons>
            <text class="menu-label">退出群聊</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import config from '@/config/index.js'
import { groupApi } from '@/api/modules/group.js'
import WebSocketClient from '@/utils/websocket.js'

// 当前用户ID
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

  if (options && options.id) {
    groupInfo.value.id = options.id
    // 可以从参数中获取群名称、头像等
    if (options.name) {
      groupInfo.value.name = decodeURIComponent(options.name)
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
 * 连接WebSocket - IM-010: 连接WebSocket（群聊）
 */
const connectWebSocket = async () => {
  try {
    if (!currentUserId.value || !token.value) {
      return
    }

    // 构建WebSocket URL（群聊）
    const wsUrl = `${config.wsURL}/chat?userId=${encodeURIComponent(currentUserId.value)}&token=${encodeURIComponent(token.value)}`

    // 创建WebSocket客户端
    wsClient = new WebSocketClient(wsUrl)

    // 监听连接成功
    wsClient.on('_connected', () => {
      console.log('[GroupChat] WebSocket已连接')
    })

    // 监听消息
    wsClient.on('message', (data) => {
      console.log('[GroupChat] 收到消息', data)
      handleMessage(data)
    })

    // 连接
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
 * 断开WebSocket - IM-010
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
 * 加载消息 - IM-011: 调用API获取群消息列表
 */
const loadMessages = async () => {
  try {
    uni.showLoading({ title: '加载中...' })

    // IM-011: 调用API获取群消息列表
    const res = await groupApi.getMessages(groupInfo.value.id, {
      page: currentPage.value,
      size: pageSize
    })

    uni.hideLoading()

    if (res.code === 200 && res.data) {
      // 转换消息格式
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

      // 滚动到底部
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

    // 如果API调用失败，使用模拟数据（开发阶段）
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
 * 加载历史消息
 */
const loadMoreMessages = async () => {
  if (!hasMoreHistory.value || loadingHistory.value) return

  loadingHistory.value = true

  try {
    currentPage.value++

    // IM-011: 调用API获取历史消息
    const res = await groupApi.getMessages(groupInfo.value.id, {
      page: currentPage.value,
      size: pageSize
    })

    if (res.code === 200 && res.data) {
      if (res.data.length === 0) {
        hasMoreHistory.value = false
      } else {
        // 转换消息格式并插入到列表顶部
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
 * 发送消息 - IM-012: 发送群消息到服务器
 */
const sendMessage = async () => {
  if (!canSend.value) return

  const newMessage = {
    id: Date.now(),
    isSelf: true,
    userId: currentUserId.value,
    nickname: '我',
    avatar: uni.getStorageSync('avatar') || 'https://via.placeholder.com/80/FAAD14/FFFFFF?text=我',
    isGroup: true,
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
    // IM-012: 调用API发送群消息
    const res = await groupApi.sendMessage({
      groupId: groupInfo.value.id,
      senderId: currentUserId.value,
      type: 'text',
      content: contentToSend,
      quote: newMessage.quote
    })

    if (res.code === 200) {
      newMessage.status = 'success'

      // 同时通过WebSocket发送（实时通信）
      if (wsClient && wsClient.isConnected()) {
        wsClient.send({
          type: 'group_message',
          groupId: groupInfo.value.id,
          senderId: currentUserId.value,
          dataType: 'text',
          content: contentToSend,
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
 * 发送图片消息
 */
const sendImageMessage = (imagePath) => {
  const newMessage = {
    id: Date.now(),
    isSelf: true,
    userId: 0,
    nickname: '我',
    avatar: 'https://via.placeholder.com/80/FAAD14/FFFFFF?text=我',
    isGroup: true,
    type: 'image',
    content: imagePath,
    time: new Date(),
    showTime: true,
    status: 'sending'
  }

  messageList.value.push(newMessage)
  scrollToBottom()
}

/**
 * 选择菜品
 */
const chooseDish = () => {
  uni.showToast({
    title: '选择菜品',
    icon: 'none'
  })
}

/**
 * 创建群订单
 */
const createGroupOrder = () => {
  uni.navigateTo({
    url: `/pages/group-order/create?groupId=${groupInfo.value.id}`
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
 * 查看群订单
 */
const viewGroupOrder = (orderId) => {
  uni.navigateTo({
    url: `/pages/group-order/detail?id=${orderId}`
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
 * 显示群详情
 */
const showGroupDetail = () => {
  uni.navigateTo({
    url: `/pages-common/chat/group-detail?id=${groupInfo.value.id}`
  })
}

/**
 * 查看公告
 */
const viewNotice = () => {
  uni.showModal({
    title: '群公告',
    content: groupInfo.value.notice,
    showCancel: false
  })
}

/**
 * 显示成员信息 - IM-013: 显示成员信息卡片
 */
const showMemberInfo = (userId) => {
  // 跳转到成员详情页面
  uni.navigateTo({
    url: `/pages-common/chat/member-card?userId=${userId}&groupId=${groupInfo.value.id}`
  })
}

/**
 * 显示消息菜单 - IM-014: 消息操作菜单（复制、引用、撤回）
 */
const showMessageMenu = (message) => {
  // 判断是否可以撤回（2分钟内且是自己发送的消息）
  const canRecall = message.isSelf &&
    (new Date() - new Date(message.time)) < 2 * 60 * 1000

  const menuOptions = [
    { label: '复制', value: 'copy' },
    { label: '引用', value: 'quote' }
  ]

  if (canRecall) {
    menuOptions.push({ label: '撤回', value: 'recall' })
  }

  uni.showActionSheet({
    itemList: menuOptions.map(opt => opt.label),
    success: (res) => {
      const action = menuOptions[res.tapIndex].value

      switch (action) {
        case 'copy':
          // 复制消息内容
          uni.setClipboardData({
            data: message.content,
            success: () => {
              uni.showToast({ title: '已复制', icon: 'success' })
            }
          })
          break

        case 'quote':
          // 引用消息
          replyMessage.value = {
            author: message.nickname,
            content: message.content
          }
          break

        case 'recall':
          // 撤回消息
          recallMessage(message)
          break
      }
    }
  })
}

/**
 * 撤回消息 - IM-014
 */
const recallMessage = async (message) => {
  try {
    // 调用撤回消息API（假设后端有这个接口）
    // const res = await messageApi.recallMessage(message.id)

    // 从列表中移除消息
    const index = messageList.value.findIndex(m => m.id === message.id)
    if (index > -1) {
      messageList.value.splice(index, 1)
    }

    // 添加系统提示
    messageList.value.push({
      id: Date.now(),
      isSelf: false,
      isGroup: true,
      type: 'system',
      content: '你撤回了一条消息',
      time: new Date(),
      showTime: true
    })

    uni.showToast({ title: '消息已撤回', icon: 'success' })
  } catch (error) {
    console.error('撤回消息失败:', error)
    uni.showToast({
      title: error.message || '撤回失败',
      icon: 'none'
    })
  }
}

/**
 * 显示更多菜单
 */
const showMoreMenu = () => {
  morePopup.value?.open()
}

/**
 * 搜索记录 - IM-015: 跳转到搜索页面
 */
const searchHistory = () => {
  morePopup.value?.close()

  // IM-015: 跳转到搜索页面
  uni.navigateTo({
    url: `/pages-common/chat/search?groupId=${groupInfo.value.id}&type=group`
  })
}

/**
 * 清空记录
 */
const clearHistory = () => {
  morePopup.value?.close()
  uni.showModal({
    title: '清空记录',
    content: '确定清空所有聊天记录吗？',
    success: (res) => {
      if (res.confirm) {
        messageList.value = []
      }
    }
  })
}

/**
 * 退出群聊 - IM-016: 调用退出群聊API
 */
const quitGroup = async () => {
  morePopup.value?.close()

  uni.showModal({
    title: '退出群聊',
    content: '确定退出该群聊吗？',
    confirmColor: '#F5222D',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '退出中...' })

          // IM-016: 调用退出群聊API
          const apiRes = await groupApi.leaveGroup(groupInfo.value.id)

          uni.hideLoading()

          if (apiRes.code === 200) {
            uni.showToast({
              title: '已退出群聊',
              icon: 'success'
            })
            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          } else {
            throw new Error(apiRes.message || '退出失败')
          }
        } catch (error) {
          console.error('退出群聊失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '退出失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.group-chat-container {
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

.group-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 12rpx;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.group-name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.group-members {
  font-size: 22rpx;
  color: #999;
}

/* 群公告 */
.group-notice {
  background: #FFF7E6;
  padding: 15rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.notice-text {
  flex: 1;
  font-size: 24rpx;
  color: #FF6B35;
  @include text-ellipsis;
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
  gap: 15rpx;
}

.message-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.member-name {
  font-size: 22rpx;
  color: #999;
  margin-top: 10rpx;
  align-self: flex-start;
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
  color: #52C41A;
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

.group-order-card {
  background: linear-gradient(135deg, #FF6B35, #FF8C5A);
  border-radius: 12rpx;
  padding: 20rpx;
}

.group-order-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 15rpx;
}

.group-order-title {
  font-size: 26rpx;
  color: #fff;
  font-weight: 500;
}

.group-order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-order-avatars {
  display: flex;
  align-items: center;
}

.avatar-item {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 2rpx solid #fff;
  margin-left: -10rpx;

  &:first-child {
    margin-left: 0;
  }
}

.avatar-count {
  width: 50rpx;
  height: 50rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  font-size: 20rpx;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -10rpx;
}

.group-order-amount {
  font-size: 32rpx;
  color: #fff;
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

.menu-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 30rpx;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx;
  background: #F5F5F5;
  border-radius: 12rpx;

  &.danger .menu-label {
    color: #F5222D;
  }
}

.menu-label {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}
</style>
