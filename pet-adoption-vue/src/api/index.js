import axios from 'axios'

// 开发环境：/api 由 Vite 代理到 http://localhost:8080
// 后端接口路径：POST /api/auth/login、POST /api/auth/register
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

// 直接把后端 Result 的 data 部分（整个 response body）交给业务代码
request.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function checkUsername(username) {
  return request.get('/auth/check', { params: { username } })
}
