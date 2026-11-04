<script setup>
/**
 * 成就解锁弹窗（卡片收集 / 打卡 / 答题 / 抽奖共用）
 * 后端在动作结果里返回 newAchievements，非空时由外部把 show 置为 true。
 */
import { computed } from 'vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
  show: { type: Boolean, default: false }
})

const emit = defineEmits(['close'])

const list = computed(() => props.items || [])
const title = computed(() => (list.value.length > 1 ? `一次解锁 ${list.value.length} 个成就！` : (list.value[0]?.title || '')))
</script>

<template>
  <transition name="ach-fade">
    <div v-if="show && list.length" class="ach-mask" @click.self="emit('close')">
      <div class="ach-box">
        <div class="ach-glow"></div>
        <div class="ach-sparkles">
          <span v-for="i in 8" :key="i" :style="{ '--i': i }">✨</span>
        </div>

        <p class="ach-kicker">🎉 ACHIEVEMENT UNLOCKED</p>
        <div class="ach-badge">{{ list[0].emoji }}</div>
        <h2>{{ title }}</h2>

        <ul class="ach-list">
          <li v-for="a in list" :key="a.code">
            <span class="ai">{{ a.emoji }}</span>
            <span class="at">
              <strong>{{ a.title }}</strong>
              <small>{{ a.desc }}</small>
            </span>
          </li>
        </ul>

        <button class="ach-btn" @click="emit('close')">收下这份甜甜的成就 🎀</button>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.ach-mask {
  position: fixed;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(122, 75, 94, 0.42);
  backdrop-filter: blur(3px);
}

.ach-box {
  position: relative;
  width: min(420px, 100%);
  padding: 30px 26px 24px;
  border-radius: 28px;
  background: linear-gradient(160deg, #fff8fb 0%, #ffeef6 100%);
  border: 3px solid #ffd0e3;
  box-shadow: 0 24px 60px rgba(214, 60, 118, 0.35);
  text-align: center;
  overflow: hidden;
  animation: achPop 0.5s cubic-bezier(0.2, 1.4, 0.4, 1) both;
}

.ach-glow {
  position: absolute;
  left: 50%;
  top: -90px;
  width: 260px;
  height: 260px;
  transform: translateX(-50%);
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 187, 214, 0.9), rgba(255, 187, 214, 0) 70%);
  pointer-events: none;
}

.ach-sparkles { position: absolute; inset: 0; pointer-events: none; }
.ach-sparkles span {
  position: absolute;
  left: calc(8% + var(--i) * 10.5%);
  top: calc(6% + (var(--i) % 3) * 26%);
  font-size: 13px;
  opacity: 0.75;
  animation: sparkle 2.4s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.18s);
}

.ach-kicker {
  position: relative;
  margin: 0 0 12px;
  font-size: 10.5px;
  letter-spacing: 2px;
  color: #d98bb0;
}

.ach-badge {
  position: relative;
  width: 84px;
  height: 84px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff, #ffe0ee);
  border: 3px solid #ffc0da;
  box-shadow: 0 12px 26px rgba(255, 105, 160, 0.32);
  animation: badgeBounce 1.6s ease-in-out infinite;
}

.ach-box h2 {
  position: relative;
  margin: 0 0 16px;
  font-size: 18px;
  color: #ff4f8b;
  letter-spacing: 0.5px;
}

.ach-list {
  position: relative;
  list-style: none;
  margin: 0 0 20px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ach-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid #ffdceb;
  text-align: left;
}
.ai { font-size: 20px; }
.at { display: flex; flex-direction: column; gap: 1px; }
.at strong { font-size: 13.5px; color: #7a4b5e; }
.at small { font-size: 11px; color: #b98ba0; }

.ach-btn {
  position: relative;
  width: 100%;
  padding: 12px 18px;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-size: 13.5px;
  font-weight: bold;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(255, 79, 139, 0.35);
  transition: 0.2s;
}
.ach-btn:hover { transform: translateY(-2px); box-shadow: 0 14px 26px rgba(255, 79, 139, 0.45); }

@keyframes achPop {
  from { opacity: 0; transform: scale(0.82) translateY(12px); }
  to { opacity: 1; transform: none; }
}
@keyframes badgeBounce {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50% { transform: translateY(-6px) rotate(3deg); }
}
@keyframes sparkle {
  0%, 100% { opacity: 0.2; transform: scale(0.8); }
  50% { opacity: 0.95; transform: scale(1.15); }
}

.ach-fade-enter-active,
.ach-fade-leave-active { transition: opacity 0.25s ease; }
.ach-fade-enter-from,
.ach-fade-leave-to { opacity: 0; }
</style>
