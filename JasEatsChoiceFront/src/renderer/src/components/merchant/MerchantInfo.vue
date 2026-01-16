<script setup>
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'
import { computed, onMounted, ref } from 'vue'
import {
  ElMessage,
  ElTag,
  ElIcon,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElSwitch,
  ElUpload,
  ElMessageBox,
  ElCascader,
  ElTimePicker
} from 'element-plus'
import {
  Location,
  Phone,
  Message,
  Clock,
  ShoppingBag,
  Star,
  Money,
  CircleCheck,
  CircleClose,
  Loading,
  Edit,
  Document,
  Switch
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'

const authStore = useAuthStore()
const userStore = useUserStore()
const loading = ref(false)

// 编辑对话框相关
const editDialogVisible = ref(false)
const editForm = ref({})
const editFormRef = ref(null)

// 地址选项数据（通过API获取）
const addressOptions = ref([])

// 获取地址数据
const fetchAddressOptions = async () => {
  try {
    // console.log('开始获取地址数据，API配置:', API_CONFIG.baseURL + API_CONFIG.location.cascaderData)
    const response = await api.get(API_CONFIG.location.cascaderData)
    // console.log('地址数据API响应:', response)

    if (response && response.success) {
      addressOptions.value = response.data || []
      // console.log('地址数据设置成功:', addressOptions.value)
      // 将成功获取的地址数据保存到 localStorage 中
      localStorage.setItem('addressOptions', JSON.stringify(addressOptions.value))
    } else {
      console.error('获取地址数据失败：API返回失败', response)
      // 从 localStorage 中获取缓存的地址数据
      const cachedOptions = localStorage.getItem('addressOptions')
      if (cachedOptions) {
        addressOptions.value = JSON.parse(cachedOptions)
        ElMessage.warning('地址数据加载失败，使用缓存数据')
      } else {
        ElMessage.error('获取地址数据失败，请检查网络连接')
        addressOptions.value = []
      }
    }
  } catch (error) {
    console.error('获取地址数据失败:', error)
    console.error('错误详情:', {
      name: error.name,
      message: error.message,
      stack: error.stack,
      response: error.response,
      request: error.request,
      code: error.code
    })
    // 从 localStorage 中获取缓存的地址数据
    const cachedOptions = localStorage.getItem('addressOptions')
    if (cachedOptions) {
      addressOptions.value = JSON.parse(cachedOptions)
      ElMessage.warning('地址数据加载失败，使用缓存数据')
    } else {
      ElMessage.error('获取地址数据失败，请检查网络连接')
      addressOptions.value = []
    }
  }
}

// 经营品类相关变量
const categoryOptions = ref([])
const categoryLoading = ref(false)
const quickCategoriesExpanded = ref(false) // 快捷选择是否展开
const commonCategories = ref([]) // 常用品类（从后端获取）
const allCategories = ref([]) // 所有品类（从后端获取，用于搜索）

// 切换快捷选择展开/收起
const toggleQuickCategories = () => {
  quickCategoriesExpanded.value = !quickCategoriesExpanded.value
}

// 获取常用品类数据
const fetchCommonCategories = async () => {
  try {
    // console.log('开始获取常用品类数据')
    const response = await api.get(API_CONFIG.category.common)
    // console.log('常用品类API响应:', response)

    if (response && response.success) {
      commonCategories.value = response.data || []
      // console.log('常用品类数据设置成功:', commonCategories.value)
      // 将成功获取的数据保存到 localStorage 中
      localStorage.setItem('commonCategories', JSON.stringify(commonCategories.value))
    } else {
      console.error('获取常用品类失败：API返回失败', response)
      // 从 localStorage 中获取缓存数据
      const cachedCategories = localStorage.getItem('commonCategories')
      if (cachedCategories) {
        commonCategories.value = JSON.parse(cachedCategories)
        ElMessage.warning('常用品类数据加载失败，使用缓存数据')
      } else {
        // 如果没有缓存，使用默认数据
        commonCategories.value = [
          '中式快餐',
          '火锅',
          '烧烤',
          '川菜',
          '湘菜',
          '粤菜',
          '西餐',
          '日韩料理'
        ]
        ElMessage.warning('常用品类数据加载失败，使用默认数据')
      }
    }
  } catch (error) {
    console.error('获取常用品类失败:', error)
    // 从 localStorage 中获取缓存数据
    const cachedCategories = localStorage.getItem('commonCategories')
    if (cachedCategories) {
      commonCategories.value = JSON.parse(cachedCategories)
      ElMessage.warning('常用品类数据加载失败，使用缓存数据')
    } else {
      // 如果没有缓存，使用默认数据
      commonCategories.value = [
        '中式快餐',
        '火锅',
        '烧烤',
        '川菜',
        '湘菜',
        '粤菜',
        '西餐',
        '日韩料理'
      ]
      ElMessage.warning('常用品类数据加载失败，使用默认数据')
    }
  }
}

// 获取所有品类数据
const fetchAllCategories = async () => {
  try {
    // console.log('开始获取所有品类数据')
    const response = await api.get(API_CONFIG.category.list)
    // console.log('所有品类API响应:', response)

    if (response && response.success) {
      allCategories.value = response.data || []
      // console.log('所有品类数据设置成功:', allCategories.value)
      // 将成功获取的数据保存到 localStorage 中
      localStorage.setItem('allCategories', JSON.stringify(allCategories.value))
    } else {
      console.error('获取所有品类失败：API返回失败', response)
      // 从 localStorage 中获取缓存数据
      const cachedCategories = localStorage.getItem('allCategories')
      if (cachedCategories) {
        allCategories.value = JSON.parse(cachedCategories)
      }
    }
  } catch (error) {
    console.error('获取所有品类失败:', error)
    // 从 localStorage 中获取缓存数据
    const cachedCategories = localStorage.getItem('allCategories')
    if (cachedCategories) {
      allCategories.value = JSON.parse(cachedCategories)
    }
  }
}

// 经营品类搜索方法
const remoteSearchCategory = async (query) => {
  if (!query) {
    categoryOptions.value = []
    return
  }

  categoryLoading.value = true

  try {
    // 如果还没有加载所有品类，先加载
    if (allCategories.value.length === 0) {
      await fetchAllCategories()
    }

    // 从所有品类中搜索匹配的结果
    const filtered = allCategories.value.filter((category) => category.includes(query))

    categoryOptions.value = filtered.map((category) => ({
      value: category,
      label: category
    }))
  } catch (error) {
    console.error('搜索经营品类失败:', error)
    categoryOptions.value = []
  } finally {
    categoryLoading.value = false
  }
}

// 处理选择器下拉框关闭事件
const handleCategoryVisibleChange = (visible) => {
  if (!visible) {
    categoryOptions.value = []
  }
}

// 处理选择器变化事件
const handleCategoryChange = () => {
  categoryOptions.value = []
}

// 移除经营品类
const removeCategory = (index) => {
  if (editForm.value.category && editForm.value.category.length > 0) {
    editForm.value.category.splice(index, 1)
  }
}

// 切换经营品类（用于常用品类点击，支持添加/移除）
const toggleCategory = (category) => {
  if (!editForm.value.category) {
    editForm.value.category = []
  }
  const index = editForm.value.category.indexOf(category)
  if (index > -1) {
    // 如果已选中，则移除
    editForm.value.category.splice(index, 1)
  } else {
    // 如果未选中，则添加
    editForm.value.category.push(category)
  }
}

// 搜索结果相关变量
const searchResults = ref([])
const showSearchResults = ref(false)

// 地理位置搜索定位功能
const searchLocation = async () => {
  if (
    !editForm.value.areaAddress ||
    editForm.value.areaAddress.length < 3 ||
    !editForm.value.detailAddress
  ) {
    ElMessage.warning('请先选择完整区域地址并输入详细地址')
    return
  }

  try {
    // 构建完整地址字符串
    const fullAddress = editForm.value.areaAddress.join('') + editForm.value.detailAddress
    // console.log('搜索地址:', fullAddress)

    // 调用地址搜索API
    const response = await api.get(API_CONFIG.location.search, {
      params: {
        address: fullAddress
      }
    })

    // console.log('地址搜索API响应:', response)

    if (response && response.success && response.data) {
      // 处理搜索结果，后端返回的是数组
      const results = response.data
      if (results.length > 0) {
        searchResults.value = results
        showSearchResults.value = true
        ElMessage.success(`找到 ${results.length} 个匹配地址`)
      } else {
        ElMessage.warning('未找到匹配的地址，请尝试调整搜索条件')
        showSearchResults.value = false
      }
    } else {
      ElMessage.error('地址搜索失败')
      showSearchResults.value = false
    }
  } catch (error) {
    console.error('地址搜索失败:', error)
    ElMessage.error('地址搜索失败，请检查网络连接')
    showSearchResults.value = false
  }
}

// 选择搜索结果
const selectSearchResult = (result) => {
  // console.log('选择的地址:', result)

  // 将坐标信息保存到表单中
  if (result.latitude && result.longitude) {
    editForm.value.latitude = result.latitude
    editForm.value.longitude = result.longitude
  }

  // 可以根据搜索结果优化详细地址
  if (result.address) {
    editForm.value.detailAddress = result.address
  }

  // 隐藏搜索结果下拉框
  showSearchResults.value = false
  ElMessage.success('地址选择成功')
}

// 关闭搜索结果下拉框
const closeSearchResults = () => {
  showSearchResults.value = false
}

// 从 userStore 中获取商家信息
const merchantInfo = computed(
  () =>
    userStore.merchantInfo || {
      id: authStore.merchantId,
      name: '',
      rating: 0,
      phone: '',
      email: '',
      address: '',
      avatar: '',
      status: false,
      businessHours: '',
      category: '',
      averagePrice: 0,
      businessScope: []
    }
)

// 获取商家信息
const fetchMerchantInfo = async () => {
  if (!authStore.merchantId) {
    console.error('商家ID不存在')
    return
  }

  loading.value = true
  try {
    await userStore.fetchMerchantInfo()
    console.log('商家信息获取成功:', userStore.merchantInfo)
  } catch (error) {
    console.error('获取商家信息失败:', error)
    ElMessage.error('获取商家信息失败')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取商家信息、地址数据和品类数据
onMounted(async () => {
  await fetchMerchantInfo()
  await fetchAddressOptions()
  await fetchCommonCategories()
})

// 格式化营业时间
const formatBusinessHours = (hours) => {
  if (!hours) return '暂无'

  // 如果是 JSON 对象格式 {start: "HH:mm", end: "HH:mm"}
  if (typeof hours === 'object' && hours.start && hours.end) {
    return `${hours.start} 至 ${hours.end}`
  }

  // 如果是字符串格式 HH:mm-HH:mm，转换为 "HH:mm 至 HH:mm" 格式显示
  if (typeof hours === 'string' && hours.includes('-')) {
    return hours.replace('-', ' 至 ')
  }

  return hours
}

// 格式化经营品类
const formatBusinessScope = (scope) => {
  if (!scope || scope === null || scope === '') return '暂无'

  // 处理字符串格式的数组，如 "[\"快餐\"]"
  let processedScope = scope
  if (typeof processedScope === 'string') {
    // 去除可能存在的引号和括号
    processedScope = processedScope
      .replace(/^\[|\]$/g, '')
      .replace(/\"/g, '')
      .replace(/\'/g, '')
    // 如果包含逗号，则分割成数组
    if (processedScope.includes(',')) {
      processedScope = processedScope.split(',').map((item) => item.trim())
    }
  }

  // 确保scope是数组类型
  const scopeArray = Array.isArray(processedScope) ? processedScope : [processedScope]

  // 过滤空字符串
  const filteredScope = scopeArray.filter((item) => item && item.trim())

  if (filteredScope.length === 0) return '暂无'

  return filteredScope.join('、')
}

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '暂无'

  // 如果是数组（来自级联选择器），转换为字符串
  if (Array.isArray(address)) {
    return address.join('/')
  }

  // 如果是字符串，直接返回
  return address
}

// 格式化平均价格
const formatAveragePrice = (price) => {
  if (!price) return '暂无'
  return `¥${price}/人`
}

// 点击编辑按钮
const handleEditClick = () => {
  // 初始化编辑表单数据
  const info = {
    ...merchantInfo.value
  }

  // 将地址字符串拆分为区域地址和详细地址
  if (info.address) {
    // 假设地址格式是 "省/市/区/详细地址"
    if (typeof info.address === 'string') {
      let areaAddress = []
      let detailAddress = ''

      if (info.address.includes('/')) {
        const parts = info.address.split('/')
        if (parts.length >= 3) {
          areaAddress = parts.slice(0, 3)
          detailAddress = parts.slice(3).join('/').trim()
        } else {
          areaAddress = parts
        }
      } else {
        areaAddress = [info.address]
      }

      info.areaAddress = areaAddress
      info.detailAddress = detailAddress
    }
  }

  // 确保营业时间格式正确
  if (info.businessHours) {
    // 如果是 JSON 对象格式，转换为数组格式以便 TimePicker 组件使用
    if (
      typeof info.businessHours === 'object' &&
      info.businessHours.start &&
      info.businessHours.end
    ) {
      info.businessHours = [info.businessHours.start, info.businessHours.end]
    } else if (typeof info.businessHours === 'string') {
      // 如果是字符串格式，确保格式正确
      if (info.businessHours.includes(' 至 ')) {
        info.businessHours = info.businessHours.replace(' 至 ', '-')
      }
      // 去除可能存在的重复格式（如 HH:mm-HH:mm-HH:mm-HH:mm）
      const match = info.businessHours.match(/^(\d{2}:\d{2})-(\d{2}:\d{2})$/)
      if (match) {
        info.businessHours = [match[1], match[2]]
      }
    }
  }

  // 处理经营品类格式转换（将字符串转换为数组）
  if (info.category) {
    if (typeof info.category === 'string') {
      // 如果是字符串，按顿号分割
      info.category = info.category.split('、').filter((item) => item.trim())
    } else if (!Array.isArray(info.category)) {
      // 如果是其他类型，转换为数组
      info.category = [info.category]
    }
  } else {
    // 如果没有经营品类，设置为空数组
    info.category = []
  }

  editForm.value = info
  editDialogVisible.value = true
}

// 表单验证
const validateForm = () => {
  // 简单的表单验证
  if (!editForm.value.name?.trim()) {
    ElMessage.warning('请输入商家名称')
    return false
  }
  if (editForm.value.name.length < 2 || editForm.value.name.length > 50) {
    ElMessage.warning('商家名称长度应在 2 到 50 个字符之间')
    return false
  }

  if (!editForm.value.phone?.trim()) {
    ElMessage.warning('请输入联系电话')
    return false
  }
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(editForm.value.phone)) {
    ElMessage.warning('请输入有效的手机号码')
    return false
  }

  if (!editForm.value.email?.trim()) {
    ElMessage.warning('请输入邮箱地址')
    return false
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(editForm.value.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return false
  }

  // 区域地址验证：确保选择了完整的地址（省/市/区）
  if (
    !editForm.value.areaAddress ||
    !Array.isArray(editForm.value.areaAddress) ||
    editForm.value.areaAddress.length < 3
  ) {
    ElMessage.warning('请选择完整的区域地址（省/市/区）')
    return false
  }

  // 详细地址验证：确保输入了详细地址
  if (!editForm.value.detailAddress?.trim()) {
    ElMessage.warning('请输入详细地址')
    return false
  }
  if (editForm.value.detailAddress.length < 5) {
    ElMessage.warning('详细地址长度至少5个字符')
    return false
  }

  // 营业时间验证：确保选择了营业时间
  // console.log('businessHours 类型:', typeof editForm.value.businessHours)
  // console.log('businessHours 值:', editForm.value.businessHours)

  if (!editForm.value.businessHours) {
    ElMessage.warning('请选择营业时间')
    return false
  }

  // 检查营业时间的数据类型，ElTimePicker 可能返回数组或其他类型
  if (!Array.isArray(editForm.value.businessHours) || editForm.value.businessHours.length !== 2) {
    ElMessage.warning('请选择有效的营业时间')
    return false
  }

  // 检查时间格式是否正确
  const timePattern = /^([01]\d|2[0-3]):[0-5]\d$/
  if (
    !timePattern.test(editForm.value.businessHours[0]) ||
    !timePattern.test(editForm.value.businessHours[1])
  ) {
    ElMessage.warning('请选择有效的营业时间格式')
    return false
  }

  if (!editForm.value.category || editForm.value.category.length === 0) {
    ElMessage.warning('请选择经营品类')
    return false
  }

  if (
    editForm.value.averagePrice === null ||
    editForm.value.averagePrice === undefined ||
    editForm.value.averagePrice < 0
  ) {
    ElMessage.warning('请输入有效的平均价格')
    return false
  }

  return true
}

// 保存编辑信息
const handleSaveEdit = async () => {
  if (!validateForm()) {
    return
  }

  try {
    // 处理地址数据，将区域地址和详细地址合并
    const formData = {
      ...editForm.value
    }

    // 将区域地址数组转换为字符串（用 "/" 分隔），并与详细地址合并
    if (Array.isArray(formData.areaAddress)) {
      formData.address =
        formData.areaAddress.join('/') + '/' + (formData.detailAddress || '').trim()
    }

    // 将经营品类数组转换为字符串（用 "、" 分隔）
    if (Array.isArray(formData.category)) {
      formData.category = formData.category.join('、')
    }

    // 确保营业时间格式正确，转换为 JSON 对象格式 {start: "HH:mm", end: "HH:mm"}
    if (Array.isArray(formData.businessHours) && formData.businessHours.length === 2) {
      formData.businessHours = {
        start: formData.businessHours[0].trim(),
        end: formData.businessHours[1].trim()
      }
    } else if (typeof formData.businessHours === 'string' && formData.businessHours.includes('-')) {
      const timeRange = formData.businessHours.split('-')
      if (timeRange.length === 2) {
        formData.businessHours = {
          start: timeRange[0].trim(),
          end: timeRange[1].trim()
        }
      } else {
        formData.businessHours = null
      }
    } else if (!formData.businessHours || formData.businessHours === '') {
      formData.businessHours = null
    }

    // 调用API更新商家信息
    const response = await api.put(
      API_CONFIG.merchant.update.replace('{merchantId}', authStore.merchantId),
      formData
    )
    if (response.code === '200') {
      // 更新用户存储中的信息
      await userStore.fetchMerchantInfo()
      ElMessage.success('商家信息更新成功')
      editDialogVisible.value = false
    } else {
      ElMessage.error('更新失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('更新商家信息失败:', error)
    ElMessage.error('更新商家信息失败')
  }
}

// 取消编辑
const handleCancelEdit = () => {
  editDialogVisible.value = false
  editFormRef.value?.resetFields()
}

// 表单验证规则
const editFormRules = {
  name: [
    { required: true, message: '请输入商家名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商家名称长度应在 2 到 50 个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  areaAddress: [{ required: true, message: '请选择区域地址', trigger: 'change' }],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, message: '详细地址长度至少5个字符', trigger: 'blur' }
  ],
  businessHours: [{ required: true, message: '请选择营业时间', trigger: 'change' }],
  category: [{ required: true, message: '请输入经营品类', trigger: 'blur' }],
  averagePrice: [
    { required: true, message: '请输入平均价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '平均价格必须大于等于0', trigger: 'blur' }
  ]
}
</script>

<template>
  <div class="merchant-info-card">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <ElIcon class="is-loading"><Loading /></ElIcon>
      <span>加载中...</span>
    </div>

    <!-- 商家信息卡片 -->
    <div v-else class="info-content">
      <div class="info-header">
        <div class="avatar-section">
          <img
            v-if="merchantInfo.avatar"
            :src="merchantInfo.avatar"
            class="avatar"
            alt="商家头像"
          />
          <div v-else class="avatar-placeholder">
            <ShoppingBag style="font-size: 32px; color: #409eff" />
          </div>
        </div>

        <div class="detail-section">
          <div class="merchant-header">
            <div class="merchant-name">
              {{ merchantInfo.name }}
              <ElTag
                :type="merchantInfo.status ? 'success' : 'danger'"
                size="small"
                class="status-tag"
              >
                <ElIcon v-if="merchantInfo.status"><CircleCheck /></ElIcon>
                <ElIcon v-else><CircleClose /></ElIcon>
                {{ merchantInfo.status ? '营业中' : '已打烊' }}
              </ElTag>
            </div>
            <ElButton type="primary" size="small" @click="handleEditClick" class="edit-button">
              <ElIcon><Edit /></ElIcon>
              <span>编辑信息</span>
            </ElButton>
          </div>

          <div class="merchant-rating">
            <ElTag type="warning" size="small" class="rating-tag">
              <ElIcon><Star /></ElIcon>
              <span>{{ merchantInfo.rating || 0.0 }}分</span>
            </ElTag>
          </div>

          <div class="contact-info">
            <div class="contact-item" v-if="merchantInfo.phone">
              <ElIcon class="icon"><Phone /></ElIcon>
              <span>{{ merchantInfo.phone }}</span>
            </div>
            <div class="contact-item" v-if="merchantInfo.email">
              <ElIcon class="icon"><Message /></ElIcon>
              <span>{{ merchantInfo.email }}</span>
            </div>
            <div class="contact-item" v-if="merchantInfo.address">
              <ElIcon class="icon"><Location /></ElIcon>
              <span>{{ formatAddress(merchantInfo.address) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细信息 -->
      <div class="info-details">
        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><Clock /></ElIcon>
            <span>营业时间</span>
          </div>
          <div class="detail-value">{{ formatBusinessHours(merchantInfo.businessHours) }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><ShoppingBag /></ElIcon>
            <span>经营品类</span>
          </div>
          <div class="detail-value">{{ formatBusinessScope(merchantInfo.businessScope) }}</div>
        </div>

        <div class="detail-item">
          <div class="detail-label">
            <ElIcon><Money /></ElIcon>
            <span>平均价格</span>
          </div>
          <div class="detail-value">{{ formatAveragePrice(merchantInfo.averagePrice) }}</div>
        </div>
      </div>
    </div>
  </div>
  <!-- 编辑信息对话框 -->
  <ElDialog
    v-model="editDialogVisible"
    title="编辑商家信息"
    width="600px"
    top="10%"
    transition="dialog-fade"
  >
    <div class="edit-form">
      <div class="info-item">
        <span class="info-label"
          ><ElIcon><Document /></ElIcon> 商家名称</span
        >
        <ElInput
          v-model="editForm.name"
          placeholder="请输入商家名称"
          style="width: 300px"
          clearable
        />
      </div>

      <div class="info-item">
        <span class="info-label"
          ><ElIcon><Phone /></ElIcon> 联系电话</span
        >
        <ElInput
          v-model="editForm.phone"
          placeholder="请输入联系电话"
          style="width: 300px"
          clearable
        />
      </div>

      <div class="info-item">
        <span class="info-label"
          ><ElIcon><Message /></ElIcon> 邮箱地址</span
        >
        <ElInput
          v-model="editForm.email"
          placeholder="请输入邮箱地址"
          style="width: 300px"
          clearable
        />
      </div>

      <div class="info-item">
        <span class="info-label"
          ><ElIcon><Location /></ElIcon> 区域地址</span
        >
        <ElCascader
          v-model="editForm.areaAddress"
          :options="addressOptions"
          placeholder="请选择省/市/区"
          style="width: 300px"
          clearable
          :props="{ checkStrictly: false, expandTrigger: 'click' }"
          popper-class="address-cascader-popper"
          teleported
        />
      </div>

      <div class="info-item" style="position: relative">
        <span class="info-label"
          ><ElIcon><Location /></ElIcon> 详细地址</span
        >
        <div style="display: flex; gap: 8px; align-items: center">
          <ElInput
            v-model="editForm.detailAddress"
            placeholder="请输入街道、门牌号等详细地址"
            style="width: 220px"
            clearable
          />
          <ElButton
            type="primary"
            size="small"
            @click="searchLocation"
            :disabled="
              !editForm.areaAddress ||
              editForm.areaAddress.length < 3 ||
              !editForm.detailAddress?.trim()
            "
          >
            <ElIcon><Location /></ElIcon>
            搜索定位
          </ElButton>
        </div>
        <!-- 搜索结果下拉框 -->
        <div v-if="showSearchResults" class="search-results-dropdown">
          <div class="dropdown-header">
            <span>找到 {{ searchResults.length }} 个匹配地址</span>
            <ElButton type="text" size="small" @click="closeSearchResults" style="padding: 0">
              <ElIcon><CircleClose /></ElIcon>
            </ElButton>
          </div>
          <div class="dropdown-content">
            <div
              v-for="(result, index) in searchResults"
              :key="result.id || index"
              class="result-item"
              @click="selectSearchResult(result)"
              @mouseenter="result.hover = true"
              @mouseleave="result.hover = false"
            >
              <div class="result-name">{{ result.name || '未命名地址' }}</div>
              <div class="result-address">
                {{ result.address || result.pname + result.cityname + result.adname }}
              </div>
              <div class="result-location" v-if="result.latitude && result.longitude">
                <ElIcon><Location /></ElIcon>
                {{ result.latitude.toFixed(6) }}, {{ result.longitude.toFixed(6) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="info-item">
        <span class="info-label"
          ><ElIcon><Clock /></ElIcon> 营业时间</span
        >
        <div style="width: 300px">
          <ElTimePicker
            v-model="editForm.businessHours"
            is-range
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            placeholder="请选择营业时间"
            format="HH:mm"
            value-format="HH:mm"
            clearable
          />
        </div>
      </div>

      <div class="info-item" style="position: relative; align-items: flex-start">
        <span class="info-label"
          ><ElIcon><ShoppingBag /></ElIcon> 经营品类</span
        >
        <div class="category-selector">
          <!-- 选择器 -->
          <ElSelect
            v-model="editForm.category"
            placeholder="输入品类名称或从下拉列表选择"
            style="width: 300px"
            multiple
            clearable
            filterable
            remote
            :remote-method="remoteSearchCategory"
            :loading="categoryLoading"
            allow-create
            default-first-option
            @visible-change="handleCategoryVisibleChange"
            @change="handleCategoryChange"
          >
            <ElOption
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </ElSelect>
          <!-- 常用品类快捷选择 -->
          <div class="quick-categories">
            <div class="quick-header" @click="toggleQuickCategories">
              <span class="quick-label">快捷选择</span>
              <ElIcon :class="{ rotate: !quickCategoriesExpanded }"><Switch /></ElIcon>
            </div>
            <div class="quick-tags" v-if="quickCategoriesExpanded">
              <ElTag
                v-for="tag in commonCategories"
                :key="tag"
                size="small"
                @click="toggleCategory(tag)"
                :class="[
                  'quick-tag',
                  editForm.category && editForm.category.includes(tag)
                    ? 'quick-tag-selected'
                    : 'quick-tag-default'
                ]"
              >
                {{ tag }}
              </ElTag>
            </div>
          </div>
        </div>
      </div>

      <div class="info-item">
        <span class="info-label"
          ><ElIcon><Money /></ElIcon> 平均价格</span
        >
        <ElInputNumber
          v-model="editForm.averagePrice"
          :min="0"
          :step="10"
          :precision="0"
          style="width: 300px"
          placeholder="请输入平均价格"
        />
      </div>

      <div class="info-item">
        <span class="info-label"
          ><ElIcon><Switch /></ElIcon> 营业状态</span
        >
        <ElSwitch
          v-model="editForm.status"
          active-text="营业中"
          inactive-text="已打烊"
          active-color="#409eff"
          inactive-color="#909399"
        />
      </div>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancelEdit">取消</ElButton>
        <ElButton type="primary" @click="handleSaveEdit">保存</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<style scoped lang="less">
/* 对话框淡入动画 */
:deep(.dialog-fade-enter-active),
:deep(.dialog-fade-leave-active) {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}

:deep(.dialog-fade-enter-from) {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

:deep(.dialog-fade-leave-to) {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

/* 地址级联选择器样式优化 - 修复长文字导致上级选择器左移问题 */
:deep(.address-cascader-popper) {
  /* 强制级联菜单容器使用固定布局 */
  .el-cascader__menus {
    display: flex !important;
    width: fit-content !important;
  }

  .el-cascader__menu {
    flex-shrink: 0 !important;
    /* 设置固定宽度，防止菜单列宽变化导致位移 */
    width: 160px !important;
    min-width: 160px !important;
    max-width: 160px !important;
    flex-basis: 160px !important;
  }

  .el-cascader-menu {
    /* 确保每一列都有固定宽度 */
    width: 160px !important;
    min-width: 160px !important;
    max-width: 160px !important;
    flex-basis: 160px !important;
    /* 防止菜单内容溢出 */
    overflow: hidden !important;
    box-sizing: border-box !important;

    .el-cascader-menu__item {
      overflow: hidden !important;
      text-overflow: ellipsis !important;
      white-space: nowrap !important;
      width: 100% !important;
      box-sizing: border-box !important;

      :deep(.el-cascader-menu__label) {
        overflow: hidden !important;
        text-overflow: ellipsis !important;
        white-space: nowrap !important;
        /* 确保标签文字不会撑破容器 */
        max-width: 130px !important;
        display: inline-block !important;
        vertical-align: middle !important;
        /* 强制限制最大宽度 */
        width: 100% !important;
        box-sizing: border-box !important;
      }

      /* 确保箭头图标不会导致宽度变化 */
      .el-cascader-menu__item__arrow {
        flex-shrink: 0 !important;
        width: 16px !important;
        min-width: 16px !important;
        max-width: 16px !important;
      }
    }
  }

  /* 确保整个级联菜单容器不会被撑大 */
  &.el-popper {
    width: auto !important;
    min-width: 0 !important;
    max-width: none !important;
    overflow: visible !important;
  }
}

/* 编辑表单样式 - 参考菜单管理添加菜单样式 */
.edit-form {
  padding-left: 40px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: 20px;

    .info-label {
      color: #555;
      width: 100px;
      font-weight: 500;
      font-size: 14px;
      padding-top: 5px; /* 调整标签顶部对齐 */
    }

    /* 输入框、选择框、数字输入器悬浮效果优化 - 与菜单管理保持一致 */
    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper),
    :deep(.el-input-number .el-input__wrapper) {
      border-radius: 8px;
      border: 2px solid #e5e7eb;
      transition: all 0.3s ease;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);

      &:hover {
        border-color: #91d5ff;
        box-shadow: 0 0 0 3px rgba(145, 213, 255, 0.1);
      }

      &.is-focus,
      &.is-focused {
        border-color: #40a9ff;
        box-shadow: 0 0 0 3px rgba(64, 169, 255, 0.15);
      }
    }
  }
}

/* 对话框样式优化 - 与菜单管理添加菜单保持一致 */
:deep(.el-dialog) {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

:deep(.el-dialog__header) {
  border-bottom: 2px solid rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(230, 247, 255, 0.8) 0%, rgba(186, 231, 255, 0.8) 100%);
  padding: 24px 28px;
  border-radius: 12px 12px 0 0;
}

:deep(.el-dialog__title) {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-dialog__body) {
  padding: 32px 28px;
  background-color: #fafbfc;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #f1f3f5;
  padding: 0 28px 24px;
  border-radius: 0 0 12px 12px;
  background-color: #ffffff;
}

/* 经营品类选择器样式 */
.category-selector {
  width: 300px;
}

/* 选择器内部标签显示样式 - 蓝色主题 */
:deep(.category-selector .el-select__tags) {
  .el-tag {
    background-color: #e6f7ff;
    border-color: #91d5ff;
    color: #0050b3;

    &:hover {
      background-color: #bae7ff;
      border-color: #69c0ff;
    }

    .el-tag__close {
      color: #0050b3;

      &:hover {
        color: #ffffff;
        background-color: #409eff;
      }
    }
  }
}

/* 优化选择器下拉框样式 */
:deep(.category-selector) {
  .el-select-dropdown__item {
    padding: 8px 12px;
    font-size: 13px;
    transition: all 0.2s ease;

    &:hover {
      background-color: #ecf5ff;
      color: #409eff;
    }

    &.is-selected {
      color: #409eff;
      font-weight: 600;
      background-color: #e6f7ff;
    }
  }

  .el-select__popper {
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
}

/* 已选品类标签 */
.selected-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  max-width: 300px;
}

.selected-tag {
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(64, 169, 255, 0.3);
  }
}

/* 快捷选择区域 */
.quick-categories {
  margin-top: 8px;
  max-width: 300px;
  background-color: #f5f7fa;
  border-radius: 6px;
  overflow: hidden;
}

.quick-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  cursor: pointer;
  user-select: none;
  transition: all 0.2s ease;

  &:hover {
    background-color: #ecf5ff;
  }

  .quick-label {
    font-size: 13px;
    font-weight: 500;
    color: #606266;
  }

  .el-icon {
    font-size: 14px;
    color: #909399;
    transition: transform 0.2s ease;
  }

  .rotate {
    transform: rotate(180deg);
  }
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.quick-tag {
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(64, 169, 255, 0.3);
  }

  &.quick-tag-default {
    background-color: #e6f7ff;
    border-color: #91d5ff;
    color: #0050b3;

    &:hover {
      background-color: #bae7ff;
      border-color: #69c0ff;
    }
  }

  &.quick-tag-selected {
    background-color: #409eff;
    border-color: #409eff;
    color: #ffffff;
    font-weight: 500;

    &:hover {
      background-color: #66b1ff;
      border-color: #66b1ff;
    }
  }
}

/* 搜索结果下拉框样式 */
.search-results-dropdown {
  margin-top: 8px;
  width: 328px; /* 与输入框宽度一致 */
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  position: absolute;
  left: 100px; /* 与详细地址标签宽度一致，确保对齐 */
  top: 100%;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fafafa;
  border-radius: 8px 8px 0 0;
  font-size: 12px;
  color: #606266;
}

.dropdown-header span {
  font-weight: 500;
}

.dropdown-content {
  padding: 8px 0;
}

.result-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid #f5f7fa;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item:hover {
  background-color: #f5f7fa;
}

.result-item:hover .result-name {
  color: #409eff;
}

.result-name {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.result-address {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  line-height: 1.4;
}

.result-location {
  font-size: 11px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 对话框按钮样式 - 参考编辑按钮样式 */
.dialog-footer {
  text-align: right;
  padding: 0 28px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.dialog-footer .el-button) {
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  min-width: 80px;
}

:deep(.dialog-footer .el-button--primary) {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
  box-shadow: 0 2px 8px rgba(64, 169, 255, 0.2);
}

:deep(.dialog-footer .el-button--primary:hover) {
  background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
  box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
  transform: translateY(-1px);
}

:deep(.dialog-footer .el-button--default) {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  border: 1px solid #dcdfe6;
  color: #606266;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.dialog-footer .el-button--default:hover) {
  background: linear-gradient(135deg, #e4e7ed 0%, #c0c4cc 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translateY(-1px);
}

/* 按钮点击动画 */
:deep(.dialog-footer .el-button:active) {
  transform: translateY(0);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
}

/* 搜索定位按钮样式美化 */
:deep(.info-item .el-button) {
  border-radius: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: hidden;
}

:deep(.info-item .el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
}

/* 按钮点击效果 */
:deep(.el-button) {
  &:active {
    transform: translateY(0);
  }
}

/* 按钮加载状态 */
:deep(.el-button.is-loading) {
  opacity: 0.7;
  cursor: not-allowed;
}

.merchant-info-card {
  margin-bottom: 24px;
  padding: 24px;
  border: 1px solid #ebeef5;
  border-radius: 16px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: #606266;
    font-size: 14px;

    .el-icon {
      margin-right: 8px;
      font-size: 20px;
    }
  }

  .info-content {
    .info-header {
      display: flex;
      align-items: flex-start;
      gap: 24px;
      margin-bottom: 20px;
      padding-bottom: 20px;
      border-bottom: 1px solid #f0f0f0;

      .avatar-section {
        flex-shrink: 0;

        .avatar {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          object-fit: cover;
          border: 3px solid #f0f9ff;
        }

        .avatar-placeholder {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          background-color: #f0f9ff;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .detail-section {
        flex: 1;

        .merchant-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 12px;

          .merchant-name {
            font-size: 24px;
            font-weight: 700;
            color: #303133;
            display: flex;
            align-items: center;
            gap: 12px;
          }

          .merchant-actions {
            display: flex;
            align-items: center;
            gap: 12px;

            .merchant-status {
              .status-text {
                margin-left: 4px;
              }
            }

            .edit-button {
              display: flex;
              align-items: center;
              gap: 4px;
              background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
              border: 1px solid #91d5ff;
              color: #0050b3;
              border-radius: 8px;
              padding: 8px 16px;
              font-weight: 500;
              box-shadow: 0 2px 8px rgba(64, 169, 255, 0.2);
              transition: all 0.3s ease;

              &:hover {
                background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
                box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
                transform: translateY(-1px);
              }
            }
          }
        }

        .merchant-rating {
          margin-bottom: 12px;
        }

        .contact-info {
          display: flex;
          flex-wrap: wrap;
          gap: 20px;

          .contact-item {
            display: flex;
            align-items: center;
            font-size: 14px;
            color: #606266;

            .icon {
              margin-right: 6px;
              color: #409eff;
            }
          }
        }
      }
    }

    .info-details {
      display: flex;
      flex-wrap: wrap;
      gap: 32px;

      .detail-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .detail-label {
          display: flex;
          align-items: center;
          min-width: 100px;
          color: #606266;
          font-size: 14px;
          font-weight: 500;

          .el-icon {
            margin-right: 6px;
            color: #409eff;
          }
        }

        .detail-value {
          color: #303133;
          font-size: 14px;
          font-weight: 400;
        }
      }
    }
  }
}

// 状态标签和评分标签样式优化
.status-tag,
.rating-tag {
  cursor: pointer;
  line-height: 28px;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  border-radius: 8px;
  padding: 4px 12px;

  :deep(.el-tag__content) {
    white-space: nowrap;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  &:hover {
    opacity: 0.8;
  }
}

// 编辑按钮样式优化
.edit-button {
  background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
  border: 1px solid #91d5ff;
  color: #0050b3;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(64, 169, 255, 0.2);
  transition: all 0.3s ease;

  &:hover {
    background: linear-gradient(135deg, #bae7ff 0%, #91d5ff 100%);
    box-shadow: 0 4px 12px rgba(64, 169, 255, 0.3);
    transform: translateY(-1px);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .merchant-info-card {
    padding: 16px;

    .info-header {
      flex-direction: column;
      align-items: center;
      text-align: center;

      .detail-section {
        .merchant-header {
          flex-direction: column;
          gap: 8px;

          .merchant-name {
            font-size: 20px;
          }

          .merchant-actions {
            flex-direction: column;
            gap: 8px;
          }
        }

        .contact-info {
          justify-content: center;
        }
      }
    }

    .info-details {
      flex-direction: column;
      gap: 16px;
    }
  }
}
</style>
