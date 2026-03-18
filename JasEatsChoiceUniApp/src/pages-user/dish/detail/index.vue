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
            <image class="dish-image" :src="image" mode="aspectFill" @click="previewImage(index)" />
          </swiper-item>
        </swiper>
      </view>

      <!-- 菜品基本信息 -->
      <view class="info-section card">
        <view class="dish-header">
          <view class="dish-name">{{ dishDetail.name }}</view>
          <view class="dish-favorite" @click="toggleFavorite">
            <text class="favorite-icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
          </view>
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
              <image class="ingredient-image" :src="item.image" mode="aspectFill" />
              <view class="ingredient-name">{{ item.name }}</view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 商家信息 -->
      <view class="merchant-section card" @click="toMerchant">
        <view class="merchant-info">
          <image class="merchant-logo" :src="dishDetail.merchant.logo" mode="aspectFill" />
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
      </view>

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
              <image class="user-avatar" :src="review.user.avatar" mode="aspectFill" />
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
              <image
                class="review-image"
                v-for="(image, index) in review.images"
                :key="index"
                :src="image"
                mode="aspectFill"
                @click="previewReviewImage(review.images, index)"
              />
            </view>

            <view class="review-merchant" v-if="review.merchantReply">
              <text class="merchant-label">商家回复：</text>
              <text class="merchant-reply">{{ review.merchantReply }}</text>
            </view>
          </view>
        </view>

        <view class="view-all-reviews" @click="viewAllReviews">
          查看全部评价 ›
        </view>
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
            <image class="dish-image" :src="dish.image" mode="aspectFill" />
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
        <view class="bar-icon" @click="toCart">
          <text class="icon">🛒</text>
          <view class="badge" v-if="cartCount > 0">{{ cartCount }}</view>
        </view>
        <view class="bar-icon" @click="contactMerchant">
          <text class="icon">💬</text>
        </view>
      </view>

      <view class="bar-right">
        <view class="quantity-control">
          <view class="quantity-btn" @click="decreaseQuantity">
            <text>-</text>
          </view>
          <view class="quantity-value">{{ quantity }}</view>
          <view class="quantity-btn" @click="increaseQuantity">
            <text>+</text>
          </view>
        </view>
        <view class="add-cart-btn" @click="addToCart">
          加入购物车
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCartStore } from '@/store'

// Store
const cartStore = useCartStore()

