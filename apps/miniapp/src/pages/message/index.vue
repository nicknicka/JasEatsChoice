<template>
  <view class="message-container">
    <view class="overview-card">
      <view class="overview-main">
        <text class="overview-kicker">消息总览</text>
        <view class="overview-title-row">
          <text class="overview-number">{{ unreadCount }}</text>
          <text class="overview-unit">条未读</text>
        </view>
        <text class="overview-desc">共 {{ totalCount }} 条消息，当前查看{{ activeFilterLabel }}</text>
      </view>

      <view class="overview-actions">
        <button
          class="overview-action primary"
          :class="{ disabled: !hasUnread }"
          :disabled="!hasUnread"
          @click="markAllRead"
        >
          全部已读
        </button>
        <button class="overview-action ghost" @click="deleteRead">删除已读</button>
      </view>
    </view>

    <view class="filter-bar">
      <scroll-view class="filter-scroll" scroll-x show-scrollbar="false">
        <view
          v-for="filter in filters"
          :key="filter.value"
          class="filter-item"
          :class="{ active: selectedFilter === filter.value }"
          @click="changeFilter(filter.value)"
        >
          <text class="filter-text">{{ filter.label }}</text>
          <text class="filter-count">{{ filter.count > 99 ? '99+' : filter.count }}</text>
        </view>
      </scroll-view>
    </view>

    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <view v-if="messages.length === 0 && !loading" class="empty-state">
        <view class="empty-illustration">
          <view class="empty-bubble large"></view>
          <view class="empty-bubble small"></view>
          <text class="empty-icon">💬</text>
        </view>
        <text class="empty-text">还没有消息</text>
        <text class="empty-tips">订单、聊天和系统提醒会在这里汇总</text>
      </view>

      <view v-else class="message-list">
        <view
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ unread: msg.unread }"
          @click="viewMessage(msg)"
        >
          <view class="message-left">
            <view class="avatar-wrapper" :class="msg.type">
              <image
                v-if="msg.avatar"
                class="avatar-image"
                :src="msg.avatar"
                mode="aspectFill"
              />
              <text v-else class="avatar-icon">{{ getIconGlyph(msg.type) }}</text>
            </view>
          </view>

          <view class="message-content">
            <view class="message-header">
              <view class="message-title-wrap">
                <view v-if="msg.unread" class="unread-dot"></view>
                <text class="message-title">{{ msg.title }}</text>
              </view>
              <text class="message-time">{{ msg.time }}</text>
            </view>

            <view class="message-body">
              <text v-if="msg.type !== 'order'" class="message-preview">
                {{ msg.content }}
              </text>
              <view v-else class="order-info">
                <text class="order-status">{{ msg.orderStatus }}</text>
                <text class="order-text">{{ msg.content }}</text>
              </view>
            </view>

            <view class="message-footer">
              <view class="type-chip" :class="msg.type">{{ getTypeLabel(msg.type) }}</view>
              <view v-if="msg.tag" class="tag-item">
                {{ msg.tag }}
              </view>
              <text v-if="msg.type === 'chat' && msg.lastMessage" class="chat-preview">
                {{ msg.lastMessage }}
              </text>
            </view>
          </view>

          <view class="message-right" @click.stop>
            <view class="delete-btn" @click="deleteMessage(msg)">×</view>
          </view>
        </view>
      </view>

      <view v-if="messages.length > 0" class="load-more">
        <view v-if="loading" class="load-text">加载中...</view>
        <view v-else-if="!hasMore" class="load-text">没有更多了</view>
        <view v-else class="load-text">上拉加载更多</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { notificationApi } from '@/api'
import { conversationApi } from '@/api/modules/conversation'
import { navigate, paths, toOrderDetail } from '@/utils/router'

// 用户store
const userStore = useUserStore()

// 消息类型筛选
const filters = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '系统', value: 'system', count: 0 },
  { label: '订单', value: 'order', count: 0 },
  { label: '聊天', value: 'chat', count: 0 },
  { label: '活动', value: 'activity', count: 0 }
])

