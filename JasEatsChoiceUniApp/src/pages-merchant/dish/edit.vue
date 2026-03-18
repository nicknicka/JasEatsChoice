<template>
  <view class="dish-edit-container">
    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading" />
    </view>

    <!-- 编辑表单 -->
    <template v-else>
      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <!-- 菜品图片 -->
        <view class="form-item">
          <text class="label">菜品图片</text>
          <view class="image-upload">
            <view
              class="image-item"
              v-for="(img, index) in formData.images"
              :key="index"
            >
              <image class="upload-image" :src="img" mode="aspectFill"></image>
              <view class="delete-btn" @tap="deleteImage(index)">
                <uni-icons type="closeempty" size="16" color="#fff"></uni-icons>
              </view>
              <view class="cover-badge" v-if="index === 0">封面</view>
            </view>
            <view class="upload-btn" v-if="formData.images.length < 9" @tap="chooseImage">
              <uni-icons type="camera" size="40" color="#D9D9D9"></uni-icons>
              <text class="upload-text">添加图片</text>
              <text class="upload-tips">最多9张</text>
            </view>
          </view>
        </view>

        <!-- 菜品名称 -->
        <view class="form-item">
          <text class="label required">菜品名称</text>
          <input
            class="input"
            v-model="formData.name"
            placeholder="请输入菜品名称"
            maxlength="30"
          />
        </view>

        <!-- 菜品分类 -->
        <view class="form-item">
          <text class="label required">菜品分类</text>
          <picker
            mode="selector"
            :range="categories"
            range-key="label"
            :value="categoryIndex"
            @change="onCategoryChange"
          >
            <view class="picker">
              <text class="picker-value" :class="{ placeholder: !formData.category }">
                {{ formData.category || '请选择分类' }}
              </text>
              <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 菜品简介 -->
        <view class="form-item">
          <text class="label">菜品简介</text>
          <textarea
            class="textarea"
            v-model="formData.description"
            placeholder="请输入菜品简介，如口味、特色等"
            maxlength="200"
          />
          <view class="textarea-count">{{ formData.description.length }}/200</view>
        </view>
      </view>

      <!-- 规格价格 -->
      <view class="form-section">
        <view class="section-title">规格价格</view>

        <!-- 规格类型 -->
        <view class="form-item">
          <text class="label">规格类型</text>
          <view class="spec-type-tabs">
            <view
              class="type-tab"
              :class="{ active: formData.specType === 'single' }"
              @tap="changeSpecType('single')"
            >
              单规格
            </view>
            <view
              class="type-tab"
              :class="{ active: formData.specType === 'multi' }"
              @tap="changeSpecType('multi')"
            >
              多规格
            </view>
          </view>
        </view>

        <!-- 单规格 -->
        <template v-if="formData.specType === 'single'">
          <view class="form-item">
            <text class="label required">价格</text>
            <view class="price-input">
              <text class="price-symbol">¥</text>
              <input
                class="input"
                type="digit"
                v-model="formData.price"
                placeholder="0.00"
              />
            </view>
          </view>

          <view class="form-item">
            <text class="label">原价</text>
            <view class="price-input">
              <text class="price-symbol">¥</text>
              <input
                class="input"
                type="digit"
                v-model="formData.originalPrice"
                placeholder="0.00"
              />
            </view>
          </view>
        </template>

        <!-- 多规格 -->
        <template v-else>
          <view class="spec-list">
            <view
              class="spec-item"
              v-for="(spec, index) in formData.specs"
              :key="index"
            >
              <view class="spec-header">
                <text class="spec-name">规格 {{ index + 1 }}</text>
                <view class="delete-btn" @tap="deleteSpec(index)" v-if="formData.specs.length > 1">
                  <uni-icons type="closeempty" size="18" color="#F5222D"></uni-icons>
                </view>
              </view>
              <view class="spec-form">
                <input
                  class="input"
                  v-model="spec.name"
                  placeholder="规格名称，如：小份、中份、大份"
                  maxlength="20"
                />
                <view class="price-input">
                  <text class="price-symbol">¥</text>
                  <input
                    class="input"
                    type="digit"
                    v-model="spec.price"
                    placeholder="价格"
                  />
                </view>
              </view>
            </view>
            <view class="add-spec-btn" @tap="addSpec">
              <uni-icons type="plus" size="18" color="#FF6B35"></uni-icons>
              <text>添加规格</text>
            </view>
          </view>
        </template>
      </view>

      <!-- 详细信息 -->
      <view class="form-section">
        <view class="section-title">详细信息</view>

        <!-- 口味标签 -->
        <view class="form-item">
          <text class="label">口味标签</text>
          <view class="tag-list">
            <view
              class="tag-item"
              :class="{ active: formData.tags.includes(tag) }"
              v-for="tag in tasteTags"
              :key="tag"
              @tap="toggleTag(tag)"
            >
              {{ tag }}
            </view>
          </view>
        </view>

        <!-- 制作时长 -->
        <view class="form-item">
          <text class="label">制作时长</text>
          <view class="time-input">
            <input
              class="input"
              type="number"
              v-model="formData.cookTime"
              placeholder="请输入制作时长"
            />
            <text class="unit">分钟</text>
          </view>
        </view>

        <!-- 热量 -->
        <view class="form-item">
          <text class="label">热量</text>
          <view class="time-input">
            <input
              class="input"
              type="digit"
              v-model="formData.calories"
              placeholder="请输入热量"
            />
            <text class="unit">千卡</text>
          </view>
        </view>

        <!-- 菜品步骤 -->
        <view class="form-item">
          <text class="label">制作步骤</text>
          <view class="step-list">
            <view
              class="step-item"
              v-for="(step, index) in formData.steps"
              :key="index"
            >
              <view class="step-number">{{ index + 1 }}</view>
              <view class="step-content">
                <textarea
                  class="textarea"
                  v-model="step.content"
                  placeholder="请输入制作步骤"
                  maxlength="500"
                />
                <view class="step-actions">
                  <text class="delete-btn" @tap="deleteStep(index)">删除</text>
                </view>
              </view>
            </view>
            <view class="add-step-btn" @tap="addStep">
              <uni-icons type="plus" size="18" color="#FF6B35"></uni-icons>
              <text>添加步骤</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 销售设置 -->
      <view class="form-section">
        <view class="section-title">销售设置</view>

        <!-- 是否上架 -->
        <view class="form-item switch-item">
          <view class="switch-info">
            <text class="label">上架状态</text>
            <text class="desc">关闭后菜品将从菜单中隐藏</text>
          </view>
          <switch
            :checked="formData.isActive"
            color="#FF6B35"
            @change="onActiveChange"
          />
        </view>

        <!-- 限购数量 -->
        <view class="form-item">
          <text class="label">限购数量</text>
          <view class="limit-input">
            <input
              class="input"
              type="number"
              v-model="formData.limitCount"
              placeholder="不填则不限购"
            />
            <text class="unit">份/单</text>
          </view>
        </view>
      </view>

      <!-- 数据统计 -->
      <view class="form-section">
        <view class="section-title">数据统计</view>
        <view class="stats-grid">
          <view class="stat-item">
            <text class="stat-value">{{ dishStats.sales }}</text>
            <text class="stat-label">总销量</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ dishStats.rating }}</text>
            <text class="stat-label">评分</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ dishStats.reviews }}</text>
            <text class="stat-label">评价数</text>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <button class="action-btn danger" @tap="deleteDish">删除菜品</button>
        <button class="action-btn primary" @tap="submitDish">保存修改</button>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showConfirm } from '@/utils/helper'

