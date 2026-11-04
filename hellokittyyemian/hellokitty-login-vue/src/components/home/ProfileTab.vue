<script setup>
/**
 * 「我的」页（新增）：用户信息 + 数据概览 + 快捷入口 + 退出登录
 * 复用已有 auth 存取、收藏工具和后端 /api/home/stats，后端零改动。
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import KittyAvatar from '../KittyAvatar.vue'
import { getUser, clearUser } from '../../utils/auth.js'
import { favoriteCount } from '../../utils/favorites.js'
import { fetchStats } from '../../api/home.js'
// 新增：小游戏总览（收集进度 / 连续打卡 / 积分），只读接口，不影响原有数据
import { fetchGameProfile } from '../../api/game.js'
import { showToast } from '../../utils/toast.js'

const emit = defineEmits(['jump'])
const router = useRouter()

const user = ref(getUser() || {})
const displayName = computed(() => user.value.nickname || user.value.username || '小猫咪')
const initial = computed(() => (user.value.username || 'K').charAt(0).toUpperCase())
const loginWay = computed(() => (user.value.rememberMe ? '已记住我（长期有效）' : '本次登录（关浏览器失效）'))

const favCount = ref(favoriteCount())
const stats = ref({ itemTotal: 0, categoryTotal: 0, likeTotal: 0 })

// 新增：小游戏数据（收集进度 / 连续打卡 / 积分）
const game = ref(null)

const menu = [
  { icon: '🧭', label: '去发现页', desc: '看看热门榜和最新上架', act: '发现' },
  { icon: '💗', label: '我的收藏', desc: '翻翻收藏的小宝贝', act: '收藏' },
  { icon: '🎮', label: '小游戏乐园', desc: '收集卡片 / 打卡 / 答题 / 抽奖', act: '小游戏' },
  { icon: '🛠️', label: '后台管理', desc: '内容与分类管理入口', act: 'admin' }
]

function onMenu(item) {
  if (item.act === 'admin') {
    router.push('/admin/login')
    return
  }
  emit('jump', item.act)
}

function handleLogout() {
  clearUser()
  showToast.success('已安全退出，下次见~ 🎀')
  setTimeout(() => router.replace('/login'), 700)
}

onMounted(async () => {
  try {
    stats.value = await fetchStats()
  } catch (e) {
    /* 统计失败不打扰主流程 */
  }
  // 新增：小游戏总览，失败也只是不显示这张卡，不影响上面的内容
  try {
    game.value = await fetchGameProfile(Number(user.value.id) || 0)
  } catch (e) {
    game.value = null
  }
})
</script>

<template>
  <div class="me">
    <!-- 用户信息卡 -->
    <section class="me-card">
      <div class="me-left">
        <div class="me-avatar"><KittyAvatar /></div>
        <div class="me-info">
          <h2>{{ displayName }} <span class="me-wave">♡</span></h2>
          <p class="me-uid">@{{ user.username || 'kitty' }} · ID {{ user.id || '-' }}</p>
          <span class="me-way">{{ loginWay }}</span>
        </div>
      </div>

      <div class="me-stats">
        <div class="mst">
          <span class="mst-num">{{ favCount }}</span>
          <span class="mst-label">收藏</span>
        </div>
        <div class="mst">
          <span class="mst-num">{{ stats.itemTotal }}</span>
          <span class="mst-label">内容总数</span>
        </div>
        <div class="mst">
          <span class="mst-num">{{ stats.likeTotal }}</span>
          <span class="mst-label">累计点赞</span>
        </div>
      </div>
    </section>

    <!-- 新增：小游戏总览（收集进度 / 连续打卡 / 积分） -->
    <section v-if="game" class="game-card" @click="emit('jump', '小游戏')">
      <div class="gc-head">
        <h3>🎮 我的小游戏</h3>
        <span class="gc-more">去玩 ›</span>
      </div>
      <div class="gc-stats">
        <div class="gst">
          <span class="gst-num">{{ game.collectedCount }}<em>/{{ game.totalCards }}</em></span>
          <span class="gst-label">已收集卡片</span>
        </div>
        <div class="gst">
          <span class="gst-num">{{ game.streakDays }}<em>天</em></span>
          <span class="gst-label">连续打卡</span>
        </div>
        <div class="gst">
          <span class="gst-num">{{ game.points }}</span>
          <span class="gst-label">可用积分</span>
        </div>
      </div>
      <div class="gc-bar"><i :style="{ width: game.collectPercent + '%' }"></i></div>
      <p class="gc-tip">
        图鉴完成度 {{ game.collectPercent }}%
        <template v-if="game.todayChecked"> · 今天已打卡 ✓</template>
        <template v-else> · 今天还没打卡，去点一下~</template>
      </p>
    </section>

    <!-- 快捷入口 -->
    <section class="menu-card">
      <button v-for="m in menu" :key="m.label" class="menu-row" @click="onMenu(m)">
        <span class="mr-icon">{{ m.icon }}</span>
        <span class="mr-text">
          <strong>{{ m.label }}</strong>
          <small>{{ m.desc }}</small>
        </span>
        <span class="mr-arrow">›</span>
      </button>

      <div class="menu-line"></div>

      <button class="menu-row danger" @click="handleLogout">
        <span class="mr-icon">🚪</span>
        <span class="mr-text">
          <strong>退出登录</strong>
          <small>清除本地登录状态并回到登录页</small>
        </span>
        <span class="mr-arrow">›</span>
      </button>
    </section>
  </div>
