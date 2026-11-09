<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Box } from '@element-plus/icons-vue'
import { API_CONFIG } from '../../config'
import axios from 'axios'
import { useAuthStore } from '../../store/authStore'
import orderApi from '../../api/order'

// 导入自定义组件
import RecipeDetail from '../../components/RecipeDetail.vue'
import AddDish from '../../components/AddDish.vue'
import ImportMerchantDish from '../../components/ImportMerchantDish.vue'
import AddRecipe from '../../components/recipe/AddRecipe.vue'
import ReplaceDish from '../../components/ReplaceDish.vue'
import RecipeCard from '../../components/RecipeCard.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

// 获取认证信息
const authStore = useAuthStore()

// 订单接口字段兼容：后端可能返回订单菜品为不同字段名
const normalizeImportDish = (item) => {
  if (!item || typeof item !== 'object') {
    return null
  }

  const name = item.dishName || item.name || item.itemName || '未命名菜品'
  const calorieValue = item.calorie ?? item.calories ?? item.kcal ?? 0
  const quantity = item.quantity || 1
  return {
    name,
    quantity,
    nutrition: calorieValue ? `${calorieValue}kcal` : ''
  }
}

// 安全地反序列化JSON字符串
const safeParseArray = (value) => {
  if (!value) return []
  if (Array.isArray(value)) return value
  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return []
    }
  }
  return []
}

// 今日食谱数据
const todayRecipes = ref([])

// 批量操作相关变量
const selectedRecipes = ref([]) // 存储选中的食谱

// 添加菜品相关变量
const newDish = ref({
  name: '',
  ingredients: [],
  calories: 0,
  protein: 0,
  carbs: 0,
  fat: 0
})
const newIngredient = ref('') // 单个食材输入

// 营养摄入数据 - 从菜品数据计算得出
// TODO: 未来可以通过AI预测菜品营养数据，当前若菜品无数据则默认为0
const nutritionData = computed(() => {
  let totalCalories = 0
  let totalProtein = 0
  let totalCarbs = 0
  let totalFat = 0

  // 遍历筛选后的食谱和菜品，确保recipe和recipe.items存在
  filteredRecipes.value.forEach((recipe) => {
    if (recipe && recipe.items) {
      // 确保recipe.items是数组
      const items = Array.isArray(recipe.items)
        ? recipe.items
        : typeof recipe.items === 'string'
          ? JSON.parse(recipe.items)
          : []
      items.forEach((dish) => {
        // 如果菜品有营养数据则累加，否则默认为0
        totalCalories += dish?.calories || 0
        totalProtein += dish?.protein || 0
        totalCarbs += dish?.carbs || 0
        totalFat += dish?.fat || 0
      })
    }
  })

  return {
    calories: totalCalories,
    protein: totalProtein,
    carbs: totalCarbs,
    fat: totalFat
  }
})

// 筛选条件
const filters = ref({
  mealType: 'all'
})

// 加载今日食谱数据
const loadTodayRecipes = () => {
  // 确保有userId
  if (!authStore.userId) {
    console.error('加载今日食谱失败: 缺少userId')
    ElMessage.error('加载今日食谱失败: 用户未登录')
    todayRecipes.value = []
    return
  }

  axios
    .get(API_CONFIG.baseURL + API_CONFIG.recipe.today, {
      params: {
        userId: authStore.userId
      }
    })
    .then((response) => {
      console.log(response) ;
      if (
        response.data.data &&
        response.data.data.recipes &&
        response.data.data.recipes.length > 0
      ) {
        // console.log('加载今日食谱成功:', response.data.data.recipes);
        // 确保所有食谱都有items数组，并且移除任何null或无效的食谱
        todayRecipes.value = response.data.data.recipes
          .filter((recipe) => recipe && recipe.id) // 确保食谱存在且有id
          .map((recipe) => ({
            ...recipe,
            items: typeof recipe.items === 'string' ? JSON.parse(recipe.items) : recipe.items || []
          }))
        // nutritionData now computed from recipe items, no need for direct assignment
      } else {
        // 后端没有返回数据
        todayRecipes.value = []
      }
    })
    .catch((error) => {
      console.error('加载今日食谱失败:', error)
      // 请求失败时，也显示没有数据
      todayRecipes.value = []
      ElMessage.error('加载今日食谱失败')
    })
}

// 组件挂载时加载数据
onMounted(() => {
  loadTodayRecipes()
  loadImportOrders()
})

// 默认使用一列布局
const layoutType = ref('one-column')

// 模态框状态
const detailDialogVisible = ref(false)
const replaceDishVisible = ref(false)
const addDishVisible = ref(false)

// 当前选中的食谱和菜品
const selectedRecipe = ref(null)
const selectedDish = ref(null)

