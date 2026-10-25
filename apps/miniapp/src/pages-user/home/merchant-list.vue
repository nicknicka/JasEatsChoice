<template>
  <view class="merchant-list-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input" @tap="toSearch">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <text class="placeholder">搜索商家</text>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item" @tap="showSortMenu">
        <text>{{ sortText }}</text>
        <uni-icons type="down" size="14"></uni-icons>
      </view>
      <view class="filter-item" @tap="showCategoryMenu">
        <text>{{ categoryText }}</text>
        <uni-icons type="down" size="14"></uni-icons>
      </view>
      <view class="filter-item" @tap="toggleFilter">
        <text>筛选</text>
        <uni-icons type="filter" size="14"></uni-icons>
      </view>
    </view>

    <!-- 商家列表 -->
    <scroll-view 
      class="merchant-list" 
      scroll-y 
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view 
        class="merchant-item" 
        v-for="merchant in merchantList" 
        :key="merchant.id"
        @tap="toMerchantDetail(merchant.id)"
      >
        <!-- 商家图片 -->
        <image class="merchant-image" :src="merchant.image" mode="aspectFill"></image>
        
        <!-- 商家信息 -->
        <view class="merchant-info">
          <view class="merchant-header">
            <text class="merchant-name">{{ merchant.name }}</text>
            <view class="merchant-tags" v-if="merchant.tags && merchant.tags.length">
              <text class="tag" v-for="tag in merchant.tags" :key="tag">{{ tag }}</text>
            </view>
          </view>
          
          <view class="merchant-rating">
            <uni-rate :value="merchant.rating" size="12" readonly></uni-rate>
            <text class="rating-text">{{ merchant.rating }}</text>
            <text class="sales">月售{{ merchant.monthlySales }}</text>
          </view>
          
          <view class="merchant-desc">
            <text class="delivery-time">{{ merchant.deliveryTime }}</text>
            <text class="distance">{{ merchant.distance }}</text>
            <text class="delivery-price">起送¥{{ merchant.minPrice }}</text>
          </view>
          
          <!-- 优惠券 -->
          <view class="coupons" v-if="merchant.coupons && merchant.coupons.length">
            <text 
              class="coupon-item" 
              v-for="coupon in merchant.coupons.slice(0, 2)" 
              :key="coupon.id"
            >
              {{ coupon.name }}
            </text>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="merchantList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else @tap="loadMore">上拉加载更多</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="merchantList.length === 0 && !loading">
        <empty />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { toSearch } from '@/utils/router'
import { merchantApi } from '@/api'

const merchantList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 10

// 筛选条件
const sortType = ref('distance')
const category = ref('all')

const sortText = computed(() => {
  const sortMap = {
    distance: '距离优先',
    rating: '评分优先',
    sales: '销量优先'
  }
  return sortMap[sortType.value]
})

const categoryText = computed(() => {
  const categoryMap = {
    all: '全部分类',
    chinese: '中餐',
    western: '西餐',
    snack: '小吃快餐',
    drink: '饮品'
  }
  return categoryMap[category.value]
})

onMounted(() => {
  loadMerchants()
})

const loadMerchants = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // 调用后端API获取商家列表
    const params = {
      page: page.value,
      size: pageSize,
      sort: sortType.value
    }

    // 如果有分类筛选，添加分类参数
    if (category.value && category.value !== 'all') {
      params.category = category.value
    }

    const res = await merchantApi.getList(params)

    // 处理返回的数据
    let merchants = []
    if (Array.isArray(res)) {
      merchants = res
    } else if (res && res.list) {
      merchants = res.list
    } else if (res && res.records) {
      merchants = res.records
    }

    // 数据映射：将后端返回的字段映射到前端需要的字段
    const mappedMerchants = merchants.map(merchant => ({
      id: merchant.merchantId || merchant.id,
      name: merchant.merchantName || merchant.name,
      image: merchant.avatar || merchant.image || merchant.coverImage,
      rating: merchant.rating || merchant.score || 5.0,
      monthlySales: merchant.monthlySales || merchant.sales || 0,
      deliveryTime: merchant.deliveryTime ? `${merchant.deliveryTime}分钟` : '30分钟',
      distance: merchant.distance ? `${merchant.distance}km` : '1.0km',
      minPrice: merchant.minPrice || merchant.minOrderAmount || 0,
      tags: merchant.tags || [],
      coupons: merchant.coupons || []
    }))

    if (isRefresh) {
      merchantList.value = mappedMerchants
    } else {
      merchantList.value = [...merchantList.value, ...mappedMerchants]
    }

    if (mappedMerchants.length < pageSize) {
      noMore.value = true
    }

    loading.value = false
    refreshing.value = false
  } catch (error) {
    console.error('加载商家列表失败:', error)

    // 如果API调用失败，显示错误提示
    uni.showToast({
      title: error.message || '加载失败，请重试',
      icon: 'none'
    })

    loading.value = false
    refreshing.value = false
  }
}

