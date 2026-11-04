<script setup>
/**
 * 玩法四：幸运抽奖
 * 消耗积分抽卡片（后端按权重随机），前端负责转盘旋转 + 停在对应稀有度扇区 + 闪光揭晓。
 */
import { ref, computed } from 'vue'
import { drawLuckyCard } from '../../api/game.js'
import { showToast } from '../../utils/toast.js'
import AchievementPopup from './AchievementPopup.vue'

const props = defineProps({
  userId: { type: Number, default: 0 },
  points: { type: Number, default: 0 },
  meta: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['changed'])

const RARITIES = ['COMMON', 'RARE', 'EPIC', 'LEGENDARY']
const RARITY_LABEL = { COMMON: '普通', RARE: '稀有', EPIC: '史诗', LEGENDARY: '传说' }

const rotation = ref(0)
const spinning = ref(false)
const result = ref(null)
const revealing = ref(false)
const history = ref([])
const popupShow = ref(false)
const popupItems = ref([])

const cost = computed(() => props.meta.drawCost || 20)
const canDraw = computed(() => !spinning.value && props.points >= cost.value)
const wheelStyle = computed(() => ({
  transform: `rotate(${rotation.value}deg)`,
  transition: spinning.value ? 'transform 2.6s cubic-bezier(0.15, 0.9, 0.2, 1)' : 'none'
}))

/** 把后端返回的稀有度换算成转盘要停的角度（指针在正上方） */
function rotationFor(rarity) {
  const idx = Math.max(0, RARITIES.indexOf(rarity))
  const sectorCenter = idx * 90 + 45
  const jitter = Math.random() * 44 - 22
  const current = rotation.value
  const normalized = ((current % 360) + 360) % 360
  const desired = ((360 - sectorCenter + jitter) % 360 + 360) % 360
  let delta = desired - normalized
  if (delta < 0) delta += 360
  return current + 360 * 5 + delta
}

async function onDraw() {
  if (spinning.value) return
  if (props.points < cost.value) {
    showToast.warning(`积分不足 ${cost.value} 分，先去打卡或答题攒一点吧~`)
    return
  }
  spinning.value = true
  result.value = null
  revealing.value = false
  try {
    const res = await drawLuckyCard(props.userId)
    rotation.value = rotationFor(res.rarity)

    // 等转盘停下再揭晓
    setTimeout(() => {
      result.value = res
      revealing.value = true
      spinning.value = false
      history.value = [
        { id: res.card.id, name: res.card.name, emoji: res.card.emoji, rarity: res.rarity, firstTime: res.firstTime },
        ...history.value
      ].slice(0, 6)
      if (res.newAchievements && res.newAchievements.length) {
        popupItems.value = res.newAchievements
        setTimeout(() => (popupShow.value = true), 1100)
      }
      emit('changed')
    }, 2650)
  } catch (e) {
    spinning.value = false
    showToast.error(e.message)
  }
}
</script>

<template>
  <div class="draw">
    <!-- 顶部余额 -->
    <section class="d-bar">
      <div class="d-balance">
        <span class="db-label">我的积分</span>
        <strong>{{ points }}</strong>
      </div>
      <div class="d-cost">
        抽一次 <em>{{ cost }}</em> 分 · 重复卡返还 5 分
      </div>
    </section>

    <!-- 转盘 -->
    <section class="d-stage">
      <div class="d-pointer">▼</div>

      <div class="wheel-wrap">
        <div class="wheel" :style="wheelStyle">
          <div class="sector s0"><span>普通</span></div>
          <div class="sector s1"><span>稀有</span></div>
          <div class="sector s2"><span>史诗</span></div>
          <div class="sector s3"><span>传说</span></div>
          <div class="wheel-core">🎀</div>
        </div>

        <!-- 揭晓闪光 -->
        <transition name="flash">
          <div v-if="revealing && result" class="flash" :class="'f-' + result.rarity.toLowerCase()"></div>
        </transition>
      </div>

      <!-- 抽到的卡片 -->
      <transition name="reveal">
        <div v-if="result" class="d-result" :class="'r-' + result.rarity.toLowerCase()">
          <div class="dr-card">
            <span class="dr-rarity">{{ result.rarityName }}</span>
            <div class="dr-art" :style="{ background: result.card.bg }">
              <span class="dr-emoji">{{ result.card.emoji }}</span>
            </div>
            <div class="dr-body">
              <strong>{{ result.card.name }}</strong>
              <small>{{ result.card.series }} · {{ result.firstTime ? '首张收集' : '重复 ×' + result.card.count }}</small>
            </div>
          </div>
          <p class="dr-msg">{{ result.message }}</p>
        </div>
      </transition>

      <button class="d-btn" :disabled="spinning" :class="{ poor: points < cost }" @click="onDraw">
        <template v-if="spinning">✨ 转盘转起来啦…</template>
        <template v-else-if="points < cost">💤 积分不够，去攒一点</template>
        <template v-else>🎰 抽一次（-{{ cost }} 分）</template>
      </button>
    </section>

    <!-- 最近抽到 -->
    <section v-if="history.length" class="d-history">
      <h4>最近抽到</h4>
      <ul>
        <li v-for="(h, i) in history" :key="i" :class="'r-' + h.rarity.toLowerCase()">
          <span class="dh-emoji">{{ h.emoji }}</span>
          <span class="dh-name">{{ h.name }}</span>
          <span class="dh-tag">{{ RARITY_LABEL[h.rarity] }}</span>
          <span v-if="!h.firstTime" class="dh-dup">重复</span>
        </li>
      </ul>
    </section>

    <AchievementPopup :show="popupShow" :items="popupItems" @close="popupShow = false" />
  </div>
</template>

<style scoped>
.draw { animation: fadeUp 0.4s ease backwards; }

/* 余额条 */
.d-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #ffd9e8;
  margin-bottom: 16px;
}
.d-balance { display: flex; align-items: baseline; gap: 8px; }
.db-label { font-size: 11.5px; color: #b98ba0; }
.d-balance strong { font-size: 22px; color: #ff4f8b; }
.d-cost { font-size: 11.5px; color: #b96a8d; }
.d-cost em { font-style: normal; font-weight: bold; color: #ff4f8b; }

/* 转盘舞台 */
.d-stage {
  position: relative;
  padding: 26px 20px 24px;
  border-radius: 26px;
  background: linear-gradient(150deg, #fff8fb, #ffe9f4);
  border: 2px solid #ffd0e3;
  box-shadow: 0 16px 34px rgba(255, 105, 160, 0.16);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.d-pointer {
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 20px;
  color: #ff4f8b;
  filter: drop-shadow(0 2px 4px rgba(255, 79, 139, 0.5));
  z-index: 4;
}

.wheel-wrap {
  position: relative;
  width: 232px;
  height: 232px;
  margin: 16px 0 18px;
}

.wheel {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 8px solid #fff;
  box-shadow: 0 12px 28px rgba(214, 60, 118, 0.25), inset 0 0 0 2px #ffd9e8;
  background: conic-gradient(
    #ffe0ec 0deg 90deg,
    #d9ecff 90deg 180deg,
    #efe0ff 180deg 270deg,
    #fff0c2 270deg 360deg
  );
  overflow: hidden;
}

.sector {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
}
.sector span {
  margin-top: 16px;
  font-size: 11px;
  font-weight: bold;
  color: #a86b87;
  letter-spacing: 1px;
}
.sector.s0 { transform: rotate(45deg); }
.sector.s1 { transform: rotate(135deg); }
.sector.s2 { transform: rotate(225deg); }
.sector.s3 { transform: rotate(315deg); }
.sector.s1 span { color: #3f7fd0; }
.sector.s2 span { color: #8b5cf6; }
.sector.s3 span { color: #d99a00; }

.wheel-core {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 66px;
  height: 66px;
  border-radius: 50%;
  background: #fff;
  border: 4px solid #ffd0e3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  box-shadow: 0 6px 16px rgba(214, 60, 118, 0.25);
  z-index: 3;
}

/* 闪光 */
.flash {
  position: absolute;
  inset: -18px;
  border-radius: 50%;
  pointer-events: none;
  animation: flashBurst 1s ease-out forwards;
}
.flash.f-common { box-shadow: 0 0 0 0 rgba(255, 143, 187, 0.9); }
.flash.f-rare { box-shadow: 0 0 0 0 rgba(96, 165, 250, 0.9); }
.flash.f-epic { box-shadow: 0 0 0 0 rgba(167, 139, 250, 0.9); }
.flash.f-legendary { box-shadow: 0 0 0 0 rgba(255, 196, 61, 0.95); }

@keyframes flashBurst {
  0% { box-shadow: 0 0 0 0 currentColor; opacity: 1; transform: scale(0.9); }
  40% { box-shadow: 0 0 60px 26px rgba(255, 224, 130, 0.85); opacity: 0.95; transform: scale(1.04); }
  100% { box-shadow: 0 0 90px 40px rgba(255, 224, 130, 0); opacity: 0; transform: scale(1.12); }
}

/* 结果卡 */
.d-result { text-align: center; margin-bottom: 16px; }
.dr-card {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px 10px 10px;
  border-radius: 20px;
  background: #fff;
  border: 2px solid #ffd9e8;
  box-shadow: 0 12px 26px rgba(255, 105, 160, 0.2);
}
.d-result.r-rare .dr-card { border-color: #a9cdf7; box-shadow: 0 12px 26px rgba(96, 165, 250, 0.25); }
.d-result.r-epic .dr-card { border-color: #c9b6fb; box-shadow: 0 12px 26px rgba(167, 139, 250, 0.3); }
.d-result.r-legendary .dr-card {
  border-color: #ffd166;
  box-shadow: 0 0 0 3px rgba(255, 224, 130, 0.45), 0 14px 30px rgba(255, 176, 32, 0.35);
  animation: legendGlow 1.8s ease-in-out infinite;
}
.dr-rarity {
  align-self: flex-start;
  padding: 1px 8px;
  border-radius: 50px;
  font-size: 10px;
  font-weight: bold;
  background: #ffe9f3;
  color: #ff4f8b;
}
.d-result.r-rare .dr-rarity { background: #e3f0ff; color: #3f7fd0; }
.d-result.r-epic .dr-rarity { background: #f3e6ff; color: #8b5cf6; }
.d-result.r-legendary .dr-rarity { background: #fff3cc; color: #d99a00; }

.dr-art {
  width: 54px;
  height: 54px;
  flex: 0 0 54px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.dr-emoji { font-size: 28px; }
.dr-body { display: flex; flex-direction: column; gap: 2px; text-align: left; }
.dr-body strong { font-size: 13.5px; color: #7a4b5e; }
.dr-body small { font-size: 10.5px; color: #c98fae; }
.dr-msg { margin: 8px 0 0; font-size: 12px; color: #b96a8d; }

/* 按钮 */
.d-btn {
  width: 100%;
  max-width: 320px;
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
.d-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 16px 30px rgba(255, 79, 139, 0.44); }
.d-btn:disabled { cursor: default; }
.d-btn.poor { background: #fff; color: #d1689a; border: 2px solid #ffd0e3; box-shadow: none; }

/* 历史 */
.d-history {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.88);
  border: 2px solid #ffd9e8;
}
.d-history h4 { margin: 0 0 10px; font-size: 12.5px; color: #ff4f8b; }
.d-history ul { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 6px; }
.d-history li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: 12px;
  background: #fffafc;
  border: 1px solid #ffe6f0;
  font-size: 12px;
}
.dh-emoji { font-size: 16px; }
.dh-name { flex: 1; color: #7a4b5e; }
.dh-tag {
  padding: 0 7px;
  border-radius: 50px;
  font-size: 10px;
  background: #ffe9f3;
  color: #ff4f8b;
}
li.r-rare .dh-tag { background: #e3f0ff; color: #3f7fd0; }
li.r-epic .dh-tag { background: #f3e6ff; color: #8b5cf6; }
li.r-legendary .dh-tag { background: #fff3cc; color: #d99a00; }
.dh-dup { font-size: 10px; color: #c98fae; }

/* 过渡 */
.reveal-enter-active { transition: all 0.45s cubic-bezier(0.2, 1.4, 0.4, 1); }
.reveal-enter-from { opacity: 0; transform: translateY(14px) scale(0.9); }
.flash-enter-active { transition: opacity 0.2s; }
.flash-leave-active { transition: opacity 0.6s; }
.flash-leave-to { opacity: 0; }

@keyframes legendGlow {
  0%, 100% { box-shadow: 0 0 0 3px rgba(255, 224, 130, 0.45), 0 14px 30px rgba(255, 176, 32, 0.3); }
  50% { box-shadow: 0 0 0 8px rgba(255, 224, 130, 0.22), 0 16px 34px rgba(255, 176, 32, 0.45); }
}
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: none; }
}

@media (max-width: 620px) {
  .wheel-wrap { width: 200px; height: 200px; }
  .d-bar { flex-direction: column; align-items: flex-start; gap: 6px; }
}
</style>
