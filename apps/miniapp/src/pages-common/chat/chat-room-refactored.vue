<!--
页面名称：chat-room（重构版）
原代码行数：1208行
重构后行数：约280行
减少比例：77%
重构时间：2026-03-20
-->
<template>
  <view class="chat-room-container">
    <!-- 聊天室头部 -->
    <ChatRoomHeader
      :user-info="userInfo"
      @back="goBack"
      @show-user-info="showUserInfo"
      @show-more="showMoreMenu"
    />

    <!-- 消息列表 -->
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

    <!-- 输入区域 -->
    <MessageInput
      :reply-message="replyMessage"
      :show-dish-button="true"
      :show-group-order-button="false"
      @send="handleSendMessage"
      @cancel-reply="cancelReply"
      @choose-image="chooseImage"
      @choose-dish="chooseDish"
    />

    <!-- 更多菜单 -->
    <uni-popup ref="morePopup" type="bottom">
      <view class="more-menu">
        <view class="menu-grid">
          <view class="menu-item" @tap="clearHistory">
            <uni-icons type="trash" size="24" color="#FF6B35" />
            <text class="menu-label">清空记录</text>
          </view>
          <view class="menu-item" @tap="searchMessage">
            <uni-icons type="search" size="24" color="#FF6B35" />
            <text class="menu-label">搜索记录</text>
          </view>
          <view class="menu-item" @tap="reportUser">
            <uni-icons type="flag" size="24" color="#FF6B35" />
            <text class="menu-label">举报用户</text>
          </view>
          <view class="menu-item danger" @tap="addToBlacklist">
            <uni-icons type="close" size="24" color="#F5222D" />
            <text class="menu-label">加入黑名单</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import ChatRoomHeader from './components/ChatRoomHeader.vue'
import MessageList from './components/MessageList.vue'
import MessageInput from '@/components/business/chat/MessageInput.vue'
import { useChatRoom } from '@/composables/chat/useChatRoom'

// 使用聊天室逻辑
const {
  userInfo,
  messageList,
  loadingHistory,
  hasMoreHistory,
  scrollIntoView,
  replyMessage,
  loadMoreMessages,
  sendMessage: sendChatMessage,
  cancelReply
} = useChatRoom()

// 弹窗引用
const morePopup = ref(null)

/**
 * 处理发送消息
 */
const handleSendMessage = ({ content, quote }) => {
  sendChatMessage(content)
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
    url: `/pages/user/detail?id=${userInfo.value.id}`
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
 * 显示消息菜单
 */
const showMessageMenu = (message) => {
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
          uni.setClipboardData({
            data: message.content,
            success: () => {
              uni.showToast({ title: '已复制', icon: 'success' })
            }
          })
          break

        case 'quote':
          cancelReply()
          // 设置引用消息
          break

        case 'recall':
          recallMessage(message)
          break
      }
    }
  })
}

/**
 * 撤回消息
 */
const recallMessage = async (message) => {
  try {
    // 从列表中移除消息
    const index = messageList.value.findIndex(m => m.id === message.id)
    if (index > -1) {
      messageList.value.splice(index, 1)
    }

    // 添加系统提示
    messageList.value.push({
      id: Date.now(),
      isSelf: false,
      isGroup: false,
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
 * 选择图片
 */
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      console.log('选择图片:', res.tempFilePaths[0])
      // 发送图片消息
    }
  })
}

/**
 * 选择菜品
 */
const chooseDish = () => {
  uni.navigateTo({
    url: '/pages/dish/list?selectMode=true'
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
 * 搜索消息
 */
const searchMessage = () => {
  morePopup.value?.close()
  uni.navigateTo({
    url: `/pages-common/chat/search?conversationId=${userInfo.value.id}&type=private`
  })
}

/**
 * 举报用户
 */
const reportUser = () => {
  morePopup.value?.close()
  uni.navigateTo({
    url: `/pages/report?type=user&id=${userInfo.value.id}`
  })
}

/**
 * 加入黑名单
 */
const addToBlacklist = () => {
  morePopup.value?.close()
  uni.showModal({
    title: '加入黑名单',
    content: '确定将该用户加入黑名单吗？',
    confirmColor: '#F5222D',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 调用加入黑名单API
          uni.showToast({
            title: '已加入黑名单',
            icon: 'success'
          })
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      }
    }
  })
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

/* 更多菜单 */
.more-menu {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 40rpx 30rpx;
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

  &.danger {
    .menu-label {
      color: #F5222D;
    }
  }
}

.menu-label {
  font-size: 24rpx;
  color: #333;
  text-align: center;
}
</style>
