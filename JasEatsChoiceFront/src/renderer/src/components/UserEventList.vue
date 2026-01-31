<template>
  <div class="user-event-list">
    <!-- 头部 -->
    <div class="list-header">
      <h3 class="list-title">我的事件</h3>
      <el-button type="primary" size="small" @click="showCreateDialog = true">
        创建事件
      </el-button>
    </div>

    <!-- 事件列表 -->
    <div v-loading="loading" class="event-content">
      <!-- 即将到来的事件 -->
      <div v-if="upcomingEvents.length > 0" class="event-section">
        <h4 class="section-title">即将到来</h4>
        <div class="event-cards">
          <div
            v-for="event in upcomingEvents"
            :key="event.id"
            class="event-card upcoming"
          >
            <div class="event-icon">{{ event.eventTypeIcon }}</div>
            <div class="event-info">
              <h5 class="event-name">{{ event.eventName }}</h5>
              <p class="event-date">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(event.eventDate) }}
                <el-tag v-if="event.daysUntilEvent !== null" size="small" type="danger">
                  {{ event.daysUntilEvent }}天后
                </el-tag>
              </p>
              <p v-if="event.description" class="event-desc">{{ event.description }}</p>
              <div v-if="event.guestCount || event.budgetPerPerson" class="event-meta">
                <span v-if="event.guestCount">👥 {{ event.guestCount }}人</span>
                <span v-if="event.budgetPerPerson">💰 ¥{{ event.budgetPerPerson }}/人</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 所有事件 -->
      <div class="event-section">
        <h4 class="section-title">全部事件</h4>
        <div class="event-cards">
          <div
            v-for="event in allEvents"
            :key="event.id"
            class="event-card"
            :class="{ upcoming: isUpcoming(event) }"
          >
            <div class="event-icon">{{ event.eventTypeIcon }}</div>
            <div class="event-info">
              <h5 class="event-name">{{ event.eventName }}</h5>
              <p class="event-date">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(event.eventDate) }}
              </p>
              <p v-if="event.description" class="event-desc">{{ event.description }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && allEvents.length === 0" description="暂无事件，点击右上角创建" />
    </div>

    <!-- 创建事件对话框 -->
    <user-custom-event-dialog
      v-model:visible="showCreateDialog"
      :available-dishes="availableDishes"
      @success="loadEvents"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import UserCustomEventDialog from './UserCustomEventDialog.vue'
import api from '@/api'

const loading = ref(false)
const showCreateDialog = ref(false)
const allEvents = ref([])
const availableDishes = ref([])

// 即将到来的事件（30天内）
const upcomingEvents = computed(() => {
  return allEvents.value.filter(event =>
    event.daysUntilEvent !== null && event.daysUntilEvent <= 30
  ).sort((a, b) => a.daysUntilEvent - b.daysUntilEvent)
})

// 判断是否即将到来
const isUpcoming = (event) => {
  return event.daysUntilEvent !== null && event.daysUntilEvent <= 30
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const [month, day] = dateStr.split('-')
  return `${month}月${day}日`
}

// 加载用户事件
const loadEvents = async () => {
  loading.value = true
  try {
    const response = await api.get('/v1/festival/custom-events')
    if (response.data.code === 200) {
      allEvents.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载事件失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 加载可用菜品
const loadAvailableDishes = async () => {
  try {
    const response = await api.get('/v1/dish/list')
    if (response.data.code === 200) {
      availableDishes.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载菜品失败:', error)
  }
}

onMounted(() => {
  loadEvents()
  loadAvailableDishes()
})
</script>

<style scoped>
.user-event-list {
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-title {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.event-content {
  min-height: 200px;
}

.event-section {
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #606266;
}

.event-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-card {
  display: flex;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
}

.event-card.upcoming {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border-color: #ffcdd2;
}

.event-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.event-icon {
  font-size: 36px;
  margin-right: 16px;
  flex-shrink: 0;
}

.event-info {
  flex: 1;
}

.event-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.event-date {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
  gap: 4px;
}

.event-desc {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.event-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #606266;
}
</style>
