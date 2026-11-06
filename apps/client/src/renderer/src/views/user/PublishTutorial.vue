<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { VideoCamera, Document, Upload, View, Edit, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const router = useRouter()
const route = useRoute()

// 表单数据
const tutorialForm = ref({
  id: null,
  title: '',
  type: 'article',
  content: '',
  difficulty: 'BEGINNER',
  duration: '',
  calories: null,
  prep_time: '',
  servings: null,
  cover_image: '',
  video_url: '', // 视频URL（视频教程专用）
  tags: null, // 改为 null，避免空字符串导致数据库 JSON 字段报错
  status: 'DRAFT',
  review_status: 'NOT_SUBMITTED'
})

// 提交状态
const submitting = ref(false)

// 图片上传相关
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadDialogVisible = ref(false)
const uploadPreviewUrl = ref('')

// 视频上传相关
const uploadingVideo = ref(false)
const videoUploadProgress = ref(0)
const videoPreviewDialogVisible = ref(false)
const videoPreviewUrl = ref('')
const videoFileInfo = ref(null) // 保存视频文件信息
const videoPlayerRef = ref(null) // 视频播放器引用

// 内容图片上传相关
const uploadingContentImage = ref(false)
const contentImageUploadProgress = ref(0)
const contentImageDialogVisible = ref(false)
const uploadedContentImageUrl = ref('')

// Markdown 预览模式：'edit' | 'preview' | 'split'
const previewMode = ref('edit')

// Textarea引用
const contentTextarea = ref(null)

// 完成度：标题 / 内容 / 封面必填，视频教程额外要求视频
const completionItems = computed(() => {
  const form = tutorialForm.value
  const items = [
    { label: '标题', done: !!form.title },
    { label: '内容', done: !!form.content },
    { label: '封面', done: !!form.cover_image }
  ]
  if (form.type === 'video') {
    items.push({ label: '视频', done: !!form.video_url })
  }
  return items
})

const completionPercent = computed(() => {
  const items = completionItems.value
  const done = items.filter(item => item.done).length
  return Math.round((done / items.length) * 100)
})

// 自动调整textarea高度
const autoResizeTextarea = () => {
  const textarea = contentTextarea.value
  if (!textarea) return

  // 重置高度以获取正确的scrollHeight
  textarea.style.height = 'auto'

  // 计算新高度（基于内容）
  const scrollHeight = textarea.scrollHeight

  // 设置最小高度为3行（约72px，24px每行）
  const minHeight = 72

  // 设置新高度
  textarea.style.height = Math.max(minHeight, scrollHeight) + 'px'
}

// 监听内容变化
const handleContentInput = () => {
  autoResizeTextarea()
}

// 快速模板
const quickTemplates = [
  {
    title: '我的拿手菜',
    type: 'video',
    difficulty: 'INTERMEDIATE',
    contentTemplate: '## 菜品介绍\n\n### 食材准备\n\n### 制作步骤\n\n### 小贴士'
  },
  {
    title: '健康饮食心得',
    type: 'article',
    difficulty: 'BEGINNER',
    contentTemplate: '## 饮食心得\n\n### 推荐搭配\n\n### 营养建议'
  },
  {
    title: '快手食谱分享',
    type: 'video',
    difficulty: 'BEGINNER',
    contentTemplate: '## 食谱名称\n\n### 食材\n\n### 步骤\n\n### 注意事项'
  }
]

// 切换预览模式
const togglePreviewMode = (mode) => {
  previewMode.value = mode
}

// 简单的 Markdown 转 HTML
const renderMarkdown = (content) => {
  if (!content) return ''

  let html = content
    // 图片（需要在标题、粗体等之前处理）
    .replace(/!\[([^\]]*)\]\(([^)]+)\)/gim, '<img src="$2" alt="$1" class="markdown-image" />')
    // 标题
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    // 粗体
    .replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
    // 斜体
    .replace(/\*(.*?)\*/gim, '<em>$1</em>')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/gim, '<a href="$2" target="_blank">$1</a>')
    // 列表
    .replace(/^- (.*$)/gim, '<li>$1</li>')
    .replace(/(<li>.*<\/li>)/gim, '<ul>$1</ul>')
    // 换行
    .replace(/\n/gim, '<br>')

  return html
}

// 使用快速模板
const useTemplate = (template) => {
  tutorialForm.value.title = template.title
  tutorialForm.value.type = template.type
  tutorialForm.value.difficulty = template.difficulty
  tutorialForm.value.content = template.contentTemplate
  ElMessage.success('模板已应用，请填写具体内容')
}

