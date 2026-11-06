<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElUpload } from 'element-plus'
import axios from 'axios'
import { API_CONFIG } from '../../config/index.js'
import { useAuthStore } from '../../store/authStore'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import AppCard from '../../components/common/AppCard.vue'
import { Box, Document, EditPen, Folder, Goods, Key, Money, Picture, Plus, Tools } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 保存中的视觉状态，防止重复提交
const saving = ref(false)

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

  saving.value = true

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
    .finally(() => {
      saving.value = false
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
    <AppPageHeader title="【菜品编辑】" :compact="true">
      <template #actions>
        <CommonBackButton type="default" />
      </template>
    </AppPageHeader>

    <AppCard class="dish-edit-card" padding="md">
      <div class="edit-grid">
        <section class="media-column">
          <h2 class="section-title">
            <el-icon><Picture /></el-icon>
            菜品图片
          </h2>
          <el-upload
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            @change="handleUpload"
          >
            <el-icon class="avatar-uploader-icon">
              <Plus />
            </el-icon>
          </el-upload>
        </section>

        <section class="form-column">
          <h2 class="section-title">
            <el-icon><EditPen /></el-icon>
            菜品基本信息
          </h2>

          <div class="form-grid">
            <div class="form-field form-field--full">
              <label class="info-label form-label" for="dish-name">
                <el-icon><Goods /></el-icon>
                菜品名称
                <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <el-input
                id="dish-name"
                v-model="dishInfo.name"
                size="default"
                placeholder="请输入菜品名称"
                aria-required="true"
              />
            </div>

            <div class="form-field form-field--third">
              <label class="info-label form-label" for="dish-category">
                <el-icon><Folder /></el-icon>
                菜品分类
                <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <el-select
                id="dish-category"
                v-model="dishInfo.category"
                placeholder="选择或输入菜品分类"
                filterable
                allow-create
                default-first-option
                aria-required="true"
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

            <div class="form-field form-field--third">
              <label class="info-label form-label" for="dish-price">
                <el-icon><Money /></el-icon>
                价格
                <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <div class="number-field">
                <el-input-number
                  id="dish-price"
                  v-model="dishInfo.price"
                  :min="0.01"
                  :step="0.01"
                  aria-required="true"
                />
                <span class="unit">元</span>
              </div>
            </div>

            <div class="form-field form-field--third">
              <label class="info-label form-label" for="dish-stock">
                <el-icon><Box /></el-icon>
                库存
                <span class="required-mark" aria-hidden="true">*</span>
              </label>
              <div class="number-field">
                <el-input-number
                  id="dish-stock"
                  v-model="dishInfo.stock"
                  :min="0"
                  :step="1"
                  aria-required="true"
                />
                <span class="unit">份</span>
              </div>
            </div>

            <div class="form-field form-field--third">
              <span class="info-label form-label">
                <el-icon><Document /></el-icon>
                总卡路里
              </span>
              <div class="calorie-display">
                <strong>{{ dishInfo.totalCalories }}</strong>
                <span>kcal</span>
              </div>
            </div>

            <div class="form-field form-field--full">
              <label class="info-label form-label" for="dish-description">
                <el-icon><Document /></el-icon>
                菜品描述
              </label>
              <el-input
                id="dish-description"
                v-model="dishInfo.description"
                placeholder="请输入菜品描述"
                type="textarea"
                :rows="4"
              />
            </div>

            <div class="form-field form-field--full">
              <div class="ingredient-grid">
                <div class="ingredient-field">
                  <span class="info-label form-label">
                    <el-icon><Key /></el-icon>
                    必选食材
                  </span>
                  <div class="input-button-row">
                    <el-input
                      v-model="newMandatoryIngredient"
                      placeholder="请输入必选食材"
                      clearable
                      @keyup.enter="addMandatoryIngredient"
                    />
                    <el-button type="default" size="small" @click="addMandatoryIngredient">
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

                <div class="ingredient-field">
                  <span class="info-label form-label">
                    <el-icon><Tools /></el-icon>
                    可选食材
                  </span>
                  <div class="input-button-row">
                    <el-input
                      v-model="newOptionalIngredient"
                      placeholder="请输入可选食材"
                      clearable
                      @keyup.enter="addOptionalIngredient"
                    />
                    <el-button type="default" size="small" @click="addOptionalIngredient">
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
            </div>
          </div>
        </section>
      </div>
    </AppCard>

    <div class="action-bar">
      <div class="action-bar__actions">
        <CommonBackButton type="default" text="取消编辑" />
        <el-button type="primary" :loading="saving" @click="saveDish">保存菜品</el-button>
      </div>
    </div>
  </div>
</template>


<style scoped>
/* 页面容器：暖白页面底 + 内容区自适应（§3.1、§3.3） */
.dish-edit-container {
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  padding: 0 var(--page-gutter) var(--space-10);
  background: var(--canvas);
}

/* L1 微质感卡片由 AppCard 承担，merchant 内边距取紧凑档（§3.4、§4.2） */
.dish-edit-card {
  margin-bottom: var(--card-gap-compact);
  padding: var(--card-padding-compact);
}

/* 图片与基本信息两栏铺满卡片，收回右侧留白（§4.4） */
.edit-grid {
  display: grid;
  grid-template-columns: minmax(260px, 0.82fr) minmax(0, 2.18fr);
  gap: var(--space-6);
  align-items: start;
}

.media-column {
  min-width: 0;
  padding-right: var(--space-6);
  border-right: 1px solid var(--line);
}

.form-column {
  min-width: 0;
}

/* 区块标题从 h2 起（页头已占用 h1），字号 --text-h3（§3.2、§4.7） */
.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-4);
  font-size: var(--text-h3);
  line-height: 24px;
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
}

