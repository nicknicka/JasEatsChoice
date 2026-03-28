<template>
  <view class="notification-list-container">
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

    <!-- 通知列表 - NOTIF-001 -->
    <scroll-view
      class="notification-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 全选 -->
      <view class="select-all-bar" v-if="notificationList.length > 0">
        <checkbox
          :checked="isAllSelected"
          @click="toggleSelectAll"
          color="#FF6B35"
        />
        <text class="select-all-text">全选</text>
      </view>

      <view
        class="notification-item"
        v-for="notification in notificationList"
        :key="notification.id"
        :class="{ unread: !notification.isRead, selected: selectedIds.includes(notification.id) }"
        @tap="goToDetail(notification)"
      >
        <!-- 选择框 -->
        <checkbox
          class="item-checkbox"
          :checked="selectedIds.includes(notification.id)"
          @click.stop="toggleSelect(notification.id)"
          color="#FF6B35"
        />

        <!-- 图标 -->
        <view class="notification-icon" :class="'type-' + notification.type">
          <uni-icons
            :type="getIconType(notification.type)"
            size="24"
            :color="notification.isRead ? '#999' : '#FF6B35'"
          />
        </view>

        <!-- 内容 -->
        <view class="notification-content">
          <view class="content-header">
            <text class="title">{{ notification.title }}</text>
            <text class="time">{{ notification.time }}</text>
          </view>
          <text class="description">{{ notification.content }}</text>

          <!-- 额外信息 -->
          <view class="extra-info" v-if="notification.extra">
            <image
              class="extra-image"
              v-if="notification.extra.image"
              :src="notification.extra.image"
              mode="aspectFill"
            />
            <text class="extra-text" v-if="notification.extra.text">
              {{ notification.extra.text }}
            </text>
          </view>
        </view>

        <!-- 未读标识 -->
        <view class="unread-dot" v-if="!notification.isRead"></view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="notificationList.length === 0 && !loading">
        <empty text="暂无通知" icon="📢" />
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="loading">
        <uni-load-more :status="hasMore ? 'loading' : 'noMore'" />
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
import { ref, computed, onMounted } from 'vue'
import { notificationApi } from '@/api/modules/notification.js'
import { formatRelativeTime } from '@/utils/helper'

const userId = ref('')

// 类型筛选
const activeType = ref('all')
const typeTabs = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '系统', value: 'system', count: 0 },
  { label: '订单', value: 'order', count: 0 },
  { label: '活动', value: 'activity', count: 0 },
  { label: '互动', value: 'chat', count: 0 }
])

// 通知列表
const notificationList = ref([])
const selectedIds = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 是否全选
const isAllSelected = computed(() => {
  return notificationList.value.length > 0 &&
         selectedIds.value.length === notificationList.value.length
})

onMounted(() => {
  userId.value = uni.getStorageSync('userId') || ''
  // NOTIF-001: 加载通知列表
  loadNotificationList()
})

/**
 * NOTIF-001: 加载通知列表
 */
