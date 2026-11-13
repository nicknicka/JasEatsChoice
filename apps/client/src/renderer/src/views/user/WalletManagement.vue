<template>
  <div class="wallet-management-container">
    <!-- 页头：标准版；「返回」收进右侧动作区（§4.7） -->
    <AppPageHeader title="钱包管理">
      <template #actions>
        <CommonBackButton type="default" :use-router-back="false" @click="goBack" />
      </template>
    </AppPageHeader>

    <!-- 钱包主区：余额、累计数据、入口统一在一张 L1 卡内（§4.2、§4.8） -->
    <AppCard class="wallet-overview-card" padding="lg">
      <div class="wallet-overview-content">
        <!-- 余额 + 两个主操作：充值取唯一实心主按钮，提现为同高次按钮（§4.1、§5.7） -->
        <div class="balance-row">
          <div class="balance-section">
            <div class="balance-label">平台币余额</div>
            <div class="balance-value">
              <span class="balance-number">{{ formatNumber(walletInfo.balance) }}</span>
              <span class="balance-unit">个</span>
            </div>
            <div class="balance-tips">
              <el-icon><InfoFilled /></el-icon>
              <span>1平台币 = 1元人民币</span>
            </div>
          </div>

          <div class="wallet-actions">
            <el-button type="primary" size="large" @click="showRechargeDialog">
              <el-icon class="btn-icon"><WalletFilled /></el-icon>
              充值
            </el-button>
            <el-button type="default" size="large" @click="showWithdrawDialog">
              <el-icon class="btn-icon"><Money /></el-icon>
              提现
            </el-button>
          </div>
        </div>

        <!-- 累计数据：金额不染色，数值统一 --font-num + tabular-nums（§4.8、§5.2） -->
        <div class="wallet-stats">
          <div class="stat-item">
            <el-icon class="stat-icon"><Top /></el-icon>
            <div class="stat-content">
              <div class="stat-label">累计充值</div>
              <div class="stat-value">{{ formatNumber(walletInfo.totalRecharge) }}个</div>
            </div>
          </div>
          <div class="stat-item">
            <el-icon class="stat-icon"><ShoppingCart /></el-icon>
            <div class="stat-content">
              <div class="stat-label">累计消费</div>
              <div class="stat-value">{{ formatNumber(walletInfo.totalConsume) }}个</div>
            </div>
          </div>
          <div class="stat-item">
            <el-icon class="stat-icon"><CreditCard /></el-icon>
            <div class="stat-content">
              <div class="stat-label">累计提现</div>
              <div class="stat-value">{{ formatNumber(walletInfo.totalWithdraw) }}个</div>
            </div>
          </div>
        </div>

        <!-- 其他入口降为文字动作，一组不超过 3 个（§4.1） -->
        <div class="wallet-entries">
          <el-button type="primary" link @click="setupPaymentPassword">支付密码</el-button>
          <el-button type="primary" link @click="viewWalletSecurity">安全设置</el-button>
          <el-button type="primary" link @click="contactSupport">联系客服</el-button>
        </div>
      </div>
    </AppCard>

    <!-- 最近交易：交易入口降为列表 + 文字链接（P0，§4.3、§5.2） -->
    <AppCard class="recent-transactions-card" padding="md">
      <div class="card-header">
        <h2 class="card-title">最近交易</h2>
        <el-button type="primary" link @click="viewTransactionHistory">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-if="recentTransactions.length > 0" class="transaction-list">
        <div
          v-for="transaction in recentTransactions"
          :key="transaction.id"
          class="transaction-item"
        >
          <div class="transaction-icon">
            <el-icon><component :is="getTransactionIcon(transaction.type)" /></el-icon>
          </div>
          <div class="transaction-info">
            <div class="transaction-type">{{ getTransactionTypeText(transaction.type) }}</div>
            <div class="transaction-time">{{ formatTime(transaction.createdAt) }}</div>
          </div>
          <div class="transaction-amount">
            <span class="transaction-amount-value">
              {{ getAmountSign(transaction.type) }}{{ formatNumber(transaction.amount) }}
            </span>
            <span class="transaction-amount-role">
              {{ getAmountRole(transaction.type) }}
            </span>
          </div>
        </div>
      </div>

      <AppEmptyState
        v-else
        icon="Money"
        description="暂无交易记录"
        action-text="去充值"
        @action="showRechargeDialog"
      />
    </AppCard>

    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeDialogVisible" title="充值" width="450px" center>
      <el-alert
        title="充值说明"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <ul style="margin: 5px 0; padding-left: 20px">
            <li>在线充值通道暂未开放，需充值请联系客服</li>
            <li>1平台币 = 1元人民币</li>
            <li>客服充值由管理端入账，到账后余额实时刷新</li>
            <li>充值遇到问题请联系客服</li>
          </ul>
        </template>
      </el-alert>

      <el-form :model="rechargeForm" label-width="100px">
        <el-form-item label="充值金额">
          <el-input
            v-model.number="rechargeForm.amount"
            type="number"
            placeholder="请输入充值金额"
            :min="1"
          >
            <template #append>平台币</template>
          </el-input>
        </el-form-item>
        <el-form-item label="充值方式">
          <el-tag type="info">平台币充值</el-tag>
          <span class="method-tip">微信支付、支付宝暂未开放</span>
        </el-form-item>
        <el-form-item label="快捷金额">
          <div class="quick-amount-buttons">
            <el-button
              v-for="amount in quickAmounts"
              :key="amount"
              @click="setRechargeAmount(amount)"
            >
              {{ amount }}币
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRecharge" :loading="recharging">
          立即充值
        </el-button>
      </template>
    </el-dialog>

    <!-- 提现对话框 -->
    <el-dialog v-model="withdrawDialogVisible" title="提现" width="450px" center>
      <el-alert
        title="提现规则"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <ul style="margin: 5px 0; padding-left: 20px">
            <li>单笔提现最低金额：10平台币</li>
            <li>提现手续费：2%（最低1平台币）</li>
            <li>提现到账时间：1-3个工作日</li>
            <li>每日提现限额：10000平台币</li>
          </ul>
        </template>
      </el-alert>

      <el-form :model="withdrawForm" label-width="100px">
        <el-form-item label="可提现余额">
          <span class="balance-highlight">{{ walletInfo.balance || 0 }}平台币</span>
        </el-form-item>
        <el-form-item label="提现金额">
          <el-input
            v-model.number="withdrawForm.amount"
            type="number"
            placeholder="请输入提现金额"
            :min="10"
            :max="walletInfo.balance || 0"
          >
            <template #append>平台币</template>
          </el-input>
        </el-form-item>
        <el-form-item label="预计手续费">
          <span class="fee-text">{{ calculateWithdrawFee(withdrawForm.amount) }}平台币</span>
        </el-form-item>
        <el-form-item label="预计到账">
          <span class="actual-amount">{{ calculateActualAmount(withdrawForm.amount) }}平台币</span>
        </el-form-item>
        <el-form-item label="支付密码" required>
          <el-input
            v-model="withdrawForm.password"
            type="password"
            placeholder="请输入支付密码"
            show-password
            maxlength="6"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="withdrawAll">全部提现</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmWithdraw" :loading="withdrawing">
          确认提现
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  InfoFilled,
  WalletFilled,
  Money,
  ArrowRight,
  Top,
  ShoppingCart,
  CreditCard
} from '@element-plus/icons-vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import walletApi from '../../api/wallet'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// 钱包信息
const walletInfo = ref({
  balance: 0,
  totalRecharge: 0,
  totalConsume: 0,
  totalWithdraw: 0
})

