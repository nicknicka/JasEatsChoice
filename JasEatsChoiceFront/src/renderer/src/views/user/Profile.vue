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
            <span class="balance-number number-scroll">{{ userInfo.wallet?.balance || 0 }}</span>
            <span class="balance-unit">个</span>
          </div>
          <div class="wallet-summary">
            <div class="summary-item">
              <span class="summary-label">累计充值</span>
              <span class="summary-value">{{ userInfo.wallet?.totalRecharge || 0 }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">累计消费</span>
              <span class="summary-value">{{ userInfo.wallet?.totalConsume || 0 }}</span>
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
        <el-button type="text" size="small" @click="goToContact">
          <el-icon><Service /></el-icon>
          <span style="margin-left: 5px">联系客服</span>
        </el-button>
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
    <el-dialog v-model="editProfileDialogVisible" title="编辑资料" width="500px" center>
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
        style="margin-top: 20px"
      >
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" disabled />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" type="email" />
        </el-form-item>

        <el-form-item label="所在地" prop="location">
          <el-cascader
            v-model="selectedLocation"
            :options="cascaderData"
            :props="cascaderProps"
            placeholder="请选择所在地"
            clearable
            filterable
            style="width: 100%"
            @change="handleLocationChange"
          />
        </el-form-item>

        <el-form-item label="身高" prop="height">
          <el-input-number
            v-model="editForm.height"
            :min="30"
            :max="280"
            :precision="1"
            :step="0.1"
            controls-position="right"
            placeholder="请输入身高"
            style="width: 100%"
          />
          <span class="unit-hint">cm</span>
        </el-form-item>

        <el-form-item label="体重" prop="weight">
          <el-input-number
            v-model="editForm.weight"
            :min="5"
            :max="300"
            :precision="1"
            :step="0.1"
            controls-position="right"
            placeholder="请输入体重"
            style="width: 100%"
          />
          <span class="unit-hint">kg</span>
        </el-form-item>

        <el-form-item label="饮食目标" prop="dietGoal">
          <el-select v-model="editForm.dietGoal" placeholder="请选择饮食目标" style="width: 100%">
            <el-option label="减肥" value="减肥" />
            <el-option label="增肌" value="增肌" />
            <el-option label="保持健康" value="保持健康" />
          </el-select>
        </el-form-item>

        <el-form-item label="饮食偏好标签" prop="preferTags">
          <el-select
            v-model="editForm.preferTags"
            multiple
            filterable
            allow-create
            placeholder="请选择您的饮食偏好（可多选）"
            style="width: 100%"
          >
            <el-option label="🥬 蔬菜" value="蒸菜" />
            <el-option label="🥗 清淡" value="清淡" />
            <el-option label="🌶️ 重辣" value="重辣" />
            <el-option label="🥗 油腻" value="油腻" />
            <el-option label="🥬 素食" value="素食" />
            <el-option label="🍤 甜食" value="甜食" />
            <el-option label="🥦 健康" value="健康" />
          </el-select>
        </el-form-item>

        <el-form-item label="过敏信息" prop="allergies">
          <el-input
            v-model="editForm.allergies"
            type="textarea"
            :rows="3"
            placeholder="请输入您的过敏信息（如：花生、海鲜等）"
            clearable
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editProfileDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditProfile" :loading="saving">保存</el-button>
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
              <span style="float: right; color: #8492a6; font-size: 12px">
                <el-icon><InfoFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="问题反馈" value="bug">
              <span>问题反馈</span>
              <span style="float: right; color: #8492a6; font-size: 12px">
                <el-icon><WarningFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="使用咨询" value="inquiry">
              <span>使用咨询</span>
              <span style="float: right; color: #8492a6; font-size: 12px">
                <el-icon><QuestionFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="其他" value="other">
              <span>其他</span>
              <span style="float: right; color: #8492a6; font-size: 12px">
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
              <div style="font-size: 12px; color: #909399; margin-top: 5px">
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
  Service,
  ChatDotSquare,
  SwitchButton,
  DocumentCopy,
  QuestionFilled,
  WarningFilled,
  InfoFilled,
  MoreFilled,
  Plus,
  Promotion
} from '@element-plus/icons-vue'
import CommonAvatar from '../../components/CommonAvatar.vue'
import api from '../../utils/api'
import { API_CONFIG } from '../../config'
import QRCode from 'qrcode'

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
    totalConsume: 0
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
const cascaderData = ref([])
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
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  location: [{ required: false, message: '请选择所在地', trigger: ['blur', 'change'] }],
  height: [
    {
      type: 'number',
      min: 30,
      max: 280,
      message: '身高范围在 30 到 280 cm',
      trigger: ['blur', 'change']
    }
  ],
  weight: [
    {
      type: 'number',
      min: 5,
      max: 300,
      message: '体重范围在 5 到 300 kg',
      trigger: ['blur', 'change']
    }
  ],
  dietGoal: [{ required: true, message: '请选择饮食目标', trigger: 'change' }]
})

