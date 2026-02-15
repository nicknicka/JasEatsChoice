<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import {
  Shop,
  Clock,
  Location,
  Money,
  Van,
  CircleCheck,
  EditPen,
  Phone
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const orderId = ref(route.params.id)
const order = ref(null)
const loading = ref(true)

// 订单状态映射
const orderStatusMap = {
  all: '全部订单',
  pending: '待支付',
  pendingAccept: '待接单',
  processing: '进行中',
  pendingComment: '待评价',
  delivered: '已送达',
  completed: '已完成',
  cancelled: '已取消'
}

// 订单状态标签样式映射
const statusTagTypeMap = {
  pending: 'info',
  pendingAccept: 'warning',
  processing: 'primary',
  pendingComment: 'info',
  delivered: 'success',
  completed: 'success',
  cancelled: 'danger'
}

// 订单进度步骤配置
const orderProgressSteps = computed(() => {
  if (!order.value) return []

  const steps = [
    { title: '待支付', icon: Clock, status: 'wait' },
    { title: '待接单', icon: Clock, status: 'wait' },
    { title: '进行中', icon: Clock, status: 'process' },
    { title: '已送达', icon: Van, status: 'wait' },
    { title: '已完成', icon: CircleCheck, status: 'wait' }
  ]

  // 根据订单状态调整进度
  const statusIndexMap = {
    pending: 0,
    pendingAccept: 1,
    processing: 2,
    delivered: 3,
    completed: 4,
    cancelled: 2,
    pendingComment: 4
  }

  const currentIndex = statusIndexMap[order.value.status] || 0

  steps.forEach((step, index) => {
    if (index < currentIndex) {
      step.status = 'success'
    } else if (index === currentIndex) {
      step.status = order.value.status === 'cancelled' ? 'error' : 'process'
    } else {
      step.status = 'wait'
    }
  })

  return steps
})

// 将后端状态码转换为前端状态文本
const orderStatusToText = (statusCode) => {
  const statusMap = {
    0: 'pending', // 待支付
    1: 'pendingAccept', // 待接单
    2: 'processing', // 备菜中
    3: 'processing', // 烹饪中
    4: 'processing', // 待上菜
    5: 'delivered', // 已送达
    6: 'cancelled' // 已取消
  }
  return statusMap[statusCode] || 'pending'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 处理图片加载错误
const handleImageError = (event) => {
  const img = event.target
  img.style.display = 'none'
  const parent = img.parentElement
  if (parent && !parent.querySelector('.no-image')) {
    const noImageDiv = document.createElement('div')
    noImageDiv.className = 'no-image'
    noImageDiv.innerHTML = '<span>菜</span>'
    parent.appendChild(noImageDiv)
  }
}

// 加载订单详情(包含菜品信息)
const loadOrderDetail = async () => {
  loading.value = true
  try {
    // 1. 获取订单基本信息
    const orderResponse = await axios.get(
      API_CONFIG.baseURL + API_CONFIG.order.detail + orderId.value
    )

    if (!orderResponse.data?.data) {
      throw new Error('订单不存在')
    }

    const orderData = orderResponse.data.data

    // 2. 获取订单菜品信息
    const dishesResponse = await axios.get(`${API_CONFIG.baseURL}/v1/orders/${orderData.id}/dishes`)

    // 3. 获取菜品详情
    let items = []
    if (dishesResponse.data?.data && dishesResponse.data.data.length > 0) {
      items = await Promise.all(
        dishesResponse.data.data.map(async (orderDish) => {
          try {
            const dishResponse = await axios.get(`${API_CONFIG.baseURL}/dishes/${orderDish.dishId}`)
            const dish = dishResponse.data?.data
            return {
              id: dish?.id || orderDish.dishId,
              name: dish?.name || `菜品${orderDish.dishId}`,
              quantity: orderDish.quantity,
              price: orderDish.price,
              customization: orderDish.customization,
              note: orderDish.note || '',
              image: dish?.image || '',
              // 添加食材信息
              optionalIngredients: dish?.optionalIngredients || [],
              requiredIngredients: dish?.requiredIngredients || []
            }
          } catch (error) {
            console.error(`获取菜品${orderDish.dishId}详情失败:`, error)
            // 降级处理：使用订单中存储的信息
            return {
              id: orderDish.dishId,
              name: orderDish.dishName || `菜品${orderDish.dishId}`,  // 优先使用订单中的菜品名称
              quantity: orderDish.quantity,
              price: orderDish.price,
              image: orderDish.dishImage || '',                     // 优先使用订单中的图片
              optionalIngredients: orderDish.optionalIngredients || [],
              requiredIngredients: orderDish.requiredIngredients || [],
              note: orderDish.note || '',
              unavailable: true                                     // 标记为不可用
            }
          }
        })
      )
    }

    // 4. 组装订单数据
    order.value = {
      id: orderData.id,
      orderNo: orderData.id,
      status: orderStatusToText(orderData.status),
      merchant: `商家${orderData.merchantId}`,
      merchantId: orderData.merchantId,
      total: orderData.totalAmount,
      time: formatTime(orderData.createTime),
      items: items,
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
      // 添加配送信息
      address: orderData.address || '商家地址',
      deliveryFee: 5.0,
      // 添加支付信息
      paymentMethod: '平台币余额',
      paymentTime: orderData.updateTime ? formatTime(orderData.updateTime) : null,
      remark: orderData.remark || '',
      _raw: orderData
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
    order.value = null
  } finally {
    loading.value = false
  }
}

// 组件挂载时加载订单详情
onMounted(() => {
  loadOrderDetail()
})

// 取消订单
const cancelOrder = (order) => {
  axios
    .put(API_CONFIG.baseURL + API_CONFIG.order.detail + order.id + '/cancel')
    .then((response) => {
      if (response.data.success) {
        order.status = 'cancelled'
        ElMessage.success('订单已取消')
        loadOrderDetail() // 重新加载订单详情
      } else {
        ElMessage.error(response.data.message || '取消订单失败')
      }
    })
    .catch((error) => {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请稍后重试')
    })
}

// 跳转到评价页面
const goToEvaluate = (orderId) => {
  router.push(`/user/home/evaluate-order/${orderId}`)
}

// 联系商家
const contactMerchant = () => {
  if (!order.value?.merchantId) {
    ElMessage.error('商家信息不存在')
    return
  }

  // 跳转到聊天页面，并创建与商家的对话
  router.push({
    path: '/user/home/chat',
    query: {
      type: 'single',
      targetId: order.value.merchantId,
      targetName: order.value.merchantName || '商家'
    }
  })
}

// 获取当前活动步骤
const getActiveStep = () => {
  if (!order.value) return 0

  const statusStepMap = {
    pending: 0,
    pendingAccept: 1,
    processing: 2,
    delivered: 3,
    completed: 4,
    cancelled: 2,
    pendingComment: 4
  }

  return statusStepMap[order.value.status] || 0
}
</script>

<template>
  <div class="order-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <CommonBackButton />
      <h2 class="page-title">订单详情</h2>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" element-loading-text="加载中..." class="loading-container">
      <!-- 订单详情内容 -->
      <div v-if="order" class="order-detail-content">
        <!-- 订单状态卡片 -->
        <el-card class="status-card scale-in" shadow="hover">
          <div class="status-header">
            <div class="order-info-basic">
              <div class="order-no">订单号: {{ order.orderNo }}</div>
              <el-tag :type="statusTagTypeMap[order.status]" size="large" effect="dark">
                {{ orderStatusMap[order.status] }}
              </el-tag>
            </div>
          </div>

          <!-- 订单进度 -->
          <div class="order-progress">
            <el-steps :active="getActiveStep()" finish-status="success" align-center>
              <el-step title="待支付" />
              <el-step title="待接单" />
              <el-step title="进行中" />
              <el-step title="已送达" />
              <el-step title="已完成" />
            </el-steps>
          </div>
        </el-card>

        <!-- 商家信息卡片 -->
        <el-card class="merchant-card fade-in-up delay-100" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="#6ba4ff"><Shop /></el-icon>
              <span class="card-title">商家信息</span>
              <el-button type="primary" link @click="contactMerchant">
                <el-icon><Phone /></el-icon>
                联系商家
              </el-button>
            </div>
          </template>
          <div class="merchant-details">
            <div class="merchant-name">{{ order.merchant }}</div>
            <div class="merchant-meta">
              <span class="rating">4.8分</span>
              <span class="delivery-time">约30分钟</span>
            </div>
          </div>
        </el-card>

        <!-- 配送信息卡片 -->
        <el-card class="delivery-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="#67c23a"><Location /></el-icon>
              <span class="card-title">配送信息</span>
            </div>
          </template>
          <div class="delivery-details">
            <div class="delivery-item">
              <span class="label">配送地址:</span>
              <span class="value">{{ order.address }}</span>
            </div>
            <div class="delivery-item">
              <span class="label">配送费:</span>
              <span class="value">¥{{ order.deliveryFee.toFixed(2) }}</span>
            </div>
            <div
              class="delivery-item"
              v-if="order.status === 'processing' || order.status === 'delivered'"
            >
              <span class="label">预计送达:</span>
              <span class="value highlight">约30分钟</span>
            </div>
          </div>
        </el-card>

        <!-- 订单商品卡片 -->
        <el-card class="items-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单商品</span>
              <el-tag type="info" effect="plain">共 {{ order.itemCount || 0 }} 件商品</el-tag>
            </div>
          </template>

          <div class="items-list" v-if="order.items && order.items.length > 0">
            <div class="item-row stagger-item" v-for="(item, index) in order.items" :key="index">
              <!-- 商品图片 -->
              <div class="item-image">
                <img
                  v-if="item.image"
                  :src="item.image"
                  :alt="item.name"
                  loading="lazy"
                  @error="handleImageError($event)"
                />
                <div v-else class="no-image">
                  <span>{{ item.name?.charAt(0) || '菜' }}</span>
                </div>
                <!-- 数量徽章 -->
                <div class="quantity-badge" :class="{ 'large-number': item.quantity > 9 }">
                  {{ item.quantity }}
                </div>
              </div>

              <!-- 商品信息 -->
              <div class="item-info" :class="{ 'item-unavailable': item.unavailable }">
                <div class="item-name">
                  {{ item.name }}
                  <el-tag v-if="item.unavailable" type="danger" size="small" style="margin-left: 8px">
                    该菜品已下架
                  </el-tag>
                </div>

                <!-- 必选食材 -->
                <div
                  v-if="item.requiredIngredients && item.requiredIngredients.length > 0"
                  class="item-ingredients"
                >
                  <div class="ingredients-label">
                    <span class="label-text">必选:</span>
                  </div>
                  <div class="ingredients-list">
                    <span
                      class="ingredient-tag required"
                      v-for="ing in item.requiredIngredients"
                      :key="ing"
                    >
                      {{ ing }}
                    </span>
                  </div>
                </div>

                <!-- 可选食材 -->
                <div
                  v-if="item.optionalIngredients && item.optionalIngredients.length > 0"
                  class="item-ingredients"
                >
                  <div class="ingredients-label">
                    <span class="label-text">可选:</span>
                  </div>
                  <div class="ingredients-list">
                    <span
                      class="ingredient-tag optional"
                      v-for="ing in item.optionalIngredients"
                      :key="ing.id || ing.name"
                    >
                      {{ ing.name }}
                      <span v-if="ing.price" class="ingredient-price"
                        >+¥{{ ing.price.toFixed(2) }}</span
                      >
                    </span>
                  </div>
                </div>

                <!-- 备注信息 -->
                <div v-if="item.note" class="item-note">
                  <el-icon><EditPen /></el-icon>
                  <span class="note-text">{{ item.note }}</span>
                </div>

                <!-- 自定义信息（兼容旧数据） -->
                <div v-if="item.customization && !item.note" class="item-note">
                  <el-icon><EditPen /></el-icon>
                  <span class="note-text">{{ item.customization }}</span>
                </div>

                <div class="item-price-detail">
                  <span class="unit-price">¥{{ item.price.toFixed(2) }} /份</span>
                  <span class="total-price"
                    >小计 ¥{{ (item.price * item.quantity).toFixed(2) }}</span
                  >
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="items-empty">
            <el-empty description="暂无商品信息" :image-size="60" />
          </div>
        </el-card>

        <!-- 订单金额卡片 -->
        <el-card class="amount-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="#e6a23c"><Money /></el-icon>
              <span class="card-title">订单金额</span>
            </div>
          </template>

          <div class="amount-details">
            <div class="amount-row">
              <span class="amount-label">商品总额</span>
              <span class="amount-value">¥{{ (order.total - order.deliveryFee).toFixed(2) }}</span>
            </div>
            <div class="amount-row">
              <span class="amount-label">配送费</span>
              <span class="amount-value">¥{{ order.deliveryFee.toFixed(2) }}</span>
            </div>
            <el-divider />
            <div class="amount-row total-row">
              <span class="total-label">实付金额</span>
              <span class="total-value number-scroll">¥{{ order.total.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 支付信息卡片 -->
        <el-card class="payment-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="#909399"><Money /></el-icon>
              <span class="card-title">支付信息</span>
            </div>
          </template>

          <div class="payment-details">
            <div class="payment-row">
              <span class="payment-label">支付方式:</span>
              <span class="payment-value">{{ order.paymentMethod }}</span>
            </div>
            <div class="payment-row">
              <span class="payment-label">下单时间:</span>
              <span class="payment-value">{{ order.time }}</span>
            </div>
            <div class="payment-row" v-if="order.paymentTime">
              <span class="payment-label">支付时间:</span>
              <span class="payment-value">{{ order.paymentTime }}</span>
            </div>
          </div>
        </el-card>

        <!-- 订单备注卡片 -->
        <el-card v-if="order.remark" class="remark-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="#409eff"><EditPen /></el-icon>
              <span class="card-title">订单备注</span>
            </div>
          </template>
          <div class="remark-content">{{ order.remark }}</div>
        </el-card>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button
            v-if="order.status === 'pendingAccept'"
            type="danger"
            size="large"
            @click="cancelOrder(order)"
            class="action-btn cancel-btn"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'pendingComment'"
            type="success"
            size="large"
            @click="goToEvaluate(order.id)"
            class="action-btn evaluate-btn"
          >
            <el-icon><CircleCheck /></el-icon>
            去评价
          </el-button>
          <el-button
            v-if="order.status === 'pendingAccept' || order.status === 'processing'"
            type="primary"
            size="large"
            @click="contactMerchant"
            class="action-btn contact-btn"
          >
            <el-icon><Phone /></el-icon>
            联系商家
          </el-button>
        </div>
      </div>

      <!-- 空数据提示 -->
      <el-empty v-else-if="!loading" description="暂无订单详情" />
    </div>
  </div>
</template>

<style scoped lang="less">
.order-detail-container {
  padding: 0 20px 20px 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 80px);

  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: #ffffff;
    border-radius: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.06);
  }

  .page-title {
    font-size: 24px;
    margin: 0;
    margin-left: 15px;
    color: #2c5282;
    font-weight: 600;
  }

  .loading-container {
    min-height: 400px;
  }

  .order-detail-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  // 通用卡片样式
  .el-card {
    background: #ffffff;
    border-radius: 16px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    overflow: hidden;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(92, 142, 255, 0.15);
      border-color: rgba(92, 142, 255, 0.3);
    }

    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid rgba(0, 0, 0, 0.06);
      background: linear-gradient(to bottom, #fafbfc 0%, #ffffff 100%);
    }

    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #2c3e50;
      flex: 1;
    }
  }

  // 状态卡片
  .status-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;

    :deep(.el-card__body) {
      padding: 24px;
    }

    .status-header {
      margin-bottom: 24px;
    }

    .order-info-basic {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 12px;

      .order-no {
        font-size: 16px;
        font-weight: 600;
        color: #ffffff;
      }

      :deep(.el-tag) {
        border: none;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      }
    }

    .order-progress {
      :deep(.el-steps) {
        .el-step__head {
          .el-step__icon {
            background: rgba(255, 255, 255, 0.2);
            border-color: rgba(255, 255, 255, 0.4);

            .el-step__icon-inner {
              color: #ffffff;
            }
          }

          &.is-process {
            .el-step__icon {
              background: #ffffff;
              border-color: #ffffff;

              .el-step__icon-inner {
                color: #667eea;
              }
            }
          }

          &.is-success {
            .el-step__icon {
              background: rgba(255, 255, 255, 0.3);
              border-color: #ffffff;

              .el-step__icon-inner {
                color: #ffffff;
              }
            }
          }
        }

        .el-step__title {
          &.is-process {
            color: #ffffff;
            font-weight: 600;
          }

          &.is-success {
            color: rgba(255, 255, 255, 0.8);
          }
        }

        .el-step__line {
          background-color: rgba(255, 255, 255, 0.3);

          .el-step__line-inner {
            background-color: #ffffff;
          }
        }
      }
    }
  }

  // 商家卡片
  .merchant-card {
    .merchant-details {
      .merchant-name {
        font-size: 18px;
        font-weight: 600;
        color: #2c3e50;
        margin-bottom: 8px;
      }

      .merchant-meta {
        display: flex;
        align-items: center;
        gap: 16px;
        font-size: 14px;
        color: #7f8c8d;

        .rating {
          color: #e6a23c;
          font-weight: 500;
        }

        .delivery-time {
          color: #67c23a;
        }
      }
    }
  }

  // 配送信息卡片
  .delivery-card {
    .delivery-details {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .delivery-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 0;
        border-bottom: 1px solid rgba(0, 0, 0, 0.06);

        &:last-child {
          border-bottom: none;
        }

        .label {
          font-size: 14px;
          color: #7f8c8d;
          font-weight: 500;
        }

        .value {
          font-size: 14px;
          color: #2c3e50;
          font-weight: 500;

          &.highlight {
            color: #67c23a;
            font-weight: 600;
          }
        }
      }
    }
  }

  // 商品卡片
  .items-card {
    .items-list {
      display: flex;
      flex-direction: column;
      gap: 14px;
    }

    .item-row {
      display: flex;
      align-items: stretch;
      padding: 14px;
      background: #fafbfc;
      border-radius: 12px;
      transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      border: 1px solid rgba(0, 0, 0, 0.06);
      min-height: 94px;

      &:hover {
        box-shadow: 0 6px 20px rgba(92, 142, 255, 0.2);
        transform: translateY(-3px) scale(1.015);
        border-color: rgba(92, 142, 255, 0.5);
        background: linear-gradient(to bottom, #ffffff 0%, #f8faff 100%);

        &::after {
          content: '';
          position: absolute;
          inset: 0;
          border-radius: 12px;
          box-shadow: inset 0 0 0 1px rgba(92, 142, 255, 0.3);
          pointer-events: none;
        }
      }

      .item-image {
        width: 70px;
        min-height: 70px;
        height: 70px;
        border-radius: 10px;
        overflow: visible;
        margin-right: 14px;
        flex-shrink: 0;
        background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
        position: relative;
        z-index: 1;
        box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px solid #91d5ff;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 10px;
          position: relative;
          z-index: 1;
        }

        .no-image {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;

          span {
            font-size: 28px;
            font-weight: 600;
            color: #1890ff;
          }
        }

        .quantity-badge {
          position: absolute;
          top: -4px;
          right: -4px;
          background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
          color: white;
          font-size: 11px;
          font-weight: 700;
          min-width: 18px;
          height: 18px;
          padding: 0 5px;
          border-radius: 9px;
          box-shadow:
            0 2px 6px rgba(255, 77, 79, 0.4),
            0 0 0 1.5px rgba(255, 255, 255, 1);
          z-index: 100;
          display: flex;
          align-items: center;
          justify-content: center;
          line-height: 1;
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          animation: badge-bounce 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55);
          pointer-events: none;

          .item-row:hover & {
            transform: scale(1.1) rotate(-3deg);
            box-shadow:
              0 3px 8px rgba(255, 77, 79, 0.5),
              0 0 0 1.5px rgba(255, 255, 255, 1);
            background: linear-gradient(135deg, #ff4d4f 0%, #ff2626 100%);
          }

          &.large-number {
            font-size: 9px;
            min-width: 20px;
            padding: 0 4px;
          }
        }

        @keyframes badge-bounce {
          0% {
            transform: scale(0) rotate(-180deg);
            opacity: 0;
          }
          50% {
            transform: scale(1.2) rotate(10deg);
          }
          100% {
            transform: scale(1) rotate(0deg);
            opacity: 1;
          }
        }
      }

      .item-info {
        flex: 1;
        min-width: 0;
        display: flex;
        flex-direction: column;
        justify-content: space-between;

        .item-name {
          font-size: 15px;
          font-weight: 600;
          color: #2c5282;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          flex-shrink: 0;
          display: flex;
          align-items: center;
        }

        // 不可用菜品样式
        &.item-unavailable {
          opacity: 0.6;

          .item-name {
            color: #909399;
          }
        }

        .item-ingredients {
          display: flex;
          align-items: flex-start;
          gap: 6px;
          margin-bottom: 8px;
          font-size: 12px;

          .ingredients-label {
            flex-shrink: 0;

            .label-text {
              color: #64748b;
              font-weight: 500;
            }
          }

          .ingredients-list {
            display: flex;
            flex-wrap: wrap;
            gap: 5px;
            flex: 1;

            .ingredient-tag {
              display: inline-flex;
              align-items: center;
              gap: 3px;
              padding: 3px 8px;
              border-radius: 6px;
              font-size: 11px;
              line-height: 1.4;
              transition: all 0.2s ease;

              &.required {
                background: linear-gradient(
                  135deg,
                  rgba(103, 194, 58, 0.9) 0%,
                  rgba(93, 175, 52, 0.9) 100%
                );
                color: white;
                font-weight: 500;
                box-shadow: 0 1px 4px rgba(103, 194, 58, 0.25);
              }

              &.optional {
                background: rgba(232, 244, 232, 0.8);
                color: #5da842;
                border: 1px solid rgba(179, 225, 157, 0.5);
              }

              .ingredient-price {
                font-size: 10px;
                opacity: 0.85;
                margin-left: 2px;
              }
            }
          }
        }

        .item-note {
          display: flex;
          align-items: flex-start;
          gap: 5px;
          margin-bottom: 8px;
          font-size: 12px;
          color: #c4873a;
          padding: 6px 10px;
          background: rgba(253, 246, 236, 0.8);
          border-radius: 8px;
          border: 1px solid rgba(245, 218, 177, 0.5);
          line-height: 1.5;
          box-shadow: 0 1px 4px rgba(230, 162, 60, 0.08);

          .el-icon {
            font-size: 13px;
            color: #c4873a;
            flex-shrink: 0;
            margin-top: 1px;
          }

          .note-text {
            flex: 1;
            word-break: break-word;
          }
        }

        .item-price-detail {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-top: auto;
          flex-shrink: 0;

          .unit-price {
            font-size: 12px;
            color: #94a3b8;
          }

          .total-price {
            font-size: 16px;
            font-weight: 700;
            color: #ff6b6b;
          }
        }
      }
    }

    .items-empty {
      text-align: center;
      padding: 24px;
      color: #94a3b8;
      font-size: 14px;
    }
  }

  // 金额卡片
  .amount-card {
    .amount-details {
      .amount-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 0;

        .amount-label {
          font-size: 14px;
          color: #7f8c8d;
        }

        .amount-value {
          font-size: 15px;
          color: #2c3e50;
          font-weight: 500;
        }

        &.total-row {
          padding-top: 16px;

          .total-label {
            font-size: 16px;
            font-weight: 600;
            color: #2c3e50;
          }

          .total-value {
            font-size: 24px;
            font-weight: 700;
            color: #e6a23c;
          }
        }
      }
    }
  }

  // 支付信息卡片
  .payment-card {
    .payment-details {
      display: flex;
      flex-direction: column;
      gap: 10px;

      .payment-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;

        .payment-label {
          font-size: 14px;
          color: #7f8c8d;
        }

        .payment-value {
          font-size: 14px;
          color: #2c3e50;
          font-weight: 500;
        }
      }
    }
  }

  // 备注卡片
  .remark-card {
    .remark-content {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 8px;
    }
  }

  // 操作按钮
  .order-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding: 20px 0;

    .action-btn {
      min-width: 160px;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 24px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
      }

      &.cancel-btn {
        background: linear-gradient(135deg, #ff8a80 0%, #ff6b6b 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(255, 107, 107, 0.3);

        &:hover {
          box-shadow: 0 6px 16px rgba(255, 107, 107, 0.4);
        }
      }

      &.evaluate-btn {
        background: linear-gradient(135deg, #81c784 0%, #66bb6a 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(102, 187, 106, 0.3);

        &:hover {
          box-shadow: 0 6px 16px rgba(102, 187, 106, 0.4);
        }
      }

      &.contact-btn {
        background: linear-gradient(135deg, #6ba4ff 0%, #5c8eff 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(92, 142, 255, 0.3);

        &:hover {
          box-shadow: 0 6px 16px rgba(92, 142, 255, 0.4);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .order-detail-container {
    padding: 0 12px 16px 12px;

    .page-header {
      padding: 14px 16px;
      border-radius: 14px;
    }

    .page-title {
      font-size: 20px;
    }

    .order-detail-content {
      gap: 12px;
    }

    .el-card {
      border-radius: 14px;

      :deep(.el-card__body) {
        padding: 16px;
      }

      :deep(.el-card__header) {
        padding: 14px 16px;
      }
    }

    .status-card {
      :deep(.el-card__body) {
        padding: 20px 16px;
      }

      .order-info-basic {
        flex-direction: column;
        align-items: flex-start !important;
        gap: 10px;
      }

      .order-progress {
        :deep(.el-steps) {
          .el-step__title {
            font-size: 12px !important;
          }

          .el-step__icon {
            width: 28px;
            height: 28px;
          }
        }
      }
    }

    .items-card {
      .item-row {
        padding: 12px;
        min-height: 80px;
        border-radius: 10px;

        .item-image {
          width: 56px;
          min-height: 56px;
          height: 56px;
          margin-right: 12px;
          border-radius: 8px;

          img {
            border-radius: 8px;
          }

          .quantity-badge {
            min-width: 16px;
            height: 16px;
            font-size: 10px;
            padding: 0 4px;
            top: -3px;
            right: -3px;
            border-radius: 8px;

            &.large-number {
              font-size: 8px;
              min-width: 18px;
              padding: 0 3px;
            }
          }

          .no-image span {
            font-size: 22px;
          }
        }

        .item-info {
          .item-name {
            font-size: 14px;
            margin-bottom: 6px;
          }

          .item-ingredients {
            font-size: 11px;
            margin-bottom: 6px;
            gap: 5px;

            .ingredients-list {
              gap: 4px;

              .ingredient-tag {
                font-size: 10px;
                padding: 2px 6px;
                border-radius: 4px;
              }
            }
          }

          .item-note {
            font-size: 11px;
            padding: 5px 8px;
            margin-bottom: 6px;
            border-radius: 6px;

            .el-icon {
              font-size: 12px;
            }
          }

          .item-price-detail {
            flex-direction: column;
            align-items: flex-start;
            gap: 4px;

            .unit-price {
              font-size: 11px;
            }

            .total-price {
              font-size: 15px;
            }
          }
        }
      }
    }

    .order-actions {
      flex-direction: column;
      gap: 10px;
      padding: 16px 0;

      .action-btn {
        width: 100%;
        min-width: auto;
        height: 44px;
        font-size: 15px;
      }
    }
  }
}
</style>
