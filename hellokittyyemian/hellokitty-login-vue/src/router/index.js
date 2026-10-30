import { createRouter, createWebHistory } from 'vue-router'
import { authGuard } from './guard.js'
// 新增：已登录访问 /login、/register 时自动跳回 /home
import { loggedInRedirect } from './loggedInGuard.js'

import KittyLogin from '../components/KittyLogin.vue'
import KittyRegister from '../components/KittyRegister.vue'
import Home from '../views/Home.vue'
import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import AdminItems from '../views/admin/AdminItems.vue'
import AdminCategories from '../views/admin/AdminCategories.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'login',
    component: KittyLogin,
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: KittyRegister,
    meta: { title: '注册' }
  },
  {
    path: '/home',
    name: 'home',
    component: Home,
    // requiresAuth 为 true，路由守卫会拦住没登录的人
    meta: { title: '首页', requiresAuth: true }
  },

  // ================= 后台管理系统 =================
  {
    // 后台登录页：只有登录，没有注册
    path: '/admin/login',
    name: 'admin-login',
    component: AdminLogin,
    meta: { title: '后台登录' }
  },
  {
    // 后台主体：左侧边栏 + 右侧内容区，子页面渲染在 <router-view /> 里
    path: '/admin',
    component: AdminLayout,
    redirect: '/admin/dashboard',
    meta: { requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: AdminDashboard,
        meta: { title: '数据概览', requiresAdmin: true }
      },
      {
        path: 'items',
        name: 'admin-items',
        component: AdminItems,
        meta: { title: '内容管理', requiresAdmin: true }
      },
      {
        path: 'categories',
        name: 'admin-categories',
        component: AdminCategories,
        meta: { title: '分类管理', requiresAdmin: true }
      }
    ]
  },

  {
    // 兜底：访问不存在的路径回登录页
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置守卫：需要登录的页面（meta.requiresAuth / meta.requiresAdmin）没 token 就跳回对应登录页
 * 具体判断在 guard.js 里，这里只做接线
 */
router.beforeEach((to) => authGuard(to))

// 新增守卫：已登录还访问登录/注册页 -> 送回首页（与上面互不影响，先后的两个守卫都通过才放行）
router.beforeEach((to) => loggedInRedirect(to))

// 顺手把页面标题改成"Hello Kitty · 登录 / 首页"这样
router.afterEach((to) => {
  const t = to.meta && to.meta.title
  document.title = t ? `Hello Kitty · ${t}` : 'Hello Kitty'
})

export default router