// 编辑表单引用
const editFormRef = ref(null)

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
      fetchAddressData(),
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
  const isUserInfoEmpty =
    !userStore.userInfo ||
    Object.keys(userStore.userInfo).length === 0 ||
    !userStore.userInfo.nickname ||
    !userStore.userInfo.phone ||
    !userStore.userInfo.avatar ||
    !userStore.userInfo.avatar.length

  if (isUserInfoEmpty) {
    console.log('从后端API获取用户信息')
    userInfo.value = await userStore.fetchUserInfo(userId)
  } else {
    userInfo.value = { ...userStore.userInfo }
  }

  console.log('用户信息:', userInfo.value)
}

// 获取钱包信息
const fetchWalletInfo = async () => {
  try {
    const userId = parseInt(authStore.userId || '0', 10)
    const walletResponse = await walletApi.getWalletInfo(userId)

    if (walletResponse.code === '200' && walletResponse.data) {
      userInfo.value.wallet = walletResponse.data
      console.log('钱包信息已更新:', walletResponse.data)
    }
  } catch (error) {
    console.error('获取钱包信息失败:', error)
    // 钱包信息获取失败不影响其他功能
  }
}

// 获取地址数据
const fetchAddressData = async () => {
  try {
    const response = await api.get('/v1/location/cascader')
    console.log('获取地址数据成功:', response)

    if (response.code === '200' && response.data) {
      cascaderData.value = response.data
    }
  } catch (error) {
    console.error('获取地址数据失败:', error)
    // 地址数据获取失败不影响其他功能
  }
}

// 获取健康数据（今日摄入、本周均衡度）
const fetchHealthData = async () => {
  try {
    const userId = authStore.userId || '0'

    // 获取今日营养摄入统计
    const todayResponse = await api.get(`/calorie-records/user/${userId}/today-summary`)
    if (todayResponse.code === '200' && todayResponse.data) {
      userInfo.value.todayCalorie = Math.round(todayResponse.data.totalCalorie || 0)
    }

    // 获取本周卡路里数据并计算均衡度
    const weekResponse = await api.get(`/calorie-records/user/${userId}/week`)
    if (weekResponse.code === '200' && weekResponse.data) {
      // 计算本周均衡度（简单算法：根据每日摄入是否在合理范围内）
      const weekData = weekResponse.data || []
      let totalDays = weekData.length
      let balancedDays = 0

      // 假设合理摄入范围是1500-2500卡路里
      const minCalorie = 1500
      const maxCalorie = 2500

      weekData.forEach(day => {
        const consumed = day.consumed || 0
        if (consumed >= minCalorie && consumed <= maxCalorie) {
          balancedDays++
        }
      })

      // 计算均衡度百分比
      userInfo.value.weekBalance = totalDays > 0 ? Math.round((balancedDays / totalDays) * 100) : 0
    }

    console.log('健康数据:', {
      todayCalorie: userInfo.value.todayCalorie,
      weekBalance: userInfo.value.weekBalance
    })
  } catch (error) {
    console.error('获取健康数据失败:', error)
    userInfo.value.todayCalorie = 0
    userInfo.value.weekBalance = 0
  }
}

// 获取订单统计
const fetchOrderStats = async () => {
  try {
    const userId = authStore.userId || '0'
    const response = await api.get(`/v1/orders/user/${userId}/statistics`)

    if (response.code === '200' && response.data) {
      userInfo.value.orders = {
        inProgress: response.data.inProgress || 0,
        pending: response.data.pending || 0,
        pendingComment: response.data.pendingComment || 0
      }
    } else {
      userInfo.value.orders = {
        inProgress: 0,
        pending: 0,
        pendingComment: 0
      }
    }

    console.log('订单统计:', userInfo.value.orders)
  } catch (error) {
    console.error('获取订单统计失败:', error)
    userInfo.value.orders = {
      inProgress: 0,
      pending: 0,
      pendingComment: 0
    }
  }
}

