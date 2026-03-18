<template>
  <view class="menu-edit-container">
    <!-- 菜单信息 -->
    <view class="menu-info-section">
      <view class="section-title">菜单信息</view>

      <view class="form-item">
        <text class="label required">菜单名称</text>
        <input
          class="input"
          v-model="menuInfo.name"
          placeholder="请输入菜单名称"
          maxlength="30"
        />
      </view>

      <view class="form-item">
        <text class="label">菜单描述</text>
        <textarea
          class="textarea"
          v-model="menuInfo.description"
          placeholder="请输入菜单描述"
          maxlength="200"
        />
      </view>
    </view>

    <!-- 分类管理 -->
    <view class="category-section">
      <view class="section-header">
        <text class="section-title">菜品分类</text>
        <view class="add-btn" @tap="addCategory">
          <uni-icons type="plus" size="18" color="#FF6B35"></uni-icons>
          <text>添加分类</text>
        </view>
      </view>

      <view class="category-list">
        <view
          class="category-item"
          v-for="(category, index) in categories"
          :key="category.id"
        >
          <view class="category-header">
            <view class="drag-handle">
              <uni-icons type="bars" size="20" color="#D9D9D9"></uni-icons>
            </view>
            <input
              class="category-name-input"
              v-model="category.name"
              placeholder="分类名称"
              maxlength="20"
            />
            <view class="category-actions">
              <view class="action-btn edit" @tap="editCategory(category)">
                <uni-icons type="compose" size="18" color="#1890FF"></uni-icons>
              </view>
              <view class="action-btn delete" @tap="deleteCategory(index)" v-if="categories.length > 1">
                <uni-icons type="trash" size="18" color="#F5222D"></uni-icons>
              </view>
            </view>
          </view>

          <!-- 分类下的菜品 -->
          <view class="category-dishes">
            <view class="dish-tags">
              <text
                class="dish-tag"
                v-for="dish in category.dishes"
                :key="dish.id"
              >
                {{ dish.name }}
                <uni-icons
                  type="closeempty"
                  size="12"
                  color="#999"
                  @tap="removeDish(category, dish)"
                />
              </text>
              <text class="add-dish-tag" @tap="addDish(category)">
                <uni-icons type="plus" size="14" color="#FF6B35"></uni-icons>
                添加菜品
              </text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 菜单预览 -->
    <view class="preview-section">
      <view class="section-header">
        <text class="section-title">菜单预览</text>
        <text class="preview-mode">{{ menuInfo.name || '未命名菜单' }}</text>
      </view>

      <view class="preview-list">
        <view
          class="preview-category"
          v-for="category in categories"
          :key="category.id"
        >
          <view class="preview-category-title">{{ category.name }}</view>
          <view class="preview-dishes">
            <text
              class="preview-dish"
              v-for="dish in category.dishes"
              :key="dish.id"
            >
              {{ dish.name }}
            </text>
            <text class="preview-empty" v-if="category.dishes.length === 0">
              暂无菜品
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 菜单设置 -->
    <view class="settings-section">
      <view class="section-title">菜单设置</view>

      <view class="setting-item switch-item">
        <view class="setting-info">
          <text class="setting-name">设为默认菜单</text>
          <text class="setting-desc">开启后将作为主要菜单展示</text>
        </view>
        <switch
          :checked="menuInfo.isDefault"
          color="#FF6B35"
          @change="onDefaultChange"
        />
      </view>

      <view class="setting-item switch-item">
        <view class="setting-info">
          <text class="setting-name">显示售罄菜品</text>
          <text class="setting-desc">在菜单中显示已售罄的菜品</text>
        </view>
        <switch
          :checked="menuInfo.showSoldOut"
          color="#FF6B35"
          @change="onSoldOutChange"
        />
      </view>

      <view class="setting-item">
        <text class="setting-name">菜单排序</text>
        <picker
          mode="selector"
          :range="sortOptions"
          range-key="label"
          :value="sortIndex"
          @change="onSortChange"
        >
          <view class="picker">
            <text class="picker-value">{{ sortOptions[sortIndex].label }}</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn" @tap="previewMenu">预览效果</button>
      <button class="action-btn primary" @tap="saveMenu">保存菜单</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showConfirm } from '@/utils/helper'

// 菜单信息
const menuInfo = ref({
  id: 1,
  name: '午餐菜单',
  description: '工作日午餐精选',
  isDefault: true,
  showSoldOut: false,
  sortOrder: 'custom'
})

// 分类列表
const categories = ref([
  {
    id: 1,
    name: '热菜',
    dishes: [
      { id: 1, name: '宫保鸡丁' },
      { id: 2, name: '鱼香肉丝' },
      { id: 3, name: '回锅肉' }
    ]
  },
  {
    id: 2,
    name: '凉菜',
    dishes: [
      { id: 4, name: '凉拌黄瓜' },
      { id: 5, name: '口水鸡' }
    ]
  },
  {
    id: 3,
    name: '汤羹',
    dishes: [
      { id: 6, name: '紫菜蛋花汤' }
    ]
  }
])

// 排序选项
const sortOptions = ref([
  { label: '自定义排序', value: 'custom' },
  { label: '按销量排序', value: 'sales' },
  { label: '按价格排序', value: 'price' },
  { label: '按评分排序', value: 'rating' },
  { label: '按创建时间', value: 'createTime' }
])

const sortIndex = ref(0)

onMounted(() => {
  loadMenuDetail()
})

