/**
 * 内容提取相关API
 */
import api from '../utils/api'

export default {
  /**
   * 获取内容源列表
   */
  getSources() {
    return api.get('/v1/content-extraction/sources')
  },

  /**
   * 创建内容源
   */
  createSource(data) {
    return api.post('/v1/content-extraction/source', data)
  },

  /**
   * 获取提取详情
   */
  getExtractionDetail(extractionId) {
    return api.get(`/v1/content-extraction/extraction/${extractionId}`)
  },

  /**
   * 更新提取内容
   */
  updateExtraction(extractionId, data) {
    return api.put(`/v1/content-extraction/extraction`, data)
  },

  /**
   * 重新提取
   */
  reExtract(sourceId) {
    return api.post(`/v1/content-extraction/source/${sourceId}/re-extract`)
  },

  /**
   * 删除内容源
   */
  deleteSource(sourceId) {
    return api.delete(`/v1/content-extraction/source/${sourceId}`)
  },

  /**
   * 发布为食谱
   */
  publishAsRecipe(extractionId, data) {
    return api.post(`/v1/content-extraction/extraction/${extractionId}/publish`, data)
  }
}
