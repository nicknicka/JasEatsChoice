<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, onMounted, onUnmounted, computed, watch, provide, nextTick } from 'vue'
import {
  Search,
  Close,
  Menu,
  Shop,
  Calendar,
  DataAnalysis,
  Document,
  List,
  Message,
  ChatDotRound,
  Setting,
  HomeFilled,
  User,
  VideoCamera
} from '@element-plus/icons-vue'
import { decodeJwt } from '../utils/api.js'
import { useAuthStore } from '../store/authStore'
import { useUserStore } from '../store/userStore'
import { useLoginTransition } from '../composables/useLoginTransition'
import api from '../utils/api.js'
// 导入CommonAvatar组件
import CommonAvatar from './CommonAvatar.vue'

const router = useRouter()
const route = useRoute()

// 登录过渡动画控制
const { hideTransition } = useLoginTransition()

// 内容区域引用
const contentAreaRef = ref(null)

// 路由监听：路由切换时自动滚动到页面顶部
watch(
  () => route.path,
  () => {
    // 路由变化时，使用 nextTick 确保 DOM 更新后再滚动
    nextTick(() => {
      setTimeout(() => {
        if (contentAreaRef.value) {
          // Element Plus 组件需要访问 $el 属性获取实际 DOM 元素
          const domElement = contentAreaRef.value.$el || contentAreaRef.value
          if (domElement && typeof domElement.scrollTo === 'function') {
            domElement.scrollTo({ top: 0, behavior: 'smooth' })
          } else if (domElement) {
            // 如果 scrollTo 不可用，直接设置 scrollTop
            domElement.scrollTop = 0
          }
        }
      }, 50)
    })
  },
  { immediate: false } // 不在首次加载时触发
)

// 导航到指定路径
const navigateTo = (path, fromSidebar = false) => {
  // 如果是从侧边栏跳转，添加查询参数
  if (fromSidebar) {
    // 检查路径是否已经有查询参数
    const separator = path.includes('?') ? '&' : '?'
    router.push(`${path}${separator}fromSidebar=true`)
  } else {
    router.push(path)
  }
}

// 获取 Pinia 存储
const authStore = useAuthStore()
const userStore = useUserStore()

// 用户信息 - 从 Pinia 中获取
// 注释：使用计算属性直接从 userStore 获取 userInfo

// 用户角色
const userRole = ref('user') // 'user' 或 'merchant'
// 是否已注册商家 - 直接通过userInfo.merchantId判断，不再需要单独的状态变量

// 提供更新用户信息的方法给子组件
const updateSidebarAvatar = (avatarUrl) => {
  if (userStore.userInfo && avatarUrl) {
    userStore.setUserInfo({ ...userStore.userInfo, avatar: avatarUrl })
  }
}
provide('updateSidebarAvatar', updateSidebarAvatar)

// 预定义菜单数据
const menuData = {
  // 用户端菜单 - 分组折叠版本
  user: [
    { index: '1', name: '首页', icon: HomeFilled, path: '/user/home' }, // 首页入口
    // AI饮食助手 - 单独一级菜单
    { index: '2', name: 'AI饮食助手', icon: ChatDotRound, path: '/user/home/ai' },
    // 推荐与发现分组
    {
      index: 'group-1',
      name: '推荐与发现',
      icon: Menu,
      children: [
        { index: '3', name: '我的推荐', icon: Menu, path: '/user/home/recommend' },
        { index: '4', name: '商家查找', icon: Shop, path: '/user/home/merchants' },
        { index: '5', name: '教程广场', icon: VideoCamera, path: '/user/home/tutorials' }
      ]
    },
    // 饮食管理分组
    {
      index: 'group-2',
      name: '饮食管理',
      icon: Calendar,
      children: [
        { index: '6', name: '今日食谱', icon: Calendar, path: '/user/home/today-recipe' },
        { index: '71', name: '饮食记录', icon: Calendar, path: '/user/home/diet-record' },
        { index: '7', name: '卡路里统计', icon: DataAnalysis, path: '/user/home/calorie' },
        { index: '8', name: '我的食谱', icon: Document, path: '/user/home/my-recipe' }
      ]
    },
    // 个人中心分组
    {
      index: 'group-3',
      name: '个人中心',
      icon: User,
      children: [
        { index: '9', name: '用户中心', icon: User, path: '/user/home/profile' },
        { index: '10', name: '订单中心', icon: List, path: '/user/home/orders' }
      ]
    },
    // 消息沟通分组
    {
      index: 'group-4',
      name: '消息沟通',
      icon: Message,
      children: [
        { index: '11', name: '消息中心', icon: Message, path: '/user/home/message-center' },
        { index: '12', name: '聊天消息', icon: ChatDotRound, path: '/user/home/chat' },
        { index: '13', name: '通讯录', icon: User, path: '/user/home/contacts' }
      ]
    },
    // 设置菜单
    {
      index: '14',
      name: '设置',
      icon: Setting,
      path: '/user/home/settings',
      isSetting: true
    }
  ],
  // 商家端菜单 - 按功能模块重新排序：首页 → AI助手 → 核心业务 → 店铺管理 → 客户沟通 → 经营分析
  merchant: [
    // 首页模块
    { index: '1', name: '我的店铺', icon: HomeFilled, path: '/merchant/home' },

    // AI经营助手 - 单独一级菜单
    { index: '2', name: 'AI经营助手', icon: ChatDotRound, path: '/merchant/home/ai' },

    // 订单管理模块
    {
      index: '3',
      name: '订单管理',
      icon: List,
      children: [
        { index: '3-1', name: '今日订单', icon: Calendar, path: '/merchant/home/today-orders' },
        { index: '3-2', name: '全部订单', icon: Document, path: '/merchant/home/orders' }
      ]
    },

    // 店铺管理模块
    {
      index: '4',
      name: '店铺管理',
      icon: Shop,
      children: [
        { index: '4-1', name: '菜单管理', icon: Shop, path: '/merchant/home/menu' },
        { index: '4-2', name: '菜品管理', icon: Document, path: '/merchant/home/dish-management' }
      ]
    },

    // 客户沟通模块
    {
      index: '5',
      name: '客户沟通',
      icon: Message,
      children: [
        { index: '5-1', name: '商家聊天', icon: ChatDotRound, path: '/merchant/home/chat' },
        { index: '5-2', name: '消息管理', icon: Message, path: '/merchant/home/messages' }
      ]
    },

    // 经营分析模块
    {
      index: '6',
      name: '经营分析',
      icon: DataAnalysis,
      children: [
        { index: '6-1', name: '评价中心', icon: DataAnalysis, path: '/merchant/home/comments' },
        { index: '6-2', name: '经营统计', icon: DataAnalysis, path: '/merchant/home/statistics' }
      ]
    },

    // 设置菜单
    {
      index: '7',
      name: '设置',
      icon: Setting,
      path: '/merchant/home/settings',
      isSetting: true
    }
  ]
}

