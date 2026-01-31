<script setup>
import { ref, onMounted } from 'vue'
import CommonLocationPicker from '../../components/CommonLocationPicker.vue'
import CommonWeatherWidget from '../../components/CommonWeatherWidget.vue'
import FestivalRecommendList from '../../components/FestivalRecommendList.vue'
import UserCustomEventDialog from '../../components/UserCustomEventDialog.vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDebounceFn } from '@vueuse/core'
import { useRecommendations } from '../../composables/useRecommendations.js'
import { useFavorites } from '../../composables/useFavorites.js'
import { useRecommendationFilters } from '../../composables/useRecommendationFilters.js'
import { RECOMMENDATION_TYPE_TAGS } from '../../constants/recommendationConstants.js'
import { Calendar } from '@element-plus/icons-vue'

const router = useRouter()

// 使用composables
const {
  recommendations,
  isLoading,
  refreshing,
  loadAllRecommendations,
  rejectRecommendation,
  onRefresh
} = useRecommendations()

const { favorites, favoritesCount, initFavorites, toggleFavorite, isFavoritedItem } = useFavorites()

const {
  selectedCalorieRange,
  selectedTypes,
  selectedSources,
  searchKeyword,
  sortBy,
  CALORIE_RANGES,
  RECOMMENDATION_TYPES,
  filteredAndSortedRecommendations,
  availableTypes,
  hasActiveFilters,
  resetFilters
} = useRecommendationFilters(recommendations)

// 定位相关
const locationPicker = ref(null)
const currentLocation = ref(null)
const locationError = ref(false)
const currentCity = ref('')

// 天气相关
const weatherWidget = ref(null)

// UI状态
const showFilters = ref(false)
const showNutritionDetail = ref(null)

// 定位变化处理（防抖优化）
const handleLocationChanged = useDebounceFn((locationData) => {
  currentLocation.value = locationData.location
  currentCity.value = locationData.city
  // 根据位置更新推荐
  updateRecommendationsByLocation(locationData)
}, 1000)

// 天气变化处理（防抖优化）
const handleWeatherUpdated = useDebounceFn((weatherData) => {
  // 根据天气数据更新推荐
  console.log('天气数据更新:', weatherData)
  // 重新加载推荐
  loadAllRecommendations()
}, 2000)

// 定位成功后的处理
const handleLocationSuccess = (position) => {
  const { latitude, longitude } = position.coords
  const accuracy = position.coords.accuracy

  currentLocation.value = { latitude, longitude }

  // 检查定位误差是否超过500米
  if (accuracy > 500) {
    locationError.value = true
    ElMessageBox.warning({
      title: '定位误差提示',
      message: `当前定位误差为${Math.round(accuracy)}米，可能影响推荐准确性。是否重新定位？`,
      confirmButtonText: '重新定位',
      cancelButtonText: '取消',
      callback: (action) => {
        if (action === 'confirm') {
          getCurrentLocation()
        }
      }
    })
  } else {
    locationError.value = false
    ElMessage.success(`定位成功，误差${Math.round(accuracy)}米`)
    updateRecommendationsByLocation({ latitude, longitude })
  }
}

// 定位失败后的处理
const handleLocationError = (error) => {
  let errorMessage = '定位失败'
  switch (error.code) {
    case error.PERMISSION_DENIED:
      locationError.value = true
      return
    case error.POSITION_UNAVAILABLE:
      errorMessage = '定位信息不可用'
      break
    case error.TIMEOUT:
      errorMessage = '定位请求超时'
      break
    case error.UNKNOWN_ERROR:
      errorMessage = '未知定位错误'
      break
  }
  locationError.value = true
  ElMessage.error(errorMessage)
}

// 获取当前位置
const getCurrentLocation = () => {
  if ('geolocation' in navigator) {
    navigator.geolocation.getCurrentPosition(handleLocationSuccess, handleLocationError, {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 300000
    })
  } else {
    locationError.value = true
    ElMessage.error('您的浏览器不支持GPS定位功能')
  }
}

// 根据位置更新推荐
const updateRecommendationsByLocation = (location) => {
  console.log('根据位置更新推荐:', location)
  // 这里可以添加根据经纬度获取附近商家和推荐菜品的逻辑
}

