<template>
  <view class="notification-center-container">
    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <text class="page-title">消息中心</text>
      <view class="top-actions">
        <button class="action-btn" @tap="markAllRead">
          <uni-icons type="checkbox" size="16" color="#666"></uni-icons>
          <text>全部已读</text>
        </button>
        <button class="action-btn" @tap="clearAll">
          <uni-icons type="trash" size="16" color="#666"></uni-icons>
          <text>清空</text>
        </button>
      </view>
    </view>

    <!-- 分类标签 -->
    <view class="category-tabs">
      <view
        class="tab-item"
        :class="{ active: activeCategory === item.value }"
        v-for="item in categoryTabs"
        :key="item.value"
        @tap="changeCategory(item.value)"
      >
        {{ item.label }}
        <view class="tab-badge" v-if="item.unread > 0">
          {{ item.unread > 99 ? '99+' : item.unread }}
        </view>
      </view>
    </view>

    <!-- 通知列表 -->
    <scroll-view
      class="notification-list"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 按日期分组 -->
      <view
        class="date-group"
        v-for="group in notificationGroups"
        :key="group.date"
      >
        <view class="date-header">
          <text class="date-text">{{ group.date }}</text>
        </view>

        <view
          class="notification-item"
          :class="{ unread: !item.isRead }"
          v-for="item in group.notifications"
          :key="item.id"
          @tap="viewNotification(item)"
        >
          <!-- 图标 -->
          <view class="notification-icon" :class="'type-' + item.type">
            <uni-icons
              :type="getNotificationIcon(item.type)"
              size="24"
              :color="item.isRead ? '#999' : '#FF6B35'"
            ></uni-icons>
          </view>

          <!-- 内容 -->
          <view class="notification-content">
            <view class="content-header">
              <text class="content-title">{{ item.title }}</text>
              <text class="content-time">{{ item.time }}</text>
            </view>
            <text class="content-preview">{{ item.preview }}</text>
          </view>

          <!-- 状态标识 -->
          <view class="notification-status" v-if="!item.isRead"></view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="notificationList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="notificationList.length === 0 && !loading">
        <empty text="暂无消息" icon="📬" buttonText="去逛逛" @button-click="goHome" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 分类标签
const categoryTabs = ref([
  { label: '全部', value: 'all', unread: 0 },
  { label: '订单', value: 'order', unread: 3 },
  { label: '菜品', value: 'dish', unread: 1 },
  { label: '商家', value: 'merchant', unread: 0 },
  { label: '评价', value: 'review', unread: 2 },
  { label: '系统', value: 'system', unread: 1 }
])

const activeCategory = ref('all')

// 通知列表
const notificationList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

onMounted(() => {
  loadNotifications()
})

/**
 * 按日期分组的通知列表
 */
const notificationGroups = computed(() => {
  const groups = {}

  notificationList.value.forEach(item => {
    if (!groups[item.date]) {
      groups[item.date] = []
    }
    groups[item.date].push(item)
  })

  return Object.entries(groups).map(([date, notifications]) => ({ date, notifications }))
})

/**
 * 切换分类
 */
const changeCategory = (category) => {
  activeCategory.value = category
  loadNotifications(true)
}

/**
 * 加载通知列表
 */
