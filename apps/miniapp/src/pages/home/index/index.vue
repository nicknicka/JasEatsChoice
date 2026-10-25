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
            <image class="banner-image" :src="banner.image" mode="aspectFill" @error="handleBannerImageError(banner)" />
          </swiper-item>
        </swiper>
      </view>

      <!-- 分类导航 -->
      <view class="category-section">
        <view class="section-header">
          <text class="section-title">美食分类</text>
          <button class="section-more" @click="toMoreCategories" aria-label="查看更多分类">更多 ›</button>
        </view>
        <scroll-view class="category-scroll" scroll-x show-scrollbar="false">
          <view class="category-list">
            <button
              class="category-item"
              v-for="category in categories"
              :key="category.id"
              @click="handleCategoryClick(category)"
            >
              <view class="category-icon">{{ category.icon }}</view>
              <view class="category-name">{{ category.name }}</view>
            </button>
          </view>
        </scroll-view>
      </view>

      <!-- 推荐商家 -->
      <view class="merchant-section" v-if="recommendMerchants.length > 0">
        <view class="section-header">
          <text class="section-title">推荐商家</text>
          <button class="section-more" @click="toMoreMerchants" aria-label="查看更多商家">更多 ›</button>
        </view>
        <scroll-view class="merchant-scroll" scroll-x show-scrollbar="false">
          <view class="merchant-list">
            <button
              class="merchant-card"
              v-for="merchant in recommendMerchants"
              :key="merchant.id"
              @click="toMerchantDetail(merchant.id)"
            >
              <image class="merchant-logo" :src="merchant.logo" mode="aspectFill" @error="handleMerchantImageError(merchant)" />
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
            </button>
          </view>
        </scroll-view>
      </view>

      <!-- 推荐菜品 -->
      <view class="dish-section">
        <view class="section-header">
          <text class="section-title">为你推荐</text>
          <button class="section-refresh" @click="refreshRecommend" aria-label="刷新推荐菜品">
            <text class="refresh-icon">🔄</text> 换一换
          </button>
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
            <image class="dish-image" :src="dish.image" mode="aspectFill" @error="handleDishImageError(dish)" />

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
            <button class="add-cart-btn" @click.stop="addToCart(dish)" :aria-label="`将${dish.name}加入购物车`">
              <text>+</text>
            </button>
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
import { toSearch, toMerchantDetail, toDishDetail, toRecipeDetail } from '@/utils/router'
import { useLocationStore, useUserStore } from '@/store'
import { recommendationApi, merchantApi, bannerApi, categoryApi, dishApi } from '@/api'
import { processImageUrl } from '@/utils/helper'
import { createPageDebug } from '@/utils/page-debug'
import { normalizeCategories } from '@/config/category-icons'
import { USER_DISH_LIST, USER_HOME_MERCHANT_LIST } from '@/constants/routes'
import WeatherLocation from '@/components/common/WeatherLocation.vue'

// Store
const locationStore = useLocationStore()
const userStore = useUserStore()
const pageDebug = createPageDebug('首页')
const isDev = process.env.NODE_ENV !== 'production'

// 组件引用
const weatherRef = ref(null)
const DEFAULT_BANNER_IMAGE = '/static/images/default-banner.png'
const DEFAULT_MERCHANT_IMAGE = '/static/images/default-merchant.png'
const DEFAULT_DISH_IMAGE = '/static/images/default-dish.png'

// 状态
const refreshing = ref(false)
const loadingMore = ref(false)
const noMore = ref(false)

// 分页参数
const currentPage = ref(1)
const pageSize = 10

// 轮播图数据 - 从后端加载
const banners = ref([])

const resolveImage = (image, fallback) => processImageUrl(image) || fallback

// 分类数据 - 从后端加载
const categories = ref([])

// 推荐商家数据 - 从后端加载
const recommendMerchants = ref([])

// 推荐菜品数据 - 从后端加载
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
  pageDebug.action('下拉刷新', {
    page: currentPage.value,
    currentFilter: currentFilter.value
  })
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
    pageDebug.requestSuccess('首页刷新', {
      banners: banners.value.length,
      merchants: recommendMerchants.value.length,
      dishes: recommendDishes.value.length
    })
  } catch (error) {
    pageDebug.requestFail('首页刷新', error)
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
  if (loadingMore.value || noMore.value) {
    pageDebug.state('跳过加载更多', {
      loadingMore: loadingMore.value,
      noMore: noMore.value
    })
    return
  }

  loadingMore.value = true
  currentPage.value++
  pageDebug.action('加载更多推荐菜品', {
    currentPage: currentPage.value
  })

  try {
    await loadDishes(false)
  } catch (error) {
    pageDebug.requestFail('加载更多推荐菜品', error)
    console.error('加载更多失败:', error)
    currentPage.value--
  } finally {
    loadingMore.value = false
  }
}

