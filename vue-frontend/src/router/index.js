import { createRouter, createWebHistory } from 'vue-router'

// 首页
import HomePage from '../pages/HomePage.vue'

// 认证模块
import AuthPage from '../pages/auth/AuthPage.vue'
import SuperAdminPage from '../pages/auth/SuperAdminPage.vue'

// 网络命令模块
import CmdList from '../pages/cmd/CmdList.vue'
import CmdDetail from '../pages/cmd/CmdDetail.vue'
import CmdAdmin from '../pages/cmd/CmdAdmin.vue'
import CmdAdd from '../pages/cmd/CmdAdd.vue'
import CmdEdit from '../pages/cmd/CmdEdit.vue'

// 网络故障模块
import FaultList from '../pages/fault/FaultList.vue'
import FaultDetail from '../pages/fault/FaultDetail.vue'
import FaultAdmin from '../pages/fault/FaultAdmin.vue'
import FaultAdd from '../pages/fault/FaultAdd.vue'
import FaultEdit from '../pages/fault/FaultEdit.vue'

// 桌面运维模块
import DesktopList from '../pages/desktop/DesktopList.vue'
import DesktopDetail from '../pages/desktop/DesktopDetail.vue'
import DesktopAdmin from '../pages/desktop/DesktopAdmin.vue'
import DesktopAdd from '../pages/desktop/DesktopAdd.vue'
import DesktopEdit from '../pages/desktop/DesktopEdit.vue'

// Linux 模块
import LinuxList from '../pages/linux/LinuxList.vue'
import LinuxDetail from '../pages/linux/LinuxDetail.vue'
import LinuxAdmin from '../pages/linux/LinuxAdmin.vue'
import LinuxAdd from '../pages/linux/LinuxAdd.vue'
import LinuxEdit from '../pages/linux/LinuxEdit.vue'

// Office 模块
import OfficeList from '../pages/office/OfficeList.vue'
import OfficeDetail from '../pages/office/OfficeDetail.vue'
import OfficeAdmin from '../pages/office/OfficeAdmin.vue'
import OfficeAdd from '../pages/office/OfficeAdd.vue'
import OfficeEdit from '../pages/office/OfficeEdit.vue'

// 个人页面
import ProfilePage from '../pages/auth/ProfilePage.vue'

// AI运维模块
import AiList from '../pages/ai/AiList.vue'
import AiDetail from '../pages/ai/AiDetail.vue'
import AiAdmin from '../pages/ai/AiAdmin.vue'
import AiAdd from '../pages/ai/AiAdd.vue'
import AiEdit from '../pages/ai/AiEdit.vue'

const routes = [
  { path: '/', name: 'home', component: HomePage },
  // 认证
  { path: '/login', name: 'login', component: AuthPage },
  { path: '/register', name: 'register', component: AuthPage },
  { path: '/super-admin', name: 'super-admin', component: SuperAdminPage, meta: { requiresAuth: true, requiresSuperAdmin: true } },
  // 网络命令
  { path: '/cmd', name: 'cmd-list', component: CmdList },
  { path: '/cmd/detail/:id', name: 'cmd-detail', component: CmdDetail },
  { path: '/cmd/admin', name: 'cmd-admin', component: CmdAdmin, meta: { requiresAuth: true } },
  { path: '/cmd/add', name: 'cmd-add', component: CmdAdd, meta: { requiresAuth: true } },
  { path: '/cmd/edit/:id', name: 'cmd-edit', component: CmdEdit, meta: { requiresAuth: true } },
  // 网络故障
  { path: '/fault', name: 'fault-list', component: FaultList },
  { path: '/fault/detail/:id', name: 'fault-detail', component: FaultDetail },
  { path: '/fault/admin', name: 'fault-admin', component: FaultAdmin, meta: { requiresAuth: true } },
  { path: '/fault/add', name: 'fault-add', component: FaultAdd, meta: { requiresAuth: true } },
  { path: '/fault/edit/:id', name: 'fault-edit', component: FaultEdit, meta: { requiresAuth: true } },
  // 桌面运维
  { path: '/desktop', name: 'desktop-list', component: DesktopList },
  { path: '/desktop/detail/:id', name: 'desktop-detail', component: DesktopDetail },
  { path: '/desktop/admin', name: 'desktop-admin', component: DesktopAdmin, meta: { requiresAuth: true } },
  { path: '/desktop/add', name: 'desktop-add', component: DesktopAdd, meta: { requiresAuth: true } },
  { path: '/desktop/edit/:id', name: 'desktop-edit', component: DesktopEdit, meta: { requiresAuth: true } },
  // Linux
  { path: '/linux', name: 'linux-list', component: LinuxList },
  { path: '/linux/detail/:id', name: 'linux-detail', component: LinuxDetail },
  { path: '/linux/admin', name: 'linux-admin', component: LinuxAdmin, meta: { requiresAuth: true } },
  { path: '/linux/add', name: 'linux-add', component: LinuxAdd, meta: { requiresAuth: true } },
  { path: '/linux/edit/:id', name: 'linux-edit', component: LinuxEdit, meta: { requiresAuth: true } },
  // Office
  { path: '/office', name: 'office-list', component: OfficeList },
  { path: '/office/detail/:id', name: 'office-detail', component: OfficeDetail },
  { path: '/office/admin', name: 'office-admin', component: OfficeAdmin, meta: { requiresAuth: true } },
  { path: '/office/add', name: 'office-add', component: OfficeAdd, meta: { requiresAuth: true } },
  { path: '/office/edit/:id', name: 'office-edit', component: OfficeEdit, meta: { requiresAuth: true } },
  // 个人页面
  { path: '/profile', name: 'profile', component: ProfilePage, meta: { requiresAuth: true } },
  // AI运维
  { path: '/ai', name: 'ai-list', component: AiList },
  { path: '/ai/detail/:id', name: 'ai-detail', component: AiDetail },
  { path: '/ai/admin', name: 'ai-admin', component: AiAdmin, meta: { requiresAuth: true } },
  { path: '/ai/add', name: 'ai-add', component: AiAdd, meta: { requiresAuth: true } },
  { path: '/ai/edit/:id', name: 'ai-edit', component: AiEdit, meta: { requiresAuth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 需要认证的路由
  if (to.meta.requiresAuth) {
    if (!token) {
      return next('/login')
    }
    // 超级管理员页面额外检查
    if (to.meta.requiresSuperAdmin && role !== 'SUPER_ADMIN') {
      return next('/')
    }
  }

  // 已登录用户访问登录页，重定向到首页
  if (to.path === '/login' && token) {
    return next('/')
  }

  next()
})

export default router
