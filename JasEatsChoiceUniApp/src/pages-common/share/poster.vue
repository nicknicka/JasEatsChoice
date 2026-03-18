<template>
  <view class="share-poster">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">生成海报</view>
      <view class="nav-action" @click="savePoster">
        <text class="action-text">保存</text>
      </view>
    </view>

    <!-- 海报预览 -->
    <view class="poster-container">
      <canvas class="poster-canvas" canvas-id="posterCanvas" :style="{ width: posterWidth + 'px', height: posterHeight + 'px' }"></canvas>
    </view>

    <!-- 海报样式选择 -->
    <view class="poster-styles">
      <view class="style-header">
        <text class="title">选择样式</text>
        <text class="desc">选择你喜欢的海报样式</text>
      </view>
      <scroll-view class="style-list" scroll-x>
        <view class="style-item" v-for="(style, index) in styles" :key="index" :class="{ active: currentStyle === index }" @click="selectStyle(index)">
          <view class="style-preview" :style="{ background: style.background }">
            <text class="style-icon">{{ style.icon }}</text>
          </view>
          <text class="style-name">{{ style.name }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 海报选项 -->
    <view class="poster-options">
      <view class="option-item" @click="toggleOption('showQrCode')">
        <view class="option-left">
          <text class="option-icon">📱</text>
          <text class="option-label">显示二维码</text>
        </view>
        <view class="option-right">
          <switch :checked="posterOptions.showQrCode" @change="toggleOption('showQrCode')" color="#ff6b6b" />
        </view>
      </view>
      <view class="option-item" @click="toggleOption('showPrice')">
        <view class="option-left">
          <text class="option-icon">💰</text>
          <text class="option-label">显示价格</text>
        </view>
        <view class="option-right">
          <switch :checked="posterOptions.showPrice" @change="toggleOption('showPrice')" color="#ff6b6b" />
        </view>
      </view>
      <view class="option-item" @click="toggleOption('showDesc')">
        <view class="option-left">
          <text class="option-icon">📝</text>
          <text class="option-label">显示描述</text>
        </view>
        <view class="option-right">
          <switch :checked="posterOptions.showDesc" @change="toggleOption('showDesc')" color="#ff6b6b" />
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="btn btn-primary" @click="savePoster">
        <text class="icon">💾</text>
        <text>保存海报</text>
      </button>
      <button class="btn btn-outline" @click="sharePoster">
        <text class="icon">📤</text>
        <text>分享海报</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 海报尺寸
const posterWidth = ref(375)
const posterHeight = ref(600)

// 当前选择的样式
const currentStyle = ref(0)

// 海报选项
const posterOptions = ref({
  showQrCode: true,
  showPrice: true,
  showDesc: true
})

// 海报样式列表
const styles = ref([
  { name: '经典红', icon: '🔴', background: 'linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%)' },
  { name: '活力橙', icon: '🟠', background: 'linear-gradient(135deg, #ff9800 0%, #ff5722 100%)' },
  { name: '清新绿', icon: '🟢', background: 'linear-gradient(135deg, #52c41a 0%, #73d13d 100%)' },
  { name: '商务蓝', icon: '🔵', background: 'linear-gradient(135deg, #1677ff 0%, #0958d9 100%)' },
  { name: '梦幻紫', icon: '🟣', background: 'linear-gradient(135deg, #722ed1 0%, #531dab 100%)' },
  { name: '简约灰', icon: '⚪', background: 'linear-gradient(135deg, #595959 0%, #262626 100%)' }
])

// 海报数据
const posterData = ref({
  title: '美味餐厅',
  image: 'https://via.placeholder.com/400x300',
  price: '29.90',
  desc: '招牌菜品，限时优惠！',
  qrCode: 'https://example.com/qrcode'
})

// 组件挂载
onMounted(() => {
  drawPoster()
})

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 选择样式
const selectStyle = (index) => {
  currentStyle.value = index
  drawPoster()
}

// 切换选项
const toggleOption = (option) => {
  posterOptions.value[option] = !posterOptions.value[option]
  drawPoster()
}

// 绘制海报
const drawPoster = () => {
  const ctx = uni.createCanvasContext('posterCanvas')
  const width = posterWidth.value
  const height = posterHeight.value
  const style = styles.value[currentStyle.value]

  // 清空画布
  ctx.clearRect(0, 0, width, height)

  // 绘制背景
  const gradient = ctx.createLinearGradient(0, 0, width, height)
  if (currentStyle.value === 0) {
    gradient.addColorStop(0, '#ff6b6b')
    gradient.addColorStop(1, '#ee5a6f')
  } else if (currentStyle.value === 1) {
    gradient.addColorStop(0, '#ff9800')
    gradient.addColorStop(1, '#ff5722')
  } else if (currentStyle.value === 2) {
    gradient.addColorStop(0, '#52c41a')
    gradient.addColorStop(1, '#73d13d')
  } else if (currentStyle.value === 3) {
    gradient.addColorStop(0, '#1677ff')
    gradient.addColorStop(1, '#0958d9')
  } else if (currentStyle.value === 4) {
    gradient.addColorStop(0, '#722ed1')
    gradient.addColorStop(1, '#531dab')
  } else {
    gradient.addColorStop(0, '#595959')
    gradient.addColorStop(1, '#262626')
  }
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, width, height)

  // 绘制白色卡片背景
  const cardMargin = 20
  const cardTop = 80
  const cardHeight = 320
  ctx.fillStyle = '#ffffff'
  ctx.setShadow(0, 4, 12, 'rgba(0, 0, 0, 0.1)')
  ctx.fillRect(cardMargin, cardTop, width - cardMargin * 2, cardHeight)

  // 绘制商品图片（占位矩形）
  ctx.setShadow(0, 0, 0, 'transparent')
  ctx.fillStyle = '#f0f0f0'
  ctx.fillRect(cardMargin + 10, cardTop + 10, width - cardMargin * 2 - 20, 180)

  // 绘制商品图片提示文字
  ctx.fillStyle = '#999999'
  ctx.font = '14px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('商品图片', width / 2, cardTop + 100)

  // 绘制标题
  ctx.fillStyle = '#333333'
  ctx.font = 'bold 18px sans-serif'
  ctx.textAlign = 'left'
  ctx.fillText(posterData.value.title, cardMargin + 20, cardTop + 220)

  // 绘制描述（如果启用）
  if (posterOptions.value.showDesc) {
    ctx.fillStyle = '#666666'
    ctx.font = '12px sans-serif'
    ctx.fillText(posterData.value.desc, cardMargin + 20, cardTop + 250)
  }

  // 绘制价格（如果启用）
  if (posterOptions.value.showPrice) {
    ctx.fillStyle = '#ff6b6b'
    ctx.font = 'bold 24px sans-serif'
    ctx.fillText('¥' + posterData.value.price, cardMargin + 20, cardTop + 290)
  }

  // 绘制底部信息
  const bottomY = height - 80
  ctx.fillStyle = '#ffffff'
  ctx.font = 'bold 16px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('佳食宜选', width / 2, bottomY)

  ctx.fillStyle = '#ffffff'
  ctx.font = '12px sans-serif'
  ctx.fillText('长按识别二维码，立即下单', width / 2, bottomY + 25)

  // 绘制二维码（如果启用）
  if (posterOptions.showQrCode) {
    const qrSize = 80
    const qrX = (width - qrSize) / 2
    const qrY = bottomY - 120
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(qrX, qrY, qrSize, qrSize)
    ctx.fillStyle = '#333333'
    ctx.font = '10px sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText('二维码', qrX + qrSize / 2, qrY + qrSize / 2)
  }

  ctx.draw()
}

