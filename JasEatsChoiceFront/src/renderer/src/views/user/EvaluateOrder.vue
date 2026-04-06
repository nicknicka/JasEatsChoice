<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import { reviewAPI } from '../../api/review.js'

const route = useRoute()
const router = useRouter()
const orderId = ref(route.params.id)
const order = ref(null)
const loading = ref(true)
const isAdditionalReview = ref(false) // 是否为追评
const existingReview = ref(null) // 已存在的评价

// 评价数据
const rating = ref(0)
const tags = ref([])
const content = ref('')
const customTagInput = ref('')

// 计算页面标题
const pageTitle = computed(() => {
  return isAdditionalReview.value ? '追加评价' : '评价订单'
})

// 评价标签选项
const tagOptions = [
  '口味赞',
  '分量足',
  '上菜快',
  '服务好',
  '环境佳',
  '性价比高',
  '食材新鲜',
  '包装精美'
]

// 加载订单详情
const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderResponse = await axios.get(
      API_CONFIG.baseURL + API_CONFIG.order.detail + orderId.value
    )

    if (!orderResponse.data?.data) {
      throw new Error('订单不存在')
    }

    const orderData = orderResponse.data.data

    // 获取商家名称
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

    // 获取订单菜品信息
    const dishesResponse = await axios.get(`${API_CONFIG.baseURL}/v1/orders/${orderData.id}/dishes`)

    // 获取菜品详情
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
              image: dish?.image || ''
            }
          } catch (error) {
            console.error(`获取菜品${orderDish.dishId}详情失败:`, error)
            return {
              id: orderDish.dishId,
              name: orderDish.dishName || '菜品',
              quantity: orderDish.quantity,
              price: orderDish.price,
              image: orderDish.dishImage || ''
            }
          }
        })
      )
    }

    order.value = {
      id: orderData.id,
      orderNo: orderData.id,
      merchant: merchantName,
      merchantId: orderData.merchantId,
      total: orderData.totalAmount,
      items: items
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }

  // 检查是否为追评模式
  if (route.query.type === 'additional') {
    isAdditionalReview.value = true
    await loadExistingReview()
  }
}

// 加载已存在的评价
const loadExistingReview = async () => {
  try {
    console.log('📋 加载已存在的评价', {
      orderId: orderId.value,
      timestamp: new Date().toISOString()
    })

    const response = await reviewAPI.getReviewByOrderId(orderId.value)

    if (response.data?.data) {
      existingReview.value = response.data.data
      console.log('✅ 找到已存在的评价', {
        reviewId: existingReview.value.id,
        rating: existingReview.value.rating,
        timestamp: new Date().toISOString()
      })
    } else {
      console.log('⚠️ 未找到已有的评价数据，可能是后端API未实现')
      // 不显示警告，允许用户继续追加评价
    }
  } catch (error) {
    console.error('加载已存在的评价失败:', error)
    // 如果是404错误（API不存在或评价不存在），只记录日志，不中断流程
    // 允许用户继续追加评价，只是不显示已有评价内容
    if (error.response?.status === 404) {
      console.log('⚠️ 评价数据API不存在或评价记录不存在，允许用户继续追加评价')
      // 不显示警告消息，允许用户继续追加评价
    } else {
      // 其他错误也只记录日志，允许继续
      console.error('⚠️ 加载评价数据出错，但不影响追加评价功能')
    }
  }
}

// 切换标签
const toggleTag = (tag) => {
  const index = tags.value.indexOf(tag)
  if (index > -1) {
    tags.value.splice(index, 1)
  } else {
    tags.value.push(tag)
  }
}

// 添加自定义标签
const addCustomTag = () => {
  const tag = customTagInput.value.trim()
  if (!tag) {
    ElMessage.warning('请输入标签内容')
    return
  }

  if (tags.value.includes(tag)) {
    ElMessage.warning('该标签已存在')
    return
  }

  if (tags.value.length >= 10) {
    ElMessage.warning('最多只能添加10个标签')
    return
  }

  tags.value.push(tag)
  customTagInput.value = ''
  ElMessage.success('标签添加成功')
}

// 处理回车添加标签
const handleTagInputKeydown = (e) => {
  if (e.key === 'Enter') {
    e.preventDefault()
    addCustomTag()
  }
}

// 移除标签
const removeTag = (index) => {
  tags.value.splice(index, 1)
}

// 获取评分文本
const getRatingText = (score) => {
  if (score === 0) return '请评分'
  if (score <= 0.5) return '非常差'
  if (score <= 1.5) return '差'
  if (score <= 2.5) return '一般'
  if (score <= 3.5) return '好'
  if (score <= 4.5) return '很好'
  return '非常好'
}

