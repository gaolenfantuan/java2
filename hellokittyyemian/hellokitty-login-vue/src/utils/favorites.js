/**
 * 本地收藏（纯前端能力，新增，不动后端任何接口）
 *
 * 数据结构：[{ id: 内容id, ts: 收藏时间戳 }]，按 ts 天然可倒序展示"最近收藏"。
 * 存储：localStorage（key: kitty_favs），登录与否都可用。
 */

const KEY = 'kitty_favs'

function read() {
  try {
    const raw = localStorage.getItem(KEY)
    const list = raw ? JSON.parse(raw) : []
    return Array.isArray(list) ? list : []
  } catch (e) {
    return []
  }
}

function write(list) {
  localStorage.setItem(KEY, JSON.stringify(list))
}

/** 收藏 id 列表（按收藏时间倒序） */
export function getFavorites() {
  return [...read()].sort((a, b) => b.ts - a.ts)
}

/** 收藏的 id 集合，方便快速判断 */
export function getFavoriteIds() {
  return new Set(read().map((f) => f.id))
}

/** 是否已收藏 */
export function isFavorite(id) {
  return read().some((f) => f.id === id)
}

/** 收藏数量 */
export function favoriteCount() {
  return read().length
}

/**
 * 切换收藏状态
 * @returns {boolean} true = 现在已收藏，false = 已取消
 */
export function toggleFavorite(id) {
  const list = read()
  const idx = list.findIndex((f) => f.id === id)
  if (idx >= 0) {
    list.splice(idx, 1)
    write(list)
    return false
  }
  list.push({ id, ts: Date.now() })
  write(list)
  return true
}

/** 取消收藏 */
export function removeFavorite(id) {
  write(read().filter((f) => f.id !== id))
}
