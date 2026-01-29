<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { VideoCamera, Document, Upload, ArrowLeft, View, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()

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
  tags: [],
  status: 'DRAFT',
  review_status: 'NOT_SUBMITTED'
})

// 提交状态
const submitting = ref(false)

// Markdown 预览模式：'edit' | 'preview' | 'split'
const previewMode = ref('edit')

// Textarea引用
const contentTextarea = ref(null)

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

// 悬浮球配置
const floatingButtons = computed(() => {
  // 主操作按钮（上方）
  const primaryButton = {
    REVIEW: {
      icon: '⏳',
      color: 'linear-gradient(135deg, #f57c00 0%, #ff9800 100%)',
      text: '撤回审核',
      action: 'withdraw',
      disabled: false
    },
    DRAFT: {
      icon: '✨',
      color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      text: '提交审核',
      action: 'submit',
      disabled: false
    },
    EDIT: {
      icon: '🚀',
      color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      text: '提交审核',
      action: 'submit',
      disabled: false
    }
  }

  // 辅助操作按钮（下方）
  const secondaryButton = {
    REVIEW: {
      icon: '💾',
      color: 'linear-gradient(135deg, #90caf9 0%, #64b5f6 100%)',
      text: '保存草稿',
      action: 'save',
      disabled: true  // 审核中禁用保存
    },
    DRAFT: {
      icon: '💾',
      color: 'linear-gradient(135deg, #409eff 0%, #66b1ff 100%)',
      text: '保存草稿',
      action: 'save',
      disabled: false
    },
    EDIT: {
      icon: '💾',
      color: 'linear-gradient(135deg, #409eff 0%, #66b1ff 100%)',
      text: '保存草稿',
      action: 'save',
      disabled: false
    }
  }

  return {
    primary: primaryButton[currentStatus.value],
    secondary: secondaryButton[currentStatus.value]
  }
})

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

// 计算当前状态
const currentStatus = computed(() => {
  if (tutorialForm.value.review_status === 'PENDING') {
    return 'REVIEW' // 审核中
  } else if (tutorialForm.value.status === 'DRAFT') {
    return 'DRAFT' // 草稿
  } else {
    return 'EDIT' // 编辑中
  }
})

// 切换预览模式
const togglePreviewMode = (mode) => {
  previewMode.value = mode
}

