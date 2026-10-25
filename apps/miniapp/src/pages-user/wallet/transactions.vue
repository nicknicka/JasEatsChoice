<template>
  <view class="wallet-transactions-page">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="nav-content">
        <view class="nav-back" @click="handleGoBack">
          <uni-icons type="left" size="20" color="#FFFFFF" />
        </view>
        <text class="nav-title">交易明细</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <!-- 筛选栏 -->
    <view class="filter-bar">
      <scroll-view class="filter-scroll" scroll-x>
        <view
          class="filter-item"
          :class="{ active: selectedType === type.value }"
          v-for="type in transactionTypes"
          :key="type.value"
          @click="changeFilter(type.value)"
        >
          <text class="filter-text">{{ type.label }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 交易列表 -->
    <scroll-view
      class="scroll-container"
      scroll-y
      refresher-enabled
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
      @scrolltolower="onLoadMore"
    >
      <!-- 空状态 -->
      <view class="empty-state" v-if="transactions.length === 0 && !loading">
        <text class="empty-icon">💰</text>
        <text class="empty-text">暂无交易记录</text>
      </view>

      <!-- 交易列表 -->
      <view class="transaction-list" v-else>
        <!-- 按日期分组 -->
        <view
          class="date-group"
          v-for="(group, dateIndex) in groupedTransactions"
          :key="dateIndex"
        >
          <view class="date-header">
            <text class="date-text">{{ group.dateText }}</text>
            <text class="date-amount">支出: ¥{{ group.expense }} | 收入: ¥{{ group.income }}</text>
          </view>

          <view
            class="transaction-item"
            v-for="item in group.items"
            :key="item.id"
            @click="viewDetail(item)"
          >
            <!-- 左侧图标 -->
            <view class="transaction-icon" :class="item.type">
              <text class="icon-text">{{ getIcon(item.type) }}</text>
            </view>

            <!-- 中间内容 -->
            <view class="transaction-content">
              <view class="transaction-header">
                <text class="transaction-title">{{ item.title }}</text>
                <text
                  class="transaction-amount"
                  :class="item.direction === 'income' ? 'income' : 'expense'"
                >
                  {{ item.direction === 'income' ? '+' : '-' }}¥{{ item.amount }}
                </text>
              </view>

              <view class="transaction-info">
                <text class="transaction-time">{{ item.time }}</text>
                <text class="transaction-status" :class="'status-' + item.status">
                  {{ getStatusText(item.status) }}
                </text>
              </view>

              <view class="transaction-desc" v-if="item.description">
                {{ item.description }}
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="load-more" v-if="transactions.length > 0">
        <view class="load-text" v-if="loading">加载中...</view>
        <view class="load-text" v-else-if="!hasMore">没有更多了</view>
        <view class="load-text" v-else>上拉加载更多</view>
      </view>
    </scroll-view>

    <!-- 交易详情弹窗 -->
    <uni-popup ref="detailPopup" type="bottom">
      <view class="detail-popup">
        <view class="detail-header">
          <text class="detail-title">交易详情</text>
          <uni-icons type="close" size="20" color="#666" @click="closeDetail" />
        </view>

        <view class="detail-content" v-if="selectedTransaction">
          <!-- 金额 -->
          <view class="detail-amount-section">
            <text
              class="detail-amount"
              :class="selectedTransaction.direction === 'income' ? 'income' : 'expense'"
            >
              {{ selectedTransaction.direction === 'income' ? '+' : '-' }}¥{{ selectedTransaction.amount }}
            </text>
            <view
              class="detail-status"
              :class="'status-' + selectedTransaction.status"
            >
              {{ getStatusText(selectedTransaction.status) }}
            </view>
          </view>

          <!-- 交易信息 -->
          <view class="detail-info">
            <view class="detail-item">
              <text class="detail-label">交易类型</text>
              <text class="detail-value">{{ selectedTransaction.title }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">交易时间</text>
              <text class="detail-value">{{ selectedTransaction.fullTime }}</text>
            </view>
            <view class="detail-item">
              <text class="detail-label">交易单号</text>
              <view class="detail-value-wrapper">
                <text class="detail-value">{{ selectedTransaction.orderNo }}</text>
                <text class="copy-btn" @click="copyOrderNo(selectedTransaction.orderNo)">复制</text>
              </view>
            </view>
            <view class="detail-item" v-if="selectedTransaction.description">
              <text class="detail-label">备注说明</text>
              <text class="detail-value">{{ selectedTransaction.description }}</text>
            </view>
            <view class="detail-item" v-if="selectedTransaction.balanceAfter !== undefined">
              <text class="detail-label">交易后余额</text>
              <text class="detail-value">¥{{ selectedTransaction.balanceAfter }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="detail-actions">
            <button
              class="detail-btn secondary"
              v-if="selectedTransaction.status === 'pending'"
              @click="cancelTransaction"
            >
              取消交易
            </button>
            <button
              class="detail-btn secondary"
              @click="contactService"
            >
              联系客服
            </button>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store'
import { goBack, toCustomerService } from '@/utils/router'
import { walletApi } from '@/api'

const userStore = useUserStore()
const getCurrentUserId = () => {
  return userStore.userInfo?.userId || userStore.userInfo?.id || uni.getStorageSync('userId') || ''
}

// 交易类型筛选
const transactionTypes = ref([
  { label: '全部', value: 'all' },
  { label: '充值', value: 'recharge' },
  { label: '消费', value: 'consume' },
  { label: '提现', value: 'withdraw' },
  { label: '退款', value: 'refund' },
  { label: '奖励', value: 'reward' }
])

const selectedType = ref('all')

// 交易列表
const transactions = ref([])

// 加载状态
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)

// 分页参数
const page = ref(1)
const pageSize = ref(20)

// 选中的交易
const selectedTransaction = ref(null)
const detailPopup = ref(null)

// 获取交易图标
const getIcon = (type) => {
  const icons = {
    recharge: '💰',
    consume: '🛒',
    withdraw: '💸',
    refund: '↩️',
    reward: '🎁'
  }
  return icons[type] || '💳'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    success: '交易成功',
    pending: '处理中',
    failed: '交易失败',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知状态'
}

const normalizeTransactionType = (type) => {
  const validTypes = ['recharge', 'consume', 'withdraw', 'refund']
  if (!type || type === 'all') {
    return 'all'
  }
  return validTypes.includes(type) ? type : 'other'
}

const getTypeTitle = (type) => {
  const titles = {
    recharge: '账户充值',
    consume: '订单消费',
    withdraw: '余额提现',
    refund: '订单退款',
    other: '其他交易'
  }
  return titles[type] || '交易'
}

const normalizeTransactionItem = (record) => {
  const type = normalizeTransactionType(record?.type)
  const amount = Number(record?.amount || 0)
  const amountNumber = Number.isFinite(amount) ? amount : 0
  const normalizedType = type === 'other' ? record?.type || 'other' : type
  const createTime = record?.createTime || record?.time
  const direction = normalizedType === 'recharge' ? 'income' : 'expense'
  const parsedTime = createTime ? new Date(createTime).getTime() : NaN

  return {
    id: record?.id || `${normalizedType}-${Date.now()}-${Math.random()}`,
    type: normalizedType,
    title: getTypeTitle(normalizedType),
    amount: amountNumber,
    direction,
    status: record?.status === 'failed' ? 'failed' : 'success',
    rawTime: Number.isFinite(parsedTime) ? parsedTime : Date.now(),
    time: formatTime(createTime),
    fullTime: formatFullTime(createTime),
    orderNo: record?.orderNo || record?.id || '',
    description: record?.description || ''
  }
}

const resolveApiType = (uiType) => {
  if (uiType === 'income') {
    return 'recharge'
  }
  if (uiType === 'expense') {
    return 'consume'
  }
  return uiType
}

// 按日期分组交易
const groupedTransactions = computed(() => {
  const groups = {}

  transactions.value.forEach(item => {
    const date = new Date(item.rawTime)
    if (Number.isNaN(date.getTime())) {
      return
    }

    const dateKey = `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`

    if (!groups[dateKey]) {
      // 计算日期文本
      const today = new Date()
      const yesterday = new Date(today)
      yesterday.setDate(yesterday.getDate() - 1)

      let dateText = `${date.getMonth() + 1}月${date.getDate()}日`
      if (date.toDateString() === today.toDateString()) {
        dateText = '今天'
      } else if (date.toDateString() === yesterday.toDateString()) {
        dateText = '昨天'
      }

      groups[dateKey] = {
        dateText,
        income: '0.00',
        expense: '0.00',
        items: []
      }
    }

    groups[dateKey].items.push(item)

    // 累计收支
    const amount = parseFloat(item.amount)
    if (item.direction === 'income') {
      groups[dateKey].income = (parseFloat(groups[dateKey].income) + amount).toFixed(2)
    } else {
      groups[dateKey].expense = (parseFloat(groups[dateKey].expense) + amount).toFixed(2)
    }
  })

  return Object.values(groups)
})

// 切换筛选
const changeFilter = (value) => {
  selectedType.value = value
  page.value = 1
  transactions.value = []
  loadTransactions()
}

// 加载交易列表
const loadTransactions = async (showLoading = true) => {
  if (!userStore.isLogin) {
    return
  }

  if (showLoading) {
    loading.value = true
  }

  try {
    const userId = getCurrentUserId()
    const params = {
      userId,
      page: page.value,
      size: pageSize.value
    }

    const apiType = resolveApiType(selectedType.value)
    if (apiType !== 'all') {
      params.type = apiType
    }

    const res = await walletApi.getTransactions(params)
    const rawList = res.list || res.data?.list || []
    const list = rawList.map((item) => normalizeTransactionItem(item))

    if (page.value === 1) {
      transactions.value = list
    } else {
      transactions.value.push(...list)
    }

    hasMore.value = list.length >= pageSize.value
  } catch (error) {
    console.error('加载交易记录失败:', error)
    uni.showToast({
      title: '加载失败，请重试',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 格式化时间
const formatTime = (date) => {
  if (!date) {
    return ''
  }

  const now = new Date()
  const target = new Date(date)
  if (Number.isNaN(target.getTime())) {
    return ''
  }
  const diff = now - target

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  const month = target.getMonth() + 1
  const day = target.getDate()
  const hour = target.getHours().toString().padStart(2, '0')
  const minute = target.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

// 格式化完整时间
const formatFullTime = (date) => {
  if (!date) {
    return ''
  }

  const target = new Date(date)
  if (Number.isNaN(target.getTime())) {
    return ''
  }
  const year = target.getFullYear()
  const month = (target.getMonth() + 1).toString().padStart(2, '0')
  const day = target.getDate().toString().padStart(2, '0')
  const hour = target.getHours().toString().padStart(2, '0')
  const minute = target.getMinutes().toString().padStart(2, '0')
  const second = target.getSeconds().toString().padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

// 下拉刷新
const onRefresh = async () => {
  refreshing.value = true
  page.value = 1
  await loadTransactions(false)
  refreshing.value = false
}

// 上拉加载更多
const onLoadMore = () => {
  if (loading.value || !hasMore.value) return
  page.value++
  loadTransactions()
}

// 查看详情
const viewDetail = (item) => {
  selectedTransaction.value = item
  detailPopup.value?.open()
}

// 关闭详情
const closeDetail = () => {
  detailPopup.value?.close()
}

// 复制订单号
const copyOrderNo = (orderNo) => {
  uni.setClipboardData({
    data: orderNo,
    success: () => {
      uni.showToast({
        title: '已复制',
        icon: 'success'
      })
    }
  })
}

// 取消交易
const cancelTransaction = () => {
  uni.showModal({
    title: '取消交易',
    content: '确定要取消这笔交易吗？',
    confirmColor: '#FF6B00',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '处理中...' })
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({
            title: '已取消',
            icon: 'success'
          })
          closeDetail()
          onRefresh()
        }, 1000)
      }
    }
  })
}

// 联系客服
const contactService = () => {
  closeDetail()
  toCustomerService()
}

// 返回上一页
const handleGoBack = () => {
  goBack()
}

onMounted(() => {
  loadTransactions()
})
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.wallet-transactions-page {
  @include flex-column;
  height: 100vh;
  background-color: $bg-color-base;
}

.navbar {
  @include navbar;
  background: linear-gradient(135deg, #FF6B00 0%, #FF8F00 100%);

  .nav-content {
    @include flex-row;
    align-items: center;
    justify-content: space-between;
    height: 100%;
    padding: 0 $spacing-md;

    .nav-back {
      width: 32px;
      height: 32px;
      @include flex-center;
      border-radius: 50%;
      transition: all 0.3s;

      &:active {
        background-color: rgba(255, 255, 255, 0.2);
      }
    }

    .nav-title {
      font-size: 18px;
      font-weight: bold;
      color: #FFFFFF;
    }

    .nav-placeholder {
      width: 32px;
    }
  }
}

.filter-bar {
  background-color: #FFFFFF;
  border-bottom: 1px solid $border-color;

  .filter-scroll {
    @include flex-center;
    white-space: nowrap;
    padding: $spacing-md;
  }

  .filter-item {
    flex-shrink: 0;
    padding: 6px 16px;
    margin-right: 12px;
    background-color: $bg-color-hover;
    border-radius: 20px;
    transition: all 0.3s;

    &.active {
      background-color: $primary-color;

      .filter-text {
        color: #FFFFFF;
        font-weight: bold;
      }
    }

    &:active {
      transform: scale(0.95);
    }
  }

  .filter-text {
    font-size: 14px;
    color: $text-color-primary;
  }
}

.scroll-container {
  flex: 1;
  overflow-y: auto;
}

.empty-state {
  @include flex-center-column;
  padding: 120px 0;

  .empty-icon {
    font-size: 64px;
    margin-bottom: 16px;
    opacity: 0.5;
  }

  .empty-text {
    font-size: 16px;
    color: $text-color-primary;
  }
}

.transaction-list {
  padding: $spacing-md;
}

.date-group {
  margin-bottom: $spacing-md;

  .date-header {
    @include flex-row;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    margin-bottom: 8px;

    .date-text {
      font-size: 13px;
      font-weight: bold;
      color: $text-color-primary;
    }

    .date-amount {
      font-size: 11px;
      color: $text-color-secondary;
    }
  }
}

.transaction-item {
  @include flex-row;
  background-color: #FFFFFF;
  border-radius: 12px;
  padding: 12px;
  margin-bottom: 8px;
  transition: all 0.3s;

  &:active {
    background-color: $bg-color-hover;
  }
}

.transaction-icon {
  width: 40px;
  height: 40px;
  @include flex-center;
  border-radius: 12px;
  margin-right: 12px;
  flex-shrink: 0;

  &.recharge {
    background: linear-gradient(135deg, #FFB74D, #FF9800);
  }

  &.consume {
    background: linear-gradient(135deg, #64B5F6, #2196F3);
  }

  &.withdraw {
    background: linear-gradient(135deg, #F06292, #E91E63);
  }

  &.refund {
    background: linear-gradient(135deg, #81C784, #4CAF50);
  }

  &.reward {
    background: linear-gradient(135deg, #FF6B35, #FF8F61);
  }

  .icon-text {
    font-size: 20px;
  }
}

.transaction-content {
  flex: 1;
  min-width: 0;
}

.transaction-header {
  @include flex-row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;

  .transaction-title {
    font-size: 15px;
    font-weight: bold;
    color: $text-color-primary;
  }

  .transaction-amount {
    font-size: 16px;
    font-weight: bold;

    &.income {
      color: #4CAF50;
    }

    &.expense {
      color: $text-color-primary;
    }
  }
}

.transaction-info {
  @include flex-row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;

  .transaction-time {
    font-size: 12px;
    color: $text-color-secondary;
  }

  .transaction-status {
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 8px;

    &.status-success {
      background-color: rgba(76, 175, 80, 0.1);
      color: #4CAF50;
    }

    &.status-pending {
      background-color: rgba(255, 152, 0, 0.1);
      color: #FF9800;
    }

    &.status-failed,
    &.status-cancelled {
      background-color: rgba(244, 67, 54, 0.1);
      color: #F44336;
    }
  }
}

.transaction-desc {
  font-size: 12px;
  color: $text-color-secondary;
  margin-top: 4px;
}

.load-more {
  @include flex-center;
  padding: 16px 0;

  .load-text {
    font-size: 13px;
    color: $text-color-secondary;
  }
}

.detail-popup {
  height: 70vh;
  background-color: #FFFFFF;
  border-radius: 20px 20px 0 0;
  @include flex-column;

  .detail-header {
    @include flex-row;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid $border-color;

    .detail-title {
      font-size: 16px;
      font-weight: bold;
    }
  }

  .detail-content {
    flex: 1;
    padding: 16px;
    overflow-y: auto;
  }

  .detail-amount-section {
    @include flex-column;
    align-items: center;
    padding: 24px 0;
    border-bottom: 1px solid $border-color;
    margin-bottom: 16px;

    .detail-amount {
      font-size: 32px;
      font-weight: bold;
      margin-bottom: 8px;

      &.income {
        color: #4CAF50;
      }

      &.expense {
        color: $text-color-primary;
      }
    }

    .detail-status {
      font-size: 13px;
      padding: 4px 12px;
      border-radius: 12px;

      &.status-success {
        background-color: rgba(76, 175, 80, 0.1);
        color: #4CAF50;
      }

      &.status-pending {
        background-color: rgba(255, 152, 0, 0.1);
        color: #FF9800;
      }

      &.status-failed,
      &.status-cancelled {
        background-color: rgba(244, 67, 54, 0.1);
        color: #F44336;
      }
    }
  }

  .detail-info {
    @include flex-column;
    gap: 16px;

    .detail-item {
      @include flex-row;
      align-items: flex-start;
      justify-content: space-between;

      .detail-label {
        font-size: 14px;
        color: $text-color-secondary;
        flex-shrink: 0;
      }

      .detail-value {
        flex: 1;
        font-size: 14px;
        color: $text-color-primary;
        text-align: right;
        word-break: break-all;
      }

      .detail-value-wrapper {
        @include flex-row;
        align-items: center;
        gap: 8px;

        .copy-btn {
          flex-shrink: 0;
          font-size: 12px;
          color: $primary-color;
        }
      }
    }
  }

  .detail-actions {
    @include flex-row;
    gap: 12px;
    margin-top: 24px;

    .detail-btn {
      flex: 1;
      height: 44px;
      @include flex-center;
      border-radius: 22px;
      font-size: 15px;
      border: none;

      &.secondary {
        background-color: $bg-color-hover;
        color: $text-color-primary;
      }

      &:active {
        opacity: 0.9;
      }
    }
  }
}
</style>
