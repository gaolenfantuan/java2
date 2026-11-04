<script setup>
/**
 * 玩法一：卡片收集
 * 点卡片 -> 收集（写库）-> 爱心动画 -> 达成数量自动弹成就弹窗
 * 已收集的卡片会点亮，未收集的显示成剪影，收集进度实时更新。
 */
import { ref, onMounted } from 'vue'
import { fetchCards, collectCard } from '../../api/game.js'
import { showToast } from '../../utils/toast.js'
import AchievementPopup from './AchievementPopup.vue'

const props = defineProps({
  userId: { type: Number, default: 0 }
})
const emit = defineEmits(['changed'])

const cards = ref([])
const loading = ref(true)
const loadError = ref('')
const busy = ref(false)
const total = ref(0)
const collected = ref(0)
const percent = ref(0)

// 爱心动画
const gridRef = ref(null)
const hearts = ref([])
let heartSeq = 0

// 成就弹窗
const popupShow = ref(false)
const popupItems = ref([])

const rarityClass = (r) => 'r-' + String(r || 'COMMON').toLowerCase()

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const list = await fetchCards(props.userId)
    cards.value = list
    total.value = list.length
    collected.value = list.filter((c) => c.collected).length
    percent.value = total.value ? Math.round((collected.value * 100) / total.value) : 0
  } catch (e) {
    loadError.value = e.message
  } finally {
    loading.value = false
  }
}

/** 在点击位置撒一把爱心 */
function burstHearts(e) {
  const box = gridRef.value
  if (!box) return
  const rect = box.getBoundingClientRect()
  const x = (e.clientX || rect.left + rect.width / 2) - rect.left
  const y = (e.clientY || rect.top + rect.height / 2) - rect.top
  const icons = ['💗', '💖', '🎀', '✨', '💕']
  for (let i = 0; i < 6; i++) {
    const id = ++heartSeq
    hearts.value.push({
      id,
      x: x + (Math.random() * 44 - 22),
      y: y + (Math.random() * 20 - 10),
      icon: icons[Math.floor(Math.random() * icons.length)],
      delay: Math.random() * 0.12,
      drift: Math.random() * 40 - 20
    })
    setTimeout(() => {
      hearts.value = hearts.value.filter((h) => h.id !== id)
    }, 1000)
  }
}

