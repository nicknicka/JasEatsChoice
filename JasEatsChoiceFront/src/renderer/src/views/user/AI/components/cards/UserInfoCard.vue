<template>
  <div class="user-info-card">
    <div class="card-header">
      <div class="header-title">
        <span class="icon">👤</span>
        <span class="title">我的信息</span>
      </div>
      <div class="header-summary">{{ data.summary }}</div>
    </div>

    <div class="card-content">
      <!-- 基本信息 -->
      <div class="section">
        <div class="section-title">📋 基本信息</div>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">昵称</span>
            <span class="value">{{ data.basicInfo.nickname }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号</span>
            <span class="value">{{ data.basicInfo.phone }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ data.basicInfo.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">地区</span>
            <span class="value">{{ data.basicInfo.location || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">性别</span>
            <span class="value">{{ getGenderText(data.basicInfo.gender) }}</span>
          </div>
          <div class="info-item">
            <span class="label">注册时间</span>
            <span class="value">{{ data.basicInfo.registerTime }}</span>
          </div>
        </div>
      </div>

      <!-- 身体数据 -->
      <div class="section">
        <div class="section-title">💪 身体数据</div>
        <div class="body-data">
          <div class="data-row">
            <div class="data-item">
              <span class="data-label">身高</span>
              <span class="data-value">{{ data.bodyData.height }} cm</span>
            </div>
            <div class="data-item">
              <span class="data-label">体重</span>
              <span class="data-value">{{ data.bodyData.weight }} kg</span>
            </div>
            <div class="data-item" v-if="data.bodyData.bmi">
              <span class="data-label">BMI</span>
              <el-tag :type="getBMIColor(data.bodyData.bmiStatus)" size="small">
                {{ data.bodyData.bmi }}
              </el-tag>
            </div>
          </div>
          <div v-if="data.bodyData.bmiText" class="bmi-hint">
            <el-alert
              :title="`您的BMI状态：${data.bodyData.bmiText}`"
              :type="getBMIColor(data.bodyData.bmiStatus)"
              :closable="false"
              show-icon
            />
          </div>
        </div>
      </div>

      <!-- 饮食偏好 -->
      <div class="section" v-if="hasPreferences">
        <div class="section-title">🍽️ 饮食偏好</div>
        <div class="preferences">
          <div class="preference-item" v-if="data.preferences.dietGoal">
            <span class="label">饮食目标：</span>
            <span class="value">{{ data.preferences.dietGoal }}</span>
          </div>
          <div class="preference-item" v-if="data.preferences.allergies && data.preferences.allergies.length > 0">
            <span class="label">过敏食材：</span>
            <el-tag
              v-for="(allergy, index) in data.preferences.allergies"
              :key="index"
              type="danger"
              size="small"
              style="margin-right: 4px;"
            >
              {{ allergy }}
            </el-tag>
          </div>
          <div class="preference-item" v-if="data.preferences.tags && data.preferences.tags.length > 0">
            <span class="label">偏好标签：</span>
            <el-tag
              v-for="(tag, index) in data.preferences.tags"
              :key="index"
              type="success"
              size="small"
              style="margin-right: 4px;"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="actions-section" v-if="data.actions && data.actions.length > 0">
        <el-button
          v-for="action in data.actions"
          :key="action.type"
          :type="action.type === 'edit_profile' ? 'primary' : 'success'"
          :icon="getActionIcon(action.icon)"
          @click="handleAction(action.type)"
        >
          {{ action.text }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { BMIStatusMap } from '../../constants/messageTypes'
import { computed } from 'vue'

const props = defineProps({
  data: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['action'])

// 是否有饮食偏好
const hasPreferences = computed(() => {
  const pref = props.data.preferences
  return pref &&
    (pref.dietGoal ||
     (pref.allergies && pref.allergies.length > 0) ||
     (pref.tags && pref.tags.length > 0))
})

// 获取性别文本
const getGenderText = (gender) => {
  const genderMap = {
    'male': '男',
    'female': '女',
    'other': '其他'
  }
  return genderMap[gender] || '未设置'
}

// 获取BMI颜色
const getBMIColor = (status) => {
  return BMIStatusMap[status]?.color || 'info'
}

// 获取操作图标
const getActionIcon = (iconName) => {
  const iconMap = {
    'Edit': 'Edit',
    'TrendCharts': 'TrendCharts'
  }
  return iconMap[iconName] || 'Operation'
}

// 处理操作
const handleAction = (actionType) => {
  emit('action', {
    type: actionType
  })
}
</script>

<style scoped>
.user-info-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  padding: 16px 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.icon {
  font-size: 24px;
}

.header-summary {
  font-size: 14px;
  opacity: 0.9;
}

.card-content {
  background: white;
  padding: 16px;
}

.section {
  margin-bottom: 20px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 6px;
}

.label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.body-data {
  background: #f0f7ff;
  padding: 12px;
  border-radius: 8px;
}

.data-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.data-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px;
  background: white;
  border-radius: 6px;
}

.data-label {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.data-value {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.bmi-hint {
  margin-top: 12px;
}

.preferences {
  background: #fff9f0;
  padding: 12px;
  border-radius: 8px;
}

.preference-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.preference-item:last-child {
  margin-bottom: 0;
}

.preference-item .label {
  min-width: 80px;
  font-size: 14px;
  color: #666;
}

.preference-item .value {
  font-size: 14px;
  color: #333;
}

.actions-section {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
