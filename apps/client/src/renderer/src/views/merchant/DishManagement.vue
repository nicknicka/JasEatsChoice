<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { API_CONFIG } from '../../config/index.js'
// 导入authStore
import { useAuthStore } from '../../store/authStore'
// 导入公共组件
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
import StatusTag from '../../components/common/StatusTag.vue'
// 导入图标
import { Search, Plus, CircleCheck, CircleClose, Delete, ArrowDown, Edit } from '@element-plus/icons-vue'
import {
  Goods as GoodsIcon,
  Money as MoneyIcon,
  Folder as FolderIcon,
  GoodsFilled as StockIcon,
  SwitchButton as StatusIcon,
  List as IngredientsIcon,
  Document as DetailsIcon,
  Warning as FlameIcon,
  Star as StarIcon
} from '@element-plus/icons-vue'

// 创建路由实例
const router = useRouter()

// 菜品数据
const dishesList = ref([])

const loading = ref(false)
const searchKeyword = ref('')
const selectedDishes = ref([])

// 菜品明细（食材 / 营养）展开状态，仅用于展示层级（评审 P1：默认折叠为摘要行）
const expandedDishes = ref({})
const toggleDishDetail = (dish) => {
  expandedDishes.value = { ...expandedDishes.value, [dish.id]: !expandedDishes.value[dish.id] }
}

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const filteredDishes = ref([])
const paginatedDishes = ref([]) // 分页后的菜品数据

// 更新分页数据
const updatePagination = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  paginatedDishes.value = filteredDishes.value.slice(startIndex, endIndex)
}
// 三态全选复选框的状态：0=未选择，1=部分选择，2=全选
// const selectAllState = ref(0); // 不再需要这个状态变量，直接通过计算获得
// 页面加载时初始化
onMounted(() => {
  loading.value = true
  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    loading.value = false
    return
  }

  // 从API获取菜品数据
  axios
    .get(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`, {
      params: {
        merchantId: merchantId
      }
    })
    .then((response) => {
      if (response.data && response.data.code === '200') {
        // 预处理菜品数据，确保所有菜品都有有效的状态和时间格式
        const processedDishes = response.data.data.map((dish) => {
          // 转换时间格式为 yyyy-MM-dd HH:mm:ss
          if (dish.createdAt) {
            dish.createdAt = new Date(dish.createdAt).toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            })
          }
          if (dish.updatedAt) {
            dish.updatedAt = new Date(dish.updatedAt).toLocaleString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            })
          }

          // 确保price显示为正确的数字格式
          if (dish.price && typeof dish.price === 'object') {
            dish.price = dish.price.toString()
          }

          // 将布尔值的 status 转换为显示用的字符串
          if (typeof dish.status === 'boolean') {
            // 保持布尔值不变，用于判断
            dish.statusBoolean = dish.status
            dish.statusString = dish.status ? 'online' : 'offline'
          } else {
            // 如果已经是字符串，保持不变
            dish.statusBoolean = dish.status === 'online'
            dish.statusString = dish.status || 'offline'
          }

          // 处理库存空值情况
          if (dish.stock == null || dish.stock === '') {
            dish.stock = 0
          }

          // 优化分类字段显示
          if (dish.category && dish.category.startsWith('category_')) {
            // 将 category_1 转换为 分类1
            dish.category = `分类${dish.category.replace('category_', '')}`
          }

          // 处理食材数据 - 支持多种格式
          // 优先使用 ingredients 字段，如果没有则从 optionalIngredients 和 requiredIngredients 构建
          if (dish.ingredients && typeof dish.ingredients === 'string') {
            try {
              dish.ingredients = JSON.parse(dish.ingredients)
            } catch (error) {
              console.error('解析食材信息失败:', error)
              dish.ingredients = { mandatory: [], optional: [] }
            }
          } else if (!dish.ingredients || (typeof dish.ingredients === 'object' && !dish.ingredients.mandatory && !dish.ingredients.optional)) {
            // 如果没有 ingredients 对象，或者 ingredients 对象中没有 mandatory 和 optional 字段
            // 尝试从 optionalIngredients 和 requiredIngredients 构建兼容格式
            const mandatory = dish.requiredIngredients || []
            const optional = dish.optionalIngredients || []

            dish.ingredients = {
              mandatory: Array.isArray(mandatory) ? mandatory : [],
              optional: Array.isArray(optional) ? optional.map(item => {
                // 处理可选食材可能是字符串或对象的情况
                return typeof item === 'string' ? item : (item.name || String(item))
              }) : []
            }
          }

          // 确保 ingredients 有正确的结构
          if (!dish.ingredients) {
            dish.ingredients = { mandatory: [], optional: [] }
          }
          if (!dish.ingredients.mandatory) {
            dish.ingredients.mandatory = []
          }
          if (!dish.ingredients.optional) {
            dish.ingredients.optional = []
          }

          // 将后端的 calorie 字段映射到前端的 totalCalories 字段
          if (dish.calorie !== undefined) {
            dish.totalCalories = dish.calorie
          }

          return dish
        })
        dishesList.value = processedDishes
        filteredDishes.value = [...dishesList.value] // 更新筛选后的菜品
        updatePagination() // 初始化分页数据
      }
    })
    .catch((error) => {
      console.error('加载菜品失败:', error)
      ElMessage.error('加载菜品失败')
    })
    .finally(() => {
      loading.value = false
    })
})

// 筛选菜品 - 修复重复声明

// 更新筛选
const updateFilter = () => {
  filteredDishes.value = dishesList.value.filter((dish) => {
    // 搜索筛选
    if (
      searchKeyword.value &&
      !dish.name.includes(searchKeyword.value) &&
      !dish.category.includes(searchKeyword.value)
    ) {
      return false
    }

    return true
  })

  // 重置到第一页
  currentPage.value = 1

  // 更新分页数据
  updatePagination()
}

// 编辑菜品
const editDish = (dish) => {
  openEditDishDialog(dish)
}

// 保存编辑后的菜品
const saveEditedDish = () => {
  // 简单的表单验证
  if (!editDishForm.value.name.trim()) {
    ElMessage.warning('请填写菜品名称')
    return
  }

  // 准备请求数据，将 ingredients 对象序列化为 JSON 字符串，并将 totalCalories 映射为 calorie
  const requestData = {
    id: editDishForm.value.id,
    merchantId: editDishForm.value.merchantId,
    name: editDishForm.value.name,
    price: editDishForm.value.price,
    category: editDishForm.value.category,
    description: editDishForm.value.description,
    stock: editDishForm.value.stock || 0,
    calorie: editDishForm.value.totalCalories,
    image: editDishForm.value.image,
    ingredients: JSON.stringify(editDishForm.value.ingredients),
    status: editDishForm.value.statusString === 'online' // 将 statusString 转换为布尔值
  }

  // 发送后端请求
  axios
    .put(`${API_CONFIG.baseURL}${API_CONFIG.dish.detail}${requestData.id}`, requestData)
    .then((response) => {
      console.log('编辑菜品响应:', response)
      if (response.data && response.data.code === '200') {
        // 从后端返回中获取更新后的菜品数据
        let updatedDish = response.data.data

        // 处理后端返回的食材数据格式
        if (updatedDish) {
          // 处理 status 字段
          if (typeof updatedDish.status === 'boolean') {
            updatedDish.statusBoolean = updatedDish.status
            updatedDish.statusString = updatedDish.status ? 'online' : 'offline'
          }

          // 如果 ingredients 是字符串，解析它
          if (typeof updatedDish.ingredients === 'string') {
            try {
              updatedDish.ingredients = JSON.parse(updatedDish.ingredients)
            } catch (error) {
              console.error('解析食材信息失败:', error)
              updatedDish.ingredients = { mandatory: [], optional: [] }
            }
          }

          // 如果后端返回的是 optionalIngredients 和 requiredIngredients，转换为 ingredients 格式
          if (!updatedDish.ingredients || (typeof updatedDish.ingredients === 'object' && !updatedDish.ingredients.mandatory && !updatedDish.ingredients.optional)) {
            const mandatory = updatedDish.requiredIngredients || []
            const optional = updatedDish.optionalIngredients || []

            updatedDish.ingredients = {
              mandatory: Array.isArray(mandatory) ? mandatory : [],
              optional: Array.isArray(optional) ? optional.map(item => {
                return typeof item === 'string' ? item : (item.name || String(item))
              }) : []
            }
          }

          // 确保 ingredients 有正确的结构
          if (!updatedDish.ingredients) {
            updatedDish.ingredients = { mandatory: [], optional: [] }
          }
          if (!updatedDish.ingredients.mandatory) {
            updatedDish.ingredients.mandatory = []
          }
          if (!updatedDish.ingredients.optional) {
            updatedDish.ingredients.optional = []
          }

          // 映射 calorie 字段
          if (updatedDish.calorie !== undefined) {
            updatedDish.totalCalories = updatedDish.calorie
          }
        }

        // 更新本地菜品列表
        console.log('updatedDish.id:', updatedDish.id, '类型:', typeof updatedDish.id)
        console.log('dishesList中的菜品ID:', dishesList.value.map(item => ({ id: item.id, type: typeof item.id })))
        const index = dishesList.value.findIndex((item) => item.id === updatedDish.id)
        console.log('找到的索引:', index)
        if (index !== -1) {
          console.log('更新前菜品数据:', dishesList.value[index])
          console.log('更新后菜品数据:', updatedDish)
          dishesList.value[index] = updatedDish
          updateFilter()
        }
        // 关闭对话框并显示成功消息
        editDishDialogVisible.value = false
        ElMessage.success('菜品已更新')
      } else {
        ElMessage.error(response.data?.message || '菜品更新失败')
      }
    })
    .catch((error) => {
      console.error('更新菜品失败:', error)
      ElMessage.error('网络错误，菜品更新失败')
    })
}

// 切换菜品状态（上架/下架）
const toggleDishStatus = (dish) => {
  const currentStatus = dish.statusBoolean
  const newStatus = !currentStatus
  const statusText = newStatus ? '上架' : '下架'

  ElMessageBox.confirm(`确定要将该菜品${statusText}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用后端API更新菜品状态
      axios
        .put(`${API_CONFIG.baseURL}${API_CONFIG.dish.status}/${dish.id}/status`, {
          status: newStatus
        })
        .then((response) => {
          console.log('更改菜品状态响应:', response)
          if (response.data && response.data.code === '200') {
            dish.statusBoolean = newStatus
            dish.statusString = newStatus ? 'online' : 'offline'
            ElMessage.success(`菜品已${statusText}`)

            // 当菜品下架时，同步更新该菜品在所有菜单中的状态为下架
            if (!newStatus) {
              axios
                .get(`${API_CONFIG.baseURL}/v1/menus/dishes/${dish.id}/menus`)
                .then((menuResponse) => {
                  if (menuResponse.data && menuResponse.data.code === '200') {
                    const menuIds = menuResponse.data.data.map((menu) => menu.id)
                    axios
                      .put(`${API_CONFIG.baseURL}/v1/menus/dishes/${dish.id}/status`, {
                        menuIds: menuIds,
                        status: 0 // 0 表示下架
                      })
                      .then((batchResponse) => {
                        if (batchResponse.data && batchResponse.data.code === '200') {
                          console.log('菜品在所有菜单中的状态已同步下架')
                        }
                      })
                      .catch((error) => {
                        console.error('同步菜品在菜单中的状态失败:', error)
                      })
                  }
                })
                .catch((error) => {
                  console.error('获取菜品关联菜单失败:', error)
                })
            }
          } else {
            ElMessage.error(response.data?.message || `菜品${statusText}失败`)
          }
        })
        .catch((error) => {
          console.error(`更新菜品状态失败:`, error)
          ElMessage.error(`网络错误，菜品${statusText}失败`)
        })
    })
    .catch(() => {
      ElMessage.info('已取消操作')
    })
}

