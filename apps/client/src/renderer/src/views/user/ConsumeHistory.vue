<template>
  <div class="consume-history-page">
    <!-- 页头：标准版，标题 --text-h1 + 上下 --space-6（§4.7） -->
    <AppPageHeader title="消费记录">
      <template #actions>
        <CommonBackButton type="default" :use-router-back="false" @click="goBack" />
      </template>
    </AppPageHeader>

    <!-- 统计卡：线性图标 + 中性金额，金额不靠红绿蓝编码（§4.8、§5.2） -->
    <div class="stat-row">
      <AppStatCard label="当前余额" :value="`¥${formatAmount(currentBalance)}`" :icon="Wallet" />
      <AppStatCard label="总收入" :value="`+¥${formatAmount(totalIncome)}`" :icon="Top" />
      <AppStatCard label="总支出" :value="`-¥${formatAmount(totalExpense)}`" :icon="Bottom" />
    </div>

    <!-- 筛选条：控件同高、同间距（§3.3、§4.3） -->
    <AppCard class="filter-card" padding="md">
      <div class="filter-bar">
        <el-select
          v-model="filterType"
          class="filter-control filter-control--select"
          placeholder="筛选类型"
          clearable
          @change="handleFilterChange"
        >
          <el-option label="全部" value="all" />
          <el-option label="充值" value="recharge" />
          <el-option label="消费" value="consume" />
          <el-option label="提现" value="withdraw" />
        </el-select>

        <el-select
          v-model="filterStatus"
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
          v-model="dateRange"
          class="filter-control filter-control--date"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleFilterChange"
        />

        <el-button type="primary" @click="applyFilter">
          <el-icon><Search /></el-icon> 查询
        </el-button>

        <el-button @click="resetFilter">
          <el-icon><Refresh /></el-icon> 重置
        </el-button>
      </div>
    </AppCard>

    <!-- 交易列表：收入 / 支出统一为「符号 + 金额 + 角色文字」的行（P0，§4.3、§5.2） -->
    <AppCard class="list-card" padding="md">
      <div v-loading="loading" class="tx-list">
        <div v-if="history.length > 0" class="tx-rows">
          <div
            v-for="item in history"
            :key="item.id"
            class="tx-row"
            tabindex="0"
            @click="viewDetail(item)"
            @keydown.enter="viewDetail(item)"
          >
            <div class="tx-row__icon">
              <el-icon><component :is="getTypeIcon(item.type)" /></el-icon>
            </div>
            <div class="tx-row__main">
              <div class="tx-row__head">
                <span class="tx-row__type">{{ getTypeText(item.type) }}</span>
                <StatusTag :status="item.status" size="sm" />
              </div>
              <div class="tx-row__desc">{{ item.description }}</div>
              <div class="tx-row__time">{{ formatDateTime(item.createdAt) }}</div>
            </div>
            <div class="tx-row__amount">
              <span class="tx-row__amount-value">
                {{ getAmountSign(item.type) }} ¥{{ formatAmount(item.amount) }}
              </span>
              <span class="tx-row__amount-role">{{ getAmountRole(item.type) }}</span>
            </div>
          </div>
        </div>
        <AppEmptyState v-else icon="Money" description="暂无消费记录" />
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </AppCard>

    <!-- 交易详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="交易详情" width="500px" center>
      <div v-if="selectedItem" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">交易类型：</span>
          <span class="detail-value">{{ getTypeText(selectedItem.type) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易金额：</span>
          <span class="detail-amount">
            {{ getAmountSign(selectedItem.type) }} ¥{{ formatAmount(selectedItem.amount) }}
            <span class="detail-role">{{ getAmountRole(selectedItem.type) }}</span>
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易状态：</span>
          <StatusTag :status="selectedItem.status" />
        </div>
        <div class="detail-row">
          <span class="detail-label">交易描述：</span>
          <span class="detail-value">{{ selectedItem.description }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易时间：</span>
          <span class="detail-value">{{ formatDateTime(selectedItem.createdAt) }}</span>
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
import {
  Search,
  Refresh,
  Wallet,
  ShoppingCart,
  CreditCard,
  Top,
  Bottom,
  Money
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppStatCard from '../../components/common/AppStatCard.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import walletApi from '../../api/wallet'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// 消费记录数据
const history = ref([])
const total = ref(0)
const loading = ref(false)

// 筛选条件
const filterType = ref('all')
const filterStatus = ref('')
const dateRange = ref(null)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)

// 当前余额
const currentBalance = ref(0)

// 统计数据
const totalIncome = computed(() => {
  return history.value
    .filter((t) => t.type === 'recharge' && t.status === 'success')
    .reduce((sum, t) => sum + Number(t.amount || 0), 0)
})

const totalExpense = computed(() => {
  return history.value
    .filter((t) => (t.type === 'consume' || t.type === 'withdraw') && t.status === 'success')
    .reduce((sum, t) => sum + Number(t.amount || 0), 0)
})

// 交易详情对话框
const detailDialogVisible = ref(false)
const selectedItem = ref(null)

// 金额统一格式：千分位 + 两位小数，配合 --font-num 保证跨行对齐（§3.2、§5.2）
const formatAmount = (num) => {
  return Number(num || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 格式化日期时间：统一 YYYY-MM-DD HH:mm（§4.3、§6.1）
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const d = new Date(dateTime)
  if (Number.isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// 交易类型图标：结构性图标统一用 Element Plus 线性图标（§4.9）
const getTypeIcon = (type) => {
  const icons = {
    recharge: Wallet,
    consume: ShoppingCart,
    withdraw: CreditCard
  }
  return icons[type] || Money
}

// 获取交易类型文本
const getTypeText = (type) => {
  const texts = {
    recharge: '充值',
    consume: '消费',
    withdraw: '提现'
  }
  return texts[type] || '其他'
}

// 金额方向：充值入账为正，消费/提现出账为负（§5.2 符号先于颜色）
const getAmountSign = (type) => (type === 'recharge' ? '+' : '-')

// 金额角色：用文字把收支含义说清，色盲友好（§5.2）
const getAmountRole = (type) => (type === 'recharge' ? '收入' : '支出')

// 获取钱包余额
const fetchWalletBalance = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') return

  try {
    const result = await walletApi.getBalance(userId)
    if (result.code === '200') {
      currentBalance.value = result.data || 0
    }
  } catch (error) {
    console.error('获取钱包余额失败:', error)
  }
}

// 从后端获取消费记录
const fetchConsumeHistory = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') {
    ElMessage.error('用户未登录')
    return
  }

  loading.value = true
  try {
    const startDate =
      dateRange.value && dateRange.value.length === 2 ? formatDate(dateRange.value[0]) : null
    const endDate =
      dateRange.value && dateRange.value.length === 2 ? formatDate(dateRange.value[1]) : null

    const result = await walletApi.getConsumeHistory(
      userId,
      filterType.value || 'all',
      currentPage.value,
      pageSize.value,
      startDate,
      endDate
    )

    if (result.code === '200' && result.data) {
      history.value = result.data.records || []
      total.value = result.data.total || 0
    } else {
      ElMessage.error(result.message || '获取消费记录失败')
    }
  } catch (error) {
    console.error('获取消费记录失败:', error)
    ElMessage.error('获取消费记录失败，请稍后重试')
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
  currentPage.value = 1
  fetchConsumeHistory()
}

// 应用筛选
const applyFilter = () => {
  currentPage.value = 1
  fetchConsumeHistory()
}

// 重置筛选
const resetFilter = () => {
  filterType.value = 'all'
  filterStatus.value = ''
  dateRange.value = null
  currentPage.value = 1
  fetchConsumeHistory()
  ElMessage.info('筛选条件已重置')
}

// 页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  fetchConsumeHistory()
}

// 页面变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchConsumeHistory()
}

// 查看详情
const viewDetail = (item) => {
  selectedItem.value = item
  detailDialogVisible.value = true
}

// 返回个人中心
const goBack = () => {
  router.push('/user/home/profile')
}

// 页面加载时获取数据
onMounted(() => {
  fetchWalletBalance()
  fetchConsumeHistory()
})
</script>

<style scoped lang="less">
/* 页面颜色、间距、圆角、字阶与动效均取自 tokens.css（§3、§7.1），无裸色值。 */
.consume-history-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 统计卡栅格：数值一律 --font-num + tabular-nums（§4.8） */
.stat-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--card-gap);
  margin-bottom: var(--space-6);
}

