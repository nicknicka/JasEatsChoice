<template>
  <view class="share-group-order-container">
    <!-- 顶部卡片 -->
    <view class="share-card">
      <view class="card-header">
        <text class="title">邀请好友加入群订单</text>
        <text class="subtitle">分享订单码或二维码，让好友一起点餐</text>
      </view>

      <!-- 订单码 -->
      <view class="order-code-section">
        <text class="code-label">6位订单码</text>
        <view class="order-code">
          <text class="code-text">{{ orderCode }}</text>
          <view class="copy-btn" @tap="copyOrderCode">
            <uni-icons type="copy" size="18" color="#FF6B35"></uni-icons>
            <text>复制</text>
          </view>
        </view>
      </view>

      <!-- 二维码 - GROUP-001 -->
      <view class="qrcode-section">
        <text class="qrcode-label">邀请二维码</text>
        <view class="qrcode-wrapper">
          <image
            v-if="qrcodeUrl"
            class="qrcode-image"
            :src="qrcodeUrl"
            mode="aspectFit"
            show-menu-by-longpress
          />
          <view v-else class="qrcode-loading">
            <uni-load-more status="loading" />
            <text>加载中...</text>
          </view>
        </view>
        <text class="qrcode-tip">长按保存二维码图片</text>
      </view>
    </view>

    <!-- 群订单信息 -->
    <view class="order-info" v-if="orderInfo.id">
      <view class="info-item">
        <text class="label">群订单名称</text>
        <text class="value">{{ orderInfo.name }}</text>
      </view>
      <view class="info-item">
        <text class="label">商家</text>
        <text class="value">{{ orderInfo.merchantName }}</text>
      </view>
      <view class="info-item">
        <text class="label">已加入/总人数</text>
        <text class="value">{{ orderInfo.currentCount }}/{{ orderInfo.maxParticipants }}人</text>
      </view>
      <view class="info-item">
        <text class="label">截止时间</text>
        <text class="value">{{ orderInfo.deadline }}</text>
      </view>
    </view>

    <!-- 分享按钮 -->
    <view class="share-actions">
      <button class="share-btn wechat" @tap="shareToWechat">
        <uni-icons type="weixin" size="20" color="#fff"></uni-icons>
        <text>分享给微信好友</text>
      </button>
      <button class="share-btn poster" @tap="generatePoster">
        <uni-icons type="image" size="20" color="#fff"></uni-icons>
        <text>生成分享海报</text>
      </button>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-actions">
      <button class="action-btn secondary" @tap="viewOrder">查看群订单</button>
      <button class="action-btn primary" @tap="inviteMembers">邀请好友</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { groupOrderApi } from '@/api/modules/group-order-api.js'

const orderId = ref('')
const orderCode = ref('')
const qrcodeUrl = ref('')

// 群订单信息
const orderInfo = ref({
  id: '',
  name: '',
  merchantName: '',
  currentCount: 0,
  maxParticipants: 0,
  deadline: ''
})

onMounted(async () => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  orderId.value = options.id || ''
  orderCode.value = options.code || ''

  // 加载群订单详情
  if (orderId.value) {
    await loadOrderDetail()
    // GROUP-001: 生成邀请二维码
    await generateQRCode()
  }
})

/**
 * 加载群订单详情
 */
const loadOrderDetail = async () => {
  try {
    const res = await groupOrderApi.getDetail(orderId.value)

    if (res.code === 200 && res.data) {
      orderInfo.value = {
        id: res.data.id,
        name: res.data.name,
        merchantName: res.data.merchantName || '',
        currentCount: res.data.currentCount || 0,
        maxParticipants: res.data.maxParticipants || 0,
        deadline: res.data.deadline || ''
      }

      // 如果API返回了订单码，使用API返回的
      if (res.data.orderCode) {
        orderCode.value = res.data.orderCode
      }
    }
  } catch (error) {
    console.error('加载群订单详情失败:', error)
  }
}

/**
 * GROUP-001: 生成邀请二维码
 */
