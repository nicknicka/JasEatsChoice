/**
 * 卡路里记录相关API
 * 对接后端 CalorieRecordController
 * 基础路径: /calorie-records
 */
import { post } from '@/utils/request'

export const calorieApi = {
  /**
   * 创建卡路里记录
   * POST /calorie-records
   * @param {Object} data - 卡路里记录
   * @returns {Promise} 返回创建结果
   */
  createRecord: (data) => post('/calorie-records', data)
}

export default calorieApi
