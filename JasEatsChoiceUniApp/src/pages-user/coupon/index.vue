<template>
  <view class="coupon-container">
    <!-- 优惠券Tab -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'available' }"
        @click="changeTab('available')"
      >
        <text class="tab-text">可用</text>
        <view class="tab-badge" v-if="availableCount > 0">{{ availableCount }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'used' }"
        @click="changeTab('used')"
      >
        <text class="tab-text">已使用</text>
        <view class="tab-badge" v-if="usedCount > 0">{{ usedCount }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'expired' }"
        @click="changeTab('expired')"
      >
        <text class="tab-text">已过期</text>
        <view class="tab-badge" v-if="expiredCount > 0">{{ expiredCount }}</view>
      </view>
    </view>

    <!-- 优惠券列表 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view class="empty-state" v-if="couponList.length === 0 && !loading">
        <Empty
          icon="🎫"
          :text="emptyText"
          :description="emptyDesc"
          buttonText="去领券"
          @button-click="goToCoupons"
        />
      </view>

      <!-- 优惠券列表 -->
      <view class="coupon-list" v-else>
        <view
          class="coupon-item"
          v-for="coupon in couponList"
          :key="coupon.id"
          :class="{ disabled: coupon.status !== 'available' }"
        >
          <!-- 左侧金额区 -->
          <view class="coupon-left" :class="`coupon-${coupon.type}`">
            <text class="coupon-amount">¥{{ coupon.amount }}</text>
            <text class="coupon-condition">{{ coupon.condition }}</text>
          </view>

          <!-- 分割线 -->
          <view class="coupon-divider">
            <view class="divider-circle top"></view>
            <view class="divider-dots"></view>
            <view class="divider-circle bottom"></view>
          </view>

          <!-- 右侧信息区 -->
          <view class="coupon-right">
            <text class="coupon-name">{{ coupon.name }}</text>
            <text class="coupon-desc">{{ coupon.description }}</text>
            <view class="coupon-time">
              <text class="time-icon">⏰</text>
              <text class="time-text">{{ coupon.validPeriod }}</text>
            </view>

            <!-- 状态标签 -->
            <view class="coupon-status" v-if="coupon.status !== 'available'">
              <text class="status-text">{{ coupon.statusText }}</text>
            </view>

            <!-- 操作按钮 -->
            <view class="coupon-actions" v-else>
              <button
                class="action-btn"
                @click="useCoupon(coupon)"
                v-if="coupon.canUse"
              >
                立即使用
              </button>
              <button
                class="action-btn outline"
                @click="shareCoupon(coupon)"
              >
                分享
              </button>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部领取中心按钮 -->
    <view class="bottom-bar" v-if="activeTab === 'available'">
      <button class="receive-center-btn" @click="goToCoupons">
        <text class="btn-icon">🎫</text>
        <text>领券中心</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { couponApi } from '@/api'
import Empty from '@/components/common/Empty.vue'

// Store
const userStore = useUserStore()

// 当前激活的Tab
const activeTab = ref('available')

// 优惠券列表
const couponList = ref([])

// 数量统计
const availableCount = ref(0)
const usedCount = ref(0)
const expiredCount = ref(0)

// 加载状态
const loading = ref(false)
const refreshing = ref(false)

// 空状态文字
const emptyText = computed(() => {
  const texts = {
    available: '还没有可用优惠券',
    used: '还没有使用记录',
    expired: '暂无过期优惠券'
  }
  return texts[activeTab.value]
})

const emptyDesc = computed(() => {
  const descs = {
    available: '快去领券中心看看吧',
    used: '使用过的优惠券会显示在这里',
    expired: '过期的优惠券会显示在这里'
  }
  return descs[activeTab.value]
})

/**
 * 切换Tab
 */
const changeTab = (tab) => {
  activeTab.value = tab
  loadCoupons()
}

/**
 * 加载优惠券列表
 */
