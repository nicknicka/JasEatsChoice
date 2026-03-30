<template>
  <view class="recipe-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">我的食谱</view>
      <view class="nav-actions">
        <view class="action-btn" @click="toggleSearch">
          <text class="action-icon">🔍</text>
        </view>
        <view class="action-btn" @click="toggleBatchMode">
          <text class="action-icon">{{ batchMode ? '✓' : '☰' }}</text>
        </view>
      </view>
    </view>

    <!-- 搜索栏 -->
    <view class="search-bar" v-if="showSearch">
      <view class="search-input-wrapper">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="searchKeyword"
          placeholder="搜索食谱名称..."
          @input="handleSearch"
        />
        <text class="clear-icon" v-if="searchKeyword" @click="searchKeyword = ''; handleSearch()">×</text>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view class="batch-bar" v-if="batchMode">
      <view class="batch-left">
        <view class="select-all-btn" @click="toggleSelectAll">
          <text class="checkbox">{{ selectedAll ? '☑️' : '☐' }}</text>
          <text class="select-text">全选</text>
        </view>
        <text class="selected-count">已选 {{ selectedRecipes.length }} 项</text>
      </view>
      <view class="batch-right">
        <button class="batch-btn delete-btn" @click="batchDelete">删除</button>
        <button class="batch-btn share-btn" @click="batchShare">分享</button>
      </view>
    </view>

    <!-- 食谱列表 -->
    <scroll-view
      class="recipe-scroll"
      scroll-y
      :refresher-enabled="!batchMode"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="loadMore"
    >
      <!-- 空状态 -->
      <view class="empty-container" v-if="filteredRecipes.length === 0 && !loading">
        <text class="empty-icon">📖</text>
        <text class="empty-text">{{ getEmptyText() }}</text>
        <text class="empty-desc">{{ getEmptyDesc() }}</text>
        <button class="create-btn" @click="createRecipe">创建食谱</button>
      </view>

      <!-- 食谱列表 -->
      <view class="recipe-list" v-else>
        <view
          class="recipe-item"
          v-for="recipe in filteredRecipes"
          :key="recipe.id"
          @click="handleRecipeClick(recipe)"
        >
          <!-- 批选模式复选框 -->
          <view class="recipe-checkbox" v-if="batchMode" @click.stop="toggleRecipeSelect(recipe.id)">
            <text class="checkbox-icon">{{ selectedRecipes.includes(recipe.id) ? '☑️' : '☐' }}</text>
          </view>

          <!-- 食谱封面 -->
          <image
            class="recipe-image"
            :src="recipe.image || '/static/recipe-placeholder.png'"
            mode="aspectFill"
          />

          <!-- 食谱信息 -->
          <view class="recipe-info">
            <view class="recipe-header">
              <text class="recipe-name">{{ recipe.name }}</text>
              <view class="recipe-nutrition" @click.stop="viewNutrition(recipe)">
                <text class="nutrition-icon">📊</text>
                <text class="nutrition-text">{{ recipe.calories }}kcal</text>
              </view>
            </view>

            <view class="recipe-meta">
              <text class="meta-item" v-if="recipe.dishes">
                <text class="meta-icon">🍽️</text>
                {{ recipe.dishes.length }}道菜品
              </text>
              <text class="meta-item" v-if="recipe.servings">
                <text class="meta-icon">👥</text>
                {{ recipe.servings }}人份
              </text>
              <text class="meta-item" v-if="recipe.time">
                <text class="meta-icon">⏱️</text>
                {{ recipe.time }}分钟
              </text>
            </view>

            <view class="recipe-tags">
              <text
                class="recipe-tag"
                v-for="tag in recipe.tags"
                :key="tag"
              >{{ tag }}</text>
            </view>

            <text class="recipe-time">{{ formatTime(recipe.createTime) }}</text>
          </view>

          <!-- 快速操作 -->
          <view class="recipe-actions" v-if="!batchMode" @click.stop>
            <view class="action-item" @click="editRecipe(recipe)">
              <text class="action-icon">✏️</text>
            </view>
            <view class="action-item" @click="shareRecipe(recipe)">
              <text class="action-icon">📤</text>
            </view>
            <view class="action-item" @click="deleteRecipe(recipe)">
              <text class="action-icon">🗑️</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="filteredRecipes.length > 0">
        <uni-load-more
          :status="loadMoreStatus"
          :content-text="{
            contentdown: '上拉加载更多',
            contentrefresh: '加载中...',
            contentnomore: '没有更多了'
          }"
        ></uni-load-more>
      </view>
    </scroll-view>

    <!-- 悬浮创建按钮 -->
    <view class="fab-button" v-if="!batchMode" @click="createRecipe">
      <text class="fab-icon">➕</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { recipeApi } from '@/api'

// 食谱列表
const recipes = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const loadMoreStatus = ref('more')