// 提交评价
const submitReview = async () => {
  // 追评模式只需要填写内容
  if (isAdditionalReview.value) {
    if (!content.value.trim()) {
      ElMessage.warning('请填写追评内容')
      return
    }

    try {
      console.log('📝 开始提交追评', {
        orderId: order.value.id,
        reviewId: existingReview.value?.id,
        content: content.value,
        timestamp: new Date().toISOString()
      })

      // 调用追评接口
      const additionalReviewData = {
        content: content.value.trim(),
        images: [] // 暂不支持图片上传
      }

      // 调用后端API提交追评
      const reviewResponse = await reviewAPI.addAdditionalReview(existingReview.value.id, additionalReviewData)

      console.log('✅ 追评提交成功', {
        reviewId: existingReview.value?.id,
        responseData: reviewResponse.data,
        timestamp: new Date().toISOString()
      })

      ElMessage.success('追评提交成功')
      // 返回订单详情页，方便用户立即看到新追评
      setTimeout(() => {
        router.push(`/user/home/order-detail/${orderId.value}?refresh=${Date.now()}`)
      }, 1500)
    } catch (error) {
      console.error('❌ 提交追评失败:', {
        error: error.message,
        reviewId: existingReview.value?.id,
        timestamp: new Date().toISOString()
      })
      ElMessage.error(error.response?.data?.message || '提交追评失败，请稍后重试')
    }
    return
  }

  // 初始评价模式
  if (rating.value === 0) {
    ElMessage.warning('请选择评分')
    return
  }

  if (tags.value.length === 0 && !content.value.trim()) {
    ElMessage.warning('请选择标签或填写评价内容')
    return
  }

  try {
    console.log('📝 开始提交评价', {
      orderId: order.value.id,
      merchantId: order.value.merchantId,
      rating: rating.value,
      tags: tags.value,
      content: content.value,
      timestamp: new Date().toISOString()
    })

    // 调用评价提交接口
    const reviewData = {
      orderId: order.value.id,
      merchantId: order.value.merchantId,
      rating: rating.value,
      tags: tags.value,
      content: content.value.trim(),
      images: [] // 暂不支持图片上传
    }

    console.log('📡 提交评价数据', reviewData)

    const reviewResponse = await reviewAPI.submitReview(reviewData)

    console.log('📡 评价提交响应', {
      status: reviewResponse.status,
      success: reviewResponse.data?.success,
      data: reviewResponse.data?.data
    })

    if (reviewResponse.data?.success) {
      console.log('✅ 评价提交成功', {
        reviewId: reviewResponse.data?.data?.id,
        orderId: order.value.id,
        timestamp: new Date().toISOString()
      })

      ElMessage.success('评价提交成功')

      // 返回订单列表并刷新
      setTimeout(() => {
        router.push('/user/home/orders?refresh=' + Date.now())
      }, 1500)
    } else {
      throw new Error(reviewResponse.data?.message || '评价提交失败')
    }
  } catch (error) {
    console.error('❌ 提交评价失败:', {
      error: error.message,
      orderId: order.value?.id,
      errorResponse: error.response?.data,
      timestamp: new Date().toISOString()
    })
    ElMessage.error(error.response?.data?.message || '提交评价失败，请稍后重试')
  }
}

// 组件挂载时加载订单详情
onMounted(() => {
  loadOrderDetail()
})
</script>

