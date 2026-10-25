<!--
页面名称：merchant/detail/index（重构版）
原代码行数：1469行
重构后行数：约300行
减少比例：80%
重构时间：2026-03-20
-->
<template>
  <view class="merchant-detail-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 商家头部 -->
      <MerchantHeader
        :merchant="merchantDetail"
        :is-favorite="isFavorite"
        @toggle-favorite="toggleFavorite"
        @share="shareMerchant"
      />

      <!-- 优惠券 -->
      <CouponSection
        :coupons="coupons"
        @receive="receiveCoupon"
      />

      <!-- 商家公告 -->
      <view class="card notice-section" v-if="merchantDetail.notice">
        <view class="notice-icon">📢</view>
        <view class="notice-content">{{ merchantDetail.notice }}</view>
      </view>

      <!-- 菜品分类Tab -->
      <CategoryTabs
        v-model="activeCategory"
        :categories="categories"
      />

      <!-- 菜品列表 -->
      <view class="dish-list-section">
        <view class="dish-list">
          <DishCard
            v-for="dish in currentDishes"
            :key="dish.id"
            :dish="dish"
            @tap="toDishDetail"
            @add="quickAdd"
          />
        </view>
      </view>

      <!-- 商家评价 -->
      <MerchantReviews
        :review-count="merchantDetail.reviewCount"
        :rating="merchantDetail.rating"
        :review-tags="reviewTags"
        :reviews="reviews"
        @view-all="viewAllReviews"
      />

      <!-- 商家信息 -->
      <view class="card merchant-info-section">
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

        <view class="info-item" @tap="callMerchant">
          <text class="info-label">联系电话</text>
          <text class="info-value phone">{{ merchantDetail.phone }} ›</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="bar-left">
        <view class="bar-icon" @tap="toCart">
          <text class="icon">🛒</text>
          <view class="badge" v-if="cartCount > 0">{{ cartCount }}</view>
          <view class="cart-total" v-if="cartTotal > 0">¥{{ cartTotal }}</view>
        </view>
      </view>

      <view class="bar-right">
        <view class="start-order-btn" @tap="startOrder">
          去结算
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import MerchantHeader from '@/components/business/merchant/MerchantHeader.vue'
import CouponSection from '@/components/business/merchant/CouponSection.vue'
import CategoryTabs from '@/components/business/merchant/CategoryTabs.vue'
import DishCard from '@/components/business/merchant/DishCard.vue'
import MerchantReviews from '@/components/business/merchant/MerchantReviews.vue'

// 商家详情
const merchantDetail = ref({
  id: '',
  name: '老王家常菜',
  logo: 'https://via.placeholder.com/120/FF6B35/FFFFFF?text=店',
  rating: 4.8,
  reviewCount: 256,
  tags: ['快餐', '川菜', '性价比高'],
  monthlySales: 999,
  dishCount: 45,
  deliveryTime: 30,
  notice: '本店所有菜品均使用新鲜食材，欢迎品尝！',
  businessHours: '10:00-22:00',
  deliveryFee: '5.00',
  minOrderAmount: '20.00',
  address: '深圳市南山区科技园南区XX大厦',
  phone: '0755-12345678'
})

// 是否收藏
const isFavorite = ref(false)

// 优惠券
const coupons = ref([])

// 分类
const categories = ref([])
const activeCategory = ref('all')

// 菜品列表
const allDishes = ref([])
const currentDishes = computed(() => {
  if (activeCategory.value === 'all') {
    return allDishes.value
  }
  return allDishes.value.filter(dish => dish.categoryId === activeCategory.value)
})

// 评价标签
const reviewTags = ref([
  { label: '口味好', count: 180 },
  { label: '分量足', count: 150 },
  { label: '配送快', count: 120 }
])

// 评价列表
const reviews = ref([])

// 购物车
const cartCount = ref(0)
const cartTotal = ref(0)

onLoad((options) => {
  if (options && options.id) {
    loadMerchantDetail(options.id)
  }
})

/**
 * 加载商家详情
 */
