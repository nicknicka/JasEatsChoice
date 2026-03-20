<template>
  <view class="index-container">
    <!-- 下拉刷新容器 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
      :lower-threshold="100"
    >
      <!-- 顶部区域：定位和天气 -->
      <view class="top-section">
        <WeatherLocation ref="weatherRef" />

        <!-- 搜索栏 -->
        <view class="search-bar" @click="toSearch">
          <view class="search-icon">🔍</view>
          <view class="search-input">搜索菜品、商家或食谱...</view>
        </view>
      </view>

      <!-- 轮播图 -->
      <view class="banner-section" v-if="banners.length > 0">
        <swiper
          class="banner-swiper"
          autoplay
          interval="3000"
          circular
          indicator-dots
          indicator-color="rgba(255,255,255,0.5)"
          indicator-active-color="#fff"
        >
          <swiper-item v-for="banner in banners" :key="banner.id" @click="handleBannerClick(banner)">
            <image class="banner-image" :src="banner.image" mode="aspectFill" />
          </swiper-item>
        </swiper>
      </view>

      <!-- 分类导航 -->
      <view class="category-section">
        <view class="section-header">
          <text class="section-title">美食分类</text>
          <text class="section-more" @click="toMoreCategories">更多 ›</text>
        </view>
        <scroll-view class="category-scroll" scroll-x show-scrollbar="false">
          <view class="category-list">
            <view
              class="category-item"
              v-for="category in categories"
              :key="category.id"
              @click="handleCategoryClick(category)"
            >
              <view class="category-icon">{{ category.icon }}</view>
              <view class="category-name">{{ category.name }}</view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 推荐商家 -->
      <view class="merchant-section" v-if="recommendMerchants.length > 0">
        <view class="section-header">
          <text class="section-title">推荐商家</text>
          <text class="section-more" @click="toMoreMerchants">更多 ›</text>
        </view>
        <scroll-view class="merchant-scroll" scroll-x show-scrollbar="false">
          <view class="merchant-list">
            <view
              class="merchant-card"
              v-for="merchant in recommendMerchants"
              :key="merchant.id"
              @click="toMerchantDetail(merchant.id)"
            >
              <image class="merchant-logo" :src="merchant.logo" mode="aspectFill" />
              <view class="merchant-info">
                <view class="merchant-name">{{ merchant.name }}</view>
                <view class="merchant-rating">
                  <text class="star">⭐</text>
                  <text>{{ merchant.rating }}</text>
                  <text class="sales">月售{{ merchant.monthlySales }}</text>
                </view>
                <view class="merchant-tags">
                  <text class="tag" v-for="tag in merchant.tags" :key="tag">{{ tag }}</text>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 推荐菜品 -->
      <view class="dish-section">
        <view class="section-header">
          <text class="section-title">为你推荐</text>
          <text class="section-refresh" @click="refreshRecommend">
            <text class="refresh-icon">🔄</text> 换一换
          </text>
        </view>
        <view class="dish-grid">
          <view
            class="dish-card"
            v-for="dish in recommendDishes"
            :key="dish.id"
            @click="toDishDetail(dish.id)"
          >
            <image class="dish-image" :src="dish.image" mode="aspectFill" />
            <view class="dish-info">
              <view class="dish-name">{{ dish.name }}</view>
              <view class="dish-desc">{{ dish.description }}</view>
              <view class="dish-bottom">
                <view class="dish-price">
                  <text class="price-symbol">¥</text>
                  <text class="price-value">{{ dish.price }}</text>
                </view>
                <view class="dish-sales">已售{{ dish.sales }}</view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="!noMore">
        <uni-load-more :status="loadMoreStatus" />
      </view>

      <!-- 没有更多 -->
      <view class="no-more" v-if="noMore">
        <text>~ 没有更多了 ~</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { toSearch, toMerchantDetail, toDishDetail } from '@/utils/router'
import { useLocationStore, useUserStore } from '@/store'
import { dishApi, merchantApi } from '@/api'
import WeatherLocation from '@/components/common/WeatherLocation.vue'

// Store
const locationStore = useLocationStore()
const userStore = useUserStore()

// 组件引用
const weatherRef = ref(null)

// 状态
const refreshing = ref(false)
const loadingMore = ref(false)
const noMore = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = 10

// 轮播图数据
const banners = ref([
  {
    id: 1,
    image: 'https://via.placeholder.com/750x300/FF6B35/FFFFFF?text=今日推荐',
    link: '',
    title: '今日推荐'
  },
  {
    id: 2,
    image: 'https://via.placeholder.com/750x300/667eea/FFFFFF?text=美食特惠',
    link: '',
    title: '美食特惠'
  },
  {
    id: 3,
    image: 'https://via.placeholder.com/750x300/52c41a/FFFFFF?text=新品上市',
    link: '',
    title: '新品上市'
  }
])

