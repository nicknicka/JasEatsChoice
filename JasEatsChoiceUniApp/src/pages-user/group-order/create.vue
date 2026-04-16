<template>
  <view class="create-group-order-container">
    <!-- 商家信息 -->
    <view class="merchant-info" v-if="merchantInfo.id">
      <image class="merchant-avatar" :src="merchantInfo.avatar" mode="aspectFill"></image>
      <view class="merchant-details">
        <text class="merchant-name">{{ merchantInfo.name }}</text>
        <text class="merchant-address">{{ merchantInfo.address }}</text>
      </view>
    </view>

    <!-- 表单 -->
    <view class="form-section">
      <!-- 群订单名称 -->
      <view class="form-item">
        <text class="label">群订单名称</text>
        <input
          class="input"
          v-model="formData.name"
          placeholder="给群订单起个名字吧"
          maxlength="30"
        />
      </view>

      <!-- 最大人数 -->
      <view class="form-item">
        <text class="label">最大人数</text>
        <view class="counter">
          <view
            class="counter-btn"
            :class="{ disabled: formData.maxParticipants <= 2 }"
            @tap="changeParticipants(-1)"
          >
            <text>-</text>
          </view>
          <text class="counter-value">{{ formData.maxParticipants }}人</text>
          <view
            class="counter-btn"
            :class="{ disabled: formData.maxParticipants >= 20 }"
            @tap="changeParticipants(1)"
          >
            <text>+</text>
          </view>
        </view>
      </view>

      <!-- 截止时间 -->
      <view class="form-item">
        <text class="label">截止时间</text>
        <picker
          mode="time"
          :value="formData.deadline"
          @change="onDeadlineChange"
        >
          <view class="picker">
            <text>{{ formData.deadline || '请选择' }}</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
      </view>

      <!-- 配送地址 -->
      <view class="form-item">
        <text class="label">配送地址</text>
        <view class="address-picker" @tap="selectAddress">
          <text class="address-text" v-if="selectedAddress.address">{{ selectedAddress.address }}</text>
          <text class="placeholder" v-else>请选择配送地址</text>
          <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
        </view>
      </view>

      <!-- 备注 -->
      <view class="form-item">
        <text class="label">备注（选填）</text>
        <textarea
          class="textarea"
          v-model="formData.remark"
          placeholder="有什么特殊要求可以写在这里"
          maxlength="200"
        />
      </view>
    </view>

    <!-- 预览卡片 -->
    <view class="preview-section">
      <text class="section-title">预览</text>
      <view class="preview-card">
        <view class="card-header">
          <text class="card-title">{{ formData.name || '群订单' }}</text>
          <text class="card-code">订单码：生成后显示</text>
        </view>
        <view class="card-info">
          <view class="info-item">
            <text class="label">商家：</text>
            <text class="value">{{ merchantInfo.name }}</text>
          </view>
          <view class="info-item">
            <text class="label">人数：</text>
            <text class="value">{{ formData.maxParticipants }}人</text>
          </view>
          <view class="info-item">
            <text class="label">截止：</text>
            <text class="value">{{ formData.deadline }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 创建按钮 -->
    <view class="action-buttons">
      <button class="submit-btn" @tap="createGroupOrder">创建群订单</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { groupOrderApi } from '@/api/modules/group-order-api.js'

// 商家信息
const merchantInfo = ref({
  id: '',
  name: '',
  avatar: '',
  address: ''
})

const groupId = ref('')

// 表单数据
const formData = ref({
  name: '',
  maxParticipants: 5,
  deadline: '',
  deliveryAddress: '',
  remark: ''
})

// 选中的地址
const selectedAddress = ref({
  id: '',
  address: '',
  contact: '',
  phone: ''
})

onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options || {}

  // 如果有商家ID，加载商家信息
  if (options.merchantId) {
    loadMerchantInfo(options.merchantId)
  }
  if (options.groupId) {
    groupId.value = options.groupId
  }

  // 设置默认截止时间为当前时间+2小时
  const now = new Date()
  now.setHours(now.getHours() + 2)
  formData.value.deadline = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
})

onShow(() => {
  const selected = uni.getStorageSync('selectedAddress')
  if (selected) {
    selectedAddress.value = {
      id: selected.id || selected.addressId || '',
      address: selected.detailAddress || selected.detail || selected.address || '',
      contact: selected.receiverName || selected.name || '',
      phone: selected.receiverPhone || selected.phone || ''
    }
  }
})

/**
 * 加载商家信息
 */
const loadMerchantInfo = async (merchantId) => {
  try {
    // 这里假设有获取商家详情的API
    // const res = await merchantApi.getDetail(merchantId)
    // if (res.code === 200) {
    //   merchantInfo.value = res.data
    // }

    // 临时模拟数据
    merchantInfo.value = {
      id: merchantId,
      name: '测试商家',
      avatar: 'https://via.placeholder.com/100',
      address: '测试地址123号'
    }
  } catch (error) {
    console.error('加载商家信息失败:', error)
  }
}

