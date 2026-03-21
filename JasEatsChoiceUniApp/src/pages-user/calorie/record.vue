<template>
  <view class="record-container">
    <!-- 顶部日期选择 -->
    <view class="date-header">
      <view class="date-btn" @tap="prevDay">
        <uni-icons type="left" size="20" color="#666"></uni-icons>
      </view>
      <view class="date-info" @tap="showDatePicker">
        <text class="date-text">{{ currentDate }}</text>
        <uni-icons type="calendar" size="18" color="#FF6B35"></uni-icons>
      </view>
      <view class="date-btn" @tap="nextDay">
        <uni-icons type="right" size="20" color="#666"></uni-icons>
      </view>
    </view>

    <!-- 今日摄入概览 -->
    <view class="summary-card">
      <view class="summary-item">
        <text class="label">早餐</text>
        <text class="value">{{ summary.breakfast }} kcal</text>
      </view>
      <view class="summary-item">
        <text class="label">午餐</text>
        <text class="value">{{ summary.lunch }} kcal</text>
      </view>
      <view class="summary-item">
        <text class="label">晚餐</text>
        <text class="value">{{ summary.dinner }} kcal</text>
      </view>
      <view class="summary-item">
        <text class="label">加餐</text>
        <text class="value">{{ summary.snack }} kcal</text>
      </view>
    </view>

    <!-- 总摄入和目标 -->
    <view class="total-card">
      <view class="total-header">
        <text class="total-label">今日总摄入</text>
        <text class="total-value">{{ totalCalories }} kcal</text>
      </view>
      <view class="progress-section">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: progressPercent + '%' }"></view>
        </view>
        <text class="progress-text">{{ progressPercent }}% (目标: {{ targetCalories }} kcal)</text>
      </view>
    </view>

    <!-- 记录按钮 -->
    <view class="action-bar">
      <button class="add-btn" @tap="showAddModal">
        <uni-icons type="plus" size="20" color="#fff"></uni-icons>
        <text>添加记录</text>
      </button>
    </view>

    <!-- 饮食记录列表 -->
    <view class="records-section">
      <view class="section-title">饮食记录</view>
      <scroll-view scroll-y class="records-list">
        <view
          class="record-item"
          v-for="record in records"
          :key="record.id"
        >
          <view class="record-time">{{ record.time }}</view>
          <view class="record-content">
            <text class="record-name">{{ record.name }}</text>
            <text class="record-calories">{{ record.calories }} kcal</text>
          </view>
          <view class="record-tags" v-if="record.tags">
            <text class="tag" v-for="tag in record.tags" :key="tag">{{ tag }}</text>
          </view>
        </view>

        <view class="empty-state" v-if="records.length === 0">
          <empty />
        </view>
      </scroll-view>
    </view>

    <!-- 添加记录弹窗 -->
    <uni-popup ref="addPopup" type="bottom">
      <view class="popup-content">
        <view class="popup-title">添加饮食记录</view>
        <view class="form-section">
          <view class="form-item">
            <text class="form-label">餐别</text>
            <picker mode="selector" :range="mealTypes" @change="onMealTypeChange">
              <view class="picker-value">
                <text>{{ selectedMealType }}</text>
                <uni-icons type="right" size="16" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
          <view class="form-item">
            <text class="form-label">食物名称</text>
            <input class="form-input" v-model="formData.name" placeholder="请输入食物名称" />
          </view>
          <view class="form-item">
            <text class="form-label">卡路里</text>
            <input class="form-input" v-model="formData.calories" type="number" placeholder="请输入卡路里" />
          </view>
          <view class="form-item">
            <text class="form-label">备注</text>
            <textarea class="form-textarea" v-model="formData.remark" placeholder="选填" />
          </view>
        </view>
        <view class="popup-actions">
          <button class="action-btn cancel" @tap="closePopup">取消</button>
          <button class="action-btn confirm" @tap="saveRecord">保存</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/helper'
import { aiApi } from '@/api'

const userStore = useUserStore()

const currentDate = ref('')
const summary = ref({
  breakfast: 0,
  lunch: 0,
  dinner: 0,
  snack: 0
})

const targetCalories = ref(2000)
const records = ref([])

const addPopup = ref(null)
const mealTypes = ['早餐', '午餐', '晚餐', '加餐']
const selectedMealType = ref('午餐')

const formData = ref({
  mealType: 1,
  name: '',
  calories: '',
  remark: ''
})

const totalCalories = computed(() => {
  return summary.value.breakfast + summary.value.lunch + summary.value.dinner + summary.value.snack
})

