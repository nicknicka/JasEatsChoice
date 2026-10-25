<template>
  <view class="share-poster">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <button class="nav-back" @click="goBack" aria-label="返回上一页">
        <text class="icon">‹</text>
      </button>
      <view class="nav-title">生成海报</view>
      <button class="nav-action" @click="savePoster" aria-label="保存海报">
        <text class="action-text">保存</text>
      </button>
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
        <button class="style-item" v-for="(style, index) in styles" :key="index" :class="{ active: currentStyle === index }" @click="selectStyle(index)" :aria-label="`选择${style.name}样式`">
          <view class="style-preview" :style="{ background: style.background }">
            <text class="style-icon">{{ style.label }}</text>
          </view>
          <text class="style-name">{{ style.name }}</text>
        </button>
      </scroll-view>
    </view>

    <!-- 海报选项 -->
    <view class="poster-options">
      <button class="option-item" @click="toggleOption('showQrCode')" aria-label="切换二维码显示">
        <view class="option-left">
          <text class="option-icon">码</text>
          <text class="option-label">显示二维码</text>
        </view>
        <view class="option-right">
          <switch :checked="posterOptions.showQrCode" @change.stop="handleOptionChange('showQrCode', $event)" color="#ff6b6b" />
        </view>
      </button>
      <button class="option-item" @click="toggleOption('showPrice')" aria-label="切换价格显示">
        <view class="option-left">
          <text class="option-icon">价</text>
          <text class="option-label">显示价格</text>
        </view>
        <view class="option-right">
          <switch :checked="posterOptions.showPrice" @change.stop="handleOptionChange('showPrice', $event)" color="#ff6b6b" />
        </view>
      </button>
      <button class="option-item" @click="toggleOption('showDesc')" aria-label="切换描述显示">
        <view class="option-left">
          <text class="option-icon">介</text>
          <text class="option-label">显示描述</text>
        </view>
        <view class="option-right">
          <switch :checked="posterOptions.showDesc" @change.stop="handleOptionChange('showDesc', $event)" color="#ff6b6b" />
        </view>
      </button>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="btn btn-primary" @click="savePoster">
        <text class="icon">保存</text>
        <text>保存海报</text>
      </button>
      <button class="btn btn-outline" @click="sharePoster">
        <text class="icon">分享</text>
        <text>分享海报</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const palette = [
  { name: '暖食红', label: '主', colors: ['#FF6B35', '#E55A2B'] },
  { name: '杏仁橙', label: '橙', colors: ['#FF8B5D', '#FF6B35'] },
  { name: '麦穗金', label: '金', colors: ['#F4B740', '#E0911A'] },
  { name: '炭焙棕', label: '棕', colors: ['#6A4A3C', '#3E2B22'] }
]

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
  ...palette.map((item) => ({
    ...item,
    background: `linear-gradient(135deg, ${item.colors[0]} 0%, ${item.colors[1]} 100%)`
  }))
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