/**
 * 加载美食分类 - 从后端获取
 */
const loadCategories = async () => {
  try {
    pageDebug.requestStart('加载美食分类')
    // 调用后端API获取常用品类
    const res = await categoryApi.getCommon()

    if (res && res.data && Array.isArray(res.data)) {
      // 将分类名称转换为分类对象（包含图标和代码）
      categories.value = normalizeCategories(res.data)
      pageDebug.requestSuccess('加载美食分类', {
        count: categories.value.length
      })
    }
  } catch (error) {
    pageDebug.requestFail('加载美食分类', error)
    console.error('加载分类失败，使用默认数据:', error)
    // 使用本地默认数据作为fallback
    categories.value = normalizeCategories([
      '中式快餐',
      '火锅',
      '烧烤',
      '川菜',
      '西餐',
      '日韩料理',
      '小吃快餐',
      '饮品甜点'
    ])
    pageDebug.anomaly('美食分类回退到本地默认数据', {
      count: categories.value.length
    })
  }
}

/**
 * 加载轮播图 - U-022: 调用后端API
 */
const loadBanners = async () => {
  try {
    pageDebug.requestStart('加载首页轮播图')
    // U-022: 调用后端API获取轮播图
    const res = await bannerApi.getList({ position: 'home' })

    if (res && res.data && Array.isArray(res.data)) {
      banners.value = res.data.map(banner => ({
        id: banner.bannerId || banner.id,
        image: resolveImage(banner.imageUrl || banner.image, DEFAULT_BANNER_IMAGE),
        title: banner.title || '',
        type: banner.type || 'link', // link, dish, merchant, activity
        targetType: banner.targetType || '', // 跳转目标类型
        targetId: banner.targetId || '', // 跳转目标ID
        link: banner.link || '' // 外部链接
      }))
      pageDebug.requestSuccess('加载首页轮播图', {
        count: banners.value.length
      })
    }
  } catch (error) {
    pageDebug.requestFail('加载首页轮播图', error)
    console.error('加载轮播图失败，使用默认数据:', error)
    // 使用本地默认数据作为fallback
    banners.value = [
      {
        id: 1,
        image: '/static/banner1.jpg',
        title: '今日推荐',
        type: 'link'
      },
      {
        id: 2,
        image: '/static/banner2.jpg',
        title: '美食特惠',
        type: 'link'
      },
      {
        id: 3,
        image: '/static/banner3.jpg',
        title: '新品上市',
        type: 'link'
      }
    ]
    pageDebug.anomaly('首页轮播图回退到本地默认数据', {
      count: banners.value.length
    })
  }
}

/**
 * 加载推荐商家
 */
const loadMerchants = async () => {
  try {
    pageDebug.requestStart('加载推荐商家', {
      hasLocation: Boolean(locationStore.currentLocation)
    })
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
        logo: resolveImage(merchant.avatar || merchant.logo || merchant.coverImage, DEFAULT_MERCHANT_IMAGE),
        rating: merchant.rating || merchant.score || 0,
        monthlySales: merchant.monthlySales || 0,
        tags: merchant.tags || []
      }))
      pageDebug.requestSuccess('加载推荐商家', {
        count: recommendMerchants.value.length
      })
    }
  } catch (error) {
    pageDebug.requestFail('加载推荐商家', error)
    console.error('加载商家失败:', error)
    // 不使用mock数据，直接显示空状态
    recommendMerchants.value = []
  }
}

/**
 * 加载推荐菜品（使用后端推荐系统）
 */
