<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_CONFIG } from '../../config'
import { ElMessage } from 'element-plus'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
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
const submitting = ref(false)

// 计算页面标题
const pageTitle = computed(() => {
  return isAdditionalReview.value ? '追加评价' : '评价订单'
})

// 提交进度：评分与（标签或内容）两项，用于底部主按钮上方展示完成度
const reviewProgress = computed(() => {
  const done = (rating.value > 0 ? 1 : 0) + (tags.value.length > 0 || content.value.trim() ? 1 : 0)
  return { done, total: 2 }
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

    // 订单菜品接口已返回菜品名称、图片与数量（含内嵌 dish 对象），直接用其组装，避免逐个请求 /v1/dishes/{id} 的 N+1 与无谓降级。
    const dishesResponse = await axios.get(`${API_CONFIG.baseURL}/v1/orders/${orderData.id}/dishes`)
    const orderDishes = Array.isArray(dishesResponse.data?.data) ? dishesResponse.data.data : []
    const items = orderDishes.map((orderDish) => {
      const dish = orderDish.dish || {}
      return {
        id: dish.id || orderDish.dishId,
        name: dish.name || orderDish.dishName || '菜品',
        quantity: Number(orderDish.quantity) || 0,
        price: Number(orderDish.price) || 0,
        image: dish.image || orderDish.dishImage || ''
      }
    })

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

    submitting.value = true
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
    } finally {
      submitting.value = false
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

  submitting.value = true
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
    const reviewContent = content.value.trim() || tags.value.join('、')
    const reviewData = {
      orderId: order.value.id,
      merchantId: order.value.merchantId,
      rating: Math.min(5, Math.max(1, Math.round(Number(rating.value)))),
      tags: tags.value,
      content: reviewContent,
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
  } finally {
    submitting.value = false
  }
}

// 组件挂载时加载订单详情
onMounted(() => {
  loadOrderDetail()
})
</script>

<template>
  <div class="evaluate-order-page">
    <AppPageHeader :title="pageTitle" subtitle="确认菜品后完成评分与评价">
      <template #actions>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <div v-loading="loading" element-loading-text="加载中..." class="content-container">
      <div v-if="order" class="evaluate-content">
        <!-- 订单信息：压缩为一行摘要 + 菜品行（§4.2 卡片骨架、§5.2 金额中性色） -->
        <section class="order-card">
          <header class="order-card__head">
            <span class="order-card__merchant">{{ order.merchant || '商家' }}</span>
            <span class="order-card__meta">
              <span class="order-no">订单号 {{ order.orderNo }}</span>
              <span class="order-card__total">
                <span class="order-card__total-label">合计</span>
                <span class="order-card__total-amount">¥{{ order.total.toFixed(2) }}</span>
              </span>
            </span>
          </header>

          <ul class="dish-list">
            <li v-for="item in order.items" :key="item.id" class="dish-row">
              <div class="dish-thumb">
                <img v-if="item.image" :src="item.image" :alt="item.name" loading="lazy" />
                <span v-else class="dish-thumb__fallback">{{ item.name?.charAt(0) || '菜' }}</span>
              </div>
              <div class="dish-info">
                <span class="dish-name">{{ item.name }}</span>
                <div class="dish-sub">
                  <span class="dish-quantity">×{{ item.quantity }}</span>
                  <span class="dish-unit-price">¥{{ item.price.toFixed(2) }}</span>
                </div>
              </div>
              <span class="dish-amount">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </li>
          </ul>
        </section>

        <!-- 评分（追评时不显示，§4.6 即时选中反馈） -->
        <section v-if="!isAdditionalReview" class="form-card">
          <h2 class="form-card__title">评分</h2>
          <p class="form-card__hint">请为本次用餐体验评分</p>
          <div class="rating-line">
            <el-rate
              v-model="rating"
              :colors="['var(--warning)', 'var(--warning)', 'var(--warning)']"
              :texts="['非常差', '差', '一般', '好', '非常好']"
              show-text
              size="large"
            />
            <span class="rating-text" :class="{ 'rating-text--empty': rating === 0 }">
              {{ getRatingText(rating) }}
            </span>
          </div>
        </section>

        <!-- 已有评价（追评模式） -->
        <section v-if="isAdditionalReview && existingReview" class="form-card">
          <h2 class="form-card__title">我的评价</h2>
          <div class="existing-review">
            <el-rate
              v-model="existingReview.rating"
              disabled
              :colors="['var(--warning)', 'var(--warning)', 'var(--warning)']"
              size="large"
            />
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
            <p v-if="existingReview.content" class="review-content">{{ existingReview.content }}</p>
          </div>
        </section>

        <!-- 标签（追评时不显示，§3.1 语义色状态切换） -->
        <section v-if="!isAdditionalReview" class="form-card">
          <h2 class="form-card__title">标签<span class="form-card__note">可多选</span></h2>
          <div class="tag-list">
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

          <div class="tag-custom">
            <el-input
              v-model="customTagInput"
              placeholder="自定义标签"
              maxlength="10"
              show-word-limit
              class="tag-input"
              @keydown="handleTagInputKeydown"
            />
            <el-button class="tag-add-btn" @click="addCustomTag">添加</el-button>
          </div>

          <transition name="fade-slide">
            <div v-if="tags.length > 0" class="tag-selected">
              <span class="tag-selected__label">已选标签</span>
              <div class="tag-selected__list">
                <el-tag
                  v-for="(tag, index) in tags"
                  :key="`selected-${tag}`"
                  closable
                  type="warning"
                  effect="light"
                  class="tag-selected__item"
                  @close="removeTag(index)"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </transition>
        </section>

        <!-- 评价内容 -->
        <section class="form-card">
          <h2 class="form-card__title">{{ isAdditionalReview ? '追评内容' : '评价内容' }}</h2>
          <el-input
            v-model="content"
            type="textarea"
            :rows="5"
            :placeholder="
              isAdditionalReview
                ? '继续分享您的用餐体验...'
                : '分享您的用餐体验，说说您的感受...'
            "
            maxlength="500"
            show-word-limit
            class="review-textarea"
          />
        </section>

        <!-- 提交：底部唯一实心主按钮 + 完成进度（§4.1、§4.6） -->
        <footer class="submit-bar">
          <p class="submit-progress">
            已完成
            <span class="submit-progress__num">{{ reviewProgress.done }}/{{ reviewProgress.total }}</span>
            项
          </p>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="submitting"
            @click="submitReview"
          >
            {{ isAdditionalReview ? '提交追评' : '提交评价' }}
          </el-button>
        </footer>
      </div>

      <el-empty v-else-if="!loading" description="订单信息不存在" />
    </div>
  </div>
</template>

<style scoped lang="less">
/* 页面基准色调全部来自 tokens.css（§3.1/§3.4/§7.1），不写裸色值。 */

/* 单列居中 1200px，public/user 标准版栅格（§3.3）。 */
.evaluate-order-page {
  box-sizing: border-box;
  width: 100%;
  max-width: var(--page-max);
  min-height: calc(100vh - 80px);
  margin: 0 auto;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* 页头标准版：标题 --text-h1 + --space-6，返回为次级动作（§4.1、§4.7）。 */
:deep(.app-page-header) {
  background: var(--canvas);
}

.content-container {
  min-height: 400px;
}

.evaluate-content {
  display: flex;
  flex-direction: column;
  gap: var(--card-gap);
}

/* L1 微质感卡片：--surface 底 + --line 描边 + 顶部内高光，不用大阴影（§3.4）。 */
.order-card,
.form-card,
.submit-bar {
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: inset 0 1px 0 var(--surface);
}

/* 订单信息压缩为一行摘要 + 菜品行，让首屏聚焦评价对象（§4.2）。 */
.order-card {
  padding: var(--card-padding);
}

.order-card__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-4);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--line);
}

