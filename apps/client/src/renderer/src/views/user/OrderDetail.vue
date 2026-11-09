<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { reviewAPI } from '../../api/review.js'
import paymentApi from '../../api/payment'
import { useOrderData } from '../../composables/useOrderData'
import {
  Shop,
  Money,
  CircleCheck,
  EditPen,
  Phone,
  Star,
  Picture
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const orderId = ref(route.params.id)
const order = ref(null)
const loading = ref(true)
const paying = ref(false)
const { getUserId } = useOrderData()

// 评价相关
const review = ref(null)
const reviewLoading = ref(false)

// 订单状态映射（5状态系统：0-待支付、1-待接单、2-制作中、3-已完成、4-已取消）
const orderStatusMap = {
  all: '全部订单',
  pending: '待支付',
  pendingAccept: '待接单',
  preparing: '制作中',
  completed: '已完成',
  cancelled: '已取消'
}

// 订单状态标签样式映射
const statusTagTypeMap = {
  pending: 'warning',
  pendingAccept: 'warning',
  preparing: 'info',
  completed: 'success',
  cancelled: 'danger'
}

// 将后端状态码转换为前端状态文本（5状态系统）
const orderStatusToText = (statusCode) => {
  const statusMap = {
    0: 'pending',       // 待支付
    1: 'pendingAccept', // 待接单
    2: 'preparing',     // 制作中
    3: 'completed',     // 已完成
    4: 'cancelled'      // 已取消
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

    // 2. 获取商家名称
    let merchantName = ''
    try {
      const merchantResponse = await axios.get(
        `${API_CONFIG.baseURL}${API_CONFIG.merchant.detail}${orderData.merchantId}`
      )
      if (merchantResponse.data?.data?.name) {
        merchantName = merchantResponse.data.data.name
      }
    } catch (error) {
      console.error('获取商家名称失败:', error)
    }

    // 3. 获取订单菜品信息
    const dishesResponse = await axios.get(`${API_CONFIG.baseURL}/v1/orders/${orderData.id}/dishes`)

    // 3. 获取菜品详情
    let items = []
    if (dishesResponse.data?.data && dishesResponse.data.data.length > 0) {
      items = await Promise.all(
        dishesResponse.data.data.map(async (orderDish) => {
          try {
            const dishResponse = await axios.get(`${API_CONFIG.baseURL}${API_CONFIG.dish.detail}${orderDish.dishId}`)
            const dish = dishResponse.data?.data
            return {
              id: dish?.id || orderDish.dishId,
              name: dish?.name || orderDish.dishName || '菜品',
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
              name: orderDish.dishName || '菜品',
              quantity: orderDish.quantity,
              price: orderDish.price,
              image: orderDish.dishImage || '',
              optionalIngredients: orderDish.optionalIngredients || [],
              requiredIngredients: orderDish.requiredIngredients || [],
              note: orderDish.note || '',
              unavailable: true
            }
          }
        })
      )
    }

    const status = orderStatusToText(orderData.status)

    // 4. 组装订单数据
    order.value = {
      id: orderData.id,
      orderNo: orderData.id,
      status,
      merchant: merchantName,
      merchantId: orderData.merchantId,
      total: orderData.totalAmount,
      time: formatTime(orderData.createdAt),
      cancelTime: status === 'cancelled' ? formatTime(orderData.updatedAt) : '',
      items: items,
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
      paymentMethod: '平台币余额',
      paymentTime: orderData.paymentTime ? formatTime(orderData.paymentTime) : null,
      remark: orderData.remark || '',
      _raw: orderData
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
    order.value = null
  } finally {
    loading.value = false
    // 订单详情加载完成
    console.log('📋 订单详情加载完成', {
      hasOrder: !!order.value,
      orderStatus: order.value?.status,
      rawStatus: order.value?._raw?.status
    })
  }
}

// 加载评价数据
const loadReview = async () => {
  console.log('🔍 loadReview 函数被调用', {
    hasOrder: !!order.value,
    orderStatus: order.value?.status
  })

  if (!order.value) {
    console.error('❌ 订单数据不存在，无法加载评价')
    return
  }

  if (order.value.status !== 'reviewed') {
    console.error('❌ 订单状态不是"已评价"，无需加载评价数据', {
      currentStatus: order.value.status,
      expectedStatus: 'reviewed'
    })
    return
  }

  reviewLoading.value = true
  try {
    console.log('📋 开始加载订单评价', {
      orderId: orderId.value,
      timestamp: new Date().toISOString()
    })

    const response = await reviewAPI.getReviewByOrderId(orderId.value)

    console.log('📡 API响应数据', {
      status: response.status,
      success: response.data?.success,
      hasData: !!response.data?.data,
      data: response.data?.data
    })

    // 检查响应是否成功且包含数据
    if (response.data?.success && response.data?.data) {
      review.value = response.data.data
      console.log('✅ 评价数据加载成功', {
        reviewId: review.value.id,
        rating: review.value.rating,
        content: review.value.content,
        hasImages: review.value.images?.length || 0,
        hasReplies: review.value.replies?.length || 0
      })
    } else {
      // 评价数据不存在，可能是数据不一致问题
      console.warn('⚠️ 评价数据不存在，订单状态与评价记录不一致', {
        orderId: orderId.value,
        responseMessage: response.data?.message,
        orderStatus: order.value?.status
      })

      // 如果订单状态是已评价但没有评价记录，提示用户
      if (order.value?.status === 'reviewed') {
        console.warn('⚠️ 检测到数据不一致：订单状态为已评价，但没有找到评价记录')
        // 可以选择将订单状态回滚到待评价，或者只是不显示评价卡片
      }
    }
  } catch (error) {
    console.error('❌ 加载评价数据出错', {
      orderId: orderId.value,
      errorMessage: error.message,
      errorStatus: error.response?.status,
      errorData: error.response?.data
    })
    // 不显示错误给用户，只是没有评价而已
  } finally {
    reviewLoading.value = false
    console.log('✅ 评价加载流程结束', {
      hasReview: !!review.value
    })
  }
}

// 组件挂载时加载订单详情
onMounted(() => {
  console.log('🚀 OrderDetail组件挂载', {
    orderId: orderId.value
  })
  loadOrderDetail()

  // 延迟检查订单状态
  setTimeout(() => {
    console.log('⏰ 延迟检查订单状态', {
      hasOrder: !!order.value,
      orderStatus: order.value?.status
    })
  }, 500)
})

// 监听路由变化，当从评价页面返回时重新加载数据
watch(
  () => route.query.refresh,
  (newRefresh) => {
    if (newRefresh) {
      console.log('🔄 检测到刷新参数，重新加载数据', {
        refresh: newRefresh,
        timestamp: new Date().toISOString()
      })
      // 重新加载订单详情
      loadOrderDetail()
    }
  }
)

// 支付订单
const payOrder = async (order) => {
  paying.value = true
  try {
    const response = await paymentApi.payOrder(order.id, getUserId(), 'wallet')

    if (response?.success) {
      ElMessage.success(response.message || '支付成功')
      await loadOrderDetail()
    } else {
      ElMessage.error(response?.message || '支付失败')
    }
  } catch (error) {
    console.error('订单支付失败:', error)
    ElMessage.error(error?.data?.message || error?.response?.data?.message || error?.message || '支付失败')
  } finally {
    paying.value = false
  }
}

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
const goToEvaluate = (orderId, isAdditional = false) => {
  if (isAdditional) {
    router.push({
      path: `/user/home/evaluate-order/${orderId}`,
      query: { type: 'additional' }
    })
  } else {
    router.push(`/user/home/evaluate-order/${orderId}`)
  }
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

// 返回订单列表
const goToOrderList = () => {
  router.push('/user/home/orders')
}

// 获取当前活动步骤（5状态系统）
const getActiveStep = () => {
  if (!order.value) return 0

  const statusStepMap = {
    pending: 0,
    pendingAccept: 1,
    preparing: 2,
    completed: 4,
    cancelled: 0
  }

  return statusStepMap[order.value.status] || 0
}
</script>

<template>
  <div class="order-detail-container">
    <!-- 页面头部 -->
    <AppPageHeader class="page-header" title="订单详情">
      <template #actions>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <!-- 加载状态 -->
    <div v-loading="loading" element-loading-text="加载中..." class="loading-container">
      <!-- 订单详情内容 -->
      <div v-if="order" class="order-detail-content">
        <!-- 订单状态卡片 -->
        <el-card class="status-card" shadow="never">
          <div class="status-summary">
            <div class="status-header">
              <span class="status-label">订单状态</span>
              <el-tag :type="statusTagTypeMap[order.status]" effect="light">
                {{ orderStatusMap[order.status] }}
              </el-tag>
            </div>
            <div class="order-meta">
              <span class="order-meta-label">订单号</span>
              <span class="order-no">{{ order.orderNo }}</span>
            </div>
          </div>

          <!-- 订单进度 -->
          <div v-if="order.status !== 'cancelled'" class="order-progress">
            <el-steps :active="getActiveStep()" finish-status="success" align-center>
              <el-step title="待支付" />
              <el-step title="待接单" />
              <el-step title="制作中" />
              <el-step title="已完成" />
            </el-steps>
          </div>

          <!-- 取消信息 -->
          <div v-else class="cancelled-summary">
            <div class="cancelled-meta">
              <span class="cancelled-label">取消时间</span>
              <span class="cancelled-value">{{ order.cancelTime || order.time }}</span>
            </div>
            <el-button type="default" @click="goToOrderList">返回订单列表</el-button>
          </div>
        </el-card>

        <!-- 商家信息卡片 -->
        <el-card class="merchant-card fade-in-up delay-100" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="var(--info)"><Shop /></el-icon>
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
            </div>
          </div>
        </el-card>

        <!-- 订单商品卡片 -->
        <el-card v-if="order.items && order.items.length > 0" class="items-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单商品</span>
              <el-tag type="info" effect="plain">共 {{ order.itemCount || 0 }} 件商品</el-tag>
            </div>
          </template>

          <div class="items-list">
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
                      v-for="(ing, idx) in item.optionalIngredients"
                      :key="typeof ing === 'string' ? ing : (ing.id || ing.name || idx)"
                    >
                      {{ typeof ing === 'string' ? ing : ing.name }}
                      <span
                        v-if="typeof ing !== 'string' && ing.price"
                        class="ingredient-price"
                        >+¥{{ Number(ing.price).toFixed(2) }}</span
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

        </el-card>

        <!-- 订单金额卡片 -->
        <el-card class="amount-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="var(--brand-500)"><Money /></el-icon>
              <span class="card-title">订单金额</span>
            </div>
          </template>

          <div class="amount-details">
            <div class="amount-row total-row">
              <span class="total-label">订单总额</span>
              <span class="total-value number-scroll">¥{{ order.total.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 支付信息卡片 -->
        <el-card class="payment-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="var(--ink-500)"><Money /></el-icon>
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
              <el-icon :size="20" color="var(--brand-500)"><EditPen /></el-icon>
              <span class="card-title">订单备注</span>
            </div>
          </template>
          <div class="remark-content">{{ order.remark }}</div>
        </el-card>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button
            v-if="order.status === 'pending'"
            type="primary"
            size="large"
            :loading="paying"
            @click="payOrder(order)"
            class="action-btn pay-btn"
          >
            支付
          </el-button>
          <el-button
            v-if="order.status === 'pending' || order.status === 'pendingAccept'"
            type="danger"
            plain
            size="large"
            @click="cancelOrder(order)"
            class="action-btn cancel-btn"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'pendingAccept' || order.status === 'preparing'"
            type="primary"
            plain
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
@import '../../assets/css/nordic-theme.less';

.order-detail-container {
  padding: 0 var(--space-6) var(--space-8);
  background: var(--canvas);
  min-height: calc(100vh - 80px);

  .page-header {
    margin-bottom: var(--space-6);
  }

  .loading-container {
    min-height: 400px;
  }

  .order-detail-content {
    display: flex;
    flex-direction: column;
    gap: var(--space-5);
  }

  // 通用卡片样式
  .el-card {
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-sm);
    transition: box-shadow var(--duration-fast) var(--ease-standard);
    overflow: hidden;

    &:hover {
      box-shadow: var(--shadow-md);
    }

    :deep(.el-card__header) {
      padding: var(--space-4) var(--space-5);
      border-bottom: 1px solid var(--line);
      background: var(--canvas);
    }

    :deep(.el-card__body) {
      padding: var(--space-5);
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: var(--space-3);

    .card-title {
      font-size: var(--text-h3);
      line-height: 24px;
      font-weight: var(--weight-semibold);
      color: var(--ink-900);
      flex: 1;
    }
  }

  // 状态卡片
  .status-card {
    background: var(--surface);
    border: 1px solid var(--line);

    :deep(.el-card__body) {
      padding: var(--space-5);
    }

    .status-summary {
      display: grid;
      grid-template-columns: minmax(0, 1fr) minmax(240px, auto);
      align-items: center;
      gap: var(--space-6);
      padding-bottom: var(--space-5);
      border-bottom: 1px solid var(--line);
    }

    .status-header,
    .order-meta {
      display: flex;
      flex-direction: column;
      gap: var(--space-2);
      min-width: 0;
    }

    .status-label,
    .order-meta-label {
      font-size: var(--text-body-sm);
      color: var(--ink-500);
    }

    .order-meta {
      justify-self: end;
      align-items: flex-end;
      text-align: right;
    }

    .order-no {
      font-family: var(--font-num);
      font-size: var(--text-body);
      font-weight: var(--weight-medium);
      color: var(--ink-700);
      font-variant-numeric: tabular-nums;
      white-space: nowrap;
    }

    :deep(.el-tag) {
      padding: var(--space-1) var(--space-3);
      height: auto;
      align-self: flex-start;
      width: fit-content;
      border: none;
      font-size: var(--text-body);
      line-height: 22px;
      font-weight: var(--weight-medium);
    }

    :deep(.el-tag--warning) {
      color: var(--warning);
      background: var(--warning-bg);
    }

    :deep(.el-tag--info) {
      color: var(--info);
      background: var(--info-bg);
    }

    :deep(.el-tag--success) {
      color: var(--success);
      background: var(--success-bg);
    }

    :deep(.el-tag--danger) {
      color: var(--danger);
      background: var(--danger-bg);
    }

    .order-progress {
      padding-top: var(--space-5);

      :deep(.el-steps) {
        .el-step__title {
          font-size: var(--text-body-sm);
        }
      }
    }

    .cancelled-summary {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--space-4);
      padding-top: var(--space-5);
    }

    .cancelled-meta {
      display: flex;
      align-items: center;
      gap: var(--space-3);
    }

    .cancelled-label {
      font-size: var(--text-body-sm);
      color: var(--ink-500);
    }

    .cancelled-value {
      font-family: var(--font-num);
      font-size: var(--text-body);
      color: var(--ink-700);
      font-variant-numeric: tabular-nums;
    }
  }

  // 商家卡片
  .merchant-card {
    .merchant-details {
      .merchant-name {
        font-size: var(--text-h2);
        line-height: 28px;
        font-weight: var(--weight-semibold);
        color: var(--ink-900);
        margin-bottom: var(--space-2);
      }

      .merchant-meta {
        display: flex;
        align-items: center;
        gap: var(--space-4);
        font-size: var(--text-body);
        color: var(--ink-500);

        .rating {
          color: var(--warning);
          font-weight: var(--weight-medium);
        }
      }
    }
  }

  // 商品卡片
  .items-card {
    .items-list {
      display: flex;
      flex-direction: column;
      gap: var(--space-3);
    }

    .item-row {
      display: flex;
      align-items: stretch;
      padding: var(--space-4);
      background: var(--canvas);
      border-radius: var(--radius-md);
      transition: box-shadow var(--duration-fast) var(--ease-standard);
      position: relative;
      border: 1px solid var(--line);
      min-height: 94px;

      &:hover {
        box-shadow: var(--shadow-sm);
      }

      .item-image {
        width: 70px;
        min-height: 70px;
        height: 70px;
        border-radius: var(--radius-md);
        overflow: visible;
        margin-right: var(--space-4);
        flex-shrink: 0;
        background: var(--info-bg);
        position: relative;
        z-index: 1;
        box-shadow: var(--shadow-sm);
        display: flex;
        align-items: center;
        justify-content: center;
        border: 1px solid var(--line);

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: var(--radius-md);
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
            font-size: 2rem;
            font-weight: var(--weight-semibold);
            color: var(--info);
          }
        }

        .quantity-badge {
          position: absolute;
          top: -4px;
          right: -4px;
          background: var(--danger);
          color: var(--surface);
          font-size: var(--text-caption);
          font-weight: var(--weight-bold);
          min-width: 18px;
          height: 18px;
          padding: 0 5px;
          border-radius: var(--radius-full);
          box-shadow: var(--shadow-sm);
          z-index: 100;
          display: flex;
          align-items: center;
          justify-content: center;
          line-height: 1;
          transition: transform var(--duration-fast) var(--ease-standard);
          pointer-events: none;

          .item-row:hover & {
            transform: scale(1.1) rotate(-3deg);
            box-shadow: var(--shadow-md);
          }

          &.large-number {
            font-size: 9px;
            min-width: 20px;
            padding: 0 4px;
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
          font-size: var(--text-body);
          font-weight: var(--weight-semibold);
          color: var(--ink-900);
          margin-bottom: var(--space-2);
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
            color: @nordic-text-muted;
          }
        }

        .item-ingredients {
          display: flex;
          align-items: flex-start;
          gap: 6px;
          margin-bottom: @nordic-space-sm;
          font-size: @nordic-text-xs;

          .ingredients-label {
            flex-shrink: 0;

            .label-text {
              color: @nordic-text-secondary;
              font-weight: var(--weight-medium);
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
              border-radius: var(--radius-sm);
              font-size: var(--text-caption);
              line-height: 1.4;
              transition: all 0.2s ease;

              &.required {
                background: var(--success);
                color: var(--surface);
                font-weight: var(--weight-medium);
                box-shadow: var(--shadow-sm);
              }

              &.optional {
                background: var(--success-bg);
                color: var(--success);
                border: 1px solid var(--success);
              }

              .ingredient-price {
                font-size: var(--text-caption);
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
          margin-bottom: var(--space-2);
          font-size: var(--text-caption);
          color: var(--warning);
          padding: var(--space-1) var(--space-3);
          background: var(--warning-bg);
          border-radius: var(--radius-sm);
          border: 1px solid var(--warning);
          line-height: 1.5;

          .el-icon {
            font-size: var(--text-body-sm);
            color: var(--warning);
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
          gap: var(--space-4);
          margin-top: auto;
          flex-shrink: 0;

          .unit-price {
            font-size: var(--text-body-sm);
            color: var(--ink-500);
          }

          .total-price {
            font-family: var(--font-num);
            font-size: var(--text-h3);
            font-weight: var(--weight-bold);
            color: var(--ink-900);
            font-variant-numeric: tabular-nums;
          }
        }
      }
    }
  }

  // 金额卡片
  .amount-card {
    .amount-details {
      .amount-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--space-2) 0;

        .amount-label {
          font-size: var(--text-body);
          color: var(--ink-500);
        }

        .amount-value {
          font-family: var(--font-num);
          font-size: var(--text-body);
          color: var(--ink-900);
          font-weight: var(--weight-medium);
          font-variant-numeric: tabular-nums;
        }

        &.total-row {
          padding-top: var(--space-4);
          border-top: 1px solid var(--line);

          .total-label {
            font-size: var(--text-h3);
            font-weight: var(--weight-semibold);
            color: var(--ink-900);
          }

          .total-value {
            font-family: var(--font-num);
            font-size: var(--text-h1);
            font-weight: var(--weight-bold);
            color: var(--ink-900);
            font-variant-numeric: tabular-nums;
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
      gap: var(--space-2);

      .payment-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--space-2) 0;

        .payment-label {
          font-size: var(--text-body);
          color: var(--ink-500);
        }

        .payment-value {
          font-family: var(--font-num);
          font-size: var(--text-body);
          color: var(--ink-700);
          font-weight: var(--weight-medium);
          font-variant-numeric: tabular-nums;
        }
      }
    }
  }

  // 备注卡片
  .remark-card {
    .remark-content {
      font-size: @nordic-text-base;
      color: @nordic-text-secondary;
      line-height: 1.6;
      padding: @nordic-space-md;
      background: fade(@nordic-bg, 60%);
      border-radius: @nordic-radius-sm;
    }
  }

  // 评价卡片
  .review-card {
    .review-content {
      .review-rating {
        display: flex;
        align-items: center;
        gap: @nordic-space-md;
        padding: @nordic-space-md 0;
        border-bottom: 1px solid @nordic-divider;
        margin-bottom: @nordic-space-md;

        .rating-label {
          font-size: 15px;
          font-weight: 600;
          color: @nordic-text-secondary;
        }

        .rating-score {
          font-size: @nordic-text-lg;
          font-weight: 700;
          color: @nordic-yellow;
          margin-left: @nordic-space-sm;
        }

        :deep(.el-rate) {
          .el-rate__icon {
            font-size: 1.714rem;
            margin-right: 4px;
          }
        }
      }

      .review-text {
        font-size: 15px;
        color: @nordic-text;
        line-height: 1.8;
        padding: @nordic-space-md;
        background: fade(@nordic-bg, 60%);
        border-radius: @nordic-radius-sm;
        margin-bottom: @nordic-space-md;
      }

      .review-images {
        display: flex;
        gap: @nordic-space-md;
        flex-wrap: wrap;
        margin-bottom: @nordic-space-xl;

        .review-image {
          width: 100px;
          height: 100px;
          border-radius: @nordic-radius-sm;
          overflow: hidden;
          border: 1px solid @nordic-border;
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 4px 12px @nordic-shadow-hover;
          }

          :deep(.el-image__inner) {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }

          .image-error {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background: @nordic-bg;
            color: @nordic-text-muted;
            font-size: 2.286rem;
          }
        }
      }

      .review-replies {
        display: flex;
        flex-direction: column;
        gap: @nordic-space-md;
        margin-top: @nordic-space-md;
        padding-top: @nordic-space-md;
        border-top: 1px dashed @nordic-border;

        .reply-item {
          padding: @nordic-space-md;
          border-radius: @nordic-radius-sm;
          transition: all 0.3s ease;

          &.merchant-reply {
            background: linear-gradient(135deg, @nordic-green-light 0%, fade(@nordic-green-light, 60%) 100%);
            border-left: 3px solid @nordic-green;
          }

          &.user-reply {
            background: linear-gradient(135deg, @nordic-yellow-light 0%, fade(@nordic-yellow-light, 60%) 100%);
            border-left: 3px solid @nordic-yellow;
          }

          &:hover {
            transform: translateX(4px);
            box-shadow: 0 2px 8px @nordic-shadow;
          }

          .reply-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: @nordic-space-sm;

            .reply-type {
              display: flex;
              gap: @nordic-space-sm;
            }

            .reply-time {
              font-size: @nordic-text-xs;
              color: @nordic-text-muted;
            }
          }

          .reply-content {
            font-size: @nordic-text-base;
            color: @nordic-text-secondary;
            line-height: 1.6;
          }
        }
      }
    }
  }

  // 操作按钮
  .order-actions {
    display: flex;
    justify-content: center;
    gap: var(--space-3);
    padding: var(--space-5) 0;

    .action-btn {
      min-width: 160px;
      height: 44px;
      font-size: var(--text-body);
      font-weight: var(--weight-semibold);
      border-radius: var(--radius-md);
      transition:
        color var(--duration-fast) var(--ease-standard),
        border-color var(--duration-fast) var(--ease-standard),
        background-color var(--duration-fast) var(--ease-standard);
      box-shadow: none;

      &:hover {
        transform: none;
      }
    }
  }
}

// 响应式设计
@media (max-width: @nordic-breakpoint-md) {
  .order-detail-container {
    padding: 0 var(--space-4) var(--space-8);

    .page-header {
      margin-bottom: var(--space-5);
    }

    .order-detail-content {
      gap: var(--space-4);
    }

    .el-card {
      border-radius: var(--radius-lg);

      :deep(.el-card__body) {
        padding: var(--space-4);
      }

      :deep(.el-card__header) {
        padding: var(--space-3) var(--space-4);
      }
    }

    .status-card {
      :deep(.el-card__body) {
        padding: var(--space-5);
      }

      .status-summary {
        grid-template-columns: 1fr;
        gap: var(--space-4);
      }

      .order-meta {
        justify-self: start;
        align-items: flex-start;
        text-align: left;
      }

      .cancelled-summary {
        flex-direction: column;
        align-items: flex-start;
      }

      .order-progress {
        :deep(.el-steps) {
          .el-step__title {
            font-size: var(--text-body-sm) !important;
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
        padding: @nordic-space-md;
        min-height: 80px;
        border-radius: @nordic-radius-md;

        .item-image {
          width: 56px;
          min-height: 56px;
          height: 56px;
          margin-right: @nordic-space-md;
          border-radius: @nordic-radius-sm;

          img {
            border-radius: @nordic-radius-sm;
          }

          .quantity-badge {
            min-width: 16px;
            height: 16px;
            font-size: 10px;
            padding: 0 4px;
            top: -3px;
            right: -3px;
            border-radius: @nordic-radius-sm;

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
            font-size: @nordic-text-base;
            margin-bottom: 6px;
          }

          .item-ingredients {
            font-size: @nordic-text-xs;
            margin-bottom: 6px;
            gap: 5px;

            .ingredients-list {
              gap: 4px;

              .ingredient-tag {
                font-size: 10px;
                padding: 2px 6px;
                border-radius: @nordic-radius-xs;
              }
            }
          }

          .item-note {
            font-size: @nordic-text-xs;
            padding: 5px @nordic-space-sm;
            margin-bottom: 6px;
            border-radius: @nordic-radius-sm;

            .el-icon {
              font-size: @nordic-text-xs;
            }
          }

          .item-price-detail {
            flex-direction: column;
            align-items: flex-start;
            gap: 4px;

            .unit-price {
              font-size: @nordic-text-xs;
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
      gap: @nordic-space-2sm;
      padding: @nordic-space-md 0;

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
