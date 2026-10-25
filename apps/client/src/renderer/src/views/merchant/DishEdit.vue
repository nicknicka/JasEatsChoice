<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElUpload } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import { useAuthStore } from '../../store/authStore'
import CommonBackButton from '../../components/common/CommonBackButton.vue'

const route = useRoute()
const router = useRouter()

// 菜品基本信息
const dishInfo = ref({
  name: '麻辣香锅饭',
  category: '主食',
  price: 18,
  stock: 50,
  description: '精选食材，麻辣鲜香，回味无穷',
  ingredients: {
    mandatory: [], // 必选食材改为字符串数组
    optional: [] // 可选食材改为字符串数组
  },
  totalCalories: 0 // 总卡路里
})

// 新必选食材输入
const newMandatoryIngredient = ref('')

// 新可选食材输入
const newOptionalIngredient = ref('')

// 添加必选食材
const addMandatoryIngredient = () => {
  if (newMandatoryIngredient.value.trim()) {
    const ingredient = newMandatoryIngredient.value.trim()
    // 检查重复
    if (!dishInfo.value.ingredients.mandatory.includes(ingredient)) {
      dishInfo.value.ingredients.mandatory.push(ingredient)
      newMandatoryIngredient.value = ''
      calculateTotalCalories()
    } else {
      ElMessage.warning('该必选食材已存在')
    }
  }
}

// 添加可选食材
const addOptionalIngredient = () => {
  if (newOptionalIngredient.value.trim()) {
    const ingredient = newOptionalIngredient.value.trim()
    // 检查重复
    if (!dishInfo.value.ingredients.optional.includes(ingredient)) {
      dishInfo.value.ingredients.optional.push(ingredient)
      newOptionalIngredient.value = ''
      calculateTotalCalories()
    } else {
      ElMessage.warning('该可选食材已存在')
    }
  }
}

// 删除必选食材
const removeMandatoryIngredient = (index) => {
  dishInfo.value.ingredients.mandatory.splice(index, 1)
  calculateTotalCalories()
}

// 删除可选食材
const removeOptionalIngredient = (index) => {
  dishInfo.value.ingredients.optional.splice(index, 1)
  calculateTotalCalories()
}

// 计算总卡路里
const calculateTotalCalories = () => {
  // 由于改为直接输入食材名称，暂时简化卡路里计算
  // 实际项目中可以根据食材名称匹配数据库中的卡路里数据
  // 或添加输入框让商家直接输入卡路里
  dishInfo.value.totalCalories = 0
}

// 菜品分类选项
const categories = ['主食', '汤品', '饮料', '小吃']

// 页面加载
onMounted(() => {
  // 可以从路由参数获取菜品ID并加载菜品数据
})

// 保存菜品
const saveDish = () => {
  // 从authStore获取商家ID
  const authStore = useAuthStore()
  const merchantId = authStore.merchantId
  if (!merchantId) {
    ElMessage.error('未检测到商家ID，请重新登录')
    return
  }

  // 准备请求数据，将 ingredients 对象序列化为 JSON 字符串，并将 totalCalories 映射为 calorie
  const requestData = {
    ...dishInfo.value,
    merchantId,
    calorie: dishInfo.value.totalCalories,
    ingredients: JSON.stringify(dishInfo.value.ingredients)
  }
  // 删除不需要的 totalCalories 字段
  delete requestData.totalCalories

  // 判断是新增还是编辑操作（根据是否有id字段）
  const isEdit = !!dishInfo.value.id

  // 发送请求
  const request = isEdit
    ? axios.put(`${API_CONFIG.baseURL}${API_CONFIG.dish.detail}${dishInfo.value.id}`, requestData)
    : axios.post(`${API_CONFIG.baseURL}${API_CONFIG.dish.list}`, requestData)

  request
    .then((response) => {
      console.log('保存菜品结果:', response)
      // 根据后端API的实际响应格式调整，通常检查HTTP状态码和业务成功标识
      if (response.status === 200 && response.data && response.data.success) {
        ElMessage.success('菜品保存成功')
        // 跳回菜品管理页面
        router.push('/merchant/dish-management')
      } else {
        ElMessage.error(response.data?.message || '菜品保存失败')
      }
    })
    .catch((error) => {
      console.error('保存菜品失败:', error)
      ElMessage.error('网络错误，菜品保存失败')
    })
}

// 上传菜品图片
const handleUpload = (file) => {
  console.log('上传菜品图片:', file)
  ElMessage.success('图片上传成功')
  return false // 阻止自动上传
}
</script>

