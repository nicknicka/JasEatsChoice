<template>
  <div class="merchant-remark-display">
    <div class="remark-header">
      <h4>客户备注</h4>
      <el-tag v-if="priorityCount.high > 0" type="danger" size="small">
        {{ priorityCount.high }} 个重要
      </el-tag>
    </div>

    <div v-if="remarks.length === 0" class="no-remark">
      <el-empty description="暂无备注信息" :image-size="60" />
    </div>

    <div v-else class="remark-list">
      <!-- 高优先级：过敏食材（红色） -->
      <div
        v-for="remark in highPriorityRemarks"
        :key="remark.id"
        class="remark-item priority-high"
      >
        <div class="remark-header">
          <el-icon class="priority-icon"><Warning /></el-icon>
          <span class="priority-label">过敏风险</span>
          <span class="dish-name">{{ remark.dishName }}</span>
        </div>
        <div class="remark-content">{{ remark.content }}</div>
        <div class="remark-meta">
          <span class="remark-time">{{ formatTime(remark.createTime) }}</span>
        </div>
      </div>

      <!-- 中高优先级：核心需求（黄色） -->
      <div
        v-for="remark in mediumHighRemarks"
        :key="remark.id"
        class="remark-item priority-medium-high"
      >
        <div class="remark-header">
          <el-icon class="priority-icon"><Star /></el-icon>
          <span class="priority-label">重要需求</span>
          <span class="dish-name">{{ remark.dishName }}</span>
        </div>
        <div class="remark-content">{{ remark.content }}</div>
        <div class="remark-meta">
          <span class="remark-time">{{ formatTime(remark.createTime) }}</span>
        </div>
      </div>

      <!-- 中优先级：口味调整（蓝色） -->
      <div
        v-for="remark in mediumRemarks"
        :key="remark.id"
        class="remark-item priority-medium"
      >
        <div class="remark-header">
          <el-icon class="priority-icon"><Edit /></el-icon>
          <span class="priority-label">口味调整</span>
          <span class="dish-name">{{ remark.dishName }}</span>
        </div>
        <div class="remark-content">{{ remark.content }}</div>
        <div class="remark-meta">
          <span class="remark-time">{{ formatTime(remark.createTime) }}</span>
        </div>
      </div>

      <!-- 低优先级：一般备注（灰色） -->
      <div
        v-for="remark in lowPriorityRemarks"
        :key="remark.id"
        class="remark-item priority-low"
      >
        <div class="remark-header">
          <el-icon class="priority-icon"><InfoFilled /></el-icon>
          <span class="priority-label">一般备注</span>
          <span class="dish-name">{{ remark.dishName }}</span>
        </div>
        <div class="remark-content">{{ remark.content }}</div>
        <div class="remark-meta">
          <span class="remark-time">{{ formatTime(remark.createTime) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Warning, Star, Edit, InfoFilled } from '@element-plus/icons-vue'
import api from '../../utils/api.js'

// Props
const props = defineProps({
  orderId: {
    type: String,
    required: true
  }
})

// 数据
const remarks = ref([])

// 优先级统计
const priorityCount = computed(() => {
  const count = {
    high: 0,
    mediumHigh: 0,
    medium: 0,
    low: 0
  }

  remarks.value.forEach(remark => {
    if (remark.priority === 1) count.high++
    else if (remark.priority === 2) count.mediumHigh++
    else if (remark.priority === 3) count.medium++
    else count.low++
  })

  return count
})

// 按优先级分组
const highPriorityRemarks = computed(() => {
  return remarks.value.filter(r => r.priority === 1)
})

const mediumHighRemarks = computed(() => {
  return remarks.value.filter(r => r.priority === 2)
})

const mediumRemarks = computed(() => {
  return remarks.value.filter(r => r.priority === 3)
})

const lowPriorityRemarks = computed(() => {
  return remarks.value.filter(r => r.priority === 4)
})

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`

  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`

  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载订单备注
const loadRemarks = async () => {
  try {
    // 这里应该调用获取订单备注的API
    // 暂时使用模拟数据
    const response = await api.get(`/v1/orders/${props.orderId}/remarks`)

    if (response.data.success) {
      remarks.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载备注失败:', error)
    // 使用模拟数据
    remarks.value = []
  }
}

// 刷新
const refresh = () => {
  loadRemarks()
}

// 生命周期
onMounted(() => {
  loadRemarks()
})

// 暴露方法
defineExpose({
  refresh
})
</script>

<style scoped>
.merchant-remark-display {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.remark-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #EBEEF5;
}

.remark-header h4 {
  margin: 0;
  font-size: 1.143rem /* 原值: 16px */;
  font-weight: 600;
  color: #303133;
}

.no-remark {
  padding: 20px 0;
}

.remark-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.remark-item {
  padding: 12px 16px;
  border-radius: 8px;
  border-left: 4px solid;
  transition: all 0.3s;
}

.remark-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 高优先级：过敏 - 红色 */
.remark-item.priority-high {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

/* 中高优先级：核心需求 - 黄色 */
.remark-item.priority-medium-high {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

/* 中优先级：口味调整 - 蓝色 */
.remark-item.priority-medium {
  background: #ecf5ff;
  border-left-color: #409eff;
}

/* 低优先级：一般备注 - 灰色 */
.remark-item.priority-low {
  background: #f4f4f5;
  border-left-color: #909399;
}

.remark-item > .remark-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding-bottom: 0;
  border-bottom: none;
}

.priority-icon {
  font-size: 1.286rem /* 原值: 18px */;
}

.priority-high .priority-icon {
  color: #f56c6c;
}

.priority-medium-high .priority-icon {
  color: #e6a23c;
}

.priority-medium .priority-icon {
  color: #409eff;
}

.priority-low .priority-icon {
  color: #909399;
}

.priority-label {
  font-weight: 600;
  font-size: 0.929rem /* 原值: 13px */;
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.8);
}

.priority-high .priority-label {
  color: #f56c6c;
}

.priority-medium-high .priority-label {
  color: #e6a23c;
}

.priority-medium .priority-label {
  color: #409eff;
}

.priority-low .priority-label {
  color: #909399;
}

.dish-name {
  font-size: 1rem /* 原值: 14px */;
  color: #606266;
  font-weight: 500;
}

.remark-content {
  font-size: 1rem /* 原值: 14px */;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 8px;
}

.remark-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 0.857rem /* 原值: 12px */;
  color: #909399;
}

.remark-time {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