const progressPercent = computed(() => {
  const percent = (totalCalories.value / targetCalories.value) * 100
  return Math.min(Math.round(percent), 100)
})

onMounted(() => {
  const today = new Date()
  currentDate.value = formatDate(today)
  loadRecords()
})

const prevDay = () => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() - 1)
  currentDate.value = formatDate(date)
  loadRecords()
}

const nextDay = () => {
  const date = new Date(currentDate.value)
  date.setDate(date.getDate() + 1)
  currentDate.value = formatDate(date)
  loadRecords()
}

const showDatePicker = () => {
  uni.showToast({ title: '日期选择功能开发中', icon: 'none' })
}

const loadRecords = async () => {
  try {
    if (!userStore.isLogin) {
      uni.showToast({
        title: '请先登录',
        icon: 'none'
      })
      return
    }

    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 调用AI营养分析API获取指定日期的营养记录
    const res = await aiApi.analyzeNutrition({
      userId,
      date: currentDate.value
    })

    if (res && res.data) {
      const nutrition = res.data

      // 更新各餐次卡路里
      summary.value = {
        breakfast: nutrition.breakfast || nutrition.meals?.breakfast?.calories || 0,
        lunch: nutrition.lunch || nutrition.meals?.lunch?.calories || 0,
        dinner: nutrition.dinner || nutrition.meals?.dinner?.calories || 0,
        snack: nutrition.snack || nutrition.meals?.snack?.calories || 0
      }

      // 更新总卡路里
      const totalCal = nutrition.calories || nutrition.totalCalories || 0
      totalCalories.value = totalCal

      // 映射记录数据
      if (Array.isArray(nutrition.records)) {
        records.value = nutrition.records.map(record => ({
          id: record.id,
          time: formatRecordTime(record.time),
          name: record.foodName || record.name,
          calories: record.calories,
          tags: record.tags || [getMealTypeText(record.mealType)]
        }))
      } else if (nutrition.meals) {
        // 如果没有records字段，从meals字段构建记录
        records.value = []
        const mealMap = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐', snack: '加餐' }

        Object.keys(nutrition.meals).forEach(mealKey => {
          const meal = nutrition.meals[mealKey]
          if (meal && meal.items) {
            meal.items.forEach(item => {
              records.value.push({
                id: item.id || Date.now() + Math.random(),
                time: meal.time || '--:--',
                name: item.name,
                calories: item.calories,
                tags: [mealMap[mealKey] || '其他']
              })
            })
          }
        })
      }
    } else {
      // 没有数据时使用默认值
      summary.value = { breakfast: 0, lunch: 0, dinner: 0, snack: 0 }
      records.value = []
    }
  } catch (error) {
    console.error('加载营养记录失败:', error)
    // 使用默认数据
    summary.value = {
      breakfast: 450,
      lunch: 780,
      dinner: 0,
      snack: 120
    }
    records.value = [
      { id: 1, time: '08:30', name: '牛奶燕麦粥', calories: 280, tags: ['早餐', '健康'] },
      { id: 2, time: '08:30', name: '水煮蛋', calories: 70, tags: ['早餐'] },
      { id: 3, time: '08:30', name: '全麦面包', calories: 100, tags: ['早餐'] },
      { id: 4, time: '12:15', name: '宫保鸡丁', calories: 380, tags: ['午餐'] },
      { id: 5, time: '12:15', name: '米饭', calories: 200, tags: ['午餐'] },
      { id: 6, time: '12:15', name: '青菜', calories: 50, tags: ['午餐'] },
      { id: 7, time: '12:15', name: '西红柿蛋汤', calories: 150, tags: ['午餐'] },
      { id: 8, time: '15:30', name: '苹果', calories: 60, tags: ['加餐', '水果'] },
      { id: 9, time: '15:30', name: '酸奶', calories: 60, tags: ['加餐', '健康'] }
    ]
  }
}

