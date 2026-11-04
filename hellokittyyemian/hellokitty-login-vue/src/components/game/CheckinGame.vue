<script setup>
/**
 * 玩法二：每日打卡
 * 一天只能打一次（后端唯一索引兜底），打卡成功播放 Hello Kitty 动画。
 */
import { ref, computed, onMounted } from 'vue'
import { fetchGameProfile, doCheckin } from '../../api/game.js'
import { showToast } from '../../utils/toast.js'
import AchievementPopup from './AchievementPopup.vue'

const props = defineProps({
  userId: { type: Number, default: 0 }
})
const emit = defineEmits(['changed'])

const profile = ref(null)
const loading = ref(true)
const loadError = ref('')
const busy = ref(false)

const playing = ref(false)
const gainText = ref('')

const popupShow = ref(false)
const popupItems = ref([])

const streak = computed(() => profile.value?.streakDays || 0)
const todayChecked = computed(() => !!profile.value?.todayChecked)
const totalDays = computed(() => profile.value?.totalCheckinDays || 0)

/** 周进度：连续 7 天为一轮，满 7 天额外奖励 */
const weekFilled = computed(() => {
  const s = streak.value
  if (s <= 0) return 0
  return s % 7 === 0 ? 7 : s % 7
})

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    profile.value = await fetchGameProfile(props.userId)
  } catch (e) {
    loadError.value = e.message
  } finally {
    loading.value = false
  }
}

