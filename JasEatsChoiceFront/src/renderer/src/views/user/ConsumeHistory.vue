<template>
  <div class="consume-history-container">
    <common-back-button
      type="default"
      size="small"
      @click="goBack"
      :use-router-back="false"
      style="margin-bottom: 20px"
    />
    <h2>消费记录</h2>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon balance-icon">💰</div>
        <div class="stat-content">
          <div class="stat-label">当前余额</div>
          <div class="stat-value balance-color">{{ formatNumber(currentBalance) }}个</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon recharge-icon">📈</div>
        <div class="stat-content">
          <div class="stat-label">总收入</div>
          <div class="stat-value recharge-color">+{{ formatNumber(totalIncome) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon expense-icon">📉</div>
        <div class="stat-content">
          <div class="stat-label">总支出</div>
          <div class="stat-value expense-color">-{{ formatNumber(totalExpense) }}</div>
        </div>
      </div>
    </div>

    <!-- 筛选条件卡片 -->
    <el-card class="filter-card" shadow="hover">
      <div class="filter-bar">
        <el-select
          v-model="filterType"
          placeholder="筛选类型"
          style="width: 150px"
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
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 300px"
          @change="handleFilterChange"
        />

        <el-button type="primary" @click="applyFilter">
          <el-icon><Search /></el-icon> 查询
        </el-button>

        <el-button @click="resetFilter">
          <el-icon><Refresh /></el-icon> 重置
        </el-button>
      </div>
    </el-card>

    <!-- 交易列表 -->
    <el-card class="transactions-card" shadow="hover">
      <div v-loading="loading" class="transactions-list">
        <div v-if="history.length > 0">
          <div
            v-for="item in history"
            :key="item.id"
            class="transaction-item"
            @click="viewDetail(item)"
          >
            <div class="transaction-icon" :class="getIconClass(item.type)">
              {{ getIcon(item.type) }}
            </div>
            <div class="transaction-info">
              <div class="transaction-header">
                <span class="transaction-type">{{ getTypeText(item.type) }}</span>
                <el-tag
                  :type="item.status === 'success' ? 'success' : 'danger'"
                  size="small"
                >
                  {{ item.status === 'success' ? '成功' : '失败' }}
                </el-tag>
              </div>
              <div class="transaction-desc">{{ item.description }}</div>
              <div class="transaction-time">{{ formatDateTime(item.date) }}</div>
            </div>
            <div class="transaction-amount" :class="item.type === 'recharge' ? 'income' : 'expense'">
              {{ item.type === 'recharge' ? '+' : '-' }}{{ formatNumber(item.amount) }}
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无消费记录">
          <el-icon class="empty-icon"><Money /></el-icon>
        </el-empty>
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
    </el-card>

    <!-- 交易详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="交易详情" width="500px" center>
      <div v-if="selectedItem" class="detail-content">
        <div class="detail-row">
          <span class="detail-label">交易类型：</span>
          <el-tag :type="getDetailTagType(selectedItem.type)">
            {{ getTypeText(selectedItem.type) }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易金额：</span>
          <span class="detail-amount" :class="selectedItem.type === 'recharge' ? 'income' : 'expense'">
            {{ formatNumber(selectedItem.amount) }} 平台币
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易状态：</span>
          <el-tag :type="selectedItem.status === 'success' ? 'success' : 'danger'">
            {{ selectedItem.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易描述：</span>
          <span class="detail-value">{{ selectedItem.description }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">交易时间：</span>
          <span class="detail-value">{{ formatDateTime(selectedItem.date) }}</span>
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
import { Search, Refresh, Money } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
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
const getIcon = (type) => {
  const icons = {
    recharge: '💰',
    consume: '🛒',
    withdraw: '🏦'
  }
  return icons[type] || '📄'
}

// 获取图标样式类
const getIconClass = (type) => {
  const classes = {
    recharge: 'icon-recharge',
    consume: 'icon-consume',
    withdraw: 'icon-withdraw'
  }
  return classes[type] || ''
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
      dateRange.value && dateRange.value.length === 2
        ? formatDate(dateRange.value[0])
        : null
    const endDate =
      dateRange.value && dateRange.value.length === 2
        ? formatDate(dateRange.value[1])
        : null

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

<style scoped>
.consume-history-container {
  padding: 0 20px 20px 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

h2 {
  font-size: 2rem /* 原值: 28px */;
  margin: 0 0 20px 0;
  color: #333;
  font-weight: 700;
}

/* 统计卡片 */
.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  font-size: 2.857rem /* 原值: 40px */;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f7fafc;
  border-radius: 12px;
}

.balance-icon {
  background: linear-gradient(135deg, #fef5e7 0%, #fdebd0 100%);
}

.recharge-icon {
  background: linear-gradient(135deg, #c6f6d5 0%, #9ae6b4 100%);
}

.expense-icon {
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 1rem /* 原值: 14px */;
  color: #718096;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 1.714rem /* 原值: 24px */;
  font-weight: 700;
  color: #2d3748;
}

.balance-color {
  color: #d69e2e;
}

.recharge-color {
  color: #48bb78;
}

.expense-color {
  color: #f56565;
}

/* 筛选卡片 */
.filter-card {
  border-radius: 16px;
  margin-bottom: 20px;
  border: none;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

/* 交易列表 */
.transactions-card {
  border-radius: 16px;
  border: none;
}

.transactions-list {
  min-height: 300px;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.transaction-item:hover {
  background: #edf2f7;
  transform: translateX(4px);
}

.transaction-item:last-child {
  margin-bottom: 0;
}

.transaction-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.714rem /* 原值: 24px */;
  background: white;
  flex-shrink: 0;
}

.icon-recharge {
  background: linear-gradient(135deg, #c6f6d5 0%, #9ae6b4 100%);
}

.icon-consume {
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
}

.icon-withdraw {
  background: linear-gradient(135deg, #bee3f8 0%, #90cdf4 100%);
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
  font-size: 1.071rem /* 原值: 15px */;
  font-weight: 600;
  color: #2d3748;
}

.transaction-desc {
  font-size: 0.929rem /* 原值: 13px */;
  color: #718096;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.transaction-time {
  font-size: 0.857rem /* 原值: 12px */;
  color: #a0aec0;
}

.transaction-amount {
  font-size: 1.286rem /* 原值: 18px */;
  font-weight: 600;
  flex-shrink: 0;
}

.income {
  color: #48bb78;
}

.expense {
  color: #f56565;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 详情对话框 */
.detail-content {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #edf2f7;
}

.detail-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.detail-label {
  font-size: 1rem /* 原值: 14px */;
  color: #718096;
  width: 100px;
  flex-shrink: 0;
}

.detail-value {
  font-size: 1rem /* 原值: 14px */;
  color: #2d3748;
}

.detail-amount {
  font-size: 1.286rem /* 原值: 18px */;
  font-weight: 600;
}

.empty-icon {
  font-size: 64px;
  color: #dcdfe6;
}
</style>
