<template>
  <div class="diet-record-container">
    <!-- 顶部日历选择区域 -->
    <div class="calendar-section">
      <div class="section-header">
        <div class="section-title">
          <el-icon class="title-icon"><CalendarIcon /></el-icon>
          <span>饮食记录</span>
        </div>
        <div class="calendar-control">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            format="YYYY年MM月DD日"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
            size="large"
            class="calendar-picker"
            popper-class="custom-date-picker"
            :clearable="false"
          >
            <template #suffix-icon>
              <el-icon class="calendar-suffix-icon"><CalendarIcon /></el-icon>
            </template>
          </el-date-picker>
          <el-button type="primary" size="small" class="add-btn" @click="openAddRecordDialog">
            <el-icon><Plus /></el-icon>
            添加记录
          </el-button>
        </div>
      </div>
    </div>

    <!-- 优化后的统计卡片 -->
    <div class="stats-card">
      <div class="stat-item">
        <div class="stat-number">{{ totalCalories }}<span class="stat-unit">kcal</span></div>
        <div class="stat-label">总卡路里</div>
        <el-progress
          v-if="dailyCalorieGoal > 0"
          :percentage="calorieProgress"
          :color="progressColor"
          :show-text="false"
          class="stat-progress"
          :stroke-width="6"
        />
        <div v-if="dailyCalorieGoal > 0" class="stat-detail">目标: {{ dailyCalorieGoal }} kcal</div>
      </div>

      <div class="stat-divider"></div>

      <div class="stat-item">
        <div class="stat-number">{{ dietRecords.length }}</div>
        <div class="stat-label">记录条数</div>
      </div>

      <div class="stat-divider" v-if="showNutrientStats"></div>

      <!-- 营养素统计 -->
      <template v-if="showNutrientStats">
        <div class="stat-item nutrient-stat">
          <div class="nutrient-value">{{ totalProtein }}<span class="nutrient-unit">g</span></div>
          <div class="nutrient-label">蛋白质</div>
        </div>

        <div class="stat-item nutrient-stat">
          <div class="nutrient-value">{{ totalFat }}<span class="nutrient-unit">g</span></div>
          <div class="nutrient-label">脂肪</div>
        </div>

        <div class="stat-item nutrient-stat">
          <div class="nutrient-value">
            {{ totalCarbohydrate }}<span class="nutrient-unit">g</span>
          </div>
          <div class="nutrient-label">碳水</div>
        </div>
      </template>
    </div>

    <!-- 饮食记录显示区域 -->
    <div class="records-section">
      <!-- 骨架屏加载 -->
      <div v-if="loading" class="skeleton-container">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated style="margin-top: 20px" />
        <el-skeleton :rows="3" animated style="margin-top: 20px" />
      </div>

      <!-- 饮食记录内容 -->
      <div v-else class="meal-sections">
        <!-- 早餐 -->
        <div v-if="getMealsByType('breakfast').length > 0" class="meal-section">
          <div
            class="meal-section-header breakfast"
            @click="toggleSection('breakfast')"
            style="cursor: pointer"
          >
            <el-icon class="meal-icon"><Sunrise /> </el-icon>
            <span class="meal-section-title">早餐</span>
            <el-tag size="small" type="success" class="meal-count">
              {{ getMealsByType('breakfast').length }}
            </el-tag>
            <el-icon :class="{ 'rotate-180': !expandedSections.breakfast }" class="arrow-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <Transition name="collapse">
            <div v-show="expandedSections.breakfast" class="meal-records">
              <DietRecordCard
                v-for="record in getMealsByType('breakfast')"
                :key="record.id"
                :record="record"
                @edit="openEditRecordDialog"
                @delete="openDeleteConfirm"
              />
            </div>
          </Transition>
        </div>

        <!-- 午餐 -->
        <div v-if="getMealsByType('lunch').length > 0" class="meal-section">
          <div
            class="meal-section-header lunch"
            @click="toggleSection('lunch')"
            style="cursor: pointer"
          >
            <el-icon class="meal-icon"><Sunny /> </el-icon>
            <span class="meal-section-title">午餐</span>
            <el-tag size="small" type="warning" class="meal-count">
              {{ getMealsByType('lunch').length }}
            </el-tag>
            <el-icon :class="{ 'rotate-180': !expandedSections.lunch }" class="arrow-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <Transition name="collapse">
            <div v-show="expandedSections.lunch" class="meal-records">
              <DietRecordCard
                v-for="record in getMealsByType('lunch')"
                :key="record.id"
                :record="record"
                @edit="openEditRecordDialog"
                @delete="openDeleteConfirm"
              />
            </div>
          </Transition>
        </div>

        <!-- 晚餐 -->
        <div v-if="getMealsByType('dinner').length > 0" class="meal-section">
          <div
            class="meal-section-header dinner"
            @click="toggleSection('dinner')"
            style="cursor: pointer"
          >
            <el-icon class="meal-icon"><Moon /> </el-icon>
            <span class="meal-section-title">晚餐</span>
            <el-tag size="small" type="danger" class="meal-count">
              {{ getMealsByType('dinner').length }}
            </el-tag>
            <el-icon :class="{ 'rotate-180': !expandedSections.dinner }" class="arrow-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <Transition name="collapse">
            <div v-show="expandedSections.dinner" class="meal-records">
              <DietRecordCard
                v-for="record in getMealsByType('dinner')"
                :key="record.id"
                :record="record"
                @edit="openEditRecordDialog"
                @delete="openDeleteConfirm"
              />
            </div>
          </Transition>
        </div>

        <!-- 加餐 -->
        <div v-if="getMealsByType('snack').length > 0" class="meal-section">
          <div
            class="meal-section-header snack"
            @click="toggleSection('snack')"
            style="cursor: pointer"
          >
            <el-icon class="meal-icon"><Coffee /> </el-icon>
            <span class="meal-section-title">加餐</span>
            <el-tag size="small" type="info" class="meal-count">
              {{ getMealsByType('snack').length }}
            </el-tag>
            <el-icon :class="{ 'rotate-180': !expandedSections.snack }" class="arrow-icon">
              <ArrowDown />
            </el-icon>
          </div>
          <Transition name="collapse">
            <div v-show="expandedSections.snack" class="meal-records">
              <DietRecordCard
                v-for="record in getMealsByType('snack')"
                :key="record.id"
                :record="record"
                @edit="openEditRecordDialog"
                @delete="openDeleteConfirm"
              />
            </div>
          </Transition>
        </div>
      </div>

      <!-- 优化后的空数据提示 -->
      <div v-if="!loading && dietRecords.length === 0" class="empty-records">
        <el-empty
          image="https://cdn-icons-png.flaticon.com/128/4385/4385277.png"
          description="暂无饮食记录"
        >
          <template #bottom>
            <el-button type="primary" class="add-empty-btn" @click="openAddRecordDialog">
              <el-icon><Plus /></el-icon>
              开始记录第一餐
            </el-button>
            <div class="empty-tips">💡 小提示：记录饮食可以帮助你更好地管理健康</div>
          </template>
        </el-empty>
      </div>
    </div>

    <!-- 共用表单组件 -->
    <DietRecordForm
      v-model="formDialogVisible"
      :mode="formMode"
      :record="currentEditRecord"
      :loading="submitLoading"
      @submit="handleFormSubmit"
    />

    <!-- 删除确认弹窗 -->
    <el-dialog
      title="删除确认"
      v-model="deleteConfirmVisible"
      width="400px"
      transition="dialog-fade"
    >
      <div class="delete-confirm-content">
        <el-icon class="delete-icon"><Warning /></el-icon>
        <p>您确定要删除这条饮食记录吗？</p>
        <p class="delete-record-info">记录：{{ currentDeleteRecord?.foodName }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteConfirmVisible = false">取消</el-button>
          <el-button type="danger" @click="submitDeleteRecord" :loading="deleteLoading">
            确定删除
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Calendar as CalendarIcon,
  Plus,
  Sunrise,
  Sunny,
  Moon,
  Coffee,
  ArrowDown,
  Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import DietRecordCard from '../../components/DietRecordCard.vue'
import DietRecordForm from '../../components/DietRecordForm.vue'
import { mealTypeToChinese, mealTypeToEnglish } from '../../utils/mealTypeUtils.js'
import { debounce } from 'lodash-es'

// ==================== 状态管理 ====================

// 展开/折叠状态
const expandedSections = ref({
  breakfast: true,
  lunch: true,
  dinner: true,
  snack: true
})

// 日历选择的日期，默认是今天
const selectedDate = ref(new Date().toISOString().split('T')[0])

// 饮食记录数据
const dietRecords = ref([])

// 加载状态
const loading = ref(false)

// 提交加载状态
const submitLoading = ref(false)

// 删除加载状态
const deleteLoading = ref(false)

// 表单相关状态
const formDialogVisible = ref(false)
const formMode = ref('add') // 'add' or 'edit'
const currentEditRecord = ref({})

// 删除确认状态
const deleteConfirmVisible = ref(false)
const currentDeleteRecord = ref(null)

// 每日卡路里目标（可以从用户设置中获取）
const dailyCalorieGoal = ref(2000)

// ==================== 计算属性 ====================

// 计算总卡路里
const totalCalories = computed(() => {
  return dietRecords.value.reduce((total, record) => total + (record.calories || 0), 0)
})

// 计算卡路里进度百分比
const calorieProgress = computed(() => {
  if (dailyCalorieGoal.value === 0) return 0
  const progress = (totalCalories.value / dailyCalorieGoal.value) * 100
  return Math.min(Math.round(progress), 100)
})

// 进度条颜色
const progressColor = computed(() => {
  const progress = calorieProgress.value
  if (progress < 50) return '#67c23a'
  if (progress < 80) return '#e6a23c'
  if (progress <= 100) return '#f56c6c'
  return '#f56c6c'
})

// 是否显示营养素统计
const showNutrientStats = computed(() => {
  return totalProtein.value > 0 || totalFat.value > 0 || totalCarbohydrate.value > 0
})

// 计算总蛋白质
const totalProtein = computed(() => {
  return dietRecords.value.reduce((total, record) => total + (record.protein || 0), 0).toFixed(1)
})

// 计算总脂肪
const totalFat = computed(() => {
  return dietRecords.value.reduce((total, record) => total + (record.fat || 0), 0).toFixed(1)
})

// 计算总碳水化合物
const totalCarbohydrate = computed(() => {
  return dietRecords.value
    .reduce((total, record) => total + (record.carbohydrate || 0), 0)
    .toFixed(1)
})

// ==================== 方法 ====================

// 切换展开/折叠
const toggleSection = (section) => {
  expandedSections.value[section] = !expandedSections.value[section]
}

// 根据餐食类型筛选记录
const getMealsByType = (mealType) => {
  return dietRecords.value.filter((record) => record.mealType === mealType)
}

// 从后端获取饮食记录（优化错误处理）
const fetchDietRecords = async (date) => {
  try {
    loading.value = true

    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (!userInfo || !userInfo.userId) {
      dietRecords.value = []
      return
    }

    // 调用后端API
    const apiUrl = API_CONFIG.diet.date.replace('{userId}', userInfo.userId) + date
    const response = await api.get(apiUrl)

    // 转换数据格式
    if (response && response.data) {
      dietRecords.value = response.data.map((record) => ({
        id: record.id,
        mealType: mealTypeToEnglish(record.mealTime),
        mealTypeName: record.mealTime,
        time: record.recordTime ? record.recordTime.split('T')[1].substring(0, 5) : '',
        foodName: record.foodName || '暂未定义食物名称',
        calories: record.calorie,
        protein: record.protein || 0,
        fat: record.fat || 0,
        carbohydrate: record.carbohydrate || 0,
        description: record.description || ''
      }))
    } else {
      dietRecords.value = []
    }
  } catch (error) {
    console.error('获取饮食记录失败:', error)

    // 优化错误处理
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      // 可以在这里跳转到登录页
    } else if (error.response?.status === 500) {
      ElMessage.error('服务器错误，请稍后重试')
    } else if (error.response?.status === 404) {
      // 没有找到记录，视为正常情况
      dietRecords.value = []
    } else {
      ElMessage.error(error.message || '获取饮食记录失败，请稍后重试')
    }

    dietRecords.value = []
  } finally {
    loading.value = false
  }
}

