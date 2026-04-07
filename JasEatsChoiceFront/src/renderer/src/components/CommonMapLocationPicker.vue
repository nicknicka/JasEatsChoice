<template>
  <el-dialog
    v-model="dialogVisible"
    width="560px"
    :close-on-click-modal="false"
    class="atlas-dialog"
    @open="handleDialogOpen"
    @close="handleDialogClose"
  >
    <!-- 自定义头部 -->
    <template #header>
      <div class="atlas-header">
        <span class="atlas-header-icon">📍</span>
        <span class="atlas-header-title">选择位置</span>
      </div>
    </template>

    <div class="atlas-body">
      <!-- 搜索栏（浮动在顶部） -->
      <div class="atlas-search-panel">
        <div class="atlas-search-bar">
          <el-icon class="atlas-search-icon"><Search /></el-icon>
          <input
            v-model="searchKeyword"
            class="atlas-search-input"
            placeholder="搜索地点、商圈、学校、地铁站…"
            @keyup.enter="handleSearch"
            @focus="searchFocused = true"
            @blur="handleSearchBlur"
          />
          <button v-if="searchKeyword" class="atlas-search-clear" @click="clearSearch">
            <el-icon :size="14"><Close /></el-icon>
          </button>
          <button class="atlas-search-btn" @click="handleSearch" :class="{ active: searchKeyword }">
            搜索
          </button>
        </div>

        <!-- 搜索结果（浮动面板） -->
        <transition name="slide-down">
          <div v-if="showResultsPanel" class="atlas-results-dropdown">
            <!-- 搜索中 -->
            <div v-if="searching" class="atlas-results-loading">
              <el-icon class="is-loading" :size="20"><Loading /></el-icon>
              <span>正在搜索…</span>
            </div>
            <!-- 搜索结果列表 -->
            <template v-else-if="searchResults.length > 0">
              <div class="atlas-results-header">
                <span>找到 {{ searchResults.length }} 个结果</span>
              </div>
              <div
                v-for="(item, index) in searchResults"
                :key="index"
                class="atlas-result-item"
                @mousedown.prevent="selectSearchResult(item)"
              >
                <span class="atlas-result-letter">{{ String.fromCharCode(65 + index) }}</span>
                <div class="atlas-result-body">
                  <span class="atlas-result-name">{{ item.name }}</span>
                  <span class="atlas-result-addr">{{ item.address }}</span>
                </div>
                <el-icon class="atlas-result-arrow"><ArrowRight /></el-icon>
              </div>
            </template>
            <!-- 无结果 -->
            <div v-else-if="searchKeyword && hasSearched" class="atlas-results-empty">
              <span class="atlas-results-empty-icon">🔍</span>
              <p>未找到「{{ searchKeyword }}」相关结果</p>
              <p class="atlas-results-empty-hint">试试更换关键词，或在地图上直接点击选择</p>
            </div>
          </div>
        </transition>
      </div>

      <!-- 地图 -->
      <div class="atlas-map-wrap">
        <div id="mapContainer" class="atlas-map-el"></div>

        <div v-if="mapLoading" class="atlas-map-loading">
          <el-icon class="is-loading" :size="28"><Loading /></el-icon>
          <span>地图载入中…</span>
        </div>

        <!-- 定位按钮 -->
        <button
          class="atlas-locate-fab"
          :class="{ 'is-locating': locating }"
          @click="handleGetCurrentLocation"
          title="我的位置"
        >
          <span class="atlas-locate-ring" v-if="locating"></span>
          <span class="atlas-locate-ring atlas-locate-ring-2" v-if="locating"></span>
          <el-icon :size="18"><Location /></el-icon>
        </button>

        <!-- 已选位置浮层（在地图上方） -->
        <transition name="slide-up">
          <div v-if="selectedAddress" class="atlas-selected-overlay" @click="handleConfirm">
            <div class="atlas-selected-content">
              <div class="atlas-selected-pin">
                <el-icon :size="16"><Location /></el-icon>
              </div>
              <div class="atlas-selected-info">
                <p class="atlas-selected-addr">{{ selectedAddress }}</p>
              </div>
              <span class="atlas-selected-action">确认</span>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <!-- 底部提示 -->
    <template #footer>
      <div class="atlas-footer">
        <span class="atlas-footer-hint">点击地图或搜索选择位置</span>
        <div class="atlas-footer-actions">
          <el-button @click="handleCancel" text>取消</el-button>
          <el-button type="primary" @click="handleConfirm" :disabled="!selectedPosition" size="default">
            确认选择
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed, onBeforeUnmount } from 'vue'
import { Search, Location, Loading, Close, ArrowRight } from '@element-plus/icons-vue'
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
const searchFocused = ref(false)
const searching = ref(false)
const hasSearched = ref(false)

