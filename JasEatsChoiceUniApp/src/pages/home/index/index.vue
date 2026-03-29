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
          <view class="search-scan">📷</view>
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

        <!-- 快速筛选 -->
        <view class="filter-section">
          <scroll-view class="filter-scroll" scroll-x show-scrollbar="false">
            <view class="filter-list">
              <view
                v-for="filter in filters"
                :key="filter.key"
                class="filter-item"
                :class="{ active: currentFilter === filter.key }"
                @click="handleFilterChange(filter.key)"
              >
                <text class="filter-icon">{{ filter.icon }}</text>
                <text class="filter-text">{{ filter.label }}</text>
              </view>
            </view>
          </scroll-view>
        </view>
        <view class="dish-grid">
          <view
            class="dish-card"
            v-for="dish in recommendDishes"
            :key="dish.id"
            @click="handleDishClick(dish)"
          >
            <image class="dish-image" :src="dish.image" mode="aspectFill" />

            <!-- 标签 -->
            <view class="dish-tags" v-if="dish.tags && dish.tags.length">
              <text class="tag tag-discount" v-if="dish.discount">{{ dish.discount }}</text>
              <text class="tag tag-new" v-if="dish.isNew">新品</text>
              <text class="tag tag-hot" v-if="dish.isHot">热销</text>
            </view>

            <view class="dish-info">
              <view class="dish-name">{{ dish.name }}</view>

              <!-- 推荐理由 -->
              <view class="dish-reason" v-if="dish.recommendReason">
                <text class="reason-icon">✨</text>
                <text class="reason-text">{{ dish.recommendReason }}</text>
              </view>

              <view class="dish-desc" v-else>{{ dish.description }}</view>

              <view class="dish-bottom">
                <view class="dish-price">
                  <text class="price-symbol">¥</text>
                  <text class="price-value">{{ dish.price }}</text>
                  <text class="price-original" v-if="dish.originalPrice">¥{{ dish.originalPrice }}</text>
                </view>
                <view class="dish-sales">已售{{ dish.sales }}</view>
              </view>
            </view>

            <!-- 购物车按钮 -->
            <view class="add-cart-btn" @click.stop="addToCart(dish)">
              <text>+</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="recommendDishes.length === 0 && !refreshing">
        <view class="empty-icon">🍽️</view>
        <text class="empty-title">暂无推荐菜品</text>
        <text class="empty-desc">试试换个筛选条件或刷新一下吧</text>
        <button class="empty-btn" @click="refreshRecommend">
          <text class="btn-icon">🔄</text>
          <text>重新加载</text>
        </button>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="!noMore">
        <uni-load-more :status="loadMoreStatus" />
      </view>

      <!-- 没有更多 -->
      <view class="no-more" v-if="noMore">
        <text>~ 没有更多了 ~</text>
      </view>

      <!-- 底部安全区域 -->
      <view class="safe-area-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { toSearch, toMerchantDetail, toDishDetail } from '@/utils/router'
import { useLocationStore, useUserStore } from '@/store'
import { recommendationApi, merchantApi, bannerApi } from '@/api'
import { processImageUrl } from '@/utils/helper'
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

// 筛选器配置
const filters = [
  { key: 'all', label: '全部', icon: '🍽️' },
  { key: 'low_calorie', label: '低卡', icon: '🥗' },
  { key: 'high_rating', label: '高分', icon: '⭐' },
  { key: 'nearby', label: '附近', icon: '📍' },
  { key: 'discount', label: '优惠', icon: '🎁' }
]

