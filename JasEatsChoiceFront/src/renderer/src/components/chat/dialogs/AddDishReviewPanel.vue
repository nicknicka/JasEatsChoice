<template>
  <el-drawer
    v-model="visible"
    title="加菜审核"
    size="50%"
    @close="handleClose"
  >
    <!-- 工具栏 -->
    <div class="review-toolbar">
      <el-button @click="selectAll" :disabled="reviewList.length === 0">
        全选
      </el-button>
      <el-button @click="deselectAll" :disabled="reviewList.length === 0">
        取消全选
      </el-button>
      <el-button
        type="success"
        @click="handleApprove"
        :disabled="!hasSelection"
      >
        批量通过 ({{ selectedCount }})
      </el-button>
      <el-button
        type="danger"
        @click="showRejectDialog = true"
        :disabled="!hasSelection"
      >
        批量驳回
      </el-button>
    </div>

    <!-- 审核列表 -->
    <div class="review-list" v-loading="loading">
      <div
        v-for="item in reviewList"
        :key="item.id"
        class="review-item"
        :class="{ 'allergy-conflict': item.allergyConflict }"
      >
        <el-checkbox
          v-model="item.selected"
          @change="handleSelectionChange"
        ></el-checkbox>

        <div class="item-info">
          <!-- 请求人信息 -->
          <div class="requester">
            <el-avatar :src="item.requestUserInfo?.avatar" :size="40">
              {{ item.requestUserInfo?.nickname?.charAt(0) }}
            </el-avatar>
            <div class="requester-details">
              <span class="nickname">{{ item.requestUserInfo?.nickname }}</span>
              <span class="submit-time">{{ formatTime(item.submitTime) }}</span>
            </div>
            <el-tag
              :type="getStatusTagType(item.status)"
              size="small"
            >
              {{ item.statusDesc }}
            </el-tag>
          </div>

          <!-- 菜品列表 -->
          <div class="dish-list">
            <div v-for="dish in item.dishInfo" :key="dish.dishId" class="dish-item">
              <span class="add-dish-prefix">{{ ADD_DISH_PREFIX }}</span>
              {{ dish.dishName }} x{{ dish.quantity }}
              <span class="price">¥{{ dish.price?.toFixed(2) || '0.00' }}</span>
            </div>
          </div>

          <!-- 饮食禁忌警告 -->
          <div class="allergy-warning" v-if="item.allergyConflict">
            <el-tag
              v-for="conflict in item.conflictDetails"
              :key="conflict"
              type="danger"
              size="small"
            >
              {{ conflict }}
            </el-tag>
          </div>

          <!-- 剩余时间 -->
          <div class="remaining-time" v-if="item.remainingTime > 0">
            <el-icon><Clock /></el-icon>
            <span>剩余 {{ formatRemainingTime(item.remainingTime) }}</span>
          </div>

          <!-- 总金额 -->
          <div class="item-footer">
            <span class="total-amount">总计: ¥{{ item.totalAmount?.toFixed(2) || '0.00' }}</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && reviewList.length === 0"
        description="暂无待审核的加菜请求"
      />
    </div>

    <!-- 驳回原因对话框 -->
    <el-dialog
      v-model="showRejectDialog"
      title="选择驳回原因"
      width="400px"
    >
      <el-select
        v-model="rejectReason"
        placeholder="请选择驳回原因"
        style="width: 100%"
      >
        <el-option
          v-for="reason in DEFAULT_REJECT_REASONS"
          :key="reason"
          :label="reason"
          :value="reason"
        />
      </el-select>
      <el-input
        v-if="rejectReason === '其他原因'"
        v-model="customReason"
        type="textarea"
        placeholder="请输入驳回原因"
        :rows="3"
        style="margin-top: 12px"
      />
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReject" :disabled="!rejectReason">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import { ADD_DISH_CONFIG, DEFAULT_REJECT_REASONS, STATUS_TAG_MAP } from '@/constants/addDishConstants'
import addDishApi from '@/api/addDish'
import { useAuthStore } from '@/store/authStore'

