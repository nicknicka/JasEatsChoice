<template>
  <view class="order-process-container">
    <!-- 订单信息 -->
    <view class="order-card">
      <view class="card-header">
        <text class="order-no">订单号：{{ orderInfo.orderNo }}</text>
        <view class="order-status">{{ orderInfo.statusText }}</view>
      </view>
      <view class="dish-summary">
        <view class="dish-item" v-for="dish in orderInfo.dishes" :key="dish.id">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="dish-quantity">x{{ dish.quantity }}</text>
        </view>
      </view>
    </view>

    <!-- 制作进度 -->
    <view class="process-section">
      <view class="section-title">制作进度</view>
      <view class="progress-list">
        <view
          class="progress-item"
          v-for="(step, index) in processSteps"
          :key="index"
          :class="{ active: currentStep === index, completed: currentStep > index }"
          @tap="selectStep(index)"
        >
          <view class="step-icon">
            <uni-icons
              :type="currentStep > index ? 'checkmarkempty' : 'radio-button-off'"
              size="24"
              :color="currentStep >= index ? '#FF6B35' : '#D9D9D9'"
            ></uni-icons>
          </view>
          <view class="step-content">
            <text class="step-name">{{ step.name }}</text>
            <text class="step-time" v-if="step.time">{{ step.time }}</text>
          </view>
          <view class="step-check" v-if="currentStep === index">
            <uni-icons type="checkmarkempty" size="20" color="#fff"></uni-icons>
          </view>
        </view>
      </view>
    </view>

    <!-- 预计时间 -->
    <view class="time-section">
      <view class="section-title">预计完成时间</view>
      <view class="time-picker">
        <picker mode="time" :value="expectTime" @change="onTimeChange">
          <view class="time-display">
            <text class="time-value">{{ expectTime }}</text>
            <uni-icons type="arrowright" size="16" color="#999"></uni-icons>
          </view>
        </picker>
      </view>
      <view class="time-tips">
        <uni-icons type="info" size="16" color="#999"></uni-icons>
        <text>当前预计 {{ expectTime }} 完成</text>
      </view>
    </view>

    <!-- 进度备注 -->
    <view class="remark-section">
      <view class="section-title">进度备注</view>
      <textarea
        class="remark-input"
        v-model="processRemark"
        placeholder="请输入当前进度备注（可选）"
        maxlength="200"
      ></textarea>
      <view class="remark-count">{{ processRemark.length }}/200</view>
    </view>

    <!-- 历史进度 -->
    <view class="history-section" v-if="progressHistory.length > 0">
      <view class="section-title">进度历史</view>
      <view class="history-list">
        <view class="history-item" v-for="(item, index) in progressHistory" :key="index">
          <view class="history-dot"></view>
          <view class="history-content">
            <view class="history-header">
              <text class="history-step">{{ item.step }}</text>
              <text class="history-time">{{ item.time }}</text>
            </view>
            <text class="history-remark" v-if="item.remark">{{ item.remark }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <button class="action-btn primary" @tap="submitProgress">
        更新进度
      </button>
      <button class="action-btn" @tap="callCustomer">
        联系顾客
      </button>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { merchantApi } from '@/api'
import { useMerchantStore } from '@/stores/merchant'

const merchantStore = useMerchantStore()

// 订单ID
const orderId = ref('')

// 订单信息
const orderInfo = ref({
  id: 1,
  orderNo: 'OD202603180001',
  statusText: '制作中',
  dishes: [
    { id: 1, name: '宫保鸡丁', quantity: 1 },
    { id: 2, name: '鱼香肉丝', quantity: 2 }
  ]
})

// 制作步骤
const processSteps = ref([
  { name: '开始制作', time: '' },
  { name: '食材准备中', time: '' },
  { name: '烹饪中', time: '' },
  { name: '摆盘装盒', time: '' },
  { name: '已完成', time: '' }
])

const currentStep = ref(0)
const expectTime = ref('13:00')
const processRemark = ref('')

// 进度历史
const progressHistory = ref([
  {
    step: '开始制作',
    time: '12:32',
    remark: '已确认订单，开始准备食材'
  },
  {
    step: '食材准备中',
    time: '12:35',
    remark: ''
  }
])

onMounted(() => {
  // 从页面参数获取订单ID
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options

  if (options.id) {
    orderId.value = options.id
    loadOrderProcess()
  }
})

/**
 * 加载订单进度
 */
const loadOrderProcess = async () => {
  try {
    // 调用API获取订单进度
    const res = await merchantApi.getOrderProgress(orderId.value)

    if (res && res.data) {
      const data = res.data

      // 更新订单信息
      if (data.order) {
        orderInfo.value = {
          id: data.order.orderId || data.order.id,
          orderNo: data.order.orderNo || data.order.order_no,
          statusText: getStatusText(data.order.status),
          dishes: Array.isArray(data.order.dishes) ? data.order.dishes.map(dish => ({
            id: dish.dishId || dish.id,
            name: dish.dishName || dish.name,
            quantity: dish.quantity || 1
          })) : []
        }
      }

      // 更新当前步骤
      currentStep.value = data.currentStep || 0

      // 更新预计完成时间
      if (data.expectTime) {
        expectTime.value = formatTime(data.expectTime)
      }

      // 更新进度历史
      if (Array.isArray(data.history)) {
        progressHistory.value = data.history.map(item => ({
          step: item.step || item.stepName,
          time: formatTime(item.time || item.created_at),
          remark: item.remark || ''
        }))
      }

      // 更新制作步骤
      if (Array.isArray(data.steps)) {
        processSteps.value = data.steps.map(step => ({
          name: step.name || step.stepName,
          time: step.time ? formatTime(step.time) : ''
        }))
      }
    }
  } catch (error) {
    console.error('加载订单进度失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  if (typeof time === 'string' && time.includes(':')) {
    return time.split(':').slice(0, 2).join(':')
  }
  const date = new Date(time)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    pending: '待接单',
    cooking: '制作中',
    ready: '待取餐',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

/**
 * 选择步骤
 */
const selectStep = (index) => {
  if (index <= currentStep.value + 1) {
    currentStep.value = index
  }
}

/**
 * 时间变更
 */
const onTimeChange = (e) => {
  expectTime.value = e.detail.value
}

/**
 * 提交进度
 */
const submitProgress = async () => {
  const stepName = processSteps.value[currentStep.value].name

  uni.showModal({
    title: '确认更新',
    content: `确认更新进度为"${stepName}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const merchantId = merchantStore.merchantInfo?.merchantId || merchantStore.merchantInfo?.id

          // 调用API更新进度
          await merchantApi.updateOrderProgress(orderId.value, {
            merchantId,
            step: currentStep.value,
            stepName: stepName,
            expectTime: expectTime.value,
            remark: processRemark.value
          })

          uni.showToast({
            title: '更新成功',
            icon: 'success'
          })

          // 添加到历史记录
          progressHistory.value.unshift({
            step: stepName,
            time: getCurrentTime(),
            remark: processRemark.value
          })

          // 延迟返回
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error) {
          console.error('更新进度失败:', error)
          uni.showToast({
            title: '更新失败，请重试',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 联系顾客
 */
const callCustomer = () => {
  uni.showActionSheet({
    itemList: ['拨打电话', '发送消息'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.makePhoneCall({
          phoneNumber: '138****8888'
        })
      } else {
        uni.showToast({
          title: '跳转聊天',
          icon: 'none'
        })
      }
    }
  })
}

/**
 * 获取当前时间
 */
const getCurrentTime = () => {
  const now = new Date()
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.order-process-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 40rpx;
}

/* 订单卡片 */
.order-card {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.order-no {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.order-status {
  padding: 8rpx 20rpx;
  background: #E6F7FF;
  color: #1890FF;
  font-size: 24rpx;
  border-radius: 20rpx;
  font-weight: bold;
}

.dish-summary {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.dish-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
}

.dish-name {
  color: #333;
}

.dish-quantity {
  color: #FF6B35;
  font-weight: bold;
}

/* 通用section */
.process-section,
.time-section,
.remark-section,
.history-section {
  background: #fff;
  margin: 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

/* 制作进度 */
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 25rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  position: relative;

  &.active {
    background: rgba(255, 107, 53, 0.1);
    border: 2rpx solid #FF6B35;
  }

  &.completed {
    opacity: 0.6;
  }
}

.step-icon {
  width: 40rpx;
  height: 40rpx;
  @include flex-center;
  flex-shrink: 0;
}

.step-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.step-name {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;

  .progress-item.active & {
    color: #FF6B35;
  }

  .progress-item.completed & {
    color: #52C41A;
  }
}

.step-time {
  font-size: 24rpx;
  color: #999;
}

.step-check {
  width: 40rpx;
  height: 40rpx;
  background: #FF6B35;
  border-radius: 50%;
  @include flex-center;
  flex-shrink: 0;
}

/* 预计时间 */
.time-picker {
  margin-bottom: 20rpx;
}

.time-display {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25rpx 30rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.time-value {
  font-size: 32rpx;
  font-weight: bold;
  color: #FF6B35;
}

.time-tips {
  display: flex;
  align-items: center;
  gap: 10rpx;
  font-size: 26rpx;
  color: #999;
}

/* 进度备注 */
.remark-input {
  width: 100%;
  min-height: 150rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.remark-count {
  text-align: right;
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

/* 历史记录 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 25rpx;
}

.history-item {
  display: flex;
  gap: 20rpx;
  position: relative;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    left: 11rpx;
    top: 24rpx;
    width: 2rpx;
    height: calc(100% + 25rpx);
    background: #E8E8E8;
  }
}

.history-dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #FF6B35;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.history-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-step {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.history-time {
  font-size: 24rpx;
  color: #999;
}

.history-remark {
  font-size: 26rpx;
  color: #666;
  line-height: 1.5;
}

/* 操作按钮 */
.action-buttons {
  padding: 40rpx 20rpx 0;
  display: flex;
  gap: 20rpx;
}

.action-btn {
  flex: 1;
  height: 90rpx;
  border-radius: 45rpx;
  font-size: 28rpx;
  background: #fff;
  color: #666;
  border: none;

  &.primary {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
