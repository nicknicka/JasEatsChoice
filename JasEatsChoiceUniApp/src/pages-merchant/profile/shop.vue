<template>
  <view class="shop-settings-container">
    <!-- 店铺基本信息 -->
    <view class="shop-info-card">
      <view class="shop-avatar-section">
        <image class="shop-avatar" :src="shopInfo.avatar" mode="aspectFill"></image>
        <button class="change-avatar-btn" @tap="changeAvatar">
          <uni-icons type="camera" size="20" color="#fff"></uni-icons>
          <text>更换头像</text>
        </button>
      </view>
      <view class="shop-name-section">
        <text class="shop-name">{{ shopInfo.name }}</text>
        <uni-icons type="compose" size="18" color="#999" @tap="editShopName"></uni-icons>
      </view>
    </view>

    <!-- 店铺设置表单 -->
    <view class="settings-form">
      <!-- 营业状态 -->
      <view class="form-section">
        <view class="section-title">营业状态</view>
        <view class="status-switch">
          <text class="status-label">营业中</text>
          <switch
            :checked="shopInfo.isOpen"
            color="#FF6B35"
            @change="toggleBusinessStatus"
          />
        </view>
      </view>

      <!-- 营业时间 -->
      <view class="form-section">
        <view class="section-title">
          <text>营业时间</text>
          <text class="edit-link" @tap="editBusinessHours">编辑</text>
        </view>
        <view class="business-hours-list">
          <view
            class="hours-item"
            v-for="(item, index) in shopInfo.businessHours"
            :key="index"
          >
            <text class="day-label">{{ item.day }}</text>
            <text class="hours-text" :class="{ closed: !item.isOpen }">
              {{ item.isOpen ? item.timeRange : '休息' }}
            </text>
          </view>
        </view>
      </view>

      <!-- 联系信息 -->
      <view class="form-section">
        <view class="section-title">联系信息</view>
        <view class="form-item">
          <text class="item-label">联系电话</text>
          <input
            class="item-input"
            v-model="shopInfo.phone"
            placeholder="请输入联系电话"
            type="number"
            maxlength="11"
          />
        </view>
        <view class="form-item">
          <text class="item-label">店铺地址</text>
          <view class="address-input" @tap="selectAddress">
            <text class="address-text" :class="{ placeholder: !shopInfo.address }">
              {{ shopInfo.address || '请选择店铺地址' }}
            </text>
            <uni-icons type="location" size="18" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 配送设置 -->
      <view class="form-section">
        <view class="section-title">配送设置</view>
        <view class="form-item">
          <text class="item-label">配送范围</text>
          <input
            class="item-input"
            v-model="shopInfo.deliveryRange"
            placeholder="如：3公里内"
          />
        </view>
        <view class="form-item">
          <text class="item-label">配送费</text>
          <view class="price-input">
            <text class="price-symbol">¥</text>
            <input
              class="price-number"
              v-model="shopInfo.deliveryFee"
              placeholder="0"
              type="digit"
            />
          </view>
        </view>
        <view class="form-item">
          <text class="item-label">起送金额</text>
          <view class="price-input">
            <text class="price-symbol">¥</text>
            <input
              class="price-number"
              v-model="shopInfo.minOrderAmount"
              placeholder="0"
              type="digit"
            />
          </view>
        </view>
      </view>

      <!-- 店铺简介 -->
      <view class="form-section">
        <view class="section-title">店铺简介</view>
        <textarea
          class="shop-desc"
          v-model="shopInfo.description"
          placeholder="请输入店铺简介，介绍您的特色菜品、经营理念等..."
          maxlength="500"
          :show-confirm-bar="false"
        />
        <view class="word-count">{{ shopInfo.description.length }}/500</view>
      </view>

      <!-- 店铺图片 -->
      <view class="form-section">
        <view class="section-title">店铺图片</view>
        <view class="image-upload-section">
          <view class="upload-tips">
            <uni-icons type="info" size="16" color="#999"></uni-icons>
            <text>最多上传5张图片，建议尺寸750x400</text>
          </view>
          <view class="image-list">
            <view
              class="image-item"
              v-for="(img, index) in shopInfo.images"
              :key="index"
            >
              <image class="upload-image" :src="img" mode="aspectFill"></image>
              <view class="delete-btn" @tap="deleteImage(index)">
                <uni-icons type="closeempty" size="16" color="#fff"></uni-icons>
              </view>
            </view>
            <view
              class="upload-btn"
              v-if="shopInfo.images.length < 5"
              @tap="chooseImage"
            >
              <uni-icons type="camera" size="30" color="#D9D9D9"></uni-icons>
              <text class="upload-text">添加图片</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 保存按钮 -->
    <view class="save-button-container">
      <button class="save-button" @tap="saveSettings">保存设置</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { merchantApi } from '@/api'