// 菜单关联对话框状态
const menuAssociationDialogVisible = ref(false)
const currentDish = ref(null)
const associatedMenus = ref([])
const selectedStatus = ref(1)
const loadingMenus = ref(false)

// 根据菜单类型获取标签类型
const getMenuTypeTagType = (type) => {
  const typeMap = {
    早餐: 'warning',
    午餐: 'success',
    晚餐: 'info',
    夜宵: 'danger',
    套餐: 'primary'
  }
  return typeMap[type] || 'info'
}

// 查看菜单详情
const viewMenuDetails = (menu) => {
  // 跳转到菜单编辑页面，因为菜单编辑页面包含了菜单的详细信息
  router.push({
    name: 'merchant-menu-edit',
    query: { menuId: menu.id }
  })
  // 关闭当前对话框
  menuAssociationDialogVisible.value = false
}

// 显示菜单关联对话框
const showMenuAssociationDialog = (dish) => {
  currentDish.value = dish
  menuAssociationDialogVisible.value = true
  loadingMenus.value = true

  console.log('开始获取菜品关联菜单，菜品ID:', dish.id) // 新增调试日志

  // 调用API获取该菜品关联的所有菜单
  axios
    .get(`${API_CONFIG.baseURL}/v1/menus/dishes/${dish.id}/menus`)
    .then((response) => {
      console.log('获取菜单关联响应:', response) // 新增调试日志
      if (response.data && response.data.code === '200') {
        console.log('菜单关联数据:', response.data.data) // 新增调试日志
        associatedMenus.value = response.data.data.map((menu) => ({
          ...menu,
          dishStatus: menu.dish_status
        }))
        // 如果没有关联菜单，也显示提示信息
        if (response.data.data.length === 0) {
          console.log('该菜品尚未关联任何菜单')
        }
      } else {
        console.error('响应状态码错误:', response.data?.code)
        ElMessage.error('获取菜单关联失败')
      }
    })
    .catch((error) => {
      console.error('获取菜单关联失败:', error)
      ElMessage.error('获取菜单关联失败')
    })
    .finally(() => {
      loadingMenus.value = false
    })
}

