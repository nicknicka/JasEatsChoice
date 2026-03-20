<template>
  <view class="review-submit-container">
    <scroll-view class="scroll-container" scroll-y>
      <!-- 评价目标信息 -->
      <view class="target-info card" v-if="targetInfo">
        <image class="target-image" :src="targetInfo.image" mode="aspectFill" />
        <view class="target-detail">
          <view class="target-name">{{ targetInfo.name }}</view>
          <view class="target-meta" v-if="targetInfo.meta">{{ targetInfo.meta }}</view>
        </view>
      </view>

      <!-- 星级评分 -->
      <view class="rating-section card">
        <view class="section-title">总体评分</view>
        <view class="rating-stars">
          <view
            class="star-item"
            v-for="i in 5"
            :key="i"
            @click="setRating(i)"
          >
            <text class="star-icon">{{ i <= rating ? '⭐' : '☆' }}</text>
          </view>
        </view>
        <view class="rating-text">{{ ratingText }}</view>
      </view>

      <!-- 评价标签 -->
      <view class="tags-section card">
        <view class="section-title">评价标签（可多选）</view>
        <view class="tags-grid">
          <view
            class="tag-item"
            :class="{ active: selectedTags.includes(tag.id) }"
            v-for="tag in reviewTags"
            :key="tag.id"
            @click="toggleTag(tag.id)"
          >
            {{ tag.label }}
          </view>
        </view>
      </view>

      <!-- 评价内容 -->
      <view class="content-section card">
        <view class="section-title">评价内容</view>
        <textarea
          class="content-input"
          v-model="reviewContent"
          placeholder="分享你的用餐体验吧~"
          :maxlength="500"
        />
        <view class="content-count">{{ reviewContent.length }}/500</view>
      </view>

      <!-- 图片上传 -->
      <view class="images-section card">
        <view class="section-title">
          <text>上传图片</text>
          <text class="section-tips">（最多9张，可选填）</text>
        </view>
        <view class="images-grid">
          <!-- 已上传的图片 -->
          <view
            class="image-item uploaded"
            v-for="(image, index) in uploadImages"
            :key="index"
          >
            <image class="upload-image" :src="image" mode="aspectFill" />
            <view class="delete-btn" @click="removeImage(index)">
              <text>×</text>
            </view>
          </view>

          <!-- 上传按钮 -->
          <view
            class="image-item upload-btn"
            v-if="uploadImages.length < 9"
            @click="chooseImage"
          >
            <text class="upload-icon">📷</text>
            <text class="upload-text">{{ uploadImages.length }}/9</text>
          </view>
        </view>
      </view>

      <!-- 匿名选项 -->
      <view class="anonymous-section card">
        <view class="anonymous-item">
          <view class="anonymous-label">匿名评价</view>
          <switch
            :checked="isAnonymous"
            @change="toggleAnonymous"
            color="#FF6B35"
          />
        </view>
        <view class="anonymous-tips">
          开启后，您的评价将以匿名形式展示，其他人无法看到您的信息
        </view>
      </view>
    </scroll-view>

    <!-- 底部提交栏 -->
    <view class="bottom-bar">
      <button class="submit-btn" @click="submitReview" :disabled="!canSubmit">
        提交评价
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { reviewApi } from '@/api'

// Store
const userStore = useUserStore()

// 状态
const orderId = ref('')
const targetType = ref('order') // order 或 dish
const targetId = ref('')

// 评分
const rating = ref(0)

// 计算属性：评分文字
const ratingText = computed(() => {
  const texts = ['请评分', '非常差', '差', '一般', '好', '非常好']
  return texts[rating.value]
})

// 选中的标签
const selectedTags = ref([])

// 评价内容
const reviewContent = ref('')

// 上传的图片
const uploadImages = ref([])

// 是否匿名
const isAnonymous = ref(false)

// 是否可以提交
const canSubmit = computed(() => {
  return rating.value > 0 && reviewContent.value.trim().length > 0
})

// 评价目标信息
const targetInfo = ref(null)

// 评价标签
const reviewTags = ref([
  { id: 'tasty', label: '味道好' },
  { id: 'portion', label: '分量足' },
  { id: 'fresh', label: '食材新鲜' },
  { id: 'clean', label: '卫生好' },
  { id: 'fast', label: '配送快' },
  { id: 'warm', label: '服务好' },
  { id: 'value', label: '性价比高' },
  { id: 'packaging', label: '包装精美' }
])

/**
 * 设置评分
 */
