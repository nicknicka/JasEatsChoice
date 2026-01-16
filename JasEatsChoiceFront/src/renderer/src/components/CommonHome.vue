<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted, computed, watch, provide, nextTick } from 'vue'
import {
  Search,
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
  CircleClose,
  CircleCheckFilled,
  Goods,
  Dish
} from '@element-plus/icons-vue'
import { decodeJwt } from '../utils/api.js'
import { useAuthStore } from '../store/authStore'
import { useUserStore } from '../store/userStore'
// 导入CommonAvatar组件
import CommonAvatar from './CommonAvatar.vue'

const router = useRouter()

// 导航到指定路径
const navigateTo = (path) => {
  router.push(path)
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
  if (userStore.userInfo) {
    userStore.userInfo.avatar = avatarUrl
  }
}
provide('updateSidebarAvatar', updateSidebarAvatar)

// 预定义菜单数据
const menuData = {
  // 用户端菜单 - 分组折叠版本
  user: [
    { index: '1', name: '首页', icon: HomeFilled, path: '/user/home' }, // 首页入口
    // 推荐与发现分组
    {
      index: 'group-1',
      name: '推荐与发现',
      icon: Menu,
      children: [
        { index: '2', name: '我的推荐', icon: Menu, path: '/user/home/recommend' },
        { index: '3', name: '商家查找', icon: Shop, path: '/user/home/merchants' }
      ]
    },
    // 饮食管理分组
    {
      index: 'group-2',
      name: '饮食管理',
      icon: Calendar,
      children: [
        { index: '4', name: '今日食谱', icon: Calendar, path: '/user/home/today-recipe' },
        { index: '61', name: '饮食记录', icon: Calendar, path: '/user/home/diet-record' },
        { index: '5', name: '卡路里统计', icon: DataAnalysis, path: '/user/home/calorie' },
        { index: '6', name: '我的食谱', icon: Document, path: '/user/home/my-recipe' }
      ]
    },
    // 个人中心分组
    {
      index: 'group-3',
      name: '个人中心',
      icon: User,
      children: [
        { index: '7', name: '用户中心', icon: User, path: '/user/home/profile' },
        { index: '8', name: '订单中心', icon: List, path: '/user/home/orders' }
      ]
    },
    // 智能助手分组
    {
      index: 'group-4',
      name: '智能助手',
      icon: ChatDotRound,
      children: [
        { index: '9', name: '消息中心', icon: Message, path: '/user/home/message-center' },
        { index: '11', name: '聊天消息', icon: ChatDotRound, path: '/user/home/chat' },
        { index: '10', name: 'AI饮食助手', icon: ChatDotRound, path: '/user/home/ai' }
      ]
    },
    // 设置菜单
    {
      index: '12',
      name: '设置',
      icon: Setting,
      path: '/user/home/settings',
      isSetting: true
    }
  ],
  // 商家端菜单
  // 商家端菜单 - 按功能模块重新排序：首页 → 核心业务 → 店铺管理 → 客户沟通 → 经营分析
  merchant: [
    // 首页模块
    { index: '1', name: '我的店铺', icon: HomeFilled, path: '/merchant/home' },

    // 订单管理模块
    {
      index: '2',
      name: '订单管理',
      icon: List,
      children: [
        { index: '2-1', name: '今日订单', icon: Calendar, path: '/merchant/home/today-orders' },
        { index: '2-2', name: '全部订单', icon: Document, path: '/merchant/home/orders' }
      ]
    },

    // 店铺管理模块
    {
      index: '3',
      name: '店铺管理',
      icon: Shop,
      children: [
        { index: '3-1', name: '菜单管理', icon: Shop, path: '/merchant/home/menu' },
        { index: '3-2', name: '菜品管理', icon: Document, path: '/merchant/home/dish-management' }
      ]
    },

    // 客户沟通模块
    {
      index: '4',
      name: '客户沟通',
      icon: Message,
      children: [
        { index: '4-1', name: '商家聊天', icon: ChatDotRound, path: '/merchant/home/chat' },
        { index: '4-2', name: '消息管理', icon: Message, path: '/merchant/home/messages' }
      ]
    },

    // 经营分析模块
    {
      index: '5',
      name: '经营分析',
      icon: DataAnalysis,
      children: [
        { index: '5-1', name: '评价中心', icon: DataAnalysis, path: '/merchant/home/comments' },
        { index: '5-2', name: '经营统计', icon: DataAnalysis, path: '/merchant/home/statistics' }
      ]
    },

    // 设置菜单
    {
      index: '6',
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
    { pattern: /\/user\/home\/order-confirmation/, menuIndex: '8' }, // 订单确认 → 查看订单
    { pattern: /\/user\/home\/order-detail/, menuIndex: '8' }, // 订单详情 → 查看订单
    { pattern: /\/user\/home\/system-notification/, menuIndex: '9' }, // 系统通知 → 消息中心
    { pattern: /\/user\/home\/address/, menuIndex: '7' }, // 地址管理 → 用户中心
    { pattern: /\/user\/home\/contact/, menuIndex: '7' }, // 联系客服 → 用户中心
    { pattern: /\/user\/home\/my-collection/, menuIndex: '2' }, // 我的收藏 → 我的推荐
    { pattern: /\/user\/home\/tutorials/, menuIndex: '2' } // 制作教程 → 我的推荐
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
    console.log('智能匹配到父级菜单:', parentMenuIndex)
  } else {
    // 实在没有匹配，才激活第一个菜单项
    activeMenuIndex.value = currentMenu.value[0]?.index || '1'
    console.log('未匹配到菜单项，默认激活第一个')
  }

  // 重置侧边栏宽度为默认值，防止自动展开菜单时宽度变宽
  sidebarWidth.value = '170px' // 这里的默认宽度要和初始化时一致

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

  // 如果找到目标菜单，进行跳转
  if (targetMenuItem) {
    navigateTo(targetMenuItem.path)
  }
}

// 头像放大弹窗
const showLargeAvatar = ref(false)

const sidebarWidth = ref('170px')

// 监听菜单展开事件 - 展开时增宽，给二级菜单足够空间
const handleMenuOpen = () => {
  sidebarWidth.value = '220px' // 展开时增宽

  // 菜单展开时，移除所有父菜单标题的激活类，避免视觉冲突
  // const menuTitles = document.querySelectorAll('.menu-list .el-sub-menu__title')
  // menuTitles.forEach(title => title.classList.remove('is-active'))
}

// 监听菜单关闭事件 - 关闭时恢复默认较短宽度
const handleMenuClose = () => {
  sidebarWidth.value = '170px' // 关闭时恢复默认宽度

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

    // 跳转对应页面
    if (userRole.value === 'user') {
      navigateTo('/user/home')
    } else {
      navigateTo('/merchant/home')
    }

    // Don't save role to localStorage - always default to user

    console.log('角色切换成功:', userRole.value)
  } catch (error) {
    console.error('角色切换失败:', error)
  }
}

