<template>
  <div class="wallet-transactions-container">
    <common-back-button
      type="default"
      size="small"
      @click="goBack"
      :use-router-back="false"
      style="margin-bottom: 20px"
    />

    <h2 class="page-title">钱包交易记录</h2>

    <!-- 钱包余额卡片 -->
    <el-card class="balance-card" shadow="hover">
      <div class="balance-info">
        <div class="balance-label">当前余额</div>
        <div class="balance-amount">{{ formatNumber(walletBalance) }} 平台币</div>
      </div>
    </el-card>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-bar">
        <el-select
          v-model="filterForm.type"
          placeholder="交易类型"
          style="width: 150px"
          clearable
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="充值" value="recharge" />
          <el-option label="消费" value="consume" />
          <el-option label="提现" value="withdraw" />
        </el-select>

        <el-select
          v-model="filterForm.status"
          placeholder="交易状态"
          style="width: 150px"
          clearable
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="成功" value="success" />
          <el-option label="失败" value="failed" />
        </el-select>

        <el-date-picker
          v-model="filterForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 280px"
          @change="handleFilterChange"
        />

        <el-button type="primary" @click="fetchTransactions">
          <el-icon><Search /></el-icon>
          查询
        </el-button>

        <el-button @click="resetFilter">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </el-card>

    <!-- 交易统计 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon recharge-icon">💰</div>
        <div class="stat-content">
          <div class="stat-label">总收入</div>
          <div class="stat-value recharge-color">+{{ formatNumber(totalIncome) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon expense-icon">🛒</div>
        <div class="stat-content">
          <div class="stat-label">总支出</div>
          <div class="stat-value expense-color">-{{ formatNumber(totalExpense) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon count-icon">📊</div>
        <div class="stat-content">
          <div class="stat-label">交易笔数</div>
          <div class="stat-value">{{ totalCount }}</div>
        </div>
      </div>
    </div>

    <!-- 交易列表 -->
    <el-card class="transactions-card" shadow="hover">
      <div v-loading="loading" class="transactions-list">
        <div v-if="transactions.length > 0">
          <div
            v-for="transaction in transactions"
            :key="transaction.id"
            class="transaction-item"
            @click="viewTransactionDetail(transaction)"
          >
            <div class="transaction-icon" :class="getTransactionIconClass(transaction.type)">
              {{ getTransactionIcon(transaction.type) }}
            </div>
            <div class="transaction-info">
              <div class="transaction-header">
                <span class="transaction-type">{{ getTransactionTypeText(transaction.type) }}</span>
                <el-tag
                  :type="transaction.status === 'success' ? 'success' : 'danger'"
                  size="small"
                >
                  {{ transaction.status === 'success' ? '成功' : '失败' }}
                </el-tag>
              </div>
              <div class="transaction-desc">{{ transaction.description }}</div>
              <div class="transaction-time">{{ formatDateTime(transaction.createTime) }}</div>
            </div>
            <div class="transaction-amount" :class="getAmountClass(transaction.type)">
              {{ transaction.type === 'recharge' ? '+' : '-'
              }}{{ formatNumber(transaction.amount) }}
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无交易记录" />
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 交易详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="交易详情" width="500px" center>
      <div v-if="selectedTransaction" class="transaction-detail">
        <div class="detail-row">
          <span class="detail-label">交易类型：</span>
          <el-tag :type="getDetailTagType(selectedTransaction.type)">
            {{ getTransactionTypeText(selectedTransaction.type) }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易金额：</span>
          <span class="detail-amount" :class="getAmountClass(selectedTransaction.type)">
            {{ formatNumber(selectedTransaction.amount) }} 平台币
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易状态：</span>
          <el-tag :type="selectedTransaction.status === 'success' ? 'success' : 'danger'">
            {{ selectedTransaction.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易描述：</span>
          <span class="detail-value">{{ selectedTransaction.description }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易时间：</span>
          <span class="detail-value">{{ formatDateTime(selectedTransaction.createTime) }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import walletApi from '../../api/wallet'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// 钱包余额
const walletBalance = ref(0)

// 交易列表
const transactions = ref([])
const loading = ref(false)
const total = ref(0)

// 筛选条件
const filterForm = ref({
  type: '',
  status: '',
  dateRange: null
})

// 分页
const pagination = ref({
  page: 1,
  pageSize: 20
})

// 交易详情对话框
const detailDialogVisible = ref(false)
const selectedTransaction = ref(null)

// 统计数据
const totalIncome = computed(() => {
  return transactions.value
    .filter((t) => t.type === 'recharge' && t.status === 'success')
    .reduce((sum, t) => sum + Number(t.amount || 0), 0)
})

const totalExpense = computed(() => {
  return transactions.value
    .filter((t) => (t.type === 'consume' || t.type === 'withdraw') && t.status === 'success')
    .reduce((sum, t) => sum + Number(t.amount || 0), 0)
})

const totalCount = computed(() => transactions.value.length)

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  return Number(num).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取交易图标
const getTransactionIcon = (type) => {
  const icons = {
    recharge: '💰',
    consume: '🛒',
    withdraw: '🏦'
  }
  return icons[type] || '📄'
}

// 获取交易图标样式类
const getTransactionIconClass = (type) => {
  const classes = {
    recharge: 'icon-recharge',
    consume: 'icon-consume',
    withdraw: 'icon-withdraw'
  }
  return classes[type] || ''
}

// 获取交易类型文本
const getTransactionTypeText = (type) => {
  const texts = {
    recharge: '充值',
    consume: '消费',
    withdraw: '提现'
  }
  return texts[type] || '其他'
}

// 获取金额样式类
const getAmountClass = (type) => {
  return type === 'recharge' ? 'amount-income' : 'amount-expense'
}

// 获取详情标签类型
const getDetailTagType = (type) => {
  const types = {
    recharge: 'success',
    consume: 'warning',
    withdraw: 'info'
  }
  return types[type] || ''
}

// 获取钱包余额
const fetchWalletBalance = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') return

  try {
    const result = await walletApi.getBalance(userId)
    if (result.code === '200') {
      walletBalance.value = result.data || 0
    }
  } catch (error) {
    console.error('获取钱包余额失败:', error)
  }
}

// 获取交易记录
const fetchTransactions = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') {
    ElMessage.error('用户未登录')
    return
  }

  loading.value = true
  try {
    const startDate =
      filterForm.value.dateRange && filterForm.value.dateRange.length === 2
        ? formatDate(filterForm.value.dateRange[0])
        : null
    const endDate =
      filterForm.value.dateRange && filterForm.value.dateRange.length === 2
        ? formatDate(filterForm.value.dateRange[1])
        : null

    const result = await walletApi.getConsumeHistory(
      userId,
      filterForm.value.type || 'all',
      pagination.value.page,
      pagination.value.pageSize,
      startDate,
      endDate,
      filterForm.value.status || null
    )

    if (result.code === '200' && result.data) {
      transactions.value = result.data.records || []
      total.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取交易记录失败')
    }
  } catch (error) {
    console.error('获取交易记录失败:', error)
    ElMessage.error('获取交易记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 筛选条件改变
const handleFilterChange = () => {
  pagination.value.page = 1
  fetchTransactions()
}

// 重置筛选
const resetFilter = () => {
  filterForm.value = {
    type: '',
    status: '',
    dateRange: null
  }
  pagination.value.page = 1
  fetchTransactions()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.value.page = 1
  fetchTransactions()
}

// 页码改变
const handlePageChange = () => {
  fetchTransactions()
}

// 查看交易详情
const viewTransactionDetail = (transaction) => {
  selectedTransaction.value = transaction
  detailDialogVisible.value = true
}

// 返回
const goBack = () => {
  router.back()
}

// 页面加载时获取数据
onMounted(() => {
  fetchWalletBalance()
  fetchTransactions()
})
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.wallet-transactions-container {
  padding: 0 @nordic-space-lg @nordic-space-lg @nordic-space-lg;
  min-height: 100vh;
  background: @nordic-bg;
}

.page-title {
  font-size: @nordic-text-xl;
  margin: 0 0 25px 0;
  color: @nordic-text;
  font-weight: 700;
}

.balance-card {
  background: linear-gradient(135deg, @nordic-accent-light 0%, darken(@nordic-accent-light, 5%) 100%);
  border-radius: @nordic-radius-lg;
  margin-bottom: @nordic-space-lg;
  border: none;
}

.balance-info {
  text-align: center;
  padding: @nordic-space-lg;
}

.balance-label {
  font-size: @nordic-text-md;
  color: @nordic-text-secondary;
  margin-bottom: @nordic-space-sm;
}

.balance-amount {
  font-size: @nordic-text-2xl;
  font-weight: 700;
  color: @nordic-yellow;
}

.filter-card {
  border-radius: @nordic-radius-lg;
  margin-bottom: @nordic-space-lg;
  border: none;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: @nordic-space-lg;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: @nordic-space-lg;
  background: @nordic-surface;
  border-radius: @nordic-radius-md;
  box-shadow: 0 2px 8px @nordic-shadow;
  transition: all @nordic-transition-slow ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px @nordic-shadow-hover;
}

.stat-icon {
  font-size: 2.857rem;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: @nordic-divider;
  border-radius: @nordic-radius-md;
}

.recharge-icon {
  background: linear-gradient(135deg, @nordic-green-light 0%, darken(@nordic-green-light, 5%) 100%);
}

.expense-icon {
  background: linear-gradient(135deg, @nordic-red-light 0%, darken(@nordic-red-light, 5%) 100%);
}

.count-icon {
  background: linear-gradient(135deg, @nordic-blue-light 0%, darken(@nordic-blue-light, 5%) 100%);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: @nordic-text-base;
  color: @nordic-text-secondary;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 1.714rem;
  font-weight: 700;
  color: @nordic-text;
}

.recharge-color {
  color: @nordic-green;
}

.expense-color {
  color: @nordic-red;
}

.transactions-card {
  border-radius: @nordic-radius-lg;
  border: none;
}

.transactions-list {
  min-height: 300px;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: @nordic-space-md;
  background: @nordic-divider;
  border-radius: @nordic-radius-md;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all @nordic-transition-fast ease;
}

.transaction-item:hover {
  background: @nordic-border;
  transform: translateX(4px);
}

.transaction-item:last-child {
  margin-bottom: 0;
}

.transaction-icon {
  width: 48px;
  height: 48px;
  border-radius: @nordic-radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.714rem;
  background: @nordic-surface;
  flex-shrink: 0;
}

.icon-recharge {
  background: linear-gradient(135deg, @nordic-green-light 0%, darken(@nordic-green-light, 5%) 100%);
}

.icon-consume {
  background: linear-gradient(135deg, @nordic-red-light 0%, darken(@nordic-red-light, 5%) 100%);
}

.icon-withdraw {
  background: linear-gradient(135deg, @nordic-blue-light 0%, darken(@nordic-blue-light, 5%) 100%);
}

.transaction-info {
  flex: 1;
  min-width: 0;
}

.transaction-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.transaction-type {
  font-size: 1.071rem;
  font-weight: 600;
  color: @nordic-text;
}

.transaction-desc {
  font-size: @nordic-text-sm;
  color: @nordic-text-secondary;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.transaction-time {
  font-size: @nordic-text-xs;
  color: @nordic-text-muted;
}

.transaction-amount {
  font-size: 1.286rem;
  font-weight: 600;
  flex-shrink: 0;
}

.amount-income {
  color: @nordic-green;
}

.amount-expense {
  color: @nordic-red;
}

.pagination-container {
  margin-top: @nordic-space-lg;
  display: flex;
  justify-content: center;
}

.transaction-detail {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: @nordic-space-lg;
  padding-bottom: 15px;
  border-bottom: 1px solid @nordic-divider;
}

.detail-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.detail-label {
  font-size: @nordic-text-base;
  color: @nordic-text-secondary;
  width: 100px;
  flex-shrink: 0;
}

.detail-value {
  font-size: @nordic-text-base;
  color: @nordic-text;
}

.detail-amount {
  font-size: 1.286rem;
  font-weight: 600;
}
</style>