<template>
  <div class="evaluate-order-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <CommonBackButton />
      <h2 class="page-title">{{ pageTitle }}</h2>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" element-loading-text="加载中..." class="content-container">
      <!-- 评价内容 -->
      <div v-if="order" class="evaluate-content">
        <!-- 订单信息卡片 -->
        <el-card class="order-info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单信息</span>
              <span class="order-no">订单号: {{ order.orderNo }}</span>
            </div>
          </template>

          <div class="merchant-info">
            <div class="merchant-name">{{ order.merchant }}</div>
            <div class="order-items">
              <div
                v-for="item in order.items"
                :key="item.id"
                class="order-item"
              >
                <img
                  v-if="item.image"
                  :src="item.image"
                  :alt="item.name"
                  class="item-image"
                />
                <div v-else class="item-image-placeholder">
                  <span>{{ item.name?.charAt(0) || '菜' }}</span>
                </div>
                <div class="item-details">
                  <div class="item-name">{{ item.name }}</div>
                  <div class="item-quantity">x{{ item.quantity }}</div>
                  <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
                </div>
              </div>
            </div>
            <div class="order-total">
              <span class="total-label">订单总额:</span>
              <span class="total-amount">¥{{ order.total.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 评分卡片（追评时不显示） -->
        <el-card v-if="!isAdditionalReview" class="rating-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">评分</span>
            </div>
          </template>

          <div class="rating-section">
            <div class="rating-label">请为本次用餐体验评分</div>
            <div class="rating-wrapper">
              <div class="rating-stars">
                <el-rate
                  v-model="rating"
                  :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                  show-text
                  :texts="['非常差', '差', '一般', '好', '非常好']"
                  size="large"
                  allow-half
                />
              </div>
              <div class="rating-text" :class="{ 'no-rating': rating === 0 }">
                {{ getRatingText(rating) }}
              </div>
            </div>
          </div>
        </el-card>

        <!-- 已有评价展示（追评时显示） -->
        <el-card v-if="isAdditionalReview && existingReview" class="existing-review-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">我的评价</span>
            </div>
          </template>

          <div class="existing-review-content">
            <div class="review-rating">
              <el-rate
                v-model="existingReview.rating"
                disabled
                :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                size="large"
              />
            </div>
            <div v-if="existingReview.tags && existingReview.tags.length > 0" class="review-tags">
              <el-tag
                v-for="tag in existingReview.tags"
                :key="tag"
                type="warning"
                effect="plain"
                class="review-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
            <div v-if="existingReview.content" class="review-content">
              {{ existingReview.content }}
            </div>
          </div>
        </el-card>

        <!-- 标签卡片（追评时不显示） -->
        <el-card v-if="!isAdditionalReview" class="tags-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">标签（可多选）</span>
            </div>
          </template>

          <div class="tags-section">
            <!-- 预设标签 -->
            <div class="preset-tags">
              <el-tag
                v-for="tag in tagOptions"
                :key="tag"
                :type="tags.includes(tag) ? 'warning' : 'info'"
                :effect="tags.includes(tag) ? 'dark' : 'plain'"
                class="tag-item"
                @click="toggleTag(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>

            <!-- 自定义标签输入 -->
            <div class="custom-tag-input">
              <el-input
                v-model="customTagInput"
                placeholder="自定义标签"
                maxlength="10"
                show-word-limit
                class="tag-input"
                @keydown="handleTagInputKeydown"
              />
              <el-button
                type="primary"
                size="default"
                class="add-tag-btn"
                @click="addCustomTag"
              >
                添加
              </el-button>
            </div>

            <!-- 已选标签显示 -->
            <div v-if="tags.length > 0" class="selected-tags">
              <div class="selected-tags-label">已选标签：</div>
              <div class="selected-tags-list">
                <el-tag
                  v-for="(tag, index) in tags"
                  :key="`selected-${tag}`"
                  closable
                  type="warning"
                  effect="dark"
                  @close="removeTag(index)"
                  class="selected-tag-item"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 评价内容卡片 -->
        <el-card class="content-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ isAdditionalReview ? '追评内容' : '评价内容' }}</span>
            </div>
          </template>

          <el-input
            v-model="content"
            type="textarea"
            :rows="5"
            :placeholder="isAdditionalReview ? '继续分享您的用餐体验...' : '分享您的用餐体验，说说您的感受...'"
            maxlength="500"
            show-word-limit
            class="review-textarea"
          />
        </el-card>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            @click="submitReview"
          >
            {{ isAdditionalReview ? '提交追评' : '提交评价' }}
          </el-button>
        </div>
      </div>

      <!-- 空数据提示 -->
      <el-empty v-else-if="!loading" description="订单信息不存在" />
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

.evaluate-order-container {
  padding: 0 20px 20px 20px;
  background: @nordic-bg;
  min-height: calc(100vh - 80px);

  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px 20px;
    background: @nordic-surface;
    border-radius: @nordic-radius-lg;
    box-shadow: 0 2px 8px @nordic-shadow;
    border: 1px solid @nordic-border;
  }

  .page-title {
    font-size: 1.714rem;
    margin: 0;
    margin-left: 15px;
    color: @nordic-text;
    font-weight: 600;
  }

  .content-container {
    min-height: 400px;
  }

  .evaluate-content {
    display: flex;
    flex-direction: column;
    gap: @nordic-space-md;
  }

  // 通用卡片样式
  .el-card {
    background: @nordic-surface;
    border-radius: @nordic-radius-lg;
    border: 1px solid @nordic-border;
    box-shadow: 0 2px 12px @nordic-shadow;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px @nordic-shadow-hover;
      border-color: @nordic-accent-light;
    }

    :deep(.el-card__header) {
      padding: @nordic-space-md 20px;
      border-bottom: 1px solid @nordic-divider;
      background: linear-gradient(to bottom, fade(@nordic-bg, 60%) 0%, @nordic-surface 100%);
    }

    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .card-title {
      font-size: 1.143rem;
      font-weight: 600;
      color: @nordic-text;
    }

    .order-no {
      font-size: 1rem;
      color: @nordic-text-secondary;
    }
  }

  // 订单信息卡片
  .order-info-card {
    .merchant-info {
      .merchant-name {
        font-size: 1.286rem;
        font-weight: 600;
        color: @nordic-text;
        margin-bottom: @nordic-space-md;
      }

      .order-items {
        display: flex;
        flex-direction: column;
        gap: 12px;
        margin-bottom: @nordic-space-md;
        padding-bottom: @nordic-space-md;
        border-bottom: 1px solid @nordic-divider;
      }

      .order-item {
        display: flex;
        align-items: center;
        gap: 12px;

        .item-image {
          width: 60px;
          height: 60px;
          border-radius: @nordic-radius-sm;
          object-fit: cover;
        }

        .item-image-placeholder {
          width: 60px;
          height: 60px;
          border-radius: @nordic-radius-sm;
          background: linear-gradient(135deg, @nordic-blue-light 0%, fade(@nordic-blue, 30%) 100%);
          display: flex;
          align-items: center;
          justify-content: center;

          span {
            font-size: 1.714rem;
            font-weight: 600;
            color: @nordic-blue;
          }
        }

        .item-details {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 12px;

          .item-name {
            flex: 1;
            font-size: 1.071rem;
            font-weight: 500;
            color: @nordic-text;
          }

          .item-quantity {
            font-size: 1rem;
            color: @nordic-text-secondary;
          }

          .item-price {
            font-size: 1.143rem;
            font-weight: 600;
            color: @nordic-red;
          }
        }
      }

      .order-total {
        display: flex;
        justify-content: flex-end;
        align-items: center;
        gap: 12px;

        .total-label {
          font-size: 1.071rem;
          color: @nordic-text-secondary;
        }

        .total-amount {
          font-size: 22px;
          font-weight: 700;
          color: @nordic-red;
        }
      }
    }
  }

  // 评分卡片
  .rating-card {
    .rating-section {
      text-align: center;

      .rating-label {
        font-size: 1.143rem;
        color: @nordic-text;
        margin-bottom: 20px;
      }

      .rating-wrapper {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;

        .rating-stars {
          :deep(.el-rate) {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 0;

            .el-rate__item {
              display: inline-flex;
              align-items: center;
              margin-right: 6px;
            }

            .el-rate__icon {
              font-size: 2.857rem;
              display: inline-block;
              vertical-align: middle;
              line-height: 1;
              position: relative;
            }

            // 修复半颗星的显示
            .el-rate__icon .el-rate__decimal {
              position: absolute;
              left: 0;
              top: 0;
              display: inline-block;
              overflow: hidden;
            }

            // 隐藏默认的文本显示
            .el-rate__text {
              display: none;
            }
          }
        }

        .rating-text {
          font-size: 1.286rem;
          font-weight: 600;
          color: @nordic-yellow;
          min-width: 80px;
          text-align: center;
          line-height: 1.5;
          transition: color @nordic-transition-base ease;

          &.no-rating {
            color: @nordic-text-muted;
            font-weight: 400;
          }
        }
      }
    }
  }

  // 标签卡片
  .tags-card {
    .tags-section {
      display: flex;
      flex-direction: column;
      gap: @nordic-space-md;

      // 预设标签容器
      .preset-tags {
        display: flex;
        flex-wrap: wrap;
        gap: @nordic-space-2sm;

        .tag-item {
          cursor: pointer;
          transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);

          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px @nordic-shadow-hover;
          }
        }
      }

      // 自定义标签输入区域
      .custom-tag-input {
        display: flex;
        gap: @nordic-space-2sm;
        width: 100%;

        .tag-input {
          flex: 1;

          :deep(.el-input__wrapper) {
            border-radius: @nordic-radius-sm;
            transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);

            &:hover {
              border-color: @nordic-blue;
            }

            &.is-focus {
              border-color: @nordic-blue;
              box-shadow: 0 0 0 2px fade(@nordic-blue, 10%);
            }
          }
        }

        .add-tag-btn {
          border-radius: @nordic-radius-sm;
          background: linear-gradient(135deg, @nordic-blue 0%, fade(@nordic-blue, 80%) 100%);
          border: none;
          box-shadow: 0 2px 8px fade(@nordic-blue, 25%);
          transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);
          white-space: nowrap;

          &:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px fade(@nordic-blue, 35%);
          }

          &:active {
            transform: translateY(0);
          }
        }
      }

      // 已选标签显示区域
      .selected-tags {
        padding: 12px;
        background: @nordic-yellow-light;
        border-radius: @nordic-radius-sm;
        border: 1px solid fade(@nordic-yellow, 30%);

        .selected-tags-label {
          font-size: 1rem;
          color: @nordic-yellow-dark;
          font-weight: 500;
          margin-bottom: @nordic-space-sm;
        }

        .selected-tags-list {
          display: flex;
          flex-wrap: wrap;
          gap: @nordic-space-sm;

          .selected-tag-item {
            :deep(.el-tag__close) {
              color: rgba(0, 0, 0, 0.6);

              &:hover {
                color: rgba(0, 0, 0, 0.9);
                background-color: rgba(0, 0, 0, 0.1);
              }
            }
          }
        }
      }
    }
  }

  // 已有评价卡片（追评模式）
  .existing-review-card {
    .existing-review-content {
      .review-rating {
        margin-bottom: @nordic-space-md;
      }

      .review-tags {
        margin-bottom: 12px;

        .review-tag {
          margin-right: @nordic-space-sm;
          margin-bottom: @nordic-space-sm;
        }
      }

      .review-content {
        font-size: 1.071rem;
        line-height: 1.6;
        color: @nordic-text-secondary;
        padding: 12px;
        background: @nordic-bg;
        border-radius: @nordic-radius-sm;
      }
    }
  }

  // 评价内容卡片
  .content-card {
    .review-textarea {
      :deep(.el-textarea__inner) {
        border-radius: @nordic-radius-sm;
        border: 1px solid @nordic-border;
        font-size: 1.071rem;
        line-height: 1.6;

        &:focus {
          border-color: @nordic-blue;
          box-shadow: 0 0 0 2px fade(@nordic-blue, 10%);
        }
      }
    }
  }

  // 提交按钮区域
  .submit-section {
    display: flex;
    justify-content: center;
    padding: 20px 0;

    .submit-btn {
      min-width: 200px;
      height: 50px;
      font-size: 1.143rem;
      font-weight: 600;
      border-radius: @nordic-radius-pill;
      background: linear-gradient(135deg, @nordic-blue 0%, fade(@nordic-blue, 80%) 100%);
      border: none;
      box-shadow: 0 4px 12px fade(@nordic-blue, 30%);
      transition: all @nordic-transition-slow cubic-bezier(0.4, 0, 0.2, 1);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px fade(@nordic-blue, 40%);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .evaluate-order-container {
    padding: 0 12px 16px 12px;

    .page-header {
      padding: 14px 16px;
      border-radius: 14px;
    }

    .page-title {
      font-size: 1.429rem;
    }

    .evaluate-content {
      gap: 12px;
    }

    .el-card {
      border-radius: 14px;

      :deep(.el-card__body) {
        padding: @nordic-space-md;
      }

      :deep(.el-card__header) {
        padding: 14px @nordic-space-md;
      }
    }

    .order-info-card {
      .merchant-info {
        .order-items {
          .order-item {
            .item-image,
            .item-image-placeholder {
              width: 50px;
              height: 50px;
            }

            .item-details {
              .item-name {
                font-size: 1rem;
              }

              .item-price {
                font-size: 1.071rem;
              }
            }
          }
        }

        .order-total {
          .total-label {
            font-size: 1rem;
          }

          .total-amount {
            font-size: 1.429rem;
          }
        }
      }
    }

    .rating-card {
      .rating-section {
        .rating-wrapper {
          .rating-stars {
            :deep(.el-rate) {
              .el-rate__icon {
                font-size: 2.571rem;
              }
            }
          }

          .rating-text {
            font-size: 1.143rem;
            min-width: 70px;
          }
        }
      }
    }

    .tags-card {
      .tags-section {
        gap: 12px;

        .preset-tags {
          gap: @nordic-space-sm;
        }

        .custom-tag-input {
          flex-direction: column;

          .add-tag-btn {
            width: 100%;
          }
        }

        .selected-tags {
          padding: @nordic-space-2sm;

          .selected-tags-label {
            font-size: 0.929rem;
          }

          .selected-tags-list {
            gap: 6px;

            .selected-tag-item {
              font-size: 0.857rem;
            }
          }
        }
      }
    }

    .submit-section {
      padding: @nordic-space-md 0;

      .submit-btn {
        min-width: 100%;
        height: 48px;
        font-size: 1.071rem;
      }
    }
  }
}
</style>
