<template>
  <view class="history-container">
    <!-- 顶部操作栏 -->
    <view class="top-bar">
      <text class="title-text">浏览历史</text>
      <text class="clear-btn" @click="clearHistory" v-if="hasHistory">清空</text>
    </view>

    <!-- 历史列表 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view class="empty-state" v-if="historyList.length === 0 && !loading">
        <Empty
          icon="📖"
          text="还没有浏览记录"
          description="您浏览过的菜品会显示在这里"
          buttonText="去逛逛"
          @button-click="goToHome"
        />
      </view>

      <!-- 历史记录 -->
      <view class="history-list" v-else>
        <view
          class="history-group"
          v-for="group in historyList"
          :key="group.date"
        >
          <!-- 日期标题 -->
          <view class="group-title">
            <text class="title-text">{{ group.dateText }}</text>
            <text class="title-count">{{ group.items.length }}条</text>
          </view>

          <!-- 历史项列表 -->
          <view class="history-items">
            <view
              class="history-item"
              v-for="item in group.items"
              :key="item.id"
              @click="viewDishDetail(item)"
            >
              <!-- 菜品图片 -->
              <image class="dish-image" :src="item.image" mode="aspectFill" />

              <!-- 菜品信息 -->
              <view class="dish-info">
                <text class="dish-name">{{ item.name }}</text>
                <view class="dish-meta">
                  <text class="dish-price">¥{{ item.price }}</text>
                  <text class="view-time">{{ item.viewTime }}</text>
                </view>

                <!-- 商家信息 -->
                <view class="merchant-info" @click.stop="toMerchant(item.merchantId)">
                  <text class="merchant-name">{{ item.merchantName }}</text>
                  <text class="merchant-arrow">→</text>
                </view>
              </view>

              <!-- 删除按钮 -->
              <view class="delete-btn" @click.stop="deleteHistory(item)">
                <text>×</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import Empty from '@/components/common/Empty.vue'
import { historyApi } from '@/api'

// 用户信息store
const userStore = useUserStore()

// 历史列表
const historyList = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)

// 是否有历史记录
const hasHistory = computed(() => {
  return historyList.value.length > 0
})

/**
 * 格式化历史数据
 */
const formatHistoryData = (list) => {
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  const groups = {
    today: { date: today, dateText: '今天', items: [] },
    yesterday: { date: yesterday, dateText: '昨天', items: [] },
    earlier: { date: null, dateText: '更早', items: [] }
  }

  list.forEach(item => {
    const viewDate = new Date(item.viewTime)
    const viewDateOnly = new Date(viewDate.getFullYear(), viewDate.getMonth(), viewDate.getDate())

    if (viewDateOnly.getTime() === groups.today.date.getTime()) {
      groups.today.items.push(item)
    } else if (viewDateOnly.getTime() === groups.yesterday.date.getTime()) {
      groups.yesterday.items.push(item)
    } else {
      groups.earlier.items.push(item)
    }
  })

  const result = []
  if (groups.today.items.length > 0) {
    result.push(groups.today)
  }
  if (groups.yesterday.items.length > 0) {
    result.push(groups.yesterday)
  }
  if (groups.earlier.items.length > 0) {
    result.push(groups.earlier)
  }

  return result
}

/**
 * 加载浏览历史
 */
