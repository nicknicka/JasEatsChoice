<template>
  <view class="step-config-container">
    <!-- 菜品信息预览 -->
    <view class="dish-preview">
      <image class="dish-image" :src="dishInfo.image" mode="aspectFill"></image>
      <view class="dish-info">
        <text class="dish-name">{{ dishInfo.name }}</text>
        <text class="dish-desc">{{ dishInfo.description }}</text>
      </view>
    </view>

    <!-- 步骤模板选择 -->
    <view class="template-section">
      <view class="section-title">
        <text>选择步骤模板</text>
        <text class="tips">（可选）</text>
      </view>
      <scroll-view scroll-x class="template-scroll">
        <view
          class="template-item"
          :class="{ active: selectedTemplate === item.id }"
          v-for="item in templates"
          :key="item.id"
          @tap="selectTemplate(item)"
        >
          <view class="template-icon">{{ item.icon }}</view>
          <text class="template-name">{{ item.name }}</text>
        </view>
        <view class="template-item custom" @tap="showAddTemplate">
          <view class="template-icon">+</view>
          <text class="template-name">新建模板</text>
        </view>
      </scroll-view>
    </view>

    <!-- 步骤配置列表 -->
    <view class="steps-section">
      <view class="section-header">
        <text class="section-title">制作步骤</text>
        <text class="step-count">共{{ steps.length }}步</text>
      </view>

      <view class="steps-list">
        <view
          class="step-item"
          v-for="(step, index) in steps"
          :key="index"
        >
          <view class="step-header">
            <view class="step-number">
              <text>{{ index + 1 }}</text>
            </view>
            <input
              class="step-name-input"
              v-model="step.name"
              placeholder="输入步骤名称"
              maxlength="20"
            />
            <view class="step-time">
              <input
                type="number"
                v-model="step.duration"
                placeholder="时长"
                maxlength="3"
              />
              <text class="unit">分钟</text>
            </view>
            <view class="step-actions">
              <view
                class="action-btn up"
                @tap="moveStep(index, -1)"
                v-if="index > 0"
              >
                <uni-icons type="up" size="18" color="#666"></uni-icons>
              </view>
              <view
                class="action-btn down"
                @tap="moveStep(index, 1)"
                v-if="index < steps.length - 1"
              >
                <uni-icons type="down" size="18" color="#666"></uni-icons>
              </view>
              <view
                class="action-btn delete"
                @tap="deleteStep(index)"
                v-if="steps.length > 1"
              >
                <uni-icons type="trash" size="18" color="#F5222D"></uni-icons>
              </view>
            </view>
          </view>

          <view class="step-content">
            <textarea
              class="step-desc"
              v-model="step.description"
              placeholder="输入步骤描述，说明具体操作方法"
              maxlength="500"
            />
            <view class="step-media">
              <view
                class="media-item"
                v-for="(media, mIndex) in step.media"
                :key="mIndex"
              >
                <image
                  v-if="media.type === 'image'"
                  class="media-image"
                  :src="media.url"
                  mode="aspectFill"
                />
                <view
                  class="media-delete"
                  @tap="deleteMedia(index, mIndex)"
                >
                  <uni-icons type="closeempty" size="14" color="#fff"></uni-icons>
                </view>
              </view>
              <view class="media-add" @tap="addMedia(index)" v-if="step.media.length < 3">
                <uni-icons type="camera" size="30" color="#D9D9D9"></uni-icons>
                <text class="add-text">添加图片</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view class="add-step-btn" @tap="addStep">
        <uni-icons type="plus" size="18" color="#FF6B35"></uni-icons>
        <text>添加步骤</text>
      </view>
    </view>

    <!-- 完成总时长 -->
    <view class="total-time-section">
      <text class="label">预计制作时长：</text>
      <text class="time-value">{{ totalDuration }}分钟</text>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn" @tap="saveAsTemplate">保存为模板</button>
      <button class="action-btn primary" @tap="submitSteps">保存步骤</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { dishApi } from '@/api/modules/dish.js'

// 菜品信息
const dishInfo = ref({
  id: 1,
  name: '宫保鸡丁',
  image: 'https://via.placeholder.com/100/FF6B35/FFFFFF?text=1',
  description: '经典川菜，麻辣鲜香'
})

// 步骤模板
const templates = ref([
  { id: 1, name: '炒菜模板', icon: '🍳', steps: [] },
  { id: 2, name: '炖菜模板', icon: '🍲', steps: [] },
  { id: 3, name: '凉菜模板', icon: '🥗', steps: [] },
  { id: 4, name: '汤羹模板', icon: '🍜', steps: [] }
])