// 处理收藏点击
const handleFavoriteClick = async (item) => {
  await toggleFavorite(item)
  // 更新收藏状态
  initFavorites()
}

// 显示营养详情
const showNutritionDialog = (item) => {
  showNutritionDetail.value = item
}

// 节日推荐相关
const showMyEvents = ref(false)

// 节日菜品点击
const handleFestivalDishClick = (dish) => {
  console.log('节日菜品点击:', dish)
  // TODO: 跳转到菜品详情或商家页面
}

// 节日推荐加入购物车
const handleFestivalAddToCart = async (dish) => {
  try {
    // TODO: 调用加入购物车API
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 节日推荐反馈
const handleFestivalFeedback = async (feedback) => {
  try {
    await api.post('/v1/festival/feedback', feedback)
    ElMessage.success('感谢您的反馈')
  } catch (error) {
    ElMessage.error('反馈提交失败')
  }
}

// 用户事件创建成功
const handleEventSuccess = () => {
  ElMessage.success('纪念日添加成功')
}

// 页面加载时获取定位和推荐数据
onMounted(async () => {
  getCurrentLocation()
  await loadAllRecommendations()
  initFavorites()
})
</script>

<template>
  <div class="recommend-container">
    <!-- 引入定位和天气组件 -->
    <CommonLocationPicker
      ref="locationPicker"
      auto-locate
      @location-changed="handleLocationChanged"
    />
    <CommonWeatherWidget
      ref="weatherWidget"
      :city="currentCity"
      @weather-updated="handleWeatherUpdated"
    />

    <h2>我的推荐</h2>

    <!-- 筛选和排序工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索推荐菜品..."
          prefix-icon="Search"
          clearable
          style="width: 300px"
        />
      </div>
      <div class="toolbar-right">
        <el-button
          :type="showFilters ? 'primary' : 'default'"
          :icon="showFilters ? 'FilterFilled' : 'Filter'"
          @click="showFilters = !showFilters"
        >
          筛选
        </el-button>
        <el-select v-model="sortBy" placeholder="排序方式" style="width: 150px; margin-left: 10px">
          <el-option label="默认排序" value="default" />
          <el-option label="卡路里从低到高" value="calories_asc" />
          <el-option label="卡路里从高到低" value="calories_desc" />
          <el-option label="评分从高到低" value="rating_desc" />
          <el-option label="评分从低到高" value="rating_asc" />
        </el-select>
      </div>
    </div>

    <!-- 筛选面板 -->
    <transition name="filter-slide">
      <div class="filter-panel" v-show="showFilters">
        <div class="filter-section">
          <div class="filter-title">卡路里范围</div>
          <el-radio-group v-model="selectedCalorieRange">
            <el-radio :label="0">全部</el-radio>
            <el-radio v-for="range in CALORIE_RANGES" :key="range.id" :label="range.id">{{
              range.label
            }}</el-radio>
          </el-radio-group>
        </div>

        <div class="filter-section">
          <div class="filter-title">餐食类型</div>
          <el-checkbox-group v-model="selectedTypes">
            <el-checkbox
              label="全部"
              :indeterminate="
                selectedTypes.length > 0 && selectedTypes.length < availableTypes.length
              "
              @change="
                (checked) =>
                  checked ? (selectedTypes = [...availableTypes]) : (selectedTypes = [])
              "
            />
            <el-checkbox v-for="type in availableTypes.slice(0, 10)" :key="type" :label="type">{{
              type
            }}</el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="filter-section">
          <div class="filter-title">推荐来源</div>
          <el-checkbox-group v-model="selectedSources">
            <el-checkbox
              label="全部"
              @change="
                (checked) =>
                  checked
                    ? (selectedSources = Object.values(RECOMMENDATION_TYPES))
                    : (selectedSources = [])
              "
            />
            <el-checkbox v-for="(label, key) in RECOMMENDATION_TYPES" :key="key" :label="label">{{
              label
            }}</el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="filter-actions">
          <el-button type="primary" @click="showFilters = false">应用筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>
    </transition>

    <!-- 筛选结果提示 -->
    <div class="filter-info" v-if="hasActiveFilters">
      <span>找到 {{ filteredAndSortedRecommendations.length }} 个推荐结果</span>
      <el-button type="text" size="small" @click="resetFilters">清除筛选</el-button>
    </div>

    <!-- 刷新按钮 -->
    <div class="refresh-bar">
      <el-button
        :icon="refreshing ? 'Loading' : 'Refresh'"
        :loading="refreshing"
        @click="onRefresh"
        circle
      />
    </div>

    <!-- 加载中状态 -->
    <div class="loading-skeleton" v-if="isLoading && recommendations.length === 0">
      <el-skeleton :rows="6" type="card" :border="false" />
    </div>

    <!-- 推荐列表 -->
    <transition-group
      name="recommend-card"
      tag="div"
      class="recommend-grid"
      v-else-if="filteredAndSortedRecommendations.length > 0"
    >
      <el-card
        v-for="item in filteredAndSortedRecommendations"
        :key="item.id"
        class="recommend-card"
        :class="{ featured: item.rating >= 4.9 }"
      >
        <!-- 推荐来源标签 -->
        <div class="recommend-source-tag" v-if="item.recommendSource">
          <el-tag
            :type="RECOMMENDATION_TYPE_TAGS[item.recommendSource]?.type || 'info'"
            size="small"
            effect="dark"
          >
            {{ item.recommendSource }}
          </el-tag>
        </div>

        <div class="card-header">
          <div class="dish-image">{{ item.image }}</div>
          <div class="dish-info">
            <div class="dish-name">{{ item.name }}</div>
            <div class="dish-type">
              <el-tag type="primary" size="small" v-if="item.type">{{ item.type }}</el-tag>
              <el-tag type="info" size="small" effect="plain" v-else>未分类</el-tag>
            </div>
          </div>
        </div>

        <!-- 卡路里信息（带营养详情） -->
        <div class="calories-info" v-if="item.calories">
          <span>🔥</span>
          <span>{{ item.calories }} kcal</span>
          <el-button
            type="text"
            size="small"
            @click="showNutritionDialog(item)"
            v-if="item.nutrition"
            style="margin-left: auto"
          >
            营养详情
          </el-button>
        </div>
        <div class="calories-info-unavailable" v-else>
          <span>🔥</span>
          <span>卡路里信息暂不可用</span>
        </div>

        <!-- 标签 -->
        <div class="tags-section">
          <el-tag v-for="tag in item.tagsWithType" :key="tag.name" size="small" :type="tag.type">
            {{ tag.name }}
          </el-tag>
        </div>

        <!-- 推荐理由 -->
        <div class="reason-section">
          <div class="reason-title">推荐理由</div>
          <div class="reason-text" :class="{ 'empty-reason': !item.reason }">
            {{ item.reason || '暂无推荐理由' }}
          </div>
        </div>

        <!-- 评分 -->
        <div class="rating">
          <el-rate v-model="item.rating" :disabled="true" show-text />
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
          <el-button
            type="primary"
            size="small"
            @click="
              router.push({
                path: '/user/home/merchants',
                query: {
                  search: item.name.replace(/(.*推荐:|.*特色:)/, '').trim()
                }
              })
            "
          >
            立即下单
          </el-button>
          <el-button
            :type="isFavoritedItem(item) ? 'warning' : 'default'"
            size="small"
            :icon="isFavoritedItem(item) ? 'StarFilled' : 'Star'"
            @click="handleFavoriteClick(item)"
          >
            {{ isFavoritedItem(item) ? '已收藏' : '收藏' }}
          </el-button>
          <el-button type="text" size="small" @click="rejectRecommendation(item)">
            不感兴趣
          </el-button>
        </div>
      </el-card>
    </transition-group>

    <!-- 空状态提示 -->
    <div class="empty-state" v-else>
      <div class="empty-icon">🥺</div>
      <div class="empty-text">
        {{ hasActiveFilters ? '没有找到符合条件的推荐' : '暂无推荐数据' }}
      </div>
      <div class="empty-subtext">
        {{ hasActiveFilters ? '试试调整筛选条件' : '系统正在努力为您生成个性化推荐' }}
      </div>
      <el-button
        type="primary"
        size="small"
        @click="hasActiveFilters ? resetFilters() : onRefresh()"
      >
        {{ hasActiveFilters ? '清除筛选' : '重新获取推荐' }}
      </el-button>
    </div>

    <!-- 节日推荐区域 -->
    <div class="festival-section">
      <div class="section-header">
        <h3>节日美食推荐</h3>
        <el-button type="text" @click="showMyEvents = true">
          <el-icon><Calendar /></el-icon>
          我的纪念日
        </el-button>
      </div>
      <festival-recommend-list
        v-if="!showFestivalsOnly"
        @dish-click="handleFestivalDishClick"
        @add-to-cart="handleFestivalAddToCart"
        @feedback="handleFestivalFeedback"
      />
    </div>

    <!-- 营养详情弹窗 -->
    <el-dialog v-model="showNutritionDetail" title="营养成分详情" width="400px">
      <div class="nutrition-detail" v-if="showNutritionDetail?.nutrition">
        <div class="nutrition-item">
          <span class="nutrition-label">碳水化合物</span>
          <span class="nutrition-value">{{ showNutritionDetail.nutrition.carbs }}g</span>
        </div>
        <div class="nutrition-item">
          <span class="nutrition-label">蛋白质</span>
          <span class="nutrition-value">{{ showNutritionDetail.nutrition.protein }}g</span>
        </div>
        <div class="nutrition-item">
          <span class="nutrition-label">脂肪</span>
          <span class="nutrition-value">{{ showNutritionDetail.nutrition.fat }}g</span>
        </div>
      </div>
      <div class="nutrition-empty" v-else>暂无详细营养信息</div>
    </el-dialog>

    <!-- 用户自定义事件弹窗 -->
    <user-custom-event-dialog
      v-model:visible="showMyEvents"
      @success="handleEventSuccess"
    />
  </div>
</template>

<style scoped lang="less">
.recommend-container {
  padding: 0 20px 20px 20px;

  h2 {
    font-size: 32px;
    margin: 0 0 32px 20px;
    color: #1a202c;
    font-weight: 800;
    letter-spacing: -0.5px;

    &::after {
      content: '';
      display: block;
      width: 60px;
      height: 4px;
      background: linear-gradient(135deg, #23d160 0%, #20c997 100%);
      border-radius: 2px;
      margin-top: 12px;
    }
  }

  // 工具栏
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px 20px 20px;
    gap: 15px;

    .toolbar-left {
      display: flex;
      gap: 15px;
    }

    .toolbar-right {
      display: flex;
      gap: 10px;
    }
  }

  // 筛选面板
  .filter-panel {
    background: #f9fafb;
    border-radius: 12px;
    padding: 20px;
    margin: 0 20px 20px 20px;
    border: 1px solid #e5e7eb;

    .filter-section {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-title {
        font-weight: 600;
        color: #333;
        margin-bottom: 12px;
        font-size: 14px;
      }
    }

    .filter-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #e5e7eb;
    }
  }

  .filter-slide-enter-active,
  .filter-slide-leave-active {
    transition: all 0.3s ease;
    max-height: 500px;
    overflow: hidden;
  }

  .filter-slide-enter-from,
  .filter-slide-leave-to {
    max-height: 0;
    opacity: 0;
  }

  // 筛选信息
  .filter-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 20px;
    margin: 0 20px 20px 20px;
    background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
    border-radius: 8px;
    color: #333;
    font-size: 14px;
  }

  // 刷新按钮
  .refresh-bar {
    display: flex;
    justify-content: center;
    padding: 20px;
  }

  .recommend-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 20px;
    padding: 0 20px;
  }

  .recommend-card {
    width: 100%;
    box-sizing: border-box;
    transition: all 0.3s ease;
    border-radius: 12px;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);
    position: relative;

    &:hover {
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
      transform: translateY(-8px) scale(1.02);
    }

    // 精选推荐卡片
    &.featured {
      border: 2px solid #f59e0b;
      box-shadow: 0 4px 20px rgba(245, 158, 11, 0.2);

      &:hover {
        box-shadow: 0 10px 40px rgba(245, 158, 11, 0.3);
      }
    }

    // 推荐来源标签
    .recommend-source-tag {
      position: absolute;
      top: 12px;
      right: 12px;
      z-index: 1;
    }

    .card-header {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
      align-items: center;
      padding: 0;

      .dish-image {
        font-size: 70px;
        line-height: 1;
      }

      .dish-info {
        flex: 1;

        .dish-name {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 8px;
          color: #333;
        }
      }
    }

    .calories-info {
      display: flex;
      gap: 10px;
      color: #ff6b6b;
      font-weight: 800;
      margin-bottom: 18px;
      font-size: 20px;
      align-items: center;
      padding: 8px 16px;
      background: linear-gradient(135deg, #fff5f5 0%, #ffe4e4 100%);
      border-radius: 20px;
    }

    .calories-info-unavailable {
      display: flex;
      gap: 8px;
      color: #c0c4cc;
      margin-bottom: 18px;
      font-size: 14px;
      align-items: center;
    }

    .tags-section {
      margin-bottom: 20px;
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      :deep(.el-tag) {
        border-radius: 20px;
      }
    }

    .reason-section {
      margin-bottom: 24px;

      .reason-title {
        font-weight: bold;
        margin-bottom: 8px;
        color: #333;
        font-size: 15px;
      }

      .reason-text {
        color: #666;
        font-size: 14px;
        line-height: 1.6;
      }

      .reason-text.empty-reason {
        color: #c0c4cc;
        font-style: italic;
      }
    }

    .rating {
      margin-bottom: 24px;

      :deep(.el-rate__text) {
        font-size: 14px;
        color: #e6a23c;
      }

      :deep(.el-rate__icon) {
        font-size: 16px;
      }
    }

    .card-actions {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 15px;
      padding-top: 16px;
      border-top: 1px solid #f0f0f0;
      flex-wrap: wrap;

      .el-button {
        border-radius: 8px;
        font-weight: 500;
      }
    }
  }

  /* 推荐卡片过渡动画 */
  .recommend-card-move {
    transition: all 0.5s ease;
  }

  .recommend-card-enter-active,
  .recommend-card-leave-active {
    transition:
      opacity 0.3s ease,
      transform 0.5s ease;
  }

  .recommend-card-enter-from {
    opacity: 0;
    transform: translateY(20px);
  }

  .recommend-card-leave-to {
    opacity: 0;
    transform: translateY(-20px);
  }

  // 加载中样式
  .loading-skeleton {
    padding: 20px 0;
  }
}

