<template>
  <view class="profile-edit-container">
    <!-- 头像上传 -->
    <view class="avatar-section">
      <image class="avatar" :src="profile.avatar" mode="aspectFill"></image>
      <button class="change-avatar-btn" @tap="changeAvatar">
        <uni-icons type="camera" size="18" color="#fff"></uni-icons>
        <text>更换头像</text>
      </button>
    </view>

    <!-- 基本信息 -->
    <view class="form-section">
      <view class="section-title">基本信息</view>
      <view class="form-item">
        <text class="item-label required">联系人姓名</text>
        <input
          class="item-input"
          v-model="profile.contactName"
          placeholder="请输入联系人姓名"
        />
      </view>
      <view class="form-item">
        <text class="item-label required">联系电话</text>
        <input
          class="item-input"
          v-model="profile.phone"
          placeholder="请输入联系电话"
          type="number"
          maxlength="11"
        />
      </view>
      <view class="form-item">
        <text class="item-label">微信号</text>
        <input
          class="item-input"
          v-model="profile.wechat"
          placeholder="方便与客户联系"
        />
      </view>
    </view>

    <!-- 认证信息 -->
    <view class="form-section">
      <view class="section-title">认证信息</view>
      <view class="form-item">
        <text class="item-label required">商户类型</text>
        <picker
          :value="merchantTypeIndex"
          :range="merchantTypes"
          range-key="label"
          @change="onMerchantTypeChange"
        >
          <view class="picker-value">
            {{ profile.merchantType ? merchantTypes[merchantTypeIndex].label : '请选择商户类型' }}
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="item-label required">营业执照号</text>
        <input
          class="item-input"
          v-model="profile.licenseNo"
          placeholder="请输入营业执照号"
        />
      </view>
      <view class="form-item">
        <text class="item-label required">店铺名称</text>
        <input
          class="item-input"
          v-model="profile.shopName"
          placeholder="请输入店铺名称"
        />
      </view>
    </view>

    <!-- 营业执照 -->
    <view class="form-section">
      <view class="section-title">营业执照</view>
      <view class="upload-section">
        <view
          class="upload-card"
          v-if="profile.licenseImage"
          @tap="previewImage(profile.licenseImage)"
        >
          <image class="license-image" :src="profile.licenseImage" mode="aspectFill"></image>
          <view class="delete-btn" @tap.stop="deleteLicense">
            <uni-icons type="closeempty" size="16" color="#fff"></uni-icons>
          </view>
        </view>
        <view class="upload-btn" v-else @tap="uploadLicense">
          <uni-icons type="image" size="40" color="#D9D9D9"></uni-icons>
          <text class="upload-text">上传营业执照</text>
          <text class="upload-tips">支持JPG/PNG，最大5MB</text>
        </view>
      </view>
    </view>

    <!-- 银行信息 -->
    <view class="form-section">
      <view class="section-title">结算信息</view>
      <view class="form-item">
        <text class="item-label required">开户银行</text>
        <picker
          :value="bankIndex"
          :range="banks"
          range-key="name"
          @change="onBankChange"
        >
          <view class="picker-value">
            {{ profile.bankName || '请选择开户银行' }}
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="item-label required">银行卡号</text>
        <input
          class="item-input"
          v-model="profile.bankAccount"
          placeholder="请输入银行卡号"
          type="number"
        />
      </view>
      <view class="form-item">
        <text class="item-label required">开户人姓名</text>
        <input
          class="item-input"
          v-model="profile.accountName"
          placeholder="请输入开户人姓名"
        />
      </view>
    </view>

    <!-- 认证状态 -->
    <view class="auth-status-card" v-if="profile.authStatus !== 'pending'">
      <view class="status-icon" :class="'status-' + profile.authStatus">
        <uni-icons
          :type="profile.authStatus === 'approved' ? 'checkbox-filled' : 'close-filled'"
          size="40"
          color="#fff"
        ></uni-icons>
      </view>
      <view class="status-info">
        <text class="status-title">{{ getAuthStatusText(profile.authStatus) }}</text>
        <text class="status-desc">{{ getAuthStatusDesc(profile.authStatus) }}</text>
      </view>
      <view class="status-time" v-if="profile.authTime">
        认证时间：{{ profile.authTime }}
      </view>
    </view>

    <!-- 认证提示 -->
    <view class="auth-tips-card" v-else>
      <view class="tips-header">
        <uni-icons type="info" size="20" color="#FF6B35"></uni-icons>
        <text class="tips-title">认证说明</text>
      </view>
      <view class="tips-content">
        <text class="tips-item">1. 请确保上传的营业执照清晰完整</text>
        <text class="tips-item">2. 营业执照信息需与填写信息一致</text>
        <text class="tips-item">3. 银行卡信息将用于收入结算</text>
        <text class="tips-item">4. 认证审核时间为1-3个工作日</text>
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="save-button-container">
      <button
        class="save-button"
        @tap="saveProfile"
        :disabled="profile.authStatus === 'approved'"
      >
        {{ profile.authStatus === 'approved' ? '已认证' : '提交认证' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 商户类型
const merchantTypes = [
  { label: '个体工商户', value: 'individual' },
  { label: '企业商户', value: 'company' },
  { label: '小微商户', value: 'micro' }
]

const merchantTypeIndex = ref(0)

// 银行列表
const banks = [
  { name: '中国工商银行', code: 'ICBC' },
  { name: '中国建设银行', code: 'CCB' },
  { name: '中国农业银行', code: 'ABC' },
  { name: '中国银行', code: 'BOC' },
  { name: '交通银行', code: 'BOCOM' },
  { name: '招商银行', code: 'CMB' },
  { name: '浦发银行', code: 'SPDB' },
  { name: '民生银行', code: 'CMBC' }
]

const bankIndex = ref(0)

// 商家资料
const profile = ref({
  avatar: 'https://via.placeholder.com/120/FF6B35/FFFFFF?text=头像',
  contactName: '王老板',
  phone: '13800138000',
  wechat: 'wanglaoban123',
  merchantType: 'individual',
  licenseNo: '92110108MA01234567',
  shopName: '老王家常菜',
  licenseImage: '',
  bankName: '',
  bankAccount: '',
  accountName: '',
  authStatus: 'pending', // pending, approved, rejected
  authTime: ''
})

onMounted(() => {
  loadProfile()
})

/**
 * 加载商家资料
 */
const loadProfile = () => {
  // TODO: 调用API获取商家资料
  // const res = await merchantApi.getProfile()
  // profile.value = res.data

  // 查找类型索引
  const typeIndex = merchantTypes.findIndex(t => t.value === profile.value.merchantType)
  if (typeIndex !== -1) {
    merchantTypeIndex.value = typeIndex
  }
}

/**
 * 更换头像
 */
const changeAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      profile.value.avatar = res.tempFilePaths[0]
      uni.showToast({
        title: '头像已更新',
        icon: 'success'
      })
    }
  })
}

