<template>
  <view class="message-container">
    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <text class="mark-read-btn" @click="markAllRead" v-if="hasUnread">全部已读</text>
      <text class="delete-read-btn" @click="deleteRead">删除已读</text>
    </view>

    <!-- 消息分类Tab -->
    <view class="filter-bar">
      <scroll-view class="filter-scroll" scroll-x>
        <view
          class="filter-item"
          :class="{ active: selectedFilter === filter.value }"
          v-for="filter in filters"
          :key="filter.value"
          @click="changeFilter(filter.value)"
        >
          <text class="filter-text">{{ filter.label }}</text>
          <view class="filter-badge" v-if="filter.count > 0">
            {{ filter.count > 99 ? '99+' : filter.count }}
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 消息列表 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 空状态 -->
      <view class="empty-state" v-if="messages.length === 0 && !loading">
        <text class="empty-icon">💬</text>
        <text class="empty-text">还没有消息</text>
        <text class="empty-tips">有新消息时会在这里显示哦</text>
      </view>

      <!-- 消息列表 -->
      <view class="message-list" v-else>
        <view
          class="message-item"
          :class="{ unread: msg.unread }"
          v-for="msg in messages"
          :key="msg.id"
          @click="viewMessage(msg)"
        >
          <!-- 左侧图标/头像 -->
          <view class="message-left">
            <view class="avatar-wrapper" :class="msg.type">
              <image
                class="avatar-image"
                v-if="msg.avatar"
                :src="msg.avatar"
                mode="aspectFill"
              />
              <text class="avatar-icon" v-else>{{ getIcon(msg.type) }}</text>
              <view class="unread-dot" v-if="msg.unread"></view>
            </view>
          </view>

          <!-- 中间内容 -->
          <view class="message-content">
            <view class="message-header">
              <text class="message-title">{{ msg.title }}</text>
              <text class="message-time">{{ msg.time }}</text>
            </view>

            <view class="message-body">
              <text class="message-preview" v-if="msg.type !== 'order'">
                {{ msg.content }}
              </text>
              <view class="order-info" v-else>
                <text class="order-status">{{ msg.orderStatus }}</text>
                <text class="order-text">{{ msg.content }}</text>
              </view>
            </view>

            <!-- 底部标签 -->
            <view class="message-footer" v-if="msg.tag || msg.type === 'chat'">
              <view class="tag-item" v-if="msg.tag">
                {{ msg.tag }}
              </view>
              <text class="chat-preview" v-if="msg.type === 'chat' && msg.lastMessage">
                {{ msg.lastMessage }}
              </text>
            </view>
          </view>

          <!-- 右侧操作 -->
          <view class="message-right" @click.stop>
            <text class="delete-btn" @click="deleteMessage(msg)">×</text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-more" v-if="messages.length > 0">
        <view class="load-text" v-if="loading">加载中...</view>
        <view class="load-text" v-else-if="!hasMore">没有更多了</view>
        <view class="load-text" v-else>上拉加载更多</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { notificationApi, chatApi } from '@/api'

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
  return messages.value.some(msg => msg.unread)
})

/**
 * 获取消息图标
 */
const getIcon = (type) => {
  const icons = {
    system: '📢',
    order: '📦',
    chat: '💬',
    activity: '🎉'
  }
  return icons[type] || '📄'
}

/**
 * 切换筛选
 */
const changeFilter = (value) => {
  selectedFilter.value = value
  page.value = 1
  messages.value = []
  loadMessages()
}

/**
 * 加载消息列表
 */