const loadNotificationList = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (isRefresh) {
      pageNum.value = 1
      hasMore.value = true
    }

    const params = {
      userId: userId.value,
      page: pageNum.value,
      size: pageSize.value
    }

    if (activeType.value !== 'all') {
      params.type = activeType.value
    }

    // NOTIF-001: 调用API获取通知列表
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

      if (isRefresh) {
        notificationList.value = formattedNotifications
      } else {
        notificationList.value.push(...formattedNotifications)
      }

      // 更新未读计数
      if (res.data.unreadCount !== undefined) {
        updateUnreadCount(res.data.unreadCount)
      }

      // 更新类型计数
      if (res.data.counts) {
        typeTabs.value.forEach(tab => {
          tab.count = res.data.counts[tab.value] || 0
        })
      }

      hasMore.value = notifications.length >= pageSize.value
      pageNum.value++
    }

    loading.value = false
    refreshing.value = false
  } catch (error) {
    console.error('加载通知列表失败:', error)
    loading.value = false
    refreshing.value = false
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadNotificationList(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
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
 * NOTIF-002: 批量标记已读
 */
const batchMarkRead = async () => {
  if (selectedIds.value.length === 0) return

  try {
    uni.showLoading({ title: '处理中...' })

    // NOTIF-002: 调用API批量标记已读
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
    } else {
      throw new Error(res.message || '操作失败')
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

          // 调用API批量删除
          const res = await notificationApi.batchDelete({
            userId: userId.value,
            ids: selectedIds.value
          })

          uni.hideLoading()

          if (res.code === 200) {
            // 从列表中移除
            notificationList.value = notificationList.value.filter(
              n => !selectedIds.value.includes(n.id)
            )

            selectedIds.value = []

            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
          } else {
            throw new Error(res.message || '删除失败')
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
 * NOTIF-003: 全部标记已读
 */
const markAllRead = async () => {
  if (notificationList.value.length === 0) return

  try {
    uni.showLoading({ title: '处理中...' })

    // NOTIF-003: 调用API全部标记已读
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
    } else {
      throw new Error(res.message || '操作失败')
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
 * NOTIF-004: 清空通知
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

          // NOTIF-004: 调用API清空通知
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
          } else {
            throw new Error(apiRes.message || '清空失败')
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
  uni.navigateTo({
    url: `/pages-user/notification/detail?id=${notification.id}`
  })
}

/**
 * 获取图标类型
 */
const getIconType = (type) => {
  const iconMap = {
    system: 'sound',
    order: 'paperplane',
    activity: 'gift',
    chat: 'chatbubble'
  }
  return iconMap[type] || 'notification'
}

/**
 * 更新未读计数
 */
const updateUnreadCount = (count) => {
  // 更新TabBar的未读数
  const unreadCount = count || 0

  // 通过事件总线或全局状态更新
  uni.$emit('updateUnreadCount', unreadCount)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.notification-list-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 顶部Tab */
.header-tabs {
  background: #fff;
  padding: 20rpx;
  display: flex;
  gap: 15rpx;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  @include flex-center;
  font-size: 26rpx;
  color: #666;
  position: relative;

  &.active {
    background: #FF6B35;
    color: #fff;
    font-weight: bold;
  }
}

.badge {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 8rpx;
  background: #F5222D;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  @include flex-center;
}

/* 批量操作栏 */
.batch-actions {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #eee;
}

.select-count {
  font-size: 26rpx;
  color: #333;
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
  font-size: 24rpx;
  background: #F5F5F5;
  color: #666;
  border: none;

  &.danger {
    background: rgba(245, 34, 45, 0.1);
    color: #F5222D;
  }
}

/* 列表 */
.notification-scroll {
  height: calc(100vh - 300rpx);
  padding: 20rpx;
}

.select-all-bar {
  background: #fff;
  padding: 20rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.select-all-text {
  font-size: 26rpx;
  color: #666;
}

.notification-item {
  background: #fff;
  border-radius: 12rpx;
  padding: 25rpx;
  margin-bottom: 20rpx;
  display: flex;
  gap: 20rpx;
  position: relative;

  &.unread {
    background: linear-gradient(to right, #FFF7E6 0%, #fff 50%);
  }

  &.selected {
    background: rgba(255, 107, 53, 0.05);
  }
}

.item-checkbox {
  flex-shrink: 0;
}

.notification-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #F5F5F5;
  @include flex-center;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10rpx;
}

.title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  flex: 1;
}

.time {
  font-size: 24rpx;
  color: #999;
  flex-shrink: 0;
}

.description {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.extra-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.extra-image {
  width: 80rpx;
  height: 80rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.extra-text {
  font-size: 24rpx;
  color: #999;
}

.unread-dot {
  position: absolute;
  top: 25rpx;
  right: 25rpx;
  width: 16rpx;
  height: 16rpx;
  background: #F5222D;
  border-radius: 50%;
}

/* 底部操作 */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.bottom-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: 26rpx;
  background: #F5F5F5;
  color: #666;
  border: none;

  &[disabled] {
    opacity: 0.5;
  }

  &.danger {
    background: rgba(245, 34, 45, 0.1);
    color: #F5222D;
  }
}
</style>
