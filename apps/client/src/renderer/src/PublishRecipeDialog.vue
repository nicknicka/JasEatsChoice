<template>
  <el-dialog
    :model-value="visible"
    title="发布为食谱"
    width="420px"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="publish-form">
      <label>选择餐次</label>
      <el-select v-model="mealType" placeholder="请选择餐次" style="width: 100%">
        <el-option
          v-for="option in mealTypeOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <p class="publish-hint">不选择时默认归入午餐</p>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="submit">确认发布</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { mealTypeOptions } from '@/utils/mealTypeUtils'
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

const mealType = ref('lunch')
const publishing = ref(false)

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      mealType.value = 'lunch'
    }
  }
)

const close = () => {
  emit('update:visible', false)
}

const submit = async () => {
  if (!props.extractionId) {
    ElMessage.warning('无法发布，请等待提取完成')
    return
  }

  publishing.value = true
  try {
    const response = await contentExtractionApi.publishAsRecipe(props.extractionId, {
      mealType: mealType.value
    })
    if (response.code === '200' || response.code === 200) {
      ElMessage.success('发布成功')
      emit('published', response.data)
      close()
    } else {
      ElMessage.error(response.message || '发布失败')
    }
  } catch (error) {
    const backendMessage = error?.data?.message || error?.response?.data?.message
    ElMessage.error(backendMessage || error?.message || '发布失败，请稍后重试')
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.publish-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.publish-form label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.publish-hint {
  margin: 0;
  color: #909399;
  font-size: 12px;
}
</style>