const loadMessages = async (showLoading = true) => {
  // 登录检查
  if (!userStore.checkLogin()) {
    return
  }

  if (showLoading) {
    loading.value = true
  }

  try {
    const params = {
      userId: userStore.userInfo?.userId,
      page: page.value,
      size: pageSize.value
    }

    // 根据筛选条件添加类型参数
    if (selectedFilter.value !== 'all') {
      params.type = selectedFilter.value
    }

    const res = await notificationApi.getList(params)

    if (res.code === 200 && res.data) {
      const notificationList = res.data.records || res.data.list || []

      // 映射数据格式
      const mappedMessages = notificationList.map(notif => ({
        id: notif.notificationId || notif.id,
        type: notif.type || 'system',
        unread: notif.status === 'unread',
        title: notif.title,
        content: notif.content,
        avatar: notif.avatar || '',
        time: formatTime(notif.createTime || notif.createdAt),
        tag: notif.tag || '',
        // 订单相关字段
        orderStatus: notif.orderStatus || '',
        orderId: notif.targetId || notif.orderId,
        // 聊天相关字段
        merchantId: notif.merchantId || notif.senderId,
        lastMessage: notif.lastMessage || '',
        // 目标类型
        targetType: notif.targetType
      }))

      if (page.value === 1) {
        messages.value = mappedMessages
      } else {
        messages.value.push(...mappedMessages)
      }

      // 判断是否还有更多数据
      hasMore.value = notificationList.length >= pageSize.value

      // 更新筛选数量
      await updateCounts()
    }
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

/**
 * 获取通知图标
 */
const getNotificationIcon = (type) => {
  const icons = {
    system: '📢',
    order: '📦',
    chat: '💬',
    activity: '🎉'
  }
  return icons[type] || '📄'
}

/**
 * 更新各类型消息数量
 */
const updateCounts = async () => {
  try {
    // 获取未读总数
    const unreadRes = await notificationApi.getUnreadCount({
      userId: userStore.userInfo?.userId
    })

    if (unreadRes.code === 200) {
      const totalUnread = unreadRes.data || 0
      filters.value[0].count = totalUnread
    }

    // 统计当前列表中各类型的未读数
    const typeCounts = {
      system: 0,
      order: 0,
      chat: 0,
      activity: 0
    }

    messages.value.forEach(msg => {
      if (msg.unread && typeCounts[msg.type] !== undefined) {
        typeCounts[msg.type]++
      }
    })

    filters.value[1].count = typeCounts.system
    filters.value[2].count = typeCounts.order
    filters.value[3].count = typeCounts.chat
    filters.value[4].count = typeCounts.activity
  } catch (error) {
    console.error('更新消息数量失败:', error)
  }
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
  loadMessages()
}

/**
 * 查看消息详情
 */
const viewMessage = async (msg) => {
  try {
    // 标记为已读
    if (msg.unread) {
      await notificationApi.markAsRead(msg.id, {
        userId: userStore.userInfo?.userId
      })
      msg.unread = false
      await updateCounts()
    }

    // 跳转到详情页
    if (msg.type === 'chat') {
      // 跳转到聊天页面
      uni.navigateTo({
        url: `/pages-common/chat/index?merchantId=${msg.merchantId}`
      })
    } else if (msg.type === 'order') {
      // 跳转到订单详情
      uni.navigateTo({
        url: `/pages-user/order/detail/index?id=${msg.orderId}`
      })
    } else {
      // 跳转到消息详情页
      uni.navigateTo({
        url: `/pages-user/message/detail/index?id=${msg.id}`
      })
    }
  } catch (error) {
    console.error('查看消息失败:', error)
    // 即使API调用失败，也继续跳转
    if (msg.type === 'chat') {
      uni.navigateTo({
        url: `/pages-common/chat/index?merchantId=${msg.merchantId}`
      })
    } else if (msg.type === 'order') {
      uni.navigateTo({
        url: `/pages-user/order/detail/index?id=${msg.orderId}`
      })
    } else {
      uni.navigateTo({
        url: `/pages-user/message/detail/index?id=${msg.id}`
      })
    }
  }
}

/**
 * 全部已读
 */
const markAllRead = async () => {
  try {
    await notificationApi.markAllAsRead({
      userId: userStore.userInfo?.userId
    })

    // 更新本地状态
    messages.value.forEach(msg => {
      msg.unread = false
    })
    await updateCounts()

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
    await notificationApi.delete(msg.id, {
      userId: userStore.userInfo?.userId
    })

    // 从列表中移除
    const index = messages.value.findIndex(item => item.id === msg.id)
    if (index > -1) {
      messages.value.splice(index, 1)
      await updateCounts()
    }

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
  const readMessages = messages.value.filter(msg => !msg.unread)

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
          // 获取已读消息的ID列表
          const readIds = readMessages.map(msg => msg.id)

          await notificationApi.batchDelete({
            userId: userStore.userInfo?.userId,
            ids: readIds
          })

          // 更新本地状态
          messages.value = messages.value.filter(msg => msg.unread)
          await updateCounts()

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
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 顶部操作栏 */
.top-bar {
  @include flex-between;
  padding: $spacing-md;
  background-color: $bg-color-white;
  box-shadow: $box-shadow-sm;
}

.mark-read-btn {
  color: $primary-color;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;

  &:active {
    opacity: 0.6;
  }
}

.delete-read-btn {
  color: $text-color-secondary;
  font-size: $font-size-sm;

  &:active {
    opacity: 0.6;
  }
}

/* 筛选栏 */
.filter-bar {
  background-color: $bg-color-white;
  border-bottom: 1rpx solid $border-color-lighter;
}

.filter-scroll {
  @include flex-center;
  white-space: nowrap;
  padding: $spacing-md;
}

.filter-item {
  position: relative;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  @include flex-center;
  gap: $spacing-xs;
  flex-shrink: 0;
  transition: all 0.3s;

  &.active {
    .filter-text {
      color: $primary-color;
      font-weight: $font-weight-bold;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.filter-text {
  font-size: $font-size-base;
  color: $text-color-regular;
}

.filter-badge {
  min-width: 32rpx;
  height: 32rpx;
  @include flex-center;
  padding: 0 8rpx;
  background-color: $danger-color;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
  color: #fff;
  font-weight: $font-weight-bold;
}

/* 滚动容器 */
.scroll-container {
  flex: 1;
  height: calc(100vh - 200rpx);
}

/* 空状态 */
.empty-state {
  @include flex-center-column;
  padding: 200rpx $spacing-lg;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: $spacing-lg;
    opacity: 0.5;
  }

  .empty-text {
    font-size: $font-size-lg;
    color: $text-color-primary;
    margin-bottom: $spacing-sm;
  }

  .empty-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}

/* 消息列表 */
.message-list {
  padding: $spacing-md;
}

.message-item {
  @include flex-center;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
  transition: all 0.3s;

  &.unread {
    background-color: rgba(255, 107, 53, 0.03);
  }

  &:active {
    transform: scale(0.98);
  }
}

.message-left {
  flex-shrink: 0;
  margin-right: $spacing-md;
}

.avatar-wrapper {
  position: relative;
  width: 96rpx;
  height: 96rpx;
  @include flex-center;
  border-radius: $border-radius-lg;
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
  border-radius: $border-radius-lg;
}

.avatar-icon {
  font-size: 48rpx;
}

.unread-dot {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  width: 16rpx;
  height: 16rpx;
  background-color: $danger-color;
  border-radius: 50%;
  border: 2rpx solid $bg-color-white;
}

.message-content {
  flex: 1;
  min-width: 0;
  margin-right: $spacing-sm;
}

.message-header {
  @include flex-between;
  align-items: flex-start;
  margin-bottom: $spacing-xs;
}

.message-title {
  flex: 1;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  margin-right: $spacing-sm;
  @include text-ellipsis;
}

.message-time {
  flex-shrink: 0;
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.message-body {
  margin-bottom: $spacing-xs;
}

.message-preview {
  font-size: $font-size-sm;
  color: $text-color-regular;
  line-height: $line-height-base;
  @include text-ellipsis-multi(2);
}

.order-info {
  @include flex-center;
  gap: $spacing-xs;
}

.order-status {
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
  font-weight: $font-weight-medium;
}

.order-text {
  flex: 1;
  font-size: $font-size-sm;
  color: $text-color-regular;
  @include text-ellipsis;
}

.message-footer {
  @include flex-center;
  gap: $spacing-xs;
}

.tag-item {
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

.chat-preview {
  flex: 1;
  font-size: $font-size-xs;
  color: $text-color-secondary;
  @include text-ellipsis;
}

.message-right {
  flex-shrink: 0;
}

.delete-btn {
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  font-size: 48rpx;
  color: $text-color-placeholder;

  &:active {
    color: $danger-color;
  }
}

/* 加载状态 */
.load-more {
  @include flex-center;
  padding: $spacing-lg 0;
}

.load-text {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}
</style>
