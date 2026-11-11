<template>
  <div class="profile-container">
    <AppPageHeader title="个人中心" />

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

    <div v-else-if="profileError" class="profile-card">
      <el-empty :description="profileError" />
    </div>

    <div v-else class="profile-card">
      <!-- 资料头部：身份 + 动作（唯一实心主按钮 = 编辑资料，§4.1） -->
      <section class="profile-hero">
        <div class="profile-hero__identity">
          <div class="avatar-wrapper">
            <CommonAvatar
              :avatar-url="avatarSrc"
              :fallback-text="userInfo.nickname || '未设置'"
              :size="108"
              :show-upload="true"
              :show-upload-button="false"
              :click-to-enlarge="true"
              ref="commonAvatarRef"
              @upload="handleAvatarUpload"
            />
          </div>

          <div class="identity-copy">
            <span class="identity-label">个人资料</span>
            <h2 class="user-name">{{ userInfo.nickname || '未设置' }}</h2>
            <div class="identity-meta">
              <span class="meta-chip">
                <el-icon><Iphone /></el-icon>
                <span>{{ userInfo.phone || '未绑定手机号' }}</span>
              </span>
              <span class="meta-chip">
                <el-icon><Location /></el-icon>
                <span>{{ userInfo.location || '未设置所在地' }}</span>
              </span>
            </div>
          </div>
        </div>

        <div class="profile-actions">
          <el-button class="action-btn" @click="triggerAvatarUpload">
            <el-icon><Camera /></el-icon>
            <span>更换头像</span>
          </el-button>
          <el-button class="action-btn" @click="shareProfile">
            <el-icon><Share /></el-icon>
            <span>分享</span>
          </el-button>
          <el-button type="primary" class="action-btn" @click="editProfile">
            <el-icon><Edit /></el-icon>
            <span>编辑资料</span>
          </el-button>
        </div>
      </section>

      <!-- 资产 / 行为摘要：指标卡合并为一条四列摘要（§4.8） -->
      <section class="asset-summary">
        <div class="summary-card">
          <span class="summary-card__icon"><el-icon><Wallet /></el-icon></span>
          <span class="summary-card__value">
            {{ formatNumber(userInfo.wallet?.balance || 0) }}<i>个</i>
          </span>
          <span class="summary-card__label">平台币余额</span>
        </div>
        <div class="summary-card">
          <span class="summary-card__icon"><el-icon><DataLine /></el-icon></span>
          <span class="summary-card__value">{{ userInfo.todayCalorie || 0 }}<i>kcal</i></span>
          <span class="summary-card__label">今日摄入</span>
        </div>
        <div class="summary-card">
          <span class="summary-card__icon"><el-icon><Check /></el-icon></span>
          <span class="summary-card__value">{{ userInfo.weekBalance || 0 }}<i>%</i></span>
          <span class="summary-card__label">本周均衡度</span>
        </div>
        <div class="summary-card">
          <span class="summary-card__icon"><el-icon><Document /></el-icon></span>
          <span class="summary-card__value">{{ userInfo.orders?.inProgress || 0 }}<i>笔</i></span>
          <span class="summary-card__label">进行中订单</span>
        </div>
      </section>

      <!-- 快捷入口：订单 / 钱包 / 饮食记录 / 设置（固定顺序 + 同一套中性卡面，§4.2） -->
      <section class="entry-grid entry-grid--primary">
        <button type="button" class="entry-card" @click="goToAllOrders">
          <span class="entry-card__icon"><el-icon><Document /></el-icon></span>
          <span class="entry-card__text">
            <span class="entry-card__title">订单</span>
            <span class="entry-card__desc">
              进行中 {{ userInfo.orders?.inProgress || 0 }} · 待确认
              {{ userInfo.orders?.pending || 0 }} · 待评价
              {{ userInfo.orders?.pendingComment || 0 }}
            </span>
          </span>
        </button>
        <button type="button" class="entry-card" @click="goToWalletManagement">
          <span class="entry-card__icon"><el-icon><Wallet /></el-icon></span>
          <span class="entry-card__text">
            <span class="entry-card__title">钱包</span>
            <span class="entry-card__desc">
              余额 {{ formatNumber(userInfo.wallet?.balance || 0) }} 个
            </span>
          </span>
        </button>
        <button type="button" class="entry-card" @click="goToDietRecord">
          <span class="entry-card__icon"><el-icon><DataLine /></el-icon></span>
          <span class="entry-card__text">
            <span class="entry-card__title">饮食记录</span>
            <span class="entry-card__desc">记录每日饮食与摄入</span>
          </span>
        </button>
        <button type="button" class="entry-card" @click="goToSettings">
          <span class="entry-card__icon"><el-icon><Setting /></el-icon></span>
          <span class="entry-card__text">
            <span class="entry-card__title">设置</span>
            <span class="entry-card__desc">账号与偏好设置</span>
          </span>
        </button>
      </section>

      <!-- 其他入口：我的收藏 / 我的地址（沿用同一中性卡面） -->
      <section class="entry-grid entry-grid--secondary">
        <button type="button" class="entry-card" @click="goToMyCollection">
          <span class="entry-card__icon"><el-icon><StarFilled /></el-icon></span>
          <span class="entry-card__text">
            <span class="entry-card__title">我的收藏</span>
            <span class="entry-card__desc">共 {{ userInfo.collections || 0 }} 个</span>
          </span>
        </button>
        <button type="button" class="entry-card" @click="goToAddress">
          <span class="entry-card__icon"><el-icon><Location /></el-icon></span>
          <span class="entry-card__text">
            <span class="entry-card__title">我的地址</span>
            <span class="entry-card__desc">
              共 {{ userInfo.addresses || 0 }} 个 · 默认 {{ userInfo.defaultAddress || '未设置' }}
            </span>
          </span>
        </button>
      </section>

      <!-- 底部次级操作 -->
      <footer class="bottom-actions">
        <el-button type="text" @click="submitFeedback">
          <el-icon><ChatDotSquare /></el-icon>
          <span>反馈建议</span>
        </el-button>
        <el-button type="text" danger @click="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-button>
      </footer>
    </div>

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
                  v-if="cascaderData.length > 0"
                  v-model="selectedLocation"
                  :options="cascaderData"
                  :props="cascaderProps"
                  :loading="locationDataLoading"
                  placeholder="选择省 / 市 / 区"
                  clearable
                  filterable
                  style="width: 100%"
                  @change="handleLocationChange"
                />
                <el-input
                  v-else
                  :model-value="editForm.location"
                  placeholder="选择省 / 市 / 区"
                  disabled
                />
                <div v-if="cascaderData.length === 0 && !locationDataLoading" class="ep-error">
                  地址数据加载失败，当前仅展示已保存资料
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
              <span style="float: right; color: var(--ink-400); font-size: var(--text-caption)">
                <el-icon><InfoFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="问题反馈" value="bug">
              <span>问题反馈</span>
              <span style="float: right; color: var(--ink-400); font-size: var(--text-caption)">
                <el-icon><WarningFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="使用咨询" value="inquiry">
              <span>使用咨询</span>
              <span style="float: right; color: var(--ink-400); font-size: var(--text-caption)">
                <el-icon><QuestionFilled /></el-icon>
              </span>
            </el-option>
            <el-option label="其他" value="other">
              <span>其他</span>
              <span style="float: right; color: var(--ink-400); font-size: var(--text-caption)">
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
              <div style="margin-top: var(--space-1); color: var(--ink-500); font-size: var(--text-caption)">
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
  Wallet,
  Setting,
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
import AppPageHeader from '../../components/common/AppPageHeader.vue'
import api from '../../utils/api'
import { createFeedback } from '../../api/feedback'
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
const profileError = ref('')

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
  createdAt: '',
  updatedAt: null
})

