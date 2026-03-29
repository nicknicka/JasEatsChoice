<template>
  <view class="conversation-list-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <uni-icons type="search" size="18" color="#999"></uni-icons>
      <input
        class="search-input"
        v-model="searchKeyword"
        placeholder="搜索联系人"
        @input="onSearch"
      />
    </view>

    <!-- 会话列表 -->
    <scroll-view
      class="conversation-scroll"
      scroll-y
      @scrolltolower="loadMore"
    >
      <view
        class="conversation-item"
        v-for="item in conversationList"
        :key="item.id"
        @tap="openChat(item)"
        @longpress="showLongPressMenu(item)"
      >
        <!-- 头像 -->
        <view class="avatar-wrapper">
          <image
            class="avatar"
            :src="item.avatar"
            mode="aspectFill"
          ></image>
          <view class="online-badge" v-if="item.isOnline"></view>
          <view class="unread-badge" v-if="item.unread > 0">
            {{ item.unread > 99 ? '99+' : item.unread }}
          </view>
        </view>

        <!-- 内容 -->
        <view class="conversation-content">
          <view class="content-header">
            <text class="name">{{ item.name }}</text>
            <text class="time">{{ formatTime(item.lastTime) }}</text>
          </view>
          <view class="content-body">
            <text class="last-message">{{ item.lastMessage }}</text>
            <view class="message-type" v-if="item.lastMessageType !== 'text'">
              <uni-icons
                :type="getMessageIcon(item.lastMessageType)"
                size="14"
                color="#999"
              ></uni-icons>
            </view>
          </view>
        </view>

        <!-- 置顶标识 -->
        <view class="pin-badge" v-if="item.isPinned">
          <uni-icons type="star-filled" size="16" color="#FFA500"></uni-icons>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="hasMore">
        <text v-if="loading">加载中...</text>
        <text v-else>上拉加载更多</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="conversationList.length === 0 && !loading">
        <empty text="暂无会话" icon="💬" />
      </view>
    </scroll-view>

    <!-- 悬浮按钮 -->
    <view class="fab-buttons">
      <view class="fab-btn" @tap="showSystemNotifications">
        <uni-icons type="notification" size="24" color="#fff"></uni-icons>
        <view class="fab-badge" v-if="systemUnread > 0">
          {{ systemUnread }}
        </view>
      </view>
      <view class="fab-btn primary" @tap="showNewChatMenu">
        <uni-icons type="plus" size="24" color="#fff"></uni-icons>
      </view>
    </view>

    <!-- 新建聊天菜单 -->
    <uni-popup ref="newChatPopup" type="bottom">
      <view class="new-chat-menu">
        <view class="menu-title">发起聊天</view>
        <view class="menu-list">
          <view class="menu-item" @tap="createSingleChat">
            <uni-icons type="person" size="24" color="#FF6B35"></uni-icons>
            <text class="menu-text">单聊</text>
          </view>
          <view class="menu-item" @tap="createGroupChat">
            <uni-icons type="person-filled" size="24" color="#FF6B35"></uni-icons>
            <text class="menu-text">群聊</text>
          </view>
          <view class="menu-item" @tap="createGroupOrder">
            <uni-icons type="shop" size="24" color="#FF6B35"></uni-icons>
            <text class="menu-text">群订单</text>
          </view>
        </view>
        <view class="menu-cancel" @tap="closeNewChatMenu">取消</view>
      </view>
    </uni-popup>

    <!-- 长按菜单 -->
    <uni-popup ref="longPressPopup" type="dialog">
      <uni-popup-dialog
        type="info"
        :title="selectedConversation?.name"
        :content="longPressMenuOptions.map(opt => opt.label).join('\n')"
        :duration="0"
        @confirm="handleLongPressAction"
      ></uni-popup-dialog>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { conversationApi } from '@/api/modules/conversation.js'
import { formatRelativeTime } from '@/utils/helper'

// 当前用户ID
const currentUserId = ref('')

// 搜索关键词
const searchKeyword = ref('')

