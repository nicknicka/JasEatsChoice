<template>
  <view class="notification-list-wrapper">
    <!-- 顶部筛选 -->
    <view class="header-tabs">
      <view
        class="tab-item"
        :class="{ active: activeType === item.value }"
        v-for="item in typeTabs"
        :key="item.value"
        @tap="changeType(item.value)"
      >
        {{ item.label }}
        <view class="badge" v-if="item.count > 0">{{ item.count }}</view>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view class="batch-actions" v-if="selectedIds.length > 0">
      <text class="select-count">已选{{ selectedIds.length }}条</text>
      <view class="action-buttons">
        <button class="action-btn" @tap="batchMarkRead">标记已读</button>
        <button class="action-btn danger" @tap="batchDelete">删除</button>
      </view>
    </view>

    <!-- 通知列表 -->
    <scroll-view
      class="notification-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 骨架屏 -->
      <view v-if="loading && notificationList.length === 0" class="skeleton-container">
        <SkeletonScreen :count="5" />
      </view>

      <!-- 批量操作栏 -->
      <view class="batch-actions" v-if="selectedIds.length > 0">
        <text class="select-count">已选{{ selectedIds.length }}条</text>
        <view class="action-buttons">
          <button class="action-btn" @tap="batchMarkRead">标记已读</button>
          <button class="action-btn danger" @tap="batchDelete">删除</button>
        </view>
      </view>

      <!-- 通知卡片列表 -->
      <view class="notification-cards">
        <MessageCard
          v-for="notification in notificationList"
          :key="notification.id"
          :type="notification.type"
          :title="notification.title"
          :description="notification.content"
          :time="notification.time"
          :badge="!notification.isRead ? 1 : 0"
          :tags="notification.tags"
          :class="{ selected: selectedIds.includes(notification.id) }"
          @click="goToDetail(notification)"
        >
          <template #extra v-if="notification.extra">
            <image
              v-if="notification.extra.image"
              :src="notification.extra.image"
              class="notification-extra-image"
              mode="aspectFill"
            />
            <text v-if="notification.extra.text" class="notification-extra-text">
              {{ notification.extra.text }}
            </text>
          </template>
        </MessageCard>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="loading">
        <text>加载中...</text>
      </view>

      <!-- 没有更多 -->
      <view class="load-status" v-else-if="!hasMore && notificationList.length > 0">
        <text>没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="notificationList.length === 0 && !loading">
        <text class="empty-icon">📢</text>
        <text class="empty-text">暂无通知</text>
        <text class="empty-tips">系统消息和活动通知会在这里显示</text>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <button class="bottom-btn" @tap="markAllRead" :disabled="notificationList.length === 0">
        <uni-icons type="checkmarkempty" size="18" color="#666"></uni-icons>
        <text>全部已读</text>
      </button>
      <button class="bottom-btn danger" @tap="clearAll" :disabled="notificationList.length === 0">
        <uni-icons type="trash" size="18" color="#666"></uni-icons>
        <text>清空</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
// import { notificationApi } from '@/api'
import { formatRelativeTime } from '../../../utils/helper'
import MessageCard from './MessageCard.vue'
import SkeletonScreen from './SkeletonScreen.vue'
// import { MessageUtils } from '@/utils/messageUtils'
// import { NotificationType } from '@/enums/message'

// 临时类型定义
const NotificationType = {
  SYSTEM: 'system',
  ORDER: 'order',
  ACTIVITY: 'activity'
}

const userId = ref('')

// 类型筛选
const activeType = ref('all')
const typeTabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '系统', value: 'system', count: 0 },
  { label: '订单', value: 'order', count: 0 },
  { label: '活动', value: 'activity', count: 0 }
])

// 通知列表
const notificationList = ref([])
const selectedIds = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 未读总数
const unreadCount = computed(() => {
  return typeTabs.value[0].count || 0
})

// 是否全选
const isAllSelected = computed(() => {
  return notificationList.value.length > 0 &&
         selectedIds.value.length === notificationList.value.length
})

/**
 * 加载通知列表
 */