/**
 * 选择商户类型
 */
const onMerchantTypeChange = (e) => {
  merchantTypeIndex.value = e.detail.value
  profile.value.merchantType = merchantTypes[merchantTypeIndex.value].value
}

/**
 * 选择银行
 */
const onBankChange = (e) => {
  bankIndex.value = e.detail.value
  profile.value.bankName = banks[bankIndex.value].name
}

/**
 * 上传营业执照
 */
const uploadLicense = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      profile.value.licenseImage = res.tempFilePaths[0]
      uni.showToast({
        title: '上传成功',
        icon: 'success'
      })
    }
  })
}

/**
 * 删除营业执照
 */
const deleteLicense = () => {
  uni.showModal({
    title: '提示',
    content: '确定删除营业执照吗？',
    success: (res) => {
      if (res.confirm) {
        profile.value.licenseImage = ''
      }
    }
  })
}

/**
 * 预览图片
 */
const previewImage = (url) => {
  uni.previewImage({
    urls: [url],
    current: url
  })
}

/**
 * 获取认证状态文本
 */
const getAuthStatusText = (status) => {
  const statusMap = {
    pending: '待审核',
    approved: '认证成功',
    rejected: '认证失败'
  }
  return statusMap[status] || ''
}

/**
 * 获取认证状态描述
 */
