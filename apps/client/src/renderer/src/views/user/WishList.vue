<template>
  <div class="wish-list-page">
    <AppPageHeader title="我的想吃列表" subtitle="提交想吃的菜品，等待商家审核上架">
      <template #actions>
        <el-button :icon="Refresh" :loading="loading" @click="loadItems">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">发布想吃</el-button>
      </template>
    </AppPageHeader>

    <div class="summary-row">
      <div class="summary-block">
        <span class="summary-number">{{ items.length }}</span>
        <span class="summary-label">全部</span>
      </div>
      <div class="summary-block">
        <span class="summary-number">{{ countByStatus(0) }}</span>
        <span class="summary-label">
          <i class="summary-dot summary-dot--warning"></i>
          待审核
        </span>
      </div>
      <div class="summary-block">
        <span class="summary-number">{{ countByStatus(1) + countByStatus(6) }}</span>
        <span class="summary-label">
          <i class="summary-dot summary-dot--info"></i>
          已通过
        </span>
      </div>
      <div class="summary-block">
        <span class="summary-number">{{ countByStatus(2) }}</span>
        <span class="summary-label">
          <i class="summary-dot summary-dot--danger"></i>
          已拒绝
        </span>
      </div>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="keyword"
        clearable
        :prefix-icon="Search"
        placeholder="搜索菜品名称或口味要求"
      />
      <el-select v-model="statusFilter" placeholder="状态筛选">
        <el-option label="全部" value="all" />
        <el-option label="待审核" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
        <el-option label="申诉中" :value="3" />
        <el-option label="已撤回" :value="7" />
      </el-select>
    </div>

    <div v-loading="loading" class="content-area">
      <div v-if="filteredItems.length" class="wish-grid">
        <el-card v-for="item in filteredItems" :key="item.id" class="wish-card" shadow="never">
          <div class="card-top">
            <div class="image-box">
              <img v-if="isUsableImage(item.dishImage)" :src="item.dishImage" :alt="item.dishName" />
              <div v-else class="image-fallback">
                <el-icon><Bowl /></el-icon>
                <span>{{ getDishInitials(item.dishName) }}</span>
              </div>
            </div>
            <div class="dish-info">
              <div class="title-row">
                <h3>{{ item.dishName || '未命名菜品' }}</h3>
                <StatusTag
                  :status="getStatusKey(item.auditStatus)"
                  :text="item.auditStatusName || getStatusText(item.auditStatus)"
                  size="sm"
                />
              </div>
              <p v-if="item.tasteRequirement" class="taste">{{ item.tasteRequirement }}</p>
              <p class="desc">{{ item.description || '暂无描述' }}</p>
            </div>
          </div>

          <div class="meta-row">
            <span>提交：{{ formatTime(item.createdAt) }}</span>
            <span v-if="item.auditStatus === 0">剩余：{{ item.remainingHours ?? '-' }}小时</span>
            <span v-if="item.expectedAvailableTime">期望：{{ formatDate(item.expectedAvailableTime) }}</span>
          </div>

          <div v-if="item.auditStatus === 2" class="reject-box">
            <strong>{{ item.rejectionReasonTitle || '拒绝原因' }}</strong>
            <span>{{ item.auditRemark || item.rejectionReasonDescription || '商家暂未填写说明' }}</span>
          </div>

          <div v-if="item.appealContent || item.appealReply" class="appeal-box">
            <p v-if="item.appealContent">申诉：{{ item.appealContent }}</p>
            <p v-if="item.appealReply">回复：{{ item.appealReply }}</p>
          </div>

          <template #footer>
            <div class="card-actions">
              <el-button size="small" type="primary" @click="viewDetail(item)">查看详情</el-button>
              <el-button
                v-if="item.canWithdraw || item.auditStatus === 0"
                size="small"
                class="action-text"
                @click="withdraw(item)"
              >
                撤回
              </el-button>
              <el-button
                v-if="item.canAppeal || item.auditStatus === 2"
                size="small"
                class="action-text"
                @click="openAppealDialog(item)"
              >
                申诉
              </el-button>
            </div>
          </template>
        </el-card>
      </div>

      <div v-else class="empty-wrap">
        <AppEmptyState description="还没有想吃记录" :icon="Bowl" />
        <el-button type="primary" @click="openCreateDialog">发布第一条想吃</el-button>
      </div>
    </div>

    <el-dialog v-model="createDialogVisible" title="发布想吃" width="560px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="96px">
        <el-form-item label="商家ID">
          <el-input v-model="createForm.merchantId" placeholder="不填则由后端兜底处理" />
        </el-form-item>
        <el-form-item label="菜品名称" prop="dishName">
          <el-input v-model="createForm.dishName" placeholder="例如：低脂藤椒鸡胸饭" />
        </el-form-item>
        <el-form-item label="图片地址">
          <el-input v-model="createForm.dishImage" placeholder="可选，填写图片 URL" />
        </el-form-item>
        <el-form-item label="口味要求">
          <el-input v-model="createForm.tasteRequirement" placeholder="例如：少油少盐、微辣" />
        </el-form-item>
        <el-form-item label="期望日期">
          <el-date-picker
            v-model="createForm.expectedAvailableTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="选择期望上架时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
            placeholder="描述你想吃的原因、配料、营养或口味要求"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="appealDialogVisible" title="提交申诉" width="520px">
      <el-form ref="appealFormRef" :model="appealForm" :rules="appealRules" label-width="80px">
        <el-form-item label="申诉内容" prop="appealContent">
          <el-input
            v-model="appealForm.appealContent"
            type="textarea"
            :rows="5"
            maxlength="300"
            show-word-limit
            placeholder="说明你认为该想吃需求应被通过的理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="appealing" @click="submitAppeal">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bowl, Plus, Refresh, Search } from '@element-plus/icons-vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import wishListApi from '@/api/wishList'