// 最近交易记录
const recentTransactionLimit = 10
const recentTransactions = ref([])

// 充值相关
const rechargeDialogVisible = ref(false)
const recharging = ref(false)
const rechargeForm = ref({
  amount: null,
  paymentMethod: 'wallet'
})
const quickAmounts = [10, 50, 100, 200, 500]

// 提现相关
const withdrawDialogVisible = ref(false)
const withdrawing = ref(false)
const withdrawForm = ref({
  amount: null,
  password: ''
})

// 计算提现手续费
const calculateWithdrawFee = (amount) => {
  if (!amount || amount <= 0) return '0'
  const fee = Math.max(Math.floor(amount * 0.02), 1)
  return fee.toFixed(2)
}

// 计算实际到账金额
const calculateActualAmount = (amount) => {
  if (!amount || amount <= 0) return '0'
  const fee = Math.max(Math.floor(amount * 0.02), 1)
  return (amount - fee).toFixed(2)
}

// 格式化数字显示
const formatNumber = (num) => {
  if (!num) return '0'
  return Number(num).toLocaleString('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days === 1) {
    return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  } else if (days < 7) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return (
      weekdays[date.getDay()] +
      ' ' +
      date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    )
  } else {
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  }
}

// 交易类型图标：结构性图标统一用 Element Plus 线性图标（§4.9）
const getTransactionIcon = (type) => {
  const icons = {
    recharge: WalletFilled,
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

// 金额角色：用文字把收支含义说清，色盲友好（§5.2）
const getAmountRole = (type) => (type === 'recharge' ? '收入' : '支出')

// 获取钱包信息
const fetchWalletInfo = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') {
    ElMessage.error('用户未登录，请重新登录')
    return
  }

  try {
    const response = await walletApi.getWalletInfo(userId)
    if (response.code === '200' && response.data) {
      walletInfo.value = response.data
    }
  } catch (error) {
    console.error('获取钱包信息失败:', error)
    ElMessage.error('获取钱包信息失败')
  }
}