// 提交教程
const submitTutorial = async () => {
  console.log('submitTutorial 被调用')

  // 验证表单
  if (!tutorialForm.value.title) {
    ElMessage.warning('请输入教程标题')
    return
  }
  if (!tutorialForm.value.content) {
    ElMessage.warning('请输入教程内容')
    return
  }

  submitting.value = true

  try {
    // 先保存教程（创建或更新）
    // 构建请求数据，将前端字段名转换为后端期望的字段名
    const data = {
      id: tutorialForm.value.id,
      title: tutorialForm.value.title,
      type: tutorialForm.value.type,
      content: tutorialForm.value.content,
      difficulty: tutorialForm.value.difficulty,
      duration: tutorialForm.value.duration,
      calories: tutorialForm.value.calories,
      prepTime: tutorialForm.value.prep_time,
      servings: tutorialForm.value.servings,
      coverImage: tutorialForm.value.cover_image, // 转换为驼峰命名
      videoUrl: tutorialForm.value.video_url, // 转换为驼峰命名
      tags: tutorialForm.value.tags,
      status: 'DRAFT',
      reviewStatus: 'NOT_SUBMITTED' // 转换为驼峰命名
    }

    let tutorialId = tutorialForm.value.id
    console.log('教程ID:', tutorialId)

    if (tutorialId) {
      // 更新现有教程
      console.log('更新现有教程')
      await api.put(`${API_CONFIG.tutorial.userUpdate}/${tutorialId}`, data)
    } else {
      // 创建新教程
      console.log('创建新教程，请求数据:', data)
      const createResponse = await api.post(API_CONFIG.tutorial.userCreate, data)
      console.log('创建响应完整内容:', createResponse)
      console.log('创建响应的data:', createResponse.data)

      if (!createResponse) {
        throw new Error('创建教程失败：服务器未返回数据')
      }

      tutorialId = createResponse.id || createResponse.data?.id
      if (!tutorialId) {
        console.error('无法从响应中获取教程ID，响应内容:', createResponse)
        throw new Error('创建教程失败：无法获取教程ID')
      }

      tutorialForm.value.id = tutorialId
      console.log('创建成功，教程ID:', tutorialId)
    }

    // 然后调用专门的提交审核接口
    console.log('提交审核，教程ID:', tutorialId)
    const submitResponse = await api.post(`${API_CONFIG.tutorial.userSubmit}/${tutorialId}/submit`)
    console.log('提交审核响应:', submitResponse)

    if (submitResponse && submitResponse.success) {
      tutorialForm.value.status = 'PENDING'
      tutorialForm.value.review_status = 'PENDING'

      ElMessage.success('提交成功！您的教程已进入审核队列')
    } else {
      throw new Error(submitResponse?.message || '提交失败')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 保存为草稿
const saveDraft = async () => {
  console.log('=== saveDraft 开始执行 ===')

  if (!tutorialForm.value.title) {
    ElMessage.warning('请至少输入教程标题')
    return
  }

  submitting.value = true
  console.log('当前教程ID:', tutorialForm.value.id)

  try {
    // 构建请求数据，将前端字段名转换为后端期望的字段名
    const data = {
      id: tutorialForm.value.id,
      title: tutorialForm.value.title,
      type: tutorialForm.value.type,
      content: tutorialForm.value.content,
      difficulty: tutorialForm.value.difficulty,
      duration: tutorialForm.value.duration,
      calories: tutorialForm.value.calories,
      // 将下划线命名转换为驼峰命名
      prepTime: tutorialForm.value.prep_time,
      servings: tutorialForm.value.servings,
      coverImage: tutorialForm.value.cover_image, // 转换为驼峰命名
      videoUrl: tutorialForm.value.video_url, // 转换为驼峰命名
      tags: tutorialForm.value.tags,
      status: 'DRAFT',
      reviewStatus: 'NOT_SUBMITTED' // 转换为驼峰命名
    }

    console.log('请求数据（转换后）:', data)

    let response
    if (tutorialForm.value.id) {
      // 更新现有草稿
      console.log('更新现有草稿，URL:', `${API_CONFIG.tutorial.userUpdate}/${tutorialForm.value.id}`)
      response = await api.put(`${API_CONFIG.tutorial.userUpdate}/${tutorialForm.value.id}`, data)
    } else {
      // 创建新草稿
      console.log('创建新草稿，URL:', API_CONFIG.tutorial.userCreate)
      response = await api.post(API_CONFIG.tutorial.userCreate, data)
    }

    console.log('API响应完整内容:', response)
    console.log('response.data:', response.data)
    console.log('response.status:', response.status)

    // 判断响应是否成功：
    // 1. 创建操作：response本身是教程对象（有id）
    // 2. 更新操作：response包含success=true或message字段
    // 3. 标准格式：response包含data或status字段
    const isSuccess = response && (
      (response.success === true) ||                          // 操作成功标识
      (response.message && response.message.includes('成功')) || // 成功消息
      (response.data && typeof response.data === 'object') || // response.data存在
      (response.id && typeof response.id === 'string') ||      // response本身是教程对象
      response.status === 200 ||                                // 有status字段
      response.status === 201
    )

    if (isSuccess) {
      // 尝试从不同位置获取id
      const newId = response.data?.id || response.id || tutorialForm.value.id

      if (newId) {
        tutorialForm.value.id = newId
        console.log('教程ID已更新:', newId)
      }

      tutorialForm.value.status = 'DRAFT'
      tutorialForm.value.review_status = 'NOT_SUBMITTED'

      console.log('✅ 草稿保存成功')
      ElMessage.success('草稿已保存！')
    } else {
      console.warn('❌ 响应格式异常，response:', response)
      ElMessage.warning('保存完成，但响应格式异常')
    }
  } catch (error) {
    console.error('=== 保存草稿失败 ===')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)
    console.error('响应数据:', error.response?.data)
    console.error('响应状态:', error.response?.status)
    console.error('请求配置:', error.config)

    // 显示更详细的错误信息
    const errorMsg = error.response?.data?.message || error.message || '保存失败，请稍后重试'
    ElMessage.error(errorMsg)
  } finally {
    submitting.value = false
    console.log('=== saveDraft 执行结束 ===')
  }
}

// 图片上传处理
const handleImageUpload = async (file) => {
  // 验证文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }

  // 验证文件大小（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }

  uploading.value = true
  uploadProgress.value = 0

  try {
    // 创建 FormData
    const formData = new FormData()
    formData.append('file', file)

    // 使用原生 axios 进行上传，以便监听进度
    const response = await api.post(API_CONFIG.upload.image, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        uploadProgress.value = percentCompleted
      }
    })

    if (response.data && response.data.fullUrl) {
      tutorialForm.value.cover_image = response.data.fullUrl
      ElMessage.success('图片上传成功！')
    } else {
      throw new Error('上传响应格式错误')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败，请稍后重试')
  } finally {
    uploading.value = false
    uploadProgress.value = 0
  }

  return false // 阻止 el-upload 的默认上传行为
}

// 删除封面图
const removeCoverImage = () => {
  tutorialForm.value.cover_image = ''
  ElMessage.success('已删除封面图')
}

// 预览封面图
const previewCoverImage = () => {
  if (tutorialForm.value.cover_image) {
    uploadPreviewUrl.value = tutorialForm.value.cover_image
    uploadDialogVisible.value = true
  }
}

// 视频上传处理
const handleVideoUpload = async (file) => {
  // 验证文件类型
  const isVideo = file.type.startsWith('video/')
  if (!isVideo) {
    ElMessage.error('只能上传视频文件！')
    return false
  }

  // 验证文件大小（200MB）
  const isLt200M = file.size / 1024 / 1024 < 200
  if (!isLt200M) {
    ElMessage.error('视频大小不能超过 200MB！')
    return false
  }

  uploadingVideo.value = true
  videoUploadProgress.value = 0

  try {
    // 创建 FormData
    const formData = new FormData()
    formData.append('file', file)

    console.log('=== 开始上传视频 ===')
    console.log('文件名:', file.name)
    console.log('文件大小:', file.size)
    console.log('文件类型:', file.type)

    // 保存文件信息用于显示
    videoFileInfo.value = {
      name: file.name,
      size: file.size,
      type: file.type
    }

    // 使用原生 axios 进行上传，以便监听进度
    const response = await api.post(API_CONFIG.upload.file, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        videoUploadProgress.value = percentCompleted
      }
    })

    console.log('=== 视频上传响应 ===')
    console.log('完整响应:', response)
    console.log('响应类型:', typeof response)
    console.log('response.data:', response?.data)

    // 与图片上传保持一致的响应解析逻辑
    if (response.data && response.data.fullUrl) {
      tutorialForm.value.video_url = response.data.fullUrl
      ElMessage.success('视频上传成功！')
    } else {
      console.error('❌ 响应格式错误')
      console.error('期望格式: { data: { fullUrl: "..." } }')
      console.error('实际响应:', response)
      throw new Error('上传响应格式错误')
    }
  } catch (error) {
    console.error('视频上传失败:', error)
    ElMessage.error('视频上传失败，请稍后重试')
  } finally {
    uploadingVideo.value = false
    videoUploadProgress.value = 0
  }

  return false // 阻止 el-upload 的默认上传行为
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  const size = bytes / Math.pow(k, i)
  return `${size.toFixed(i === 0 ? 0 : 2)} ${sizes[i]}`
}

