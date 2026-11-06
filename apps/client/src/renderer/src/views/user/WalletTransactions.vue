<template>
  <div class="wallet-transactions-page">
    <!-- 页头：标准版，标题 --text-h1，返回统一放右侧动作区（§4.7） -->
    <AppPageHeader title="钱包交易记录">
      <template #actions>
        <CommonBackButton type="default" :use-router-back="false" @click="goBack" />
      </template>
    </AppPageHeader>

    <!-- 余额摘要：余额 + 收支概览收敛为一块，交易列表提前进首屏（P0，§4.8） -->
    <AppCard class="summary-card" padding="md">
      <div class="summary">
        <div class="summary-balance">
          <div class="summary-balance__label">当前余额</div>
          <div class="balance-amount">{{ formatNumber(walletBalance) }} 平台币</div>
        </div>

        <div class="stats-row">
          <div class="stat-card">
            <el-icon class="stat-card__icon"><Top /></el-icon>
            <div class="stat-card__body">
              <div class="stat-card__label">总收入</div>
              <div class="stat-value">+{{ formatNumber(totalIncome) }}</div>
            </div>
          </div>
          <div class="stat-card">
            <el-icon class="stat-card__icon"><Bottom /></el-icon>
            <div class="stat-card__body">
              <div class="stat-card__label">总支出</div>
              <div class="stat-value">-{{ formatNumber(totalExpense) }}</div>
            </div>
          </div>
          <div class="stat-card">
            <el-icon class="stat-card__icon"><Tickets /></el-icon>
            <div class="stat-card__body">
              <div class="stat-card__label">交易笔数</div>
              <div class="stat-value">{{ totalCount }}</div>
            </div>
          </div>
        </div>
      </div>
    </AppCard>

    <!-- 筛选工具栏：控件同高、同间距（§3.3、§4.3） -->
    <AppCard class="filter-card" padding="md">
      <div class="filter-bar">
        <el-select
          v-model="filterForm.type"
          class="filter-control filter-control--select"
          placeholder="交易类型"
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
          class="filter-control filter-control--select"
          placeholder="交易状态"
          clearable
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="成功" value="success" />
          <el-option label="失败" value="failed" />
        </el-select>

        <el-date-picker
          v-model="filterForm.dateRange"
          class="filter-control filter-control--date"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
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
    </AppCard>

    <!-- 交易列表：收入 / 支出统一为「图标 + 类型 + 状态标签 + 符号金额 + 收支角色」（P0、P1，§4.3、§5.2） -->
    <AppCard class="transactions-card" padding="md">
      <div v-loading="loading" class="transactions-list">
        <div v-if="transactions.length > 0" class="transaction-rows">
          <div
            v-for="transaction in transactions"
            :key="transaction.id"
            class="transaction-item"
            tabindex="0"
            @click="viewTransactionDetail(transaction)"
            @keydown.enter="viewTransactionDetail(transaction)"
          >
            <div class="transaction-icon">
              <el-icon><component :is="getTransactionIcon(transaction.type)" /></el-icon>
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
              <div class="transaction-time">{{ formatDateTime(transaction.createdAt) }}</div>
            </div>
            <div class="transaction-amount-wrap">
              <div class="transaction-amount">
                {{ getAmountSign(transaction.type) }}{{ formatNumber(transaction.amount) }}
              </div>
              <div class="transaction-amount-role">{{ getAmountRole(transaction.type) }}</div>
            </div>
          </div>
        </div>
        <AppEmptyState v-else icon="Money" description="暂无交易记录" />
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
    </AppCard>

    <!-- 交易详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="交易详情" width="500px" center>
      <div v-if="selectedTransaction" class="transaction-detail">
        <div class="detail-row">
          <span class="detail-label">交易类型：</span>
          <span class="detail-value">{{ getTransactionTypeText(selectedTransaction.type) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易金额：</span>
          <span class="detail-amount">
            {{ getAmountSign(selectedTransaction.type)
            }}{{ formatNumber(selectedTransaction.amount) }} 平台币
            <span class="detail-role">{{ getAmountRole(selectedTransaction.type) }}</span>
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
          <span class="detail-value">{{ formatDateTime(selectedTransaction.createdAt) }}</span>
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
import {
  Search,
  Refresh,
  Wallet,
  ShoppingCart,
  CreditCard,
  Top,
  Bottom,
  Tickets,
  Money
} from '@element-plus/icons-vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
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

// 交易类型图标：结构性图标统一用 Element Plus 线性图标（§4.9）
const getTransactionIcon = (type) => {
  const icons = {
    recharge: Wallet,
    consume: ShoppingCart,
    withdraw: CreditCard
  }
  return icons[type] || Money
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

// 金额方向：充值入账为正，消费/提现出账为负（§5.2 符号先于颜色）
const getAmountSign = (type) => (type === 'recharge' ? '+' : '-')

// 金额角色：用文字说清收支含义，金额本身不再靠红绿蓝编码（§5.2）
const getAmountRole = (type) => (type === 'recharge' ? '收入' : '支出')

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

<style scoped>
/* 页面颜色、间距、圆角、字阶与动效均取自 tokens.css（§3、§7.1），无裸色值。 */
.wallet-transactions-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 余额摘要：余额 + 收支概览合并为一块，不再逐层堆卡片（P0，§4.8） */
.summary-card {
  margin-bottom: var(--space-6);
}

.summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-8);
}

.summary-balance {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 200px;
  padding-right: var(--space-8);
  border-right: 1px solid var(--line);
}

.summary-balance__label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

/* 余额数值：--font-num + tabular-nums，与消费记录同格式（§3.2、§4.8） */
.balance-amount {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h1);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
  line-height: 32px;
}