// 导入商家菜品对话框
const importMerchantDishVisible = ref(false)

// 导入订单数据（用于从订单生成食谱）
const orders = ref([])

const loadImportOrders = async () => {
  if (!authStore.userId) {
    ElMessage.error('加载订单失败：用户未登录')
    orders.value = []
    return
  }

  try {
    const response = await orderApi.getOrdersByUserId(authStore.userId)
    const orderList = response?.data?.data
    if (!Array.isArray(orderList)) {
      orders.value = []
      return
    }

    const normalizedOrders = await Promise.all(
      orderList.map(async (order) => {
        const totalPrice = Number(order.totalAmount ?? order.total ?? order.totalPrice ?? 0) || 0
        let dishes = []

        const dishSource = order.dishes ?? order.orderItems ?? order.items
        if (dishSource !== undefined) {
          dishes = safeParseArray(dishSource)
        } else {
          const orderDetail = await axios.get(`${API_CONFIG.baseURL}/v1/orders/${order.id}/dishes`)
          dishes = safeParseArray(orderDetail?.data?.data)
        }

        const normalizedDishes = dishes.map(normalizeImportDish).filter(Boolean)

        return {
          ...order,
          orderNo: order.orderNo || order.orderNumber || order.id,
          totalPrice,
          dishes: normalizedDishes
        }
      })
    )

    orders.value = normalizedOrders
  } catch (error) {
    console.error('加载订单失败:', error)
    orders.value = []
    ElMessage.error('加载订单失败')
  }
}

// 导入订单对话框
const importOrderVisible = ref(false)
const selectedOrder = ref(null)

// 添加食谱对话框
const addRecipeVisible = ref(false)

// 查看详情
const viewRecipeDetails = (recipe) => {
  selectedRecipe.value = recipe
  detailDialogVisible.value = true
}

// 统一处理食谱更新
const handleUpdateRecipe = (updatedRecipe) => {
  if (!updatedRecipe || !updatedRecipe.id) {
    console.error('更新食谱失败：无效的食谱数据')
    return
  }

  console.log('收到更新的食谱数据:', updatedRecipe)

  // 在todayRecipes数组中找到对应的食谱并更新
  const index = todayRecipes.value.findIndex((recipe) => recipe.id === updatedRecipe.id)
  if (index !== -1) {
    // 确保items字段正确解析
    const parsedRecipe = {
      ...updatedRecipe,
      items:
        typeof updatedRecipe.items === 'string'
          ? JSON.parse(updatedRecipe.items)
          : updatedRecipe.items || []
    }
    todayRecipes.value[index] = parsedRecipe

    // 更新selectedRecipe
    if (selectedRecipe.value && selectedRecipe.value.id === updatedRecipe.id) {
      selectedRecipe.value = parsedRecipe
    }

    console.log('食谱已在本地列表中更新')
  } else {
    console.warn('未找到对应的食谱:', updatedRecipe.id)
  }
}

// 替换菜品
const replaceDish = (recipe, dish) => {
  selectedRecipe.value = recipe
  selectedDish.value = dish
  replaceDishVisible.value = true
}

// 处理替换菜品点击（从 RecipeCard 组件）
const handleReplaceDishClick = ({ recipe, dish }) => {
  replaceDish(recipe, dish)
}

// 处理删除菜品点击（从 RecipeCard 组件）
const handleDeleteDishClick = ({ recipe, dish }) => {
  deleteDish(recipe, dish)
}

// 处理菜品替换
const handleReplaceDish = async ({ recipe, oldDish, newDish }) => {
  if (!recipe || !oldDish || !recipe.items) {
    console.error('替换菜品失败：无效的参数')
    return
  }

  // 先保存原菜品，以便失败时恢复
  const originalItems = [...recipe.items]

  try {
    // 找到并替换菜品
    const index = recipe.items.indexOf(oldDish)
    if (index !== -1) {
      // 替换菜品，保留营养数据
      recipe.items[index] = {
        name: newDish.name,
        ingredients: newDish.ingredients || [],
        calories: newDish.calories || newDish.calorie || 0,
        protein: newDish.protein || 0,
        carbs: newDish.carbs || 0,
        fat: newDish.fat || 0
      }

      // 调用后端API更新食谱 - 将items转换为JSON字符串
      const updateData = {
        ...recipe,
        items: JSON.stringify(recipe.items)
      }

      const response = await axios.put(
        API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id,
        updateData
      )

      // 更新本地数据 - 确保items字段已解析
      const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
      if (recipeIndex !== -1) {
        const updatedRecipe = {
          ...response.data.data,
          items:
            typeof response.data.data.items === 'string'
              ? JSON.parse(response.data.data.items)
              : response.data.data.items || []
        }
        todayRecipes.value[recipeIndex] = updatedRecipe
      }

      // 关闭对话框并重置状态
      replaceDishVisible.value = false
      selectedRecipe.value = null
      selectedDish.value = null
    }
  } catch (error) {
    console.error('替换菜品失败:', error)
    // 失败时恢复本地数据
    recipe.items = originalItems
    throw error // 让组件处理错误提示
  }
}

