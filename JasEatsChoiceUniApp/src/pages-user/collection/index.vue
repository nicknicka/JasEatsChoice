<template>
  <view class="collection-container">
    <!-- 分类Tab -->
    <view class="filter-tabs">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'dish' }"
        @click="changeTab('dish')"
      >
        <text class="tab-text">菜品</text>
        <view class="tab-badge" v-if="dishCount > 0">{{ dishCount }}</view>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'merchant' }"
        @click="changeTab('merchant')"
      >
        <text class="tab-text">商家</text>
        <view class="tab-badge" v-if="merchantCount > 0">{{ merchantCount }}</view>
      </view>
    </view>

    <!-- 收藏列表 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 菜品收藏 -->
      <view class="collection-list" v-if="activeTab === 'dish'">
        <!-- 空状态 -->
        <view class="empty-state" v-if="dishList.length === 0 && !loading">
          <Empty
            icon="⭐"
            text="还没有收藏的菜品"
            description="去首页看看心仪的美食吧"
            buttonText="去逛逛"
            @button-click="goToHome"
          />
        </view>

        <!-- 菜品列表 -->
        <view class="dish-grid" v-else>
          <view
            class="dish-item"
            v-for="dish in dishList"
            :key="dish.id"
            @click="viewDishDetail(dish)"
          >
            <!-- 收藏按钮 -->
            <view class="favorite-btn" @click.stop="toggleDishFavorite(dish)">
              <text class="favorite-icon">⭐</text>
            </view>

            <!-- 菜品图片 -->
            <image class="dish-image" :src="dish.image" mode="aspectFill" />

            <!-- 菜品信息 -->
            <view class="dish-info">
              <text class="dish-name">{{ dish.name }}</text>
              <view class="dish-tags" v-if="dish.tags && dish.tags.length">
                <text
                  class="tag-item"
                  v-for="tag in dish.tags.slice(0, 2)"
                  :key="tag"
                >{{ tag }}</text>
              </view>
              <view class="dish-meta">
                <text class="dish-price">¥{{ dish.price }}</text>
                <text class="dish-sales">月售{{ dish.monthlySales }}</text>
              </view>
            </view>

            <!-- 商家信息 -->
            <view class="dish-merchant" @click.stop="toMerchant(dish.merchantId)">
              <text class="merchant-name">{{ dish.merchantName }}</text>
              <text class="merchant-arrow">→</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 商家收藏 -->
      <view class="collection-list" v-if="activeTab === 'merchant'">
        <!-- 空状态 -->
        <view class="empty-state" v-if="merchantList.length === 0 && !loading">
          <Empty
            icon="🏪"
            text="还没有收藏的商家"
            description="收藏喜欢的商家，第一时间获取优惠信息"
            buttonText="去发现"
            @button-click="goToHome"
          />
        </view>

        <!-- 商家列表 -->
        <view class="merchant-list" v-else>
          <view
            class="merchant-item"
            v-for="merchant in merchantList"
            :key="merchant.id"
            @click="viewMerchantDetail(merchant)"
          >
            <!-- 商家图片 -->
            <image class="merchant-image" :src="merchant.image" mode="aspectFill" />

            <!-- 商家信息 -->
            <view class="merchant-info">
              <text class="merchant-name">{{ merchant.name }}</text>
              <view class="merchant-rating">
                <text class="rating-star">⭐ {{ merchant.rating }}</text>
                <text class="rating-count">({{ merchant.reviewCount }}条评价)</text>
              </view>
              <view class="merchant-tags" v-if="merchant.tags && merchant.tags.length">
                <text
                  class="tag-item"
                  v-for="tag in merchant.tags.slice(0, 3)"
                  :key="tag"
                >{{ tag }}</text>
              </view>
              <text class="merchant-desc">{{ merchant.description }}</text>
            </view>

            <!-- 取消收藏按钮 -->
            <view class="action-btn" @click.stop="toggleMerchantFavorite(merchant)">
              <text class="action-text">{{ merchant.isFavorite ? '已收藏' : '收藏' }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-more" v-if="hasMore && (dishList.length > 0 || merchantList.length > 0)">
        <view class="load-text" v-if="loading">加载中...</view>
        <view class="load-text" v-else>上拉加载更多</view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import Empty from '@/components/common/Empty.vue'
import { favoriteApi } from '@/api'

// 用户信息store
const userStore = useUserStore()

// 当前激活的Tab
const activeTab = ref('dish')

// 菜品列表
const dishList = ref([])
const dishCount = ref(0)

// 商家列表
const merchantList = ref([])
const merchantCount = ref(0)

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)

// 分页参数
const page = ref(1)
const pageSize = ref(10)

/**
 * 切换Tab
 */
const changeTab = (tab) => {
  activeTab.value = tab
  page.value = 1
  dishList.value = []
  merchantList.value = []
  loadData()
}

/**
 * 加载数据
 */
