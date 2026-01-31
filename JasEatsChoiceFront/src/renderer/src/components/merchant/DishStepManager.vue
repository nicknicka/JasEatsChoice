<template>
  <div class="dish-step-manager">
    <!-- 步骤筛选和批量操作栏 -->
    <div class="step-toolbar">
      <el-form :inline="true" class="step-filter-form">
        <el-form-item label="步骤筛选">
          <el-select
            v-model="filterStepStatus"
            placeholder="选择步骤状态"
            clearable
            @change="handleFilterChange"
          >
            <el-option label="全部" :value="null" />
            <el-option
              v-for="step in stepOptions"
              :key="step.code"
              :label="step.name"
              :value="step.code"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :disabled="selectedDishes.length === 0"
            @click="showBatchMarkDialog"
          >
            批量标记 ({{ selectedDishes.length }})
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button @click="refreshSteps">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 菜品步骤列表 -->
    <div class="dish-list" v-loading="loading">
      <div
        v-for="dish in filteredDishes"
        :key="dish.orderDishId"
        class="dish-step-card"
        :class="{ 'selected': selectedDishes.includes(dish.orderDishId) }"
      >
        <!-- 选择框 -->
        <el-checkbox
          v-model="selectedDishes"
          :label="dish.orderDishId"
          class="dish-checkbox"
        />

        <!-- 菜品基本信息 -->
        <div class="dish-info">
          <el-image
            :src="dish.dishImage"
            class="dish-image"
            fit="cover"
          />
          <div class="dish-details">
            <div class="dish-name">{{ dish.dishName }} x{{ dish.quantity }}</div>
            <div class="step-status-badge" :style="{ backgroundColor: getStepColor(dish.stepStatus) }">
              {{ dish.stepStatusName }}
            </div>
          </div>
        </div>

        <!-- 步骤进度条 -->
        <div class="step-progress">
          <el-progress
            :percentage="dish.progressPercent"
            :color="getStepColor(dish.stepStatus)"
            :stroke-width="8"
          />
          <div class="step-time-info">
            <span v-if="dish.elapsedMinutes">
              已用时: {{ dish.elapsedMinutes }}分钟
            </span>
            <span v-if="dish.remainingMinutes !== null" class="remaining-time">
              剩余: {{ dish.remainingMinutes }}分钟
            </span>
          </div>
        </div>

        <!-- 步骤操作按钮 -->
        <div class="step-actions">
          <el-dropdown @command="(cmd) => handleStepCommand(cmd, dish)">
            <el-button type="primary" size="small">
              更新步骤
              <el-icon><MoreFilled /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="step in getNextSteps(dish.stepStatus)"
                  :key="step.code"
                  :command="step.code"
                >
                  <el-icon><CircleCheck /></el-icon>
                  {{ step.name }}
                </el-dropdown-item>
                <el-dropdown-item divided @click.native="showRollbackDialog(dish)">
                  <el-icon><CircleClose /></el-icon>
                  回退步骤
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button
            type="info"
            size="small"
            @click="showStepHistory(dish)"
          >
            <el-icon><View /></el-icon>
            历史记录
          </el-button>
        </div>

        <!-- 拖拽手柄 -->
        <div class="drag-handle" @mousedown="startDrag($event, dish)">
          <el-icon><Rank /></el-icon>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="filteredDishes.length === 0"
        description="暂无菜品数据"
      />
    </div>

    <!-- 批量标记对话框 -->
    <el-dialog
      v-model="batchMarkDialogVisible"
      title="批量标记菜品步骤"
      width="500px"
    >
      <el-form :model="batchMarkForm" label-width="100px">
        <el-form-item label="目标步骤">
          <el-select v-model="batchMarkForm.targetStep" placeholder="选择目标步骤">
            <el-option
              v-for="step in stepOptions"
              :key="step.code"
              :label="step.name"
              :value="step.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="batchMarkForm.remark"
            type="textarea"
            placeholder="可选填备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchMarkDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchMark">确定</el-button>
      </template>
    </el-dialog>

    <!-- 回退步骤对话框 -->
    <el-dialog
      v-model="rollbackDialogVisible"
      title="回退菜品步骤"
      width="500px"
    >
      <el-form :model="rollbackForm" label-width="100px">
        <el-form-item label="回退到">
          <el-select v-model="rollbackForm.targetStep" placeholder="选择回退目标">
            <el-option
              v-for="step in getRollbackSteps(currentDish?.stepStatus)"
              :key="step.code"
              :label="step.name"
              :value="step.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="回退原因" required>
          <el-input
            v-model="rollbackForm.reason"
            type="textarea"
            placeholder="请填写回退原因（必填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rollbackDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRollback">确定</el-button>
      </template>
    </el-dialog>

    <!-- 步骤历史对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="步骤历史记录"
      width="700px"
    >
      <el-timeline>
        <el-timeline-item
          v-for="item in currentDishHistory"
          :key="item.id"
          :timestamp="item.createTime"
          placement="top"
        >
          <el-card>
            <div class="history-item">
              <div class="history-step">
                <el-tag :type="item.oldStepStatus ? 'info' : 'success'" size="small">
                  {{ item.oldStepStatusName || '初始' }}
                </el-tag>
                <el-icon><ArrowRight /></el-icon>
                <el-tag type="success" size="small">
                  {{ item.newStepStatusName }}
                </el-tag>
              </div>
              <div class="history-info">
                <span>操作类型: {{ getOperationTypeText(item.operationType) }}</span>
                <span v-if="item.operatorName">操作人: {{ item.operatorName }}</span>
              </div>
              <div v-if="item.rollbackReason" class="history-reason">
                回退原因: {{ item.rollbackReason }}
              </div>
              <div v-if="item.remark" class="history-remark">
                备注: {{ item.remark }}
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="historyDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  MoreFilled,
  CircleCheck,
  CircleClose,
  View,
  ArrowRight,
  Rank
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'

