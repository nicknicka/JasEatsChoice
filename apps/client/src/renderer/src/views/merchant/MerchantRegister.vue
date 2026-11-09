<template>
  <div class="register-container">
    <!-- 装饰光球：与 01–03 公共端基调一致 -->
    <div class="orb orb-1"></div>
    <div class="orb orb-2"></div>
    <div class="orb orb-3"></div>

    <!-- 自定义标题栏：认证页统一带标题栏（§4.7 每页唯一 h1） -->
    <WindowTitleBar />

    <main class="register-panel">
      <!-- 标题与表单共用同一中轴（§4.4） -->
      <header class="register-header">
        <h1 class="register-title">商户注册</h1>
        <CommonBackButton
          class="register-back-btn"
          :use-router-back="false"
          @click="goToUserHome"
        />
      </header>

      <!-- 认证类窄列：沿用 AppFormContainer 契约，宽度只取 --form-width-auth / --form-width-settings 两档（§3.3 / §4.4） -->
      <AppFormContainer variant="auth">
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
        >
          <el-form-item label="商户名称" prop="merchantName">
            <el-input
              v-model="registerForm.merchantName"
              placeholder="请输入商户名称"
              autocomplete="off"
            />
          </el-form-item>

          <el-form-item label="营业执照号" prop="businessLicense">
            <el-input v-model="registerForm.businessLicense" placeholder="请输入营业执照号" />
          </el-form-item>

          <el-form-item label="经营范围" prop="businessScope">
            <el-select
              ref="businessScopeSelect"
              v-model="registerForm.businessScope"
              placeholder="请选择经营范围"
              multiple
              style="width: 100%"
              @change="handleBusinessScopeChange"
            >
              <el-option label="中餐" value="中餐" />
              <el-option label="西餐" value="西餐" />
              <el-option label="快餐" value="快餐" />
              <el-option label="甜点" value="甜点" />
              <el-option label="饮品" value="饮品" />

              <!-- 自定义选项部分 -->
              <template #footer>
                <div v-if="!isAddingCustomScope" style="padding: 8px">
                  <el-button text size="small" block @click="isAddingCustomScope = true">
                    + 添加自定义经营范围
                  </el-button>
                </div>
                <div v-else class="select-custom-footer" style="padding: 8px 10px">
                  <el-input
                    v-model="customBusinessScope"
                    placeholder="请输入自定义经营范围"
                    size="small"
                    style="margin-bottom: 8px"
                    @keyup.enter="addCustomBusinessScope"
                  />
                  <div style="display: flex; gap: 8px">
                    <el-button type="primary" size="small" block @click="addCustomBusinessScope">
                      确定
                    </el-button>
                    <el-button
                      size="small"
                      block
                      @click="
                        () => {
                          isAddingCustomScope = false
                          customBusinessScope = ''
                        }
                      "
                    >
                      取消
                    </el-button>
                  </div>
                </div>
              </template>
            </el-select>
          </el-form-item>

          <el-form-item label="联系人姓名" prop="contactName">
            <el-input v-model="registerForm.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>

          <el-form-item label="联系人电话" prop="contactPhone">
            <el-input v-model="registerForm.contactPhone" placeholder="请输入联系人电话" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" type="email" />
          </el-form-item>

          <el-form-item label="验证码" prop="captcha">
            <div class="captcha-row">
              <el-input v-model="registerForm.captcha" placeholder="请输入验证码" />
              <img
                v-if="captchaImage"
                :src="captchaImage"
                class="captcha-img"
                alt="验证码"
                @click="getCaptcha"
              />
              <el-link :underline="false" class="captcha-refresh-link" @click="getCaptcha">
                刷新
              </el-link>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="register-submit-btn"
              :loading="isSubmitting"
              :disabled="isSubmitting"
              @click="submitForm"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>
      </AppFormContainer>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElForm, ElFormItem, ElInput, ElButton, ElSelect, ElOption } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '../../utils/api'
import { API_CONFIG } from '../../config/index.js'
import { useAuthStore } from '../../store/authStore'
import { useUserStore } from '../../store/userStore'
import CommonBackButton from '../../components/common/CommonBackButton.vue'
import AppFormContainer from '../../components/common/AppFormContainer.vue'
import WindowTitleBar from '../../components/WindowTitleBar.vue'

const router = useRouter()

// 注册表单数据
const registerForm = reactive({
  merchantName: '',
  businessLicense: '',
  businessScope: [],
  contactName: '',
  contactPhone: '',
  email: '',
  captcha: ''
})

// 自定义经营范围相关
const isAddingCustomScope = ref(false) // 是否正在添加自定义经营范围
const customBusinessScope = ref('') // 用于存储自定义经营范围输入

// 自定义经营范围变化处理
const handleBusinessScopeChange = (value) => {
  // 检查是否选择了"自定义"选项
  // 如果取消选择"自定义"，则清空输入框
  if (!value.includes('自定义')) {
    customBusinessScope.value = ''
  }
}

