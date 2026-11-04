import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { API_CONFIG } from '../config'
import { logger } from '../config/chatConfig'
import { useAuthStore } from '../store/authStore'

const selectedImage = ref(null)
const selectedFile = ref(null)
const recognitionLoading = ref(false)
const recognitionProgress = ref(0)
const recognitionResult = ref(null)
const recognitionError = ref(null)

let progressTimer = null
let runningTask = null

const normalizeRecognitionMessage = (result, fallbackMessage) => {
  return result?.message || result?.msg || result?.data?.reason || result?.reason || fallbackMessage
}

const isNonDishRecognitionResult = (result) => {
  if (!result) {
    return false
  }

  const payload = result.data || result
  return result.code === '4001' || result.code === 4001 || result.notDish === true || result.isDish === false || payload?.notDish === true || payload?.isDish === false
}

const mapRecognitionResult = (data) => ({
  isDish: data.isDish === true,
  name: data.name || '未知菜品',
  calories: Number(data.calories) || 0,
  protein: Number(data.protein) || 0,
  fat: Number(data.fat) || 0,
  carbs: Number(data.carbs) || 0,
  difficulty: data.difficulty || '中等',
  preparationTime: data.preparationTime || '30分钟',
  ingredients: Array.isArray(data.ingredients) ? data.ingredients : [],
  tags: Array.isArray(data.tags) ? data.tags : [],
  confidence: Number(data.confidence) || 0,
  nutritionScore: Number(data.nutritionScore) || 0,
  nutritionEstimated: data.nutritionEstimated === true,
  imageUrl: data.imageUrl || ''
})

const compressImageFile = (file) => new Promise((resolve, reject) => {
  const supportedTypes = ['image/jpeg', 'image/png', 'image/webp']
  const sourceType = file.type === 'image/jpg' ? 'image/jpeg' : file.type
  if (!supportedTypes.includes(sourceType)) {
    reject(new Error('仅支持 JPEG、PNG 或 WebP 图片'))
    return
  }

  const objectUrl = URL.createObjectURL(file)
  const image = new Image()
  image.onload = () => {
    const maxEdge = 1600
    const scale = Math.min(1, maxEdge / Math.max(image.naturalWidth, image.naturalHeight))
    const width = Math.max(1, Math.round(image.naturalWidth * scale))
    const height = Math.max(1, Math.round(image.naturalHeight * scale))
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const context = canvas.getContext('2d')
    context.drawImage(image, 0, 0, width, height)
    canvas.toBlob((blob) => {
      URL.revokeObjectURL(objectUrl)
      if (!blob) {
        reject(new Error('图片压缩失败，请重试'))
        return
      }
      const extension = sourceType.split('/')[1]
      resolve(new File([blob], `${file.name.replace(/\.[^.]+$/, '') || 'dish-image'}.${extension}`, {
        type: sourceType,
        lastModified: Date.now()
      }))
    }, sourceType, 0.82)
  }
  image.onerror = () => {
    URL.revokeObjectURL(objectUrl)
    reject(new Error('图片读取失败，请重新选择'))
  }
  image.src = objectUrl
})

const stopProgressTimer = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

const startProgressTimer = () => {
  stopProgressTimer()
  recognitionProgress.value = 0

  // 渐近式推进：越接近 99% 越慢，并保留最小步进，
  // 保证识别期间进度条始终有可见变化，不会卡在一个数值上。
  progressTimer = setInterval(() => {
    if (recognitionProgress.value >= 99) return
    const remaining = 99 - recognitionProgress.value
    recognitionProgress.value = Math.min(
      99,
      recognitionProgress.value + Math.max(0.2, remaining * 0.06)
    )
  }, 150)
}

const revokeSelectedImage = () => {
  if (selectedImage.value) {
    URL.revokeObjectURL(selectedImage.value)
  }
}

const setImageFile = (file) => {
  if (recognitionLoading.value) {
    ElMessage.info('菜品识别正在进行中，切换页面不会中断')
    return false
  }

  revokeSelectedImage()
  selectedFile.value = file
  selectedImage.value = URL.createObjectURL(file)
  recognitionResult.value = null
  recognitionError.value = null
  recognitionProgress.value = 0
  return true
}

const clearImage = () => {
  if (recognitionLoading.value) {
    ElMessage.info('菜品识别正在进行中，切换页面不会中断')
    return
  }

  revokeSelectedImage()
  selectedImage.value = null
  selectedFile.value = null
  recognitionResult.value = null
  recognitionError.value = null
  recognitionProgress.value = 0
}

const runRecognition = async (file) => {
  const compressedFile = await compressImageFile(file)
  logger.log('开始识别菜品，压缩后文件名:', compressedFile.name)

  const formData = new FormData()
  formData.append('image', compressedFile)

  const authStore = useAuthStore()
  const token = authStore.token || localStorage.getItem('admin_token')
  const headers = {}
  if (token) headers.Authorization = `Bearer ${token}`

  const apiUrl = API_CONFIG.baseURL + API_CONFIG.ai.recognizeDish
  const response = await fetch(apiUrl, {
    method: 'POST',
    headers,
    body: formData
  })
  const result = await response.json().catch(() => null)
  const responseData = result?.data || null
  const responseMessage = normalizeRecognitionMessage(result, `请求失败 (${response.status})`)

  if (!response.ok) {
    throw new Error(responseMessage)
  }

  if (isNonDishRecognitionResult(result) || responseData?.isDish === false) {
    const error = new Error(responseMessage || '请上传食物图片')
    error.nonDish = true
    throw error
  }

  if ((result?.success === true || result?.code === '200') && responseData?.isDish === true) {
    recognitionProgress.value = 100
    recognitionResult.value = mapRecognitionResult(responseData)
    recognitionError.value = null
    ElMessage.success('识别成功！')
    logger.log('✅ 菜品识别完成:', recognitionResult.value.name)
    return
  }

  throw new Error(responseMessage || '识别失败，请稍后重试')
}

const recognizeDish = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先上传菜品图片')
    return
  }

  if (recognitionLoading.value) {
    ElMessage.info('菜品识别正在进行中，切换页面不会中断')
    return runningTask
  }

  const file = selectedFile.value
  recognitionLoading.value = true
  recognitionResult.value = null
  recognitionError.value = null
  startProgressTimer()

  runningTask = runRecognition(file)
    .catch((error) => {
      recognitionProgress.value = 0
      const errorMessage = error?.message || '识别失败，请稍后重试'
      recognitionError.value = {
        message: errorMessage,
        nonDish: error?.nonDish === true
      }
      if (error?.nonDish) {
        ElMessage.warning(errorMessage)
        logger.warn('⚠️ 非菜品图片:', errorMessage)
        return
      }

      ElMessage.error('识别失败：' + errorMessage)
      logger.error('❌ 菜品识别失败:', error)
    })
    .finally(() => {
      stopProgressTimer()
      recognitionLoading.value = false
      runningTask = null
    })

  return runningTask
}

const reRecognize = () => {
  recognitionResult.value = null
  recognitionError.value = null
  return recognizeDish()
}

export function useDishRecognitionTask() {
  return {
    selectedImage,
    selectedFile,
    recognitionLoading,
    recognitionProgress,
    recognitionResult,
    recognitionError,
    setImageFile,
    clearImage,
    recognizeDish,
    reRecognize
  }
}
