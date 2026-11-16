<template>
  <view class="wishlist-container">
    <!-- 后端只提供待审核列表（GET /v1/wish-list/merchant/pending），
         没有按审核状态筛选、也没有分页，所以这里不再有状态 Tab 和上拉加载 -->
    <view class="list-header">
      <text class="list-title">待审核需求</text>
      <text class="list-count" v-if="!loading && !errorMessage">共 {{ wishlist.length }} 条</text>
    </view>

    <!-- 身份缺失 / 接口失败：提示 + 重试，不回填任何假数据 -->
    <view class="error-state" v-if="errorMessage && !loading">
      <text class="error-text">{{ errorMessage }}</text>
      <button class="retry-btn" @tap="loadWishlist">重新加载</button>
    </view>

    <scroll-view
      v-else
      class="wishlist-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >
      <view
        class="wish-card"
        v-for="item in wishlist"
        :key="item.id"
        @tap="toDetail(item)"
      >
        <!-- 用户信息：userNickname / userAvatar 后端 VO 声明了但 service 未赋值，
             取不到就不渲染头像，昵称缺失时退回展示真实的用户 ID -->
        <view class="user-header">
          <image
            v-if="item.userAvatar"
            class="user-avatar"
            :src="item.userAvatar"
            mode="aspectFill"
          ></image>
          <view class="user-info">
            <text class="user-name">{{ displayUserName(item) }}</text>
            <text class="submit-time">{{ formatRelativeTime(item.createdAt) }}</text>
          </view>
          <view class="wish-status">{{ item.auditStatusName }}</view>
        </view>

        <!-- 需求内容（全部取自后端 WishListItemDetailVO 的真实字段） -->
        <view class="wish-content">
          <view class="dish-row">
            <image
              v-if="item.dishImage"
              class="dish-image"
              :src="item.dishImage"
              mode="aspectFill"
            ></image>
            <view class="dish-main">
              <text class="dish-name">{{ item.dishName }}</text>
              <text class="wish-text" v-if="item.description">{{ item.description }}</text>
            </view>
          </view>

          <view class="detail-line" v-if="item.tasteRequirement">
            <text class="line-label">口味要求</text>
            <text class="line-value">{{ item.tasteRequirement }}</text>
          </view>

          <view class="detail-line" v-if="item.expectedAvailableTime">
            <text class="line-label">期望上架</text>
            <text class="line-value">{{ formatDateTime(item.expectedAvailableTime) }}</text>
          </view>

          <view class="detail-line" v-if="hasRemainingHours(item)">
            <text class="line-label">剩余审核</text>
            <text class="line-value">{{ item.remainingHours }} 小时</text>
          </view>
        </view>

        <!-- 快捷审核：受理 / 拒绝都打 POST /v1/wish-list/audit -->
        <view class="quick-actions" v-if="item.auditStatus === 0" @tap.stop="stopPropagation">
          <button class="action-btn reject" :disabled="submitting" @tap="rejectWish(item)">
            暂无法满足
          </button>
          <button class="action-btn accept" :disabled="submitting" @tap="acceptWish(item)">
            受理需求
          </button>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-status" v-if="loading">
        <text>加载中...</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="wishlist.length === 0 && !loading">
        <empty text="暂无待审核需求" icon="💭" buttonText="刷新列表" @button-click="onRefresh" />
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { wishApi, WISH_REJECTION_REASONS, OTHER_REJECTION_CODE } from '@/api/modules/wish.js'
import { useMerchantStore } from '@/store/modules/merchant'
import { formatRelativeTime, formatDateTime, showConfirm } from '@/utils/helper'

const merchantStore = useMerchantStore()

const wishlist = ref([])
const loading = ref(false)
const refreshing = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

onMounted(() => {
  loadWishlist()
})

/**
 * 后端取不到昵称时用真实的用户 ID 兜底，不编造姓名
 */
const displayUserName = (item) => item.userNickname || item.userId || ''

/**
 * remainingHours 只在待审核且后端算得出来时才有值
 */
const hasRemainingHours = (item) =>
  item.remainingHours !== null && item.remainingHours !== undefined

const stopPropagation = () => {}

/**
 * 加载商家待审核列表
 * 后端 merchantId 从请求身份取，所以这里只校验本地有没有商家身份，不往接口传参
 */
