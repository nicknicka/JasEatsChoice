<template>
  <div class="my-collection-page">
    <!-- 页头：标准版标题 --text-h1 + 上下 --space-6；返回统一收进右侧动作区（§4.7） -->
    <AppPageHeader title="我的收藏" class="collection-header">
      <template #actions>
        <CommonBackButton type="default" size="default" />
      </template>
    </AppPageHeader>

    <!-- 筛选工具栏：单行紧凑；收藏为空时整体收起（§4.5） -->
    <div v-if="collections.length > 0" class="toolbar">
      <div class="toolbar__filters">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索收藏名称..."
          class="toolbar__search"
          clearable
          :prefix-icon="Search"
        />

        <el-select
          v-model="filterType"
          placeholder="筛选类型"
          class="toolbar__select"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="all">
            <span class="option-label"><el-icon><Grid /></el-icon>全部</span>
          </el-option>
          <el-option label="商家" value="merchant">
            <span class="option-label"><el-icon><Shop /></el-icon>商家</span>
          </el-option>
          <el-option label="菜品" value="dish">
            <span class="option-label"><el-icon><Food /></el-icon>菜品</span>
          </el-option>
          <el-option label="文章" value="article">
            <span class="option-label"><el-icon><Document /></el-icon>文章</span>
          </el-option>
        </el-select>

        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          class="toolbar__select"
          @change="handleSortChange"
        >
          <el-option label="按时间" value="date" />
          <el-option label="按类型" value="type" />
          <el-option label="按名称" value="name" />
        </el-select>

        <el-button
          circle
          :icon="sortOrder === 'desc' ? ArrowDown : ArrowUp"
          title="切换排序顺序"
          @click="toggleSortOrder"
        />
        <el-button
          circle
          :icon="Refresh"
          :loading="loading"
          title="刷新"
          @click="refreshCollections"
        />
      </div>

      <div class="toolbar__actions">
        <span class="collection-stats">共 {{ filteredCollections.length }} 个收藏</span>
        <el-button
          :icon="RefreshLeft"
          :disabled="filterType === 'all' && !searchKeyword"
          @click="resetFilter"
        >
          重置筛选
        </el-button>
        <el-button
          type="danger"
          plain
          :icon="Delete"
          :disabled="filteredCollections.length === 0"
          @click="clearAll"
        >
          清空全部
        </el-button>
      </div>
    </div>

    <!-- 首屏用骨架，刷新用遮罩，不整页白屏（§4.6） -->
    <div v-if="loading && collections.length === 0" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 收藏列表：同列卡片高度与图片比例一致（§4.2） -->
    <div v-else-if="paginatedCollections.length > 0" v-loading="loading" class="collection-grid">
      <transition-group name="collection-fade">
        <article
          v-for="item in paginatedCollections"
          :key="item.id"
          class="collection-card"
          @click="viewDetails(item)"
        >
          <div class="collection-card__media">
            <img
              v-if="item.image && !imageLoadErrors[item.id]"
              :src="item.image"
              :alt="item.title"
              class="collection-image"
              @error="handleImageError($event, item)"
            />
            <div v-else class="media-fallback">
              <el-icon class="media-fallback__icon">
                <component :is="getTypeIcon(item.type)" />
              </el-icon>
              <span class="media-fallback__char">{{ (item.title || '?').charAt(0) }}</span>
            </div>
            <span class="type-chip">
              <el-icon><component :is="getTypeIcon(item.type)" /></el-icon>
              {{ getTypeName(item.type) }}
            </span>
          </div>

          <div class="collection-card__body">
            <h3 class="collection-title" :title="item.title">{{ item.title }}</h3>
            <p class="collection-description">{{ item.description }}</p>
            <div class="collection-meta">
              <el-icon><Calendar /></el-icon>
              <span>收藏于 {{ item.date }}</span>
            </div>
          </div>

          <div class="collection-card__footer">
            <el-button link class="view-btn" @click.stop="viewDetails(item)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
            <el-button
              link
              type="danger"
              :icon="Delete"
              class="delete-btn"
              title="删除收藏"
              aria-label="删除收藏"
              @click.stop="removeCollection(item)"
            />
          </div>
        </article>
      </transition-group>
    </div>

    <!-- 空状态：图标 + 中文说明 + 替代动作（§4.5） -->
    <div v-else class="empty-state">
      <el-icon class="empty-icon"><Star /></el-icon>
      <h3 class="empty-title">{{ getEmptyTitle() }}</h3>
      <p class="empty-description">{{ getEmptyDescription() }}</p>
      <div class="empty-actions">
        <el-button type="primary" :icon="House" @click="goToHome">去首页看看</el-button>
        <el-button v-if="filterType !== 'all'" :icon="RefreshLeft" @click="resetFilter">
          查看全部收藏
        </el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="!loading && filteredCollections.length > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[9, 18, 36]"
        :total="filteredCollections.length"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 菜品详情弹窗 -->
    <DishDetailDialog v-model="dishDialogVisible" :dish-data="selectedDish" />
  </div>
