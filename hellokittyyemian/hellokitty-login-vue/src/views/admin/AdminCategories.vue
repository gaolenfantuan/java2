<script setup>
import { ref, reactive, computed, onMounted, inject } from 'vue'
import {
  fetchAdminCategories,
  createAdminCategory,
  updateAdminCategory,
  deleteAdminCategory
} from '../../api/admin.js'
import AdminModal from '../../components/admin/AdminModal.vue'
import ConfirmDialog from '../../components/admin/ConfirmDialog.vue'

const toast = inject('adminToast', () => {})

const categories = ref([])
const loading = ref(false)
const listError = ref('')

// 只有一级分类才能当「上级」
const roots = computed(() => categories.value.filter((c) => c.level === 1))

const summary = computed(() => {
  const level1 = roots.value.length
  const level2 = categories.value.filter((c) => c.level === 2).length
  return { level1, level2, total: categories.value.length }
})

async function load() {
  loading.value = true
  listError.value = ''
  try {
    categories.value = await fetchAdminCategories()
  } catch (e) {
    listError.value = e.message
    categories.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)

// ---------------- 新增 / 编辑 ----------------
const iconPresets = ['🍰', '🎀', '🧸', '🌸', '🍮', '🍩', '👗', '🧦', '☕', '🎧', '🌷', '📖', '✨', '🎨']

function emptyForm() {
  return { name: '', code: '', parentId: 0, icon: '🎀', sort: 0, status: 1 }
}

const formOpen = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')
const form = ref(emptyForm())

const dialogTitle = computed(() => (editingId.value ? '编辑分类' : '新增分类'))

function openCreate(parentId = 0) {
  editingId.value = null
  form.value = emptyForm()
  form.value.parentId = parentId
  formError.value = ''
  formOpen.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    name: row.name || '',
    code: row.code || '',
    parentId: row.parentId ?? 0,
    icon: row.icon || '🎀',
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

  if (!f.name.trim()) {
    formError.value = '分类名称不能为空哦'
    return
  }

  const payload = {
    name: f.name.trim(),
    code: f.code.trim(),
    parentId: Number(f.parentId) || 0,
    icon: f.icon.trim(),
    sort: Number(f.sort) || 0,
    status: Number(f.status)
  }

  saving.value = true
  try {
    if (editingId.value) {
      await updateAdminCategory(editingId.value, payload)
      toast('🎀 分类已保存')
    } else {
      await createAdminCategory(payload)
      toast('🎀 分类已新增')
    }
    formOpen.value = false
    await load()
  } catch (e) {
    formError.value = e.message
  } finally {
    saving.value = false
  }
}

// ---------------- 删除 ----------------
const confirmState = reactive({ open: false, id: null, name: '', deleting: false })

function askDelete(row) {
  confirmState.open = true
  confirmState.id = row.id
  confirmState.name = row.name
}

async function doDelete() {
  confirmState.deleting = true
  try {
    await deleteAdminCategory(confirmState.id)
    toast('🗑️ 分类已删除')
    confirmState.open = false
    await load()
  } catch (e) {
    // 有子分类或有内容时后端会拦住，这里直接把原因提示出来
    toast(e.message, 'error')
  } finally {
    confirmState.deleting = false
  }
}
</script>

<template>
  <div class="cat-page">
    <section class="a-card toolbar">
      <div class="stat-line">
        <span>共 <b>{{ summary.total }}</b> 个分类</span>
        <span class="dot">·</span>
        <span>一级 <b>{{ summary.level1 }}</b></span>
        <span class="dot">·</span>
        <span>二级 <b>{{ summary.level2 }}</b></span>
        <span class="tip">分类最多两级，删除前要先清空它下面的内容</span>
      </div>
      <button class="a-btn primary" @click="openCreate(0)">＋ 新增分类</button>
    </section>

    <section class="a-card">
      <div v-if="listError" class="a-empty">
        <div class="ico">😿</div>
        <h3>{{ listError }}</h3>
        <p style="margin-bottom: 14px">确认后端已启动（localhost:8080）后重试</p>
        <button class="a-btn" @click="load">重新加载</button>
      </div>

      <div v-else-if="!categories.length && !loading" class="a-empty">
        <div class="ico">🫧</div>
        <h3>还没有任何分类</h3>
        <p>先建一个一级分类，比如「甜点」</p>
      </div>

      <div v-else class="a-table-wrap">
        <table class="a-table">
          <thead>
            <tr>
              <th>分类名称</th>
              <th style="width: 80px">层级</th>
              <th style="width: 130px">英文标识</th>
              <th style="width: 110px">上级分类</th>
              <th style="width: 90px">内容数</th>
              <th style="width: 70px">排序</th>
              <th style="width: 80px">状态</th>
              <th style="width: 150px; text-align: right">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in categories" :key="row.id">
              <td>
                <div class="name-cell" :class="{ child: row.level === 2 }">
                  <span v-if="row.level === 2" class="branch">└</span>
                  <span class="ico">{{ row.icon || '📁' }}</span>
                  <strong>{{ row.name }}</strong>
                  <span v-if="row.itemCount" class="count">{{ row.itemCount }}</span>
                </div>
              </td>
              <td>
                <span class="a-badge" :class="row.level === 1 ? 'lv1' : 'lv2'">
                  {{ row.level === 1 ? '一级' : '二级' }}
                </span>
              </td>
              <td class="code">{{ row.code }}</td>
              <td class="num">{{ row.parentName || '—' }}</td>
              <td class="num">{{ row.itemCount }} 条</td>
              <td class="num">{{ row.sort }}</td>
              <td>
                <span class="a-badge" :class="row.status === 1 ? 'on' : 'off'">
                  {{ row.status === 1 ? '正常' : '隐藏' }}
                </span>
              </td>
              <td>
                <div class="a-actions">
                  <button v-if="row.level === 1" class="a-btn ghost sm" title="在这个分类下加子分类" @click="openCreate(row.id)">
                    ＋子类
                  </button>
                  <button class="a-btn ghost sm" @click="openEdit(row)">编辑</button>
                  <button class="a-btn ghost sm danger-text" @click="askDelete(row)">删除</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- 新增 / 编辑 -->
    <AdminModal :open="formOpen" :title="dialogTitle" width="540px" @close="closeForm">
      <div class="form-grid">
        <div class="a-field">
          <label class="a-label">分类名称 <span class="req">*</span></label>
          <input v-model="form.name" class="a-input" placeholder="例如：甜点 / 蛋糕" maxlength="50" />
        </div>

        <div class="a-field">
          <label class="a-label">上级分类</label>
          <select v-model.number="form.parentId" class="a-select">
            <option :value="0">（无，作为一级分类）</option>
            <option v-for="r in roots" :key="r.id" :value="r.id" :disabled="r.id === editingId">
              {{ r.icon }} {{ r.name }}
            </option>
          </select>
          <span class="a-hint">选了上级就是二级分类，最多两级</span>
        </div>

        <div class="a-field">
          <label class="a-label">英文标识</label>
          <input v-model="form.code" class="a-input" placeholder="留空自动生成，如 cake" maxlength="50" />
          <span class="a-hint">字母开头，只能用字母 / 数字 / 下划线</span>
        </div>

        <div class="a-field">
          <label class="a-label">图标 emoji</label>
          <input v-model="form.icon" class="a-input" placeholder="🎀" maxlength="16" />
          <div class="presets">
            <button
              v-for="e in iconPresets"
              :key="e"
              type="button"
              class="preset-emoji"
              :class="{ on: form.icon === e }"
              @click="form.icon = e"
            >
              {{ e }}
            </button>
          </div>
        </div>

        <div class="a-field">
          <label class="a-label">排序号</label>
          <input v-model.number="form.sort" class="a-input" type="number" />
          <span class="a-hint">越小越靠前</span>
        </div>

        <div class="a-field">
          <label class="a-label">状态</label>
          <select v-model.number="form.status" class="a-select">
            <option :value="1">正常（首页显示）</option>
            <option :value="0">隐藏（首页不显示）</option>
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

    <ConfirmDialog
      :open="confirmState.open"
      title="删除分类"
      :message="`确定要删除分类「${confirmState.name}」吗？`"
      :loading="confirmState.deleting"
      @confirm="doDelete"
      @cancel="confirmState.open = false"
    />
  </div>
</template>

<style scoped>
.cat-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
  max-width: 1080px;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 16px;
}

.stat-line {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-wrap: wrap;
  font-size: 12.5px;
  color: var(--a-text-sub);
}
.stat-line b {
  color: #ff4f8b;
  font-size: 13.5px;
}
.stat-line .dot {
  color: #ffc0da;
}
.stat-line .tip {
  margin-left: 10px;
  font-size: 11px;
  color: #c9a3b3;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 5px;
}
.name-cell.child {
  padding-left: 22px;
}
.branch {
  color: #ffb3cf;
  font-size: 12px;
}
.name-cell .ico {
  font-size: 14px;
}
.name-cell strong {
  font-size: 13px;
  color: #5c3d4a;
}
.name-cell.child strong {
  font-weight: 600;
  color: #7a4b5e;
}
.count {
  padding: 0 6px;
  border-radius: 50px;
  background: #fff0f6;
  color: #ff6fa5;
  font-size: 10.5px;
}

.code {
  font-family: Consolas, Monaco, monospace;
  font-size: 11.5px;
  color: #a97e90;
}

.danger-text {
  color: #d92a62;
}

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
