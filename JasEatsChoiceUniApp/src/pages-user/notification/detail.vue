<template>
  <view class="notification-detail-container">
    <!-- 加载状态 -->
    <view class="loading-state" v-if="loading">
      <uni-load-more status="loading" />
    </view>

    <!-- 详情内容 -->
    <template v-else-if="notificationDetail.id">
      <!-- 顶部导航 -->
      <view class="nav-bar">
        <view class="nav-back" @tap="goBack">
          <uni-icons type="arrowleft" size="20" color="#333"></uni-icons>
        </view>
        <text class="nav-title">通知详情</text>
        <view class="nav-more" @tap="showMoreMenu">
          <uni-icons type="more" size="20" color="#333"></uni-icons>
        </view>
      </view>

      <!-- 通知卡片 -->
      <view class="notification-card">
        <!-- 类型图标 -->
        <view class="card-icon" :class="'type-' + notificationDetail.type">
          <uni-icons
            :type="getIconType(notificationDetail.type)"
            size="40"
            color="#FF6B35"
          />
        </view>

        <!-- 标题和时间 -->
        <view class="card-header">
          <text class="title">{{ notificationDetail.title }}</text>
          <text class="time">{{ notificationDetail.time }}</text>
        </view>

        <!-- 内容 -->
        <text class="content">{{ notificationDetail.content }}</text>

        <!-- 额外信息 -->
        <view class="extra-section" v-if="notificationDetail.extra">
          <!-- 图片 -->
          <image
            class="extra-image"
            v-if="notificationDetail.extra.image"
            :src="notificationDetail.extra.image"
            mode="aspectFill"
            @tap="previewImage"
          />

          <!-- 关联对象 -->
          <view class="related-card" v-if="notificationDetail.extra.related">
            <image
              class="related-image"
              v-if="notificationDetail.extra.related.image"
              :src="notificationDetail.extra.related.image"
              mode="aspectFill"
            />
            <view class="related-info">
              <text class="related-title">{{ notificationDetail.extra.related.title }}</text>
              <text class="related-desc">{{ notificationDetail.extra.related.description }}</text>
            </view>
          </view>

          <!-- 额外文本 -->
          <text class="extra-text" v-if="notificationDetail.extra.text">
            {{ notificationDetail.extra.text }}
          </text>
        </view>

        <!-- 跳转按钮 -->
        <button
          class="action-btn"
          v-if="notificationDetail.action"
          @tap="handleAction"
        >
          {{ notificationDetail.action.text }}
        </button>
      </view>

      <!-- 相关列表（如果有） -->
      <view class="related-list" v-if="notificationDetail.relatedList && notificationDetail.relatedList.length > 0">
        <text class="section-title">相关内容</text>
        <view
          class="related-item"
          v-for="item in notificationDetail.relatedList"
          :key="item.id"
          @tap="handleRelatedClick(item)"
        >
          <image class="item-image" :src="item.image" mode="aspectFill"></image>
          <view class="item-info">
            <text class="item-title">{{ item.title }}</text>
            <text class="item-desc">{{ item.description }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { notificationApi } from '@/api/modules/notification.js'
import { formatShortDateTime } from '@/utils/helper'

const notificationId = ref('')
const userId = ref('')
const loading = ref(true)

// 通知详情 - NOTIF-005
const notificationDetail = ref({
  id: '',
  type: 'system',
  title: '',
  content: '',
  isRead: false,
  time: '',
  extra: null,
  action: null,
  relatedList: []
})

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  notificationId.value = options.id || ''
  userId.value = uni.getStorageSync('userId') || ''

  // NOTIF-005: 加载通知详情
  loadNotificationDetail()
})

/**
 * NOTIF-005: 加载通知详情
 */
