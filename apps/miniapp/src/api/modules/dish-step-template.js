/**
 * 菜品步骤模板API
 * 对接后端 DishStepTemplateController
 * 基础路径: /v1/dishes/step-templates
 */
import { get, post, put, del } from '@/utils/request'

export const dishStepTemplateApi = {
  /**
   * DISH-008: 获取步骤模板列表
   * GET /v1/dishes/step-templates
   * @param {Object} params - 查询参数
   * @param {string} params.merchantId - 商家ID
   * @param {string} params.category - 菜品分类（可选）
   */
  getList: (params) => get('/v1/dishes/step-templates', params),

  /**
   * DISH-008: 获取步骤模板详情
   * GET /v1/dishes/step-templates/{templateId}
   * @param {string} templateId - 模板ID
   */
  getDetail: (templateId) => get(`/v1/dishes/step-templates/${templateId}`),

  /**
   * DISH-009: 创建步骤模板
   * POST /v1/dishes/step-templates
   * @param {Object} data - 模板数据
   * @param {string} data.merchantId - 商家ID
   * @param {string} data.name - 模板名称
   * @param {string} data.category - 菜品分类
   * @param {string} data.icon - 模板图标
   * @param {Array} data.steps - 步骤列表
   * @param {number} data.totalDuration - 总时长
   */
  create: (data) => post('/v1/dishes/step-templates', data),

  /**
   * DISH-009: 更新步骤模板
   * PUT /v1/dishes/step-templates/{templateId}
   * @param {string} templateId - 模板ID
   * @param {Object} data - 模板数据
   */
  update: (templateId, data) => put(`/v1/dishes/step-templates/${templateId}`, data),

  /**
   * 删除步骤模板
   * DELETE /v1/dishes/step-templates/{templateId}
   * @param {string} templateId - 模板ID
   */
  delete: (templateId) => del(`/v1/dishes/step-templates/${templateId}`),

  /**
   * 应用模板到菜品
   * POST /v1/dishes/step-templates/{templateId}/apply
   * @param {string} templateId - 模板ID
   * @param {Object} data - 应用数据
   * @param {string} data.dishId - 菜品ID
   */
  apply: (templateId, data) => post(`/v1/dishes/step-templates/${templateId}/apply`, data)
}

export default dishStepTemplateApi
