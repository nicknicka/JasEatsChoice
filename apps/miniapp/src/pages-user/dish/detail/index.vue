<template>
  <view class="dish-detail-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 菜品图片轮播 -->
      <view class="image-section">
        <swiper
          class="image-swiper"
          :indicator-dots="dishDetail.images.length > 1"
          indicator-color="rgba(255,255,255,0.5)"
          indicator-active-color="#fff"
        >
          <swiper-item v-for="(image, index) in dishDetail.images" :key="index">
            <AppImage class="dish-image" :src="image" mode="aspectFill" :aria-label="`${dishDetail.name}图片${index + 1}`" @click="previewImage(index)" />
          </swiper-item>
        </swiper>
      </view>

      <!-- 菜品基本信息 -->
      <view class="info-section card">
        <view class="dish-header">
          <view class="dish-name">{{ dishDetail.name }}</view>
          <button class="dish-favorite" @click="toggleFavorite" :aria-label="isFavorite ? '取消收藏菜品' : '收藏菜品'">
            <text class="favorite-icon">{{ isFavorite ? '已收藏' : '收藏' }}</text>
          </button>
        </view>

        <view class="dish-description">{{ dishDetail.description }}</view>

        <view class="dish-tags">
          <text class="tag" v-for="tag in dishDetail.tags" :key="tag">{{ tag }}</text>
        </view>

        <view class="dish-bottom">
          <view class="price-section">
            <text class="price-symbol">¥</text>
            <text class="price-value">{{ dishDetail.price }}</text>
            <text class="price-original" v-if="dishDetail.originalPrice">¥{{ dishDetail.originalPrice }}</text>
          </view>
          <view class="sales-info">已售 {{ dishDetail.sales }}</view>
        </view>
      </view>

      <!-- 营养信息 -->
      <view class="nutrition-section card" v-if="dishDetail.nutrition">
        <view class="section-title">
          <text class="title-text">营养信息</text>
          <text class="title-unit">每100克</text>
        </view>
        <view class="nutrition-grid">
          <view class="nutrition-item" v-for="(value, key) in dishDetail.nutrition" :key="key">
            <view class="nutrition-icon">{{ nutritionIcons[key] }}</view>
            <view class="nutrition-value">{{ value }}</view>
            <view class="nutrition-label">{{ nutritionLabels[key] }}</view>
          </view>
        </view>
      </view>

      <!-- 食材列表 -->
      <view class="ingredients-section card" v-if="dishDetail.ingredients && dishDetail.ingredients.length > 0">
        <view class="section-title">主要食材</view>
        <scroll-view class="ingredients-scroll" scroll-x show-scrollbar="false">
          <view class="ingredients-list">
            <view class="ingredient-item" v-for="item in dishDetail.ingredients" :key="item.name">
              <AppImage class="ingredient-image" :src="item.image" mode="aspectFill" :aria-label="item.name" />
              <view class="ingredient-name">{{ item.name }}</view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 商家信息 -->
      <button class="merchant-section card" @click="toMerchant" aria-label="查看商家详情">
        <view class="merchant-info">
          <AppImage class="merchant-logo" :src="dishDetail.merchant.logo" mode="aspectFill" :aria-label="dishDetail.merchant.name" />
          <view class="merchant-detail">
            <view class="merchant-name">{{ dishDetail.merchant.name }}</view>
            <view class="merchant-rating">
              <text class="star">⭐</text>
              <text>{{ dishDetail.merchant.rating }}</text>
              <text class="merchant-sales">月售{{ dishDetail.merchant.monthlySales }}</text>
            </view>
          </view>
        </view>
        <view class="merchant-arrow">›</view>
      </button>

      <!-- 评价列表 -->
      <view class="review-section card">
        <view class="section-header">
          <text class="section-title">用户评价</text>
          <text class="review-count">{{ dishDetail.reviewCount }}条评价</text>
        </view>

        <view class="review-summary" v-if="dishDetail.reviewSummary">
          <view class="rating-overview">
            <text class="rating-score">{{ dishDetail.reviewSummary.averageRating }}</text>
            <view class="rating-stars">
              <text class="star" v-for="i in 5" :key="i">
                {{ i <= Math.floor(dishDetail.reviewSummary.averageRating) ? '⭐' : '☆' }}
              </text>
            </view>
            <text class="rating-total">{{ dishDetail.reviewCount }}条</text>
          </view>

          <view class="rating-tags">
            <text
              class="rating-tag"
              v-for="(tag, index) in dishDetail.reviewSummary.tags"
              :key="index"
            >
              {{ tag.label }} {{ tag.percentage }}%
            </text>
          </view>
        </view>

        <view class="review-list">
          <view class="review-item" v-for="review in reviews" :key="review.id">
            <view class="review-user">
              <AppImage class="user-avatar" :src="review.user.avatar" mode="aspectFill" :aria-label="review.user.name" />
              <view class="user-info">
                <view class="user-name">{{ review.user.name }}</view>
                <view class="review-stars">
                  <text class="star" v-for="i in 5" :key="i">
                    {{ i <= review.rating ? '⭐' : '☆' }}
                  </text>
                </view>
              </view>
              <view class="review-date">{{ review.date }}</view>
            </view>

            <view class="review-content">{{ review.content }}</view>

            <view class="review-images" v-if="review.images && review.images.length > 0">
              <AppImage
                class="review-image"
                v-for="(image, index) in review.images"
                :key="index"
                :src="image"
                mode="aspectFill"
                :aria-label="`${review.user.name}的评价图片${index + 1}`"
                @click="previewReviewImage(review.images, index)"
              />
            </view>

            <view class="review-merchant" v-if="review.merchantReply">
              <text class="merchant-label">商家回复：</text>
              <text class="merchant-reply">{{ review.merchantReply }}</text>
            </view>
          </view>
        </view>

        <button class="view-all-reviews" @click="viewAllReviews" aria-label="查看全部评价">
          查看全部评价 ›
        </button>
      </view>

      <!-- 相关推荐 -->
      <view class="recommend-section" v-if="recommendDishes.length > 0">
        <view class="section-title">相关推荐</view>
        <view class="dish-grid">
          <view
            class="dish-card"
            v-for="dish in recommendDishes"
            :key="dish.id"
            @click="toDishDetail(dish.id)"
          >
            <AppImage class="dish-image" :src="dish.image" mode="aspectFill" :aria-label="dish.name" />
            <view class="dish-info">
              <view class="dish-name">{{ dish.name }}</view>
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
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bar-left">
        <button class="bar-icon" @click="toCart" aria-label="查看购物车">
          <text class="icon">购物车</text>
          <view class="badge" v-if="cartCount > 0">{{ cartCount }}</view>
        </button>
        <button class="bar-icon" @click="contactMerchant" aria-label="联系商家">
          <text class="icon">联系</text>
        </button>
      </view>

      <view class="bar-right">
        <view class="quantity-control">
          <button class="quantity-btn" @click="decreaseQuantity" aria-label="减少数量">
            <text>-</text>
          </button>
          <view class="quantity-value">{{ quantity }}</view>
          <button class="quantity-btn" @click="increaseQuantity" aria-label="增加数量">
            <text>+</text>
          </button>
        </view>
        <button class="add-cart-btn" @click="addToCart" aria-label="加入购物车">
          加入购物车
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCartStore, useUserStore } from '@/store'
import { cartApi, dishApi, favoriteApi, reviewApi } from '@/api'
import { createPageDebug } from '@/utils/page-debug'
import {
  toMerchantDetail,
  toCart as goToCartPage
} from '@/utils/router'

