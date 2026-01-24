# Chat.vue 加菜功能集成指南

本指南说明如何将加菜功能集成到 Chat.vue 中。

## 一、Import 语句添加（约267行）

在import GroupOrderDrawer后添加：

```javascript
import GroupOrderDrawer from '../../components/chat/dialogs/GroupOrderDrawer.vue'
import AddDishDialog from '../../components/chat/dialogs/AddDishDialog.vue'
import AddDishReviewPanel from '../../components/chat/dialogs/AddDishReviewPanel.vue'
```

## 二、Template中添加组件（约162-170行）

在 GroupOrderDrawer 标签后添加：

```vue
<GroupOrderDrawer
  v-model="orderDrawerVisible"
  :group-order="currentGroupOrder"
  :current-user-id="userId"
  :pending-review-count="pendingReviewCount"
  :pending-payment-count="pendingPaymentCount"
  @change-merchant="changeMerchant"
  @continue-order="openMerchantSelectDialog"
  @select-merchant="openMerchantSelectDialog"
  @go-to-pay="goToOrderConfirmation"
  @open-add-dish-dialog="openAddDishDialog"
  @open-add-dish-review="openAddDishReview"
  @open-pending-payment="openPendingPayment"
/>

<!-- 加菜对话框 -->
<AddDishDialog
  v-model="addDishDialogVisible"
  :group-order-id="currentGroupOrderId"
  :ordered-dishes="orderedDishes"
  :available-dishes="availableDishes"
  :allergy-conflicts="allergyConflicts"
  @success="handleAddDishSuccess"
/>

<!-- 加菜审核面板 -->
<AddDishReviewPanel
  v-model="addDishReviewVisible"
  :group-order-id="currentGroupOrderId"
  @refresh="loadPendingReviewCount"
/>
```

## 三、状态变量添加（约391行后）

在群订单管理部分添加：

```javascript
// ========== 加菜功能 ==========
const addDishDialogVisible = ref(false)
const addDishReviewVisible = ref(false)
const pendingReviewCount = ref(0)
const pendingPaymentCount = ref(0)

// 当前群订单ID
const currentGroupOrderId = computed(() => {
  return currentGroupOrder.value?.id || null
})

// 已点菜品列表
const orderedDishes = ref([])

// 可用菜品列表
const availableDishes = ref([])

// 饮食禁忌冲突
const allergyConflicts = ref([])

// 加载已点菜品
const loadOrderedDishes = async () => {
  if (!currentGroupOrderId.value) return

  try {
    // 从群订单中获取已点菜品
    orderedDishes.value = currentGroupOrder.value?.orderItems || []
  } catch (error) {
    console.error('加载已点菜品失败:', error)
  }
}

// 加载可用菜品
const loadAvailableDishes = async () => {
  if (!currentGroupOrder.value?.merchantId) return

  try {
    const response = await api.get(`/v1/dishes/merchant/${currentGroupOrder.value.merchantId}`)
    availableDishes.value = response.data.data || []
  } catch (error) {
    console.error('加载可用菜品失败:', error)
  }
}

// 检查饮食禁忌冲突
const checkAllergyConflicts = async (dishItems) => {
  try {
    const response = await api.post('/v1/add-dish/check-allergy', {
      groupOrderId: currentGroupOrderId.value,
      dishItems: dishItems.map(dish => ({
        dishId: dish.dishId,
        quantity: dish.quantity
      }))
    })

    if (response.data.data?.hasConflict) {
      allergyConflicts.value = response.data.data.conflicts || []
    } else {
      allergyConflicts.value = []
    }
  } catch (error) {
    console.error('检查饮食禁忌失败:', error)
    allergyConflicts.value = []
  }
}

// 打开加菜对话框
const openAddDishDialog = async () => {
  await loadOrderedDishes()
  await loadAvailableDishes()
  addDishDialogVisible.value = true
}

// 打开审核面板
const openAddDishReview = () => {
  addDishReviewVisible.value = true
}

// 打开待支付池（可选功能）
const openPendingPayment = () => {
  ElMessage.info('待支付加菜池功能开发中')
}

// 加菜成功回调
const handleAddDishSuccess = async () => {
  ElMessage.success('加菜请求已提交')
  await loadPendingReviewCount()
  await loadOrderedDishes()
}

// 加载待审核数量
const loadPendingReviewCount = async () => {
  if (!currentGroupOrderId.value) return

  try {
    const response = await api.get(`/v1/add-dish/review-list/${currentGroupOrderId.value}`)
    const reviewList = response.data.data || []
    pendingReviewCount.value = reviewList.length
  } catch (error) {
    console.error('加载待审核数量失败:', error)
  }
}
```

## 四、在GroupOrderDrawer打开时加载数据

在打开群订单抽屉的函数中添加：

```javascript
watch(orderDrawerVisible, async (newVal) => {
  if (newVal) {
    await loadPendingReviewCount()
    await loadOrderedDishes()
  }
})
```

## 五、在群聊消息按钮区域添加加菜按钮（可选）

如果需要在群聊界面添加快速加菜按钮，可以在消息输入区域添加：

```vue
<el-button
  v-if="isGroupChat && hasGroupOrder"
  type="warning"
  size="small"
  @click="openAddDishDialog"
>
  <el-icon><Dish /></el-icon>
  加菜
</el-button>
```

## 六、导入图标

在import语句中添加：

```javascript
import { Dish, DocumentChecked } from '@element-plus/icons-vue'
```

## 七、测试验证

完成上述修改后，测试以下功能：

1. ✅ 在群订单抽屉中看到"加菜功能"区域
2. ✅ 点击"我要加菜"按钮打开加菜对话框
3. ✅ 发起者可以看到"查看审核"按钮
4. ✅ 提交加菜请求后，待审核数量徽章更新
5. ✅ 打开审核面板可以看到待审核列表

## 注意事项

1. **确保数据格式匹配**：检查 `currentGroupOrder.value` 的数据结构
2. **错误处理**：所有API调用都包含 try-catch
3. **响应式更新**：使用 `computed` 和 `watch` 确保数据实时更新
4. **权限控制**：审核功能仅对发起者可见
5. **状态同步**：加菜成功后刷新相关数据

## 调试技巧

如果遇到问题，检查以下几点：

1. **console.log** 查看数据结构：
   ```javascript
   console.log('群订单数据:', currentGroupOrder.value)
   console.log('用户ID:', userId.value)
   ```

2. **网络请求**：
   - 打开浏览器开发者工具 → Network
   - 查看 API 请求是否成功
   - 检查请求参数和响应数据

3. **组件状态**：
   - 使用 Vue DevTools 查看组件状态
   - 检查 ref 和 computed 的值

4. **事件触发**：
   - 检查 emit 事件是否正确绑定
   - 确认事件名称与组件定义一致

---

**完成时间估算**：30-45分钟
**难度等级**：中等
**所需技能**：Vue 3基础、Element Plus使用、异步数据加载