// 删除视频
const removeVideo = () => {
  tutorialForm.value.video_url = ''
  videoFileInfo.value = null
  ElMessage.success('已删除视频')
}

// 打开内容图片上传对话框
const openContentImageUpload = () => {
  // 清空之前的上传记录
  uploadedContentImageUrl.value = ''
  contentImageUploadProgress.value = 0
  contentImageDialogVisible.value = true
}

// 内容图片上传处理
const handleContentImageUpload = async (file) => {
  // 验证文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }

  // 验证文件大小（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }

  uploadingContentImage.value = true
  contentImageUploadProgress.value = 0

  try {
    // 创建 FormData
    const formData = new FormData()
    formData.append('file', file)

    // 使用原生 axios 进行上传，以便监听进度
    const response = await api.post(API_CONFIG.upload.image, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        contentImageUploadProgress.value = percentCompleted
      }
    })

    if (response.data && response.data.fullUrl) {
      uploadedContentImageUrl.value = response.data.fullUrl
      ElMessage.success('图片上传成功！请点击"插入到内容"按钮')
      // 不再自动插入，让用户手动点击按钮插入
    } else {
      throw new Error('上传响应格式错误')
    }
  } catch (error) {
    console.error('内容图片上传失败:', error)
    ElMessage.error('图片上传失败，请稍后重试')
  } finally {
    uploadingContentImage.value = false
    contentImageUploadProgress.value = 0
  }

  return false // 阻止 el-upload 的默认上传行为
}

// 在光标位置插入图片语法
const insertImageToContent = (imageUrl) => {
  const textarea = contentTextarea.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = tutorialForm.value.content

  // 在光标位置插入 Markdown 图片语法
  const imageMarkdown = `\n
![图片描述](${imageUrl})\n`

  const before = text.substring(0, start)
  const after = text.substring(end)

  tutorialForm.value.content = before + imageMarkdown + after

  // 设置光标位置到插入的图片后面
  setTimeout(() => {
    const newPosition = start + imageMarkdown.length
    textarea.setSelectionRange(newPosition, newPosition)
    textarea.focus()
  }, 10)
}

// 手动插入图片URL（用于对话框中手动输入URL）
const insertContentImageUrl = () => {
  if (!uploadedContentImageUrl.value) {
    ElMessage.warning('请先上传图片')
    return
  }

  insertImageToContent(uploadedContentImageUrl.value)

  // 关闭对话框
  contentImageDialogVisible.value = false
  uploadedContentImageUrl.value = ''
}

// 预览视频
const previewVideo = () => {
  if (tutorialForm.value.video_url) {
    videoPreviewUrl.value = tutorialForm.value.video_url
    videoPreviewDialogVisible.value = true
  }
}

// 关闭视频预览对话框时暂停并销毁视频
const handleVideoDialogClose = () => {
  if (videoPlayerRef.value) {
    videoPlayerRef.value.pause()
    videoPlayerRef.value.src = ''
    videoPlayerRef.value.load()
  }
}

// 加载教程数据（用于编辑）
const loadTutorialData = async (tutorialId) => {
  console.log('=== loadTutorialData 开始执行 ===')
  console.log('教程ID:', tutorialId)

  try {
    const response = await api.get(`${API_CONFIG.tutorial.detail}${tutorialId}`)

    console.log('API响应完整内容:', response)

    // 支持多种响应格式
    let tutorial = null
    if (response.data && typeof response.data === 'object' && response.data.id) {
      tutorial = response.data
      console.log('使用 response.data')
    } else if (response && typeof response === 'object' && response.id) {
      tutorial = response
      console.log('使用 response')
    }

    if (tutorial) {
      // 详细打印所有字段，帮助调试
      console.log('教程对象所有字段:', {
        id: tutorial.id,
        title: tutorial.title,
        type: tutorial.type,
        coverImage: tutorial.coverImage,
        content: tutorial.content ? tutorial.content.substring(0, 50) + '...' : '',
        difficulty: tutorial.difficulty,
        duration: tutorial.duration
      })

      // 将后端数据映射到表单字段（后端返回驼峰命名）
      tutorialForm.value = {
        id: tutorial.id,
        title: tutorial.title || '',
        type: tutorial.type || 'article',
        content: tutorial.content || '',
        difficulty: tutorial.difficulty || 'BEGINNER',
        duration: tutorial.duration || '',
        calories: tutorial.calories || null,
        prep_time: tutorial.prepTime || '',
        servings: tutorial.servings || null,
        cover_image: tutorial.coverImage || '',
        video_url: tutorial.videoUrl || '', // 加载视频URL
        tags: tutorial.tags || null,
        status: tutorial.status || 'DRAFT',
        review_status: tutorial.reviewStatus || 'NOT_SUBMITTED'
      }

      console.log('✅ 教程数据加载成功:', tutorialForm.value.title)
      console.log('表单数据:', tutorialForm.value)
      console.log('封面图URL:', tutorialForm.value.cover_image)

      // 自动调整textarea高度
      setTimeout(() => {
        autoResizeTextarea()
      }, 200)
    } else {
      console.warn('⚠️ 未找到教程数据')
      ElMessage.warning('未找到教程数据')
    }
  } catch (error) {
    console.error('❌ 加载教程数据失败')
    console.error('错误对象:', error)
    console.error('错误消息:', error.message)

    if (error.response?.status === 404) {
      ElMessage.error('教程不存在')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '加载教程数据失败')
    }

    // 如果加载失败，跳转回我的教程页面
    setTimeout(() => {
      router.push('/user/home/tutorials/my')
    }, 1500)
  } finally {
    console.log('=== loadTutorialData 执行结束 ===')
  }
}