const selectedTemplate = ref('')

// 步骤列表
const steps = ref([
  {
    name: '食材准备',
    duration: 5,
    description: '将鸡胸肉切丁，用料酒、淀粉腌制10分钟',
    media: []
  },
  {
    name: '炒制',
    duration: 8,
    description: '热锅下油，下干辣椒、花椒爆香，下鸡丁炒至变色',
    media: []
  },
  {
    name: '调味',
    duration: 2,
    description: '调入糖醋汁，翻炒均匀即可',
    media: []
  }
])

// 预计总时长
const totalDuration = computed(() => {
  return steps.value.reduce((sum, step) => sum + (Number(step.duration) || 0), 0)
})

onMounted(() => {
  // 如果从编辑页面跳转过来，可以获取已有步骤
  // const pages = getCurrentPages()
  // const prevPage = pages[pages.length - 2]
  // if (prevPage && prevPage.$vm.dishSteps) {
  //   steps.value = prevPage.$vm.dishSteps
  // }
})

/**
 * 选择模板
 */
/**
 * 选择模板 - DISH-008: 调用API获取模板步骤
 */
const selectTemplate = async (template) => {
  selectedTemplate.value = template.id

  uni.showModal({
    title: '应用模板',
    content: `确定使用"${template.name}"模板吗？当前步骤将被替换。`,
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '加载中...' })

          // 如果模板有步骤数据，直接使用
          if (template.steps && template.steps.length > 0) {
            steps.value = template.steps.map(step => ({
              name: step.name || `步骤`,
              duration: step.duration || 5,
              description: step.description || '',
              media: step.media || []
            }))

            uni.hideLoading()
            uni.showToast({
              title: '模板应用成功',
              icon: 'success'
            })
          } else {
            // 如果模板没有步骤数据，尝试从API获取
            // 注意：这里假设后端有获取模板详情的API
            // 如果后端没有此API，可以提示用户
            uni.hideLoading()
            uni.showToast({
              title: '该模板暂无步骤数据',
              icon: 'none'
            })
          }
        } catch (error) {
          console.error('应用模板失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: '应用模板失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 新建模板
 */
const showAddTemplate = () => {
  uni.showToast({
    title: '模板管理功能开发中',
    icon: 'none'
  })
}

/**
 * 添加步骤
 */
const addStep = () => {
  steps.value.push({
    name: '',
    duration: '',
    description: '',
    media: []
  })
}

/**
 * 删除步骤
 */
const deleteStep = (index) => {
  uni.showModal({
    title: '提示',
    content: '确认删除此步骤吗？',
    success: (res) => {
      if (res.confirm) {
        steps.value.splice(index, 1)
      }
    }
  })
}

/**
 * 移动步骤
 */
const moveStep = (index, direction) => {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= steps.value.length) return

  const temp = steps.value[index]
  steps.value[index] = steps.value[newIndex]
  steps.value[newIndex] = temp
}

/**
 * 添加媒体
 */
const addMedia = (stepIndex) => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      steps.value[stepIndex].media.push({
        type: 'image',
        url: res.tempFilePaths[0]
      })
    }
  })
}

/**
 * 删除媒体
 */
const deleteMedia = (stepIndex, mediaIndex) => {
  steps.value[stepIndex].media.splice(mediaIndex, 1)
}

/**
 * 保存为模板
 */