// 分类数据
const categories = ref([
  { id: 1, name: '中餐', icon: '🍚', code: 'chinese' },
  { id: 2, name: '西餐', icon: '🍔', code: 'western' },
  { id: 3, name: '日料', icon: '🍣', code: 'japanese' },
  { id: 4, name: '韩料', icon: '🍜', code: 'korean' },
  { id: 5, name: '快餐', icon: '🍟', code: 'fast_food' },
  { id: 6, name: '甜点', icon: '🍰', code: 'dessert' },
  { id: 7, name: '饮品', icon: '🥤', code: 'drink' },
  { id: 8, name: '小吃', icon: '🍢', code: 'snack' }
])

// 推荐商家数据
const recommendMerchants = ref([
  {
    id: 1,
    name: '老王家常菜',
    logo: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王',
    rating: 4.8,
    monthlySales: 999,
    tags: ['家常菜', '配送快', '好评多']
  },
  {
    id: 2,
    name: '李记川菜馆',
    logo: 'https://via.placeholder.com/200x200/667eea/FFFFFF?text=李记',
    rating: 4.6,
    monthlySales: 666,
    tags: ['川菜', '麻辣', '分量足']
  },
  {
    id: 3,
    name: '张胖子烧烤',
    logo: 'https://via.placeholder.com/200x200/52c41a/FFFFFF?text=张胖',
    rating: 4.7,
    monthlySales: 888,
    tags: ['烧烤', '夜宵', '啤酒']
  }
])

// 推荐菜品数据
const recommendDishes = ref([])

// 计算属性：加载更多状态
const loadMoreStatus = computed(() => {
  if (refreshing.value) return 'loading'
  if (noMore.value) return 'noMore'
  if (loadingMore.value) return 'loading'
  return 'more'
})

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  currentPage.value = 1
  noMore.value = false

  try {
    // 重新获取数据
    await Promise.all([
      loadBanners(),
      loadMerchants(),
      loadDishes(true)
    ])

    // 刷新位置和天气
    if (weatherRef.value) {
      weatherRef.value.getLocationAndWeather()
    }

    uni.showToast({
      title: '刷新成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('刷新失败:', error)
    uni.showToast({
      title: '刷新失败',
      icon: 'none'
    })
  } finally {
    refreshing.value = false
  }
}

/**
 * 上拉加载更多
 */
const onLoadMore = async () => {
  if (loadingMore.value || noMore.value) return

  loadingMore.value = true
  currentPage.value++

  try {
    await loadDishes(false)
  } catch (error) {
    console.error('加载更多失败:', error)
    currentPage.value--
  } finally {
    loadingMore.value = false
  }
}

/**
 * 加载轮播图 - U-022: 调用后端API
 */
const loadBanners = async () => {
  try {
    // U-022: 调用后端API获取轮播图
    const { bannerApi } = await import('@/api')
    const res = await bannerApi.getList({ position: 'home' })

    if (res && res.data && Array.isArray(res.data)) {
      banners.value = res.data.map(banner => ({
        id: banner.bannerId || banner.id,
        image: banner.imageUrl || banner.image,
        title: banner.title || '',
        type: banner.type || 'link', // link, dish, merchant, activity
        targetType: banner.targetType || '', // 跳转目标类型
        targetId: banner.targetId || '', // 跳转目标ID
        link: banner.link || '' // 外部链接
      }))
    }
  } catch (error) {
    console.error('加载轮播图失败:', error)
    // 使用空数组，不显示模拟数据
    banners.value = []
  }
}

/**
 * 加载推荐商家
 */
const loadMerchants = async () => {
  try {
    // 调用后端API获取附近商家
    const params = {
      limit: 10
    }

    // 如果有位置信息，添加位置参数
    if (locationStore.currentLocation) {
      params.latitude = locationStore.currentLocation.latitude
      params.longitude = locationStore.currentLocation.longitude
      params.radius = 5000 // 5公里范围
    }

    const res = await merchantApi.getNearby(params)

    // 数据映射
    if (Array.isArray(res)) {
      recommendMerchants.value = res.map(merchant => ({
        id: merchant.merchantId || merchant.id,
        name: merchant.merchantName || merchant.name,
        logo: merchant.avatar || merchant.logo || merchant.coverImage,
        rating: merchant.rating || merchant.score || 0,
        monthlySales: merchant.monthlySales || 0,
        tags: merchant.tags || []
      }))
    }
  } catch (error) {
    console.error('加载商家失败:', error)
    // 商家加载失败不影响页面显示
    recommendMerchants.value = []
  }
}

/**
 * 加载推荐菜品
 */
const loadDishes = async (refresh = false) => {
  try {
    // 调用后端API获取推荐菜品
    const params = {
      page: currentPage.value,
      size: pageSize
    }

    // 如果有用户ID，用于个性化推荐
    if (userStore.isLogin) {
      params.userId = userStore.userInfo?.userId || userStore.userInfo?.id
    }

    const res = await dishApi.getRecommend(params)

    // 数据映射
    let dishes = []
    if (Array.isArray(res)) {
      dishes = res
    } else if (res && res.list) {
      dishes = res.list
    } else if (res && res.records) {
      dishes = res.records
    }

    const mappedDishes = dishes.map(dish => ({
      id: dish.dishId || dish.id,
      name: dish.dishName || dish.name,
      description: dish.description || dish.desc || '',
      price: dish.price ? String(dish.price) : '0',
      sales: dish.monthlySales || dish.sales || 0,
      image: dish.image || dish.coverImage
    }))

    if (refresh) {
      recommendDishes.value = mappedDishes
    } else {
      recommendDishes.value.push(...mappedDishes)
    }

    if (mappedDishes.length < pageSize) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载推荐菜品失败:', error)
    // 使用空数组，不影响页面显示
    if (refresh) {
      recommendDishes.value = []
    }
  }
}
        description: '经典川菜，酸甜可口',
        price: '28',
        sales: 999,
        image: 'https://via.placeholder.com/300x300/FF6B35/FFFFFF?text=宫保鸡丁'
      },
      {
        id: Date.now() + 2,
        name: '鱼香肉丝',
        description: '传统川菜，下饭神器',
        price: '26',
        sales: 888,
        image: 'https://via.placeholder.com/300x300/667eea/FFFFFF?text=鱼香肉丝'
      },
      {
        id: Date.now() + 3,
        name: '回锅肉',
        description: '四川名菜，肥而不腻',
        price: '32',
        sales: 777,
        image: 'https://via.placeholder.com/300x300/52c41a/FFFFFF?text=回锅肉'
      },
      {
        id: Date.now() + 4,
        name: '麻婆豆腐',
        description: '麻辣鲜香，嫩滑爽口',
        price: '18',
        sales: 666,
        image: 'https://via.placeholder.com/300x300/faad14/FFFFFF?text=麻婆豆腐'
      }
    ]

    if (refresh) {
      recommendDishes.value = mockDishes
    } else {
      recommendDishes.value.push(...mockDishes)
    }

    // 模拟没有更多数据
    if (currentPage.value >= 3) {
      noMore.value = true
    }
  } catch (error) {
    console.error('加载菜品失败:', error)
  }
}