const setRating = (value) => {
  rating.value = value
}

/**
 * 切换标签
 */
const toggleTag = (tagId) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagId)
  }
}

/**
 * 选择图片
 */
const chooseImage = async () => {
  const remainCount = 9 - uploadImages.value.length

  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePaths = res.tempFilePaths

      try {
        uni.showLoading({ title: '上传中...' })

        // 上传图片到服务器
        const uploadPromises = tempFilePaths.map(filePath => {
          return new Promise((resolve, reject) => {
            uni.uploadFile({
              url: `${getApp().globalData.baseUrl}/v1/reviews/images`,
              filePath: filePath,
              name: 'file',
              header: {
                'Authorization': `Bearer ${uni.getStorageSync('token')}`
              },
              success: (uploadRes) => {
                const data = JSON.parse(uploadRes.data)
                if (data.success) {
                  resolve(data.data)
                } else {
                  reject(new Error(data.message))
                }
              },
              fail: reject
            })
          })
        })

        const uploadedUrls = await Promise.all(uploadPromises)
        uploadImages.value.push(...uploadedUrls)

        uni.hideLoading()
        uni.showToast({
          title: '上传成功',
          icon: 'success'
        })
      } catch (error) {
        console.error('上传图片失败:', error)
        uni.hideLoading()
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 删除图片
 */
const removeImage = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这张图片吗？',
    success: (res) => {
      if (res.confirm) {
        uploadImages.value.splice(index, 1)
      }
    }
  })
}

/**
 * 切换匿名
 */
const toggleAnonymous = (e) => {
  isAnonymous.value = e.detail.value
}

/**
 * REVIEW-004: 提交评价
 */
const submitReview = async () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请先评分并填写评价内容',
      icon: 'none'
    })
    return
  }

  if (!userStore.isLogin) {
    uni.showToast({
      title: '请先登录',
      icon: 'none'
    })
    setTimeout(() => {
      uni.navigateTo({
        url: '/pages/login/index'
      })
    }, 1500)
    return
  }

  try {
    uni.showLoading({
      title: '提交中...'
    })

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // REVIEW-004: 准备评价数据
    const reviewData = {
      userId,
      targetType: targetType.value,
      targetId: targetId.value,
      orderId: orderId.value || null,
      rating: rating.value,
      content: reviewContent.value,
      tags: selectedTags.value,
      images: uploadImages.value,
      isAnonymous: isAnonymous.value
    }

    // REVIEW-004: 调用后端API提交评价
    const res = await reviewApi.create(reviewData)

    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({
        title: '评价成功',
        icon: 'success'
      })

      setTimeout(() => {
        // 返回上一页或跳转到评价列表
        uni.navigateBack()
      }, 1500)
    } else {
      throw new Error(res.message || '提交失败')
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '提交失败，请重试',
      icon: 'none'
    })
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.orderId) {
    orderId.value = options.orderId
  }

  if (options.type) {
    targetType.value = options.type
  }

  if (options.id) {
    targetId.value = options.id
  }

  // REVIEW-003: 根据targetId加载评价目标信息
  await loadTargetInfo()
})

/**
 * REVIEW-003: 加载评价目标信息
 */
