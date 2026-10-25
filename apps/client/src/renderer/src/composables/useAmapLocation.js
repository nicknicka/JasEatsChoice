import { AMAP_CONFIG } from '../config'

let amapLoadPromise = null
const ONE_DAY_MS = 24 * 60 * 60 * 1000
const HIGH_ACCURACY_TIMEOUT_MS = 8000
const TRUSTED_CACHE_SOURCES = ['gps', 'manual', 'search']

const getAmapGlobal = () => {
  if (typeof window === 'undefined') {
    return null
  }

  return window.AMap || globalThis.AMap || null
}

export const loadAMapSDK = () => {
  if (amapLoadPromise) {
    return amapLoadPromise
  }

  amapLoadPromise = new Promise((resolve, reject) => {
    if (typeof window !== 'undefined') {
      window._AMapSecurityConfig = {
        securityJsCode: AMAP_CONFIG.securityJsCode
      }
    }

    const existingAMap = getAmapGlobal()
    if (existingAMap && existingAMap.Map) {
      resolve(existingAMap)
      return
    }

    const existingScript = document.querySelector('script[src*="webapi.amap.com/maps"]')
    if (existingScript) {
      const waitExisting = () => {
        const loadedAMap = getAmapGlobal()
        if (loadedAMap && loadedAMap.Map) {
          resolve(loadedAMap)
        } else {
          setTimeout(waitExisting, 100)
        }
      }

      setTimeout(waitExisting, 100)
      setTimeout(() => {
        reject(new Error('地图 SDK 加载超时'))
      }, 15000)
      return
    }

    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=1.4.15&key=${AMAP_CONFIG.key}&plugin=AMap.Scale,AMap.ToolBar,AMap.Geocoder,AMap.PlaceSearch,AMap.CitySearch`
    script.type = 'text/javascript'

    const timeout = setTimeout(() => {
      reject(new Error('地图 SDK 加载超时'))
    }, 15000)

    script.onload = () => {
      clearTimeout(timeout)

      const checkReady = () => {
        const loadedAMap = getAmapGlobal()
        if (loadedAMap && loadedAMap.Map) {
          console.log('地图 SDK 动态加载完成')
          resolve(loadedAMap)
        } else {
          setTimeout(checkReady, 50)
        }
      }

      checkReady()
    }

    script.onerror = (event) => {
      clearTimeout(timeout)
      console.error('地图 SDK 脚本加载失败:', event)
      amapLoadPromise = null
      reject(new Error('地图 SDK 脚本加载失败，请检查网络连接'))
    }

    document.head.appendChild(script)
  })

  return amapLoadPromise
}

const reverseGeocode = async ({ lng, lat, AMap }) => {
  try {
    const locationApi = (await import('../api/location.js')).default
    const response = await locationApi.reverseGeocode(String(lng), String(lat))

    if (response && response.code === '200' && response.data) {
      return response.data.formattedAddress || ''
    }
  } catch (error) {
    console.warn('后端逆地理编码失败，尝试前端 Geocoder:', error.message)
  }

  if (AMap && AMap.Geocoder) {
    try {
      const geocoder = new AMap.Geocoder()
      const result = await new Promise((resolve) => {
        geocoder.getAddress([lng, lat], (status, geoResult) => {
          if (status === 'complete' && geoResult?.info === 'OK') {
            resolve(geoResult?.regeocode?.formattedAddress || '')
          } else {
            resolve('')
          }
        })
      })

      if (result) {
        return result
      }
    } catch (error) {
      console.warn('前端逆地理编码失败:', error.message)
    }
  }

  return ''
}

const getIpLocationFromBackend = async (clientIp = null) => {
  const locationApi = (await import('../api/location.js')).default
  let finalIp = clientIp

  if (!finalIp) {
    try {
      const { resolveAndStorePublicIp } = await import('../utils/publicIp')
      finalIp = await resolveAndStorePublicIp()
    } catch (error) {
      console.warn('自动获取公网IP失败:', error.message)
    }
  }

  if (!finalIp) {
    finalIp = getClientIpCandidate()
  }

  return locationApi.ipLocation(finalIp)
}

const getClientIpCandidate = () => {
  if (typeof window === 'undefined') {
    return null
  }

  const fromRuntime = window.__CLIENT_IP__
  if (typeof fromRuntime === 'string' && fromRuntime.trim()) {
    return fromRuntime.trim()
  }

  try {
    const fromStorage = localStorage.getItem('client_ip') || localStorage.getItem('public_ip')
    if (typeof fromStorage === 'string' && fromStorage.trim()) {
      return fromStorage.trim()
    }
  } catch (error) {
    console.warn('读取本地IP候选失败:', error.message)
  }

  return null
}

const geocodeAddress = async ({ address, AMap }) => {
  if (!address || !AMap || !AMap.Geocoder) {
    return null
  }

  try {
    const geocoder = new AMap.Geocoder()
    const geocodeResult = await new Promise((resolve) => {
      geocoder.getLocation(address, (status, result) => {
        if (status === 'complete' && result?.geocodes?.length > 0) {
          resolve(result.geocodes[0])
        } else {
          resolve(null)
        }
      })
    })

    if (geocodeResult?.location?.lng && geocodeResult?.location?.lat) {
      return {
        lng: geocodeResult.location.lng,
        lat: geocodeResult.location.lat
      }
    }
  } catch (error) {
    console.warn('地理编码失败:', error.message)
  }

  return null
}

const getCitySearchFallback = async ({ AMap }) => {
  if (!AMap || !AMap.CitySearch) {
    return null
  }

  try {
    const cityResult = await new Promise((resolve, reject) => {
      const citySearch = new AMap.CitySearch()
      citySearch.getLocalCity((status, result) => {
        if (status === 'complete' && result) {
          resolve(result)
        } else {
          const detail = typeof result === 'object' ? JSON.stringify(result) : String(result || '')
          reject(new Error(result?.info || detail || '城市级定位失败'))
        }
      })
    })

    if (cityResult?.bounds && typeof cityResult.bounds.getCenter === 'function') {
      const center = cityResult.bounds.getCenter()
      if (center?.lng && center?.lat) {
        return {
          lng: center.lng,
          lat: center.lat,
          city: cityResult.city || cityResult.province || '',
          source: 'city-fallback'
        }
      }
    }

    const fallbackAddress = cityResult?.city || cityResult?.province
    const geocodeResult = await geocodeAddress({ address: fallbackAddress, AMap })
    if (geocodeResult?.lng && geocodeResult?.lat) {
      return {
        lng: geocodeResult.lng,
        lat: geocodeResult.lat,
        city: fallbackAddress || '',
        source: 'city-fallback'
      }
    }
  } catch (error) {
    console.log('城市级兜底定位失败:', error.message)
  }

  return null
}

const toNumberOrNull = (value) => {
  if (value === null || value === undefined || value === '') {
    return null
  }

  const num = typeof value === 'number' ? value : Number(value)
  return Number.isFinite(num) ? num : null
}

const normalizeLocationPayload = (payload) => {
  if (!payload || typeof payload !== 'object') {
    return {
      lng: null,
      lat: null,
      province: '',
      city: '',
      address: '',
      accuracy: ''
    }
  }

  return {
    lng: toNumberOrNull(payload.lng ?? payload.longitude),
    lat: toNumberOrNull(payload.lat ?? payload.latitude),
    province: payload.province || '',
    city: payload.city || '',
    address: payload.address || payload.formattedAddress || '',
    accuracy: payload.accuracy || ''
  }
}

const shouldUseCachedLocation = (lastLocation, allowedSources = TRUSTED_CACHE_SOURCES) => {
  if (!lastLocation) {
    return false
  }

  const source = typeof lastLocation.source === 'string' ? lastLocation.source : ''
  if (!allowedSources.includes(source)) {
    return false
  }

  const lng = toNumberOrNull(lastLocation.lng)
  const lat = toNumberOrNull(lastLocation.lat)
  if (lng === null || lat === null) {
    return false
  }

  const cacheTime = Number(lastLocation.timestamp)
  if (!Number.isFinite(cacheTime)) {
    return false
  }

  return Date.now() - cacheTime <= ONE_DAY_MS
}

export const resolveAmapAddress = async ({ lng, lat, AMap = getAmapGlobal() }) => {
  if (lng == null || lat == null) {
    return ''
  }

  return reverseGeocode({ lng, lat, AMap })
}

export const resolveAmapLocation = async ({
  getLastLocation,
  saveLastLocation,
  defaultPosition,
  clientIp = null,
  preferCacheFirst = true,
  cacheSources = TRUSTED_CACHE_SOURCES,
  useHighAccuracy = false,
  AMap = getAmapGlobal()
} = {}) => {
  let highAccuracyError = ''

  if (preferCacheFirst && typeof getLastLocation === 'function') {
    const lastLocation = getLastLocation()
    if (shouldUseCachedLocation(lastLocation, cacheSources)) {
      const lastLng = toNumberOrNull(lastLocation.lng)
      const lastLat = toNumberOrNull(lastLocation.lat)
      if (lastLng !== null && lastLat !== null) {
        const address = await reverseGeocode({ lng: lastLocation.lng, lat: lastLocation.lat, AMap })
        return {
          lng: lastLng,
          lat: lastLat,
          province: '',
          city: '',
          address,
          source: lastLocation.source || 'cache',
          accuracy: lastLocation.accuracy || lastLocation.source || 'cache',
          reason: '',
          hasLocation: true
        }
      }
    }
  }

  if (useHighAccuracy && AMap && AMap.Geolocation) {
    try {
      const position = await new Promise((resolve, reject) => {
        const geolocation = new AMap.Geolocation({
          enableHighAccuracy: true,
          timeout: HIGH_ACCURACY_TIMEOUT_MS,
          zoomToAccuracy: true,
          GeoLocationFirst: true,
          noIpLocate: 1,
          noGeoLocation: 0,
          needAddress: false,
          extensions: 'base'
        })

        geolocation.getCurrentPosition((status, result) => {
          if (status === 'complete' && result?.position) {
            resolve(result)
          } else {
            reject(new Error(result?.message || '定位失败'))
          }
        })
      })

      const { lng, lat } = position.position
      const address = await reverseGeocode({ lng, lat, AMap })
      if (typeof saveLastLocation === 'function') {
        saveLastLocation(lng, lat, 'gps')
      }

      return {
        lng,
        lat,
        province: '',
        city: '',
        address,
        source: 'gps',
        accuracy: 'gps',
        reason: '',
        hasLocation: true
      }
    } catch (error) {
      highAccuracyError = error.message || '系统精确定位不可用'
      console.info('系统精确定位不可用，继续使用粗定位兜底:', highAccuracyError)
    }
  }

  try {
    const effectiveIp = clientIp || getClientIpCandidate()
    const response = await getIpLocationFromBackend(effectiveIp)

    if (response && response.code === '200' && response.data) {
      const ipLocation = normalizeLocationPayload(response.data)

      if (ipLocation.lng !== null && ipLocation.lat !== null) {
        const address = await reverseGeocode({ lng: ipLocation.lng, lat: ipLocation.lat, AMap })

        return {
          lng: ipLocation.lng,
          lat: ipLocation.lat,
          province: ipLocation.province || '',
          city: ipLocation.city || '',
          address: ipLocation.address || address || '',
          source: 'ip',
          accuracy: ipLocation.accuracy || 'city',
          reason: highAccuracyError,
          hasLocation: true
        }
      }

      if (ipLocation.province || ipLocation.city) {
        const fallbackAddress = ipLocation.city || ipLocation.province
        const geocodeResult = await geocodeAddress({ address: fallbackAddress, AMap })

        if (geocodeResult?.lng && geocodeResult?.lat) {
          const address = await reverseGeocode({ lng: geocodeResult.lng, lat: geocodeResult.lat, AMap })

          return {
            lng: geocodeResult.lng,
            lat: geocodeResult.lat,
            province: ipLocation.province || '',
            city: ipLocation.city || '',
            address,
            source: 'ip',
            accuracy: 'city',
            reason: highAccuracyError,
            hasLocation: true
          }
        }
      }
    }
  } catch (error) {
    console.log('IP 定位失败，尝试其他方式:', error.message)
  }

  const cityFallback = await getCitySearchFallback({ AMap })
  if (cityFallback?.lng && cityFallback?.lat) {
    const address = await reverseGeocode({ lng: cityFallback.lng, lat: cityFallback.lat, AMap })

    return {
      lng: cityFallback.lng,
      lat: cityFallback.lat,
      province: '',
      city: cityFallback.city || '',
      address,
      source: cityFallback.source || 'city-fallback',
      accuracy: 'city',
      reason: highAccuracyError,
      hasLocation: true
    }
  }

  if (defaultPosition?.lng && defaultPosition?.lat) {
    const address = await reverseGeocode({ lng: defaultPosition.lng, lat: defaultPosition.lat, AMap })
    return {
      lng: defaultPosition.lng,
      lat: defaultPosition.lat,
      province: '',
      city: '',
      address,
      source: 'default',
      accuracy: 'default',
      reason: highAccuracyError,
      hasLocation: true
    }
  }

  return null
}