// 添加菜品
const addDish = (recipe) => {
  // 确保recipe.items是数组
  recipe.items = recipe.items || []
  selectedRecipe.value = recipe
  addDishVisible.value = true
}

// 确认添加菜品（已迁移到组件）
// const confirmAddDish = () => {

// 确认从订单导入食谱
const confirmImportOrder = () => {
  if (!selectedOrder.value) {
    ElMessage.warning('请选择要导入的订单')
    return
  }

  if (!selectedOrder.value.dishes || selectedOrder.value.dishes.length === 0) {
    ElMessage.warning('该订单暂无可导入的菜品')
    return
  }

  console.log('Selected order:', selectedOrder.value)
  try {
    // 创建新食谱数据
    const newRecipeData = {
      name: `订单-${selectedOrder.value.orderNo}`,
      type: 'dinner', // 默认类型，可根据实际情况调整
      items: selectedOrder.value.dishes.map((dish) => ({
        name: dish.name,
        ingredients: [],
        calories: 0,
        protein: 0,
        carbs: 0,
        fat: 0
      })),
      userId: authStore.userId
    }

    // 调用后端API添加食谱 - 将items转换为JSON字符串
    const newRecipeDataWithStringItems = {
      ...newRecipeData,
      items: JSON.stringify(newRecipeData.items)
    }

    axios
      .post(API_CONFIG.baseURL + API_CONFIG.recipe.add, newRecipeDataWithStringItems)
      .then((response) => {
        // 检查返回的数据是否有效
        if (response.data.data) {
          // 确保返回的食谱有items数组并已解析
          const newRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          // 将返回的食谱添加到本地列表
          todayRecipes.value.push(newRecipe)
          ElMessage.success('订单已成功导入为新食谱')
          importOrderVisible.value = false
          selectedOrder.value = null
        } else {
          ElMessage.error('导入订单失败: 服务器返回无效数据')
        }
      })
      .catch((error) => {
        console.error('导入订单失败:', error)
        ElMessage.error('导入订单失败')
      })
  } catch (error) {
    console.error('构造订单导入食谱失败:', error)
    ElMessage.error('订单数据异常，导入失败')
  }
}

// 确认添加菜品
const confirmAddDish = () => {
  if (selectedRecipe.value && newDish.value.name.trim()) {
    // 验证菜品名称格式
    if (!isValidDishName(newDish.value.name)) {
      ElMessage.error('菜品名称只能包含中文、英文、数字和常见符号')
      return
    }

    // 如果有食材，将菜品和食材一起保存
    const dishWithIngredients = {
      name: newDish.value.name,
      ingredients: [...newDish.value.ingredients],
      calories: newDish.value.calories,
      protein: newDish.value.protein,
      carbs: newDish.value.carbs,
      fat: newDish.value.fat
    }

    // 先添加到本地
    selectedRecipe.value.items.push(dishWithIngredients)

    // 调用后端API更新食谱 - 将items转换为JSON字符串
    const updateData = {
      ...selectedRecipe.value,
      items: JSON.stringify(selectedRecipe.value.items)
    }

    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + selectedRecipe.value.id, updateData)
      .then((response) => {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === selectedRecipe.value.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const updatedRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          todayRecipes.value[recipeIndex] = updatedRecipe
        }

        ElMessage.success('菜品已添加')
        addDishVisible.value = false

        // 重置表单
        newDish.value = {
          name: '',
          ingredients: [],
          calories: 0,
          protein: 0,
          carbs: 0,
          fat: 0
        }
        newIngredient.value = ''

        selectedRecipe.value = null
      })
      .catch((error) => {
        console.error('添加菜品失败:', error)
        // 失败时恢复本地数据
        selectedRecipe.value.items.pop()
        ElMessage.error('添加菜品失败')
      })
  } else {
    ElMessage.error('请输入菜品名称')
  }
}

// 验证菜品名称格式的函数
const isValidDishName = (name) => {
  // 允许中文、英文、数字、空格以及常见的标点符号
  const nameRegex = /^[\u4e00-\u9fa5a-zA-Z0-9\s\-_()[\]{}/.,，。！？；：]*$/
  return nameRegex.test(name.trim())
}

