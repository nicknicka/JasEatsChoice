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
                {{ (adminInfo.realName || adminInfo.username).charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ adminInfo.realName || adminInfo.username }}</span>
              <el-tag v-if="adminInfo.roleName" size="small" type="warning">{{ adminInfo.roleName }}</el-tag>
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

const router = useRouter()
const route = useRoute()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 管理员信息
const adminInfo = ref({
  username: 'admin',
  realName: '系统管理员',
  avatar: '',
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
        // 清除token
        localStorage.removeItem('admin_token')
        // 跳转到登录页
        router.push('/admin/login')
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
    // TODO: 调用API获取管理员信息
    // const response = await api.get('/api/admin/current')
    // adminInfo.value = response.data.admin
  } catch (error) {
    console.error('获取管理员信息失败:', error)
  }
}

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
