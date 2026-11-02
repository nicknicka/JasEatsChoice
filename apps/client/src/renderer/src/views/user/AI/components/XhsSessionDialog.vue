<template>
  <el-dialog
    v-model="dialogVisible"
    title="绑定小红书登录态"
    width="560px"
    @close="handleClose"
  >
    <div class="xhs-session-dialog">
      <el-alert type="warning" :closable="false" show-icon class="risk-alert">
        <p>登录态会保存在服务端，仅用于抓取小红书内容，可随时解绑。</p>
        <p>自动化读取平台内容存在账号风控风险，请知悉后再绑定。</p>
      </el-alert>

      <el-alert
        v-if="status === 'expired'"
        type="error"
        :closable="false"
        show-icon
        class="expired-alert"
      >
        <p>登录态已失效，请重新绑定。</p>
      </el-alert>

      <div class="session-status">
        当前状态：
        <span class="xhs-status" :class="{ bound, expired: status === 'expired' }">
          {{ status === 'expired' ? '已失效' : bound ? '已绑定' : '未绑定' }}
        </span>
        <span v-if="bound && updatedAt" class="updated-at">（更新于 {{ formatUpdatedAt(updatedAt) }}）</span>
      </div>

      <el-input
        v-model="cookie"
        type="textarea"
        :rows="4"
        placeholder="粘贴小红书登录态 Cookie（需包含 web_session）"
      />

      <el-checkbox v-model="confirmed" class="confirm-checkbox">
        我已了解：登录态保存在服务端用于抓取内容，可随时解绑；自动化读取平台内容存在账号风控风险。
      </el-checkbox>

      <div v-if="xhsSupported" class="xhs-login-entry">
        <el-button
          type="primary"
          :loading="opening"
          :disabled="!confirmed"
          @click="handleOpenLoginWindow"
        >
          打开小红书登录窗口
        </el-button>
        <span class="xhs-login-hint">也可打开官方登录窗口，登录成功后自动完成绑定</span>
      </div>
    </div>

    <template #footer>
      <el-button v-if="bound" :loading="unbinding" @click="handleUnbind">解绑</el-button>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        :disabled="!confirmed || !cookie.trim()"
        @click="handleBind"
      >
        绑定并重试
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import contentExtractionApi from '@/api/contentExtraction'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const bound = ref(false)
const status = ref('none')
const updatedAt = ref('')
const cookie = ref('')
const confirmed = ref(false)
const submitting = ref(false)
const unbinding = ref(false)
const opening = ref(false)

const xhsSupported = computed(() => window.api?.xhs?.supported === true)

const isOk = (response) => response.code === '200' || response.code === 200

const formatUpdatedAt = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

const loadStatus = async () => {
  try {
    const response = await contentExtractionApi.getXhsSession()
    if (isOk(response)) {
      bound.value = !!response.data?.bound
      status.value = response.data?.status || 'none'
      updatedAt.value = response.data?.updatedAt || ''
    }
  } catch (error) {
    console.error('读取小红书登录态失败:', error)
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    cookie.value = ''
    confirmed.value = false
    loadStatus()
  }
})

const handleBind = async () => {
  const value = cookie.value.trim()
  if (!value || !confirmed.value) return

  submitting.value = true
  try {
    const response = await contentExtractionApi.bindXhsSession(value)
    if (isOk(response)) {
      ElMessage.success('绑定成功，正在重新提取')
      emit('success')
      handleClose()
    } else {
      ElMessage.error(response.message || '绑定失败，请检查登录态')
    }
  } catch (error) {
    const backendMessage = error?.data?.message || error?.response?.data?.message
    ElMessage.error(backendMessage || error?.message || '绑定失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleOpenLoginWindow = async () => {
  if (!xhsSupported.value || opening.value) return

  opening.value = true
  try {
    const result = await window.api.xhs.login()
    if (!result || result.canceled) {
      ElMessage.info('已取消小红书登录')
      return
    }
    if (!result.cookie) {
      ElMessage.error('未获取到小红书登录态，请重试')
      return
    }
    // 复用已有绑定逻辑：填好 Cookie 后直接提交，成功后照常自动重试
    cookie.value = result.cookie
    await handleBind()
  } catch (error) {
    ElMessage.error(error?.message || '打开小红书登录窗口失败')
  } finally {
    opening.value = false
  }
}

const handleUnbind = async () => {
  unbinding.value = true
  try {
    const response = await contentExtractionApi.unbindXhsSession()
    if (isOk(response)) {
      bound.value = false
      status.value = 'none'
      updatedAt.value = ''
      if (xhsSupported.value) {
        try {
          await window.api.xhs.clear()
        } catch (error) {
          console.error('清理本地小红书登录态失败:', error)
        }
      }
      ElMessage.success('已解绑小红书登录态')
    } else {
      ElMessage.error(response.message || '解绑失败')
    }
  } catch (error) {
    const backendMessage = error?.data?.message || error?.response?.data?.message
    ElMessage.error(backendMessage || error?.message || '解绑失败，请稍后重试')
  } finally {
    unbinding.value = false
  }
}

const handleClose = () => {
  emit('update:visible', false)
}
</script>

<style scoped>
.xhs-session-dialog {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.risk-alert p {
  margin: 0;
  line-height: 1.6;
}

.session-status {
  font-size: 0.929rem /* 原值: 13px */;
  color: #606266;
}

.xhs-status {
  font-weight: 600;
  color: #909399;
}

.xhs-status.bound {
  color: #07c160;
}

.xhs-status.expired {
  color: #f56c6c;
}

.expired-alert p {
  margin: 0;
  line-height: 1.6;
}

.xhs-login-entry {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.xhs-login-hint {
  font-size: 0.857rem /* 原值: 12px */;
  color: #909399;
  line-height: 1.5;
}

.updated-at {
  color: #909399;
  font-size: 0.857rem /* 原值: 12px */;
}

.confirm-checkbox {
  align-items: flex-start;
  white-space: normal;
}

.confirm-checkbox :deep(.el-checkbox__label) {
  white-space: normal;
  line-height: 1.6;
}
</style>
