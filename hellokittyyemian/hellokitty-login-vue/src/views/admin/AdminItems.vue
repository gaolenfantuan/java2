<script setup>
import { ref, reactive, computed, onMounted, inject, watch } from 'vue'
import {
  fetchAdminItems,
  fetchAdminCategories,
  createAdminItem,
  updateAdminItem,
  deleteAdminItem
} from '../../api/admin.js'
import AdminModal from '../../components/admin/AdminModal.vue'
import ConfirmDialog from '../../components/admin/ConfirmDialog.vue'

const toast = inject('adminToast', () => {})

// ---------------- 列表状态 ----------------
const categories = ref([])
const items = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const pages = ref(0)

const keyword = ref('')
const filterCategory = ref('')
const filterStatus = ref('')

const loading = ref(false)
const listError = ref('')

// 一级分类 + 它下面的二级分类，用于下拉框分组
const categoryGroups = computed(() =>
  categories.value
    .filter((c) => c.level === 1)
    .map((parent) => ({
      ...parent,
      children: categories.value.filter((c) => c.parentId === parent.id)
    }))
)

const hasFilter = computed(() => !!keyword.value || filterCategory.value !== '' || filterStatus.value !== '')

// ---------------- 接口 ----------------
async function loadCategories() {
  try {
    categories.value = await fetchAdminCategories()
  } catch (e) {
    toast(e.message, 'error')
  }
}

async function loadItems() {
  loading.value = true
  listError.value = ''
  try {
    const data = await fetchAdminItems({
      keyword: keyword.value.trim(),
      categoryId: filterCategory.value === '' ? undefined : filterCategory.value,
      status: filterStatus.value === '' ? undefined : filterStatus.value,
      page: page.value,
      size: size.value
    })
    items.value = data.list
    total.value = data.total
    pages.value = data.pages
  } catch (e) {
    listError.value = e.message
    items.value = []
    total.value = 0
    pages.value = 0
  } finally {
    loading.value = false
  }
}

// ---------------- 筛选 ----------------
let searchTimer = null
watch(keyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    loadItems()
  }, 300)
})

function onFilterChange() {
  page.value = 1
  loadItems()
}

function resetFilter() {
  keyword.value = ''
  filterCategory.value = ''
  filterStatus.value = ''
  page.value = 1
  loadItems()
}

function goPage(target) {
  if (target < 1 || target > pages.value || target === page.value) return
  page.value = target
  loadItems()
}

onMounted(async () => {
  await loadCategories()
  await loadItems()
})

// ---------------- 新增 / 编辑 ----------------
const coverPresets = [
  { name: '草莓粉', value: 'linear-gradient(135deg,#ffe3ef,#ffc9de)' },
  { name: '奶油黄', value: 'linear-gradient(135deg,#fff2d8,#ffe0b3)' },
  { name: '薄荷绿', value: 'linear-gradient(135deg,#e6f7ef,#c8ecdb)' },
  { name: '天空蓝', value: 'linear-gradient(135deg,#e8f2ff,#cfe2ff)' },
  { name: '薰衣草', value: 'linear-gradient(135deg,#f6efff,#e8dcff)' },
  { name: '奶茶棕', value: 'linear-gradient(135deg,#f3e7dd,#e6d2c3)' }
]

const emojiPresets = ['🍰', '🍮', '🍩', '🎀', '👗', '🧦', '☕', '🧸', '🎧', '🌸', '🌷', '📖']

function emptyForm() {
  return {
    title: '',
    summary: '',
    emoji: '🎀',
    tag: '',
    coverBg: coverPresets[0].value,
    categoryId: null,
    likes: 0,
    views: 0,
    isHot: 0,
    sort: 0,
    status: 1
  }
}

const formOpen = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')
const form = ref(emptyForm())

const dialogTitle = computed(() => (editingId.value ? '编辑内容' : '新增内容'))

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  formError.value = ''
  formOpen.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    title: row.title || '',
    summary: row.summary || '',
    emoji: row.emoji || '🎀',
    tag: row.tag || '',
    coverBg: row.coverBg || coverPresets[0].value,
    categoryId: row.categoryId,
    likes: row.likes ?? 0,
    views: row.views ?? 0,
    isHot: row.isHot ?? 0,
    sort: row.sort ?? 0,
    status: row.status ?? 1
  }
  formError.value = ''
  formOpen.value = true
}

function closeForm() {
  formOpen.value = false
}

