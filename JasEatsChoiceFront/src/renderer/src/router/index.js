import { createRouter, createWebHistory } from 'vue-router'
import { isAdminLoggedIn } from '../utils/auth'

// Import auth views
const Login = () => import('../views/user/Login.vue')
const Register = () => import('../views/user/Register.vue')

// Import user views
const UserHome = () => import('../views/user/Home.vue') // 用户首页
const UserHomeContent = () => import('../views/user/HomeContent.vue') // 用户首页内容
const UserRecommend = () => import('../views/user/Recommend.vue') // 我的推荐
const UserMerchantList = () => import('../views/user/MerchantList.vue') // 商家查找
const UserMerchantDetail = () => import('../views/user/MerchantDetail.vue') // 商家详情
const UserTodayRecipe = () => import('../views/user/TodayRecipe.vue') // 今日食谱
const UserCalorie = () => import('../views/user/Calorie.vue') // 卡路里统计
const UserMyRecipe = () => import('../views/user/MyRecipe.vue') // 我的食谱
const UserDietRecord = () => import('../views/user/DietRecord.vue') // 饮食记录
const UserOrders = () => import('../views/user/Orders.vue') // 我的订单
const UserOrderDetail = () => import('../views/user/OrderDetail.vue') // 订单详情
const UserEvaluateOrder = () => import('../views/user/EvaluateOrder.vue') // 评价订单
const UserConsumeHistory = () => import('../views/user/ConsumeHistory.vue') // 消费记录
const UserMessageCenter = () => import('../views/user/MessageCenter.vue') // 消息中心
const UserSystemNotification = () => import('../views/user/SystemNotification.vue') // 系统通知
const UserAI = () => import('../views/user/AI.vue') // AI饮食助手
const UserOrderConfirmation = () => import('../views/user/OrderConfirmation.vue') // 订单确认
const UserSettings = () => import('../views/user/Settings.vue') // 设置
const UserProfile = () => import('../views/user/Profile.vue') // 用户中心
const UserWalletManagement = () => import('../views/user/WalletManagement.vue') // 钱包管理
const UserWalletTransactions = () => import('../views/user/WalletTransactions.vue') // 钱包交易记录
const UserPaymentPasswordSetup = () => import('../views/user/PaymentPasswordSetup.vue') // 支付密码设置
const UserWalletSecurity = () => import('../views/user/WalletSecurity.vue') // 钱包安全设置
const UserAddress = () => import('../views/user/Address.vue') // 地址管理
const UserContact = () => import('../views/user/Contact.vue') // 联系客服
const UserDishDetail = () => import('../views/user/DishDetail.vue') // 菜品详情

// Import merchant views
const MerchantHome = () => import('../views/merchant/Home.vue') // 商家首页
const MerchantHomeContent = () => import('../views/merchant/HomeContent.vue') // 商家首页内容
const MerchantOrders = () => import('../views/merchant/Orders.vue') // 商家订单管理
const MerchantTodayOrders = () => import('../views/merchant/TodayOrders.vue') // 商家今日订单
const MerchantMenu = () => import('../views/merchant/Menu.vue') // 商家菜单管理
const MerchantMessages = () => import('../views/merchant/Messages.vue') // 商家消息管理
const MerchantMenuEdit = () => import('../views/merchant/MenuEdit.vue') // 菜单编辑
const MerchantDishManagement = () => import('../views/merchant/DishManagement.vue') // 菜品管理
const MerchantDishEdit = () => import('../views/merchant/DishEdit.vue') // 菜品编辑
const MerchantChat = () => import('../views/merchant/Chat.vue') // 商家聊天
const MerchantStatistics = () => import('../views/merchant/Statistics.vue') // 经营统计
const MerchantOrderDetail = () => import('../views/merchant/OrderDetail.vue') // 订单详情
const MerchantComments = () => import('../views/merchant/Comments.vue') // 商家评价中心
const MerchantRegister = () => import('../views/merchant/MerchantRegister.vue') // 商家注册
const MerchantWishListAudit = () => import('../views/merchant/WishListAudit.vue') // 想吃列表审核
const MerchantAI = () => import('../views/merchant/AI/index.vue') // AI经营助手