.filter-card {
  margin-bottom: var(--space-6);
}

/* 筛选条：控件统一高度与间距（§3.3、§4.3） */
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
.tx-list {
  min-height: 240px;
}

.tx-rows {
  display: flex;
  flex-direction: column;
}

.tx-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-2);
  border-bottom: 1px solid var(--line);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--ease-out);
}

.tx-row:last-child {
  /* 保留 1px 占位，保证所有交易行等高（§4.3 行高一致） */
  border-bottom-color: transparent;
}

.tx-row:hover,
.tx-row:focus-visible {
  background: var(--canvas-sunken);
  outline: none;
}

.tx-row:focus-visible {
  box-shadow: inset 0 0 0 2px var(--brand-400);
}

.tx-row__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  color: var(--ink-500);
  font-size: var(--text-h3);
}

.tx-row__main {
  flex: 1;
  min-width: 0;
}

.tx-row__head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.tx-row__type {
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  line-height: 22px;
}

.tx-row__desc {
  overflow: hidden;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tx-row__time {
  margin-top: var(--space-1);
  color: var(--ink-400);
  font-size: var(--text-caption);
  line-height: 18px;
}

.tx-row__amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
  gap: var(--space-1);
  text-align: right;
}

.tx-row__amount-value {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.tx-row__amount-role {
  color: var(--ink-500);
  font-size: var(--text-caption);
  line-height: 18px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}

/* 详情弹层 */
.detail-content {
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