// 初始化和监听
onMounted(async () => {
  // 检查URL参数，如果有id则加载教程数据（编辑模式）
  const tutorialId = route.query.id
  if (tutorialId) {
    console.log('检测到教程ID，进入编辑模式:', tutorialId)
    await loadTutorialData(tutorialId)
  } else {
    console.log('未检测到教程ID，进入新建模式')
  }

  // 初始化textarea高度
  setTimeout(() => {
    autoResizeTextarea()
  }, 100)
})

// 监听内容变化（处理编程式的内容变更）
watch(() => tutorialForm.value.content, () => {
  setTimeout(() => {
    autoResizeTextarea()
  }, 10)
})
</script>

<template>
  <div class="user-tutorial-publish-container">
    <!-- 页面头部（§4.7 标准版：标题 --text-h1 + 上下 --space-6，返回放右侧动作区） -->
    <AppPageHeader title="发布教程" subtitle="分享你的美食经验，帮助更多人">
      <template #extra>
        <span
          class="completion-pill"
          :class="{ 'completion-pill--done': completionPercent >= 100 }"
        >
          完成度 {{ completionPercent }}%
        </span>
      </template>
      <template #actions>
        <CommonBackButton type="default" />
        <el-button :loading="submitting" @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTutorial">提交审核</el-button>
      </template>
    </AppPageHeader>

    <div class="content-wrapper">
      <!-- 左侧：编辑表单 -->
      <div class="main-content">
        <!-- 快速模板 -->
        <el-card class="template-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>快速模板</span>
            </div>
          </template>
          <div class="templates-grid">
            <div
              v-for="(template, index) in quickTemplates"
              :key="index"
              class="template-item"
              @click="useTemplate(template)"
            >
              <div class="template-icon" :class="template.type">
                <el-icon v-if="template.type === 'video'"><VideoCamera /></el-icon>
                <el-icon v-else><Document /></el-icon>
              </div>
              <div class="template-info">
                <div class="template-title">{{ template.title }}</div>
                <div class="template-difficulty">
                  <el-tag size="small" :type="template.difficulty === 'BEGINNER' ? 'success' : template.difficulty === 'INTERMEDIATE' ? 'warning' : 'danger'">
                    {{ template.difficulty === 'BEGINNER' ? '初级' : template.difficulty === 'INTERMEDIATE' ? '中级' : '高级' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 基本信息表单 -->
        <el-card class="form-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>基本信息</span>
            </div>
          </template>

          <el-form :model="tutorialForm" label-width="100px" class="publish-form">
            <el-form-item label="教程标题" required>
              <el-input
                v-model="tutorialForm.title"
                placeholder="给教程起个好标题，例如：家常红烧肉的秘诀"
                maxlength="100"
                show-word-limit
                size="large"
              />
            </el-form-item>

            <el-form-item label="教程类型" required>
              <el-radio-group v-model="tutorialForm.type" size="large">
                <el-radio-button value="article">
                  <el-icon><Document /></el-icon> 图文指南
                </el-radio-button>
                <el-radio-button value="video">
                  <el-icon><VideoCamera /></el-icon> 视频教程
                </el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="难度">
              <el-radio-group v-model="tutorialForm.difficulty" size="large">
                <el-radio-button value="BEGINNER">🌱 初级</el-radio-button>
                <el-radio-button value="INTERMEDIATE">🌿 中级</el-radio-button>
                <el-radio-button value="ADVANCED">🌳 高级</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="时长">
              <el-input
                v-model="tutorialForm.duration"
                placeholder="例如: 15分钟（可选）"
                size="large"
              >
                <template #prefix>
                  <el-icon><VideoCamera /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 详细信息表单 -->
        <el-card class="form-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>详细信息</span>
            </div>
          </template>

          <el-form :model="tutorialForm" label-width="100px" class="publish-form">
            <el-form-item label="卡路里">
              <el-input-number
                v-model="tutorialForm.calories"
                :min="0"
                :step="10"
                placeholder="千卡"
                size="large"
              />
              <span class="form-tip">标注卡路里有助于健康管理</span>
            </el-form-item>

            <el-form-item label="准备时间">
              <el-input
                v-model="tutorialForm.prep_time"
                placeholder="例如: 20分钟（可选）"
                size="large"
              />
            </el-form-item>

            <el-form-item label="份量">
              <el-input-number
                v-model="tutorialForm.servings"
                :min="1"
                :max="20"
                placeholder="人份"
                size="large"
              />
            </el-form-item>

            <el-form-item label="封面图">
              <div class="cover-upload-container">
                <!-- 上传区域 -->
                <el-upload
                  class="cover-uploader"
                  :show-file-list="false"
                  :before-upload="handleImageUpload"
                  :disabled="uploading"
                  accept="image/*"
                  drag
                >
                  <div v-if="uploading" class="uploading-state">
                    <el-progress
                      type="circle"
                      :percentage="uploadProgress"
                      :width="60"
                    />
                    <p>正在上传... {{ uploadProgress }}%</p>
                  </div>
                  <div v-else class="upload-placeholder">
                    <el-icon class="upload-icon"><Upload /></el-icon>
                    <div class="upload-text">
                      <p>点击或拖拽上传</p>
                      <p class="upload-hint">支持 jpg、png、gif</p>
                    </div>
                  </div>
                </el-upload>

                <!-- 已有封面图时显示预览 -->
                <div v-if="tutorialForm.cover_image" class="cover-preview-wrapper">
                  <div class="cover-image-box">
                    <img
                      :src="tutorialForm.cover_image"
                      alt="封面图"
                      class="cover-preview-image"
                      @click="previewCoverImage"
                    />
                    <div class="cover-image-actions">
                      <el-button
                        type="primary"
                        size="small"
                        @click="previewCoverImage"
                        :disabled="uploading"
                      >
                        <el-icon><View /></el-icon> 预览
                      </el-button>
                      <el-button
                        type="danger"
                        size="small"
                        @click="removeCoverImage"
                        :disabled="uploading"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </el-form-item>

            <!-- 视频上传（仅视频教程显示） -->
            <el-form-item v-if="tutorialForm.type === 'video'" label="视频文件">
              <div class="cover-upload-container">
                <!-- 上传区域 -->
                <el-upload
                  class="cover-uploader"
                  :show-file-list="false"
                  :before-upload="handleVideoUpload"
                  :disabled="uploadingVideo"
                  accept="video/*"
                  drag
                >
                  <div v-if="uploadingVideo" class="uploading-state">
                    <el-progress
                      type="circle"
                      :percentage="videoUploadProgress"
                      :width="60"
                    />
                    <p>正在上传... {{ videoUploadProgress }}%</p>
                  </div>
                  <div v-else class="upload-placeholder">
                    <el-icon class="upload-icon"><VideoCamera /></el-icon>
                    <div class="upload-text">
                      <p>点击或拖拽上传视频</p>
                      <p class="upload-hint">支持 mp4、avi、mov等格式，最大200MB</p>
                    </div>
                  </div>
                </el-upload>

                <!-- 已有视频时显示预览 -->
                <div v-if="tutorialForm.video_url" class="cover-preview-wrapper">
                  <div class="video-preview-box">
                    <video
                      :src="tutorialForm.video_url"
                      class="video-preview-player"
                      preload="metadata"
                      @click="previewVideo"
                    ></video>
                    <!-- 删除按钮 -->
                    <div
                      class="video-delete-btn"
                      @click="removeVideo"
                      :disabled="uploadingVideo"
                      title="删除视频"
                    >
                      <el-icon><Close /></el-icon>
                    </div>
                    <!-- 文件信息 -->
                    <div v-if="videoFileInfo" class="video-file-info">
                      <span class="file-name">{{ videoFileInfo.name }}</span>
                      <span class="file-size">{{ formatFileSize(videoFileInfo.size) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 教程内容 -->
        <el-card class="form-card content-card" shadow="hover">
          <template #header>
            <div class="card-header-content">
              <div class="card-header">
                <el-icon><Document /></el-icon>
                <span>教程内容</span>
              </div>
              <div class="preview-mode-switcher">
                <el-button
                  :type="previewMode === 'edit' ? 'primary' : ''"
                  size="small"
                  @click="togglePreviewMode('edit')"
                >
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>
                <el-button
                  :type="previewMode === 'preview' ? 'primary' : ''"
                  size="small"
                  @click="togglePreviewMode('preview')"
                >
                  <el-icon><View /></el-icon> 预览
                </el-button>
                <el-button
                  :type="previewMode === 'split' ? 'primary' : ''"
                  size="small"
                  @click="togglePreviewMode('split')"
                >
                  <el-icon><View /></el-icon> 分屏
                </el-button>
              </div>
            </div>
          </template>

          <div :class="['content-editor-wrapper', `mode-${previewMode}`]">
            <!-- 编辑区域 -->
            <div v-show="previewMode === 'edit' || previewMode === 'split'" class="editor-pane">
              <!-- 工具栏 -->
              <div class="editor-toolbar">
                <el-button
                  type="primary"
                  size="small"
                  @click="openContentImageUpload"
                  :disabled="uploadingContentImage"
                >
                  <el-icon><Upload /></el-icon>
                  插入图片
                </el-button>
                <span class="toolbar-tip">
                  💡 支持Markdown语法，上传的图片会自动插入到光标位置
                </span>
              </div>

              <textarea
                ref="contentTextarea"
                v-model="tutorialForm.content"
                class="native-textarea"
                rows="3"
                @input="handleContentInput"
                placeholder="💡 输入内容会自动增高，也可拖动右下角手动调整

支持Markdown格式，例如：
## 标题
- 要点1
- 要点2

**粗体文字**
![图片](url)
[链接](url)"
              ></textarea>
            </div>

            <!-- 预览区域 -->
            <div v-show="previewMode === 'preview' || previewMode === 'split'" class="preview-pane">
              <div v-if="tutorialForm.content" class="markdown-preview" v-html="renderMarkdown(tutorialForm.content)"></div>
              <div v-else class="no-preview-content">
                <el-icon :size="48"><Document /></el-icon>
                <p>暂无内容预览</p>
              </div>
            </div>
          </div>

          <div class="content-help">
            <p><strong>💡 内容提示：</strong></p>
            <ul>
              <li>详细描述制作步骤或饮食建议</li>
              <li>可以添加小贴士和注意事项</li>
              <li>支持Markdown格式，排版更美观（支持标题、粗体、列表、链接等）</li>
            </ul>
          </div>
        </el-card>

        <!-- 提示信息 -->
        <el-card class="tips-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>发布须知</span>
            </div>
          </template>
          <div class="tips-content">
            <div class="tip-section">
              <h5>✅ 内容要求</h5>
              <ul>
                <li>内容需原创，不得抄袭</li>
                <li>步骤清晰，易于理解</li>
                <li>图片清晰，画质良好</li>
                <li>语言文明，友善交流</li>
              </ul>
            </div>

            <div class="tip-section">
              <h5>⏰ 审核时效</h5>
              <p>提交后，我们将在 <strong>1-3个工作日</strong> 内完成审核</p>
              <p>审核通过后，您的教程将展示在"制作教程与指南"板块</p>
            </div>

            <div class="tip-section">
              <h5>📝 审核标准</h5>
              <ul>
                <li>内容完整度：步骤/信息是否详细</li>
                <li>实用价值：是否有实际指导意义</li>
                <li>原创性：是否为原创内容</li>
                <li>规范性：是否符合社区规范</li>
              </ul>
            </div>

            <div class="tip-section">
              <h5>⚠️ 注意事项</h5>
              <ul>
                <li>请勿发布虚假信息</li>
                <li>请勿发布违反法律法规的内容</li>
                <li>请勿发布广告或推广信息</li>
                <li>审核通过后，您的教程将展示给所有用户</li>
              </ul>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：预览面板 -->
      <div class="side-content">
        <el-card class="preview-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>教程预览</span>
            </div>
          </template>

          <div class="preview-content">
            <div class="preview-cover">
              <img
                v-if="tutorialForm.cover_image"
                :src="tutorialForm.cover_image"
                alt="封面预览"
              />
              <div v-else class="no-cover">
                <el-icon :size="48"><Document /></el-icon>
                <span>暂无封面</span>
              </div>
              <div class="type-badge">
                <el-icon v-if="tutorialForm.type === 'video'"><VideoCamera /></el-icon>
                <el-icon v-else><Document /></el-icon>
                {{ tutorialForm.type === 'video' ? '视频' : '图文' }}
              </div>
            </div>

            <div class="preview-info">
              <h3>{{ tutorialForm.title || '教程标题' }}</h3>

              <div class="preview-meta">
                <span v-if="tutorialForm.difficulty" class="difficulty-badge">
                  {{ tutorialForm.difficulty === 'BEGINNER' ? '🌱 初级' : tutorialForm.difficulty === 'INTERMEDIATE' ? '🌿 中级' : '🌳 高级' }}
                </span>
                <span v-if="tutorialForm.duration" class="duration-badge">
                  <el-icon><VideoCamera /></el-icon> {{ tutorialForm.duration }}
                </span>
                <span v-if="tutorialForm.calories" class="calories-badge">
                  {{ tutorialForm.calories }} 千卡
                </span>
              </div>

              <div class="preview-body">
                <div v-if="tutorialForm.content" class="content-preview">
                  <div v-html="tutorialForm.content.replace(/\n/g, '<br>')"></div>
                </div>
                <div v-else class="no-content">
                  教程内容将在这里显示...
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 图片预览对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="封面图预览"
      width="60%"
      :close-on-click-modal="true"
    >
      <div class="preview-dialog-content">
        <img :src="uploadPreviewUrl" alt="封面图预览" class="preview-dialog-image" />
      </div>
    </el-dialog>

    <!-- 视频预览对话框 -->
    <el-dialog
      v-model="videoPreviewDialogVisible"
      title="视频预览"
      width="70%"
      :close-on-click-modal="true"
      @close="handleVideoDialogClose"
    >
      <div class="video-preview-dialog-content">
        <video
          ref="videoPlayerRef"
          :src="videoPreviewUrl"
          controls
          autoplay
          class="video-preview-dialog-player"
        ></video>
      </div>
    </el-dialog>

    <!-- 内容图片上传对话框 -->
    <el-dialog
      v-model="contentImageDialogVisible"
      title="插入图片到内容"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="content-image-upload-dialog">
        <el-upload
          class="content-image-uploader"
          :show-file-list="false"
          :before-upload="handleContentImageUpload"
          :disabled="uploadingContentImage"
          accept="image/*"
          drag
        >
          <div v-if="uploadingContentImage" class="uploading-state">
            <el-progress
              type="circle"
              :percentage="contentImageUploadProgress"
              :width="80"
            />
            <p>正在上传... {{ contentImageUploadProgress }}%</p>
          </div>
          <div v-else class="upload-placeholder">
            <el-icon class="upload-icon"><Upload /></el-icon>
            <div class="upload-text">
              <p>点击或拖拽上传图片</p>
              <p class="upload-hint">支持 jpg、png、gif，最大 5MB</p>
            </div>
          </div>
        </el-upload>

        <div v-if="uploadedContentImageUrl" class="uploaded-preview">
          <p class="preview-title">图片预览：</p>
          <img :src="uploadedContentImageUrl" alt="上传的图片" class="preview-image" />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="contentImageDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="insertContentImageUrl"
            :disabled="!uploadedContentImageUrl"
          >
            插入到内容
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped lang="less">
/* 颜色、间距、圆角、字阶全部来自 tokens.css（§3、§7.1）。 */
.user-tutorial-publish-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7）。 */
.user-tutorial-publish-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
  min-height: var(--control-height-md);
}

/* 完成度 pill：小面积中性色，达标后转成功语义色（§3.1）。 */
.completion-pill {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  background: var(--neutral-bg);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  transition:
    background var(--duration-base) var(--ease-standard),
    color var(--duration-base) var(--ease-standard);
}

.completion-pill--done {
  background: var(--success-bg);
  color: var(--success);
}

/* 左侧编辑 + 右侧预览两栏（§3.3）。 */
.content-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  align-items: start;
  gap: var(--card-gap);
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap);
  min-width: 0;
}

/* L1 微质感卡：--surface + 1px --line + 顶部内高光（§3.4）。 */
.main-content :deep(.el-card),
.side-content :deep(.el-card) {
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  transition:
    border-color var(--duration-fast) var(--ease-standard),
    box-shadow var(--duration-fast) var(--ease-standard);
}

.main-content :deep(.el-card__header),
.side-content :deep(.el-card__header) {
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--line);
}