const loading = ref(true)
const dishId = ref('')

// 表单数据
const formData = ref({
  images: [],
  name: '',
  category: '',
  description: '',
  specType: 'single',
  price: '',
  originalPrice: '',
  specs: [
    { name: '', price: '' }
  ],
  tags: [],
  cookTime: '',
  calories: '',
  steps: [
    { content: '' }
  ],
  isActive: true,
  limitCount: ''
})

// 分类列表
const categories = ref([
  { label: '热菜', value: 'hot' },
  { label: '凉菜', value: 'cold' },
  { label: '汤羹', value: 'soup' },
  { label: '主食', value: 'staple' },
  { label: '饮料', value: 'drink' },
  { label: '小吃', value: 'snack' }
])

const categoryIndex = ref(-1)

// 口味标签
const tasteTags = ref(['微辣', '中辣', '特辣', '不辣', '酸', '甜', '清淡'])

// 菜品统计
const dishStats = ref({
  sales: 0,
  rating: 0,
  reviews: 0
})

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  dishId.value = options.id

  if (dishId.value) {
    loadDishDetail()
  }
})

/**
 * 加载菜品详情
 */
const loadDishDetail = async () => {
  loading.value = true

  try {
    // TODO: 调用API获取菜品详情
    // const res = await merchantApi.getDishDetail({ id: dishId.value })
    // formData.value = res.data

    // 模拟数据
    setTimeout(() => {
      formData.value = {
        images: [
          'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=1',
          'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=2'
        ],
        name: '宫保鸡丁',
        category: '热菜',
        description: '经典川菜，麻辣鲜香，鸡肉嫩滑，花生香脆',
        specType: 'single',
        price: '28',
        originalPrice: '32',
        specs: [{ name: '', price: '' }],
        tags: ['中辣', '微甜'],
        cookTime: '15',
        calories: '280',
        steps: [
          { content: '将鸡胸肉切丁，用料酒、淀粉腌制10分钟' },
          { content: '热锅下油，下花生米炸至金黄捞起' },
          { content: '下干辣椒、花椒爆香' },
          { content: '下鸡丁炒至变色' },
          { content: '调入糖醋汁，翻炒均匀即可' }
        ],
        isActive: true,
        limitCount: ''
      }

      dishStats.value = {
        sales: 156,
        rating: 4.8,
        reviews: 89
      }

      categoryIndex.value = 0

      loading.value = false
    }, 500)
  } catch (error) {
    console.error('加载菜品详情失败:', error)
    loading.value = false
  }
}

