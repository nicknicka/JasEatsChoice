<template>
  <div class="diet-record-container">
    <AppPageHeader title="饮食记录">
      <template #actions>
        <div class="page-actions">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            format="YYYY年MM月DD日"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
            class="calendar-picker"
            popper-class="custom-date-picker"
            :clearable="false"
          >
            <template #suffix-icon>
              <el-icon class="calendar-suffix-icon"><CalendarIcon /></el-icon>
            </template>
          </el-date-picker>
          <el-button type="primary" class="add-btn" @click="openAddRecordDialog">
            <el-icon><Plus /></el-icon>
            添加记录
          </el-button>
        </div>
      </template>
    </AppPageHeader>

    <!-- 有记录时才显示统计摘要，空状态不再重复展示无效数据（§4.5、§4.8） -->
    <div v-if="dietRecords.length > 0" class="stats-card">
      <div class="stats-period">所选日期：{{ selectedDate }}</div>

      <div class="stat-item stat-item--primary scale-in">
        <div class="stat-number number-scroll">{{ totalCalories }}<span class="stat-unit">kcal</span></div>
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

      <div class="stat-item scale-in">
        <div class="stat-number number-scroll">{{ dietRecords.length }}<span class="stat-unit">条</span></div>
        <div class="stat-label">记录条数</div>
      </div>

      <div class="stat-divider" v-if="showNutrientStats"></div>

      <!-- 营养素统计 -->
      <template v-if="showNutrientStats">
        <div class="stat-item nutrient-stat scale-in">
          <div class="nutrient-value number-scroll">{{ totalProtein }}<span class="nutrient-unit">g</span></div>
          <div class="nutrient-label">蛋白质</div>
        </div>

        <div class="stat-item nutrient-stat scale-in">
          <div class="nutrient-value number-scroll">{{ totalFat }}<span class="nutrient-unit">g</span></div>
          <div class="nutrient-label">脂肪</div>
        </div>

        <div class="stat-item nutrient-stat scale-in">
          <div class="nutrient-value number-scroll">
            {{ totalCarbohydrate }}<span class="nutrient-unit">g</span>
          </div>
          <div class="nutrient-label">碳水</div>
        </div>
      </template>
    </div>

    <!-- 饮食记录显示区域 -->
    <div class="records-section" :class="{ 'is-empty': !loading && dietRecords.length === 0 }">
      <!-- 骨架屏加载 -->
      <div v-if="loading" class="skeleton-container">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated class="skeleton-block" />
        <el-skeleton :rows="3" animated class="skeleton-block" />
      </div>

      <!-- 饮食记录内容 -->
      <div v-else class="meal-sections">
        <!-- 早餐 -->
        <div v-if="getMealsByType('breakfast').length > 0" class="meal-section stagger-item">
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
        <div v-if="getMealsByType('lunch').length > 0" class="meal-section stagger-item">
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
        <div v-if="getMealsByType('dinner').length > 0" class="meal-section stagger-item">
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
        <div v-if="getMealsByType('snack').length > 0" class="meal-section stagger-item">
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

      <!-- 空状态复用 L1，紧凑尺寸 + CTA 上方结果说明（§4.5） -->
      <div v-if="!loading && dietRecords.length === 0" class="empty-records">
        <AppEmptyState
          compact
          :icon="Document"
          description="暂无饮食记录；记录后会在这里汇总每日卡路里和营养摄入"
          action-text="开始记录第一餐"
          @action="openAddRecordDialog"
        />
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
  Warning,
  Document
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api.js'
import { API_CONFIG } from '../../config/index.js'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppEmptyState from '../../components/common/AppEmptyState.vue'
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

