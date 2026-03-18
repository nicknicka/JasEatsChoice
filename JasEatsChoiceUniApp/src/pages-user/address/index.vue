<template>
  <view class="address-container">
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <!-- 空状态 -->
      <view class="empty-state" v-if="addresses.length === 0">
        <text class="empty-icon">📍</text>
        <text class="empty-text">还没有收货地址</text>
        <text class="empty-tips">添加一个收货地址吧</text>
      </view>

      <!-- 地址列表 -->
      <view class="address-list" v-else>
        <view
          class="address-item"
          v-for="address in addresses"
          :key="address.id"
          @click="selectAddress(address)"
        >
          <view class="address-content">
            <view class="address-header">
              <view class="name-phone">
                <text class="address-name">{{ address.name }}</text>
                <text class="address-phone">{{ address.phone }}</text>
              </view>
              <view class="default-badge" v-if="address.isDefault">默认</view>
            </view>

            <view class="address-detail">
              <text class="address-region">{{ address.province }} {{ address.city }} {{ address.district }}</text>
              <text class="address-text">{{ address.detail }}</text>
            </view>

            <view class="address-tags" v-if="address.tags && address.tags.length">
              <text class="tag-item" v-for="tag in address.tags" :key="tag">{{ tag }}</text>
            </view>
          </view>

          <view class="address-actions" @click.stop>
            <view class="action-item" @click="setDefaultAddress(address)" v-if="!address.isDefault">
              <text class="action-icon">⭐</text>
              <text class="action-text">设为默认</text>
            </view>
            <view class="action-item" @click="editAddress(address)">
              <text class="action-icon">✏️</text>
              <text class="action-text">编辑</text>
            </view>
            <view class="action-item" @click="deleteAddress(address.id)">
              <text class="action-icon">🗑️</text>
              <text class="action-text">删除</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMore">
        <text class="load-text">{{ loading ? '加载中...' : '上拉加载更多' }}</text>
      </view>
    </scroll-view>

    <!-- 底部添加按钮 -->
    <view class="bottom-bar">
      <button class="add-btn" @click="addAddress">
        <text class="add-icon">➕</text>
        <text>添加新地址</text>
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { addressApi } from '@/api'

// Store
const userStore = useUserStore()

// 状态
const addresses = ref([])
const refreshing = ref(false)
const loading = ref(false)
const hasMore = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 是否从订单确认页跳转过来
const fromOrder = ref(false)

/**
 * 加载地址列表
 */
