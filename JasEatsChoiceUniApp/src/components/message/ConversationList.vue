<template>
  <view class="conversation-list-wrapper">
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
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 骨架屏 -->
      <SkeletonScreen v-if="loading && conversationList.length === 0" :count="5" />

      <!-- 会话列表 -->
      <ConversationItem
        v-for="item in conversationList"
        :key="item.id"
        :conversation="item"
        @click="openChat"
        @pin="handlePin"
        @unpin="handleUnpin"
        @markRead="handleMarkRead"
        @delete="handleDelete"
        @longpress="showLongPressMenu"
      />

      <!-- 加载状态 -->
      <view class="load-status" v-if="loading">
        <text>加载中...</text>
      </view>

      <!-- 没有更多 -->
      <view class="load-status" v-else-if="!hasMore && conversationList.length > 0">
        <text>没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="conversationList.length === 0 && !loading">
        <text class="empty-icon">💬</text>
        <text class="empty-text">暂无会话</text>
        <text class="empty-tips">与商家或好友聊天后会在这里显示</text>
      </view>
    </scroll-view>

    <!-- 悬浮按钮 -->
    <view class="fab-button" @tap="showNewChatMenu">
      <uni-icons type="plus" size="24" color="#fff"></uni-icons>
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
        </view>
        <view class="menu-cancel" @tap="closeNewChatMenu">取消</view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
// import { conversationApi } from '@/api'
import { formatRelativeTime } from '../../../utils/helper'
import ConversationItem from './ConversationItem.vue'
import SkeletonScreen from './SkeletonScreen.vue'
// import { MessageUtils } from '@/utils/messageUtils'

const props = defineProps({
  searchKeyword: {
    type: String,
    default: ''
  },
  filterType: {
    type: String,
    default: 'all'
  }
})

// 会话列表
const conversationList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20

// 选中的会话
const selectedConversation = ref(null)

// 弹窗引用
const newChatPopup = ref(null)

// 未读总数
const unreadCount = computed(() => {
  return conversationList.value.reduce((sum, item) => sum + (item.unread || 0), 0)
})

let pollingTimer = null

onMounted(() => {
  loadConversations()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})

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
 * 加载会话列表
 */
