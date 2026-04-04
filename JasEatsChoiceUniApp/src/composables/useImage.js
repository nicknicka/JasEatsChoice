/**
 * 图片URL处理composable
 * 统一处理相对路径和绝对路径的图片URL
 */
import { computed } from 'vue'

// 服务器配置（注意：需要包含/api前缀，因为后端context-path是/api）
const IMAGE_SERVER = 'http://192.168.137.188:7777/api'

/**
 * 处理单个图片URL
 * @param {string} url - 原始图片URL
 * @returns {string} 处理后的完整URL
 */
export const processImageUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return url || ''
  }

  // 如果已经是完整URL（http/https开头），直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
    // 开发环境下替换localhost为局域网IP
    if (url.includes('localhost')) {
      return url.replace('localhost', '192.168.137.188')
    }
    return url
  }

  // 相对路径：添加服务器地址
  if (url.startsWith('/')) {
    return IMAGE_SERVER + url
  }

  // 其他情况：直接返回
  return url
}

/**
 * 处理图片列表URL
 * @param {Array} images - 图片URL数组
 * @returns {Array} 处理后的图片URL数组
 */
export const processImageUrls = (images) => {
  if (!Array.isArray(images)) {
    return []
  }
  return images.map(processImageUrl)
}

/**
 * 创建图片URL的computed属性
 * @param {import('vue').Ref<string>} urlRef - 图片URL的ref
 * @returns {import('vue').ComputedRef<string>} 处理后的图片URL
 */
export const useImageUrl = (urlRef) => {
  return computed(() => processImageUrl(urlRef.value))
}

/**
 * 创建图片URL列表的computed属性
 * @param {import('vue').Ref<Array>} urlsRef - 图片URL数组的ref
 * @returns {import('vue').ComputedRef<Array>} 处理后的图片URL数组
 */
export const useImageUrls = (urlsRef) => {
  return computed(() => processImageUrls(urlsRef.value))
}

export default {
  processImageUrl,
  processImageUrls,
  useImageUrl,
  useImageUrls
}
