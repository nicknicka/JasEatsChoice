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
import { ref, watch, onBeforeUnmount } from 'vue'
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

  console.log('开始初始化地图...')
  mapLoading.value = true

  try {
    // 创建地图实例 - 使用1.4.15兼容配置
    map.value = new AMap.Map('mapContainer', {
      zoom: 15,
      center: [props.defaultPosition.lng, props.defaultPosition.lat],
      viewMode: '2D'
    })

    console.log('地图实例创建成功')

    // 添加工具栏
    if (typeof AMap.ToolBar !== 'undefined') {
      map.value.addControl(new AMap.ToolBar())
      console.log('工具栏已添加')
    }

    if (typeof AMap.Scale !== 'undefined') {
      map.value.addControl(new AMap.Scale())
      console.log('比例尺已添加')
    }

    // 添加标记
    if (props.defaultPosition) {
      addMarker(props.defaultPosition.lng, props.defaultPosition.lat)
      console.log('默认标记已添加')
    }

    // 点击地图事件
    map.value.on('click', (e) => {
      const { lng, lat } = e.lnglat
      console.log('地图点击位置:', lng, lat)
      updateMarkerPosition(lng, lat)
      getAddressByLocation(lng, lat)
    })

    // 地图加载完成
    map.value.on('complete', () => {
      mapLoading.value = false
      console.log('地图加载完成')
    })

    // 设置超时，防止一直loading
    setTimeout(() => {
      if (mapLoading.value) {
        mapLoading.value = false
        console.log('地图初始化超时，但已继续')
      }
    }, 5000)
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

// 根据经纬度获取地址（使用后端代理）
const getAddressByLocation = async (lng, lat) => {
  try {
    // 动态导入 api 模块
    const amapApi = (await import('../api/amap.js')).default

    const response = await amapApi.regeocode(lng.toString(), lat.toString())

    // 修复：api 响应拦截器已经返回 response.data，所以直接检查 response.code
    if (response && response.code === '200' && response.data) {
      selectedAddress.value = response.data.formattedAddress || '未知地址'
    } else {
      selectedAddress.value = '未知地址'
    }
  } catch (error) {
    console.error('获取地址失败:', error)
    // 降级到前端 API
    if (typeof AMap !== 'undefined' && AMap.Geocoder) {
      try {
        const geocoder = new AMap.Geocoder()
        geocoder.getAddress([lng, lat], (status, result) => {
          if (status === 'complete' && result.info === 'OK') {
            selectedAddress.value = result.regeocode.formattedAddress
          }
        })
      } catch (e) {
        console.error('前端地址获取也失败:', e)
        selectedAddress.value = '未知地址'
      }
    }
  }
}

// 搜索地址（使用后端代理）
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  try {
    console.log('开始搜索:', searchKeyword.value)

    // 动态导入 api 模块
    const amapApi = (await import('../api/amap.js')).default

    const response = await amapApi.searchAddress(searchKeyword.value, '全国')

    console.log('后端搜索响应:', response)

    // 修复：response 直接包含 code, message, data（不是 response.data.code）
    if (response && response.code === '200') {
      const results = response.data || []
      console.log('搜索成功，找到', results.length, '个结果')

      if (results.length > 0) {
        searchResults.value = results.map((item) => ({
          name: item.name,
          address: item.address || '暂无详细地址',
          location: item.location
        }))
        ElMessage.success('找到 ' + results.length + ' 个结果，请点击选择')
      } else {
        searchResults.value = []
        ElMessage.warning('未找到相关地址，请尝试其他关键词')
      }
    } else {
      searchResults.value = []
      ElMessage.warning(response?.message || '搜索失败，请直接在地图上点击选择位置')
    }
  } catch (error) {
    console.error('搜索异常:', error)
    // 降级到前端 Autocomplete API
    console.log('降级使用前端 API 搜索')
    try {
      if (typeof AMap !== 'undefined') {
        AMap.plugin('AMap.Autocomplete', function () {
          const autocomplete = new AMap.Autocomplete({
            city: '全国',
            input: searchKeyword.value
          })

          autocomplete.search(searchKeyword.value, function (status, result) {
            if (status === 'complete' && result.info === 'OK') {
              if (result.tips && result.tips.length > 0) {
                searchResults.value = result.tips
                  .filter(tip => tip.location && tip.name)
                  .map(tip => ({
                    name: tip.name,
                    address: tip.district || tip.address || '暂无详细地址',
                    location: {
                      lng: tip.location.lng,
                      lat: tip.location.lat
                    }
                  }))
                  .slice(0, 10)
                ElMessage.success('找到 ' + searchResults.value.length + ' 个结果')
              } else {
                ElMessage.warning('未找到相关地址')
              }
            } else {
              ElMessage.warning('搜索失败，请直接在地图上选择位置')
            }
          })
        })
      } else {
        ElMessage.error('地图功能不可用，请刷新页面')
      }
    } catch (fallbackError) {
      console.error('前端 API 降级也失败:', fallbackError)
      ElMessage.error('搜索功能不可用，请直接在地图上点击选择位置')
    }
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
    console.log('开始获取当前位置...')

    // 先尝试使用 cached 位置（如果有）
    navigator.geolocation.getCurrentPosition(
      (position) => {
        console.log('定位成功:', position)
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
        let suggestAction = ''

        switch (error.code) {
          case error.PERMISSION_DENIED:
            errorMsg = '您拒绝了定位请求'
            suggestAction = '请在系统设置中允许定位权限'
            break
          case error.POSITION_UNAVAILABLE:
            errorMsg = '无法获取位置信息'
            suggestAction = '请检查网络连接或系统定位服务'
            break
          case error.TIMEOUT:
            errorMsg = '定位请求超时'
            suggestAction = '桌面应用定位功能受限，建议直接在地图上选择位置'
            break
          default:
            errorMsg = '定位失败(错误码: ' + error.code + ')'
            suggestAction = '建议直接在地图上选择位置'
            break
        }

        // 定位失败时的友好提示
        ElMessage.warning({
          message: suggestAction ? `${errorMsg}，${suggestAction}` : errorMsg,
          duration: 6000,
          showClose: true
        })

        // 定位失败后，自动使用默认位置（如北京）
        if (props.defaultPosition && error.code === error.TIMEOUT) {
          console.log('定位超时，使用默认位置')
          updateMarkerPosition(
            props.defaultPosition.lng,
            props.defaultPosition.lat
          )
          ElMessage.info('已定位到默认位置，请在地图上选择您的实际位置')
        }
      },
      {
        enableHighAccuracy: false, // 改为 false 以提高响应速度
        timeout: 30000, // 超时时间到 30 秒
        maximumAge: 600000 // 允许使用 10 分钟内的缓存位置
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