const props = defineProps({
  modelValue: Boolean,
  groupOrderId: [String, Number]
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const reviewList = ref([])
const rejectReason = ref('')
const customReason = ref('')
const showRejectDialog = ref(false)

const ADD_DISH_PREFIX = ADD_DISH_CONFIG.ADD_DISH_PREFIX

const selectedCount = computed(() => {
  return reviewList.value.filter(item => item.selected).length
})

const hasSelection = computed(() => {
  return selectedCount.value > 0
})

// 加载审核列表
const loadReviewList = async () => {
  if (!props.groupOrderId) return

  loading.value = true
  try {
    const response = await addDishApi.getReviewList(props.groupOrderId)
    reviewList.value = (response.data || []).map(item => ({
      ...item,
      selected: false
    }))
  } catch (error) {
    ElMessage.error('加载审核列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 全选
const selectAll = () => {
  reviewList.value.forEach(item => {
    item.selected = true
  })
}

// 取消全选
const deselectAll = () => {
  reviewList.value.forEach(item => {
    item.selected = false
  })
}

// 批量通过
const handleApprove = async () => {
  const selectedIds = reviewList.value
    .filter(item => item.selected)
    .map(item => item.id)

  try {
    await addDishApi.batchReview({
      requestIds: selectedIds,
      action: 'approve',
      reviewerId: getCurrentUserId()
    })
    ElMessage.success(`已通过 ${selectedIds.length} 个加菜请求`)
    emit('refresh')
    loadReviewList()
  } catch (error) {
    ElMessage.error('批量通过失败：' + (error.message || '未知错误'))
  }
}

// 批量驳回
const handleReject = async () => {
  const selectedIds = reviewList.value
    .filter(item => item.selected)
    .map(item => item.id)

  const reason = rejectReason.value === '其他原因' ? customReason.value : rejectReason.value

  try {
    await addDishApi.batchReview({
      requestIds: selectedIds,
      action: 'reject',
      rejectReason: reason,
      reviewerId: getCurrentUserId()
    })
    ElMessage.success(`已驳回 ${selectedIds.length} 个加菜请求`)
    showRejectDialog.value = false
    emit('refresh')
    loadReviewList()
  } catch (error) {
    ElMessage.error('批量驳回失败：' + (error.message || '未知错误'))
  }
}

const handleSelectionChange = () => {
  // 触发计算属性更新
}

const handleClose = () => {
  reviewList.value = []
  rejectReason.value = ''
  customReason.value = ''
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

const formatRemainingTime = (seconds) => {
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes}分${secs}秒`
}

const getStatusTagType = (status) => {
  return STATUS_TAG_MAP[status]?.type || 'info'
}

const getCurrentUserId = () => {
  const authStore = useAuthStore()
  return parseInt(authStore.userId || '0', 10) || 1
}

// 监听visible变化，加载数据
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    loadReviewList()
  }
})

// 定时刷新（每30秒）
let refreshTimer = null
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    refreshTimer = setInterval(() => {
      loadReviewList()
    }, ADD_DISH_CONFIG.REFRESH_INTERVAL)
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }
})
</script>

<style scoped lang="scss">
.review-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.review-list {
  .review-item {
    display: flex;
    gap: 12px;
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    margin-bottom: 16px;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }

    &.allergy-conflict {
      border-color: #f56c6c;
      background-color: #fef0f0;
    }
  }

  .item-info {
    flex: 1;
  }

  .requester {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }

  .requester-details {
    display: flex;
    flex-direction: column;
    flex: 1;
  }

  .nickname {
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 4px;
  }

  .submit-time {
    font-size: 12px;
    color: #909399;
  }

  .dish-list {
    margin-bottom: 12px;
  }

  .dish-item {
    padding: 8px 0;
    font-size: 14px;
  }

  .add-dish-prefix {
    color: #f56c6c;
    font-weight: 500;
    margin-right: 4px;
  }

  .price {
    margin-left: 12px;
    color: #f56c6c;
  }

  .allergy-warning {
    margin-bottom: 8px;
  }

  .remaining-time {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #e6a23c;
    margin-bottom: 8px;
  }

  .item-footer {
    display: flex;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #ebeef5;
  }

  .total-amount {
    font-size: 16px;
    font-weight: 500;
    color: #f56c6c;
  }
}
</style>
