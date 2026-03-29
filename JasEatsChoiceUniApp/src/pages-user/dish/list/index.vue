<template>
  <view class="dish-list-container">
    <!-- 分类标题 -->
    <view class="category-header">
      <text class="category-name">{{ categoryName }}</text>
      <text class="dish-count">共 {{ dishList.length }} 道菜品</text>
    </view>

    <!-- 菜品列表 -->
    <view class="dish-list" v-if="dishList.length > 0">
      <view
        class="dish-item"
        v-for="dish in dishList"
        :key="dish.id"
        @click="goToDetail(dish.id)"
      >
        <image class="dish-image" :src="dish.image || '/static/dish-placeholder.png'" mode="aspectFill"></image>
        <view class="dish-info">
          <text class="dish-name">{{ dish.name }}</text>
          <view class="dish-tags">
            <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
          </view>
          <view class="dish-bottom">
            <text class="dish-price">¥{{ dish.price }}</text>
            <text class="dish-calories">{{ dish.calories }}千卡/100g</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-else-if="!loading">
      <image class="empty-icon" src="/static/empty.png" mode="aspectFit"></image>
      <text class="empty-text">暂无该分类菜品</text>
    </view>

    <!-- 加载中 -->
    <view class="loading-state" v-if="loading">
      <uni-load-more status="loading"></uni-load-more>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from 'vue'
import { dishApi } from '@/api/modules/dish'

// 数据
const categoryName = ref('')
const categoryCode = ref('')
const dishList = ref([])
const loading = ref(false)

// 页面加载
onLoad((options) => {
  if (options.name) {
    categoryName.value = decodeURIComponent(options.name)
  }
  if (options.category) {
    categoryCode.value = decodeURIComponent(options.category)
    loadDishList()
  }
})

// 加载菜品列表
const loadDishList = async () => {
  try {
    loading.value = true
    const res = await dishApi.getDishList({
      category: categoryCode.value
    })
    if (res.code === 200) {
      dishList.value = res.data || []
    } else {
      uni.showToast({
        title: res.message || '加载失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('加载菜品列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 跳转到菜品详情
const goToDetail = (dishId) => {
  uni.navigateTo({
    url: `/pages-user/dish/detail/index?id=${dishId}`
  })
}
</script>

<style lang="scss" scoped>
.dish-list-container {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 20rpx;
}

.category-header {
  background-color: #fff;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1rpx solid #f0f0f0;

  .category-name {
    font-size: 36rpx;
    font-weight: bold;
    color: #333;
  }

  .dish-count {
    font-size: 28rpx;
    color: #999;
  }
}

.dish-list {
  padding: 20rpx;
}

.dish-item {
  background-color: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
  display: flex;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.08);

  .dish-image {
    width: 200rpx;
    height: 200rpx;
    flex-shrink: 0;
  }

  .dish-info {
    flex: 1;
    padding: 20rpx;
    display: flex;
    flex-direction: column;
    justify-content: space-between;

    .dish-name {
      font-size: 32rpx;
      font-weight: bold;
      color: #333;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .dish-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 10rpx;
      margin-top: 10rpx;

      .tag {
        font-size: 22rpx;
        color: #FF6B35;
        background-color: rgba(255, 107, 53, 0.1);
        padding: 6rpx 16rpx;
        border-radius: 8rpx;
      }
    }

    .dish-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10rpx;

      .dish-price {
        font-size: 32rpx;
        font-weight: bold;
        color: #FF6B35;
      }

      .dish-calories {
        font-size: 24rpx;
        color: #999;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;

  .empty-icon {
    width: 300rpx;
    height: 300rpx;
    margin-bottom: 40rpx;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

.loading-state {
  padding-top: 100rpx;
}
</style>
