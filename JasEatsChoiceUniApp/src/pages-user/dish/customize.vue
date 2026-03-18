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

const dish = ref({
  id: 1,
  name: '宫保鸡丁',
  description: '经典川菜，麻辣鲜香',
  image: 'https://picsum.photos/400/300?random=1',
  price: 38,
  originalPrice: 48,
  calories: 280
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

const addToCart = () => {
  const cartItem = {
    dish: dish.value,
    options: selectedOptions.value,
    tags: selectedTags.value,
    remark: remark.value,
    quantity: quantity.value,
    totalPrice: totalPrice.value
  }
  
  // TODO: 调用添加到购物车API
  uni.showToast({
    title: '已加入购物车',
    icon: 'success'
  })
  
  setTimeout(() => {
    uni.navigateBack()
  }, 1000)
}
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