// 计算属性：是否显示搜索结果面板
const showResultsPanel = computed(() => {
  return searchFocused.value && (searchResults.value.length > 0 || (searchKeyword.value && hasSearched.value) || searching.value)
})

// 处理搜索框失焦（延迟关闭以允许点击结果）
const handleSearchBlur = () => {
  setTimeout(() => {
    searchFocused.value = false
  }, 200)
}

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

// 等待高德地图 SDK 完全加载
const waitForAMap = () => {
  return new Promise((resolve, reject) => {
    if (typeof AMap !== 'undefined' && AMap.Map) {
      console.log('高德地图 SDK 已就绪')
      resolve()
      return
    }

    const timeout = setTimeout(() => {
      reject(new Error('高德地图 SDK 加载超时'))
    }, 10000)

    const checkInterval = setInterval(() => {
      if (typeof AMap !== 'undefined' && AMap.Map) {
        clearTimeout(timeout)
        clearInterval(checkInterval)
        console.log('高德地图 SDK 加载完成')
        resolve()
      }
    }, 100)
  })
}

// 初始化高德地图
const initMap = async () => {
  mapLoading.value = true

  try {
    // 等待 AMap SDK 加载完成
    await waitForAMap()

    console.log('开始初始化地图...')

    // 创建地图实例 - 高德地图 1.4.15 使用标准配置
    map.value = new AMap.Map('mapContainer', {
      zoom: 15,
      center: [props.defaultPosition.lng, props.defaultPosition.lat]
    })

    console.log('地图实例创建成功')

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

      // 地图加载完成后自动定位
      console.log('开始自动定位...')
      autoLocate()
    })

    // 捕获地图错误
    map.value.on('error', (error) => {
      console.error('地图运行时错误:', error)
      mapLoading.value = false
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
    ElMessage.error('地图 SDK 加载失败，请刷新页面重试')
  }
}

// 添加标记
const addMarker = (lng, lat) => {
  if (!map.value) return

  // 移除旧标记
  if (marker.value) {
    map.value.remove(marker.value)
  }

  // 创建新标记（2.0 API 简化）
  marker.value = new AMap.Marker({
    position: [lng, lat],
    title: '选中的位置',
    // 2.0 版本动画参数不同
    animation: 'AMAP_ANIMATION_DROP' // 1.4.15 的写法，2.0 应该兼容
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
    return
  }

  searching.value = true
  searchResults.value = []
  hasSearched.value = false
  searchFocused.value = true

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
      } else {
        searchResults.value = []
      }
      hasSearched.value = true
    } else {
      searchResults.value = []
      hasSearched.value = true
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
              }
            }
            hasSearched.value = true
            searching.value = false
          })
        })
      }
    } catch (fallbackError) {
      console.error('前端 API 降级也失败:', fallbackError)
      hasSearched.value = true
      searching.value = false
    }
  }

  searching.value = false
}

// 清空搜索
const clearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
  hasSearched.value = false
  searchFocused.value = false
}

// 选择搜索结果
const selectSearchResult = (item) => {
  if (item.location) {
    const { lng, lat } = item.location
    updateMarkerPosition(lng, lat)
    selectedAddress.value = item.name + ' ' + item.address
    searchResults.value = []
    searchKeyword.value = ''
    hasSearched.value = false
    searchFocused.value = false
  }
}

