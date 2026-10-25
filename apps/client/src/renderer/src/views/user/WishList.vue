<template>
  <div class="wish-list-page">
    <div class="page-header">
      <div>
        <h2>我的想吃列表</h2>
        <p>提交想吃的菜品，等待商家审核上架</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Refresh" :loading="loading" @click="loadItems">刷新</el-button>
        <el-button type="primary" :icon="Plus" @click="openCreateDialog">发布想吃</el-button>
      </div>
    </div>

    <div class="summary-row">
      <el-card shadow="never" class="summary-card">
        <span class="summary-number">{{ items.length }}</span>
        <span class="summary-label">全部</span>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <span class="summary-number">{{ countByStatus(0) }}</span>
        <span class="summary-label">待审核</span>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <span class="summary-number">{{ countByStatus(1) + countByStatus(6) }}</span>
        <span class="summary-label">已通过</span>
      </el-card>
      <el-card shadow="never" class="summary-card">
        <span class="summary-number">{{ countByStatus(2) }}</span>
        <span class="summary-label">已拒绝</span>
      </el-card>
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
        <el-card v-for="item in filteredItems" :key="item.id" class="wish-card" shadow="hover">
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
                <el-tag :type="getStatusType(item.auditStatus)">
                  {{ item.auditStatusName || getStatusText(item.auditStatus) }}
                </el-tag>
              </div>
              <p v-if="item.tasteRequirement" class="taste">{{ item.tasteRequirement }}</p>
              <p class="desc">{{ item.description || '暂无描述' }}</p>
            </div>
          </div>

          <div class="meta-row">
            <span>提交：{{ formatTime(item.createTime) }}</span>
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
              <el-button size="small" @click="viewDetail(item)">详情</el-button>
              <el-button
                v-if="item.canWithdraw || item.auditStatus === 0"
                size="small"
                type="warning"
                @click="withdraw(item)"
              >
                撤回
              </el-button>
              <el-button
                v-if="item.canAppeal || item.auditStatus === 2"
                size="small"
                type="primary"
                @click="openAppealDialog(item)"
              >
                申诉
              </el-button>
            </div>
          </template>
        </el-card>
      </div>

      <el-empty v-else description="暂无想吃记录">
        <el-button type="primary" @click="openCreateDialog">发布第一条想吃</el-button>
      </el-empty>
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

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'primary',
    4: 'success',
    5: 'danger',
    6: 'success',
    7: 'info'
  }
  return map[status] || 'info'
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
@accent: #d67a58;
@accent-strong: #c96b49;
@ink: #26384d;
@muted: #8b98a8;
@line: #eadfd5;
@paper: rgba(255, 255, 255, 0.86);

.wish-list-page {
  min-height: 100%;
  padding: 30px 38px 40px;
  background:
    radial-gradient(circle at 8% 6%, rgba(214, 122, 88, 0.12), transparent 28%),
    linear-gradient(180deg, #fffaf6 0%, #f7f9fc 58%, #f5f7fb 100%);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding: 22px 24px;
  border: 1px solid rgba(234, 223, 213, 0.9);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: 0 12px 28px rgba(120, 91, 72, 0.07);

  h2 {
    margin: 0;
    font-size: 28px;
    font-weight: 750;
    color: @ink;
    letter-spacing: 0;
  }

  p {
    margin: 8px 0 0;
    color: @muted;
    font-size: 15px;
  }
}

.header-actions {
  display: flex;
  gap: 10px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.summary-card {
  border: 1px solid @line;
  border-radius: 8px;
  background: @paper;
  box-shadow: 0 10px 22px rgba(49, 56, 67, 0.04);

  :deep(.el-card__body) {
    display: flex;
    align-items: baseline;
    gap: 12px;
    padding: 15px 18px;
  }
}

.summary-number {
  font-size: 28px;
  font-weight: 700;
  color: @accent;
}

.summary-label {
  color: #5f6e7d;
  font-weight: 600;
}

.filter-bar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 180px;
  gap: 12px;
  margin-bottom: 18px;

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 38px;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 0 1px @line inset;
  }
}

.content-area {
  min-height: 320px;
}

.wish-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.wish-card {
  overflow: hidden;
  border: 1px solid @line;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 28px rgba(49, 56, 67, 0.06);
  transition: transform 0.18s ease, box-shadow 0.18s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 16px 34px rgba(49, 56, 67, 0.09);
  }

  :deep(.el-card__body) {
    padding: 20px 22px 18px;
  }

  :deep(.el-card__footer) {
    padding: 10px 20px;
    background: linear-gradient(90deg, rgba(255, 248, 244, 0.9), rgba(255, 255, 255, 0.9));
    border-top: 1px solid #f0e6de;
  }
}

.card-top {
  display: flex;
  gap: 18px;
}

.image-box {
  width: 92px;
  height: 92px;
  flex: 0 0 92px;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(214, 122, 88, 0.92), rgba(240, 169, 125, 0.92)),
    #fff2ea;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: #e8825c;
  font-size: 32px;

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
  gap: 6px;
  color: #fff;
  font-weight: 700;

  .el-icon {
    font-size: 26px;
    opacity: 0.9;
  }

  span {
    max-width: 78px;
    font-size: 17px;
    line-height: 1.2;
    text-align: center;
    word-break: break-all;
  }
}

.dish-info {
  min-width: 0;
  flex: 1;
}

.title-row {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;

  h3 {
    margin: 0;
    font-size: 20px;
    color: @ink;
    word-break: break-word;
  }

  :deep(.el-tag) {
    border-radius: 6px;
    font-weight: 700;
  }
}

.taste {
  margin: 9px 0 0;
  color: @accent-strong;
  font-weight: 600;
}

.desc {
  margin: 8px 0 0;
  color: #5f6e7d;
  line-height: 1.6;
  max-width: 760px;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px dashed #eadfd5;
  color: @muted;
  font-size: 13px;
}

.reject-box,
.appeal-box {
  margin-top: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff7f4;
  color: #755247;
  border: 1px solid #f2ded5;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.appeal-box {
  background: #f2f7ff;
  color: #36526f;

  p {
    margin: 0;
  }
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

:deep(.el-button--primary) {
  --el-button-bg-color: @accent;
  --el-button-border-color: @accent;
  --el-button-hover-bg-color: @accent-strong;
  --el-button-hover-border-color: @accent-strong;
  --el-button-active-bg-color: @accent-strong;
  --el-button-active-border-color: @accent-strong;
}

:deep(.el-button) {
  border-radius: 7px;
}

@media (max-width: 920px) {
  .wish-list-page {
    padding: 22px;
  }

  .page-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .summary-row {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
  }

  .filter-bar {
    grid-template-columns: 1fr;
  }
}
</style>