async function submitForm() {
  formError.value = ''
  const f = form.value

  if (!f.title.trim()) {
    formError.value = '标题不能为空哦'
    return
  }
  if (!f.categoryId) {
    formError.value = '请选择所属分类'
    return
  }

  const payload = {
    title: f.title.trim(),
    summary: f.summary.trim(),
    emoji: f.emoji.trim(),
    tag: f.tag.trim(),
    coverBg: f.coverBg.trim(),
    categoryId: Number(f.categoryId),
    likes: Number(f.likes) || 0,
    views: Number(f.views) || 0,
    isHot: Number(f.isHot) || 0,
    sort: Number(f.sort) || 0,
    status: Number(f.status)
  }

  saving.value = true
  try {
    if (editingId.value) {
      await updateAdminItem(editingId.value, payload)
      toast('🎀 保存成功，首页已同步')
    } else {
      await createAdminItem(payload)
      toast('🎀 新增成功，首页已同步')
    }
    formOpen.value = false
    await loadItems()
  } catch (e) {
    formError.value = e.message
  } finally {
    saving.value = false
  }
}

// ---------------- 删除 ----------------
const confirmState = reactive({
  open: false,
  id: null,
  title: '',
  deleting: false
})

function askDelete(row) {
  confirmState.open = true
  confirmState.id = row.id
  confirmState.title = row.title
}

function cancelDelete() {
  confirmState.open = false
}

async function doDelete() {
  confirmState.deleting = true
  try {
    await deleteAdminItem(confirmState.id)
    toast('🗑️ 已删除')
    confirmState.open = false
    // 删掉当前页最后一条时，自动往前翻一页
    if (items.value.length === 1 && page.value > 1) page.value -= 1
    await loadItems()
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    confirmState.deleting = false
  }
}

// ---------------- 快速上下架 ----------------
const togglingId = ref(null)

async function toggleStatus(row) {
  togglingId.value = row.id
  const nextStatus = row.status === 1 ? 0 : 1
  try {
    await updateAdminItem(row.id, {
      title: row.title,
      summary: row.summary,
      emoji: row.emoji,
      tag: row.tag,
      coverBg: row.coverBg,
      categoryId: row.categoryId,
      likes: row.likes,
      views: row.views,
      isHot: row.isHot,
      sort: row.sort,
      status: nextStatus
    })
    row.status = nextStatus
    toast(nextStatus === 1 ? '✅ 已上架，首页可见' : '📦 已下架，首页不再展示')
  } catch (e) {
    toast(e.message, 'error')
  } finally {
    togglingId.value = null
  }
}
</script>

