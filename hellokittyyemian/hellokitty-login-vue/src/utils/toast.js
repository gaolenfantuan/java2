/**
 * 全局消息提示（新增能力，不替换各页面已有的局部 toast）
 *
 * 纯 DOM 实现，零依赖：任何 .js / .vue 文件里 import 后直接调用即可，
 * 不需要在组件里写模板和状态。
 *
 * 用法：
 *   import { showToast } from '@/utils/toast.js'
 *   showToast('保存成功', 'success')
 *   showToast.success('登录成功')
 *   showToast.error('连不上服务器')
 *   showToast.warning('请先选择分类')
 *   showToast.info('🎀 欢迎回来~')
 *
 * 样式在 styles/beauty.css 里（kitty-toast 系列类名）。
 */

const ICONS = {
  success: '✅',
  error: '😿',
  warning: '⚠️',
  info: '🎀'
}

/** 顶部消息容器（懒创建，整个页面共用一个） */
let container = null

function ensureContainer() {
  if (container && document.body.contains(container)) return container
  container = document.createElement('div')
  container.className = 'kitty-toasts'
  document.body.appendChild(container)
  return container
}

/**
 * 弹出一条全局消息
 * @param {string} message 消息文本
 * @param {'success'|'error'|'warning'|'info'} type 类型（决定图标和颜色）
 * @param {number} duration 显示时长（毫秒），默认 2400
 */
export function showToast(message, type = 'info', duration = 2400) {
  if (typeof document === 'undefined') return // 防御 SSR 场景

  const box = ensureContainer()
  const el = document.createElement('div')
  el.className = `kitty-toast kitty-toast--${ICONS[type] ? type : 'info'}`
  el.setAttribute('role', 'status')

  // 用 textContent 组装，避免把调用方文本当 HTML 解析
  const icon = document.createElement('span')
  icon.className = 'kitty-toast__icon'
  icon.textContent = ICONS[type] || ICONS.info
  const text = document.createElement('span')
  text.className = 'kitty-toast__text'
  text.textContent = String(message)
  el.appendChild(icon)
  el.appendChild(text)

  box.appendChild(el)

  // 下一帧再加 is-show，保证过渡动画生效
  requestAnimationFrame(() => el.classList.add('is-show'))

  setTimeout(() => {
    el.classList.remove('is-show')
    el.addEventListener('transitionend', () => el.remove(), { once: true })
    // transitionend 偶发不触发的兜底
    setTimeout(() => el.remove(), 600)
  }, duration)
}

// 快捷方法
showToast.success = (msg, duration) => showToast(msg, 'success', duration)
showToast.error = (msg, duration) => showToast(msg, 'error', duration)
showToast.warning = (msg, duration) => showToast(msg, 'warning', duration)
showToast.info = (msg, duration) => showToast(msg, 'info', duration)

export default showToast
