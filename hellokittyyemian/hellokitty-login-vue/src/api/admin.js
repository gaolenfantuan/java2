/**
 * 后台管理接口封装
 *
 * 和 src/api/home.js 的区别：
 *   1. 每个请求都会自动带上请求头 X-Admin-Token
 *   2. 后端返回 code 401（token 失效）时，先清掉本地登录态，再通知外层跳回登录页
 *
 * 后端统一返回 { code, message, data }，code === 200 才算成功。
 */

import { getAdminToken, clearAdmin } from '../utils/adminAuth.js'

const BASE_URL = '/api/admin'

/** 401 时要做什么，由 AdminLayout 在挂载时注册（跳回登录页） */
let unauthorizedHandler = null

export function setUnauthorizedHandler(fn) {
  unauthorizedHandler = fn
}

function buildQuery(params) {
  const query = new URLSearchParams()
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') query.append(k, v)
  })
  return query.toString()
}

async function request(path, { method = 'GET', body, params } = {}) {
  const query = buildQuery(params)
  const url = BASE_URL + path + (query ? '?' + query : '')

  const headers = {}
  const token = getAdminToken()
  if (token) headers['X-Admin-Token'] = token
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let res
  try {
    res = await fetch(url, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body)
    })
  } catch (e) {
    throw new Error('连不上服务器，请确认后端已经启动（localhost:8080）')
  }

  let data
  try {
    data = await res.json()
  } catch (e) {
    throw new Error('服务器返回的数据格式不对')
  }

  // token 失效：清掉本地登录态，交给 layout 跳登录页
  if (data.code === 401) {
    clearAdmin()
    if (unauthorizedHandler) unauthorizedHandler()
    const err = new Error(data.message || '登录已过期，请重新登录')
    err.unauthorized = true
    throw err
  }

  if (data.code !== 200) {
    throw new Error(data.message || '请求失败')
  }
  return data.data
}

// ---------------- 登录 ----------------
export function adminLogin(username, password) {
  return request('/auth/login', { method: 'POST', body: { username, password } })
}

export function adminMe() {
  return request('/auth/me')
}

export function adminLogout() {
  return request('/auth/logout', { method: 'POST' })
}

// ---------------- 概览 ----------------
export function fetchAdminStats() {
  return request('/stats')
}

// ---------------- 内容管理 ----------------
export function fetchAdminItems(p = {}) {
  return request('/items', {
    params: {
      keyword: p.keyword,
      categoryId: p.categoryId,
      status: p.status,
      page: p.page,
      size: p.size
    }
  })
}

export function fetchAdminItem(id) {
  return request('/items/' + encodeURIComponent(id))
}

export function createAdminItem(payload) {
  return request('/items', { method: 'POST', body: payload })
}

export function updateAdminItem(id, payload) {
  return request('/items/' + encodeURIComponent(id), { method: 'PUT', body: payload })
}

export function deleteAdminItem(id) {
  return request('/items/' + encodeURIComponent(id), { method: 'DELETE' })
}

// ---------------- 分类管理 ----------------
export function fetchAdminCategories() {
  return request('/categories')
}

export function createAdminCategory(payload) {
  return request('/categories', { method: 'POST', body: payload })
}

export function updateAdminCategory(id, payload) {
  return request('/categories/' + encodeURIComponent(id), { method: 'PUT', body: payload })
}

export function deleteAdminCategory(id) {
  return request('/categories/' + encodeURIComponent(id), { method: 'DELETE' })
}
