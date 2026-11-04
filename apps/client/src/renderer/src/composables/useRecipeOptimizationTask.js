import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { API_CONFIG } from '../config'
import { logger } from '../config/chatConfig'
import { validateRecipe } from '../utils/imageValidator'
import { readRecipeEventStream } from '../utils/recipeOptimizationStream'
import { useAuthStore } from '../store/authStore'

const DEFAULT_RECIPE_STREAM_TIMEOUT_MS = 120000
const STREAM_TIMEOUT_GRACE_MS = 5000
const AI_ERROR_MESSAGES = {
  INVALID_REQUEST: '请求参数有误，请检查后重试',
  AI_NOT_CONFIGURED: 'AI 服务尚未配置，请联系管理员',
  AI_CALL_FAILED: 'AI 服务暂时不可用，请稍后重试',
  AI_RESULT_PARSE_FAILED: 'AI 返回内容无法解析，请稍后重试',
  AI_TIMEOUT: '生成超时，请稍后重试'
}

const originalRecipe = ref('')
const optimizedRecipe = ref(null)
const recipeGoal = ref('balanced')
const optimizationLoading = ref(false)
const smoothProgress = ref(0)
const progressStatus = ref('')
const streamedContent = ref('')
const elapsedMs = ref(null)
const optimizationError = ref('')
const optimizationCancelled = ref(false)

let progressTimer = null
let runningTask = null
let activeController = null
let timeoutTimer = null
let abortReason = ''
let progressMessageLocked = false
let recipeStreamTimeoutMs = DEFAULT_RECIPE_STREAM_TIMEOUT_MS

class RecipeStreamError extends Error {
  constructor(code, message) {
    super(message)
    this.name = 'RecipeStreamError'
    this.code = code
  }
}

const normalizeTimeout = (value) => {
  const timeout = Number(value)
  return Number.isFinite(timeout) && timeout > 0
    ? timeout
    : DEFAULT_RECIPE_STREAM_TIMEOUT_MS
}

export function setRecipeStreamTimeoutMs(value) {
  recipeStreamTimeoutMs = normalizeTimeout(value)
}

const startSmoothProgress = () => {
  if (progressTimer) clearInterval(progressTimer)

  smoothProgress.value = 0
  progressStatus.value = '正在分析食谱内容...'
  progressMessageLocked = false

  progressTimer = setInterval(() => {
    if (smoothProgress.value < 15) {
      smoothProgress.value += 2
    } else if (smoothProgress.value < 85) {
      smoothProgress.value += 0.3
    } else if (smoothProgress.value < 95) {
      smoothProgress.value += 0.05
    }

    smoothProgress.value = Math.min(smoothProgress.value, 95)

    if (!progressMessageLocked) {
      if (smoothProgress.value >= 15 && smoothProgress.value < 85) {
        progressStatus.value = 'AI 正在精心优化您的食谱...'
      } else if (smoothProgress.value >= 85) {
        progressStatus.value = '正在生成优化结果...'
      }
    }
  }, 200)
}

const stopSmoothProgress = (completed) => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
  if (completed) {
    smoothProgress.value = 100
    progressStatus.value = '优化完成'
  }
}

const buildOptimizedRecipe = (result, recipeText) => {
  const recipe = result?.recipe
  if (!recipe) return null

  const original = result.nutrition?.original || {}
  const optimized = result.nutrition?.optimized || {}
  const num = (value) => Number(value) || 0

  return {
    original: recipeText,
    goal: result.goal || recipeGoal.value,
    optimized: `推荐食谱：${recipe.name || 'AI 优化食谱'}
难度：${recipe.difficulty || '-'}
卡路里：${num(optimized.calorie)}大卡
蛋白质：${num(optimized.protein)}g
脂肪：${num(optimized.fat)}g
碳水化合物：${num(optimized.carb)}g
食材：${recipe.ingredients || ''}
步骤：${recipe.steps || ''}`,
    improvements: Array.isArray(result.improvements)
      ? result.improvements.map((item) =>
          typeof item === 'string'
            ? item
            : [item.title, item.detail].filter(Boolean).join('：')
        )
      : [],
    nutrition: { original, optimized },
    estimationNote: result.estimationNote || '',
    rawData: {
      name: recipe.name,
      difficulty: recipe.difficulty,
      ingredients: recipe.ingredients,
      steps: recipe.steps,
      calorie: num(optimized.calorie),
      protein: num(optimized.protein),
      fat: num(optimized.fat),
      carb: num(optimized.carb)
    }
  }
}

