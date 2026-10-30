<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
  user: { type: Object, default: () => ({}) },
  keyword: { type: String, default: '' },
  active: { type: String, default: '首页' }
})

const emit = defineEmits(['update:keyword', 'select-tab', 'logout'])

const tabs = ['首页', '发现', '收藏', '我的']
const menuOpen = ref(false)

const displayName = computed(() => props.user.nickname || props.user.username || '小猫咪')
const initial = computed(() => (props.user.username || 'K').charAt(0).toUpperCase())

/** 去后台管理系统（没登录后台的话，后台登录页会拦下来） */
function goAdmin() {
  menuOpen.value = false
  router.push('/admin/login')
}

function onInput(e) {
  emit('update:keyword', e.target.value)
}

function clearKeyword() {
  emit('update:keyword', '')
}

function selectTab(tab) {
  menuOpen.value = false
  emit('select-tab', tab)
}

function handleLogout() {
  menuOpen.value = false
  emit('logout')
}
</script>

<template>
  <header class="nav">
    <div class="nav-inner">
      <!-- 左：Logo -->
      <div class="logo" @click="selectTab('首页')">
        <span class="bow">🎀</span>
        <span class="logo-text">
          <strong>Hello Kitty</strong>
          <em>Home</em>
        </span>
      </div>

      <!-- 中：导航菜单 -->
      <nav class="menu">
        <a
          v-for="tab in tabs"
          :key="tab"
          class="menu-item"
          :class="{ on: active === tab }"
          href="#"
          @click.prevent="selectTab(tab)"
        >
          {{ tab }}
        </a>
      </nav>

      <!-- 搜索栏 -->
      <div class="search">
        <span class="search-icon">🔍</span>
        <input
          :value="keyword"
          type="search"
          placeholder="搜一搜甜点、好物、心情…"
          @input="onInput"
        />
        <button v-if="keyword" class="search-clear" title="清空" @click="clearKeyword">✕</button>
      </div>

      <!-- 后台管理入口 -->
      <button class="admin-entry" title="进入后台管理系统" @click="goAdmin">
        <span class="ae-ico">🛠️</span>
        <span class="ae-text">后台管理</span>
      </button>

      <!-- 右：用户 -->
      <div class="user">
        <button class="avatar" :title="displayName" @click="menuOpen = !menuOpen">
          {{ initial }}
        </button>
        <span class="user-name">{{ displayName }}</span>

        <div v-if="menuOpen" class="mask" @click="menuOpen = false"></div>
        <div v-if="menuOpen" class="dropdown">
          <div class="dd-head">
            <span class="dd-avatar">{{ initial }}</span>
            <div class="dd-info">
              <strong>{{ displayName }}</strong>
              <small>@{{ user.username || 'kitty' }}</small>
            </div>
          </div>
          <button class="dd-item" @click="selectTab('我的')">👤 个人中心</button>
          <button class="dd-item" @click="selectTab('收藏')">💗 我的收藏</button>
          <div class="dd-line"></div>
          <button class="dd-item danger" @click="handleLogout">🚪 退出登录</button>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.nav {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  border-bottom: 2px solid #ffd9e8;
  box-shadow: 0 4px 18px rgba(255, 105, 160, 0.12);
}

.nav-inner {
  max-width: 1100px;
  margin: 0 auto;
  height: 66px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 18px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex: 0 0 auto;
  user-select: none;
}
.logo .bow {
  font-size: 22px;
  filter: drop-shadow(0 2px 3px rgba(255, 79, 139, 0.35));
}
.logo-text { display: flex; flex-direction: column; line-height: 1.1; }
.logo-text strong {
  color: #ff4f8b;
  font-size: 15px;
  letter-spacing: 0.5px;
}
.logo-text em {
  font-style: normal;
  font-size: 10px;
  color: #d79bb4;
  letter-spacing: 3px;
}