const loadConversations = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    refreshing.value = true
  }

  try {
    const userId = uni.getStorageSync('userId') || ''

    // 调用API获取会话列表
    const res = await conversationApi.getList(userId)

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

      conversationList.value = formattedConversations

      // 判断是否还有更多数据
      hasMore.value = formattedConversations.length >= pageSize
    }
  } catch (error) {
    console.error('加载会话失败:', error)

    // 开发阶段：使用模拟数据
    if (page.value === 1) {
      conversationList.value = generateMockConversations()
    }
    hasMore.value = false
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟会话数据
 */
const generateMockConversations = () => {
  return [
    {
      id: 1,
      name: '老王家常菜',
      avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=老',
      isGroup: false,
      isOnline: true,
      isPinned: true,
      unread: 2,
      lastMessage: '您好，订单已经准备好了',
      lastMessageType: 'text',
      lastTime: new Date(Date.now() - 300000)
    },
    {
      id: 2,
      name: '美食交流群',
      avatar: 'https://via.placeholder.com/80/52C41A/FFFFFF?text=群',
      isGroup: true,
      isOnline: false,
      isPinned: false,
      unread: 0,
      lastMessage: '今天的菜品很不错',
      lastMessageType: 'text',
      lastTime: new Date(Date.now() - 3600000)
    }
  ]
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  loadConversations(true)
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
 * 格式化时间
 */
const formatTime = (time) => {
  return formatRelativeTime(time)
}

/**
 * 打开聊天
 */
const openChat = (conversation) => {
  // 震动反馈
  uni.vibrateShort({
    type: 'light'
  })

  // 清除未读
  const oldUnread = conversation.unread
  conversation.unread = 0

  // 触发未读变化事件
  if (oldUnread > 0) {
    emit('unread-change', Math.max(0, unreadCount.value - oldUnread))
  }

  const url = conversation.isGroup
    ? `/src/pages-common/chat/group-chat?id=${conversation.id}`
    : `/src/pages-common/chat/chat-room?userId=${conversation.id}`

  uni.navigateTo({ url })
}

/**
 * 置顶会话
 */
const handlePin = async (conversation) => {
  try {
    await conversationApi.setPin(conversation.id, true)
    conversation.isPinned = true

    // 重新排序
    conversationList.value = sortConversations(conversationList.value)

    uni.showToast({ title: '已置顶', icon: 'success' })
  } catch (error) {
    console.error('置顶失败:', error)
    uni.showToast({ title: '置顶失败', icon: 'none' })
  }
}

/**
 * 取消置顶
 */
const handleUnpin = async (conversation) => {
  try {
    await conversationApi.setPin(conversation.id, false)
    conversation.isPinned = false

    // 重新排序
    conversationList.value = sortConversations(conversationList.value)

    uni.showToast({ title: '已取消置顶', icon: 'success' })
  } catch (error) {
    console.error('取消置顶失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

/**
 * 标记已读
 */
const handleMarkRead = async (conversation) => {
  try {
    await conversationApi.markRead(conversation.id)
    const oldUnread = conversation.unread
    conversation.unread = 0

    // 触发未读变化事件
    if (oldUnread > 0) {
      emit('unread-change', Math.max(0, unreadCount.value - oldUnread))
    }

    uni.showToast({ title: '已标记为已读', icon: 'success' })
  } catch (error) {
    console.error('标记已读失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

/**
 * 删除会话
 */
const handleDelete = (conversation) => {
  uni.showModal({
    title: '确认删除',
    content: `确定删除与"${conversation.name}"的会话吗？`,
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          await conversationApi.delete(conversation.id)

          // 从列表中移除
          const index = conversationList.value.findIndex(c => c.id === conversation.id)
          if (index > -1) {
            conversationList.value.splice(index, 1)
          }

          // 触发未读变化事件
          emit('unread-change', unreadCount.value)

          uni.showToast({ title: '删除成功', icon: 'success' })
        } catch (error) {
          console.error('删除失败:', error)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 触发事件
 */
const emit = defineEmits(['unread-change'])

// 临时工具函数（替代 MessageUtils）
const sortConversations = (list) => {
  return [...list].sort((a, b) => {
    if (a.isPinned && !b.isPinned) return -1
    if (!a.isPinned && b.isPinned) return 1
    const timeA = new Date(a.lastTime || 0).getTime()
    const timeB = new Date(b.lastTime || 0).getTime()
    return timeB - timeA
  })
}

const searchConversations = (list, keyword) => {
  if (!keyword || !keyword.trim()) return list
  const lowerKeyword = keyword.toLowerCase()
  return list.filter(conv => {
    return conv.name?.toLowerCase().includes(lowerKeyword) ||
           conv.lastMessage?.toLowerCase().includes(lowerKeyword)
  })
}

const filterConversations = (list, filter) => {
  switch (filter) {
    case 'unread':
      return list.filter(conv => conv.unread > 0)
    case 'pinned':
      return list.filter(conv => conv.isPinned)
    case 'group':
      return list.filter(conv => conv.isGroup)
    default:
      return list
  }
}

/**
 * 长按菜单
 */
const showLongPressMenu = (conversation) => {
  selectedConversation.value = conversation

  uni.showActionSheet({
    itemList: conversation.isPinned ? ['取消置顶', '标为已读', '删除'] : ['置顶', '标为已读', '删除'],
    success: (res) => {
      const actions = ['pin', 'unpin', 'markRead', 'delete']
      const action = conversation.isPinned ? actions[res.tapIndex + 1] : actions[res.tapIndex]
      handleLongPressAction(action, conversation)
    }
  })
}

/**
 * 处理长按操作
 */
const handleLongPressAction = async (action, conversation) => {
  try {
    switch (action) {
      case 'pin':
        conversation.isPinned = true
        uni.showToast({ title: '已置顶', icon: 'success' })
        break

      case 'unpin':
        conversation.isPinned = false
        uni.showToast({ title: '已取消置顶', icon: 'success' })
        break

      case 'markRead':
        conversation.unread = 0
        uni.showToast({ title: '已标为已读', icon: 'success' })
        break

      case 'delete':
        uni.showModal({
          title: '确认删除',
          content: `确定删除与"${conversation.name}"的会话吗？`,
          success: (res) => {
            if (res.confirm) {
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

    // 重新排序列表
    conversationList.value.sort((a, b) => {
      if (a.isPinned && !b.isPinned) return -1
      if (!a.isPinned && b.isPinned) return 1
      return new Date(b.lastTime) - new Date(a.lastTime)
    })
  } catch (error) {
    console.error('操作失败:', error)
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
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
 * 创建单聊
 */
const createSingleChat = () => {
  closeNewChatMenu()
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

/**
 * 创建群聊
 */
const createGroupChat = () => {
  closeNewChatMenu()
  uni.showToast({
    title: '功能开发中',
    icon: 'none'
  })
}

/**
 * 刷新列表
 */
const refresh = () => {
  loadConversations(true)
}

/**
 * 搜索会话
 */
const search = (keyword) => {
  if (keyword && keyword.trim()) {
    const allConversations = JSON.parse(JSON.stringify(conversationList.value))
    conversationList.value = searchConversations(allConversations, keyword)
  } else {
    loadConversations(true)
  }
}

/**
 * 筛选会话
 */
const filter = (filterType) => {
  if (filterType && filterType !== 'all') {
    const allConversations = JSON.parse(JSON.stringify(conversationList.value))
    conversationList.value = filterConversations(allConversations, filterType)
  } else {
    loadConversations(true)
  }
}

/**
 * 清除搜索
 */
const clearSearch = () => {
  loadConversations(true)
}

/**
 * 获取未读数
 */
const getUnreadCount = () => {
  return unreadCount.value
}

// 暴露方法给父组件
defineExpose({
  refresh,
  search,
  filter,
  clearSearch,
  getUnreadCount
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.conversation-list-wrapper {
  height: 100%;
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
  border-bottom: 1rpx solid $border-color-lighter;
}

.search-input {
  flex: 1;
  height: 60rpx;
  padding: 0 20rpx;
  background: $bg-color-base;
  border-radius: 30rpx;
  font-size: $font-size-base;
}

/* 会话列表 */
.conversation-scroll {
  flex: 1;
  height: 100%;
}

.conversation-scroll {
  flex: 1;
  height: 100%;
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 空状态 */
.empty-state {
  @include flex-center-column;
  padding: 200rpx $spacing-lg;

  .empty-icon {
    font-size: 140rpx;
    margin-bottom: $spacing-lg;
    animation: float 3s ease-in-out infinite;
  }

  .empty-text {
    font-size: $font-size-xl;
    color: $text-color-primary;
    margin-bottom: $spacing-sm;
    font-weight: $font-weight-medium;
  }

  .empty-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    line-height: 1.6;
    text-align: center;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20rpx);
  }
}

/* 悬浮按钮 */
.fab-button {
  position: fixed;
  right: 30rpx;
  bottom: 120rpx;
  width: 110rpx;
  height: 110rpx;
  background: linear-gradient(135deg, $primary-color, #ff8f61);
  border-radius: 50%;
  @include flex-center;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.4);
  z-index: 100;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: fabBounce 0.6s ease;

  &:active {
    transform: scale(0.9);
    box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);
  }
}

@keyframes fabBounce {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* 新建聊天菜单 */
.new-chat-menu {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 30rpx;
}

.menu-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
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
  background: $bg-color-base;
  border-radius: $border-radius-lg;

  &:active {
    background: rgba(255, 107, 53, 0.1);
  }
}

.menu-text {
  font-size: $font-size-lg;
  color: $text-color-primary;
}

.menu-cancel {
  text-align: center;
  padding: 30rpx;
  font-size: $font-size-lg;
  color: $text-color-secondary;
  border-top: 1rpx solid $border-color-lighter;
}
</style>