.empty-state {
  text-align: center;
  padding: 100px 20px;
  background-color: #ffffff;
  border-radius: 12px;
  margin-top: 20px;
  box-shadow: 0 2px 15px rgba(0, 0, 0, 0.08);
  border: 1px dashed #e4e7ed;

  .empty-icon {
    font-size: 90px;
    margin-bottom: 20px;
    opacity: 0.7;
  }

  .empty-text {
    font-size: 22px;
    font-weight: bold;
    color: #333;
    margin-bottom: 12px;
  }

  .empty-subtext {
    font-size: 14px;
    color: #909399;
    margin-bottom: 36px;
    line-height: 1.6;
  }

  .el-button {
    border-radius: 8px;
    font-weight: 500;
    padding: 10px 24px;
  }
}

// 营养详情弹窗
.nutrition-detail {
  padding: 20px 0;

  .nutrition-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .nutrition-label {
      font-size: 15px;
      color: #666;
    }

    .nutrition-value {
      font-size: 18px;
      font-weight: bold;
      color: #333;
    }
  }
}

.nutrition-empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

// 响应式优化
@media (max-width: 768px) {
  .recommend-container {
    padding: 0 10px 20px 10px;

    h2 {
      font-size: 24px;
      margin: 0 0 20px 10px;
    }

    .toolbar {
      flex-direction: column;
      align-items: stretch;

      .toolbar-left {
        flex-direction: column;
      }

      .toolbar-right {
        flex-direction: column;

        .el-select {
          width: 100% !important;
          margin-left: 0 !important;
        }
      }
    }

    .recommend-grid {
      grid-template-columns: 1fr;
      gap: 12px;
      padding: 0 10px;
    }

    .filter-panel {
      margin: 0 10px 20px 10px;
    }

    .filter-info {
      margin: 0 10px 20px 10px;
    }
  }
}
</style>
