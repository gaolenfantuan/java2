<script setup>
/**
 * 收集图鉴（在「收藏」页里作为第二个页签使用）
 * 只展示 + 收藏，不做收集动作；收集在「小游戏 → 卡片收集」里完成。
 */
import { ref, computed, onMounted } from 'vue'
import { fetchCards, fetchGameProfile, toggleCardFavorite } from '../../api/game.js'
import { showToast } from '../../utils/toast.js'

const props = defineProps({
  userId: { type: Number, default: 0 }
})
const emit = defineEmits(['changed'])

const cards = ref([])
const profile = ref(null)
const loading = ref(true)
const error = ref('')
const filter = ref('all')       // all | collected | favorite
const busy = ref(false)

const filters = [
  { key: 'all', label: '全部' },
  { key: 'collected', label: '已收集' },
  { key: 'favorite', label: '已收藏' }
]

const shown = computed(() => {
  if (filter.value === 'collected') return cards.value.filter((c) => c.collected)
  if (filter.value === 'favorite') return cards.value.filter((c) => c.favorite)
  return cards.value
})

const collected = computed(() => cards.value.filter((c) => c.collected).length)
const total = computed(() => cards.value.length)
const percent = computed(() => (total.value ? Math.round((collected.value * 100) / total.value) : 0))

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [list, p] = await Promise.all([fetchCards(props.userId), fetchGameProfile(props.userId)])
    cards.value = list
    profile.value = p
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