/* 收支概览：线性图标 + 中性底色 + 数值，不用彩色渐变图标块（§3.4、§4.8） */
.stats-row {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: var(--space-6);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.stat-card__icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  color: var(--ink-500);
  font-size: var(--text-h3);
}

.stat-card__body {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.stat-card__label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.stat-value {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
  line-height: 24px;
}

/* 筛选工具栏 */
.filter-card {
  margin-bottom: var(--space-6);
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
}

.filter-control--select {
  width: 160px;
}

.filter-control--date {
  width: 300px;
}

/* 交易列表 */
.transactions-card {
  margin-bottom: var(--space-6);
}

.transactions-list {
  min-height: 240px;
}

.transaction-rows {
  display: flex;
  flex-direction: column;
}

/* 每行等高、行内控件 32px 档，不写死高度（§3.4、§4.3） */
.transaction-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-2);
  border-bottom: 1px solid var(--line);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--ease-out);
}

.transaction-item:last-child {
  /* 保留 1px 占位，保证所有交易行等高（§4.3） */
  border-bottom-color: transparent;
}

.transaction-item:hover,
.transaction-item:focus-visible {
  background: var(--canvas-sunken);
  outline: none;
}

.transaction-item:focus-visible {
  box-shadow: inset 0 0 0 2px var(--brand-400);
}

.transaction-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  color: var(--ink-500);
  font-size: var(--text-h3);
}

.transaction-info {
  flex: 1;
  min-width: 0;
}

.transaction-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.transaction-type {
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  line-height: 22px;
}

.transaction-desc {
  overflow: hidden;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.transaction-time {
  margin-top: var(--space-1);
  color: var(--ink-400);
  font-size: var(--text-caption);
  line-height: 18px;
}

/* 金额：中性色 + 符号 + 收支角色文字，不靠红绿编码（P1，§5.2） */
.transaction-amount-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
  gap: var(--space-1);
  text-align: right;
}

.transaction-amount {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.transaction-amount-role {
  color: var(--ink-500);
  font-size: var(--text-caption);
  line-height: 18px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}

/* 详情弹层 */
.transaction-detail {
  padding: var(--space-2) 0;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--line);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  flex-shrink: 0;
  width: 84px;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.detail-value {
  color: var(--ink-700);
  font-size: var(--text-body-sm);
}

.detail-amount {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
}

.detail-role {
  margin-left: var(--space-2);
  color: var(--ink-500);
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  font-weight: var(--weight-regular);
}
</style>