const generateQRCode = async () => {
  try {
    // 调用API生成二维码
    const res = await groupOrderApi.getQRCode(orderId.value, {
      width: 300
    })

    if (res.code === 200 && res.data) {
      // 如果返回的是base64图片数据
      if (typeof res.data === 'string' && res.data.startsWith('data:image')) {
        qrcodeUrl.value = res.data
      }
      // 如果返回的是图片URL
      else if (res.data.url) {
        qrcodeUrl.value = res.data.url
      }
      // 如果返回的是二维码内容，使用前端生成
      else if (res.data.qrcodeContent) {
        // 这里需要引入二维码生成库，如uQRCode
        // 临时使用占位图
        qrcodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(res.data.qrcodeContent)}`
      }
    }
  } catch (error) {
    console.error('生成二维码失败:', error)

    // 失败时使用前端生成二维码
    // 临时使用在线API生成（生产环境应该使用后端API或本地生成）
    const qrContent = JSON.stringify({
      type: 'group_order',
      orderId: orderId.value,
      orderCode: orderCode.value
    })
    qrcodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(qrContent)}`
  }
}

/**
 * 复制订单码
 */
const copyOrderCode = () => {
  uni.setClipboardData({
    data: orderCode.value,
    success: () => {
      uni.showToast({
        title: '订单码已复制',
        icon: 'success'
      })
    }
  })
}

/**
 * 分享到微信
 */
const shareToWechat = () => {
  // 小程序分享
  uni.share({
    provider: 'weixin',
    type: 5, // 小程序
    title: `加入我的群订单「${orderInfo.value.name}」`,
    imageUrl: qrcodeUrl.value,
    success: () => {
      uni.showToast({
        title: '分享成功',
        icon: 'success'
      })
    },
    fail: () => {
      // 小程序环境下使用分享API
      uni.showShareMenu({
        withShareTicket: true
      })
    }
  })
}

/**
 * 生成分享海报
 */
const generatePoster = () => {
  uni.showToast({
    title: '海报生成功能开发中',
    icon: 'none'
  })
  // TODO: 使用canvas绘制海报
  // 包含：群订单信息、二维码、商家Logo等
}

/**
 * 查看群订单
 */
const viewOrder = () => {
  uni.navigateTo({
    url: `/pages-user/group-order/detail?id=${orderId.value}`
  })
}

/**
 * 邀请好友
 */
const inviteMembers = () => {
  // 显示分享菜单
  uni.showActionSheet({
    itemList: ['分享给微信好友', '生成分享海报', '复制订单码'],
    success: (res) => {
      switch (res.tapIndex) {
        case 0:
          shareToWechat()
          break
        case 1:
          generatePoster()
          break
        case 2:
          copyOrderCode()
          break
      }
    }
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.share-group-order-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #FF6B35 0%, #FF8C5A 100%);
  padding: 30rpx;
  padding-bottom: 200rpx;
}

/* 分享卡片 */
.share-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
}

.card-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.title {
  display: block;
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 10rpx;
}

.subtitle {
  display: block;
  font-size: 26rpx;
  color: #999;
}

/* 订单码 */
.order-code-section {
  margin-bottom: 40rpx;
}

.code-label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 15rpx;
  text-align: center;
}

.order-code {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  border-radius: 12rpx;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.code-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  letter-spacing: 5rpx;
}

.copy-btn {
  background: rgba(255, 255, 255, 0.3);
  padding: 10rpx 20rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #fff;
  font-size: 24rpx;
}

/* 二维码 */
.qrcode-section {
  text-align: center;
}

.qrcode-label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.qrcode-wrapper {
  width: 400rpx;
  height: 400rpx;
  margin: 0 auto 20rpx;
  border-radius: 12rpx;
  overflow: hidden;
  background: #F5F5F5;
  @include flex-center;
}

.qrcode-image {
  width: 100%;
  height: 100%;
}

.qrcode-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15rpx;
  color: #999;
  font-size: 26rpx;
}

.qrcode-tip {
  display: block;
  font-size: 24rpx;
  color: #999;
}

/* 订单信息 */
.order-info {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.info-item .label {
  font-size: 28rpx;
  color: #666;
}

.info-item .value {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 分享按钮 */
.share-actions {
  display: flex;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.share-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: 28rpx;
  color: #fff;
  border: none;

  &.wechat {
    background: #07C160;
  }

  &.poster {
    background: #FF6B35;
  }
}

/* 底部操作 */
.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  background: #fff;
  display: flex;
  gap: 20rpx;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 28rpx;
  border: none;

  &.secondary {
    background: #F5F5F5;
    color: #666;
  }

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