// 当前筛选
const selectedFilter = ref('all')

// 全量消息列表
const allMessages = ref([])

// 消息列表
const messages = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)

// 分页参数
const page = ref(1)
const pageSize = ref(20)

// 是否有未读消息
const hasUnread = computed(() => {
  return allMessages.value.some(msg => msg.unread)
})

const unreadCount = computed(() => {
  return allMessages.value.filter(msg => msg.unread).length
})

const totalCount = computed(() => allMessages.value.length)

const activeFilterLabel = computed(() => {
  const current = filters.value.find(item => item.value === selectedFilter.value)
  return current?.label || '全部'
})

const resolveUserId = () => {
  const cachedUserInfo = uni.getStorageSync('userInfo') || {}
  return userStore.userInfo?.userId || userStore.userId || cachedUserInfo.userId || uni.getStorageSync('userId') || ''
}

const normalizeMessageType = (type) => {
  if (type === 'promotion') {
    return 'activity'
  }

  return type || 'system'
}

const isDisplayableAvatar = (avatar) => {
  return typeof avatar === 'string' && /^(https?:\/\/|\/|data:)/.test(avatar)
}

const resolveUnread = (item = {}) => {
  if (typeof item.readStatus === 'boolean') {
    return !item.readStatus
  }

  if (typeof item.isRead === 'boolean') {
    return !item.isRead
  }

  return item.status === 'unread'
}

const resolveSortTime = (value, fallback = 0) => {
  if (!value) return fallback

  const timestamp = new Date(value).getTime()
  return Number.isNaN(timestamp) ? fallback : timestamp
}

const resolveConversationSortTime = (conversation, index) => {
  const directTimestamp = resolveSortTime(
    conversation.lastMessageTime || conversation.updateTime || conversation.createTime,
    0
  )
  if (directTimestamp > 0) {
    return directTimestamp
  }

  if (typeof conversation.time === 'string' && /^\d{2}:\d{2}$/.test(conversation.time)) {
    const [hours, minutes] = conversation.time.split(':').map(Number)
    const now = new Date()
    const target = new Date(now)
    target.setHours(hours, minutes, 0, 0)
    if (target.getTime() > now.getTime() + 5 * 60 * 1000) {
      target.setDate(target.getDate() - 1)
    }
    return target.getTime()
  }

  return Date.now() - index
}

const extractNotificationList = (data) => {
  if (Array.isArray(data)) {
    return data
  }

  if (Array.isArray(data?.records)) {
    return data.records
  }

  if (Array.isArray(data?.list)) {
    return data.list
  }

  return []
}

const mapNotificationMessage = (notif, index) => {
  const rawId = notif.notificationId || notif.id
  const sortTime = resolveSortTime(
    notif.sendTime || notif.createTime || notif.createdAt,
    Date.now() - 100000 - index
  )
  const type = normalizeMessageType(notif.type)

  return {
    id: `notification-${rawId}`,
    rawId,
    source: 'notification',
    type,
    unread: resolveUnread(notif),
    title: notif.title || '通知消息',
    content: notif.content || '',
    avatar: isDisplayableAvatar(notif.avatar) ? notif.avatar : '',
    time: formatTime(sortTime),
    tag: notif.tag || '',
    orderStatus: notif.orderStatus || '',
    orderId: notif.targetId || notif.orderId || '',
    merchantId: notif.merchantId || notif.senderId || '',
    lastMessage: notif.lastMessage || '',
    targetType: notif.targetType || '',
    sortTime
  }
}

