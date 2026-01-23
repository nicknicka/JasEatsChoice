<template>
  <div class="error-boundary">
    <slot v-if="!hasError" />
    <div v-else class="error-fallback">
      <el-icon class="error-icon" :size="64"><CircleClose /></el-icon>
      <h3 class="error-title">{{ title }}</h3>
      <p class="error-message">{{ errorMessage }}</p>
      <div class="error-actions">
        <el-button type="primary" @click="retry">
          <el-icon><Refresh /></el-icon>
          重试
        </el-button>
        <el-button @click="goBack" v-if="showBack">
          <el-icon><Back /></el-icon>
          返回
        </el-button>
        <el-button type="info" @click="copyError" v-if="showCopy">
          复制错误信息
        </el-button>
      </div>
      <el-collapse v-if="showDetails" class="error-details">
        <el-collapse-item title="错误详情" name="details">
          <pre class="error-stack">{{ errorStack }}</pre>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleClose, Refresh, Back } from '@element-plus/icons-vue'

const props = defineProps({
  title: {
    type: String,
    default: '出错了'
  },
  errorMessage: {
    type: String,
    default: '页面遇到一些问题，请稍后再试'
  },
  showBack: {
    type: Boolean,
    default: true
  },
  showCopy: {
    type: Boolean,
    default: true
  },
  showDetails: {
    type: Boolean,
    default: false
  },
  onError: {
    type: Function,
    default: null
  }
})

const emit = defineEmits(['error', 'retry'])

const router = useRouter()
const hasError = ref(false)
const errorStack = ref('')

const retry = () => {
  hasError.value = false
  emit('retry')
}

const goBack = () => {
  router.back()
}

const copyError = async () => {
  const errorText = `错误信息: ${props.errorMessage}\n\n错误堆栈:\n${errorStack.value}`

  try {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(errorText)
      ElMessage.success('错误信息已复制到剪贴板')
    } else {
      // 降级方案
      const textArea = document.createElement('textarea')
      textArea.value = errorText
      textArea.style.position = 'fixed'
      textArea.style.opacity = '0'
      document.body.appendChild(textArea)
      textArea.select()
      document.execCommand('copy')
      document.body.removeChild(textArea)
      ElMessage.success('错误信息已复制到剪贴板')
    }
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

// 捕获子组件错误
onErrorCaptured((error, instance, info) => {
  console.error('ErrorBoundary caught an error:', error)
  console.error('Error info:', info)

  hasError.value = true
  errorStack.value = error.stack || error.toString()

  // 调用自定义错误处理函数
  if (props.onError) {
    props.onError(error, instance, info)
  }

  // 触发错误事件
  emit('error', error, instance, info)

  // 阻止错误继续向上传播
  return false
})
</script>

<style scoped lang="less">
.error-boundary {
  width: 100%;
  height: 100%;
}

.error-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 20px;
  text-align: center;

  .error-icon {
    color: #f56c6c;
    margin-bottom: 20px;
  }

  .error-title {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 12px 0;
  }

  .error-message {
    font-size: 14px;
    color: #606266;
    margin: 0 0 32px 0;
    max-width: 500px;
  }

  .error-actions {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;
  }

  .error-details {
    width: 100%;
    max-width: 600px;

    .error-stack {
      background: #f5f7fa;
      border-radius: 8px;
      padding: 16px;
      font-size: 12px;
      color: #606266;
      text-align: left;
      overflow-x: auto;
      margin: 0;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
}
</style>