// 搜索和筛选
const showSearch = ref(false)
const searchKeyword = ref('')
const batchMode = ref(false)
const selectedRecipes = ref([])

// 分页
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

// 计算属性：过滤后的食谱列表
const filteredRecipes = computed(() => {
  let result = [...recipes.value]

  // 搜索过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(recipe =>
      recipe.name.toLowerCase().includes(keyword) ||
      (recipe.description && recipe.description.toLowerCase().includes(keyword))
    )
  }

  return result
})

// 是否全选
const selectedAll = computed(() => {
  return filteredRecipes.value.length > 0 &&
    selectedRecipes.value.length === filteredRecipes.value.length
})

// 组件挂载
onMounted(() => {
  loadRecipes()
})

/**
 * 加载食谱列表
 */
const loadRecipes = async () => {
  if (loading.value) return

  loading.value = true

  try {
    // TODO: 调用真实API
    // const res = await recipeApi.getList({
    //   page: page.value,
    //   size: pageSize.value
    // })

    // 模拟数据
    const mockData = [
      {
        id: 1,
        name: '低脂健康餐',
        image: 'https://via.placeholder.com/400x300/FF6B35/FFFFFF?text=低脂健康餐',
        description: '适合减脂期的低热量营养餐',
        calories: 450,
        protein: 35,
        carbs: 50,
        fat: 12,
        dishes: [
          { id: 1, name: '清蒸鲈鱼', calories: 120 },
          { id: 2, name: '西兰花炒虾仁', calories: 180 },
          { id: 3, name: '紫菜蛋花汤', calories: 80 }
        ],
        servings: 2,
        time: 30,
        tags: ['低脂', '高蛋白', '健康'],
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString()
      },
      {
        id: 2,
        name: '增肌食谱',
        image: 'https://via.placeholder.com/400x300/FF6B35/FFFFFF?text=增肌食谱',
        description: '适合健身后补充蛋白质',
        calories: 680,
        protein: 55,
        carbs: 65,
        fat: 20,
        dishes: [
          { id: 1, name: '煎鸡胸肉', calories: 250 },
          { id: 2, name: '糙米饭', calories: 180 },
          { id: 3, name: '蔬菜沙拉', calories: 120 },
          { id: 4, name: '蛋白粉', calories: 130 }
        ],
        servings: 1,
        time: 20,
        tags: ['高蛋白', '增肌', '健身'],
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString()
      },
      {
        id: 3,
        name: '家常营养套餐',
        image: 'https://via.placeholder.com/400x300/FF6B35/FFFFFF?text=家常营养套餐',
        description: '营养丰富，适合全家人',
        calories: 520,
        protein: 28,
        carbs: 68,
        fat: 18,
        dishes: [
          { id: 1, name: '红烧肉', calories: 320 },
          { id: 2, name: '清炒时蔬', calories: 80 },
          { id: 3, name: '番茄蛋汤', calories: 120 }
        ],
        servings: 3,
        time: 45,
        tags: ['家常', '营养', '下饭'],
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 24 * 2).toISOString()
      },
      {
        id: 4,
        name: '轻食沙拉',
        image: 'https://via.placeholder.com/400x300/FF6B35/FFFFFF?text=轻食沙拉',
        description: '清爽低卡的轻食选择',
        calories: 380,
        protein: 22,
        carbs: 35,
        fat: 15,
        dishes: [
          { id: 1, name: '鸡胸肉沙拉', calories: 280 },
          { id: 2, name: '水果杯', calories: 100 }
        ],
        servings: 1,
        time: 15,
        tags: ['轻食', '低卡', '素食'],
        createTime: new Date(Date.now() - 1000 * 60 * 60 * 24 * 3).toISOString()
      }
    ]

    if (page.value === 1) {
      recipes.value = mockData
    } else {
      recipes.value = [...recipes.value, ...mockData]
    }

    hasMore.value = recipes.value.length < 20 // 假设总共20条
    if (!hasMore.value) {
      loadMoreStatus.value = 'noMore'
    }
  } catch (error) {
    console.error('加载食谱失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  hasMore.value = true
  loadMoreStatus.value = 'more'

  await loadRecipes()
  refreshing.value = false
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (hasMore.value && !loading.value) {
    page.value++
    loadMoreStatus.value = 'loading'
    loadRecipes()
  }
}

/**
 * 切换搜索
 */
const toggleSearch = () => {
  showSearch.value = !showSearch.value
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  // 搜索通过 computed 自动处理
}

/**
 * 切换批量模式
 */
const toggleBatchMode = () => {
  batchMode.value = !batchMode.value
  if (!batchMode.value) {
    selectedRecipes.value = []
  }
}

/**
 * 切换全选
 */
const toggleSelectAll = () => {
  if (selectedAll.value) {
    selectedRecipes.value = []
  } else {
    selectedRecipes.value = filteredRecipes.value.map(recipe => recipe.id)
  }
}

/**
 * 切换单个选择
 */
const toggleRecipeSelect = (id) => {
  const index = selectedRecipes.value.indexOf(id)
  if (index > -1) {
    selectedRecipes.value.splice(index, 1)
  } else {
    selectedRecipes.value.push(id)
  }
}

/**
 * 处理食谱点击
 */
const handleRecipeClick = (recipe) => {
  if (batchMode.value) {
    toggleRecipeSelect(recipe.id)
    return
  }

  // 跳转到食谱详情
  uni.navigateTo({
    url: `/pages/recipe/detail?id=${recipe.id}`
  })
}

/**
 * 创建食谱
 */
const createRecipe = () => {
  uni.navigateTo({
    url: '/pages/recipe/edit'
  })
}

/**
 * 编辑食谱
 */
const editRecipe = (recipe) => {
  uni.navigateTo({
    url: `/pages/recipe/edit?id=${recipe.id}`
  })
}

/**
 * 查看营养
 */
const viewNutrition = (recipe) => {
  uni.showModal({
    title: '营养成分',
    content: `卡路里: ${recipe.calories} kcal\n蛋白质: ${recipe.protein}g\n碳水: ${recipe.carbs}g\n脂肪: ${recipe.fat}g`,
    showCancel: false
  })
}

/**
 * 分享食谱
 */
const shareRecipe = (recipe) => {
  uni.showActionSheet({
    itemList: ['分享给好友', '生成海报', '复制链接'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          // 分享给好友
          uni.showToast({
            title: '分享功能开发中...',
            icon: 'none'
          })
          break
        case 1:
          // 生成海报
          uni.showToast({
            title: '海报生成功能开发中...',
            icon: 'none'
          })
          break
        case 2:
          // 复制链接
          uni.setClipboardData({
            data: `食谱分享：${recipe.name}`,
            success: () => {
              uni.showToast({
                title: '链接已复制',
                icon: 'success'
              })
            }
          })
          break
      }
    }
  })
}