// 更新菜品在特定菜单中的状态
const updateDishStatusInMenu = (menu) => {
  axios
    .put(`${API_CONFIG.baseURL}/v1/menus/menu/${menu.id}/dishes/${currentDish.value.id}/status`, {
      status: menu.dishStatus
    })
    .then((response) => {
      if (response.data && response.data.code === '200') {
        ElMessage.success('菜品状态更新成功')
      } else {
        ElMessage.error(response.data?.message || '菜品状态更新失败')
      }
    })
    .catch((error) => {
      console.error('更新菜品状态失败:', error)
      ElMessage.error('网络错误，菜品状态更新失败')
    })
}

// 批量更新菜品在所有菜单中的状态
const batchUpdateDishStatus = () => {
  const menuIds = associatedMenus.value.map((menu) => menu.id)
  axios
    .put(`${API_CONFIG.baseURL}/v1/menus/dishes/${currentDish.value.id}/status`, {
      menuIds: menuIds,
      status: selectedStatus.value
    })
    .then((response) => {
      if (response.data && response.data.code === '200') {
        ElMessage.success('批量更新成功')
        // 更新本地数据
        associatedMenus.value.forEach((menu) => {
          menu.dishStatus = selectedStatus.value
        })
      } else {
        ElMessage.error(response.data?.message || '批量更新失败')
      }
    })
    .catch((error) => {
      console.error('批量更新失败:', error)
      ElMessage.error('网络错误，批量更新失败')
    })
}

// 删除菜品
const deleteDish = (dish) => {
  ElMessageBox.confirm('确定要删除该菜品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 调用后端API删除菜品
      axios
        .delete(`${API_CONFIG.baseURL}${API_CONFIG.dish.detail}${dish.id}`)
        .then((response) => {
          if (response.data && response.data.code === '200') {
            const index = dishesList.value.findIndex((item) => item.id === dish.id)
            if (index !== -1) {
              dishesList.value.splice(index, 1)
              updateFilter()
              ElMessage.success('菜品已删除')
            }
          } else {
            ElMessage.error(response.data?.message || '删除菜品失败')
          }
        })
        .catch((error) => {
          console.error('删除菜品失败:', error)
          ElMessage.error('网络错误，删除菜品失败')
        })
    })
    .catch(() => {
      ElMessage.info('已取消删除')
    })
}

// 批量操作
const batchOperation = (operation) => {
  if (selectedDishes.value.length === 0) {
    ElMessage.warning('请先选择菜品')
    return
  }

  // 批量操作确认对话框
  const getConfirmMessage = () => {
    switch (operation) {
      case 'online':
        return '确定要将所选菜品批量上架吗？'
      case 'offline':
        return '确定要将所选菜品批量下架吗？'
      case 'delete':
        return '确定要删除所选菜品吗？'
      default:
        return '确定要执行批量操作吗？'
    }
  }

  const getSuccessMessage = () => {
    switch (operation) {
      case 'online':
        return '批量上架成功'
      case 'offline':
        return '批量下架成功'
      case 'delete':
        return '批量删除成功'
      default:
        return '批量操作成功'
    }
  }

  // 显示确认对话框
  ElMessageBox.confirm(getConfirmMessage(), '操作确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 执行批量操作
      if (operation === 'online' || operation === 'offline') {
        const statusBoolean = operation === 'online' ? true : false
        const dishIds = selectedDishes.value.map((dish) => dish.id)

        // 调用后端API批量更新菜品状态
        axios
          .put(`${API_CONFIG.baseURL}${API_CONFIG.dish.batchStatus}`, {
            dishIds: dishIds,
            status: statusBoolean
          })
          .then((response) => {
            if (response.data && response.data.code === '200') {
              // 更新前端状态
              selectedDishes.value.forEach((dish) => {
                dish.statusBoolean = statusBoolean
                dish.statusString = statusBoolean ? 'online' : 'offline'
              })
              updateFilter()
              selectedDishes.value = []
              ElMessage.success(getSuccessMessage())
            } else {
              ElMessage.error(response.data?.message || '批量操作失败')
            }
          })
          .catch((error) => {
            console.error('批量更新菜品状态失败:', error)
            ElMessage.error('网络错误，批量操作失败')
          })
      } else if (operation === 'delete') {
        // 批量删除操作
        const dishIds = selectedDishes.value.map((dish) => dish.id)

        // 调用后端API批量删除菜品（后端不支持DELETE，使用PUT方法）
        axios
          .put(`${API_CONFIG.baseURL}${API_CONFIG.dish.batchDelete}`, {
            dishIds: dishIds
          })
          .then((response) => {
            if (response.data && response.data.code === '200') {
              // 从前端列表中删除已删除的菜品
              dishesList.value = dishesList.value.filter((dish) => !selectedDishes.value.includes(dish))
              updateFilter()
              selectedDishes.value = []
              ElMessage.success(getSuccessMessage())
            } else {
              ElMessage.error(response.data?.message || '批量删除失败')
            }
          })
          .catch((error) => {
            console.error('批量删除菜品失败:', error)
            ElMessage.error('网络错误，批量删除失败')
          })
      }
    })
    .catch(() => {
      // 用户取消操作
      ElMessage.info('已取消批量操作')
    })
}

// 新增菜品对话框
const addDishDialogVisible = ref(false)

// 食材数据已移除，改为直接输入

// 新必选食材输入
const newMandatoryIngredient = ref('')

// 新可选食材输入
const newOptionalIngredient = ref('')

// 新菜品表单数据
const newDish = ref({
  name: '',
  price: 0,
  category: '主食',
  stock: 100,
  status: 'online', // 默认上架
  ingredients: {
    mandatory: [], // 必选食材改为字符串数组
    optional: [] // 可选食材改为字符串数组
  },
  totalCalories: 0, // 总卡路里
  protein: 0, // 蛋白质
  fat: 0, // 脂肪
  carbs: 0 // 碳水化合物
})

// 添加必选食材
const addMandatoryIngredient = () => {
  if (newMandatoryIngredient.value.trim()) {
    const ingredient = newMandatoryIngredient.value.trim()
    // 检查重复
    if (!newDish.value.ingredients.mandatory.includes(ingredient)) {
      newDish.value.ingredients.mandatory.push(ingredient)
      newMandatoryIngredient.value = ''
      calculateTotalCalories()
    } else {
      ElMessage.warning('该必选食材已存在')
    }
  }
}

