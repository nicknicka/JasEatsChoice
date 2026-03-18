<template>
  <view class="menu-manage-container">
    <!-- 顶部提示 -->
    <view class="tips-card">
      <uni-icons type="info" size="18" color="#FF6B35"></uni-icons>
      <text class="tips-text">拖动菜品可调整排序，点击开关可控制上架状态</text>
    </view>

    <!-- 分类菜单 -->
    <view class="category-menu">
      <scroll-view scroll-x class="category-scroll">
        <view
          class="category-item"
          :class="{ active: activeCategory === item.value }"
          v-for="item in categories"
          :key="item.value"
          @tap="changeCategory(item.value)"
        >
          {{ item.label }}
          <text class="count">({{ item.count }})</text>
        </view>
      </scroll-view>
    </view>

    <!-- 菜品列表 -->
    <scroll-view
      class="dish-list"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view class="list-section" v-for="section in dishSections" :key="section.id">
        <view class="section-header">
          <text class="section-title">{{ section.name }}</text>
          <view class="section-actions">
            <view class="action-btn" @tap="editCategory(section)">
              <uni-icons type="compose" size="16" color="#1890FF"></uni-icons>
            </view>
            <view class="action-btn" @tap="addDish(section.id)">
              <uni-icons type="plus" size="16" color="#52C41A"></uni-icons>
            </view>
          </view>
        </view>

        <!-- 菜品列表（支持拖拽排序） -->
        <movable-area class="dish-area">
          <view
            class="dish-item"
            v-for="(dish, index) in section.dishes"
            :key="dish.id"
          >
            <movable-view
              class="dish-movable"
              direction="vertical"
              :y="dish.y"
              @change="onDragChange($event, dish, index, section.dishes)"
              @touchend="onDragEnd(dish, section.dishes)"
            >
              <view class="dish-content">
                <view class="drag-handle">
                  <uni-icons type="bars" size="18" color="#D9D9D9"></uni-icons>
                </view>
                <image class="dish-image" :src="dish.image" mode="aspectFill"></image>
                <view class="dish-info">
                  <text class="dish-name">{{ dish.name }}</text>
                  <view class="dish-tags" v-if="dish.tags.length > 0">
                    <text class="tag" v-for="tag in dish.tags" :key="tag">{{ tag }}</text>
                  </view>
                  <text class="dish-price">¥{{ dish.price }}</text>
                </view>
                <view class="dish-status">
                  <switch
                    :checked="dish.isActive"
                    color="#FF6B35"
                    @change="toggleDishStatus(dish, $event)"
                  />
                </view>
                <view class="dish-actions">
                  <view class="action-btn" @tap="editDish(dish)">
                    <uni-icons type="compose" size="18" color="#1890FF"></uni-icons>
                  </view>
                  <view class="action-btn delete" @tap="deleteDish(dish)">
                    <uni-icons type="trash" size="18" color="#F5222D"></uni-icons>
                  </view>
                </view>
              </view>
            </movable-view>
          </view>
        </movable-area>

        <!-- 空状态 -->
        <view class="empty-state" v-if="section.dishes.length === 0">
          <empty text="暂无菜品" icon="🍜" buttonText="添加菜品" @button-click="addDish(section.id)" />
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="loading">
        <uni-load-more status="loading" />
      </view>
    </scroll-view>

    <!-- 批量操作栏 -->
    <view class="batch-actions" v-if="batchMode">
      <text class="selected-count">已选 {{ selectedDishes.length }} 项</text>
      <view class="action-buttons">
        <button class="action-btn" @tap="batchOffShelf">批量下架</button>
        <button class="action-btn danger" @tap="batchDelete">批量删除</button>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="bottom-actions" v-if="!batchMode">
      <button class="action-btn" @tap="toggleBatchMode">批量管理</button>
      <button class="action-btn primary" @tap="showMenuActions">菜单管理</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showConfirm } from '@/utils/helper'

// 分类列表
const categories = ref([
  { label: '全部', value: 'all', count: 45 },
  { label: '热菜', value: 'hot', count: 18 },
  { label: '凉菜', value: 'cold', count: 8 },
  { label: '汤羹', value: 'soup', count: 6 },
  { label: '主食', value: 'staple', count: 7 },
  { label: '饮料', value: 'drink', count: 4 },
  { label: '小吃', value: 'snack', count: 2 }
])

const activeCategory = ref('all')

// 菜品分组
const dishSections = ref([])

const loading = ref(false)
const refreshing = ref(false)
const batchMode = ref(false)
const selectedDishes = ref([])

onMounted(() => {
  loadMenuList()
})

/**
 * 切换分类
 */
const changeCategory = (category) => {
  activeCategory.value = category
  loadMenuList()
}

/**
 * 加载菜单列表
 */
