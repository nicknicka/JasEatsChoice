<template>
  <view class="home-container">
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

        <!-- 增强搜索栏 -->
        <SearchBar placeholder="搜索菜品、商家或食谱..." />
      </view>

      <!-- 今日热点 - U-026: 从后端获取动态热点 -->
      <view class="hot-topic-section" v-if="hotTopic.content">
        <view class="hot-topic-header">
          <text class="hot-topic-icon">🔥</text>
          <text class="hot-topic-title">今日热点</text>
        </view>
        <view class="hot-topic-content" @click="handleHotTopicClick">
          <text class="hot-topic-text">{{ hotTopic.content }}</text>
          <view class="hot-topic-arrow" v-if="hotTopic.clickable">›</view>
        </view>
      </view>

      <!-- 轮播图 -->
      <!-- 骨架屏 -->
      <BannerSkeleton v-if="isInitialLoading" />
      <!-- 实际内容 -->
      <view class="banner-section" v-else-if="banners.length > 0">
        <swiper
          class="banner-swiper"
          autoplay
          interval="5000"
          circular
          indicator-dots
          indicator-color="rgba(255,255,255,0.5)"
          indicator-active-color="#fff"
        >
          <swiper-item v-for="banner in banners" :key="banner.id" @click="handleBannerClick(banner)">
            <image
	              class="banner-image"
	              :src="banner.image"
	              mode="aspectFill"
	              lazy-load
	              @error="handleBannerImageError($event, banner)"
	            />
            <view class="banner-title" v-if="banner.title">{{ banner.title }}</view>
          </swiper-item>
        </swiper>
      </view>

      <!-- 分类导航 -->
      <!-- 骨架屏 -->
      <CategorySkeleton v-if="isInitialLoading" />
      <!-- 实际内容 -->
      <view class="category-section" v-else>
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
      <!-- 骨架屏 -->
      <MerchantSkeleton v-if="isInitialLoading" />
      <!-- 实际内容 -->
      <view class="merchant-section" v-else-if="recommendMerchants.length > 0">
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
              <image
	                class="merchant-logo"
	                :src="merchant.logo"
	                mode="aspectFill"
	                lazy-load
	                @error="handleMerchantImageError($event, merchant)"
	              />
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

      <!-- 智能推荐菜品 - U-027: 使用智能推荐系统 -->
      <view class="recommend-section">
        <view class="section-header">
          <text class="section-title">为你推荐</text>
          <view class="section-actions">
            <text class="section-refresh" @click="handleRefreshRecommend">
              <text class="refresh-icon" :class="{ rotating: isLoadingRecommend }">🔄</text>
              <text>换一换</text>
            </text>
          </view>
        </view>

        <!-- 快速筛选 -->
        <QuickFilters
          v-model="activeFilter"
          @filter-change="handleFilterChange"
        />

        <!-- 骨架屏 - 初始加载 -->
        <view class="dish-list" v-if="isInitialLoading">
          <DishCardSkeleton v-for="n in 5" :key="`skeleton-${n}`" />
        </view>

        <!-- 骨架屏 - 刷新中 -->
        <view class="loading-container" v-else-if="isLoadingRecommend">
          <uni-load-more status="loading" />
        </view>

        <!-- 推荐菜品列表 -->
        <view class="dish-list" v-else-if="recommendDishes.length > 0">
          <view
            class="dish-card"
            v-for="dish in recommendDishes"
            :key="dish.id"
            @click="handleDishClick(dish)"
          >
            <image
	              class="dish-image"
	              :src="dish.image"
	              mode="aspectFill"
	              lazy-load
	              @error="handleDishImageError($event, dish)"
	            />
            <view class="dish-info">
              <view class="dish-header">
                <view class="dish-name">{{ dish.name }}</view>
                <view class="dish-actions">
                  <text
                    class="action-btn favorite-btn"
                    :class="{ active: isFavorite(dish.id) }"
                    @click.stop="toggleFavorite(dish)"
                  >
                    {{ isFavorite(dish.id) ? '★' : '☆' }}
                  </text>
                </view>
              </view>
              <view class="dish-tags">
                <text class="tag source-tag">{{ dish.recommendSource || '推荐' }}</text>
                <text class="tag calorie-tag" v-if="dish.calories">{{ dish.calories }} kcal</text>
              </view>
              <view class="dish-reason" v-if="dish.recommendReason">
                <text class="reason-icon">💡</text>
                <text class="reason-text">{{ dish.recommendReason }}</text>
              </view>
              <view class="dish-bottom">
                <view class="dish-price">
                  <text class="price-symbol">¥</text>
                  <text class="price-value">{{ dish.price }}</text>
                </view>
                <view class="dish-rating" v-if="dish.rating">
                  <text class="star">⭐</text>
                  <text>{{ dish.rating.toFixed(1) }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <EmptyState
          v-else
          :type="emptyType"
          :title="emptyTitle"
          :description="emptyDescription"
          action-text="刷新推荐"
          @action="loadRecommendations"
        />
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
    <!-- 分享弹窗 -->
    <ShareModal
      ref="shareModalRef"
      :dish="currentShareDish"
    />

</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { toSearch, toMerchantDetail, toDishDetail } from '@/utils/router'
import { useLocationStore, useUserStore } from '@/store'
import { dishApi, merchantApi } from '@/api'
import { useRecommendations } from '@/composables/useRecommendations'
import WeatherLocation from '@/components/common/WeatherLocation.vue'
import DishCardSkeleton from '@/components/home/DishCardSkeleton.vue'
import CategorySkeleton from '@/components/home/CategorySkeleton.vue'
import MerchantSkeleton from '@/components/home/MerchantSkeleton.vue'
import BannerSkeleton from '@/components/home/BannerSkeleton.vue'
import EmptyState from '@/components/home/EmptyState.vue'
import SearchBar from '@/components/home/SearchBar.vue'
import QuickFilters from '@/components/home/QuickFilters.vue'
import ShareModal from '@/components/home/ShareModal.vue'
import analytics from '@/utils/analytics'
import recommendationAnalytics from '@/utils/recommendationAnalytics'

// Store
const locationStore = useLocationStore()
const userStore = useUserStore()

// 初始加载状态
const isInitialLoading = ref(true)

// 组件引用
const weatherRef = ref(null)
const shareModalRef = ref(null)

// 筛选状态
const activeFilter = ref('all')
const currentShareDish = ref(null)

// 使用智能推荐系统
const {
  recommendations: recommendDishes,
  isLoading: isLoadingRecommend,
  error: recommendError,
  hasRecommendations,
  isEmpty,
  loadRecommendations,
  refreshRecommendations,
  recordClickFeedback
} = useRecommendations()

// 监听推荐菜品变化，记录曝光埋点
const trackedDishes = ref(new Set())

watch(recommendDishes, (newDishes) => {
  if (newDishes && newDishes.length > 0) {
    newDishes.forEach((dish, index) => {
      const key = `${dish.id}_${dish.recommendSource || 'unknown'}`

      // 只记录新曝光的菜品
      if (!trackedDishes.value.has(key)) {
        trackedDishes.value.add(key)

        // 曝光埋点
        analytics.trackImpression('recommend_dish', {
          dish_id: dish.id,
          dish_name: dish.name,
          recommend_source: dish.recommendSource,
          position: index,
          price: dish.price,
          calories: dish.calories
        })

        // 推荐分析曝光
        recommendationAnalytics.recordImpression(dish.id, dish.recommendSource || 'unknown', {
          position: index
        })
      }
    })
  }
}, { deep: true })

// 状态
const refreshing = ref(false)
const loadingMore = ref(false)
const noMore = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = 10

// 轮播图数据
const banners = ref([])

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
const recommendMerchants = ref([])

// 今日热点数据
const hotTopic = ref({
  content: '',
  sourceType: '',
  sourceId: '',
  redirectUrl: '',
  clickable: false
})

// 收藏的菜品ID集合
const favoriteDishIds = ref(new Set())

// 计算属性：加载更多状态
const loadMoreStatus = computed(() => {
  if (refreshing.value) return 'loading'
  if (noMore.value) return 'noMore'
  if (loadingMore.value) return 'loading'
  return 'more'
})

// 推荐空状态消息
const recommendEmptyMessage = computed(() => {
  if (recommendError.value) {
    return '加载失败，请重试'
  }
  return '暂无推荐菜品'
})

// 空状态类型
const emptyType = computed(() => {
  if (recommendError.value) return 'network'
  if (hasRecommendations.value) return 'default'
  return 'dish'
})

// 空状态标题
const emptyTitle = computed(() => {
  if (recommendError.value) return '加载失败'
  return '暂无推荐菜品'
})

// 空状态描述
const emptyDescription = computed(() => {
  if (recommendError.value) return '请检查网络连接后重试'
  return '试试调整筛选条件或刷新推荐'
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
      loadHotTopic(),
      loadRecommendations({ forceRefresh: true })
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
    // 这里可以加载更多推荐或商家
    // await loadMoreData()
  } catch (error) {
    console.error('加载更多失败:', error)
    currentPage.value--
  } finally {
    loadingMore.value = false
  }
}

/**
 * 加载今日热点 - U-026
 */
const loadHotTopic = async () => {
  try {
    const response = await uni.request({
      url: '/api/v1/home/hot-topic',
      method: 'GET'
    })

    if (response.data && response.data.data) {
      const data = response.data.data
      if (typeof data === 'object') {
        hotTopic.value = data
      } else {
        hotTopic.value = {
          content: data,
          clickable: false
        }
      }
    }
  } catch (error) {
    console.error('加载今日热点失败:', error)
    hotTopic.value = { content: '', clickable: false }
  }
}

/**
 * 处理热点点击 - U-026
 */
const handleHotTopicClick = () => {
  if (!hotTopic.value.clickable) return

  // 记录点击
  uni.request({
    url: '/api/v1/home/hot-topic/click',
    method: 'POST',
    data: { content: hotTopic.value.content }
  })

  // 保存热点数据并跳转
  uni.setStorageSync('currentHotTopic', JSON.stringify(hotTopic.value))
  uni.navigateTo({
    url: '/home/hot-topic/index'
  })
}

/**
 * 处理刷新推荐 - U-027
 */
const handleRefreshRecommend = async () => {
  try {
    await refreshRecommendations()
  } catch (error) {
    console.error('刷新推荐失败:', error)
  }
}

/**
 * 处理菜品点击 - U-028
 */
const handleDishClick = async (dish) => {
  try {
    // 点击埋点
    analytics.trackClick('dish_card', 'dish', {
      dish_id: dish.id,
      dish_name: dish.name,
      recommend_source: dish.recommendSource,
      position: recommendDishes.value.findIndex(d => d.id === dish.id),
      price: dish.price,
      calories: dish.calories
    })

    // 推荐分析埋点
    recommendationAnalytics.recordClick(dish.id, dish.recommendSource || 'unknown', {
      position: recommendDishes.value.findIndex(d => d.id === dish.id)
    })

    // 记录点击反馈
    await recordClickFeedback(dish)

    // 跳转到菜品详情
    toDishDetail(dish.id)
  } catch (error) {
    console.error('处理菜品点击失败:', error)
    // 即使反馈失败也跳转
    toDishDetail(dish.id)
  }
}

/**
 * 切换收藏状态
 */
const toggleFavorite = async (dish) => {
  // 收藏按钮点击埋点
  analytics.trackClick('favorite_button', 'dish', {
    dish_id: dish.id,
    dish_name: dish.name,
    action: isFavorite(dish.id) ? 'unfavorite' : 'favorite'
  })

  try {
    const { favoriteApi } = await import('@/api')
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    if (isFavorite(dish.id)) {
      await favoriteApi.remove(userId, 'dish', dish.id)
      favoriteDishIds.value.delete(dish.id)
      uni.showToast({ title: '已取消收藏', icon: 'success' })

      // 取消收藏埋点
      analytics.trackEvent('unfavorite_dish', {
        dish_id: dish.id,
        dish_name: dish.name
      })
    } else {
      await favoriteApi.add(userId, 'dish', dish.id)
      favoriteDishIds.value.add(dish.id)
      uni.showToast({ title: '已收藏', icon: 'success' })

      // 收藏埋点
      analytics.trackEvent('favorite_dish', {
        dish_id: dish.id,
        dish_name: dish.name,
        recommend_source: dish.recommendSource
      })
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

/**
 * 处理分享
 */
const handleShare = (dish) => {
  // 分享按钮点击埋点
  analytics.trackClick('share_button', 'dish', {
    dish_id: dish.id,
    dish_name: dish.name,
    recommend_source: dish.recommendSource
  })

  currentShareDish.value = dish
  shareModalRef.value?.show()
}

/**
 * 处理筛选变化
 */
const handleFilterChange = async (filterType) => {
  console.log('筛选类型:', filterType)

  // 筛选埋点
  analytics.trackClick('quick_filter', 'filter', {
    filter_type: filterType,
    previous_filter: activeFilter.value
  })

  try {
    // 根据筛选类型应用不同的过滤条件
    const filterOptions = {
      forceRefresh: true
    }

    switch (filterType) {
      case 'low-calorie':
        // 低卡路里筛选
        filterOptions.maxCalories = 300
        break

      case 'high-rating':
        // 高评分筛选
        filterOptions.minRating = 4.5
        break

      case 'nearby':
        // 附近筛选
        if (locationStore.currentLocation) {
          filterOptions.latitude = locationStore.currentLocation.latitude
          filterOptions.longitude = locationStore.currentLocation.longitude
          filterOptions.radius = 3000
        }
        break

      case 'discount':
        // 优惠筛选
        filterOptions.hasDiscount = true
        break

      case 'spicy':
        // 辣味筛选
        filterOptions.flavor = 'spicy'
        break

      case 'sweet':
        // 甜食筛选
        filterOptions.flavor = 'sweet'
        break

      default:
        // 全部，不过滤
        break
    }

    // 重新加载推荐数据
    await loadRecommendations(filterOptions)

    uni.showToast({
      title: '筛选已应用',
      icon: 'success',
      duration: 1500
    })
  } catch (error) {
    console.error('应用筛选失败:', error)
    uni.showToast({
      title: '筛选失败',
      icon: 'none'
    })
  }
}

/**
 * 检查是否已收藏
 */
const isFavorite = (dishId) => {
  return favoriteDishIds.value.has(dishId)
}

/**
 * 图片加载失败处理 - Banner
 */
const handleBannerImageError = (event, banner) => {
  console.warn('Banner图片加载失败:', banner.image)
  banner.image = '/static/images/default-banner.png'
}

/**
 * 图片加载失败处理 - 商家Logo
 */
const handleMerchantImageError = (event, merchant) => {
  console.warn('商家Logo加载失败:', merchant.logo)
  merchant.logo = '/static/images/default-merchant.png'
}

/**
 * 图片加载失败处理 - 菜品图片
 */
const handleDishImageError = (event, dish) => {
  console.warn('菜品图片加载失败:', dish.image)
  dish.image = '/static/images/default-dish.png'
}

/**
 * 加载轮播图 - U-022: 调用后端API
 */
const loadBanners = async () => {
  try {
    const { bannerApi } = await import('@/api')
    const res = await bannerApi.getList({ position: 'home' })

    if (res && res.data && Array.isArray(res.data)) {
      banners.value = res.data.map(banner => ({
        id: banner.bannerId || banner.id,
        image: banner.imageUrl || banner.image,
        title: banner.title || '',
        type: banner.type || 'link',
        targetType: banner.targetType || '',
        targetId: banner.targetId || '',
        link: banner.link || ''
      }))
    }
  } catch (error) {
    console.error('加载轮播图失败:', error)
    banners.value = []
  }
}

/**
 * 加载推荐商家
 */
const loadMerchants = async () => {
  try {
    const params = {
      limit: 10
    }

    if (locationStore.currentLocation) {
      params.latitude = locationStore.currentLocation.latitude
      params.longitude = locationStore.currentLocation.longitude
      params.radius = 5000
    }

    const res = await merchantApi.getNearby(params)

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
    recommendMerchants.value = []
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

  // 分类点击埋点
  analytics.trackClick('category_item', 'category', {
    category_id: category.id,
    category_name: category.name,
    category_code: category.code
  })

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
    url: '/category/index',
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
    url: '/home/merchant-list'
  })
}

// 组件挂载时加载数据
onMounted(async () => {
  // 页面访问埋点
  analytics.trackPageView('home', {
    source: getApp().globalData?.launchOptions?.scene || 'direct',
    timestamp: Date.now()
  })

  try {
    // 并行加载所有数据
    await Promise.all([
      loadBanners(),
      loadMerchants(),
      loadHotTopic()
    ])

    // 加载智能推荐
    await loadRecommendations()

    // 加载用户收藏
    if (userStore.isLogin) {
      loadUserFavorites()
    }
  } finally {
    // 初始加载完成
    isInitialLoading.value = false
  }
})

// 组件卸载时上报埋点数据
onUnmounted(() => {
  analytics.flush()
  recommendationAnalytics.report()
})

/**
 * 加载用户收藏列表
 */
const loadUserFavorites = async () => {
  try {
    const { favoriteApi } = await import('@/api')
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const res = await favoriteApi.listByType(userId, 'dish')

    if (Array.isArray(res)) {
      favoriteDishIds.value = new Set(res.map(fav => fav.itemId))
    }
  } catch (error) {
    console.error('加载收藏失败:', error)
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.home-container {
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

/* 今日热点 - U-026 */
.hot-topic-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: $spacing-md;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

  .hot-topic-header {
    @include flex-center;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;

    .hot-topic-icon {
      font-size: 40rpx;
      animation: flame-flicker 0.5s ease-in-out infinite alternate;
    }

    .hot-topic-title {
      font-size: $font-size-lg;
      font-weight: $font-weight-bold;
      color: #fff;
    }
  }

  .hot-topic-content {
    @include flex-between;
    background-color: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    border-radius: $border-radius-base;
    padding: $spacing-md;
    border: 1px solid rgba(255, 255, 255, 0.3);

    .hot-topic-text {
      flex: 1;
      font-size: $font-size-base;
      color: #fff;
      line-height: 1.5;
      font-weight: 500;
    }

    .hot-topic-arrow {
      font-size: 48rpx;
      color: rgba(255, 255, 255, 0.8);
      margin-left: $spacing-sm;
    }
  }
}

@keyframes flame-flicker {
  0% {
    transform: scale(1) rotate(-2deg);
    opacity: 0.9;
  }
  100% {
    transform: scale(1.1) rotate(2deg);
    opacity: 1;
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

.banner-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  color: #fff;
  padding: $spacing-md;
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
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

.section-actions {
  @include flex-center;
  gap: $spacing-md;
}

.section-more,
.section-refresh {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-color-secondary;

  .refresh-icon {
    transition: transform 0.3s ease;

    &.rotating {
      animation: rotate 0.5s linear infinite;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
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

/* 智能推荐 - U-027 */
.recommend-section {
  background-color: $bg-color-white;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
}

.loading-container {
  padding: $spacing-lg 0;
  display: flex;
  justify-content: center;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.dish-card {
  background-color: $bg-color-white;
  border-radius: $border-radius-base;
  overflow: hidden;
  box-shadow: $box-shadow-light;
  display: flex;
  transition: transform 0.2s ease;
  animation: slideInUp 0.4s ease-out backwards;

  &:active {
    transform: scale(0.98);
  }
}

.dish-image {
  width: 200rpx;
  height: 200rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  padding: $spacing-sm;
  display: flex;
  flex-direction: column;
}

.dish-header {
  @include flex-between;
  margin-bottom: $spacing-xs;

  .dish-name {
    flex: 1;
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
    @include text-ellipsis;
  }

  .dish-actions {
    @include flex-center;
    gap: $spacing-xs;

    .action-btn {
      font-size: 48rpx;
      color: $text-color-secondary;
      transition: all 0.3s ease;

      &.share-btn {
        font-size: 40rpx;

        &:active {
          transform: scale(0.9);
        }
      }

      &.favorite-btn.active {
        color: #ffd700;
        animation: star-bounce 0.3s ease;
      }
    }
  }
}

@keyframes star-bounce {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}

.dish-tags {
  @include flex-center;
  gap: $spacing-xs;
  margin-bottom: $spacing-xs;
  flex-wrap: wrap;

  .tag {
    font-size: $font-size-xs;
    padding: 4rpx 12rpx;
    border-radius: 12rpx;

    &.source-tag {
      background-color: rgba(102, 126, 234, 0.1);
      color: #667eea;
    }

    &.calorie-tag {
      background-color: rgba(255, 107, 107, 0.1);
      color: #ff6b6b;
    }
  }
}

.dish-reason {
  @include flex-center;
  gap: $spacing-xs;
  margin-bottom: $spacing-sm;
  padding: $spacing-xs;
  background-color: $bg-color-base;
  border-radius: $border-radius-sm;

  .reason-icon {
    font-size: $font-size-sm;
  }

  .reason-text {
    flex: 1;
    font-size: $font-size-sm;
    color: $text-color-regular;
    line-height: 1.4;
  }
}

.dish-bottom {
  @include flex-between;
  margin-top: auto;

  .dish-price {
    @include flex-center;
    gap: 2rpx;
    color: $danger-color;
    font-weight: $font-weight-bold;

    .price-symbol {
      font-size: $font-size-sm;
    }

    .price-value {
      font-size: $font-size-xl;
    }
  }

  .dish-rating {
    @include flex-center;
    gap: $spacing-xs;
    font-size: $font-size-sm;

    .star {
      color: #f5a623;
    }
  }
}

/* 空状态 */
.empty-container {
  padding: $spacing-xl 0;
  @include flex-center-column;
  gap: $spacing-md;

  .empty-icon {
    font-size: 120rpx;
    opacity: 0.5;
  }

  .empty-text {
    font-size: $font-size-base;
    color: $text-color-secondary;
  }

  .retry-btn {
    margin-top: $spacing-sm;
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
</style>