// Store
const cartStore = useCartStore()
const userStore = useUserStore()
const pageDebug = createPageDebug('菜品详情')

// 状态
const dishId = ref('')
const merchantId = ref('')
const isFavorite = ref(false)
const quantity = ref(1)
const reviews = ref([])

// 计算属性
const cartCount = computed(() => cartStore.totalCount)

// 营养图标和标签
const nutritionIcons = {
  calories: '🔥',
  protein: '💪',
  fat: '🧈',
  carbohydrate: '🍞',
  fiber: '🥬'
}

const nutritionLabels = {
  calories: '热量',
  protein: '蛋白质',
  fat: '脂肪',
  carbohydrate: '碳水',
  fiber: '纤维'
}

// 菜品详情数据
const dishDetail = ref({
  id: '',
  name: '宫保鸡丁',
  description: '经典川菜，选用鸡胸肉配以花生米、葱花等辅料烹制而成，口感酸甜微辣，肉质鲜嫩，花生香脆，是一道下饭神器。',
  images: [
    'https://via.placeholder.com/750x750/FF6B35/FFFFFF?text=宫保鸡丁1',
    'https://via.placeholder.com/750x750/FF6B35/FFFFFF?text=宫保鸡丁2',
    'https://via.placeholder.com/750x750/FF6B35/FFFFFF?text=宫保鸡丁3'
  ],
  tags: ['川菜', '微辣', '下饭'],
  price: '28',
  originalPrice: '32',
  sales: 999,
  nutrition: {
    calories: '156',
    protein: '18.5',
    fat: '6.2',
    carbohydrate: '12.3',
    fiber: '1.2'
  },
  ingredients: [
    { name: '鸡胸肉', image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=鸡肉' },
    { name: '花生米', image: 'https://via.placeholder.com/200x200/667eea/FFFFFF?text=花生' },
    { name: '青椒', image: 'https://via.placeholder.com/200x200/52c41a/FFFFFF?text=青椒' },
    { name: '胡萝卜', image: 'https://via.placeholder.com/200x200/faad14/FFFFFF?text=胡萝卜' }
  ],
  merchant: {
    id: 1,
    name: '老王家常菜',
    logo: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王',
    rating: 4.8,
    monthlySales: 999
  },
  reviewCount: 256,
  reviewSummary: {
    averageRating: 4.7,
    tags: [
      { label: '好吃', percentage: 85 },
      { label: '分量足', percentage: 72 },
      { label: '配送快', percentage: 68 },
      { label: '味道好', percentage: 65 }
    ]
  }
})

// 推荐菜品
const recommendDishes = ref([])

/**
 * 加载菜品详情
 */
const loadDishDetail = async () => {
  try {
    pageDebug.requestStart('加载菜品详情', {
      dishId: dishId.value
    })
    uni.showLoading({ title: '加载中...' })

    // 调用后端API获取菜品详情
    const res = await dishApi.getDetail(dishId.value)
    const detail = res?.data || res || {}
    const images = Array.isArray(detail.images) && detail.images.length > 0
      ? detail.images
      : [detail.image || detail.coverImage].filter(Boolean)

    // 数据映射
    dishDetail.value = {
      id: detail.dishId || detail.id,
      name: detail.dishName || detail.name,
      description: detail.description || detail.desc || '',
      images,
      tags: detail.tags || [],
      price: detail.price ? String(detail.price) : '0',
      originalPrice: detail.originalPrice ? String(detail.originalPrice) : '',
      sales: detail.monthlySales || detail.sales || 0,
      nutrition: detail.nutrition || {},
      ingredients: detail.ingredients || [],
      merchant: {
        id: detail.merchantId || detail.merchant?.id,
        name: detail.merchantName || detail.merchant?.name || '',
        logo: detail.merchant?.logo || detail.merchant?.avatar || '',
        rating: detail.merchant?.rating || 0,
        monthlySales: detail.merchant?.monthlySales || 0
      },
      reviewCount: detail.reviewCount || 0,
      reviewSummary: detail.reviewSummary || null
    }

    // 保存商家ID
    merchantId.value = dishDetail.value.merchant.id

    // 检查收藏状态
    await checkFavorite()

    uni.hideLoading()
    pageDebug.requestSuccess('加载菜品详情', {
      dishId: dishDetail.value.id,
      merchantId: merchantId.value,
      imageCount: dishDetail.value.images.length
    })
  } catch (error) {
    pageDebug.requestFail('加载菜品详情', error)
    console.error('加载菜品详情失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 加载评价列表
 */
const loadReviews = async () => {
  try {
    pageDebug.requestStart('加载菜品评价', {
      dishId: dishId.value
    })
    const res = await reviewApi.getDishReviews(dishId.value, {
      page: 1,
      size: 3
    })

    // 数据映射
    if (Array.isArray(res)) {
      reviews.value = res.map(review => ({
        id: review.reviewId || review.id,
        userId: review.userId,
        userName: review.userName || review.user?.name || '匿名用户',
        avatar: review.userAvatar || review.user?.avatar || '',
        rating: review.rating || 5,
        content: review.content || '',
        images: review.images || [],
        tags: review.tags || [],
        reply: review.merchantReply || null,
        createTime: review.createTime || review.createdAt,
        likeCount: review.likeCount || 0
      }))
    } else {
      reviews.value = []
    }
    pageDebug.requestSuccess('加载菜品评价', {
      count: reviews.value.length
    })
  } catch (error) {
    pageDebug.requestFail('加载菜品评价', error)
    console.error('加载菜品评价失败:', error)
    reviews.value = []
  }
}

/**
 * 加载相关推荐
 */
const loadRecommendDishes = async () => {
  try {
    pageDebug.requestStart('加载相关推荐菜品', {
      dishId: dishId.value
    })
    // 调用后端API获取推荐菜品
    const params = {
      limit: 4
    }

    // 如果有用户ID，用于个性化推荐
    if (userStore.isLogin) {
      params.userId = userStore.userInfo?.userId || userStore.userInfo?.id
    }

    const res = await dishApi.getRecommend(params)

    // 数据映射
    if (Array.isArray(res)) {
      recommendDishes.value = res.map(dish => ({
        id: dish.dishId || dish.id,
        name: dish.dishName || dish.name,
        price: dish.price ? String(dish.price) : '0',
        sales: dish.monthlySales || dish.sales || 0,
        image: dish.image || dish.coverImage
      }))
      pageDebug.requestSuccess('加载相关推荐菜品', {
        count: recommendDishes.value.length
      })
    }
  } catch (error) {
    pageDebug.requestFail('加载相关推荐菜品', error)
    console.error('加载相关推荐失败:', error)
    recommendDishes.value = []
  }
}

/**
 * 预览图片
 */
const previewImage = (index) => {
  pageDebug.action('预览菜品图片', {
    index,
    imageCount: dishDetail.value.images.length
  })
  uni.previewImage({
    urls: dishDetail.value.images,
    current: index
  })
}

/**
 * 预览评价图片
 */
const previewReviewImage = (images, index) => {
  pageDebug.action('预览评价图片', {
    index,
    imageCount: images.length
  })
  uni.previewImage({
    urls: images,
    current: index
  })
}

/**
 * 检查收藏状态
 */
const checkFavorite = async () => {
  try {
    if (!userStore.isLogin) {
      isFavorite.value = false
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    const res = await favoriteApi.checkDish(dishId.value, { userId })
    isFavorite.value = res || false
    pageDebug.requestSuccess('检查菜品收藏状态', {
      dishId: dishId.value,
      isFavorite: isFavorite.value
    })
  } catch (error) {
    pageDebug.requestFail('检查菜品收藏状态', error)
    console.error('检查菜品收藏状态失败:', error)
    isFavorite.value = false
  }
}

/**
 * 切换收藏
 */
const toggleFavorite = async () => {
  try {
    pageDebug.action('切换菜品收藏', {
      dishId: dishId.value,
      current: isFavorite.value
    })
    if (!userStore.isLogin) {
      pageDebug.anomaly('菜品收藏被登录校验拦截', {
        dishId: dishId.value
      })
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      setTimeout(() => {
        uni.navigateTo({
          url: '/pages/login/index'
        })
      }, 1500)
      return
    }

    uni.showLoading({ title: '处理中...' })

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    if (isFavorite.value) {
      // 取消收藏
      await favoriteApi.removeDish(dishId.value, { userId })
      isFavorite.value = false
    } else {
      // 添加收藏
      await favoriteApi.addDish({
        userId,
        dishId: dishId.value,
        merchantId: merchantId.value
      })
      isFavorite.value = true
    }

    uni.hideLoading()
    pageDebug.requestSuccess('切换菜品收藏', {
      dishId: dishId.value,
      isFavorite: isFavorite.value
    })
    uni.showToast({
      title: isFavorite.value ? '已收藏' : '已取消收藏',
      icon: 'success'
    })
  } catch (error) {
    pageDebug.requestFail('切换菜品收藏', error)
    console.error('切换菜品收藏失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

/**
 * 跳转到商家详情
 */
const toMerchant = () => {
  pageDebug.action('进入商家详情', {
    merchantId: dishDetail.value.merchant.id,
    dishId: dishId.value
  })
  toMerchantDetail(dishDetail.value.merchant.id)
}

/**
 * 跳转到菜品详情
 */
const toDishDetail = (id) => {
  pageDebug.action('切换到相关推荐菜品', {
    fromDishId: dishId.value,
    toDishId: id
  })
  dishId.value = id
  loadDishDetail()
  loadReviews()
  loadRecommendDishes()
  // 重新滚动到顶部
  uni.pageScrollTo({
    scrollTop: 0,
    duration: 300
  })
}

/**
 * 查看全部评价 - U-012: 跳转到评价列表页
 */
const viewAllReviews = () => {
  pageDebug.action('查看全部菜品评价', {
    dishId: dishId.value,
    merchantId: merchantId.value
  })
  if (!dishId.value) {
    uni.showToast({
      title: '菜品信息不存在',
      icon: 'none'
    })
    return
  }

  // 跳转到菜品评价列表页面
  uni.navigateTo({
    url: `/pages-user/review/list/index?dishId=${dishId.value}&merchantId=${merchantId.value}`,
    fail: (err) => {
      console.error('跳转评价列表页失败:', err)
      uni.showToast({
        title: '打开评价页面失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 跳转到购物车 - U-013: 跳转到购物车页
 */
const toCart = () => {
  pageDebug.action('从菜品页进入购物车', {
    dishId: dishId.value,
    cartCount: cartCount.value
  })
  goToCartPage()
}

/**
 * 联系商家 - U-014: 跳转到聊天页
 */
const contactMerchant = async () => {
  try {
    pageDebug.action('联系商家', {
      dishId: dishId.value,
      merchantId: merchantId.value
    })
    if (!merchantId.value) {
      uni.showToast({
        title: '商家信息不存在',
        icon: 'none'
      })
      return
    }

    // 导入聊天API
    const { chatApi } = await import('@/api')

    // 调用API创建或获取与商家的会话
    const res = await chatApi.createConversation({
      targetUserId: merchantId.value,
      dishId: dishId.value
    })

    if (res.code === 200 || res.conversationId) {
      const conversationId = res.conversationId || res.data?.conversationId || res.id

      // 跳转到聊天页面
      uni.navigateTo({
        url: `/pages-common/chat/chat-room?conversationId=${conversationId}&merchantId=${merchantId.value}`,
        fail: () => {
          // 如果第一个路径失败，尝试其他可能的路径
          uni.navigateTo({
            url: `/pages-user/chat/chat-room?conversationId=${conversationId}&merchantId=${merchantId.value}`
          })
        }
      })
    } else {
      throw new Error(res.message || '创建会话失败')
    }
  } catch (error) {
    pageDebug.requestFail('联系商家', error)
    console.error('跳转聊天页面失败:', error)
    uni.showToast({
      title: error.message || '打开聊天失败',
      icon: 'none'
    })
  }
}

/**
 * 增加数量
 */
const increaseQuantity = () => {
  quantity.value++
  pageDebug.state('增加菜品数量', {
    quantity: quantity.value
  })
}

/**
 * 减少数量
 */
const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
    pageDebug.state('减少菜品数量', {
      quantity: quantity.value
    })
  }
}

/**
 * 加入购物车 - U-015: 调用后端API
 */
const addToCart = async () => {
  try {
    pageDebug.action('菜品详情加入购物车', {
      dishId: dishId.value,
      quantity: quantity.value
    })
    // 调用后端购物车API
    await cartApi.add({
      dishId: dishId.value,
      merchantId: dishDetail.value.merchant.id,
      quantity: quantity.value,
      spec: '',
      remark: ''
    })

    // 同时更新本地store以保持一致性
    cartStore.addToCart({
      merchantId: dishDetail.value.merchant.id,
      merchantName: dishDetail.value.merchant.name,
      dish: {
        id: dishDetail.value.id,
        name: dishDetail.value.name,
        price: parseFloat(dishDetail.value.price),
        image: dishDetail.value.images[0]
      },
      quantity: quantity.value
    })

    uni.showToast({
      title: '已加入购物车',
      icon: 'success',
      duration: 2000
    })

    // 重置数量
    quantity.value = 1
    pageDebug.requestSuccess('菜品详情加入购物车', {
      dishId: dishId.value,
      cartCount: cartCount.value
    })
  } catch (error) {
    pageDebug.requestFail('菜品详情加入购物车', error)
    console.error('加入购物车失败:', error)
    uni.showToast({
      title: error.message || '添加失败',
      icon: 'none'
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  pageDebug.lifecycle('页面挂载')
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.id) {
    dishId.value = options.id
    pageDebug.state('读取菜品页面参数', {
      dishId: dishId.value
    })
  }

  // 加载数据
  loadDishDetail()
  loadReviews()
  loadRecommendDishes()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-detail-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

.scroll-container {
  height: 100vh;
}

/* 卡片通用样式 */
.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

/* 图片轮播 */
.image-section {
  background-color: $bg-color-white;
}

.image-swiper {
  width: 100%;
  height: 750rpx;
}

.dish-image {
  width: 100%;
  height: 100%;
}

/* 菜品信息 */
.info-section {
  .dish-header {
    @include flex-between;
    margin-bottom: $spacing-md;
  }

  .dish-name {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
    flex: 1;
    padding-right: $spacing-md;
  }

  .dish-favorite {
    border: none;
    background: transparent;

    .favorite-icon {
      font-size: $font-size-sm;
      color: $primary-color;
      font-weight: $font-weight-medium;
    }
  }

  .dish-description {
    font-size: $font-size-base;
    color: $text-color-regular;
    line-height: $line-height-lg;
    margin-bottom: $spacing-md;
  }

  .dish-tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
    margin-bottom: $spacing-md;

    .tag {
      font-size: $font-size-sm;
      color: $primary-color;
      background-color: $primary-100;
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
    }
  }

  .dish-bottom {
    @include flex-between;
  }

  .price-section {
    @include flex-center;
    gap: 4rpx;
    color: $danger-color;

    .price-symbol {
      font-size: $font-size-base;
    }

    .price-value {
      font-size: $font-size-xxl;
      font-weight: $font-weight-bold;
    }

    .price-original {
      font-size: $font-size-sm;
      color: $text-color-secondary;
      text-decoration: line-through;
      margin-left: $spacing-xs;
    }
  }

  .sales-info {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}

/* 营养信息 */
.nutrition-section {
  .section-title {
    @include flex-between;
    margin-bottom: $spacing-md;
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;

    .title-unit {
      font-size: $font-size-sm;
      color: $text-color-secondary;
      font-weight: $font-weight-normal;
    }
  }

  .nutrition-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: $spacing-md;
  }

  .nutrition-item {
    @include flex-center-column;
    gap: $spacing-xs;

    .nutrition-icon {
      font-size: 48rpx;
    }

    .nutrition-value {
      font-size: $font-size-base;
      font-weight: $font-weight-medium;
      color: $text-color-primary;
    }

    .nutrition-label {
      font-size: $font-size-xs;
      color: $text-color-secondary;
    }
  }
}

/* 食材列表 */
.ingredients-section {
  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
    margin-bottom: $spacing-md;
  }

  .ingredients-scroll {
    white-space: nowrap;
  }

  .ingredients-list {
    display: flex;
    gap: $spacing-md;
  }

  .ingredient-item {
    @include flex-center-column;
    gap: $spacing-xs;
    flex-shrink: 0;

    .ingredient-image {
      width: 120rpx;
      height: 120rpx;
      border-radius: $border-radius-base;
    }

    .ingredient-name {
      font-size: $font-size-sm;
      color: $text-color-regular;
    }
  }
}

/* 商家信息 */
.merchant-section {
  @include flex-between;
  align-items: center;
  width: 100%;
  border: none;
  background-color: $bg-color-white;
  text-align: left;
}

.merchant-info {
  @include flex-center;
  gap: $spacing-md;
  flex: 1;
}

.merchant-logo {
  width: 100rpx;
  height: 100rpx;
  border-radius: $border-radius-base;
}

.merchant-detail {
  flex: 1;
}

.merchant-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
}

.merchant-rating {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;

  .star {
    color: $warning-color;
  }

  .merchant-sales {
    color: $text-color-secondary;
  }
}

.merchant-arrow {
  font-size: 48rpx;
  color: $text-color-secondary;
}

/* 评价列表 */
.review-section {
  .section-header {
    @include flex-between;
    margin-bottom: $spacing-md;
  }

  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
  }

  .review-count {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .review-summary {
    padding: $spacing-md;
    background-color: $bg-color-base;
    border-radius: $border-radius-base;
    margin-bottom: $spacing-md;
  }

  .rating-overview {
    @include flex-center;
    gap: $spacing-md;
    margin-bottom: $spacing-md;

    .rating-score {
      font-size: 48rpx;
      font-weight: $font-weight-bold;
      color: $text-color-primary;
    }

    .rating-stars {
      @include flex-center;
      gap: 4rpx;

      .star {
        font-size: $font-size-base;
        color: $warning-color;
      }
    }

    .rating-total {
      font-size: $font-size-sm;
      color: $text-color-secondary;
    }
  }

  .rating-tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;

    .rating-tag {
      font-size: $font-size-sm;
      color: $text-color-regular;
      background-color: $bg-color-white;
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
    }
  }

  .review-list {
    .review-item {
      padding: $spacing-md 0;
      border-bottom: 1rpx solid $border-color-light;

      &:last-child {
        border-bottom: none;
      }
    }

    .review-user {
      @include flex-between;
      margin-bottom: $spacing-sm;
    }

    .user-avatar {
      width: 72rpx;
      height: 72rpx;
      border-radius: 50%;
    }

    .user-info {
      flex: 1;
      margin-left: $spacing-sm;
    }

    .user-name {
      font-size: $font-size-base;
      color: $text-color-primary;
      margin-bottom: $spacing-xs;
    }

    .review-stars {
      .star {
        font-size: $font-size-sm;
        color: $warning-color;
      }
    }

    .review-date {
      font-size: $font-size-sm;
      color: $text-color-secondary;
    }

    .review-content {
      font-size: $font-size-base;
      color: $text-color-regular;
      line-height: $line-height-lg;
      margin-bottom: $spacing-sm;
    }

    .review-images {
      display: flex;
      gap: $spacing-sm;
      margin-bottom: $spacing-sm;
    }

    .review-image {
      width: 160rpx;
      height: 160rpx;
      border-radius: $border-radius-base;
    }

    .review-merchant {
      padding: $spacing-sm;
      background-color: $bg-color-base;
      border-radius: $border-radius-base;
      font-size: $font-size-sm;

      .merchant-label {
        color: $text-color-secondary;
      }

      .merchant-reply {
        color: $text-color-regular;
      }
    }
  }

  .view-all-reviews {
    text-align: center;
    padding: $spacing-md;
    color: $primary-color;
    font-size: $font-size-base;
    border: none;
    background: transparent;
    width: 100%;
  }
}

/* 相关推荐 */
.recommend-section {
  padding: $spacing-md;

  .section-title {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
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
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include flex-between;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: $box-shadow-md;
  z-index: $z-index-fixed;
}

.bar-left {
  @include flex-center;
  gap: $spacing-lg;
}

.bar-icon {
  position: relative;
  @include flex-center-column;
  gap: 4rpx;
  min-width: $touch-min-size;
  min-height: $touch-min-size;
  border: none;
  background: transparent;

  .icon {
    font-size: $font-size-sm;
    color: $text-color-primary;
  }

  .badge {
    position: absolute;
    top: 0;
    right: -8rpx;
    min-width: 32rpx;
    height: 32rpx;
    padding: 0 8rpx;
    background-color: $danger-color;
    color: #fff;
    font-size: $font-size-xs;
    line-height: 32rpx;
    text-align: center;
    border-radius: 16rpx;
  }
}

.bar-right {
  @include flex-center;
  gap: $spacing-md;
  flex: 1;
}

.quantity-control {
  @include flex-center;
  gap: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  padding: $spacing-xs;

  .quantity-btn {
    width: $touch-min-size;
    height: $touch-min-size;
    @include flex-center;
    background-color: $bg-color-white;
    border-radius: $border-radius-sm;
    font-size: $font-size-lg;
    color: $text-color-primary;
    border: none;
  }

  .quantity-value {
    width: 80rpx;
    text-align: center;
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }
}

.add-cart-btn {
  flex: 1;
  min-height: $touch-min-size;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-base;
  border: none;
}

button::after {
  border: none;
}
</style>
