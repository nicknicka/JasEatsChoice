<template>
  <div class="extraction-detail-dialog">
    <el-dialog
      v-model="dialogVisible"
      title="提取详情"
      width="900px"
      @close="handleClose"
    >
      <div v-loading="loading" class="detail-content">
        <!-- 基本信息 -->
        <div class="section" v-if="extraction">
          <h4 class="section-title">基本信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <label>菜品名称：</label>
              <el-input
                v-model="extraction.dishName"
                placeholder="菜品名称"
                :disabled="!isEditing"
              />
            </div>
            <div class="info-item">
              <label>难度：</label>
              <el-select v-model="extraction.difficulty" :disabled="!isEditing">
                <el-option label="简单" value="EASY" />
                <el-option label="中等" value="MEDIUM" />
                <el-option label="困难" value="HARD" />
              </el-select>
            </div>
            <div class="info-item">
              <label>制作时长：</label>
              <el-input-number
                v-model="extraction.cookingTime"
                :min="1"
                :disabled="!isEditing"
              />
              <span style="margin-left: 8px">分钟</span>
            </div>
            <div class="info-item">
              <label>卡路里：</label>
              <el-input-number
                v-model="extraction.calories"
                :min="0"
                :disabled="!isEditing"
              />
              <span style="margin-left: 8px">kcal</span>
            </div>
          </div>

          <div class="info-item full-width">
            <label>菜品描述：</label>
            <el-input
              v-model="extraction.description"
              type="textarea"
              :rows="3"
              :disabled="!isEditing"
            />
          </div>
        </div>

        <!-- 食材列表 -->
        <div class="section" v-if="extraction">
          <h4 class="section-title">
            食材列表
            <el-button
              type="primary"
              size="small"
              text
              @click="addIngredient"
              v-if="isEditing"
            >
              <el-icon><Plus /></el-icon>
              添加食材
            </el-button>
          </h4>
          <div class="ingredients-list">
            <div
              v-for="(ingredient, index) in extraction.ingredients"
              :key="index"
              class="ingredient-item"
            >
              <el-input
                v-model="ingredient.name"
                placeholder="食材名称"
                :disabled="!isEditing"
                style="width: 200px"
              />
              <el-input
                v-model="ingredient.amount"
                placeholder="用量"
                :disabled="!isEditing"
                style="width: 150px; margin-left: 8px"
              />
              <el-button
                type="danger"
                size="small"
                text
                @click="removeIngredient(index)"
                v-if="isEditing"
              >
                删除
              </el-button>
            </div>
            <el-empty
              v-if="!extraction.ingredients || extraction.ingredients.length === 0"
              description="暂无食材"
              :image-size="60"
            />
          </div>
        </div>

        <!-- 制作步骤 -->
        <div class="section" v-if="extraction">
          <h4 class="section-title">
            制作步骤
            <el-button
              type="primary"
              size="small"
              text
              @click="addStep"
              v-if="isEditing"
            >
              <el-icon><Plus /></el-icon>
              添加步骤
            </el-button>
          </h4>
          <div class="steps-list">
            <div
              v-for="(step, index) in extraction.steps"
              :key="index"
              class="step-item"
            >
              <div class="step-number">{{ index + 1 }}</div>
              <el-input
                v-model="step.description"
                type="textarea"
                :rows="2"
                :disabled="!isEditing"
                placeholder="步骤描述"
              />
              <el-button
                type="danger"
                size="small"
                text
                @click="removeStep(index)"
                v-if="isEditing"
              >
                删除
              </el-button>
            </div>
            <el-empty
              v-if="!extraction.steps || extraction.steps.length === 0"
              description="暂无步骤"
              :image-size="60"
            />
          </div>
        </div>

        <!-- 来源信息 -->
        <div class="section" v-if="extraction">
          <h4 class="section-title">来源信息</h4>
          <div class="source-info">
            <p><label>平台：</label>{{ extraction.platformName }}</p>
            <p><label>原始链接：</label>
              <el-link :href="extraction.contentUrl" target="_blank" type="primary">
                {{ truncateUrl(extraction.contentUrl) }}
              </el-link>
            </p>
            <p v-if="extraction.originalTitle">
              <label>原始标题：</label>{{ extraction.originalTitle }}
            </p>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleClose">关闭</el-button>
        <el-button v-if="!isEditing" type="primary" @click="isEditing = true">
          编辑
        </el-button>
        <template v-else>
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="saveExtraction" :loading="saving">
            保存
          </el-button>
        </template>
        <el-button
          v-if="extraction && !extraction.isPublished"
          type="success"
          @click="publishAsRecipe"
          :loading="publishing"
        >
          发布为食谱
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'

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

// 加载提取详情
const loadExtraction = async () => {
  if (!props.extractionId) return

  loading.value = true
  try {
    const response = await api.get(`/v1/content-extraction/extraction/${props.extractionId}`)
    if (response.data.code === 200) {
      extraction.value = response.data.data

      // 确保ingredients和steps是数组
      if (!extraction.value.ingredients) {
        extraction.value.ingredients = []
      }
      if (!extraction.value.steps) {
        extraction.value.steps = []
      }
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
    description: '',
    image: ''
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

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  loadExtraction() // 重新加载数据
}

// 保存提取内容
const saveExtraction = async () => {
  try {
    saving.value = true
    const response = await api.put('/v1/content-extraction/extraction', {
      extractionId: extraction.value.id,
      dishName: extraction.value.dishName,
      dishImage: extraction.value.dishImage,
      description: extraction.value.description,
      ingredients: extraction.value.ingredients,
      steps: extraction.value.steps,
      cookingTime: extraction.value.cookingTime,
      difficulty: extraction.value.difficulty,
      tags: extraction.value.tags,
      calories: extraction.value.calories
    })

    if (response.data.code === 200) {
      ElMessage.success('保存成功')
      isEditing.value = false
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
    const response = await api.post(
      `/v1/content-extraction/extraction/${extraction.value.id}/publish`
    )

    if (response.data.code === 200) {
      ElMessage.success('发布成功')
      emit('published')
      handleClose()
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

// 截断URL
const truncateUrl = (url) => {
  if (!url) return ''
  if (url.length > 50) {
    return url.substring(0, 50) + '...'
  }
  return url
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
.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item.full-width {
  grid-column: span 2;
  flex-direction: column;
  align-items: flex-start;
}

.info-item label {
  font-size: 14px;
  color: #606266;
  margin-right: 8px;
  white-space: nowrap;
}

.ingredients-list,
.steps-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ingredient-item {
  display: flex;
  align-items: center;
}

.step-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.step-number {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

.source-info p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

.source-info label {
  font-weight: 600;
  margin-right: 8px;
}
</style>
