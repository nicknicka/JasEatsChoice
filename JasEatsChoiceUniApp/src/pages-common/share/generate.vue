<template>
  <view class="share-generate">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="icon">‹</text>
      </view>
      <view class="nav-title">分享</view>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 分享预览 -->
    <view class="share-preview">
      <view class="preview-header">
        <text class="title">{{ shareTitle }}</text>
      </view>
      <view class="preview-content">
        <image class="preview-image" :src="shareImage" mode="aspectFill"></image>
        <view class="preview-info">
          <text class="desc">{{ shareDesc }}</text>
        </view>
      </view>
      <view class="preview-footer">
        <text class="app-name">佳食宜选</text>
        <text class="slogan">美食外卖，一键即达</text>
      </view>
    </view>

    <!-- 分享渠道 -->
    <view class="share-channels">
      <view class="channel-list">
        <view class="channel-item" v-for="(channel, index) in channels" :key="index" @click="shareToChannel(channel)">
          <view class="channel-icon" :class="channel.type">
            <text class="icon">{{ channel.icon }}</text>
          </view>
          <text class="channel-name">{{ channel.name }}</text>
        </view>
      </view>
    </view>

    <!-- 其他操作 -->
    <view class="other-actions">
      <view class="action-item" @click="saveToAlbum">
        <text class="icon">⬇</text>
        <text class="label">保存到相册</text>
      </view>
      <view class="action-item" @click="copyLink">
        <text class="icon">🔗</text>
        <text class="label">复制链接</text>
      </view>
      <view class="action-item" @click="generatePoster">
        <text class="icon">🖼</text>
        <text class="label">生成海报</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

// 分享内容
const shareTitle = ref('发现一家超好吃的餐厅！')
const shareDesc = ref('美味餐厅，这个菜品太好吃了，强烈推荐给大家！')
const shareImage = ref('https://via.placeholder.com/400x300')

// 分享渠道列表
const channels = ref([
  { name: '微信', icon: '💬', type: 'wechat' },
  { name: '朋友圈', icon: '⭕', type: 'moments' },
  { name: 'QQ', icon: '🐧', type: 'qq' },
  { name: '微博', icon: '🔶', type: 'weibo' },
  { name: '支付宝', icon: '💙', type: 'alipay' },
  { name: '短信', icon: '✉️', type: 'sms' }
])

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 分享到指定渠道
const shareToChannel = (channel) => {
  uni.showLoading({ title: '分享中...' })

  // 模拟分享处理
  setTimeout(() => {
    uni.hideLoading()

    if (channel.type === 'wechat' || channel.type === 'moments') {
      // 微信分享需要调用微信SDK
      uni.share({
        provider: 'weixin',
        type: channel.type === 'moments' ? 2 : 0,
        title: shareTitle.value,
        summary: shareDesc.value,
        imageUrl: shareImage.value,
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
    } else {
      // 其他渠道分享
      uni.showToast({
        title: `已${channel.name}分享`,
        icon: 'success'
      })
    }
  }, 1000)
}

// 保存到相册
const saveToAlbum = () => {
  uni.showLoading({ title: '保存中...' })

  // 下载图片到本地
  uni.downloadFile({
    url: shareImage.value,
    success: (res) => {
      if (res.statusCode === 200) {
        // 保存图片到相册
        uni.saveImageToPhotosAlbum({
          filePath: res.tempFilePath,
          success: () => {
            uni.hideLoading()
            uni.showToast({
              title: '已保存到相册',
              icon: 'success'
            })
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

// 复制链接
const copyLink = () => {
  const shareLink = 'https://example.com/share/123'

  uni.setClipboardData({
    data: shareLink,
    success: () => {
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    }
  })
}

// 生成海报
const generatePoster = () => {
  uni.navigateTo({
    url: '/share/poster'
  })
}
</script>

<style lang="scss" scoped>
.share-generate {
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

  .nav-placeholder {
    width: 60rpx;
  }
}

.share-preview {
  margin: 24rpx 32rpx;
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;

  .preview-header {
    padding: 24rpx 32rpx;
    border-bottom: 1rpx solid #f0f0f0;

    .title {
      font-size: 32rpx;
      font-weight: bold;
      color: #333333;
    }
  }

  .preview-content {
    padding: 32rpx;

    .preview-image {
      width: 100%;
      height: 400rpx;
      border-radius: 12rpx;
      margin-bottom: 24rpx;
    }

    .preview-info {
      .desc {
        font-size: 28rpx;
        color: #666666;
        line-height: 1.6;
      }
    }
  }

  .preview-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 32rpx;
    background: #f9f9f9;
    border-top: 1rpx solid #f0f0f0;

    .app-name {
      font-size: 28rpx;
      font-weight: bold;
      color: #333333;
    }

    .slogan {
      font-size: 24rpx;
      color: #999999;
    }
  }
}

.share-channels {
  margin: 24rpx 32rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;

  .channel-list {
    display: flex;
    flex-wrap: wrap;
    gap: 32rpx;

    .channel-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 12rpx;
      width: calc((100% - 96rpx) / 4);

      .channel-icon {
        width: 96rpx;
        height: 96rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;

        &.wechat {
          background: #07c160;
        }

        &.moments {
          background: #07c160;
        }

        &.qq {
          background: #1296db;
        }

        &.weibo {
          background: #ff6b6b;
        }

        &.alipay {
          background: #1677ff;
        }

        &.sms {
          background: #ff9800;
        }

        .icon {
          font-size: 48rpx;
        }
      }

      .channel-name {
        font-size: 24rpx;
        color: #666666;
      }

      &:active {
        opacity: 0.7;
      }
    }
  }
}

.other-actions {
  margin: 24rpx 32rpx;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 32rpx;
  display: flex;
  justify-content: space-around;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12rpx;

    .icon {
      font-size: 48rpx;
    }

    .label {
      font-size: 24rpx;
      color: #666666;
    }

    &:active {
      opacity: 0.7;
    }
  }
}
</style>