const currentFilter = ref('all')

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
    const res = await bannerApi.getList({ position: 'home' })

    if (res && res.data && Array.isArray(res.data)) {
      banners.value = res.data.map(banner => ({
        id: banner.bannerId || banner.id,
        image: processImageUrl(banner.imageUrl || banner.image),
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
 * 加载推荐菜品（使用后端推荐系统）
 */
const loadDishes = async (refresh = false) => {
  try {
    // 获取用户ID
    const userId = userStore.isLogin
      ? (userStore.userInfo?.userId || userStore.userInfo?.id)
      : '1'

    // 获取当前时段
    const getTimePeriod = () => {
      const hour = new Date().getHours()
      if (hour >= 6 && hour < 10) return '早餐'
      if (hour >= 10 && hour < 14) return '午餐'
      if (hour >= 14 && hour < 18) return '下午茶'
      if (hour >= 18 && hour < 22) return '晚餐'
      return '夜宵'
    }

    // 调用后端推荐系统API
    const res = await recommendationApi.getRecommendations(userId, {
      scene: 'home',
      limit: pageSize,
      timePeriod: getTimePeriod()
    })

    console.log('推荐系统返回:', res)

    // 数据映射 - 兼容多种返回格式
    let dishes = []
    if (res && res.data) {
      if (res.data.recommendations) {
        dishes = res.data.recommendations
      } else if (Array.isArray(res.data)) {
        dishes = res.data
      }
    } else if (Array.isArray(res)) {
      dishes = res
    }

    // 统一字段映射
    const mappedDishes = dishes.map(dish => ({
      id: dish.dishId || dish.id,
      dishId: dish.dishId || dish.id,
      name: dish.dishName || dish.name,
      description: dish.description || dish.desc || '',
      price: dish.price ? String(dish.price) : '0',
      originalPrice: dish.originalPrice || '',
      sales: dish.monthlySales || dish.sales || 0,
      image: dish.image || dish.coverImage,
      recommendReason: dish.recommendReason || dish.reason,
      recommendSource: dish.recommendSource || '系统推荐',
      rating: dish.rating || dish.avgRating || 4.5,
      // 标签
      tags: dish.tags || [],
      discount: dish.discount || '',
      isNew: dish.isNew || false,
      isHot: dish.isHot || false
    }))

    if (refresh) {
      recommendDishes.value = mappedDishes
    } else {
      recommendDishes.value.push(...mappedDishes)
    }

    if (mappedDishes.length < pageSize) {
      noMore.value = true
    }

    console.log(`✅ 推荐加载成功: ${mappedDishes.length}个菜品`)
  } catch (error) {
    console.error('❌ 加载推荐菜品失败:', error)

    // 降级方案：使用简单推荐接口
    try {
      console.log('🔄 使用降级方案...')
      const { dishApi } = await import('@/api')
      const fallbackRes = await dishApi.getRecommend({
        page: currentPage.value,
        size: pageSize
      })

      let dishes = []
      if (Array.isArray(fallbackRes)) {
        dishes = fallbackRes
      } else if (fallbackRes && fallbackRes.list) {
        dishes = fallbackRes.list
      }

      const mappedDishes = dishes.map(dish => ({
        id: dish.dishId || dish.id,
        name: dish.dishName || dish.name,
        description: dish.description || dish.desc || '',
        price: dish.price ? String(dish.price) : '0',
        sales: dish.monthlySales || dish.sales || 0,
        image: dish.image || dish.coverImage,
        recommendSource: '基础推荐'
      }))

      if (refresh) {
        recommendDishes.value = mappedDishes
      } else {
        recommendDishes.value.push(...mappedDishes)
      }

      console.log(`✅ 降级方案成功: ${mappedDishes.length}个菜品`)
    } catch (fallbackError) {
      console.error('❌ 降级方案也失败:', fallbackError)
      if (refresh) {
        recommendDishes.value = []
      }
    }
  }
}

/**
 * 刷新推荐（使用推荐系统刷新接口）
 */
const refreshRecommend = async () => {
  try {
    refreshing.value = true
    currentPage.value = 1
    noMore.value = false

    // 如果已登录，使用推荐系统的刷新接口
    if (userStore.isLogin) {
      const userId = userStore.userInfo?.userId || userStore.userInfo?.id || '1'
      await recommendationApi.refreshRecommendations(userId)
      console.log('✅ 推荐刷新成功')
    }

    // 重新加载推荐
    await loadDishes(true)

    uni.showToast({ title: '刷新成功', icon: 'success' })
  } catch (error) {
    console.error('❌ 刷新推荐失败:', error)
    // 即使刷新失败，也重新加载
    await loadDishes(true)
  } finally {
    refreshing.value = false
  }
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

/**
 * 处理菜品点击 - 记录推荐反馈并跳转
 */
const handleDishClick = async (dish) => {
  if (!dish) return

  try {
    // 异步记录点击反馈（不阻塞跳转）
    if (userStore.isLogin && dish.recommendSource) {
      const userId = userStore.userInfo?.userId || userStore.userInfo?.id || '1'

      recommendationApi.recordFeedback({
        userId,
        dishId: String(dish.dishId || dish.id),
        recommendationId: String(dish.id),
        isClicked: true,
        isOrdered: false
      }).then(() => {
        console.log('✓ 点击反馈已记录')
      }).catch(err => {
        console.warn('记录点击反馈失败:', err)
      })
    }
  } catch (error) {
    console.warn('记录点击反馈出错:', error)
  }

  // 立即跳转到详情页
  toDishDetail(dish.id)
}

/**
 * 添加到购物车
 */
const addToCart = (dish) => {
  if (!dish) return

  // TODO: 实现添加到购物车逻辑
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })

  console.log('添加到购物车:', dish)
}

/**
 * 处理筛选变化
 */
const handleFilterChange = (filterKey) => {
  if (currentFilter.value === filterKey) return

  currentFilter.value = filterKey

  // 显示加载提示
  uni.showLoading({
    title: '加载中...'
  })

  // 重新加载推荐数据
  loadDishes(true).finally(() => {
    uni.hideLoading()
  })

  console.log('筛选条件:', filterKey)
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
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin-top: $spacing-md;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    background-color: #f0f0f0;
  }

  .search-icon {
    font-size: 36rpx;
    color: $text-color-secondary;
  }

  .search-input {
    flex: 1;
    font-size: $font-size-base;
    color: $text-color-secondary;
    line-height: 1.5;
  }

  .search-scan {
    font-size: 36rpx;
    padding: $spacing-xs;
    margin-left: $spacing-xs;
  }
}

/* 轮播图 */
.banner-section {
  background-color: $bg-color-white;
  margin-bottom: $spacing-sm;
  padding: 0;

  .banner-swiper {
    width: 100%;
    height: 320rpx;
    border-radius: 0;
    overflow: hidden;
  }

  .banner-image {
    width: 100%;
    height: 100%;
    border-radius: 0;
  }
}

/* 分类导航 */
.category-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  display: flex;
  align-items: center;
  gap: 4rpx;

  .refresh-icon {
    margin-right: $spacing-xs;
  }
}

.category-scroll {
  white-space: nowrap;
}

.category-list {
  display: inline-flex;
  gap: $spacing-lg;
  padding: 0 $spacing-sm;
}

.category-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-sm;
  flex-shrink: 0;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
  }

  .category-icon {
    width: 100rpx;
    height: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #FFE5D9 0%, #FFD4C4 100%);
    border-radius: $border-radius-lg;
    font-size: 48rpx;
    box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.1);
  }

  .category-name {
    font-size: $font-size-sm;
    color: $text-color-regular;
    text-align: center;
  }
}