const loading = ref(false)
const submitting = ref(false)
const appealing = ref(false)
const items = ref([])
const keyword = ref('')
const statusFilter = ref('all')

const createDialogVisible = ref(false)
const appealDialogVisible = ref(false)
const createFormRef = ref(null)
const appealFormRef = ref(null)
const currentItem = ref(null)

const defaultMerchantId = '7638432224340229'

const createForm = reactive({
  merchantId: defaultMerchantId,
  dishName: '',
  dishImage: '',
  tasteRequirement: '',
  description: '',
  expectedAvailableTime: ''
})

const appealForm = reactive({
  appealContent: ''
})

const createRules = {
  dishName: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }]
}

const appealRules = {
  appealContent: [{ required: true, message: '请输入申诉内容', trigger: 'blur' }]
}

const filteredItems = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  return items.value.filter((item) => {
    const matchStatus = statusFilter.value === 'all' || item.auditStatus === statusFilter.value
    const matchText =
      !text ||
      [item.dishName, item.tasteRequirement, item.description]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(text))
    return matchStatus && matchText
  })
})

const normalizeList = (response) => {
  if (Array.isArray(response?.data)) return response.data
  if (Array.isArray(response)) return response
  return []
}

const loadItems = async () => {
  loading.value = true
  try {
    const response = await wishListApi.getUserItems()
    items.value = normalizeList(response)
  } catch (error) {
    console.error('加载想吃列表失败:', error)
    ElMessage.error('加载想吃列表失败')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  createForm.merchantId = defaultMerchantId
  createForm.dishName = ''
  createForm.dishImage = ''
  createForm.tasteRequirement = ''
  createForm.description = ''
  createForm.expectedAvailableTime = ''
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()

  submitting.value = true
  try {
    await wishListApi.createItem({
      merchantId: createForm.merchantId || null,
      dishName: createForm.dishName,
      dishImage: createForm.dishImage || null,
      tasteRequirement: createForm.tasteRequirement || null,
      description: createForm.description || null,
      expectedAvailableTime: createForm.expectedAvailableTime || null
    })
    ElMessage.success('已发布，等待商家审核')
    createDialogVisible.value = false
    await loadItems()
  } catch (error) {
    console.error('发布想吃失败:', error)
    ElMessage.error(error?.response?.data?.message || '发布想吃失败')
  } finally {
    submitting.value = false
  }
}

const withdraw = async (item) => {
  try {
    await ElMessageBox.confirm(`确定撤回“${item.dishName}”吗？`, '撤回确认', {
      confirmButtonText: '撤回',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await wishListApi.withdrawItem(item.id)
    ElMessage.success('已撤回')
    await loadItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回失败:', error)
      ElMessage.error('撤回失败')
    }
  }
}

const openAppealDialog = (item) => {
  currentItem.value = item
  appealForm.appealContent = ''
  appealDialogVisible.value = true
}

const submitAppeal = async () => {
  if (!appealFormRef.value || !currentItem.value) return
  await appealFormRef.value.validate()

  appealing.value = true
  try {
    await wishListApi.appealRejection(currentItem.value.id, {
      appealContent: appealForm.appealContent
    })
    ElMessage.success('申诉已提交')
    appealDialogVisible.value = false
    await loadItems()
  } catch (error) {
    console.error('提交申诉失败:', error)
    ElMessage.error(error?.response?.data?.message || '提交申诉失败')
  } finally {
    appealing.value = false
  }
}

const viewDetail = (item) => {
  ElMessageBox.alert(
    `<div class="wish-detail-dialog">
      <p><strong>菜品名称：</strong>${item.dishName || '-'}</p>
      <p><strong>口味要求：</strong>${item.tasteRequirement || '-'}</p>
      <p><strong>详细描述：</strong>${item.description || '-'}</p>
      <p><strong>审核状态：</strong>${item.auditStatusName || getStatusText(item.auditStatus)}</p>
      <p><strong>期望上架：</strong>${formatDate(item.expectedAvailableTime)}</p>
      <p><strong>实际安排：</strong>${item.actualAvailableTime || '-'}</p>
      <p><strong>商家备注：</strong>${item.auditRemark || '-'}</p>
    </div>`,
    '想吃详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

const countByStatus = (status) => items.value.filter((item) => item.auditStatus === status).length

const isUsableImage = (image) => {
  return image && !String(image).includes('via.placeholder.com')
}

const getDishInitials = (name) => {
  return String(name || '想吃').slice(0, 2)
}

const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '申诉中',
    4: '申诉成功',
    5: '申诉失败',
    6: '超时通过',
    7: '已撤回'
  }
  return map[status] || '未知'
}

const getStatusKey = (status) => {
  const map = {
    0: 'pendingaudit',
    1: 'approved',
    2: 'rejected',
    3: 'appealing',
    4: 'appealapproved',
    5: 'appealrejected',
    6: 'autoapproved',
    7: 'withdrawn'
  }
  return map[status] || 'unknown'
}

const formatDate = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

const formatTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadItems)
</script>

<style scoped lang="less">
/* 页面基准色调全部来自 tokens.css（§3.1/§3.4/§7.1），不写裸色值。 */
.wish-list-page {
  box-sizing: border-box;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 页头：AppPageHeader 标准版（标题 --text-h1 + --space-6，§4.7）。 */
:deep(.app-page-header) {
  background: var(--canvas);
}

/* 概览卡压缩成一条摘要，把首屏让给列表（§4.8）。 */
.summary-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.summary-block {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.summary-number {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h2);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
  line-height: 1;
}

.summary-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

/* 统计卡的状态指示点只做语义标记（§3.1、§4.8）。 */
.summary-dot {
  width: 6px;
  height: 6px;
  flex: 0 0 6px;
  border-radius: var(--radius-full);
}

.summary-dot--warning {
  background: var(--warning);
}

.summary-dot--info {
  background: var(--info);
}

.summary-dot--danger {
  background: var(--danger);
}

.filter-bar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 180px;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.filter-bar :deep(.el-input__wrapper),
.filter-bar :deep(.el-select__wrapper) {
  min-height: 38px;
  border-radius: var(--radius-md);
  background: var(--surface);
  box-shadow: 0 0 0 1px var(--line) inset;
}

.content-area {
  min-height: 320px;
}

.wish-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-4);
}

