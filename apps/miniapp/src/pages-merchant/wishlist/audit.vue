<template>
  <view class="wishlist-audit-container">
    <!-- 加载中 -->
    <view class="state-block" v-if="loading">
      <text class="state-text">加载中...</text>
    </view>

    <!-- 缺少 itemId / 缺少商家身份 / 接口失败：提示 + 重试 -->
    <view class="state-block" v-else-if="errorMessage">
      <text class="state-text">{{ errorMessage }}</text>
      <button class="retry-btn" @tap="loadDetail">重新加载</button>
    </view>

    <template v-else-if="detail">
      <!-- 用户信息卡片：userNickname / userAvatar 后端 VO 声明了但 service 未赋值，
           取不到就不渲染头像，昵称缺失时退回展示真实的用户 ID -->
      <view class="user-card">
        <image
          v-if="detail.userAvatar"
          class="user-avatar"
          :src="detail.userAvatar"
          mode="aspectFill"
        ></image>
        <view class="user-info">
          <text class="user-name">{{ displayUserName }}</text>
          <text class="submit-time">{{ formatRelativeTime(detail.createdAt) }}</text>
        </view>
        <view class="status-badge">{{ detail.auditStatusName }}</view>
      </view>

      <!-- 需求内容 -->
      <view class="wish-detail-section">
        <view class="section-title">需求内容</view>

        <view class="dish-row">
          <image
            v-if="detail.dishImage"
            class="dish-image"
            :src="detail.dishImage"
            mode="aspectFill"
          ></image>
          <view class="dish-main">
            <text class="dish-name">{{ detail.dishName }}</text>
            <text class="dish-desc" v-if="detail.description">{{ detail.description }}</text>
          </view>
        </view>

        <view class="detail-item" v-if="detail.tasteRequirement">
          <text class="item-label">口味要求</text>
          <text class="item-value">{{ detail.tasteRequirement }}</text>
        </view>

        <view class="detail-item" v-if="detail.recipeId">
          <text class="item-label">参考食谱</text>
          <text class="item-value">{{ detail.recipeName || detail.recipeId }}</text>
        </view>

        <view class="detail-item" v-if="detail.expectedAvailableTime">
          <text class="item-label">期望上架时间</text>
          <text class="item-value">{{ formatDateTime(detail.expectedAvailableTime) }}</text>
        </view>

        <view class="detail-item" v-if="hasRemainingHours">
          <text class="item-label">剩余审核时间</text>
          <text class="item-value">{{ detail.remainingHours }} 小时</text>
        </view>
      </view>

      <!-- 审核信息：审核后才会有值 -->
      <view class="wish-detail-section" v-if="isAudited">
        <view class="section-title">审核信息</view>

        <view class="detail-item">
          <text class="item-label">审核结果</text>
          <text class="item-value">{{ detail.auditStatusName }}</text>
        </view>

        <view class="detail-item" v-if="detail.auditorName">
          <text class="item-label">审核人</text>
          <text class="item-value">{{ detail.auditorName }}</text>
        </view>

        <view class="detail-item" v-if="detail.auditTime">
          <text class="item-label">审核时间</text>
          <text class="item-value">{{ formatDateTime(detail.auditTime) }}</text>
        </view>

        <view class="detail-item" v-if="detail.rejectionReasonTitle">
          <text class="item-label">拒绝原因</text>
          <text class="item-value">
            {{ detail.rejectionReasonTitle }}
            <text v-if="detail.rejectionReasonDescription">
              （{{ detail.rejectionReasonDescription }}）
            </text>
          </text>
        </view>

        <view class="detail-item" v-if="detail.auditRemark">
          <text class="item-label">审核备注</text>
          <text class="item-value">{{ detail.auditRemark }}</text>
        </view>

        <view class="detail-item" v-if="detail.actualAvailableTime">
          <text class="item-label">实际上架时间</text>
          <text class="item-value">{{ detail.actualAvailableTime }}</text>
        </view>
      </view>

      <!-- 申诉信息 -->
      <view class="wish-detail-section" v-if="detail.isAppealed">
        <view class="section-title">申诉信息</view>

        <view class="detail-item" v-if="detail.appealContent">
          <text class="item-label">申诉内容</text>
          <text class="item-value">{{ detail.appealContent }}</text>
        </view>

        <view class="detail-item" v-if="detail.appealTime">
          <text class="item-label">申诉时间</text>
          <text class="item-value">{{ formatDateTime(detail.appealTime) }}</text>
        </view>

        <view class="detail-item" v-if="detail.appealReply">
          <text class="item-label">申诉回复</text>
          <text class="item-value">{{ detail.appealReply }}</text>
        </view>
      </view>

      <!-- 操作按钮：只在待审核时出现 -->
      <view class="action-buttons" v-if="detail.auditStatus === 0">
        <button class="action-btn reject" :disabled="submitting" @tap="rejectWish">
          无法满足
        </button>
        <button class="action-btn accept" :disabled="submitting" @tap="acceptWish">
          受理需求
        </button>
      </view>
    </template>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { wishApi, WISH_REJECTION_REASONS, OTHER_REJECTION_CODE } from '@/api/modules/wish.js'