// 页面加载时从当前路由恢复角色，默认进入用户角色
onMounted(() => {
  try {
    if (!userStore.userInfo || userStore.userInfo.avatar === '') {
      userStore.fetchUserInfo()
    }

    // 1. First check current route to determine role
    let detectedRole = 'user' // Always default to user

    if (router.currentRoute.value?.path?.startsWith('/merchant/')) {
      detectedRole = 'merchant'
    }

    // 3. Always use detected role from route or default to user, ignore saved role
    userRole.value = detectedRole

    // User info is now managed through Pinia - no need to initialize it here
    // 从JWT令牌获取实际用户名（仅作参考，实际应用应将用户信息存储在userStore中）
    if (userRole.value === 'user' && authStore.token) {
      const decodedToken = decodeJwt(authStore.token)
      if (decodedToken && decodedToken.username && userStore.userInfo) {
        userStore.userInfo.name = decodedToken.username
      }
    }

    // Don't save role to localStorage - always default to user

    console.log('恢复角色成功:', userRole.value)

    // 确保当前菜单已更新后再计算激活菜单，使用nextTick确保DOM更新完成
    nextTick(() => {
      // 等待路由完全准备就绪
      router.isReady().then(() => {
        updateActiveMenuIndex()
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

      // Update user info based on role (using Pinia store)
      if (userRole.value === 'merchant') {
        // 商户端信息从userStore.merchantInfo获取
        userStore.userInfo = {
          name: '商户端',
          avatar: userStore.merchantInfo?.avatar || ''
        }
      } else if (userRole.value === 'user') {
        // 从authStore获取token并解码用户名
        let username = '用户端'
        if (authStore.token) {
          const decodedToken = decodeJwt(authStore.token)
          if (decodedToken && decodedToken.username) {
            username = decodedToken.username
          }
        }
        // 使用userStore管理用户信息
        userStore.userInfo = {
          ...userStore.userInfo
        }
      }

      // Role is now managed through Pinia - no need to save to localStorage
      console.log('路由变化自动更新角色:', userRole.value)
      // 更新角色后，重新计算激活的菜单项索引
      console.log('=== 更新角色后调用updateActiveMenuIndex ===')
      updateActiveMenuIndex()
    }
  }
)

const searchQuery = ref('')

const handleSearch = (value) => {
  // 实现搜索逻辑
  try {
    // 如果搜索内容为空，不执行搜索
    if (!value || value.trim() === '') {
      return
    }

    console.log('开始搜索:', value)

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
      console.log('商家角色搜索功能:', value)
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
        🎨 佳食宜选
      </div>
      <el-input
        v-model="searchQuery"
        placeholder="🔍 搜索框(支持菜品/商家搜索)"
        clearable
        class="search-input"
        @input="handleSearch"
        @keyup.enter="handleSearch(searchQuery)"
      >
        <template #append>
          <el-button type="primary" @click="handleSearch(searchQuery)">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
        </template>
      </el-input>
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
            :avatar-url="userStore.userInfo?.avatar"
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
            class="menu-list"
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
                    <template #title>{{ childItem.name }}</template>
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
          <el-menu class="setting-menu-list" @select="handleMenuSelect">
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
      <el-main class="content-area">
        <router-view />
      </el-main>
    </div>

    <!-- 头像放大对话框 -->
    <el-dialog v-model="showLargeAvatar" title="个人头像" width="300px" top="20%">
      <div style="text-align: center; padding: 20px 0">
        <CommonAvatar
          :size="200"
          class="user-avatar"
          :avatar-url="userStore.userInfo?.avatar"
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
.app-container {
  height: 100vh;
  width: 100%;
}

.top-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: #ff6b6b;
  cursor: pointer;
}

.search-input {
  width: 400px;
  margin: 0 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-right: 10px;

  .identity-switch {
    font-size: 18px;
    padding: 0;

    .user-icon,
    .merchant-icon {
      transition:
        transform 0.3s ease,
        color 0.3s ease;
      font-size: 18px;
    }

    .icon-enlarged {
      transform: scale(1.3);
      color: #ff6b6b;
    }
  }
}

.main-content {
  display: flex;
  height: calc(100vh - 60px);
}

.sidebar-menu {
  background-color: #fff;
  border-right: 1px solid #eee;
  transition: width 0.3s ease-in-out; /* 添加平滑过渡动画 */
  display: flex;
  flex-direction: column;
  height: 100%;

  .avatar-section {
    text-align: center;
    padding: 20px 0;
    border-bottom: 1px solid #eee;

    .username {
      margin-top: 8px;
      font-size: 14px;
      font-weight: 500;
      color: #333;
      white-space: nowrap; /* 不换行 */
      overflow: hidden; /* 隐藏溢出 */
      text-overflow: ellipsis; /* 显示省略号 */
      width: 100%; /* 自适应宽度 */
    }
  }

  .menu-content {
    flex: 1;
    overflow-y: auto;

    .menu-list {
      border: none;
      height: 100%;
      overflow-y: auto;
    }
  }

  /* 当一级菜单组包含激活的子菜单时，保持高亮 */
  /* 确保即使在 scoped 样式下，激活状态也能正确应用 */
  .menu-list,
  .setting-menu-list {
    :deep(.el-menu-item.is-active),
    :deep(.el-sub-menu__title.is-active) {
      background-color: var(--el-menu-item-hover-bg-color) !important;
      color: var(--el-menu-active-color) !important;
    }
  }

  .setting-menu-container {
    border-top: 1px solid #eee;
    flex-shrink: 0;

    .setting-menu-list {
      border: none;
      background-color: transparent;

      .setting-menu-item {
        background-color: transparent;
      }
    }
  }
}

.content-area {
  flex: 1; /* 让内容区占据剩余空间 */
  padding: 20px;
  background-color: #f5f5f5;
  overflow-y: auto;
}
</style>