// 根据当前角色过滤菜单
// 当前激活的菜单项索引
const activeMenuIndex = ref('')

// 根据当前角色过滤菜单
const currentMenu = computed(() => {
  return menuData[userRole.value] ? menuData[userRole.value] : menuData.user || []
})

// 智能匹配父级菜单 - 根据路径模式匹配到相关的父级菜单
const smartMatchParentMenu = (path, role) => {
  // 用户端路径映射
  const userPathMappings = [
    { pattern: /\/user\/home\/merchant-detail/, menuIndex: '3' }, // 商家详情 → 商家查找
    { pattern: /\/user\/home\/order-confirmation/, menuIndex: '10' }, // 订单确认 → 订单中心
    { pattern: /\/user\/home\/order-detail/, menuIndex: '10' }, // 订单详情 → 订单中心
    { pattern: /\/user\/home\/system-notification/, menuIndex: '11' }, // 系统通知 → 消息中心
    { pattern: /\/user\/home\/address/, menuIndex: '9' }, // 地址管理 → 用户中心
    { pattern: /\/user\/home\/contact/, menuIndex: '9' }, // 联系客服 → 用户中心
    { pattern: /\/user\/home\/my-collection/, menuIndex: '2' }, // 我的收藏 → 我的推荐
    { pattern: /\/user\/home\/tutorials/, menuIndex: '5' } // 教程广场 → 教程广场
  ]

  // 商家端路径映射
  const merchantPathMappings = [
    { pattern: /\/merchant\/home\/order-detail/, menuIndex: '2-2' }, // 订单详情 → 全部订单
    { pattern: /\/merchant\/home\/menu-edit/, menuIndex: '3-1' }, // 菜单编辑 → 菜单管理
    { pattern: /\/merchant\/home\/dish-edit/, menuIndex: '3-2' } // 菜品编辑 → 菜品管理
  ]

  // 根据角色选择对应的映射表
  const mappings = role === 'merchant' ? merchantPathMappings : userPathMappings

  // 遍历映射表，查找匹配的路径模式
  for (const mapping of mappings) {
    if (mapping.pattern.test(path)) {
      return mapping.menuIndex
    }
  }

  // 没有匹配到
  return null
}