const loadCoupons = async () => {
  loading.value = true

  try {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/index'
        })
      }, 1500)
      loading.value = false
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 调用后端API获取用户优惠券列表
    const res = await couponApi.getUserCoupons({
      userId,
      status: activeTab.value === 'available' ? 'unused' : activeTab.value
    })

    // 数据映射
    if (Array.isArray(res)) {
      couponList.value = res.map(coupon => ({
        id: coupon.userCouponId || coupon.id,
        name: coupon.name,
        description: coupon.description || '',
        amount: coupon.amount,
        condition: coupon.condition || coupon.minAmount ? `满${coupon.minAmount}元可用` : '',
        type: coupon.type || 'discount',
        validPeriod: `${coupon.startTime} - ${coupon.endTime}`,
        status: activeTab.value,
        statusText: getStatusText(activeTab.value),
        canUse: coupon.status === 'available' || coupon.status === 'unused'
      }))
    } else {
      couponList.value = []
    }

    // 更新数量
    await updateCounts()
  } catch (error) {
    console.error('加载优惠券失败:', error)
    uni.showToast({
      title: error.message || '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    'available': '可用',
    'unused': '可用',
    'used': '已使用',
    'expired': '已过期'
  }
  return statusMap[status] || status
}

/**
 * 更新数量
 */
const updateCounts = async () => {
  if (!userStore.isLogin) {
    return
  }

  const userId = userStore.userInfo?.userId || userStore.userInfo?.id

  try {
    const res = await couponApi.getUserCoupons({
      userId,
      status: 'unused'
    })
    availableCount.value = Array.isArray(res) ? res.length : 0
  } catch (error) {
    console.error('获取可用数量失败:', error)
  }

  try {
    const res = await couponApi.getUserCoupons({
      userId,
      status: 'used'
    })
    usedCount.value = Array.isArray(res) ? res.length : 0
  } catch (error) {
    console.error('获取已用数量失败:', error)
  }

  try {
    const res = await couponApi.getUserCoupons({
      userId,
      status: 'expired'
    })
    expiredCount.value = Array.isArray(res) ? res.length : 0
  } catch (error) {
    console.error('获取过期数量失败:', error)
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  await loadCoupons()
  refreshing.value = false
}

/**
 * 使用优惠券 - U-028: 跳转到可以使用优惠券的页面
 */
const useCoupon = (coupon) => {
  // U-028: 跳转到可以使用优惠券的页面（首页或购物车）
  // 优先跳转到购物车，如果有可用商品的话
  uni.switchTab({
    url: '/pages-user/cart/index',
    success: () => {
      console.log('跳转到购物车成功')
    },
    fail: () => {
      // 如果购物车跳转失败，跳转到首页
      uni.switchTab({
        url: '/pages-user/home/index/index',
        success: () => {
          console.log('跳转到首页成功')
        },
        fail: () => {
          uni.showToast({
            title: '请选择要购买的菜品',
            icon: 'none'
          })
        }
      })
    }
  })
}

/**
 * 分享优惠券
 */
const shareCoupon = (coupon) => {
  uni.showShareMenu({
    withShareTicket: true
  })

  uni.showToast({
    title: '点击右上角分享给好友',
    icon: 'none'
  })
}

/**
 * 去领券中心
 */
const goToCoupons = () => {
  uni.navigateTo({
    url: '/pages/coupon/center/index'
  })
}

// 组件挂载
onMounted(() => {
  loadCoupons()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.coupon-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

/* 优惠券Tab */
.filter-tabs {
  @include flex-center;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: $box-shadow-sm;
  position: sticky;
  top: 0;
  z-index: $z-index-sticky;
}

.tab-item {
  @include flex-center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-xl;
  position: relative;
  transition: all 0.3s;

  &.active {
    .tab-text {
      color: $primary-color;
      font-weight: $font-weight-bold;
    }
  }

  &:active {
    transform: scale(0.95);
  }
}

.tab-text {
  font-size: $font-size-base;
  color: $text-color-regular;
}

.tab-badge {
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
  height: calc(100vh - 200rpx);
}

/* 空状态 */
.empty-state {
  padding: 120rpx $spacing-lg;
}

/* 优惠券列表 */
.coupon-list {
  padding: $spacing-md;
  @include flex-center-column;
  gap: $spacing-md;
}

.coupon-item {
  @include flex-center;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  overflow: hidden;
  box-shadow: $box-shadow-sm;

  &.disabled {
    opacity: 0.6;
  }
}

/* 左侧金额区 */
.coupon-left {
  width: 240rpx;
  padding: $spacing-lg 0;
  @include flex-center-column;
  justify-content: center;
  align-items: center;
  text-align: center;
  color: #fff;

  &.coupon-red {
    background: linear-gradient(135deg, #FF6B6B, #FF8E8E);
  }

  &.coupon-orange {
    background: linear-gradient(135deg, #FFB74D, #FFA726);
  }

  &.coupon-blue {
    background: linear-gradient(135deg, #64B5F6, #42A5F5);
  }

  &.coupon-green {
    background: linear-gradient(135deg, #81C784, #66BB6A);
  }
}

.coupon-amount {
  font-size: 48rpx;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-xs;
}

.coupon-condition {
  font-size: $font-size-sm;
  opacity: 0.9;
}

/* 分割线 */
.coupon-divider {
  position: relative;
  width: 2rpx;
  height: 180rpx;
  background: repeating-linear-gradient(
    to bottom,
    $border-color-base,
    $border-color-base 4rpx,
    transparent 4rpx
  );
  margin: 0 $spacing-md;
}

.divider-circle {
  position: absolute;
  left: -6rpx;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background-color: $bg-color-base;

  &.top {
    top: 0;
  }

  &.bottom {
    bottom: 0;
  }
}

.divider-dots {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* 右侧信息区 */
.coupon-right {
  flex: 1;
  padding: $spacing-md;
  @include flex-center-column;
  justify-content: space-between;
  align-items: flex-start;
  gap: $spacing-xs;
  min-width: 0;
}

.coupon-name {
  font-size: $font-size-base;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  width: 100%;
  @include text-ellipsis;
}

.coupon-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  line-height: $line-height-base;
  @include text-ellipsis-multiline(2);
}

.coupon-time {
  @include flex-center;
  gap: $spacing-xs;
  width: 100%;
}

.time-icon {
  font-size: $font-size-sm;
}

.time-text {
  flex: 1;
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

/* 状态标签 */
.coupon-status {
  padding: $spacing-xs $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-sm;
}

.status-text {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

/* 操作按钮 */
.coupon-actions {
  @include flex-center;
  gap: $spacing-sm;
  width: 100%;
}

.action-btn {
  flex: 1;
  height: 56rpx;
  @include flex-center;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;

  &.outline {
    background-color: transparent;
    color: $primary-color;
    border: 1rpx solid $primary-color;

    &:active {
      background-color: rgba(255, 107, 53, 0.05);
    }
  }

  &:not(.outline) {
    background: linear-gradient(135deg, $primary-color, #FF8F61);
    color: #fff;

    &:active {
      opacity: 0.8;
    }
  }
}

/* 底部按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
  @include safe-area-bottom;
}

.receive-center-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  gap: $spacing-sm;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);

  &:active {
    transform: scale(0.98);
  }

  .btn-icon {
    font-size: $font-size-xl;
  }
}
</style>
