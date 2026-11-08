<template>
  <div class="wish-list-audit">
    <!-- 头部统计 -->
    <div v-if="items.length > 0" class="audit-header">
      <div class="stats-cards">
        <AppStatCard
          :class="{ 'audit-stat--pending': pendingCount > 0 }"
          label="待审核"
          :value="pendingCount"
          :icon="Timer"
        />
        <AppStatCard label="今日已通过" :value="todayApprovedCount" :icon="CircleCheck" />
        <AppStatCard label="今日已拒绝" :value="todayRejectedCount" :icon="CircleClose" />
      </div>
    </div>

    <!-- 标签筛选 -->
    <div v-if="items.length > 0" class="tabs-wrapper">
      <el-tabs v-model="activeTab" @tab-change="loadItems">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审核" name="pending" />
        <el-tab-pane label="已通过" name="approved" />
        <el-tab-pane label="已拒绝" name="rejected" />
        <el-tab-pane label="申诉中" name="appealing" />
      </el-tabs>
    </div>

    <!-- 列表内容 -->
    <div v-loading="loading" class="audit-content">
      <div v-if="items.length > 0" class="wish-cards">
        <div v-for="item in items" :key="item.id" class="wish-card">
          <!-- 菜品信息 -->
          <div class="wish-header">
            <div class="dish-image" v-if="item.dishImage">
              <img :src="item.dishImage" :alt="item.dishName" />
            </div>
            <div class="dish-info">
              <h2 class="dish-name">{{ item.dishName }}</h2>
              <p class="wish-meta">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatTime(item.createdAt) }}</span>
                <el-tag v-if="item.tasteRequirement" size="small" type="info">
                  {{ item.tasteRequirement }}
                </el-tag>
              </p>
            </div>
          </div>

          <!-- 用户描述 -->
          <div class="wish-description" v-if="item.description">
            <p>{{ item.description }}</p>
          </div>

          <!-- 状态和操作 -->
          <div class="wish-footer">
            <div class="status-info">
              <StatusTag
                :status="statusTagStates[getStatusTagType(item.auditStatus)]"
                :text="item.auditStatusName"
              />
              <span v-if="item.auditStatus === 0" class="deadline">
                剩余：{{ item.remainingHours }}小时
              </span>
            </div>

            <div class="audit-actions">
              <el-button v-if="item.auditStatus === 0" @click="handleAudit(item, true)">
                通过
              </el-button>
              <el-button v-if="item.auditStatus === 2" @click="handleAppeal(item)">
                申诉
              </el-button>
              <el-dropdown
                v-if="item.auditStatus === 0"
                trigger="click"
                popper-class="wish-audit-menu"
                @command="$event === 'reject' ? handleAudit(item, false) : viewDetail(item)"
              >
                <el-button link class="text-action" :aria-label="`${item.dishName}的更多操作`">
                  更多
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="detail">查看详情</el-dropdown-item>
                    <el-dropdown-item command="reject" divided class="reject-action">
                      拒绝
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button v-else link class="text-action" @click="viewDetail(item)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <AppEmptyState
        v-else-if="!loading"
        class="audit-empty"
        compact
        description="暂无审核项目，顾客提交想吃条目后会出现在这里"
        action-text="刷新"
        @action="loadItems"
      />
    </div>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="isApprove ? '审核通过' : '审核拒绝'"
      width="600px"
    >
      <el-form v-if="isApprove" :model="auditForm" label-width="120px">
        <el-form-item label="预计上架时间">
          <el-date-picker
            v-model="auditForm.actualAvailableTime"
            type="date"
            placeholder="选择预计上架日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <el-form v-else :model="auditForm" label-width="120px">
        <el-form-item label="拒绝原因" prop="rejectionReasonCode">
          <el-select
            v-model="auditForm.rejectionReasonCode"
            placeholder="请选择拒绝原因"
            style="width: 100%"
          >
            <el-option label="请选择" :value="null" />
            <el-option
              v-for="reason in rejectionReasons"
              :key="reason.code"
              :label="reason.title"
              :value="reason.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="拒绝说明">
          <el-input
            v-model="auditForm.auditRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入拒绝说明（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button class="dialog-cancel" @click="auditDialogVisible = false">取消</el-button>
        <el-button
          class="dialog-confirm"
          :class="{ 'dialog-confirm--danger': !isApprove }"
          :type="isApprove ? 'primary' : 'danger'"
          @click="confirmAudit"
          :loading="auditing"
        >
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 申诉对话框 -->
    <el-dialog
      v-model="appealDialogVisible"
      title="申诉"
      width="600px"
    >
      <el-form :model="appealForm" :rules="appealRules" ref="appealFormRef" label-width="120px">
        <el-form-item label="申诉内容" prop="appealContent">
          <el-input
            v-model="appealForm.appealContent"
            type="textarea"
            :rows="4"
            placeholder="请说明为什么您认为这个需求应该被通过..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button class="dialog-cancel" @click="appealDialogVisible = false">取消</el-button>
        <el-button class="dialog-confirm" type="primary" @click="submitAppeal" :loading="appealing">
          提交申诉
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Calendar, CircleCheck, CircleClose, Timer } from '@element-plus/icons-vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import wishListApi from '@/api/wishList'