.main-content :deep(.el-card__body),
.side-content :deep(.el-card__body) {
  padding: var(--space-5);
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.card-header .el-icon {
  font-size: 18px;
  color: var(--brand-500);
}

/* 快速模板 */
.templates-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

.template-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  cursor: pointer;
  transition:
    border-color var(--duration-fast) var(--ease-standard),
    background var(--duration-fast) var(--ease-standard),
    transform var(--duration-fast) var(--ease-out);
}

.template-item:hover {
  border-color: var(--brand-300);
  background: var(--brand-50);
  transform: translateY(-1px);
}

.template-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  font-size: 22px;

  &.video {
    background: var(--info-bg);
    color: var(--info);
  }

  &.article {
    background: var(--brand-100);
    color: var(--brand-500);
  }
}

.template-info {
  min-width: 0;
}

.template-title {
  margin-bottom: var(--space-1);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.template-difficulty {
  display: flex;
  align-items: center;
}

/* 基本信息 / 详细信息表单 */
.publish-form {
  :deep(.el-form-item__label) {
    font-weight: var(--weight-medium);
    color: var(--ink-500);
  }

  :deep(.el-input),
  :deep(.el-input-number),
  :deep(.el-radio-group) {
    width: 100%;
  }

  :deep(.el-input__wrapper),
  :deep(.el-input-number .el-input__wrapper) {
    min-height: var(--control-height-md);
  }
}

.form-tip {
  margin-left: var(--space-3);
  color: var(--ink-400);
  font-size: var(--text-body-sm);
}

/* 封面 / 视频上传 */
.cover-upload-container {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  width: 100%;
}

.cover-uploader {
  width: 33.33%;
  aspect-ratio: 1 / 1;

  :deep(.el-upload),
  :deep(.el-upload-dragger) {
    width: 100%;
    height: 100%;
  }

  :deep(.el-upload-dragger) {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: var(--space-4) var(--space-3);
    border: 1px dashed var(--line-strong);
    border-radius: var(--radius-md);
    background: var(--canvas-sunken);
    transition:
      border-color var(--duration-fast) var(--ease-standard),
      background var(--duration-fast) var(--ease-standard);
  }

  :deep(.el-upload-dragger:hover) {
    border-color: var(--brand-400);
    background: var(--brand-50);
  }
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);

  .upload-icon {
    font-size: 32px;
    color: var(--brand-500);
  }
}