// 处理日期变化（使用防抖优化）
const handleDateChange = debounce((date) => {
  selectedDate.value = date
  fetchDietRecords(date)
}, 300)

// 打开添加记录弹窗
const openAddRecordDialog = () => {
  formMode.value = 'add'
  currentEditRecord.value = {}
  formDialogVisible.value = true
}

// 打开编辑记录弹窗
const openEditRecordDialog = (record) => {
  formMode.value = 'edit'
  currentEditRecord.value = { ...record }
  formDialogVisible.value = true
}

// 处理表单提交
const handleFormSubmit = async (formData) => {
  try {
    submitLoading.value = true

    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (!userInfo || !userInfo.userId) {
      ElMessage.error('未找到用户信息，请先登录')
      return
    }

    // 合并日期和时间为时间字符串格式
    const recordTime = `${selectedDate.value}T${formData.time}:00`

    if (formMode.value === 'add') {
      // 添加记录
      const requestData = {
        userId: userInfo.userId,
        mealTime: mealTypeToChinese(formData.mealType),
        foodName: formData.foodName,
        calorie: formData.calories,
        protein: formData.protein,
        fat: formData.fat,
        carbohydrate: formData.carbohydrate,
        description: formData.description,
        recordTime: recordTime
      }

      await api.post(API_CONFIG.diet.add, requestData)
      ElMessage.success('添加成功')
    } else {
      // 编辑记录
      const requestData = {
        id: currentEditRecord.value.id,
        userId: Number(userInfo.userId),
        mealTime: mealTypeToChinese(formData.mealType),
        foodName: formData.foodName,
        calorie: formData.calories,
        protein: formData.protein,
        fat: formData.fat,
        carbohydrate: formData.carbohydrate,
        description: formData.description,
        recordTime: recordTime
      }

      await api.put(API_CONFIG.diet.update, requestData)
      ElMessage.success('修改成功')
    }

    // 关闭弹窗并刷新记录
    formDialogVisible.value = false
    await fetchDietRecords(selectedDate.value)
  } catch (error) {
    console.error('提交记录失败:', error)

    // 优化错误处理
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response?.status === 400) {
      ElMessage.error(error.response?.data?.message || '请检查输入信息')
    } else {
      ElMessage.error(error.response?.data?.message || '提交失败，请稍后重试')
    }
  } finally {
    submitLoading.value = false
  }
}