// 处理添加菜品
const handleAddDish = (recipe, newDish) => {
  if (recipe && newDish.name.trim()) {
    // 先添加到本地
    recipe.items.push(newDish)

    // 调用后端API更新食谱 - 将items转换为JSON字符串
    const updateData = {
      ...recipe,
      items: JSON.stringify(recipe.items)
    }

    axios
      .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
      .then((response) => {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const updatedRecipe = {
            ...response.data.data,
            items:
              typeof response.data.data.items === 'string'
                ? JSON.parse(response.data.data.items)
                : response.data.data.items || []
          }
          todayRecipes.value[recipeIndex] = updatedRecipe
        }

        ElMessage.success('菜品已添加')
      })
      .catch((error) => {
        console.error('添加菜品失败:', error)
        // 失败时恢复本地数据
        recipe.items.pop()
        ElMessage.error('添加菜品失败')
      })
  }
}

// 删除菜品
const deleteDish = (recipe, dish) => {
  if (recipe && dish && recipe.items) {
    // 先更新本地数据
    const index = recipe.items.indexOf(dish)
    if (index !== -1) {
      recipe.items.splice(index, 1)

      // 调用后端API更新食谱
      const updateData = {
        ...recipe
      }

      axios
        .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
        .then((response) => {
          // 更新本地数据
          const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            todayRecipes.value[recipeIndex] = response.data.data
          }

          ElMessage.success('菜品已删除')
        })
        .catch((error) => {
          console.error('删除菜品失败:', error)
          // 失败时恢复本地数据
          recipe.items.splice(index, 0, dish)
          ElMessage.error('删除菜品失败')
        })
    }
  }
}

// 批量删除食谱
const batchDeleteRecipes = () => {
  if (selectedRecipes.value.length === 0) return

  // 确认删除
  ElMessageBox.confirm('确定要批量删除选中的食谱吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 遍历删除选中的食谱
      const deletePromises = selectedRecipes.value.map((id) =>
        axios.delete(API_CONFIG.baseURL + API_CONFIG.recipe.delete + id)
      )

      Promise.all(deletePromises)
        .then((responses) => {
          // 删除成功，更新本地数据
          selectedRecipes.value.forEach((id) => {
            const index = todayRecipes.value.findIndex((r) => r.id === id)
            if (index !== -1) {
              todayRecipes.value.splice(index, 1)
            }
          })
          // 清空选中列表
          selectedRecipes.value = []
          ElMessage.success(`成功删除${responses.length}个食谱`)
        })
        .catch((error) => {
          console.error('批量删除失败:', error)
          ElMessage.error('批量删除失败')
        })
    })
    .catch(() => {
      // 取消删除
    })
}

// 处理导入商家菜品
const handleImportMerchantDishes = (recipe, dishesToImport) => {
  if (dishesToImport.length > 0) {
    // 这里需要知道要导入到哪个食谱，需要先设置 selectedRecipe
    if (recipe) {
      // 先保存当前的items，以便失败时恢复
      const originalItems = [...recipe.items]

      // 将商家菜品转换为食谱需要的格式并添加到本地
      dishesToImport.forEach((dish) => {
        const recipeDish = {
          name: dish.name,
          ingredients: [], // 商家菜品默认没有食材，用户可以后续添加
          calories: 0,
          protein: 0,
          carbs: 0,
          fat: 0
        }
        recipe.items.push(recipeDish)
      })

      // 调用后端API更新食谱 - 将items转换为JSON字符串
      const updateData = {
        ...recipe,
        items: JSON.stringify(recipe.items)
      }

      axios
        .put(API_CONFIG.baseURL + API_CONFIG.recipe.update + recipe.id, updateData)
        .then((response) => {
          // 更新本地数据 - 确保items字段已解析
          const recipeIndex = todayRecipes.value.findIndex((r) => r.id === recipe.id)
          if (recipeIndex !== -1) {
            // 确保返回的食谱有items数组并已解析
            const updatedRecipe = {
              ...response.data.data,
              items:
                typeof response.data.data.items === 'string'
                  ? JSON.parse(response.data.data.items)
                  : response.data.data.items || []
            }
            todayRecipes.value[recipeIndex] = updatedRecipe
          }

          ElMessage.success(`成功导入 ${dishesToImport.length} 道菜品`)
        })
        .catch((error) => {
          console.error('导入商家菜品失败:', error)
          // 失败时恢复本地数据
          recipe.items = originalItems
          ElMessage.error('导入商家菜品失败')
        })
    } else {
      ElMessage.error('请先选择要导入到的食谱')
    }
  } else {
    ElMessage.warning('请先选择要导入的菜品')
  }
}