// Props
const props = defineProps({
  orderId: {
    type: String,
    required: true
  }
})

// 数据
const loading = ref(false)
const dishes = ref([])
const selectedDishes = ref([])
const filterStepStatus = ref(null)
const currentDish = ref(null)
const currentDishHistory = ref([])

// 对话框状态
const batchMarkDialogVisible = ref(false)
const rollbackDialogVisible = ref(false)
const historyDialogVisible = ref(false)

// 表单数据
const batchMarkForm = ref({
  targetStep: null,
  remark: ''
})

const rollbackForm = ref({
  targetStep: null,
  reason: ''
})

// 步骤选项（正餐流程）
const stepOptions = ref([
  { code: 0, name: '待备菜' },
  { code: 1, name: '备菜中' },
  { code: 2, name: '预处理中' },
  { code: 3, name: '烹饪中' },
  { code: 4, name: '摆盘中' },
  { code: 5, name: '待上菜' },
  { code: 6, name: '已上菜' },
  { code: 10, name: '快餐-制作中' },
  { code: 11, name: '快餐-打包中' },
  { code: 12, name: '快餐-待出餐' },
  { code: 13, name: '快餐-已出餐' }
])

// 过滤后的菜品列表
const filteredDishes = computed(() => {
  if (filterStepStatus.value === null) {
    return dishes.value
  }
  return dishes.value.filter(dish => dish.stepStatus === filterStepStatus.value)
})

// 获取步骤颜色
const getStepColor = (stepStatus) => {
  const colorMap = {
    0: '#909399', // 待备菜 - 灰色
    1: '#409EFF', // 备菜中 - 蓝色
    2: '#67C23A', // 预处理中 - 绿色
    3: '#E6A23C', // 烹饪中 - 橙色
    4: '#F56C6C', // 摆盘中 - 红色
    5: '#909399', // 待上菜 - 灰色
    6: '#67C23A', // 已上菜 - 绿色
    10: '#409EFF', // 快餐制作中 - 蓝色
    11: '#E6A23C', // 快餐打包中 - 橙色
    12: '#909399', // 快餐待出餐 - 灰色
    13: '#67C23A'  // 快餐已出餐 - 绿色
  }
  return colorMap[stepStatus] || '#909399'
}

// 获取下一步骤选项
const getNextSteps = (currentStep) => {
  // 简化逻辑：返回当前步骤之后的所有步骤
  const stepMap = {
    0: [1], // 待备菜 -> 备菜中
    1: [2], // 备菜中 -> 预处理中
    2: [3], // 预处理中 -> 烹饪中
    3: [4], // 烹饪中 -> 摆盘中
    4: [5], // 摆盘中 -> 待上菜
    5: [6], // 待上菜 -> 已上菜
    10: [11], // 快餐制作中 -> 打包中
    11: [12], // 快餐打包中 -> 待出餐
    12: [13]  // 快餐待出餐 -> 已出餐
  }
  const nextCodes = stepMap[currentStep] || []
  return stepOptions.value.filter(step => nextCodes.includes(step.code))
}

// 获取可回退的步骤选项
const getRollbackSteps = (currentStep) => {
  const rollbackMap = {
    2: [1], // 预处理 -> 备菜中
    3: [2, 1], // 烹饪中 -> 预处理/备菜中
    4: [3, 2, 1], // 摆盘中 -> 烹饪/预处理/备菜
    5: [4, 3, 2, 1], // 待上菜 -> 摆盘/烹饪/预处理/备菜
    11: [10], // 快餐打包 -> 制作
    12: [11, 10] // 快餐待出餐 -> 打包/制作
  }
  const rollbackCodes = rollbackMap[currentStep] || []
  return stepOptions.value.filter(step => rollbackCodes.includes(step.code))
}

// 获取操作类型文本
const getOperationTypeText = (type) => {
  const typeMap = {
    'FORWARD': '前进',
    'BACKWARD': '回退',
    'SKIP': '跳过'
  }
  return typeMap[type] || type
}

// 加载订单菜品步骤
const loadOrderDishSteps = async () => {
  loading.value = true
  try {
    const response = await api.get(`/v1/dish-steps/order/${props.orderId}`)
    if (response.data.success) {
      dishes.value = response.data.data || []
    } else {
      ElMessage.error(response.data.message || '加载菜品步骤失败')
    }
  } catch (error) {
    console.error('加载菜品步骤失败:', error)
    ElMessage.error('加载菜品步骤失败')
  } finally {
    loading.value = false
  }
}

