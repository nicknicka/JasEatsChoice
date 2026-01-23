import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { validateImageFile, readFileAsDataURL } from '../utils/imageValidator'
import { ERROR_MESSAGES, logger } from '../config/chatConfig'

/**
 * 图片上传处理
 * @returns {Object} 图片上传相关方法和状态
 */
export function useImageUpload() {
  const uploadedImages = ref([])
  const isUploading = ref(false)

  /**
   * 处理图片上传
   * @param {File} file - 文件对象
   * @param {Function} onSuccess - 成功回调
   * @param {Function} onError - 错误回调
   * @returns {Promise<void>}
   */
  const handleImageUpload = async (file, onSuccess, onError) => {
    // 验证文件
    const validation = validateImageFile(file)
    if (!validation.valid) {
      ElMessage.error(validation.error)
      if (onError) onError(new Error(validation.error))
      return
    }

    try {
      isUploading.value = true

      // 读取文件为DataURL
      const dataUrl = await readFileAsDataURL(file)

      // 添加到上传列表
      const imageItem = {
        id: Date.now(),
        url: dataUrl,
        file: file,
        name: file.name
      }

      uploadedImages.value.push(imageItem)

      ElMessage.success(ERROR_MESSAGES.UPLOAD_SUCCESS)
      logger.log('✅ 图片上传成功:', file.name)

      if (onSuccess) onSuccess(imageItem)
    } catch (error) {
      logger.error('❌ 图片上传失败:', error)
      ElMessage.error('图片上传失败')

      if (onError) onError(error)
    } finally {
      isUploading.value = false
    }
  }

  /**
   * 移除已上传的图片
   * @param {number} imageId - 图片ID
   */
  const removeUploadedImage = (imageId) => {
    const index = uploadedImages.value.findIndex(img => img.id === imageId)
    if (index > -1) {
      const removed = uploadedImages.value.splice(index, 1)[0]

      // 释放blob URL
      if (removed.url && removed.url.startsWith('blob:')) {
        URL.revokeObjectURL(removed.url)
      }

      logger.log('🗑️ 已移除图片:', removed.name)
    }
  }

  /**
   * 清空所有已上传的图片
   */
  const clearUploadedImages = () => {
    uploadedImages.value.forEach(img => {
      if (img.url && img.url.startsWith('blob:')) {
        URL.revokeObjectURL(img.url)
      }
    })
    uploadedImages.value = []
    logger.log('🗑️ 已清空所有图片')
  }

  /**
   * 获取第一张图片的文件对象
   * @returns {File|null}
   */
  const getFirstImageFile = () => {
    return uploadedImages.value.length > 0 ? uploadedImages.value[0].file : null
  }

  return {
    uploadedImages,
    isUploading,
    handleImageUpload,
    removeUploadedImage,
    clearUploadedImages,
    getFirstImageFile
  }
}
