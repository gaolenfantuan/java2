<script setup>
/**
 * 「发现」页（新增）：热门榜 + 最新上架
 * 复用后端已有接口 /api/home/hot 和 /api/home/items，后端零改动。
 */
import { ref, onMounted } from 'vue'
import { fetchHot, fetchItems, fetchItemDetail } from '../../api/home.js'
import { isFavorite, toggleFavorite } from '../../utils/favorites.js'
import { showToast } from '../../utils/toast.js'

const emit = defineEmits(['jump'])

const hotList = ref([])
const newList = ref([])
const loading = ref(true)
const loadError = ref('')

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const [hot, latest] = await Promise.all([fetchHot(5), fetchItems({ sort: 'new', size: 12 })])
    hotList.value = hot
    newList.value = latest.list
  } catch (e) {
    loadError.value = e.message
  } finally {
    loading.value = false
  }
}

const medals = ['🥇', '🥈', '🥉']

function openDetail(item) {
  fetchItemDetail(item.id)
    .then((detail) => {
      item.views = detail.views
      showToast.info(`🎀 ${detail.title} · ${detail.category}/${detail.subCategory} · 看过 ${detail.views} 次`)
    })
    .catch((e) => showToast.error(e.message))
}

function toggleFav(item) {
  const nowFav = toggleFavorite(item.id)
  showToast(nowFav ? '已加入收藏 💗' : '已取消收藏', nowFav ? 'success' : 'info')
}

onMounted(load)
</script>

