/**
 * 高德地图后端代理 API
 * 通过后端调用高德地图接口，避免安全密钥暴露给前端
 */
import api from '../utils/api'

export default {
  /**
   * 地址搜索
   * @param {string} keywords - 搜索关键词
   * @param {string} city - 城市（可选，默认全国）
   * @returns {Promise} 搜索结果列表
   */
  searchAddress(keywords, city = null) {
    const params = { keywords }
    if (city) {
      params.city = city
    }
    return api.get('/v1/amap/search', { params })
  },

  /**
   * 地理编码（地址 → 坐标）
   * @param {string} address - 地址
   * @param {string} city - 城市（可选）
   * @returns {Promise} 坐标信息
   */
  geocode(address, city = null) {
    const params = { address }
    if (city) {
      params.city = city
    }
    return api.get('/v1/amap/geocode', { params })
  },

  /**
   * 逆地理编码（坐标 → 地址）
   * @param {string} lng - 经度
   * @param {string} lat - 纬度
   * @returns {Promise} 地址信息
   */
  regeocode(lng, lat) {
    return api.get('/v1/amap/regeocode', {
      params: { lng, lat }
    })
  },

  /**
   * IP 定位（通过 IP 地址获取大概位置）
   * @returns {Promise} 位置信息（城市级别精度）
   */
  ipLocation() {
    return api.get('/v1/amap/ip/location')
  }
}
