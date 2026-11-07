<template>
  <div class="smart-remark-input">
    <!-- 口味标签选择区 -->
    <div class="taste-tags-section">
      <div class="section-title">
        <el-icon><Star /></el-icon>
        口味偏好（可选）
      </div>

      <!-- 分类标签 -->
      <div class="tag-categories">
        <!-- 辣度标签 -->
        <div class="tag-category">
          <div class="category-name">辣度</div>
          <div class="tag-list">
            <el-tag
              v-for="tag in spicyTags"
              :key="tag.code"
              :type="selectedTasteTags.includes(tag.code) ? 'danger' : 'info'"
              :effect="selectedTasteTags.includes(tag.code) ? 'dark' : 'plain'"
              @click="toggleTag(tag.code)"
              class="taste-tag"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>

        <!-- 甜度标签 -->
        <div class="tag-category">
          <div class="category-name">甜度</div>
          <div class="tag-list">
            <el-tag
              v-for="tag in sweetTags"
              :key="tag.code"
              :type="selectedTasteTags.includes(tag.code) ? 'success' : 'info'"
              :effect="selectedTasteTags.includes(tag.code) ? 'dark' : 'plain'"
              @click="toggleTag(tag.code)"
              class="taste-tag"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>

        <!-- 食材排除标签 -->
        <div class="tag-category">
          <div class="category-name">不要放</div>
          <div class="tag-list">
            <el-tag
              v-for="tag in exclusionTags"
              :key="tag.code"
              :type="selectedTasteTags.includes(tag.code) ? 'warning' : 'info'"
              :effect="selectedTasteTags.includes(tag.code) ? 'dark' : 'plain'"
              @click="toggleTag(tag.code)"
              class="taste-tag"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 备注输入区 -->
    <div class="remark-input-section">
      <div class="section-title">
        <el-icon><Edit /></el-icon>
        自定义备注
      </div>
      <el-input
        v-model="customRemark"
        type="textarea"
        :rows="3"
        placeholder="请输入您的特殊要求..."
        @input="handleRemarkInput"
        :maxlength="200"
        show-word-limit
      />
    </div>

    <!-- 冲突提示区 -->
    <div v-if="conflictResult.hasConflict" class="conflict-alert">
      <el-alert
        :type="getAlertType()"
        :closable="false"
        show-icon
      >
        <template #title>
          <span class="conflict-title">{{ getConflictTitle() }}</span>
        </template>
        <div class="conflict-list">
          <div
            v-for="(conflict, index) in conflictResult.conflicts"
            :key="index"
            class="conflict-item"
            :style="{ borderLeftColor: conflict.color }"
          >
            <div class="conflict-header">
              <el-tag :type="getConflictTagType(conflict.priority)" size="small">
                {{ getConflictTypeText(conflict.conflictType) }}
              </el-tag>
              <span class="conflict-description">{{ conflict.description }}</span>
            </div>
            <div class="conflict-item-detail">
              <span class="conflict-ingredient">涉及食材：{{ conflict.conflictItem }}</span>
            </div>
          </div>
        </div>

        <!-- 建议方案 -->
        <div v-if="conflictResult.suggestions.length > 0" class="suggestions">
          <div class="suggestion-title">建议：</div>
          <ul>
            <li v-for="(suggestion, index) in conflictResult.suggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </div>
      </el-alert>
    </div>

    <!-- 格式化备注预览 -->
    <div v-if="formattedRemark" class="remark-preview">
      <div class="section-title">
        <el-icon><View /></el-icon>
        备注预览
      </div>
      <div class="preview-content">{{ formattedRemark }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Star, Edit, View, Warning } from '@element-plus/icons-vue'
import api from '../../utils/api.js'