</template>
<script setup>
import pinia from '../../store'
import { useAuthStore } from '../../store/authStore'

const authStore = useAuthStore(pinia)

import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Refresh,
  Delete,
  Calendar,
  View,
  Star,
  House,
  Grid,
  Shop,
  Food,
  Document,
  RefreshLeft,
  Search,
  ArrowDown,
  ArrowUp
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import DishDetailDialog from '../../components/dish/DishDetailDialog.vue'
import api from '../../utils/api'

const router = useRouter()

// 加载状态
const loading = ref(true)

// 收藏数据
const collections = ref([])

// 筛选条件
const filterType = ref('all')

// 搜索关键词
const searchKeyword = ref('')

// 排序方式
const sortBy = ref('date') // date: 时间, type: 类型, name: 名称
const sortOrder = ref('desc') // desc: 降序, asc: 升序

// 分页
const currentPage = ref(1)
const pageSize = ref(9)

// 菜品详情弹窗
const dishDialogVisible = ref(false)
const selectedDish = ref(null)

// 图片加载错误状态
const imageLoadErrors = ref({})

// 类型配置映射
const TYPE_CONFIG = {
  merchant: { name: '商家', icon: Shop, color: 'green', value: 1 },
  dish: { name: '菜品', icon: Food, color: 'orange', value: 2 },
  article: { name: '文章', icon: Document, color: 'blue', value: 3 }
}

// 获取类型配置
const getTypeConfig = (type) => TYPE_CONFIG[type] || TYPE_CONFIG.merchant

// 计算过滤和排序后的收藏
const filteredCollections = computed(() => {
  let filtered = [...collections.value]

  // 类型过滤
  if (filterType.value !== 'all') {
    filtered = filtered.filter((item) => item.type === filterType.value)
  }

  // 搜索过滤
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    filtered = filtered.filter((item) =>
      item.title.toLowerCase().includes(keyword) ||
      item.description.toLowerCase().includes(keyword)
    )
  }

  // 排序
  filtered.sort((a, b) => {
    let compareA, compareB

    switch (sortBy.value) {
      case 'date':
        compareA = new Date(a.date).getTime()
        compareB = new Date(b.date).getTime()
        break
      case 'type':
        compareA = getTypeConfig(a.type).value
        compareB = getTypeConfig(b.type).value
        break
      case 'name':
        compareA = a.title.toLowerCase()
        compareB = b.title.toLowerCase()
        break
      default:
        return 0
    }

    if (sortOrder.value === 'asc') {
      return compareA > compareB ? 1 : -1
    } else {
      return compareA < compareB ? 1 : -1
    }
  })

  return filtered
})

// 分页数据
const paginatedCollections = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCollections.value.slice(start, end)
})

// 获取类型图标（保持兼容性）
const getTypeIcon = (type) => getTypeConfig(type).icon

// 获取类型名称（保持兼容性）
const getTypeName = (type) => getTypeConfig(type).name

// 获取筛选类型名称
const getFilterTypeName = (type) => getTypeName(type)

// 获取空状态标题
const getEmptyTitle = () => {
  if (filterType.value !== 'all') {
    return `没有找到${getFilterTypeName(filterType.value)}收藏`
  }
  return '还没有收藏任何内容'
}

// 获取空状态描述
const getEmptyDescription = () => {
  if (filterType.value !== 'all') {
    return `您还没有收藏任何${getFilterTypeName(filterType.value)}，去首页看看吧`
  }
  return '收藏喜欢的商家和菜品，随时查看'
}

// 处理图片加载错误
const handleImageError = (_event, item) => {
  imageLoadErrors.value[item.id] = true
}

// 筛选变化处理
const handleFilterChange = () => {
  currentPage.value = 1
}

// 排序变化处理
const handleSortChange = () => {
  currentPage.value = 1
}

// 切换排序顺序
const toggleSortOrder = () => {
  sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  currentPage.value = 1
}