<template>
  <div class="discover">
    <!-- 错误提示 -->
    <section v-if="loadError && !hotList.length" class="tab-empty">
      <div class="te-emoji">😿</div>
      <h3>{{ loadError }}</h3>
      <button class="te-btn" @click="load">重新加载</button>
    </section>

    <template v-else>
      <!-- 热门榜 -->
      <section class="rank-card">
        <div class="sec-head">
          <h2>🔥 热门榜</h2>
          <span class="sec-sub">大家都在点点点的小可爱</span>
        </div>

        <div v-if="loading" class="rank-loading">🎀 榜单加载中…</div>
        <ol v-else class="rank-list">
          <li v-for="(it, i) in hotList" :key="it.id" class="rank-item" @click="openDetail(it)">
            <span class="rank-no" :class="{ top: i < 3 }">{{ i < 3 ? medals[i] : i + 1 }}</span>
            <span class="rank-emoji">{{ it.emoji }}</span>
            <div class="rank-info">
              <strong>{{ it.title }}</strong>
              <small>{{ it.category }} · {{ it.desc }}</small>
            </div>
            <div class="rank-meta">
              <span>❤ {{ it.likes }}</span>
              <span>👁 {{ it.views }}</span>
            </div>
          </li>
        </ol>
      </section>

      <!-- 最新上架 -->
      <section class="latest">
        <div class="sec-head">
          <h2>🆕 最新上架</h2>
          <span class="sec-sub">点右上角的小心心就能收藏哦</span>
        </div>

        <div v-if="loading" class="tab-empty"><div class="te-emoji">🎀</div><h3>正在搬新宝贝…</h3></div>
        <div v-else class="grid">
          <article v-for="it in newList" :key="it.id" class="d-card" @click="openDetail(it)">
            <div class="thumb" :style="{ background: it.bg }">
              <span class="emoji">{{ it.emoji }}</span>
              <span class="badge">{{ it.category }}</span>
              <button
                class="fav"
                :class="{ on: isFavorite(it.id) }"
                :title="isFavorite(it.id) ? '取消收藏' : '收藏'"
                @click.stop="toggleFav(it)"
              >
                {{ isFavorite(it.id) ? '💗' : '🤍' }}
              </button>
            </div>
            <div class="body">
              <h3>{{ it.title }}</h3>
              <p>{{ it.desc }}</p>
              <div class="meta">
                <span class="tag">{{ it.tag }}</span>
                <span class="nums">❤ {{ it.likes }} · 👁 {{ it.views }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.discover { animation: fadeUp 0.45s ease backwards; }

.sec-head { display: flex; align-items: baseline; gap: 10px; margin: 0 0 12px; }
.sec-head h2 { margin: 0; font-size: 17px; color: #ff4f8b; }
.sec-sub { font-size: 12px; color: #c98fae; }

/* 热门榜 */
.rank-card {
  background: rgba(255, 255, 255, 0.88);
  border: 2px solid #ffd9e8;
  border-radius: 24px;
  padding: 20px 22px;
  box-shadow: 0 10px 24px rgba(255, 105, 160, 0.12);
}
.rank-loading { padding: 22px; text-align: center; color: #b96a8d; font-size: 13px; }
.rank-list { list-style: none; margin: 0; padding: 0; }
.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 11px 10px;
  border-radius: 16px;
  cursor: pointer;
  transition: 0.2s;
}
.rank-item:hover { background: #fff0f6; transform: translateX(4px); }
.rank-no {
  flex: 0 0 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffe3ee;
  color: #d1689a;
  font-size: 13px;
  font-weight: bold;
}
.rank-no.top { background: transparent; font-size: 22px; }
.rank-emoji { font-size: 26px; filter: drop-shadow(0 3px 5px rgba(180, 80, 120, 0.2)); }
.rank-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.rank-info strong { font-size: 14px; color: #7a4b5e; }
.rank-info small { font-size: 11.5px; color: #b98ba0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-meta { display: flex; flex-direction: column; align-items: flex-end; gap: 2px; font-size: 11px; color: #d79bb4; }

/* 最新卡片 */
.latest { margin-top: 22px; }
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(206px, 1fr));
  gap: 16px;
}
.d-card {
  background: #fff;
  border: 2px solid #ffd9e8;
  border-radius: 22px;
  overflow: hidden;
  cursor: pointer;
  transition: 0.25s;
  display: flex;
  flex-direction: column;
}
.d-card:hover {
  transform: translateY(-5px);
  border-color: #ffb0cf;
  box-shadow: 0 14px 28px rgba(255, 105, 160, 0.25);
}
.thumb { position: relative; height: 118px; display: flex; align-items: center; justify-content: center; }
.emoji { font-size: 46px; filter: drop-shadow(0 4px 8px rgba(180, 80, 120, 0.25)); transition: 0.3s; }
.d-card:hover .emoji { transform: scale(1.12) rotate(-4deg); }
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
.fav {
  position: absolute;
  right: 10px;
  top: 10px;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
  transition: 0.2s;
  box-shadow: 0 3px 8px rgba(180, 80, 120, 0.2);
}
.fav:hover { transform: scale(1.18); }
.fav.on { animation: favPop 0.35s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes favPop {
  0% { transform: scale(0.4); }
  60% { transform: scale(1.35); }
  100% { transform: scale(1); }
}
.body { padding: 12px 14px 14px; display: flex; flex-direction: column; gap: 6px; flex: 1; }
.body h3 { margin: 0; font-size: 14px; color: #7a4b5e; font-weight: bold; }
.body p { margin: 0; font-size: 12px; line-height: 1.5; color: #b98ba0; flex: 1; }
.meta { display: flex; justify-content: space-between; align-items: center; margin-top: 2px; }
.tag { font-size: 11px; color: #ff4f8b; background: #fff0f6; padding: 2px 8px; border-radius: 50px; }
.nums { font-size: 11px; color: #d79bb4; }

/* 空状态（发现页复用风格，样式全新，不影响首页同名类） */
.tab-empty {
  background: rgba(255, 255, 255, 0.86);
  border: 2px dashed #ffc0da;
  border-radius: 24px;
  padding: 40px 24px;
  text-align: center;
}
.te-emoji { font-size: 40px; margin-bottom: 8px; }
.tab-empty h3 { margin: 0 0 14px; font-size: 15px; color: #ff4f8b; }
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
  .rank-card { padding: 16px; }
}
</style>
