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
import { menuApi } from '@/api/modules/menu.js'

// 商家ID
const merchantId = ref('')

// 分类列表
const categories = ref([
  { label: '全部', value: 'all', count: 0 },
  { label: '热菜', value: 'hot', count: 0 },
  { label: '凉菜', value: 'cold', count: 0 },
  { label: '汤羹', value: 'soup', count: 0 },
  { label: '主食', value: 'staple', count: 0 },
  { label: '饮料', value: 'drink', count: 0 },
  { label: '小吃', value: 'snack', count: 0 }
])

const activeCategory = ref('all')

// 菜品分组
const dishSections = ref([])

// 菜单列表（用于管理）
const menuList = ref([])

const loading = ref(false)
const refreshing = ref(false)
const batchMode = ref(false)
const selectedDishes = ref([])

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
 * 加载菜单列表 - MENU-001: 调用API获取菜单列表
 */
const loadMenuList = async () => {
  if (loading.value) return

  loading.value = true

  try {
    // MENU-001: 调用API获取菜单列表
    const res = await menuApi.getList(merchantId.value)

    if (res.code === 200 && res.data) {
      // 保存菜单列表
      menuList.value = res.data

      // 根据选中的分类过滤菜单
      const filteredMenus = activeCategory.value === 'all'
        ? res.data
        : res.data.filter(menu => menu.category === activeCategory.value)

      // 将菜单数据转换为菜品分组格式
      dishSections.value = filteredMenus.map(menu => ({
        id: menu.id,
        name: menu.menuName || menu.name,
        dishes: (menu.dishes || []).map(dish => ({
          id: dish.id,
          name: dish.name,
          image: dish.image || '/static/default-dish.png',
          price: dish.price || 0,
          tags: generateDishTags(dish),
          // status字段映射：online→true, offline→false
          isActive: dish.status === 'online',
          y: 0,
          // 保留原始状态
          _originalStatus: dish.status,
          _globalStatus: dish.globalStatus
        }))
      }))

      // 更新分类计数
      updateCategoryCount(res.data)

      console.log('加载菜单成功，数量:', filteredMenus.length)
    } else {
      throw new Error(res.message || '获取菜单列表失败')
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
    uni.showToast({
      title: error.message || '加载菜单失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 更新分类计数
 */
const updateCategoryCount = (menus) => {
  // 重置计数
  categories.value.forEach(cat => {
    if (cat.value !== 'all') {
      cat.count = 0
    }
  })

  // 统计每个分类的菜品数
  let totalCount = 0
  menus.forEach(menu => {
    const category = categories.value.find(cat => cat.value === menu.category)
    if (category) {
      category.count += (menu.dishes || []).length
    }
    totalCount += (menu.dishes || []).length
  })

  // 更新全部分类的计数
  const allCategory = categories.value.find(cat => cat.value === 'all')
  if (allCategory) {
    allCategory.count = totalCount
  }
}

/**
 * 生成菜品标签
 */
const generateDishTags = (dish) => {
  const tags = []

  // 根据分类添加标签
  if (dish.category) {
    const categoryMap = {
      'hot': '热菜',
      'cold': '凉菜',
      'soup': '汤羹',
      'staple': '主食',
      'drink': '饮料',
      'snack': '小吃'
    }
    const categoryName = categoryMap[dish.category]
    if (categoryName) {
      tags.push(categoryName)
    }
  }

  // 如果没有标签，至少添加"推荐"
  if (tags.length === 0) {
    tags.push('推荐')
  }

  return tags
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
 * 拖拽结束 - MENU-002: 调用API更新排序
 */
const onDragEnd = async (dish, dishes) => {
  // 获取当前菜品的新位置
  const newIndex = dishes.findIndex(d => d.id === dish.id)

  if (newIndex === -1) {
    dish.y = 0
    return
  }

  // 如果位置没有变化，直接返回
  if (dish._originalIndex === newIndex) {
    dish.y = 0
    return
  }

  try {
    uni.showLoading({ title: '更新中...', mask: true })

    // 获取当前菜单ID
    const currentMenuId = getCurrentMenuId()
    if (!currentMenuId) {
      throw new Error('未找到菜单信息')
    }

    // MENU-002: 调用API更新菜品排序
    const apiRes = await menuApi.updateDishSort(currentMenuId, dish.id, newIndex)

    if (apiRes.code === 200) {
      // 更新本地数据
      dish._originalIndex = newIndex

      // 重新排序整个列表
      const sortedDishes = [...dishes].sort((a, b) => {
        const indexA = dishes.findIndex(d => d.id === a.id)
        const indexB = dishes.findIndex(d => d.id === b.id)
        return indexA - indexB
      })

      // 更新所有菜品的排序索引
      sortedDishes.forEach((d, i) => {
        const originalDish = dishes.find(item => item.id === d.id)
        if (originalDish) {
          originalDish._originalIndex = i
        }
      })

      uni.hideLoading()
      uni.showToast({
        title: '排序更新成功',
        icon: 'success'
      })
    } else {
      throw new Error(apiRes.message || '更新失败')
    }
  } catch (error) {
    console.error('更新排序失败:', error)

    // 失败时恢复原位置
    const oldIndex = dish._originalIndex || 0
    if (oldIndex !== newIndex) {
      // 简单的恢复逻辑
      dishes.splice(newIndex, 1)
      dishes.splice(oldIndex, 0, dish)
    }

    uni.hideLoading()
    uni.showToast({
      title: error.message || '排序更新失败',
      icon: 'none'
    })
  } finally {
    dish.y = 0
  }
}

/**
 * 切换菜品状态 - MENU-004: 调用API更新菜品在菜单中的状态
 */
const toggleDishStatus = async (dish, e) => {
  const isActive = e.detail.value
  const action = isActive ? '上架' : '下架'

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

          // 获取当前菜单ID（从dishSections中获取）
          const currentMenuId = getCurrentMenuId()
          if (!currentMenuId) {
            throw new Error('未找到菜单信息')
          }

          // MENU-004: 调用API更新菜品在菜单中的状态
          // 注意：后端API使用数字状态：1=上架，0=下架
          const status = isActive ? 1 : 0
          const apiRes = await menuApi.updateDishStatus(currentMenuId, dish.id, status)

          if (apiRes.code === 200) {
            // 更新本地数据
            dish.isActive = isActive
            dish._originalStatus = isActive ? 'online' : 'offline'

            uni.hideLoading()
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
          dish.isActive = !isActive

          uni.hideLoading()
          uni.showToast({
            title: error.message || '操作失败',
            icon: 'none'
          })
        }
      } else {
        // 取消操作，恢复原状态
        dish.isActive = !isActive
      }
    }
  })
}

/**
 * 获取当前菜单ID
 */
const getCurrentMenuId = () => {
  // 从dishSections中获取第一个菜单的ID（简化逻辑，实际可能需要更复杂的处理）
  if (dishSections.value.length > 0) {
    return dishSections.value[0].id
  }
  return null
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
 * 批量下架 - MENU-005: 调用API批量更新菜品状态
 */
const batchOffShelf = async () => {
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
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({
            title: '提交中...',
            mask: true
          })

          // MENU-005: 调用API批量更新菜品状态
          // 这里我们对每个菜品调用更新接口
          const currentMenuId = getCurrentMenuId()
          const promises = selectedDishes.value.map(dish =>
            menuApi.updateDishStatus(currentMenuId, dish.id, 0) // 0表示下架
          )

          await Promise.all(promises)

          uni.hideLoading()
          uni.showToast({
            title: '下架成功',
            icon: 'success'
          })

          batchMode.value = false
          selectedDishes.value = []
          loadMenuList()
        } catch (error) {
          console.error('批量下架失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: error.message || '批量下架失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 批量删除
 */
const batchDelete = () => {
  // 注意：这里应该是从菜单中移除菜品，而不是删除菜品本身
  // 但是后端API可能没有提供从菜单中移除菜品的接口
  // 暂时使用下架功能代替
  batchOffShelf()
}

/**
 * 显示菜单操作 - MENU-008: 调用API批量操作菜单
 */
const showMenuActions = () => {
  uni.showActionSheet({
    itemList: ['添加菜单', '批量上线菜单', '批量下线菜单'],
    success: async (res) => {
      if (res.tapIndex === 0) {
        // 添加菜单
        uni.navigateTo({
          url: '/pages-merchant/menu/edit'
        })
      } else if (res.tapIndex === 1) {
        // 批量上线菜单
        await batchOperateMenus('online')
      } else if (res.tapIndex === 2) {
        // 批量下线菜单
        await batchOperateMenus('offline')
      }
    }
  })
}

/**
 * 批量操作菜单 - MENU-008
 */
const batchOperateMenus = async (action) => {
  if (menuList.value.length === 0) {
    uni.showToast({
      title: '暂无菜单可操作',
      icon: 'none'
    })
    return
  }

  // 获取所有菜单ID
  const menuIds = menuList.value.map(menu => menu.id)

  try {
    uni.showLoading({
      title: '提交中...',
      mask: true
    })

    // MENU-008: 调用API批量操作菜单
    const res = await menuApi.batchOperate(menuIds, action)

    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({
        title: '操作成功',
        icon: 'success'
      })

      // 刷新列表
      loadMenuList()
    } else {
      throw new Error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('批量操作菜单失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
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
