<template>
  <div class="order-confirmation-container">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="header-content">
        <common-back-button type="text" size="small" />
        <h2 class="page-title">
          <template v-if="fromChat">
            <el-tag :type="fromSingleChat ? 'primary' : 'success'" size="large" class="chat-tag">
              {{ fromSingleChat ? '单聊' : '群聊' }}
            </el-tag>
            订单确认
          </template>
          <template v-else> 订单确认 </template>
        </h2>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧主要内容区 -->
      <div class="content-left">
        <!-- 商家信息卡片 -->
        <el-card class="info-card merchant-card" shadow="hover">
          <div class="merchant-info">
            <div class="merchant-avatar">
              <el-icon :size="40"><Shop /></el-icon>
            </div>
            <div class="merchant-details">
              <div class="merchant-name">{{ merchantInfo.name }}</div>
              <div class="merchant-meta">
                <span class="rating">{{ merchantInfo.rating }} 分</span>
                <span class="separator">|</span>
                <span class="delivery-time">{{ merchantInfo.deliveryTime }}</span>
                <span class="separator">|</span>
                <span class="delivery-fee">配送 ¥{{ merchantInfo.deliveryFee }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 订单概览卡片 -->
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单概览</span>
              <el-tag type="info" size="small">{{ orderInfo.orderId }}</el-tag>
            </div>
          </template>

          <div class="order-summary">
            <div class="summary-row">
              <span class="label">{{ isGroupOrder ? '群聊名称' : '下单用户' }}</span>
              <span class="value">{{
                isGroupOrder ? orderInfo.groupName : orderInfo.userName || '未知用户'
              }}</span>
            </div>
            <div class="summary-row amount-row">
              <div class="amount-item">
                <div class="amount-label">已支付</div>
                <div class="amount-value paid">¥{{ orderInfo.totalPaid.toFixed(2) }}</div>
              </div>
              <div class="amount-divider"></div>
              <div class="amount-item">
                <div class="amount-label">待支付</div>
                <div class="amount-value unpaid">¥{{ orderInfo.totalUnpaid.toFixed(2) }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 订单商品卡片 -->
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单商品</span>
              <el-button type="text" size="small" @click="cartVisible = true">
                <el-icon><Edit /></el-icon>
                编辑订单
              </el-button>
            </div>
          </template>

          <!-- 已支付订单 -->
          <div v-if="orderInfo.paidItems.length > 0" class="order-section">
            <div class="section-header">
              <el-icon color="#67c23a"><CircleCheck /></el-icon>
              <span class="section-title">已支付商品</span>
              <el-tag type="success" size="small" effect="plain">不可修改</el-tag>
            </div>
            <order-item-list :items="orderInfo.paidItems" :show-payment-info="true" />
          </div>

          <!-- 未支付订单 -->
          <div class="order-section" :class="{ 'mt-20': orderInfo.paidItems.length > 0 }">
            <div class="section-header">
              <el-icon color="#e6a23c"><Clock /></el-icon>
              <span class="section-title">待支付商品</span>
              <el-tag type="warning" size="small" effect="plain">可编辑</el-tag>
            </div>
            <order-item-list :items="orderInfo.unpaidItems" :show-payment-info="false" />
          </div>
        </el-card>

        <!-- 支付方式卡片 -->
        <el-card class="info-card" shadow="hover">
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

      <!-- 右侧支付信息区 -->
      <div class="content-right">
        <el-card class="payment-summary-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">支付明细</span>
            </div>
          </template>

          <div class="payment-details">
            <div class="detail-row">
              <span class="detail-label">商品总额</span>
              <span class="detail-value">¥{{ orderInfo.totalUnpaid.toFixed(2) }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">配送费</span>
              <span class="detail-value">¥{{ merchantInfo.deliveryFee }}</span>
            </div>
            <div class="detail-row" v-if="discountApplied">
              <span class="detail-label">优惠减免</span>
              <span class="detail-value discount">-¥{{ discountAmount }}</span>
            </div>

            <el-divider></el-divider>

            <div class="detail-row total-row">
              <span class="total-label">应付金额</span>
              <span class="total-value">¥{{ finalAmount.toFixed(2) }}</span>
            </div>
          </div>

          <!-- 优惠券 -->
          <div class="coupon-section" v-if="discounts.length > 0">
            <div class="coupon-item" v-for="discount in discounts" :key="discount.id">
              <div class="coupon-info">
                <el-icon color="#e6a23c"><Ticket /></el-icon>
                <span class="coupon-name">{{ discount.name }}</span>
              </div>
              <el-button
                v-if="!discount.used"
                type="warning"
                size="small"
                plain
                @click="useDiscount"
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

        <!-- 提交订单按钮 -->
        <el-button
          type="primary"
          size="large"
          class="submit-button"
          @click="confirmOrder"
          :loading="submitting"
        >
          {{ submitButtonText }}
        </el-button>

        <!-- 支付规则提示 -->
        <div class="payment-tips">
          <el-icon color="#909399" :size="14"><InfoFilled /></el-icon>
          <span>已支付订单不可修改，仅支付未支付部分</span>
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
          </div>
          <div class="cart-item-controls">
            <el-input-number
              v-model="item.quantity"
              :min="0"
              :max="99"
              size="small"
              @change="updateItemTotal(item)"
            />
            <div class="item-total">¥{{ (item.totalPrice || item.price * item.quantity).toFixed(2) }}</div>
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
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  Shop,
  Edit,
  CircleCheck,
  Clock,
  Ticket,
  InfoFilled
} from '@element-plus/icons-vue'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import OrderItemList from './components/OrderItemList.vue'

const router = useRouter()

// 从会话存储获取订单信息
const pendingOrder = JSON.parse(sessionStorage.getItem('pendingOrder')) || {}

// 检查订单是否为空
const isEmptyOrder = !pendingOrder.cartItems || pendingOrder.cartItems.length === 0

// 订单信息
const orderInfo = ref({
  orderId: `JD${new Date().getTime()}`,
  groupName: pendingOrder.groupName || '默认订单群',
  userName: pendingOrder.userName || '',
  creator: pendingOrder.creator || '',
  paidItems: [],
  unpaidItems: pendingOrder.cartItems || [],
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

// 商家信息
const merchantInfo = ref({
  name: '佳食优选餐厅',
  rating: 4.8,
  deliveryTime: '约30分钟',
  deliveryFee: 5.0
})

// 购物车数据
const cartItems = ref(pendingOrder.cartItems || [])
const cartVisible = ref(false)

// 检测订单类型
const isGroupOrder = ref(orderInfo.value.groupName !== '默认订单群')
const fromChat = ref(pendingOrder.fromChat || false)
const fromSingleChat = ref(fromChat.value && !isGroupOrder.value)

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

// 平台币余额
const platformBalance = ref(125.0)

// 优惠券
const discounts = ref([
  {
    id: 1,
    name: '新用户专享50元优惠券',
    amount: 50.0,
    available: true,
    used: false
  }
])

const selectedDiscount = ref(null)
const discountApplied = computed(() => selectedDiscount.value !== null)
const discountAmount = computed(() => {
  return selectedDiscount.value ? selectedDiscount.value.amount : 0
})

// 最终金额（注意：orderInfo.totalUnpaid 已经在 useDiscount 中被修改过了）
const finalAmount = computed(() => {
  return orderInfo.value.totalUnpaid + merchantInfo.value.deliveryFee
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
const useDiscount = () => {
  const discount = discounts.value[0]
  if (!discount || !discount.available || discount.used) return

  selectedDiscount.value = discount
  discount.used = true

  if (!orderInfo.value.originalTotal) {
    orderInfo.value.originalTotal = orderInfo.value.totalUnpaid
  }

  const discountAmount = Math.min(discount.amount, orderInfo.value.totalUnpaid)
  orderInfo.value.totalUnpaid -= discountAmount

  ElMessage.success('优惠已使用')
}

// 取消使用优惠
const cancelDiscount = () => {
  if (!selectedDiscount.value) return

  const discountAmount = Math.min(
    selectedDiscount.value.amount,
    orderInfo.value.totalUnpaid + selectedDiscount.value.amount
  )
  orderInfo.value.totalUnpaid += discountAmount

  delete orderInfo.value.originalTotal

  selectedDiscount.value.used = false
  selectedDiscount.value = null

  ElMessage.success('优惠已取消')
}

// 关闭购物车
const closeCart = () => {
  cartVisible.value = false
}

// 更新单项总价
const updateItemTotal = (item) => {
  item.totalPrice = item.price * item.quantity
}

// 更新订单信息
const updateOrderInfo = () => {
  orderInfo.value.unpaidItems = cartItems.value.filter(item => item.quantity > 0)
  orderInfo.value.totalUnpaid = cartItems.value
    .filter(item => item.quantity > 0)
    .reduce((total, item) => total + (item.totalPrice || item.price * item.quantity), 0)

  const updatedOrder = { ...pendingOrder }
  updatedOrder.cartItems = cartItems.value.filter(item => item.quantity > 0)
  updatedOrder.totalAmount = orderInfo.value.totalUnpaid
  sessionStorage.setItem('pendingOrder', JSON.stringify(updatedOrder))

  closeCart()
  ElMessage.success('订单已更新')
}

// 确认订单
const confirmOrder = () => {
  const methodId = selectedPaymentMethod.value.id

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

  // 普通支付流程
  ElMessageBox.confirm('请确认订单信息无误后支付', '订单确认', {
    confirmButtonText: '立即支付',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      submitting.value = true
      sessionStorage.removeItem('pendingOrder')
      ElMessage.success('支付成功！您的订单正在处理中')
      setTimeout(() => {
        submitting.value = false
        router.push('/user/home/orders')
      }, 1500)
    })
    .catch(() => {
      ElMessage.info('已取消支付')
    })
}
</script>

<style scoped lang="less">
.order-confirmation-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 40px;

  .page-header {
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    position: sticky;
    top: 0;
    z-index: 100;

    .header-content {
      max-width: 1200px;
      margin: 0 auto;
      padding: 16px 24px;
      display: flex;
      align-items: center;
      gap: 16px;

      .page-title {
        font-size: 20px;
        font-weight: 600;
        margin: 0;
        color: #2c3e50;
        display: flex;
        align-items: center;
        gap: 12px;

        .chat-tag {
          font-size: 14px;
        }
      }
    }
  }

  .main-content {
    max-width: 1200px;
    margin: 24px auto;
    padding: 0 24px;
    display: grid;
    grid-template-columns: 1fr 380px;
    gap: 24px;

    @media (max-width: 1024px) {
      grid-template-columns: 1fr;
    }

    .content-left {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .content-right {
      display: flex;
      flex-direction: column;
      gap: 16px;

      @media (max-width: 1024px) {
        order: -1;
      }
    }
  }

  .info-card {
    border-radius: 12px;
    border: none;

    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid #f0f0f0;
    }

    :deep(.el-card__body) {
      padding: 20px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #2c3e50;
      }
    }
  }

  // 商家信息卡片
  .merchant-card {
    .merchant-info {
      display: flex;
      gap: 16px;
      align-items: center;

      .merchant-avatar {
        width: 60px;
        height: 60px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      .merchant-details {
        flex: 1;

        .merchant-name {
          font-size: 18px;
          font-weight: 600;
          color: #2c3e50;
          margin-bottom: 8px;
        }

        .merchant-meta {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          color: #7f8c8d;

          .separator {
            color: #dcdfe6;
          }
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
      padding: 12px 0;
      border-bottom: 1px solid #f5f7fa;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: #7f8c8d;
        font-size: 14px;
      }

      .value {
        color: #2c3e50;
        font-weight: 500;
      }

      &.amount-row {
        display: flex;
        justify-content: space-around;
        padding: 20px 0;
        margin-top: 8px;
        background: #f8f9fa;
        border-radius: 8px;

        .amount-item {
          text-align: center;
          flex: 1;

          .amount-label {
            font-size: 13px;
            color: #7f8c8d;
            margin-bottom: 8px;
          }

          .amount-value {
            font-size: 24px;
            font-weight: 700;

            &.paid {
              color: #67c23a;
            }

            &.unpaid {
              color: #e6a23c;
            }
          }
        }

        .amount-divider {
          width: 1px;
          background: #dcdfe6;
        }
      }
    }
  }

  // 订单商品
  .order-section {
    &.mt-20 {
      margin-top: 20px;
    }

    .section-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;

      .section-title {
        font-size: 15px;
        font-weight: 600;
        color: #2c3e50;
        flex: 1;
      }
    }
  }

  // 支付方式
  .payment-methods {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .payment-method-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      border: 2px solid #e4e7ed;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        border-color: #409eff;
        background: #f5f7fa;
      }

      &.active {
        border-color: #409eff;
        background: rgba(64, 158, 255, 0.05);
      }

      .method-icon {
        font-size: 28px;
      }

      .method-info {
        flex: 1;

        .method-name {
          font-size: 15px;
          font-weight: 500;
          color: #2c3e50;
        }

        .method-desc {
          font-size: 13px;
          color: #7f8c8d;
          margin-top: 4px;
        }
      }
    }
  }

  // 支付明细卡片
  .payment-summary-card {

    .payment-details {
      .detail-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;

        .detail-label {
          color: #7f8c8d;
          font-size: 14px;
        }

        .detail-value {
          color: #2c3e50;
          font-weight: 500;

          &.discount {
            color: #f56c6c;
          }
        }

        &.total-row {
          padding-top: 12px;

          .total-label {
            font-size: 16px;
            font-weight: 600;
            color: #2c3e50;
          }

          .total-value {
            font-size: 28px;
            font-weight: 700;
            color: #e6a23c;
          }
        }
      }
    }

    .coupon-section {
      margin: 16px 0;

      .coupon-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        background: #fff9e6;
        border: 1px solid #ffe58f;
        border-radius: 6px;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .coupon-info {
          display: flex;
          align-items: center;
          gap: 8px;

          .coupon-name {
            font-size: 14px;
            color: #856404;
          }
        }
      }
    }

    .balance-info {
      .balance-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 12px;
        background: #f0f9ff;
        border-radius: 6px;

        .balance-label {
          font-size: 13px;
          color: #7f8c8d;
        }

        .balance-value {
          font-size: 15px;
          font-weight: 600;
          color: #67c23a;
        }
      }
    }
  }

  // 提交按钮
  .submit-button {
    width: 100%;
    height: 50px;
    font-size: 17px;
    font-weight: 600;
    border-radius: 25px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);

    &:hover {
      background: linear-gradient(135deg, #7c8ff0 0%, #865aba 100%);
      box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
      transform: translateY(-2px);
    }

    &:active {
      transform: translateY(0);
    }
  }

  // 支付提示
  .payment-tips {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 6px;
    font-size: 13px;
    color: #7f8c8d;
    text-align: center;
    justify-content: center;
  }
}

