<template>
  <div class="content-extraction-dialog">
    <el-dialog
      v-model="dialogVisible"
      title="从视频/文章提取菜品"
      width="600px"
      @close="handleClose"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="内容链接" prop="contentUrl">
          <el-input
            v-model="form.contentUrl"
            placeholder="请输入抖音、小红书、B站、微信公众号等平台的内容链接"
            type="textarea"
            :rows="3"
          />
          <div class="url-tips">
            支持平台：抖音、小红书、哔哩哔哩、微信公众号、今日头条、快手等
          </div>
        </el-form-item>

        <el-form-item label="内容类型" prop="contentType">
          <el-select v-model="form.contentType" placeholder="自动识别" disabled>
            <el-option label="视频" value="VIDEO" />
            <el-option label="文章" value="ARTICLE" />
            <el-option label="图片" value="IMAGE" />
          </el-select>
          <span style="margin-left: 8px; color: #909399; font-size: 0.929rem /* 原值: 13px */">
            系统会自动识别
          </span>
        </el-form-item>
      </el-form>

      <div class="platform-info" v-if="detectedPlatform">
        <el-icon><Platform /></el-icon>
        <span>检测到平台：{{ detectedPlatform }}</span>
      </div>

      <template #footer>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          开始提取
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Platform } from '@element-plus/icons-vue'
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

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  contentUrl: '',
  contentType: 'VIDEO'
})

const normalizeContentUrl = (value) => {
  return typeof value === 'string' ? value.trim() : ''
}

const validateContentUrl = (_rule, value, callback) => {
  const url = normalizeContentUrl(value)

  if (!url) {
    callback(new Error('请输入内容链接'))
    return
  }

  // 放宽校验，兼容短链和部分平台分享链接格式
  const looseUrlPattern = /^(https?:\/\/)?([\w-]+\.)+[\w-]+([/?#].*)?$/i
  if (!looseUrlPattern.test(url)) {
    callback(new Error('请输入有效的内容链接'))
    return
  }

  callback()
}

const isValidationError = (error) => {
  return !!error &&
    typeof error === 'object' &&
    !('status' in error) &&
    Object.values(error).some((item) => Array.isArray(item))
}

const rules = {
  contentUrl: [
    { validator: validateContentUrl, trigger: 'blur' }
  ]
}

// 检测平台
const detectedPlatform = computed(() => {
  if (!form.contentUrl) return ''

  const url = form.contentUrl.toLowerCase()
  if (url.includes('douyin.com')) return '抖音'
  if (url.includes('xiaohongshu.com')) return '小红书'
  if (url.includes('bilibili.com')) return '哔哩哔哩'
  if (url.includes('mp.weixin.qq.com') || url.includes('weixin.qq.com')) return '微信'
  if (url.includes('toutiao.com')) return '今日头条'
  if (url.includes('kuaishou.com')) return '快手'

  return '其他平台'
})

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitting.value = true
    const contentUrl = normalizeContentUrl(form.contentUrl)
    form.contentUrl = contentUrl

    const response = await contentExtractionApi.createSource({
      contentUrl
    })

    // 修复：API 响应拦截器已经返回 response.data，所以直接检查 response.code
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('添加成功，系统正在后台提取中...')
      emit('success', response.data)
      handleClose()
    } else {
      ElMessage.error(response.message || '添加失败，请稍后重试')
    }
  } catch (error) {
    if (isValidationError(error) || error === false) {
      return
    }

    console.error('添加失败:', error)
    const backendMessage = error?.data?.message || error?.response?.data?.message
    ElMessage.error(backendMessage || error?.message || '添加失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  form.contentUrl = ''
  form.contentType = 'VIDEO'
  emit('update:visible', false)
}
</script>

<style scoped>
.url-tips {
  margin-top: 8px;
  font-size: 0.857rem /* 原值: 12px */;
  color: #909399;
}

.platform-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 1rem /* 原值: 14px */;
}
</style>
