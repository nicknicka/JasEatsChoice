<template>
  <view class="chat-detail-container">
    <!-- 顶部用户信息栏 -->
    <view class="chat-header">
      <view class="user-info-section" @tap="viewUserInfo">
        <image class="user-avatar" :src="userInfo.avatar" mode="aspectFill"></image>
        <view class="user-info">
          <text class="user-name">{{ userInfo.name }}</text>
          <text class="user-status" :class="{ online: userInfo.online }">
            {{ userInfo.online ? '在线' : '离线' }}
          </text>
        </view>
      </view>
      <view class="header-actions">
        <button class="action-btn" @tap="viewOrder">
          <uni-icons type="shop" size="18" color="#666"></uni-icons>
        </button>
      </view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="message-list"
      scroll-y
      :scroll-into-view="scrollIntoView"
      :scroll-with-animation="true"
      @scrolltoupper="loadMoreMessages"
    >
      <!-- 时间分割线 -->
      <view class="time-divider" v-for="(time, index) in messageTimes" :key="'time-' + index">
        <text class="time-text">{{ time }}</text>
      </view>

      <!-- 消息项 -->
      <view
        :id="'message-' + msg.id"
        class="message-item"
        :class="{ 'is-self': msg.isSelf }"
        v-for="msg in messageList"
        :key="msg.id"
        @longpress="showMessageMenu(msg)"
      >
        <!-- 对方消息 -->
        <template v-if="!msg.isSelf">
          <image class="message-avatar" :src="userInfo.avatar" mode="aspectFill"></image>
          <view class="message-content-wrapper">
            <!-- 消息内容 -->
            <view class="message-bubble" :class="'type-' + msg.type">
              <text class="message-text" v-if="msg.type === 'text'">{{ msg.content }}</text>
              <image
                class="message-image"
                v-else-if="msg.type === 'image'"
                :src="msg.content"
                mode="widthFix"
                @tap="previewImage(msg.content)"
              ></image>
              <!-- 订单卡片 -->
              <view class="order-card" v-else-if="msg.type === 'orderCard'">
                <view class="order-header">
                  <uni-icons type="shop" size="16" color="#FF6B35"></uni-icons>
                  <text class="order-no">{{ msg.content.orderNo }}</text>
                </view>
                <view class="order-info">
                  <text class="order-dishes">{{ msg.content.dishes }}</text>
                  <text class="order-amount">¥{{ msg.content.amount }}</text>
                </view>
                <button class="order-btn" @tap.stop="viewOrderDetail(msg.content.orderId)">
                  查看订单
                </button>
              </view>
            </view>
            <text class="message-time">{{ msg.time }}</text>
          </view>
        </template>

        <!-- 自己的消息 -->
        <template v-else>
          <view class="message-content-wrapper self">
            <view class="message-bubble self" :class="'type-' + msg.type">
              <text class="message-text" v-if="msg.type === 'text'">{{ msg.content }}</text>
              <image
                class="message-image"
                v-else-if="msg.type === 'image'"
                :src="msg.content"
                mode="widthFix"
                @tap="previewImage(msg.content)"
              ></image>
            </view>
            <text class="message-time">{{ msg.time }}</text>
            <view class="message-status">
              <uni-icons
                v-if="msg.status === 'sending'"
                type="spinner-cycle"
                size="14"
                color="#999"
              ></uni-icons>
              <uni-icons
                v-else-if="msg.status === 'sent' && msg.read"
                type="checkmarkempty"
                size="14"
                color="#52C41A"
              ></uni-icons>
            </view>
          </view>
          <image class="message-avatar" :src="merchantInfo.avatar" mode="aspectFill"></image>
        </template>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="loadingMore">
        <text>加载中...</text>
      </view>
    </scroll-view>

    <!-- 输入栏 -->
    <view class="input-bar">
      <button class="icon-btn" @tap="chooseImage">
        <uni-icons type="image" size="24" color="#666"></uni-icons>
      </button>
      <button class="icon-btn" @tap="sendOrderCard">
        <uni-icons type="shop" size="24" color="#666"></uni-icons>
      </button>
      <view class="input-wrapper">
        <input
          class="message-input"
          v-model="inputContent"
          placeholder="输入消息..."
          @confirm="sendMessage"
        />
      </view>
      <button class="send-btn" :class="{ active: inputContent.trim() }" @tap="sendMessage">
        发送
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { formatTime } from '@/utils/helper'

const userInfo = ref({
  id: 1,
  name: '张同学',
  avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=张',
  online: true
})

const merchantInfo = ref({
  id: 1,
  name: '老王家常菜',
  avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=店'
})

const messageList = ref([])
const messageTimes = ref(['今天 12:30', '今天 14:20'])
const scrollIntoView = ref('')
const loadingMore = ref(false)
const inputContent = ref('')

onMounted(() => {
  loadMessages()
})

const loadMessages = () => {
  // 模拟消息数据
  messageList.value = [
    {
      id: 1,
      isSelf: false,
      type: 'text',
      content: '您好，请问宫保鸡丁还有吗？',
      time: '12:30',
      status: 'read',
      read: true
    },
    {
      id: 2,
      isSelf: true,
      type: 'text',
      content: '您好！宫保鸡丁还有的，现在下单吗？',
      time: '12:31',
      status: 'read',
      read: true
    },
    {
      id: 3,
      isSelf: false,
      type: 'orderCard',
      content: {
        orderId: 123,
        orderNo: 'OD202603180001',
        dishes: '宫保鸡丁 x1',
        amount: '28.00'
      },
      time: '12:35',
      status: 'read',
      read: true
    }
  ]

  nextTick(() => {
    scrollToBottom()
  })
}