// 重置筛选
const resetFilter = () => {
  filterType.value = 'all'
  searchKeyword.value = ''
  sortBy.value = 'date'
  sortOrder.value = 'desc'
  currentPage.value = 1
  ElMessage.info('筛选条件已重置')
}

// 移除收藏
const removeCollection = (item) => {
  // 找到要删除的收藏项
  const collectionIndex = collections.value.findIndex((entry) => entry.id === item.id)
  if (collectionIndex === -1) {
    ElMessage.error('未找到该收藏项')
    return
  }

  ElMessageBox.confirm('确定要删除该收藏吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const response = await api.delete('/v1/collections', {
          params: {
            userId: String(authStore.userId || '1'),
            type: item.type,
            id: item.collectableId
          }
        })

        if (response.code === '200') {
          const query = await api.get('/v1/collections', { params: { userId: String(authStore.userId || '1') } })
          const rawCollections = Array.isArray(query.data?.data)
            ? query.data.data
            : Array.isArray(query.data)
            ? query.data
            : []
          const stillExists = rawCollections.some(
            (entry) => String(entry.id) === String(item.id)
          )

          if (stillExists) {
            ElMessage.error('删除失败：取消收藏后仍查询到该记录')
            return
          }

          collections.value.splice(collectionIndex, 1)
          ElMessage.success('收藏已删除')
        } else {
          ElMessage.error('删除失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('删除收藏失败:', error)
        ElMessage.error('删除失败，请稍后重试')
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 查看详情
const viewDetails = (item) => {
  switch (item.type) {
    case 'merchant':
      // 跳转到商家详情页
      router.push({
        path: '/user/home/merchant-detail',
        query: { merchantId: item.collectableId, viewMode: 'detail' }
      })
      break
    case 'dish':
      // 显示菜品详情弹窗
      selectedDish.value = item.dishData
      dishDialogVisible.value = true
      break
    case 'article':
      // 跳转到文章/教程详情页
      router.push({
        path: '/user/home/tutorial-detail',
        query: { id: item.collectableId }
      })
      break
    default:
      ElMessage.info('未知收藏类型')
  }
}

// 关闭菜品详情弹窗
const closeDishDialog = () => {
  dishDialogVisible.value = false
  selectedDish.value = null
}

// 清空全部
const clearAll = () => {
  ElMessageBox.confirm('确定要清空所有收藏吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'danger'
  })
    .then(async () => {
      try {
        const userId = String(authStore.userId || 1)

        // 调用后端API清空所有收藏
        const response = await api.delete(`/v1/collections/user/${userId}`)

        if (response.code === '200') {
          // 清空本地数据
          collections.value = []
          ElMessage.success('所有收藏已清空')
        } else {
          ElMessage.error('清空收藏失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        console.error('清空收藏失败:', error)
        ElMessage.error('清空收藏失败，请稍后重试')
      }
    })
    .catch(() => {
      ElMessage.info('已取消清空')
    })
}

// 刷新收藏列表
const refreshCollections = async () => {
  if (loading.value) return

  loading.value = true
  try {
    const userId = String(authStore.userId || '1')
    const response = await api.get('/v1/collections', { params: { userId } })

    console.log('刷新收藏API响应:', response)

    if (response.code === '200') {
      // 兼容不同的数据结构：response.data.data 或 response.data
      const rawCollections = Array.isArray(response.data?.data)
        ? response.data.data
        : Array.isArray(response.data)
        ? response.data
        : []

      if (rawCollections.length === 0) {
        collections.value = []
        ElMessage.success('刷新成功，暂无收藏')
        return
      }

      const detailPromises = rawCollections.map(item => fetchCollectionItemDetails(item))
      const results = await Promise.allSettled(detailPromises)

      const processedCollections = results.map((result, index) => {
        if (result.status === 'fulfilled') {
          return result.value
        } else {
          const item = rawCollections[index]
          const typeName = item.collectableType === 'merchant' ? '商家' : item.collectableType === 'dish' ? '菜品' : '文章'
          return {
            id: item.id,
            collectableId: item.collectableId,
            type: item.collectableType === 'recipe' ? 'article' : item.collectableType,
            date: formatDate(item.createdAt),
            title: typeName,
            description: '信息加载失败',
            image: null
          }
        }
      })

      collections.value = processedCollections
      ElMessage.success('刷新成功')
    } else {
      ElMessage.error('刷新失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('刷新收藏失败:', error)
    ElMessage.error('刷新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 去首页
const goToHome = () => {
  router.push('/user/home')
}

// 页面大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

// 页面变化
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 辅助函数：格式化日期
const formatDate = (dateString) => {
  if (!dateString) return new Date().toISOString().split('T')[0]
  const date = new Date(dateString)
  if (isNaN(date.getTime())) return new Date().toISOString().split('T')[0]
  return date.toISOString().split('T')[0]
}

// 辅助函数：获取收藏项详情（优化版）
const fetchCollectionItemDetails = async (item) => {
  // 后端 collectable_type 为 merchant/dish/article/recipe；页面把 recipe（教程）并入「文章」展示
  const normalizedType = item.collectableType === 'recipe' ? 'article' : item.collectableType
  const baseCollection = {
    id: item.id,
    collectableId: item.collectableId,
    type: normalizedType,
    date: formatDate(item.createdAt)
  }

  try {
    if (item.collectableType === 'merchant') {
      const merchantRes = await api.get(`/v1/merchant/${item.collectableId}`)
      if (merchantRes.code === '200' && merchantRes.data) {
        const merchant = merchantRes.data
        return {
          ...baseCollection,
          title: merchant.name || '未知商家',
          description: merchant.description || `这是${merchant.name || '商家'}的简介`,
          image: merchant.image || null
        }
      }
    } else if (item.collectableType === 'dish') {
      const dishRes = await api.get(`/v1/dishes/${item.collectableId}`)
      if (dishRes.code === '200' && dishRes.data) {
        const dish = dishRes.data
        return {
          ...baseCollection,
          title: dish.name || '未知菜品',
          description: dish.description || `这是${dish.name || '菜品'}的简介`,
          image: dish.image || null,
          dishData: {
            id: dish.id,
            name: dish.name,
            price: dish.price || 0,
            description: dish.description,
            category: dish.category,
            image: dish.image
          }
        }
      }
    } else if (normalizedType === 'article') {
      // 文章没有可用的标题字段，用中性文案代替原始 ID（issue 318 P0）
      return {
        ...baseCollection,
        title: '收藏的文章',
        description: '文章收藏功能正在开发中',
        image: null
      }
    }
  } catch (error) {
    console.warn(`加载${item.collectableType}详情失败 (ID: ${item.collectableId}):`, error.message)
  }

  // 详情接口失败时用类型名兜底，不再把原始 ID 当标题（issue 318 P0）
  const typeName = item.collectableType === 'merchant' ? '商家' : item.collectableType === 'dish' ? '菜品' : '文章'

  return {
    ...baseCollection,
    title: typeName,
    description: '详情加载失败，请稍后重试',
    image: null
  }
}

// 从后端加载收藏数据（优化版：减少 N+1 查询影响）
onMounted(async () => {
  loading.value = true
  try {
    const userId = String(authStore.userId || '1')

    // 获取收藏列表
    const response = await api.get('/v1/collections', {
      params: { userId }
    })

    console.log('收藏API响应:', response)

    if (response.code === '200') {
      // 兼容不同的数据结构：response.data.data 或 response.data
      const rawCollections = Array.isArray(response.data?.data)
        ? response.data.data
        : Array.isArray(response.data)
        ? response.data
        : []

      if (rawCollections.length === 0) {
        collections.value = []
        console.log('暂无收藏数据')
        return
      }

      // 使用 Promise.allSettled 确保部分失败不影响整体
      // 这样即使某些详情加载失败，也能显示基本信息
      const detailPromises = rawCollections.map(item => fetchCollectionItemDetails(item))
      const results = await Promise.allSettled(detailPromises)

      // 处理结果：只保留成功的，或者失败的也用默认值
      const processedCollections = results.map((result, index) => {
        if (result.status === 'fulfilled') {
          return result.value
        } else {
          // 即使 Promise 失败，也返回基本信息
          const item = rawCollections[index]
          const typeName = item.collectableType === 'merchant' ? '商家' : item.collectableType === 'dish' ? '菜品' : '文章'
          return {
            id: item.id,
            collectableId: item.collectableId,
            type: item.collectableType === 'recipe' ? 'article' : item.collectableType,
            date: formatDate(item.createdAt),
            title: typeName,
            description: '信息加载失败',
            image: null
          }
        }
      })

      collections.value = processedCollections
      console.log(`成功加载 ${processedCollections.length} 个收藏项`)
    } else {
      ElMessage.error('加载收藏数据失败：' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('加载收藏数据失败:', error)
    ElMessage.error('加载收藏数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* 页面颜色、间距、圆角、字阶与动效全部取自 tokens.css（§3、§7.1），无裸色值。 */
.my-collection-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-12);
  background: var(--canvas);
}

/* 页头动作区控件统一 40px（§3.4、§4.7）。 */
.collection-header :deep(.el-button) {
  height: var(--control-height-md);
}

/* 筛选工具栏：单行紧凑，次级/危险动作靠右（§3.4、§4.1）。 */
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
}

.toolbar__filters,
.toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.toolbar__search {
  width: 240px;
}

.toolbar__select {
  width: 148px;
}

/* 工具条控件高度只用 --control-height-sm（§3.4）。 */
.toolbar :deep(.el-input__wrapper),
.toolbar :deep(.el-select__wrapper) {
  min-height: var(--control-height-sm);
  border-radius: var(--radius-md);
}

.toolbar :deep(.el-button) {
  height: var(--control-height-sm);
}

.toolbar :deep(.el-button.is-circle) {
  width: var(--control-height-sm);
  padding: 0;
}

/* 危险动作与普通动作之间留出 --space-6（§4.1）。 */
.toolbar__actions :deep(.el-button--danger) {
  margin-left: var(--space-3);
}

.option-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}

/* 收藏总数：中性辅助文字（§3.2）。 */
.collection-stats {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
}

.loading-state {
  padding: var(--space-6);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
}

/* 三列卡片流，间距取 --card-gap（§3.3）。 */
.collection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--card-gap);
  margin-bottom: var(--space-6);
}

/* 收藏卡：L2 浮起质感，可点击（§3.4、§4.2）。 */
.collection-card {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition:
    box-shadow var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);
}

.collection-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}

/* 图片统一 4:3 裁切，同列表内比例一致（§4.2）。 */
.collection-card__media {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: var(--canvas-sunken);
}

.collection-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--duration-base) var(--ease-out);
}

.collection-card:hover .collection-image {
  transform: scale(1.03);
}

/* 无图/加载失败：中性底 + 线性图标 + 首字，不出现占位文字（§4.2）。 */
.media-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  width: 100%;
  height: 100%;
  color: var(--ink-400);
}

.media-fallback__icon {
  font-size: 32px;
}

.media-fallback__char {
  color: var(--ink-500);
  font-size: var(--text-h2);
  font-weight: var(--weight-semibold);
}

/* 类型标签：分类只用中性标签表达，不用语义色（§3.1、§4.2）。 */
.type-chip {
  position: absolute;
  top: var(--space-3);
  left: var(--space-3);
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 2px var(--space-2);
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  background: var(--surface);
  color: var(--ink-700);
  font-size: var(--text-caption);
  line-height: 18px;
}

.collection-card__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--card-padding);
}

.collection-title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 描述固定两行高度，保证同列卡片等高（§4.2）。 */
.collection-description {
  display: -webkit-box;
  margin: 0;
  min-height: 40px;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.collection-meta {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: auto;
  color: var(--ink-400);
  font-size: var(--text-caption);
}

.collection-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-4);
  border-top: 1px solid var(--line);
}

