<script setup>
import { ref, computed, onMounted, provide } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdmin, clearAdmin } from '../../utils/adminAuth.js'
import { adminLogout, adminMe, setUnauthorizedHandler } from '../../api/admin.js'
import '../../styles/admin.css'

const route = useRoute()
const router = useRouter()

const admin = ref(getAdmin() || {})
const sidebarOpen = ref(false)

const menus = [
  { path: '/admin/dashboard', icon: '📊', label: '数据概览', key: 'dashboard' },
  { path: '/admin/items', icon: '🍰', label: '内容管理', key: 'items' },
  { path: '/admin/categories', icon: '🗂️', label: '分类管理', key: 'categories' }
]

const pageTitle = computed(() => (route.meta && route.meta.title) || '后台管理')
const displayName = computed(() => admin.value.nickname || admin.value.username || '管理员')
const initial = computed(() => (admin.value.username || 'A').charAt(0).toUpperCase())

// ---------------- 轻提示（子页面用 inject('adminToast') 调用） ----------------
const toast = ref({ text: '', type: 'ok' })
let toastTimer = null

function showToast(text, type = 'ok') {
  toast.value = { text, type }
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    toast.value = { text: '', type: 'ok' }
  }, 2400)
}

provide('adminToast', showToast)

// ---------------- 登录态 ----------------
onMounted(async () => {
  // 任何接口返回 401 都会走到这里：清完本地状态后回登录页
  setUnauthorizedHandler(() => {
    showToast('登录已过期，正在返回登录页…', 'error')
    setTimeout(() => router.replace('/admin/login'), 900)
  })

  try {
    // 刷新页面时用 token 换一次最新信息，顺便验证 token 是否还有效
    const me = await adminMe()
    admin.value = me
  } catch (e) {
    // 401 已经由上面的 handler 处理，其他错误（比如后端没开）只提示
    if (!e.unauthorized) showToast(e.message, 'error')
  }
})

async function handleLogout() {
  try {
    await adminLogout()
  } catch (e) {
    /* 后端没开也要允许本地退出 */
  }
  clearAdmin()
  router.replace('/admin/login')
}
</script>

<template>
  <div class="shell">
    <!-- 移动端抽屉遮罩 -->
    <div v-if="sidebarOpen" class="mask" @click="sidebarOpen = false"></div>

    <!-- 左侧边栏 -->
    <aside class="side" :class="{ open: sidebarOpen }">
      <div class="brand">
        <span class="bow">🎀</span>
        <span class="brand-text">
          <strong>Hello Kitty</strong>
          <small>管理后台</small>
        </span>
      </div>

      <nav class="menu">
        <router-link
          v-for="m in menus"
          :key="m.key"
          :to="m.path"
          class="item"
          active-class="on"
          @click="sidebarOpen = false"
        >
          <span class="ico">{{ m.icon }}</span>
          <span>{{ m.label }}</span>
        </router-link>
      </nav>

      <div class="side-foot">
        <div class="tip">🎀 数据改动会立即同步到首页</div>
        <router-link to="/home" class="link">🏠 返回前台首页</router-link>
        <button class="link danger" @click="handleLogout">🚪 退出登录</button>
      </div>
    </aside>

    <!-- 右侧主体 -->
    <div class="main">
      <header class="topbar">
        <button class="menu-btn" title="菜单" @click="sidebarOpen = !sidebarOpen">☰</button>
        <h1>{{ pageTitle }}</h1>
        <div class="who">
          <span class="avatar">{{ initial }}</span>
          <span class="name">{{ displayName }}</span>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>

    <!-- 轻提示 -->
    <Transition name="a-toast">
      <div v-if="toast.text" class="a-toast" :class="{ error: toast.type === 'error' }">
        {{ toast.text }}
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.shell {
  display: flex;
  min-height: 100vh;
  background: var(--a-bg);
}

/* ================= 侧边栏 ================= */
.side {
  flex: 0 0 218px;
  width: 218px;
  display: flex;
  flex-direction: column;
  padding: 18px 14px 16px;
  background: linear-gradient(180deg, #ff7fae 0%, #ff4f8b 62%, #f0346f 100%);
  color: #fff;
  position: sticky;
  top: 0;
  height: 100vh;
  box-sizing: border-box;
  z-index: 40;
}

.brand {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 4px 8px 18px;
}
.brand .bow {
  font-size: 24px;
  filter: drop-shadow(0 3px 5px rgba(140, 20, 70, 0.35));
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.brand-text strong {
  font-size: 14px;
  letter-spacing: 0.4px;
}
.brand-text small {
  font-size: 10.5px;
  color: #ffd9e8;
  letter-spacing: 2.4px;
}

.menu {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-top: 4px;
}
.item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 11px 14px;
  border-radius: 13px;
  color: #ffe6f1;
  font-size: 13.5px;
  text-decoration: none;
  transition: 0.18s;
  border: 1px solid transparent;
}
.item .ico {
  font-size: 14px;
  width: 18px;
  text-align: center;
}
.item:hover {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
}
.item.on {
  background: #fff;
  color: #ff4f8b;
  font-weight: bold;
  box-shadow: 0 8px 18px rgba(160, 25, 80, 0.28);
}

.side-foot {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 14px;
}
.tip {
  font-size: 10.5px;
  line-height: 1.5;
  color: #ffd3e5;
  padding: 8px 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.14);
  margin-bottom: 4px;
}
.link {
  display: block;
  width: 100%;
  text-align: left;
  padding: 8px 12px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #ffe6f1;
  font-size: 12.5px;
  font-family: inherit;
  text-decoration: none;
  cursor: pointer;
  transition: 0.18s;
  box-sizing: border-box;
}
.link:hover {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
}
.link.danger:hover {
  background: rgba(120, 0, 40, 0.28);
}

/* ================= 主体 ================= */
.main {
  flex: 1 1 auto;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 62px;
  padding: 0 22px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 2px solid var(--a-pink-line);
}
.topbar h1 {
  margin: 0;
  font-size: 15.5px;
  color: #ff4f8b;
  letter-spacing: 0.5px;
}
.menu-btn {
  display: none;
  width: 34px;
  height: 34px;
  border: 2px solid var(--a-pink-line);
  border-radius: 10px;
  background: #fff;
  color: #ff4f8b;
  font-size: 14px;
  cursor: pointer;
}
.who {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-size: 13px;
  font-weight: bold;
}
.name {
  font-size: 13px;
  color: #7a4b5e;
  font-weight: 600;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content {
  flex: 1 1 auto;
  padding: 20px 22px 34px;
  min-width: 0;
}

/* ================= 移动端 ================= */
.mask {
  display: none;
}

@media (max-width: 860px) {
  .side {
    position: fixed;
    left: 0;
    top: 0;
    transform: translateX(-100%);
    transition: transform 0.24s ease;
    box-shadow: 8px 0 30px rgba(140, 20, 70, 0.25);
  }
  .side.open {
    transform: translateX(0);
  }
  .mask {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 35;
    background: rgba(90, 45, 66, 0.28);
  }
  .menu-btn {
    display: block;
  }
  .content {
    padding: 14px 12px 26px;
  }
  .topbar {
    padding: 0 12px;
  }
  .name {
    display: none;
  }
}
</style>