const scrollToBottom = () => {
  if (messageList.value.length > 0) {
    const lastMsg = messageList.value[messageList.value.length - 1]
    scrollIntoView.value = 'message-' + lastMsg.id
  }
}

const loadMoreMessages = () => {
  if (loadingMore.value) return
  loadingMore.value = true
  setTimeout(() => {
    loadingMore.value = false
  }, 1000)
}

const sendMessage = () => {
  const content = inputContent.value.trim()
  if (!content) return

  const newMessage = {
    id: Date.now(),
    isSelf: true,
    type: 'text',
    content: content,
    time: formatTime(new Date()),
    status: 'sending',
    read: false
  }

  messageList.value.push(newMessage)
  inputContent.value = ''

  nextTick(() => {
    scrollToBottom()
  })

  setTimeout(() => {
    newMessage.status = 'sent'
    messageList.value = [...messageList.value]
  }, 1000)
}

const showMessageMenu = (msg) => {
  uni.showActionSheet({
    itemList: ['复制', '删除'],
    success: (res) => {
      if (res.tapIndex === 0) {
        copyMessage(msg)
      } else if (res.tapIndex === 1) {
        deleteMessage(msg)
      }
    }
  })
}

const copyMessage = (msg) => {
  uni.setClipboardData({
    data: msg.content,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

const deleteMessage = (msg) => {
  uni.showModal({
    title: '删除消息',
    content: '确定删除这条消息吗？',
    success: (res) => {
      if (res.confirm) {
        const index = messageList.value.findIndex(m => m.id === msg.id)
        if (index !== -1) {
          messageList.value.splice(index, 1)
        }
      }
    }
  })
}

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const newMessage = {
        id: Date.now(),
        isSelf: true,
        type: 'image',
        content: res.tempFilePaths[0],
        time: formatTime(new Date()),
        status: 'sending',
        read: false
      }
      messageList.value.push(newMessage)
      nextTick(() => scrollToBottom())
    }
  })
}

const previewImage = (url) => {
  uni.previewImage({ urls: [url], current: url })
}

const sendOrderCard = () => {
  uni.navigateTo({
    url: `/pages-merchant/order/select?userId=${userInfo.value.id}`
  })
}

const viewOrderDetail = (orderId) => {
  uni.navigateTo({
    url: `/pages-merchant/order/detail?id=${orderId}`
  })
}

const viewUserInfo = () => {
  uni.showToast({ title: '查看用户信息', icon: 'none' })
}

const viewOrder = () => {
  uni.navigateTo({
    url: `/pages-merchant/order/list?userId=${userInfo.value.id}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.chat-detail-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F5F5F5;
}

.chat-header {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #eee;
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.user-name {
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
}

.user-status {
  font-size: 24rpx;
  color: #999;

  &.online {
    color: #52C41A;
  }
}

.action-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F5F5F5;
  @include flex-center;
  border: none;
}

.message-list {
  flex: 1;
  padding: 20rpx;
}

.time-divider {
  text-align: center;
  padding: 20rpx 0;
}

.time-text {
  font-size: 24rpx;
  color: #999;
  padding: 8rpx 20rpx;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 20rpx;
}

.message-item {
  display: flex;
  margin-bottom: 30rpx;
}

.message-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-content-wrapper {
  max-width: 500rpx;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
  margin-left: 20rpx;

  &.self {
    align-items: flex-end;
    margin-left: 0;
    margin-right: 20rpx;
  }
}

.message-bubble {
  padding: 20rpx;
  background: #fff;
  border-radius: 12rpx;

  &.self {
    background: #FF6B35;
  }

  &.type-image,
  &.type-orderCard {
    padding: 0;
    background: transparent;
  }
}

.message-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;

  .message-bubble.self & {
    color: #fff;
  }
}

.message-image {
  max-width: 400rpx;
  border-radius: 12rpx;
}

.order-card {
  background: #fff;
  border-radius: 12rpx;
  overflow: hidden;
  width: 400rpx;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.order-no {
  font-size: 26rpx;
  color: #666;
}

.order-info {
  padding: 20rpx;
  display: flex;
  justify-content: space-between;
}

.order-dishes {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.order-amount {
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
}

.order-btn {
  margin: 0 20rpx 20rpx;
  height: 60rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 26rpx;
  border-radius: 30rpx;
  border: none;
}

.message-time {
  font-size: 22rpx;
  color: #999;
}

.message-status {
  margin-left: 10rpx;
}

.load-status {
  text-align: center;
  padding: 20rpx 0;
  color: #999;
  font-size: 26rpx;
}

.input-bar {
  background: #fff;
  padding: 15rpx 20rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
  border-top: 1rpx solid #eee;
}

.icon-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F5F5F5;
  @include flex-center;
  border: none;
}

.input-wrapper {
  flex: 1;
  background: #F5F5F5;
  border-radius: 30rpx;
  padding: 0 20rpx;
  height: 70rpx;
  @include flex-center;
}

.message-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.send-btn {
  padding: 0 30rpx;
  height: 70rpx;
  background: #F5F5F5;
  color: #999;
  font-size: 28rpx;
  border-radius: 35rpx;
  border: none;
  @include flex-center;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