// 购物车弹窗
.cart-items {
  max-height: 400px;
  overflow-y: auto;

  .cart-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px;
    border: 1px solid #f0f0f0;
    border-radius: 8px;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    .cart-item-main {
      flex: 1;

      .item-name {
        font-size: 15px;
        font-weight: 500;
        color: #2c3e50;
        margin-bottom: 6px;
      }

      .item-price {
        font-size: 14px;
        color: #7f8c8d;
      }
    }

    .cart-item-controls {
      display: flex;
      align-items: center;
      gap: 12px;

      .item-total {
        font-size: 16px;
        font-weight: 600;
        color: #e6a23c;
        min-width: 80px;
        text-align: right;
      }
    }
  }
}

.empty-cart {
  padding: 40px 0;
}

// AA支付模态框
.aa-payment-content {
  padding: 20px 0;

  .aa-info {
    .info-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 16px;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 6px;

      &:last-child {
        margin-bottom: 0;
      }

      .info-label {
        font-weight: 500;
        color: #7f8c8d;
      }

      .info-value {
        font-size: 16px;
        color: #2c3e50;
        font-weight: 500;

        &.highlight {
          color: #e6a23c;
          font-weight: 600;
          font-size: 20px;
        }
      }
    }
  }
}

// 自定义分摊模态框
.custom-share-content {
  padding: 20px 0;

  .custom-info {
    margin-bottom: 20px;

    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 12px;
      background: #f8f9fa;
      border-radius: 6px;

      .info-label {
        font-weight: 500;
        color: #7f8c8d;
      }

      .info-value {
        font-size: 16px;
        color: #2c3e50;
        font-weight: 500;
      }
    }
  }

  .share-list {
    .share-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      border: 1px solid #f0f0f0;
      border-radius: 6px;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .member-name {
        font-weight: 500;
        color: #2c3e50;
      }
    }
  }
}
</style>