const loadWishlist = async () => {
  if (loading.value) return

  const merchantId = merchantStore.resolveMerchantId()
  if (!merchantId) {
    wishlist.value = []
    errorMessage.value = '未获取到商家身份，请重新登录商家账号后再试'
    refreshing.value = false
    return
  }

  loading.value = true
  errorMessage.value = ''

  try {
    const res = await wishApi.getMerchantPending()
    wishlist.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('加载待审核需求失败:', error)
    wishlist.value = []
    errorMessage.value = error.message || '待审核需求加载失败，请稍后重试'
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  loadWishlist()
}

/**
 * 跳转到详情
 */
const toDetail = (item) => {
  uni.navigateTo({
    url: `/pages-merchant/wishlist/audit?id=${item.id}`
  })
}

/**
 * 让商家选一个拒绝原因
 * 后端 approved=false 时 rejectionReasonCode 必填，所以必须选一个 code；
 * 选「其他原因」时再补一句说明写进 auditRemark
 * @returns {Promise<{code:number,title:string,remark:string}|null>} 取消返回 null
 */
const pickRejectionReason = () =>
  new Promise((resolve) => {
    uni.showActionSheet({
      itemList: WISH_REJECTION_REASONS.map((reason) => reason.title),
      success: (res) => {
        const reason = WISH_REJECTION_REASONS[res.tapIndex]
        if (!reason) {
          resolve(null)
          return
        }
        if (reason.code !== OTHER_REJECTION_CODE) {
          resolve({ code: reason.code, title: reason.title, remark: '' })
          return
        }
        uni.showModal({
          title: '其他原因',
          editable: true,
          placeholderText: '请说明具体原因',
          success: (modalRes) => {
            const remark = (modalRes.content || '').trim()
            if (!modalRes.confirm) {
              resolve(null)
              return
            }
            if (!remark) {
              uni.showToast({ title: '请填写具体原因', icon: 'none' })
              resolve(null)
              return
            }
            resolve({ code: reason.code, title: reason.title, remark })
          },
          fail: () => resolve(null)
        })
      },
      fail: () => resolve(null)
    })
  })

/**
 * 受理需求：POST /v1/wish-list/audit { approved: true }
 */
const acceptWish = async (item) => {
  const confirmed = await showConfirm('受理后该需求状态变为已通过，确认受理吗？', '受理需求')
  if (!confirmed) return

  submitting.value = true
  uni.showLoading({ title: '处理中...' })
  try {
    const res = await wishApi.approve(item.id)
    if (res.data !== true) {
      throw new Error('受理失败')
    }
    uni.showToast({ title: '已受理', icon: 'success' })
    await loadWishlist()
  } catch (error) {
    console.error('受理需求失败:', error)
    uni.showToast({ title: error.message || '受理失败', icon: 'none' })
  } finally {
    uni.hideLoading()
    submitting.value = false
  }
}

/**
 * 拒绝需求：POST /v1/wish-list/audit { approved: false, rejectionReasonCode, ... }
 */
const rejectWish = async (item) => {
  const reason = await pickRejectionReason()
  if (!reason) return

  submitting.value = true
  uni.showLoading({ title: '处理中...' })
  try {
    const res = await wishApi.reject(item.id, {
      rejectionReasonCode: reason.code,
      rejectionReason: reason.remark || reason.title,
      auditRemark: reason.remark || ''
    })
    if (res.data !== true) {
      throw new Error('拒绝失败')
    }
    uni.showToast({ title: '已拒绝', icon: 'success' })
    await loadWishlist()
  } catch (error) {
    console.error('拒绝需求失败:', error)
    uni.showToast({ title: error.message || '拒绝失败', icon: 'none' })
  } finally {
    uni.hideLoading()
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.wishlist-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 列表头 */
.list-header {
  background: #fff;
  padding: 24rpx 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid #eee;
}

.list-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.list-count {
  font-size: 26rpx;
  color: #999;
}

/* 错误态 */
.error-state {
  margin: 100rpx 40rpx 0;
  padding: 60rpx 30rpx;
  background: #fff;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.error-text {
  font-size: 28rpx;
  color: #666;
  text-align: center;
  line-height: 1.6;
}

.retry-btn {
  min-width: 240rpx;
  height: 70rpx;
  line-height: 70rpx;
  border-radius: 35rpx;
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  border: none;

  &::after {
    border: none;
  }
}

/* 列表 */
.wishlist-scroll {
  flex: 1;
  padding: 20rpx;
}

.wish-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

/* 用户头部 */
.user-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 20rpx;
  padding-bottom: 15rpx;
  border-bottom: 1rpx solid #eee;
}

.user-avatar {
  width: 70rpx;
  height: 70rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.submit-time {
  font-size: 24rpx;
  color: #999;
}

.wish-status {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  font-weight: bold;
  background: #FFF7E6;
  color: #FAAD14;
}

/* 需求内容 */
.wish-content {
  margin-bottom: 15rpx;
}

.dish-row {
  display: flex;
  gap: 15rpx;
  margin-bottom: 15rpx;
}

.dish-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.dish-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.dish-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.wish-text {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.detail-line {
  display: flex;
  gap: 12rpx;
  margin-bottom: 10rpx;
}

.line-label {
  width: 130rpx;
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.line-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

/* 快捷操作 */
.quick-actions {
  display: flex;
  gap: 15rpx;
  margin-top: 20rpx;
}

.action-btn {
  flex: 1;
  height: 70rpx;
  line-height: 70rpx;
  border-radius: 35rpx;
  font-size: 28rpx;
  border: none;

  &::after {
    border: none;
  }

  &.accept {
    background: #FF6B35;
    color: #fff;
  }

  &.reject {
    background: #F5F5F5;
    color: #666;
  }
}

/* 加载状态 */
.load-status {
  text-align: center;
  padding: 30rpx 0;
  color: #999;
  font-size: 26rpx;
}

/* 空状态 */
.empty-state {
  padding-top: 200rpx;
}
</style>
