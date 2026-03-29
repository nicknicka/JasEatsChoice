<template>
  <view class="cart-container">
    <!-- 空购物车 -->
    <view class="empty-cart" v-if="cartList.length === 0">
      <view class="empty-icon">🛒</view>
      <view class="empty-text">购物车还是空的</view>
      <view class="empty-tips">快去添加喜欢的菜品吧</view>
      <button class="go-shopping-btn" @click="goShopping">去逛逛</button>
    </view>

    <!-- 购物车列表 -->
    <scroll-view class="cart-scroll" scroll-y v-else>
      <!-- 按商家分组显示 -->
      <view class="merchant-group" v-for="group in cartList" :key="group.merchantId">
        <!-- 商家头部 -->
        <view class="merchant-header">
          <checkbox-group @change="handleMerchantSelect(group.merchantId, $event)">
            <label class="merchant-select">
              <checkbox
                :value="group.merchantId"
                :checked="group.selected"
                color="#FF6B35"
              />
              <image class="merchant-logo" :src="group.merchant.logo" mode="aspectFill" />
              <text class="merchant-name">{{ group.merchant.name }}</text>
            </label>
          </checkbox-group>
          <view class="merchant-edit" @click="editMerchant(group.merchantId)">
            <text>{{ group.editing ? '完成' : '编辑' }}</text>
          </view>
        </view>

        <!-- 商品列表 -->
        <view class="dish-list">
          <view
            class="dish-item"
            v-for="item in group.items"
            :key="item.dish.id"
          >
            <checkbox-group @change="handleDishSelect(group.merchantId, item.dish.id, $event)">
              <label class="dish-select">
                <checkbox
                  :value="item.dish.id"
                  :checked="item.selected"
                  color="#FF6B35"
                />
              </label>
            </checkbox-group>

            <image class="dish-image" :src="item.dish.image" mode="aspectFill" />

            <view class="dish-info">
              <view class="dish-name">{{ item.dish.name }}</view>
              <view class="dish-spec" v-if="item.spec">{{ item.spec }}</view>
              <view class="dish-bottom">
                <view class="dish-price">
                  <text class="price-symbol">¥</text>
                  <text class="price-value">{{ item.dish.price }}</text>
                </view>
                <view class="quantity-control">
                  <view class="quantity-btn" @click="decreaseQuantity(group.merchantId, item.dish.id)">
                    <text>-</text>
                  </view>
                  <view class="quantity-value">{{ item.quantity }}</view>
                  <view class="quantity-btn" @click="increaseQuantity(group.merchantId, item.dish.id)">
                    <text>+</text>
                  </view>
                </view>
              </view>
            </view>

            <!-- 删除按钮（编辑模式显示） -->
            <view class="delete-btn" v-if="group.editing" @click="deleteItem(group.merchantId, item.dish.id)">
              <text>🗑️</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 配送费说明 -->
      <view class="delivery-fee-info">
        <view class="info-item">
          <text class="info-label">配送费</text>
          <text class="info-value">¥{{ totalDeliveryFee }}</text>
        </view>
        <view class="info-item">
          <text class="info-label">包装费</text>
          <text class="info-value">¥{{ totalPackingFee }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部结算栏 -->
    <view class="bottom-bar" v-if="cartList.length > 0">
      <view class="bar-left">
        <checkbox-group @change="handleSelectAll">
          <label class="select-all">
            <checkbox
              value="all"
              :checked="allSelected"
              color="#FF6B35"
            />
            <text>全选</text>
          </label>
        </checkbox-group>
        <view class="total-price">
          <text class="price-label">合计：</text>
          <text class="price-symbol">¥</text>
          <text class="price-value">{{ totalPrice }}</text>
        </view>
      </view>

      <view class="bar-right">
        <view class="submit-btn" @click="submitOrder">
          结算（{{ selectedCount }}）
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

// 购物车列表（按商家分组）
const cartList = ref([])

// 计算属性
const allSelected = computed(() => {
  return cartList.value.length > 0 && cartList.value.every(group => group.selected)
})

const selectedCount = computed(() => {
  let count = 0
  cartList.value.forEach(group => {
    group.items.forEach(item => {
      if (item.selected) {
        count += item.quantity
      }
    })
  })
  return count
})

const totalPrice = computed(() => {
  let total = 0
  cartList.value.forEach(group => {
    group.items.forEach(item => {
      if (item.selected) {
        total += item.dish.price * item.quantity
      }
    })
  })
  return total.toFixed(2)
})

const totalDeliveryFee = computed(() => {
  // 简单计算：每个商家5元配送费
  let fee = 0
  cartList.value.forEach(group => {
    const hasSelected = group.items.some(item => item.selected)
    if (hasSelected) {
      fee += 5
    }
  })
  return fee.toFixed(2)
})

const totalPackingFee = computed(() => {
  // 简单计算：每份1元包装费
  let fee = 0
  cartList.value.forEach(group => {
    group.items.forEach(item => {
      if (item.selected) {
        fee += item.quantity * 1
      }
    })
  })
  return fee.toFixed(2)
})

/**
 * 加载购物车数据
 */
const loadCartData = () => {
  // 从store获取购物车数据
  const carts = cartStore.carts

  // 转换为按商家分组的格式
  const list = Object.keys(carts).map(merchantId => {
    const merchantData = carts[merchantId]
    return {
      merchantId,
      merchant: {
        id: merchantId,
        name: '老王家常菜', // TODO: 从商家信息获取
        logo: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王'
      },
      selected: false,
      editing: false,
      items: merchantData.items.map(item => ({
        dish: item,
        quantity: item.quantity,
        spec: '',
        selected: false
      }))
    }
  })

  cartList.value = list
}

/**
 * 商家选择
 */
const handleMerchantSelect = (merchantId, e) => {
  const checked = e.detail.value.length > 0
  const group = cartList.value.find(g => g.merchantId === merchantId)

  if (group) {
    group.selected = checked
    group.items.forEach(item => {
      item.selected = checked
    })
  }
}

/**
 * 商品选择
 */
const handleDishSelect = (merchantId, dishId, e) => {
  const checked = e.detail.value.length > 0
  const group = cartList.value.find(g => g.merchantId === merchantId)

  if (group) {
    const item = group.items.find(i => i.dish.id === dishId)
    if (item) {
      item.selected = checked
    }

    // 检查是否所有商品都已选中
    group.selected = group.items.every(i => i.selected)
  }
}

/**
 * 全选
 */
const handleSelectAll = (e) => {
  const checked = e.detail.value.length > 0

  cartList.value.forEach(group => {
    group.selected = checked
    group.items.forEach(item => {
      item.selected = checked
    })
  })
}

/**
 * 增加数量
 */
const increaseQuantity = (merchantId, dishId) => {
  const group = cartList.value.find(g => g.merchantId === merchantId)
  if (group) {
    const item = group.items.find(i => i.dish.id === dishId)
    if (item) {
      item.quantity++

      // 更新store
      cartStore.updateQuantity(merchantId, dishId, item.quantity)
    }
  }
}

/**
 * 减少数量
 */
const decreaseQuantity = (merchantId, dishId) => {
  const group = cartList.value.find(g => g.merchantId === merchantId)
  if (group) {
    const item = group.items.find(i => i.dish.id === dishId)
    if (item && item.quantity > 1) {
      item.quantity--

      // 更新store
      cartStore.updateQuantity(merchantId, dishId, item.quantity)
    }
  }
}

/**
 * 删除商品
 */
const deleteItem = (merchantId, dishId) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这个商品吗？',
    success: (res) => {
      if (res.confirm) {
        const group = cartList.value.find(g => g.merchantId === merchantId)
        if (group) {
          const index = group.items.findIndex(i => i.dish.id === dishId)
          if (index > -1) {
            group.items.splice(index, 1)

            // 如果该商家没有商品了，移除整个分组
            if (group.items.length === 0) {
              const groupIndex = cartList.value.findIndex(g => g.merchantId === merchantId)
              cartList.value.splice(groupIndex, 1)
            }

            // 更新store
            cartStore.removeFromCart(merchantId, dishId)
          }
        }

        uni.showToast({
          title: '已删除',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 编辑商家
 */
const editMerchant = (merchantId) => {
  const group = cartList.value.find(g => g.merchantId === merchantId)
  if (group) {
    group.editing = !group.editing
  }
}

/**
 * 去购物
 */
const goShopping = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}

/**
 * 提交订单
 */
const submitOrder = () => {
  if (selectedCount.value === 0) {
    uni.showToast({
      title: '请先选择商品',
      icon: 'none'
    })
    return
  }

  // 收集选中的商品
  const selectedItems = []
  cartList.value.forEach(group => {
    group.items.forEach(item => {
      if (item.selected) {
        selectedItems.push({
          merchantId: group.merchantId,
          merchant: group.merchant,
          dish: item.dish,
          quantity: item.quantity,
          spec: item.spec
        })
      }
    })
  })

  // 将选中的商品数据存储到临时状态，供订单确认页使用
  // 可以通过事件总线、Pinia store或URL参数传递
  // 这里使用uni.setStorageSync临时存储
  try {
    uni.setStorageSync('temp_order_items', selectedItems)
    uni.setStorageSync('temp_order_summary', {
      total_price: totalPrice.value,
      delivery_fee: totalDeliveryFee.value,
      packing_fee: totalPackingFee.value,
      selected_count: selectedCount.value
    })

    // 跳转到订单确认页
    uni.navigateTo({
      url: '/order/confirm/index'
    })
  } catch (error) {
    console.error('准备订单数据失败:', error)
    uni.showToast({
      title: '订单数据准备失败',
      icon: 'none'
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadCartData()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.cart-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

/* 空购物车 */
.empty-cart {
  @include flex-center-column;
  gap: $spacing-md;
  padding: 200rpx 0;

  .empty-icon {
    font-size: 160rpx;
  }

  .empty-text {
    font-size: $font-size-lg;
    color: $text-color-primary;
  }

  .empty-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .go-shopping-btn {
    margin-top: $spacing-lg;
    width: 300rpx;
    height: 80rpx;
    line-height: 80rpx;
    background-color: $primary-color;
    color: #fff;
    font-size: $font-size-base;
    border-radius: $border-radius-round;
    border: none;
  }
}

/* 购物车列表 */
.cart-scroll {
  height: calc(100vh - 120rpx);
}

.merchant-group {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
}

.merchant-header {
  @include flex-between;
  align-items: center;
  padding: $spacing-md;
  border-bottom: 1rpx solid $border-color-light;
}

.merchant-select {
  @include flex-center;
  gap: $spacing-sm;
  flex: 1;

  .merchant-logo {
    width: 60rpx;
    height: 60rpx;
    border-radius: $border-radius-sm;
  }

  .merchant-name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }
}

.merchant-edit {
  font-size: $font-size-sm;
  color: $primary-color;
}

.dish-list {
  padding: 0 $spacing-md $spacing-md;
}

.dish-item {
  display: flex;
  align-items: center;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $border-color-light;
  position: relative;

  &:last-child {
    border-bottom: none;
  }
}

.dish-select {
  margin-right: $spacing-sm;
}

.dish-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  margin-left: $spacing-sm;
  height: 160rpx;
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

.dish-spec {
  font-size: $font-size-sm;
  color: $text-color-secondary;
  margin-top: $spacing-xs;
}

.dish-bottom {
  @include flex-between;
  align-items: center;
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
    font-size: $font-size-xl;
  }
}

.quantity-control {
  @include flex-center;
  gap: $spacing-sm;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  padding: $spacing-xs;

  .quantity-btn {
    width: 48rpx;
    height: 48rpx;
    @include flex-center;
    background-color: $bg-color-white;
    border-radius: $border-radius-sm;
    font-size: $font-size-lg;
    color: $text-color-primary;
  }

  .quantity-value {
    width: 60rpx;
    text-align: center;
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $text-color-primary;
  }
}

.delete-btn {
  position: absolute;
  right: $spacing-md;
  top: 50%;
  transform: translateY(-50%);
  font-size: 48rpx;
}

/* 配送费说明 */
.delivery-fee-info {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;

  .info-item {
    @include flex-between;
    padding: $spacing-sm 0;

    &:last-child {
      padding-bottom: 0;
    }
  }

  .info-label {
    font-size: $font-size-base;
    color: $text-color-regular;
  }

  .info-value {
    font-size: $font-size-base;
    color: $text-color-primary;
  }
}

/* 底部结算栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include flex-between;
  align-items: center;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
}

.bar-left {
  @include flex-center;
  gap: $spacing-md;
  flex: 1;
}

.select-all {
  @include flex-center;
  gap: $spacing-sm;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.total-price {
  @include flex-center;
  font-size: $font-size-base;

  .price-label {
    color: $text-color-regular;
  }

  .price-symbol {
    color: $danger-color;
    font-size: $font-size-sm;
  }

  .price-value {
    color: $danger-color;
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
  }
}

.bar-right {
  margin-left: $spacing-md;
}

.submit-btn {
  width: 280rpx;
  height: 80rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
}
</style>
