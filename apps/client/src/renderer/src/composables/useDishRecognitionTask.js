import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { API_CONFIG } from '../config'
import { logger } from '../config/chatConfig'

const selectedImage = ref(null)
const selectedFile = ref(null)
const recognitionLoading = ref(false)
const recognitionProgress = ref(0)
const recognitionResult = ref(null)

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
  nutritionScore: Number(data.nutritionScore) || 0
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

  progressTimer = setInterval(() => {
    if (recognitionProgress.value < 90) {
      recognitionProgress.value += 10
    }
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
  recognitionProgress.value = 0
}

const runRecognition = async (file) => {
  logger.log('开始识别菜品，文件名:', file.name)

  const formData = new FormData()
  formData.append('image', file)

  const apiUrl = API_CONFIG.baseURL + API_CONFIG.ai.recognizeDish
  console.log('🔍 开始识别菜品，API URL:', apiUrl)
  console.log('📤 发送图片:', file.name, file.size, 'bytes')

  const response = await fetch(apiUrl, {
    method: 'POST',
    body: formData
  })

  console.log('📡 后端响应状态:', response.status, response.statusText)

  if (!response.ok) {
    const errorText = await response.text()
    console.error('❌ 请求失败:', response.status, errorText)
    throw new Error(`请求失败 (${response.status}): ${errorText}`)
  }

  const result = await response.json()
  const responseData = result?.data || null
  const nonDishMessage = normalizeRecognitionMessage(result, '请上传菜品图片')

  if (isNonDishRecognitionResult(result) || responseData?.isDish === false) {
    recognitionProgress.value = 0
    recognitionResult.value = null
    ElMessage.warning(nonDishMessage || '请上传菜品图片')
    logger.warn('⚠️ 非菜品图片:', nonDishMessage)
    return
  }

  if ((result.success === true || result.code === '200') && responseData) {
    recognitionProgress.value = 100
    recognitionResult.value = mapRecognitionResult(responseData)
    ElMessage.success('识别成功！')
    logger.log('✅ 菜品识别完成:', recognitionResult.value.name)
    return
  }

  throw new Error(normalizeRecognitionMessage(result, '识别失败'))
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
  startProgressTimer()

  runningTask = runRecognition(file)
    .catch((error) => {
      recognitionProgress.value = 0
      const errorMessage = error?.message || '识别失败'
      if (/菜品|食物图片/.test(errorMessage)) {
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
  return recognizeDish()
}

export function useDishRecognitionTask() {
  return {
    selectedImage,
    selectedFile,
    recognitionLoading,
    recognitionProgress,
    recognitionResult,
    setImageFile,
    clearImage,
    recognizeDish,
    reRecognize
  }
}
