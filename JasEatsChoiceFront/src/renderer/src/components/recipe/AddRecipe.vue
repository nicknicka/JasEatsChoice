<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ForkSpoon as ForkSpoonIcon,
  List as ListIcon,
  Clock as ClockIcon,
  Document as DocumentIcon
} from '@element-plus/icons-vue'

// 接收props
const props = defineProps({
  visible: Boolean,
  recipe: {
    type: Object,
    default: null
  }
})

// 定义事件
const emit = defineEmits(['update:visible', 'add-recipe', 'update-recipe', 'start-edit'])

// 判断是否为编辑模式
const isEditMode = ref(false)

// 新食谱表单数据
const newRecipe = ref({
  name: '',
  type: '早餐',
  time: '',
  details: '' // 食谱描述字段
})

// 表单验证规则
const formRules = ref({
  name: [
    { required: true, message: '请填写食谱名称', trigger: 'blur' },
    { min: 1, max: 50, message: '食谱名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  time: [{ required: true, message: '请选择准备时间', trigger: 'change' }]
})

// 重置表单
const resetForm = () => {
  newRecipe.value = {
    name: '',
    type: '早餐',
    time: '',
    details: ''
  }
  isEditMode.value = false
}

// 关闭对话框
const closeDialog = () => {
  emit('update:visible', false)
  setTimeout(() => {
    resetForm()
  }, 300)
}

// 监听 props.recipe 变化，当有食谱数据时填充表单
watch(
  () => props.recipe,
  (newRecipeData) => {
    console.log('AddRecipe watch 触发，recipe 数据:', newRecipeData)
    if (newRecipeData && Object.keys(newRecipeData).length > 0) {
      console.log('进入编辑模式，填充表单')
      isEditMode.value = true
      newRecipe.value = {
        name: newRecipeData.name || '',
        type: newRecipeData.type || '早餐',
        time: newRecipeData.cookTime || newRecipeData.time || '',
        details: newRecipeData.details || ''
      }
    } else {
      console.log('进入新增模式，重置表单')
      isEditMode.value = false
      resetForm()
    }
  },
  { immediate: true }
)

// 保存食谱
const saveNewRecipe = () => {
  console.log('保存食谱，编辑模式:', isEditMode.value, '表单数据:', newRecipe.value)

  // 简单的表单验证
  if (!newRecipe.value.name.trim()) {
    ElMessage.warning('请填写食谱名称')
    return
  }

  // 准备时间验证
  if (!newRecipe.value.time) {
    ElMessage.warning('请选择有效的准备时间')
    return
  }

  if (isEditMode.value && props.recipe) {
    // 编辑模式：更新食谱
    const updatedRecipe = {
      ...props.recipe,
      name: newRecipe.value.name,
      type: newRecipe.value.type,
      cookTime: newRecipe.value.time,
      time: newRecipe.value.time,
      details: newRecipe.value.details
    }
    console.log('触发 update-recipe 事件:', updatedRecipe)
    emit('update-recipe', updatedRecipe)
  } else {
    // 新增模式：创建新食谱
    const recipe = {
      id: Date.now(), // 使用时间戳作为唯一ID
      name: newRecipe.value.name,
      type: newRecipe.value.type,
      time: newRecipe.value.time,
      details: newRecipe.value.details
    }
    console.log('触发 add-recipe 事件:', recipe)
    emit('add-recipe', recipe)
  }
  closeDialog()
  // 移除默认的成功提示，由后端响应后统一处理
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    @update:model-value="(val) => emit('update:visible', val)"
    :title="isEditMode ? '编辑食谱' : '添加新食谱'"
    width="500px"
    top="10%"
    transition="dialog-fade"
    custom-class="add-recipe-dialog"
  >
    <div class="add-recipe-form">
      <el-form
        :model="newRecipe"
        label-width="120px"
        status-icon
        :rules="formRules"
        @keyup.enter.native="saveNewRecipe"
      >
        <el-form-item label="名称" prop="name" required>
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><ForkSpoonIcon /></el-icon>
              <span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称</span>
            </div>
          </template>
          <el-input v-model="newRecipe.name" placeholder="例：牛奶燕麦粥" />
        </el-form-item>

        <el-form-item label="类型" prop="type" required>
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><ListIcon /></el-icon>
              <span>类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;型</span>
            </div>
          </template>
          <el-select v-model="newRecipe.type" style="width: 100%">
            <el-option label="早餐" value="早餐" />
            <el-option label="午餐" value="午餐" />
            <el-option label="晚餐" value="晚餐" />
            <el-option label="加餐" value="加餐" />
          </el-select>
        </el-form-item>

        <el-form-item label="准备时间" prop="time" required>
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><ClockIcon /></el-icon>
              <span>准备时间</span>
            </div>
          </template>
          <el-time-picker
            v-model="newRecipe.time"
            placeholder="例：00:15"
            type="time"
            format="HH:mm"
            value-format="HH:mm"
            :is-range="false"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="食谱详情" prop="details">
          <template #label>
            <div class="form-item-label">
              <el-icon class="label-icon"><DocumentIcon /></el-icon>
              <span>食谱详情</span>
            </div>
          </template>
          <el-input
            v-model="newRecipe.details"
            type="textarea"
            :rows="5"
            placeholder="请输入详细的食材和烹饪步骤"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="saveNewRecipe">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style>
/* 只针对添加食谱对话框的样式，不影响其他组件 */
.add-recipe-dialog .el-dialog__header {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(230, 247, 255, 0.8) 0%, rgba(186, 231, 255, 0.8) 100%);
  padding: 24px 28px;
}

.add-recipe-dialog .el-dialog__title {
  font-size: 1.429rem /* 原值: 20px */;
  font-weight: 600;
  color: #1890ff;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.add-recipe-dialog .el-dialog__body {
  padding: 32px 28px;
}

/* 表单容器 */
.add-recipe-form {
  padding: 30px 0;
  max-width: 440px;
  margin: 0 auto;
}

/* 表单字段样式 - 只影响对话框内的表单项 */
.add-recipe-dialog .el-form-item {
  margin-bottom: 32px;
}

.add-recipe-dialog .el-form-item__label {
  font-weight: 500;
  color: #555;
  font-size: 1rem /* 原值: 14px */;
}

.add-recipe-dialog .el-form-item__label::before {
  content: '';
  display: none;
}

/* 带图标的标签样式 */
.form-item-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label-icon {
  font-size: 1.286rem /* 原值: 18px */;
  color: #1890ff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  vertical-align: middle;
}

/* 输入框样式 - 只影响对话框内的输入框 */
.add-recipe-dialog .el-input__wrapper,
.add-recipe-dialog .el-select__wrapper,
.add-recipe-dialog .el-textarea__inner {
  border-radius: 8px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.add-recipe-dialog .el-input__wrapper:hover,
.add-recipe-dialog .el-select__wrapper:hover,
.add-recipe-dialog .el-textarea__inner:hover {
  border-color: #91d5ff;
  box-shadow: 0 0 0 3px rgba(145, 213, 255, 0.1);
}

.add-recipe-dialog .el-input__wrapper.is-focus,
.add-recipe-dialog .el-select__wrapper.is-focus,
.add-recipe-dialog .el-textarea__inner.is-focus {
  border-color: #40a9ff;
  box-shadow: 0 0 0 3px rgba(64, 169, 255, 0.15);
}

/* 时间选择器样式 - 只影响对话框内 */
.add-recipe-dialog .el-time-picker__input {
  font-size: 1rem /* 原值: 14px */;
}

/* 按钮样式 - 只影响对话框内的按钮 */
.add-recipe-dialog .dialog-footer {
  text-align: center;
  padding: 0 28px 24px;
}

.add-recipe-dialog .dialog-footer .el-button {
  padding: 10px 28px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 1rem /* 原值: 14px */;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.add-recipe-dialog .dialog-footer .el-button--primary {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
}

.add-recipe-dialog .dialog-footer .el-button--primary:hover {
  background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(64, 169, 255, 0.3);
}

.add-recipe-dialog .dialog-footer .el-button--default {
  border-color: #e5e7eb;
  background-color: #fafafa;
  color: #666;
}

.add-recipe-dialog .dialog-footer .el-button--default:hover {
  border-color: #d9d9d9;
  background-color: #f0f0f0;
  color: #333;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 弹窗动画 */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.dialog-fade-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.dialog-fade-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