// 保存海报
const savePoster = () => {
  uni.canvasToTempFilePath({
    canvasId: 'posterCanvas',
    success: (res) => {
      uni.saveImageToPhotosAlbum({
        filePath: res.tempFilePath,
        success: () => {
          uni.showToast({
            title: '已保存到相册',
            icon: 'success'
          })
        },
        fail: () => {
          uni.showToast({
            title: '保存失败',
            icon: 'error'
          })
        }
      })
    },
    fail: () => {
      uni.showToast({
        title: '生成失败',
        icon: 'error'
      })
    }
  })
}

// 分享海报
const sharePoster = () => {
  uni.canvasToTempFilePath({
    canvasId: 'posterCanvas',
    success: (res) => {
      uni.share({
        provider: 'weixin',
        type: 0,
        title: '佳食宜选 - 美食外卖',
        summary: '发现一家超好吃的餐厅！',
        imageUrl: res.tempFilePath,
        href: 'https://example.com/share/123',
        success: () => {
          uni.showToast({
            title: '分享成功',
            icon: 'success'
          })
        },
        fail: () => {
          uni.showToast({
            title: '分享失败',
            icon: 'error'
          })
        }
      })
    }
  })
}
</script>

<style lang="scss" scoped>
.share-poster {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-bar {
  height: 88rpx;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .nav-back {
    width: 60rpx;
    height: 60rpx;
    display: flex;
    align-items: center;
    justify-content: center;

    .icon {
      font-size: 48rpx;
      color: #333333;
    }
  }

  .nav-title {
    font-size: 32rpx;
    font-weight: bold;
    color: #333333;
  }

  .nav-action {
    .action-text {
      font-size: 28rpx;
      color: #ff6b6b;
    }
  }
}

.poster-container {
  margin: 24rpx 32rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  display: flex;
  justify-content: center;
  align-items: center;

  .poster-canvas {
    border-radius: 12rpx;
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  }
}

.poster-styles {
  margin: 24rpx 32rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;

  .style-header {
    margin-bottom: 24rpx;

    .title {
      font-size: 28rpx;
      font-weight: bold;
      color: #333333;
      display: block;
      margin-bottom: 8rpx;
    }

    .desc {
      font-size: 24rpx;
      color: #999999;
    }
  }

  .style-list {
    white-space: nowrap;
    display: flex;
    gap: 24rpx;

    .style-item {
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      gap: 12rpx;
      width: 120rpx;

      .style-preview {
        width: 100rpx;
        height: 100rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 3rpx solid transparent;
        transition: all 0.3s;

        .style-icon {
          font-size: 48rpx;
        }
      }

      .style-name {
        font-size: 24rpx;
        color: #666666;
        text-align: center;
      }

      &.active .style-preview {
        border-color: #ff6b6b;
        transform: scale(1.05);
      }
    }
  }
}

.poster-options {
  margin: 24rpx 32rpx;
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;

  .option-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 32rpx;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .option-left {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .option-icon {
        font-size: 36rpx;
      }

      .option-label {
        font-size: 28rpx;
        color: #333333;
      }
    }
  }
}

.action-buttons {
  margin: 24rpx 32rpx;
  display: flex;
  gap: 24rpx;

  .btn {
    flex: 1;
    height: 88rpx;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12rpx;
    font-size: 28rpx;
    font-weight: 500;
    border: none;

    .icon {
      font-size: 32rpx;
    }

    &.btn-primary {
      background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
      color: #ffffff;
    }

    &.btn-outline {
      background: #ffffff;
      color: #ff6b6b;
      border: 2rpx solid #ff6b6b;
    }

    &:active {
      opacity: 0.8;
    }
  }
}
</style>
