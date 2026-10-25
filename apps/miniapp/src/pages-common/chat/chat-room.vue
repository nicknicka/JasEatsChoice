<template>
  <view class="chat-room-container">
    <ChatRoomHeader
      :user-info="userInfo"
      @back="goBack"
      @show-user-info="showUserInfo"
      @show-more="showMoreMenu"
    />

    <MessageList
      :messages="messageList"
      :scroll-into-view="scrollIntoView"
      :has-more="hasMoreHistory"
      :loading="loadingHistory"
      @load-more="loadMoreMessages"
      @image-tap="previewImage"
      @dish-tap="viewDish"
      @order-tap="viewOrder"
      @longpress="showMessageMenu"
    />

    <view class="input-dock">
      <MessageInput
        :reply-message="replyMessage"
        :show-dish-button="true"
        :show-group-order-button="false"
        @send="handleSendMessage"
        @cancel-reply="cancelReply"
        @choose-image="chooseImage"
        @choose-dish="chooseDish"
      />
    </view>

    <uni-popup ref="morePopup" type="bottom">
      <view class="more-menu">
        <view class="menu-handle"></view>
        <view class="menu-title">会话操作</view>
        <view class="menu-grid">
          <view class="menu-item" @tap="clearHistory">
            <view class="menu-icon soft">
              <uni-icons type="trash" size="22" color="#FF6B35" />
            </view>
            <text class="menu-label">清空记录</text>
          </view>
          <view class="menu-item" @tap="searchMessage">
            <view class="menu-icon soft">
              <uni-icons type="search" size="22" color="#FF6B35" />
            </view>
            <text class="menu-label">搜索记录</text>
          </view>
          <view class="menu-item" @tap="reportUser">
            <view class="menu-icon soft">
              <uni-icons type="flag" size="22" color="#FF6B35" />
            </view>
            <text class="menu-label">举报用户</text>
          </view>
          <view class="menu-item danger" @tap="addToBlacklist">
            <view class="menu-icon danger">
              <uni-icons type="close" size="22" color="#F5222D" />
            </view>
            <text class="menu-label">加入黑名单</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import ChatRoomHeader from './chat-room/components/ChatRoomHeader.vue'
import MessageList from './chat-room/components/MessageList.vue'
import MessageInput from '@/components/business/chat/MessageInput.vue'
import { useChatRoom } from '@/composables/chat/useChatRoom'

const {
  userInfo,
  messageList,
  loadingHistory,
  hasMoreHistory,
  scrollIntoView,
  replyMessage,
  loadMoreMessages,
  sendMessage: sendChatMessage,
  setReplyMessage,
  cancelReply
} = useChatRoom()

const morePopup = ref(null)

const handleSendMessage = ({ content }) => {
  sendChatMessage(content)
}

const goBack = () => {
  uni.navigateBack()
}

const showUserInfo = () => {
  uni.showToast({
    title: `查看${userInfo.value.name || '用户'}资料`,
    icon: 'none'
  })
}

const previewImage = (url) => {
  uni.previewImage({
    urls: [url],
    current: url
  })
}

const viewDish = (dishId) => {
  if (!dishId) return
  uni.navigateTo({
    url: `/pages-user/dish/detail/index?id=${dishId}`
  })
}

const viewOrder = (orderId) => {
  if (!orderId) return
  uni.navigateTo({
    url: `/pages-user/order/detail/index?id=${orderId}`
  })
}

const showMessageMenu = (message) => {
  const itemList = ['复制', '引用']

  if (message.isSelf) {
    itemList.push('撤回')
  }

  uni.showActionSheet({
    itemList,
    success: (res) => {
      const action = itemList[res.tapIndex]

      if (action === '复制') {
        uni.setClipboardData({
          data: String(message.content || ''),
          success: () => uni.showToast({ title: '已复制', icon: 'success' })
        })
        return
      }

      if (action === '引用') {
        setReplyMessage(message)
        return
      }

      if (action === '撤回') {
        recallMessage(message)
      }
    }
  })
}

const recallMessage = (message) => {
  const index = messageList.value.findIndex(item => item.id === message.id)
  if (index === -1) return

  messageList.value.splice(index, 1)
  uni.showToast({
    title: '消息已撤回',
    icon: 'success'
  })
}

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      console.log('选择图片:', res.tempFilePaths[0])
      uni.showToast({
        title: '图片发送逻辑待接入',
        icon: 'none'
      })
    }
  })
}

const chooseDish = () => {
  uni.navigateTo({
    url: '/pages-user/dish/list/index'
  })
}

const showMoreMenu = () => {
  morePopup.value?.open()
}

const clearHistory = () => {
  morePopup.value?.close()
  uni.showModal({
    title: '清空记录',
    content: '确定清空当前会话的聊天记录吗？',
    success: (res) => {
      if (res.confirm) {
        messageList.value = []
      }
    }
  })
}

const searchMessage = () => {
  morePopup.value?.close()
  uni.showToast({
    title: '消息搜索待接入',
    icon: 'none'
  })
}

const reportUser = () => {
  morePopup.value?.close()
  uni.showToast({
    title: '举报入口待接入',
    icon: 'none'
  })
}

const addToBlacklist = () => {
  morePopup.value?.close()
  uni.showModal({
    title: '加入黑名单',
    content: '确定不再接收该用户的消息吗？',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '已加入黑名单',
          icon: 'success'
        })
        setTimeout(() => {
          uni.navigateBack()
        }, 1200)
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.chat-room-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background:
    radial-gradient(circle at top, rgba(255, 238, 228, 0.95), transparent 42%),
    linear-gradient(180deg, #fffaf7 0%, #f7f4ef 52%, #f4f1eb 100%);
}

.input-dock {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 30;
}

.more-menu {
  background: #fffdfb;
  border-radius: 36rpx 36rpx 0 0;
  padding: 18rpx 30rpx calc(44rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -12rpx 40rpx rgba(123, 86, 53, 0.08);
}

.menu-handle {
  width: 96rpx;
  height: 8rpx;
  margin: 0 auto 20rpx;
  border-radius: 999rpx;
  background: rgba(51, 51, 51, 0.12);
}

.menu-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $text-color-primary;
  text-align: center;
  margin-bottom: 28rpx;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
}

.menu-item {
  @include flex-center-column;
  gap: 14rpx;
}

.menu-icon {
  width: 92rpx;
  height: 92rpx;
  border-radius: 28rpx;
  @include flex-center;

  &.soft {
    background: #fff3ec;
  }

  &.danger {
    background: #fff1f0;
  }
}

.menu-label {
  font-size: 24rpx;
  color: $text-color-primary;
  text-align: center;
}

.danger .menu-label {
  color: $danger-color;
}
</style>