/* 推荐商家 */
.merchant-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
}

.merchant-scroll {
  white-space: nowrap;
}

.merchant-list {
  display: inline-flex;
  gap: $spacing-md;
  padding: 0 $spacing-sm;
}

.merchant-card {
  width: 240rpx;
  flex-shrink: 0;
  background-color: $bg-color-white;
  border-radius: $border-radius-base;
  overflow: hidden;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:active {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 16rpx rgba(0, 0, 0, 0.12);
  }
}

.merchant-logo {
  width: 100%;
  height: 160rpx;
  background-color: $bg-color-base;
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
  display: flex;
  align-items: center;
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
    color: #FF6B35;
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

/* 快速筛选 */
.filter-section {
  margin: $spacing-md (-$spacing-md);
  padding: 0 $spacing-md $spacing-md;
  border-bottom: 1rpx solid $border-color-light;
}

.filter-scroll {
  white-space: nowrap;
}

.filter-list {
  display: inline-flex;
  gap: $spacing-md;
}

.filter-item {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background-color: $bg-color-base;
  border-radius: 40rpx;
  transition: all 0.3s ease;
  flex-shrink: 0;

  &.active {
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
    box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);

    .filter-text {
      color: #fff;
    }
  }

  .filter-icon {
    font-size: 28rpx;
  }

  .filter-text {
    font-size: $font-size-sm;
    color: $text-color-regular;
  }
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;
}

