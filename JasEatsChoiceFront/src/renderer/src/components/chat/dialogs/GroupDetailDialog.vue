<template>
  <el-dialog v-model="visible" title="群聊详情" width="500px">
    <div v-if="groupInfo" class="group-detail-content">
      <div class="group-avatar">{{ groupInfo.avatar }}</div>
      <div class="group-name">{{ groupInfo.name }}</div>

      <div class="group-info-item">成员数量: {{ groupInfo.memberCount }}人</div>
      <div class="group-info-item">创建人: {{ groupInfo.creator }}</div>
      <div class="group-info-item">创建时间: {{ groupInfo.createdAt }}</div>

      <div class="group-members">
        <div class="section-title">群成员:</div>
        <div v-for="member in groupInfo.members" :key="member" class="member-item">
          {{ member }}
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
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
  groupInfo: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped lang="less">
.group-detail-content {
  padding: 20px;

  .group-avatar {
    font-size: 64px;
    margin-bottom: 16px;
    text-align: center;
  }

  .group-name {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 16px;
    text-align: center;
  }

  .group-info-item {
    margin-bottom: 12px;
    font-size: 14px;
    color: #606266;
  }

  .group-members {
    margin-top: 20px;

    .section-title {
      font-weight: 500;
      margin-bottom: 12px;
    }

    .member-item {
      margin-bottom: 8px;
      font-size: 14px;
      padding: 8px;
      background-color: #f5f7fa;
      border-radius: 4px;
    }
  }
}
</style>
