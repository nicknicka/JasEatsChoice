<template>
  <div class="wallet-security-page">
    <!-- 页头：标准版；「返回」收进右侧动作区（§4.7） -->
    <AppPageHeader title="钱包安全设置">
      <template #actions>
        <CommonBackButton
          type="default"
          size="large"
          :use-router-back="false"
          @click="goBack"
        />
      </template>
    </AppPageHeader>

    <!-- 安全等级：数值、首要风险和直达动作放在同一张卡内（P0，§4.1、§4.2） -->
    <AppCard class="security-level-card" padding="lg">
      <div class="security-level-content">
        <div class="level-info">
          <div class="level-label">安全等级</div>
          <div class="level-value">
            <el-rate
              v-model="securityLevel"
              disabled
              show-score
              :colors="rateColors"
              score-template="{value}"
            />
          </div>
          <div class="level-desc">{{ getSecurityLevelText() }}</div>
        </div>

        <div class="risk-panel" :class="`risk-panel--${primaryRisk.tone}`">
          <StatusTag
            class="risk-status"
            :status="primaryRisk.status"
            :text="primaryRisk.statusText"
            size="sm"
          />
          <div class="risk-text">{{ primaryRisk.text }}</div>
        </div>

        <div class="risk-action">
          <el-button
            size="large"
            :type="primaryRisk.actionType"
            @click="handlePrimaryRiskAction"
          >
            {{ primaryRisk.actionText }}
          </el-button>
        </div>
      </div>
    </AppCard>

    <!-- 密码管理 -->
    <AppCard class="security-section-card" padding="md">
      <h2 class="section-title">密码管理</h2>
      <div class="security-items">
        <div class="security-item">
          <div class="item-info">
            <div class="item-icon">🔒</div>
            <div class="item-content">
              <div class="item-name">支付密码</div>
              <StatusTag
                class="item-status"
                :status="hasPaymentPassword ? 'success' : 'failed'"
                :text="hasPaymentPassword ? '已设置' : '未设置'"
                size="sm"
              />
            </div>
          </div>
          <div class="item-action">
            <el-button type="default" size="default" @click="setupPaymentPassword">
              {{ hasPaymentPassword ? '修改' : '设置' }}
            </el-button>
          </div>
        </div>
      </div>
    </AppCard>

    <!-- 钱包状态 -->
    <AppCard class="security-section-card" padding="md">
      <h2 class="section-title">钱包状态</h2>
      <div class="security-items">
        <div class="security-item">
          <div class="item-info">
            <div class="item-icon">🔐</div>
            <div class="item-content">
              <div class="item-name">钱包锁定</div>
              <StatusTag
                class="item-state"
                :status="walletLocked ? 'locked' : 'normal'"
                :text="walletLocked ? '已锁定' : '正常'"
                size="sm"
              />
              <div class="item-desc">锁定后无法进行支付、提现等操作</div>
            </div>
          </div>
          <div class="item-action">
            <el-switch
              v-model="walletLocked"
              aria-label="钱包锁定"
              @change="handleWalletLockChange"
            />
          </div>
        </div>
      </div>
    </AppCard>

    <!-- 交易设置 -->
    <AppCard class="security-section-card" padding="md">
      <h2 class="section-title">交易设置</h2>
      <div class="security-items">
        <div class="security-item">
          <div class="item-info">
            <div class="item-icon">💰</div>
            <div class="item-content">
              <div class="item-name">单笔交易限额</div>
              <StatusTag
                class="item-state"
                status="neutral"
                :text="`当前限额：${dailyLimit} 平台币`"
                size="sm"
              />
            </div>
          </div>
          <div class="item-action">
            <el-button type="default" size="default" @click="showLimitDialog = true">
              修改
            </el-button>
          </div>
        </div>
        <div class="security-item">
          <div class="item-info">
            <div class="item-icon">📱</div>
            <div class="item-content">
              <div class="item-name">支付验证</div>
              <StatusTag
                class="item-state"
                :status="needVerify ? 'enabled' : 'neutral'"
                :text="needVerify ? '已开启' : '已关闭'"
                size="sm"
              />
              <div class="item-desc">超过{{ verifyAmount }}平台币需要输入支付密码</div>
            </div>
          </div>
          <div class="item-action">
            <el-switch v-model="needVerify" aria-label="支付验证" @change="handleVerifyChange" />
          </div>
        </div>
      </div>
    </AppCard>

    <!-- 安全日志 -->
    <AppCard class="security-section-card" padding="md">
      <div class="section-header">
        <h2 class="section-title">安全日志</h2>
        <el-button link size="small" @click="viewAllLogs">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      <div v-if="recentLogs.length > 0" class="logs-list">
        <div v-for="log in recentLogs" :key="log.id" class="log-item">
          <div class="log-icon" :class="getLogIconClass(log.type)">
            {{ getLogIcon(log.type) }}
          </div>
          <div class="log-info">
            <div class="log-action">{{ log.action }}</div>
            <div class="log-time">{{ formatTime(log.time) }}</div>
          </div>
          <StatusTag
            class="log-result"
            :status="log.success ? 'success' : 'failed'"
            :text="log.success ? '成功' : '失败'"
            size="sm"
          />
        </div>
      </div>
      <AppEmptyState v-else description="暂无安全日志" />
    </AppCard>

    <!-- 限额设置对话框 -->
    <el-dialog v-model="showLimitDialog" title="设置交易限额" width="400px" center>
      <el-form :model="limitForm" label-width="100px">
        <el-form-item label="单日限额">
          <el-input-number
            v-model="limitForm.dailyLimit"
            :min="0"
            :max="999999"
            :step="100"
            :precision="0"
          />
        </el-form-item>
        <el-form-item label="单笔限额">
          <el-input-number
            v-model="limitForm.singleLimit"
            :min="0"
            :max="999999"
            :step="100"
            :precision="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showLimitDialog = false">取消</el-button>
        <el-button type="primary" size="large" @click="saveLimit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import AppCard from '../../components/common/AppCard.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import StatusTag from '../../components/common/StatusTag.vue'