.dish-card {
  position: relative;
  background-color: $bg-color-white;
  border-radius: $border-radius-base;
  overflow: visible;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:active {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
  }
}

.dish-image {
  width: 100%;
  height: 220rpx;
  border-radius: $border-radius-base $border-radius-base 0 0;
}

/* 菜品标签 */
.dish-tags {
  position: absolute;
  top: 12rpx;
  left: 12rpx;
  display: flex;
  gap: 8rpx;

  .tag {
    padding: 4rpx 12rpx;
    font-size: $font-size-xs;
    border-radius: 20rpx;
    color: #fff;

    &.tag-discount {
      background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
    }

    &.tag-new {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    &.tag-hot {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
  }
}

.dish-info {
  padding: $spacing-sm;
  position: relative;
}

.dish-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  @include text-ellipsis;
  margin-bottom: 6rpx;
}

/* 推荐理由 */
.dish-reason {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 12rpx;
  background: linear-gradient(135deg, #FFF8E1 0%, #FFECB3 100%);
  border-radius: 8rpx;
  margin-bottom: $spacing-xs;

  .reason-icon {
    font-size: $font-size-sm;
  }

  .reason-text {
    font-size: $font-size-xs;
    color: #F57C00;
    flex: 1;
    @include text-ellipsis;
  }
}

.dish-desc {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-top: $spacing-xs;
  @include text-ellipsis;
}

.dish-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: $spacing-sm;
}

.dish-price {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  color: $danger-color;
  font-weight: $font-weight-bold;

  .price-symbol {
    font-size: $font-size-sm;
  }

  .price-value {
    font-size: $font-size-lg;
  }

  .price-original {
    font-size: $font-size-xs;
    color: $text-color-placeholder;
    text-decoration: line-through;
    font-weight: normal;
  }
}

.dish-sales {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

/* 购物车按钮 */
.add-cart-btn {
  position: absolute;
  bottom: 12rpx;
  right: 12rpx;
  width: 56rpx;
  height: 56rpx;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.4);
  z-index: 10;

  &:active {
    transform: scale(0.9);
  }
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

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx $spacing-lg;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: $spacing-lg;
    opacity: 0.5;
  }

  .empty-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
    margin-bottom: $spacing-sm;
  }

  .empty-desc {
    font-size: $font-size-base;
    color: $text-color-secondary;
    margin-bottom: $spacing-xl;
  }

  .empty-btn {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: 20rpx 48rpx;
    background: linear-gradient(135deg, #FF6B35 0%, #FF8C61 100%);
    color: #fff;
    border-radius: 40rpx;
    font-size: $font-size-base;
    border: none;
    box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);

    &::after {
      border: none;
    }

    .btn-icon {
      font-size: $font-size-lg;
    }
  }
}

/* 底部安全区域 */
.safe-area-bottom {
  height: constant(safe-area-inset-bottom);
  height: env(safe-area-inset-bottom);
  background-color: $bg-color-base;
}
</style>
