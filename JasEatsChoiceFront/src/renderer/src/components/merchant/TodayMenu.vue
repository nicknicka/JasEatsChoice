<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  CircleCheck,
  Dish,
  Clock,
  Sunny,
  Moon,
  Coffee,
  ArrowRight,
  CircleClose,
  CircleCheckFilled
} from '@element-plus/icons-vue'
import api from '../../utils/api.js'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

let merchantId = authStore.merchantId

// 如果 Pinia 中没有商家ID，尝试从 localStorage 读取
if (!merchantId) {
  const localStorageMerchantId = localStorage.getItem('auth_merchantId')
  if (localStorageMerchantId) {
    merchantId = localStorageMerchantId
    authStore.setMerchantId(localStorageMerchantId) // 更新到 Pinia 中
  }
}

// 今日菜单数据
const todayMenus = ref([])

// 当前选中的菜单
const selectedMenu = ref(null)

// 筛选后的菜单
const filteredMenus = ref([...todayMenus.value])

// 菜单类型筛选
const activeMenuTypeFilter = ref('全部')

// 菜单状态筛选
const activeMenuFilter = ref('全部')

// 菜单分类列表
const menuCategories = ref(['全部', '早餐', '午餐', '晚餐', '加餐'])

// 菜单状态列表
const menuStatuses = ref([
  { value: '全部', label: '全部' },
  { value: 'online', label: '上架中' },
  { value: 'offline', label: '下架中' },
  { value: 'draft', label: '草稿' }
])

// 菜单状态映射
const menuStatusMap = {
  online: { text: '上架中', icon: '🟢', type: 'success' },
  draft: { text: '草稿', icon: '🟡', type: 'warning' },
  offline: { text: '下架中', icon: '🔴', type: 'danger' }
}

// 统计数据
const menuStatistics = computed(() => {
  const total = todayMenus.value.length
  const online = todayMenus.value.filter((m) => m.status === 'online').length
  const totalDishes = todayMenus.value.reduce((sum, menu) => sum + (menu.dishes || 0), 0)
  const latestUpdate =
    todayMenus.value.length > 0
      ? todayMenus.value.reduce((latest, menu) => {
          return !latest || (menu.updateTime && menu.updateTime > latest) ? menu.updateTime : latest
        }, '')
      : '暂无'

  return {
    total,
    online,
    totalDishes,
    latestUpdate
  }
})

// 从后端获取今日菜单数据
const fetchTodayMenus = () => {
  api
    .get(`/v1/menus/merchants/${merchantId}/menu`)
    .then((response) => {
      if (response.code === '200' && response.data) {
        todayMenus.value = response.data.map((menu) => ({
          ...menu,
          // 后端直接返回 online/offline，不需要转换
          status: menu.status || 'offline',
          dishes: Array.isArray(menu.dishes) ? menu.dishes.length : menu.dishes || 0,
          updateTime: menu.updateTime ? menu.updateTime.replace('T', ' ') : '',
          autoOnline: menu.autoOnline ? menu.autoOnline.replace('T', ' ') : '',
          autoOffline: menu.autoOffline ? menu.autoOffline.replace('T', ' ') : ''
        }))
        filteredMenus.value = [...todayMenus.value]
      }
    })
    .catch((error) => {
      console.error('获取今日菜单数据失败:', error)
    })
}

// 切换菜单
const switchMenu = (menu) => {
  selectedMenu.value = menu
}

// 筛选菜单：先按类型，再按状态
const filterMenus = (filterType, filterCategory = 'status') => {
  if (filterCategory === 'status') {
    activeMenuFilter.value = filterType
  } else if (filterCategory === 'type') {
    activeMenuTypeFilter.value = filterType
  }

  // 确保总是基于todayMenus最新的数据进行筛选
  let newFilteredMenus = [...todayMenus.value]

  // 状态筛选
  if (activeMenuFilter.value !== '全部') {
    newFilteredMenus = newFilteredMenus.filter((menu) => menu.status === activeMenuFilter.value)
  }

  // 类型筛选
  if (activeMenuTypeFilter.value !== '全部') {
    const targetType = activeMenuTypeFilter.value
    newFilteredMenus = newFilteredMenus.filter((menu) => {
      const menuName = String(menu.name || '')
      return menuName.includes(targetType) || menuName.replace('菜单', '').includes(targetType)
    })
  }

  filteredMenus.value = newFilteredMenus

  // 如果当前选中的菜单不在筛选结果中，重置选择
  if (
    selectedMenu.value &&
    !filteredMenus.value.some((menu) => menu.id === selectedMenu.value.id)
  ) {
    selectedMenu.value = null
    currentMenuDishes.value = []
  }
}