const loadHistory = async (showLoading = true) => {
  if (!userStore.isLogin) {
    return
  }

  if (showLoading) {
    loading.value = true
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const res = await historyApi.getDishHistory({
      userId,
      page: 1,
      size: 100
    })

    const list = res.list || res.data?.list || []
    historyList.value = formatHistoryData(list)
  } catch (error) {
    console.error('加载浏览历史失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  await loadHistory(false)
  refreshing.value = false
}

/**
 * 查看菜品详情
 */
const viewDishDetail = (item) => {
  // 记录浏览
  if (userStore.isLogin) {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    historyApi.add({
      userId,
      targetType: 'dish',
      targetId: item.id
    }).catch(() => {})
  }

  uni.navigateTo({
    url: `/pages/dish/detail/index?id=${item.id}`
  })
}

/**
 * 跳转商家
 */
const toMerchant = (merchantId) => {
  uni.navigateTo({
    url: `/pages/merchant/detail/index?id=${merchantId}`
  })
}

/**
 * 删除单条历史
 */
const deleteHistory = async (item) => {
  if (!userStore.isLogin) {
    return
  }

  uni.showModal({
    title: '删除记录',
    content: '确定要删除这条浏览记录吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          const userId = userStore.userInfo?.userId || userStore.userInfo?.id
          await historyApi.delete(item.id, { userId })

          // 从列表中移除
          for (const group of historyList.value) {
            const index = group.items.findIndex(i => i.id === item.id)
            if (index > -1) {
              group.items.splice(index, 1)
              break
            }
          }

          // 移除空组
          historyList.value = historyList.value.filter(group => group.items.length > 0)

          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
        } catch (error) {
          console.error('删除失败:', error)
          uni.showToast({
            title: '删除失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 清空历史
 */
const clearHistory = () => {
  if (!userStore.isLogin) {
    return
  }

  uni.showModal({
    title: '清空历史',
    content: '确定要清空所有浏览记录吗？',
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          const userId = userStore.userInfo?.userId || userStore.userInfo?.id
          await historyApi.clear({ userId, type: 'dish' })

          historyList.value = []

          uni.showToast({
            title: '已清空',
            icon: 'success'
          })
        } catch (error) {
          console.error('清空失败:', error)
          uni.showToast({
            title: '清空失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 去首页
 */
const goToHome = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}

// 组件挂载
onMounted(() => {
  loadHistory()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.history-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

/* 顶部操作栏 */
.top-bar {
  @include flex-between;
  background-color: $bg-color-white;
  padding: $spacing-md $spacing-lg;
  box-shadow: $box-shadow-sm;
}

.title-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.clear-btn {
  padding: $spacing-sm $spacing-md;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-sm;
  border-radius: $border-radius-round;
  font-weight: $font-weight-medium;

  &:active {
    opacity: 0.6;
  }
}

/* 滚动容器 */
.scroll-container {
  height: calc(100vh - 100rpx);
}

/* 空状态 */
.empty-state {
  padding: 120rpx $spacing-lg;
}

/* 历史列表 */
.history-list {
  padding-bottom: $spacing-md;
}

.history-group {
  margin-bottom: $spacing-md;
}

.group-title {
  @include flex-between;
  align-items: center;
  padding: $spacing-md $spacing-lg;
  background-color: $bg-color-white;
  border-bottom: 1rpx solid $border-color-lighter;
}

.title-text {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.title-count {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 历史项列表 */
.history-items {
  background-color: $bg-color-white;
}

.history-item {
  position: relative;
  @include flex-center;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color-lighter;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: $bg-color-base;
    margin: 0 (-$spacing-md);
    padding-left: $spacing-md;
    padding-right: $spacing-md;
  }
}

.dish-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
  min-width: 0;
}

.dish-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  @include text-ellipsis;
}

.dish-meta {
  @include flex-between;
  width: 100%;
}

.dish-price {
  font-size: $font-size-lg;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

.view-time {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.merchant-info {
  @include flex-center;
  gap: $spacing-xs;
  padding: $spacing-xs 0;
}

.merchant-name {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.merchant-arrow {
  font-size: $font-size-sm;
  color: $text-color-placeholder;
}

.delete-btn {
  position: absolute;
  top: 50%;
  right: $spacing-md;
  transform: translateY(-50%);
  width: 48rpx;
  height: 48rpx;
  @include flex-center;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 50%;
  font-size: 48rpx;
  color: $text-color-placeholder;

  &:active {
    background-color: rgba(239, 83, 80, 0.1);
    color: $danger-color;
  }
}
</style>
