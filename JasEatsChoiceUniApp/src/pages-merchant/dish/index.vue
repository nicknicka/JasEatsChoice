<template>
  <view class="dish-manage-container">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input" @tap="toSearch">
        <uni-icons type="search" size="18" color="#999"></uni-icons>
        <text class="placeholder">搜索菜品名称</text>
      </view>
    </view>

    <!-- 分类Tab -->
    <view class="category-tabs">
      <scroll-view scroll-x class="tabs-scroll">
        <view
          class="tab-item"
          :class="{ active: activeCategory === item.value }"
          v-for="item in categories"
          :key="item.value"
          @tap="changeCategory(item.value)"
        >
          {{ item.label }}
        </view>
      </scroll-view>
    </view>

    <!-- 菜品列表 -->
    <scroll-view
      class="dish-list"
      scroll-y
      @scrolltolower="loadMore"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        class="dish-card"
        v-for="dish in dishList"
        :key="dish.id"
        @tap="toEditDish(dish.id)"
      >
        <image class="dish-image" :src="dish.image" mode="aspectFill"></image>

        <view class="dish-info">
          <view class="dish-header">
            <text class="dish-name">{{ dish.name }}</text>
            <view class="dish-status" :class="{ active: dish.isActive }">
              {{ dish.isActive ? '在售' : '已下架' }}
            </view>
          </view>

          <text class="dish-desc">{{ dish.description }}</text>

          <view class="dish-price-row">
            <text class="dish-price">¥{{ dish.price }}</text>
            <view class="dish-tags">
              <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
            </view>
          </view>

          <view class="dish-stats">
            <text class="stat-item">销量 {{ dish.sales }}</text>
            <text class="stat-item">评分 {{ dish.rating }}</text>
          </view>
        </view>

        <!-- 快捷操作 -->
        <view class="quick-actions" @tap.stop>
          <view class="action-btn" @tap="toggleStatus(dish)">
            <uni-icons :type="dish.isActive ? 'eye-slash' : 'eye'" size="20" :color="dish.isActive ? '#999' : '#52C41A'"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="dishList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else @tap="loadMore">上拉加载更多</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="dishList.length === 0 && !loading">
        <empty text="暂无菜品" icon="🍜" buttonText="添加菜品" @button-click="toAddDish" />
      </view>
    </scroll-view>

    <!-- 添加按钮 -->
    <view class="add-btn" @tap="toAddDish">
      <uni-icons type="plus" size="24" color="#fff"></uni-icons>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dishApi } from '@/api/modules/dish.js'

// 分类数据
const categories = ref([
  { label: '全部', value: 'all' },
  { label: '热菜', value: 'hot' },
  { label: '凉菜', value: 'cold' },
  { label: '汤羹', value: 'soup' },
  { label: '主食', value: 'staple' },
  { label: '饮料', value: 'drink' },
  { label: '小吃', value: 'snack' }
])

const activeCategory = ref('all')
const dishList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const noMore = ref(false)
const page = ref(1)
const pageSize = 10

// 商家ID
const merchantId = ref('')

onMounted(() => {
  // 获取商家ID
  merchantId.value = uni.getStorageSync('merchantId') || ''
  if (!merchantId.value) {
    uni.showToast({
      title: '未登录或商家信息缺失',
      icon: 'none'
    })
    return
  }
  loadDishes()
})

/**
 * 切换分类
 */
const changeCategory = (category) => {
  activeCategory.value = category
  page.value = 1
  noMore.value = false
  loadDishes()
}

/**
 * 加载菜品列表 - DISH-001: 调用API获取菜品列表
 */
