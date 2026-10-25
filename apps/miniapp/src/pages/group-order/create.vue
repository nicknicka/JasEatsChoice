<template>
  <view class="create-group-order-container">
    <!-- 步骤指示器 -->
    <view class="steps-indicator">
      <view class="step-item" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
        <view class="step-number">1</view>
        <text class="step-label">选择商家</text>
      </view>
      <view class="step-line" :class="{ active: currentStep > 1 }"></view>
      <view class="step-item" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
        <view class="step-number">2</view>
        <text class="step-label">设置信息</text>
      </view>
      <view class="step-line" :class="{ active: currentStep > 2 }"></view>
      <view class="step-item" :class="{ active: currentStep >= 3 }">
        <view class="step-number">3</view>
        <text class="step-label">邀请好友</text>
      </view>
    </view>

    <!-- 步骤内容 -->
    <swiper
      class="steps-swiper"
      :current="currentStep - 1"
      @change="onSwiperChange"
    >
      <!-- 步骤1：选择商家 -->
      <swiper-item>
        <scroll-view class="step-content" scroll-y>
          <!-- 搜索框 -->
          <view class="search-section">
            <uni-search-bar
              placeholder="搜索商家或菜品"
              :focus="false"
              @confirm="onSearch"
            />
          </view>

          <!-- 推荐商家 -->
          <view class="recommend-section">
            <view class="section-title">推荐商家</view>
            <view class="merchant-list">
              <view
                class="merchant-card"
                v-for="merchant in recommendMerchants"
                :key="merchant.id"
                @tap="selectMerchant(merchant)"
              >
                <image class="merchant-image" :src="merchant.image" mode="aspectFill"></image>
                <view class="merchant-info">
                  <text class="merchant-name">{{ merchant.name }}</text>
                  <view class="merchant-meta">
                    <uni-rate :value="merchant.rating" size="12" readonly></uni-rate>
                    <text class="rating-text">{{ merchant.rating }}分</text>
                    <text class="sales-text">月售{{ merchant.sales }}</text>
                  </view>
                  <view class="merchant-tags">
                    <text class="tag" v-for="tag in merchant.tags" :key="tag">{{ tag }}</text>
                  </view>
                </view>
                <view class="select-icon" v-if="selectedMerchant && selectedMerchant.id === merchant.id">
                  <uni-icons type="checkbox-filled" size="24" color="#FF6B35"></uni-icons>
                </view>
              </view>
            </view>
          </view>

          <!-- 最近光顾 -->
          <view class="recent-section" v-if="recentMerchants.length > 0">
            <view class="section-title">最近光顾</view>
            <scroll-view scroll-x class="recent-scroll">
              <view
                class="recent-item"
                v-for="merchant in recentMerchants"
                :key="merchant.id"
                @tap="selectMerchant(merchant)"
              >
                <image class="recent-image" :src="merchant.image" mode="aspectFill"></image>
                <text class="recent-name">{{ merchant.name }}</text>
              </view>
            </scroll-view>
          </view>
        </scroll-view>
      </swiper-item>

      <!-- 步骤2：设置信息 -->
      <swiper-item>
        <scroll-view class="step-content" scroll-y>
          <view class="form-section">
            <view class="section-title">基本信息</view>
            <view class="form-item">
              <text class="item-label">订单名称</text>
              <input
                class="item-input"
                v-model="orderForm.name"
                placeholder="给群订单起个名字吧"
                maxlength="30"
              />
            </view>
            <view class="form-item">
              <text class="item-label">用餐时间</text>
              <picker
                mode="date"
                :value="orderForm.date"
                :start="minDate"
                @change="onDateChange"
              >
                <view class="picker-value">
                  {{ orderForm.date || '选择日期' }}
                  <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="item-label">用餐时段</text>
              <picker
                mode="time"
                :value="orderForm.time"
                @change="onTimeChange"
              >
                <view class="picker-value">
                  {{ orderForm.time || '选择时间' }}
                  <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="item-label">人数限制</text>
              <view class="counter-wrapper">
                <button class="counter-btn" @tap="adjustCount(-1)">-</button>
                <text class="counter-value">{{ orderForm.maxPeople }}人</text>
                <button class="counter-btn" @tap="adjustCount(1)">+</button>
              </view>
            </view>
          </view>

          <view class="form-section">
            <view class="section-title">订单设置</view>
            <view class="setting-item">
              <text class="setting-label">设置截止时间</text>
              <switch
                :checked="orderForm.hasDeadline"
                color="#FF6B35"
                @change="toggleDeadline"
              />
            </view>
            <view class="setting-desc" v-if="orderForm.hasDeadline">
              <text class="desc-text">订单将在截止时间后自动结算</text>
            </view>
            <view class="setting-item" v-if="orderForm.hasDeadline">
              <text class="setting-label">截止时长</text>
              <picker
                :value="deadlineIndex"
                :range="deadlineOptions"
                range-key="label"
                @change="onDeadlineChange"
              >
                <view class="picker-value">
                  {{ deadlineOptions[deadlineIndex].label }}
                  <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </view>

          <view class="form-section">
            <view class="section-title">留言备注</view>
            <textarea
              class="remark-input"
              v-model="orderForm.remark"
              placeholder="添加留言，让大家了解订单详情..."
              maxlength="200"
              :show-confirm-bar="false"
            />
            <view class="word-count">{{ orderForm.remark.length }}/200</view>
          </view>
        </scroll-view>
      </swiper-item>

      <!-- 步骤3：邀请好友 -->
      <swiper-item>
        <scroll-view class="step-content" scroll-y>
          <!-- 邀请方式 -->
          <view class="invite-section">
            <view class="section-title">邀请方式</view>
            <view class="invite-methods">
              <view class="method-item" @tap="inviteByQRCode">
                <view class="method-icon">
                  <uni-icons type="qrcode" size="32" color="#FF6B35"></uni-icons>
                </view>
                <text class="method-label">二维码邀请</text>
                <text class="method-desc">生成二维码分享给好友</text>
              </view>
              <view class="method-item" @tap="inviteByLink">
                <view class="method-icon">
                  <uni-icons type="link" size="32" color="#1890FF"></uni-icons>
                </view>
                <text class="method-label">链接邀请</text>
                <text class="method-desc">复制链接发送给好友</text>
              </view>
              <view class="method-item" @tap="inviteByCode">
                <view class="method-icon">
                  <uni-icons type="key" size="32" color="#52C41A"></uni-icons>
                </view>
                <text class="method-label">订单码邀请</text>
                <text class="method-desc">分享6位订单码</text>
              </view>
            </view>
          </view>

          <!-- 已邀请好友 -->
          <view class="invited-section" v-if="invitedUsers.length > 0">
            <view class="section-title">
              已邀请好友 ({{ invitedUsers.length }})
            </view>
            <view class="invited-list">
              <view
                class="invited-item"
                v-for="user in invitedUsers"
                :key="user.id"
              >
                <image class="user-avatar" :src="user.avatar" mode="aspectFill"></image>
                <text class="user-name">{{ user.name }}</text>
                <view class="user-status" :class="'status-' + user.status">
                  {{ user.statusText }}
                </view>
              </view>
            </view>
          </view>

          <!-- 温馨提示 -->
          <view class="tips-card">
            <uni-icons type="info" size="16" color="#FF6B35"></uni-icons>
            <text class="tips-text">创建后，您可以在订单管理中查看订单详情和进度</text>
          </view>
        </scroll-view>
      </swiper-item>
    </swiper>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <button class="nav-btn" @tap="prevStep" v-if="currentStep > 1">
        上一步
      </button>
      <button
        class="nav-btn primary"
        :disabled="!canNext"
        @tap="nextStep"
      >
        {{ currentStep === 3 ? '创建订单' : '下一步' }}
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

