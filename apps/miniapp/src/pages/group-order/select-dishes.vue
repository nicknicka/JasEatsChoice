<template>
  <view class="select-dishes-container">
    <!-- 商家信息 -->
    <view class="merchant-header">
      <image class="merchant-avatar" :src="merchantInfo.avatar" mode="aspectFill"></image>
      <view class="merchant-info">
        <text class="merchant-name">{{ merchantInfo.name }}</text>
        <text class="merchant-category">{{ merchantInfo.category }}</text>
      </view>
    </view>

    <!-- 菜品分类 -->
    <view class="category-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view
          class="tab-item"
          :class="{ active: activeCategory === item.id }"
          v-for="item in categories"
          :key="item.id"
          @tap="changeCategory(item.id)"
        >
          {{ item.name }}
        </view>
      </scroll-view>
    </view>

    <!-- 菜品列表 -->
    <scroll-view
      class="dishes-list"
      scroll-y
      @scrolltolower="loadMore"
    >
      <view
        class="dish-card"
        v-for="dish in dishList"
        :key="dish.id"
      >
        <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
        <view class="dish-info">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="dish-desc">{{ dish.description }}</text>
          <view class="dish-meta">
            <text class="dish-price">¥{{ dish.price }}</text>
            <text class="dish-sales">月售{{ dish.sales }}</text>
          </view>
        </view>
        <view class="dish-action">
          <view class="counter-wrapper" v-if="getCartCount(dish.id) > 0">
            <button class="counter-btn minus" @tap="updateCount(dish, -1)">-</button>
            <text class="counter-value">{{ getCartCount(dish.id) }}</text>
            <button class="counter-btn plus" @tap="updateCount(dish, 1)">+</button>
          </view>
          <button
            class="add-btn"
            v-else
            @tap="updateCount(dish, 1)"
          >
            加入
          </button>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="dishList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
      </view>
    </scroll-view>

    <!-- 已选菜品 -->
    <view class="cart-bar">
      <view class="cart-info" @tap="viewCart">
        <view class="cart-icon-wrapper">
          <uni-icons type="shop" size="24" color="#fff"></uni-icons>
          <view class="cart-badge" v-if="totalCount > 0">
            {{ totalCount }}
          </view>
        </view>
        <view class="cart-detail">
          <text class="cart-count">{{ totalCount }}道菜</text>
          <text class="cart-amount">¥{{ totalAmount }}</text>
        </view>
      </view>
      <button
        class="confirm-btn"
        :disabled="totalCount === 0"
        @tap="confirmSelection"
      >
        确认选择
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 商家信息
const merchantInfo = ref({
  id: 1,
  name: '老王家常菜',
  avatar: 'https://via.placeholder.com/80/FF6B35/FFFFFF?text=店',
  category: '川菜'
})

// 菜品分类
const categories = ref([
  { id: 'all', name: '全部' },
  { id: '1', name: '热销' },
  { id: '2', name: '川菜' },
  { id: '3', name: '凉菜' },
  { id: '4', name: '汤羹' }
])

const activeCategory = ref('all')

// 菜品列表
const dishList = ref([])
const loading = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 20

// 购物车
const cart = ref({})

onMounted(() => {
  loadDishes()
})

/**
 * 切换分类
 */
const changeCategory = (categoryId) => {
  activeCategory.value = categoryId
  loadDishes(true)
}

/**
 * 加载菜品
 */
const loadDishes = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // TODO: 调用API获取菜品列表
    setTimeout(() => {
      const mockData = generateMockDishes()
      if (isRefresh) {
        dishList.value = mockData
      } else {
        dishList.value = [...dishList.value, ...mockData]
      }

      if (mockData.length < pageSize) {
        noMore.value = true
      }

      loading.value = false
    }, 500)
  } catch (error) {
    console.error('加载菜品失败:', error)
    loading.value = false
  }
}

/**
 * 生成模拟菜品数据
 */
const generateMockDishes = () => {
  const dishes = []
  const count = Math.floor(Math.random() * 5) + 5

  const dishNames = [
    { name: '宫保鸡丁', price: 28, description: '经典川菜，麻辣鲜香' },
    { name: '鱼香肉丝', price: 26, description: '酸甜口味，下饭神器' },
    { name: '麻婆豆腐', price: 18, description: '麻辣嫩滑，经典川菜' },
    { name: '水煮鱼', price: 48, description: '鱼肉鲜嫩，麻辣过瘾' },
    { name: '回锅肉', price: 32, description: '肥而不腻，香味浓郁' }
  ]

  for (let i = 0; i < count; i++) {
    const dish = dishNames[i % dishNames.length]
    dishes.push({
      id: page.value * 20 + i,
      name: dish.name,
      image: `https://via.placeholder.com/200/FF6B35/FFFFFF?text=${i + 1}`,
      price: dish.price,
      description: dish.description,
      sales: Math.floor(Math.random() * 500) + 100
    })
  }

  return dishes
}