/**
 * 批量分享
 */
const batchShare = () => {
  if (selectedRecipes.value.length === 0) {
    uni.showToast({
      title: '请先选择要分享的食谱',
      icon: 'none'
    })
    return
  }

  uni.showToast({
    title: `已选中 ${selectedRecipes.value.length} 个食谱`,
    icon: 'none'
  })
}

/**
 * 删除食谱
 */
const deleteRecipe = (recipe) => {
  uni.showModal({
    title: '提示',
    content: `确定要删除食谱"${recipe.name}"吗？`,
    success: (res) => {
      if (res.confirm) {
        const index = recipes.value.findIndex(r => r.id === recipe.id)
        if (index > -1) {
          recipes.value.splice(index, 1)
        }

        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 批量删除
 */
const batchDelete = () => {
  if (selectedRecipes.value.length === 0) {
    uni.showToast({
      title: '请先选择要删除的食谱',
      icon: 'none'
    })
    return
  }

  uni.showModal({
    title: '提示',
    content: `确定要删除选中的 ${selectedRecipes.value.length} 个食谱吗？`,
    success: (res) => {
      if (res.confirm) {
        // 从列表中移除
        recipes.value = recipes.value.filter(
          recipe => !selectedRecipes.value.includes(recipe.id)
        )

        selectedRecipes.value = []
        batchMode.value = false

        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
      }
    }
  })
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''

  const now = Date.now()
  const itemTime = new Date(time).getTime()
  const diff = now - itemTime

  if (diff < 1000 * 60 * 60 * 24) {
    return `${Math.floor(diff / (1000 * 60 * 60))}小时前`
  } else if (diff < 1000 * 60 * 60 * 24 * 7) {
    return `${Math.floor(diff / (1000 * 60 * 60 * 24))}天前`
  } else {
    const date = new Date(time)
    return `${date.getMonth() + 1}-${date.getDate()}`
  }
}

/**
 * 获取空状态文本
 */
const getEmptyText = () => {
  if (searchKeyword.value) return '没有找到相关食谱'
  return '暂无食谱'
}

/**
 * 获取空状态描述
 */
const getEmptyDesc = () => {
  if (searchKeyword.value) return '试试搜索其他关键词'
  return '创建您的第一个食谱吧~'
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.recipe-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  display: flex;
  flex-direction: column;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.back-icon {
  font-size: 48rpx;
  color: $text-color-primary;
  font-weight: bold;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.nav-actions {
  @include flex-center;
  gap: $spacing-md;
}

.action-btn {
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: 50%;
}

.action-icon {
  font-size: 32rpx;
}

/* 搜索栏 */
.search-bar {
  position: fixed;
  top: 88rpx;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-sm $spacing-md;
  z-index: 99;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

.search-input-wrapper {
  @include flex-center;
  padding: $spacing-sm $spacing-md;
  background-color: $bg-color-base;
  border-radius: $border-radius-round;
}

.search-icon {
  font-size: 32rpx;
  margin-right: $spacing-sm;
}

.search-input {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
}

.clear-icon {
  font-size: 36rpx;
  color: $text-color-secondary;
  padding: 0 $spacing-xs;
}

/* 批量操作栏 */
.batch-bar {
  position: fixed;
  top: 88rpx;
  left: 0;
  right: 0;
  background-color: $warning-color;
  padding: $spacing-md;
  @include flex-between;
  z-index: 99;
}

.batch-left {
  @include flex-center;
  gap: $spacing-md;
}

.select-all-btn {
  @include flex-center;
  gap: $spacing-sm;
}

.checkbox {
  font-size: 36rpx;
}

.select-text {
  font-size: $font-size-base;
  color: #fff;
  font-weight: $font-weight-bold;
}

.selected-count {
  font-size: $font-size-sm;
  color: #fff;
}

.batch-right {
  @include flex-center;
  gap: $spacing-sm;
}

.batch-btn {
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius-round;
  font-size: $font-size-sm;
  border: none;

  &::after {
    border: none;
  }
}

.delete-btn {
  background-color: #fff;
  color: $danger-color;
}

.share-btn {
  background-color: #fff;
  color: $primary-color;
}

/* 食谱列表 */
.recipe-scroll {
  flex: 1;
  margin-top: 88rpx;
  padding: $spacing-md;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 空状态 */
.empty-container {
  @include flex-center-column;
  padding: 200rpx $spacing-xl;
  text-align: center;
}

.empty-icon {
  font-size: 160rpx;
  margin-bottom: $spacing-lg;
}

.empty-text {
  font-size: $font-size-xl;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.empty-desc {
  font-size: $font-size-base;
  color: $text-color-secondary;
  margin-bottom: $spacing-xl;
}

.create-btn {
  padding: $spacing-md $spacing-xl;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;
}

/* 食谱列表 */
.recipe-list {
  .recipe-item {
    display: flex;
    background-color: $bg-color-white;
    border-radius: $border-radius-lg;
    padding: $spacing-md;
    margin-bottom: $spacing-md;
    box-shadow: $box-shadow-sm;
    position: relative;
  }
}

.recipe-checkbox {
  position: absolute;
  top: $spacing-md;
  left: $spacing-md;
  z-index: 10;
}

.checkbox-icon {
  font-size: 40rpx;
}

.recipe-image {
  width: 200rpx;
  height: 150rpx;
  border-radius: $border-radius-base;
  flex-shrink: 0;
  margin-right: $spacing-md;
}

.recipe-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding-left: batchMode.value ? '60rpx' : '0';
}

.recipe-header {
  @include flex-between;
  align-items: flex-start;
}

.recipe-name {
  flex: 1;
  font-size: $font-size-base;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.recipe-nutrition {
  @include flex-center;
  gap: 4rpx;
  padding: 4rpx 8rpx;
  background-color: rgba($success-color, 0.1);
  border-radius: $border-radius-round;
}

.nutrition-icon {
  font-size: 20rpx;
}

.nutrition-text {
  font-size: $font-size-xs;
  color: $success-color;
  font-weight: $font-weight-bold;
}

.recipe-meta {
  @include flex-center;
  gap: $spacing-sm;
  flex-wrap: wrap;
}

.meta-item {
  @include flex-center;
  gap: 4rpx;
  font-size: $font-size-xs;
  color: $text-color-secondary;
}

.meta-icon {
  font-size: 20rpx;
}

.recipe-tags {
  @include flex-center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}

.recipe-tag {
  padding: 4rpx 12rpx;
  background-color: rgba($primary-color, 0.1);
  color: $primary-color;
  border-radius: 4rpx;
  font-size: $font-size-xs;
}

.recipe-time {
  font-size: $font-size-xs;
  color: $text-color-placeholder;
}

.recipe-actions {
  flex-shrink: 0;
  @include flex-center-column;
  gap: $spacing-sm;
  padding-left: $spacing-md;
  margin-left: auto;
}

.action-item {
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  background-color: $bg-color-base;
  border-radius: 50%;
}

.action-icon {
  font-size: 28rpx;
}

/* 悬浮按钮 */
.fab-button {
  position: fixed;
  right: $spacing-xl;
  bottom: calc(100rpx + env(safe-area-inset-bottom));
  width: 120rpx;
  height: 120rpx;
  @include flex-center;
  background: linear-gradient(135deg, #FF6B35, #FF8C61);
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.4);
  z-index: 100;
}

.fab-icon {
  font-size: 48rpx;
  color: #fff;
}
</style>