const mapConversationMessage = (conversation, index) => {
  const conversationId = conversation.conversationId || conversation.sessionId || conversation.id
  const lastMessage = typeof conversation.lastMessage === 'string'
    ? conversation.lastMessage
    : (conversation.lastMessage?.content || '')

  return {
    id: `conversation-${conversationId}`,
    rawId: conversationId,
    source: 'conversation',
    type: 'chat',
    unread: Number(conversation.unreadCount || 0) > 0,
    title: conversation.name || conversation.sessionName || '聊天消息',
    content: lastMessage || '暂无消息',
    avatar: isDisplayableAvatar(conversation.avatar) ? conversation.avatar : '',
    time: conversation.time || formatTime(resolveConversationSortTime(conversation, index)),
    tag: '',
    orderStatus: '',
    orderId: '',
    merchantId: conversation.targetId || conversation.targetUserId || '',
    targetId: conversation.targetId || conversation.targetUserId || '',
    lastMessage,
    targetType: 'chat',
    conversationId,
    sortTime: resolveConversationSortTime(conversation, index)
  }
}

const applyCurrentFilter = () => {
  const filteredMessages = selectedFilter.value === 'all'
    ? allMessages.value
    : allMessages.value.filter(item => item.type === selectedFilter.value)

  const endIndex = page.value * pageSize.value
  messages.value = filteredMessages.slice(0, endIndex)
  hasMore.value = filteredMessages.length > endIndex
}

const getIconGlyph = (type) => {
  const icons = {
    system: '📢',
    order: '📦',
    chat: '💬',
    activity: '🎉'
  }
  return icons[type] || '📄'
}

const getTypeLabel = (type) => {
  const labels = {
    system: '系统',
    order: '订单',
    chat: '聊天',
    activity: '活动'
  }
  return labels[type] || '消息'
}

/**
 * 切换筛选
 */
const changeFilter = (value) => {
  selectedFilter.value = value
  page.value = 1
  applyCurrentFilter()
}

/**
 * 加载消息列表
 */
