<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 接收从父组件传递的 props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  recipe: {
    type: Object,
    default: null
  },
  merchants: {
    type: Array,
    default: () => [
      {
        id: 1,
        name: '健康餐厅',
        dishes: [
          { id: 1, name: '有机蔬菜沙拉', nutrition: '120kcal/份' },
          { id: 2, name: '烤三文鱼', nutrition: '280kcal/份' }
        ]
      },
      {
        id: 2,
        name: '健身餐吧',
        dishes: [
          { id: 3, name: '鸡胸肉盖饭', nutrition: '450kcal/份' },
          { id: 4, name: '糙米粥', nutrition: '180kcal/份' }
        ]
      }
    ]
  }
})

// 定义事件
const emit = defineEmits(['close', 'import', 'update:visible'])

// 商家列表和选中商家
const selectedMerchant = ref(null)
const merchantDishes = ref([]) // 选中商家的菜品
const selectedMerchantDishes = ref([]) // 选中的导入菜品

// 加载商家菜品
const loadMerchantDishes = () => {
  if (selectedMerchant.value) {
    merchantDishes.value = selectedMerchant.value.dishes
    selectedMerchantDishes.value = [] // 重置选中菜品
  }
}

// 确认导入商家菜品
const confirmImportMerchantDishes = () => {
  if (selectedMerchantDishes.value.length > 0) {
    // 这里需要知道要导入到哪个食谱，需要先设置 selectedRecipe
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
          @change="loadMerchantDishes"
        >
          <el-option
            v-for="merchant in props.merchants"
            :key="merchant.id"
            :label="merchant.nickname"
            :value="merchant"
          />
        </el-select>
      </el-form-item>

      <!-- 菜品列表 -->
      <div v-if="merchantDishes.length > 0" class="merchant-dishes-list">
        <h4>{{ selectedMerchant?.name }} 的菜品</h4>
        <el-checkbox-group v-model="selectedMerchantDishes">
          <div v-for="dish in merchantDishes" :key="dish.id" class="dish-item">
            <el-checkbox :label="dish">{{ dish.name }}</el-checkbox>
            <span class="dish-nutrition">{{ dish.nutrition }}</span>
          </div>
        </el-checkbox-group>
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
  .el-form-item__label {
    font-weight: 700 !important;
    font-size: 14px !important;
    color: #2c3e50 !important;
  }

  /* 下拉选择框 */
  .el-select__wrapper {
    border-radius: 8px !important;
    border: 1px solid #d9d9d9 !important;
    transition: all 0.3s ease !important;

    &:focus-within {
      border-color: #667eea !important;
      box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1) !important;
    }
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