// 根据当前路由计算并设置激活的菜单项索引 - 支持分组菜单
const updateActiveMenuIndex = () => {
  const currentPath = router.currentRoute.value.path
  // console.log(
  //   '=== updateActiveMenuIndex ===',
  //   '当前路由:',
  //   currentPath,
  //   '当前菜单:',
  //   currentMenu.value.map((item) => item.name),
  //   '当前activeMenu:',
  //   activeMenuIndex.value
  // )

  // 清除所有菜单的激活状态
  nextTick(() => {
    const menuTitles = document.querySelectorAll('.menu-list .el-sub-menu__title')
    menuTitles.forEach((title) => title.classList.remove('is-active'))
  })

  // 查找当前路由对应的菜单项 - 包括分组内的子菜单
  for (const menuItem of currentMenu.value) {
    // 如果是分组菜单，检查其子菜单
    if (menuItem.children) {
      // console.log(
      //   '检查分组:',
      //   menuItem.name,
      //   '的子菜单:',
      //   menuItem.children.map((child) => child.name)
      // )
      for (const childItem of menuItem.children) {
        // console.log(
        //   '检查子菜单:',
        //   childItem.name,
        //   'path:',
        //   childItem.path,
        //   '是否匹配当前path:',
        //   currentPath
        // )
        if (currentPath === childItem.path) {
          // console.log('匹配到子菜单:', childItem.name)
          activeMenuIndex.value = childItem.index

          // 查找并激活当前子菜单所在的父菜单组
          nextTick(() => {
            const activeMenuItem = document.querySelector('.menu-list .el-menu-item.is-active')
            if (activeMenuItem) {
              // 查找当前激活菜单项所在的父级一级菜单
              const parentSubMenu = activeMenuItem.closest('.el-sub-menu')
              if (parentSubMenu) {
                // 检查该一级菜单下是否包含当前激活的二级菜单
                const hasActiveChild = parentSubMenu.contains(activeMenuItem)
                if (hasActiveChild) {
                  const parentMenuTitle = parentSubMenu.querySelector('.el-sub-menu__title')
                  if (parentMenuTitle) {
                    parentMenuTitle.classList.add('is-active')
                  }
                }
              }
            }
          })

          return
        }
      }
    }
    // 如果是普通菜单，直接检查
    else {
      // console.log(
      //   '检查普通菜单:',
      //   menuItem.name,
      //   'path:',
      //   menuItem.path,
      //   '是否匹配当前path:',
      //   currentPath
      // )
      if (currentPath === menuItem.path) {
        // console.log('匹配到普通菜单:', menuItem.name)
        activeMenuIndex.value = menuItem.index
        return
      }
    }
  }

  // 如果没有精确匹配到菜单项，尝试智能匹配父级菜单
  const parentMenuIndex = smartMatchParentMenu(currentPath, userRole.value)
  if (parentMenuIndex) {
    activeMenuIndex.value = parentMenuIndex
  } else {
    // 实在没有匹配，才激活第一个菜单项
    activeMenuIndex.value = currentMenu.value[0]?.index || '1'
  }

  // 重置侧边栏宽度为默认值，防止自动展开菜单时宽度变宽
  sidebarWidth.value = '200px' // 这里的默认宽度要和初始化时一致

  // 延迟更新菜单激活状态，确保DOM已渲染完成
  nextTick(() => {
    // 查找当前激活的菜单项
    const activeMenuItem = document.querySelector('.menu-list .el-menu-item.is-active')
    if (activeMenuItem) {
      // 查找当前激活菜单项所在的父级一级菜单
      const parentSubMenu = activeMenuItem.closest('.el-sub-menu')
      if (parentSubMenu) {
        // 检查该一级菜单下是否包含当前激活的二级菜单
        const hasActiveChild = parentSubMenu.contains(activeMenuItem)
        if (hasActiveChild) {
          const parentMenuTitle = parentSubMenu.querySelector('.el-sub-menu__title')
          if (parentMenuTitle) {
            // 给父级菜单组标题添加激活类
            parentMenuTitle.classList.add('is-active')
          }
        }
      }
    }
  })
}

// 菜单点击事件处理 - 支持分组菜单
const handleMenuSelect = (index) => {
  // 查找菜单项，包括分组内的子菜单
  let targetMenuItem = null

  // 遍历当前菜单
  for (const menuItem of currentMenu.value) {
    // 如果是分组菜单，查找其子菜单
    if (menuItem.children) {
      targetMenuItem = menuItem.children.find((childItem) => childItem.index === index)
      if (targetMenuItem) break
    }
    // 如果是普通菜单，直接比较
    else if (menuItem.index === index) {
      targetMenuItem = menuItem
      break
    }
  }

  // 如果找到目标菜单，进行跳转（从侧边栏跳转，添加 fromSidebar 参数）
  if (targetMenuItem) {
    navigateTo(targetMenuItem.path, true)
  }
}

// 头像放大弹窗
const showLargeAvatar = ref(false)

const sidebarWidth = ref('200px')

// 跟踪所有展开的子菜单索引
const openedMenus = ref(new Set())

// 监听菜单展开事件 - 展开时增宽，给二级菜单足够空间
const handleMenuOpen = (index) => {
  openedMenus.value.add(index)
  sidebarWidth.value = '240px' // 有菜单展开时保持较宽
}

// 监听菜单关闭事件 - 只有当所有菜单都关闭时才恢复较窄宽度
const handleMenuClose = (index) => {
  openedMenus.value.delete(index)

  // 只有当所有子菜单都关闭时，才恢复较窄宽度
  if (openedMenus.value.size === 0) {
    sidebarWidth.value = '200px'
  }

  // 确保菜单关闭后，包含激活子菜单的一级菜单组仍然保持激活状态
  const activeMenuItem = document.querySelector('.menu-list .el-menu-item.is-active')

  if (activeMenuItem) {
    // 查找当前激活菜单项所在的父级一级菜单
    const parentSubMenu = activeMenuItem.closest('.el-sub-menu')

    if (parentSubMenu) {
      // 检查该一级菜单下是否包含当前激活的二级菜单
      const hasActiveChild = parentSubMenu.contains(activeMenuItem)

      if (hasActiveChild) {
        const parentMenuTitle = parentSubMenu.querySelector('.el-sub-menu__title')

        if (parentMenuTitle) {
          parentMenuTitle.classList.add('is-active')
        }
      }
    }
  }
}