// 进度条颜色：JS 颜色从 tokens.css 读取，不复制色值（§7.1）
const progressColor = computed(() => {
  const styles = getComputedStyle(document.documentElement)
  const progress = calorieProgress.value
  if (progress < 50) return styles.getPropertyValue('--success').trim()
  if (progress < 80) return styles.getPropertyValue('--warning').trim()
  return styles.getPropertyValue('--danger').trim()
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

<style scoped lang="less">
/* 页面颜色、间距、圆角、字阶与动效均取自 tokens.css。 */
.diet-record-container {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: 100%;
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

.page-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.calendar-picker {
  width: 190px;
}

.calendar-suffix-icon {
  color: var(--ink-400);
}

.add-btn {
  font-weight: var(--weight-semibold);
  white-space: nowrap;
}

/* 统计摘要：数值、单位、周期统一表达（§4.8） */
.stats-card {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-around;
  gap: var(--space-3) var(--space-6);
  padding: var(--space-5) var(--space-6);
  margin-bottom: var(--space-6);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.stats-period {
  flex: 0 0 100%;
  margin-bottom: var(--space-1);
  color: var(--ink-400);
  font-size: var(--text-caption);
  line-height: 18px;
  text-align: right;
}

.stat-item {
  min-width: 100px;
  text-align: center;
}

.stat-item--primary {
  min-width: 160px;
}

.nutrient-stat {
  min-width: 60px;
}

.stat-number,
.nutrient-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: var(--space-1);
  color: var(--ink-900);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  font-weight: var(--weight-semibold);
}

.stat-number {
  margin-bottom: var(--space-1);
  font-size: var(--text-h1);
  line-height: 32px;
}

.nutrient-value {
  font-size: var(--text-h2);
  line-height: 28px;
}

.stat-unit,
.nutrient-unit {
  color: var(--ink-500);
  font-family: var(--font-sans);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  line-height: 18px;
}

.stat-label,
.nutrient-label {
  color: var(--ink-500);
  font-size: var(--text-caption);
  font-weight: var(--weight-medium);
  line-height: 18px;
}

.stat-progress {
  width: 100%;
  margin-top: var(--space-3);
}

.stat-detail {
  margin-top: var(--space-1);
  color: var(--ink-400);
  font-size: var(--text-caption);
  line-height: 18px;
}

.stat-divider {
  width: 1px;
  height: 48px;
  background-color: var(--line);
}

/* 记录区域：非交互卡片使用 L1 微质感，不做悬停位移（§3.4） */
.records-section {
  flex: 1;
  min-height: 400px;
  padding: var(--space-6);
  overflow-y: auto;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

/* 列表空状态高度上限 240px（§4.5） */
.records-section.is-empty {
  flex: 0 0 auto;
  min-height: 240px;
  padding: 0;
  overflow: hidden;
}

.skeleton-container {
  padding: var(--space-2);
}

.skeleton-block {
  margin-top: var(--space-5);
}

.meal-sections {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* 餐次标题改为中性表面 + 语义色线性图标（§3.4、§4.9） */
.meal-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.meal-section-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-md);
  color: var(--ink-900);
  cursor: pointer;
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  user-select: none;
  transition:
    background var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out);
}

.meal-section-header:hover {
  background: var(--canvas);
  border-color: var(--line-strong);
}

.meal-section-header:focus-visible {
  outline: 2px solid var(--brand-500);
  outline-offset: 2px;
}

.meal-icon {
  color: var(--ink-400);
  font-size: 20px;
}

.meal-section-header.breakfast .meal-icon {
  color: var(--brand-500);
}

.meal-section-header.lunch .meal-icon {
  color: var(--warning);
}

.meal-section-header.dinner .meal-icon {
  color: var(--info);
}

.meal-section-header.snack .meal-icon {
  color: var(--success);
}

.meal-count {
  margin-left: auto;
}

.arrow-icon {
  margin-left: var(--space-2);
  font-size: 18px;
  transition: transform var(--duration-base) var(--ease-out);
}

.rotate-180 {
  transform: rotate(180deg);
}

.collapse-enter-active,
.collapse-leave-active {
  overflow: hidden;
  transition: all var(--duration-base) var(--ease-out);
}

.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  margin: 0;
  opacity: 0;
}

.meal-records {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: 0 var(--space-2);
}

.empty-records {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
}

.empty-records :deep(.app-empty-state) {
  width: 100%;
}

/* 删除确认弹窗 */
.delete-confirm-content {
  padding: var(--space-6) 0;
  text-align: center;
}

.delete-icon {
  margin-bottom: var(--space-4);
  color: var(--danger);
  font-size: 48px;
}

.delete-record-info {
  margin-top: var(--space-2);
  color: var(--ink-500);
  font-size: var(--text-body);
}

@media (max-width: 768px) {
  .diet-record-container {
    padding: 0 var(--page-gutter) var(--space-8);
  }

  :deep(.app-page-header__main) {
    flex-wrap: wrap;
  }

  .page-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .calendar-picker {
    flex: 1;
    min-width: 180px;
  }

  .stats-card {
    gap: var(--space-4);
    padding: var(--space-4);
  }

  .stat-divider {
    height: 1px;
    width: 100%;
  }
}

@media (max-width: 480px) {
  .stats-card {
    flex-direction: column;
  }

  .stat-item,
  .stat-item--primary,
  .nutrient-stat {
    width: 100%;
  }
}
</style>