// 批量收藏食谱
const batchFavoriteRecipes = () => {
  if (selectedRecipes.value.length === 0) return

  // 批量设置所有选中的食谱为收藏状态
  const recipeIds = selectedRecipes.value
  const favorite = true // 批量收藏

  axios
    .put(API_CONFIG.baseURL + API_CONFIG.recipe.batchToggleFavorite, {
      recipeIds,
      favorite
    })
    .then((response) => {
      // 更新本地数据
      console.log('批量收藏成功:', response)
      const updatedRecipes = response.data.data || []
      updatedRecipes.forEach((updatedRecipe) => {
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const originalRecipe = todayRecipes.value[recipeIndex]
          // 使用 Object.assign 创建新对象，避免覆盖原对象
          const updatedRecipeWithParsedItems = Object.assign({}, originalRecipe, updatedRecipe, {
            items:
              typeof updatedRecipe.items === 'string'
                ? JSON.parse(updatedRecipe.items)
                : updatedRecipe.items || originalRecipe.items || [], // 确保items字段不会丢失
            // 统一字段名称，将后端返回的favorite映射为前端使用的isFavorite
            isFavorite:
              updatedRecipe.favorite !== undefined
                ? updatedRecipe.favorite
                : updatedRecipe.isFavorite
          })
          todayRecipes.value[recipeIndex] = updatedRecipeWithParsedItems
        }
      })

      // 清空选中列表
      selectedRecipes.value = []
      ElMessage.success(`成功收藏${updatedRecipes.length}个食谱`)
    })
    .catch((error) => {
      console.error('批量收藏失败:', error)
      ElMessage.error('批量收藏失败')
    })
}

// 添加新菜单
const addNewMenu = () => {
  // 这个函数已经被AddRecipe组件替换
}

// 处理从AddRecipe组件添加的新食谱
const handleAddRecipe = (newRecipeData) => {
  const authStore = useAuthStore()

  // 准备食谱数据 - 将type转换为小写
  const recipeData = {
    ...newRecipeData,
    type: newRecipeData.type.toLowerCase(), // 保持与系统一致的小写格式
    items: newRecipeData.ingredients || [], // 映射字段
    userId: authStore.userId || 0 // 使用当前用户ID
  }

  // 调用后端API添加食谱 - 将items转换为JSON字符串
  const recipeDataWithStringItems = {
    ...recipeData,
    items: JSON.stringify(recipeData.items)
  }

  axios
    .post(API_CONFIG.baseURL + API_CONFIG.recipe.add, recipeDataWithStringItems)
    .then((response) => {
      // 检查返回的数据是否有效
      if (response.data.data) {
        // 确保返回的食谱有items数组并已解析
        const newRecipe = {
          ...response.data.data,
          items:
            typeof response.data.data.items === 'string'
              ? JSON.parse(response.data.data.items)
              : response.data.data.items || [],
          isFavorite: response.data.data.favorite || false // 映射收藏字段
        }

        // 将返回的食谱添加到本地列表
        todayRecipes.value.push(newRecipe)
        ElMessage.success('食谱已添加')
      } else {
        ElMessage.error('添加食谱失败: 服务器返回无效数据')
      }
    })
    .catch((error) => {
      console.error('添加食谱失败:', error)
      ElMessage.error('添加食谱失败')
    })
}

// 单个食谱收藏/取消收藏
const toggleRecipeFavorite = (recipe) => {
  // 发送API请求切换收藏状态
  axios
    .put(API_CONFIG.baseURL + API_CONFIG.recipe.toggleFavorite + recipe.id, {})
    .then((response) => {
      console.log('切换收藏状态成功:', response)
      const updatedRecipe = response.data.data
      if (updatedRecipe && updatedRecipe.id) {
        // 更新本地数据 - 确保items字段已解析
        const recipeIndex = todayRecipes.value.findIndex((r) => r.id === updatedRecipe.id)
        if (recipeIndex !== -1) {
          // 确保返回的食谱有items数组并已解析
          const originalRecipe = todayRecipes.value[recipeIndex]
          // 使用 Object.assign 创建新对象，避免覆盖原对象
          const updatedRecipeWithParsedItems = Object.assign({}, originalRecipe, updatedRecipe, {
            items:
              typeof updatedRecipe.items === 'string'
                ? JSON.parse(updatedRecipe.items)
                : updatedRecipe.items || originalRecipe.items || [], // 确保items字段不会丢失
            // 统一字段名称，将后端返回的favorite映射为前端使用的isFavorite
            isFavorite:
              updatedRecipe.favorite !== undefined
                ? updatedRecipe.favorite
                : updatedRecipe.isFavorite
          })
          todayRecipes.value[recipeIndex] = updatedRecipeWithParsedItems
        }
        // 显示提示
        if (updatedRecipe.isFavorite) {
          ElMessage.success('食谱已收藏')
        } else {
          ElMessage.success('食谱已取消收藏')
        }
      } else {
        // API请求失败，本地切换收藏状态
        recipe.isFavorite = !recipe.isFavorite
        // 显示提示
        if (recipe.isFavorite) {
          ElMessage.success('食谱已收藏')
        } else {
          ElMessage.success('食谱已取消收藏')
        }
      }
    })
    .catch((error) => {
      console.error('切换收藏状态失败:', error)
      // 网络错误时，本地切换收藏状态作为降级方案
      recipe.isFavorite = !recipe.isFavorite
      // 显示提示
      if (recipe.isFavorite) {
        ElMessage.warning('网络异常，已在本地标记为收藏')
      } else {
        ElMessage.warning('网络异常，已在本地取消收藏')
      }
    })
}

