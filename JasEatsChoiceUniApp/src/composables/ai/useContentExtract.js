/**
 * Composable: useContentExtract
 * 用途：AI内容提取逻辑
 * 包含：文件上传、内容提取、结果处理
 * 创建时间：2026-03-20
 */
import { ref, computed } from 'vue'

export function useContentExtract(userStore) {
  // 提取方式
  const extractMethod = ref('image')
  const uploadedFile = ref('')
  const articleUrl = ref('')
  const urlInput = ref(false)

  // 提取选项
  const extractOptions = ref({
    dishName: true,
    ingredients: true,
    steps: true,
    nutrition: false
  })

  // 提取状态
  const extracting = ref(false)
  const extractResult = ref(null)

  // 营养列表（用于 NutritionGrid 组件）
  const nutritionList = computed(() => {
    if (!extractResult.value?.nutrition) return []
    const nutrition = extractResult.value.nutrition
    return [
      { icon: '🔥', value: nutrition.calories, label: '卡路里' },
      { icon: '💪', value: nutrition.protein + 'g', label: '蛋白质' },
      { icon: '🍞', value: nutrition.carbs + 'g', label: '碳水' },
      { icon: '🧈', value: nutrition.fat + 'g', label: '脂肪' }
    ]
  })

  // 是否可以提取
  const canExtract = computed(() => {
    return uploadedFile.value && !extracting.value
  })

  /**
   * 选择提取方式
   */
  const selectMethod = (method) => {
    extractMethod.value = method
    uploadedFile.value = ''
    articleUrl.value = ''
    extractResult.value = null
  }

  /**
   * 获取方式文本
   */
  const getMethodText = () => {
    const textMap = {
      image: '菜品图片',
      video: '美食视频',
      text: '文章链接'
    }
    return textMap[extractMethod.value]
  }

  /**
   * 获取提示文本
   */
  const getMethodHint = () => {
    const hintMap = {
      image: '支持JPG、PNG格式，最大5MB',
      video: '支持MP4格式，最大50MB',
      text: '支持微信公众号、知乎等平台'
    }
    return hintMap[extractMethod.value]
  }

  /**
   * 选择文件
   */
  const chooseFile = () => {
    if (extractMethod.value === 'text') {
      showUrlInput()
      return
    }

    if (extractMethod.value === 'image') {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          uploadedFile.value = res.tempFilePaths[0]
        }
      })
    } else if (extractMethod.value === 'video') {
      uni.chooseVideo({
        sourceType: ['album', 'camera'],
        maxDuration: 300,
        success: (res) => {
          uploadedFile.value = res.tempFilePath
        }
      })
    }
  }

  /**
   * 显示URL输入框
   */
  const showUrlInput = () => {
    urlInput.value = true
  }

  /**
   * 处理URL提交
   */
  const handleUrlSubmit = () => {
    if (!articleUrl.value) {
      uni.showToast({ title: '请输入文章链接', icon: 'none' })
      return
    }

    const urlPattern = /^https?:\/\/.+/
    if (!urlPattern.test(articleUrl.value)) {
      uni.showToast({ title: '请输入有效的链接', icon: 'none' })
      return
    }

    uploadedFile.value = articleUrl.value
    urlInput.value = false
  }

  /**
   * 移除文件
   */
  const removeFile = () => {
    uni.showModal({
      title: '提示',
      content: '确定要移除已上传的文件吗？',
      success: (res) => {
        if (res.confirm) {
          uploadedFile.value = ''
          extractResult.value = null
        }
      }
    })
  }

  /**
   * 切换提取选项
   */
  const toggleOption = (option) => {
    extractOptions.value[option] = !extractOptions.value[option]
  }

  /**
   * 开始提取
   */
  const startExtract = async () => {
    if (!userStore.isLogin) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    extracting.value = true

    try {
      const extractData = {
        type: extractMethod.value,
        url: extractMethod.value === 'text' ? uploadedFile.value : undefined,
        options: extractOptions.value
      }

      // 如果是图片或视频，需要先上传
      if (extractMethod.value === 'image' || extractMethod.value === 'video') {
        try {
          uni.showLoading({ title: '正在上传文件...' })

          const { upload } = await import('@/utils/request')
          const uploadRes = await upload('/api/upload/file', uploadedFile.value, {
            type: extractMethod.value
          })

          uni.hideLoading()

          if (uploadRes && uploadRes.url) {
            extractData.fileUrl = uploadRes.url
            extractData.url = uploadRes.url
          } else {
            throw new Error('上传失败，未返回文件URL')
          }

          uni.showToast({ title: '上传成功', icon: 'success', duration: 1000 })
        } catch (error) {
          console.error('文件上传失败:', error)
          uni.hideLoading()
          throw new Error('文件上传失败：' + error.message)
        }
      }

      // 调用AI提取API
      const { aiApi } = await import('@/api')
      const res = await aiApi.extractContent(extractData)

      if (res.data) {
        extractResult.value = {
          dishName: res.data.dishName || '',
          ingredients: res.data.ingredients || [],
          steps: res.data.steps || [],
          nutrition: res.data.nutrition || {},
          confidence: res.data.confidence || 0
        }

        uni.showToast({ title: '提取成功', icon: 'success' })
      } else {
        throw new Error('提取失败')
      }
    } catch (error) {
      console.error('提取失败:', error)

      // 使用模拟数据
      extractResult.value = {
        dishName: '宫保鸡丁',
        ingredients: [
          { name: '鸡胸肉', amount: '300g' },
          { name: '花生米', amount: '50g' },
          { name: '干辣椒', amount: '10个' },
          { name: '花椒', amount: '适量' }
        ],
        steps: [
          '鸡胸肉切丁，用料酒、生抽腌制15分钟',
          '花生米炸酥脆，盛起备用',
          '热锅下油，爆香花椒和干辣椒'
        ],
        nutrition: {
          calories: '280',
          protein: '25',
          carbs: '12',
          fat: '18'
        },
        confidence: 95
      }

      uni.showToast({ title: '提取失败，已使用示例数据', icon: 'none' })
    } finally {
      extracting.value = false
    }
  }

  /**
   * 复制结果
   */
  const copyResult = () => {
    let text = ''
    if (extractResult.value.dishName) {
      text += `菜品名称：${extractResult.value.dishName}\n\n`
    }
    if (extractResult.value.ingredients) {
      text += `食材清单：\n`
      extractResult.value.ingredients.forEach(item => {
        text += `- ${item.name} ${item.amount}\n`
      })
      text += '\n'
    }
    if (extractResult.value.steps) {
      text += `制作步骤：\n`
      extractResult.value.steps.forEach((step, index) => {
        text += `${index + 1}. ${step}\n`
      })
    }

    uni.setClipboardData({
      data: text,
      success: () => {
        uni.showToast({ title: '已复制到剪贴板', icon: 'success' })
      }
    })
  }

  /**
   * 保存为食谱
   */
  const saveAsRecipe = async () => {
    uni.showModal({
      title: '保存为食谱',
      content: '确认将提取结果保存为食谱吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            uni.showLoading({ title: '保存中...' })

            const { recipeApi } = await import('@/api')

            const recipeData = {
              recipeName: extractResult.value.dishName,
              ingredients: extractResult.value.ingredients.map(ing => ({
                ingredientName: ing.name,
                amount: ing.amount
              })),
              steps: extractResult.value.steps.map((step, index) => ({
                stepOrder: index + 1,
                description: step.description || step
              })),
              nutritionInfo: extractResult.value.nutrition,
              tags: ['AI提取', extractResult.value.dishName]
            }

            const saveRes = await recipeApi.createRecipe(recipeData)

            uni.hideLoading()

            if (saveRes && (saveRes.code === 200 || saveRes.recipeId || saveRes.id)) {
              const recipeId = saveRes.recipeId || saveRes.id || saveRes.data?.recipeId

              uni.showToast({ title: '保存成功', icon: 'success', duration: 2000 })

              setTimeout(() => {
                if (recipeId) {
                  uni.redirectTo({ url: `/pages-user/recipe/detail/index?id=${recipeId}` })
                } else {
                  uni.navigateBack()
                }
              }, 1500)
            } else {
              throw new Error(saveRes.message || '保存失败')
            }
          } catch (error) {
            console.error('保存食谱失败:', error)
            uni.hideLoading()
            uni.showToast({ title: error.message || '保存失败', icon: 'none' })
          }
        }
      }
    })
  }

  return {
    extractMethod,
    uploadedFile,
    articleUrl,
    urlInput,
    extractOptions,
    extracting,
    extractResult,
    nutritionList,
    canExtract,
    selectMethod,
    getMethodText,
    getMethodHint,
    chooseFile,
    showUrlInput,
    handleUrlSubmit,
    removeFile,
    toggleOption,
    startExtract,
    copyResult,
    saveAsRecipe
  }
}