/**
 * 选择分类
 */
const onCategoryChange = (e) => {
  categoryIndex.value = e.detail.value
  formData.value.category = categories.value[e.detail.value].label
}

/**
 * 切换规格类型
 */
const changeSpecType = (type) => {
  formData.value.specType = type
  if (type === 'single') {
    formData.value.specs = [{ name: '', price: '' }]
  }
}

/**
 * 添加规格
 */
const addSpec = () => {
  if (formData.value.specs.length < 10) {
    formData.value.specs.push({ name: '', price: '' })
  } else {
    uni.showToast({
      title: '最多添加10个规格',
      icon: 'none'
    })
  }
}

/**
 * 删除规格
 */
const deleteSpec = (index) => {
  formData.value.specs.splice(index, 1)
}

/**
 * 切换标签
 */
const toggleTag = (tag) => {
  const index = formData.value.tags.indexOf(tag)
  if (index > -1) {
    formData.value.tags.splice(index, 1)
  } else {
    if (formData.value.tags.length < 5) {
      formData.value.tags.push(tag)
    } else {
      uni.showToast({
        title: '最多选择5个标签',
        icon: 'none'
      })
    }
  }
}

/**
 * 添加步骤
 */
const addStep = () => {
  formData.value.steps.push({ content: '' })
}

/**
 * 删除步骤
 */
const deleteStep = (index) => {
  if (formData.value.steps.length > 1) {
    formData.value.steps.splice(index, 1)
  } else {
    uni.showToast({
      title: '至少保留一个步骤',
      icon: 'none'
    })
  }
}

/**
 * 选择图片
 */
const chooseImage = () => {
  const remainCount = 9 - formData.value.images.length
  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      formData.value.images.push(...res.tempFilePaths)
    }
  })
}

/**
 * 删除图片
 */
const deleteImage = (index) => {
  uni.showModal({
    title: '提示',
    content: '确认删除此图片吗？',
    success: (res) => {
      if (res.confirm) {
        formData.value.images.splice(index, 1)
      }
    }
  })
}