async function onCardClick(card, e) {
  burstHearts(e)
  if (card.collected) {
    showToast.info(`「${card.name}」已经在图鉴里啦~`)
    return
  }
  if (busy.value) return
  busy.value = true
  try {
    const res = await collectCard(props.userId, card.id)
    // 直接把后端返回的最新状态覆盖到这张卡上，不用整页重拉
    Object.assign(card, res.card)
    collected.value = res.collectedCount
    total.value = res.totalCards
    percent.value = res.collectPercent
    showToast.success(res.message)
    if (res.newAchievements && res.newAchievements.length) {
      popupItems.value = res.newAchievements
      popupShow.value = true
    }
    emit('changed')
  } catch (err) {
    showToast.error(err.message)
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="collect">
    <!-- 进度条 -->
    <section class="progress-card">
      <div class="pc-head">
        <h3>🍬 收集进度</h3>
        <span class="pc-num">{{ collected }}<em>/{{ total }}</em></span>
      </div>
      <div class="pc-bar">
        <div class="pc-fill" :style="{ width: percent + '%' }"></div>
      </div>
      <p class="pc-tip">
        <template v-if="percent >= 100">🎉 全部收集完成，图鉴圆满啦！</template>
        <template v-else>点一下没亮起来的卡片就能收进图鉴，集满 3 / 6 / 9 / 12 张会解锁成就哦～</template>
      </p>
    </section>

    <!-- 出错 -->
    <section v-if="loadError" class="g-empty">
      <div class="ge-emoji">😿</div>
      <h3>{{ loadError }}</h3>
      <button class="ge-btn" @click="load">重新加载</button>
    </section>

    <!-- 加载中 -->
    <section v-else-if="loading" class="g-empty">
      <div class="ge-emoji">🎀</div>
      <h3>正在把卡片摆上桌…</h3>
    </section>

    <!-- 卡片网格 -->
    <section v-else ref="gridRef" class="card-grid">
      <button
        v-for="c in cards"
        :key="c.id"
        class="gcard"
        :class="[rarityClass(c.rarity), { on: c.collected, locked: !c.collected }]"
        @click="onCardClick(c, $event)"
      >
        <span class="gc-rarity">{{ c.rarityName }}</span>
        <div class="gc-art" :style="{ background: c.bg }">
          <span class="gc-emoji">{{ c.collected ? c.emoji : '❔' }}</span>
          <span v-if="!c.collected" class="gc-lock">点击收集</span>
          <span v-else-if="c.count > 1" class="gc-count">×{{ c.count }}</span>
        </div>
        <div class="gc-body">
          <strong>{{ c.collected ? c.name : '？？？' }}</strong>
          <small>{{ c.series }}</small>
        </div>
      </button>

      <!-- 爱心动画层 -->
      <span
        v-for="h in hearts"
        :key="h.id"
        class="heart-pop"
        :style="{ left: h.x + 'px', top: h.y + 'px', '--drift': h.drift + 'px', animationDelay: h.delay + 's' }"
      >{{ h.icon }}</span>
    </section>

    <AchievementPopup :show="popupShow" :items="popupItems" @close="popupShow = false" />
  </div>
</template>

<style scoped>
.collect { animation: fadeUp 0.4s ease backwards; }

/* 进度卡 */
.progress-card {
  padding: 18px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #ffd9e8;
  box-shadow: 0 10px 24px rgba(255, 105, 160, 0.12);
  margin-bottom: 16px;
}
.pc-head { display: flex; align-items: baseline; justify-content: space-between; }
.pc-head h3 { margin: 0; font-size: 15px; color: #ff4f8b; }
.pc-num { font-size: 20px; font-weight: bold; color: #ff4f8b; }
.pc-num em { font-style: normal; font-size: 12px; color: #c98fae; }
.pc-bar {
  height: 10px;
  margin: 10px 0 8px;
  border-radius: 50px;
  background: #ffe3ee;
  overflow: hidden;
}
.pc-fill {
  height: 100%;
  border-radius: 50px;
  background: linear-gradient(90deg, #ff9dc0, #ff4f8b);
  box-shadow: 0 0 10px rgba(255, 79, 139, 0.5);
  transition: width 0.5s cubic-bezier(0.2, 1, 0.4, 1);
}
.pc-tip { margin: 0; font-size: 11.5px; color: #b98ba0; line-height: 1.6; }

/* 网格 */
.card-grid {
  position: relative;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.gcard {
  position: relative;
  padding: 0;
  border: 2px solid #ffd9e8;
  border-radius: 20px;
  background: #fff;
  overflow: hidden;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: 0.24s;
}
.gcard:hover { transform: translateY(-4px); border-color: #ffb0cf; box-shadow: 0 14px 26px rgba(255, 105, 160, 0.22); }
.gcard.locked .gc-art { filter: grayscale(0.72) brightness(1.06); }
.gcard.on { border-color: #ff8ab8; }

.gc-rarity {
  position: absolute;
  right: 8px;
  top: 8px;
  z-index: 2;
  padding: 1px 8px;
  border-radius: 50px;
  font-size: 10px;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.9);
  color: #b96a8d;
}
.gcard.r-rare .gc-rarity { background: #e3f0ff; color: #3f7fd0; }
.gcard.r-epic .gc-rarity { background: #f3e6ff; color: #8b5cf6; }
.gcard.r-legendary .gc-rarity { background: #fff3cc; color: #d99a00; }

.gc-art {
  position: relative;
  height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gc-emoji { font-size: 40px; filter: drop-shadow(0 4px 8px rgba(180, 80, 120, 0.25)); transition: 0.3s; }
.gcard:hover .gc-emoji { transform: scale(1.12) rotate(-4deg); }
.gc-lock {
  position: absolute;
  bottom: 6px;
  font-size: 10px;
  color: #fff;
  background: rgba(255, 79, 139, 0.72);
  padding: 1px 8px;
  border-radius: 50px;
}
.gc-count {
  position: absolute;
  left: 8px;
  bottom: 6px;
  font-size: 10px;
  font-weight: bold;
  color: #fff;
  background: rgba(122, 75, 94, 0.55);
  padding: 1px 7px;
  border-radius: 50px;
}

.gc-body { padding: 8px 12px 11px; display: flex; flex-direction: column; gap: 1px; }
.gc-body strong { font-size: 12.5px; color: #7a4b5e; }
.gc-body small { font-size: 10.5px; color: #c98fae; }

/* 爱心 */
.heart-pop {
  position: absolute;
  font-size: 18px;
  pointer-events: none;
  transform: translate(-50%, -50%);
  animation: heartUp 1s ease-out forwards;
  z-index: 5;
}
@keyframes heartUp {
  0% { opacity: 0; transform: translate(-50%, -50%) scale(0.4); }
  25% { opacity: 1; transform: translate(-50%, -60%) scale(1.15); }
  100% { opacity: 0; transform: translate(calc(-50% + var(--drift)), -170%) scale(0.85); }
}

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
</style>