// 当前步骤
const currentStep = ref(1)

// 选中的商家
const selectedMerchant = ref(null)

// 推荐商家
const recommendMerchants = ref([
  {
    id: 1,
    name: '老王家常菜',
    image: 'https://via.placeholder.com/200/FF6B35/FFFFFF?text=1',
    rating: 4.8,
    sales: '1000+',
    tags: ['川菜', '口碑好']
  },
  {
    id: 2,
    name: '李记私房菜',
    image: 'https://via.placeholder.com/200/52C41A/FFFFFF?text=2',
    rating: 4.6,
    sales: '800+',
    tags: ['湘菜', '环境好']
  },
  {
    id: 3,
    name: '川味轩',
    image: 'https://via.placeholder.com/200/1890FF/FFFFFF?text=3',
    rating: 4.7,
    sales: '900+',
    tags: ['川菜', '性价比高']
  }
])

// 最近光顾
const recentMerchants = ref([
  {
    id: 4,
    name: '老王家常菜',
    image: 'https://via.placeholder.com/120/FF6B35/FFFFFF?text=1'
  },
  {
    id: 5,
    name: '川味轩',
    image: 'https://via.placeholder.com/120/1890FF/FFFFFF?text=3'
  }
])

// 订单表单
const orderForm = ref({
  name: '',
  date: '',
  time: '',
  maxPeople: 10,
  hasDeadline: true,
  deadline: 30,
  remark: ''
})