/**
 * 修改人数
 */
const changeParticipants = (delta) => {
  const newValue = formData.value.maxParticipants + delta
  if (newValue >= 2 && newValue <= 20) {
    formData.value.maxParticipants = newValue
  }
}

/**
 * 截止时间变更
 */
const onDeadlineChange = (e) => {
  formData.value.deadline = e.detail.value
}

/**
 * 选择地址
 */
const selectAddress = () => {
  uni.navigateTo({
    url: '/address/select?mode=select'
  })
}

/**
 * 创建群订单 - GROUP-002
 */
const createGroupOrder = async () => {
  // 验证表单
  if (!formData.value.name) {
    uni.showToast({
      title: '请输入群订单名称',
      icon: 'none'
    })
    return
  }

  if (!formData.value.deadline) {
    uni.showToast({
      title: '请选择截止时间',
      icon: 'none'
    })
    return
  }

  if (!selectedAddress.value.address) {
    uni.showToast({
      title: '请选择配送地址',
      icon: 'none'
    })
    return
  }

  if (!groupId.value) {
    uni.showToast({
      title: '当前页面缺少群ID，无法创建拼单',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '创建中...',
      mask: true
    })

    const userId = uni.getStorageSync('userId') || ''

    // GROUP-002: 调用API创建群订单
    const res = await groupOrderApi.create({
      groupId: groupId.value,
      merchantId: merchantInfo.value.id,
      creatorId: userId,
      merchantName: merchantInfo.value.name,
      merchantAvatar: merchantInfo.value.avatar,
      name: formData.value.name,
      maxParticipants: formData.value.maxParticipants,
      deadline: formData.value.deadline,
      deliveryAddress: selectedAddress.value.address,
      addressId: selectedAddress.value.id,
      remark: formData.value.remark || ''
    })

    if (res.code === 200 && res.data) {
      const orderId = res.data.id || res.data.orderId
      const orderCode = res.data.orderCode || ''

      uni.hideLoading()

      // 显示创建成功提示
      uni.showModal({
        title: '创建成功',
        content: `群订单创建成功！\n\n订单码：${orderCode}\n\n您可以分享订单码或二维码邀请好友加入`,
        confirmText: '去分享',
        cancelText: '稍后',
        success: (modalRes) => {
          if (modalRes.confirm) {
            // 跳转到分享页面
            navigateToShare(orderId, orderCode)
          } else {
            // 返回上一页
            uni.navigateBack()
          }
        }
      })
    } else {
      throw new Error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建群订单失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '创建失败',
      icon: 'none'
    })
  }
}

/**
 * 跳转到分享页面
 */
const navigateToShare = (orderId, orderCode) => {
  uni.navigateTo({
    url: `/pages-user/group-order/share?id=${orderId}&code=${orderCode}`
  })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.create-group-order-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 商家信息 */
.merchant-info {
  background: #fff;
  padding: 30rpx;
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.merchant-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.merchant-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10rpx;
}

.merchant-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.merchant-address {
  font-size: 26rpx;
  color: #999;
}

/* 表单 */
.form-section {
  background: #fff;
  padding: 0 30rpx;
  margin-bottom: 20rpx;
}

.form-item {
  padding: 30rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 20rpx;
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

.textarea {
  width: 100%;
  min-height: 160rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

/* 计数器 */
.counter {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.counter-btn {
  width: 60rpx;
  height: 60rpx;
  background: #FF6B35;
  color: #fff;
  border-radius: 50%;
  @include flex-center;
  font-size: 32rpx;

  &.disabled {
    background: #D9D9D9;
  }
}

.counter-value {
  font-size: 28rpx;
  color: #333;
  min-width: 100rpx;
  text-align: center;
}

/* 选择器 */
.picker,
.address-picker {
  height: 80rpx;
  padding: 0 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.address-text {
  font-size: 28rpx;
  color: #333;
  flex: 1;
}

.placeholder {
  font-size: 28rpx;
  color: #999;
}

/* 预览 */
.preview-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  display: block;
  font-size: 28rpx;
  color: #666;
  margin-bottom: 20rpx;
}

.preview-card {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C5A 100%);
  border-radius: 16rpx;
  padding: 30rpx;
  color: #fff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid rgba(255, 255, 255, 0.3);
}

.card-title {
  font-size: 32rpx;
  font-weight: bold;
}

.card-code {
  font-size: 24rpx;
  opacity: 0.9;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.info-item {
  display: flex;
  font-size: 26rpx;

  .label {
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 0;
  }

  .value {
    color: #fff;
    flex: 1;
  }
}

/* 按钮 */
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
}
</style>