const props = defineProps({
  merchantId: {
    type: String,
    default: ''
  }
})

const loading = ref(false)
const activeTab = ref('all')
const items = ref([])

// 统计数据
const pendingCount = ref(0)
const todayApprovedCount = ref(0)
const todayRejectedCount = ref(0)

// 审核对话框
const auditDialogVisible = ref(false)
const isApprove = ref(true)
const auditing = ref(false)
const currentItem = ref(null)

const auditForm = reactive({
  actualAvailableTime: null,
  rejectionReasonCode: null,
  auditRemark: ''
})

// 申诉对话框
const appealDialogVisible = ref(false)
const appealing = ref(false)
const appealFormRef = ref(null)
const appealForm = reactive({
  appealContent: ''
})

const appealRules = {
  appealContent: [
    { required: true, message: '请输入申诉内容', trigger: 'blur' }
  ]
}

// 拒绝原因列表
const rejectionReasons = [
  { code: 1, title: '食材季节性短缺' },
  { code: 2, title: '食材供应链问题' },
  { code: 3, title: '制作工艺过于复杂' },
  { code: 4, title: '成本过高' },
  { code: 5, title: '与餐厅定位不符' },
  { code: 6, title: '食品安全考虑' },
  { code: 7, title: '原料品质不稳定' },
  { code: 8, title: '制作时间过长' },
  { code: 9, title: '特殊设备限制' },
  { code: 99, title: '其他原因' }
]