const loadDishes = async (isRefresh = false) => {
  if (loading.value) return

  loading.value = true
  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    // 调用API获取菜品列表
    const params = {
      merchantId: merchantId.value,
      page: page.value,
      size: pageSize
    }

    // 如果不是"全部"分类，添加分类参数
    if (activeCategory.value !== 'all') {
      params.category = activeCategory.value
    }

    const res = await dishApi.getList(params)

    if (res.code === 200 && res.data) {
      // 转换后端数据格式为前端所需格式
      const transformedDishes = res.data.map(dish => ({
        id: dish.id,
        name: dish.name,
        description: dish.description || '暂无描述',
        image: dish.image || '/static/default-dish.png',
        price: parseFloat(dish.price || 0).toFixed(2),
        // 后端status字段映射为isActive
        isActive: dish.status === true || dish.status === 1,
        tags: generateDishTags(dish),
        sales: dish.sales || 0,
        rating: dish.rating || dish.avgRating || '5.0',
        category: dish.category || 'hot'
      }))

      if (isRefresh) {
        dishList.value = transformedDishes
      } else {
        dishList.value = [...dishList.value, ...transformedDishes]
      }

      // 判断是否还有更多数据
      if (transformedDishes.length < pageSize) {
        noMore.value = true
      }

      console.log('加载菜品成功，数量:', transformedDishes.length)
    } else {
      throw new Error(res.message || '获取菜品列表失败')
    }
  } catch (error) {
    console.error('加载菜品失败:', error)
    uni.showToast({
      title: error.message || '加载菜品失败',
      icon: 'none'
    })
    // 失败时保持当前数据
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 生成菜品标签
 */
const generateDishTags = (dish) => {
  const tags = []

  // 根据分类添加标签
  if (dish.category === 'hot') {
    tags.push('热菜')
  }

  // 可以根据其他条件添加标签
  if (dish.calorie && dish.calorie < 300) {
    tags.push('低卡')
  }

  // 如果没有标签，至少添加"推荐"
  if (tags.length === 0) {
    tags.push('推荐')
  }

  return tags
}

/**
 * 生成模拟菜品数据
 */
const generateMockDishes = () => {
  const dishes = []
  const count = Math.floor(Math.random() * 5) + 5

  const dishNames = [
    '宫保鸡丁', '鱼香肉丝', '回锅肉', '麻婆豆腐',
    '水煮鱼', '糖醋排骨', '青椒肉丝', '红烧肉'
  ]

  for (let i = 0; i < count; i++) {
    dishes.push({
      id: page.value * 10 + i,
      name: dishNames[i % dishNames.length],
      description: '经典川菜，麻辣鲜香',
      image: `https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=${i}`,
      price: Math.floor(Math.random() * 50) + 20,
      isActive: Math.random() > 0.2,
      tags: i % 3 === 0 ? ['招牌', '辣'] : ['推荐'],
      sales: Math.floor(Math.random() * 500) + 50,
      rating: (Math.random() * 1 + 4).toFixed(1),
      category: activeCategory.value === 'all' ? 'hot' : activeCategory.value
    })
  }

  return dishes
}

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
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadDishes(true)
}

/**
 * 切换菜品状态 - DISH-002: 调用API更新状态（上架/下架）
 */
const toggleStatus = async (dish) => {
  const action = dish.isActive ? '下架' : '上架'
  const newStatus = !dish.isActive

  uni.showModal({
    title: '提示',
    content: `确认${action}菜品"${dish.name}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({
            title: '提交中...',
            mask: true
          })

          // 调用API更新菜品状态
          // 注意：后端使用status字段，前端使用isActive
          const apiRes = await dishApi.setAvailability(dish.id, newStatus)

          if (apiRes.code === 200) {
            // 更新本地数据
            dish.isActive = newStatus

            uni.showToast({
              title: `${action}成功`,
              icon: 'success'
            })
          } else {
            throw new Error(apiRes.message || '操作失败')
          }
        } catch (error) {
          console.error('更新菜品状态失败:', error)

          // 恢复原状态
          dish.isActive = !newStatus

          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

/**
 * 跳转到搜索
 */
const toSearch = () => {
  uni.showToast({
    title: '搜索功能开发中',
    icon: 'none'
  })
}

/**
 * 跳转到添加菜品
 */
const toAddDish = () => {
  uni.navigateTo({
    url: '/pages-merchant/dish/add'
  })
}

/**
 * 跳转到编辑菜品
 */
const toEditDish = (id) => {
  uni.navigateTo({
    url: `/pages-merchant/dish/edit?id=${id}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.dish-manage-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 搜索栏 */
.search-bar {
  background: #fff;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.search-input {
  background: #F5F5F5;
  border-radius: 40rpx;
  height: 70rpx;
  @include flex-center;
  padding: 0 30rpx;
  gap: 15rpx;
}

.placeholder {
  color: #999;
  font-size: 28rpx;
}

/* 分类Tab */
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
  padding: 12rpx 30rpx;
  margin-right: 20rpx;
  font-size: 28rpx;
  color: #666;
  border-radius: 30rpx;

  &.active {
    background: #FF6B35;
    color: #fff;
    font-weight: bold;
  }

  &:last-child {
    margin-right: 0;
  }
}

/* 菜品列表 */
.dish-list {
  flex: 1;
  padding: 20rpx;
}

.dish-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  display: flex;
  gap: 20rpx;
  padding: 20rpx;
  position: relative;
}

.dish-image {
  width: 180rpx;
  height: 180rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.dish-status {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  background: #F5F5F5;
  color: #999;

  &.active {
    background: #F6FFED;
    color: #52C41A;
  }
}

.dish-desc {
  font-size: 24rpx;
  color: #999;
  margin: 10rpx 0;
  @include text-ellipsis(2);
}

.dish-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10rpx;
}

.dish-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B35;
}

.dish-tags {
  display: flex;
  gap: 10rpx;
}

.tag {
  padding: 4rpx 12rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 20rpx;
  border-radius: 4rpx;
}

.dish-stats {
  display: flex;
  gap: 30rpx;
  margin-top: 10rpx;
}

.stat-item {
  font-size: 24rpx;
  color: #999;
}

/* 快捷操作 */
.quick-actions {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.action-btn {
  width: 60rpx;
  height: 60rpx;
  @include flex-center;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 空状态 */
.empty-state {
  padding-top: 200rpx;
}

/* 添加按钮 */
.add-btn {
  position: fixed;
  bottom: 100rpx;
  right: 40rpx;
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  border-radius: 60rpx;
  @include flex-center;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.4);
  z-index: 100;
}
</style>
