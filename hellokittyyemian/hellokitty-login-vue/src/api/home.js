/**
 * 首页接口封装
 * Vite 已配置代理：/api -> http://localhost:8080
 *
 * 后端统一返回 { code, message, data }：
 *   code === 200 时返回 data，否则抛 Error（message 就是后端的中文提示）
 */

const BASE_URL = '/api/home'

async function request(path, params) {
  const query = new URLSearchParams()
  Object.entries(params || {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') query.append(k, v)
  })
  const url = BASE_URL + path + (query.toString() ? '?' + query.toString() : '')

  let res
  try {
    res = await fetch(url)
  } catch (e) {
    // 网络层失败，通常是后端没启动
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

/**
 * 分类树：一级分类 + children（二级分类），带每个分类的内容数量
 * 返回 [{ id, name, icon, count, children: [...] }]
 */
export function fetchCategories() {
  return request('/categories')
}

/**
 * 搜索 / 列表
 * @param {Object} p
 * @param {String} p.keyword     关键词（可空格分隔多个词）
 * @param {Number} p.categoryId  分类ID（一级分类会自动包含其下二级分类）
 * @param {String} p.sort        default | hot | new
 * @param {Number} p.page        页码，从 1 开始
 * @param {Number} p.size        每页条数
 * 返回 { list, total, page, size, pages, hasNext }
 */
export function fetchItems(p = {}) {
  return request('/items', {
    keyword: p.keyword,
    categoryId: p.categoryId,
    includeChildren: p.includeChildren,
    sort: p.sort,
    page: p.page,
    size: p.size
  })
}

/** 热门推荐（按点赞数） */
export function fetchHot(limit = 6) {
  return request('/hot', { limit })
}

/** 内容详情（后端会把浏览量 +1） */
export function fetchItemDetail(id) {
  return request('/items/' + encodeURIComponent(id))
}

/** 首页统计：{ itemTotal, categoryTotal, likeTotal, hotTotal } */
export function fetchStats() {
  return request('/stats')
}
