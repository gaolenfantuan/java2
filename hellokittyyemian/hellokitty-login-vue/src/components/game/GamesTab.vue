<script setup>
/**
 * 小游戏大厅：把四个互不干扰的玩法装在一个页签里
 *   🍬 卡片收集   ☀️ 每日打卡   🧁 趣味答题   🎰 幸运抽奖
 *
 * 只依赖新增的 /api/game/** 接口，原有首页接口一个都没碰。
 */
import { ref, computed, onMounted } from 'vue'
import { getUser } from '../../utils/auth.js'
import { fetchGameMeta, fetchGameProfile } from '../../api/game.js'
import { showToast } from '../../utils/toast.js'
import CardCollectGame from './CardCollectGame.vue'
import CheckinGame from './CheckinGame.vue'
import QuizGame from './QuizGame.vue'
import LuckyDrawGame from './LuckyDrawGame.vue'

const user = ref(getUser() || {})
const userId = computed(() => Number(user.value.id) || 0)

const games = [
  { key: 'card', icon: '🍬', name: '卡片收集', desc: '点卡片收进图鉴' },
  { key: 'checkin', icon: '☀️', name: '每日打卡', desc: '每天来领积分' },
  { key: 'quiz', icon: '🧁', name: '趣味答题', desc: '甜点穿搭小知识' },
  { key: 'draw', icon: '🎰', name: '幸运抽奖', desc: '积分抽稀有卡' }
]
const active = ref('card')

const meta = ref({})
const profile = ref(null)
const loading = ref(true)
const error = ref('')

const points = computed(() => profile.value?.points ?? 0)
const collected = computed(() => profile.value?.collectedCount ?? 0)
const totalCards = computed(() => profile.value?.totalCards ?? 0)
const percent = computed(() => profile.value?.collectPercent ?? 0)
const streak = computed(() => profile.value?.streakDays ?? 0)

async function loadProfile() {
  try {
    profile.value = await fetchGameProfile(userId.value)
    error.value = ''
  } catch (e) {
    error.value = e.message
  }
}

