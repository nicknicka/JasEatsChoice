<template>
  <div class="dish-step-timeline">
    <div class="timeline-header">
      <h3>菜品制作进度</h3>
      <el-tag v-if="completedCount === totalCount && totalCount > 0" type="success">
        全部完成
      </el-tag>
      <el-tag v-else type="info">
        {{ completedCount }}/{{ totalCount }} 已完成
      </el-tag>
    </div>

    <div class="timeline-content">
      <div
        v-for="dish in dishes"
        :key="dish.orderDishId"
        class="dish-timeline-item"
      >
        <!-- 菜品头部 -->
        <div class="dish-header">
          <el-image
            :src="dish.dishImage"
            class="dish-thumb"
            fit="cover"
          />
          <div class="dish-basic-info">
            <div class="dish-name">{{ dish.dishName }} x{{ dish.quantity }}</div>
            <div class="dish-status-text" :style="{ color: getStepColor(dish.stepStatus) }">
              {{ dish.stepStatusName }}
            </div>
          </div>
          <div class="dish-progress-circle">
            <el-progress
              type="circle"
              :percentage="dish.progressPercent"
              :color="getStepColor(dish.stepStatus)"
              :width="60"
            />
          </div>
        </div>

        <!-- 步骤时间线 -->
        <div class="steps-timeline">
          <el-timeline>
            <el-timeline-item
              v-for="(step, index) in getDishSteps(dish.isFastFood)"
              :key="index"
              :timestamp="getStepTime(dish, step.code)"
              :type="getTimelineType(dish.stepStatus, step.code)"
              :icon="getTimelineIcon(dish.stepStatus, step.code)"
              :size="getTimelineSize(dish.stepStatus, step.code)"
            >
              <div class="timeline-step-content">
                <div class="step-name">{{ step.name }}</div>
                <div v-if="isCurrentStep(dish.stepStatus, step.code)" class="step-time-remaining">
                  <span v-if="dish.remainingMinutes !== null">
                    预计剩余 {{ dish.remainingMinutes }} 分钟
                  </span>
                  <span v-else-if="dish.elapsedMinutes">
                    已用时 {{ dish.elapsedMinutes }} 分钟
                  </span>
                </div>
                <div v-if="isCompletedStep(dish.stepStatus, step.code)" class="step-completed-badge">
                  <el-icon><CircleCheck /></el-icon>
                  已完成
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <!-- 历史记录折叠面板 -->
        <el-collapse v-if="dish.stepHistory && dish.stepHistory.length > 0" class="history-collapse">
          <el-collapse-item name="history">
            <template #title>
              <span class="history-title">
                <el-icon><Clock /></el-icon>
                制作记录 ({{ dish.stepHistory.length }})
              </span>
            </template>
            <div class="history-list">
              <div
                v-for="record in dish.stepHistory"
                :key="record.id"
                class="history-record"
              >
                <div class="record-time">{{ formatDateTime(record.createTime) }}</div>
                <div class="record-content">
                  <el-tag size="small" type="info">{{ record.oldStepStatusName || '初始' }}</el-tag>
                  <el-icon><ArrowRight /></el-icon>
                  <el-tag size="small" type="success">{{ record.newStepStatusName }}</el-tag>
                  <span class="record-operator" v-if="record.operatorName">
                    by {{ record.operatorName }}
                  </span>
                </div>
                <div v-if="record.rollbackReason" class="record-reason">
                  回退原因：{{ record.rollbackReason }}
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="dishes.length === 0"
      description="暂无菜品进度信息"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  CircleCheck,
  Clock,
  ArrowRight,
  Loading,
  Check
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'

// Props
const props = defineProps({
  orderId: {
    type: String,
    required: true
  },
  autoRefresh: {
    type: Boolean,
    default: false
  },
  refreshInterval: {
    type: Number,
    default: 30000 // 30秒
  }
})

// 数据
const loading = ref(false)
const dishes = ref([])
let refreshTimer = null

// 步骤定义
const normalSteps = [
  { code: 0, name: '待备菜' },
  { code: 1, name: '备菜中' },
  { code: 2, name: '预处理中' },
  { code: 3, name: '烹饪中' },
  { code: 4, name: '摆盘中' },
  { code: 5, name: '待上菜' },
  { code: 6, name: '已上菜' }
]

const fastFoodSteps = [
  { code: 10, name: '制作中' },
  { code: 11, name: '打包中' },
  { code: 12, name: '待出餐' },
  { code: 13, name: '已出餐' }
]

// 统计数据
const totalCount = computed(() => dishes.value.length)
const completedCount = computed(() => {
  return dishes.value.filter(dish =>
    dish.stepStatus === 6 || dish.stepStatus === 13
  ).length
})

