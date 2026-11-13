<template>
  <div class="order-confirmation-container">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <template v-if="fromChat">
            <el-tag :type="fromSingleChat ? 'primary' : 'success'" size="large" class="chat-tag">
              {{ fromSingleChat ? '单聊' : '群聊' }}
            </el-tag>
            订单确认
          </template>
          <template v-else> 订单确认 </template>
        </h1>
        <!-- 「返回」统一放右侧动作区（§4.7），次按钮样式（§4.9） -->
        <common-back-button />
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧主要内容区 -->
      <div class="content-left">
        <!-- 商家信息卡片 -->
        <el-card class="info-card merchant-card enter" shadow="never">
          <div class="merchant-info">
            <div class="merchant-avatar">
              <el-icon :size="40"><Shop /></el-icon>
            </div>
            <div class="merchant-details">
              <div class="merchant-name">{{ merchantInfo.name }}</div>
              <div class="merchant-meta">
                <span class="rating">{{ merchantInfo.rating }} 分</span>
                <span class="separator">|</span>
                <span class="pickup-type">仅支持到店自取</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 自取提示卡片 -->
        <el-card class="info-card pickup-card enter" shadow="never">
          <div class="pickup-info">
            <div class="pickup-icon">
              <el-icon :size="32"><Shop /></el-icon>
            </div>
            <div class="pickup-details">
              <div class="pickup-title">到店自取</div>
              <div class="pickup-desc">请到店取餐，商家地址：{{ merchantInfo.address || '商家地址' }}</div>
              <div class="pickup-time">
                <el-icon><Clock /></el-icon>
                预计等待时间：约15-20分钟
              </div>
            </div>
          </div>
        </el-card>

        <!-- 订单商品卡片 -->
        <el-card class="info-card enter" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单商品</span>
              <el-button v-if="canEditOrderItems" type="text" size="small" @click="openCart">
                <el-icon><Edit /></el-icon>
                编辑订单
              </el-button>
            </div>
          </template>

          <!-- 已支付订单 -->
          <div v-if="orderInfo.paidItems.length > 0" class="order-section">
            <div class="section-header">
              <el-icon class="icon-success"><CircleCheck /></el-icon>
              <span class="section-title">已支付商品</span>
              <el-tag type="success" size="small" effect="plain">不可修改</el-tag>
            </div>
            <order-item-list :items="orderInfo.paidItems" :show-payment-info="true" />
          </div>

          <!-- 未支付订单 -->
          <div class="order-section" :class="{ 'mt-20': orderInfo.paidItems.length > 0 }">
            <div class="section-header">
              <el-icon class="icon-warning"><Clock /></el-icon>
              <span class="section-title">待支付商品</span>
              <el-tag :type="canEditOrderItems ? 'warning' : 'info'" size="small" effect="plain">
                {{ canEditOrderItems ? '可编辑' : '已锁单' }}
              </el-tag>
            </div>
            <order-item-list :items="orderInfo.unpaidItems" :show-payment-info="false" />
          </div>
        </el-card>

        <!-- 支付明细卡片 -->
        <el-card class="info-card payment-summary-card enter" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">支付明细</span>
            </div>
          </template>

          <div class="payment-details">
            <div class="detail-row">
              <span class="detail-label">商品总额</span>
              <span class="detail-value"
                >¥{{ (orderInfo.originalTotal || orderInfo.totalUnpaid).toFixed(2) }}</span
              >
            </div>
            <div class="detail-row" v-if="discountApplied">
              <span class="detail-label">优惠减免</span>
              <span class="detail-value discount">-¥{{ discountAmount.toFixed(2) }}</span>
            </div>

            <el-divider></el-divider>

            <div class="detail-row total-row">
              <span class="total-label">应付金额</span>
              <span class="total-value" :class="{ 'amount-updated': amountFlashing }">
                ¥{{ finalAmount.toFixed(2) }}
              </span>
            </div>
          </div>

          <!-- 优惠券 -->
          <div class="coupon-section" v-if="discounts.length > 0">
            <div class="coupon-item" v-for="discount in discounts" :key="discount.id">
              <div class="coupon-info-main">
                <div class="coupon-left">
                  <el-icon class="icon-warning"><Ticket /></el-icon>
                  <div class="coupon-details">
                    <span class="coupon-name">{{ discount.name }}</span>
                    <span class="coupon-desc" v-if="discount.minAmount > 0">
                      满 {{ discount.minAmount }} 元可用
                    </span>
                  </div>
                </div>
                <div class="coupon-amount">¥{{ discount.amount.toFixed(2) }}</div>
              </div>
              <el-button
                v-if="!discount.used"
                type="warning"
                size="small"
                plain
                @click="useDiscount(discount)"
              >
                使用
              </el-button>
              <el-button v-else type="danger" size="small" plain @click="cancelDiscount">
                取消
              </el-button>
            </div>
          </div>

          <el-divider></el-divider>

          <!-- 支付渠道余额 -->
          <div class="balance-info">
            <div class="balance-item">
              <span class="balance-label">平台币余额</span>
              <span class="balance-value">¥{{ platformBalance.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 订单备注卡片 -->
        <el-card class="info-card enter" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单备注</span>
              <el-tag type="info" size="small">选填</el-tag>
            </div>
          </template>

          <div class="order-remark-section">
            <el-input
              v-model="orderRemark"
              type="textarea"
              :rows="3"
              placeholder="填写订单备注，如：口味偏好、 preparation时间等要求"
              maxlength="200"
              show-word-limit
              resize="none"
            />
            <div class="quick-remarks">
              <span class="quick-remark-label">快速备注：</span>
              <el-tag
                v-for="quickRemark in quickRemarks"
                :key="quickRemark"
                size="small"
                effect="plain"
                class="quick-remark-tag"
                @click="addQuickRemark(quickRemark)"
              >
                {{ quickRemark }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <!-- 支付方式卡片 -->
        <el-card class="info-card enter" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">支付方式</span>
            </div>
          </template>

          <div class="payment-methods">
            <div
              v-for="option in paymentMethods"
              :key="option.id"
              class="payment-method-item"
              :class="{ active: selectedPaymentMethod.id === option.id }"
              @click="selectedPaymentMethod = option"
            >
              <div class="method-icon">{{ option.icon }}</div>
              <div class="method-info">
                <div class="method-name">{{ option.name }}</div>
                <div class="method-desc" v-if="option.desc">{{ option.desc }}</div>
              </div>
              <el-radio v-model="selectedPaymentMethod.id" :label="option.id"></el-radio>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 底部提交栏 -->
    <div class="bottom-submit-bar enter">
      <div class="submit-bar-content">
        <div class="submit-bar-left">
          <div class="payment-tips">
            <el-icon class="icon-muted" :size="16"><InfoFilled /></el-icon>
            <span>已支付订单不可修改，仅支付未支付部分</span>
          </div>
        </div>
        <div class="submit-bar-right">
          <div class="total-info">
            <span class="total-label">应付金额：</span>
            <span class="total-amount" :class="{ 'amount-updated': amountFlashing }">¥{{ finalAmount.toFixed(2) }}</span>
          </div>
          <el-button
            type="primary"
            size="large"
            class="submit-button-fixed"
            @click="confirmOrder"
            :loading="submitting"
            :disabled="submitting"
          >
            {{ submitButtonText }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 购物车编辑弹窗 -->
    <el-dialog v-model="cartVisible" title="编辑订单" width="600px" @close="closeCart">
      <div v-if="cartItems.length === 0" class="empty-cart">
        <el-empty description="购物车是空的"></el-empty>
      </div>
      <div v-else class="cart-items">
        <div class="cart-item" v-for="item in cartItems" :key="item.id">
          <div class="cart-item-main">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
            <!-- 食材信息 -->
            <div class="item-specs" v-if="hasIngredients(item)">
              <el-tag
                v-if="item.requiredIngredients && item.requiredIngredients.length > 0"
                size="small"
                type="info"
                effect="plain"
                class="ingredient-tag"
              >
                必选: {{ item.requiredIngredients.join('、') }}
              </el-tag>
              <el-tag
                v-if="
                  item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0
                "
                size="small"
                type="success"
                effect="plain"
                class="ingredient-tag"
              >
                加选: {{ formatOptionalIngredients(item.selectedOptionalIngredients) }}
              </el-tag>
            </div>
            <!-- 备注编辑 -->
            <div class="item-note-edit">
              <el-input
                v-model="item.note"
                type="textarea"
                :rows="2"
                placeholder="添加备注，如：少辣、不要香菜等"
                maxlength="100"
                show-word-limit
                size="small"
              />
            </div>
          </div>
          <div class="cart-item-controls">
            <el-input-number
              v-model="item.quantity"
              :min="0"
              :max="99"
              size="small"
              @change="updateItemTotal(item)"
            />
            <div class="item-total">
              ¥{{ (item.totalPrice || item.price * item.quantity).toFixed(2) }}
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeCart">取消</el-button>
          <el-button type="primary" v-if="cartItems.length > 0" @click="updateOrderInfo">
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- AA支付模态框 -->
    <el-dialog v-model="aaPaymentModalVisible" title="AA支付确认" width="400px">
      <div class="aa-payment-content">
        <div class="aa-info">
          <div class="info-item">
            <span class="info-label">订单总金额:</span>
            <span class="info-value">¥{{ orderInfo.totalUnpaid.toFixed(2) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">参与人数:</span>
            <span class="info-value">{{ orderInfo.members?.length || 2 }}人</span>
          </div>
          <div class="info-item">
            <span class="info-label">每人需支付:</span>
            <span class="info-value highlight">¥{{ aaShareAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="aaPaymentModalVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAAPayment">确认AA支付</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 自定义分摊模态框 -->
    <el-dialog v-model="customShareModalVisible" title="自定义分摊" width="500px">
      <div class="custom-share-content">
        <div class="custom-info">
          <div class="info-item">
            <span class="info-label">订单总金额:</span>
            <span class="info-value">¥{{ orderInfo.totalUnpaid.toFixed(2) }}</span>
          </div>
        </div>

        <div class="share-list">
          <div class="share-item" v-for="(share, index) in customShares" :key="index">
            <div class="member-name">{{ share.member }}</div>
            <el-input-number
              v-model="share.amount"
              :min="0"
              :precision="2"
              :step="0.01"
              style="width: 120px"
              @change="updateCustomShare(index, share.amount)"
            />
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="customShareModalVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCustomShare">确认自定义分摊</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { Shop, Edit, CircleCheck, Clock, Ticket, InfoFilled } from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import OrderItemList from './components/OrderItemList.vue'
import groupOrderApi from '../../api/groupOrder'
import walletApi from '../../api/wallet'
import paymentApi from '../../api/payment'
import orderApi from '../../api/order'
import couponApi from '../../api/coupon'
import { useAuthStore } from '../../store/authStore'

const router = useRouter()
const authStore = useAuthStore()

// 从会话存储获取订单信息
const pendingOrder = JSON.parse(sessionStorage.getItem('pendingOrder')) || {}

// 检查订单是否为空
const isEmptyOrder = !pendingOrder.cartItems || pendingOrder.cartItems.length === 0

// 从pendingOrder中提取商家ID（兼容 merchantId 和 id 两种字段名）
const merchantId = ref(String(pendingOrder.merchant?.merchantId || pendingOrder.merchant?.id || ''))
if (!merchantId.value) {
  console.error('商家ID缺失，pendingOrder:', pendingOrder)
  ElMessage.error('商家信息异常，请返回重试')
  router.back()
}

// 合并重复商品行：同一菜品（含同规格、同备注）只保留一行并累加数量，总额不变
const mergeOrderItems = (items) => {
  const merged = []
  const indexByKey = new Map()

  ;(items || []).forEach((item) => {
    const key = JSON.stringify([
      String(item.id ?? item.dishId ?? ''),
      item.requiredIngredients || [],
      item.selectedOptionalIngredients || [],
      item.note || item.customization || ''
    ])
    const hitIndex = indexByKey.get(key)

    if (hitIndex === undefined) {
      indexByKey.set(key, merged.length)
      merged.push({ ...item })
      return
    }

    const target = merged[hitIndex]
    const quantity = Number(item.quantity) || 0
    target.quantity = (Number(target.quantity) || 0) + quantity
    target.totalPrice = (Number(target.price) || 0) * target.quantity
  })

  return merged
}

// 订单信息（ID由后端IdGenerator统一生成，避免重复）
const orderInfo = ref({
  orderId: null,
  groupName: pendingOrder.groupName || '默认订单群',
  userName: pendingOrder.userName || '',
  creator: pendingOrder.creator || '',
  members: pendingOrder.members || [],
  paidItems: [],
  unpaidItems: mergeOrderItems(pendingOrder.cartItems),
  totalPaid: 0.0,
  totalUnpaid:
    pendingOrder.totalAmount ||
    (pendingOrder.cartItems || []).reduce(
      (total, item) => total + (item.totalPrice || item.price * item.quantity),
      0
    )
})

// 如果订单为空，返回上一页并提示
if (isEmptyOrder) {
  ElMessage.warning('购物车为空，无法进行订单确认')
  router.back()
}

// 商家信息 - 从pendingOrder中获取，如果没有则使用默认值
const merchantInfo = ref({
  id: String(pendingOrder.merchant?.merchantId || pendingOrder.merchant?.id || '1'),
  name: pendingOrder.merchant?.name || '佳食优选餐厅',
  rating: pendingOrder.merchant?.rating || 4.8,
  deliveryTime: pendingOrder.merchant?.deliveryTime || '约30分钟',
  deliveryFee: pendingOrder.merchant?.deliveryFee || 5.0
})

// 购物车数据
const cartItems = ref([])
const cartVisible = ref(false)

// 订单备注
const orderRemark = ref('')

// 快速备注选项
const quickRemarks = ref(['尽快准备好', '准时到店取', '请提前准备', '到店电话联系', '趁热食用'])

// 添加快速备注
const addQuickRemark = (remark) => {
  if (orderRemark.value) {
    // 如果已有备注，追加备注
    if (!orderRemark.value.includes(remark)) {
      orderRemark.value += '，' + remark
    }
  } else {
    // 如果没有备注，直接设置
    orderRemark.value = remark
  }
}

// 打开购物车编辑弹窗
const openCart = () => {
  if (!canEditOrderItems.value) {
    ElMessage.warning('拼单已确认成团，订单商品不可修改')
    return
  }

  // 创建深拷贝，避免直接修改原始数据
  cartItems.value = JSON.parse(JSON.stringify(orderInfo.value.unpaidItems))
  cartVisible.value = true
}

// 检测订单类型
const isGroupOrder = ref(orderInfo.value.groupName !== '默认订单群')
const fromChat = ref(pendingOrder.fromChat || false)
const fromSingleChat = ref(fromChat.value && !isGroupOrder.value)
const groupOrderId = ref(String(pendingOrder.groupOrderId || pendingOrder.orderId || ''))
const groupOrderTotalAmount = ref(Number(pendingOrder.totalAmount || orderInfo.value.totalUnpaid || 0))
const personalPayableAmount = ref(Number(orderInfo.value.totalUnpaid || 0))
const canEditOrderItems = computed(() => !(isGroupOrder.value && fromChat.value))

// 支付方式
const paymentMethods = computed(() => {
  if (isGroupOrder.value) {
    const methods = [{ id: 1, name: '个人支付', icon: '💳', desc: '仅支付我的部分' }]
    if (orderInfo.value.creator === '我') {
      methods.push(
        { id: 2, name: '统一支付', icon: '🧮', desc: '一人支付全部订单' },
        { id: 3, name: 'AA支付', icon: '🎉', desc: '平均分摊订单金额' },
        { id: 4, name: '自定义分摊', icon: '📝', desc: '按需分配订单金额' }
      )
    }
    return methods
  } else {
    return [
      { id: 1, name: '个人支付', icon: '💳', desc: '使用账户余额支付' },
      { id: 2, name: '他人代付', icon: '🤝', desc: '邀请好友代为支付' }
    ]
  }
})

const selectedPaymentMethod = ref(paymentMethods.value[0])

// 提交状态
const submitting = ref(false)

// 平台币余额 - 从后端获取
const platformBalance = ref(0.0)

// 提交按钮文字
const submitButtonText = computed(() => {
  const method = selectedPaymentMethod.value
  switch (method.id) {
    case 1:
      return isGroupOrder.value ? '立即支付' : '确认支付'
    case 2:
      return isGroupOrder.value ? '统一支付' : '请求代付'
    case 3:
      return '发起AA支付'
    case 4:
      return '自定义分摊'
    default:
      return '确认支付'
  }
})

// AA支付相关
const aaPaymentModalVisible = ref(false)
const aaShareAmount = ref(0)

// 自定义分摊相关
const customShareModalVisible = ref(false)
const customShares = ref([])

// 优惠券（从后端加载，初始为空）
const discounts = ref([])

const selectedDiscount = ref(null)
const discountApplied = computed(() => selectedDiscount.value !== null)
const discountAmount = computed(() => {
  if (!selectedDiscount.value || !orderInfo.value.originalTotal) return 0
  // 计算实际优惠金额：原始总额 - 当前总额
  return orderInfo.value.originalTotal - orderInfo.value.totalUnpaid
})

// 最终金额（注意：orderInfo.totalUnpaid 已经在 useDiscount 中被修改过了）
const finalAmount = computed(() => {
  return orderInfo.value.totalUnpaid
})

// 金额变化反馈：实付金额变化时一次性高亮，动画时长/缓动由 CSS token 决定
const amountFlashing = ref(false)
let amountFlashTimer = null
watch(finalAmount, () => {
  amountFlashing.value = false
  if (amountFlashTimer) clearTimeout(amountFlashTimer)
  requestAnimationFrame(() => {
    amountFlashing.value = true
    amountFlashTimer = setTimeout(() => {
      amountFlashing.value = false
    }, 600)
  })
})

const resetDiscountState = () => {
  if (selectedDiscount.value) {
    selectedDiscount.value.used = false
    selectedDiscount.value = null
  }
  delete orderInfo.value.originalTotal
}

const syncGroupOrderPayableAmount = () => {
  if (!(isGroupOrder.value && fromChat.value)) {
    return
  }

  const methodId = selectedPaymentMethod.value.id
  orderInfo.value.totalUnpaid = methodId === 2
    ? Number(groupOrderTotalAmount.value || 0)
    : Number(personalPayableAmount.value || 0)
}

const loadBalance = async (userId) => {
  const balanceResponse = await walletApi.getBalance(userId)
  console.log('余额获取响应:', balanceResponse)
  if (balanceResponse.code === '200' || balanceResponse.code === 200) {
    const latestBalance = Number(balanceResponse.data || 0)
    platformBalance.value = Number.isFinite(latestBalance) ? latestBalance : 0
    return platformBalance.value
  }

  throw new Error(balanceResponse.message || '余额获取失败')
}

const loadCoupons = async (userId) => {
  try {
    const couponResponse = await couponApi.getUserCoupons(userId)
    if (couponResponse.code === '200' || couponResponse.code === 200) {
      discounts.value = (couponResponse.data || []).filter(
        (c) => c.status === 'available' || c.status === 'valid'
      )
      return
    }
  } catch (couponError) {
    console.error('获取优惠券失败:', couponError)
  }

  discounts.value = []
}

const refreshGroupOrderSettlement = async (userId) => {
  if (!groupOrderId.value) {
    return
  }

  const response = await groupOrderApi.getSettlement(groupOrderId.value, userId)
  if (!(response.code === '200' || response.code === 200 || response.success)) {
    throw new Error(response.message || '获取拼单结算信息失败')
  }

  const settlement = response.data || {}
  personalPayableAmount.value = Number(settlement.totalAmount || 0)
  if (Array.isArray(settlement.participants) && settlement.participants.length > 0) {
    orderInfo.value.members = settlement.participants
  }
}

const payGroupOrder = async (userId, methodId) => {
  const paymentType = methodId === 2 ? 'all' : 'single'
  const response = await groupOrderApi.pay(groupOrderId.value, {
    userId,
    paymentType,
    paymentMethod: 'balance',
    couponId: selectedDiscount.value?.id || ''
  })

  if (!(response.code === '200' || response.code === 200 || response.success)) {
    throw new Error(response.message || '支付失败，请重试')
  }

  const paymentData = response.data || {}
  sessionStorage.removeItem('pendingOrder')
  sessionStorage.setItem('paidGroupOrderId', groupOrderId.value)
  sessionStorage.setItem('paidGroupOrderAmount', String(paymentData.amount || finalAmount.value))
  ElMessage.success(paymentType === 'all' ? '统一支付成功！' : '支付成功！您的拼单正在等待商家接单')

  await loadBalance(userId)

  setTimeout(() => {
    submitting.value = false
    router.push('/user/home/orders')
  }, 1500)
}

watch(() => selectedPaymentMethod.value.id, async () => {
  if (!(isGroupOrder.value && fromChat.value)) {
    return
  }

  resetDiscountState()
  syncGroupOrderPayableAmount()

  const userId = String(authStore.userId || '').trim()
  if (userId) {
    await loadCoupons(userId)
  }
})

// 初始化余额、优惠券
onMounted(async () => {
  try {
    const userId = String(authStore.userId || '').trim()
    if (userId) {
      if (isGroupOrder.value && fromChat.value) {
        await refreshGroupOrderSettlement(userId)
        syncGroupOrderPayableAmount()
      }

      await loadBalance(userId)
      await loadCoupons(userId)
    }
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.warning('初始化失败，请刷新页面重试')
  }
})

// 计算AA支付每人金额
const calculateAAShare = () => {
  const memberCount = orderInfo.value.members?.length || 2
  const share = finalAmount.value / memberCount
  aaShareAmount.value = parseFloat(share.toFixed(2))
}

// 初始化自定义分摊
const initCustomShares = () => {
  const members = orderInfo.value.members || ['我', '好友']
  customShares.value = members.map((member) => ({
    member,
    amount: parseFloat((finalAmount.value / members.length).toFixed(2))
  }))
}

// 打开AA支付模态框
const openAAPaymentModal = () => {
  calculateAAShare()
  aaPaymentModalVisible.value = true
}

// 打开自定义分摊模态框
const openCustomShareModal = () => {
  initCustomShares()
  customShareModalVisible.value = true
}

// 确认AA支付
const confirmAAPayment = () => {
  submitting.value = true
  aaPaymentModalVisible.value = false
  ElMessage.success('AA支付已发起，将自动为每位成员创建支付订单')

  sessionStorage.removeItem('pendingOrder')

  setTimeout(() => {
    submitting.value = false
    router.push('/user/home/orders')
  }, 1500)
}

// 确认自定义分摊
const confirmCustomShare = () => {
  const totalShare = customShares.value.reduce((sum, share) => sum + share.amount, 0)
  if (Math.abs(totalShare - finalAmount.value) > 0.01) {
    ElMessage.error('分摊总额必须等于订单总额')
    return
  }

  submitting.value = true
  customShareModalVisible.value = false
  ElMessage.success('自定义分摊已发起，将为每位成员创建对应金额的支付订单')

  sessionStorage.removeItem('pendingOrder')

  setTimeout(() => {
    submitting.value = false
    router.push('/user/home/orders')
  }, 1500)
}

// 更新自定义分摊金额
const updateCustomShare = (index, amount) => {
  customShares.value[index].amount = parseFloat(amount)
}

// 使用优惠
const useDiscount = async (discount) => {
  if (!discount || discount.used) return

  if (discount.status !== 'available' && discount.status !== 'valid') {
    ElMessage.warning(discount.status === 'expired' ? '优惠券已过期' : '优惠券已使用')
    return
  }

  // 检查最低消费限制（如果有）
  const minAmount = Number(discount.minAmount || 0)
  const currentAmount = orderInfo.value.totalUnpaid

  if (currentAmount < minAmount) {
    ElMessage.warning(`此优惠券需满${minAmount}元可用，当前订单金额为¥${currentAmount.toFixed(2)}`)
    return
  }

  // 检查优惠券是否可用（调用后端API）
  try {
    const checkResponse = await couponApi.checkCouponAvailable(discount.id, currentAmount)
    if (checkResponse.code !== '200') {
      ElMessage.warning(checkResponse.message || '优惠券不可用')
      return
    }
    if (!checkResponse.data?.available) {
      ElMessage.warning(checkResponse.data?.note || '优惠券不可用')
      return
    }
  } catch (error) {
    console.error('检查优惠券失败:', error)
    ElMessage.warning('优惠券校验失败，请稍后重试')
    return
  }

  selectedDiscount.value = discount
  discount.used = true

  if (!orderInfo.value.originalTotal) {
    orderInfo.value.originalTotal = orderInfo.value.totalUnpaid
  }

  const discountAmount = Math.min(discount.amount, orderInfo.value.totalUnpaid)
  orderInfo.value.totalUnpaid -= discountAmount

  console.log('优惠券使用成功:', {
    优惠券ID: discount.id,
    优惠券名称: discount.name,
    优惠金额: discountAmount,
    原始总额: orderInfo.value.originalTotal,
    优惠后总额: orderInfo.value.totalUnpaid
  })

  ElMessage.success(`优惠已使用，减免¥${discountAmount.toFixed(2)}`)
}

// 取消使用优惠
const cancelDiscount = () => {
  if (!selectedDiscount.value) return

  // 使用保存的原始总额恢复
  if (orderInfo.value.originalTotal) {
    orderInfo.value.totalUnpaid = orderInfo.value.originalTotal
  } else {
    // 如果没有保存原始总额，则重新计算（兜底逻辑）
    const discountAmount = Math.min(
      selectedDiscount.value.amount,
      orderInfo.value.totalUnpaid + selectedDiscount.value.amount
    )
    orderInfo.value.totalUnpaid += discountAmount
  }

  delete orderInfo.value.originalTotal

  selectedDiscount.value.used = false
  selectedDiscount.value = null

  ElMessage.success('优惠已取消')
}

// 关闭购物车
const closeCart = () => {
  cartVisible.value = false
  cartItems.value = [] // 清空编辑数据
}

// 更新单项总价
const updateItemTotal = (item) => {
  item.totalPrice = item.price * item.quantity
}

// 更新订单信息
const updateOrderInfo = () => {
  orderInfo.value.unpaidItems = cartItems.value.filter((item) => item.quantity > 0)

  // 计算新的商品总额
  const newTotalUnpaid = cartItems.value
    .filter((item) => item.quantity > 0)
    .reduce((total, item) => total + (item.totalPrice || item.price * item.quantity), 0)

  // 如果有已使用的优惠券，需要重新应用
  if (selectedDiscount.value) {
    // 保存新的原始总额
    orderInfo.value.originalTotal = newTotalUnpaid
    // 应用优惠券折扣
    const discountAmount = Math.min(selectedDiscount.value.amount, newTotalUnpaid)
    orderInfo.value.totalUnpaid = newTotalUnpaid - discountAmount
  } else {
    orderInfo.value.totalUnpaid = newTotalUnpaid
  }

  const updatedOrder = { ...pendingOrder }
  updatedOrder.cartItems = cartItems.value.filter((item) => item.quantity > 0)
  updatedOrder.totalAmount = orderInfo.value.totalUnpaid
  sessionStorage.setItem('pendingOrder', JSON.stringify(updatedOrder))

  closeCart()
  ElMessage.success('订单已更新')
}

// 判断是否有食材信息
const hasIngredients = (item) => {
  return (
    (item.requiredIngredients && item.requiredIngredients.length > 0) ||
    (item.selectedOptionalIngredients && item.selectedOptionalIngredients.length > 0)
  )
}

// 格式化可选食材
const formatOptionalIngredients = (ingredients) => {
  return ingredients.map((ing) => (typeof ing === 'object' ? ing.name : ing)).join('、')
}

// 确认订单
const confirmOrder = async () => {
  const methodId = selectedPaymentMethod.value.id

  if (isGroupOrder.value && fromChat.value && (methodId === 3 || methodId === 4)) {
    ElMessage.warning('桌面端拼单暂仅支持个人支付或统一支付')
    return
  }

  if (methodId === 3) {
    openAAPaymentModal()
    return
  }

  if (methodId === 4) {
    openCustomShareModal()
    return
  }

  if (methodId === 2 && !isGroupOrder.value) {
    ElMessageBox.prompt('请输入代付人手机号码或昵称:', '他人代付', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /^1[3456789]\d{9}$|^[\u4e00-\u9fa5]{2,8}$/,
      inputErrorMessage: '请输入有效的手机号码或2-8位中文昵称'
    })
      .then(({ value }) => {
        submitting.value = true
        sessionStorage.removeItem('pendingOrder')
        ElMessage.success(`代付请求已发送给${value}！`)
        setTimeout(() => {
          submitting.value = false
          router.push('/user/home/orders')
        }, 1500)
      })
      .catch(() => {
        ElMessage.info('已取消代付')
      })
    return
  }

  // 检查余额
  const userId = String(authStore.userId || '').trim()
  if (!userId) {
    ElMessage.error('用户未登录，请重新登录')
    return
  }

  let latestBalance = platformBalance.value
  try {
    latestBalance = await loadBalance(userId)
  } catch (error) {
    console.error('刷新余额失败:', error)
    ElMessage.error('余额获取失败，请稍后重试')
    return
  }

  if (platformBalance.value < finalAmount.value) {
    ElMessage.error(
      `余额不足！当前余额：¥${latestBalance.toFixed(2)}，需要：¥${finalAmount.value.toFixed(2)}`
    )
    return
  }

  // 普通支付流程
  ElMessageBox.confirm('请确认订单信息无误后支付', '订单确认', {
    confirmButtonText: '立即支付',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        submitting.value = true

        if (isGroupOrder.value && fromChat.value) {
          await payGroupOrder(userId, methodId)
          return
        }

        // 支付密码：后端强制校验，缺失或错误一律 400。
        // 只在本次请求内持有，不落 localStorage、不写日志。
        let paymentPassword
        try {
          const { value } = await ElMessageBox.prompt('请输入 6 位支付密码', '支付验证', {
            confirmButtonText: '确认支付',
            cancelButtonText: '取消',
            inputType: 'password',
            inputPattern: /^\d{6}$/,
            inputErrorMessage: '支付密码为 6 位数字'
          })
          paymentPassword = value
        } catch {
          submitting.value = false
          ElMessage.info('已取消支付')
          return
        }

        console.log('开始创建订单...', {
          orderId: orderInfo.value.orderId,
          userId: String(userId),
          merchantId: String(merchantId.value),
          totalAmount: finalAmount.value,
          items: orderInfo.value.unpaidItems,
          couponId: selectedDiscount.value?.id || null,
          couponAmount: discountAmount.value || 0,
          orderRemark: orderRemark.value
        })

        // 将购物车项转换为订单菜品格式
        const dishes = orderInfo.value.unpaidItems.map((item) => ({
          dishId: String(item.id || item.dishId),
          quantity: item.quantity,
          price: item.price,
          customization: item.note || item.customization || null
        }))

        // 先创建订单(包含菜品列表)
        // 构建订单备注（合并优惠券信息和用户备注）
        let orderRemarkText = ''
        if (selectedDiscount.value) {
          orderRemarkText += `使用优惠券: ${selectedDiscount.value.name}`
        }
        if (orderRemark.value) {
          if (orderRemarkText) {
            orderRemarkText += ' | '
          }
          orderRemarkText += orderRemark.value
        }

        const createOrderResponse = await orderApi.createOrder({
          order: {
            id: null,  // 不传ID，由后端IdGenerator统一生成
            userId: String(userId),
            merchantId: String(merchantId.value),
            totalAmount: finalAmount.value,
            status: 0, // 0-待支付
            address: '到店自取',
            remark: orderRemarkText || null
          },
          dishes: dishes
        })

        console.log('创建订单响应:', createOrderResponse)

        if (createOrderResponse.code !== '200') {
          submitting.value = false
          ElMessage.error(createOrderResponse.message || '创建订单失败')
          return
        }

        // 使用后端返回的订单ID（后端IdGenerator生成的唯一ID）
        const actualOrderId = createOrderResponse.data
        console.log('订单创建成功，订单ID:', actualOrderId)

        // 调用支付API
        const response = await paymentApi.payOrder(
          actualOrderId,
          String(userId),
          'wallet',
          paymentPassword
        )
        console.log('支付结果:', response)

        if (response.code === '200') {
          sessionStorage.removeItem('pendingOrder')
          ElMessage.success('支付成功！您的订单正在处理中')

          // 更新余额
          await loadBalance(userId)

          setTimeout(() => {
            submitting.value = false
            router.push('/user/home/orders')
          }, 1500)
        } else {
          submitting.value = false
          ElMessage.error(response.message || '支付失败，请重试')
        }
      } catch (error) {
        console.error('支付失败:', error)
        submitting.value = false
        ElMessage.error(error.message || '支付失败，请重试')
      }
    })
    .catch(() => {
      ElMessage.info('已取消支付')
    })
}
</script>

<style scoped lang="less">
.order-confirmation-container {
  min-height: 100vh;
  background: var(--canvas);
  color: var(--ink-900);
  font-family: var(--font-sans);
  /* 底部固定支付栏高度 + 滚动安全距离，最后一张卡片不被吸底栏遮挡（§4.4 / §4.6） */
  padding-bottom: calc(80px + var(--space-6));

  .page-header {
    background: var(--surface);
    border-bottom: 1px solid var(--line);
    position: sticky;
    top: 0;
    z-index: 100;

    .header-content {
      max-width: var(--page-max);
      margin: 0 auto;
      padding: var(--space-6) var(--page-gutter);
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: var(--space-4);

      .page-title {
        font-size: var(--text-h1);
        line-height: 32px;
        font-weight: var(--weight-semibold);
        letter-spacing: var(--tracking-tight);
        margin: 0;
        color: var(--ink-900);
        display: flex;
        align-items: center;
        gap: var(--space-3);

        .chat-tag {
          font-size: var(--text-body);
        }
      }
    }
  }

  .main-content {
    max-width: var(--page-max);
    margin: var(--space-6) auto;
    padding: 0 var(--page-gutter);

    .content-left {
      display: flex;
      flex-direction: column;
      gap: var(--space-5);
    }
  }

  // 卡片统一 L1 微质感：--surface + 1px --line + 顶部内高光（§3.4 / §4.2）
  .info-card {
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-lg);
    box-shadow: inset 0 1px 0 color-mix(in srgb, var(--surface) 60%, transparent);

    :deep(.el-card__header) {
      padding: var(--space-4) var(--space-5);
      border-bottom: 1px solid var(--line);
    }

    :deep(.el-card__body) {
      padding: var(--space-5);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: var(--text-h3);
        font-weight: var(--weight-semibold);
        color: var(--ink-900);
      }
    }
  }

  // 商家信息卡片
  .merchant-card {
    .merchant-info {
      display: flex;
      gap: var(--space-4);
      align-items: center;

      .merchant-avatar {
        width: 56px;
        height: 56px;
        background: var(--brand-500);
        border-radius: var(--radius-lg);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--surface);
      }

      .merchant-details {
        flex: 1;

        .merchant-name {
          font-size: var(--text-h3);
          font-weight: var(--weight-semibold);
          color: var(--ink-900);
          margin-bottom: var(--space-1);
        }

        .merchant-meta {
          display: flex;
          align-items: center;
          gap: var(--space-2);
          font-size: var(--text-body);
          color: var(--ink-500);

          .separator {
            color: var(--line-strong);
          }

          .pickup-type {
            color: var(--ink-500);
          }
        }
      }
    }
  }

  // 自取提示：中性信息块，不再用蓝色作强调（§2 冲突裁决）
  .pickup-card {
    background: var(--canvas-sunken);
    border: 1px solid var(--line);
    box-shadow: none;

    .pickup-info {
      display: flex;
      gap: var(--space-4);
      align-items: flex-start;

      .pickup-icon {
        width: 48px;
        height: 48px;
        background: var(--surface);
        border: 1px solid var(--line);
        border-radius: var(--radius-lg);
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--ink-500);
        flex-shrink: 0;
      }

      .pickup-details {
        flex: 1;

        .pickup-title {
          font-size: var(--text-h3);
          font-weight: var(--weight-semibold);
          color: var(--ink-900);
          margin-bottom: var(--space-1);
        }

        .pickup-desc {
          font-size: var(--text-body);
          color: var(--ink-500);
          margin-bottom: var(--space-1);
          line-height: 1.5;
        }

        .pickup-time {
          font-size: var(--text-body-sm);
          color: var(--ink-500);
          display: flex;
          align-items: center;
          gap: var(--space-1);
        }
      }
    }
  }

  // 订单概览
  .order-summary {
    .summary-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: var(--space-3) 0;
      border-bottom: 1px solid var(--line);

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: var(--ink-500);
        font-size: var(--text-body);
      }

      .value {
        color: var(--ink-900);
        font-weight: var(--weight-medium);
      }

      &.amount-row {
        display: flex;
        justify-content: space-around;
        padding: var(--space-5) 0;
        margin-top: var(--space-2);
        background: var(--canvas);
        border-radius: var(--radius-sm);

        .amount-item {
          text-align: center;
          flex: 1;

          .amount-label {
            font-size: var(--text-body-sm);
            color: var(--ink-500);
            margin-bottom: var(--space-2);
          }

          .amount-value {
            font-size: var(--text-display);
            font-weight: var(--weight-bold);
            color: var(--ink-900);

            &.paid {
              color: var(--ink-900);
            }

            &.unpaid {
              color: var(--ink-900);
            }
          }
        }

        .amount-divider {
          width: 1px;
          background: var(--line);
        }
      }
    }
  }

  // 订单商品
  .order-section {
    &.mt-20 {
      margin-top: var(--space-5);
    }

    .section-header {
      display: flex;
      align-items: center;
      gap: var(--space-2);
      margin-bottom: var(--space-4);

      .section-title {
        font-size: var(--text-body);
        font-weight: var(--weight-semibold);
        color: var(--ink-900);
        flex: 1;
      }
    }
  }

  // 语义图标只做小面积提示（§3.1）
  .icon-success {
    color: var(--success);
  }

  .icon-warning {
    color: var(--warning);
  }

  .icon-muted {
    color: var(--ink-400);
  }

  // 支付方式
  .payment-methods {
    display: flex;
    flex-direction: column;
    gap: var(--space-3);

    .payment-method-item {
      display: flex;
      align-items: center;
      gap: var(--space-3);
      padding: var(--space-4);
      border: 2px solid var(--line);
      border-radius: var(--radius-md);
      cursor: pointer;
      transition:
        border-color var(--duration-fast) var(--ease-out),
        background var(--duration-fast) var(--ease-out);

      &:hover {
        border-color: var(--brand-300);
        background: var(--canvas);
      }

      &.active {
        border-color: var(--brand-500);
        background: var(--brand-50);
      }

      .method-icon {
        font-size: var(--text-h2);
      }

      .method-info {
        flex: 1;

        .method-name {
          font-size: var(--text-body);
          font-weight: var(--weight-medium);
          color: var(--ink-900);
        }

        .method-desc {
          font-size: var(--text-body-sm);
          color: var(--ink-500);
          margin-top: var(--space-1);
        }
      }
    }
  }

  // 订单备注卡片
  .order-remark-section {
    .order-remark-textarea {
      margin-bottom: var(--space-3);

      :deep(.el-textarea__inner) {
        border-radius: var(--radius-sm);
        font-size: var(--text-body);
        padding: var(--space-3);
        background: var(--canvas);
        transition: border-color var(--duration-fast) var(--ease-out);

        &:focus {
          background: var(--surface);
          border-color: var(--brand-500);
          box-shadow: 0 0 0 2px var(--brand-100);
        }

        &::placeholder {
          color: var(--ink-400);
          font-size: var(--text-body-sm);
        }
      }

      :deep(.el-input__count) {
        font-size: var(--text-caption);
        color: var(--ink-400);
        background: transparent;
      }
    }

    .quick-remarks {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: var(--space-2);
      padding-top: var(--space-2);
      border-top: 1px solid var(--line);

      .quick-remark-label {
        font-size: var(--text-body-sm);
        color: var(--ink-500);
        font-weight: var(--weight-medium);
      }

      .quick-remark-tag {
        cursor: pointer;
        transition:
          background var(--duration-fast) var(--ease-out),
          color var(--duration-fast) var(--ease-out),
          border-color var(--duration-fast) var(--ease-out);
        user-select: none;

        &:hover {
          background: var(--brand-500);
          color: var(--surface);
          border-color: var(--brand-500);
        }
      }
    }
  }

  // 支付明细卡片：原价 / 优惠 / 实付三段关系（§5.2 金额不染色、tabular-nums）
  .payment-summary-card {
    .payment-details {
      .detail-row {
        display: flex;
        justify-content: space-between;
        align-items: baseline;
        gap: var(--space-4);
        padding: var(--space-2) 0;

        .detail-label {
          color: var(--ink-500);
          font-size: var(--text-body);
        }

        .detail-value {
          color: var(--ink-900);
          font-family: var(--font-num);
          font-variant-numeric: tabular-nums;
          font-weight: var(--weight-medium);
          white-space: nowrap;
        }

        &.total-row {
          padding-top: var(--space-3);

          .total-label {
            font-size: var(--text-h3);
            font-weight: var(--weight-semibold);
            color: var(--ink-900);
          }

          .total-value {
            font-size: var(--text-display);
            line-height: 40px;
            font-weight: var(--weight-bold);
            color: var(--ink-900);
            font-family: var(--font-num);
            font-variant-numeric: tabular-nums;
            white-space: nowrap;
          }
        }
      }
    }

    .coupon-section {
      margin: var(--space-4) 0;

      .coupon-item {
        display: flex;
        flex-direction: column;
        gap: var(--space-3);
        padding: var(--space-4);
        background: var(--warning-bg);
        border: 1px solid color-mix(in srgb, var(--warning) 30%, transparent);
        border-radius: var(--radius-lg);
        margin-bottom: var(--space-3);
        transition: border-color var(--duration-fast) var(--ease-out);

        &:last-child {
          margin-bottom: 0;
        }

        &:hover {
          border-color: color-mix(in srgb, var(--warning) 60%, transparent);
        }

        .coupon-info-main {
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: var(--space-3);

          .coupon-left {
            flex: 1;
            display: flex;
            align-items: center;
            gap: var(--space-3);

            .el-icon {
              font-size: var(--text-h2);
            }

            .coupon-details {
              display: flex;
              flex-direction: column;
              gap: var(--space-1);

              .coupon-name {
                font-size: var(--text-body);
                font-weight: var(--weight-semibold);
                color: var(--ink-900);
              }

              .coupon-desc {
                font-size: var(--text-caption);
                color: var(--ink-500);
              }
            }
          }

          .coupon-amount {
            font-size: var(--text-h3);
            font-weight: var(--weight-bold);
            color: var(--ink-900);
            font-family: var(--font-num);
            font-variant-numeric: tabular-nums;
            white-space: nowrap;
          }
        }
      }
    }

    .balance-info {
      .balance-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--space-2) var(--space-3);
        background: var(--canvas-sunken);
        border-radius: var(--radius-sm);

        .balance-label {
          font-size: var(--text-body-sm);
          color: var(--ink-500);
        }

        .balance-value {
          font-size: var(--text-body);
          font-weight: var(--weight-semibold);
          color: var(--ink-900);
          font-family: var(--font-num);
          font-variant-numeric: tabular-nums;
        }
      }
    }
  }

  // 支付提示
  .payment-tips {
    display: flex;
    align-items: center;
    gap: var(--space-1);
    padding: var(--space-3);
    background: var(--canvas);
    border-radius: var(--radius-sm);
    font-size: var(--text-body-sm);
    color: var(--ink-500);
    text-align: center;
    justify-content: center;
  }
}