const loadNotificationList = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true

  if (isRefresh) {
    pageNum.value = 1
    refreshing.value = true
    hasMore.value = true
  }

  try {
    userId.value = uni.getStorageSync('userId') || ''

    const params = {
      userId: userId.value,
      page: pageNum.value,
      size: pageSize.value
    }

    if (activeType.value !== 'all') {
      params.type = activeType.value
    }

    // 调用API获取通知列表
    const res = await notificationApi.getList(params)

    if (res.code === 200 && res.data) {
      const notifications = res.data.list || res.data || []

      // 转换数据格式
      const formattedNotifications = notifications.map(notif => ({
        id: notif.id,
        type: notif.type || 'system',
        title: notif.title || '通知',
        content: notif.content || '',
        isRead: notif.isRead || false,
        time: formatRelativeTime(notif.createdAt),
        extra: notif.extra || null
      }))

      if (isRefresh || pageNum.value === 1) {
        notificationList.value = formattedNotifications
      } else {
        notificationList.value.push(...formattedNotifications)
      }

      // 更新未读计数
      if (res.data.unreadCount !== undefined) {
        typeTabs.value[0].count = res.data.unreadCount
      }

      // 更新类型计数
      if (res.data.counts) {
        typeTabs.value.forEach(tab => {
          tab.count = res.data.counts[tab.value] || 0
        })
      }

      hasMore.value = notifications.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载通知列表失败:', error)

    // 开发阶段：使用模拟数据
    if (pageNum.value === 1) {
      notificationList.value = generateMockNotifications()
    }
    hasMore.value = false
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟通知数据
 */
const generateMockNotifications = () => {
  return [
    {
      id: 1,
      type: NotificationType.ORDER,
      title: '订单状态更新',
      content: '您的订单已发货，预计明天送达',
      isRead: false,
      time: '10分钟前',
      tags: ['配送中', '明日达'],
      extra: null
    },
    {
      id: 2,
      type: NotificationType.SYSTEM,
      title: '优惠券到期提醒',
      content: '您有一张优惠券即将到期，请尽快使用',
      isRead: false,
      time: '1小时前',
      tags: ['优惠券', '即将到期'],
      extra: null
    },
    {
      id: 3,
      type: NotificationType.ACTIVITY,
      title: '限时优惠活动',
      content: '新用户专享优惠，全场8折起',
      isRead: true,
      time: '2小时前',
      tags: ['限时', '新用户专享'],
      extra: {
        image: 'https://via.placeholder.com/200/FF6B35/FFFFFF?text=活动',
        text: '点击查看详情'
      }
    }
  ]
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  loadNotificationList(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    pageNum.value++
    loadNotificationList()
  }
}

/**
 * 切换类型
 */
const changeType = (type) => {
  activeType.value = type
  selectedIds.value = []
  loadNotificationList(true)
}

/**
 * 切换选择
 */
const toggleSelect = (id) => {
  const index = selectedIds.value.indexOf(id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(id)
  }
}

/**
 * 全选/取消全选
 */
const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = notificationList.value.map(n => n.id)
  }
}

/**
 * 批量标记已读
 */
