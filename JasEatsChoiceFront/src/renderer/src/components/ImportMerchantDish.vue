<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import merchantApi from '../api/merchant'

// 接收从父组件传递的 props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  recipe: {
    type: Object,
    default: null
  }
})

// 定义事件
const emit = defineEmits(['close', 'import', 'update:visible'])

// 商家列表和选中商家
const merchants = ref([]) // 商家列表
const selectedMerchant = ref(null)
const merchantDishes = ref([]) // 选中商家的菜品
const selectedMerchantDishes = ref([]) // 选中的导入菜品
const loadingMerchants = ref(false) // 加载商家列表状态
const loadingDishes = ref(false) // 加载菜品列表状态

// 加载商家列表
const loadMerchants = async () => {
  loadingMerchants.value = true
  try {
    const response = await merchantApi.getMerchants({})
    if (response.data && response.data.code === 200) {
      merchants.value = response.data.data || []
    } else {
      ElMessage.error('获取商家列表失败')
    }
  } catch (error) {
    console.error('获取商家列表失败:', error)
    ElMessage.error('获取商家列表失败')
  } finally {
    loadingMerchants.value = false
  }
}

// 加载商家菜品
const loadMerchantDishes = async () => {
  if (!selectedMerchant.value) {
    merchantDishes.value = []
    return
  }

  loadingDishes.value = true
  try {
    const response = await merchantApi.getMerchantDishes(selectedMerchant.value.id)
    if (response.data && response.data.code === 200) {
      merchantDishes.value = response.data.data || []
    } else {
      ElMessage.error('获取菜品列表失败')
      merchantDishes.value = []
    }
  } catch (error) {
    console.error('获取菜品列表失败:', error)
    ElMessage.error('获取菜品列表失败')
    merchantDishes.value = []
  } finally {
    loadingDishes.value = false
  }

  // 重置选中的菜品
  selectedMerchantDishes.value = []
}

// 确认导入商家菜品
const confirmImportMerchantDishes = () => {
  if (selectedMerchantDishes.value.length > 0) {
    if (props.recipe) {
      // 发送导入事件
      emit('import', props.recipe, selectedMerchantDishes.value)

      // 重置状态
      resetState()

      // 关闭对话框
      emit('close')
    } else {
      ElMessage.error('请先选择要导入到的食谱')
    }
  } else {
    ElMessage.warning('请先选择要导入的菜品')
  }
}

// 处理关闭事件
const handleClose = () => {
  resetState()
  emit('close')
}

// 重置状态
const resetState = () => {
  selectedMerchant.value = null
  merchantDishes.value = []
  selectedMerchantDishes.value = []
}

// 监听对话框显示状态，打开时加载商家列表
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadMerchants()
  }
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="导入商家菜品"
    width="600px"
    top="10%"
    @update:model-value="emit('update:visible', $event)"
    @close="handleClose"
  >
    <div class="import-merchant-dish-container">
      <!-- 商家选择 -->
      <el-form-item label="选择商家">
        <el-select
          v-model="selectedMerchant"
          placeholder="请选择商家"
          style="width: 100%"
          :loading="loadingMerchants"
          @change="loadMerchantDishes"
        >
          <el-option
            v-for="merchant in merchants"
            :key="merchant.id"
            :label="merchant.name || merchant.nickname"
            :value="merchant"
          />
        </el-select>
      </el-form-item>

      <!-- 菜品列表 -->
      <div v-if="loadingDishes" class="loading-container">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>加载菜品中...</span>
      </div>
      <div v-else-if="merchantDishes.length > 0" class="merchant-dishes-list">
        <h4>{{ selectedMerchant?.name || selectedMerchant?.nickname }} 的菜品</h4>
        <el-checkbox-group v-model="selectedMerchantDishes">
          <div v-for="dish in merchantDishes" :key="dish.id" class="dish-item">
            <el-checkbox :label="dish">{{ dish.name }}</el-checkbox>
            <span class="dish-nutrition">{{ dish.calorie }}kcal/份</span>
          </div>
        </el-checkbox-group>
      </div>
      <div v-else-if="selectedMerchant && merchantDishes.length === 0" class="empty-container">
        <el-empty description="该商家暂无菜品" />
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="confirmImportMerchantDishes"> 导入选中菜品 </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 导入商家菜品对话框样式 */
.import-merchant-dish-container {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9ff 100%);
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #e3f2fd;

  /* 表单标签 */
  :deep(.el-form-item__label) {
    font-weight: 700 !important;
    font-size: 14px !important;
    color: #2c3e50 !important;
  }

  /* 下拉选择框 */
  :deep(.el-select__wrapper) {
    border-radius: 8px !important;
    border: 1px solid #d9d9d9 !important;
    transition: all 0.3s ease !important;

    &:focus-within {
      border-color: #667eea !important;
      box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1) !important;
    }
  }

  /* 加载容器 */
  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 0;
    color: #667eea;
    font-size: 14px;

    .el-icon {
      font-size: 32px;
      margin-bottom: 12px;
    }
  }

  /* 空状态容器 */
  .empty-container {
    padding: 20px 0;
  }

  /* 菜品列表 */
  .merchant-dishes-list {
    margin-top: 20px;
    padding: 16px;
    background: white;
    border-radius: 8px;
    border: 1px solid #e0e0e0;

    h4 {
      color: #2c3e50;
      margin-bottom: 16px;
      font-size: 16px;
      font-weight: 700;
    }
  }

  /* 菜品项 */
  .dish-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px solid #f5f5f5;

    &:last-child {
      border-bottom: none;
    }

    /* 复选框 */
    :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
      background-color: #667eea;
      border-color: #667eea;
    }

    /* 营养信息 */
    .dish-nutrition {
      font-size: 14px;
      color: #999;
    }
  }
}
</style>