// 最小日期
const minDate = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// 截止时间选项
const deadlineOptions = [
  { label: '30分钟', value: 30 },
  { label: '1小时', value: 60 },
  { label: '2小时', value: 120 },
  { label: '4小时', value: 240 }
]

const deadlineIndex = ref(0)

// 已邀请用户
const invitedUsers = ref([])

onMounted(() => {
  // 设置默认日期为今天
  orderForm.value.date = minDate.value
})

/**
 * 是否可以下一步
 */
const canNext = computed(() => {
  if (currentStep.value === 1) {
    return selectedMerchant.value !== null
  }
  if (currentStep.value === 2) {
    return orderForm.value.name && orderForm.value.date && orderForm.value.time
  }
  return true
})

/**
 * 选择商家
 */
const selectMerchant = (merchant) => {
  selectedMerchant.value = merchant
}

/**
 * 搜索
 */
const onSearch = (e) => {
  const keyword = e.value
  uni.showToast({
    title: '搜索功能开发中',
    icon: 'none'
  })
}

/**
 * 日期变化
 */
const onDateChange = (e) => {
  orderForm.value.date = e.detail.value
}

/**
 * 时间变化
 */
const onTimeChange = (e) => {
  orderForm.value.time = e.detail.value
}

/**
 * 调整人数
 */
const adjustCount = (delta) => {
  const newValue = orderForm.value.maxPeople + delta
  if (newValue >= 2 && newValue <= 50) {
    orderForm.value.maxPeople = newValue
  }
}

/**
 * 切换截止时间
 */
const toggleDeadline = (e) => {
  orderForm.value.hasDeadline = e.detail.value
}

/**
 * 截止时间变化
 */
const onDeadlineChange = (e) => {
  deadlineIndex.value = e.detail.value
  orderForm.value.deadline = deadlineOptions[e.detail.value].value
}

/**
 * Swiper变化
 */
const onSwiperChange = (e) => {
  currentStep.value = e.detail.current + 1
}

/**
 * 上一步
 */
const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

/**
 * 下一步
 */
const nextStep = () => {
  if (currentStep.value === 3) {
    createOrder()
  } else {
    currentStep.value++
  }
}

/**
 * 二维码邀请
 */
const inviteByQRCode = () => {
  uni.showToast({
    title: '生成二维码中...',
    icon: 'loading'
  })
  // TODO: 生成邀请二维码
}

/**
 * 链接邀请
 */
const inviteByLink = () => {
  const link = `https://example.com/group-order/join?code=ABC123`
  uni.setClipboardData({
    data: link,
    success: () => {
      uni.showToast({
        title: '链接已复制',
        icon: 'success'
      })
    }
  })
}

/**
 * 订单码邀请
 */
const inviteByCode = () => {
  uni.showModal({
    title: '订单码',
    content: 'ABC123',
    confirmText: '复制',
    success: (res) => {
      if (res.confirm) {
        uni.setClipboardData({
          data: 'ABC123',
          success: () => {
            uni.showToast({
              title: '已复制',
              icon: 'success'
            })
          }
        })
      }
    }
  })
}

/**
 * 创建订单
 */