/* 菜单 */
.menu {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 0 0 auto;
}
.menu-item {
  position: relative;
  padding: 7px 12px;
  border-radius: 50px;
  font-size: 14px;
  color: #b96a8d;
  text-decoration: none;
  transition: 0.2s;
  white-space: nowrap;
}
.menu-item:hover { background: #fff0f6; color: #ff4f8b; }
.menu-item.on {
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 79, 139, 0.35);
}

/* 搜索栏 */
.search {
  position: relative;
  flex: 1 1 auto;
  max-width: 340px;
  margin: 0 auto;
  display: flex;
  align-items: center;
}
.search-icon {
  position: absolute;
  left: 14px;
  font-size: 14px;
  opacity: 0.75;
  pointer-events: none;
}
.search input {
  width: 100%;
  padding: 10px 36px 10px 38px;
  border: 2px solid #ffd3e5;
  border-radius: 50px;
  background: #fff8fb;
  color: #7a4b5e;
  font-size: 13px;
  outline: none;
  transition: 0.22s;
}
.search input::placeholder { color: #d9a8bd; }
.search input:focus {
  border-color: #ff4f8b;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(255, 79, 139, 0.14);
}
.search input::-webkit-search-cancel-button { display: none; }
.search-clear {
  position: absolute;
  right: 10px;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background: #ffd3e5;
  color: #b96a8d;
  font-size: 11px;
  cursor: pointer;
  line-height: 1;
}
.search-clear:hover { background: #ff4f8b; color: #fff; }

/* 后台管理入口 */
.admin-entry {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  flex: 0 0 auto;
  border: 2px solid #ffd3e5;
  border-radius: 50px;
  background: #fff8fb;
  color: #b96a8d;
  font-size: 12.5px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: 0.2s;
  white-space: nowrap;
}
.admin-entry:hover {
  border-color: #ff4f8b;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(255, 79, 139, 0.3);
}
.ae-ico { font-size: 12px; }

/* 用户 */
.user {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #ffc0da;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.2s;
}
.avatar:hover { transform: translateY(-2px); box-shadow: 0 6px 14px rgba(255, 79, 139, 0.4); }
.user-name {
  font-size: 13px;
  color: #ff4f8b;
  font-weight: bold;
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mask {
  position: fixed;
  inset: 0;
  z-index: 60;
}
.dropdown {
  position: absolute;
  top: 48px;
  right: 0;
  z-index: 61;
  width: 208px;
  background: #fff;
  border: 2px solid #ffd9e8;
  border-radius: 18px;
  padding: 10px;
  box-shadow: 0 14px 34px rgba(255, 105, 160, 0.28);
}
.dd-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 8px 10px;
}
.dd-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}
.dd-info { display: flex; flex-direction: column; line-height: 1.25; min-width: 0; }
.dd-info strong { color: #7a4b5e; font-size: 13px; }
.dd-info small { color: #c77a9c; font-size: 11px; }
.dd-item {
  width: 100%;
  text-align: left;
  padding: 9px 10px;
  border: none;
  background: transparent;
  border-radius: 10px;
  font-size: 13px;
  color: #7a4b5e;
  cursor: pointer;
  transition: 0.18s;
}
.dd-item:hover { background: #fff0f6; color: #ff4f8b; }
.dd-item.danger:hover { background: #ffeef4; color: #e0346b; }
.dd-line { height: 1px; background: #ffe3ee; margin: 6px 4px; }

/* 响应式 */
@media (max-width: 860px) {
  .menu { display: none; }
  .user-name { display: none; }
  .nav-inner { gap: 12px; }
}

@media (max-width: 620px) {
  .nav-inner {
    height: auto;
    padding: 10px 14px;
    flex-wrap: wrap;
    row-gap: 8px;
  }
  .search {
    order: 3;
    flex: 1 1 100%;
    max-width: none;
    margin: 0;
  }
  .admin-entry {
    margin-left: auto;
    padding: 7px 10px;
  }
  .ae-text { display: none; }
  .user { margin-left: 0; }
}
</style>