// 打开导入商家菜品对话框
const openImportMerchantDish = (recipe) => {
  selectedRecipe.value = recipe
  importMerchantDishVisible.value = true
}

// 切换卡片选中状态
const toggleCardSelection = (recipeId) => {
  const index = selectedRecipes.value.indexOf(recipeId)
  if (index > -1) {
    selectedRecipes.value.splice(index, 1)
  } else {
    selectedRecipes.value.push(recipeId)
  }
}

// 筛选后的食谱列表
const filteredRecipes = computed(() => {
  let filtered = [...todayRecipes.value]

  // 首先筛选掉null和没有id的食谱
  filtered = filtered.filter((recipe) => recipe && recipe.id)

  // 按收藏状态和修改时间排序：收藏的食谱置顶，然后按照修改时间从晚到早排序（更改越晚越靠前）
  filtered.sort((a, b) => {
    // 首先比较收藏状态，收藏的排前面
    if (a.isFavorite && !b.isFavorite) return -1
    if (!a.isFavorite && b.isFavorite) return 1

    // 如果收藏状态相同，比较修改时间（假设字段名为updatedAt）
    // 这里需要根据实际字段名调整，如果没有则可以注释这部分
    const timeA = new Date(a.updatedAt || 0)
    const timeB = new Date(b.updatedAt || 0)
    return timeB - timeA // 从晚到早排序（最新修改的在最上面）
  })

  // 餐型筛选
  if (filters.value.mealType !== 'all') {
    if (filters.value.mealType === 'snack') {
      // 加餐包含所有零食类餐型
      filtered = filtered.filter(
        (recipe) =>
          recipe &&
          [
            // 再次确保recipe不为null
            'snack',
            'night_snack',
            'morning_snack',
            'afternoon_tea',
            'tea',
            'brunch',
            'midnight_snack'
          ].includes(recipe.type)
      )
    } else {
      filtered = filtered.filter((recipe) => recipe && recipe.type === filters.value.mealType)
    }
  }

  return filtered
})
</script>