// 获取当前位置（直接使用 IP 定位）
const handleGetCurrentLocation = async () => {
  locating.value = true

  console.log('开始获取当前位置...')

  // ========== 第一级：IP 定位（快速且稳定） ==========
  try {
    const amapApi = (await import('../api/amap.js')).default
    const response = await amapApi.ipLocation()

    if (response && response.code === '200' && response.data) {
      const { lng, lat, province, city } = response.data

      if (lng && lat) {
        console.log('IP定位成功:', province, city, lng, lat)
        updateMarkerPosition(lng, lat)
        getAddressByLocation(lng, lat)

        // 保存到本地存储
        saveLastLocation(lng, lat)

        locating.value = false
        ElMessage.success(`IP定位成功：${province || ''}${city || ''}`)
        return
      }
    }
  } catch (error) {
    console.error('IP定位失败:', error)
  }

  // ========== 第二级：使用本地存储的上次位置 ==========
  const lastLocation = getLastLocation()
  if (lastLocation) {
    console.log('使用上次保存的位置:', lastLocation)
    updateMarkerPosition(lastLocation.lng, lastLocation.lat)
    getAddressByLocation(lastLocation.lng, lastLocation.lat)
    locating.value = false
    ElMessage.info('使用上次选择的位置')
    return
  }

  // ========== 第三级：尝试浏览器 GPS 定位（可选，用于移动端） ==========
  if ('geolocation' in navigator) {
    try {
      await new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            console.log('GPS定位成功:', position)
            const { latitude, longitude } = position.coords
            updateMarkerPosition(longitude, latitude)
            getAddressByLocation(longitude, latitude)

            // 保存到本地存储
            saveLastLocation(longitude, latitude)

            locating.value = false
            ElMessage.success('GPS定位成功')
            resolve()
          },
          (error) => {
            console.log('GPS定位失败，已降级到其他方式')
            reject(error)
          },
          {
            enableHighAccuracy: false,
            timeout: 15000, // 增加到 15 秒，给用户主动点击的定位更多时间
            maximumAge: 600000
          }
        )
      })
    } catch (error) {
      console.log('GPS定位异常，继续其他方式')
    }
  }

  // ========== 第四级：使用默认位置 ==========
  console.log('使用默认位置')
  if (props.defaultPosition) {
    updateMarkerPosition(props.defaultPosition.lng, props.defaultPosition.lat)
    getAddressByLocation(props.defaultPosition.lng, props.defaultPosition.lat)
  }
  locating.value = false
  ElMessage.warning({
    message: '无法自动定位，已显示默认位置。请在地图上点击选择您的实际位置。',
    duration: 5000,
    showClose: true
  })
}

// 保存位置到本地存储
const saveLastLocation = (lng, lat) => {
  try {
    const locationData = {
      lng,
      lat,
      timestamp: Date.now()
    }
    localStorage.setItem('user_last_location', JSON.stringify(locationData))
    console.log('位置已保存到本地存储')
  } catch (error) {
    console.warn('保存位置失败:', error)
  }
}

// 从本地存储获取上次位置
const getLastLocation = () => {
  try {
    const stored = localStorage.getItem('user_last_location')
    if (stored) {
      const locationData = JSON.parse(stored)

      // 检查是否过期（7天内有效）
      const sevenDays = 7 * 24 * 60 * 60 * 1000
      if (Date.now() - locationData.timestamp < sevenDays) {
        return {
          lng: locationData.lng,
          lat: locationData.lat
        }
      } else {
        // 过期则删除
        localStorage.removeItem('user_last_location')
      }
    }
  } catch (error) {
    console.warn('读取本地位置失败:', error)
  }
  return null
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

// 自动定位（静默模式，不显示加载状态和提示）
const autoLocate = async () => {
  console.log('开始自动定位...')

  // ========== 第一级：优先使用本地存储的上次位置（最快） ==========
  const lastLocation = getLastLocation()
  if (lastLocation) {
    console.log('使用上次保存的位置:', lastLocation)
    updateMarkerPosition(lastLocation.lng, lastLocation.lat)
    getAddressByLocation(lastLocation.lng, lastLocation.lat)
    return
  }

  // ========== 第二级：优先使用 IP 定位（快速稳定，无需授权） ==========
  try {
    const amapApi = (await import('../api/amap.js')).default
    const response = await amapApi.ipLocation()

    if (response && response.code === '200' && response.data) {
      const { lng, lat } = response.data

      if (lng && lat) {
        console.log('IP定位成功:', lng, lat)
        updateMarkerPosition(lng, lat)
        getAddressByLocation(lng, lat)

        // 保存到本地存储
        saveLastLocation(lng, lat)
        return
      }
    }
  } catch (error) {
    console.log('IP定位失败，尝试其他方式:', error.message)
  }

  // ========== 第三级：尝试浏览器 GPS 定位（需要用户授权，超时限制较短） ==========
  if ('geolocation' in navigator) {
    try {
      await new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            console.log('GPS定位成功:', position)
            const { latitude, longitude } = position.coords
            updateMarkerPosition(longitude, latitude)
            getAddressByLocation(longitude, latitude)

            // 保存到本地存储
            saveLastLocation(longitude, latitude)

            resolve()
          },
          (error) => {
            console.log('GPS定位失败:', error.message)
            reject(error)
          },
          {
            enableHighAccuracy: false,
            timeout: 8000, // 增加到 8 秒
            maximumAge: 600000
          }
        )
      })
      return
    } catch (error) {
      console.log('GPS定位异常或被拒绝:', error)
    }
  }

  // ========== 第四级：使用默认位置（北京） ==========
  console.log('使用默认位置（北京）')
  if (props.defaultPosition) {
    updateMarkerPosition(props.defaultPosition.lng, props.defaultPosition.lat)
    getAddressByLocation(props.defaultPosition.lng, props.defaultPosition.lat)
  }
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
@import '../assets/css/nordic-theme.less';