// 获取收藏数量
const fetchCollectionCount = async () => {
  try {
    const userId = authStore.userId || '0'
    const response = await api.get('/v1/collections', {
      params: { userId }
    })

    if (response.code === '200' && response.data) {
      userInfo.value.collections = response.data.length || 0
    } else {
      userInfo.value.collections = 0
    }

    console.log('收藏数量:', userInfo.value.collections)
  } catch (error) {
    console.error('获取收藏数量失败:', error)
    userInfo.value.collections = 0
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

      console.log('update avatar response:', response)
      if (response.code === '200') {
        console.log('update avatar success')
        userInfo.value = await userStore.fetchUserInfo(userId)
        ElMessage.success('头像上传成功')
      } else {
        ElMessage.error('头像上传失败: ' + response.message)
      }
    } catch (error) {
      console.error('Avatar upload failed:', error)
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

// 跳转到联系客服页面
const goToContact = () => {
  router.push('/user/home/contact')
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
    console.log('feedbackForm:', feedbackForm)
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

          console.log('提交反馈数据:', submitData)

          // 发送POST请求提交反馈
          const response = await api.post(API_CONFIG.user.feedback, submitData)

          console.log('反馈提交响应:', response)
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
const handleFeedbackTypeChange = (value) => {
  console.log('反馈类型变化:', value)
  // 可以根据类型动态调整表单提示等
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
    ElMessage.success('图片上传成功')
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
const handleImageRemove = (file, fileList) => {
  console.log('移除图片:', file)
  // 从表单中移除对应的图片
  const index = feedbackForm.images.findIndex(img => img.name === file.name)
  if (index > -1) {
    feedbackForm.images.splice(index, 1)
  }
}

// 资料编辑功能
// 编辑资料
const editProfile = () => {
  console.log('userInfo:', userInfo.value)

  // 将当前用户信息填充到编辑表单
  Object.assign(editForm, {
    nickname: userInfo.value.nickname || '',
    phone: userInfo.value.phone || '',
    email: userInfo.value.email || '',
    location: userInfo.value.location || '',
    height: Number(userInfo.value.height) || 0,
    weight: Number(userInfo.value.weight) || 0,
    dietGoal: userInfo.value.dietGoal || '',
    preferTags: userInfo.value.preferTags || [],        // ✅ 映射饮食偏好标签
    allergies: userInfo.value.allergies || ''          // ✅ 映射过敏信息
  })

  // 初始化地址选择器
  initLocationSelect(userInfo.value.location || '')

  // 打开编辑资料对话框
  editProfileDialogVisible.value = true
}

// 地址选择变化处理
const handleLocationChange = (value) => {
  if (value && value.length > 0) {
    editForm.location = value.join(' ')
  } else {
    editForm.location = ''
  }
}

// 初始化地址选择器
const initLocationSelect = (location) => {
  if (!location) {
    selectedLocation.value = []
    return
  }

  const parts = location.split(' ').filter(Boolean)
  selectedLocation.value = parts
}

// 更新保存编辑的资料方法
const saveEditProfile = () => {
  if (editFormRef.value) {
    console.log('editForm:', editForm)
    editFormRef.value.validate(async (valid) => {
      if (valid) {
        try {
          saving.value = true

          const userId = parseInt(String(authStore.userId || 1), 10)

          // 发送PUT请求更新用户资料
          const response = await api.put(
            API_CONFIG.user.update.replace('{userId}', userId),
            editForm
          )

          console.log('更新用户信息响应:', response)
          if (response.code === '200') {
            // 更新本地用户信息
            const updatedUserInfo = { ...userInfo.value, ...editForm }
            userInfo.value = updatedUserInfo

            // 更新store中的用户信息并保存到localStorage
            userStore.setUserInfo(updatedUserInfo)

            // 关闭对话框
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
      } else {
        ElMessage.error('表单验证失败，请检查输入')
      }
    })
  }
}

// 退出登录
const logout = () => {
  ElMessageBox.confirm('确认要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      // 清除localStorage中的所有用户相关数据
      localStorage.removeItem('userInfo')
      localStorage.removeItem('userAvatar')
      localStorage.removeItem('phone')
      localStorage.removeItem('userId')
      localStorage.removeItem('token')

      // 清除Store中的用户信息
      authStore.clearAuth()
      userStore.clearUserInfo()

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
    await navigator.clipboard.writeText(shareLink.value)
    ElMessage.success('分享链接已复制到剪贴板')
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

<style scoped>
/* 基础容器样式 */
.profile-container {
  padding: 0 20px 20px 20px;
  min-height: 100vh;
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.profile-card {
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background-color: #fff;
}

/* 标题样式 */
.profile-container h2 {
  font-size: 28px;
  margin: 0 0 25px 0;
  color: #333;
  font-weight: 700;
}

.module-title {
  font-size: 18px;
  margin: 0 0 20px 0;
  font-weight: 700;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 顶部头像区域 */
.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 25px;
  padding: 25px;
  flex-wrap: wrap;
}

.user-name-container {
  width: 100%;
  text-align: center;
}

.user-name {
  font-size: 32px;
  font-weight: 800;
  margin: 10px 0 20px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 2px 2px 4px rgba(102, 126, 234, 0.15);
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
  padding-right: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.user-basic-info {
  margin-bottom: 20px;
}

/* 用户统计信息 */
.user-stats {
  font-size: 14px;
  margin-bottom: 20px;
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
  gap: 4px;
  text-align: center;
  min-width: clamp(120px, 20vw, 140px);
}

.stat-label {
  font-size: 14px;
  font-weight: 500;
  color: #718096;
}

.user-stats .stat-label {
  color: #606266;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
}

.user-stats .stat-value {
  font-weight: bold;
  color: #ff6b6b;
}

.calorie-highlight {
  color: #ff6b6b;
}

.balance-highlight {
  color: #48bb78;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  transition: all 0.2s ease;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 订单统计 */
.order-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}

.order-stat-card {
  flex: 1;
  min-width: 140px;
  padding: 20px;
  background: linear-gradient(135deg, #f0f4f8 0%, #e2e8f0 100%);
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  cursor: pointer;
}

.order-stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.order-stat-card .stat-value {
  font-size: 32px;
  color: #2d3748;
  margin-bottom: 6px;
}

.order-stat-card .stat-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 2px;
}

.order-in-progress {
  color: #2b6cb0;
}

.order-pending {
  color: #dd6b20;
}

.order-pending-comment {
  color: #805ad5;
}

/* 钱包模块 */
.wallet-card {
  background: linear-gradient(135deg, #fef5e7 0%, #fdebd0 100%);
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.wallet-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(214, 158, 46, 0.2);
}

.wallet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.wallet-label {
  font-size: 16px;
  color: #718096;
  font-weight: 500;
}

.wallet-hint {
  font-size: 14px;
  color: #d69e2e;
  font-weight: 500;
}

.wallet-balance {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 20px;
}

.balance-number {
  font-size: 48px;
  font-weight: 700;
  color: #d69e2e;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.balance-unit {
  font-size: 18px;
  color: #d69e2e;
  font-weight: 500;
}

.wallet-summary {
  display: flex;
  gap: 30px;
  padding-top: 15px;
  border-top: 1px solid rgba(214, 158, 46, 0.2);
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.summary-label {
  font-size: 13px;
  color: #718096;
}

.summary-value {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

/* 其他模块 */
.other-modules {
  margin-bottom: 20px;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.module-item-card {
  background: linear-gradient(135deg, #ebf8ff 0%, #bee3f8 100%);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.module-item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(190, 227, 248, 0.3);
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
  background: rgba(43, 108, 176, 0.1);
  border-radius: 8px;
  color: #2b6cb0;
}

.module-item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.module-item-title {
  font-size: 16px;
  font-weight: 600;
  color: #2b6cb0;
}

.module-item-desc {
  font-size: 14px;
  color: #718096;
}

.module-item-btn {
  color: #2b6cb0;
  font-weight: 600;
}

/* 底部操作按钮 */
.bottom-actions {
  margin-top: 30px;
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.bottom-actions .el-button {
  flex: 1;
  min-width: 120px;
  height: 40px;
  border-radius: 8px;
  font-weight: 600;
  transition: transform 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bottom-actions .el-button:hover {
  transform: translateY(-2px);
}

.bottom-actions .el-button:nth-child(1) {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  border: none;
  color: #fff;
}

.bottom-actions .el-button:nth-child(2) {
  background: linear-gradient(135deg, #9f7aea 0%, #805ad5 100%);
  border: none;
  color: #fff;
}

.bottom-actions .el-button:nth-child(3) {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  border: none;
  color: #fff;
}

/* 分享对话框样式 */
.share-content {
  padding: 20px 0;
}

.share-link-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
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
  border-top: 1px solid #eee;
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
  color: #909399;
  font-size: 14px;
}

/* 响应式优化 */
@media (max-width: 768px) {
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