/**
 * 加载菜单详情
 */
const loadMenuDetail = () => {
  // TODO: 调用API获取菜单详情
  // const res = await merchantApi.getMenuDetail({ id: menuId })
  // menuInfo.value = res.data.menu
  // categories.value = res.data.categories
}

/**
 * 添加分类
 */
const addCategory = () => {
  categories.value.push({
    id: Date.now(),
    name: '',
    dishes: []
  })
}

/**
 * 编辑分类
 */
const editCategory = (category) => {
  uni.showToast({
    title: '分类编辑功能开发中',
    icon: 'none'
  })
}

/**
 * 删除分类
 */
const deleteCategory = async (index) => {
  const category = categories.value[index]
  const hasDishes = category.dishes && category.dishes.length > 0

  let confirmMsg = '确认删除此分类吗？'
  if (hasDishes) {
    confirmMsg = `该分类下有${category.dishes.length}道菜品，删除分类不会删除菜品。确认删除吗？`
  }

  const confirmed = await showConfirm(confirmMsg)
  if (confirmed) {
    categories.value.splice(index, 1)
  }
}

/**
 * 添加菜品到分类
 */
const addDish = (category) => {
  // TODO: 跳转到菜品选择页面
  uni.showToast({
    title: '菜品选择功能开发中',
    icon: 'none'
  })
}

/**
 * 从分类移除菜品
 */
const removeDish = (category, dish) => {
  const index = category.dishes.findIndex(d => d.id === dish.id)
  if (index > -1) {
    category.dishes.splice(index, 1)
  }
}

/**
 * 默认菜单变更
 */
const onDefaultChange = (e) => {
  menuInfo.value.isDefault = e.detail.value
}

/**
 * 显示售罄变更
 */
const onSoldOutChange = (e) => {
  menuInfo.value.showSoldOut = e.detail.value
}

/**
 * 排序方式变更
 */
const onSortChange = (e) => {
  sortIndex.value = e.detail.value
  menuInfo.value.sortOrder = sortOptions.value[e.detail.value].value
}

/**
 * 预览菜单
 */
const previewMenu = () => {
  // TODO: 跳转到预览页面
  uni.showToast({
    title: '预览功能开发中',
    icon: 'none'
  })
}

/**
 * 保存菜单
 */
const saveMenu = () => {
  // 表单验证
  if (!menuInfo.value.name) {
    uni.showToast({
      title: '请输入菜单名称',
      icon: 'none'
    })
    return
  }

  for (let i = 0; i < categories.value.length; i++) {
    const category = categories.value[i]
    if (!category.name) {
      uni.showToast({
        title: `请输入第${i + 1}个分类的名称`,
        icon: 'none'
      })
      return
    }
  }

  // TODO: 调用API保存菜单
  uni.showToast({
    title: '保存成功',
    icon: 'success'
  })

  setTimeout(() => {
    uni.navigateBack()
  }, 1500)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.menu-edit-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 菜单信息 */
.menu-info-section {
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

.form-item {
  margin-bottom: 25rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 10rpx;
  font-weight: 500;

  &.required::before {
    content: '*';
    color: #F5222D;
    margin-right: 5rpx;
  }
}

.input {
  width: 100%;
  height: 80rpx;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
}

.textarea {
  width: 100%;
  min-height: 120rpx;
  padding: 15rpx 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

/* 分类管理 */
.category-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: #FF6B35;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.category-item {
  background: #F5F5F5;
  border-radius: 12rpx;
  padding: 20rpx;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.drag-handle {
  width: 40rpx;
  @include flex-center;
  flex-shrink: 0;
}

.category-name-input {
  flex: 1;
  height: 70rpx;
  padding: 0 20rpx;
  background: #fff;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.category-actions {
  display: flex;
  gap: 10rpx;
}

.action-btn {
  width: 50rpx;
  height: 50rpx;
  background: #fff;
  border-radius: 50%;
  @include flex-center;
}

.category-dishes {
  padding-left: 55rpx;
}

.dish-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.dish-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #fff;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #333;
}

.add-dish-tag {
  display: inline-flex;
  align-items: center;
  gap: 5rpx;
  padding: 8rpx 16rpx;
  background: rgba(255, 107, 53, 0.1);
  border: 1rpx dashed #FF6B35;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #FF6B35;
}

/* 菜单预览 */
.preview-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.preview-mode {
  font-size: 26rpx;
  color: #FF6B35;
  font-weight: normal;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 25rpx;
}

.preview-category {
  background: #F5F5F5;
  border-radius: 12rpx;
  padding: 20rpx;
}

.preview-category-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 15rpx;
}

.preview-dishes {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.preview-dish {
  padding: 6rpx 16rpx;
  background: #fff;
  border-radius: 20rpx;
  font-size: 24rpx;
  color: #666;
}

.preview-empty {
  font-size: 24rpx;
  color: #999;
  padding: 6rpx 16rpx;
}

/* 菜单设置 */
.settings-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.switch-item {
  align-items: flex-start;
}

.setting-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.setting-name {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.setting-desc {
  font-size: 24rpx;
  color: #999;
}

.picker {
  display: flex;
  align-items: center;
  gap: 10rpx;
  padding: 10rpx 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
}

.picker-value {
  font-size: 26rpx;
  color: #333;
}

/* 操作按钮 */
.action-buttons {
  padding: 0 20rpx;
  display: flex;
  gap: 20rpx;
}

.action-btn {
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
