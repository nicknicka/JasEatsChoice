<template>
  <view class="merchant-detail-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 商家头部信息 -->
      <view class="merchant-header">
        <view class="header-bg"></view>

        <view class="merchant-info-card">
          <image class="merchant-logo" :src="merchantDetail.logo" mode="aspectFill" />

          <view class="merchant-basic">
            <view class="merchant-name">{{ merchantDetail.name }}</view>
            <view class="merchant-rating">
              <text class="star">⭐</text>
              <text class="rating-score">{{ merchantDetail.rating }}</text>
              <text class="rating-count">（{{ merchantDetail.reviewCount }}条评价）</text>
            </view>

            <view class="merchant-tags">
              <text class="tag" v-for="tag in merchantDetail.tags" :key="tag">{{ tag }}</text>
            </view>

            <view class="merchant-stats">
              <view class="stat-item">
                <text class="stat-value">{{ merchantDetail.monthlySales }}</text>
                <text class="stat-label">月售</text>
              </view>
              <view class="stat-divider"></view>
              <view class="stat-item">
                <text class="stat-value">{{ merchantDetail.dishCount }}</text>
                <text class="stat-label">菜品</text>
              </view>
              <view class="stat-divider"></view>
              <view class="stat-item">
                <text class="stat-value">{{ merchantDetail.deliveryTime }}</text>
                <text class="stat-label">分钟</text>
              </view>
            </view>
          </view>

          <view class="merchant-actions">
            <view class="action-btn" @click="toggleFavorite">
              <text class="action-icon">{{ isFavorite ? '❤️' : '🤍' }}</text>
              <text class="action-text">{{ isFavorite ? '已收藏' : '收藏' }}</text>
            </view>
            <view class="action-btn" @click="shareMerchant">
              <text class="action-icon">📤</text>
              <text class="action-text">分享</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 优惠券 -->
      <view class="coupon-section card" v-if="coupons.length > 0">
        <view class="section-title">店铺优惠</view>
        <scroll-view class="coupon-scroll" scroll-x show-scrollbar="false">
          <view class="coupon-list">
            <view
              class="coupon-item"
              v-for="coupon in coupons"
              :key="coupon.id"
              @click="receiveCoupon(coupon)"
            >
              <view class="coupon-left">
                <text class="coupon-amount">¥{{ coupon.amount }}</text>
                <text class="coupon-condition">{{ coupon.condition }}</text>
              </view>
              <view class="coupon-right">
                <text class="coupon-btn">{{ coupon.received ? '已领取' : '领取' }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 商家公告 -->
      <view class="notice-section card" v-if="merchantDetail.notice">
        <view class="notice-icon">📢</view>
        <view class="notice-content">{{ merchantDetail.notice }}</view>
      </view>

      <!-- 菜品分类Tab -->
      <view class="category-tabs">
        <scroll-view class="tabs-scroll" scroll-x show-scrollbar="false">
          <view class="tabs-list">
            <view
              class="tab-item"
              :class="{ active: activeCategory === category.id }"
              v-for="category in categories"
              :key="category.id"
              @click="switchCategory(category.id)"
            >
              {{ category.name }}
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 菜品列表 -->
      <view class="dish-list-section">
        <view class="dish-list">
          <view
            class="dish-item"
            v-for="dish in currentDishes"
            :key="dish.id"
            @click="toDishDetail(dish.id)"
          >
            <image class="dish-image" :src="dish.image" mode="aspectFill" />

            <view class="dish-info">
              <view class="dish-name">{{ dish.name }}</view>
              <view class="dish-description">{{ dish.description }}</view>

              <view class="dish-bottom">
                <view class="price-section">
                  <text class="price-symbol">¥</text>
                  <text class="price-value">{{ dish.price }}</text>
                  <text class="price-original" v-if="dish.originalPrice">¥{{ dish.originalPrice }}</text>
                </view>
                <view class="sales-info">月售{{ dish.monthlySales }}</view>
              </view>

              <view class="dish-tags" v-if="dish.tags && dish.tags.length > 0">
                <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
              </view>
            </view>

            <view class="dish-action">
              <view class="add-btn" @click.stop="quickAdd(dish)">+</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 商家评价 -->
      <view class="review-section card">
        <view class="section-header">
          <text class="section-title">用户评价</text>
          <text class="review-count">{{ merchantDetail.reviewCount }}条</text>
        </view>

        <view class="review-summary">
          <view class="rating-overview">
            <text class="rating-score">{{ merchantDetail.rating }}</text>
            <view class="rating-stars">
              <text class="star" v-for="i in 5" :key="i">
                {{ i <= Math.floor(merchantDetail.rating) ? '⭐' : '☆' }}
              </text>
            </view>
          </view>

          <view class="rating-tags">
            <text class="rating-tag" v-for="tag in reviewTags" :key="tag.label">
              {{ tag.label }} {{ tag.count }}条
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

            <view class="review-dishes" v-if="review.dishes && review.dishes.length > 0">
              <text class="dish-tag" v-for="dish in review.dishes" :key="dish">{{ dish }}</text>
            </view>
          </view>
        </view>

        <view class="view-all-reviews" @click="viewAllReviews">
          查看全部评价 ›
        </view>
      </view>

      <!-- 商家信息 -->
      <view class="merchant-info-section card">
        <view class="section-title">商家信息</view>

        <view class="info-item">
          <text class="info-label">营业时间</text>
          <text class="info-value">{{ merchantDetail.businessHours }}</text>
        </view>

        <view class="info-item">
          <text class="info-label">配送时间</text>
          <text class="info-value">{{ merchantDetail.deliveryTime }}分钟</text>
        </view>

        <view class="info-item">
          <text class="info-label">配送费</text>
          <text class="info-value">¥{{ merchantDetail.deliveryFee }}</text>
        </view>

        <view class="info-item">
          <text class="info-label">起送价</text>
          <text class="info-value">¥{{ merchantDetail.minOrderAmount }}</text>
        </view>

        <view class="info-item">
          <text class="info-label">商家地址</text>
          <text class="info-value">{{ merchantDetail.address }}</text>
        </view>

        <view class="info-item" @click="callMerchant">
          <text class="info-label">联系电话</text>
          <text class="info-value phone">{{ merchantDetail.phone }} ›</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bar-left">
        <view class="bar-icon" @click="toCart">
          <text class="icon">🛒</text>
          <view class="badge" v-if="cartCount > 0">{{ cartCount }}</view>
          <view class="cart-total" v-if="cartTotal > 0">¥{{ cartTotal }}</view>
        </view>
      </view>

      <view class="bar-right">
        <view class="start-order-btn" @click="startOrder">
          去结算
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
const merchantId = ref('')
const isFavorite = ref(false)
const activeCategory = ref('all')

// 计算属性
const cartCount = computed(() => cartStore.totalCount)
const cartTotal = computed(() => cartStore.totalPrice.toFixed(2))

// 商家详情数据
const merchantDetail = ref({
  id: '',
  name: '老王家常菜',
  logo: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王',
  rating: 4.8,
  reviewCount: 256,
  tags: ['家常菜', '配送快', '好评多'],
  monthlySales: 999,
  dishCount: 35,
  deliveryTime: 30,
  notice: '本店所有菜品均为现做，如需加急请提前联系，感谢您的理解与支持！',
  businessHours: '09:00-22:00',
  deliveryFee: 5,
  minOrderAmount: 20,
  address: 'XX市XX区XX街道XX号',
  phone: '13800138000'
})

// 优惠券数据
const coupons = ref([
  {
    id: 1,
    amount: 10,
    condition: '满50可用',
    received: false
  },
  {
    id: 2,
    amount: 5,
    condition: '满30可用',
    received: true
  },
  {
    id: 3,
    amount: 20,
    condition: '满100可用',
    received: false
  }
])

// 菜品分类
const categories = ref([
  { id: 'all', name: '全部' },
  { id: 'hot', name: '热销' },
  { id: 'recommend', name: '推荐' },
  { id: 'meat', name: '荤菜' },
  { id: 'vegetable', name: '素菜' },
  { id: 'soup', name: '汤类' },
  { id: 'staple', name: '主食' }
])

// 菜品列表
const dishes = ref({
  all: [],
  hot: [],
  recommend: [],
  meat: [],
  vegetable: [],
  soup: [],
  staple: []
})

// 当前分类的菜品
const currentDishes = computed(() => {
  return dishes.value[activeCategory.value] || []
})

// 评价标签
const reviewTags = ref([
  { label: '味道好', count: 156 },
  { label: '分量足', count: 128 },
  { label: '配送快', count: 98 },
  { label: '卫生好', count: 87 }
])

// 评价列表
const reviews = ref([])

/**
 * 加载商家详情
 */
const loadMerchantDetail = async () => {
  try {
    // TODO: 调用后端API
    // const res = await merchantApi.getDetail(merchantId.value)
    // merchantDetail.value = res.data

    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
  } catch (error) {
    console.error('加载商家详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 加载优惠券
 */
const loadCoupons = async () => {
  try {
    // TODO: 调用后端API
    // const res = await couponApi.getList({ merchantId: merchantId.value })
    // coupons.value = res.data

    // 模拟数据已在上面定义
  } catch (error) {
    console.error('加载优惠券失败:', error)
  }
}

/**
 * 加载菜品列表
 */
const loadDishes = async () => {
  try {
    // TODO: 调用后端API
    // const res = await dishApi.getList({ merchantId: merchantId.value })
    // dishes.value = res.data

    // 模拟数据 - 全部菜品
    const allDishes = [
      {
        id: 1,
        name: '宫保鸡丁',
        description: '经典川菜，酸甜可口',
        price: '28',
        originalPrice: '32',
        monthlySales: 999,
        image: 'https://via.placeholder.com/300x300/FF6B35/FFFFFF?text=宫保鸡丁',
        tags: ['热销', '推荐']
      },
      {
        id: 2,
        name: '鱼香肉丝',
        description: '传统川菜，下饭神器',
        price: '26',
        monthlySales: 888,
        image: 'https://via.placeholder.com/300x300/667eea/FFFFFF?text=鱼香肉丝',
        tags: ['热销']
      },
      {
        id: 3,
        name: '回锅肉',
        description: '四川名菜，肥而不腻',
        price: '32',
        monthlySales: 777,
        image: 'https://via.placeholder.com/300x300/52c41a/FFFFFF?text=回锅肉',
        tags: ['荤菜', '推荐']
      },
      {
        id: 4,
        name: '麻婆豆腐',
        description: '麻辣鲜香，嫩滑爽口',
        price: '18',
        monthlySales: 666,
        image: 'https://via.placeholder.com/300x300/faad14/FFFFFF?text=麻婆豆腐',
        tags: ['素菜', '热销']
      },
      {
        id: 5,
        name: '番茄鸡蛋汤',
        description: '酸甜开胃，营养丰富',
        price: '12',
        monthlySales: 555,
        image: 'https://via.placeholder.com/300x300/FF6B35/FFFFFF?text=番茄蛋汤',
        tags: ['汤类']
      },
      {
        id: 6,
        name: '米饭',
        description: '优质大米',
        price: '2',
        monthlySales: 1200,
        image: 'https://via.placeholder.com/300x300/667eea/FFFFFF?text=米饭',
        tags: ['主食', '热销']
      }
    ]

    // 按分类分配
    dishes.value.all = allDishes
    dishes.value.hot = allDishes.filter(d => d.tags.includes('热销'))
    dishes.value.recommend = allDishes.filter(d => d.tags.includes('推荐'))
    dishes.value.meat = allDishes.filter(d => d.tags.includes('荤菜'))
    dishes.value.vegetable = allDishes.filter(d => d.tags.includes('素菜'))
    dishes.value.soup = allDishes.filter(d => d.tags.includes('汤类'))
    dishes.value.staple = allDishes.filter(d => d.tags.includes('主食'))
  } catch (error) {
    console.error('加载菜品失败:', error)
  }
}

/**
 * 加载评价列表
 */
const loadReviews = async () => {
  try {
    // TODO: 调用后端API
    // const res = await reviewApi.getList({
    //   merchantId: merchantId.value,
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
        content: '味道很好，分量足，配送也快，好评！',
        dishes: ['宫保鸡丁', '麻婆豆腐']
      },
      {
        id: 2,
        user: {
          avatar: 'https://via.placeholder.com/100x100/667eea/FFFFFF?text=U2',
          name: '用户***2'
        },
        rating: 4,
        date: '2026-03-14',
        content: '整体不错，就是有一道菜有点咸。',
        dishes: ['鱼香肉丝']
      },
      {
        id: 3,
        user: {
          avatar: 'https://via.placeholder.com/100x100/52c41a/FFFFFF?text=U3',
          name: '用户***3'
        },
        rating: 5,
        date: '2026-03-13',
        content: '老顾客了，一直很喜欢这家店的味道。',
        dishes: ['回锅肉', '番茄鸡蛋汤']
      }
    ]
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

/**
 * 切换分类
 */
const switchCategory = (categoryId) => {
  activeCategory.value = categoryId
}

/**
 * 切换收藏
 */
const toggleFavorite = async () => {
  try {
    // TODO: 调用后端API
    // await favoriteApi.toggle({
    //   type: 'merchant',
    //   id: merchantId.value
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
 * 分享商家
 */
const shareMerchant = () => {
  uni.showToast({
    title: '分享功能开发中',
    icon: 'none'
  })
  // TODO: 实现分享功能
}

/**
 * 领取优惠券
 */
const receiveCoupon = async (coupon) => {
  if (coupon.received) return

  try {
    // TODO: 调用后端API
    // await couponApi.receive(coupon.id)

    coupon.received = true

    uni.showToast({
      title: '领取成功',
      icon: 'success'
    })
  } catch (error) {
    console.error('领取优惠券失败:', error)
    uni.showToast({
      title: '领取失败',
      icon: 'none'
    })
  }
}

/**
 * 跳转到菜品详情
 */
const toDishDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages/dish/detail/index?id=${dishId}`
  })
}

/**
 * 快速添加到购物车
 */
const quickAdd = async (dish) => {
  try {
    // 使用store添加到购物车
    cartStore.addToCart({
      merchantId: merchantId.value,
      dish: {
        id: dish.id,
        name: dish.name,
        price: parseFloat(dish.price),
        image: dish.image
      },
      quantity: 1
    })

    uni.showToast({
      title: '已加入购物车',
      icon: 'success',
      duration: 1500
    })
  } catch (error) {
    console.error('加入购物车失败:', error)
  }
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
}

/**
 * 拨打商家电话
 */
const callMerchant = () => {
  uni.makePhoneCall({
    phoneNumber: merchantDetail.value.phone
  })
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
 * 去结算
 */
const startOrder = () => {
  if (cartCount.value === 0) {
    uni.showToast({
      title: '请先添加菜品',
      icon: 'none'
    })
    return
  }

  uni.showToast({
    title: '订单确认页开发中',
    icon: 'none'
  })
  // TODO: 跳转到订单确认页
  // uni.navigateTo({
  //   url: '/pages/order/confirm/index'
  // })
}

// 组件挂载时加载数据
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.id) {
    merchantId.value = options.id
  }

  // 加载数据
  loadMerchantDetail()
  loadCoupons()
  loadDishes()
  loadReviews()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.merchant-detail-container {
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

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;
}

/* 商家头部 */
.merchant-header {
  position: relative;
  padding-bottom: $spacing-md;
}

.header-bg {
  height: 200rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.merchant-info-card {
  position: relative;
  margin: -60rpx $spacing-md 0;
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  box-shadow: $box-shadow-base;
}

.merchant-logo {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
  position: absolute;
  top: -$spacing-lg;
  left: $spacing-md;
  border: 4rpx solid $bg-color-white;
}

.merchant-basic {
  margin-left: 140rpx;
  margin-bottom: $spacing-md;
}

.merchant-name {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
}

.merchant-rating {
  @include flex-center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  margin-bottom: $spacing-sm;

  .star {
    color: #f5a623;
  }

  .rating-score {
    font-weight: $font-weight-bold;
    color: $text-color-primary;
  }

  .rating-count {
    color: $text-color-secondary;
  }
}

.merchant-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-xs;
  margin-bottom: $spacing-md;

  .tag {
    font-size: $font-size-xs;
    color: $primary-color;
    background-color: rgba(255, 107, 53, 0.1);
    padding: 6rpx 12rpx;
    border-radius: 6rpx;
  }
}

.merchant-stats {
  @include flex-center;
  gap: $spacing-md;
}

.stat-item {
  @include flex-center-column;
  gap: 4rpx;

  .stat-value {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $text-color-primary;
  }

  .stat-label {
    font-size: $font-size-xs;
    color: $text-color-secondary;
  }
}

.stat-divider {
  width: 1rpx;
  height: 40rpx;
  background-color: $border-color-light;
}

.merchant-actions {
  @include flex-center;
  gap: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1rpx solid $border-color-light;
}

.action-btn {
  flex: 1;
  @include flex-center-column;
  gap: 4rpx;
  padding: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;

  .action-icon {
    font-size: 36rpx;
  }

  .action-text {
    font-size: $font-size-sm;
    color: $text-color-regular;
  }
}

/* 优惠券 */
.coupon-section {
  .coupon-scroll {
    white-space: nowrap;
  }

  .coupon-list {
    display: flex;
    gap: $spacing-md;
  }

  .coupon-item {
    display: flex;
    flex-shrink: 0;
    width: 280rpx;
    height: 140rpx;
    background: linear-gradient(135deg, #FF6B35 0%, #FF8A5C 100%);
    border-radius: $border-radius-base;
    overflow: hidden;
  }

  .coupon-left {
    flex: 1;
    @include flex-center-column;
    gap: 4rpx;
    padding: $spacing-sm;
    border-right: 1rpx dashed rgba(255, 255, 255, 0.3);

    .coupon-amount {
      font-size: $font-size-xxl;
      font-weight: $font-weight-bold;
      color: #fff;
    }

    .coupon-condition {
      font-size: $font-size-xs;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .coupon-right {
    width: 80rpx;
    @include flex-center;

    .coupon-btn {
      font-size: $font-size-sm;
      color: #fff;
      padding: 8rpx 16rpx;
      background-color: rgba(255, 255, 255, 0.2);
      border-radius: $border-radius-round;
    }
  }
}

/* 商家公告 */
.notice-section {
  @include flex-center;
  gap: $spacing-sm;
  background-color: #fff9e6;
  border: 1rpx solid #ffe58f;

  .notice-icon {
    font-size: $font-size-lg;
  }

  .notice-content {
    flex: 1;
    font-size: $font-size-sm;
    color: $text-color-regular;
    @include text-ellipsis;
  }
}

/* 分类Tab */
.category-tabs {
  background-color: $bg-color-white;
  position: sticky;
  top: 0;
  z-index: $z-index-normal;
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs-list {
  display: flex;
  padding: $spacing-sm $spacing-md;
}

.tab-item {
  flex-shrink: 0;
  padding: $spacing-sm $spacing-md;
  margin-right: $spacing-sm;
  font-size: $font-size-base;
  color: $text-color-regular;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
  transition: $transition-base;

  &.active {
    color: #fff;
    background-color: $primary-color;
    font-weight: $font-weight-medium;
  }
}

/* 菜品列表 */
.dish-list-section {
  padding: $spacing-md;
}

.dish-list {
  .dish-item {
    display: flex;
    background-color: $bg-color-white;
    border-radius: $border-radius-base;
    padding: $spacing-md;
    margin-bottom: $spacing-md;
  }

  .dish-image {
    width: 180rpx;
    height: 180rpx;
    border-radius: $border-radius-base;
    flex-shrink: 0;
  }

  .dish-info {
    flex: 1;
    margin-left: $spacing-md;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  .dish-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
    @include text-ellipsis;
  }

  .dish-description {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-top: $spacing-xs;
    @include text-ellipsis;
  }

  .dish-bottom {
    @include flex-between;
    margin-top: $spacing-xs;
  }

  .price-section {
    @include flex-center;
    gap: 4rpx;
    color: $danger-color;

    .price-symbol {
      font-size: $font-size-sm;
    }

    .price-value {
      font-size: $font-size-xl;
      font-weight: $font-weight-bold;
    }

    .price-original {
      font-size: $font-size-xs;
      color: $text-color-secondary;
      text-decoration: line-through;
      margin-left: $spacing-xs;
    }
  }

  .sales-info {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .dish-tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-top: $spacing-xs;

    .tag {
      font-size: $font-size-xs;
      color: $primary-color;
      background-color: rgba(255, 107, 53, 0.1);
      padding: 4rpx 8rpx;
      border-radius: 4rpx;
    }
  }

  .dish-action {
    @include flex-center;
    align-self: flex-end;
  }

  .add-btn {
    width: 56rpx;
    height: 56rpx;
    @include flex-center;
    background-color: $primary-color;
    color: #fff;
    font-size: $font-size-xl;
    border-radius: 50%;
  }
}

/* 评价列表 */
.review-section {
  .section-header {
    @include flex-between;
    margin-bottom: $spacing-md;
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
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;

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

    .review-dishes {
      display: flex;
      flex-wrap: wrap;
      gap: $spacing-xs;

      .dish-tag {
        font-size: $font-size-xs;
        color: $text-color-secondary;
        background-color: $bg-color-base;
        padding: 6rpx 12rpx;
        border-radius: 6rpx;
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

/* 商家信息 */
.merchant-info-section {
  .info-item {
    @include flex-between;
    padding: $spacing-sm 0;
    border-bottom: 1rpx solid $border-color-light;

    &:last-child {
      border-bottom: none;
    }
  }

  .info-label {
    font-size: $font-size-base;
    color: $text-color-regular;
  }

  .info-value {
    font-size: $font-size-base;
    color: $text-color-primary;
    text-align: right;

    &.phone {
      color: $primary-color;
    }
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

  .cart-total {
    font-size: $font-size-xs;
    color: $danger-color;
    font-weight: $font-weight-bold;
  }
}

.bar-right {
  flex: 1;
  margin-left: $spacing-md;
}

.start-order-btn {
  width: 100%;
  height: 72rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-base;
}
</style>
