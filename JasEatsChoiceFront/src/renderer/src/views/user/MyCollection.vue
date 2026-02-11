<template>
  <div class="my-collection-container">
    <!-- 头部区域 -->
    <div class="header fade-in-up">
      <common-back-button />
      <div class="header-content">
        <h2>我的收藏</h2>
        <div class="collection-stats">
          <el-tag type="info" size="large"> 共 {{ filteredCollections.length }} 个收藏 </el-tag>
        </div>
      </div>
    </div>

    <!-- 筛选工具栏 -->
    <div class="filter-bar fade-in-up delay-100">
      <div class="filter-left">
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

        <div class="filter-summary">
          <span v-if="filterType !== 'all'" class="filter-active">
            已筛选:
            <el-tag size="small" closable @close="resetFilter">
              {{ getFilterTypeName(filterType) }}
            </el-tag>
          </span>
        </div>
      </div>

      <div class="filter-right">
        <el-button-group>
          <el-button type="default" size="small" @click="applyFilter">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button
            type="default"
            size="small"
            @click="resetFilter"
            :disabled="filterType === 'all'"
          >
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-button-group>

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
              <div class="image-wrapper">
                <!-- 文章类型显示首字母 -->
                <div
                  v-if="item.type === 'article' && !item.image && item.firstChar"
                  class="article-first-char"
                >
                  {{ item.firstChar }}
                </div>
                <!-- 其他类型显示图片 -->
                <img
                  v-else
                  :src="item.image || defaultImage"
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
  Search,
  Refresh,
  Delete,
  Calendar,
  View,
  Star,
  House,
  InfoFilled,
  Grid,
  Shop,
  Food,
  Document,
  RefreshLeft
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import DishDetailDialog from '../../components/dish/DishDetailDialog.vue'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'

const router = useRouter()

// 加载状态
const loading = ref(true)

// 默认图片
const defaultImage =
  'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3QgeD0iMCIgeT0iMCIgd2lkdGg9IjIwMCIgaGVpZ2h0PSIxNTAiIGZpbGw9IiNGRkYiIGNsaXAtcnVsZT0iZXZlbm9kZCIvPgo8cGF0aCBkPSJNMTAwIDc1QzEzMS4zIDc1IDE1OCA1MS4zIDE1OCAyMEMxNTggNC4zIDE0My43IDAgMTIwIDBDOTYuMyAwIDgyLjMgNy41IDY5LjMgMjMuN0w1MCA0M1YxMUM1MCA1LjUgNDUuNSAwIDQwIDBDMzQuNSAwIDMwIDUuNSAzMCAxMXYzMkwxNS43IDIzLjdDNC4zIDcuNSAwIDIzLjMgMCAyMEMwIDUxLjMgMjYuNyA3NSA1OCA3NUg1MEw4NSAxMDBMMTAwIDc1WiIgZmlsbD0iIzY3QzIzQSIvPgo8L3N2Zz4='

// 模拟收藏数据
const collections = ref([])

// 筛选条件
const filterType = ref('all')

// 分页
const currentPage = ref(1)
const pageSize = ref(9)

// 菜品详情弹窗
const dishDialogVisible = ref(false)
const selectedDish = ref(null)

// 计算过滤后的收藏
const filteredCollections = computed(() => {
  let filtered = [...collections.value]

  // 类型过滤
  if (filterType.value !== 'all') {
    filtered = filtered.filter((item) => item.type === filterType.value)
  }

  return filtered
})

// 分页数据
const paginatedCollections = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCollections.value.slice(start, end)
})

// 获取类型图标
const getTypeIcon = (type) => {
  const iconMap = {
    merchant: Shop,
    dish: Food,
    article: Document
  }
  return iconMap[type] || Star
}

// 获取类型名称
const getTypeName = (type) => {
  const nameMap = {
    merchant: '商家',
    dish: '菜品',
    article: '文章'
  }
  return nameMap[type] || '未知'
}

// 获取筛选类型名称
const getFilterTypeName = (type) => {
  return getTypeName(type)
}

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
const handleImageError = (event, item) => {
  event.target.src = defaultImage
  event.target.onerror = null
}

// 筛选变化处理
const handleFilterChange = () => {
  currentPage.value = 1
  ElMessage.success(`已切换到${getFilterTypeName(filterType.value)}收藏`)
}

// 应用筛选
const applyFilter = () => {
  ElMessage.success('筛选条件已应用')
  currentPage.value = 1
}