async function onFav(card) {
  if (busy.value) return
  if (!card.collected) {
    showToast.warning('先把这张卡片收集起来，才能收藏哦~')
    return
  }
  busy.value = true
  try {
    const updated = await toggleCardFavorite(props.userId, card.id, !card.favorite)
    card.favorite = updated.favorite
    showToast(updated.favorite ? '已收藏 💗' : '已取消收藏', updated.favorite ? 'success' : 'info')
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
  <div class="book">
    <!-- 进度 -->
    <section class="bk-head">
      <div class="bh-left">
        <h3>📖 收集图鉴</h3>
        <p>点右上角小心心可以收藏，收集在「小游戏 → 卡片收集」里完成</p>
      </div>
      <div class="bh-right">
        <strong>{{ collected }}<em>/{{ total }}</em></strong>
        <div class="bh-bar"><i :style="{ width: percent + '%' }"></i></div>
        <small>{{ percent }}%</small>
      </div>
    </section>

    <!-- 筛选 -->
    <div class="bk-filters">
      <button
        v-for="f in filters"
        :key="f.key"
        class="bk-chip"
        :class="{ on: filter === f.key }"
        @click="filter = f.key"
      >
        {{ f.label }}
        <span v-if="f.key === 'collected'" class="chip-num">{{ collected }}</span>
        <span v-else-if="f.key === 'favorite'" class="chip-num">{{ profile ? profile.favoriteCardCount : 0 }}</span>
      </button>
    </div>

    <!-- 状态 -->
    <section v-if="error" class="g-empty">
      <div class="ge-emoji">😿</div>
      <h3>{{ error }}</h3>
      <button class="ge-btn" @click="load">重新加载</button>
    </section>

    <section v-else-if="loading" class="g-empty">
      <div class="ge-emoji">🎀</div>
      <h3>正在翻开图鉴…</h3>
    </section>

    <section v-else-if="!shown.length" class="g-empty">
      <div class="ge-emoji">🫧</div>
      <h3>{{ filter === 'favorite' ? '还没有收藏任何卡片' : '这一页还空着' }}</h3>
      <p>{{ filter === 'favorite' ? '点卡片右上角的小心心就能收进来~' : '去小游戏里收集几张卡片吧~' }}</p>
    </section>

    <!-- 图鉴网格 -->
    <section v-else class="bk-grid">
      <article
        v-for="c in shown"
        :key="c.id"
        class="bcard"
        :class="['r-' + String(c.rarity).toLowerCase(), { locked: !c.collected }]"
      >
        <span class="bc-rarity">{{ c.rarityName }}</span>
        <button
          class="bc-fav"
          :class="{ on: c.favorite }"
          :title="c.favorite ? '取消收藏' : '收藏'"
          @click.stop="onFav(c)"
        >
          {{ c.favorite ? '💗' : '🤍' }}
        </button>
        <div class="bc-art" :style="{ background: c.bg }">
          <span class="bc-emoji">{{ c.collected ? c.emoji : '❔' }}</span>
        </div>
        <div class="bc-body">
          <strong>{{ c.collected ? c.name : '未收集' }}</strong>
          <small>{{ c.series }}<template v-if="c.count > 1"> · ×{{ c.count }}</template></small>
          <p v-if="c.collected" class="bc-intro">{{ c.intro }}</p>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.book { animation: fadeUp 0.4s ease backwards; }

/* 头部 */
.bk-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid #ffd9e8;
  margin-bottom: 12px;
}
.bh-left h3 { margin: 0 0 3px; font-size: 15px; color: #ff4f8b; }
.bh-left p { margin: 0; font-size: 11px; color: #b98ba0; }
.bh-right { display: flex; align-items: center; gap: 8px; }
.bh-right strong { font-size: 18px; color: #ff4f8b; }
.bh-right em { font-style: normal; font-size: 11px; color: #c98fae; }
.bh-bar { width: 92px; height: 7px; border-radius: 50px; background: #ffe3ee; overflow: hidden; }
.bh-bar i {
  display: block;
  height: 100%;
  border-radius: 50px;
  background: linear-gradient(90deg, #ff9dc0, #ff4f8b);
  transition: width 0.5s ease;
}
.bh-right small { font-size: 10.5px; color: #c98fae; }

/* 筛选 */
.bk-filters { display: flex; gap: 8px; margin-bottom: 14px; flex-wrap: wrap; }
.bk-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 15px;
  border-radius: 50px;
  border: 2px solid #ffd3e5;
  background: rgba(255, 255, 255, 0.82);
  color: #b96a8d;
  font-size: 12.5px;
  font-family: inherit;
  cursor: pointer;
  transition: 0.2s;
}
.bk-chip:hover { border-color: #ff8ab8; color: #ff4f8b; }
.bk-chip.on {
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  border-color: transparent;
  color: #fff;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 79, 139, 0.3);
}
.chip-num {
  font-size: 10px;
  padding: 0 5px;
  border-radius: 50px;
  background: rgba(255, 79, 139, 0.12);
  color: #d1689a;
}
.bk-chip.on .chip-num { background: rgba(255, 255, 255, 0.28); color: #fff; }

/* 网格 */
.bk-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(158px, 1fr));
  gap: 12px;
}
.bcard {
  position: relative;
  border-radius: 20px;
  background: #fff;
  border: 2px solid #ffd9e8;
  overflow: hidden;
  transition: 0.24s;
}
.bcard:hover { transform: translateY(-4px); box-shadow: 0 14px 26px rgba(255, 105, 160, 0.2); }
.bcard.locked { background: #fffafc; }
.bcard.locked .bc-art { filter: grayscale(0.75) brightness(1.05); }
.bcard.r-rare { border-color: #c9dffa; }
.bcard.r-epic { border-color: #ddcffc; }
.bcard.r-legendary { border-color: #ffe08a; }

.bc-rarity {
  position: absolute;
  left: 8px;
  top: 8px;
  z-index: 2;
  padding: 1px 8px;
  border-radius: 50px;
  font-size: 10px;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.9);
  color: #b96a8d;
}
.bcard.r-rare .bc-rarity { background: #e3f0ff; color: #3f7fd0; }
.bcard.r-epic .bc-rarity { background: #f3e6ff; color: #8b5cf6; }
.bcard.r-legendary .bc-rarity { background: #fff3cc; color: #d99a00; }

.bc-fav {
  position: absolute;
  right: 8px;
  top: 8px;
  z-index: 2;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  font-size: 12px;
  line-height: 1;
  cursor: pointer;
  box-shadow: 0 3px 8px rgba(180, 80, 120, 0.18);
  transition: 0.2s;
}
.bc-fav:hover { transform: scale(1.12); }
.bc-fav.on { background: #fff0f6; }

.bc-art { height: 92px; display: flex; align-items: center; justify-content: center; }
.bc-emoji { font-size: 36px; filter: drop-shadow(0 4px 8px rgba(180, 80, 120, 0.22)); transition: 0.3s; }
.bcard:hover .bc-emoji { transform: scale(1.1) rotate(-4deg); }

.bc-body { padding: 9px 12px 12px; display: flex; flex-direction: column; gap: 2px; }
.bc-body strong { font-size: 12.5px; color: #7a4b5e; }
.bc-body small { font-size: 10.5px; color: #c98fae; }
.bc-intro { margin: 4px 0 0; font-size: 10.5px; line-height: 1.5; color: #b9a2ac; }

/* 空态 */
.g-empty {
  background: rgba(255, 255, 255, 0.88);
  border: 2px dashed #ffc0da;
  border-radius: 22px;
  padding: 36px 22px;
  text-align: center;
}
.ge-emoji { font-size: 34px; margin-bottom: 8px; }
.g-empty h3 { margin: 0 0 6px; font-size: 14px; color: #ff4f8b; }
.g-empty p { margin: 0 0 12px; font-size: 12px; color: #b98ba0; }
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
  .bk-grid { grid-template-columns: repeat(auto-fill, minmax(132px, 1fr)); gap: 10px; }
  .bk-head { flex-direction: column; align-items: flex-start; }
}
</style>
