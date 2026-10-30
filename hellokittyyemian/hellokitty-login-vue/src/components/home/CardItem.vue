<script setup>
const props = defineProps({
  item: { type: Object, required: true }
})

const emit = defineEmits(['open'])
</script>

<template>
  <article class="card" @click="emit('open', item)">
    <!-- 用 emoji + 渐变当封面，避免依赖外部图片资源 -->
    <div class="thumb" :style="{ background: item.bg }">
      <span class="emoji">{{ item.emoji }}</span>
      <span class="badge">{{ item.category }}</span>
    </div>

    <div class="body">
      <h3>{{ item.title }}</h3>
      <p>{{ item.desc }}</p>
      <div class="meta">
        <span class="tag">{{ item.tag }}</span>
        <span class="like">♡ {{ item.likes }}</span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.card {
  background: #fff;
  border: 2px solid #ffd9e8;
  border-radius: 22px;
  overflow: hidden;
  cursor: pointer;
  transition: 0.25s;
  display: flex;
  flex-direction: column;
}
.card:hover {
  transform: translateY(-5px);
  border-color: #ffb0cf;
  box-shadow: 0 14px 28px rgba(255, 105, 160, 0.25);
}

.thumb {
  position: relative;
  height: 118px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.emoji {
  font-size: 46px;
  filter: drop-shadow(0 4px 8px rgba(180, 80, 120, 0.25));
  transition: 0.3s;
}
.card:hover .emoji { transform: scale(1.12) rotate(-4deg); }

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

.body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}
h3 {
  margin: 0;
  font-size: 14px;
  color: #7a4b5e;
  font-weight: bold;
}
p {
  margin: 0;
  font-size: 12px;
  line-height: 1.5;
  color: #b98ba0;
  flex: 1;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}
.tag {
  font-size: 11px;
  color: #ff4f8b;
  background: #fff0f6;
  padding: 2px 8px;
  border-radius: 50px;
}
.like { font-size: 11px; color: #d79bb4; }
</style>
