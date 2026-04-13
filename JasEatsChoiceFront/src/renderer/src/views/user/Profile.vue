<template>
  <div class="profile-container">
    <h2 class="page-title">个人中心</h2>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton animated>
        <template #template>
          <el-skeleton-item
            variant="circle"
            style="width: 120px; height: 120px; margin: 0 auto 20px"
          />
          <el-skeleton-item variant="h1" style="width: 50%; margin: 0 auto 20px" />
          <el-skeleton-item variant="rect" style="height: 200px; margin-bottom: 20px" />
          <el-skeleton-item variant="rect" style="height: 150px" />
        </template>
      </el-skeleton>
    </div>

    <el-card v-else class="profile-card scale-in">
      <!-- 顶部头像区域 -->
      <div class="profile-header fade-in-up">
        <!-- 单独拎出的名字 -->
        <div class="user-name-container">
          <h3 class="user-name">{{ userInfo.nickname || '未设置' }}</h3>
        </div>

        <div class="profile-content">
          <div class="avatar-wrapper">
            <CommonAvatar
              :avatar-url="avatarSrc"
              :fallback-text="userInfo.nickname || '未设置'"
              :size="120"
              :show-upload="true"
              :show-upload-button="false"
              :click-to-enlarge="true"
              ref="commonAvatarRef"
              @upload="handleAvatarUpload"
            />
          </div>

          <div class="user-info-section">
            <div class="user-basic-info">
              <div class="user-stats">
                <div class="stat-row">
                  <div class="stat-item">
                    <span class="stat-label">手机号</span>
                    <span class="stat-value">{{ userInfo.phone || '未绑定' }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">所在地</span>
                    <span class="stat-value">{{ userInfo.location || '未设置' }}</span>
                  </div>
                </div>
                <div class="stat-row">
                  <div class="stat-item">
                    <span class="stat-label">今日摄入</span>
                    <span class="stat-value calorie-highlight">{{ todayCalorieDisplay }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">本周均衡度</span>
                    <span class="stat-value balance-highlight">{{ weekBalanceDisplay }}</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                class="action-btn upload-avatar-btn"
                @click="triggerAvatarUpload"
              >
                <el-icon><Camera /></el-icon>
                <span style="margin-left: 5px">更换头像</span>
              </el-button>
              <el-button
                type="primary"
                size="small"
                class="action-btn share-btn"
                @click="shareProfile"
              >
                <el-icon><Share /></el-icon>
                <span style="margin-left: 5px">分享</span>
              </el-button>
              <el-button
                type="primary"
                size="small"
                class="action-btn edit-btn"
                @click="editProfile"
              >
                <el-icon><Edit /></el-icon>
                <span style="margin-left: 5px">编辑资料</span>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="order-module fade-in-up delay-100">
        <h3 class="module-title">
          <el-icon><Document /></el-icon>
          <span>订单模块</span>
        </h3>
        <div class="order-stats">
          <div class="order-stat-card stagger-item" @click="goToOrdersByStatus('processing')">
            <div class="stat-value order-in-progress number-scroll">{{ userInfo.orders?.inProgress || 0 }}笔</div>
            <div class="stat-label">进行中订单</div>
          </div>
          <div class="order-stat-card stagger-item" @click="goToOrdersByStatus('pending')">
            <div class="stat-value order-pending number-scroll">{{ userInfo.orders?.pending || 0 }}笔</div>
            <div class="stat-label">待确认订单</div>
          </div>
          <div class="order-stat-card stagger-item" @click="goToOrdersByStatus('pendingComment')">
            <div class="stat-value order-pending-comment number-scroll">
              {{ userInfo.orders?.pendingComment || 0 }}笔
            </div>
            <div class="stat-label">待评价订单</div>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; margin-top: 10px">
          <el-button type="primary" size="small" @click="goToAllOrders">
            <el-icon><Search /></el-icon>
            <span style="margin-left: 5px">查看所有订单</span>
          </el-button>
        </div>
      </div>

      <el-divider />

      <div class="wallet-module fade-in-up delay-200">
        <h3 class="module-title">
          <el-icon><Wallet /></el-icon>
          <span>钱包模块</span>
        </h3>
        <div class="wallet-card scale-in" @click="goToWalletManagement">
          <div class="wallet-header">
            <div class="wallet-label">平台币余额</div>
            <div class="wallet-hint">点击查看详情 →</div>
          </div>
          <div class="wallet-balance">
            <span class="balance-number number-scroll">{{ formatNumber(userInfo.wallet?.balance || 0) }}</span>
            <span class="balance-unit">个</span>
          </div>
          <div class="wallet-summary">
            <div class="summary-item">
              <span class="summary-label">累计充值</span>
              <span class="summary-value">{{ formatNumber(userInfo.wallet?.totalRecharge || 0) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">累计消费</span>
              <span class="summary-value">{{ formatNumber(userInfo.wallet?.totalConsume || 0) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">累计提现</span>
              <span class="summary-value">{{ formatNumber(userInfo.wallet?.totalWithdraw || 0) }}</span>
            </div>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="other-modules fade-in-up delay-300">
        <div class="module-grid">
          <div class="module-item-card stagger-item" @click="goToMyCollection">
            <div class="module-item-content">
              <div class="module-item-icon">
                <el-icon :size="24"><StarFilled /></el-icon>
              </div>
              <div class="module-item-info">
                <div class="module-item-title">我的收藏</div>
                <div class="module-item-desc">共{{ userInfo.collections || 0 }}个</div>
              </div>
            </div>
            <el-button
              type="text"
              size="small"
              class="module-item-btn"
              @click.stop="goToMyCollection"
            >
              查看收藏
            </el-button>
          </div>

          <div class="module-item-card stagger-item" @click="goToAddress">
            <div class="module-item-content">
              <div class="module-item-icon">
                <el-icon :size="24"><Location /></el-icon>
              </div>
              <div class="module-item-info">
                <div class="module-item-title">我的地址</div>
                <div class="module-item-desc">
                  共{{ userInfo.addresses || 0 }}个 | 默认地址：{{
                    userInfo.defaultAddress || '未设置'
                  }}
                </div>
              </div>
            </div>
            <el-button type="text" size="small" class="module-item-btn" @click.stop="goToAddress">
              管理地址
            </el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="bottom-actions fade-in-up delay-400">
        <el-button type="text" size="small" @click="submitFeedback">
          <el-icon><ChatDotSquare /></el-icon>
          <span style="margin-left: 5px">反馈建议</span>
        </el-button>
        <el-button type="text" size="small" danger @click="logout">
          <el-icon><SwitchButton /></el-icon>
          <span style="margin-left: 5px">退出登录</span>
        </el-button>
      </div>
    </el-card>

    <!-- 分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="分享个人中心" width="400px" center>
      <div class="share-content">
        <div class="share-link-section">
          <div class="section-title">分享链接</div>
          <el-input v-model="shareLink" readonly class="share-input" />
          <el-button type="primary" size="small" class="copy-btn" @click="copyShareLink">
            <el-icon><DocumentCopy /></el-icon>
            <span style="margin-left: 5px">复制链接</span>
          </el-button>
        </div>

        <div v-if="qrCodeDataUrl" class="qr-code-section">
          <div class="section-title">二维码分享</div>
          <img :src="qrCodeDataUrl" alt="分享二维码" class="qr-code" />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="shareDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="editProfileDialogVisible"
      width="600px"
      :close-on-click-modal="false"
      :show-close="false"
      destroy-on-close
      class="edit-profile-dialog"
    >
      <template #header="{ close }">
        <div class="ep-header">
          <div class="ep-header-left">
            <CommonAvatar
              :avatar-url="avatarSrc"
              :fallback-text="editForm.nickname || '用户'"
              :size="48"
              :show-upload="false"
              :click-to-enlarge="false"
            />
            <div class="ep-header-text">
              <h3 class="ep-title">编辑资料</h3>
              <p class="ep-subtitle">完善个人信息，获取精准饮食推荐</p>
            </div>
          </div>
          <button class="ep-close-btn" @click="close">
            <el-icon :size="16"><Close /></el-icon>
          </button>
        </div>
      </template>

      <div class="ep-body">
        <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editFormRules"
          label-position="top"
          class="ep-form"
          hide-required-asterisk
        >
          <!-- 基本信息 -->
          <div class="ep-section">
            <div class="ep-section-bar">
              <div class="ep-section-icon"><el-icon :size="15"><User /></el-icon></div>
              <span class="ep-section-label">基本信息</span>
              <div class="ep-section-line"></div>
            </div>
            <div class="ep-section-content">
              <div class="ep-grid-2">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="editForm.nickname" placeholder="给自己取个名字吧" maxlength="20" show-word-limit />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="editForm.email" placeholder="example@email.com" />
                </el-form-item>
              </div>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="editForm.phone" disabled>
                  <template #prefix><el-icon><Iphone /></el-icon></template>
                </el-input>
                <div class="ep-hint">手机号暂不支持修改</div>
              </el-form-item>
            </div>
          </div>

          <!-- 所在地区 -->
          <div class="ep-section">
            <div class="ep-section-bar">
              <div class="ep-section-icon ep-icon--green"><el-icon :size="15"><Location /></el-icon></div>
              <span class="ep-section-label">所在地区</span>
              <div class="ep-section-line"></div>
            </div>
            <div class="ep-section-content">
              <el-form-item prop="location">
                <el-cascader
                  v-model="selectedLocation"
                  :options="cascaderData"
                  :props="cascaderProps"
                  :loading="locationDataLoading"
                  :disabled="cascaderData.length === 0 && !locationDataLoading"
                  placeholder="选择省 / 市 / 区"
                  clearable
                  filterable
                  style="width: 100%"
                  @change="handleLocationChange"
                />
                <div v-if="cascaderData.length === 0 && !locationDataLoading" class="ep-error">
                  地址数据加载失败，请刷新页面重试
                </div>
              </el-form-item>
            </div>
          </div>

          <!-- 身体数据 -->
          <div class="ep-section">
            <div class="ep-section-bar">
              <div class="ep-section-icon ep-icon--blue"><el-icon :size="15"><DataLine /></el-icon></div>
              <span class="ep-section-label">身体数据</span>
              <div class="ep-section-line"></div>
            </div>
            <div class="ep-section-content">
              <div class="ep-grid-2">
                <el-form-item label="身高" prop="height">
                  <div class="ep-number-wrap">
                    <el-input-number v-model="editForm.height" :min="30" :max="280" :precision="1" :step="0.1" controls-position="right" style="width: 100%" />
                    <span class="ep-unit">cm</span>
                  </div>
                </el-form-item>
                <el-form-item label="体重" prop="weight">
                  <div class="ep-number-wrap">
                    <el-input-number v-model="editForm.weight" :min="5" :max="300" :precision="1" :step="0.1" controls-position="right" style="width: 100%" />
                    <span class="ep-unit">kg</span>
                  </div>
                </el-form-item>
              </div>
              <el-form-item label="饮食目标" prop="dietGoal">
                <div class="ep-goal-cards">
                  <div
                    v-for="goal in dietGoalOptions"
                    :key="goal.value"
                    :class="['ep-goal-card', { 'ep-goal-card--active': editForm.dietGoal === goal.value }]"
                    :style="{ '--goal-accent': goal.color }"
                    @click="selectDietGoal(goal.value)"
                  >
                    <span class="ep-goal-name">{{ goal.label }}</span>
                    <el-icon v-if="editForm.dietGoal === goal.value" class="ep-goal-check" :size="14"><Check /></el-icon>
                  </div>
                </div>
              </el-form-item>
            </div>
          </div>

          <!-- 饮食偏好 -->
          <div class="ep-section">
            <div class="ep-section-bar">
              <div class="ep-section-icon ep-icon--accent"><el-icon :size="15"><Edit /></el-icon></div>
              <span class="ep-section-label">饮食偏好</span>
              <div class="ep-section-line"></div>
            </div>
            <div class="ep-section-content">
              <el-form-item label="口味标签" prop="preferTags">
                <el-select
                  v-model="editForm.preferTags"
                  multiple
                  filterable
                  allow-create
                  placeholder="选择或输入你的口味偏好"
                  style="width: 100%"
                >
                  <el-option v-for="tag in tasteTagOptions" :key="tag.value" :label="tag.label" :value="tag.value">
                    <span class="ep-tag-dot" :style="{ background: tag.color }"></span>
                    {{ tag.label }}
                  </el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="过敏信息" prop="allergies">
                <el-input
                  v-model="editForm.allergies"
                  type="textarea"
                  :rows="3"
                  placeholder="如有过敏请详细说明，如：花生、海鲜、牛奶等"
                  resize="none"
                />
              </el-form-item>
            </div>
          </div>
        </el-form>
      </div>

      <template #footer>
        <div class="ep-footer">
          <el-button class="ep-btn-cancel" @click="editProfileDialogVisible = false">取消</el-button>
          <el-button class="ep-btn-save" type="primary" @click="saveEditProfile" :loading="saving">
            <el-icon v-if="!saving"><Check /></el-icon>
            <span>{{ saving ? '保存中...' : '保存修改' }}</span>
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 反馈建议对话框 -->
    <el-dialog
      v-model="feedbackDialogVisible"
      title="反馈建议"
      width="600px"
      :close-on-click-modal="false"
      center
    >
      <el-form
        ref="feedbackFormRef"
        :model="feedbackForm"
        :rules="feedbackFormRules"
        label-width="100px"
        style="margin-top: 20px"
      >
        <el-form-item label="反馈类型" prop="type">
          <el-select
            v-model="feedbackForm.type"
            placeholder="请选择反馈类型"
            style="width: 100%"
            @change="handleFeedbackTypeChange"
          >
            <el-option label="功能建议" value="suggestion">
              <span>功能建议</span>
              <span style="float: right; color: #8492a6; font-size: 0.857rem /* 原值: 12px */">
                <el-icon><InfoFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="问题反馈" value="bug">
              <span>问题反馈</span>
              <span style="float: right; color: #8492a6; font-size: 0.857rem /* 原值: 12px */">
                <el-icon><WarningFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="使用咨询" value="inquiry">
              <span>使用咨询</span>
              <span style="float: right; color: #8492a6; font-size: 0.857rem /* 原值: 12px */">
                <el-icon><QuestionFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="其他" value="other">
              <span>其他</span>
              <span style="float: right; color: #8492a6; font-size: 0.857rem /* 原值: 12px */">
                <el-icon><MoreFilled /></el-icon>
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="反馈标题" prop="title">
          <el-input
            v-model="feedbackForm.title"
            placeholder="请简要描述您的反馈（最多50字）"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="详细描述" prop="content">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="6"
            placeholder="请详细描述您的反馈内容，包括具体的场景、操作步骤、期望效果等..."
            maxlength="500"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="联系方式" prop="contact">
          <el-input
            v-model="feedbackForm.contact"
            placeholder="请留下您的联系方式（手机/邮箱），方便我们回复"
            clearable
          />
        </el-form-item>

        <el-form-item label="上传图片" prop="images">
          <el-upload
            v-model:file-list="feedbackImageList"
            :action="uploadAction"
            :headers="uploadHeaders"
            list-type="picture-card"
            :limit="3"
            :on-preview="handleImagePreview"
            :on-remove="handleImageRemove"
            :on-success="handleImageUploadSuccess"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div style="font-size: 0.857rem /* 原值: 12px */; color: #909399; margin-top: 5px">
                最多上传3张图片，支持 jpg/png 格式，单张图片不超过 2MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="feedbackDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitFeedbackForm" :loading="submittingFeedback">
            <el-icon><Promotion /></el-icon>
            <span style="margin-left: 5px">提交反馈</span>
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 图片预览对话框 -->
    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="600px" center>
      <img :src="previewImageUrl" alt="预览图片" style="width: 100%; display: block" />
    </el-dialog>
  </div>
</template>

<script setup>
// 导入依赖
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Camera,
  Share,
  Edit,
  Document,
  Search,
  Wallet,
  StarFilled,
  Location,
  ChatDotSquare,
  SwitchButton,
  DocumentCopy,
  QuestionFilled,
  WarningFilled,
  InfoFilled,
  MoreFilled,
  Plus,
  Promotion,
  User,
  Iphone,
  DataLine,
  Close,
  Check
} from '@element-plus/icons-vue'
import CommonAvatar from '../../components/CommonAvatar.vue'
import api from '../../utils/api'
import { API_CONFIG } from '../../config'
import QRCode from 'qrcode'
import { useCascaderLocationData } from '../../composables/useCascaderLocationData'

// 导入状态管理
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'
import walletApi from '../../api/wallet'

// 初始化路由和状态管理
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

// 响应式变量 & Refs
const loading = ref(true)
const saving = ref(false)

// 用户信息 - 必须与 userStore.UserInfo 接口的字段完全匹配
const userInfo = ref({
  // 基本信息
  userId: '',
  nickname: '',
  name: '',
  phone: '',
  email: '',
  location: '',
  avatar: '',

  // 身体数据
  height: null,
  weight: null,
  dietGoal: null,
  allergies: null,      // 过敏信息
  preferTags: null,     // 饮食偏好标签

  // 统计数据
  todayCalorie: 0,
  weekBalance: 0,
  orders: {
    inProgress: 0,
    pending: 0,
    pendingComment: 0
  },
  wallet: {
    balance: 0,
    totalRecharge: 0,
    totalConsume: 0,
    totalWithdraw: 0
  },
  collections: 0,
  addresses: 0,
  defaultAddress: '',

  // 商家相关（用户可能注册为商家）
  merchantId: null,

  // 时间戳
  createTime: '',
  updateTime: null
})

// 组件引用
const commonAvatarRef = ref(null)

// 计算属性
// 头像来源 - 统一使用userStore中的头像信息
const avatarSrc = computed(() => {
  return userStore.userInfo?.avatar
})

// 今日摄入显示
const todayCalorieDisplay = computed(() => {
  const calorie = userInfo.value.todayCalorie || 0
  return `${calorie}kcal`
})

// 本周均衡度显示
const weekBalanceDisplay = computed(() => {
  const balance = userInfo.value.weekBalance || 0
  return `${balance}%`
})

// 格式化数字显示（保留两位小数）
const formatNumber = (num) => {
  if (typeof num === 'number') {
    return num.toFixed(2)
  }
  // 处理字符串或BigDecimal情况
  const number = parseFloat(String(num || '0'))
  return isNaN(number) ? '0.00' : number.toFixed(2)
}

// 分享功能变量
const shareDialogVisible = ref(false)
const shareLink = ref('')
const qrCodeDataUrl = ref('')

// 资料编辑功能变量
const editProfileDialogVisible = ref(false)
const editForm = reactive({
  nickname: '',
  phone: '',
  email: '',
  location: '',
  height: 0,
  weight: 0,
  dietGoal: '',
  preferTags: [],      // 饮食偏好标签
  allergies: ''           // 过敏信息
})

// 地址选择功能变量
const selectedLocation = ref([])
const { cascaderData, loading: locationDataLoading, loadLocationData } = useCascaderLocationData()
const cascaderProps = {
  value: 'value',
  label: 'label',
  children: 'children',
  expandTrigger: 'hover'
}

// 资料编辑表单验证规则
const editFormRules = ref({
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  location: [{ required: false, message: '请选择所在地', trigger: 'blur' }],
  height: [
    {
      type: 'number',
      min: 30,
      max: 280,
      message: '身高范围在 30 到 280 cm',
      trigger: 'blur'
    }
  ],
  weight: [
    {
      type: 'number',
      min: 5,
      max: 300,
      message: '体重范围在 5 到 300 kg',
      trigger: 'blur'
    }
  ],
  dietGoal: [{ required: true, message: '请选择饮食目标', trigger: 'change' }]
})

// 编辑表单引用
const editFormRef = ref(null)

// 饮食目标选项
const dietGoalOptions = [
  { value: '减肥', label: '减肥', color: '#7BAE7F' },
  { value: '增肌', label: '增肌', color: '#6B9BD2' },
  { value: '保持健康', label: '保持健康', color: '#E2B455' }
]

// 口味标签选项
const tasteTagOptions = [
  { label: '蔬菜', value: '蒸菜', color: '#7BAE7F' },
  { label: '清淡', value: '清淡', color: '#6B9BD2' },
  { label: '重辣', value: '重辣', color: '#D47B7B' },
  { label: '油腻', value: '油腻', color: '#E2B455' },
  { label: '素食', value: '素食', color: '#4a7a4d' },
  { label: '甜食', value: '甜食', color: '#D4845A' },
  { label: '健康', value: '健康', color: '#7BAE7F' }
]

// 选择饮食目标
const selectDietGoal = (value) => {
  editForm.dietGoal = value
  editFormRef.value?.validateField('dietGoal')
}

// 反馈功能变量
const feedbackDialogVisible = ref(false)
const feedbackFormRef = ref(null)
const submittingFeedback = ref(false)
const feedbackForm = reactive({
  type: '',
  title: '',
  content: '',
  contact: '',
  images: []
})
const feedbackImageList = ref([])
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

// 反馈表单验证规则
const feedbackFormRules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入反馈标题', trigger: 'blur' },
    { min: 5, max: 50, message: '标题长度在 5 到 50 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  contact: [
    {
      pattern: /^1[3-9]\d{9}$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '请输入正确的手机号或邮箱',
      trigger: ['blur', 'change']
    }
  ]
}

// 上传配置
const uploadAction = computed(() => {
  return `${API_CONFIG.baseURL}${API_CONFIG.upload.image}`
})
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${authStore.token || localStorage.getItem('admin_token')}`
  }
})

// 生命周期钩子 - 统一的初始化入口
onMounted(async () => {
  await initPage()
})

// 页面初始化
const initPage = async () => {
  try {
    loading.value = true

    // 并行执行所有数据获取
    await Promise.all([
      fetchUserInfo(),
      fetchWalletInfo(),
      loadLocationData(),
      fetchHealthData(),
      fetchOrderStats(),
      fetchCollectionCount()
    ])
  } catch (error) {
    console.error('初始化页面失败:', error)
    ElMessage.error('加载用户信息失败，请刷新重试')
  } finally {
    loading.value = false
  }
}

// 获取用户信息
const fetchUserInfo = async () => {
  const userId = parseInt(authStore.userId || '0', 10)

  if (isNaN(userId) || userId <= 0) {
    ElMessage.error('用户未登录或登录信息无效，请重新登录')
    setTimeout(() => {
      router.push('/login')
    }, 1000)
    throw new Error('无效的用户ID')
  }

  // 检查是否需要从后端获取
  const storedInfo = userStore.userInfo
  const isUserInfoEmpty =
    !storedInfo ||
    Object.keys(storedInfo).length === 0 ||
    !storedInfo.nickname ||
    !storedInfo.phone

  if (isUserInfoEmpty) {
    const freshInfo = await userStore.fetchUserInfo(userId)
    userInfo.value = { ...userInfo.value, ...freshInfo }
  } else {
    userInfo.value = { ...userInfo.value, ...storedInfo }
  }
}

// 获取钱包信息
const fetchWalletInfo = async () => {
  try {
    const userId = parseInt(authStore.userId || '0', 10)
    if (isNaN(userId) || userId <= 0) return

    const walletResponse = await walletApi.getWalletInfo(userId)

    if (walletResponse.code === '200' && walletResponse.data) {
      userInfo.value = {
        ...userInfo.value,
        wallet: {
          balance: walletResponse.data.balance || 0,
          totalRecharge: walletResponse.data.totalRecharge || 0,
          totalConsume: walletResponse.data.totalConsume || 0,
          totalWithdraw: walletResponse.data.totalWithdraw || 0
        }
      }
    }
  } catch (error) {
    console.error('获取钱包信息失败:', error)
    // 钱包信息获取失败不影响其他功能
  }
}

// 获取健康数据（今日摄入、本周均衡度）
const fetchHealthData = async () => {
  try {
    const userId = authStore.userId || '0'
    if (!userId || userId === '0') return

    // 获取今日营养摄入统计
    const todayResponse = await api.get(`/calorie-records/user/${userId}/today-summary`)
    if (todayResponse.code === '200' && todayResponse.data) {
      userInfo.value = {
        ...userInfo.value,
        todayCalorie: Math.round(todayResponse.data.totalCalorie || 0)
      }
    }

    // 获取本周卡路里数据并计算均衡度
    const weekResponse = await api.get(`/calorie-records/user/${userId}/week`)
    if (weekResponse.code === '200' && weekResponse.data) {
      const weekData = Array.isArray(weekResponse.data) ? weekResponse.data : []
      const totalDays = weekData.length
      let balancedDays = 0

      // 合理摄入范围是1500-2500卡路里
      const minCalorie = 1500
      const maxCalorie = 2500

      weekData.forEach(day => {
        const consumed = day?.consumed || 0
        if (consumed >= minCalorie && consumed <= maxCalorie) {
          balancedDays++
        }
      })

      userInfo.value = {
        ...userInfo.value,
        weekBalance: totalDays > 0 ? Math.round((balancedDays / totalDays) * 100) : 0
      }
    }
  } catch (error) {
    console.error('获取健康数据失败:', error)
    userInfo.value = { ...userInfo.value, todayCalorie: 0, weekBalance: 0 }
  }
}

// 获取订单统计
const fetchOrderStats = async () => {
  const defaultOrders = { inProgress: 0, pending: 0, pendingComment: 0 }

  try {
    const userId = authStore.userId || '0'
    if (!userId || userId === '0') {
      userInfo.value = { ...userInfo.value, orders: defaultOrders }
      return
    }

    const response = await api.get(`/v1/orders/user/${userId}/statistics`)

    if (response.code === '200' && response.data) {
      userInfo.value = {
        ...userInfo.value,
        orders: {
          inProgress: response.data.inProgress || 0,
          pending: response.data.pending || 0,
          pendingComment: response.data.pendingComment || 0
        }
      }
    } else {
      userInfo.value = { ...userInfo.value, orders: defaultOrders }
    }
  } catch (error) {
    console.error('获取订单统计失败:', error)
    userInfo.value = { ...userInfo.value, orders: defaultOrders }
  }
}

// 获取收藏数量
const fetchCollectionCount = async () => {
  try {
    const userId = authStore.userId || '0'
    if (!userId || userId === '0') {
      userInfo.value = { ...userInfo.value, collections: 0 }
      return
    }

    const response = await api.get('/v1/collections', {
      params: { userId }
    })

    if (response.code === '200') {
      // 兼容不同的数据结构：response.data.data 或 response.data
      const collectionsData = Array.isArray(response.data?.data)
        ? response.data.data
        : Array.isArray(response.data)
        ? response.data
        : []

      userInfo.value = { ...userInfo.value, collections: collectionsData.length || 0 }
    } else {
      userInfo.value = { ...userInfo.value, collections: 0 }
    }
  } catch (error) {
    console.error('获取收藏数量失败:', error)
    userInfo.value = { ...userInfo.value, collections: 0 }
  }
}

// 刷新页面数据
const refreshData = async () => {
  await initPage()
  ElMessage.success('数据已刷新')
}

// 头像相关功能
// 触发头像上传
const triggerAvatarUpload = () => {
  commonAvatarRef.value?.$refs?.avatarInput?.click()
}

// 处理头像上传
const handleAvatarUpload = (file) => {
  if (!file) return

  const reader = new FileReader()
  reader.onload = async (e) => {
    const base64Image = e.target.result

    try {
      const userId = authStore.userId
      if (!userId) {
        ElMessage.error('用户未登录，请重新登录')
        return
      }

      // 直接将base64图片上传到后端
      const response = await api.put(`/v1/users/${userId}/avatar/base64`, {
        avatarBase64: base64Image
      })

      if (response.code === '200') {
        const freshInfo = await userStore.fetchUserInfo(userId)
        if (freshInfo) {
          userInfo.value = { ...userInfo.value, ...freshInfo }
        }
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error('头像上传失败: ' + (response.message || '未知错误'))
      }
    } catch (error) {
      console.error('头像上传失败:', error)
      ElMessage.error('头像上传失败')
    }
  }

  reader.readAsDataURL(file)
}

// 导航功能
// 跳转到所有订单页面
const goToAllOrders = () => {
  router.push('/user/home/orders')
}

// 跳转到指定状态的订单
const goToOrdersByStatus = (status) => {
  router.push({
    path: '/user/home/orders',
    query: { status }
  })
}

// 跳转到钱包管理页面
const goToWalletManagement = () => {
  router.push('/user/home/wallet-management')
}

// 跳转到我的收藏页面
const goToMyCollection = () => {
  router.push('/user/home/my-collection')
}

// 跳转到地址管理页面
const goToAddress = () => {
  router.push('/user/home/address')
}

// 设置功能
// 打开反馈建议对话框
const submitFeedback = () => {
  // 重置表单
  Object.assign(feedbackForm, {
    type: '',
    title: '',
    content: '',
    contact: '',
    images: []
  })
  feedbackImageList.value = []

  // 打开反馈对话框
  feedbackDialogVisible.value = true
}

// 提交反馈表单
const submitFeedbackForm = async () => {
  if (feedbackFormRef.value) {
    feedbackFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          submittingFeedback.value = true

          const userId = parseInt(String(authStore.userId || 1), 10)

          // 准备提交数据
          const submitData = {
            userId,
            type: feedbackForm.type,
            title: feedbackForm.title,
            content: feedbackForm.content,
            contact: feedbackForm.contact,
            images: feedbackForm.images.map(img => img.url || img.response?.data?.url || img.url)
          }

          // 发送POST请求提交反馈
          const response = await api.post(API_CONFIG.user.feedback, submitData)

          if (response.code === '200') {
            // 关闭对话框
            feedbackDialogVisible.value = false
            ElMessage.success('反馈已提交，感谢您的宝贵建议！')
          } else {
            ElMessage.error('反馈提交失败: ' + (response.message || '未知错误'))
          }
        } catch (error) {
          console.error('提交反馈失败:', error)
          ElMessage.error('网络请求失败，请稍后重试')
        } finally {
          submittingFeedback.value = false
        }
      } else {
        ElMessage.error('请检查表单填写是否完整')
      }
    })
  }
}

// 反馈类型变化处理
const handleFeedbackTypeChange = () => {
  // 可以根据类型动态调整表单提示
}

// 图片上传前校验
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isJpgOrPng) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 图片上传成功回调
const handleImageUploadSuccess = (response, file, fileList) => {
  console.log('图片上传成功:', response)
  if (response.code === '200') {
    // 将上传成功的图片URL添加到表单中
    feedbackForm.images.push({
      url: response.data.url,
      name: file.name
    })
  } else {
    ElMessage.error('图片上传失败: ' + response.message)
    // 上传失败，从文件列表中移除
    const index = feedbackImageList.value.indexOf(file)
    if (index > -1) {
      feedbackImageList.value.splice(index, 1)
    }
  }
}

// 图片预览
const handleImagePreview = (file) => {
  previewImageUrl.value = file.url || file.response?.data?.url
  imagePreviewVisible.value = true
}

// 图片移除
const handleImageRemove = (file) => {
  // 从表单中移除对应的图片
  const index = feedbackForm.images.findIndex(img => img.name === file.name)
  if (index > -1) {
    feedbackForm.images.splice(index, 1)
  }
}

// 资料编辑功能
// 编辑资料
const editProfile = () => {
  // 将当前用户信息填充到编辑表单
  Object.assign(editForm, {
    nickname: userInfo.value.nickname || '',
    phone: userInfo.value.phone || '',
    email: userInfo.value.email || '',
    location: userInfo.value.location || '',
    height: Number(userInfo.value.height) || 0,
    weight: Number(userInfo.value.weight) || 0,
    dietGoal: userInfo.value.dietGoal || '',
    preferTags: userInfo.value.preferTags || [],
    allergies: userInfo.value.allergies || ''
  })

  // 初始化地址选择器
  initLocationSelect(userInfo.value.location || '')

  // 打开编辑资料对话框
  editProfileDialogVisible.value = true
}

// 地址选择变化处理
const handleLocationChange = (value) => {
  if (value && Array.isArray(value) && value.length > 0) {
    // 将选中的adcode转换为对应的地区名称
    const locationNames = getLabelsByValues(value, cascaderData.value)
    editForm.location = locationNames.join(' ')
  } else {
    editForm.location = ''
  }
}

// 初始化地址选择器
const initLocationSelect = (location) => {
  // 处理空值或null/undefined
  if (!location || location.trim() === '') {
    selectedLocation.value = []
    return
  }

  // 拆分location字符串，并过滤掉空字符串
  const parts = location.split(' ').filter(part => part && part.trim() !== '')

  // 将地区名称转换为对应的adcode
  const adcodes = getValuesByLabels(parts, cascaderData.value)
  selectedLocation.value = adcodes
}

// 辅助函数：根据value（adcode）获取label（地区名称）
const getLabelsByValues = (values, data, level = 0, result = []) => {
  if (!values || values.length === 0 || !data || data.length === 0) {
    return result
  }

  const currentValue = values[level]
  const found = data.find(item => item.value === currentValue)

  if (found) {
    result.push(found.label)

    // 如果还有下一级，递归查找
    if (level + 1 < values.length && found.children) {
      return getLabelsByValues(values, found.children, level + 1, result)
    }
  }

  return result
}

// 辅助函数：根据label（地区名称）获取value（adcode）
const getValuesByLabels = (labels, data, level = 0, result = []) => {
  if (!labels || labels.length === 0 || !data || data.length === 0) {
    return result
  }

  const currentLabel = labels[level]
  const found = data.find(item => item.label === currentLabel)

  if (found) {
    result.push(found.value)

    // 如果还有下一级，递归查找
    if (level + 1 < labels.length && found.children) {
      return getValuesByLabels(labels, found.children, level + 1, result)
    }
  }

  return result
}

// 更新保存编辑的资料方法
const saveEditProfile = () => {
  if (!editFormRef.value) {
    ElMessage.error('表单初始化异常，请刷新页面重试')
    return
  }

  editFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('表单验证失败，请检查输入')
      return
    }

    try {
      saving.value = true

      const userId = parseInt(String(authStore.userId || '0'), 10)
      if (isNaN(userId) || userId <= 0) {
        ElMessage.error('用户信息异常，请重新登录')
        return
      }

      // 使用 updateInfo 端点更新基本信息（昵称/身高/体重/饮食目标/地址等）
      const response = await api.put(
        API_CONFIG.user.updateInfo.replace('{userId}', userId),
        {
          nickname: editForm.nickname,
          email: editForm.email,
          height: editForm.height,
          weight: editForm.weight,
          dietGoal: editForm.dietGoal,
          location: editForm.location,
          preferTags: editForm.preferTags,
          allergies: editForm.allergies
        }
      )

      if (response.code === '200') {
        // 从后端重新获取最新用户信息，确保数据一致
        const freshInfo = await userStore.fetchUserInfo(userId)
        if (freshInfo) {
          userInfo.value = { ...freshInfo }
        }

        editProfileDialogVisible.value = false
        ElMessage.success('资料更新成功')
      } else {
        ElMessage.error('资料更新失败: ' + (response.message || '未知错误'))
      }
    } catch (error) {
      console.error('更新资料失败:', error)
      ElMessage.error('网络请求失败，请稍后重试')
    } finally {
      saving.value = false
    }
  })
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm('确认要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      // 清除localStorage中的所有用户相关数据
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userAvatar')
      localStorage.removeItem('phone')
      localStorage.removeItem('userId')
      localStorage.removeItem('token')

      // 清除Store中的用户信息
      authStore.clearAuth()
      userStore.clearUserInfo()

      // 缩小窗口到登录尺寸
      if (window.api?.window?.resizeToLogin) {
        await window.api.window.resizeToLogin()
      }

      // 跳转到登录页面
      router.push('/login')
      ElMessage.success('已退出登录')
    })
    .catch(() => {
      ElMessage.info('已取消退出登录')
    })
}

// 分享功能
const shareProfile = () => {
  // 生成分享链接
  const userId = parseInt(String(authStore.userId || 1) || '1', 10)
  shareLink.value = `${window.location.origin}/user/profile/${userId}`

  // 生成二维码
  QRCode.toDataURL(shareLink.value, (err, url) => {
    if (err) {
      console.error('生成二维码失败:', err)
      qrCodeDataUrl.value = ''
    } else {
      qrCodeDataUrl.value = url
    }
  })

  // 打开分享对话框
  shareDialogVisible.value = true
}

// 复制分享链接
const copyShareLink = async () => {
  try {
    // 优先使用 Electron 的 clipboard API（更可靠）
    if (window.api && window.api.clipboard) {
      window.api.clipboard.writeText(shareLink.value)
      ElMessage.success('分享链接已复制到剪贴板')
      return
    }

    // 降级方案：使用 navigator.clipboard（浏览器环境）
    if (navigator.clipboard && navigator.clipboard.writeText) {
      await navigator.clipboard.writeText(shareLink.value)
      ElMessage.success('分享链接已复制到剪贴板')
      return
    }

    // 最后的降级方案：使用传统的 document.execCommand
    const textArea = document.createElement('textarea')
    textArea.value = shareLink.value
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('分享链接已复制到剪贴板')
    } catch (err) {
      throw err
    } finally {
      document.body.removeChild(textArea)
    }
  } catch (err) {
    console.error('复制失败:', err)
    ElMessage.error('复制失败，请手动复制')
  }
}

// 暴露刷新方法给外部使用
defineExpose({
  refreshData
})
</script>

<style scoped lang="less">
@import '../../assets/css/nordic-theme.less';

/* 基础容器样式 */
.profile-container {
  padding: 0 @nordic-space-lg @nordic-space-lg @nordic-space-lg;
  min-height: 100vh;
  background: @nordic-bg;
}

.loading-container {
  padding: 40px;
  background: @nordic-surface;
  border-radius: @nordic-radius-lg;
  box-shadow: 0 2px 8px @nordic-shadow;
}

.profile-card {
  padding: 25px;
  border-radius: @nordic-radius-lg;
  box-shadow: 0 1px 4px @nordic-shadow;
  background-color: @nordic-surface;
  border: 1px solid @nordic-border;
}

/* 标题样式 */
.profile-container h2 {
  font-size: @nordic-text-xl;
  margin: 0 0 @nordic-space-lg 0;
  color: @nordic-text;
  font-weight: 700;
  letter-spacing: @nordic-letter-tight;
}

.module-title {
  font-size: @nordic-text-md;
  margin: 0 0 @nordic-space-lg 0;
  font-weight: 600;
  color: @nordic-text;
  display: flex;
  align-items: center;
  gap: @nordic-space-sm;
  letter-spacing: -0.3px;
}

/* 顶部头像区域 */
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: @nordic-space-lg;
  padding: @nordic-space-lg;
  flex-wrap: wrap;
}

.user-name-container {
  width: 100%;
  text-align: center;
}

.user-name {
  font-size: @nordic-text-2xl;
  font-weight: 800;
  margin: 10px 0 20px 0;
  background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  display: inline-block;
  letter-spacing: 1px;
  line-height: 1.2;
}

/* 头像和用户信息内容区 */
.profile-content {
  display: flex;
  flex-direction: row;
  justify-content: space-evenly;
  align-items: center;
  gap: clamp(25px, 5vw, 45px);
  width: 100%;
  flex-wrap: wrap;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-info-section {
  min-width: 300px;
  padding-right: @nordic-space-lg;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-basic-info {
  margin-bottom: @nordic-space-lg;
}

/* 用户统计信息 */
.user-stats {
  font-size: @nordic-text-base;
  margin-bottom: @nordic-space-lg;
}

.stat-row {
  display: flex;
  gap: clamp(25px, 4vw, 40px);
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: @nordic-space-xs;
  text-align: center;
  min-width: clamp(120px, 20vw, 140px);
}

.stat-label {
  font-size: @nordic-text-base;
  font-weight: 500;
  color: @nordic-text-secondary;
}

.user-stats .stat-label {
  color: @nordic-text-secondary;
  margin-bottom: 5px;
}

.stat-value {
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-text;
}

.user-stats .stat-value {
  font-weight: bold;
  color: @nordic-red;
}

.calorie-highlight {
  color: @nordic-red;
}

.balance-highlight {
  color: @nordic-green;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  transition: all @nordic-transition-fast ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px @nordic-shadow-hover;
}

/* 订单统计 */
.order-stats {
  display: flex;
  flex-wrap: wrap;
  gap: @nordic-space-lg;
  margin-bottom: @nordic-space-lg;
}

.order-stat-card {
  flex: 1;
  min-width: 140px;
  padding: @nordic-space-lg;
  background: @nordic-surface;
  border-radius: @nordic-radius-lg;
  border: 1px solid @nordic-border;
  text-align: center;
  box-shadow: 0 1px 4px @nordic-shadow;
  transition:
    transform @nordic-transition-slow ease,
    box-shadow @nordic-transition-slow ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px @nordic-shadow-hover;
  }

  .stat-value {
    font-size: @nordic-text-2xl;
    color: @nordic-text;
    margin-bottom: 6px;
  }

  .stat-label {
    font-size: @nordic-text-base;
    color: @nordic-text-secondary;
    margin-bottom: 2px;
  }
}

.order-in-progress {
  color: @nordic-blue;
}

.order-pending {
  color: @nordic-yellow;
}

.order-pending-comment {
  color: @nordic-accent;
}

/* 钱包模块 */
.wallet-card {
  background: linear-gradient(135deg, @nordic-yellow-light 0%, @nordic-accent-light 100%);
  padding: 25px;
  border-radius: @nordic-radius-lg;
  box-shadow: 0 1px 4px @nordic-shadow;
  border: 1px solid @nordic-border;
  cursor: pointer;
  transition: all @nordic-transition-slow ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px @nordic-shadow-hover;
  }
}

.wallet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.wallet-label {
  font-size: @nordic-text-md;
  color: @nordic-text-secondary;
  font-weight: 500;
}

.wallet-hint {
  font-size: @nordic-text-base;
  color: @nordic-yellow-dark;
  font-weight: 500;
}

.wallet-balance {
  display: flex;
  align-items: baseline;
  gap: @nordic-space-sm;
  margin-bottom: @nordic-space-lg;
}

.balance-number {
  font-size: 3.429rem;
  font-weight: 700;
  color: @nordic-yellow-dark;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.balance-unit {
  font-size: @nordic-text-md;
  color: @nordic-yellow-dark;
  font-weight: 500;
}

.wallet-summary {
  display: flex;
  gap: 30px;
  padding-top: 15px;
  border-top: 1px solid @nordic-divider;
  flex-wrap: wrap;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.summary-label {
  font-size: @nordic-text-sm;
  color: @nordic-text-muted;
}

.summary-value {
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-text;
}

/* 其他模块 */
.other-modules {
  margin-bottom: @nordic-space-lg;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: @nordic-space-lg;
}

.module-item-card {
  background: @nordic-blue-light;
  padding: @nordic-space-lg;
  border-radius: @nordic-radius-lg;
  border: 1px solid @nordic-border;
  box-shadow: 0 1px 4px @nordic-shadow;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all @nordic-transition-slow ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px @nordic-shadow-hover;
  }
}

.module-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.module-item-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: fade(@nordic-blue, 12%);
  border-radius: @nordic-radius-sm;
  color: @nordic-blue;
}

.module-item-info {
  display: flex;
  flex-direction: column;
  gap: @nordic-space-xs;
}

.module-item-title {
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-blue;
}

.module-item-desc {
  font-size: @nordic-text-base;
  color: @nordic-text-secondary;
}

.module-item-btn {
  color: @nordic-blue;
  font-weight: 600;
}

/* 底部操作按钮 */
.bottom-actions {
  margin-top: @nordic-space-xl;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;

  .el-button {
    flex: 1;
    min-width: 120px;
    height: 40px;
    border-radius: @nordic-radius-sm;
    font-weight: 600;
    transition: transform @nordic-transition-fast ease;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      transform: translateY(-2px);
    }

    &:nth-child(1) {
      background: linear-gradient(135deg, @nordic-green 0%, @nordic-green-dark 100%);
      border: none;
      color: @nordic-surface;
    }

    &:nth-child(2) {
      background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
      border: none;
      color: @nordic-surface;
    }
  }
}

/* 分享对话框样式 */
.share-content {
  padding: @nordic-space-lg 0;
}

.share-link-section {
  margin-bottom: @nordic-space-lg;
}

.section-title {
  font-size: @nordic-text-md;
  font-weight: 600;
  color: @nordic-text;
  margin-bottom: 15px;
}

.share-input {
  margin-bottom: 15px;
}

.copy-btn {
  width: 100%;
}

.qr-code-section {
  margin-top: 25px;
  padding-top: 25px;
  border-top: 1px solid @nordic-border;
}

.qr-code {
  width: 200px;
  height: 200px;
  margin: 0 auto;
  display: block;
}

/* 单位提示 */
.unit-hint {
  margin-left: 10px;
  color: @nordic-text-muted;
  font-size: @nordic-text-base;
}

/* === 编辑资料对话框 === */

/* 对话框头部 */
.ep-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.ep-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ep-title {
  font-size: 20px;
  font-weight: 700;
  color: @nordic-text;
  margin: 0;
  letter-spacing: @nordic-letter-tight;
  line-height: 1.3;
}

.ep-subtitle {
  font-size: @nordic-text-sm;
  color: @nordic-text-muted;
  margin: 4px 0 0;
  letter-spacing: 0.2px;
}

.ep-close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: @nordic-bg;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: @nordic-text-secondary;
  transition: all @nordic-transition-fast ease;
  flex-shrink: 0;

  &:hover {
    background: @nordic-accent-light;
    color: @nordic-accent;
  }
}

/* 对话框内容区 */
.ep-body {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 4px;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: @nordic-border;
    border-radius: 2px;
  }
}

/* 表单全局样式 */
.ep-form {
  :deep(.el-form-item__label) {
    font-size: @nordic-text-sm;
    font-weight: 500;
    color: @nordic-text-secondary;
    padding-bottom: 6px;
    line-height: 1;
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: @nordic-radius-md;
    transition: all @nordic-transition-base ease;

    &:hover {
      box-shadow: 0 0 0 1px @nordic-border inset;
    }
  }

  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px @nordic-accent inset !important;
  }

  :deep(.el-textarea__inner:focus) {
    box-shadow: 0 0 0 1px @nordic-accent inset !important;
  }

  :deep(.el-input.is-disabled .el-input__wrapper) {
    background: @nordic-bg;
    box-shadow: 0 0 0 1px @nordic-border inset;
  }
}

/* 分区 */
.ep-section {
  margin-bottom: 28px;
  animation: ep-fade-up 0.4s ease both;

  &:nth-child(1) { animation-delay: 0s; }
  &:nth-child(2) { animation-delay: 0.06s; }
  &:nth-child(3) { animation-delay: 0.12s; }
  &:nth-child(4) { animation-delay: 0.18s; }

  &:last-child {
    margin-bottom: 0;
  }
}

@keyframes ep-fade-up {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ep-section-bar {
  display: flex;
  align-items: center;
  gap: @nordic-space-sm;
  margin-bottom: @nordic-space-lg;
}

.ep-section-icon {
  width: 28px;
  height: 28px;
  border-radius: @nordic-radius-md;
  background: fade(@nordic-accent, 10%);
  color: @nordic-accent;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.ep-icon--green {
    background: fade(@nordic-green, 10%);
    color: @nordic-green;
  }
  &.ep-icon--blue {
    background: fade(@nordic-blue, 10%);
    color: @nordic-blue;
  }
  &.ep-icon--accent {
    background: fade(@nordic-accent, 10%);
    color: @nordic-accent;
  }
}

.ep-section-label {
  font-size: @nordic-text-base;
  font-weight: 600;
  color: @nordic-text;
  white-space: nowrap;
  letter-spacing: -0.2px;
}

.ep-section-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, @nordic-divider 0%, transparent 100%);
}

.ep-section-content {
  padding-left: 36px;
}

/* 双列网格 */
.ep-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;
}

/* 数字输入包装 */
.ep-number-wrap {
  position: relative;
  width: 100%;
}

.ep-unit {
  position: absolute;
  right: 44px;
  top: 50%;
  transform: translateY(-50%);
  color: @nordic-text-muted;
  font-size: @nordic-text-sm;
  pointer-events: none;
  z-index: 1;
}

/* 提示文字 */
.ep-hint {
  font-size: @nordic-text-xs;
  color: @nordic-text-muted;
  margin-top: 4px;
}

.ep-error {
  color: @nordic-red;
  font-size: @nordic-text-xs;
  margin-top: 4px;
}

/* 饮食目标卡片 */
.ep-goal-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  width: 100%;
}

.ep-goal-card {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 14px 12px;
  border: 2px solid @nordic-border;
  border-radius: @nordic-radius-lg;
  cursor: pointer;
  transition: all @nordic-transition-base ease;
  background: @nordic-surface;
  user-select: none;

  &:hover {
    border-color: var(--goal-accent);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px @nordic-shadow-hover;
  }

  &--active {
    border-color: var(--goal-accent);
    background: fade(@nordic-accent-light, 40%);
    box-shadow: 0 2px 8px @nordic-shadow;

    .ep-goal-name {
      color: var(--goal-accent);
      font-weight: 600;
    }
    .ep-goal-check {
      opacity: 1;
      transform: scale(1);
    }
  }
}

.ep-goal-name {
  font-size: @nordic-text-base;
  color: @nordic-text;
  font-weight: 500;
  transition: color @nordic-transition-fast ease;
}

.ep-goal-check {
  position: absolute;
  top: 6px;
  right: 6px;
  color: var(--goal-accent);
  opacity: 0;
  transform: scale(0.5);
  transition: all @nordic-transition-fast ease;
}

/* 口味标签选项圆点 */
.ep-tag-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
  vertical-align: middle;
}

/* 底部按钮 */
.ep-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.ep-btn-cancel {
  min-width: 88px;
  height: 36px;
  border-radius: @nordic-radius-md;
  font-weight: 500;
}

.ep-btn-save {
  min-width: 120px;
  height: 36px;
  border-radius: @nordic-radius-md;
  font-weight: 600;
  background: linear-gradient(135deg, @nordic-accent 0%, @nordic-accent-dark 100%);
  border: none;
  letter-spacing: 0.5px;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px fade(@nordic-accent, 30%);
  }
}

/* 响应式优化 */
@media (max-width: @nordic-breakpoint-md) {
  .profile-content {
    flex-direction: column;
    align-items: center;
  }

  .user-info-section {
    align-items: center;
    padding-right: 0;
    min-width: auto;
    width: 100%;
  }

  .action-buttons {
    justify-content: center;
    width: 100%;
  }

  .action-btn {
    flex: 1;
    min-width: 100px;
  }

  .bottom-actions .el-button {
    min-width: 100px;
  }
}
</style>

<style lang="less">
@import '../../assets/css/nordic-theme.less';

/* 编辑资料对话框 - 容器级覆盖 (unscoped) */
.edit-profile-dialog {
  border-radius: @nordic-radius-lg;
  overflow: hidden;
  box-shadow:
    0 24px 80px rgba(0, 0, 0, 0.12),
    0 0 0 1px rgba(0, 0, 0, 0.04);

  .el-dialog__header {
    padding: 20px 24px 16px;
    margin-right: 0;
    border-bottom: 1px solid @nordic-divider;
    background: linear-gradient(180deg, @nordic-bg 0%, @nordic-surface 100%);
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-dialog__footer {
    padding: 16px 24px 20px;
    border-top: 1px solid @nordic-divider;
  }
}
</style>