// 获取最近交易记录
const fetchRecentTransactions = async () => {
  const userId = authStore.userId
  if (!userId || userId === '0') return

  try {
    // 使用 walletApi 调用消费记录API
    const result = await walletApi.getConsumeHistory(userId, 'all', 1, recentTransactionLimit)
    if (result.code === '200' && result.data && result.data.records) {
      recentTransactions.value = result.data.records
    }
  } catch (error) {
    console.error('获取交易记录失败:', error)
  }
}

// 显示充值对话框
const showRechargeDialog = () => {
  rechargeForm.value.amount = null
  rechargeDialogVisible.value = true
}

// 设置充值金额
const setRechargeAmount = (amount) => {
  rechargeForm.value.amount = amount
}

// 确认充值
// 后端已把钱包充值收口到管理端：用户侧没有可验证的资金到账渠道，
// 放开自助充值等于凭空造余额，这里只给出说明，不再调用接口。
const confirmRecharge = async () => {
  ElMessage.warning('在线充值通道暂未开放，请联系客服充值')
}

// 显示提现对话框
const showWithdrawDialog = () => {
  withdrawForm.value.amount = null
  withdrawForm.value.password = ''
  withdrawDialogVisible.value = true
}

// 全部提现
const withdrawAll = () => {
  withdrawForm.value.amount = walletInfo.value.balance || 0
}

// 验证支付密码
const validatePaymentPassword = (password) => {
  // 这里应该调用后端API验证支付密码
  // 目前简化处理，检查密码格式
  if (!password || password.length !== 6) {
    return false
  }
  return /^\d{6}$/.test(password)
}

// 确认提现
const confirmWithdraw = async () => {
  if (!withdrawForm.value.amount || withdrawForm.value.amount <= 0) {
    ElMessage.warning('请输入有效的提现金额')
    return
  }

  // 最低提现金额检查
  if (withdrawForm.value.amount < 10) {
    ElMessage.warning('单笔提现最低金额为10平台币')
    return
  }

  if (withdrawForm.value.amount > (walletInfo.value.balance || 0)) {
    ElMessage.warning('提现金额不能超过余额')
    return
  }

  // 每日提现限额检查
  if (withdrawForm.value.amount > 10000) {
    ElMessage.warning('每日提现限额为10000平台币')
    return
  }

  // 支付密码验证
  if (!withdrawForm.value.password) {
    ElMessage.warning('请输入支付密码')
    return
  }

  if (!validatePaymentPassword(withdrawForm.value.password)) {
    ElMessage.warning('支付密码格式不正确，请输入6位数字密码')
    return
  }

  const userId = authStore.userId
  if (!userId || userId === '0') {
    ElMessage.error('用户未登录，请重新登录')
    return
  }

  withdrawing.value = true
  try {
    const withdrawNo = 'WTH' + new Date().getTime() + Math.floor(Math.random() * 1000)

    // 调用提现接口（现在会创建提现记录，等待审核）
    const response = await walletApi.withdraw(
      userId,
      withdrawForm.value.amount,
      withdrawNo,
      'wechat', // 默认微信提现
      '微信钱包' // 账号信息
    )

    if (response.code === '200') {
      const fee = calculateWithdrawFee(withdrawForm.value.amount)
      const actualAmount = calculateActualAmount(withdrawForm.value.amount)
      ElMessage.success(
        `提现申请已提交：提现 ${withdrawForm.value.amount}平台币，手续费 ${fee}平台币，预计到账 ${actualAmount}平台币，等待管理员审核`
      )
      withdrawDialogVisible.value = false
      // 注意：提现只是申请，余额还未扣除，所以不需要刷新钱包信息
      // await fetchWalletInfo()
    } else {
      if (response.message && response.message.includes('密码')) {
        ElMessage.error('支付密码错误，请重新输入')
      } else {
        ElMessage.error(response.message || '提现申请失败，请重试')
      }
    }
  } catch (error) {
    console.error('提现申请失败:', error)
    ElMessage.error(error.message || '提现申请失败，请重试')
  } finally {
    withdrawing.value = false
  }
}