const handleOptionChange = (option, event) => {
  posterOptions.value[option] = Boolean(event?.detail?.value)
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
  gradient.addColorStop(0, style.colors[0])
  gradient.addColorStop(1, style.colors[1])
  ctx.fillStyle = gradient
  ctx.fillRect(0, 0, width, height)

  // 绘制白色卡片背景
  const cardMargin = 20
  const cardTop = 80
  const cardHeight = 320
  ctx.fillStyle = '#fffaf6'
  ctx.setShadow(0, 4, 12, 'rgba(0, 0, 0, 0.1)')
  ctx.fillRect(cardMargin, cardTop, width - cardMargin * 2, cardHeight)

  // 绘制商品图片（占位矩形）
  ctx.setShadow(0, 0, 0, 'transparent')
  ctx.fillStyle = '#f7ece4'
  ctx.fillRect(cardMargin + 10, cardTop + 10, width - cardMargin * 2 - 20, 180)

  // 绘制商品图片提示文字
  ctx.fillStyle = '#8b786d'
  ctx.font = '14px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('商品图片', width / 2, cardTop + 100)

  // 绘制标题
  ctx.fillStyle = '#2f221c'
  ctx.font = 'bold 18px sans-serif'
  ctx.textAlign = 'left'
  ctx.fillText(posterData.value.title, cardMargin + 20, cardTop + 220)

  // 绘制描述（如果启用）
  if (posterOptions.value.showDesc) {
    ctx.fillStyle = '#6e5a4d'
    ctx.font = '12px sans-serif'
    ctx.fillText(posterData.value.desc, cardMargin + 20, cardTop + 250)
  }

  // 绘制价格（如果启用）
  if (posterOptions.value.showPrice) {
    ctx.fillStyle = style.colors[0]
    ctx.font = 'bold 24px sans-serif'
    ctx.fillText('¥' + posterData.value.price, cardMargin + 20, cardTop + 290)
  }

  // 绘制底部信息
  const bottomY = height - 80
  ctx.fillStyle = '#fffaf6'
  ctx.font = 'bold 16px sans-serif'
  ctx.textAlign = 'center'
  ctx.fillText('佳食宜选', width / 2, bottomY)

  ctx.fillStyle = '#fff3ea'
  ctx.font = '12px sans-serif'
  ctx.fillText('长按识别二维码，立即下单', width / 2, bottomY + 25)

  // 绘制二维码（如果启用）
  if (posterOptions.value.showQrCode) {
    const qrSize = 80
    const qrX = (width - qrSize) / 2
    const qrY = bottomY - 120
    ctx.fillStyle = '#fffaf6'
    ctx.fillRect(qrX, qrY, qrSize, qrSize)
    ctx.fillStyle = '#2f221c'
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
@import '@/styles/variables.scss';

.share-poster {
  min-height: 100vh;
  background: $bg-color-base;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.nav-bar {
  height: 88rpx;
  background: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  border-bottom: 1rpx solid $border-color-light;

  .nav-back {
    min-width: $touch-min-size;
    min-height: $touch-min-size;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: transparent;

    .icon {
      font-size: 48rpx;
      color: $text-color-primary;
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
      color: $primary-600;
    }
  }
}

.poster-container {
  margin: 24rpx 32rpx;
  background: $bg-color-white;
  border-radius: 16rpx;
  padding: 32rpx;
  display: flex;
  justify-content: center;
  align-items: center;

  .poster-canvas {
    border-radius: 12rpx;
    box-shadow: $box-shadow-md;
  }
}

.poster-styles {
  margin: 24rpx 32rpx;
  background: $bg-color-white;
  border-radius: 16rpx;
  padding: 32rpx;

  .style-header {
    margin-bottom: 24rpx;

    .title {
      font-size: 28rpx;
      font-weight: bold;
      color: $text-color-primary;
      display: block;
      margin-bottom: 8rpx;
    }

    .desc {
      font-size: 24rpx;
      color: $text-color-secondary;
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
      border: none;
      background: transparent;

      .style-preview {
        width: 100rpx;
        height: 100rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        border: 3rpx solid transparent;
        transition: transform 0.3s ease, border-color 0.3s ease;

        .style-icon {
          font-size: 48rpx;
        }
      }

      .style-name {
        font-size: 24rpx;
        color: $text-color-regular;
        text-align: center;
      }

      &.active .style-preview {
        border-color: $primary-500;
        transform: scale(1.05);
      }
    }
  }
}

.poster-options {
  margin: 24rpx 32rpx;
  background: $bg-color-white;
  border-radius: 16rpx;
  overflow: hidden;

  .option-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 32rpx;
    border-bottom: 1rpx solid #f0f0f0;
    border-left: none;
    border-right: none;
    border-top: none;
    background: $bg-color-white;
    width: 100%;
    text-align: left;

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
        color: $text-color-primary;
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
      background: linear-gradient(135deg, $primary-500 0%, $primary-700 100%);
      color: #ffffff;
    }

    &.btn-outline {
      background: $bg-color-white;
      color: $primary-600;
      border: 2rpx solid $primary-500;
    }

    &:active {
      opacity: 0.8;
    }
  }
}

button::after {
  border: none;
}
</style>
