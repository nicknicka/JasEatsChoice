<template>
  <div class="wallet-management-container">
    <common-back-button
      type="default"
      size="small"
      @click="goBack"
      :use-router-back="false"
      style="margin-bottom: 20px"
    />

    <h2 class="page-title">钱包管理</h2>

    <!-- 钱包概览卡片 -->
    <el-card class="wallet-overview-card scale-in" shadow="hover">
      <div class="wallet-overview-content">
        <div class="balance-section">
          <div class="balance-label">平台币余额</div>
          <div class="balance-value">
            <span class="balance-number number-scroll">{{ formatNumber(walletInfo.balance) }}</span>
            <span class="balance-unit">个</span>
          </div>
          <div class="balance-tips">
            <el-icon><InfoFilled /></el-icon>
            <span>1平台币 = 1元人民币</span>
          </div>
        </div>

        <div class="wallet-stats">
          <div class="stat-item">
            <div class="stat-icon">💰</div>
            <div class="stat-content">
              <div class="stat-label">累计充值</div>
              <div class="stat-value recharge-color">
                {{ formatNumber(walletInfo.totalRecharge) }}个
              </div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">🛒</div>
            <div class="stat-content">
              <div class="stat-label">累计消费</div>
              <div class="stat-value consume-color">
                {{ formatNumber(walletInfo.totalConsume) }}个
              </div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">🏦</div>
            <div class="stat-content">
              <div class="stat-label">累计提现</div>
              <div class="stat-value withdraw-color">
                {{ formatNumber(walletInfo.totalWithdraw) }}个
              </div>
            </div>
          </div>
        </div>

        <div class="wallet-actions">
          <el-button type="primary" size="large" @click="showRechargeDialog">
            <el-icon class="btn-icon"><WalletFilled /></el-icon>
            充值
          </el-button>
          <el-button type="success" size="large" @click="showWithdrawDialog">
            <el-icon class="btn-icon"><Money /></el-icon>
            提现
          </el-button>
          <el-button type="info" size="large" @click="viewTransactionHistory">
            <el-icon class="btn-icon"><List /></el-icon>
            交易记录
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 快捷功能 -->
    <el-card class="quick-actions-card stagger-item" shadow="hover">
      <h3 class="card-title">快捷功能</h3>
      <div class="quick-actions-grid">
        <div class="quick-action-item" @click="viewTransactionHistory">
          <div class="action-icon">📋</div>
          <div class="action-text">交易记录</div>
          <div class="action-desc">查看所有交易明细</div>
        </div>
        <div class="quick-action-item" @click="setupPaymentPassword">
          <div class="action-icon">🔒</div>
          <div class="action-text">支付密码</div>
          <div class="action-desc">设置或修改支付密码</div>
        </div>
        <div class="quick-action-item" @click="viewWalletSecurity">
          <div class="action-icon">🛡️</div>
          <div class="action-text">安全设置</div>
          <div class="action-desc">管理账户安全</div>
        </div>
        <div class="quick-action-item" @click="contactSupport">
          <div class="action-icon">💬</div>
          <div class="action-text">联系客服</div>
          <div class="action-desc">遇到问题请联系我们</div>
        </div>
      </div>
    </el-card>

    <!-- 最近交易 -->
    <el-card class="recent-transactions-card stagger-item" shadow="hover">
      <div class="card-header">
        <h3 class="card-title">最近交易</h3>
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
          <div class="transaction-icon" :class="getTransactionIconClass(transaction.type)">
            {{ getTransactionIcon(transaction.type) }}
          </div>
          <div class="transaction-info">
            <div class="transaction-type">{{ getTransactionTypeText(transaction.type) }}</div>
            <div class="transaction-time">{{ formatTime(transaction.createTime) }}</div>
          </div>
          <div class="transaction-amount" :class="getAmountClass(transaction.type)">
            {{ transaction.type === 'recharge' ? '+' : '-' }}{{ formatNumber(transaction.amount) }}
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无交易记录" />
    </el-card>

    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeDialogVisible" title="充值" width="400px" center>
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
        <el-form-item label="快捷金额">
          <div class="quick-amount-buttons">
            <el-button
              v-for="amount in quickAmounts"
              :key="amount"
              size="small"
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
          确认充值
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
          <el-button size="small" @click="withdrawAll">全部提现</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmWithdraw" :loading="withdrawing">
          确认提现
        </el-button>
      </template>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeDialogVisible" title="充值" width="450px" center>
      <el-alert
        title="充值说明"
        type="success"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <ul style="margin: 5px 0; padding-left: 20px">
            <li>充值即时到账，无手续费</li>
            <li>1平台币 = 1元人民币</li>
            <li>支持微信支付、支付宝等方式</li>
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
        <el-form-item label="支付方式">
          <el-radio-group v-model="rechargeForm.paymentMethod">
            <el-radio value="wechat">
              <span style="display: flex; align-items: center; gap: 5px">
                <span style="color: #09bb07">💬</span> 微信支付
              </span>
            </el-radio>
            <el-radio value="alipay">
              <span style="display: flex; align-items: center; gap: 5px">
                <span style="color: #1677ff">💳</span> 支付宝
              </span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="快捷金额">
          <div class="quick-amount-buttons">
            <el-button
              v-for="amount in quickAmounts"
              :key="amount"
              size="small"
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled, WalletFilled, List, ArrowRight } from '@element-plus/icons-vue'
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
const recentTransactions = ref([])

