<script setup>
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'
import { computed, onMounted, ref, watch } from 'vue'
import {
  ElMessage,
  ElTag,
  ElIcon,
  ElButton,
  ElDialog,
  ElInput,
  ElInputNumber,
  ElSwitch,
  ElUpload,
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
import { getAvatarUrl } from '../../utils/avatar'

const authStore = useAuthStore()
const userStore = useUserStore()
const loading = ref(false)

// 头像上传相关
const avatarUploadDialogVisible = ref(false)
const avatarFileList = ref([])
const avatarUploading = ref(false)
const tempAvatarUrl = ref('')
const avatarFile = ref(null)
const avatarLoadError = ref(false)

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

// 当头像地址变化时，重置加载失败标记，避免旧错误状态阻塞新头像显示
watch(
  () => merchantInfo.value?.avatar,
  () => {
    avatarLoadError.value = false
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

// 头像加载失败时回退到占位符
const handleAvatarError = () => {
  avatarLoadError.value = true
}

// 打开头像上传对话框
const handleAvatarUpload = () => {
  avatarUploadDialogVisible.value = true
  tempAvatarUrl.value = merchantInfo.value.avatar ? getAvatarUrl(merchantInfo.value.avatar) : ''
  avatarFileList.value = []
  avatarFile.value = null
}

// 关闭头像上传对话框
const handleCloseAvatarDialog = () => {
  avatarUploadDialogVisible.value = false
  tempAvatarUrl.value = ''
  avatarFileList.value = []
  avatarFile.value = null
}

// 头像上传前的处理
const handleBeforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  // 保存文件对象
  avatarFile.value = file

  // 读取文件并转换为 Base64 用于预览
  const reader = new FileReader()
  reader.onload = (e) => {
    tempAvatarUrl.value = e.target.result
  }
  reader.readAsDataURL(file)
  return true // 阻止自动上传
}

// 头像文件变化处理
const handleAvatarChange = (uploadFile) => {
  if (uploadFile.raw) {
    handleBeforeAvatarUpload(uploadFile.raw)
  }
}

// 保存头像
const handleSaveAvatar = async () => {
  if (!avatarFile.value) {
    ElMessage.warning('请先选择头像图片')
    return
  }

  avatarUploading.value = true
  try {
    // 创建 FormData 对象
    const formData = new FormData()
    formData.append('avatar', avatarFile.value)

    // 调用API上传头像 - 使用POST方法，发送FormData
    const response = await api.post(
      API_CONFIG.merchant.avatar.replace('{merchantId}', authStore.merchantId),
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )

    if (response.code === '200') {
      // 接口返回新头像地址时，先做本地回填，避免等待刷新期间的显示空窗
      if (typeof response.data === 'string' && response.data) {
        userStore.updateMerchantAvatar(response.data)
      }

      // 更新商家信息
      await userStore.fetchMerchantInfo()
      avatarLoadError.value = false
      ElMessage.success('头像更新成功')
      handleCloseAvatarDialog()
    } else {
      ElMessage.error('头像更新失败：' + (response.data?.message || '未知错误'))
    }
  } catch (error) {
    console.error('更新头像失败:', error)
    ElMessage.error('头像更新失败')
  } finally {
    avatarUploading.value = false
  }
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
          <div class="avatar-wrapper">
            <img
              v-if="merchantInfo.avatar && !avatarLoadError"
              :src="getAvatarUrl(merchantInfo.avatar)"
              class="avatar"
              alt="商家头像"
              @error="handleAvatarError"
            />
            <div v-else class="avatar-placeholder">
              <ShoppingBag style="font-size: 2.286rem /* 原值: 32px */; color: #409eff" />
            </div>
            <ElButton
              type="primary"
              size="small"
              circle
              class="avatar-edit-btn"
              @click="handleAvatarUpload"
            >
              <ElIcon><Edit /></ElIcon>
            </ElButton>
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

  <!-- 头像上传对话框 -->
  <ElDialog
    v-model="avatarUploadDialogVisible"
    title="更换头像"
    width="500px"
    :before-close="handleCloseAvatarDialog"
  >
    <div class="avatar-upload-content">
      <div class="avatar-preview">
        <img
          v-if="tempAvatarUrl"
          :src="tempAvatarUrl"
          class="preview-avatar"
          alt="头像预览"
        />
        <div v-else class="preview-avatar-placeholder">
          <ShoppingBag style="font-size: 64px; color: #c0c4cc" />
          <p>暂无头像</p>
        </div>
      </div>

      <ElUpload
        class="avatar-uploader"
        :show-file-list="false"
        :on-change="handleAvatarChange"
        :auto-upload="false"
        accept="image/*"
      >
        <ElButton type="primary">
          <ElIcon><Edit /></ElIcon>
          选择图片
        </ElButton>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 JPG/PNG/GIF 格式的图片，且不超过 2MB
          </div>
        </template>
      </ElUpload>
    </div>

    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCloseAvatarDialog">取消</ElButton>
        <ElButton
          type="primary"
          @click="handleSaveAvatar"
          :loading="avatarUploading"
          :disabled="!tempAvatarUrl"
        >
          保存
        </ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

// ===== 对话框动画 =====
:deep(.dialog-fade-enter-active),
:deep(.dialog-fade-leave-active) {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

:deep(.dialog-fade-enter-from) {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

:deep(.dialog-fade-leave-to) {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

// ===== 地址级联选择器样式 =====
:deep(.address-cascader-popper) {
  .el-cascader__menus {
    display: flex !important;
    width: fit-content !important;
  }

  .el-cascader__menu {
    flex-shrink: 0 !important;
    width: 160px !important;
    min-width: 160px !important;
    max-width: 160px !important;
    flex-basis: 160px !important;
  }

  .el-cascader-menu {
    width: 160px !important;
    min-width: 160px !important;
    max-width: 160px !important;
    flex-basis: 160px !important;
    overflow: hidden !important;
    box-sizing: border-box !important;

    .el-cascader-menu__item {
      overflow: hidden !important;
      text-overflow: ellipsis !important;
      white-space: nowrap !important;
      width: 100% !important;
      box-sizing: border-box !important;
    }
  }

  &.el-popper {
    width: auto !important;
    min-width: 0 !important;
    max-width: none !important;
    overflow: visible !important;
  }
}

// ===== 编辑表单样式 =====
.edit-form {
  padding-left: 40px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 4px;
    margin-bottom: @nordic-space-lg;

    .info-label {
      color: @merchant-text-sec;
      width: 100px;
      font-weight: 500;
      font-size: @nordic-text-base;
      padding-top: 5px;
    }

    :deep(.el-input__wrapper),
    :deep(.el-select__wrapper),
    :deep(.el-input-number .el-input__wrapper) {
      border-radius: @nordic-radius-sm;
      border: 2px solid @merchant-border;
      transition: all @nordic-transition-base ease;
      box-shadow: 0 1px 2px @merchant-shadow;

      &:hover {
        border-color: @merchant-primary;
        box-shadow: 0 0 0 3px @merchant-primary-light;
      }

      &.is-focus,
      &.is-focused {
        border-color: @merchant-primary;
        box-shadow: 0 0 0 3px @merchant-primary-light;
      }
    }
  }
}

// ===== 对话框样式 =====
:deep(.el-dialog) {
  border-radius: @nordic-radius-lg;
  box-shadow: 0 4px 20px @merchant-shadow;
}

:deep(.el-dialog__header) {
  border-bottom: 2px solid @merchant-primary-light;
  background: linear-gradient(135deg, @merchant-primary-light, @merchant-surface);
  padding: 24px 28px;
  border-radius: @nordic-radius-lg @nordic-radius-lg 0 0;
}

:deep(.el-dialog__title) {
  font-size: @nordic-text-lg;
  font-weight: 600;
  color: @merchant-primary-dark;
}

:deep(.el-dialog__body) {
  padding: @nordic-space-xl 28px;
  background-color: @merchant-surface-alt;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid @merchant-divider;
  padding: 0 28px 24px;
  border-radius: 0 0 @nordic-radius-lg @nordic-radius-lg;
  background-color: @merchant-surface;
}

// ===== 经营品类选择器样式 =====
.category-selector {
  width: 300px;
}

:deep(.category-selector .el-select__tags) {
  .el-tag {
    background-color: @merchant-primary-light;
    border-color: @merchant-primary;
    color: @merchant-primary-dark;

    &:hover {
      background-color: @merchant-primary;
      border-color: @merchant-primary-dark;
      color: @merchant-surface;
    }

    .el-tag__close {
      color: @merchant-primary-dark;

      &:hover {
        color: @merchant-surface;
        background-color: @merchant-primary;
      }
    }
  }
}

:deep(.category-selector) {
  .el-select-dropdown__item {
    padding: 8px 12px;
    font-size: @nordic-text-sm;
    transition: all 0.2s ease;

    &:hover {
      background-color: @merchant-primary-light;
      color: @merchant-primary;
    }

    &.is-selected {
      color: @merchant-primary;
      font-weight: 600;
      background-color: @merchant-primary-light;
    }
  }
}

// ===== 快捷选择区域 =====
.quick-categories {
  margin-top: @nordic-space-sm;
  max-width: 300px;
  background-color: @merchant-surface-alt;
  border-radius: @nordic-radius-sm;
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
    background-color: @merchant-primary-light;
  }

  .quick-label {
    font-size: @nordic-text-sm;
    font-weight: 500;
    color: @merchant-text-sec;
  }

  .el-icon {
    font-size: @nordic-text-base;
    color: @merchant-text-muted;
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
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.quick-tag {
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(74, 122, 77, 0.2);
  }

  &.quick-tag-default {
    background-color: @merchant-primary-light;
    border-color: @merchant-primary;
    color: @merchant-primary-dark;

    &:hover {
      background-color: @merchant-primary;
      border-color: @merchant-primary-dark;
      color: @merchant-surface;
    }
  }

  &.quick-tag-selected {
    background-color: @merchant-primary;
    border-color: @merchant-primary;
    color: @merchant-surface;
    font-weight: 500;

    &:hover {
      background-color: @merchant-primary-dark;
      border-color: @merchant-primary-dark;
    }
  }
}

// ===== 搜索结果下拉框样式 =====
.search-results-dropdown {
  margin-top: @nordic-space-sm;
  width: 328px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid @merchant-border;
  border-radius: @nordic-radius-sm;
  background-color: @merchant-surface;
  box-shadow: 0 2px 12px @merchant-shadow;
  z-index: 1000;
  position: absolute;
  left: 100px;
  top: 100%;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px @nordic-space-md;
  border-bottom: 1px solid @merchant-divider;
  background-color: @merchant-surface-alt;
  border-radius: @nordic-radius-sm @nordic-radius-sm 0 0;
  font-size: @nordic-text-xs;
  color: @merchant-text-sec;

  span {
    font-weight: 500;
  }
}

.dropdown-content {
  padding: 8px 0;
}

.result-item {
  padding: 12px @nordic-space-md;
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid @merchant-divider;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: @merchant-surface-alt;

    .result-name {
      color: @merchant-primary;
    }
  }
}

.result-name {
  font-weight: 500;
  font-size: @nordic-text-base;
  color: @merchant-text;
  margin-bottom: 4px;
}

.result-address {
  font-size: @nordic-text-xs;
  color: @merchant-text-sec;
  margin-bottom: 4px;
  line-height: 1.4;
}

.result-location {
  font-size: 11px;
  color: @merchant-text-muted;
  display: flex;
  align-items: center;
  gap: 4px;
}

// ===== 对话框按钮样式 =====
.dialog-footer {
  text-align: right;
  padding: 0 28px 24px;
  display: flex;
  justify-content: flex-end;
  gap: @nordic-space-md;
}

:deep(.dialog-footer .el-button) {
  padding: 8px @nordic-space-md;
  border-radius: @nordic-radius-sm;
  font-weight: 500;
  font-size: @nordic-text-base;
  transition: all @nordic-transition-base ease;
  min-width: 80px;
}

:deep(.dialog-footer .el-button--primary) {
  background: @merchant-primary;
  border: 1px solid @merchant-primary;
  color: @merchant-surface;
  box-shadow: 0 2px 8px rgba(74, 122, 77, 0.2);

  &:hover {
    background: @merchant-primary-dark;
    border-color: @merchant-primary-dark;
    box-shadow: 0 4px 12px rgba(74, 122, 77, 0.3);
    transform: translateY(-1px);
  }
}

:deep(.dialog-footer .el-button--default) {
  background: @merchant-surface-alt;
  border: 1px solid @merchant-border;
  color: @merchant-text-sec;
  box-shadow: 0 2px 8px @merchant-shadow;

  &:hover {
    background: @merchant-divider;
    box-shadow: 0 4px 12px @merchant-shadow-hover;
    transform: translateY(-1px);
  }
}

:deep(.dialog-footer .el-button:active) {
  transform: translateY(0);
  box-shadow: 0 1px 4px @merchant-shadow;
}

:deep(.info-item .el-button) {
  border-radius: @nordic-radius-sm;
  transition: all @nordic-transition-base ease;
  box-shadow: 0 2px 6px @merchant-shadow;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(74, 122, 77, 0.2);
  }
}

:deep(.el-button) {
  &:active {
    transform: translateY(0);
  }
}

:deep(.el-button.is-loading) {
  opacity: 0.7;
  cursor: not-allowed;
}

// ===== 商家信息卡片 =====
.merchant-info-card {
  margin-bottom: @nordic-space-lg;
  padding: @nordic-space-lg;
  background: @merchant-surface;
  border-radius: @nordic-radius-lg;
  border: 1px solid @merchant-border;
  border-left: 4px solid @merchant-secondary;
  box-shadow: 0 1px 4px @merchant-shadow;
  transition: all @nordic-transition-base ease;

  &:hover {
    box-shadow: 0 8px 24px @merchant-shadow-hover;
    border-color: @merchant-primary;
    transform: translateY(-3px);
  }

  .loading-container {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px;
    color: @merchant-text-sec;
    font-size: @nordic-text-base;

    .el-icon {
      margin-right: @nordic-space-sm;
      font-size: @nordic-text-lg;
    }
  }

  .info-content {
    .info-header {
      display: flex;
      align-items: flex-start;
      gap: @nordic-space-lg;
      margin-bottom: @nordic-space-lg;
      padding-bottom: @nordic-space-lg;
      border-bottom: 1px solid @merchant-divider;

      .avatar-section {
        flex-shrink: 0;

        .avatar-wrapper {
          position: relative;
          display: inline-block;

          .avatar {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid @merchant-primary-light;
          }

          .avatar-placeholder {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background-color: @merchant-primary-light;
            display: flex;
            align-items: center;
            justify-content: center;
          }

          .avatar-edit-btn {
            position: absolute;
            bottom: 0;
            right: 0;
            width: 28px;
            height: 28px;
            padding: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            border: 2px solid @merchant-surface;
            box-shadow: 0 2px 8px @merchant-shadow;

            &:hover {
              transform: scale(1.1);
            }
          }
        }
      }

      .detail-section {
        flex: 1;

        .merchant-header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: @nordic-space-md;

          .merchant-name {
            font-size: @nordic-text-xl;
            font-weight: 700;
            color: @merchant-text;
            display: flex;
            align-items: center;
            gap: @nordic-space-md;
          }
        }

        .merchant-rating {
          margin-bottom: @nordic-space-md;
        }

        .contact-info {
          display: flex;
          flex-wrap: wrap;
          gap: @nordic-space-lg;

          .contact-item {
            display: flex;
            align-items: center;
            font-size: @nordic-text-base;
            color: @merchant-text-sec;

            .icon {
              margin-right: 6px;
              color: @merchant-primary;
            }
          }
        }
      }
    }

    .info-details {
      display: flex;
      flex-wrap: wrap;
      gap: @nordic-space-xl;

      .detail-item {
        display: flex;
        align-items: center;
        gap: @nordic-space-sm;

        .detail-label {
          display: flex;
          align-items: center;
          min-width: 100px;
          color: @merchant-text-sec;
          font-size: @nordic-text-base;
          font-weight: 500;

          .el-icon {
            margin-right: 6px;
            color: @merchant-primary;
          }
        }

        .detail-value {
          color: @merchant-text;
          font-size: @nordic-text-base;
          font-weight: 400;
        }
      }
    }
  }
}

// ===== 状态标签和评分标签 =====
.status-tag,
.rating-tag {
  cursor: pointer;
  line-height: 28px;
  white-space: nowrap;
  display: inline-flex;
  align-items: center;
  border-radius: @nordic-radius-sm;
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

// ===== 编辑按钮 =====
.edit-button {
  background: @merchant-primary;
  border: 1px solid @merchant-primary;
  color: @merchant-surface;
  border-radius: @nordic-radius-sm;
  padding: 8px @nordic-space-md;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(74, 122, 77, 0.2);
  transition: all @nordic-transition-base ease;

  &:hover {
    background: @merchant-primary-dark;
    border-color: @merchant-primary-dark;
    box-shadow: 0 4px 12px rgba(74, 122, 77, 0.3);
    transform: translateY(-1px);
  }
}

// ===== 头像上传对话框 =====
.avatar-upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: @nordic-space-lg;
  padding: 20px 0;
}

.avatar-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: @nordic-space-md;

  .preview-avatar {
    width: 150px;
    height: 150px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid @merchant-primary-light;
    box-shadow: 0 4px 16px @merchant-shadow;
  }

  .preview-avatar-placeholder {
    width: 150px;
    height: 150px;
    border-radius: 50%;
    background-color: @merchant-surface-alt;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border: 4px solid @merchant-border;
    box-shadow: 0 4px 16px @merchant-shadow;

    p {
      margin: 8px 0 0 0;
      font-size: @nordic-text-base;
      color: @merchant-text-muted;
    }
  }
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: @nordic-space-sm;

  .el-button {
    border-radius: @nordic-radius-sm;
    padding: 10px 20px;
    font-weight: 500;
  }

  .el-upload__tip {
    font-size: @nordic-text-xs;
    color: @merchant-text-muted;
    line-height: 1.5;
    text-align: center;
  }
}

// ===== 响应式 =====
@media (max-width: 768px) {
  .merchant-info-card {
    padding: @nordic-space-md;

    .info-header {
      flex-direction: column;
      align-items: center;
      text-align: center;

      .detail-section {
        .merchant-header {
          flex-direction: column;
          gap: @nordic-space-sm;

          .merchant-name {
            font-size: @nordic-text-lg;
          }
        }

        .contact-info {
          justify-content: center;
        }
      }
    }

    .info-details {
      flex-direction: column;
      gap: @nordic-space-md;
    }
  }
}
</style>