// 页面跳转
const navigateToMenu = () => {
  router.push('/merchant/home/menu')
}

// 格式化时间显示
const formatTime = (timeStr) => {
  if (!timeStr) return '暂无'
  // 只返回时间部分 HH:mm
  const parts = timeStr.split(' ')
  if (parts.length >= 2) {
    return parts[1].substring(0, 5)
  }
  return timeStr.substring(0, 5)
}

onMounted(() => {
  fetchTodayMenus()
})
</script>

<template>
  <div class="today-menu-container">
    <!-- 统计概览卡片 -->
    <div class="statistics-overview">
      <div class="stat-card stat-total">
        <div class="stat-icon">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ menuStatistics.total }}</div>
          <div class="stat-label">总菜单</div>
        </div>
      </div>
      <div class="stat-card stat-online">
        <div class="stat-icon">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ menuStatistics.online }}</div>
          <div class="stat-label">上架中</div>
        </div>
      </div>
      <div class="stat-card stat-dishes">
        <div class="stat-icon">
          <el-icon :size="28"><Dish /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ menuStatistics.totalDishes }}</div>
          <div class="stat-label">总菜品</div>
        </div>
      </div>
      <div class="stat-card stat-time">
        <div class="stat-icon">
          <el-icon :size="28"><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value-small">{{ menuStatistics.latestUpdate }}</div>
          <div class="stat-label">最近更新</div>
        </div>
      </div>
    </div>

    <!-- 菜单卡片 -->
    <div class="quick-actions-card today-menu-card">
      <div class="menu-header">
        <h3 class="card-title">
          <el-icon :size="22"><Document /></el-icon> 今日菜单
        </h3>
        <div class="filter-section">
          <span class="filter-label">分类：</span>
          <el-tag
            v-for="category in menuCategories"
            :key="category"
            effect="plain"
            class="menu-filter-tag"
            :class="{ active: activeMenuTypeFilter === category }"
            @click="filterMenus(category, 'type')"
            >{{ category }}</el-tag
          >
        </div>
      </div>

      <div class="menu-header">
        <div class="filter-section">
          <span class="filter-label">状态：</span>
          <el-tag
            v-for="status in menuStatuses"
            :key="status.value"
            effect="plain"
            class="menu-status-tag"
            :class="[
              { active: activeMenuFilter === status.value },
              status.value === 'online'
                ? 'tag-online'
                : status.value === 'offline'
                  ? 'tag-offline'
                  : status.value === 'draft'
                    ? 'tag-draft'
                    : ''
            ]"
            @click="filterMenus(status.value, 'status')"
            >{{ status.label }}</el-tag
          >
        </div>
      </div>

      <!-- 菜单卡片网格 -->
      <transition name="fade" mode="out-in">
        <div class="menu-grid" :key="activeMenuFilter + activeMenuTypeFilter">
          <div
            v-for="menu in filteredMenus"
            :key="menu.id"
            class="menu-card"
            :class="[{ selected: selectedMenu?.id === menu.id }, `status-${menu.status}`]"
            @click="switchMenu(menu)"
          >
            <div class="menu-card-header">
              <div class="menu-icon">
                <el-icon :size="24">
                  <Sunny
                    v-if="menu.name && (menu.name.includes('早餐') || menu.name.includes('午餐'))"
                  />
                  <Moon v-else-if="menu.name && menu.name.includes('晚餐')" />
                  <Coffee v-else-if="menu.name && menu.name.includes('加餐')" />
                  <Dish v-else />
                </el-icon>
              </div>
              <div class="menu-status-badge" :class="`status-badge-${menu.status}`">
                <el-icon :size="14">
                  <CircleCheck v-if="menu.status === 'online'" />
                  <CircleClose v-else-if="menu.status === 'offline'" />
                  <Clock v-else />
                </el-icon>
                <span>{{ menuStatusMap[menu.status].text }}</span>
              </div>
            </div>
            <div class="menu-name">{{ menu.name }}</div>
            <div class="menu-info">
              <span class="dishes-count"
                ><el-icon :size="14"><Dish /></el-icon> {{ menu.dishes }} 菜品</span
              >
              <span class="update-time"
                ><el-icon :size="14"><Clock /></el-icon> {{ formatTime(menu.updateTime) }}</span
              >
            </div>
            <div class="menu-auto-time" v-if="menu.autoOnline || menu.autoOffline">
              <span v-if="menu.autoOnline"
                ><el-icon :size="12"><ArrowRight /></el-icon>
                {{ formatTime(menu.autoOnline) }}</span
              >
              <span v-if="menu.autoOffline"
                ><el-icon :size="12"><ArrowRight /></el-icon>
                {{ formatTime(menu.autoOffline) }}</span
              >
            </div>
          </div>
        </div>
      </transition>

      <!-- 空状态 -->
      <div v-if="filteredMenus.length === 0" class="empty-state">
        <div class="empty-icon">
          <el-icon :size="64"><Dish /></el-icon>
        </div>
        <p class="empty-text">今日暂未设置菜单</p>
      </div>

      <div class="view-all">
        <el-button type="text" @click="navigateToMenu">
          <el-icon :size="18"><ArrowRight /></el-icon> 查看全部菜单
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
.today-menu-container {
  // 统计概览卡片
  .statistics-overview {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;

    .stat-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
      border: 1px solid #f0f0f0;
      transition: all 0.3s ease;
      cursor: default;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
      }

      .stat-icon {
        font-size: 36px;
        width: 60px;
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 12px;
        background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
        flex-shrink: 0;

        .el-icon {
          color: #909399;
        }
      }

      .stat-content {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: #303133;
          line-height: 1;
          margin-bottom: 6px;
        }

        .stat-value-small {
          font-size: 13px;
          font-weight: 500;
          color: #606266;
          line-height: 1.4;
          margin-bottom: 6px;
        }

        .stat-label {
          font-size: 13px;
          color: #909399;
          font-weight: 500;
        }
      }

      &.stat-total .stat-icon {
        background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
      }

      &.stat-online .stat-icon {
        background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
      }

      &.stat-dishes .stat-icon {
        background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
      }

      &.stat-time .stat-icon {
        background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
      }
    }
  }

  // 今日菜单卡片
  .quick-actions-card {
    margin-bottom: 24px;

    &.today-menu-card {
      padding: 24px;
      border-radius: 16px;
      background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
      border: 1px solid #e8e8e8;

      .menu-header {
        display: flex;
        justify-content: flex-start;
        align-items: center;
        margin-bottom: 20px;
        flex-wrap: wrap;
        gap: 16px;

        &:first-child {
          padding-bottom: 16px;
          border-bottom: 2px solid #f0f0f0;
        }

        .card-title {
          font-size: 20px;
          font-weight: 700;
          margin: 0;
          color: #303133;
          margin-right: auto;
          display: flex;
          align-items: center;
          gap: 8px;

          .el-icon {
            color: #909399;
          }
        }

        .filter-label {
          font-weight: 600;
          margin-right: 12px;
          color: #606266;
          font-size: 14px;
        }

        .filter-section {
          display: flex;
          align-items: center;
          gap: 12px;
          flex-wrap: wrap;

          .menu-filter-tag,
          .menu-status-tag {
            cursor: pointer;
            transition: all 0.3s ease;
            border-radius: 20px;
            padding: 6px 16px;
            font-size: 13px;
            font-weight: 500;
            border: 1.5px solid #e0e0e0;
            background: #fafafa;
            color: #606266;

            &:hover {
              transform: translateY(-2px);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
              background: #f0f0f0;
            }

            &.active {
              background: #67c23a;
              color: #ffffff;
              border-color: #67c23a;
              box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
            }

            &.tag-online {
              &:hover,
              &.active {
                background: #67c23a;
                border-color: #67c23a;
                color: #ffffff;
              }
            }

            &.tag-offline {
              &:hover,
              &.active {
                background: #909399;
                border-color: #909399;
                color: #ffffff;
              }
            }

            &.tag-draft {
              &:hover,
              &.active {
                background: #e6a23c;
                border-color: #e6a23c;
                color: #ffffff;
              }
            }
          }
        }
      }

      // 菜单卡片网格
      .menu-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
        gap: 16px;
        margin-bottom: 20px;

        .menu-card {
          background: #ffffff;
          border-radius: 12px;
          padding: 18px;
          border: 2px solid #f0f0f0;
          transition: all 0.3s ease;
          cursor: pointer;
          position: relative;
          overflow: hidden;

          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
            opacity: 0;
            transition: opacity 0.3s ease;
          }

          &:hover {
            transform: translateY(-6px);
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
            border-color: #409eff;

            &::before {
              opacity: 1;
            }

            .menu-icon {
              transform: scale(1.1) rotate(5deg);
            }
          }

          &.selected {
            border-color: #409eff;
            background: linear-gradient(135deg, #ecf5ff 0%, #ffffff 100%);
            box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);

            &::before {
              opacity: 1;
            }
          }

          &.status-online {
            border-left: 4px solid #67c23a;
          }

          &.status-offline {
            border-left: 4px solid #909399;
          }

          &.status-draft {
            border-left: 4px solid #e6a23c;
          }

          .menu-card-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 12px;

            .menu-icon {
              font-size: 32px;
              width: 50px;
              height: 50px;
              display: flex;
              align-items: center;
              justify-content: center;
              background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
              border-radius: 10px;
              transition: transform 0.3s ease;

              .el-icon {
                color: #909399;
              }
            }

            .menu-status-badge {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 12px;
              font-weight: 500;
              padding: 4px 10px;
              border-radius: 12px;
              background: #f5f7fa;

              .el-icon {
                color: #909399;
              }

              &.status-badge-online {
                background: #f0f9ff;
                color: #67c23a;

                .el-icon {
                  color: #67c23a;
                }
              }

              &.status-badge-offline {
                background: #f5f7fa;
                color: #909399;

                .el-icon {
                  color: #909399;
                }
              }

              &.status-badge-draft {
                background: #fdf6ec;
                color: #e6a23c;

                .el-icon {
                  color: #e6a23c;
                }
              }
            }
          }

          .menu-name {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 12px;
            line-height: 1.4;
          }

          .menu-info {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            font-size: 13px;
            color: #909399;
            margin-bottom: 8px;

            .dishes-count {
              color: #606266;
              font-weight: 500;
              display: flex;
              align-items: center;
              gap: 4px;

              .el-icon {
                color: #909399;
              }
            }

            .update-time {
              display: flex;
              align-items: center;
              gap: 4px;

              .el-icon {
                color: #909399;
              }
            }
          }

          .menu-auto-time {
            display: flex;
            gap: 12px;
            font-size: 12px;
            color: #909399;
            padding-top: 8px;
            border-top: 1px dashed #e8e8e8;

            span {
              display: flex;
              align-items: center;
              gap: 4px;

              .el-icon {
                color: #909399;
              }
            }
          }
        }
      }

      // 空状态
      .empty-state {
        text-align: center;
        padding: 60px 20px;
        color: #909399;

        .empty-icon {
          font-size: 64px;
          margin-bottom: 16px;
          opacity: 0.5;

          .el-icon {
            color: #c0c4cc;
          }
        }

        .empty-text {
          font-size: 16px;
          margin: 0;
          font-weight: 500;
        }
      }

      .view-all {
        text-align: right;
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #f0f0f0;

        .el-button {
          color: #409eff;
          font-weight: 600;
          transition: all 0.3s ease;

          &:hover {
            color: #66b1ff;
            transform: translateX(4px);
          }
        }
      }
    }

    .quick-actions-card {
      margin-bottom: 24px;

      &.today-menu-card {
        padding: 24px;
        border-radius: 16px;
        background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
        box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
        border: 1px solid #e8e8e8;

        .menu-header {
          display: flex;
          justify-content: flex-start;
          align-items: center;
          margin-bottom: 20px;
          flex-wrap: wrap;
          gap: 16px;

          &:first-child {
            padding-bottom: 16px;
            border-bottom: 2px solid #f0f0f0;
          }

          .card-title {
            font-size: 20px;
            font-weight: 700;
            margin: 0;
            color: #303133;
            margin-right: auto;
            display: flex;
            align-items: center;
            gap: 8px;

            .el-icon {
              color: #909399;
            }
          }

          .filter-label {
            font-weight: 600;
            margin-right: 12px;
            color: #606266;
            font-size: 14px;
          }

          .filter-section {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-wrap: wrap;

            .menu-filter-tag,
            .menu-status-tag {
              cursor: pointer;
              transition: all 0.3s ease;
              border-radius: 20px;
              padding: 6px 16px;
              font-size: 13px;
              font-weight: 500;
              border: 1.5px solid #e0e0e0;
              background: #fafafa;
              color: #606266;

              &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                background: #f0f0f0;
              }

              &.active {
                background: #67c23a;
                color: #ffffff;
                border-color: #67c23a;
                box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
              }

              &.tag-online {
                &:hover,
                &.active {
                  background: #67c23a;
                  border-color: #67c23a;
                  color: #ffffff;
                }
              }

              &.tag-offline {
                &:hover,
                &.active {
                  background: #909399;
                  border-color: #909399;
                  color: #ffffff;
                }
              }

              &.tag-draft {
                &:hover,
                &.active {
                  background: #e6a23c;
                  border-color: #e6a23c;
                  color: #ffffff;
                }
              }
            }
          }
        }

        // 菜单卡片网格
        .menu-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
          gap: 16px;
          margin-bottom: 20px;

          .menu-card {
            background: #ffffff;
            border-radius: 12px;
            padding: 18px;
            border: 2px solid #f0f0f0;
            transition: all 0.3s ease;
            cursor: pointer;
            position: relative;
            overflow: hidden;

            &::before {
              content: '';
              position: absolute;
              top: 0;
              left: 0;
              right: 0;
              height: 4px;
              background: linear-gradient(90deg, #409eff 0%, #66b1ff 100%);
              opacity: 0;
              transition: opacity 0.3s ease;
            }

            &:hover {
              transform: translateY(-6px);
              box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
              border-color: #409eff;

              &::before {
                opacity: 1;
              }

              .menu-icon {
                transform: scale(1.1) rotate(5deg);
              }
            }

            &.selected {
              border-color: #409eff;
              background: linear-gradient(135deg, #ecf5ff 0%, #ffffff 100%);
              box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);

              &::before {
                opacity: 1;
              }
            }

            &.status-online {
              border-left: 4px solid #67c23a;
            }

            &.status-offline {
              border-left: 4px solid #909399;
            }

            &.status-draft {
              border-left: 4px solid #e6a23c;
            }

            .menu-card-header {
              display: flex;
              justify-content: space-between;
              align-items: flex-start;
              margin-bottom: 12px;

              .menu-icon {
                font-size: 32px;
                width: 50px;
                height: 50px;
                display: flex;
                align-items: center;
                justify-content: center;
                background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
                border-radius: 10px;
                transition: transform 0.3s ease;

                .el-icon {
                  color: #909399;
                }
              }

              .menu-status-badge {
                display: flex;
                align-items: center;
                gap: 4px;
                font-size: 12px;
                font-weight: 500;
                padding: 4px 10px;
                border-radius: 12px;
                background: #f5f7fa;

                .el-icon {
                  color: #909399;
                }

                &.status-badge-online {
                  background: #f0f9ff;
                  color: #67c23a;

                  .el-icon {
                    color: #67c23a;
                  }
                }

                &.status-badge-offline {
                  background: #f5f7fa;
                  color: #909399;

                  .el-icon {
                    color: #909399;
                  }
                }

                &.status-badge-draft {
                  background: #fdf6ec;
                  color: #e6a23c;

                  .el-icon {
                    color: #e6a23c;
                  }
                }
              }
            }

            .menu-name {
              font-size: 16px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 12px;
              line-height: 1.4;
            }

            .menu-info {
              display: flex;
              flex-wrap: wrap;
              gap: 12px;
              font-size: 13px;
              color: #909399;
              margin-bottom: 8px;

              .dishes-count {
                color: #606266;
                font-weight: 500;
                display: flex;
                align-items: center;
                gap: 4px;

                .el-icon {
                  color: #909399;
                }
              }

              .update-time {
                display: flex;
                align-items: center;
                gap: 4px;

                .el-icon {
                  color: #909399;
                }
              }
            }

            .menu-auto-time {
              display: flex;
              gap: 12px;
              font-size: 12px;
              color: #909399;
              padding-top: 8px;
              border-top: 1px dashed #e8e8e8;

              span {
                display: flex;
                align-items: center;
                gap: 4px;

                .el-icon {
                  color: #909399;
                }
              }
            }
          }
        }

        // 空状态
        .empty-state {
          text-align: center;
          padding: 60px 20px;
          color: #909399;

          .empty-icon {
            font-size: 64px;
            margin-bottom: 16px;
            opacity: 0.5;

            .el-icon {
              color: #c0c4cc;
            }
          }

          .empty-text {
            font-size: 16px;
            margin: 0;
            font-weight: 500;
          }
        }

        .view-all {
          text-align: right;
          margin-top: 20px;
          padding-top: 20px;
          border-top: 1px solid #f0f0f0;

          .el-button {
            color: #409eff;
            font-weight: 600;
            transition: all 0.3s ease;

            &:hover {
              color: #66b1ff;
              transform: translateX(4px);
            }
          }
        }
      }
    }
  }
}

// 动画效果
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.4s ease;
}

.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}

.list-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

// 响应式设计
@media (max-width: 768px) {
  .today-menu-container {
    .statistics-overview {
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;

      .stat-card {
        padding: 16px;

        .stat-icon {
          font-size: 28px;
          width: 48px;
          height: 48px;
        }

        .stat-content {
          .stat-value {
            font-size: 22px;
          }

          .stat-value-small {
            font-size: 11px;
          }

          .stat-label {
            font-size: 12px;
          }
        }
      }
    }

    .quick-actions-card.today-menu-card {
      .menu-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>