const loadNotificationDetail = async () => {
  try {
    // NOTIF-005: 调用API获取通知详情
    const res = await notificationApi.getDetail(notificationId.value)

    if (res.code === 200 && res.data) {
      const data = res.data

      // 解析额外信息
      let extra = null
      try {
        extra = typeof data.extra === 'string' ? JSON.parse(data.extra) : data.extra
      } catch (e) {
        extra = data.extra
      }

      notificationDetail.value = {
        id: data.id,
        type: data.type || 'system',
        title: data.title || '通知',
        content: data.content || '',
        isRead: data.isRead || false,
        time: formatShortDateTime(data.createdAt),
        extra: extra,
        action: data.action || null,
        relatedList: data.relatedList || []
      }

      // NOTIF-006: 如果未读，标记为已读
      if (!data.isRead) {
        await markAsRead()
      }
    }

    loading.value = false
  } catch (error) {
    console.error('加载通知详情失败:', error)
    loading.value = false
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * NOTIF-006: 标记为已读
 */
const markAsRead = async () => {
  try {
    // NOTIF-006: 调用API标记为已读
    const res = await notificationApi.markAsRead(notificationId.value, {
      userId: userId.value
    })

    if (res.code === 200) {
      notificationDetail.value.isRead = true

      // 更新列表页的未读数
      uni.$emit('notificationRead', notificationId.value)
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

/**
 * NOTIF-007: 删除通知
 */
const deleteNotification = () => {
  uni.showModal({
    title: '确认删除',
    content: '确定删除此通知吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })

          // NOTIF-007: 调用API删除通知
          const apiRes = await notificationApi.delete(notificationId.value, {
            userId: userId.value
          })

          uni.hideLoading()

          if (apiRes.code === 200) {
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })

            setTimeout(() => {
              uni.navigateBack()
            }, 1500)
          } else {
            throw new Error(apiRes.message || '删除失败')
          }
        } catch (error) {
          console.error('删除通知失败:', error)
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
 * 显示更多菜单
 */
const showMoreMenu = () => {
  const menuItems = ['标记为未读', '删除']

  uni.showActionSheet({
    itemList: menuItems,
    success: (res) => {
      if (res.tapIndex === 0) {
        // TODO: 标记为未读（需要后端支持）
        uni.showToast({
          title: '功能开发中',
          icon: 'none'
        })
      } else if (res.tapIndex === 1) {
        deleteNotification()
      }
    }
  })
}

/**
 * 处理跳转操作
 */
const handleAction = () => {
  if (!notificationDetail.value.action) return

  const action = notificationDetail.value.action

  if (action.type === 'url') {
    // 跳转URL
    if (action.url.startsWith('/')) {
      uni.navigateTo({ url: action.url })
    } else {
      // 外部链接
      plus.runtime.openURL(action.url)
    }
  } else if (action.type === 'navigate') {
    uni.navigateTo({ url: action.url })
  } else if (action.type === 'switchTab') {
    uni.switchTab({ url: action.url })
  }
}

/**
 * 预览图片
 */
const previewImage = () => {
  if (!notificationDetail.value.extra || !notificationDetail.value.extra.image) return

  uni.previewImage({
    urls: [notificationDetail.value.extra.image],
    current: 0
  })
}

/**
 * 处理相关项点击
 */
const handleRelatedClick = (item) => {
  if (item.url) {
    uni.navigateTo({ url: item.url })
  } else if (item.type) {
    // 根据类型跳转
    switch (item.type) {
      case 'dish':
        uni.navigateTo({ url: `/pages-user/dish/detail?id=${item.id}` })
        break
      case 'order':
        uni.navigateTo({ url: `/pages-user/order/detail?id=${item.id}` })
        break
      case 'merchant':
        uni.navigateTo({ url: `/pages-user/merchant/detail?id=${item.id}` })
        break
    }
  }
}

/**
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
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
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.notification-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
}

.loading-state {
  padding: 100rpx 0;
}

/* 导航栏 */
.nav-bar {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid #eee;
}

.nav-back {
  width: 60rpx;
  height: 60rpx;
  @include flex-center;
}

.nav-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.nav-more {
  width: 60rpx;
  height: 60rpx;
  @include flex-center;
}

/* 通知卡片 */
.notification-card {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  padding: 40rpx;
}

.card-icon {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: #FFF7E6;
  @include flex-center;
  margin: 0 auto 30rpx;
}

.card-header {
  text-align: center;
  margin-bottom: 30rpx;
}

.title {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.time {
  display: block;
  font-size: 24rpx;
  color: #999;
}

.content {
  display: block;
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
  margin-bottom: 30rpx;
  text-align: justify;
}

/* 额外信息 */
.extra-section {
  margin-bottom: 30rpx;
}

.extra-image {
  width: 100%;
  max-height: 400rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.related-card {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.related-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.related-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.related-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.related-desc {
  display: block;
  font-size: 24rpx;
  color: #666;
}

.extra-text {
  display: block;
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
}

.action-btn {
  width: 100%;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
  @include flex-center;
}

/* 相关列表 */
.related-list {
  background: #fff;
  margin: 0 20rpx 20rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.related-item {
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  margin-bottom: 15rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.item-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-title {
  display: block;
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 8rpx;
}

.item-desc {
  display: block;
  font-size: 24rpx;
  color: #666;
}
</style>