/**
 * 获取购物车数量
 */
const getCartCount = (dishId) => {
  return cart.value[dishId] || 0
}

/**
 * 更新数量
 */
const updateCount = (dish, delta) => {
  const dishId = dish.id
  const currentCount = cart.value[dishId] || 0
  const newCount = currentCount + delta

  if (newCount <= 0) {
    delete cart.value[dishId]
  } else {
    cart.value[dishId] = newCount
  }

  cart.value = { ...cart.value }
}

/**
 * 总数量
 */
const totalCount = computed(() => {
  return Object.values(cart.value).reduce((sum, count) => sum + count, 0)
})

/**
 * 总金额
 */
const totalAmount = computed(() => {
  let amount = 0
  dishList.value.forEach(dish => {
    const count = cart.value[dish.id] || 0
    if (count > 0) {
      amount += dish.price * count
    }
  })
  return amount.toFixed(2)
})

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && !noMore.value) {
    page.value++
    loadDishes()
  }
}

/**
 * 查看购物车
 */
const viewCart = () => {
  // 显示已选菜品列表
  uni.showModal({
    title: '已选菜品',
    content: `共${totalCount.value}道菜，¥${totalAmount.value}`,
    showCancel: false
  })
}

/**
 * 确认选择
 */
const confirmSelection = () => {
  if (totalCount.value === 0) {
    uni.showToast({
      title: '请选择菜品',
      icon: 'none'
    })
    return
  }

  // 将选中的菜品保存到群订单
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  const orderId = options.id

  // TODO: 调用API保存菜品选择
  uni.showLoading({
    title: '保存中...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1500)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.select-dishes-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 商家头部 */
.merchant-header {
  background: #fff;
  padding: 25rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.merchant-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.merchant-name {
  font-size: 30rpx;
  color: #333;
  font-weight: bold;
}

.merchant-category {
  font-size: 24rpx;
  color: #999;
}

/* 分类标签 */
.category-tabs {
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.tabs-scroll {
  white-space: nowrap;
  padding: 20rpx 30rpx;
}

.tab-item {
  display: inline-block;
  padding: 10rpx 24rpx;
  margin-right: 20rpx;
  font-size: 28rpx;
  color: #666;
  border-radius: 20rpx;

  &.active {
    background: #FF6B35;
    color: #fff;
  }

  &:last-child {
    margin-right: 0;
  }
}

/* 菜品列表 */
.dishes-list {
  flex: 1;
  padding: 20rpx;
}

.dish-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
  display: flex;
  gap: 20rpx;
}

.dish-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.dish-desc {
  font-size: 24rpx;
  color: #999;
  @include text-ellipsis;
}

.dish-meta {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.dish-price {
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
}

.dish-sales {
  font-size: 24rpx;
  color: #999;
}

.dish-action {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-end;
}

.counter-wrapper {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.counter-btn {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  @include flex-center;
  font-size: 28rpx;
  border: none;

  &.minus {
    background: #F5F5F5;
    color: #666;
  }

  &.plus {
    background: #FF6B35;
    color: #fff;
  }
}

.counter-value {
  font-size: 28rpx;
  color: #333;
  min-width: 40rpx;
  text-align: center;
}

.add-btn {
  padding: 10rpx 24rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 24rpx;
  border-radius: 20rpx;
  border: none;
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 购物车栏 */
.cart-bar {
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.cart-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.cart-icon-wrapper {
  position: relative;
  width: 80rpx;
  height: 80rpx;
  background: #FF6B35;
  border-radius: 50%;
  @include flex-center;
}

.cart-badge {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  min-width: 32rpx;
  height: 32rpx;
  padding: 0 6rpx;
  background: #F5222D;
  color: #fff;
  font-size: 20rpx;
  border-radius: 16rpx;
  @include flex-center;
}

.cart-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.cart-count {
  font-size: 26rpx;
  color: #333;
}

.cart-amount {
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
}

.confirm-btn {
  padding: 0 40rpx;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border-radius: 40rpx;
  border: none;
  @include flex-center;

  &[disabled] {
    background: #E8E8E8;
    color: #999;
  }
}
</style>