<template>
  <div class="today-recipe-wrapper">
    <div class="nordic-today-recipe">
    <!-- 页面标题：标准版页头，标题 --text-h1 + 上下 --space-6（§4.7） -->
    <AppPageHeader title="今日食谱" />

    <!-- 营养摄入概览：列表为空时收起无效统计（§4.5） -->
    <div v-if="todayRecipes.length > 0" class="nutrition-overview">
      <div class="nutri-stat">
        <span class="nutri-val">{{ nutritionData.calories }}</span>
        <span class="nutri-unit">kcal</span>
        <span class="nutri-label">卡路里</span>
      </div>
      <div class="nutri-divider"></div>
      <div class="nutri-stat">
        <span class="nutri-val">{{ nutritionData.protein }}</span>
        <span class="nutri-unit">g</span>
        <span class="nutri-label">蛋白质</span>
      </div>
      <div class="nutri-divider"></div>
      <div class="nutri-stat">
        <span class="nutri-val">{{ nutritionData.carbs }}</span>
        <span class="nutri-unit">g</span>
        <span class="nutri-label">碳水</span>
      </div>
      <div class="nutri-divider"></div>
      <div class="nutri-stat">
        <span class="nutri-val">{{ nutritionData.fat }}</span>
        <span class="nutri-unit">g</span>
        <span class="nutri-label">脂肪</span>
      </div>
    </div>

    <!-- 餐型筛选 + 操作 -->
    <div class="meal-filter-row" :class="{ 'is-empty': todayRecipes.length === 0 }">
      <div v-if="todayRecipes.length > 0" class="meal-chips">
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'all' }"
          @click="filters.mealType = 'all'"
        >全部</button>
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'breakfast' }"
          @click="filters.mealType = 'breakfast'"
        >早餐</button>
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'lunch' }"
          @click="filters.mealType = 'lunch'"
        >午餐</button>
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'dinner' }"
          @click="filters.mealType = 'dinner'"
        >晚餐</button>
        <button
          class="meal-chip"
          :class="{ active: filters.mealType === 'snack' }"
          @click="filters.mealType = 'snack'"
        >加餐</button>
      </div>
      <div class="action-btns">
        <button
          v-if="filteredRecipes.length > 0"
          class="nordic-btn accent"
          @click="addRecipeVisible = true"
        >+ 添加食谱</button>
        <button class="nordic-btn" @click="importOrderVisible = true">从订单导入</button>
        <button
          class="nordic-btn ghost"
          :disabled="selectedRecipes.length === 0"
          @click="batchDeleteRecipes"
        >批量删除</button>
        <button
          class="nordic-btn ghost"
          :disabled="selectedRecipes.length === 0"
          @click="batchFavoriteRecipes"
        >批量收藏</button>
      </div>
    </div>

    <!-- 食谱列表 -->
    <div class="recipe-list" :class="layoutType">
      <!-- 空状态：图标 32px --ink-400 + 一句说明 + 一个替代动作 + 可选次动作（§4.5） -->
      <div v-if="filteredRecipes.length === 0" class="empty-recipes">
        <el-icon class="empty-recipes__icon" aria-hidden="true"><Box /></el-icon>
        <p class="empty-recipes__desc">今日没有食谱数据</p>
        <div class="empty-recipes__actions">
          <button class="nordic-btn accent" @click="addRecipeVisible = true">添加食谱</button>
          <CommonBackButton type="default" size="default" />
        </div>
      </div>
      <div v-else class="recipe-cards">
        <RecipeCard
          v-for="recipe in filteredRecipes"
          :key="recipe.id"
          :recipe="recipe"
          :selectable="true"
          :selected-ids="selectedRecipes"
          @toggle-select="toggleCardSelection"
          @toggle-favorite="toggleRecipeFavorite"
          @view-details="viewRecipeDetails"
          @add-dish="addDish"
          @import-merchant-dish="openImportMerchantDish"
          @replace-dish="handleReplaceDishClick"
          @delete-dish="handleDeleteDishClick"
        />
      </div>
    </div>
  </div>

  <!-- 查看详情组件 -->
  <RecipeDetail
    v-model:visible="detailDialogVisible"
    :recipe="selectedRecipe"
    @close="selectedRecipe = null"
    @update:recipe="handleUpdateRecipe"
  ></RecipeDetail>

  <!-- 替换菜品组件 -->
  <ReplaceDish
    v-model:visible="replaceDishVisible"
    :recipe="selectedRecipe"
    :dish="selectedDish"
    @replace="handleReplaceDish"
    @close="selectedRecipe = null; selectedDish = null"
  ></ReplaceDish>

  <!-- 添加菜品组件 -->
  <AddDish
    v-model:visible="addDishVisible"
    :recipe="selectedRecipe"
    @add="handleAddDish"
    @close="selectedRecipe = null"
  ></AddDish>

  <!-- 导入商家菜品组件 -->
  <ImportMerchantDish
    v-model:visible="importMerchantDishVisible"
    :recipe="selectedRecipe"
    @import="handleImportMerchantDishes"
    @close="selectedRecipe = null"
  ></ImportMerchantDish>

  <!-- 导入订单对话框 -->
  <el-dialog v-model="importOrderVisible" title="从订单导入食谱" width="600px" top="10%">
    <div class="import-order-panel">
      <el-select v-model="selectedOrder" placeholder="请选择要导入的订单" style="width: 100%">
        <el-option
          v-for="order in orders"
          :key="order.id"
          :label="`订单号: ${order.orderNo} - 总价: ${order.totalPrice}元`"
          :value="order"
        >
          <template v-slot:default>
            <div>
              <div style="margin-bottom: 6px;">订单号: {{ order.orderNo }} - 总价: {{ order.totalPrice }}元</div>
              <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                <el-tag v-for="dish in order.dishes" :key="dish.name" size="small" type="info">
                  {{ dish.name }}
                </el-tag>
              </div>
            </div>
          </template>
        </el-option>
      </el-select>
    </div>

    <template #footer>
      <el-button @click="importOrderVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmImportOrder">导入为新食谱</el-button>
    </template>
  </el-dialog>

  <!-- 添加食谱对话框 -->
  <AddRecipe v-model:visible="addRecipeVisible" @add-recipe="handleAddRecipe" />
  </div>
</template>

<style scoped lang="less">
// 颜色/间距/圆角/字阶/动效全部取自 tokens.css（DESIGN-BASELINE §3）。
.today-recipe-wrapper {
  display: contents;
}