const batchMarkRead = async () => {
  if (selectedIds.value.length === 0) return

  try {
    uni.showLoading({ title: '处理中...' })

    const res = await notificationApi.batchMarkAsRead({
      userId: userId.value,
      ids: selectedIds.value
    })

    uni.hideLoading()

    if (res.code === 200) {
      // 更新本地状态
      notificationList.value.forEach(notif => {
        if (selectedIds.value.includes(notif.id)) {
          notif.isRead = true
        }
      })

      selectedIds.value = []

      uni.showToast({
        title: '已标记为已读',
        icon: 'success'
      })

      // 更新未读计数
      updateUnreadCount(0)
    }
  } catch (error) {
    console.error('批量标记已读失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
}

/**
 * 批量删除
 */
const batchDelete = async () => {
  if (selectedIds.value.length === 0) return

  uni.showModal({
    title: '确认删除',
    content: `确定删除选中的${selectedIds.value.length}条通知吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })

          const resDelete = await notificationApi.batchDelete({
            userId: userId.value,
            ids: selectedIds.value
          })

          uni.hideLoading()

          if (resDelete.code === 200) {
            // 从列表中移除
            notificationList.value = notificationList.value.filter(
              n => !selectedIds.value.includes(n.id)
            )

            selectedIds.value = []

            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
          }
        } catch (error) {
          console.error('批量删除失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 全部标记已读
 */
const markAllRead = async () => {
  if (notificationList.value.length === 0) return

  try {
    uni.showLoading({ title: '处理中...' })

    const res = await notificationApi.markAllAsRead({
      userId: userId.value
    })

    uni.hideLoading()

    if (res.code === 200) {
      // 更新本地状态
      notificationList.value.forEach(notif => {
        notif.isRead = true
      })

      uni.showToast({
        title: '已全部标记为已读',
        icon: 'success'
      })

      // 更新未读计数
      updateUnreadCount(0)
    }
  } catch (error) {
    console.error('全部标记已读失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: '操作失败',
      icon: 'none'
    })
  }
}

/**
 * 清空通知
 */
const clearAll = async () => {
  if (notificationList.value.length === 0) return

  uni.showModal({
    title: '确认清空',
    content: '确定清空所有通知吗？此操作不可恢复。',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '清空中...' })

          const apiRes = await notificationApi.clear({
            userId: userId.value
          })

          uni.hideLoading()

          if (apiRes.code === 200) {
            notificationList.value = []

            uni.showToast({
              title: '已清空',
              icon: 'success'
            })

            // 更新未读计数
            updateUnreadCount(0)
          }
        } catch (error) {
          console.error('清空失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: '清空失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 跳转到详情
 */
const goToDetail = (notification) => {
  // 标记为已读
  if (!notification.isRead) {
    notification.isRead = true
    // 更新未读数
    const currentUnread = typeTabs.value[0].count
    typeTabs.value[0].count = Math.max(0, currentUnread - 1)
  }

  // 根据类型跳转
  if (notification.type === 'order') {
    // 跳转到订单详情
    uni.navigateTo({
      url: `/src/pages-user/order/detail/index?id=${notification.extra?.orderId || ''}`
    })
  } else if (notification.type === 'activity') {
    // 跳转到活动页面
    uni.navigateTo({
      url: `/src/pages-user/activity/index?id=${notification.extra?.activityId || ''}`
    })
  } else {
    // 跳转到通知详情页
    uni.navigateTo({
      url: `/src/pages-user/notification/detail/index?id=${notification.id}`
    })
  }
}

/**
 * 获取图标类型
 */
const getIconType = (type) => {
  const iconMap = {
    system: 'sound',
    order: 'paperplane',
    activity: 'gift'
  }
  return iconMap[type] || 'notification'
}

/**
 * 获取图标 emoji
 */
const getIconEmoji = (type) => {
  const emojiMap = {
    system: '📢',
    order: '📦',
    activity: '🎉'
  }
  return emojiMap[type] || '📄'
}

/**
 * 更新未读计数
 */
const updateUnreadCount = (count) => {
  typeTabs.value[0].count = count || 0
}

/**
 * 刷新列表
 */
const refresh = () => {
  loadNotificationList(true)
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
  getUnreadCount
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.notification-list-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: $bg-color-base;
}

/* 顶部Tab */
.header-tabs {
  background: #fff;
  padding: 20rpx;
  display: flex;
  gap: 15rpx;
  border-bottom: 1rpx solid $border-color-lighter;
}

.tab-item {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  @include flex-center;
  font-size: $font-size-base;
  color: $text-color-secondary;
  position: relative;

  &.active {
    background: $primary-color;
    color: #fff;
    font-weight: $font-weight-bold;
  }
}

.badge {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: $danger-color;
  color: #fff;
  font-size: $font-size-xs;
  border-radius: $border-radius-round;
  @include flex-center;
  font-weight: $font-weight-bold;
}

/* 批量操作栏 */
.batch-actions {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid $border-color-lighter;
}

.select-count {
  font-size: $font-size-sm;
  color: $text-color-primary;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
}

.action-btn {
  padding: 0 20rpx;
  height: 60rpx;
  line-height: 60rpx;
  border-radius: 30rpx;
  font-size: $font-size-sm;
  background: $bg-color-base;
  color: $text-color-secondary;
  border: none;

  &.danger {
    background: rgba(245, 34, 45, 0.1);
    color: $danger-color;
  }
}

/* 列表 */
.notification-scroll {
  flex: 1;
  padding: 20rpx;
}

.skeleton-container {
  padding: 20rpx 0;
}

.notification-cards {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.notification-card {
  &.selected {
    opacity: 0.6;
  }
}

.notification-extra-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
  margin-top: 10rpx;
}

.notification-extra-text {
  display: block;
  font-size: $font-size-sm;
  color: $primary-color;
  margin-top: 10rpx;
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

/* 底部操作 */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.08);
  border-top: 1rpx solid $border-color-lighter;
  z-index: 10;
}

.bottom-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: $font-size-base;
  background: $bg-color-base;
  color: $text-color-secondary;
  border: none;
  transition: all 0.3s ease;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.08);

  &:active:not([disabled]) {
    transform: scale(0.95);
  }

  &[disabled] {
    opacity: 0.4;
    cursor: not-allowed;
  }

  &.danger {
    background: linear-gradient(135deg, rgba(245, 34, 45, 0.1), rgba(245, 34, 45, 0.15));
    color: $danger-color;
  }
}
</style>