// 刷新步骤列表
const refreshSteps = () => {
  loadOrderDishSteps()
}

// 处理筛选变化
const handleFilterChange = () => {
  // 筛选逻辑由 computed 自动处理
}

// 显示批量标记对话框
const showBatchMarkDialog = () => {
  batchMarkForm.value = {
    targetStep: null,
    remark: ''
  }
  batchMarkDialogVisible.value = true
}

// 批量标记步骤
const handleBatchMark = async () => {
  if (!batchMarkForm.value.targetStep) {
    ElMessage.warning('请选择目标步骤')
    return
  }

  try {
    const response = await api.post('/v1/dish-steps/batch-mark', null, {
      params: {
        orderDishIds: selectedDishes.value,
        targetStepStatus: batchMarkForm.value.targetStep
      }
    })

    if (response.data.success) {
      ElMessage.success(response.data.data)
      batchMarkDialogVisible.value = false
      selectedDishes.value = []
      await loadOrderDishSteps()
    } else {
      ElMessage.error(response.data.message || '批量标记失败')
    }
  } catch (error) {
    console.error('批量标记失败:', error)
    ElMessage.error('批量标记失败')
  }
}

// 处理步骤命令
const handleStepCommand = async (command, dish) => {
  try {
    const response = await api.post('/v1/dish-steps/update', {
      orderDishId: dish.orderDishId,
      newStepStatus: command,
      operationType: 'FORWARD'
    })

    if (response.data.success) {
      ElMessage.success('步骤更新成功')
      await loadOrderDishSteps()
    } else {
      ElMessage.error(response.data.message || '步骤更新失败')
    }
  } catch (error) {
    console.error('更新步骤失败:', error)
    ElMessage.error('步骤更新失败')
  }
}

// 显示回退对话框
const showRollbackDialog = (dish) => {
  currentDish.value = dish
  rollbackForm.value = {
    targetStep: null,
    reason: ''
  }
  rollbackDialogVisible.value = true
}

// 处理回退
const handleRollback = async () => {
  if (!rollbackForm.value.targetStep) {
    ElMessage.warning('请选择回退目标步骤')
    return
  }

  if (!rollbackForm.value.reason.trim()) {
    ElMessage.warning('请填写回退原因')
    return
  }

  try {
    const response = await api.post('/v1/dish-steps/rollback', {
      orderDishId: currentDish.value.orderDishId,
      newStepStatus: rollbackForm.value.targetStep,
      operationType: 'BACKWARD',
      rollbackReason: rollbackForm.value.reason
    })

    if (response.data.success) {
      ElMessage.success('步骤回退成功')
      rollbackDialogVisible.value = false
      await loadOrderDishSteps()
    } else {
      ElMessage.error(response.data.message || '步骤回退失败')
    }
  } catch (error) {
    console.error('步骤回退失败:', error)
    ElMessage.error('步骤回退失败')
  }
}

// 显示步骤历史
const showStepHistory = async (dish) => {
  currentDish.value = dish
  try {
    const response = await api.get(`/v1/dish-steps/detail/${dish.orderDishId}`)
    if (response.data.success) {
      currentDishHistory.value = response.data.data.stepHistory || []
      historyDialogVisible.value = true
    } else {
      ElMessage.error(response.data.message || '获取步骤历史失败')
    }
  } catch (error) {
    console.error('获取步骤历史失败:', error)
    ElMessage.error('获取步骤历史失败')
  }
}

// 拖拽相关
const draggedItem = ref(null)
const dragStartIndex = ref(-1)

const startDrag = (event, dish) => {
  draggedItem.value = dish
  dragStartIndex.value = dishes.value.findIndex(d => d.orderDishId === dish.orderDishId)
}

// 生命周期
onMounted(() => {
  loadOrderDishSteps()
})

// 监听订单变化
watch(() => props.orderId, () => {
  if (props.orderId) {
    loadOrderDishSteps()
  }
})
</script>

<style scoped>
.dish-step-manager {
  padding: 20px;
}

.step-toolbar {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.step-filter-form {
  margin: 0;
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dish-step-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  position: relative;
}

.dish-step-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.dish-step-card.selected {
  background: #f0f9ff;
  border: 2px solid #409EFF;
}

.dish-checkbox {
  flex-shrink: 0;
}

.dish-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.dish-image {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
}

.dish-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dish-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.step-status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}

.step-progress {
  flex: 2;
  min-width: 200px;
}

.step-time-info {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.remaining-time {
  color: #67C23A;
  font-weight: 600;
}

.step-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.drag-handle {
  flex-shrink: 0;
  cursor: move;
  padding: 8px;
  color: #909399;
  transition: color 0.3s;
}

.drag-handle:hover {
  color: #409EFF;
}

.history-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-step {
  display: flex;
  align-items: center;
  gap: 8px;
}

.history-info {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.history-reason {
  color: #F56C6C;
  font-size: 12px;
}

.history-remark {
  color: #606266;
  font-size: 12px;
}
</style>
