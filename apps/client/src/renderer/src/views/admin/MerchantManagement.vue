<template>
  <div class="merchant-management-container">
    <!-- 页面标题 -->
    <AppPageHeader title="商家管理" subtitle="管理系统所有商家" :compact="true" />

    <!-- 统计卡片 -->
    <div class="stats-section">
      <AppStatCard label="总商家数" :value="stats.total" icon="Shop" />
      <AppStatCard label="营业中" :value="stats.active" icon="CircleCheck" />
      <AppStatCard class="stat-card-pending" label="待审核" :value="stats.pending" icon="Clock" />
      <AppStatCard label="已暂停" :value="stats.inactive" icon="CircleClose" />
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索商家名称、地址"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="营业中" value="ACTIVE" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已暂停" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 商家列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="merchantList" v-loading="loading" stripe>
        <el-table-column prop="merchantId" label="商家ID" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="cell-id">{{ row.merchantId }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商家名称" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="170" show-overflow-tooltip />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <RowActions
              :primary="{ text: '查看', onClick: () => handleView(row) }"
              :items="rowActions(row)"
              :max-visible="1"
            />
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchMerchantList"
          @current-change="fetchMerchantList"
        />
      </div>
    </el-card>

    <!-- 商家详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商家详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentMerchant" :column="2" border>
        <el-descriptions-item label="商家ID">{{ currentMerchant.merchantId }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentMerchant.name }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentMerchant)">
            {{ getStatusText(currentMerchant) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMerchant.phone }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ currentMerchant.address }}</el-descriptions-item>
        <el-descriptions-item label="营业时间" :span="2">{{ currentMerchant.businessHours || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商家简介" :span="2">{{ currentMerchant.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ formatTime(currentMerchant.createdAt) }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="getMerchantDisplayStatus(currentMerchant) === 'PENDING'"
          type="primary"
          @click="handleAudit(currentMerchant)"
        >
          去审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="商家审核"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" v-if="auditForm.status === 'REJECTED'">
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import RowActions from '@/components/common/RowActions.vue'
import { formatTime } from '../../utils/formatters'
import { getMerchantList, auditMerchant, updateMerchantStatus } from '@/api/admin'

const loading = ref(false)
const merchantList = ref([])
const currentMerchant = ref(null)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)

const stats = reactive({
  total: 0,
  active: 0,
  pending: 0,
  inactive: 0
})

const searchForm = reactive({
  keyword: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  merchantId: null,
  status: 'APPROVED',
  reason: ''
})

const getMerchantDisplayStatus = (merchant) => {
  if (!merchant) {
    return ''
  }

  if (merchant.auditStatus === 'PENDING' || merchant.auditStatus === 'REJECTED') {
    return merchant.auditStatus
  }

  if (merchant.auditStatus === 'APPROVED') {
    return merchant.status ? 'ACTIVE' : 'INACTIVE'
  }

  if (merchant.status === true) {
    return 'ACTIVE'
  }

  if (merchant.status === false) {
    return 'INACTIVE'
  }

  return merchant.status || ''
}

// 获取商家列表
const fetchMerchantList = async () => {
  loading.value = true
  try {
    console.log('[商家管理] 获取商家列表, 页码:', pagination.page, '每页:', pagination.pageSize)
    const response = await getMerchantList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })

    if (response) {
      merchantList.value = response.records || []
      // 后端分页插件缺失时返回 total=0 与全量记录，此处以实际记录数兜底
      pagination.total = response.total || merchantList.value.length

      // 更新统计数据
      stats.total = pagination.total
      stats.active = merchantList.value.filter(m => getMerchantDisplayStatus(m) === 'ACTIVE').length
      stats.pending = merchantList.value.filter(m => getMerchantDisplayStatus(m) === 'PENDING').length
      stats.inactive = merchantList.value.filter(m => getMerchantDisplayStatus(m) === 'INACTIVE').length
      console.log('[商家管理] 获取商家列表成功, 总数:', pagination.total)
    }
  } catch (error) {
    console.error('[商家管理] 获取商家列表失败:', error)
    ElMessage.error('获取商家列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (merchant) => {
  const status = getMerchantDisplayStatus(merchant)
  const types = {
    'ACTIVE': 'success',
    'PENDING': 'warning',
    'INACTIVE': 'info',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (merchant) => {
  const status = getMerchantDisplayStatus(merchant)
  const texts = {
    'ACTIVE': '营业中',
    'PENDING': '待审核',
    'INACTIVE': '已暂停',
    'REJECTED': '已拒绝'
  }
  return texts[status] || '未知'
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchMerchantList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.page = 1
  fetchMerchantList()
}

// 行操作：查看 + 审核/状态（§4.3）
// 「审核」与「状态」都先弹对话框，二次确认由对话框自身承担，故保持可见、不进「更多」。
const rowActions = (row) => [
  getMerchantDisplayStatus(row) === 'PENDING'
    ? { text: '审核', onClick: () => handleAudit(row) }
    : { text: '状态', onClick: () => handleEditStatus(row) }
]

// 查看商家详情
const handleView = (row) => {
  currentMerchant.value = row
  detailDialogVisible.value = true
}

// 审核
const handleAudit = (row) => {
  auditForm.merchantId = row.merchantId
  auditForm.status = 'APPROVED'
  auditForm.reason = ''
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  try {
    console.log('[商家管理] 提交审核, 商家ID:', auditForm.merchantId, '状态:', auditForm.status)
    const response = await auditMerchant(auditForm.merchantId, {
      status: auditForm.status,
      reason: auditForm.reason
    })

    if (response.success) {
      ElMessage.success('审核完成')
      auditDialogVisible.value = false
      detailDialogVisible.value = false
      fetchMerchantList()
      console.log('[商家管理] 审核成功')
    } else {
      ElMessage.error(response.message || '审核失败')
    }
  } catch (error) {
    console.error('[商家管理] 审核失败:', error)
    ElMessage.error('审核失败: ' + (error.message || '网络错误'))
  }
}

// 修改状态
const handleEditStatus = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请选择新状态', '修改状态', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'select',
      inputOptions: [
        { value: 'ACTIVE', label: '营业中' },
        { value: 'INACTIVE', label: '已暂停' }
      ],
      inputValue: row.status
    })

    console.log('[商家管理] 修改商家状态, 商家ID:', row.merchantId, '状态:', value)
    const response = await updateMerchantStatus(row.merchantId, value)

    if (response.success) {
      ElMessage.success('状态修改成功')
      fetchMerchantList()
      console.log('[商家管理] 状态修改成功')
    } else {
      ElMessage.error(response.message || '状态修改失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[商家管理] 状态修改失败:', error)
      ElMessage.error('状态修改失败: ' + (error.message || '网络错误'))
    }
  }
}

onMounted(() => {
  fetchMerchantList()
})
</script>

<style scoped lang="less">
@import '@/assets/css/nordic-theme.less';

.merchant-management-container {
  padding: var(--card-padding-compact) var(--page-gutter);
  background: var(--canvas);
  max-width: 1440px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;

  .stats-section {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: var(--card-gap-compact);
    margin-bottom: var(--space-8);
  }

  /* 待审核是待办入口，按 §3.1 语义色给警示黄强调（§4.8） */
  .stat-card-pending :deep(.app-stat-card__icon) {
    color: var(--warning);
  }

  .search-card {
    margin-bottom: var(--space-8);

    .search-form {
      margin-bottom: 0;
    }
  }

  .table-card {
    .pagination-container {
      margin-top: var(--space-5);
      display: flex;
      justify-content: flex-end;
    }
  }

  /* 商家 ID 是不可拆分的标识：单行 + 等宽数字（§4.3 §5.4） */
  .cell-id {
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
  }
}
</style>
