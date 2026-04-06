<template>
  <div class="wallet-security-container">
    <common-back-button
      type="default"
      size="small"
      @click="goBack"
      :use-router-back="false"
      style="margin-bottom: 20px"
    />

    <h2 class="page-title">钱包安全设置</h2>

    <!-- 安全等级卡片 -->
    <el-card class="security-level-card" shadow="hover">
      <div class="security-level-content">
        <div class="level-info">
          <div class="level-title">安全等级</div>
          <div class="level-value">
            <el-rate
              v-model="securityLevel"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
            />
          </div>
          <div class="level-desc">{{ getSecurityLevelText() }}</div>
        </div>
        <div class="security-tips">
          <h4>安全建议</h4>
          <ul>
            <li v-for="tip in securityTips" :key="tip">{{ tip }}</li>
          </ul>
        </div>
      </div>
    </el-card>

    <!-- 密码管理 -->
    <el-card class="security-section-card" shadow="hover">
      <h3 class="section-title">密码管理</h3>
      <div class="security-items">
        <div class="security-item">
          <div class="item-info">
            <div class="item-icon">🔒</div>
            <div class="item-content">
              <div class="item-name">支付密码</div>
              <div class="item-status" :class="hasPaymentPassword ? 'status-set' : 'status-unset'">
                {{ hasPaymentPassword ? '已设置' : '未设置' }}
              </div>
            </div>
          </div>
          <el-button type="primary" @click="setupPaymentPassword">
            {{ hasPaymentPassword ? '修改' : '设置' }}
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 钱包状态 -->
    <el-card class="security-section-card" shadow="hover">
      <h3 class="section-title">钱包状态</h3>
      <div class="security-items">
        <div class="security-item">
          <div class="item-info">
            <div class="item-icon">🔐</div>
            <div class="item-content">
              <div class="item-name">钱包锁定</div>
              <div class="item-desc">锁定后无法进行支付、提现等操作</div>
            </div>
          </div>
          <el-switch
            v-model="walletLocked"
            active-text="已锁定"
            inactive-text="正常"
            @change="handleWalletLockChange"
          />
        </div>
      </div>
    </el-card>

    <!-- 交易设置 -->
    <el-card class="security-section-card" shadow="hover">
      <h3 class="section-title">交易设置</h3>
      <div class="security-items">
        <div class="security-item">
          <div class="item-info">
            <div class="item-icon">💰</div>
            <div class="item-content">
              <div class="item-name">单笔交易限额</div>
              <div class="item-desc">当前限额：{{ dailyLimit }} 平台币</div>
            </div>
          </div>
          <el-button type="primary" link @click="showLimitDialog = true"> 修改 </el-button>
        </div>
        <div class="security-item">
          <div class="item-info">
            <div class="item-icon">📱</div>
            <div class="item-content">
              <div class="item-name">支付验证</div>
              <div class="item-desc">超过{{ verifyAmount }}平台币需要输入支付密码</div>
            </div>
          </div>
          <el-switch v-model="needVerify" @change="handleVerifyChange" />
        </div>
      </div>
    </el-card>

    <!-- 安全日志 -->
    <el-card class="security-section-card" shadow="hover">
      <div class="section-header">
        <h3 class="section-title">安全日志</h3>
        <el-button type="primary" link @click="viewAllLogs">
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
          <div class="log-result" :class="log.success ? 'success' : 'failed'">
            {{ log.success ? '成功' : '失败' }}
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无安全日志" />
    </el-card>

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
        <el-button @click="showLimitDialog = false">取消</el-button>
        <el-button type="primary" @click="saveLimit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
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