// 打开删除确认弹窗
const openDeleteConfirm = (record) => {
  currentDeleteRecord.value = record
  deleteConfirmVisible.value = true
}

// 提交删除记录
const submitDeleteRecord = async () => {
  try {
    if (!currentDeleteRecord.value?.id) {
      ElMessage.error('未找到要删除的记录')
      return
    }

    deleteLoading.value = true

    // 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo'))
    if (!userInfo || !userInfo.userId) {
      ElMessage.error('未找到用户信息，请先登录')
      return
    }

    // 调用后端API删除记录
    await api.delete(API_CONFIG.diet.delete.replace('{id}', currentDeleteRecord.value.id))

    // 删除成功后，关闭弹窗并刷新记录
    deleteConfirmVisible.value = false
    ElMessage.success('删除成功')
    await fetchDietRecords(selectedDate.value)
  } catch (error) {
    console.error('删除记录失败:', error)

    // 优化错误处理
    if (error.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response?.status === 404) {
      ElMessage.error('记录不存在或已被删除')
    } else {
      ElMessage.error(error.response?.data?.message || '删除失败，请稍后重试')
    }
  } finally {
    deleteLoading.value = false
  }
}

// 页面加载时初始化数据
onMounted(() => {
  // 加载默认日期的饮食记录数据
  fetchDietRecords(selectedDate.value)
})
</script>