// 分区首次进入：opacity + translateY(4px)，只播一次（§3.5）
@keyframes today-recipe-enter {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.nordic-today-recipe {
  box-sizing: border-box;
  max-width: var(--page-max);
  margin: 0 auto;
  padding: var(--space-6);
  background: var(--canvas);
  min-height: 100%;

  // --- 营养概览：数值统一 --font-num / --text-h1 / tabular-nums（§4.8）---
  .nutrition-overview {
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: var(--space-5) var(--space-6);
    margin-bottom: var(--space-6);
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    animation: today-recipe-enter var(--duration-base) var(--ease-out) both;

    .nutri-stat {
      flex: 1;
      text-align: center;
    }

    .nutri-val {
      display: block;
      font-family: var(--font-num);
      font-size: var(--text-h1);
      font-weight: var(--weight-semibold);
      line-height: 32px;
      color: var(--ink-900);
      font-variant-numeric: tabular-nums;
    }

    .nutri-unit {
      font-size: var(--text-caption);
      color: var(--ink-500);
    }

    .nutri-label {
      display: block;
      margin-top: var(--space-1);
      font-size: var(--text-caption);
      color: var(--ink-500);
    }

    .nutri-divider {
      width: 1px;
      height: 40px;
      background: var(--line);
    }
  }

  // --- 筛选与操作 ---
  .meal-filter-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-4);
    margin-bottom: var(--space-6);
    flex-wrap: wrap;

    // 列表为空时筛选已收起，操作区右对齐（§4.5）
    &.is-empty {
      justify-content: flex-end;
    }

    .meal-chips {
      display: flex;
      gap: var(--space-2);
    }

    .meal-chip {
      padding: 6px var(--space-4);
      border: 1px solid var(--line);
      background: var(--surface);
      border-radius: var(--radius-full);
      font-size: var(--text-body-sm);
      font-weight: var(--weight-medium);
      color: var(--ink-500);
      cursor: pointer;
      transition:
        background var(--duration-fast) var(--ease-out),
        border-color var(--duration-fast) var(--ease-out),
        color var(--duration-fast) var(--ease-out);

      &:hover {
        border-color: var(--brand-300);
        color: var(--brand-600);
      }

      // 选中态用浅底胶囊，不与主按钮争实心层级（§4.1）
      &.active {
        background: var(--brand-50);
        border-color: var(--brand-300);
        color: var(--brand-700);
        font-weight: var(--weight-semibold);
      }
    }

    .action-btns {
      display: flex;
      gap: var(--space-2);
      flex-wrap: wrap;
    }
  }

  // --- 按钮层级：主/次/文字三级（§4.1）---
  .nordic-btn {
    padding: 6px var(--space-4);
    border: 1px solid var(--line-strong);
    background: var(--surface);
    border-radius: var(--radius-md);
    font-size: var(--text-body-sm);
    font-weight: var(--weight-medium);
    color: var(--ink-700);
    cursor: pointer;
    transition:
      background var(--duration-fast) var(--ease-out),
      border-color var(--duration-fast) var(--ease-out),
      color var(--duration-fast) var(--ease-out),
      box-shadow var(--duration-fast) var(--ease-out);

    &:hover:not(:disabled) {
      border-color: var(--ink-400);
    }

    // 主按钮：--brand-500 实心 + --surface 文字，每屏唯一
    &.accent {
      min-height: var(--control-height-md);
      border-color: var(--brand-500);
      background: var(--brand-500);
      color: var(--surface);

      &:hover:not(:disabled) {
        border-color: var(--brand-600);
        background: var(--brand-600);
        box-shadow: var(--shadow-sm);
      }
    }

    // 文字级动作：批量操作等第三层级
    &.ghost {
      border-color: transparent;
      background: transparent;
      color: var(--ink-500);

      &:hover:not(:disabled) {
        border-color: var(--line-strong);
        background: var(--surface);
        color: var(--ink-700);
      }
    }

    // 禁用态：--ink-400 文字 + --neutral-bg 底（§4.1）
    &:disabled,
    &.ghost:disabled,
    &.accent:disabled {
      border-color: transparent;
      background: var(--neutral-bg);
      color: var(--ink-400);
      cursor: not-allowed;
    }
  }

  // --- 食谱列表 ---
  .recipe-list {
    .recipe-cards {
      display: flex;
      flex-direction: column;
      gap: var(--space-4);
    }
  }

  // --- 空状态：图标 + 说明 + 一个替代动作 + 可选次动作（§4.5）---
  .empty-recipes {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--space-2);
    padding: var(--space-8) var(--space-6);
    text-align: center;

    .empty-recipes__icon {
      font-size: 32px;
      color: var(--ink-400);
    }

    .empty-recipes__desc {
      margin: 0;
      font-size: var(--text-body);
      line-height: 22px;
      color: var(--ink-500);
    }

    .empty-recipes__actions {
      display: flex;
      align-items: center;
      gap: var(--space-3);
      margin-top: var(--space-2);
    }
  }

  // --- 导入订单面板 ---
  .import-order-panel {
    padding: var(--space-4);
    background: var(--canvas-sunken);
    border-radius: var(--radius-md);
  }
}
</style>