/**
 * 刷新推荐
 */
const refreshRecommend = () => {
  currentPage.value = 1
  noMore.value = false
  loadDishes(true)
}

/**
 * 点击轮播图 - U-023: 根据banner类型跳转
 */
const handleBannerClick = (banner) => {
  console.log('点击banner:', banner)

  // U-023: 根据banner类型进行不同的跳转
  if (!banner) return

  try {
    switch (banner.type) {
      case 'dish':
        // 跳转到菜品详情
        if (banner.targetId) {
          uni.navigateTo({
            url: `/pages-user/dish/detail/index?id=${banner.targetId}`
          })
        }
        break

      case 'merchant':
        // 跳转到商家详情
        if (banner.targetId) {
          uni.navigateTo({
            url: `/pages-user/merchant/detail/index?id=${banner.targetId}`
          })
        }
        break

      case 'activity':
        // 跳转到活动页面（如果有）
        if (banner.targetId) {
          uni.navigateTo({
            url: `/pages-user/activity/detail/index?id=${banner.targetId}`
          })
        }
        break

      case 'link':
        // 外部链接，使用webview打开
        if (banner.link) {
          uni.navigateTo({
            url: `/pages/common/webview/index?url=${encodeURIComponent(banner.link)}`
          })
        }
        break

      case 'recipe':
        // 跳转到食谱详情
        if (banner.targetId) {
          uni.navigateTo({
            url: `/pages-user/recipe/detail/index?id=${banner.targetId}`
          })
        }
        break

      default:
        // 默认不做任何操作或提示
        console.log('未知的banner类型:', banner.type)
    }
  } catch (error) {
    console.error('Banner跳转失败:', error)
    uni.showToast({
      title: '页面跳转失败',
      icon: 'none'
    })
  }
}