.upload-text {
  text-align: center;

  p {
    margin: 0;
    font-size: var(--text-body-sm);
    color: var(--ink-700);
  }

  .upload-hint {
    margin-top: var(--space-1);
    font-size: var(--text-caption);
    color: var(--ink-400);
  }
}

.uploading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);

  p {
    margin: 0;
    font-size: var(--text-body-sm);
    color: var(--ink-500);
  }
}

.cover-preview-wrapper {
  width: 33.33%;
  aspect-ratio: 1 / 1;
}

.cover-image-box,
.video-preview-box {
  position: relative;
  width: 100%;
  height: 100%;
  margin: 0;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
}

.cover-preview-image,
.video-preview-player {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform var(--duration-base) var(--ease-out);
}

.cover-preview-image:hover,
.video-preview-player:hover {
  transform: scale(1.02);
}

.cover-image-actions {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  z-index: 10;
  display: flex;
  gap: var(--space-2);
  opacity: 0;
  transition: opacity var(--duration-fast) var(--ease-standard);

  :deep(.el-button) {
    height: var(--control-height-sm);
    padding: 0 var(--space-3);
    font-size: var(--text-body-sm);
  }
}

.cover-image-box:hover .cover-image-actions {
  opacity: 1;
}

.video-delete-btn {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  background: var(--surface-scrim);
  cursor: pointer;
  opacity: 0;
  transform: scale(0.8);
  transition:
    opacity var(--duration-fast) var(--ease-standard),
    background var(--duration-fast) var(--ease-standard),
    transform var(--duration-fast) var(--ease-out);

  .el-icon {
    color: var(--ink-700);
    font-size: 16px;
  }
}