.section-title .el-icon {
  color: var(--brand-500);
}

/* 图片上传：线性图标 + 中性虚线上传区（§4.2、§4.9） */
.avatar-uploader-icon {
  font-size: 28px;
  color: var(--ink-400);
}

:deep(.el-upload--picture-card) {
  width: 160px;
  height: 160px;
  border-color: var(--line-strong);
  border-radius: var(--radius-md);
  background: var(--canvas-sunken);
  transition:
    border-color var(--duration-fast) var(--ease-out),
    background-color var(--duration-fast) var(--ease-out);
}

:deep(.el-upload--picture-card:hover) {
  border-color: var(--brand-400);
  background: var(--brand-50);
}

/* 表单：6 列栅格支持全宽、三分之一行与并排食材组（§3.3、§4.4） */
.form-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: var(--space-4);
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.form-field--full {
  grid-column: 1 / -1;
}

.form-field--third {
  grid-column: span 2;
}

/* 字段标签：统一“标签在上”与线性图标，必填星号取 --danger（§4.4、§4.9） */
.form-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  min-height: 20px;
  font-size: var(--text-body);
  font-weight: var(--weight-medium);
  color: var(--ink-700);
}

.form-label .el-icon {
  color: var(--ink-400);
}

.required-mark {
  color: var(--danger);
}

/* 控件铺满各自栅格列，焦点态统一品牌色（§4.6、§7.1） */
.form-field :deep(.el-input),
.form-field :deep(.el-select),
.form-field :deep(.el-input-number) {
  width: 100%;
}

.form-field :deep(.el-input__wrapper),
.form-field :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);
}

.form-field :deep(.el-input__wrapper:hover),
.form-field :deep(.el-textarea__inner:hover) {
  border-color: var(--brand-400);
}

.form-field :deep(.el-input__wrapper.is-focus),
.form-field :deep(.el-textarea__inner:focus) {
  border-color: var(--brand-500);
  box-shadow: 0 0 0 2px var(--brand-400);
}

.number-field {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
}

.number-field :deep(.el-input-number) {
  flex: 1;
}

.unit {
  flex-shrink: 0;
  color: var(--ink-500);
  font-size: var(--text-body-sm);
}

/* 卡路里是中性读数，不再使用错误红（§3.1、§4.4） */
.calorie-display {
  display: inline-flex;
  align-items: baseline;
  justify-content: flex-end;
  gap: var(--space-1);
  min-height: var(--control-height-md);
  padding: 0 var(--space-3);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--canvas-sunken);
  color: var(--ink-700);
  font-family: var(--font-num);
  font-variant-numeric: tabular-nums;
  line-height: var(--control-height-md);
}

.calorie-display strong {
  color: var(--ink-900);
  font-weight: var(--weight-semibold);
}

/* 食材两组并排，输入和标签共用一套 8px 节奏（§4.4） */
.ingredient-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

.ingredient-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
}

.input-button-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.input-button-row :deep(.el-input) {
  flex: 1;
}

.input-button-row :deep(.el-button) {
  height: var(--control-height-sm);
}

.ingredients-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  min-height: var(--control-height-sm);
}

/* 保存动作吸底，主按钮唯一，返回/取消为次级（§4.1、§4.4、§4.7） */
.action-bar {
  position: sticky;
  bottom: 0;
  z-index: 2;
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
}

.action-bar__actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.action-bar__actions :deep(.el-button) {
  height: var(--control-height-md);
  min-width: 112px;
}

/* 窄屏：两栏改单栏，食材也改成上下排列，避免横向挤压 */
@media (max-width: 1100px) {
  .edit-grid {
    grid-template-columns: 1fr;
  }

  .media-column {
    padding-right: 0;
    padding-bottom: var(--space-6);
    border-right: 0;
    border-bottom: 1px solid var(--line);
  }

  .form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .form-field--third {
    grid-column: span 1;
  }
}

@media (max-width: 720px) {
  .form-grid,
  .ingredient-grid {
    grid-template-columns: 1fr;
  }

  .form-field--full,
  .form-field--third {
    grid-column: auto;
  }

  .action-bar__actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
