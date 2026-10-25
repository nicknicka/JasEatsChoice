<template>
  <view class="image-upload">
    <view class="image-list">
      <!-- 已上传的图片 -->
      <view
        class="image-item"
        v-for="(item, index) in imageList"
        :key="index"
        :style="{ width: width + 'rpx', height: height + 'rpx' }"
      >
        <image class="image" :src="item.url" mode="aspectFill" @click="previewImage(index)"></image>

        <!-- 上传进度 -->
        <view class="upload-progress" v-if="item.status === 'uploading'">
          <view class="progress-bar">
            <view class="progress-value" :style="{ width: item.progress + '%' }"></view>
          </view>
          <text class="progress-text">{{ item.progress }}%</text>
        </view>

        <!-- 上传失败 -->
        <view class="upload-fail" v-if="item.status === 'fail'" @click="retryUpload(index)">
          <text class="fail-icon">⚠️</text>
          <text class="fail-text">点击重试</text>
        </view>

        <!-- 删除按钮 -->
        <view class="delete-btn" v-if="!disabled && item.status !== 'uploading'" @click="deleteImage(index)">
          <text class="delete-icon">×</text>
        </view>

        <!-- 遮罩（上传中） -->
        <view class="mask" v-if="item.status === 'uploading'"></view>
      </view>

      <!-- 上传按钮 -->
      <view
        class="upload-btn"
        v-if="!disabled && imageList.length < maxCount"
        :style="{ width: width + 'rpx', height: height + 'rpx' }"
        @click="chooseImage"
      >
        <text class="upload-icon">+</text>
        <text class="upload-text">{{ imageList.length }}/{{ maxCount }}</text>
      </view>
    </view>

    <!-- 提示信息 -->
    <view class="upload-tip" v-if="showTip">
      <text class="tip-icon">ℹ️</text>
      <text class="tip-text">{{ tipText }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  // 图片列表
  modelValue: {
    type: Array,
    default: () => []
  },
  // 最大上传数量
  maxCount: {
    type: Number,
    default: 9
  },
  // 图片宽度
  width: {
    type: Number,
    default: 200
  },
  // 图片高度
  height: {
    type: Number,
    default: 200
  },
  // 是否禁用
  disabled: {
    type: Boolean,
    default: false
  },
  // 显示提示
  showTip: {
    type: Boolean,
    default: true
  },
  // 提示文本
  tipText: {
    type: String,
    default: '最多上传9张图片'
  },
  // 图片质量（0-100）
  quality: {
    type: Number,
    default: 80
  },
  // 是否压缩
  compress: {
    type: Boolean,
    default: true
  },
  // 上传接口
  action: {
    type: String,
    default: '/api/upload/image'
  }
})

const emit = defineEmits(['update:modelValue', 'change', 'uploadSuccess', 'uploadFail'])

// 图片列表
const imageList = ref([])

// 监听外部数据变化
watch(
  () => props.modelValue,
  (val) => {
    if (val && val.length > 0) {
      imageList.value = val.map(url => ({
        url,
        status: 'success',
        progress: 100
      }))
    } else {
      imageList.value = []
    }
  },
  { immediate: true }
)

// 选择图片
const chooseImage = () => {
  const remainCount = props.maxCount - imageList.value.length

  uni.chooseImage({
    count: remainCount,
    sizeType: props.compress ? ['compressed'] : ['original', 'compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePaths = res.tempFilePaths

      // 添加到列表
      tempFilePaths.forEach(filePath => {
        imageList.value.push({
          url: filePath,
          status: 'uploading',
          progress: 0
        })
      })

      // 上传图片
      tempFilePaths.forEach((filePath, index) => {
        const imageIndex = imageList.value.length - tempFilePaths.length + index
        uploadImage(filePath, imageIndex)
      })

      emitChange()
    }
  })
}

