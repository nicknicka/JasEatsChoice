<template>
  <el-dialog
    :title="mode === 'add' ? '添加饮食记录' : '编辑饮食记录'"
    v-model="dialogVisible"
    width="720px"
    top="8%"
    transition="dialog-fade"
    class="add-diet-dialog"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
    >
      <!-- 餐次选择 -->
      <el-row justify="center">
        <el-col :xs="24" :sm="20">
          <el-form-item label="餐次" required prop="mealType">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><ListIcon /></el-icon>
                <span>餐&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;次</span>
              </div>
            </template>
            <el-select
              v-model="formData.mealType"
              placeholder="请选择餐次"
              size="large"
            >
              <el-option
                v-for="mealOption in mealTypeOptions"
                :key="mealOption.value"
                :label="mealOption.label"
                :value="mealOption.value"
              >
                <template #default>
                  <div class="select-option">
                    <el-icon v-if="mealOption.value === 'breakfast'"><Sunrise /></el-icon>
                    <el-icon v-else-if="mealOption.value === 'lunch'"><Sunny /></el-icon>
                    <el-icon v-else-if="mealOption.value === 'dinner'"><Moon /></el-icon>
                    <el-icon v-else-if="mealOption.value === 'snack'"><Coffee /></el-icon>
                    <span>{{ mealOption.label }}</span>
                  </div>
                </template>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 时间选择 -->
      <el-row justify="center">
        <el-col :xs="24" :sm="20">
          <el-form-item label="时间" required prop="time">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><ClockIcon /></el-icon>
                <span>时&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;间</span>
              </div>
            </template>
            <el-time-picker
              v-model="formData.time"
              type="time"
              placeholder="选择时间"
              format="HH:mm"
              value-format="HH:mm"
              style="width: 100%"
              size="large"
            >
            </el-time-picker>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 食物名称 -->
      <el-row justify="center">
        <el-col :xs="24" :sm="20">
          <el-form-item label="食物名称" required prop="foodName">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><ForkSpoonIcon /></el-icon>
                <span>食物名称</span>
              </div>
            </template>
            <el-input
              v-model="formData.foodName"
              placeholder="请输入食物名称"
              size="large"
              clearable
            >
              <template #prefix-icon>
                <el-icon class="input-prefix-icon"><ForkSpoonIcon /></el-icon>
              </template>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 营养成分输入区 -->
      <el-row justify="center" :gutter="20">
        <el-col :xs="24" :sm="10">
          <!-- 卡路里输入 -->
          <el-form-item label="卡路里" required prop="calories">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><ScaleIcon /></el-icon>
                <span>卡&nbsp;路&nbsp;里</span>
              </div>
            </template>
            <el-input-number
              v-model="formData.calories"
              :min="0"
              :step="10"
              :precision="0"
              placeholder="请输入卡路里"
              size="large"
              style="width: 100%"
              controls-position="right"
            >
            </el-input-number>
          </el-form-item>
        </el-col>

        <el-col :xs="24" :sm="10">
          <!-- 蛋白质输入 -->
          <el-form-item label="蛋白质(g)" prop="protein">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><MilkTea /></el-icon>
                <span>蛋&nbsp;白&nbsp;质</span>
              </div>
            </template>
            <el-input-number
              v-model="formData.protein"
              :min="0"
              :max="1000"
              :step="0.1"
              :precision="1"
              placeholder="请输入蛋白质含量"
              size="large"
              style="width: 100%"
              controls-position="right"
            >
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row justify="center" :gutter="20" style="margin-top: 20px">
        <el-col :xs="24" :sm="10">
          <!-- 脂肪输入 -->
          <el-form-item label="脂肪(g)" prop="fat">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><Timer /></el-icon>
                <span>脂&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;肪</span>
              </div>
            </template>
            <el-input-number
              v-model="formData.fat"
              :min="0"
              :max="1000"
              :step="0.1"
              :precision="1"
              placeholder="请输入脂肪含量"
              size="large"
              style="width: 100%"
              controls-position="right"
            >
            </el-input-number>
          </el-form-item>
        </el-col>

        <el-col :xs="24" :sm="10">
          <!-- 碳水化合物输入 -->
          <el-form-item label="碳水化合物(g)" prop="carbohydrate">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><Food /></el-icon>
                <span>碳&nbsp;水&nbsp;化&nbsp;合&nbsp;物</span>
              </div>
            </template>
            <el-input-number
              v-model="formData.carbohydrate"
              :min="0"
              :max="1000"
              :step="0.1"
              :precision="1"
              placeholder="请输入碳水化合物含量"
              size="large"
              style="width: 100%"
              controls-position="right"
            >
            </el-input-number>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 描述 -->
      <el-row justify="center">
        <el-col :xs="24" :sm="20">
          <el-form-item label="描述" prop="description">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><DocumentIcon /></el-icon>
                <span>描&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;述</span>
              </div>
            </template>
            <el-input
              v-model="formData.description"
              type="textarea"
              placeholder="请输入描述（如：份量、做法等）"
              :rows="4"
              size="large"
              maxlength="200"
              show-word-limit
            >
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button size="large" @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          size="large"
          @click="handleSubmit"
          :loading="loading"
        >
          <el-icon><Check /></el-icon>
          {{ mode === 'add' ? '确认添加' : '保存修改' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import {
  List as ListIcon,
  Clock as ClockIcon,
  ForkSpoon as ForkSpoonIcon,
  DataAnalysis as ScaleIcon,
  Document as DocumentIcon,
  Sunrise,
  Sunny,
  Moon,
  Coffee,
  Check,
  Timer,
  Food,
  MilkTea
} from '@element-plus/icons-vue'
import { mealTypeOptions } from '../utils/mealTypeUtils.js'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  mode: {
    type: String,
    default: 'add', // 'add' or 'edit'
    validator: (value) => ['add', 'edit'].includes(value)
  },
  record: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)

// 表单数据
const formData = ref({
  mealType: 'breakfast',
  time: '',
  foodName: '',
  calories: 0,
  protein: 0,
  fat: 0,
  carbohydrate: 0,
  description: ''
})

// 表单验证规则
const formRules = {
  mealType: [
    { required: true, message: '请选择餐次', trigger: 'change' }
  ],
  time: [
    { required: true, message: '请选择时间', trigger: 'change' }
  ],
  foodName: [
    { required: true, message: '请输入食物名称', trigger: 'blur' },
    { min: 1, max: 50, message: '食物名称长度在1-50个字符之间', trigger: 'blur' }
  ],
  calories: [
    { required: true, message: '请输入卡路里', trigger: 'blur' },
    { type: 'number', min: 0, max: 10000, message: '卡路里范围为0-10000', trigger: 'blur' }
  ],
  protein: [
    { type: 'number', min: 0, max: 1000, message: '蛋白质范围为0-1000g', trigger: 'blur' }
  ],
  fat: [
    { type: 'number', min: 0, max: 1000, message: '脂肪范围为0-1000g', trigger: 'blur' }
  ],
  carbohydrate: [
    { type: 'number', min: 0, max: 1000, message: '碳水化合物范围为0-1000g', trigger: 'blur' }
  ]
}

// 监听记录变化，用于编辑模式
watch(() => props.record, (newRecord) => {
  if (props.mode === 'edit' && newRecord && Object.keys(newRecord).length > 0) {
    formData.value = {
      mealType: newRecord.mealType || 'breakfast',
      time: newRecord.time || '',
      foodName: newRecord.foodName || '',
      calories: newRecord.calories || 0,
      protein: newRecord.protein || 0,
      fat: newRecord.fat || 0,
      carbohydrate: newRecord.carbohydrate || 0,
      description: newRecord.description || ''
    }
  }
}, { immediate: true, deep: true })

// 监听对话框打开，重置表单（添加模式）
watch(dialogVisible, (newValue) => {
  if (newValue && props.mode === 'add') {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  const currentTime = new Date().toTimeString().slice(0, 5)
  formData.value = {
    mealType: 'breakfast',
    time: currentTime,
    foodName: '',
    calories: 0,
    protein: 0,
    fat: 0,
    carbohydrate: 0,
    description: ''
  }
  formRef.value?.clearValidate()
}

// 获取表单数据
const getFormData = () => ({ ...formData.value })

// 验证表单
const validateForm = async () => {
  if (!formRef.value) return false
  try {
    await formRef.value.validate()
    return true
  } catch (error) {
    return false
  }
}

// 关闭对话框
const handleClose = () => {
  emit('update:modelValue', false)
}

// 提交表单
const handleSubmit = async () => {
  const isValid = await validateForm()
  if (isValid) {
    emit('submit', getFormData())
  }
}

// 暴露方法给父组件
defineExpose({
  resetForm,
  validateForm,
  getFormData
})
</script>

<style scoped>
/* 选择器选项样式 */
.select-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 输入框前缀图标 */
.input-prefix-icon {
  color: #667eea;
  font-size: 18px;
}

/* 带图标的标签样式 */
.form-item-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-icon {
  font-size: 18px;
  color: #667eea;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

/* 弹窗样式 */
:deep(.el-dialog__header) {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  padding: 24px 28px;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
}

/* 表单字段样式 */
:deep(.el-form-item) {
  margin-bottom: 20px;
  align-items: center;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

/* 输入框样式 */
:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-textarea__inner),
:deep(.el-time-picker__input-inner),
:deep(.el-input-number__wrapper) {
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover),
:deep(.el-textarea__inner:hover),
:deep(.el-time-picker__input-inner:hover),
:deep(.el-input-number__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-select__wrapper.is-focus),
:deep(.el-textarea__inner.is-focus),
:deep(.el-time-picker__input-inner.is-focus),
:deep(.el-input-number__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

/* 数字输入框样式优化 */
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-input-number .el-input-number__decrease),
:deep(.el-input-number .el-input-number__increase) {
  background-color: #f5f7fa;
  border-radius: 6px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 按钮样式 */
:deep(.dialog-footer) {
  text-align: center;
  padding: 0 28px 24px;
}

:deep(.dialog-footer .el-button) {
  padding: 10px 28px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.dialog-footer .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.dialog-footer .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

:deep(.dialog-footer .el-button--default) {
  border-color: #e5e7eb;
}

:deep(.dialog-footer .el-button--default:hover) {
  border-color: #667eea;
  color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}
</style>
