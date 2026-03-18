<template>
  <view class="image-preview" v-if="visible" @click="close">
    <!-- 背景遮罩 -->
    <view class="preview-mask" @click.stop="close"></view>

    <!-- 主容器 -->
    <view class="preview-container" @click.stop>
      <!-- 顶部操作栏 -->
      <view class="preview-header">
        <view class="header-left">
          <text class="current-index">{{ currentIndex + 1 }}</text>
          <text class="separator">/</text>
          <text class="total-count">{{ images.length }}</text>
        </view>
        <view class="header-right">
          <view class="action-btn" @click="downloadImage">
            <text class="icon">⬇</text>
            <text class="text">保存</text>
          </view>
          <view class="close-btn" @click="close">
            <text class="icon">×</text>
          </view>
        </view>
      </view>

      <!-- 图片展示区域 -->
      <swiper
        class="preview-swiper"
        :current="currentIndex"
        @change="onSwiperChange"
        circular
      >
        <swiper-item v-for="(image, index) in images" :key="index">
          <view class="swiper-item-content">
            <!-- 网络图片加载状态 -->
            <view class="image-loading" v-if="loadingStates[index]">
              <view class="loading-spinner"></view>
              <text class="loading-text">加载中...</text>
            </view>

            <!-- 图片 -->
            <image
              class="preview-image"
              :src="image"
              mode="aspectFit"
              @load="onImageLoad(index)"
              @error="onImageError(index)"
              :class="{ loaded: !loadingStates[index] && !errorStates[index] }"
            ></image>

            <!-- 加载失败 -->
            <view class="image-error" v-if="errorStates[index]">
              <text class="error-icon">❌</text>
              <text class="error-text">加载失败</text>
            </view>

            <!-- 缩放提示 -->
            <view class="zoom-hint" v-if="!loadingStates[index] && !errorStates[index] && showZoomHint">
              <text class="hint-text">双指缩放查看</text>
            </view>
          </view>
        </swiper-item>
      </swiper>

      <!-- 底部指示器 -->
      <view class="preview-indicator">
        <view
          class="indicator-dot"
          v-for="(image, index) in images"
          :key="index"
          :class="{ active: index === currentIndex }"
        ></view>
      </view>

      <!-- 左右切换按钮 -->
      <view class="preview-arrows" v-if="images.length > 1">
        <view class="arrow-btn arrow-left" @click="prevImage" v-if="currentIndex > 0">
          <text class="arrow-icon">‹</text>
        </view>
        <view class="arrow-btn arrow-right" @click="nextImage" v-if="currentIndex < images.length - 1">
          <text class="arrow-icon">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  // 图片列表
  images: {
    type: Array,
    default: () => []
  },
  // 初始显示索引
  initialIndex: {
    type: Number,
    default: 0
  },
  // 是否显示
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'change', 'download'])

// 当前索引
const currentIndex = ref(0)

// 加载状态
const loadingStates = ref({})

// 错误状态
const errorStates = ref({})

// 显示缩放提示
const showZoomHint = ref(true)

// 缩放提示定时器
let hintTimer = null

// 监听visible变化
watch(() => props.visible, (val) => {
  if (val) {
    currentIndex.value = props.initialIndex

    // 初始化加载状态
    loadingStates.value = {}
    errorStates.value = {}
    props.images.forEach((_, index) => {
      loadingStates.value[index] = true
      errorStates.value[index] = false
    })

    // 显示缩放提示
    showZoomHint.value = true
    hintTimer = setTimeout(() => {
      showZoomHint.value = false
    }, 2000)
  } else {
    if (hintTimer) {
      clearTimeout(hintTimer)
    }
  }
})

// 监听initialIndex变化
watch(() => props.initialIndex, (val) => {
  currentIndex.value = val
})

// Swiper变化
const onSwiperChange = (e) => {
  currentIndex.value = e.detail.current
  emit('change', currentIndex.value)

  // 重置缩放提示
  showZoomHint.value = true
  if (hintTimer) {
    clearTimeout(hintTimer)
  }
  hintTimer = setTimeout(() => {
    showZoomHint.value = false
  }, 2000)
}

// 图片加载完成
const onImageLoad = (index) => {
  loadingStates.value[index] = false
  errorStates.value[index] = false
}

// 图片加载失败
const onImageError = (index) => {
  loadingStates.value[index] = false
  errorStates.value[index] = true
}

// 上一张
const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

// 下一张
const nextImage = () => {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++
  }
}

// 关闭预览
const close = () => {
  emit('update:visible', false)
}