/* 想吃条：L1 微质感卡片，同列高度与图片比例一致（§3.4、§4.2）。 */
.wish-card {
  border-radius: var(--radius-lg);
  overflow: hidden;

  :deep(.el-card__body) {
    padding: var(--card-padding);
  }

  :deep(.el-card__footer) {
    padding: var(--space-3) var(--card-padding);
    border-top: 1px solid var(--line);
    background: var(--surface);
  }
}

.wish-card :deep(.el-card__body) {
  border-bottom: 0;
}

.card-top {
  display: flex;
  align-items: flex-start;
  gap: var(--card-padding);
}

/* 图片统一 4:3 裁切（§4.2）：144 × 108。 */
.image-box {
  width: 144px;
  height: 108px;
  flex: 0 0 144px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--brand-400), var(--brand-200));
  color: var(--brand-700);
  font-size: var(--text-display);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.image-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  color: var(--surface);
  font-weight: var(--weight-bold);

  .el-icon {
    font-size: 24px;
    opacity: 0.9;
  }

  span {
    max-width: 116px;
    overflow: hidden;
    font-size: var(--text-body-sm);
    line-height: 1.2;
    text-align: center;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.dish-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
  flex: 1;
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
  min-width: 0;

  /* 标题为卡片第一扫描点（§3.2：--text-h2）。 */
  h3 {
    margin: 0;
    min-width: 0;
    color: var(--ink-900);
    font-size: var(--text-h2);
    font-weight: var(--weight-semibold);
    line-height: 28px;
    letter-spacing: var(--tracking-tight);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.taste {
  margin: 0;
  color: var(--brand-600);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
}

.desc {
  margin: 0;
  max-width: 760px;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1px dashed var(--line);
  color: var(--ink-400);
  font-size: var(--text-caption);
}

.reject-box,
.appeal-box {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  margin-top: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-sm);
}

.reject-box {
  border: 1px solid var(--danger-bg);
  background: var(--danger-bg);
  color: var(--danger);
}

.appeal-box {
  border: 1px solid var(--info-bg);
  background: var(--info-bg);
  color: var(--info);

  p {
    margin: 0;
  }
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

/* 卡片主操作唯一实心按钮；次要动作走文字层级（§4.1）。 */
.wish-card :deep(.action-text) {
  --el-button-text-color: var(--brand-600);
  --el-button-hover-text-color: var(--brand-700);
  --el-button-hover-bg-color: var(--brand-50);
  --el-button-hover-border-color: transparent;
  --el-button-active-text-color: var(--brand-700);
  --el-button-active-bg-color: var(--brand-100);
  border-color: transparent;
  background: transparent;
}

.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
  padding-bottom: var(--space-8);
}

@media (max-width: 920px) {
  .wish-list-page {
    padding: 0 var(--page-gutter) var(--space-8);
  }

  .summary-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-bar {
    grid-template-columns: 1fr;
  }

  .card-top {
    gap: var(--space-4);
  }

  .image-box {
    width: 120px;
    height: 90px;
    flex: 0 0 120px;
  }
}

@media (max-width: 640px) {
  .wish-list-page {
    padding: 0 var(--space-4) var(--space-8);
  }

  .image-box {
    width: 108px;
    height: 81px;
    flex: 0 0 108px;
  }

  .title-row h3 {
    font-size: var(--text-h3);
    line-height: 24px;
  }
}
</style>
