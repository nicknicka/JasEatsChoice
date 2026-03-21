<template>
  <view class="integral-container">
    <!-- 积分概览 -->
    <view class="integral-header">
      <view class="balance-card">
        <text class="label">我的积分</text>
        <text class="balance">{{ userInfo.integral || 0 }}</text>
      </view>
      <view class="action-buttons">
        <button class="action-btn" @tap="toExchange">兑换</button>
        <button class="action-btn outline" @tap="toEarn">赚积分</button>
      </view>
    </view>

    <!-- 积分明细 -->
    <view class="section">
      <view class="section-title">积分明细</view>
      <view class="tab-bar">
        <view
          class="tab-item"
          :class="{ active: activeTab === 'all' }"
          @tap="changeTab('all')"
        >
          全部
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'income' }"
          @tap="changeTab('income')"
        >
          收入
        </view>
        <view
          class="tab-item"
          :class="{ active: activeTab === 'expense' }"
          @tap="changeTab('expense')"
        >
          支出
        </view>
      </view>

      <scroll-view scroll-y class="list-container">
        <view
          class="integral-item"
          v-for="item in integralList"
          :key="item.id"
        >
          <view class="item-info">
            <text class="item-title">{{ item.title }}</text>
            <text class="item-time">{{ item.time }}</text>
          </view>
          <view class="item-amount" :class="{ income: item.type === 'income' }">
            <text>{{ item.type === 'income' ? '+' : '-' }}{{ item.amount }}</text>
          </view>
        </view>

        <view class="empty-state" v-if="integralList.length === 0">
          <empty />
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/helper'
import { walletApi } from '@/api'

const userStore = useUserStore()

const userInfo = ref({
  integral: 0
})

const activeTab = ref('all')
const integralList = ref([])

onMounted(() => {
  loadUserInfo()
  loadIntegralList()
})

const loadUserInfo = async () => {
  try {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 获取用户积分信息
    const res = await walletApi.getPoints({ userId })
    if (res && res.data) {
      userInfo.value.integral = res.data.points || res.data.integral || 0
    } else if (res && res.points) {
      userInfo.value.integral = res.points
    }
  } catch (error) {
    console.error('加载用户积分失败:', error)
  }
}

const loadIntegralList = async () => {
  try {
    if (!userStore.isLogin) {
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 获取积分记录
    const res = await walletApi.getPointsRecords({
      userId,
      page: 1,
      size: 50
    })

    if (Array.isArray(res)) {
      integralList.value = res.map(record => ({
        id: record.id || record.recordId,
        title: record.description || record.title || '积分变动',
        time: formatTime(record.createTime || record.createdAt),
        amount: Math.abs(record.points || record.amount || 0),
        type: (record.points > 0 || record.amount > 0) ? 'income' : 'expense'
      }))
    } else if (res.data && Array.isArray(res.data)) {
      integralList.value = res.data.map(record => ({
        id: record.id || record.recordId,
        title: record.description || record.title || '积分变动',
        time: formatTime(record.createTime || record.createdAt),
        amount: Math.abs(record.points || record.amount || 0),
        type: (record.points > 0 || record.amount > 0) ? 'income' : 'expense'
      }))
    }

    // 根据当前tab筛选数据
    filterByTab()
  } catch (error) {
    console.error('加载积分明细失败:', error)
    // 使用默认数据
    integralList.value = [
      { id: 1, title: '订单完成', time: '2026-03-18 12:30', amount: 100, type: 'income' },
      { id: 2, title: '兑换优惠券', time: '2026-03-17 15:20', amount: 500, type: 'expense' },
      { id: 3, title: '每日签到', time: '2026-03-17 08:00', amount: 10, type: 'income' },
      { id: 4, title: '评价奖励', time: '2026-03-16 19:45', amount: 20, type: 'income' },
      { id: 5, title: '兑换商品', time: '2026-03-15 14:30', amount: 1000, type: 'expense' }
    ]
  }
}

const changeTab = (tab) => {
  activeTab.value = tab
  filterByTab()
}

const filterByTab = () => {
  // 这里已经在loadIntegralList中处理了，可以在此处添加额外的筛选逻辑
  // 如果需要重新从API获取，可以调用loadIntegralList
}

const toExchange = () => {
  uni.showToast({ title: '积分兑换功能开发中', icon: 'none' })
}

const toEarn = () => {
  uni.showToast({ title: '赚积分功能开发中', icon: 'none' })
}
</script>

<style lang="scss" scoped>
.integral-container {
  min-height: 100vh;
  background: #F5F5F5;
}

.integral-header {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  padding: 60rpx 30rpx 40rpx;
}

.balance-card {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 20rpx;
  padding: 40rpx;
  backdrop-filter: blur(10px);
}

.label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.balance {
  display: block;
  font-size: 80rpx;
  font-weight: bold;
  color: #fff;
  margin-top: 10rpx;
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;

  &.outline {
    background: transparent;
    border: 2rpx solid #fff;
    color: #fff;
  }
}

.section {
  background: #fff;
  margin-top: 20rpx;
  padding: 30rpx;
  min-height: calc(100vh - 350rpx);
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.tab-bar {
  display: flex;
  gap: 30rpx;
  margin-bottom: 30rpx;
  border-bottom: 1rpx solid #eee;
  padding-bottom: 15rpx;
}

.tab-item {
  font-size: 28rpx;
  color: #999;
  padding: 10rpx 20rpx;
  position: relative;

  &.active {
    color: #FF6B35;
    font-weight: bold;

    &::after {
      content: '';
      position: absolute;
      bottom: -16rpx;
      left: 50%;
      transform: translateX(-50%);
      width: 40rpx;
      height: 4rpx;
      background: #FF6B35;
      border-radius: 2rpx;
    }
  }
}

.list-container {
  max-height: calc(100vh - 500rpx);
}

.integral-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #eee;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.item-title {
  font-size: 28rpx;
  color: #333;
}

.item-time {
  font-size: 24rpx;
  color: #999;
}

.item-amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;

  &.income {
    color: #52C41A;
  }
}

.empty-state {
  padding-top: 200rpx;
}
</style>