// 充值相关
const rechargeDialogVisible = ref(false)
const recharging = ref(false)
const rechargeForm = ref({
  amount: null,
  paymentMethod: 'wechat'
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

// 获取交易类型图标
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
    const result = await walletApi.getConsumeHistory(userId, 'all', 1, 5)
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
const confirmRecharge = async () => {
  if (!rechargeForm.value.amount || rechargeForm.value.amount <= 0) {
    ElMessage.warning('请输入有效的充值金额')
    return
  }

  const userId = authStore.userId
  if (!userId || userId === '0') {
    ElMessage.error('用户未登录，请重新登录')
    return
  }

  // 检查支付方式
  if (!rechargeForm.value.paymentMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }

  recharging.value = true
  try {
    const rechargeNo = 'RCH' + new Date().getTime() + Math.floor(Math.random() * 1000)

    // 这里应该调用第三方支付接口
    // 目前模拟充值成功
    ElMessage.info(`正在跳转到${rechargeForm.value.paymentMethod === 'wechat' ? '微信' : '支付宝'}支付...`)

    // 模拟支付成功（实际应该等待第三方支付回调）
    await new Promise(resolve => setTimeout(resolve, 1000))

    const response = await walletApi.recharge(userId, rechargeForm.value.amount, rechargeNo)

    if (response.code === '200') {
      ElMessage.success(`充值成功！已到账${rechargeForm.value.amount}平台币`)
      rechargeDialogVisible.value = false
      rechargeForm.value.amount = null
      await fetchWalletInfo()
    } else {
      ElMessage.error(response.message || '充值失败，请重试')
    }
  } catch (error) {
    console.error('充值失败:', error)
    ElMessage.error(error.message || '充值失败，请重试')
  } finally {
    recharging.value = false
  }
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

    // 这里应该验证支付密码是否正确
    // 目前简化处理，直接调用提现接口
    const response = await walletApi.withdraw(userId, withdrawForm.value.amount, withdrawNo)

    if (response.code === '200') {
      const fee = calculateWithdrawFee(withdrawForm.value.amount)
      const actualAmount = calculateActualAmount(withdrawForm.value.amount)

      ElMessage.success({
        dangerouslyUseHTMLString: true,
        message: `
          <div>提现申请已提交！</div>
          <div style="margin-top: 8px; font-size: 0.857rem /* 原值: 12px */;">
            提现金额：${withdrawForm.value.amount}平台币<br>
            手续费：${fee}平台币<br>
            预计到账：${actualAmount}平台币<br>
            到账时间：1-3个工作日
          </div>
        `
      })
      withdrawDialogVisible.value = false
      await fetchWalletInfo()
    } else {
      if (response.message && response.message.includes('密码')) {
        ElMessage.error('支付密码错误，请重新输入')
      } else {
        ElMessage.error(response.message || '提现失败，请重试')
      }
    }
  } catch (error) {
    console.error('提现失败:', error)
    ElMessage.error(error.message || '提现失败，请重试')
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

<style scoped>
.wallet-management-container {
  padding: 0 20px 20px 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

.page-title {
  font-size: 2rem /* 原值: 28px */;
  margin: 0 0 25px 0;
  color: #333;
  font-weight: 700;
}

.wallet-overview-card {
  background: linear-gradient(135deg, #fef5e7 0%, #fdebd0 100%);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(214, 158, 46, 0.15);
  margin-bottom: 20px;
  border: none;
  transition: all 0.3s ease;
}

.wallet-overview-card:hover {
  box-shadow: 0 6px 24px rgba(214, 158, 46, 0.25);
}

.wallet-overview-content {
  padding: 30px;
}

.balance-section {
  text-align: center;
  margin-bottom: 30px;
}

.balance-label {
  font-size: 1.143rem /* 原值: 16px */;
  color: #718096;
  font-weight: 500;
  margin-bottom: 10px;
}

.balance-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
}

.balance-number {
  font-size: 56px;
  font-weight: 700;
  color: #d69e2e;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #d69e2e 0%, #ecc94b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.balance-unit {
  font-size: 1.429rem /* 原值: 20px */;
  color: #d69e2e;
  font-weight: 500;
}

.balance-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-top: 10px;
  font-size: 0.929rem /* 原值: 13px */;
  color: #a0aec0;
}

.wallet-stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
  padding: 20px 0;
  border-top: 1px solid rgba(214, 158, 46, 0.2);
  border-bottom: 1px solid rgba(214, 158, 46, 0.2);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  font-size: 2.571rem /* 原值: 36px */;
  line-height: 1;
}

.stat-content {
  text-align: left;
}

.stat-label {
  font-size: 1rem /* 原值: 14px */;
  color: #718096;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 1.429rem /* 原值: 20px */;
  font-weight: 600;
}

.recharge-color {
  color: #48bb78;
}

.consume-color {
  color: #f56565;
}

.withdraw-color {
  color: #4299e1;
}

.wallet-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  flex-wrap: wrap;
}

.wallet-actions .el-button {
  min-width: 120px;
  font-size: 1.143rem /* 原值: 16px */;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
}

.wallet-actions .btn-icon {
  margin-right: 5px;
  font-size: 1.286rem /* 原值: 18px */;
}

.quick-actions-card {
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  border: none;
}

.card-title {
  font-size: 1.286rem /* 原值: 18px */;
  margin: 0 0 20px 0;
  font-weight: 700;
  color: #2d3748;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 15px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #ebf8ff 0%, #bee3f8 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.quick-action-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(66, 153, 225, 0.3);
  border-color: #4299e1;
}

.action-icon {
  font-size: 2.286rem /* 原值: 32px */;
  margin-bottom: 10px;
}

.action-text {
  font-size: 1rem /* 原值: 14px */;
  font-weight: 600;
  color: #2b6cb0;
  margin-bottom: 4px;
}

.action-desc {
  font-size: 0.857rem /* 原值: 12px */;
  color: #718096;
  text-align: center;
  line-height: 1.4;
}

.recent-transactions-card {
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: none;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.transaction-item:hover {
  background: #edf2f7;
  transform: translateX(4px);
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
}

.transaction-type {
  font-size: 1.071rem /* 原值: 15px */;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px;
}

.transaction-time {
  font-size: 0.929rem /* 原值: 13px */;
  color: #a0aec0;
}

.transaction-amount {
  font-size: 1.286rem /* 原值: 18px */;
  font-weight: 600;
}

.amount-income {
  color: #48bb78;
}

.amount-expense {
  color: #f56565;
}

.quick-amount-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.balance-highlight {
  color: #d69e2e;
  font-weight: 600;
  font-size: 1.143rem /* 原值: 16px */;
}

.fee-text {
  color: #f56565;
  font-weight: 500;
  font-size: 1.071rem /* 原值: 15px */;
}

.actual-amount {
  color: #48bb78;
  font-weight: 600;
  font-size: 1.143rem /* 原值: 16px */;
}
</style>