// 底部提交栏：全屏唯一主动作（§4.1），吸底不遮挡内容（§4.6）
.bottom-submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: var(--surface);
  border-top: 1px solid var(--line);
  box-shadow: var(--shadow-md);

  .submit-bar-content {
    max-width: var(--page-max);
    margin: 0 auto;
    padding: var(--space-4) var(--page-gutter);
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: var(--space-6);

    @media (max-width: 768px) {
      flex-direction: column;
      gap: var(--space-3);
    }

    .submit-bar-left {
      flex: 1;

      .payment-tips {
        display: flex;
        align-items: center;
        gap: var(--space-1);
        font-size: var(--text-body);
        color: var(--ink-500);
        text-align: left;
        justify-content: flex-start;
        padding: 0;
        background: transparent;
        border-radius: 0;
      }
    }

    .submit-bar-right {
      display: flex;
      align-items: center;
      gap: var(--space-5);

      @media (max-width: 768px) {
        width: 100%;
        justify-content: space-between;
      }

      .total-info {
        text-align: right;

        @media (max-width: 768px) {
          flex: 1;
        }

        .total-label {
          font-size: var(--text-body);
          color: var(--ink-500);
          margin-right: var(--space-2);
        }

        .total-amount {
          font-size: var(--text-display);
          line-height: 40px;
          font-weight: var(--weight-bold);
          color: var(--ink-900);
          font-family: var(--font-num);
          font-variant-numeric: tabular-nums;
          white-space: nowrap;
        }
      }

      .submit-button-fixed {
        min-width: 160px;
        height: var(--control-height-md);
        font-size: var(--text-h3);
        font-weight: var(--weight-semibold);
        border-radius: var(--radius-md);
        background: var(--brand-500);
        border: none;
        box-shadow: none;

        &:hover {
          background: var(--brand-600);
        }

        @media (max-width: 768px) {
          min-width: 140px;
          height: var(--control-height-md);
          font-size: var(--text-body);
        }
      }
    }
  }
}