// 查看交易记录
const viewTransactionHistory = () => {
  router.push('/user/home/wallet-transactions')
}

// 设置支付密码
const setupPaymentPassword = () => {
  router.push('/user/home/payment-password-setup')
}

// 查看安全设置
const viewWalletSecurity = () => {
  router.push('/user/home/wallet-security')
}

// 联系客服
const contactSupport = () => {
  router.push('/user/home/contact')
}

// 返回
const goBack = () => {
  router.back()
}

// 页面加载时获取钱包信息
onMounted(() => {
  fetchWalletInfo()
  fetchRecentTransactions()
})
</script>

<style scoped lang="less">
/* 页面颜色、间距、圆角、字阶与动效均取自 tokens.css（§3、§7.1），无裸色值。 */
.wallet-management-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 钱包主区：默认 L1 微质感，不加彩色渐变与重阴影（§3.4、§4.2） */
.wallet-overview-card {
  margin-bottom: var(--card-gap);
}

.wallet-overview-content {
  min-width: 0;
}

/* 余额在左、动作在右，窄屏折行（§4.7） */
.balance-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-6);
}

.balance-section {
  min-width: 0;
}

.balance-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.balance-value {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

/* 余额是页面数值主轴：--font-num + tabular-nums，中性深色不染色（§3.2、§5.2） */
.balance-number {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-display);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
  line-height: 40px;
}

.balance-unit {
  color: var(--ink-500);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
}

.balance-tips {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-2);
  color: var(--ink-400);
  font-size: var(--text-caption);
}

/* 两个主操作同高并排；高度只用 size="large"（--control-height-md），页面不写死高度（§3.4、§4.1） */
.wallet-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
}

.wallet-actions .el-button {
  min-width: 128px;
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
}

.btn-icon {
  margin-right: var(--space-1);
}

/* 累计数据：与余额同卡统一，三项等分，金额中性（§4.8、§5.2） */
.wallet-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--card-gap);
  margin-top: var(--space-6);
  padding-top: var(--space-5);
  border-top: 1px solid var(--line);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.stat-icon {
  flex-shrink: 0;
  color: var(--ink-400);
  font-size: var(--text-h2);
}

.stat-content {
  min-width: 0;
}

.stat-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

/* 累计值统一 --font-num + tabular-nums，中性深色，不给数字染红绿蓝（§3.2、§5.2） */
.stat-value {
  margin-top: var(--space-1);
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
  line-height: 24px;
}

/* 其他入口的文字动作行（§4.1 文字按钮） */
.wallet-entries {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-5);
  margin-top: var(--space-5);
}

.recent-transactions-card {
  /* AppCard 默认 L1 微质感，列表卡片不加大阴影（§3.4） */
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

/* 区块标题从 h2 起，字阶 --text-h2（§4.7） */
.card-title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-h2);
  font-weight: var(--weight-semibold);
  line-height: 28px;
}

.transaction-list {
  display: flex;
  flex-direction: column;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--line);
}

.transaction-item:last-child {
  /* 保留占位，保证所有交易行等高（§4.3） */
  border-bottom-color: transparent;
}

.transaction-icon {
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

.transaction-info {
  flex: 1;
  min-width: 0;
}

.transaction-type {
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  line-height: 22px;
}

.transaction-time {
  margin-top: var(--space-1);
  color: var(--ink-400);
  font-size: var(--text-caption);
  line-height: 18px;
}

/* 金额右对齐、中性深色、tabular-nums；正负靠符号 + 文字角色表达（§5.2） */
.transaction-amount {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  flex-shrink: 0;
  gap: var(--space-1);
  text-align: right;
}

.transaction-amount-value {
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

/* 弹层内金额一律中性 + --font-num，不给数字染红绿（§5.2） */
.balance-highlight,
.fee-text,
.actual-amount {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
}

.method-tip {
  margin-left: var(--space-3);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.quick-amount-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
</style>
