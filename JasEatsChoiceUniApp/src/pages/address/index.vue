<template>
  <view class="address-container">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <view class="nav-title">收货地址</view>
      <view class="nav-action" @click="addAddress">
        <text class="action-text">新增</text>
      </view>
    </view>

    <scroll-view
      class="address-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view class="empty-container" v-if="addressList.length === 0 && !loading">
        <text class="empty-icon">📍</text>
        <text class="empty-text">暂无收货地址</text>
        <text class="empty-desc">添加收货地址，方便订单配送</text>
        <button class="add-btn" @click="addAddress">添加地址</button>
      </view>

      <!-- 地址列表 -->
      <view class="address-list" v-else>
        <view
          class="address-item"
          v-for="address in addressList"
          :key="address.id"
          @click="editAddress(address)"
        >
          <!-- 默认标签 -->
          <view class="default-badge" v-if="address.isDefault">
            <text class="badge-text">默认</text>
          </view>

          <!-- 地址信息 -->
          <view class="address-content">
            <view class="address-header">
              <text class="contact-name">{{ address.name }}</text>
              <text class="contact-phone">{{ maskedPhone(address.phone) }}</text>
            </view>

            <text class="address-detail">
              {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detail }}
            </text>

            <view class="address-tags" v-if="address.tags && address.tags.length > 0">
              <text
                class="address-tag"
                v-for="tag in address.tags"
                :key="tag"
              >{{ tag }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="address-actions" @click.stop>
            <view class="action-btn" @click="setDefault(address)">
              <text class="action-icon">{{ address.isDefault ? '⭐' : '☆' }}</text>
              <text class="action-text">{{ address.isDefault ? '默认地址' : '设为默认' }}</text>
            </view>

            <view class="action-btn" @click="editAddress(address)">
              <text class="action-icon">✏️</text>
              <text class="action-text">编辑</text>
            </view>

            <view class="action-btn danger-btn" @click="deleteAddress(address)">
              <text class="action-icon">🗑️</text>
              <text class="action-text">删除</text>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { addressApi } from '@/api'

// 地址列表
const addressList = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)

// 组件挂载
onMounted(() => {
  loadAddresses()
})

/**
 * 加载地址列表
 */
const loadAddresses = async () => {
  loading.value = true

  try {
    // TODO: 调用接口获取地址列表
    // const res = await addressApi.getList()

    // 模拟数据
    addressList.value = [
      {
        id: 1,
        name: '张三',
        phone: '13800138000',
        province: '广东省',
        city: '深圳市',
        district: '南山区',
        detail: '科技园南区深圳湾科技生态园10栋A座',
        isDefault: true,
        tags: ['家', '公司']
      },
      {
        id: 2,
        name: '李四',
        phone: '13900139000',
        province: '广东省',
        city: '深圳市',
        district: '福田区',
        detail: '福田中心区福华一路168号',
        isDefault: false,
        tags: ['学校']
      }
    ]
  } catch (error) {
    console.error('加载地址列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  await loadAddresses()
  refreshing.value = false
}

/**
 * 手机号脱敏
 */
const maskedPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 新增地址
 */
const addAddress = () => {
  uni.navigateTo({
    url: '/pages/address/edit'
  })
}

/**
 * 编辑地址
 */
const editAddress = (address) => {
  uni.navigateTo({
    url: `/pages/address/edit?id=${address.id}`
  })
}

/**
 * 设为默认地址
 */
const setDefault = async (address) => {
  if (address.isDefault) return

  try {
    uni.showLoading({ title: '设置中...' })

    // TODO: 调用接口设置默认地址
    // await addressApi.setDefault(address.id)

    // 更新本地列表
    addressList.value.forEach(item => {
      item.isDefault = item.id === address.id
    })

    uni.hideLoading()
    uni.showToast({
      title: '设置成功',
      icon: 'success'
    })
  } catch (error) {
    uni.hideLoading()
    uni.showToast({
      title: error.message || '设置失败',
      icon: 'none'
    })
  }
}

/**
 * 删除地址
 */
const deleteAddress = (address) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该地址吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })

          // TODO: 调用接口删除地址
          // await addressApi.delete(address.id)

          // 从列表中移除
          const index = addressList.value.findIndex(item => item.id === address.id)
          if (index > -1) {
            addressList.value.splice(index, 1)
          }

          uni.hideLoading()
          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })
        } catch (error) {
          uni.hideLoading()
          uni.showToast({
            title: error.message || '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.address-container {
  min-height: 100vh;
  background-color: $bg-color-base;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background-color: $bg-color-white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-back {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.back-icon {
  font-size: 48rpx;
  color: $text-color-primary;
  font-weight: bold;
}

.nav-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.nav-action {
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
}

.action-text {
  font-size: $font-size-base;
  color: $primary-color;
  font-weight: $font-weight-bold;
}

/* 地址列表 */
.address-scroll {
  height: 100vh;
  padding: $spacing-md;
  padding-top: calc(108rpx + #{$spacing-md});
  padding-bottom: env(safe-area-inset-bottom);
}

/* 空状态 */
.empty-container {
  @include flex-center-column;
  padding: 200rpx $spacing-xl;
  text-align: center;
}

.empty-icon {
  font-size: 160rpx;
  margin-bottom: $spacing-lg;
}

.empty-text {
  font-size: $font-size-xl;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.empty-desc {
  font-size: $font-size-base;
  color: $text-color-secondary;
  margin-bottom: $spacing-xl;
}

.add-btn {
  padding: $spacing-md $spacing-xl;
  background-color: $primary-color;
  color: #fff;
  border-radius: $border-radius-round;
  font-size: $font-size-base;
  border: none;
}

/* 地址列表 */
.address-list {
  .address-item {
    position: relative;
    background-color: $bg-color-white;
    border-radius: $border-radius-lg;
    padding: $spacing-lg;
    margin-bottom: $spacing-md;
    box-shadow: $box-shadow-sm;
  }
}

/* 默认标签 */
.default-badge {
  position: absolute;
  top: $spacing-md;
  right: $spacing-md;
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border-radius: $border-radius-round;

  .badge-text {
    font-size: $font-size-xs;
    color: #fff;
    font-weight: $font-weight-bold;
  }
}

/* 地址内容 */
.address-content {
  margin-bottom: $spacing-md;
}

.address-header {
  @include flex-center;
  gap: $spacing-md;
  margin-bottom: $spacing-sm;
}

.contact-name {
  font-size: $font-size-lg;
  color: $text-color-primary;
  font-weight: $font-weight-bold;
}

.contact-phone {
  font-size: $font-size-base;
  color: $text-color-secondary;
}

.address-detail {
  display: block;
  font-size: $font-size-base;
  color: $text-color-primary;
  line-height: 1.6;
  margin-bottom: $spacing-sm;
}

.address-tags {
  @include flex-center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}

.address-tag {
  padding: 4rpx 12rpx;
  background-color: rgba($primary-color, 0.1);
  color: $primary-color;
  border-radius: $border-radius-round;
  font-size: $font-size-xs;
}

/* 地址操作 */
.address-actions {
  display: flex;
  align-items: center;
  border-top: 1rpx solid $border-color-lighter;
  padding-top: $spacing-md;
}

.action-btn {
  flex: 1;
  @include flex-center-column;
  gap: 4rpx;
  padding: $spacing-sm 0;

  &.danger-btn {
    .action-text {
      color: $danger-color;
    }
  }
}

.action-icon {
  font-size: 32rpx;
}

.action-text {
  font-size: $font-size-xs;
  color: $text-color-secondary;
}
</style>
