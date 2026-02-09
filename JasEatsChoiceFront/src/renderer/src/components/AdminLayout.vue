<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="admin-aside">
      <div class="logo-container">
        <el-icon class="logo-icon"><Platform /></el-icon>
        <span v-if="!isCollapse" class="logo-text">管理员后台</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="admin-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <template #title>控制台</template>
        </el-menu-item>

        <el-sub-menu index="users">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="/admin/users">用户列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="merchants">
          <template #title>
            <el-icon><Shop /></el-icon>
            <span>商家管理</span>
          </template>
          <el-menu-item index="/admin/merchants">商家列表</el-menu-item>
          <el-menu-item index="/admin/merchants/audit">商家审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="orders">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>订单管理</span>
          </template>
          <el-menu-item index="/admin/orders">订单列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="dishes">
          <template #title>
            <el-icon><Food /></el-icon>
            <span>菜品管理</span>
          </template>
          <el-menu-item index="/admin/dishes">菜品列表</el-menu-item>
          <el-menu-item index="/admin/dishes/audit">菜品审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="tutorials">
          <template #title>
            <el-icon><VideoCamera /></el-icon>
            <span>教程管理</span>
          </template>
          <el-menu-item index="/admin/tutorials/manage">教程列表</el-menu-item>
          <el-menu-item index="/admin/tutorials/review">教程审核</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="content">
          <template #title>
            <el-icon><ChatLineSquare /></el-icon>
            <span>内容管理</span>
          </template>
          <el-menu-item index="/admin/topics">热点话题</el-menu-item>
          <el-menu-item index="/admin/announcements">公告管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="finance">
          <template #title>
            <el-icon><Wallet /></el-icon>
            <span>财务管理</span>
          </template>
          <el-menu-item index="/admin/finance/withdrawals">提现审核</el-menu-item>
          <el-menu-item index="/admin/finance/recharges">充值记录</el-menu-item>
          <el-menu-item index="/admin/finance/refunds">退款管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="settings">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="/admin/settings/roles">角色管理</el-menu-item>
          <el-menu-item index="/admin/settings/permissions">权限管理</el-menu-item>
          <el-menu-item index="/admin/settings/logs">系统日志</el-menu-item>
          <el-menu-item index="/admin/settings">系统配置</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/admin/statistics">
          <el-icon><TrendCharts /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶栏 -->
      <el-header height="60px" class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="{ path: item.path }">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 刷新按钮 -->
          <el-tooltip content="刷新" placement="bottom">
            <el-icon class="header-icon" @click="refreshPage">
              <Refresh />
            </el-icon>
          </el-tooltip>

          <!-- 全屏按钮 -->
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" @click="toggleFullscreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>

          <!-- 用户信息下拉菜单 -->
          <el-dropdown class="user-dropdown" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32">
                {{ getDisplayName().charAt(0) }}
              </el-avatar>
              <span class="username">{{ getDisplayName() }}</span>
              <el-tag v-if="getRoleName()" size="small" type="warning">{{ getRoleName() }}</el-tag>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="admin-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  DataBoard,
  User,
  Shop,
  Document,
  Food,
  VideoCamera,
  ChatLineSquare,
  Wallet,
  Setting,
  TrendCharts,
  Platform,
  Fold,
  Expand,
  Refresh,
  FullScreen,
  ArrowDown,
  Lock,
  SwitchButton
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentAdmin } from '@/api/admin'
import { getAdminInfo, adminLogout } from '@/utils/auth'
import { ROLE_NAME_MAP } from '@/config'

const router = useRouter()
const route = useRoute()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 管理员信息 - 从localStorage获取
const adminInfo = ref(getAdminInfo() || {
  adminId: '',
  username: '管理员',
  realName: '管理员',
  avatar: '',
  roleCode: '',
  roleName: '超级管理员'
})

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title
  }))
})

// 切换侧边栏折叠
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 刷新页面
const refreshPage = () => {
  router.go(0)
}

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
  }
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/admin/settings/profile')
      break
    case 'password':
      router.push('/admin/settings/password')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        // 使用统一的登出方法
        adminLogout()
        ElMessage.success('退出登录成功')
      } catch {
        // 用户取消
      }
      break
  }
}

