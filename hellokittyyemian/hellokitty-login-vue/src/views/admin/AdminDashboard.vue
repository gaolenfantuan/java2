<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useRouter } from 'vue-router'
import { fetchAdminStats, fetchAdminCategories } from '../../api/admin.js'

const router = useRouter()
const toast = inject('adminToast', () => {})

const stats = ref({
  itemTotal: 0,
  onlineTotal: 0,
  offlineTotal: 0,
  categoryTotal: 0,
  likeTotal: 0,
  viewTotal: 0,
  hotTotal: 0
})
const categories = ref([])
const loading = ref(true)
const loadError = ref('')

const cards = computed(() => [
  { key: 'itemTotal', label: '内容总数', value: stats.value.itemTotal, icon: '🍰', color: 'pink' },
  { key: 'onlineTotal', label: '已上架', value: stats.value.onlineTotal, icon: '✅', color: 'green' },
  { key: 'offlineTotal', label: '已下架', value: stats.value.offlineTotal, icon: '📦', color: 'gray' },
  { key: 'categoryTotal', label: '分类数量', value: stats.value.categoryTotal, icon: '🗂️', color: 'purple' },
  { key: 'hotTotal', label: '热门内容', value: stats.value.hotTotal, icon: '🔥', color: 'orange' },
  { key: 'likeTotal', label: '累计点赞', value: stats.value.likeTotal, icon: '💗', color: 'rose' },
  { key: 'viewTotal', label: '累计浏览', value: stats.value.viewTotal, icon: '👀', color: 'blue' }
])

// 一级分类的内容数 = 它自己挂的 + 所有子分类挂的
const distribution = computed(() => {
  const all = categories.value
  return all
    .filter((c) => c.level === 1)
    .map((root) => {
      const children = all.filter((c) => c.parentId === root.id)
      const count = (root.itemCount || 0) + children.reduce((sum, c) => sum + (c.itemCount || 0), 0)
      return { id: root.id, name: root.name, icon: root.icon, count, children: children.length }
    })
    .sort((a, b) => b.count - a.count)
})

const maxCount = computed(() => Math.max(1, ...distribution.value.map((d) => d.count)))

async function load() {
  loading.value = true
  loadError.value = ''
  try {
    const [s, c] = await Promise.all([fetchAdminStats(), fetchAdminCategories()])
    stats.value = s
    categories.value = c
  } catch (e) {
    loadError.value = e.message
  } finally {
    loading.value = false
  }
}

onMounted(load)

function go(path) {
  router.push(path)
}

function refresh() {
  load()
  toast('🎀 数据已刷新')
}
</script>

<template>
  <div class="dash">
    <!-- 欢迎条 -->
    <section class="welcome">
      <div>
        <h2>🎀 欢迎回来，数据都在这儿啦</h2>
        <p>首页展示的内容和分类都在「内容管理」「分类管理」里维护，改完刷新首页就能看到。</p>
      </div>
      <div class="welcome-btns">
        <button class="a-btn" @click="refresh">🔄 刷新数据</button>
        <button class="a-btn primary" @click="go('/admin/items')">✏️ 去管理内容</button>
      </div>
    </section>

    <div v-if="loadError" class="a-card">
      <div class="a-empty">
        <div class="ico">😿</div>
        <h3>{{ loadError }}</h3>
        <p style="margin-bottom: 14px">确认后端已启动（localhost:8080）后重试</p>
        <button class="a-btn" @click="load">重新加载</button>
      </div>
    </div>

    <template v-else>
      <!-- 统计卡片 -->
      <section class="cards">
        <div v-for="c in cards" :key="c.key" class="stat-card" :class="c.color">
          <span class="stat-ico">{{ c.icon }}</span>
          <div class="stat-body">
            <span class="stat-label">{{ c.label }}</span>
            <strong class="stat-value">{{ loading ? '—' : c.value }}</strong>
          </div>
        </div>
      </section>

      <!-- 分类分布 -->
      <section class="a-card">
        <div class="a-card-head">
          <h2>🍰 各一级分类的内容数量</h2>
          <button class="a-btn ghost sm" @click="go('/admin/categories')">管理分类 →</button>
        </div>
        <div class="a-card-body">
          <div v-if="!distribution.length" class="a-empty">
            <div class="ico">🫧</div>
            <h3>还没有分类</h3>
            <p>先去分类管理建几个吧~</p>
          </div>

          <div v-else class="bars">
            <div v-for="d in distribution" :key="d.id" class="bar-row">
              <span class="bar-name">
                <span class="bar-ico">{{ d.icon || '📁' }}</span>
                {{ d.name }}
                <small>{{ d.children }} 个子分类</small>
              </span>
              <span class="bar-track">
                <span class="bar-fill" :style="{ width: (d.count / maxCount) * 100 + '%' }"></span>
              </span>
              <span class="bar-num">{{ d.count }} 条</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 快捷入口 -->
      <section class="quick">
        <button class="quick-item" @click="go('/admin/items')">
          <span class="q-ico">🍰</span>
          <strong>内容管理</strong>
          <small>新增、编辑、删除首页展示的内容</small>
        </button>
        <button class="quick-item" @click="go('/admin/categories')">
          <span class="q-ico">🗂️</span>
          <strong>分类管理</strong>
          <small>维护一级 / 二级分类和它们的排序</small>
        </button>
        <button class="quick-item" @click="go('/home')">
          <span class="q-ico">🏠</span>
          <strong>查看前台首页</strong>
          <small>看看改动在用户眼里是什么样</small>
        </button>
      </section>
    </template>
  </div>