// 添加自定义经营范围
const addCustomBusinessScope = () => {
  if (customBusinessScope.value.trim()) {
    // 如果"自定义"选项还在列表中，先移除它
    const index = registerForm.businessScope.indexOf('自定义')
    if (index !== -1) {
      registerForm.businessScope.splice(index, 1)
    }

    // 添加自定义经营范围
    registerForm.businessScope.push(customBusinessScope.value.trim())

    // 清空输入框
    customBusinessScope.value = ''
  }
}

// 表单验证规则
const registerRules = reactive({
  merchantName: [
    { required: true, message: '请输入商户名称', trigger: 'blur' },
    { min: 2, max: 50, message: '商户名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  businessLicense: [
    { required: true, message: '请输入营业执照号', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9]+$/,
      message: '营业执照号只能包含字母和数字',
      trigger: ['blur', 'change']
    }
  ],
  businessScope: [{ required: true, message: '请选择经营范围', trigger: 'change' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系人电话', trigger: ['blur', 'change'] },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: ['blur', 'change'] }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: ['blur', 'change'] },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
})

// 验证码相关
const captchaImage = ref('') // 验证码图片
const captchaKey = ref('') // 验证码Key（用于后端验证）

// 从后端获取验证码
let captchaRequestId = 0

const getCaptcha = async () => {
  const requestId = ++captchaRequestId
  try {
    const response = await api.get('/v1/captcha/checkCode') // 调用后端验证码接口
    if (requestId !== captchaRequestId) return
    // 添加调试信息，查看响应结构
    // console.log('验证码接口完整响应:', response);
    // console.log('验证码接口响应data:', response.data);

    // 智能判断响应结构
    const captchaData = response.data
    if (captchaData) {
      // Base64图片需要添加前缀才能正确显示
      captchaImage.value = 'data:image/png;base64,' + captchaData.checkCode
      captchaKey.value = captchaData.checkCodeKey // 验证码唯一标识
    }
  } catch (error) {
    if (requestId !== captchaRequestId) return
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败，请稍后重试')
  }
}

// 表单引用
const registerFormRef = ref(null)

// 提交中状态：loading 与防重复点击（§4.1 / §4.6）
const isSubmitting = ref(false)

// 经营范围选择器引用
const businessScopeSelect = ref(null)

// 跳转到用户首页
const goToUserHome = () => {
  router.push('/user/home')
}

// 页面加载时获取验证码
onMounted(() => {
  getCaptcha()
})

// 提交表单
const submitForm = () => {
  if (registerFormRef.value) {
    registerFormRef.value.validate((valid) => {
      if (valid) {
        const authStore = useAuthStore()
        if (!authStore.token) {
          ElMessage.error('请先登录后再注册商家')
          return
        }
        if (isSubmitting.value) return
        isSubmitting.value = true

        // 准备后端需要的数据结构
        const merchantData = {
          name: registerForm.merchantName,
          phone: registerForm.contactPhone,
          businessLicense: registerForm.businessLicense,
          businessScope: registerForm.businessScope, // 直接发送数组
          contactName: registerForm.contactName,
          email: registerForm.email,
          captcha: registerForm.captcha,
          captchaKey: captchaKey.value
        }

        // 调用商家注册API
        api
          .post(API_CONFIG.merchant.register, merchantData)
          .then((apiResponse) => {
            if (!apiResponse?.success) {
              console.error('注册失败:', apiResponse)
              ElMessage.error(apiResponse?.message || '注册失败，请稍后重试')
              getCaptcha() // 注册失败时刷新验证码
              return
            }

            if (!apiResponse.data?.merchantId) {
              console.error('注册失败: 无效的API响应', apiResponse)
              ElMessage.error('注册失败: 服务器返回无效响应')
              getCaptcha() // 注册失败时刷新验证码
              return
            }

            ElMessage.success('注册成功！')
            getCaptcha() // 注册成功时刷新验证码

            const userStore = useUserStore()

            console.log('商家注册响应:', apiResponse.data)
            // 保存用户信息和角色
            const merchantInfo = {
              merchantId: apiResponse.data.merchantId,
              name: apiResponse.data.name,
              phone: apiResponse.data.phone,
              email: apiResponse.data.email,
              businessLicense: apiResponse.data.businessLicense,
              businessScope: apiResponse.data.businessScope,
              contactName: apiResponse.data.contactName,
              avatar: apiResponse.data.avatar,
              rating: apiResponse.data.rating,
              status: apiResponse.data.status,
              businessHours: apiResponse.data.businessHours
            }

            // 使用 Pinia 存储商家信息
            userStore.setMerchantInfo(merchantInfo)

            // 使用 Pinia 存储商家ID到认证信息
            authStore.setMerchantId(merchantInfo.merchantId)

            // 假设商家注册成功后，后端会返回一个真实的 token
            // const token = response.data.data;
            // authStore.setToken(token);

            // 注册成功后跳转到用户首页
            setTimeout(() => {
              router.push('/user/home')
            }, 1500)
          })
          .catch((error) => {
            console.error('注册失败:', error)
            ElMessage.error(error?.message || '注册失败，请稍后重试')
            getCaptcha() // 注册失败时刷新验证码
          })
          .finally(() => {
            isSubmitting.value = false
          })
      } else {
        ElMessage.error('表单验证失败，请检查输入')
      }
    })
  }
}
</script>

<style scoped lang="less">
.register-container {
  box-sizing: border-box;
  width: 100%;
  min-height: 100vh;
  background: var(--canvas);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  overflow: auto;
  font-family: var(--font-sans);
}

// 装饰光球：与 01–03 公共端基调一致（§3.4）
.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
}

.orb-1 {
  width: 260px;
  height: 260px;
  background: radial-gradient(
    circle,
    color-mix(in srgb, var(--brand-500) 20%, transparent) 0%,
    transparent 70%
  );
  top: -70px;
  left: -50px;
}

.orb-2 {
  width: 180px;
  height: 180px;
  background: radial-gradient(
    circle,
    color-mix(in srgb, var(--brand-400) 18%, transparent) 0%,
    transparent 70%
  );
  bottom: 30px;
  right: -50px;
}

.orb-3 {
  width: 120px;
  height: 120px;
  background: radial-gradient(
    circle,
    color-mix(in srgb, var(--brand-500) 12%, transparent) 0%,
    transparent 70%
  );
  top: 50%;
  left: 65%;
}

// 认证窄列：panel 用 margin-block:auto 承担垂直居中；
// 根容器不加 justify-content，避免把 WindowTitleBar 一并拽到页面中段
.register-panel {
  box-sizing: border-box;
  width: min(100%, var(--form-width-auth));
  margin-block: auto;
  padding-block: var(--space-6);
  position: relative;
  z-index: 2;
}

// 标题 + 右侧返回动作区，动作区控件取 md（§4.7）
.register-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.register-title {
  margin: 0;
  font-family: var(--font-sans);
  font-size: var(--text-h1);
  line-height: 32px;
  font-weight: var(--weight-semibold);
  color: var(--ink-900);
  letter-spacing: var(--tracking-tight);
}

// 表单：标签在上，输入框与主按钮同列宽（§4.4）
:deep(.el-form-item) {
  margin-bottom: var(--space-4);
}

:deep(.el-form-item__label) {
  color: var(--ink-700);
  font-size: var(--text-body-sm);
  line-height: 20px;
  padding-bottom: var(--space-1);
}

:deep(.el-form-item__error) {
  color: var(--danger);
  font-size: var(--text-caption);
  padding-top: var(--space-1);
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  box-shadow: none;
  transition:
    border-color var(--duration-fast) var(--ease-out),
    box-shadow var(--duration-fast) var(--ease-out);

  &:hover {
    border-color: var(--line-strong);
  }

  &.is-focus,
  &.is-focused {
    border-color: var(--brand-500);
    box-shadow: 0 0 0 3px color-mix(in srgb, var(--brand-500) 14%, transparent);
  }
}

:deep(.el-input__inner) {
  color: var(--ink-900);
  font-size: var(--text-body);
  caret-color: var(--brand-500);

  &::placeholder {
    color: var(--ink-400);
  }
}

:deep(.el-select__placeholder) {
  color: var(--ink-400);
}

// 验证码行
.captcha-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  width: 100%;

  :deep(.el-input) {
    flex: 1;
    min-width: 0;
  }
}

.captcha-img {
  width: 104px;
  height: 36px;
  flex-shrink: 0;
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  background: var(--canvas-sunken);
  cursor: pointer;
  object-fit: contain;
  transition: border-color var(--duration-fast) var(--ease-out);

  &:hover {
    border-color: var(--brand-500);
  }
}

// 「刷新」中性色，不与主按钮抢权重（§4.1）
.captcha-row :deep(.el-link) {
  flex-shrink: 0;
  font-size: var(--text-body-sm);
  color: var(--ink-500);
  transition: color var(--duration-fast) var(--ease-out);

  &:hover,
  &:focus-visible {
    color: var(--ink-900);
  }
}

// 唯一实心主按钮：占满表单列宽（§4.1）
.register-submit-btn {
  width: 100%;
  height: var(--control-height-md);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-body);
  font-weight: var(--weight-semibold);
  color: var(--surface);
  background: var(--brand-500);
  transition:
    background-color var(--duration-fast) var(--ease-out),
    transform var(--duration-fast) var(--ease-out);

  &:hover:not(.is-disabled) {
    background: var(--brand-600);
    transform: translateY(-1px);
  }

  &:active:not(.is-disabled) {
    transform: translateY(0) scale(0.98);
  }
}
</style>
