/**
 * 登录状态的存取（路由守卫和首页都用这里的判断）
 *
 * 约定：
 *   - 勾了"记住我"  -> localStorage，关掉浏览器再打开还是登录状态
 *   - 没勾          -> sessionStorage，关掉浏览器就退出
 *
 * 两个地方都查，是因为用户可能选择其中任意一种。
 */

const KEY = 'kitty_user'

/** 保存登录信息，data 是后端返回的 { id, username, nickname, token } */
export function saveUser(data, remember = false) {
  const value = JSON.stringify({ ...data, rememberMe: !!remember })
  if (remember) {
    localStorage.setItem(KEY, value)
    sessionStorage.removeItem(KEY)
  } else {
    sessionStorage.setItem(KEY, value)
    localStorage.removeItem(KEY)
  }
}

/** 读取登录信息，没有则返回 null */
export function getUser() {
  const raw = sessionStorage.getItem(KEY) || localStorage.getItem(KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    // 数据被改坏了就清掉，避免一直卡在异常状态
    clearUser()
    return null
  }
}

/** 取出 token，没有则返回空字符串 */
export function getToken() {
  const user = getUser()
  return user && user.token ? user.token : ''
}

/** 是否已登录（路由守卫的唯一判据） */
export function isLoggedIn() {
  return !!getToken()
}

/** 退出登录：两个存储都清掉 */
export function clearUser() {
  sessionStorage.removeItem(KEY)
  localStorage.removeItem(KEY)
}
