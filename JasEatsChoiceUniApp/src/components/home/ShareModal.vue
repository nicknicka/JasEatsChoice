<template>
  <uni-popup ref="sharePopup" type="bottom" :safe-area="false">
    <view class="share-modal">
      <view class="share-title">分享给好友</view>

      <view class="share-channels">
        <view
          class="channel-item"
          @click="handleShare('wechat')"
        >
          <view class="channel-icon wechat">
            <text class="icon-emoji">💬</text>
          </view>
          <text class="channel-name">微信好友</text>
        </view>

        <view
          class="channel-item"
          @click="handleShare('moments')"
        >
          <view class="channel-icon moments">
            <text class="icon-emoji">🕒</text>
          </view>
          <text class="channel-name">朋友圈</text>
        </view>

        <view
          class="channel-item"
          @click="handleShare('poster')"
        >
          <view class="channel-icon poster">
            <text class="icon-emoji">🖼️</text>
          </view>
          <text class="channel-name">生成海报</text>
        </view>

        <view
          class="channel-item"
          @click="handleShare('link')"
        >
          <view class="channel-icon link">
            <text class="icon-emoji">🔗</text>
          </view>
          <text class="channel-name">复制链接</text>
        </view>
      </view>

      <view class="share-cancel" @click="close">取消</view>
    </view>
  </uni-popup>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  dish: {
    type: Object,
    default: () => ({})
  },
  merchant: {
    type: Object,
    default: () => ({})
  }
})

const sharePopup = ref(null)

/**
 * 显示分享弹窗
 */
const show = () => {
  sharePopup.value?.open()
}

/**
 * 关闭分享弹窗
 */
const close = () => {
  sharePopup.value?.close()
}

/**
 * 处理分享
 */
const handleShare = async (channel) => {
  try {
    switch (channel) {
      case 'wechat':
        await shareToWechat()
        break

      case 'moments':
        await shareToMoments()
        break

      case 'poster':
        await generatePoster()
        break

      case 'link':
        await copyLink()
        break
    }
  } catch (error) {
    console.error('分享失败:', error)
    uni.showToast({
      title: '分享失败',
      icon: 'none'
    })
  }

  close()
}

/**
 * 分享到微信好友
 */
const shareToWechat = () => {
  return new Promise((resolve, reject) => {
    // 检查是否支持分享
    if (!uni.share) {
      // 不支持分享，降级为复制链接
      copyLink()
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
      resolve()
      return
    }

    const shareData = {
      provider: 'weixin',
      type: 0, // 图文分享
      title: props.dish.name || props.merchant.name || '佳食宜选',
      summary: props.dish.description || props.merchant.description || '发现美食好去处',
      href: getShareUrl(),
      imageUrl: props.dish.image || props.merchant.logo || '',
      success: () => {
        uni.showToast({ title: '分享成功', icon: 'success' })
        resolve()
      },
      fail: (err) => {
        console.error('分享失败:', err)
        reject(err)
      }
    }

    uni.share(shareData)
  })
}

/**
 * 分享到朋友圈
 */
const shareToMoments = () => {
  return new Promise((resolve, reject) => {
    if (!uni.share) {
      uni.showToast({
        title: '当前环境不支持',
        icon: 'none'
      })
      reject(new Error('不支持分享'))
      return
    }

    uni.share({
      provider: 'weixin',
      type: 1, // 图片分享
      title: props.dish.name || props.merchant.name || '佳食宜选',
      imageUrl: props.dish.image || props.merchant.logo || '',
      success: () => {
        uni.showToast({ title: '分享成功', icon: 'success' })
        resolve()
      },
      fail: reject
    })
  })
}

/**
 * 生成分享海报
 */
const generatePoster = async () => {
  try {
    uni.showLoading({ title: '生成中...' })

    // 调用后端API生成海报
    const res = await uni.request({
      url: '/api/v1/share/poster',
      method: 'POST',
      data: {
        type: props.dish.id ? 'dish' : 'merchant',
        id: props.dish.id || props.merchant.id
      }
    })

    uni.hideLoading()

    if (res.data && res.data.data && res.data.data.posterUrl) {
      const posterUrl = res.data.data.posterUrl

      // 预览海报
      uni.previewImage({
        urls: [posterUrl],
        current: 0
      })

      // 提示保存
      uni.showModal({
        title: '保存海报',
        content: '是否保存海报到相册？',
        success: (modalRes) => {
          if (modalRes.confirm) {
            // 保存到相册
            uni.saveImageToPhotosAlbum({
              filePath: posterUrl,
              success: () => {
                uni.showToast({
                  title: '已保存到相册',
                  icon: 'success'
                })
              },
              fail: () => {
                uni.showToast({
                  title: '保存失败',
                  icon: 'none'
                })
              }
            })
          }
        }
      })
    } else {
      throw new Error('生成海报失败')
    }
  } catch (error) {
    uni.hideLoading()
    throw error
  }
}

/**
 * 复制链接
 */
const copyLink = () => {
  const link = getShareUrl()

  uni.setClipboardData({
    data: link,
    success: () => {
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    },
    fail: () => {
      uni.showToast({
        title: '复制失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 获取分享链接
 */
const getShareUrl = () => {
  const baseUrl = 'https://your-domain.com'

  if (props.dish.id) {
    return `${baseUrl}/dish/${props.dish.id}`
  } else if (props.merchant.id) {
    return `${baseUrl}/merchant/${props.merchant.id}`
  }

  return baseUrl
}

// 暴露方法
defineExpose({
  show,
  close
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.share-modal {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg $border-radius-lg 0 0;
  padding: $spacing-xl $spacing-md;
  padding-bottom: calc($spacing-xl + env(safe-area-inset-bottom));
}

.share-title {
  text-align: center;
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  margin-bottom: $spacing-xl;
  color: $text-color-primary;
}

.share-channels {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-xl;
  margin-bottom: $spacing-xl;
}

.channel-item {
  @include flex-center-column;
  gap: $spacing-sm;
}

.channel-icon {
  width: 120rpx;
  height: 120rpx;
  @include flex-center;
  border-radius: $border-radius-lg;
  font-size: 60rpx;
  transition: all 0.2s ease;

  &.wechat {
    background-color: rgba(7, 193, 96, 0.1);
  }

  &.moments {
    background-color: rgba(7, 193, 96, 0.1);
  }

  &.poster {
    background-color: rgba(255, 107, 53, 0.1);
  }

  &.link {
    background-color: rgba(102, 126, 234, 0.1);
  }

  .icon-emoji {
    font-size: 60rpx;
  }
}

.channel-item:active .channel-icon {
  transform: scale(0.9);
}

.channel-name {
  font-size: $font-size-sm;
  color: $text-color-regular;
}

.share-cancel {
  height: 88rpx;
  line-height: 88rpx;
  text-align: center;
  background-color: $bg-color-base;
  border-radius: $border-radius-base;
  font-size: $font-size-base;
  color: $text-color-primary;
  transition: all 0.2s ease;

  &:active {
    background-color: darken($bg-color-base, 5%);
  }
}
</style>