.order-card__merchant {
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
}

.order-card__meta {
  display: inline-flex;
  align-items: baseline;
  gap: var(--space-5);
}

.order-no {
  color: var(--ink-500);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
}

.order-card__total {
  display: inline-flex;
  align-items: baseline;
  gap: var(--space-2);
}

.order-card__total-label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

/* 金额中性色 + --font-num + tabular-nums，不用红/绿（§5.2）。 */
.order-card__total-amount {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h3);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
}

.dish-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.dish-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--line);
}

.dish-row:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

/* 菜品缩略图固定 4:3 裁切（§4.2），80×60。 */
.dish-thumb {
  width: 80px;
  height: 60px;
  flex: 0 0 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: var(--radius-sm);
  background: var(--canvas-sunken);
}

.dish-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 占位态用中性底 + 菜品首字，不引第三方占位服务（§4.2）。 */
.dish-thumb__fallback {
  color: var(--ink-400);
  font-size: var(--text-h2);
  font-weight: var(--weight-semibold);
}

.dish-info {
  flex: 1;
  min-width: 0;
}

.dish-name {
  display: block;
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
}

.dish-sub {
  display: inline-flex;
  align-items: baseline;
  gap: var(--space-3);
  margin-top: var(--space-1);
}

.dish-quantity {
  color: var(--ink-500);
  font-family: var(--font-num);
  font-size: var(--text-body-sm);
  font-variant-numeric: tabular-nums;
}