// 店铺信息
const shopInfo = ref({
  avatar: 'https://via.placeholder.com/120/FF6B35/FFFFFF?text=店',
  name: '老王家常菜',
  isOpen: true,
  businessHours: [
    { day: '周一', isOpen: true, timeRange: '10:00-22:00' },
    { day: '周二', isOpen: true, timeRange: '10:00-22:00' },
    { day: '周三', isOpen: true, timeRange: '10:00-22:00' },
    { day: '周四', isOpen: true, timeRange: '10:00-22:00' },
    { day: '周五', isOpen: true, timeRange: '10:00-22:00' },
    { day: '周六', isOpen: true, timeRange: '09:00-23:00' },
    { day: '周日', isOpen: true, timeRange: '09:00-23:00' }
  ],
  phone: '13800138000',
  address: 'XX大学第一食堂一楼101号',
  latitude: 39.90469,
  longitude: 116.40717,
  deliveryRange: '3公里内',
  deliveryFee: '5',
  minOrderAmount: '20',
  description: '本店专注家常菜，用心做好每一道菜。食材新鲜，口味正宗，价格实惠。欢迎同学们光临！',
  images: [
    'https://via.placeholder.com/750x400/FF6B35/FFFFFF?text=1',
    'https://via.placeholder.com/750x400/FF6B35/FFFFFF?text=2'
  ]
})

onMounted(async () => {
  await loadShopInfo()
})

/**
 * M-003: 加载店铺信息
 */
const loadShopInfo = async () => {
  try {
    const res = await merchantApi.getShopInfo()
    if (res.code === 200 && res.data) {
      shopInfo.value = {
        avatar: res.data.avatar || '',
        name: res.data.name || '',
        isOpen: res.data.isOpen !== undefined ? res.data.isOpen : true,
        businessHours: res.data.businessHours || shopInfo.value.businessHours,
        phone: res.data.phone || '',
        address: res.data.address || '',
        latitude: res.data.latitude || 0,
        longitude: res.data.longitude || 0,
        deliveryRange: res.data.deliveryRange || '',
        deliveryFee: res.data.deliveryFee || '',
        minOrderAmount: res.data.minOrderAmount || '',
        description: res.data.description || '',
        images: res.data.images || []
      }
    }
  } catch (error) {
    console.error('加载店铺信息失败:', error)
    // 保持默认数据
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
      shopInfo.value.avatar = res.tempFilePaths[0]
      uni.showToast({
        title: '头像已更新',
        icon: 'success'
      })
    }
  })
}

/**
 * 编辑店铺名称
 */
const editShopName = () => {
  uni.showModal({
    title: '修改店铺名称',
    content: shopInfo.value.name,
    editable: true,
    placeholderText: '请输入店铺名称',
    success: (res) => {
      if (res.confirm && res.content) {
        shopInfo.value.name = res.content
      }
    }
  })
}

/**
 * 切换营业状态
 */
const toggleBusinessStatus = (e) => {
  const isOpen = e.detail.value
  uni.showModal({
    title: isOpen ? '开始营业' : '停止营业',
    content: isOpen ? '确认开始营业吗？' : '确认停止营业吗？停止后用户将无法下单。',
    success: (res) => {
      if (res.confirm) {
        shopInfo.value.isOpen = isOpen
        uni.showToast({
          title: isOpen ? '已开始营业' : '已停止营业',
          icon: 'success'
        })
      } else {
        // 取消则恢复原状态
        shopInfo.value.isOpen = !isOpen
      }
    }
  })
}

/**
 * 编辑营业时间
 */
const editBusinessHours = () => {
  uni.navigateTo({
    url: '/profile/business-hours'
  })
}

/**
 * 选择地址
 * 添加超时处理，默认30秒超时
 */