const resolveAiErrorMessage = (data) =>
  data?.message || AI_ERROR_MESSAGES[data?.code] || 'AI 生成失败，请稍后重试'

const readHttpErrorPayload = async (response) => {
  const text = await response.text()
  if (!text) return {}

  try {
    const payload = JSON.parse(text)
    return {
      code: payload?.code,
      message: payload?.message || payload?.data?.message
    }
  } catch {
    return {}
  }
}

const createHttpError = async (response) => {
  const payload = await readHttpErrorPayload(response)

  if (response.status === 401) {
    return new RecipeStreamError('UNAUTHORIZED', '登录状态已失效，请重新登录')
  }
  if (response.status === 403) {
    return new RecipeStreamError('FORBIDDEN', payload.message || '没有权限使用食谱优化')
  }
  if (response.status === 429) {
    return new RecipeStreamError(
      payload.code || 'RATE_LIMITED',
      payload.message || 'AI 使用次数已达上限，请稍后再试'
    )
  }
  if (response.status === 503 && payload.code === 'AI_NOT_CONFIGURED') {
    return new RecipeStreamError('AI_NOT_CONFIGURED', resolveAiErrorMessage(payload))
  }

  return new RecipeStreamError(
    payload.code || 'AI_CALL_FAILED',
    payload.message || resolveAiErrorMessage({ code: payload.code })
  )
}

const createAbortError = () => {
  if (abortReason === 'timeout') {
    const seconds = Math.round(recipeStreamTimeoutMs / 1000)
    return new RecipeStreamError(
      'AI_TIMEOUT',
      `生成超时（超过 ${seconds} 秒未完成），请稍后重试`
    )
  }
  // abortReason 只会在超时或用户取消时被设置（见 runOptimization/cancelOptimization），非超时即取消
  const error = new Error('已取消优化')
  error.name = 'RecipeOptimizationCancelled'
  return error
}

const normalizeStreamError = (error) => {
  if (error instanceof RecipeStreamError) return error
  if (error?.message?.includes('SSE 事件数据')) {
    return new RecipeStreamError('AI_RESULT_PARSE_FAILED', AI_ERROR_MESSAGES.AI_RESULT_PARSE_FAILED)
  }
  return new RecipeStreamError(
    'AI_CALL_FAILED',
    error?.message || 'AI 服务连接失败，请稍后重试'
  )
}