.dish-unit-price {
  color: var(--ink-500);
  font-family: var(--font-num);
  font-size: var(--text-caption);
  font-variant-numeric: tabular-nums;
}

.dish-amount {
  flex: 0 0 auto;
  color: var(--ink-700);
  font-family: var(--font-num);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  font-variant-numeric: tabular-nums;
}

/* 表单区块：L1 卡 + --card-padding、区块间 --card-gap（§3.3、§3.4）。 */
.form-card {
  padding: var(--card-padding);
}

.form-card__title {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-h3);
  font-weight: var(--weight-semibold);
}

.form-card__note {
  margin-left: var(--space-2);
  color: var(--ink-400);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-regular);
}

.form-card__hint {
  margin: var(--space-2) 0 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.rating-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-5);
  margin-top: var(--space-4);
}

.rating-line :deep(.el-rate) {
  height: auto;
}

/* 评分文字取 warning 语义，未评为中性（§3.1）；切换走 --duration-instant 即时反馈（§3.5）。 */
.rating-text {
  color: var(--warning);
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  transition: color var(--duration-instant) var(--ease-standard);
}

.rating-text--empty {
  color: var(--ink-400);
  font-weight: var(--weight-regular);
}

.existing-review {
  margin-top: var(--space-4);
}

.review-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-3);
}

.review-content {
  margin: var(--space-4) 0 0;
  padding: var(--space-3);
  border-radius: var(--radius-sm);
  background: var(--canvas-sunken);
  color: var(--ink-700);
  font-size: var(--text-body);
  line-height: 22px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-4);
}

/* 高频微交互：选中即时反馈用 CSS transition，沿用 Element Plus 过渡（§3.5）。 */
.tag-item {
  cursor: pointer;
  transition:
    color var(--duration-instant) var(--ease-standard),
    border-color var(--duration-instant) var(--ease-standard),
    background-color var(--duration-instant) var(--ease-standard);
}

.tag-custom {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
}

.tag-custom .tag-input {
  flex: 1;
}

.tag-custom :deep(.el-input__wrapper) {
  border-radius: var(--radius-sm);
}

/* 非主按钮：次级描边，不抢唯一主按钮（§4.1）。 */
.tag-add-btn {
  border-radius: var(--radius-md);
}

.tag-selected {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  margin-top: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--canvas-sunken);
}

.tag-selected__label {
  flex: 0 0 auto;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.tag-selected__list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

/* 选中标签区展开用 CSS 过渡，时长与缓动取 token（§3.5）。 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition:
    opacity var(--duration-base) var(--ease-standard),
    transform var(--duration-base) var(--ease-standard);
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.review-textarea {
  margin-top: var(--space-4);
}

.review-textarea :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  font-size: var(--text-body);
  line-height: 22px;
}

/* 提交区：底部唯一实心主按钮（--brand-500，§4.1），圆角 --radius-md，不做彩色渐变。 */
.submit-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--card-padding);
}

.submit-progress {
  margin: 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

.submit-progress__num {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
}

.submit-btn {
  min-width: 180px;
  height: var(--control-height-md);
  border-radius: var(--radius-md);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
}

/* 移动端：页边距降到 --space-4 并让主按钮铺满（§3.3）。 */
@media (max-width: 768px) {
  .evaluate-order-page {
    padding: 0 var(--space-4) var(--space-6);
  }

  .submit-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .submit-btn {
    width: 100%;
  }
}
</style>