/**
 * 点击分类 - U-024: 跳转到分类列表页
 */
const handleCategoryClick = (category) => {
  if (!category) return

  // U-024: 跳转到分类菜品列表页
  uni.navigateTo({
    url: `/pages-user/dish/list/index?category=${encodeURIComponent(category.code || category.name)}&name=${encodeURIComponent(category.name)}`,
    success: () => {
      console.log('跳转到分类列表成功:', category.name)
    },
    fail: (err) => {
      console.error('跳转分类列表失败:', err)
      uni.showToast({
        title: '打开分类页面失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 查看更多分类 - U-025: 跳转到分类页面
 */
const toMoreCategories = () => {
  // U-025: 跳转到全部分类页面
  uni.navigateTo({
    url: '/pages-user/category/index',
    success: () => {
      console.log('跳转到分类页面成功')
    },
    fail: (err) => {
      console.error('跳转分类页面失败:', err)
      uni.showToast({
        title: '打开分类页面失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 查看更多商家
 */
const toMoreMerchants = () => {
  uni.navigateTo({
    url: '/pages-user/home/merchant-list'
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadBanners()
  loadMerchants()
  loadDishes(true)
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.index-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

.scroll-container {
  height: 100vh;
}

/* 顶部区域 */
.top-section {
  padding: $spacing-md;
  background-color: $bg-color-white;
}

/* 搜索栏 */
.search-bar {
  @include flex-center;
  gap: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  padding: $spacing-sm $spacing-md;
  margin-top: $spacing-sm;

  .search-icon {
    font-size: $font-size-lg;
  }

  .search-input {
    flex: 1;
    font-size: $font-size-base;
    color: $text-color-secondary;
  }
}

/* 轮播图 */
.banner-section {
  padding: $spacing-md 0;
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
}

.banner-swiper {
  width: 100%;
  height: 300rpx;
  border-radius: $border-radius-lg;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
}

/* 分类导航 */
.category-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.section-header {
  @include flex-between;
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.section-more,
.section-refresh {
  font-size: $font-size-sm;
  color: $text-color-secondary;

  .refresh-icon {
    margin-right: $spacing-xs;
  }
}

.category-scroll {
  white-space: nowrap;
}

.category-list {
  @include flex-center;
  gap: $spacing-lg;
}

.category-item {
  @include flex-center-column;
  gap: $spacing-xs;
  padding: $spacing-md;
  flex-shrink: 0;

  .category-icon {
    width: 100rpx;
    height: 100rpx;
    @include flex-center;
    background-color: $bg-color-base;
    border-radius: $border-radius-lg;
    font-size: 48rpx;
  }

  .category-name {
    font-size: $font-size-sm;
    color: $text-color-regular;
  }
}

/* 推荐商家 */
.merchant-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.merchant-scroll {
  white-space: nowrap;
}

.merchant-list {
  @include flex-center;
  gap: $spacing-md;
}

.merchant-card {
  width: 240rpx;
  flex-shrink: 0;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  overflow: hidden;
}

.merchant-logo {
  width: 100%;
  height: 160rpx;
}

.merchant-info {
  padding: $spacing-sm;
}

.merchant-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  @include text-ellipsis;
}

.merchant-rating {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  margin-top: $spacing-xs;

  .star {
    color: #f5a623;
  }

  .sales {
    color: $text-color-secondary;
  }
}

.merchant-tags {
  margin-top: $spacing-xs;
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;

  .tag {
    font-size: $font-size-xs;
    color: $primary-color;
    background-color: rgba(255, 107, 53, 0.1);
    padding: 4rpx 8rpx;
    border-radius: 4rpx;
  }
}

/* 推荐菜品 */
.dish-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.dish-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-base;
  overflow: hidden;
  box-shadow: $box-shadow-light;
}

.dish-image {
  width: 100%;
  height: 200rpx;
}

.dish-info {
  padding: $spacing-sm;
}

.dish-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  @include text-ellipsis;
}

.dish-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-top: $spacing-xs;
  @include text-ellipsis;
}

.dish-bottom {
  @include flex-between;
  margin-top: $spacing-sm;
}

.dish-price {
  @include flex-center;
  gap: 2rpx;
  color: $danger-color;
  font-weight: $font-weight-bold;

  .price-symbol {
    font-size: $font-size-sm;
  }

  .price-value {
    font-size: $font-size-lg;
  }
}

.dish-sales {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 加载更多 */
.load-more {
  padding: $spacing-lg 0;
}

.no-more {
  padding: $spacing-lg 0;
  text-align: center;
  color: $text-color-secondary;
  font-size: $font-size-sm;
}
</style>