const loadNotifications = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // TODO: 调用API获取通知列表
    // const res = await userApi.getNotifications({
    //   type: activeCategory.value,
    //   page: page.value,
    //   size: pageSize
    // })

    // 模拟数据
    setTimeout(() => {
      const mockData = generateMockNotifications()
      if (isRefresh) {
        notificationList.value = mockData
      } else {
        notificationList.value = [...notificationList.value, ...mockData]
      }

      // 更新未读数
      updateUnreadCounts()

      if (mockData.length < pageSize) {
        noMore.value = true
      }

      loading.value = false
      refreshing.value = false
    }, 500)
  } catch (error) {
    console.error('加载通知失败:', error)
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成模拟通知数据
 */
const generateMockNotifications = () => {
  const notifications = []
  const count = Math.floor(Math.random() * 5) + 5

  const types = [
    {
      type: 'order',
      titles: ['订单已接单', '订单已取消', '订单配送中', '订单已完成'],
      previews: ['您的订单已被商家接单，正在准备中', '您的订单已被取消', '您的订单正在配送中，请保持电话畅通', '您的订单已完成，欢迎再次光临']
    },
    {
      type: 'dish',
      titles: ['新菜品推荐', '限时优惠', '您关注的菜品降价了'],
      previews: ['老王家常菜推出了新菜品，快来看看吧', '您关注的宫保鸡丁限时优惠8折', '您关注的麻婆豆腐降价了']
    },
    {
      type: 'review',
      titles: ['收到新评价', '您的评价已被回复'],
      previews: ['您的订单收到新评价，快来看看吧', '商家回复了您的评价']
    },
    {
      type: 'system',
      titles: ['系统通知', '账户余额变动', '优惠券到账'],
      previews: ['您的账户余额不足，请及时充值', '您有一张优惠券即将到期', '系统升级公告']
    }
  ]

  const dates = ['今天', '昨天', '前天', '3月17日']

  for (let i = 0; i < count; i++) {
    const typeGroup = types[Math.floor(Math.random() * types.length)]
    const item = typeGroup[Math.floor(Math.random() * typeGroup.length)]

    notifications.push({
      id: page.value * 20 + i,
      type: typeGroup[0].type,
      title: item.title,
      preview: item.preview,
      time: getRandomTime(),
      date: dates[Math.floor(Math.random() * dates.length)],
      isRead: Math.random() > 0.7
    })
  }

  return notifications
}

/**
 * 随机时间
 */
const getRandomTime = () => {
  const hours = Math.floor(Math.random() * 12) + 1
  const minutes = Math.floor(Math.random() * 60).toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

/**
 * 更新未读数
 */
const updateUnreadCounts = () => {
  const unreadByType = {
    order: 0,
    dish: 0,
    merchant: 0,
    review: 0,
    system: 0
  }

  notificationList.value.forEach(item => {
    if (!item.isRead) {
      unreadByType[item.type] = (unreadByType[item.type] || 0) + 1
    }
  })

  categoryTabs.value.forEach(tab => {
    if (tab.value !== 'all') {
      tab.unread = unreadByType[tab.value] || 0
    }
  })

  categoryTabs.value[0].unread = notificationList.value.filter(item => !item.isRead).length
}

/**
 * 获取通知图标
 */
const getNotificationIcon = (type) => {
  const iconMap = {
    order: 'shop',
    dish: 'image',
    merchant: 'home',
    review: 'star',
    system: 'notification'
  }
  return iconMap[type] || 'notification'
}

/**
 * 查看通知详情
 */
const viewNotification = (notification) => {
  if (!notification.isRead) {
    // 标记为已读
    notification.isRead = true
    updateUnreadCounts()

    // TODO: 调用API标记为已读
  }

  uni.navigateTo({
    url: `/pages/notification/detail?id=${notification.id}`
  })
}

/**
 * 全部已读
 */
const markAllRead = () => {
  if (notificationList.value.length === 0) {
    uni.showToast({
      title: '暂无消息',
      icon: 'none'
    })
    return
  }

  const unreadCount = notificationList.value.filter(item => !item.isRead).length
  if (unreadCount === 0) {
    uni.showToast({
      title: '全部已读',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '全部已读',
    content: `将${unreadCount}条未读消息标记为已读`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API全部已读
        notificationList.value.forEach(item => {
          item.isRead = true
        })
        updateUnreadCounts()

        uni.showToast({
          title: '已标记为已读',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 清空消息
 */
const clearAll = () => {
  if (notificationList.value.length === 0) {
    uni.showToast({
      title: '暂无消息',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '清空消息',
    content: '确定清空所有消息吗？此操作不可恢复。',
    confirmColor: '#F5222D',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API清空消息
        notificationList.value = []
        updateUnreadCounts()

        uni.showToast({
          title: '已清空',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadNotifications(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadNotifications()
  }
}

/**
 * 返回首页
 */
const goHome = () => {
  uni.switchTab({
    url: '/home/index/index'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.notification-center-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 顶部操作栏 */
.top-bar {
  background: #fff;
  padding: 25rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #eee;
}

.page-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.top-actions {
  display: flex;
  gap: 15rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #F5F5F5;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
  border: none;
}

/* 分类标签 */
.category-tabs {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  gap: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.tab-item {
  position: relative;
  padding: 10rpx 20rpx;
  font-size: 28rpx;
  color: #666;

  &.active {
    color: #FF6B35;
    font-weight: bold;
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

/* 通知列表 */
.notification-list {
  flex: 1;
  padding: 20rpx;
}

.date-group {
  margin-bottom: 30rpx;
}

.date-header {
  padding: 20rpx 0;
  text-align: center;
}

.date-text {
  font-size: 24rpx;
  color: #999;
  padding: 8rpx 20rpx;
  background: #E8E8E8;
  border-radius: 20rpx;
}

.notification-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 25rpx;
  margin-bottom: 15rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  position: relative;

  &.unread {
    background: #FFFBF0;
  }
}

.notification-icon {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  background: #F5F5F5;
  @include flex-center;
  flex-shrink: 0;
}

.notification-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-title {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.content-time {
  font-size: 24rpx;
  color: #999;
  flex-shrink: 0;
}

.content-preview {
  font-size: 26rpx;
  color: #999;
  @include text-ellipsis;
  line-height: 1.6;
}

.notification-status {
  position: absolute;
  top: 25rpx;
  right: 25rpx;
  width: 12rpx;
  height: 12rpx;
  background: #F5222D;
  border-radius: 50%;
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
</style>
