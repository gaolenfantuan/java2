/**
 * 后台管理员登录态的存取
 *
 * 和前台用户（kitty_user）完全分开：
 *   - 用的 key 不一样：kitty_admin
 *   - 存在 localStorage（关掉浏览器再打开还在，方便运营连续操作）
 *   - 路由守卫、后台接口都只认这个
 */

const KEY = 'kitty_admin'

/** 保存登录信息：{ token, adminId, username, nickname, expiresIn } */
export function saveAdmin(data) {
  localStorage.setItem(KEY, JSON.stringify(data))
}

/** 读取登录信息，没有或格式坏了返回 null */
export function getAdmin() {
  const raw = localStorage.getItem(KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch (e) {
    clearAdmin()
    return null
  }
}

/** 取 token（后台接口要放在请求头 X-Admin-Token 里） */
export function getAdminToken() {
  const admin = getAdmin()
  return admin && admin.token ? admin.token : ''
}

/** 是否已登录后台（后台路由守卫的唯一判据） */
export function isAdminLoggedIn() {
  return !!getAdminToken()
}

/** 退出后台登录 */
export function clearAdmin() {
  localStorage.removeItem(KEY)
}
