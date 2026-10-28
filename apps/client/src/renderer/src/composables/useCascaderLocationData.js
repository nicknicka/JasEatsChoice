import { ref } from 'vue'
import api from '../utils/api.js'
import { API_CONFIG } from '../config/index.js'

const DEFAULT_CACHE_KEY = 'jaseatschoice_location_cascader_data'

const readCachedData = (cacheKey) => {
  try {
    const cachedData = localStorage.getItem(cacheKey)
    return cachedData ? JSON.parse(cachedData) : []
  } catch (error) {
    console.warn('读取缓存地址数据失败:', error)
    return []
  }
}

const saveCachedData = (cacheKey, data) => {
  try {
    localStorage.setItem(cacheKey, JSON.stringify(data))
  } catch (error) {
    console.warn('保存缓存地址数据失败:', error)
  }
}

const normalizeLocationData = (response) => {
  const payload = response?.data ?? response

  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.data)) return payload.data
  if (Array.isArray(payload?.rows)) return payload.rows
  if (Array.isArray(payload?.list)) return payload.list

  return []
}

export function useCascaderLocationData(options = {}) {
  const cascaderData = ref([])
  const loading = ref(false)
  const cacheKey = options.cacheKey || DEFAULT_CACHE_KEY

  const loadLocationData = async () => {
    loading.value = true

    try {
      const response = await api.get(API_CONFIG.location.cascaderData)
      const locationData = normalizeLocationData(response)

      if (locationData.length > 0) {
        cascaderData.value = locationData
        saveCachedData(cacheKey, locationData)
        return { source: 'backend', data: locationData }
      }

      throw new Error('后端未返回有效的地区数据')
    } catch (error) {
      console.error('加载地区级联数据失败:', error)

      const cachedData = readCachedData(cacheKey)
      if (cachedData.length > 0) {
        cascaderData.value = cachedData
        return { source: 'cache', data: cachedData }
      }

      cascaderData.value = []
      return { source: 'empty', data: [] }
    } finally {
      loading.value = false
    }
  }

  return {
    cascaderData,
    loading,
    loadLocationData
  }
}
