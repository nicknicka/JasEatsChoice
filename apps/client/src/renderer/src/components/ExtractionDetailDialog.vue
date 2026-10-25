<template>
  <div class="extraction-detail-dialog">
    <el-dialog
      v-model="dialogVisible"
      title="提取详情"
      width="650px"
      @close="handleClose"
    >
      <div v-loading="loading" class="detail-content">
        <!-- 菜品头部信息 -->
        <div class="dish-header" v-if="extraction">
          <div class="dish-image">
            <img
              v-if="extraction.dishImage"
              :src="extraction.dishImage"
              :alt="extraction.dishName"
            />
            <div v-else class="no-image">
              <el-icon :size="48"><Picture /></el-icon>
            </div>
          </div>
          <div class="dish-info">
            <h3 class="dish-name">{{ extraction.dishName || '未命名菜品' }}</h3>
            <div class="dish-meta">
              <el-tag :type="getDifficultyType(extraction.difficulty)" size="small">
                {{ getDifficultyText(extraction.difficulty) }}
              </el-tag>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ extraction.cookingTime }}分钟
              </span>
              <span class="meta-item" v-if="extraction.calories">
                🔥 {{ extraction.calories }} kcal
              </span>
            </div>
            <!-- 标签 -->
            <div class="tags-wrapper" v-if="extraction.tags && extraction.tags.length > 0">
              <el-tag
                v-for="(tag, index) in extraction.tags"
                :key="index"
                size="small"
                type="info"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 菜品描述 -->
        <div class="section description-section" v-if="extraction && extraction.description">
          <h4 class="section-title">菜品描述</h4>
          <p class="description-text">{{ extraction.description }}</p>
        </div>

        <el-divider />

        <!-- 食材列表 -->
        <div class="section" v-if="extraction">
          <h4 class="section-title">
            <el-icon><ShoppingBag /></el-icon>
            食材清单
            <el-button
              type="primary"
              size="small"
              @click="addIngredient"
              v-if="isEditing"
              class="add-btn"
            >
              <el-icon><Plus /></el-icon>
              添加食材
            </el-button>
          </h4>
          <div class="ingredients-list" v-if="extraction.ingredients && extraction.ingredients.length > 0">
            <div
              v-for="(ingredient, index) in extraction.ingredients"
              :key="index"
              class="ingredient-item"
            >
              <div class="ingredient-checkbox" v-if="!isEditing">
                <el-checkbox :model-value="false" disabled />
              </div>
              <div class="ingredient-content">
                <span class="ingredient-name">{{ ingredient.name }}</span>
                <span class="ingredient-amount" v-if="ingredient.amount">{{ ingredient.amount }}</span>
              </div>
              <el-button
                type="danger"
                size="small"
                circle
                :icon="Delete"
                @click="removeIngredient(index)"
                v-if="isEditing"
                class="remove-btn"
              />
            </div>
          </div>
          <el-empty
            v-if="!extraction.ingredients || extraction.ingredients.length === 0"
            description="暂无食材"
            :image-size="60"
          />
        </div>

        <el-divider />

        <!-- 制作步骤 -->
        <div class="section steps-section" v-if="extraction">
          <h4 class="section-title">
            <el-icon><List /></el-icon>
            制作步骤
            <el-button
              type="primary"
              size="small"
              @click="addStep"
              v-if="isEditing"
              class="add-btn"
            >
              <el-icon><Plus /></el-icon>
              添加步骤
            </el-button>
          </h4>

          <div class="steps-wrapper" v-if="extraction.steps && extraction.steps.length > 0">
            <div
              v-for="(step, index) in extraction.steps"
              :key="index"
              class="step-card"
            >
              <div class="step-card-header">
                <div class="step-number-with-title">
                  <div class="step-number-circle">{{ step.stepNumber || index + 1 }}</div>
                  <!-- 步骤标题 -->
                  <template v-if="isEditing || step.isNew">
                    <el-input
                      v-model="step.title"
                      placeholder="步骤标题（如：准备食材）"
                      class="step-title-input"
                    />
                  </template>
                  <span v-else class="step-title-text">{{ step.title || '暂无标题' }}</span>
                </div>
                <div class="step-actions" v-if="isEditing || step.isNew">
                  <el-button
                    type="primary"
                    size="small"
                    circle
                    @click="moveStepUp(index)"
                    :disabled="index === 0"
                    title="上移"
                    class="move-btn"
                  >
                    <el-icon><Top /></el-icon>
                  </el-button>
                  <el-button
                    type="primary"
                    size="small"
                    circle
                    @click="moveStepDown(index)"
                    :disabled="index === extraction.steps.length - 1"
                    title="下移"
                    class="move-btn"
                  >
                    <el-icon><Bottom /></el-icon>
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    circle
                    :icon="Delete"
                    @click="removeStep(index)"
                    class="remove-step-btn"
                  />
                </div>
              </div>
              <div class="step-card-body">
                <!-- 步骤详细描述 -->
                <el-input
                  v-model="step.description"
                  type="textarea"
                  :rows="4"
                  :disabled="!isEditing && !step.isNew"
                  placeholder="详细说明此步骤的具体操作..."
                  class="step-desc-textarea"
                />
              </div>
            </div>
          </div>
          <el-empty
            v-if="!extraction.steps || extraction.steps.length === 0"
            description="暂无步骤"
            :image-size="60"
          />
        </div>

        <el-divider />

        <!-- 编辑模式下的基本信息编辑 -->
        <div class="section edit-section" v-if="isEditing && extraction">
          <h4 class="section-title">编辑信息</h4>
          <div class="edit-grid">
            <div class="edit-item">
              <label>菜品名称</label>
              <el-input v-model="extraction.dishName" placeholder="菜品名称" />
            </div>
            <div class="edit-item">
              <label>难度</label>
              <el-select v-model="extraction.difficulty" placeholder="选择难度">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </div>
            <div class="edit-item">
              <label>制作时长（分钟）</label>
              <el-input-number v-model="extraction.cookingTime" :min="1" />
            </div>
            <div class="edit-item">
              <label>卡路里（kcal）</label>
              <el-input-number v-model="extraction.calories" :min="0" />
            </div>
            <div class="edit-item full-width">
              <label>菜品描述</label>
              <el-input
                v-model="extraction.description"
                type="textarea"
                :rows="3"
                placeholder="菜品描述"
              />
            </div>
          </div>
        </div>

        <!-- 来源信息 -->
        <div class="section source-section" v-if="extraction">
          <h4 class="section-title">
            <el-icon><Link /></el-icon>
            来源信息
          </h4>
          <div class="source-info-grid">
            <div class="source-info-item">
              <label>平台</label>
              <span class="platform-name">{{ extraction.platformName }}</span>
            </div>
            <div class="source-info-item full-width">
              <label>原始链接</label>
              <el-link
                :href="extraction.contentUrl"
                target="_blank"
                type="primary"
                :underline="false"
              >
                {{ extraction.contentUrl }}
                <el-icon class="link-icon"><TopRight /></el-icon>
              </el-link>
            </div>
            <div class="source-info-item full-width" v-if="extraction.originalTitle">
              <label>原始标题</label>
              <span class="original-title">{{ extraction.originalTitle }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose" size="large">关闭</el-button>
          <el-button
            v-if="!isEditing"
            type="primary"
            @click="isEditing = true"
            size="large"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <template v-else>
            <el-button @click="cancelEdit" size="large">取消</el-button>
            <el-button
              type="primary"
              @click="saveExtraction"
              :loading="saving"
              size="large"
            >
              <el-icon><Check /></el-icon>
              保存修改
            </el-button>
          </template>
          <el-button
            v-if="extraction && !extraction.isPublished"
            type="success"
            @click="publishAsRecipe"
            :loading="publishing"
            size="large"
          >
            <el-icon><Upload /></el-icon>
            发布为食谱
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Delete,
  Edit,
  Check,
  Upload,
  Picture,
  Clock,
  ShoppingBag,
  List,
  Link,
  TopRight,
  Top,
  Bottom
} from '@element-plus/icons-vue'
import contentExtractionApi from '@/api/contentExtraction'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  extractionId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'published'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const loading = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const publishing = ref(false)
const extraction = ref(null)