</template>

<style scoped>
.dash {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 1080px;
}

/* 欢迎条 */
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 18px 22px;
  border-radius: 18px;
  background: linear-gradient(135deg, rgba(255, 143, 187, 0.96), rgba(255, 79, 139, 0.96));
  box-shadow: 0 12px 28px rgba(255, 79, 139, 0.28);
  color: #fff;
}
.welcome h2 {
  margin: 0 0 5px;
  font-size: 16px;
  letter-spacing: 0.5px;
}
.welcome p {
  margin: 0;
  font-size: 12px;
  color: #ffe6f1;
  line-height: 1.6;
}
.welcome-btns {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.welcome-btns .a-btn {
  background: rgba(255, 255, 255, 0.94);
  border-color: transparent;
}

/* 统计卡片 */
.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 11px;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff;
  border: 2px solid var(--a-pink-line);
  box-shadow: 0 6px 16px rgba(255, 105, 160, 0.1);
}
.stat-ico {
  width: 38px;
  height: 38px;
  flex: 0 0 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 17px;
  background: #fff0f6;
}
.stat-card.green .stat-ico { background: #eafaf0; }
.stat-card.gray .stat-ico { background: #f4f3f6; }
.stat-card.purple .stat-ico { background: #f2f0ff; }
.stat-card.orange .stat-ico { background: #fff4e8; }
.stat-card.rose .stat-ico { background: #ffeef4; }
.stat-card.blue .stat-ico { background: #eaf4ff; }

.stat-body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.stat-label {
  font-size: 11.5px;
  color: var(--a-text-sub);
}
.stat-value {
  font-size: 20px;
  line-height: 1.25;
  color: #ff4f8b;
  font-variant-numeric: tabular-nums;
}

/* 条形图 */
.bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.bar-row {
  display: grid;
  grid-template-columns: 170px 1fr 60px;
  align-items: center;
  gap: 12px;
}
.bar-name {
  font-size: 13px;
  color: var(--a-text);
  display: flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
  overflow: hidden;
}
.bar-name small {
  font-size: 10.5px;
  color: #c9a3b3;
}
.bar-ico {
  font-size: 13px;
}
.bar-track {
  height: 10px;
  border-radius: 50px;
  background: #fff0f6;
  overflow: hidden;
}
.bar-fill {
  display: block;
  height: 100%;
  border-radius: 50px;
  background: linear-gradient(90deg, #ffa8c8, #ff4f8b);
  transition: width 0.4s ease;
  min-width: 4px;
}
.bar-num {
  font-size: 12px;
  color: var(--a-text-sub);
  text-align: right;
  font-variant-numeric: tabular-nums;
}

/* 快捷入口 */
.quick {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 16px 18px;
  border-radius: 16px;
  border: 2px dashed var(--a-pink-line);
  background: #fff;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition: 0.2s;
}
.quick-item:hover {
  border-style: solid;
  border-color: var(--a-pink);
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(255, 79, 139, 0.16);
}
.q-ico {
  font-size: 20px;
}
.quick-item strong {
  font-size: 13.5px;
  color: var(--a-pink);
}
.quick-item small {
  font-size: 11.5px;
  color: var(--a-text-sub);
  line-height: 1.5;
}

@media (max-width: 640px) {
  .bar-row {
    grid-template-columns: 120px 1fr 46px;
    gap: 8px;
  }
  .bar-name small {
    display: none;
  }
  .welcome {
    padding: 16px;
  }
}
</style>
