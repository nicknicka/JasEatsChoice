<template>
  <view class="chat-center-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <uni-search-bar
        placeholder="搜索联系人或消息"
        :focus="false"
        :show-action="false"
        @confirm="onSearch"
      />
    </view>

    <!-- 系统通知入口 -->
    <view class="system-notifications" @tap="openSystemNotifications">
      <view class="notification-icon">
        <uni-icons type="notification" size="24" color="#FF6B35"></uni-icons>
      </view>
      <view class="notification-info">
        <text class="notification-title">系统通知</text>
        <text class="notification-preview">{{ latestSystemNotification }}</text>
      </view>
      <view class="notification-badge" v-if="unreadSystemCount > 0">
        <text class="badge-count">{{ unreadSystemCount > 99 ? '99+' : unreadSystemCount }}</text>
      </view>
      <view class="notification-time">
        {{ systemNotificationTime }}
      </view>
    </view>

    <!-- 消息类型筛选 -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: activeTab === item.value }"
        v-for="item in filterTabs"
        :key="item.value"
        @tap="changeTab(item.value)"
      >
        {{ item.label }}
        <view class="tab-badge" v-if="item.unread > 0">
          {{ item.unread > 99 ? '99+' : item.unread }}
        </view>
      </view>
    </view>

    <!-- 会话列表 -->
    <scroll-view
      class="conversation-list"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        class="conversation-item"
        v-for="item in conversationList"
        :key="item.id"
        @tap="openConversation(item)"
        @longpress="showConversationMenu(item)"
      >
        <view class="conversation-avatar-wrapper">
          <image
            class="conversation-avatar"
            :src="item.avatar"
            mode="aspectFill"
          ></image>
          <view class="online-status" v-if="item.online && item.type === 'user'"></view>
        </view>

        <view class="conversation-info">
          <view class="conversation-header">
            <text class="conversation-name">{{ item.name }}</text>
            <text class="conversation-time">{{ item.time }}</text>
          </view>

          <!-- 订单卡片标识 -->
          <view class="order-card-tag" v-if="item.lastMessageType === 'orderCard'">
            <uni-icons type="shop" size="12" color="#FF6B35"></uni-icons>
            <text>[订单卡片]</text>
          </view>

          <text class="conversation-preview" :class="{ unread: item.unread > 0 }">
            {{ item.lastMessage }}
          </text>
        </view>

        <view class="conversation-badge" v-if="item.unread > 0">
          <text class="badge-count">{{ item.unread > 99 ? '99+' : item.unread }}</text>
        </view>

        <!-- 置顶标识 -->
        <view class="pinned-badge" v-if="item.pinned">
          <uni-icons type="eye" size="12" color="#FF6B35"></uni-icons>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="conversationList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="conversationList.length === 0 && !loading">
        <empty text="暂无消息" icon="💬" buttonText="开始聊天" @button-click="startChat" />
      </view>
    </scroll-view>

    <!-- 快捷操作 -->
    <view class="quick-actions">
      <button class="action-btn" @tap="startChat">
        <uni-icons type="plus" size="20" color="#FF6B35"></uni-icons>
        <text>发起聊天</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { conversationApi } from '@/api/modules/conversation.js'
import { formatRelativeTime } from '@/utils/helper'

// 商家ID
const merchantId = ref('')

// 筛选标签
const filterTabs = ref([
  { label: '全部', value: 'all', unread: 0 },
  { label: '用户', value: 'user', unread: 5 },
  { label: '商家', value: 'merchant', unread: 0 },
  { label: '群组', value: 'group', unread: 2 }
])

const activeTab = ref('all')

// 系统通知
const latestSystemNotification = ref('您有新的订单待处理')
const systemNotificationTime = ref('5分钟前')
const unreadSystemCount = ref(3)

// 会话列表
const conversationList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

let pollingTimer = null