const loadAddresses = async (showLoading = true) => {
  if (showLoading) {
    loading.value = true
  }

  try {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      loading.value = false
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 调用后端API获取地址列表
    const res = await addressApi.getList({
      userId,
      page: page.value,
      size: pageSize.value
    })

    // 数据映射
    if (Array.isArray(res)) {
      const mappedAddresses = res.map(addr => ({
        id: addr.addressId || addr.id,
        name: addr.receiverName || addr.name,
        phone: addr.receiverPhone || addr.phone,
        province: addr.province || '',
        city: addr.city || '',
        district: addr.district || '',
        detail: addr.detailAddress || addr.detail || '',
        isDefault: addr.isDefault || false,
        tags: addr.tags || []
      }))

      if (page.value === 1) {
        addresses.value = mappedAddresses
      } else {
        addresses.value.push(...mappedAddresses)
      }

      // 判断是否还有更多数据
      hasMore.value = mappedAddresses.length >= pageSize.value
    } else {
      if (page.value === 1) {
        addresses.value = []
      }
      hasMore.value = false
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  await loadAddresses(false)
  refreshing.value = false
}

/**
 * 上拉加载更多
 */
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  loadAddresses()
}

/**
 * 选择地址（从订单页跳转过来）
 */
const selectAddress = (address) => {
  if (!fromOrder.value) return

  // 保存选择的地址到本地
  uni.setStorageSync('selectedAddress', address)

  // 返回上一页
  uni.navigateBack()
}

/**
 * 添加地址
 */
const addAddress = () => {
  uni.navigateTo({
    url: '/pages/address/edit/index'
  })
}

/**
 * 编辑地址
 */
const editAddress = (address) => {
  // 将地址信息转换为URL参数
  const params = encodeURIComponent(JSON.stringify(address))
  uni.navigateTo({
    url: `/pages/address/edit/index?data=${params}`
  })
}

/**
 * 删除地址
 */
const deleteAddress = (id) => {
  const address = addresses.value.find(item => item.id === id)

  uni.showModal({
    title: '删除地址',
    content: `确定要删除「${address.name} ${address.phone}」这个地址吗？`,
    confirmColor: '#FF6B35',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })

          const userId = userStore.userInfo?.userId || userStore.userInfo?.id
          await addressApi.delete(id, { userId })

          uni.hideLoading()

          // 从列表中移除
          const index = addresses.value.findIndex(item => item.id === id)
          if (index > -1) {
            addresses.value.splice(index, 1)
          }

          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })

          // 如果删除的是默认地址，将第一个地址设为默认
          if (address.isDefault && addresses.value.length > 0) {
            await setDefaultAddress(addresses.value[0])
          }
        } catch (error) {
          console.error('删除地址失败:', error)
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
 * 设置默认地址
 */
const setDefaultAddress = async (address) => {
  try {
    uni.showLoading({ title: '设置中...' })

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id
    await addressApi.setDefault(address.id, { userId })

    uni.hideLoading()

    // 更新列表
    addresses.value.forEach(item => {
      item.isDefault = item.id === address.id
    })

    uni.showToast({
      title: '已设为默认地址',
      icon: 'success'
    })
  } catch (error) {
    console.error('设置默认地址失败:', error)
    uni.hideLoading()
    uni.showToast({
      title: error.message || '设置失败',
      icon: 'none'
    })
  }
}

// 组件挂载
onMounted(() => {
  // 获取页面参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.from === 'order') {
    fromOrder.value = true
  }

  // 加载地址列表
  loadAddresses()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.address-container {
  min-height: 100vh;
  background-color: $bg-color-base;
  padding-bottom: 120rpx;
}

.scroll-container {
  height: 100vh;
}

/* 空状态 */
.empty-state {
  @include flex-center-column;
  padding: 200rpx 0;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: $spacing-lg;
    opacity: 0.5;
  }

  .empty-text {
    font-size: $font-size-lg;
    color: $text-color-primary;
    margin-bottom: $spacing-sm;
  }

  .empty-tips {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}

/* 地址列表 */
.address-list {
  padding: $spacing-md;
}

.address-item {
  background-color: $bg-color-white;
  border-radius: $border-radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  box-shadow: $box-shadow-sm;
  transition: all 0.3s;

  &:active {
    transform: scale(0.98);
  }
}

.address-content {
  margin-bottom: $spacing-md;
}

.address-header {
  @include flex-between;
  align-items: center;
  margin-bottom: $spacing-sm;
}

.name-phone {
  @include flex-center;
  gap: $spacing-sm;
}

.address-name {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-color-primary;
}

.address-phone {
  font-size: $font-size-base;
  color: $text-color-regular;
}

.default-badge {
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, #FF6B35, #FF8F61);
  color: #fff;
  font-size: $font-size-xs;
  border-radius: $border-radius-round;
  font-weight: $font-weight-medium;
}

.address-detail {
  @include flex-center;
  gap: $spacing-xs;
  margin-bottom: $spacing-sm;

  .address-region {
    flex-shrink: 0;
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }

  .address-text {
    flex: 1;
    font-size: $font-size-base;
    color: $text-color-primary;
    line-height: $line-height-lg;
    @include text-ellipsis-multi(2);
  }
}

.address-tags {
  @include flex-center;
  gap: $spacing-xs;
  flex-wrap: wrap;
}

.tag-item {
  padding: 4rpx 12rpx;
  background-color: rgba(255, 107, 53, 0.1);
  color: $primary-color;
  font-size: $font-size-xs;
  border-radius: $border-radius-sm;
}

/* 地址操作 */
.address-actions {
  @include flex-center;
  gap: $spacing-lg;
  padding-top: $spacing-sm;
  border-top: 1rpx solid $border-color-lighter;
}

.action-item {
  @include flex-center;
  gap: $spacing-xs;
  padding: $spacing-sm;
  font-size: $font-size-sm;
  color: $text-color-regular;
  cursor: pointer;

  &:active {
    opacity: 0.6;
  }

  .action-icon {
    font-size: $font-size-base;
  }
}

/* 加载更多 */
.load-more {
  @include flex-center;
  padding: $spacing-lg 0;

  .load-text {
    font-size: $font-size-sm;
    color: $text-color-secondary;
  }
}

/* 底部添加按钮 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $bg-color-white;
  padding: $spacing-md;
  box-shadow: 0 -2rpx 10rpx rgba(0, 0, 0, 0.1);
  z-index: $z-index-fixed;
  @include safe-area-bottom;
}

.add-btn {
  width: 100%;
  height: 88rpx;
  @include flex-center;
  gap: $spacing-sm;
  background: linear-gradient(135deg, $primary-color, #FF8F61);
  color: #fff;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $border-radius-round;
  border: none;
  box-shadow: 0 4rpx 12rpx rgba(255, 107, 53, 0.3);

  &:active {
    transform: scale(0.98);
  }

  .add-icon {
    font-size: $font-size-lg;
  }
}
</style>
