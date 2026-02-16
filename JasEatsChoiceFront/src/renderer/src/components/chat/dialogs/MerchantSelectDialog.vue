<template>
  <el-dialog
    v-model="visible"
    title=""
    width="560px"
    @close="handleClose"
    class="merchant-select-dialog"
    :close-on-click-modal="false"
    :show-close="false"
  >
    <div class="dialog-content" :class="{ 'is-searching': isSearching }">
      <!-- 搜索框区域 -->
      <div class="search-section" :class="{ 'searching': isSearching }">
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索附近商家..."
            size="large"
            clearable
            :loading="searchLoading"
            @input="handleSearchInput"
            @focus="handleSearchFocus"
            @blur="handleSearchBlur"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 推荐商家区域（未搜索时显示） -->
      <div v-if="!isSearching && recommendedMerchants.length > 0" class="recommended-section">
        <div class="section-header">
          <el-icon><Star /></el-icon>
          <span>推荐商家</span>
        </div>
        <div class="merchant-list">
          <div
            v-for="merchant in recommendedMerchants"
            :key="merchant.id"
            class="merchant-card"
            @click="handleSelectMerchant(merchant)"
          >
            <div class="merchant-left">
              <div class="merchant-avatar">
                <img v-if="merchant.avatar && typeof merchant.avatar === 'string' && merchant.avatar.startsWith('http')"
                     :src="merchant.avatar"
                     :alt="merchant.name" />
                <span v-else class="avatar-placeholder">{{ merchant.avatar || '🏪' }}</span>
              </div>
              <div class="merchant-info">
                <h3 class="merchant-name">{{ merchant.name }}</h3>
                <div class="merchant-meta">
                  <el-tag size="small" type="info">{{ merchant.category || '餐饮' }}</el-tag>
                  <span class="distance" v-if="merchant.distance">
                    <el-icon><Location /></el-icon>
                    {{ merchant.distance }}
                  </span>
                </div>
                <p class="merchant-description" v-if="merchant.description">
                  {{ merchant.description }}
                </p>
              </div>
            </div>
            <div class="merchant-right">
              <el-icon :size="20" class="select-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索结果区域（搜索时显示） -->
      <div v-if="isSearching" class="search-results-section">
        <!-- 加载状态 -->
        <div v-if="searchLoading" class="loading-state">
          <el-icon class="is-loading" :size="32"><Loading /></el-icon>
          <p>搜索中...</p>
        </div>
        <!-- 搜索结果 -->
        <div v-else-if="searchResults.length > 0" class="merchant-list">
          <div class="result-count">
            找到 {{ searchResults.length }} 个商家
          </div>
          <div
            v-for="merchant in searchResults"
            :key="merchant.id"
            class="merchant-card"
            @click="handleSelectMerchant(merchant)"
          >
            <div class="merchant-left">
              <div class="merchant-avatar">
                <img v-if="merchant.avatar && typeof merchant.avatar === 'string' && merchant.avatar.startsWith('http')"
                     :src="merchant.avatar"
                     :alt="merchant.name" />
                <span v-else class="avatar-placeholder">{{ merchant.avatar || '🏪' }}</span>
              </div>
              <div class="merchant-info">
                <h3 class="merchant-name" v-html="highlightKeyword(merchant.name)"></h3>
                <div class="merchant-meta">
                  <el-tag size="small" type="info">{{ merchant.category || '餐饮' }}</el-tag>
                  <span class="distance" v-if="merchant.distance">
                    <el-icon><Location /></el-icon>
                    {{ merchant.distance }}
                  </span>
                </div>
              </div>
            </div>
            <div class="merchant-right">
              <el-icon :size="20" class="select-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
        <!-- 空状态 -->
        <div v-else class="empty-search">
          <el-empty :description="searchKeyword ? `未找到与'${searchKeyword}'相关的商家` : '请输入商家名称搜索'">
          </el-empty>
        </div>
      </div>

      <!-- 初始无数据状态 -->
      <div v-if="!isSearching && (!recommendedMerchants || recommendedMerchants.length === 0)" class="empty-state">
        <el-empty description="暂无推荐商家">
        </el-empty>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose" size="large">
          <el-icon><Close /></el-icon> 取消
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Shop, Search, Star, ArrowRight, Close, Location, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import merchantApi from '@/api/merchant'
import { debounce } from 'lodash-es'