/**
 * 上架状态变更
 */
const onActiveChange = (e) => {
  formData.value.isActive = e.detail.value
}

/**
 * 删除菜品
 */
const deleteDish = async () => {
  const confirmed = await showConfirm('确认删除此菜品吗？删除后不可恢复。')

  if (confirmed) {
    // TODO: 调用API删除菜品
    uni.showToast({
      title: '删除成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }
}

/**
 * 保存修改
 */
const submitDish = () => {
  // 表单验证
  if (!formData.value.name) {
    uni.showToast({
      title: '请输入菜品名称',
      icon: 'none'
    })
    return
  }

  if (!formData.value.category) {
    uni.showToast({
      title: '请选择菜品分类',
      icon: 'none'
    })
    return
  }

  if (formData.value.specType === 'single' && !formData.value.price) {
    uni.showToast({
      title: '请输入价格',
      icon: 'none'
    })
    return
  }

  // TODO: 调用API保存修改
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

.dish-edit-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

.loading-container {
  padding-top: 200rpx;
}

.form-section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 15rpx;
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
  min-height: 150rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.textarea-count {
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

/* 图片上传 */
.image-upload {
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
  top: 10rpx;
  right: 10rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

.cover-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40rpx;
  background: rgba(255, 107, 53, 0.9);
  color: #fff;
  font-size: 22rpx;
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
  font-size: 26rpx;
  color: #666;
}

.upload-tips {
  font-size: 22rpx;
  color: #999;
}

/* 选择器 */
.picker {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 80rpx;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
}

.picker-value {
  font-size: 28rpx;
  color: #333;

  &.placeholder {
    color: #999;
  }
}

/* 规格类型 */
.spec-type-tabs {
  display: flex;
  gap: 20rpx;
}

.type-tab {
  flex: 1;
  height: 70rpx;
  border-radius: 35rpx;
  background: #F5F5F5;
  @include flex-center;
  font-size: 28rpx;
  color: #666;

  &.active {
    background: #FF6B35;
    color: #fff;
  }
}

/* 价格输入 */
.price-input {
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;

  .input {
    flex: 1;
    height: 80rpx;
    background: transparent;
    padding: 0 10rpx;
  }
}

.price-symbol {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

/* 多规格列表 */
.spec-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.spec-item {
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.spec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15rpx;
}

.spec-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.spec-form {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.add-spec-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  height: 80rpx;
  border: 2rpx dashed #FF6B35;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #FF6B35;
}

/* 标签列表 */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.tag-item {
  padding: 10rpx 24rpx;
  background: #F5F5F5;
  border-radius: 20rpx;
  font-size: 26rpx;
  color: #666;

  &.active {
    background: rgba(255, 107, 53, 0.1);
    color: #FF6B35;
    border: 1rpx solid #FF6B35;
  }
}

/* 时间输入 */
.time-input,
.limit-input {
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;

  .input {
    flex: 1;
    height: 80rpx;
    background: transparent;
    padding: 0;
  }
}

.unit {
  font-size: 26rpx;
  color: #999;
  margin-left: 15rpx;
}

/* 步骤列表 */
.step-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.step-item {
  display: flex;
  gap: 15rpx;
}

.step-number {
  width: 50rpx;
  height: 50rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
  border-radius: 50%;
  flex-shrink: 0;
  @include flex-center;
}

.step-content {
  flex: 1;
}

.step-actions {
  margin-top: 10rpx;
}

.step-actions .delete-btn {
  font-size: 26rpx;
  color: #F5222D;
}

.add-step-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  height: 80rpx;
  border: 2rpx dashed #FF6B35;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #FF6B35;
}

/* 开关项 */
.switch-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
}

.switch-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.desc {
  font-size: 24rpx;
  color: #999;
}

/* 数据统计 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  padding: 25rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF6B35;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

/* 操作按钮 */
.action-buttons {
  padding: 40rpx 20rpx 0;
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

  &.danger {
    background: #F5222D;
    color: #fff;
  }
}
</style>
