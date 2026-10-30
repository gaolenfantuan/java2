<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import KittyAvatar from '../components/KittyAvatar.vue'
import HomeNav from '../components/home/HomeNav.vue'
import CardItem from '../components/home/CardItem.vue'
// 新增：发现 / 收藏 / 我的 三个 tab 页面组件
import DiscoverTab from '../components/home/DiscoverTab.vue'
import FavoritesTab from '../components/home/FavoritesTab.vue'
import ProfileTab from '../components/home/ProfileTab.vue'
import { getUser, clearUser } from '../utils/auth.js'
import { fetchCategories, fetchItems, fetchStats, fetchItemDetail } from '../api/home.js'

const router = useRouter()

// 登录时存下来的用户信息（路由守卫已保证这里一定有值）
const user = ref(getUser() || {})
const displayName = computed(() => user.value.nickname || user.value.username || '小猫咪')

const PAGE_SIZE = 12

// ---------------- 数据状态 ----------------
const categories = ref([])          // 分类树（一级 + 二级）
const items = ref([])               // 当前页内容
const stats = ref({ itemTotal: 0, categoryTotal: 0, likeTotal: 0 })

const keyword = ref('')             // 搜索词（输入即搜，300ms 防抖）
const primaryId = ref(0)            // 一级分类，0 = 全部
const subId = ref(0)                // 二级分类，0 = 该一级下的全部
const sort = ref('default')         // default | hot | new

const total = ref(0)
const page = ref(1)
const hasNext = ref(false)
const loading = ref(false)
const loadError = ref('')

const activeTab = ref('首页')

// 当前选中的一级分类对象
const currentPrimary = computed(() => categories.value.find((c) => c.id === primaryId.value) || null)
// 它的二级分类（点开一级分类后才出现第二排筛选条）
const subCategories = computed(() => currentPrimary.value?.children || [])

// 第一排：全部 + 四个一级分类
const primaryChips = computed(() => [
  { id: 0, name: '全部', icon: '✨', count: stats.value.itemTotal },
  ...categories.value
])

const sortOptions = [
  { key: 'default', label: '默认' },
  { key: 'hot', label: '最热' },
  { key: 'new', label: '最新' }
]

// ---------------- 请求 ----------------
async function loadCategories() {
  try {
    categories.value = await fetchCategories()
  } catch (e) {
    loadError.value = e.message
  }
}

async function loadStats() {
  try {
    stats.value = await fetchStats()
  } catch (e) {
    /* 统计失败不影响主流程，静默即可 */
  }
}

async function loadItems(append = false) {
  if (append) {
    page.value += 1
  } else {
    page.value = 1
  }
  loading.value = true
  loadError.value = ''
  try {
    const data = await fetchItems({
      keyword: keyword.value.trim(),
      // 选了二级分类就按二级，否则按一级（后端会自动带上它的子分类）
      categoryId: subId.value || primaryId.value || undefined,
      sort: sort.value,
      page: page.value,
      size: PAGE_SIZE
    })
    items.value = append ? [...items.value, ...data.list] : data.list
    total.value = data.total
    hasNext.value = data.hasNext
  } catch (e) {
    loadError.value = e.message
    if (append) {
      page.value -= 1
    } else {
      items.value = []
      total.value = 0
    }
    hasNext.value = false
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (!loading.value && hasNext.value) loadItems(true)
}

// ---------------- 交互 ----------------
let searchTimer = null
watch(keyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => loadItems(), 300)
})

watch([primaryId, subId, sort], () => loadItems())

function selectPrimary(id) {
  if (primaryId.value === id) return
  primaryId.value = id
  subId.value = 0
}
function selectSub(id) {
  if (subId.value === id) return
  subId.value = id
}

function resetFilter() {
  keyword.value = ''
  primaryId.value = 0
  subId.value = 0
  sort.value = 'default'
  loadItems()
}

async function openItem(item) {
  // 详情页还没做，但顺手调一下详情接口（后端会 +1 浏览量），把真实数据提示出来
  try {
    const detail = await fetchItemDetail(item.id)
    showToastMsg(`🎀 ${detail.title} · ${detail.category}/${detail.subCategory} · 看过 ${detail.views} 次`)
    // 本地同步一下浏览量，省得再拉一次列表
    item.views = detail.views
  } catch (e) {
    showToastMsg('😿 ' + e.message)
  }
}