// 角色切换功能
const toggleRole = () => {
  try {
    // 切换角色
    userRole.value = userRole.value === 'user' ? 'merchant' : 'user'

    // 重置展开的菜单状态
    openedMenus.value.clear()
    sidebarWidth.value = '200px'

    // 跳转对应页面
    if (userRole.value === 'user') {
      navigateTo('/user/home')
    } else {
      navigateTo('/merchant/home')
    }

  } catch (error) {
    console.error('角色切换失败:', error)
  }
}

// 页面加载时从当前路由恢复角色，默认进入用户角色
onMounted(() => {
  try {
    // 初始化时重置展开菜单状态
    openedMenus.value.clear()
    sidebarWidth.value = '200px'

    if (!userStore.userInfo || userStore.userInfo.avatar === '') {
      userStore.fetchUserInfo()
    }

    // 1. First check current route to determine role
    let detectedRole = 'user'

    if (router.currentRoute.value?.path?.startsWith('/merchant/')) {
      detectedRole = 'merchant'
    }

    userRole.value = detectedRole

    if (userRole.value === 'user' && authStore.token) {
      const decodedToken = decodeJwt(authStore.token)
      if (decodedToken && decodedToken.username && userStore.userInfo) {
        userStore.userInfo.name = decodedToken.username
      }
    }

    // 确保当前菜单已更新后再计算激活菜单，使用nextTick确保DOM更新完成
    nextTick(() => {
      // 等待路由完全准备就绪
      router.isReady().then(() => {
        updateActiveMenuIndex()
        // 页面渲染完成后，隐藏登录过渡动画覆盖层
        nextTick(() => {
          setTimeout(() => {
            hideTransition()
          }, 300)
        })
      })
    })
  } catch (error) {
    console.error('恢复角色失败:', error)
  }
})

// 监听路由变化，更新菜单项高亮
watch(
  () => router.currentRoute.value.path,
  () => {
    // 清除所有菜单的激活状态
    nextTick(() => {
      const menuTitles = document.querySelectorAll('.menu-list .el-sub-menu__title')
      menuTitles.forEach((title) => title.classList.remove('is-active'))

      // 更新激活的菜单项
      updateActiveMenuIndex()
    })
  }
)

// 监听当前菜单变化，更新菜单项高亮
watch(
  currentMenu,
  () => {
    // console.log('=== 监听currentMenu变化，调用updateActiveMenuIndex ===')

    // 清除所有菜单的激活状态
    nextTick(() => {
      const menuTitles = document.querySelectorAll('.menu-list .el-sub-menu__title')
      menuTitles.forEach((title) => title.classList.remove('is-active'))

      // 更新激活的菜单项
      updateActiveMenuIndex()
    })
  },
  { deep: true }
)

// 监听商家注册状态变化

// Watch for route changes to update role automatically
watch(
  () => router.currentRoute.value?.path,
  (newPath) => {
    let newRole = 'user' // Default to user

    if (newPath?.startsWith('/merchant/')) {
      newRole = 'merchant'
    }

    // Only update if role changed
    if (userRole.value !== newRole) {
      userRole.value = newRole

      // 重置展开的菜单状态
      openedMenus.value.clear()
      sidebarWidth.value = '200px'

      // Update user info based on role (using Pinia store)
      if (userRole.value === 'merchant') {
        // 商户端信息从userStore.merchantInfo获取，保留原有 userInfo 字段
        if (userStore.userInfo) {
          userStore.userInfo = {
            ...userStore.userInfo,
            avatar: userStore.merchantInfo?.avatar || userStore.userInfo.avatar || ''
          }
        } else {
          userStore.userInfo = {
            name: '商户端',
            nickname: userStore.merchantInfo?.name || '商户端',
            avatar: userStore.merchantInfo?.avatar || ''
          }
        }
      } else if (userRole.value === 'user') {
        // 用户端信息已在 userStore 中，无需额外处理
      }

      // Role is now managed through Pinia - no need to save to localStorage
      updateActiveMenuIndex()
    }
  }
)

const searchQuery = ref('')

// ========== 未读消息管理 ==========
// 未读消息数量 - 分开存储
const unreadNotificationCount = ref(0) // 系统通知未读数
const unreadFriendRequestCount = ref(0) // 好友请求未读数

/**
 * 获取未读消息数量（分开计算系统通知和好友请求）
 */