// 获取管理员信息
const fetchAdminInfo = async () => {
  try {
    const response = await getCurrentAdmin()
    console.log('[AdminLayout] 获取管理员信息', response)
    if (response && response.success && response.admin) {
      // 确保字符串正确编码（处理可能的乱码问题）
      const newAdminInfo = {
        adminId: response.admin.adminId,
        username: String(response.admin.username || '管理员'),
        realName: String(response.admin.realName || '管理员'),
        avatar: response.admin.avatar || '',
        roleCode: response.admin.roleCode || '',
        roleName: String(response.admin.roleName || '超级管理员')
      }
      // 更新localStorage中的管理员信息
      localStorage.setItem('admin_info', JSON.stringify(newAdminInfo))
      adminInfo.value = newAdminInfo
      console.log('[AdminLayout] 管理员信息已更新:', adminInfo.value)
    }
  } catch (error) {
    console.error('[AdminLayout] 获取管理员信息失败:', error)
    // 如果获取失败，确保有默认值
    if (!adminInfo.value || !adminInfo.value.username) {
      adminInfo.value = {
        adminId: '',
        username: '管理员',
        realName: '管理员',
        avatar: '',
        roleCode: '',
        roleName: '超级管理员'
      }
    }
  }
}

// 获取显示名称（安全处理中文和空值）
const getDisplayName = () => {
  try {
    const name = adminInfo.value?.username || '管理员'
    // 确保是字符串并去除首尾空格
    return String(name).trim() || '管理员'
  } catch (error) {
    console.error('[AdminLayout] 获取显示名称失败:', error)
    return '管理员'
  }
}

// 获取角色名称（修复乱码问题）
const getRoleName = () => {
  try {
    const roleCode = adminInfo.value?.roleCode
    const roleName = adminInfo.value?.roleName

    // 如果有roleCode映射，直接使用映射值（最可靠的方式）
    if (roleCode && ROLE_NAME_MAP && ROLE_NAME_MAP[roleCode]) {
      return ROLE_NAME_MAP[roleCode]
    }

    // 检测乱码：包含常见的编码错误字符或异常长的字符串
    if (!roleName || roleName.length > 50 || /[\u0080-\uFFFF][\u0000-\u007F]/.test(roleName)) {
      return ROLE_NAME_MAP?.[roleCode] || ROLE_NAME_MAP?.['SUPER_ADMIN'] || '超级管理员'
    }

    return roleName || '超级管理员'
  } catch (error) {
    console.error('[AdminLayout] 获取角色名称失败:', error)
    return '超级管理员'
  }
}

// 初始化时获取管理员信息
fetchAdminInfo()

// 监听路由变化
watch(
  () => route.path,
  () => {
    // 可以在这里做一些路由变化时的处理
  }
)
</script>

<style scoped lang="less">
.admin-layout {
  height: 100vh;

  .admin-aside {
    background: #304156;
    transition: width 0.3s;
    overflow-x: hidden;

    .logo-container {
      height: 60px;
      display: flex;
      align-items: center;
      padding: 0 20px;
      background: #2b3a4b;

      .logo-img {
        width: 32px;
        height: 32px;
        margin-right: 12px;
      }

      .logo-text {
        font-size: 18px;
        font-weight: bold;
        color: #fff;
      }

      .logo-icon {
        font-size: 24px;
        color: #fff;
        margin: 0 auto;
      }
    }

    :deep(.el-menu) {
      border-right: none;
      background: #304156;

      .el-menu-item,
      .el-sub-menu__title {
        color: #bfcbd9;

        &:hover {
          background: #263445;
        }
      }

      .el-menu-item.is-active {
        background: #409eff !important;
        color: #fff;
      }

      .el-sub-menu.is-active > .el-sub-menu__title {
        color: #409eff;
      }
    }
  }

  .admin-header {
    background: #fff;
    border-bottom: 1px solid #e6e6e6;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;

    .header-left {
      display: flex;
      align-items: center;

      .collapse-icon {
        font-size: 20px;
        cursor: pointer;
        margin-right: 20px;
        color: #5a5e66;

        &:hover {
          color: #409eff;
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 20px;

      .header-icon {
        font-size: 18px;
        cursor: pointer;
        color: #5a5e66;

        &:hover {
          color: #409eff;
        }
      }

      .user-dropdown {
        .user-info {
          display: flex;
          align-items: center;
          gap: 8px;
          cursor: pointer;
          padding: 0 12px;
          height: 40px;
          border-radius: 4px;
          transition: background 0.3s;

          &:hover {
            background: #f5f7fa;
          }

          .username {
            font-size: 14px;
            color: #303133;
          }
        }
      }
    }
  }

  .admin-main {
    background: #f0f2f5;
    padding: 20px;
    overflow-y: auto;
  }
}

// 路由过渡动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