// 会话列表
const conversationList = ref([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

// 系统通知未读数
const systemUnread = ref(3)

// 选中的会话和操作
const selectedConversation = ref(null)
const selectedAction = ref('')

const longPressMenuOptions = [
  { label: '置顶', value: 'pin' },
  { label: '取消置顶', value: 'unpin' },
  { label: '标为已读', value: 'markRead' },
  { label: '删除', value: 'delete' }
]

// 弹窗引用
const newChatPopup = ref(null)
const longPressPopup = ref(null)

onMounted(() => {
  // 获取当前用户ID
  currentUserId.value = uni.getStorageSync('userId') || ''

  loadConversations()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

let pollingTimer = null

/**
 * 轮询更新
 */
const startPolling = () => {
  pollingTimer = setInterval(() => {
    loadConversations(true)
  }, 30000)
}

const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

/**
 * 加载会话列表 - IM-029: 调用API获取会话列表
 */
const loadConversations = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
  }

  try {
    // IM-029: 调用API获取会话列表
    const res = await conversationApi.getList(currentUserId.value)

    if (res.code === 200 && res.data) {
      // 转换会话数据格式
      const formattedConversations = res.data.map(conv => ({
        id: conv.id,
        name: conv.name || conv.displayName || '未命名',
        avatar: conv.avatar || '/static/default-avatar.png',
        isGroup: conv.type === 'group',
        isOnline: conv.isOnline || false,
        isPinned: conv.isPinned || false,
        unread: conv.unreadCount || 0,
        lastMessage: conv.lastMessage?.content || '',
        lastMessageType: conv.lastMessage?.type || 'text',
        lastTime: conv.lastMessageTime || conv.updateTime || new Date()
      }))

      // 排序：置顶的在前，然后按时间排序
      formattedConversations.sort((a, b) => {
        if (a.isPinned && !b.isPinned) return -1
        if (!a.isPinned && b.isPinned) return 1
        return new Date(b.lastTime) - new Date(a.lastTime)
      })

      if (isRefresh) {
        conversationList.value = formattedConversations
      } else {
        conversationList.value = [...conversationList.value, ...formattedConversations]
      }

      // 判断是否还有更多数据
      if (formattedConversations.length < pageSize) {
        hasMore.value = false
      }

      console.log('加载会话成功，数量:', formattedConversations.length)
    } else {
      throw new Error(res.message || '获取会话列表失败')
    }
  } catch (error) {
    console.error('加载会话失败:', error)

    // 开发阶段：使用模拟数据
    const mockData = generateMockConversations()
    if (isRefresh) {
      conversationList.value = mockData
    } else {
      conversationList.value = [...conversationList.value, ...mockData]
    }

    if (mockData.length < pageSize) {
      hasMore.value = false
    }
  } finally {
    loading.value = false
  }
}

/**
 * 生成模拟会话数据
 */
const generateMockConversations = () => {
  const conversations = []
  const count = Math.floor(Math.random() * 5) + 5

  const names = ['张三', '李四', '老王家常菜', '王五', '美食群', '同学聚会群']
  const messages = [
    '你好，在吗？',
    '订单已发货',
    '今天的菜品很不错',
    '[图片]',
    '[菜品卡片]',
    '好的，我知道了'
  ]

  for (let i = 0; i < count; i++) {
    const isGroup = Math.random() > 0.7
    conversations.push({
      id: page.value * 20 + i,
      name: names[i % names.length],
      avatar: `https://via.placeholder.com/80/FF6B35/FFFFFF?text=${names[i % names.length][0]}`,
      isGroup,
      isOnline: Math.random() > 0.5,
      isPinned: Math.random() > 0.8,
      unread: Math.floor(Math.random() * 5),
      lastMessage: messages[Math.floor(Math.random() * messages.length)],
      lastMessageType: Math.random() > 0.8 ? 'image' : 'text',
      lastTime: new Date(Date.now() - Math.random() * 3600000)
    })
  }

  return conversations
}

/**
 * 搜索
 */
const onSearch = () => {
  loadConversations(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    page.value++
    loadConversations()
  }
}

/**
 * 获取消息图标
 */
const getMessageIcon = (type) => {
  const iconMap = {
    image: 'image',
    dish: 'shop',
    order: 'list',
    voice: 'mic'
  }
  return iconMap[type] || 'chatbubble'
}

/**
 * 打开聊天
 */
const openChat = (conversation) => {
  // 清除未读
  conversation.unread = 0

  const url = conversation.isGroup
    ? `/pages-common/chat/group-chat?id=${conversation.id}`
    : `/pages-common/chat/chat-room?userId=${conversation.id}`

  uni.navigateTo({ url })
}

/**
 * 长按菜单 - IM-030: 长按操作菜单（置顶、删除）
 */
const showLongPressMenu = (conversation) => {
  selectedConversation.value = conversation

  // 动态调整菜单选项
  if (conversation.isPinned) {
    longPressMenuOptions[0] = { label: '取消置顶', value: 'unpin' }
  } else {
    longPressMenuOptions[0] = { label: '置顶', value: 'pin' }
  }

  longPressPopup.value?.open()
}

/**
 * 处理长按操作 - IM-030
 */
const handleLongPressAction = async (action) => {
  if (!selectedConversation.value) return

  const conversation = selectedConversation.value
  longPressPopup.value?.close()

  try {
    switch (action) {
      case 'pin':
        // IM-034: 保存置顶状态
        await conversationApi.setPin(conversation.id, true)
        conversation.isPinned = true
        uni.showToast({ title: '已置顶', icon: 'success' })
        break

      case 'unpin':
        await conversationApi.setPin(conversation.id, false)
        conversation.isPinned = false
        uni.showToast({ title: '已取消置顶', icon: 'success' })
        break

      case 'markRead':
        // IM-035: 标记已读
        await conversationApi.markRead(conversation.id)
        conversation.unread = 0
        uni.showToast({ title: '已标为已读', icon: 'success' })
        break

      case 'delete':
        // IM-036: 删除会话
        uni.showModal({
          title: '确认删除',
          content: `确定删除与"${conversation.name}"的会话吗？`,
          success: async (res) => {
            if (res.confirm) {
              await conversationApi.delete(conversation.id)

              // 从列表中移除
              const index = conversationList.value.findIndex(c => c.id === conversation.id)
              if (index > -1) {
                conversationList.value.splice(index, 1)
              }

              uni.showToast({ title: '删除成功', icon: 'success' })
            }
          }
        })
        break
    }

    // 重新排序列表（置顶操作后）
    if (action === 'pin' || action === 'unpin') {
      conversationList.value.sort((a, b) => {
        if (a.isPinned && !b.isPinned) return -1
        if (!a.isPinned && b.isPinned) return 1
        return new Date(b.lastTime) - new Date(a.lastTime)
      })
    }
  } catch (error) {
    console.error('操作失败:', error)
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

/**
 * 显示系统通知
 */
const showSystemNotifications = () => {
  uni.navigateTo({
    url: '/pages/notification/index'
  })
}

/**
 * 显示新建聊天菜单
 */
const showNewChatMenu = () => {
  newChatPopup.value?.open()
}

/**
 * 关闭新建聊天菜单
 */
const closeNewChatMenu = () => {
  newChatPopup.value?.close()
}

/**
 * 创建单聊 - IM-031: 跳转到选择联系人页面
 */
const createSingleChat = () => {
  closeNewChatMenu()

  // IM-031: 跳转到选择联系人页面
  uni.navigateTo({
    url: '/chat/contact-selector'
  })
}

/**
 * 创建群聊
 */
const createGroupChat = () => {
  closeNewChatMenu()
  uni.navigateTo({
    url: '/chat/group-detail?action=create'
  })
}

/**
 * 创建群订单
 */
const createGroupOrder = () => {
  closeNewChatMenu()
  uni.navigateTo({
    url: '/pages/group-order/create'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.conversation-list-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 搜索栏 */
.search-bar {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.search-input {
  flex: 1;
  height: 60rpx;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 30rpx;
  font-size: 28rpx;
}

/* 会话列表 */
.conversation-scroll {
  flex: 1;
}

.conversation-item {
  position: relative;
  background: #fff;
  padding: 25rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-bottom: 1rpx solid #eee;

  &:active {
    background: #F5F5F5;
  }
}

.avatar-wrapper {
  position: relative;
  width: 100rpx;
  height: 100rpx;
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}

.online-badge {
  position: absolute;
  bottom: 2rpx;
  right: 2rpx;
  width: 20rpx;
  height: 20rpx;
  background: #52C41A;
  border: 2rpx solid #fff;
  border-radius: 50%;
}

.unread-badge {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 6rpx;
  background: #F5222D;
  color: #fff;
  font-size: 20rpx;
  border-radius: 18rpx;
  @include flex-center;
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.name {
  font-size: 30rpx;
  font-weight: 500;
  color: #333;
}

.time {
  font-size: 24rpx;
  color: #999;
  flex-shrink: 0;
}

.content-body {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.last-message {
  flex: 1;
  font-size: 26rpx;
  color: #999;
  @include text-ellipsis;
}

.message-type {
  flex-shrink: 0;
}

.pin-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  font-size: 26rpx;
  color: #999;
}

/* 空状态 */
.empty-state {
  padding-top: 200rpx;
}

/* 悬浮按钮 */
.fab-buttons {
  position: fixed;
  right: 30rpx;
  bottom: 100rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  z-index: 100;
}

.fab-btn {
  width: 100rpx;
  height: 100rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);

  &.primary {
    background: #FF6B35;
  }
}

.fab-badge {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 6rpx;
  background: #F5222D;
  color: #fff;
  font-size: 20rpx;
  border-radius: 18rpx;
  @include flex-center;
}

/* 新建聊天菜单 */
.new-chat-menu {
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
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 30rpx;
  padding: 30rpx;
  background: #F5F5F5;
  border-radius: 16rpx;
}

.menu-text {
  font-size: 30rpx;
  color: #333;
}

.menu-cancel {
  text-align: center;
  padding: 30rpx;
  font-size: 30rpx;
  color: #666;
  border-top: 1rpx solid #eee;
}
</style>