// 下载图片
const downloadImage = () => {
  const currentImage = props.images[currentIndex.value]

  uni.showLoading({ title: '保存中...' })

  uni.downloadFile({
    url: currentImage,
    success: (res) => {
      if (res.statusCode === 200) {
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.hideLoading()
            uni.showToast({
              title: '已保存到相册',
              icon: 'success'
            })
            emit('download', currentImage, currentIndex.value)
          },
          fail: () => {
            uni.hideLoading()
            uni.showToast({
              title: '保存失败',
              icon: 'error'
            })
          }
        })
      }
    },
    fail: () => {
      uni.hideLoading()
      uni.showToast({
        title: '下载失败',
        icon: 'error'
      })
    }
  })
}
</script>

<style lang="scss" scoped>
.image-preview {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;

  .preview-mask {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.95);
  }

  .preview-container {
    position: relative;
    width: 100%;
    height: 100%;

    .preview-header {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 88rpx;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 32rpx;
      background: linear-gradient(to bottom, rgba(0, 0, 0, 0.5), transparent);
      z-index: 10;

      .header-left {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .current-index {
          font-size: 32rpx;
          font-weight: bold;
          color: #ffffff;
        }

        .separator {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.6);
        }

        .total-count {
          font-size: 28rpx;
          color: rgba(255, 255, 255, 0.8);
        }
      }

      .header-right {
        display: flex;
        align-items: center;
        gap: 24rpx;

        .action-btn {
          display: flex;
          align-items: center;
          gap: 8rpx;
          padding: 12rpx 20rpx;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 32rpx;
          backdrop-filter: blur(10rpx);

          .icon {
            font-size: 28rpx;
            color: #ffffff;
          }

          .text {
            font-size: 26rpx;
            color: #ffffff;
          }

          &:active {
            opacity: 0.8;
          }
        }

        .close-btn {
          width: 56rpx;
          height: 56rpx;
          background: rgba(255, 255, 255, 0.2);
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          backdrop-filter: blur(10rpx);

          .icon {
            font-size: 48rpx;
            color: #ffffff;
            line-height: 1;
            transform: translateY(-4rpx);
          }

          &:active {
            opacity: 0.8;
          }
        }
      }
    }

    .preview-swiper {
      width: 100%;
      height: 100%;

      .swiper-item-content {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;

        .image-loading {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 16rpx;

          .loading-spinner {
            width: 60rpx;
            height: 60rpx;
            border: 4rpx solid rgba(255, 255, 255, 0.3);
            border-top-color: #ffffff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
          }

          .loading-text {
            font-size: 26rpx;
            color: rgba(255, 255, 255, 0.8);
          }
        }

        .preview-image {
          width: 100%;
          height: 100%;
          opacity: 0;
          transition: opacity 0.3s;

          &.loaded {
            opacity: 1;
          }
        }

        .image-error {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 16rpx;

          .error-icon {
            font-size: 80rpx;
          }

          .error-text {
            font-size: 26rpx;
            color: rgba(255, 255, 255, 0.6);
          }
        }

        .zoom-hint {
          position: absolute;
          bottom: 120rpx;
          left: 50%;
          transform: translateX(-50%);
          padding: 12rpx 24rpx;
          background: rgba(0, 0, 0, 0.6);
          border-radius: 32rpx;
          animation: fadeInOut 2s ease-in-out;

          .hint-text {
            font-size: 24rpx;
            color: rgba(255, 255, 255, 0.9);
          }
        }
      }
    }

    .preview-indicator {
      position: absolute;
      bottom: 80rpx;
      left: 50%;
      transform: translateX(-50%);
      display: flex;
      gap: 12rpx;
      z-index: 10;

      .indicator-dot {
        width: 12rpx;
        height: 12rpx;
        background: rgba(255, 255, 255, 0.4);
        border-radius: 50%;
        transition: all 0.3s;

        &.active {
          width: 32rpx;
          background: #ffffff;
          border-radius: 6rpx;
        }
      }
    }

    .preview-arrows {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      pointer-events: none;

      .arrow-btn {
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        width: 80rpx;
        height: 80rpx;
        background: rgba(0, 0, 0, 0.3);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        pointer-events: auto;

        &.arrow-left {
          left: 32rpx;
        }

        &.arrow-right {
          right: 32rpx;
        }

        .arrow-icon {
          font-size: 64rpx;
          color: #ffffff;
          line-height: 1;
        }

        &:active {
          background: rgba(0, 0, 0, 0.5);
        }
      }
    }
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(10rpx);
  }
  20% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  80% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateX(-50%) translateY(-10rpx);
  }
}
</style>
