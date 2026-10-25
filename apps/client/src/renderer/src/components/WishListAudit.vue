<template>
  <div class="wish-list-audit">
    <!-- 头部统计 -->
    <div class="audit-header">
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-number">{{ pendingCount }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ todayApprovedCount }}</div>
          <div class="stat-label">今日已通过</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ todayRejectedCount }}</div>
          <div class="stat-label">今日已拒绝</div>
        </div>
      </div>
    </div>

    <!-- 标签筛选 -->
    <div class="tabs-wrapper">
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
        <div
          v-for="item in items"
          :key="item.id"
          class="wish-card"
          :class="getStatusClass(item.auditStatus)"
        >
          <!-- 菜品信息 -->
          <div class="wish-header">
            <div class="dish-image" v-if="item.dishImage">
              <img :src="item.dishImage" :alt="item.dishName" />
            </div>
            <div class="dish-info">
              <h4 class="dish-name">{{ item.dishName }}</h4>
              <p class="wish-meta">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatTime(item.createTime) }}</span>
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
              <el-tag :type="getStatusTagType(item.auditStatus)">
                {{ item.auditStatusName }}
              </el-tag>
              <span v-if="item.auditStatus === 0" class="deadline">
                剩余：{{ item.remainingHours }}小时
              </span>
            </div>

            <!-- 审核操作（仅待审核状态） -->
            <div class="audit-actions" v-if="item.auditStatus === 0">
              <el-button
                type="success"
                size="small"
                @click="handleAudit(item, true)"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleAudit(item, false)"
              >
                拒绝
              </el-button>
            </div>

            <!-- 申诉操作（已拒绝状态） -->
            <div class="audit-actions" v-if="item.auditStatus === 2">
              <el-button
                type="primary"
                size="small"
                @click="handleAppeal(item)"
              >
                申诉
              </el-button>
            </div>

            <!-- 查看详情 -->
            <el-button
              type="info"
              size="small"
              text
              @click="viewDetail(item)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-else-if="!loading"
        description="暂无审核项目"
        :image-size="80"
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
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAudit" :loading="auditing">
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
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppeal" :loading="appealing">
          提交申诉
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
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

// 获取状态类名
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-approved',
    2: 'status-rejected',
    3: 'status-appealing'
  }
  return classMap[status] || ''
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
      <p><strong>提交时间：</strong>${item.createTime || ''}</p>
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
  margin-bottom: 20px;
}

.stats-cards {
  display: flex;
  gap: 16px;
}

.stat-card {
  flex: 1;
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-number {
  font-size: 2rem /* 原值: 28px */;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 1rem /* 原值: 14px */;
  color: #909399;
}

.tabs-wrapper {
  margin-bottom: 20px;
}

.audit-content {
  min-height: 200px;
}

.wish-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.wish-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #e4e7ed;
  transition: all 0.3s;
}

.wish-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.wish-card.status-pending {
  border-left-color: #e6a23c;
}

.wish-card.status-approved {
  border-left-color: #67c23a;
}

.wish-card.status-rejected {
  border-left-color: #f56c6c;
}

.wish-card.status-appealing {
  border-left-color: #409eff;
}

.wish-header {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
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
  border-radius: 6px;
}

.dish-info {
  flex: 1;
}

.dish-name {
  margin: 0 0 8px 0;
  font-size: 1.143rem /* 原值: 16px */;
  font-weight: 600;
  color: #303133;
}

.wish-meta {
  margin: 0;
  font-size: 0.929rem /* 原值: 13px */;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.wish-description {
  margin: 0 0 12px 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.wish-description p {
  margin: 0;
  font-size: 1rem /* 原值: 14px */;
  color: #606266;
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
  gap: 8px;
}

.deadline {
  font-size: 0.857rem /* 原值: 12px */;
  color: #e6a23c;
}

.audit-actions {
  display: flex;
  gap: 8px;
}
</style>