const statusTagStates = {
  warning: 'pending',
  success: 'approved',
  danger: 'rejected',
  info: 'appealing'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return typeMap[status] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

// 加载审核列表
const loadItems = async () => {
  loading.value = true
  try {
    const response = await wishListApi.getPendingItems(props.merchantId || '')
    if (response.code === 200) {
      items.value = response.data || []

      // 计算统计
      pendingCount.value = items.value.filter(i => i.auditStatus === 0).length
      todayApprovedCount.value = items.value.filter(i =>
        i.auditStatus === 1 && isToday(i.auditTime)
      ).length
      todayRejectedCount.value = items.value.filter(i =>
        i.auditStatus === 2 && isToday(i.auditTime)
      ).length
    }
  } catch (error) {
    console.error('加载失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 判断是否是今天
const isToday = (time) => {
  if (!time) return false
  const date = new Date(time)
  const today = new Date()
  return date.getDate() === today.getDate() &&
    date.getMonth() === today.getMonth() &&
    date.getFullYear() === today.getFullYear()
}

// 处理审核
const handleAudit = (item, approve) => {
  currentItem.value = item
  isApprove.value = approve
  auditDialogVisible.value = true

  // 重置表单
  auditForm.actualAvailableTime = null
  auditForm.rejectionReasonCode = null
  auditForm.auditRemark = ''
}

// 确认审核
const confirmAudit = async () => {
  try {
    auditing.value = true
    const response = await wishListApi.auditItem(currentItem.value.id, {
      approved: isApprove.value,
      actualAvailableTime: auditForm.actualAvailableTime,
      rejectionReasonCode: auditForm.rejectionReasonCode,
      auditRemark: auditForm.auditRemark
    })

    if (response.code === 200) {
      ElMessage.success(isApprove.value ? '审核通过' : '已拒绝')
      auditDialogVisible.value = false
      loadItems()
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  } finally {
    auditing.value = false
  }
}

// 处理申诉
const handleAppeal = (item) => {
  currentItem.value = item
  appealDialogVisible.value = true
  appealForm.appealContent = ''
}

// 提交申诉
const submitAppeal = async () => {
  try {
    await appealFormRef.value.validate()

    appealing.value = true
    const response = await wishListApi.appealRejection(currentItem.value.id, {
      appealContent: appealForm.appealContent
    })

    if (response.code === 200) {
      ElMessage.success('申诉提交成功')
      appealDialogVisible.value = false
      loadItems()
    }
  } catch (error) {
    console.error('申诉失败:', error)
    ElMessage.error('申诉失败')
  } finally {
    appealing.value = false
  }
}

// 查看详情
const viewDetail = (item) => {
  const detailHtml = `
    <div style="line-height: 1.8;">
      <p><strong>菜品名称：</strong>${item.dishName || '未命名'}</p>
      <p><strong>期望上架时间：</strong>${item.expectedAvailableTime || '未设置'}</p>
      <p><strong>提交时间：</strong>${item.createdAt || ''}</p>
      <p><strong>用户备注：</strong>${item.remark || '无'}</p>
      ${item.image ? `<p><strong>图片：</strong><br/><img src="${item.image}" style="max-width: 200px; max-height: 200px; border-radius: 8px;" /></p>` : ''}
    </div>
  `

  ElMessageBox.alert(detailHtml, '心愿单详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.audit-header {
  margin-bottom: var(--card-gap-compact);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--card-gap-compact);
}

.stats-cards :deep(.app-stat-card) {
  padding: var(--card-padding-compact);
}

.stats-cards :deep(.app-stat-card__value) {
  font-size: var(--text-h1);
}

.stats-cards :deep(.app-stat-card__icon) {
  color: var(--ink-400);
}

.audit-stat--pending :deep(.app-stat-card__icon),
.audit-stat--pending :deep(.app-stat-card__value) {
  color: var(--brand-600);
}

.tabs-wrapper {
  margin-bottom: var(--card-gap-compact);
}

.audit-content {
  min-height: 200px;
}

.audit-empty {
  animation: audit-empty-in var(--duration-base) var(--ease-out);
}

@keyframes audit-empty-in {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

.wish-cards {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
}

.wish-card {
  padding: var(--card-padding-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
}

.wish-header {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.dish-image {
  width: 80px;
  height: 80px;
  flex-shrink: 0;
}

.dish-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.dish-info {
  flex: 1;
}

.dish-name {
  margin: 0 0 var(--space-2);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.wish-meta {
  margin: 0;
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-body-sm);
  color: var(--ink-500);
}

.wish-description {
  margin: 0 0 var(--space-3);
  padding: var(--space-3);
  background: var(--canvas-sunken);
  border-radius: var(--radius-sm);
}

.wish-description p {
  margin: 0;
  font-size: var(--text-body);
  color: var(--ink-700);
  line-height: 1.5;
}

.wish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.deadline {
  font-size: var(--text-body-sm);
  color: var(--warning);
}

.audit-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.audit-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.wish-footer :deep(.el-button),
.audit-empty :deep(.el-button),
.dialog-cancel {
  height: var(--control-height-sm);
  border-radius: var(--radius-md);
}

.text-action {
  --el-button-text-color: var(--brand-600);
  --el-button-hover-text-color: var(--brand-500);
  --el-button-active-text-color: var(--brand-700);
}

:global(.wish-audit-menu .reject-action) {
  color: var(--danger);
  margin-top: var(--space-6);
}

:global(.wish-audit-menu .reject-action:not(.is-disabled):hover),
:global(.wish-audit-menu .reject-action:focus) {
  color: var(--danger);
  background: var(--danger-bg);
}

.dialog-confirm {
  height: var(--control-height-md);
  border-radius: var(--radius-md);
}

.dialog-confirm--danger {
  margin-left: var(--space-6);
}
</style>