// Import admin layout
const AdminLayout = () => import('../components/AdminLayout.vue') // 管理员布局
const AdminLogin = () => import('../views/admin/Login.vue') // 管理员登录
const AdminDashboard = () => import('../views/admin/Dashboard.vue') // 管理员控制台
const AdminUserManagement = () => import('../views/admin/UserManagement.vue') // 用户管理
const AdminTutorialReview = () => import('../views/admin/TutorialReview.vue') // 教程审核
const AdminTutorialManage = () => import('../views/admin/TutorialManage.vue') // 教程管理
const AdminSettings = () => import('../views/admin/Settings.vue') // 系统设置

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes: [
    // 认证相关路由
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
      meta: { title: '用户注册' }
    },
    {
      path: '/merchant/register',
      name: 'merchant-register',
      component: MerchantRegister,
      meta: { title: '商户注册' }
    },
    // 根路径默认跳转到登录页面
    {
      path: '/',
      redirect: '/login'
    },
    // 用户模块根路径跳转到用户首页
    {
      path: '/user',
      redirect: '/user/home'
    },
    // 商家模块根路径跳转到商家首页
    {
      path: '/merchant',
      redirect: '/merchant/home'
    },
    // 用户模块路由
    {
      path: '/user/home',
      name: 'user-home',
      component: UserHome,
      meta: { title: '用户首页' },
      // 所有Home的子功能都作为嵌套路由
      children: [
        // 默认显示HomeContent作为首页内容
        {
          path: '',
          name: 'user-home-content',
          component: UserHomeContent,
          meta: { title: '用户首页' }
        },
        {
          path: 'recommend',
          name: 'user-recommend',
          component: UserRecommend,
          meta: { title: '我的推荐' }
        },
        {
          path: 'merchants',
          name: 'user-merchants',
          component: UserMerchantList,
          meta: { title: '商家查找' }
        },
        {
          path: 'merchant-detail',
          name: 'user-merchant-detail',
          component: UserMerchantDetail,
          meta: { title: '商家详情', transition: 'slide-left' }
        },
        {
          path: 'order-confirmation',
          name: 'user-order-confirmation',
          component: UserOrderConfirmation,
          meta: { title: '订单确认', transition: 'slide-up' }
        },
        {
          path: 'today-recipe',
          name: 'user-today-recipe',
          component: UserTodayRecipe,
          meta: { title: '今日食谱' }
        },
        {
          path: 'calorie',
          name: 'user-calorie',
          component: UserCalorie,
          meta: { title: '卡路里统计' }
        },
        {
          path: 'my-recipe',
          name: 'user-my-recipe',
          component: UserMyRecipe,
          meta: { title: '我的食谱' }
        },
        {
          path: 'diet-record',
          name: 'user-diet-record',
          component: UserDietRecord,
          meta: { title: '饮食记录' }
        },
        {
          path: 'orders',
          name: 'user-orders',
          component: UserOrders,
          meta: { title: '我的订单' }
        },
        {
          path: 'order-detail/:id',
          name: 'user-order-detail',
          component: UserOrderDetail,
          meta: { title: '订单详情', transition: 'zoom-fade' }
        },
        {
          path: 'evaluate-order/:id',
          name: 'user-evaluate-order',
          component: UserEvaluateOrder,
          meta: { title: '评价订单' }
        },
        {
          path: 'consume-history',
          name: 'user-consume-history',
          component: UserConsumeHistory,
          meta: { title: '消费记录' }
        },
        {
          path: 'message-center',
          name: 'user-message-center',
          component: UserMessageCenter,
          meta: { title: '消息中心' }
        },
        {
          path: 'system-notification',
          name: 'user-system-notification',
          component: UserSystemNotification,
          meta: { title: '系统通知' }
        },
        {
          path: 'chat',
          name: 'user-chat',
          component: () => import('../views/user/Chat.vue'),
          meta: { title: '消息中心' }
        },
        {
          path: 'contacts',
          name: 'user-contacts',
          component: () => import('../views/user/Contacts.vue'),
          meta: { title: '通讯录' }
        },
        {
          path: 'ai',
          name: 'user-ai',
          component: UserAI,
          meta: { title: 'AI饮食助手', transition: 'bounce' }
        },
        {
          path: 'settings',
          name: 'user-settings',
          component: UserSettings,
          meta: { title: '设置' }
        },
        {
          path: 'profile',
          name: 'user-profile',
          component: UserProfile,
          meta: { title: '用户中心', transition: 'fade-slide' }
        },
        {
          path: 'wallet-management',
          name: 'user-wallet-management',
          component: UserWalletManagement,
          meta: { title: '钱包管理' }
        },
        {
          path: 'wallet-transactions',
          name: 'user-wallet-transactions',
          component: UserWalletTransactions,
          meta: { title: '钱包交易记录' }
        },
        {
          path: 'payment-password-setup',
          name: 'user-payment-password-setup',
          component: UserPaymentPasswordSetup,
          meta: { title: '支付密码设置' }
        },
        {
          path: 'wallet-security',
          name: 'user-wallet-security',
          component: UserWalletSecurity,
          meta: { title: '钱包安全设置' }
        },
        {
          path: 'address',
          name: 'user-address',
          component: UserAddress,
          meta: { title: '地址管理' }
        },
        {
          path: 'contact',
          name: 'user-contact',
          component: UserContact,
          meta: { title: '联系客服' }
        },
        {
          path: 'dish-detail/:dishId',
          name: 'dish-detail',
          component: UserDishDetail,
          meta: { title: '菜品详情', transition: 'slide-left' }
        },
        {
          path: 'my-collection',
          name: 'user-my-collection',
          component: () => import('../views/user/MyCollection.vue'),
          meta: { title: '我的收藏' }
        },
        {
          path: 'tutorials',
          name: 'user-tutorials',
          component: () => import('../views/user/Tutorials.vue'),
          meta: { title: '制作教程与指南' }
        },
        {
          path: 'tutorials/:id',
          name: 'user-tutorial-detail',
          component: () => import('../views/user/TutorialDetail.vue'),
          meta: { title: '教程详情', transition: 'slide-left' }
        },
        {
          path: 'tutorials/publish',
          name: 'user-publish-tutorial',
          component: () => import('../views/user/PublishTutorial.vue'),
          meta: { title: '发布教程', transition: 'slide-up' }
        },
        {
          path: 'tutorials/my',
          name: 'user-my-tutorials',
          component: () => import('../views/user/MyTutorials.vue'),
          meta: { title: '我的教程' }
        },
        {
          path: 'hot-topic/:data?',
          name: 'user-hot-topic-detail',
          component: () => import('../views/user/HotTopicDetail.vue'),
          meta: { title: '今日热点' }
        }
      ]
    },
    // 商家模块路由 - 嵌套结构
    {
      path: '/merchant/home',
      name: 'merchant-home',
      component: MerchantHome, // 使用用户端的Home.vue作为基础容器，它包含CommonHome
      meta: { title: '商家首页' },
      children: [
        // 默认显示商家首页内容
        {
          path: '',
          name: 'merchant-home-content',
          component: MerchantHomeContent,
          meta: { title: '商家首页' }
        },
        {
          path: 'orders', // 相对路径，继承自 /merchant/home
          name: 'merchant-orders',
          component: MerchantOrders,
          meta: { title: '商家订单管理' }
        },
        {
          path: 'today-orders', // 相对路径，继承自 /merchant/home
          name: 'merchant-today-orders',
          component: MerchantTodayOrders,
          meta: { title: '商家今日订单' }
        },
        {
          path: 'menu', // 相对路径，继承自 /merchant/home
          name: 'merchant-menu',
          component: MerchantMenu,
          meta: { title: '商家菜单管理' }
        },
        {
          path: 'messages', // 相对路径，继承自 /merchant/home
          name: 'merchant-messages',
          component: MerchantMessages,
          meta: { title: '商家消息管理' }
        },
        {
          path: 'menu-edit', // 相对路径，继承自 /merchant/home
          name: 'merchant-menu-edit',
          component: MerchantMenuEdit,
          meta: { title: '菜单编辑' }
        },
        {
          path: 'dish-management', // 相对路径，继承自 /merchant/home
          name: 'merchant-dish-management',
          component: MerchantDishManagement,
          meta: { title: '菜品管理' }
        },
        {
          path: 'dish-edit', // 相对路径，继承自 /merchant/home
          name: 'merchant-dish-edit',
          component: MerchantDishEdit,
          meta: { title: '菜品编辑' }
        },
        {
          path: 'chat', // 相对路径，继承自 /merchant/home
          name: 'merchant-chat',
          component: MerchantChat,
          meta: { title: '商家聊天' }
        },
        {
          path: 'statistics', // 相对路径，继承自 /merchant/home
          name: 'merchant-statistics',
          component: MerchantStatistics,
          meta: { title: '经营统计' }
        },
        {
          path: 'order-detail/:id', // 订单详情路由
          name: 'merchant-order-detail',
          component: MerchantOrderDetail,
          meta: { title: '订单详情' }
        },
        {
          path: 'comments', // 评价中心路由
          name: 'merchant-comments',
          component: MerchantComments,
          meta: { title: '商家评价中心' }
        },
        {
          path: 'settings', // 商家设置路由
          name: 'merchant-settings',
          component: () => import('../views/user/Settings.vue'), // 复用用户端设置页面
          meta: { title: '商家设置' }
        },
        {
          path: 'tutorials', // 商家教程管理
          name: 'merchant-tutorials',
          component: () => import('../views/merchant/TutorialManage.vue'),
          meta: { title: '教程管理' }
        },
        {
          path: 'wish-list-audit', // 想吃列表审核
          name: 'merchant-wish-list-audit',
          component: MerchantWishListAudit,
          meta: { title: '想吃列表审核' }
        },
        {
          path: 'ai', // AI经营助手
          name: 'merchant-ai',
          component: MerchantAI,
          meta: { title: 'AI经营助手' }
        }
      ]
    },
    // 管理员登录路由
    {
      path: '/admin/login',
      name: 'admin-login',
      component: AdminLogin,
      meta: { title: '管理员登录' }
    },
    // 管理员模块路由
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      meta: { title: '管理员中心', requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: AdminDashboard,
          meta: { title: '管理员控制台' }
        },
        {
          path: 'users',
          name: 'admin-users',
          component: AdminUserManagement,
          meta: { title: '用户管理' }
        },
        {
          path: 'merchants',
          name: 'admin-merchants',
          component: () => import('../views/admin/MerchantManagement.vue'),
          meta: { title: '商家管理' }
        },
        {
          path: 'merchants/audit',
          name: 'admin-merchants-audit',
          component: () => import('../views/admin/MerchantAudit.vue'),
          meta: { title: '商家审核' }
        },
        {
          path: 'orders',
          name: 'admin-orders',
          component: () => import('../views/admin/OrderManagement.vue'),
          meta: { title: '订单管理' }
        },
        {
          path: 'dishes',
          name: 'admin-dishes',
          component: () => import('../views/admin/DishManagement.vue'),
          meta: { title: '菜品管理' }
        },
        {
          path: 'dishes/audit',
          name: 'admin-dishes-audit',
          component: () => import('../views/admin/DishAudit.vue'),
          meta: { title: '菜品审核' }
        },
        {
          path: 'tutorials/manage', // 教程管理
          name: 'admin-tutorial-manage',
          component: AdminTutorialManage,
          meta: { title: '教程管理' }
        },
        {
          path: 'tutorials/review', // 教程审核
          name: 'admin-tutorial-review',
          component: AdminTutorialReview,
          meta: { title: '教程审核' }
        },
        {
          path: 'topics',
          name: 'admin-topics',
          component: () => import('../views/admin/TopicManagement.vue'),
          meta: { title: '热点话题管理' }
        },
        {
          path: 'announcements',
          name: 'admin-announcements',
          component: () => import('../views/admin/AnnouncementManagement.vue'),
          meta: { title: '公告管理' }
        },
        {
          path: 'finance/withdrawals',
          name: 'admin-withdrawals',
          component: () => import('../views/admin/WithdrawalAudit.vue'),
          meta: { title: '提现审核' }
        },
        {
          path: 'finance/recharges',
          name: 'admin-recharges',
          component: () => import('../views/admin/RechargeManagement.vue'),
          meta: { title: '充值记录' }
        },
        {
          path: 'finance/refunds',
          name: 'admin-refunds',
          component: () => import('../views/admin/RefundManagement.vue'),
          meta: { title: '退款管理' }
        },
        {
          path: 'settings', // 管理员设置
          name: 'admin-settings',
          component: AdminSettings,
          meta: { title: '系统设置' }
        },
        {
          path: 'settings/profile', // 管理员个人信息
          name: 'admin-profile',
          component: () => import('../views/admin/Profile.vue'),
          meta: { title: '个人信息' }
        },
        {
          path: 'settings/password', // 修改密码
          name: 'admin-password',
          component: () => import('../views/admin/Password.vue'),
          meta: { title: '修改密码' }
        },
        {
          path: 'settings/roles',
          name: 'admin-roles',
          component: () => import('../views/admin/RoleManagement.vue'),
          meta: { title: '角色管理' }
        },
        {
          path: 'settings/permissions',
          name: 'admin-permissions',
          component: () => import('../views/admin/PermissionManagement.vue'),
          meta: { title: '权限管理' }
        },
        {
          path: 'settings/logs',
          name: 'admin-logs',
          component: () => import('../views/admin/SystemLogs.vue'),
          meta: { title: '系统日志' }
        },
        {
          path: 'statistics',
          name: 'admin-statistics',
          component: () => import('../views/admin/DataStatistics.vue'),
          meta: { title: '数据统计' }
        }
      ]
    }
  ]
})