const saveAsTemplate = () => {
  uni.showModal({
    title: '保存为模板',
    content: '请输入模板名称',
    editable: true,
    placeholderText: '如：经典炒菜模板',
    success: (res) {
      if (res.confirm) {
        const templateName = res.content || '自定义模板'

        // DISH-009: 调用API保存模板（暂时使用本地存储）
        try {
          uni.showLoading({ title: '保存中...' })

          const templateData = {
            merchantId: uni.getStorageSync('merchantId') || '',
            name: templateName,
            category: dishInfo.value.category || '通用',
            steps: steps.value,
            icon: '🍳',
            totalDuration: totalDuration.value
          }

          // 暂时保存到本地存储（建议后端开发模板管理API）
          const templates = uni.getStorageSync('stepTemplates') || []
          const newTemplate = {
            id: Date.now().toString(),
            ...templateData,
            createdAt: new Date().toISOString()
          }
          templates.push(newTemplate)
          uni.setStorageSync('stepTemplates', templates)

          // 更新本地模板列表
          templates.value.push(newTemplate)

          uni.hideLoading()
          uni.showToast({
            title: '模板保存成功（本地）',
            icon: 'success'
          })
        } catch (error) {
          console.error('保存模板失败:', error)
          uni.hideLoading()
          uni.showToast({
            title: '保存失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 提交步骤
 */
const submitSteps = () => {
  // 验证步骤
  for (let i = 0; i < steps.value.length; i++) {
    const step = steps.value[i]
    if (!step.name) {
      uni.showToast({
        title: `请输入第${i + 1}步的名称`,
        icon: 'none'
      })
      return
    }
    if (!step.duration) {
      uni.showToast({
        title: `请输入第${i + 1}步的时长`,
        icon: 'none'
      })
      return
    }
  }

  // DISH-010: 调用API保存步骤
  try {
    uni.showLoading({
      title: '保存中...',
      mask: true
    })

    // 构建步骤数据
    const stepsData = steps.value.map((step, index) => ({
      stepNumber: index + 1,
      title: step.name,
      description: step.description,
      duration: parseInt(step.duration) || 0,
      media: step.media || []
    }))

    console.log('保存步骤数据:', stepsData)

    // 调用API更新菜品的cookingSteps字段
    const res = await dishApi.update(dishInfo.value.id, {
      cookingSteps: JSON.stringify(stepsData)
    })

    if (res.code === 200) {
      uni.hideLoading()
      uni.showToast({
        title: '步骤保存成功',
        icon: 'success'
      })

      // 返回上一页并传递数据
      setTimeout(() => {
        const pages = getCurrentPages()
        const prevPage = pages[pages.length - 2]
        if (prevPage && prevPage.$vm && prevPage.$vm.dishSteps) {
          prevPage.$vm.dishSteps = steps.value
        }
        uni.navigateBack()
      }, 1500)
    } else {
      throw new Error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存步骤失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '保存步骤失败',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.step-config-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 菜品预览 */
.dish-preview {
  background: #fff;
  padding: 30rpx;
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.dish-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10rpx;
}

.dish-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.dish-desc {
  font-size: 26rpx;
  color: #999;
  @include text-ellipsis(2);
}

/* 模板选择 */
.template-section {
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

.tips {
  font-size: 26rpx;
  color: #999;
  font-weight: normal;
}

.template-scroll {
  white-space: nowrap;
}

.template-item {
  display: inline-block;
  width: 140rpx;
  padding: 20rpx;
  margin-right: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  text-align: center;
  border: 2rpx solid transparent;

  &.active {
    background: rgba(255, 107, 53, 0.1);
    border-color: #FF6B35;
  }

  &.custom {
    border: 2rpx dashed #D9D9D9;
  }
}

.template-icon {
  font-size: 48rpx;
  margin-bottom: 10rpx;
}

.template-name {
  display: block;
  font-size: 24rpx;
  color: #666;
  white-space: nowrap;
}

/* 步骤配置 */
.steps-section {
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

.step-count {
  font-size: 26rpx;
  color: #999;
}

.steps-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.step-item {
  background: #F5F5F5;
  border-radius: 12rpx;
  overflow: hidden;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.step-number {
  width: 50rpx;
  height: 50rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 24rpx;
  font-weight: bold;
  border-radius: 50%;
  @include flex-center;
  flex-shrink: 0;
}

.step-name-input {
  flex: 1;
  height: 60rpx;
  padding: 0 20rpx;
  background: #fff;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
}

.step-time {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 0 15rpx;
  height: 60rpx;
  background: #fff;
  border-radius: 8rpx;

  input {
    width: 80rpx;
    font-size: 26rpx;
    color: #333;
    text-align: center;
  }

  .unit {
    font-size: 24rpx;
    color: #999;
  }
}

.step-actions {
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

.step-content {
  padding: 20rpx;
}

.step-desc {
  width: 100%;
  min-height: 120rpx;
  padding: 15rpx;
  background: #fff;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15rpx;
}

.step-media {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.media-item {
  width: 150rpx;
  height: 150rpx;
  position: relative;
  border-radius: 8rpx;
  overflow: hidden;
}

.media-image {
  width: 100%;
  height: 100%;
}

.media-delete {
  position: absolute;
  top: 5rpx;
  right: 5rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

.media-add {
  width: 150rpx;
  height: 150rpx;
  border: 2rpx dashed #D9D9D9;
  border-radius: 8rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10rpx;
}

.add-text {
  font-size: 24rpx;
  color: #999;
}

.add-step-btn {
  margin-top: 20rpx;
  height: 80rpx;
  border: 2rpx dashed #FF6B35;
  border-radius: 8rpx;
  @include flex-center;
  gap: 10rpx;
  font-size: 28rpx;
  color: #FF6B35;
}

/* 总时长 */
.total-time-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.label {
  font-size: 28rpx;
  color: #666;
}

.time-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #FF6B35;
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
