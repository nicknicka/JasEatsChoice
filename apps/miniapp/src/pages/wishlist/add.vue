<template>
  <view class="add-wish-container">
    <!-- 标题输入 -->
    <view class="form-section">
      <view class="section-title">我想吃</view>
      <textarea
        class="wish-input"
        v-model="wishForm.content"
        placeholder="描述你想吃的菜品，比如：想吃正宗的川菜，麻辣鲜香的..."
        maxlength="500"
        :show-confirm-bar="false"
        @input="onContentInput"
      />
      <view class="input-footer">
        <text class="word-count">{{ wishForm.content.length }}/500</text>
      </view>
    </view>

    <!-- 期望菜品 -->
    <view class="form-section">
      <view class="section-title">
        <text>期望菜品</text>
        <text class="optional">（选填）</text>
      </view>
      <view class="dish-input-wrapper" @tap="chooseDishes">
        <view class="selected-dishes" v-if="wishForm.dishes.length > 0">
          <text
            class="dish-tag"
            v-for="(dish, index) in wishForm.dishes"
            :key="index"
          >
            {{ dish }}
            <uni-icons
              type="closeempty"
              size="14"
              color="#FF6B35"
              @tap.stop="removeDish(index)"
            ></uni-icons>
          </text>
        </view>
        <text class="placeholder" v-else>选择你期望的菜品</text>
        <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <!-- 预算范围 -->
    <view class="form-section">
      <view class="section-title">
        <text>预算范围</text>
        <text class="optional">（选填）</text>
      </view>
      <view class="budget-input-wrapper">
        <view class="budget-item">
          <text class="budget-label">最低</text>
          <input
            class="budget-input"
            v-model="wishForm.minBudget"
            placeholder="0"
            type="digit"
          />
          <text class="budget-unit">元</text>
        </view>
        <text class="budget-separator">-</text>
        <view class="budget-item">
          <text class="budget-label">最高</text>
          <input
            class="budget-input"
            v-model="wishForm.maxBudget"
            placeholder="100"
            type="digit"
          />
          <text class="budget-unit">元</text>
        </view>
      </view>
    </view>

    <!-- 期望时间 -->
    <view class="form-section">
      <view class="section-title">
        <text>期望时间</text>
        <text class="optional">（选填）</text>
      </view>
      <picker
        mode="date"
        :value="wishForm.expectDate"
        :end="maxDate"
        @change="onDateChange"
      >
        <view class="picker-input">
          <text :class="{ placeholder: !wishForm.expectDate }">
            {{ wishForm.expectDate || '选择期望日期' }}
          </text>
          <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
        </view>
      </picker>
    </view>

    <!-- 用餐时段 -->
    <view class="form-section" v-if="wishForm.expectDate">
      <view class="section-title">
        <text>用餐时段</text>
        <text class="optional">（选填）</text>
      </view>
      <view class="meal-type-list">
        <view
          class="meal-type-item"
          :class="{ active: wishForm.mealType === item.value }"
          v-for="item in mealTypes"
          :key="item.value"
          @tap="selectMealType(item.value)"
        >
          {{ item.label }}
        </view>
      </view>
    </view>

    <!-- 特殊要求 -->
    <view class="form-section">
      <view class="section-title">
        <text>特殊要求</text>
        <text class="optional">（选填）</text>
      </view>
      <textarea
        class="requirements-input"
        v-model="wishForm.requirements"
        placeholder="比如：少放辣椒、多放葱花、需要包厢等..."
        maxlength="200"
        :show-confirm-bar="false"
      />
      <view class="input-footer">
        <text class="word-count">{{ wishForm.requirements.length }}/200</text>
      </view>
    </view>

    <!-- 图片上传 -->
    <view class="form-section">
      <view class="section-title">
        <text>参考图片</text>
        <text class="optional">（选填，最多3张）</text>
      </view>
      <view class="image-list">
        <view
          class="image-item"
          v-for="(img, index) in wishForm.images"
          :key="index"
        >
          <image class="upload-image" :src="img" mode="aspectFill"></image>
          <view class="delete-btn" @tap="deleteImage(index)">
            <uni-icons type="closeempty" size="16" color="#fff"></uni-icons>
          </view>
        </view>
        <view
          class="upload-btn"
          v-if="wishForm.images.length < 3"
          @tap="chooseImage"
        >
          <uni-icons type="camera" size="30" color="#D9D9D9"></uni-icons>
          <text class="upload-text">添加图片</text>
        </view>
      </view>
    </view>

    <!-- 发布提示 -->
    <view class="tips-card">
      <uni-icons type="info" size="16" color="#FF6B35"></uni-icons>
      <text class="tips-text">发布后，附近的商家会看到您的需求并回复</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn cancel" @tap="cancel">取消</button>
      <button class="action-btn primary" @tap="submitWish">发布</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 用餐时段
const mealTypes = [
  { label: '早餐', value: 'breakfast' },
  { label: '午餐', value: 'lunch' },
  { label: '晚餐', value: 'dinner' },
  { label: '夜宵', value: 'supper' }
]

// 最大日期（今天后30天）
const maxDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() + 30)
  return date.toISOString().split('T')[0]
})

// 心愿表单
const wishForm = ref({
  content: '',
  dishes: [],
  minBudget: '',
  maxBudget: '',
  expectDate: '',
  mealType: '',
  requirements: '',
  images: []
})