// ===== 设计令牌 =====
@clay: #C67B5C;
@clay-dark: #A8613F;
@clay-glow: rgba(198, 123, 92, 0.2);
@ink: #2D2A26;
@ink-sec: #8A857E;
@ink-muted: #B5AFA6;
@warm-bg: #F6F3ED;
@warm-surface: #FFFDF9;
@warm-border: #E8E2D8;

// ===== Dialog 整体 =====
.atlas-dialog {
  :deep(.el-dialog) {
    border-radius: 20px;
    overflow: hidden;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  }

  :deep(.el-dialog__header) {
    margin: 0;
    padding: 0;
    border-bottom: 1px solid @warm-border;
  }

  :deep(.el-dialog__body) {
    padding: 12px 16px;
    background: @warm-surface;
  }

  :deep(.el-dialog__footer) {
    padding: 10px 16px;
    border-top: 1px solid @warm-border;
    background: @warm-surface;
  }
}

// ===== 头部 =====
.atlas-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: linear-gradient(135deg, #FAF0E8 0%, #F4E6DE 100%);

  .atlas-header-icon {
    font-size: 20px;
  }

  .atlas-header-title {
    font-family: 'Noto Serif SC', 'Georgia', serif;
    font-size: 17px;
    font-weight: 700;
    color: @ink;
    letter-spacing: -0.2px;
  }
}

// ===== 搜索面板（相对定位容器） =====
.atlas-search-panel {
  position: relative;
  z-index: 20;
  margin-bottom: 10px;
}

// ===== 搜索栏 =====
.atlas-search-bar {
  display: flex;
  align-items: center;
  gap: 0;
  height: 42px;
  background: #fff;
  border: 1.5px solid @warm-border;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s ease;

  &:focus-within {
    border-color: @clay;
    box-shadow: 0 0 0 3px @clay-glow;
  }
}

.atlas-search-icon {
  flex-shrink: 0;
  padding-left: 12px;
  color: @ink-muted;
  font-size: 16px;
}

.atlas-search-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  padding: 0 10px;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: @ink;
  font-family: inherit;

  &::placeholder {
    color: @ink-muted;
    font-size: 13px;
  }
}

.atlas-search-clear {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border: none;
  background: #F0ECE6;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: @ink-sec;
  margin-right: 6px;
  transition: all 0.15s ease;

  &:hover {
    background: @clay;
    color: #fff;
  }
}

.atlas-search-btn {
  flex-shrink: 0;
  height: 100%;
  padding: 0 16px;
  border: none;
  background: #F0ECE6;
  color: @ink-sec;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;

  &.active {
    background: @clay;
    color: #fff;

    &:hover {
      background: @clay-dark;
    }
  }
}

// ===== 搜索结果下拉面板 =====
.atlas-results-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  right: 0;
  max-height: 280px;
  overflow-y: auto;
  background: #fff;
  border: 1.5px solid @warm-border;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  z-index: 30;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb {
    background: @warm-border;
    border-radius: 2px;
  }
}

.atlas-results-header {
  padding: 8px 14px 4px;
  font-size: 12px;
  color: @ink-muted;
  border-bottom: 1px solid #F5F0EA;
}

.atlas-results-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: @ink-sec;
  font-size: 13px;

  .el-icon { color: @clay; }
}

.atlas-results-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  text-align: center;

  .atlas-results-empty-icon {
    font-size: 28px;
    margin-bottom: 8px;
  }

  p {
    margin: 0;
    font-size: 13px;
    color: @ink-sec;
  }

  .atlas-results-empty-hint {
    font-size: 12px;
    color: @ink-muted;
    margin-top: 4px;
  }
}

