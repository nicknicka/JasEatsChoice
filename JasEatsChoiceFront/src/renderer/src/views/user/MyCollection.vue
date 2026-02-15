<template>
  <div class="my-collection-container">
    <!-- 头部区域 -->
    <div class="header fade-in-up">
      <common-back-button />
      <div class="header-content">
        <h2>我的收藏</h2>
        <div class="collection-stats">
          <el-tag type="info" size="large"> 共 {{ filteredCollections.length }} 个收藏 </el-tag>
          <el-button
            type="primary"
            circle
            size="small"
            :icon="Refresh"
            :loading="loading"
            @click="refreshCollections"
            class="refresh-btn"
            title="刷新"
          />
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar fade-in-up delay-100">
      <div class="filter-left">
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索收藏名称..."
          class="search-input"
          clearable
          :prefix-icon="Search"
        />

        <!-- 类型筛选 -->
        <el-select
          v-model="filterType"
          placeholder="筛选类型"
          class="filter-select"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="all">
            <span class="option-label">
              <el-icon><Grid /></el-icon>
              全部
            </span>
          </el-option>
          <el-option label="商家" value="merchant">
            <span class="option-label">
              <el-icon><Shop /></el-icon>
              商家
            </span>
          </el-option>
          <el-option label="菜品" value="dish">
            <span class="option-label">
              <el-icon><Food /></el-icon>
              菜品
            </span>
          </el-option>
          <el-option label="文章" value="article">
            <span class="option-label">
              <el-icon><Document /></el-icon>
              文章
            </span>
          </el-option>
        </el-select>

        <!-- 排序选择 -->
        <el-select v-model="sortBy" placeholder="排序方式" class="sort-select" @change="handleSortChange">
          <el-option label="按时间" value="date" />
          <el-option label="按类型" value="type" />
          <el-option label="按名称" value="name" />
        </el-select>

        <el-button
          circle
          size="small"
          @click="toggleSortOrder"
          title="切换排序顺序"
        >
          <el-icon>
            <ArrowDown v-if="sortOrder === 'desc'" />
            <ArrowUp v-else />
          </el-icon>
        </el-button>

        <div class="filter-summary">
          <span v-if="filterType !== 'all' || searchKeyword" class="filter-active">
            已筛选:
            <el-tag v-if="filterType !== 'all'" size="small" closable @close="resetFilter">
              {{ getFilterTypeName(filterType) }}
            </el-tag>
            <el-tag v-if="searchKeyword" size="small" closable @click="searchKeyword = ''">
              搜索: {{ searchKeyword }}
            </el-tag>
          </span>
        </div>
      </div>

      <div class="filter-right">
        <el-button
          type="default"
          size="small"
          @click="resetFilter"
          :disabled="filterType === 'all' && !searchKeyword"
        >
          <el-icon><Refresh /></el-icon>
          重置筛选
        </el-button>

        <el-divider direction="vertical" />

        <el-button
          type="danger"
          size="small"
          @click="clearAll"
          :disabled="filteredCollections.length === 0"
        >
          <el-icon><Delete /></el-icon>
          清空全部
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 收藏列表 -->
    <div v-else-if="paginatedCollections.length > 0" class="collection-grid">
      <transition-group name="collection-fade">
        <div v-for="item in paginatedCollections" :key="item.id" class="collection-card-wrapper stagger-item card-hover-effect">
          <el-card class="collection-card" shadow="hover" @click="viewDetails(item)">
            <!-- 类型标签和删除按钮 -->
            <div class="card-header">
              <div class="item-type-badge" :class="`type-${item.type}`">
                <el-icon class="type-icon">
                  <component :is="getTypeIcon(item.type)" />
                </el-icon>
                <span>{{ getTypeName(item.type) }}</span>
              </div>
              <el-button
                type="danger"
                :icon="Delete"
                circle
                size="small"
                class="delete-btn"
                @click.stop="removeCollection(item.id)"
              />
            </div>

            <!-- 卡片内容 -->
            <div class="card-body">
              <!-- 图片区域 -->
              <div class="image-wrapper" :class="`gradient-${item.type}`">
                <!-- 无图片时显示图标+首字组合 -->
                <div v-if="!item.image || imageLoadErrors[item.id]" class="fallback-content">
                  <div class="type-icon-bg">
                    <el-icon class="type-fallback-icon">
                      <component :is="getTypeIcon(item.type)" />
                    </el-icon>
                  </div>
                  <div class="title-first-char">{{ item.title?.charAt(0) || '?' }}</div>
                </div>
                <!-- 有图片时显示图片 -->
                <img
                  v-if="item.image && !imageLoadErrors[item.id]"
                  :src="item.image"
                  :alt="item.title"
                  class="collection-image"
                  @error="handleImageError($event, item)"
                />
                <div class="image-overlay">
                  <el-icon class="view-icon"><View /></el-icon>
                  <span>点击查看</span>
                </div>
              </div>

              <!-- 标题和描述 -->
              <h3 class="collection-title" :title="item.title">{{ item.title }}</h3>
              <p class="collection-description">{{ item.description }}</p>

              <!-- 元信息 -->
              <div class="collection-meta">
                <div class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  <span>收藏于 {{ item.date }}</span>
                </div>
              </div>
            </div>

            <!-- 卡片底部操作栏 -->
            <div class="card-footer">
              <el-button
                type="primary"
                size="small"
                class="view-btn"
                @click.stop="viewDetails(item)"
              >
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
            </div>
          </el-card>
        </div>
      </transition-group>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-content">
        <el-icon class="empty-icon"><Star /></el-icon>
        <h3 class="empty-title">{{ getEmptyTitle() }}</h3>
        <p class="empty-description">{{ getEmptyDescription() }}</p>
        <div class="empty-actions">
          <el-button type="primary" @click="goToHome">
            <el-icon><House /></el-icon>
            去首页看看
          </el-button>
          <el-button v-if="filterType !== 'all'" @click="resetFilter">
            <el-icon><RefreshLeft /></el-icon>
            查看全部收藏
          </el-button>
        </div>
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
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import DishDetailDialog from '../../components/dish/DishDetailDialog.vue'
import api from '../../utils/api'