// 添加可选食材
const addOptionalIngredient = () => {
  if (newOptionalIngredient.value.trim()) {
    const ingredient = newOptionalIngredient.value.trim()
    // 检查重复
    if (!newDish.value.ingredients.optional.includes(ingredient)) {
      newDish.value.ingredients.optional.push(ingredient)
      newOptionalIngredient.value = ''
      calculateTotalCalories()
    } else {
      ElMessage.warning('该可选食材已存在')
    }
  }
}

// 删除必选食材
const removeMandatoryIngredient = (index) => {
  newDish.value.ingredients.mandatory.splice(index, 1)
  calculateTotalCalories()
}

// 删除可选食材
const removeOptionalIngredient = (index) => {
  newDish.value.ingredients.optional.splice(index, 1)
  calculateTotalCalories()
}

// 计算总卡路里
const calculateTotalCalories = () => {
  // 当食材是商家自定义时，保持当前输入的卡路里值不变
  // 如果需要自动计算，需要实现食材卡路里数据库匹配功能
  // 目前支持商家手动输入总卡路里值
}

// 编辑菜品对话框
const editDishDialogVisible = ref(false)

// 编辑菜品表单数据
const editDishForm = ref({
  ingredients: {
    mandatory: [], // 必选食材改为字符串数组
    optional: [] // 可选食材改为字符串数组
  },
  totalCalories: 0,
  protein: 0, // 蛋白质
  fat: 0, // 脂肪
  carbs: 0 // 碳水化合物
})

// 新必选食材输入（编辑时使用）
const editNewMandatoryIngredient = ref('')

// 新可选食材输入（编辑时使用）
const editNewOptionalIngredient = ref('')

// 添加必选食材（编辑时使用）
const editAddMandatoryIngredient = () => {
  if (editNewMandatoryIngredient.value.trim()) {
    const ingredient = editNewMandatoryIngredient.value.trim()
    // 检查重复
    if (!editDishForm.value.ingredients.mandatory.includes(ingredient)) {
      editDishForm.value.ingredients.mandatory.push(ingredient)
      editNewMandatoryIngredient.value = ''
      calculateEditTotalCalories()
    } else {
      ElMessage.warning('该必选食材已存在')
    }
  }
}

// 添加可选食材（编辑时使用）
const editAddOptionalIngredient = () => {
  if (editNewOptionalIngredient.value.trim()) {
    const ingredient = editNewOptionalIngredient.value.trim()
    // 检查重复
    if (!editDishForm.value.ingredients.optional.includes(ingredient)) {
      editDishForm.value.ingredients.optional.push(ingredient)
      editNewOptionalIngredient.value = ''
      calculateEditTotalCalories()
    } else {
      ElMessage.warning('该可选食材已存在')
    }
  }
}

// 删除必选食材（编辑时使用）
const editRemoveMandatoryIngredient = (index) => {
  editDishForm.value.ingredients.mandatory.splice(index, 1)
  calculateEditTotalCalories()
}

// 删除可选食材（编辑时使用）
const editRemoveOptionalIngredient = (index) => {
  editDishForm.value.ingredients.optional.splice(index, 1)
  calculateEditTotalCalories()
}

// 打开编辑菜品对话框
const openEditDishDialog = (dish) => {
  // 复制菜品数据到编辑表单，确保包含食材信息且为数组
  editDishForm.value = JSON.parse(
    JSON.stringify({
      ...dish,
      ingredients: {
        mandatory: Array.isArray(dish.ingredients?.mandatory) ? dish.ingredients.mandatory : [],
        optional: Array.isArray(dish.ingredients?.optional) ? dish.ingredients.optional : []
      },
      totalCalories: dish.totalCalories || 0,
      statusString: dish.statusString || (dish.statusBoolean ? 'online' : 'offline')
    })
  )

  editDishDialogVisible.value = true
}

// 计算编辑菜品的总卡路里
const calculateEditTotalCalories = () => {
  // 当食材是商家自定义时，保持当前输入的卡路里值不变
  // 如果需要自动计算，需要实现食材卡路里数据库匹配功能
  // 目前支持商家手动输入总卡路里值
}

// 打开添加菜品对话框
const openAddDishDialog = () => {
  addDishDialogVisible.value = true
}