const loadData = async (showLoading = true) => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  if (showLoading) {
    loading.value = true
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    if (activeTab.value === 'dish') {
      // 加载菜品收藏
      const res = await favoriteApi.getDishList({
        userId,
        page: page.value,
        size: pageSize.value
      })

      const list = res.list || res.data?.list || []
      if (page.value === 1) {
        dishList.value = list
        dishCount.value = res.total || res.data?.total || 0
      } else {
        dishList.value.push(...list)
      }

      hasMore.value = list.length >= pageSize.value
    } else {
      // 加载商家收藏
      const res = await favoriteApi.getMerchantList({
        userId,
        page: page.value,
        size: pageSize.value
      })

      const list = res.list || res.data?.list || []
      if (page.value === 1) {
        merchantList.value = list
        merchantCount.value = res.total || res.data?.total || 0
      } else {
        merchantList.value.push(...list)
      }

      hasMore.value = list.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载收藏数据失败:', error)
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
  page.value = 1
  await loadData(false)
  refreshing.value = false
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  loadData()
}

/**
 * 查看菜品详情
 */
const viewDishDetail = (dish) => {
  uni.navigateTo({
    url: `/pages/dish/detail/index?id=${dish.id}`
  })
}

/**
 * 查看商家详情
 */
const viewMerchantDetail = (merchant) => {
  uni.navigateTo({
    url: `/pages/merchant/detail/index?id=${merchant.id}`
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
 * 切换菜品收藏
 */
const toggleDishFavorite = async (dish) => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    if (dish.isFavorite) {
      await favoriteApi.removeDish(dish.id, { userId })
      // 从列表中移除
      const index = dishList.value.findIndex(item => item.id === dish.id)
      if (index > -1) {
        dishList.value.splice(index, 1)
      }
      dishCount.value--
      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
    } else {
      await favoriteApi.addDish({
        userId,
        dishId: dish.id
      })
      dish.isFavorite = true
      uni.showToast({
        title: '收藏成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('操作失败:', error)
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    })
  }
}

/**
 * 切换商家收藏
 */
const toggleMerchantFavorite = async (merchant) => {
  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    return
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    if (merchant.isFavorite) {
      await favoriteApi.removeMerchant(merchant.id, { userId })
      merchant.isFavorite = false
      merchantCount.value--
      uni.showToast({
        title: '已取消收藏',
        icon: 'success'
      })
    } else {
      await favoriteApi.addMerchant({
        userId,
        merchantId: merchant.id
      })
      merchant.isFavorite = true
      uni.showToast({
        title: '收藏成功',
        icon: 'success'
      })
    }
  } catch (error) {
    console.error('操作失败:', error)
    uni.showToast({
      title: '操作失败，请重试',
      icon: 'none'
    })
  }
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
  loadData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.collection-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

/* 分类Tab */
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
  height: calc(100vh - 100rpx);
}

/* 空状态 */
.empty-state {
  padding: 120rpx $spacing-lg;
}

/* 菜品列表 */
.dish-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
  padding: $spacing-md;
}

.dish-item {
  position: relative;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  overflow: hidden;
  box-shadow: $box-shadow-sm;

  &:active {
    transform: scale(0.98);
  }
}

.favorite-btn {
  position: absolute;
  top: $spacing-sm;
  right: $spacing-sm;
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  z-index: 1;

  &:active {
    transform: scale(0.9);
  }
}

.favorite-icon {
  font-size: $font-size-xl;
}

.dish-image {
  width: 100%;
  height: 240rpx;
}

.dish-info {
  padding: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
}

.dish-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  @include text-ellipsis;
}

.dish-tags {
  @include flex-center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}

.tag-item {
  padding: 4rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-round;
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

.dish-sales {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.dish-merchant {
  @include flex-center;
  justify-content: space-between;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-color-base;
}

.merchant-name {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.merchant-arrow {
  font-size: $font-size-sm;
  color: $text-color-placeholder;
}

/* 商家列表 */
.merchant-list {
  padding: $spacing-md;
  @include flex-center-column;
  gap: $spacing-md;
}

.merchant-item {
  @include flex-center;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: $box-shadow-sm;

  &:active {
    transform: scale(0.98);
  }
}

.merchant-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  margin-left: $spacing-md;
  @include flex-center-column;
  align-items: flex-start;
  gap: $spacing-xs;
  min-width: 0;
}

.merchant-name {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.merchant-rating {
  @include flex-center;
  gap: $spacing-sm;
}

.rating-star {
  font-size: $font-size-sm;
  color: $warning-color;
  font-weight: $font-weight-medium;
}

.rating-count {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

.merchant-tags {
  @include flex-center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}

.merchant-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  @include text-ellipsis-multi(2);
}

.action-btn {
  padding: $spacing-sm $spacing-md;
  background-color: rgba(255, 107, 53, 0.1);
  border-radius: $border-radius-round;
  flex-shrink: 0;
}

.action-text {
  font-size: $font-size-sm;
  color: $primary-color;
  font-weight: $font-weight-medium;
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