// 安全建议
const securityTips = computed(() => {
  const tips = []
  if (!hasPaymentPassword.value) {
    tips.push('建议设置支付密码以提高账户安全性')
  }
  if (!needVerify.value) {
    tips.push('建议开启大额支付验证功能')
  }
  if (dailyLimit.value > 10000) {
    tips.push('建议设置合理的交易限额')
  }
  if (tips.length === 0) {
    tips.push('您的钱包安全设置完善，请继续保持良好的安全习惯')
  }
  return tips
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
@import '../../assets/css/nordic-theme.less';

.wallet-security-container {
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

.security-level-card {
  border-radius: @nordic-radius-lg;
  margin-bottom: @nordic-space-lg;
  border: none;
  background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
  color: @nordic-surface;
}

.security-level-content {
  padding: @nordic-space-lg;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: @nordic-space-lg;
}

.level-info {
  flex: 1;
}

.level-title {
  font-size: @nordic-text-md;
  margin-bottom: @nordic-space-sm;
  opacity: 0.9;
}

.level-value {
  margin-bottom: @nordic-space-sm;
}

.level-desc {
  font-size: @nordic-text-base;
  opacity: 0.8;
}

.security-tips {
  flex: 1;
}

.security-tips h4 {
  margin: 0 0 @nordic-space-sm 0;
  font-size: @nordic-text-base;
  opacity: 0.9;
}

.security-tips ul {
  margin: 0;
  padding-left: @nordic-space-lg;
}

.security-tips li {
  font-size: @nordic-text-sm;
  line-height: 1.8;
  opacity: 0.85;
}

.security-section-card {
  border-radius: @nordic-radius-lg;
  margin-bottom: @nordic-space-lg;
  border: none;
}

.section-title {
  font-size: @nordic-text-md + 2px;
  margin: 0 0 @nordic-space-lg 0;
  font-weight: 700;
  color: @nordic-text;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: @nordic-space-lg;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: @nordic-space-md;
  background: @nordic-divider;
  border-radius: @nordic-radius-md;
  transition: all @nordic-transition-fast ease;
}

.security-item:hover {
  background: @nordic-border;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.item-icon {
  font-size: 2.286rem;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: @nordic-surface;
  border-radius: @nordic-radius-sm;
}

.item-content {
  flex: 1;
}

.item-name {
  font-size: 1.071rem;
  font-weight: 600;
  color: @nordic-text;
  margin-bottom: 5px;
}

.item-status {
  font-size: @nordic-text-sm;
  display: inline-block;
  padding: 2px @nordic-space-sm;
  border-radius: @nordic-radius-xs;
}

.status-set {
  color: @nordic-green;
  background: @nordic-green-light;
}

.status-unset {
  color: @nordic-red;
  background: @nordic-red-light;
}

.item-desc {
  font-size: @nordic-text-sm;
  color: @nordic-text-secondary;
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: @nordic-divider;
  border-radius: @nordic-radius-sm;
}

.log-icon {
  width: 40px;
  height: 40px;
  border-radius: @nordic-radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.429rem;
  background: @nordic-surface;
}

.icon-login {
  background: linear-gradient(135deg, @nordic-blue-light 0%, darken(@nordic-blue-light, 5%) 100%);
}

.icon-payment {
  background: linear-gradient(135deg, @nordic-green-light 0%, darken(@nordic-green-light, 5%) 100%);
}

.icon-password {
  background: linear-gradient(135deg, @nordic-yellow-light 0%, darken(@nordic-yellow-light, 5%) 100%);
}

.icon-withdraw {
  background: linear-gradient(135deg, @nordic-red-light 0%, darken(@nordic-red-light, 5%) 100%);
}

.log-info {
  flex: 1;
}

.log-action {
  font-size: @nordic-text-base;
  font-weight: 500;
  color: @nordic-text;
  margin-bottom: 4px;
}

.log-time {
  font-size: @nordic-text-xs;
  color: @nordic-text-muted;
}

.log-result {
  font-size: @nordic-text-sm;
  padding: 4px 10px;
  border-radius: @nordic-radius-xs;
  font-weight: 500;
}

.log-result.success {
  color: @nordic-green;
  background: @nordic-green-light;
}

.log-result.failed {
  color: @nordic-red;
  background: @nordic-red-light;
}
</style>