// 保存新菜品
const saveNewDish = () => {
  // 简单的表单验证
  if (!newDish.value.name.trim()) {
    ElMessage.warning('请填写菜品名称')
    return
  }

  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    return
  }

  // 准备请求数据，将 ingredients 对象序列化为 JSON 字符串，并将 totalCalories 映射为 calorie
  const requestData = {
    name: newDish.value.name,
    price: newDish.value.price,
    category: newDish.value.category,
    stock: newDish.value.stock || 100,
    ingredients: JSON.stringify(newDish.value.ingredients),
    calorie: newDish.value.totalCalories,
    merchantId,
    description: newDish.value.description,
    image: newDish.value.image,
    status: newDish.value.status === 'online' || newDish.value.status === true // 转换为布尔值，默认上架
  }

  // 发送后端请求
  axios
    .post(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`, requestData)
    .then((response) => {
      console.log('新增菜品响应:', response)
      if (response.data && response.data.code === '200') {
        let dishData = response.data.data // 获取后端返回的完整菜品数据

        // 处理后端返回的食材数据格式
        if (dishData) {
          // 处理 status 字段
          if (typeof dishData.status === 'boolean') {
            dishData.statusBoolean = dishData.status
            dishData.statusString = dishData.status ? 'online' : 'offline'
          }

          // 如果 ingredients 是字符串，解析它
          if (typeof dishData.ingredients === 'string') {
            try {
              dishData.ingredients = JSON.parse(dishData.ingredients)
            } catch (error) {
              console.error('解析食材信息失败:', error)
              dishData.ingredients = { mandatory: [], optional: [] }
            }
          }

          // 如果后端返回的是 optionalIngredients 和 requiredIngredients，转换为 ingredients 格式
          if (!dishData.ingredients || (typeof dishData.ingredients === 'object' && !dishData.ingredients.mandatory && !dishData.ingredients.optional)) {
            const mandatory = dishData.requiredIngredients || []
            const optional = dishData.optionalIngredients || []

            dishData.ingredients = {
              mandatory: Array.isArray(mandatory) ? mandatory : [],
              optional: Array.isArray(optional) ? optional.map(item => {
                return typeof item === 'string' ? item : (item.name || String(item))
              }) : []
            }
          }

          // 确保 ingredients 有正确的结构
          if (!dishData.ingredients) {
            dishData.ingredients = { mandatory: [], optional: [] }
          }
          if (!dishData.ingredients.mandatory) {
            dishData.ingredients.mandatory = []
          }
          if (!dishData.ingredients.optional) {
            dishData.ingredients.optional = []
          }

          // 映射 calorie 字段
          if (dishData.calorie !== undefined) {
            dishData.totalCalories = dishData.calorie
          }
        }

        dishesList.value.push(dishData)
        updateFilter()
        addDishDialogVisible.value = false
        ElMessage.success('菜品已添加')
      } else {
        ElMessage.error(response.data?.message || '菜品添加失败')
      }
    })
    .catch((error) => {
      console.error('添加菜品失败:', error)
      ElMessage.error('网络错误，菜品添加失败')
    })
}

// 选择/取消选择单个菜品
const toggleDishSelection = (dish) => {
  const index = selectedDishes.value.findIndex((item) => item.id === dish.id)

  if (index === -1) {
    selectedDishes.value.push(dish)
  } else {
    selectedDishes.value.splice(index, 1)
  }
  // console.log('dish',dish) ;
  // console.log('选择状态：', getSelectAllState());
  // console.log('已选择菜品：', selectedDishes.value);
}

// 全选/取消全选
const toggleSelectAll = () => {
  const currentState = getSelectAllState()

  if (currentState === 2) {
    // 当前是全选状态，点击后取消全选
    selectedDishes.value = []
  } else {
    // 当前是未选或部分选择状态，点击后全选
    selectedDishes.value = [...filteredDishes.value]
  }

  // 触发Vue的响应式更新
  selectedDishes.value = [...selectedDishes.value]

  // console.log('全选状态：', getSelectAllState());
  // console.log('已选择菜品：', selectedDishes.value);
}

// 检查全选状态
const getSelectAllState = () => {
  if (selectedDishes.value.length === 0) {
    return 0
  } else if (
    selectedDishes.value.length === filteredDishes.value.length &&
    filteredDishes.value.length > 0
  ) {
    // 已选择所有项目
    return 2
  } else {
    // 部分选择
    return 1
  }
}

// 监听filteredDishes变化，确保全选状态正确更新
watch(
  () => filteredDishes.value,
  () => {
    // 如果过滤后的菜品数量减少，且当前选中的菜品数量等于过滤前的数量，那么需要调整选中的菜品
    if (selectedDishes.value.length > filteredDishes.value.length) {
      // 只保留过滤后仍存在的菜品
      selectedDishes.value = selectedDishes.value.filter((selectedDish) =>
        filteredDishes.value.some((filteredDish) => filteredDish.id === selectedDish.id)
      )
    }
  }
)

// 获取单个菜品的选中状态
const getDishCheckedState = (dish) => {
  // 直接根据selectedDishes数组判断菜品是否被选中
  // console.log('getDishCheckedState selected',selectedDishes.value);
  // console.log('getDishCheckedState',dish);
  // console.log('getDishCheckedState checked', selectedDishes.value.some(item => item.id === dish.id));

  // 确保返回值是布尔类型
  const isChecked = selectedDishes.value.some((item) => item.id === dish.id)
  // console.log('getDishCheckedState final result:', isChecked);
  return isChecked
}
</script>

<template>
  <div class="dish-management-container">
    <!-- 页头：merchant 紧凑版，标题 --text-h2 + 上下 --space-4；新增为唯一主操作，返回在右侧动作区（§4.7） -->
    <AppPageHeader title="菜品管理" :compact="true">
      <template #actions>
        <el-button type="primary" @click="openAddDishDialog">
          <el-icon><CirclePlus /></el-icon>
          <span>新增菜品</span>
        </el-button>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <!-- 工具条：搜索独立成行，控件高度取 --control-height-sm（§3.4、§4.3） -->
    <div class="dish-toolbar">
      <el-input
        v-model="searchKeyword"
        class="dish-toolbar__search"
        placeholder="输入菜品名称或分类..."
        clearable
        @input="updateFilter"
      >
        <template #prefix>
          <el-icon class="dish-toolbar__search-icon"><Search /></el-icon>
        </template>
      </el-input>
      <span v-if="filteredDishes.length > 0" class="dish-toolbar__summary">
        共 {{ filteredDishes.length }} 道菜品
      </span>
    </div>

    <!-- 批量操作条：选中后才滑入，默认不显示，按钮不再常亮（评审 P1、§3.4） -->
    <transition name="dish-batch">
      <div v-if="selectedDishes.length > 0" class="dish-batch-bar">
        <label class="dish-batch-bar__all">
          <el-checkbox
            :indeterminate="getSelectAllState() === 1"
            :model-value="getSelectAllState() === 2"
            @change="toggleSelectAll"
          />
          <span>全选</span>
          <span class="dish-batch-bar__count">{{ selectedDishes.length }}/{{ filteredDishes.length }}</span>
        </label>

        <div class="dish-batch-bar__actions">
          <el-button @click="batchOperation('online')">
            <el-icon><CircleCheck /></el-icon>
            <span>批量上架</span>
          </el-button>
          <el-button @click="batchOperation('offline')">
            <el-icon><CircleClose /></el-icon>
            <span>批量下架</span>
          </el-button>
          <el-button type="danger" plain @click="batchOperation('delete')">
            <el-icon><Delete /></el-icon>
            <span>批量删除</span>
          </el-button>
          <el-button link @click="selectedDishes = []">取消选择</el-button>
        </div>
      </div>
    </transition>

    <!-- 菜品列表：紧凑摘要行 + 可展开的食材/营养明细（评审 P0/P1） -->
    <div class="dish-list">
      <transition-group name="list" tag="div" class="dish-list__items">
        <div
          v-for="dish in paginatedDishes"
          :key="dish.id"
          class="dish-item"
          :class="{ 'dish-item--selected': getDishCheckedState(dish) }"
        >
          <el-checkbox
            class="dish-item__check"
            :model-value="getDishCheckedState(dish)"
            @change="toggleDishSelection(dish)"
          />

          <div class="dish-item__main">
            <div class="dish-item__head">
              <span class="dish-item__name">{{ dish.name }}</span>
              <StatusTag
                :status="dish.statusBoolean ? 'online' : 'offline'"
                :text="dish.statusBoolean ? '上架' : '下架'"
                size="sm"
              />
              <span v-if="dish.category" class="dish-item__category">{{ dish.category }}</span>
            </div>

            <div class="dish-item__meta">
              <span class="dish-item__meta-item">
                <el-icon><MoneyIcon /></el-icon>
                ¥{{ dish.price }}
              </span>
              <span class="dish-item__meta-item">
                <el-icon><StockIcon /></el-icon>
                库存 {{ dish.stock || 0 }}
              </span>
              <span class="dish-item__meta-item">
                <el-icon><StatusIcon /></el-icon>
                更新时间：{{ dish.updatedAt || '—' }}
              </span>
            </div>

            <div class="dish-item__summary">
              <span>
                必选食材
                {{ dish.ingredients && dish.ingredients.mandatory ? dish.ingredients.mandatory.length : 0 }} 项
              </span>
              <span>
                可选食材
                {{ dish.ingredients && dish.ingredients.optional ? dish.ingredients.optional.length : 0 }} 项
              </span>
              <span>热量 {{ dish.totalCalories || 0 }} kcal</span>
              <el-button link class="dish-item__toggle" @click="toggleDishDetail(dish)">
                <span>{{ expandedDishes[dish.id] ? '收起明细' : '展开明细' }}</span>
                <el-icon
                  class="dish-item__toggle-icon"
                  :class="{ 'is-open': expandedDishes[dish.id] }"
                >
                  <ArrowDown />
                </el-icon>
              </el-button>
            </div>

            <!-- 折叠明细：食材分组 + 营养，chip 统一中性样式（评审 P1、§3.5 展开 240ms） -->
            <el-collapse-transition>
              <div v-show="expandedDishes[dish.id]" class="dish-item__detail">
                <div class="dish-detail__group">
                  <span class="dish-detail__label">必选食材</span>
                  <div class="dish-detail__tags">
                    <template
                      v-if="dish.ingredients && dish.ingredients.mandatory && dish.ingredients.mandatory.length"
                    >
                      <el-tag
                        v-for="(ingredient, index) in dish.ingredients.mandatory.slice(0, 12)"
                        :key="index"
                        effect="plain"
                        size="small"
                        class="ingredient-tag"
                      >
                        {{ ingredient }}
                      </el-tag>
                      <span
                        v-if="dish.ingredients.mandatory.length > 12"
                        class="dish-detail__more"
                      >
                        +{{ dish.ingredients.mandatory.length - 12 }}
                      </span>
                    </template>
                    <span v-else class="dish-detail__empty">无</span>
                  </div>
                </div>

                <div class="dish-detail__group">
                  <span class="dish-detail__label">可选食材</span>
                  <div class="dish-detail__tags">
                    <template
                      v-if="dish.ingredients && dish.ingredients.optional && dish.ingredients.optional.length"
                    >
                      <el-tag
                        v-for="(ingredient, index) in dish.ingredients.optional.slice(0, 12)"
                        :key="index"
                        effect="plain"
                        size="small"
                        class="ingredient-tag"
                      >
                        {{ ingredient }}
                      </el-tag>
                      <span
                        v-if="dish.ingredients.optional.length > 12"
                        class="dish-detail__more"
                      >
                        +{{ dish.ingredients.optional.length - 12 }}
                      </span>
                    </template>
                    <span v-else class="dish-detail__empty">无</span>
                  </div>
                </div>

                <div class="dish-detail__group">
                  <span class="dish-detail__label">营养成分</span>
                  <div class="dish-detail__tags">
                    <el-tag
                      v-if="dish.totalCalories"
                      effect="plain"
                      size="small"
                      class="ingredient-tag"
                    >
                      {{ dish.totalCalories }} kcal
                    </el-tag>
                    <span v-else class="dish-detail__empty">无</span>
                  </div>
                </div>
              </div>
            </el-collapse-transition>
          </div>

          <div class="dish-item__actions">
            <el-button type="primary" link @click="editDish(dish)">
              <el-icon><Edit /></el-icon>
              <span>编辑</span>
            </el-button>
            <el-button link @click="toggleDishStatus(dish)">
              <el-icon v-if="dish.statusBoolean"><CircleClose /></el-icon>
              <el-icon v-else><CircleCheck /></el-icon>
              <span>{{ dish.statusBoolean ? '下架' : '上架' }}</span>
            </el-button>
            <el-button link @click="showMenuAssociationDialog(dish)">
              <el-icon><IngredientsIcon /></el-icon>
              <span>菜单关联</span>
            </el-button>
            <el-button type="danger" link @click="deleteDish(dish)">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </el-button>
          </div>
        </div>
      </transition-group>

      <!-- 空状态：图标 + 中文说明 + 替代动作（§4.5） -->
      <AppEmptyState
        v-if="!loading && filteredDishes.length === 0"
        class="dish-list__empty"
        :icon="GoodsIcon"
        description="还没有菜品，先添加一道招牌菜吧"
        action-text="新增菜品"
        @action="openAddDishDialog"
      />

      <!-- 分页：中文文案，置于列表末尾（§4.3） -->
      <div v-if="filteredDishes.length > 0" class="dish-pagination">
        <span class="dish-pagination__total">共 {{ filteredDishes.length }} 条</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          layout="prev, pager, next"
          :total="filteredDishes.length"
          @current-change="updatePagination"
        />
      </div>
    </div>

    <!-- 添加菜品对话框 -->
    <el-dialog
      v-model="addDishDialogVisible"
      title="添加新菜品"
      width="700px"
      center
      transition="dialog-fade"
    >
      <div class="add-dish-form">
        <el-form :model="newDish" label-width="120px" status-icon class="custom-form">
          <el-form-item label="名称" prop="name" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><GoodsIcon /></el-icon>
                <span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称</span>
              </div>
            </template>
            <el-input v-model="newDish.name" placeholder="例：宫保鸡丁" />
          </el-form-item>

          <el-form-item label="价格" prop="price" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><MoneyIcon /></el-icon>
                <span>价&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;格</span>
              </div>
            </template>
            <el-input v-model.number="newDish.price" placeholder="请输入价格" type="number" />
          </el-form-item>

          <el-form-item label="分类" prop="category" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FolderIcon /></el-icon>
                <span>分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类</span>
              </div>
            </template>
            <el-select
              v-model="newDish.category"
              style="width: 100%"
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入分类"
            >
              <el-option label="主食" value="主食" />
              <el-option label="汤品" value="汤品" />
              <el-option label="饮料" value="饮料" />
              <el-option label="小吃" value="小吃" />
            </el-select>
          </el-form-item>

          <el-form-item label="库存" prop="stock" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StockIcon /></el-icon>
                <span>库&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;存</span>
              </div>
            </template>
            <el-input v-model.number="newDish.stock" placeholder="请输入库存" type="number" />
          </el-form-item>

          <el-form-item label="状态">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StatusIcon /></el-icon>
                <span>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态</span>
              </div>
            </template>
            <el-select v-model="newDish.status" style="width: 100%">
              <el-option label="上架" value="online" />
              <el-option label="即将售罄" value="almost_sold" />
              <el-option label="下架" value="offline" />
            </el-select>
          </el-form-item>

          <!-- 必选食材 -->
          <el-form-item label="必选食材" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>必选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="newMandatoryIngredient"
                  placeholder="请输入必选食材"
                  clearable
                  style="width: calc(350px - 80px)"
                  @keyup.enter="addMandatoryIngredient"
                />
                <el-button
                  type="primary"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                  @click="addMandatoryIngredient"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in newDish.ingredients.mandatory"
                  :key="index"
                  type="warning"
                  closable
                  class="ingredient-tag"
                  @close="removeMandatoryIngredient(index)"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 可选食材 -->
          <el-form-item label="可选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>可选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="newOptionalIngredient"
                  placeholder="请输入可选食材"
                  clearable
                  style="width: calc(350px - 80px)"
                  @keyup.enter="addOptionalIngredient"
                />
                <el-button
                  type="primary"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                  @click="addOptionalIngredient"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in newDish.ingredients.optional"
                  :key="index"
                  type="success"
                  closable
                  class="ingredient-tag"
                  @close="removeOptionalIngredient(index)"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 卡路里计算 -->
          <el-form-item label="总卡路里">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FlameIcon /></el-icon>
                <span>总卡路里</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.totalCalories"
              placeholder="请输入总卡路里值"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>kcal</template>
            </el-input>
          </el-form-item>

          <!-- 营养成分 -->
          <el-form-item label="蛋白质">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>蛋白质</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.protein"
              placeholder="请输入蛋白质含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>

          <el-form-item label="脂肪">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>脂肪</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.fat"
              placeholder="请输入脂肪含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>

          <el-form-item label="碳水">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>碳水</span>
              </div>
            </template>
            <el-input
              v-model.number="newDish.carbs"
              placeholder="请输入碳水含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewDish">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑菜品对话框 -->
    <el-dialog
      v-model="editDishDialogVisible"
      title="编辑菜品"
      width="700px"
      center
      transition="dialog-fade"
    >
      <div class="add-dish-form">
        <el-form :model="editDishForm" label-width="120px" status-icon class="custom-form">
          <el-form-item label="名称" prop="name" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><GoodsIcon /></el-icon>
                <span>名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称</span>
              </div>
            </template>
            <el-input v-model="editDishForm.name" placeholder="例：宫保鸡丁" />
          </el-form-item>

          <el-form-item label="价格" prop="price" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><MoneyIcon /></el-icon>
                <span>价&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;格</span>
              </div>
            </template>
            <el-input v-model.number="editDishForm.price" placeholder="请输入价格" type="number" />
          </el-form-item>

          <el-form-item label="分类" prop="category" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FolderIcon /></el-icon>
                <span>分&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;类</span>
              </div>
            </template>
            <el-select
              v-model="editDishForm.category"
              style="width: 100%"
              filterable
              allow-create
              default-first-option
              placeholder="请选择或输入分类"
            >
              <el-option label="主食" value="主食" />
              <el-option label="汤品" value="汤品" />
              <el-option label="饮料" value="饮料" />
              <el-option label="小吃" value="小吃" />
            </el-select>
          </el-form-item>

          <el-form-item label="库存" prop="stock" required>
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StockIcon /></el-icon>
                <span>库&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;存</span>
              </div>
            </template>
            <el-input v-model.number="editDishForm.stock" placeholder="请输入库存" type="number" />
          </el-form-item>

          <el-form-item label="状态">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StatusIcon /></el-icon>
                <span>状&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;态</span>
              </div>
            </template>
            <el-select v-model="editDishForm.statusString" style="width: 100%">
              <el-option label="上架" value="online" />
              <el-option label="即将售罄" value="almost_sold" />
              <el-option label="下架" value="offline" />
            </el-select>
          </el-form-item>

          <!-- 必选食材 -->
          <el-form-item label="必选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>必选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="editNewMandatoryIngredient"
                  placeholder="请输入必选食材"
                  clearable
                  style="width: calc(350px - 80px)"
                  @keyup.enter="editAddMandatoryIngredient"
                />
                <el-button
                  type="primary"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                  @click="editAddMandatoryIngredient"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in editDishForm.ingredients.mandatory"
                  :key="index"
                  type="warning"
                  closable
                  class="ingredient-tag"
                  @close="editRemoveMandatoryIngredient(index)"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 可选食材 -->
          <el-form-item label="可选食材">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><IngredientsIcon /></el-icon>
                <span>可选食材</span>
              </div>
            </template>
            <div class="optional-ingredients-container">
              <div class="input-button-row">
                <el-input
                  v-model="editNewOptionalIngredient"
                  placeholder="请输入可选食材"
                  clearable
                  style="width: calc(350px - 80px)"
                  @keyup.enter="editAddOptionalIngredient"
                />
                <el-button
                  type="primary"
                  style="margin-left: 10px"
                  class="add-ingredient-btn"
                  @click="editAddOptionalIngredient"
                >
                  添加
                </el-button>
              </div>
              <div class="ingredients-tags">
                <el-tag
                  v-for="(ingredient, index) in editDishForm.ingredients.optional"
                  :key="index"
                  type="success"
                  closable
                  class="ingredient-tag"
                  @close="editRemoveOptionalIngredient(index)"
                >
                  {{ ingredient }}
                </el-tag>
              </div>
            </div>
          </el-form-item>

          <!-- 卡路里计算 -->
          <el-form-item label="总卡路里">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><FlameIcon /></el-icon>
                <span>总卡路里</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.totalCalories"
              placeholder="请输入总卡路里值"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>kcal</template>
            </el-input>
            <div class="calorie-hint">
              <el-icon size="12"><Warning /></el-icon>
              <span>自定义食材请手动输入卡路里值</span>
            </div>
          </el-form-item>

          <!-- 营养成分 -->
          <el-form-item label="蛋白质">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>蛋白质</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.protein"
              placeholder="请输入蛋白质含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>

          <el-form-item label="脂肪">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>脂肪</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.fat"
              placeholder="请输入脂肪含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>

          <el-form-item label="碳水">
            <template #label>
              <div class="form-item-label">
                <el-icon class="label-icon"><StarIcon /></el-icon>
                <span>碳水</span>
              </div>
            </template>
            <el-input
              v-model.number="editDishForm.carbs"
              placeholder="请输入碳水含量"
              type="number"
              min="0"
              style="width: 200px"
            >
              <template #suffix>g</template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditedDish">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 菜单关联对话框 -->
    <el-dialog v-model="menuAssociationDialogVisible" title="菜品菜单关联管理" width="600px" center>
      <div class="menu-association-container">
        <div class="dialog-header">
          <h4>{{ currentDish?.name }} - 菜单关联状态</h4>
          <p class="dialog-description">管理该菜品在各个菜单中的上架/下架状态</p>
        </div>

        <div class="batch-operation">
          <el-select
            v-model="selectedStatus"
            placeholder="选择批量操作状态"
            style="width: 200px; margin-right: 10px"
          >
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
          <el-button type="primary" @click="batchUpdateDishStatus"> 批量应用到所有菜单 </el-button>
        </div>

        <el-table
          :data="associatedMenus"
          style="width: 100%; margin-top: 20px"
          border
          stripe
          v-loading="loadingMenus"
          empty-text="暂无关联菜单"
        >
          <el-table-column
            prop="name"
            label="菜单名称"
            min-width="180"
            show-overflow-tooltip
            align="center"
          >
            <template #default="{ row }">
              <div class="menu-info">
                <el-tag
                  v-if="row.type"
                  size="small"
                  style="margin-right: 8px; margin-bottom: 4px"
                  :type="getMenuTypeTagType(row.type)"
                >
                  {{ row.type }}
                </el-tag>
                <div class="menu-name">{{ row.name }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="menu_status" label="菜单状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                :type="row.menu_status === 'active' ? 'success' : 'danger'"
                size="small"
                effect="light"
              >
                {{ row.menu_status === 'active' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="dishStatus" label="菜品状态" width="120" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.dishStatus"
                :active-value="1"
                :inactive-value="0"
                @change="updateDishStatusInMenu(row)"
                :active-text="`上架`"
                :inactive-text="`下架`"
              />
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click="viewMenuDetails(row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="menuAssociationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 本页颜色、间距、圆角、字阶、动效全部取自 tokens.css 的 CSS 变量（§3、§7.1），无裸色值。 */
.dish-management-container {
  box-sizing: border-box;
  padding: 0 var(--space-6) var(--space-6);
}

/* 页头动作区控件高度取 --control-height-md（§3.4、§4.7） */
.dish-management-container :deep(.app-page-header__actions .el-button) {
  height: var(--control-height-md);
}

/* 工具条：搜索独立一行 + 数量摘要，L1 微质感（§3.4、§4.3） */
.dish-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3) var(--space-4);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--card-gap-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
}

.dish-toolbar__search {
  width: 280px;
  max-width: 100%;
}

.dish-toolbar__search-icon {
  color: var(--ink-400);
}

.dish-toolbar__summary {
  margin-left: auto;
  color: var(--ink-500);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
}

/* 工具条控件统一 --control-height-sm（§3.4） */
.dish-toolbar :deep(.el-input__wrapper) {
  min-height: var(--control-height-sm);
  border-radius: var(--radius-sm);
}

/* 批量操作条：选中后滑入（§3.5 状态切换 200ms，reduced-motion 由 token 兜底） */
.dish-batch-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-2) var(--space-4);
  margin-bottom: var(--card-gap-compact);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-lg);
  background: var(--brand-50);
}

.dish-batch-bar__all {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--ink-700);
  font-size: var(--text-body-sm);
}

.dish-batch-bar__count {
  color: var(--ink-500);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
}

.dish-batch-bar__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.dish-batch-bar__actions :deep(.el-button) {
  height: var(--control-height-sm);
}

.dish-batch-enter-active,
.dish-batch-leave-active {
  transition:
    opacity var(--duration-base) var(--ease-out),
    transform var(--duration-base) var(--ease-out);
}

.dish-batch-enter-from,
.dish-batch-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
}

.dish-list__items {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap-compact);
}

/* 菜品卡：L1 微质感摘要行，内边距取 --card-padding-compact，默认高度收在 120–160px（评审 P0、§3.4） */
.dish-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--card-padding-compact);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface-highlight);
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);
}

.dish-item:hover {
  border-color: var(--line-strong);
  box-shadow:
    inset 0 1px 0 var(--surface-highlight),
    var(--shadow-sm);
}

/* 选中态由左边界 + 复选框共同表达，不叠加浅色底（§4.6） */
.dish-item--selected {
  border-color: var(--brand-300);
  box-shadow:
    inset 3px 0 0 var(--brand-500),
    inset 0 1px 0 var(--surface-highlight);
}

.dish-item__check {
  flex-shrink: 0;
  margin-top: var(--space-1);
}

.dish-item__main {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.dish-item__head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
}

.dish-item__name {
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
  line-height: 24px;
}

.dish-item__category {
  color: var(--ink-500);
  font-size: var(--text-caption);
  line-height: 18px;
}

.dish-item__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-1) var(--space-4);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.dish-item__meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  white-space: nowrap;
}

/* 汇总行：默认只给计数，不铺原始 ingredient 串；展开开关固定在右侧（评审 P1） */
.dish-item__summary {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2) var(--space-4);
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.dish-item__toggle {
  margin-left: auto;
  height: var(--control-height-sm);
  color: var(--brand-600);
  font-size: var(--text-body-sm);
}

.dish-item__toggle-icon {
  transition: transform var(--duration-fast) var(--ease-out);
}

.dish-item__toggle-icon.is-open {
  transform: rotate(180deg);
}

/* 折叠明细：食材分组 + 营养，chip 统一中性样式（评审 P1；展开 240ms，§3.5） */
.dish-item__detail {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding-top: var(--space-2);
  border-top: 1px solid var(--line);
}

.dish-detail__group {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
}

.dish-detail__label {
  flex-shrink: 0;
  width: 72px;
  padding-top: 2px;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.dish-detail__tags {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
  gap: var(--space-1);
  min-width: 0;
}

.dish-detail__more,
.dish-detail__empty {
  color: var(--ink-400);
  font-size: var(--text-caption);
  line-height: 20px;
}

.ingredient-tag {
  border-radius: var(--radius-sm);
  font-size: var(--text-caption);
}

.dish-item__actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: var(--space-3);
  white-space: nowrap;
}

.dish-item__actions :deep(.el-button) {
  height: var(--control-height-sm);
  padding: 0;
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

.dish-item__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

/* 删除按钮与其余操作拉开间距、单独危险色（评审 P0、§4.1、§4.9） */
.dish-item__actions :deep(.el-button--danger) {
  margin-left: var(--space-3);
}

.dish-list__empty {
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
}

/* 列表过渡：只动 transform / opacity（§3.5） */
.list-move,
.list-enter-active,
.list-leave-active {
  transition:
    opacity var(--duration-base) var(--ease-out),
    transform var(--duration-base) var(--ease-out);
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

.list-leave-active {
  position: absolute;
  width: 100%;
}

/* 分页：中文文案，置于列表末尾（§4.3） */
.dish-pagination {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-3);
}

.dish-pagination__total {
  color: var(--ink-500);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
}

/* 对话框：只保留信息行布局，颜色交给全局 Element Plus 变量（§7.2） */
.add-dish-form .form-item-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

.add-dish-form .label-icon {
  color: var(--ink-400);
}

.optional-ingredients-container {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  width: 100%;
}

.input-button-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.ingredients-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
}

.dialog-footer {
  display: block;
  text-align: center;
}

.menu-association-container .dialog-header {
  margin-bottom: var(--space-4);
}

.menu-association-container .dialog-header h4 {
  margin: 0 0 var(--space-1);
  color: var(--ink-900);
  font-size: var(--text-h3);
  line-height: 24px;
}

.menu-association-container .dialog-description {
  margin: 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: 20px;
}

.menu-association-container .batch-operation {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.dish-management-container :deep(.el-dialog) {
  border-radius: var(--radius-lg);
}
</style>
