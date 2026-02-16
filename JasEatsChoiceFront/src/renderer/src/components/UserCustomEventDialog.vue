<template>
  <el-dialog
    v-model="dialogVisible"
    title="创建自定义事件"
    width="600px"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="事件名称" prop="eventName">
        <el-input v-model="form.eventName" placeholder="请输入事件名称" />
      </el-form-item>

      <el-form-item label="事件类型" prop="eventType">
        <el-select v-model="form.eventType" placeholder="请选择事件类型">
          <el-option label="生日" value="BIRTHDAY">
            <span style="float: left">🎂 生日</span>
          </el-option>
          <el-option label="纪念日" value="ANNIVERSARY">
            <span style="float: left">💍 纪念日</span>
          </el-option>
          <el-option label="聚会" value="PARTY">
            <span style="float: left">🎉 聚会</span>
          </el-option>
          <el-option label="其他" value="OTHER">
            <span style="float: left">📅 其他</span>
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="事件日期" prop="eventDate">
        <el-date-picker
          v-model="eventDateValue"
          type="date"
          placeholder="选择日期"
          format="MM-DD"
          value-format="MM-DD"
          @change="handleDateChange"
        />
      </el-form-item>

      <el-form-item label="每年重复">
        <el-switch v-model="isRecurring" />
        <span style="margin-left: 8px; color: #909399; font-size: 0.929rem /* 原值: 13px */">
          开启后每年都会提醒
        </span>
      </el-form-item>

      <el-form-item label="提前提醒天数">
        <el-input-number v-model="form.reminderDays" :min="1" :max="30" />
        <span style="margin-left: 8px; color: #909399; font-size: 0.929rem /* 原值: 13px */">天</span>
      </el-form-item>

      <el-form-item label="预计用餐人数">
        <el-input-number v-model="form.guestCount" :min="1" :max="100" />
        <span style="margin-left: 8px; color: #909399; font-size: 0.929rem /* 原值: 13px */">人</span>
      </el-form-item>

      <el-form-item label="人均预算">
        <el-input-number v-model="form.budgetPerPerson" :min="0" :precision="2" />
        <span style="margin-left: 8px; color: #909399; font-size: 0.929rem /* 原值: 13px */">元</span>
      </el-form-item>

      <el-form-item label="事件描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入事件描述"
        />
      </el-form-item>

      <el-form-item label="偏好菜品">
        <el-select
          v-model="form.preferredDishIds"
          multiple
          filterable
          placeholder="选择偏好菜品"
          style="width: 100%"
        >
          <el-option
            v-for="dish in availableDishes"
            :key="dish.id"
            :label="dish.dishName"
            :value="dish.id"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        创建
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import festivalApi from '@/api/festival'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  availableDishes: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'success'])

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const formRef = ref(null)
const submitting = ref(false)
const isRecurring = ref(true)
const eventDateValue = ref('')

const form = reactive({
  eventName: '',
  eventType: '',
  eventDate: '',
  year: null,
  reminderDays: 3,
  guestCount: null,
  budgetPerPerson: null,
  description: '',
  preferredDishIds: []
})

const rules = {
  eventName: [{ required: true, message: '请输入事件名称', trigger: 'blur' }],
  eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
  eventDate: [{ required: true, message: '请选择事件日期', trigger: 'change' }]
}

const handleDateChange = (value) => {
  form.eventDate = value
  if (!isRecurring.value) {
    // 如果不重复，记录年份
    const currentDate = new Date()
    form.year = currentDate.getFullYear()
  } else {
    form.year = null
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitting.value = true
    const response = await festivalApi.createCustomEvent(form)

    if (response.code === 200) {
      ElMessage.success('创建成功')
      emit('success')
      handleClose()
    }
  } catch (error) {
    console.error('创建失败:', error)
    if (error !== false) { // 排除表单验证失败
      ElMessage.error('创建失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    eventName: '',
    eventType: '',
    eventDate: '',
    year: null,
    reminderDays: 3,
    guestCount: null,
    budgetPerPerson: null,
    description: '',
    preferredDishIds: []
  })
  eventDateValue.value = ''
  isRecurring.value = true
  emit('update:visible', false)
}
</script>

<style scoped>
:deep(.el-select) {
  width: 100%;
}
</style>
