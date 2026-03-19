<template>
  <view class="customize-container">
    <!-- 菜品信息 -->
    <view class="dish-header">
      <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
      <view class="dish-info">
        <text class="dish-name">{{ dish.name }}</text>
        <text class="dish-desc">{{ dish.description }}</text>
        <view class="dish-price">
          <text class="price">¥{{ dish.price }}</text>
          <text class="original-price" v-if="dish.originalPrice">¥{{ dish.originalPrice }}</text>
        </view>
      </view>
    </view>

    <!-- 定制选项 -->
    <view class="customize-section">
      <view class="section-title">食材选择</view>
      <view class="option-group">
        <view 
          class="option-item"
          v-for="option in ingredientOptions"
          :key="option.id"
          @tap="selectOption('ingredient', option)"
        >
          <view class="option-info">
            <text class="option-name">{{ option.name }}</text>
            <text class="option-price" v-if="option.price > 0">+¥{{ option.price }}</text>
          </view>
          <view class="option-check" :class="{ checked: isOptionSelected('ingredient', option.id) }">
            <uni-icons v-if="isOptionSelected('ingredient', option.id)" type="checkmarkempty" size="18" color="#fff"></uni-icons>
          </view>
        </view>
      </view>

      <view class="section-title">规格选择</view>
      <view class="option-group">
        <view 
          class="option-item"
          v-for="option in sizeOptions"
          :key="option.id"
          @tap="selectOption('size', option)"
        >
          <view class="option-info">
            <text class="option-name">{{ option.name }}</text>
            <text class="option-price" v-if="option.priceExtra > 0">+¥{{ option.priceExtra }}</text>
          </view>
          <view class="option-check" :class="{ checked: isOptionSelected('size', option.id) }">
            <uni-icons v-if="isOptionSelected('size', option.id)" type="checkmarkempty" size="18" color="#fff"></uni-icons>
          </view>
        </view>
      </view>

      <view class="section-title">口味偏好</view>
      <view class="option-group">
        <view 
          class="tag-item"
          v-for="tag in tasteTags"
          :key="tag.id"
          :class="{ selected: isTagSelected(tag.id) }"
          @tap="toggleTag(tag.id)"
        >
          {{ tag.name }}
        </view>
      </view>
    </view>

    <!-- 备注输入 -->
    <view class="remark-section">
      <view class="section-title">备注</view>
      <textarea 
        class="remark-input"
        v-model="remark"
        placeholder="请输入特殊要求，如：少辣、不要香菜等"
        maxlength="100"
      ></textarea>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="quantity-control">
        <view class="btn minus" @tap="changeQuantity(-1)">
          <text>-</text>
        </view>
        <text class="quantity">{{ quantity }}</text>
        <view class="btn plus" @tap="changeQuantity(1)">
          <text>+</text>
        </view>
      </view>
      <view class="price-info">
        <text class="total-price">¥{{ totalPrice }}</text>
        <text class="unit">/份</text>
      </view>
      <button class="add-cart-btn" @tap="addToCart">加入购物车</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { dishApi, cartApi } from '@/api'

const userStore = useUserStore()

const dish = ref({
  id: 1,
  name: '宫保鸡丁',
  description: '经典川菜，麻辣鲜香',
  image: 'https://picsum.photos/400/300?random=1',
  price: 38,
  originalPrice: 48,
  calories: 280,
  merchantId: null
})

const ingredientOptions = ref([
  { id: 1, name: '加花生', price: 3 },
  { id: 2, name: '加腰果', price: 5 },
  { id: 3, name: '不要葱花', price: 0 },
  { id: 4, name: '不要香菜', price: 0 },
  { id: 5, name: '多放辣', price: 0 }
])

const sizeOptions = ref([
  { id: 1, name: '小份', priceExtra: -10 },
  { id: 2, name: '标准份', priceExtra: 0 },
  { id: 3, name: '大份', priceExtra: 15 }
])

const tasteTags = ref([
  { id: 1, name: '不辣' },
  { id: 2, name: '微辣' },
  { id: 3, name: '中辣' },
  { id: 4, name: '特辣' },
  { id: 5, name: '免辣' }
])

const selectedOptions = ref({
  ingredient: null,
  size: 2
})

const selectedTags = ref([])

const remark = ref('')
const quantity = ref(1)

const totalPrice = computed(() => {
  let price = dish.value.price

  // 加上食材选项价格
  if (selectedOptions.value.ingredient) {
    const option = ingredientOptions.value.find(o => o.id === selectedOptions.value.ingredient)
    if (option) price += option.price
  }

  // 加上规格价格
  const sizeOption = sizeOptions.value.find(o => o.id === selectedOptions.value.size)
  if (sizeOption) price += sizeOption.priceExtra

  return (price * quantity.value).toFixed(2)
})

const isOptionSelected = (type, id) => {
  return selectedOptions.value[type] === id
}

const selectOption = (type, option) => {
  selectedOptions.value[type] = option.id
}

const isTagSelected = (id) => {
  return selectedTags.value.includes(id)
}

const toggleTag = (id) => {
  const index = selectedTags.value.indexOf(id)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(id)
  }
}

const changeQuantity = (delta) => {
  const newQuantity = quantity.value + delta
  if (newQuantity >= 1 && newQuantity <= 99) {
    quantity.value = newQuantity
  }
}

