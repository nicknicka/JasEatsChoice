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
            <checkbox-group @change="handleDishSelect(group.merchantId, item.id, $event)">
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
                  <view class="quantity-btn" @click="decreaseQuantity(group.merchantId, item)">
                    <text>-</text>
                  </view>
                  <view class="quantity-value">{{ item.quantity }}</view>
                  <view class="quantity-btn" @click="increaseQuantity(group.merchantId, item)">
                    <text>+</text>
                  </view>
                </view>
              </view>
            </view>

            <!-- 删除按钮（编辑模式显示） -->
            <view class="delete-btn" v-if="group.editing" @click="deleteItem(group.merchantId, item)">
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
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useCartStore } from '@/store'
import { createPageDebug } from '@/utils/page-debug'
import { HOME, USER_ORDER_CONFIRM } from '@/constants/routes'

// Store
const cartStore = useCartStore()
const pageDebug = createPageDebug('购物车')
const DEFAULT_MERCHANT_IMAGE = '/static/images/default-merchant.png'
const DEFAULT_DISH_IMAGE = '/static/images/default-dish.png'

// 购物车列表（按商家分组）
const cartList = ref([])

const normalizeImage = (src, fallback) => {
  if (!src || typeof src !== 'string') {
    return fallback
  }

  if (src.includes('via.placeholder.com')) {
    return fallback
  }

  return src
}

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
 * 加载购物车数据：先向后端拉取，再映射成页面结构。
 * 失败时清空列表并提示，绝不拿本地缓存冒充真实购物车。
 */
const loading = ref(false)
const loadError = ref('')

const loadCartData = async () => {
  pageDebug.requestStart('加载购物车数据')
  loading.value = true
  loadError.value = ''

  const userId = uni.getStorageSync('userId')
  const ok = await cartStore.fetchCart(userId)
  loading.value = false

  if (!ok) {
    cartList.value = []
    loadError.value = '购物车加载失败，请下拉重试'
    pageDebug.requestFail('加载购物车数据', new Error('fetchCart failed'))
    return
  }

  const carts = cartStore.carts || []
  cartList.value = carts.map((merchantData) => {
    const items = (merchantData.items || []).map(item => ({
      id: item.id,
      dish: {
        id: item.dishId,
        name: item.name,
        price: item.price,
        image: normalizeImage(item.image, DEFAULT_DISH_IMAGE)
      },
      quantity: item.quantity,
      spec: item.spec || '',
      selected: item.selected !== false,
      available: item.available !== false
    }))
    return {
      merchantId: merchantData.merchantId,
      merchant: {
        id: merchantData.merchantId,
        name: merchantData.merchantName || '商家',
        logo: normalizeImage(merchantData.merchantLogo, DEFAULT_MERCHANT_IMAGE)
      },
      // 分组选中状态由子项推导，不再单独维护一份可能不同步的布尔值
      selected: items.length > 0 && items.every(item => item.selected),
      editing: false,
      items
    }
  })

  pageDebug.requestSuccess('加载购物车数据', {
    merchantCount: cartList.value.length,
    selectedCount: selectedCount.value
  })
}

/**
 * 商家选择：勾选后把该商家下所有条目一起同步到后端
 */
const handleMerchantSelect = async (merchantId, e) => {
  const checked = e.detail.value.length > 0
  pageDebug.action('勾选商家', { merchantId, checked })
  const ok = await cartStore.selectAll(checked, merchantId)
  if (!ok) {
    uni.showToast({ title: '更新选中状态失败', icon: 'none' })
    return
  }
  await loadCartData()
}

/**
 * 商品选择
 */
const handleDishSelect = async (merchantId, itemId, e) => {
  const checked = e.detail.value.length > 0
  pageDebug.action('勾选商品', { merchantId, itemId, checked })
  const ok = await cartStore.selectItem(itemId, checked)
  if (!ok) {
    uni.showToast({ title: '更新选中状态失败', icon: 'none' })
    return
  }
  await loadCartData()
}

/**
 * 全选
 */
const handleSelectAll = async (e) => {
  const checked = e.detail.value.length > 0
  pageDebug.action('购物车全选', { checked })
  const ok = await cartStore.selectAll(checked)
  if (!ok) {
    uni.showToast({ title: '更新选中状态失败', icon: 'none' })
    return
  }
  await loadCartData()
}

/**
 * 增加数量：调接口后重载，以服务端为准
 */
const increaseQuantity = async (merchantId, item) => {
  const target = item || findItem(merchantId)
  if (!target) return
  pageDebug.action('增加购物车商品数量', { merchantId, itemId: target.id })
  const ok = await cartStore.updateQuantity(target.id, target.quantity + 1)
  if (ok) {
    await loadCartData()
  } else {
    uni.showToast({ title: '更新数量失败', icon: 'none' })
  }
}

/**
 * 减少数量：数量减到 0 时后端会删除该项
 */
const decreaseQuantity = async (merchantId, item) => {
  const target = item || findItem(merchantId)
  if (!target || target.quantity <= 1) return
  pageDebug.action('减少购物车商品数量', { merchantId, itemId: target.id })
  const ok = await cartStore.updateQuantity(target.id, target.quantity - 1)
  if (ok) {
    await loadCartData()
  } else {
    uni.showToast({ title: '更新数量失败', icon: 'none' })
  }
}

/**
 * 删除商品
 */
const deleteItem = (merchantId, item) => {
  const target = item || findItem(merchantId)
  if (!target) return
  pageDebug.action('删除购物车商品', { merchantId, itemId: target.id })
  uni.showModal({
    title: '提示',
    content: '确定要删除这个商品吗？',
    success: async (res) => {
      if (!res.confirm) return
      const ok = await cartStore.removeItem(target.id)
      if (ok) {
        await loadCartData()
        uni.showToast({ title: '已删除', icon: 'success' })
      } else {
        uni.showToast({ title: '删除失败，请重试', icon: 'none' })
      }
    }
  })
}

/** 在当前列表里按 itemId 找条目；兼容旧调用只传 merchantId 的情况。 */
function findItem(itemId) {
  for (const group of cartList.value) {
    const hit = group.items.find(i => i.id === itemId)
    if (hit) return hit
  }
  return null
}

/**
 * 编辑商家
 */
const editMerchant = (merchantId) => {
  const group = cartList.value.find(g => g.merchantId === merchantId)
  if (group) {
    group.editing = !group.editing
    pageDebug.state('切换商家编辑态', {
      merchantId,
      editing: group.editing
    })
  }
}

/**
 * 去购物
 */
const goShopping = () => {
  pageDebug.action('购物车去逛逛')
  uni.switchTab({
    url: HOME
  })
}

/**
 * 提交订单
 */
const submitOrder = () => {
  pageDebug.action('提交购物车订单', {
    selectedCount: selectedCount.value,
    merchantCount: cartList.value.length
  })
  if (selectedCount.value === 0) {
    pageDebug.anomaly('购物车结算被未选商品拦截')
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
      url: USER_ORDER_CONFIRM
    })
    pageDebug.requestSuccess('提交购物车订单-准备结算数据', {
      itemCount: selectedItems.length,
      totalPrice: totalPrice.value
    })
  } catch (error) {
    pageDebug.requestFail('提交购物车订单-准备结算数据', error)
    console.error('准备订单数据失败:', error)
    uni.showToast({
      title: '订单数据准备失败',
      icon: 'none'
    })
  }
}

// 每次显示都重新拉取：从商品页加购后返回本页必须能看到最新购物车
onShow(() => {
  pageDebug.lifecycle('页面显示')
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
