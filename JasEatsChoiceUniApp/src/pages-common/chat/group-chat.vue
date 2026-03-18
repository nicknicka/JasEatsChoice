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

// 群信息
const groupInfo = ref({
  id: 1,
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

// 输入
const inputContent = ref('')
const replyMessage = ref(null)

// 弹窗
const morePopup = ref(null)

onMounted(() => {
  loadMessages()
  scrollToBottom()
  connectWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})

let ws = null

/**
 * 连接WebSocket
 */
const connectWebSocket = () => {
  // TODO: 连接WebSocket
}

/**
 * 断开WebSocket
 */
const disconnectWebSocket = () => {
  if (ws) {
    ws.close()
    ws = null
  }
}

/**
 * 加载消息
 */
const loadMessages = async () => {
  // TODO: 调用API获取群消息列表
  setTimeout(() => {
    messageList.value = generateMockMessages()
    nextTick(() => {
      scrollToBottom()
    })
  }, 300)
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
const loadMoreMessages = () => {
  if (!hasMoreHistory.value || loadingHistory.value) return

  loadingHistory.value = true
  setTimeout(() => {
    loadingHistory.value = false
    hasMoreHistory.value = false
  }, 1000)
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
 * 发送消息
 */
const sendMessage = async () => {
  if (!canSend.value) return

  const newMessage = {
    id: Date.now(),
    isSelf: true,
    userId: 0,
    nickname: '我',
    avatar: 'https://via.placeholder.com/80/FAAD14/FFFFFF?text=我',
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
  inputContent.value = ''
  replyMessage.value = null

  scrollToBottom()

  // TODO: 发送群消息到服务器
  setTimeout(() => {
    newMessage.status = 'success'
  }, 1000)
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
 * 显示成员信息
 */
const showMemberInfo = (userId) => {
  // TODO: 显示成员信息卡片
}

/**
 * 显示消息菜单
 */
const showMessageMenu = (message) => {
  // TODO: 显示消息操作菜单（复制、引用、撤回等）
}

/**
 * 显示更多菜单
 */
const showMoreMenu = () => {
  morePopup.value?.open()
}

/**
 * 搜索记录
 */
const searchHistory = () => {
  morePopup.value?.close()
  // TODO: 跳转到搜索页面
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
 * 退出群聊
 */
const quitGroup = () => {
  morePopup.value?.close()
  uni.showModal({
    title: '退出群聊',
    content: '确定退出该群聊吗？',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用退出群聊API
        uni.showToast({
          title: '已退出群聊',
          icon: 'success'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1500)
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