const createOrder = () => {
  if (!selectedMerchant.value) {
    uni.showToast({
      title: '请选择商家',
      icon: 'none'
    })
    return
  }

  uni.showLoading({
    title: '创建中...'
  })

  // TODO: 调用API创建群订单
  const data = {
    merchantId: selectedMerchant.value.id,
    ...orderForm.value
  }

  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '创建成功',
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

.create-group-order-container {
  min-height: 100vh;
  background: #F5F5F5;
  display: flex;
  flex-direction: column;
}

/* 步骤指示器 */
.steps-indicator {
  background: #fff;
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.step-number {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  background: #E8E8E8;
  color: #999;
  font-size: 24rpx;
  font-weight: bold;
  @include flex-center;

  .step-item.active & {
    background: #FF6B35;
    color: #fff;
  }

  .step-item.completed & {
    background: #52C41A;
    color: #fff;
  }
}

.step-label {
  font-size: 24rpx;
  color: #999;

  .step-item.active & {
    color: #FF6B35;
    font-weight: bold;
  }
}

.step-line {
  flex: 1;
  height: 4rpx;
  background: #E8E8E8;
  margin: 0 10rpx 30rpx;

  &.active {
    background: #FF6B35;
  }
}

/* 步骤内容 */
.steps-swiper {
  flex: 1;
}

.step-content {
  height: 100%;
  padding: 20rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

/* 搜索 */
.search-section {
  background: #fff;
  padding: 20rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

/* 商家列表 */
.merchant-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.merchant-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx;
  display: flex;
  gap: 20rpx;
  position: relative;
}

.merchant-image {
  width: 150rpx;
  height: 150rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.merchant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.merchant-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.merchant-meta {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.rating-text,
.sales-text {
  font-size: 24rpx;
  color: #999;
}

.merchant-tags {
  display: flex;
  gap: 10rpx;
}

.tag {
  padding: 4rpx 12rpx;
  background: rgba(255, 107, 53, 0.1);
  color: #FF6B35;
  font-size: 22rpx;
  border-radius: 4rpx;
}

.select-icon {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
}

/* 最近光顾 */
.recent-section {
  margin-top: 30rpx;
}

.recent-scroll {
  white-space: nowrap;
}

.recent-item {
  display: inline-block;
  width: 150rpx;
  margin-right: 20rpx;
  text-align: center;
}

.recent-image {
  width: 150rpx;
  height: 150rpx;
  border-radius: 12rpx;
  margin-bottom: 10rpx;
}

.recent-name {
  font-size: 24rpx;
  color: #666;
  display: block;
}

/* 表单 */
.form-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;

  &:last-child {
    border-bottom: none;
  }
}

.item-label {
  font-size: 28rpx;
  color: #333;
  width: 180rpx;
  flex-shrink: 0;
}

.item-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  text-align: right;
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

.counter-wrapper {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.counter-btn {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
  background: #F5F5F5;
  color: #666;
  font-size: 32rpx;
  @include flex-center;
  border: none;
}

.counter-value {
  font-size: 28rpx;
  color: #333;
  min-width: 80rpx;
  text-align: center;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 0;
}

.setting-label {
  font-size: 28rpx;
  color: #333;
}

.setting-desc {
  padding-left: 20rpx;
  margin-bottom: 15rpx;
}

.desc-text {
  font-size: 24rpx;
  color: #999;
  line-height: 1.6;
}

.remark-input {
  width: 100%;
  min-height: 150rpx;
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

/* 邀请方式 */
.invite-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.invite-methods {
  display: flex;
  gap: 20rpx;
}

.method-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15rpx;
  padding: 30rpx 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.method-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: #fff;
  @include flex-center;
}

.method-label {
  font-size: 26rpx;
  color: #333;
  font-weight: 500;
}

.method-desc {
  font-size: 22rpx;
  color: #999;
  text-align: center;
}

/* 已邀请 */
.invited-section {
  background: #fff;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}

.invited-list {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.invited-item {
  display: flex;
  align-items: center;
  gap: 15rpx;
  padding: 15rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.user-avatar {
  width: 60rpx;
  height: 60rpx;
  border-radius: 50%;
}

.user-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.user-status {
  padding: 6rpx 16rpx;
  border-radius: 20rpx;
  font-size: 24rpx;

  &.status-joined {
    background: #F6FFED;
    color: #52C41A;
  }

  &.status-pending {
    background: #FFF7E6;
    color: #FAAD14;
  }
}

/* 提示卡片 */
.tips-card {
  background: #FFF7E6;
  padding: 20rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.tips-text {
  flex: 1;
  font-size: 26rpx;
  color: #FF6B35;
  line-height: 1.5;
}

/* 底部操作栏 */
.bottom-bar {
  background: #fff;
  padding: 20rpx;
  display: flex;
  gap: 20rpx;
  border-top: 1rpx solid #eee;
}

.nav-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 32rpx;
  border: none;
  @include flex-center;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }

  &:not(.primary) {
    background: #F5F5F5;
    color: #666;
  }

  &[disabled] {
    background: #E8E8E8;
    color: #999;
  }
}
</style>