const generateMockData = () => {
  const merchants = []
  const count = Math.floor(Math.random() * 5) + 5
  
  for (let i = 0; i < count; i++) {
    merchants.push({
      id: page.value * 10 + i,
      name: `美味餐厅${page.value}-${i}`,
      image: 'https://picsum.photos/200/200?random=' + (page.value * 10 + i),
      rating: (Math.random() * 2 + 3).toFixed(1),
      monthlySales: Math.floor(Math.random() * 1000) + 100,
      deliveryTime: `${Math.floor(Math.random() * 20) + 20}分钟`,
      distance: `${(Math.random() * 5 + 0.5).toFixed(1)}km`,
      minPrice: Math.floor(Math.random() * 20) + 10,
      tags: i % 3 === 0 ? ['新店', '优惠'] : ['品牌'],
      coupons: i % 2 === 0 ? [{ id: 1, name: '满30减5' }] : []
    })
  }
  
  return merchants
}

const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadMerchants()
  }
}

const onRefresh = () => {
  refreshing.value = true
  loadMerchants(true)
}

const toMerchantDetail = (id) => {
  uni.navigateTo({
    url: `/pages-user/merchant/detail/index?id=${id}`
  })
}

const showSortMenu = () => {
  uni.showActionSheet({
    itemList: ['距离优先', '评分优先', '销量优先'],
    success: (res) => {
      const sortMap = ['distance', 'rating', 'sales']
      sortType.value = sortMap[res.tapIndex]
      loadMerchants(true)
    }
  })
}

const showCategoryMenu = () => {
  uni.showActionSheet({
    itemList: ['全部分类', '中餐', '西餐', '小吃快餐', '饮品'],
    success: (res) => {
      const categoryMap = ['all', 'chinese', 'western', 'snack', 'drink']
      category.value = categoryMap[res.tapIndex]
      loadMerchants(true)
    }
  })
}

const toggleFilter = () => {
  uni.showToast({
    title: '筛选功能开发中',
    icon: 'none'
  })
}
</script>

<style lang="scss" scoped>
.merchant-list-container {
  width: 100%;
  height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

.search-bar {
  background: #fff;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.search-input {
  background: #F5F5F5;
  border-radius: 40rpx;
  height: 70rpx;
  display: flex;
  align-items: center;
  padding: 0 30rpx;
  gap: 15rpx;
}

.placeholder {
  color: #999;
  font-size: 28rpx;
}

.filter-bar {
  background: #fff;
  display: flex;
  justify-content: space-around;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 28rpx;
  color: #333;
}

.merchant-list {
  flex: 1;
  padding: 20rpx 30rpx;
}

.merchant-item {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  padding: 24rpx;
  display: flex;
  gap: 24rpx;
}

.merchant-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.merchant-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.merchant-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.merchant-tags {
  display: flex;
  gap: 8rpx;
}

.tag {
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  color: #fff;
  font-size: 20rpx;
  border-radius: 4rpx;
}

.merchant-rating {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.rating-text {
  font-size: 24rpx;
  color: #FF6B35;
  font-weight: bold;
}

.sales {
  font-size: 24rpx;
  color: #999;
}

.merchant-desc {
  display: flex;
  gap: 20rpx;
  font-size: 24rpx;
  color: #666;
}

.coupons {
  display: flex;
  gap: 12rpx;
}

.coupon-item {
  padding: 8rpx 16rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 22rpx;
  border-radius: 4rpx;
  border: 1rpx solid rgba(255, 107, 53, 0.3);
}

.load-status {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

.empty-state {
  padding-top: 200rpx;
}
</style>
