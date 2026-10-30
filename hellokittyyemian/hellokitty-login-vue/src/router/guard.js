/**
 * 路由守卫（单独放一个文件，方便直接测试，不依赖 .vue 文件）
 *
 * 返回 true 表示放行，返回一个地址对象表示重定向。
 *
 * 两种受保护页面：
 *   meta.requiresAuth   -> 前台页面，判断 kitty_user（普通用户）
 *   meta.requiresAdmin  -> 后台页面，判断 kitty_admin（管理员）
 *   两者互不干扰：普通用户登录了也进不去后台，管理员登录了也不代表前台已登录。
 */
import { isLoggedIn } from '../utils/auth.js'
import { isAdminLoggedIn } from '../utils/adminAuth.js'

export function authGuard(to) {
  // 后台页面：没管理员 token 就去后台登录页
  if (to.meta && to.meta.requiresAdmin && !isAdminLoggedIn()) {
    return {
      path: '/admin/login',
      query: { redirect: to.fullPath || to.path }
    }
  }

  // 前台页面：没登录就去前台登录页
  if (to.meta && to.meta.requiresAuth && !isLoggedIn()) {
    return {
      path: '/login',
      // 记住原本要去的地址，登录成功后可以跳回去
      query: { redirect: to.fullPath || to.path }
    }
  }

  return true
}