onMounted(() => {
  // 获取商家ID
  merchantId.value = uni.getStorageSync('merchantId') || ''

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
 * 加载会话列表 - IM-032: 调用API获取会话列表
 */
const loadConversations = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // IM-032: 调用API获取会话列表（商家端）
    const res = await conversationApi.getList(merchantId.value)

    if (res.code === 200 && res.data) {
      // 转换会话数据格式
      const formattedConversations = res.data.map(conv => ({
        id: conv.id,
        type: conv.type || 'user', // user, merchant, group
        name: conv.name || conv.displayName || '未命名',
        avatar: conv.avatar || '/static/default-avatar.png',
        online: conv.isOnline || false,
        pinned: conv.isPinned || false,
        lastMessage: conv.lastMessage?.content || '',
        lastMessageType: conv.lastMessage?.type || 'text',
        time: formatTime(conv.lastMessageTime || conv.updateTime || new Date()),
        unread: conv.unreadCount || 0
      }))

      // 根据activeTab筛选
      let filteredConversations = formattedConversations
      if (activeTab.value !== 'all') {
        filteredConversations = formattedConversations.filter(conv => conv.type === activeTab.value)
      }

      // 排序：置顶的在前，然后按时间排序
      filteredConversations.sort((a, b) => {
        if (a.pinned && !b.pinned) return -1
        if (!a.pinned && b.pinned) return 1
        return new Date(b.time) - new Date(a.time)
      })

      if (isRefresh) {
        conversationList.value = filteredConversations
      } else {
        conversationList.value = [...conversationList.value, ...filteredConversations]
      }

      // 更新未读数
      updateUnreadCounts()

      if (filteredConversations.length < pageSize) {
        noMore.value = true
      }

      console.log('加载会话成功，数量:', filteredConversations.length)
    } else {
      throw new Error(res.message || '获取会话列表失败')
    }
  } catch (error) {
    console.error('加载会话失败:', error)

    // 开发阶段：使用模拟数据
    const mockData = generateMockConversations()

    // 根据activeTab筛选
    let filteredData = mockData
    if (activeTab.value !== 'all') {
      filteredData = mockData.filter(conv => conv.type === activeTab.value)
    }

    if (isRefresh) {
      conversationList.value = filteredData
    } else {
      conversationList.value = [...conversationList.value, ...filteredData]
    }

    updateUnreadCounts()

    if (filteredData.length < pageSize) {
      noMore.value = true
    }
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟会话数据
 */
const generateMockConversations = () => {
  const conversations = []

  // 用户会话
  const users = [
    {
      id: 1,
      type: 'user',
      name: '张同学',
      avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=张',
      online: true,
      pinned: true,
      lastMessage: '好的，我马上就过去取餐',
      lastMessageType: 'text',
      time: '刚刚',
      unread: 2
    },
    {
      id: 2,
      type: 'user',
      name: '李同学',
      avatar: 'https://via.placeholder.com/80/52C41A/FFFFFF?text=李',
      online: false,
      pinned: false,
      lastMessage: '请问宫保鸡丁还有吗？',
      lastMessageType: 'text',
      time: '10分钟前',
      unread: 1
    },
    {
      id: 3,
      type: 'user',
      name: '王同学',
      avatar: 'https://via.placeholder.com/80/1890FF/FFFFFF?text=王',
      online: true,
      pinned: false,
      lastMessage: '[订单卡片] 订单号：OD202603180003',
      lastMessageType: 'orderCard',
      time: '30分钟前',
      unread: 0
    }
  ]

  // 群组会话
  const groups = [
    {
      id: 4,
      type: 'group',
      name: '商家交流群',
      avatar: 'https://via.placeholder.com/80/722ED1/FFFFFF?text=群',
      online: false,
      pinned: false,
      lastMessage: '王商家: 大家今天的生意怎么样？',
      lastMessageType: 'text',
      time: '1小时前',
      unread: 5
    }
  ]

  conversations.push(...users, ...groups)
  return conversations
}

/**
 * 更新未读数
 */
const updateUnreadCounts = () => {
  let userUnread = 0
  let groupUnread = 0

  conversationList.value.forEach(item => {
    if (item.type === 'user') {
      userUnread += item.unread
    } else if (item.type === 'group') {
      groupUnread += item.unread
    }
  })

  filterTabs.value[1].unread = userUnread
  filterTabs.value[3].unread = groupUnread
  filterTabs.value[0].unread = userUnread + groupUnread
}

/**
 * 切换标签
 */
const changeTab = (tab) => {
  activeTab.value = tab
  loadConversations(true)
}

/**
 * 搜索会话 - IM-033: 调用API搜索会话
 */
const onSearch = async (e) => {
  const keyword = e.value

  if (!keyword || keyword.trim() === '') {
    // 如果搜索关键词为空，重新加载全部会话
    loadConversations(true)
    return
  }

  try {
    loading.value = true

    // IM-033: 调用API搜索会话
    const res = await conversationApi.search(merchantId.value, keyword)

    if (res.code === 200 && res.data) {
      // 转换搜索结果
      const searchResults = res.data.map(conv => ({
        id: conv.id,
        type: conv.type || 'user',
        name: conv.name || conv.displayName || '未命名',
        avatar: conv.avatar || '/static/default-avatar.png',
        online: conv.isOnline || false,
        pinned: conv.isPinned || false,
        lastMessage: conv.lastMessage?.content || '',
        lastMessageType: conv.lastMessage?.type || 'text',
        time: formatTime(conv.lastMessageTime || conv.updateTime || new Date()),
        unread: conv.unreadCount || 0
      }))

      conversationList.value = searchResults

      console.log('搜索会话成功，结果数量:', searchResults.length)
    } else {
      throw new Error(res.message || '搜索失败')
    }
  } catch (error) {
    console.error('搜索会话失败:', error)
    uni.showToast({
      title: error.message || '搜索失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 打开系统通知
 */
const openSystemNotifications = () => {
  uni.navigateTo({
    url: '/pages-merchant/notification/index'
  })
}

/**
 * 打开会话
 */
const openConversation = (conversation) => {
  // 清除未读
  conversation.unread = 0
  updateUnreadCounts()

  uni.navigateTo({
    url: `/pages-merchant/chat/detail?id=${conversation.id}&type=${conversation.type}&name=${conversation.name}`
  })
}

/**
 * 显示会话菜单
 */
const showConversationMenu = (conversation) => {
  const itemList = []

  if (conversation.pinned) {
    itemList.push('取消置顶')
  } else {
    itemList.push('置顶聊天')
  }

  itemList.push('标记已读')
  itemList.push('删除聊天')

  uni.showActionSheet({
    itemList: itemList,
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          togglePinned(conversation)
          break
        case 1:
          markAsRead(conversation)
          break
        case 2:
          deleteConversation(conversation)
          break
      }
    }
  })
}

/**
 * 置顶/取消置顶
 */
const togglePinned = (conversation) => {
  conversation.pinned = !conversation.pinned
  uni.showToast({
    title: conversation.pinned ? '已置顶' : '已取消置顶',
    icon: 'success'
  })
  // TODO: 调用API保存置顶状态
}

/**
 * 标记已读
 */
const markAsRead = (conversation) => {
  conversation.unread = 0
  updateUnreadCounts()
  uni.showToast({
    title: '已标记为已读',
    icon: 'success'
  })
  // TODO: 调用API标记已读
}

/**
 * 删除会话
 */
const deleteConversation = (conversation) => {
  uni.showModal({
    title: '删除聊天',
    content: '确定删除该聊天吗？删除后将无法恢复。',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        const index = conversationList.value.findIndex(c => c.id === conversation.id)
        if (index !== -1) {
          conversationList.value.splice(index, 1)
          updateUnreadCounts()
          uni.showToast({
            title: '已删除',
            icon: 'success'
          })
        }
        // TODO: 调用API删除会话
      }
    }
  })
}

/**
 * 发起聊天
 */
const startChat = () => {
  uni.navigateTo({
    url: '/pages-merchant/chat/start'
  })
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadConversations(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadConversations()
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.chat-center-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 搜索栏 */
.search-bar {
  background: #fff;
  padding: 20rpx;
}

/* 系统通知 */
.system-notifications {
  background: #fff;
  padding: 25rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.notification-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  @include flex-center;
  flex-shrink: 0;
}

.notification-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
  min-width: 0;
}

.notification-title {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.notification-preview {
  font-size: 26rpx;
  color: #999;
  @include text-ellipsis;
}

.notification-badge {
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 8rpx;
  background: #F5222D;
  border-radius: 18rpx;
  @include flex-center;
  flex-shrink: 0;
}

.badge-count {
  font-size: 20rpx;
  color: #fff;
}

.notification-time {
  font-size: 24rpx;
  color: #999;
  flex-shrink: 0;
}

/* 筛选标签 */
.filter-tabs {
  background: #fff;
  display: flex;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  position: relative;
  padding: 10rpx 24rpx;
  margin-right: 30rpx;
  font-size: 28rpx;
  color: #666;

  &.active {
    color: #FF6B35;
    font-weight: bold;
  }

  &:last-child {
    margin-right: 0;
  }
}

.tab-badge {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 28rpx;
  height: 28rpx;
  padding: 0 6rpx;
  background: #F5222D;
  color: #fff;
  font-size: 18rpx;
  border-radius: 14rpx;
  @include flex-center;
}

/* 会话列表 */
.conversation-list {
  flex: 1;
  padding: 20rpx;
}

.conversation-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx;
  background: #fff;
  border-radius: 12rpx;
  margin-bottom: 15rpx;
}

.conversation-avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.conversation-avatar {
  width: 90rpx;
  height: 90rpx;
  border-radius: 50%;
}

.online-status {
  position: absolute;
  bottom: 5rpx;
  right: 5rpx;
  width: 20rpx;
  height: 20rpx;
  background: #52C41A;
  border: 3rpx solid #fff;
  border-radius: 50%;
}

.conversation-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
  flex: 1;
  @include text-ellipsis;
}

.conversation-time {
  font-size: 24rpx;
  color: #999;
  flex-shrink: 0;
}

.order-card-tag {
  display: flex;
  align-items: center;
  gap: 5rpx;
  padding: 4rpx 10rpx;
  background: #FFF7E6;
  border-radius: 4rpx;
  align-self: flex-start;
  font-size: 22rpx;
  color: #FF6B35;
}

.conversation-preview {
  font-size: 26rpx;
  color: #999;
  @include text-ellipsis;

  &.unread {
    color: #333;
    font-weight: 500;
  }
}

.conversation-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 8rpx;
  background: #F5222D;
  border-radius: 18rpx;
  @include flex-center;
  flex-shrink: 0;
}

.pinned-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  width: 32rpx;
  height: 32rpx;
  background: #FFF7E6;
  border-radius: 50%;
  @include flex-center;
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 空状态 */
.empty-state {
  padding-top: 200rpx;
}

/* 快捷操作 */
.quick-actions {
  background: #fff;
  padding: 20rpx;
  border-top: 1rpx solid #eee;
}

.action-btn {
  width: 100%;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 40rpx;
  border: none;
  @include flex-center;
  gap: 10rpx;
}
</style>