const fetchUnreadMessageCount = async () => {
  try {
    // 只在用户端获取未读消息
    if (userRole.value !== 'user') {
      unreadNotificationCount.value = 0
      unreadFriendRequestCount.value = 0
      return
    }

    // 获取当前用户ID
    const userId = authStore.userId || 1

    // 获取未读系统消息数量
    const notificationResponse = await api.get('/notifications/unread-count', {
      params: { userId }
    })

    if (notificationResponse.code === '200') {
      unreadNotificationCount.value = notificationResponse.data || 0
    } else {
      unreadNotificationCount.value = 0
    }

    // 获取好友请求数量
    try {
      const friendsResponse = await api.get(`/v1/contacts/friends/requests`, {
        params: { userId }
      })

      if (friendsResponse.code === '200' && friendsResponse.data) {
        // 获取当前好友列表，过滤掉已经是好友的请求
        let friendIdSet = new Set()
        try {
          const friendsListResponse = await api.get(`/v1/contacts/friends?userId=${userId}`)
          if (friendsListResponse.code === '200') {
            friendIdSet = new Set(friendsListResponse.data.map(contact => String(contact.targetId)))
          }
        } catch (error) {
          console.error('获取好友列表失败:', error)
        }

        // 过滤掉已经是好友的请求
        unreadFriendRequestCount.value = friendsResponse.data.filter(request => {
          const requestUserId = String(request.userId || request.requesterInfo?.id)
          return !friendIdSet.has(requestUserId)
        }).length
      } else {
        unreadFriendRequestCount.value = 0
      }
    } catch (error) {
      console.error('获取好友请求数量失败:', error)
      unreadFriendRequestCount.value = 0
    }
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
    unreadNotificationCount.value = 0
    unreadFriendRequestCount.value = 0
  }
}

/**
 * 暴露刷新方法供子组件调用
 */
const refreshUnreadCount = () => {
  fetchUnreadMessageCount()
}

// 将刷新方法提供给子组件
provide('refreshUnreadCount', refreshUnreadCount)

// 未读消息定时器引用
let unreadCountTimer = null

// 在组件挂载时获取未读消息数量
onMounted(() => {
  fetchUnreadMessageCount()

  // 每30秒刷新一次未读消息数量
  unreadCountTimer = setInterval(() => {
    fetchUnreadMessageCount()
  }, 30000)
})

// 组件卸载时清理定时器
onUnmounted(() => {
  if (unreadCountTimer) {
    clearInterval(unreadCountTimer)
    unreadCountTimer = null
  }
})

// 监听路由变化，当从消息中心返回时刷新未读数量
watch(
  () => router.currentRoute.value?.path,
  (newPath, oldPath) => {
    // 当离开消息中心相关页面时，刷新未读数量
    if (oldPath?.includes('/message-center') && !newPath?.includes('/message-center')) {
      fetchUnreadMessageCount()
    }
    // 当离开通讯录页面时，刷新未读数量（可能处理了好友请求）
    if (oldPath?.includes('/contacts') && !newPath?.includes('/contacts')) {
      fetchUnreadMessageCount()
    }
  }
)

const handleSearch = (value) => {
  try {
    if (!value || value.trim() === '') {
      return
    }

    // 根据当前角色跳转到对应的搜索页面
    if (userRole.value === 'user') {
      // 用户角色，跳转到商家列表页面并携带搜索参数
      navigateTo({
        path: '/user/home/merchants',
        query: { search: value.trim() }
      })
    } else {
      // 商家角色，跳转到订单页面并携带搜索参数
      navigateTo({
        path: '/merchant/home/orders',
        query: { search: value.trim() }
      })
    }
  } catch (error) {
    console.error('搜索失败:', error)
  }
}
</script>