<template>
  <div class="items-page">
    <!-- 工具栏 -->
    <section class="a-card toolbar">
      <div class="filters">
        <div class="search">
          <span class="s-ico">🔍</span>
          <input v-model="keyword" class="a-input" type="search" placeholder="搜标题 / 简介 / 标签…" />
          <button v-if="keyword" class="s-clear" @click="keyword = ''">✕</button>
        </div>

        <select v-model="filterCategory" class="a-select" @change="onFilterChange">
          <option value="">全部分类</option>
          <optgroup v-for="g in categoryGroups" :key="g.id" :label="g.name">
            <option :value="g.id">{{ g.icon }} {{ g.name }}（整个一级分类）</option>
            <option v-for="c in g.children" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
          </optgroup>
        </select>

        <select v-model="filterStatus" class="a-select narrow" @change="onFilterChange">
          <option value="">全部状态</option>
          <option :value="1">已上架</option>
          <option :value="0">已下架</option>
        </select>

        <button v-if="hasFilter" class="a-btn ghost sm" @click="resetFilter">重置</button>
      </div>

      <button class="a-btn primary" @click="openCreate">＋ 新增内容</button>
    </section>

    <!-- 表格 -->
    <section class="a-card">
      <div v-if="listError" class="a-empty">
        <div class="ico">😿</div>
        <h3>{{ listError }}</h3>
        <p style="margin-bottom: 14px">确认后端已启动（localhost:8080）后重试</p>
        <button class="a-btn" @click="loadItems">重新加载</button>
      </div>

      <div v-else-if="!items.length && !loading" class="a-empty">
        <div class="ico">🫧</div>
        <h3>{{ hasFilter ? '没有符合条件的内容' : '还没有任何内容' }}</h3>
        <p>{{ hasFilter ? '换个关键词或筛选条件试试~' : '点右上角「新增内容」加一条吧~' }}</p>
      </div>

      <template v-else>
        <div class="a-table-wrap">
          <table class="a-table">
            <thead>
              <tr>
                <th style="width: 60px">封面</th>
                <th>标题</th>
                <th style="width: 150px">分类</th>
                <th style="width: 110px">数据</th>
                <th style="width: 60px">排序</th>
                <th style="width: 90px">状态</th>
                <th style="width: 130px; text-align: right">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in items" :key="row.id">
                <td>
                  <span class="thumb" :style="{ background: row.coverBg }">{{ row.emoji }}</span>
                </td>
                <td>
                  <div class="title-cell">
                    <strong>{{ row.title }}</strong>
                    <span class="sub">
                      <span v-if="row.tag" class="tag">{{ row.tag }}</span>
                      <span class="desc">{{ row.summary || '（没有简介）' }}</span>
                    </span>
                  </div>
                </td>
                <td>
                  <span class="cat">{{ row.categoryName || '—' }}</span>
                  <span v-if="row.subCategoryName" class="cat-sub">/ {{ row.subCategoryName }}</span>
                </td>
                <td class="num">♡ {{ row.likes }} · 👀 {{ row.views }}</td>
                <td class="num">{{ row.sort }}</td>
                <td>
                  <button
                    class="a-badge"
                    :class="row.status === 1 ? 'on' : 'off'"
                    :disabled="togglingId === row.id"
                    :title="row.status === 1 ? '点击下架' : '点击上架'"
                    @click="toggleStatus(row)"
                  >
                    {{ row.status === 1 ? '✅ 上架' : '📦 下架' }}
                  </button>
                  <span v-if="row.isHot === 1" class="a-badge hot" style="margin-left: 4px">🔥 热门</span>
                </td>
                <td>
                  <div class="a-actions">
                    <button class="a-btn ghost sm" @click="openEdit(row)">编辑</button>
                    <button class="a-btn ghost sm danger-text" @click="askDelete(row)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="a-pager">
          <span class="a-pager-info">
            共 {{ total }} 条 · 第 {{ page }} / {{ pages || 1 }} 页
            <span v-if="loading" style="margin-left: 6px; color: #ff8ab8">加载中…</span>
          </span>
          <div class="a-pager-btns">
            <button class="a-btn sm" :disabled="page <= 1 || loading" @click="goPage(page - 1)">← 上一页</button>
            <span class="a-pager-num">{{ page }} / {{ pages || 1 }}</span>
            <button class="a-btn sm" :disabled="page >= pages || loading" @click="goPage(page + 1)">下一页 →</button>
          </div>
        </div>
      </template>
    </section>

    <!-- 新增 / 编辑弹窗 -->
    <AdminModal :open="formOpen" :title="dialogTitle" width="640px" @close="closeForm">
      <div class="form-grid">
        <div class="a-field span-2">
          <label class="a-label">标题 <span class="req">*</span></label>
          <input v-model="form.title" class="a-input" placeholder="例如：草莓奶油蛋糕" maxlength="100" />
        </div>

        <div class="a-field span-2">
          <label class="a-label">简介</label>
          <textarea
            v-model="form.summary"
            class="a-textarea"
            placeholder="一句话介绍它，会显示在首页卡片上"
            maxlength="255"
          ></textarea>
        </div>

        <div class="a-field">
          <label class="a-label">所属分类 <span class="req">*</span></label>
          <select v-model="form.categoryId" class="a-select">
            <option :value="null" disabled>请选择分类</option>
            <optgroup v-for="g in categoryGroups" :key="g.id" :label="g.name">
              <option :value="g.id">{{ g.icon }} {{ g.name }}（一级）</option>
              <option v-for="c in g.children" :key="c.id" :value="c.id">{{ c.icon }} {{ c.name }}</option>
            </optgroup>
          </select>
          <span class="a-hint">一般选二级分类，首页点一级分类时会自动包含它</span>
        </div>

        <div class="a-field">
          <label class="a-label">标签</label>
          <input v-model="form.tag" class="a-input" placeholder="例如：#今日推荐" maxlength="50" />
        </div>

        <div class="a-field">
          <label class="a-label">封面 emoji</label>
          <input v-model="form.emoji" class="a-input" placeholder="🎀" maxlength="16" />
          <div class="presets">
            <button
              v-for="e in emojiPresets"
              :key="e"
              type="button"
              class="preset-emoji"
              :class="{ on: form.emoji === e }"
              @click="form.emoji = e"
            >
              {{ e }}
            </button>
          </div>
        </div>

        <div class="a-field">
          <label class="a-label">封面背景</label>
          <div class="bg-preview" :style="{ background: form.coverBg }">
            <span>{{ form.emoji || '🎀' }}</span>
          </div>
          <div class="presets">
            <button
              v-for="p in coverPresets"
              :key="p.name"
              type="button"
              class="preset-bg"
              :class="{ on: form.coverBg === p.value }"
              :style="{ background: p.value }"
              :title="p.name"
              @click="form.coverBg = p.value"
            ></button>
          </div>
          <input v-model="form.coverBg" class="a-input mt6" placeholder="也可以粘贴自定义 CSS 渐变" maxlength="160" />
        </div>

        <div class="a-field">
          <label class="a-label">点赞数</label>
          <input v-model.number="form.likes" class="a-input" type="number" min="0" />
        </div>

        <div class="a-field">
          <label class="a-label">浏览量</label>
          <input v-model.number="form.views" class="a-input" type="number" min="0" />
        </div>

        <div class="a-field">
          <label class="a-label">排序号</label>
          <input v-model.number="form.sort" class="a-input" type="number" />
          <span class="a-hint">越小越靠前</span>
        </div>

        <div class="a-field">
          <label class="a-label">状态</label>
          <select v-model.number="form.status" class="a-select">
            <option :value="1">✅ 上架（首页可见）</option>
            <option :value="0">📦 下架（首页隐藏）</option>
          </select>
        </div>

        <div class="a-field">
          <label class="a-label">是否热门</label>
          <select v-model.number="form.isHot" class="a-select">
            <option :value="0">普通内容</option>
            <option :value="1">🔥 热门推荐</option>
          </select>
        </div>

        <p v-if="formError" class="form-error span-2">⚠️ {{ formError }}</p>
      </div>

      <template #footer>
        <button class="a-btn ghost" :disabled="saving" @click="closeForm">取消</button>
        <button class="a-btn primary" :disabled="saving" @click="submitForm">
          {{ saving ? '保存中…' : editingId ? '保存修改' : '确定新增' }}
        </button>
      </template>
    </AdminModal>

    <!-- 删除确认 -->
    <ConfirmDialog
      :open="confirmState.open"
      title="删除内容"
      :message="`确定要删除「${confirmState.title}」吗？`"
      :loading="confirmState.deleting"
      @confirm="doDelete"
      @cancel="cancelDelete"
    />
  </div>