const getAuthStatusDesc = (status) => {
  const descMap = {
    pending: '您的认证信息正在审核中，请耐心等待',
    approved: '恭喜您，已通过实名认证',
    rejected: '认证未通过，请检查信息后重新提交'
  }
  return descMap[status] || ''
}

/**
 * 保存资料
 */
const saveProfile = () => {
  // 验证必填项
  if (!profile.value.contactName) {
    uni.showToast({
      title: '请输入联系人姓名',
      icon: 'none'
    })
    return
  }

  if (!profile.value.phone) {
    uni.showToast({
      title: '请输入联系电话',
      icon: 'none'
    })
    return
  }

  if (!/^1[3-9]\d{9}$/.test(profile.value.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  if (!profile.value.licenseNo) {
    uni.showToast({
      title: '请输入营业执照号',
      icon: 'none'
    })
    return
  }

  if (!profile.value.shopName) {
    uni.showToast({
      title: '请输入店铺名称',
      icon: 'none'
    })
    return
  }

  if (!profile.value.licenseImage) {
    uni.showToast({
      title: '请上传营业执照',
      icon: 'none'
    })
    return
  }

  if (!profile.value.bankName) {
    uni.showToast({
      title: '请选择开户银行',
      icon: 'none'
    })
    return
  }

  if (!profile.value.bankAccount) {
    uni.showToast({
      title: '请输入银行卡号',
      icon: 'none'
    })
    return
  }

  if (!/^\d{16,19}$/.test(profile.value.bankAccount)) {
    uni.showToast({
      title: '请输入正确的银行卡号',
      icon: 'none'
    })
    return
  }

  if (!profile.value.accountName) {
    uni.showToast({
      title: '请输入开户人姓名',
      icon: 'none'
    })
    return
  }

  // TODO: 调用API保存商家资料
  uni.showLoading({
    title: '提交中...'
  })

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '提交成功',
      icon: 'success'
    })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  }, 1500)
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.profile-edit-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 头像区域 */
.avatar-section {
  background: #fff;
  padding: 60rpx 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 25rpx;
  margin-bottom: 20rpx;
}

.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 4rpx solid #eee;
}

.change-avatar-btn {
  background: #FF6B35;
  color: #fff;
  font-size: 24rpx;
  padding: 10rpx 24rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  gap: 8rpx;
  border: none;
}

/* 表单区域 */
.form-section {
  background: #fff;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 25rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.item-label {
  width: 200rpx;
  font-size: 28rpx;
  color: #666;
  flex-shrink: 0;

  &.required::before {
    content: '*';
    color: #F5222D;
    margin-right: 5rpx;
  }
}

.item-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.picker-value {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10rpx;
  font-size: 28rpx;
  color: #333;
}

/* 上传区域 */
.upload-section {
  display: flex;
  justify-content: center;
}

.upload-card {
  width: 500rpx;
  height: 350rpx;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.license-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 44rpx;
  height: 44rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

.upload-btn {
  width: 500rpx;
  height: 350rpx;
  border: 2rpx dashed #D9D9D9;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 15rpx;
}

.upload-text {
  font-size: 28rpx;
  color: #666;
}

.upload-tips {
  font-size: 24rpx;
  color: #999;
}

/* 认证状态 */
.auth-status-card {
  background: #fff;
  padding: 40rpx 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.status-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  @include flex-center;

  &.status-approved {
    background: #52C41A;
  }

  &.status-rejected {
    background: #F5222D;
  }
}

.status-info {
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.status-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.status-desc {
  font-size: 26rpx;
  color: #666;
}

.status-time {
  font-size: 24rpx;
  color: #999;
}

/* 认证提示 */
.auth-tips-card {
  background: #FFF7E6;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
  border: 1rpx solid #FFE7BA;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 20rpx;
}

.tips-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B35;
}

.tips-content {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.tips-item {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

/* 保存按钮 */
.save-button-container {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx;
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.save-button {
  width: 100%;
  height: 90rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  border-radius: 45rpx;
  border: none;
  @include flex-center;

  &[disabled] {
    background: #D9D9D9;
    color: #999;
  }
}
</style>