// 金额变化一次性反馈：时长/缓动取自 token（§3.5）
@keyframes amount-feedback {
  0% {
    opacity: 0.45;
    transform: translateY(2px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.amount-updated {
  animation: amount-feedback var(--refresh-feedback) var(--ease-spring);
}

// 卡片 / 分区首次进入：只播一次，时长/缓动取自 token（§3.5）
@keyframes enter-rise {
  from {
    opacity: 0;
    transform: translateY(4px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.enter {
  animation: enter-rise var(--duration-base) var(--ease-out) both;
}

// 购物车弹窗
.cart-items {
  max-height: 400px;
  overflow-y: auto;

  .cart-item {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: var(--space-3);
    border: 1px solid var(--line);
    border-radius: var(--radius-sm);
    margin-bottom: var(--space-3);

    &:last-child {
      margin-bottom: 0;
    }

    .cart-item-main {
      flex: 1;
      padding-right: var(--space-3);

      .item-name {
        font-size: var(--text-body);
        font-weight: var(--weight-medium);
        color: var(--ink-900);
        margin-bottom: var(--space-1);
      }

      .item-price {
        font-family: var(--font-num);
        font-variant-numeric: tabular-nums;
        font-size: var(--text-body-sm);
        color: var(--ink-500);
        margin-bottom: var(--space-2);
      }

      .item-specs {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-1);
        margin-bottom: var(--space-2);

        .ingredient-tag {
          font-size: var(--text-caption);
        }
      }

      .item-note-edit {
        margin-top: var(--space-2);

        :deep(.el-textarea__inner) {
          border-radius: var(--radius-sm);
          font-size: var(--text-body-sm);
          padding: var(--space-2) var(--space-3);
          background: var(--canvas);
          transition: border-color var(--duration-fast) var(--ease-out);

          &:focus {
            background: var(--surface);
            border-color: var(--brand-500);
          }

          &::placeholder {
            color: var(--ink-400);
            font-size: var(--text-caption);
          }
        }

        :deep(.el-input__count) {
          font-size: var(--text-caption);
          color: var(--ink-400);
          background: transparent;
        }
      }
    }

    .cart-item-controls {
      display: flex;
      align-items: center;
      gap: var(--space-3);

      .item-total {
        font-family: var(--font-num);
        font-variant-numeric: tabular-nums;
        font-size: var(--text-h3);
        font-weight: var(--weight-semibold);
        color: var(--ink-900);
        min-width: 80px;
        text-align: right;
      }
    }
  }
}

.empty-cart {
  padding: var(--space-10) 0;
}

// AA支付模态框
.aa-payment-content {
  padding: var(--space-5) 0;

  .aa-info {
    .info-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: var(--space-4);
      padding: var(--space-3);
      background: var(--canvas);
      border-radius: var(--radius-sm);

      &:last-child {
        margin-bottom: 0;
      }

      .info-label {
        font-weight: var(--weight-medium);
        color: var(--ink-500);
      }

      .info-value {
        font-family: var(--font-num);
        font-variant-numeric: tabular-nums;
        font-size: var(--text-h3);
        color: var(--ink-900);
        font-weight: var(--weight-medium);

        &.highlight {
          color: var(--ink-900);
          font-weight: var(--weight-bold);
          font-size: var(--text-h2);
        }
      }
    }
  }
}

// 自定义分摊模态框
.custom-share-content {
  padding: var(--space-5) 0;

  .custom-info {
    margin-bottom: var(--space-5);

    .info-item {
      display: flex;
      justify-content: space-between;
      padding: var(--space-3);
      background: var(--canvas);
      border-radius: var(--radius-sm);

      .info-label {
        font-weight: var(--weight-medium);
        color: var(--ink-500);
      }

      .info-value {
        font-family: var(--font-num);
        font-variant-numeric: tabular-nums;
        font-size: var(--text-h3);
        color: var(--ink-900);
        font-weight: var(--weight-medium);
      }
    }
  }

  .share-list {
    .share-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: var(--space-3);
      border: 1px solid var(--line);
      border-radius: var(--radius-sm);
      margin-bottom: var(--space-3);

      &:last-child {
        margin-bottom: 0;
      }

      .member-name {
        font-weight: var(--weight-medium);
        color: var(--ink-900);
      }
    }
  }
}
</style>
