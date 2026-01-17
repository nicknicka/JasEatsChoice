<template>
  <el-dialog
    v-model="dialogVisible"
    title="选择位置"
    width="600px"
    :close-on-click-modal="false"
    class="map-location-dialog"
    @open="handleDialogOpen"
    @close="handleDialogClose"
  >
    <div class="map-location-content">
      <!-- 搜索栏 -->
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索地址"
          clearable
          @clear="clearSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button :icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 地图容器 -->
      <div class="map-container">
        <div id="mapContainer" class="map-wrapper"></div>

        <!-- 地图加载状态 -->
        <div v-if="mapLoading" class="map-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>地图加载中...</span>
        </div>

        <!-- 定位按钮 -->
        <div class="location-controls">
          <el-button
            type="primary"
            circle
            :icon="Location"
            :loading="locating"
            @click="handleGetCurrentLocation"
            title="获取当前位置"
          />
        </div>
      </div>

      <!-- 当前位置显示 -->
      <div class="current-location-section">
        <div class="location-info">
          <div class="location-icon">
            <el-icon><Location /></el-icon>
          </div>
          <div class="location-details">
            <div class="location-address">
              {{ selectedAddress || '请在地图上选择位置' }}
            </div>
            <div v-if="selectedPosition" class="location-coords">
              经度: {{ selectedPosition.lng.toFixed(6) }} | 纬度:
              {{ selectedPosition.lat.toFixed(6) }}
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索结果列表 -->
      <div v-if="searchResults.length > 0" class="search-results">
        <div
          v-for="(item, index) in searchResults"
          :key="index"
          class="search-result-item"
          @click="selectSearchResult(item)"
        >
          <div class="result-icon">
            <el-icon><Location /></el-icon>
          </div>
          <div class="result-details">
            <div class="result-name">{{ item.name }}</div>
            <div class="result-address">{{ item.address }}</div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :disabled="!selectedPosition">
          确认选择
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { Search, Location, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  // 默认位置
  defaultPosition: {
    type: Object,
    default: () => ({ lng: 116.397428, lat: 39.90923 }) // 默认北京
  }
})

// Emits
const emit = defineEmits(['update:visible', 'location-selected'])

// 状态管理
const dialogVisible = ref(false)
const map = ref(null)
const marker = ref(null)
const mapLoading = ref(false)
const locating = ref(false)
const searchKeyword = ref('')
const searchResults = ref([])
const selectedPosition = ref(null)
const selectedAddress = ref('')

// 监听 visible prop
watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val
  }
)

// 监听 dialogVisible
watch(dialogVisible, (val) => {
  emit('update:visible', val)
})

// 初始化高德地图
const initMap = () => {
  if (typeof AMap === 'undefined') {
    console.error('高德地图 SDK 未加载')
    ElMessage.error('地图 SDK 加载失败，请刷新页面重试')
    return
  }

  mapLoading.value = true

  try {
    // 创建地图实例
    map.value = new AMap.Map('mapContainer', {
      zoom: 15,
      center: [props.defaultPosition.lng, props.defaultPosition.lat],
      mapStyle: 'amap://styles/normal',
      viewMode: '2D'
    })

    // 添加工具栏
    map.value.addControl(new AMap.ToolBar())
    map.value.addControl(new AMap.Scale())

    // 添加标记
    if (props.defaultPosition) {
      addMarker(props.defaultPosition.lng, props.defaultPosition.lat)
    }

    // 点击地图事件
    map.value.on('click', (e) => {
      const { lng, lat } = e.lnglat
      updateMarkerPosition(lng, lat)
      getAddressByLocation(lng, lat)
    })

    // 地图加载完成
    map.value.on('complete', () => {
      mapLoading.value = false
      console.log('地图加载完成')
    })
  } catch (error) {
    console.error('地图初始化失败:', error)
    mapLoading.value = false
    ElMessage.error('地图初始化失败')
  }
}

// 添加标记
const addMarker = (lng, lat) => {
  if (!map.value) return

  // 移除旧标记
  if (marker.value) {
    map.value.remove(marker.value)
  }

  // 创建新标记
  marker.value = new AMap.Marker({
    position: [lng, lat],
    animation: 'AMAP_ANIMATION_DROP',
    title: '选中的位置'
  })

  map.value.add(marker.value)
}

// 更新标记位置
const updateMarkerPosition = (lng, lat) => {
  selectedPosition.value = { lng, lat }

  if (marker.value) {
    marker.value.setPosition([lng, lat])
  } else {
    addMarker(lng, lat)
  }

  // 移动地图中心
  if (map.value) {
    map.value.setCenter([lng, lat])
  }
}

// 根据经纬度获取地址
const getAddressByLocation = async (lng, lat) => {
  try {
    if (typeof AMap === 'undefined') return

    const geocoder = new AMap.Geocoder()

    geocoder.getAddress([lng, lat], (status, result) => {
      if (status === 'complete' && result.info === 'OK') {
        selectedAddress.value = result.regeocode.formattedAddress
      } else {
        selectedAddress.value = '未知地址'
      }
    })
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

// 搜索地址
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    if (typeof AMap === 'undefined') return

    const placeSearch = new AMap.PlaceSearch({
      city: '全国', // 城市设为全国，自动在全国范围内搜索
      pageSize: 5 // 每页显示结果数
    })

    placeSearch.search(searchKeyword.value, (status, result) => {
      if (status === 'complete' && result.info === 'OK' && result.poiList.pois.length > 0) {
        searchResults.value = result.poiList.pois.map((poi) => ({
          name: poi.name,
          address: poi.address,
          location: poi.location
        }))
      } else {
        searchResults.value = []
        ElMessage.warning('未找到相关地址')
      }
    })
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请重试')
  }
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
}