const loadMenuList = async () => {
  loading.value = true

  try {
    // TODO: 调用API获取菜单列表
    // const res = await merchantApi.getMenuList({ category: activeCategory.value })
    // dishSections.value = res.data

    // 模拟数据
    setTimeout(() => {
      if (activeCategory.value === 'all') {
        dishSections.value = [
          {
            id: 'hot',
            name: '热菜',
            dishes: [
              {
                id: 1,
                name: '宫保鸡丁',
                image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1',
                price: 28,
                tags: ['中辣', '微甜'],
                isActive: true,
                y: 0
              },
              {
                id: 2,
                name: '鱼香肉丝',
                image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=2',
                price: 26,
                tags: ['不辣'],
                isActive: true,
                y: 0
              },
              {
                id: 3,
                name: '回锅肉',
                image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=3',
                price: 32,
                tags: ['中辣'],
                isActive: false,
                y: 0
              }
            ]
          },
          {
            id: 'cold',
            name: '凉菜',
            dishes: [
              {
                id: 4,
                name: '凉拌黄瓜',
                image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=4',
                price: 12,
                tags: ['清爽'],
                isActive: true,
                y: 0
              },
              {
                id: 5,
                name: '口水鸡',
                image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=5',
                price: 22,
                tags: ['中辣'],
                isActive: true,
                y: 0
              }
            ]
          },
          {
            id: 'soup',
            name: '汤羹',
            dishes: [
              {
                id: 6,
                name: '紫菜蛋花汤',
                image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=6',
                price: 8,
                tags: [],
                isActive: true,
                y: 0
              }
            ]
          }
        ]
      } else if (activeCategory.value === 'hot') {
        dishSections.value = [
          {
            id: 'hot',
            name: '热菜',
            dishes: [
              {
                id: 1,
                name: '宫保鸡丁',
                image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1',
                price: 28,
                tags: ['中辣', '微甜'],
                isActive: true,
                y: 0
              },
              {
                id: 2,
                name: '鱼香肉丝',
                image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=2',
                price: 26,
                tags: ['不辣'],
                isActive: true,
                y: 0
              }
            ]
          }
        ]
      } else {
        dishSections.value = []
      }

      loading.value = false
      refreshing.value = false
    }, 500)
  } catch (error) {
    console.error('加载菜单失败:', error)
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadMenuList()
}

/**
 * 拖拽变更
 */
const onDragChange = (e, dish, index, dishes) => {
  dish.y = e.detail.y
}

/**
 * 拖拽结束
 */
const onDragEnd = (dish, dishes) => {
  // 简化的拖拽排序逻辑
  // 实际项目中需要根据位置计算新的排序
  dish.y = 0

  // TODO: 调用API更新排序
  // merchantApi.updateDishSort({
  //   dishId: dish.id,
  //   newPosition: newIndex
  // })
}

/**
 * 切换菜品状态
 */
const toggleDishStatus = (dish, e) => {
  const isActive = e.detail.value
  const action = isActive ? '上架' : '下架'

  uni.showModal({
    title: '提示',
    content: `确认${action}菜品"${dish.name}"吗？`,
    success: (res) => {
      if (res.confirm) {
        dish.isActive = isActive
        // TODO: 调用API更新状态
        uni.showToast({
          title: `${action}成功`,
          icon: 'success'
        })
      } else {
        // 取消操作，恢复原状态
        dish.isActive = !isActive
      }
    }
  })
}

/**
 * 添加菜品
 */
const addDish = (categoryId) => {
  uni.navigateTo({
    url: `/pages-merchant/dish/add?category=${categoryId}`
  })
}

/**
 * 编辑菜品
 */
const editDish = (dish) => {
  uni.navigateTo({
    url: `/pages-merchant/dish/edit?id=${dish.id}`
  })
}

/**
 * 删除菜品
 */
const deleteDish = async (dish) => {
  const confirmed = await showConfirm(`确认删除菜品"${dish.name}"吗？`)

  if (confirmed) {
    // TODO: 调用API删除菜品
    uni.showToast({
      title: '删除成功',
      icon: 'success'
    })

    // 从列表中移除
    for (const section of dishSections.value) {
      const index = section.dishes.findIndex(d => d.id === dish.id)
      if (index > -1) {
        section.dishes.splice(index, 1)
        break
      }
    }
  }
}

/**
 * 编辑分类
 */
const editCategory = (section) => {
  uni.showToast({
    title: '分类编辑功能开发中',
    icon: 'none'
  })
}

/**
 * 切换批量管理模式
 */
const toggleBatchMode = () => {
  batchMode.value = !batchMode.value
  selectedDishes.value = []
}

/**
 * 批量下架
 */
const batchOffShelf = () => {
  if (selectedDishes.value.length === 0) {
    uni.showToast({
      title: '请先选择菜品',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认下架',
    content: `确认下架选中的 ${selectedDishes.value.length} 个菜品吗？`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API批量下架
        uni.showToast({
          title: '下架成功',
          icon: 'success'
        })
        batchMode.value = false
        selectedDishes.value = []
        loadMenuList()
      }
    }
  })
}