import walletApi from '../../api/wallet'
import paymentApi from '../../api/payment'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// 安全等级
const securityLevel = ref(3)
const hasPaymentPassword = ref(false)
const walletLocked = ref(false)
const dailyLimit = ref(5000)
const verifyAmount = ref(500)
const needVerify = ref(true)
const rateColors = ['var(--danger)', 'var(--warning)', 'var(--success)']

// 当前最需要处理的风险项：先密码，再验证，再限额；全部完成时回到正向提示（P0）
const primaryRisk = computed(() => {
  if (!hasPaymentPassword.value) {
    return {
      key: 'payment',
      tone: 'danger',
      status: 'failed',
      statusText: '未设置',
      text: '建议设置支付密码以提高账户安全性',
      actionText: '设置',
      actionType: 'primary'
    }
  }
  if (!needVerify.value) {
    return {
      key: 'verify',
      tone: 'warning',
      status: 'warning',
      statusText: '未开启',
      text: '建议开启大额支付验证功能',
      actionText: '开启',
      actionType: 'primary'
    }
  }
  if (dailyLimit.value > 10000) {
    return {
      key: 'limit',
      tone: 'warning',
      status: 'warning',
      statusText: '待调整',
      text: '建议设置合理的交易限额',
      actionText: '修改',
      actionType: 'primary'
    }
  }
  return {
    key: 'none',
    tone: 'success',
    status: 'success',
    statusText: '良好',
    text: '您的钱包安全设置完善，请继续保持良好的安全习惯',
    actionText: '查看全部',
    actionType: 'default'
  }
})

// 最近日志
const recentLogs = ref([
  {
    id: 1,
    type: 'login',
    action: '登录账户',
    time: new Date(Date.now() - 1000 * 60 * 30),
    success: true
  },
  {
    id: 2,
    type: 'payment',
    action: '钱包支付',
    time: new Date(Date.now() - 1000 * 60 * 60 * 2),
    success: true
  },
  {
    id: 3,
    type: 'password',
    action: '修改支付密码',
    time: new Date(Date.now() - 1000 * 60 * 60 * 24),
    success: true
  }
])

// 限额表单
const showLimitDialog = ref(false)
const limitForm = ref({
  dailyLimit: 5000,
  singleLimit: 1000
})