// 选择搜索结果
const selectSearchResult = (item) => {
  if (item.location) {
    const { lng, lat } = item.location
    updateMarkerPosition(lng, lat)
    selectedAddress.value = item.name + ' ' + item.address
    searchResults.value = []
    searchKeyword.value = ''
  }
}

// 获取当前位置
const handleGetCurrentLocation = () => {
  locating.value = true

  if ('geolocation' in navigator) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude } = position.coords
        updateMarkerPosition(longitude, latitude)
        getAddressByLocation(longitude, latitude)
        locating.value = false
        ElMessage.success('定位成功')
      },
      (error) => {
        console.error('定位失败:', error)
        locating.value = false
        let errorMsg = '定位失败'
        if (error.code === 1) {
          errorMsg = '您拒绝了定位请求'
        } else if (error.code === 2) {
          errorMsg = '无法获取位置信息'
        } else if (error.code === 3) {
          errorMsg = '定位请求超时'
        }
        ElMessage.error(errorMsg)
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000
      }
    )
  } else {
    locating.value = false
    ElMessage.error('您的浏览器不支持定位功能')
  }
}

// 确认选择
const handleConfirm = () => {
  if (!selectedPosition.value) {
    ElMessage.warning('请先选择位置')
    return
  }

  emit('location-selected', {
    position: selectedPosition.value,
    address: selectedAddress.value
  })

  dialogVisible.value = false
}

// 取消
const handleCancel = () => {
  dialogVisible.value = false
}

// 对话框打开
const handleDialogOpen = () => {
  // 延迟初始化地图，确保 DOM 已渲染
  setTimeout(() => {
    initMap()
  }, 300)
}

// 对话框关闭
const handleDialogClose = () => {
  // 清理搜索结果
  searchResults.value = []
  searchKeyword.value = ''
}

// 组件卸载
onBeforeUnmount(() => {
  if (map.value) {
    map.value.destroy()
    map.value = null
  }
})
</script>

<style scoped lang="less">
.map-location-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
    max-height: 70vh;
    overflow-y: auto;
  }
}

.map-location-content {
  .search-section {
    margin-bottom: 16px;

    .el-input {
      :deep(.el-input__wrapper) {
        border-radius: 20px;
      }

      :deep(.el-input-group__append) {
        border-radius: 0 20px 20px 0;
        background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
        border: none;
        color: white;

        .el-button {
          background: transparent;
          border: none;
          color: white;

          &:hover {
            background: rgba(255, 255, 255, 0.1);
          }
        }
      }
    }
  }

  .map-container {
    position: relative;
    width: 100%;
    height: 350px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    margin-bottom: 16px;

    .map-wrapper {
      width: 100%;
      height: 100%;
    }

    .map-loading {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.95);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
      z-index: 10;

      .el-icon {
        font-size: 32px;
        color: #409eff;
      }

      span {
        font-size: 14px;
        color: #666;
      }
    }

    .location-controls {
      position: absolute;
      right: 16px;
      bottom: 16px;
      z-index: 5;

      .el-button {
        width: 48px;
        height: 48px;
        background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);

        &:hover {
          transform: scale(1.05);
          box-shadow: 0 6px 16px rgba(64, 158, 255, 0.5);
        }
      }
    }
  }

  .current-location-section {
    margin-bottom: 16px;

    .location-info {
      display: flex;
      gap: 12px;
      padding: 16px;
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      border-radius: 12px;
      border-left: 4px solid #409eff;

      .location-icon {
        flex-shrink: 0;
        width: 40px;
        height: 40px;
        background: linear-gradient(135deg, #409eff 0%, #5dade2 100%);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 20px;
      }

      .location-details {
        flex: 1;
        min-width: 0;

        .location-address {
          font-size: 15px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .location-coords {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }

  .search-results {
    max-height: 250px;
    overflow-y: auto;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    background: white;

    .search-result-item {
      display: flex;
      gap: 12px;
      padding: 12px 16px;
      cursor: pointer;
      transition: all 0.2s ease;
      border-bottom: 1px solid #f5f5f5;

      &:last-child {
        border-bottom: none;
      }

      &:hover {
        background: #f5f7fa;
      }

      .result-icon {
        flex-shrink: 0;
        width: 32px;
        height: 32px;
        background: #e8f3ff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #409eff;
      }

      .result-details {
        flex: 1;
        min-width: 0;

        .result-name {
          font-size: 14px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .result-address {
          font-size: 12px;
          color: #999;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .el-button {
    border-radius: 20px;
    padding: 10px 24px;
  }
}

// 自定义滚动条
.search-results {
  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;

    &:hover {
      background: #c0c4cc;
    }
  }

  &::-webkit-scrollbar-track {
    background: #f5f5f5;
  }
}
</style>