</template>

<style scoped>
.items-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-width: 1180px;
}

/* 工具栏 */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 16px;
}
.filters {
  display: flex;
  align-items: center;
  gap: 9px;
  flex-wrap: wrap;
  flex: 1 1 auto;
}
.search {
  position: relative;
  display: flex;
  align-items: center;
  flex: 0 1 280px;
  min-width: 190px;
}
.search .a-input {
  padding-left: 34px;
  padding-right: 30px;
}
.s-ico {
  position: absolute;
  left: 12px;
  font-size: 12px;
  opacity: 0.7;
  pointer-events: none;
}
.s-clear {
  position: absolute;
  right: 8px;
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 50%;
  background: #ffd3e5;
  color: #b96a8d;
  font-size: 10px;
  cursor: pointer;
  line-height: 1;
}
.filters .a-select {
  flex: 0 0 auto;
  width: 178px;
}
.filters .a-select.narrow {
  width: 124px;
}

/* 表格内容 */
.thumb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  font-size: 19px;
  border: 1px solid #ffe3ee;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  max-width: 340px;
}
.title-cell strong {
  font-size: 13px;
  color: #5c3d4a;
}
.title-cell .sub {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #b98ba0;
  overflow: hidden;
}
.title-cell .tag {
  flex: 0 0 auto;
  padding: 0 6px;
  border-radius: 50px;
  background: #fff0f6;
  color: #ff6fa5;
}
.title-cell .desc {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cat {
  font-size: 12.5px;
  color: #7a4b5e;
}
.cat-sub {
  font-size: 11.5px;
  color: #b98ba0;
}

.a-badge[disabled] {
  opacity: 0.6;
  cursor: default;
}
td .a-badge {
  cursor: pointer;
  border: none;
  font-family: inherit;
}

.danger-text {
  color: #d92a62;
}

/* 表单 */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.span-2 {
  grid-column: span 2;
}
@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  .span-2 {
    grid-column: span 1;
  }
}

.presets {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 6px;
}
.preset-emoji {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  border: 2px solid transparent;
  background: #fff5f9;
  font-size: 14px;
  cursor: pointer;
  line-height: 1;
  transition: 0.15s;
}
.preset-emoji:hover {
  background: #ffe9f2;
}
.preset-emoji.on {
  border-color: #ff4f8b;
  background: #fff;
}

.preset-bg {
  width: 30px;
  height: 24px;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: 0.15s;
}
.preset-bg.on {
  border-color: #ff4f8b;
  transform: scale(1.08);
}

.bg-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 62px;
  border-radius: 12px;
  border: 2px solid #ffe3ee;
  font-size: 26px;
}

.mt6 {
  margin-top: 6px;
}

.form-error {
  margin: 0;
  padding: 9px 13px;
  border-radius: 12px;
  background: #fff0f4;
  border: 1px solid #ffd0de;
  color: #d92a62;
  font-size: 12px;
}
</style>
