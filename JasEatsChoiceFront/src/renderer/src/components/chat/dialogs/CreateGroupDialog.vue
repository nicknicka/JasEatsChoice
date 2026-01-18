<template>
  <el-dialog v-model="visible" title="新建群聊" width="400px" @close="handleClose">
    <el-form :model="form" label-width="80px">
      <el-form-item label="群名称">
        <el-input v-model="form.name" placeholder="请输入群名称" />
      </el-form-item>

      <el-form-item label="成员列表">
        <div class="member-list-container">
          <el-input
            v-model="form.members"
            type="textarea"
            placeholder="请输入成员名称，用逗号分隔"
            :rows="2"
            readonly
          />
          <el-button type="primary" size="default" @click="$emit('show-friend-selection')">
            +
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  members: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'create', 'show-friend-selection'])

const visible = ref(props.modelValue)
const form = ref({
  name: '',
  members: props.members
})

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

watch(() => props.members, (val) => {
  form.value.members = val
})

const handleCreate = () => {
  if (!form.value.name.trim()) {
    return
  }

  emit('create', {
    name: form.value.name.trim(),
    members: form.value.members
  })

  // 重置表单
  form.value = {
    name: '',
    members: ''
  }
}

const handleClose = () => {
  visible.value = false
  form.value = {
    name: '',
    members: ''
  }
}
</script>

<style scoped lang="less">
.member-list-container {
  display: flex;
  gap: 10px;
  align-items: stretch;

  .el-input {
    flex: 1;
  }

  .el-button {
    align-self: flex-end;
  }
}
</style>