function onSelectTab(tab) {
  if (tab === activeTab.value) return
  activeTab.value = tab
  // 原先这里的「页面还没做」提示已移除：发现/收藏/我的 三个页面已实现
}

function handleLogout() {
  clearUser()
  showToastMsg('🎀 已安全退出，下次见~')
  setTimeout(() => router.replace('/login'), 700)
}

// 背景漂浮装饰
const decorations = ref([])

// 顶部提示
const toast = ref('')
const showToast = ref(false)
let toastTimer = null
function showToastMsg(msg) {
  toast.value = msg
  showToast.value = true
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => (showToast.value = false), 2200)
}

onMounted(async () => {
  const icons = ['💗', '🎀', '✨', '💖', '🌸']
  for (let i = 0; i < 14; i++) {
    decorations.value.push({
      icon: icons[Math.floor(Math.random() * icons.length)],
      left: Math.random() * 100,
      size: 13 + Math.random() * 18,
      duration: 9 + Math.random() * 9,
      delay: Math.random() * 12
    })
  }

  // 三个请求互不依赖，并行发
  await Promise.all([loadCategories(), loadStats(), loadItems()])
})
</script>

<template>
  <div class="page">
    <!-- 背景漂浮装饰 -->
    <div class="bg">
      <div
        v-for="(d, i) in decorations"
        :key="i"
        class="heart"
        :style="{
          left: d.left + 'vw',
          fontSize: d.size + 'px',
          animationDuration: d.duration + 's',
          animationDelay: d.delay + 's'
        }"
      >
        {{ d.icon }}
      </div>
    </div>

    <div class="toast" :class="{ show: showToast }">{{ toast }}</div>

    <!-- 导航栏（含搜索） -->
    <HomeNav
      :user="user"
      :keyword="keyword"
      :active="activeTab"
      @update:keyword="keyword = $event"
      @select-tab="onSelectTab"
      @logout="handleLogout"
    />

    <main class="container">
      <!-- 首页内容 -->
      <template v-if="activeTab === '首页'">
        <!-- 欢迎横幅 -->
        <section class="hero">
          <div class="hero-text">
            <p class="hi">Hi，{{ displayName }} ♡</p>
            <h2>欢迎回到 Hello Kitty 小窝</h2>
            <p class="desc">今天也要元气满满，先挑一件让你开心的小事吧~</p>

            <div class="stats">
              <div class="stat">
                <span class="s-label">内容总数</span>
                <span class="s-value">{{ stats.itemTotal }} 条</span>
              </div>
              <div class="stat">
                <span class="s-label">分类</span>
                <span class="s-value">{{ stats.categoryTotal }} 个</span>
              </div>
              <div class="stat">
                <span class="s-label">累计点赞</span>
                <span class="s-value">{{ stats.likeTotal }}</span>
              </div>
            </div>
          </div>

          <div class="hero-kitty">
            <KittyAvatar />
            <span class="bubble">喵~</span>
          </div>
        </section>

        <!-- 分类筛选 -->
        <section class="filters">
          <div class="chips">
            <button
              v-for="c in primaryChips"
              :key="c.id"
              class="chip"
              :class="{ on: primaryId === c.id }"
              @click="selectPrimary(c.id)"
            >
              <span v-if="c.icon" class="chip-icon">{{ c.icon }}</span>
              {{ c.name }}
              <span v-if="c.id && c.count !== undefined" class="chip-num">{{ c.count }}</span>
            </button>
          </div>

          <div class="sort">
            <button
              v-for="s in sortOptions"
              :key="s.key"
              class="sort-btn"
              :class="{ on: sort === s.key }"
              @click="sort = s.key"
            >
              {{ s.label }}
            </button>
          </div>
        </section>

        <!-- 二级分类（点了一级分类后才出现） -->
        <section v-if="subCategories.length" class="filters sub">
          <div class="chips">
            <button class="chip small" :class="{ on: subId === 0 }" @click="selectSub(0)">
              本类全部
            </button>
            <button
              v-for="c in subCategories"
              :key="c.id"
              class="chip small"
              :class="{ on: subId === c.id }"
              @click="selectSub(c.id)"
            >
              <span v-if="c.icon" class="chip-icon">{{ c.icon }}</span>
              {{ c.name }}
              <span class="chip-num">{{ c.count }}</span>
            </button>
          </div>
        </section>

        <!-- 结果条 -->
        <section class="result-bar">
          <span class="count">
            <template v-if="keyword">「{{ keyword }}」· </template>
            <template v-if="currentPrimary && primaryId"> {{ currentPrimary.name }} · </template>
            共 {{ total }} 个结果
          </span>
          <button v-if="keyword || primaryId || sort !== 'default'" class="reset" @click="resetFilter">
            重置筛选
          </button>
        </section>

        <!-- 加载中 -->
        <section v-if="loading && !items.length" class="empty">
          <div class="empty-emoji">🎀</div>
          <h3>正在把可爱的小东西搬过来…</h3>
        </section>

        <!-- 后端出错 -->
        <section v-else-if="loadError && !items.length" class="empty">
          <div class="empty-emoji">😿</div>
          <h3>{{ loadError }}</h3>
          <p>确认后端已启动后点下面重试一下~</p>
          <button class="empty-btn" @click="loadItems()">重新加载</button>
        </section>

        <!-- 内容卡片 -->
        <section v-else-if="items.length" class="grid" :class="{ dim: loading }">
          <CardItem v-for="it in items" :key="it.id" :item="it" @open="openItem" />
        </section>

        <!-- 搜不到时 -->
        <section v-else class="empty">
          <div class="empty-emoji">😿</div>
          <h3>没有找到相关的小东西</h3>
          <p>换个关键词试试，或者看看全部内容~</p>
          <button class="empty-btn" @click="resetFilter">清空筛选条件</button>
        </section>

        <!-- 加载更多 -->
        <div v-if="items.length && hasNext" class="more">
          <button class="more-btn" :disabled="loading" @click="loadMore">
            {{ loading ? '加载中…' : '看看更多' }}
          </button>
        </div>
      </template>

      <!-- 新增：发现 / 收藏 / 我的 三个 tab 的真实页面 -->
      <template v-else-if="activeTab === '发现'">
        <DiscoverTab @jump="onSelectTab" />
      </template>
      <template v-else-if="activeTab === '收藏'">
        <FavoritesTab @jump="onSelectTab" />
      </template>
      <template v-else-if="activeTab === '我的'">
        <ProfileTab @jump="onSelectTab" />
      </template>

      <!-- 其他页面占位（按需求先不做） -->
      <section v-else class="placeholder">
        <div class="empty-emoji">🚧</div>
        <h3>「{{ activeTab }}」页面还在装修中</h3>
        <p>这次先只做了首页，其他页面以后再慢慢补上~</p>
        <button class="empty-btn" @click="onSelectTab('首页')">回到首页</button>
      </section>

      <footer class="footer">
        🎀 Hello Kitty Home · 数据来自后端 /api/home（Spring Boot + MyBatis-Plus + H2）
      </footer>
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(160deg, #ffd6e8 0%, #ffc2dd 35%, #ffe9f3 100%);
  position: relative;
  overflow-x: hidden;
}