.video-delete-btn:hover {
  background: var(--danger-bg);
  transform: scale(1.05);
}

.video-preview-box:hover .video-delete-btn {
  opacity: 1;
  transform: scale(1);
}

.video-file-info {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-2) var(--space-3);
  background: var(--overlay);
  font-size: var(--text-caption);
  color: var(--surface);
  pointer-events: none;

  .file-name {
    flex: 1;
    overflow: hidden;
    font-weight: var(--weight-medium);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .file-size {
    margin-left: var(--space-2);
    font-size: var(--text-caption);
    white-space: nowrap;
    opacity: 0.85;
  }
}

/* 教程内容 */
.card-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  width: 100%;
}

.preview-mode-switcher {
  display: flex;
  gap: var(--space-2);

  :deep(.el-button) {
    height: var(--control-height-sm);
  }
}

.content-editor-wrapper {
  display: grid;
  gap: var(--space-4);
  margin-bottom: var(--space-4);

  &.mode-edit,
  &.mode-preview {
    grid-template-columns: minmax(0, 1fr);
  }

  &.mode-split {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.editor-pane,
.preview-pane {
  display: flex;
  flex-direction: column;
  min-height: 120px;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
}

.editor-toolbar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-3);
  border-bottom: 1px solid var(--line);
  background: var(--canvas-sunken);

  :deep(.el-button) {
    height: var(--control-height-sm);
  }

  .toolbar-tip {
    flex: 1;
    font-size: var(--text-caption);
    color: var(--ink-500);
  }
}

.native-textarea {
  box-sizing: border-box;
  width: 100%;
  min-height: 120px;
  padding: var(--space-3);
  border: none;
  outline: none;
  background: var(--surface);
  color: var(--ink-700);
  font-family: var(--font-num);
  font-size: var(--text-body);
  line-height: 1.7;
  resize: vertical;
}

.native-textarea::placeholder {
  color: var(--ink-400);
}

.preview-pane {
  justify-content: flex-start;
  padding: var(--space-4);
  overflow-y: auto;
  background: var(--surface);
}

.markdown-preview {
  color: var(--ink-700);
  line-height: 1.8;

  h1, h2, h3, h4, h5, h6 {
    margin: var(--space-4) 0 var(--space-3);
    font-weight: var(--weight-semibold);
    line-height: 1.4;
    color: var(--ink-900);
  }

  h1 { font-size: var(--text-h1); }
  h2 { font-size: var(--text-h2); }
  h3 { font-size: var(--text-h3); }
  h4, h5, h6 { font-size: var(--text-body); }

  p {
    margin-bottom: var(--space-4);
    line-height: 1.8;
  }

  ul, ol {
    margin-bottom: var(--space-4);
    padding-left: var(--space-6);

    li {
      margin-bottom: var(--space-2);
      line-height: 1.8;
    }
  }

  strong {
    color: var(--brand-600);
    font-weight: var(--weight-semibold);
  }

  em {
    color: var(--ink-500);
  }

  a {
    color: var(--info);
    text-decoration: none;
    border-bottom: 1px solid var(--info);

    &:hover {
      border-bottom-color: var(--brand-300);
    }
  }

  code {
    padding: 2px var(--space-1);
    border-radius: var(--radius-sm);
    background: var(--canvas-sunken);
    color: var(--danger);
    font-family: var(--font-num);
    font-size: var(--text-body-sm);
  }

  blockquote {
    margin: var(--space-3) 0;
    padding: var(--space-3) var(--space-4);
    border-left: 3px solid var(--brand-500);
    background: var(--canvas-sunken);
    color: var(--ink-500);
  }

  .markdown-image {
    display: block;
    max-width: 100%;
    height: auto;
    margin: var(--space-3) 0;
    border-radius: var(--radius-sm);
  }
}