const addToCart = async () => {
  try {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 构建可选食材数组
    const optionalIngredients = []
    if (selectedOptions.value.ingredient) {
      const ingredient = ingredientOptions.value.find(o => o.id === selectedOptions.value.ingredient)
      if (ingredient && ingredient.price > 0) {
        optionalIngredients.push({
          id: ingredient.id,
          name: ingredient.name,
          price: ingredient.price,
          quantity: 1
        })
      }
    }

    // 构建规格字符串
    const specParts = []
    const sizeOption = sizeOptions.value.find(o => o.id === selectedOptions.value.size)
    if (sizeOption) {
      specParts.push(sizeOption.name)
    }
    if (selectedTags.value.length > 0) {
      const tasteNames = selectedTags.value.map(id => tasteTags.value.find(t => t.id === id)?.name).filter(Boolean)
      specParts.push(tasteNames.join('+'))
    }
    const spec = specParts.length > 0 ? specParts.join(' / ') : ''

    // 调用购物车API
    const res = await cartApi.add({
      dishId: dish.value.id,
      merchantId: dish.value.merchantId,
      quantity: quantity.value,
      optionalIngredients,
      spec,
      remark: remark.value
    })

    if (res && (res.code === 200 || res.success || res.data)) {
      uni.showToast({
        title: '已加入购物车',
        icon: 'success'
      })

      setTimeout(() => {
        uni.navigateBack()
      }, 1000)
    } else {
      // 如果后端API调用失败，使用本地存储作为后备方案
      console.warn('购物车API调用失败，使用本地存储')

      let cart = uni.getStorageSync('cart') || []
      const cartItem = {
        userId,
        dishId: dish.value.id,
        dishName: dish.value.name,
        image: dish.value.image,
        price: parseFloat(totalPrice.value),
        quantity: quantity.value,
        spec,
        optionalIngredients,
        remark: remark.value
      }
      cart.push(cartItem)
      uni.setStorageSync('cart', cart)

      uni.showToast({
        title: '已加入购物车（本地）',
        icon: 'success'
      })

      setTimeout(() => {
        uni.navigateBack()
      }, 1000)
    }
  } catch (error) {
    console.error('加入购物车失败:', error)

    // 错误时使用本地存储
    try {
      let cart = uni.getStorageSync('cart') || []
      const cartItem = {
        dishId: dish.value.id,
        dishName: dish.value.name,
        price: parseFloat(totalPrice.value),
        quantity: quantity.value,
        spec: sizeOptions.value.find(o => o.id === selectedOptions.value.size)?.name,
        remark: remark.value
      }
      cart.push(cartItem)
      uni.setStorageSync('cart', cart)

      uni.showToast({
        title: '已加入购物车（离线）',
        icon: 'success'
      })
    } catch (e) {
      uni.showToast({
        title: '操作失败，请重试',
        icon: 'none'
      })
    }
  }
}

onMounted(async () => {
  // 从页面参数获取菜品ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.dishId) {
    try {
      const res = await dishApi.getDetail(options.dishId)
      if (res && res.data) {
        dish.value = {
          id: res.data.dishId || res.data.id,
          name: res.data.name,
          description: res.data.description,
          image: res.data.image || res.data.coverImage,
          price: parseFloat(res.data.price || 0),
          originalPrice: res.data.originalPrice ? parseFloat(res.data.originalPrice) : null,
          calories: res.data.calories || 0,
          merchantId: res.data.merchantId || res.data.merchant?.id
        }
      }
    } catch (error) {
      console.error('加载菜品详情失败:', error)
    }
  }
})
</script>

<style lang="scss" scoped>
.customize-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 150rpx;
}

.dish-header {
  background: #fff;
  padding: 30rpx;
  display: flex;
  gap: 24rpx;
  margin-bottom: 20rpx;
}

.dish-image {
  width: 200rpx;
  height: 200rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.dish-desc {
  font-size: 26rpx;
  color: #999;
  margin: 10rpx 0;
}

.dish-price {
  display: flex;
  align-items: baseline;
  gap: 10rpx;
}

.price {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF6B35;
}

.original-price {
  font-size: 26rpx;
  color: #999;
  text-decoration: line-through;
}

.customize-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.option-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.option-name {
  font-size: 28rpx;
  color: #333;
}

.option-price {
  font-size: 24rpx;
  color: #FF6B35;
}

.option-check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &.checked {
    background: #FF6B35;
    border-color: #FF6B35;
  }
}

.tag-item {
  display: inline-block;
  padding: 16rpx 32rpx;
  background: #F5F5F5;
  color: #666;
  font-size: 28rpx;
  border-radius: 40rpx;
  margin-right: 16rpx;
  margin-bottom: 16rpx;
  
  &.selected {
    background: #FF6B35;
    color: #fff;
  }
}

.remark-section {
  background: #fff;
  padding: 30rpx;
}

.remark-input {
  width: 100%;
  min-height: 150rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.btn {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 2rpx solid #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  color: #666;
  
  &.plus {
    background: #FF6B35;
    border-color: #FF6B35;
    color: #fff;
  }
}

.quantity {
  font-size: 32rpx;
  font-weight: bold;
  min-width: 60rpx;
  text-align: center;
}

.price-info {
  flex: 1;
  display: flex;
  align-items: baseline;
  gap: 5rpx;
}

.total-price {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF6B35;
}

.unit {
  font-size: 24rpx;
  color: #999;
}

.add-cart-btn {
  background: #FF6B35;
  color: #fff;
  border: none;
  padding: 20rpx 50rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
}
</style>