// 获取菜品步骤列表
const getDishSteps = (isFastFood) => {
  return isFastFood ? fastFoodSteps : normalSteps
}

// 获取步骤颜色
const getStepColor = (stepStatus) => {
  const colorMap = {
    0: '#909399',
    1: '#409EFF',
    2: '#67C23A',
    3: '#E6A23C',
    4: '#F56C6C',
    5: '#909399',
    6: '#67C23A',
    10: '#409EFF',
    11: '#E6A23C',
    12: '#909399',
    13: '#67C23A'
  }
  return colorMap[stepStatus] || '#909399'
}

// 判断是否为当前步骤
const isCurrentStep = (currentStatus, stepCode) => {
  return currentStatus === stepCode
}

// 判断是否为已完成步骤
const isCompletedStep = (currentStatus, stepCode) => {
  if (currentStatus >= 10) {
    // 快餐流程
    return stepCode < currentStatus || stepCode === 13
  } else {
    // 正餐流程
    return stepCode < currentStatus || stepCode === 6
  }
}

// 获取步骤时间
const getStepTime = (dish, stepCode) => {
  if (isCompletedStep(dish.stepStatus, stepCode)) {
    // 从历史记录中查找完成时间
    const history = dish.stepHistory || []
    const record = history.find(h => h.newStepStatus === stepCode)
    return record ? formatDateTime(record.createTime) : ''
  }
  return ''
}

// 获取时间线类型
const getTimelineType = (currentStatus, stepCode) => {
  if (isCompletedStep(currentStatus, stepCode)) {
    return 'success'
  }
  if (isCurrentStep(currentStatus, stepCode)) {
    return 'primary'
  }
  return 'info'
}

// 获取时间线图标
const getTimelineIcon = (currentStatus, stepCode) => {
  if (isCompletedStep(currentStatus, stepCode)) {
    return Check
  }
  if (isCurrentStep(currentStatus, stepCode)) {
    return Loading
  }
  return null
}

// 获取时间线尺寸
const getTimelineSize = (currentStatus, stepCode) => {
  if (isCurrentStep(currentStatus, stepCode)) {
    return 'large'
  }
  return 'normal'
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
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

// 加载订单菜品步骤
const loadOrderDishSteps = async () => {
  loading.value = true
  try {
    const response = await api.get(`/v1/dish-steps/order/${props.orderId}`)
    if (response.data.success) {
      dishes.value = response.data.data || []
    } else {
      ElMessage.error(response.data.message || '加载菜品进度失败')
    }
  } catch (error) {
    console.error('加载菜品进度失败:', error)
    ElMessage.error('加载菜品进度失败')
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refresh = () => {
  loadOrderDishSteps()
}

// 启动自动刷新
const startAutoRefresh = () => {
  if (props.autoRefresh && props.refreshInterval > 0) {
    refreshTimer = setInterval(() => {
      loadOrderDishSteps()
    }, props.refreshInterval)
  }
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

// 生命周期
onMounted(() => {
  loadOrderDishSteps()
  startAutoRefresh()
})

// 监听订单ID变化
watch(() => props.orderId, () => {
  if (props.orderId) {
    loadOrderDishSteps()
  }
})

// 监听自动刷新配置变化
watch([() => props.autoRefresh, () => props.refreshInterval], () => {
  stopAutoRefresh()
  startAutoRefresh()
})

// 组件卸载时停止定时器
import { onUnmounted } from 'vue'
onUnmounted(() => {
  stopAutoRefresh()
})

// 暴露方法给父组件
defineExpose({
  refresh,
  loadOrderDishSteps
})
</script>

<style scoped>
.dish-step-timeline {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.timeline-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dish-timeline-item {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.dish-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.dish-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
}

.dish-basic-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dish-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.dish-status-text {
  font-size: 14px;
  font-weight: 500;
}

.dish-progress-circle {
  flex-shrink: 0;
}

.steps-timeline {
  padding: 0 20px;
}

.timeline-step-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.step-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.step-time-remaining {
  font-size: 12px;
  color: #909399;
}

.step-completed-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #67C23A;
  font-weight: 500;
}

.history-collapse {
  margin-top: 16px;
  border-top: 1px solid #EBEEF5;
  padding-top: 16px;
}

.history-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-record {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.record-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.record-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.record-operator {
  font-size: 12px;
  color: #606266;
}

.record-reason {
  margin-top: 8px;
  font-size: 12px;
  color: #F56C6C;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dish-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .dish-progress-circle {
    align-self: flex-end;
  }

  .steps-timeline {
    padding: 0 10px;
  }
}
</style>