</template>

<style scoped>
.me { animation: fadeUp 0.45s ease backwards; }

/* 用户信息卡 */
.me-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  padding: 24px 26px;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(255, 143, 187, 0.95), rgba(255, 79, 139, 0.95));
  box-shadow: 0 16px 34px rgba(255, 79, 139, 0.32);
  position: relative;
  overflow: hidden;
}
.me-card::after {
  content: '';
  position: absolute;
  right: -50px;
  top: -60px;
  width: 190px;
  height: 190px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
}
.me-left { display: flex; align-items: center; gap: 16px; position: relative; z-index: 1; }
.me-avatar { transform: scale(0.82); transform-origin: left center; }
.me-info h2 { margin: 0 0 3px; font-size: 19px; color: #fff; letter-spacing: 0.5px; }
.me-wave { font-size: 14px; opacity: 0.9; }
.me-uid { margin: 0 0 7px; font-size: 12px; color: #ffe3ef; }
.me-way {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.4);
  font-size: 11px;
  color: #fff;
}
.me-stats { display: flex; gap: 10px; position: relative; z-index: 1; }
.mst {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  min-width: 76px;
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.4);
  transition: transform 0.2s ease;
}
.mst:hover { transform: translateY(-3px); }
.mst-num { font-size: 18px; font-weight: bold; color: #fff; }
.mst-label { font-size: 11px; color: #ffe3ef; }

/* 新增：小游戏总览卡 */
.game-card {
  margin-top: 18px;
  padding: 16px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #ffd9e8;
  box-shadow: 0 10px 24px rgba(255, 105, 160, 0.12);
  cursor: pointer;
  transition: 0.22s;
}
.game-card:hover { border-color: #ffb0cf; transform: translateY(-2px); }
.gc-head { display: flex; align-items: baseline; justify-content: space-between; margin-bottom: 12px; }
.gc-head h3 { margin: 0; font-size: 14px; color: #ff4f8b; }
.gc-more { font-size: 11.5px; color: #c98fae; }
.gc-stats { display: flex; gap: 8px; }
.gst {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  padding: 9px 6px;
  border-radius: 14px;
  background: #fff6fa;
  border: 1px solid #ffe3ee;
}
.gst-num { font-size: 16px; font-weight: bold; color: #ff4f8b; }
.gst-num em { font-style: normal; font-size: 10px; color: #d79bb4; }
.gst-label { font-size: 10px; color: #b98ba0; }
.gc-bar {
  height: 7px;
  margin: 12px 0 7px;
  border-radius: 50px;
  background: #ffe3ee;
  overflow: hidden;
}
.gc-bar i {
  display: block;
  height: 100%;
  border-radius: 50px;
  background: linear-gradient(90deg, #ff9dc0, #ff4f8b);
  transition: width 0.5s ease;
}
.gc-tip { margin: 0; font-size: 11px; color: #b98ba0; }

/* 快捷入口 */
.menu-card {
  margin-top: 18px;
  background: rgba(255, 255, 255, 0.88);
  border: 2px solid #ffd9e8;
  border-radius: 24px;
  padding: 10px;
  box-shadow: 0 10px 24px rgba(255, 105, 160, 0.12);
}
.menu-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 13px 12px;
  border: none;
  background: transparent;
  border-radius: 16px;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition: 0.2s;
}
.menu-row:hover { background: #fff0f6; transform: translateX(4px); }
.mr-icon {
  flex: 0 0 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  border-radius: 14px;
  background: #ffe9f3;
}
.mr-text { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.mr-text strong { font-size: 14px; color: #7a4b5e; }
.mr-text small { font-size: 11.5px; color: #b98ba0; }
.mr-arrow { font-size: 20px; color: #e8b7cc; }
.menu-line { height: 1px; background: #ffe3ee; margin: 6px 12px; }
.menu-row.danger .mr-text strong { color: #e0346b; }
.menu-row.danger .mr-icon { background: #ffeef4; }

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: none; }
}

@media (max-width: 720px) {
  .me-card { flex-direction: column; align-items: flex-start; padding: 20px; }
  .me-stats { width: 100%; }
  .mst { flex: 1; }
}
</style>