<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <el-header class="top-nav-bar">
      <div
        class="logo"
        @click="() => navigateTo(userRole === 'merchant' ? '/merchant/home' : '/user/home')"
      >
        佳食宜选
      </div>
      <div class="search-box">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="searchQuery"
          type="text"
          class="search-input-native"
          placeholder="搜索菜品、商家、教程..."
          @keyup.enter="handleSearch(searchQuery)"
        />
        <button v-if="searchQuery" class="search-clear" @click="searchQuery = ''">
          <el-icon><Close /></el-icon>
        </button>
      </div>
      <div class="user-info">
        <!-- 商家端已注册：显示角色切换按钮 -->
        <el-button
          v-if="!!authStore.hasMerchantId"
          type="text"
          class="identity-switch"
          @click="toggleRole"
        >
          <el-icon :class="['user-icon', userRole === 'user' ? 'icon-enlarged' : '']">
            <User />
          </el-icon>
          <el-icon :class="['merchant-icon', userRole === 'merchant' ? 'icon-enlarged' : '']">
            <Shop />
          </el-icon>
        </el-button>
        <!-- 商家端未注册：显示注册跳转图标 -->
        <el-button v-else type="text" @click="navigateTo('/merchant/register')">
          <el-icon><Shop /></el-icon>
          <span>商家注册</span>
        </el-button>
      </div>
    </el-header>

    <div class="main-content">
      <!-- 左侧菜单栏 -->
      <el-aside :width="sidebarWidth" class="sidebar-menu">
        <div class="avatar-section">
          <CommonAvatar
            :size="80"
            class="user-avatar"
            :avatar-url="userRole === 'merchant' ? userStore.merchantInfo?.avatar : userStore.userInfo?.avatar"
            :fallback-text="userStore.userInfo?.nickname"
            :show-upload="false"
            :click-to-enlarge="true"
          >
          </CommonAvatar>
          <div class="username">
            {{
              userRole === 'merchant'
                ? userStore.merchantInfo?.nickname
                : userStore.userInfo?.nickname
            }}
          </div>
        </div>

        <!-- 菜单区域 -->
        <div class="menu-content">
          <el-menu
            v-model:default-active="activeMenuIndex"
            class="custom-menu menu-list"
            :key="userRole"
            @select="handleMenuSelect"
            @open="handleMenuOpen"
            @close="handleMenuClose"
          >
            <!-- 遍历菜单，区分分组菜单和普通菜单项 -->
            <template v-for="menuItem in currentMenu" :key="menuItem.index">
              <!-- 排除设置菜单，单独处理 -->
              <template v-if="!menuItem.isSetting">
                <!-- 分组菜单 -->
                <el-sub-menu v-if="menuItem.children" :index="menuItem.index">
                  <template #title>
                    <el-icon>
                      <component :is="menuItem.icon" />
                    </el-icon>
                    <span>{{ menuItem.name }}</span>
                  </template>
                  <!-- 分组下的子菜单 -->
                  <el-menu-item
                    v-for="childItem in menuItem.children"
                    :key="childItem.index"
                    :index="childItem.index"
                  >
                    <el-icon>
                      <component :is="childItem.icon" />
                    </el-icon>
                    <template #title>
                      <div class="menu-item-with-badge">
                        <span class="menu-text">{{ childItem.name }}</span>
                        <!-- 消息中心：只显示系统通知未读数 -->
                        <span
                          v-if="childItem.index === '11' && unreadNotificationCount > 0"
                          class="unread-badge"
                        >
                          {{ unreadNotificationCount > 99 ? '99+' : unreadNotificationCount }}
                        </span>
                        <!-- 通讯录：显示好友请求未读数 -->
                        <span
                          v-if="childItem.index === '13' && unreadFriendRequestCount > 0"
                          class="unread-badge"
                        >
                          {{ unreadFriendRequestCount > 99 ? '99+' : unreadFriendRequestCount }}
                        </span>
                      </div>
                    </template>
                  </el-menu-item>
                </el-sub-menu>

                <!-- 普通菜单项 -->
                <el-menu-item v-else :index="menuItem.index">
                  <el-icon>
                    <component :is="menuItem.icon" />
                  </el-icon>
                  <template #title>{{ menuItem.name }}</template>
                </el-menu-item>
              </template>
            </template>
          </el-menu>
        </div>

        <!-- 设置菜单 - 固定在底部 -->
        <div class="setting-menu-container">
          <el-menu
            v-model:default-active="activeMenuIndex"
            class="custom-menu setting-menu-list"
            @select="handleMenuSelect"
          >
            <template v-for="menuItem in currentMenu" :key="menuItem.index">
              <!-- 只渲染设置菜单 -->
              <el-menu-item
                v-if="menuItem.isSetting"
                :index="menuItem.index"
                class="setting-menu-item"
              >
                <el-icon>
                  <component :is="menuItem.icon" />
                </el-icon>
                <template #title>{{ menuItem.name }}</template>
              </el-menu-item>
            </template>
          </el-menu>
        </div>
      </el-aside>

      <!-- 右侧内容区域，使用router-view实现子组件内容访问 -->
      <el-main ref="contentAreaRef" class="content-area">
        <router-view v-slot="{ Component, route }">
          <transition :name="route.meta.transition || 'fade-slide'" :duration="{ enter: 250, leave: 200 }" mode="default">
            <keep-alive>
              <component :is="Component" :key="route.path" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </div>

    <!-- 头像放大对话框 -->
    <el-dialog v-model="showLargeAvatar" title="个人头像" width="300px" top="20%">
      <div style="text-align: center; padding: 20px 0">
        <CommonAvatar
          :size="200"
          class="user-avatar"
          :avatar-url="userRole === 'merchant' ? userStore.merchantInfo?.avatar : userStore.userInfo?.avatar"
          :fallback-text="userRole === 'merchant' ? '商户' : '用户'"
          :show-upload="false"
        >
        </CommonAvatar>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="showLargeAvatar = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="less">
@import '../assets/css/nordic-theme.less';

// ===== Savour 设计系统变量 =====
@savour-bg: #F6F3ED;
@savour-surface: #FFFFFF;
@savour-text: #2D2A26;
@savour-text-sec: #8A857E;
@savour-text-muted: #B5AFA6;
@savour-accent: #C67B5C;
@savour-accent-hover: #B56A4A;
@savour-accent-light: #F4E6DE;
@savour-green: #7BAE7F;
@savour-gold: #D4A855;
@savour-border: #E8E2D8;
@savour-radius: 16px;
@savour-radius-lg: 24px;
@savour-pill: 100px;

.font-display() {
  font-family: 'Georgia', 'Noto Serif SC', 'Songti SC', 'STSong', serif;
}

