<script setup>
/**
 * 「收藏」页（新增）：展示本地收藏（localStorage），支持取消收藏、跳发现页
 * 内容数据仍来自后端 /api/home/items，按收藏时间倒序展示。
 */
import { ref, computed, onMounted } from 'vue'
import { fetchItems, fetchItemDetail } from '../../api/home.js'
import { getFavorites, removeFavorite } from '../../utils/favorites.js'
import { showToast } from '../../utils/toast.js'

const emit = defineEmits(['jump'])

const allItems = ref([])
const favIds = ref([])          // [{ id, ts }] 按收藏时间倒序
const loading = ref(true)
const loadError = ref('')

/** 收藏的内容（按收藏时间倒序对齐） */
const favItems = computed(() => {
  const map = new Map(allItems.value.map((it) => [it.id, it]))
  return favIds.value.map((f) => ({ ...map.get(f.id), favTs: f.ts })).filter((it) => it.id)
})

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    favIds.value = getFavorites()
    // 一次拉全量（当前内容量小），再在本地按收藏 id 匹配
    const data = await fetchItems({ page: 1, size: 200 })
    allItems.value = data.list
  } catch (e) {
    loadError.value = e.message
  } finally {
    loading.value = false
  }
}

function openDetail(item) {
  fetchItemDetail(item.id)
    .then((detail) => {
      item.views = detail.views
      showToast.info(`🎀 ${detail.title} · ${detail.category}/${detail.subCategory} · 看过 ${detail.views} 次`)
    })
    .catch((e) => showToast.error(e.message))
}

function unFav(item) {
  removeFavorite(item.id)
  favIds.value = getFavorites()
  showToast.info('已从收藏移除')
}

function fmtTime(ts) {
  const d = new Date(ts)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getMonth() + 1}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

onMounted(load)
</script>

<template>
  <div class="favs">
    <!-- 标题条 -->
    <div class="sec-head">
      <h2>💗 我的收藏</h2>
      <span class="sec-sub">共 {{ favItems.length }} 件宝贝</span>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="tab-empty"><div class="te-emoji">🎀</div><h3>正在翻你的百宝箱…</h3></div>

    <!-- 出错 -->
    <div v-else-if="loadError" class="tab-empty">
      <div class="te-emoji">😿</div>
      <h3>{{ loadError }}</h3>
      <button class="te-btn" @click="load">重新加载</button>
    </div>

    <!-- 空收藏 -->
    <div v-else-if="!favItems.length" class="tab-empty">
      <div class="te-emoji">🫧</div>
      <h3>收藏夹还是空的</h3>
      <p>去「发现」页逛逛，看到喜欢的点小心心收进来~</p>
      <button class="te-btn" @click="emit('jump', '发现')">去发现页逛逛</button>
    </div>

    <!-- 收藏列表 -->
    <div v-else class="grid">
      <article v-for="it in favItems" :key="it.id" class="f-card" @click="openDetail(it)">
        <div class="thumb" :style="{ background: it.bg }">
          <span class="emoji">{{ it.emoji }}</span>
          <span class="badge">{{ it.category }}</span>
          <button class="unfav" title="取消收藏" @click.stop="unFav(it)">✕</button>
        </div>
        <div class="body">
          <h3>{{ it.title }}</h3>
          <p>{{ it.desc }}</p>
          <div class="meta">
            <span class="tag">{{ it.tag }}</span>
            <span class="time">收藏于 {{ fmtTime(it.favTs) }}</span>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<style scoped>
.favs { animation: fadeUp 0.45s ease backwards; }

.sec-head { display: flex; align-items: baseline; gap: 10px; margin: 0 0 14px; }
.sec-head h2 { margin: 0; font-size: 17px; color: #ff4f8b; }
.sec-sub { font-size: 12px; color: #c98fae; }

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(206px, 1fr));
  gap: 16px;
}
.f-card {
  background: #fff;
  border: 2px solid #ffd9e8;
  border-radius: 22px;
  overflow: hidden;
  cursor: pointer;
  transition: 0.25s;
  display: flex;
  flex-direction: column;
}
.f-card:hover {
  transform: translateY(-5px);
  border-color: #ffb0cf;
  box-shadow: 0 14px 28px rgba(255, 105, 160, 0.25);
}
.thumb { position: relative; height: 118px; display: flex; align-items: center; justify-content: center; }
.emoji { font-size: 46px; filter: drop-shadow(0 4px 8px rgba(180, 80, 120, 0.25)); transition: 0.3s; }
.f-card:hover .emoji { transform: scale(1.12) rotate(-4deg); }
.badge {
  position: absolute;
  left: 10px;
  top: 10px;
  padding: 2px 9px;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.85);
  color: #ff4f8b;
  font-size: 11px;
  font-weight: bold;
}
.unfav {
  position: absolute;
  right: 10px;
  top: 10px;
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: #e0346b;
  font-size: 12px;
  font-weight: bold;
  line-height: 1;
  cursor: pointer;
  transition: 0.2s;
  box-shadow: 0 3px 8px rgba(180, 80, 120, 0.2);
}
.unfav:hover { background: #ff4f8b; color: #fff; transform: scale(1.12); }
.body { padding: 12px 14px 14px; display: flex; flex-direction: column; gap: 6px; flex: 1; }
.body h3 { margin: 0; font-size: 14px; color: #7a4b5e; font-weight: bold; }
.body p { margin: 0; font-size: 12px; line-height: 1.5; color: #b98ba0; flex: 1; }
.meta { display: flex; justify-content: space-between; align-items: center; margin-top: 2px; }
.tag { font-size: 11px; color: #ff4f8b; background: #fff0f6; padding: 2px 8px; border-radius: 50px; }
.time { font-size: 10.5px; color: #d79bb4; }

.tab-empty {
  background: rgba(255, 255, 255, 0.86);
  border: 2px dashed #ffc0da;
  border-radius: 24px;
  padding: 40px 24px;
  text-align: center;
}
.te-emoji { font-size: 40px; margin-bottom: 8px; }
.tab-empty h3 { margin: 0 0 8px; font-size: 15px; color: #ff4f8b; }
.tab-empty p { margin: 0 0 16px; font-size: 13px; color: #b98ba0; }
.te-btn {
  padding: 9px 22px;
  border: 2px solid #ff4f8b;
  border-radius: 50px;
  background: #fff;
  color: #ff4f8b;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.22s;
}
.te-btn:hover { background: linear-gradient(135deg, #ff6fa5, #ff4f8b); color: #fff; transform: translateY(-2px); }

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: none; }
}

@media (max-width: 720px) {
  .grid { grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
}
</style>