// Props
const props = defineProps({
  dishId: {
    type: String,
    required: true
  },
  userAllergies: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['update:modelValue'])

// 数据
const selectedTasteTags = ref([])
const customRemark = ref('')
const conflictResult = ref({
  hasConflict: false,
  conflicts: [],
  suggestions: [],
  conflictLevel: null
})
const checkingTimer = ref(null)

// 口味标签定义
const spicyTags = [
  { code: 'mild_no_spicy', name: '免辣' },
  { code: 'mild_spicy', name: '微辣' },
  { code: 'medium_spicy', name: '中辣' },
  { code: 'hot_spicy', name: '特辣' }
]

const sweetTags = [
  { code: 'no_sugar', name: '免糖' },
  { code: 'less_sugar', name: '少糖' },
  { code: 'normal_sugar', name: '正常糖' },
  { code: 'extra_sugar', name: '多糖' }
]

const exclusionTags = [
  { code: 'no_onion', name: '不要葱' },
  { code: 'no_ginger', name: '不要姜' },
  { code: 'no_garlic', name: '不要蒜' },
  { code: 'no_coriander', name: '不要香菜' },
  { code: 'no_sesame', name: '不要芝麻' },
  { code: 'no_pepper', name: '不要胡椒' },
  { code: 'no_vinegar', name: '不要醋' }
]

// 计算属性：格式化备注
const formattedRemark = computed(() => {
  if (selectedTasteTags.value.length === 0 && !customRemark.value.trim()) {
    return ''
  }

  let result = ''
  const tagNames = []

  // 按类别分组
  const selectedSpicy = spicyTags.filter(t => selectedTasteTags.value.includes(t.code))
  const selectedSweet = sweetTags.filter(t => selectedTasteTags.value.includes(t.code))
  const selectedExclusion = exclusionTags.filter(t => selectedTasteTags.value.includes(t.code))

  if (selectedSpicy.length > 0) {
    tagNames.push('【' + selectedSpicy.map(t => t.name).join('、') + '】')
  }
  if (selectedSweet.length > 0) {
    tagNames.push('【' + selectedSweet.map(t => t.name).join('、') + '】')
  }
  if (selectedExclusion.length > 0) {
    tagNames.push('【' + selectedExclusion.map(t => t.name).join('、') + '】')
  }

  result = tagNames.join(' ')

  if (customRemark.value.trim()) {
    result += ' ' + customRemark.value.trim()
  }

  return result
})

// 切换标签
const toggleTag = (tagCode) => {
  const index = selectedTasteTags.value.indexOf(tagCode)
  if (index > -1) {
    selectedTasteTags.value.splice(index, 1)
  } else {
    selectedTasteTags.value.push(tagCode)
  }

  // 触发冲突检测
  checkConflict()

  // 通知父组件
  emitValueChange()
}

// 处理备注输入
const handleRemarkInput = () => {
  // 防抖处理
  if (checkingTimer.value) {
    clearTimeout(checkingTimer.value)
  }

  checkingTimer.value = setTimeout(() => {
    checkConflict()
    emitValueChange()
  }, 500)
}

// 检测冲突
const checkConflict = async () => {
  if (!props.dishId) {
    return
  }

  try {
    const response = await api.post('/v1/remark-conflict/check', {
      dishId: props.dishId,
      remark: customRemark.value,
      tasteTags: selectedTasteTags.value,
      userAllergies: props.userAllergies
    })

    if (response.data.success) {
      conflictResult.value = response.data.data
    }
  } catch (error) {
    console.error('冲突检测失败:', error)
  }
}

// 获取警告类型
const getAlertType = () => {
  if (conflictResult.value.conflictLevel === 'HIGH') {
    return 'error'
  } else if (conflictResult.value.conflictLevel === 'MEDIUM_HIGH') {
    return 'warning'
  }
  return 'info'
}

// 获取冲突标题
const getConflictTitle = () => {
  if (conflictResult.value.conflictLevel === 'HIGH') {
    return '⚠️ 严重冲突 - 过敏风险'
  } else if (conflictResult.value.conflictLevel === 'MEDIUM_HIGH') {
    return '⚠️ 注意 - 备注冲突'
  }
  return 'ℹ️ 提示 - 可能的冲突'
}

// 获取冲突标签类型
const getConflictTagType = (priority) => {
  if (priority === 1) return 'danger'
  if (priority === 2) return 'warning'
  if (priority === 3) return 'primary'
  return 'info'
}

// 获取冲突类型文本
const getConflictTypeText = (type) => {
  const typeMap = {
    'ALLERGY': '过敏',
    'INCOMPATIBLE': '冲突',
    'CUISINE': '禁忌',
    'TAG_MISMATCH': '标签冲突'
  }
  return typeMap[type] || type
}

// 通知父组件值变化
const emitValueChange = () => {
  emit('update:modelValue', formattedRemark.value)
}

// 监听格式化备注变化
watch(formattedRemark, (newValue) => {
  emit('update:modelValue', newValue)
})

// 生命周期
onMounted(() => {
  // 初始检测冲突
  checkConflict()
})

// 监听菜品ID变化
watch(() => props.dishId, () => {
  checkConflict()
})

// 暴露方法给父组件
defineExpose({
  getFormattedRemark: () => formattedRemark.value,
  checkConflict,
  reset: () => {
    selectedTasteTags.value = []
    customRemark.value = ''
    conflictResult.value = {
      hasConflict: false,
      conflicts: [],
      suggestions: [],
      conflictLevel: null
    }
  }
})
</script>

<style scoped>
.smart-remark-input {
  padding: 20px;
}

.taste-tags-section,
.remark-input-section,
.remark-preview {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.143rem /* 原值: 16px */;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.tag-categories {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tag-category {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px;
}

.category-name {
  font-size: 1rem /* 原值: 14px */;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.taste-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.taste-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.conflict-alert {
  margin-bottom: 20px;
}

.conflict-title {
  font-weight: 600;
}

.conflict-list {
  margin-top: 12px;
}

.conflict-item {
  padding: 12px;
  margin-bottom: 12px;
  background: #fef0f0;
  border-left: 4px solid #f56c6c;
  border-radius: 4px;
}

.conflict-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.conflict-description {
  font-size: 1rem /* 原值: 14px */;
  font-weight: 500;
  color: #303133;
}

.conflict-item-detail {
  font-size: 0.857rem /* 原值: 12px */;
  color: #606266;
}

.conflict-ingredient {
  font-weight: 500;
}

.suggestions {
  margin-top: 16px;
  padding: 12px;
  background: #f4f4f5;
  border-radius: 4px;
}

.suggestion-title {
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.suggestions ul {
  margin: 0;
  padding-left: 20px;
}

.suggestions li {
  margin-bottom: 4px;
  color: #606266;
}

.remark-preview {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
}

.preview-content {
  font-size: 1rem /* 原值: 14px */;
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