// 路由导航守卫 - 用于设置页面标题、登录状态检查和窗口尺寸管理
router.beforeEach(async (to, from, next) => {
  // 设置当前页面标题
  if (to?.meta?.title) {
    document.title = to.meta.title
  }

  // 窗口尺寸自动切换：仅在登录/注册等页面之间切换时调整，主页面内导航不触发
  const authRoutes = ['/login', '/register', '/admin/login', '/merchant/register']
  const api = window.api
  if (api?.window) {
    if (to.path === '/register' || to.path === '/merchant/register') {
      await api.window.resizeToRegister()
    } else if (to.path === '/admin/login') {
      await api.window.resizeToAdminLogin()
    } else if (to.path === '/login') {
      await api.window.resizeToLogin()
    } else if (authRoutes.some(r => from.path === r || from.path.startsWith(r)) && (to.path.startsWith('/user') || to.path.startsWith('/merchant') || to.path.startsWith('/admin'))) {
      // 仅从登录/注册页进入主页面时才调整窗口尺寸
      await api.window.resizeToMain()
    }
  }

  // 管理员路由权限验证
  if (to.path.startsWith('/admin')) {
    // 管理员登录页面不需要验证
    if (to.path === '/admin/login') {
      next()
      return
    }

    // 其他管理员页面需要验证登录状态
    if (!isAdminLoggedIn()) {
      // 未登录，跳转到登录页
      next('/admin/login')
      return
    }
  }

  next()
})

export default router