// 组件引用
const commonAvatarRef = ref(null)

// 计算属性
// 头像来源 - 统一使用userStore中的头像信息
const avatarSrc = computed(() => {
  return userInfo.value.avatar
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
  { value: '减肥', label: '减肥', color: 'var(--success)' },
  { value: '增肌', label: '增肌', color: 'var(--info)' },
  { value: '保持健康', label: '保持健康', color: 'var(--warning)' }
]

// 口味标签选项
const tasteTagOptions = [
  { label: '蔬菜', value: '蒸菜', color: 'var(--success)' },
  { label: '清淡', value: '清淡', color: 'var(--info)' },
  { label: '重辣', value: '重辣', color: 'var(--danger)' },
  { label: '油腻', value: '油腻', color: 'var(--warning)' },
  { label: '素食', value: '素食', color: 'var(--brand-500)' },
  { label: '甜食', value: '甜食', color: 'var(--brand-500)' },
  { label: '健康', value: '健康', color: 'var(--success)' }
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

  try {
    profileError.value = ''
    const freshInfo = await userStore.fetchUserInfo(userId)
    if (!freshInfo) {
      throw new Error('用户资料为空')
    }
    userInfo.value = { ...userInfo.value, ...freshInfo }
  } catch (error) {
    profileError.value = '个人资料加载失败，请刷新重试'
    throw error
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

// 跳转到饮食记录页面
const goToDietRecord = () => {
  router.push('/user/home/diet-record')
}

// 跳转到设置页面
const goToSettings = () => {
  router.push('/user/home/settings')
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

          // 标题并入正文，归属由后端从可信 JWT 推导
          const content = [feedbackForm.title, feedbackForm.content]
            .map((part) => (part ? String(part).trim() : ''))
            .filter(Boolean)
            .join('\n\n')

          const response = await createFeedback({
            type: feedbackForm.type,
            content,
            contact: feedbackForm.contact || undefined,
            images: feedbackForm.images.map(img => img.url || img.response?.data?.url || img.url)
          })

          if (response.success) {
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
const normalizePreferTags = (preferTags) => {
  if (Array.isArray(preferTags)) {
    return preferTags
  }
  if (!preferTags || typeof preferTags !== 'object') {
    return []
  }
  return Object.values(preferTags)
    .flat()
    .filter((value) => value !== null && value !== undefined && value !== '')
    .map((value) => String(value))
}

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
    preferTags: normalizePreferTags(userInfo.value.preferTags),
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

/* 基础容器样式：暖白页面底（§3.1） */
.profile-container {
  padding: 0 var(--space-6) var(--space-6);
  min-height: 100vh;
  background: var(--canvas);
}

.loading-container {
  padding: var(--space-10);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

/* 资料卡：L1 微质感（§3.4） */
.profile-card {
  padding: var(--space-6);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

/* 资料头部 */
.profile-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: var(--space-6);
  align-items: center;
}

.profile-hero__identity {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  min-width: 0;
}

.avatar-wrapper {
  flex: 0 0 auto;
}

.identity-copy {
  min-width: 0;
}

.identity-label {
  display: inline-block;
  margin-bottom: var(--space-1);
  color: var(--brand-600);
  font-size: var(--text-caption);
  font-weight: var(--weight-semibold);
  letter-spacing: var(--tracking-wide);
}

/* 昵称：页内区块标题从 h2 起（§4.7） */
.user-name {
  margin: 0;
  color: var(--ink-900);
  font-size: var(--text-display);
  font-weight: var(--weight-bold);
  letter-spacing: var(--tracking-tight);
  line-height: 1.12;
}

.identity-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-top: var(--space-3);
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  min-width: 0;
  max-width: 100%;
  padding: var(--space-1) var(--space-3);
  color: var(--ink-500);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-full);
  font-size: var(--text-caption);

  .el-icon {
    flex: 0 0 auto;
    color: var(--ink-400);
  }

  span:last-child {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.profile-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: var(--space-2);
}

/* 页头动作区控件高度取 md（§3.4）；hover/focus 走 --duration-fast（§3.5） */
.action-btn {
  height: var(--control-height-md);
  padding: 0 var(--space-4);
  margin-left: 0;
  transition:
    color var(--duration-fast) var(--ease-out),
    background-color var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out);
}

/* 资产 / 行为摘要：一条四列（§4.8） */
.asset-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-4);
  margin-top: var(--space-6);
}

.summary-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  min-width: 0;
  padding: var(--space-5);
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.summary-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: var(--space-8);
  height: var(--space-8);
  border-radius: var(--radius-md);
  color: var(--brand-500);
  background: var(--brand-50);
  font-size: var(--text-h3);
}

.summary-card__value {
  color: var(--ink-900);
  font-family: var(--font-num);
  font-size: var(--text-h1);
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
  line-height: normal;

  i {
    margin-left: var(--space-1);
    color: var(--ink-500);
    font-family: var(--font-sans);
    font-size: var(--text-body-sm);
    font-style: normal;
    font-weight: var(--weight-medium);
  }
}

.summary-card__label {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  font-weight: var(--weight-medium);
}

/* 快捷入口：同一套中性卡面（§4.2） */
.entry-grid {
  display: grid;
  gap: var(--space-4);
  margin-top: var(--space-6);
}

.entry-grid--primary {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.entry-grid--secondary {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: var(--space-4);
}

.entry-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  width: 100%;
  min-width: 0;
  padding: var(--space-5);
  font-family: inherit;
  text-align: left;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition:
    box-shadow var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out),
    border-color var(--duration-fast) var(--ease-out);

  &:hover {
    border-color: var(--line-strong);
    box-shadow: var(--shadow-md);
    transform: translateY(-1px);
  }

  &:focus-visible {
    outline: none;
    box-shadow: 0 0 0 2px var(--brand-400);
  }

  &:active {
    transform: translateY(0);
    border-color: var(--brand-400);
  }
}