// 获取安全等级文本
const getSecurityLevelText = () => {
  if (securityLevel.value <= 2) return '安全等级较低，建议加强安全设置'
  if (securityLevel.value <= 4) return '安全等级良好，建议继续完善'
  return '安全等级高，账户安全性强'
}

// 获取日志图标
const getLogIcon = (type) => {
  const icons = {
    login: '🔑',
    payment: '💳',
    password: '🔒',
    withdraw: '🏦'
  }
  return icons[type] || '📄'
}

// 获取日志图标样式
const getLogIconClass = (type) => {
  const classes = {
    login: 'icon-login',
    payment: 'icon-payment',
    password: 'icon-password',
    withdraw: 'icon-withdraw'
  }
  return classes[type] || ''
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 设置支付密码
const setupPaymentPassword = () => {
  router.push('/user/home/payment-password-setup')
}

// 钱包锁定状态改变
const handleWalletLockChange = async (value) => {
  const action = value ? '锁定' : '解锁'
  try {
    await ElMessageBox.confirm(
      `确认${action}钱包吗？${action}后将${value ? '无法' : '可以'}进行支付、提现等操作。`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // 调用后端API更新钱包锁定状态
    const userId = parseInt(authStore.userId || '0', 10)
    if (userId <= 0) {
      ElMessage.error('用户未登录')
      walletLocked.value = !value
      return
    }

    try {
      const response = await walletApi.updateWalletLockStatus(userId, value)
      if (response.code === '200') {
        ElMessage.success(`钱包已${action}`)
        walletLocked.value = value
      } else {
        ElMessage.error(response.message || `${action}失败，请稍后重试`)
        walletLocked.value = !value
      }
    } catch (error) {
      console.error('更新钱包锁定状态失败:', error)
      ElMessage.error(`${action}失败，请稍后重试`)
      walletLocked.value = !value
    }
  } catch {
    // 用户取消操作，恢复开关状态
    walletLocked.value = !value
  }
}

// 支付验证状态改变
const handleVerifyChange = (value) => {
  ElMessage.success(value ? '已开启支付验证' : '已关闭支付验证')
}

// 保存限额
const saveLimit = () => {
  dailyLimit.value = limitForm.value.dailyLimit
  showLimitDialog.value = false
  ElMessage.success('交易限额设置成功')
}

// 查看全部日志
const viewAllLogs = () => {
  // 跳转到钱包页面，查看交易记录（包含安全日志）
  router.push('/user/home/wallet')
}

// 从安全等级卡直达当前风险项对应的现有动作
const handlePrimaryRiskAction = () => {
  if (primaryRisk.value.key === 'payment') {
    setupPaymentPassword()
    return
  }
  if (primaryRisk.value.key === 'verify') {
    needVerify.value = true
    handleVerifyChange(true)
    return
  }
  if (primaryRisk.value.key === 'limit') {
    showLimitDialog.value = true
    return
  }
  viewAllLogs()
}

// 返回
const goBack = () => {
  router.back()
}

// 页面加载时初始化
onMounted(async () => {
  // 初始化限额表单
  limitForm.value.dailyLimit = dailyLimit.value
  limitForm.value.singleLimit = dailyLimit.value / 5

  // 从后端获取安全设置数据
  const userId = parseInt(authStore.userId || '0', 10)
  if (userId > 0) {
    try {
      // 获取钱包安全设置
      const securityResponse = await walletApi.getWalletSecuritySettings(userId)
      if (securityResponse.code === '200' && securityResponse.data) {
        const settings = securityResponse.data
        walletLocked.value = settings.locked || false
        needVerify.value = settings.verifyEnabled !== false
        dailyLimit.value = settings.dailyLimit || 5000

        // 更新限额表单
        limitForm.value.dailyLimit = dailyLimit.value
        limitForm.value.singleLimit = dailyLimit.value / 5
      }

      // 检查是否已设置支付密码
      const passwordResponse = await paymentApi.checkPaymentPassword(String(userId))
      if (passwordResponse.code === '200' && passwordResponse.data) {
        hasPaymentPassword.value = passwordResponse.data.hasPaymentPassword || false
      }
    } catch (error) {
      console.error('获取安全设置失败:', error)
      // 使用默认值
    }
  }
})
</script>

<style scoped lang="less">
/* 页面：单列居中 1200px；颜色、间距、圆角、字阶与动效全部取自 tokens.css（§3、§7.1）。 */
.wallet-security-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-12);
  background: var(--canvas);
}