import { useMerchantStore } from '@/store/modules/merchant'
import { formatRelativeTime, formatDateTime, showConfirm } from '@/utils/helper'

const merchantStore = useMerchantStore()

const itemId = ref('')
const detail = ref(null)
const loading = ref(true)
const submitting = ref(false)
const errorMessage = ref('')

/** 昵称缺失时退回真实的用户 ID，不编造姓名 */
const displayUserName = computed(() =>
  detail.value ? detail.value.userNickname || detail.value.userId || '' : ''
)

/** auditStatus: 0 待审核，其余都算已处理（枚举见后端 WishListAuditStatus） */
const isAudited = computed(() => !!detail.value && detail.value.auditStatus !== 0)

/** remainingHours 只在待审核且后端算得出来时才有值 */
const hasRemainingHours = computed(
  () =>
    !!detail.value &&
    detail.value.remainingHours !== null &&
    detail.value.remainingHours !== undefined
)

onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = (currentPage && currentPage.options) || {}
  itemId.value = options.id || ''
  loadDetail()
})

/**
 * 拉取需求详情
 * GET /v1/wish-list/item/{itemId}
 */
const loadDetail = async () => {
  if (!itemId.value) {
    loading.value = false
    errorMessage.value = '缺少需求ID，无法加载详情'
    return
  }

  if (!merchantStore.resolveMerchantId()) {
    loading.value = false
    errorMessage.value = '未获取到商家身份，请重新登录商家账号后再试'
    return
  }

  loading.value = true
  errorMessage.value = ''
  try {
    const res = await wishApi.getDetail(itemId.value)
    detail.value = res.data || null
    if (!detail.value) {
      errorMessage.value = '未查询到该需求'
    }
  } catch (error) {
    console.error('加载需求详情失败:', error)
    detail.value = null
    errorMessage.value = error.message || '需求详情加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
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
 * 成功后回读详情，用后端返回的真实状态刷新页面
 */
const acceptWish = async () => {
  const confirmed = await showConfirm('受理后该需求状态变为已通过，确认受理吗？', '受理需求')
  if (!confirmed) return

  submitting.value = true
  uni.showLoading({ title: '处理中...' })
  try {
    const res = await wishApi.approve(itemId.value)
    if (res.data !== true) {
      throw new Error('受理失败')
    }
    uni.showToast({ title: '已受理', icon: 'success' })
    await loadDetail()
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
 * 成功后回读详情，失败不提示成功
 */
const rejectWish = async () => {
  const reason = await pickRejectionReason()
  if (!reason) return

  submitting.value = true
  uni.showLoading({ title: '处理中...' })
  try {
    const res = await wishApi.reject(itemId.value, {
      rejectionReasonCode: reason.code,
      rejectionReason: reason.remark || reason.title,
      auditRemark: reason.remark || ''
    })
    if (res.data !== true) {
      throw new Error('拒绝失败')
    }
    uni.showToast({ title: '已拒绝', icon: 'success' })
    await loadDetail()
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

.wishlist-audit-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 加载 / 错误态 */
.state-block {
  margin: 100rpx 40rpx 0;
  padding: 60rpx 30rpx;
  background: #fff;
  border-radius: 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30rpx;
}

.state-text {
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

/* 用户卡片 */
.user-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 40rpx 30rpx;
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.user-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
}

.submit-time {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.status-badge {
  padding: 8rpx 20rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.9);
  color: #FAAD14;
}

/* 详情区块 */
.wish-detail-section {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.dish-row {
  display: flex;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.dish-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.dish-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.dish-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.dish-desc {
  font-size: 26rpx;
  color: #666;
  line-height: 1.6;
}

.detail-item {
  display: flex;
  margin-bottom: 15rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 8rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.item-label {
  width: 180rpx;
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.item-value {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

/* 操作按钮 */
.action-buttons {
  padding: 20rpx;
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 90rpx;
  line-height: 90rpx;
  border-radius: 45rpx;
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
    background: #fff;
    color: #666;
    border: 2rpx solid #eee;
  }
}
</style>