const router = useRouter()

// 加载状态
const loading = ref(true)

// 默认图片 - 使用更美观的设计
const defaultImage =
  'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjE1MCIgZmlsbD0iI0Y1RjVGNSIvPjxjaXJjbGUgY3g9IjEwMCIgY3k9Ijc1IiByPSIzMCIgZmlsbD0iI0U1RUFFNSIvPjxwYXRoIGQ9Ik0xMDAgNDVMMTMwIDc1SDEwMEw3MCA3NVoiIGZpbGw9IiNDQ0MiIvPjxwYXRoIGQ9Ik04NSA2NUwxMTUgMzVMMTAwIDMwTDg1IDM1WiIgZmlsbD0iIzk2QTIyMiIvPjx0ZXh0IHg9IjEwMCIgeT0iMTMwIiBmb250LXNpemU9IjE0IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjOTk5Ij7lpKnnvaHmmL7lvIM8L3RleHQ+PC9zdmc+'

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
const removeCollection = (id) => {
  // 找到要删除的收藏项
  const collectionIndex = collections.value.findIndex((item) => item.id === id)
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
        // 使用 RESTful 风格的 API 调用
        const response = await api.delete(`/v1/collections/${id}`)

        if (response.code === '200') {
          // 从本地数组中删除该收藏项
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
      router.push({ path: '/user/home/merchant-detail', query: { id: item.collectableId } })
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
            type: item.collectableType,
            date: formatDate(item.createTime),
            title: `${typeName} ${item.collectableId}`,
            description: '信息加载失败',
            image: item.collectableType === 'article' ? null : defaultImage,
            firstChar: item.collectableType === 'article' ? typeName.charAt(0) : undefined
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
  const baseCollection = {
    id: item.id,
    collectableId: item.collectableId,
    type: item.collectableType,
    date: formatDate(item.createTime)
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
          image: merchant.image || defaultImage
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
          image: dish.image || defaultImage,
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
    } else if (item.collectableType === 'article') {
      const articleTitle = `文章 ${item.collectableId}`
      return {
        ...baseCollection,
        title: articleTitle,
        description: '文章收藏功能正在开发中',
        image: null,
        firstChar: articleTitle.charAt(0)
      }
    }
  } catch (error) {
    console.warn(`加载${item.collectableType}详情失败 (ID: ${item.collectableId}):`, error.message)
  }

  // 返回默认值（即使失败也显示基本信息）
  const typeName = item.collectableType === 'merchant' ? '商家' : item.collectableType === 'dish' ? '菜品' : '文章'
  const defaultTitle = `${typeName} ${item.collectableId}`
  const isArticle = item.collectableType === 'article'

  return {
    ...baseCollection,
    title: defaultTitle,
    description: `这是${typeName}的描述`,
    image: isArticle ? null : defaultImage,
    firstChar: isArticle ? defaultTitle.charAt(0) : undefined
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
            type: item.collectableType,
            date: formatDate(item.createTime),
            title: `${typeName} ${item.collectableId}`,
            description: '信息加载失败',
            image: item.collectableType === 'article' ? null : defaultImage,
            firstChar: item.collectableType === 'article' ? typeName.charAt(0) : undefined
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

<style scoped lang="less">
.my-collection-container {
  padding: 0 20px 20px 20px;
  min-height: calc(100vh - 40px);
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);

  .header {
    display: flex;
    align-items: center;
    margin-bottom: 24px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);

    .header-content {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-left: 20px;

      h2 {
        font-size: 28px;
        margin: 0;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        font-weight: 700;
      }

      .collection-stats {
        display: flex;
        align-items: center;
        gap: 12px;

        :deep(.el-tag) {
          font-size: 14px;
          padding: 8px 16px;
          font-weight: 500;
        }

        .refresh-btn {
          transition: all 0.3s ease;

          &:hover {
            transform: rotate(180deg);
          }
        }
      }
    }
  }

  .filter-bar {
    margin-bottom: 24px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 16px;

    .filter-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
      min-width: 300px;
      flex-wrap: wrap;

      .search-input {
        width: 240px;
        min-width: 200px;

        :deep(.el-input__wrapper) {
          border-radius: 8px;
        }
      }

      .filter-select {
        width: 160px;

        :deep(.el-input__wrapper) {
          border-radius: 8px;
        }

        .option-label {
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            font-size: 16px;
          }
        }
      }

      .sort-select {
        width: 140px;

        :deep(.el-input__wrapper) {
          border-radius: 8px;
        }
      }

      .filter-summary {
        .filter-active {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: #606266;
          flex-wrap: wrap;
        }
      }
    }

    .filter-right {
      display: flex;
      align-items: center;
      gap: 12px;

      :deep(.el-divider--vertical) {
        height: 24px;
        margin: 0;
      }
    }
  }

  .loading-state {
    padding: 40px 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }

  .collection-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 24px;
    margin-bottom: 24px;

    // 列表动画
    .collection-fade-enter-active,
    .collection-fade-leave-active {
      transition: all 0.3s ease;
    }

    .collection-fade-enter-from {
      opacity: 0;
      transform: translateY(20px);
    }

    .collection-fade-leave-to {
      opacity: 0;
      transform: scale(0.9);
    }

    .collection-card-wrapper {
      height: 100%;

      .collection-card {
        height: 100%;
        border-radius: 16px;
        border: none;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        cursor: pointer;
        overflow: hidden;

        &:hover {
          transform: translateY(-8px);
          box-shadow: 0 12px 40px rgba(102, 126, 234, 0.25);
        }

        :deep(.el-card__body) {
          padding: 0;
          height: 100%;
          display: flex;
          flex-direction: column;
        }

        .card-header {
          padding: 16px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
          border-bottom: 1px solid rgba(0, 0, 0, 0.05);

          .item-type-badge {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 6px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
            color: white;
            transition: all 0.3s ease;

            .type-icon {
              font-size: 16px;
            }

            &.type-merchant {
              background: linear-gradient(135deg, #4caf50 0%, #45a049 100%);
            }

            &.type-dish {
              background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
            }

            &.type-article {
              background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
            }
          }

          .delete-btn {
            opacity: 0;
            transition: all 0.3s ease;

            &:hover {
              transform: rotate(90deg) scale(1.1);
            }
          }
        }

        &:hover .delete-btn {
          opacity: 1;
        }

        // 移动端优化：始终显示删除按钮
        @media (max-width: 768px) {
          .delete-btn {
            opacity: 1;
          }
        }

        .card-body {
          flex: 1;
          padding: 16px;
          display: flex;
          flex-direction: column;

          .image-wrapper {
            position: relative;
            width: 100%;
            height: 200px;
            border-radius: 12px;
            overflow: hidden;
            margin-bottom: 16px;

            // 类型渐变背景
            &.gradient-merchant {
              background: linear-gradient(135deg, #4caf50 0%, #81c784 100%);
            }

            &.gradient-dish {
              background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
            }

            &.gradient-article {
              background: linear-gradient(135deg, #2196f3 0%, #64b5f6 100%);
            }

            // 备用内容（图标+首字）
            .fallback-content {
              width: 100%;
              height: 100%;
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              gap: 16px;
              color: white;
              position: relative;

              .type-icon-bg {
                width: 80px;
                height: 80px;
                border-radius: 50%;
                background: rgba(255, 255, 255, 0.2);
                backdrop-filter: blur(10px);
                display: flex;
              align-items: center;
              justify-content: center;
                box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
                transition: transform 0.3s ease;

                .type-fallback-icon {
                  font-size: 40px;
                }
              }

              .title-first-char {
                font-size: 72px;
                font-weight: 700;
                text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
                line-height: 1;
                margin: 0;
              }
            }

            .collection-image {
              width: 100%;
              height: 100%;
              object-fit: cover;
              transition: transform 0.3s ease;
            }

            .image-overlay {
              position: absolute;
              top: 0;
              left: 0;
              width: 100%;
              height: 100%;
              background: rgba(0, 0, 0, 0.5);
              display: flex;
              flex-direction: column;
              align-items: center;
              justify-content: center;
              gap: 8px;
              color: white;
              opacity: 0;
              transition: opacity 0.3s ease;

              .view-icon {
                font-size: 32px;
              }

              span {
                font-size: 14px;
                font-weight: 500;
              }
            }
          }

          &:hover {
            .collection-image {
              transform: scale(1.1);
            }

            .fallback-content {
              .type-icon-bg {
                transform: scale(1.1) rotate(5deg);
              }

              .title-first-char {
                transform: scale(1.05);
              }
            }

            .image-overlay {
              opacity: 1;
            }
          }

          .collection-title {
            font-size: 18px;
            font-weight: 700;
            margin: 0 0 12px 0;
            color: #2c3e50;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .collection-description {
            font-size: 14px;
            color: #666;
            margin: 0 0 16px 0;
            line-height: 1.6;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            min-height: 42px;
          }

          .collection-meta {
            margin-top: auto;

            .meta-item {
              display: flex;
              align-items: center;
              gap: 6px;
              font-size: 13px;
              color: #999;
              padding: 8px 12px;
              background: #f8f9fa;
              border-radius: 8px;

              .el-icon {
                font-size: 14px;
              }
            }
          }
        }

        .card-footer {
          padding: 16px;
          border-top: 1px solid rgba(0, 0, 0, 0.05);
          background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);

          .view-btn {
            width: 100%;
            border-radius: 8px;
            font-weight: 500;
            transition: all 0.3s ease;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
            }
          }
        }
      }
    }
  }

  .empty-state {
    padding: 80px 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    text-align: center;

    .empty-content {
      .empty-icon {
        font-size: 120px;
        color: #ddd;
        margin-bottom: 24px;
        animation: float 3s ease-in-out infinite;
      }

      .empty-title {
        font-size: 24px;
        font-weight: 600;
        color: #2c3e50;
        margin: 0 0 12px 0;
      }

      .empty-description {
        font-size: 16px;
        color: #999;
        margin: 0 0 32px 0;
      }

      .empty-actions {
        display: flex;
        justify-content: center;
        gap: 16px;
      }
    }
  }

  .pagination-container {
    padding: 24px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    display: flex;
    justify-content: center;

    :deep(.el-pagination) {
      .btn-prev,
      .btn-next,
      .el-pager li {
        border-radius: 8px;
        font-weight: 500;
      }

      .el-pager li.is-active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }
    }
  }
}

// 浮动动画
@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .my-collection-container {
    padding: 0 10px 10px 10px;

    .header {
      padding: 16px;

      .header-content {
        margin-left: 12px;
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;

        h2 {
          font-size: 22px;
        }
      }
    }

    .filter-bar {
      padding: 16px;
      flex-direction: column;
      align-items: stretch;

      .filter-left {
        flex-direction: column;
        align-items: stretch;
        min-width: auto;

        .search-input {
          width: 100%;
        }

        .filter-select,
        .sort-select {
          width: 100%;
        }
      }

      .filter-right {
        flex-direction: column;
        gap: 8px;

        .el-button {
          width: 100%;
        }
      }
    }

    .collection-grid {
      grid-template-columns: 1fr;
      gap: 16px;
    }

    .empty-state {
      padding: 40px 16px;

      .empty-content {
        .empty-icon {
          font-size: 80px;
        }

        .empty-title {
          font-size: 20px;
        }

        .empty-description {
          font-size: 14px;
        }

        .empty-actions {
          flex-direction: column;
          gap: 12px;

          .el-button {
            width: 100%;
          }
        }
      }
    }
  }
}
</style>