const loadTargetInfo = async () => {
  try {
    // U-031: 根据targetId加载信息
    if (targetType.value === 'dish') {
      // 调用菜品详情API获取菜品信息
      try {
        const { dishApi } = await import('@/api')
        const res = await dishApi.getDetail(targetId.value)

        if (res && res.data) {
          targetInfo.value = {
            name: res.data.dishName || res.data.name,
            image: res.data.image || res.data.coverImage,
            meta: `${res.data.merchantName || '商家'} - ¥${res.data.price}`
          }
        }
      } catch (error) {
        console.error('加载菜品信息失败，使用模拟数据:', error)
        // 使用模拟数据作为降级处理
        targetInfo.value = {
          name: '宫保鸡丁',
          image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=宫保鸡丁',
          meta: '老王家常菜 - ¥28'
        }
      }
    } else if (targetType.value === 'merchant') {
      // 调用商家详情API获取商家信息
      try {
        const { merchantApi } = await import('@/api')
        const res = await merchantApi.getDetail(targetId.value)

        if (res && res.data) {
          targetInfo.value = {
            name: res.data.merchantName || res.data.name,
            image: res.data.logo || res.data.avatar || res.data.coverImage,
            meta: `${res.data.category || '餐饮'} - ${res.data.rating || 4.5}分`
          }
        }
      } catch (error) {
        console.error('加载商家信息失败，使用模拟数据:', error)
        // 使用模拟数据作为降级处理
        targetInfo.value = {
          name: '老王家常菜',
          image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王',
          meta: '川菜 - 4.7分'
        }
      }
    } else if (targetType.value === 'order') {
      // 调用订单详情API获取订单信息
      try {
        const { orderApi } = await import('@/api')
        const res = await orderApi.getDetail(orderId.value)

        if (res && res.data) {
          targetInfo.value = {
            name: res.data.merchantName || res.data.merchant?.name,
            image: res.data.merchantImage || res.data.merchant?.logo || res.data.merchant?.avatar,
            meta: `${res.data.createTime || res.data.createdAt} 订单`
          }
        }
      } catch (error) {
        console.error('加载订单信息失败，使用模拟数据:', error)
        // 使用模拟数据作为降级处理
        targetInfo.value = {
          name: '老王家常菜',
          image: 'https://via.placeholder.com/200x200/FF6B35/FFFFFF?text=老王',
          meta: '2026-03-17 订单'
        }
      }
    }
  } catch (error) {
    console.error('加载目标信息失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.review-submit-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

.scroll-container {
  height: calc(100vh - 120rpx);
}

.card {
  background-color: $bg-color-white;
  margin-bottom: $spacing-md;
  padding: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-md;

  .section-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    font-weight: $font-weight-normal;
    margin-left: $spacing-xs;
  }
}

/* 评价目标信息 */
.target-info {
  @include flex-center;
  gap: $spacing-md;
}

.target-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: $border-radius-base;
}

.target-detail {
  flex: 1;
}

.target-name {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
  margin-bottom: $spacing-xs;
}

.target-meta {
  font-size: $font-size-sm;
  color: $text-color-secondary;
}

/* 星级评分 */
.rating-section {
  text-align: center;
}

.rating-stars {
  @include flex-center;
  gap: $spacing-lg;
  margin: $spacing-lg 0;
}

.star-item {
  .star-icon {
    font-size: 80rpx;
    color: #ddd;
  }

  &:active {
    transform: scale(0.9);
  }
}

.rating-text {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

/* 评价标签 */
.tags-section {
  .tags-grid {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .tag-item {
    padding: $spacing-sm $spacing-md;
    font-size: $font-size-base;
    color: $text-color-regular;
    background-color: $bg-color-base;
    border-radius: $border-radius-base;
    border: 1rpx solid $border-color-base;

    &.active {
      color: $primary-color;
      background-color: rgba(255, 107, 53, 0.1);
      border-color: $primary-color;
      font-weight: $font-weight-medium;
    }
  }
}

/* 评价内容 */
.content-section {
  .content-input {
    width: 100%;
    min-height: 200rpx;
    padding: $spacing-sm;
    background-color: $bg-color-base;
    border-radius: $border-radius-base;
    font-size: $font-size-base;
    color: $text-color-primary;
    line-height: $line-height-lg;
  }

  .content-count {
    text-align: right;
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-top: $spacing-sm;
  }
}

/* 图片上传 */
.images-section {
  .images-grid {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  .image-item {
    width: 200rpx;
    height: 200rpx;
    position: relative;
  }

  .upload-image {
    width: 100%;
    height: 100%;
    border-radius: $border-radius-base;
  }

  .delete-btn {
    position: absolute;
    top: -$spacing-xs;
    right: -$spacing-xs;
    width: 48rpx;
    height: 48rpx;
    @include flex-center;
    background-color: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    color: #fff;
    font-size: $font-size-xl;
  }

  .upload-btn {
    background-color: $bg-color-base;
    border: 2rpx dashed $border-color-base;
    border-radius: $border-radius-base;
    @include flex-center-column;
    gap: $spacing-xs;
  }

  .upload-icon {
    font-size: 64rpx;
  }

  .upload-text {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}

/* 匿名选项 */
.anonymous-section {
  .anonymous-item {
    @include flex-between;
    align-items: center;
  }

  .anonymous-label {
    font-size: $font-size-base;
    color: $text-color-primary;
  }

  .anonymous-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
    margin-top: $spacing-sm;
    line-height: $line-height-lg;
  }
}

/* 底部提交栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
}

.submit-btn {
  width: 100%;
  height: 80rpx;
  @include flex-center;
  background-color: $primary-color;
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;

  &:disabled {
    opacity: 0.6;
  }
}
</style>
