/**
 * 登录 / 注册接口封装
 * Vite 已配置代理：/api -> http://localhost:8080
 * 所以这里写相对路径就行，不用写完整域名。
 */

const BASE_URL = '/api/auth'

/**
 * 统一请求方法：把后端的 { code, message, data } 转成好用的结果
 * 成功：返回 data
 * 失败：抛出 Error，message 就是后端给的提示语
 */
async function request(path, options = {}) {
  let res
  try {
    res = await fetch(BASE_URL + path, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    })
  } catch (e) {
    // 网络层直接失败，通常是后端没启动
    throw new Error('连不上服务器，请确认后端已经启动（localhost:8080）')
  }

  let body
  try {
    body = await res.json()
  } catch (e) {
    throw new Error('服务器返回的数据格式不对')
  }

  if (body.code !== 200) {
    throw new Error(body.message || '请求失败')
  }
  return body.data
}

/** 登录，参数 { username, password } */
export function login(payload) {
  return request('/login', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

/** 注册，参数 { username, nickname, password } */
export function register(payload) {
  return request('/register', {
    method: 'POST',
    body: JSON.stringify(payload)
  })
}

/** 检查用户名是否已被占用，返回 true 表示已存在 */
export function checkUsername(username) {
  return request('/check?username=' + encodeURIComponent(username), { method: 'GET' })
}
