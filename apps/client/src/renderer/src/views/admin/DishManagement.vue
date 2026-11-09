<template>
  <div class="dish-management-container">
    <!-- 页面标题（§4.7 统一页头容器，admin 用紧凑版） -->
    <AppPageHeader title="菜品管理" subtitle="管理系统所有菜品" :compact="true" />

    <!-- 统计卡片（§4.8 线性图标 + 数值 + 标签，四卡一行） -->
    <div class="stats-section">
      <AppStatCard compact label="菜品总数" :value="stats.total" icon="Dish" />
      <AppStatCard compact label="已上架" :value="stats.active" icon="CircleCheck" />
      <AppStatCard compact label="已下架" :value="stats.inactive" icon="CircleClose" />
      <AppStatCard compact label="待审核" :value="stats.pending" icon="Clock" />
    </div>

    <!-- 搜索栏：关键词 + 分类 + 状态 + 搜索/重置 单行排布（§4.3 筛选条） -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索菜品名称、商家名称"
            clearable
            style="width: 250px"
          />
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select v-model="searchForm.category" placeholder="全部" clearable style="width: 140px">
            <el-option label="主食" value="主食" />
            <el-option label="菜品" value="菜品" />
            <el-option label="汤品" value="汤品" />
            <el-option label="甜品" value="甜品" />
            <el-option label="饮品" value="饮品" />
            <el-option label="小吃" value="小吃" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="已上架" value="ACTIVE" />
            <el-option label="已下架" value="INACTIVE" />
            <el-option label="待审核" value="PENDING" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 菜品列表 -->
    <el-card class="table-card" shadow="never">
      <AppEmptyState
        v-if="!loading && dishList.length === 0"
        compact
        icon="Dish"
        description="没有符合条件的菜品，换个关键词或清空筛选试试"
        action-text="重置筛选"
        @action="handleReset"
      />

      <template v-else>
        <el-table :data="dishList" v-loading="loading" stripe>
          <!-- ID 单行完整，不折行（§4.3、§5.4） -->
          <el-table-column prop="dishId" label="菜品ID" min-width="200" show-overflow-tooltip />
          <el-table-column prop="dishName" label="菜品名称" min-width="150" />
          <el-table-column prop="merchantName" label="商家名称" min-width="140" />
          <el-table-column prop="category" label="分类" width="90" />
          <!-- 价格：中性色 + 无衬线等宽数字，不带红绿蓝（§4.3） -->
          <el-table-column label="价格" width="90" align="right">
            <template #default="{ row }">
              <span class="amount">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <StatusTag :status="getDishStatusKey(row)" />
            </template>
          </el-table-column>
          <!-- 时间统一 YYYY-MM-DD HH:mm，去掉 ISO 的 T（§4.3） -->
          <el-table-column label="创建时间" width="150">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <RowActions
                :primary="{ text: '查看', onClick: () => handleView(row) }"
                :items="rowActions(row)"
                :max-visible="1"
              />
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页：空数据时不渲染（§4.3） -->
        <div v-if="dishList.length > 0" class="pagination-container">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchDishList"
            @current-change="fetchDishList"
          />
        </div>
      </template>
    </el-card>

    <!-- 菜品详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="菜品详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentDish" :column="2" border>
        <el-descriptions-item label="菜品ID">{{ currentDish.dishId }}</el-descriptions-item>
        <el-descriptions-item label="菜品名称">{{ currentDish.dishName }}</el-descriptions-item>
        <el-descriptions-item label="商家名称">{{ currentDish.merchantName }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentDish.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="价格">
          <span class="amount">¥{{ currentDish.price }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="getDishStatusKey(currentDish)" />
        </el-descriptions-item>
        <el-descriptions-item label="热量">{{ currentDish.calorie ?? '-' }} kcal</el-descriptions-item>
        <el-descriptions-item label="库存">{{ currentDish.stock || '不限量' }}</el-descriptions-item>
        <el-descriptions-item label="销量">{{ currentDish.orderCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="评分">-</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatTime(currentDish.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="菜品描述" :span="2">{{ currentDish.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="菜品图片" :span="2">
          <el-image
            v-if="currentDish.image"
            :src="currentDish.image"
            style="width: 100px; height: 100px"
            fit="cover"
            :preview-src-list="[currentDish.image]"
          />
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          :type="isCurrentDishActive ? 'warning' : 'success'"
          :loading="togglingId === currentDish?.dishId"
          @click="handleToggleStatus(currentDish)"
        >
          {{ isCurrentDishActive ? '下架' : '上架' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/common/AppPageHeader.vue'
import AppStatCard from '@/components/common/AppStatCard.vue'
import AppEmptyState from '@/components/common/AppEmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import RowActions from '@/components/common/RowActions.vue'
import { formatTime } from '@/utils/formatters'
import { getDishList, getDishDetail, updateDishStatus } from '@/api/admin'

const loading = ref(false)
const dishList = ref([])
const currentDish = ref(null)
const detailDialogVisible = ref(false)
// 正在切换状态的菜品 ID，用于行内 loading 与防重复点击（§4.6）
const togglingId = ref(null)

const stats = reactive({
  total: 0,
  active: 0,
  inactive: 0,
  pending: 0
})

const searchForm = reactive({
  keyword: '',
  category: '',
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 获取菜品列表
const fetchDishList = async () => {
  loading.value = true
  try {
    console.log('[菜品管理] 获取菜品列表, 页码:', pagination.page, '每页:', pagination.pageSize)
    const response = await getDishList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      category: searchForm.category,
      status: searchForm.status
    })

    if (response) {
      const records = response.records || []
      dishList.value = records
      pagination.total = response.total ?? 0

      // 更新统计数据
      stats.total = pagination.total
      stats.active = records.filter(d => d.status === 'ACTIVE' || d.statusCode === 'ACTIVE').length
      stats.inactive = records.filter(d => d.status === 'INACTIVE' || d.statusCode === 'INACTIVE').length
      stats.pending = records.filter(d => d.status === 'PENDING' || d.statusCode === 'PENDING').length
      console.log('[菜品管理] 获取菜品列表成功, 总数:', pagination.total)
    }
  } catch (error) {
    console.error('[菜品管理] 获取菜品列表失败:', error)
    ElMessage.error('获取菜品列表失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 状态词典键：交给 StatusTag 按 §3.1 状态表取色（已上架 info / 已下架 neutral / 待审核 warning）
const DISH_STATUS_KEYS = {
  'ACTIVE': 'onshelf',
  'INACTIVE': 'offline',
  'PENDING': 'pendingreview'
}

// 兼容 statusCode（字符串）与 status（布尔）两种字段形态
const getDishStatusCode = (row) => {
  if (!row) return ''
  if (row.statusCode) return row.statusCode
  if (typeof row.status === 'boolean') return row.status ? 'ACTIVE' : 'INACTIVE'
  return row.status || ''
}

const getDishStatusKey = (row) => DISH_STATUS_KEYS[getDishStatusCode(row)] || 'unknown'

// 判断是否为上架状态（支持Boolean和String两种类型）
const isActiveStatus = (status, statusCode) => {
  // 优先使用statusCode
  if (statusCode === 'ACTIVE') return true
  if (statusCode === 'INACTIVE') return false

  // 如果status是Boolean类型
  if (typeof status === 'boolean') {
    return status
  }

  // 如果status是String类型
  return status === 'ACTIVE'
}

// 当前弹窗菜品的上架态：优先 statusCode，兼容 status 布尔（复用 isActiveStatus）
const isCurrentDishActive = computed(() =>
  isActiveStatus(currentDish.value?.status, currentDish.value?.statusCode)
)

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchDishList()
}

// 重置
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = ''
  pagination.page = 1
  fetchDishList()
}

// 查看菜品详情
const handleView = async (row) => {
  try {
    console.log('[菜品管理] 查看菜品详情, 菜品ID:', row.dishId)
    const response = await getDishDetail(row.dishId)

    if (response.success || response.data) {
      currentDish.value = response.dish || response.data || response
      detailDialogVisible.value = true
      console.log('[菜品管理] 获取菜品详情成功')
    } else {
      ElMessage.error(response.message || '获取菜品详情失败')
    }
  } catch (error) {
    console.error('[菜品管理] 获取菜品详情失败:', error)
    ElMessage.error('获取菜品详情失败: ' + (error.message || '网络错误'))
  }
}

// 行操作：查看 + 更多（§4.3）
// 「下架」是点一下即生效的破坏性动作，进「更多」并由 RowActions 承担危险色与二次确认；
// 「上架」非破坏性，保持可见，确认仍由本页负责。
const rowActions = (row) => {
  const isActive = isActiveStatus(row.status, row.statusCode)
  return [
    isActive
      ? { text: '下架', danger: true, onClick: () => handleToggleStatus(row, { confirmed: true }) }
      : { text: '上架', onClick: () => handleToggleStatus(row) }
  ]
}

// 切换状态：二次确认 + loading + 结果反馈（§4.1、§4.6）
const handleToggleStatus = async (row, { confirmed = false } = {}) => {
  const isActive = isActiveStatus(row.status, row.statusCode)
  const actionText = isActive ? '下架' : '上架'

  try {
    if (!confirmed) {
      await ElMessageBox.confirm(`确定要${actionText}该菜品吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    }

    const newStatus = isActive ? 'INACTIVE' : 'ACTIVE'
    console.log('[菜品管理] 修改菜品状态, 菜品ID:', row.dishId, '新状态:', newStatus)

    togglingId.value = row.dishId
    const response = await updateDishStatus(row.dishId, newStatus)

    if (response.success) {
      ElMessage.success(`${actionText}成功`)
      detailDialogVisible.value = false
      await fetchDishList()
      console.log('[菜品管理] 修改菜品状态成功')
    } else {
      ElMessage.error(response.message || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('[菜品管理] 修改菜品状态失败:', error)
      ElMessage.error(`${actionText}失败: ` + (error.message || '网络错误'))
    }
  } finally {
    togglingId.value = null
  }
}

onMounted(() => {
  fetchDishList()
})
</script>

<style scoped lang="less">
.dish-management-container {
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

  /* 金额/价格：无衬线 + 等宽数字 + 中性色，不带红绿蓝（§4.3） */
  .amount {
    color: var(--ink-900);
    font-family: var(--font-num);
    font-variant-numeric: tabular-nums;
    font-weight: var(--weight-semibold);
  }
}

/* §4.3：admin 列表只保留单行 40px / 双行 ≤56px 两档，列宽不足由单元格省略而不是折行。 */
.dish-management-container :deep(.el-table__body .el-table__cell) {
  padding-top: 0;
  padding-bottom: 0;
}

.dish-management-container :deep(.el-table__body .el-table__row) {
  height: 40px;
}

/* §4.10：统计卡使用紧凑档后，收口卡片间距，保证 1440×900 下的首屏预算。 */
.dish-management-container .stats-section {
  margin-bottom: var(--space-3);
}

.dish-management-container .search-card {
  margin-bottom: var(--space-3);
}

.dish-management-container :deep(.search-card > .el-card__body) {
  padding: var(--space-2) var(--space-4);
}

.dish-management-container :deep(.table-card > .el-card__body) {
  padding-top: var(--space-2);
}
</style>