const loadDishes = async (refresh = false) => {
  try {
    pageDebug.requestStart('加载推荐菜品', {
      refresh,
      page: currentPage.value,
      currentFilter: currentFilter.value
    })
    // 获取用户ID
    const userId = userStore.isLogin
      ? (userStore.userInfo?.userId || userStore.userInfo?.id || userStore.userId || '1')
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

    if (isDev) {
      console.log('推荐系统返回:', res)
    }

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

    // 统一字段映射 - 确保所有字段都是正确的类型
    const mappedDishes = dishes.map(dish => ({
      id: dish.dishId || dish.id,
      dishId: dish.dishId || dish.id,
      name: String(dish.dishName || dish.name || '未知菜品'),
      description: String(dish.description || dish.desc || ''),
      price: dish.price ? String(dish.price) : '0',
      originalPrice: dish.originalPrice ? String(dish.originalPrice) : '',
      sales: Number(dish.monthlySales || dish.sales || 0),
      image: resolveImage(dish.dishImage || dish.image || dish.coverImage || '', DEFAULT_DISH_IMAGE),
      recommendReason: dish.reason && typeof dish.reason === 'object'
        ? String(dish.reason.mainReason || dish.reason.text || '')
        : String(dish.recommendReason || dish.reason || ''),
      recommendSource: String(dish.source || dish.recommendSource || '系统推荐'),
      rating: Number(dish.rating || dish.avgRating || 4.5),
      // 标签 - 确保是数组
      tags: Array.isArray(dish.tags) ? dish.tags : [],
      discount: String(dish.discount || ''),
      isNew: Boolean(dish.isNew),
      isHot: Boolean(dish.isHot)
    }))

    if (refresh) {
      recommendDishes.value = mappedDishes
    } else {
      recommendDishes.value.push(...mappedDishes)
    }

    if (mappedDishes.length < pageSize) {
      noMore.value = true
    }

    pageDebug.requestSuccess('加载推荐菜品', {
      refresh,
      count: mappedDishes.length,
      total: recommendDishes.value.length,
      noMore: noMore.value
    })
  } catch (error) {
    pageDebug.requestFail('加载推荐菜品', error)
    console.error('加载推荐菜品失败:', error)

    // 降级方案：使用简单推荐接口
    try {
      pageDebug.anomaly('推荐系统主接口失败，尝试降级接口', {
        page: currentPage.value
      })
      if (isDev) {
        console.log('推荐系统主接口失败，切换到降级接口')
      }
      // 直接使用已经导入的 dishApi
      const fallbackRes = await dishApi.getRecommend({
        userId: userStore.userId || userStore.userInfo?.userId || '1',
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
        dishId: dish.dishId || dish.id,
        name: String(dish.dishName || dish.name || '未知菜品'),
        description: String(dish.description || dish.desc || ''),
        price: dish.price ? String(dish.price) : '0',
        sales: Number(dish.monthlySales || dish.sales || 0),
        image: resolveImage(dish.dishImage || dish.image || dish.coverImage || '', DEFAULT_DISH_IMAGE),
        recommendSource: '基础推荐',
        rating: Number(dish.rating || 4.5),
        tags: Array.isArray(dish.tags) ? dish.tags : []
      }))

      if (refresh) {
        recommendDishes.value = mappedDishes
      } else {
        recommendDishes.value.push(...mappedDishes)
      }

      pageDebug.requestSuccess('加载推荐菜品-降级接口', {
        count: mappedDishes.length,
        total: recommendDishes.value.length
      })
    } catch (fallbackError) {
      pageDebug.requestFail('加载推荐菜品-降级接口', fallbackError)
      console.error('降级推荐接口失败:', fallbackError)
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
    pageDebug.action('点击换一换', {
      currentFilter: currentFilter.value
    })
    refreshing.value = true
    currentPage.value = 1
    noMore.value = false

    // 如果已登录，使用推荐系统的刷新接口
    if (userStore.isLogin) {
      const userId = userStore.userInfo?.userId || userStore.userInfo?.id || '1'
      await recommendationApi.refreshRecommendations(userId)
    }

    // 重新加载推荐
    await loadDishes(true)

    uni.showToast({ title: '刷新成功', icon: 'success' })
    pageDebug.requestSuccess('刷新推荐菜品', {
      count: recommendDishes.value.length
    })
  } catch (error) {
    pageDebug.requestFail('刷新推荐菜品', error)
    console.error('刷新推荐失败:', error)
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
  pageDebug.action('点击轮播图', {
    bannerId: banner?.id,
    type: banner?.type,
    targetId: banner?.targetId
  })
  // U-023: 根据banner类型进行不同的跳转
  if (!banner) return

  try {
    switch (banner.type) {
      case 'dish':
        // 跳转到菜品详情
        if (banner.targetId) {
          toDishDetail(banner.targetId)
        }
        break

      case 'merchant':
        // 跳转到商家详情
        if (banner.targetId) {
          toMerchantDetail(banner.targetId)
        }
        break

      case 'activity':
        // 活动页面暂未实现
        uni.showToast({
          title: '活动功能开发中',
          icon: 'none'
        })
        break

      case 'link':
        // 外部链接，暂不支持
        uni.showToast({
          title: '外部链接功能开发中',
          icon: 'none'
        })
        break

      case 'recipe':
        // 跳转到食谱详情
        if (banner.targetId) {
          toRecipeDetail(banner.targetId)
        }
        break

      default:
        // 默认不做任何操作或提示
        pageDebug.anomaly('未知轮播图类型', {
          type: banner.type
        })
    }
  } catch (error) {
    console.error('Banner跳转失败:', error)
    uni.showToast({
      title: '页面跳转失败',
      icon: 'none'
    })
  }
}

const handleBannerImageError = (banner) => {
  banner.image = DEFAULT_BANNER_IMAGE
  pageDebug.anomaly('轮播图图片加载失败', {
    bannerId: banner?.id
  })
}

const handleMerchantImageError = (merchant) => {
  merchant.logo = DEFAULT_MERCHANT_IMAGE
  pageDebug.anomaly('商家图片加载失败', {
    merchantId: merchant?.id
  })
}

const handleDishImageError = (dish) => {
  dish.image = DEFAULT_DISH_IMAGE
  pageDebug.anomaly('菜品图片加载失败', {
    dishId: dish?.id
  })
}

/**
 * 点击分类 - U-024: 跳转到分类列表页
 */
const handleCategoryClick = (category) => {
  if (!category) return
  pageDebug.action('点击分类', {
    categoryId: category.id,
    categoryName: category.name,
    categoryCode: category.code
  })

  // U-024: 跳转到分类菜品列表页
  uni.navigateTo({
    url: `${USER_DISH_LIST}?category=${encodeURIComponent(category.code || category.name)}&name=${encodeURIComponent(category.name)}`,
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
  pageDebug.action('点击更多分类')
  // 全部分类页面暂未实现
  uni.showToast({
    title: '全部分类页面开发中',
    icon: 'none'
  })
}

/**
 * 查看更多商家
 */
const toMoreMerchants = () => {
  pageDebug.action('点击更多商家')
  uni.navigateTo({
    url: USER_HOME_MERCHANT_LIST
  })
}

/**
 * 处理菜品点击 - 记录推荐反馈并跳转
 */
const handleDishClick = async (dish) => {
  if (!dish) return
  pageDebug.action('点击推荐菜品', {
    dishId: dish.id,
    dishName: dish.name,
    recommendSource: dish.recommendSource
  })

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
  pageDebug.action('首页加入购物车', {
    dishId: dish.id,
    dishName: dish.name,
    price: dish.price
  })

  // TODO: 实现添加到购物车逻辑
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })

}

/**
 * 处理筛选变化
 */
const handleFilterChange = (filterKey) => {
  if (currentFilter.value === filterKey) return

  pageDebug.action('切换推荐筛选', {
    from: currentFilter.value,
    to: filterKey
  })
  currentFilter.value = filterKey

  // 显示加载提示
  uni.showLoading({
    title: '加载中...'
  })

  // 重新加载推荐数据
  loadDishes(true).finally(() => {
    uni.hideLoading()
  })

}

// 组件挂载时加载数据
onMounted(() => {
  pageDebug.lifecycle('页面挂载', {
    isLogin: userStore.isLogin,
    hasLocation: Boolean(locationStore.currentLocation)
  })
  loadCategories()
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
  min-height: $touch-min-size;
  padding: 0 $spacing-sm;
  border: none;
  background: transparent;

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
  border: none;
  background: transparent;

  &:active {
    transform: scale(0.95);
  }

  .category-icon {
    width: 100rpx;
    height: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: $primary-100;
    border-radius: $border-radius-lg;
    font-size: 48rpx;
    box-shadow: $box-shadow-sm;
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
  box-shadow: $box-shadow-sm;
  transition: all 0.3s ease;
  border: none;
  text-align: left;

  &:active {
    transform: translateY(-4rpx);
    box-shadow: $box-shadow-md;
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
    color: $warning-color;
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
    color: $primary-700;
    background-color: $primary-100;
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
    background: linear-gradient(135deg, $primary-500 0%, $primary-700 100%);
    box-shadow: $box-shadow-md;

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
  box-shadow: $box-shadow-md;
  transition: all 0.3s ease;

  &:active {
    transform: translateY(-4rpx);
    box-shadow: $box-shadow-base;
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
      background: linear-gradient(135deg, $primary-500 0%, $primary-700 100%);
    }

    &.tag-new {
      background-color: $info-color;
    }

    &.tag-hot {
      background-color: $warning-color;
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
  background-color: $warning-50;
  border-radius: 8rpx;
  margin-bottom: $spacing-xs;

  .reason-icon {
    font-size: $font-size-sm;
  }

  .reason-text {
    font-size: $font-size-xs;
    color: $warning-color;
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
  min-width: $touch-min-size;
  min-height: $touch-min-size;
  border: none;
  position: absolute;
  bottom: 12rpx;
  right: 12rpx;
  width: 56rpx;
  height: 56rpx;
  background: linear-gradient(135deg, $primary-500 0%, $primary-700 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  box-shadow: $box-shadow-md;
  z-index: 10;

  &:active {
    transform: scale(0.9);
  }
}

button::after {
  border: none;
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
    background: linear-gradient(135deg, $primary-500 0%, $primary-700 100%);
    color: #fff;
    border-radius: 40rpx;
    font-size: $font-size-base;
    border: none;
    box-shadow: $box-shadow-md;

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
