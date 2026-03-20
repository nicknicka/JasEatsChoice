<template>
  <view class="wish-create-container">
    <!-- 表单 -->
    <view class="form-section">
      <!-- 心愿内容 -->
      <view class="form-item">
        <text class="label">心愿内容 <text class="required">*</text></text>
        <textarea
          class="textarea"
          v-model="formData.content"
          placeholder="描述你想要的美食，比如：想吃正宗的四川火锅，要麻辣鲜香..."
          maxlength="500"
        />
        <text class="char-count">{{ formData.content.length }}/500</text>
      </view>

      <!-- 期望菜品 -->
      <view class="form-item">
        <text class="label">期望菜品（选填）</text>
        <view class="tags-input">
          <view
            class="tag-item"
            v-for="(dish, index) in formData.dishes"
            :key="index"
          >
            {{ dish }}
            <uni-icons
              type="closeempty"
              size="14"
              color="#999"
              @tap="removeDish(index)"
            />
          </view>
          <input
            class="tag-input"
            v-model="dishInput"
            placeholder="输入菜品名称"
            @confirm="addDish"
          />
        </view>
        <text class="tips">提示：输入菜品名称后按回车添加</text>
      </view>

      <!-- 分类 -->
      <view class="form-item">
        <text class="label">分类（选填）</text>
        <picker :value="categoryIndex" :range="categoryOptions" @change="onCategoryChange">
          <view class="picker">
            <text v-if="formData.category">{{ formData.category }}</text>
            <text class="placeholder" v-else>请选择分类</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 预算 -->
      <view class="form-item">
        <text class="label">预算（选填）</text>
        <input
          class="input"
          v-model="formData.budget"
          type="digit"
          placeholder="请输入预算金额"
        />
      </view>

      <!-- 期望时间 -->
      <view class="form-item">
        <text class="label">期望时间（选填）</text>
        <picker mode="date" :value="formData.expectedTime" @change="onDateChange">
          <view class="picker">
            <text v-if="formData.expectedTime">{{ formData.expectedTime }}</text>
            <text class="placeholder" v-else>请选择日期</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 图片 -->
      <view class="form-item">
        <text class="label">图片（选填）</text>
        <view class="images-upload">
          <view
            class="image-item"
            v-for="(img, index) in formData.images"
            :key="index"
          >
            <image class="image" :src="img" mode="aspectFill"></image>
            <view class="delete-btn" @tap="removeImage(index)">
              <uni-icons type="closeempty" size="14" color="#fff"></uni-icons>
            </view>
          </view>
          <view
            class="upload-btn"
            v-if="formData.images.length < 9"
            @tap="chooseImage"
          >
            <uni-icons type="camera" size="30" color="#D9D9D9"></uni-icons>
            <text class="upload-text">添加图片</text>
          </view>
        </view>
        <text class="tips">最多可上传9张图片</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="submit-btn" @tap="submitWish" :disabled="submitting">
        {{ submitting ? '提交中...' : isEdit ? '保存修改' : '发布心愿' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { wishApi } from '@/api/modules/wish.js'

const userId = ref('')
const wishId = ref('')
const mode = ref('create') // create 或 edit
const submitting = ref(false)

// 表单数据
const formData = ref({
  content: '',
  dishes: [],
  category: '',
  budget: '',
  expectedTime: '',
  images: []
})

// 菜品输入
const dishInput = ref('')

// 分类选项
const categoryIndex = ref(0)
const categoryOptions = ['中餐', '西餐', '日韩料理', '小吃快餐', '甜品饮品', '其他']

// 是否是编辑模式
const isEdit = computed(() => mode.value === 'edit')

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  wishId.value = options.id || ''
  mode.value = options.mode || 'create'
  userId.value = uni.getStorageSync('userId') || ''

  // 如果是编辑模式，加载数据
  if (mode.value === 'edit' && wishId.value) {
    loadWishDetail()
  }
})

/**
 * 加载心愿详情（编辑模式）
 */