/* 安全等级卡：等级、风险与动作形成一条明确的修复路径（P0，§4.1、§4.2）。 */
.security-level-card {
  margin-bottom: var(--card-gap);
}

.security-level-content {
  display: grid;
  grid-template-columns: minmax(180px, 0.8fr) minmax(320px, 1.5fr) auto;
  align-items: center;
  gap: var(--space-6);
}

.level-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.level-value {
  margin-top: var(--space-2);
}

.level-value :deep(.el-rate__text) {
  color: var(--ink-700);
  font-family: var(--font-num);
  font-weight: var(--weight-semibold);
}

.level-desc {
  margin-top: var(--space-2);
  color: var(--ink-700);
  font-size: var(--text-body);
  line-height: 22px;
}

/* 风险区只使用语义浅底，不让整张卡铺彩色渐变（§3.1、§3.4）。 */
.risk-panel {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-4);
  border-radius: var(--radius-md);
}

.risk-panel--danger {
  background: var(--danger-bg);
}

.risk-panel--warning {
  background: var(--warning-bg);
}

.risk-panel--success {
  background: var(--success-bg);
}

.risk-text {
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  line-height: 22px;
}

.risk-action {
  display: flex;
  justify-content: flex-end;
}

.risk-action :deep(.el-button) {
  min-width: 96px;
}

/* 设置分组：每组一张 L1 卡，组内统一“状态 + 一个动作”（P0，§3.4、§4.2）。 */
.security-section-card {
  margin-bottom: var(--card-gap);
}

.section-title {
  margin: 0 0 var(--space-5);
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.section-header .section-title {
  margin-bottom: 0;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--canvas-sunken);
  border-radius: var(--radius-md);
  box-shadow: inset 0 0 0 1px transparent;
  transition:
    background-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);
}

.security-item:hover,
.security-item:focus-within {
  background: var(--surface);
  box-shadow: inset 0 0 0 1px var(--line);
}

.item-info {
  display: flex;
  flex: 1;
  align-items: center;
  gap: var(--space-4);
  min-width: 0;
}

.item-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: var(--space-10);
  height: var(--space-10);
  background: var(--surface);
  border-radius: var(--radius-md);
  font-size: var(--text-h2);
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-name {
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
}

.item-status,
.item-state {
  margin-top: var(--space-1);
}

.item-desc {
  margin-top: var(--space-1);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.item-action {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: flex-end;
  min-width: 88px;
}

/* “查看全部”属于文字动作，统一为品牌色，不再出现蓝色链接（P1，§4.1、§7.1）。 */
.section-header :deep(.el-button.is-link) {
  color: var(--brand-600);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.section-header :deep(.el-button.is-link:hover) {
  color: var(--brand-700);
}

/* 安全日志：状态、时间和结果统一成一条紧凑记录（§4.3）。 */
.logs-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.log-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--canvas-sunken);
  border-radius: var(--radius-md);
}

.log-icon {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: var(--space-10);
  height: var(--space-10);
  background: var(--neutral-bg);
  border-radius: var(--radius-md);
  color: var(--neutral);
  font-size: var(--text-h3);
}

.icon-login {
  background: var(--info-bg);
  color: var(--info);
}

.icon-payment {
  background: var(--success-bg);
  color: var(--success);
}

.icon-password {
  background: var(--warning-bg);
  color: var(--warning);
}

.icon-withdraw {
  background: var(--danger-bg);
  color: var(--danger);
}

.log-info {
  flex: 1;
  min-width: 0;
}

.log-action {
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  line-height: 22px;
}

.log-time {
  margin-top: var(--space-1);
  color: var(--ink-500);
  font-size: var(--text-caption);
  line-height: 18px;
}

@media (max-width: 768px) {
  .wallet-security-page {
    padding-inline: var(--space-4);
  }

  .security-level-content {
    grid-template-columns: 1fr;
  }

  .risk-action {
    justify-content: flex-start;
  }
}
</style>