.entry-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: var(--space-10);
  height: var(--space-10);
  flex: 0 0 auto;
  border-radius: var(--radius-md);
  color: var(--brand-500);
  background: var(--brand-50);
  font-size: var(--text-h2);
}

.entry-card__text {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.entry-card__title {
  color: var(--ink-900);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  line-height: normal;
}

.entry-card__desc {
  color: var(--ink-500);
  font-size: var(--text-body-sm);
  line-height: normal;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 底部次级操作：控件高度取 sm（§3.4）；危险动作与普通动作留 --space-6 间距（§4.1） */
.bottom-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-6);
  margin-top: var(--space-6);
  padding-top: var(--space-6);
  border-top: 1px solid var(--line);

  .el-button {
    height: var(--control-height-sm);
    padding: 0 var(--space-3);
    margin-left: 0;
    transition: color var(--duration-fast) var(--ease-out);
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
  border-radius: @nordic-radius-md;
  font-weight: 500;
}

.ep-btn-save {
  min-width: 120px;
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
@media (max-width: 1200px) {
  .profile-hero {
    grid-template-columns: minmax(0, 1fr);
    gap: var(--space-4);
  }

  .profile-actions {
    justify-content: flex-start;
  }

  .asset-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .entry-grid--primary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .entry-grid--secondary {
    grid-template-columns: minmax(0, 1fr);
  }

  .entry-card__desc {
    white-space: normal;
  }
}

@media (max-width: 768px) {
  .profile-card {
    padding: var(--space-4);
  }

  .profile-hero__identity {
    align-items: flex-start;
    gap: var(--space-4);
  }

  .user-name {
    font-size: var(--text-h1);
  }

  .profile-actions {
    width: 100%;
  }

  .profile-actions .el-button {
    flex: 1 1 auto;
  }

  .asset-summary,
  .entry-grid--primary {
    grid-template-columns: minmax(0, 1fr);
  }

  .bottom-actions {
    flex-direction: column-reverse;
    align-items: stretch;

    .el-button {
      width: 100%;
    }
  }
}
</style>

<style lang="less">
@import '../../assets/css/nordic-theme.less';

/* 编辑资料对话框 - 容器级覆盖 (unscoped) */
.edit-profile-dialog {
  border-radius: @nordic-radius-lg;
  overflow: hidden;
  box-shadow: var(--shadow-lg);

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