const loadMessages = async (showLoading = true) => {
  if (showLoading) {
    loading.value = true
  }

  try {
    const userId = resolveUserId()
    if (!userId) {
      allMessages.value = []
      page.value = 1
      applyCurrentFilter()
      updateCounts()
      return
    }

    const [notificationResult, conversationResult] = await Promise.allSettled([
      notificationApi.getList({ userId, page: 1, size: 200 }),
      conversationApi.getList(userId)
    ])

    const notifications = notificationResult.status === 'fulfilled'
      ? extractNotificationList(notificationResult.value?.data)
      : []
    const conversations = conversationResult.status === 'fulfilled'
      ? (conversationResult.value?.data || [])
      : []

    if (notificationResult.status === 'rejected' && conversationResult.status === 'rejected') {
      throw notificationResult.reason || conversationResult.reason
    }

    const mergedMessages = [
      ...conversations.map((conversation, index) => mapConversationMessage(conversation, index)),
      ...notifications.map((notif, index) => mapNotificationMessage(notif, index))
    ].sort((a, b) => b.sortTime - a.sortTime)

    allMessages.value = mergedMessages
    applyCurrentFilter()
    updateCounts()
  } catch (error) {
    console.error('加载消息列表失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''

  const now = new Date()
  const target = new Date(time)
  const diff = now - target

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚'
  }
  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  }
  // 小于24小时
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  }
  // 小于7天
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`
  }

  // 超过7天显示日期
  const month = target.getMonth() + 1
  const date = target.getDate()
  return `${month}月${date}日`
}

const updateCounts = () => {
  const unreadMessages = allMessages.value.filter(item => item.unread)
  const typeCounts = {
    system: 0,
    order: 0,
    chat: 0,
    activity: 0
  }

  unreadMessages.forEach((msg) => {
    if (typeCounts[msg.type] !== undefined) {
      typeCounts[msg.type]++
    }
  })

  filters.value[0].count = unreadMessages.length
  filters.value[1].count = typeCounts.system
  filters.value[2].count = typeCounts.order
  filters.value[3].count = typeCounts.chat
  filters.value[4].count = typeCounts.activity
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  await loadMessages(false)
  refreshing.value = false
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  applyCurrentFilter()
}

/**
 * 查看消息详情
 */
const viewMessage = async (msg) => {
  const openMessageTarget = () => {
    if (msg.type === 'chat') {
      navigate(paths.COMMON.CHAT_ROOM, {
        merchantId: msg.targetId || msg.merchantId || '',
        userName: msg.title || '',
        userAvatar: msg.avatar || ''
      })
      return
    }

    if (msg.type === 'order') {
      toOrderDetail(msg.orderId)
      return
    }

    uni.showModal({
      title: msg.title || '消息详情',
      content: msg.content || '暂无详情内容',
      showCancel: false
    })
  }

  try {
    // 标记为已读
    if (msg.unread) {
      if (msg.source === 'conversation') {
        await conversationApi.markRead(msg.conversationId)
      } else {
        await notificationApi.markAsRead(msg.rawId)
      }
      msg.unread = false
      updateCounts()
    }

    // 跳转到详情页
    openMessageTarget()
  } catch (error) {
    console.error('查看消息失败:', error)
    openMessageTarget()
  }
}

/**
 * 全部已读
 */
const markAllRead = async () => {
  try {
    const userId = resolveUserId()
    const unreadNotificationIds = allMessages.value
      .filter(msg => msg.source === 'notification' && msg.unread)
      .map(msg => msg.rawId)
    const unreadConversationIds = allMessages.value
      .filter(msg => msg.source === 'conversation' && msg.unread)
      .map(msg => msg.conversationId)

    await Promise.all([
      unreadNotificationIds.length > 0
        ? notificationApi.markAllAsRead({ userId })
        : Promise.resolve(),
      ...unreadConversationIds.map(id => conversationApi.markRead(id))
    ])

    // 更新本地状态
    messages.value.forEach(msg => {
      msg.unread = false
    })
    allMessages.value.forEach(msg => {
      msg.unread = false
    })
    updateCounts()

    uni.showToast({
      title: '已全部标记为已读',
      icon: 'success'
    })
  } catch (error) {
    console.error('标记已读失败:', error)
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 删除消息
 */
const deleteMessage = async (msg) => {
  try {
    if (msg.source === 'conversation') {
      await conversationApi.delete(msg.conversationId)
    } else {
      await notificationApi.delete(msg.rawId)
    }

    // 从列表中移除
    allMessages.value = allMessages.value.filter(item => item.id !== msg.id)
    applyCurrentFilter()
    updateCounts()

    uni.showToast({
      title: '删除成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('删除消息失败:', error)
    uni.showToast({
      title: '删除失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 删除已读
 */
const deleteRead = async () => {
  const readMessages = allMessages.value.filter(msg => !msg.unread)

  if (readMessages.length === 0) {
    uni.showToast({
      title: '没有已读消息',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '删除已读消息',
    content: `确定要删除${readMessages.length}条已读消息吗？`,
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          const readNotificationIds = readMessages
            .filter(msg => msg.source === 'notification')
            .map(msg => msg.rawId)
          const readConversationIds = readMessages
            .filter(msg => msg.source === 'conversation')
            .map(msg => msg.conversationId)

          await Promise.all([
            readNotificationIds.length > 0
              ? notificationApi.batchDelete({
                  userId: resolveUserId(),
                  ids: readNotificationIds
                })
              : Promise.resolve(),
            ...readConversationIds.map(id => conversationApi.delete(id))
          ])

          // 更新本地状态
          allMessages.value = allMessages.value.filter(msg => msg.unread)
          page.value = 1
          applyCurrentFilter()
          updateCounts()

          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
        } catch (error) {
          console.error('删除已读失败:', error)
          uni.showToast({
            title: '删除失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

// 组件挂载
onMounted(() => {
  loadMessages()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.message-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #fff7f2 0%, #f6f7fb 34%, #f6f7fb 100%);
  display: flex;
  flex-direction: column;
  padding: 24rpx 24rpx 0;
  box-sizing: border-box;
}

.overview-card {
  display: flex;
  justify-content: space-between;
  gap: 24rpx;
  padding: 30rpx;
  background: linear-gradient(135deg, #ff6b35 0%, #ff8b5d 100%);
  border-radius: 28rpx;
  box-shadow: 0 18rpx 38rpx rgba(255, 107, 53, 0.2);
  color: #fff;
}

.overview-main {
  flex: 1;
  min-width: 0;
}

.overview-kicker {
  display: block;
  font-size: 24rpx;
  opacity: 0.86;
}

.overview-title-row {
  display: flex;
  align-items: baseline;
  margin-top: 10rpx;
}

.overview-number {
  font-size: 64rpx;
  line-height: 1;
  font-weight: 700;
}

.overview-unit {
  margin-left: 10rpx;
  font-size: 26rpx;
  font-weight: 500;
}

.overview-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.5;
  opacity: 0.86;
  @include text-ellipsis;
}

.overview-actions {
  width: 148rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  flex-shrink: 0;
}

.overview-action {
  height: 58rpx;
  line-height: 58rpx;
  padding: 0;
  margin: 0;
  border-radius: 999rpx;
  font-size: 23rpx;
  font-weight: 600;
  border: 0;

  &::after {
    border: 0;
  }

  &.primary {
    color: $primary-color;
    background: #fff;
  }

  &.ghost {
    color: #fff;
    background: rgba(255, 255, 255, 0.18);
    border: 1rpx solid rgba(255, 255, 255, 0.4);
  }

  &.disabled {
    color: rgba(255, 107, 53, 0.45);
  }
}

.filter-bar {
  margin: 22rpx -24rpx 0;
  padding: 0 0 20rpx;
}

.filter-scroll {
  white-space: nowrap;
  padding: 0 24rpx;
  box-sizing: border-box;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  height: 62rpx;
  padding: 0 24rpx;
  margin-right: 14rpx;
  border-radius: 999rpx;
  background: #fff;
  border: 1rpx solid #eeeeee;
  box-shadow: 0 8rpx 18rpx rgba(31, 35, 41, 0.04);
  transition: all 0.2s ease;

  &.active {
    background: #2f2f32;
    border-color: #2f2f32;
    box-shadow: 0 12rpx 24rpx rgba(47, 47, 50, 0.16);

    .filter-text,
    .filter-count {
      color: #fff;
    }
  }

  &:active {
    transform: scale(0.96);
  }
}

.filter-text {
  font-size: 26rpx;
  color: $text-color-primary;
  font-weight: 600;
}

.filter-count {
  margin-left: 10rpx;
  font-size: 22rpx;
  color: $text-color-secondary;
}

.scroll-container {
  flex: 1;
  height: 0;
  min-height: 0;
  margin: 0 -24rpx;
}

.empty-state {
  @include flex-center-column;
  min-height: 650rpx;
  padding: 72rpx 48rpx 140rpx;
  text-align: center;
  box-sizing: border-box;
}

.empty-illustration {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  margin-bottom: 28rpx;
  border-radius: 48rpx;
  background: #fff;
  @include flex-center;
  box-shadow: 0 18rpx 42rpx rgba(37, 42, 49, 0.08);
}

.empty-icon {
  position: relative;
  z-index: 2;
  font-size: 72rpx;
}

.empty-bubble {
  position: absolute;
  border-radius: 999rpx;
  background: rgba(255, 107, 53, 0.12);

  &.large {
    width: 58rpx;
    height: 58rpx;
    top: 20rpx;
    right: 22rpx;
  }

  &.small {
    width: 28rpx;
    height: 28rpx;
    left: 28rpx;
    bottom: 26rpx;
  }
}

.empty-text {
  font-size: 34rpx;
  line-height: 1.4;
  color: $text-color-primary;
  font-weight: 700;
  margin-bottom: 10rpx;
}

.empty-tips {
  font-size: 25rpx;
  line-height: 1.6;
  color: $text-color-secondary;
}

.message-list {
  padding: 4rpx 24rpx 28rpx;
}

.message-item {
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  margin-bottom: 18rpx;
  background: #fff;
  border-radius: 22rpx;
  border: 1rpx solid rgba(232, 232, 232, 0.9);
  box-shadow: 0 10rpx 26rpx rgba(31, 35, 41, 0.05);
  transition: all 0.2s ease;

  &.unread {
    border-color: rgba(255, 107, 53, 0.25);
    box-shadow: 0 12rpx 30rpx rgba(255, 107, 53, 0.09);
  }

  &:active {
    transform: scale(0.98);
  }
}

.message-left {
  flex-shrink: 0;
  margin-right: 20rpx;
}

.avatar-wrapper {
  position: relative;
  width: 82rpx;
  height: 82rpx;
  @include flex-center;
  border-radius: 24rpx;
  background-color: $bg-color-base;

  &.system {
    background: linear-gradient(135deg, #FFB74D, #FF9800);
  }

  &.order {
    background: linear-gradient(135deg, #64B5F6, #2196F3);
  }

  &.chat {
    background: linear-gradient(135deg, #81C784, #4CAF50);
  }

  &.activity {
    background: linear-gradient(135deg, #FF6B35, #FF8F61);
  }
}

.avatar-image {
  width: 100%;
  height: 100%;
  border-radius: 24rpx;
}

.avatar-icon {
  font-size: 34rpx;
  line-height: 1;
}

.message-content {
  flex: 1;
  min-width: 0;
  margin-right: 12rpx;
}

.message-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 8rpx;
  gap: 12rpx;
}

.message-title-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
}

.unread-dot {
  width: 12rpx;
  height: 12rpx;
  margin-right: 10rpx;
  background-color: $danger-color;
  border-radius: 50%;
  flex-shrink: 0;
}

.message-title {
  flex: 1;
  min-width: 0;
  font-size: 29rpx;
  line-height: 1.35;
  font-weight: 700;
  color: $text-color-primary;
  @include text-ellipsis;
}

.message-time {
  flex-shrink: 0;
  padding-top: 2rpx;
  font-size: 22rpx;
  color: $text-color-secondary;
}

.message-body {
  margin-bottom: 12rpx;
}

.message-preview {
  font-size: 25rpx;
  color: $text-color-regular;
  line-height: 1.55;
  @include text-ellipsis-multiline(2);
}

.order-info {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 10rpx;
}

.order-status {
  flex-shrink: 0;
  padding: 5rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: 21rpx;
  border-radius: 8rpx;
  font-weight: $font-weight-medium;
}

.order-text {
  flex: 1;
  min-width: 0;
  font-size: 25rpx;
  color: $text-color-regular;
  @include text-ellipsis;
}

.message-footer {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 10rpx;
}

.type-chip {
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  font-size: 21rpx;
  font-weight: 600;
  color: $text-color-secondary;
  background: #f2f3f5;

  &.system {
    color: #d46b08;
    background: #fff4e6;
  }

  &.order {
    color: #1677c8;
    background: #eaf5ff;
  }

  &.chat {
    color: #288a42;
    background: #ecf8ef;
  }

  &.activity {
    color: #d64c1f;
    background: #fff0e8;
  }
}

.tag-item {
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: 21rpx;
  border-radius: 999rpx;
}

.chat-preview {
  flex: 1;
  min-width: 0;
  font-size: 22rpx;
  color: $text-color-secondary;
  @include text-ellipsis;
}

.message-right {
  flex-shrink: 0;
}

.delete-btn {
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  border-radius: 50%;
  background: #f7f8fa;
  font-size: 34rpx;
  line-height: 1;
  color: #b8b8b8;

  &:active {
    background: #fff1f0;
  }
}

.load-more {
  @include flex-center;
  padding: 28rpx 0 42rpx;
}

.load-text {
  font-size: 24rpx;
  color: $text-color-secondary;
}
</style>