onMounted(() => {
  // 设置默认日期为今天
  const today = new Date().toISOString().split('T')[0]
  wishForm.value.expectDate = today
})

/**
 * 内容输入
 */
const onContentInput = (e) => {
  wishForm.value.content = e.detail.value
}

/**
 * 选择菜品
 */
const chooseDishes = () => {
  // 跳转到菜品选择页面
  uni.navigateTo({
    url: '/pages/wishlist/select-dishes?selected=' + JSON.stringify(wishForm.value.dishes)
  })
}

/**
 * 移除菜品
 */
const removeDish = (index) => {
  wishForm.value.dishes.splice(index, 1)
}

/**
 * 日期变化
 */
const onDateChange = (e) => {
  wishForm.value.expectDate = e.detail.value
}

/**
 * 选择用餐时段
 */
const selectMealType = (type) => {
  wishForm.value.mealType = type
}

/**
 * 选择图片
 */
const chooseImage = () => {
  const remainCount = 3 - wishForm.value.images.length
  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      wishForm.value.images.push(...res.tempFilePaths)
    }
  })
}

/**
 * 删除图片
 */
const deleteImage = (index) => {
  wishForm.value.images.splice(index, 1)
}

/**
 * 取消
 */
const cancel = () => {
  if (wishForm.value.content || wishForm.value.dishes.length > 0) {
    uni.showModal({
      title: '提示',
      content: '确定放弃编辑吗？已输入的内容将不会保存。',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
  } else {
    uni.navigateBack()
  }
}

/**
 * 提交心愿
 */
const submitWish = () => {
  // 验证
  if (!wishForm.value.content.trim()) {
    uni.showToast({
      title: '请描述你想吃什么',
      icon: 'none'
    })
    return
  }

  if (wishForm.value.content.length < 10) {
    uni.showToast({
      title: '描述至少10个字',
      icon: 'none'
    })
    return
  }

  // 验证预算
  if (wishForm.value.minBudget && wishForm.value.maxBudget) {
    const min = parseFloat(wishForm.value.minBudget)
    const max = parseFloat(wishForm.value.maxBudget)
    if (min > max) {
      uni.showToast({
        title: '最低预算不能高于最高预算',
        icon: 'none'
      })
      return
    }
  }

  uni.showModal({
    title: '确认发布',
    content: '发布后附近的商家会看到您的需求，确认发布吗？',
    success: (res) => {
      if (res.confirm) {
        publishWish()
      }
    }
  })
}

/**
 * 发布心愿
 */
const publishWish = () => {
  uni.showLoading({
    title: '发布中...'
  })

  // TODO: 调用API发布心愿
  const data = {
    content: wishForm.value.content,
    dishes: wishForm.value.dishes,
    budget: wishForm.value.minBudget && wishForm.value.maxBudget
      ? `${wishForm.value.minBudget}-${wishForm.value.maxBudget}`
      : '',
    expectTime: wishForm.value.expectDate,
    mealType: wishForm.value.mealType,
    requirements: wishForm.value.requirements,
    images: wishForm.value.images
  }

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '发布成功',
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

.add-wish-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 140rpx;
}

.form-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.optional {
  font-size: 24rpx;
  color: #999;
  font-weight: normal;
}

.wish-input {
  width: 100%;
  min-height: 200rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.input-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10rpx;
}

.word-count {
  font-size: 24rpx;
  color: #999;
}

/* 菜品选择 */
.dish-input-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  min-height: 80rpx;
}

.selected-dishes {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dish-tag {
  padding: 8rpx 16rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 24rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 5rpx;
}

.placeholder {
  flex: 1;
  font-size: 28rpx;
  color: #999;

  &.placeholder {
    color: #999;
  }
}

/* 预算输入 */
.budget-input-wrapper {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.budget-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.budget-label {
  font-size: 26rpx;
  color: #666;
  flex-shrink: 0;
}

.budget-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  text-align: center;
}

.budget-unit {
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.budget-separator {
  font-size: 28rpx;
  color: #999;
}

/* 选择器 */
.picker-input {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;

  .placeholder {
    color: #999;
  }
}

/* 用餐时段 */
.meal-type-list {
  display: flex;
  gap: 15rpx;
}

.meal-type-item {
  flex: 1;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  text-align: center;
  font-size: 28rpx;
  color: #666;
  border: 2rpx solid transparent;

  &.active {
    background: #FFF7E6;
    color: #FF6B35;
    border-color: #FF6B35;
  }
}

/* 特殊要求 */
.requirements-input {
  width: 100%;
  min-height: 150rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

/* 图片上传 */
.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.image-item {
  width: 200rpx;
  height: 200rpx;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.upload-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 5rpx;
  right: 5rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

.upload-btn {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed #D9D9D9;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10rpx;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
}

/* 提示卡片 */
.tips-card {
  background: #FFF7E6;
  padding: 20rpx;
  margin: 20rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.tips-text {
  flex: 1;
  font-size: 26rpx;
  color: #FF6B35;
  line-height: 1.5;
}

/* 操作按钮 */
.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 32rpx;
  border: none;
  @include flex-center;

  &.cancel {
    background: #fff;
    color: #666;
  }

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
