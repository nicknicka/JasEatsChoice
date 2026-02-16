<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { reviewAPI } from '../../api/review.js'
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

// 评价相关
const review = ref(null)
const reviewLoading = ref(false)

// 订单状态映射（与 orderStatus.js 保持一致）
const orderStatusMap = {
  all: '全部订单',
  pending: '待支付',
  pendingAccept: '待接单',
  processing: '进行中',
  pendingComment: '待评价',
  delivered: '已上菜',
  reviewed: '已评价',
  cancelled: '已取消'
}

// 订单状态标签样式映射（与 orderStatus.js 保持一致）
const statusTagTypeMap = {
  pending: 'info',
  pendingAccept: 'warning',
  processing: 'primary',
  pendingComment: 'info',
  delivered: 'success',
  reviewed: 'success',
  cancelled: 'danger'
}

// 将后端状态码转换为前端状态文本
const orderStatusToText = (statusCode) => {
  const statusMap = {
    0: 'pending', // 待支付
    1: 'pendingAccept', // 待接单
    2: 'processing', // 备菜中
    3: 'processing', // 烹饪中
    4: 'processing', // 待上菜
    5: 'delivered', // 已上菜
    6: 'cancelled', // 已取消
    7: 'pendingComment', // 待评价
    8: 'reviewed' // 已评价
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

    // 4. 组装订单数据
    order.value = {
      id: orderData.id,
      orderNo: orderData.id,
      status: orderStatusToText(orderData.status),
      merchant: merchantName,
      merchantId: orderData.merchantId,
      total: orderData.totalAmount,
      time: formatTime(orderData.createTime),
      items: items,
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
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
    // 订单详情加载完成后，如果订单已评价，则加载评价数据
    console.log('📋 订单详情加载完成，检查是否需要加载评价', {
      hasOrder: !!order.value,
      orderStatus: order.value?.status,
      rawStatus: order.value?._raw?.status,
      shouldLoadReview: order.value && order.value.status === 'reviewed'
    })

    if (order.value && order.value.status === 'reviewed') {
      await loadReview()
    }
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

  // 延迟检查，确保订单状态更新后再尝试加载评价
  setTimeout(() => {
    console.log('⏰ 延迟检查订单状态', {
      hasOrder: !!order.value,
      orderStatus: order.value?.status,
      orderStatusText: orderStatusToText(order.value?.status)
    })

    if (order.value && order.value.status === 'reviewed' && !review.value) {
      console.log('📝 延迟加载评价数据')
      loadReview()
    }
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
      // 如果订单已评价，重新加载评价数据
      if (order.value?.status === 'reviewed') {
        loadReview()
      }
    }
  }
)

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

// 获取当前活动步骤
const getActiveStep = () => {
  if (!order.value) return 0

  const statusStepMap = {
    pending: 0,
    pendingAccept: 1,
    processing: 2,
    delivered: 3,
    pendingComment: 4,
    reviewed: 4,
    cancelled: 2
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
              <el-step title="已上菜" />
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

        <!-- 评价卡片 -->
        <el-card v-if="review" class="review-card" shadow="hover">
          <!-- 调试信息 -->
          <div style="display: none;">
            <pre>{{ JSON.stringify({
              hasReview: !!review,
              reviewId: review?.id,
              rating: review?.rating,
              hasContent: !!review?.content,
              hasImages: review?.images?.length > 0,
              hasReplies: review?.replies?.length > 0,
              repliesCount: review?.replies?.length
            }, null, 2) }}</pre>
          </div>
          <!-- 调试信息结束 -->
          <template #header>
            <div class="card-header">
              <el-icon :size="20" color="#f7ba2a"><Star /></el-icon>
              <span class="card-title">我的评价</span>
            </div>
          </template>

          <div v-loading="reviewLoading" class="review-content">
            <!-- 评分 -->
            <div class="review-rating">
              <span class="rating-label">评分：</span>
              <el-rate
                v-model="review.rating"
                disabled
                :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                size="large"
              />
              <span class="rating-score">{{ review.rating }}分</span>
            </div>

            <!-- 评价内容 -->
            <div v-if="review.content" class="review-text">
              {{ review.content }}
            </div>

            <!-- 评价图片 -->
            <div v-if="review.images && review.images.length > 0" class="review-images">
              <el-image
                v-for="(img, index) in review.images"
                :key="index"
                :src="img"
                :preview-src-list="review.images"
                :initial-index="index"
                fit="cover"
                class="review-image"
                lazy
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>

            <!-- 追评和商家回复 -->
            <div v-if="review.replies && review.replies.length > 0" class="review-replies">
              <div
                v-for="reply in review.replies"
                :key="reply.id"
                class="reply-item"
                :class="{ 'merchant-reply': reply.isAdditional === 0, 'user-reply': reply.isAdditional === 1 }"
              >
                <div class="reply-header">
                  <div class="reply-type">
                    <el-tag v-if="reply.isAdditional === 0" type="success" size="small">
                      商家回复
                    </el-tag>
                    <el-tag v-else-if="reply.isAdditional === 1" type="warning" size="small">
                      追加评价
                    </el-tag>
                  </div>
                  <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
              </div>
            </div>
          </div>
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
            v-if="order.status === 'reviewed'"
            type="warning"
            size="large"
            @click="goToEvaluate(order.id, true)"
            class="action-btn additional-review-btn"
          >
            <el-icon><EditPen /></el-icon>
            追加评价
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
    font-size: 1.714rem /* 原值: 24px */;
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
      font-size: 1.143rem /* 原值: 16px */;
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
        font-size: 1.143rem /* 原值: 16px */;
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
        font-size: 1.286rem /* 原值: 18px */;
        font-weight: 600;
        color: #2c3e50;
        margin-bottom: 8px;
      }

      .merchant-meta {
        display: flex;
        align-items: center;
        gap: 16px;
        font-size: 1rem /* 原值: 14px */;
        color: #7f8c8d;

        .rating {
          color: #e6a23c;
          font-weight: 500;
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
            font-size: 2rem /* 原值: 28px */;
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
          font-size: 0.75rem /* 原值: 11px */;
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
          font-size: 1.071rem /* 原值: 15px */;
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
          font-size: 0.857rem /* 原值: 12px */;

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
              font-size: 0.75rem /* 原值: 11px */;
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
          font-size: 0.857rem /* 原值: 12px */;
          color: #c4873a;
          padding: 6px 10px;
          background: rgba(253, 246, 236, 0.8);
          border-radius: 8px;
          border: 1px solid rgba(245, 218, 177, 0.5);
          line-height: 1.5;
          box-shadow: 0 1px 4px rgba(230, 162, 60, 0.08);

          .el-icon {
            font-size: 0.929rem /* 原值: 13px */;
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
            font-size: 0.857rem /* 原值: 12px */;
            color: #94a3b8;
          }

          .total-price {
            font-size: 1.143rem /* 原值: 16px */;
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
      font-size: 1rem /* 原值: 14px */;
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
          font-size: 1rem /* 原值: 14px */;
          color: #7f8c8d;
        }

        .amount-value {
          font-size: 1.071rem /* 原值: 15px */;
          color: #2c3e50;
          font-weight: 500;
        }

        &.total-row {
          padding-top: 16px;

          .total-label {
            font-size: 1.143rem /* 原值: 16px */;
            font-weight: 600;
            color: #2c3e50;
          }

          .total-value {
            font-size: 1.714rem /* 原值: 24px */;
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
          font-size: 1rem /* 原值: 14px */;
          color: #7f8c8d;
        }

        .payment-value {
          font-size: 1rem /* 原值: 14px */;
          color: #2c3e50;
          font-weight: 500;
        }
      }
    }
  }

  // 备注卡片
  .remark-card {
    .remark-content {
      font-size: 1rem /* 原值: 14px */;
      color: #606266;
      line-height: 1.6;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 8px;
    }
  }

  // 评价卡片
  .review-card {
    .review-content {
      .review-rating {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px 0;
        border-bottom: 1px solid rgba(0, 0, 0, 0.06);
        margin-bottom: 16px;

        .rating-label {
          font-size: 1.071rem /* 原值: 15px */;
          font-weight: 600;
          color: #606266;
        }

        .rating-score {
          font-size: 1.286rem /* 原值: 18px */;
          font-weight: 700;
          color: #f7ba2a;
          margin-left: 8px;
        }

        :deep(.el-rate) {
          .el-rate__icon {
            font-size: 1.714rem /* 原值: 24px */;
            margin-right: 4px;
          }
        }
      }

      .review-text {
        font-size: 1.071rem /* 原值: 15px */;
        color: #303133;
        line-height: 1.8;
        padding: 12px 16px;
        background: #f8f9fa;
        border-radius: 8px;
        margin-bottom: 16px;
      }

      .review-images {
        display: flex;
        gap: 12px;
        flex-wrap: wrap;
        margin-bottom: 20px;

        .review-image {
          width: 100px;
          height: 100px;
          border-radius: 8px;
          overflow: hidden;
          border: 1px solid rgba(0, 0, 0, 0.08);
          cursor: pointer;
          transition: all 0.3s ease;

          &:hover {
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
            background: #f5f7fa;
            color: #c0c4cc;
            font-size: 2.286rem /* 原值: 32px */;
          }
        }
      }

      .review-replies {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px dashed rgba(0, 0, 0, 0.1);

        .reply-item {
          padding: 12px 16px;
          border-radius: 8px;
          transition: all 0.3s ease;

          &.merchant-reply {
            background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
            border-left: 3px solid #10b981;
          }

          &.user-reply {
            background: linear-gradient(135deg, #fffbeb 0%, #fef3c7 100%);
            border-left: 3px solid #f59e0b;
          }

          &:hover {
            transform: translateX(4px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
          }

          .reply-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;

            .reply-type {
              display: flex;
              gap: 8px;
            }

            .reply-time {
              font-size: 0.857rem /* 原值: 12px */;
              color: #909399;
            }
          }

          .reply-content {
            font-size: 1rem /* 原值: 14px */;
            color: #606266;
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
    gap: 16px;
    padding: 20px 0;

    .action-btn {
      min-width: 160px;
      height: 48px;
      font-size: 1.143rem /* 原值: 16px */;
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
      font-size: 1.429rem /* 原值: 20px */;
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
            font-size: 0.857rem /* 原值: 12px */ !important;
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
            font-size: 1rem /* 原值: 14px */;
            margin-bottom: 6px;
          }

          .item-ingredients {
            font-size: 0.75rem /* 原值: 11px */;
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
            font-size: 0.75rem /* 原值: 11px */;
            padding: 5px 8px;
            margin-bottom: 6px;
            border-radius: 6px;

            .el-icon {
              font-size: 0.857rem /* 原值: 12px */;
            }
          }

          .item-price-detail {
            flex-direction: column;
            align-items: flex-start;
            gap: 4px;

            .unit-price {
              font-size: 0.75rem /* 原值: 11px */;
            }

            .total-price {
              font-size: 1.071rem /* 原值: 15px */;
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
        font-size: 1.071rem /* 原值: 15px */;
      }
    }
  }
}
</style>
