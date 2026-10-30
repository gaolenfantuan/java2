<script setup>
import AdminModal from './AdminModal.vue'

defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '确认操作' },
  message: { type: String, default: '' },
  okText: { type: String, default: '确定删除' },
  loading: { type: Boolean, default: false }
})

const emit = defineEmits(['confirm', 'cancel'])
</script>

<template>
  <AdminModal :open="open" :title="title" width="420px" @close="emit('cancel')">
    <div class="confirm">
      <div class="ico">🗑️</div>
      <p>{{ message }}</p>
      <small>删除后首页就看不到它了（数据会做逻辑删除，不会真的丢）</small>
    </div>

    <template #footer>
      <button class="a-btn ghost" :disabled="loading" @click="emit('cancel')">再想想</button>
      <button class="a-btn danger" :disabled="loading" @click="emit('confirm')">
        {{ loading ? '处理中…' : okText }}
      </button>
    </template>
  </AdminModal>
</template>

<style scoped>
.confirm {
  text-align: center;
  padding: 6px 4px 2px;
}
.ico {
  font-size: 38px;
  margin-bottom: 10px;
}
.confirm p {
  margin: 0 0 8px;
  font-size: 14px;
  color: #5c3d4a;
  line-height: 1.6;
}
.confirm small {
  font-size: 11.5px;
  color: #b98ba0;
}
</style>