<style scoped>
.diet-record-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 0;
}

/* 顶部日历区域 */
.calendar-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 700;
  color: white;
}

.title-icon {
  font-size: 32px;
}

.calendar-control {
  display: flex;
  align-items: center;
  gap: 12px;
}

.calendar-picker {
  background-color: white;
  border-radius: 8px;
  padding: 4px;
}

.calendar-suffix-icon {
  color: #667eea;
}

.add-btn {
  border-radius: 24px !important;
  padding: 10px 24px !important;
  font-weight: 600 !important;
  background: white;
  color: #667eea;
  border: 2px solid white;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 255, 255, 0.3);
  background: white;
  color: #667eea;
}

/* 优化后的统计卡片 */
.stats-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  gap: 24px;
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  flex-wrap: wrap;
}

.stat-item {
  text-align: center;
  min-width: 100px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.stat-unit {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.stat-label {
  font-size: 14px;
  color: #999;
  font-weight: 500;
}

.stat-progress {
  margin-top: 8px;
  width: 100%;
}

.stat-detail {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 营养素统计样式 */
.nutrient-stat {
  min-width: 60px;
}

.nutrient-value {
  font-size: 20px;
  font-weight: 600;
  color: #667eea;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
}

.nutrient-unit {
  font-size: 12px;
  color: #999;
}

.nutrient-label {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.stat-divider {
  width: 2px;
  height: 48px;
  background-color: #e8e8e8;
}

/* 记录区域 */
.records-section {
  flex: 1;
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow-y: auto;
  min-height: 400px;
}

/* 骨架屏样式 */
.skeleton-container {
  padding: 20px;
}

.meal-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 餐食分类标题 */
.meal-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meal-section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 18px;
  font-weight: 600;
  color: white;
  user-select: none;
}

.meal-section-header.breakfast {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.meal-section-header.lunch {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.meal-section-header.dinner {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.meal-section-header.snack {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.meal-icon {
  font-size: 24px;
}

.meal-count {
  margin-left: auto;
}

/* 箭头图标样式 */
.arrow-icon {
  transition: transform 0.3s ease;
  font-size: 16px;
  margin-left: 8px;
}

.rotate-180 {
  transform: rotate(180deg);
}

/* 折叠动画样式 */
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  opacity: 0;
  margin: 0;
}

/* 记录卡片 */
.meal-records {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 8px;
}

/* 删除确认弹窗样式 */
.delete-confirm-content {
  text-align: center;
  padding: 20px 0;
}

.delete-icon {
  font-size: 48px;
  color: #f56c6c;
  margin-bottom: 16px;
}

.delete-record-info {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}

/* 空数据 */
.empty-records {
  margin: 60px 0;
  text-align: center;
}

.add-empty-btn {
  margin-top: 24px;
  border-radius: 8px;
  font-weight: 600;
}

.empty-tips {
  margin-top: 16px;
  font-size: 14px;
  color: #999;
  padding: 12px 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: inline-block;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .calendar-section {
    padding: 20px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-title {
    font-size: 24px;
  }

  .stats-card {
    gap: 16px;
    padding: 16px;
  }

  .stat-number {
    font-size: 24px;
  }

  .nutrient-value {
    font-size: 18px;
  }
}

@media (max-width: 480px) {
  .stats-card {
    flex-direction: column;
    gap: 20px;
  }

  .stat-divider {
    width: 100%;
    height: 2px;
  }
}
</style>