const loadMerchantDetail = async (merchantId) => {
  try {
    // 模拟数据
    merchantDetail.value = {
      id: merchantId,
      name: '老王家常菜',
      logo: 'https://via.placeholder.com/120/FF6B35/FFFFFF?text=店',
      rating: 4.8,
      reviewCount: 256,
      tags: ['快餐', '川菜', '性价比高'],
      monthlySales: 999,
      dishCount: 45,
      deliveryTime: 30,
      notice: '本店所有菜品均使用新鲜食材，欢迎品尝！',
      businessHours: '10:00-22:00',
      deliveryFee: '5.00',
      minOrderAmount: '20.00',
      address: '深圳市南山区科技园南区XX大厦',
      phone: '0755-12345678'
    }

    categories.value = [
      { id: 'all', name: '全部' },
      { id: '1', name: '热销' },
      { id: '2', name: '主食' },
      { id: '3', name: '小吃' },
      { id: '4', name: '饮料' }
    ]

    allDishes.value = [
      {
        id: '1',
        name: '宫保鸡丁',
        price: '28.00',
        originalPrice: '35.00',
        image: 'https://via.placeholder.com/180/FF6B35/FFFFFF?text=菜',
        description: '经典川菜，麻辣鲜香',
        monthlySales: 999,
        tags: ['辣', '招牌'],
        categoryId: '1'
      },
      {
        id: '2',
        name: '鱼香肉丝',
        price: '25.00',
        image: 'https://via.placeholder.com/180/52C41A/FFFFFF?text=菜',
        description: '酸甜可口，下饭神器',
        monthlySales: 666,
        tags: ['微辣'],
        categoryId: '1'
      }
    ]

    coupons.value = [
      { id: 1, amount: '5', condition: '满30可用', received: false },
      { id: 2, amount: '3', condition: '满20可用', received: false }
    ]

    reviews.value = [
      {
        id: 1,
        user: {
          name: '张三',
          avatar: 'https://via.placeholder.com/60/1677FF/FFFFFF?text=张'
        },
        rating: 5,
        date: '2026-03-15',
        content: '菜品很好吃，配送也很快，下次还会再来！',
        dishes: ['宫保鸡丁']
      },
      {
        id: 2,
        user: {
          name: '李四',
          avatar: 'https://via.placeholder.com/60/52C41A/FFFFFF?text=李'
        },
        rating: 4,
        date: '2026-03-14',
        content: '味道不错，就是稍微有点辣',
        dishes: ['鱼香肉丝']
      }
    ]
  } catch (error) {
    console.error('加载商家详情失败:', error)
  }
}

/**
 * 切换收藏
 */
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value
  uni.showToast({
    title: isFavorite.value ? '已收藏' : '已取消收藏',
    icon: 'success'
  })
}

/**
 * 分享商家
 */
const shareMerchant = () => {
  uni.share({
    provider: 'weixin',
    scene: 'merchant',
    title: merchantDetail.value.name,
    imageUrl: merchantDetail.value.logo
  })
}

/**
 * 领取优惠券
 */
const receiveCoupon = (coupon) => {
  if (coupon.received) return

  uni.showToast({
    title: '领取成功',
    icon: 'success'
  })

  // 更新状态
  coupon.received = true
}

/**
 * 跳转菜品详情
 */
const toDishDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages/dish/detail?id=${dishId}`
  })
}

/**
 * 快速添加
 */
const quickAdd = (dish) => {
  cartCount.value++
  cartTotal.value = (parseFloat(cartTotal.value) + parseFloat(dish.price)).toFixed(2)

  uni.showToast({
    title: '已加入购物车',
    icon: 'success',
    duration: 1000
  })
}

/**
 * 查看全部评价
 */
const viewAllReviews = () => {
  uni.navigateTo({
    url: `/pages/review/list?merchantId=${merchantDetail.value.id}`
  })
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
 * 去购物车
 */
const toCart = () => {
  uni.navigateTo({
    url: '/pages/cart/index'
  })
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

  uni.navigateTo({
    url: '/pages/order/confirm'
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.merchant-detail-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

.scroll-container {
  padding: 20rpx;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.notice-section {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 20rpx 30rpx;
  background: #FFF7E6;
}

.notice-icon {
  font-size: 32rpx;
}

.notice-content {
  flex: 1;
  font-size: 24rpx;
  color: #FF6B35;
  @include text-ellipsis;
}

.dish-list-section {
  background: transparent;
  padding: 0;
  margin-bottom: 20rpx;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.merchant-info-section {
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12rpx 0;

    &:last-child {
      padding-bottom: 0;
    }
  }

  .info-label {
    font-size: 26rpx;
    color: #666;
  }

  .info-value {
    font-size: 26rpx;
    color: #333;

    &.phone {
      color: #FF6B35;
    }
  }
}

/* 底部操作栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.bar-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.bar-icon {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  background: #F5F5F5;
  border-radius: 50%;
  @include flex-center;

  .icon {
    font-size: 36rpx;
  }

  .badge {
    position: absolute;
    top: 0;
    right: 0;
    min-width: 32rpx;
    height: 32rpx;
    background: #FF6B35;
    color: #fff;
    font-size: 20rpx;
    border-radius: 16rpx;
    @include flex-center;
    padding: 0 8rpx;
  }

  .cart-total {
    position: absolute;
    bottom: -5rpx;
    left: 50%;
    transform: translateX(-50%);
    font-size: 20rpx;
    color: #FF6B35;
    white-space: nowrap;
  }
}

.start-order-btn {
  min-width: 240rpx;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 40rpx;
  @include flex-center;
}
</style>