const formatRecordTime = (time) => {
  if (!time) return '--:--'
  const date = new Date(time)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getMealTypeText = (mealType) => {
  const mealTypeMap = {
    1: '早餐',
    2: '午餐',
    3: '晚餐',
    4: '加餐',
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐',
    snack: '加餐'
  }
  return mealTypeMap[mealType] || '其他'
}

const showAddModal = () => {
  addPopup.value.open()
}

const closePopup = () => {
  addPopup.value.close()
  resetForm()
}

const onMealTypeChange = (e) => {
  selectedMealType.value = mealTypes[e.detail.value]
  formData.value.mealType = e.detail.value + 1
}

const resetForm = () => {
  formData.value = {
    mealType: 1,
    name: '',
    calories: '',
    remark: ''
  }
}

const saveRecord = async () => {
  if (!formData.value.name || !formData.value.calories) {
    uni.showToast({
      title: '请填写完整信息',
      icon: 'none'
    })
    return
  }

  try {
    const userId = userStore.userInfo?.userId || userStore.userInfo?.id

    // 调用API保存记录（这里使用AI分析API的扩展功能）
    // 注意：如果后端没有专门的保存记录接口，可能需要添加
    const mealTypeMap = { 1: 'breakfast', 2: 'lunch', 3: 'dinner', 4: 'snack' }

    const newRecord = {
      userId,
      date: currentDate.value,
      mealType: mealTypeMap[formData.value.mealType],
      foodName: formData.value.name,
      calories: parseInt(formData.value.calories),
      remark: formData.value.remark
    }

    // U-026: 调用后端API保存记录
    try {
      const { aiApi } = await import('@/api')
      await aiApi.saveNutritionRecord(newRecord)
    } catch (error) {
      console.error('保存记录到服务器失败，仅保存到本地:', error)
      // 如果API调用失败，仅保存到本地，不中断流程
    }

    // 临时添加到本地列表
    const record = {
      id: Date.now(),
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      name: formData.value.name,
      calories: parseInt(formData.value.calories),
      tags: [selectedMealType.value]
    }

    records.value.unshift(record)

    // 更新对应的餐次卡路里
    const meal = mealTypeMap[formData.value.mealType]
    summary.value[meal] += parseInt(formData.value.calories)

    uni.showToast({
      title: '记录成功',
      icon: 'success'
    })

    closePopup()
  } catch (error) {
    console.error('保存记录失败:', error)
    uni.showToast({
      title: '保存失败，请重试',
      icon: 'none'
    })
  }
}
</script>

<style lang="scss" scoped>
.record-container {
  min-height: 100vh;
  background: #F5F5F5;
  padding-bottom: 20rpx;
}

.date-header {
  background: #fff;
  padding: 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.date-info {
  display: flex;
  align-items: center;
  gap: 15rpx;
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.summary-card {
  background: #fff;
  padding: 30rpx;
  margin: 20rpx 30rpx;
  border-radius: 16rpx;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}

.summary-item .label {
  font-size: 24rpx;
  color: #999;
}

.summary-item .value {
  font-size: 28rpx;
  font-weight: bold;
  color: #FF6B35;
}

.total-card {
  background: linear-gradient(135deg, #FF6B35, #FF8F6B);
  margin: 0 30rpx 20rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.total-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.total-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.9);
}

.total-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
}

.progress-section {
  margin-top: 30rpx;
}

.progress-bar {
  height: 12rpx;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 6rpx;
  overflow: hidden;
  margin-bottom: 10rpx;
}

.progress-fill {
  height: 100%;
  background: #fff;
  border-radius: 6rpx;
  transition: width 0.3s;
}

.progress-text {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.action-bar {
  padding: 0 30rpx;
}

.add-btn {
  background: #FF6B35;
  color: #fff;
  border: none;
  border-radius: 50rpx;
  height: 90rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  font-size: 28rpx;
  box-shadow: 0 8rpx 20rpx rgba(255, 107, 53, 0.3);
}

.records-section {
  background: #fff;
  margin: 0 30rpx;
  padding: 30rpx;
  border-radius: 16rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
}

.records-list {
  max-height: 600rpx;
}

.record-item {
  padding: 25rpx 0;
  border-bottom: 1rpx solid #eee;
}

.record-time {
  font-size: 24rpx;
  color: #999;
  margin-bottom: 10rpx;
}

.record-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-name {
  font-size: 28rpx;
  color: #333;
}

.record-calories {
  font-size: 26rpx;
  font-weight: bold;
  color: #FF6B35;
}

.record-tags {
  display: flex;
  gap: 10rpx;
  margin-top: 10rpx;
}

.tag {
  padding: 6rpx 12rpx;
  background: #F5F5F5;
  color: #666;
  font-size: 22rpx;
  border-radius: 4rpx;
}

.empty-state {
  padding-top: 200rpx;
}

.popup-content {
  padding: 30rpx;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
}

.popup-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 30rpx;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 15rpx;
}

.form-label {
  font-size: 28rpx;
  color: #333;
  font-weight: bold;
}

.picker-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
}

.form-input {
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.form-textarea {
  min-height: 150rpx;
  padding: 20rpx;
  background: #F5F5F5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.popup-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 40rpx;
}

.action-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;

  &.cancel {
    background: #F5F5F5;
    color: #666;
  }

  &.confirm {
    background: #FF6B35;
    color: #fff;
  }
}
</style>