// 重置筛选
const resetFilter = () => {
  filterType.value = 'all'
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
  const collection = collections.value[collectionIndex]

  ElMessageBox.confirm('确定要删除该收藏吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        // 获取当前用户ID
        const userId = String(authStore.userId || 1) || 1
        // 发送删除请求到后端
        const response = await axios.delete(`${API_CONFIG.baseURL}/v1/collections`, {
          params: {
            userId,
            type: collection.type,
            id: collection.id
          }
        })

        if (response.data && response.data.code === '200') {
          // 从本地数组中删除该收藏项
          collections.value.splice(collectionIndex, 1)
          ElMessage.success('收藏已删除')
        } else {
          ElMessage.error('删除失败：' + (response.data?.message || '未知错误'))
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
        // 获取当前用户ID
        const userId = String(authStore.userId || 1) || '1'

        // 调用后端API清空所有收藏
        const response = await axios.delete(`${API_CONFIG.baseURL}/v1/collections/user/${userId}`)

        if (response.data && response.data.code === '200') {
          // 清空本地数据
          collections.value = []
          ElMessage.success('所有收藏已清空')
        } else {
          ElMessage.error('清空收藏失败：' + (response.data?.message || '未知错误'))
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

// 从后端加载收藏数据
onMounted(async () => {
  loading.value = true
  try {
    // 获取当前用户ID（从localStorage中获取）
    const userId = String(authStore.userId || 1) || '1'
    // 从后端获取收藏数据
    const response = await axios.get(`${API_CONFIG.baseURL}/v1/collections`, {
      params: { userId }
    })
    if (response.data && response.data.code === '200') {
      const rawCollections = response.data.data

      // 处理收藏数据,根据类型获取详细信息
      const processedCollections = await Promise.all(
        rawCollections.map(async (item) => {
          const baseCollection = {
            id: item.id,
            collectableId: item.collectableId,
            type: item.collectableType,
            date: new Date(item.createTime).toISOString().split('T')[0]
          }

          try {
            if (item.collectableType === 'merchant') {
              // 获取商家详情
              const merchantRes = await axios.get(
                `${API_CONFIG.baseURL}/v1/merchant/${item.collectableId}`
              )
              if (merchantRes.data?.code === '200' && merchantRes.data?.data) {
                const merchant = merchantRes.data.data
                return {
                  ...baseCollection,
                  title: merchant.name || '未知商家',
                  description: merchant.description || `这是${merchant.name || '商家'}的简介`,
                  image: merchant.image || defaultImage
                }
              }
            } else if (item.collectableType === 'dish') {
              // 获取菜品详情
              const dishRes = await axios.get(
                `${API_CONFIG.baseURL}/v1/dishes/${item.collectableId}`
              )
              if (dishRes.data?.code === '200' && dishRes.data?.data) {
                const dish = dishRes.data.data
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
              // 文章收藏 - 使用标题首字母作为图片
              const articleTitle = `文章 ${item.collectableId}`
              const firstChar = articleTitle.charAt(0)
              return {
                ...baseCollection,
                title: articleTitle,
                description: '文章收藏功能正在开发中',
                image: null, // 使用null来触发首字母显示
                firstChar: firstChar // 添加首字母字段
              }
            }
          } catch (error) {
            console.error(`加载${item.collectableType}详情失败:`, error)
          }

          // 默认返回值
          const defaultTitle = `${item.collectableType === 'merchant' ? '商家' : item.collectableType === 'dish' ? '菜品' : '文章'} ${item.collectableId}`
          const isArticle = item.collectableType === 'article'

          return {
            ...baseCollection,
            title: defaultTitle,
            description: `这是${item.collectableType === 'merchant' ? '商家' : item.collectableType === 'dish' ? '菜品' : '文章'}的描述`,
            image: isArticle ? null : defaultImage, // 文章使用null触发首字母显示
            firstChar: isArticle ? defaultTitle.charAt(0) : undefined // 文章添加首字母
          }
        })
      )

      collections.value = processedCollections
    } else {
      ElMessage.error('加载收藏数据失败：' + (response.data?.message || '未知错误'))
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
        :deep(.el-tag) {
          font-size: 14px;
          padding: 8px 16px;
          font-weight: 500;
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
      gap: 16px;
      flex: 1;
      min-width: 300px;

      .filter-select {
        width: 200px;

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

      .filter-summary {
        .filter-active {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: #606266;
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
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);

            .collection-image {
              width: 100%;
              height: 100%;
              object-fit: cover;
              transition: transform 0.3s ease;
            }

            .article-first-char {
              width: 100%;
              height: 100%;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 96px;
              font-weight: 700;
              color: white;
              background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
              text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
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

            .article-first-char {
              transform: scale(1.05);
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

        .filter-select {
          width: 100%;
        }
      }

      .filter-right {
        justify-content: space-between;

        .el-button-group {
          display: flex;
          flex: 1;

          .el-button {
            flex: 1;
          }
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