const loadWishDetail = async () => {
  try {
    const res = await wishApi.getDetail(wishId.value)

    if (res.code === 200 && res.data) {
      const data = res.data
      formData.value = {
        content: data.content || '',
        dishes: data.dishes || [],
        category: data.category || '',
        budget: data.budget || '',
        expectedTime: data.expectedTime || '',
        images: data.images || []
      }

      // 设置分类索引
      const idx = categoryOptions.findIndex(c => c === formData.value.category)
      if (idx !== -1) {
        categoryIndex.value = idx
      }
    }
  } catch (error) {
    console.error('加载心愿详情失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 添加菜品
 */
const addDish = () => {
  const dish = dishInput.value.trim()
  if (!dish) return

  if (formData.value.dishes.includes(dish)) {
    uni.showToast({
      title: '该菜品已添加',
      icon: 'none'
    })
    return
  }

  if (formData.value.dishes.length >= 10) {
    uni.showToast({
      title: '最多添加10个菜品',
      icon: 'none'
    })
    return
  }

  formData.value.dishes.push(dish)
  dishInput.value = ''
}

/**
 * 移除菜品
 */
const removeDish = (index) => {
  formData.value.dishes.splice(index, 1)
}

/**
 * 分类变更
 */
const onCategoryChange = (e) => {
  categoryIndex.value = e.detail.value
  formData.value.category = categoryOptions[categoryIndex.value]
}

/**
 * 日期变更
 */
const onDateChange = (e) => {
  formData.value.expectedTime = e.detail.value
}

/**
 * 选择图片
 */
const chooseImage = () => {
  const maxCount = 9 - formData.value.images.length

  uni.chooseImage({
    count: maxCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // 压缩图片
      res.tempFilePaths.forEach(tempFilePath => {
        uni.compressImage({
          src: tempFilePath,
          quality: 80,
          success: (compressRes) => {
            formData.value.images.push(compressRes.tempFilePath)
          }
        })
      })
    }
  })
}

/**
 * 移除图片
 */
const removeImage = (index) => {
  formData.value.images.splice(index, 1)
}

/**
 * WISH-004: 发布心愿
 */
const submitWish = async () => {
  // 验证表单
  if (!formData.value.content.trim()) {
    uni.showToast({
      title: '请输入心愿内容',
      icon: 'none'
    })
    return
  }

  if (formData.value.content.length < 10) {
    uni.showToast({
      title: '心愿内容至少10个字',
      icon: 'none'
    })
    return
  }

  try {
    submitting.value = true

    // 上传图片
    const uploadedImages = []
    if (formData.value.images.length > 0) {
      for (let i = 0; i < formData.value.images.length; i++) {
        try {
          const uploadRes = await uploadImage(formData.value.images[i])
          uploadedImages.push(uploadRes.url)
        } catch (error) {
          console.error('图片上传失败:', error)
        }
      }
    }

    const submitData = {
      userId: userId.value,
      content: formData.value.content,
      dishes: formData.value.dishes,
      category: formData.value.category,
      budget: formData.value.budget,
      expectedTime: formData.value.expectedTime,
      images: uploadedImages
    }

    let res
    if (mode.value === 'edit') {
      // 编辑模式
      res = await wishApi.update(wishId.value, submitData)
    } else {
      // WISH-004: 创建模式
      res = await wishApi.create(submitData)
    }

    if (res.code === 200) {
      uni.showToast({
        title: mode.value === 'edit' ? '保存成功' : '发布成功',
        icon: 'success'
      })

      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      throw new Error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交心愿失败:', error)
    uni.showToast({
      title: error.message || '提交失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

/**
 * 上传图片
 */
const uploadImage = (filePath) => {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: 'https://api.example.com/v1/upload/image',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': uni.getStorageSync('token') || ''
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data)
          if (data.code === 200) {
            resolve(data.data)
          } else {
            reject(new Error(data.message || '上传失败'))
          }
        } catch (error) {
          reject(error)
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.wish-create-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

.form-section {
  background: #fff;
  padding: 30rpx;
}

.form-item {
  margin-bottom: 40rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
  font-weight: 500;
}

.required {
  color: #F5222D;
}

.textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.char-count {
  display: block;
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
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

.picker {
  height: 80rpx;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.placeholder {
  color: #999;
}

/* 标签输入 */
.tags-input {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  min-height: 80rpx;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 20rpx;
  font-size: 24rpx;
}

.tag-input {
  flex: 1;
  min-width: 150rpx;
  font-size: 26rpx;
  color: #333;
}

.tips {
  display: block;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

/* 图片上传 */
.images-upload {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.image-item {
  position: relative;
  width: 180rpx;
  height: 180rpx;
}

.image {
  width: 100%;
  height: 100%;
  border-radius: 12rpx;
}

.delete-btn {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

.upload-btn {
  width: 180rpx;
  height: 180rpx;
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

/* 操作按钮 */
.action-buttons {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.submit-btn {
  width: 100%;
  height: 90rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 45rpx;
  font-size: 32rpx;
  border: none;

  &[disabled] {
    background: #D9D9D9;
  }
}
</style>