/**
 * 商家选择对话框组件
 * @description 用于群订单中选择下单商家，支持搜索附近商家功能
 */
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  merchants: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = ref(props.modelValue)
const searchKeyword = ref('')
const isSearching = ref(false)
const searchResults = ref([])
const searchLoading = ref(false)
const allMerchants = ref([])

// 推荐商家（前6个商家）
const recommendedMerchants = computed(() => {
  // 如果从后端加载了商家，使用后端数据，否则使用props传入的数据
  const merchantList = allMerchants.value.length > 0 ? allMerchants.value : props.merchants
  return merchantList.slice(0, 6)
})

/**
 * 加载商家列表
 */
const loadMerchants = async () => {
  try {
    const response = await merchantApi.getMerchants()
    console.log('加载商家列表响应:', response)
    if (response.code === "200" && response.data) {
      allMerchants.value = response.data
    }
  } catch (error) {
    console.error('加载商家列表失败:', error)
  }
}

/**
 * 调用后端API搜索商家
 */
const searchMerchantsFromApi = debounce(async (keyword) => {
  if (!keyword || keyword.trim() === '') {
    searchResults.value = []
    searchLoading.value = false
    return
  }

  try {
    searchLoading.value = true
    const response = await merchantApi.getMerchants({ keyword })

    if (response.code === "200" && response.data) {
      searchResults.value = response.data
    } else {
      ElMessage.warning(response.message || '搜索商家失败')
      searchResults.value = []
    }
  } catch (error) {
    console.error('搜索商家失败:', error)
    ElMessage.error('搜索商家失败，请稍后重试')
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}, 300)

/**
 * 处理搜索输入
 */
const handleSearchInput = (value) => {
  if (!value || value.trim() === '') {
    searchResults.value = []
    return
  }

  // 调用后端API搜索
  searchMerchantsFromApi(value)
}

/**
 * 处理搜索框获得焦点
 */
const handleSearchFocus = () => {
  if (searchKeyword.value.trim() !== '') {
    isSearching.value = true
  }
}

/**
 * 处理搜索框失去焦点
 */
const handleSearchBlur = () => {
  // 延迟处理，避免点击搜索结果时先关闭搜索状态
  setTimeout(() => {
    if (searchKeyword.value.trim() === '') {
      isSearching.value = false
    }
  }, 200)
}

/**
 * 高亮关键词
 */
const highlightKeyword = (text) => {
  if (!searchKeyword.value || !text) return text

  const keyword = searchKeyword.value.trim()
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<span class="highlight">$1</span>')
}

/**
 * 处理对话框关闭
 */
const handleClose = () => {
  visible.value = false
  emit('update:modelValue', false)
  // 重置搜索状态
  setTimeout(() => {
    searchKeyword.value = ''
    isSearching.value = false
    searchResults.value = []
  }, 300)
}

/**
 * 选择商家
 */
const handleSelectMerchant = (merchant) => {
  emit('select', merchant)
  handleClose()
}

/**
 * 监听外部 modelValue 变化
 */
watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
  if (newVal) {
    // 对话框打开时加载商家列表
    loadMerchants()
  } else {
    // 对话框关闭时重置搜索状态
    searchKeyword.value = ''
    isSearching.value = false
    searchResults.value = []
  }
})

/**
 * 监听内部 visible 变化
 */
watch(visible, (newVal) => {
  if (!newVal) {
    emit('update:modelValue', false)
  }
})

/**
 * 监听搜索关键词变化
 */
watch(searchKeyword, (newVal) => {
  isSearching.value = newVal.trim() !== ''
  handleSearchInput(newVal)
})
</script>

