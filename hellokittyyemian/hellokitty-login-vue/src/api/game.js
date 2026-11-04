/**
 * 四个小游戏的接口封装（新增模块，和 api/home.js 完全独立）
 *
 * 后端统一返回 { code, message, data }：
 *   code === 200 时返回 data，否则抛 Error（message 就是后端的中文提示）
 *
 * 说明：前台用户没有独立令牌，userId 由前端从登录信息里带上来（后端会校验用户是否存在）。
 */

const BASE_URL = '/api/game'

async function request(path, { method = 'GET', params, body } = {}) {
  let url = BASE_URL + path
  if (params) {
    const query = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') query.append(k, v)
    })
    if (query.toString()) url += '?' + query.toString()
  }

  let res
  try {
    res = await fetch(url, {
      method,
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined
    })
  } catch (e) {
    throw new Error('连不上服务器，请确认后端已经启动（192.168.68.53:8080）')
  }

  let data
  try {
    data = await res.json()
  } catch (e) {
    throw new Error('服务器返回的数据格式不对')
  }

  if (data.code !== 200) {
    throw new Error(data.message || '请求失败')
  }
  return data.data
}

/** 游戏常量：抽奖消耗、每日答题上限、各稀有度权重 */
export function fetchGameMeta() {
  return request('/meta')
}

/** 游戏总览：积分 / 收集进度 / 连续打卡 / 答题统计 / 成就 */
export function fetchGameProfile(userId) {
  return request('/profile', { params: { userId } })
}

/** 卡片图鉴（含当前用户的收集、收藏状态） */
export function fetchCards(userId) {
  return request('/cards', { params: { userId } })
}

/** 收集一张卡片 */
export function collectCard(userId, cardId) {
  return request('/collect', { method: 'POST', body: { userId, cardId } })
}

/** 收藏 / 取消收藏（favorite 不传则切换） */
export function toggleCardFavorite(userId, cardId, favorite) {
  return request('/favorite', { method: 'POST', body: { userId, cardId, favorite } })
}

/** 每日打卡 */
export function doCheckin(userId) {
  return request('/checkin', { method: 'POST', body: { userId } })
}

/** 随机取题（不含答案） */
export function fetchQuizQuestions(userId, limit = 5) {
  return request('/quiz', { params: { userId, limit } })
}

/** 提交答案 */
export function submitQuizAnswer(userId, questionId, choice) {
  return request('/quiz/answer', { method: 'POST', body: { userId, questionId, choice } })
}

/** 幸运抽奖 */
export function drawLuckyCard(userId) {
  return request('/draw', { method: 'POST', body: { userId } })
}