<template>
  <div class="dish-edit-container">
    <div class="dish-edit-header">
      <div class="header-left">
        <h3 class="page-title">【菜品编辑】</h3>
        <common-back-button type="text" class="back-btn" />
      </div>
    </div>

    <div class="dish-edit-content">
      <!-- 菜品图片管理 -->
      <div class="dish-images-section">
        <h4 class="section-title">📷 菜品图片</h4>
        <el-upload action="#" list-type="picture-card" :auto-upload="false" @change="handleUpload">
          <el-icon class="avatar-uploader-icon">
            <Plus />
          </el-icon>
        </el-upload>
      </div>

      <!-- 菜品基本信息 -->
      <div class="dish-info-section">
        <h4 class="section-title">📝 菜品基本信息</h4>
        <div class="info-item">
          <span class="info-label">🍽️ 菜品名称：</span>
          <el-input v-model="dishInfo.name" placeholder="请输入菜品名称" style="width: 300px" />
        </div>
        <div class="info-item">
          <span class="info-label">📋 菜品分类：</span>
          <el-select
            v-model="dishInfo.category"
            placeholder="选择或输入菜品分类"
            style="width: 200px"
            filterable
            allow-create
            default-first-option
            @create="(inputValue) => (dishInfo.category = inputValue)"
          >
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </div>
        <div class="info-item">
          <span class="info-label">💰 价格：</span>
          <el-input-number v-model="dishInfo.price" :min="0.01" :step="0.01" style="width: 200px" />
          <span class="unit">元</span>
        </div>
        <div class="info-item">
          <span class="info-label">📦 库存：</span>
          <el-input-number v-model="dishInfo.stock" :min="0" :step="1" style="width: 200px" />
          <span class="unit">份</span>
        </div>
        <div class="info-item">
          <span class="info-label">📝 菜品描述：</span>
          <el-input
            v-model="dishInfo.description"
            placeholder="请输入菜品描述"
            style="width: 500px"
            type="textarea"
            :rows="4"
          />
        </div>

        <!-- 必选食材 -->
        <div class="info-item">
          <span class="info-label">🔑 必选食材：</span>
          <div class="optional-ingredients-container">
            <div class="input-button-row">
              <el-input
                v-model="newMandatoryIngredient"
                placeholder="请输入必选食材"
                style="width: calc(300px - 80px)"
                @keyup.enter="addMandatoryIngredient"
                clearable
              />
              <el-button
                type="primary"
                size="small"
                @click="addMandatoryIngredient"
                style="margin-left: 8px"
              >
                添加
              </el-button>
            </div>
            <div class="ingredients-tags">
              <el-tag
                v-for="(ingredient, index) in dishInfo.ingredients.mandatory"
                :key="index"
                type="warning"
                size="small"
                closable
                @close="removeMandatoryIngredient(index)"
              >
                {{ ingredient }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 可选食材 -->
        <div class="info-item">
          <span class="info-label">🔧 可选食材：</span>
          <div class="optional-ingredients-container">
            <div class="input-button-row">
              <el-input
                v-model="newOptionalIngredient"
                placeholder="请输入可选食材"
                style="width: calc(300px - 80px)"
                @keyup.enter="addOptionalIngredient"
                clearable
              />
              <el-button
                type="primary"
                size="small"
                @click="addOptionalIngredient"
                style="margin-left: 8px"
              >
                添加
              </el-button>
            </div>
            <div class="ingredients-tags">
              <el-tag
                v-for="(ingredient, index) in dishInfo.ingredients.optional"
                :key="index"
                type="success"
                size="small"
                closable
                @close="removeOptionalIngredient(index)"
              >
                {{ ingredient }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 总卡路里 -->
        <div class="info-item">
          <span class="info-label">🔥 总卡路里：</span>
          <div class="calorie-display">{{ dishInfo.totalCalories }} kcal</div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="success" @click="saveDish()">💾 保存菜品</el-button>
        <CommonBackButton type="text" text="🔙 取消编辑" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';
@import '../../assets/css/merchant-theme.less';

.dish-edit-container {
  padding: 0 20px 20px 20px;

  .dish-edit-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 1.286rem /* 原值: 18px */;
      font-weight: 600;
      margin: 0;
    }
  }

  .dish-edit-content {
    .dish-images-section,
    .dish-info-section {
      background-color: @merchant-surface;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 20px;
      box-shadow: 0 2px 12px 0 @merchant-shadow;
    }

    .section-title {
      font-size: 1.143rem /* 原值: 16px */;
      font-weight: 600;
      margin-bottom: 16px;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;

      .info-label {
        color: @merchant-text-sec;
        width: 120px;
      }

      .unit {
        margin-left: 8px;
        color: @merchant-text-sec;
      }
    }

    .action-buttons {
      display: flex;
      gap: 12px;
    }
  }
}
.calorie-display {
  font-size: 1.143rem /* 原值: 16px */;
  font-weight: 600;
  color: @merchant-error;
}

.optional-ingredients-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 300px;

  .input-button-row {
    display: flex;
    align-items: center;
  }

  .ingredients-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