<style scoped lang="less">
.merchant-select-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 0;
    background-color: #f5f7fa;
  }

  :deep(.el-dialog__footer) {
    padding: 16px 20px;
    background: white;
    border-top: 1px solid #e4e7ed;
  }
}

.dialog-content {
  min-height: 400px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;

  &.is-searching {
    .search-section {
      position: static;
      margin-bottom: 16px;
    }
  }
}

.search-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

  &.searching {
    padding: 12px 20px;
    justify-content: flex-start;
  }

  .search-box {
    width: 100%;
    max-width: 500px;

    .search-input {
      :deep(.el-input__wrapper) {
        border-radius: 24px;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
        padding: 8px 16px;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
        }

        &.is-focus {
          box-shadow: 0 6px 24px rgba(64, 158, 255, 0.25);
          border-color: #409eff;
        }
      }

      :deep(.el-input__inner) {
        font-size: 1.143rem /* 原值: 16px */;
        font-weight: 500;
      }

      :deep(.el-input__prefix) {
        font-size: 1.286rem /* 原值: 18px */;
        color: #909399;
      }
    }
  }
}

.recommended-section {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 16px;

  .section-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 1.143rem /* 原值: 16px */;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    padding: 0 8px;

    .el-icon {
      color: #f5a623;
      font-size: 1.429rem /* 原值: 20px */;
    }
  }
}

.search-results-section {
  flex: 1;
  overflow-y: auto;
  padding: 0 20px 16px;

  .result-count {
    font-size: 0.929rem /* 原值: 13px */;
    color: #909399;
    margin-bottom: 12px;
    padding: 0 8px;
  }

  .merchant-card {
    .merchant-name {
      :deep(.highlight) {
        background-color: #fff2cc;
        color: #f5a623;
        font-weight: 600;
        padding: 0 2px;
        border-radius: 2px;
      }
    }
  }
}

.merchant-list {
  .merchant-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px;
    background: white;
    border: 2px solid #e4e7ed;
    border-radius: 10px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      border-color: #409eff;
      box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
      transform: translateY(-2px);

      .select-arrow {
        color: #409eff;
        transform: translateX(4px);
      }
    }

    .merchant-left {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;

      .merchant-avatar {
        width: 50px;
        height: 50px;
        border-radius: 10px;
        overflow: hidden;
        background: linear-gradient(135deg, #e4e7ed 0%, #dcdfe6 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .avatar-placeholder {
          font-size: 1.714rem /* 原值: 24px */;
        }
      }

      .merchant-info {
        flex: 1;

        .merchant-name {
          font-size: 1.071rem /* 原值: 15px */;
          font-weight: 600;
          margin: 0 0 6px 0;
          color: #303133;
        }

        .merchant-meta {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 6px;

          .distance {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 0.929rem /* 原值: 13px */;
            color: #67c23a;
            font-weight: 500;

            .el-icon {
              font-size: 1rem /* 原值: 14px */;
            }
          }
        }

        .merchant-description {
          font-size: 0.929rem /* 原值: 13px */;
          color: #909399;
          margin: 4px 0 0 0;
          line-height: 1.5;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
      }
    }

    .merchant-right {
      flex-shrink: 0;

      .select-arrow {
        color: #c0c4cc;
        transition: all 0.3s;
      }
    }
  }
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  margin: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #909399;

  .el-icon {
    color: #409eff;
  }

  p {
    margin: 0;
    font-size: 1rem /* 原值: 14px */;
  }
}

.empty-state,
.empty-search {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  margin: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: center;

  .el-button {
    min-width: 120px;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .search-section {
    padding: 30px 16px;

    &.searching {
      padding: 12px 16px;
    }

    .search-box {
      max-width: 100%;
    }
  }

  .recommended-section,
  .search-results-section {
    padding: 0 16px 16px;
  }

  .merchant-list {
    .merchant-card {
      padding: 16px;

      .merchant-left {
        gap: 12px;

        .merchant-avatar {
          width: 56px;
          height: 56px;
        }

        .merchant-info {
          .merchant-name {
            font-size: 1.143rem /* 原值: 16px */;
          }
        }
      }
    }
  }
}
</style>