// 状态
const dishId = ref('')
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
    // TODO: 调用后端API
    // const res = await dishApi.getDetail(dishId.value)
    // dishDetail.value = res.data

    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
  } catch (error) {
    console.error('加载菜品详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 加载评价列表
 */
const loadReviews = async () => {
  try {
    // TODO: 调用后端API
    // const res = await reviewApi.getList({
    //   dishId: dishId.value,
    //   page: 1,
    //   size: 3
    // })
    // reviews.value = res.data.list

    // 模拟数据
    reviews.value = [
      {
        id: 1,
        user: {
          avatar: 'https://via.placeholder.com/100x100/FF6B35/FFFFFF?text=U1',
          name: '用户***8'
        },
        rating: 5,
        date: '2026-03-15',
        content: '味道很好，鸡肉嫩滑，花生香脆，非常下饭！分量也很足，下次还会再点。',
        images: [
          'https://via.placeholder.com/400x400/FF6B35/FFFFFF?text=评价1'
        ],
        merchantReply: '感谢您的好评，我们会继续努力提供美味的菜品！'
      },
      {
        id: 2,
        user: {
          avatar: 'https://via.placeholder.com/100x100/667eea/FFFFFF?text=U2',
          name: '用户***2'
        },
        rating: 4,
        date: '2026-03-14',
        content: '味道不错，就是稍微有点辣，不过还在可接受范围内。',
        images: []
      },
      {
        id: 3,
        user: {
          avatar: 'https://via.placeholder.com/100x100/52c41a/FFFFFF?text=U3',
          name: '用户***3'
        },
        rating: 5,
        date: '2026-03-13',
        content: '配送很快，包装也很好，菜品的味道和分量都对得起这个价格。',
        images: []
      }
    ]
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

/**
 * 加载相关推荐
 */
const loadRecommendDishes = async () => {
  try {
    // TODO: 调用后端API
    // const res = await dishApi.getRecommend({
    //   dishId: dishId.value,
    //   limit: 4
    // })
    // recommendDishes.value = res.data

    // 模拟数据
    recommendDishes.value = [
      {
        id: 2,
        name: '鱼香肉丝',
        price: '26',
        sales: 888,
        image: 'https://via.placeholder.com/300x300/667eea/FFFFFF?text=鱼香肉丝'
      },
      {
        id: 3,
        name: '回锅肉',
        price: '32',
        sales: 777,
        image: 'https://via.placeholder.com/300x300/52c41a/FFFFFF?text=回锅肉'
      },
      {
        id: 4,
        name: '麻婆豆腐',
        price: '18',
        sales: 666,
        image: 'https://via.placeholder.com/300x300/faad14/FFFFFF?text=麻婆豆腐'
      },
      {
        id: 5,
        name: '水煮鱼',
        price: '38',
        sales: 555,
        image: 'https://via.placeholder.com/300x300/FF6B35/FFFFFF?text=水煮鱼'
      }
    ]
  } catch (error) {
    console.error('加载推荐失败:', error)
  }
}

/**
 * 预览图片
 */
const previewImage = (index) => {
  uni.previewImage({
    urls: dishDetail.value.images,
    current: index
  })
}

/**
 * 预览评价图片
 */
const previewReviewImage = (images, index) => {
  uni.previewImage({
    urls: images,
    current: index
  })
}

/**
 * 切换收藏
 */
const toggleFavorite = async () => {
  try {
    // TODO: 调用后端API
    // await favoriteApi.toggle({
    //   type: 'dish',
    //   id: dishId.value
    // })

    isFavorite.value = !isFavorite.value

    uni.showToast({
      title: isFavorite.value ? '已收藏' : '已取消收藏',
      icon: 'success'
    })
  } catch (error) {
    console.error('收藏失败:', error)
  }
}

/**
 * 跳转到商家详情
 */
const toMerchant = () => {
  uni.navigateTo({
    url: `/pages/merchant/detail/index?id=${dishDetail.value.merchant.id}`
  })
}

/**
 * 跳转到菜品详情
 */
const toDishDetail = (id) => {
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
 * 查看全部评价
 */
const viewAllReviews = () => {
  uni.showToast({
    title: '评价列表页开发中',
    icon: 'none'
  })
  // TODO: 跳转到评价列表页
  // uni.navigateTo({
  //   url: `/pages/review/list/index?dishId=${dishId.value}`
  // })
}

/**
 * 跳转到购物车
 */
const toCart = () => {
  uni.showToast({
    title: '购物车功能开发中',
    icon: 'none'
  })
  // TODO: 跳转到购物车页
}

/**
 * 联系商家
 */
const contactMerchant = () => {
  uni.showToast({
    title: '聊天功能开发中',
    icon: 'none'
  })
  // TODO: 跳转到聊天页
}

/**
 * 增加数量
 */
const increaseQuantity = () => {
  quantity.value++
}

/**
 * 减少数量
 */
const decreaseQuantity = () => {
  if (quantity.value > 1) {
    quantity.value--
  }
}

/**
 * 加入购物车
 */
const addToCart = async () => {
  try {
    // TODO: 调用后端API
    // await cartApi.add({
    //   dishId: dishId.value,
    //   quantity: quantity.value,
    //   merchantId: dishDetail.value.merchant.id
    // })

    // 使用store添加到购物车
    cartStore.addToCart({
      merchantId: dishDetail.value.merchant.id,
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
      icon: 'success'
    })

    // 重置数量
    quantity.value = 1
  } catch (error) {
    console.error('加入购物车失败:', error)
    uni.showToast({
      title: '添加失败',
      icon: 'none'
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.id) {
    dishId.value = options.id
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
    .favorite-icon {
      font-size: 48rpx;
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
      background-color: rgba(255, 107, 53, 0.1);
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
    color: #f5a623;
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
        color: #f5a623;
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
        color: #f5a623;
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
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
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

  .icon {
    font-size: 48rpx;
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
    width: 56rpx;
    height: 56rpx;
    @include flex-center;
    background-color: $bg-color-white;
    border-radius: $border-radius-sm;
    font-size: $font-size-lg;
    color: $text-color-primary;
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
  height: 72rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-base;
}
</style>
