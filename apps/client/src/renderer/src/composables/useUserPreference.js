import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../config/index'
import { useAuthStore } from '../store/authStore'
import { ERROR_MESSAGES, logger } from '../config/chatConfig'
import { handleApiError } from '../utils/errorHandler'

/**
 * 用户偏好设置管理
 * @returns {Object} 用户偏好相关方法和状态
 */
export function useUserPreference() {
  const authStore = useAuthStore()

  // AI个性化数据开关状态（默认关闭 - 隐私保护原则）
  const aiPersonalDataEnabled = ref(false)

  /**
   * 获取用户ID
   * @returns {string}
   */
  const getUserId = () => {
    return String(authStore.userId || '1')
  }

  /**
   * 加载用户偏好设置
   * @returns {Promise<void>}
   */
  const loadUserPreference = async () => {
    try {
      const userId = getUserId()
      logger.log('📥 加载用户偏好设置，userId:', userId)

      const response = await axios.get(`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`)

      if (response.data && response.data.data) {
        // 只有明确设置为 true 时才启用（隐私保护原则）
        aiPersonalDataEnabled.value = response.data.data.enableAiPersonalData === true
        logger.log('✅ 用户偏好加载成功:', aiPersonalDataEnabled.value)
      }
    } catch (error) {
      logger.error('❌ 加载用户偏好失败:', error)
      // 失败时使用默认值（隐私保护原则：默认未授权）
      aiPersonalDataEnabled.value = false
    }
  }

  /**
   * 切换个性化数据开关
   * @param {boolean} value - 新值
   * @returns {Promise<void>}
   */
  const handlePersonalDataToggle = async (value) => {
    try {
      const userId = getUserId()
      logger.log('🔄 切换AI个性化数据:', value)

      await axios.put(`${API_CONFIG.baseURL}/v1/users/${userId}/preferences`, {
        enableAiPersonalData: value
      })

      ElMessage.success(value ? ERROR_MESSAGES.PERSONAL_ENABLED : ERROR_MESSAGES.PERSONAL_DISABLED)
      logger.log('✅ 用户偏好更新成功')
    } catch (error) {
      logger.error('❌ 更新偏好设置失败:', error)
      ElMessage.error(ERROR_MESSAGES.SETTING_SAVE_FAILED)

      // 恢复原状态
      aiPersonalDataEnabled.value = !value
    }
  }

  return {
    aiPersonalDataEnabled,
    loadUserPreference,
    handlePersonalDataToggle
  }
}
