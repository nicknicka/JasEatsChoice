import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { API_CONFIG } from '../config'
import { logger } from '../config/chatConfig'
import { handleApiError } from '../utils/errorHandler'
import { validateRecipe } from '../utils/imageValidator'
import { useStreamResponse } from './useStreamResponse'

const originalRecipe = ref('')
const optimizedRecipe = ref(null)
const recipeGoal = ref('balanced')
const optimizationLoading = ref(false)
const smoothProgress = ref(0)
const progressStatus = ref('')

let progressTimer = null
let runningTask = null
const { parseSSELine } = useStreamResponse()

const startSmoothProgress = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
  }

  smoothProgress.value = 0
  progressStatus.value = '正在分析食谱内容...'

  progressTimer = setInterval(() => {
    if (smoothProgress.value < 15) {
      smoothProgress.value += 2
    } else if (smoothProgress.value < 85) {
      smoothProgress.value += 0.3
    } else if (smoothProgress.value < 95) {
      smoothProgress.value += 0.05
    }

    smoothProgress.value = Math.min(smoothProgress.value, 95)

    if (smoothProgress.value >= 15 && smoothProgress.value < 85) {
      progressStatus.value = 'AI 正在精心优化您的食谱...'
    } else if (smoothProgress.value >= 85) {
      progressStatus.value = '正在生成优化结果...'
    }
  }, 200)
}

const stopSmoothProgress = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
  smoothProgress.value = 100
  progressStatus.value = '优化完成'
}

const runOptimization = async (recipeText) => {
  const response = await fetch(API_CONFIG.baseURL + API_CONFIG.ai.recipeStream, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream'
    },
    body: JSON.stringify({ recipe: recipeText, goal: recipeGoal.value })
  })

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''

  // SSE 流式读取的标准循环：靠 reader.read() 返回的 done 退出，不是死循环
  // eslint-disable-next-line no-constant-condition
  while (true) {
    const { done, value } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''

    for (const line of lines) {
      const parsedData = parseSSELine(line)
      if (!parsedData) continue

      if (parsedData.streaming === true && parsedData.content) {
        continue
      }

      const progressMessage = parsedData.message || parsedData.content
      if (parsedData.progress === true && progressMessage) {
        progressStatus.value = progressMessage
        continue
      }

      if (parsedData.done === true && parsedData.result) {
        stopSmoothProgress()
        const r = parsedData.result
        const recipe = r.recipe || {}
        if (r.recipe) {
          const original = r.nutrition?.original || {}
          const optimized = r.nutrition?.optimized || {}
          const num = (v) => Number(v) || 0
          optimizedRecipe.value = {
            original: recipeText,
            goal: r.goal || recipeGoal.value,
            optimized: `推荐食谱：${recipe.name || 'AI 优化食谱'}
难度：${recipe.difficulty || '-'}
卡路里：${num(optimized.calorie)}大卡
蛋白质：${num(optimized.protein)}g
脂肪：${num(optimized.fat)}g
碳水化合物：${num(optimized.carb)}g
食材：${recipe.ingredients || ''}
步骤：${recipe.steps || ''}`,
            improvements: Array.isArray(r.improvements)
              ? r.improvements.map((it) =>
                  typeof it === 'string'
                    ? it
                    : [it.title, it.detail].filter(Boolean).join('：')
                )
              : [],
            nutrition: { original, optimized },
            estimationNote: r.estimationNote || '',
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
          logger.log('✅ 食谱优化成功:', recipe.name)
        } else {
          optimizedRecipe.value = {
            original: recipeText,
            optimized: '优化失败：没有找到合适的优化食谱。',
            improvements: []
          }
        }
        continue
      }

      if (parsedData.error) {
        throw new Error(parsedData.message || '优化失败')
      }
    }
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
  startSmoothProgress()

  runningTask = runOptimization(recipeText)
    .catch((error) => {
      logger.error('❌ 食谱优化失败:', error)
      optimizedRecipe.value = {
        original: recipeText,
        optimized: handleApiError(error),
        improvements: []
      }
      ElMessage.error(handleApiError(error))
    })
    .finally(() => {
      stopSmoothProgress()
      setTimeout(() => {
        optimizationLoading.value = false
        runningTask = null
      }, 600)
    })

  return runningTask
}

export function useRecipeOptimizationTask() {
  return {
    originalRecipe,
    optimizedRecipe,
    recipeGoal,
    optimizationLoading,
    smoothProgress,
    progressStatus,
    optimizeRecipe
  }
}
