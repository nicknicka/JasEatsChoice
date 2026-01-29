<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { VideoCamera, Document, Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()

// 表单数据
const tutorialForm = ref({
  title: '',
  type: 'article',
  content: '',
  difficulty: 'BEGINNER',
  duration: '',
  calories: null,
  prep_time: '',
  servings: null,
  cover_image: '',
  tags: []
})

// 提交状态
const submitting = ref(false)

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
    const response = await api.post(API_CONFIG.tutorial.userCreate, tutorialForm.value)

    if (response.data) {
      ElMessage.success('发布成功！您的教程已提交审核，审核通过后将展示在教程列表中')
      // 跳转到我的教程页面
      router.push('/user/my-tutorials')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败，请稍后重试')
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

  try {
    const response = await api.post(API_CONFIG.tutorial.userCreate, {
      ...tutorialForm.value,
      status: 'DRAFT',
      review_status: 'NOT_SUBMITTED'
    })

    if (response.data) {
      ElMessage.success('草稿已保存！')
      router.push('/user/my-tutorials')
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

// 返回
const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="user-tutorial-publish-container">
    <div class="page-header">
      <common-back-button type="primary" size="small" text="返回" @click="goBack" :use-router-back="false" />
      <h1>发布教程</h1>
      <p class="subtitle">分享你的美食经验，帮助更多人</p>
    </div>

    <div class="content-wrapper">
      <!-- 左侧：编辑表单 -->
      <div class="main-content">
        <el-card shadow="never">
          <el-form :model="tutorialForm" label-width="100px">
            <!-- 快速模板 -->
            <div class="quick-templates">
              <h4>
                <el-icon><Document /></el-icon>
                快速模板
              </h4>
              <div class="templates-grid">
                <div
                  v-for="(template, index) in quickTemplates"
                  :key="index"
                  class="template-card"
                  @click="useTemplate(template)"
                >
                  <el-icon v-if="template.type === 'video'"><VideoCamera /></el-icon>
                  <el-icon v-else><Document /></el-icon>
                  <span>{{ template.title }}</span>
                </div>
              </div>
            </div>

            <el-divider />

            <!-- 基本信息 -->
            <h3>基本信息</h3>
            <el-form-item label="教程标题" required>
              <el-input
                v-model="tutorialForm.title"
                placeholder="给教程起个好标题，例如：家常红烧肉的秘诀"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="教程类型" required>
              <el-radio-group v-model="tutorialForm.type">
                <el-radio value="article">
                  <el-icon><Document /></el-icon> 图文指南
                </el-radio>
                <el-radio value="video">
                  <el-icon><VideoCamera /></el-icon> 视频教程
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="难度">
              <el-radio-group v-model="tutorialForm.difficulty">
                <el-radio value="BEGINNER">初级</el-radio>
                <el-radio value="INTERMEDIATE">中级</el-radio>
                <el-radio value="ADVANCED">高级</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="时长">
              <el-input
                v-model="tutorialForm.duration"
                placeholder="例如: 15分钟（可选）"
              />
            </el-form-item>

            <!-- 详细信息 -->
            <h3>详细信息</h3>
            <el-form-item label="卡路里">
              <el-input-number
                v-model="tutorialForm.calories"
                :min="0"
                :step="10"
                placeholder="千卡"
              />
              <span class="form-tip">标注卡路里有助于健康管理</span>
            </el-form-item>

            <el-form-item label="准备时间">
              <el-input
                v-model="tutorialForm.prep_time"
                placeholder="例如: 20分钟（可选）"
              />
            </el-form-item>

            <el-form-item label="份量">
              <el-input-number
                v-model="tutorialForm.servings"
                :min="1"
                :max="20"
                placeholder="人份"
              />
            </el-form-item>

            <el-form-item label="封面图">
              <el-input
                v-model="tutorialForm.cover_image"
                placeholder="图片URL（可选）"
              >
                <template #append>
                  <el-button :icon="Upload">上传图片</el-button>
                </template>
              </el-input>
            </el-form-item>

            <!-- 教程内容 -->
            <h3>
              <el-icon><Document /></el-icon>
              教程内容
            </h3>
            <el-form-item label="内容" required>
              <el-input
                v-model="tutorialForm.content"
                type="textarea"
                :rows="15"
                placeholder="支持Markdown格式，例如：
## 标题
- 要点1
- 要点2

**粗体文字**
[链接](url)"
              />
              <div class="content-help">
                <p><strong>💡 内容提示：</strong></p>
                <ul>
                  <li>详细描述制作步骤或饮食建议</li>
                  <li>可以添加小贴士和注意事项</li>
                  <li>支持Markdown格式，排版更美观</li>
                </ul>
              </div>
            </el-form-item>

            <!-- 操作按钮 -->
            <el-form-item>
              <el-button @click="goBack">取消</el-button>
              <el-button type="info" @click="saveDraft">保存草稿</el-button>
              <el-button
                type="primary"
                @click="submitTutorial"
                :loading="submitting"
              >
                提交审核
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 提示信息 -->
        <el-card class="tips-card" shadow="never">
          <template #header>
            <h4>
              <el-icon><Document /></el-icon>
              发布须知
            </h4>
          </template>
          <div class="tips-content">
            <h5>✅ 内容要求</h5>
            <ul>
              <li>内容需原创，不得抄袭</li>
              <li>步骤清晰，易于理解</li>
              <li>图片清晰，画质良好</li>
              <li>语言文明，友善交流</li>
            </ul>

            <h5>⏰ 审核时效</h5>
            <p>提交后，我们将在 <strong>1-3个工作日</strong> 内完成审核</p>
            <p>审核通过后，您的教程将展示在"制作教程与指南"板块</p>

            <h5>📝 审核标准</h5>
            <ul>
              <li>内容完整度：步骤/信息是否详细</li>
              <li>实用价值：是否有实际指导意义</li>
              <li>原创性：是否为原创内容</li>
              <li>规范性：是否符合社区规范</li>
            </ul>

            <h5>⚠️ 注意事项</h5>
            <ul>
              <li>请勿发布虚假信息</li>
              <li>请勿发布违反法律法规的内容</li>
              <li>请勿发布广告或推广信息</li>
              <li>审核通过后，您的教程将展示给所有用户</li>
            </ul>
          </div>
        </el-card>
      </div>

      <!-- 右侧：我的教程预览 -->
      <div class="side-content">
        <el-card shadow="never">
          <template #header>
            <h4>教程预览</h4>
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
                  {{ tutorialForm.difficulty === 'BEGINNER' ? '初级' : tutorialForm.difficulty === 'INTERMEDIATE' ? '中级' : '高级' }}
                </span>
                <span v-if="tutorialForm.duration" class="duration-badge">
                  {{ tutorialForm.duration }}
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
  </div>
</template>

<style scoped lang="less">
.user-tutorial-publish-container {
  padding: 20px;

  .page-header {
    margin-bottom: 24px;

    h1 {
      font-size: 28px;
      color: #303133;
      margin: 0 0 8px 0;
    }

    .subtitle {
      color: #909399;
      margin: 0;
      font-size: 14px;
    }
  }

  .content-wrapper {
    display: grid;
    grid-template-columns: 1fr 380px;
    gap: 24px;
  }

  .main-content {
    .quick-templates {
      margin-bottom: 20px;
      padding: 16px;
      background: #f8f9fa;
      border-radius: 8px;

      h4 {
        display: flex;
        align-items: center;
        gap: 8px;
        margin: 0 0 12px 0;
        font-size: 16px;
        color: #303133;
      }

      .templates-grid {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 12px;

        .template-card {
          padding: 12px;
          background: white;
          border: 1px solid #e0e0e0;
          border-radius: 8px;
          text-align: center;
          cursor: pointer;
          transition: all 0.3s;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 8px;

          &:hover {
            border-color: #409eff;
            background: #f0f7ff;
            transform: translateY(-2px);
          }

          .el-icon {
            font-size: 24px;
            color: #409eff;
          }

          span {
            font-size: 13px;
            color: #606266;
          }
        }
      }
    }

    h3 {
      font-size: 18px;
      color: #303133;
      margin: 28px 0 16px 0;
      padding-bottom: 8px;
      border-bottom: 2px solid #ff6b6b;

      &:first-of-type {
        margin-top: 0;
      }
    }

    .form-tip {
      margin-left: 10px;
      color: #909399;
      font-size: 12px;
    }

    .content-help {
      margin-top: 10px;
      padding: 12px;
      background: #f0f7ff;
      border-left: 3px solid #409eff;
      border-radius: 4px;

      p {
        margin: 8px 0 0 0;
        font-size: 13px;
        color: #606266;

        &:first-child {
          margin-top: 0;
        }
      }

      ul {
        margin: 8px 0 0 16px;
        padding-left: 20px;

        li {
          margin: 4px 0;
          font-size: 13px;
          color: #606266;
        }
      }
    }

    .tips-card {
      margin-top: 24px;

      .tips-content {
        h5 {
          font-size: 14px;
          color: #303133;
          margin: 16px 0 8px 0;
        }

        p {
          font-size: 13px;
          color: #606266;
          line-height: 1.6;
          margin: 8px 0;

          strong {
            color: #303133;
          }
        }

        ul {
          margin: 8px 0 16px 0;
          padding-left: 20px;

          li {
            font-size: 13px;
            color: #606266;
            margin: 4px 0;
          }
        }
      }
    }
  }

  .side-content {
    .preview-content {
      position: sticky;
      top: 20px;

      .preview-cover {
        position: relative;
        width: 100%;
        height: 200px;
        border-radius: 8px;
        overflow: hidden;
        margin-bottom: 16px;
        background: #f5f5f5;

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

          .el-icon {
            margin-bottom: 8px;
          }
        }

        .type-badge {
          position: absolute;
          top: 12px;
          right: 12px;
          background: rgba(255, 255, 255, 0.95);
          padding: 6px 12px;
          border-radius: 16px;
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          font-weight: 600;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
      }

      .preview-info {
        h3 {
          font-size: 18px;
          color: #303133;
          margin: 0 0 12px 0;
          line-height: 1.4;
          min-height: 24px;
        }

        .preview-meta {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          margin-bottom: 16px;

          span {
            padding: 4px 10px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 500;
          }

          .difficulty-badge {
            background: #e0f2ff;
            color: #409eff;
          }

          .duration-badge {
            background: #fff8e1;
            color: #feca57;
          }

          .calories-badge {
            background: #f0f9ff;
            color: #67c23a;
          }
        }

        .preview-body {
          .content-preview {
            font-size: 14px;
            color: #606266;
            line-height: 1.8;
            max-height: 400px;
            overflow-y: auto;

            :deep(h1),
            :deep(h2),
            :deep(h3) {
              margin-top: 16px;
              margin-bottom: 8px;
              color: #303133;
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
              color: #ff6b6b;
            }
          }

          .no-content {
            color: #c0c4cc;
            font-style: italic;
            text-align: center;
            padding: 40px 0;
          }
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }

  .side-content {
    display: none;
  }
}
</style>
