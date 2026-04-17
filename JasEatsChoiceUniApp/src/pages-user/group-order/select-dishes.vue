<template>
  <view class="select-dishes-container">
    <view class="lock-banner" v-if="isReadonly">
      {{ readonlyMessage }}
    </view>

    <!-- 分类筛选 -->
    <view class="category-tabs">
      <scroll-view scroll-x class="category-scroll">
        <view
          class="category-item"
          :class="{ active: activeCategory === item.value }"
          v-for="item in categories"
          :key="item.value"
          @tap="changeCategory(item.value)"
        >
          {{ item.label }}
        </view>
      </scroll-view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar">
      <uni-icons type="search" size="18" color="#999"></uni-icons>
      <input
        class="search-input"
        v-model="searchKeyword"
        placeholder="搜索菜品"
        @input="onSearchInput"
      />
      <view class="clear-btn" v-if="searchKeyword" @tap="clearSearch">
        <uni-icons type="clear" size="16" color="#999"></uni-icons>
      </view>
    </view>

    <!-- 菜品列表 - GROUP-005 -->
    <scroll-view
      class="dish-list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <view class="dish-item" v-for="dish in dishList" :key="dish.id">
        <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
        <view class="dish-info">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="dish-desc" v-if="dish.description">{{ dish.description }}</text>
          <view class="dish-specs" v-if="dish.specifications && dish.specifications.length > 0">
            <view
              class="spec-tag"
              v-for="spec in dish.specifications"
              :key="spec.id"
            >
              {{ spec.name }}
            </view>
          </view>
          <view class="dish-footer">
            <text class="dish-price">¥{{ dish.price }}</text>
            <view class="quantity-control">
              <view
                class="control-btn minus"
                :class="{ disabled: isReadonly || !getSelectedQuantity(dish.id) }"
                @tap="decreaseQuantity(dish)"
              >
                <text>-</text>
              </view>
              <text class="quantity">{{ getSelectedQuantity(dish.id) }}</text>
              <view class="control-btn plus" :class="{ disabled: isReadonly }" @tap="increaseQuantity(dish)">
                <text>+</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="dishList.length === 0 && !loading">
        <empty text="暂无菜品" icon="🍜" />
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="loading">
        <uni-load-more status="loading" />
      </view>
    </scroll-view>

    <!-- 已选菜品 -->
    <view class="selected-dishes" v-if="selectedDishes.length > 0">
      <view class="selected-header">
        <text class="title">已选 {{ selectedDishes.length }} 件</text>
        <text class="clear-btn" @tap="clearAll" :class="{ disabled: isReadonly }">清空</text>
      </view>
      <scroll-view scroll-x class="selected-list">
        <view
          class="selected-item"
          v-for="item in selectedDishes"
          :key="item.dishId"
        >
          <image class="item-image" :src="item.image" mode="aspectFill"></image>
          <text class="item-quantity">×{{ item.quantity }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 底部操作栏 -->
    <view class="action-bar">
      <view class="total-info">
        <text class="quantity">已选 {{ totalQuantity }} 件</text>
        <text class="amount">¥{{ totalAmount }}</text>
      </view>
      <button class="submit-btn" @tap="submitSelections" :disabled="selectedDishes.length === 0 || isReadonly">
        {{ isReadonly ? '不可修改' : '确认选择' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { groupOrderApi } from '@/api/modules/group-order-api.js'

const orderId = ref('')
const userId = ref('')
const orderLocked = ref(false)
const currentUserJoined = ref(false)
const currentUserPaid = ref(false)
const canEdit = ref(true)

const readonlyMessage = computed(() => {
  if (!currentUserJoined.value) {
    return '您还未加入该拼单，当前页面为只读模式。'
  }
  if (currentUserPaid.value) {
    return '您已完成支付，当前页面为只读模式，不能再修改选菜。'
  }
  if (orderLocked.value) {
    return '拼单已确认成团，当前页面为只读模式，不能再修改选菜。'
  }
  if (!canEdit.value) {
    return '当前拼单暂不可修改菜品，请稍后再试。'
  }
  return ''
})

const isReadonly = computed(() => {
  return !currentUserJoined.value || currentUserPaid.value || orderLocked.value || !canEdit.value
})

// 分类
const activeCategory = ref('all')
const categories = ref([
  { label: '全部', value: 'all' },
  { label: '热菜', value: 'hot' },
  { label: '凉菜', value: 'cold' },
  { label: '主食', value: 'staple' },
  { label: '汤类', value: 'soup' },
  { label: '饮料', value: 'drink' }
])

// 搜索
const searchKeyword = ref('')

// 菜品列表
const dishList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// 已选菜品 { [dishId]: quantity }
const selectedMap = ref({})
const selectedDishes = computed(() => {
  return Object.entries(selectedMap.value)
    .filter(([_, quantity]) => quantity > 0)
    .map(([dishId, quantity]) => {
      const dish = dishList.value.find(d => d.id === dishId)
      return {
        dishId,
        quantity,
        name: dish?.name || '',
        image: dish?.image || '',
        price: dish?.price || '0'
      }
    })
})

// 总数量和总金额
const totalQuantity = computed(() => {
  return Object.values(selectedMap.value).reduce((sum, qty) => sum + qty, 0)
})

const totalAmount = computed(() => {
  return selectedDishes.value.reduce((sum, item) => {
    const dish = dishList.value.find(d => d.id === item.dishId)
    if (dish) {
      return sum + (parseFloat(dish.price) * item.quantity)
    }
    return sum
  }, 0).toFixed(2)
})

onMounted(async () => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  orderId.value = options.id || ''
  userId.value = uni.getStorageSync('userId') || ''

  await loadOrderMeta()

  // 加载菜品列表
  loadDishList()

  // 加载已选菜品
  loadUserSelections()
})

const loadOrderMeta = async () => {
  try {
    const res = await groupOrderApi.getDetail(orderId.value)
    if (res.code === 200 && res.data) {
      orderLocked.value = Boolean(res.data.locked)
      currentUserJoined.value = typeof res.data.currentUserJoined === 'boolean'
        ? res.data.currentUserJoined
        : Boolean((res.data.members || []).find(item => item.userId === userId.value))
      currentUserPaid.value = typeof res.data.currentUserPaid === 'boolean'
        ? res.data.currentUserPaid
        : Boolean((res.data.members || []).find(item => item.userId === userId.value)?.paid)
      canEdit.value = typeof res.data.canEdit === 'boolean'
        ? res.data.canEdit
        : !orderLocked.value
    }
  } catch (error) {
    console.error('加载拼单状态失败:', error)
  }
}

/**
 * GROUP-005: 加载可选菜品列表
 */
const loadDishList = async (isRefresh = false) => {
  if (loading.value) return

  try {
    loading.value = true

    if (isRefresh) {
      pageNum.value = 1
      hasMore.value = true
    }

    const params = {
      page: pageNum.value,
      size: pageSize.value
    }

    if (activeCategory.value !== 'all') {
      params.category = activeCategory.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    // GROUP-005: 调用API获取菜品列表
    const res = await groupOrderApi.getAvailableDishes(orderId.value, params)

    if (res.code === 200 && res.data) {
      const dishes = res.data.list || res.data || []

      if (isRefresh) {
        dishList.value = dishes
      } else {
        dishList.value.push(...dishes)
      }

      // 判断是否还有更多
      hasMore.value = dishes.length >= pageSize.value
      pageNum.value++
    }
  } catch (error) {
    console.error('加载菜品列表失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 加载用户已选菜品
 */
const loadUserSelections = async () => {
  try {
    const res = await groupOrderApi.getUserSelections(orderId.value, userId.value)

    if (res.code === 200 && res.data && Array.isArray(res.data)) {
      const selections = res.data
      selections.forEach(item => {
        selectedMap.value[item.dishId] = item.quantity
      })
    }
  } catch (error) {
    console.error('加载已选菜品失败:', error)
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadDishList(true)
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadDishList()
  }
}

/**
 * 切换分类
 */
const changeCategory = (category) => {
  activeCategory.value = category
  loadDishList(true)
}

/**
 * 搜索输入
 */
const onSearchInput = (e) => {
  searchKeyword.value = e.detail.value
  // 防抖搜索
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadDishList(true)
  }, 500)
}

let searchTimer = null

/**
 * 清除搜索
 */
const clearSearch = () => {
  searchKeyword.value = ''
  loadDishList(true)
}

/**
 * 获取已选数量
 */
const getSelectedQuantity = (dishId) => {
  return selectedMap.value[dishId] || 0
}

/**
 * 增加数量
 */
const increaseQuantity = (dish) => {
  if (isReadonly.value) {
    uni.showToast({
      title: readonlyMessage.value || '当前不可修改菜品',
      icon: 'none'
    })
    return
  }
  const currentQuantity = selectedMap.value[dish.id] || 0
  selectedMap.value[dish.id] = currentQuantity + 1
}

/**
 * 减少数量
 */
const decreaseQuantity = (dish) => {
  if (isReadonly.value) {
    uni.showToast({
      title: readonlyMessage.value || '当前不可修改菜品',
      icon: 'none'
    })
    return
  }
  const currentQuantity = selectedMap.value[dish.id] || 0
  if (currentQuantity > 0) {
    selectedMap.value[dish.id] = currentQuantity - 1
  }
}

/**
 * 清空所有
 */
const clearAll = () => {
  if (isReadonly.value) {
    uni.showToast({
      title: readonlyMessage.value || '当前不可修改菜品',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '提示',
    content: '确定清空已选菜品吗？',
    success: (res) => {
      if (res.confirm) {
        selectedMap.value = {}
      }
    }
  })
}

/**
 * GROUP-006: 保存菜品选择
 */
const submitSelections = async () => {
  if (isReadonly.value) {
    uni.showToast({
      title: readonlyMessage.value || '当前不可修改菜品',
      icon: 'none'
    })
    return
  }

  if (selectedDishes.value.length === 0) {
    uni.showToast({
      title: '请先选择菜品',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '保存中...',
      mask: true
    })

    // 构建选择数据
    const selections = selectedDishes.value.map(item => ({
      dishId: item.dishId,
      quantity: item.quantity,
      specification: {} // 可根据需要添加规格信息
    }))

    // GROUP-006: 调用API保存菜品选择
    const res = await groupOrderApi.saveSelections(orderId.value, {
      userId: userId.value,
      dishes: selections
    })

    if (res.code === 200) {
      uni.hideLoading()
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })

      // 返回上一页
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      throw new Error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存菜品选择失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '保存失败',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.select-dishes-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

.lock-banner {
  margin: 20rpx;
  padding: 20rpx 24rpx;
  background: #fff7e6;
  color: #d48806;
  border: 2rpx solid #ffd591;
  border-radius: 16rpx;
  font-size: 24rpx;
  line-height: 1.5;
}

/* 分类筛选 */
.category-tabs {
  background: #fff;
  padding: 20rpx 0;
  margin-bottom: 20rpx;
}

.category-scroll {
  white-space: nowrap;
  padding: 0 20rpx;
}

.category-item {
  display: inline-block;
  padding: 12rpx 30rpx;
  margin-right: 20rpx;
  background: #F5F5F5;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #666;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}

.disabled {
  opacity: 0.45;
}

/* 搜索栏 */
.search-bar {
  background: #fff;
  padding: 20rpx 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.search-input {
  flex: 1;
  height: 60rpx;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #333;
}

.clear-btn {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
}

/* 菜品列表 */
.dish-list {
  height: calc(100vh - 400rpx);
  padding: 0 20rpx;
}

.dish-item {
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
  justify-content: space-between;
}

.dish-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.dish-desc {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 10rpx;
  @include text-ellipsis-multiline(2);
}

.dish-specs {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
  margin-bottom: 10rpx;
}

.spec-tag {
  padding: 4rpx 12rpx;
  background: #F5F5F5;
  border-radius: 4rpx;
  font-size: 22rpx;
  color: #666;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dish-price {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.control-btn {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  @include flex-center;
  font-size: 28rpx;

  &.minus {
    background: #F5F5F5;
    color: #666;

    &.disabled {
      opacity: 0.3;
    }
  }

  &.plus {
    background: #FF6B35;
    color: #fff;
  }
}

.quantity {
  font-size: 28rpx;
  color: #333;
  min-width: 40rpx;
  text-align: center;
}

/* 已选菜品 */
.selected-dishes {
  position: fixed;
  bottom: 120rpx;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.selected-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.selected-header .title {
  font-size: 26rpx;
  color: #333;
}

.selected-header .clear-btn {
  font-size: 24rpx;
  color: #999;
}

.selected-list {
  white-space: nowrap;
}

.selected-item {
  display: inline-block;
  position: relative;
  margin-right: 15rpx;
}

.item-image {
  width: 80rpx;
  height: 80rpx;
  border-radius: 8rpx;
}

.item-quantity {
  position: absolute;
  top: -5rpx;
  right: -5rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 20rpx;
  padding: 2rpx 8rpx;
  border-radius: 10rpx;
}

/* 底部操作栏 */
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.total-info {
  display: flex;
  flex-direction: column;
  gap: 5rpx;
}

.total-info .quantity {
  font-size: 24rpx;
  color: #999;
}

.total-info .amount {
  font-size: 36rpx;
  font-weight: bold;
  color: #FF6B35;
}

.submit-btn {
  padding: 0 50rpx;
  height: 80rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;

  &[disabled] {
    background: #D9D9D9;
  }
}
</style>