// 简单的 Markdown 转 HTML
const renderMarkdown = (content) => {
  if (!content) return ''

  let html = content
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
    .replace(/^\- (.*$)/gim, '<li>$1</li>')
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
    const data = {
      ...tutorialForm.value,
      status: 'PUBLISHED',
      review_status: 'PENDING'
    }

    let response
    if (tutorialForm.value.id) {
      // 更新现有教程
      response = await api.put(`${API_CONFIG.tutorial.userUpdate}/${tutorialForm.value.id}`, data)
    } else {
      // 创建新教程
      response = await api.post(API_CONFIG.tutorial.userCreate, data)
    }

    if (response.data) {
      tutorialForm.value.id = response.data.id || tutorialForm.value.id
      tutorialForm.value.status = 'PUBLISHED'
      tutorialForm.value.review_status = 'PENDING'

      ElMessage.success('提交成功！您的教程已进入审核队列')
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 保存为草稿
const saveDraft = async () => {
  if (!tutorialForm.value.title) {
    ElMessage.warning('请至少输入教程标题')
    return
  }

  submitting.value = true

  try {
    const data = {
      ...tutorialForm.value,
      status: 'DRAFT',
      review_status: 'NOT_SUBMITTED'
    }

    let response
    if (tutorialForm.value.id) {
      // 更新现有草稿
      response = await api.put(`${API_CONFIG.tutorial.userUpdate}/${tutorialForm.value.id}`, data)
    } else {
      // 创建新草稿
      response = await api.post(API_CONFIG.tutorial.userCreate, data)
    }

    if (response.data) {
      tutorialForm.value.id = response.data.id || tutorialForm.value.id
      tutorialForm.value.status = 'DRAFT'
      tutorialForm.value.review_status = 'NOT_SUBMITTED'

      ElMessage.success('草稿已保存！')
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 撤回审核
const withdrawReview = async () => {
  try {
    await ElMessageBox.confirm(
      '撤回审核后，教程将恢复为草稿状态。您可以继续编辑后再次提交。',
      '确认撤回',
      {
        confirmButtonText: '确认撤回',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    const data = {
      ...tutorialForm.value,
      status: 'DRAFT',
      review_status: 'NOT_SUBMITTED'
    }

    const response = await api.put(`${API_CONFIG.tutorial.userUpdate}/${tutorialForm.value.id}`, data)

    if (response.data) {
      tutorialForm.value.status = 'DRAFT'
      tutorialForm.value.review_status = 'NOT_SUBMITTED'

      ElMessage.success('已撤回审核，教程恢复为草稿状态')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回失败:', error)
      ElMessage.error('撤回失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 悬浮球点击处理
const handleFloatingButtonClick = (action) => {
  switch (action) {
    case 'save':
      saveDraft()
      break
    case 'submit':
      submitTutorial()
      break
    case 'withdraw':
      withdrawReview()
      break
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 初始化和监听
onMounted(() => {
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
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <div class="title-section">
            <h1>发布教程</h1>
            <p class="subtitle">分享你的美食经验，帮助更多人</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button @click="goBack" size="default">
            <el-icon><ArrowLeft /></el-icon> 取消
          </el-button>
          <el-button type="info" @click="saveDraft" size="default" :loading="submitting">
            <el-icon><Document /></el-icon> 保存草稿
          </el-button>
          <el-button type="primary" @click="submitTutorial" size="default" :loading="submitting">
            <el-icon><Upload /></el-icon> 提交审核
          </el-button>
        </div>
      </div>
    </div>

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
              <el-input
                v-model="tutorialForm.cover_image"
                placeholder="图片URL（可选）"
                size="large"
              >
                <template #append>
                  <el-button :icon="Upload">上传图片</el-button>
                </template>
              </el-input>
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

    <!-- 智能悬浮球组 -->
    <div class="floating-buttons-container">
      <!-- 主操作按钮（上方） -->
      <div
        v-show="floatingButtons.primary"
        class="floating-action-button primary-button"
        :style="{ background: floatingButtons.primary.color }"
        :class="{ disabled: floatingButtons.primary.disabled }"
        @click="!floatingButtons.primary.disabled && handleFloatingButtonClick(floatingButtons.primary.action)"
      >
        <span class="floating-icon">{{ floatingButtons.primary.icon }}</span>
        <div class="floating-tooltip">
          <span class="tooltip-text">{{ floatingButtons.primary.text }}</span>
        </div>
      </div>

      <!-- 辅助操作按钮（下方） -->
      <div
        v-show="floatingButtons.secondary"
        class="floating-action-button secondary-button"
        :style="{ background: floatingButtons.secondary.color }"
        :class="{ disabled: floatingButtons.secondary.disabled }"
        @click="!floatingButtons.secondary.disabled && handleFloatingButtonClick(floatingButtons.secondary.action)"
      >
        <span class="floating-icon">{{ floatingButtons.secondary.icon }}</span>
        <div class="floating-tooltip">
          <span class="tooltip-text">{{ floatingButtons.secondary.text }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.user-tutorial-publish-container {
  padding: 0;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);

  .page-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 32px 40px;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.3);
    margin-bottom: 32px;

    .header-content {
      max-width: 1600px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 24px;

      .header-left {
        display: flex;
        align-items: center;
        gap: 20px;
        flex: 1;

        .title-section {
          h1 {
            margin: 0 0 8px 0;
            font-size: 32px;
            font-weight: bold;
            color: white;
            line-height: 1.2;
          }

          .subtitle {
            margin: 0;
            font-size: 15px;
            color: rgba(255, 255, 255, 0.85);
          }
        }
      }

      .header-actions {
        display: flex;
        flex-direction: column;
        gap: 10px;
        flex-shrink: 0;
        min-width: 140px;

        .el-button {
          width: 100%;
          min-width: auto;
          font-weight: 500;
          border: none;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 8px 16px;

          :deep(.el-icon) {
            margin-right: 6px;
          }

          &:not(.el-button--primary) {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            backdrop-filter: blur(10px);

            &:hover {
              background: rgba(255, 255, 255, 0.3);
            }
          }

          &.el-button--primary {
            background: white;
            color: #667eea;

            &:hover {
              background: #f0f0f0;
              transform: translateY(-2px);
              box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
            }
          }
        }
      }
    }
  }

  .content-wrapper {
    max-width: 1600px;
    margin: 0 auto;
    padding: 0 40px 40px;
    display: grid;
    grid-template-columns: 1fr 420px;
    gap: 24px;
  }

  .main-content {
    display: flex;
    flex-direction: column;
    gap: 24px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;

      .el-icon {
        font-size: 18px;
        color: #667eea;
      }
    }

    .template-card {
      border: none;
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        transform: translateY(-2px);
      }

      .templates-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16px;

        .template-item {
          padding: 20px;
          background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
          border: 2px solid #e9ecef;
          border-radius: 12px;
          cursor: pointer;
          transition: all 0.3s;
          display: flex;
          align-items: center;
          gap: 16px;

          &:hover {
            border-color: #667eea;
            background: linear-gradient(135deg, #f0f3ff 0%, #ffffff 100%);
            transform: translateY(-4px);
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.2);
          }

          .template-icon {
            width: 56px;
            height: 56px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-shrink: 0;
            font-size: 24px;

            &.video {
              background: linear-gradient(135deg, #ff6b6b 0%, #ff8e8e 100%);
              color: white;
            }

            &.article {
              background: linear-gradient(135deg, #f7b267 0%, #ffcc80 100%);
              color: white;
            }

            .el-icon {
              font-size: 28px;
            }
          }

          .template-info {
            flex: 1;

            .template-title {
              font-size: 15px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 8px;
            }

            .template-difficulty {
              display: flex;
              align-items: center;
            }
          }
        }
      }
    }

    .form-card {
      border: none;
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      &.content-card {
        .card-header-content {
          display: flex;
          justify-content: space-between;
          align-items: center;
          width: 100%;

          .card-header {
            flex: 1;
          }

          .preview-mode-switcher {
            display: flex;
            gap: 8px;
          }
        }

        .content-editor-wrapper {
          display: grid;
          gap: 20px;
          margin-bottom: 16px;

          &.mode-edit {
            grid-template-columns: 1fr;
          }

          &.mode-preview {
            grid-template-columns: 1fr;
          }

          &.mode-split {
            grid-template-columns: 1fr 1fr;

            @media (max-width: 1200px) {
              grid-template-columns: 1fr;
            }
          }

          .editor-pane,
          .preview-pane {
            min-height: 72px; /* 3行高度 */
            border-radius: 8px;
            display: flex;
            flex-direction: column;
          }

          .editor-pane {
            .native-textarea {
              width: 100%;
              min-height: 72px; /* 3行高度 (24px * 3) */
              border: 1px solid #dcdfe6;
              border-radius: 8px;
              padding: 12px 16px;
              font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
              font-size: 14px;
              line-height: 1.6;
              color: #606266;
              background-color: #ffffff;
              resize: vertical; /* 允许手动拖动调整高度 */
              overflow-y: auto;
              overflow-x: hidden;
              outline: none;
              transition: border-color 0.3s;
              box-sizing: border-box;

              &:hover {
                border-color: #c0c4cc;
              }

              &:focus {
                border-color: #409eff;
              }

              &::placeholder {
                color: #c0c4cc;
              }
            }
          }

          .preview-pane {
            background: #ffffff;
            border: 1px solid #dcdfe6;
            padding: 20px;
            overflow-y: auto;
            min-height: 72px; /* 与编辑器保持一致，3行高度 */
            max-height: none;
            align-self: flex-start;

            .markdown-preview {
              line-height: 1.8;
              color: #303133;

              h1, h2, h3, h4, h5, h6 {
                margin-top: 24px;
                margin-bottom: 16px;
                font-weight: 600;
                line-height: 1.4;
                color: #303133;
              }

              h1 { font-size: 28px; border-bottom: 2px solid #e4e7ed; padding-bottom: 10px; }
              h2 { font-size: 24px; border-bottom: 1px solid #e4e7ed; padding-bottom: 8px; }
              h3 { font-size: 20px; }
              h4 { font-size: 18px; }
              h5 { font-size: 16px; }
              h6 { font-size: 14px; }

              p {
                margin-bottom: 16px;
                line-height: 1.8;
              }

              ul, ol {
                margin-bottom: 16px;
                padding-left: 24px;

                li {
                  margin-bottom: 8px;
                  line-height: 1.8;
                }
              }

              strong {
                color: #667eea;
                font-weight: 600;
              }

              em {
                font-style: italic;
                color: #606266;
              }

              a {
                color: #409eff;
                text-decoration: none;
                border-bottom: 1px solid #409eff;
                transition: all 0.3s;

                &:hover {
                  color: #66b1ff;
                  border-bottom-color: #66b1ff;
                }
              }

              code {
                background: #f4f4f5;
                padding: 2px 6px;
                border-radius: 4px;
                font-family: 'Monaco', 'Menlo', monospace;
                font-size: 13px;
                color: #e83e8c;
              }

              blockquote {
                margin: 16px 0;
                padding: 12px 16px;
                background: #f4f4f5;
                border-left: 4px solid #667eea;
                color: #606266;
              }
            }

            .no-preview-content {
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              height: 100%;
              color: #909399;
              gap: 12px;

              .el-icon {
                color: #c0c4cc;
              }

              p {
                margin: 0;
                font-size: 14px;
              }
            }
          }
        }

        .el-form-item {
          margin-bottom: 0;
        }

        :deep(.el-form-item__content) {
          line-height: 1;
        }
      }

      .publish-form {
        :deep(.el-form-item__label) {
          font-weight: 500;
          color: #606266;
        }

        .form-tip {
          margin-left: 12px;
          color: #909399;
          font-size: 13px;
        }

        .content-help {
          margin-top: 16px;
          padding: 16px;
          background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
          border-left: 4px solid #667eea;
          border-radius: 8px;

          p {
            margin: 0 0 8px 0;
            font-size: 14px;
            color: #303133;

            &:first-child {
              margin-top: 0;
            }
          }

          ul {
            margin: 8px 0 0 0;
            padding-left: 20px;

            li {
              margin: 6px 0;
              font-size: 13px;
              color: #606266;
              line-height: 1.6;
            }
          }
        }
      }
    }

    .tips-card {
      border: none;
      border-radius: 16px;
      background: linear-gradient(135deg, #fff9e6 0%, #ffffff 100%);

      .tips-content {
        .tip-section {
          margin-bottom: 24px;

          &:last-child {
            margin-bottom: 0;
          }

          h5 {
            font-size: 15px;
            color: #303133;
            margin: 0 0 12px 0;
            font-weight: 600;
          }

          p {
            font-size: 14px;
            color: #606266;
            line-height: 1.8;
            margin: 8px 0;

            strong {
              color: #667eea;
              font-weight: 600;
            }
          }

          ul {
            margin: 8px 0 0 0;
            padding-left: 20px;

            li {
              font-size: 14px;
              color: #606266;
              margin: 8px 0;
              line-height: 1.6;
            }
          }
        }
      }
    }
  }

  .side-content {
    .preview-card {
      position: sticky;
      top: 20px;
      border: none;
      border-radius: 16px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);

      .preview-content {
        .preview-cover {
          position: relative;
          width: 100%;
          height: 220px;
          border-radius: 12px;
          overflow: hidden;
          margin-bottom: 20px;
          background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);

          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .no-cover {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #909399;
            gap: 8px;

            .el-icon {
              color: #c0c4cc;
            }
          }

          .type-badge {
            position: absolute;
            top: 16px;
            right: 16px;
            background: rgba(255, 255, 255, 0.98);
            padding: 8px 16px;
            border-radius: 20px;
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            font-weight: 600;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            backdrop-filter: blur(10px);
          }
        }

        .preview-info {
          h3 {
            font-size: 20px;
            font-weight: 700;
            color: #303133;
            margin: 0 0 16px 0;
            line-height: 1.4;
            min-height: 28px;
          }

          .preview-meta {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 20px;

            span {
              padding: 6px 14px;
              border-radius: 16px;
              font-size: 13px;
              font-weight: 500;
              display: flex;
              align-items: center;
              gap: 6px;
            }

            .difficulty-badge {
              background: linear-gradient(135deg, #e0f2ff 0%, #f0f7ff 100%);
              color: #409eff;
            }

            .duration-badge {
              background: linear-gradient(135deg, #fff8e1 0%, #ffecb3 100%);
              color: #f57c00;
            }

            .calories-badge {
              background: linear-gradient(135deg, #f0f9ff 0%, #e1f5fe 100%);
              color: #67c23a;
            }
          }

          .preview-body {
            .content-preview {
              font-size: 14px;
              color: #606266;
              line-height: 1.8;
              max-height: 500px;
              overflow-y: auto;
              padding: 16px;
              background: #f8f9fa;
              border-radius: 8px;

              :deep(h1),
              :deep(h2),
              :deep(h3) {
                margin-top: 16px;
                margin-bottom: 8px;
                color: #303133;
                font-weight: 600;
              }

              :deep(p) {
                margin-bottom: 8px;
              }

              :deep(ul),
              :deep(ol) {
                padding-left: 20px;
                margin-bottom: 8px;
              }

              :deep(strong) {
                color: #667eea;
                font-weight: 600;
              }
            }

            .no-content {
              color: #c0c4cc;
              font-style: italic;
              text-align: center;
              padding: 60px 20px;
              background: #f8f9fa;
              border-radius: 8px;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1400px) {
  .user-tutorial-publish-container {
    .content-wrapper {
      grid-template-columns: 1fr 380px;
    }
  }
}

@media (max-width: 1200px) {
  .user-tutorial-publish-container {
    .content-wrapper {
      grid-template-columns: 1fr;
      padding: 0 24px 24px;
    }

    .side-content {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .user-tutorial-publish-container {
    .page-header {
      padding: 24px 20px;

      .header-content {
        flex-direction: column;
        gap: 20px;

        .header-left {
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;

          .title-section {
            h1 {
              font-size: 24px;
            }

            .subtitle {
              font-size: 13px;
            }
          }
        }

        .header-actions {
          width: 100%;
          flex-direction: column;
          gap: 8px;

          .el-button {
            width: 100%;
            min-width: auto;
          }
        }
      }
    }

    .main-content {
      .template-card {
        .templates-grid {
          grid-template-columns: 1fr;
        }
      }

      .form-card {
        &.content-card {
          .content-editor-wrapper {
            &.mode-split {
              grid-template-columns: 1fr;
            }
          }

          .preview-mode-switcher {
            flex-wrap: wrap;

            .el-button {
              flex: 1;
              min-width: 60px;
            }
          }
        }
      }
    }

    // 移动端悬浮球组调整
    .floating-buttons-container {
      right: 20px;
      top: auto;
      bottom: 100px;
      transform: none !important;
      flex-direction: row;
      gap: 12px;

      .floating-action-button {
        width: 56px;
        height: 56px;

        &.secondary-button {
          width: 50px;
          height: 50px;

          .floating-icon {
            font-size: 22px;
          }
        }

        &:hover {
          transform: scale(1.05) !important;
        }

        &:active {
          transform: scale(0.95) !important;
        }

        .floating-icon {
          font-size: 24px;
        }

        .floating-tooltip {
          display: none;
        }
      }
    }
  }

  // 智能悬浮球组
  .floating-buttons-container {
    position: fixed;
    right: 6%;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    gap: 16px;
    z-index: 1000;

    .floating-action-button {
      position: relative;
      width: 64px;
      height: 64px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      backdrop-filter: blur(10px);

      &:not(.disabled):hover {
        transform: scale(1.1);
        box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);

        .floating-tooltip {
          opacity: 1;
          transform: translateY(-50%) translateX(-12px);
        }
      }

      &:not(.disabled):active {
        transform: scale(0.95);
      }

      &.disabled {
        opacity: 0.5;
        cursor: not-allowed;
        filter: grayscale(0.3);
      }

      .floating-icon {
        font-size: 28px;
        filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
      }

      .floating-tooltip {
        position: absolute;
        right: 76px;
        top: 50%;
        transform: translateY(-50%) translateX(-8px);
        background: rgba(0, 0, 0, 0.85);
        color: white;
        padding: 8px 16px;
        border-radius: 6px;
        font-size: 13px;
        font-weight: 500;
        white-space: nowrap;
        opacity: 0;
        transition: all 0.3s ease;
        pointer-events: none;

        &::after {
          content: '';
          position: absolute;
          right: -6px;
          top: 50%;
          transform: translateY(-50%);
          width: 0;
          height: 0;
          border-left: 6px solid rgba(0, 0, 0, 0.85);
          border-top: 6px solid transparent;
          border-bottom: 6px solid transparent;
        }
      }

      // 主按钮样式
      &.primary-button {
        animation: pulse-subtle 2s ease-in-out infinite;
      }

      // 辅助按钮样式
      &.secondary-button {
        // 稍微小一点
        width: 56px;
        height: 56px;

        .floating-icon {
          font-size: 24px;
        }
      }
    }
  }

  @keyframes pulse-subtle {
    0%, 100% {
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
    }
    50% {
      box-shadow: 0 8px 32px rgba(102, 126, 234, 0.6);
    }
  }
}
</style>
