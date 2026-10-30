/**
 * 登录态路由增强守卫（新增，不改动原有 guard.js 的逻辑）
 *
 * 原有守卫解决的是"没登录不让进 /home"；
 * 这个守卫解决的是反方向："已经登录了还留在 /login、/register"，
 * 避免登录成功后手动输地址又绕回登录页。
 *
 * 返回 true 放行，返回地址对象表示重定向。
 */
import { isLoggedIn } from '../utils/auth.js'

/** 已登录后不应该再停留的页面 */
const SKIP_WHEN_LOGGED_IN = ['/login', '/register']

export function loggedInRedirect(to) {
  if (SKIP_WHEN_LOGGED_IN.includes(to.path) && isLoggedIn()) {
    // 已登录却访问登录/注册页 -> 直接送回首页
    return { path: '/home' }
  }
  return true
}
