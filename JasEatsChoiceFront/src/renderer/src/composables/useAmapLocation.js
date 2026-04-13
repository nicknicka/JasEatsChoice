import { AMAP_CONFIG } from '../config'

let amapLoadPromise = null

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
        reject(new Error('高德地图 SDK 加载超时'))
      }, 15000)
      return
    }

    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=1.4.15&key=${AMAP_CONFIG.key}&plugin=AMap.Scale,AMap.ToolBar,AMap.Geocoder,AMap.PlaceSearch,AMap.Geolocation,AMap.CitySearch`
    script.type = 'text/javascript'

    const timeout = setTimeout(() => {
      reject(new Error('高德地图 SDK 加载超时'))
    }, 15000)

    script.onload = () => {
      clearTimeout(timeout)

      const checkReady = () => {
        const loadedAMap = getAmapGlobal()
        if (loadedAMap && loadedAMap.Map) {
          console.log('高德地图 SDK 动态加载完成')
          resolve(loadedAMap)
        } else {
          setTimeout(checkReady, 50)
        }
      }

      checkReady()
    }

    script.onerror = (event) => {
      clearTimeout(timeout)
      console.error('高德地图 SDK 脚本加载失败:', event)
      amapLoadPromise = null
      reject(new Error('高德地图 SDK 脚本加载失败，请检查网络连接'))
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

const getIpLocationFromBackend = async () => {
  const locationApi = (await import('../api/location.js')).default
  return locationApi.ipLocation()
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
  preferCacheFirst = true,
  AMap = getAmapGlobal()
} = {}) => {
  if (preferCacheFirst && typeof getLastLocation === 'function') {
    const lastLocation = getLastLocation()
    if (lastLocation?.lng && lastLocation?.lat) {
      const address = await reverseGeocode({ lng: lastLocation.lng, lat: lastLocation.lat, AMap })
      return {
        lng: lastLocation.lng,
        lat: lastLocation.lat,
        province: '',
        city: '',
        address,
        source: lastLocation.source || 'cache',
        hasLocation: true
      }
    }
  }

  try {
    const response = await getIpLocationFromBackend()

    if (response && response.code === '200' && response.data) {
      const { lng, lat, province, city, accuracy } = response.data

      if (lng && lat) {
        const address = await reverseGeocode({ lng, lat, AMap })
        if (typeof saveLastLocation === 'function') {
          saveLastLocation(lng, lat, 'ip')
        }

        return {
          lng,
          lat,
          province: province || '',
          city: city || '',
          address,
          source: 'ip',
          accuracy: accuracy || 'city',
          hasLocation: true
        }
      }

      if (province || city) {
        const fallbackAddress = city || province
        const geocodeResult = await geocodeAddress({ address: fallbackAddress, AMap })

        if (geocodeResult?.lng && geocodeResult?.lat) {
          const address = await reverseGeocode({ lng: geocodeResult.lng, lat: geocodeResult.lat, AMap })
          if (typeof saveLastLocation === 'function') {
            saveLastLocation(geocodeResult.lng, geocodeResult.lat, 'ip')
          }

          return {
            lng: geocodeResult.lng,
            lat: geocodeResult.lat,
            province: province || '',
            city: city || '',
            address,
            source: 'ip',
            accuracy: 'city',
            hasLocation: true
          }
        }
      }
    }
  } catch (error) {
    console.log('IP定位失败，尝试其他方式:', error.message)
  }

  if (AMap && AMap.Geolocation) {
    try {
      const position = await new Promise((resolve, reject) => {
        const geolocation = new AMap.Geolocation({
          enableHighAccuracy: true,
          timeout: 15000,
          zoomToAccuracy: true,
          GeoLocationFirst: false,
          noIpLocate: 0,
          noGeoLocation: 0,
          needAddress: false,
          extensions: 'base'
        })

        geolocation.getCurrentPosition((status, result) => {
          if (status === 'complete' && result?.position) {
            resolve(result)
          } else {
            reject(new Error(result?.message || '高德定位失败'))
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
        hasLocation: true
      }
    } catch (error) {
      console.log('高德定位失败，继续其他方式:', error.message)
    }
  }

  const cityFallback = await getCitySearchFallback({ AMap })
  if (cityFallback?.lng && cityFallback?.lat) {
    const address = await reverseGeocode({ lng: cityFallback.lng, lat: cityFallback.lat, AMap })
    if (typeof saveLastLocation === 'function') {
      saveLastLocation(cityFallback.lng, cityFallback.lat, cityFallback.source || 'city-fallback')
    }

    return {
      lng: cityFallback.lng,
      lat: cityFallback.lat,
      province: '',
      city: cityFallback.city || '',
      address,
      source: cityFallback.source || 'city-fallback',
      accuracy: 'city',
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
      hasLocation: true
    }
  }

  return null
}
