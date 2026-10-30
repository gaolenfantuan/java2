<script setup>
import { watch, onBeforeUnmount } from 'vue'

const props = defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '560px' }
})

const emit = defineEmits(['close'])

function close() {
  emit('close')
}

// 弹窗打开时锁住背景滚动，按 Esc 关闭
function onKeydown(e) {
  if (e.key === 'Escape') close()
}

watch(
  () => props.open,
  (open) => {
    if (open) {
      document.addEventListener('keydown', onKeydown)
    } else {
      document.removeEventListener('keydown', onKeydown)
    }
  }
)

onBeforeUnmount(() => {
  document.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="a-fade">
      <div v-if="open" class="a-mask" @click.self="close">
        <div class="a-modal" :style="{ width }">
          <header class="a-modal-head">
            <h3>{{ title }}</h3>
            <button class="a-modal-x" title="关闭" @click="close">✕</button>
          </header>

          <div class="a-modal-body">
            <slot />
          </div>

          <footer class="a-modal-foot">
            <slot name="footer">
              <button class="a-btn ghost" @click="close">取消</button>
            </slot>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>