/* 卡片次级动作统一 32px（§3.4）。 */
.collection-card__footer :deep(.el-button) {
  height: var(--control-height-sm);
}

/* 空状态：图标 + 中文说明 + 替代动作（§4.5）。 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  max-width: 360px;
  margin: var(--space-10) auto 0;
  padding: var(--space-8) var(--space-6);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  text-align: center;
}

.empty-icon {
  color: var(--ink-400);
  font-size: 40px;
}

.empty-title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
}

.empty-description {
  margin: 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.empty-actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-2);
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
}

.pagination-container :deep(.el-pager li.is-active) {
  background: var(--brand-500);
  color: var(--surface);
}

/* 列表进入/退出过渡，时长与曲线取 §3.5。 */
.collection-fade-enter-active {
  transition:
    opacity var(--duration-base) var(--ease-out),
    transform var(--duration-base) var(--ease-out);
}

.collection-fade-leave-active {
  transition:
    opacity var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);
}

.collection-fade-enter-from {
  opacity: 0;
  transform: translateY(4px);
}

.collection-fade-leave-to {
  opacity: 0;
}

@media (max-width: 960px) {
  .toolbar__filters,
  .toolbar__actions {
    width: 100%;
  }

  .toolbar__search {
    flex: 1;
    width: auto;
    min-width: 200px;
  }
}

@media (max-width: 640px) {
  .my-collection-page {
    padding: 0 var(--space-4) var(--space-8);
  }

  .collection-grid {
    grid-template-columns: 1fr;
  }

  .toolbar__search,
  .toolbar__select {
    width: 100%;
  }

  .toolbar__actions {
    justify-content: space-between;
  }
}
</style>