async function onCheckin() {
  if (busy.value) return
  if (todayChecked.value) {
    showToast.info('今天已经打过卡啦，明天记得再来哦~')
    return
  }
  busy.value = true
  try {
    const res = await doCheckin(props.userId)
    profile.value = {
      ...profile.value,
      streakDays: res.streakDays,
      todayChecked: true,
      totalCheckinDays: res.totalCheckinDays,
      points: res.points
    }
    if (res.alreadyChecked) {
      showToast.info(res.message)
    } else {
      gainText.value = `打卡成功  +${res.pointsAwarded} 分`
      playing.value = true
      setTimeout(() => (playing.value = false), 2200)
      showToast.success(res.message)
      if (res.newAchievements && res.newAchievements.length) {
        popupItems.value = res.newAchievements
        setTimeout(() => (popupShow.value = true), 1200)
      }
    }
    emit('changed')
  } catch (e) {
    showToast.error(e.message)
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="checkin">
    <section v-if="loadError" class="g-empty">
      <div class="ge-emoji">😿</div>
      <h3>{{ loadError }}</h3>
      <button class="ge-btn" @click="load">重新加载</button>
    </section>

    <section v-else-if="loading" class="g-empty">
      <div class="ge-emoji">🎀</div>
      <h3>正在翻日历…</h3>
    </section>

    <template v-else>
      <!-- 主卡片 -->
      <section class="ck-card">
        <div class="ck-deco">
          <span v-for="i in 6" :key="i" :style="{ '--i': i }">💗</span>
        </div>

        <div class="ck-top">
          <div class="ck-left">
            <p class="ck-kicker">DAILY CHECK-IN</p>
            <h2>连续打卡 <em>{{ streak }}</em> 天</h2>
            <p class="ck-sub">
              累计打卡 {{ totalDays }} 天 · 每天 +10 分，连满 7 天额外 +20 分
            </p>
          </div>
          <div class="ck-seal">
            <span class="seal-kitty">🐱</span>
            <span class="seal-text">{{ todayChecked ? '今日已打卡' : '今天还没打卡' }}</span>
          </div>
        </div>

        <!-- 一周进度 -->
        <div class="ck-week">
          <div
            v-for="i in 7"
            :key="i"
            class="week-dot"
            :class="{ on: i <= weekFilled }"
          >
            <span class="wd-heart">{{ i <= weekFilled ? '💗' : '🤍' }}</span>
            <small>第{{ i }}天</small>
          </div>
        </div>

        <button class="ck-btn" :class="{ done: todayChecked }" :disabled="busy" @click="onCheckin">
          <template v-if="todayChecked">🌙 今天已打卡，明天见</template>
          <template v-else-if="busy">打卡中…</template>
          <template v-else>☀️ 立即打卡</template>
        </button>
      </section>

      <!-- 小提示 -->
      <section class="ck-tips">
        <div class="tip-row"><span>🎁</span><p>打卡积分可以用来抽卡片，攒够 20 分就能抽一次。</p></div>
        <div class="tip-row"><span>🔥</span><p>连续天数断了会从 1 重新算，成就里的「最长连续」不会回退。</p></div>
      </section>
    </template>

    <!-- Hello Kitty 打卡动画 -->
    <transition name="kitty-fade">
      <div v-if="playing" class="kitty-mask">
        <div class="kitty-stage">
          <span class="km-heart h1">💗</span>
          <span class="km-heart h2">💖</span>
          <span class="km-heart h3">🎀</span>
          <span class="km-heart h4">✨</span>
          <div class="km-kitty">🐱</div>
          <div class="km-bow">🎀</div>
          <p class="km-text">{{ gainText }}</p>
          <p class="km-sub">喵~ 今天也是元气满满的一天</p>
        </div>
      </div>
    </transition>

    <AchievementPopup :show="popupShow" :items="popupItems" @close="popupShow = false" />
  </div>
</template>

<style scoped>
.checkin { animation: fadeUp 0.4s ease backwards; }

/* 主卡 */
.ck-card {
  position: relative;
  padding: 24px 24px 22px;
  border-radius: 26px;
  background: linear-gradient(140deg, #fff7fb 0%, #ffeaf4 60%, #ffe0ef 100%);
  border: 2px solid #ffd0e3;
  box-shadow: 0 16px 34px rgba(255, 105, 160, 0.18);
  overflow: hidden;
}
.ck-deco { position: absolute; inset: 0; pointer-events: none; }
.ck-deco span {
  position: absolute;
  right: calc(4% + var(--i) * 6%);
  top: calc(10% + (var(--i) % 3) * 22%);
  font-size: 12px;
  opacity: 0.45;
  animation: floaty 3.6s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.25s);
}

.ck-top {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
}
.ck-kicker {
  margin: 0 0 6px;
  font-size: 10px;
  letter-spacing: 2.5px;
  color: #d98bb0;
}
.ck-left h2 {
  margin: 0 0 6px;
  font-size: 20px;
  color: #7a4b5e;
  letter-spacing: 0.5px;
}
.ck-left h2 em {
  font-style: normal;
  font-size: 30px;
  color: #ff4f8b;
  margin: 0 2px;
}
.ck-sub { margin: 0; font-size: 11.5px; color: #b98ba0; }

.ck-seal {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
  border: 2px dashed #ffbcd8;
}
.seal-kitty { font-size: 26px; animation: kittyWiggle 2.6s ease-in-out infinite; }
.seal-text { font-size: 10.5px; color: #d1689a; white-space: nowrap; }

/* 一周进度 */
.ck-week {
  display: flex;
  justify-content: space-between;
  gap: 6px;
  margin: 20px 0 18px;
  padding: 12px 10px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid #ffe0ee;
}
.week-dot {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 4px 0;
  border-radius: 12px;
  transition: 0.25s;
}
.week-dot.on { background: #fff0f6; }
.wd-heart { font-size: 16px; }
.week-dot small { font-size: 9.5px; color: #c98fae; }

/* 打卡按钮 */
.ck-btn {
  width: 100%;
  padding: 14px 18px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-size: 14.5px;
  font-weight: bold;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 12px 24px rgba(255, 79, 139, 0.34);
  transition: 0.22s;
}
.ck-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 16px 30px rgba(255, 79, 139, 0.44); }
.ck-btn:disabled { cursor: default; }
.ck-btn.done {
  background: #fff;
  color: #d1689a;
  border: 2px solid #ffd0e3;
  box-shadow: none;
}

/* 提示 */
.ck-tips {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.86);
  border: 2px solid #ffd9e8;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.tip-row { display: flex; align-items: flex-start; gap: 8px; }
.tip-row span { font-size: 13px; }
.tip-row p { margin: 0; font-size: 11.5px; color: #b98ba0; line-height: 1.6; }

/* Kitty 动画 */
.kitty-mask {
  position: fixed;
  inset: 0;
  z-index: 180;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 214, 232, 0.55);
  backdrop-filter: blur(4px);
}
.kitty-stage { position: relative; text-align: center; }
.km-kitty {
  font-size: 78px;
  animation: kittyJump 0.9s cubic-bezier(0.3, 1.6, 0.5, 1) infinite;
  filter: drop-shadow(0 12px 20px rgba(214, 60, 118, 0.35));
}
.km-bow {
  position: absolute;
  left: 50%;
  top: 2px;
  transform: translateX(-50%) rotate(-12deg);
  font-size: 26px;
  animation: bowSwing 1.1s ease-in-out infinite;
}
.km-text {
  margin: 12px 0 4px;
  font-size: 19px;
  font-weight: bold;
  color: #ff4f8b;
  text-shadow: 0 2px 8px rgba(255, 255, 255, 0.9);
}
.km-sub { margin: 0; font-size: 12px; color: #b96a8d; }
.km-heart {
  position: absolute;
  font-size: 20px;
  animation: heartFly 1.6s ease-out infinite;
}
.km-heart.h1 { left: -58px; top: 10px; animation-delay: 0s; }
.km-heart.h2 { right: -54px; top: 22px; animation-delay: 0.3s; }
.km-heart.h3 { left: -44px; bottom: 18px; animation-delay: 0.55s; }
.km-heart.h4 { right: -46px; bottom: 8px; animation-delay: 0.75s; }

@keyframes kittyJump {
  0%, 100% { transform: translateY(0) scale(1); }
  40% { transform: translateY(-16px) scale(1.06); }
  70% { transform: translateY(-4px) scale(1.02); }
}
@keyframes bowSwing {
  0%, 100% { transform: translateX(-50%) rotate(-12deg); }
  50% { transform: translateX(-50%) rotate(10deg); }
}
@keyframes heartFly {
  0% { opacity: 0; transform: translateY(10px) scale(0.6); }
  30% { opacity: 1; }
  100% { opacity: 0; transform: translateY(-46px) scale(1.1); }
}
@keyframes kittyWiggle {
  0%, 100% { transform: rotate(-4deg); }
  50% { transform: rotate(4deg); }
}
@keyframes floaty {
  0%, 100% { transform: translateY(0); opacity: 0.3; }
  50% { transform: translateY(-8px); opacity: 0.7; }
}

.kitty-fade-enter-active, .kitty-fade-leave-active { transition: opacity 0.28s ease; }
.kitty-fade-enter-from, .kitty-fade-leave-to { opacity: 0; }

/* 空态 */
.g-empty {
  background: rgba(255, 255, 255, 0.88);
  border: 2px dashed #ffc0da;
  border-radius: 22px;
  padding: 38px 22px;
  text-align: center;
}
.ge-emoji { font-size: 36px; margin-bottom: 8px; }
.g-empty h3 { margin: 0 0 10px; font-size: 14px; color: #ff4f8b; }
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

@media (max-width: 620px) {
  .ck-card { padding: 20px 16px; }
  .ck-seal { width: 100%; flex-direction: row; justify-content: center; }
  .ck-week { gap: 2px; padding: 10px 4px; }
  .week-dot small { font-size: 8.5px; }
}
</style>