/**
 * 批量删除
 */
const batchDelete = () => {
  if (selectedDishes.value.length === 0) {
    uni.showToast({
      title: '请先选择菜品',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '确认删除',
    content: `确认删除选中的 ${selectedDishes.value.length} 个菜品吗？删除后不可恢复。`,
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用API批量删除
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
        batchMode.value = false
        selectedDishes.value = []
        loadMenuList()
      }
    }
  })
}

/**
 * 显示菜单操作
 */
const showMenuActions = () => {
  uni.showActionSheet({
    itemList: ['添加分类', '分类排序', '导出菜单'],
    success: (res) => {
      if (res.tapIndex === 0) {
        // 添加分类
        uni.showToast({
          title: '添加分类功能开发中',
          icon: 'none'
        })
      } else if (res.tapIndex === 1) {
        // 分类排序
        uni.showToast({
          title: '分类排序功能开发中',
          icon: 'none'
        })
      } else if (res.tapIndex === 2) {
        // 导出菜单
        uni.showToast({
          title: '导出菜单功能开发中',
          icon: 'none'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.menu-manage-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 顶部提示 */
.tips-card {
  background: #FFF7E6;
  padding: 20rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 15rpx;
  border-bottom: 1rpx solid #FFD666;
}

.tips-text {
  flex: 1;
  font-size: 26rpx;
  color: #FF6B35;
}

/* 分类菜单 */
.category-menu {
  background: #fff;
  border-bottom: 1rpx solid #eee;
}

.category-scroll {
  white-space: nowrap;
  padding: 20rpx 30rpx;
}

.category-item {
  display: inline-block;
  padding: 12rpx 30rpx;
  margin-right: 20rpx;
  font-size: 28rpx;
  color: #666;
  border-radius: 30rpx;
  background: #F5F5F5;

  &.active {
    background: #FF6B35;
    color: #fff;
    font-weight: bold;
  }

  &:last-child {
    margin-right: 0;
  }
}

.count {
  font-size: 24rpx;
  margin-left: 5rpx;
  opacity: 0.8;
}

/* 菜品列表 */
.dish-list {
  flex: 1;
  padding: 20rpx;
}

.list-section {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 30rpx;
  border-bottom: 1rpx solid #eee;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.section-actions {
  display: flex;
  gap: 15rpx;
}

.section-actions .action-btn {
  width: 50rpx;
  height: 50rpx;
  background: #F5F5F5;
  border-radius: 50%;
  @include flex-center;
}

/* 菜品区域 */
.dish-area {
  width: 100%;
  height: auto;
}

.dish-item {
  width: 100%;
  position: relative;
}

.dish-movable {
  width: 100%;
  height: auto;
}

.dish-content {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #eee;
  background: #fff;

  &:last-child {
    border-bottom: none;
  }
}

.drag-handle {
  width: 40rpx;
  @include flex-center;
  flex-shrink: 0;
}

.dish-image {
  width: 100rpx;
  height: 100rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.dish-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  @include text-ellipsis;
}

.dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag {
  padding: 4rpx 12rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 20rpx;
  border-radius: 4rpx;
}

.dish-price {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B35;
}

.dish-status {
  flex-shrink: 0;
}

.dish-actions {
  display: flex;
  gap: 15rpx;
  flex-shrink: 0;
}

.dish-actions .action-btn {
  width: 50rpx;
  height: 50rpx;
  background: #F5F5F5;
  border-radius: 50%;
  @include flex-center;

  &.delete {
    background: rgba(245, 34, 45, 0.1);
  }
}

/* 空状态 */
.empty-state {
  padding: 100rpx 0;
}

/* 加载状态 */
.load-status {
  padding: 30rpx 0;
}

/* 批量操作栏 */
.batch-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 30rpx;
  border-top: 1rpx solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.05);
}

.selected-count {
  font-size: 28rpx;
  color: #333;
}

.batch-actions .action-buttons {
  display: flex;
  gap: 15rpx;
}

.batch-actions .action-btn {
  height: 70rpx;
  padding: 0 30rpx;
  border-radius: 35rpx;
  font-size: 26rpx;
  background: #F5F5F5;
  color: #666;
  border: none;

  &.danger {
    background: #F5222D;
    color: #fff;
  }
}

/* 底部操作按钮 */
.bottom-actions {
  padding: 20rpx;
  display: flex;
  gap: 20rpx;
}

.bottom-actions .action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 28rpx;
  background: #fff;
  color: #666;
  border: none;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