// ===== 动画 =====
@keyframes savour-fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes nav-slide-down {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes sidebar-fade-in {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes menu-item-enter {
  from {
    opacity: 0;
    transform: translateX(-4px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes avatar-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(198, 123, 92, 0.2); }
  50% { box-shadow: 0 0 0 8px rgba(198, 123, 92, 0); }
}

.app-container {
  height: 100vh;
  width: 100%;
  background: @savour-bg;
  position: relative;

  // 噪点纹理覆盖层
  &::before {
    content: '';
    position: fixed;
    inset: 0;
    pointer-events: none;
    z-index: 9999;
    opacity: 0.015;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
    background-repeat: repeat;
    background-size: 256px;
  }
}

// ===== 顶部导航栏 =====
.top-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 @nordic-space-lg;
  height: 64px;
  background: linear-gradient(180deg, @savour-surface 0%, rgba(255, 255, 255, 0.95) 100%);
  border-bottom: 1px solid @savour-border;
  position: relative;
  z-index: 100;
  animation: nav-slide-down 0.4s ease both;
  -webkit-app-region: drag;
  user-select: none;

  // 顶部细微阴影
  &::after {
    content: '';
    position: absolute;
    bottom: -8px;
    left: 0;
    right: 0;
    height: 8px;
    background: linear-gradient(180deg, rgba(45, 42, 38, 0.03) 0%, transparent 100%);
    pointer-events: none;
  }

  .logo {
    .font-display();
    font-size: 22px;
    font-weight: 700;
    color: @savour-accent;
    cursor: pointer;
    -webkit-app-region: no-drag;
    letter-spacing: -0.3px;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.25s ease;
    padding: 8px 16px;
    margin-left: -16px;
    border-radius: @savour-radius;

    &::before {
      content: '🍽';
      font-size: 24px;
      filter: drop-shadow(0 2px 4px rgba(198, 123, 92, 0.3));
    }

    &:hover {
      background: @savour-accent-light;
      transform: translateY(-1px);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .search-box {
    display: flex;
    align-items: center;
    width: 380px;
    height: 42px;
    margin: 0 @nordic-space-lg;
    padding: 0 16px;
    background: @savour-bg;
    border: 1.5px solid @savour-border;
    border-radius: @savour-pill;
    transition: all 0.25s ease;
    -webkit-app-region: no-drag;

    &:hover {
      border-color: @savour-accent;
      box-shadow: 0 2px 8px rgba(198, 123, 92, 0.08);
    }

    &:focus-within {
      border-color: @savour-accent;
      background: @savour-surface;
      box-shadow: 0 4px 16px rgba(198, 123, 92, 0.12);
    }

    .search-icon {
      font-size: 18px;
      color: @savour-text-muted;
      margin-right: 10px;
      flex-shrink: 0;
      transition: color 0.25s ease;
    }

    &:focus-within .search-icon {
      color: @savour-accent;
    }

    .search-input-native {
      flex: 1;
      height: 100%;
      border: none;
      background: transparent;
      font-size: @nordic-text-base;
      color: @savour-text;
      outline: none;

      &::placeholder {
        color: @savour-text-muted;
      }
    }

    .search-clear {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      border: none;
      background: @savour-border;
      border-radius: 50%;
      cursor: pointer;
      opacity: 0;
      transform: scale(0.8);
      transition: all 0.2s ease;
      flex-shrink: 0;

      .el-icon {
        font-size: 12px;
        color: @savour-text-sec;
      }

      &:hover {
        background: @savour-accent-light;

        .el-icon {
          color: @savour-accent;
        }
      }
    }

    &:focus-within .search-clear,
    &:hover .search-clear {
      opacity: 1;
      transform: scale(1);
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: @nordic-space-md;
    margin-right: 8px;
    -webkit-app-region: no-drag;

    .identity-switch {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 16px;
      border-radius: @savour-radius;
      background: @savour-bg;
      border: 1.5px solid @savour-border;
      transition: all 0.25s ease;
      font-size: @nordic-text-base;

      &:hover {
        border-color: @savour-accent;
        background: @savour-accent-light;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(198, 123, 92, 0.15);
      }

      .user-icon,
      .merchant-icon {
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        font-size: 18px;
        color: @savour-text-sec;
      }

      .icon-enlarged {
        transform: scale(1.25);
        color: @savour-accent;
        filter: drop-shadow(0 2px 4px rgba(198, 123, 92, 0.3));
      }
    }

    .el-button:not(.identity-switch) {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      border-radius: @savour-radius;
      background: @savour-accent;
      border: none;
      color: #fff;
      font-weight: 600;
      font-size: @nordic-text-sm;
      transition: all 0.25s ease;

      &:hover {
        background: @savour-accent-hover;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(198, 123, 92, 0.3);
      }

      .el-icon {
        font-size: 16px;
      }
    }
  }
}

.main-content {
  display: flex;
  height: calc(100vh - 64px);
}

// ===== 侧边栏 =====
.sidebar-menu {
  background: linear-gradient(180deg, @savour-surface 0%, rgba(255, 255, 255, 0.98) 100%);
  border-right: 1px solid @savour-border;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  animation: sidebar-fade-in 0.5s ease both;

  // 右侧细微阴影
  &::after {
    content: '';
    position: absolute;
    top: 0;
    right: -8px;
    bottom: 0;
    width: 8px;
    background: linear-gradient(90deg, transparent 0%, rgba(45, 42, 38, 0.02) 100%);
    pointer-events: none;
  }

  .avatar-section {
    text-align: center;
    padding: @nordic-space-lg 0;
    border-bottom: 1px solid @savour-border;
    background: linear-gradient(180deg, @savour-accent-light 0%, @savour-surface 100%);

    .user-avatar {
      animation: avatar-pulse 3s ease-in-out infinite;
      cursor: pointer;
      transition: transform 0.3s ease;

      &:hover {
        transform: scale(1.05);
      }
    }

    .username {
      margin-top: @nordic-space-sm;
      .font-display();
      font-size: @nordic-text-md;
      font-weight: 600;
      color: @savour-text;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      width: 100%;
      line-height: 1.4;
      text-align: center;
    }
  }

  .menu-content {
    flex: 1;
    overflow-y: auto;
    padding: @nordic-space-sm 0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: @savour-border;
      border-radius: 2px;

      &:hover {
        background: @savour-text-muted;
      }
    }

    .menu-list {
      border: none;
      background: transparent;
      height: 100%;
      overflow-y: auto;
    }
  }

  // ===== 自定义菜单样式 =====
  .custom-menu {
    background: transparent;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      font-size: @nordic-text-base !important;
      color: @savour-text;
      border-radius: 0;
      margin: 2px @nordic-space-sm;
      transition: all 0.25s ease;
      position: relative;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 0;
        background: @savour-accent;
        border-radius: 0 2px 2px 0;
        transition: height 0.25s ease;
      }

      &:hover {
        background: @savour-accent-light;
        color: @savour-accent;

        &::before {
          height: 60%;
        }
      }

      .el-icon {
        color: @savour-text-sec;
        transition: color 0.25s ease;
      }

      &:hover .el-icon {
        color: @savour-accent;
      }
    }

    :deep(.el-menu-item.is-active),
    :deep(.el-sub-menu__title.is-active) {
      background: @savour-accent-light !important;
      color: @savour-accent !important;
      font-weight: 600;

      &::before {
        height: 70%;
      }

      .el-icon {
        color: @savour-accent;
      }
    }

    :deep(.el-sub-menu) {
      .el-menu {
        background: rgba(246, 243, 237, 0.5);
        padding: 4px 0;
      }

      .el-menu-item {
        padding-left: 52px !important;
        font-size: @nordic-text-sm !important;

        &::before {
          left: 32px;
        }
      }

      &.is-opened {
        > .el-sub-menu__title {
          .el-sub-menu__icon-arrow {
            transform: rotate(180deg);
          }
        }
      }
    }

    :deep(.el-sub-menu__icon-arrow) {
      color: @savour-text-muted;
      transition: transform 0.25s ease;
    }
  }

  // 菜单项带徽章容器
  .menu-item-with-badge {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    gap: @nordic-space-sm;

    .menu-text {
      flex: 1;
    }

    .unread-badge {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #E25B45, #FF7B5C);
      color: #fff;
      font-size: 11px;
      font-weight: 700;
      height: 18px;
      line-height: 18px;
      padding: 0 6px;
      min-width: 18px;
      border-radius: 9px;
      white-space: nowrap;
      flex-shrink: 0;
      box-shadow: 0 2px 6px rgba(226, 91, 69, 0.3);
    }
  }

  .setting-menu-container {
    border-top: 1px solid @savour-border;
    flex-shrink: 0;
    background: linear-gradient(180deg, transparent 0%, rgba(246, 243, 237, 0.3) 100%);

    .setting-menu-list {
      border: none;
      background: transparent;

      .setting-menu-item {
        background: transparent;
        border-radius: 0;
        margin: 2px @nordic-space-sm;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 0;
          background: @savour-text-muted;
          border-radius: 0 2px 2px 0;
          transition: height 0.25s ease;
        }

        &:hover::before {
          height: 60%;
          background: @savour-text-sec;
        }
      }
    }
  }
}

.content-area {
  flex: 1;
  padding: 0;
  background: @savour-bg;
  overflow-y: auto;
  position: relative;
}

/* ========== 页面转换动画样式 ========== */

/* 默认动画：淡入淡出 + 轻微滑动 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

/* 从右向左滑入动画 */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  width: 100%;
}

.slide-left-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-100%);
}

/* 从左向右滑入动画 */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  width: 100%;
}

.slide-right-enter-from {
  opacity: 0;
  transform: translateX(-100%);
}

.slide-right-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* 从下向上滑入动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  width: 100%;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

/* 缩放淡入动画 */
.zoom-fade-enter-active,
.zoom-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  width: 100%;
}

.zoom-fade-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.zoom-fade-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/* 弹性缩放动画 */
.bounce-enter-active {
  animation: bounce-in 0.4s;
}

.bounce-leave-active {
  animation: bounce-in 0.4s reverse;
}

@keyframes bounce-in {
  0% {
    opacity: 0;
    transform: scale(0.9);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

/* 旋转淡入动画 */
.rotate-fade-enter-active,
.rotate-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  width: 100%;
}

.rotate-fade-enter-from {
  opacity: 0;
  transform: rotateY(90deg);
}

.rotate-fade-leave-to {
  opacity: 0;
  transform: rotateY(-90deg);
}
</style>