/* 背景装饰 */
.bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}
.heart {
  position: absolute;
  color: #ff8ab8;
  opacity: 0.3;
  animation: floatUp 10s linear infinite;
  user-select: none;
}
@keyframes floatUp {
  0% { transform: translateY(105vh) rotate(0deg); }
  100% { transform: translateY(-10vh) rotate(360deg); }
}

.container {
  position: relative;
  z-index: 1;
  max-width: 1100px;
  margin: 0 auto;
  padding: 22px 20px 36px;
}

/* 欢迎横幅 */
.hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 26px 30px;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(255, 143, 187, 0.95), rgba(255, 79, 139, 0.95));
  box-shadow: 0 16px 34px rgba(255, 79, 139, 0.32);
  overflow: hidden;
}
.hero::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -70px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
}
.hero-text { position: relative; z-index: 1; }
.hi {
  margin: 0 0 6px;
  font-size: 13px;
  color: #ffe3ef;
  letter-spacing: 1px;
}
.hero h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #fff;
  letter-spacing: 1px;
}
.hero .desc {
  margin: 0 0 16px;
  font-size: 13px;
  color: #ffeaf3;
}
.stats { display: flex; flex-wrap: wrap; gap: 10px; }
.stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.4);
}
.s-label { font-size: 11px; color: #ffe3ef; }
.s-value { font-size: 14px; color: #fff; font-weight: bold; }

.hero-kitty {
  position: relative;
  z-index: 1;
  flex: 0 0 auto;
  transform: scale(0.9);
}
.bubble {
  position: absolute;
  bottom: 4px;
  left: -18px;
  background: #fff;
  color: #ff4f8b;
  font-size: 12px;
  font-weight: bold;
  padding: 3px 10px;
  border-radius: 50px;
  box-shadow: 0 4px 10px rgba(180, 60, 110, 0.25);
  white-space: nowrap;
}

/* 筛选 */
.filters {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin: 20px 0 14px;
}
.filters.sub { margin: -4px 0 12px; }
.chips { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 7px 16px;
  border-radius: 50px;
  border: 2px solid #ffd3e5;
  background: rgba(255, 255, 255, 0.8);
  color: #b96a8d;
  font-size: 13px;
  cursor: pointer;
  transition: 0.2s;
}
.chip:hover { border-color: #ff8ab8; color: #ff4f8b; }
.chip.on {
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  border-color: transparent;
  color: #fff;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(255, 79, 139, 0.3);
}
.chip.small { padding: 5px 13px; font-size: 12px; }
.chip-icon { font-size: 13px; }
.chip-num {
  font-size: 10px;
  padding: 0 5px;
  border-radius: 50px;
  background: rgba(255, 79, 139, 0.12);
  color: #d1689a;
}
.chip.on .chip-num { background: rgba(255, 255, 255, 0.28); color: #fff; }

/* 排序 */
.sort {
  display: flex;
  gap: 4px;
  padding: 3px;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.72);
  border: 2px solid #ffd3e5;
}
.sort-btn {
  padding: 5px 13px;
  border: none;
  border-radius: 50px;
  background: transparent;
  color: #b96a8d;
  font-size: 12px;
  cursor: pointer;
  transition: 0.2s;
}
.sort-btn:hover { color: #ff4f8b; }
.sort-btn.on {
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  font-weight: bold;
}

/* 结果条 */
.result-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin: 0 0 12px;
  min-height: 20px;
}
.count { font-size: 12px; color: #b96a8d; }
.reset {
  border: none;
  background: transparent;
  color: #ff4f8b;
  font-size: 12px;
  cursor: pointer;
  text-decoration: underline;
}

/* 卡片网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(206px, 1fr));
  gap: 16px;
  transition: opacity 0.2s;
}
.grid.dim { opacity: 0.55; }

/* 空状态 / 占位 */
.empty,
.placeholder {
  background: rgba(255, 255, 255, 0.86);
  border: 2px dashed #ffc0da;
  border-radius: 24px;
  padding: 44px 24px;
  text-align: center;
}
.empty-emoji { font-size: 44px; margin-bottom: 8px; }
.empty h3,
.placeholder h3 {
  margin: 0 0 8px;
  font-size: 16px;
  color: #ff4f8b;
}
.empty p,
.placeholder p {
  margin: 0 0 18px;
  font-size: 13px;
  color: #b98ba0;
}
.empty-btn {
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
.empty-btn:hover {
  background: linear-gradient(135deg, #ff6fa5, #ff4f8b);
  color: #fff;
  transform: translateY(-2px);
}

/* 加载更多 */
.more { display: flex; justify-content: center; margin-top: 20px; }
.more-btn {
  padding: 10px 30px;
  border: 2px solid #ffd3e5;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.9);
  color: #ff4f8b;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  transition: 0.22s;
}
.more-btn:hover:not(:disabled) {
  border-color: #ff4f8b;
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(255, 79, 139, 0.22);
}
.more-btn:disabled { opacity: 0.6; cursor: default; }

.footer {
  margin-top: 30px;
  text-align: center;
  font-size: 11px;
  color: #c98fae;
}

/* 提示 */
.toast {
  position: fixed;
  top: 82px;
  left: 50%;
  transform: translateX(-50%) translateY(-140px);
  opacity: 0;
  visibility: hidden;
  background: #ff4f8b;
  color: #fff;
  padding: 11px 24px;
  border-radius: 50px;
  font-size: 13px;
  z-index: 99;
  transition: transform 0.4s ease, opacity 0.3s ease, visibility 0.3s;
  box-shadow: 0 8px 20px rgba(255, 79, 139, 0.4);
  max-width: calc(100vw - 40px);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.toast.show {
  transform: translateX(-50%) translateY(0);
  opacity: 1;
  visibility: visible;
}

/* 响应式 */
@media (max-width: 720px) {
  .hero { flex-direction: column; align-items: flex-start; padding: 22px; }
  .hero-kitty { display: none; }
  .hero h2 { font-size: 19px; }
  .grid { grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 12px; }
  .container { padding: 16px 14px 30px; }
  .filters { gap: 8px; }
  .sort { order: 2; }
}
</style>