.no-preview-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  min-height: 120px;
  color: var(--ink-400);
  font-size: var(--text-body-sm);
}

.content-help {
  margin-top: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border-left: 3px solid var(--brand-300);
  border-radius: var(--radius-sm);
  background: var(--brand-50);

  p {
    margin: 0 0 var(--space-2);
    font-size: var(--text-body);
    color: var(--ink-700);

    &:last-child {
      margin-bottom: 0;
    }
  }

  ul {
    margin: 0;
    padding-left: var(--space-5);

    li {
      margin: var(--space-1) 0;
      font-size: var(--text-body-sm);
      color: var(--ink-500);
      line-height: 1.6;
    }
  }
}

/* 发布须知 */
.tips-content {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4) var(--space-6);
}

.tip-section {
  h5 {
    margin: 0 0 var(--space-2);
    font-size: var(--text-body);
    font-weight: var(--weight-semibold);
    color: var(--ink-900);
  }

  p {
    margin: var(--space-1) 0;
    font-size: var(--text-body-sm);
    line-height: 1.7;
    color: var(--ink-500);

    strong {
      color: var(--brand-600);
      font-weight: var(--weight-semibold);
    }
  }

  ul {
    margin: 0;
    padding-left: var(--space-5);

    li {
      margin: var(--space-1) 0;
      font-size: var(--text-body-sm);
      line-height: 1.6;
      color: var(--ink-500);
    }
  }
}

/* 右侧预览：沿用教程详情的排版语言（§4.4、§4.7） */
.side-content {
  min-width: 0;
}

.preview-card {
  position: sticky;
  top: var(--space-4);
}

.preview-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  margin-bottom: var(--space-4);
  overflow: hidden;
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .no-cover {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: var(--space-2);
    height: 100%;
    color: var(--ink-400);
    font-size: var(--text-body-sm);
  }

  .type-badge {
    position: absolute;
    top: var(--space-3);
    right: var(--space-3);
    display: flex;
    align-items: center;
    gap: var(--space-1);
    padding: var(--space-1) var(--space-3);
    border-radius: var(--radius-full);
    background: var(--surface-scrim);
    color: var(--ink-700);
    font-size: var(--text-caption);
    font-weight: var(--weight-semibold);
  }
}

.preview-info {
  h3 {
    margin: 0 0 var(--space-3);
    font-size: var(--text-h2);
    font-weight: var(--weight-semibold);
    line-height: 1.4;
    color: var(--ink-900);
  }

  .preview-meta {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-2);
    margin-bottom: var(--space-4);

    span {
      display: flex;
      align-items: center;
      gap: var(--space-1);
      padding: var(--space-1) var(--space-3);
      border-radius: var(--radius-full);
      font-size: var(--text-caption);
      font-weight: var(--weight-medium);
    }

    .difficulty-badge {
      background: var(--info-bg);
      color: var(--info);
    }

    .duration-badge {
      background: var(--warning-bg);
      color: var(--warning);
    }

    .calories-badge {
      background: var(--success-bg);
      color: var(--success);
    }
  }
}

.preview-body {
  .content-preview {
    max-height: 460px;
    padding: var(--space-3);
    overflow-y: auto;
    border-radius: var(--radius-sm);
    background: var(--canvas-sunken);
    color: var(--ink-500);
    font-size: var(--text-body-sm);
    line-height: 1.8;

    :deep(h1),
    :deep(h2),
    :deep(h3) {
      margin: var(--space-3) 0 var(--space-2);
      font-weight: var(--weight-semibold);
      color: var(--ink-900);
    }

    :deep(p) {
      margin-bottom: var(--space-2);
    }

    :deep(ul),
    :deep(ol) {
      margin-bottom: var(--space-2);
      padding-left: var(--space-5);
    }

    :deep(strong) {
      color: var(--brand-600);
      font-weight: var(--weight-semibold);
    }
  }

  .no-content {
    padding: var(--space-8) var(--space-4);
    border-radius: var(--radius-sm);
    background: var(--canvas-sunken);
    color: var(--ink-400);
    font-size: var(--text-body-sm);
    font-style: italic;
    text-align: center;
  }
}

/* 弹窗内容 */
.preview-dialog-content,
.video-preview-dialog-content {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4);
}

.preview-dialog-image {
  max-width: 100%;
  max-height: 70vh;
  border-radius: var(--radius-md);
}

.video-preview-dialog-player {
  width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: var(--radius-md);
}

.content-image-upload-dialog {
  .content-image-uploader {
    width: 100%;
    margin-bottom: var(--space-4);

    :deep(.el-upload),
    :deep(.el-upload-dragger) {
      width: 100%;
    }

    :deep(.el-upload-dragger) {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: var(--space-8);
      border: 1px dashed var(--line-strong);
      border-radius: var(--radius-md);
      background: var(--canvas-sunken);
      transition:
        border-color var(--duration-fast) var(--ease-standard),
        background var(--duration-fast) var(--ease-standard);
    }

    :deep(.el-upload-dragger:hover) {
      border-color: var(--brand-400);
      background: var(--brand-50);
    }

    .uploading-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-3);

      p {
        margin: 0;
        font-size: var(--text-body-sm);
        color: var(--ink-500);
      }
    }

    .upload-placeholder {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: var(--space-2);

      .upload-icon {
        font-size: 40px;
        color: var(--brand-500);
      }
    }
  }

  .uploaded-preview {
    padding: var(--space-3);
    border-radius: var(--radius-sm);
    background: var(--canvas-sunken);

    .preview-title {
      margin: 0 0 var(--space-3);
      font-size: var(--text-body);
      font-weight: var(--weight-medium);
      color: var(--ink-900);
    }

    .preview-image {
      width: 100%;
      max-height: 300px;
      object-fit: contain;
      border: 1px solid var(--line);
      border-radius: var(--radius-sm);
    }
  }
}

/* 响应式 */
@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: minmax(0, 1fr);
  }

  .side-content {
    display: none;
  }
}

@media (max-width: 768px) {
  .templates-grid,
  .tips-content {
    grid-template-columns: minmax(0, 1fr);
  }

  .content-editor-wrapper.mode-split {
    grid-template-columns: minmax(0, 1fr);
  }

  .cover-upload-container {
    flex-direction: column;
  }

  .cover-uploader,
  .cover-preview-wrapper {
    width: 100%;
  }
}
</style>