const selectAddress = () => {
  uni.showLoading({
    title: '请选择地址...'
  })

  // 创建超时定时器
  const timer = setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '选择地址超时',
      icon: 'none'
    })
  }, 30000)

  uni.chooseLocation({
    success: (res) => {
      clearTimeout(timer)
      uni.hideLoading()
      shopInfo.value.address = res.address + res.name
      shopInfo.value.latitude = res.latitude
      shopInfo.value.longitude = res.longitude
    },
    fail: (err) => {
      clearTimeout(timer)
      uni.hideLoading()

      // 用户取消选择不提示错误
      if (err.errMsg && !err.errMsg.includes('cancel')) {
        console.error('选择地址失败:', err)
        uni.showToast({
          title: '选择地址失败',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 选择图片
 */
const chooseImage = () => {
  const remainCount = 5 - shopInfo.value.images.length
  uni.chooseImage({
    count: remainCount,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      shopInfo.value.images.push(...res.tempFilePaths)
    }
  })
}

/**
 * 删除图片
 */
const deleteImage = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定删除这张图片吗？',
    success: (res) => {
      if (res.confirm) {
        shopInfo.value.images.splice(index, 1)
      }
    }
  })
}

/**
 * M-004: 保存设置
 */
const saveSettings = async () => {
  // 验证
  if (!shopInfo.value.phone) {
    uni.showToast({
      title: '请输入联系电话',
      icon: 'none'
    })
    return
  }

  if (!/^1[3-9]\d{9}$/.test(shopInfo.value.phone)) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  if (!shopInfo.value.address) {
    uni.showToast({
      title: '请选择店铺地址',
      icon: 'none'
    })
    return
  }

  try {
    uni.showLoading({
      title: '保存中...'
    })

    // M-004: 调用API保存店铺信息
    const data = {
      avatar: shopInfo.value.avatar,
      name: shopInfo.value.name,
      isOpen: shopInfo.value.isOpen,
      businessHours: shopInfo.value.businessHours,
      phone: shopInfo.value.phone,
      address: shopInfo.value.address,
      latitude: shopInfo.value.latitude,
      longitude: shopInfo.value.longitude,
      deliveryRange: shopInfo.value.deliveryRange,
      deliveryFee: shopInfo.value.deliveryFee,
      minOrderAmount: shopInfo.value.minOrderAmount,
      description: shopInfo.value.description,
      images: shopInfo.value.images
    }

    const res = await merchantApi.updateShopInfo(data)

    uni.hideLoading()

    if (res.code === 200) {
      uni.showToast({
        title: '保存成功',
        icon: 'success'
      })

      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      throw new Error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存店铺信息失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '保存失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.shop-settings-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 120rpx;
}

/* 店铺基本信息 */
.shop-info-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 40rpx 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20rpx;
}

.shop-avatar-section {
  position: relative;
}

.shop-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 20rpx;
  border: 6rpx solid rgba(255, 255, 255, 0.3);
}

.change-avatar-btn {
  position: absolute;
  bottom: -10rpx;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  gap: 5rpx;
  border: none;
}

.shop-name-section {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.shop-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

/* 设置表单 */
.settings-form {
  padding: 20rpx;
}

.form-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 25rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-link {
  font-size: 26rpx;
  color: #1890FF;
  font-weight: normal;
}

/* 营业状态 */
.status-switch {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
}

.status-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

/* 营业时间 */
.business-hours-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.hours-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 8rpx;
}

.day-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.hours-text {
  font-size: 26rpx;
  color: #666;

  &.closed {
    color: #999;
  }
}

/* 表单项 */
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
  width: 180rpx;
  font-size: 28rpx;
  color: #666;
  flex-shrink: 0;
}

.item-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  text-align: right;
}

.address-input {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10rpx;
}

.address-text {
  font-size: 28rpx;
  color: #333;
  text-align: right;

  &.placeholder {
    color: #999;
  }
}

.price-input {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 5rpx;
}

.price-symbol {
  font-size: 28rpx;
  color: #FF6B35;
  font-weight: bold;
}

.price-number {
  width: 150rpx;
  font-size: 32rpx;
  color: #FF6B35;
  font-weight: bold;
  text-align: right;
}

/* 店铺简介 */
.shop-desc {
  width: 100%;
  min-height: 200rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.word-count {
  text-align: right;
  padding-top: 10rpx;
  font-size: 24rpx;
  color: #999;
}

/* 店铺图片 */
.upload-tips {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 15rpx;
  background: #FFF7E6;
  border-radius: 8rpx;
  margin-bottom: 20rpx;
  font-size: 24rpx;
  color: #FAAD14;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15rpx;
}

.image-item {
  width: 220rpx;
  height: 220rpx;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.upload-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 5rpx;
  right: 5rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  @include flex-center;
}

.upload-btn {
  width: 220rpx;
  height: 220rpx;
  border: 2rpx dashed #D9D9D9;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 10rpx;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
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
}
</style>