// 获取难度类型
const getDifficultyType = (difficulty) => {
  const typeMap = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return typeMap[difficulty] || 'info'
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const textMap = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return textMap[difficulty] || '未知'
}

// 加载提取详情
const loadExtraction = async () => {
  if (!props.extractionId) return

  loading.value = true
  try {
    const response = await contentExtractionApi.getExtractionDetail(props.extractionId)
    if (response.code === '200' || response.code === 200) {
      extraction.value = response.data

      // 确保ingredients和steps是数组
      if (!extraction.value.ingredients) {
        extraction.value.ingredients = []
      }
      if (!extraction.value.steps) {
        extraction.value.steps = []
      }
      if (!extraction.value.tags) {
        extraction.value.tags = []
      }
      // 确保每个步骤都有title字段
      extraction.value.steps.forEach(step => {
        if (!step.title) {
          step.title = ''
        }
      })
    } else {
      ElMessage.error(response.message || '加载失败')
    }
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 添加食材
const addIngredient = () => {
  extraction.value.ingredients.push({
    name: '',
    amount: ''
  })
}

// 删除食材
const removeIngredient = (index) => {
  extraction.value.ingredients.splice(index, 1)
}

// 添加步骤
const addStep = () => {
  extraction.value.steps.push({
    stepNumber: extraction.value.steps.length + 1,
    title: '',
    description: '',
    image: '',
    isNew: true // 标记为新增步骤
  })
}

// 删除步骤
const removeStep = (index) => {
  extraction.value.steps.splice(index, 1)
  // 重新编号
  extraction.value.steps.forEach((step, i) => {
    step.stepNumber = i + 1
  })
}

// 上移步骤
const moveStepUp = (index) => {
  if (index === 0) return
  const steps = extraction.value.steps
  const temp = steps[index]
  steps[index] = steps[index - 1]
  steps[index - 1] = temp
  // 重新编号
  steps.forEach((step, i) => {
    step.stepNumber = i + 1
  })
}

// 下移步骤
const moveStepDown = (index) => {
  const steps = extraction.value.steps
  if (index === steps.length - 1) return
  const temp = steps[index]
  steps[index] = steps[index + 1]
  steps[index + 1] = temp
  // 重新编号
  steps.forEach((step, i) => {
    step.stepNumber = i + 1
  })
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  loadExtraction() // 重新加载数据
}

// 保存提取内容
const saveExtraction = async () => {
  try {
    saving.value = true
    // 清理步骤中的isNew标识
    const cleanSteps = extraction.value.steps.map(step => {
      const { isNew, ...stepData } = step
      return stepData
    })

    const response = await contentExtractionApi.updateExtraction(extraction.value.id, {
      extractionId: extraction.value.id,
      dishName: extraction.value.dishName,
      dishImage: extraction.value.dishImage,
      description: extraction.value.description,
      ingredients: extraction.value.ingredients,
      steps: cleanSteps,
      cookingTime: extraction.value.cookingTime,
      difficulty: extraction.value.difficulty,
      tags: extraction.value.tags,
      calories: extraction.value.calories
    })

    if (response.code === '200' || response.code === 200) {
      ElMessage.success('保存成功')
      isEditing.value = false
      // 移除所有步骤的isNew标识
      extraction.value.steps.forEach(step => {
        delete step.isNew
      })
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 发布为食谱
const publishAsRecipe = async () => {
  try {
    publishing.value = true
    const response = await contentExtractionApi.publishAsRecipe(extraction.value.id, {})

    if (response.code === '200' || response.code === 200) {
      ElMessage.success('发布成功')
      emit('published')
      handleClose()
    } else {
      ElMessage.error(response.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const handleClose = () => {
  isEditing.value = false
  extraction.value = null
  emit('update:visible', false)
}

// 监听visible变化
watch(() => props.visible, (val) => {
  if (val) {
    loadExtraction()
  }
})
</script>

<style scoped>
.extraction-detail-dialog {
  --primary-color: #409eff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --text-color: #303133;
  --border-color: #dcdfe6;
}

.detail-content {
  max-height: 55vh;
  overflow-y: auto;
  padding: 0;
}

@media (max-width: 768px) {
  .detail-content {
    max-height: 45vh;
  }
}

/* 菜品头部 */
.dish-header {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c6e2ff 100%);
  border-radius: 10px;
  margin-bottom: 16px;
}

.dish-image {
  width: 180px;
  height: 180px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0.08);
}

.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dish-image .no-image {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
  color: #c0c4cc;
}

.dish-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-name {
  margin: 0;
  font-size: 1.429rem /* 原值: 20px */;
  font-weight: bold;
  color: var(--text-color);
}

.dish-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.929rem /* 原值: 13px */;
  color: #606266;
}

.tags-wrapper {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 描述区域 */
.description-section {
  padding: 0 24px;
}

.description-text {
  margin: 0;
  font-size: 1rem /* 原值: 14px */;
  line-height: 1.7;
  color: #606266;
}

/* 分割线 */
:deep(.el-divider) {
  margin: 16px 0;
}

/* 章节 */
.section {
  padding: 0 16px 16px;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 1.143rem /* 原值: 16px */;
  font-weight: 600;
  color: var(--text-color);
  display: flex;
  align-items: center;
  gap: 8px;
}

.add-btn {
  margin-left: auto;
}

/* 食材列表 */
.ingredients-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
}

.ingredient-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f9fafc;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: all 0.3s;
}

.ingredient-item:hover {
  background: #ecf5ff;
  border-color: var(--primary-color);
}

.ingredient-checkbox {
  flex-shrink: 0;
}

.ingredient-content {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ingredient-name {
  font-size: 1rem /* 原值: 14px */;
  color: var(--text-color);
  font-weight: 500;
  flex: 1;
}

.ingredient-amount {
  font-size: 0.857rem /* 原值: 12px */;
  color: #606266;
  background: #fff;
  padding: 2px 8px;
  border-radius: 8px;
  border: 1px solid #e4e7ec;
}

.remove-btn {
  flex-shrink: 0;
  opacity: 0.6;
}

.remove-btn:hover {
  opacity: 1;
}

:deep(.el-button--small) {
  padding: 4px 8px;
}

/* 步骤区域 */
.steps-section {
  padding: 0 16px 24px;
}

/* 步骤列表 */
.steps-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.step-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.step-card:hover {
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.12);
  border-color: #c6e2ff;
  transform: translateY(-2px);
}

.step-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ecf5ff 100%);
  border-bottom: 1px solid #e4e7ed;
}

.step-number-with-title {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}
.step-number-circle {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, #66b1ff 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: bold;
  flex-shrink: 0;
  box-shadow: 0 3px 8px rgba(64, 158, 255, 0.35);
}

.step-actions {
  display: flex;
  gap: 4px;
  align-items: center;
  margin-left: 16px;
}

.move-btn {
  opacity: 0.75;
  transition: all 0.2s;
}

.move-btn:hover:not(:disabled) {
  opacity: 1;
  transform: scale(1.05);
}
.step-card-body {
  padding: 24px 20px 24px 80px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.remove-step-btn {
  flex-shrink: 0;
}


.step-title-input {
  flex: 1;
  min-width: 200px;
}

/* 禁用状态时隐藏标题输入框 */
:deep(.step-title-input .el-input__wrapper.is-disabled) {
  display: none !important;
}

:deep(.step-title-input .el-input__wrapper) {
  background: #f9fafb;
  font-weight: 600;
  font-size: 1.143rem /* 原值: 16px */;
  padding: 11px 15px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.2s;
}

:deep(.step-title-input .el-input__wrapper:hover) {
  background: #f5f7fa;
  border-color: #c6e2ff;
}

:deep(.step-title-input .el-input__wrapper.is-focus) {
  background: #fff;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

.step-desc-textarea {
  width: 100%;
}

:deep(.step-desc-textarea .el-textarea__inner) {
  font-size: 1rem /* 原值: 14px */;
  line-height: 1.8;
  color: #606266;
  padding: 16px 18px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  background: #f9fafb;
  transition: all 0.2s;
}

:deep(.step-desc-textarea .el-textarea__inner:hover) {
  background: #f5f7fa;
  border-color: #c6e2ff;
}

:deep(.step-desc-textarea .el-textarea__inner:focus) {
  background: #fff;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
}

/* 禁用状态 */
:deep(.step-title-input .el-input__wrapper.is-disabled),
:deep(.step-desc-textarea .el-textarea__inner:disabled) {
  background: #f9fafb !important;
  border-color: #e4e7ed !important;
  padding: 16px 18px !important;
}

:deep(.step-title-input .el-input__inner:disabled),
:deep(.step-desc-textarea .el-textarea__inner:disabled) {
  color: #909399;
}

/* 禁用状态下的步骤标题文本 */
.step-title-text {
  display: block;
  width: 100%;
  padding: 8px 0;
  font-size: 1.143rem /* 原值: 16px */;
  font-weight: 600;
  color: #606266;
  line-height: 1.5;
  border-left: 2px solid #e4e7ed;
  padding-left: 10px;
}
.edit-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.edit-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.edit-item.full-width {
  grid-column: span 2;
}

.edit-item label {
  font-size: 0.929rem /* 原值: 13px */;
  font-weight: 600;
  color: var(--text-color);
}

/* 来源信息 */
.source-section {
  background: #fef0f0;
  border-radius: 8px;
  padding: 16px;
}

.source-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.source-info-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
}

.source-info-item.full-width {
  grid-column: span 2;
}

.source-info-item label {
  font-size: 0.929rem /* 原值: 13px */;
  color: #909399;
  font-weight: 500;
  min-width: 70px;
  flex-shrink: 0;
}

.platform-name {
  font-size: 1.071rem /* 原值: 15px */;
  color: var(--text-color);
  font-weight: 500;
}

.original-title {
  font-size: 1rem /* 原值: 14px */;
  color: #606266;
}

.link-icon {
  margin-left: 4px;
  font-size: 0.857rem /* 原值: 12px */;
}

/* 底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px 8px;
  flex-wrap: wrap;
}

/* 响应式 */
@media (max-width: 1024px) {
  .dish-header {
    padding: 20px;
  }

  .edit-grid,
  .source-info-grid {
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .dish-header {
    flex-direction: column;
    padding: 16px;
  }

  .dish-image {
    width: 100%;
    height: 160px;
  }

  .edit-grid,
  .source-info-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .ingredients-list {
    grid-template-columns: 1fr;
  }

  .step-card {
    padding: 12px;
  }
}

@media (max-width: 480px) {
  .dish-image {
    height: 140px;
  }

  .dialog-footer {
    flex-direction: column;
  }

  .dialog-footer .el-button {
    width: 100%;
  }

  .ingredient-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>