async function loadAll() {
  loading.value = true
  try {
    const [m] = await Promise.all([fetchGameMeta(), loadProfile()])
    meta.value = m
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

/** 子游戏完成动作后回调：只需要刷新总览里的积分 / 进度 */
async function onChanged() {
  await loadProfile()
}

onMounted(() => {
  if (!userId.value) {
    error.value = '没拿到用户信息，请重新登录后再来玩~'
    loading.value = false
    return
  }
  loadAll()
})
</script>

<template>
  <div class="games">
    <!-- 顶部概览 -->
    <section class="g-hero">
      <div class="gh-glow"></div>
      <div class="gh-left">
        <p class="gh-kicker">HELLO KITTY MINI GAMES</p>
        <h2>🎮 小游戏乐园</h2>
        <p class="gh-desc">四个小游戏互相独立，玩哪个都不影响首页内容~</p>
        <div class="gh-stats">
          <div class="gs">
            <span class="gs-num">{{ points }}</span>
            <span class="gs-label">积分</span>
          </div>
          <div class="gs">
            <span class="gs-num">{{ collected }}<em>/{{ totalCards }}</em></span>
            <span class="gs-label">已收集</span>
          </div>
          <div class="gs">
            <span class="gs-num">{{ streak }}<em>天</em></span>
            <span class="gs-label">连续打卡</span>
          </div>
        </div>
      </div>

      <div class="gh-progress">
        <div class="gp-ring" :style="{ '--p': percent + '%' }">
          <span>{{ percent }}%</span>
        </div>
        <small>图鉴完成度</small>
      </div>
    </section>

    <!-- 玩法切换 -->
    <nav class="g-tabs">
      <button
        v-for="g in games"
        :key="g.key"
        class="g-tab"
        :class="{ on: active === g.key }"
        @click="active = g.key"
      >
        <span class="gt-icon">{{ g.icon }}</span>
        <span class="gt-text">
          <strong>{{ g.name }}</strong>
          <small>{{ g.desc }}</small>
        </span>
      </button>
    </nav>

    <!-- 出错 -->
    <section v-if="error" class="g-err">
      <div class="ge-emoji">😿</div>
      <h3>{{ error }}</h3>
      <button class="ge-btn" @click="loadAll">重新加载</button>
    </section>

    <section v-else-if="loading" class="g-err">
      <div class="ge-emoji">🎀</div>
      <h3>正在准备小游戏…</h3>
    </section>

    <!-- 玩法内容 -->
    <template v-else>
      <CardCollectGame v-if="active === 'card'" :user-id="userId" @changed="onChanged" />
      <CheckinGame v-else-if="active === 'checkin'" :user-id="userId" @changed="onChanged" />
      <QuizGame v-else-if="active === 'quiz'" :user-id="userId" @changed="onChanged" />
      <LuckyDrawGame
        v-else-if="active === 'draw'"
        :user-id="userId"
        :points="points"
        :meta="meta"
        @changed="onChanged"
      />
    </template>
  </div>
</template>

<style scoped>
.games { animation: fadeUp 0.42s ease backwards; }

/* 概览 */
.g-hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  flex-wrap: wrap;
  padding: 22px 26px;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(255, 143, 187, 0.96), rgba(255, 79, 139, 0.96));
  box-shadow: 0 16px 34px rgba(255, 79, 139, 0.3);
  overflow: hidden;
}
.gh-glow {
  position: absolute;
  right: -50px;
  top: -70px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
}
.gh-left { position: relative; z-index: 1; }
.gh-kicker {
  margin: 0 0 5px;
  font-size: 9.5px;
  letter-spacing: 2.4px;
  color: #ffe3ef;
}
.gh-left h2 { margin: 0 0 5px; font-size: 20px; color: #fff; letter-spacing: 0.5px; }
.gh-desc { margin: 0 0 14px; font-size: 12px; color: #ffeaf3; }
.gh-stats { display: flex; gap: 9px; flex-wrap: wrap; }
.gs {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1px;
  min-width: 72px;
  padding: 8px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.4);
}
.gs-num { font-size: 16px; font-weight: bold; color: #fff; }
.gs-num em { font-style: normal; font-size: 10px; opacity: 0.85; }
.gs-label { font-size: 10px; color: #ffe3ef; }

.gh-progress {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}
.gp-ring {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: conic-gradient(#fff var(--p), rgba(255, 255, 255, 0.28) 0);
}
.gp-ring span {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: bold;
  color: #fff;
}
.gh-progress small { font-size: 10px; color: #ffe3ef; }

/* 玩法切换 */
.g-tabs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 9px;
  margin: 16px 0;
}
.g-tab {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 11px 12px;
  border-radius: 18px;
  border: 2px solid #ffd9e8;
  background: rgba(255, 255, 255, 0.86);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: 0.22s;
}
.g-tab:hover { border-color: #ffb0cf; transform: translateY(-2px); }
.g-tab.on {
  background: linear-gradient(135deg, #fff0f6, #ffe0ee);
  border-color: #ff8ab8;
  box-shadow: 0 8px 18px rgba(255, 79, 139, 0.18);
}
.gt-icon { font-size: 20px; }
.gt-text { display: flex; flex-direction: column; gap: 1px; min-width: 0; }
.gt-text strong { font-size: 12.5px; color: #7a4b5e; white-space: nowrap; }
.gt-text small { font-size: 10px; color: #c98fae; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.g-tab.on .gt-text strong { color: #ff4f8b; }

/* 错误 / 加载 */
.g-err {
  background: rgba(255, 255, 255, 0.88);
  border: 2px dashed #ffc0da;
  border-radius: 22px;
  padding: 38px 22px;
  text-align: center;
}
.ge-emoji { font-size: 36px; margin-bottom: 8px; }
.g-err h3 { margin: 0 0 10px; font-size: 14px; color: #ff4f8b; }
.ge-btn {
  padding: 8px 20px;
  border: 2px solid #ff4f8b;
  border-radius: 50px;
  background: #fff;
  color: #ff4f8b;
  font-size: 12.5px;
  font-weight: bold;
  cursor: pointer;
  font-family: inherit;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: none; }
}

@media (max-width: 720px) {
  .g-tabs { grid-template-columns: repeat(2, 1fr); }
  .gt-text small { display: none; }
  .g-hero { padding: 20px; }
  .gh-progress { display: none; }
}
</style>