export function useRecipeOptimizationTask(options = {}) {
  const authStore = useAuthStore()
  if (options.recipeStreamTimeoutMs !== undefined) {
    setRecipeStreamTimeoutMs(options.recipeStreamTimeoutMs)
  }

  const runOptimization = async (recipeText) => {
    const token = authStore.token || localStorage.getItem('admin_token')
    const controller = new AbortController()
    activeController = controller
    abortReason = ''

    timeoutTimer = setTimeout(() => {
      abortReason = 'timeout'
      controller.abort()
    }, recipeStreamTimeoutMs + STREAM_TIMEOUT_GRACE_MS)

    try {
      const headers = {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream'
      }
      if (token) headers.Authorization = `Bearer ${token}`

      const response = await fetch(API_CONFIG.baseURL + API_CONFIG.ai.recipeStream, {
        method: 'POST',
        headers,
        body: JSON.stringify({ recipe: recipeText, goal: recipeGoal.value }),
        signal: controller.signal
      })

      if (!response.ok) {
        throw await createHttpError(response)
      }
      if (!response.body) {
        throw new RecipeStreamError('AI_CALL_FAILED', 'AI 未返回可读取的流式内容')
      }

      let streamError = null
      let resultReceived = false
      let doneReceived = false

      await readRecipeEventStream(response.body.getReader(), ({ event, data }) => {
        if (event === 'progress') {
          if (data?.message) {
            progressStatus.value = data.message
            progressMessageLocked = true
          }
          return
        }

        if (event === 'token') {
          if (typeof data?.content === 'string') {
            streamedContent.value += data.content
          }
          return
        }

        if (event === 'result') {
          const nextRecipe = buildOptimizedRecipe(data?.result, recipeText)
          if (!nextRecipe) {
            streamError = new RecipeStreamError(
              'AI_RESULT_PARSE_FAILED',
              AI_ERROR_MESSAGES.AI_RESULT_PARSE_FAILED
            )
            return
          }
          optimizedRecipe.value = nextRecipe
          resultReceived = true
          return
        }

        if (event === 'error') {
          streamError = new RecipeStreamError(data?.code || 'AI_CALL_FAILED', resolveAiErrorMessage(data))
          progressStatus.value = streamError.message
          return
        }

        if (event === 'done') {
          doneReceived = true
          const elapsed = Number(data?.elapsedMs)
          if (Number.isFinite(elapsed) && elapsed >= 0) elapsedMs.value = elapsed
        }
      })

      if (streamError) throw streamError
      if (!resultReceived) {
        throw new RecipeStreamError(
          'AI_RESULT_PARSE_FAILED',
          AI_ERROR_MESSAGES.AI_RESULT_PARSE_FAILED
        )
      }
      if (!doneReceived) {
        throw new RecipeStreamError('AI_CALL_FAILED', 'AI 连接提前结束，请稍后重试')
      }

      stopSmoothProgress(true)
    } catch (error) {
      if (controller.signal.aborted) {
        throw createAbortError()
      }
      throw normalizeStreamError(error)
    } finally {
      if (timeoutTimer) {
        clearTimeout(timeoutTimer)
        timeoutTimer = null
      }
      if (activeController === controller) activeController = null
    }
  }

  const optimizeRecipe = async () => {
    const validation = validateRecipe(originalRecipe.value)
    if (!validation.valid) {
      ElMessage.warning(validation.error)
      return
    }

    if (optimizationLoading.value) {
      ElMessage.info('食谱优化正在进行中，切换页面不会中断')
      return runningTask
    }

    const recipeText = originalRecipe.value
    optimizationLoading.value = true
    optimizedRecipe.value = null
    streamedContent.value = ''
    elapsedMs.value = null
    optimizationError.value = ''
    optimizationCancelled.value = false
    startSmoothProgress()

    runningTask = runOptimization(recipeText)
      .catch((error) => {
        const cancelled = error?.name === 'RecipeOptimizationCancelled'
        const message = cancelled
          ? '已取消优化'
          : error?.message || '食谱优化失败，请稍后重试'

        stopSmoothProgress(false)
        optimizedRecipe.value = null
        optimizationError.value = cancelled ? '' : message
        optimizationCancelled.value = cancelled
        logger.error('❌ 食谱优化失败:', error)
        if (cancelled) {
          ElMessage.info(message)
        } else {
          ElMessage.error(message)
        }
      })
      .finally(() => {
        optimizationLoading.value = false
        runningTask = null
      })

    return runningTask
  }

  const cancelOptimization = () => {
    if (!optimizationLoading.value || !activeController || activeController.signal.aborted) {
      return false
    }
    abortReason = 'cancelled'
    activeController.abort()
    return true
  }

  return {
    originalRecipe,
    optimizedRecipe,
    recipeGoal,
    optimizationLoading,
    smoothProgress,
    progressStatus,
    streamedContent,
    elapsedMs,
    optimizationError,
    optimizationCancelled,
    optimizeRecipe,
    cancelOptimization,
    setRecipeStreamTimeoutMs
  }
}