.atlas-result-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s ease;
  border-bottom: 1px solid #F5F0EA;

  &:last-child { border-bottom: none; }

  &:hover {
    background: #FAF0E8;

    .atlas-result-letter {
      background: @clay;
      color: #fff;
    }

    .atlas-result-arrow {
      color: @clay;
    }
  }

  .atlas-result-letter {
    flex-shrink: 0;
    width: 28px;
    height: 28px;
    border-radius: 8px;
    background: #F0ECE6;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    color: @clay;
    transition: all 0.15s ease;
  }

  .atlas-result-body {
    flex: 1;
    min-width: 0;

    .atlas-result-name {
      display: block;
      font-size: 14px;
      font-weight: 600;
      color: @ink;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .atlas-result-addr {
      display: block;
      font-size: 12px;
      color: @ink-muted;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      margin-top: 2px;
    }
  }

  .atlas-result-arrow {
    flex-shrink: 0;
    color: @warm-border;
    transition: color 0.15s ease;
  }
}

// ===== 地图区域 =====
.atlas-map-wrap {
  position: relative;
  width: 100%;
  height: 340px;
  border-radius: 14px;
  overflow: hidden;
  border: 1.5px solid @warm-border;
  background: #EDE9E1;

  .atlas-map-el {
    width: 100%;
    height: 100%;
  }

  .atlas-map-loading {
    position: absolute;
    inset: 0;
    background: rgba(246, 243, 237, 0.9);
    backdrop-filter: blur(4px);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    z-index: 10;

    .el-icon { color: @clay; }

    span {
      font-size: 13px;
      color: @ink-sec;
    }
  }
}

// 定位按钮
.atlas-locate-fab {
  position: absolute;
  right: 12px;
  bottom: 72px;
  z-index: 5;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1.5px solid rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  color: @clay;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;

  &:hover {
    background: @clay;
    color: #fff;
    border-color: @clay;
    transform: scale(1.08);
    box-shadow: 0 4px 14px @clay-glow;
  }

  &.is-locating {
    pointer-events: none;
    background: fade(@clay, 12%);
    border-color: fade(@clay, 30%);
  }
}

// 定位脉冲波纹
.atlas-locate-ring {
  position: absolute;
  inset: 0;
  border-radius: 12px;
  border: 2px solid @clay;
  animation: pulse-ring 1.6s ease-out infinite;

  &.atlas-locate-ring-2 {
    animation-delay: 0.5s;
  }
}

@keyframes pulse-ring {
  0% {
    transform: scale(1);
    opacity: 0.7;
  }
  100% {
    transform: scale(1.8);
    opacity: 0;
  }
}

// 已选位置浮层（地图底部）
.atlas-selected-overlay {
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: 10px;
  z-index: 5;
  cursor: pointer;

  .atlas-selected-content {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(12px);
    border-radius: 12px;
    border: 1.5px solid rgba(198, 123, 92, 0.2);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transition: all 0.2s ease;

    &:hover {
      border-color: @clay;
      box-shadow: 0 4px 20px rgba(198, 123, 92, 0.2);
    }
  }

  .atlas-selected-pin {
    flex-shrink: 0;
    width: 32px;
    height: 32px;
    background: @clay;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .atlas-selected-info {
    flex: 1;
    min-width: 0;

    .atlas-selected-addr {
      font-size: 13px;
      font-weight: 600;
      color: @ink;
      margin: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .atlas-selected-action {
    flex-shrink: 0;
    padding: 4px 12px;
    background: @clay;
    color: #fff;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 600;
  }
}

// ===== 动画 =====
@keyframes spin {
  to { transform: rotate(360deg); }
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.2s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

// ===== 底部 =====
.atlas-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .atlas-footer-hint {
    font-size: 12px;
    color: @ink-muted;
  }

  .atlas-footer-actions {
    display: flex;
    gap: 8px;
  }

  :deep(.el-button) {
    border-radius: 10px;
    font-weight: 600;
  }

  :deep(.el-button--primary) {
    background: @clay;
    border-color: @clay;

    &:hover {
      background: @clay-dark;
      border-color: @clay-dark;
      box-shadow: 0 4px 12px @clay-glow;
    }

    &.is-disabled {
      background: @warm-border;
      border-color: @warm-border;
    }
  }
}
</style>
