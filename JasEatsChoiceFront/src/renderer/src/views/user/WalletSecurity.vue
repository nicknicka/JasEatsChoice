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

<style scoped>
.wallet-security-container {
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

.security-level-card {
  border-radius: 16px;
  margin-bottom: 20px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.security-level-content {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.level-info {
  flex: 1;
}

.level-title {
  font-size: 1.143rem /* 原值: 16px */;
  margin-bottom: 10px;
  opacity: 0.9;
}

.level-value {
  margin-bottom: 10px;
}

.level-desc {
  font-size: 1rem /* 原值: 14px */;
  opacity: 0.8;
}

.security-tips {
  flex: 1;
}

.security-tips h4 {
  margin: 0 0 10px 0;
  font-size: 1rem /* 原值: 14px */;
  opacity: 0.9;
}

.security-tips ul {
  margin: 0;
  padding-left: 20px;
}

.security-tips li {
  font-size: 0.929rem /* 原值: 13px */;
  line-height: 1.8;
  opacity: 0.85;
}

.security-section-card {
  border-radius: 16px;
  margin-bottom: 20px;
  border: none;
}

.section-title {
  font-size: 1.286rem /* 原值: 18px */;
  margin: 0 0 20px 0;
  font-weight: 700;
  color: #2d3748;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.security-item:hover {
  background: #edf2f7;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.item-icon {
  font-size: 2.286rem /* 原值: 32px */;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 10px;
}

.item-content {
  flex: 1;
}

.item-name {
  font-size: 1.071rem /* 原值: 15px */;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 5px;
}

.item-status {
  font-size: 0.929rem /* 原值: 13px */;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-set {
  color: #67c23a;
  background: #f0f9ff;
}

.status-unset {
  color: #f56c6c;
  background: #fef2f2;
}

.item-desc {
  font-size: 0.929rem /* 原值: 13px */;
  color: #718096;
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
  background: #f8fafc;
  border-radius: 10px;
}

.log-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.429rem /* 原值: 20px */;
  background: white;
}

.icon-login {
  background: linear-gradient(135deg, #bee3f8 0%, #90cdf4 100%);
}

.icon-payment {
  background: linear-gradient(135deg, #c6f6d5 0%, #9ae6b4 100%);
}

.icon-password {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}

.icon-withdraw {
  background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%);
}

.log-info {
  flex: 1;
}

.log-action {
  font-size: 1rem /* 原值: 14px */;
  font-weight: 500;
  color: #2d3748;
  margin-bottom: 4px;
}

.log-time {
  font-size: 0.857rem /* 原值: 12px */;
  color: #a0aec0;
}

.log-result {
  font-size: 0.929rem /* 原值: 13px */;
  padding: 4px 10px;
  border-radius: 4px;
  font-weight: 500;
}

.log-result.success {
  color: #67c23a;
  background: #f0f9ff;
}

.log-result.failed {
  color: #f56c6c;
  background: #fef2f2;
}
</style>