// 上传图片
const uploadImage = (filePath, index) => {
  const item = imageList.value[index]

  // 模拟上传进度
  let progress = 0
  const progressTimer = setInterval(() => {
    progress += Math.random() * 20
    if (progress >= 90) {
      clearInterval(progressTimer)
      progress = 90
    }
    item.progress = Math.floor(progress)
  }, 200)

  // 实际上传逻辑（使用uni.uploadFile）
  uni.uploadFile({
    url: props.action,
    filePath: filePath,
    name: 'file',
    formData: {
      quality: props.quality
    },
    success: (uploadRes) => {
      clearInterval(progressTimer)

      if (uploadRes.statusCode === 200) {
        const data = JSON.parse(uploadRes.data)

        if (data.code === 200) {
          // 上传成功
          item.url = data.data.url
          item.status = 'success'
          item.progress = 100

          emit('uploadSuccess', data.data, index)
          emitChange()
        } else {
          // 上传失败
          item.status = 'fail'
          emit('uploadFail', data.message, index)
        }
      } else {
        item.status = 'fail'
        emit('uploadFail', '上传失败', index)
      }
    },
    fail: (err) => {
      clearInterval(progressTimer)
      item.status = 'fail'
      emit('uploadFail', err.errMsg, index)
    }
  })
}

// 重试上传
const retryUpload = (index) => {
  const item = imageList.value[index]

  item.status = 'uploading'
  item.progress = 0

  uploadImage(item.url, index)
}

// 删除图片
const deleteImage = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定删除这张图片吗？',
    success: (res) => {
      if (res.confirm) {
        imageList.value.splice(index, 1)
        emitChange()
      }
    }
  })
}

// 预览图片
const previewImage = (index) => {
  const urls = imageList.value
    .filter(item => item.status === 'success')
    .map(item => item.url)

  uni.previewImage({
    current: index,
    urls: urls
  })
}

// 触发change事件
const emitChange = () => {
  const urls = imageList.value
    .filter(item => item.status === 'success')
    .map(item => item.url)

  emit('update:modelValue', urls)
  emit('change', urls)
}
</script>

<style lang="scss" scoped>
.image-upload {
  .image-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;

    .image-item {
      position: relative;
      border-radius: 12rpx;
      overflow: hidden;
      background: #f5f5f5;

      .image {
        width: 100%;
        height: 100%;
      }

      .upload-progress {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16rpx;

        .progress-bar {
          width: 120rpx;
          height: 8rpx;
          background: rgba(255, 255, 255, 0.3);
          border-radius: 4rpx;
          overflow: hidden;

          .progress-value {
            height: 100%;
            background: #ffffff;
            border-radius: 4rpx;
            transition: width 0.3s;
          }
        }

        .progress-text {
          font-size: 24rpx;
          color: #ffffff;
        }
      }

      .upload-fail {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.6);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 8rpx;

        .fail-icon {
          font-size: 48rpx;
        }

        .fail-text {
          font-size: 22rpx;
          color: #ffffff;
        }
      }

      .delete-btn {
        position: absolute;
        top: 0;
        right: 0;
        width: 44rpx;
        height: 44rpx;
        background: rgba(0, 0, 0, 0.6);
        border-bottom-left-radius: 12rpx;
        display: flex;
        align-items: center;
        justify-content: center;

        .delete-icon {
          font-size: 40rpx;
          color: #ffffff;
          line-height: 1;
          transform: translateY(-4rpx);
        }
      }

      .mask {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(255, 255, 255, 0.3);
      }
    }

    .upload-btn {
      border-radius: 12rpx;
      border: 2rpx dashed #dddddd;
      background: #fafafa;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8rpx;

      .upload-icon {
        font-size: 64rpx;
        color: #999999;
        line-height: 1;
      }

      .upload-text {
        font-size: 24rpx;
        color: #999999;
      }

      &:active {
        background: #f0f0f0;
      }
    }
  }

  .upload-tip {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-top: 16rpx;
    padding: 0 8rpx;

    .tip-icon {
      font-size: 28rpx;
    }

    .tip-text {
      font-size: 24rpx;
      color: #999999;
    }
  }
}
</style>
